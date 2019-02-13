package edu.mtu.cabals.wup;

import edu.mtu.utilities.Constants;

// Note that Red Maple should be ready for harvest at sawtimber in about 60 years from seeding
// 
// https://www.na.fs.fed.us/pubs/silvics_manual/volume_2/acer/rubrum.htm
// http://www.nrs.fs.fed.us/pubs/rp/rp_nc257.pdf 
// http://dnr.wi.gov/topic/ForestManagement/documents/24315/51.pdf
public class AcerRebrum implements WesternUPSpecies {		
		
	public final static double MaxHeight = 30d;
	
	public double getAboveGroundBiomass(double dbh) {
		// Jenkins et al., 2003 - https://www.fs.fed.us/ne/durham/4104/papers/Heathbiomass_eqns.pdf
		double beta0 = -2.0127, beta1 = 2.4342;
		return Math.exp(beta0 + beta1 * Math.log(dbh));
	}

	public double getStemWoodBiomassRatio(double dbh) {
		// Jenkins et al., 2003 - https://www.fs.fed.us/ne/durham/4104/papers/Heathbiomass_eqns.pdf
		double beta0 = -0.3065, beta1 = -5.4240;
		return Math.exp(beta0 + (beta1 / dbh));
	}
	
	// Get the height of the given stand using the height-diameter equation (Kershaw et al. 2008)
	public double getHeight(double dbh) {
		double b1 = 29.007, b2 = 0.053, b3 = 1.175;	
		double height = Constants.DbhTakenAt + b1 * Math.pow(1 - Math.pow(Math.E, -b2 * dbh), b3);
		return height;
	}
	
	/**
	 * Get the DBH (cm) of the tree given the height (m); (Kershaw et al. 2008)
	 * 
	 * @return The DBH (cm) or -1 if the height is out of bounds ([2, 30])
	 */
	@Override
	public double heightToDbh(double height) {
		double b1 = 29.007, b2 = 0.053, b3 = 1.175;	
		
		// Check to make sure we can do math
		if (height < 2 || height > MaxHeight) { return -1; }
		
		double dbh = -Math.log(Math.pow(1 - Math.pow((height - Constants.DbhTakenAt) / b1, 1 / b3), 1 / b2));
		return dbh;
	}

	public String getName() {
		return "Red Maple";
	}

	public double getDbhGrowth() {
		return 0.57;
	}

	public double getMaximumDbh() {
		return 76.0;
	}
	
	public String getDataFile() {
		return "data/AcerRebrum.csv";
	}
	
	public double getPulpwoodValue() {
		return 37.00;		// Gwinn Forst MGMT Unit, Q1 2017
	}
	
	public  double getSawtimberValue() {
		return 479.75;		// Baraga Forest MGMT Unit, Q1 2017
	}

	@Override
	public double getMaximumHeight() { return MaxHeight; }
}
