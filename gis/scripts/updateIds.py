import arcpy, csv, datetime
from arcpy import env
from settings import *

ADDRESS_IDS = 'address-ids.csv'
NAMES_IDS = 'name-ids.csv'

def apply(id, parent, source):
    with arcpy.da.UpdateCursor(WUP_OWNERS, [ 'PARENT_ID', 'PARENT_SOURCE' ], 'OWNER_ID = ' + id) as cursor:
        for row in cursor:
            row[0] = parent
            row[1] = source
            cursor.updateRow(row)


def scan(file):
    # Note the tag
    source = file.replace('-ids.csv', '')

    # Scan the values, apply them if the parent isn't the same as the id
    with open(file, 'r') as file:
        reader = csv.reader(file)
        next(reader, None)
        for row in reader:
            if row[0] <> row[1]:
                apply(row[1], row[0], source)
            apply(row[2], row[0], source)
            

if __name__ == '__main__':
    # Prepare to run
    env.workspace = WUP_WORKSPACE

    # Apply the IDs
    scan(ADDRESS_IDS)
    scan(NAMES_IDS)

