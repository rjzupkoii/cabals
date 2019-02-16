package edu.mtu.cabals.model.steppable;

import java.util.ArrayList;
import java.util.List;

import edu.mtu.cabals.model.marketplace.CfHarvester;
import edu.mtu.cabals.model.marketplace.HarvestBid;
import edu.mtu.steppables.LandUseGeomWrapper;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.steppables.ParcelAgentType;

@SuppressWarnings("serial")
public class CfAgent extends ParcelAgent {

	private double targetPatch;
	private int years;
	private List<LandUseGeomWrapper> parcels;
	
	public CfAgent(ParcelAgentType type, LandUseGeomWrapper lu) {
		super(type, lu);

		parcels = new ArrayList<LandUseGeomWrapper>();
		parcels.add(lu);
	}

	@Override
	protected void doHarvestOperation() {
		// Make sure we have a patch size to work with
		calculatePatch();

		// Prepare for bidding
		HarvestBid bestBid = null;
		LandUseGeomWrapper bestParcel = null;
		CfHarvester harvester = CfHarvester.getInstance();
						
		// Find the highest value patch based on stumpage
		for (LandUseGeomWrapper lu : parcels) {
			
			double patch = (lu.getDoubleAttribute("AREA_HA") < targetPatch) ? lu.getDoubleAttribute("AREA_HA") : targetPatch;
			HarvestBid bid = harvester.requestBid(lu, patch);

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
			CfHarvester.getInstance().requestHarvest(bestParcel, bestBid.patch);
		}
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
		for (LandUseGeomWrapper lu : parcels) {
			sum += lu.getDoubleAttribute("AREA_HA");
		}
		
		// Divide by the number of years we plan out for
		targetPatch = sum / years;
	}

	public void setYears(int value) { years = value; }
}
