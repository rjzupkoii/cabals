package edu.mtu.cabals.model;

public enum LandfireEvc {
	CoverTenToTwenty(101, 10, 20),
	CoverTwentyToThirty(102, 20, 30),
	CoverThirtyToFourty(103, 30, 40),
	CoverFourtyToFifty(104, 40, 50),
	CoverFiftyToSixty(105, 50, 60),
	CoverSixtyToSeventy(106, 60, 70),
	CoverSeventyToEighty(107, 70, 80),
	CoverEightyToNinety(108, 80, 90),
	CoverNinetyToHundered(109, 90, 100);
	
	private int code, min, max;
	
	private LandfireEvc(int code, int min, int max) {
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
	
	public int getMin() { return min; }
	
	public int getMax() { return max; }
}
