package edu.mtu.cabals.model;

import edu.mtu.simulation.parameters.ParameterBase;

public class Parameters extends ParameterBase {

	// Model settings
	private String outputDirectory;
	private String pricesFile;
	
	// GIS data
	private String parcelShapeFile;
	private String nlcdRaster;
	private String landfireHeightRaster;
	private String landfireCoverRaster;
	
	public String getParcelShapeFile() {
		return parcelShapeFile;
	}
	
	public String getNlcdRaster() {
		return nlcdRaster;
	}
	
	public String getLandfireHeightRaster() {
		return landfireHeightRaster;
	}
	
	public String getLandfireCoverRaster() {
		return landfireCoverRaster;
	}
	
	public String getOutputDirectory() {
		return outputDirectory;
	}
	
	public String getPricesFile() {
		return pricesFile;
	}
	
	public void setParcelShapeFile(String value) {
		parcelShapeFile = value;
	}
	
	public void setNlcdRaster(String value) {
		nlcdRaster = value;
	}
	
	public void setLandfireHeightRaster(String value) {
		landfireHeightRaster = value;
	}
	
	public void setLandfireCoverRaster(String value) {
		landfireCoverRaster = value;
	}

	public void setOutputDirectory(String value) {
		outputDirectory = value;
	}

	public void setPricesFile(String value) {
		pricesFile = value;
	}
}
