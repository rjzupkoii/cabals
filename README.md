# CABALS: Agent-Based Life Cycle Sustainablity Assessment

## Introduction
CABALS is an anagram of agent-based life cycle sustainablity assessment (agent-based LCSA) and is a research project into the integration of agent-based modeling (ABM) and LCSA. 

## Organization
- /cabals - Files related to the interconnect code between ABM and life cycle assessment (LCA).
- /gis - Files related to the processing of geographic information system (GIS) data. 

## GIS Data
A limited collection of GIS data is included based upon the settings in `model/data/settings.ini` the files included are:

wup_cfr_forestsim.shp - Shapefiles that are limited to the Western Upper Peninsula (WUP) bounderies for Michigan, USA. These shapefiles were clipped to the region based upon the [Michigan Commercial Forest Land](https://gis-midnr.opendata.arcgis.com/datasets/0e78979ab94648b8b5e34759bbdc8cf2_5) shapefile produced by the State of Michigan. \
wup_nlcd_utm.asc - Clipped version of National Land Cover Database (NLCD) data for the commercial forest land (Homer et al. 2015). \
wup_evh_utm.asc - Clipped version of [LANDFIRE](https://www.landfire.gov/) height data (Rollins & Frame 2006). \
wup_evc_utm.asc - Clipped version of LANDFIRE cover data (Rollins & Frame 2006). \
wup_buff_utm.asc - A visual buffer file created by the author.

In order to use the GIS data, the file `gis/gis.zip` needs to be extracted and the files moved to `model/data/gis` in accordance with the settings. The GIS data should be aligned correctly for use with ForestSim (i.e., same number of rows, columns, and reference point). Note that while good compression is achived, when the files are decompressed they will be quite large.

### References
Homer, C. G., Dewitz, J. A., Yang, L., Jin, S., Danielson, P., Xian, G., Coulston, J., Herold, N. D., Wickham, J. D., & Megown, K. (2015). Completion of the 2011 National Land Cover Database for the conterminous United States-Representing a decade of land cover change information. *Photogrammetric Engineering and Remote Sensing*, 81(5), 345–354.

Rollins, M. G., & Frame, C. K. (2006). The LANDFIRE Prototype Project: Nationally consistent and locally relevant geospatial data for wildland fire management. Gen. Tech. Rep. RMRS-GTR-175. Fort Collins: US Department of Agriculture, Forest Service, Rocky Mountain Research Station. 416 p., 175.

### Publications
Zupko, R. (2021). Application of agent-based modeling and life cycle sustainability assessment to evaluate biorefinery placement. *Biomass and Bioenergy*, 144, 105916. https://doi.org/10.1016/j.biombioe.2020.105916
