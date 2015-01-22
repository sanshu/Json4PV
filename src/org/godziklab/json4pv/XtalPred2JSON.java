package org.godziklab.json4pv;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.godziklab.protael.dao.ALiObj;
import org.godziklab.protael.dao.Feature;
import org.godziklab.protael.dao.Interval;
import org.godziklab.protael.dao.ProteinObj;
import org.godziklab.protael.dao.QTrack; 
import org.godziklab.protael.dao.SeqColors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Servlet implementation class XtalPred2JSON
 */
@WebServlet("/getjson")
public class XtalPred2JSON extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String BASE_URL = "http://ffas.burnham.org/XtalPred-cgi/download.pl?dir=d";
	private static final String PDBSS_URL = "http://www.rcsb.org/pdb/explore/sequenceText.do?structureId=3RGZ&chainId=A&format=txt";

	private static String DSSP = "";

	private static String DSSP_BASE = "";
	// sequence in FASTA
	private static final String fasta = "A.csq";

	// file with loop, helix, strand
	private static final String psipred = "A.ss2";
	// file with disordered region predicted by DISOPRED2
	private static final String disopred = "A.diso";
	// file with low complexity region predicted by SEG
	private static final String seqpred = "A.seg";

	// coiled coils region predicted by COILS
	private static final String coildpred = "A.coils";

	// transmembrane helices predicted by TMHMM
	private static final String tmhdpred = "A.tmh";

	private boolean showAli = false;

	// SIGNAL PEPTIDES signal peptites predicted by RPSP

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public XtalPred2JSON() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		long t1 = System.currentTimeMillis();
		String dir = request.getParameter("dir");
		int limit = 20;
		DSSP = getServletContext().getRealPath(
				"/WEB-INF/data/ss_diso_merged.txt");
		DSSP_BASE = getServletContext().getRealPath("/data/");

		try {
			showAli = Boolean.parseBoolean(request.getParameter("ali"));
			limit = Integer.parseInt(request.getParameter("limit"));
		} catch (Exception e) {
		}

		String url = "http://ffas.burnham.org/XtalPred-cgi/download.pl?dir="
				+ dir + "/";

		String summaryUrl = "http://ffas.burnham.org/XtalPred-cgi/download.pl?dir="
				+ dir + "&type=summary";

		String pdbaliUrl = url + "pdb101.ali";

		response.setContentType("text/plain");
		response.setHeader("Access-Control-Allow-Origin", "*");
		BufferedReader input = new BufferedReader(new InputStreamReader(
				new URL(summaryUrl).openStream()));

		PrintWriter out = response.getWriter();
		String l;

		ProteinObj protein = new ProteinObj();
		protein.setAlidisplay(showAli);

		// System.out.println("ALIDISP:" + protein.alidisplay);

		boolean found = loadFasta(protein, url, out);
		if (found) {
			loadDisopred(protein, url, out);
			loadPsipred(protein, url, out);
			loadTmmh(protein, url, out);
			loadSeg(protein, url, out);
			loadCoils(protein, url, out);
			loadSummary(protein, summaryUrl, out);
			loadPdb101(protein, pdbaliUrl, out, limit);
		}

		// Gson gson = new Gson();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		String json = gson.toJson(protein);
		out.println(json);
		long t2 = System.currentTimeMillis();
		System.out.println("Time: " + ((t2 - t1) * 0.001));
	}

	private boolean loadFasta(ProteinObj p, String url, PrintWriter out) {
		String loadUrl = url + fasta;
		System.out.println("Loading fasta file : " + loadUrl);
		// out.println("Loading fasta file : " + loadUrl);

		String l;
		StringBuilder b = new StringBuilder();
		BufferedReader input;
		try {
			input = new BufferedReader(new InputStreamReader(
					new URL(loadUrl).openStream()));

			while ((l = input.readLine()) != null) {
				// out.println(l);
				if (l.trim().isEmpty())
					continue;

				if (l.startsWith(">")) {
					p.setLabel(l.trim().substring(1));
				} else
					b.append(l);
			}
			p.setSequence(b.toString());
			return true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private boolean loadDisopred(ProteinObj p, String url, PrintWriter out) {
		String loadUrl = url + disopred;
		System.out.println("Loading disorder file : " + loadUrl);
		// out.println("Loading disorder file : " + loadUrl);

		String l;
		StringBuilder b = new StringBuilder();
		BufferedReader input;
		try {
			input = new BufferedReader(new InputStreamReader(
					new URL(loadUrl).openStream()));

			int i = 0;

			QTrack diso = new QTrack();
			diso.setColor("orange");
			diso.setLabel("Disorder");

			List<Float> values = new ArrayList<Float>();

			while ((l = input.readLine()) != null) {

				// out.println(l);
				if (i < 5) {
					i++;
					continue;
				}
				if (l.isEmpty())
					continue;
				String[] pats = l.split(" ");

				diso.addValue(Float.parseFloat(pats[pats.length - 1]));
			}
			p.addQTrack(diso);
			return true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private boolean loadPsipred(ProteinObj p, String url, PrintWriter out) {
		String loadUrl = url + psipred;
		System.out.println("Loading pripred file : " + loadUrl);
		// out.println("Loading pripred file : " + loadUrl);

		String l;

		BufferedReader r;
		try {
			r = new BufferedReader(new InputStreamReader(
					new URL(loadUrl).openStream()));

			int i = 0;

			StringBuilder b = new StringBuilder();

			QTrack coils = new QTrack();
			coils.setColor("#000");
			coils.setLabel("Coil");

			QTrack helix = new QTrack();
			helix.setColor("red");
			helix.setLabel("Helix");

			QTrack strand = new QTrack();
			strand.setColor("blue");
			strand.setLabel("Strand");

			while ((l = r.readLine()) != null) {
				if (i < 2) {
					// skip 5 lines
					i++;
					continue;
				}

				if (l.isEmpty())
					continue;

				l.trim();
				String[] parts = l.split(" +");

				try {
					// index amino_acid secondary_structure coil_prob helix_prob
					// sheet_prob
					if (parts.length > 6) {
						// line has some char which messes up splitting, need to
						// +1 to indexes

						b.append(parts[3].trim());
						coils.addValue(Float.parseFloat(parts[4]));
						helix.addValue(Float.parseFloat(parts[5]));
						strand.addValue(Float.parseFloat(parts[6]));
					} else {
						b.append(parts[2].trim());
						coils.addValue(Float.parseFloat(parts[3]));
						helix.addValue(Float.parseFloat(parts[4]));
						strand.addValue(Float.parseFloat(parts[5]));
					}
				} catch (Exception e) {
					e.printStackTrace();

				}
			}
			p.getSeqcolors().setData(b.toString());
			// p.setSeqcolors(cols);
			p.addQTrack(coils);

			p.addQTrack(helix);

			p.addQTrack(strand);
			return true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private boolean loadTmmh(ProteinObj p, String url, PrintWriter out) {
		String loadUrl = url + tmhdpred;
		System.out.println("Loading tmhmm file : " + loadUrl);
		// out.println("Loading tmhmm file : " + loadUrl);

		String l;
		BufferedReader input;
		try {
			input = new BufferedReader(new InputStreamReader(
					new URL(loadUrl).openStream()));

			while ((l = input.readLine()) != null) {
				if (l.trim().isEmpty() || l.startsWith("#"))
					continue;

				l = l.trim().replaceAll("\t", " ");
				String[] parts = l.split(" +");
				if (parts[2].trim().equalsIgnoreCase("TMhelix")) {

					int start = Integer.parseInt(parts[3].trim());

					int end = Integer.parseInt(parts[4].trim());

					Feature f = new Feature();
					f.setStart(start);
					f.setEnd(end);
					f.setColor("#9acd32");
					f.setRegionType("TRANSMEMBRANE HELICE");
					f.setLabel("TMhelix");

					p.addPrediction(f);
				}
			}
			return true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private boolean loadSeg(ProteinObj p, String url, PrintWriter out) {
		String loadUrl = url + seqpred;
		System.out.println("Loading low complexity file : " + loadUrl);

		String l;
		StringBuilder b = new StringBuilder();
		BufferedReader input;
		try {
			input = new BufferedReader(new InputStreamReader(
					new URL(loadUrl).openStream()));

			while ((l = input.readLine()) != null) {
				// out.println(l);
				if (l.trim().isEmpty())
					continue;

				if (l.startsWith(">")) {
					p.setLabel(l.trim().substring(1));
				} else
					b.append(l);
			}
			Pattern pattern = Pattern.compile("x+");
			Matcher m = pattern.matcher(b.toString());
			while (m.find()) {
				Feature f = new Feature();
				f.setStart(m.start());
				f.setEnd(m.end());
				f.setColor("#DDD");
				f.setRegionType("LOW COMPLEXITY");
				f.setLabel("Low complexity");
				p.addPrediction(f);
			}

			return true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private boolean loadCoils(ProteinObj p, String url, PrintWriter out) {
		String loadUrl = url + coildpred;
		System.out.println("Loading coils file : " + loadUrl);

		String l;
		StringBuilder b = new StringBuilder();
		BufferedReader input;
		try {
			input = new BufferedReader(new InputStreamReader(
					new URL(loadUrl).openStream()));

			while ((l = input.readLine()) != null) {
				// out.println(l);
				if (l.trim().isEmpty())
					continue;

				if (l.startsWith(">")) {
					p.setLabel(l.trim().substring(1));
				} else
					b.append(l);
			}
			Pattern pattern = Pattern.compile("x+");
			Matcher m = pattern.matcher(b.toString());
			while (m.find()) {
				Feature f = new Feature();
				f.setStart(m.start());
				f.setEnd(m.end());
				f.setColor("#444");
				f.setRegionType("Coils");
				f.setLabel("Coils");
				p.addPrediction(f);
			}

			return true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private boolean loadSummary(ProteinObj p, String loadUrl, PrintWriter out) {
		System.out.println("Loading summary file : " + loadUrl);

		String l;
		BufferedReader input;
		try {
			input = new BufferedReader(new InputStreamReader(
					new URL(loadUrl).openStream()));

			int i = 0;

			while ((l = input.readLine()) != null) {
				if (i < 3) {
					i++;
					continue;
				}
				if (l.trim().isEmpty())
					continue;

				if (l.startsWith("---")) {
					break;
				}

				else {
					String[] parts = l.split(":");
					if (parts.length > 1) {
						p.addProperty(parts[0].trim(), parts[1].trim());
					}
				}
			}

			return true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private String lineOfDOts(int length) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < length; i++)
			b.append("-");
		return b.toString();
	}

	private boolean loadPdb101(ProteinObj p, String loadUrl, PrintWriter out,
			int limit) {
		System.out.println("Loading psi-blast file : " + loadUrl);
		// out.println("Loading tmhmm file : " + loadUrl);

		String l;
		BufferedReader input;

		try {
			input = new BufferedReader(new InputStreamReader(
					new URL(loadUrl).openStream()));

			int i = 0, c = 0, start = -1;
			String label = "";
			String desc = "";

			List<Interval> gaps = new ArrayList<Interval>();

			Pattern pattern = Pattern.compile("-+");

			while ((l = input.readLine()) != null) {
				if (i < 2) {
					i++;
					continue;
				}
				if (l.startsWith(">")) {
					desc = l.substring(1).trim();
					int idx = desc.indexOf(">");

					if (idx > 0) {
						int idx2 = desc.indexOf(">", idx);
						if (idx2 > 0)
							label = desc.substring(idx, idx2);
					}
				} else if (start < 0) {
					String[] pt = l.trim().split(" +");
					if (pt.length > 0)
						start = Integer.parseInt(pt[0]);

					// System.out.println("1|" + pt[1]);

					Matcher m = pattern.matcher(pt[1].toString());
					while (m.find()) {
						Interval g = new Interval(m.start(), m.end());
						gaps.add(g);
						// System.out.println("gap:" + g.toString());
					}
				} else if (start > 0) {

					ALiObj ali = new ALiObj();
					ali.setGaps(gaps);

					if (!showAli)
						ali.setCS("ALI");

					ali.setDescription(desc);
					ali.setLabel(label);
					ali.setStart(start);

					String[] pt = l.trim().split(" +");
					String seq = pt[1].trim();
					ali.setFragmentStart(Integer.parseInt(pt[0]));

					// SS and Diso of the original PDS seq
					String data = DSSPExtractor.getDSSPFromZip(
							ali.getPdbid().toUpperCase() + ":"
									+ ali.getChain().toUpperCase(), DSSP_BASE
									+ File.separator + "merged.zip");
					
					if (data.length() > 0) {
						StringBuilder sb = new StringBuilder(data);

						// remove the very first gap (ali start)
						sb.delete(0, ali.getFragmentStart() - 1);
					
						// insert gaps from aligned PDS seq into SS&Diso
						Matcher m = pattern.matcher(seq.toString());
						List<Interval> othergaps = new ArrayList<Interval>();

						while (m.find()) {
							Interval g = new Interval(m.start(), m.end());
							othergaps.add(g);
						}

						for (int z = 0; z < othergaps.size(); z++) {
							Interval g = othergaps.get(z);
							sb.insert(g.getStart(),
									lineOfDOts(g.getEnd() - g.getStart()));
						}
			
                                                for (int z = gaps.size() - 1; z >= 0; z--) {
							Interval g = gaps.get(z);
							sb.delete(g.getStart(), g.getEnd());
						}

						data = sb.toString();

						SeqColors sq = new SeqColors();
						sq.setData(data);
						ali.setSeqColors(sq);
					}
					ali.setSequence(seq);
					// System.out.println("7|" + ali.getSequence());

					p.addAli(ali);

					start = -1;
					gaps = new ArrayList<Interval>();
					c++;
					if (c >= limit)
						break;
				}
			}
			return true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
}
