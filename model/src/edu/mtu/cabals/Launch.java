package edu.mtu.cabals;

import edu.mtu.cabals.model.WupModel;
import edu.mtu.simulation.ForestSim;
import edu.mtu.simulation.ForestSimWithUI;

public class Launch {
	public static void main(String[] args) {
		// No arguments implies UI
		if (args.length == 0) {
			WupModel model = new WupModel(System.currentTimeMillis());
			ForestSimWithUI fs = new ForestSimWithUI(model);
			fs.load();
			return;
		}
		
		// Otherwise we are running on the command line, MASON arguments
		ForestSim.load(WupModel.class, args);
	}
}
