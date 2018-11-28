# This script scans the WUP_SHAPEFILE shapefile and updates empty Owner_Id's
# with the correct ID. This is meant to be a one time data load script so it
# is on the slower side.
import arcpy, datetime
from arcpy import env  
from settings import *


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

        # Since we expect only one match, return the first one
        with arcpy.da.SearchCursor(WUP_OWNERS, [ "OWNER_ID" ], where) as cursor:
                for row in cursor:
                        return row[0]
        return None


def updateOwner(data, county_id):
        # Check if the owner is set already
        where =  "PIN = '" + data[0] + "' AND County_Id = " + str(county_id)
        with arcpy.da.UpdateCursor(WUP_SHAPEFILE, [ "PIN", "Owner_Id" ], where) as cursor:
                for row in cursor:
                        if row[1] <> 0: continue
                        # Owner was not set, so update it if we can find them
                        owner_id = loadOwner(data)
                        if owner_id is None: continue
                        row[1] = owner_id
                        cursor.updateRow(row)


def main():
        # Prepare ArcGIS to work
        print datetime.datetime.now(), "Loading..."
        env.workspace = WUP_WORKSPACE  


        # Open search cursors on the secondary files
        for key in WUP_COUNTIES:
                print datetime.datetime.now(), "Processing County: ", key
                values = WUP_COUNTIES.get(key)
                path = values[0]
                values = values[1:]
                if '' in values: values.remove('')

                # Iterate over the county plats, attempt to update the owners
                print datetime.datetime.now(), "Feature Count: ", arcpy.GetCount_management(path)
                count = 0
                with arcpy.da.SearchCursor(path, values) as cursor:
                        for row in cursor:
                                updateOwner(row, key)
                                count = count + 1
                                if count % 100 == 0: print datetime.datetime.now(), "Processed: ", count
                print datetime.datetime.now(), "Processed: ", count


if __name__ == '__main__':
        main()
