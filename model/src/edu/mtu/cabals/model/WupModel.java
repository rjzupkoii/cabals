package edu.mtu.cabals.model;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ec.util.MersenneTwisterFast;
import edu.mtu.cabals.model.marketplace.CfHarvester;
import edu.mtu.cabals.model.marketplace.NipfHarvester;
import edu.mtu.cabals.model.marketplace.Transporter;
import edu.mtu.cabals.model.steppable.CfAgent;
import edu.mtu.cabals.model.steppable.NipfAgent;
import edu.mtu.cabals.model.steppable.WupAgent;
import edu.mtu.cabals.scorecard.WupScorecard;
import edu.mtu.policy.PolicyBase;
import edu.mtu.simulation.ForestSim;
import edu.mtu.simulation.ForestSimException;
import edu.mtu.simulation.Scorecard;
import edu.mtu.simulation.parameters.ParameterBase;
import edu.mtu.simulation.parameters.ParseParameters;
import edu.mtu.steppables.LandUseGeomWrapper;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.steppables.ParcelAgentType;
import edu.mtu.utilities.GisUtility;
import sim.field.grid.IntGrid2D;
import sim.util.Bag;
import sim.util.IntBag;
import sim.util.geo.AttributeValue;

@SuppressWarnings("serial")
public class WupModel extends ForestSim {
	
	private static Parameters parameters = null;
	private Scorecard scorecard = null;
	
	private static IntGrid2D visualBuffer;
	
	private Map<Integer, ParcelAgent> owners;
	
	public WupModel(long seed) {
		super(seed);
		
		// Prepare the mapping
		owners = new HashMap<Integer, ParcelAgent>();
		
		// Load the reference GIS files
		try {
			Parameters parameters = WupModel.getParameters();
			visualBuffer = GisUtility.importRaster(parameters.getVisualBufferRaster());
		} catch (FileNotFoundException ex) {
			System.err.println(ex);
			System.err.println(ex.getStackTrace());
			System.exit(-1);
		}
	}
	
