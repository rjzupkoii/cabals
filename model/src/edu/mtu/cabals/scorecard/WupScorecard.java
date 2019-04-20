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
import edu.mtu.utilities.Precision;

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
			File dir = new File(directory);
			dir.mkdirs();
						
			// Count how many files are there already, use this for naming
			int count = (int)(dir.listFiles().length / Indicators.values().length);
			
			// Create the buffered file writers
			writers = new BufferedCsvWriter[Indicators.length];
			for (Indicators indicator : Indicators.values()) {
				writers[indicator.index()] = new BufferedCsvWriter(directory + String.format(indicator.path(), count), true);
			}			
			
			// Write the headers
			writeHarvestHeader(Indicators.NipfHarvesting.index());
			writeHarvestHeader(Indicators.CfHarvesting.index());
			writeTransportationheader(Indicators.Transport.index());
			
		} catch (IOException ex) {
			System.err.println("Unhandled IOException: " + ex.toString());
			System.exit(-1);
		}	
	}

	@Override
	public void processTimeStep(ForestSim state) {
		try {
			collectHarvestReports();
			for (Indicators indicator : Indicators.values()) {
				writers[indicator.index()].flush();
			}	
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
	
	// Write the column headers for the harvest report
	private void writeHarvestHeader(int index) throws IOException {
		writers[index].write("Biomass");
		writers[index].write("Merchantable");
		writers[index].write("CWD");
		
		writers[index].write("Visual");
		writers[index].write("Wetland");
		
		writers[index].write("Labor");
		
		writers[index].write("Recoverable");
		writers[index].write("Labor");
		
		// Finish the line
		writers[index].newLine();
		writers[index].flush();
	}
	
	// Write the actual data for the harvest report
	private void writeHarvestReport(HarvestReport report, int index) throws IOException {
		writers[index].write(Precision.round(report.biomass, 2));
		writers[index].write(Precision.round(report.merchantable, 2));
		writers[index].write(Precision.round(report.cwd, 2));
		
		writers[index].write(Precision.round(report.visualImpact, 2));
		writers[index].write(Precision.round(report.wetlandImpact, 2));
		
		writers[index].write(Precision.round(report.labor, 2));
		
		writers[index].write(Precision.round(report.biomassRecoverable, 2));
		writers[index].write(Precision.round(report.biomassLabor, 2));
		writers[index].newLine();
	}
	
	// Write the column headers for the transportation report
	private void writeTransportationheader(int index) throws IOException {
		writers[index].write("Distance");
		writers[index].write("CWD");
		
		// Finish the line
		writers[index].newLine();
		writers[index].flush();
	}
	
	// Write the actual data for the transportation report
	private void writeTransportReport(int index) throws IOException {
		Transporter transporter = Transporter.getInstance();
		writers[index].write(Precision.round(transporter.getDistance(), 2));
		writers[index].write(Precision.round(transporter.getWoodyBiomass(), 2));
		writers[index].newLine();
		transporter.reset();
	}
}
