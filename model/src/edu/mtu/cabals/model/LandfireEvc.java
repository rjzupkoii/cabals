package edu.mtu.cabals.model;

public enum LandfireEvc {
	CoverTenToTwenty(101, 0.1, 0.2),
	CoverTwentyToThirty(102, 0.2, 0.3),
	CoverThirtyToFourty(103, 0.3, 0.4),
	CoverFourtyToFifty(104, 0.4, 0.5),
	CoverFiftyToSixty(105, 0.5, 0.6),
	CoverSixtyToSeventy(106, 0.6, 0.7),
	CoverSeventyToEighty(107, 0.7, 0.8),
	CoverEightyToNinety(108, 0.8, 0.9),
	CoverNinetyToHundered(109, 0.9, 1.0);
	
	private int code;
	private double min, max;
	
	private LandfireEvc(int code, double min, double max) {
		this.code = code;
		this.min = min;
		this.max = max;
	}
	
	/**
	 * Get the evc enum for the given code.
	 */
	public static LandfireEvc getEvc(int code) {
		for (LandfireEvc evc : values()) {
			if (evc.code == code) {
				return evc;
			}
		}
		return null;
	}
	
	public double getMin() { return min; }
	
	public double getMax() { return max; }
}
