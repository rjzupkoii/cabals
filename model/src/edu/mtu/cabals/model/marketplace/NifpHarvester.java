package edu.mtu.cabals.model.marketplace;

import java.awt.Point;
import java.util.List;

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
	 * Harvest the given patch of land. 
	 * 
	 * @param lu The parcel that is being harvested.
	 * @param patch The points that make up the patch.
	 * @param biomassCost The cost of woody biomass (dollars per ha).
	 */
	public void requestHarvest(LandUseGeomWrapper lu, List<Point> patch) {
		
		// Conduct the harvest
		HarvestReport report = harvest(lu, patch);
		
		// TODO Determine if woody biomass can be removed
		
		// Update the annual report
		update(report);
	}
}
