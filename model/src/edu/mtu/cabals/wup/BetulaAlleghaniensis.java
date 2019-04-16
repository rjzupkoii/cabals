package edu.mtu.cabals.wup;

import edu.mtu.utilities.Constants;

public class BetulaAlleghaniensis implements WupSpecies {
	
	public final static double GrowthPerYear = 0.51;	// cm/year
	public final static double MaxDbh = 60;				// cm
	public final static double MaxHeight = 22;			// m 
	
	// Jenkins et al., 2003
	@Override
	public double getAboveGroundBiomass(double dbh) {
		double beta0 = -2.5356, beta1 = 2.4349;
		return Math.exp(beta0 + beta1 * Math.log(dbh));
	}
	
	// Jenkins et al., 2003
	@Override
	public double getStemWoodBiomassRatio(double dbh) {
		double beta0 = -0.3065, beta1 = -5.4240;
		return Math.exp(beta0 + (beta1 / dbh));
	}

	// Wykoff functional form, Lake States FVS Variant
	@Override
	public double getHeight(double dbh) {
		final double B1 = 4.4388, B2 = -4.0872;
		
		dbh /= 2.54;				// dbh in cm to in
		double ht = 4.5 + Math.exp(B1 + B2 / (dbh + 1.0));
		return (ht / 3.281);		// ht in ft to m
	}
	
	// Peng et al., 2001
	@Override
	public double heightToDbh(double height) {
		double b1 = 20.7576, b2 = 0.0858, b3 = 1.0532;	

		if (height < 2 || height > MaxHeight) { return -1; }
		
		double dbh = -Math.log(1 - Math.pow((height - Constants.DbhTakenAt) / b1, 1 / b3)) / b2;
		return dbh;	
	}
	
	// Silvics Manual, vol. 2, assuming 46 cm in about 90 years
	@Override
	public double getDbhGrowth() { return GrowthPerYear; }

	// Silvics Manual, vol. 2, 56 cm is financially mature, 46 cm is more common, constrain with a bit of space to work with 
	@Override
	public double getMaximumDbh() { return MaxDbh; }

	@Override
	public double getMaximumHeight() { return MaxHeight; }

	@Override
	public String getName() { return "Yellow Birch"; }
	
	@Override
	public String getDataFile() { return "data/BetulaAlleghaniensis.csv"; }

	// Sullivan, 1994
	@Override
	public double getBarkThickness() { return 1.27; }
}
