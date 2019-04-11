package edu.mtu.cabals.model.marketplace;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.javatuples.Pair;

import edu.mtu.cabals.WupConstants;
import edu.mtu.cabals.model.Parameters;
import edu.mtu.cabals.model.TimberMarketplace;
import edu.mtu.cabals.model.WupModel;
import edu.mtu.environment.Forest;
import edu.mtu.environment.NlcdClassification;
import edu.mtu.environment.Stand;
import edu.mtu.steppables.LandUseGeomWrapper;
import sim.field.grid.IntGrid2D;

/**
 * Harvesters (e.g., CF and NIPF) both share the same way of harvesting.
 */
public abstract class Harvester {
	
	// Multiplier to go from dry to green ton, based on Dulys-Nusbaum et al., 2019
	public final static double DryToGreen = 2;
	
	private double markup;
	private double woodyBiomassRetention;
	private HarvestReport annualReport = new HarvestReport();
	
	private class Cell {
		public ArrayList<Point> points = new ArrayList<Point>();
	}
	
	/**
	 * Find the most mature patch in the given parcel that matches the size.
	 * 
	 * @param parcel The parcel to examine.
	 * @param patch The size (ha) of the patch to be harvested.
	 * @return The points in the patch, or null if a match cannot be found.
	 */
	protected List<Point> findPatch(final Point[] parcel, double patch) {
				
		// If the patch is greater than or equal to the size of the parcel, just return it
		double width = Math.sqrt(Forest.getInstance().getPixelArea());		// Assume square pixels
		double area = (parcel.length * Math.pow(width, 2)) / 10000;
		if (area <= patch) {
			return Arrays.asList(parcel);
		}
		
		// Start by finding our bounds
		int xMin = Integer.MAX_VALUE, yMin = Integer.MAX_VALUE, xMax = Integer.MIN_VALUE, yMax = Integer.MIN_VALUE;
		for (Point point : parcel) {
			xMin = (xMin < point.x) ? xMin : point.x;
			yMin = (yMin < point.y) ? yMin : point.y;
			xMax = (xMax > point.x) ? xMax : point.x;
			yMax = (yMax > point.y) ? yMax : point.y;
		}
		
		// Use the bounds to setup our grids
		int divisor = (int)Math.floor(10000 / width);						// 1 ha / pixel size in m
		int xBound = (int)Math.ceil((xMax - xMin) / divisor);
		int yBound = (int)Math.ceil((yMax - yMin) / divisor);
		Cell[][] patches = new Cell[xBound][yBound];
		double[][] meanDbh = new double[xBound][yBound];
		
		// Iterate through the points, sum the DBH and assign points to patches
		Forest forest = Forest.getInstance();
		for (Point point : parcel) {
			int x = point.x % divisor;
			int y = point.y % divisor;
			patches[x][y].points.add(point);
			
			Stand stand = forest.getStand(point);
			meanDbh[x][y] += stand.arithmeticMeanDiameter;
		}
		
		// Find the mean DBH and note the max
		int x = 0, y = 0;
		double max = Double.MIN_VALUE;
		for (int ndx = 0; ndx < meanDbh.length; ndx++) {
			for (int ndy = 0; ndy < meanDbh[ndx].length; ndy++) {
				meanDbh[ndx][ndy] /= patches[ndx][ndy].points.size();
				if (meanDbh[ndx][ndy] > max) {
					max = meanDbh[ndx][ndy];
					x = ndx;
					y = ndy;
				}
			}
		}
		
		return findBest(patches, meanDbh, x, y, patch);
	}
	
	/**
	 * Use a basic greedy algorithm to find the highest value patch from the current location. 
	 */
	private List<Point> findBest(Cell[][] patches, double[][] meanDbh, int ndx, int ndy, double target) {
		
		// Note the pixel area and prepare
		double area = Forest.getInstance().getPixelArea();
		List<Point> points = new ArrayList<Point>();
		double harvest = 0;
		
		while (harvest < target) {
			// Copy the points over and update the harvest
			harvest += (patches[ndx][ndy].points.size() * area) / 10000;			// Harvest in ha 
			points.addAll(patches[ndx][ndy].points);
			meanDbh[ndx][ndy] = 0;
			
			// Find the next harvest patch
			int x = 0, y = 0;
			double max = Double.MIN_VALUE;
			
			if (ndy != 0 && meanDbh[ndx][ndy - 1] > max) {
				max = meanDbh[ndx][ndy - 1];
				x = ndx; y = ndy - 1;
			}
			
			if (ndx != 0 && meanDbh[ndx - 1][ndy] > max) {
				max = meanDbh[ndx - 1][ndy];
				x = ndx - 1; y = ndy;
			}
			
			if ((ndx + 1) != meanDbh.length && meanDbh[ndx + 1][ndy] > max) {
				max = meanDbh[ndx + 1][ndy];
				x = ndx + 1; y = ndy;
			}
			
			if ((ndy + 1) != meanDbh[ndx].length && meanDbh[ndx][ndy + 1] > max) {
				max = meanDbh[ndx][ndy + 1];
				x = ndx; y = ndy + 1;
			}
			
			// Move
			ndx = x; ndy = y;
			
			// Exit if we didn't move
			if (max == Double.MIN_VALUE) {
				break;
			}
		}
		
		return points;
	}
	
