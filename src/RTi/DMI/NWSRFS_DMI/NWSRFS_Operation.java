//-----------------------------------------------------------------------------
// NWSRFS_Operation class to store the organizational information about an 
//                  NWSRFS operations.
//-----------------------------------------------------------------------------
// History:
//
// 
// 2004-4-22	Shawn chen		Initial version  
// 2004-4-22	Scott Townsend, RTi	Update due to change in design
// 2004-07-12   Anne Morgan Love, RTi   Removed setVerbose(), which
//                                      added additional identifier strings in
//                                      front of the String returned from
//                                      .toString() if was true (for example:
//                                      "OP: " for operation).
// 2004-08-19	J. Thomas Sapienza, RTi	* Revised for RTi standards.
//					* Added get*() and set*() methods.
// 2004-08-23	JTS, RTi		Added finalize().
//-----------------------------------------------------------------------------
// Endheader


package RTi.DMI.NWSRFS_DMI;

import java.util.Vector;

import RTi.DMI.NWSRFS_DMI.NWSRFS_RatingCurve;
import RTi.DMI.NWSRFS_DMI.NWSRFS_Segment;

/**
The NWSRFS_Operation class stores the organizational information about an
NWSRFS Operations which are defined by parameters, carryover states, and time 
series. This class reads and stores data from the FCSEGSTS and 
FCPARAM processed database files. The FCPARAM has the following definition:
<pre>

FILE NAME:  FCPARAM

Purpose

File FCPARAM holds the parameter values needed by the Forecast
Component.  This includes the P, T, and TS array contents for each
Segment.


Description

ATTRIBUTES: fixed length 400 byte binary records

NUMBER OF RECORDS: defined by MAXRP in file FCSEGPTR

RECORD STRUCTURE:

    The first record for a Segment is defined by IPREC in the entry in
    file FCSEGSTS for that Segment.

    The number of words (NWORD) used by a Segment can be computed as
    follows:

        NWORD=2+NP+NT+NTS

        where NP, NT and NTS are also found in file FCSEGSTS

    The number of records (NREC) used by a Segment can be computed as
    follows:

        NREC=(NWORD+LRECL-1)/LRECL

        where LRECL is the record length in words

    These parameter values for a Segment always begin in a new record.

    The 'conceptual' record for each Segment is as follows:

    Variable    Type   Dimension   Word Pos.   Description

    IDSEG        A8       1           1        Segment name

    P            R*4      NP          3        P array

    T            I*4      NT         3+NP      T array

    TS           R*4      NTS        3+NP+NT   TS array

                 IX.4.5B-FCPARAM
</pre>

The P, T, and TS arrays have the following definitions respectively:
<pre>

VIII.2-ARRAY_P  FORECAST COMPONENT INTERNAL ARRAY P 
 
Function 
  The P array contains the parameters for Operations used in a 
  ForecastComponent Segment. 
 
 
Listing 
  DIMENSION P(MP) 
  where MP is the maximum size of array P 
 
 
Contents 
  The P array contains the following information for each Operation: 
 
         Position  Contents 
 
         1         Operation number: 
 
                        -1 =last entry in the P array (STOP Operation) 
 
         2         Pointer indicating where in the P array the information
                   for the next Operation begins 
 
         3-4       8-character user specified name of the Operation 
 
         5-6       8-character name used during Segment redefinition to
                   indicate how carryover values are determined 
 
         7         Pointer indicating where the carryover values for this
                   Operation are located in the C array (the starting
                   location of the second part of the portion of the C
                   array assigned to the Operation) - if the Operation has
                   no carryover the pointer is zero 
 
         8-?       Parameters for the Operation - these values are entered
                   by the input (PIN) routine for the Operation length is
                   variable (this is sometimes referred to as the second
                   part of the portion of the P array assigned to the
                   Operation - it is referred to as the PO array in
                   Chapter VIII.4)

              VIII.2-ARRAY_P 
</pre>

<pre>

VIII.2-ARRAY_T  FORECAST COMPONENT INTERNAL ARRAY T 
  
Function  
  The T array contains the Operations Table entries for each Operation
  used in a Forecast Component Segment. 
 
  The entries consist of pointers to the portions of the other arrays
  used in the execution of each Operation. 
 
 
Listing 
  INTEGER T 
  DIMENSION  T(MT) 
  where MT is the maximum length of array T 
 
 
Contents 
  The T array contains the following information for each Operation. 
  All T array entries for each Operation are made in the Operations
  Table entry (TAB) subroutine. 
 
         Position  Contents 
 
         1         Operation number: 
 
                        -1 =last entry in the T array (STOP Operation) 
 
         2         Pointer indicating where in the T array the entry for
                   the next Operation to be executed begins 
 
         3         For all Operations with information in the P array, a
                   pointer indicating the starting location of the second
                   part of the portion of the P array assigned to the
                   Operation 
 
         4         For Operations with carryover, a pointer indicating the
                   starting location of the second part of the portion of
                   the C array assigned to the Operation 
 
      3, 4 or 5-?  Remaining entries vary from one Operation to another,
                   but are generally pointers to the starting location of
                   time series data or working space used by the Operation
                   in the D array - for Operations using Rating Curves the
                   location of the Rating Curve identifier in the P array
                   is also stored

              VIII.2-ARRAY_T 
</pre>

<pre>

VIII.2-ARRAY_TS  FORECAST COMPONENT INTERNAL ARRAY TS  
 
Function 
  The TS array contains all of the information about each time series
  used in a Forecast Component Segment. 
 
 
Listing 
  DIMENSION TS(MTS) 
  where MTS is the maximum size of array TS 
 
 
Contents 
  The TS array contains the following information for each time series
  that is defined for the Segment: 
 
         Position  Contents 
 
         1         Indicator for the type of time series: 
 
                        0 = last entry in the TS array 
 
                        1 = Input (data values initially read from datafiles) 
 
                        2 = Update (data values initially read from datafiles 
                            and later written back to the files) 
 
                        3 = Output (data values written to data files) 
 
                        4 = Internal (used only to pass information from
                            one Operation to another - not read or written
                            to any data files) 
 
         2         Pointer indicating where in the TS array the
                   information for the next time series begins 
 
         3-4       8-character identifier for the time series 
 
         5         4-character data type code for the time series 
 
         6         Data time interval of the time series in hours 
 
         7         Number of values in the time series per time interval 
 
         8         Pointer indicating the starting location of the 
                   timeseries data in the D array 
 
         9         Indicator whether the time series contains data
                   values:
                        0 =no 
                        1 =yes 
 
                   This indicator is initially set to 1 for Input and
                   Update time series and zero for all others - the
                   indicator is checked in the Operations Table entry
                   subroutine for each Operation and set equal to 1 when
                   an Operation puts data values into the time series 
 
         10        4-character code indicating the type of data file
                   accessed for this time series - not included for
                   Internal time series 
 
         11        Indicator for Output and Update time series indicating
                   when the time series is written to the data files: 
                        0 = written after executing the Operations 
                        1 = written during execution of the OperationsTable 
                   Indicator is set to zero for Input time series  
                   Not included for Internal time series 
 
         12        Number of values (NV) of external location information
                   (information needed to read or write the time series
                   to the data files) 
                   Not included for Internal time series 
 
      13 through   External location information for the time series - 
      12+NV        contents depends on type of data file used 
 
      13+NV        Number of values of additional information (NADD)  
      (10 for      that are associated with the time series 
       Internal) 
 
   14+NV through   Additional information associated with the time 
   13+NV+NADD      series (information that is required when using  
   (11 through     the time series for some special application) 
    10+NADD for 
    Internal)

              VIII.2-ARRAY_TS
</pre>
*/
public class NWSRFS_Operation {

/**
Parent to this Operation.
*/
private NWSRFS_Segment __segment;

/**
NWSRFSRatingCurve IDs in the operation.
*/
private Vector  __rcIDs;

/**
NWSRFSRatingCurve objects in the operation.
*/
private Vector  __ratingCurves;

/**
System identifier for the operation.
Should be the same as opTypeName which is the name for the operation type.
*/
private String __sysID;

/**
Time series data types in the operation (list of data type strings).
*/
private Vector  __tsDTs;

/**
Time series identifiers in the operation (list of TSIdent strings).
*/
private Vector  __tsIDs;

/**
TimeSeries in the operation.
*/
private Vector	__timeseries;

/**
User identifier for the operation.
*/
private String __userID;

/**
The parameters to the Operation (often called the PO array).
*/
private float[] __opParameters;

/**
The array of Integers holding either the starting location of TS data or
working space used by the Operation in D array or location in P array for 
RatingCurve ID.
*/
private int [] __opParameterTable;

/**
Points to the location in the C array where Carryover for the 
particular Operation resides.
*/
private int __opCarryoverPointer;

/**
Points to the second part of the Carryover or C array resides for a 
given Operation.
*/
private int __opCarryoverPointerCO;

/**
The Operation number.
*/
private int __opNumber;

/**
Points to the location in the P array where the "PO" array or parameter 
data starts for a given Operation.
*/
private int __opParameterArrayPointer;	

/**
The Segment ID for this list of Operations.  Should be the same 
as __segment.IDSEG.
*/
private String __IDSEG;

/**
The Operation name that is associated with the Operation number.
*/
private String __opName;

/**
The name used during Segment redefinition.
*/
private String __opRedefName;

/**
The Operation type name that is associated with the Operation number. 
e.g. "Plot-Tul".
*/
private String __opTypeName;

/**
Constructor.  Initializes the system and user IDs to blank.
@param segment the segment parent.  Cannot be null.
@throws NullPointerException if segment is null.
*/
public NWSRFS_Operation(NWSRFS_Segment segment) 
throws Exception {
	// Set parent segment object
	if (segment != null) {
		__segment = segment;
	}
	else {
		throw new NullPointerException(
			"The parent Segment was null. Can not "
			+ "initialize the class.");
	}

	initialize();
}

/**
Constructor.  Initializes with no time series.
@param sysid the system ID.
@param userid the user ID.
@param segment the segment parent.  Cannot be null.
@throws NullPointerException if the segment is null.
*/
public NWSRFS_Operation (String sysid, String userid, NWSRFS_Segment segment) 
throws Exception {
	String routine = "NWSRFS_Operation";
	
	// Set parent segment object
	if ( segment != null) {
		__segment = segment;
	}
	else {
		throw new NullPointerException(
			"The parent Segment was null. Can not "
			+ "initialize the class.");
	}

	initialize();

	if (sysid != null) {
		__sysID = sysid;
	}

	if (userid != null) {
		__userID = userid;
	}
}

/**
Add a Rating Curve identifers to the NWSRFS_Operation.
@param rcid Rating Curve ID to add.
*/
public void addRCID(String rcid) {
	__rcIDs.addElement(rcid);
}

/**
Add a Rating Curve to the NWSRFS_Operation.
@param rc NWSRFS_RatingCurve object to add.
*/
public void addRatingCurve(NWSRFS_RatingCurve rc) {	
	__ratingCurves.addElement(rc);
}

/**
Add a TimeSeries DataType to this operation. Generally this will be a 
TimeSeries that is is defined on this Operation object.
@param tsDT A TimeSeries DataType to add to the operation.
*/
public void addTSDT(String tsDT) {
		__tsDTs.addElement((String)tsDT);
}

/**
Add a TimeSeries identifier to this operation. Generally this will be a 
TimeSeries that is is defined on this Operation object.
@param tsID A TimeSeries object identifier to add to the operation.
*/
public void addTSID(String tsID) {
		__tsIDs.addElement( (String)tsID );
}

/**
Add a TimeSeries to this operation. Generally this will be a TimeSeries that is
is defined on this Operation object.
@param ts A NWSRFS_TimeSeries object to add to the operation.
*/
public void addTimeSeries(NWSRFS_TimeSeries ts) {
		__timeseries.addElement(ts);
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize() 
throws Throwable {
	__segment = null;
	__rcIDs = null;
	__ratingCurves = null;
	__sysID = null;
	__tsDTs = null;
	__tsIDs = null;
	__timeseries = null;
	__userID = null;
	__opParameters = null;
	__opParameterTable = null;
	__IDSEG = null;
	__opName = null;
	__opRedefName = null;
	__opTypeName = null;
}

/**
Returns the segment ID for this operation.
@return the segment ID for this operation.
*/
public String getIDSEG() {
	return __IDSEG;
}

/**
Return the number of Rating Curve IDs for this Opertion object.
@return an int value for the number of Rating Curve IDs in the Operation object.
*/
public int getNumberOfRatingCurveIDs() {
	return __rcIDs.size();
}

/**
Return the number of Rating Curves for this Opertion object.
@return an int value for the number of Rating Curves in the Operation object.
*/
public int getNumberOfRatingCurves() {
	return __ratingCurves.size();
}

/**
Return the number of Timeseries DataTypes for this Operation object.
@return an int value for the number of Timeseries DataTypes define on this 
Operation object.
*/
public int getNumberOfTSDTs() {
	return __tsDTs.size();
}

/**
Return the number of Timeseries IDs for this Operation object.
@return an int value for the number of Timeseries IDs define on this 
Operation object.
*/
public int getNumberOfTSIDs() {
	return __tsIDs.size();
}

/**
Return the number of TimeSeries for this Operation object.
@return an int value for the number of TimeSeries define on this 
Operation object.
*/
public int getNumberOfTimeSeries() {
	return __timeseries.size();
}

/**
Returns the int that points to the location in the C array where carryover
for this operation can be found.
@return the int that points to the location in the C array where carryover
for this operation can be found.
*/
public int getOpCarryoverPointer() {
	return __opCarryoverPointer;
}

/**
Returns the int that points to the second part of the carryover or C
array resides for a given operation.
@return the int that points to the second part of the carryover or C
array resides for a given operation.
*/
public int getOpCarryoverPointerCO() {
	return __opCarryoverPointerCO;
}

/**
Returns the operation name.
@return the operation name.
*/
public String getOpName() {
	return __opName;
}

/**
Returns the operation number.
@return the operation number.
*/
public int getOpNumber() {
	return __opNumber;
}

/**
Returns the location in the P array where the PO array or parameter data starts
for a given operation.
@return the location in the P array where the PO array or parameter data starts
for a given operation.
*/
public int getOpParameterArrayPointer() {
	return __opParameterArrayPointer;
}

/**
Returns the PO array.
@return the PO array.
*/
public float[] getOpParameters() {
	return __opParameters;
}

/**
Returns the array of integers that specify the starting location of TS data
or working space used by the Operation in D array or the location in the P 
array for the rating curve ID.
@return the array of integers that specify the starting location of TS data
or working space used by the Operation in D array or the location in the P 
array for the rating curve ID.
*/
public int[] getOpParameterTable() {
	return __opParameterTable;
}

/**
Returns the name used during segment redefinition.
@return the name used during segment redefinition.
*/
public String getOpRedefName() {
	return __opRedefName;
}

/**
Returns the operation type name.
@return the operation type name.
*/
public String getOpTypeName() {
	return __opTypeName;
}

/**
Return the Rating Curve IDs.
@return the Vector of Rating Curve IDs.
*/
public Vector getRCIDs() {
	return __rcIDs;
}

/**
Return the Rating Curve ID at an index.
@param index Index of Rating Curve ID.
@return Rating Curve ID at an index.
*/
public String getRCID(int index) {
	return (String)__rcIDs.elementAt(index);
}

/**
Return the NWSRFS_RatingCurve.
@return the Vector of NWSRFS_RatingCurve objects.
*/
public Vector getRatingCurves() {
	return __ratingCurves;
}

/**
Return the NWSRFS_RatingCurve at an index.
@param index Index of rating curve.
@return NWSRFS_RatingCurve object at an index.
*/
public NWSRFS_RatingCurve getRatingCurve(int index) {
	return (NWSRFS_RatingCurve)__ratingCurves.elementAt(index);
}


/**
Return the rating curve matching the rating curve identifier 
or null if not found.
@param rcid Rating Curve identifier.
@return the Rating Curve matching the identifier.
*/
public NWSRFS_RatingCurve getRatingCurve (String rcid) {
	int size = __ratingCurves.size();
	NWSRFS_RatingCurve rc= null;
	for (int i = 0; i < size; i++)  {
		rc = (NWSRFS_RatingCurve)__ratingCurves.elementAt(i);
		if (rc.getRCID().equalsIgnoreCase(rcid)) {
			return rc;
		}
	}

	return rc;
}

/**
Get the Parent NWSRFS_Segment object for this Operation.
@return the NWSRFS_Segment parent.
*/
public NWSRFS_Segment getSegment() {
	return __segment;
}

/**
Return the Operation system identifier.
*/
public String getSystemID() {
	return __sysID;
}

/**
Return the TimeSeries DataTypes.
@return the Vector of TimeSeries DataTypes.
*/	
public Vector getTSDTs() {
	return __tsDTs;
}

/**
Return the TimeSeries DataType at an index.
@param index Index of TimeSeries DataType.
@return the TimeSeries DataType at an index.
*/
public String getTSDT(int index) {
	return (String)__tsDTs.elementAt(index);
}

/**
Return the TimeSeries IDs.
@return the Vector of TimeSeries Identifiers.
*/	
public Vector getTSIDs() {
	return __tsIDs;
}

/**
Return the TimeSeries IDs at an index.
@param index Index of TimeSeries identifiers.
@return the TimeSeries identifier at an index.
*/
public String getTSID(int index) {
	return (String)__tsIDs.elementAt(index);
}

/**
Return the TimeSeries.
@return the Vector of TimeSeries objects.
*/	
public Vector getTimeSeries() {
	return __timeseries;
}

/**
Return the TimeSeries at an index.
@param index Index of TimeSeries.
@return the TimeSeries at an index.
*/
public NWSRFS_TimeSeries getTimeSeries(int index) {
	return (NWSRFS_TimeSeries)__timeseries.elementAt(index);
}

/**
Return the TimeSeries matching the TimeSeries identifier or null if not found.
@param tsid TimeSeries identifier.
@return the TimeSeres matching the identifier.
*/
public NWSRFS_TimeSeries getTimeSeries(String tsid) {
	int size = __timeseries.size();
	NWSRFS_TimeSeries ts = null;
	for (int i = 0; i < size; i++) {
		ts = (NWSRFS_TimeSeries)__timeseries.elementAt(i);
		if (ts.getTSID().equalsIgnoreCase(tsid))  {
			return ts;
		}
	}
	return ts;
}

/**
Return the segment user identifier.
*/
public String getUserID() {
	return __userID;
}

/**
Initialize global objects
*/
private void initialize() {
	__sysID = "";
	__userID = "";
	__ratingCurves = new Vector();
	__rcIDs = new Vector();
	__tsDTs = new Vector();
	__tsIDs = new Vector();
	__timeseries = new Vector();

	__IDSEG = null;
	__opNumber = -1;
	__opTypeName = null;
	__opName = null;
	__opRedefName = null;
	__opCarryoverPointer = -1;
	__opParameters = new float[__segment.getNP()];
	__opParameterArrayPointer = -1;
	__opCarryoverPointerCO = -1;
	__opParameterTable = new int[__segment.getNT()];
}

/**
Sets the segment ID for this operation.
@param IDSEG the segment ID for this operation.
*/
public void setIDSEG(String IDSEG) {
	__IDSEG = IDSEG;
}

/**
Sets the int that points to the location in the C array where carryover
for this operation can be found.
@param opCarryoverPointer the int that points to the location in the C array 
where carryover for this operation can be found.
*/
public void setOpCarryoverPointer(int opCarryoverPointer) {
	__opCarryoverPointer = opCarryoverPointer;
}

/**
Sets the int that points to the second part of the carryover or C
array resides for a given operation.
@param opCarryoverPointerCO the int that points to the second part of the 
carryover or C array resides for a given operation.
*/
public void setOpCarryoverPointerCO(int opCarryoverPointerCO) {
	__opCarryoverPointerCO = opCarryoverPointerCO;
}

/**
Sets the operation name.
@param opName the operation name.
*/
public void setOpName(String opName) {
	__opName = opName;
}

/**
Sets the operation number.
@param opNumber the operation number.
*/
public void setOpNumber(int opNumber) {
	__opNumber = opNumber;
}

/**
Sets the location in the P array where the PO array or parameter data starts
for a given operation.
@param opParameterArrayPointer the location in the P array where the PO array 
or parameter data starts for a given operation.
*/
public void setOpParameterArrayPointer(int opParameterArrayPointer) {
	__opParameterArrayPointer = opParameterArrayPointer;
}

/**
Sets a PO array value.
@param pos the position in the array to set
@param val the value to set
*/
public void setOpParameters(int pos, float val) {
	__opParameters[pos] = val;
}

/**
Sets an op parameter table array value.
@param pos the position in the array to set.
@param val the value to set.
*/
public void setOpParameterTable(int pos, int val) {
	__opParameterTable[pos] = val;
}

/**
Sets the name used during segment redefinition.
@param opRedefName the name used during segment redefinition.
*/
public void setOpRedefName(String opRedefName) {
	__opRedefName = opRedefName;
}

/**
Sets the operation type name.
@param opTypeName the operation type name.
*/
public void setOpTypeName(String opTypeName) {
	__opTypeName = opTypeName;
}

/**
Set the Parent to a NWSRFS_Segment object for this Operation.
*/
public void setSegment(NWSRFS_Segment parent) {
		__segment = parent;
}

/**
Return a String representation of the operation (sysID,userID).
*/
public String toString() {
	__sysID = __sysID.trim();
	
	// the operation should be a fixed width of 10 characters.
	int len = __sysID.length();

	StringBuffer b_sys = new StringBuffer(__sysID);

	for (int i = len; i < 10; i++) {
		b_sys.append(' ');
	}
	
	__sysID = b_sys.toString();

	//return __sysID + " " +  __userID;
	return __sysID + __userID;
}

}
