package edu.mtu.cabals.wup;

// https://www.forestasyst.org/hardwoods/alleghaniensis.htm
public class BetulaAlleghaniensis implements WupSpecies {
	
	public final static double GrowthPerYear = 2.5d;	// cm/year
	public final static double MaxDbh = 56.7d;			// cm
	public final static double MaxHeight = 34.7d;		// m 
	
	// 
	@Override
	public double getAboveGroundBiomass(double dbh) {
		double beta0 = -2.5356, beta1 = 2.4349;
		return Math.exp(beta0 + beta1 * Math.log(dbh));
	}
	
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
	
	@Override
	public double heightToDbh(double height) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String getName() { return "Yellow Birch"; }

	@Override
	public double getDbhGrowth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMaximumDbh() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMaximumHeight() { return MaxHeight; }

	@Override
	public String getDataFile() {
		// TODO Auto-generated method stub
		return null;
	}
}
