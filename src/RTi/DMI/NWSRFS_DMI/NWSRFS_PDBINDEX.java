// NWSRFS_PDBINDEX - class to contain the preprocessor index/record pointers

package RTi.DMI.NWSRFS_DMI;

import java.util.ArrayList;
import java.util.List;

/**
The NWSRFS_PDBINDEX - class to contain the preprocessor record 
pointers and is used to increase performance in reading the files PDBLYn and 
PDBRRS by retrieving the record number for a specific time series used in those 
files. This class reads and stores data from the PDBINDEX preprocessor 
database file; it stores the entire contents of PDBINDEX in this object. The 
PDBINDEX database file has the following definition:
<pre>
IX.4.2B-PDBINDEX    PREPROCESSOR DATA BASE FILE PDBINDEX

Purpose
File PDBINDEX contains information about stations defined in the
Preprocessor Data Base (PPDB).

The information is used to access the station data and includes
control information for dates and record pointers for the daily data
types.

Description

ATTRIBUTES: fixed length 64 byte binary records

RECORD STRUCTURE:
                                  Word
  Variable Type     Dimension   Position   Description

  The first record is the File Control Record.

  NWRDS       I*4       1          1       Number of words in record

  LRECL1      I*4       1          2       Logical record length of daily
                                           data files

  LRECL2      I*4       1          3       Logical record length of RRS
                                           file

  LRECL3      I*4       1          4       Logical record length of Index
                                           file

  MAXTYP      I*4       1          5       Maximum number of daily data
                                           types

  NUMTYP      I*4       1          6       Actual number of daily data
                                           types

  TYPREC      I*4       1          7       Record of the first Data Type
                                           Directory record

  NHASHR      I*4       1          8       Number of I*2 words in hash
                                           records

  H8CREC      I*4       1          9       Record number of the first
                                           character hash record

  HINTRC      I*4       1          10      Record number of the first
                                           integer hash record

  INFREC      I*4       1          11      Record number of the first
                                           Station Information record
                                          (first record used is
                                          INFREC+1)

  MFILE      I*4       1          12      Maximum records in file

  LFILE      I*4       1          13      Last used record in file

  LURRS      I*4       1          14      Maximum days between last day
                                          of observed data and first day
                                          of future data

  MAXDDF     I*4       1          15      Maximum number of daily data
                                          files

  NUMDDF     I*4       1          16      Number of daily data files
                                          used

  The next group of records are the data file information records.

  Words 1 thru 4 are repeated NUMDDF times:

  MDDFRC     I*4       1          1       Maximum records in daily data
                                          file

  LDDFRC     I*4       1          2       Last record used in daily data
                                          file

             I*4       2         3-4      Unused

  The next group of records are the Daily Data Type Directory records.

  NWRDS      I*2       1          1       Number of words in record

  DTYPE      A4        1         2-3      Data type

  LUFILE     I*2       1          4       Logical unit of file in which
                                          data is stored

  NPNTRS     I*2       1          5       See note 1/

  NDATA      I*2       1          6       Number of data values for each
                                          station (can vary for data
                                          types PPVR and TAVR)

  MAXDAY     I*2       1          7       Maximum days of data

  EDATE      I*4       1         8-9      Julian day of earliest data

  ECRECN     I*2       1          10      Record number of earliest data
                                          (not used for TF24)

  LDATE      I*4       1        11-12     Julian day of latest data (not
                                          used for TF24)

  LDRECN     I*2       1          13      Record number of latest data

  PNTR       I*2       1          14      Record number of pointer
                                          record

  DATAR1     I*2       1          15      Record number of first data
                                          record

  MAXSTA     I*2       1          16      Maximum number of stations

  NUMSTA     I*2       1          17      Number of stations defined

  LSTPTR     I*2       1          18      Last used word for pointer
                                          record

  LSTDTA     I*2       1          19      Last used word in data record

  NSTATS     I*2       1          20      Number of words of statistics
                                          per station for data type

  NREC1D     I*2       1          21      Number of data records for one
                                          day of data

             I*2       2         22-24    Unused

  The next group of records contain the Station Hash Indexes. 2/

  IPDHSC     I*2    NHASHR         1      Record numbers of Station
                                          Information records for
                                          station character identifiers

  IPDHSI     I*2    NHASHR         1      Record numbers of Station
                                          Information records for
                                          station integer identifiers

  The next group of records are the Station Information records. 3/

  NWRDS      I*2       1           1      Number of words in record

  STAID      A8       1           2-5     Station character identifier

  NUMID      I*2       1           6      Station integer identifier

  PRMPTR     I*2       1           7      Record number of GENL
                                          parameter record in
                                          Preprocessor Parametric Data
                                          Base

  PCPPTR     I*2       1           8      Array location of 24 hour
                                          precipitation data 4/


  TMPPTR     I*2       1           9       Array location of 24 hour
                                           temperature max/min data 4/

  NADDTP     I*2       1           10      Number of additional data
                                           types

  Words 11 thru 13 are repeated NADDTP times.

  ADDDTP     A4        1         11-12     Data type 4/

  ADTPTR     I*2       1           13      Array location if daily data
                                           type or record number of RRS
                                           primary data 4/ 5/

  The following statistic are stored for stations with 24 hour PCPN
  data:

  BDATE      I*2       2       11+NADDTP*3 Julian day statistics begin

  RDATE      I*2       2       13+NADDTP*3 Julian day of most recent
                                           report

  NDAYS      I*2       1       15+NADDTP*3 Number of days with reports

  NTOTAL     I*2       1       16+NADDTP*3 Total number of reports

  NZERO      I*2       1       17+NADDTP*3 Number of days that zero
                                           precipitation is reported

  ACDCP      R*4       1       18+NADDTP*3 Accumulated precipitation

  RPTLG      I*2       1       20+NADDTP*3 Largest reported value (stored
                                           as value*100)

  LDATE      I*2       2       21+NADDTP*3 Julian day of largest reported
                                           value

  RPT2LG     I*2       1       23+NADDTP*3 Second largest value reported
                                           (stored as value*100)

  L2DATE     I*2       2       24+NADDTP*3 Julian day of second largest
                                           reported value

  SMNOZO     I*2       1       26+NADDTP*3 Smallest non-zero report

  ACPSQ      R*4       1       27+NADDTP*3 Accumulated precipitation
                                           squared

  The following word is used for stations that have been redefined:

  NWRDSO     I*2       1        NWRDS+1    Number of words in old SIF
                                           entry

Notes:

1/ For data types PP24, PPVR, TAVR, TM24, MDR6, TF24, EA24, PPST, APIG
   and PG24 the value is the number of pointers.

   For data types TX24 and TN24 the value is the indicator for the
   write data types in read data type TM24.

   For data types TFMX and TFMN the value is the indicator for the
   write data types in read data type TF24.

   For data types PP01, PP03, PP06, TA01, TA03 and TA06 the value is
   the data time interval for the write only types.

   For data types TA24, TD24, US24, RC24, RP24 and RI24 the value is
   the indicator for the write only types in read data type EA24.

   For data type PPSR the value is the number of words in the one
   pointer record needed for this type.

2/ Access to the Station Information records is through a hashing
   algorithm. The hash can be done using the 8-character station
   identifier or the user-assigned integer station number. The first
   set of records stores the hashed indices for the 8-character
   station identifier. The second set of records stores the hashed
   indices for the user-assigned integer station number. These hashed
   indices point to the Station Information record in another part of
   the file.

3/ The Station Information records contain pointers to the data for
   each data type reported by a station. For stations with PCPN data
   they also have room for statistical information.

4/ For Daily data types the value stored is the starting location of
   the data in the data array returned from the PPDB read daily data
   routine (RPDDLY) for the specified data type.

5/ For RRS data types the value stored is the record number of the
   data in the RRS primary data file.


                IX.4.2B-PDBINDEX-5
</pre>
*/

