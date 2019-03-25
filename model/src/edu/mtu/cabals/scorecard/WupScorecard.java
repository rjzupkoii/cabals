package edu.mtu.cabals.scorecard;

import java.io.File;
import java.io.IOException;

import edu.mtu.cabals.model.marketplace.CfHarvester;
import edu.mtu.cabals.model.marketplace.HarvestReport;
import edu.mtu.cabals.model.marketplace.NipfHarvester;
import edu.mtu.cabals.model.marketplace.Transporter;
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
		try {
			for (int ndx = 0; ndx < writers.length; ndx++) {
				writers[ndx].close();
			}
		} catch (IOException ex) {
			System.err.println("Unhandled IOException: " + ex.toString());
			System.exit(-1);
		}
	}

	@Override
	public void processInitialization(ForestSim state) {
		try {
			// Bootstrap any relevant paths
			(new File(directory)).mkdirs();

			// Create the buffered file writers
			writers = new BufferedCsvWriter[Indicators.length];
			for (Indicators indicator : Indicators.values()) {
				writers[indicator.index()] = new BufferedCsvWriter(directory + indicator.path(), true);
			}			
		} catch (IOException ex) {
			System.err.println("Unhandled IOException: " + ex.toString());
			System.exit(-1);
		}	
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
		HarvestReport report = NipfHarvester.getInstance().report();
		writeHarvestReport(report, Indicators.NipfHarvesting.index());
		NipfHarvester.getInstance().reset();
		
		report = CfHarvester.getInstance().report();
		writeHarvestReport(report, Indicators.CfHarvesting.index());
		CfHarvester.getInstance().reset();		
	
		writeTransportReport(Indicators.Transport.index());
	}
	
	private void writeHarvestReport(HarvestReport report, int index) throws IOException {
		writers[index].write(report.merchantable);
		writers[index].write(report.woodyBiomass);
		writers[index].write(report.visualImpact);
		writers[index].write(report.wetlandImpact);
		writers[index].write(report.labor);
		writers[index].newLine();
	}
	
	private void writeTransportReport(int index) throws IOException {
		Transporter transporter = Transporter.getInstance();
		writers[index].write(transporter.getDistance());
		writers[index].write(transporter.getWoodyBiomass());
		writers[index].newLine();
		transporter.reset();
	}
}
