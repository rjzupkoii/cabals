package edu.mtu.cabals.wup;

import edu.mtu.utilities.Constants;

public class AcerRebrum implements WupSpecies {		
		
	private final static double GrowthPerYear = 0.57;	// cm/year
	private final static double MaxDbh = 76.0;			// cm
	private final static double MaxHeight = 30;			// m 
	
	// Jenkins et al., 2003
	public double getAboveGroundBiomass(double dbh) {
		double beta0 = -2.0127, beta1 = 2.4342;
		return Math.exp(beta0 + beta1 * Math.log(dbh));
	}

	// Jenkins et al., 2003
	public double getStemWoodBiomassRatio(double dbh) {
		double beta0 = -0.3065, beta1 = -5.4240;
		return Math.exp(beta0 + (beta1 / dbh));
	}
	
	// Wykoff functional form, Lake States FVS Variant
	public double getHeight(double dbh) {
		final double B1 = 4.3379, B2 = -3.8214;
		
		dbh /= 2.54;				// dbh in cm to in
		double ht = 4.5 + Math.exp(B1 + B2/ (dbh + 1.0));
		return (ht / 3.281);		// ht in ft to m
	}
	
	// Kershaw et al. 2008
	@Override
	public double heightToDbh(double height) {
		double b1 = 29.007, b2 = 0.053, b3 = 1.175;	

		if (height < 2 || height > MaxHeight) { return -1; }
		
		double dbh = -Math.log(1 - Math.pow((height - Constants.DbhTakenAt) / b1, 1 / b3)) / b2;
		return dbh;
	}


	// Silvics Manual, vol. 2, conservative estimate
	@Override
	public double getDbhGrowth() { return GrowthPerYear; }

	// Silvics Manual, vol. 2, high-end mature tree estimate
	@Override
	public double getMaximumDbh() { return MaxDbh; }

	@Override
	public double getMaximumHeight() { return MaxHeight; }

	@Override
	public String getName() { return "Red Maple"; }

	@Override
	public String getDataFile() { return "data/AcerRebrum.csv"; }	
}
