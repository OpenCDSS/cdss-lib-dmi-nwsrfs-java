//------------------------------------------------------------------------------
// NWSRFS_PPPINDEX - class to contain the parametric preprocessor 
// index/record pointers
//------------------------------------------------------------------------------
// History:
//
// 2004-08-17	Scott Townsend, RTi	Initial version.
// 2004-08-19	J. Thomas Sapienza, RTi	* Revised for RTi standards.
//					* Added get*() and set*() methods.
// 2004-08-23	JTS, RTi		Added finalize().
//------------------------------------------------------------------------------
// Endheader

package RTi.DMI.NWSRFS_DMI;

import java.util.List;
import java.util.Vector;

/**
The NWSRFS_PPPINDEX - class to contain the preprocessor parametric record 
pointers and is used to increase performance in reading the file PPPPARMn 
by retrieving the record number for a specific parameter used in that file. 
This class reads and stores data from the PPPINDEX preprocessor parametric 
database file; it stores the entire contents of PPPINDEX in this object. The 
PPPINDEX database file has the following definition:
<pre>
 IX.4.3B-PPPINDEX  PREPROCESSOR PARAMETRIC DATA BASE FILE PPPINDEX

Purpose

File PPPINDEX contains the directory of parameter types and the parameter record
index for the Preprocessor Parametric Data Base.


Description

ATTRIBUTES: fixed length 16 byte binary records

RECORD STRUCTURE:

						Word
	Variable	Type	Dimension	Position	Description
The first two records are the parameter index control records.
Record 1:
	MAXREC		I*4	1		1		Maximum records 
								in file
	MAXTYP		I*4	1		2		Maximum 
								parameter types
	NUMTYP		I*4	1		3		Number of 
								parameter types 
								defined
	NUMFIL		I*4	1		4		Number of 
								parameters 
								files 

Record 2:
	FSTIDX		I*4	1		1		Record number of
								first index 
								record
	USERID		A8	1		2-3		User name
						4		Unused
The next MAXTYP records contain the following information for each 
parameter type:
	PARMTP		A4	1		1		Parameter type
	LUFILE		I*4	1		2		Logical unit 
								assigned to file
								that contains 
								the parameter 
								type
	FIRST		I*4	1		3		Record number of
								first parameter 
								record of this 
								type 1/
	LAST		I*4	1		4		Record number of
								last parameter 
								record of this 
								type
	NUMPRM		I*4	1		5		Number of 
								parameter 
								records of this 
								type
	ISNGL		I*4	1		6		Single record 
								type indicator: 
								1/
									0 = not 
									single 
									record 
									type
									1 = 
									single 
									record 
									type
						7-8		Unused
The remaining records are the parameter index records.  There is one index 
record for each parameter record. 2/
	ID		A8	1		1-2		Identifier 3/
	ITYPE		A4	1		3		Parameter type 
								3/
	IREC		I	1		4		Record number of
								parameter record

Notes:

	1/ A single record parameter type is one that has only one record 
	defined.  
	For these types the variable FIRST provides enough information for 
	access without 
	going to the index.

	2/ These records are used for direct access to a specific parameter 
	record.  
	Entries are made and retrieved using a hashing algorithm which is 
	applied to 
	the identifier and the parameter type.  The resulting number is 
	the index 
	record number of the parameter record.  The index contains the 
	record number 
	of the parameter record in the data file.

	3/ If ID(1:4)=-1 and ID(5:8)=0 and ITYPE=0 then the index record has 
	been deleted.

					IX.4.3B-PPPINDEX
</pre>
*/

