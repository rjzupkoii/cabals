 #!/bin/bash
 
# Note we are preventing sleep
echo "Preventing sleep during simulations"
caffeinate &
 
# Set the total runs
total=1
repeat=10
 
# Run the simulation
for ndx in $(seq 1 $total);
do
	date
	echo "Block $ndx of $total"
	java -Xms4G -XX:+UseG1GC -jar cabals.jar -for 50 -repeat $repeat -time 10
done
date

 # Restore sleep
killall caffeinate
 