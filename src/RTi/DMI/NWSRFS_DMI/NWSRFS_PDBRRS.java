//------------------------------------------------------------------------------
// NWSRFS_PDBRRS - class to contain the preprocessor 
// index/record Data for RRS data
//------------------------------------------------------------------------------
// History:
//
// 2004-11-1	Scott Townsend, RTi	Initial version.
//------------------------------------------------------------------------------
// Endheader

package RTi.DMI.NWSRFS_DMI;

import java.util.List;
import java.util.Vector;

/**
The NWSRFS_PDBRRS - class to contain the preprocessor record data for RRS data
and is used to retrieve the data record for a specific time series. This class 
reads and stores data from the PDBRRS preprocessor database file; it stores 
the contents of PDBRRS for a particular station in this object. The 
PDBRRS database file has the following definition:
<pre>
IX.4.2B-PDBRRS      PREPROCESSOR DATA BASE FILE PDBRRS

Purpose
File PDBRRS contains the data for River, reservoir and snow (RRS) data
types.

RRS data are stored by observation with two or three values per
observation, depending on the data type. Each set of observations has
a header containing information about the data and station.

The RRS data records are stored in two areas.

The primary record area is stored in the PDBRRS file and contains data
values for the number of observations specified when the station was
defined.

The free pool record area is stored in one of the daily data files.
The free pool records are stored in a different file than the primary
records in order to minimize disk accesses. The free pool records are
used to expand a set of observations if a station reports more
frequently than usual and still retain the minimum period of observed
data specified for the station.

The data that is stored in the free pool records is for the oldest
days in the period of record for the station.


Description
ATTRIBUTES: fixed length 64 byte binary records
RECORD STRUCTURE:

                                    Word
  Variable Type      Dimension    Position   Description

  The first record is the File Control Record.

  MAXREC      I*4         1           1      Maximum primary records

  NEXTRC      I*4         1           2      Next available primary record

  FREE1       I*4         1           3      Record number of first free
                                             pool record

  FREEN       I*4         1           4      Record number of next
                                             available free pool record

  FREEL       I*4         1           5      Number of words in a free pool
                                             record

  LUFREE      I*4         1           6      Ordinal number of the daily
                                             data file in which the free
                                             pool data are stored

  MAXFRE      I*4         1           7      Maximum free pool records

  MAXPD       I*4         1           8      Length of longest observation
                                             period

  NUMSET      I*4         1           9      Number of daily and RRS
                                             stations defined

  INUSE       I*4         1          10      In-use indicator

  USER        A8          1          11      User name

  The next group of records are the RRS Data Records. 1/
  The contents of the RRS primary data record is as follows:

  NWRDS       I*4         1           1      Number of words in RRS primary
                                             record (header, statistics and
                                             data)

  STAID       A8          1         2-3      Station identifier

  NUMID       I*4         1           4      Station number

  DTYPE       A4          1           5      Data type

  MINDAY      A4          1           6      Minimum number of days of
                                             observed data to be retained

  MAXOBS      I*4         1           7      Maximum number of observations
                                             that can be stored in primary
                                             space

  NUMOBS      I*4         1           8      Number of observations in
                                             primary space

  EVAL        I*4         1           9      Word position of earliest
                                             value

  REVAL       I*4         1          10      Not used

  LVAL        I*4         1          11      Word position of latest value

  RLVAL       I*4         1          12      Not used

  IFREC1      I*4         1          13      Record number of first free
                                             pool record (zero if none)

  NVALS       I*4         1          14      Number of values per
                                             observation

  FTIME       I*4         1           15      Time of first data free pool
                                               record

  LSTHR       I*4         1           16      Julian hour of last value of
                                               observed data

  NSTAT       I*4         1           17      Number of words of statistics

  BDATE       I*4         1           18      Julian hour statistics begin

  RDATE       I*4         1           19      Julian date of most recent
                                              report

  NTOTAL      I*4         1           20      Total number of reports

  RPTLG       R*4         1           21      Largest value reported

  LDATE       R*4         1           22      Julian date of largest value
                                              reported

  RPT2LG      R*4         1           23      Second largest value reported

  L2DATE      I*4         1           24      Julian date of second largest
                                              value reported

  RPTSM       R*4         1           25      Julian date of smallest value
                                              reported

  SDATE       I*4         1           26      Julian date of smallest
                                              reported

  RPT2SM      I*4         1           27      Second smallest value reported

  S2DATE      I*4         1           28      Julian date of second smallest
                                              value reported

  DATA      I*4,R*4       ?           29+     See note 2/

  The free pool records are a continuation of the data part of the RRS
  record. The content of the RRS free pool data record is as follows:

  NXTREC      I*4         1            1      Pointer to next available free
                                              pool record (zero if none)

  NVALS       I*4         1            2      Number of observations in this
                                              record

  DATA      I*4,R*4     NVALS         3+      See note 2/

Notes:

1/ The RRS data records consist of a header followed by the data.
   The user determines a typical number of values for the maximum
   reporting period and this amount of space is reserved when a
   station is defined. If the stations report more frequently and
   thus require more records to keep the minimum reporting period of
   observed data, these records are taken from the free pool record
   area. When a free record is no longer needed, it is reset to
   unused. If no more free records can be found from the pointers, a
   search will be made for unused records to find one that has been
   returned to the pool. The word FREEL points to the last unused
   record found to reduce the number of records that must be searched.

   Access to these RRS data records is through one of two methods.
   The first method is to read the next sequential record in the file.
   If this is not the desired RRS station, the routines use a hashing
   algorithm to read the index record to get the record number of the
   desired station.

2/ If the data is an instantaneous value, the observation time and the
   data values are stored.

   If the data is a mean value, the observation time, the data value
   and the data time interval are stored.

   The observation time is stored as Julian minutes.


              IX.4.2B-PDBRRS-4 
</pre>
*/

