package edu.mtu.cabals.wup;

import edu.mtu.environment.Species;

public interface WesternUPSpecies extends Species {
	double getDbhGrowth();
	double getMaximumDbh();
	String getDataFile();
	
	/**
	 * Get the bid of one cord of pulpwood.
	 */
	double getPulpwoodValue();
	
	/**
	 * Get the bid per thousand board feet (MBF) of sawtimber.
	 */
	double getSawtimberValue();
}
