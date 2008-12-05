//-----------------------------------------------------------------------------
// NWSRFS_TimeSeries 
//-----------------------------------------------------------------------------
// History:
//
// 2004-02-26   Shawn Chen            Initial version.
// 2004-04-14   Shawn Chen            Updated to produce javadoc
// 2004-04-27	Scott Townsend, RTi   Finished to read in timeseries from 
//                                    processed database.
// 2004-07-12   Anne Morgan Love, RTi   Removed setVerbose(), which
//                                      added additional identifier strings in
//                                      front of the String returned from
//                                      .toString() if was true (for example:
//                                      "TS: " for time series).
// 2004-08-18	J. Thomas Sapienza, RTi	* Revised for RTi standards.
//					* Added set*() and get*() methods.
// 2004-08-23	JTS, RTi		Added finalize().
// 2004-08-26	SAT, RTi		Cleaned up the TSID variables and add
//					isDataFilled variable and methods.
//-----------------------------------------------------------------------------
// Endheader

package RTi.DMI.NWSRFS_DMI;

import java.util.List;

import RTi.TS.TS;
import RTi.TS.HourTS;

/**
The NWSRFS_TimeSeries class stores the organizational information about an
NWSRFS TimeSeries. This class stores data from the PRDINDEX, PRDPARM, and 
PRDTS<I>n</I> processed database files and is a repository for data 
retrieved from the NWSRFS timeseries binary files. It differs from the 
standard RTi TS classes in that there is more data available to 
NWSRFS-specific applications that implement this class. It is also 
possible to construct an RTi standard TS class from the timeseries
stored in this class. The PRDINDEX has the following definition:
<pre>

FILE NAME:  PRDINDEX

Purpose

File PRDINDEX contains the index to the time series in the Processed
Data Base.

A hashing algorithm is used to determine the location in the index
based on the time series identifier and data type.  A hashing
algorithm is a technique which applies a function to a key to compute
an address.  In this case, the key is the time series identifier and
data type.  The address will be the index array subscript.  The goal
is to scatter the entries over the entire index and avoid more than
one key 'hashing' to the same address.


Description

ATTRIBUTES: fixed length 16 byte binary records

RECORD STRUCTURE:

  Variable  Type   Dimension   Word Pos.   Description

  TSID        A8       1         1         Time series identifier

  DTYPE       A4       1         3         Data type code

  RECNO       I*4      1         4         Record number of first logical
                                           record in time series file:
                                             o unused record if all
                                               values are zero
                                             o deleted record if all
                                               values are -1

              IX.4.4B-PRDINDEX
</pre>

The PRDPARM has the following definition (PLEASE NOTE!! That the record 
length is incorrect in this documentation. In reality the first record 
is 240 bytes all others are 60 byte records.):
<pre>

FILE NAME:  PRDPARM

Purpose

File PRDPARM contains control information and the Data Type Index for
the Processed Data Base.


Description

ATTRIBUTES: fixed length 240 byte records

RECORD STRUCTURE:

  Variable  Type    Dimension  Word Pos.   Description

  The first record contains the following control information:

  NAMERF        A8       1         1       User identifier

  MAXDTP        I*4      1         3       Maximum number of data types

  MAXTMS        I*4      1         4       Maximum number of time series

  MINDAY        I*4      1         5       Minimum number of days of
                                           observed data to be kept

                         7         6       Unused

  NUMTMS        I*4      1        13       Actual number of time series

  NUMDTP        I*4      1        14       Actual number of data types

  The following records contain the Data Type Index:

  DTYPE          A4       1         1      Data type code

  IUNIT         I*4       1         2      Unit number

  NCORE         I*4       1         3      Number of time series of this
                                           data type to keep in core

  MAXDAY        I*4       1         4      Maximum days data

  MINDT         I*4       1         5      Smallest time interval allowed

  IPROC         I*4       1         6      Processing indicator - used if
                                           the requested data time
                                           interval is different from the
                                           time series data time
                                           interval:
                                             1 = pick off values (INST)
                                             2 = sum (ACCM)
                                             3 = average (MEAN)

  IFDAT         I*4       1         7      Future data indicator:
                                             0 = no future
                                            <0 = this is a regular time
                                                  series and value is
                                                  pointer to future
                                                  subscript in table
                                            >0 = this is future time
                                                  series and absolute
                                                  value is pointer to
                                                  regular subscript in
                                                  table

  IFRECD        I*4       1         8      First record in file of this type

  ILRECD        I*4       1         9      Last record in file of this type

  ICPTR         I*4       1        10      Pointer to incore data table

  ICALL         I*4       1        11      Indicator for write access:
                                              0 = Preprocessor only
                                              1 = Forecast Component only

  IDIM          I*4       1        12      Units dimension

  NVAL          I*4       1        13      Number of values per time
                                           interval:
                                             -1 = variable

  NXHDR         I*4       1        14      Number of extra words in header

  NUMTS         I*4       1        15      Number of time series defined
                                           for data type

              IX.4.4B-PRDPARM
</pre>

The PRDTS<I>n</I> has the following definition:
<pre>

FILE NAME:  PRDTSn

Purpose

Files PRDTSn contain the Processed Data Base time series data.


Description

ATTRIBUTES: fixed length 64 byte binary records

RECORD STRUCTURE:

  Variable  Type    Dimension    Word Pos.    Description

  The first record contains file control information:

  LUNIT      I*4       1            1         File unit number

  MAXREC     I*4       1            2         Maximum number of records

  NEXTRC     I*4       1            3         Next available record

  NDATYP     I*4       1            4         Number of data types in file

  LSTREC     I*4       1            5         Record number of last record
                                              read (used during execution
                                              only)

  The remaining records are the time series records. 1/

                                 Word Pos. 2/
  Variable   Type   Dimension    (cmp)  (exp)   Description

  The time series header records contain the following information:

  LTSHDR      I*1     1           1        1    Length of header in words -
                                                set to zero if the length is
                                                more than 256 when compacted

  IDTINT      I*1     1           1        2    Data time interval

  NVLINT      I*1     1           1        3    Number of values per data
                                                time interval

                                                (byte 4 not used)

  NTSMAX      I*2     1           2        4    Maximum number of data
                                                values 3/

  NTSNUM      I*2     1           2        5    Actual number of data values

  IPTREG      I*2     1           3        6    Location in the record of
                                                first regular data value

  IPTFUT      I*2     1           3        7    Location in the record of
                                                first future data value:
                                                  0 = no future data

  TSID        A8      1          4-5      8-9   Time series identifier

  TSDTYP      A4      1           6        10   Data type code

  TSUNIT      A4      1           7        11   Data units code

  TSLOC       R*4     2          8-9     12-13  Latitude and longitude
                                                (degrees and tenths)

  JULBEG      I*4     1          10        14   Julian hour of first data
                                                value

  ITSFUT      I*4     1          11        15   If the code for the
                                                component that can write the
                                                data type is 'PP', then this
                                                is the record number of the
                                                future time series data.
                                                If the code for the
                                                component that can write the
                                                data type is 'FC', then this
                                                is the QPF flag with the
                                                following characteristics:
                                                   0 = QPF not used
                                                  -1 = QPF used for the
                                                       entire forecast run
                                                   1-120 = number of hours of
                                                       QPF used in
                                                       forecast run

                                 12        16   Unused

  NRECNX       I*4     1         13        17   Record number of next time
                                                series record of the same
                                                data type

  TSDESC       A20     1       14-18     18-22  Description

  XBUF          ?      ?         19        23   Extra Buffer 4/

  The time series data records contain the following information:

  TS            R*4   TSMAX      ?         ?    Time series data


NOTES:

1/ The number of records used for a time series can be computed as
   follows:

     NREC=(NWORDS+LRECLT-1)/LRECLT

     where NREC is the number of records
           NWORDS is the number of words
           LRECLT is the number of words per record

  The number of words can be computed as follows:

     NWORDS=LTSHDR+LXBUF+NTSMAX

     where NWORDS is the number of records
           LTSHDR is the number of words in the time series header
           LXBUF  is the number of words in XBUF
           NTSMAX is the maximum number of time series values 3/

2/ 'cmp' is the word position as it is stored in the file in
   compacted form.

   'exp' is the word position as it is returned in expanded form from
    routines RPRDH and RPRDFH (see Section IX.3.5B).

3/  The maximum number of data values can be computed as follows:

     NTSMAX=MAXDAY*24/IDTINT*NVLINT

     where NTSMAX is the maximum number of data values
           MAXDAY is the maximum number of days of data for the data type
           IDTINT is the data time interval
           NVLINT is the number of values per data time interval

4/ The Extra Buffer is an optional array and is defined if:

     LENHDR-LENHED is greater than zero

     where LENHDR is the length of the header in words
           LENHED is the minimum length of a header in words (stored
                  in common block PDATAS)

              IX.4.4B-PRDTSn
</pre>
*/
public class NWSRFS_TimeSeries
{

/*
REVISIT (JTS - 2004-08-18)
- add finalize()
*/

/**
The boolean value for TimeSeries indicating whether the TS
contains data.
*/
protected boolean _tsDataIndicator;

/**
Time Series Header - Data time interval;
*/
protected byte _IDTINT;

/**
Time Series Header - Length of header in words.
*/
protected byte _LTSHDR;

/**
Time Series Header - Number of values per data time interval.
*/
protected byte _NVLINT;

/**
Time Series Header - Time Series Latitude.
*/
protected float _TSLAT;

/**
Time Series Header - Time Series Longitude
*/
protected float _TSLONG;

/**
Indicator for write access.
*/
protected int _ICALL;

/**
Pointer to incore data table.
*/
protected int _ICPTR;

/**
Units dimension.
*/
protected int _IDIM;

/**
Future data indicator.
*/
protected int _IFDAT;

/**
First record in file of this data type.
*/
protected int _IFRECD;

/**
Last record in file of this data type.
*/
protected int _ILRECD;

/**
Processing indicator (see notes above!)
*/
protected int _IPROC;

/**
Time Series Header - Code for component write.
*/
protected int _ITSFUT;

/**
Unit number. This will be used to determine the filename for actual TimeSeries
data retrieval.
*/
protected int _IUNIT;

/**
Time Series Header - Julian hour of first data value.
*/
protected int _JULBEG;

/**
Maximum number of days of data.
*/
protected int _MAXDAY;

/**
Maximum number of data types.
*/
protected int _MAXDTP;

/**
Maximum number of Time Series.
*/
protected int _MAXTMS;

/**
Minimum number of days of observed data kept.
*/
protected int _MINDAY;

/**
Smallest time interval allowed.
*/
protected int _MINDT;

/**
Number of Time Series of the data type kept in core.
*/
protected int _NCORE;

/**
Time Series Header - Record number of next time series.
*/
protected int _NRECNX;

/**
Actual number of Time Series.
*/
protected int _NUMTMS;

/**
Actual number of data types.
*/
protected int _NUMDTP;

/**
Number of Time Series defined for data type.
*/
protected int _NUMTS;

/**
Number of values per time interval;
*/
protected int _NVAL;

/**
Number of extra words in header.
*/
protected int _NXHDR;

/**
This is the index value of the PRDTSn file.
*/
protected int _prdIndex;

/**
The Integer for TimeSeries DateTime interval in hours.
*/
protected int _tsDTInterval;

/**
The Integer for TimeSeries number of values of
external location information.
*/
protected int _tsExtNVAL;

/**
The Integer for the NWSRFS type of TimeSeries.
*/
protected int _tsIndicator;

/**
The Integer for TimeSeries number of values
of additional information.
*/
protected int _tsNADD;

/**
The Integer for TimeSeries number of values per time interval.
*/
protected int _tsNVAL;

/**
The Ineger for starting location of the Timeseries in the D array.
*/
protected int _tsTimeseriesPointer;

/**
The Integer for TimeSeries Output and Update types
indicating when the time series is written to the data files.
*/
protected int _tsWhenWriteIndicator;

/**
Parent to this TimeSeries
*/
private NWSRFS_Operation __operation;

/**
Time Series Header - Location in the record of the the
first future data value.
*/
protected short _IPTFUT;

/**
Time Series Header - Location in the record of the first
regular/ observed data value.
*/
protected short _IPTREG;

/**
Flag specifying whether data has been added to this class or not.
*/
protected boolean _isDataFilled;

/**
Time Series Header - Maximum number of data values.
*/
protected short _NTSMAX;

/**
Time Series Header - Actual number of data values.
*/
protected short _NTSNUM;

/**
The Segment ID for this list of TimeSeries. 
Should be the same as __operation.IDSEG.
*/
protected String _IDSEG;

/**
The user identifier.
*/
protected String _NAMERF;

/**
The String for TimeSeries data file type code.
*/
protected String _tsDataFileCode;

/**
The String for the TimeSeries DataType code.
*/
protected String _tsDataType;

/**
Time Series Header - Time Series description.
*/
protected String _TSDESC;

/**
The TimeSeries identifier for this object.
*/
protected String _TSID;

/**
Time Series Header - Data units code
*/
protected String _TSUNIT;

/**
The future or QPF/QTF TimeSeries TS object.
*/
protected TS _futureTS;

/**
The regular/observed TimeSeries TS object.
*/
protected TS _observedTS;

/**
The String for TimeSeries additional information.
*/
protected List _tsAddInformation;

/**
The String for external location information
for TimeSeries and depends on type data file used.
*/
protected List _tsExtLocInformation;

/**
Construct an NWSRFS_TimeSeries instance using TimeSeries identifier and data 
type.  
@param tsID a String value representing the TimeSeries identifier which is 
necessary to generate the data for this object.
@param tsDataType a String value representing the TimeSeries data type which 
is necessary to generate the data for this object.
@param tsDTInterval an int value representing the TimeSeries data time interval which 
is necessary to generate the data for this object.
*/
public NWSRFS_TimeSeries(String tsID, String tsDataType, int tsDTInterval)  {
	initialize();

	// Set the global values needed for this TimeSeries.	
	if (tsID != null) {
		_TSID = tsID;
	}

	if (tsDataType != null) {
		_tsDataType = tsDataType;
	}

	if (tsDTInterval > 0) {
		_tsDTInterval = tsDTInterval;
	}
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize()
throws Throwable {
	_isDataFilled = false;
	__operation = null;
	_IDSEG = null;
	_NAMERF = null;
	_tsDataFileCode = null;
	_tsDataType = null;
	_TSDESC = null;
	_TSID = null;
	_TSUNIT = null;
	_futureTS = null;
	_observedTS = null;
	_tsAddInformation = null;
	_tsExtLocInformation = null;
}

/**
Returns the future or QPF/QTF time series.
@return the future or QPF/QTF time series.
*/
public TS getFutureTS() {
	return _futureTS;
}

/**
Returns the indicator for write access.
@return the indicator for write access.
*/
public int getICALL() {
	return _ICALL;
}

/**
Returns the pointer to the incore data table.
@return the pointer to the incore data table.
*/
public int getICPTR() {
	return _ICPTR;
}

/**
Returns the units dimension.
@return the units dimension.
*/
public int getIDIM() {
	return _IDIM;
}

/**
Returns the segment ID for this time series.  
@return the segment ID for this time series.
*/
public String getIDSEG() {
	return _IDSEG;
}

/**
Returns the data time interval from the time series header.
@return the data time interval from the time series header.
*/
public byte getIDTINT() {
	return _IDTINT;
}

/**
Returns the future data indicator.
@return the future data indicator.
*/
public int getIFDAT() {
	return _IFDAT;
}

/**
Returns the first record in the file of this data type.
@return the first record in the file of this data type.
*/
public int getIFRECD() {
	return _IFRECD;
}

/**
Returns the last record in the file of this data type.
@return the last record in the file of this data type.
*/
public int getILRECD() {
	return _ILRECD;
}

/**
Returns the processing indicator (see the notes for the class for more info).
@return the processing indicator (see the notes for the class for more info).
*/
public int getIPROC() {
	return _IPROC;
}

/**
Returns the location in the record of the first future data value from the 
time series header.
@return the location in the record of the first future data value from the 
time series header.
*/
public short getIPTFUT() {
	return _IPTFUT;
}

/**
Returns the location in the record of the first regular/observed data value
from the time series header.
@return the location in the record of the first regular/observed data value
from the time series header.
*/
public short getIPTREG() {
	return _IPTREG;
}

/**
Returns the flag that specifies whether data has been added 
to this class or not.
@return a boolean specifying whether data has been added to the 
class or not.
*/
public boolean getIsDataFilled() {
	return _isDataFilled;
}

/**
Returns the code for component write from the time series header.
@return the code for component write from the time series header.
*/
public int getITSFUT() {
	return _ITSFUT;
}

/**
Returns the unit number.  This is used to determine the filename for 
time series data retrieval.
@return the unit number.  
*/
public int getIUNIT() {
	return _IUNIT;
}

/**
Returns the Julian hour of the first data value from the time series header.
@return the Julian hour of the first data value from the time series header.
*/
public int getJULBEG() {
	return _JULBEG;
}

/**
Returns the length of the time series header in words.
@return the length of the time series header in words.
*/
public byte getLTSHDR() {
	return _LTSHDR;
}

/**
Returns the maximum number of days of data.
@return the maximum number of days of data.
*/
public int getMAXDAY() {
	return _MAXDAY;
}

/**
Returns the maximum number of data types.
@return the maximum number of data types.
*/
public int getMAXDTP() {
	return _MAXDTP;
}

/**
Returns the maximum number of time series.
@return the maximum number of time series.
*/
public int getMAXTMS() {
	return _MAXTMS;
}

/**
Returns the minimum number of days of observed data kept.
@return the minimum number of days of observed data kept.
*/
public int getMINDAY() {
	return _MINDAY;
}

/**
Returns the smallest time interval allowed.
@return the smallest time interval allowed.
*/
public int getMINDT() {
	return _MINDT;
}

/**
Returns the user identifier.
@return the user identifier.
*/
public String getNAMERF() {
	return _NAMERF;
}

/**
Returns the number of time series of the data type kept in the core.
@return the number of time series of the data type kept in the core.
*/
public int getNCORE() {
	return _NCORE;
}

/**
Returns the record number of the next time series from the time series header.
@return the record number of the next time series from the time series header.
*/
public int getNRECNX() {
	return _NRECNX;
}

/**
Returns the maximum number of data values from the time series header.
@return the maximum number of data values from the time series header.
*/
public short getNTSMAX() {
	return _NTSMAX;
}

/**
Returns the number of data values from the time series header.
@return the number of data values from the time series header.
*/
public short getNTSNUM() {
	return _NTSNUM;
}

/**
Returns the number of time series.
@return the number of time series.
*/
public int getNUMTMS() {
	return _NUMTMS;
}

/**
Returns the number of data types.
@return the number of data types.
*/
public int getNUMDTP() {
	return _NUMDTP;
}

/**
Returns the number of time series defined for the data type.
@return the number of time series defined for the data type.
*/
public int getNUMTS() {
	return _NUMTS;
}

/**
Returns the number of values per time interval.
@return the number of values per time interval.
*/
public int getNVAL() {
	return _NVAL;
}

/**
Returns the number of values per data time interval from the time series header.
@return the number of values per data time interval from the time series header.
*/
public byte getNVLINT() {
	return _NVLINT;
}

/**
Returns the number of extra words in the header.
@return the number of extra words in the header.
*/
public int getNXHDR() {
	return _NXHDR;
}

/**
Returns the regular/observed time series.
@return the regular/observed time series.
*/
public TS getObservedTS() {
	return _observedTS;
}

/**
Returns the parent NWSRFS_Operation object for this time series.
@return the parent NWSRFS_Operation object for this time series.
*/
public NWSRFS_Operation getOperation() {
	return __operation;
}

/**
Returns the prdIndex value for this time series.
@return the prdIndex value for this time series.
*/
public int getPrdIndex() {
	return _prdIndex;
}

/**
Returns the Segment identifier for this time series.
@return the segment identifier for this time series.
*/
public String getSegmentID() {
	return _IDSEG;
}

/**
Returns additional time series information.
@return additional time series information.
*/
public List getTSAddInformation() {
	return _tsAddInformation;
}

/**
Returns the time series data file type code.
@return the time series data file type code.
*/
public String getTSDataFileCode() {
	return _tsDataFileCode;
}

/**
Returns the time series data type.
@return the time series data type.
*/
public String getTSDataType() {
	return _tsDataType;
}

/**
Returns the time series description from the time series header.
@return the time series description from the time series header.
*/
public String getTSDESC() {
	return _TSDESC;
}

/**
Returns the number of hours in the time series date time interval.
@return the number of hours in the time series date time interval.
*/
public int getTSDTInterval() {
	return _tsDTInterval;
}

/**
Returns time series external location information.
@return time series external location information.
*/
public List getTSExtLocInformation() {
	return _tsExtLocInformation;
}

/**
Returns the number of values of external location information.
@return the number of values of external location information.
*/
public int getTSExtNVAL() {
	return _tsExtNVAL;
}

/**
Returns the time series identifier.
@return the time series identifier.
*/
public String getTSID() {
	return _TSID;
}

/**
Returns the int indicator for the NWSRFS type of time series.
@return the int indicator for the NWSRFS type of time series.
*/
public int getTSIndicator() {
	return _tsIndicator;
}

/**
Returns the time series latitude from the time series header.
@return the time series latitude from the time series header.
*/
public float getTSLAT() {
	return _TSLAT;
}

/**
Returns the time series longitude from the time series header.
@return the time series longitude from the time series header.
*/
public float getTSLONG() {
	return _TSLONG;
}

/**
Returns the number of values of additional time series information.
@return the number of values of additional time series information.
*/
public int getTSNADD() {
	return _tsNADD;
}

/**
Returns the number of values per time series time interval.
@return the number of values per time series time interval.
*/
public int getTSNVAL() {
	return _tsNVAL;
}

/**
Returns the starting location of the time series in the D array.
@return the starting location of the time series in the D array.
*/
public int getTSTimeseriesPointer() {
	return _tsTimeseriesPointer;
}

/**
Returns the time series data units code from the time series header.
@return the time series data units code from the time series header.
*/
public String getTSUNIT() {
	return _TSUNIT;
}

/**
Returns the value for time series output and update types that indicates when
the time series is written to the data files.
@return the value for time series output and update types that indicates when
the time series is written to the data files.
*/
public int getTSWhenWriteIndicator() {
	return _tsWhenWriteIndicator;
}

/**
Returns whether the time series has data.
@return true if the time series has data, false if not.
*/
public boolean hasData() {
	return _tsDataIndicator;
}

/**
Initialize member variables.
*/
private void initialize() {
	_isDataFilled = false;
	__operation = null;
	_prdIndex = 13; // The first of the PRDTS TS data files
	_tsIndicator = 0;
	_tsDataType = null;
	_tsDTInterval = -1;
	_tsNVAL = -1;
	_tsTimeseriesPointer = -1;
	_tsDataIndicator = false;
	_tsDataFileCode = null;
	_tsWhenWriteIndicator = -1;
	_tsExtNVAL = -1;
	_tsExtLocInformation = null;
	_tsNADD = -1;
	_tsAddInformation = null;
	_IDSEG = null;
	_TSID = null;
	_observedTS = new HourTS();
	_futureTS = new HourTS();
	_NAMERF = null;
	_MAXDTP = 0;
	_MAXTMS = 0;
	_MINDAY = 0;
	_NUMTMS = 0;
	_NUMDTP = 0;
	_IUNIT = 1;
	_NCORE = 0;
	_MAXDAY = 0;
	_MINDT = 0;
	_IPROC = 0;
	_IFDAT = 0;
	_IFRECD = 0;
	_ILRECD = 0;
	_ICPTR = 0;
	_ICALL = 0;
	_IDIM = 0;
	_NVAL = 0;
	_NXHDR = 0;
	_NUMTS = 0;
	_LTSHDR = 0x0;
	_IDTINT = 0x0;
	_NVLINT = 0x0;
	_NTSMAX = 0;
	_NTSNUM = 0;
	_IPTREG = 0;
	_IPTFUT = 0;
	_TSUNIT = null;
	_TSLAT = 0;
	_TSLONG = 0;
	_JULBEG = 0;
	_ITSFUT = 0;
	_NRECNX = 0;
	_TSDESC = null;
}

/**
Sets the future or QPF/QTF time series.
@param futureTS the future or QPF/QTF time series to set.
*/
public void setFutureTS(TS futureTS) {
	_futureTS = futureTS;
}

/**
Sets whether the time series has data.
@param hasData if true the time series has data, false it does not.
*/
public void setHasData(boolean hasData) {
	_tsDataIndicator = hasData;
}

/**
Sets the indicator for write access.
@param ICALL the indicator for write access.
*/
public void setICALL(int ICALL) {
	_ICALL = ICALL;
}

/**
Sets the pointer to the incore data table.
@param ICPTR the pointer to the incore data table.
*/
public void setICPTR(int ICPTR) {
	_ICPTR = ICPTR;
}

/**
Sets the units dimension.
@param IDIM the units dimension.
*/
public void setIDIM(int IDIM) {
	_IDIM = IDIM;
}

/**
Sets the segment ID for this time series.  
@param IDSEG the segment ID for this time series.
*/
public void setIDSEG(String IDSEG) {
	_IDSEG = IDSEG;
}

/**
Sets the data time interval from the time series header.
@param IDTINT the data time interval from the time series header.
*/
public void setIDTINT(byte IDTINT) {
	_IDTINT = IDTINT;
}

/**
Sets the future data indicator.
@param IFDAT the future data indicator.
*/
public void setIFDAT(int IFDAT) {
	_IFDAT = IFDAT;
}

/**
Sets the number of the first record in the file of this data type.
@param IFRECD the number of the first record in the file of this data type.
*/
public void setIFRECD(int IFRECD) {
	_IFRECD = IFRECD;
}

/**
Sets the number of the last record in the file of this data type.
@param ILRECD the number of the last record in the file of this data type.
*/
public void setILRECD(int ILRECD) {
	_ILRECD = ILRECD;
}

/**
Sets the processing indicator (see the notes for the class for more info).
@param IPROC the processing indicator (see the notes for the class for 
more info).
*/
public void setIPROC(int IPROC) {
	_IPROC = IPROC;
}

/**
Sets the location in the record of the first future data value from the 
time series header.
@param IPTFUT the location in the record of the first future data value from 
the time series header.
*/
public void setIPTFUT(short IPTFUT) {
	_IPTFUT = IPTFUT;
}

/**
Sets the location in the record of the first regular/observed data value
from the time series header.
@param IPTREG the location in the record of the first regular/observed data 
value from the time series header.
*/
public void setIPTREG(short IPTREG) {
	_IPTREG = IPTREG;
}

/**
Sets the _isDataFilled flag so it is known whether or not data has
been added to this class.
*/
public void setIsDataFilled(boolean isDataFilled) {
	_isDataFilled = isDataFilled;
}

/**
Sets the code for component write from the time series header.
@param ITSFUT the code for component write from the time series header.
*/
public void setITSFUT(int ITSFUT) {
	_ITSFUT = ITSFUT;
}

/**
Sets the unit number.  This is used to determine the filename for 
time series data retrieval.
@param IUNIT the unit number to set.
*/
public void setIUNIT(int IUNIT) {
	_IUNIT = IUNIT;
}

/**
Sets the Julian hour of the first data value from the time series header.
@param JULBEG the Julian hour of the first data value from the time series 
header.
*/
public void setJULBEG(int JULBEG) {
	_JULBEG = JULBEG;
}

/**
Sets the length of the time series header in words.
@param LTSHDR the length of the time series header in words.
*/
public void setLTSHDR(byte LTSHDR) {
	_LTSHDR = LTSHDR;
}

/**
Sets the maximum number of days of data.
@param MAXDAY the maximum number of days of data.
*/
public void setMAXDAY(int MAXDAY) {
	_MAXDAY = MAXDAY;
}

/**
Sets the maximum number of data types.
@param MAXDTP the maximum number of data types.
*/
public void setMAXDTP(int MAXDTP) {
	_MAXDTP = MAXDTP;
}

/**
Sets the maximum number of time series.
@param MAXTMS the maximum number of time series.
*/
public void setMAXTMS(int MAXTMS) {
	_MAXTMS = MAXTMS;
}

/**
Sets the minimum number of days of observed data kept.
@param MINDAY the minimum number of days of observed data kept.
*/
public void setMINDAY(int MINDAY) {
	_MINDAY = MINDAY;
}

/**
Sets the smallest time interval allowed.
@param MINDT the smallest time interval allowed.
*/
public void setMINDT(int MINDT) {	
	_MINDT = MINDT;
}

/**
Sets the user identifier.
@param NAMERF the user identifier.
*/
public void setNAMERF(String NAMERF) {
	_NAMERF = NAMERF;
}

/**
Sets the number of time series of the data type kept in the core.
@param NCORE the number of time series of the data type kept in the core.
*/
public void setNCORE(int NCORE) {
	_NCORE = NCORE;
}

/**
Sets the record number of the next time series from the time series header.
@param NRECNX the record number of the next time series from the time series 
header.
*/
public void setNRECNX(int NRECNX) {
	_NRECNX = NRECNX;
}

/**
Sets the maximum number of data values from the time series header.
@param NTSMAX the maximum number of data values from the time series header.
*/
public void setNTSMAX(short NTSMAX) {
	_NTSMAX = NTSMAX;
}

/**
Sets the number of data values from the time series header.
@param NTSNUM the number of data values from the time series header.
*/
public void setNTSNUM(short NTSNUM) {
	_NTSNUM = NTSNUM;
}

/**
Sets the number of time series.
@param NUMTMS the number of time series.
*/
public void setNUMTMS(int NUMTMS) {
	_NUMTMS = NUMTMS;
}

/**
Sets the number of data types.
@param NUMDTP the number of data types.
*/
public void setNUMDTP(int NUMDTP) {
	_NUMDTP = NUMDTP;
}

/**
Sets the number of time series defined for the data type.
@param NUMTS the number of time series defined for the data type.
*/
public void setNUMTS(int NUMTS) {
	_NUMTS = NUMTS;
}

/**
Sets the number of values per time interval.
@param NVAL the number of values per time interval.
*/
public void setNVAL(int NVAL) {
	_NVAL = NVAL;
}

/**
Sets the number of values per data time interval from the time series header.
@param NVLINT the number of values per data time interval from the time 
series header.
*/
public void setNVLINT(byte NVLINT) {
	_NVLINT = NVLINT;
}

/**
Sets the number of extra words in the header.
@param NXHDR the number of extra words in the header.
*/
public void setNXHDR(int NXHDR) {
	_NXHDR = NXHDR;
}

/**
Sets the regular/observed time series.
@param observedTS the regular/observed time series.
*/
public void setObservedTS(TS observedTS) {
	_observedTS = observedTS;
}

/**
Sets the parent NWSRFS_Operation object for this time series.
@param operation the parent NWSRFS_Operation object for this time series.
*/
public void setOperation(NWSRFS_Operation operation) {	
	__operation = operation;
}

/**
Sets the prdIndex value for this time series.
@param prdIndex the prdIndex value for this time series.
*/
public void setPrdIndex(int prdIndex) {
	_prdIndex = prdIndex;
}

/**
Sets the Segment identifier for this time series.
@param IDSEG the segment identifier for this time series.
*/
public void setSegmentID(String IDSEG) {
	_IDSEG = IDSEG;
}

/**
Sets additional time series information.
@param tsAddInformation additional time series information.
*/
public void setTSAddInformation(List tsAddInformation) {
	_tsAddInformation = tsAddInformation;
}

/**
Sets the time series data file type code.
@param tsDataFileCode the time series data file type code.
*/
public void setTSDataFileCode(String tsDataFileCode) {
	_tsDataFileCode = tsDataFileCode;
}

/**
Sets the time series data type.
@param tsDataType the time series data type.
*/
public void setTSDataType(String tsDataType) {
	_tsDataType = tsDataType;
}

/**
Sets the time series description from the time series header.
@param TSDESC the time series description from the time series header.
*/
public void setTSDESC(String TSDESC) {
	_TSDESC = TSDESC;
}

/**
Sets the number of hours in the time series date time interval.
@param tsDTInterval the number of hours in the time series date time interval.
*/
public void setTSDTInterval(int tsDTInterval) {
	_tsDTInterval = tsDTInterval;
}

/**
Sets time series external location information.
@param tsExtLocInformation time series external location information.
*/
public void setTSExtLocInformation(List tsExtLocInformation) {
	_tsExtLocInformation = tsExtLocInformation;
}

/**
Sets the number of values of external location information.
@param tsExtNVAL the number of values of external location information.
*/
public void setTSExtNVAL(int tsExtNVAL) {
	_tsExtNVAL = tsExtNVAL;
}

/**
Set the time series identifier. 
@param tsID the time series identifier.
*/
public void setTSID(String tsID) {
	_TSID = tsID;
}

/**
Sets the int indicator for the NWSRFS type of time series.
@param tsIndicator the int indicator for the NWSRFS type of time series.
*/
public void setTSIndicator(int tsIndicator) {
	_tsIndicator = tsIndicator;
}

/**
Sets the time series latitude from the time series header.
@param TSLAT the time series latitude from the time series header.
*/
public void setTSLAT(float TSLAT) {
	_TSLAT = TSLAT;
}

/**
Sets the time series longitude from the time series header.
@param TSLONG the time series longitude from the time series header.
*/
public void setTSLONG(float TSLONG) {
	_TSLONG = TSLONG;
}

/**
Sets the number of values of additional time series information.
@param tsNADD the number of values of additional time series information.
*/
public void setTSNADD(int tsNADD) {
	_tsNADD = tsNADD;
}

/**
Sets the number of values per time series time interval.
@param tsNVAL the number of values per time series time interval.
*/
public void setTSNVAL(int tsNVAL) {
	_tsNVAL = tsNVAL;
}

/**
Sets the starting location of the time series in the D array.
@param tsTimeseriesPointer the starting location of the time series in the 
D array.
*/
public void setTSTimeseriesPointer(int tsTimeseriesPointer) {
	_tsTimeseriesPointer = tsTimeseriesPointer;
}

/**
Sets the time series data units code from the time series header.
@param TSUNIT the time series data units code from the time series header.
*/
public void setTSUNIT(String TSUNIT) {
	_TSUNIT = TSUNIT;
}

/**
Sets the value for time series output and update types that indicates when
the time series is written to the data files.
@param tsWhenWriteIndicator the value for time series output and update types 
that indicates when the time series is written to the data files.
*/
public void setTSWhenWriteIndicator(int tsWhenWriteIndicator) {
	_tsWhenWriteIndicator = tsWhenWriteIndicator;
}

/**
Returns a String representation of the time series (the ID).
@return a String representation of the time series (the ID).
*/
public String toString() {
	return _TSID;
}

}
