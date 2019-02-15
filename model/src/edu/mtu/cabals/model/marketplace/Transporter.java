package edu.mtu.cabals.model.marketplace;

public class Transporter {

	private static Transporter instance;	
	
	private double capacity;
	private double hourlyPay;
	private double kmph;
		
	private double distance;
	private double woodyBiomass;
	
	private Transporter() { }
	
	public double getDistance() { return distance; }

	public static Transporter getInstance() {
		if (instance == null) {
			instance = new Transporter();
		}
		return instance;
	}
	
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
		double trips = Math.ceil(woodyBiomass / capacity) * 2;
		double hours = (trips * distance) / kmph;
		double payment = hours * hourlyPay;
		return payment;
	}
	
	public double getCapacity() {
		return capacity;
	}
	
	public double getHourlyPay() {
		return hourlyPay;
	}

	public double getKmph() {
		return kmph;
	}

	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}
	
	public void setHourlyPay(double value) {
		hourlyPay = value;
	}

	public void setKmph(double value) {
		kmph = value;
	}
}