public class NWSRFS_PDBRRS {

protected int _MAXREC;
protected int _NEXTRC;
protected int _FREE1;
protected int _FREEN;
protected int _FREEL;
protected int _LUFREE;
protected int _MAXFRE;
protected int _MAXPD;
protected int _NUMSET;
protected int _INUSE;
protected String _USER;
protected int _NWRDS;
protected String _STAID;
protected int _NUMID;
protected String _DTYPE;
protected int _MINDAY;
protected int _MAXOBS;
protected int _NUMOBS;
protected int _EVAL;
protected int _REVAL;
protected int _LVAL;
protected int _RLVAL;
protected int _IFREC1;
protected int _NVALS;
protected int _FTIME;
protected int _LSTHR;
protected int _NSTAT;
protected int _BDATE;
protected int _RDATE;
protected int _NTOTAL;
protected float _RPTLG;
protected int _LDATE;
protected float _RPT2LG;
protected int _L2DATE;
protected float _RPTSM;
protected int _SDATE;
protected float _RPT2SM;
protected int _S2DATE;
protected List _OBSTIME;
protected List _DATAVAL;
protected List _DATATIMEINT;
protected int _NXTREC;
protected int _NVALSFP;

/**
Constructor.
*/
public NWSRFS_PDBRRS(String ID) {
	initialize();
	setSTAID(ID);
}

// Add methods for Vector structures
public void addOBSTIME(int OBSTIME) {
	_OBSTIME.add(new Integer(OBSTIME)); 
}

public void addDATAVAL(float DATAVAL) {
	_DATAVAL.add(new Float(DATAVAL)); 
}

public void addDATATIMEINT(int DATATIMEINT) {
	_DATATIMEINT.add(new Integer(DATATIMEINT)); 
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize() {
	_MAXREC		= -1;
	_NEXTRC		= -1;
	_FREE1		= -1;
	_FREEN		= -1;
	_FREEL		= -1;
	_LUFREE		= -1;
	_MAXFRE		= -1;
	_MAXPD		= -1;
	_NUMSET		= -1;
	_INUSE		= -1;
	_USER		= null;
	_NWRDS		= -1;
	_STAID		= null;
	_NUMID		= -1;
	_DTYPE		= null;
	_MINDAY		= -1;
	_MAXOBS		= -1;
	_NUMOBS		= -1;
	_EVAL		= -1;
	_REVAL		= -1;
	_LVAL		= -1;
	_RLVAL		= -1;
	_IFREC1		= -1;
	_NVALS		= -1;
	_FTIME		= -1;
	_LSTHR		= -1;
	_NSTAT		= -1;
	_BDATE		= -1;
	_RDATE		= -1;
	_NTOTAL		= -1;
	_RPTLG		= -1;
	_LDATE		= -1;
	_RPT2LG		= -1;
	_L2DATE		= -1;
	_RPTSM		= -1;
	_SDATE		= -1;
	_RPT2SM		= -1;
	_S2DATE		= -1;
	_OBSTIME	= null;
	_DATAVAL	= null;
	_DATATIMEINT	= null;
	_NXTREC		= -1;
	_NVALSFP	= -1;
}

/**
This method will determine whether or not the timeseries is instantaneous 
or mean data.
@return a boolean -- true if instanteneous -- false if mean data.
*/
public boolean getIsInstantaneous() {
	if(getNVALS() < 3) {
		return true;
	}
	
	return false;
}

public int getMAXREC() {
	return _MAXREC; 
}

public int getNEXTRC() {
	return _NEXTRC; 
}

public int getFREE1() {
	return _FREE1; 
}

public int getFREEN() {
	return _FREEN; 
}

public int getFREEL() {
	return _FREEL; 
}

public int getLUFREE() {
	return _LUFREE; 
}

public int getMAXFRE() {
	return _MAXFRE; 
}

public int getMAXPD() {
	return _MAXPD; 
}

public int getNUMSET() {
	return _NUMSET; 
}

public int getINUSE() {
	return _INUSE; 
}

public String getUSER() {
	return _USER; 
}

public int getNWRDS() {
	return _NWRDS; 
}

public String getSTAID() {
	return _STAID; 
}

public int getNUMID() {
	return _NUMID; 
}

public String getDTYPE() {
	return _DTYPE; 
}

public int getMINDAY() {
	return _MINDAY; 
}

public int getMAXOBS() {
	return _MAXOBS; 
}

public int getNUMOBS() {
	return _NUMOBS; 
}

public int getEVAL() {
	return _EVAL; 
}

public int getREVAL() {
	return _REVAL; 
}

public int getLVAL() {
	return _LVAL; 
}

public int getRLVAL() {
	return _RLVAL; 
}

public int getIFREC1() {
	return _IFREC1; 
}

public int getNVALS() {
	return _NVALS; 
}

public int getFTIME() {
	return _FTIME; 
}

public int getLSTHR() {
	return _LSTHR; 
}

public int getNSTAT() {
	return _NSTAT; 
}

public int getBDATE() {
	return _BDATE; 
}

public int getRDATE() {
	return _RDATE; 
}

public int getNTOTAL() {
	return _NTOTAL; 
}

public float getRPTLG() {
	return _RPTLG; 
}

public int getLDATE() {
	return _LDATE; 
}

public float getRPT2LG() {
	return _RPT2LG; 
}

public int getL2DATE() {
	return _L2DATE; 
}

public float getRPTSM() {
	return _RPTSM; 
}

public int getSDATE() {
	return _SDATE; 
}

public float getRPT2SM() {
	return _RPT2SM; 
}

public int getS2DATE() {
	return _S2DATE; 
}

public int getNXTREC() {
	return _NXTREC; 
}

public int getNVALSFP() {
	return _NVALSFP; 
}

public List getOBSTIME() {
	return _OBSTIME; 
}  

public int getOBSTIME(int OBSTIMEindex) { 
	return (int)((Integer)_OBSTIME.get(OBSTIMEindex)).intValue();
}

public List getDATAVAL() {
	return _DATAVAL; 
}  

public float getDATAVAL(int DATAVALindex) { 
	return (float)((Float)_DATAVAL.get(DATAVALindex)).floatValue();
}

public List getDATATIMEINT() {
	return _DATATIMEINT; 
}  

public int getDATATIMEINT(int DATATIMEINTindex) { 
	return (int)((Integer)_DATATIMEINT.get(DATATIMEINTindex)).intValue();
}

/**
Initialize global objects.
*/
private void initialize() {
	_MAXREC		= -1;
	_NEXTRC		= -1;
	_FREE1		= -1;
	_FREEN		= -1;
	_FREEL		= -1;
	_LUFREE		= -1;
	_MAXFRE		= -1;
	_MAXPD		= -1;
	_NUMSET		= -1;
	_INUSE		= -1;
	_USER		= new String();
	_NWRDS		= -1;
	_STAID		= new String();
	_NUMID		= -1;
	_DTYPE		= new String();
	_MINDAY		= -1;
	_MAXOBS		= -1;
	_NUMOBS		= -1;
	_EVAL		= -1;
	_REVAL		= -1;
	_LVAL		= -1;
	_RLVAL		= -1;
	_IFREC1		= -1;
	_NVALS		= -1;
	_FTIME		= -1;
	_LSTHR		= -1;
	_NSTAT		= -1;
	_BDATE		= -1;
	_RDATE		= -1;
	_NTOTAL		= -1;
	_RPTLG		= -1;
	_LDATE		= -1;
	_RPT2LG		= -1;
	_L2DATE		= -1;
	_RPTSM		= -1;
	_SDATE		= -1;
	_RPT2SM		= -1;
	_S2DATE		= -1;
	_OBSTIME	= new Vector();
	_DATAVAL	= new Vector();
	_DATATIMEINT	= new Vector();
	_NXTREC		= -1;
	_NVALSFP	= -1;
}

public void setMAXREC(int MAXREC) {
	_MAXREC = MAXREC; 
}

public void setNEXTRC(int NEXTRC) {
	_NEXTRC = NEXTRC; 
}

public void setFREE1(int FREE1) {
	_FREE1 = FREE1; 
}

public void setFREEN(int FREEN) {
	_FREEN = FREEN; 
}

public void setFREEL(int FREEL) {
	_FREEL = FREEL; 
}

public void setLUFREE(int LUFREE) {
	_LUFREE = LUFREE; 
}

public void setMAXFRE(int MAXFRE) {
	_MAXFRE = MAXFRE; 
}

public void setMAXPD(int MAXPD) {
	_MAXPD = MAXPD; 
}

public void setNUMSET(int NUMSET) {
	_NUMSET = NUMSET; 
}

public void setINUSE(int INUSE) {
	_INUSE = INUSE; 
}

public void setUSER(String USER) {
	_USER = USER; 
}

public void setNWRDS(int NWRDS) {
	_NWRDS = NWRDS; 
}

public void setSTAID(String STAID) {
	_STAID = STAID; 
}

public void setNUMID(int NUMID) {
	_NUMID = NUMID; 
}

public void setDTYPE(String DTYPE) {
	_DTYPE = DTYPE; 
}

public void setMINDAY(int MINDAY) {
	_MINDAY = MINDAY; 
}

public void setMAXOBS(int MAXOBS) {
	_MAXOBS = MAXOBS; 
}

public void setNUMOBS(int NUMOBS) {
	_NUMOBS = NUMOBS; 
}

public void setEVAL(int EVAL) {
	_EVAL = EVAL; 
}

public void setREVAL(int REVAL) {
	_REVAL = REVAL; 
}

public void setLVAL(int LVAL) {
	_LVAL = LVAL; 
}

public void setRLVAL(int RLVAL) {
	_RLVAL = RLVAL; 
}

public void setIFREC1(int IFREC1) {
	_IFREC1 = IFREC1; 
}

public void setNVALS(int NVALS) {
	_NVALS = NVALS; 
}

public void setFTIME(int FTIME) {
	_FTIME = FTIME; 
}

public void setLSTHR(int LSTHR) {
	_LSTHR = LSTHR; 
}

public void setNSTAT(int NSTAT) {
	_NSTAT = NSTAT; 
}

public void setBDATE(int BDATE) {
	_BDATE = BDATE; 
}

public void setRDATE(int RDATE) {
	_RDATE = RDATE; 
}

public void setNTOTAL(int NTOTAL) {
	_NTOTAL = NTOTAL; 
}

public void setRPTLG(float RPTLG) {
	_RPTLG = RPTLG; 
}

public void setLDATE(int LDATE) {
	_LDATE = LDATE; 
}

public void setRPT2LG(float RPT2LG) {
	_RPT2LG = RPT2LG; 
}

public void setL2DATE(int RPTSM) {
	_RPTSM = RPTSM; 
}

public void setRPTSM(float RPTSM) {
	_RPTSM = RPTSM; 
}

public void setSDATE(int SDATE) {
	_SDATE = SDATE; 
}

public void setRPT2SM(float RPT2SM) {
	_RPT2SM = RPT2SM; 
}

public void setS2DATE(int S2DATE) {
	_S2DATE = S2DATE; 
}

public void setNXTREC(int NXTREC) {
	_NXTREC = NXTREC; 
}

public void setNVALSFP(int NVALSFP) {
	_NVALSFP = NVALSFP; 
}


}
