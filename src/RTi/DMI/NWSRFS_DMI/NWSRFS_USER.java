//-----------------------------------------------------------------------------
// NWSRFS_USER - class to store the organizational information about an 
//               NWSRFS USER Parameters.
//-----------------------------------------------------------------------------
// History:
//
// 
// 2004-09-28	Scott Townsend, RTi	Initial version.
//-----------------------------------------------------------------------------
// Endheader


package RTi.DMI.NWSRFS_DMI;

/**
The NWSRFS_USER class stores the organizational information about an
NWSRFS USER general parameters. This class holds the information stored in the 
preprocesseor parameteric database for areal information. It holds the parameters 
from the USER parameteric datatype which is an areal parameter
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
IX.4.3C-USER  PREPROCESSOR PARAMETRIC DATA BASE PARAMETER ARRAY USER:
                  USER GENERAL PARAMETERS

Purpose
Parameter array USER contains general user parameters.

Array Contents
  Starting                          Input/
  Position    Dimension    Type    Generated    Description


      1             1      I*4         G        Parameter array version number


      2             1       A8         I        User name


      4             2      I*4         I        Beginning month of summer and
                                                winter seasons for program FCST
                                                Function MAP


                                                Subset of MDR grid being used:
      6             1      I*4         I          o western most column
      7             1      I*4         I          o number of columns
      8             1      I*4         I          o southern most row
      9             1      I*4         I          o number of rows


                                                Latitude and longitude limits;
                                                units of decimal degrees:
     10             1      R*4         I          o northern latitude
     11             1      R*4         I          o southern latitude
     12             1      R*4         I          o eastern longitude
     13             1      R*4         I          o western longitude


                                                Elevation limits; units of M:
     14             1      R*4         I          o maximum elevation
     15             1      R*4         I          o minimum elevation


     16             1      I*4         I        Blend period for program FCST
                                                Function MAT computations;
                                                units of DAY


     17             1      I*4         I        Blend period for program FCST
                                                Function MAPE computations;
                                                units of DAY


     18             1      I*4         G        Program FCST Function MAP
                                                status indicator:
                                                  0 = complete and program
                                                      PPINIT command ORDER run
                                                  1 = MAP areas defined but
                                                      program PPINIT command
                                                      ORDER not run
                                                  2 = incomplete

     19          1         I*4         G        Program FCST Function MAT
                                                status indicator:
                                                  0 = complete
                                                  1 = incomplete


                                                Default values of exponent in
                                                l/D**POWER for area
                                                definitions:
     20          1         R*4         I          o MAP
     21          1         R*4         I          o MAT
     22          1         R*4         I          o MAPE


     23          1         R*4         I        Minimum daily precipitation at
                                                less than 24-hour stations for
                                                estimating time distribution;
                                                units of IN


     24          1         R*4         I        Minimum weight of stations to
                                                be kept when doing station
                                                weighting


     25          1         A4          I        Indicator how station
                                                information is to be sorted:
                                                  'ID'       = by identifier
                                                  'DESC'     = by description


     26          1         I*4         G        Number of user run defaults set
                                                with SAVEDFLT option of program
                                                PPINIT command SETOPT; maximum
                                                is 6


     27          1         I*4         I        Maximum lines per page; set
                                                with program PPINIT command
                                                SETOPT option PAGESIZE


     28          1         I*4         I        Option to begin commands on a
                                                new page; set with program
                                                PPINIT command SETOPT option
                                                NEWPAGE:
                                                  0 = no
                                                  1 = yes


     29          1         I*4         I        Option to overprint error and
                                                warning messages; set with
                                                program PPINIT command SETOPT
                                                option OVERPRNT:
                                                  0 = no
                                                  1 = yes


     30          1         I*4         I        Option to print log of commands
                                                executed; set with program
                                                PPINIT command SETOPT option

                                                CMDLOG:
                                                  0 = no
                                                  1 = yes


     31          1         I*4         I        Unused; reserved for use by
                                                SAVEDFLT option of program
                                                PPINIT command SETOPT


  The following is only in parameter arrays with a version number
  greater than 1:


                                                Subset of HRAP grid being used
                                                for processing NEXRAD
                                                precipitation data:
     32          1         I*4         I          o western most column
     33          1         I*4         I          o number of columns
     34          1         I*4         I          o southern most row
     35          1         I*4         I          o number of rows





07/22/2004                         IX.4.3C-USER-3
</pre>
*/
public class NWSRFS_USER {

// General parameters set for all USER parameters.
// REVISIT SAT 09/28/2004 Need to fill out the Class more. Many parameters are
// not included since there is no budget to fill it out.
/**
Identifier for the USER parameters.
*/
protected String _ID;

/**
This logical unit number corresponds to the <i>n</i> in the file
PPPPARM<i>n</i> and is used to pull all of the USER information.
*/
protected int _logicalUnitNum;

/**
The record number in the PPPINDEX file for retrieving data from the 
PPPPARM<i>n</i> file.
*/
protected int _recordNum;

/**
USER Parameter beggining Summer Month.
*/
protected int _begSummerMon;

/**
USER Parameter beggining Winter Month.
*/
protected int _begWinterMon;

/**
USER Parameter Elevation Maximum.
*/
protected float _elevationMax;

/**
USER Parameter Elevation Minimum.
*/
protected float _elevationMin;

/**
USER Parameter Northern Latitude Limit
*/
protected float _latitudeNorthLimit;

/**
USER Parameter Southern Latitude Limit
*/
protected float _latitudeSouthLimit;

/**
USER Parameter Eastern Longitude Limit
*/
protected float _longitudeEastLimit;

/**
USER Parameter Western Longitude Limit
*/
protected float _longitudeWestLimit;

/**
USER Parameter MDR Grid Western most column.
*/
protected int _mdrWestColumn;

/**
USER Parameter MDR Grid number of columns.
*/
protected int _mdrNumColumns;

/**
USER Parameter MDR Grid Eastern most row.
*/
protected int _mdrEastRow;

/**
USER Parameter MDR Grid number of rows.
*/
protected int _mdrNumRows;

/**
USER Parameter user name.
*/
protected String _userName;

/**
Constructor.
@param id USER Parameter ID.
*/
public NWSRFS_USER(String id) {
	initialize();

	if (id != null) {
		_ID = id;
	}
}

/**
Constructor.
*/
public NWSRFS_USER() {
	initialize();
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize() 
throws Throwable {
	_ID     = null;
	_begSummerMon       = 0;
	_begWinterMon       = 0;
	_elevationMax       = 0;
	_elevationMin       = 0;
	_latitudeNorthLimit = -999;
	_latitudeSouthLimit = -999;
	_longitudeEastLimit = -999;
	_longitudeWestLimit = -999;
	_mdrWestColumn      = 0;
	_mdrNumColumns      = 0;
	_mdrEastRow         = 0;
	_mdrNumRows         = 0;
	_userName           = null;
	_logicalUnitNum     = -1;
	_recordNum          = 0;
}

// GET Member methods for general USER Parameter variables
/**
Returns the USER parameter's identifier.
Member method for general USER variables
@return the USER parameter's identifier.
*/
public String getID() {
	return _ID;
}

/**
Returns the logical unit number for the USER parameter.
@return the logical unit number for the USER.
*/
public int getLogicalUnitNum() {
	return _logicalUnitNum;
}

/**
Returns the record number for the MAP Area.
@return the record number for the MAP Area.
*/
public int getRecordNum() {
	return _recordNum;
}

/**
Returns the USER Name.
Member method for USER parameter variables
@return the USER Name.
*/
public String getUSERName() {
	return _userName;
}

/**
Gets the month which is the begining of Summer.
Member method for USER parameter variables
@return the begining of Summer Month.
*/
public int getUSERBegSummerMon() {
	return _begSummerMon;
}

/**
Gets the month which is the begining of Winter.
Member method for USER parameter variables
@return the begining of Winter Month.
*/
public int getUSERBegWinterMon() {
	return _begWinterMon;
}

/**
Gets the Maximum elevation.
Member method for USER parameter variables
@return the Maximum elevation.
*/
public float getUSERElevationMax() {
	return _elevationMax;
}

/**
Gets the Minimum elevation.
Member method for USER parameter variables
@return the Minimum elevation.
*/
public float getUSERElevationMin() {
	return _elevationMin;
}

/**
Gets the Northern latitude limit.
Member method for USER parameter variables
@return the Northern latitude limit.
*/
public float getUSERLatitudeNorthLimit() {
	return _latitudeNorthLimit;
}

/**
Gets the Southern latitude limit.
Member method for USER parameter variables
@return the Southern latitude limit.
*/
public float getUSERLatitudeSouthLimit() {
	return _latitudeSouthLimit;
}

/**
Gets the Eastern longitude limit.
Member method for USER parameter variables
@return the Eastern longitude limit.
*/
public float getUSERLongitudeEastLimit() {
	return _longitudeEastLimit;
}

/**
Gets the Western longitude limit.
Member method for USER parameter variables
@return the Western longitude limit.
*/
public float getUSERLongitudeWestLimit() {
	return _longitudeWestLimit;
}

/**
Gets the MDR Grid for the Western most column.
Member method for USER parameter variables
@return the MDR grid value for the western most column.
*/
public int getUSERMDRWestColumn() {
	return _mdrWestColumn;
}

/**
Gets the MDR Grid for the number of columns.
Member method for USER parameter variables
@return the MDR grid value for the number of columns.
*/
public int getUSERMDRNumColumns() {
	return _mdrNumColumns;
}

/**
Gets the MDR Grid for the Southern most row.
Member method for USER parameter variables
@return the MDR grid value for the Southern most row.
*/
public int getUSERMDRSouthRow() {
	return _mdrEastRow;
}

/**
Gets the MDR Grid for the number of rows.
Member method for USER parameter variables
@return the MDR grid value for the number of rows.
*/
public int getUSERMDRNumRows() {
	return _mdrNumRows;
}

/**
Initialize data members.
*/
private void initialize() {
	_ID     = null;
	_begSummerMon       = 0;
	_begWinterMon       = 0;
	_elevationMax       = 0;
	_elevationMin       = 0;
	_latitudeNorthLimit = -999;
	_latitudeSouthLimit = -999;
	_longitudeEastLimit = -999;
	_longitudeWestLimit = -999;
	_mdrWestColumn      = 0;
	_mdrNumColumns      = 0;
	_mdrEastRow         = 0;
	_mdrNumRows         = 0;
	_userName           = null;
	_logicalUnitNum     = -1;
	_recordNum          = 0;
}

// SET Member methods for general USER parameter variables
/**
Sets the USER parameter's identifier.
Member method for general USER variables
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
Sets the record number for the MAP Area.
@param recordNum the record number for the MAP Area.
*/
public void setRecordNum(int recordNum) {
	_recordNum = recordNum;
}

/**
Sets the USER Name.
Member method for USER parameter variables
@param userName the USER Name.
*/
public void setUSERName(String userName) {
	_userName = userName;
}

/**
Sets the month which is the begining of Summer.
Member method for USER parameter variables
@param begSummerMon the begining of Summer Month.
*/
public void setUSERBegSummerMon(int begSummerMon) {
	_begSummerMon = begSummerMon;
}

/**
Sets the month which is the begining of Winter.
Member method for USER parameter variables
@param begWinterMon the begining of Winter Month.
*/
public void setUSERBegWinterMon(int begWinterMon) {
	_begWinterMon = begWinterMon;
}

/**
Sets the Maximum elevation.
Member method for USER parameter variables
@param elevationMax the Maximum elevation.
*/
public void setUSERElevationMax(float elevationMax) {
	_elevationMax = elevationMax;
}

/**
Sets the Minimum elevation.
Member method for USER parameter variables
@param elevationMin the Minimum elevation.
*/
public void setUSERElevationMin(float elevationMin) {
	_elevationMin = elevationMin;
}

/**
Sets the Northern latitude limit.
Member method for USER parameter variables
@param latitudeNorthLimit the Northern latitude limit.
*/
public void setUSERLatitudeNorthLimit(float latitudeNorthLimit) {
	_latitudeNorthLimit = latitudeNorthLimit;
}

/**
Sets the Southern latitude limit.
Member method for USER parameter variables
@param latitudeSouthLimit the Southern latitude limit.
*/
public void setUSERLatitudeSouthLimit(float latitudeSouthLimit) {
	_latitudeSouthLimit = latitudeSouthLimit;
}

/**
Sets the Eastern longitude limit.
Member method for USER parameter variables
@param longitudeEastLimit the Eastern longitude limit.
*/
public void setUSERLongitudeEastLimit(float longitudeEastLimit) {
	_longitudeEastLimit = longitudeEastLimit;
}

/**
Sets the Western longitude limit.
Member method for USER parameter variables
@param longitudeWestLimit the Western longitude limit.
*/
public void setUSERLongitudeWestLimit(float longitudeWestLimit) {
	_longitudeWestLimit = longitudeWestLimit;
}

/**
Sets the MDR Grid for the Western most column.
Member method for USER parameter variables
@param mdrWestColumn the MDR grid value for the western most column.
*/
public void setUSERMDRWestColumn(int mdrWestColumn) {
	_mdrWestColumn = mdrWestColumn;
}

/**
Sets the MDR Grid for the number of columns.
Member method for USER parameter variables
@param mdrNumColumns the MDR grid value for the number of columns.
*/
public void setUSERMDRNumColumns(int mdrNumColumns) {
	_mdrNumColumns = mdrNumColumns;
}

/**
Sets the MDR Grid for the Southern most row.
Member method for USER parameter variables
@param mdrEastRow the MDR grid value for the Southern most row.
*/
public void setUSERMDRSouthRow(int mdrEastRow) {
	_mdrEastRow = mdrEastRow;
}

/**
Sets the MDR Grid for the number of rows.
Member method for USER parameter variables
@param mdrNumRows the MDR grid value for the number of rows.
*/
public void setUSERMDRNumRows(int mdrNumRows) {
	_mdrNumRows = mdrNumRows;
}

/**
toString method prints name of ID.
@return a String value with _ID value or "USER:"_ID value
*/
public String toString() {
	return _ID;
}

}
