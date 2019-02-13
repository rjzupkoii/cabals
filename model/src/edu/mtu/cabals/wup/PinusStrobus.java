package edu.mtu.cabals.wup;

import edu.mtu.utilities.Constants;

// https://www.na.fs.fed.us/spfo/pubs/silvics_manual/Volume_1/pinus/strobus.htm
// http://dnr.wi.gov/topic/ForestManagement/documents/24315/31.pdf
public class PinusStrobus implements WesternUPSpecies {
	
	public final static double MaxHeight = 50d;
	
	public double getAboveGroundBiomass(double dbh) {
		// Jenkins et al., 2003 - https://www.fs.fed.us/ne/durham/4104/papers/Heathbiomass_eqns.pdf
		double beta0 = -2.5356, beta1 = 2.4349;
		return Math.exp(beta0 + beta1 * Math.log(dbh));
	}
	
	public double getStemWoodBiomassRatio(double dbh) {
		// Jenkins et al., 2003 - https://www.fs.fed.us/ne/durham/4104/papers/Heathbiomass_eqns.pdf
		double beta0 = -0.3737, beta1 = -1.8055;
		return Math.exp(beta0 + (beta1 / dbh));
	}

	public double getHeight(double dbh) {
		double b1 = 49.071, b2 = 0.016, b3 = 1.0;
		double height = Constants.DbhTakenAt + b1 * Math.pow(1 - Math.pow(Math.E, -b2 * dbh), b3);
		return height;
	}
	
	/**
	 * Get the DBH (cm) of the tree given the height (m); (Kershaw et al. 2008)
	 * 
	 * @return The DBH (cm) or -1 if the height is out of bounds ([2, 50])
	 */
	@Override
	public double heightToDbh(double height) {
		double b1 = 49.071, b2 = 0.016;
		
		// Check that we can do the math
		if (height < 2 || height > MaxHeight) { return -1; }
		
		double dbh = -Math.log(Math.pow(1 - ((height - Constants.DbhTakenAt) / b1), 1 / b2));
		return dbh;
	}

	public String getName() {
		return "Eastern White Pine";
	}

	public double getDbhGrowth() {
		return 0.5;
	}

	public double getMaximumDbh() {
		return 102.0;
	}

	public String getDataFile() {
		return "data/PinusStrobus.csv";
	}
	
	public double getPulpwoodValue() {
		return 48.45;		// Gwinn Forest MGMT Unit, Q1 2017
	}
	
	public double getSawtimberValue() {
		return 100.00;		// Gwinn Forest MGMT Unit, Q1 2017
	}

	@Override
	public double getMaximumHeight() { return MaxHeight; }
}
