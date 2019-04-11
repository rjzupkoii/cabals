package edu.mtu.cabals.model;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import edu.mtu.cabals.model.marketplace.Harvester;
import edu.mtu.cabals.wup.WupSpecies;
import edu.mtu.environment.Stand;
import edu.mtu.measures.TimberMeasures;
import edu.mtu.simulation.ForestSimException;
import edu.mtu.utilities.Constants;

/**
 * The timber marketplace contains the basic price information and allows for 
 * variability to take place.  
 */
public class TimberMarketplace {

	private static TimberMarketplace instance;
	
	private final static int Pulpwood = 0;
	private final static int Sawlog = 1;
	private final static int Size = 0;
	private final static int Price = 1;
	
	private double woodyBiomassPrice;
	private HashMap<String, Double[][]> prices;
	
	private TimberMarketplace() { }
	
	public static TimberMarketplace getInstance() {
		if (instance == null) {
			instance = new TimberMarketplace();
		}
		return instance;
	}
	
	/**
	 * Calculate the bid for the forest stand.
	 * 
	 * @param stand The stand to bid on.
	 * @return The bid for the stand based upon market prices.
	 */
	public double calculateBid(Stand stand) {
		try {
			// Start by getting the price
			WupSpecies species = (WupSpecies)stand.dominateSpecies;
			double price = getPrice(species, stand.arithmeticMeanDiameter);
			
			
			// If there was no price, assume it's just going to get chipped
			if (price == 0) {
				double biomass = species.getAboveGroundBiomass(stand.arithmeticMeanDiameter);
				double bid = biomass * Harvester.DryToGreen * stand.numberOfTrees * woodyBiomassPrice;
				return bid;
			}
	
			// Are we looking at saw logs?
			if (isSawLog(species, stand.arithmeticMeanDiameter)) {
				double dib = (stand.arithmeticMeanDiameter - species.getBackThickness()) / Constants.InchToCentimeter;
				double length = (species.getHeight(stand.arithmeticMeanDiameter) / Constants.InchToCentimeter) / 12;
				double bid = price * (TimberMeasures.scribnerLogRule(dib, length) * stand.numberOfTrees) / 1000;
				return bid;
			}
			
			// Must have been pulpwood
			double bid = TimberMeasures.metricDbhToCord(stand.arithmeticMeanDiameter) * stand.numberOfTrees * price;
			return bid;
			
		} catch (ForestSimException ex) {
			System.err.println(ex);
			System.exit(-1);
			return -1;
		}
	}
	
	/**
	 * Return true if the DBH is saw log, false otherwise, assumed getPrice() has already been called.
	 */
	private boolean isSawLog(WupSpecies species, double dbh) {
		Double[][] chart = prices.get(species.getName());
		return (dbh >= chart[Sawlog][Size]);
	}
		
	/**
	 * Get the current price for the species at the given dbh.
	 */
	public double getPrice(WupSpecies species, double dbh) {
		// Do we have prices for the species?
		if (!prices.containsKey(species.getName())) {
			return 0;
		}
		
		// Check the prices
		Double[][] chart = prices.get(species.getName());
		if (dbh < chart[Pulpwood][Size]) { return 0; }
		if (chart[Pulpwood][Size] < dbh && dbh < chart[Sawlog][Size]) { return chart[Pulpwood][Price]; }
		return chart[Sawlog][Price];
	}
	
	/**
	 * Get the current price for woody biomass (green ton)
	 */
	public double getWoodyBiomassPrice() {
		return woodyBiomassPrice;
	}

	/**
	 * Load the prices file for the timber marketplace.
	 */
	public void load(String fileName, Random random) {
		try {
			// Setup the read
			Reader reader = new FileReader(fileName);
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
			
			// Load the data
			prices = new HashMap<String, Double[][]>();
			for (CSVRecord record : records) {
				// Is this the CWD value?
				String key = record.get("SPECIES");
				if (key.equals("CWD")) {
					woodyBiomassPrice = Double.parseDouble(record.get("MEAN"));
					continue;
				}
				
				// Have we seen this before?
				if (!prices.containsKey(key)) {
					prices.put(key, new Double[2][2]);
				}
				
				// Load the data
				double dbh = Double.parseDouble(record.get("DBH"));
				double mean = Double.parseDouble(record.get("MEAN"));
				double sd = Double.parseDouble(record.get("SD"));

				// Note the correct index, note this can break easily as well
				int index = record.get("PRODUCT").equals("SAWTIMBER") ? Sawlog : Pulpwood;
				
				// Create the record with a randomized value
				double price = random.nextGaussian() * sd + mean;
				prices.get(key)[index][Size] = dbh;
				prices.get(key)[index][Price] = price;
			}
			
		} catch (IOException ex) {
			System.err.println(ex);
			System.exit(-1);
		}
	}
	
	/**
	 * Set the current pulpwood market price for the given species.
	 */
	public void setPulpwoodPrice(WupSpecies species, double price) {
		prices.get(species.getName())[Pulpwood][Price] = price;
	}
	
	/**
	 * Set the current sawlog market price for the given species.
	 */
	public void setSawlogPrice(WupSpecies species, double price) {
		prices.get(species.getName())[Sawlog][Price] = price;
	}

	/**
	 * Set the current price for woody biomass (green ton)
	 */
	public void setWoodyBiomassPrice(double value) {
		woodyBiomassPrice = value;
	}
}
