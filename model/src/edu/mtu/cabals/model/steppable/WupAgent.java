package edu.mtu.cabals.model.steppable;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import edu.mtu.steppables.LandUseGeomWrapper;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.steppables.ParcelAgentType;

@SuppressWarnings("serial")
public abstract class WupAgent extends ParcelAgent {

	protected Map<LandUseGeomWrapper, Point[]> parcels;
		
	public WupAgent(ParcelAgentType type, LandUseGeomWrapper lu) {
		super(type, lu);
		
		parcels = new HashMap<LandUseGeomWrapper, Point[]>();
	}
	
	/**
	 * Register the parcel with the agent.
	 */
	public void addParcel(LandUseGeomWrapper lu, Point[] points) {
		parcels.put(lu, points);
	}
}
