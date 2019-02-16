package edu.mtu.cabals.model.marketplace;

import java.awt.Point;
import java.util.List;

import edu.mtu.cabals.model.TimberMarketplace;
import edu.mtu.steppables.LandUseGeomWrapper;

public class NipfHarvester extends Harvester {
	
	private static NipfHarvester instance;

	private NipfHarvester() { }
	
	/**
	 * Get an instance of this harvester.
	 */
	public static NipfHarvester getInstance() {
		if (instance == null) {
			instance = new NipfHarvester();
		}
		return instance;
	}
	
	/**
	 * Harvest the given patch of land. 
	 * 
	 * @param lu The parcel that is being harvested.
	 * @param patch The points that make up the patch.
	 * @param woodyBiomassBid The price per ha that the owner is willing to sell woody biomass
	 */
	public void requestHarvest(LandUseGeomWrapper lu, List<Point> patch, double woodyBiomassBid) {
		
		// Conduct the harvest
		HarvestReport report = harvest(lu, patch);
		
		// Are we doing an integrated harvest?
		double value = report.biomassRecoverable * TimberMarketplace.getInstance().getWoodyBiomassPrice();
		double cost = report.biomassCost + (report.harvestedArea * woodyBiomassBid);
		if (value > cost * getMarkup()) {
			Transporter.getInstance().transport(lu.getDoubleAttribute("NEAR_KM"), report.biomassRecoverable);
		}
		
		// Update the annual report
		update(report);
	}
}
