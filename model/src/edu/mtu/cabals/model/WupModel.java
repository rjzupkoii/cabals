package edu.mtu.cabals.model;

import java.io.FileNotFoundException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import ec.util.MersenneTwisterFast;
import edu.mtu.cabals.model.marketplace.CfHarvester;
import edu.mtu.cabals.model.marketplace.NipfHarvester;
import edu.mtu.cabals.model.steppable.CfAgent;
import edu.mtu.cabals.model.steppable.NipfAgent;
import edu.mtu.cabals.scorecard.WupScorecard;
import edu.mtu.policy.PolicyBase;
import edu.mtu.simulation.ForestSim;
import edu.mtu.simulation.ForestSimException;
import edu.mtu.simulation.Scorecard;
import edu.mtu.simulation.parameters.ParseParameters;
import edu.mtu.steppables.LandUseGeomWrapper;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.steppables.ParcelAgentType;
import edu.mtu.utilities.GisUtility;
import sim.field.grid.IntGrid2D;
import sim.util.IntBag;
import sim.util.geo.AttributeValue;

@SuppressWarnings("serial")
public class WupModel extends ForestSim {

	private static Logger Log = Logger.getLogger(GrowthModel.class.getName());
	
	private static Parameters parameters = null;
	private Scorecard scorecard = null;
	
	private static IntGrid2D visualBuffer;
	private static IntGrid2D wetlands;
	
	public WupModel(long seed) {
		super(seed);
		
		// Load the reference GIS files
		try {
			Parameters parameters = WupModel.getParameters();
			visualBuffer = GisUtility.importRaster(parameters.getVisualBufferRaster());
			wetlands = GisUtility.importRaster(parameters.getWetlandsRaster());
		} catch (FileNotFoundException ex) {
			System.err.println(ex);
			System.exit(-1);
		}
	}

	/**
	 * Hard override of the ForestSim createAgent method since we care about
	 * what the underlying parcel is.
	 */
	@Override
	protected ParcelAgent createAgent(LandUseGeomWrapper lu, double probablity) {
		
		// Create our parcel and make sure we have a valid geometry
		IntBag xPos = new IntBag();
		IntBag yPos = new IntBag();
		createAgentParcel(lu.geometry, xPos, yPos);
		if (xPos.size() == 0) {
			return null;
		}
		
		// Create the correct agent type for the parcel
		ParcelAgent agent = null;
		AttributeValue value = lu.getAttributes().get("type");
		switch (value.getString()) {
		case "CF": agent = createCfAgent(lu);
			break;
		case "NIPF":
		case "FF": agent = createNifpAgent(lu);
			break;
		default:
			System.err.println("Unknown parcel type: " + value.getString());
			System.exit(-1);
		}
		
		// Update the agent geometry
		agent.createCoverPoints(xPos, yPos);
		agent.getGeometry().updateShpaefile();
		return agent;
	}
		
	protected ParcelAgent createCfAgent(LandUseGeomWrapper lu) {
		CfAgent agent = new CfAgent(ParcelAgentType.INDUSTRIAL, lu);
			
		// Set the number of years to plan for
		int value = parameters.getCfYears();
		agent.setYears(value);
		
		return agent;
	}

	protected ParcelAgent createNifpAgent(LandUseGeomWrapper lu) {
		NipfAgent agent = new NipfAgent(ParcelAgentType.OTHER, lu);
		
		// Set patch size
		double mean = parameters.getNipfStandMean();
		double sd = parameters.getNipfStandSd();
		double value = random.nextGaussian() * sd + mean;
		agent.setTargetHarvest(value);
		
		// Set target profit
		mean = parameters.getNipfProftMean();
		sd = parameters.getNipfProftSd();
		value = random.nextGaussian() * sd + mean;
		agent.setMinimumProfit(value);
		
		// Set woody biomass target
		mean = parameters.getNipfWoodyBiomassMean();
		sd = parameters.getNipfWoodyBiomassSd();
		value = random.nextGaussian() * sd + mean;
		agent.setWoodyBiomassBid(value);
		
		return agent;
	}	
	
	/**
	 * Hard override of ForestSim initializeMarketplace since we are going to 
	 * manage creation of the marketplace ourself.
	 */
	@Override
	protected void initializeMarketplace() throws ForestSimException {
		CfHarvester.getInstance().setMarkup(parameters.getCfMarkup());
		NipfHarvester.getInstance().setMarkup(parameters.getNipfMarkup());
	}

	@Override
	public String getDefaultCoverFile() {
		return getParameters().getNlcdRaster();
	}

	@Override
	public String getDefaultOutputDirectory() {
		return getParameters().getOutputDirectory();
	}

	@Override
	public String getDefaultParcelFile() {
		return getParameters().getParcelShapeFile();
	}

	@Override
	public GrowthModel getGrowthModel() {
		return new GrowthModel(getRandom());
	}

	@Override
	public int getHarvestCapacity() {
		throw new IllegalAccessError("Aggregate harvester not being used.");
	}

	@Override
	public Object getModelParameters() {
		return getParameters();
	}
	
	public static IntGrid2D getVisualBuffer() {
		return visualBuffer;
	}
	
	public static IntGrid2D getWetlands() {
		return wetlands;
	}
	
	public static Parameters getParameters() {
		// Load the parameters if need be
		if (parameters == null) {
			try {
				parameters = new Parameters();
				ParseParameters.read("data/settings.ini", parameters);
			} catch (ForestSimException ex) {
				System.err.println(ex);
				System.exit(-1);
			}
		}

		// Return the parameters
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
		// Set the logging
		Log.setLevel(Level.ALL);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new SimpleFormatter());
		handler.setLevel(Level.ALL);
		Log.addHandler(handler);
		Log.fine("Logging initlized...");
	}

	@Override
	public boolean useAggregateHarvester() {
		return false;
	}
	
	// Included to conform to interface
	@Override
	public ParcelAgent createEconomicAgent(MersenneTwisterFast arg0, LandUseGeomWrapper arg1) {
		throw new IllegalAccessError("createEconomicAgent");
	}

	// Included to conform to interface
	@Override
	public ParcelAgent createEcosystemsAgent(MersenneTwisterFast arg0, LandUseGeomWrapper arg1) {
		throw new IllegalAccessError("createEcosystemsAgent");
	}
}
