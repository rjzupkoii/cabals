package edu.mtu.cabals.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import ec.util.MersenneTwisterFast;
import edu.mtu.cabals.wup.AcerRebrum;
import edu.mtu.cabals.wup.BetulaAlleghaniensis;
import edu.mtu.cabals.wup.PinusStrobus;
import edu.mtu.cabals.wup.WupSpecies;
import edu.mtu.environment.Forest;
import edu.mtu.environment.NlcdClassification;
import edu.mtu.environment.Species;
import edu.mtu.environment.Stand;
import edu.mtu.environment.StockingCondition;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomGridField.GridDataType;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.io.geo.ArcInfoASCGridImporter;

/**
 * Growth model for the agent-based LCSA proof-of-concept. The landscape
 * is initialized based upon NLCD and LANDFIRE data while growth is based
 * upon the dominate species in the stands.
 */
public class GrowthModel implements edu.mtu.environment.GrowthModel {
	
	private static Logger Log = Logger.getLogger(GrowthModel.class.getName());
	
	// The set of reference plants to use for the growth patterns, use a sparse array for this
	private final static WupSpecies[] growthPatterns;
	static {
		growthPatterns = new WupSpecies[NlcdClassification.HighestValue + 1];
		growthPatterns[NlcdClassification.DeciduousForest.getValue()] = new AcerRebrum();
		growthPatterns[NlcdClassification.EvergreenForest.getValue()] = new PinusStrobus();
		growthPatterns[NlcdClassification.WoodyWetlands.getValue()] = new BetulaAlleghaniensis();
		growthPatterns[NlcdClassification.MixedForest.getValue()] = new AcerRebrum();
	}
	
	private HashMap<String, double[][]> stockingGuides;
	private MersenneTwisterFast random;	
	
	/**
	 * Constructor.
	 */
	public GrowthModel(MersenneTwisterFast random) {
		this.random = random;
	}
	
	@Override
	public void calculateInitialStands() {
		Log.entering("GrowthModel", "calculateInitialStands");
	
		// Get the relevant data from ForestSim
		Forest forest = Forest.getInstance();
		GeomGridField landCover = forest.getLandCover();
		int height = forest.getMapHeight();
		int width = forest.getMapWidth();

		// Load the stocking guides
		stockingGuides = new HashMap<String, double[][]>();
		WupSpecies key = new AcerRebrum();
		stockingGuides.put(key.getName(), readStockingGuide(key.getDataFile(), forest.getAcresPerPixel()));
		key = new PinusStrobus();
		stockingGuides.put(key.getName(), readStockingGuide(key.getDataFile(), forest.getAcresPerPixel()));
		
		// Load the LANDFIRE data, fail out if we can't
		IntGrid2D wupEvc = null, wupEvh = null;
		try {
			Parameters parameters = WupModel.getParameters();
			wupEvc = importRaster(parameters.getLandfireCoverRaster());
			wupEvh = importRaster(parameters.getLandfireHeightRaster());
		} catch (FileNotFoundException ex) {
			System.err.println(ex);
			System.exit(-1);
		}

		// Calculate the initial stand based upon NLCD and LANDFIRE data
		DoubleGrid2D dbhGrid = new DoubleGrid2D(width, height);
		IntGrid2D ageGrid = new IntGrid2D(width, height);
		IntGrid2D countGrid = new IntGrid2D(width, height);
		for (int ndx = 0; ndx < width; ndx++) {
			for (int ndy = 0; ndy < height; ndy++) {
				// Start by checking to see if we care about this NLCD grid point
				int nlcd = ((IntGrid2D)landCover.getGrid()).get(ndx, ndy);
				if (!NlcdClassification.isWoodyBiomass(nlcd)) {
					((IntGrid2D)landCover.getGrid()).set(ndx, ndy, 0);					
					continue;
				}
				
				// Note the species
				WupSpecies reference = (WupSpecies)getSpecies(nlcd);
				
				// Get the LANDFIRE bounds
				LandfireEvh evh = LandfireEvh.getEvh(wupEvh.get(ndx, ndy));
				if (evh == null) { continue; }				
				LandfireEvc evc = LandfireEvc.getEvc(wupEvc.get(ndx, ndy));
				if (evc == null) { continue; }
				
				// Randomize the stand height and get the dbh
				double min = (evh.getMin() > 2) ? evh.getMin() : 2;
				double max = (evh.getMax() < reference.getMaximumHeight()) ? evh.getMax() : reference.getMaximumHeight(); 
				double treeHeight = min + (max - min) * random.nextDouble();
				double dbh = reference.heightToDbh(treeHeight);
				
				// Use the DBH and cover min/max bounds to approximate the number of trees, note we assume the pixel is 30x30 meters
				min = Math.floor((900 * evc.getMin()) / (Math.PI * Math.pow(dbh / 10, 2)));
				max = Math.ceil((900 * evc.getMax()) / (Math.PI * Math.pow(dbh / 10, 2)));
				int count = random.nextInt((int)(max - min) + 1) + (int)min;
				
				// Set the values on the grids
				dbhGrid.set(ndx, ndy, dbh);
				countGrid.set(ndx, ndy, count);
				ageGrid.set(ndx, ndy, (int)(dbh / reference.getDbhGrowth()));
				
				Log.fine("DBH: " + dbh + ", Count: " + count);
			}
		}
		
		// Finish setting up the geometric grids
		GeomGridField standDiameter = new GeomGridField(dbhGrid);
		standDiameter.setPixelHeight(landCover.getPixelHeight());
		standDiameter.setPixelWidth(landCover.getPixelWidth());
		standDiameter.setMBR(landCover.getMBR());
				
		// Pass the updates along to the forest
		Forest.getInstance().setStandAgeMap(ageGrid);
		Forest.getInstance().setTreeCountMap(countGrid);
		Forest.getInstance().setStandDiameterMap(standDiameter);
		
		Log.exiting("GrowthModel", "calculateInitialStands");
	}

