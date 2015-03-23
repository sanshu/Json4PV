package org.godziklab.protael.dao;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ProteinObj {
	String label = "";
	String sequence = "";
	SeqColors seqcolors = new SeqColors();
	boolean alidisplay = true;
	List<ALiObj> alignments = new ArrayList<ALiObj>();

	List<QTrack> qtracks = new ArrayList<QTrack>();

	FTrack overlayfeatures = new FTrack();

	List<FTrack> ftracks = new ArrayList<FTrack>();

	List<Marker> markers = new ArrayList<Marker>();

	Hashtable<String, String> properties = new Hashtable<String, String>();

	public ProteinObj() {
		overlayfeatures.setLabel("predictions");
		String coil = "#696969";;
		String helix = "red";
		String strand = "blue";
		seqcolors.addColor("H", helix);
		seqcolors.addColor("E", strand);
		seqcolors.addColor("C", coil);

		seqcolors.addColor("G", helix);
		seqcolors.addColor("I", helix);

		seqcolors.addColor("B", strand);
		seqcolors.addColor("T", coil);

		seqcolors.addColor("S", coil);
		seqcolors.addColor(" ", "#DDD");
		seqcolors.addColor("X", "orange"); // disorder
	}

	public void setAlidisplay(boolean alidisplay) {
		this.alidisplay = alidisplay;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSeqcolors(SeqColors seqcolors) {
		this.seqcolors = seqcolors;
	}

	public SeqColors getSeqcolors() {
		return seqcolors;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void addQTrack(QTrack q) {
		this.qtracks.add(q);
	}

	public void addPrediction(Feature f) {
		overlayfeatures.addFeature(f);
	}

	public void addFTrack(FTrack q) {
		this.ftracks.add(q);
	}

	public void addProperty(String key, String value) {
		properties.put(key, value);
	}

	public void addAli(ALiObj ali) {
		alignments.add(ali);
	}

	
	
	/**
	 * @return the alignments
	 */
	public List<ALiObj> getAlignments() {
		return alignments;
	}

	/**
	 * @param alignments the alignments to set
	 */
	public void setAlignments(List<ALiObj> alignments) {
		this.alignments = alignments;
	}

	/**
	 * @return the qtracks
	 */
	public List<QTrack> getQtracks() {
		return qtracks;
	}

	/**
	 * @param qtracks the qtracks to set
	 */
	public void setQtracks(List<QTrack> qtracks) {
		this.qtracks = qtracks;
	}

	/**
	 * @return the overlayfeatures
	 */
	public FTrack getOverlayfeatures() {
		return overlayfeatures;
	}

	/**
	 * @param overlayfeatures the overlayfeatures to set
	 */
	public void setOverlayfeatures(FTrack overlayfeatures) {
		this.overlayfeatures = overlayfeatures;
	}

	/**
	 * @return the ftracks
	 */
	public List<FTrack> getFtracks() {
		return ftracks;
	}

	/**
	 * @param ftracks the ftracks to set
	 */
	public void setFtracks(List<FTrack> ftracks) {
		this.ftracks = ftracks;
	}

	/**
	 * @return the markers
	 */
	public List<Marker> getMarkers() {
		return markers;
	}

	/**
	 * @param markers the markers to set
	 */
	public void setMarkers(List<Marker> markers) {
		this.markers = markers;
	}

	/**
	 * @return the properties
	 */
	public Hashtable<String, String> getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Hashtable<String, String> properties) {
		this.properties = properties;
	}

	/**
	 * @return the alidisplay
	 */
	public boolean isAlidisplay() {
		return alidisplay;
	}

	public String toJsonString() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		return gson.toJson(this);
	}
}