	/**
	 * Harvest the given patch.
	 * 
	 * @param lu The parcel that the patch is in.
	 * @param patch The points that make up the patch.
	 * @return The report on the harvest.
	 */
	protected HarvestReport harvest(LandUseGeomWrapper lu, List<Point> patch) {

		// Get the reference GIS files
		IntGrid2D visualBuffer = WupModel.getVisualBuffer();
		IntGrid2D wetlands = WupModel.getWetlands();
		
		// Perform the initial harvest of the patch
		Forest forest = Forest.getInstance();
		Pair<Double, Double> results = forest.harvest(patch.toArray(new Point[0]));
		
		// Update the report with the results of the harvest
		HarvestReport report = new HarvestReport();
		double area = forest.getPixelArea();
		report.merchantable = (results.getValue0() / 1000);							// Stem / 1000 for metric tons
		report.merchantable *= DryToGreen;											// dry to green tons
		report.woodyBiomass = (results.getValue1() - results.getValue0()) / 1000;	// (Aboveground - Stem) / 1000 for metric tons
		report.woodyBiomass *= DryToGreen;											// dry to green tons
		report.harvestedArea = (patch.size() * area) / 10000;		// sq.m to ha
		
		// Check to see what the impacts are via GIS
		for (Point point : patch) {
			if (visualBuffer.get(point.x, point.y) != 0) {
				report.visualImpact += area;
			}
			if (wetlands.get(point.x, point.y) == NlcdClassification.WoodyWetlands.getValue()) {
				report.wetlandImpact += area;
			}
		}
		report.visualImpact /= 10000;	// sq.m to ha
		report.wetlandImpact /= 10000;	// sq.m to ha
						
		// Apply the economic calculations
		report.labor = harvestDuration(report.merchantable);
		report.biomassRecoverable = report.woodyBiomass * (1 - woodyBiomassRetention);
		results = biomassCosts(report.biomassRecoverable, lu.getDoubleAttribute("NEAR_KM"));
		report.biomassLabor = results.getValue0();
		report.biomassCost = results.getValue1();
		
		return report;
	}
	
	private double harvestDuration(double merchantable) {
		Parameters parameters = WupModel.getParameters();
		
		double hours = merchantable / parameters.getMerchantableProductivity();
		hours = Math.round(hours * 100d) / 100d;
		return hours;
	}
	
	// Returns tuple of [labor hours, total cost]
	private Pair<Double, Double> biomassCosts(double biomass, double distance) {
		Parameters parameters = WupModel.getParameters();
		
		// Calculate total hours
		double chippingHr = biomass / parameters.getBiomassChipping();
		int trips = (int)Math.ceil(biomass / WupConstants.ChipVanCapacity); 
		double chipVanHr = (distance * trips) / parameters.getKmPerHour();
		double hours = chippingHr + chipVanHr;
		
		// Calculate total costs
		double fuel = ((distance * trips) / parameters.getKmPerLiter()) * parameters.getDieselPerLiter();
		double costs = fuel + chippingHr * parameters.getLoggerPerHour() + chipVanHr * parameters.getDriverPerHour();
		
		return new Pair<Double, Double>(hours, costs);		
	}
		
	/**
	 * Get the report of harvesting.
	 */
	public HarvestReport report() { return annualReport; }
	
	/**
	 * Request a bit from the harvester for the given parcel and patch size.
	 * 
	 * @param parcel The parcel to request the bid on.
	 * @param patch The size (ha) of the patch to be harvested.
	 * @return The bid and the patch being bid on.
	 */
	public HarvestBid requestBid(Point[] parcel, double patch) {
		// Find a patch with the given size
		List<Point> points = findPatch(parcel, patch);
		if (points == null) {
			return null;
		}
		
		// Prepare the bid
		HarvestBid bid = new HarvestBid();
		bid.patch = points;
		
		// Calculate the stumpage
		Forest forest = Forest.getInstance();
		TimberMarketplace marketplace = TimberMarketplace.getInstance();
		for (Point point : points) {
			Stand stand = forest.getStand(point);
			double marketPrice = marketplace.calculateBid(stand);			
			bid.bid += marketPrice;
		}
		
		// Return the bid
		return bid;
	}
	
	/**
	 * Reset the harvest report.
	 */
	public void reset() { annualReport = new HarvestReport(); }
	
	/**
	 * Update the annual harvest report with the harvest.
	 */
	protected void update(HarvestReport harvest) {
		annualReport.labor += harvest.labor;
		annualReport.merchantable += harvest.merchantable;
		annualReport.visualImpact += harvest.visualImpact;
		annualReport.wetlandImpact += harvest.wetlandImpact;
		annualReport.woodyBiomass += harvest.woodyBiomass;
	}

	public double getMarkup() { return markup; }
	
	public double getWoodyBiomassRetention() { return woodyBiomassRetention; }

	public void setMarkup(double value) { markup = value; }
	
	public void setWoodyBiomassRetention(double value) { woodyBiomassRetention = value; }
}
