//-----------------------------------------------------------------------------
// NWSRFS_Segment - class to store the organizational information about an 
//                  NWSRFS segment group (list of operations).
//-----------------------------------------------------------------------------
// History:
//
// 
// 2004-3-31	Shawn chen		Initial version  
// 2004-4-15	Scott Townsend, RTi	Update due to change in design
// 2004-07-12   Anne Morgan Love, RTi   Removed setVerbose(), which
//                                      added additional identifier strings in
//                                      front of the String returned from
//                                      .toString() if was true (for example:
//                                      "SEG: " for segment).
// 2004-08-19	J. Thomas Sapienza, RTi	* Revised for RTi standards.
//					* Added get*() and set*() methods.
//-----------------------------------------------------------------------------
// Endheader


package RTi.DMI.NWSRFS_DMI;

import java.util.Vector;

import RTi.DMI.NWSRFS_DMI.NWSRFS_Carryover;
import RTi.DMI.NWSRFS_DMI.NWSRFS_ForecastGroup;
import RTi.DMI.NWSRFS_DMI.NWSRFS_Operation;

import RTi.Util.Message.Message;

/**
The NWSRFS_Segment class stores the organizational information about an
NWSRFS Segment group (list of operations). This class reads and stores data 
from the FCSEGSTS processed database file; The FCSEGSTS database file has 
the following description:
<pre>

FILE NAME:  FCSEGSTS

Purpose

File FCSEGSTS contains Segment definitions status information.
Record numbers for a given Segment can be found in file FCSEGPTR.
One Segment definition can be held in common block FSGSTS.


Description

ATTRIBUTES: fixed length 260 byte binary records
RECORD STRUCTURE:

  Variable     Type   Dimension    Word Pos.   Description

  IDSEG         A8       1             1       Segment identifier (blank if 
  						not used)

  IUPSEG        A8       5             3       Upstream Segments (blank if none)

  IDNSEG        A8       2            13       Downstream Segments (blank if 
  						none)
                                                 (1) = first Segment
                                                 (2) = second Segment

  IPREC        I*4       1            17       Record number of first
                                               parameter record in file FCPARAM

  IWOCRY       I*4       1            18       Word offset in carryover file 
  						FCCARRY

  IFGID         A8       1            19       Forecast Group identifier to
                                               which this Segment belongs
                                               (blank if none)

  ICGID         A8       1            21       Carryover Group identifier to
                                               which this Segment belongs
                                               (blank if none)

  SGDSCR       A20       1            23       Segment description

  ICRDTE       I*4       5            28       Date Segment defined or 
  						redefined:
                                                 (1) =  month
                                                 (2) =  day
                                                 (3) =  year (4 digit)
                                                 (4) =  hour (military time)
                                                 (5) =  seconds and hundredths

  MINDT        I*4       1            33       Minimum time step this Segment
                                               can be run (hours)

  XLAT         R*4       1            34       Latitude in degrees and
                                               decimal degrees - range of +90
                                               to -90 (positive for north) -
                                               value if undefined is 100

  XLONG        R*4       1            35       Longitude in degrees and
                                               decimal degrees - range of
                                               +180 to -180 (positive for
                                               west) - undefined when XLAT is 
					       100
  NC           I*4       1            36       Length of C array

  ND           I*4       1            37       Length of D array

  NT           I*4       1            38       Length of T array

  NTS          I*4       1            39       Length of TS array

  NP           I*4       1            40       Length of P array

  NCOPS        I*4       1            41       Number of Operations with 
  						carryover

  INCSEG       I*4      20            42       Carryover status for each slot:
                                                 1 = complete
                                                 0 = incomplete

  IDEFSG       I*4       1            62       Segment definition status:
                                                 0 = defined
                                                 1 = parameters not stored
                                                 2 = time series were not
                                                     found when Segment was 
						     defined

  IEREC        I*4       1            63       Record number in file ESPPARM:
                                                 0 = Segment not used for ESP

                                      64-65    Not used

               IX.4.5B-FCSEGSTS
</pre> 
*/
public class NWSRFS_Segment {

/**
Grandparent to this Segment.
*/
private NWSRFS_CarryoverGroup __cg;

/**
Parent to this Segment.
*/
private NWSRFS_ForecastGroup __fg;

/**
Identifier for the Segment.
*/
private String __segmentID;

/**
Carryover in the Segment.
*/
private Vector	__carryover;

/**
Operations in the Segment.
*/
private Vector	__operations;

/**
Latitude in degrees and decimal degrees.
*/
protected float _XLAT;

/**
Longitude in degrees and decimal degrees.
*/
protected float _XLONG;

/**
Data Segment defined or redefined.
*/
protected int[] _ICRDTE;

/**
Carryover status for each slot.
*/
protected int[] _INCSEG;

/**
Segment definition status.
*/
protected int _IDEFSG;

/**
Record number in file ESPPARM.
*/
protected int _IEREC;
  
/**
Record number of first parameter record in FCPARAM.
*/
protected int _IPREC;

/**
Word offset in carryover file FCCARRY.
*/
protected int _IWOCRY;

/**
Minimum time step this Segment can be run.
*/
protected int _MINDT;

/**
Length of the NWSRFS C array.
*/
protected int _NC;

/**
Number of Operations with carryover.
*/
protected int _NCOPS;

/**
Length of the NWSRFS D array.
*/
protected int _ND;

/**
Length of the NWSRFS P array.
*/
protected int _NP;

/**
Length of the NWSRFS T array.
*/
protected int _NT;

/**
Length of the NWSRFS TS array.
*/
protected int _NTS;

/**
Carryover Group identifier to which this Segment belongs.
*/
protected String _ICGID;

/**
Downstream Segments array.
*/
protected String[] _IDNSEG;

/**
Upstream Segments array.
*/
protected String[] _IUPSEG;

/**
Segment identifier.
*/
protected String _IDSEG;

/**
Forecast Group identifier to which this Segment belongs.
*/
protected String _IFGID;

/**
Segment description.
*/
protected String _SGDSCR;

/**
Constructor.  Initializes without a parent segment.
@param id segment ID.  Can be null.
*/
public NWSRFS_Segment(String id) {
	initialize();

	if (id != null) {
		__segmentID = id;
	}
}

/**
Constructor.  
@param id segment id.
@param parent the NWSRFS_ForecastGroup parent to this Segment object.
*/
public NWSRFS_Segment(String id, NWSRFS_ForecastGroup parent) {
	initialize();

	if (id != null) {
		__segmentID = id;
	}

	if (parent != null) {
		__fg = parent;
	}
}

/**
Add carryover (states) to this segment. This will be carryover that
is defined on this Segment object in a specific carryover slot.
@param co A NWSRFS_Carryover object to add to the segment.
*/
public void addCarryover(NWSRFS_Carryover co) {
		__carryover.addElement( co );
}

/**
Add an Operation to this segment. Generally this will be an Operation that is
is defined on this Segment object.
@param op A NWSRFS_Operation object to add to the segment.
*/
public void addOperation(NWSRFS_Operation op) {
		__operations.addElement( op );
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize() 
throws Throwable {
	__cg = null;
	__fg = null;
	__segmentID = null;
	__carryover = null;
	__operations = null;
	_ICRDTE = null;
	_INCSEG = null;
	_ICGID = null;
	_IDNSEG = null;
	_IUPSEG = null;
	_IDSEG = null;
	_IFGID = null;
	_SGDSCR = null;
}

/**
Return the Carryover.
@return the Vector of Carryover objects in the Segment.
*/	
public Vector getCarryover() {
	return __carryover;
}

/**
Return the Carryover at an index.
@param index Index of Carryover.
@return the Carryover object at an index.
*/
public NWSRFS_Carryover getCarryover(int index) {
	return (NWSRFS_Carryover)__carryover.elementAt(index);
}

/**
Returns the NWSRFS_CarryoverGroup that is the grandparent of this Segment.
*/
public NWSRFS_CarryoverGroup getCarryoverGroup() {
	return __cg;
}

/**
Returns the NWSRFS_ForecastGroup that is the parent of this Segment.
@return the NWSRFS_ForecastGroup that is the parent of this Segment.
*/
public NWSRFS_ForecastGroup getForecastGroup() {
	return __fg;
}

/**
Returns the carryover group identifier.
@return the carryover group identifier.
*/
public String getICGID() {
	return _ICGID;
}

/**
Returns the data segment defined or redefined array.
@return the data segment defined or redefined array.
*/
public int[] getICRDTE() {
	return _ICRDTE;
}

/**
Returns the segment definition status.
@return the segment definition status.
*/
public int getIDEFSG() {
	return _IDEFSG;
}

/**
Returns the downstream segments array.
@return the downstream segments array.
*/
public String[] getIDNSEG() {
	return _IDNSEG;
}

/**
Returns the segment identifier.
@return the segment identifier.
*/
public String getIDSEG() {
	return _IDSEG;
}

/**
Returns the record number in file ESPPARM.
@return the record number in file ESPPARM.
*/
public int getIEREC() {
	return _IEREC;
}

/**
Returns the forecast group identifier.
@return the forecast group identifier.
*/
public String getIFGID() {
	return _IFGID;
}

/**
Returns the carryover status array.
@return the carryover status array.
*/
public int[] getINCSEG() {
	return _INCSEG;
}

/**
Returns the record number of the first parameter record.
@return the record number of the first parameter record.
*/
public int getIPREC() {
	return _IPREC;
}

/**
Returns the upstream segments array.
@return the upstream segments array.
*/
public String[] getIUPSEG() {
	return _IUPSEG;
}

/**
Returns the word offset in the carryover file.
@return the word offset in the carryover file.
*/
public int getIWOCRY() {
	return _IWOCRY;
}

/** 
Returns the minimum time step this segment can be run.
@return the minimum time step this segment can be run.
*/
public int getMINDT() {
	return _MINDT;
}

/**
Returns the length of the NWSRFS C array.
@return the length of the NWSRFS C array.
*/
public int getNC() {
	return _NC;
}

/**
Returns the number of operations with carryover.
@return the number of operations with carryover.
*/
public int getNCOPS() {
	return _NCOPS;
}

/**
Returns the length of the NWSRFS D array.
@return the length of the NWSRFS D array.
*/
public int getND() {
	return _ND;
}

/**
Returns the length of the NWSRFS P array.
@return the length of the NWSRFS P array.
*/
public int getNP() {
	return _NP;
}

/**
Returns the length of the NWSRFS T array.
@return the length of the NWSRFS T array.
*/
public int getNT() {
	return _NT;
}

/**
Returns the length of the NWSRFS TS array.
@return the length of the NWSRFS TS array.
*/
public int getNTS() {
	return _NTS;
}

/**
Returns the number of carryover slots defined for this Segment object.
@return the number of carryover slots defined for this Segment object.
*/
public int getNumberOfCarryover() {
	return __carryover.size();
}

/**
Returns the number of Operations for this Segment object.
@return the number of Operations defines on this Segment object.
*/
public int getNumberOfOperations() {
	return __operations.size();
}

/**
Returns the Operations.
@return the Vector of Operation objects.
*/	
public Vector getOperations() {
	return __operations;
}

/**
Return the Operation at an index.
@param index Index of Operation.
@return the Operation at an index.
*/
public NWSRFS_Operation getOperation(int index) {
	return (NWSRFS_Operation)__operations.elementAt(index);
}

/**
Return the Operation matching the Operation identifier or null if not found.
@param sysid Operation system identifier.
@param userid Operation user identifier.
@return the Operation matching the identifer.
*/	
public NWSRFS_Operation getOperation(String sysid, String userid) {
	int size = __operations.size();
	NWSRFS_Operation op= null;
	for (int i = 0; i < size; i++) {
		op = (NWSRFS_Operation)__operations.elementAt(i);
		if (	op.getSystemID().equalsIgnoreCase(sysid) 
			&& op.getUserID().equalsIgnoreCase(userid)) {
			return op;
		}
	}
	return op;
}

/**
Returns the segment description.
@return the segment description.
*/
public String getSGDSCR() {
	return _SGDSCR;
}

/**
Returns the Segment identifier.
@return the segment identifier.
*/
public String getSegID() {
	return __segmentID;
}

/**
Returns the latitude.
@return the latitude.
*/
public float getXLAT() {
	return _XLAT;
}

/**
Returns the longitude.
@return the longitude.
*/
public float getXLONG() {
	return _XLONG;
}

/**
Initialize global objects.
*/
private void initialize() {
	__carryover = new Vector();
	__segmentID = null;
	__fg = null;
	__operations = new Vector();
	_IDSEG = null;
	_IUPSEG = new String[5];
	_IDNSEG = new String[2];
	_IPREC = 0;
	_IWOCRY = 0;
	_IFGID = null;
	_ICGID = null;
	_SGDSCR = null;
	_ICRDTE = new int[5];
	_MINDT = 0;
	_XLAT = 0;
	_XLONG = 0;
	_NC = 0;
	_ND = 0;
	_NT = 0;
	_NTS = 0;
	_NP = 0;
	_NCOPS = 0;
	_INCSEG = new int[20];
	_IDEFSG = 0;
	_IEREC = 0;
}

/**
Set the NWSRFS_CarryoverGroup that is the grandparent of this Segment.
@param gParent the segment grandparent.
*/
public void setCarryoverGroup(NWSRFS_CarryoverGroup gParent) {
	String routine = "NWSRFS_Segment.setCarryoverGroup";

	try {
		if (gParent == null) {
			__cg = new NWSRFS_CarryoverGroup(_ICGID);
		}
		else {
			__cg = gParent;
		}
	}
	catch (Exception e) {
		Message.printWarning(2, routine, 
			"An Exception occured: " + e.getMessage());
		Message.printWarning(2, routine, e);
	}
}

/**
Sets the id String. 
@param id the Segment id string to set. 
*/
public void setSegID(String id) {
	__segmentID = id;
}

/**
Set the NWSRFS_ForecastGroup that is the parent of this Segment.
@param parent the segment parent.
*/
public void setForecastGroup(NWSRFS_ForecastGroup parent) {
	String routine = "NWSRFS_Segment.setForecastGroup";

	try {
		if (parent == null) {
			__fg = new NWSRFS_ForecastGroup(_IFGID);
		}
		else {
			__fg = parent;
		}
	}
	catch(Exception e) {
		Message.printWarning(2, routine,
			"An Exception occured: " + e.getMessage());
		Message.printWarning(2, routine, e);
	}
}

/**
Sets the carryover group identifier.
@param XICGID the carryover group identifier.
*/
public void setICGID(String XICGID) {
	_ICGID = XICGID;
}

/**
Sets the data segment defined or redefined array.
@param pos the position in the array to set.
@param XICRDTE the data segment defined or redefined array.
*/
public void setICRDTE(int pos, int XICRDTE) {
	_ICRDTE[pos] = XICRDTE;
}

/**
Sets the segment definition status.
@param XIDEFSG the segment definition status.
*/
public void setIDEFSG(int XIDEFSG) {
	_IDEFSG = XIDEFSG;
}

/**
Sets the downstream segments array.
@param pos the position in the array to set.
@param XIDNSEG the downstream segments array.
*/
public void setIDNSEG(int pos, String XIDNSEG) {
	_IDNSEG[pos] = XIDNSEG;
}

/**
Sets the segment identifier.
@param XIDSEG the segment identifier.
*/
public void setIDSEG(String XIDSEG) {
	_IDSEG = XIDSEG;
}

/**
Sets the record number in file ESPPARM.
@param XIEREC the record number in file ESPPARM.
*/
public void setIEREC(int XIEREC) {
	_IEREC = XIEREC;
}

/**
Sets the forecast group identifier.
@param XIFGID the forecast group identifier.
*/
public void setIFGID(String XIFGID) {
	_IFGID = XIFGID;
}

/**
Sets the carryover status array.
@param pos the position in the array to set.
@param XINCSEG the carryover status array.
*/
public void setINCSEG(int pos, int XINCSEG) {
	_INCSEG[pos] = XINCSEG;
}

/**
Sets the record number of the first parameter record.
@param XIPREC the record number of the first parameter record.
*/
public void setIPREC(int XIPREC) {
	_IPREC = XIPREC;
}

/**
Sets the upstream segments array.
@param pos the position in the array to set.
@param XIUPSEG the upstream segments array.
*/
public void setIUPSEG(int pos, String XIUPSEG) {
	_IUPSEG[pos] = XIUPSEG;
}

/**
Sets the word offset in the carryover file.
@param XIWOCRY the word offset in the carryover file.
*/
public void setIWOCRY(int XIWOCRY) {
	_IWOCRY = XIWOCRY;
}

/** 
Sets the minimum time step this segment can be run.
@param XMINDT the minimum time step this segment can be run.
*/
public void setMINDT(int XMINDT) {
	_MINDT = XMINDT;
}

/**
Sets the length of the NWSRFS C array.
@param XNC the length of the NWSRFS C array.
*/
public void setNC(int XNC) {
	_NC = XNC;
}

/**
Sets the number of operations with carryover.
@param XNCOPS the number of operations with carryover.
*/
public void setNCOPS(int XNCOPS) {
	_NCOPS = XNCOPS;
}

/**
Sets the length of the NWSRFS D array.
@param XND the length of the NWSRFS D array.
*/
public void setND(int XND) {
	_ND = XND;
}

/**
Sets the length of the NWSRFS P array.
@param XNP the length of the NWSRFS P array.
*/
public void setNP(int XNP) {
	_NP = XNP;
}

/**
Sets the length of the NWSRFS T array.
@param XNT the length of the NWSRFS T array.
*/
public void setNT(int XNT) {
	_NT = XNT;
}

/**
Sets the length of the NWSRFS TS array.
@param XNTS the length of the NWSRFS TS array.
*/
public void setNTS(int XNTS) {
	_NTS = XNTS;
}

/**
Sets the segment description.
@param XSGDSCR the segment description.
*/
public void setSGDSCR(String XSGDSCR) {
	_SGDSCR = XSGDSCR;
}

/**
Sets the latitude.
@param XXLAT the latitude.
*/
public void setXLAT(float XXLAT) {
	_XLAT = XXLAT;
}

/**
Sets the longitude.
@param XXLONG the longitude.
*/
public void setXLONG(float XXLONG) {
	_XLONG = XXLONG;
}

/**
Returns a String representation of the Segment (the ID).
@return a String representation of the Segment (the ID).
*/
public String toString () {
	return __segmentID;
}

}
