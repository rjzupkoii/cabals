package edu.mtu.cabals.model.marketplace;

import java.awt.Point;
import java.util.List;

import edu.mtu.cabals.model.TimberMarketplace;
import edu.mtu.cabals.wup.WupSpecies;
import edu.mtu.environment.Forest;
import edu.mtu.environment.Stand;
import edu.mtu.steppables.LandUseGeomWrapper;

/**
 * Harvesters (e.g., CF and NIPF) both share the same way of harvesting.
 */
public abstract class Harvester {
	
	private double woodyBiomassRetention;
	
	private HarvestReport annualReport = new HarvestReport();
	
	/**
	 * Find the most mature patch in the given parcel that matches the size.
	 * 
	 * @param lu The parcel to examine.
	 * @param patch The size (ha) of the patch to be harvested.
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
				
		// TODO Harvest the trees on the parcel
		
		// TODO Project the costs of harvesting the woody biomass
				
		return new HarvestReport();
	}
	
	/**
	 * Get the report of harvesting.
	 */
	public HarvestReport report() { return annualReport; }
	
	/**
	 * Request a bit from the harvester for the given parcel and patch size.
	 * 
	 * @param lu The parcel to request the bid on.
	 * @param patch The size (ha) of the patch to be harvested.
	 * @return The bid and the patch being bid on.
	 */
	public HarvestBid requestBid(LandUseGeomWrapper lu, double patch) {
		// Find a patch with the given size
		List<Point> points = findPatch(lu, patch);
		if (points == null) {
			return null;
		}
		
		// Prepare the bid
		HarvestBid bid = new HarvestBid();
		bid.patch = points;
		
		// Calculate the stumpage
		Forest forest = Forest.getInstance();
		TimberMarketplace marketplace = TimberMarketplace.getInstance();
		for (Point point : points) {
			Stand stand = forest.getStand(point);
			double marketPrice = marketplace.getPrice((WupSpecies)stand.dominateSpecies, stand.arithmeticMeanDiameter);
			bid.bid += stand.numberOfTrees * marketPrice;
		}
		
		// Return the bid
		return bid;
	}
	
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

	public double getWoodyBiomassRetention() { return woodyBiomassRetention; }

	public void setWoodyBiomassRetention(double value) { woodyBiomassRetention = value; }
}
