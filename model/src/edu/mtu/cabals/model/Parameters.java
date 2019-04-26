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
	private String visualBufferRaster;

	// Harvesting settings
	private double merchantableProductivity;
	private double biomassChipping;
	private double dieselPerLiter;
	private double harvestHours;
	private double loggerPerHour;	
	private double markup;
	private double chipperFuel;
	private double woodyBiomassRetention;
	
	// Transportation settings
	private double chipVanCapacity;
	private double driverPerHour;
	private double kmPerHour;
	private double kmPerLiter;
		
	// CF settings
	private double cfMinimumDbh;
	private double cfReserve;
	private int cfYears;
	
	// NIPF settings
	private double nipfMinimumDbh;
	private double nipfProfitMean, nipfProfitSd;
	private double nipfStandMean, nipfStandSd;
	private double nipfWoodyBiomassMean, nipfWoodyBiomassSd;
		
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
	
	public double getNipfStandMean() {
		return nipfStandMean;
	}

	public double getNipfStandSd() {
		return nipfStandSd;
	}
	
	public double getNipfProfitMean() {
		return nipfProfitMean;
	}
	
	public double getNipfProfitSd() {
		return nipfProfitSd;
	}
	
	public int getCfYears() {
		return cfYears;
	}
	
	public double getMarkup() {
		return markup;
	}
	
	public double getNipfWoodyBiomassMean() {
		return nipfWoodyBiomassMean;
	}

	public double getNipfWoodyBiomassSd() {
		return nipfWoodyBiomassSd;
	}
	
	public String getVisualBufferRaster() {
		return visualBufferRaster;
	}
	
	public double getMerchantableProductivity() {
		return merchantableProductivity;
	}
	
	public double getBiomassChipping() {
		return biomassChipping;
	}

	public double getDieselPerLiter() {
		return dieselPerLiter;
	}

	public double getKmPerLiter() {
		return kmPerLiter;
	}

	public double getLoggerPerHour() {
		return loggerPerHour;
	}

	public double getDriverPerHour() {
		return driverPerHour;
	}
	
	public double getKmPerHour() {
		return kmPerHour;
	}
	
	public double getHarvestHours() {
		return harvestHours;
	}
	
	public double getChipVanCapacity() {
		return chipVanCapacity;
	}
	
	public double getCfMinimumDbh() {
		return cfMinimumDbh;
	}

	public double getCfReserve() {
		return cfReserve;
	}

	public double getNipfMinimumDbh() {
		return nipfMinimumDbh;
	}
	
	public double getChipperFuel() {
		return chipperFuel;
	}
	
	public double getWoodyBiomassRetention() {
		return woodyBiomassRetention;
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

	public void setNipfStandMean(double value) {
		nipfStandMean = value;
	}

	public void setNipfStandSd(double value) {
		nipfStandSd = value;
	}

	public void setNipfProfitMean(double value) {
		nipfProfitMean = value;
	}

	public void setNipfProfitSd(double value) {
		nipfProfitSd = value;
	}

	public void setCfYears(int value) {
		cfYears = value;
	}

	public void setMarkup(double value) {
		markup = value;
	}

	public void setNipfWoodyBiomassMean(double value) {
		nipfWoodyBiomassMean = value;
	}

	public void setNipfWoodyBiomassSd(double value) {
		nipfWoodyBiomassSd = value;
	}
	
	public void setVisualBufferRaster(String value) {
		visualBufferRaster = value;
	}

	public void setMerchantableProductivity(double value) {
		merchantableProductivity = value;
	}

	public void setBiomassChipping(double value) {
		biomassChipping = value;
	}

	public void setDieselPerLiter(double value) {
		dieselPerLiter = value;
	}

	public void setKmPerLiter(double value) {
		kmPerLiter = value;
	}

	public void setLoggerPerHour(double value) {
		loggerPerHour = value;
	}

	public void setDriverPerHour(double value) {
		driverPerHour = value;
	}

	public void setKmPerHour(double value) {
		kmPerHour = value;
	}

	public void setHarvestHours(double value) {
		harvestHours = value;
	}

	public void setChipVanCapacity(double value) {
		chipVanCapacity = value;
	}

	public void setCfMinimumDbh(double value) {
		cfMinimumDbh = value;
	}

	public void setCfReserve(double value) {
		cfReserve = value;
	}

	public void setNipfMinimumDbh(double value) {
		nipfMinimumDbh = value;
	}
	
	public void setChipperFuel(double value) {
		chipperFuel = value;
	}

	public void setWoodyBiomassRetention(double value) {
		woodyBiomassRetention = value;
	}
}
