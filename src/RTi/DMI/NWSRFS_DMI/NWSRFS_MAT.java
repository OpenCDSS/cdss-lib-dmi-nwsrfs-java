//-----------------------------------------------------------------------------
// NWSRFS_MAT - class to store the organizational information about an 
//              NWSRFS MAT Area.
//-----------------------------------------------------------------------------
// History:
//
// 
// 2004-09-28	Scott Townsend, RTi	Initial version.
//-----------------------------------------------------------------------------
// Endheader


package RTi.DMI.NWSRFS_DMI;

/**
The NWSRFS_MAT class stores the organizational information about an
NWSRFS MAT Areal parameters. This class holds the information stored in the 
preprocesseor parameteric database for areal information. It holds the parameters 
from the MAT parameteric datatype which is an areal parameter
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
IX.4.3C-MAT        PREPROCESSOR PARAMETRIC DATA BASE PARAMETER ARRAY MAT:
                   MEAN AREAL PRECIPITATION (MAT) AREA PARAMETERS

Purpose
Parameter array MAT contains parameters used to compute Mean Areal
Temperature (MAT) for an MAT area.

Array Contents
    Starting                          Input/
    Position    Dimension    Type    Generated    Description
        1           1        I*4         G        Parameter array version number


        2           1         A8         I        MAT area identifier


        4           1        A20         I        Description


        9           2        R*4      I or G      Centroid of area; latitude and
                                                  longitude; units of decimal
                                                  degrees


       11          1          A8        I         Identifier of basin boundary
                                                  used by this area 1/


       13          1         I*4         I        Type of station weights:
                                                      1 = predetermined
                                                      2 = grid point
                                                      3 = 1/D**POWER


       14          1         R*4         I        Exponent in 1/D**POWER 2/


       15          2         R*4         G        Unused


       17          1         I*4         G        Number of TEMP stations used to
                                                  compute MAT (NTEMP) 3/


       18        NTEMP        A8      I or G      TEMP station identifiers


18+2*NTEMP       NTEMP       I*4         G        Array locations for
                                                  maximum/minimum TEMP data for
                                                  station 4/


18+3*NTEMP       NTEMP       R*4      I or G      Station weights

Notes:
1/    Defined only if grid point weights are being used.  If not, the
      identifier is blank.

2/    Defined only if 1/D**POWER weights used.

3/    No maximum value.

4/    Array location is the location of the pointers in the pointer array
      returned from the Preprocessor Data Base routine RPDDLY for data
      type TM24.

07/22/2004                     IX.4.3C-MAT-2
</pre>
*/
public class NWSRFS_MAT {

// General parameters set for all MAT Area parameters.
// REVISIT SAT 09/28/2004 Need to fill out the Class more. Many parameters are
// not included since there is no budget to fill it out.
/**
Identifier for the MAT Area.
*/
protected String _ID;

/**
This logical unit number corresponds to the <i>n</i> in the file
PPPPARM<i>n</i> and is used to pull all of the MAT information.
*/
protected int _logicalUnitNum;

/**
The record number in the PPPINDEX file for retrieving data from the 
PPPPARM<i>n</i> file.
*/
protected int _recordNum;

/**
MAT Area Basin Boundary Identifier.
*/
protected String _basinBoundaryID;

/**
MAT Area Centroid X coordinate
*/
protected float _centroidX;

/**
MAT Area Centroid Y coordinate
*/
protected float _centroidY;

/**
MAT Area description.
*/
protected String _description;

/**
MAT Area Exponent in 1/D**POWER.
*/
protected float _exponent;

/**
MAT Area Number of stations used to compute MAT (NTEMP)..
*/
protected int _NTEMP;

/**
MAT Area Type of Station Weights.
*/
protected int _typeStationWeights;

/**
Constructor.
@param id MAT Area ID.  Can not be null
*/
public NWSRFS_MAT(String id) {
	initialize();

	if (id != null) {
		_ID = id;
	}
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize() 
throws Throwable {
	_ID     = null;
	_logicalUnitNum     = -1;
	_description        = null;
	_basinBoundaryID    = null;
	_centroidX          = 0;
	_centroidY          = 0;
	_exponent           = 0;
	_NTEMP              = 0;
	_recordNum          = 0;
	_typeStationWeights = 0;
}

// GET Member methods for general MAP Area variables
/**
Returns the MAT Area's identifier.
Member method for general MAT Area variables
@return the MAT Area's identifier.
*/
public String getID() {
	return _ID;
}

/**
Returns the logical unit number for the MAT Area.
@return the logical unit number for the MAT Area.
*/
public int getLogicalUnitNum() {
	return _logicalUnitNum;
}

/**
Returns the MAT Area description.
Member method for MAT Area parameter variables
@return the MAT description.
*/
public String getDescription() {
	return _description;
}

/**
Gets the MAT Area Basin Boundaries identifiers.
Member method for MAT Area parameter variables
@return the MAT Area Basin Boundaries ID.
*/
public String getMATBasinBoundaryID() {
	return _basinBoundaryID;
}

/**
Gets the MAT Area Centroid X coordinate.
Member method for MAT Area parameter variables
@return the MAT Area Centroid X value.
*/
public float getMATCentroidX() {
	return _centroidX;
}

/**
Gets the MAT Area Centroid Y coordinate.
Member method for MAT Area parameter variables
@return the MAT Area Centroid Y value.
*/
public float getMATCentroidY() {
	return _centroidY;
}

/**
Gets the MAT Area Exponent in 1/D**POWER.
Member method for MAT Area parameter variables
@return the MAT Area Exponent.
*/
public float getMATExponent() {
	return _exponent;
}

/**
Gets the MAT Area Number of Stations used to compute MAT (NTEMP).
Member method for MAT Area parameter variables
@return the MAT Area Number of stations to compute MAT.
*/
public int getMATNTEMP() {
	return _NTEMP;
}

/**
Gets the MAT Area type of Station Weights.
Member method for MAT Area parameter variables
@return the MAT Area type of station weights.
*/
public int getMATTypeStationWeights() {
	return _typeStationWeights;
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
	_exponent           = 0;
	_NTEMP              = 0;
	_recordNum          = 0;
	_typeStationWeights = 0;
}

// SET Member methods for general MAP Area variables
/**
Sets the station's identifier.
Member method for general MAT variables
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
Sets the MAT Area description.
Member method for MAT Area parameter variables
@param description is the MAT Area description.
*/
public void setDescription(String description) {
	_description = description;
}

/**
Sets the MAT Area Basin Boundaries identifiers.
Member method for MAT Area parameter variables
@param basinBoundaryID is the MAT Area Basin Boundaries ID.
*/
public void setMATBasinBoundaryID(String basinBoundaryID) {
	_basinBoundaryID = basinBoundaryID;
}

/**
Sets the MAT Area Centroid X coordinate.
Member method for MAT Area parameter variables
@param centroidX the MAT Area Centroid X value.
*/
public void setMATCentroidX(float centroidX) {
	_centroidX = centroidX;
}

/**
Sets the MAT Area Centroid Y coordinate.
Member method for MAT Area parameter variables
@param centroidY the MAT Area Centroid Y value.
*/
public void setMATCentroidY(float centroidY) {
	_centroidY = centroidY;
}

/**
Sets the MAT Area Exponent in 1/D**POWER.
Member method for MAT Area parameter variables
@param exponent is the MAT Area Exponent.
*/
public void setMATExponent(float exponent) {
	_exponent = exponent;
}

/**
Sets the MAT Area Number of Stations used to compute MAP (NTEMP).
Member method for MAT Area parameter variables
@param NTEMP is the MAT Area Number of stations to compute MAT.
*/
public void setMATNTEMP(int NTEMP) {
	_NTEMP = NTEMP;
}

/**
Sets the MAT Area type of Station Weights.
Member method for MAT Area parameter variables
@param typeStationWeights is the MAT Area type of station weights.
*/
public void setMATTypeStationWeights(int typeStationWeights) {
	_typeStationWeights = typeStationWeights;
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
@return a String value with _ID value or "MAT:"_ID value
*/
public String toString() {
	return _ID;
}

}
