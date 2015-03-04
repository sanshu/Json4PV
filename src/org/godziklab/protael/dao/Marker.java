package org.godziklab.protael.dao;

import java.util.HashSet;
import java.util.Set;

public class Marker {
	private static transient Set<String> ALLOWED_POS = new HashSet<String>();
	private static transient Set<String> ALLOWED_TYPES = new HashSet<String>();
	static {
		ALLOWED_TYPES.add("triangle");

		ALLOWED_TYPES.add("revtriangle");

		ALLOWED_TYPES.add("stick");

		ALLOWED_TYPES.add("balloon");

		ALLOWED_TYPES.add("glycan");

		ALLOWED_TYPES.add("oliglycan");

		ALLOWED_TYPES.add("unknownglycan");

		ALLOWED_POS.add("top");
		ALLOWED_POS.add("bottom");
		ALLOWED_POS.add("center");
	}

	private String type = "stick";
	private String label;
	private String position = "top";
	private int x;

	public Marker() {
		// TODO Auto-generated constructor stub
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setPosition(String position) {
		if (ALLOWED_POS.contains(position))
			this.position = position;
		else
			this.position = "top";
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setX(int x) {
		this.x = x;
	}

	public String getLabel() {
		return label;
	}

	public String getType() {
		return type;
	}

	public int getX() {
		return x;
	}

	public String getPosition() {
		return position;
	}
}
