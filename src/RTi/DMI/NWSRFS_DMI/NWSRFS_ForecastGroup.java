//-----------------------------------------------------------------------------
// NWSRFS_ForecastGroup - NWSRFS Forecast Group definition
//-----------------------------------------------------------------------------
// History:
//
// 2004-04-19	Scott Townsend, RTi	Initial version.
// 2004-07-12	Anne Morgan Love, RTi	Removed setVerbose(), which
//					added additional identifier strings in
//					front of the String returned from
//					.toString() if was true (for example:
//					"FG: " for forecast group).
// 2004-08-19	J. Thomas Sapienza, RTi	* Revised for RTi standards.
//					* Added get*() and set*() methods.
//-----------------------------------------------------------------------------

package RTi.DMI.NWSRFS_DMI;

import java.util.List;
import java.util.Vector;

import RTi.DMI.NWSRFS_DMI.NWSRFS_CarryoverGroup;
import RTi.DMI.NWSRFS_DMI.NWSRFS_Segment;

/**
The NWSRFS_ForecastGroup class stores the organizational information about an
NWSRFS Forecast Group (list of Segments). This class reads and stores data 
from the FCFGSTAT processed database file.  The FCFGSTAT file has the 
following definition:
<pre>

FILE NAME:  FCFGSTAT

Purpose

File FCFGSTAT contains the status of all Forecast Group definitions
including a pointer to the list of Segments in each Forecast Group
which is in file FCFGLIST.

One Forecast Group definition is held in common block FCFGS.


Description

ATTRIBUTES: fixed length 80 byte binary records

RECORD STRUCTURE:

  Variable   Type   Dimension   Word Pos.   Description

  FGID        A8       1           1        Forecast Group identifier

  NSEG       I*4       1           3        Number of Segments in Forecast
                                            Group

  IREC       I*4       1           4        Record number of first Segment
                                            identifier in file FCFGLIST
                                            for this Forecast Group

  ISPEC      I*4       1           5        Special Forecast Group
                                            indicator:
                                              0 = normal Forecast Group
                                              1 = special Forecast Group
                                            A Segment can belong to only
                                            one normal Forecast Group but
                                            more than one special Forecast
                                            Groups. A special Forecast
                                            Group cannot belong to a
                                            Carryover Group.

   CGID      A8        1           6        Carryover Group identifier to
                                            which this Forecast Group
                                            belongs - blank if none

   ICOSEQ    I*4       1           8        Computational order of this
                                            Forecast Group in the
                                            Carryover Group

   MINDT     I*4       1           9        Minimum time step that this
                                            Forecast Group can be run

   DESCR     A20       1          10        Forecast Group description

   ICRDAT    I*4       5          15        Forecast Group creation date:
                                               ICRDAT(1) = month
                                               ICRDAT(2) = day
                                               ICRDAT(3) = year
                                               ICRDAT(4) = hour and minute
                                                           (military)
                                               ICRDAT(5) = seconds and
                                                           milliseconds

   NFGREC     I*4      1           20       Number of Forecast Groups defined
                                            (only used in record 1)

   MFGREC     I*4      1           20       Maximum number of Forecast Groups
                                            allowed (only used in record 2)

              IX.4.5B-FCFGSTAT
</pre>
*/
public class NWSRFS_ForecastGroup {	

/**
Forecast Group creation date.
*/
protected int[] _ICRDAT;

/**
Computational order of this Forecast Group.
*/
protected int _ICOSEQ;

/**
Record number of first segment contained in the Forecast Group in the FCFGLIST 
file.  The combination of NSEG and IREC will provide the means to pull the 
list of Segment IDs from the FCFGLIST file for this Forecast Group. 
*/
protected int _IREC;

/**
Special Forecast Group indicator. 
*/
protected int _ISPEC;

/**
Maximum number of Forecast Groups allowed in the binary database file FCFGSTAT.
*/
protected int _MFGREC;

/**
Minimum time step this Forecast Group can run.
*/
protected int _MINDT;

/**
Number of Forecast Groups defined in the binary database file FCFGSTAT.
*/
protected int _NFGREC;

/**
Number of segments in the Forecast Group.
*/
protected int _NSEG;

/**
Parent to the Forecast Group.
*/
private NWSRFS_CarryoverGroup __cg = null;

/**
Carryover group identifier for this Forecast Group. If it is a special 
Forecast Group it may not have a Carryover Group parent.
*/
protected String _CGID;

/**
Forecast Group description.
*/
protected String _DESCR;

/**
Identifier for the Forecast Group.
*/
private String __fgid;

/**
Forecast Group identifier.
*/
protected String _FGID;

/**
Segments in the Forecast Group.
*/
private List	__segment = new Vector();

/**
Segment IDs in the Forecast Group.
*/
private List	__segmentID = new Vector();

/**
Constructor.  Initializes with no segment groups or parent.
@param id forecast group ID.
*/
public NWSRFS_ForecastGroup (String id) {	
	initialize();

	if (id != null) {
		__fgid = id;
	}
}

/**
Constructor.  Initializes with no segment groups.
@param id forecast group ID.
@param parent NWSRFS_CarryoverGroup that this ForecastGroup inherits from.
*/
public NWSRFS_ForecastGroup (String id, NWSRFS_CarryoverGroup parent) {	
	initialize();

	if (id != null)  {
		__fgid = id;
	}

	if (parent != null) {
		__cg = parent;
	}
}

/**
Add an NWSRFS_Segment to the NWSRFS_ForecastGroup.
@param seg NWSRFS_Segment to add.
*/
public void addSegment(NWSRFS_Segment seg) {
	__segment.add(seg);
}

/**
Add a Segment ID to the NWSRFS_ForecastGroup.
@param segID Segment ID to add.
*/
public void addSegmentID(String segID) {
	__segmentID.add(segID);
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize()
throws Throwable {
	_ICRDAT = null;
	__cg = null;
	_CGID = null;
	_DESCR = null;
	__fgid = null;
	_FGID = null;
	__segment = null;
	__segmentID = null;
}

/**
Returns the NWSRFS_CarryoverGroup that is the parent of this Forecast Group.
@return the NWSRFS_CarryoverGroup that is the parent of this Forecast Group.
*/
public NWSRFS_CarryoverGroup getCarryoverGroup() {
	return __cg;
}

/**
Returns the carryover group identifier for this forecast group.
@return the carryover group identifier for this forecast group.
*/
public String getCGID() {
	return _CGID;
}

/**
Returns the forecast group description.
@return the forecast group description.
*/
public String getDESCR() {
	return _DESCR;
}

/**
Return the Forecast Group identifier.
@return the Forecast Group identifier.
*/
public String getFGID() {
	return __fgid;
}

/**
Returns the forecast group identifier.
@return the forecast group identifier.
REVISIT (JTS - 2004-08-19)
how does this differ from __fgid??
*/
public String getFGID1() {
	return _FGID;
}

/**
Returns the computational order of this forecast group.
@return the computational order of this forecast group.
*/
public int getICOSEQ() {
	return _ICOSEQ;
}

/**
Returns the array of the forecast group creation date.
@return the array of the forecast group creation date.
*/
public int[] getICRDAT() {
	return _ICRDAT;
}

/**
Returns the record number of the first segment in the forecast group.
@return the record number of the first segment in the forecast group.
*/
public int getIREC() {
	return _IREC;
}

/**
Returns the special forecast group indicator.
@return the special forecast group indicator.
*/
public int getISPEC() {
	return _ISPEC;
}

/**
Returns the maximum number of forecast groups allowed in the binary database.
@return the maximum number of forecast groups allowed in the binary database.
*/
public int getMFGREC() {
	return _MFGREC;
}

/**
Returns the minimum time step this forecast group can run.
@return the minimum time step this forecast group can run.
*/
public int getMINDT() {
	return _MINDT;
}

/**
Returns the number of forecast groups defined in the binary database.
@return the number of forecast groups defined in the binary database.
*/
public int getNFGREC() {
	return _NFGREC;
}

/**
Returns the number of segments in the forecast group.
@return the number of segments in the forecast group.
*/
public int getNSEG() {
	return _NSEG;
}

/**
Return the number of Segments in this Forecast Group object.
@return an int value for the number of Segments define on this Forecast 
Group object.
*/
public int getNumberOfSegments() {
	return __segment.size();
}

/**
Return the number of Segment IDs in this Forecast Group object.
@return an int value for the number of Segment IDs define on this Forecast 
Group object.
*/
public int getNumberOfSegmentIDs() {
	return __segmentID.size();
}

/**
Return the Segment at an index.
@param index Index of Segment.
@return the Segment at an index.
*/
public NWSRFS_Segment getSegment(int index) {
	return (NWSRFS_Segment)__segment.get(index);
}

/**
Return the Segment matching the Segment identifier or null if not found.
@param segid Segment identifier.
@return the Segment matching the identifier.
*/
public NWSRFS_Segment getSegment (String segid) {
	int size = __segment.size();
	NWSRFS_Segment seg = null;
	for (int i = 0; i < size; i++) {
		seg = (NWSRFS_Segment)__segment.get(i);
		if (seg.getSegID().equalsIgnoreCase(segid)) {
			return seg;
		}
	}
	return seg;
}

/**
Return the Segments groups.  This is guaranteed to be non-null.
@return the list of Segments.
*/
public List getSegments() {
	return __segment;
}

/**
Return the Segment ID at an index.
@param index Index of Segment.
@return the Segment ID at an index.
*/
public String getSegmentID(int index) {
	return (String)__segmentID.get(index);
}

/**
Return the Segment IDs.
@return the list of Segment IDs.
*/
public List getSegmentIDs() {
	return __segmentID;
}

/**
Initialize global objects.
*/
private void initialize() {
	__cg = null;
	__fgid = null;
	_FGID = null;
	_NSEG = 0;
	_IREC = 0;
	_ISPEC = 0;
	_CGID = null;
	_ICOSEQ = 0;
	_MINDT = 0;
	_DESCR = null;
	_ICRDAT = new int[5];
	_NFGREC = 0;
	_MFGREC = 0;
}

/**
Set the Parent to a NWSRFS_CarryoverGroup object for this Forecast Group.
*/
public void setCarryoverGroup(NWSRFS_CarryoverGroup parent) {
	__cg = parent;
}

/**
Sets the carryover group identifier for this forecast group.
@param CGID the carryover group identifier for this forecast group.
*/
public void setCGID(String CGID) {
	_CGID = CGID;
}

/**
Sets the forecast group description.
@param DESCR the forecast group description.
*/
public void setDESCR(String DESCR) {
	_DESCR = DESCR;
}

/**
Sets the id String. 
@param id the Forecast Group id string to set. 
*/
public void setFGID(String id) {
	__fgid = id;
}

/**
Sets the forecast group identifier.
@param FGID the forecast group identifier.
REVISIT (JTS - 2004-08-19)
how does this differ from __fgid??
*/
public void setFGID1(String FGID) {
	_FGID = FGID;
}

/**
Sets the computational order of this forecast group.
@param ICOSEQ the computational order of this forecast group.
*/
public void setICOSEQ(int ICOSEQ) {
	_ICOSEQ = ICOSEQ;
}

/**
Sets a value in the array of the forecast group creation date.
@param pos the position to set.
@param value the value to set in the array.
*/
public void setICRDAT(int pos, int value) {
	_ICRDAT[pos] = value;
}

/**
Sets the record number of the first segment in the forecast group.
@param IREC the record number of the first segment in the forecast group.
*/
public void setIREC(int IREC) {
	_IREC = IREC;
}

/**
Sets the special forecast group indicator.
@param ISPEC the special forecast group indicator.
*/
public void setISPEC(int ISPEC) {
	_ISPEC = ISPEC;
}

/**
Sets the maximum number of forecast groups allowed in the binary database.
@param MFGREC the maximum number of forecast groups allowed in the binary 
database.
*/
public void setMFGREC(int MFGREC) {
	_MFGREC = MFGREC;
}

/**
Sets the minimum time step this forecast group can run.
@param MINDT the minimum time step this forecast group can run.
*/
public void setMINDT(int MINDT) {
	_MINDT = MINDT;
}

/**
Sets the number of forecast groups defined in the binary database.
@param NFGREC the number of forecast groups defined in the binary database.
*/
public void setNFGREC(int NFGREC) {
	_NFGREC = NFGREC;
}

/**
Sets the number of segments in the forecast group.
@param NSEG the number of segments in the forecast group.
*/
public void setNSEG(int NSEG) {
	_NSEG = NSEG;
}

/**
Return a String representation of the Forecast Group (the ID).
*/
public String toString () {	
	return __fgid;
}

}
