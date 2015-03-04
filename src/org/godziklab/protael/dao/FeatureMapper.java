package org.godziklab.protael.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FeatureMapper is a tool for mapping features from one sequence to another
 * 
 * @author msedova
 * 
 */
public class FeatureMapper {

	public static void map(String refSeq, String aliSeq,
			List<Interval> intervals) {
		// pattern to detect not aligned intervals
		Pattern pattern = Pattern.compile("-+");

		// first we have to find gaps in both sequences
		List<Interval> refGaps = new ArrayList<Interval>();
		Matcher m = pattern.matcher(refSeq);
		while (m.find()) {
			Interval g = new Interval(m.start(), m.end());
			refGaps.add(g);
			// System.out.println("gap:" + g.toString());
		}

	//	List<Interval> aliGaps = new ArrayList<Interval>();
		m = pattern.matcher(refSeq);
		while (m.find()) {
			Interval g = new Interval(m.start(), m.end());
			refGaps.add(g);
			// System.out.println("gap:" + g.toString());
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