	@Override
	public Species getSpecies(int nlcd) {
		return growthPatterns[nlcd];
	}

	@Override
	public double[][] getStockingGuide(int nlcd) {
		WupSpecies species = growthPatterns[nlcd];
		return stockingGuides.get(species.getName());
	}

	@Override
	public double[][] getStockingGuide(Species species) {
		return stockingGuides.get(species.getName());
	}

	@Override
	public Stand growStand(Stand stand) {
		
		// Get species reference
		WupSpecies species = (WupSpecies)stand.dominateSpecies;
		
		// Get the growth, assume that it is a normal distribution
		double growth = random.nextGaussian() + species.getDbhGrowth();
		growth = (growth > 0) ? growth : 0;
		
		// Apply the growth
		stand.arithmeticMeanDiameter += growth;
		stand.arithmeticMeanDiameter = (stand.arithmeticMeanDiameter > species.getMaximumDbh()) ? species.getMaximumDbh() : stand.arithmeticMeanDiameter;
		stand.age++;
		
		// Randomly thin the stand if need be
		if (stand.stocking == StockingCondition.Overstocked.getValue()) {
			double thinning = random.nextInt(10) / 100.0;
			stand.numberOfTrees -= stand.numberOfTrees * thinning;
		}
		
		return stand;		
	}

	/**
	 * Import the given raster file.
	 */
	private IntGrid2D importRaster(String fileName) throws FileNotFoundException {
		Log.fine("Importing: " + fileName);
		
		InputStream inputStream = new FileInputStream(fileName);
		GeomGridField raster = new GeomGridField();
		ArcInfoASCGridImporter.read(inputStream, GridDataType.INTEGER, raster);
		return (IntGrid2D)raster.getGrid();
	}
	
	/**
	 * Read the stocking guide for the species.
	 */
	private static double[][] readStockingGuide(String fileName, double acresPerPixel) {
		try {
			// Read the CSV file in
			Reader file = new FileReader(fileName);
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(file);
			List<double[]> working = new ArrayList<double[]>();
			for (CSVRecord record : records) {
				working.add(new double[] { 
						Double.parseDouble(record.get(0)), 
						Double.parseDouble(record.get(1)), 
						Double.parseDouble(record.get(2)) });
			}
			
			// Convert it to a matrix, scale from acres to pixels, and return
			double[][] results = new double[working.size()][3];
			for (int ndx = 0; ndx < working.size(); ndx++) {
				results[ndx][0] = working.get(ndx)[0];
				results[ndx][1] = working.get(ndx)[1];
				results[ndx][2] = working.get(ndx)[2] * acresPerPixel;
			}
			return results;
		} catch (FileNotFoundException ex) {
			System.err.println("The file indicated, '" + fileName + "', was not found");
			System.exit(-1);
			return null;
		} catch (IOException ex) {
			System.err.println("An error occured while reading the file, '" + fileName + "'");
			System.exit(-1);
			return null;
		}
	}
}
