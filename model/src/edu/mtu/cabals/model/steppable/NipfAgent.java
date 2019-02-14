package edu.mtu.cabals.model.steppable;

import edu.mtu.steppables.LandUseGeomWrapper;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.steppables.ParcelAgentType;

@SuppressWarnings("serial")
public class NipfAgent extends ParcelAgent{

	private double targetHarvest;
	
	public NipfAgent(ParcelAgentType type, LandUseGeomWrapper lu) {
		super(type, lu);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doHarvestOperation() {
		
		
	}

	@Override
	public void doHarvestedOperation() {	}

	@Override
	protected void doPolicyOperation() {	}

	public void setTargetHarvest(double value) { targetHarvest = value; }
	
}
