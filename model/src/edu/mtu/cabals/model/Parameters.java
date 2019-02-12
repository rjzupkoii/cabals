package edu.mtu.cabals.model;

import edu.mtu.simulation.parameters.ParameterBase;

public class Parameters extends ParameterBase {

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
}
