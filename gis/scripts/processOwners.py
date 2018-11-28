import arcpy, sys, os, fileinput, glob, string  
from arcpy import env  

from settings import *

# The reference files are unindexed, so opening a cursor and
# scanning them is faster
searchCursors = {}


def load():
        # Open search cursors on the secondary files
        for key in WUP_COUNTIES:
                values = WUP_COUNTIES.get(key)
                path = values[0]
                values = values[1:]
                if '' in values: values.remove('')
                searchCursors[key] = arcpy.da.SearchCursor(path, values)    


def loadOwner(data):
        # Prepare the where clause
        where = "OWNER = '" + data[1].replace("'", "''") + "' AND " + \
                "ADDRESS_ONE = '" + data[2] + "' AND "
        offset = 1
        if len(data) == 7:
                offset = 0
                where = where + "ADDRESS_TWO = '" + data[3] + "' AND "
        where = where + "CITY = '" + data[4 - offset].replace("'", "''") + "' AND " + \
                        "STATE = '" + data[5 - offset] + "' AND " + \
                        "ZIPCODE = '" + str(data[6 - offset]) + "'"
        
        owners = arcpy.da.SearchCursor(WUP_OWNERS, [ "OWNER_ID" ], where)
        for row in owners:
                return row[0]
        print data[0]
        return None


def search(pin, county):
        # Check to make sure we have a valid county
        if county not in searchCursors:
                print 'County not found: ', county
                return None

        # Scan for the matching PIN
        cursor = searchCursors.get(county)
        cursor.reset()
        for row in cursor:
                if row[0] == pin: return row
        return None


def main():
        # Prepare ArcGIS to work
        print "Loading..."
        env.workspace = WUP_WORKSPACE  
          
        # Prepare our cursors
        cursor = arcpy.da.UpdateCursor(WUP_SHAPEFILE, [ "PIN", "County_Id", "Owner_Id" ])
        load()

        # Iterate over the features
        print "Processing..."
        updated = 0
        for row in cursor:
                # Skip if the owner is already set
                if row[2] <> 0: continue

                # Search for a result
                result = search(row[0], row[1])
                if result is not None :
                        owner_id = loadOwner(result)
                        if owner_id is None: continue
                        row[2] = owner_id
                        cursor.updateRow(row)
                        updated = updated + 1

        # Notify the user
        print "Done"                                
        print "Updated: ", updated


if __name__ == '__main__':
        main()
