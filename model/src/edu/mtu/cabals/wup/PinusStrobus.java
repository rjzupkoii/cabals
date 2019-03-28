package edu.mtu.cabals.wup;

import edu.mtu.utilities.Constants;

public class PinusStrobus implements WupSpecies {
	
	private final static double GrowthPerYear = 0.5;	// cm/year
	private final static double MaxDbh = 102.0;			// cm
	private final static double MaxHeight = 50;			// m 
	
	
	// Jenkins et al., 2003
	public double getAboveGroundBiomass(double dbh) {
		double beta0 = -2.5356, beta1 = 2.4349;
		return Math.exp(beta0 + beta1 * Math.log(dbh));
	}
	
	// Jenkins et al., 2003
	public double getStemWoodBiomassRatio(double dbh) {
		double beta0 = -0.3737, beta1 = -1.8055;
		return Math.exp(beta0 + (beta1 / dbh));
	}

	// Curtis-Arney equation, Lake States FVS Variant
	public double getHeight(double dbh) {
		final double P2 = 2108.8442d, P3 = 5.6595, P4 = -0.1856;
		
		dbh /= 2.54;				// dbh in cm to in
		if (dbh < 3.0) { return -1; }
		double ht = 4.5 + P2 * Math.exp(-P3 * Math.pow(dbh, P4));
		return (ht / 3.281);		// ht in ft to m
	}

	// Kershaw et al. 2008
	@Override
	public double heightToDbh(double height) {
		double b1 = 49.071, b2 = 0.016;
		
		if (height < 2 || height > MaxHeight) { return -1; }
		
		double dbh = -Math.log(1 - (height - Constants.DbhTakenAt) / b1) / b2;
		return dbh;
	}

	// Silvics Manual, vol. 2, average estimates
	@Override
	public double getDbhGrowth() { return GrowthPerYear; }
	
	// Silvics Manual, vol. 2, common high-end value
	@Override
	public double getMaximumDbh() { return MaxDbh; }

	@Override
	public double getMaximumHeight() { return MaxHeight; }

	@Override
	public String getName() { return "White Pine"; }
	
	@Override
	public String getDataFile() { return "data/PinusStrobus.csv"; }
}
