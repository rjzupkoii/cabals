package edu.mtu.cabals.scorecard;

import java.io.IOException;

import edu.mtu.cabals.model.marketplace.CfHarvester;
import edu.mtu.cabals.model.marketplace.HarvestReport;
import edu.mtu.cabals.model.marketplace.NifpHarvester;
import edu.mtu.simulation.ForestSim;
import edu.mtu.simulation.Scorecard;
import edu.mtu.utilities.BufferedCsvWriter;

public class WupScorecard implements Scorecard {

	private BufferedCsvWriter[] writers;
	private String directory;
	
	public WupScorecard(String directory) {
		this.directory = directory;
	}
	
	@Override
	public void processFinalization(ForestSim state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processInitialization(ForestSim state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processTimeStep(ForestSim state) {
		try {
		
			collectHarvestReports();
		
		} catch (IOException ex) {
			System.err.println(ex);
			System.exit(-1);
		}
	}
	
	private void collectHarvestReports() throws IOException {
		HarvestReport report = NifpHarvester.getInstance().report();
		writeHarvestReport(report, Indicators.NipfHarvesting.index());
		NifpHarvester.getInstance().reset();
		
		report = CfHarvester.getInstance().report();
		writeHarvestReport(report, Indicators.CfHarvesting.index());
		CfHarvester.getInstance().reset();		
	}
	
	private void writeHarvestReport(HarvestReport report, int index) throws IOException {
		writers[index].write(report.merchantable);
		writers[index].write(report.woodyBiomass);
		writers[index].write(report.visualImpact);
		writers[index].write(report.wetlandImpact);
		writers[index].write(report.labor);
		writers[index].newLine();
	}
}
