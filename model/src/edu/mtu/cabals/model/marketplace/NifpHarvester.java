package edu.mtu.cabals.model.marketplace;

import java.awt.Point;
import java.util.List;

import edu.mtu.cabals.model.TimberMarketplace;
import edu.mtu.cabals.wup.WupSpecies;
import edu.mtu.environment.Forest;
import edu.mtu.environment.Stand;
import edu.mtu.steppables.LandUseGeomWrapper;

public class NifpHarvester extends Harvester {
	
	private static NifpHarvester instance;
		
	private NifpHarvester() { }
	
	/**
	 * Get an instance of this harvester.
	 */
	public static NifpHarvester getInstance() {
		if (instance == null) {
			instance = new NifpHarvester();
		}
		return instance;
	}

	/**
	 * Request a bit from the harvester for the given parcel and patch size.
	 * 
	 * @param lu The parcel to request the bid on.
	 * @param patch The size (sq.m) of the patch to be harvested.
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
	 * Harvest the given patch of land. 
	 * 
	 * @param lu The parcel that is being harvested.
	 * @param patch The points that make up the patch.
	 */
	public void requestHarvest(LandUseGeomWrapper lu, List<Point> patch) {
		
		// Conduct the harvest
		HarvestReport report = harvest(lu, patch);
		
		// TODO Should we deliver the woody biomass?
		
		// Update the annual report
		update(report);
	}
}
