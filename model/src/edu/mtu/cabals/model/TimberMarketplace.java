package edu.mtu.cabals.model;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import edu.mtu.cabals.wup.WupSpecies;

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
