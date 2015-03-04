package org.godziklab.protael.dao;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ALiObj {
	String id;
	String CS = "";
	String label;
	String sequence;
	String description;
	int start;
	SeqColors seqcolors;
	String color;
	Hashtable<String, String> data = new Hashtable<String, String>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the color
	 */
	public String getColor() {
		return color;
	}

	/**
	 * @param color
	 *            the color to set
	 */
	public void setColor(String color) {
		this.color = color;
	}
	
	public Hashtable<String, String> getData(){
		return data;
	}

	private transient String pdbid;

	/**
	 * @return the seqcolors
	 */
	public SeqColors getSeqcolors() {
		return seqcolors;
	}

	/**
	 * @param seqcolors
	 *            the seqcolors to set
	 */
	public void setSeqcolors(SeqColors seqcolors) {
		this.seqcolors = seqcolors;
	}

	private transient String chain;
	transient int fragmentStart;
	transient List<Interval> gaps = new ArrayList<Interval>();

	public String getDescription() {
		return description;
	}

	public String getLabel() {
		return label;
	}
	

	public String getSequence() {
		return sequence;
	}

	public void setDescription(String description) {
		this.description = description;
		// try to get pdbid:

		int i = this.description.indexOf(">");
		if (i < 0)
			return;
		int j = this.description.indexOf(" ", i);
		String p = this.description.substring(i + 1, j);
		String ps[] = p.split("_");
		label = setPdbid(ps[0]);
		if (ps.length > 1)
			setChain(ps[1]);
		// System.out.println(pdbid);
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setSequence(String seq) {
		StringBuilder sb = new StringBuilder(seq);
		// System.out.println("2|"+seq);
		for (int i = gaps.size() - 1; i >= 0; i--) {
			Interval g = gaps.get(i);
			sb.delete(g.getStart(), g.getEnd());
		}

		this.sequence = sb.toString();
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getStart() {
		return start;
	}

	public void setCS(String cS) {
		CS = cS;
	}

	public String getCS() {
		return CS;
	}

	public void setGaps(List<Interval> gaps) {
		this.gaps = gaps;
	}

	public List<Interval> getGaps() {
		return gaps;
	}

	public void setFragmentStart(int fragmentStart) {
		this.fragmentStart = fragmentStart;
	}

	public int getFragmentStart() {
		return fragmentStart;
	}

	public SeqColors getSeqColors() {
		return seqcolors;
	}

	public void setSeqColors(SeqColors seqColors) {
		this.seqcolors = seqColors;
	}

	public String getPdbid() {
		return pdbid;
	}

	public String setPdbid(String pdbid) {
		this.pdbid = pdbid;
		return pdbid;
	}

	public String getChain() {
		return chain;
	}

	public void setChain(String chain) {
		this.chain = chain;
	}
	
	public void addData(String key, String value){
		data.put(key, value);
	}
	
	public String getData(String key){
		return data.get(key);
	}
}
