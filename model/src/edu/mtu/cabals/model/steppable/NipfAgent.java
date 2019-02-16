package edu.mtu.cabals.model.steppable;

import java.util.ArrayList;
import java.util.List;

import edu.mtu.cabals.model.marketplace.HarvestBid;
import edu.mtu.cabals.model.marketplace.NipfHarvester;
import edu.mtu.steppables.LandUseGeomWrapper;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.steppables.ParcelAgentType;

@SuppressWarnings("serial")
public class NipfAgent extends ParcelAgent{

	private double minimumProfit;
	private double targetHarvest;
	private double woodyBiomassBid;
	
	private List<LandUseGeomWrapper> parcels;
	
	public NipfAgent(ParcelAgentType type, LandUseGeomWrapper lu) {
		super(type, lu);
		
		parcels = new ArrayList<LandUseGeomWrapper>();
		parcels.add(lu);
	}
	
	@Override
	protected void doHarvestOperation() {
		// Randomly select one of our parcels
		int ndx = state.random.nextInt(parcels.size());
		LandUseGeomWrapper lu = parcels.get(ndx);
		
		// Solicit a bid for it
		HarvestBid bid = NipfHarvester.getInstance().requestBid(lu, targetHarvest);
			
		// Request the harvest if the bid is high enough
		if ((bid.bid / targetHarvest) > minimumProfit) {
			NipfHarvester.getInstance().requestHarvest(lu, bid.patch, woodyBiomassBid);
		}		
	}

	@Override
	public void doHarvestedOperation() {	}

	@Override
	protected void doPolicyOperation() {	}

	public void addParcel(LandUseGeomWrapper lu) { parcels.add(lu); }
	
	public void setMinimumProfit(double value) { minimumProfit = value; }
	
	public void setTargetHarvest(double value) { targetHarvest = value; }

	public void setWoodyBiomassBid(double value) { woodyBiomassBid = value; }	
}
