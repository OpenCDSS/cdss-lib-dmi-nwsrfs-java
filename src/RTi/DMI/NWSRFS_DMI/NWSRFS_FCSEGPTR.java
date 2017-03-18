//-----------------------------------------------------------------------------
// NWSRFS_FCSEGPTR - class to contain the segment definition file record 
//	pointers
//-----------------------------------------------------------------------------
// History:
//
// 2004-03-12	Scott Townsend, RTi	Initial version.
// 2004-03-30	SAT, RTi		Revised to adhere to the RTi
//					coding standards.
// 2004-08-18	J. Thomas Sapienza, RTi * Fit to RTi code standards.
//					* Added get*() and set*() methods.
// 2004-08-23	JTS, RTi		Added finalize().
//-----------------------------------------------------------------------------
// Endheader

package RTi.DMI.NWSRFS_DMI;

import java.util.List;
import java.util.Vector;

/**
The NWSRFS_FCSEGPTR - class to contain the segment definition file 
record pointers and is used to increase performance in reading the 
file FCRSEGSTS by retrieving the record number for a specific Segment 
identifier used in that file. This class reads and stores data from 
the FCSEGPTR processed database file; it store the entire contents 
if FCSEGPTR in this object.  The FCSEGPTR database file has the 
following definition:
<pre>

FILE NAME:  FCSEGPTR


Purpose

File FCSEGPTR is a pointer file used to locate Segment information in
files FCPARAM and FCSEGSTS.
The contents of record 1 is held in common block FCSEGP.
The contents of record 2 is held in common block FCSGP2.
The contents of records 3 through the last are held in common block
FSGLST.


Description

ATTRIBUTES: fixed length 12 bytes binary records

RECORD STRUCTURE:

    Variable   Type    Dimension   Word Pos.   Description

    Record 1 contains the following information:

    NSEG       I*4         1            1      Number of Segments defined in
                                               files FCPARAM and FCSEGSTS

    NRECST     I*4         1            2      Number of records used in file
                                               FCSEGSTS

    MAXRST     I*4         1            3      Maximum number of records
                                               available in file FCSEGSTS

    Record 2 contains the following information:

    NRECP      I*4         1            1      Number of records used in file
                                               FCPARAM

    MAXRP      I*4         1            2      Maximum number of records
                                               available in file FCPARAM

    NWPRP      I*4         1            3      Number of words per record in
                                               file FCPARAM

    Records 3 through record NSEG+2 contain the following information:

    ISEG       A8          1            1      Segment identifier

    IREC       I*4         1            3      Record number in file FCSEGSTS
                                               for Segment ISEG

               IX.4.5B-FCSEGPTR
</pre>
*/
public class NWSRFS_FCSEGPTR {

/**
Max number of records allowed in FCPARAM.
*/
protected int _MAXRP;

/**
Max number of segments allowed in FCSEGSTS.
*/
protected int _MAXRST;

/**
Number of records in FCPARAM.
*/
protected int _NRECP; 

/**
Number of records in FCSEGSTS.
*/
protected int _NRECST;

/**
Number of segment identifiers defined in FCSEGSTS and FCPARAM.
*/
protected int _NSEG; 

/**
Number of words per record in FCPARAM.
*/
protected int _NWPRP;

/**
List holding record number associated with file FCSEGSTS.
*/
protected List<Integer> _IREC; 

/**
List holding segment identifier.
*/
protected List<String> _ISEG;

/**
Constructor.
*/
public NWSRFS_FCSEGPTR() {
	initialize();
}

/**
Adds a value to the IREC Vector.
@param i the int to add to the IREC Vector -- will be added as an Integer.
*/
public void addIREC(int i) {
	addIREC(new Integer(i));
}

/**
Adds a value to the IREC Vector.
@param I the Integer to add to the IREC Vector.
*/
public void addIREC(Integer I) {
	if (_IREC == null) {
		_IREC = new Vector<Integer>();
	}
	_IREC.add(I);
}

/**
Adds a value to the ISEG Vector.
@param s the value to add to the ISEG Vector.
*/
public void addISEG(String s) {
	if (_ISEG == null) {
		_ISEG = new Vector<String>();
	}
	_ISEG.add(s);
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize() 
throws Throwable {
	_IREC = null;
	_ISEG = null;
}

/**
Returns the max number of records allowed in FCPARAM.
@return the max number of records allowed in FCPARAM.
*/
public int getMAXRP() {
	return _MAXRP;
}

/**
Returns the max number of segments allowed in FCSEGSTS.
@return the max number of segments allowed in FCSEGSTS.
*/
public int getMAXRST() {
	return _MAXRST;
}

/**
Returns the number of records in FCPARAM.
@return the number of records in FCPARAM.
*/
public int getNRECP() {
	return _NRECP;
}

/**
Returns the number of records in FCSEGSTS.
@return the number of records in FCSEGSTS.
*/
public int getNRECST() {
	return _NRECST;
}

/**
Returns the number of segment identifiers defined in FCSEGSTS and FCPARAM.
@return the number of segment identifiers defined in FCSEGSTS and FCPARAM.
*/
public int getNSEG() {
	return _NSEG;
}

/**
Returns the number of words per record in FCPARAM.
@return the number of words per record in FCPARAM.
*/
public int getNWPRP() {
	return _NWPRP;
}

/**
Returns the Vector that holds record numbers associated with file FCSEGSTS.
@return the Vector that holds record numbers associated with file FCSEGSTS.
*/
public List<Integer> getIREC() {
	return _IREC;
}

/**
Returns the Vector that holds segment identifiers.
@return the Vector that holds segment identifiers.
*/
public List<String> getISEG() {
	return _ISEG;
}

/**
Initialize data members.
*/
private void initialize() {
	_NSEG  = 0; 
	_NRECST  = 0;
	_MAXRST = 0;
	_NRECP  = 0; 
	_NWPRP  = 0;
	_MAXRP = 0;
	_ISEG = null;
	_IREC = null; 
}

/**
Sets the max number of records allowed in FCPARAM.
@param MAXRP the max number of records allowed in FCPARAM.
*/
public void setMAXRP(int MAXRP) {
	_MAXRP = MAXRP;
}

/**
Set the max number of segments allowed in FCSEGSTS.
@param MAXRST the max number of segments allowed in FCSEGSTS.
*/
public void setMAXRST(int MAXRST) {
	_MAXRST = MAXRST;
}

/**
Set the number of records in FCPARAM.
@param NRECP the number of records in FCPARAM.
*/
public void setNRECP(int NRECP) {
	_NRECP = NRECP;
}

/**
Set the number of records in FCSEGSTS.
@param NRECST the number of records in FCSEGSTS.
*/
public void setNRECST(int NRECST) {
	_NRECST = NRECST;
}

/**
Set the number of segment identifiers defined in FCSEGSTS and FCPARAM.
@param NSEG the number of segment identifiers defined in FCSEGSTS and FCPARAM.
*/
public void setNSEG(int NSEG) {
	_NSEG = NSEG;
}

/**
Set the number of words per record in FCPARAM.
@param NWPRP the number of words per record in FCPARAM.
*/
public void setNWPRP(int NWPRP) {
	_NWPRP = NWPRP;
}

}
