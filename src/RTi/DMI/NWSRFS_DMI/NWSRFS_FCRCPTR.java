// NWSRFS_FCRCPTR - class to contain the rating curve file index/record pointers

package RTi.DMI.NWSRFS_DMI;

import java.util.List;
import java.util.Vector;

/**
The NWSRFS_FCRCPTR - class to contain the rating curve file record pointers 
and is used to increase performance in reading the file FCRATING by 
retrieving the record number for a specific rating curve used in that 
file. This class reads and stores data from the FCRCPTR forecast 
component database file; it stores the entire contents of FCRCPTR in 
this object.  The FCRCPTR database file has the following definition:
<pre>

FILE NAME:  FCRCPTR


Purpose

File FCRCPTR is a pointer file for locating Rating Curve definitions
in file FCRATING.
The variables in record 1 and the rating curve identifiers and record
locations are stored in common block /FRCPTR/.


Description

ATTRIBUTES: fixed length 12 byte binary records

RECORD STRUCTURE:

    Variable   Type  Dimension   Word Pos.   Description

    Record 1 contains file control information.

    NRC        I*4      1           1        Number of Rating Curves
                                             defined

    MRC        I*4      1           2        Not used

    MRCF       I*4      1           3        Maximum number of Rating
                                             Curves allowed in file

    Record 2 through NRC+1 contain the following:

    RCID       A8       1           1        Rating Curve identifier

    IREC       I*4      1           3        Record number in file FCRATING

               IX.4.5B-FCRCPTR
</pre>
*/
public class NWSRFS_FCRCPTR {

/**
A holding integer.  Not currently used.
*/
protected int _MRC;

/**
Maximun number of allowed rating curves in the binary file.
*/
protected int _MRCF; 

/**
The number of rating curves defined. This will the number of records in the 
binary file.
*/
protected int _NRC; 

/**
The list holding the record number of the associated rating curve 
identifier in the binary file FCRATING.
*/
protected List<Integer> _IREC;

/**
The list holding all of the rating curve identifiers.
*/
protected List<String> _RCID;

/**
Constructor.
If the calling class uses this constructor then it will need to call the 
readFile method manually.  This constructor is needed to allow multiple 
calls through the same DMI object.
*/
public NWSRFS_FCRCPTR() {
	initialize();
}

/**
Adds an int to the _IREC Vector.  It is converted to an Integer first.
@param i the int to add.
*/
public void addIREC(int i) {
	addIREC(Integer.valueOf(i));
}

/**
Adds an Integer to the _IREC Vector.
@param I the Integer to add.
*/
public void addIREC(Integer I) {
	if (_IREC == null) {
		_IREC = new Vector<Integer>();
	}
	_IREC.add(I);
}

/**
Adds an Integer to the _RCID Vector.
@param s the String to add.
*/
public void addRCID(String s) {
	if (_RCID == null) {
		_RCID = new Vector<String>();
	}
	_RCID.add(s);
}

/**
Returns the holding integer, which is not currently used.
@return the holding integer, which is not currently used.
*/
public int getMRC() {
	return _MRC;
}

/**
Returns the maximum number of allowed rating curves in the binary file.
@return the maximum number of allowed rating curves in the binary file.
*/
public int getMRCF() {
	return _MRCF;
}

/**
Returns the number of rating curves defined.
@return the number of rating curves defined.
*/
public int getNRC() {
	return _NRC;
}

/**
Returns the list holding the record number of the associated rating curve
identifier in the binary file FCRATING.
@return the list holding the record number of the associated rating curve
identifier in the binary file FCRATING.
*/
public List<Integer> getIREC() {
	return _IREC;
}

/**
Returns the list holding all the rating curve identifiers.
@return the list holding all the rating curve identifiers.
*/
public List<String> getRCID() {
	return _RCID;
}

/**
Initialize data members.
*/
private void initialize() {
	_NRC  = 0;
	_MRC  = 0;
	_MRCF = 0;
	_RCID = null;
	_IREC = null;
}

/**
Sets the holding integer, which is not currently used.
@param MRC the holding integer, which is not currently used.
*/
public void setMRC(int MRC) {
	_MRC = MRC;
}

/**
Sets the maximum number of allowed rating curves in the binary file.
@param MRCF the maximum number of allowed rating curves in the binary file.
*/
public void setMRCF(int MRCF) {
	_MRCF = MRCF;
}

/**
Sets the number of rating curves defined.
@param NRC the number of rating curves defined.
*/
public void setNRC(int NRC) {
	_NRC = NRC;
}

}
