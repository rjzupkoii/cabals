package edu.mtu.cabals.model;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import edu.mtu.utilities.GisUtility;
import sim.field.geo.GeomGridField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;

/**
 * Growth model for the agent-based LCSA proof-of-concept. The landscape
 * is initialized based upon NLCD and LANDFIRE data while growth is based
 * upon the dominate species in the stands.
 */
public class GrowthModel implements edu.mtu.environment.GrowthModel {
		
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

		// Get the relevant data from ForestSim
		Forest forest = Forest.getInstance();
		GeomGridField landCover = forest.getLandCover();
		int height = forest.getMapHeight();
		int width = forest.getMapWidth();
		double acres = forest.getAcresPerPixel();
		
		// Load the stocking guides
		stockingGuides = new HashMap<String, double[][]>();
		WupSpecies key = new AcerRebrum();
		stockingGuides.put(key.getName(), readStockingGuide(key.getDataFile(), acres));
		key = new PinusStrobus();
		stockingGuides.put(key.getName(), readStockingGuide(key.getDataFile(), acres));
		key = new BetulaAlleghaniensis();
		stockingGuides.put(key.getName(), readStockingGuide(key.getDataFile(), acres));
		
		// Load the LANDFIRE data, fail out if we can't
		IntGrid2D wupEvh = null;
		try {
			Parameters parameters = WupModel.getParameters();
			wupEvh = GisUtility.importRaster(parameters.getLandfireHeightRaster());
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
				
				// Get the LANDFIRE bounds, zero out the NLCD if we are out of parcel bounds
				LandfireEvh evh = LandfireEvh.getEvh(wupEvh.get(ndx, ndy));
				if (evh == null) {
					((IntGrid2D)landCover.getGrid()).set(ndx, ndy, 0);
					continue;
				}

				// Randomize the stand height and get the dbh
				double min = (evh.getMin() > 2) ? evh.getMin() : 2;
				double treeHeight = min + (evh.getMax() - min) * random.nextDouble();
				treeHeight = (reference.getMaximumHeight() < treeHeight) ? reference.getMaximumHeight() : treeHeight;	// Prevent sizes greater than the species
				double dbh = reference.heightToDbh(treeHeight);
				if (dbh == -1) {
					throw new IllegalArgumentException("Result of heightToDbh cannot be -1, height: " + treeHeight);
				}

				// Use the stocking to determine the number of trees per pixel, results are pre-calculated per pixel
				int count = calculateTargetStocking(reference, dbh);
				
				// Set the values on the grids
				dbhGrid.set(ndx, ndy, dbh);
				countGrid.set(ndx, ndy, count);
				ageGrid.set(ndx, ndy, (int)(dbh / reference.getDbhGrowth()));
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

	}
	
	/**
	 * Determine the number of trees that a given stand should be seeded with.
	 * 
	 * @param species The species to reference.
	 * @param dbh Mean DBH in cm for the stand.
	 * @return The number of trees for the stand.
	 */
	public int calculateTargetStocking(WupSpecies species, double dbh) {
		// Start by finding the guideline to use
		double[][] stocking = stockingGuides.get(species.getName());
		int ndx = 0;
		for (; ndx < stocking.length; ndx++) {
			// Scan until we find the break to use
			if (dbh < stocking[ndx][0]) {
				break;
			}
		}

		// Find the value for fully stocked from the guide
		int ideal = (int)(ndx > 0 ? stocking[ndx - 1][2] : stocking[0][2]); 
		
		// Adjust the value by +/- 20%
		int skew = (int)(ideal * 0.2);
		skew = random.nextInt(skew * 2 + 1) - skew;
		int result = ideal + skew;
		return result;
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
				
		// Apply the growth, assume it is U(0, mean) and species cannot exceed max
		stand.arithmeticMeanDiameter += (random.nextDouble() * species.getDbhGrowth());
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