public class NWSRFS_PDBINDEX {

protected int _H8CREC;
protected int _HINTRC;
protected int _INFREC;
protected int _LFILE;
protected int _LRECL1;
protected int _LRECL2;
protected int _LRECL3;
protected int _LURRS;
protected int _MAXDDF;
protected int _MAXTYP;
protected int _MFILE;
protected int _NHASHR;
protected int _NUMDDF;
protected int _NUMTYP;
protected int _NWRDS;
protected int _TYPREC;
protected List<Float> _ACDCP;
protected List<Float> _ACPSQ;
protected List<List<String>> _ADDDTP;
protected List<List<Integer>> _ADTPTR;
protected List<Integer> _BDATE;
protected List<Integer> _DATAR1;
protected List<String> _DTYPE;
protected List<Integer> _ECRECN;
protected List<Integer> _EDATE;
protected List<Integer> _L2DATE;
protected List<Integer> _LDATE;
protected List<Integer> _LDATEDDT;
protected List<Integer> _LDDFRC;
protected List<Integer> _LDRECN;
protected List<Integer> _LSTDTA;
protected List<Integer> _LSTPTR;
protected List<Integer> _LUFILE;
protected List<Integer> _MAXDAY;
protected List<Integer> _MAXSTA;
protected List<Integer> _MDDFRC;
protected List<Integer> _NADDTP;
protected List<Integer> _NDATA;
protected List<Integer> _NDAYS;
protected List<Integer> _NPNTRS;
protected List<Integer> _NREC1D;
protected List<Integer> _NSTATS;
protected List<Integer> _NTOTAL;
protected List<Integer> _NUMID;
protected List<Integer> _NUMSTA;
protected List<Integer> _NWRDSDDT;
protected List<Integer> _NWRDSSTI;
protected List<Integer> _NWRDSO;
protected List<Integer> _NZERO;
protected List<Integer> _PCPPTR;
protected List<Integer> _PNTR;
protected List<Integer> _PRMPTR;
protected List<Integer> _RDATE;
protected List<Integer> _RPT2LG;
protected List<Integer> _RPTLG;
protected List<Integer> _SMNOZO;
protected List<String> _STAID;
protected List<Integer> _TMPPTR;

/**
Constructor.
If the calling class uses this constructor then it will need to call the 
readFile method manually.  This constructor is needed to allow multiple calls 
through the same DMI object.
*/
public NWSRFS_PDBINDEX() {
	initialize();
}

public void addACDCP(float ACDCP) {
	_ACDCP.add(Float.valueOf(ACDCP)); 
}

public void addACPSQ(float ACPSQ) {
	_ACPSQ.add(Float.valueOf(ACPSQ)); 
}

public void addADDDTP(List<String> ADDDTP) {
	_ADDDTP.add(ADDDTP); 
}

public void addADTPTR(List<Integer> ADTPTR) {
	_ADTPTR.add(ADTPTR); 
}

public void addBDATE(int BDATE) {
	_BDATE.add(Integer.valueOf(BDATE)); 
}

public void addDATAR1(int DATAR1) {
	_DATAR1.add(Integer.valueOf(DATAR1)); 
}

public void addDTYPE(String DTYPE) {
	_DTYPE.add(DTYPE); 
}

public void addECRECN(int ECRECN) {
	_ECRECN.add(Integer.valueOf(ECRECN)); 
}

public void addEDATE(int EDATE) {
	_EDATE.add(Integer.valueOf(EDATE)); 
}

public void addL2DATE(int L2DATE) {
	_L2DATE.add(Integer.valueOf(L2DATE)); 
}

public void addLDATE(int LDATE) {
	_LDATE.add(Integer.valueOf(LDATE)); 
}

public void addLDATEDDT(int LDATEDDT) {
	_LDATEDDT.add(Integer.valueOf(LDATEDDT)); 
}

public void addLDDFRC(int LDDFRC) {
	_LDDFRC.add(Integer.valueOf(LDDFRC)); 
}

public void addLDRECN(int LDRECN) {
	_LDRECN.add(Integer.valueOf(LDRECN)); 
}

public void addLSTDTA(int LSTDTA) {
	_LSTDTA.add(Integer.valueOf(LSTDTA)); 
}

public void addLSTPTR(int LSTPTR) {
	_LSTPTR.add(Integer.valueOf(LSTPTR)); 
}

public void addLUFILE(int LUFILE) {
	_LUFILE.add(Integer.valueOf(LUFILE)); 
}

public void addMAXDAY(int MAXDAY) {
	_MAXDAY.add(Integer.valueOf(MAXDAY)); 
}

public void addMAXSTA(int MAXSTA) {
	_MAXSTA.add(Integer.valueOf(MAXSTA)); 
}

public void addMDDFRC(int MDDFRC) {
	_MDDFRC.add(Integer.valueOf(MDDFRC)); 
}

public void addNADDTP(int NADDTP) {
	_NADDTP.add(Integer.valueOf(NADDTP)); 
}

public void addNDATA(int NDATA) {
	_NDATA.add(Integer.valueOf(NDATA)); 
}

public void addNDAYS(int NDAYS) {
	_NDAYS.add(Integer.valueOf(NDAYS)); 
}

public void addNPNTRS(int NPNTRS) {
	_NPNTRS.add(Integer.valueOf(NPNTRS)); 
}

public void addNREC1D(int NREC1D) {
	_NREC1D.add(Integer.valueOf(NREC1D)); 
}

public void addNSTATS(int NSTATS) {
	_NSTATS.add(Integer.valueOf(NSTATS)); 
}

public void addNTOTAL(int NTOTAL) {
	_NTOTAL.add(Integer.valueOf(NTOTAL)); 
}

public void addNUMID(int NUMID) {
	_NUMID.add(Integer.valueOf(NUMID)); 
}

public void addNUMSTA(int NUMSTA) {
	_NUMSTA.add(Integer.valueOf(NUMSTA)); 
}

public void addNWRDSDDT(int NWRDSDDT) {
	_NWRDSDDT.add(Integer.valueOf(NWRDSDDT)); 
}

public void addNWRDSSTI(int NWRDSSTI) {
	_NWRDSSTI.add(Integer.valueOf(NWRDSSTI)); 
}

public void addNWRDSO(int NWRDSO) {
	_NWRDSO.add(Integer.valueOf(NWRDSO)); 
}

public void addNZERO(int NZERO) {
	_NZERO.add(Integer.valueOf(NZERO)); 
}

public void addPCPPTR(int PCPPTR) {
	_PCPPTR.add(Integer.valueOf(PCPPTR)); 
}

public void addPNTR(int PNTR) {
	_PNTR.add(Integer.valueOf(PNTR)); 
}

public void addPRMPTR(int PRMPTR) {
	_PRMPTR.add(Integer.valueOf(PRMPTR)); 
}

public void addRDATE(int RDATE) {
	_RDATE.add(Integer.valueOf(RDATE)); 
}

public void addRPT2LG(int RPT2LG) {
	_RPT2LG.add(Integer.valueOf(RPT2LG)); 
}

public void addRPTLG(int RPTLG) {
	_RPTLG.add(Integer.valueOf(RPTLG)); 
}

public void addSMNOZO(int SMNOZO) {
	_SMNOZO.add(Integer.valueOf(SMNOZO)); 
}

public void addSTAID(String STAID) {
	_STAID.add(STAID); 
}

public void addTMPPTR(int TMPPTR) {
	_TMPPTR.add(Integer.valueOf(TMPPTR)); 
}

public int getH8CREC() {
	return _H8CREC; 
}

public int getHINTRC() {
	return _HINTRC; 
}

public int getINFREC() {
	return _INFREC; 
}

/**
This method determine whether or not a PDB data type is an RRS data type or
not. If it is not then it is a Daily Data type.
@param dataType the data type to check.
@return a boolean - true if it is a RRS data type -- false otherwise.
*/
public static boolean getIsRRSType(String dataType) {

	if(dataType.equalsIgnoreCase("AESC")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("DQIN")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("DQME")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("FBEL")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("FGDP")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("GATE")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("GTCS")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("ICET")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("LAKH")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("LELV")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("NFBD")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("PCFD")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("PELV")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("QIN")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("QME")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("RQGM")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("RQIM")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("RQIN")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("RQME")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("RQOT")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("RQSW")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("RSTO")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("SNOG")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("SNWE")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("STG")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("TID")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("TWEL")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("TWSW")) {
		return true;
	}
	else if(dataType.equalsIgnoreCase("ZELV")) {
		return true;
	}

	return false;
}

public int getLFILE() {
	return _LFILE; 
}

public int getLRECL1() {
	return _LRECL1; 
}

public int getLRECL2() {
	return _LRECL2; 
}

public int getLRECL3() {
	return _LRECL3; 
}

public int getLURRS() {
	return _LURRS; 
}

public int getMAXDDF() {
	return _MAXDDF; 
}

public int getMAXTYP() {
	return _MAXTYP; 
}

public int getMFILE() {
	return _MFILE; 
}

public int getNHASHR() {
	return _NHASHR; 
}

public int getNUMDDF() {
	return _NUMDDF; 
}

public int getNUMTYP() {
	return _NUMTYP; 
}

public int getNWRDS() {
	return _NWRDS; 
}

public int getTYPREC() {
	return _TYPREC; 
}

public List<Float> getACDCP() {
	return _ACDCP; 
}  
public float getACDCP(int ACDCPindex) { 
	return (float)((Float)_ACDCP.get(ACDCPindex)).floatValue();
}

public List<Float> getACPSQ() {
	return _ACPSQ; 
}  
public float getACPSQ(int ACPSQindex) { 
	return (float)((Float)_ACPSQ.get(ACPSQindex)).floatValue();
}

public List<List<String>> getADDDTP() {
	return _ADDDTP; 
}  
public List<String> getADDDTP(int ADDDTPVindex) { 
	return (List<String>)_ADDDTP.get(ADDDTPVindex);
}
public String getADDDTP(int ADDDTPVindex,int ADDDTPindex) { 
	List<String> adddtpVect = (List<String>)_ADDDTP.get(ADDDTPVindex);
	return adddtpVect.get(ADDDTPindex);
}

public List<List<Integer>> getADTPTR() {
	return _ADTPTR; 
}  
public List<Integer> getADTPTR(int ADTPTRVindex) { 
	return (List<Integer>)_ADTPTR.get(ADTPTRVindex);
}
public int getADTPTR(int ADTPTRVindex, int ADTPTRindex) { 
	List<Integer> adtptrVect = (List<Integer>)_ADTPTR.get(ADTPTRVindex);
	return (int)((Integer)adtptrVect.get(ADTPTRindex)).intValue();
}

public List<Integer> getBDATE() {
	return _BDATE; 
}  
public int getBDATE(int BDATEindex) { 
	return (int)((Integer)_BDATE.get(BDATEindex)).intValue();
}

public List<Integer> getDATAR1() {
	return _DATAR1; 
}  
public int getDATAR1(int DATAR1index) { 
	return (int)(_DATAR1.get(DATAR1index)).intValue();
}

public List<String> getDTYPE() {
	return _DTYPE; 
}  
public String getDTYPE(int DTYPEindex) { 
	return _DTYPE.get(DTYPEindex);
}

public List<Integer> getECRECN() {
	return _ECRECN; 
}  
public int getECRECN(int ECRECNindex) { 
	return (int)(_ECRECN.get(ECRECNindex)).intValue();
}

public List<Integer> getEDATE() {
	return _EDATE; 
}  
public int getEDATE(int EDATEindex) { 
	return (int)(_EDATE.get(EDATEindex)).intValue();
}

public List<Integer> getL2DATE() {
	return _L2DATE; 
}  
public int getL2DATE(int L2DATEindex) { 
	return (int)(_L2DATE.get(L2DATEindex)).intValue();
}

public List<Integer> getLDATE() {
	return _LDATE; 
}  
public int getLDATE(int LDATEindex) { 
	return (int)(_LDATE.get(LDATEindex)).intValue();
}

public List<Integer> getLDATEDDT() {
	return _LDATEDDT; 
}  
public int getLDATEDDT(int LDATEDDTindex) { 
	return (int)(_LDATEDDT.get(LDATEDDTindex)).intValue();
}

public List<Integer> getLDDFRC() {
	return _LDDFRC; 
}  
public int getLDDFRC(int LDDFRCindex) { 
	return (int)(_LDDFRC.get(LDDFRCindex)).intValue();
}

public List<Integer> getLDRECN() {
	return _LDRECN; 
}  
public int getLDRECN(int LDRECNindex) { 
	return (int)(_LDRECN.get(LDRECNindex)).intValue();
}

public List<Integer> getLSTDTA() {
	return _LSTDTA; 
}  
public int getLSTDTA(int LSTDTAindex) { 
	return (int)(_LSTDTA.get(LSTDTAindex)).intValue();
}

public List<Integer> getLSTPTR() {
	return _LSTPTR; 
}  
public int getLSTPTR(int LSTPTRindex) { 
	return (int)(_LSTPTR.get(LSTPTRindex)).intValue();
}

public List<Integer> getLUFILE() {
	return _LUFILE; 
}  
public int getLUFILE(int LUFILEindex) { 
	return (int)(_LUFILE.get(LUFILEindex)).intValue();
}

public List<Integer> getMAXDAY() {
	return _MAXDAY; 
}  
public int getMAXDAY(int MAXDAYindex) { 
	return (int)(_MAXDAY.get(MAXDAYindex)).intValue();
}

public List<Integer> getMAXSTA() {
	return _MAXSTA; 
}  
public int getMAXSTA(int MAXSTAindex) { 
	return (int)(_MAXSTA.get(MAXSTAindex)).intValue();
}

public List<Integer> getMDDFRC() {
	return _MDDFRC; 
}  
public int getMDDFRC(int MDDFRCindex) { 
	return (int)(_MDDFRC.get(MDDFRCindex)).intValue();
}

public List<Integer> getNADDTP() {
	return _NADDTP; 
}  
public int getNADDTP(int NADDTPindex) { 
	return (int)(_NADDTP.get(NADDTPindex)).intValue();
}

public List<Integer> getNDATA() {
	return _NDATA; 
}  
public int getNDATA(int NDATAindex) { 
	return (int)(_NDATA.get(NDATAindex)).intValue();
}

public List<Integer> getNDAYS() {
	return _NDAYS; 
}  
public int getNDAYS(int NDAYSindex) { 
	return (int)(_NDAYS.get(NDAYSindex)).intValue();
}

public List<Integer> getNPNTRS() {
	return _NPNTRS; 
}  
public int getNPNTRS(int NPNTRSindex) { 
	return (int)(_NPNTRS.get(NPNTRSindex)).intValue();
}

public List<Integer> getNREC1D() {
	return _NREC1D; 
}  
public int getNREC1D(int NREC1Dindex) { 
	return (int)(_NREC1D.get(NREC1Dindex)).intValue();
}

public List<Integer> getNSTATS() {
	return _NSTATS; 
}  
public int getNSTATS(int NSTATSindex) { 
	return (int)((Integer)_NSTATS.get(NSTATSindex)).intValue();
}

public List<Integer> getNTOTAL() {
	return _NTOTAL; 
}
public int getNTOTAL(int NTOTALindex) { 
	return (int)(_NTOTAL.get(NTOTALindex)).intValue();
}

public List<Integer> getNUMID() {
	return _NUMID; 
}  
public int getNUMID(int NUMIDindex) { 
	return (int)(_NUMID.get(NUMIDindex)).intValue();
}

public List<Integer> getNUMSTA() {
	return _NUMSTA; 
}  
public int getNUMSTA(int NUMSTAindex) { 
	return (int)(_NUMSTA.get(NUMSTAindex)).intValue();
}

public List<Integer> getNWRDSDDT() {
	return _NWRDSDDT; 
}  
public int getNWRDSDDT(int NWRDSDDTindex) { 
	return (int)(_NWRDSDDT.get(NWRDSDDTindex)).intValue();
}

public List<Integer> getNWRDSSTI() {
	return _NWRDSSTI; 
}  
public int getNWRDSSTI(int NWRDSSTIindex) { 
	return (int)((Integer)_NWRDSSTI.get(NWRDSSTIindex)).intValue();
}

public List<Integer> getNWRDSO() {
	return _NWRDSO; 
}  
public int getNWRDSO(int NWRDSOindex) { 
	return (int)((Integer)_NWRDSO.get(NWRDSOindex)).intValue();
}

public List<Integer> getNZERO() {
	return _NZERO; 
}  
public int getNZERO(int NZEROindex) { 
	return (int)((Integer)_NZERO.get(NZEROindex)).intValue();
}

public List<Integer> getPCPPTR() {
	return _PCPPTR; 
}  
public int getPCPPTR(int PCPPTRindex) { 
	return (int)((Integer)_PCPPTR.get(PCPPTRindex)).intValue();
}

public List<Integer> getPNTR() {
	return _PNTR; 
}  
public int getPNTR(int PNTRindex) { 
	return (int)((Integer)_PNTR.get(PNTRindex)).intValue();
}

public List<Integer> getPRMPTR() {
	return _PRMPTR; 
}  
public int getPRMPTR(int PRMPTRindex) { 
	return (int)((Integer)_PRMPTR.get(PRMPTRindex)).intValue();
}

public List<Integer> getRDATE() {
	return _RDATE; 
}  
public int getRDATE(int RDATEindex) { 
	return (int)((Integer)_RDATE.get(RDATEindex)).intValue();
}

public List<Integer> getRPT2LG() {
	return _RPT2LG; 
}  
public int getRPT2LG(int RPT2LGindex) { 
	return (int)((Integer)_RPT2LG.get(RPT2LGindex)).intValue();
}

public List<Integer> getRPTLG() {
	return _RPTLG; 
}  
public int getRPTLG(int RPTLGindex) { 
	return (int)((Integer)_RPTLG.get(RPTLGindex)).intValue();
}

public List<Integer> getSMNOZO() {
	return _SMNOZO; 
}  
public int getSMNOZO(int SMNOZOindex) { 
	return (int)((Integer)_SMNOZO.get(SMNOZOindex)).intValue();
}

public List<String> getSTAID() {
	return _STAID; 
}  
public String getSTAID(int STAIDindex) { 
	return (String)_STAID.get(STAIDindex);
}

public List<Integer> getTMPPTR() {
	return _TMPPTR; 
}  
public int getTMPPTR(int TMPPTRindex) { 
	return (int)((Integer)_TMPPTR.get(TMPPTRindex)).intValue();
}

/**
Initialize global objects.
*/
private void initialize() {
	_ACDCP 		= new ArrayList<>();
	_ACPSQ 		= new ArrayList<>();
	_ADDDTP 	= new ArrayList<>();
	_ADTPTR 	= new ArrayList<>();
	_BDATE 		= new ArrayList<>();
	_DATAR1 	= new ArrayList<>();
	_DTYPE 		= new ArrayList<>();
	_ECRECN 	= new ArrayList<>();
	_EDATE 		= new ArrayList<>();
	_H8CREC 	= -1;
	_HINTRC 	= -1;
	_INFREC 	= -1;
	_L2DATE 	= new ArrayList<>();
	_LDATE 		= new ArrayList<>();
	_LDATEDDT 	= new ArrayList<>();
	_LDDFRC 	= new ArrayList<>();
	_LDRECN 	= new ArrayList<>();
	_LFILE 		= -1;
	_LRECL1 	= -1;
	_LRECL2 	= -1;
	_LRECL3 	= -1;
	_LSTDTA 	= new ArrayList<>();
	_LSTPTR 	= new ArrayList<>();
	_LUFILE 	= new ArrayList<>();
	_LURRS 		= -1;
	_MAXDAY 	= new ArrayList<>();
	_MAXDDF 	= -1;
	_MAXSTA 	= new ArrayList<>();
	_MAXTYP 	= -1;
	_MDDFRC 	= new ArrayList<>();
	_MFILE 		= -1;
	_NADDTP 	= new ArrayList<>();
	_NDATA 		= new ArrayList<>();
	_NDAYS 		= new ArrayList<>();
	_NHASHR 	= -1;
	_NPNTRS 	= new ArrayList<>();
	_NREC1D 	= new ArrayList<>();
	_NSTATS 	= new ArrayList<>();
	_NTOTAL 	= new ArrayList<>();
	_NUMDDF 	= -1;
	_NUMID 		= new ArrayList<>();
	_NUMSTA 	= new ArrayList<>();
	_NUMTYP 	= -1;
	_NWRDS 		= -1;
	_NWRDSDDT 	= new ArrayList<>();
	_NWRDSSTI 	= new ArrayList<>();
	_NWRDSO 	= new ArrayList<>();
	_NZERO 		= new ArrayList<>();
	_PCPPTR 	= new ArrayList<>();
	_PNTR 		= new ArrayList<>();
	_PRMPTR 	= new ArrayList<>();
	_RDATE 		= new ArrayList<>();
	_RPT2LG 	= new ArrayList<>();
	_RPTLG 		= new ArrayList<>();
	_SMNOZO 	= new ArrayList<>();
	_STAID 		= new ArrayList<>();
	_TMPPTR 	= new ArrayList<>();
	_TYPREC 	= -1;
}

public void setH8CREC(int H8CREC) {
	_H8CREC = H8CREC; 
}

public void setHINTRC(int HINTRC) {
	_HINTRC = HINTRC; 
}

public void setINFREC(int INFREC) {
	_INFREC = INFREC; 
}

public void setLFILE(int LFILE) {
	_LFILE = LFILE; 
}

public void setLRECL1(int LRECL1) {
	_LRECL1 = LRECL1; 
}

public void setLRECL2(int LRECL2) {
	_LRECL2 = LRECL2; 
}

public void setLRECL3(int LRECL3) {
	_LRECL3 = LRECL3; 
}

public void setLURRS(int LURRS) {
	_LURRS = LURRS; 
}

public void setMAXDDF(int MAXDDF) {
	_MAXDDF = MAXDDF; 
}

public void setMAXTYP(int MAXTYP) {
	_MAXTYP = MAXTYP; 
}

public void setMFILE(int MFILE) {
	_MFILE = MFILE; 
}

public void setNHASHR(int NHASHR) {
	_NHASHR = NHASHR; 
}

public void setNUMDDF(int NUMDDF) {
	_NUMDDF = NUMDDF; 
}

public void setNUMTYP(int NUMTYP) {
	_NUMTYP = NUMTYP; 
}

public void setNWRDS(int NWRDS) {
	_NWRDS = NWRDS; 
}

public void setTYPREC(int TYPREC) {
	_TYPREC = TYPREC; 
}

}
