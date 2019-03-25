package edu.mtu.cabals.model.steppable;

import edu.mtu.cabals.model.marketplace.HarvestBid;
import edu.mtu.cabals.model.marketplace.NipfHarvester;
import edu.mtu.steppables.LandUseGeomWrapper;
import edu.mtu.steppables.ParcelAgentType;

@SuppressWarnings("serial")
public class NipfAgent extends WupAgent {

	private double minimumProfit;
	private double targetHarvest;
	private double woodyBiomassBid;
		
	public NipfAgent(ParcelAgentType type, LandUseGeomWrapper lu) {
		super(type, lu);
	}
	
	@Override
	protected void doHarvestOperation() {
		// Randomly select one of our parcels
		int ndx = state.random.nextInt(parcels.size());
		LandUseGeomWrapper lu = parcels.keySet().toArray(new LandUseGeomWrapper[0])[ndx];
		
		// Solicit a bid for it
		HarvestBid bid = NipfHarvester.getInstance().requestBid(parcels.get(lu), targetHarvest);
			
		// Request the harvest if the bid is high enough
		if ((bid.bid / targetHarvest) > minimumProfit) {
			NipfHarvester.getInstance().requestHarvest(lu, bid.patch, woodyBiomassBid);
		}		
	}

	@Override
	public void doHarvestedOperation() {	}

	@Override
	protected void doPolicyOperation() {	}
	
	public void setMinimumProfit(double value) { minimumProfit = value; }
	
	public void setTargetHarvest(double value) { targetHarvest = value; }

	public void setWoodyBiomassBid(double value) { woodyBiomassBid = value; }	
}
