package edu.mtu.cabals.model.marketplace;

public class HarvestReport {
	public double biomass;				// green tons
	public double merchantable;			// green tons
	public double cwd;					// green tons

	public double harvestedArea;		// ha
	public double visualImpact;			// ha
	public double wetlandImpact;		// ha
	
	public double labor;				// hours
		
	public double biomassRecoverable;	// projected green tons
	public double loggerHours;			// projected hours chipping
	public double driverHours;			// projected hours driving
	public double biomassCost;			// projected dollars for collection and transport
}
