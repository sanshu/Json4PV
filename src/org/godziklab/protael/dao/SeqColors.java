package org.godziklab.protael.dao;

import java.util.Hashtable;

public class SeqColors {
	String data = "";
	/**
	 * @return the colors
	 */
	public Hashtable<String, String> getColors() {
		return colors;
	}

	/**
	 * @param colors the colors to set
	 */
	public void setColors(Hashtable<String, String> colors) {
		this.colors = colors;
	}

	Hashtable<String, String> colors = new Hashtable<String, String>();;

	public SeqColors() {	
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getData() {
		return data;
	}

	public void addColor(String dataItem, String color) {
		colors.put(dataItem, color);
	}
}
