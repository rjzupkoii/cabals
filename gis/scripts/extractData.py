import arcpy, csv, datetime, numpy
from arcpy import env
from settings import *

# Flags related to the GIS structure
COLUMNS = [ 'OWNER_ID', 'NATIVE', 'FEDERAL', 'STATE_GOV', 'LOCAL_GOV', 'CFR', 'NIPFO', 'TRUST', 'FOREST_AC', 'WETLAND_AC', 'ACRES' ]
OWNER_ID = 0
FOREST_AC = 8
WETLAND_AC = 9
ACRES = 10

# Trackers for total work
discard = []
processed = []

# Trackers for labeled work
indexing = []
forest = {}
wetland = {}
acres = {}

# Tracker for unlabeled work
unclassified = {"forest":[], "wetland":[], "acres":[]}

# Tracker for owners
owners = {}

def main(path):
    # Prepare to run
    env.workspace = WUP_WORKSPACE
    for ndx in range(1, 8):
        index = COLUMNS[ndx]
        indexing.append(index)
        forest[index] = []
        wetland[index] = []
        acres[index] = []
    print indexing

    # Status update
    print path
    print datetime.datetime.now(), 0

    # Scan all of the entries
    count = 0
    with arcpy.da.SearchCursor(path, COLUMNS) as cursor:
        for row in cursor:
            process(row)
            count = count + 1
            if count % 1000 == 0: print datetime.datetime.now(), count
    print datetime.datetime.now(), count

            
def process(row):
    # Discard if there is less than 1 ac of forest
    if row[FOREST_AC] < 1:
        discard.append(row[ACRES])
        return

    # Note the basic data
    processed.append(row[ACRES])

    # Add the data to the relevent maps
    found = False
    for ndx in range(0, len(indexing)):
        # Pass if the label is not set
        if row[ndx + 1] == 0: continue

        # Add them and note a label
        found = True
        forest[indexing[ndx]].append(row[FOREST_AC])
        wetland[indexing[ndx]].append(row[WETLAND_AC])
        acres[indexing[ndx]].append(row[ACRES])

    # Dump in the unclassified if a label was not set
    if not found:
        unclassified["forest"].append(row[FOREST_AC])
        unclassified["wetland"].append(row[WETLAND_AC])
        unclassified["acres"].append(row[ACRES])

    # Add the data to the owner map
    owner = row[OWNER_ID]
    if not(row[OWNER_ID] in owners):
        owners[owner] = {"forest":[], "wetland":[], "acres":[]}
    owners[owner]["forest"].append(row[FOREST_AC])
    owners[owner]["wetland"].append(row[WETLAND_AC])
    owners[owner]["acres"].append(row[ACRES])


def write():
    with open('results.csv', 'wb') as out:
        writer = csv.writer(out, delimiter=',', quotechar='"')
        writer.writerow(["label", "type", "count", "sum", "mean", "sd"])
        for index in indexing:
            if len(acres[index]) == 0: continue
            writer.writerow([index, "forest", len(forest[index]), numpy.sum(forest[index]), numpy.mean(forest[index]), numpy.std(forest[index])])
            writer.writerow([index, "wetland", len(wetland[index]), numpy.sum(wetland[index]), numpy.mean(wetland[index]), numpy.std(wetland[index])])
            writer.writerow([index, "parcel", len(acres[index]), numpy.sum(acres[index]), numpy.mean(acres[index]), numpy.std(acres[index])])
        writer.writerow(["UNCLASSIFIED", "forest", len(unclassified["forest"]), numpy.sum(unclassified["forest"]), numpy.mean(unclassified["forest"]), numpy.std(unclassified["forest"])])
        writer.writerow(["UNCLASSIFIED", "wetland", len(unclassified["wetland"]), numpy.sum(unclassified["wetland"]), numpy.mean(unclassified["wetland"]), numpy.std(unclassified["wetland"])])
        writer.writerow(["UNCLASSIFIED", "parcel", len(unclassified["acres"]), numpy.sum(unclassified["acres"]), numpy.mean(unclassified["acres"]), numpy.std(unclassified["acres"])])

    with open('owners.csv', 'wb') as out:
        writer = csv.writer(out, delimiter=',', quotechar='"')
        writer.writerow(["owner_id", "count", "sum_acres", "mean", "sd", "sum_forest", "mean", "sd", "sum_wetland", "mean", "sd"])
        for owner_id in owners.keys():
            owner = owners[owner_id]
            row = [ owner_id, len(owner["acres"]), numpy.sum(owner["acres"]), numpy.mean(owner["acres"]), numpy.std(owner["acres"]),
                                                   numpy.sum(owner["forest"]), numpy.mean(owner["forest"]), numpy.std(owner["forest"]),
                                                   numpy.sum(owner["wetland"]), numpy.mean(owner["wetland"]), numpy.std(owner["wetland"])]
            writer.writerow(row)


if __name__ == '__main__':
    main(WUP_NLCD_2011)
    write()    

    # Print summary results
    print
    print "Processed Parcels:", len(processed)
    print "Sum:", numpy.sum(processed), "Mean:", numpy.mean(processed), "SD:", numpy.std(processed)
    print "Discarded Parcels:", len(discard)
    print "Sum:", numpy.sum(discard), "Mean:", numpy.mean(discard), "SD:", numpy.std(discard)
    print
    for index in indexing:
        if len(acres[index]) == 0: continue
        print index, "Parcels:", len(acres[index])
        print "Forest  / Sum: ", numpy.sum(forest[index]), "Mean:", numpy.mean(forest[index]), "SD:", numpy.std(forest[index])
        print "Wetland / Sum: ", numpy.sum(wetland[index]), "Mean:", numpy.mean(wetland[index]), "SD:", numpy.std(wetland[index])
        print "Acres:  / Sum: ", numpy.sum(acres[index]), "Mean:", numpy.mean(acres[index]), "SD:", numpy.std(acres[index])