public class NWSRFS_PPPINDEX {

/**
Record number of first index record
*/
protected int _FSTIDX; 

/**
The maximum number of records defined.
*/
protected int _MAXREC; 

/**
Maximum parameter types
*/
protected int _MAXTYP; 

/**
Number of parameters files
*/
protected int _NUMFIL; 

/**
Number of parameter types defined
*/
protected int _NUMTYP; 

/**
User Id
*/
protected String _USERID; 

/**
Record number of first parameter 
record of this type.
*/
protected List _FIRST;

/**
Holds the record number of the associated parameter identifier in the 
binary file PPPPARMn.
*/
protected List _IREC;

/**
Single record type indicator:
  0 =  not a single record type
  1 = single record type
*/
protected List _ISNGL;

/**
Record number of last parameter record of this type.
*/
protected List _LAST;

/**
Logical unit assigned to file that contains the parameter type.
*/
protected List _LUFILE;

/**
Number of parameter records of this type.
*/
protected List _NUMPRM;

/**
Holds all of the parameter identifiers.
*/
protected List _ID;

/**
Holds the parameter type of the associated parameter identifier in the 
binary file PPPINDEX.
*/
protected List _ITYPE;

/**
Holds the available parameter types.
*/
protected List _PARMTP;

/**
Constructor.
If the calling class uses this constructor then it will need to call the 
readFile method manually.  This constructor is needed to allow multiple calls 
through the same DMI object.
*/
public NWSRFS_PPPINDEX() {
	initialize();
}

/**
Adds a value to the _FIRST Vector.
@param i the int to add (added as an Integer).
*/
public void addFIRST(int i) {
	addFIRST(new Integer(i));
}

/**
Adds a value to the _FIRST Vector.
@param I the Integer to add.
*/
public void addFIRST(Integer I) {
	if (_FIRST == null) {
		_FIRST = new Vector();
	}
	_FIRST.add(I);
}

/**
Adds a value to the _IREC Vector.
@param i the int to add (added as an Integer).
*/
public void addIREC(int i) {
	addIREC(new Integer(i));
}

/**
Adds a value to the _IREC Vector.
@param I the Integer to add.
*/
public void addIREC(Integer I) {
	if (_IREC == null) {
		_IREC = new Vector();
	}
	_IREC.add(I);
}

/**
Adds a value to the _ISNGL Vector.
@param i the int to add (added as an Integer).
*/
public void addISNGL(int i) {
	addISNGL(new Integer(i));
}

/**
Adds a value to the _ISNGL Vector.
@param I the Integer to add.
*/
public void addISNGL(Integer I) {
	if (_ISNGL == null) {
		_ISNGL = new Vector();
	}
	_ISNGL.add(I);
}

/**
Adds a value to the _LAST Vector.
@param i the int to add (added as an Integer).
*/
public void addLAST(int i) {
	addLAST(new Integer(i));
}

/**
Adds a value to the _LAST Vector.
@param I the Integer to add.
*/
public void addLAST(Integer I) {
	if (_LAST == null) {
		_LAST = new Vector();
	}
	_LAST.add(I);
}

/**
Adds a value to the _LUFILE Vector.
@param i the int to add (added as an Integer).
*/
public void addLUFILE(int i) {
	addLUFILE(new Integer(i));
}

/**
Adds a value to the _LUFILE Vector.
@param I the Integer to add.
*/
public void addLUFILE(Integer I) {
	if (_LUFILE == null) {
		_LUFILE = new Vector();
	}
	_LUFILE.add(I);
}

/**
Adds a value to the _NUMPRM Vector.
@param i the int to add (added as an Integer).
*/
public void addNUMPRM(int i) {
	addNUMPRM(new Integer(i));
}

/**
Adds a value to the _NUMPRM Vector.
@param I the Integer to add.
*/
public void addNUMPRM(Integer I) {
	if (_NUMPRM == null) {
		_NUMPRM = new Vector();
	}
	_NUMPRM.add(I);
}

/**
Adds a value to the _ID Vector.
@param s the value to add.
*/
public void addID(String s) {
	if (_ID == null) {
		_ID = new Vector();
	}
	_ID.add(s);
}

/**
Adds a value to the _ITYPE Vector.
@param s the value to add.
*/
public void addITYPE(String s) {
	if (_ITYPE == null) {
		_ITYPE = new Vector();
	}
	_ITYPE.add(s);
}

/**
Adds a value to the _PARMTP Vector.
@param s the value to add.
*/
public void addPARMTP(String s) {
	if (_PARMTP == null) {
		_PARMTP = new Vector();
	}
	_PARMTP.add(s);
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize() {
	_USERID = null;
	_FIRST = null;
	_IREC = null;
	_ISNGL = null;
	_LAST = null;
	_LUFILE = null;
	_NUMPRM = null;
	_ID = null;
	_ITYPE = null;
	_PARMTP = null;
}

/**
Returns record number of first index record.
@return record number of first index record.
*/
public int getFSTIDX() {
	return _FSTIDX;
}

/**
Returns the maximum number of records defined.
@return the maximum number of records defined.
*/
public int getMAXREC() {
	return _MAXREC;
}

/**
Returns the maximum parameter types.
@return the maximum parameter types.
*/
public int getMAXTYP() {
	return _MAXTYP;
}

/**
Returns the number of parameters files.
@return the number of parameters files.
*/
public int getNUMFIL() {
	return _NUMFIL;
}

/**
Returns the number of parameter types defined.
@return the number of parameter types defined.
*/
public int getNUMTYP() {
	return _NUMTYP;
}

/**
Returns the user ID.
@return the user ID.
*/
public String getUSERID() {
	return _USERID;
}

/**
Returns the Vector of record numbers of the first parameters.
@return the Vector of record numbers of the first parameters.
*/
public List getFIRST() {
	return _FIRST;
}

/**
Returns the record number of the first parameters at an index.
@param firstIndex the index of the _FIRST data record to return
@return the record number of the first parameters at an index.
*/
public int getFIRST(int firstIndex) {
	return (int)((Integer)_FIRST.get(firstIndex)).intValue();
}

/**
Returns the List of record numbers of associated parameter identifiers.
@return the List of record numbers of associated parameter identifiers.
*/
public List getIREC() {
	return _IREC;
}

/**
Returns the record number of the associated parameter identifiers at an index.
@param irecIndex the index of the _IREC data record to return
@return the record number of the associated parameter identifiers at an index.
*/
public int getIREC(int irecIndex) {
	return (int)((Integer)_IREC.get(irecIndex)).intValue();
}

/**
Returns the List of single record type indicators.
@return the List of single record type indicators.
*/
public List getISNGL() {
	return _ISNGL;
}

/**
Returns the single record type indicator at an index.
@param isnglIndex the index of the _ISNGL data record to return
@return the single record type indicator at an index.
*/
public int getISNGL(int isnglIndex) {
	return (int)((Integer)_ISNGL.get(isnglIndex)).intValue();
}

/**
Returns the List of record numbers of last parameters.
@return the List of record numbers of last parameters.
*/
public List getLAST() {
	return _LAST;
}

/**
Returns the record number of the last parameter at an index.
@param lastIndex the index of the _LAST data record to return
@return the record number of the last parameter at an index.
*/
public int getLAST(int lastIndex) {
	return (int)((Integer)_LAST.get(lastIndex)).intValue();
}

/**
Returns the List of logical units assigned to files.
@return the List of logical units assigned to files.
*/
public List getLUFILE() {
	return _LUFILE;
}

/**
Returns the logical units assigned to files at an index.
@param lufileIndex the index of the _LUFILE data record to return
@return the logical units assigned to files at an index.
*/
public int getLUFILE(int lufileIndex) {
	return (int)((Integer)_LUFILE.get(lufileIndex)).intValue();
}

/**
Returns the List of the number of parameter records.
@return the List of the number of parameter records.
*/
public List getNUMPRM() {
	return _NUMPRM;
}

/**
Returns the number of parameter records at an index.
@param numprmIndex the index of the _NUMPRM data record to return
@return the number of parameter records at an index.
*/
public int getNUMPRM(int numprmIndex) {
	return (int)((Integer)_NUMPRM.get(numprmIndex)).intValue();
}

/**
Returns the List of the parameter identifiers.
@return the List of the parameter identifiers.
*/
public List getID() {
	return _ID;
}

/**
Returns the parameter identifier at an index.
@param idIndex the index of the _ID data record to return
@return the parameter identifier at an index.
*/
public String getID(int idIndex) {
	return (String)_ID.get(idIndex);
}

/**
Returns the List of parameter types of the associated parameter identifiers.
@return the List of parameter types of the associated parameter identifiers.
*/
public List getITYPE() {
	return _ITYPE;
}

/**
Returns the record number of the assocatied parameter identifiers at an index.
@param itypeIndex the index of the _ITYPE data record to return
@return the record number of the assocatied parameter identifiers at an index.
*/
public String getITYPE(int itypeIndex) {
	return (String)_ITYPE.get(itypeIndex);
}

/**
Returns the List of available parameter types.
@return the List of available parameter types.
*/
public List getPARMTP() {
	return _PARMTP;
}

/**
Returns the record number of the available parameter types at an index.
@param parmtpIndex the index of the _PARMTP data record to return
@return the record number of the available parameter types at an index.
*/
public String getPARMTP(int parmtpIndex) {
	return (String)_PARMTP.get(parmtpIndex);
}

/**
Initialize global objects.
*/
private void initialize() {
	_MAXREC = 0;
	_MAXTYP = 0;
	_NUMTYP = 0;
	_NUMFIL = 0;
	_FSTIDX = 0;
	_USERID = null;
	_PARMTP = null;
	_LUFILE = null;
	_FIRST = null;
	_LAST = null;
	_NUMPRM = null;
	_ISNGL = null;
	_ID = null;
	_ITYPE = null;
	_IREC = null;
}

/**
Sets record number of first index record.
@param FSTIDX record number of first index record.
*/
public void setFSTIDX(int FSTIDX) {
	_FSTIDX = FSTIDX;
}

/**
Sets the maximum number of records defined.
@param MAXREC the maximum number of records defined.
*/
public void setMAXREC(int MAXREC) {
	_MAXREC = MAXREC;
}

/**
Sets the maximum parameter types.
@param MAXTYP the maximum parameter types.
*/
public void setMAXTYP(int MAXTYP) {
	_MAXTYP = MAXTYP;
}

/**
Sets the number of parameters files.
@param NUMFIL the number of parameters files.
*/
public void setNUMFIL(int NUMFIL) {
	_NUMFIL = NUMFIL;
}

/**
Sets the number of parameter types defined.
@param NUMTYP the number of parameter types defined.
*/
public void setNUMTYP(int NUMTYP) {
	_NUMTYP = NUMTYP;
}

/**
Sets the user ID.
@param USERID the user ID.
*/
public void setUSERID(String USERID) {
	_USERID = USERID;
}

}
