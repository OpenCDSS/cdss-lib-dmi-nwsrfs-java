//-----------------------------------------------------------------------------
// NWSRFS_BASN - class to store the organizational information about an 
//               NWSRFS Basin boundary parameters.
//-----------------------------------------------------------------------------
// History:
//
// 
// 2004-09-30	Scott Townsend, RTi	Initial version.
//-----------------------------------------------------------------------------
// Endheader


package RTi.DMI.NWSRFS_DMI;

import java.util.List;
import java.util.Vector;

/**
The NWSRFS_BASN class stores the organizational information about an
NWSRFS Basin boundary parameters. This class holds the information stored in the 
preprocesseor parameteric database for basin information. It holds the parameters 
from the BASN parameteric datatype which is a basin parameter
type. All of the parameters are read from the FS5Files binary DB file: 
PPPPARM<i>n</i> where <i>n</i> is determined from the preproccesed database index
file PPPINDEX. All PPDB (Preprocessed Parameteric DataBase) parameters actually 
reside in an array on the DB. This array must be parsed according to the type of 
parameter requested. For more information see below:
<pre>
    IX.4.3B-PPPPARMn  PREPROCESSOR PARAMETRIC DATA BASE FILE PPPPARMn

Purpose
Files PPPPARMn contain the Preprocessor Parametric Data Base parameter records.

Description
ATTRIBUTES: fixed length 64 byte binary records
RECORD STRUCTURE:

            Word
Variable    Type    Dimension    Position    Description

The first record in the file is a file control record:

MAXREC      I*4     1            1           Maximum records

LASTRC      I*4     1            2           Last record used

NUMPRM      I*4     1            3           Number of parameter 
                                             records in file

                                 4+          Unused

The remaining records in the file are the Parameter Records.

For regular parameter types:
NWRDS       I*4     1            1           Number of words in record

ID          A8      1            2-3         Identifier

ITYPE       A4      1            4           Parameter type

IRECNX      I*4     1            5           Record number of next 
                                             parameter record of this 
                                             type

PARMS       R*4     NWRDS-5      6+          Parameters

For special parameter types: 1/
Special parameter type control record: 2/
NWORDS      I*4     1            1           Number of words in 
                                             control record

FTYPE       I*4     1            2           Record number of first 
                                             entry

NTYPER      I*4     1            3           Number of records for 
                                             each entry
 
NTYPES      I*4     1            4           Number of values per 
                                             entry

TYPE        A4      1            5           Parameter type

NENTRY      I*4     1            6           Number of entries per 
                                             station

NSTAS       I*4     1            7           Number of stations 
                                             defined

MAXSTA      I*4     1            8           Maximum number of 
                                             stations

NXTSTA      I*4     1            9           Last station slot used

Special parameter type record: 3/
PARMS       I*4     ?            1           Special parameters 4/

Notes:

1/  Special parameter types have records that hold the same information for 
    each station (or other entity) for each month (or other key).  For these 
    types space is reserved for all possible entries when the files are 
    created.  These records are for station precipitation characteristics 
    (CHAR) and mean monthly maximum/minimum temperatures (MMMT) which are 
    stored by month for all stations.

2/  This record design is flexible and can accept additional special parameter 
    types if necessary.  This record provides the information needed to compute 
    the record number needed for a special parameter type and will precede 
    the set of special parameter records.  By using that first record of the 
    type and the number of physical records for each entry the record for the 
    appropriate month (or other key) is computed.  The Parameter Type Directory 
    points to this record for CHAR and MMMT. 
    
    Any previously deleted station slots will be reused before using the 
    next available slot.

3/  The special parameter type records immediately follow the special 
    parameter type control record.

4/  If the special parameter type is 'CHAR' then the values stored are in 
    units of hundredths of an IN.

    If the special parameter type is 'MMMT' then the values stored are in 
    units of tenths of DEGF.
    
                IX.4.3B-PPPPARMn
</pre>
<p>
<pre>
IX.4.3C-BASN     PREPROCESSOR PARAMETRIC DATA BASE PARAMETER ARRAY BASN:
                 BASIN BOUNDARY PARAMETERS

Purpose
Parameter array BASN contains basin boundary parameters for an area.

Array Contents
  Starting                         Input/
  Position     Dimension   Type   Generated Description

      1            1       I*4        G      Parameter array version number

      2            1        A8        I      Basin boundary identifier

      4            1       A20        I      Description

      9            1       R*4        I      Mean elevation; units of M

     10            1       R*4        I      Basin area; units of KM2

     11            1       R*4        G      Computed basin area; units of
                                             KM2

     12            2       I*4        G      Centroid of area; NWSRFS/HRAP
                                             coordinates stored as (X,Y)

     14            1        A8        G      Identifier of MAP area that
                                             uses this basin boundary 1/

     16            1        A8        G      Identifier of MAT area that
                                             uses this basin boundary 1/

     18            1       I*4        G      Update indicator for MAP
                                             parameters: 2/
                                                0 = not updated
                                                1 = updated

     19            1       I*4        G      Update indicator for MAT
                                             parameters: 2/
                                                0 = not updated
                                                1 = updated

     20            1        A8        G      Identifier of NEXRAD MAP (MAPX)
                                             area that uses this basin
                                             boundary 3/

     22            1       I*4        G      NWSRFS/HRAP grid spacing factor

     23            1       I*4        G      Number of pairs of basin
                                             boundary points (NBPTS)

     24            1       I*4        G      Number of NWSRFS/HRAP grid
                                             segments used to define the
                                             basin (NSEGS)

     25        (NBPTS,2)   R*4        I      Pairs of basin boundary points;
                                             latitude and longitude; units
                                             of decimal degrees; stored in
                                             clockwise order

 25+2*NBPTS    (NSEGS,3)   I*4        G      Grid point definition 4/

Notes:

1/ Blank if not referenced.

2/ Indicates whether parameters were updated since basin boundary was
   defined or redefined.

3/ Set to -999.0 if not referenced.

4/ The grid point definition for the basin is stored as NWSRFS/HRAP
   grid segments. Each segment is defined by the row number and the
   beginning and ending column on the row. The row numbers for all
   segments are stored first followed by the beginning column for all
   segments and then the ending column for all segments.

   The grid point definition values are stored as actual I*4 bytes.

                      IX.4.3C-BASN-2
</pre>
*/
public class NWSRFS_BASN {

// General parameters set for all Basin parameters.
/**
Identifier for the Basin.
*/
protected String _ID;

/**
This logical unit number corresponds to the <i>n</i> in the file
PPPPARM<i>n</i> and is used to pull all of the Basin information.
*/
protected int _logicalUnitNum;

/**
The record number in the PPPINDEX file for retrieving data from the 
PPPPARM<i>n</i> file.
*/
protected int _recordNum;

/**
Basin Area in KM^2.
*/
protected float _basinArea;

/**
Basin boundary points latitude.
*/
protected List _basinLatitude;

/**
Basin boundary points longitude.
*/
protected List _basinLongitude;

/**
Centroid of the basin area, NWSRFS/HRAP X coordinate
*/
protected int _centroidX;

/**
Centroid of the basin area, NWSRFS/HRAP Y coordinate
*/
protected int _centroidY;

/**
Computed Basin Area in KM^2.
*/
protected float _computedBasinArea;

/**
Basin description.
*/
protected String _description;

/**
Basin grid point definition. A Vector of 3-value Vectors.
*/
protected List _gridDef;

/**
MAP ID that uses Basin boundary.
*/
protected String _mapID;

/**
Update indicator for MAP parameters.
*/
protected int _mapUpdateInd;

/**
NEXRAD MAP (MAPX) ID that uses Basin boundary.
*/
protected String _mapxID;

/**
MAT ID that uses Basin boundary.
*/
protected String _matID;

/**
Update indicator for MAT parameters.
*/
protected int _matUpdateInd;

/**
Basin Mean Elevation.
*/
protected float _meanElevation;

/**
Number of pairs of basin boundary points (NBPTS).
*/
protected int _NBPTS;

/**
Number of NWSRFS/HRAP grid segments used to define the basin (NSEGS).
*/
protected int _NSEGS;

/**
Basin NWSRFS/HRAP grid spacing factor.
*/
protected int _spacingFactor;

/**
Constructor.
@param id MAT Area ID.  Can not be null
*/
public NWSRFS_BASN(String id) {
	initialize();

	if (id != null) {
		_ID = id;
	}
}

/**
Adds values to the Basin Latitude Vector. Each element is latitude of the
boundary points stored in clockwise order
Member method for Basin parameter variables
@param latitude the latitude for a basin boundary point.
*/
public void addBASNLatitude(float latitude) {
	_basinLatitude.add(new Float(latitude));
}

/**
Adds values to the Basin Longitude Vector. Each element is longitude of the
boundary points stored in clockwise order
Member method for Basin parameter variables
@param longitude the longitude for a basin boundary point.
*/
public void addBASNLongitude(float longitude) {
	_basinLongitude.add(new Float(longitude));
}

/**
Adds values to the Grid point definition Vector. Each element is a Vector of
grid point definitions. The grid point definition is three int's: row number, 
beginning column, and ending column in THAT ORDER! This is a Vector of Vectors.
Member method for Basin parameter variables
@param gpDef the grid point definition Vector to add to this Vector.
*/
public void addBASNGridPointDef(List gpDef) {
	_gridDef.add(gpDef);
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize() 
throws Throwable {
	_ID                 = null;
	_logicalUnitNum     = -1;
	_basinArea          = -1;
	_basinLatitude      = null;
	_basinLongitude     = null;
	_centroidX          = -1;
	_centroidY          = -1;
	_computedBasinArea  = -1;
	_description        = null;
	_gridDef            = null;
	_mapID              = null;
	_mapUpdateInd       = -1;
	_mapxID             = null;
	_matID              = null;
	_matUpdateInd       = -1;
	_meanElevation      = -1;
	_NBPTS              = -1;
	_NSEGS              = -1;
	_recordNum          = -1;
	_spacingFactor      = -1;
}

// GET Member methods for general Basin variables
/**
Returns the Basin boundary identifier.
Member method for general Basin variables
@return the Basin Boundary identifier.
*/
public String getID() {
	return _ID;
}

/**
Returns the Basin Boundary description.
Member method for Basin parameter variables
@return the Basin Boundary description.
*/
public String getDescription() {
	return _description;
}

/**
Returns the logical unit number for the Basin.
@return the logical unit number for the Basin.
*/
public int getLogicalUnitNum() {
	return _logicalUnitNum;
}

/**
Returns the Basin Area in KM^2.
@return the Basin Area.
*/
public float getBASNArea() {
	return _basinArea;
}

/**
Returns the Basin Centroid X HRAP Coordinate.
@return the Basin Centroid X HRAP coordinate.
*/
public int getBASNCentroidX() {
	return _centroidX;
}

/**
Returns the Basin Centroid Y HRAP Coordinate.
@return the Basin Centroid Y HRAP coordinate.
*/
public int getBASNCentroidY() {
	return _centroidY;
}

/**
Returns the Computed Basin Area in KM^2.
@return the Computed Basin Area.
*/
public float getBASNComputedBasinArea() {
	return _computedBasinArea;
}

/**
Returns the Vector of the latitude of basin boundary points.
Member method for Basin parameter variables
@return the Vector of the latitudes of basin boundary points.
*/
public List getBASNLatitude() {
	return _basinLatitude;
}

/**
Returns the latitude of basin boundary points at an index.
Member method for BASN parameter variables
@param basnLatIndex index to get the specific element in the Latitude Vector
@return the latitude of basin boundary points at an index.
*/
public float getBASNLatitude(int basnLatIndex) {
	return (float)((Float)_basinLatitude.get(basnLatIndex)).floatValue();
}

/**
Returns the Vector of the longitude of basin boundary points.
Member method for Basin parameter variables
@return the Vector of the longitudes of basin boundary points.
*/
public List getBASNLongitude() {
	return _basinLongitude;
}

/**
Returns the longitude of basin boundary points at an index.
Member method for BASN parameter variables
@param basnLongIndex index to get the specific element in the Longitude Vector
@return the longitude of basin boundary points at an index.
*/
public float getBASNLongitude(int basnLongIndex) {
	return (float)((Float)_basinLongitude.get(basnLongIndex)).floatValue();
}

/**
Returns the Vector of the grid point definition Vectors.
Member method for Basin parameter variables
@return the Vector of the grid point definition Vectors.
*/
public List getBASNGridPointDef() {
	return _gridDef;
}

/**
Returns the grid point definition list at an index.
Member method for BASN parameter variables
@param gridDefIndex index to get the specific element in the grid point 
definition Vector
@return the grid point definition Vector at an index.
*/
public List getBASNGridPointDef(int gridDefIndex) {
	return (List)_gridDef.get(gridDefIndex);
}

/**
Returns the MAP ID for the Basin Boundary.
@return the MAP ID for the Basin Boundary.
*/
public String getBASNMAPID() {
	return _mapID;
}

/**
Returns the MAP Update Indicator for the Basin Boundary.
@return the MAP Update Indicator for the Basin Boundary.
*/
public int getBASNMAPUpdateInd() {
	return _mapUpdateInd;
}

/**
Returns the MAPX ID for the Basin Boundary.
@return the MAPX ID for the Basin Boundary.
*/
public String getBASNMAPXID() {
	return _mapxID;
}

/**
Returns the MAT ID for the Basin Boundary.
@return the MAT ID for the Basin Boundary.
*/
public String getBASNMATID() {
	return _matID;
}

/**
Returns the MAT Update Indicator for the Basin Boundary.
@return the MAT Update Indicator for the Basin Boundary.
*/
public int getBASNMATUpdateInd() {
	return _matUpdateInd;
}

/**
Returns the Basin Mean Elevation in M.
@return the Basin Mean Elevation.
*/
public float getBASNMeanElevation() {
	return _meanElevation;
}

/**
Returns the number of pairs of Basin Boundary points.
@return the number of pairs of Basin Boundary points.
*/
public int getBASNNBPTS() {
	return _NBPTS;
}

/**
Returns the number of HRAP grid segments.
@return the number of HRAP grid segments.
*/
public int getBASNNSEGS() {
	return _NSEGS;
}

/**
Returns the HRAP grid spacing factor.
@return the HRAP grid spacing factor.
*/
public int getBASNGridSpacingFactor() {
	return _spacingFactor;
}

/**
Returns the record number for the Basin Boundary.
@return the record number for the Basin Boundary.
*/
public int getRecordNum() {
	return _recordNum;
}

/**
Initialize data members.
*/
private void initialize() {
	_ID                 = null;
	_logicalUnitNum     = -1;
	_basinArea          = -1;
	_basinLatitude      = new Vector();
	_basinLongitude     = new Vector();
	_centroidX          = -1;
	_centroidY          = -1;
	_computedBasinArea  = -1;
	_description        = null;
	_gridDef            = new Vector();
	_mapID              = null;
	_mapUpdateInd       = -1;
	_mapxID             = null;
	_matID              = null;
	_matUpdateInd       = -1;
	_meanElevation      = -1;
	_NBPTS              = -1;
	_NSEGS              = -1;
	_recordNum          = -1;
	_spacingFactor      = -1;
}

// SET Member methods for general Basin variables
/**
Sets the Basin Boundary identifier.
Member method for general Basin variables
*/
public void setID(String id) {
	_ID = id;
}

/**
Sets a logical unit number to the _logicalUnitNum int. This value will be
the <i>n</i> in the file PPPPARM<i>n</i>.
@param logicalUnitNum is the actual file unit number to set.
*/
public void setLogicalUnitNum(int logicalUnitNum) {
	_logicalUnitNum = logicalUnitNum;
}

/**
Sets the Basin Boundary description.
Member method for Basin parameter variables
@param description is the Basin Boundary description.
*/
public void setDescription(String description) {
	_description = description;
}

/**
Sets the Basin Area in KM^2.
@param basinArea the Basin Area.
*/
public void setBASNArea(float basinArea) {
	_basinArea = basinArea;
}

/**
Sets the Basin Centroid X HRAP Coordinate.
@param centroidX the Basin Centroid X HRAP coordinate.
*/
public void setBASNCentroidX(int centroidX) {
	_centroidX = centroidX;
}

/**
Sets the Basin Centroid Y HRAP Coordinate.
@param centroidY the Basin Centroid Y HRAP coordinate.
*/
public void setBASNCentroidY(int centroidY) {
	_centroidY = centroidY;
}

/**
Sets the Computed Basin Area in KM^2.
@param computedBasinArea the Computed Basin Area.
*/
public void setBASNComputedBasinArea(float computedBasinArea) {
	_computedBasinArea = computedBasinArea;
}

/**
Sets the MAP ID for the Basin Boundary.
@param mapID the MAP ID for the Basin Boundary.
*/
public void setBASNMAPID(String mapID) {
	_mapID = mapID;
}

/**
Sets the MAP Update Indicator for the Basin Boundary.
@param mapUpdateInd the MAP Update Indicator for the Basin Boundary.
*/
public void setBASNMAPUpdateInd(int mapUpdateInd) {
	_mapUpdateInd = mapUpdateInd;
}

/**
Sets the MAPX ID for the Basin Boundary.
@param mapxID the MAPX ID for the Basin Boundary.
*/
public void setBASNMAPXID(String mapxID) {
	_mapxID = mapxID;
}

/**
Sets the MAT ID for the Basin Boundary.
@param matID the MAT ID for the Basin Boundary.
*/
public void setBASNMATID(String matID) {
	_matID = matID;
}

/**
Sets the MAT Update Indicator for the Basin Boundary.
@param matUpdateInd the MAT Update Indicator for the Basin Boundary.
*/
public void setBASNMATUpdateInd(int matUpdateInd) {
	_matUpdateInd = matUpdateInd;
}

/**
Sets the Basin Mean Elevation in M.
@param meanElevation the Basin Mean Elevation.
*/
public void setBASNMeanElevation(float meanElevation) {
	_meanElevation = meanElevation;
}

/**
Sets the number of pairs of Basin Boundary points.
@param NBPTS the number of pairs of Basin Boundary points.
*/
public void setBASNNBPTS(int NBPTS) {
	_NBPTS = NBPTS;
}

/**
Sets the number of HRAP grid segments.
@param NSEGS the number of HRAP grid segments.
*/
public void setBASNNSEGS(int NSEGS) {
	_NSEGS = NSEGS;
}

/**
Sets the HRAP grid spacing factor.
@param spacingFactor the HRAP grid spacing factor.
*/
public void setBASNGridSpacingFactor(int spacingFactor) {
	_spacingFactor = spacingFactor;
}

/**
Sets a record number to the _recordNum int. This value will be
the record in the file PPPPARM<i>n</i> file to find data for the given Basin 
Boundary ID.
@param recordNum is the actual record number.
*/
public void setRecordNum(int recordNum) {
	_recordNum = recordNum;
}

/**
toString method prints name of ID.
@return a String value with _ID value or "BASN:"_ID value
*/
public String toString() {
	return _ID;
}

}
