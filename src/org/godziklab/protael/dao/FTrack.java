package org.godziklab.protael.dao;

import java.util.ArrayList;
import java.util.List;

public class FTrack {
	String label;
	String display;
	String color;
	List<Feature> features = new ArrayList<Feature>();

	public void addFeature(Feature f) {
		features.add(f);
	}

	public List<Feature> getFeatures() {
		return features;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public String getDisplay() {
		return display;
	}

	public String getColor() {
		return color;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