	/**
	 * Hard override of the ForestSim createParcelAgents method since agents can 
	 * have multiple parcels under their control.
	 */
	@Override
	protected void createParcelAgents() throws ForestSimException {
		int discarded = 0;
		
		Bag geometries = getParcelLayer().getGeometries();
		List<ParcelAgent> working = new ArrayList<ParcelAgent>();
		for (int ndx = 0; ndx < geometries.numObjs; ndx++) {
			// Create the geometry for the agent and index it
			LandUseGeomWrapper geometry = (LandUseGeomWrapper)geometries.objs[ndx];
			geometry.setIndex(ndx);
			
			// Create the agent
			ParcelAgent agent = createAgent(geometry, ((ParameterBase)getModelParameters()).getEconomicAgentPercentage());
			if (agent == null) {
				discarded++;
				geometries.remove(ndx);
				ndx--;
				continue;
			}
			
			// Update the global geometry with the agents updates
			geometries.objs[ndx] = agent.getGeometry();
			
			// Schedule the agent
			working.add(agent);
		}
		
		// Reconcile the working list of agents with the actual list
		ParcelAgent[] agents = new ParcelAgent[geometries.numObjs];
		for (int ndx = 0; ndx < working.size(); ndx++) {
			agents[ndx] = working.get(ndx);
		}
		setParcelAgents(agents);
		
		// Shuffle all of the owner ids and then schedule the agents
		ArrayList<Integer> ids = new ArrayList<Integer>(owners.keySet());
		for (int ndx = ids.size() - 1; ndx > 0; ndx--) {
		      int index = random.nextInt(ndx + 1);
		      int swap = ids.get(index);
		      ids.set(index, ids.get(ndx));
		      ids.set(ndx, swap);
		    }
		for (int key : ids) {
			schedule.scheduleRepeating(owners.get(key));
		}
		
		// If we discarded anything, let the user know
		if (discarded != 0) {
			String message = "WARNING: discarded " + discarded + " parcels due to invalid geometry.";
			System.err.println(message);
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
				
		// Check to see if we have seen this owner before
		int id = lu.getIntegerAttribute("owner_id");
		if (!owners.containsKey(id)) {
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
			owners.put(id, agent);
		}
				
		// Get the agent
		WupAgent agent = (WupAgent)owners.get(id);
		
		// Note the points and add the parcel
		Point[] points = new Point[xPos.size()];
		for(int i=0; i < xPos.size(); i++) {
			points[i] = new Point(xPos.get(i), yPos.get(i));
		}
		agent.addParcel(lu, points);

		// Return the agent
		return agent;
	}
		
	protected ParcelAgent createCfAgent(LandUseGeomWrapper lu) {
		CfAgent agent = new CfAgent(ParcelAgentType.INDUSTRIAL, lu);
			
		// Set the parameters for the agent
		agent.setMinimumDbh(parameters.getCfMinimumDbh());
		agent.setReserve(parameters.getCfReserve());
		agent.setYears(parameters.getCfYears());
		
		return agent;
	}

	protected ParcelAgent createNifpAgent(LandUseGeomWrapper lu) {
		NipfAgent agent = new NipfAgent(ParcelAgentType.OTHER, lu);
		
		// Set patch size, note Gaussian means it can be zero
		double mean = parameters.getNipfStandMean();
		double sd = parameters.getNipfStandSd();
		double value = random.nextGaussian() * sd + mean;
		value = (value > 0) ? value : 0;
		agent.setTargetHarvest(value);
		
		// Set target profit, note Gaussian means it can be zero
		mean = parameters.getNipfProfitMean();
		sd = parameters.getNipfProfitSd();
		value = random.nextGaussian() * sd + mean;
		value = (value > 0) ? value : 0;
		agent.setMinimumProfit(value);
		
		// Set woody biomass target, note Gaussian means it can be zero
		mean = parameters.getNipfWoodyBiomassMean();
		sd = parameters.getNipfWoodyBiomassSd();
		value = random.nextGaussian() * sd + mean;
		value = (value > 0) ? value : 0;
		agent.setWoodyBiomassBid(value);
		
		// Set the target DBH
		agent.setMinimumDbh(parameters.getNipfMinimumDbh());
		
		return agent;
	}	
	
	/**
	 * Hard override of ForestSim initializeMarketplace since we are going to 
	 * manage creation of the marketplace ourself.
	 */
	@Override
	protected void initializeMarketplace() throws ForestSimException {
		TimberMarketplace tm = TimberMarketplace.getInstance();
		tm.load(parameters.getPricesFile(), random);
		
		CfHarvester.getInstance().setRandom(random);
		CfHarvester.getInstance().setMarkup(parameters.getMarkup());
		CfHarvester.getInstance().setAnnualHarvestLimit(parameters.getHarvestHours());
		CfHarvester.getInstance().setWoodyBiomassRetention(parameters.getWoodyBiomassRetention());
		
		NipfHarvester.getInstance().setRandom(random);
		NipfHarvester.getInstance().setMarkup(parameters.getMarkup());
		NipfHarvester.getInstance().setAnnualHarvestLimit(parameters.getHarvestHours());
		NipfHarvester.getInstance().setWoodyBiomassRetention(parameters.getWoodyBiomassRetention());
		
		Transporter transporter = Transporter.getInstance();
		transporter.setCapacity(parameters.getChipVanCapacity());
		transporter.setDieselPerLiter(parameters.getDriverPerHour());
		transporter.setDriverPerHour(parameters.getDriverPerHour());
		transporter.setKmPerHour(parameters.getKmPerHour());
		transporter.setKmPerLiter(parameters.getKmPerLiter());
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
		
	public static Parameters getParameters() {
		// Load the parameters if need be
		if (parameters == null) {
			try {
				parameters = new Parameters();
				ParseParameters.read("data/settings.ini", parameters);
			} catch (ForestSimException ex) {
				System.err.println(ex);
				System.err.println(ex.getStackTrace());
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
	public void initialize() { }

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
