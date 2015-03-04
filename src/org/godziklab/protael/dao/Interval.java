package org.godziklab.protael.dao;

public class Interval {
	private int start;
	private int end;

	public Interval() {
	}

	public Interval(int start, int end) {
		this.end = end;
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public int getStart() {
		return start;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public void setStart(int start) {
		this.start = start;
	}
	@Override
	public String toString() {
		
		return "["+start +":"+end
				+ "]";
	}
}
