//-----------------------------------------------------------------------------
// NWSRFS_DMI - class to interact with NWSRFS databases
//-----------------------------------------------------------------------------
// History:
//
// 2004-02-18	Shawn chen, RTi		Initial version.
// 2004-03-09	Scott Townsend, RTi	Updated to produce javadoc
//						and cleaned up code.
// 2004-03-10	SAT, RTi		Started to modify code to meet 
//						the design.
// 2004-03-30	SAT, RTi		Revised to meet RTi coding
//						standards
// 2004-05-26	SAT, RTi		Cleaned up code based on peer
//						review
// 2004-07-21	SAT, RTi		Added code to read in data from
//						the prepocessor parameter DB.
//						Also cleaned up comments and
//						documentation.
// 2004-07-27	A. Morgan Love, RTi	Commented out references to
//						NWSRFS_PPPINDEX to get 
//						class to compile.  
//
// 2004-08-18	J. Thomas Sapienza, RTi	* Revised to match RTi DMI code 
//					  standards.
//					* Began implementing get() and set() 
//					  methods for data classes.
// 2004-08-24	SAT			Cleaned up code and added reading from
//					the preprocessor parameteric DB. Also
//					addressing preformance problems.
// 2004-09-02	SAT			Modifying for TSTool support.
// 2004-09-11	SAM, RTi		* Add getDatabaseProperties().
//					* Add __opened_with_AppsDefaults and
//					  openedWithAppsDefaults().  This
//					  information is used by TSTool, for
//					  example, to properly format time
//					  series identifiers.
// 2004-09-15	SAM, RTi		* Fix bug in readTimeSeriesList() - was
//					  not handling a specific ID (returned
//					  all).
// 2004-09-16	SAM, RTi		* Fix bug in readTimeSeriesList() - null
//					  time series were being added to the
//					  returned list.
// 2004-09-17	SAT, RTi		Modified readStation to read from the
//					preproccessed parameteric database and
//					added readStationList methods.
// 2004-10-13	SAT, RTi		Added MAP/MAT... USER, BASN, NTWK, and
//					ORRS preprocessed parameteric DB data.
// 2004-10-13	SAT, RTi		Determined that a problem with both
//					IPCO and Mexico CPRCN fs5files was an
//					endian problem. Made appropriate fix.
// 2004-10-15	SAT, RTi		Added readPDBINDEX to read the pre-
//					processor database index. Also moved the
//                                      call to the readPPPINDEX from 
//					readGlobalData to readStationHashtable
//					and readStation to improve performance.
//					Also did the same for the readPRDINDEX.
// 2004-11-1	SAT, RTi		Adding methods to read from the 
//					preprocessed database.
// 2004-11-23	SAT, RTi		Modified code in parseOperationsRecord
//					to fix a bug in the printing of TS names
//					in plot-tul operations
// 2004-11-24	SAT, RTi		Modified code in ParseOperationsRecord
//					to check to see if MAPX TS found in
//					operations table truely exists or is
//					of a different time step. If different
//					return the TS name of the TS that does
//					exist.
// 2004-12-02	SAT, RTi		Modified code in readPDBDLY to only 
//					print a warning rather than throw an 
//					exception.
// 2006-10-01	KAT, RTi		readTimeSeries() with requested
//					DateTimes was resulting in returned
//					DateTimes that were not "Z".  Change to
//					always return "Z".
// 2006-10-03	SAM, RTi		* Add readUSERPARM() method.
//					* Use EndianRandomAccessFile instead
//					  of RandomAccessFile for the binary
//					  files.  This allows for more
//					  flexibility when reading.
// 2006-11-22	SAM, RTi		Clean up the readTimeSeries() method to
//					not be so convoluted.  Add more Javadoc
//					to benefit other developers.
//-----------------------------------------------------------------------------
// EndHeader

package RTi.DMI.NWSRFS_DMI;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.lang.reflect.Array;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import RTi.DMI.NWSRFS_DMI.NWSRFS_Carryover;
import RTi.DMI.NWSRFS_DMI.NWSRFS_CarryoverGroup;
import RTi.DMI.NWSRFS_DMI.NWSRFS_ESPTraceEnsemble;
import RTi.DMI.NWSRFS_DMI.NWSRFS_FCRCPTR;
import RTi.DMI.NWSRFS_DMI.NWSRFS_FCSEGPTR;
import RTi.DMI.NWSRFS_DMI.NWSRFS_ForecastGroup;
import RTi.DMI.NWSRFS_DMI.NWSRFS_MAP;
import RTi.DMI.NWSRFS_DMI.NWSRFS_MAT;
import RTi.DMI.NWSRFS_DMI.NWSRFS_NTWK;
import RTi.DMI.NWSRFS_DMI.NWSRFS_Operation;
import RTi.DMI.NWSRFS_DMI.NWSRFS_ORRS;
//import RTi.DMI.NWSRFS_DMI.NWSRFS_PDBDLY;
import RTi.DMI.NWSRFS_DMI.NWSRFS_PDBINDEX;
import RTi.DMI.NWSRFS_DMI.NWSRFS_PDBRRS;
import RTi.DMI.NWSRFS_DMI.NWSRFS_PPPINDEX;
import RTi.DMI.NWSRFS_DMI.NWSRFS_PRDINDEX;
import RTi.DMI.NWSRFS_DMI.NWSRFS_RatingCurve;
import RTi.DMI.NWSRFS_DMI.NWSRFS_Segment;
import RTi.DMI.NWSRFS_DMI.NWSRFS_Station;
import RTi.DMI.NWSRFS_DMI.NWSRFS_USER;
import RTi.DMI.NWSRFS_DMI.NWSRFS_Util;

import RTi.TS.TS;
import RTi.TS.HourTS;
import RTi.TS.TSIdent;
import RTi.TS.TSIterator;
import RTi.TS.TSUtil;

import RTi.Util.Message.Message;

import RTi.Util.IO.DataType;
import RTi.Util.IO.DataUnits;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.EndianDataInputStream;
import RTi.Util.IO.EndianDataOutputStream;
import RTi.Util.IO.EndianRandomAccessFile;
import RTi.Util.IO.PropList;

import RTi.Util.String.StringUtil;

import RTi.Util.Time.DateTime;
import RTi.Util.Time.StopWatch;
import RTi.Util.Time.TimeInterval;

import RTi.GRTS.TSGraph;

/**
The NWSRFS_DMI class interacts with the NWSRFS Fortran database. This class also
opens several "index" files from the Forecast Component (FC) and Preprocessor 
Parameteric (PPP) databases and stores the data from these files as members for 
use in other classes to speed data retrieval through an index rather than a 
search.

REVISIT (JTS - 2004-08-18)
Need an explanation and examples of how the class should be used.
*/
public class NWSRFS_DMI
{
// Private data members and objects
/**
The enumeration index for the file pointer arrays.
The first 9 are files from the Forecast Component Database (FC),
the next three from the Processed Database (PRD), and the last two
from the Prepocessor Parameteric Database (PPP).
*/
private final int 
		__FCCARRY  =	0,
		__FCCOGDEF =	1,
		__FCFGLIST =	2,
		__FCFGSTAT =	3,
		__FCPARAM  =	4,
		__FCRATING =	5,
		__FCRCPTR  =	6,
		__FCSEGPTR =	7,
		__FCSEGSTS =	8,
		__PRDINDEX =	9,
		__PRDPARM  =	10,
		__PPPINDEX =	11,
		__PRDTS1   =	12,
		//__PRDTS2   =	13,
		//__PRDTS3   =	14,
		//__PRDTS4   =	15,
		//__PRDTS5   =	16,
		//__PPPPARM1 =	17,
		//__PPPPARM2 =	18,
		//__PPPPARM3 =	19,
		//__PPPPARM4 =	20,
		//__PPPPARM5 =	21,
		__PDBINDEX =	22,
		__PDBRRS   =	23,
		//__PDBDLY1  =	24,
		//__PDBDLY2  =	25,
		//__PDBDLY3  =	26,
		//__PDBDLY4  =	27,
		//__PDBDLY5  =	28,
		__USERPARM  =	29;
		// TODO SAM 2006-10-03 The order is probably not important -
		// why not just list alphabetically to match file listing?
		// Scott's comments above do not add up the number of files
		// listed.

/**
The length of a word in bytes.  Used to avoid magic numbers.
*/
private final int __WORDSIZE = 4;

/**
The byte length of a record in the above binary files.
*/
private final int[] __byteLength = {	400,	// FCCARRY
					456,	// FCCOGDEF
					8,	// FCFGLIST
					80,	// FCFGSTAT
					400,	// FCPARAM
					1200,	// FCRATING
					12,	// FCRCPTR
					12,	// FCSEGPTR
					260,	// FCSEGSTS
					16,	// PRDINDEX
					72,	// PRDPARM
					16,	// PPPINDEX
					64,	// PRDTS1
					64,	// PRDTS2
					64,	// PRDTS3
					64,	// PRDTS4
					64,	// PRDTS5
					64,	// PPPPARM1
					64,	// PPPPARM2
					64,	// PPPPARM3
					64,	// PPPPARM4
					64,	// PPPPARM5
					64,	// PDBINDEX
					64,	// PDBRRS
					64,	// PDBDLY1
					64,	// PDBDLY2
					64,	// PDBDLY3
					64,	// PDBDLY4
					64,	// PDBDLY5
					240};	// USERPARM
/**
A boolean array specifiying whether or not the file is OPEN
*/
private boolean[] __isOpen = {	false,		// FCCARRY
				false,		// FCCOGDEF
				false,		// FCFGLIST
				false,		// FCFGSTAT
				false,		// FCPARAM
				false,		// FCRATING
				false,		// FCRCPTR
				false,		// FCSEGPTR
				false,		// FCSEGSTS
				false,		// PRDINDEX
				false,		// PRDPARM
				false,		// PPPINDEX
				false,		// PRDTS1
				false,		// PRDTS2
				false,		// PRDTS3
				false,		// PRDTS4
				false,		// PRDTS5
				false,		// PPPPARM1
				false,		// PPPPARM2
				false,		// PPPPARM3
				false,		// PPPPARM4
				false,		// PPPPARM5
				false,		// PDBINDEX
				false,		// PDBRRS
				false,		// PDBDLY1
				false,		// PDBDLY2
				false,		// PDBDLY3
				false,		// PDBDLY4
				false,		// PDBDLY5
				false};		// USERPARM

/**
The filename Strings associated with the above indices.
*/
private final String[] __dbFileNames = {"FCCARRY",
					"FCCOGDEF",
					"FCFGLIST",
					"FCFGSTAT",
					"FCPARAM",
					"FCRATING",
					"FCRCPTR",
					"FCSEGPTR",
					"FCSEGSTS",
					"PRDINDEX",
					"PRDPARM",
					"PPPINDEX",
					"PRDTS1",
					"PRDTS2",
					"PRDTS3",
					"PRDTS4",
					"PRDTS5",
					"PPPPARM1",
					"PPPPARM2",
					"PPPPARM3",
					"PPPPARM4",
					"PPPPARM5",
					"PDBINDEX",
					"PDBRRS",
					"PDBDLY1",
					"PDBDLY2",
					"PDBDLY3",
					"PDBDLY4",
					"PDBDLY5",
					"USERPARM"};

/**
The name Strings associated with the operation number in the NWSRFS system.
*/
private final int 
	//__OP_NONE = 		0,
	//__OP_SAC_SMA = 		1,
	//__OP_UNIT_HG = 		2,
	//__OP_REDO_UHG = 	3,
	//__OP_CLEAR_TS = 	4,
	//__OP_SAC_PLOT = 	5,
	__OP_MEAN_Q = 		6,
	__OP_LAG_K = 		7,
	//__OP_CHANLOSS = 	8,
	//__OP_MUSKROUT = 	9,
	__OP_ADD_SUB = 		10,
	//__OP_LAY_COEF = 	11,
	//__OP_INSQPLOT = 	12,
	//__OP_TATUM = 		13,
	//__OP_ADJUST_Q = 	14,
	//__OP_WEIGH_TS = 	15,
	//__OP_STAT_QME = 	16,
	//__OP_WY_PLOT = 		17,
	//__OP_PLOT_TS = 		18,
	//__OP_SNOW_17 = 		19,
	__OP_CHANGE_T = 	20,
	//__OP_DWOPER = 		21,
	//__OP_HFS = 		22,
	__OP_STAGE_Q = 		23,
	//__OP_API_CONT = 	24,
	__OP_PLOT_TUL = 	25,
	//__OP_RES_SNGL = 	26,
	//__OP_LIST_FTW = 	27,
	//__OP_CHANLEAK = 	28,
	//__OP_API_MKC = 		29,
	__OP_MERGE_TS = 	30,
	//__OP_SNOW_17U = 	31,
	__OP_FFG = 		32
	//__OP_API_CIN = 		33,
	//__OP_API_SLC = 		34,
	//__OP_API_HAR = 		35,
	//__OP_XIN_SMA = 		36,
	//__OP_LIST_MSP = 	37,
	//__OP_BASEFLOW = 	38,
	//__OP_LOOKUP = 		39,
	//__OP_WATERBAL = 	40,
	//__OP_API_HAR2 = 	41,
	//__OP_RSNWELEV = 	42,
	//__OP_API_HFD = 		43,
	//__OP_SARROUTE = 	44,
	//__OP_DELTA_TS = 	45,
	//__OP_NOMSNG = 		46,
	//__OP_PEAKFLOW = 	47,
	//__OP_DIVIDE = 		48,
	//__OP_BEGASSIM = 	49,
	//__OP_ASSIM = 		50,
	//__OP_SSARRESV = 	51,
	//__OP_SUMPOINT = 	52,
	//__OP_LOOKUP3 = 		53
	;

/**
The name Strings associated with the operation number in the NWSRFS system.
*/
private final String[] __operationNames = {  	"NONE",         //   0
						"SAC-SMA",      //   1
						"UNIT-HG",      //   2
						"REDO-UHG",     //   3
						"CLEAR-TS",     //   4
						"SAC-PLOT",     //   5
						"MEAN-Q",       //   6
						"LAG/K",        //   7
						"CHANLOSS",     //   8
						"MUSKROUT",     //   9
						"ADD/SUB",      //  10
						"LAY-COEF",     //  11
						"INSQPLOT",     //  12
						"TATUM",        //  13
						"ADJUST-Q",     //  14
						"WEIGH-TS",     //  15
						"STAT-QME",     //  16
						"WY-PLOT",      //  17
						"PLOT-TS",      //  18
						"SNOW-17",      //  19
						"CHANGE-T",     //  20
						"DWOPER",       //  21
						"HFS",          //  22
						"STAGE-Q",      //  23
						"API-CONT",     //  24
						"PLOT-TUL",     //  25
						"RES-SNGL",     //  26
						"LIST-FTW",     //  27
						"CHANLEAK",     //  28
						"API-MKC",      //  29
						"MERGE-TS",     //  30
						"SNOW-17U",     //  31
						"FFG",          //  32
						"API-CIN",      //  33
						"API-SLC",      //  34
						"API-HAR",      //  35
						"XIN-SMA",      //  36
						"LIST-MSP",     //  37
						"BASEFLOW",     //  38
						"LOOKUP",       //  39
						"WATERBAL",     //  40
						"API-HAR2",     //  41
						"RSNWELEV",     //  42
						"API-HFD",      //  43
						"SARROUTE",     //  44
						"DELTA-TS",     //  45
						"NOMSNG",       //  46
						"PEAKFLOW",     //  47
						"DIVIDE",       //  48
						"BEGASSIM",     //  49
						"ASSIM",        //  50
						"SSARRESV",     //  51
						"SUMPOINT",     //  52
						"LOOKUP3"};     //  53

/**
This boolean value specifies whether the data files are big endian or little 
endian.
*/
private boolean __isBigEndian;

/**
Whether to use the FS5 files or not.  Not used for stand alone DMIs.  If true, 
use the fs5files, if false do not.
*/
private boolean __useFS5Files; 

/**
Hashtable for TS tsid values to check for existence in a very fast manner.
*/
private Hashtable __tsHashtable = null;

/**
Hashtable for TS Data Type and Logical Unit values 
to check for existence in a very fast manner.
*/
private Hashtable __tsDTUHashtable = null;

/**
A boolean specifying whether to cache the Time Series values or not.
It is often adventageous to used cached TS data to speed processes but
it also makes the data become "Outdated".
*/
private boolean __cacheTS = false;

/**
Properties of the fs5files. This includes Apps_defaults tokens fs5files and 
rfs_sys_dir.
*/
private PropList __NWSRFS_properties = null; // Properties of the fs5files

/**
The array of EndianRandomAccessFile objects holding for the binary database
files.
*/
private EndianRandomAccessFile[] __NWSRFS_DBFiles = null;

/**
String holding the location of the fs5files. This could be for either OFS or 
IFP.
*/
private String __fs5FilesLocation = null;

/**
The input name, used with time series.  This may often be the same as the FS5 Files location,
but in the future may be a virtual handle on the database.
*/
private String __input_name = "";

/**
Indicate whether the DMI was opened with Apps Defaults.  This is used, for
example, in TSTool, to automatically define the InputName part of time series
identifiers.
*/
private boolean __opened_with_AppsDefaults = false;

/**
This object holds index values for the FCRATING rating curve file.
*/
protected NWSRFS_FCRCPTR _fcrcptr = null;

/**
This object holds index values for the FCSEGSTS rating curve file.
*/
protected NWSRFS_FCSEGPTR _fcsegptr = null;

/**
This object holds index values for the Preprocessor database files:
PDBLYn and PDBRRS.
*/
protected NWSRFS_PDBINDEX _pdbindex = null;

/**
This object holds index values for the Preprocessor Parameteric database file:
PPPPARMn.
*/
protected NWSRFS_PPPINDEX _pppindex = null;

/**
This object holds index values for the Processed database file:
PRDTSn.
*/
protected NWSRFS_PRDINDEX _prdindex = null;

// TODO 05/26/2004 SAT -- There is a problem with file locking. The 
// Fortran binary database files could be open by NWSRFS while the DMI is
// being used. This could have some repercussions in being able to find all
// of the available data or having what was once a correct record number 
// changing. As long as everything is read only this is just a minor problem
// but if ever write back to the files then need to do some checks.

/**
Construct a blank NWSRFS_DMI instance where the location of the Fortran
data is determined by an NWSRFS Apps-defaults token.
Since the location of the NWSRFS Fortran database is required
this constructor will pull the location from the OS environment.
If the location is not in the environment it will print a message then
go into a stand alone mode where a binary file can be read or written to
but many of the useful feature of dmi will not be available.
@throws Exception if an error occurs while trying to get the 
location of the NWSRFS Fortran database via the "ofs_fs5files" OS 
environment variable.
*/
public NWSRFS_DMI() 
throws Exception {
	String routine = "NWSRFS_DMI.<Constructor>";

	initialize();
	
	__fs5FilesLocation = NWSRFS_Util.getAppsDefaults("ofs_fs5files");
	__opened_with_AppsDefaults = true;
	
	// If __fs5FilesLocation string is null then print message
	// and set the boolean to run in stand alone mode.
	if (__fs5FilesLocation == null) {
		__useFS5Files = false;
		Message.printWarning(10,routine,
			"NWSRFS_DMI is instantiated with reduced functionality: The environment variable "
			+ "ofs_fs5files is not defined.  Running stand alone.");
	}
	else {
		__fs5FilesLocation = __fs5FilesLocation.trim();
		setInputName ( __fs5FilesLocation );
	
		if (!__fs5FilesLocation.endsWith(File.separator)) {
			// If the ofs_fs5files environment variable does not have a file separator on the end, 
			// put one on.
			__fs5FilesLocation = __fs5FilesLocation + File.separator;
		}
	}
}
 
/**
Construct a copy NWSRFS_DMI instance. This is a copy constructor which
takes an instance of NWSRFS_DMI and creates a copy.
*/
public NWSRFS_DMI(NWSRFS_DMI dmi) {
	initialize();
	
	__fs5FilesLocation = 	dmi.getFS5FilesLocation();
	__input_name = dmi.getInputName();
	__opened_with_AppsDefaults = dmi.openedWithAppsDefaults();
	__NWSRFS_DBFiles = 	dmi.getNWSRFSDBFiles(); 
	__useFS5Files = 	dmi.usingFS5Files(); 
	__isOpen = 		dmi.isOpen();
	__isBigEndian = 	dmi.usingBigEndian();
	__NWSRFS_properties = 	dmi.getNWSRFSProperties();
	__tsHashtable = 	dmi.getTSHashtable();
	__tsDTUHashtable = 	dmi.getTSDTUHashtable();
	_fcrcptr = 		dmi.getFcrcptr(); 
	_fcsegptr = 		dmi.getFcsegptr(); 
	_pdbindex = 		dmi.getPDBIndex();
	_pppindex = 		dmi.getPPPIndex();
	_prdindex = 		dmi.getPRDIndex();
}
 
/**
Construct an NWSRFS_DMI instance by passing the directory location of the 
NWSRFS Fortran database and NWSRFS system files.
Since the location of the NWSRFS Fortran database is required
this constructor will pass the location from as an argument.
@param directory holds a string which is the directory path to the NWSRFS 
Fortran database for either the operational forecast system OFS or 
interactive forecast program IFP. If this value is an empty string, null, or 
equals to <pre>"stand alone"</pre>, the dmi goes into stand alone mode.
*/
public NWSRFS_DMI(String directory) {
	String routine = "NWSRFS_DMI.<Constructor>";

	initialize();

	__fs5FilesLocation = directory;
	setInputName ( directory );
	__opened_with_AppsDefaults = false;
	
	// If input string is empty or equals "stand alone" then print message
	// and set the boolean to run in stand alone mode.
	if (__fs5FilesLocation == null || __fs5FilesLocation.length() == 0 
		|| __fs5FilesLocation.equalsIgnoreCase("stand alone")) {
		__useFS5Files = false;
		Message.printWarning(10,routine,
			"NWSRFS_DMI is instantiated with reduced functionality: Running stand alone.");
	}
	else {
		__fs5FilesLocation = __fs5FilesLocation.trim();

		if (!__fs5FilesLocation.endsWith(File.separator)) {
			// If the ofs_fs5files environment variable does 
			// not have a file separator on the end, put one on.
			__fs5FilesLocation = __fs5FilesLocation + File.separator;
		}
	}
}

/**
Determine the NWSRFS process database version (HP or Linux) by determining the 
byte order.
REVISIT (JTS - 2004-08-21)
doesn't actually determine the database version.  versioning isn't really 
implemented yet, so it's not a big deal, but these docs either need changed
now or versioning needs added now.
@param filename a string holding the filename of the NWSRFS Fortran database 
file used to check the version.
@throws Exception if there is a problem opening the specified file as a 
EndianRandomAccessFile, reading the first bytes, or closing the file.
*/
protected void checkDatabaseEndianess(String filename)
throws Exception {
	String routine = "NWSRFS_DMI.checkDatabaseEndianess";
	EndianDataInputStream EDIS = null;
	
	if (__useFS5Files) {
		// Create a Random Access file object to test 
		// which endian big or little
		// the database binary files are using. 
		EndianRandomAccessFile endianCheck 
			= new EndianRandomAccessFile(
			__fs5FilesLocation + filename, "r");

		// Read the first set of bytes which should be an 
		// integer if the integer is too big or small (should 
		// be positive) must change the endianess. check big 
		// endian first.
		int byteTest = endianCheck.readInt();

		// If byteTest is < 0 or > 100000 then probably there is
		// an endian problem. The files passed in will alway
		// contain an integer for the first four bytes thus if it is
		// too big then chances are the endianess is wrong.
		// Also we check twice if the test fails since the fs5files
		// should be little endian! The second test is to check using
		// little endian. This is necessary since the first check
		// could definately yield a false positive.
		if (byteTest >= 0 && byteTest < 100000) {
			endianCheck.seek(0);
			EDIS = read(endianCheck, 0, 4);
			byteTest = EDIS.readEndianInt();
			if (byteTest < 0 || byteTest > 100000) {
				__isBigEndian = true;
			}
			EDIS.close();
		}
	
		endianCheck.close();
	}
	else {
		// Check the machine to determine the endianess.
		__isBigEndian = IOUtil.isBigEndianMachine();
	}
	
	if(__isBigEndian)
		Message.printStatus(10,routine,
		"The NWSRFS FS5Files endianess is Big Endian");
	else
		Message.printStatus(10,routine,
		"The NWSRFS FS5Files endianess is Little Endian");
}

/**
Checks to see whether the file pointed to by "filePointer" (see __Filename) is
open.   If not it opens it and returns.
@return true if the open has no errors, false if an error is caught.
*/
private boolean checkRandomAccessFileOpen(int filePointer, 
boolean readOFSFS5Files)
{
	boolean checkDB = false;
	checkDB = checkRandomAccessFileOpen(filePointer, readOFSFS5Files,false);
	return checkDB;
}

/**
Checks to see if the file pointed to by filepointer is open. 
If not it opens it and returns.
@return true if the open has no errors, false if an error is caught.
*/
private boolean checkRandomAccessFileOpen(int filePointer, 
boolean readOFSFS5Files, boolean readWrite) {
	String routine = "NWSRFS_DMI.checkRandomAccessFileOpen";

	try {
		// If the__isOpen[fileIndex] is false open the database binary 
		// file as a Random Access object
		if (!__isOpen[filePointer]) {
			if (__useFS5Files && readOFSFS5Files) {
				if(readWrite) {
					__NWSRFS_DBFiles[filePointer] 
						= new EndianRandomAccessFile(
						__fs5FilesLocation
						+ __dbFileNames[filePointer], 
						"rw");
				
						Message.printStatus(10,routine,
						"__dbFileNames[filePointer] = "+
						__dbFileNames[filePointer]+
						" is now open read/write");
				}
				else {
					__NWSRFS_DBFiles[filePointer] 
						= new EndianRandomAccessFile(
						__fs5FilesLocation
						+ __dbFileNames[filePointer], 
						"r");
				
						Message.printStatus(10,routine,
						"__dbFileNames[filePointer] = "+
						__dbFileNames[filePointer]+
						" is now open read only");
				}
			}
			else {
				if(readWrite) {
					__NWSRFS_DBFiles[filePointer] 
						= new EndianRandomAccessFile(
						__dbFileNames[filePointer], 
						"rw");
				
						Message.printStatus(10,routine,
						"__dbFileNames[filePointer] = "+
						__dbFileNames[filePointer]+
						" is now open read/write");
				}
				else {
					__NWSRFS_DBFiles[filePointer] 
						= new EndianRandomAccessFile(
						__dbFileNames[filePointer], 
						"r");
				
						Message.printStatus(10,routine,
						"__dbFileNames[filePointer] = "+
						__dbFileNames[filePointer]+
						" is now open read only");
				}
			}
			__isOpen[filePointer] = true;
		}
		else {
			rewind(__NWSRFS_DBFiles[filePointer]);
		}
	}
	catch (Exception e) {
		// TODO (JTS - 2004-08-18)
		// why not handle the exception, get more information about
		// what failed and print some warning messages?
		__isOpen[filePointer] = false;
		Message.printWarning(10,routine,e);
		exceptionCount++;
		return false;
	}

	return true;
}

/**
Checks to see if the time series exists in the binary Fortran database.
@param ts the time series to check.  Cannot be null.
@return true if the time series exists in the database, false if it does not.
@throws NullPointerException if the time series passed in is null.
*/
public boolean checkTimeSeriesExists(NWSRFS_TimeSeries ts) 
throws Exception {
	String routine = "NWSRFS_DMI.checkTimeSeriesExists";
	String tsIdentKey;
	
	if (ts == null) {
		throw new NullPointerException("Time series is null");
	}

	tsIdentKey = ts.getTSID() + "." + ts.getTSDataType() + "." 
		+ ts.getTSDTInterval();
	Message.printStatus(10, routine, "tsIdentKey = " + tsIdentKey 
		+ " __tsHashtable.containsKey(tsIdentKey) = " 
		+ __tsHashtable.containsKey(tsIdentKey));	

	if(__tsHashtable.containsKey(tsIdentKey)) {
		if (IOUtil.testing()) {
			Message.printStatus(10, "", "tsIdentKey '"
				+ tsIdentKey + "' in hash table.");
		}
		return true;
	}
	
	if(checkTimeSeriesExists(ts.getTSID(), ts.getTSDataType(), 
	    ts.getTSDTInterval())) {
		// Put the the tsID into the hash table for future checks.
		__tsHashtable.put(tsIdentKey,ts);
	}
	else {
		return false;
	}
	
	return true;
}

// SAT -- JTS overloaded this method M 2004-08-30 in order to see if could get
// working a version that not only checks for existence of a time series, but 
// also existence of time series data.  The old code was returning 'true' all
// the time.  The only change in this version of the method is that there is
// a second parameter and that parameter is passed through to the overloaded
// version of the method that takes String ID information.

public boolean checkTimeSeriesExists(NWSRFS_TimeSeries ts, 
boolean alsoCheckDataExist) 
throws Exception {
	String routine = "NWSRFS_DMI.checkTimeSeriesExists";
	String tsIdentKey;
	
	if (ts == null) {
		throw new NullPointerException("Time series is null");
	}

	tsIdentKey = ts.getTSID() + "." + ts.getTSDataType() + "." 
		+ ts.getTSDTInterval();
	Message.printStatus(10, routine, "tsIdentKey = " + tsIdentKey 
		+ " __tsHashtable.containsKey(tsIdentKey) = " 
		+ __tsHashtable.containsKey(tsIdentKey));	

	if(__tsHashtable.containsKey(tsIdentKey)) {
		if (IOUtil.testing()) {
			Message.printStatus(10, "", "tsIdentKey '"
				+ tsIdentKey + "' in hash table.");
		}
		return true;
	}
	
	if(checkTimeSeriesExists(ts.getTSID(), ts.getTSDataType(), 
	    ts.getTSDTInterval(), alsoCheckDataExist)) {
		// Put the the tsID into the hash table for future checks.
		__tsHashtable.put(tsIdentKey,ts);
	}
	else {
		return false;
	}
	
	return true;
}

/**
Check to see if the TS exists in the binary Fortran database.
@param tsID this is a String object that holds the TimeSeries Identifier
for the TimeSeries object. 
@param tsDT this is the String value of the TimeSeries data type. It is
necessary that the data type be supplied to get a unique set of Time Series
from the data files.
@param tsDTInterval this is the int value of the TimeSeries data time interval.
It is necessary that the data time interval be supplied to get a unique set of 
Time Series from the data files.
@return a boolean indicating whether the TS exists in the Database.  If true, 
the TS exists in the database.
@throws NullPointerException if TS identifier or datatype are null, or if there
is a problem reading from the database files.
*/
public boolean checkTimeSeriesExists(String tsID, String tsDT, int tsDTInterval)
throws Exception {
	return checkTimeSeriesExists(tsID, tsDT, tsDTInterval, false);
}

// SAT -- JTS overloaded this method M 2004-08-30 in order to see if could get
// working a version that not only checks for existence of a time series, but 
// also existence of time series data.  The old code was returning 'true' all
// the time.  To find changes, grep for 'found = false' -- all the code 
// involving 'found' and 'alsoCheckDataExist' is new.

public boolean checkTimeSeriesExists(String tsID, String tsDT, int tsDTInterval,
boolean alsoCheckDataExist)
throws Exception {
	String routine = "NWSRFS_DMI.checkTimeSeriesExists";
	//String tsIdentIn = null, prdtsDataFile = null;
	String parseChar;
	char[] charValue;
	int i, prdIndex = 1, prdTSIDSize, recordNum, unitNum = -1;
	EndianDataInputStream EDIS = null;
	// A dummy object to be passed to the readPRDTS method
	NWSRFS_TimeSeries tsFile = new NWSRFS_TimeSeries(tsID, tsDT,tsDTInterval); 
	
	// Check to see if the pppindex file exists! If not return empty list.
	if(getPRDIndex() == null) { 
		setPRDIndex(readPRDINDEX());
	}
	
	// Check the tsID and tsDT. They must not be null
	if (tsID == null) {
		throw new NullPointerException("Time series identifier is null.");
	}
	else if (tsDT == null) {
		throw new NullPointerException(
			"Time series data type is null. TSIdent = "+tsID+"."+ tsDT+"."+tsDTInterval);
	}
	else if (tsDTInterval <= 0) {
		throw new NullPointerException(	"Time series data time interval is null.");
	}
	else {
		//tsIdentIn = tsID + "." + tsDT + "." + tsDTInterval;
	}

	// Check if the the database binary file is open as a
	// Random Access object
	if (!checkRandomAccessFileOpen(__PRDPARM, true)) {
		throw new Exception("Can not open the " + __dbFileNames[__PRDPARM] + " binary database file");
	}

	if(__tsDTUHashtable.containsKey(tsDT)) {
		unitNum = ((Integer)__tsDTUHashtable.get(tsDT)).intValue();
	}
	else {
		// Now read the Time Series parameter file to get the parameters
		// for the Time series in the PRDTSn binary file.

		// Skip the first first record (240 bytes) and go to the 241 byte
		EDIS = read(__NWSRFS_DBFiles[__PRDPARM],0,240);
		EDIS.close();
	
		while (true) {
			// Read until EOF or break.
			try {
				// Read the subsequent records (72 bytes) 
				EDIS = read(__NWSRFS_DBFiles[__PRDPARM], 0,	__byteLength[__PRDPARM]);

				// Field 1 - [type field name here]
				charValue = new char[4];
				for (i = 0; i < 4; i++) {
					charValue[i] = EDIS.readEndianChar1();
				}
			
				parseChar = new String(charValue).trim();

				if (parseChar.length() == 0 || !parseChar.equalsIgnoreCase(tsDT)) {
						continue;
				}

				// Field 2 - [type field name here]
				unitNum = checkInt(EDIS.readEndianInt(), 0, 100, 0);				
				break;
			}
			catch (EOFException EOFe) {
				// Should never get here.
				exceptionCount++;
				Message.printStatus(10, routine, "No Time Series of data type: " + tsDT + " was found.");
				return false;
			}
			catch (IOException IOe) {
				// Should never get here.
				exceptionCount++;
				Message.printStatus(10, routine, "No Time Series of data type: " + tsDT + " was found.");
				return false;
			}
		}

		__tsDTUHashtable.put(tsDT,new Integer(unitNum));
		EDIS.close();
	}
	
	// Create the tsDataFile String. Need to loop through all 5 of the
	// TS files until find right unit number. If LUNIT from the PRDTSn
	// Equals the IUNIT value from the PRDPARM file then have the right
	// Time Series file to read the TS into.
	for (i = 0; i < 5; i++) {
		// FIXME SAM 2008-04-07 What is the following?  Simplify
		prdIndex = (int)new Integer(String.valueOf(__PRDTS1 + i)).intValue();
		/*
		if (__useFS5Files && true) {
			prdtsDataFile = __fs5FilesLocation + __dbFileNames[prdIndex]; 
		}
		else {
			prdtsDataFile = __dbFileNames[prdIndex];
		}
		*/

		// Check if the the database binary file is open as a Random Access object
		if (!checkRandomAccessFileOpen(prdIndex, true)) {
			throw new Exception("Cannot open the " + __dbFileNames[prdIndex] + " binary database file");
		}

// TODO (JTS - 2004-08-21)
// explain the magic number 4 and 5		
		EDIS = read(__NWSRFS_DBFiles[prdIndex],0, 4, 5);
		if (unitNum == EDIS.readEndianInt()) {
			break;
		}
		else {
			EDIS.close();
		}
	}

	// Check the PRDTSn to see if it is null!
	if (__NWSRFS_DBFiles[prdIndex] == null) {
		Message.printWarning(10, routine,
			"No Time Series of data type: "
			+ tsDT + " was found.");
		return false;
	}

	// Rewind the PRDTSn to prepare for the TS read.
	rewind(__NWSRFS_DBFiles[prdIndex]);

	// Now read the Time Series index object to get the Record number
	// for the Time series in the PRDTSn binary file.
	// This might look a little convoluted but what is going on is
	// this: We read a record from PRDINDEX which contains TDID, TSDT, and 
	// record
	// number for the PRDTSn file. Since no date time interval is used
	// here as a way to find a unique time series we must then read in the
	// date time interval from the PRDTSn file and check to see if it is the
	// same. If so we keep reading the time series data then break out. If 
	// not
	// we break out of the PRDTSn read and go to the next record in PRDINDEX
	// which matches TSID and TSDT... Ugly but there is no other way to do 
	// it
	// given the structure of the FS5Files.

	prdTSIDSize = ((getPRDIndex()).getTSID()).size();
	
	boolean found = false;
	
	for (i = 0; i < prdTSIDSize; i++) {
		// Check the TS ID to see if we match
		if (!((getPRDIndex()).getTSID(i)).equalsIgnoreCase(tsID)) {
			continue;
		}

		// Check the TS Data Type to see if we match
		if (!((getPRDIndex()).getTSDT(i)).equalsIgnoreCase(tsDT)) {
			continue;
		}

		// If we match the TS ID and TS Data Type the get record number
		recordNum = (getPRDIndex()).getIREC(i);

		// Now call readPRDTS to first see if we have the right record
		// then read the TS datafile to see if TS exists! The false 
		// tells the
		// method not to read all the data.
		if (readPRDTS(__NWSRFS_DBFiles[prdIndex], recordNum, tsFile,
		    false)) {
			found = true;
			break;
		}
		else {
			continue;
		}
	}

	EDIS.close();

	// if not concerned about whether the time series actually has data,
	// function like the old version of the method and just return true
	if (!alsoCheckDataExist) {
		return true;
	}
	else {
		// otherwise, return true if the time series has data, and 
		// false if the time series does not have data -- this value
		// is stored in 'found', which tells whether data were read
		// (or at least found) for the time series.
		return found;
	}
}

/**
Close the NWSRFS processed database files. It will loop through the 
__NWSRFS_DBFiles EndianRandomAccessFile objects and close them.
@throws Exception if there is a problem closing any of the files.  The method
will attempt to close all files, and if any them could not be closed, an
exception will be thrown.
*/ 
public void close() 
throws Exception
{
	Vector filenames = new Vector();

	for (int i = 0; i < __dbFileNames.length; i++) {
		if (__isOpen[i]) {
			try {
				__NWSRFS_DBFiles[i].close();
				__isOpen[i] = false;
			}
			catch (Throwable e) {
				exceptionCount++;
				filenames.add(__dbFileNames[i]);
			}
		}
	}

	int size = filenames.size();
	if (size > 0) {
		String error = null;
		if (size == 1) {
			error = "Could not close file: ";
		}
		else {
			error = "Could not close files: ";
		}

		for (int i = 0; i < filenames.size(); i++) {
			if (i > 0) {
				error += ", ";
			}
			error += filenames.elementAt(i);
		}
		throw new Exception(error);
	}
} 

/**
Determine the NWSRFS Fortran database version.
REVISIT 05/26/2004 SAT to determine a means by which to tell which 
version of the NWSRFS binary database it is.
*/
protected long determineDatabaseVersion() {
	return (long)0;
}

/**
Return a Vector of String containing database properties.  Currently this is
used mainly for basic user feedback and troubleshooting (e.g., to make sure
that the expected directory is being used.
@return a Vector of String containing database properties.
@param level A level indicating the amount of information to provide - currently
not implemented.
*/
public Vector getDatabaseProperties( int level )
{	Vector v = new Vector();
	if ( __fs5FilesLocation == null ) {
		v.addElement ( "No FS5Files directory has been specified." );
	}
	else {	v.addElement ( "FS5Files directory:  " +  getFS5FilesLocation());
	}
	return v;
}

/**
Returns the object storing index values for the FCRATING rating curve file.
@return the object storing index values for the FCRATING rating curve file.
*/
public NWSRFS_FCRCPTR getFcrcptr() {
	return _fcrcptr;
}

/**
Returns the object storing index values for the FCSEGSTS rating curve file.
@return the object storing index values for the FCSEGSTS rating curve file.
*/
public NWSRFS_FCSEGPTR getFcsegptr() {
	return _fcsegptr;
}

/** 
Return the path to the FS5Files.
The private String __fs5FilesLocation holds the path and
includes a trailing slash. This method removes the trailing slash
for applications which are not expecing it.
@return a String holding the path to the FS5Files.
@deprecated use getFS5FilesLocation()
*/
public String getFS5Files() {
	if (!__fs5FilesLocation.endsWith(File.separator)) 
		return __fs5FilesLocation;
	else
		return __fs5FilesLocation.substring(0,__fs5FilesLocation.
			length()-1);
}

/**
Returns the path to the FS5 files.
The private String __fs5FilesLocation holds the path and
includes a trailing slash. This method removes the trailing slash
for applications which are not expecing it.
@return the path to the FS5 files.
*/
public String getFS5FilesLocation() {
	if (!__fs5FilesLocation.endsWith(File.separator))
		return __fs5FilesLocation;
	else
		return __fs5FilesLocation.substring(0,__fs5FilesLocation.
			length()-1);
}

/**
Return the input name, used with time series identifiers.
*/
public String getInputName ()
{	return __input_name;
}

/** 
Returns the isBigEndian boolean value.
@deprecated use usingBigEndian() instead.
*/
public boolean getIsBigEndian(){
	return __isBigEndian;
}

/**
Returns the array of NWSRFS Database files.
@return the array of NWSRFS Database files.
*/
public EndianRandomAccessFile[] getNWSRFSDBFiles() {
	return __NWSRFS_DBFiles;
}

/**
Returns the NWSRFS properties.
@return the NWSRFS properties.
*/
public PropList getNWSRFSProperties() {
	return __NWSRFS_properties;
}

/**
Returns the object holding index values for the preprocessor 
database files PDBLYn and PDBRRS.
@return the object holding index values for the preprocessor 
database files PDBLYn and PDBRRS.
*/
public NWSRFS_PDBINDEX getPDBIndex() {
	try {
		if(_pdbindex == null)
			_pdbindex = readPDBINDEX();
	}
	catch(Exception e) {
		return null;
	}

	return _pdbindex;
}

/**
Returns the object holding index values for the preprocessor parametric 
database file PPPPARMn.
@return the object holding index values for the preprocessor parametric 
database file PPPPARMn.
*/
public NWSRFS_PPPINDEX getPPPIndex() {
	try {
		if(_pppindex == null)
			_pppindex = readPPPINDEX();
	}
	catch(Exception e) {
		return null;
	}
		
	return _pppindex;
}

/**
Returns the object holding index values for the processed
database file PRDTSn.
@return the object holding index values for the processed 
database file PRDTSn.
*/
public NWSRFS_PRDINDEX getPRDIndex() {
	try {
		if(_prdindex == null)
			_prdindex = readPRDINDEX();
	}
	catch(Exception e) {
		return null;
	}
		
	return _prdindex;
}

/**
Returns the time series hashtable.
@return the time series hashtable.
*/
public Hashtable getTSHashtable() {
	return __tsHashtable;
}

/**
Returns the time series Data Type and Logical Unit hashtable.
@return the time series Data Type and Logical Unit hashtable.
*/
public Hashtable getTSDTUHashtable() {
	return __tsDTUHashtable;
}

/**
Initialize the DMI instance.
*/
private void initialize() {
	__fs5FilesLocation = null;
	__isBigEndian = false;
	__NWSRFS_DBFiles = new EndianRandomAccessFile[__dbFileNames.length];
	__NWSRFS_properties = null;
	__tsHashtable = new Hashtable();
	__tsDTUHashtable = new Hashtable();
	__useFS5Files = true;
	_fcrcptr = null;
	_fcsegptr = null;
	_pppindex = null;	
	_prdindex = null;	

	setupDatabaseProperties();
}

/**
Returns the array specifying if database files are open or not.
@return the array specifying if database files are open or not.
*/
public boolean[] isOpen() {
	return __isOpen;
}

/**
Open the NWSRFS processed database files.
@throws Exception if an error occurs while trying to determine database 
endianness 
*/
public void open() 
throws Exception {
	// Test whether the database binary files are big or little endian.
	// Here is a little trick to catch malformed DBs. Since the vast majority
	// of the NWSRFS DB's come from Linux which is little endian, if the
	// check comes back saying the fs5files are big endian we check a 
	// different file just to make sure!!
	checkDatabaseEndianess("FCRCPTR");
	if(getIsBigEndian()) {
		checkDatabaseEndianess("FCSEGPTR");
	}

	// Read in the index files in to the index file static classes from 
	// readGlobalData. Do this only if using fs5files otherwise the 
	// index files cannot be properly isntantiated since it's not known
	// where they exist.
	if (__useFS5Files) {
		readGlobalData();
	}
}

/**
Indicate whether the DMI was opened using FS5Files.
@return true if the DMI was opened using AppsDefaults, false if using a
directory.
*/
public boolean openedWithAppsDefaults ()
{	return __opened_with_AppsDefaults;
}

/**
Parse a record from the FCCARRY binary database file into the associated 
NWSRFS C array. This array is then parsed into the public vectors that this 
class needs. It is possible to read the data from the 
record stream directly into the Vectors but it is possible that the complete 
array may be needed in future development and made protected or possibly public.
@param EDIS an EndianDataInputStream which holds the ByteArray record that holds
the data for the NWSRFS C array from the FCCARRY binary database file.
@param carryoverSlot the slot number that this carryover data holds.
@param segObj the parent NWSRFS_Segment object in which to receive the carryover
data.
@throws Exception if there is an error parsing the carryover record.
*/	
private void parseCarryoverRecord(EndianDataInputStream EDIS, 
NWSRFS_Segment segObj, int carryoverSlot) 
throws Exception
{
	int ICDAY = (int)EDIS.readEndianInt();
	int ICHR = (int)EDIS.readEndianInt();
	int NC = (int)EDIS.readEndianInt();

	int[] LUPTIM = new int[5];
	for (int i = 0; i < 5; i++) {
		LUPTIM[i] = EDIS.readEndianInt();
	}

	// Now fill local variables from the C array portion of stream.
	int nc = segObj.getNC();
	
	char[] charValue = null;
	int[] coOperationPointer = new int[nc];
	int[] opNumber = new int[nc];
	int cIndex = 0;
	int j = 0;
	int nextOP = 0;
	String[] opName = new String[nc];
	String parseChar = null;
	Vector[] coCarryoverValues = new Vector[nc];
	while (cIndex * 5 < nc) {
		try {
			// Field 1 - operation number
			opNumber[cIndex] = (int)EDIS.readEndianInt();
	
			// Field 2 - next operation
			nextOP = (int)EDIS.readEndianInt();

			// Field 3 - operation name
			charValue = new char[8];
			for (j = 0; j < 8; j++) {
				charValue[j] = EDIS.readEndianChar1();
			}
			parseChar = new String(charValue).trim();
			if (parseChar.length() != 0) {
				opName[cIndex] = parseChar;
			}
			else {
				opName[cIndex] = null;
			}

			// Field 4 - co operation pointer
			coOperationPointer[cIndex] = (int)EDIS.readEndianInt();

			// Field 5 - co carryover values
			// This array of floats will be placed directly 
			// into a Vector.
			coCarryoverValues[cIndex] = new Vector();
			for (j = 4; j < nextOP; j++) {
				coCarryoverValues[cIndex].add(
				new Float((float)
					EDIS.readEndianFloat()));
			}
			
			// SAT 8/24/2004 Ok since exceptions are expensive 
			// I will check to see how many bytes
			// are available on the input stream. If less than 36 
			// break rather than wait to hit
			// the EOF with an exception. The MAGIC NUMBER of 36 
			// bytes is three 4 byte Ints, one 8 byte
			// String, and four 4 byte Floats or 12+8+16 = 36!!
			if(EDIS.available() < 36)
				break;
			
		}
		catch (IOException IOe) {
			exceptionCount++;
			// An Exception occured so break.
			break; 
		}

		// This was the last operation and need to break loop. 
		// It should be at j = segObj.NP-1 anyway but do 
		// this as a precaution.
		if (opNumber[cIndex] == -1) {
			break;
		}

		cIndex++;
	}

	EDIS.close();

	NWSRFS_Carryover CO = null;
	for (int i = 0; i < cIndex; i++) {
		// Create new Carryover object
		CO = new NWSRFS_Carryover(segObj);

		// Add values co CO object
		CO.setISEG(segObj.getIDSEG());
		CO.setICDAY(ICDAY);
		CO.setICHR(ICHR);
		CO.setNC(NC);
		CO.setSlotNumber(carryoverSlot);
		for (j = 0; j < 5; j++) {
			CO.setLUPTIM(j, LUPTIM[j]);
		}

		CO.setOpNumber(opNumber[i]);
		CO.setOpName(opName[i]);
		CO.setCoOperationPointer(coOperationPointer[i]);
		CO.setCoCarryoverValues(coCarryoverValues[i]);

		// Add the Carryover to the segment.
		segObj.addCarryover(CO);
	}
}

/**
Take a record from the FCPARAM binary database file and parses the record
into the associated NWSRFS P, T, and TS arrays as class Vectors.
@param EDIS an EndianDataInputStream which holds the ByteArray record that holds
the data for the NWSRFS P, T, and TS arrays from the FCPARAM binary database 
file.
@param segObj the parent NWSRFS_Segment object.
@param deepRead a boolean to determine whether data is read (true) or only the 
header (false), name and number and the time series name and datatype.
@throws Exception when an error occurs trying to parse the operation record.
*/	
protected void parseOperationRecord(EndianDataInputStream EDIS, 
NWSRFS_Segment segObj, boolean deepRead) throws Exception 
{
	// Now mark the EndianDataInputStream for rewinding.
	// TODO (JTS - 2004-08-21)
	// explain the magic number 2
	EDIS.mark(segObj.getNP() + segObj.getNT() + segObj.getNTS() + 2);

	// loop through and parse the P array to fill the arrays 
	// for eventual populating of the Operation object.

	String routine =  "NWSRFS_DMI.parseOperationRecord";
	boolean rcExists = true;
	char[] charValue;
	int np = segObj.getNP();
	//int nt = segObj.getNT();
	int nts= segObj.getNTS();
	int[] opCarryoverPointer = new int[np];
	//int[] opCarryoverPointerCO = new int[np];
	int[] opNumberP = new int[np];
	//int[] opNumberT = new int[np];
	//int[] opParameterArrayPointer = new int[np];
	int i = 0;
	int j = 0;
	int loopCheck = 0;
	int nextOPRecord = 0;
	int opIndex = 0;
	int pIndex = 0;
	int pSize = 0;
	int thisOPRecord = 0;
	//int tIndex = 0;
	int tsExists = 0;
	//int tSize = 0;
	NWSRFS_Operation OP = null;
	NWSRFS_TimeSeries TS = null;
	NWSRFS_TimeSeries TS1 = null;
	String[] opDesc = new String[np];
	String[] opName = new String[np];
	String[] opRedefName = new String[np];
	String[] rcID = new String[np];
	String parseChar = null;
	String parseDT = null;
	String parseTemp = null;
	String parseTS = null;
	Vector[] opTSDT = new Vector[np];
	Vector[] opTSID = new Vector[np];
	Vector poArray = null;

	// Need to catch OutOfMemoryError since when trying to load
	// all of the operations in all segments it's possible to run 
	// out of memory and then this Exception will be thrown.
	// If caught, System.gc() will be called.
	try 
	{

	// Get a vector of Data Types and put into a Vector of Strings! 
	// TODO -- sat 2004-11-24 Should do globally at startup!
	Vector dtVect = DataType.getDataTypesData();
	Vector dtVectString = new Vector();
	for(i = 0; i < dtVect.size(); i++) {
		dtVectString.addElement((String)((DataType)dtVect.elementAt(i))
			.getAbbreviation());
	}
	
	// Now loop through the P array and get the operation parameters. If 
	// there are more than 100 operations in the segment then generally 
	// something is wrong with the FS5Files for that segment definition! 
	// On several sets of FS5Files it has been observed that the only times 
	// the number of ops exceeds 50 is very rarely and only on broken segment 
	// definitions (I.E. where an operation is defined identically 200 times) 
	// does the number of ops exceed 100. If the number ops exceeds 100 here
	// we stop the loop.
	while (pSize < np && pIndex <= 100) 
	{
		try {
		// Allocate Vector arrays
		opTSID[pIndex] = new Vector();
		opTSDT[pIndex] = new Vector();

		// Set thisOPRecord to the current P array value.
		thisOPRecord = nextOPRecord;

		//Field 1 - [type field name here]
		opNumberP[pIndex] = (int)EDIS.readEndianFloat();
		// This was the last operation and need to break loop. It
		// should be at j = segObj.NP-1 anyway but do this as a
		// precaution.
		if (opNumberP[pIndex] == -1) 
		{
			break;
		}

		//Field 2 - [type field name here]
		nextOPRecord = (int)EDIS.readEndianFloat()-1;

		//Field 3 - [type field name here]
		charValue = new char[8];
		for (j = 0; j < 8; j++) 
		{
			charValue[j] = EDIS.readEndianChar1();
		}
		parseChar = new String(charValue).trim();
		if (parseChar.length() != 0) 
		{
			opName[pIndex] = parseChar;
		}
		else 
		{
			opName[pIndex] = null;
		}

		//Field 4 - [type field name here]
		charValue = new char[8];
		for (j = 0; j < 8; j++) 
		{
			charValue[j] = EDIS.readEndianChar1();
		}
		parseChar = new String(charValue).trim();
		if (parseChar.length() != 0) 
		{
			opRedefName[pIndex] = parseChar;
		}
		else 
		{
			opRedefName[pIndex] = null;
		}

		//Field 5 - [type field name here]
		opCarryoverPointer[pIndex] = (int)EDIS.readEndianFloat();

		//Field 6- - [type field name here]
		// The rating curve name  and tsIDs will be found here
		// pull them out if they exist. Place all strings of the
		// PO array into a vector for parsing later.
		poArray = new Vector();
		// TODO (JTS - 2004-08-21)
		// explain the magic number 7
		for (j = thisOPRecord + 7; j < nextOPRecord; j++) 
		{
			charValue = new char[4];
			for (i = 0; i < 4; i++) 
			{
				charValue[i] = EDIS.readEndianChar1();
			}

			// Hold the first 4 bytes in a
			// temp String
			parseChar = new String(charValue).trim();

			// Now put the string into a Vector
			poArray.add(parseChar);
		}

		// Now parse the PO array Vector for RC and TSIDs
		opIndex = 0;
		for (j = 0; j < poArray.size(); j++) 
		{

		// now do special formatting for Mean-Q, LAG/K, ADD/SUB, 
		// Change-T op since it does not follow the way every other
		// operation seems to be formatted.
			if (opNumberP[pIndex] == __OP_MEAN_Q || 
				opNumberP[pIndex] == __OP_LAG_K ||
				opNumberP[pIndex] == __OP_ADD_SUB || 
				opNumberP[pIndex] == __OP_CHANGE_T ) 
			{
				// Always skip array elements 0
				// Since for these operations the 0
				// element always contains a NULL.
				if (j == 0) 
				{
					parseChar = "";
					parseTS = "";
					tsExists = 0;
					loopCheck = 0;
					rcID[pIndex] = null;
					continue;
				}

				// This operation has no description or
				// RC it just goes into TSIDs.
				// Get the TS identifiers for the operation
				// Check to see if the value in the PO
				// array are a true string if so it is a
				// most likely a TS name
				if (StringUtil.isASCII(
					(String)poArray.elementAt(j))) 
				{
					// Hold the 4 bytes in a
					// temp String
					parseTemp = 
					new String(
						(String)poArray.elementAt(j));
					parseTemp = parseTemp.trim();
					tsExists++;

					// TODO (JTS - 2004-08-21)
					// what do the different values of 
					// tsExists mean??

					if (tsExists == 1 
						&& parseTemp != null
						&& parseTemp.length() != 0) 
					{
						// TSID part 1
						parseTS = parseTemp;
						parseTS = parseTS.trim();
						loopCheck = j;
					}
					else if (tsExists == 2 
						&& parseTemp != null
						&& parseTemp.length() != 0) 
					{
						 // TSID part 2
						parseTS += parseTemp;
						parseTS = 
							parseTS.trim();
					}
					else if (tsExists == 3 
						&& parseTemp != null
						&& parseTemp.length() != 0) 
					{
					 	// TS DataType
						parseDT = parseTemp;
						parseDT = 
							parseDT.trim();
					}
				}

				// This is the tough part: If three 
				// consecutive array elements are valid
				// ASCII strings then it is a TSID and
				// DataType code. The TSID forms two
				// parts and the DataType forms one.
				// The tsExists int above is used to
				// determine which part is which and
				// the loopCheck is used to make sure
				// they are consecutive elements.
				if (loopCheck == j-2 && tsExists == 3
					&& parseTS != null
					&& parseTS.length() != 0
					&& parseDT != null
					&& parseDT.length() != 0) 
				{
					opTSID[pIndex].add(
						parseTS);
					opTSDT[pIndex].add(
						parseDT);
					parseTS = "";
					parseDT = "";
					tsExists = 0;
				}
				else if (loopCheck < j 
					&& tsExists == 1) 
				{
					parseTS = "";
					parseDT = "";
					tsExists = 0;
				}
				else if (loopCheck < j-1 
					&& tsExists == 2) 
				{
					parseTS = "";
					parseDT = "";
					tsExists = 0;
				}
				else if (loopCheck < j-2 
					&& tsExists == 3) 
				{
					parseTS = "";
					parseDT = "";
					tsExists = 0;
				}
				else if (tsExists > 3) 
				{
					parseTS = "";
					parseDT = "";
					tsExists = 0;
				}

				continue;
			}

			// now do special formatting for PLOT-TUL op
			// since it does not follow the way every other
			// operation seems to be formatted.		
			if (opNumberP[pIndex] == __OP_PLOT_TUL) 
			{
//Message.printWarning(2,routine,"PO Array at "+j+" = "+(String)poArray.elementAt(j));

				// Always skip array elements 0-19
				// Since element 0 - 19 contain non useful
				// data.
				if (j <= 19) 
				{
					parseChar = "";
					parseTS = "";
					tsExists = 0;
					loopCheck = 0;
					rcID[pIndex] = null;
					continue;
				}
//				else if ((j == 20 || j == 21) && deepRead)
				else if (j == 20 || j == 21)
				{
					// If this op has a rating curve it 
					// will be here at array element 20
					// Get the rating curve identifier 
					// if it exists
					rcExists = false;
					if (StringUtil.isASCII(
						(String)poArray.elementAt(j))) 
					{
						// Hold the 4 bytes in a
						// temp String
						parseTemp = 
						new String((String)poArray.
							elementAt(j));
						parseTemp = parseTemp.trim();

						parseChar += parseTemp;
						parseChar = parseChar.trim();
						rcExists = true;
					}

					if (j == 21 && rcExists && 
						parseChar.indexOf(' ') < 0 &&
						parseChar.length() > 2) 
					{
						rcID[pIndex] = parseChar;
						parseChar = "";
					}
					else if (j == 21) 
					{
						parseChar = "";
					}
				}
//				else if ((j >= 25 && j < 40) && deepRead)
				else if (j >= 25 && j < 40)
				{
					// Array Elements 25 - 39 holds the 
					// operation description!
					// At element 39 stop. Get the operation 
					// description
					if (StringUtil.isASCII(
					(String)poArray.elementAt(j))) 
					{
						// Hold the 4 bytes in a
						// temp String
						parseTemp = 
						new String((String)poArray.
						elementAt(j));
						parseTemp = parseTemp.trim();

						parseChar += parseTemp;
						parseChar = parseChar.trim();
					}

					if (j == 39) 
					{
						opDesc[pIndex] = parseChar;
						parseChar = "";
					}
				}
				else 
				{
					// Get the TS identifiers for the 
					// operation
					// Check to see if the value in the PO
					// array are a true string if so it is a
					// most likely a TS name
					if (StringUtil.isASCII(
						(String)poArray.elementAt(j))) 
					{
						// Hold the 4 bytes in a
						// temp String
						parseTemp = 
							new String(
							(String)poArray.
							elementAt(j));
						parseTemp = parseTemp.trim();

						// It seems that if parseTemp
						// equals "BEFORE" then need
						// to skip.
						if (parseTemp.equalsIgnoreCase(
							"BEFO")) 
						{
							continue;
						}

						// Time Series exists
						tsExists++;

						if (tsExists == 1 
						    && parseTemp != null
						    && parseTemp.length() != 0)
						{
						    	// TSID part 1
							parseTS = parseTemp;
							parseTS = 
								parseTS.trim();
							loopCheck = j;
						}
						else if (tsExists == 2 
						    && parseTemp != null
						    && parseTemp.length() != 0)
						{
						 	// TSID part 2
							parseTS += parseTemp;
							parseTS = 
								parseTS.trim();
						}
						else if (tsExists == 3 
						    && parseTemp != null
						    && parseTemp.length() !=0) 
						{
							// TS DataType
							parseDT = parseTemp;
							parseDT = 
								parseDT.trim();
						}
					}

					// This is the tough part: If three 
					// consecutive array elements are valid
					// ASCII strings then it is a TSID and
					// DataType code. The TSID forms two
					// parts and the DataType forms one.
					// The tsExists int above is used to
					// determine which part is which and
					// the loopCheck is used to make sure
					// they are consecutive elements.
					// We need to do a check on the
					// Data Type to see if it is a
					// real data type. If so we add
					// to the Vector otherwise just
					// continue!
					if (loopCheck == j-2 && tsExists == 3 
						&& parseTS != null
						&& parseTS.length() != 0
						&& parseDT != null
						&& parseDT.length() != 0
						&& StringUtil.indexOf(
						   dtVectString,parseDT) >= 0) 
					{
						opTSID[pIndex].add(parseTS);
						opTSDT[pIndex].add(parseDT);

						parseTS = "";
						parseDT = "";
						tsExists = 0;

						// If get an OP need 
						// to skip 10 array spots
						j += 10;
					}
					else if (loopCheck < j 
						&& tsExists == 1) 
					{
						parseTS = "";
						parseDT = "";
						tsExists = 0;
					}
					else if (loopCheck < j-1 
						&& tsExists == 2) 
					{
						parseTS = "";
						parseDT = "";
						tsExists = 0;
					}
					else if (loopCheck < j-2 
						&& tsExists == 3) 
					{
						parseTS = "";
						parseDT = "";
						tsExists = 0;
					}
					else if (tsExists > 3) 
					{
						parseTS = "";
						parseDT = "";
						tsExists = 0;
					}
				}

				continue;
			}

			// now do special formatting for MERGE-TS op
			// since it does not follow the way every other
			// operation seems to be formatted.
			if (opNumberP[pIndex] == __OP_MERGE_TS) 
			{
				// Always skip array elements 0
				// since element 0 is NULL.
				if (j == 0) 
				{
					parseChar = "";
					parseTS = "";
					tsExists = 0;
					loopCheck = 0;
					rcID[pIndex] = null;
					continue;
				}

				// This operation has no description or
				// RC it just goes into TSIDs.
				// Get the TS identifiers for the operation
				// Check to see if the value in the PO
				// array are a true string if so it is a
				// most likely a TS name
				if (StringUtil.isASCII(
					(String)poArray.elementAt(j))) 
				{
					// Hold the 4 bytes in a
					// temp String
					parseTemp = new String(
						(String)poArray.elementAt(j));
					parseTemp = parseTemp.trim();
					tsExists++;
					// TODO (JTS - 2004-08-21)
					// what do the different values of 
					// tsExists mean??

					if (tsExists == 1 
						&& parseTemp != null
						&& parseTemp.length() != 0) 
					{
					 	// TSID part 1
						parseTS = parseTemp;
						parseTS = parseTS.trim();
						loopCheck = j;
					}
					else if (tsExists == 2 
						&& parseTemp != null
						&& parseTemp.length() != 0) 
					{
						// TSID part 2
						parseTS += parseTemp;
						parseTS = parseTS.trim();
					}
					else if (tsExists == 3 
						&& parseTemp != null
						&& parseTemp.length() != 0) 
					{
						// TS DataType
						parseDT = parseTemp;
						parseDT = parseDT.trim();
					}
				}

				// This is the tough part. If three 
				// consecutive array elements are valid
				// ASCII strings then it is a TSID and
				// DataType code. The TSID forms two
				// parts and the DataType forms one.
				// The tsExists int above is used to
				// determine which part is which and
				// the loopCheck is used to make sure
				// they are consecutive elements.
				if (loopCheck == j-2 && tsExists == 3 
					&& parseTS != null
					&& parseTS.length() != 0
					&& parseDT != null
					&& parseDT.length() != 0) 
				{
					opTSID[pIndex].add(parseTS);
					opTSDT[pIndex].add(parseDT);
					parseTS = "";
					parseDT = "";
					tsExists = 0;

					// Skip one array spot
					// TODO (JTS - 2004-08-19)
					// explain WHY
					j++;
				}
				else if (loopCheck < j && tsExists == 1) 
				{
					parseTS = "";
					parseDT = "";
					tsExists = 0;
				}
				else if (loopCheck < j-1 && tsExists == 2) 
				{
					parseTS = "";
					parseDT = "";
					tsExists = 0;
				}
				else if (loopCheck < j-2 && tsExists == 3) 
				{
					parseTS = "";
					parseDT = "";
					tsExists = 0;
				}
				else if (tsExists > 3) 
				{
					parseTS = "";
					parseDT = "";
					tsExists = 0;
				}

				continue;
			}

			// now do special formatting for FFG op
			// since it does not follow the way every other
			// operation seems to be formatted
			if (opNumberP[pIndex] == __OP_FFG) 
			{
				// No TS so just continue
				// Continue so it does not fall through
				rcID[pIndex] = null;
				continue;
			}

			// Most of the OPs have the same format so below
			// is the general format for operations.
			if (j == 0) 
			{
				// First element always skip
				// Since element 0 is NULL.
				// explain WHY
				parseChar = "";
				parseTS = "";
				tsExists = 0;
				loopCheck = 0;
				rcID[pIndex] = null;
				continue;
			}
			else if ((j <= 5) && deepRead) 
			{
			 	// The 1-5 element is OP desc
				// Get the operation description
				if (StringUtil.isASCII(
					(String)poArray.elementAt(j))) 
				{
					// Hold the 4 bytes in a
					// temp String
					parseTemp = new String(
						(String)poArray.elementAt(j));
					parseTemp = parseTemp.trim();

					parseChar += parseTemp;
					parseChar = parseChar.trim();
				}

				if (j == 5) 
				{
					opDesc[pIndex] = parseChar;
					parseChar = "";
				}
			}
			else if ((j == 15 || j == 16) && 
				opNumberP[pIndex] == __OP_STAGE_Q) 
//				opNumberP[pIndex] == __OP_STAGE_Q && deepRead) 
			{
				// For StageQ array elements 15 and 16 hold the 
				// RCID. If a RC exists it will be here
				// Get the rating curve identifier if it exists
				rcExists = false;
				if (StringUtil.isASCII(
					(String)poArray.elementAt(j))) 
				{
					// Hold the 4 bytes in a
					// temp String
					parseTemp = new String(
						(String)poArray.elementAt(j));
					parseTemp = parseTemp.trim();

					parseChar += parseTemp;
					parseChar = parseChar.trim();
					rcExists = true;
				}

				if (j == 16 && rcExists) 
				{
					rcID[pIndex] = parseChar;
					parseChar = "";
				}
				else if (j == 16) 
				{
					parseChar = "";
				}
			}
			else 
			{
				// check for TSIDs and TS DataTypes
				// Get the TS identifiers for the operation
				// Check to see if the value in the PO
				// array are a true string if so it is a
				// most likely a TS name
				if (StringUtil.isASCII(
					(String)poArray.elementAt(j))) 
				{
					// Hold the 4 bytes in a
					// temp String
					parseTemp = new String(
						(String)poArray.elementAt(j));
					parseTemp = parseTemp.trim();
					tsExists++;
//Message.printStatus(1, "", "ParseTemp \"" + parseTemp + "\" (" 
//	+ tsExists + ")");
					if (tsExists == 1 
						&& parseTemp != null
						&& parseTemp.length() != 0) 
					{
						// TSID part 1
						parseTS = parseTemp;
						parseTS = parseTS.trim();
						loopCheck = j;
					}
					else if (tsExists == 2 
						&& parseTemp != null
						&& parseTemp.length() != 0) 
					{
						// TSID part 2
						parseTS += parseTemp;
						parseTS = parseTS.trim();
					}
					else if (tsExists == 3 
						&& parseTemp != null 
						&& parseTemp.length() != 0) 
					{
						// TS DataType
						parseDT = parseTemp;
						parseDT = parseDT.trim();
					}
				}

				// This is the tough part: If three 
				// consecutive array elements are valid
				// ASCII strings then it is a TSID and
				// DataType code. The TSID forms two
				// parts and the DataType forms one.
				// The tsExists int above is used to
				// determine which part is which and
				// the loopCheck is used to make sure
				// they are consecutive elements.
				if (loopCheck == j-2 && tsExists == 3 
					&& parseTS != null 
					&& parseTS.length() != 0
					&& parseDT != null
					&& parseDT.length() != 0) 
				{
//Message.printStatus(1, "", "" + parseTS + "  " + parseDT + " " + pIndex);
					opTSID[pIndex].add(parseTS);
					opTSDT[pIndex].add(parseDT);
					parseTS = "";
					parseDT = "";
					tsExists = 0;
				}
				else if (loopCheck < j && tsExists == 1) 
				{
					parseTS = "";
					parseDT = "";
					tsExists = 0;
				}
				else if (loopCheck < j-1 && tsExists == 2) 
				{
					parseTS = "";
					parseDT = "";
					tsExists = 0;
				}
				else if (loopCheck < j-2 && tsExists == 3) 
				{
					parseTS = "";
					parseDT = "";
					tsExists = 0;
				}
				else if (tsExists > 3) 
				{
					parseTS = "";
					parseDT = "";
					tsExists = 0;
				}
			}

			opIndex++;
		}
		}
		catch (Exception e) 
		{
			parseOperationExceptionCount++;
			// Should never get here unless an error happened
//			Message.printWarning(2,routine,"At the exception for P"+
//				" Array loop: "+parseOperationExceptionCount);
			Message.printWarning(2,routine,e);
//Message.printDebug(1,routine,IOe);
			break; // At end of stream.
		}

		// Determine index and size for next operation loop
		// Always add 7 to skip the last seven bytes of the array 
		// which contains spurious data
		pSize += 7 + opIndex;

/*
if (pIndex == 3 || pIndex == 4 || pIndex == 5) {
	Message.printStatus(1, "", "\"" + opTSID[pIndex] + "\"");
	Message.printStatus(1, "", "\"" + opTSDT[pIndex] + "\"");
}
*/
		// Update the P array index.
		pIndex++;
		}

		// Rewind the EndianDataInputStream so that a seek to proper
		// location can be done.
		EDIS.reset();
		EDIS.skipBytes(np * __WORDSIZE);

		// Now loop through and parse the T array to fill the Vectors
		nextOPRecord = 0;
		// TODO SAM 2008-04-07 Evaluate need
		//tIndex = 0;
		//tSize = 0;
		opIndex = 0;

// TODO (SAT 2004-08-24) Why even read in the T array? 
// It does not add any thing to the the operation data and just 
// consumes resources. I will remove it and see if things
// preformance improves.
		// Need to catch OutOfMemoryError since if try to load
		// all of the Operations in all segments.... It could use
		// the memroy available and throw this exception. Will
		// garbage collect if this is caught.

		// Now loop through the T array and get the operation records. 
		// If there are more than 100 operations in the segment then 
		// generally something is wrong with the FS5Files for that 
		// segment definition! On several sets of FS5Files it has been 
		// observed that the only times the number of ops exceeds 50 is 
		// very rarely and  only on broken segment definitions (I.E. 
		// where an operation is defined identically 200 times) does 
		// the number of ops exceed 100. If the number ops exceeds 100 
		// here we stop the loop.
/*		while (tSize < nt && tIndex <= 100) 
		{
			// Set thisOPRecord to the current T array value.
			thisOPRecord = nextOPRecord;

			try 
			{
			//Field 1 The Operation number 
			opNumberT[tIndex] = (int)EDIS.readEndianInt();		

			// This was the last operation and need to 
			// break loop. It should be at j = segObj.NT-1 
			// anyway but do this as a precaution.
			if (opNumberT[tIndex] == -1) 
			{
				break;
			}

			//Field 2 - [type field name here]
			nextOPRecord = (int)EDIS.readEndianInt();

			// Check to see if Operation has additional 
			// information like a "PO" array.
			// Check to see where the "PO" array starts
			opParameterArrayPointer[tIndex] 
				= (int)EDIS.readEndianInt();

			// Get the next pointer value
			opCarryoverPointerCO[tIndex] 
				= (int)EDIS.readEndianInt();

			// This array of floats will be skipped 
			// to end of array position.
			opIndex = 0;

			for (i = thisOPRecord + 4; i < nextOPRecord; i ++) 
			{
				EDIS.readEndianInt();
				opIndex++;
			}
			}
			catch (IOException IOe) 
			{
				parseOperationExceptionCount++;
				// Should never get here unless something is wrong
			 	// At end of stream.
Message.printStatus(1,routine,"At the exception for T Array loop: "+parseOperationExceptionCount);
Message.printStatus(1,routine,IOe.getMessage());
Message.printDebug(1,routine,IOe);
				break;
			}

			// Determine index and size for next operation loop
			tSize += 4+opIndex;
			tIndex++;
		}
*/
		// Now loop through the arrays and create an Operation 
		// objects to load into the segment. Also a limit is set 
		// to the number of Operations allowed per segment. This 
		// is done for memory management sake.
		for (i = 0; i < pIndex; i++) 
		{
			// Create a new Operations object
			try 
			{
			// If the op number from database exceeds what we expect
			// set opnum to 0 or "none" operation
			if ( opNumberP[i] > 53 ) {
                Message.printWarning(2, routine, "Parsing operations does not handle opnum>53. "
                        + "Trying to parse " + opNumberP[i] + " " + opName[i] );
				opNumberP[i] = 0;
            }
			
			OP = new NWSRFS_Operation( __operationNames[opNumberP[i]], opName[i],segObj);

			// Fill public data values for the Operation.
			OP.setOpNumber(opNumberP[i]);
			OP.setOpTypeName(__operationNames[opNumberP[i]]);
			OP.setOpName(opName[i]);
			if (rcID[i] != null && rcID[i].length() > 0) 
			{
				OP.addRCID(rcID[i]);
			}

			// Check to see if just reading the header. If so
			// do not fill the entire Operation object.
			if (deepRead) 
			{
				OP.setOpRedefName(opRedefName[i]);
//				OP.setOpCarryoverPointer(opCarryoverPointer[i]);

				// Create the Rating Curve object and add it
				// to this object
				if (rcID[i] != null) 
				{
					OP.addRatingCurve(
					      readRatingCurve(rcID[i]));
				}

				// Now loop through the t array values and put 
				// into the proper Operation.
// TODO (SAT 2004-08-24) Getting rid of T array
/*				for (j = 0;j < tIndex; j++) 
				{
					// Check to see Operation numbers 
					// are the same.
					// set Operation values and break if so.
					if (opNumberT[j] == opNumberP[i]) 
					{
						OP.setOpParameterArrayPointer(
						    opParameterArrayPointer[j]);
						OP.setOpCarryoverPointerCO( 
						       opCarryoverPointerCO[j]);
						break;
					}
				}
*/
			}

			segObj.addOperation(OP);
			}
			catch (Exception e) 
			{
				// Should never get here
				parseOperationExceptionCount++;
//Message.printStatus(1,routine,"At the exception for add Operation loop: "+parseOperationExceptionCount);
//Message.printStatus(1,routine,e.getMessage());
//Message.printDebug(1,routine,e);
			}
		}

	} 
	catch (OutOfMemoryError OOMe) 
	{
		// Create a RunTime object to call GC
		// TODO (JTS - 2004-08-19)
		// this does NOTHING -- garbage collection is not guaranteed
		// to run at all.
		Runtime.getRuntime().gc();
	}

	// Rewind the EndianDataInputStream so that a seek to proper
	// location can be done.
	EDIS.reset();
	EDIS.skipBytes(segObj.getNP() * __WORDSIZE 
		+ segObj.getNT() * __WORDSIZE);

	// Now loop through and parse the TS array to fill the Vectors
	boolean[] tsDataIndicator = new boolean[nts];
	int[] tsDTInterval = new int[nts];
	int[] tsExtNVAL = new int[nts];
	int[] tsIndicator = new int[nts];
	int[] tsNADD = new int[nts];
	int[] tsNVAL = new int[nts];
	int[] tsTimeseriesPointer = new int[nts];
	int[] tsWhenWriteIndicator = new int[nts];
	String[] tsDataFileCode = new String[nts];
	String[] tsDataType = new String[nts];	
	String[] tsID = new String[nts];
	Vector[] tsAddInformation = new Vector[nts];
	Vector[] tsExtLocInformation = new Vector[nts];
	nextOPRecord = 0;
	int m = 0;
	int opIndex1 = 0;
	int opIndex2 = 0;
	int tsIndex = 0;
	int tsSize = 0;
	// TODO SAM 2008-04-07 Evaluate need
	//boolean duplicate = false;

	// Need to catch OutOfMemoryError since if try to load
	// all of the Operations in all segments.... It could use
	// the memroy available and throw this exception. Will
	// garbage collect if this is caught.
	
	// Now loop through the TS array and get the TS parameters. If there are
	// more than 100 time series in the operation then generally something 
	// is wrong with the FS5Files for that segment definition! On several 
	// sets of FS5Files it has been observed that the only times the number 
	// of ts exceeds 50 is very rarely and only on broken segment definitions 
	// (I.E. where an time series is defined identically 200 times) does the 
	// number of ts exceed 100. If the number ts exceeds 100 here we stop 
	// the loop.
	try 
	{
	while (tsSize < nts && tsIndex <= 100) 
	{
		//duplicate = false;
		// Set thisOPRecord to the current P array value.
		thisOPRecord = nextOPRecord;

		// TimeSeries Vectors
		try 
		{
		//Field 1 - [type field name here]
		tsIndicator[tsIndex] = (int)EDIS.readEndianFloat();

		// If tsIndicator is 0 then at end of TS array so break.
		if (tsIndicator[tsIndex] == 0) 
		{
			break;
		}

		//Field 2 - [type field name here]
		nextOPRecord = (int)EDIS.readEndianFloat() - 1;

		//Field 3 - [type field name here]
		charValue = new char[8];
		for (j = 0; j < 8; j++) 
		{
			charValue[j] = EDIS.readEndianChar1();
		}
		parseChar = new String(charValue).trim();
		if (parseChar.length() != 0) 
		{
			tsID[tsIndex] = parseChar;
		}
		else 
		{
			tsID[tsIndex] = null;
		}

		//Field 4 - [type field name here]
		charValue = new char[4];
		for (j = 0; j < 4; j++) 
		{
			charValue[j] = EDIS.readEndianChar1();
		}
		parseChar = new String(charValue).trim();
		if (parseChar.length() != 0) 
		{
			tsDataType[tsIndex] = parseChar;
		}
		else 
		{
			tsDataType[tsIndex] = null;
		}

//Message.printStatus(1, "", "TSID[" + tsIndex + "]: \"" + tsID[tsIndex]
//	+ "\" \"" + tsDataType[tsIndex] + "\"");

		// check to make sure this is not a duplicate TSID
		// TODO TS BUG (JTS - 2004-08-20)
		// I added this
		// TODO (SAT - 2004-08-24)
		// No this must not be done!! It will keep timeseries 
		// with different intervals from showing up!!
//		for (int k = 0; k < tsIndex; k++) {
//			if (tsDataType[k].equalsIgnoreCase(tsDataType[tsIndex])
//				&& tsID[k].equalsIgnoreCase(tsID[tsIndex])) {
//Message.printStatus(1, "", "    ** DUP[" + tsIndex + "]: \"" + tsID[tsIndex]
//	+ "\" \"" + tsDataType[tsIndex] + "\"");
//				duplicate = true;
//			}
//		}
	
		//Field 5 - [type field name here]
		tsDTInterval[tsIndex] = (int)EDIS.readEndianFloat();

// Definately TODO (SAT 8-27-2004) I am running out of time right now to
// fix this but I need to figure out what I can skip here. Somehow my choice
// broke the code.....
//		if(deepRead) {
			//Field 6 - [type field name here]
			tsNVAL[tsIndex] = (int)EDIS.readEndianFloat();

			//Field 7 - [type field name here]
			tsTimeseriesPointer[tsIndex] = (int)EDIS.readEndianFloat();

			//Field 8 - [type field name here]
			if ((int)EDIS.readEndianFloat() == 1) 
			{
				tsDataIndicator[tsIndex] = true;
			}
			else 
			{
				tsDataIndicator[tsIndex] = false;
			}

			// Field 9- This is variable and depends on Timeseries 
			// type. Now if the TimeSeries indicator shows it is an 
			// Internal TS Then have to fill Vectors differently.
		
// TODO (JTS - 2004-08-21)
// explain the magic number 4
			
			if (tsIndicator[tsIndex] == 4) 
			{
				tsDataFileCode[tsIndex] = null;
				tsWhenWriteIndicator[tsIndex] = -1;
				tsExtNVAL[tsIndex] = -1;
				tsExtLocInformation[tsIndex] = new Vector();
				tsExtLocInformation[tsIndex].add(null);
				tsNADD[tsIndex] = (int)EDIS.readEndianFloat();

				// This Array of floats will be placed directly 
				// into a Vector.
				tsAddInformation[tsIndex] = new Vector();
				opIndex1 = 0;
// TODO (JTS - 2004-08-21)
// explain the magic number 10
				for (j = thisOPRecord + 10; j < nextOPRecord; j++) 
				{
					tsAddInformation[tsIndex].add(
						new Float(EDIS.readEndianFloat()));
					opIndex1++;
				}
			}
			else 
			{
				charValue = new char[4];
				for (j = 0; j < 4; j++) 
				{
					charValue[j] = EDIS.readEndianChar1();
				}

				parseChar = new String(charValue).trim();
 
				if (parseChar.length() != 0) 
				{
					tsDataFileCode[tsIndex] = parseChar;
				}
				else 
				{
					tsDataFileCode[tsIndex] = null;
				}

				tsWhenWriteIndicator[tsIndex] = 
					(int)EDIS.readEndianFloat();

				tsExtNVAL[tsIndex] = (int)EDIS.readEndianFloat();

				// This array of floats will be placed directly 
				// into a Vector.

				tsExtLocInformation[tsIndex] = new Vector();
				opIndex1 = 0;

				for (j = 0; j < tsExtNVAL[tsIndex]; j++) 
				{
					// TODO (JTS - 2004-08-19)
					// the next line needs an explanation
					if ((j - 2) <= 0) 
					{
						int csize = 4;
						if (j == 0) 
						{
							csize = 8;
							j++;
						}

						charValue = new char[csize];
						for (int k = 0; k < csize; k++) 
						{
							charValue[k] 
							= EDIS.readEndianChar1();
						}

						parseChar 
						= new String(charValue).trim();

						if (parseChar.length() != 0) 
						{
							tsExtLocInformation[tsIndex].
							add(parseChar);
						}
						else 
						{
							tsExtLocInformation[tsIndex].
							add(null);
						}
					}
					else 
					{
						tsExtLocInformation[tsIndex].add(
							new Float(
							EDIS.readEndianFloat()));
					}
					opIndex1++;
				}

				tsNADD[tsIndex] = (int)EDIS.readEndianFloat();

				// This array of floats will be placed directly 
				// into a Vector.

				tsAddInformation[tsIndex] = new Vector();
				opIndex2 = 0;
// TODO (JTS - 2004-08-21)
// explain the magic number 13			
				for (j = (thisOPRecord + 13 + tsExtNVAL[tsIndex]);
					j<nextOPRecord; j++) 
				{
					tsAddInformation[tsIndex].add(
						new Float(EDIS.readEndianFloat()));
					opIndex1++;
				}
			}
//		}
		}
		catch (IOException IOe) 
		{
			parseOperationExceptionCount++;
			// Should never get here
			// At end of stream.
//Message.printStatus(1,routine,"At the exception for TS Array loop: "+parseOperationExceptionCount);
//Message.printStatus(1,routine,IOe.getMessage());
//Message.printDebug(1,routine,IOe);
			break;
		}

// TODO (JTS - 2004-08-21)
// explain the magic number 12

		// Determine index and size for next operation loop
		tsSize += 12 + opIndex1 + opIndex2;
		tsIndex++;

	}

	// Now loop through the arrays and create an TimeSeries objects 
	// to load into the segment. Also a limit to the number of TS 
	// objects per segment is enforced.

	// Now loop through the TS array and get the TS parameters. If there are
	// more than 100 time series in the operation then generally something 
	// is wrong with the FS5Files for that segment definition! On several 
	// sets of FS5Files it has been observed that the only times the number 
	// of ts exceeds 50 is very rarely and only on broken segment definitions 
	// (I.E. where an time series is defined identically 200 times) does the 
	// number of ts exceed 100. If the number ts exceeds 100 here we stop 
	// the loop.
	String tsIdentCheck;
	int[] tsIntCheck = {1,3,6,12,18,24};
	for (i = 0; i < tsIndex && tsIndex <= 100; i++) 
	{
		// TODO SAT 2004-11-24 Here is a time consuming loop that is
		// very neccessary. Since not all of the Time Series found in op
		// table contain data we need to check to see if the op table
		// made a mistake in the interval! (This was noticed first in
		// TS with MAPX data type data. The TS interval was shown in the
		// OP table as being 6 hour when in reality it was 1 hour data
		// in the PRD!! I loop through 1,3,6,12,18, and 24 hour intervals
		// and check to see if data exists. If no data exists I just
		// use what was given. If data exists at a different interval
		// than was specified I use that interval instead!! If data
		// exists at two or more intervals I use the first one. This 
		// should probably not be the default behavior but I do not
		// have budget to fix!! Since it is so time consuming I will
		// only do it on data type MAPX. It should be done on other data
		// types but performance is an issue!!
		if(tsDataType[i].equalsIgnoreCase("MAPX")) {
		for(int k = 0; k < 6; k++) {
			tsIdentCheck = tsID[i]+".NWSRFS."+tsDataType[i]+"."+
				tsIntCheck[k]+"Hour~NWSRFS_FS5Files~" + getFS5FilesLocation();
			if(readTimeSeries(tsIdentCheck,null,null,null,false) !=
				null) {
					tsDTInterval[i] = tsIntCheck[k];
					break;
			}
		}
		}
		
		// Need to have the data interval Deep read or shallow
		if (tsDTInterval[i] < 1) 
		{
			TS.setTSDTInterval(segObj.getMINDT());
			tsDTInterval[i] = segObj.getMINDT();
		}

		// Create a new TimeSeries Object
		TS = new NWSRFS_TimeSeries(tsID[i],tsDataType[i], tsDTInterval[i]);

		// Check to see if the header is being read.  If so,
		// skip to the next timeseries and continue.
		if (deepRead) 
		{
			// NOTE ***********************************************
			// 
			// Get the Process database TSID. This will fix a huge bug.
			// In some cases the TSID in the PRDINDEX and PRDTSn 
			// binary files is different than it is in the Operations 
			// table. Until now RTi did not know ths mapping of how 
			// the NWSRFS system could find the timeseries data and map 
			// it to the Operations table. Now it is known that it is 
			// mapped via the 12th array element of the TS array from the
			// FCPARM binary file. This is true if the data code is 
			// "FPDB". Most of the time the TSIDs are the same but to 
			// handle the small number of cases use the external 
			// location information if the condition is true.
			if (tsDataFileCode[i] != null 
				&& tsDataFileCode[i].equalsIgnoreCase("FPDB")) 
			{
				TS.setTSDataFileCode(tsDataFileCode[i]);

				// Set tsID to be external location which will 
				// be at element 0 if the above conditions are true

				if (tsExtNVAL[i] > 0) 
				{
					TS.setTSID(
						(String)tsExtLocInformation[i]
						.elementAt(0));
				}
			}

			// Fill public data values for the Operation.
			TS.setTSIndicator(tsIndicator[i]);
			TS.setTSNVAL(tsNVAL[i]);
			TS.setTSTimeseriesPointer(tsTimeseriesPointer[i]);
			TS.setHasData(tsDataIndicator[i]);
			TS.setTSWhenWriteIndicator(tsWhenWriteIndicator[i]);
			TS.setTSExtNVAL(tsExtNVAL[i]);
			TS.setTSExtLocInformation(tsExtLocInformation[i]);
			TS.setTSNADD(tsNADD[i]);
			TS.setTSAddInformation(tsAddInformation[i]);
		}

		// Add TimeSeries Info into the Operation. Loop through 
		// operations defined above and match with tsID[i] to 
		// get operation to put this TS in. This will be slow 
		// but there is no other way to do it

		// Now check to make sure that pIndex does not exceed the actual 
		// length of the vector arrays
		if(opTSID == null || opTSDT == null)
		{
			continue; // continue the outerloop if either of the vector arrays are null!
		}
		else if(Array.getLength(opTSID) < pIndex - 1)
		{
			pIndex = Array.getLength(opTSID);
		}
		else if(Array.getLength(opTSDT) < pIndex - 1)
		{
			pIndex = Array.getLength(opTSDT);
		}

		// Do the pIndex loop
		for (j = 0;j < pIndex; j++) {
			// Compare tsID[i] to opTSID.elementAt(j) and
			// tsDataType[i] to opTSDT.elementAt(j). If true
			// then add TS to segObj.getOperation(j).
			try 
			{			
			for (m = 0; m < opTSID[j].size(); m++) 
			{
/*
if (j == 3) {
	Message.printStatus(1, "", "OPTSID[" + j + "][" + m + "]: \"" 
		+ opTSID[j].elementAt(m) + "\"  ?:?   \""
		+ tsID[i] + "\"");
	Message.printStatus(1, "", "OPTSDT[" + j + "][" + m + "]: \""
		+ opTSDT[j].elementAt(m) + "\"  ?:?   \""
		+ tsDataType[i] + "\"");
}
*/
				if (tsID[i].equalsIgnoreCase(
					(String)opTSID[j].elementAt(m)) 
					&& tsDataType[i].equalsIgnoreCase(
					(String)opTSDT[j].elementAt(m))) 
				{

//Message.printStatus(1,routine, "Operation Number: "+(segObj.getOperation(j)).getOpNumber()+" adding TS");
//Message.printStatus(1,routine,"TSID = "+TS.getTSID());
//Message.printStatus(1,routine,"i = "+i+" j = "+j+" m = "+m);
					(segObj.getOperation(j)).addTSID(
						TS.getTSID());
					(segObj.getOperation(j)).addTSDT(
						TS.getTSDataType());
					(segObj.getOperation(j)).addTimeSeries(
						TS);
					if (i > 0) 
					{
						// This is done to break 
						// out of outer loop
// TODO TS BUG (JTS - 2004-08-19)
// did this in order to try fixing TS bug in system tree ...
//						j = pIndex; 
					}
					break;
				}
				else if ((segObj.getOperation(j)).getOpNumber()
					== __OP_PLOT_TUL && i == 0) 
				{
				 	// A Plot-Tul operation; only do once.
					// If this is a Plot-Tul operation then
					// the time series needs to be added since
					// the TS in that operation is not included 
					// in the TS array.  Could come back 
					// to haunt me here.
					// TODO (SAT)
					// if gonna be haunted, revisit
					// Create a new TimeSeries Object
//Message.printStatus(1,routine, "PLOT_TUL Operation adding TS");
//Message.printStatus(1,routine,"TSID = "+(String)opTSID[j].elementAt(m));
//Message.printStatus(1,routine,"i = "+i+" j = "+j+" m = "+m);
					TS1 = new NWSRFS_TimeSeries(
						(String)opTSID[j].elementAt(m),
						(String)opTSDT[j].elementAt(m),
						tsDTInterval[i]);

					// Need to have the data interval
					if (tsDTInterval[i] < 1) 
					{
						TS1.setTSDTInterval(
							segObj.getMINDT());
					}

					(segObj.getOperation(j)).
						addTSID(TS1.getTSID());
					(segObj.getOperation(j)).
						addTSDT(TS1.getTSDataType());
					(segObj.getOperation(j)).
						addTimeSeries(TS1);
				}
			} // end of opTSID Vector loop
			}
			catch (Exception e) 
			{
				parseOperationExceptionCount++;
				// Should never get here. Somehow it is throwing an
				// outOfBoundsException!
//Message.printStatus(1,routine,"At the exception for opTSID loop: "+parseOperationExceptionCount);
//Message.printStatus(1,routine,"i = "+i+" j = "+j+" m = "+m);
//Message.printStatus(1,routine,e.getMessage());
//Message.printDebug(1,routine,e);
				pIndex--;
			}

		} // End of pIndex loop
	} // End of tsIndex loop

	} 
	catch (OutOfMemoryError OOMe) 
	{
		// Create a RunTime object to call GC
		// TODO (JTS - 2004-08-19)
		// this does NOTHING -- garbage collection is not guaranteed
		// to run at all.		
		Runtime.getRuntime().gc();
	}
}

/**
Reads from the preprocessed parameteric database using the particular 
parameter type, the ID, and the record number embeded in the Object an
array of values. It then parses that array and places the contents into
the members of the Object. Each parameter type will have the array parsed
in a specific manner. For instance, the RRS parameter array has completely
different elements than the MAP parameter array.  Thus this method has a
BIG LOOP for each parameter type (to be filled out as time and budget
permit) to parse the array.
@param NWSRFS_Object is a generic object type that holds the 
specific object for the parameters for instance RRS is a station parameter
so passed into this routine will an NWSRFS_Station object. On the other hand
MAP is an MAPArea object so an NWSRFS_MAPArea is passed in.
@param paramType is a String holding the specific parameter type requested. An
example would be "RRS" or "MAP".
@param deepRead is a boolean to tell the routine whether or not to deeply
read the data; in otherwords do we get all of the parameters or not from the database.
@return a boolean specifying whether or not the read succeeded or not.
@throws and Exception if anything major goes wrong.
*/
protected boolean parseParametericArray(Object NWSRFS_Object, String paramType,
boolean deepRead) throws Exception
{	// FIXME SAM 2008-04-08 Eliminate excessive casting
	// Need to cast to appropriate object type ASAP to avoid casting in each set/get call
	String routine =  "NWSRFS_DMI.parseParametericArray";
	int i, j, logicalUnitNum=-1, recordNum=-1;
	String pppParamFileName="PPPPARM", parseChar;
	int pppParamIndex=-1, NWRDS, IRECNX;
	char[] charValue;
	EndianDataInputStream EDIS;
	
	// Big if used to get the data for a specific parameter type!
	// Start off with GENL station parameter
	if(paramType.equalsIgnoreCase("GENL")) { // Station Parameter
		// Sometimes the GENL record is not complete and returns NULL
		// on logical unit number. Not sure why this is so we just
		// return false rather than throw a null exception! This has
		// the effect of leaving the station out....
		try {
		logicalUnitNum=((NWSRFS_Station)NWSRFS_Object).getLogicalUnitNum("GENL");
		recordNum=((NWSRFS_Station)NWSRFS_Object).getRecordNum("GENL");
		}
		catch (Exception e) {
			return false;
		}
		
		// Determine the PPPARMn file where n is the logicalUnitNum.
		pppParamFileName += logicalUnitNum;
		
		// Get the file handles, etc. to open the RA file.
		for(i = 17; i < 22; i++) {
			if(__dbFileNames[i].equalsIgnoreCase(pppParamFileName)) {
				pppParamIndex = i;
				break;
			}
		}
		
		// If pppParamIndex = -1 then we had a big problem! The 
		// logicalUnitNum was either > 5 or < 1 so we can not open
		// the correct parameter file to get the data!! Print a message and return false.
		if(pppParamIndex == -1) {
			Message.printStatus(10,routine, "Logical Unit Number for the Station: "+logicalUnitNum+
					". This unit number is not correct and must be between 1 and 5! "+
				"The parameter binary file can not be openned.");
			return false;
		}
		
		// Open the RA file.
		// Check if the the database binary file is open as a Random Access object
		if (!checkRandomAccessFileOpen(pppParamIndex, true)) {
			throw new Exception("Cannot open the " + __dbFileNames[pppParamIndex] + " binary database file");
		}

		// Now read the parameter file to get the parameter data in the PPPPARMn binary file.
		// Read the record at recordNum to get the parameter data.
		__NWSRFS_DBFiles[pppParamIndex].seek(0);
		__NWSRFS_DBFiles[pppParamIndex].seek((recordNum-1)*__byteLength[pppParamIndex]);
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, 4);

		// Start reading in the general parameters.
		// Field 1 - NWRDS
		NWRDS = EDIS.readEndianInt();

		// Close the Stream
		EDIS.close();

		// Read in a new Stream for the remaining fields
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, NWRDS*4);

		// Field 2 -- Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() == 0 || !parseChar.equalsIgnoreCase(((NWSRFS_Station)
			NWSRFS_Object).getID())) {
			Message.printStatus(10,routine, "Reading from PPPPARM "+ logicalUnitNum+
					" file did not produce correct results. The read in identifier is not "+
				"the same as the expected identifier!  The read in ID: "+parseChar+
				" and the expected ID:"+((NWSRFS_Station)NWSRFS_Object).getID());
			return false;
		}
		
		// Field 3 -- parameter type
		charValue = new char[4];
		for (i = 0; i < 4; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() == 0 ||
			!parseChar.equalsIgnoreCase(paramType)) {
			Message.printStatus(10,routine, "Reading from PPPPARM "+ logicalUnitNum+
					" file did not produce correct results. The read in parameter type is not "+
				"the same as the one expected parameter type! The read in paramType: "+parseChar+
				" and the expected parmType:"+paramType);
			return false;
		}
		
		// Field 4 - IRECNX
		IRECNX = checkInt(EDIS.readEndianInt(), 0, 100000, -1);
		
		// Start reading in Station parameters
		// Field 5 - Parameter array version
		EDIS.readEndianFloat();
		
		// Field 6 -- Station Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		// Field 7 -- Station Number
		((NWSRFS_Station)NWSRFS_Object).setStationNum( (int)EDIS.readEndianFloat());
		
		// Field 8 -- Station Description
		charValue = new char[20];
		for (i = 0; i < 20; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() != 0) {
			((NWSRFS_Station)NWSRFS_Object).setDescription(parseChar);
		}
		
		// Field 9 -- Station Elevation
		((NWSRFS_Station)NWSRFS_Object).setElevation(EDIS.readEndianFloat());
		
		// Field 10 -- Station Latitude
		((NWSRFS_Station)NWSRFS_Object).setLatitude(EDIS.readEndianFloat());
		
		// Field 11 -- Station Longitude
		((NWSRFS_Station)NWSRFS_Object).setLongitude(EDIS.readEndianFloat());
		
		// Field 12 -- Station HRAP X coordinate
		((NWSRFS_Station)NWSRFS_Object).setHrapX((int)EDIS.readEndianFloat());
		
		// Field 13 -- Station HRAP Y coordinate
		((NWSRFS_Station)NWSRFS_Object).setHrapY((int)EDIS.readEndianFloat());
		
		// Field 14 -- Station Complete indicator
		((NWSRFS_Station)NWSRFS_Object).setCompleteInd(	(int)EDIS.readEndianFloat());
		
		// Field 15 -- Station Postal Service 2-character code
		charValue = new char[4];
		for (i = 0; i < 4; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() != 0) {
			((NWSRFS_Station)NWSRFS_Object).setPSCode(parseChar);
		}
		
		// Now get additional parameter data if deepRead is specified.
		// Right now this does nothing. As budget is available additional parameters will be defined!
		if(deepRead) {
		}
		
		// Close the EDIS
		EDIS.close();
	}
	else if(paramType.equalsIgnoreCase("PCPN")) { // Station Parameter
		logicalUnitNum=((NWSRFS_Station)NWSRFS_Object).getLogicalUnitNum("PCPN");
		recordNum=((NWSRFS_Station)NWSRFS_Object).getRecordNum("PCPN");
		
		// Determine the PPPARMn file where n is the logicalUnitNum.
		pppParamFileName += logicalUnitNum;
		
		// Get the file handles, etc. to open the RA file.
		for(i = 17; i < 22; i++) {
			if(__dbFileNames[i].equalsIgnoreCase(pppParamFileName)) {
				pppParamIndex = i;
				break;
			}
		}
		
		// If pppParamIndex = -1 then we had a big problem! The 
		// logicalUnitNum was either > 5 or < 1 so we can not open
		// the correct parameter file to get the data!! Print a message and return false.
		if(pppParamIndex == -1) {
			Message.printStatus(10,routine, 
				"Logical Unit Number for the Station: "+ logicalUnitNum+
				". This unit number is not correct and must be between 1 and 5! "+
				"The parameter binary file can not be openned.");
			return false;
		}
		
		// Open the RA file.
		// Check if the the database binary file is open as a
		// Random Access object
		if (!checkRandomAccessFileOpen(pppParamIndex, true)) {
			throw new Exception("Can not open the " + __dbFileNames[pppParamIndex] + 
			" binary database file");
		}

		// Now read the parameter file to get the parameter data in the PPPPARMn binary file.
		// Read the record at recordNum to get the parameter data.
		__NWSRFS_DBFiles[pppParamIndex].seek(0);
		__NWSRFS_DBFiles[pppParamIndex].seek((recordNum-1)*__byteLength[pppParamIndex]);
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, 4);

		// Start reading in the general parameters.
		// Field 1 - NWRDS
		NWRDS = EDIS.readEndianInt();

		// Close the Stream
		EDIS.close();

		// Read in a new Stream for the remaining fields
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, NWRDS*4);

		// Field 2 -- Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() == 0 ||
			!parseChar.equalsIgnoreCase(((NWSRFS_Station)
			NWSRFS_Object).getID())) {
			Message.printStatus(10,routine, "Reading from PPPPARM "+	logicalUnitNum+
					" file did not produce correct results. The read in identifier is not "+
				"the same as the one expected identifier!  The read in ID: "+parseChar+
				" and the expected ID:"+((NWSRFS_Station)NWSRFS_Object).getID());
			return false;
		}
		
		// Field 3 -- parameter type
		charValue = new char[4];
		for (i = 0; i < 4; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() == 0 ||
			!parseChar.equalsIgnoreCase(paramType)) {
			Message.printStatus(10,routine, "Reading from PPPPARM "+	logicalUnitNum+
					" file did not produce correct results. The read in parameter type is not "+
				"the same as the one expected parameter type!  The read in paramType: "+parseChar+
				" and the expected parmType:"+paramType);
			return false;
		}
		
		// Field 4 - IRECNX
		IRECNX = checkInt(EDIS.readEndianInt(), 0, 100000, -1);
		if ( IRECNX < 0 ) {
			// TODO SAM 2008-04-08 Need to evaluate validation
		}
		
		// Start reading in Station parameters
		// Field 5 - PCPN Parameter array version
		checkInt(EDIS.readEndianInt(), 0, 100, -1);
		
		// Field 6 -- Station PCPN Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}

		// Field 7 -- Station PCPN Number
		EDIS.readEndianInt();
		
		// Field 8 -- Station PCPN Description
		charValue = new char[20];
		for (i = 0; i < 20; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		// Field 9 -- Station PCPN Elevation
		EDIS.readEndianFloat();
		
		// Field 10 -- Station PCPN Latitude
		EDIS.readEndianFloat();
		
		// Field 11 -- Station PCPN Longitude
		EDIS.readEndianFloat();
		
		// Field 12 -- Station PCPN HRAP X coordinate
		EDIS.readEndianInt();
		
		// Field 13 -- Station PCPN HRAP Y coordinate
		EDIS.readEndianInt();
		
		// Field 14 -- Station PCPN Processing Code
		((NWSRFS_Station)NWSRFS_Object).setPCPNProcCode((int)EDIS.readEndianFloat());
		
		// Field 15 -- Station PCPN Data Time Interval
		((NWSRFS_Station)NWSRFS_Object).setPCPNDataTimeInt((int)EDIS.readEndianFloat());
		
		// Field 16 -- Station PCPN MDR Box
		((NWSRFS_Station)NWSRFS_Object).setPCPNMDRBox((int)EDIS.readEndianFloat());
		
		// Field 17 -- Station PCPN precip correction factor 1
		((NWSRFS_Station)NWSRFS_Object).setPCPNPrecipCorrect1(	EDIS.readEndianFloat());
		
		// Field 18 -- Station PCPN precip correction factor 2
		((NWSRFS_Station)NWSRFS_Object).setPCPNPrecipCorrect2(EDIS.readEndianFloat());
		
		// Field 19 -- Station PCPN Type of 24 hour precip weights
		((NWSRFS_Station)NWSRFS_Object).setPCPNWeightType((int)EDIS.readEndianFloat());
		
		// Field 20 -- Station PCPN Network Indicator
		((NWSRFS_Station)NWSRFS_Object).setPCPNNetInd((int)EDIS.readEndianFloat());
		
		// Field 21 -- Station PCPN Weighting Indicator
		((NWSRFS_Station)NWSRFS_Object).setPCPNWeightInd((int)EDIS.readEndianFloat());
		
		// Now get additional parameter data if deepRead is specified.
		// Right now this does nothing. As budget is available additional
		// parameters will be defined!
		if(deepRead) {
		}

		// Close the EDIS
		EDIS.close();
	}
	else if(paramType.equalsIgnoreCase("PE")) { // Station Parameter
		logicalUnitNum=((NWSRFS_Station)NWSRFS_Object).getLogicalUnitNum("PE");
		recordNum=((NWSRFS_Station)NWSRFS_Object).getRecordNum("PE");
		
		// I now determine the PPPARMn file where n is the logicalUnitNum.
		pppParamFileName += logicalUnitNum;
		
		// Get the file handles, etc. to open the RA file.
		for(i = 17; i < 22; i++) {
			if(__dbFileNames[i].equalsIgnoreCase(pppParamFileName)) {
				pppParamIndex = i;
				break;
			}
		}
		
		// If pppParamIndex = -1 then we had a big problem! The 
		// logicalUnitNum was either > 5 or < 1 so we can not open
		// the correct parameter file to get the data!! Print a message and return false.
		if(pppParamIndex == -1) {
			Message.printStatus(10,routine, "Logical Unit Number for the Station: "+
				logicalUnitNum+". This unit number is not correct and must be between 1 and 5! "+
				"The parameter binary file can not be openned.");
			return false;
		}
		
		// Open the RA file.  Check if the the database binary file is open as a Random Access object
		if (!checkRandomAccessFileOpen(pppParamIndex, true)) {
			throw new Exception("Cannot open the " + __dbFileNames[pppParamIndex] + " binary database file");
		}

// Test read the first few bytes at record number to see what is really \
// happening.
//__NWSRFS_DBFiles[pppParamIndex].seek(0);
//__NWSRFS_DBFiles[pppParamIndex].seek(
//	(recordNum-1)*__byteLength[pppParamIndex]);
//EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, 512);
//charValue = new char[512];
//for (i = 0; i < 220; i++) {
//	byte byteValue = EDIS.readByte();
//	charValue[i] = (char)byteValue;
//	Message.printStatus(10,routine,"byteValue["+i+"] = "+byteValue);
//	Message.printStatus(10,routine,"charValue["+i+"] = "+charValue[i]);
//}
		// Now read the parameter file to get the parameter data
		// in the PPPPARMn binary file.
		// Read the record at recordNum to get the parameter data.
		__NWSRFS_DBFiles[pppParamIndex].seek(0);
		__NWSRFS_DBFiles[pppParamIndex].seek((recordNum-1)*__byteLength[pppParamIndex]);
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, 4);

		// Start reading in the general parameters.
		// Field 1 - NWRDS
		NWRDS = EDIS.readEndianInt();

		// Close the Stream
		EDIS.close();

		// Read in a new Stream for the remaining fields
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, NWRDS*4);

		// Field 2 -- Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() == 0 || !parseChar.equalsIgnoreCase(((NWSRFS_Station)
			NWSRFS_Object).getID())) {
			Message.printStatus(10,routine, "Reading from PPPPARM "+ logicalUnitNum+
					" file did not produce correct results. The read in identifier is not "+
				"the same as the one expected identifier!  The read in ID: "+parseChar+
				" and the expected ID:"+((NWSRFS_Station)NWSRFS_Object).getID());
			return false;
		}
		
		// Field 3 -- parameter type
		charValue = new char[4];
		for (i = 0; i < 4; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() == 0 || !parseChar.equalsIgnoreCase(paramType)) {
			Message.printStatus(10,routine, "Reading from PPPPARM "+	logicalUnitNum+
					" file did not produce correct results. The read in parameter type is not "+
				"the same as the one expected parameter type!  The read in paramType: "+parseChar+
				" and the expected parmType:"+paramType);
			return false;
		}
		
		// Field 4 - IRECNX
		IRECNX = checkInt(EDIS.readEndianInt(), 0, 100000, -1);
		
		// Start reading in Station parameters
		// Field 5 - PE Parameter array version
		checkInt(EDIS.readEndianInt(), 0, 100, -1);
		
		// Field 6 -- Station PE Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}

		// Field 7 -- Station PE Number
		EDIS.readEndianInt();
		
		// Field 8 -- Station PE Description
		charValue = new char[20];
		for (i = 0; i < 20; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		// Field 9 -- Station PE Latitude
		EDIS.readEndianFloat();
		
		// Field 10 -- Station PE Anemometer height
		((NWSRFS_Station)NWSRFS_Object).setPEAnemometerHeight(
			checkFloat(EDIS.readEndianFloat(), 0, 10000, -1));
		
		// Field 11 -- Station PE P Factor
		((NWSRFS_Station)NWSRFS_Object).setPEPFactor(
			checkFloat(EDIS.readEndianFloat(), 0, 10000, -1));
		
		// Field 12 -- Station PE Primary Radiation Type
		((NWSRFS_Station)NWSRFS_Object).setPERadiation(	checkInt(EDIS.readEndianInt(), 0, 3, -1));
		
		// Field 13 -- Station PE Correction Factor
		((NWSRFS_Station)NWSRFS_Object).setPECorrectFactor(	checkFloat(EDIS.readEndianFloat(), 0, 10000, -1));
		
		// Field 14 -- Station PE B3 parameter
		((NWSRFS_Station)NWSRFS_Object).setPEB3(checkFloat(EDIS.readEndianFloat(), 0, 10000, -1));
		
		// Field 15 -- Station PE Postal Service 2-character code
		charValue = new char[4];
		for (i = 0; i < 4; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		// Field 16 -- Unused
		EDIS.readEndianFloat();
		
		// Field 17-23 -- Station PE Fourier cooeficients
		for(i=0;i<6;i++) {
			((NWSRFS_Station)NWSRFS_Object).addPEFourierCoef(EDIS.readEndianFloat());
//Message.printStatus(10,routine,"PEFourierCoef["+i+"] = "+
//((NWSRFS_Station)NWSRFS_Object).getPEFourierCoef(i));
		}
		
		// Field 24-35 -- Station PE Sum for each of the last 12 months
		// Also called SUMPE
		for(i=0; i<12;i++) {
			((NWSRFS_Station)NWSRFS_Object).addPESUMPE(	EDIS.readEndianFloat());
//Message.printStatus(10,routine,"PESUMPE["+i+"] = "+
//((NWSRFS_Station)NWSRFS_Object).getPESUMPE(i));
		}
		
		// Field 36-47 -- Station PE Number of values in SUMPE for each
		// month
//		float tmpFloat;
		for(i=0; i<12;i++) {
//			tmpFloat = EDIS.readEndianFloat();
			((NWSRFS_Station)NWSRFS_Object).addPENumSUMPE(
//				(int)tmpFloat);
				(int)EDIS.readEndianFloat());
//Message.printStatus(10,routine,"PENumSUMPE["+i+"] = "+
//tmpFloat);
//((NWSRFS_Station)NWSRFS_Object).getPENumSUMPE(i));
		}
		
		// Field 48 -- Station PE Julian date of last day in 
		// SUMPE
//		tmpFloat = EDIS.readEndianFloat();
		((NWSRFS_Station)NWSRFS_Object).setPELastJulDay(
//			(int)tmpFloat);
			(int)EDIS.readEndianFloat());
//Message.printStatus(10,routine,"PELastJulDay = "+
//tmpFloat);
//((NWSRFS_Station)NWSRFS_Object).getPELastJulDay());

		// Now get additional parameter data if deepRead is specified.
		// Right now this does nothing. As budget is available additional
		// parameters will be defined!
		if(deepRead) {
		}
		
		// Close the EDIS
		EDIS.close();
	}
	else if(paramType.equalsIgnoreCase("RRS")) { // Station Parameter
		logicalUnitNum=((NWSRFS_Station)NWSRFS_Object).getLogicalUnitNum("RRS");
		recordNum=((NWSRFS_Station)NWSRFS_Object).getRecordNum("RRS");
		
		// I now determine the PPPARMn file where n is the
		// logicalUnitNum.
		pppParamFileName += logicalUnitNum;
		
		// Get the file handles, etc. to open the RA file.
		for(i = 17; i < 22; i++) {
			if(__dbFileNames[i].equalsIgnoreCase(pppParamFileName)) {
				pppParamIndex = i;
				break;
			}
		}
		
		// If pppParamIndex = -1 then we had a big problem! The 
		// logicalUnitNum was either > 5 or < 1 so we can not open
		// the correct parameter file to get the data!! Print a message and return false.
		if(pppParamIndex == -1) {
			Message.printStatus(10,routine, "Logical Unit Number for the Station: "+logicalUnitNum+
					". This unit number is not correct and must be between 1 and 5! "+
				"The parameter binary file can not be openned.");
			return false;
		}
		
		// Open the RA file.  Check if the the database binary file is open as a Random Access object
		if (!checkRandomAccessFileOpen(pppParamIndex, true)) {
			throw new Exception("Cannot open the " + __dbFileNames[pppParamIndex] + " binary database file");
		}

// Test read the first 512 bytes at record number to see what is really \
// happening.
//__NWSRFS_DBFiles[pppParamIndex].seek(0);
//__NWSRFS_DBFiles[pppParamIndex].seek(
//	(recordNum-1)*__byteLength[pppParamIndex]);
//EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, 512);
//charValue = new char[512];
//for (i = 0; i < 512; i++) {
//	byte byteValue = EDIS.readByte();
//	charValue[i] = (char)byteValue;
//	Message.printStatus(10,routine,"byteValue["+i+"] = "+byteValue);
//	Message.printStatus(10,routine,"charValue["+i+"] = "+charValue[i]);
//}
		

		// Now read the parameter file to get the parameter data
		// in the PPPPARMn binary file.
		// Read the record at recordNum to get the parameter data.
		__NWSRFS_DBFiles[pppParamIndex].seek(0);
		__NWSRFS_DBFiles[pppParamIndex].seek((recordNum-1)*__byteLength[pppParamIndex]);
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, 76);

		// Start reading in the general parameters.
		// Field 1 - NWRDS
		NWRDS = EDIS.readEndianInt();
//Message.printStatus(10,routine,"NWRDS: "+NWRDS);

		// Field 2 -- Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
//Message.printStatus(10,routine,"RRS Identifier: "+parseChar);
		
		if (parseChar.length() == 0 ||!parseChar.equalsIgnoreCase(((NWSRFS_Station)
			NWSRFS_Object).getID())) {
			Message.printStatus(10,routine, 
				"Reading from PPPPARM"+
				logicalUnitNum+" file did not produce correct "+
				"results. The read in identifier is not "+
				"the same as the one expected identifier! "+
				"The read in ID: "+parseChar+
				" and the expected ID:"+((NWSRFS_Station)
				NWSRFS_Object).getID());
			return false;
		}
		
		// Field 3 -- parameter type
		charValue = new char[4];
		for (i = 0; i < 4; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
//Message.printStatus(10,routine,"Parameter Type: "+parseChar);
		
		if (parseChar.length() == 0 ||
			!parseChar.equalsIgnoreCase(paramType)) {
			Message.printStatus(10,routine, 
				"Reading from PPPPARM"+
				logicalUnitNum+" file did not produce correct "+
				"results. The read in parameter type is not "+
				"the same as the one expected parameter type! "+
				"The read in paramType: "+parseChar+
				" and the expected parmType:"+paramType);
			return false;
		}
		
		// Field 4 - IRECNX
		IRECNX = checkInt(EDIS.readEndianInt(), 0, 100000, -1);
//Message.printStatus(10,routine,"IRECNX: "+IRECNX);
		
		// Start reading in Station parameters
		// Field 5 - RRS Parameter array version
		EDIS.readEndianInt();
//Message.printStatus(10,routine,"Parameter Array Version: "+(int)EDIS.readEndianFloat());
		
		// Field 6 -- Station RRS Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
//Message.printStatus(10,routine,"charValue["+i+"]: "+charValue[i]);
		}

		// Field 7 -- Station RRS Number
		EDIS.readEndianInt();
//Message.printStatus(10,routine,"Station Number: "+(int)EDIS.readEndianFloat());
		
		// Field 8 -- Station RRS Description
		charValue = new char[20];
		for (i = 0; i < 20; i++) {
			charValue[i] = EDIS.readEndianChar1();
//Message.printStatus(10,routine,"charValue["+i+"]: "+charValue[i]);
		}
		
		// Field 9 -- Station RRS Postal Service Code
		charValue = new char[4];
		for (i = 0; i < 4; i++) {
			charValue[i] = EDIS.readEndianChar1();
//Message.printStatus(10,routine,"charValue["+i+"]: "+charValue[i]);
		}

		// Field 10 -- Station RRS Unused
		EDIS.readEndianFloat();
		
		// Field 11 -- Station RRS NTYPE
		((NWSRFS_Station)NWSRFS_Object).setRRSNTYPE(
			(int)EDIS.readEndianFloat());
		int NTYPE = ((NWSRFS_Station)NWSRFS_Object).getRRSNTYPE();
//Message.printStatus(10,routine,"RRS NTYPE: "+
//((NWSRFS_Station)NWSRFS_Object).getRRSNTYPE());
		// Field 12 -- Station RRS NMISS
		((NWSRFS_Station)NWSRFS_Object).setRRSNMISS(
			(int)EDIS.readEndianFloat());
		int NMISS = ((NWSRFS_Station)NWSRFS_Object).getRRSNMISS();
//Message.printStatus(10,routine,"RRS NMISS: "+
//((NWSRFS_Station)NWSRFS_Object).getRRSNMISS());
		
		// Field 13 -- Station RRS NDIST
		((NWSRFS_Station)NWSRFS_Object).setRRSNDIST(
			(int)EDIS.readEndianFloat());
		int NDIST = ((NWSRFS_Station)NWSRFS_Object).getRRSNDIST();
//Message.printStatus(10,routine,"RRS NDIST: "+
//((NWSRFS_Station)NWSRFS_Object).getRRSNDIST());
		
		// Close the Stream
		EDIS.close();

		// The record length to pull the data from here is based on the
		// values we just pulled: NTYPE, NMISS, and NDIST. The formula
		// is this: there are 8 variables of length 4 bytes which loop
		// NTYPE times, 2 variable of length 4 bytes which loop NMISS
		// times and one variable array of size 24 of length 4 bytes
		// which loops NDIST times. Add all those up to get the number
		// of bytes to read.
		int recLen = 8*(NTYPE*4) + 2*(NMISS*4) + 24*(NDIST*4);

		// Read in a new Stream for the remaining fields
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, recLen);

		// Field 14 -- Station RRS Data Type Codes
		for(j=0;j<NTYPE;j++) {
			charValue = new char[4];
			for (i = 0; i < 4; i++) {
				charValue[i] = EDIS.readEndianChar1();
			}

			parseChar = new String(charValue).trim();

			if (parseChar.length() != 0) {
				((NWSRFS_Station)NWSRFS_Object).addRRSDataTypeCodes(parseChar);
			}
		}

		// Field 15 -- Station RRS Missing Data Allowed Indicator
		for(j=0;j<NTYPE;j++) {
			charValue = new char[4];
			for (i = 0; i < 4; i++) {
				charValue[i] = EDIS.readEndianChar1();
			}

			parseChar = new String(charValue).trim();

			if (parseChar.length() != 0) {
				((NWSRFS_Station)NWSRFS_Object).addRRSMissingInd(parseChar);
			}
		}

		// Field 16 -- Station RRS Data Time Interval
		for(j=0;j<NTYPE;j++) {
			((NWSRFS_Station)NWSRFS_Object).addRRSDataTimeInt((int)EDIS.readEndianFloat());
		}

		// Field 17 -- Station RRS Number of Values per Observation
		for(j=0;j<NTYPE;j++) {
			((NWSRFS_Station)NWSRFS_Object).addRRSNumObs((int)EDIS.readEndianFloat());
		}

		// Field 18 -- Station Min Days to Retain in Preprocessed DB
		for(j=0;j<NTYPE;j++) {
			((NWSRFS_Station)NWSRFS_Object).addRRSMinDaysToRetain((int)EDIS.readEndianFloat());
		}

		// Field 19 -- Station RRS Number of Obs in Preprocessed DB
		for(j=0;j<NTYPE;j++) {
			((NWSRFS_Station)NWSRFS_Object).addRRSNumObsInPPDB((int)EDIS.readEndianFloat());
		}

		// Field 20 -- Station Record Number in Preprocessed DB
		for(j=0;j<NTYPE;j++) {
			((NWSRFS_Station)NWSRFS_Object).addRRSIREC((int)EDIS.readEndianFloat());
		}

		// Field 21 -- Station RRS Interpolation Option
		for(j=0;j<NMISS;j++) {
			((NWSRFS_Station)NWSRFS_Object).addRRSInterpOpt((int)EDIS.readEndianInt());
		}

		// Field 22 -- Station RRS Extrapolation Recess Constant
		for(j=0;j<NMISS;j++) {
			((NWSRFS_Station)NWSRFS_Object).addRRSExtrapRecessConst(EDIS.readEndianFloat());
		}

		// Field 23 -- Station RRS Min Discharge below which a dist
		// is applied
		for(j=0;j<NTYPE;j++) {
			((NWSRFS_Station)NWSRFS_Object).addRRSMinQAllowed(EDIS.readEndianFloat());
		}

		// Field 24 -- Station RRS Fraction of flow typically occuring during each hour
		for(j=0;j<NDIST;j++) {
			for(i=0;i<24;i++) {
				((NWSRFS_Station)NWSRFS_Object).addRRSFractQ(EDIS.readEndianFloat());
			}
		}

		// Close the EDIS
		EDIS.close();
	}
	else if(paramType.equalsIgnoreCase("TEMP")) { // Station Parameter
		logicalUnitNum=((NWSRFS_Station)NWSRFS_Object).getLogicalUnitNum("TEMP");
		recordNum=((NWSRFS_Station)NWSRFS_Object).getRecordNum("TEMP");
		
		// Determine the PPPARMn file where n is the logicalUnitNum.
		pppParamFileName += logicalUnitNum;
		
		// Get the file handles, etc. to open the RA file.
		for(i = 17; i < 22; i++) {
			if(__dbFileNames[i].equalsIgnoreCase(pppParamFileName)) {
				pppParamIndex = i;
				break;
			}
		}
		
		// If pppParamIndex = -1 then we had a big problem! The 
		// logicalUnitNum was either > 5 or < 1 so we can not open
		// the correct parameter file to get the data!! Print a message and return false.
		if(pppParamIndex == -1) {
			Message.printStatus(10,routine, "Logical Unit Number for the Station: "+logicalUnitNum+
					". This unit number is not correct and must be between 1 and 5! "+
				"The parameter binary file can not be openned.");
			return false;
		}
		
		// Open the RA file.  Check if the the database binary file is open as a Random Access object
		if (!checkRandomAccessFileOpen(pppParamIndex, true)) {
			throw new Exception("Cannot open the " + __dbFileNames[pppParamIndex] + " binary database file");
		}

		// Now read the parameter file to get the parameter data
		// in the PPPPARMn binary file.
		// Read the record at recordNum to get the parameter data.
		__NWSRFS_DBFiles[pppParamIndex].seek(0);
		__NWSRFS_DBFiles[pppParamIndex].seek((recordNum-1)*__byteLength[pppParamIndex]);
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, 4);

		// Start reading in the general parameters.
		// Field 1 - NWRDS
		NWRDS = EDIS.readEndianInt();

		// Close the Stream
		EDIS.close();

		// Read in a new Stream for the remaining fields
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, NWRDS*4);

		// Field 2 -- Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() == 0 ||
			!parseChar.equalsIgnoreCase(((NWSRFS_Station)
			NWSRFS_Object).getID())) {
			Message.printStatus(10,routine, "Reading from PPPPARM"+	logicalUnitNum+
					" file did not produce correct results. The read in identifier is not "+
				"the same as the one expected identifier!  The read in ID: "+parseChar+
				" and the expected ID:"+((NWSRFS_Station)NWSRFS_Object).getID());
			return false;
		}
		
		// Field 3 -- parameter type
		charValue = new char[4];
		for (i = 0; i < 4; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() == 0 || !parseChar.equalsIgnoreCase(paramType)) {
			Message.printStatus(10,routine, "Reading from PPPPARM "+ logicalUnitNum+
					" file did not produce correct results. The read in parameter type is not "+
				"the same as the one expected parameter type!  The read in paramType: "+parseChar+
				" and the expected parmType:"+paramType);
			return false;
		}
		
		// Field 4 - IRECNX
		IRECNX = checkInt(EDIS.readEndianInt(), 0, 100000, -1);
		
		// Start reading in Station parameters
		// Field 5 - TEMP Parameter array version
		checkInt(EDIS.readEndianInt(), 0, 100, -1);
		
		// Field 6 -- Station TEMP Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}

		// Field 7 -- Station TEMP Number
		EDIS.readEndianInt();
		
		// Field 8 -- Station TEMP Description
		charValue = new char[20];
		for (i = 0; i < 20; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		// Field 9 -- Station TEMP Data Indicator
		((NWSRFS_Station)NWSRFS_Object).setTEMPDataInd(	checkInt(EDIS.readEndianInt(), 1, 4, 0));
		
		// Field 10 -- Station TEMP Mountainous Indicator
		((NWSRFS_Station)NWSRFS_Object).setTEMPMountainInd(	checkInt(EDIS.readEndianInt(), 0, 1, -1));
		
		// Field 11 -- Station TEMP Maximum correcton factor DEGF
		((NWSRFS_Station)NWSRFS_Object).setTEMPMaxCorrect(EDIS.readEndianFloat());
		
		// Field 12 -- Station TEMP Minimum correcton factor DEGF
		((NWSRFS_Station)NWSRFS_Object).setTEMPMinCorrect( EDIS.readEndianFloat());
		
		// Field 13 -- Station TEMP Forecast Max/Min Indicator
		((NWSRFS_Station)NWSRFS_Object).setTEMPMountainInd(	checkInt(EDIS.readEndianInt(), 0, 1, -1));
		
		// Field 14 -- Station TEMP Elevation Weighting Factor
		((NWSRFS_Station)NWSRFS_Object).setTEMPElevationWeight(	EDIS.readEndianFloat());
		
		// Field 15 -- Station TEMP Network Indicator
		((NWSRFS_Station)NWSRFS_Object).setTEMPNetInd( checkInt(EDIS.readEndianInt(), 0, 2, -1));
		
		// Field 16 -- Station TEMP array location of mean monthly Max/Min data
		((NWSRFS_Station)NWSRFS_Object).setTEMPLocMeanMonthMaxMin( (int)EDIS.readEndianFloat());
		
		// Field 16 -- Station TEMP array location of pointers
		// for 3 closest stations with Max/Min data for each quadrant
		for(i=0; i < 4; i++) {
			for(j=0;j < 3; j++) {
				((NWSRFS_Station)NWSRFS_Object).addTEMPLocPointMaxMin((int)EDIS.readEndianFloat());
			}
		}
		
		// Field 17 -- Station TEMP weights for 3 closest 
		// stations with Max/Min data for each quadrant
		for(i=0; i < 4; i++) {
			for(j=0;j < 3; j++) {
				((NWSRFS_Station)NWSRFS_Object).addTEMPWeightMaxMin(EDIS.readEndianFloat());
			}
		}
		
		// Field 18 -- Station TEMP array location of pointers
		// for 3 closest stations with instantaneous data for each quadrant
		for(i=0; i < 4; i++) {
			for(j=0;j < 3; j++) {
				((NWSRFS_Station)NWSRFS_Object).addTEMPLocPointInst((int)EDIS.readEndianFloat());
			}
		}
		
		// Field 19 -- Station TEMP weights for 3 closest
		// stations with instantaneous data for each quadrant
		for(i=0; i < 4; i++) {
			for(j=0;j < 3; j++) {
				((NWSRFS_Station)NWSRFS_Object).addTEMPWeightInst(EDIS.readEndianFloat());
			}
		}
		
		// Field 20 -- Station TEMP array location of pointers
		// for 2 closest stations with forecast data for each quadrant
		for(i=0; i < 4; i++) {
			for(j=0;j < 2; j++) {
				((NWSRFS_Station)NWSRFS_Object).addTEMPLocPointForecast((int)EDIS.readEndianFloat());
			}
		}
		
		// Field 21 -- Station TEMP weights for 2 closest
		// station with forecast data for each quadrant
		for(i=0; i < 4; i++) {
			for(j=0;j < 2; j++) {
				((NWSRFS_Station)NWSRFS_Object).addTEMPWeightForecast( EDIS.readEndianFloat());
			}
		}
		
		// Field 22 -- Station TEMP time interval of instantaneous temperature data
		((NWSRFS_Station)NWSRFS_Object).setTEMPTimeIntervalInst((int)EDIS.readEndianFloat());
		
		// Now get additional parameter data if deepRead is specified.
		// Right now this does nothing. As budget is available additional
		// parameters will be defined!
		if(deepRead) {
		}

		// Close the EDIS
		EDIS.close();
	}
	else if(paramType.equalsIgnoreCase("BASN")) { // Basin Parameter
		logicalUnitNum=((NWSRFS_BASN)NWSRFS_Object).getLogicalUnitNum();
		recordNum=((NWSRFS_BASN)NWSRFS_Object).getRecordNum();
		
		// Determine the PPPARMn file where n is the logicalUnitNum.
		pppParamFileName += logicalUnitNum;
		
		// Get the file handles, etc. to open the RA file.
		for(i = 17; i < 22; i++) {
			if(__dbFileNames[i].equalsIgnoreCase(pppParamFileName)) {
				pppParamIndex = i;
				break;
			}
		}
		
		// If pppParamIndex = -1 then we had a big problem! The 
		// logicalUnitNum was either > 5 or < 1 so we can not open
		// the correct parameter file to get the data!! Print a message and return false.
		if(pppParamIndex == -1) {
			Message.printStatus(10,routine, "Logical Unit Number for the Station: "+ logicalUnitNum+
					". This unit number is not correct and must be between 1 and 5! "+
				"The parameter binary file can not be opened.");
			return false;
		}
		
		// Open the RA file.
		// Check if the the database binary file is open as a Random Access object
		if (!checkRandomAccessFileOpen(pppParamIndex, true)) {
			throw new Exception("Cannot open the " + __dbFileNames[pppParamIndex] + " binary database file");
		}

		// Now read the parameter file to get the parameter data in the PPPPARMn binary file.
		// Read the record at recordNum to get the parameter data.
		__NWSRFS_DBFiles[pppParamIndex].seek(0);
		__NWSRFS_DBFiles[pppParamIndex].seek( (recordNum-1)*__byteLength[pppParamIndex]);
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, 4);

		// Start reading in the general parameters.
		// Field 1 - NWRDS
		NWRDS = EDIS.readEndianInt();

		// Close the Stream
		EDIS.close();

		// Read in a new Stream for the remaining fields
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, NWRDS*4);

		// Field 2 -- Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() == 0 ||!parseChar.equalsIgnoreCase(((NWSRFS_BASN)NWSRFS_Object).getID())) {
			Message.printStatus(10,routine, "Reading from PPPPARM "+ logicalUnitNum+
					" file did not produce correct results. The read in identifier is not "+
				"the same as the one expected identifier!  The read in ID: "+parseChar+
				" and the expected ID:"+((NWSRFS_BASN)NWSRFS_Object).getID());
			return false;
		}
		
		// Field 3 -- parameter type
		charValue = new char[4];
		for (i = 0; i < 4; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() == 0 ||
			!parseChar.equalsIgnoreCase(paramType)) {
			Message.printStatus(10,routine, "Reading from PPPPARM "+ logicalUnitNum+
					" file did not produce correct results. The read in parameter type is not "+
				"the same as the one expected parameter type!  The read in paramType: "+parseChar+
				" and the expected parmType:"+paramType);
			return false;
		}
		
		// Field 4 - IRECNX
		IRECNX = checkInt(EDIS.readEndianInt(), 0, 100000, -1);
		
		// Start reading in Basin parameters
		// Field 5 - BASN Parameter array version
		EDIS.readEndianInt();
		
		// Field 6 -- Basin Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}

		// Field 7 -- Basin Description
		charValue = new char[20];
		for (i = 0; i < 20; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() != 0) {
			((NWSRFS_BASN)NWSRFS_Object).setDescription(parseChar);
		}
		
		// Field 8 -- Mean Elevation
		((NWSRFS_BASN)NWSRFS_Object).setBASNMeanElevation(EDIS.readEndianFloat());
		
		// Field 9 -- Basin Area KM^2
		((NWSRFS_BASN)NWSRFS_Object).setBASNArea(EDIS.readEndianFloat());
		
		// Field 10 -- Basin computed Area KM^2
		((NWSRFS_BASN)NWSRFS_Object).setBASNComputedBasinArea(EDIS.readEndianFloat());
		
		// Field 11 -- Centroid of Basin X coordinate
		((NWSRFS_BASN)NWSRFS_Object).setBASNCentroidX((int)EDIS.readEndianFloat());
		
		// Field 12 -- Centroid of Basin Y coordinate
		((NWSRFS_BASN)NWSRFS_Object).setBASNCentroidY((int)EDIS.readEndianFloat());
		
		// Field 13 -- MAP Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}

		parseChar = new String(charValue).trim();
		
		if (parseChar.length() != 0) {
			((NWSRFS_BASN)NWSRFS_Object).setBASNMAPID(parseChar);
		}
		
		// Field 14 -- MAT Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}

		parseChar = new String(charValue).trim();
		
		if (parseChar.length() != 0) {
			((NWSRFS_BASN)NWSRFS_Object).setBASNMATID(parseChar);
		}
		
		// Field 15 -- MAP Update Indicator
		((NWSRFS_BASN)NWSRFS_Object).setBASNMAPUpdateInd((int)EDIS.readEndianFloat());
		
		// Field 16 -- MAT Update Indicator
		((NWSRFS_BASN)NWSRFS_Object).setBASNMATUpdateInd((int)EDIS.readEndianFloat());
		
		// Field 17 -- MAPX Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}

		parseChar = new String(charValue).trim();
		
		if (parseChar.length() != 0) {
			((NWSRFS_BASN)NWSRFS_Object).setBASNMAPXID(	parseChar);
		}
		
		// Field 18 -- HRAP grid spacing factor
		((NWSRFS_BASN)NWSRFS_Object).setBASNGridSpacingFactor((int)EDIS.readEndianFloat());
		
		// Field 19 -- Number of pairs of basin boundary points
		((NWSRFS_BASN)NWSRFS_Object).setBASNNBPTS((int)EDIS.readEndianFloat());
		
		// Field 20 -- Number of HRAP grid segments
		((NWSRFS_BASN)NWSRFS_Object).setBASNNSEGS((int)EDIS.readEndianFloat());
		
		// Field 21 -- Latitude and Longitude of basin boundary points
		for(i = 0; i < ((NWSRFS_BASN)NWSRFS_Object).getBASNNBPTS(); i++) 
		{
			((NWSRFS_BASN)NWSRFS_Object).addBASNLatitude(EDIS.readEndianFloat());
			((NWSRFS_BASN)NWSRFS_Object).addBASNLongitude(EDIS.readEndianFloat());
		}
		
		// Field 22 -- Grid point definition
		Vector gpDef;
		for(i = 0; i < ((NWSRFS_BASN)NWSRFS_Object).getBASNNSEGS(); i++) 
		{
			gpDef = new Vector();
			gpDef.addElement(new Integer(EDIS.readEndianInt()));
			gpDef.addElement(new Integer(EDIS.readEndianInt()));
			gpDef.addElement(new Integer(EDIS.readEndianInt()));
			((NWSRFS_BASN)NWSRFS_Object).addBASNGridPointDef(gpDef);
		}
		
		// Now get additional parameter data if deepRead is specified.
		// Right now this does nothing. As budget is available additional parameters will be defined!
		if(deepRead) {
		}

		// Close the EDIS
		EDIS.close();
	}
	else if(paramType.equalsIgnoreCase("MAP")) { // Areal Parameter
		logicalUnitNum=((NWSRFS_MAP)NWSRFS_Object).getLogicalUnitNum();
		recordNum=((NWSRFS_MAP)NWSRFS_Object).getRecordNum();
		
		// Determine the PPPARMn file where n is the logicalUnitNum.
		pppParamFileName += logicalUnitNum;
		
		// Get the file handles, etc. to open the RA file.
		for(i = 17; i < 22; i++) {
			if(__dbFileNames[i].equalsIgnoreCase(pppParamFileName)) {
				pppParamIndex = i;
				break;
			}
		}
		
		// If pppParamIndex = -1 then we had a big problem! The 
		// logicalUnitNum was either > 5 or < 1 so we can not open
		// the correct parameter file to get the data!! Print a message
		// and return false.
		if(pppParamIndex == -1) {
			Message.printStatus(10,routine, "Logical Unit Number for the Station: "+ logicalUnitNum+
					". This unit number is not correct and must be between 1 and 5! "+
				"The parameter binary file can not be openned.");
			return false;
		}
		
		// Open the RA file.
		// Check if the the database binary file is open as a Random Access object
		if (!checkRandomAccessFileOpen(pppParamIndex, true)) {
			throw new Exception("Cannot open the " + __dbFileNames[pppParamIndex] + " binary database file");
		}

		// Now read the parameter file to get the parameter data
		// in the PPPPARMn binary file.

		// Read the record at recordNum to get the parameter data.
		
		__NWSRFS_DBFiles[pppParamIndex].seek(0);
		__NWSRFS_DBFiles[pppParamIndex].seek( (recordNum-1)*__byteLength[pppParamIndex]);
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, 4);

		// Start reading in the general parameters.
		// Field 1 - NWRDS
		NWRDS = EDIS.readEndianInt();

		EDIS.close();
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, NWRDS*4);

		// Field 2 -- Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() == 0 || !parseChar.equalsIgnoreCase(((NWSRFS_MAP)NWSRFS_Object).getID())) {
			Message.printStatus(10,routine, "Reading from PPPPARM "+ logicalUnitNum+
					" file did not produce correct results. The read in identifier is not "+
				"the same as the one expected identifier!  The read in ID: "+parseChar+
				" and the expected ID:"+((NWSRFS_MAP)NWSRFS_Object).getID());
			return false;
		}
		
		// Field 3 -- parameter type
		charValue = new char[4];
		for (i = 0; i < 4; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() == 0 || !parseChar.equalsIgnoreCase(paramType)) {
			Message.printStatus(10,routine, "Reading from PPPPARM "+ logicalUnitNum+
					" file did not produce correct results. The read in parameter type is not "+
				"the same as the one expected parameter type!  The read in paramType: "+parseChar+
				" and the expected parmType:"+paramType);
			return false;
		}
		
		// Field 4 - IRECNX
		IRECNX = checkInt(EDIS.readEndianInt(), 0, 100000, -1);
		
		// Start reading in AREAL parameters
		// Field 5 - MAP Parameter array version
		checkInt(EDIS.readEndianInt(), 0, 100, -1);
		
		// Field 6 -- Areal MAP Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}

		parseChar = new String(charValue).trim();

		// Field 7 -- Areal MAP Description
		charValue = new char[20];
		for (i = 0; i < 20; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();

		if (parseChar.length() != 0) {
			((NWSRFS_MAP)NWSRFS_Object).setDescription( parseChar);
		}
		
		// Field 8 -- Areal MAP Data Time Interval
		((NWSRFS_MAP)NWSRFS_Object).setMAPDataTimeInt(checkInt(EDIS.readEndianInt(), 0, 24, -1));
		
		// Field 9 -- Areal MAP Basin Boundry Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();

		if (parseChar.length() != 0) {
			((NWSRFS_MAP)NWSRFS_Object).setMAPBasinBoundaryID(
				parseChar);
		}
		
		// Field 10 -- Areal MAP Type of Timing Weights
		((NWSRFS_MAP)NWSRFS_Object).setMAPTypeTimeWeights(checkInt(EDIS.readEndianInt(), 1, 3, 0));
		
		// Field 11 -- Areal MAP Exponent in 1/D**POWER
		((NWSRFS_MAP)NWSRFS_Object).setMAPExponent(	EDIS.readEndianFloat());
		
		// Field 12 -- Areal MAP Number of Stations used for Time
		// Distribution (NSTWT)
		((NWSRFS_MAP)NWSRFS_Object).setMAPNSTWT(checkInt(EDIS.readEndianInt(), 0, 100000, -1));
		
		// Field 13 -- Areal MAP Type of Station Weights
		((NWSRFS_MAP)NWSRFS_Object).setMAPTypeStationWeights(checkInt(EDIS.readEndianInt(), 1, 4, 0));
		
		// Field 14 -- Areal MAP Identifier for FMAP Area used by this
		// MAP area
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();

		if (parseChar.length() != 0) {
			((NWSRFS_MAP)NWSRFS_Object).setMAPFMAPID(parseChar);
		}
		
		// Field 15 -- Areal MAP Centroid X value
		((NWSRFS_MAP)NWSRFS_Object).setMAPCentroidX(EDIS.readEndianFloat());
		
		// Field 16 -- Areal MAP Centroid Y value
		((NWSRFS_MAP)NWSRFS_Object).setMAPCentroidY(EDIS.readEndianFloat());
		
		// Now get additional parameter data if deepRead is specified.
		// Right now this does nothing. As budget is available additional parameters will be defined!
		if(deepRead) {
		}

		// Close the EDIS
		EDIS.close();
	}
	else if(paramType.equalsIgnoreCase("MAPE")) { // Areal Parameter
		Message.printStatus(10,routine, "Parameter Type: "+	paramType+" has not been implemented yet.");
		return false;
	}
	else if(paramType.equalsIgnoreCase("MAPS")) { // Areal Parameter
		Message.printStatus(10,routine, "Parameter Type: "+	paramType+" has not been implemented yet.");
		return false;
	}
	else if(paramType.equalsIgnoreCase("MAPX")) { // Areal Parameter
		Message.printStatus(10,routine, "Parameter Type: " + paramType+" has not been implemented yet.");
		return false;
	}
	else if(paramType.equalsIgnoreCase("MAT")) { // Areal Parameter
		logicalUnitNum=((NWSRFS_MAT)NWSRFS_Object).getLogicalUnitNum();
		recordNum=((NWSRFS_MAT)NWSRFS_Object).getRecordNum();
		
		// Determine the PPPARMn file where n is the logicalUnitNum.
		pppParamFileName += logicalUnitNum;
		
		// Get the file handles, etc. to open the RA file.
		for(i = 17; i < 22; i++) {
			if(__dbFileNames[i].equalsIgnoreCase(pppParamFileName)) {
				pppParamIndex = i;
				break;
			}
		}
		
		// If pppParamIndex = -1 then we had a big problem! The 
		// logicalUnitNum was either > 5 or < 1 so we can not open
		// the correct parameter file to get the data!! Print a message and return false.
		if(pppParamIndex == -1) {
			Message.printStatus(10,routine, "Logical Unit Number for the Station: "+ logicalUnitNum+
					". This unit number is not correct and must be between 1 and 5! "+
				"The parameter binary file can not be opened.");
			return false;
		}
		
		// Open the RA file.  Check if the the database binary file is open as a Random Access object
		if (!checkRandomAccessFileOpen(pppParamIndex, true)) {
			throw new Exception("Can not open the " + __dbFileNames[pppParamIndex] + " binary database file");
		}

		// Now read the parameter file to get the parameter data in the PPPPARMn binary file.
		// Read the record at recordNum to get the parameter data.
		__NWSRFS_DBFiles[pppParamIndex].seek(0);
		__NWSRFS_DBFiles[pppParamIndex].seek( (recordNum-1)*__byteLength[pppParamIndex]);
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, 4);

		// Start reading in the general parameters.
		// Field 1 - NWRDS
		NWRDS = EDIS.readEndianInt();

		// Close the Stream
		EDIS.close();

		// Read in a new Stream for the remaining fields
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, NWRDS*4);

		// Field 2 -- Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() == 0 || !parseChar.equalsIgnoreCase(((NWSRFS_MAT)NWSRFS_Object).getID())) {
			Message.printStatus(10,routine, "Reading from PPPPARM "+ logicalUnitNum+
					" file did not produce correct results. The read in identifier is not "+
				"the same as the one expected identifier!  The read in ID: "+parseChar+
				" and the expected ID:"+((NWSRFS_MAT)NWSRFS_Object).getID());
			return false;
		}
		
		// Field 3 -- parameter type
		charValue = new char[4];
		for (i = 0; i < 4; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() == 0 || !parseChar.equalsIgnoreCase(paramType)) {
			Message.printStatus(10,routine, "Reading from PPPPARM "+ logicalUnitNum+
					" file did not produce correct results. The read in parameter type is not "+
				"the same as the one expected parameter type!  The read in paramType: "+parseChar+
				" and the expected parmType:"+paramType);
			return false;
		}
		
		// Field 4 - IRECNX
		IRECNX = checkInt(EDIS.readEndianInt(), 0, 100000, -1);
		
		// Start reading in AREAL parameters
		// Field 5 - MAT Parameter array version
		checkInt(EDIS.readEndianInt(), 0, 100, -1);
		
		// Field 6 -- Areal MAT Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}

		// Field 7 -- Areal MAT Description
		charValue = new char[20];
		for (i = 0; i < 20; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() != 0) {
			((NWSRFS_MAT)NWSRFS_Object).setDescription(	parseChar);
		}
		
		// Field 8 -- Areal MAT Centroid X value
		((NWSRFS_MAT)NWSRFS_Object).setMATCentroidX(EDIS.readEndianFloat());
		
		// Field 9 -- Areal MAT Centroid Y value
		((NWSRFS_MAT)NWSRFS_Object).setMATCentroidY(EDIS.readEndianFloat());
		
		// Now get additional parameter data if deepRead is specified.
		// Right now this does nothing. As budget is available additional parameters will be defined!
		if(deepRead) {
		}

		// Close the EDIS
		EDIS.close();
	}
	else if(paramType.equalsIgnoreCase("NTWK")) { // General Parameter
		logicalUnitNum=((NWSRFS_NTWK)NWSRFS_Object).getLogicalUnitNum();
		recordNum=((NWSRFS_NTWK)NWSRFS_Object).getRecordNum();
		
		// Determine the PPPARMn file where n is the logicalUnitNum.
		pppParamFileName += logicalUnitNum;
		
		// Get the file handles, etc. to open the RA file.
		for(i = 17; i < 22; i++) {
			if(__dbFileNames[i].equalsIgnoreCase(pppParamFileName)) {
				pppParamIndex = i;
				break;
			}
		}
		
		// If pppParamIndex = -1 then we had a big problem! The 
		// logicalUnitNum was either > 5 or < 1 so we can not open
		// the correct parameter file to get the data!! Print a message
		// and return false.
		if(pppParamIndex == -1) {
			Message.printStatus(10,routine, "Logical Unit Number for the Station: "+ logicalUnitNum+
					". This unit number is not correct and must be between 1 and 5! "+
				"The parameter binary file can not be openned.");
			return false;
		}
		
		// Open the RA file.  Check if the the database binary file is open as a Random Access object
		if (!checkRandomAccessFileOpen(pppParamIndex, true)) {
			throw new Exception("Cannot open the " + __dbFileNames[pppParamIndex] + " binary database file");
		}

// Test read the first 64 bytes at record number to see what is really \
// happening.
//__NWSRFS_DBFiles[pppParamIndex].seek(0);
//__NWSRFS_DBFiles[pppParamIndex].seek(
//	(recordNum-1)*__byteLength[pppParamIndex]);
//EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, 64);
//charValue = new char[64];
//for (i = 0; i < 64; i++) {
//	byte byteValue = EDIS.readByte();
//	charValue[i] = (char)byteValue;
//	Message.printStatus(10,routine,"byteValue["+i+"] = "+byteValue);
//	Message.printStatus(10,routine,"charValue["+i+"] = "+charValue[i]);
//}
		
		// Now read the parameter file to get the parameter data
		// in the PPPPARMn binary file.
		// Read the record at recordNum to get the parameter data.
		__NWSRFS_DBFiles[pppParamIndex].seek(0);
		__NWSRFS_DBFiles[pppParamIndex].seek( (recordNum-1)*__byteLength[pppParamIndex]);
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, 4);

		// Start reading in the general parameters.
		// Field 1 - NWRDS
		NWRDS = EDIS.readEndianInt();

		// Close the Stream
		EDIS.close();

		// Read in a new Stream for the remaining fields
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, NWRDS*4);

		// Field 2 -- Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
/*		if (parseChar.length() == 0 ||
			!parseChar.equalsIgnoreCase(((NWSRFS_NTWK)
			NWSRFS_Object).getID())) {
			Message.printStatus(10,routine, 
				"Reading from PPPPARM"+
				logicalUnitNum+" file did not produce correct "+
				"results. The read in identifier is not "+
				"the same as the one expected identifier! "+
				"The read in ID: "+parseChar+
				" and the expected ID:"+((NWSRFS_NTWK)
				NWSRFS_Object).getID());
			return false;
		}
*/
		// Field 3 -- parameter type
		charValue = new char[4];
		for (i = 0; i < 4; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() == 0 ||
			!parseChar.equalsIgnoreCase(paramType)) {
			Message.printStatus(10,routine, "Reading from PPPPARM "+ logicalUnitNum+
					" file did not produce correct results. The read in parameter type is not "+
				"the same as the one expected parameter type! The read in paramType: "+parseChar+
				" and the expected parmType:"+paramType);
			return false;
		}
		
		// Field 4 - IRECNX
		IRECNX = checkInt(EDIS.readEndianInt(), 0, 100000, -1);
		
		// Start reading in GENERAL parameters
		// Field 5 - NTWK Parameter array version
		EDIS.readEndianFloat();
		
		// Field 6 -- General NTWK Update month
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKMonth( (int)EDIS.readEndianFloat());
		//int NTWKMon = ((NWSRFS_NTWK)NWSRFS_Object).getNTWKMonth();
		
		// Field 7 -- General NTWK Update day
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKDay((int)EDIS.readEndianFloat());
		//int NTWKDay = ((NWSRFS_NTWK)NWSRFS_Object).getNTWKDay();
		
		// Field 8 -- General NTWK Update year
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKYear((int)EDIS.readEndianFloat());
		//int NTWKYear = ((NWSRFS_NTWK)NWSRFS_Object).getNTWKYear();
		
		// Field 9 -- General NTWK Update Hour and Minute
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKHourMin((int)EDIS.readEndianFloat());
		//int NTWKHourMin = ((NWSRFS_NTWK)NWSRFS_Object).getNTWKHourMin();

		// Calculate the DateTime object!
//		((NWSRFS_NTWK)NWSRFS_Object).setNTWKDateTime(
//			NTWKMon,
//			NTWKDay,
//			NTWKYear,
//			NTWKHourMin);

		// Field 10 -- General NTWK Update number of indicators
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKNumberInd((int)EDIS.readEndianFloat());
		
		// Field 11 -- General NTWK update 5 closest PCPN stations
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKFivePCPNInd((int)EDIS.readEndianFloat());
		
		// Field 12 -- General NTWK update 3 closest PCPN stations
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKThreePCPNInd((int)EDIS.readEndianFloat());
		
		// Field 13 -- General NTWK update 3 closest Max/Min
		// Temp stations
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKThreeMaxMinTEMPInd((int)EDIS.readEndianFloat());
		
		// Field 14 -- General NTWK update 3 closest Instantaneous 
		// Temp stations
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKThreeInstTEMPInd((int)EDIS.readEndianFloat());
		
		// Field 15 -- General NTWK update 2 closest forecast
		// Temp stations
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKTwoForecastTEMPInd((int)EDIS.readEndianFloat());
		
		// Field 16 -- General NTWK update MAP Time Distribution
		// Weights indicator
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKMAPTimeDistWeights(	(int)EDIS.readEndianFloat());
		
		// Field 17 -- General NTWK update MAP grid point weights
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKMAPGridPointWeights((int)EDIS.readEndianFloat());
		
		// Field 18 -- General NTWK update MAT grid point weights
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKMATGridPointWeights((int)EDIS.readEndianFloat());
		
		// Field 19 -- General NTWK update MAP params due to basin
		// change
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKMAPParamBasnBound((int)EDIS.readEndianFloat());
		
		// Field 20 -- General NTWK update MAT params due to basin
		// change
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKMATParamBasnBound((int)EDIS.readEndianFloat());
		
		// Field 21 -- General NTWK update MAPE Weights
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKMAPEWeights((int)EDIS.readEndianFloat());
		
		// Field 22 -- General NTWK update precip station list
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKOP24UpdateInd((int)EDIS.readEndianFloat());
		
		// Field 23 -- General NTWK update less than 24 precip station
		// list
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKOPVRUpdateInd((int)EDIS.readEndianFloat());
		
		// Field 24 -- General NTWK update TEMP station list
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKOT24UpdateInd((int)EDIS.readEndianFloat());
		
		// Field 25 -- General NTWK update evap station list
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKOE24UpdateInd((int)EDIS.readEndianFloat());
		
		// Field 26 -- General NTWK update RRS alphabetical order 
		// station indicator
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKORRSUpdateInd((int)EDIS.readEndianFloat());
		
		// Field 27 -- General NTWK update station grid point locations
		// and grid station alphbetical order
		((NWSRFS_NTWK)NWSRFS_Object).setNTWKGPOG24UpdateInd((int)EDIS.readEndianFloat());
		
		// Now get additional parameter data if deepRead is specified.
		// Right now this does nothing. As budget is available additional
		// parameters will be defined!
		if(deepRead) {
		}

		// Close the EDIS
		EDIS.close();
	}
	else if(paramType.equalsIgnoreCase("ORRS")) { // AlphBet Order Parameter
		logicalUnitNum=((NWSRFS_ORRS)NWSRFS_Object).getLogicalUnitNum();
		recordNum=((NWSRFS_ORRS)NWSRFS_Object).getRecordNum();
		
		// I now determine the PPPARMn file where n is the logicalUnitNum.
		pppParamFileName += logicalUnitNum;
		
		// Get the file handles, etc. to open the RA file.
		for(i = 17; i < 22; i++) {
			if(__dbFileNames[i].equalsIgnoreCase(pppParamFileName)) {
				pppParamIndex = i;
				break;
			}
		}
		
		// If pppParamIndex = -1 then we had a big problem! The 
		// logicalUnitNum was either > 5 or < 1 so we can not open
		// the correct parameter file to get the data!! Print a message and return false.
		if(pppParamIndex == -1) {
			Message.printStatus(10,routine, "Logical Unit Number for the Station: "+logicalUnitNum+
					". This unit number is not correct and must be between 1 and 5! "+
				"The parameter binary file can not be openned.");
			return false;
		}
		
		// Open the RA file. Check if the the database binary file is open as a Random Access object
		if (!checkRandomAccessFileOpen(pppParamIndex, true)) {
			throw new Exception("Cannot open the " + __dbFileNames[pppParamIndex] + " binary database file");
		}

// Test read the first 64 bytes at record number to see what is really \
// happening.
//__NWSRFS_DBFiles[pppParamIndex].seek(0);
//__NWSRFS_DBFiles[pppParamIndex].seek(
//	(recordNum-1)*__byteLength[pppParamIndex]);
//EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, 64);
//charValue = new char[64];
//for (i = 0; i < 64; i++) {
//	byte byteValue = EDIS.readByte();
//	charValue[i] = (char)byteValue;
//	Message.printStatus(10,routine,"byteValue["+i+"] = "+byteValue);
//	Message.printStatus(10,routine,"charValue["+i+"] = "+charValue[i]);
//}
		
		// Now read the parameter file to get the parameter data
		// in the PPPPARMn binary file.
		// Read the record at recordNum to get the parameter data.
		__NWSRFS_DBFiles[pppParamIndex].seek(0);
		__NWSRFS_DBFiles[pppParamIndex].seek( (recordNum-1)*__byteLength[pppParamIndex]);
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, 4);

		// Start reading in the general parameters.
		// Field 1 - NWRDS
		NWRDS = EDIS.readEndianInt();

		// Close the Stream
		EDIS.close();

		// Read in a new Stream for the remaining fields
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, NWRDS*4);

		// Field 2 -- Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
//Message.printStatus(10,routine,"ID = "+parseChar);
/*		
		if (parseChar.length() == 0 ||
			!parseChar.equalsIgnoreCase(((NWSRFS_ORRS)
			NWSRFS_Object).getID())) {
			Message.printStatus(10,routine, 
				"Reading from PPPPARM"+
				logicalUnitNum+" file did not produce correct "+
				"results. The read in identifier is not "+
				"the same as the one expected identifier! "+
				"The read in ID: "+parseChar+
				" and the expected ID:"+((NWSRFS_ORRS)
				NWSRFS_Object).getID());
			return false;
		}
*/
		// Field 3 -- parameter type
		charValue = new char[4];
		for (i = 0; i < 4; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
//Message.printStatus(10,routine,"Type = "+parseChar);
		
		if (parseChar.length() == 0 ||
			!parseChar.equalsIgnoreCase(paramType)) {
			Message.printStatus(10,routine, "Reading from PPPPARM "+ logicalUnitNum+
					" file did not produce correct results. The read in parameter type is not "+
				"the same as the one expected parameter type! The read in paramType: "+parseChar+
				" and the expected parmType:"+paramType);
			return false;
		}
		
		// Field 4 - IRECNX
		IRECNX = checkInt(EDIS.readEndianInt(), 0, 100000, -1);
		
		// Start reading in GENERAL parameters
		// Field 5 - ORRS Parameter array version
		EDIS.readEndianFloat();
		
		// Field 6 -- General ORRS Indicator of how list was ordered
		((NWSRFS_ORRS)NWSRFS_Object).setORRSListInd((int)EDIS.readEndianFloat());
//Message.printStatus(10,routine,"ORRS Order Indicator = "+(int)EDIS.readEndianFloat());
		
		// Field 7 -- General ORRS Unused
		EDIS.readEndianFloat();
		EDIS.readEndianFloat();
		
		// Field 8 -- General ORRS number of stations (NSTA)
		((NWSRFS_ORRS)NWSRFS_Object).setORRSNSTA((int)EDIS.readEndianFloat());
		int orrsNSTA = ((NWSRFS_ORRS)NWSRFS_Object).getORRSNSTA();
//Message.printStatus(10,routine,"ORRS NSTA = "+orrsNSTA);
		
		// Close the Stream
		EDIS.close();

		// Read in a new Stream for the remaining fields
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, orrsNSTA*4);

		// Field 9 -- General ORRS Vector of Record numbers
		for(i=0; i<orrsNSTA; i++) {
			((NWSRFS_ORRS)NWSRFS_Object).addORRSIREC((short)EDIS.readEndianFloat());
//Message.printStatus(10,routine,"ORRS Record Numbers = "+(short)EDIS.readEndianFloat());
		}

		// Close the EDIS
		EDIS.close();
	}
	else if(paramType.equalsIgnoreCase("STBN")) { // General Parameter
		Message.printStatus(10,routine, "Parameter Type: "+	paramType+" has not been implemented yet.");
		return false;
	}
	else if(paramType.equalsIgnoreCase("USER")) { // General Parameter
		logicalUnitNum=((NWSRFS_USER)NWSRFS_Object).getLogicalUnitNum();
		recordNum=((NWSRFS_USER)NWSRFS_Object).getRecordNum();
		
		// Determine the PPPARMn file where n is the logicalUnitNum.
		pppParamFileName += logicalUnitNum;
		
		// Get the file handles, etc. to open the RA file.
		for(i = 17; i < 22; i++) {
			if(__dbFileNames[i].equalsIgnoreCase(pppParamFileName)) {
				pppParamIndex = i;
				break;
			}
		}
		
		// If pppParamIndex = -1 then we had a big problem! The 
		// logicalUnitNum was either > 5 or < 1 so we can not open
		// the correct parameter file to get the data!! Print a message
		// and return false.
		if(pppParamIndex == -1) {
			Message.printStatus(10,routine, "Logical Unit Number for the Station: "+ logicalUnitNum+
					". This unit number is not correct and must be between 1 and 5! "+
				"The parameter binary file can not be openned.");
			return false;
		}
		
		// Open the RA file.  Check if the the database binary file is open as a Random Access object
		if (!checkRandomAccessFileOpen(pppParamIndex, true)) {
			throw new Exception("Can not open the " + __dbFileNames[pppParamIndex] + " binary database file");
		}

//__NWSRFS_DBFiles[pppParamIndex].seek(0);
//__NWSRFS_DBFiles[pppParamIndex].seek(
//	(recordNum-1)*__byteLength[pppParamIndex]);
//EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, 128);
//charValue = new char[128];
//byte byteValue;
//for (i = 0; i < 128; i++) {
//	byteValue = EDIS.readByte();
//	charValue[i] = (char)byteValue;
//	Message.printStatus(10,routine,"byteValue["+i+"] = "+byteValue);
//	Message.printStatus(10,routine,"charValue["+i+"] = "+charValue[i]);
//}

		// Now read the parameter file to get the parameter data
		// in the PPPPARMn binary file.
		// Read the record at recordNum to get the parameter data.
		__NWSRFS_DBFiles[pppParamIndex].seek(0);
		__NWSRFS_DBFiles[pppParamIndex].seek((recordNum-1)*__byteLength[pppParamIndex]);
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, 4);

		// Start reading in the general parameters.
		// Field 1 - NWRDS
		NWRDS = EDIS.readEndianInt();

		// Close the Stream
		EDIS.close();

		// Read in a new Stream for the remaining fields
		EDIS = read(__NWSRFS_DBFiles[pppParamIndex], 0, NWRDS*4);

		// Field 2 -- Identifier
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
//		if (parseChar.length() == 0 ) {
//			Message.printStatus(10,routine, 
//				"Reading from PPPPARM"+
//				logicalUnitNum+" file did not produce correct "+
//				"results. The read in identifier is not "+
//				"the same as the one expected identifier! "+
//				"The read in ID: "+parseChar+
//				" and the expected ID:"+((NWSRFS_USER)
//				NWSRFS_Object).getID());
//			return false;
//		}
//		else {
			// Set the ID since we do not know it APRIORI
			((NWSRFS_USER)NWSRFS_Object).setID(parseChar);
//		}
		
		// Field 3 -- parameter type
		charValue = new char[4];
		for (i = 0; i < 4; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() == 0 || !parseChar.equalsIgnoreCase(paramType)) {
			Message.printStatus(10,routine, "Reading from PPPPARM "+ logicalUnitNum+
					" file did not produce correct results. The read in parameter type is not "+
				"the same as the one expected parameter type!  The read in paramType: "+parseChar+
				" and the expected parmType:"+paramType);
			return false;
		}
		
		// Field 4 - IRECNX
		IRECNX = checkInt(EDIS.readEndianInt(), 0, 100000, -1);
		
		// Start reading in GENERAL parameters
		// Field 5 - USER Parameter array version
		int arrayVer = (int)EDIS.readEndianFloat();
		
		// Field 6 -- General USER Name
		charValue = new char[8];
		for (i = 0; i < 8; i++) {
			charValue[i] = EDIS.readEndianChar1();
		}
		
		parseChar = new String(charValue).trim();
		
		if (parseChar.length() != 0) {
			((NWSRFS_USER)NWSRFS_Object).setUSERName(parseChar);
		}
		
		// Field 7 -- General USER Set Beginning Summer Month
		((NWSRFS_USER)NWSRFS_Object).setUSERBegSummerMon((int)EDIS.readEndianFloat());
		
		// Field 8 -- General USER Set Beginning Winter Month
		((NWSRFS_USER)NWSRFS_Object).setUSERBegWinterMon((int)EDIS.readEndianFloat());
		
		// Field 9 -- General USER Subset of MDR Grid being used --
		// Western most column
		((NWSRFS_USER)NWSRFS_Object).setUSERMDRWestColumn((int)EDIS.readEndianFloat());
		
		// Field 10 -- General USER Subset of MDR Grid being used -- Number of columns
		((NWSRFS_USER)NWSRFS_Object).setUSERMDRNumColumns((int)EDIS.readEndianFloat());
		
		// Field 11 -- General USER Subset of MDR Grid being used -- Southern most row
		((NWSRFS_USER)NWSRFS_Object).setUSERMDRSouthRow((int)EDIS.readEndianFloat());
		
		// Field 12 -- General USER Subset of MDR Grid being used -- Number of rows
		((NWSRFS_USER)NWSRFS_Object).setUSERMDRNumRows(	(int)EDIS.readEndianFloat());
		
		// Field 13 -- General USER Latitude Limits -- Northern limit
		((NWSRFS_USER)NWSRFS_Object).setUSERLatitudeNorthLimit(	EDIS.readEndianFloat());
		
		// Field 14 -- General USER Latitude Limits -- Southern limit
		((NWSRFS_USER)NWSRFS_Object).setUSERLatitudeSouthLimit(	EDIS.readEndianFloat());
		
		// Field 15 -- General USER Longitude Limits -- Eastern limit
		((NWSRFS_USER)NWSRFS_Object).setUSERLongitudeEastLimit(	EDIS.readEndianFloat());
		
		// Field 16 -- General USER Longitude Limits -- Western limit
		((NWSRFS_USER)NWSRFS_Object).setUSERLongitudeWestLimit(	EDIS.readEndianFloat());
		
		// Field 17 -- General USER Elevation Limits -- Maximum
		((NWSRFS_USER)NWSRFS_Object).setUSERElevationMax(EDIS.readEndianFloat());
		
		// Field 18 -- General USER Elevation Limits -- Minimum
		((NWSRFS_USER)NWSRFS_Object).setUSERElevationMin(EDIS.readEndianFloat());
		
		// Now get additional parameter data if deepRead is specified.
		// Right now this does nothing. As budget is available additional parameters will be defined!
		if(deepRead || arrayVer > 1) {
			// TODO SAT 09/29/2004
			// Field 19 -- General USER Blend Period MAT 
//			((NWSRFS_USER)NWSRFS_Object).setUSER((int)EDIS.readEndianFloat());
			EDIS.readEndianFloat();
		
			// TODO SAT 09/29/2004
			// Field 20 -- General USER Blend Period MAPE 
//			((NWSRFS_USER)NWSRFS_Object).setUSER((int)EDIS.readEndianFloat());
			EDIS.readEndianFloat();
		
			// TODO SAT 09/29/2004
			// Field 21 -- General USER MAP Status Indicator 
//			((NWSRFS_USER)NWSRFS_Object).setUSER((int)EDIS.readEndianFloat());
			EDIS.readEndianFloat();
		
			// TODO SAT 09/29/2004
			// Field 22 -- General USER MAT Status Indicator
//			((NWSRFS_USER)NWSRFS_Object).setUSER((int)EDIS.readEndianFloat());
			EDIS.readEndianFloat();
		
			// TODO SAT 09/29/2004
			// Field 23 -- General USER Exponent of 1/D**POWER for MAP 
//			((NWSRFS_USER)NWSRFS_Object).setUSER((int)EDIS.readEndianFloat());
			EDIS.readEndianFloat();
		
			// TODO SAT 09/29/2004
			// Field 24 -- General USER Exponent of 1/D**POWER for MAT 
//			((NWSRFS_USER)NWSRFS_Object).setUSER((int)EDIS.readEndianFloat());
			EDIS.readEndianFloat();
		
			// TODO SAT 09/29/2004
			// Field 25 -- General USER Exponent of 1/D**POWER for MAPE 
//			((NWSRFS_USER)NWSRFS_Object).setUSER((int)EDIS.readEndianFloat());
			EDIS.readEndianFloat();
		
			// TODO SAT 09/29/2004
			// Field 26 -- General USER Min daily Precip 
//			((NWSRFS_USER)NWSRFS_Object).setUSER((int)EDIS.readEndianFloat());
			EDIS.readEndianFloat();
		
			// TODO SAT 09/29/2004
			// Field 27 -- General USER Min Weight of Stations 
//			((NWSRFS_USER)NWSRFS_Object).setUSER((int)EDIS.readEndianFloat());
			EDIS.readEndianFloat();
		
			// TODO SAT 09/29/2004
			// Field 28 -- General USER Sorting station indicator
//			((NWSRFS_USER)NWSRFS_Object).setUSER((int)EDIS.readEndianFloat());
			charValue = new char[4];
			for (i = 0; i < 4; i++) {
				charValue[i] = EDIS.readEndianChar1();
			}
		
			parseChar = new String(charValue).trim();
		
//			if (parseChar.length() != 0) {
//				((NWSRFS_USER)NWSRFS_Object).setUSER(
//					parseChar);
//			}
		
			// TODO SAT 09/29/2004
			// Field 29 -- General USER Number of user run defaults
//			((NWSRFS_USER)NWSRFS_Object).setUSER( (int)EDIS.readEndianFloat());
			EDIS.readEndianFloat();
		
			// TODO SAT 09/29/2004
			// Field 30 -- General USER Max lines per page 
//			((NWSRFS_USER)NWSRFS_Object).setUSER((int)EDIS.readEndianFloat());
			EDIS.readEndianFloat();
		
			// TODO SAT 09/29/2004
			// Field 31 -- General USER Option to begin commands on new page
//			((NWSRFS_USER)NWSRFS_Object).setUSER( (int)EDIS.readEndianFloat());
			EDIS.readEndianFloat();
		
			// TODO SAT 09/29/2004
			// Field 32 -- General USER Option to overprint errors and warnings
//			((NWSRFS_USER)NWSRFS_Object).setUSER( (int)EDIS.readEndianFloat());
			EDIS.readEndianFloat();
		
			// TODO SAT 09/29/2004
			// Field 33 -- General USER Option to print log of commands
//			((NWSRFS_USER)NWSRFS_Object).setUSER( (int)EDIS.readEndianFloat());
			EDIS.readEndianFloat();
		
			// REVIST SAT 09/29/2004
			// Field 34 -- General USER Unused 
			EDIS.readEndianFloat();
		
			// If we get here and the arrayVer <= 1 we do not
			// want to overwrite the Grid values from above!
			if(arrayVer > 1) {
			// REVIST SAT 09/29/2004
			// Field 35 -- General USER Optional HRAP Grid Western most column
//			((NWSRFS_USER)NWSRFS_Object).setUSER( (int)EDIS.readEndianFloat());
			((NWSRFS_USER)NWSRFS_Object).setUSERMDRWestColumn( (int)EDIS.readEndianFloat());
		
			// REVIST SAT 09/29/2004
			// Field 36 -- General USER Optional HRAP Grid Number of columns
//			((NWSRFS_USER)NWSRFS_Object).setUSER( (int)EDIS.readEndianFloat());
			((NWSRFS_USER)NWSRFS_Object).setUSERMDRNumColumns( (int)EDIS.readEndianFloat());
		
			// REVIST SAT 09/29/2004
			// Field 37 -- General USER Optional HRAP Grid Southern most row
//			((NWSRFS_USER)NWSRFS_Object).setUSER( (int)EDIS.readEndianFloat());
			((NWSRFS_USER)NWSRFS_Object).setUSERMDRSouthRow( (int)EDIS.readEndianFloat());
		
			// REVIST SAT 09/29/2004
			// Field 38 -- General USER Optional HRAP Grid Num rows 
//			((NWSRFS_USER)NWSRFS_Object).setUSER( (int)EDIS.readEndianFloat());
			((NWSRFS_USER)NWSRFS_Object).setUSERMDRNumRows(	(int)EDIS.readEndianFloat());
			}
		}

		// Close the EDIS
		EDIS.close();
	}
	else if(paramType.equalsIgnoreCase("URRS")) { // General Parameter
		Message.printStatus(10,routine, "Parameter Type: "+	paramType+" has not been implemented yet.");
		return false;
	}
	else {
		Message.printStatus(10,routine, 
			"Unkown Parameter Type: "+ paramType+". This could be either a misspelling or "+
			"a parameter type that has not been implemented yet.");
		return false;
	}
	
	return true;
}

/**
Read from a data file. The record number and byte length can be used for 
determining the exact place starting to read in this data file. This method is 
for static byte length binary files where data falls evenly on the record 
boundries. The data are read on a record by record basis so the bytes to read 
is the same as a record byte length.
@param raFile a EndianRandomAccessFile to the binary file to read. 
@param recordNumber a long used to set the position of the file to read the 
correct record. 
@param byteLength an integer used to tell the method how many bytes a 
record contains. The method reads "byteLength" bytes from the file at location 
"recordNumber*byteLength" bytes.
@return an endianDataInputStream which uses a byte array holding the binary 
record which is turned into a byteArrayInputStream.
@throws Exception if it catches an exception while trying to read the file.
*/
protected EndianDataInputStream read(EndianRandomAccessFile raFile,
long recordNumber, int byteLength) throws Exception 
{
	// TODO (JTS - 2004-08-19)
	// this method is the same as the next one!!
	// overload this one and have it call the next one with the number
	// of bytes to read -- perhaps if bytesToRead is -1 then just read
	// them all.

	// Check for negative byteLength
	if (byteLength < 0) {
		byteLength = 0;
	}

	byte[] record = new byte[byteLength]; 
		// Array of bytes holding binary data retrieved from file

	raFile.skipBytes((int)recordNumber * byteLength);

	// Get the record as a byte array
	for (int i = 0; i < byteLength; i++) {
		record[i] = (byte)raFile.readByte();
	}
	
	// Create the ByteArrayInputStream
	ByteArrayInputStream BAIS = new ByteArrayInputStream(record);

	// Create the EndianDataInputStream and check for the Endianess
	EndianDataInputStream EDIS = new EndianDataInputStream((InputStream)BAIS,true);
	EDIS.setBigEndian(__isBigEndian);

	return EDIS;
}

/**
Read from a data file. The record number and byte length can be used for 
determining the exact place starting to read in this data file. This overload 
method also takes a number of bytes in which to read from the location. This 
assumes that the bytes to read will be different than byte length of the record 
and is intended for binary files that have virtual record lengths.
@param raFile an EndianRandomAccessFile to the binary file to read.
@param recordNumber a long used to set the position of the file to read the 
correct record. 
@param byteLength an integer used to tell the method how many bytes a record 
contains. The method reads "byteLength" bytes from the file at location 
"recordNumber*byteLength" bytes.
@param bytesToRead an integer used to tell the method how many bytes to read 
at the starting location. 
@return an endianDataInputStream which uses a byte array holding the binary 
record which is turned into a byteArrayInputStream.
@throws Exception if an error occurs trying to read the file.
*/
protected EndianDataInputStream read(EndianRandomAccessFile raFile,
long recordNumber, int byteLength,int bytesToRead) 
throws Exception, EOFException, NullPointerException {
	return read(raFile, recordNumber, byteLength, bytesToRead, false);
}

// TODO (JTS - 2004-08-21)
// being used for testing efficiency of read operations -- leave in for now ...
protected EndianDataInputStream read(EndianRandomAccessFile raFile,
long recordNumber, int byteLength,int bytesToRead, boolean testing) 
throws Exception {
	StopWatch sw1 = new StopWatch();
	StopWatch sw2 = new StopWatch();
	StopWatch sw3 = new StopWatch();

	// Check for negative bytesToRead
	if (bytesToRead < 0) {
		bytesToRead = 0;
	}

	sw1.start();

	byte[] record = new byte[bytesToRead];
		// Array of bytes holding binary data retrieved from file
	raFile.skipBytes((int)recordNumber*byteLength);
	
	sw1.stop();
	sw2.start();

		// Get the record as a byte array
		for (int i = 0; i < bytesToRead; i++) {
			record[i] = (byte)raFile.readByte();
		}

	sw2.stop();
	sw3.start();
	
	// Create the ByteArrayInputStream
	ByteArrayInputStream BAIS = new ByteArrayInputStream(record);
	sw3.stop();

	// Create the EndianDataInputStream and check for the Endianess
	EndianDataInputStream EDIS = new EndianDataInputStream((InputStream)BAIS,true);
	EDIS.setBigEndian(__isBigEndian);

	//if (testing) {
	//	Message.printStatus(1, "", "    1: " + sw1.getSeconds());
	//	Message.printStatus(1, "", "    2: " + sw2.getSeconds());
	//	Message.printStatus(1, "", "    3: " + sw3.getSeconds());
	//	Message.printStatus(1, "", "");
	//}
	
	return EDIS;
}

/** 
Reads the preprocessed parameteric database to fill the data members of the
NWSRFS_BASN object argument. It will read the information from the 
preprocessed parameteric database (PPDB) files PPPPARM<i>n</i> where <i>n</i> 
is the logical unit found from the PPPINDEX file associated with a specific 
parameter type. The known "basin" parameter types in the PPDB are BASN 
(Basin Boundary parameters).
@param basin a NWSRFS_BASN which holds the minimum set of data for a 
Basin Boundary dervied from the PPPINDEX file. This method will fill out the 
Basin Boundary object.
@param deepRead a boolean specifying whether to read all Basin parameters
from the PPDB or just general parameters.
@return  an NWSRFS_BASN object which stores the data from
the PPDB files PPPARMn.
@throws Exception if an error is detected.
*/
public NWSRFS_BASN readBASNParam(NWSRFS_BASN basin, boolean deepRead) 
throws Exception
{
	NWSRFS_PPPINDEX pppindex = getPPPIndex();

	// Check to see if the pppindex file exists! If not return empty list.
	if(pppindex == null) { 
		setPPPIndex(readPPPINDEX());
	}
	else if(pppindex.getPARMTP() == null) {
		return basin;
	}
	
	// Fill the BASN object.
	parseParametericArray((Object)basin,"BASN",deepRead);
	
	// Return the filled out Basin object
	return basin;
}

/**
Reads in to a Vector of NWSRFS_BASN the list of Basin Boundary identifiers. It
will basically regurgetate the PPPINDEX file which creates a list of BASN ids 
and a record number.
@return Vector of NWSRFS_BASN objects containing the list of all BASN ids 
and record numbers in the database.
@throws Exception if something goes wrong.
*/
public Vector readBASNParamList() throws Exception
{
	Vector basinList = new Vector();
	int logicalUnitNum = -1;
	int numberOFParamRecs = -1;
	NWSRFS_PPPINDEX pppindex = getPPPIndex();
	NWSRFS_BASN basin;

	// Check to see if the pppindex file exists! If not return empty list.
	if(pppindex == null) { 
		setPPPIndex(readPPPINDEX());
	}
	else if(pppindex.getPARMTP() == null) {
		return basinList;
	}
	
	// Get the logical unit for the parameter type.
	for(int i=0;i<(pppindex.getPARMTP()).size();i++) {
		if(((String)(pppindex.getPARMTP()).elementAt(i)).
		equalsIgnoreCase("BASN")) {
			logicalUnitNum = ((Integer)pppindex.getLUFILE().elementAt(i)).intValue();
			numberOFParamRecs = ((Integer)pppindex.getNUMPRM().elementAt(i)).intValue();
			break;
		}
	}
	
	// Now get the records. If only one get only that record and use the 
	// FIRST value from the pppindex file.
	if(numberOFParamRecs <= 0) {
		return basinList;
	}
	else if(numberOFParamRecs == 1) {
		// Get the logical unit for the parameter type.
		for(int i=0;i<(pppindex.getPARMTP()).size();i++) {
			if(((String)(pppindex.getPARMTP()).elementAt(i)).equalsIgnoreCase("BASN")) {
				basin = new NWSRFS_BASN((String)(pppindex.getID()).elementAt(i));
				basin.setLogicalUnitNum(logicalUnitNum);
				basin.setRecordNum(((Integer)(pppindex.getFIRST()).elementAt(i)).intValue());
				basinList.addElement((NWSRFS_BASN)basin);
				break;
			}
		}
	}
	else {
		// Loop through the ID.size number of records
		for(int i=0;i<(pppindex.getID()).size();i++) {
			// If the type is BASN add to the Vector
			if(((String)(pppindex.getITYPE()).elementAt(i)).equalsIgnoreCase("BASN")) {
				basin = new NWSRFS_BASN((String)(pppindex.getID()).elementAt(i));
					basin.setLogicalUnitNum(logicalUnitNum);
					basin.setRecordNum(((Integer)(pppindex.getIREC()).elementAt(i)).intValue());
					basinList.addElement((NWSRFS_BASN)basin);
			}
		}
	}
	
	// Return Vector of NWSRFS_BASN objects
	return basinList;
}

/** 
This method is used to read in Segment Carryover from the FCCARRY binary 
database file. This read will be more convoluted than others since the data 
from the FCCARRY file is not in nice discrete values. Rather it holds Carryover
values in a C array for each Carryover slot, for each Segment in a float array. 
This array will then need to be parsed to get the actual useful Carryover 
information like "Operation Number" and "Carryover Values". This method will 
read one or more records for each Carryover slot in the FCCARRY file and process
the first String which will be the Segment ID and compare to the Segment ID from
the Segment object passed to the method. If they match the NWSRFS_Carryover 
method <code>parseRecord(...)</code> will be called to parse the record(s) into 
the C array then parse the array into the NWSRFS_Carryover's public Vectors.
@param segObject the NWSRFS_Segment object that this Carryover class will use to
be instantiated and compare ID's for reading in the FCCARRY binary database 
data.
@param deepRead a boolean used to determine whether to read all carryover slots 
or just the first.
@return Vector of NWSRFS_Carryover objects which stores the data from the 
FCCARRY binary database file.
@throws Exception if segment object is NULL or segment objects parent FG and 
CG's are NULL.
*/
public Vector readCarryover(NWSRFS_Segment segObject, boolean deepRead) 
throws Exception {
	// Check to see if the segObject is null. If so throw an Exception
	if (segObject == null) {
		throw new Exception("The Segment object used as an argument is null.");
	}

	// Get the Forecast Group which is the parent of the segment.
	NWSRFS_ForecastGroup fgObject = segObject.getForecastGroup();
	if (fgObject == null) {
		throw new Exception("Can not get Carryover data for Segment: "
			+ segObject.getIDSEG() + " - The Forecast Group object is null.");
	}

	// Get the number of slots from the CarryoverGroup
	// which is the grandparent of the segment and parent of Forecast Group.
	NWSRFS_CarryoverGroup cgObject = fgObject.getCarryoverGroup();
	if (cgObject == null) {
		throw new Exception("Can not get Carryover data for Segment: "
			+ segObject.getIDSEG() + " - The Carryover Group object is null.");
	}

	// Check if the the database binary file is open as a Random Access object

	// TODO (JTS - 2004-08-19)
	// this variable is set throughout the class and is ALWAYS defined
	// to be true.  Can it be removed??

	boolean readOFSFS5Files = true;
	if (!checkRandomAccessFileOpen(__FCCARRY, readOFSFS5Files)) {
		throw new Exception("Can not get Carryover data for Segment: "
			+ segObject.getIDSEG() + " - Can not open the "
			+ __dbFileNames[__FCCARRY] + " binary database file");
	}

// TODO (JTS - 2004-08-21)
// explain the magic number 40

	// Number of bytes to read for Carryover for each slot for the Segment.
	int bytesToRead = 40 + segObject.getNC();

	// Now determine whether or not to read all the carryover slots or just the first.
	int slotIndex = -1;
	if (deepRead) {
		slotIndex = cgObject.getNSLOTS();
	}
	else {
		slotIndex = 1;
	}

	// Now loop through all of the Carryover slots to get the record.
	char[] charValue = null;
	EndianDataInputStream EDIS = null;
	int j = -1;
	int seekPosition = -1;
	String IDSEG = null;
	String parseChar = null;
	for (int i = 0; i < slotIndex; i++) {
		// Rewind the file to the beginning.
		rewind(__NWSRFS_DBFiles[__FCCARRY]);

		// Determine where to "seek" which will be the "slot number"*
		// "the number of records in a slot" + "segment byte offset"
		seekPosition = (i + 1) * cgObject.getNRSLOT() + ((segObject.getIWOCRY() - 1) * __WORDSIZE);
		seek(__NWSRFS_DBFiles[__FCCARRY], seekPosition, readOFSFS5Files);

		// Read the number of bytes for this Segment
		EDIS = read(__NWSRFS_DBFiles[__FCCARRY], 0, __byteLength[__FCCARRY], bytesToRead);

		// Read the values of the record
		// Field 1 - [type field name here]
		charValue = new char[8];
		for (j = 0; j < 8; j++) {
			charValue[j] = EDIS.readEndianChar1();
		}
		parseChar = new String(charValue).trim();
		if (parseChar.length() != 0) {
			IDSEG = parseChar;
		}

		// Now do the compare to see if the IDSEG read from FCCARRY
		// equals the IDSEG from the passed in Segment object.
		if (IDSEG != null) {
			if (IDSEG.equalsIgnoreCase(segObject.getIDSEG())) {
				// Parse the record (i.e. the Byte Array Stream is the record) 
				// The Segment ID was already read from the EDIS
				// stream so it is only needed to parse the rest
				// of the record which constitutes the C array.
				parseCarryoverRecord(EDIS,segObject,i);
			}
		}

		EDIS.close();
	}

	// Return the Vector of Carryover objects
	return segObject.getCarryover();
}

/** 
This method is used to read in the values from the FCCOGDEF NWSRFS Fortran 
database file into the data members of the NWSRFS_CarryoverGroup class. The data
members of this class will constitute the storage of information about a 
Carryover Group in the NWSRFS system.
@param CG_ID the Carryover Group identifier String in which to pull information 
from the Fortran database file.
@return NWSRFS_CarryoverGroup the NWSRFS_CarryoverGroup object which stores the 
data from the FCCOGDEF binary database file. This class should be 
instantiated by the user.
@throws Exception if the binary file can not be opened.
*/
public NWSRFS_CarryoverGroup readCarryoverGroup(String CG_ID) 
throws Exception {
	return readCarryoverGroup(CG_ID,false);
}

/** 
This method is used to read in the values from the FCCOGDEF NWSRFS Fortran 
database file into the data members of the NWSRFS_CarryoverGroup class. The data
members of this class will constitute the storage of information about a 
Carryover Group in the NWSRFS system.
@param CG_ID the Carryover Group identifier String in which to pull information 
from the Fortran database file.
@param deepRead a boolean used to determine whether to read all of the database 
fields or not. If true read all of the data.
@return NWSRFS_CarryoverGroup the NWSRFS_CarryoverGroup object which stores the 
data from the FCCOGDEF binary database file. 
@throws Exception if the binary file can not be opened.
*/
public NWSRFS_CarryoverGroup readCarryoverGroup(String CG_ID, boolean deepRead) 
throws Exception {
	NWSRFS_CarryoverGroup cgFile = new NWSRFS_CarryoverGroup(CG_ID);

	// Check if the the database binary file is open as a
	// Random Access object
	if (!checkRandomAccessFileOpen(__FCCOGDEF, true)) {
		throw new Exception("Can not open the " 
			+ __dbFileNames[__FCCOGDEF] + " binary database file");
	}

	rewind(__NWSRFS_DBFiles[__FCCOGDEF]);
	// Read the first record which holds the record number for the
	// specific carryover group to get records for.
	EndianDataInputStream EDIS = read(__NWSRFS_DBFiles[__FCCOGDEF],0,
		__byteLength[__FCCOGDEF]);

	// If read only header for CGs then do not store
	// any of the first record but do need to read
	// a couple of fields to get the record number of the
	// CG_ID have passed in.
	char[] charValue = null;
	int bytesToSkip = 0;
	int i = 0;
	int j = 0;
	int recordNum = -1;
	String parseChar = null;
	if (!deepRead) {
		// Skip the bytes not needed
// TODO (JTS - 2004-08-21)
// explain the magic number 44 and 25
		bytesToSkip = 44;
		EDIS.skipBytes(bytesToSkip);

		int[] ICOREC = new int[25];
		String[] CGIDS = new String[25];

		// Get the field values and store in local variables
		// Field 7 - [type field name here]
		for (i = 0; i < 25; i++) {
			charValue = new char[8];
			for (j = 0; j < 8; j++)	{
				charValue[j] = EDIS.readEndianChar1();
			}
			parseChar = new String(charValue).trim();
			if (parseChar.length() != 0) {
				CGIDS[i] = parseChar;
			}
		}

		// Field 8 - [type field name here]
		for (i = 0; i < 25;i++) {
			ICOREC[i] = checkInt(EDIS.readEndianInt(), 0, 26, -1);
		}

		// Find the record number of the carryover group that 
		// corresponds to the passed in id
		for (i = 0; i < 25; i++) {
			try {
				if (CGIDS[i].equalsIgnoreCase(CG_ID)) {
					recordNum = ICOREC[i];
					break;
				}
			}
			catch (NullPointerException NPe) {
				// Should never get here
				exceptionCount++;
				break;
			}
		}
	}
	else {
		// Read the values of record one
		// Field 1 - [type field name here]
		cgFile.setNSLOTS(checkInt(EDIS.readEndianInt(), 0, 20, -1));
		
		// Field 2 - [type field name here]
		cgFile.setNWR(checkInt(EDIS.readEndianInt(), 0, 400, -1));
		
		// Field 3 - [type field name here]
		cgFile.setNRSLOT(checkInt(EDIS.readEndianInt(), 0, 100000, -1));
		
		// Bytes 13-16 are skipped
		// TODO (JTS - 2004-08-21)
		// why?
		EDIS.readEndianInt();
		
		// Field 4 - [type field name here]
		cgFile.setNWPS(checkInt(EDIS.readEndianInt(), 0, 4000000, -1));

		// Field 5 - [type field name here]
		for (i = 0; i < 5; i++) {
			cgFile.setICRDAT(i, checkInt(EDIS.readEndianInt(),
				0, 10000, -1));
		}

		// Field 6 - [type field name here]
		cgFile.setNCG(checkInt(EDIS.readEndianInt(), 0, 25, -1));
		
		// Field 7 - [type field name here]
		for (i = 0; i < 25; i++) {
			charValue = new char[8];
			for (j = 0; j < 8; j++) {
				charValue[j] = EDIS.readEndianChar1();
			}
			parseChar = new String(charValue).trim();
			if (parseChar.length() != 0) {
				cgFile.setCGIDS(i, parseChar);
			}
		}

		// Field 8 - [type field name here]
		for (i = 0; i < 25; i++) {
			cgFile.setICOREC(i, checkInt(EDIS.readEndianInt(),
				0, 26, -1));
		}

		// Find the record number of the carryover group that 
		// corresponds to the passed-in id
		for (i = 0; i < 25; i++) {
			try {
				if (cgFile.getCGIDS(i).equalsIgnoreCase(CG_ID)){
					recordNum = cgFile.getICOREC(i);
				}
			}
			catch (NullPointerException NPe) {
				exceptionCount++;
				// TODO (JTS - 2004-08-18)
				// handle this?
				break;
			}
		}
	}

	// Now rewind to get the record
	rewind(__NWSRFS_DBFiles[__FCCOGDEF]);

	//Get the record which holds the members of the CG definition status
	EDIS = read(__NWSRFS_DBFiles[__FCCOGDEF], recordNum - 1,
		__byteLength[__FCCOGDEF]);
	
	// Now if read only the header only store the CGID, CGNAME, and
	// Number of FGs in this CG
	if (!deepRead) {
		// Set the CGID
		cgFile.setCGID(CG_ID);

		// Skip the bytes not needed
		bytesToSkip = 28;
		EDIS.skipBytes(bytesToSkip);

		// Field 3 - [type field name here]
		cgFile.setNFG(checkInt(EDIS.readEndianInt(), 0, 10000, -1));

		// Field 4 - skip - [type field name here]
		// TODO (JTS - 2004-08-21)
		// why?
		EDIS.readEndianInt();

		// Field 5 - [type field name here]
		charValue = new char[20];
		for (j = 0; j < 20; j++) {
			charValue[j] = EDIS.readEndianChar1();
		}
		parseChar = new String(charValue).trim();
		if (parseChar.length() != 0) {
			cgFile.setCGNAME(parseChar);
		}

		// Field 6 - [type field name here]
		for (i = 0; i < 20; i++) {
			cgFile.setICODAY(i, checkInt(EDIS.readEndianInt(),
				0, 1000000, -1));
		}
	
		// Field 7 - [type field name here]
		for (i = 0; i < 20; i++) {
			cgFile.setICOTIM(i, checkInt(EDIS.readEndianInt(),
				0, 100000, -1));
		}

		// Field 8 - [type field name here]
		for (i = 0; i < 20;i++) {
			cgFile.setLUPDAY(i, checkInt(EDIS.readEndianInt(),
				0, 1000000, -1));
		}

		// Field 9 - [type field name here]
		for (i = 0; i < 20; i++) {
			cgFile.setLUPTIM(i, checkInt(EDIS.readEndianInt(),
				0, 100000, -1));
		}
	}
	else {
		// Field 1 - [type field name here]
		charValue = new char[8];
		for (j = 0; j < 8; j++) {
			charValue[j] = EDIS.readEndianChar1();
		}
		parseChar = new String(charValue).trim();
		if (parseChar.length() != 0) {
			cgFile.setCGID(parseChar);
		}

		// Field 2 - [type field name here]
		for (i = 0; i < 5; i++) {
			cgFile.setITDEF(i, checkInt(EDIS.readEndianInt(),
				0, 10000, -1));
		}
	
		// Field 3 - [type field name here]
		cgFile.setNFG(checkInt(EDIS.readEndianInt(), 0, 10000, -1));

		// Field 4 - [type field name here]
		cgFile.setMINDT(checkInt(EDIS.readEndianInt(), 0, 24, -1));

		// Field 5 - [type field name here]
		charValue = new char[20];
		for (j = 0; j < 20; j++) {
			charValue[j] = EDIS.readEndianChar1();
		}
		parseChar = new String(charValue).trim();
		if (parseChar.length() != 0) {
			cgFile.setCGNAME(parseChar);
		}

		// Field 6 - [type field name here]
		for (i = 0; i < 20; i++) {
			cgFile.setICODAY(i, checkInt(EDIS.readEndianInt(),
				0, 1000000, -1));
		}
	
		// Field 7 - [type field name here]
		for (i = 0; i < 20; i++) {
			cgFile.setICOTIM(i, checkInt(EDIS.readEndianInt(),
				0, 100000, -1));
		}

		// Field 8 - [type field name here]
		for (i = 0; i < 20;i++) {
			cgFile.setLUPDAY(i, checkInt(EDIS.readEndianInt(),
				0, 1000000, -1));
		}

		// Field 9 - [type field name here]
		for (i = 0; i < 20; i++) {
			cgFile.setLUPTIM(i, checkInt(EDIS.readEndianInt(),
				0, 100000, -1));
		}

		// Field 10 - [type field name here]
		for (i = 0; i < 20; i++) {
			cgFile.setIPC(i, checkInt(EDIS.readEndianInt(),
				0, 10, -1));
		}
	}

	// Get forecast group list for this carryover group
	Vector forecastGroupIDs;
	if(cgFile.getNFG() > 0)
		forecastGroupIDs = new Vector((int)cgFile.getNFG());
	else
		forecastGroupIDs = new Vector();

	forecastGroupIDs = readForecastGroupList(cgFile);
	try {
		for (i = 0; i < forecastGroupIDs.size(); i++) {
			cgFile.addForecastGroupID(
				(String)forecastGroupIDs.elementAt(i));
		}
	}
	catch (NullPointerException NPe) {
		exceptionCount++;
		// TODO (JTS - 2004-08-18)
		// do something?
	}

	EDIS.close();
	
	return cgFile;
}	

// TODO 2004-05-27 SAT Possibly put in a search pattern to get a subset list
/** 
This method returns the IDS of all the carryover groups defined in the database.
The method pulls the CGIDS String array from the database. 
@return a Vector of String Carryover Group IDs from the FCCOGDEF binary 
database file.
@throws Exception if the database could not be opened.
*/
public Vector readCarryoverGroupList() 
throws Exception
{
	// Check if the the database binary file is open as a Random Access object
	String routine = "NWSRFS_DMI.readCarryoverGroupList";
	int cgIDSLength = 0;
	int sl = 2;	// Status level

	// TODO (JTS - 2004-08-19)
	// always defined to be true in this class and used like this.  Can these be removed?	
	boolean readOFSFS5Files = true;
	if (!checkRandomAccessFileOpen(__FCCOGDEF, readOFSFS5Files)) {
		throw new Exception("Cannot open the " 	+ __dbFileNames[__FCCOGDEF] + " binary database file");
	}

	// read only the first record to get the CGIDS array
	rewind(__NWSRFS_DBFiles[__FCCOGDEF]);
	// Read the first record which holds the record number for the
	// specific carryover group to get records for.
	EndianDataInputStream EDIS = read(__NWSRFS_DBFiles[__FCCOGDEF], 0, __byteLength[__FCCOGDEF]);
		
	// Create an instance of NWSRFS_Segment
	NWSRFS_CarryoverGroup cgFile = new NWSRFS_CarryoverGroup();

	char[] charValue;
	int i = 0;
	int j = 0;
	String parseChar = null;

	// Read the values of record one
	// Field 1 - NSLOTS
	cgFile.setNSLOTS(checkInt(EDIS.readEndianInt(), 0, 20, -1));
	//cgFile.setNSLOTS((int)EDIS.readEndianFloat());
	Message.printStatus(sl,routine,"NSLOTS: "+cgFile.getNSLOTS());

	// Field 2 - NWR
	cgFile.setNWR(checkInt(EDIS.readEndianInt(), 0, 400, -1));
	//cgFile.setNWR((int)EDIS.readEndianFloat());
	Message.printStatus(sl,routine,"NWR: "+cgFile.getNWR());

	// Field 3 - NRSLOT
	cgFile.setNRSLOT(checkInt(EDIS.readEndianInt(), 0, 100000, -1));
	//cgFile.setNRSLOT((int)EDIS.readEndianFloat());
	Message.printStatus(sl,routine,"NRSLOT: "+cgFile.getNRSLOT());

	// Bytes 13-16 are skipped
	// TODO (JTS - 2004-08-21) explain why
	EDIS.readEndianInt();

	// Field 4 - NWPS
	cgFile.setNWPS(checkInt(EDIS.readEndianInt(), 0, 4000000, -1));
	//cgFile.setNWPS((int)EDIS.readEndianFloat());
	Message.printStatus(sl,routine,"NWPS: "+cgFile.getNWPS());

	// Field 5 - ICRDAT
	for (i = 0; i < 5; i++)	{
		cgFile.setICRDAT(i, checkInt(EDIS.readEndianInt(), 0, 10000, -1));
		//cgFile.setICRDAT(i,(int)EDIS.readEndianFloat());
		Message.printStatus(sl,routine,"ICRDAT["+i+"]: "+(cgFile.getICRDAT())[i]);
	}

	// Field 6 - NCG
	cgFile.setNCG(checkInt(EDIS.readEndianInt(), 0, 25, -1));
	//cgFile.setNCG((int)EDIS.readEndianFloat());
	Message.printStatus(sl,routine,"NCG: "+cgFile.getNCG());

	// Field 7 - CGIDS
	for (i = 0; i < 25; i++) {
		charValue = new char[8];
		for (j = 0; j < 8; j++) {
			charValue[j] = EDIS.readEndianChar1();
		}
		parseChar = new String(charValue).trim();
		Message.printStatus(sl,routine,"CGIDS["+i+"]: "+parseChar);

		if (parseChar.length() != 0) {
			cgFile.setCGIDS(i, parseChar);
			cgIDSLength++;
		}
	}

	// Field 8 - ICOREC
	for (i = 0; i < 25; i++) {
		cgFile.setICOREC(i, checkInt(EDIS.readEndianInt(), 0, 26, -1));
	}

	// Now loop through the CGIDS and create an NWSRFS_CarryoverGroup object for each ID
	Vector cgObjs = new Vector();
	int vectSize = cgFile.getNCG();
	if(vectSize < cgIDSLength) {
		vectSize = cgIDSLength;
		cgFile.setNCG(vectSize);
	}
	
	for (i = 0; i < vectSize; i++) {
		cgObjs.add(cgFile.getCGIDS(i));
	}

	EDIS.close();
	return cgObjs;
}	

/** 
Read the values from the system DATATYPE file.
@throws Exception if the the DATATYPE file cannot be read.
*/
public void readDataTypeList() 
throws Exception {
	File testExists = null;
	String inputDTFile = "DATATYPE";
	String rfs_sys_dir;

	try {
		rfs_sys_dir = NWSRFS_Util.getAppsDefaults("rfs_sys_dir");
	} catch(Exception e) {
		// If rfs_sys_dir string is null then print message
		rfs_sys_dir = "/awips/hydroapps/lx/rfc/nwsrfs/sys_files/";
	}

	rfs_sys_dir = rfs_sys_dir.trim();
	if (!rfs_sys_dir.endsWith(File.separator)) {
		// If the rfs_sys_dir environment variable does 
		// not have a file separator on the end, put one on.
		rfs_sys_dir = rfs_sys_dir + File.separator;
	}

	// Set the input file for reading
	inputDTFile = rfs_sys_dir + inputDTFile;

	// Check to see if this exists if not throw an Exception
	testExists = new File(inputDTFile);

	// Read in the files.
	if (testExists.exists()) {
		DataType.readNWSDataTypeFile(inputDTFile);
	}
	else {
		throw new Exception("Unable to find the Data Types file. Using defaults.");
	}
}

/** 
This method is used to read in the values from the system DATAUNITS file.
@throws Exception if DATAUNITS cannot be read.
*/
public void readDataUnitsList() 
throws Exception {
	File testExists = null;
	String inputDUFile = "DATAUNIT";
	String rfs_sys_dir;

	try {
		rfs_sys_dir = NWSRFS_Util.getAppsDefaults("rfs_sys_dir");
	} catch(Exception e) {
		// If rfs_sys_dir string is null then print message
		rfs_sys_dir = "/awips/hydroapps/lx/rfc/nwsrfs/sys_files/";
	}

	rfs_sys_dir = rfs_sys_dir.trim();
	if (!rfs_sys_dir.endsWith(File.separator)) {
		// If the rfs_sys_dir environment variable does 
		// not have a file separator on the end, put one on.
		rfs_sys_dir = rfs_sys_dir + File.separator;
	}

	// Set the input file for reading
	inputDUFile = rfs_sys_dir + inputDUFile;

	// Check to see if this exists if not throw an Exception
	testExists = new File(inputDUFile);

	// Read in the files.
	if (testExists.exists()) {
		DataUnits.readNWSUnitsFile(inputDUFile);
	}
	else {
		throw new Exception("Unable to find the Data Units file. Using defaults.");
	}
}

/** 
This method is used to read the values from an ESP trace ensemble binary file
into a TS object.
@param filename a String value representing the path to an ESP trace ensemble 
binary file.
@param read_data Indicates whether data should be read (specify false to 
only read header information).
@return NWSRFS_ESPTraceEnsemble the NWSRFS_ESPTraceEnsemble object which 
stores the data from an ESP trace binary file into a TS object.
@throws Exception if there was an error reading from the esp trace ensemble
*/
public NWSRFS_ESPTraceEnsemble readESPTraceEnsemble(String filename, 
boolean read_data) 
throws Exception {
	NWSRFS_ESPTraceEnsemble espTE = null;

	// Create an instance of NWSRFS_ESPTraceEnsemble which will 
	// do the read of the data
	espTE = new NWSRFS_ESPTraceEnsemble(filename, this, 
		read_data, true);

	// Return the instance of NWSRFS_FCRCPTR
	return espTE;
}

/** 
This method is used to read in the values from the FCRCPTR NWSRFS forecast 
component file into the data members of the NWSRFS_FCRCPTR class. The data 
members of this class will be used to pull data from the FCRATING binary 
database file.
@return NWSRFS_FCRCPTR the NWSRFS_FCRCPTR object which stores the data from 
the FCRCPTR binary database file. This is an index class and will be 
automatically instantiated when the NWSRFS_DMI is instantiated. This is so 
that the index to the FCRATING binary file will be in memory.
@throws Exception if hte database file cannot be opened or read.
*/
private NWSRFS_FCRCPTR readFCRCPTR() 
throws Exception {
	NWSRFS_FCRCPTR ptrFile = new NWSRFS_FCRCPTR();

	// Check if the the database binary file is open as a
	// Random Access object
// TODO (JTS - 2004-08-19)
// always defined to be true in this class and used like this.  Can these
// be removed?	
	boolean readOFSFS5Files = true;
	if (!checkRandomAccessFileOpen(__FCRCPTR, readOFSFS5Files)) {
		throw new Exception("Can not open the "
			+ __dbFileNames[__FCRCPTR] + " binary database file");
	}

	// Get the first record which holds the number of rating curves defined.
	EndianDataInputStream EDIS = read(__NWSRFS_DBFiles[__FCRCPTR], 0,
		__byteLength[__FCRCPTR]);

	// Now get record one information. The three public int members
	// will make up the 12 bytes - 4 bytes each. 
	ptrFile.setNRC(checkInt(EDIS.readEndianInt(), 0, 100000, -1));

	ptrFile.setMRC(checkInt(EDIS.readEndianInt(), 0, 100000, -1));

	ptrFile.setMRCF(checkInt(EDIS.readEndianInt(), 0, 100000, -1));
	
	// Close the EndianDataInputStream
	EDIS.close();

	char[] segChar = null;
	int i = 0;
	int intValue = -1;
	String parseChar = null;

	// Now loop through the remaining FCRCPTR file records to read 
	// in all the record values.
	while (true) {
		// keep reading until an exception is caught
		try {
			// Define the segChar character array to 
			// hold segment info
			segChar = new char[8];

			// Get the record which holds the members of the 
			// rating curves index.
			EDIS = read(__NWSRFS_DBFiles[__FCRCPTR], 0,
				__byteLength[__FCRCPTR]);

			// Now parse the characters in stream
			for (i = 0; i < 8; i++) {
				segChar[i] = EDIS.readEndianChar1();
			}
		
			parseChar = new String(segChar).trim();

			// Add rating curve Id
			if (parseChar.length() == 0) {
				continue; 
			}
			else {
				ptrFile.addRCID(parseChar);
			}

			// Add record number to rating curve definition
			intValue = EDIS.readEndianInt();

			if (intValue < 0 || intValue > 100000) {
				ptrFile.addIREC(Integer.valueOf("-1"));
			}
			else {
				ptrFile.addIREC(
					Integer.valueOf(
					String.valueOf(intValue)));
			}

			EDIS.close();
		}
		catch (EOFException EOFe) {
			exceptionCount++;
			// TODO (JTS - 2004-08-21)
			// maybe print a warning or something?
			EDIS.close();
			break;
		}
		catch (NullPointerException NPe) {
			exceptionCount++;
			// TODO (JTS - 2004-08-21)
			// maybe print a warning or something?
			EDIS.close();
			break;
		}
	}

	return ptrFile;
}

/** 
Reads the values from the FCSEGPTR NWSRFS processed database file into 
the data members of the NWSRFS_FCSEGPTR class. The data members of this 
class will be used to pull data from the FCSEGSTS binary database file.
@return NWSRFS_FCSEGPTR the NWSRFS_FCSEGPTR object which stores the data 
from the FCSEGPTR binary database file. This is an index class and will be 
automatically instantiated when the NWSRFS_DMI is instantiated. This is so 
that the index to the FCSEGSTS binary file will be in memory.
@throws Exception if the database file cannot be opened or read.
*/
private NWSRFS_FCSEGPTR readFCSEGPTR() 
throws Exception {
	NWSRFS_FCSEGPTR ptrFile = new NWSRFS_FCSEGPTR();

	// Check if the the database binary file is open as a
	// Random Access object
// TODO (JTS - 2004-08-19)
// always defined to be true in this class and used like this.  Can these
// be removed?	
	boolean readOFSFS5Files = true;
	if (!checkRandomAccessFileOpen(__FCSEGPTR, readOFSFS5Files)) {
		throw new Exception("Can not open the "
			+ __dbFileNames[__FCSEGPTR]
			+ " binary database file");
	}

	// Get the first record.
	EndianDataInputStream EDIS = read(__NWSRFS_DBFiles[__FCSEGPTR], 0,
		__byteLength[__FCSEGPTR]);

	// Now get record one information. The three public int members
	// will make up the 12 bytes - 4 bytes each. 
	ptrFile.setNSEG(checkInt(EDIS.readEndianInt(), 0, 100000, -1));
	ptrFile.setNRECST(checkInt(EDIS.readEndianInt(), 0, 100000, -1));
	ptrFile.setMAXRST(checkInt(EDIS.readEndianInt(), 0, 100000, -1));

	// Close the EndianDataInputStream
	EDIS.close();

	// Get the second record. 
	EDIS = read(__NWSRFS_DBFiles[__FCSEGPTR], 0,
		__byteLength[__FCSEGPTR]);

	// Now get record one information. The three public int members
	// will make up the 12 bytes - 4 bytes each. 
	ptrFile.setNRECP(checkInt(EDIS.readEndianInt(), 0, 100000, -1));
	ptrFile.setMAXRP(checkInt(EDIS.readEndianInt(), 0, 100000, -1));
	ptrFile.setNWPRP(checkInt(EDIS.readEndianInt(), 0, 100000, -1));
	
	// Close the EndianDataInputStream
	EDIS.close();

	char[] segChar = null;
	int i = 0;
	int intValue = -1;
	String parseChar = null;

	// Now loop through the FCSEGPTR file to read in all the record values
	while (true) {
		// keep reading until an EOFException is caught
		try {
			// Define the segChar character array to hold 
			// segment info
			segChar = new char[8];

			// Get the record which holds the members of 
			// the rating curves index.
			EDIS = read(__NWSRFS_DBFiles[__FCSEGPTR], 0,
				__byteLength[__FCSEGPTR]);

			// Now parse the characters in stream
			for (i = 0; i < 8; i++) {
				segChar[i] = EDIS.readEndianChar1();
			}
		
			parseChar = new String(segChar).trim();

			// Add segment Id.
			if (parseChar.length() == 0) {
				continue;
			}
			else {
				ptrFile.addISEG(parseChar);
			}

			// Get record number for the segment definition
			intValue = EDIS.readEndianInt();

			if (intValue < 0 || intValue > 100000) {
				ptrFile.addIREC(Integer.valueOf("-1"));
			}
			else {
				ptrFile.addIREC(Integer.valueOf(
					String.valueOf(intValue)));
			}

			EDIS.close();
		}
		catch (EOFException EOFe) {
			exceptionCount++;
// TODO (JTS - 2004-08-21)
// handle exception?		
			break;
		}
		catch (NullPointerException NPe) {
			exceptionCount++;
// TODO (JTS - 2004-08-21)
// handle exception?		
			break;
		}
	}

	return ptrFile;
}

/** 
Reads data into an NWSRFS_ForecastGroup object. The method 
reads FCFGSTAT processed database field to fill variables in the object. 
It will also read the FCFGLIST binary database file to get the segment id 
for all the segments in the forecast group and call the readSegment(
String segmentID, NWSRFS_ForecastGroup fcFile) method to add the segments to 
this NWSRFS_ForecastGroup object.
@param FGID the forecast group id in which to get all of the fields.
@return an NWSRFS_ForecastGroup object which stores the data from
the FCFGSTAT binary database file. This class should be instantiated 
by the user.
@throws Exception if there is a problem reading from the database.
*/
public NWSRFS_ForecastGroup readForecastGroup(String FGID) 
throws Exception {
	return readForecastGroup(FGID,false);
}

/** 
reads data into an NWSRFS_ForecastGroup object. The method 
reads FCFGSTAT processed database field to fill variables in the object. 
It will also read the FCFGLIST binary database file to get the segment id 
for all the segments in the forecast group and call the readSegment(String 
segmentID, NWSRFS_ForecastGroup fcFile) method to add the segments to this 
NWSRFS_ForecastGroup object.
@param FGID the forecast group id in which to get all of the fields.
@param deepRead a boolean used to determine whether to read all of 
the forecast group fields or not. If false read only FGID, number of 
segments, FG description, and segment IDs.
@return  an NWSRFS_ForecastGroup object which stores the data from
the FCFGSTAT binary database file. This class should be instantiated by 
the user.
@throws Exception if there are any problems reading from the database.
*/
public NWSRFS_ForecastGroup readForecastGroup(String FGID, boolean deepRead) 
throws Exception {

	// Will need to read the FCFGSTAT binary database until there is a
	// match with forecast group id passed as an argument.
	// First create an instance of NWSRFS_Segment
	NWSRFS_ForecastGroup fgFile = new NWSRFS_ForecastGroup(FGID);

	// Check if the the database binary file is open as a
	// Random Access object
// TODO (JTS - 2004-08-19)
// always defined to be true in this class and used like this.  Can these
// be removed?	
	boolean readOFSFS5Files = true;
	if (!checkRandomAccessFileOpen(__FCFGSTAT, readOFSFS5Files)) {
		throw new Exception("Can not open the " 
			+ __dbFileNames[__FCFGSTAT] + " binary database file");
	}

	rewind(__NWSRFS_DBFiles[__FCFGSTAT]);

	char[] charValue;
	EndianDataInputStream EDIS = null;
	int bytesToSkip = 0;
	int i = 0;
	String fgIdent = null;
	String parseChar = null;
	String SegID = null;

	// Need to continue until an EOFException is caught
	while (true) {
		try {
			// Read the record 
			EDIS = read(__NWSRFS_DBFiles[__FCFGSTAT], 0,
				__byteLength[__FCFGSTAT]);
		
			// Field 1 - [type field name here]
			charValue = new char[8];
			for (i = 0; i < 8; i++) {
				charValue[i] = EDIS.readEndianChar1();
			}
			parseChar = new String(charValue).trim();
			if (parseChar.length() != 0) {
				fgIdent = parseChar;
			}
		
			// Now check to see if the FGIDs match. If so
			// fill the rest of the data otherwise continue
			// the loop
			if (!fgIdent.equalsIgnoreCase(FGID)) {
				continue;
			}
			else {
				fgFile.setFGID(FGID);
			}

			// Field 2 - [type field name here]
			fgFile.setNSEG(checkInt(EDIS.readEndianInt(),
				0, 100000, -1));
			
			// If deepRead is false read FG description
			if (!deepRead) {
				// Field3
				fgFile.setIREC(checkInt(EDIS.readEndianInt(),
					0, 100000, -1));
	
				// Skip bytes not needed
// TODO (JTS - 2004-08-21)
// why not just combine all these things into a single line?
				bytesToSkip = 20;
				EDIS.skipBytes(bytesToSkip);

				// Field 8 - [type field name here]
				charValue = new char[20];
				for (i = 0; i < 20; i++) {
					charValue[i] = EDIS.readEndianChar1();
				}
				parseChar = new String(charValue).trim();
				if (parseChar.length() != 0) {
					fgFile.setDESCR(parseChar);
				}
			
				break;
			}
			else {
				// Field 3 - [type field name here]
				fgFile.setIREC(checkInt(EDIS.readEndianInt(),
					0, 100000, -1));
	
				// Field 4 - [type field name here]
				fgFile.setISPEC(checkInt(EDIS.readEndianInt(),
					0, 3, -1));

				// Field 5 - [type field name here]
				charValue = new char[8];
				for (i = 0; i < 8; i++) {
					charValue[i] = EDIS.readEndianChar1();
				}
				parseChar = new String(charValue).trim();
				if (parseChar.length() != 0) {
					fgFile.setCGID(parseChar);
				}
			
				// Field 6 - [type field name here]
				fgFile.setICOSEQ(checkInt(EDIS.readEndianInt(),
					0, 10000, -1));

				// Field 7 - [type field name here]
				fgFile.setMINDT(checkInt(EDIS.readEndianInt(),
					0, 24, -1));

				// Field 8 - [type field name here]
				charValue = new char[20];
				for (i = 0; i < 20; i++) {
					charValue[i] = EDIS.readEndianChar1();
				}
				parseChar = new String(charValue).trim();
				if (parseChar.length() != 0) {
					fgFile.setDESCR(parseChar);
				}
			
				// Field 9 - [type field name here]
				for (i = 0; i < 5; i++)	{
					fgFile.setICRDAT(i, checkInt(
						EDIS.readEndianInt(),
						0, 4000, -1));
				}

				// Break out of the loop since have 
				// what is needed
				break;
			}
		}
		catch (EOFException EOFe) {
			exceptionCount++;
// TODO (JTS - 2004-08-21)
// handle exception?		
			break;
		}
	}

	// Get the segments in this forecast group
	// Close the initial EndianDataInputStream
	EDIS.close();

	// Check if the the database binary file is open as a
	// Random Access object
	if (!checkRandomAccessFileOpen(__FCFGLIST, readOFSFS5Files)) {
		throw new Exception("Can not open the " 
			+ __dbFileNames[__FCFGLIST] + " binary database file");
	}

	// Set the file position
	rewind(__NWSRFS_DBFiles[__FCFGLIST]);
	if (fgFile.getIREC() > 0) {
// TODO (JTS - 2004-08-21)
// explain the magic number 8	
		seek(__NWSRFS_DBFiles[__FCFGLIST], (fgFile.getIREC() - 1) * 8,
			readOFSFS5Files);
	}

	int j = 0;
	int nseg = fgFile.getNSEG();
	for (i = 0; i < nseg; i++) {
		// Read the record from the FCFGLIST binary file to 
		// get segment ID
		EDIS = read(__NWSRFS_DBFiles[__FCFGLIST], 0, 8);

		// Read the values of the record
		// Field 1 - [type field name here]
		charValue = new char[8];
		for (j = 0; j < 8; j++) {
			charValue[j] = EDIS.readEndianChar1();
		}
		parseChar = new String(charValue).trim();
		if (parseChar.length() != 0) {
			SegID = parseChar;
		}

		fgFile.addSegmentID(SegID);
	}	
	
	EDIS.close();

	// Return the instance of Vector of NWSRFS_ForecastGroup
	return fgFile;
}

/** 
Reads all the IDs of the forecast groups in the specified carryover group and 
returns them in a Vector of Strings.  The method pulls the FGID and CGID 
Strings from the database to get all of the FGIDs in the passed in CG object. 
@param cg the carryover group object for which to get all of the forecast group objects.
@return  a vector of NWSRFS_ForecastGroup objects which stores the data from
the FCFGSTAT binary database file. This class should be instantiated by the user.
@throws Exceptiton if there is an error reading from the database.
*/
public Vector readForecastGroupList(NWSRFS_CarryoverGroup cg) 
throws Exception
{	String routine = "NWSRFS_DMI.readForecastGroupList";
	char[] charValue;
	EndianDataInputStream EDIS = null;
	int i = 0;
	int sl = 0;	// Status level for messages (used in troubleshooting)
	String parseChar = null;
	Vector fgObjs = new Vector();

	// Will need to read the entire FCFGSTAT binary database to get the
	// forecast groups associated with the given carryover group id.
	// This is not optimal but there is no other choice since there is no
	// record index that points to which FG in the file are associated with
	// a specific CG. This means that looping through the entire file 
	// comparing the CGID's to find all of the forecast groups is required.
	// First create an instance of NWSRFS_ForecastGroup
// TODO (JTS - 2004-08-19)
// always defined to be true in this class and used like this.  Can these be removed?	
	boolean readOFSFS5Files = true;
	
	// Check if the the database binary file is open as a Random Access object
	if (!checkRandomAccessFileOpen(__FCFGSTAT, readOFSFS5Files)) {
		throw new Exception("Cannot open the "+ __dbFileNames[__FCFGSTAT] + " binary database file");
	}

	rewind(__NWSRFS_DBFiles[__FCFGSTAT]);

	// Need to continue until an EOFException is caught
	while (true) {
		try {
			EDIS = read(__NWSRFS_DBFiles[__FCFGSTAT], 0, __byteLength[__FCFGSTAT]);
			// Create a new forecast group to hold the information (add to list if matches CG).
			NWSRFS_ForecastGroup fgFile = new NWSRFS_ForecastGroup(null);
	
			// Field 1 - [type field name here]
			charValue = new char[8];
			for (i = 0; i < 8; i++)	{
				charValue[i] = EDIS.readEndianChar1();
			}
			parseChar = new String(charValue).trim();
			if (parseChar.length() != 0) {
				fgFile.setFGID(parseChar);
			}
		
			// Field 2 - [type field name here]
			fgFile.setNSEG(checkInt(EDIS.readEndianInt(), 0, 100000, -1));
		
			// Field 3 - [type field name here]
			fgFile.setIREC(checkInt(EDIS.readEndianInt(), 0, 100000, -1));
	
			// Field 4 - [type field name here]
			fgFile.setISPEC(checkInt(EDIS.readEndianInt(), 0, 3, -1));

			// Field 5 - [type field name here]
			charValue = new char[8];
			for (i = 0; i < 8; i++)	{
				charValue[i] = EDIS.readEndianChar1();
			}
			parseChar = new String(charValue).trim();
			if (parseChar.length() != 0) {
				fgFile.setCGID(parseChar);
			}

			// If the forecast group belongs to the carryover group
			// add the forecast group to the Vector
			if ( (fgFile.getCGID() != null) && fgFile.getCGID().equalsIgnoreCase(cg.getCGID())) {
				fgObjs.add(fgFile.getFGID());
				Message.printStatus(sl, routine,
						"Adding FG \"" + fgFile.getFGID() + "\" for CG \"" + cg.getCGID() + "\"" );
			}
			else {
				continue;
			}
		}
		catch (EOFException EOFe) {
			exceptionCount++;
// TODO (JTS - 2004-08-21) handle exception?		
			break;
		}
	}
		
	EDIS.close();

	return fgObjs;
}

/**
Reads global data from the NWSRFS process database files.
This is primarily from the index file for the various large database file.
For instance the segment definition file is quite large so read in here
an index file that associates SEGID to a record number in the segment definition
file. This will speed reading of the database significantly. Create static
objects to hold the index data.
@throws Exception reading global dat from the FCRCPTR, FCSEGPTR or PPPINDEX files.
*/	
protected void readGlobalData() 
throws Exception {
	String routine = "NWSRFS_DMI.readGlobalData";
	// Start to load the index data into the index datafile classes
	// Create pointer and read data into index file objects
	_fcrcptr = readFCRCPTR();
	_fcsegptr = readFCSEGPTR();
//	_pdbindex = readPDBINDEX();
//	_pppindex = readPPPINDEX();
//	_prdindex = readPRDINDEX();
	
	// Make sure the DataTypes and DataUnits are called
	try {
		readDataTypeList();
	}
	catch(Exception e) {
		Message.printWarning(2,routine,"Error reading DATATYPE file.  Using defaults.");
	}
	try {
		readDataUnitsList();
	}
	catch(Exception e) {
		Message.printWarning(2,routine, "Error reading DATATYPE file. Using defaults." );
	}
}

/** 
Reads the preprocessed parameteric database to fill the data members of the
NWSRFS_MAP object argument. It will read the information from the 
preprocessed parameteric database (PPDB) files PPPPARM<i>n</i> where <i>n</i> 
is the logical unit found from the PPPINDEX file associated with a specific 
parameter type. The known "areal" parameter types in the PPDB are MAP 
(MAP Areas), MAPS (MAP Satellite Areas), MAPE (potential evaporation areas), 
MAPX (MAP NEXRAD Areas), and MAT (MAT Areas).
@param mapArea a NWSRFS_MAP which holds the minimum set of data for a 
MAP Area dervied from the PPPINDEX file. This method will fill out the MAP Area object.
@param deepRead a boolean specifying whether to read all MAP parameters
from the PPDB or just general parameters.
@return  an NWSRFS_MAP object which stores the data from the PPDB files PPPARMn.
@throws Exception if an error is detected.
*/
public NWSRFS_MAP readMAPArea(NWSRFS_MAP mapArea, boolean deepRead) 
throws Exception
{
	NWSRFS_PPPINDEX pppindex = getPPPIndex();

	// Check to see if the pppindex file exists! If not return empty list.
	if(pppindex == null) { 
		setPPPIndex(readPPPINDEX());
	}
	else if(pppindex.getPARMTP() == null) {
		return mapArea;
	}
	
	// Fill the MAP object.
	parseParametericArray((Object)mapArea,"MAP",deepRead);
	
	// Return the filled out MAP object
	return mapArea;
}

// TODO - SAT - 09/20/2004 - Need to create NWSRFS_MAPArea, etc. objects
// so the readMAPArea() methods can be written to actually get and store the
// PPDB data for the Areal PPDB parameter types.
/**
Reads in to a Vector of Strings the list of MAP area identifiers. It will
basically regurgetate the PPPINDEX file which creates a list of MAP ids and a
record number.
@return Vector of Strings containing the list of all MAP Area ids in the database.
@throws Exception if something goes wrong.
*/
public Vector readMAPAreaList() throws Exception
{
	Vector mapAreaList = new Vector();
	int logicalUnitNum = -1;
	int numberOFParamRecs = -1;
	NWSRFS_PPPINDEX pppindex = getPPPIndex();
	NWSRFS_MAP map;

	// Check to see if the pppindex file exists! If not return empty list.
	if(pppindex == null) { 
		setPPPIndex(readPPPINDEX());
	}
	else if(pppindex.getPARMTP() == null) {
		return mapAreaList;
	}
	
	// Get the logical unit for the parameter type.
	for(int i=0;i<(pppindex.getPARMTP()).size();i++) {
		if(((String)(pppindex.getPARMTP()).elementAt(i)).equalsIgnoreCase("MAP")) {
			logicalUnitNum = ((Integer)pppindex.getLUFILE().elementAt(i)).intValue();
			numberOFParamRecs = ((Integer)pppindex.getNUMPRM().elementAt(i)).intValue();
			break;
		}
	}
	
	// Now get the records. If only one get only that record and use the 
	// FIRST value from the pppindex file.
	if(numberOFParamRecs <= 0) {
		return mapAreaList;
	}
	else if(numberOFParamRecs == 1) {
		// Get the logical unit for the parameter type.
		for(int i=0;i<(pppindex.getPARMTP()).size();i++) {
			if(((String)(pppindex.getPARMTP()).elementAt(i)).equalsIgnoreCase("MAP")) {
				map = new NWSRFS_MAP((String)(pppindex.getID()).elementAt(i));
				map.setLogicalUnitNum(logicalUnitNum);
				map.setRecordNum(((Integer)(pppindex.getFIRST()).elementAt(i)).intValue());
				mapAreaList.addElement((NWSRFS_MAP)map);
				break;
			}
		}
	}
	else {
		// Loop through the ID.size number of records
		for(int i=0;i<(pppindex.getID()).size();i++) {
			// If the type is MAP add to the Vector
			if(((String)(pppindex.getITYPE()).elementAt(i)).equalsIgnoreCase("MAP")) {
				map = new NWSRFS_MAP((String)(pppindex.getID()).elementAt(i));
					map.setLogicalUnitNum(logicalUnitNum);
					map.setRecordNum(((Integer)(pppindex.getIREC()).elementAt(i)).intValue());
					mapAreaList.addElement((NWSRFS_MAP)map);
			}
		}
	}
	
	// Return Vector of map area ids
	return mapAreaList;
}

/**
Reads in to a Vector of Strings the list of MAPE area identifiers. It will
basically regurgetate the PPPINDEX file which creates a list of MAPE ids and a record number.
@return Vector of Strings containing the list of all MAPE Area ids in the database.
@throws Exception if something goes wrong.
*/
public Vector readMAPEAreaList() throws Exception
{
	Vector mapeAreaList = new Vector();
	NWSRFS_PPPINDEX pppindex = getPPPIndex();
	
	// Check to see if the pppindex file exists! If not return empty list.
	if(pppindex == null) { 
		setPPPIndex(readPPPINDEX());
	}
	else if(pppindex.getPARMTP() == null) {
		return mapeAreaList;
	}
	
	// Loop through the ID.size number of records
	for(int i=0;i<(pppindex.getID()).size();i++) {
		// If the type is MAPE add to the Vector
		if(((String)(pppindex.getITYPE()).elementAt(i)).equalsIgnoreCase("MAPE")) {
			mapeAreaList.addElement((String)(pppindex.getID()).elementAt(i));
		}
	}
	
	// Return Vector of map area ids
	return mapeAreaList;
}

/**
Reads in to a Vector of Strings the list of MAPS area identifiers. It will
basically regurgetate the PPPINDEX file which creates a list of MAPE ids and a record number.
@return Vector of Strings containing the list of all MAPS Area ids in the database.
@throws Exception if something goes wrong.
*/
public Vector readMAPSAreaList() throws Exception
{
	Vector mapsAreaList = new Vector();
	NWSRFS_PPPINDEX pppindex = getPPPIndex();
	
	// Check to see if the pppindex file exists! If not return empty list.
	if(pppindex == null) { 
		setPPPIndex(readPPPINDEX());
	}
	else if(pppindex.getPARMTP() == null) {
		return mapsAreaList;
	}
	
	// Loop through the ID.size number of records
	for(int i=0;i<(pppindex.getID()).size();i++) {
		// If the type is MAPS add to the Vector
		if(((String)(pppindex.getITYPE()).elementAt(i)).equalsIgnoreCase("MAPS")) {
			mapsAreaList.addElement((String)(pppindex.getID()).elementAt(i));
		}
	}
	
	// Return Vector of maps area ids
	return mapsAreaList;
}

/**
Reads in to a Vector of Strings the list of MAPX area identifiers. It will
basically regurgetate the PPPINDEX file which creates a list of MAPE ids and a record number.
@return Vector of Strings containing the list of all MAPX Area ids in the database.
@throws Exception if something goes wrong.
*/
public Vector readMAPXAreaList() throws Exception
{
	Vector mapxAreaList = new Vector();
	NWSRFS_PPPINDEX pppindex = getPPPIndex();
	
	// Check to see if the pppindex file exists! If not return empty list.
	if(pppindex == null) { 
		setPPPIndex(readPPPINDEX());
	}
	else if(pppindex.getPARMTP() == null) {
		return mapxAreaList;
	}
	
	// Loop through the ID.size number of records
	for(int i=0;i<(pppindex.getID()).size();i++) {
		// If the type is MAPX add to the Vector
		if(((String)(pppindex.getITYPE()).elementAt(i)).equalsIgnoreCase("MAPX")) {
			mapxAreaList.addElement((String)(pppindex.getID()).elementAt(i));
		}
	}
	
	// Return Vector of mapx area ids
	return mapxAreaList;
}

/** 
Reads the preprocessed parameteric database to fill the data members of the
NWSRFS_MAT object argument. It will read the information from the 
preprocessed parameteric database (PPDB) files PPPPARM<i>n</i> where <i>n</i> 
is the logical unit found from the PPPINDEX file associated with a specific 
parameter type. The known "areal" parameter types in the PPDB are MAP 
(MAP Areas), MAPS (MAP Satellite Areas), MAPE (potential evaporation areas), 
MAPX (MAP NEXRAD Areas), and MAT (MAT Areas).
@param matArea a NWSRFS_MAT which holds the minimum set of data for a 
MAT Area dervied from the PPPINDEX file. This method will fill out the MAT Area object.
@param deepRead a boolean specifying whether to read all MAT parameters
from the PPDB or just general parameters.
@return  an NWSRFS_MAT object which stores the data from the PPDB files PPPARMn.
@throws Exception if an error is detected.
*/
public NWSRFS_MAT readMATArea(NWSRFS_MAT matArea, boolean deepRead) 
throws Exception
{
	NWSRFS_PPPINDEX pppindex = getPPPIndex();

	// Check to see if the pppindex file exists! If not return empty list.
	if(pppindex == null) { 
		setPPPIndex(readPPPINDEX());
	}
	else if(pppindex.getPARMTP() == null) {
		return matArea;
	}
	
	// Fill the MAP object.
	parseParametericArray((Object)matArea,"MAT",deepRead);
	
	// Return the filled out MAT object
	return matArea;
}

/**
Reads in to a Vector of Strings the list of MAT area identifiers. It will
basically regurgetate the PPPINDEX file which creates a list of MAP ids and a record number.
@return Vector of Strings containing the list of all MAT Area ids in the database.
@throws Exception if something goes wrong.
*/
public Vector readMATAreaList() throws Exception
{
	Vector matAreaList = new Vector();
	int logicalUnitNum = -1;
	int numberOFParamRecs = -1;
	NWSRFS_PPPINDEX pppindex = getPPPIndex();
	NWSRFS_MAT mat;

	// Check to see if the pppindex file exists! If not return empty list.
	if(pppindex == null) { 
		setPPPIndex(readPPPINDEX());
	}
	else if(pppindex.getPARMTP() == null) {
		return matAreaList;
	}
	
	// Get the logical unit for the parameter type.
	for(int i=0;i<(pppindex.getPARMTP()).size();i++) {
		if(((String)(pppindex.getPARMTP()).elementAt(i)).equalsIgnoreCase("MAT")) {
			logicalUnitNum = ((Integer)pppindex.getLUFILE().elementAt(i)).intValue();
			numberOFParamRecs = ((Integer)pppindex.getNUMPRM().elementAt(i)).intValue();
			break;
		}
	}
	
	// Now get the records. If only one get only that record and use the 
	// FIRST value from the pppindex file.
	if(numberOFParamRecs <= 0) {
		return matAreaList;
	}
	else if(numberOFParamRecs == 1) {
		// Get the logical unit for the parameter type.
		for(int i=0;i<(pppindex.getPARMTP()).size();i++) {
			if(((String)(pppindex.getPARMTP()).elementAt(i)).equalsIgnoreCase("MAT")) {
				mat = new NWSRFS_MAT((String)(pppindex.getID()).elementAt(i));
				mat.setLogicalUnitNum(logicalUnitNum);
				mat.setRecordNum(((Integer)(pppindex.getFIRST()).elementAt(i)).intValue());
				matAreaList.addElement((NWSRFS_MAT)mat);
				break;
			}
		}
	}
	else {
		// Loop through the ID.size number of records
		for(int i=0;i<(pppindex.getID()).size();i++) {
			// If the type is MAT add to the Vector
			if(((String)(pppindex.getITYPE()).elementAt(i)).equalsIgnoreCase("MAT")) {
				mat = new NWSRFS_MAT((String)(pppindex.getID()).elementAt(i));
					mat.setLogicalUnitNum(logicalUnitNum);
					mat.setRecordNum(((Integer)(pppindex.getIREC()).elementAt(i)).intValue());
					matAreaList.addElement((NWSRFS_MAT)mat);
			}
		}
	}
	
	// Return Vector of mat area ids
	return matAreaList;
}

/** 
Reads the preprocessed parameteric database to fill the data members of the
NWSRFS_NTWK object argument. It will read the information from the 
preprocessed parameteric database (PPDB) files PPPPARM<i>n</i> where <i>n</i> 
is the logical unit found from the PPPINDEX file associated with a specific 
parameter type. The known "network" parameter types in the PPDB are NTWK 
(Network parameters).
@param basin a NWSRFS_NTWK which holds the minimum set of data for a 
Network object dervied from the PPPINDEX file. This method will fill out the 
Network object.
@param deepRead a boolean specifying whether to read all Network parameters
from the PPDB or just general parameters.
@return  an NWSRFS_NTWK object which stores the data from
the PPDB files PPPARMn.
@throws Exception if an error is detected.
*/
public NWSRFS_NTWK readNTWKParam(NWSRFS_NTWK network, boolean deepRead) 
throws Exception
{
	NWSRFS_PPPINDEX pppindex = getPPPIndex();

	// Check to see if the pppindex file exists! If not return empty list.
	if(pppindex == null) { 
		setPPPIndex(readPPPINDEX());
	}
	else if(pppindex.getPARMTP() == null) {
		return network;
	}
	
	// Fill the NTWK object.
	parseParametericArray((Object)network,"NTWK",deepRead);
	
	// Return the filled out Network object
	return network;
}

/**
Reads in to a Vector of NWSRFS_NTWK the list of Network identifiers. It
will basically regurgetate the PPPINDEX file which creates a list of NTWK ids 
and a record number.Please note that the NTWK parameter record is different
in that it only has one record in the PPPPARAM<i>n</i> file and is not listed
in the PPPINDEX records except in the "FIRST" and "LAST" record of the index!
@return Vector of Strings containing the list of all NTWK param ids in the 
database.
@throws Exception if something goes wrong.
*/
public Vector readNTWKParamList() throws Exception
{
	Vector networkList = new Vector();
	int logicalUnitNum = -1;
	int numberOFParamRecs = -1;
	NWSRFS_PPPINDEX pppindex = getPPPIndex();
	NWSRFS_NTWK network;

	// Check to see if the pppindex file exists! If not return empty list.
	if(pppindex == null) { 
		setPPPIndex(readPPPINDEX());
	}
	else if(pppindex.getPARMTP() == null) {
		return networkList;
	}
	
	// Get the logical unit for the parameter type.
	for(int i=0;i<(pppindex.getPARMTP()).size();i++) {
		if(((String)(pppindex.getPARMTP()).elementAt(i)).equalsIgnoreCase("NTWK")) {
			logicalUnitNum = ((Integer)pppindex.getLUFILE().elementAt(i)).intValue();
			numberOFParamRecs = ((Integer)pppindex.getNUMPRM().elementAt(i)).intValue();
			break;
		}
	}
	
	// Now get the records. If only one get only that record and use the 
	// FIRST value from the pppindex file.
	if(numberOFParamRecs <= 0) {
		return networkList;
	}
	else if(numberOFParamRecs == 1) {
		// Get the logical unit for the parameter type.
		for(int i=0;i<(pppindex.getPARMTP()).size();i++) {
			if(((String)(pppindex.getPARMTP()).elementAt(i)).equalsIgnoreCase("NTWK")) {
				network = new NWSRFS_NTWK((String)(pppindex.getID()).elementAt(i));
				network.setLogicalUnitNum(logicalUnitNum);
				network.setRecordNum(((Integer)(pppindex.getFIRST()).elementAt(i)).intValue());
				networkList.addElement((NWSRFS_NTWK)network);
				break;
			}
		}
	}
	else {
		// Loop through the ID.size number of records
		for(int i=0;i<(pppindex.getID()).size();i++) {
			// If the type is NTWK add to the Vector
			if(((String)(pppindex.getITYPE()).elementAt(i)).equalsIgnoreCase("NTWK")) {
				network = new NWSRFS_NTWK((String)(pppindex.getID()).elementAt(i));
					network.setLogicalUnitNum(logicalUnitNum);
					network.setRecordNum(((Integer)(pppindex.getIREC()).elementAt(i)).intValue());
					networkList.addElement((NWSRFS_NTWK)network);
			}
		}
	}
		
	// Return Vector of NWSRFS_NTWK objects
	return networkList;
}

/** 
Read in Segment Operations from the FCPARAM binary database file. 
@param segObject the NWSRFS_Segment object that this Operation class will use 
to be instantiated and compare ID's for reading in the FCPARAM binary database 
data.
REVISIT (JTS - 2004-08-21)
What???  That's confusing.
@param deepRead a boolean used to determine if all of the data from the 
Operation object and Timeseries object are read. If true read all of the data.
@return Vector of NWSRFS_Operation objects which stores the data from 
the FCPARAM binary database file. This class should be instantiated by 
the user.
@throws Exception if the database could not be read from.
@throws NullPointerException if the segObject is null.
*/
public Vector readOperations(NWSRFS_Segment segObject, boolean deepRead) 
throws Exception {
//StopWatch sw1 = new StopWatch();
//StopWatch sw2 = new StopWatch();
//StopWatch sw3 = new StopWatch();
//StopWatch sw4 = new StopWatch();
//StopWatch sw5 = new StopWatch();
//StopWatch swm = new StopWatch();
//sw1.start();
//swm.start();
	String routine = "NWSRFS_DMI.readOperation()";

	// This read will be more convoluted than others since the data 
	// from the FCPARAM file is not in nice discrete values. Rather 
	// it holds Operation Parameters, Tables, and Timeseries information
	// for each Segment in float, int, and float arrays respectively. 
	// These arrays will then need to be parsed to get the 
	// Operation information like "Operation Number" and "Timeseries 
	// Identifier". This method will read one or more records (at 
	// segObj.IPREC) in the FCPARAM file and process the first String 
	// which will be the Segment ID and compare to the Segment ID from the 
	// Segment object passed to the method. If they match the 
	// NWSRFS_Operation method parseRecord(...) will be called to parse 
	// the record(s) into the P, T, and TS arrays respectively then parse 
	// the arrays into the NWSRFS_Operation's public Vectors.

	if (segObject == null) {
		Message.printWarning(10,routine,"The Segment object used "
			+ "as an argument is null.");
		throw new NullPointerException("The Segment object used as an "
			+ "argument is null.");
	}

	//NWSRFS_Operation opFile = new NWSRFS_Operation(segObject);
	// Check if the the database binary file is open as a
	// Random Access object
	if (!checkRandomAccessFileOpen(__FCPARAM, true)) {
		throw new Exception("Can not open the " 
			+ __dbFileNames[__FCPARAM] + " binary database file");
	}

	// Determine the number of records and bytes to read for this segment
// TODO (JTS - 2004-08-21)
// explain the magic number 2	
	int nwords = 2 + segObject.getNP() + segObject.getNT() + segObject.getNTS();
	//int recordLengthInWords = (int)(__byteLength[__FCPARAM] / 4);
// TODO (JTS - 2004-08-21)
// explain the magic number 1	
	//int recordNum = (int)((nwords + recordLengthInWords - 1)/ recordLengthInWords);
	int bytesToRead = nwords * __WORDSIZE;

	// Read the number of bytes for this Segment
	rewind(__NWSRFS_DBFiles[__FCPARAM]);

//sw3.start();
// TODO (JTS - 2004-08-21)
// explain the magic number 1
	EndianDataInputStream EDIS = read(__NWSRFS_DBFiles[__FCPARAM],
		segObject.getIPREC() - 1, __byteLength[__FCPARAM], bytesToRead,
		true);
//sw3.stop();
	// Field 1 - [type field name here]
	char[] charValue = new char[8];
	for (int j = 0; j < 8; j++) {
		charValue[j] = EDIS.readEndianChar1();
	}
	String parseChar = new String(charValue).trim();
	String IDSEG = null;
	if (parseChar.length() != 0) {
		IDSEG = parseChar;
	}

//sw1.stop();
	// Now do the compare to see if the IDSEG read from FCPARAM
	// equals the IDSEG from the passed in Segment object.
	if (IDSEG.equalsIgnoreCase(segObject.getIDSEG())) {
		// Parse the record (i.e. the Byte Array 
		// Stream is the record). 
		// The Segment ID was already read from the EDIS
		// stream so it is only needed to parse the rest
		// of the record which constitutes the P, T, and
		// TS arrays.
//sw2.start();
		parseOperationRecord(EDIS,segObject,deepRead);
//sw2.stop();
	}

	EDIS.close();
//	swm.stop();
//Message.printStatus(1, "", " Init: " + sw1.getSeconds());
//Message.printStatus(1, "", "    1: " + sw3.getSeconds());
//Message.printStatus(1, "", "    2: " + sw4.getSeconds());
//Message.printStatus(1, "", "    3: " + sw5.getSeconds());
//Message.printStatus(1, "", " Read: " + sw2.getSeconds());
//Message.printStatus(1, "", " ----: " + swm.getSeconds());

	// Return the Vector of NWSRFS_Operation objects
	return segObject.getOperations();
}

/** 
Reads the preprocessed parameteric database to fill the data members of the
NWSRFS_ORRS object argument. It will read the information from the 
preprocessed parameteric database (PPDB) files PPPPARM<i>n</i> where <i>n</i> 
is the logical unit found from the PPPINDEX file associated with a specific 
parameter type. The known "ORRS" parameter types in the PPDB are ORRS 
(alphabetical order RRS parameters).
@param basin a NWSRFS_ORRS which holds the minimum set of data for a 
ORRS object dervied from the PPPINDEX file. This method will fill out the 
ORRS object.
@param deepRead a boolean specifying whether to read all ORRS parameters
from the PPDB or just general parameters.
@return  an NWSRFS_ORRS object which stores the data from
the PPDB files PPPARMn.
@throws Exception if an error is detected.
*/
public NWSRFS_ORRS readORRSParam(NWSRFS_ORRS orrsObj, boolean deepRead) 
throws Exception
{
	NWSRFS_PPPINDEX pppindex = getPPPIndex();

	// Check to see if the pppindex file exists! If not return empty list.
	if(pppindex == null) { 
		setPPPIndex(readPPPINDEX());
	}
	else if(pppindex.getPARMTP() == null) {
		return orrsObj;
	}
	
	// Fill the ORRS object.
	parseParametericArray((Object)orrsObj,"ORRS",deepRead);
	
	// Return the filled out ORRS object
	return orrsObj;
}

/**
Reads in to a Vector of NWSRFS_ORRS the list of ORRS identifiers. It
will basically regurgetate the PPPINDEX file which creates a list of ORRS ids 
and a record number. Please note that the ORRS parameter record is different
in that it only has one record in the PPPPARAM<i>n</i> file and is not listed
in the PPPINDEX records except in the "FIRST" and "LAST" record of the index!
@return Vector of Strings containing the list of all ORRS param ids in the 
database.
@throws Exception if something goes wrong.
*/
public Vector readORRSParamList() throws Exception
{
	Vector orrsList = new Vector();
	int logicalUnitNum = -1;
	int numberOFParamRecs = -1;
	NWSRFS_PPPINDEX pppindex = getPPPIndex();
	NWSRFS_ORRS orrsObj;

	// Check to see if the pppindex file exists! If not return empty list.
	if(pppindex == null) { 
		setPPPIndex(readPPPINDEX());
	}
	else if(pppindex.getPARMTP() == null) {
		return orrsList;
	}
	
	// Get the logical unit for the parameter type.
	for(int i=0;i<(pppindex.getPARMTP()).size();i++) {
		if(((String)(pppindex.getPARMTP()).elementAt(i)).equalsIgnoreCase("ORRS")) {
			logicalUnitNum = ((Integer)pppindex.getLUFILE().elementAt(i)).intValue();
			numberOFParamRecs = ((Integer)pppindex.getNUMPRM().elementAt(i)).intValue();
			break;
		}
	}
	
	// Now get the records. If only one get only that record and use the 
	// FIRST value from the pppindex file.
	if(numberOFParamRecs <= 0) {
		return orrsList;
	}
	else if(numberOFParamRecs == 1) {
		// Get the logical unit for the parameter type.
		for(int i=0;i<(pppindex.getPARMTP()).size();i++) {
			if(((String)(pppindex.getPARMTP()).elementAt(i)).equalsIgnoreCase("NTWK")) {
				orrsObj = new NWSRFS_ORRS((String)(pppindex.getID()).elementAt(i));
				orrsObj.setLogicalUnitNum(logicalUnitNum);
				orrsObj.setRecordNum(((Integer)(pppindex.getFIRST()).elementAt(i)).intValue());
				orrsList.addElement((NWSRFS_ORRS)orrsObj);
				break;
			}
		}
	}
	else {
		// Loop through the ID.size number of records
		for(int i=0;i<(pppindex.getID()).size();i++) {
			// If the type is ORRS add to the Vector
			if(((String)(pppindex.getITYPE()).elementAt(i)).equalsIgnoreCase("ORRS")) {
				orrsObj = new NWSRFS_ORRS((String)(pppindex.getID()).elementAt(i));
					orrsObj.setLogicalUnitNum(logicalUnitNum);
					orrsObj.setRecordNum(((Integer)(pppindex.getIREC()).elementAt(i)).intValue());
					orrsList.addElement((NWSRFS_ORRS)orrsObj);
			}
		}
	}
	
	// Return Vector of NWSRFS_ORRS objects
	return orrsList;
}

/**
Reads into a NWSRFS_PDBDLY object a time series from the NWSRFS 
preprocessor database for daily data types.
@param tsID this is a String object that holds the TimeSeries Identifier
for the TimeSeries object. 
@param tsDT this is the String value of the TimeSeries data type. It is
necessary that the data type be supplied to get a unique set of Time Series
from the data files.
@param tsDTInterval this is the int value of the TimeSeries data time interval. 
It is necessary that the data time interval be supplied to get a unique set of 
Time Series from the data files.
@return an NWSRFS_PDBDLY object which holds all of the information and data
from the PDBDLYn binary files.
@throws Exception if there is an error reading from the database.
*/
/* FIXME SAM 2008-04-07 Uncomment if functionality is enabled.
private NWSRFS_PDBDLY readPDBDLY(String tsID, String tsDT, int tsDTInterval,
boolean readData) throws Exception
{
	String routine = "NWSRFS_DMI.readPDBDLY";

	// If we have not previously retreived this time series create a 
	// new NWSRFS_PDBDLY object
	//NWSRFS_PDBDLY pdbFile = new NWSRFS_PDBDLY(tsID);

	// Throw an error since we have not implement this yet!	
	Message.printWarning(20,routine,"The Time Series Data Type is a daily "
		+ "data type. Currently only RRS data types are pulled "+
		"from the preprocessor database.");
	
	return null;
}
*/

/** 
This method is used to read in the values from the PDBINDEX NWSRFS 
preprocessor parameteric database file into the data members of the 
NWSRFS_PDBINDEX class. The data members of this class will be used to pull 
data from the PDBTSn binary database file.
@return NWSRFS_PDBINDEX the NWSRFS_PDBINDEX object which stores the data 
from the PDBINDEX binary database file. This is an index class and will 
be automatically instantiated when the NWSRFS_DMI is instantiated. This 
is so that the index to the PDBTSn binary file will be in memory.
*/
private NWSRFS_PDBINDEX readPDBINDEX() 
throws Exception
{
	int i = 0, j = 0, k = 0, INFREC, LFILE, NUMTYP, NUMDDF, NADDTP, SNWRDS;
	char[] pppChar;
	String parseChar = null;
	EndianDataInputStream EDIS = null;
	NWSRFS_PDBINDEX pdbindex = null;
	Vector tempADDDTPVect = null;
	Vector tempADTPTRVect = null;

	try {
		// Create the RandoAccessFile
		// Check if the the database binary file is open as a
		// Random Access object
		if (!checkRandomAccessFileOpen(__PDBINDEX, true)) {
			throw new Exception("Can not open the "
				+ __dbFileNames[__PDBINDEX] + 
				" binary database file");
		}

		// First create an instance of NWSRFS_PDBINDEX
		pdbindex = new NWSRFS_PDBINDEX();

		// Get the first record which holds the parameter control records.
		EDIS = read(__NWSRFS_DBFiles[__PDBINDEX], 0,
			__byteLength[__PDBINDEX]);

		// Now get record one information. The four public int members
		// will make up the 16 bytes - 4 bytes each. 
		pdbindex.setNWRDS(EDIS.readEndianInt());
		pdbindex.setLRECL1(EDIS.readEndianInt());
		pdbindex.setLRECL2(EDIS.readEndianInt());
		pdbindex.setLRECL3(EDIS.readEndianInt());
		pdbindex.setMAXTYP(EDIS.readEndianInt());
		NUMTYP = EDIS.readEndianInt();
		pdbindex.setNUMTYP(NUMTYP);
		pdbindex.setTYPREC(EDIS.readEndianInt());
		pdbindex.setNHASHR(EDIS.readEndianInt());
		pdbindex.setH8CREC(EDIS.readEndianInt());
		pdbindex.setHINTRC(EDIS.readEndianInt());
		INFREC = EDIS.readEndianInt();
		pdbindex.setINFREC(INFREC);
		pdbindex.setMFILE(EDIS.readEndianInt());
		LFILE = EDIS.readEndianInt();
		pdbindex.setLFILE(LFILE);
		pdbindex.setLURRS(EDIS.readEndianInt());
		pdbindex.setMAXDDF(EDIS.readEndianInt());
		NUMDDF = EDIS.readEndianInt();
		pdbindex.setNUMDDF(NUMDDF);

		// Close the EndianDataInputStream
		EDIS.close();

		// Now define and loop through data file information records
		for (i = 0; i < NUMDDF; i++) {
			// Each loop will need to read 16 bytes to get the
			// position correct even though only 8 bytes are
			// actually pulled from the stream.
			EDIS = read(__NWSRFS_DBFiles[__PDBINDEX],0,16);
			pdbindex.addMDDFRC(EDIS.readEndianInt());
		
			pdbindex.addLDDFRC(EDIS.readEndianInt());
		
			// Close the EndianDataInputStream
			EDIS.close();
		}
	
		// Read _byteLength bytes to position the stream to read in 
		// Daily Data Type Directory records
		EDIS = read(__NWSRFS_DBFiles[__PDBINDEX],0,
			__byteLength[__PDBINDEX]);
		EDIS.close();

		for (i = 0; i < NUMTYP; i++) {
			// For each Daily Data Type we need to read in 48 bytes
			// of data into the stream so that the position 
			// stays constant at the top of the DDT record.
			EDIS = read(__NWSRFS_DBFiles[__PDBINDEX],0,48);

			pdbindex.addNWRDSDDT((int)EDIS.readEndianShort());
		
			// Define the pppChar character array to hold segment info
			pppChar = new char[4];

			// Now parse the characters in stream
			for (j = 0; j < 4; j++) {
				pppChar[j] = EDIS.readEndianChar1();
			}
		
			parseChar = new String(pppChar);
			parseChar = parseChar.trim();
			pdbindex.addDTYPE(parseChar);

			pdbindex.addLUFILE((int)EDIS.readEndianShort());
			pdbindex.addNPNTRS((int)EDIS.readEndianShort());
			pdbindex.addNDATA((int)EDIS.readEndianShort());
			pdbindex.addMAXDAY((int)EDIS.readEndianShort());
			pdbindex.addEDATE(EDIS.readEndianInt());
			pdbindex.addECRECN((int)EDIS.readEndianShort());
			pdbindex.addLDATEDDT(EDIS.readEndianInt());
			pdbindex.addLDRECN((int)EDIS.readEndianShort());
			pdbindex.addPNTR((int)EDIS.readEndianShort());
			pdbindex.addDATAR1((int)EDIS.readEndianShort());
			pdbindex.addMAXSTA((int)EDIS.readEndianShort());
			pdbindex.addNUMSTA((int)EDIS.readEndianShort());
			pdbindex.addLSTPTR((int)EDIS.readEndianShort());
			pdbindex.addLSTDTA((int)EDIS.readEndianShort());
			pdbindex.addNSTATS((int)EDIS.readEndianShort());
			pdbindex.addNREC1D((int)EDIS.readEndianShort());
			
			// Close the EndianDataInputStream
			EDIS.close();
		}

// PLEASE NOTE WE DO NOT CARE about the station index hashes. So we skip over
// them!
		// Now get the Station Hash Indexes
//		EDIS = read(__NWSRFS_DBFiles[__PDBINDEX],0,2*NHASHR);
//		for (i = 0; i < NHASHR; i++) {
//			pdbindex.IPDHSC.addElement(
//				new Integer(EDIS.readEndianShort()));
//		}

		// Close the EndianDataInputStream
//		EDIS.close();

//		EDIS = read(__NWSRFS_DBFiles[__PDBINDEX],0,2*NHASHR);
//		for (i = 0; i < NHASHR; i++) {
//			pdbindex.IPDHSI.addElement(
//				new Integer(EDIS.readEndianShort()));
//		}

		// Close the EndianDataInputStream
//		EDIS.close();

// Test read the first 512 bytes at record number to see what is really
// happening.
//__NWSRFS_DBFiles[__PDBINDEX].seek(0);
//__NWSRFS_DBFiles[__PDBINDEX].seek((INFREC)*64);
//EDIS = read(__NWSRFS_DBFiles[__PDBINDEX], 0, 512);
//char[] charValue = new char[512];
//for (i = 0; i < 512; i++) {
//	byte byteValue = EDIS.readByte();
//	charValue[i] = (char)byteValue;
//	Message.printStatus(10,routine,"byteValue["+i+"] = "+byteValue);
//	Message.printStatus(10,routine,"charValue["+i+"] = "+charValue[i]);
//}
	// Now seek to the first Station Information Record in index where 64 is
	// the number of bytes in a record.
		__NWSRFS_DBFiles[__PDBINDEX].seek(0);
		__NWSRFS_DBFiles[__PDBINDEX].seek((INFREC)*64);
		for(j=0;j<LFILE-INFREC;j++)
		{
			// Get the record which holds the number of words for
			// the Station Information
			EDIS = read(__NWSRFS_DBFiles[__PDBINDEX],0,2);
	
			// Now get the number of words in the record information.
			SNWRDS = (int)EDIS.readEndianShort();
			pdbindex.addNWRDSSTI(SNWRDS);

			// Close the Stream
			EDIS.close();
		
			// Now determine the number of bytes to read for this 
			// record! Remember that the fixed length record for the 
			// PDBINDEX is 64 bytes. If SNWRDS*2 > 256 or < 0 then 
			// probably something is wrong so we read 64 bytes and 
			// continue.
			// Probably what has happened is that SNWRDS from the
			// previous loop pass was wrong and not enough bytes
			// were read thus when it reads a new record instead
			// of being at the top of a new record, it is reading the
			// end of the previous record so SNWRDS is all messed up!
			int recFactor = 1;
			if(SNWRDS*2> 64 && SNWRDS*2 < 256) {
				recFactor = (int)(Math.floor(SNWRDS*2/64)+1);
			}
			else if(SNWRDS*2 >= 256 || SNWRDS*2 < 0) {
				EDIS = read(__NWSRFS_DBFiles[__PDBINDEX],0,
					64*recFactor-2);
				continue;
			}
			else if(SNWRDS == 0) {
				continue;
			}

			// Get the data stream which will be 64*recFactor-2 
			// bytes. The numbers are 64 bytes is fixed record 
			// length, recFactor is the number of records to read 
			// for this data line (defaults to 1), We subtract 
			// 2 bytes for the SNWRDS value we read above!
			EDIS = read(__NWSRFS_DBFiles[__PDBINDEX],0,
				64*recFactor-2);
		
			// Define the pppChar character array to hold 
			// segment info
			pppChar = new char[8];

			// Now parse the characters in stream
			for (k = 0; k < 8; k++) {
				pppChar[k] = EDIS.readEndianChar1();
			}

			parseChar = new String(pppChar);
			parseChar = parseChar.trim();
			
			// If station ID is "DELETED" do not add but continue
			if(parseChar.equalsIgnoreCase("DELETED") || 
			parseChar.equalsIgnoreCase("DELETE")) {
				continue;
			}
			pdbindex.addSTAID(parseChar);

//Message.printStatus(10,routine,"      SNWRDS = "+SNWRDS+"\n      recFactor = "+
//recFactor+"\n      StationID = "+parseChar);
//Message.printStatus(10,routine,"================================================");
			pdbindex.addNUMID((int)EDIS.readEndianShort());
			pdbindex.addPRMPTR((int)EDIS.readEndianShort());
			pdbindex.addPCPPTR((int)EDIS.readEndianShort());
			pdbindex.addTMPPTR((int)EDIS.readEndianShort());
			NADDTP = (int)EDIS.readEndianShort();
			pdbindex.addNADDTP(NADDTP);

			// Now define and loop through station data type records
			tempADDDTPVect = new Vector();
			tempADTPTRVect = new Vector();
			
			for (i = 0; i < NADDTP; i++) {
				// Define the pppChar character array to hold 
				// segment info
				pppChar = new char[4];

				// Now parse the characters in stream
				for (k = 0; k < 4; k++) {
					pppChar[k] = EDIS.readEndianChar1();
				}

				parseChar = new String(pppChar);
				parseChar = parseChar.trim();
				tempADDDTPVect.addElement(parseChar);

				tempADTPTRVect.addElement(new Integer((int)EDIS.
					readEndianShort()));
			}
			
			// Add the temp Vectors to the pdbindex object
			pdbindex.addADDDTP(tempADDDTPVect);
			pdbindex.addADTPTR(tempADTPTRVect);

			// Have to do a check here! If the 2*SNWRDS is < 64 
			// and NADDTP is > 1 then we will have a problem reading
			// in from the stream! If this case happens (it should 
			// not but does anyway) just close the stream then read 
			// in 64 bytes then continue. This situation is caused 
			// by the fact that if NADDTP > 1 then the record will 
			// exceed 64 bytes (the files fixed record length)
			// and the 2*SNWRDS should always by > 64 but it 
			// sometimes is < 64!! Because of this we are not 
			// reading more than 1 fixed record length of 64 but 
			// the actual record IS > 64... It is clear as mud but 
			// necessary to read one addition record of 64 bytes 
			// because of this.
			if(2*SNWRDS <= 64 && NADDTP > 1) {
				EDIS.close();
				EDIS = read(__NWSRFS_DBFiles[__PDBINDEX],0,64);
				EDIS.close();
				continue;
			}

			// Read in station PCPN statistical data if available.
			pdbindex.addBDATE(EDIS.readEndianInt());
			pdbindex.addRDATE(EDIS.readEndianInt());
			pdbindex.addNDAYS((int)EDIS.readEndianShort());
			pdbindex.addNTOTAL((int)EDIS.readEndianShort());
			pdbindex.addNZERO((int)EDIS.readEndianShort());
			pdbindex.addACDCP(EDIS.readEndianFloat());
			pdbindex.addRPTLG((int)EDIS.readEndianShort());
			pdbindex.addLDATE(EDIS.readEndianInt());
			pdbindex.addRPT2LG((int)EDIS.readEndianShort());
			pdbindex.addL2DATE(EDIS.readEndianInt());
			pdbindex.addSMNOZO((int)EDIS.readEndianShort());
			pdbindex.addACPSQ(EDIS.readEndianFloat());
			pdbindex.addNWRDSO((int)EDIS.readEndianShort());
		
			// Close the stream before we loop through again.
			EDIS.close();
		}
	} catch (Exception e) {
		e.printStackTrace();
	}	
	return pdbindex;
}

/**
Reads into a NWSRFS_PDBRRS object a time series from the NWSRFS 
preprocessor database for RRS types.
@param tsID this is a String object that holds the TimeSeries Identifier
for the TimeSeries object. 
@param tsDT this is the String value of the TimeSeries data type. It is
necessary that the data type be supplied to get a unique set of Time Series
from the data files.
@return an NWSRFS_PDBRRS object which holds all of the information and data
from the PDBRRS file.
@throws Exception if there is an error reading from the database.
*/
private NWSRFS_PDBRRS readPDBRRS(String tsID, String tsDT, int tsDTInterval, 
boolean readData) throws Exception
{
	char[] charValue = null;
	EndianDataInputStream EDIS;
	int i=0, j=0, recNum=-1, pdbAddDT=0, numObs=0;
	int checkInterval = 0;
	int checkObsTime = 0;
	float checkDataValue = 0;
	String parseChar = null;
	NWSRFS_PDBRRS pdbFile;
	NWSRFS_PDBINDEX pdbIndex = null;
	//NWSRFS_Station station = null;
	
	// If we have not previously retreived this time series create a 
	// new NWSRFS_PDBRRS object
	pdbFile = new NWSRFS_PDBRRS(tsID);

	// Check if the the database binary file is open as a
	// Random Access object
	if (!checkRandomAccessFileOpen(__PDBRRS, true)) {
		throw new Exception("Can not open the " 
			+ __dbFileNames[__PDBRRS] + " binary database file");
	}

	// Now read the Time Series parameter file to get the parameters
	// for the Time series in the PRDTSn binary file.

	// Read the first record to get the global values
	EDIS = read(__NWSRFS_DBFiles[__PDBRRS], 0,__byteLength[__PDBRRS]);

	// Field 1 - Maximum primary record
	pdbFile.setMAXREC(EDIS.readEndianInt());
	
	// Field 2 - Next available primary record
	pdbFile.setNEXTRC(EDIS.readEndianInt());
	
	// Field 3 - Rec number of first free pool record
	pdbFile.setFREE1(EDIS.readEndianInt());
	
	// Field 4 - Rec number of next free pool record
	pdbFile.setFREEN(EDIS.readEndianInt());
	
	// Field 5 - Number of words in a free pool record
	pdbFile.setFREEL(EDIS.readEndianInt());
	
	// Field 6 - Ordinal number of daily data file in which free
	// records are stored.
	pdbFile.setLUFREE(EDIS.readEndianInt());
	
	// Field 7 - Maximum free pool records
	pdbFile.setMAXFRE(EDIS.readEndianInt());
	
	// Field 8 - Length of longest observation period
	pdbFile.setMAXPD(EDIS.readEndianInt());
	
	// Field 9 - Number of daily and RRS stations defined
	pdbFile.setNUMSET(EDIS.readEndianInt());
	
	// Field 10 - In use indicator
	pdbFile.setINUSE(EDIS.readEndianInt());
	
	// Field 11 - User name
	charValue = new char[8];
	for (i = 0; i < 8; i++) {
		charValue[i] = EDIS.readEndianChar1();
	}
			
	parseChar = new String(charValue).trim();

	if (parseChar.length() != 0) {
		pdbFile.setUSER(parseChar);
	}

	// Close the Stream since the first record is read
	EDIS.close();
	
	// Now pull the station data. We loop through PDBINDEX Station
	// Vector till we find the correct station.
	pdbIndex = getPDBIndex();
	//station = readStation(tsID,true);
	
	for(i = 0; i < (pdbIndex.getSTAID()).size(); i++) {
		pdbAddDT = pdbIndex.getNADDTP(i);
//Message.printStatus(10,routine,"  Station ID = "+tsID+"\n  getSTAID("+i+") = "+
//pdbIndex.getSTAID(i)+"\n   Station Num = "+station.getStationNum()+
//"\n   getNUMID("+i+") = "+pdbIndex.getNUMID(i));
		if(tsID.equalsIgnoreCase(pdbIndex.getSTAID(i))) {
			// If the number of additional data types is <= 0
			// then no more data types to check so continue!
			// Now addDTIndex is the true Vector index holding
			// the additional data types!
			if(pdbAddDT > 0) {
				for(j = 0; j < pdbAddDT; j++) {
//Message.printStatus(10,routine,"    DataType = "+tsDT+
//"\n  ADDDTP("+i+","+j+") = "+pdbIndex.getADDDTP(i,j));
					if((pdbIndex.getADDDTP(i,j)).
						equalsIgnoreCase(tsDT)) {
						recNum = pdbIndex.
							getADTPTR(i,j);
						break; // Inner loop
					}
				}
			}
			break; // Outer loop
		}
	}

	// Set pdbIndex to null to free up memory
	pdbIndex = null;
	
	// We now check to see if we have a valid record number if not
	// we throw an exception if we intended to read data or return a null
	// if we did not intend to read data!
	if(readData && recNum == -1) {
		throw new Exception("The TSID = " + tsID + "." + tsDT
			+ " has not been found in the preprocessor "
			+ "database.");
	}
	else if(recNum == -1) {
		return null;
	}
	
	// Now get the PDBRRS record for station and data type.
	// Read the record at recNum 112 bytes to get the needed values.
	__NWSRFS_DBFiles[__PDBRRS].seek(0);
	EDIS = read(__NWSRFS_DBFiles[__PDBRRS], recNum-1,__byteLength[__PDBRRS],
		112);
	
	// Field 12 - Number of words in RRS primary record
	pdbFile.setNWRDS(EDIS.readEndianInt());
	
	// Field 13 - Station Identifier
	charValue = new char[8];
	for (i = 0; i < 8; i++) {
		charValue[i] = EDIS.readEndianChar1();
	}
			
	parseChar = new String(charValue).trim();

	if (parseChar.length() != 0) {
		pdbFile.setSTAID(parseChar);
	}

	// Field 14 - Station Number
	pdbFile.setNUMID(EDIS.readEndianInt());
	
	// Field 15 - Data Type
	charValue = new char[4];
	for (i = 0; i < 4; i++) {
		charValue[i] = EDIS.readEndianChar1();
	}
			
	parseChar = new String(charValue).trim();

	if (parseChar.length() != 0) {
		pdbFile.setDTYPE(parseChar);
	}

	// Field 16 - Minimum Number of days of obs to hold
	pdbFile.setMINDAY(EDIS.readEndianInt());
	
	// Field 17 - Maximum Number of obs that can be held
	pdbFile.setMAXOBS(EDIS.readEndianInt());
	
	// Field 18 - Number of obs in primary space
	pdbFile.setNUMOBS(EDIS.readEndianInt());
	
	// Field 19 - Word position of earliest value
	pdbFile.setEVAL(EDIS.readEndianInt());
	
	// Field 20 - Unused
	pdbFile.setREVAL(EDIS.readEndianInt());
	
	// Field 21 - Word position of latest value
	pdbFile.setLVAL(EDIS.readEndianInt());
	
	// Field 22 - Unused
	pdbFile.setRLVAL(EDIS.readEndianInt());
	
	// Field 23 - Record Number of first free pool - record zero if none
	pdbFile.setIFREC1(EDIS.readEndianInt());
	
	// Field 24 - Number of values per obs
	pdbFile.setNVALS(EDIS.readEndianInt());
	
	// Field 25 - Time of first data free pool record
	pdbFile.setFTIME(EDIS.readEndianInt());
	
	// Field 26 - Julian hour of last obs data
	pdbFile.setLSTHR(EDIS.readEndianInt());
	
	// Field 28 - Number of words of stats
	pdbFile.setNSTAT(EDIS.readEndianInt());
	
	// Field 29 - Julian hour stats begin
	pdbFile.setBDATE(EDIS.readEndianInt());
	
	// Field 30 - Julian date of most recent report
	pdbFile.setRDATE(EDIS.readEndianInt());
	
	// Field 31 - Total number of reports
	pdbFile.setNTOTAL(EDIS.readEndianInt());
	
	// Field 32 - Largest value reported
	pdbFile.setRPTLG(EDIS.readEndianFloat());
	
	// Field 33 - Julian date of largest value
	pdbFile.setLDATE(EDIS.readEndianInt());
	
	// Field 34 - Second largest value reported
	pdbFile.setRPT2LG(EDIS.readEndianFloat());
	
	// Field 35 - Julian date of second largest value
	pdbFile.setL2DATE(EDIS.readEndianInt());
	
	// Field 36 - Smallest value reported
	pdbFile.setRPTSM(EDIS.readEndianFloat());
	
	// Field 37 - Julian date of smallest value
	pdbFile.setSDATE(EDIS.readEndianInt());
	
	// Field 38 - Second smallest value reported
	pdbFile.setRPT2SM(EDIS.readEndianFloat());
	
	// Field 39 - Julian date of second smallest value
	pdbFile.setS2DATE(EDIS.readEndianInt());
	
	// Close stream
	EDIS.close();
	
	// Now get observations in primary space!!
	if(pdbFile.getNVALS() > 2) {
		// We have a mean data so have to pull 3 values per observation
		// These are obs time, obs value, data time interval
		numObs = pdbFile.getNUMOBS()*3+1;
		
		// Open stream
		EDIS = read(__NWSRFS_DBFiles[__PDBRRS], 0,numObs*4);
		
		// Somehow an extra unknown byte needs to be read!!
		EDIS.readEndianInt();

		
		// Read loop
		for(i = 0;i < pdbFile.getNUMOBS();i++) {
			// Now check interval an make sure we get only the TS for 
			// requested interval. If the requested interval is 0 get
			// all time series which is probably not what we want!
			if(tsDTInterval > 0) {
				checkObsTime = EDIS.readEndianInt();
				checkDataValue = EDIS.readEndianFloat();
				checkInterval = EDIS.readEndianInt();
				if(tsDTInterval == checkInterval) {
					// Add to the Vectors
					pdbFile.addOBSTIME(checkObsTime);
					pdbFile.addDATAVAL(checkDataValue);
					pdbFile.addDATATIMEINT(checkInterval);

					// We check to see if we want
					// to really read data. If so
					// we continue else we just
					// return since we know we
					// have a record!
					if(!readData) {
						return pdbFile;
					}
				}
			}
			else {
				pdbFile.addOBSTIME(EDIS.readEndianInt());
				pdbFile.addDATAVAL(EDIS.readEndianFloat());
				pdbFile.addDATATIMEINT(EDIS.readEndianInt());
			}
		}
		
		// Close Stream
		EDIS.close();
	}
	else {
		// We have an instantaneous data so have to pull 2 values per 
		// observation. These are obs time, obs value.
		numObs = pdbFile.getNUMOBS()*2+1;
		
		// Open stream
		EDIS = read(__NWSRFS_DBFiles[__PDBRRS], 0,numObs*4);
		
		// Somehow an extra unknown byte needs to be read!!
		EDIS.readEndianInt();

		// Read loop
		for(i = 0;i < pdbFile.getNUMOBS();i++) {
			// Now check interval an make sure we get only the TS for 
			// requested interval. If the requested interval is 0 get
			// the time series regardless of the interval!
			if(tsDTInterval == 0) {
				pdbFile.addOBSTIME(EDIS.readEndianInt());			
				pdbFile.addDATAVAL(EDIS.readEndianFloat());
			}
			else if(i == 0) {
				checkObsTime = EDIS.readEndianInt();
				checkDataValue = EDIS.readEndianFloat();
			}
			else if(i == 1) {
				checkInterval = (EDIS.readEndianInt() - 
					checkObsTime)/100;
				if(tsDTInterval == checkInterval) {
					// A dd to Vectors
					pdbFile.addOBSTIME(checkObsTime);
					pdbFile.addDATAVAL(checkDataValue);
					pdbFile.addOBSTIME(checkObsTime+
						checkInterval*100);
					pdbFile.addDATAVAL(EDIS.readEndianFloat());

					// We check to see if we want
					// to really read data. If so
					// we continue else we just
					// return since we know we
					// have a record!
					if(!readData) {
						return pdbFile;
					}
				}
				else {
					return pdbFile;
				}
			}
			else {
				pdbFile.addOBSTIME(EDIS.readEndianInt());			
				pdbFile.addDATAVAL(EDIS.readEndianFloat());
			}
		}
		
		// Close Stream
		EDIS.close();
	}

	// Check to see if we have any free pool records. If not we are
	// done and just need to return! Otherwise get free pool observations.
	if(pdbFile.getIFREC1() == 0) {
		return pdbFile;
	}
	
	// Now get observations in free pool!!
	// Go to the record. Only get the first 8 bytes so we can find out
	// how many observations we need to get!
	recNum = pdbFile.getIFREC1();
	__NWSRFS_DBFiles[__PDBRRS].seek(0);
	EDIS = read(__NWSRFS_DBFiles[__PDBRRS], recNum-1,__byteLength[__PDBRRS],
		8);
	
	// Field ... Pointer to next available free pool record
	pdbFile.setNXTREC(EDIS.readEndianInt());
	
	// Field ...+1 Number of observation in this free pool record!!
	pdbFile.setNVALSFP(EDIS.readEndianInt());
	
	// Close Stream
	EDIS.close();
	
	// Get the observation
	if(pdbFile.getNVALS() > 2) {
		// We have a mean data so have to pull 3 values per observation
		// These are obs time, obs value, data time interval
		numObs = pdbFile.getNVALSFP()*3;
		
		// Open stream
		EDIS = read(__NWSRFS_DBFiles[__PDBRRS], 0,numObs*4);
		
		// Read loop
		for(i = 0;i < pdbFile.getNVALSFP();i++) {
			// Now check interval an make sure we get only the TS for 
			// requested interval. If the requested interval is 0 get
			// all time series which is probably not what we want!
			if(tsDTInterval > 0) {
				checkObsTime = EDIS.readEndianInt();
				checkDataValue = EDIS.readEndianFloat();
				checkInterval = EDIS.readEndianInt();
				if(tsDTInterval == checkInterval) {
					pdbFile.addOBSTIME(checkObsTime);
					pdbFile.addDATAVAL(checkDataValue);
					pdbFile.addDATATIMEINT(checkInterval);
				}
			}
			else {
				pdbFile.addOBSTIME(EDIS.readEndianInt());
				pdbFile.addDATAVAL(EDIS.readEndianFloat());
				pdbFile.addDATATIMEINT(EDIS.readEndianInt());
			}
		}
		
		// Close Stream
		EDIS.close();
	}
	else {
		// We have an instantaneous data so have to pull 2 values per 
		// observation. These are obs time, obs value.
		numObs = pdbFile.getNVALSFP()*2;
		
		// Open stream
		EDIS = read(__NWSRFS_DBFiles[__PDBRRS], 0,numObs*4);
		
		// Read loop
		for(i = 0;i < pdbFile.getNVALSFP();i++) {
			// Now check interval an make sure we get only the TS for 
			// requested interval. If the requested interval is 0 get
			// the time series regardless of the interval!
			if(tsDTInterval == 0) {
				pdbFile.addOBSTIME(EDIS.readEndianInt());			
				pdbFile.addDATAVAL(EDIS.readEndianFloat());
			}
			else if(i == 0) {
				checkObsTime = EDIS.readEndianInt();
				checkDataValue = EDIS.readEndianFloat();
			}
			else if(i == 1) {
				checkInterval = (EDIS.readEndianInt() - 
					checkObsTime)/100;
				if(tsDTInterval == checkInterval) {
					pdbFile.addOBSTIME(checkObsTime);
					pdbFile.addDATAVAL(checkDataValue);
					pdbFile.addOBSTIME(checkObsTime+
						checkInterval*100);
					pdbFile.addDATAVAL(EDIS.readEndianFloat());
				}
				else {
					return pdbFile;
				}
			}
			else {
				pdbFile.addOBSTIME(EDIS.readEndianInt());			
				pdbFile.addDATAVAL(EDIS.readEndianFloat());
			}
		}
		
		// Close Stream
		EDIS.close();
	}

	// Return the NWSRFS_PDBRRS object!
	return pdbFile;
}

/** 
This method is used to read in the values from the PPPINDEX NWSRFS 
preprocessor parameteric database file into the data members of the 
NWSRFS_PPPINDEX class. The data members of this class will be used to pull 
data from the PPPPARMn binary database file.
@return NWSRFS_PPPINDEX the NWSRFS_PPPINDEX object which stores the data 
from the PPPINDEX binary database file. This is an index class and will 
be automatically instantiated when the NWSRFS_DMI is instantiated. This 
is so that the index to the PPPPARMn binary file will be in memory.
@throws Exception if there were errors reading from the database.
*/
private NWSRFS_PPPINDEX readPPPINDEX() 
throws Exception
{
	NWSRFS_PPPINDEX ptrFile = new NWSRFS_PPPINDEX();
	long randomAccessFileLength = 0;
	// Define the pppChar character array to hold segment info
	char[] pppChar = new char[8];

	// Check if the the database binary file is open as a Random Access object
	if (!checkRandomAccessFileOpen(__PPPINDEX, true)) {
		throw new Exception("Can not open the "
			+ __dbFileNames[__PPPINDEX] + " binary database file");
	}

	// Get the first record which holds the parameter control records.
	EndianDataInputStream EDIS = read(__NWSRFS_DBFiles[__PPPINDEX], 0,
		__byteLength[__PPPINDEX]);

	// Now get record one information. The four public int members
	// will make up the 16 bytes - 4 bytes each. 
	ptrFile.setMAXREC(checkInt(EDIS.readEndianInt(), 0, 100000, -1));
	ptrFile.setMAXTYP(checkInt(EDIS.readEndianInt(), 0, 100000, -1));
	ptrFile.setNUMTYP(checkInt(EDIS.readEndianInt(), 0, 100000, -1));
	ptrFile.setNUMFIL(checkInt(EDIS.readEndianInt(), 0, 100000, -1));	
	
	EDIS.close();

	// Get the second record which holds the parameter control records.
	EDIS = read(__NWSRFS_DBFiles[__PPPINDEX], 0,
		__byteLength[__PPPINDEX]);

	// Now get record two information. The two public int members
	// will make up the 16 bytes - one 4 bytes each, one 8 bytes and 
	// one unsed. 
	ptrFile.setFSTIDX(checkInt(EDIS.readEndianInt(), 0, 100000, -1));


	int j = 0;
	// Now parse the characters in stream
	for (j = 0; j < 8; j++) {
		pppChar[j] = EDIS.readEndianChar1();
	}
		
	String parseChar = new String(pppChar).trim();

	// Add User Id
	ptrFile.setUSERID(parseChar);
	
	EDIS.close();

	int numtyp = ptrFile.getNUMTYP();
	for (int i = 0; i < numtyp; i++) {
		// Get the first record set which holds PARMTP, LUFILE, 
		// FIRST, and LAST
		EDIS = read(__NWSRFS_DBFiles[__PPPINDEX], 0,
			__byteLength[__PPPINDEX]);

		// Define the pppChar character array to hold segment info
		pppChar = new char[4];

		// Now parse the characters in stream
		for (j = 0; j < 4; j++) {
			pppChar[j] = EDIS.readEndianChar1();
		}
		parseChar = new String(pppChar).trim();

		// Add PARMTP
		if (parseChar.length() == 0) {
			continue;
		}
		else {
			ptrFile.addPARMTP(parseChar);
		}
//Message.printStatus(10,routine,"Parameter Types defined: "+parseChar);

		ptrFile.addLUFILE(checkInt(EDIS.readEndianInt(), 
			0, 100000, -1));

		ptrFile.addFIRST(checkInt(EDIS.readEndianInt(), 0, 100000, -1));

		ptrFile.addLAST(checkInt(EDIS.readEndianInt(), 0, 100000, -1));

		EDIS.close();

		// Get the second record set which holds NUMPRM and ISNGL
		EDIS = read(__NWSRFS_DBFiles[__PPPINDEX], 0,
			__byteLength[__PPPINDEX]);

		ptrFile.addNUMPRM(checkInt(EDIS.readEndianInt(), 
			0, 100000, -1));

		ptrFile.addISNGL(checkInt(EDIS.readEndianInt(), 0, 100000, -1));

		EDIS.close();
	}
	
	// Now loop through the remaining PPPINDEX file records to read 
	// in all the record values.
	randomAccessFileLength = __NWSRFS_DBFiles[__PPPINDEX].length();
	while(__NWSRFS_DBFiles[__PPPINDEX].getFilePointer()+16 <= randomAccessFileLength) {
		// keep reading until an exception is caught
		try {
			// Get the record which holds the members of 
			// the rating curves index.
			EDIS = read(__NWSRFS_DBFiles[__PPPINDEX], 0,
				__byteLength[__PPPINDEX]);

			// Define the pppChar character array 
			// to hold parameter id
			pppChar = new char[8];

			// Now parse the characters in stream
			for (j = 0; j < 8; j++) {
				pppChar[j] = EDIS.readEndianChar1();
			}
			parseChar = new String(pppChar).trim();

			// Add parameter Id
			if (parseChar.length() == 0) {
				continue;
			}
			else {
				ptrFile.addID(parseChar);
			}
//Message.printStatus(10,routine,"Parameter ID: "+parseChar);

			// Define the pppChar character array to 
			// hold parameter type
			pppChar = new char[4];

			// Now parse the characters in stream
			for (j = 0; j < 4; j++) {
				pppChar[j] = EDIS.readEndianChar1();
			}
			parseChar = new String(pppChar).trim();

			// Add parameter type
			if (parseChar.length() == 0) {
				continue;
			}
			else {
				ptrFile.addITYPE(parseChar);
			}
//Message.printStatus(10,routine,"Parameter Type: "+parseChar+"\n");

			// Add record number to parameter index definition
			ptrFile.addIREC(checkInt(EDIS.readEndianInt(),
				0, 100000, -1));

			EDIS.close();
		}
		catch (EOFException EOFe) {
			// Should never get here if things are working right.
			exceptionCount++;
			EDIS.close();
			break;
		}
		catch (NullPointerException NPe) {
			exceptionCount++;
			EDIS.close();
			break;
		}
	}

	return ptrFile;
}

/** 
This method is used to read in the values from the PRDINDEX NWSRFS 
processed database index file into the data members of the 
NWSRFS_PRDINDEX class. The data members of this class will be used to pull 
data from the PRDTSn binary database file.
@return NWSRFS_PRDINDEX the NWSRFS_PRDINDEX object which stores the data 
from the PRDINDEX binary database file. This is an index class and will 
be automatically instantiated when the NWSRFS_DMI is instantiated. This 
is so that the index to the PRDTSn binary file will be in memory.
@throws Exception if there were errors reading from the database.
*/
private NWSRFS_PRDINDEX readPRDINDEX() 
throws Exception {
	NWSRFS_PRDINDEX ptrFile = new NWSRFS_PRDINDEX();
	int j;
	EndianDataInputStream EDIS = null;
	char[] prdChar;
	String parseChar;
	long randomAccessFileLength = 0;
	
	// Check if the the database binary file is open as a
	// Random Access object
	if (!checkRandomAccessFileOpen(__PRDINDEX, true)) {
		throw new Exception("Can not open the "
			+ __dbFileNames[__PRDINDEX] + " binary database file");
	}

	randomAccessFileLength = __NWSRFS_DBFiles[__PRDINDEX].length();
	while(__NWSRFS_DBFiles[__PRDINDEX].getFilePointer()+16 <= randomAccessFileLength) {
		// Get the first record set which holds PARMTP, LUFILE, 
		// FIRST, and LAST
		EDIS = read(__NWSRFS_DBFiles[__PRDINDEX], 0,
			__byteLength[__PRDINDEX]);

		// Define the pppChar character array to hold segment info
		prdChar = new char[8];

		// Now parse the characters in stream
		for (j = 0; j < 8; j++) {
			prdChar[j] = EDIS.readEndianChar1();
		}
		parseChar = new String(prdChar).trim();

		// Add TSID
		if (parseChar.length() == 0) {
			continue;
		}
		else {
			ptrFile.addTSID(parseChar);
		}

		// Define the pppChar character array to hold segment info
		prdChar = new char[4];

		// Now parse the characters in stream
		for (j = 0; j < 4; j++) {
			prdChar[j] = EDIS.readEndianChar1();
		}
		parseChar = new String(prdChar).trim();

		// Add DataType
		if (parseChar.length() == 0) {
			continue;
		}
		else {
			ptrFile.addTSDT(parseChar);
		}

		// Add Record Number
		ptrFile.addIREC(checkInt(EDIS.readEndianInt(), 
			0, 1000000, -1));

		EDIS.close();
	}
	
	return ptrFile;
}

/**
This method reads Time Series directly from the PRDTS<i>n</i> binary database file
where <i>n</i> is determined from the PRDINDEX file and the UNIT NUMBER parameter 
in this file. The Time Series is placed into the input NWSRFS_TimeSeries object passed
into this method.
@param RA is the EndianRandomAccess file object openned to read the binary DB
PRDTS<i>n</i>
@param recordNum is an integer holding the starting record number to start reading Times Series data
@param tsFile is the NWSRFS_TimeSeries object to store the Time Series Data
@param readData is a boolean used to determine whether header information is read only or if
header and data are read into the tsFile object
@return boolean whether the read succeeded or not
@throws Exception if an error occurs
*/
public boolean readPRDTS(EndianRandomAccessFile RA, int recordNum,
NWSRFS_TimeSeries tsFile, boolean readData) throws Exception {

	String routine = "NWSRFS_DMI.readPRDTS";
	char[] charValue = null;
	EndianDataInputStream EDIS = null;
	int i=0, tsDTInt;
	String parseChar = null, tsident_string;
	DateTime dtTemp, dtTempStart, dtTempEnd;

	// Read the header record. Remember that recordNum is the record number
	// read from the PRDINDEX file. First must get the length of the header
	// prior to the full read.
// TODO (JTS - 2004-08-21)
// explain the magic numbers 64 and 6	
	EDIS = read(RA,recordNum-1, 64, 6);
	// Field 1 - [type field name here]
	tsFile.setLTSHDR((byte)EDIS.readByte());
	
	// Field 2 - [type field name here]
	tsDTInt = (byte)EDIS.readByte();
	if(tsDTInt != (int)tsFile.getTSDTInterval()) // Not right TS interval
	{
		EDIS.close();
		return false;
	}

	tsFile.setIDTINT((byte)tsDTInt);
	
	// Field 3 - [type field name here]
	tsFile.setNVLINT((byte)EDIS.readByte());

	// Skip byte 4
	EDIS.readByte();
	
	// Field 4 - [type field name here]
	tsFile.setNTSMAX((short)EDIS.readEndianShort());

	// Now get the number of bytes to read to get the rest of the header
	// TODO (JTS - 2004-08-18)
	// should probably explain these magic numbers
	EDIS.close();
	int bytesToRead = ((int)tsFile.getLTSHDR()
		+ (int)tsFile.getNXHDR()) * __WORDSIZE - 6;	
	EDIS = read(RA, 0, 64, bytesToRead);

	// Field 5 - [type field name here]
	tsFile.setNTSNUM((short)EDIS.readEndianShort());
	
	// Field 6 - [type field name here]
	tsFile.setIPTREG((short)EDIS.readEndianShort());
	
	// Field 7 - [type field name here]
	tsFile.setIPTFUT((short)EDIS.readEndianShort());

	// Skip the next 12 bytes in that it is the TSID and DataType values
	charValue = new char[12];
	for (i = 0;i < 12; i++) {
		charValue[i] = EDIS.readEndianChar1();
	}

	// Field 8 - [type field name here]
	charValue = new char[4];
	for (i = 0; i < 4; i++) {
		charValue[i] = EDIS.readEndianChar1();
	}
	parseChar = new String(charValue).trim();
	if (parseChar.length() != 0) {
		tsFile.setTSUNIT((String)parseChar);
	}

	// Field 9 - [type field name here]
	tsFile.setTSLAT((float)EDIS.readEndianFloat());

	// Field 10  - [type field name here]
	tsFile.setTSLONG((float)EDIS.readEndianFloat());

	// Field 11  - [type field name here]
	tsFile.setJULBEG((int)EDIS.readEndianInt());

	// Field 12  - [type field name here]
	tsFile.setITSFUT((int)EDIS.readEndianInt());

	// Skip next word
	EDIS.readEndianInt();

	// Field 13  - [type field name here]
	tsFile.setNRECNX((int)EDIS.readEndianInt());

	// Field 14 - [type field name here]
	charValue = new char[20];
	for (i = 0; i < 20; i++) {
		charValue[i] = EDIS.readEndianChar1();
	}
	parseChar = new String(charValue).trim();
	if (parseChar.length() != 0) {
		tsFile.setTSDESC((String)parseChar);
	}

	// Now that header is retrieved, it is now needed to get start and
	// end DateTime values for both observed and future data.

	int endFutJul = 0;
	int endObsJul = 0;
	int futDataNum = 0;
	int obsDataNum = 0;
	int startFutJul = 0;
	int startObsJul = tsFile.getJULBEG();

	if (tsFile.getIPTFUT() == 0) {
		// No future data so read observed data...
		Message.printStatus( 2, routine, "No future data are available.");
		endObsJul = startObsJul + (int)tsFile.getNTSNUM()
			* (int)tsFile.getIDTINT();

		// Get number of observed and future data points
		obsDataNum = (int)tsFile.getNTSNUM();
		futDataNum = 0;

		// Set values in the Observed TS object
		dtTempStart = NWSRFS_Util.getDateFromJulianHour1900(startObsJul);
		dtTempStart.setTimeZone("Z");
		tsFile.getObservedTS().setDate1(dtTempStart);
		tsFile.getObservedTS().setDate1Original(dtTempStart);
		
		dtTempEnd = NWSRFS_Util.getDateFromJulianHour1900(endObsJul);
		dtTempEnd.setTimeZone("Z");
		tsFile.getObservedTS().setDate2(dtTempEnd);
		tsFile.getObservedTS().setDate2Original(dtTempEnd);
		Message.printStatus (2,routine,"Observed start=" + dtTempStart);
		Message.printStatus (2,routine,"Observed end=" + dtTempEnd);
		

		// Set identifier string
		tsident_string = tsFile.getTSID()+".NWSRFS."+
			tsFile.getTSDataType()+".";
		
		// Set the TS object Identifier
		if(tsFile.getTSDTInterval() == 0) {
			tsident_string += "*~NWSRFS_FS5Files~" + getFS5FilesLocation();
			tsFile.getObservedTS().setIdentifier(tsident_string);
		}
		else {
			tsident_string += tsFile.getTSDTInterval()+
				"Hour~NWSRFS_FS5Files~" + getFS5FilesLocation();
			tsFile.getObservedTS().setIdentifier(tsident_string);
		}
		
		tsFile.getObservedTS().allocateDataSpace();
		tsFile.getObservedTS().setDataInterval(TimeInterval.HOUR, 
			(int)tsFile.getIDTINT());
		tsFile.getObservedTS().setDataUnits((String)tsFile.getTSUNIT());
		tsFile.getObservedTS().setDataUnitsOriginal((String)tsFile.getTSUNIT());
		tsFile.getObservedTS().setDescription(
			(String)tsFile.getTSDESC());
		tsFile.getObservedTS().addToComments((String)tsFile.getTSID());
		tsFile.getObservedTS().addToComments(
			(String)tsFile.getTSDataType());
		tsFile.getObservedTS().addToGenesis("Read time series for "+
			(String)tsFile.getTSID()+
			" from "+dtTempStart.toString()+" to "+
			dtTempEnd.toString()+" using NWSRFS FS5Files \""
			+__fs5FilesLocation.substring(0,
			__fs5FilesLocation.length()-1)+"\"");
	}
	else {
		Message.printStatus( 2, routine, "Future data are available.");
		endObsJul = startObsJul + ((int)tsFile.getIPTFUT()
			- (int)tsFile.getIPTREG() - 1) 
			* (int)tsFile.getIDTINT();
		startFutJul = endObsJul+(int)tsFile.getIDTINT();
		endFutJul = startObsJul+(int)tsFile.getNTSNUM()
			* (int)tsFile.getIDTINT();

		// Get number of observed and future data points
		obsDataNum = (int)tsFile.getIPTFUT() 
			- (int)tsFile.getIPTREG();
		futDataNum = (int)tsFile.getNTSNUM() - obsDataNum;

		// Set values into the Observed and Future TS objects
		dtTempStart = NWSRFS_Util.getDateFromJulianHour1900(startObsJul);
		dtTempStart.setTimeZone("Z");
		tsFile.getObservedTS().setDate1(dtTempStart);

		// Set identifier string
		tsident_string = tsFile.getTSID()+".NWSRFS."+
			tsFile.getTSDataType()+".";
		
		// Set the TS object Identifier
		if(tsFile.getTSDTInterval() == 0) {
			tsident_string += "*~NWSRFS_FS5Files~" + getFS5FilesLocation();
			tsFile.getObservedTS().setIdentifier(tsident_string);
		}
		else {
			tsident_string += tsFile.getTSDTInterval()+
				"Hour~NWSRFS_FS5Files~" + getFS5FilesLocation();
			tsFile.getObservedTS().setIdentifier(tsident_string);
		}
		
		dtTempEnd = NWSRFS_Util.getDateFromJulianHour1900(endObsJul);
		dtTempEnd.setTimeZone("Z");
		tsFile.getObservedTS().setDate2(dtTempEnd);
		tsFile.getObservedTS().allocateDataSpace();
		tsFile.getObservedTS().setDataInterval(TimeInterval.HOUR, 
			(int)tsFile.getIDTINT());
		tsFile.getObservedTS().setDataUnits((String)tsFile.getTSUNIT());
		tsFile.getObservedTS().setDataUnitsOriginal((String)tsFile.getTSUNIT());
		tsFile.getObservedTS().setDescription(
			(String)tsFile.getTSDESC() );
		tsFile.getObservedTS().addToComments((String)tsFile.getTSID());
		tsFile.getObservedTS().addToComments(
			(String)tsFile.getTSDataType());
		tsFile.getObservedTS().addToGenesis("Read time series for "+
			(String)tsFile.getTSID()+
			" from "+dtTempStart.toString()+" to "+
			dtTempEnd.toString()+" using NWSRFS FS5Files \""
			+__fs5FilesLocation.substring(0,
			__fs5FilesLocation.length()-1)+"\"");

		dtTempStart = NWSRFS_Util.getDateFromJulianHour1900(startFutJul);
		dtTempStart.setTimeZone("Z");
		tsFile.getFutureTS().setDate1(dtTempStart);

		dtTempEnd = NWSRFS_Util.getDateFromJulianHour1900(endFutJul);
		dtTempEnd.setTimeZone("Z");
		tsFile.getFutureTS().setDate2(dtTempEnd);
		Message.printStatus (2,routine,"Future start=" + dtTempStart);
		Message.printStatus (2,routine,"Future end=" + dtTempEnd);
		tsFile.getFutureTS().allocateDataSpace();
		tsFile.getFutureTS().setDataInterval(TimeInterval.HOUR, 
			(int)tsFile.getIDTINT());
		tsFile.getFutureTS().setIdentifier(tsident_string);
		tsFile.getFutureTS().setDataUnits((String)tsFile.getTSUNIT());
		tsFile.getFutureTS().setDataUnitsOriginal((String)tsFile.getTSUNIT());
		tsFile.getFutureTS().setDescription((String)tsFile.getTSDESC());
		tsFile.getFutureTS().addToComments((String)tsFile.getTSID());
		tsFile.getFutureTS().addToComments(
			(String)tsFile.getTSDataType());
		tsFile.getFutureTS().addToGenesis("Read time series for "+
			(String)tsFile.getTSID()+
			" from "+dtTempStart.toString()+" to "+
			dtTempEnd.toString()+" using NWSRFS FS5Files \""
			+__fs5FilesLocation.substring(0,
			__fs5FilesLocation.length()-1)+"\"");
	}
	
	// Just checking to see if data exist do not read Data
	if(!readData) 
	{
		EDIS.close();
		return true;
	}

	// Get the Observed/Regular data
	// First rewind the file
	// Now read from the PRDTSn file to get the actual Time Series data
	EDIS.close();
	rewind(RA);

	// Now seek to the beginning of the record + location of first regular
	// data value in record. Remember the minimum length of a TS header 
	// is 72 bytes then add the length of any extra header info.
// TODO (JTS - 2004-08-21)
// explain the magic number 64 and 72	
	if (recordNum > 0) {
		seek(RA, (recordNum - 1) * 64
			+ (72 + (int)tsFile.getNXHDR() * __WORDSIZE), 
			true);
	}

	// Read the data and insert into the HourTS object
// TODO (JTS - 2004-08-21)
// explain the magic number 64	
	EDIS = read(RA, 0, 64, obsDataNum * __WORDSIZE);
	float floatValue;
	for (i = 0;i < obsDataNum; i++) {
		floatValue = EDIS.readEndianFloat();
		dtTemp = NWSRFS_Util.getDateFromJulianHour1900(startObsJul 
			+ i * (int)tsFile.getIDTINT());
		dtTemp.setTimeZone("Z");
		tsFile.getObservedTS().setDataValue(dtTemp,
			(double)floatValue);
//Message.printWarning(10,routine,i+": Observation Data["+dtTemp.toString()+"] = "+floatValue);
	}

	// If there is future data read that into the FutureTS object
	if (tsFile.getIPTFUT() != 0) {
		// First rewind the file
		// Now read from the PRDTSn file to get the actual 
		// Time Series data
		EDIS.close();
		rewind(RA);

		// Now seek to the beginning of the record + location 
		// of first future data value in record. The minimum 
		// length of a TS header is 72.
// TODO (JTS - 2004-08-21)
// explain the magic number 64 and 72		
		if (recordNum > 0) {
			seek(RA,
				(recordNum - 1) * 64 
				+ (72 + (int)tsFile.getNXHDR() * __WORDSIZE 
				+ (obsDataNum) * __WORDSIZE),
				true);
		}

		// Read the data and insert into the HourTS object
// TODO (JTS - 2004-08-21)
// explain the magic number 64		
		EDIS = read(RA, 0, 64, 
			futDataNum * __WORDSIZE);
		for (i = 0; i < futDataNum; i++) {
			floatValue = EDIS.readEndianFloat();
			dtTemp = NWSRFS_Util.getDateFromJulianHour1900(startFutJul 
				+ i * (int)tsFile.getIDTINT());
			dtTemp.setTimeZone("Z");
			tsFile.getFutureTS().setDataValue(dtTemp,
				(double)floatValue);
//Message.printWarning(10,routine,i+": Future Data["+dtTemp.toString()+"] = "+floatValue);
		}
	}

	EDIS.close();
	
	return true;
}

/** 
Reads values from the FCRATING NWSRFS processed database file into the 
data members of the NWSRFS_RatingCurve class. 
The data members of this class will constitute the storage of information 
about a rating curve in the NWSRFS system.
@param ratingCurveID the Rating Curve ID in which to pull and store information 
from the processed database file.
@return NWSRFS_RatingCurve the NWSRFS_RatingCurve object which stores the 
data from the FCRATING binary database file. This class should be 
instantiated by the user.
@throws Exception if the database could not be opened or if the rating curve
ID could not be found in the database or if there was an error reading the
data.
*/
public NWSRFS_RatingCurve readRatingCurve(String ratingCurveID) 
throws Exception {
	// Check to see if the rating curve pointer object exists. If
	// not create it.
	if (_fcrcptr == null) {
		_fcrcptr = readFCRCPTR();
	}

	Enumeration enIREC = _fcrcptr.getIREC().elements();
	NWSRFS_RatingCurve rcFile = new NWSRFS_RatingCurve(ratingCurveID);

	// Check if the the database binary file is open as a
	// Random Access object
// TODO (JTS - 2004-08-19)
// always defined to be true in this class and used like this.  Can these
// be removed? 	
	boolean readOFSFS5Files = true;
	if (!checkRandomAccessFileOpen(__FCRATING, readOFSFS5Files)) {
		throw new Exception("Can not open the " 
			+ __dbFileNames[__FCRATING] + " binary database file");
	}

	//Get the records from FCSEGPTR file
	long recordNum = -1;
	long tempIREC = -1;
	String tempIRC = null;
	for (Enumeration enIRC = _fcrcptr.getRCID().elements(); 
		enIRC.hasMoreElements();) {
		tempIRC = (String)enIRC.nextElement();
		tempIREC = ((Integer)enIREC.nextElement()).longValue();

		if (tempIRC.equalsIgnoreCase(rcFile.getRCID())) {
			recordNum = tempIREC;
		}
	}
	
	if (recordNum == -1) {
		throw new Exception("NWSRFS_RatingCurve: Rating Curve ID: "
			+ rcFile.getRCID() + " not found");
	}
	else {	
		// keep reading until an EOFException is caught
		char[] charValue = null;
		int i = 0;
		int j = 0;
		String parseChar = null;
		rewind(__NWSRFS_DBFiles[__FCRATING]);
		
		//Get the record which holds the members of the 
		// RC definition status
		EndianDataInputStream EDIS = read(
			__NWSRFS_DBFiles[__FCRATING], recordNum - 1,
			__byteLength[__FCRATING]);
		
		// Field 1 - [type field name here]
		charValue = new char[8];
		for (j = 0; j < 8; j++) {
			charValue[j] = EDIS.readEndianChar1();
		}
		parseChar = new String(charValue).trim();
		if (parseChar.length() != 0)
			rcFile.setRTCVID(parseChar);

		// Field 2 - [type field name here]
		charValue = new char[20];
		for (j = 0; j < 20; j++) {
			charValue[j] = EDIS.readEndianChar1();
		}
		parseChar = new String(charValue).trim();
		if (parseChar.length() != 0) {
			rcFile.setRIVERN((String)parseChar);	
		}

		// Field 3 - [type field name here]
		charValue = new char[20];
		for (j = 0; j < 20; j++) {
			charValue[j] = EDIS.readEndianChar1();
		}
		parseChar = new String(charValue).trim();
		if (parseChar.length() != 0) {
			rcFile.setRIVSTA((String)parseChar);
		}
	
		// Field 4 - [type field name here]
		rcFile.setRLAT(checkFloat(EDIS.readEndianFloat(),
			-100000, 100000, 0));
	
		// Field 5 - [type field name here]
		rcFile.setRLONG(checkFloat(EDIS.readEndianFloat(),
			-100000, 100000, 0));
				
		// Field 6 - [type field name here]
		for (i = 0; i < 5; i++) {	
			charValue = new char[4];
			for (j = 0; j < 4; j++) {
				charValue[j] = EDIS.readEndianChar1();
			}
			parseChar = new String(charValue).trim();
			if (parseChar.length() != 0) {
				rcFile.setFPTYPE(i, (String)parseChar);
			}
		}
				
		// Field 7 - [type field name here]
		rcFile.setAREAT(checkFloat(EDIS.readEndianFloat(),
			0, 10000000, 0));
				
		// Field 8 - [type field name here]
		rcFile.setAREAL(checkFloat(EDIS.readEndianFloat(),
			0, 10000000, 0));
				
		// Field 9 - [type field name here]
		rcFile.setFLDSTG(checkFloat(EDIS.readEndianFloat(),
			-998, 100000, -999));
				
		// Field 10  - [type field name here]
		rcFile.setFLOODQ(checkFloat(EDIS.readEndianFloat(),
			-998, 100000, -999));
		
		// Field 11  - [type field name here]
		charValue = new char[4];
		for (j = 0; j < 4; j++) {
			charValue[j] = EDIS.readEndianChar1();
		}
		parseChar = new String(charValue).trim();
		if (parseChar.length() != 0) {
			rcFile.setPVISFS((String)parseChar);
		}

		// Field 12  - [type field name here]
		rcFile.setSCFSTG(checkFloat(EDIS.readEndianFloat(),
			-998, 100000, -999));
				
		// Field 13  - [type field name here]
		rcFile.setWRNSTG(checkFloat(EDIS.readEndianFloat(),
			-998, 100000, -999));
		
		// Field 14  - [type field name here]
		rcFile.setGZERO(checkFloat(EDIS.readEndianFloat(),
			-998, 100000, -999));
	
		// Field 15  - [type field name here]
		rcFile.setNRCPTS(checkInt(EDIS.readEndianInt(),
			0, 10000000, 0));

		// Field 16  - [type field name here]
		rcFile.setLOCQ(checkInt(EDIS.readEndianInt(),
			0, 225, 0));
			
		// Field 17  - [type field name here]
		rcFile.setLOCH(checkInt(EDIS.readEndianInt(),
			0, 225, 0));

		// Field 18  - [type field name here]
		rcFile.setSTGMIN(checkFloat(EDIS.readEndianFloat(),
			-998, 10000000, -999));

		// Field 19  - [type field name here]
		// intValue was compared <= 0, hence the '1'
		rcFile.setNCROSS(checkInt(EDIS.readEndianInt(),
			1, 100000, 0));

		// Field 20  - [type field name here]
		// intValue was compared <= 0, hence the '1'
		rcFile.setLXTOPW(checkInt(EDIS.readEndianInt(),
			1, 225, 0));

		// Field 21  - [type field name here]
		// intValue was compared <=0, hence the '1'
		rcFile.setLXELEV(checkInt(EDIS.readEndianInt(),
			1, 225, 0));

		// Field 22  - [type field name here]
		rcFile.setABELOW(checkFloat(EDIS.readEndianFloat(),
			0, 100000, -999));

		// Field 23  - [type field name here]
		rcFile.setFLOODN(checkFloat(EDIS.readEndianFloat(),
			-998, 100000, -999));

		// Field 24  - [type field name here]
		rcFile.setSLOPE(checkFloat(EDIS.readEndianFloat(),
			-998, 100000, -999));

		// Field 25  - [type field name here]
		rcFile.setFRLOOP(checkFloat(EDIS.readEndianFloat(),
			-998, 100000, -999));

		// Field 26  - [type field name here]
		rcFile.setSHIFT(checkFloat(EDIS.readEndianFloat(),
			-998, 100000, -999));

		// Field 27  - [type field name here]
		charValue = new char[4];
		for (j = 0; j < 4; j++) {
			charValue[j] = EDIS.readEndianChar1();
		}
		parseChar = new String(charValue).trim();
		if (parseChar.length() != 0) {
			rcFile.setOPTION((String)parseChar);
		}

		// Field 28  - [type field name here]
		rcFile.setLASDAY(checkFloat(EDIS.readEndianFloat(),
			0, 10000000, 0));

		// Field 29  - [type field name here]
		rcFile.setIPOPT(checkInt(EDIS.readEndianInt(),
			0, 225, 0));

		// Field 30  - [type field name here]
		rcFile.setRFSTG(checkFloat(EDIS.readEndianFloat(),
			-998, 10000000, -999));

		// Field 31  - [type field name here]
		rcFile.setRFQ(checkFloat(EDIS.readEndianFloat(),
			-998, 10000000, 0));

		// Field 32  - [type field name here]
		rcFile.setIRFDAY(checkInt(EDIS.readEndianInt(),
			-998, 100000, -999));

		// Field 33  - [type field name here]
		for (i = 0; i < 5;i++) {
			charValue = new char[4];
			for (j = 0; j < 4; j++) {
				charValue[j] = EDIS.readEndianChar1();
			}
			parseChar = new String(charValue).trim();
			if (parseChar.length() != 0) {
				rcFile.setRFCOMPT(i, (String)parseChar);
			}
		}

		// Field 34  - [type field name here]
		for (i = 0; i < 25;i++) {
			// comparison was <=0, hence the '1'
			rcFile.setEMPTY(i, checkFloat(
				EDIS.readEndianFloat(), 
				1, 10000000, 0));
		}

		// Field 35  - [type field name here]
		for (i = 0; i < 225;i++) {
			// comparison was <=-1000000, hence the 
			// '-999999'
			rcFile.setXRC(i, checkFloat(
				EDIS.readEndianFloat(),
				-999999, 10000000, -999));
		}

		EDIS.close();
	}

	return rcFile;
}	

/**
Reads in to a Vector of Strings the list of Rating Curve identifiers. It will
basically regurgetate the fcrcptr file which creates a list of RC ids and a
record number.
@return Vector of Strings containing the list of all RC ids in the database.
@throws Exception if something goes wrong.
*/
public Vector readRatingCurveList() throws Exception
{
	Vector rcList = new Vector();
	
	// Check to see if the rating curve pointer object exists. If not create it.
	if (_fcrcptr == null) {
		_fcrcptr = readFCRCPTR();
	}

	// check to see if still null. If so the return and empty list.
	if (_fcrcptr == null || _fcrcptr.getRCID() == null) {
		return rcList;
	}
	// Enumerate through the pointer/index file for rating curves
	for (Enumeration enIRC = _fcrcptr.getRCID().elements(); 
		enIRC.hasMoreElements();) {
		rcList.addElement((String)enIRC.nextElement());
	}
	
	// Return Vector of rc ids
	return rcList;
}
/** 
Reads values from the FCSEGSTS NWSRFS processed database file into 
the data members of the NWSRFS_Segment class. 
The data members of this class will constitute the storage of information 
about a segment in the NWSRFS system.
@param segmentID the segment identifier String in which to pull information from
the processed database file.
@return NWSRFS_Segment the NWSRFS_Segment object which stores the data 
from the FCSEGSTS binary database file. This class should be instantiated 
by the user.
@throws Exception if there are problems reading from the database.
*/
public NWSRFS_Segment readSegment(String segmentID) 
throws Exception {
	return readSegment(segmentID, (NWSRFS_ForecastGroup)null, false);
}

/** 
Reads values from the FCSEGSTS NWSRFS processed database file into the 
data members of the NWSRFS_Segment class. 
The data members of this class will constitute the storage of information 
about a segment in the NWSRFS system.
@param segmentID the segment identifier String in which to pull information 
from the processed database file.
@param FG the Forecast Group object which is the parent to this Segment if 
it is known and instantiated. This would be necessary to get the Carryover 
values out of the C array.
@return NWSRFS_Segment the NWSRFS_Segment object which stores the data 
from the FCSEGSTS binary database file. This class should be 
instantiated by the user.
@throws Exception if there are problems reading from the database.
*/
public NWSRFS_Segment readSegment(String segmentID,NWSRFS_ForecastGroup FG) 
throws Exception {
	return readSegment(segmentID, FG, false);
}

/** 
Reads values from the FCSEGSTS NWSRFS processed database file into the 
data members of the NWSRFS_Segment class. 
The data members of this class will constitute the storage of information 
about a segment in the NWSRFS system. 
@param segmentID the segment identifier String in which to pull information 
from the processed database file.
@param FG the Forecast Group object which is the parent to this Segment if 
it is known and instantiated. This would be necessary to get the 
Carryover values out of the C array.
@param deepRead a boolean specifying whether or not just header or id's 
are read from the Segment, Operations, and TimeSeries objects. If true 
read all data.
@return NWSRFS_Segment the NWSRFS_Segment object which stores the data 
from the FCSEGSTS binary database file. This class should be 
instantiated by the user.
@throws Exception if there are problems reading from the database.
*/
public NWSRFS_Segment readSegment(String segmentID,NWSRFS_ForecastGroup FG, boolean deepRead) 
throws Exception
{
	StopWatch mainsw = new StopWatch();
	mainsw.start();
	// Check to see if the Segment pointer object exists. If not
	// create it to find the record number.
	StopWatch sw1 = new StopWatch();
	sw1.start();
	if (_fcsegptr == null) {
		_fcsegptr = readFCSEGPTR();
	}

	long recordNum = -1;
	long tempIREC = -1;
	Enumeration enIREC = _fcsegptr.getIREC().elements();
	//Get the records from FCSEGPTR file
	for (Enumeration enISEG = _fcsegptr.getISEG().elements(); 
		enISEG.hasMoreElements();) {
		String tempISEG = (String)enISEG.nextElement();
		tempIREC = ((Integer)enIREC.nextElement()).longValue();

		if (tempISEG.equalsIgnoreCase(segmentID)) {
			recordNum = tempIREC;
		}
	}
	
	NWSRFS_Segment segFile = null;
	sw1.stop();
	StopWatch sw2 = new StopWatch();
	if (recordNum == -1) {
		throw new Exception("NWSRFS_Segment: Segment ID: " + segmentID
		 + " not found");
	}
	else {	
		sw2.start();
		char[] charValue = null;
		int i = 0;
		int j = 0;
		int bytesToSkip;
		String parseChar = null;

		// First create an instance of NWSRFS_Segment
		segFile = new NWSRFS_Segment(segmentID);

		// Check if the the database binary file is open as a
		// Random Access object
		boolean readOFSFS5Files = true;
		if (!checkRandomAccessFileOpen(__FCSEGSTS, readOFSFS5Files)) {
			throw new Exception("Can not open the " 
				+ __dbFileNames[__FCSEGSTS] 
				+ " binary database file");
		}

		rewind(__NWSRFS_DBFiles[__FCSEGSTS]);
		
		// Get the record which holds the members of the 
		// segment definition status
		EndianDataInputStream EDIS = read(__NWSRFS_DBFiles[__FCSEGSTS],
			recordNum-1, __byteLength[__FCSEGSTS]);
		
		// Field 1 - [type field name here]
		charValue = new char[8];
		for (j = 0; j < 8; j++) {
			charValue[j] = EDIS.readEndianChar1();
		}

		parseChar = new String(charValue).trim();
	
		if (parseChar.length() != 0) {
			segFile.setIDSEG(parseChar);
		}

		// If deepRead is false then only read the Segment ID
		// and continue from here else read in all Segment fields
		// Also need the array size for the P,T, and TS arrays.
		if (!deepRead) {
			// Skip bytes
// TODO (JTS - 2004-08-21)
// so instead of reading the value in order to skip it, why not just
// do a forward seek 8 bytes (or whatever)?			
			bytesToSkip = 56;
			EDIS.skipBytes(bytesToSkip);

			// Field 4 - [type field name here]
			segFile.setIPREC(EDIS.readEndianInt());

			// Skip more bytes
// TODO (JTS - 2004-08-21)
// so instead of reading the value in order to skip it, why not just
// do a forward seek 8 bytes (or whatever)?			
			bytesToSkip = 80;
			EDIS.skipBytes(bytesToSkip);

			// Field 15 - [type field name here]
			segFile.setNT(EDIS.readEndianInt());
		
			// Field 16 - [type field name here]
			segFile.setNTS(EDIS.readEndianInt());
	
			// Field 17 - [type field name here]
			segFile.setNP(EDIS.readEndianInt());
		}
		else {
			// Field 2 - [type field name here]
			for (i = 0; i < 5; i++) {
				charValue = new char[8];
				for (j = 0; j<8; j++) {
					charValue[j] = EDIS.readEndianChar1();
				}
				parseChar = new String(charValue).trim();
				if (parseChar.length() != 0) {
					segFile.setIUPSEG(i, parseChar);
				}
			}

			// Field 3 - [type field name here]
			for (i = 0; i < 2; i++) {
				charValue = new char[8];
				for (j = 0; j < 8; j++) {
					charValue[j] = EDIS.readEndianChar1();
				}
				parseChar = new String(charValue).trim();
				if (parseChar.length() != 0) {
					segFile.setIDNSEG(i, parseChar);
				}
			}

			// Field 4 - [type field name here]
			segFile.setIPREC(EDIS.readEndianInt());
	
			// Field 5 - [type field name here]
			segFile.setIWOCRY(EDIS.readEndianInt());

			// Field 6 - [type field name here]
			charValue = new char[8];
			for (j = 0; j < 8; j++) {
				charValue[j] = EDIS.readEndianChar1();
			}
			parseChar = new String(charValue).trim();
			if (parseChar.length() != 0) {
				segFile.setIFGID(parseChar);
			}

			// Field 7 - [type field name here]
			charValue = new char[8];
			for (j = 0; j < 8; j++) {
				charValue[j] = EDIS.readEndianChar1();
			}
			parseChar = new String(charValue).trim();
			if (parseChar.length() != 0) {
				segFile.setICGID(parseChar);
			}

			// Field 8 - [type field name here]
			charValue = new char[20];
			for (j = 0; j < 20; j++) {
				charValue[j] = EDIS.readEndianChar1();
			}
			parseChar = new String(charValue).trim();
			if (parseChar.length() != 0) {
				segFile.setSGDSCR((String)parseChar);
			}
		
			// Field 9 - [type field name here]
			for (i = 0; i < 5; i++) {
				segFile.setICRDTE(i, EDIS.readEndianInt());
			}
		
			// Field 10 - [type field name here]
			segFile.setMINDT(EDIS.readEndianInt());

			// Field 11 - [type field name here]
			segFile.setXLAT(EDIS.readEndianFloat());
		
			// Field 12 - [type field name here]
			segFile.setXLONG(EDIS.readEndianFloat());

			// Field 13 - [type field name here]
			segFile.setNC(EDIS.readEndianInt());
		
			// Field 14 - [type field name here]
			segFile.setND(EDIS.readEndianInt());
	
			// Field 15 - [type field name here]
			segFile.setNT(EDIS.readEndianInt());
		
			// Field 16 - [type field name here]
			segFile.setNTS(EDIS.readEndianInt());
	
			// Field 17 - [type field name here]
			segFile.setNP(EDIS.readEndianInt());
	
			// Field 18 - [type field name here]
			segFile.setNCOPS(EDIS.readEndianInt());
	
			// Field 19 - [type field name here]
			for (i = 0; i < 20; i++) {
				segFile.setINCSEG(i, EDIS.readEndianInt());
			}
		
			// Field 20 - [type field name here]
			segFile.setIDEFSG(EDIS.readEndianInt());
		
			// Field 21 - [type field name here]
			segFile.setIEREC(EDIS.readEndianInt());

			EDIS.close();
		}
		sw2.stop();
	}

	StopWatch sw3 = new StopWatch();
	StopWatch sw4 = new StopWatch();
	StopWatch sw5 = new StopWatch();

	// Set the Forecast Group as parent to the Segment if not null
	if (FG != null) {
		sw3.start();
		segFile.setForecastGroup(FG);
		sw3.stop();
	}

	// Create and add the Operation Object (contains Vectors 
	// of Operations) associated with this Segment 
	sw4.start();
	readOperations(segFile, deepRead);
	sw4.stop();

	// Do not read all of the carryover slots if only reading IDs.
	// Create and add the Carryover Object (contains Vectors of 
	// Carryover values) associated with this Segment 
	sw5.start();
	readCarryover(segFile, deepRead);
	sw5.stop();

	mainsw.stop();
	//Message.printStatus(1, "", "readSegment: " + mainsw.getSeconds());
	//Message.printStatus(1, "", "      Setup: " + sw1.getSeconds());
	//Message.printStatus(1, "", "       Read: " + sw2.getSeconds());
	//Message.printStatus(1, "", "         FG: " + sw3.getSeconds());
	//Message.printStatus(1, "", "         OP: " + sw4.getSeconds());
	//Message.printStatus(1, "", "         CG: " + sw5.getSeconds());
	//Message.printStatus(1, "", "");
	return segFile;
}	

/** 
Reads the preprocessed parameteric database to fill the data members of the
NWSRFS_Station object argument. It will read the information from the 
preprocessed parameteric database (PPDB) files PPPPARM<i>n</i> where <i>n</i> 
is the logical unit found from the PPPINDEX file associated with a specific 
parameter type. The known "station" parameter types in the PPDB are GENL 
(genral station), PCPN (precip), PE (potential evaporation), 
RRS (River, Reservoir, and Stream), and TEMP (temperature).
(Currently this methods does nothing. It needs to be completed.)
@param stationID a String which holds the Identifier for a Station object. 
It will create a Station Object then call the overloaded method readStation to
fill out the station object.
@param deepRead a boolean specifying whether to read all station parameters
from the PPDB or just general parameters.
@return  an NWSRFS_Station object which stores the data from
the PPDB files PPPARMn.
@throws Exception if an error is detected.
*/
public NWSRFS_Station readStation(String stationID, boolean deepRead) 
throws Exception
{
	NWSRFS_Station station = new NWSRFS_Station(stationID);
	return readStation(station,deepRead);
}

/** 
Reads the preprocessed parameteric database to fill the data members of the
NWSRFS_Station object argument. It will read the information from the 
preprocessed parameteric database (PPDB) files PPPPARM<i>n</i> where <i>n</i> 
is the logical unit found from the PPPINDEX file associated with a specific 
parameter type. The known "station" parameter types in the PPDB are GENL 
(genral station), PCPN (precip), PE (potential evaporation), 
RRS (River, Reservoir, and Stream), and TEMP (temperature).
(Currently this methods does nothing. It needs to be completed.)
@param station a NWSRFS_Station which holds the minimum set of data for a 
station dervied from the PPPINDEX file. This method will fill out the station
object.
@param deepRead a boolean specifying whether to read all station parameters
from the PPDB or just general parameters.
@return  an NWSRFS_Station object which stores the data from
the PPDB files PPPARMn.
@throws Exception if an error is detected.
*/
public NWSRFS_Station readStation(NWSRFS_Station station, boolean deepRead) 
throws Exception
{
	NWSRFS_PPPINDEX pppindex = getPPPIndex();
	Integer logicalUnitNumGENL = new Integer(-1), 
		logicalUnitNumPCPN = new Integer(-1), 
		logicalUnitNumPE   = new Integer(-1), 
		logicalUnitNumRRS  = new Integer(-1), 
		logicalUnitNumTEMP = new Integer(-1);
	boolean luDoneGENL=false, luDonePCPN=false, luDonePE=false, luDoneRRS=false, luDoneTEMP=false;

	// Check to see if the pppindex file exists! If not return station.
	if(pppindex == null) { 
		setPPPIndex(readPPPINDEX());
	}
	else if(pppindex.getPARMTP() == null) {
		return station;
	}

	// Now check to see if we have set initial data prior to calling
	// this routine. If not, we must set initial data!
	if(station.getLogicalUnitNum("GENL") == -1) {
		// Get the logical unit for the parameter type.
		for(int i=0;i<(pppindex.getPARMTP()).size();i++) {
			if(((String)(pppindex.getPARMTP()).elementAt(i)).
			equalsIgnoreCase("GENL") && !luDoneGENL) {
				logicalUnitNumGENL = (Integer)pppindex.
					getLUFILE().elementAt(i);
					luDoneGENL = true;
					continue;
			}
			else if(((String)(pppindex.getPARMTP()).elementAt(i)).
			equalsIgnoreCase("PCPN") && !luDonePCPN) {
				logicalUnitNumPCPN = (Integer)pppindex.
					getLUFILE().elementAt(i);
					luDonePCPN = true;
					continue;
			}
			else if(((String)(pppindex.getPARMTP()).elementAt(i)).
			equalsIgnoreCase("PE") && !luDonePE) {
				logicalUnitNumPE = (Integer)pppindex.
					getLUFILE().elementAt(i);
					luDonePE = true;
					continue;
			}
			else if(((String)(pppindex.getPARMTP()).elementAt(i)).
			equalsIgnoreCase("RRS") && !luDoneRRS) {
				logicalUnitNumRRS = (Integer)pppindex.
					getLUFILE().elementAt(i);
					luDoneRRS = true;
					continue;
			}
			else if(((String)(pppindex.getPARMTP()).elementAt(i)).
			equalsIgnoreCase("TEMP") && !luDoneTEMP) {
				logicalUnitNumTEMP = (Integer)pppindex.
					getLUFILE().elementAt(i);
					luDoneTEMP = true;
					continue;
			}
		}
		
		// Next loop through the ID.size number of records
		for(int i=0;i<(pppindex.getID()).size();i++) {
			// If the type is GENL add to the PropList
			if(((String)(pppindex.getITYPE()).elementAt(i)).
			equalsIgnoreCase("GENL") && 
			pppindex.getID(i).equalsIgnoreCase(station.getID())) {
				station.addLogicalUnitNum("GENL",
					logicalUnitNumGENL);
				station.addRecordNum("GENL", (Integer)
					(pppindex.getIREC()).elementAt(i));
			}
			else if(((String)(pppindex.getITYPE()).elementAt(i)).
			equalsIgnoreCase("PCPN") && 
			pppindex.getID(i).equalsIgnoreCase(station.getID())) {
				station.addLogicalUnitNum("PCPN",
					logicalUnitNumPCPN);
				station.addRecordNum("PCPN", (Integer)
					(pppindex.getIREC()).elementAt(i));
				station.setIsPCPN(true);
			}
			else if(((String)(pppindex.getITYPE()).elementAt(i)).
			equalsIgnoreCase("PE") && 
			pppindex.getID(i).equalsIgnoreCase(station.getID())) {
				station.addLogicalUnitNum("PE",logicalUnitNumPE);
				station.addRecordNum("PE", (Integer)
					(pppindex.getIREC()).elementAt(i));
				station.setIsPE(true);
			}
			else if(((String)(pppindex.getITYPE()).elementAt(i)).
			equalsIgnoreCase("RRS") && 
			pppindex.getID(i).equalsIgnoreCase(station.getID())) {
				station.addLogicalUnitNum("RRS",
					logicalUnitNumRRS);
				station.addRecordNum("RRS", (Integer)
					(pppindex.getIREC()).elementAt(i));
				station.setIsRRS(true);
			}
			else if(((String)(pppindex.getITYPE()).elementAt(i)).
			equalsIgnoreCase("TEMP") && 
			pppindex.getID(i).equalsIgnoreCase(station.getID())) {
				station.addLogicalUnitNum("TEMP",
					logicalUnitNumTEMP);
				station.addRecordNum("TEMP", (Integer)
					(pppindex.getIREC()).elementAt(i));
				station.setIsTEMP(true);
			}
		}
	}
	
	// Now check to see if station exists. If not, we return null.
	if(station.getRecordNum("GENL") == -1) {
		return null;
	}

	// Since all stations are "GENL" parameter stations just
	// parse the GENL array elements regardless.
	parseParametericArray((Object)station,"GENL",deepRead);
	
	// Now if a precip station and deepRead is requested
	// parse the PCPN array elements.
	if(station.getIsPCPN())
		parseParametericArray((Object)station,"PCPN",deepRead);
	
	// Now if a PE station and deepRead is requested
	// parse the PE array elements.
	if(station.getIsPE())
		parseParametericArray((Object)station,"PE",deepRead);
	
	// Now if a RRS station and deepRead is requested
	// parse the RRS array elements.
	if(station.getIsRRS())
		parseParametericArray((Object)station,"RRS",deepRead);
	
	// Now if a temperature station and deepRead is requested
	// parse the TEMP array elements.
	if(station.getIsTEMP())
		parseParametericArray((Object)station,"TEMP",deepRead);
	
	// Return the filled out station object
	return station;
}

/**
Reads into a Hashtable the complete list of station identifiers found in
the PPDB binary database files PPPINDEX. The Hastable will use the station ID 
value found in the PPPINDEX file for the GENL parameter type as the String key
and a NWSRFS_Station as the contents. The contents will hold NWSRFS_Station 
values for the logical unit (to read the actual station parameter values from the 
PPPPARM<i>n</i> where <i>n</i> is the logical unit), the parameter type, and the
record number in the PPPPARM<i>n</i> file to get the parameters. This method
assumes that all stations can be retrieved by using the GENL PPDB parameter type
since all stations need general parameter information regardless of the actual
station type.
@return a Hashtable of NWSRFS_Station objects containing only the station 
identifier, the logical unit number for the PPPPARM<i>n</i> file, and the
parameter types available for the station. The Hashtable key is the station
identifier
@throws Exception if an error is detected.
*/
public Hashtable readStationHashtable() throws Exception
{
	NWSRFS_Station station = null;
	NWSRFS_PPPINDEX pppindex = getPPPIndex();
	Hashtable stationList = new Hashtable();
	Integer logicalUnitNumGENL = new Integer(-1), 
		logicalUnitNumPCPN = new Integer(-1), 
		logicalUnitNumPE   = new Integer(-1), 
		logicalUnitNumRRS  = new Integer(-1), 
		logicalUnitNumTEMP = new Integer(-1);
	boolean luDoneGENL=false, luDonePCPN=false, luDonePE=false,
		luDoneRRS=false, luDoneTEMP=false;

	// Check to see if the pppindex file exists! If not return empty list.
	if(pppindex == null) { 
		setPPPIndex(readPPPINDEX());
	}
	else if(pppindex.getPARMTP() == null) {
		return stationList;
	}
	
	// Get the logical unit for the parameter type.
	for(int i=0;i<(pppindex.getPARMTP()).size();i++) {
		if(((String)(pppindex.getPARMTP()).elementAt(i)).
		equalsIgnoreCase("GENL") && !luDoneGENL) {
			logicalUnitNumGENL = (Integer)pppindex.
				getLUFILE().elementAt(i);
			luDoneGENL = true;
			continue;
		}
		else if(((String)(pppindex.getPARMTP()).elementAt(i)).
		equalsIgnoreCase("PCPN") && !luDonePCPN) {
			logicalUnitNumPCPN = (Integer)pppindex.
				getLUFILE().elementAt(i);
			luDonePCPN = true;
			continue;
		}
		else if(((String)(pppindex.getPARMTP()).elementAt(i)).
		equalsIgnoreCase("PE") && !luDonePE) {
			logicalUnitNumPE = (Integer)pppindex.
				getLUFILE().elementAt(i);
			luDonePE = true;
			continue;
		}
		else if(((String)(pppindex.getPARMTP()).elementAt(i)).
		equalsIgnoreCase("RRS") && !luDoneRRS) {
			logicalUnitNumRRS = (Integer)pppindex.
				getLUFILE().elementAt(i);
			luDoneRRS = true;
			continue;
		}
		else if(((String)(pppindex.getPARMTP()).elementAt(i)).
		equalsIgnoreCase("TEMP") && !luDoneTEMP) {
			logicalUnitNumTEMP = (Integer)pppindex.
				getLUFILE().elementAt(i);
			luDoneTEMP = true;
			continue;
		}
	}
	
	// Next loop through the ID.size number of records
	for(int i=0;i<(pppindex.getID()).size();i++) {
		// If the type is GENL add to the PropList
		if(((String)(pppindex.getITYPE()).elementAt(i)).
		equalsIgnoreCase("GENL")) {
			if(!stationList.containsKey(
				(String)(pppindex.getID()).elementAt(i))) {
				station = new NWSRFS_Station((String)
					(pppindex.getID()).elementAt(i));
				station.addLogicalUnitNum("GENL",
					logicalUnitNumGENL);
				station.addRecordNum("GENL", (Integer)
					(pppindex.getIREC()).elementAt(i));
				stationList.put(
					(String)(pppindex.getID()).elementAt(i),
					(NWSRFS_Station) station);
			}
			else {
				station = (NWSRFS_Station)stationList.get(
					(String)(pppindex.getID()).elementAt(i));
				station.addLogicalUnitNum("GENL",
					logicalUnitNumGENL);
				station.addRecordNum("GENL", (Integer)
					(pppindex.getIREC()).elementAt(i));
			}
		}
		else if(((String)(pppindex.getITYPE()).elementAt(i)).
		equalsIgnoreCase("PCPN")) {
			if(!stationList.containsKey(
				(String)(pppindex.getID()).elementAt(i))) {
				station = new NWSRFS_Station((String)
					(pppindex.getID()).elementAt(i));
				station.addLogicalUnitNum("PCPN",
					logicalUnitNumPCPN);
				station.addRecordNum("PCPN", (Integer)
					(pppindex.getIREC()).elementAt(i));
				station.setIsPCPN(true);
				stationList.put(
					(String)(pppindex.getID()).elementAt(i),
					(NWSRFS_Station) station);
			}
			else {
				station = (NWSRFS_Station)stationList.get(
					(String)(pppindex.getID()).elementAt(i));
				if(!station.getIsPCPN()) {
					station.addLogicalUnitNum("PCPN",
						logicalUnitNumPCPN);
					station.addRecordNum("PCPN", (Integer)
					(pppindex.getIREC()).elementAt(i));
					station.setIsPCPN(true);
				}
			}
		}
		else if(((String)(pppindex.getITYPE()).elementAt(i)).
		equalsIgnoreCase("PE")) {
			if(!stationList.containsKey(
				(String)(pppindex.getID()).elementAt(i))) {
				station = new NWSRFS_Station((String)
					(pppindex.getID()).elementAt(i));
				station.addLogicalUnitNum("PE",
					logicalUnitNumPE);
				station.addRecordNum("PE", (Integer)
					(pppindex.getIREC()).elementAt(i));
				station.setIsPE(true);
				stationList.put(
					(String)(pppindex.getID()).elementAt(i),
					(NWSRFS_Station) station);
			}
			else {
				station = (NWSRFS_Station)stationList.get(
					(String)(pppindex.getID()).elementAt(i));
				if(!station.getIsPE()) {
					station.addLogicalUnitNum("PE",
						logicalUnitNumPE);
					station.addRecordNum("PE", (Integer)
					(pppindex.getIREC()).elementAt(i));
					station.setIsPE(true);
				}
			}
		}
		else if(((String)(pppindex.getITYPE()).elementAt(i)).
		equalsIgnoreCase("RRS")) {
			if(!stationList.containsKey(
				(String)(pppindex.getID()).elementAt(i))) {
				station = new NWSRFS_Station((String)
					(pppindex.getID()).elementAt(i));
				station.addLogicalUnitNum("RRS",
					logicalUnitNumRRS);
				station.addRecordNum("RRS", (Integer)
					(pppindex.getIREC()).elementAt(i));
				station.setIsRRS(true);
				stationList.put(
					(String)(pppindex.getID()).elementAt(i),
					(NWSRFS_Station) station);
			}
			else {
				station = (NWSRFS_Station)stationList.get(
					(String)(pppindex.getID()).elementAt(i));
				if(!station.getIsRRS()) {
					station.addLogicalUnitNum("RRS",
						logicalUnitNumRRS);
					station.addRecordNum("RRS", (Integer)
					(pppindex.getIREC()).elementAt(i));
					station.setIsRRS(true);
				}
			}
		}
		else if(((String)(pppindex.getITYPE()).elementAt(i)).
		equalsIgnoreCase("TEMP")) {
			if(!stationList.containsKey(
				(String)(pppindex.getID()).elementAt(i))) {
				station = new NWSRFS_Station((String)
					(pppindex.getID()).elementAt(i));
				station.addLogicalUnitNum("TEMP",
					logicalUnitNumTEMP);
				station.addRecordNum("TEMP", (Integer)
					(pppindex.getIREC()).elementAt(i));
				station.setIsTEMP(true);
				stationList.put(
					(String)(pppindex.getID()).elementAt(i),
					(NWSRFS_Station) station);
			}
			else {
				station = (NWSRFS_Station)stationList.get(
					(String)(pppindex.getID()).elementAt(i));
				if(!station.getIsTEMP()) {
					station.addLogicalUnitNum("TEMP",
						logicalUnitNumTEMP);
					station.addRecordNum("TEMP", (Integer)
					(pppindex.getIREC()).elementAt(i));
					station.setIsTEMP(true);
				}
			}
		}
	}

	// return the proplist
	return stationList;
}

/**
Reads into a Hashtable the complete list of station identifiers found in
the PPDB binary database files PPPINDEX. The Hastable will use the station ID 
value found in the PPPINDEX file for the "input" parameter type as the String key
and a NWSRFS_Station as the contents. The contents will hold NWSRFS_Station values 
for the logical unit (to read the actual station parameter values from the 
PPPPARM<i>n</i> where <i>n</i> is the logical unit), the parameter type, and the
record number in the PPPPARM<i>n</i> file to get the parameters.
@param paramType a String specifying a parameter type to retrieve the list of
station identifiers. These can be PCPN, PE, RRS, and TEMP. This does not include
the GENL PPDB parameter type because the GENL parameter type would pull all
stations and that is done in the overloaded method.
@return a Hashtable of NWSRFS_Station objects containing only the station 
identifier, the logical unit number for the PPPPARM<i>n</i> file, and the
parameter types available for the station. The Hashtable key is the station
identifier.
@throws Exception if an error is detected.
*/
public Hashtable readStationHashtable(String paramType) throws Exception
{
	NWSRFS_Station station = null;
	NWSRFS_Station stationTemp = null;
	NWSRFS_PPPINDEX pppindex = getPPPIndex();
	Hashtable stationList = new Hashtable();
	Hashtable stationListTemp = new Hashtable();
	Integer logicalUnitNumGENL = new Integer(-1), 
		logicalUnitNumPCPN = new Integer(-1), 
		logicalUnitNumPE   = new Integer(-1), 
		logicalUnitNumRRS  = new Integer(-1), 
		logicalUnitNumTEMP = new Integer(-1),
		logicalUnitNum     = new Integer(-1);
	boolean luDoneGENL=false, luDonePCPN=false, luDonePE=false,
		luDoneRRS=false, luDoneTEMP=false;
	
	// Get the logical unit for the parameter type.
	for(int i=0;i<(pppindex.getPARMTP()).size();i++) {
		if(((String)(pppindex.getPARMTP()).elementAt(i)).equalsIgnoreCase("GENL") && !luDoneGENL) {
			logicalUnitNumGENL = (Integer)pppindex.getLUFILE().elementAt(i);
			luDoneGENL = true;
			continue;
		}
		else if(((String)(pppindex.getPARMTP()).elementAt(i)).equalsIgnoreCase("PCPN") && !luDonePCPN) {
			logicalUnitNumPCPN = (Integer)pppindex.getLUFILE().elementAt(i);
			luDonePCPN = true;
			continue;
		}
		else if(((String)(pppindex.getPARMTP()).elementAt(i)).equalsIgnoreCase("PE") && !luDonePE) {
			logicalUnitNumPE = (Integer)pppindex.getLUFILE().elementAt(i);
			luDonePE = true;
			continue;
		}
		else if(((String)(pppindex.getPARMTP()).elementAt(i)).equalsIgnoreCase("RRS") && !luDoneRRS) {
			logicalUnitNumRRS = (Integer)pppindex.getLUFILE().elementAt(i);
			luDoneRRS = true;
			continue;
		}
		else if(((String)(pppindex.getPARMTP()).elementAt(i)).equalsIgnoreCase("TEMP") && !luDoneTEMP) {
			logicalUnitNumTEMP = (Integer)pppindex.getLUFILE().elementAt(i);
			luDoneTEMP = true;
			continue;
		}
	}

	if(paramType.equalsIgnoreCase("PCPN"))
		logicalUnitNum = logicalUnitNumPCPN;
	else if(paramType.equalsIgnoreCase("PE"))
		logicalUnitNum = logicalUnitNumPE;
	else if(paramType.equalsIgnoreCase("RRS"))
		logicalUnitNum = logicalUnitNumRRS;
	else if(paramType.equalsIgnoreCase("TEMP"))
		logicalUnitNum = logicalUnitNumTEMP;
	else if(paramType.equalsIgnoreCase("GENL"))
		logicalUnitNum = logicalUnitNumGENL;
	else {
		throw new Exception("Wrong parameter type to read station "+
			"data from!");
	}
	
	// Next loop through the ID.size number of records
	for(int i=0;i<(pppindex.getID()).size();i++) {
		// If the type is GENL add to the PropList
		if(((String)(pppindex.getITYPE()).elementAt(i)).
		equalsIgnoreCase("GENL")) { 
			if(!stationListTemp.containsKey(
				(String)(pppindex.getID()).elementAt(i))) {
				station = new NWSRFS_Station((String)
					(pppindex.getID()).elementAt(i));
				station.addLogicalUnitNum("GENL",
					logicalUnitNumGENL);
				station.addRecordNum("GENL", (Integer)
					(pppindex.getIREC()).elementAt(i));
				stationListTemp.put(
					(String)(pppindex.getID()).elementAt(i),
					(NWSRFS_Station) station);
			}
		}
	}
	
	// Next loop through the ID.size number of records
	for(int i=0;i<(pppindex.getID()).size();i++) {
		// If the type is GENL add to the PropList
		if(((String)(pppindex.getITYPE()).elementAt(i)).
		equalsIgnoreCase("GENL") && 
		paramType.equalsIgnoreCase("GENL")) {
			if(!stationList.containsKey(
				(String)(pppindex.getID()).elementAt(i))) {
				station = new NWSRFS_Station((String)
					(pppindex.getID()).elementAt(i));
				station.addLogicalUnitNum("GENL",
					logicalUnitNumGENL);
				station.addRecordNum("GENL", (Integer)
					(pppindex.getIREC()).elementAt(i));
				stationList.put(
					(String)(pppindex.getID()).elementAt(i),
					(NWSRFS_Station) station);
			}
		}
		else if(((String)(pppindex.getITYPE()).elementAt(i)).
		equalsIgnoreCase(paramType) && stationListTemp.size() > 0) {
			if(!stationList.containsKey(
				(String)(pppindex.getID()).elementAt(i))) {
				station = new NWSRFS_Station((String)
					(pppindex.getID()).elementAt(i));
				station.addLogicalUnitNum("GENL",
					logicalUnitNumGENL);
				stationTemp = ((NWSRFS_Station)stationListTemp.
					get((String)(pppindex.getID()).
					elementAt(i)));
				try {
				station.addRecordNum("GENL",new Integer
					(stationTemp.getRecordNum("GENL")));
				} catch (NullPointerException NPe) {
				}
				station.addLogicalUnitNum(paramType,
					logicalUnitNum);
				station.addRecordNum(paramType, (Integer)
					(pppindex.getIREC()).elementAt(i));
				if(paramType.equalsIgnoreCase("PCPN"))
					station.setIsPCPN(true);
				else if(paramType.equalsIgnoreCase("PE"))
					station.setIsPE(true);
				else if(paramType.equalsIgnoreCase("RRS"))
					station.setIsRRS(true);
				else if(paramType.equalsIgnoreCase("TEMP"))
					station.setIsTEMP(true);

				stationList.put(
					(String)(pppindex.getID()).elementAt(i),
					(NWSRFS_Station) station);
			}
			else {
				station = (NWSRFS_Station)stationList.get(
					(String)(pppindex.getID()).elementAt(i));
				if(paramType.equalsIgnoreCase("PCPN") && 
					!station.getIsPCPN()) {
					station.addLogicalUnitNum("PCPN",
						logicalUnitNumPCPN);
					station.addRecordNum("PCPN", (Integer)
					(pppindex.getIREC()).elementAt(i));
					station.setIsPCPN(true);
				}
				else if(paramType.equalsIgnoreCase("PE") &&
					!station.getIsPE()) {
					station.addLogicalUnitNum("PE",
						logicalUnitNumPE);
					station.addRecordNum("PE", (Integer)
					(pppindex.getIREC()).elementAt(i));
					station.setIsPE(true);
				}
				else if(paramType.equalsIgnoreCase("RRS") &&
					!station.getIsRRS()) {
					station.addLogicalUnitNum("RRS",
						logicalUnitNumRRS);
					station.addRecordNum("RRS", (Integer)
					(pppindex.getIREC()).elementAt(i));
					station.setIsRRS(true);
				}
				else if(paramType.equalsIgnoreCase("TEMP") &&
					!station.getIsTEMP()) {
					station.addLogicalUnitNum("TEMP",
						logicalUnitNumTEMP);
					station.addRecordNum("TEMP", (Integer)
					(pppindex.getIREC()).elementAt(i));
					station.setIsTEMP(true);
				}
			}
		}
	}

	// return the proplist
	return stationList;
}

/**
Read a single time series matching a time series identifier.
@return a time series or null if the time series is not defined.
If no data records are available within the requested period, a call to
hasData() on the returned time series will return false.
@param tsident_string TSIdent string indentifying the time series. This String
must be of the form:
<pre>
	LOCATION.SOURCE.DATATYPE.TIMESTEP.SCENARIO.INPUT_TYPE~DIRECTORY
</pre>
If this is an NWSRFS TimeSeries implementation then the TSIDENT will have 
one of the following forms:
<pre>
	TSID."NWRFS".DATATYPE.TIMESTEP~NWSRFS_FS5Files
	TSID."NWRFS".DATATYPE.TIMESTEP.BOTH~NWSRFS_FS5Files
		-> Return all available data
	TSID."NWRFS".DATATYPE.TIMESTEP.OBS~NWSRFS_FS5Files
		-> Return only observed data
	TSID."NWRFS".DATATYPE.TIMESTEP.FUT~NWSRFS_FS5Files
		-> Return only future data
	TSID."NWRFS".DATATYPE.TIMESTEP.FUT~NWSRFS_ESPTraceEnsemble
		-> Avoid the above for now.  Use TSTool or use the
		-> NWSRFS_ESPTraceEnsemble.readTimeSeries() method instead
</pre>
If no scenario is specified it returns observed and future data.  Future data
mainly apply to MAP and MAT time series.
The DIRECTORY will describe where the input datafiles reside. If blank and
INPUT_TYPE is NWSRFS_FS5Files then APPS_DEFAULTS will be used to get the
directory where the binary database resides.  If blank and INPUT_TYPE is
NWSRFS_ESPTraceEnsemble then it will look in the current directory.

Some examples are as follows:
<pre>
   APP5M.NWSRFS.QINE.6Hour.OBS.NWSRFS_FS5Files  
   PRLI.NWSRFS.SQME.1Hour.NWSRFS_FS5File~/projects/ipco/ofs/files/ipco/fs5files 
</pre><p>
Prints various debugging messages at Status level 10.
@param requested_date1 DateTime for the start of the query (specify 
null to read the entire time series).  The time zone will be reset to "Z"
(currently no automatic conversion from other time zones to "Z").
@param requested_date2 DateTime for the end of the query (specify 
null to read the entire time series).  The time zone will be reset to "Z"
(currently no automatic conversion from other time zones to "Z").
@param req_units requested data units (specify null or blank string to 
return units from the database).
@param read_data Indicates whether data should be read (specify false to 
only read header information).
@exception if there is an error reading the time series.
*/
public TS readTimeSeries(String tsident_string, DateTime requested_date1,
DateTime requested_date2, String req_units, boolean read_data) 
throws Exception {
	String routine = "NWSRFS_DMI.readTimeSeries";
	int tsDTInterval = -1;
	TimeInterval timeInt = null;
	TS ts = null;

	// TODO SAM 2006-10-03
	// Allow other than Z-time to be requested - need to decide whether to
	// automatically shift or query in Z time only.

	// Check to make sure that if req_date1 or req_date2 are specified, they
	// have a time zone of "Z", which is what the database files use.  Use
	// a copy of what was passed in so that we do not change the calling
	// code.

	// Use a local copy of the DateTimes so that calling code is not
	// impacted.

	DateTime req_date1 = null;
	DateTime req_date2 = null;
	if ( requested_date1 != null ) {
		req_date1 = new DateTime ( requested_date1 );
		req_date1.setTimeZone("Z");
	}
	if ( requested_date2 != null ) {
		req_date2 = new DateTime ( requested_date2 );
		req_date2.setTimeZone("Z");
	}

//Message.printStatus(10,routine,"TSIdent String = "+tsident_string);

	// Get TSIdent parts to do the read.

	TSIdent tsident = new TSIdent(tsident_string);
	String dataLoc = tsident.getLocation();
	String dataSource = tsident.getSource();
	String dataType = tsident.getMainType();
	String subDataType = tsident.getSubType();
	String interval = tsident.getInterval();
	String dataScenario = tsident.getScenario();
	String inputType = tsident.getInputType();
	String inputDir = tsident.getInputName();
	// TODO SAM 2006-11-22
	// If this method only reads one time series, then why is the wildcard
	// even of interest?
	if(!interval.equalsIgnoreCase("*")) {
		timeInt = (TimeInterval.parseInterval(interval));
		tsDTInterval = timeInt.getMultiplier();
	}
	else {
		return null;
	}

	Message.printStatus(10, routine,"TSIdent: "
		+ "\n          location   = " + dataLoc
		+ "\n          source     = " + dataSource
		+ "\n          type       = " + dataType
		+ "\n          sub type   = " + subDataType
		+ "\n          interval   = " + interval
		+ "\n          scenario   = " + dataScenario
		+ "\n          input type = " + inputType
		+ "\n          input Dir  = " + inputDir + "\n");
	Message.printStatus(2, routine, "Requested start Date: " + req_date1 + "\n");
	Message.printStatus(2, routine, "Requested end Date: " + req_date2 + "\n");
	Message.printStatus(10, routine, "Units: " + req_units + "\n");

	if (inputType.equalsIgnoreCase("NWSRFS_ESPTraceEnsemble")) {
		NWSRFS_ESPTraceEnsemble espTE = readESPTraceEnsemble(
			tsident.getInputName(), read_data);
		// TODO (JTS - 2004-08-21)
		// ts never instantiated above -- gonna get null pointers
		// if this if() {} is ever entered.
		ts = new HourTS();
		ts.setIdentifier(tsident);
		ts.setInputName(inputDir);
		ts.allocateDataSpace();
		ts = new HourTS((HourTS)espTE.readTimeSeries(tsident_string,
			req_date1, req_date2, req_units, read_data));
	}
	else if (inputType.equalsIgnoreCase("NWSRFS_FS5Files") ||
		inputType.length() == 0) {
		// Read the requested time series from the binary FS5 Files
		// specified by the input directory...
		NWSRFS_TimeSeries tsObject = null;
		TSIterator tsi = null;
		if (	(dataScenario == null) ||
			dataScenario.equalsIgnoreCase("")) {
			// The requested time series identifier did not specify
			// a scenario so treat as both below (return observed
			// and future data).
			dataScenario = "both";
		}

		// Get the input directory to use for time series header
		// information.

		if(openedWithAppsDefaults()) {
			inputDir = "";
		}
		else if (inputDir != null && inputDir.length() > 0 &&
		    !inputDir.equalsIgnoreCase("Use Apps Defaults")) {
			if(!inputDir.equalsIgnoreCase(getFS5FilesLocation())) {
				// A set of FS5 Files is currently opened but
				// does not match the request.  This should not
				// normally be the case.
				throw new Exception("The time series \""+
				tsident_string + "\" does not reside in the"+
				" opened FS5Files binary database: "+
				getFS5FilesLocation());
			}
		}
		else {	// A valid FS5Files input directory has been
			// specified...
			inputDir = getFS5FilesLocation();
		}
		
		// The datatype sub-type indicates whether data are raw (from
		// the preprocessor database PPDB) or processed (from the
		// processed database PDB).  Read a NWSRFS_TimeSeries from the
		// appropriate database.  This will contain separate TS
		// instances for observed and future data.

		// TODO SAM 2006-11-22
		// Not sure why it is done this way, but try to simplify a lot
		// of the following code to clarify handling of observed and
		// future data.  Need to further clean up when there is time.

		if(subDataType.equalsIgnoreCase("PPDB")) {
			tsObject = readTimeSeriesPDB(dataLoc, dataType, 
				tsDTInterval, read_data);
			dataScenario = "obs";
		}
		else {
			tsObject = readTimeSeriesPRD(dataLoc, dataType, 
				tsDTInterval, read_data);
		}
		
		// TODO SAM 2006-11-22
		// Why not return null or throw an exception if nothing returned
		// above?  Why only check if data are NOT to be read?

		if ( !read_data ) {
			// Only time series header information were requested.
			if ( tsObject == null ) {
				// Have no data so return null...
				return null;
			}
			else {	// Have a time series so return the observed
				// time series with its header...
				return tsObject.getObservedTS();
				// TODO SAM 2006-11-22
				// The header should take into account the
				// future data also.
			}
		}
		else if ( tsObject == null) {
			// Want to return a time series with data, but no data
			// are available.  Populate a default object so that the
			// following logic can continue and return the header
			// information and missing data.
			tsObject = new NWSRFS_TimeSeries(dataLoc, dataType,
				tsDTInterval);
		}
		

		// So if here the time series data are to be read (not just
		// the header).

		// Observed and future TS from the FS5Files, to be processed
		// more below...
		TS observedTS = tsObject.getObservedTS();
		Message.printStatus(2, routine, "From DB observed start Date: " + observedTS.getDate1());
		Message.printStatus(2, routine, "From DB observed end Date: " + observedTS.getDate2());
		// Time series other than MAP have observed and future data in a linear array.
		// However the code called above will split the data into observed and future
		// time series so we need to reassemble the data.  If the data type is MAP,
		// then an additional read will be done below for future data.
		
		TS futureTS = tsObject.getFutureTS();
		
		if ( futureTS != null ) {
			Message.printStatus(2, routine, "After first read, DB future start Date: " + futureTS.getDate1());
			Message.printStatus(2, routine, "After first read, DB future end Date: " + futureTS.getDate2());
		}
		else {	Message.printStatus ( 2, routine, "No future data so no future dates.");
		}

		// KAT commented out 2006-10-3 
		//Message.printStatus ( 2, routine,
		//"After creating time series, date1 = ", tsObject.getDate1() +
		//" date2 = " + tsObject.getDate2() );
		
		// If this time series has data type of MAP TS and both
		// observed and future data are desired, then read the FMAP
		// datatype.
		// TODO SAM 2006-11-22
		// Why does NWSRFS_TimeSeries have observed and future time
		// series if we need to do two reads?

		if ( dataType.equalsIgnoreCase("MAP") 
		    && (dataScenario.equalsIgnoreCase("both") ||
			dataScenario.equalsIgnoreCase("fut")) ) {
			// Read the future MAP (FMAP).
			// TODO SAM 2006-12-12
			// FMAP time series are returned below as observed and
			// then set in the future time series.  This is
			// confusing and needs to be corrected.
			Message.printStatus( 2, routine,
			"Requested time series is MAP so read FMAP for " +
			dataLoc );
			try {
				NWSRFS_TimeSeries tsObject2 = readTimeSeriesPRD(dataLoc,"FMAP",tsDTInterval,true);
				if ( tsObject2 == null ) {
					Message.printStatus(2, routine,
					"No FMAP time series available for "+ dataLoc + " FMAP " + tsDTInterval );
				}
				else {	// Use the data...
				tsObject.setIPTFUT(tsObject2.getIPTREG());
				tsObject.setFutureTS(tsObject2.getObservedTS());
				futureTS = tsObject.getFutureTS();
				// For troubleshooting/understanding code...
				TS obsts2 = tsObject.getObservedTS();
				if ( obsts2 != null ) {
					Message.printStatus(2, routine,
					"After FMAP read, observed start Date: " + obsts2.getDate1());
					Message.printStatus(2, routine, "After FMAP read, observed end Date: " + obsts2.getDate2());
				}
				}
			}
			catch (Exception e) {
				Message.printWarning ( 2, routine, "Error reading FMAP (no future data will be processed): " );
				Message.printWarning ( 2, routine, e );
				exceptionCount++;
				futureTS = null;
			}
			if ( futureTS != null ) {
				Message.printStatus(2, routine,
				"After FMAP read, future start Date: " + futureTS.getDate1());
				Message.printStatus(2, routine,
				"After FMAP read, future end Date: " + futureTS.getDate2());
			}
			else {
				Message.printStatus ( 2, routine, "No future data so no future dates.");
			}
		}
		
		// Create an hourly time series to be returned and initialize
		// basic information...

		ts = new HourTS();
		// Set data type in TS object
		ts.setDataType(dataType);
		// TODO SAM 2006-11-22
		// Need to remove or use...
		//tsident.setInputType("NWSRFS_FS5Files");
		ts.setInputName("\""+getFS5FilesLocation()+":"+
			__dbFileNames[tsObject.getPrdIndex()]+"\"");
		// TODO SAM 2006-11-22
		// Need to remove or use...
		//tsident.setInputName(inputDir);
		// Set from the original request...
		ts.setIdentifier(tsident_string);
		// Get information from one of these time series...
		if ( observedTS != null ) {
			ts.setDataUnits ( observedTS.getDataUnits() );
			ts.setDataInterval( observedTS.getDataIntervalBase(),
				observedTS.getDataIntervalMult());
			ts.setDescription(observedTS.getDescription());
			ts.addToComments ( observedTS.getComments() );
		}
		else if ( futureTS != null ) {
			ts.setDataUnits ( futureTS.getDataUnits() );
			ts.setDataUnits ( futureTS.getDataUnits() );
			ts.setDataInterval( futureTS.getDataIntervalBase(),
				futureTS.getDataIntervalMult());
			ts.setDescription(futureTS.getDescription());
		}
		// Always append this if available...
		if ( futureTS != null ) {
			ts.addToComments ( futureTS.getComments() );
		}

		// The observed time series was needed for header information
		// above but don't need it anymore if it was not requested.  Set
		// it to null so that it is not processed below.
		// TODO SAM 2006-11-22
		// Need to get the header information for above, considering
		// observed and future, not just observed.
		
		if (	!dataScenario.equalsIgnoreCase("both") &&
			!dataScenario.equalsIgnoreCase("obs") ) {
			observedTS = null;
		}

		// Set the DateTimes for the time series, based on the available
		// data and requested DateTimes.  Set the original DateTimes in
		// the time series to that available in the FS5Files and the
		// normal DateTimes to what is requested (if specified).  The
		// longest period of observed and future data are used, when
		// both are read.

		DateTime d1 = null;	// For transfer
		DateTime d2 = null;
		if ( observedTS != null ) {
			// Transfer observed data into returned time series...
			d1 = observedTS.getDate1();
			if ( d1 != null ) {
				ts.setDate1Original(d1);
			}
			d2 = observedTS.getDate2();	
			if ( d2 != null ) {
				ts.setDate2Original(d2);
			}
		}
		if ( futureTS != null ) {
			// Transfer future data into returned time series...
			d1 = futureTS.getDate1();
			if ( (d1 != null) &&d1.lessThan(ts.getDate1Original())){
				ts.setDate1Original(d1);
			}
			d2 = futureTS.getDate2();
			if((d2 != null)&&d2.greaterThan(ts.getDate2Original())){
				ts.setDate2Original(d2);
			}
		}

		// Now have the original dates from the data.  Set the active
		// dates for memory allocation by defaulting to the original
		// dates overridden by the user request...
		if ( req_date1 != null ) {
			DateTime req_date1_nearest = new DateTime(
					TSGraph.getNearestDateTimeLessThanOrEqualTo(req_date1, observedTS));
			ts.setDate1 ( req_date1_nearest );
		}
		else {	ts.setDate1 ( ts.getDate1Original() );
		}
		if ( req_date2 != null ) {
			DateTime req_date2_nearest = new DateTime(
					TSGraph.getNearestDateTimeLessThanOrEqualTo(req_date2, observedTS));
			ts.setDate2 ( req_date2_nearest );
		}
		else {	ts.setDate2 ( ts.getDate2Original() );
		}

		// Allocate data space using the DateTimes...

		ts.allocateDataSpace();
		Message.printStatus(2, routine, "Returned TS start Date: " + ts.getDate1());
		Message.printStatus(2, routine, "Returned TS end Date: " + ts.getDate2());

		// Iterate through the observed time series and insert data
		// values into the time series to be returned.  For now iterate
		// through all the data (there won't be much) and allow data
		// outside the period to be ignored)...

		if ( (observedTS != null) && (observedTS.getDate1() != null) && 
				(observedTS.getDate2() != null) ) {
			tsi = observedTS.iterator();

			while (tsi.next() != null) {
				ts.setDataValue(tsi.getDate(),
					tsi.getDataValue());
			}
		}

		// Iterate through the future time series and insert data values
		// into the time series to be returned.  Start iterating using
		// the last date in the observed time series, if it is
		// available, because the observed data are more relevant.

		if ( (futureTS != null) && (futureTS.getDate1() != null) &&
				(futureTS.getDate2() != null) ) {
			d1 = observedTS.getDate2();
			if ( d1 == null ) {
				// Just iterate through all future data...
				tsi = tsObject.getFutureTS().iterator();
			}
			else {	// Iterate from the next date after observed
				// data, to the end of future data...
				d1.addInterval (
					observedTS.getDataIntervalBase(),
					observedTS.getDataIntervalMult() );
				tsi = tsObject.getFutureTS().iterator(
					d1, futureTS.getDate2());
			}

			while (tsi.next() != null) {
				ts.setDataValue(tsi.getDate(),
					tsi.getDataValue());
			}
		}
	}
	else {
		Message.printWarning(10, routine,
			"Incorrect TSIdent value "
			+ "for Source. It needs to be one of: "
			+ "NWSRFS_FS5Files or NWSRFS_ESPTraceEnsemble.");
	}
	
	// Check to see if the TS has data! If not through an error.
	if ( read_data && ts.getDataSize() <= 0 ) {
		throw new Exception("The requested time series \""+
		tsident_string + "\" had no data!");
	}
	
	// Convert Units to requested type
	if ( req_units != null && ts != null && ts.getDataSize() > 0) {
		TSUtil.convertUnits(ts,req_units);
	}
	
	// return the time series
	return ts;
}

/**
Return a Vector of TS objects given a time series identifier, optionally
containing wild cards.  It will pull
the data from the PRDINDEX, PRDPARM, and PRDTSn processed database binary files
to get the all of the times series for the given the parsed identifier.
@return a Vector of time series or null if no time series is defined.
If no data records are available within the requested period, a call to
hasData() on the returned time series will return false.
@param tsident_string TSIdent string indentifying the time series. This String
must be of the form:
<pre>
	LOCATION.SOURCE.DATATYPE.TIMESTEP.SCENARIO.INPUT_TYPE~DIRECTORY
</pre>
If the request is for an NWSRFS FS5Files time series, then the TSIDENT will
match the following:
<pre>
TSID.NWRFS.DATATYPE.TIMESTEP[.OBS|.FUT|.BOTH].NWSRFS_FS5Files[~FS5FilesPath]
</pre>
The scenario can be OBS, FUT, or blank, where OBS returns just the 
observed data, FUT will return the future data if any, and blank will
concatenate both observed and future data.
Also the INPUT_TYPE can be either NWSRFS_FS5Files or NWSRFS_ESPTraceEnsemble to 
delineate the type of NWSRFS data to read.  The DIRECTORY will describe where
the input data files reside.  If blank and INPUT_TYPE is NWSRFS_FS5Files then
APPS_DEFAULTS will be used to get the directory where the binary database
resides.  If blank and INPUT_TYPE is NWSRFS_ESPTraceEnsemble then it will look
in the current directory.

Some examples are as follows:
<pre>
	APP5M.NWSRFS.*.6Hour.OBS.NWSRFS_FS5Files  
	PRLI.NWSRFS.SQME.*Hour.NWSRFS_FS5File~/projects/ipco/ofs/files/ipco/fs5files  
</pre><p>
Prints various debugging messages at Status level 10.
@param req_date1 Optional date to specify the start of the query (specify 
null to read the entire time series).
@param req_date2 Optional date to specify the end of the query (specify 
null to read the entire time series).
@param req_units requested data units (specify null or blank string to 
return units from the database).
@param read_data Indicates whether data should be read (specify false to 
only read header information).
@exception if there is an error reading the time series.
*/
public Vector readTimeSeriesList(String tsident_string, DateTime req_date1,
DateTime req_date2, String req_units, boolean read_data) 
throws Exception
{	String routine = "NWSRFS_DMI.readTimeSeriesList";
	int i, j, k, tsDTInterval = -1;
	Vector tsList = new Vector();
	Vector tsIdentList = new Vector();
	TimeInterval timeInt = null;
	String parseDataType;
	
	// Get TSIdent info
	TSIdent tsident = new TSIdent(tsident_string);
	String dataLoc = tsident.getLocation();
	String dataSource = tsident.getSource();
	String dataType = tsident.getMainType();
	String subDataType = tsident.getSubType();
	String interval = tsident.getInterval();
	String dataScenario = tsident.getScenario();
	String inputType = tsident.getInputType();
	String inputDir = tsident.getInputName();
	if(!interval.equalsIgnoreCase("*")) {
		timeInt = (TimeInterval.parseInterval(interval));
		tsDTInterval = timeInt.getMultiplier();
	}
	else {
		tsDTInterval = -1;
	}

	Message.printStatus(10, routine,"TSIdent: "
		+ "\n          location   = " + dataLoc
		+ "\n          source     = " + dataSource
		+ "\n          type       = " + dataType
		+ "\n          sub type   = " + subDataType
		+ "\n          interval   = " + interval
		+ "\n          scenario   = " + dataScenario
		+ "\n          input type = " + inputType
		+ "\n          input Dir  = " + inputDir + "\n");
	Message.printStatus(10, routine, "Start Date: " + req_date1 + "\n");
	Message.printStatus(10, routine, "End Date: " + req_date2 + "\n");
	Message.printStatus(10, routine, "Units: " + req_units + "\n");

	TS ts = null;
	if (inputType.equalsIgnoreCase("NWSRFS_ESPTraceEnsemble")) {
		NWSRFS_ESPTraceEnsemble espTE = readESPTraceEnsemble(
			tsident.getInputName(), read_data);

		ts = new HourTS();
		ts.allocateDataSpace();
		ts = new HourTS((HourTS)espTE.readTimeSeries(tsident_string,
			req_date1, req_date2, req_units, read_data));
		tsList.addElement(ts);
	}
	else if (inputType.equalsIgnoreCase("NWSRFS_FS5Files") ||
		inputType.length() == 0) {

		// This is the NWSRFS TimeSeries implementation.
		if (dataScenario == null || dataScenario.equalsIgnoreCase("")) {
			dataScenario = "Both";
		}
		
		// Get the Vector of TSIdentList returned from the database
		if(subDataType.equalsIgnoreCase("PPDB")) {
			tsIdentList=readTSIdentListPDB(dataLoc,dataType,interval);
		}
		else {
			if(dataType.equals("*")) {
//				readDataTypeList();
				Vector v = DataType.getDataTypesData();
				int size = 0;
				if ( v != null ) {
					size = v.size();
				}
				DataType dt = null;
				Vector parseIdentList = null;
				for (i = 0; i < size; i++ ) {
					parseIdentList = new Vector();
					dt = (DataType)v.elementAt(i);
					parseDataType = dt.getAbbreviation();

					// If interval is * we need to loop
					// through some default inetervals
					// like 1Hour,3Hour, 6Hour,12Hour,
					// 18Hour, and 24Hour
					if(interval.equalsIgnoreCase("*")) {
						int intervalArray[] = new int[6];
						intervalArray[0] = 1;
						intervalArray[1] = 3;
						intervalArray[2] = 6;
						intervalArray[3] = 12;
						intervalArray[4] = 18;
						intervalArray[5] = 24;
						for(j = 0; j < 6; j++) {
							parseIdentList = 
							readTSIdentListPRD(
							parseDataType,
							intervalArray[j],
							dataScenario);
					
							for(k = 0; 
							k < parseIdentList.size(); 
							k++) {
								tsIdentList.addElement(
								parseIdentList.elementAt(k));
							}
						}
					}
					else {
						parseIdentList = 
						readTSIdentListPRD(
						parseDataType, tsDTInterval,
						dataScenario);
					
						for(k = 0; 
						k < parseIdentList.size(); k++) {
							tsIdentList.addElement(
							parseIdentList.elementAt(k));
						}
					}
				}
			}
			else {
				tsIdentList=readTSIdentListPRD(dataType,
					tsDTInterval,dataScenario);
			}
		}
		
		// Loop through the Vector of TSIdents returned by TSIdentList
		// to match the tsids coming from the TSIdent supplied above.
		// Can have wildcard characters in the tsid such as APP* or *5M
		// or AP*5M or just a *.
		int size = 0;
		if ( tsIdentList != null ) {
			size = tsIdentList.size();
		}
		Message.printStatus ( 10, routine,
			"Read " + size + " time series identifiers." );
		boolean transfer_all = false;	// Should all time series be
						// transferred?  Allows wildcard
						// check to be skipped.
		String parseString = null;	// Java-style wildcard string
		if ( dataLoc.equals("*") ) {
			transfer_all = true;
		}
		else {	
			parseString =StringUtil.replaceString(dataLoc,"*",".*");
		}
		for ( i = 0; i < size; i++ ) {			
			// The try/catch here so that an error on one time
			// series does not error out the whole read process.
			// For now assume it is not needed because the header
			// was read successfully.
			try {
				if (	transfer_all ||
					StringUtil.matchesIgnoreCase(
					((TSIdent)tsIdentList.elementAt(i)).
					getLocation(),parseString)) {
					if(((TSIdent)tsIdentList.elementAt(i)).
						getIdentifier() == null) {
						Message.printWarning (2,
						routine, "Unable to read time "+
						"series - the TS Ident String "+
						"is null." );
						continue;
					}
					
					ts = readTimeSeries(
						((TSIdent)
						tsIdentList.elementAt(i)).
						getIdentifier(),
						req_date1, req_date2, req_units,
						read_data);
					if ( ts == null ) {
						Message.printWarning (20,
						routine, "Unable to read time "+
						"series for \"" +
						((TSIdent)tsIdentList.
						elementAt(i)).
						getIdentifier() +
						"\" - not adding to " +
						"returned list." );
					}
					else {	
						tsList.addElement(ts);
					}
				}
			} catch(Exception e) {
				Message.printWarning(10,routine,e);
			}
		}
	}
	
	return tsList;
}

/**
Return a Vector of time series identifiers (TSIdent instances) given a data type
and interval.  Data are read from the PDBINDEX preprocessed database binary 
files.
@param dataType the data type identifier in which to pull data I.E. "STG"
@param interval Data interval in hours, or <= zero to retrieve all time series
identifiers for the data type.
@return a Vector of time series identifiers (TSIdent instances) given a data
type and interval.
*/
public Vector readTSIdentListPDB(String tsID, String dataType, String interval)
throws Exception
{
	int pdbAddDT;
	String timeInt = null;
	NWSRFS_PDBINDEX pdbIndex = getPDBIndex();
	String tsIdentString = null;
	Vector tsidVector = new Vector();
	
	// Check to see if the pdbindex file exists! If not get the index.
	if(pdbIndex == null) { 
		setPDBIndex(readPDBINDEX());
	}
	
	// Loop through the pdbIndex to get the station ids which have data
	// of the "dataType" type! Build the tsIdent String here and put into
	// the Vector for return to calling routine.
	for(int i = 0; i < (pdbIndex.getSTAID()).size(); i++) {
		// Check to see if the TS ID is set!
		if(!tsID.equalsIgnoreCase("*") && !pdbIndex.getSTAID(i).equalsIgnoreCase(tsID)) {
			continue;
		}
		
		tsIdentString = null;
		// Checking to see if we are requesting 24 hour precip! This is
		// special since it is not included in the Addtional data types 
		// array we check below. It is generally assumed that 24 hour
		// precip and/or 24 Temp WILL be defined.
		if(dataType.equalsIgnoreCase("PP24") &&
		pdbIndex.getSTAID(i).indexOf(".") < 0 &&
		!pdbIndex.getSTAID(i).equalsIgnoreCase("Deleted")) {
			if(pdbIndex.getPCPPTR(i) > 0) {
				tsIdentString = pdbIndex.getSTAID(i)+".NWSRFS."+
					"PP24-PPDB.24Hour"+
					"~NWSRFS_FS5Files~" + getFS5FilesLocation();
					
				// Add the tsIdentString to the TSIDent Vector
				tsidVector.addElement(new TSIdent(tsIdentString));
			}
		}
		else if(dataType.equalsIgnoreCase("TA24") && // 24 hour Temp.
		pdbIndex.getSTAID(i).indexOf(".") < 0 &&
		!pdbIndex.getSTAID(i).equalsIgnoreCase("Deleted")) {
			if(pdbIndex.getTMPPTR(i) > 0) {
				tsIdentString = pdbIndex.getSTAID(i)+".NWSRFS."+
					"TA24-PPDB.24Hour"+
					"~NWSRFS_FS5Files~" + getFS5FilesLocation();
					
				// Add the tsIdentString to the TSIDent Vector
				tsidVector.addElement(new TSIdent(tsIdentString));
			}
		}
		else if(dataType.equalsIgnoreCase("*") && // Other types wild card
		pdbIndex.getSTAID(i).indexOf(".") < 0 &&
		!pdbIndex.getSTAID(i).equalsIgnoreCase("Deleted")) {
			// Add PP24 since it also is a data type the wildcard
			// accepts
			if(pdbIndex.getPCPPTR(i) > 0 && 
				(interval.equalsIgnoreCase("24Hour")||
				interval.equalsIgnoreCase("*"))) {
				tsIdentString = pdbIndex.getSTAID(i)+".NWSRFS."+
					"PP24-PPDB.24Hour"+
					"~NWSRFS_FS5Files~" + getFS5FilesLocation();
					
				// Add the tsIdentString to the TSIDent Vector
				tsidVector.addElement(new TSIdent(tsIdentString));
			}
			
			// Add TA24
			if(pdbIndex.getTMPPTR(i) > 0 && 
				(interval.equalsIgnoreCase("24Hour")||
				interval.equalsIgnoreCase("*"))) {
				tsIdentString = pdbIndex.getSTAID(i)+".NWSRFS."+
					"TA24-PPDB.24Hour"+
					"~NWSRFS_FS5Files~" + getFS5FilesLocation();
					
				// Add the tsIdentString to the TSIDent Vector
				tsidVector.addElement(new TSIdent(tsIdentString));
			}
			
			// Everything else!
			pdbAddDT = pdbIndex.getNADDTP(i);
			
			// If the number of additional data types is <= 0
			// then no more data types to check so continue!
			// Now addDTIndex is the true Vector index holding
			// the additional data types!
			if(pdbAddDT > 0) {
				for(int j = 0; j < pdbAddDT; j++) {
					// Set the tsIdentString to null for check!
					tsIdentString = null;

					// Now check to see if we can get the 
					// interval from the data type!
					if(pdbIndex.getADDDTP(i,j).
						indexOf("24") >= 0) {
						timeInt = "24Hour";
					}
					else if(pdbIndex.getADDDTP(i,j).
						indexOf("6") >= 0) {
						timeInt = "6Hour";
					}
					else if(pdbIndex.getADDDTP(i,j).
						indexOf("3") >= 0) {
						timeInt = "3Hour";
					}
					else if(pdbIndex.getADDDTP(i,j).
						indexOf("1") >= 0) {
						timeInt = "1Hour";
					} 
//					else if(getPDBIndex().
//						getIsRRSType(pdbIndex.
//						getADDDTP(i,j))) {
//							timeInt=interval;
//					}
//					else if(interval.
//						equalsIgnoreCase("*")) {
//						timeInt = "*Hour";
//					} 
					else {
						// Use the passed in value
						timeInt = interval;
					}
					
					// Check timeInt the time interval
					if(interval.equalsIgnoreCase("*")) {
						// Now loop through most common
						// time intervals for default of
						// *.
						int intervalArray[] = new int[6];
						intervalArray[0] = 1;
						intervalArray[1] = 3;
						intervalArray[2] = 6;
						intervalArray[3] = 12;
						intervalArray[4] = 18;
						intervalArray[5] = 24;
						for(int k=0; k<6;k++) {
							tsIdentString = pdbIndex.
							getSTAID(i)+".NWSRFS."+
							pdbIndex.
							getADDDTP(i,j)+
							"-PPDB."+
							intervalArray[k]+"Hour"+
							"~NWSRFS_FS5Files~" + getFS5FilesLocation();
						
							// Add the tsIdentString to the 
							// TSIDent Vector
							if(tsIdentString != null) {
								tsidVector.
								addElement(
								new TSIdent(tsIdentString));
							}
						}
					}
					else if(interval.
					equalsIgnoreCase(timeInt)) {
						tsIdentString = pdbIndex.
						getSTAID(i)+".NWSRFS."+pdbIndex.
						getADDDTP(i,j)+"-PPDB."+
						timeInt+"~NWSRFS_FS5Files~" + getFS5FilesLocation();
						
						// Add the tsIdentString to the 
						// TSIDent Vector
						if(tsIdentString != null) {
							tsidVector.
							addElement(
							new TSIdent(tsIdentString));
						}
					}
				}
			}
		}
		else if(pdbIndex.getSTAID(i).indexOf(".") < 0 &&
			!pdbIndex.getSTAID(i).equalsIgnoreCase("Deleted")) {
			// Other data types including RRS types
			pdbAddDT = pdbIndex.getNADDTP(i);
			
			// If the number of additional data types is <= 0
			// then no more data types to check so continue!
			// Now addDTIndex is the true Vector index holding
			// the additional data types!
			if(pdbAddDT > 0) {
				for(int j = 0; j < pdbAddDT; j++) {
					// Set the tsIdentString to null for check!
					tsIdentString = null;

					// Check to see if the passed in dataType
					// is in the list
					if(!pdbIndex.getADDDTP(i,j).
						equalsIgnoreCase(dataType)) {
						continue;
					}
					
					// Now check to see if we can get the 
					// interval from the data type!
					if(pdbIndex.getADDDTP(i,j).
						indexOf("24") >= 0) {
						timeInt = "24Hour";
					}
					else if(pdbIndex.getADDDTP(i,j).
						indexOf("6") >= 0) {
						timeInt = "6Hour";
					}
					else if(pdbIndex.getADDDTP(i,j).
						indexOf("3") >= 0) {
						timeInt = "3Hour";
					}
					else if(pdbIndex.getADDDTP(i,j).
						indexOf("1") >= 0) {
						timeInt = "1Hour";
					} 
//					else if(pdbIndex.getIsRRSType(pdbIndex.
//						getADDDTP(i,j))) {
//							timeInt="*";
//					}
					else {
						// Use the passed in value
						timeInt = interval;
					}
					
					// Check timeInt the time interval
					if(interval.equalsIgnoreCase("*")) {
						// Now loop through most common
						// time intervals for default of
						// *.
						int intervalArray[] = new int[6];
						intervalArray[0] = 1;
						intervalArray[1] = 3;
						intervalArray[2] = 6;
						intervalArray[3] = 12;
						intervalArray[4] = 18;
						intervalArray[5] = 24;
						for(int k=0; k<6;k++) {
							tsIdentString = pdbIndex.
							getSTAID(i)+".NWSRFS."+
							pdbIndex.
							getADDDTP(i,j)+
							"-PPDB."+
							intervalArray[k]+"Hour"+
							"~NWSRFS_FS5Files~" + getFS5FilesLocation();
						
							// Add the tsIdentString to the 
							// TSIDent Vector
							if(tsIdentString != null) {
								tsidVector.
								addElement(
								new TSIdent(tsIdentString));
							}
						}
					}
					else if(interval.
					equalsIgnoreCase(timeInt)) {
						tsIdentString = pdbIndex.
						getSTAID(i)+".NWSRFS."+pdbIndex.
						getADDDTP(i,j)+"-PPDB."+
						timeInt+"~NWSRFS_FS5Files~" + getFS5FilesLocation();
						
						// Add the tsIdentString to the 
						// TSIDent Vector
						if(tsIdentString != null) {
							tsidVector.
							addElement(
							new TSIdent(tsIdentString));
						}
					}
				}
			}
		}
	}
	
	// Return the Vector of tsIdent Strings
	return tsidVector;
}
/**
Return a Vector of time series identifiers (TSIdent instances) given a data type
and interval.  Data are read from the PRDINDEX, PRDPARM, and PRDTSn processed
database binary files.
@param dataType the data type identifier in which to pull data I.E. "STG"
@param interval Data interval in hours, or <= zero to retrieve all time series
identifiers for the data type.
@param dataScenario determines whether or not we pull observation, future, or
both data from the database. This value is appended to the TSIdent String.
@return a Vector of time series identifiers (TSIdent instances) given a data
type and interval.
*/
public Vector readTSIdentListPRD(String dataType, int interval,
				String dataScenario) 
throws Exception
{	String routine = "NWSRFS_DMI.readTSIdentListPRD";
	int prdIndex = 1;
	
	// Check if the the database binary file is open as a
	// Random Access object
	if (!checkRandomAccessFileOpen(__PRDPARM, true)) {
		throw new Exception("Can not open the " 
			+ __dbFileNames[__PRDPARM] + " binary database file");
	}

	// Now read the Time Series parameter file to get the parameters
	// for the Time series in the PRDTSn binary file.
	// Read the first record first (240 bytes)
	EndianDataInputStream EDISParm = read(__NWSRFS_DBFiles[__PRDPARM], 0, 
		240);
	char[] charValue = null;
	int i = -1;
	int unitNumber = -1;
	String parseChar = null;
	while (true) {
		// Read until EOF or break.
		try {
			// Read in subsequent record (72 bytes each)
			// Read the record 
			EDISParm = read(__NWSRFS_DBFiles[__PRDPARM],0,72);

			// Field 1 - [type field name here]
			charValue = new char[4];
			for (i = 0; i < 4; i++) {
				charValue[i] = EDISParm.readEndianChar1();
			}
				
			parseChar = new String(charValue).trim();

			if (parseChar.length() == 0 ||
				!parseChar.equalsIgnoreCase(dataType)) {
				continue;
			}

			// Field 2 - [type field name here]
			unitNumber = EDISParm.readEndianInt();

			break;
		}
		catch (EOFException EOFe) {
			exceptionCount++;
		// TODO (JTS - 2004-08-21)
		// expensive!
			break;
		}
	}

	// Close the EndianDataInputStream
	EDISParm.close();

	// If the__isOpen[fileIndex] is false open the 
	// database binary file as a
	// Random Access object
	// Create the tsDataFile String. Need to loop 
	// through all 5 of the
	// TS files until find right unit number. 
	// If LUNIT from the PRDTSn
	// Equals the IUNIT value from the PRDPARM file 
	// then have the right
	// Time Series file to read the TS into.
	EndianDataInputStream EDISData = null;
	//String prdtsDataFile = null;
	for (i = 0; i < 5; i++) {
		//prdIndex = (int)new Integer((String)new String().valueOf(__PRDTS1 + i)).intValue();
		/* TODO SAM 2008-04-07 What are these used for?
		if (__useFS5Files) {
			prdtsDataFile = __fs5FilesLocation + __dbFileNames[prdIndex] + i; 
		}
		else {
			prdtsDataFile = __dbFileNames[prdIndex]+i; 
		}
		*/

		// Check if the the database binary file is open as a Random Access object
		if (!checkRandomAccessFileOpen(prdIndex, true)) {
			throw new Exception("Cannot open the " + __dbFileNames[prdIndex] + " binary database file");
		}

		EDISData = read(__NWSRFS_DBFiles[prdIndex],0,4,5);

		if (unitNumber == EDISData.readEndianInt()) {
			break;
		}
		else {
			EDISData.close();
		}
	}

	// Check if the the database binary file is open as a
	// Random Access object
	if (!checkRandomAccessFileOpen(__PRDINDEX, true)) {
		throw new Exception("Can not open the " 
			+ __dbFileNames[__PRDINDEX] + " binary database file");
	}

	// Now read the Time Series index file to get the Record number
	// for the Time series in the PRDTSn binary file.
	byte dataInterval = 0;
	EndianDataInputStream EDISIndex = null;
	int recordNum = -1;
	String dataIntString = null;
	String tsid = null;
	String tsIdentString = null;
	Vector tsidVector = new Vector();
	while (true) {
		// Read until EOF or break.
		try {
			// Read the record 
			EDISIndex = read(__NWSRFS_DBFiles[__PRDINDEX],0,16);

			// Field 1 - [type field name here]
			charValue = new char[8]; 
			for (i = 0; i < 8; i++) {
				charValue[i] = EDISIndex.readEndianChar1();
			}
				
			parseChar = new String(charValue).trim();
		
			if (parseChar.length() == 0) {
				continue;
			}
			else if(parseChar.indexOf('.') >= 0 
				|| parseChar.indexOf('~') >= 0) {
				Message.printWarning(2,routine,
				"Time Series Identifier: "+parseChar+
				" contains an illegal character. "+
				"The Time Series will be skipped.");
				continue;
			}
			else {
				tsid = parseChar;
			}

			// Field 2 - [type field name here]
			charValue = new char[4];
			for (i = 0; i < 4; i++) {
				charValue[i] = EDISIndex.readEndianChar1();
			}
		
			parseChar = new String(charValue).trim();
	
			if (parseChar.length() == 0 
				|| !parseChar.equalsIgnoreCase(dataType)) {
				continue;
			}

			// Field 3 - [type field name here]
			// get the recordNum and break
			recordNum = EDISIndex.readEndianInt();

			// Now go to the record which has the data interval
			rewind(__NWSRFS_DBFiles[prdIndex]);

			// Read the header record. Remember that recordNum 
			// is the record number
			// read from the PRDINDEX file. First must get the 
			// length of the header
			// prior to the full read.
			EDISData = read(__NWSRFS_DBFiles[prdIndex],
				recordNum - 1, 64, 6);

			// Field 1 - [type field name here]
			EDISData.readByte();
	
			// Field 2 - [type field name here]
			dataInterval = (byte)EDISData.readByte();

			// Check to see if the data interval equals the value
			// passed in. If so add to the tsID Vector otherwise
			// continue. If the passed in interval is 0 or -1 then
			// assume to pickup all tsIDs.
			if ((int)dataInterval == interval || interval <= 0) {
				dataIntString = "" + dataInterval + "Hour";
			} 
			else {
				continue;
			}

			// Now create the TSIdent String
			if(dataScenario.equalsIgnoreCase("both")) {
				tsIdentString=tsid + ".NWSRFS." + dataType + "."
				+ dataIntString +"~NWSRFS_FS5Files~" + getFS5FilesLocation();
			}
			else {
				tsIdentString=tsid + ".NWSRFS." + dataType + "."
				+ dataIntString + "."
				+dataScenario+"~NWSRFS_FS5Files~" + getFS5FilesLocation();
			}
			// Now fill the Vector with TSIdent objects
			tsidVector.add(new TSIdent(tsIdentString));
			EDISData.close();
			EDISIndex.close();
		}
		catch (EOFException EOFe) {
			exceptionCount++;
			break;
		}
	}
	return tsidVector;
}

/**
Reads a time series object from the binary process database.
@param tsID this is a String object that holds the TimeSeries Identifier
for the TimeSeries object. 
@param tsDT this is the String value of the TimeSeries data type. It is
necessary that the data type be supplied to get a unique set of Time Series
from the data files.
@param tsDTInterval this is the int value of the TimeSeries data time interval. 
It is necessary that the data time interval be supplied to get a unique set of 
Time Series from the data files.
@param readData this determines whether or not to actually read the data or
just determine if data exists!
@return an NWSRFS_TimeSeries object which holds all of the information and data
from the PDBRRS and PDBLYn binary files.
@throws Exception if there is an error reading from the database.
@throws NullPointerException if the time series identifier is null.
*/
private NWSRFS_TimeSeries readTimeSeriesPDB(
String tsID, String tsDT, int tsDTInterval, boolean readData) 
throws Exception
{
	int i=0;
	String tsIdentKey, tsident_string;
	NWSRFS_TimeSeries tsFile = null;
	NWSRFS_Station station = null;
	HourTS ITS = new HourTS();
	DateTime dtTemp, dtTempStart, dtTempEnd;
	
	// Check to see if the pdbindex file exists! If not return empty list.
	if(getPDBIndex() == null) { 
		setPDBIndex(readPDBINDEX());
	}
	
	if (tsID == null) {
		throw new Exception("The Time Series identifier argument is empty.");
	}
	else if(tsDT == null) {
		throw new Exception("The Time Series Data Type argument is empty.");
	}
	else if(tsDTInterval < 0) {
		throw new Exception("The Time Series Data Time Interval argument is empty.");
	}

	// Set the TSIdent String
	tsIdentKey = tsID+"."+tsDT+"."+tsDTInterval;

	// If we have already looked at this time series just return it and 
	// do not retreive it again!
	if(__tsHashtable.containsKey(tsIdentKey) && __cacheTS) {
		tsFile = (NWSRFS_TimeSeries)__tsHashtable.get(tsIdentKey);
		if(tsFile.getIsDataFilled()) {
			return tsFile;
		}
	}
	
	// Now create and then populate the tsFile object
	tsFile = new NWSRFS_TimeSeries(tsID, tsDT, tsDTInterval);

	// Check the data type to see if it is a RRS data type or daily data.
	// Then populate the pdbFile object
	if(!NWSRFS_PDBINDEX.getIsRRSType(tsDT)) {
		//NWSRFS_PDBDLY pdbFile = readPDBDLY(tsID, tsDT, tsDTInterval, readData);
			
		// Not implemented yet return a null
		return null;
	}
	else {
		NWSRFS_PDBRRS pdbFile = readPDBRRS(tsID, tsDT, tsDTInterval, 
			readData);

		// Set identifier string
		tsident_string = tsID+".NWSRFS."+tsDT+"-PPDB.";
		
		// Set the TS object Identifier
		if(tsDTInterval == 0) {
			tsident_string += "*~NWSRFS_FS5Files~" + getFS5FilesLocation();
			ITS.setIdentifier(tsident_string);
		}
		else {
			tsident_string += tsDTInterval+"Hour~NWSRFS_FS5Files~" + getFS5FilesLocation();
			ITS.setIdentifier(tsident_string);
		}
		
		// Check to see if we have data!
		// Check to see if we were to just check to see if we
		// have data without actually reading all of the data 
		// into structures!
		if (!readData && (pdbFile == null || 
			pdbFile.getOBSTIME().size() == 0 ||
			pdbFile.getDATAVAL().size() == 0)) {
//Message.printStatus(1,routine,"I am in readTimeSeriesPDB: !read_data and pdbFile == null!");
//Message.printStatus(1,routine,"        readTimeSeriesPDB: tsDTInterval = "+tsDTInterval);
//Message.printStatus(1,routine,"        readTimeSeriesPDB: tsident_string = "+tsident_string);
			return null;
		}
		else if (readData && (pdbFile == null || 
			pdbFile.getOBSTIME().size() == 0 ||
			pdbFile.getDATAVAL().size() == 0)) {
			throw new Exception("The Time Series "+tsID+".NWSRFS."+
				tsDT+" is empty.");
		}
		else {
			// Get station info
			station = readStation(tsID, true);
			
			// Start filling TS information
			ITS.setDescription(station.getDescription());
			ITS.addToComments((String)tsFile.getTSID());
			ITS.addToComments(
				(String)tsFile.getTSDataType());
			
			ITS.setDataInterval(TimeInterval.HOUR, 
				tsDTInterval);

			if(tsDT.equalsIgnoreCase("AESC")) {
				ITS.setDataUnits("PCTD");
				ITS.setDataUnitsOriginal("PCTD");
			}
			else if(tsDT.equalsIgnoreCase("DQIN")) {
				ITS.setDataUnits("CFS");
				ITS.setDataUnitsOriginal("CFS");
			}
			else if(tsDT.equalsIgnoreCase("DQME")) {
				ITS.setDataUnits("CFSD");
				ITS.setDataUnitsOriginal("CFSD");
			}
			else if(tsDT.equalsIgnoreCase("FBEL")) {
				ITS.setDataUnits("FT");
				ITS.setDataUnitsOriginal("FT");
			}
			else if(tsDT.equalsIgnoreCase("FGDP")) {
				ITS.setDataUnits("IN");
				ITS.setDataUnitsOriginal("IN");
			}
			else if(tsDT.equalsIgnoreCase("GATE")) {
				ITS.setDataUnits("FT");
				ITS.setDataUnitsOriginal("FT");
			}
			else if(tsDT.equalsIgnoreCase("GTCS")) {
				ITS.setDataUnits("INT");
				ITS.setDataUnitsOriginal("INT");
			}
			else if(tsDT.equalsIgnoreCase("ICET")) {
				ITS.setDataUnits("IN");
				ITS.setDataUnitsOriginal("IN");
			}
			else if(tsDT.equalsIgnoreCase("LAKH")) {
				ITS.setDataUnits("FT");
				ITS.setDataUnitsOriginal("FT");
			}
			else if(tsDT.equalsIgnoreCase("LELV")) {
				ITS.setDataUnits("FT");
				ITS.setDataUnitsOriginal("FT");
			}
			else if(tsDT.equalsIgnoreCase("NFBD")) {
				ITS.setDataUnits("INT");
				ITS.setDataUnitsOriginal("INT");
			}
			else if(tsDT.equalsIgnoreCase("PCFD")) {
				ITS.setDataUnits("PCTD");
				ITS.setDataUnitsOriginal("PCTD");
			}
			else if(tsDT.equalsIgnoreCase("PELV")) {
				ITS.setDataUnits("FT");
				ITS.setDataUnitsOriginal("FT");
			}
			else if(tsDT.equalsIgnoreCase("QIN")) {
				ITS.setDataUnits("CFS");
				ITS.setDataUnitsOriginal("CFS");
			}
			else if(tsDT.equalsIgnoreCase("QME")) {
				ITS.setDataUnits("CFSD");
				ITS.setDataUnitsOriginal("CFSD");
			}
			else if(tsDT.equalsIgnoreCase("RQGM")) {
				ITS.setDataUnits("CFSD");
				ITS.setDataUnitsOriginal("CFSD");
			}
			else if(tsDT.equalsIgnoreCase("RQIM")) {
				ITS.setDataUnits("CFSD");
				ITS.setDataUnitsOriginal("CFSD");
			}
			else if(tsDT.equalsIgnoreCase("RQIN")) {
				ITS.setDataUnits("CFS");
				ITS.setDataUnitsOriginal("CFS");
			}
			else if(tsDT.equalsIgnoreCase("RQME")) {
				ITS.setDataUnits("CFSD");
				ITS.setDataUnitsOriginal("CFSD");
			}
			else if(tsDT.equalsIgnoreCase("RQOT")) {
				ITS.setDataUnits("CFS");
				ITS.setDataUnitsOriginal("CFS");
			}
			else if(tsDT.equalsIgnoreCase("RQSW")) {
				ITS.setDataUnits("CFS");
				ITS.setDataUnitsOriginal("CFS");
			}
			else if(tsDT.equalsIgnoreCase("RSTO")) {
				ITS.setDataUnits("CFSD");
				ITS.setDataUnitsOriginal("CFSD");
			}
			else if(tsDT.equalsIgnoreCase("SNOG")) {
				ITS.setDataUnits("IN");
				ITS.setDataUnitsOriginal("IN");
			}
			else if(tsDT.equalsIgnoreCase("SNWE")) {
				ITS.setDataUnits("IN");
				ITS.setDataUnitsOriginal("IN");
			}
			else if(tsDT.equalsIgnoreCase("STG")) {
				ITS.setDataUnits("FT");
				ITS.setDataUnitsOriginal("FT");
			}
			else if(tsDT.equalsIgnoreCase("TID")) {
				ITS.setDataUnits("FT");
				ITS.setDataUnitsOriginal("FT");
			}
			else if(tsDT.equalsIgnoreCase("TWEL")) {
				ITS.setDataUnits("FT");
				ITS.setDataUnitsOriginal("FT");
			}
			else if(tsDT.equalsIgnoreCase("TWSW")) {
				ITS.setDataUnits("FT");
				ITS.setDataUnitsOriginal("FT");
			}
			else if(tsDT.equalsIgnoreCase("ZELV")) {
				ITS.setDataUnits("FT");
				ITS.setDataUnitsOriginal("FT");
			}
			
			// Now fill the NWSRFS_Timeseries object with values from the
			// pdbFile object
			tsFile.setNAMERF(pdbFile.getUSER());
			tsFile.setMINDAY(pdbFile.getMINDAY());
			if(tsDTInterval > 0) {
				tsFile.setMINDT(tsDTInterval); 
			}
		
			if(pdbFile.getNVALS() > 2) {
				tsFile.setIPROC(3);
			}
			else {
				tsFile.setIPROC(1);
			}

			tsFile.setIFDAT(0); 
			tsFile.setTSID(pdbFile.getSTAID());
			tsFile.setTSDataType(tsDT);
		
			if(tsDTInterval > 0) {
				tsFile.setIDTINT((byte)tsDTInterval); 
			}
			else if(pdbFile.getOBSTIME().size() > 1){
				tsDTInterval = (pdbFile.getOBSTIME(1)-
						pdbFile.getOBSTIME(0))/100;
					tsFile.setIDTINT((byte)tsDTInterval); 
//Message.printStatus(10,routine,"pdbFile.getOBSTIME(0) = "+pdbFile.getOBSTIME(0));
//Message.printStatus(10,routine,"pdbFile.getDATAVAL(0) = "+pdbFile.getDATAVAL(0));
//Message.printStatus(10,routine,"pdbFile.getOBSTIME(1) = "+pdbFile.getOBSTIME(1));
//Message.printStatus(10,routine,"pdbFile.getDATAVAL(1) = "+pdbFile.getDATAVAL(1));
//Message.printStatus(10,routine,"pdbFile.getOBSTIME(2) = "+pdbFile.getOBSTIME(2));
//Message.printStatus(10,routine,"pdbFile.getDATAVAL(2) = "+pdbFile.getDATAVAL(2));
			}
			else {
				tsFile.setIDTINT((byte)1);
				tsDTInterval = 1;
			}
		
			// getOBSTIME returns julian minutes not julian hours
			// needed by the JULBEG variable so take last julian hour.
			// Now NWSRFS Julian minutes are: "JulianHour"Min
			// E.g., 92585600 is a Julian min where the Julian hour is
			// 925856. The last two digits are the minutes of the hour!
			// So to get Julian hour just divide by 100!!
			tsFile.setJULBEG(pdbFile.getOBSTIME(0)/100);
		
			// Set values in the Observed TS object
			dtTempStart = NWSRFS_Util.getDateFromJulianHour1900(
				tsFile.getJULBEG());
			dtTempStart.setTimeZone("Z");
			ITS.setDate1(dtTempStart);
			ITS.setDate1Original(dtTempStart);
		
			dtTempEnd = NWSRFS_Util.getDateFromJulianHour1900(
				pdbFile.getLSTHR());
			dtTempEnd.setTimeZone("Z");
//Message.printStatus(10,routine,"tsDTInterval = "+tsDTInterval);
//Message.printStatus(10,routine,"tsFile.getIDTINT() = "+tsFile.getIDTINT());
//Message.printStatus(10,routine,"pdbFile.getNUMOBS() = "+pdbFile.getNUMOBS());
//Message.printStatus(10,routine,"tsFile.getJULBEG() = "+tsFile.getJULBEG());
//Message.printStatus(10,routine,"dtTempStart = "+dtTempStart.toString());
//Message.printStatus(10,routine,"pdbFile.getLSTHR() = "+pdbFile.getLSTHR());
//Message.printStatus(10,routine,"dtTempEnd = "+dtTempEnd.toString());
			ITS.setDate2(dtTempEnd);
			ITS.setDate2Original(dtTempEnd);
		
			ITS.addToGenesis("Read time series from "+
				"the preprocessor database for "+
				(String)tsFile.getTSID()+
				" from "+dtTempStart.toString()+" to "+
				dtTempEnd.toString()+" using NWSRFS FS5Files \""
				+__fs5FilesLocation.substring(0,
				__fs5FilesLocation.length()-1)+"\"");

			// Check to see if we were to just check to see if we
			// have data without actually reading all of the data 
			// into structures!
			if(!readData) {
				tsFile.setHasData(false);
				tsFile.setObservedTS(ITS);
				return tsFile;
			}
		}

		// Allocate space for the data!
		ITS.allocateDataSpace();
		tsFile.setHasData(true);
		
		// Now put in the data! See comment above about Julian minutes.
		for (i = 0;i < (pdbFile.getDATAVAL()).size(); i++) {
			float floatValue = pdbFile.getDATAVAL(i);
			dtTemp = NWSRFS_Util.getDateFromJulianHour1900(
				pdbFile.getOBSTIME(i)/100);
			dtTemp.setTimeZone("Z");
			ITS.setDataValue(dtTemp,
				(double)floatValue);
		}
	}
		
	// Put the the tsID into the hash table for future checks.
	tsFile.setObservedTS(ITS);
	tsFile.setIsDataFilled(true);
	__tsHashtable.put(tsIdentKey, tsFile);
	
	// Return the NWSRFS_TimeSeries Object!
	return tsFile;
}

/**
Reads a time series object from the binary process database.
@param segObject this is an NWSRFS_Segment object that is the grand parent
of the TimeSeries object. The Operations which are the parents will be created
from the Segment object.
@return a Vector of NWSRFS_TimeSeries object which holds all of the 
information and data from the PRDPARM and PRDTSn binary files for the segment.
@throws Exception if an error occurs.
*/
/* FIXME SAM 2008-04-07 Uncomment if functionality is enabled.
private Vector readTimeSeriesPRD(NWSRFS_Segment segObject) throws Exception {
	Vector ts = new Vector();

	// Now get the Time Series identifier from the list of 
	// operations defined on the segment.
	int j = 0;
	NWSRFS_Operation opObject = null;
	NWSRFS_TimeSeries tsFile = null;
	String tsID = null;
	String tsDT = null;
	int tsDTInterval = 0;
	
	for (int i = 0; i < segObject.getNumberOfOperations(); i++) {
		opObject = segObject.getOperation(i);
		for (j = 0; j <opObject.getNumberOfTSIDs(); j++) {
			tsID = opObject.getTSID(j);
			tsDT = opObject.getTSDT(j);
			tsDTInterval = (opObject.getTimeSeries(j)).getTSDTInterval();
			if (tsID != null && tsDTInterval != 0) {
				// Create a new 
				// NWSRFS_TimeSeries object
				tsFile = readTimeSeriesPRD(tsID,
					tsDT, tsDTInterval,true);
				ts.add(tsFile);
			}
		}
	}

	return ts;
}
*/

/**
Reads a time series object from the binary process database.
@param tsID this is a String object that holds the TimeSeries Identifier
for the TimeSeries object. 
@param tsDT this is the String value of the TimeSeries data type. It is
necessary that the data type be supplied to get a unique set of Time Series
from the data files.
@param tsDTInterval this is the int value of the TimeSeries data time interval. 
It is necessary that the data time interval be supplied to get a unique set of 
Time Series from the data files.
@param readData this determines whether or not to actually read the data or
just determine if data exists!
@return an NWSRFS_TimeSeries object which holds all of the information and data
from the PRDPARM and PRDTSn binary files.
@throws Exception if there is an error reading from the database.
@throws NullPointerException if the time series identifier is null.
*/
private NWSRFS_TimeSeries readTimeSeriesPRD(String tsID, String tsDT, int tsDTInterval, boolean readData) 
throws Exception {
	String routine = "NWSRFS_DMI.readTimeSeriesPRD";
	char[] charValue = null;
	EndianDataInputStream EDIS;
	int i=0, prdIndex = 1, prdTSIDSize, recordNum = -1;
	String parseChar = null, tsIdentKey;
	NWSRFS_TimeSeries tsFile;

	// Check to see if the prdindex file exists! If not return empty list.
	if(getPRDIndex() == null) { 
		setPRDIndex(readPRDINDEX());
	}
	
	if (tsID == null) {
		throw new Exception("The Time Series identifier argument "
			+ "is empty.");
	}
	else if(tsDT == null) {
		throw new Exception("The Time Series Data Type argument "
			+ "is empty.");
	}
	else if(tsDTInterval <= 0) {
		throw new Exception("The Time Series Data Time Interval argument "
			+ "is empty.");
	}

	// Set the TSIdent String
	tsIdentKey = tsID+"."+tsDT+"."+tsDTInterval;

	// If we have already looked at this time series just return it and 
	// do not retreive it again!
	if(__tsHashtable.containsKey(tsIdentKey) && __cacheTS) {
		tsFile = (NWSRFS_TimeSeries)__tsHashtable.get(tsIdentKey);
		if(tsFile.getIsDataFilled()) {
			return tsFile;
		}
	}
	
	// If we have not previously retreived this time series create a 
	// new NWSRFS_TimeSeries object
	tsFile = new NWSRFS_TimeSeries(tsID,tsDT,tsDTInterval);

	// Check if the the database binary file is open as a
	// Random Access object
	if (!checkRandomAccessFileOpen(__PRDPARM, true)) {
		throw new Exception("Can not open the " 
			+ __dbFileNames[__PRDPARM] + " binary database file");
	}

	// Now read the Time Series parameter file to get the parameters
	// for the Time series in the PRDTSn binary file.

	// Read the first record to get the global values (240 bytes)
	EDIS = read(__NWSRFS_DBFiles[__PRDPARM], 0, 240);

	// Field 1 - [type field name here]
	charValue = new char[8];
	for (i = 0; i < 8; i++) {
		charValue[i] = EDIS.readEndianChar1();
	}
		
	parseChar = new String(charValue).trim();
		
	if (parseChar.length() != 0) {
		tsFile.setNAMERF(parseChar);
	}
		
	// Field 2 - [type field name here]
	tsFile.setMAXDTP(checkInt(EDIS.readEndianInt(), 0, 200, -1));
		
	// Field 3 - [type field name here]
	tsFile.setMAXTMS(checkInt(EDIS.readEndianInt(), 0, 400, -1));
		
	// Field 4 - [type field name here]
	tsFile.setMINDAY(checkInt(EDIS.readEndianInt(), 0, 365, -1));
	
	// Loop through an unused portion of the first record.
	for (i = 0; i < 7; i++) {
		// TODO (JTS - 2004-08-18)
		// why not just skip ahead 32 (or is it 64?) bytes?
		EDIS.readEndianInt();
	}

	// Field 5 - [type field name here]
	tsFile.setNUMTMS(checkInt(EDIS.readEndianInt(), 0, 400, -1));
			
	// Field 6 - [type field name here]
	tsFile.setNUMDTP(checkInt(EDIS.readEndianInt(), 0, 200, -1));

	EDIS.close();
	
	while (true) {
		// Read until EOF or break.
		try {
			// Read the subsequent records 
			EDIS = read(__NWSRFS_DBFiles[__PRDPARM],0,__byteLength[__PRDPARM]);

			// Field 1 - [type field name here]
			charValue = new char[4];
			for (i = 0; i < 4; i++) {
				charValue[i] = EDIS.readEndianChar1();
			}
			
			parseChar = new String(charValue).trim();

			if (parseChar.length() == 0 
				|| !parseChar.equalsIgnoreCase(tsDT)) {
				EDIS.close();
				continue;
			}

			// Field 2 - [type field name here]
			tsFile.setIUNIT(checkInt(EDIS.readEndianInt(), 
				1, 100, 1));

			// Field 3 - [type field name here]
			tsFile.setNCORE(checkInt(EDIS.readEndianInt(), 
				0, 200, -1));

			// Field 4 - [type field name here]
			tsFile.setMAXDAY(checkInt(EDIS.readEndianInt(), 
				0, 365, -1));

			// Field 5 - [type field name here]
			tsFile.setMINDT(checkInt(EDIS.readEndianInt(), 
				0, 24, -1));

			// Field 6 - [type field name here]
			tsFile.setIPROC(checkInt(EDIS.readEndianInt(), 
				0, 10, 0));

			// Field 7 - [type field name here]
			tsFile.setIFDAT(checkInt(EDIS.readEndianInt(), 
				-10000, 10000, 0));

			// Field 8 - [type field name here]
			tsFile.setIFRECD(checkInt(EDIS.readEndianInt(), 
				0, 10000, -1));

			// Field 9 - [type field name here]
			tsFile.setILRECD(checkInt(EDIS.readEndianInt(), 
				0, 10000, -1));

			// Field 10  - [type field name here]
			tsFile.setICPTR(checkInt(EDIS.readEndianInt(), 
				0, 10000, -1));

			// Field 11  - [type field name here]
			tsFile.setICALL(checkInt(EDIS.readEndianInt(), 
				0, 10, -1));

			// Field 12  - [type field name here]
			tsFile.setIDIM(checkInt(EDIS.readEndianInt(), 
				0, 1000, -1));

			// Field 13  - [type field name here]
			tsFile.setNVAL(checkInt(EDIS.readEndianInt(), 
				-2, 24, -1));

			// Field 14  - [type field name here]
			tsFile.setNXHDR(checkInt(EDIS.readEndianInt(), 
				0, 100, -1));

			// Field 15  - [type field name here]
			tsFile.setNUMTS(checkInt(EDIS.readEndianInt(), 
				0, 10000, -1));

			break;
		}
		catch (EOFException EOFe) {
			exceptionCount++;
			Message.printWarning(10, routine,
			"No Time Series for: "
			+ tsIdentKey + " was found.");
			return (NWSRFS_TimeSeries)null;
		}
		catch (IOException IOe) {
			exceptionCount++;
			throw new Exception("The TSID = " + tsID + "." + tsDT
				+ " has not been found in the processed "
				+ "database.");
		}
	}

	EDIS.close();

	// Set the TS Data Type and Logical Unit Number into the _tsDTUHashtable for
	// reuse if it is not there already!
	if(!__tsDTUHashtable.containsKey(tsDT)) {
		__tsDTUHashtable.put(tsDT,new Integer(tsFile.getIUNIT()));
	}
	
	// Create the tsDataFile String. Need to loop through all 5 of the
	// TS files until find right unit number. If LUNIT from the PRDTSn
	// Equals the IUNIT value from the PRDPARM file then have the right
	// Time Series file to read the TS into.
	for (i = 0; i <= 5; i++) {
		prdIndex = (int)new Integer(String.valueOf(__PRDTS1 + i)).intValue();
		if(i == 5) {
			Message.printWarning(10, routine, "No Time Series for: " + tsIdentKey + " was found.");
			return (NWSRFS_TimeSeries)null;
		}
		else {
			/* TODO SAM 2008-04-07 What are these used for?
		if (__useFS5Files && true) {
			prdtsDataFile = __fs5FilesLocation + __dbFileNames[prdIndex]; 
		}
		else {
			prdtsDataFile = __dbFileNames[prdIndex];
		}
		*/

		// Check if the the database binary file is open as a Random Access object
		if (!checkRandomAccessFileOpen(prdIndex, true)) {
			throw new Exception("Can not open the " 
				+ __dbFileNames[prdIndex] + " binary database file");
		}

// TODO (JTS - 2004-08-21)
// explain the magic number 4 and 5		
		EDIS = read(__NWSRFS_DBFiles[prdIndex],0, 4, 5);
		if (tsFile.getIUNIT() == EDIS.readEndianInt()) {
			break;
		}
		else {
			EDIS.close();
		}
		}
	}

	// Set prdIndex in the NWSRFS_TimeSeries object for future use!
	tsFile.setPrdIndex(prdIndex);
	
	// Check the PRDTSn to see if it is null!
	if (__NWSRFS_DBFiles[prdIndex] == null) {
		Message.printWarning(10, routine, "No Time Series of data type: " + tsDT + " was found.");
		return (NWSRFS_TimeSeries)null;
	}

	// Rewind the PRDTSn to prepare for the TS read.
	rewind(__NWSRFS_DBFiles[prdIndex]);

	// Now read the Time Series index object to get the Record number
	// for the Time series in the PRDTSn binary file.
	// This might look a little convoluted but what is going on is
	// this: We read a record from PRDINDEX which contains TDID, TSDT, and record
	// number for the PRDTSn file. Since no date time interval is used
	// here as a way to find a unique time series we must then read in the
	// date time interval from the PRDTSn file and check to see if it is the
	// same. If so we keep reading the time series data then break out. If not
	// break out of the PRDTSn read and go to the next record in PRDINDEX
	// which matches TSID and TSDT... Ugly but there is no other way to do it
	// given the structure of the FS5Files.
	prdTSIDSize = ((getPRDIndex()).getTSID()).size();

	for(i = 0; i < prdTSIDSize; i++) {
		// Check the TS ID to see if we match
		if (!((getPRDIndex()).getTSID(i)).equalsIgnoreCase(tsID)) {
			continue;
		}

		// Check the TS Data Type to see if we match
		if (!((getPRDIndex()).getTSDT(i)).equalsIgnoreCase(tsDT)) {
			continue;
		}

		// If we match the TS ID and TS Data Type the get record number
		recordNum = (getPRDIndex()).getIREC(i);

		// Check now to if we are reading the data if so
		// continue; if not return tsFile!
		if(!readData) {
			// Now call readPRDTS to first see if we have 
			// the right record then read the TS datafile 
			// to see if TS exists! 
			if(readPRDTS(__NWSRFS_DBFiles[prdIndex],recordNum,tsFile,false)) {
				break;
			}
			else {
				continue;
			}
		}
		else {
			if(readPRDTS(__NWSRFS_DBFiles[prdIndex],recordNum,tsFile,true)) {
				break;
			}
			else {
				continue;
			}
		}
	}

	// Now check to see if i = prdTSIDSize. If so then no TS was found and we need to
	// return a null NWSRFS_TimeSeries to the calling method.
	if(i == prdTSIDSize) {
		Message.printWarning(10,routine,"No time series data found!");
		return null;
	}
//	EDIS.close();
	
	// Put the the tsID into the hash table for future checks.
	tsFile.setIsDataFilled(true);
	__tsHashtable.put(tsIdentKey, tsFile);
	
	return tsFile;
}

/**
Read a Time Series object from the database.
@param opObject this is an NWSRFS_Operation object that is the parent
of the TimeSeries object. 
@param dataType this is the String value of the Time Series data type. It is
necessary that the data type be supplied to get a unique set of Time Series
from the data files.
@return a Vector of NWSRFS_TimeSeries objects which holds all of the 
information and data from the PRDPARM and PRDTSn binary files.
*/
/* FIXME SAM 2008-04-07 Uncomment when functionality is enabled
private Vector readTimeSeriesPRD(NWSRFS_Operation opObject, String dataType) 
throws Exception {
	String tsID = null;
	int tsDTInterval = 0;
	Vector tsVec = new Vector();

	// Get the Time Series IDs
	for (int i = 0; i < opObject.getNumberOfTSIDs(); i++) {
		tsID = opObject.getTSID(i);
		tsDTInterval = (opObject.getTimeSeries(i)).getTSDTInterval();
		tsVec.add((NWSRFS_TimeSeries)readTimeSeriesPRD(
			tsID, dataType, tsDTInterval, true));
	}

	// Return the TimeSeries object
	return tsVec;
}
*/

/** 
Reads the preprocessed parameteric database to fill the data members of the
NWSRFS_USER object argument. It will read the information from the 
preprocessed parameteric database (PPDB) files PPPPARM<i>n</i> where <i>n</i> 
is the logical unit found from the PPPINDEX file associated with a specific 
parameter type. The known "general" parameter types in the PPDB are NTWK, 
STBN, USER, and URRS.
@param userParam a NWSRFS_USER which holds the minimum set of data for a 
USER param dervied from the PPPINDEX file. This method will fill out the USER 
Parameter object.
@param deepRead a boolean specifying whether to read all USER parameters
from the PPDB or just general parameters.
@return  an NWSRFS_USER object which stores the data from
the PPDB files PPPARMn.
@throws Exception if an error is detected.
*/
public NWSRFS_USER readUSERParam(NWSRFS_USER userParam, boolean deepRead) 
throws Exception
{
	NWSRFS_PPPINDEX pppindex = getPPPIndex();

	// Check to see if the pppindex file exists! If not return empty list.
	if(pppindex == null) { 
		setPPPIndex(readPPPINDEX());
	}
	else if(pppindex.getPARMTP() == null) {
		return userParam;
	}
	
	// Fill the USERParam object.
	parseParametericArray((Object)userParam,"USER",deepRead);
	
	// Return the filled out USERParam object
	return userParam;
}

/**
Reads in to a Vector of Strings the list of USER param identifiers. It will
basically regurgetate the PPPINDEX file which creates a list of USER param ids 
and a record number. Please note that the USER parameter record is different
in that it only has one record in the PPPPARAM<i>n</i> file and is not listed
in the PPPINDEX records except in the "FIRST" and "LAST" record of the index!
@return Vector of Strings containing the list of all USER param ids in the 
database.
@throws Exception if something goes wrong.
*/
public Vector readUSERParamList() throws Exception
{
	Vector userParamList = new Vector();
	int logicalUnitNum = -1;
	int numberOFParamRecs = -1;
	NWSRFS_PPPINDEX pppindex = getPPPIndex();
	NWSRFS_USER user;

	// Check to see if the pppindex file exists! If not return empty list.
	if(pppindex == null) { 
		setPPPIndex(readPPPINDEX());
	}
	else if(pppindex.getPARMTP() == null) {
		return userParamList;
	}
	
	// Get the logical unit for the parameter type.
	for(int i=0;i<(pppindex.getPARMTP()).size();i++) {
		if(((String)(pppindex.getPARMTP()).elementAt(i)).equalsIgnoreCase("USER")) {
			logicalUnitNum = ((Integer)pppindex.getLUFILE().elementAt(i)).intValue();
			numberOFParamRecs = ((Integer)pppindex.getNUMPRM().elementAt(i)).intValue();
			break;
		}
	}
	
	// Now get the records. If only one get only that record and use the 
	// FIRST value from the pppindex file.
	if(numberOFParamRecs <= 0) {
		return userParamList;
	}
	else if(numberOFParamRecs == 1) {
		// Get the logical unit for the parameter type.
		for(int i=0;i<(pppindex.getPARMTP()).size();i++) {
			if(((String)(pppindex.getPARMTP()).elementAt(i)).equalsIgnoreCase("USER")) {
				logicalUnitNum = ((Integer)pppindex.getLUFILE().elementAt(i)).intValue();
				user = new NWSRFS_USER();
				user.setLogicalUnitNum(logicalUnitNum);
				user.setRecordNum(((Integer)(pppindex.getFIRST()).elementAt(i)).intValue());
				userParamList.addElement((NWSRFS_USER)user);
				break;
			}
		}
	}
	else {
		// Loop through the ID.size number of records
		for(int i=0;i<(pppindex.getID()).size();i++) {
			// If the type is BASN add to the Vector
			if(((String)(pppindex.getITYPE()).elementAt(i)).
			equalsIgnoreCase("USER")) {
				user = new NWSRFS_USER((String)(pppindex.getID()).elementAt(i));
				user.setLogicalUnitNum(logicalUnitNum);
				user.setRecordNum(((Integer)(pppindex.getIREC()).elementAt(i)).intValue());
				userParamList.addElement((NWSRFS_USER)user);
			}
		}
	}
	
	// Return Vector of USER param ids
	return userParamList;
}

/**
Read the USERPARM file records.  This are essentially system parameters.
@return A NWSRFS_USERPARM instance from the NWSRFS USERPARM FS5Files
file, which contain general user parameters for the operational system.
@exception Exception if the USERPARM record cannot be read.
*/
public NWSRFS_USERPARM readUSERPARM ()
throws Exception
{	// TODO SAM 2006-10-03
	// Not sure why Scott was doing things the way he did.  It seems like
	// the following is easier to understand.  Maybe he thought that reading
	// the bytes and then parsing performed better?
	if (!checkRandomAccessFileOpen(__USERPARM, true)) {
		throw new Exception("Cannot open the " 
			+ __dbFileNames[__USERPARM] + " binary database file");
	}
	EndianRandomAccessFile eraf = __NWSRFS_DBFiles[__USERPARM];
	// Position at the start of the file...
	eraf.seek ( 0 );
	// Read the records in the file...
	// TODO SAM 2006-10-03
	// Add intelligent messages for end of file detection...
	//byte[] b = new byte[__byteLength[__USERPARM] -12];// Ignored bytes at end of rec
	NWSRFS_USERPARM rec = new NWSRFS_USERPARM();
	eraf.readLittleEndianString1(4);// TIME1(1), Ignored
	eraf.readLittleEndianString1(4);	// TIME(2)
	rec.setTime3 ( eraf.readLittleEndianString1(4).trim());// TIME(3)
	return rec;
}

/**
Rewind a binary file for future reading or writing.
@param raFile this is the EndianRandomAccessFile to the binary file to rewind. 
@throws Exception if an error occurs.
*/
protected void rewind(EndianRandomAccessFile raFile) 
throws Exception {
	raFile.seek(0L);
}

/**
Seek to a position in a binary file.
@param raFile the EndianRandomAccessFile in which to seek.
@param position this long value is the position to seek to from the beginning 
of the binary file.
@param useFS5Files this boolean value determines if the calling method needs 
to have the fs5files directory structure added to the filename. It is 
necessary to set this to <code>true</code> for standard NWSRFS database files 
and <code>false</code> for ESP traces, etc.
@throws Exception if it catches an exception while trying to seek in the file.
*/
protected void seek(EndianRandomAccessFile raFile, long position, 
boolean useFS5Files) 
throws Exception {
	// Check to see if the seek position is < 0 if so set to 0
	if (position < 0) {
		position = 0;
	}

	raFile.seek(position);
}

/**
Set the input name, used with time series identifiers.
@param input_name The input name associated with the FS5 Files, often the path to
the directory containing the files.
*/
public void setInputName ( String input_name )
{	__input_name = input_name;
}

/**
Sets up the global proplist for the NWSRFS database.
*/
protected void setupDatabaseProperties() {
	if (__NWSRFS_properties == null) {
		__NWSRFS_properties = new PropList("NWSRFS_properties");
	
		if (__useFS5Files) {
			__NWSRFS_properties.add(
				"fs5files = " + __fs5FilesLocation);
		}
	}
}

/**
Sets the object holding index values for the preprocessor 
database files PDBLYn and PDBRRS.
@param pdbindex the object holding index values for the preprocessor 
database files PDBLYn and PDBRRS.
*/
public void setPDBIndex(NWSRFS_PDBINDEX pdbindex) {
	_pdbindex = pdbindex;
}

/**
Sets the object holding index values for the preprocessor parametric 
database file PPPPARMn.
@param pppindex the object holding index values for the preprocessor parametric 
database file PPPPARMn.
*/
public void setPPPIndex(NWSRFS_PPPINDEX pppindex) {
	_pppindex = pppindex;
}

/**
Sets the object holding index values for the processed
database file PRDTSn.
@param prdindex the object holding index values for the processed 
database file PRDTSn.
*/
public void setPRDIndex(NWSRFS_PRDINDEX prdindex) {
	_prdindex = prdindex;
}

/**
Write to a data file. This method appends byte length bytes to the file 
referenced by raFile. It will call the overloaded write method setting 
recordNumber = 0 and insertFlag="a" thus forcing the write to be an append.
@param raFile an EndianRandomAccessFile holding the binary file to write.  If
this is a standard NWSRFS processed database file then the useFS5Files boolean 
should be set to prepend the value of the fs5files token which is the path 
to the processed database otherwise pass a full path in this variable to 
the binary file.
@param isBigEndian a boolean determining what the endianess of the file 
should be.
@param record a byte array holding the binary record to write.
@param byteLength an integer used to tell the method how many bytes a 
record contains.  The method writes "byteLength" bytes to the file at 
location "recordNumber * byteLength" bytes.
@param useFS5Files this boolean value determines if the calling method 
needs to have the fs5files directory structure added to the filename. 
It is necessary to set this to <code>true</code> for standard NWSRFS database 
files and <code>false</code> for ESP traces, etc.
@throws Exception if it catches an exception while trying to write to the file.
*/
protected void write(EndianRandomAccessFile raFile, boolean isBigEndian, 
byte[] record, int byteLength, boolean useFS5Files) 
throws Exception {
	// Call the main write method always setting recordLength 
	// to 0 and insertFlag to "a" or append.
	write(raFile, isBigEndian, record, 0, byteLength, "a", useFS5Files);
}

/**
Write to a data file. The record number and byte length can be used for 
determining the exact place to start tth write to the data file.
@param raFile an EndianRandomAccessFile holding the index to the binary file 
to write.  If this is a standard NWSRFS processed database file then the 
useFS5Files boolean  should be set to prepend the value of the fs5files 
token which is the path to the processed database otherwise pass a full 
path in this variable to the binary file.
@param isBigEndian a boolean determining what the endianess of the file 
should be.
@param record a byte array holding the binary record to write.
@param recordNumber a int used to set the position of the file to write 
the correct record. 
@param byteLength an integer used to tell the method how many bytes a 
record contains.  The method writes "byteLength" bytes to the file at 
location "recordNumber*byteLength" bytes.
@param insertFlag this String value is used to determine whether or not 
to append the record to the end of the file, insert the record at the given 
place, or replace a given record. For writing to NWSRFS database files most 
likely insertion or replacement will be needed while for ESP trace data 
and other binary NWSRFS files appending data will be used. Valid values are:
<pre>
	"a" for append
	"i" for insert
	"r" for replacement
</pre>
@param useFS5Files this boolean value determines if the calling method 
needs to have the fs5files directory structure added to the filename. It 
is necessary to set this to <code>true</code> for standard NWSRFS database 
files and <code>false</code> for ESP traces, etc.
@throws Exception if it catches an exception while trying to write to the file.
*/
protected void write(EndianRandomAccessFile raFile, boolean isBigEndian, 
byte[] record, int recordNumber, int byteLength, String insertFlag, boolean useFS5Files) 
throws Exception
{
	
	// Get the current DateTime for the temp file.
	DateTime nowDT = new DateTime((Date)new Date());

	// Now determine if append to the end of the file or replace a specific record.
	// To replace a specific record is much more complicated but can still be done.
	if (insertFlag.equalsIgnoreCase("a")) {
		// Seek to end of the file in order to append
		raFile.seek(raFile.length());

		// Now write the record
		for (int i = 0; i < byteLength; i++) {
			raFile.writeByte(record[i]);
		}
	}
	else {
		byte[] recordTemp = null;
		File tempFile = null;
		int i = -1;
		int j = -1;
		EndianRandomAccessFile endianTempFile = null;
		// Temp file to store the temporary binary data: 
		// A File object of the temp file path is also 
		// created so the temp file can be deleted later
		if (__useFS5Files) {
			endianTempFile = new EndianRandomAccessFile(
				__fs5FilesLocation + "temp"
				+ nowDT.toString(
				DateTime.FORMAT_YYYYMMDDHHmm), "rw"); 
			tempFile = new File(__fs5FilesLocation + "temp"
				+ nowDT.toString(
				DateTime.FORMAT_YYYYMMDDHHmm)); 
		}
		else {
			endianTempFile = new EndianRandomAccessFile("temp"
				+ nowDT.toString(
				DateTime.FORMAT_YYYYMMDDHHmm), "rw"); 
			tempFile = new File("temp"
				+ nowDT.toString(
				DateTime.FORMAT_YYYYMMDDHHmm)); 
		}

		// Seek to beginning of the file in order to append
		raFile.seek(0L);

		// Write the first recordNumber*byteLength records 
		// to temp file.  To do this will need to first 
		// read from the file.	
		for (j = 0; j < byteLength * recordNumber;j++) {
			// Read from binary file
			for (i = 0; i < byteLength; i++) {
				recordTemp[i] 
					= (byte)raFile.readByte();
			}

			// Write to temp file
			for (i = 0; i < byteLength; i++) {
				endianTempFile.writeByte(recordTemp[i]);
			}
		}

		// Write the passed in record to the temp file
		for (i = 0; i < byteLength; i++) {
			endianTempFile.writeByte(record[i]);
		}
			
		// If the record is to be replaced then skip over it
		// instead of writing it to the temp file.
		if (insertFlag.equalsIgnoreCase("r")) {	
			// Skip the altered record in the 
			// original binary
			raFile.skipBytes((int)byteLength);
		}

		// Write the remainder of the file until an EOFException
		try {
			while (true) {
				// Read from binary file
				for (i = 0; i < byteLength; i++) {
					recordTemp[i] 
						= (byte)raFile
						.readByte();
				}

				// Write to temp file
				for (i = 0; i < byteLength; i++) {
					endianTempFile.writeByte(
						recordTemp[i]);
				}
			}
		}
		catch (EOFException EOFe) {
			exceptionCount++;
// TODO (JTS - 2004-08-21)
// handle exception?			
			// just catch this and go on
		}

		// Rewind both files and reverse positions to write to
		// original binary file
		raFile.seek(0L);
		endianTempFile.seek(0L);

		// Write the remainder of the file until an EOFException
		try {
			while (true) {
				// Read from binary file
				for (i = 0; i < byteLength; i++) {
					recordTemp[i] 
						= (byte)endianTempFile
						.readByte();
				}

				// Write to temp file
				for (i = 0; i < byteLength; i++) {
					raFile.writeByte(recordTemp[i]);
				}
			}
		}
		catch (EOFException EOFe) {
			exceptionCount++;
// TODO (JTS - 2004-08-21)
// handle exception?			
			// just catch and go on
		}

		// Remove the temp file
		endianTempFile.close();
		tempFile.delete();
	}
}

/**
Write to a data file. This method appends byte length bytes to the file 
referenced by fileName.  It will call the overloaded write method setting 
recordNumber = 0 and insertFlag="a" thus forcing the write to be an append.
@param fileName a String holding binary filename to write. If this is a 
standard NWSRFS processed database file then the useFS5Files boolean 
should be set to prepend the value of the fs5files token which is the 
path to the processed database otherwise pass a full path in this variable 
to the binary file.
@param isBigEndian a boolean determining what the endianess of the file 
should be.
@param useFS5Files this boolean value determines if the calling method 
needs to have the fs5files directory structure added to the filename. 
It is necessary to set this to <code>true</code> for standard NWSRFS database 
files and <code>false</code> for ESP traces, etc.
@return An EndianDataOutputStream.
@throws Exception if it catches an exception while trying to write to the file.
*/
protected EndianDataOutputStream write(String fileName, boolean isBigEndian, 
boolean useFS5Files) 
throws Exception {
	// Call the main write method always setting recordLength to 0 and
	// insertFlag to "a" or append.
	return write(fileName, isBigEndian, "a", useFS5Files);
}

/**
Write to a data file. The record number and byte length can be used for 
determining the exact place to start tth write to the data file.
@param fileName a String holding binary filename to write. If this is a 
standard NWSRFS processed database file then the useFS5Files boolean should 
be set to prepend the value of the fs5files token which is the path to 
the processed database otherwise pass a full path in this variable to 
the binary file.
@param isBigEndian a boolean determining what the endianess of the file 
should be.
@param insertFlag this String value is used to determine whether or not 
to append the record to the end of the file, insert the record at the given 
place, or replace a given record. For writing to NWSRFS database files most 
likely insertion or replacement will be needed while for ESP trace data 
and other binary NWSRFS files appending data will be used. Valid values are:
<pre>
	"a" for append
	"i" for insert (Not currently implemented)
	"r" for replacement
</pre>
@param useFS5Files this boolean value determines if the calling method 
needs to have the fs5files directory structure added to the filename. It 
is necessary to set this to <code>true</code> for standard NWSRFS database 
files and <code>false</code> for ESP traces, etc.
@return An EndianDataOutputStream.
@throws Exception if it catches an exception while trying to write to the file.
*/
protected EndianDataOutputStream write(String fileName, boolean isBigEndian, 
String insertFlag, boolean useFS5Files) 
throws Exception {
	String routine = "NWSRFS_DMI.write()";
	
	// Now determine if append to the end of the file or 
	// replace a specific record.		
	// To replace a specific record is much more complicated 
	// but can still be done.
	FileOutputStream FOS = null;
	if (insertFlag.equalsIgnoreCase("a")) {
		// Create the output stream. Only 
		// append to the stream.
		FOS = new FileOutputStream(fileName,true);
	}
	else if (insertFlag.equalsIgnoreCase("i")) {
		Message.printWarning(10, routine,
			"Inserting data into the middle of "
			+ "an existing file is not currently "
			+ "implemented.");
	}
	else {
		// Create the output stream and replace the file.
	FOS = new FileOutputStream(fileName,false);
	}

	// Create the EndianDataOutputStream to return
	EndianDataOutputStream EDOS 
		= new EndianDataOutputStream((OutputStream)FOS);

	// Set the Endianess
	EDOS.setBigEndian(isBigEndian);

	return EDOS;
}

/**
Returns whether the DMI is using big endian numbers or not.
@return true if the DMi is using big endian numbers, false if using small 
endian.
*/
public boolean usingBigEndian() {
	return __isBigEndian;
}

/**
Returns whether the dmi is using FS5 files or not.
@return true if the dmi is using FS5 files, false if not.
*/
public boolean usingFS5Files() {
	return __useFS5Files;
}

//////////////////////////////////////////////////////////////////////
// JTS testing area
// Looks like Sean hadn't really heard of "methods" before, as there is a ton
// of repeated code in the read methods.  I implemented the following as 
// tests in one of the readTimeSeries() methods to see how well it cleaned
// up the code.  Readability is on the rise ... just need a chance to implement
// across the entire DMI.

public int checkInt(int value, int lowRange, int highRange, int errorValue) {
	if (value < lowRange || value > highRange) {
		return errorValue;
	}
	else {
		return value;
	}
}

public float checkFloat(float value, float lowRange, float highRange, 
float errorValue) {
	if (value < lowRange || value > highRange) {
		return errorValue;
	}
	else {
		return value;
	}
}

// In particular, every time a string is read in a new char array is created.
// that's just wasteful.  A method like the following, with a 
// statically-allocated array, can be re-used without having to worry about
// how many arrays are being created in a single pass of reading one of
// the files.

// haven't fully implemented yet, but could work out well ... just need some
// spare time in which to try it out.
private static char[] __working4Char = new char[4];
private static char[] __working8Char = new char[8];
private static char[] __working20Char = new char[20];
private static int __workingIncr = 0;

public String getCharString(EndianDataInputStream EDIS, int length, 
boolean nullAsEmpty) 
throws Exception {
	String s = null;
	if (length == 4) {
		for (__workingIncr = 0; __workingIncr < length; 
		    __workingIncr++) {
			__working4Char[__workingIncr] = 0;
			__working4Char[__workingIncr] = EDIS.readEndianChar1();
		}
		s = new String(__working4Char);
	}
	else if (length == 8) {
		for (__workingIncr = 0; __workingIncr < length; 
		    __workingIncr++) {
			__working8Char[__workingIncr] = 0;
			__working8Char[__workingIncr] = EDIS.readEndianChar1();
		}
		s = new String(__working8Char);
	}
	else if (length == 20) {
		for (__workingIncr = 0; __workingIncr < length; 
		    __workingIncr++) {
			__working20Char[__workingIncr] = 0;
			__working20Char[__workingIncr] = EDIS.readEndianChar1();
		}
		s = new String(__working20Char);
	}
		
	s = s.trim();

	if (s != null && s.length() != 0) {
		return s;
	}
	else {
		if (nullAsEmpty) {
			return "";
		}
		return null;
	}
}


// TODO (JTS)
// simply used as a test for counting the number of times exceptions are thrown
// in various places.  They can definitely be removed later.
public static int exceptionCount = 0;
public static int parseOperationExceptionCount = 0;

}

// - parseChar
// bytesToRead
