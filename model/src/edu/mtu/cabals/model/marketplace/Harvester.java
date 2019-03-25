package edu.mtu.cabals.model.marketplace;

import java.awt.Point;
import java.util.List;

import org.javatuples.Pair;

import edu.mtu.cabals.model.TimberMarketplace;
import edu.mtu.cabals.model.WupModel;
import edu.mtu.cabals.wup.WupSpecies;
import edu.mtu.environment.Forest;
import edu.mtu.environment.Stand;
import edu.mtu.steppables.LandUseGeomWrapper;
import sim.field.grid.IntGrid2D;

/**
 * Harvesters (e.g., CF and NIPF) both share the same way of harvesting.
 */
public abstract class Harvester {
	
	private double markup;
	private double woodyBiomassRetention;
	private HarvestReport annualReport = new HarvestReport();
	
	/**
	 * Find the most mature patch in the given parcel that matches the size.
	 * 
	 * @param lu The parcel to examine.
	 * @param patch The size (ha) of the patch to be harvested.
	 * @return The points in the patch, or null if a match cannot be found.
	 */
	public List<Point> findPatch(LandUseGeomWrapper lu, double patch) {
		
		// TODO Auto-generated method stub
		
		return null;
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
		report.merchantable = results.getValue0() / 1000;							// Stem / 1000 for metric tons
		report.woodyBiomass = (results.getValue1() - results.getValue0()) / 1000;	// (Aboveground - Stem) / 1000 for metric tons
		report.harvestedArea = (patch.size() * area) / 10000;		// sq.m to ha
		
		// Check to see what the impacts are via GIS
		for (Point point : patch) {
			if (visualBuffer.get(point.x, point.y) != 0) {
				report.visualImpact += area;
			}
			if (wetlands.get(point.x, point.y) != 0) {
				report.wetlandImpact += area;
			}
		}
		report.visualImpact /= 10000;	// sq.m to ha
		report.wetlandImpact /= 10000;	// sq.m to ha
				
		// Apply the economic calculations
		report.labor = harvestDuration(report.merchantable);
		report.biomassRecoverable = report.woodyBiomass * (1 - woodyBiomassRetention);
		report.biomassLabor = biomassLabor(report.biomassRecoverable);
		report.biomassCost = biomassCost(report.biomassLabor, lu.getDoubleAttribute("NEAR_KM"));
		
		return report;
	}
	
	private double harvestDuration(double merchantable) {
		
		// TODO Calculate the harvest duration based upon merchantable biomass
		
		return 0.0;
	}
	
	private double biomassLabor(double biomass) {
		
		// TODO Calculate the woody biomass labor based upon recoverable biomass
		
		return 0.0;
	}
	
	private double biomassCost(double labor, double distance) {
		
		// TODO Calculate the woody biomass cost based upon labor and distance to travel
		
		return 0.0;
	}
	
	/**
	 * Get the report of harvesting.
	 */
	public HarvestReport report() { return annualReport; }
	
	/**
	 * Request a bit from the harvester for the given parcel and patch size.
	 * 
	 * @param lu The parcel to request the bid on.
	 * @param patch The size (ha) of the patch to be harvested.
	 * @return The bid and the patch being bid on.
	 */
	public HarvestBid requestBid(LandUseGeomWrapper lu, double patch) {
		// Find a patch with the given size
		List<Point> points = findPatch(lu, patch);
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
			double marketPrice = marketplace.getPrice((WupSpecies)stand.dominateSpecies, stand.arithmeticMeanDiameter);
			bid.bid += stand.numberOfTrees * marketPrice;
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
