package edu.mtu.cabals.scorecard;

public enum Indicators {
	CfHarvesting(0, "/CfHarvesting.csv"),
	NipfHarvesting(1, "/NipfHarvesting.csv"),
	Transport(2, "/Transport.csv");
	
	private int index;
	private String path;
	
	public final static int length = Indicators.values().length;
	
	private Indicators(int index, String path) {
		this.index = index;
		this.path = path;
	}
	
	public int index() { return index; }
	
	public String path() { return path; }
}
