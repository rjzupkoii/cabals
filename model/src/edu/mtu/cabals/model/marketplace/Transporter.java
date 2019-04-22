package edu.mtu.cabals.model.marketplace;

import edu.mtu.cabals.WupConstants;

public class Transporter {

	private static Transporter instance;	
	
	private double capacity;
	private double driverPerHour;
	private double dieselPerLiter;
	private double kmPerLiter;
	private double kmPerHour;
		
	private double distance;
	private double woodyBiomass;

	public static Transporter getInstance() {
		if (instance == null) {
			instance = new Transporter();
		}
		return instance;
	}

	public double getDistance() { return distance; }
	
	public double getWoodyBiomass() { return woodyBiomass; }
		
	public void reset() {
		distance = 0;
		woodyBiomass = 0;
	}
	
	public void transport(double distance, double woodyBiomass) {
		this.distance += getTotalDistance(distance, woodyBiomass);
		this.woodyBiomass += woodyBiomass;
	}
		
	/**
	 * Calculate the total distance that the biomass must be transported.
	 * 
	 * @param distance From work site to destination.
	 * @param woodyBiomass To be transported via chip van.
	 * @return Total distance traveled, round trip in km.
	 */
	public static double getTotalDistance(double distance, double woodyBiomass) {
		return Math.ceil(woodyBiomass / WupConstants.ChipVanCapacity) * distance * 2;
	}
	
	public double getCapacity() {
		return capacity;
	}

	public double getDriverPerHour() {
		return driverPerHour;
	}

	public double getDieselPerLiter() {
		return dieselPerLiter;
	}
	
	public double getKmPerLiter() {
		return kmPerLiter;
	}

	public double getKmPerHour() {
		return kmPerHour;
	}

	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}
	
	public void setDieselPerLiter(double value) {
		dieselPerLiter = value;
	}
	
	public void setDriverPerHour(double value) {
		driverPerHour = value;
	}

	public void setKmPerLiter(double value) {
		kmPerLiter = value;
	}

	public void setKmPerHour(double value) {
		kmPerHour = value;
	}
}
