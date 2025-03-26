// NWSRFS_PRDINDEX - class to contain the processed database index/record pointers

package RTi.DMI.NWSRFS_DMI;

import java.util.ArrayList;
import java.util.List;

/**
The NWSRFS_PRDINDEX - class to contain the processed database record 
pointers and is used to increase performance in reading the file PRDTSn 
by retrieving the record number for a specific parameter used in that file. 
This class reads and stores data from the PRDINDEX preprocessor parametric 
database file; it stores the entire contents of PRDINDEX in this object. The 
PRDINDEX database file has the following definition:
<pre>
IX.4.4B-PRDINDEX    PROCESSED DATA BASE FILE PRDINDEX

Purpose
File PRDINDEX contains the index to the time series in the Processed
Data Base.

A hashing algorithm is used to determine the location in the index
based on the time series identifier and data type. A hashing
algorithm is a technique which applies a function to a key to compute
an address. In this case the key is the time series identifier and
data type. The address will be the index array subscript. The goal
is to scatter the entries over the entire index and avoid more than
one key 'hashing' to the same address.


Description

ATTRIBUTES: fixed length 16 byte binary records

RECORD STRUCTURE:

                                  Word
  Variable Type     Dimension   Position   Description

  TSID        A8       1           1       Time series identifier

  DTYPE       A4       1           3       Data type code

  RECNO       I*4       1          4       Record number of first logical
                                           record in time series file:
                                             o unused record if all
                                               values are zero
                                             o deleted record if all
                                               values are -1



                  IX.4.4B-PRDINDEX-1
</pre>
*/

public class NWSRFS_PRDINDEX {

/**
The Time Series ID
*/
protected List<String> _TSID; 

/**
The Time Series Data Type.
*/
protected List<String> _TSDataType; 

/**
The Record Number in the PRDTSn file containing the Time Series data.
*/
protected List<Integer> _IREC; 

/**
Constructor.
If the calling class uses this constructor then it will need to call the readFile method manually.
This constructor is needed to allow multiple calls through the same DMI object.
*/
public NWSRFS_PRDINDEX() {
	initialize();
}

/**
Adds a value to the _IREC list.
@param i the int to add (added as an Integer).
*/
public void addIREC(int i) {
	addIREC(Integer.valueOf(i));
}

/**
Adds a value to the _IREC list.
@param I the Integer to add.
*/
public void addIREC(Integer I) {
	if (_IREC == null) {
		_IREC = new ArrayList<>();
	}
	_IREC.add(I);
}

/**
Adds a value to the _TSDataType list.
@param s the value to add.
*/
public void addTSDT(String s) {
	if (_TSDataType == null) {
		_TSDataType = new ArrayList<>();
	}
	_TSDataType.add(s);
}

/**
Adds a value to the _TSID list.
@param s the value to add.
*/
public void addTSID(String s) {
	if (_TSID == null) {
		_TSID = new ArrayList<>();
	}
	_TSID.add(s);
}

/**
Returns the list of record numbers.
@return the list of record numbers.
*/
public List<Integer> getIREC() {
	return _IREC;
}

/**
Return the Record Number at an index.
@param index Index of of the Record Number.
@return the Record Number at an index.
*/
public int getIREC(int index) {
	return _IREC.get(index).intValue();
}

/**
Returns the list of Data Types.
@return the list of Data Types.
*/
public List<String> getTSDT() {
	return _TSDataType;
}

/**
Return the Data Type at an index.
@param index Index of Data Type.
@return the Data Type at an index.
*/
public String getTSDT(int index) {
	return _TSDataType.get(index);
}

/**
Returns the list of Time Series Identifiers.
@return the list of Time Series Identifiers.
*/
public List<String> getTSID() {
	return _TSID;
}

/**
Return the Time Series ID at an index.
@param index Index of Time Series ID.
@return the Time Series ID at an index.
*/
public String getTSID(int index) {
	return _TSID.get(index);
}

/**
Initialize global objects.
*/
private void initialize() {
	_IREC = null;
	_TSDataType = null;
	_TSID = null;
}

}