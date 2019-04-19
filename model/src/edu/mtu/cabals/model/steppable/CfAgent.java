package edu.mtu.cabals.model.steppable;

import edu.mtu.cabals.model.marketplace.CfHarvester;
import edu.mtu.cabals.model.marketplace.HarvestBid;
import edu.mtu.steppables.LandUseGeomWrapper;
import edu.mtu.steppables.ParcelAgentType;

@SuppressWarnings("serial")
public class CfAgent extends WupAgent {
	
	private double reserve;
	private double targetPatch;
	private int years;
		
	public CfAgent(ParcelAgentType type, LandUseGeomWrapper lu) {
		super(type, lu);
	}

	@Override
	protected void doHarvestOperation() {
		// Make sure we have a patch size to work with
		calculatePatch();

		// Some industrial forests have significant land holdings in the WUP, so they 
		// will need to harvest multiple parcels to hit their target
		double target = targetPatch;
		while (target > 0) {
			// Attempt a harvest
			double harvested = harvest(target);
			target -= harvested;
			
			// If there was nothing worth harvesting, we are done for the year
			if (harvested == 0) {
				return;
			}
		}
	}
	
	// Find a parcel patch to harvest up to the target size
	private double harvest(double target) {
		// Prepare for bidding
		HarvestBid bestBid = null;
		LandUseGeomWrapper bestParcel = null;
		CfHarvester harvester = CfHarvester.getInstance();
						
		// Find the highest value patch based on stumpage
		for (LandUseGeomWrapper lu : parcels.keySet()) {
			
			double patch = (lu.getDoubleAttribute("AREA_HA") < target) ? lu.getDoubleAttribute("AREA_HA") : target;
			HarvestBid bid = harvester.requestBid(lu, parcels.get(lu), patch, getMinimumDbh());

			// Is this bid irrelevant?
			if (bid.bid == 0 || (bestBid != null && bid.bid < bestBid.bid)) {
				continue;
			}
			
			// Bid must be better, note it
			bestBid = bid;
			bestParcel = lu;
		}
				
		// Request the harvest if the patch is valid
		if (bestBid != null) {
			return CfHarvester.getInstance().requestHarvest(bestParcel, bestBid.patch);
		}
		return 0;		
	}

	@Override
	public void doHarvestedOperation() {	}

	@Override
	protected void doPolicyOperation() {	}

	/**
	 * Calculate the target patch size we are harvesting each time step
	 */
	private void calculatePatch() {

		// Return if this has been done
		if (targetPatch != 0) {
			return;
		}
		
		// Find size of all of our parcels
		double sum = 0;
		for (LandUseGeomWrapper lu : parcels.keySet()) {
			sum += lu.getDoubleAttribute("AREA_HA");
		}
		
		// Divide by the number of years we plan out for
		targetPatch = (sum * reserve) / years;
	}

	public void setYears(int value) { years = value; }

	public void setReserve(double value) { reserve = value; }
}
