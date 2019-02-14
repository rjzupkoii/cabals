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
	private HashMap<String, Double> pulpwood;
	private HashMap<String, Double> sawlog;
	
	private TimberMarketplace() { }
	
	public static TimberMarketplace getInstance() {
		if (instance == null) {
			instance = new TimberMarketplace();
		}
		return instance;
	}
	
	/**
	 * Get the current pulpwood price for the given species.
	 */
	public double getPulpwoodPrice(WupSpecies species) {
		if (!pulpwood.containsKey(species.getName())) {
			return 0.0;
		}
		return pulpwood.get(species.getName());
	}
	
	/**
	 * Get the current sawlog price for the given species.
	 */
	public double getSawlogPrice(WupSpecies species) {
		if (!sawlog.containsKey(species.getName())) {
			return 0.0;
		}
		return sawlog.get(species.getName());
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
			pulpwood = new HashMap<String, Double>();
			sawlog = new HashMap<String, Double>();
			for (CSVRecord record : records) {
				// Select the correct map
				HashMap<String, Double> map = null;
				switch (record.get("PRODUCT")) {
				case "SAWTIMBER": map = sawlog;
					break;
				case "PULPWOOD": map = pulpwood;
					break;
				default:
					System.err.println("Unknown product type (discarding): " + record.get("PRODUCT"));
					continue;
				}
				
				// Load the data
				double mean = Double.parseDouble(record.get("MEAN"));
				double sd = Double.parseDouble(record.get("SD"));
				
				// Create the record with a randomized value
				double price = random.nextGaussian() * sd + mean;
				map.put(record.get("SPECIES"), price);
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
		pulpwood.put(species.getName(), price);
	}
	
	/**
	 * Set the current sawlog market price for the given species.
	 */
	public void setSawlogPrice(WupSpecies species, double price) {
		sawlog.put(species.getName(), price);
	}
}
