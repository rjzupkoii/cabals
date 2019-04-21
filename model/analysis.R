#!/usr/local/bin/rscript

require(matrixStats)
require(gtools)

PATH = 'analysis/'

analysis <- function(file, prefix) {
	data <- loadData(file);
	
	process(data, prefix);
}

process <- function(data, prefix) {

	results <- data.frame();
	
	for (name in names(data[[1]])) {   
		# Start by combining each column into a single matrix
		working <- lapply(data, function(item)item[[name]]);
		working <- matrix(unlist(working), ncol = 50, byrow = TRUE);
		
		# Dump the data for the archive
		write.csv(working, file = paste(PATH, prefix, name, '.csv', sep = ''), row.names = FALSE);
		
		# Calculate the mean and SD for each year, bind to the results
		df <- data.frame(colMeans(working), colSds(working));
		names(df) <- c(paste(name, '.mean', sep = ''), paste(name, '.sd', sep = ''));
		
		results <- smartbind(results, df);	
	}
	
	str(results);
	
	# Dump the final results
	write.csv(results, file = paste(PATH, prefix, '.csv', sep =''), row.names = TRUE);
}

# Load all of the relevent files that match the name from disk,
# note that the assumed pattern is 'out/[file]*.csv'
loadData <- function(file) {

	# Load the data to a list
	pattern <- paste(file, '.*\\.csv$', sep = '');
	files <- list.files(pattern = pattern, recursive = TRUE);
	data <- lapply(files, read.csv);
	
	# Remove the last column, artifact of model
	data <- lapply(data, function(item)item[, -9]);

	# Return the data
	return(data);
}

dir.create(PATH, showWarnings = FALSE);

analysis('CfHarvesting', 'CfOps');

print(paste('Results dumpped to ', PATH, sep = ''));