package edu.mtu.cabals.model.marketplace;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.javatuples.Pair;

import ec.util.MersenneTwisterFast;
import edu.mtu.cabals.model.Parameters;
import edu.mtu.cabals.model.TimberMarketplace;
import edu.mtu.cabals.model.WupModel;
import edu.mtu.environment.Forest;
import edu.mtu.environment.NlcdClassification;
import edu.mtu.environment.Stand;
import edu.mtu.steppables.LandUseGeomWrapper;
import edu.mtu.utilities.Constants;
import edu.mtu.utilities.Precision;
import sim.field.geo.GeomGridField;
import sim.field.grid.IntGrid2D;

/**
 * Harvesters (e.g., CF and NIPF) both share the same way of harvesting.
 */
public abstract class Harvester {
	
	// TODO Update the range for the variable mark-up to be set via the INI file
	
	// Multiplier to go from dry to green ton, based on Dulys-Nusbaum et al., 2019
	public final static double DryToGreen = 2;
	
	private double annualLimit;
	private double markup;
	private double woodyBiomassRetention;
	private MersenneTwisterFast random;
	
	private HarvestReport annualReport = new HarvestReport();
		
	private class Cell {
		public ArrayList<Point> points = new ArrayList<Point>();
	}
	
	/**
	 * Find the most mature patch in the given parcel that matches the size.
	 * 
	 * @param lu The geometry used to map the bounds.
	 * @param parcel The parcel to examine.
	 * @param patch The size (ha) of the patch to be harvested.
	 * @param dbh The minimum DBH (cm) of the trees.
	 * @return The points in the patch, or null if a match cannot be found.
	 */
	protected List<Point> findPatch(final LandUseGeomWrapper lu, Point[] parcel, double patch, double dbh) {
						
		// If the patch is greater than or equal to the size of the parcel, just return it
		Forest forest = Forest.getInstance();
		double pixel = forest.getPixelArea();		// sq.m
		double area = (parcel.length * pixel) / Constants.SquareMetersToHectares;
		if (area <= patch) {
			return Arrays.asList(parcel);
		}
		
		// The bounding rectangle of the agent's parcel converted to an IntGrid2D index (min and max)
		GeomGridField cover = Forest.getInstance().getLandCover();
		int xMin = cover.toXCoord(lu.geometry.getEnvelopeInternal().getMinX());
		int yMin = cover.toYCoord(lu.geometry.getEnvelopeInternal().getMinY());
		int xMax = cover.toXCoord(lu.geometry.getEnvelopeInternal().getMaxX());
		int yMax = cover.toYCoord(lu.geometry.getEnvelopeInternal().getMaxY());
				
		// Use the bounds to setup our grids
		double divisor = (int)Math.ceil(Constants.SquareMetersToHectares / pixel);		// ha / sq.m
		int xBound = (int)Math.ceil(Math.abs(xMin - xMax) / divisor) + 1;
		int yBound = (int)Math.ceil(Math.abs(yMin - yMax) / divisor) + 1;
		Cell[][] patches = new Cell[xBound][yBound];
		for (int ndx = 0; ndx < xBound; ndx++) {
			for (int ndy = 0; ndy < yBound; ndy++) {
				patches[ndx][ndy] = new Harvester.Cell();
			}
		}
		double[][] meanDbh = new double[xBound][yBound];
		
		// Since we don't really care about the orientation of the map, make sure we are
		// using the actual minimum values
		xMin = Math.min(xMin, xMax);
		yMin = Math.min(yMin, yMax);
		
		// Iterate through the points, sum the DBH and assign points to patches
		for (Point point : parcel) {
			int x = (int)Math.floor(Math.abs(point.x - xMin) / divisor);
			int y = (int)Math.floor(Math.abs(point.y - yMin) / divisor);
			patches[x][y].points.add(point);
			
			// Only add the DBH if it is greater than or equal to our target 
			// minimum. This also introduces a penalty for the square when
			// it contains a significant number of lower value stands
			Stand stand = forest.getStand(point);
			if (stand.arithmeticMeanDiameter >= dbh) {
				meanDbh[x][y] += stand.arithmeticMeanDiameter;
			}
		}
		
		// Find the mean DBH and note the max
		int x = 0, y = 0;
		double max = Double.MIN_VALUE;
		for (int ndx = 0; ndx < meanDbh.length; ndx++) {
			for (int ndy = 0; ndy < meanDbh[ndx].length; ndy++) {
				
				// Find the average for the square, if it is less than our target
				// then set the square to zero to reserve it for another year
				meanDbh[ndx][ndy] /= patches[ndx][ndy].points.size();
				if (meanDbh[ndx][ndy] < dbh) {
					meanDbh[ndx][ndy] = 0;
					continue;
				}
				
				// Check to see if this is a good starting point
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
			harvest += (patches[ndx][ndy].points.size() * area) / Constants.SquareMetersToHectares; 
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
		IntGrid2D wetlands = (IntGrid2D)Forest.getInstance().getLandCover().getGrid();
		
		// Perform the initial harvest of the patch
		Forest forest = Forest.getInstance();
		Pair<Double, Double> results = forest.harvest(patch.toArray(new Point[0]));
		
		// Update the report with the results of the harvest
		HarvestReport report = new HarvestReport();
		double area = forest.getPixelArea();
		report.biomass = (results.getValue1() / Constants.KilogramToMetricTon) * DryToGreen;			// Above Ground dry kg to green tons
		report.merchantable = (results.getValue0() / Constants.KilogramToMetricTon) * DryToGreen;		// Stem dry kg converted to green tons
		report.cwd = report.biomass - report.merchantable;												// Above ground woody biomass green tons
		report.harvestedArea = (patch.size() * area) / Constants.SquareMetersToHectares;				// sq.m to ha
		
		// Check to see what the impacts are via GIS
		final int wetlandsCode = NlcdClassification.WoodyWetlands.getValue();
		for (Point point : patch) {
			if (visualBuffer.get(point.x, point.y) == 0) {
				report.visualImpact += area;
			}
			if (wetlands.get(point.x, point.y) == wetlandsCode) {
				report.wetlandImpact += area;
			}
		}
		report.visualImpact /= Constants.SquareMetersToHectares;	// sq.m to ha
		report.wetlandImpact /= Constants.SquareMetersToHectares;	// sq.m to ha
						
		// Apply the economic calculations
		report.labor = harvestDuration(report.merchantable);
		report.biomassRecoverable = report.cwd * (1 - woodyBiomassRetention);

		// Apply the biomass costs and return
		biomassCosts(report, lu.getDoubleAttribute("NEAR_KM"));
		
		return report;
	}
	
	// Calculate how long we expect the harvest to take, rounded to the near nearest hundreds place
	private double harvestDuration(double merchantable) {
		Parameters parameters = WupModel.getParameters();
		
		double hours = merchantable / parameters.getMerchantableProductivity();
		return Precision.round(hours, 2);
	}
	
	// Calculate the costs assoicated with harvesting the biomass
	private void biomassCosts(HarvestReport report, double distance) {
		Parameters parameters = WupModel.getParameters();
		
		report.loggerHours = Precision.round(report.biomassRecoverable / parameters.getBiomassChipping(), 2);
		double loggerPay = report.loggerHours * parameters.getLoggerPerHour();
		
		double totalDistance = Transporter.getTotalDistance(distance, report.biomassRecoverable);
		report.driverHours = Precision.round(totalDistance / parameters.getKmPerHour(), 2);
		double driverPay = report.driverHours * parameters.getDriverPerHour();
		
		double fuelCost = (totalDistance / parameters.getKmPerLiter()) * parameters.getDieselPerLiter();
		report.biomassCost = Precision.round(loggerPay + driverPay + fuelCost, 2);
	}
	
	/**
	 * Get the report of harvesting.
	 */
	public HarvestReport report() { return annualReport; }
	
	/**
	 * Request a bit from the harvester for the given parcel and patch size.
	 * 
	 * @param lu The geometry used to map the bounds.
	 * @param parcel The parcel to request the bid on.
	 * @param patch The size (ha) of the patch to be harvested.
	 * @param dbh The minimum DBH of the harvest.
	 * @return The bid and the patch being bid on.
	 */
	public HarvestBid requestBid(LandUseGeomWrapper lu, Point[] parcel, double patch, double dbh) {
		// Find a patch with the given size
		HarvestBid bid = new HarvestBid();
		List<Point> points = findPatch(lu, parcel, patch, dbh);
		if (points == null || points.size() == 0) {
			return bid;
		}
		
		// Prepare the bid
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
	protected void update(HarvestReport harvest, boolean biomassCollected) {
		annualReport.biomass += harvest.biomass;
		annualReport.merchantable += harvest.merchantable;
		annualReport.cwd += harvest.cwd;
		
		annualReport.visualImpact += harvest.visualImpact;
		annualReport.wetlandImpact += harvest.wetlandImpact;
		
		annualReport.labor += harvest.labor;
		
		if (biomassCollected) {
			annualReport.biomassRecoverable += harvest.biomassRecoverable;
			annualReport.loggerHours += harvest.loggerHours;
			annualReport.driverHours += harvest.driverHours;
		}
	}

	protected double getAnnualHarvestLimit() { return annualLimit; }
	
	protected double getMarkup() { 
			
			// If the mark-up was set, return it
			if (markup != 0) { return markup; }
			
			// Calculate a random value and return it
			int value = 5 + random.nextInt(15);
			return 1 + (double)value / 100.0;

	}
	
	protected double getWoodyBiomassRetention() { return woodyBiomassRetention; }

	/**
	 * Set the annual limit to the number of hours NIPFs can harvest.
	 */
	public void setAnnualHarvestLimit(double value) { annualLimit = value; }
	
	/**
	 * Set the margin for woody biomass profits.
	 */
	public void setMarkup(double value) { markup = value; }
	
	/**
	 * Set the random number generator.
	 */
	public void setRandom(MersenneTwisterFast value) { random = value; }
	
	/**
	 * Set the quantity of woody biomass that must be retained on site.
	 */
	public void setWoodyBiomassRetention(double value) { woodyBiomassRetention = value; }
}
