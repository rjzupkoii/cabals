package edu.mtu.cabals.wup;

import edu.mtu.environment.Species;

public interface WupSpecies extends Species {
	
	double getDbhGrowth();
	double getMaximumDbh();
	double getMaximumHeight();
	String getDataFile();
	
	/**
	 * Get the back thickness, in cm, to use when estimating board feet.
	 */
	double getBarkThickness();
	
	/**
	 * Get the DBH (cm) of the tree given the height (m).
	 */
	double heightToDbh(double height);
}
