//-----------------------------------------------------------------------------
// NWSRFS_Carryover - class to store the organizational information about an 
//                    NWSRFS segment carryover. 
//-----------------------------------------------------------------------------
// History:
//
// 2004-04-26   Scott Townsend, RTi.  	Initial version.
// 2004-08-18	J. Thomas Sapienza, RTi	* Revised for RTi standards.
//					* Added get*() and set*() methods.
// 2004-08-23	JTS, RTi		Added finalize().
//-----------------------------------------------------------------------------
// Endheader

package RTi.DMI.NWSRFS_DMI;

import java.util.Vector;

import RTi.DMI.NWSRFS_DMI.NWSRFS_Segment;

/**
The NWSRFS_Carryover class stores the organizational information about an
NWSRFS Segment's Carryover. This class reads and stores data from the 
FCCARRY processed database files. The FCCARRY has the following definition:
<pre>

FILE NAME:  FCCARRY

Purpose

File FCCARRY contains the carryover values for the Forecast
Component.

The index files FCCOGDEF and FCSEGSTS are needed to locate
information in file FCCARRY.

The carryover values for a Segment is stored in the C array.


Description

ATTRIBUTES: fixed length 400 byte binary records

RECORD STRUCTURE:

The records are logically organized into 'carryover slots'.  Each
slot begins on a record boundary and contains NRSLOT records.  Within
a carryover slot, carryover values for each Segment are stored with
no empty space between Segments.  Carryover values for any given
Segment may begin at any point within a record and may span multiple
records.  Therefore, each Segment will have a variable length
'conceptual record' unrelated to the physical layout of the file.
The beginning of the conceptual record for each Segment is found by
the word offset from the start of the carryover slot.  This word
offset can be found in the Segment description record for the
particular Segment in file FCSEGDEF.

  Variable    Type    Dimension   Word Pos.   Description

  The structure of each conceptual record is as follows:

  ISEG        I*4         2         1         Segment identifier - blank if
                                              an obsolete definition

  ICDAY       I*4         1         3         Julian day of carryover values
                                              - a value less than or equal
                                              to zero indicates that initial
                                              undated values are in the
                                              carryover array  1/

  ICHR         I*4        1         4         Internal clock hour of
                                              carryover values

  NC           I*4        1         5         Length of carryover array (C)

  LUPTIM       I*4        5         6         Clock time of last update of
                                              this carryover:
                                                 LUPTIM(1) = month
                                                 LUPTIM(2) = day
                                                 LUPTIM(3) = year (4 digits)
                                                 LUPTIM(4) = hour and minute
                                                             (military)
                                                 LUPTIM(5) = second and 
						             milliseconds

   C            R*4      NC        11         Carryover array


NOTES:

1/ Day 1 is January 1, 1900.

                IX.4.5B-FCCARRY
</pre>

The NWSRFS C array has the following definition:

<pre>

VIII.2-ARRAY_C  FORECAST COMPONENT INTERNAL ARRAY C 
 
Function 
The C array contains the carryover data for each of the Operations
that have carryover for Forecast Component Segment. 

A copy of the C array for each date of carryover is stored in the
Forecast Component Data Base. 
 
 
Listing 
DIMENSION C(MC) 
 
where MC is the maximum length of array C 
 
 
Contents 
The C array contains the following information for each Operation with
carryover: 
 
         Position  Contents 
 
         1         Operation number: 
 
                      -1 = last entry in the C array (STOP Operation) 
 
         2         Pointer indicating where in the C array the information
                   for the next Operation with carryover begins 
 
         3-4       8-character user specified name of the Operation 
 
         5         Pointer indicating where the actual parameter
                   information for this Operation is located in the
                   P array (the starting location of the second part of
                   the portion of the P array assigned to the Operation) 
 
         6-?       Carryover values for the Operation - these values are
                   initially entered by the input (PIN) subroutine and can
                   be modified in the execution (EX) and carryover
                   transfer (COX) routines (this is sometimes referred to
                   as the second part of the portion of the C array
                   assigned to the Operation - it is referred to as the CO
                   array in Chapter VIII.4)

                   VIII.2-ARRAY_C
</pre>
*/
public class NWSRFS_Carryover {

/**
Holds the last update times of the Carryover.
*/
protected int[] _LUPTIM;

/**
Points to the location in the P array where Operation parameters for 
the particular Carryover resides.
*/
protected int _coOperationPointer;

/**
Holds the Julian day of Carryover values.
*/
protected int _ICDAY;

/**
Holds the internal clock hour for Carryover values.
*/
protected int _ICHR;

/**
Holds the length of the Carryover array <i>C</I>.
*/
protected int _NC;

/**
Holds the Operation number associated with this carryover object.
*/
protected int _opNumber;

/**
The carryover slot number. There can be 20 carryover slots for each segment. 
This means that for each operation in a segment carryover can be saved 20 times
and is the sequence number of these slots lowest to highest. The low numbers are
more recent carryover.
*/
protected int _slotNumber;

/**
Parent to this Carryover.
*/
private NWSRFS_Segment __segment;

/**
The Segment ID for this list of Operations.   Should be the same as 
__segment.IDSEG.
*/
protected String _ISEG;

/**
Holds the Operation name that is associated with the Operation number.
*/
protected String _opName;

/**
Holds the Carryover values for the Operation (often called the CO array).
*/
protected Vector _coCarryoverValues;

/**
Constructor.
@param segment the parent segment for the object.
@throws NullPointerException if the parent segment is null.
*/
public NWSRFS_Carryover(NWSRFS_Segment segment)
throws Exception {
	String routine = "NWSRFS_Carryover";

	if (segment != null) {
		__segment = segment;
	}
	else {
		throw new NullPointerException(routine 
			+ ": The parent Segment was null. Can not "
			+ "initialize the class.");
	}
	
	initialize();
}
	
/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize()
throws Throwable {
	_LUPTIM = null;
	__segment = null;
	_ISEG = null;
	_opName = null;
	_coCarryoverValues = null;
}

/**
Returns the carryover values for the operation.
@return the carryover values for the operation.
*/
public Vector getCoCarryoverValues() {
	return _coCarryoverValues;
}

/**
Returns the location in the P array where operation parameters for this 
carryover are located.
@return the location in the P array where operation parameters for this 
carryover are located.
*/
public int getCoOperationPointer() {
	return _coOperationPointer;
}

/**
Returns the julian day of carryover values.
@return the julian day of carryover values.
*/
public int getICDAY() {
	return _ICDAY;
}

/**
Returns the internal clock hour for carryover values.
@return the internal clock hour for carryover values.
*/
public int getICHR() {
	return _ICHR;
}

/**
Returns the segment id for this list of operations.
@return the segment id for this list of operations.
*/
public String getISEG() {
	return _ISEG;
}

/**
Returns the last update times of the carryover.
@return the last update times of the carryover.
*/
public int[] getLUPTIM() {
	return _LUPTIM;
}

/**
Returns the length of the carryover array.
@return the length of the carryover array.
*/
public int getNC() {
	return _NC;
}

/**
Returns the operation name associated with the operation number.
@return the operation name associated with the operation number.
*/
public String getOpName() {
	return _opName;
}

/**
Returns the operation number associated with this carryover object.
@return the operation number associated with this carryover object.
*/
public int getOpNumber() {
	return _opNumber;
}

/**
Returns the Parent NWSRFS_Segment object for this Carryover.
@return the NWSRFS_Segment parent.
*/
public NWSRFS_Segment getSegment() {
	return __segment;
}

/**
Returns the carryover slot number.
@return the carryover slot number.
*/
public int getSlotNumber() {
	return _slotNumber;
}

/**
Initialize member data.
*/
private void initialize() {
	_ISEG = null;
	_ICDAY = -1;
	_ICHR = -1;
	_NC = -1;
	_LUPTIM = new int[5];
	_opNumber = -1;
	_opName = null;
	_coOperationPointer = -1;
	_coCarryoverValues = new Vector();
}

/**
Sets the carryover values for the operation.
@param coCarryoverValues the carryover values for the operation.
*/
public void setCoCarryoverValues(Vector coCarryoverValues) {
	_coCarryoverValues = coCarryoverValues;
}

/**
Sets the location in the P array where operation parameters for this 
carryover are located.
@param coOperationPointer the location in the P array where operation 
parameters for this carryover are located.
*/
public void setCoOperationPointer(int coOperationPointer) {
	_coOperationPointer = coOperationPointer;
}

/**
Sets the julian day of carryover values.
@param ICDAY the julian day of carryover values.
*/
public void setICDAY(int ICDAY) {
	_ICDAY = ICDAY;
}

/**
Sets the internal clock hour for carryover values.
@param ICHR the internal clock hour for carryover values.
*/
public void setICHR(int ICHR) {
	_ICHR = ICHR;
}

/**
Sets the segment id for this list of operations.
@param ISEG the segment id for this list of operations.
*/
public void setISEG(String ISEG) {
	_ISEG = ISEG;
}

/**
Sets the last update times of the carryover.
@param num the position in the _LUPTIM array to set.
@param LUPTIM the last update times of the carryover.
*/
public void setLUPTIM(int num, int LUPTIM) {
	_LUPTIM[num] = LUPTIM;
}

/**
Sets the length of the carryover array.
@param NC the length of the carryover array.
*/
public void setNC(int NC) {
	_NC = NC;
}

/**
Sets the operation name associated with the operation number.
@param opName the operation name associated with the operation number.
*/
public void setOpName(String opName) {
	_opName = opName;
}

/**
Sets the operation number associated with this carryover object.
@param opNumber the operation number associated with this carryover object.
*/
public void setOpNumber(int opNumber) {
	_opNumber = opNumber;
}

/**
Sets the Parent NWSRFS_Segment object for this Carryover.
@param segment the NWSRFS_Segment parent.
*/
public void setSegment(NWSRFS_Segment segment) {
	__segment = segment;
}

/**
Sets the carryover slot number.
@param slotNumber the carryover slot number.
*/
public void setSlotNumber(int slotNumber) {
	_slotNumber = slotNumber;
}

}
