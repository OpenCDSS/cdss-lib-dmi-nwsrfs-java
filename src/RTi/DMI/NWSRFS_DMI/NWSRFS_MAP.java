//-----------------------------------------------------------------------------
// NWSRFS_MAP - class to store the organizational information about an 
//              NWSRFS MAP Area.
//-----------------------------------------------------------------------------
// History:
//
// 
// 2004-09-28	Scott Townsend, RTi	Initial version.
//-----------------------------------------------------------------------------
// Endheader


package RTi.DMI.NWSRFS_DMI;

/**
The NWSRFS_MAP class stores the organizational information about an
NWSRFS MAP Areal parameters. This class holds the information stored in the 
preprocesseor parameteric database for areal information. It holds the parameters 
from the MAP parameteric datatype which is an areal parameter
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
IX.4.3C-MAP       PREPROCESSOR PARAMETRIC DATA BASE PARAMETER ARRAY MAP:
                  MEAN AREAL PRECIPITATION (MAP) AREA PARAMETERS

Purpose
Parameter array MAP contains area parameters used to compute Mean
Areal Precipitation (MAP) for an MAP area.

Array Contents
    Starting                                Input/
    Position         Dimension     Type    Generated    Description
       1                 1         I*4         G        Parameter array version
                                                          number


       2                 1          A8         I        MAP area identifier


       4                 1         A20         I        Description


       9                 1         I*4         I        Data time interval;
                                                        units of HR 1/


       10                1          A8        I         Identifier of basin
                                                        boundary used by this
                                                        area 2/


       12                1         I*4         I        Type of timing weights:
                                                              1 = predetermined
                                                              2 = 1/D**2
                                                              3 = 1/D**POWER


       13                1         R*4         I        Exponent in 1/D**POWER
                                                        3/


       14                1         I*4      I or G      Number of stations used
                                                        for time distribution
                                                        (NSTWT):
                                                              4 = 1/D**2 weights
                                                            >10 = predetermined
                                                                  or 1/D**POWER
                                                                  weights


       15                1         I*4         I        Type of station weights:
                                                              1 = predetermined
                                                              2 = grid point
                                                              3 = Thiessen
                                                              4 = 1/D**POWER


       16                1          A8        I         Identifier of Future MAP
                                                        area used by this area


       18                2         R*4      I or G      Centroid; NWSRFS/HRAP
                                                        coordinates stored as
                                                        (X,Y)


       20              2         R*4         G        Unused


       22              1         I*4         G        Number of stations used
                                                      to compute MAP (NPCPN)
                                                      4/


       23              1         I*4         G        Number of sets of
                                                      station weights (NSETS)
                                                      5/


       24             NSTWT       A8      I or G      Station identifiers of
                                                      timing stations


      24+2*NSTWT      NSTWT      R*4      I or G      Timing weights


      24+3*NSTWT      (2,NSTWT)  I*4          G        NWSRFS/HRAP coordinates
                                                       of timing stations;
                                                       stored as (X,Y) 6/


      24+3*NSTWT      NPCPN       A8      I or G       Station identifiers of 
        +(2*NSTWT*                                     stations used to compute
          ITM)                                         MAP
        
      24+3*NSTWT   (NPCPN,NSETS)  R*4     I or G       24 hour MAP station
      +(2*NSTWT*                                       weights 7/
          ITM)
        +NPCPN


      24+3*NSTWT   (2,NPCPN)      I*4         G         NWSRFS/HRAP coordinates
        +(2*NSTWT*                                      of stations used to 
          ITM)                                          compute MAP; stored as
        +NPCPN                                          (X,Y) 6/
        +(NPCPN*
          NSETS)


Notes:
1/    Currently can be only 6 hours.

2/    Defined only if grid point or Thiessen weights used or MDR boxes
      are automatically determined.  Blank if not defined.

3/    Only defined if 1/D**POWER timing or station weights are used.

4/    No maximum value.

5/    One or two sets can be defined.  Two sets can be defined only if
      predetermined weights are being used.

6/    NWSRFS/HRAP coordinates are stored only if predetermined weights
      are not used.  ITM is zero if predetermined timing weights are
      being used and 1 if not.

7/    If two sets of weights are defined then the winter weights are
      stored first followed by the summer weights.

07/22/2004                           IX.4.3C-MAP-2
</pre>
*/
public class NWSRFS_MAP {

// General parameters set for all MAP Area parameters.
// REVISIT SAT 09/28/2004 Need to fill out the Class more. Many parameters are
// not included since there is no budget to fill it out.
/**
Identifier for the MAP Area.
*/
protected String _ID;

/**
This logical unit number corresponds to the <i>n</i> in the file
PPPPARM<i>n</i> and is used to pull all of the MAP information.
*/
protected int _logicalUnitNum;

/**
The record number in the PPPINDEX file for retrieving data from the 
PPPPARM<i>n</i> file.
*/
protected int _recordNum;

/**
MAP Area Basin Boundary Identifier.
*/
protected String _basinBoundaryID;

/**
MAP Area Centroid X coordinate
*/
protected float _centroidX;

/**
MAP Area Centroid Y coordinate
*/
protected float _centroidY;

/**
MAP Area Data Time Interval; in units of Hours.
*/
protected int _dataTimeInt;

/**
MAP Area description.
*/
protected String _description;

/**
MAP Area Exponent in 1/D**POWER.
*/
protected float _exponent;

/**
MAP Area Future MAP Area used by this MAP Area.
*/
protected String _fmapAreaID;

/**
MAP Area Number of stations used for time distribution..
*/
protected int _NSTWT;

/**
MAP Area Type of Station Weights.
*/
protected int _typeStationWeights;

/**
MAP Area Data Type of Time Weights.
*/
protected int _typeTimeWeights;

/**
Constructor.
@param id MAP Area ID.  Can not be null
*/
public NWSRFS_MAP(String id) {
	initialize();

	if (id != null) {
		_ID = id;
	}
}

// GET Member methods for general MAP Area variables
/**
Returns the MAP Area's identifier.
Member method for general MAP Area variables
@return the MAP Area's identifier.
*/
public String getID() {
	return _ID;
}

/**
Returns the logical unit number for the MAP Area.
@return the logical unit number for the MAP Area.
*/
public int getLogicalUnitNum() {
	return _logicalUnitNum;
}

/**
Returns the MAP Area description.
Member method for MAP Area parameter variables
@return the MAP description.
*/
public String getDescription() {
	return _description;
}

/**
Gets the MAP Area Basin Boundaries identifiers.
Member method for MAP Area parameter variables
@return the MAP Area Basin Boundaries ID.
*/
public String getMAPBasinBoundaryID() {
	return _basinBoundaryID;
}

/**
Gets the MAP Area Centroid X coordinate.
Member method for MAP Area parameter variables
@return the MAP Area Centroid X value.
*/
public float getMAPCentroidX() {
	return _centroidX;
}

/**
Gets the MAP Area Centroid Y coordinate.
Member method for MAP Area parameter variables
@return the MAP Area Centroid Y value.
*/
public float getMAPCentroidY() {
	return _centroidY;
}

/**
Gets the MAP Area Data Time Interval.
Member method for MAP Area parameter variables
@return the MAP Area Data Time Interval.
*/
public int getMAPDataTimeInt() {
	return _dataTimeInt;
}

/**
Gets the MAP Area Exponent in 1/D**POWER.
Member method for MAP Area parameter variables
@return the MAP Area Exponent.
*/
public float getMAPExponent() {
	return _exponent;
}

/**
Gets the MAP Area FMAP Area ID used by this MAP object.
Member method for MAP Area parameter variables
@return the MAP Area FMAP Area ID.
*/
public String getMAPFMAPID() {
	return _fmapAreaID;
}

/**
Gets the MAP Area Number of Stations used for Time Distributions (NSTWT).
Member method for MAP Area parameter variables
@return the MAP Area Number of stations for time distribution.
*/
public int getMAPNSTWT() {
	return _NSTWT;
}

/**
Gets the MAP Area type of Station Weights.
Member method for MAP Area parameter variables
@return the MAP Area type of station weights.
*/
public int getMAPTypeStationWeights() {
	return _typeStationWeights;
}

/**
Gets the MAP Area type of Time Weights.
Member method for MAP Area parameter variables
@return the MAP Area type of time weights.
*/
public int getMAPTypeTimeWeights() {
	return _typeTimeWeights;
}

/**
Returns the record number for the MAP Area.
@return the record number for the MAP Area.
*/
public int getRecordNum() {
	return _recordNum;
}

/**
Initialize data members.
*/
private void initialize() {
	_ID     = null;
	_logicalUnitNum     = -1;
	_description        = null;
	_basinBoundaryID    = null;
	_centroidX          = 0;
	_centroidY          = 0;
	_dataTimeInt        = 0;
	_exponent           = 0;
	_fmapAreaID         = null;
	_NSTWT              = 0;
	_recordNum          = 0;
	_typeStationWeights = 0;
	_typeTimeWeights    = 0;
}

// SET Member methods for general MAP Area variables
/**
Sets the station's identifier.
Member method for general station variables
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
Sets the MAP Area description.
Member method for MAP Area parameter variables
@param description is the MAP Area description.
*/
public void setDescription(String description) {
	_description = description;
}

/**
Sets the MAP Area Basin Boundaries identifiers.
Member method for MAP Area parameter variables
@param basinBoundaryID is the MAP Area Basin Boundaries ID.
*/
public void setMAPBasinBoundaryID(String basinBoundaryID) {
	_basinBoundaryID = basinBoundaryID;
}

/**
Sets the MAP Area Centroid X coordinate.
Member method for MAP Area parameter variables
@param centroidX the MAP Area Centroid X value.
*/
public void setMAPCentroidX(float centroidX) {
	_centroidX = centroidX;
}

/**
Sets the MAP Area Centroid Y coordinate.
Member method for MAP Area parameter variables
@param centroidY the MAP Area Centroid Y value.
*/
public void setMAPCentroidY(float centroidY) {
	_centroidY = centroidY;
}

/**
Sets the MAP Area Data Time Interval.
Member method for MAP Area parameter variables
@param dataTimeInt is the MAP Area Data Time Interval.
*/
public void setMAPDataTimeInt(int dataTimeInt) {
	_dataTimeInt = dataTimeInt;
}

/**
Sets the MAP Area Exponent in 1/D**POWER.
Member method for MAP Area parameter variables
@param exponent is the MAP Area Exponent.
*/
public void setMAPExponent(float exponent) {
	_exponent = exponent;
}

/**
Sets the MAP Area FMAP Area ID used by this MAP object.
Member method for MAP Area parameter variables
@param fmapAreaID is the MAP Area FMAP Area ID.
*/
public void setMAPFMAPID(String fmapAreaID) {
	_fmapAreaID = fmapAreaID;
}

/**
Sets the MAP Area Number of Stations used for Time Distributions (NSTWT).
Member method for MAP Area parameter variables
@param NSTWT is the MAP Area Number of stations for time distribution.
*/
public void setMAPNSTWT(int NSTWT) {
	_NSTWT = NSTWT;
}

/**
Sets the MAP Area type of Station Weights.
Member method for MAP Area parameter variables
@param typeStationWeights is the MAP Area type of station weights.
*/
public void setMAPTypeStationWeights(int typeStationWeights) {
	_typeStationWeights = typeStationWeights;
}

/**
Sets the MAP Area type of Time Weights.
Member method for MAP Area parameter variables
@param typeTimeWeights is the MAP Area type of time weights.
*/
public void setMAPTypeTimeWeights(int typeTimeWeights) {
	_typeTimeWeights = typeTimeWeights;
}

/**
Sets a record number to the _recordNum int. This value will be
the record in the file PPPPARM<i>n</i> file to find data for the given MAP Ares 
ID.
@param recordNum is the actual record number.
*/
public void setRecordNum(int recordNum) {
	_recordNum = recordNum;
}

/**
toString method prints name of ID.
@return a String value with _ID value or "MAP:"_ID value
*/
public String toString() {
	return _ID;
}

}
