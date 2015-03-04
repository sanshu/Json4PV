package org.godziklab.protael.dao;

public class Feature {
	String regionType = "";
	int start;
	int end;
	String label;
	String color;

	public void setColor(String color) {
		this.color = color;
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

	public void setStart(int start) {
		this.start = start;
	}

	public int getStart() {
		return start;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getEnd() {
		return end;
	}

	public void setRegionType(String regionType) {
		this.regionType = regionType;
	}

	public String getRegionType() {
		return regionType;
	}
}
