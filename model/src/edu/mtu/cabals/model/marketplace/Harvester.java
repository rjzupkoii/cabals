package edu.mtu.cabals.model.marketplace;

import java.awt.Point;
import java.util.List;

import edu.mtu.steppables.LandUseGeomWrapper;

/**
 * Harvesters (e.g., CF and NIPF) both share the same way of harvesting.
 */
public abstract class Harvester {
	
	private HarvestReport annualReport = new HarvestReport();
	
	/**
	 * Find the most mature patch in the given parcel that matches the size.
	 * 
	 * @param lu The parcel to examine.
	 * @param patch The size (sq.m) of the patch to be harvested.
	 * @return The points in the patch, or null if a match cannot be found.
	 */
	public List<Point> findPatch(LandUseGeomWrapper lu, double patch) {
		
		// TODO Method stub
		
		return null;
	}
	
	/**
	 * Harvest the given patch.
	 * 
	 * @param lu The parcel that the patch is in.
	 * @param patch The points that make up the patch.
	 * @return The report on the harvest.
	 */
	protected HarvestReport harvest(LandUseGeomWrapper lu, List<Point> patch) {
		
		// TODO Method stub
		
		return new HarvestReport();
	}
	
	/**
	 * Get the report of harvesting.
	 */
	public HarvestReport report() { return annualReport; }
	
	/**
	 * Reset the harvest report.
	 */
	public void reset() { annualReport = new HarvestReport(); }
	
	/**
	 * Update the annual harvest report with the harvest.
	 */
	protected void update(HarvestReport harvest) {
		annualReport.labor += harvest.labor;
		annualReport.merchantable += harvest.merchantable;
		annualReport.visualImpact += harvest.visualImpact;
		annualReport.wetlandImpact += harvest.wetlandImpact;
		annualReport.woodyBiomass += harvest.woodyBiomass;
	}
}
