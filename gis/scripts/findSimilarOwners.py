import arcpy, csv, datetime
from arcpy import env
from settings import *

# This number is kind of arbitrary
MIN_EDITS = 5

# Files to be written to
ADDRESS_MATCH = 'addresses.csv'
NAME_MATCH = 'names.csv'

# Use the Levenshtein distance to scan the strings
# 
# https://stackoverflow.com/questions/6400416
def levenshtein_distance(a,b):
    n, m = len(a), len(b)
    if n > m:
        # Make sure n <= m, to use O(min(n,m)) space
        a,b = b,a
        n,m = m,n

    current = range(n+1)
    for i in range(1,m+1):
        previous, current = current, [i]+[0]*n
        for j in range(1,n+1):
            add, delete = previous[j]+1, current[j-1]+1
            change = previous[j-1]
            if a[j-1] != b[i-1]:
                change = change + 1
            current[j] = min(add, delete, change)

    return current[n]


def checkOwner(one, two):
    empty = [ ' ', ' ', ' ', ' ', ' ' ]
    
    # If the names are exactly the same, flag them
    if one[1] == two[2]:
        csv.writer(open(NAME_MATCH, 'a'), delimiter=',', quotechar='"').writerow(one + two)
        return

    # Return if we can't check the addresses
    if one[2:] == empty or two[2:] == empty: return

    # Next, check to see if the addresses are exactly the same
    if one[2:] == two[2:]:
        dist = []
        dist.append(levenshtein_distance(one[1], two[1]))
        csv.writer(open(ADDRESS_MATCH, 'a'), delimiter=',', quotechar='"').writerow(dist + one + two)


def main():
    # Prepare to run
    print datetime.datetime.now(), "Staring scan..."
    env.workspace = WUP_WORKSPACE
    columns = [ 'OWNER_ID', 'OWNER', 'ADDRESS_ONE', 'ADDRESS_TWO', 'CITY', 'STATE', 'ZIPCODE' ]

    # Scan all of the entries into an array
    working = []
    with arcpy.da.SearchCursor(WUP_OWNERS, columns) as cursor:
        for row in cursor:
            working.append(row)

    # Use the array to scan the names
    touched = 0
    for ndx in xrange(0, len(working)):
        for ndy in xrange(ndx + 1, len(working)):
            checkOwner(list(working[ndx]), list(working[ndy]))
            touched = touched + 1
            
        print datetime.datetime.now(), touched
    
    print touched


if __name__== '__main__':
    main()
