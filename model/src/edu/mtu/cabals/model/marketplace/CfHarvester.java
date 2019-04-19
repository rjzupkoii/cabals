package edu.mtu.cabals.model.marketplace;

import java.awt.Point;
import java.util.List;

import edu.mtu.cabals.model.TimberMarketplace;
import edu.mtu.steppables.LandUseGeomWrapper;

public class CfHarvester extends Harvester {
	
	private static CfHarvester instance;
	
	private boolean contract;
		
	private CfHarvester() { }
	
	/**
	 * Get an instance of this harvester.
	 */
	public static CfHarvester getInstance() {
		if (instance == null) {
			instance = new CfHarvester();
		}
		return instance;
	}
		
	protected boolean atLimit() {
		double labor = NipfHarvester.getInstance().report().labor + report().labor;
		return (labor >= getAnnualHarvestLimit());
	}
	
	/**
	 * Harvest the given patch of land. 
	 * 
	 * @param lu The parcel that is being harvested.
	 * @param patch The points that make up the patch.
	 * @return The total area harvested, in ha
	 */
	public double requestHarvest(LandUseGeomWrapper lu, List<Point> patch) {
		
		// Have we hit the limit?
		if (atLimit()) {
			return 0;
		}
		
		// Conduct the harvest
		HarvestReport report = harvest(lu, patch);
		
		// Do we have to, or want to deliver woody biomass?
		boolean biomassCollected = false;
		double value = TimberMarketplace.getInstance().getWoodyBiomassPrice() * report.biomassRecoverable;
		if (contract || value > report.biomassCost * getMarkup()) {
			Transporter.getInstance().transport(lu.getDoubleAttribute("NEAR_KM"), report.biomassRecoverable);
			biomassCollected = true;
		}
		
		// Update the annual report
		update(report, biomassCollected);
		
		// Return the harvest area
		return report.harvestedArea;
	}
	
	public boolean underContract() {
		return contract;
	}

	public void setContract(boolean value) {
		contract = value;
	}
}
