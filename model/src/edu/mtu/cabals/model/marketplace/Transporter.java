package edu.mtu.cabals.model.marketplace;

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
		this.distance += distance;
		this.woodyBiomass += woodyBiomass;
	}
	
	/**
	 * Calculate the transport cost in total dollars.
	 * 
	 * @param distance to be driven (one-way) in km.
	 * @param woodyBiomass to be transported (one-way) in.
	 * @return Total payment for transport.
	 */
	public double transportCost(double distance, double woodyBiomass) {
		double totalDistance = Math.ceil(woodyBiomass / capacity) * distance * 2;
		double driverPay = (totalDistance / kmPerHour) * driverPerHour;
		double fuelCost = (totalDistance / kmPerLiter) * dieselPerLiter;
		
		return (driverPay + fuelCost);
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
