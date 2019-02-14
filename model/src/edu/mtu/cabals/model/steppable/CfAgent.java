package edu.mtu.cabals.model.steppable;

import edu.mtu.steppables.LandUseGeomWrapper;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.steppables.ParcelAgentType;

@SuppressWarnings("serial")
public class CfAgent extends ParcelAgent {

	public CfAgent(ParcelAgentType type, LandUseGeomWrapper lu) {
		super(type, lu);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doHarvestOperation() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doHarvestedOperation() {	}

	@Override
	protected void doPolicyOperation() {	}

}
