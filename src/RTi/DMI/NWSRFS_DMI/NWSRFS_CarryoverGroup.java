//-----------------------------------------------------------------------------
// NWSRFS_CarryoverGroup - NWSRFS Carryover Group definition
//-----------------------------------------------------------------------------
// History:
//
// 2004-04-19	Scott Townsend, RTi	Initial version.
// 2004-07-26	A. Morgan Love, RTi	Added method to return
//					carryover dates
//					named: getCarryoverDates()
// 2004-08-18	J. Thomas Sapienza, RTi	* Revised to fit RTi standards.
//					* Added get*() and set*() methods.
// 2004-08-23	JTS, RTi		Added finalize().
//-----------------------------------------------------------------------------

package RTi.DMI.NWSRFS_DMI;

import java.util.List;
import java.util.Vector;

import RTi.DMI.NWSRFS_DMI.NWSRFS_ForecastGroup;
import RTi.DMI.NWSRFS_DMI.NWSRFS_Util;

import RTi.Util.Message.Message;

import RTi.Util.Time.DateTime;

/**
The NWSRFS_CarryoverGroup class stores the organizational information about an
NWSRFS Carryover Group (list of Forecast Groups). This class stores data from 
the FCCOGDEF processed database file. 

The FCCOGDEF file has the following definition:
<pre>

FILE NAME:  FCCOGDEF

Purpose

File FCCOGDEF contains the Carryover Group definitions.  It contains
the Carryover Group names and carryover dates for each Carryover
Group.  It also contains information needed to access the carryover
values which are stored in the file FCCARRY.

The first record of FCCOGDEF is held in common block FCCGD and any
one of the Carryover Group definitions are held in common block
FCCGD1.


Description

ATTRIBUTES: fixed length 456 byte binary records

RECORD STRUCTURE:

    Variable  Type  Dimension  Word Pos.  Description

    The first record contains file control information.
    NSLOTS     I*4     1         1        Number of carryover slots in file
                                          FCCARRY (maximum value is 20)

    NWR        I*4     1         2        Number of words per record in
                                          file FCCARRY

    NRSLOT     I*4     1         3        Number of records per slot in
                                          file FCCARRY

                       1         4        Unused

    NWPS       I*4     1         5        Number of words used in each
                                          carryover slot - NWPS is always
                                          less than or equal to NWR*NRSLOT

    ICRDAT     I*4     5         6        Creation date:
                                            ICRDAT(1) = month
                                            ICRDAT(2) = day
                                            ICRDAT(3) = year (4 digits)
                                            ICRDAT(4) = hour and minute
                                                        (military)
                                            ICRDAT(5) = seconds and
                                                        milliseconds

    NCG        I*4     1         11       Number of Carryover Groups defined
                                          (maximum value is 25)

    CGIDS      A8      25        12       Carryover Group identifiers for the
                                          NCG Carryover Groups

    ICOREC     I*4     25        62       Record number in file FCCOGDEF of
                                          Carryover Group definitions
                                          corresponding to Carryover Group
                                          identifiers in CGIDS

                               87-114     Not used

    Records 2-26 contain a Carryover Group definitions.
    CGID        A8      1         1       Carryover Group identifier

    ITDEF      I*4      5         3       Date and time Carryover Group was
                                          defined:
                                            ITDEF(1) =  month
                                            ITDEF(2) =  day
                                            ITDEF(3) =  year (4 digit)
                                            ITDEF(4) =  hour and minute
                                                        (military)
                                            ITDEF(5) =  seconds and
                                                        milliseconds

    NFG        I*4      1         8       Number of Forecast Groups in this
                                          Carryover Croup

    MINDT      I*4      1         9       The minimum time step (in hours) that
                                          this Carryover Croup can be run

    CGNAME     A20      1         10      Carryover Group description

    ICODAY     I*4     20         15      Julian day of the carryover values
                                          saved in each of the NSLOTS (see
                                          record 1) carryover slots - ICODAY(I)
                                          is less than or equal to zero for an
                                          unused carryover slot  1/

    ICOTIM     I*4     20         35      The internal clock hour of the
                                          carryover values saved in each of the
                                          NSLOTS (see record 1) carryover slots

    LUPDAY     I*4     20         55      The Julian day of the last run that
                                          updated the values of carryover
                                          values saved in each of the NSLOTS
                                          (see record 1) carryover slots  1/

    LUPTIM     I*4     20         75      The clock time of the last run that
                                          updated the values of carryover saved
                                          in each of the NSLOTS (see record 1)
                                          carryover slots - each value of
                                          LUPTIM is a nine digit integer with
                                          the following form:
                                             hhmmsskkk
                                          where
                                             hh = hours
                                             mm = minutes
                                             ss = seconds
                                            kkk = milliseconds

    IPC        I*4     20          95    The protected/completed indicator for
                                         each of the NSLOTS carryover slots:
                                            0 = volatile and incomplete
                                            1 = volatile and complete
                                            2 = protected and incomplete
                                            3 = protected and complete
                                          An incomplete slot does not have all
                                          Segments in the Carryover Group
                                          updated and is therefore worthless.
                                          A volatile (not protected) slot can
                                          be overwritten (used for a new date).
                                          When a slot is needed for a carryover
                                          date to be saved the following
                                          hierarchy applies:
                                            1 - use any slot with the same date
                                                and time regardless of status
                                            2 - use the oldest volatile slot
                                                whether complete or not
                                            3 - use the oldest incomplete slot
                                                whether protected or not
                                            4 - if all slots are protected and
                                                complete, stop the run


NOTES:

1/ Day 1 is January 1, 1900.

               IX.4.5B-FCCOGDEF
</pre>
*/
public class NWSRFS_CarryoverGroup {

/**
Julian day of carryover values in each _NSLOTS.
*/
protected int[] _ICODAY;

/**
Record number of subsequent records in the binary file FCCOGDEF for 
the corresponding Carryover Group in the _CGIDS array.
*/
protected int[] _ICOREC; 

/**
The interval hour of carryover values in each _NSLOTS.
*/
protected int[] _ICOTIM;

/**
Creation date.
*/
protected int[] _ICRDAT;

/**
The protected/completed indicator for each _NSLOTS.
*/
protected int[] _IPC;

/**
Date and time Carryover Group was defined in the binary file FCCOGDEF.
*/
protected int[] _ITDEF;

/**
Julian day of last run that updated Carryover Group in each _NSLOTS.
*/
protected int[] _LUPDAY;

/**
The interval hour of last run that updated Carryover Group in each _NSLOTS.
*/
protected int[] _LUPTIM;

/**
Minimum time step this Carryover Group can be run.
*/
protected int _MINDT;

/**
Number of Carryover Groups defined in the binary file FCCOGDEF.
*/
protected int _NCG;

/**
Number of Forecast Groups in this Carryover Group.
*/
protected int _NFG;

/**
Number of records per slot in the binary file FCCARRY.
*/
protected int _NRSLOT;

/**
Number of carryover slots.
*/
protected int _NSLOTS;

/**
Number of words used in carryover slot.
*/
protected int _NWPS;

/**
Number of words per record in the binary file FCCARRY.
*/
protected int _NWR;

/**
Carryover identifiers array for all Carryover Groups in the binary file 
FCCOGDEF.
*/
protected String[] _CGIDS;

/**
Carryover Group identifier.
*/
protected String _CGID;

/**
Identifier for the Carryover Group.
*/
private String __cgid;

/**
Carryover Group name.
*/
protected String _CGNAME;

/**
Forecast group IDs in the Carryover Group.
*/
private List	__fgID = null;

/**
Forecast group objects in the Carryover Group.
*/
private List	__forecast_groups = null;

/**
Constructor.  Initializes to have no forecast groups and no carryover group id.
*/
public NWSRFS_CarryoverGroup () {
	initialize();
}

/**
Constructor.  Initializes to have no forecast groups.
@param cgid the carryover group id to assign to this carryover group.
*/
public NWSRFS_CarryoverGroup(String cgid) {
	initialize();

	if (cgid != null) {
		__cgid = cgid;
	}
}

/**
Adds an NWSRFS_ForecastGroup to the NWSRFS_CarryoverGroup.
@param fg NWSRFS_ForecastGroup to add.
*/
public void addForecastGroup(NWSRFS_ForecastGroup fg) {
	__forecast_groups.add(fg);
}

/**
Adds a ForecastGroup ID to the NWSRFS_CarryoverGroup.
@param fgID ForecastGroup ID to add.
*/
public void addForecastGroupID(String fgID) {	
	__fgID.add(fgID);
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize()
throws Throwable {
	_ICODAY = null;
	_ICOREC = null;
	_ICOTIM = null;
	_ICRDAT = null;
	_IPC = null;
	_ITDEF = null;
	_LUPDAY = null;
	_LUPTIM = null;
	_CGIDS = null;
	_CGID = null;
	__cgid = null;
	_CGNAME = null;
	__fgID = null;
	__forecast_groups = null;
}

/**
Return a Vector of carryover dates as DateTime instances.
The dates will be at hour 12 Z time.
Debug information is printed during this method at Debug levels of 2, 3, and 6.
@return Vector of carryover dates as DateTime objects, guaranteed to be non-null.
*/
public List getCarryoverDates()
{
	String routine = "NWSRFS_CarryoverGroup.getCarryoverDates";
	int num = _ICODAY.length;

	if (Message.isDebugOn) {
		Message.printDebug(2, routine, "Number of carryover dates = " + num);
	}

	int julianDay = -999;
	int julianHour = -999;
	DateTime date = null;
	List v = new Vector();
	for (int i = 0; i < num; i++) {
		julianDay = _ICODAY[i];
		julianHour = _ICOTIM[i];
		if (Message.isDebugOn) {
			Message.printDebug(6, routine, "Jullian Carryover " 
				+ "day at position + " + i + " is: \"" + julianDay + "\"");
			Message.printDebug(6, routine, "Jullian Carryover " 
				+ "hour at position + " + i + " is: \"" + julianHour + "\"");
		}

		// Set carryover date to have hour 12 and timezone "Z".
		date = NWSRFS_Util.getDateFromJulianHour1900( (julianDay * 24 + 12) );
		date.setTimeZone("Z");

		// Do not add julian hour since now julianhour coming out as 24 which:
		// 1) carryover should be at hour 12 and
		// 2) when at hour 24 and converted to a DateTime, the date is rolled over to the next
		//    day at hour 0.
		//(julianDay * 24) + julianHour);

		if ( (date != null) && ( date.getYear() != 1900 )) {
			if (Message.isDebugOn) {
				Message.printDebug(3, routine,
						"Carryover date at position + " + i + " is: \"" + date + "\"");
			}
			v.add(date);
		}
	}
	return v;
}

/**
Returns carryover identifiers for all carryover groups in the binary file FCCOGDEF.
@param pos the position of the array to return
@return all carryover identifier.
*/
public String getCGIDS(int pos)
{
	return _CGIDS[pos];
}

/**
Returns carryover identifiers for all carryover groups in the binary file FCCOGDEF.
@return all carryover identifier array.
*/
public int getCGIDSLength() {
	return _CGIDS.length;
}

/**
Returns the carryover group identifier.
@return the carryover group identifier.
TODO (JTS - 2004-08-18) what about _CGID??
*/
public String getCGID() {
	return __cgid;
}

/**
Returns the carryover group name.
@return the carryover group name.
*/
public String getCGNAME() {
	return _CGNAME;
}

/**
Return the Forecast Group at an index.
@param index Index of Forecast Group.  Must be &gt;= 0 and &lt; 
__forecast_groups.size().
@return the Forecast Group at an index.
*/
public NWSRFS_ForecastGroup getForecastGroup(int index) {
	if(index >= __forecast_groups.size())
		return null;
	
	return (NWSRFS_ForecastGroup)__forecast_groups.get(index);
}

/**
Return the Forecast Group matching the Forecast Group identifier or null if
not found.
@param fgid Forecast Group identifier.
@return the Forecast Group matching the identifier, or null if the identifier
was not found.
*/
public NWSRFS_ForecastGroup getForecastGroup(String fgid) {
	int size = __forecast_groups.size();
	NWSRFS_ForecastGroup fg = null;
	for (int i = 0; i < size; i++) 	{
		fg = (NWSRFS_ForecastGroup)__forecast_groups.get(i);
		if (fg.getFGID().equalsIgnoreCase(fgid)) {
			return fg;
		}
	}
	return fg;
}

/**
Return the Forecast Group ID at an index.
@param index Index of Forecast Group ID.  Must be &gt;= 0 and &lt; 
__fgID.size().
@return the Forecast Group ID at an index.
*/
public String getForecastGroupID(int index) {
	return (String)__fgID.get(index);
}

/**
Returns the Vector of forecast group IDs in this carryover group.  Guaranteed to
be non-null.
@return the Vector of forecast group IDs in this carryover group.
*/
public List getForecastGroupIDs() {
	return __fgID;
}

/**
Returns the Vector of forecast groups in this carryover group.  Guaranteed to
be non-null.
@return the Vector of forecast groups in this carryover group.
*/
public List getForecastGroups() {
	return __forecast_groups;
}

/**
Returns the array of Julian days of carryover values for each _NSLOTS.
@return the array of Julian days of carryover values for each _NSLOTS.
*/
public int[] getICODAY() {
	return _ICODAY;
}

/**
Returns the array of record numbers of subsequent records in the binary 
file FCCOGDEF for the corresponding Carryover Group in the _CGIDS array.
@return the array of record numbers of subsequent records in the binary 
file FCCOGDEF for the corresponding Carryover Group in the _CGIDS array.
*/
public int getICOREC(int pos) {
	return _ICOREC[pos];
}

/**
Returns the array of interval hours of carryover values in each _NSLOTS.
@return the array of interval hours of carryover values in each _NSLOTS.
*/
public int[] getICOTIM() {
	return _ICOTIM;
}

/**
Returns the array of creation dates.
@return the array of creation dates.
*/
public int[] getICRDAT() {
	return _ICRDAT;
}

/**
Returns the array of protected/completed indicators for each _NSLOTS.
@return the array of protected/completed indicators for each _NSLOTS.
*/
public int[] getIPC() {
	return _IPC;
}

/**
Returns the array of dates and time the carryover group was defined in the
binary file FCCOGDEF.
@return the array of dates and time the carryover group was defined in the
binary file FCCOGDEF.
*/
public int[] getITDEF() {
	return _ITDEF;
}

/**
Returns the array of julian days of the last runs that updated carryover groups
in each _NSLOTS.
@return the array of julian days of the last runs that updated carryover groups
in each _NSLOTS.
*/
public int[] getLUPDAY() {
	return _LUPDAY;
}

/**
Returns the array of interval hours of last runs that updated carryover groups
in each _NSLOTS.
@return the array of interval hours of last runs that updated carryover groups
in each _NSLOTS.
*/
public int[] getLUPTIM() {
	return _LUPTIM;
}

/**
Returns the minimum time step this carryover group can be run.
@return the minimum time step this carryover group can be run.
*/
public int getMINDT() {
	return _MINDT;
}

/**
Returns the number of carryover groups defined in the binary file FCCOGDEF.
@return the number of carryover groups defined in the binary file FCCOGDEF.
*/
public int getNCG() {
	return _NCG;
}

/**
Returns the number of forecast groups in this carryover group.
@return the number of forecast groups in this carryover group.
*/
public int getNFG() {
	return _NFG;
}

/**
Returns the number of records per slot in the binary file FCCARRY.
@return the number of records per slot in the binary file FCCARRY.
*/
public int getNRSLOT() {
	return _NRSLOT;
}

/**
Returns the number of carryover slots.
@return the number of carryover slots.
*/
public int getNSLOTS() {
	return _NSLOTS;
}

/**
Return the number of Forecast Groups in this Carryover Group object.
@return an int value for the number of Forecast Groups defined on this 
Carryover Group object.
*/
public int getNumberOfForecastGroups() {
	return __forecast_groups.size();
}

/**
Returns the number of words used in carryover slot.
@return the number of words used in carryover slot.
*/
public int getNWPS() {
	return _NWPS;
}

/**
Returns the number of words per record in the binary file FCCARRY.
@return the number of words per record in the binary file FCCARRY.
*/
public int getNWR() {
	return _NWR;
}

/**
Initialize global objects.
*/
private void initialize() {
	_NSLOTS = 0;
	_NWR = 0;
	_NRSLOT = 0;
	_NWPS = 0;
	_ICRDAT = new int[5];
	_NCG = 0;
	_CGIDS = new String[25];
	_ICOREC = new int[25]; 
	_CGID = null;
	_ITDEF = new int[5];
	_NFG = 0;
	_MINDT = 0;
	_CGNAME = null;
	_ICODAY = new int[20];
	_ICOTIM = new int[20];
	_LUPDAY = new int[20];
	_LUPTIM = new int[20];
	_IPC = new int[20];
	__cgid = null;
	__fgID = new Vector();
	__forecast_groups = new Vector();
}

/**
Sets the carryover group identifier.
@param CGID the carryover group identifier.
REVISIT (JTS - 2004-08-18)
what about _CGID???
*/
public void setCGID(String CGID) {
	__cgid = CGID;
}

/**
Sets one of the CGID array position.
@param pos the position in the record array to set.
@param cgid the sub
*/
public void setCGIDS(int pos, String cgid) {
	_CGIDS[pos] = cgid;
}

/**
Sets the carryover group name.
@param CGNAME the carryover group name.
*/
public void setCGNAME(String CGNAME) {
	_CGNAME = CGNAME;
}

/**
Sets a value in the array of julian days of carryover values for each _NSLOTS.
@param pos the position in the array to set.
@param value the value to set.
*/
public void setICODAY(int pos, int value) {
	_ICODAY[pos] = value;
}

/**
Sets a value in the array of record numbers of subsequent records in the binary 
file FCCOGDEF for the corresponding Carryover Group in the _CGIDS array.
@param pos the position in the array to set.
@param value the value to set in the array.
*/
public void setICOREC(int pos, int value) {
	_ICOREC[pos] = value;
}

/**
Sets a value in the array of interval hours of carryover values in each _NSLOTS.
@param pos the position in the array to set.
@param value the value to set in the array.
*/
public void setICOTIM(int pos, int value) {
	_ICOTIM[pos] = value;
}

/**
Sets a value in the array of creation dates.
@param pos the position in the array to set.
@param value the value to set in the array.
*/
public void setICRDAT(int pos, int value) {
	_ICRDAT[pos] = value;
}

/**
Sets a value in the array of protected/completed indicators for each _NSLOTS.
@param pos the position in the array to set.
@param value the value to set.
*/
public void setIPC(int pos, int value) {
	_IPC[pos] = value;
}

/**
Sets a value in the array of dates and time the carryover group was 
defined in the binary file FCCOGDEF.
@param pos the position in the array to set.
@param value the value to set in the array.
*/
public void setITDEF(int pos, int value) {
	_ITDEF[pos] = value;
}

/**
Sets a value in the array of julian days of the last runs that updated 
carryover groups in each _NSLOTS.
@param pos the position in the array to set.
@param value the value to set in the array.
*/
public void setLUPDAY(int pos, int value) {
	_LUPDAY[pos] = value;
}

/**
Sets a value in the array of interval hours of last runs that updated 
carryover groups in each _NSLOTS.
@param pos the position in the array to set.
@param value the value to set in the array.
*/
public void setLUPTIM(int pos, int value) {
	_LUPTIM[pos] = value;
}

/**
Sets the minimum time step this carryover group can be run.
@param MINDT the minimum time step this carryover group can be run.
*/
public void setMINDT(int MINDT) {
	_MINDT = MINDT;
}

/**
Sets the number of carryover groups defined in the binary file FCCOGDEF.
@param NCG the number of carryover groups defined in the binary file FCCOGDEF.
*/
public void setNCG(int NCG) {
	_NCG = NCG;
}

/**
Sets the number of forecast groups in this carryover group.
@param NFG the number of forecast groups in this carryover group.
*/
public void setNFG(int NFG) {
	_NFG = NFG;
}

/**
Sets the number of records per slot in the binary file FCCARRY.
@param NRSLOT the number of records per slot in the binary file FCCARRY.
*/
public void setNRSLOT(int NRSLOT) {
	_NRSLOT = NRSLOT;
}

/**
Sets the number of carryover slots.
@param NSLOTS the number of carryover slots.
*/
public void setNSLOTS(int NSLOTS) {
	_NSLOTS = NSLOTS;
}

/**
Sets the number of words used in carryover slot.
@param NWPS the number of words used in carryover slot.
*/
public void setNWPS(int NWPS) {
	_NWPS = NWPS;
}

/**
Sets the number of words per record in the binary file FCCARRY.
@param NWR the number of words per record in the binary file FCCARRY.
*/
public void setNWR(int NWR) {
	_NWR = NWR;
}

/**
Returns a String representation of the Carryover Group (the ID).
@return a String representation of the Carryover Group (the ID).
*/
public String toString() {
	return __cgid;
}

}
