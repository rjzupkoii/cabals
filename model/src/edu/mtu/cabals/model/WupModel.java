package edu.mtu.cabals.model;

import ec.util.MersenneTwisterFast;
import edu.mtu.cabals.scorecard.WupScorecard;
import edu.mtu.policy.PolicyBase;
import edu.mtu.simulation.ForestSim;
import edu.mtu.simulation.ForestSimException;
import edu.mtu.simulation.Scorecard;
import edu.mtu.simulation.parameters.ParseParameters;
import edu.mtu.steppables.LandUseGeomWrapper;
import edu.mtu.steppables.ParcelAgent;

@SuppressWarnings("serial")
public class WupModel extends ForestSim {

	private static Parameters parameters = null;
	private Scorecard scorecard = null;
	
	public WupModel(long seed) {
		super(seed);
	}

	@Override
	public ParcelAgent createEconomicAgent(MersenneTwisterFast arg0, LandUseGeomWrapper arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ParcelAgent createEcosystemsAgent(MersenneTwisterFast arg0, LandUseGeomWrapper arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDefaultCoverFile() {
		return parameters.getNlcdRaster();
	}

	@Override
	public String getDefaultOutputDirectory() {
		return parameters.getOutputDirectory();
	}

	@Override
	public String getDefaultParcelFile() {
		return parameters.getParcelShapeFile();
	}

	@Override
	public GrowthModel getGrowthModel() {
		return new GrowthModel(getRandom());
	}

	@Override
	public int getHarvestCapacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getModelParameters() {
		return getParameters();
	}
	
	public static Parameters getParameters() {
		return parameters;
	}

	@Override
	public PolicyBase getPolicy() {
		return null;
	}

	@Override
	public Scorecard getScoreCard() {
		if (scorecard == null) {
			scorecard = new WupScorecard(getOutputDirectory());
		}
		return scorecard;
	}

	@Override
	public void initialize() {
		try {
			if (parameters != null) { return; }
			parameters = new Parameters();
			ParseParameters.read("data/settings.ini", parameters);
		} catch (ForestSimException ex) {
			System.err.println(ex);
			System.exit(-1);
		}
	}

	@Override
	public boolean useAggregateHarvester() {
		// TODO Auto-generated method stub
		return false;
	}
}
