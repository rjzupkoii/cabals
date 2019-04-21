#!/usr/local/bin/rscript

require(matrixStats)

PATH = 'analysis/'

analysis <- function(file, prefix) {
	data <- loadData(file);
	process(data, prefix);
}

process <- function(data, prefix) {

	# Allocate the rows
	rows <- dim(data[[1]])[1];
	results <- matrix(nrow = rows)

	for (name in names(data[[1]])) {   
		# Start by combining each column into a single matrix
		working <- lapply(data, function(item)item[[name]]);
		working <- matrix(unlist(working), ncol = rows, byrow = TRUE);
		
		# Dump the data for the archive
		write.csv(working, file = paste(PATH, prefix, name, '.csv', sep = ''), row.names = FALSE);
		
		# Calculate the mean and SD for each year, bind to the results
		df <- data.frame(colMeans(working), colSds(working));
		names(df) <- c(paste(name, '.mean', sep = ''), paste(name, '.sd', sep = ''));
		
		# Append the results to the working data
		results <- cbind(results, df)	
	}
	
	# Drop the first placeholder column created upon allocation
	results <- results[, -1];
	
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
	col <- dim(data[[1]]);
	data <- lapply(data, function(item)item[, -col]);
	
	# Return the data
	return(data);
}

dir.create(PATH, showWarnings = FALSE);

analysis('CfHarvesting', 'CfOps');
analysis('NipfHarvesting', 'NipfOps');
analysis('Transport', 'TransportOps');

print(paste('Results dumpped to ', PATH, sep = ''));