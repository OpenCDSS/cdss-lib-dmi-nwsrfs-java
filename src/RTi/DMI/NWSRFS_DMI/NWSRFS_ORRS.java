//-----------------------------------------------------------------------------
// NWSRFS_ORRS - class to store the organizational information about an 
//               NWSRFS alphabetical order RRS station parameters.
//-----------------------------------------------------------------------------
// History:
//
// 
// 2004-10-12	Scott Townsend, RTi	Initial version.
//-----------------------------------------------------------------------------
// Endheader


package RTi.DMI.NWSRFS_DMI;

import java.util.List;
import java.util.Vector;

/**
The NWSRFS_ORRS class stores the organizational information about an
NWSRFS alphabetical order RRS station parameters. This class holds the 
information stored in the preprocesseor parameteric database for RRS 
information. It holds the parameters from the ORRS parameteric datatype which 
is a RRS parameter type. All of the parameters are read from the FS5Files 
binary DB file: PPPPARM<i>n</i> where <i>n</i> is determined from the 
preproccesed database index file PPPINDEX. All PPDB (Preprocessed Parameteric 
DataBase) parameters actually reside in an array on the DB. This array must be 
parsed according to the type of parameter requested. For more information 
see below:
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
IX.4.3C-ORRS     PREPROCESSOR PARAMETRIC DATA BASE PARAMETER ARRAY ORRS:
                 RIVER, RESERVOIR AND SNOW (RRS) STATION ALPHABETICAL
                 ORDER PARAMETERS

Purpose
Parameter array ORRS contains alphabetical order parameters for all
stations that have River, Reservoir and Snow (RRS) data.

Array Contents

  Starting                         Input/
  Position     Dimension   Type   Generated Description

      1            1       I*4        G      Parameter array version number

      2            1       I*4        G      Indicator how list was ordered:
                                                1 = by station identifier
                                                2 = by station description

      3            2       R*4        G      Unused

      5            1       I*4        G      Number of stations in list
                                             (NSTA)

      6          NSTA      I*2        G      Record number of RRS parameters
                                             in the PPPDB


                      IX.4.3C-ORRS-1
</pre>
*/
public class NWSRFS_ORRS {

// General parameters set for all ORRS parameters.
/**
Identifier for the ORRS.
*/
protected String _ID;

/**
This logical unit number corresponds to the <i>n</i> in the file
PPPPARM<i>n</i> and is used to pull all of the ORRS information.
*/
protected int _logicalUnitNum;

/**
The record number in the PPPINDEX file for retrieving data from the 
PPPPARM<i>n</i> file.
*/
protected int _recordNum;

/**
ORRS indicator on how list is ordered.
*/
protected int _orrsListInd;

/**
ORRS Number of stations in list (NSTA)
*/
protected int _orrsNSTA;

/**
ORRS record numbers of RRS parameters in the PPDB.
*/
protected List<Integer> _orrsIREC;

/**
Constructor.
@param id ORRS ID.  Can not be null
*/
public NWSRFS_ORRS(String id) {
	initialize();

	if (id != null) {
		_ID = id;
	}
}

/**
Adds values to the ORRS record number Vector. Each element is a record number
for RRS parameters in PPDB.
@param orrsIRECNum ORRS recoird number.
*/
public void addORRSIREC(int orrsIRECNum) {
	_orrsIREC.add(new Integer(orrsIRECNum));
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize() 
throws Throwable {
	_ID                 = null;
	_logicalUnitNum     = -1;
	_recordNum          = -1;
	_orrsListInd        = -1;
	_orrsNSTA           = -1;
	_orrsIREC           = null;
}

// GET Member methods for general ORRS variables
/**
Returns the ORRS identifier.
Member method for general ORRS variables
@return the ORRS identifier.
*/
public String getID() {
	return _ID;
}

/**
Returns the logical unit number for the ORRS.
@return the logical unit number for the ORRS.
*/
public int getLogicalUnitNum() {
	return _logicalUnitNum;
}

/**
Returns the ORRS list indicator for how it was ordered.
@return the ORRS list indicator.
*/
public int getORRSListInd() {
	return _orrsListInd;
}

/**
Returns the ORRS number of stations (NSTA).
@return the ORRS number of stations.
*/
public int getORRSNSTA() {
	return _orrsNSTA;
}

/**
Returns the Vector of the ORRS record numbers for RRS parameters in PPDB.
@return the Vector of the ORRS record numbers.
*/
public List<Integer> getORRSIREC() {
	return _orrsIREC;
}

/**
Returns the Value of the ORRS record numbers Vector for RRS parameters in PPDB
at an index.
@param orrsIRECIndex index to get the specific element in the ORRS IREC Vector
@return the Vector of the ORRS record numbers at an index.
*/
public int getORRSIREC(int orrsIRECIndex) {
	return (int)((Integer)_orrsIREC.get(orrsIRECIndex)).intValue();
}

/**
Returns a record number to the _recordNum int. This value will be
the record in the file PPPPARM<i>n</i> file to find data for the given ORRS 
Boundary ID.
@returns the actual record number.
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
	_recordNum          = -1;
	_orrsListInd        = -1;
	_orrsNSTA           = -1;
	_orrsIREC           = new Vector<Integer>();
}

// SET Member methods for general ORRS variables
/**
Sets the ORRS identifier.
Member method for general ORRS variables
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
Sets the ORRS list indicator for how it was ordered.
@param orrsListInd the ORRS list indicator.
*/
public void setORRSListInd(int orrsListInd) {
	_orrsListInd = orrsListInd;
}

/**
Sets the ORRS number of stations (NSTA).
@param orrsNSTA the ORRS number of stations.
*/
public void setORRSNSTA(int orrsNSTA) {
	_orrsNSTA = orrsNSTA;
}

/**
Sets a record number to the _recordNum int. This value will be
the record in the file PPPPARM<i>n</i> file to find data for the given ORRS 
Boundary ID.
@param recordNum is the actual record number.
*/
public void setRecordNum(int recordNum) {
	_recordNum = recordNum;
}

/**
toString method prints name of ID.
@return a String value with _ID value or "ORRS:"_ID value
*/
public String toString() {
	return _ID;
}

}
