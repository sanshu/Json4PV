package org.godziklab.json4pv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DSSPExtractor {

	public static String getDSSPFromZip(String pdbId, String zipLocation) {
		ZipFile zip;
		try {
			zip = new ZipFile(zipLocation);

			ZipEntry entry = zip.getEntry("merged" + File.separator + pdbId
					+ ".txt");

			if (entry == null){
				zip.close();
				return "";
				}

			BufferedReader input = new BufferedReader(new InputStreamReader(
					zip.getInputStream(entry)));
			String line; 
			boolean found = false;
			while ((line = input.readLine()) != null) {
				if (line.startsWith(">" + pdbId)) {
					found = true;
				} else if (found) {
					input.close();
					return line;
				}
			}
			input.close();
			zip.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getDSSP(String pdbId, String file) {
		System.out.println("Dont use me! I'm too slow (DSSPExtractor.getDSSP(String pdbId, String file) )");
		System.out.println("Extracting SS and DIS for: " + pdbId);

		BufferedReader input;
		try {
			input = new BufferedReader(new FileReader(file));
			pdbId = pdbId.toUpperCase();
			String line;
			boolean found = false;
			while ((line = input.readLine()) != null) {
				if (line.startsWith(">" + pdbId)) {
					found = true;
				} else if (found) {
					input.close();
					return line;
				}
			}
			input.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}

	/** all other functions are not used on server side **/
	public static String getSSandDis(String pdb, String chain, String dir) {
		return getSSandDis(pdb + ":" + chain, dir);
	}

	public static String getSSandDis(String pdbAndChain, String dir) {

		String res = "";

		StringBuilder ssb = new StringBuilder();
		StringBuilder dsb = new StringBuilder();
		BufferedReader input;
		try {
			String pdbId = pdbAndChain.toUpperCase();

			input = new BufferedReader(new FileReader(dir + File.separator
					+ pdbId));

			String line;
			boolean foundSS = false, foundDIS = false, h = false;

			while ((line = input.readLine()) != null) {
				h = line.startsWith(">");
				if (h) {
					foundSS = false;
					foundDIS = false;
				}

				if (line.startsWith(">" + pdbId + ":secstr")) {
					foundSS = true;
					foundDIS = false;
				} else if (line.startsWith(">" + pdbId + ":disorder")) {
					foundDIS = true;
					foundSS = false;
				} else if (foundSS) {
					ssb.append(line);
				} else if (foundDIS) {
					dsb.append(line);
				}
			}
			// System.out.println(ssb.toString());
			// System.out.println(dsb.toString());
			res = DSSPExtractor.mergeSSandDiso(ssb.toString(), dsb.toString(),
					pdbAndChain);
			input.close();
			// System.out.println(res + "\n ");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return res;
	}

	private static int overCount = 0;
	private static List<String> overlapped = new ArrayList<String>();

	private static String mergeSSandDiso(String ss, String diso, String pdb) {
		boolean overlaps = false;
		StringBuilder res = new StringBuilder(ss);
		for (int index = 0; index < diso.length(); index++) {
			if (diso.charAt(index) == 'X') {
				if (ss.charAt(index) != ' ')
					overlaps = true;
				res.setCharAt(index, 'X');
			}
		}
		if (overlaps) {
			overCount++;
			overlapped.add(pdb + "\n" + ss + "\n" + diso);
		}
		return res.toString();

	}

	private static String pdbFromHeaderNoChain(String h) {
		int j = h.indexOf(":");
		return h.substring(1, j);
	}

	private static String pdbFromHeader(String h) {
		int j = h.lastIndexOf(":");
		return h.substring(1, j);

	}

	private static void splitFile(String file, String outDir, boolean useChain)
			throws IOException {
		BufferedReader input;
		FileWriter output;

		input = new BufferedReader(new FileReader(file));
		String line, pdb = "";

		StringBuilder ssb = new StringBuilder();

		while ((line = input.readLine()) != null) {
			if (line.startsWith(">")) {
				//
				if (!pdb.isEmpty()) {
					System.out.println(outDir + pdb);
					output = new FileWriter(new File(outDir + pdb), true);
					output.append(ssb.toString() + "\n");
					output.close();
					ssb = new StringBuilder();
				} else {
					ssb = new StringBuilder();
				}
				ssb.append(line + "\n");

				if (useChain)
					pdb = DSSPExtractor.pdbFromHeader(line);
				else
					pdb = DSSPExtractor.pdbFromHeaderNoChain(line);

			} else {
				ssb.append(line);
			}
		}
		input.close();

	}

	public static void prepareData(String base) {
		String mainFile = base + File.separator + "ss_dis.txt";
		String split = base + File.separator + "split" + File.separator;
		File fs = new File(split);
		fs.mkdirs();
		String merged = base + File.separator + "merged" + File.separator;
		fs = new File(merged);
		fs.mkdirs();
		try {
			// split main files into separate files containing data for specific
			// pdb
			// and chain

			DSSPExtractor.splitFile(mainFile, split, true);

			DSSPExtractor.overCount = 0;
			DSSPExtractor.overlapped = new ArrayList<String>();

			// now we have bunch of files to process
			File folder = new File(split);
			File[] listOfFiles = folder.listFiles();

			BufferedWriter w = new BufferedWriter(new FileWriter(base
					+ File.separator + "ss_diso_merged.txt"));

			for (File f : listOfFiles) {
				// file name is PDB:A
				String pdb = f.getName();
				String mergedS = DSSPExtractor.getSSandDis(pdb, split);
				w.append(">" + pdb + "\n" + mergedS + "\n");
				BufferedWriter w1 = new BufferedWriter(new FileWriter(merged
						+ pdb + ".txt"));
				w1.append(">" + pdb + "\n" + mergedS + "\n");
				w1.close();
			}
			w.close();

			w = new BufferedWriter(new FileWriter(base + File.separator
					+ "ss_diso_overlaps.txt"));
			if (overCount > 0)
				for (String st : overlapped) {
					w.append(st + "\n");
				}
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		DSSPExtractor.prepareData("/Users/msedova/Downloads/dssp");
		System.out.println("DONE");
		// String r = DSSPExtractor.getSSandDis("9XIM", "A",
		// "/Users/msedova/Downloads/dssp/");

	}

}
