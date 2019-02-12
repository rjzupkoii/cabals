package edu.mtu.cabals.model;

public enum LandfireEvh {
	HeightZeroToFive(108, 0, 5),
	HeightFiveToTen(109, 5, 10),
	HeightTenToTwentyFive(110, 10, 25),
	HeightTwentyFiveToFifty(111, 25, 50);
	
	private int code, min, max;
	
	private LandfireEvh(int code, int min, int max) {
		this.code = code;
		this.min = min;
		this.max = max;
	}
	
	/**
	 * Get the evh enum for the given code.
	 */
	public static LandfireEvh getEvh(int code) {
		for (LandfireEvh evh : values()) {
			if (evh.code == code) {
				return evh;
			}
		}
		return null;
	}
	
	public int getMin() { return min; }
	
	public int getMax() { return max; }
}
