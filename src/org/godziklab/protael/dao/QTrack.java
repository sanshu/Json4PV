package org.godziklab.protael.dao;

import java.util.ArrayList;
import java.util.List;

public class QTrack {
	String label;
	String color;
	String type;
	List<Float> values = new ArrayList<Float>();

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<Float> getValues() {
		return values;
	}

	public void setValues(List<Float> values) {
		this.values = values;
	}

	public void addValue(Float v) {
		values.add(v);
	}

	public void addValue(float v) {
		values.add(v);
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}