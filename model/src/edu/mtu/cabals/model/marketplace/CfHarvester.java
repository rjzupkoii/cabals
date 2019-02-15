package edu.mtu.cabals.model.marketplace;

import java.awt.Point;
import java.util.List;

import edu.mtu.steppables.LandUseGeomWrapper;

public class CfHarvester extends Harvester {
	
	private static CfHarvester instance;
	
	private boolean contract;
	private double groundCoverPercent;
		
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

	public double getGroundCoverPercent() {
		return groundCoverPercent;
	}
	
	public boolean underContract() {
		return contract;
	}

	public void setContract(boolean value) {
		contract = value;
	}

	public void setGroundCoverPercent(double value) {
		groundCoverPercent = value;
	}
}
