// ----------------------------------------------------------------------------
// ESPTraceEnsemble - Class for reading/writing/creating/converting/managing
//			ESP Trace Ensemble files.
//
// The original code was ported to Java from C++ using code in the Linux
// /awips/hydroapps/lx/rfc/nwsrfs/ens/src/ESPTS/TEXT directory
// for AWIPS build v23.
// Java from Hank Herr's "batch builder" program was also consulted.
// ----------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2003-01-23	J. Thomas Sapienza, RTi	Initial version
// 2003-01-27	JTS, RTi		Completed initial version of all code.
// 2003-01-28	JTS, RTi		Corrected some errors in data sizes and
//					renamed all variables to match their
//					C++ equivalents.  Javadoc'd.
// 2003-01-29	JTS, RTi		Converted all the char[] variables to
//					Strings.  Started transitioning 
//					variables from similar to the C++ names 
//					to being more comprehensible.
// 2003-02-03	JTS, RTi		Eliminated the use of a holding array
//					between when data were read from the
//					file and when they were put into the
//					time series objects.
// 2003-07-21	JTS, RTi		Updated to no longer use TSDate.
// 2003-07-23	Steven A. Malers, RTi	* Review code.
//					* Move from test package to
//					  RTi.DMI.NWSRFS.
//					* Change private data commens to be
//					  right-justified instead of javadoc -
//					  easier to read.
//					* Change the constructor to NOT have as
//					  parameter the simulation type (this
//					  can be determined from the file) or
//					  the record lengths (these are standard
//					  and if they change some trick can be
//					  used to figure it out).
//					* When opening the random access file,
//					  determine how the file was written by
//					  evaluating the header data.  This
//					  allows this code to always read the
//					  file correctly whether it was written
//					  on HP-UX (big-endian) or Linux
//					  (little-endian) can be read.  Files
//					  may be transferred between machines.
//					* Change data member names to those that
//					  agree with C++ to make it easier to
//					  compare code, even if the names are
//					  not very intuitive.
//					* Add local versions of what is in the
//					  extended NWS TSIdent so that data are
//					  not lost.
//					* Change the names of the read methods
//					  to just readData() and readHeader().
// 2003-11-20	SAM, RTi		* Add a constructor to take a Vector of
//					  TS, to create a conditional simulation
//					  trace ensemble, for writing with
//					  writeESPTraceEnsemble().
// 2003-12-04	SAM, RTi		* Update to use julda and mdyh1 rather
//					  that "1900" methods - were not getting
//					  results consistent with the NWSRFS
//					  FORTRAN with the old routines.
//					* Overload the constructor to allow the
//					  file to remain open even when data are
//					  not read.
//					* Add convertESPTraceEnsembleToText()
//					  to convert an ESP trace ensemble file
//					  to a human-readable file, for
//					  troubleshooting.
//					* Add a few accessor methods like
//					  getIrec() to support the conversion
//					  method.
// 2003-12-12	SAM, RTi		* After discussions with Jay Day, clean
//					  up the code to finalize functionality.
//					* Delete the __hdr_* data that are not
//					  needed.
//					* Remove floatToString() - there is a
//					  more direct way to read the strings.
// 2003-12-15	SAM, RTi		* Starting version read code seems OK
//					  and writer is very close but does not
//					  produce traces that ESPADP can read.
//					  Troubleshoot and resolve.
// 2004-04-06	SAM, RTi		Was not able to resolve problem writing
//					the trace file.  Clean up the code and
//					Javadoc as much as possible and turn
//					over to Scott to resolve.
// 2004-04-07	Scott Townsend, RTi	Modify to fit with in the new NWSRFS_DMI
//					framework.
// 2004-08-05	SAM, RTi		Fix bug in convertESPTraceEnsembleToText
//					where zero length output units caused an
//					exception - it is now allowed, as null
//					was allowed before.
// 2004-11-29	SAM, RTi		Updates related to TSIdent now using the
//					sequence number as part of the
//					identifier.
//					* Clean up some code that wraps over
//					  80 characters to simplify review.
//					* Test with new TSIdent.
// 2004-11-30	SAT, RTi		Clarified issues with the two write
//					methods.
// 2004-12-01	SAM, RTi		* Revert to the old
//					  writeESPTraceEnsembleFile() code and
//					  figure out what the problem is with
//					  that code.
//					* The above method was modifying some
//					  instance data (trimming strings, etc.)
//					  so remove that code - only manipulate
//					  local data when writing.  Also,
//					  hopefully strings are trimmed at read
//					  time.
//					* Use StringUtil.formatString() to pad
//					  strings at write time, thereby
//					  reducing the number of lines of code.
//					* In the read code, where integers are
//					  read as floats, add .01 to each to
//					  make sure that the truncation does not
//					  undercut the value.  This used to be
//					  in the code but was apparently
//					  removed.
// 2005-10-25	SAM, RTi		When reading the binary trace file, open
//					the file in read-only mode.  This allows
//					the code to run properly on read-only
//					CDs and other protected files.  This is
//					not in conflict with the write code,
//					which opens the file separately.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.DMI.NWSRFS_DMI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.Float;
import java.util.Vector;

import RTi.DMI.NWSRFS_DMI.NWSRFS_Util;

import RTi.TS.DateValueTS;
import RTi.TS.HourTS;
import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.TS.TSIterator;

import RTi.Util.IO.DataDimension;
import RTi.Util.IO.DataType;
import RTi.Util.IO.DataUnits;
import RTi.Util.IO.DataUnitsConversion;
import RTi.Util.IO.EndianDataInputStream;
import RTi.Util.IO.EndianDataOutputStream;
import RTi.Util.IO.EndianRandomAccessFile;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;
import RTi.Util.Time.TZ;

/**
This ESPTraceEnsemble class is used to read, create, manage, and write NWS
ESP Trace Ensemble files.  Trace ensemble files are used to store multiple time
series traces and are output by the ESP
program in NWSRFS.  The files are read by ESPADP, TSTool, and other programs.
ESPADP may add additional information (statistics) to the files during
processing but for simple I/O this information can be ignored.
There are two main types of ESP trace files, conditional and historical, as
described below.  In both cases, a trace ensemble stores data for a single
time series type, at a single location.
<pre>
A conditional ESP trace ensemble represents the case where the start of traces
are conditioned to the current states of the system.  Each trace is the result
of applying historical (or synthetic) input, resulting in a variety of
forecasts.  A diagram of a conditional data set, with related internal variables
used in the trace ensemble, is shown below.
ESP traces are in local time (stored in the ensemble header) because ESP focuses
on using historical data and historical data in the calibration time series
files are stored in local time; however, the data from the real-time forecast is
processed database time (Z-time).  ESP makes necessary adjustments to align the
data.

              |
              |
              |
              |    /----------Trace 1 from Hist year i ----------
              |   /
              |  /------------Trace 2 from Hist year i + 1 ------
       /--\   | /-------------Trace N from Hist year N ----------&ltL
    --/    \--|/                                                 
   ^^         ^                                                 ^
   CS         I                                                 E

where:

C =	Carryover date/time, indicated by the Julian day ijdlst and hour ihlst,
	in local standard time.
	This is one time interval before the start of the traces.
S =	Start of the traces, which is one interval later than "C", in local
	standard time.
I =     For the input historical data, the start of the earliest year of
	historical data, indicated by Julian day idarun, in local standard time
	using ihlst.
L =     For the input historical data, the end of the latest year of
	historical data, indicated by Julian day ldarun, in local standard time
	using lhlst.
E =	End of the traces, which is the last hour of the ESP forecast, indicated
	by Julian day ljdlst, and hour lhlst.
E - S + 1 = NCM = the number of months for the traces.  The +1 is needed to
	include the ending months.  Note that an additional
	month will be added if the traces end on the last day of a month and a
	leap year is encountered anywhere in the forecast period.

Therefore, in this class, for a conditional simulation, each trace will have a
period S to E.  Each trace will have a sequence number matching the first year
in the historical year that is input for the trace.
</pre>

<br>
REVISIT SAM 2004-04-06
A historical ESP trace ensemble is ... need to finish this later.  The
conditional simulation is the initial focus.  SAM thinks the only reason ESP
trace files are used for historical data is so that ESPADP can analyze them.
If this is a goal for RTi Java code, we should use TSTool, which already has
some capabilities with any input time series.
<br>
The primary use of this class is as follows (although
<br>
Here is an example of how this class could be used in a program to read in
and process the trace data:
<pre>
String filename = "GRCCH.GRCCH.QINE.06.CS";
ESPTraceEnsemble e;

try {	e = new ESPTraceEnsemble(filename);
	e.writeDateValueFile ( filename + ".dv" );
} catch (Exception ee) {
	// handle the exception
}
</pre>

The format of the ESP trace ensemble file is as follows.  Each record is 124
4-byte words (496 bytes total).
<pre>
REVISIT SAM 2004-04-07 Need to complete this documentation, especially to
correlate the word in the file with the internal data member.  Also document in 
the TSTool appendix - SAM is working on a skeleton.
Record 1 - Identification information

Word  Data (type)      Description
1     version (real)   Binary file format version (initial is 1.0).
</pre>
*/
public class NWSRFS_ESPTraceEnsemble {

// The following constant values were taken from TSIdent.h 
// (on Pamlico at: /awips/hydroapps/lx/rfc/nwsrfs/ens/inc/TSIdent.h )

private final static int ACCUMVAR_MAX = 	0x8;
private final static int ACCUMVAR_MEAN = 	0x20;
private final static int ACCUMVAR_MIN = 	0x10;
private final static int ACCUMVAR_NDIS = 	0x100;
private final static int ACCUMVAR_NDMN = 	0x200;
private final static int ACCUMVAR_NDMX = 	0x400;
private final static int ACCUMVAR_NDTO = 	0x80;
private final static int ACCUMVAR_SUM = 	0x40;

private final static int FUNCTION_EMPIRICAL =     0;
private final static int FUNCTION_LLOGISTIC =     5;
private final static int FUNCTION_LOGNORMAL =     2;
private final static int FUNCTION_LWEIBULL =      4;
private final static int FUNCTION_NORMAL =        1;
private final static int FUNCTION_WAKEBY =        3;
private final static int FUNCTION_WEIBULL =       6;

private final static int MAX_NUM_TRACE_IN_ENS = 100;

private final static int SIMFLAG_UNKNOWN = -1;
private final static int SIMFLAG_CONDITIONAL = 3;
private final static int SIMFLAG_HISTORICAL = 1;

private final static int VARTYPE_ACCUM = 	0x4;
private final static int VARTYPE_INST = 	0x1;
private final static int VARTYPE_MEAN = 	0x2;
private final static int VARTYPE_NONE = 	0x800;

// Traces are stored as an array of HourTS, where each time series has complete
// standard headers.  To differentiate traces, the TS sequence number is set
// to the historical year (for conditional traces).  Unlike the NWS code, the
// TSIdent that is used here is the standard TSIdent and is not bloated with
// extra ESP data members.  The data members below have been named similar to
// original code to allow comparison; however, there are some differences,
// either because the original varied between code (FORTRAN and C++), because
// the original was too obscure, or because the new data members don't have
// quite the same meaning.
//
// Although there is a constructor that takes a PropList, the current approach
// is to use independent data members and not a PropList for data, in order
// to facilitate comparison with other code and optimize performance.

// The preceeding constant values were taken from TSIdent.h 
// (on Pamlico at: /awips/hydroapps/lx/rfc/nwsrfs/ens/inc/TSIdent.h )

// Data members used in the Java with no equivalent in the C++ ESPTraceEns
// or TSIdent classes...

private String __filename;		// The name opened by this class.  Note
					// that this is different from the
					// __espfname read from the ESP trace
					// ensemble itself.
private EndianRandomAccessFile __traceRAF;	// The object that will open and read
					// from the ESP file.
private boolean __traceRAFOpen;		// Test to see whether or not the Random
					// Access File __traceRAF is open or
					// not.
private NWSRFS_DMI __dmi;		// An NWSRFS_DMI object used to read
					// and write the trace ensemble.
private boolean __big_endian = true;	// Indicate whether the input that is
					// read is big- or little-endian.  The
					// value is set by evaluating header
					// data.

// The following data members correspond to the C++ ESPTraceEns class
// definition (other than TSIdent, which is a separate section below)...

private int __adjcount = 0;		// Adjusted time series counter
private int __calibration_flag = 0;	// Calibration flag
private String __cg = "";		// Carryover group identifier.
private boolean __data_read = false;	// Set to true if the data section is
					// read.
private String __dim = "";		// Dimension for time series units
					// (__ts_unit).
private int __error_model_flag = 0;	// Error model flag.
private String __espfname = "";		// The name of the original trace file
					// (does not include the full path).
private String __esptext = "";		// User comments.
private String __fg = "";		// Forecast group identifier.
private float __format_ver = (float)0.0;// File format version (1.0 is first).
private String __hclfile = "";		// HCL file name.
private boolean __hdr_read = false;	// Set to true if the header is read.
private int __idarun = 0;		// Initial Julian day of historical run
					// period - this corresponds with the
					// carryover date for the current run
					// adjusted so the year is the first
					// year of historical data that will be
					// run (first trace date)
private int __ijdlst = 0;		// Initial Julian day of current
					// forecast period - corresponding to
					// the carryover date used for the run
					// (carryover day).
private int __ihlst = 0;		// Initial hour of the current forecast
					// period (1-24) (Carry over hour).
private DateTime __carryover_date =null;// NWS __hdr_id carryover date?
					// Carryover date in local time,
					// determined from __ijdlst and __ihlst
private DateTime __start_date = null;	// NWS __hdr_id start date.
					// One interval after __carryoverydate.
private int __im = 0;			// Month of __idarun.
private int __irec = 2;			// The record number (base 1, not 0) at
					// which the first data record can be
					// found (normally = 2).
private int __iy = 0;			// Year of __idarun, in the time zone
					// specified by __nlstz.
private int __ldarun = 0;		// Last Julian day of historical run
					// period - this corresponds with the
					// last day of the forecast period
					// adjusted so the year is the last
					// year of historical data that will be
					// run (last trace date), in the time
					// zone specified by __nlstz
private int __lhlst = 0;		// Last hour of the current forecast
					// period (1-24) (last hour of
					// forecast), in the time zone specified
					// by __nlstz.
private int __ljdlst = 0;		// Last Julian day of the current
					// forecast period (last day of
					// forecast), in the time zone specified
					// by __nlstz.
private DateTime __end_date = null;	// NWS __hdr_id end date.
					// Forecast end date, in local time,
					// determined from __lhlst and __ljdlst.
private int __ncm = 0;			// The number of conditional months in
					// the file - the number of months
					// during which forecasting is taking
					// place.  If forecasting is from Apr 29
					// to May 5, __ncm = 2.
private int __nlstz = 0;		// Time zone number of local standard
					// time, which is the time zome for
					// date data (see other comments).
private int __noutds = 0;		// Time zone number of local standard
private int [] __now = null;		// Time that the ESP trace file was
					// written.
private int __headerLength = 0;		// The length of the ESP trace header.
private int __n_traces = 0;		// The number of traces in the ensemble
					// file.
private int __prsf_flag = 0;		// PRSF mode
private int __rec_words = 124;		// The length of records in the file,
					// in 4-byte words (typically 124 to
					// allow for 31 days of 6-hour data) -
					// will adjust based on version later
					// if necessary.
private String __segdesc = "";		// Segment description.
private String __seg_id = "";		// Segment identifier.
private int __simflag;			// The simulation flag for the file.
					// See the SIMFLAG_* constants above for
					// possible values.
private TZ __time_zone = null;		// Time zone information.
private HourTS[] __ts;			// Array of HourTS, one for each trace.
private String __tscale = "";		// Time scale for data (e.g., "ACCM").
private int __ts_dt;			// The Time Series time interval for the
					// data in the file (hours).
private String __ts_id = "";		// Time series identifier (external
					// location identifier)
private String __ts_type = "";		// Time series data type
private String __ts_unit = "";		// Time series data units
private String __user = "";		// User creating file
private float __xlat = (float)0.0;	// Latitude of segment in decimal
					// degrees (forecast point?)
private float __xlong = (float)0.0;	// Longitude of segment in decimal
					// degrees (forecast point?)

// The following correspond to the ESP C++ TSIdent class, which apparently was
// changed from the original RTi design to include much more than basic
// identification data.  Without changing the normal RTi TSIdent, and still
// store all the information in the NWS code, use an instance of the normal
// TSIdent and other data whose names are prefixed with __hdr_id.  Only include
// data members that seem to be necessary here (not all the NWS TSIdent data
// are needed in this class now).
//
// Data that are redundant between the ESPTraceEns and TSIdent class are only
// stored above (e.g., __xlat).  Where there is potential confusion (e.g., with
// dates, a copy is stored in the header data below).

// REVISIT SAM 2004-04-07 Can the following be renamed or be phased out?  Some
// of this seems to be redundant with the above and may be used only by ESPADP
// during accumulations.

private TSIdent __hdr_id = null;		// Standard RTi TSIdent.
private float __hdr_id_accumcrit = (float)0.0;	// NWS __hdr_id accumulation
						// criteria
private int __hdr_id_accumdir = 0;		// NWS __hdr_id accumulation
						// direction?
private int __hdr_id_accumvar = 0;		// NWS __hdr_id accumulation
						// variable?
private DateTime __hdr_id_creationdate = null;	// NWS __hdr_id creation date
private DateTime __hdr_id_enddate_orig = null;	// NWS __hdr_id end date
						// (original)
private DateTime __hdr_id_exceedProbDate = null;// NWS __hdr_id exceendance
						// probability date
private int __hdr_id_data_interval_base = 0;	// NWS __hdr_id interval base
private int __hdr_id_data_interval_base_orig=0;	// NWS __hdr_id orig interval
						// base
private int __hdr_id_data_interval_mult = 0;	// NWS __hdr_id interval
						// multiplier
private int __hdr_id_data_interval_mult_orig=0;	// NWS __hdr_id orig interval
						// multiplier
private float __hdr_id_missing = -999;		// NWS __hdr_id missing data val
private int __hdr_id_nRanges = 0;		// NWS __hdr_id # of probability
						// ranges
private int __hdr_id_probFunction = 0;		// NWS __hdr_id probability
						// function
private float[] __hdr_id_probRanges = null;	// NWS __hdr_id probability
						// ranges
private String __hdr_id_rfcname = "";		// NWS __hdr_id RFC identifier
private DateTime __hdr_id_startdate_orig = null;// NWS __hdr_id start date
						// (original)
private String __hdr_id_units_orig = null;	// NWS __hdr_id original units 
private int __hdr_id_vartype = 0;		// NWS __hdr_id variable type
private int __hdr_id_vartype_orig = 0;		// NWS __hdr_id variable type
						// (original)

/**
Construct an NWSRFS_ESPTraceEnsemble by reading an existing file.  The file is closed
after the data are read.
@param filename the file to open and read from.
@param read_data If true, read all the data.  If false, only read the file
header.
@exception Exception if there is an error reading the file.
*/
public NWSRFS_ESPTraceEnsemble ( String filename, boolean read_data ) 
throws Exception
{
	this ( filename, read_data, false );
}

/**
Construct an NWSRFS_ESPTraceEnsemble by creating a new NWSRFS_DMI.  The DMI can
be left open or be closed after the read.
after the data are read.
@param filename the file to open and read from.
@param read_data If true, read all the data.  If false, only read the file
header.
@param remain_open If true, the DMI will remain open after reading the
header.
@exception Exception if there is an error reading the file.
*/
public NWSRFS_ESPTraceEnsemble (	String filename, boolean read_data,
					boolean remain_open ) 
throws Exception
{	
	// Local variables
	int i;
	String routine = "NWSRFS_ESPTraceEnsemble";

	__filename = IOUtil.getPathUsingWorkingDir(filename);

	initialize();

	// REVISIT SAM 2004-12-01 I do not see why this is needed!  The read
	// and write can occur independent of the DMI.

	// Create a limited NWSRFS_DMI to do the read and write.
	__dmi = new NWSRFS_DMI();
//	__big_endian = __dmi.getIsBigEndian();

	readHeader();
	if ( read_data ) {
		readData();
	}

	// Close the file...

	if ( !remain_open ) {
		__dmi.close();
	}
}

/**
Construct an NWSRFS_ESPTraceEnsemble by reading an existing NWSRFS_DMI.  The DMI can be
left open or be closed after the read.
after the data are read.
@param filename the file to open and read from.
@param dmi an existing NWSRFS_DMI to do the read and writes.
@param read_data If true, read all the data.  If false, only read the file
header.
@param remain_open If true, the DMI will remain open after reading the
header.
@exception Exception if there is an error reading the file.
*/
public NWSRFS_ESPTraceEnsemble (	String filename, NWSRFS_DMI dmi,
					boolean read_data,
					boolean remain_open ) 
throws Exception
{	
	// Local variables
	int i;
	String routine = "NWSRFS_ESPTraceEnsemble";
	
	__filename = IOUtil.getPathUsingWorkingDir(filename);

	initialize();

	// Create a copy of the exiting NWSRFS_DMI to do the read and write.
	__dmi = new NWSRFS_DMI(dmi);
//	__big_endian = __dmi.getIsBigEndian();

	readHeader();
	if ( read_data ) {
		readData();
	}

	// Close the file...
	if ( !remain_open ) {
		__dmi.close();
	}
}

// REVISIT SAM 2004-04-07 - probably need to add more properties below to
// set all the data that are needed.

/**
Construct a conditional simulation NWSRFS_ESPTraceEnsemble from a Vector of HourTS,
which are assumed to have a consistent period where the start of each time
series is the start of the forecast period (one interval after the carryover
date/time), and the end of each time series is the end of the forecast period.
The period is taken from the first time series with variations due to leap
year resulting in truncation of the period for some time series - in all cases
the data is preserved as sequential, regardless of the actual dates.  Each time
series must have its sequence number (see TS.setSequenceNumber()) set to the
historical year used to produce the trace) - this information is used to compute
the "idarun", "im", "iy", and "ldarun" values.  The time series should
be in order of the historical years and the number of traces should be
consistent with the range of sequence numbers (historical years).  After
constructing, use the writeESPTraceEnsembleFile() method to write a conditional
ESP trace ensemble file.
@param tslist The Vector of TS to place in the ensemble.  As much information as
possible is taken from the time series, but can be specified as properties, as
described below.
@param props Properties for the ensemble.
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td>CarryoverGroup</td>
<td>Carryover group identifier.</td>
<td>Blank.</td>
</tr>

<tr>
<td>ForecastGroup</td>
<td>Forecast group identifier.</td>
<td>Blank.</td>
</tr>

<tr>
<td>Latitude</td>
<td>Latitude for the station.</td>
<td>0.0.</td>
</tr>

<tr>
<td>Longitude</td>
<td>Longitude for the station.</td>
<td>0.0.</td>
</tr>

<tr>
<td>RFC</td>
<td>River Forecast Center name.</td>
<td>Blank.</td>
</tr>

<tr>
<td>Segment</td>
<td>The segment identifier.</td>
<td>The location part of the time series identifier for the first time series in
the list.</td>
</tr>

<tr>
<td>SegmentDescription</td>
<td>The segment description.</td>
<td>The description of the first time series in the list.</td>
</tr>

</table>
@exception Exception if there is an error constructing the ensemble.
*/
public NWSRFS_ESPTraceEnsemble ( Vector tslist, PropList props ) 
throws Exception
{	
	String routine = "NWSRFS_ESPTraceEnsemble";
	initialize();	// Mostly blanks, conditional settings.

	// Create a limited NWSRFS_DMI to do the write.
	__dmi = new NWSRFS_DMI();

	// Make the value of the dmi's endianess Big Endian to write the trace
	__big_endian = true;
//	__big_endian = __dmi.getIsBigEndian();

	// Save the array of time series that are part of the trace ensemble...
	int size = 0;
	if ( tslist != null ) {
		size = tslist.size();
	}
	__ts = new HourTS[size];
	for ( int i = 0; i < size; i++ ) {
		__ts[i] = (HourTS)tslist.elementAt(i);
	}

	// Transfer properties to internal data...

	if ( props == null ) {
		props = new PropList ( "ESP" );
	}

	// List in general order of data in the trace file...

	// __format_ver set in the initialize() method.
	__seg_id = __ts[0].getLocation();
	String prop_val = props.getValue ( "Segment" );
	if ( prop_val != null ) {
		__seg_id = prop_val;
	}
	__ts_id = __ts[0].getLocation();
	__ts_type = __ts[0].getDataType();
	// __hdr_id_data_interval_mult_orig is used in the write method.
	__ts_dt = __hdr_id_data_interval_mult_orig =
		__ts[0].getDataIntervalMult();
	__simflag = SIMFLAG_CONDITIONAL;
	__ts_unit = __ts[0].getDataUnits();
	// __now set in the initialize() method.
	// Time zone and daylight savings flag default to zero unless passed
	// in from properties...
	__nlstz = 0; // hardcoded for testing
	__noutds = 0;
	if ( !__ts[0].getDate1().getTimeZoneAbbreviation().equals("") ) {
		try {	TZ tz = TZ.getDefinedTZ (
				__ts[0].getDate1().getTimeZoneAbbreviation() );
			__nlstz = tz.getZuluOffsetMinutes()/60;
			__noutds = tz.getDSFlag();
		}
		catch ( Exception e ) {
			// For now treat as non-fatal and treat as Zulu, as
			// intialized above...
			Message.printWarning ( 2, routine,
			"Unable to determine time zone from \"" +
			__ts[0].getDate1().getTimeZoneAbbreviation() +
			"\" - assuming Zulu." );
		}
	}
	// Need to set the date information at once to be able to make sense
	// of things.
	__start_date = new DateTime (__ts[0].getDate1());
	// REVISIT SAM 2004-11-29 Why is this commented out - can it be removed?
	//	__start_date.addHour ( __nlstz+__noutds );	// One interval less than start

	__carryover_date = new DateTime ( __start_date );
	__carryover_date.addHour ( -__ts_dt );	// One interval less than start
	DateTime start_date24 = NWSRFS_Util.toDateTime24(__start_date, true);
	__iy = __ts[0].getSequenceNumber();
	__im = start_date24.getMonth();
	DateTime carryover_date24 = NWSRFS_Util.toDateTime24(
		__carryover_date, true);
	Message.printStatus ( 2, routine, "Carryover date (24 local) = " +
		carryover_date24 );
	Message.printStatus ( 2, routine, "Start date (24 local) = " +
		start_date24 );
	__end_date = new DateTime ( __ts[0].getDate2() );
	// REVISIT SAM 2004-11-29 Why is this commented out - can it be removed?
	//	__end_date.addHour ( __nlstz+__noutds );	// One interval less than start

	DateTime end_date24 = NWSRFS_Util.toDateTime24(__end_date, true);
	Message.printStatus ( 2, routine, "End date (24 local) = " +
		end_date24 );
	__ihlst = carryover_date24.getHour();
	__lhlst = end_date24.getHour();
	int [] j = NWSRFS_Util.julda ( start_date24.getMonth(),
		start_date24.getDay(), __ts[0].getSequenceNumber(), __ihlst );
	__idarun = j[0];
	// Determine the number of conditional months
	// = number of months in historical data needed to store the forecast
	//	period's data, allowing for leap year (this assumes that the
	//	forecast period will not be long enough to require more than
	//	one additional month of data).
	// = number of months in end_date24 - start_date24
	//  +1 if end_date24 is the last day of the month and the period
	//  includes a leap year (the additional month is needed to cover for
	//  the required leap day in the forecast period).
	// Below the +1 is to make the count inclusive...
	__ncm = end_date24.getAbsoluteMonth() -
		start_date24.getAbsoluteMonth() + 1;
	// Now check for the leap year.  Use dates with precision of month to
	// avoid hour 23 issue and increase performance...
	DateTime date = new DateTime(start_date24);
	date.setPrecision(DateTime.PRECISION_MONTH);
	if ( end_date24.getDay() == TimeUtil.numDaysInMonth(end_date24) ) {
		for (	; date.lessThanOrEqualTo(end_date24); date.addMonth(1)){
			if (	(date.getMonth() == 2) &&
				TimeUtil.isLeapYear(date.getYear()) ) {
				++__ncm;
				break;
			}
		}
	}
	j = NWSRFS_Util.julda ( carryover_date24.getMonth(),
		carryover_date24.getDay(),
		carryover_date24.getYear(), __ihlst );
	__ijdlst = j[0];

	j = NWSRFS_Util.julda ( end_date24.getMonth(),
		end_date24.getDay(), end_date24.getYear(), __lhlst );
	__ljdlst = j[0];

	DateTime ldarun_date = new DateTime ( DateTime.PRECISION_MONTH );
	ldarun_date.setYear ( __ts[__ts.length - 1].getSequenceNumber() );
	ldarun_date.setMonth( __im );
	ldarun_date.addMonth ( __ncm - 1 );

	j = NWSRFS_Util.julda ( ldarun_date.getMonth(),
		end_date24.getDay(), ldarun_date.getYear(), __lhlst );
	__ldarun = j[0];

	// Simply the number of time series traces...
	__n_traces = size;
	try {	DataUnits units = DataUnits.lookupUnits ( __ts_unit );
		DataDimension dim = units.getDimension();
		__dim = dim.getAbbreviation();
	}
	catch ( Exception e ) {
		// Ignore.
	}
	try {	DataType dtype = DataType.lookupDataType ( __ts_type );
		__tscale = dtype.getMeasTimeScale();
	}
	catch ( Exception e ) {
		// Ignore.
	}

	// Check for data type, data units, and data dimension in PropList 
	prop_val = props.getValue ( "DataType" );
	if ( prop_val != null ) {
		__ts_type = prop_val;
		try 
		{	
			DataType dtype = DataType.lookupDataType ( __ts_type );
			__tscale = dtype.getMeasTimeScale();
		}
		catch ( Exception e ) 
		{
			// Ignore.
		}
	}

	prop_val = props.getValue ( "DataUnit" );
	if ( prop_val != null ) {
		__ts_unit = prop_val;
		try 
		{
			DataUnits units = DataUnits.lookupUnits ( __ts_unit );
			DataDimension dim = units.getDimension();
			__dim = dim.getAbbreviation();
		}
		catch ( Exception e ) {
			// Ignore.
		}
	}

	prop_val = props.getValue ( "DataDimension" );
	if ( prop_val != null ) {
		__dim = prop_val;
	}

	__segdesc = __ts[0].getDescription();
	prop_val = props.getValue ( "SegmentDescription" );
	if ( prop_val != null ) {
		__segdesc = prop_val;
	}
	prop_val = props.getValue ( "Latitude" );
	if ( (prop_val != null) && StringUtil.isDouble(prop_val) ) {
		__xlat = StringUtil.atof(prop_val);
	}
	prop_val = props.getValue ( "Longitude" );
	if ( (prop_val != null) && StringUtil.isDouble(prop_val) ) {
		__xlong = StringUtil.atof(prop_val);
	}
	prop_val = props.getValue ( "ForecastGroup" );
	if ( prop_val != null ) {
		__fg = prop_val;
	}
	prop_val = props.getValue ( "CarryoverGroup" );
	if ( prop_val != null ) {
		__cg = prop_val;
	}
	prop_val = props.getValue ( "RFC" );
	if ( prop_val != null ) {
		__hdr_id_rfcname = prop_val;
	}
	// ESPFname assigned at write time
	// Others not important so leave as defaults
}

/**
Convert an NWSRFS_ESPTraceEnsemble file to a human-readable text file.  This is useful
for debugging.
@param esp_filename the file to open and read from.
@param txt_filename the file to create.
@param out_units Units for output, or null if no conversion is desired.
@exception Exception if there is an error.
*/
public static void convertESPTraceEnsembleToText (	String esp_filename,
							String txt_filename,
							String out_units ) 
throws Exception
{	
	// Local variables
	String routine =
		"NWSRFS_ESPTraceEnsemble.convertESPTraceEnsembleToText";
	String full_fname = IOUtil.getPathUsingWorkingDir(esp_filename);
	double mult;
	double add;

	// Open the ESP file and read in the header and data, storing the 
	// information in this class.  

	NWSRFS_ESPTraceEnsemble esp = new NWSRFS_ESPTraceEnsemble ( full_fname,
		true, true);

	// Set message logfile
			
	// Read header info
	esp.readHeader();

	// Read data
	esp.readData();

	// Open the output file...
	full_fname = IOUtil.getPathUsingWorkingDir(txt_filename);
	PrintWriter out = new PrintWriter ( new FileOutputStream ( full_fname));

	// Get the conversion information to output units...

	if ( (out_units == null) || (out_units.length() == 0) )
	{
		mult = 1.0;
		add = 0.0;
	}
	else
	{
		DataUnitsConversion conv =
			DataUnits.getConversion(esp.getDataUnits(), out_units);
		mult = conv.getMultFactor();
		add = conv.getAddFactor();
	}

	// Print the header in the order of the file (some get methods are
	// not enabled)...

	Vector header_strings = esp.getHeaderStrings ( null );
	int size = header_strings.size();
	for ( int i = 0; i < size; i++ ) {
		out.println ( (String)header_strings.elementAt(i) );
	}
	out.println (
	"Note:  Output below has been converted from \"" +
		esp.getDataUnits() + " to \"" + out_units + "\"");
	out.println (
	"Note:  The first column below shows the start year of the historical "+
	"trace and the corresponding start of month's date in forecast time).");

	try
	{
		// Call the method to do the convert
		esp.convertESPTraceEnsembleData(out,mult,add);
	}
	catch(Exception e)
	{
		throw e;
	}

	// Close the output file...

	out.flush();
	out.close();
}

/**
Convert an NWSRFS_ESPTraceEnsemble file to a human-readable text file.  This is
useful for debugging.
@param out PrintWriter to write to the txt file.
@param mult multiplier for conversion if desired.
@param add addition for conversion.
@exception Exception if there is an error.
*/
protected void convertESPTraceEnsembleData (	PrintWriter out,
						double mult,
						double add)
throws Exception
{	
	// Local variables
	String routine = "NWSRFS_ESPTraceEnsemble.convertESPTraceEnsembleData";


	// Output the data records in bulk fashion and print to the output.
	// Don't worry about knowing the exact number of records.  Just output
	// until there is no more data...

	int i, ndata = __rec_words/4;	// Floats per line - should be 31
	int nrecpermonth = (ndata/31)*(24/__ts_dt);
	float [] data = new float[744];	// Enough for 31 days x 24 1-hour
					// values...
	int icm;	// Loop counter for conditional months in each trace.
	int y1 = getIy();
	int ndays;	// Number of days per month.
	int idata;	// Position in data array for month
	int ntran;	// Number of data to transfer for a month's data
	int ntran2;	// Number of data to transfer for a full month's data
	DateTime date;	// Date/time to used to transfer data array to time
			// series.
	DateTime hdate;	// Date/time to used to evaluate a historical date/time.
	float missing_float = (float)-999.0;
	try {
	// Loop through the number of time series traces...
	for ( int its = 0; its < __n_traces; its++ ) {
		// Initialize the date that will be used to transfer data to
		// the starting interval in the data file.
		// The dates in the file use the hour 1-24.  However,
		// the time series have been allocated in readHeader()
		// using hour 0-23.  Therefore, the starting date/time
		// must be properly set.  Each month of data in the file
		// corresponds to hour __ts_dt of the first day of the
		// HISTORICAL month, which will only be an issue if
		// __ts_dt == 24.  Take care to
		// set the starting date correctly and then just add the
		// interval as the data are processed.
		// First determine the hour 24 date/time, mainly to get
		// the correct month, and year.
		// Start by setting to the initial value...
		date = NWSRFS_Util.toDateTime24(__start_date,true);
		// Set the day to 1 and the hour to the interval...
		date.setDay ( 1 );
		date.setHour ( __ts_dt );
		// Avoid the time zone since it adds more to the output and
		// time zone is already listed in the header output...
		date.setTimeZone ( "" );
		// Convert back to 0-23 hour...
		date = NWSRFS_Util.toDateTime23(date,true);
		// Loop through the number of conditional months (the month is
		// incr...
		for ( icm = 0; icm < __ncm; icm++ ) {
			// Loop through the records in the month...
			// Transfer a complete month into the data array.
			// Determine the number of values available in the file
			// to be transferred.  The months in the file
			// correspond to the historical months, not the
			// real-time forecast years.  Therefore, for example,
			// if the forecast period is May 2002 through May 2004
			// but the starting historical years are 1995 - 1998
			// (4 traces), the second trace (historical years
			// 1996-1997) will have 28 days in February in the data
			// file, even though 2004 in the forecast period has 29.
			// Therefore, calculate the number of data values in
			// the file based on the historical year and only
			// increment the date for the time series as values are
			// transferred.
			hdate = new DateTime(DateTime.PRECISION_MONTH);
			// Set the year to the historical year...
			hdate.setYear ( __iy + its );
			hdate.setMonth ( __im );
			// Now add the number of months that have been
			// processed...
			hdate.addMonth ( icm );
			// Now get the number of days in the month.  This does
			// not look at the hour so an hour of 24 is OK...
			ndays = TimeUtil.numDaysInMonth ( hdate );
			// Now compute the number of data that will need to be
			// transferred.  It may be less than the number read
			// because of the number of days in the month...
			ntran = ndays*24/__ts_dt;
			// Now loop through the data, using the actual 0-23
			// hour and the number of intervals.  It is OK to
			// attempt transferring data outside the actual TS
			// period because data outside the period will be
			// ignored (and should be missing).
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine,
				"Transferring " + ndays + " days, " + ntran +
				" values for historical " + hdate +
				" starting at " + date );
			}
			for (	idata = 0; idata < ntran;
				idata++, date.addHour(__ts_dt) ) {
				data[idata] =
				(float)__ts[its].getDataValue(date);
			}
			// Fill in the rest of the array if necessary...
			ntran2 = 31*24/__ts_dt;
			for ( ; idata < ntran2; idata++ ) {
				data[idata] = missing_float;
			}
			idata = 0;	// Reset array position.
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine,
				"Writing trace [" + its + "] " +
				__ts[its].getSequenceNumber() +
				" conditional month [" + icm + "]" );
			}
			for (	int ir = 0; ir < nrecpermonth; ir++ ) {
				if ( ir == 0 ) {
					// Print the header information for the
					// month (sequence year and date for
					// trace data)...
					out.print ( "" + (y1 + its) + " " +
					date );
				}
				else {	// Other data records for month...
					out.print ( "                  " );
				}
				// Loop through the data in the month,
				// incrementing the hour to assign the data...
				for ( i = 0; i < ndata; i++ ) {
					if (	(data[idata] < -997.9) &&
						(data[idata] > -999.1)){
						// Probably a missing data
						// value...
						data[idata]=-999;
						out.print (
						StringUtil.formatString(
						data[idata],"%10.2f") );
					}
					else {	// Probably real data so
						// convert...
						out.print (
						StringUtil.formatString(
						(data[idata]*mult + add),
						"%10.2f") );
					}
					idata++;
				}

				// Print the newline...
				out.println ();
			}
		}
	}
	}
	catch ( Exception e ) {
		// Should not happen if loops above are correct...
		Message.printWarning ( 2, routine, "Unexpected write error." );
		Message.printWarning ( 2, routine, e );
		throw new Exception ( "Unexpected write error" );
	}
}

/** Finalize the object.
*/
public void finalize()
{
}

/**
Return the data interval in hours.
@return the data interval in hours.
*/
public int getDataIntervalHours ()
{	return __ts_dt;
}

/**
Return the data units.
@return the data units.
*/
public String getDataUnits ()
{	return __ts_unit;
}

/**
Return the forecast start date/time (one interval after the carryover date).
@return the forecast start date/time (one interval after the carryover date).
*/
public DateTime getForecastStart ()
{	return __start_date;
}

/**
Utility method to return property-like strings for time series comments,
debugging, etc.
@return a Vector of String that can be printed or added to time series comments.
@param tsin Time series to format strings for, or null for general ensemble
strings.
*/
protected Vector getHeaderStrings ( TS tsin )
{	Vector strings = new Vector ();
	strings.addElement("Values from ESP Trace Ensemble Header:");
	strings.addElement("FormatVersion = " + __format_ver );
	if ( tsin == null ) {
		StringBuffer b = new StringBuffer ("SequenceNumber =");
		for ( int i = 0; i < __n_traces; i++ ) {
			b.append ( " " + __ts[i].getSequenceNumber() );
		}
		strings.addElement(b.toString() );
	}
	else {	strings.addElement("SequenceNumber = "+
		tsin.getSequenceNumber() );
	}
	strings.addElement("Segment = \"" + __seg_id + "\"");
	strings.addElement("SegmentDescription = \""+__segdesc+"\"");
	strings.addElement("Location = \"" + __ts_id + "\"");
	strings.addElement("CarryoverGroup = \"" + __cg + "\"");
	strings.addElement("ForecastGroup = \"" + __fg + "\"");
	strings.addElement("DataType = \"" + __ts_type + "\"");
	strings.addElement("Interval = " + __ts_dt );
	strings.addElement("SimFlag = " + __simflag );
	strings.addElement("DataUnits = \"" + __ts_unit + "\"");
	strings.addElement("CreationDate = \"" + __hdr_id_creationdate + "\"");

	strings.addElement("idarun = " + __idarun );
	//strings.addElement("idarunParts = " +
		//start_year + "-" + start_month + "-" + start_day );
	strings.addElement("im = " + __im );
		strings.addElement("iy = " + __iy );
	//strings.addElement("start_date = " + start_date );
	if ( tsin != null ) {
		strings.addElement("TSDate1 = " + tsin.getDate1() );
	}
	strings.addElement("ldarun = " + __ldarun );
	//strings.addElement("ldarunParts = " +
		//StringUtil.formatString(end_year,"%4d") + "-" +
		//StringUtil.formatString(end_month,"%02d") + "-" +
		//StringUtil.formatString(end_day,"%02d") );
	//strings.addElement("end_date = " + end_date );
	if ( tsin != null ) {
		strings.addElement("TSDate2 = " + tsin.getDate2() );
	}

	strings.addElement("ijdlst = " + __ijdlst );
	strings.addElement("ihlst = " + __ihlst );
	strings.addElement("CarryoverDateLocal = "+
		NWSRFS_Util.toDateTime24(__carryover_date,false));
	strings.addElement("ForecastStartLocal = "+
		NWSRFS_Util.toDateTime24(__start_date,false));

	strings.addElement("ljdlst = " + __ljdlst );
	strings.addElement("lhlst = " + __lhlst );
	strings.addElement("ForecastEndLocal = "+
		NWSRFS_Util.toDateTime24(__end_date,false));

	strings.addElement("NumTraces = " + __n_traces );
	strings.addElement("NCM = " + __ncm );
	strings.addElement("TimeZone = " + __nlstz);
	strings.addElement("noutds = " + __noutds);
	strings.addElement("Irec = " + __irec);
	strings.addElement("Dimension = \"" + __dim + "\"");

	strings.addElement("MeasTimeScale = \"" + __tscale + "\"");
	strings.addElement("Latitude = " + __xlat);
	strings.addElement("Longitude = " + __xlong);
	
	strings.addElement("RFC = \"" + __hdr_id_rfcname + "\"");
	
	strings.addElement("PRSFFlag = " + __prsf_flag );
	strings.addElement("UserComments = \"" + __esptext + "\"");

	// REVISIT SAM 2004-04-07 - need to evaluate whether the following
	// make sense or just make the information more confusing.

	/* Extra stuff that may not be needed
	strings.addElement("Date1: '" + startDate + "'");
	strings.addElement("Date1 Orig: '" + startDate + "'");
	strings.addElement("Date2: '" + endDate + "'");
	strings.addElement("Date2 Orig: '" + endDate + "'");
	
	strings.addElement("adjcount: " + adjcount);
	strings.addElement("CreationDate: '" + creationDate + "'");
	strings.addElement("CarryoverDate: '" + carryoverDate+ "'");
	strings.addElement("ForecastEndDate: '" 
		+ forecastEndDate + "'");	
	strings.addElement("ExceedProbDate: '" + startDate + "'");
	
	strings.addElement("ProbFunction: " + FUNCTION_EMPIRICAL);
	strings.addElement("NRanges: 3");
	
	strings.addElement("IntervalOrig: '" + TimeInterval.HOUR
		+ "', '" + tsInterval + "'");
	*/
	return strings;
}

/**
Return the initial Julian day of the historical run period.
@return the initial Julian day of the historical run period.
*/
public int getIdarun ()
{	return __idarun;
}

/**
Return the initial hour of the forecast period.
@return the initial hour of the forecast period.
*/
public int getIhlst ()
{	return __ihlst;
}

/**
Return the last Julian day of the forecast period.
@return the last Julian day of the forecast period.
*/
public int getIjdlst ()
{	return __ijdlst;
}

/**
Return the record number of the first trace data record.
@return the record number of the first trace data record.
*/
public int getIrec ()
{	return __irec;
}

/**
Return the starting year for the first trace (historical year of input).
@return the starting year for the first trace (historical year of input).
*/
public int getIy ()
{	return __iy;
}

/**
Return the initial Julian day of the forecast period.
@return the initial Julian day of the forecast period.
*/
public int getLdarun ()
{	return __ldarun;
}

/**
Return the last hour of the forecast period.
@return the last hour of the forecast period.
*/
public int getLhlst ()
{	return __lhlst;
}

/**
Return the last Julian day of the forecast period.
@return the last Julian day of the forecast period.
*/
public int getLjdlst ()
{	return __ljdlst;
}

/**
Return the number of conditional months (ncm).
@return the number of conditional months (ncm).
*/
public int getNcm ()
{	return __ncm;
}

/**
Return the number of traces.
@return the number of traces.
*/
public int getNTraces ()
{	return __n_traces;
}

/**
Return the EndianRandomAccessFile that is being used with the ESPTraceEnsemble.
@return the EndianRandomAccessFile that is being used with the ESPTraceEnsemble.
*/
public RandomAccessFile getEndianRandomAccessFile ()
{	return __traceRAF;
}

/**
Return the record length in 4-byte words.
@return the record length in 4-byte words.
*/
public int getRecordWords ()
{	return __rec_words;
}

/**
Return the array of time series maintained in the ensemble.
@return the array of time series maintained in the ensemble, or null if no
time series.
*/
public TS[] getTimeSeries ()
{	return __ts;
}

/**
Return the time series maintained in the ensemble as a Vector.
@return the time series maintained in the ensemble as a Vector, or null if
no time series.
*/
public Vector getTimeSeriesVector ()
{	Vector v = new Vector(__n_traces);
	for (int i = 0; i < __n_traces;i++) {
		v.add(__ts[i]);
	}
	return v;
}

/**
Initialize the instance data.  This is similar to the C++ ESPTraceEns.init()
method.
*/
private void initialize ()
{	
	__headerLength = 496;
	__n_traces = 0;
	__traceRAF = null;
	__traceRAFOpen = false;
	__data_read = false;
	__hdr_read = false;
	__rec_words = 124;	// ORIGINAL set to zero - why not 124?
	__seg_id = "";
	__ts_id = "";
	__ts_type = "";
	__ts_dt = 0;
	// REVISIT SAM 2004-04-07 - what really is the meaning of the version?
	// Can we document the differences in the file contents and make the
	// code handle appropriately?
	__format_ver = (float)1.01;	// Value used in examples as of
					// 2003-12-14
	__simflag = SIMFLAG_CONDITIONAL;
	__ts_unit = "";
	__now = new int[5];
	DateTime now = new DateTime(DateTime.DATE_CURRENT );
	__now[0] = now.getMonth();
	__now[1] = now.getDay();
	__now[2] = now.getYear();
	__now[3] = now.getHour()*100 + now.getMinute();
	__now[4] = now.getSecond()*100 + now.getHSecond();
	__im = 0;
	__iy = 0;
	__idarun = 0;
	__ldarun = 0;
	__ijdlst = 0;
	__ihlst = 0;
	__ljdlst = 0;
	__lhlst = 0;
	__ncm = 0;
	__nlstz = 0;
	// REVISIT
	//__yrwtrec = 0;
	__irec = 2;	// Default record for first data line 
	__espfname = "";
	__user = "";
	__hclfile = "";
	__esptext = "";
	__adjcount = 0;
	__calibration_flag = 0;
	__error_model_flag = 0;
	__prsf_flag = 0;	// Default for no PRSF.
	// REVISIT - not ported from C+...
	//_histArray = new float* [MAX_TOTAL];
	__xlat = (float)0.0;
	__xlong = (float)0.0;

	__time_zone = new TZ();

}

/**
Read ensemble data from the ESP trace ensemble file.  The file must already be
opened.  The time series data space for each trace is allocated and filled with
data.
*/
private void readData() 
throws Exception
{	String routine = "NWSRFS_ESPTraceEnsemble.readData";

	if ( __data_read ) {
		// Should not happen...
		Message.printWarning ( 2, routine, "ESP trace ensemble data " +
		"are already read from file - rereading." );
	}

	int ndata = __rec_words/4;	// Floats per line - should be 31
	int nrecpermonth = (ndata/31)*(24/__ts_dt);
	float [] data = new float[744];	// Enough for 31 days x 24 1-hour
					// values...
	byte [] record = new byte[4]; 	// This is the byte value returned from 
					// the read.
	int icm;	// Loop counter for conditional months in each trace.
	int ndays;	// Number of days per month.
	int i;		// Position in loop for data per record
	int idata;	// Position in data array for month
	int ntran;	// Number of data to transfer for a month's data
	DateTime date;	// Date/time to used to transfer data array to time
			// series.
	DateTime hdate;	// Date/time to used to evaluate a historical date/time.
	EndianDataInputStream EDIS;

	try {
	// Position the file pointer...
	// Check to see if RandomAccessFile is open
	if(!__traceRAFOpen)
	{
		__traceRAF = new EndianRandomAccessFile(__filename,"r"); 
		__traceRAFOpen = true;
	}

	__dmi.rewind(__traceRAF);
	__dmi.seek(__traceRAF,(long)((__irec - 1)*__rec_words*4),false);

	// Loop through the number of time series traces...
	for ( int its = 0; its < __n_traces; its++ ) {
		// The data space is not allocated in readHeader() so do it
		// here...
		__ts[its].allocateDataSpace();
		// Initialize the date that will be used to transfer data to
		// the starting interval in the data file.
		// The dates in the file use the hour 1-24.  However,
		// the time series have been allocated in readHeader()
		// using hour 0-23.  Therefore, the starting date/time
		// must be properly set.  Each month of data in the file
		// corresponds to hour __ts_dt of the first day of the
		// HISTORICAL month, which will only be an issue if
		// __ts_dt == 24.  Take care to
		// set the starting date correctly and then just add the
		// interval as the data are processed.
		// First determine the hour 24 date/time, mainly to get
		// the correct month, and year.
		// Start by setting to the initial value...
		date = NWSRFS_Util.toDateTime24(__start_date,true);
		// Set the day to 1 and the hour to the interval...
		date.setDay ( 1 );
		date.setHour ( __ts_dt );
		// Convert back to 0-23 hour...
		date = NWSRFS_Util.toDateTime23(date,true);
		// Loop through the number of conditional months (the month is
		// incr...
		for ( icm = 0; icm < __ncm; icm++ ) {
			// Loop through the records in the month...
			idata = 0;	// Reset array position.
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine,
				"Reading trace [" + its + "] " +
				__ts[its].getSequenceNumber() +
				" conditional month [" + icm + "]" );
			}
			for (	int ir = 0; ir < nrecpermonth; ir++ ) {
				// Loop through the data in the month,
				// incrementing the hour to assign the data...
				for ( i = 0; i < ndata; i++ ) {
					// Read the data from the DMI
					EDIS = __dmi.read(__traceRAF,0,4);
					//EDIS.setBigEndian(__big_endian);

					// Convert to a float
					data[idata++] = EDIS.readEndianFloat();

					// Close the stream to get new record
					EDIS.close();
				}
			}
			// Now a complete month has been read.  Determine the
			// number of values available in the file to be
			// transferred.  The months in the file correspond to
			// the historical months, not the real-time forecast
			// years.  Therefore, for example, if the forecast
			// period is May 2002 through May 2004 but the
			// starting historical years are 1995 - 1998 (4 traces),
			// the second trace (historical years 1996-1997) will
			// have 28 days in February in the data file, even
			// though 2004 in the forecast period has 29.
			// Therefore, calculate the number of data values in
			// the file based on the historical year and only
			// increment the date for the time series as values are
			// transferred.
			hdate = new DateTime(DateTime.PRECISION_MONTH);
			// Set the year to the historical year...
			hdate.setYear ( __iy + its );
			hdate.setMonth ( __im );
			// Now add the number of months that have been
			// processed...
			hdate.addMonth ( icm );
			// Now get the number of days in the month.  This does
			// not look at the hour so an hour of 24 is OK...
			ndays = TimeUtil.numDaysInMonth ( hdate );
			// Now compute the number of data that will need to be
			// transferred.  It may be less than the number read
			// because of the number of days in the month...
			ntran = ndays*24/__ts_dt;
			// Now loop through the data, using the actual 0-23
			// hour and the number of intervals.  It is OK to
			// attempt transferring data outside the actual TS
			// period because data outside the period will be
			// ignored (and should be missing).
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine,
				"Transferring " + ndays + " days, " + ntran +
				" values for historical " + hdate +
				" starting at " + date );
			}
			for (	idata = 0; idata < ntran;
				idata++, date.addHour(__ts_dt) ) {
				__ts[its].setDataValue(date,data[idata]);
			}
		}
	}
	}
	catch ( Exception e ) {
		// Should not happen if loops above are correct...
		Message.printWarning ( 2, routine, "Unexpected end of file." );
		Message.printWarning ( 2, routine, e );
		throw new Exception ( "Unexpected end of file" );
	}

	__data_read = true;
}

/**
Read the trace ensemble file's header information, and set up internal
data.
*/
private void readHeader() 
throws Exception
{	
	// Local variables
	String routine = "NWSRFS_ESPTraceEnsemble.readHeader";
	int j = 0, i = 0;
	float floatValue;
	char[] charValue;
	boolean readOFSFS5Files = true;
	String parseChar = null;
	EndianDataInputStream EDIS;
	String prsf_string = null;
	TZ localTZ;
	Vector tzMatches;
	DateTime temp_date;
	float shift = (float).01;	// This is added to integers that are
					// read as float to make sure that the
					// truncated integer is the proper
					// value, in case the precision of the
					// write rounded under.

	// Check to see if RandomAccessFile is open
	if(!__traceRAFOpen)
	{
		__traceRAF = new EndianRandomAccessFile(__filename,"r"); 
		__traceRAFOpen = true;
	}

	// Rewind the file to make sure we read the header.
	// Although if the file has not been opened yet it will
	// not need to be rewound.
	__dmi.rewind(__traceRAF);

	// Read the header record from the dmi which is a __headerLength byte
	// record
	EDIS = __dmi.read(__traceRAF,0,__headerLength);

	// Now parse the record for the header information. It is vital to know
	// exactly the format of the ESP trace file in order to parse the header
	// correctly.  The version might have an effect here so will need to
	// revisit later to implement the version changes.
	//
	// Format version. Float value
	floatValue = EDIS.readEndianFloat();

	// Check to see if there is an endianess problem! The format version
	// should be between 1 and 10. Current version seems to be "1.01".
	if(new Float(floatValue).isNaN() || floatValue <= 0 || floatValue > 10)
	{
		// Change the value of the endianess
		if(__big_endian)
		{
			__big_endian = false;
		}
		else
		{
			__big_endian = true;
		}

		// Need to rewind to reread
		__dmi.rewind(__traceRAF);

		// Reread the header record from the dmi which is a
		// __headerLength byte record
		EDIS = __dmi.read(__traceRAF,0,__headerLength);
		EDIS.setBigEndian(__big_endian);

		floatValue = EDIS.readEndianFloat();

		// Check again.
		if(	new Float(floatValue).isNaN() ||
			(floatValue <= 0) || (floatValue > 10 ) )
		{
			throw new Exception("Can't read ESP trace file \""+
			__filename+"\" because of byte order problems.");
		}
	}
	
	__format_ver = floatValue;

	Message.printStatus ( 2, routine,
		"Read version = \"" + __format_ver + "\"");

	//
	// Segment Id. An 8 character string value
	charValue = new char[8];
	for(i=0;i<8;i++)
	{
		charValue[i] = EDIS.readEndianChar1();
	}
	
	parseChar = new String(charValue);
	__seg_id = parseChar.trim();

	Message.printStatus ( 2, routine, "Read seg_id = \"" + __seg_id + "\"");

	//
	// Time series Id. An 8 character string value
	charValue = new char[8];
	for(i=0;i<8;i++)
	{
		charValue[i] = EDIS.readEndianChar1();
	}
	
	parseChar = new String(charValue);
	__ts_id = parseChar.trim();

	Message.printStatus ( 2, routine, "Read ts_id = \"" + __ts_id + "\"");

	//
	// Time series type. A 4 character string value
	charValue = new char[4];
	for(i=0;i<4;i++)
	{
		charValue[i] = EDIS.readEndianChar1();
	}
	
	parseChar = new String(charValue);
	__ts_type = parseChar.trim();

	Message.printStatus ( 2, routine, "Read ts_type = \"" +__ts_type+ "\"");

	//
	// Time series data interval. An 4 byte integer
	floatValue = EDIS.readEndianFloat();
	
	__ts_dt = (int)(floatValue + shift);
	
	Message.printStatus ( 2, routine, "Read interval = " +__ts_dt );

	//
	// Simulation flag. A value of 0 is a CS, 1 is HS, 2 is OBS.
	// A 4 byte integer
	floatValue = EDIS.readEndianFloat();
	
	__simflag = (int)(floatValue + shift);
	
	Message.printStatus ( 2, routine, "Read simflag = " + __simflag );
	// NWS apparently has different values...
	if ( __simflag == SIMFLAG_CONDITIONAL ) {
	}
	else if ( __simflag == SIMFLAG_HISTORICAL ) {
		Message.printWarning ( 2, routine,
		"Historical trace format is not supported." );
		throw new Exception (
		"Historical trace format is not supported." );
	}
	else {	Message.printWarning ( 2, routine,
		"Unrecognized file simflag " + __simflag );
		throw new Exception ("Unrecognized file simflag " + __simflag );
	}

	//
	// Time Series Unit type. A 4 character string
	charValue = new char[4];
	for(i=0;i<4;i++)
	{
		charValue[i] = EDIS.readEndianChar1();
	}
	
	parseChar = new String(charValue);
	__ts_unit = parseChar.trim();

	Message.printStatus ( 2, routine, "Read ts_unit = \"" +__ts_unit+ "\"");

	//
	// Get the "now" int array for date time values. There are 5 int values
	floatValue = EDIS.readEndianFloat();

	__now[0] = (int)(floatValue + shift); // Month

	floatValue = EDIS.readEndianFloat();

	__now[1] = (int)(floatValue + shift); // Day

	floatValue = EDIS.readEndianFloat();

	__now[2] = (int)(floatValue + shift); // Year

	floatValue = EDIS.readEndianFloat();

	__now[3] = (int)(floatValue + shift); // Hour/Min

	floatValue = EDIS.readEndianFloat();

	__now[4] = (int)(floatValue + shift); // Sec/Millisec
	
	//
	// Month of the first day of the first time series in the file
	// A 4 byte integer
	floatValue = EDIS.readEndianFloat();

	__im = (int)(floatValue + shift);

	Message.printStatus ( 2, routine, "Read im = " + __im );
	
	//
	// Year of the first day of the first time series in the file.
	// A 4 byte integer
	floatValue = EDIS.readEndianFloat();

	__iy = (int)(floatValue + shift);

	Message.printStatus ( 2, routine, "Read iy = " + __iy );

	//
	// Start of the traces as a julian day relative to 12/31/1899.
	// A 4 byte integer
	floatValue = EDIS.readEndianFloat();

	__idarun = (int)(floatValue + shift);

	Message.printStatus ( 2, routine, "Read idarun = " + __idarun );

	//
	// End of the traces as a julian day relative to 12/31/1899.
	// A 4 byte integer
	floatValue = EDIS.readEndianFloat();

	__ldarun = (int)(floatValue + shift);

	Message.printStatus ( 2, routine, "Read ldarun = " + __ldarun );

	//
	// Carryover day (1-31). A 4 byte integer
	floatValue = EDIS.readEndianFloat();

	__ijdlst = (int)(floatValue + shift);

	Message.printStatus ( 2, routine, "Read ijdlst = " + __ijdlst );

	//
	// Carryover hour (1-24). A 4 byte integer
	floatValue = EDIS.readEndianFloat();

	__ihlst = (int)(floatValue + shift);

	Message.printStatus ( 2, routine, "Read ihlst = " + __ihlst );

	//
	// Last day of the forecast as a julian day relative to 12/31/1899.
	// A 4 byte integer
	floatValue = EDIS.readEndianFloat();

	__ljdlst = (int)(floatValue + shift);

	Message.printStatus ( 2, routine, "Read ljdlst = " + __ljdlst );

	//
	// Last hour of forecast (1-24) in zulu time. A 4 byte integer
	floatValue = EDIS.readEndianFloat();

	__lhlst = (int)(floatValue + shift);

	Message.printStatus ( 2, routine, "Read lhlst = " + __lhlst );

	//
	// Number of traces. A 4 byte integer
	floatValue = EDIS.readEndianFloat();

	__n_traces = (int)(floatValue + shift);

	Message.printStatus ( 2, routine, "Read n_traces = " + __n_traces );

	//
	// Number of conditional months. A 4 byte integer
	floatValue = EDIS.readEndianFloat();

	__ncm = (int)(floatValue + shift);

	Message.printStatus ( 2, routine, "Read ncm = " + __ncm );

	//
	// NWSRFS time zone relative to zulu. A 4 byte integer
	floatValue = EDIS.readEndianFloat();

	__nlstz = (int)(floatValue + shift);

	Message.printStatus ( 2, routine, "Read nlstz = " + __nlstz );

	//
	// NWSRFS daylight savings time flag. A 4 byte integer
	floatValue = EDIS.readEndianFloat();

	__noutds = (int)(floatValue + shift);

	Message.printStatus ( 2, routine, "Read noutds = " + __noutds );

	//
	// Number of record of the first trace data. A 4 byte integer
	floatValue = EDIS.readEndianFloat();

	__irec = (int)(floatValue + shift);

	Message.printStatus ( 2, routine, "Read irec = " + __irec );

	//
	// Unit dimensions for NWS, data units. A 4 character string
	charValue = new char[4];
	for(i=0;i<4;i++)
	{
		charValue[i] = EDIS.readEndianChar1();
	}
	
	parseChar = new String(charValue);
	__dim = parseChar.trim();

	Message.printStatus ( 2, routine, "Read dim = \"" +__dim+ "\"");

	//
	// Time scale of code. A 4 character string
	charValue = new char[4];
	for(i=0;i<4;i++)
	{
		charValue[i] = EDIS.readEndianChar1();
	}
	
	parseChar = new String(charValue);
	__tscale = parseChar.trim();

	Message.printStatus ( 2, routine, "Read tscale = \"" +__tscale+ "\"");

	//
	// Segment description. A 20 character string
	charValue = new char[20];
	for(i=0;i<20;i++)
	{
		charValue[i] = EDIS.readEndianChar1();
	}
	
	parseChar = new String(charValue);
	__segdesc = parseChar.trim();

	Message.printStatus (2, routine, "Read seg_desc = \"" +__segdesc+ "\"");

	//
	// Latitude of the segment decimal degrees. A 4 byte float value
	floatValue = EDIS.readEndianFloat();

	__xlat = floatValue;

	Message.printStatus ( 2, routine, "Read xlat = \"" +__xlat+ "\"");

	//
	// Longitude of the segment decimal degrees. A 4 byte float value
	floatValue = EDIS.readEndianFloat();

	__xlong = floatValue;

	Message.printStatus ( 2, routine, "Read xlong = \"" +__xlong+ "\"");

	//
	// Forecast group. An 8 character string
	charValue = new char[8];
	for(i=0;i<8;i++)
	{
		charValue[i] = EDIS.readEndianChar1();
	}
	
	parseChar = new String(charValue);
	__fg = parseChar.trim();

	Message.printStatus ( 2, routine, "Read fg = \"" +__fg+ "\"");

	//
	// Carryover group. An 8 character string
	charValue = new char[8];
	for(i=0;i<8;i++)
	{
		charValue[i] = EDIS.readEndianChar1();
	}
	
	parseChar = new String(charValue);
	__cg = parseChar.trim();

	Message.printStatus ( 2, routine, "Read cg = \"" +__cg+ "\"");

	//
	// RFC name. An 8 character string
	charValue = new char[8];
	for(i=0;i<8;i++)
	{
		charValue[i] = EDIS.readEndianChar1();
	}
	
	parseChar = new String(charValue);
	__hdr_id_rfcname = parseChar.trim();

	Message.printStatus ( 2, routine,
		"Read rfcname = \"" +__hdr_id_rfcname+ "\"");

	//
	// Trace file name without path. E.G. GRCCH.GRCCH.QINE.06.CS.
	// An 80 character string
	charValue = new char[80];
	for(i=0;i<80;i++)
	{
		charValue[i] = EDIS.readEndianChar1();
	}
	
	parseChar = new String(charValue);
	__espfname = parseChar.trim();

	Message.printStatus(2, routine, "Read espfname = \"" +__espfname+ "\"");

	//
	// The prsf flag string. An 80 character string
	charValue = new char[80];
	for(i=0;i<80;i++)
	{
		charValue[i] = EDIS.readEndianChar1();
	}
	
	parseChar = new String(charValue);
	prsf_string = parseChar.trim();

	Message.printStatus ( 2, routine,
		"Read prsf_string = \"" +prsf_string+ "\"");

	//
	// User comments. An 80 character string
	charValue = new char[80];
	for(i=0;i<80;i++)
	{
		charValue[i] = EDIS.readEndianChar1();
	}
	
	parseChar = new String(charValue);
	__esptext = parseChar.trim();

	Message.printStatus ( 2, routine, "Read esptext = \"" +__esptext+ "\"");

	//
	// Adjustment counter. A 4 byte integer
	floatValue = EDIS.readEndianFloat();

	__adjcount = (int)(floatValue + shift);

	Message.printStatus ( 2, routine, "Read adjcount = " + __adjcount );

	// Get the local timezone abbreviation.
	tzMatches = TZ.getMatchingDefinedTZ(__nlstz*60,__noutds);
	localTZ = (TZ)tzMatches.elementAt(0);
	i = 1;
	while((localTZ.getAbbreviation()).length() < 3 && i<tzMatches.size())
	{
		localTZ = (TZ)tzMatches.elementAt(i);
		i++;
	}

	Message.printStatus ( 2, routine,
		"Local timezone abbreviation = " +
		localTZ.getAbbreviation() );

	// idarun in the file is already in local time because that is what
	// ESP writes - use the local hour to convert, as per Jay Day
	DateTime idarun_date = NWSRFS_Util.mdyh1(__idarun, __ihlst);
	Message.printStatus ( 2, routine,
		"idarun as local date/time = " +
		NWSRFS_Util.toDateTime24(idarun_date,false) );

	// ldarun in the file is already in local time because that is what
	// ESP writes - use the local hour to convert, as per Jay Day
	// REVISIT - should __lhlst be put in here?
	DateTime ldarun_date = NWSRFS_Util.mdyh1(__ldarun, __lhlst );
	Message.printStatus ( 2, routine,
		"ldarun as local date/time = " +
		NWSRFS_Util.toDateTime24(ldarun_date,false) );

	// ijdlst and ihlst in the file are in local time
	temp_date = NWSRFS_Util.mdyh1(__ijdlst, __ihlst);
	__carryover_date = new DateTime(temp_date,DateTime.PRECISION_TIME_ZONE);
	
	// Add timezone string to carry over DateTime object
	__carryover_date.setTimeZone(localTZ.getAbbreviation());

	Message.printStatus ( 2, routine,
		"ijdlst (carryover) as local date/time = " +
		NWSRFS_Util.toDateTime24(__carryover_date,false) );

	temp_date= NWSRFS_Util.mdyh1(__ljdlst, __lhlst);
	__end_date = new DateTime(temp_date,DateTime.PRECISION_TIME_ZONE);

	// Add timezone string to end_date DateTime object
	__end_date.setTimeZone(localTZ.getAbbreviation());

	Message.printStatus ( 2, routine,
		"ljdlst (forecast end) as local date/time = " +
		NWSRFS_Util.toDateTime24(__end_date,false) );

	// Set the creation date

	__hdr_id_creationdate = new DateTime();
	__hdr_id_creationdate.setYear(__now[2]);
	__hdr_id_creationdate.setMonth(__now[0]);
	__hdr_id_creationdate.setDay(__now[1]);
	// Integer math to split out the hour and minute
	__hdr_id_creationdate.setHour(__now[3] / 100);
	__hdr_id_creationdate.setMinute(__now[3] - (__now[3] / 100) * 100);
	// Seconds are not tracked.

	// REVISIT - original C++ code had something here about weights.

	// Now set the data members of the trace ensemble

	if ( prsf_string.equalsIgnoreCase("PRSF") ) {
		__prsf_flag = 1;
	}
	else {	__prsf_flag = 0;
	}

	// REVISIT
	//__time_zone.setNumber ( __nlstz, noutds );

	// REVISIT - need to handle data types her to get the time scale
	// ACCM, MEAN, INST, etc.

	// Break the data type description by commas, and only use the first
	// token in the list.

	// REVISIT - apparently comes out of the data type information
	//Vector desc_list = StringUtil.breakStringList ( words, ",",
	//		StringUtil.DELIM_SKIP_BLANKS );
	//__hdr_id_datatypedesc ( (String)desc_list.elementAt(0) );

	// The start of the forecast is one interval after the carryover date..

	__start_date = new DateTime ( __carryover_date );
	__start_date.addHour ( __ts_dt );

	__hdr_id_startdate_orig = new DateTime ( __start_date );
	__hdr_id_exceedProbDate = new DateTime ( __start_date );

	__hdr_id_enddate_orig = new DateTime ( __end_date );

	Message.printStatus ( 2, routine,
	"Trace start local time (one interval after carryover) = " +
		__start_date );
	Message.printStatus ( 2, routine,
	"Trace end local time (end of forecast) = " + __end_date );

	// REVISIT SAM 2004-04-07 
	// These are stored but not really supported yet - we might want to
	// remove redundant data members and rename if appropriate.

	__hdr_id = new TSIdent ( __espfname );
	__hdr_id.setLocation ( __hdr_id.getSource() );
	__hdr_id.setSource ( "NWSRFS" );
	__hdr_id.setScenario ( "" );
	__hdr_id.setInputType ( "NWSRFS_ESPTraceEnsemble" );
	__hdr_id.setInputName ( __filename );
	__hdr_id.setInterval(""+StringUtil.atoi(__hdr_id.getInterval())+"Hour");
	__hdr_id_vartype = VARTYPE_NONE;
	__hdr_id_vartype_orig = VARTYPE_NONE;
	__hdr_id_accumvar = ACCUMVAR_MEAN;
	__hdr_id_accumdir = 1;
	__hdr_id_accumcrit = (float)0.0;
	__hdr_id_probFunction = FUNCTION_EMPIRICAL;
	__hdr_id_nRanges = 3;
	__hdr_id_probRanges = new float[3];		// Defaults (not read
	__hdr_id_probRanges[0] = (float)0.75;		// from file?)
	__hdr_id_probRanges[1] = (float)0.5;
	__hdr_id_probRanges[2] = (float)0.25;
	__hdr_id.setType ( __ts_type );
	__hdr_id_data_interval_base = TimeInterval.HOUR;
	__hdr_id_data_interval_mult = __ts_dt;
	__hdr_id_data_interval_base_orig = TimeInterval.HOUR;
	__hdr_id_data_interval_mult_orig = __ts_dt;
	__hdr_id_units_orig = __ts_unit;
	__hdr_id_missing = (float)-999.0;

	// Now loop through each trace and set the appropriate header items.
	// With the exception of the alias and sequence number, all of the
	// fields are the same.

	int offset = __end_date.getYear() - __start_date.getYear();
	
	__ts = new HourTS[__n_traces];
	for ( i = 0; i < __n_traces; i++ ) {
		__ts[i] = new HourTS();

		// Set information for the time series.  Because the RTi
		// TSIdent is more streamlined than the NWS version, we only
		// set some information...

		__ts[i].setIdentifier ( new TSIdent(__hdr_id) );
		// The sequence number is used for the historical year...
		__ts[i].setSequenceNumber ( __iy + i );

		Message.printStatus ( 2, routine, "Setting identifier to \"" +
			__ts[i].getIdentifier().toString(true) + "\"" );
		__ts[i].setDate1 ( new DateTime(__start_date) );
		__ts[i].setDate1Original ( new DateTime(__start_date) );
		__ts[i].setDate2 ( new DateTime(__end_date) );
		__ts[i].setDate2Original ( new DateTime(__end_date) );
		Message.printStatus ( 2, routine, "Setting TS[" + i +
		"] " + __ts[i].getSequenceNumber() +
		" period to " + __ts[i].getDate1() + " - " +__ts[i].getDate2());
		__ts[i].setDataInterval ( TimeInterval.HOUR, __ts_dt );
		__ts[i].setDataUnits ( __ts_unit );
		__ts[i].setDescription ( __segdesc );
		__ts[i].setAlias ( __hdr_id.getLocation() + "_Trace_" +
			__ts[i].getSequenceNumber() );

		// Use comments for now to pass information and troubleshoot...

		Vector header_strings = getHeaderStrings ( __ts[i] );
		int size = header_strings.size();
		for ( int istr = 0; istr < size; istr++ ) {
			__ts[i].addToComments (
				(String)header_strings.elementAt(istr) );
		}
	}

	__hdr_read = true;
}

/**
Read a time series matching a time series identifier.
@return a time series or null if the time series is not defined in the database.
If no data records are available within the requested period, a call to
hasData() on the returned time series will return false.
@param tsident_string TSIdent string indentifying the time series.
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
public TS readTimeSeries (String tsident_string, DateTime req_date1,
			  DateTime req_date2, String req_units,
			  boolean read_data ) throws Exception
{
	// Local variables 
	String routine = "NWSRFS_ESPTraceEnsemble.readTimeSeries";
	String data_source, data_type, interval, filename, input_type;

	// Get TSIdent info
	TSIdent tsident = new TSIdent(tsident_string);
	data_source = tsident.getSource();
	data_type = tsident.getType();
	interval = tsident.getInterval();
	input_type = tsident.getInputType();

	TS ts = null;
	filename = tsident.getInputName();
	NWSRFS_ESPTraceEnsemble espTE =
		new NWSRFS_ESPTraceEnsemble(filename,read_data,true);
	ts.allocateDataSpace();
	ts = new HourTS(espTE.__ts[0]); // This currently pulls the first trace

	Message.printStatus(2,routine,"tsident_string = "+tsident_string+
		"\nfilename = "+filename+"input type = "+input_type);
	return ts;
}

/**
Writes out the trace ensemble time series to the specified file as one large
DateValueTS.
@param filename the name of the file to which to write the time series.
*/
public void writeDateValueFile ( String filename ) 
throws Exception
{	// Traces are stored in an array so transfer to a Vector for writing...
	DateValueTS.writeTimeSeriesList ( getTimeSeriesVector(), filename );
}

/* REVISIT - sat 2004-11-30
  The following two write methods are really quite different. One uses an
EndianDataOutputStream (EDOS) to write the file (the preferred method)
the other does the bytes shifts manually then does a straight write to
the file. The write method which uses the EDOS somehow does not write the file 
quite right and needs to be debuged since it really is the best way to write the 
file. The original write method was intended to only be a method to call the 
EDOS write but did not become that since it wrote the file correctly and the EDOS
method did not.
*/
/**
This method is under review to discover what is causing the output ESP trace 
file to fail in ESPADP.

Write the ESP trace ensemble to an ESP trace ensemble binary file. 
A new file is created, even if the data were read from the same file originally.
Because ESP trace files are not specifically little- or big-endian, write using
the endianness of the current machine.
@param fname Name of file to write. 
@exception Exception if there is an error writing the file.
*/
public void writeESPTraceEnsembleFile ( String fname )
throws Exception
{
	// Local variables
	int i,recordIndex;
	String routine = "NWSRFS_ESPTraceEnsemble.writeESPTraceEnsembleFile";
	File f = null;
	// REVISIT SAM 2004-12-01 Both of the following approaches seem to
	// give the same results.  At some point may want to do a performance
	// test and pick the fastest.
	//EndianDataOutputStream fp;
	EndianRandomAccessFile fp;

	// Determine the full path to the file using the working directory...

	String full_fname = IOUtil.getPathUsingWorkingDir(fname);

	// Delete the old file if it exists to avoid leftover bytes in the
	// file...

	f = new File ( full_fname );
	if ( f.exists() ) {
		f.delete();
	}

	// Open EndianRandomAccessFile with the defined endian-ness and replace
	// current file if it exists.

	// REVISIT SAM 2004-12-01  SAT was using the __dmi.write() method to 
	// open the file.  Why is this necessary?  There may be times that
	// an ESP trace ensemble file needs to be written independent of any
	// DMI.  What benefit is there to use the DMI?
	// Also, the method below uses "r" for "replacement" - this is contrary
	// to the normal "r" = "read" notation.
	//fp = __dmi.write(full_fname,true,"r",false); 
	fp = new EndianRandomAccessFile (
			full_fname,
			"rw",	// Write the file
			true );	// Match the endian-ness of the system.
	/*
	fp = new EndianDataOutputStream (
			new FileOutputStream(full_fname,
			false),	// No appen since one-pass write
			true );	// Match the endian-ness of the system.
	*/

	// This is important because the read code uses the ESP file name to
	// get the TSIdent information...
	String espfname = f.getName();

	// Format version. A 4 byte float converted back to bytes to write.
	// No index is needed since we are just writing bytes to the file and
	// letting the file move its position as the write is done. Sort of like
	// a stream....
	Message.printStatus(2,routine,"Writing version \"" + __format_ver+"\"");
	fp.writeEndianFloat(__format_ver);

	// Segment ID. An 8 byte String. Convert to bytes and pad with spaces
	// if less than 8 characters.
	Message.printStatus ( 2, routine,"Writing seg_id \"" + __seg_id + "\"");
	String string = StringUtil.formatString ( __seg_id.trim(), "%-8.8s" );
	fp.writeEndianChar1(string);

	// Time series Id. An 8 byte String
	Message.printStatus ( 2, routine, "Writing ts_id \"" + __ts_id + "\"" );
	string = StringUtil.formatString ( __ts_id.trim(), "%-8.8s" );
	fp.writeEndianChar1(string);

	// Time series type. A 4 byte String
	Message.printStatus ( 2, routine,"Writing ts_type \"" + __ts_type+"\"");
	string = StringUtil.formatString ( __ts_type.trim(), "%-4.4s" );
	fp.writeEndianChar1(string);

	// Time series data interval.  A 4 byte integer written as a float.
	Message.printStatus (2,routine,"Writing data interval \""+__ts_dt+"\"");
	fp.writeEndianFloat((float)__ts_dt);

	// Simulation flag. A 4 byte integer written as a float.
	Message.printStatus ( 2, routine,"Writing simflag \"" + __simflag+"\"");
	fp.writeEndianFloat((float)__simflag);

	// Time series units. A 4 byte String
	Message.printStatus ( 2, routine, "Writing ts_unit \""+__ts_unit+"\"" );
	string = StringUtil.formatString ( __ts_unit.trim(), "%-4.4s" );
	fp.writeEndianChar1(string);

	// The "now" current date and time. This is 5 4 byte integers, as floats
	DateTime now = new DateTime ( DateTime.DATE_CURRENT );
	Message.printStatus ( 2, routine, "Writing datetime \"" + now + "\"" );
	fp.writeEndianFloat((float)now.getMonth());	// Month
	fp.writeEndianFloat((float)now.getDay());	// Day
	fp.writeEndianFloat((float)now.getYear());	// Year
	// Hour/Min...
	fp.writeEndianFloat((float)(now.getHour()*100 + now.getMinute()));
	fp.writeEndianFloat((float)now.getSecond());	// Sec/Milisec

	// Month of the first day of the Time series. A 4 byte integer as float.
	Message.printStatus ( 2, routine, "Writing im \"" + __im + "\"" );
	fp.writeEndianFloat((float)__im);

	// Year of the first day of the Time series. A 4 byte integer
	Message.printStatus ( 2, routine, "Writing iy \"" + __iy + "\"" );
	fp.writeEndianFloat((float)__iy);

	// Start of the traces. A 4 byte integer as a float
	Message.printStatus ( 2, routine,
				"Writing start of traces \"" + __idarun + "\"");
	fp.writeEndianFloat((float)__idarun);

	// End of traces. A 4 byte integer
	Message.printStatus ( 2, routine,
				"Writing end of traces \"" + __ldarun + "\"" );
	fp.writeEndianFloat((float)__ldarun);

	// Carryover day. A 4 byte integer as a float
	Message.printStatus ( 2, routine,
				"Writing carryover day \"" + __ijdlst + "\"" );
	fp.writeEndianFloat((float)__ijdlst);

	// Carryover hour. A 4 byte integer
	Message.printStatus ( 2, routine,
				"Writing carryover hour \"" + __ihlst + "\"" );
	fp.writeEndianFloat((float)__ihlst);

	// Last day of forecast. A 4 byte integer as a float
	Message.printStatus ( 2, routine,
			"Writing last day of forecast \"" + __ljdlst + "\"" );
	fp.writeEndianFloat((float)__ljdlst);

	// Last hour of forecast. A 4 byte integer as a float
	Message.printStatus ( 2, routine,
			"Writing last hour of forecast \"" + __lhlst + "\"" );
	fp.writeEndianFloat((float)__lhlst);

	// Number of traces. A 4 byte integer as a float
	Message.printStatus ( 2, routine,
			"Writing number of traces \"" + __n_traces + "\"" );
	fp.writeEndianFloat((float)__n_traces);

	// Number of conditional months. A 4 byte integer as a float
	Message.printStatus ( 2, routine,
			"Writing number of conditional months \"" + __ncm+"\"");
	fp.writeEndianFloat((float)__ncm);

	// NWSRFS Time zone. A 4 byte integer
	Message.printStatus ( 2, routine,"Writing time zone \"" + __nlstz+"\"");
	fp.writeEndianFloat((float)__nlstz);

	// The NWSRFS daylight savings time flag. A 4 byte integer as a float
	Message.printStatus ( 2, routine,
			"Writing daylight saving time \"" + __noutds + "\"" );
	fp.writeEndianFloat((float)__noutds);

	// Record number of the first trace data. A 4 byte integer as a float
	Message.printStatus ( 2, routine,
			"Writing first record with trace data \"" +__irec+"\"");
	fp.writeEndianFloat((float)__irec);

	// The unit dimensions from the NWS data units. A 4 byte String
	Message.printStatus ( 2, routine,
				"Writing unit dimensions \"" + __dim + "\"" );
	string = StringUtil.formatString ( __dim.trim(), "%-4.4s" );
	fp.writeEndianChar1(string);

	// Time scale of code. A 4 byte String
	Message.printStatus ( 2, routine,
				"Writing time scale code \"" + __tscale + "\"");
	string = StringUtil.formatString ( __tscale.trim(), "%-4.4s" );
	fp.writeEndianChar1(string);

	// Segment description. A 20 byte String
	Message.printStatus ( 2, routine,
			"Writing segment description \"" + __segdesc + "\"" );
	string = StringUtil.formatString ( __segdesc.trim(), "%-20.20s" );
	fp.writeEndianChar1(string);

	// Latitude. A 4 byte float
	Message.printStatus ( 2, routine,"Writing latitude \"" + __xlat + "\"");
	fp.writeEndianFloat(__xlat); 

	// Longitude. A 4 byte float
	Message.printStatus ( 2, routine,"Writing longitude \"" + __xlong+"\"");
	fp.writeEndianFloat(__xlong); 

	// Forecast group. An 8 byte String
	Message.printStatus ( 2, routine,"Writing forecast group \""+__fg+"\"");
	string = StringUtil.formatString ( __fg.trim(), "%-8.8s" );
	fp.writeEndianChar1(string);

	// Carryover group. An 8 byte String
	Message.printStatus ( 2, routine,
				"Writing carryover group \"" + __cg + "\"" );
	string = StringUtil.formatString ( __cg.trim(), "%-8.8s" );
	fp.writeEndianChar1(string);

	// The RFC name. An 8 byte String
	Message.printStatus ( 2, routine,
			"Writing RFC name \"" + __hdr_id_rfcname + "\"" );
	string = StringUtil.formatString ( __hdr_id_rfcname.trim(), "%-8.8s" );
	fp.writeEndianChar1(string);

	// Trace file name without path. An 80 byte String
	// If __espfname is missing then use the method argument fname
	Message.printStatus ( 2, routine,
				"Writing espfname \"" + espfname + "\"" );
	string = StringUtil.formatString ( espfname, "%-80.80s");
	fp.writeEndianChar1(string);

	// The prsf flag string. An 80 byte String
	String prsf_string = "";
	if(__prsf_flag == 1) 
	{
		prsf_string = "PRSF";
	}
	else 
	{	
		prsf_string = "";
	}
	Message.printStatus ( 2, routine,
				"Writing prsf string \"" + prsf_string + "\"" );
	string = StringUtil.formatString ( prsf_string.trim(), "%-80.80s" );
	fp.writeEndianChar1(string);

	// User comments. An 80 byte String
	Message.printStatus ( 2, routine,
				"Writing trace Comments \"" + __esptext + "\"");
	string = StringUtil.formatString ( __esptext.trim(), "%-80.80s" );
	fp.writeEndianChar1(string);

	// Adjustment counter. A 4 byte integer as float
	Message.printStatus ( 2, routine,
			"Writing adjustment counter \"" + __adjcount + "\"" );
	fp.writeEndianFloat((float)__adjcount);

	// Words 104-124. There needs to be 84 bytes of nulls written to the
	// file
	string = StringUtil.formatString ( __esptext.trim(), "%-84.84s" );
	fp.writeEndianChar1(string);
	
	// Write the data...

	int ndata = __rec_words/4;	// Floats per line - should be 31
	int nrecpermonth = (ndata/31)*(24/__ts_dt);
	float [] data = new float[744];	// Enough for 31 days x 24 1-hour
					// values...
	int icm;	// Loop counter for conditional months in each trace.
	int ndays;	// Number of days per month.
	int idata;	// Position in data array for month
	int ntran;	// Number of data to transfer for a month's data
	int ntran2;	// Number of data to transfer for a full month's data
	DateTime date;	// Date/time to used to transfer data array to time
			// series.
	DateTime hdate;	// Date/time to used to evaluate a historical date/time.
	float missing_float = (float)-999.0;
	try {
	// Loop through the number of time series traces...
	for ( int its = 0; its < __n_traces; its++ ) {
		// Initialize the date that will be used to transfer data to
		// the starting interval in the data file.
		// The dates in the file use the hour 1-24.  However,
		// the time series have been allocated in readHeader()
		// using hour 0-23.  Therefore, the starting date/time
		// must be properly set.  Each month of data in the file
		// corresponds to hour __ts_dt of the first day of the
		// HISTORICAL month, which will only be an issue if
		// __ts_dt == 24.  Take care to
		// set the starting date correctly and then just add the
		// interval as the data are processed.
		// First determine the hour 24 date/time, mainly to get
		// the correct month, and year.
		// Start by setting to the initial value...
		date = NWSRFS_Util.toDateTime24(__start_date,true);
		// Set the day to 1 and the hour to the interval...
		date.setDay ( 1 );
		date.setHour ( __ts_dt );
		// Convert back to 0-23 hour...
		date = NWSRFS_Util.toDateTime23(date,true);
		// Loop through the number of conditional months (the month is
		// incremented)...
		for ( icm = 0; icm < __ncm; icm++ ) {
			// Loop through the records in the month...
			// Transfer a complete month into the data array.
			// Determine the number of values available in the file
			// to be transferred.  The months in the file
			// correspond to the historical months, not the
			// real-time forecast years.  Therefore, for example,
			// if the forecast period is May 2002 through May 2004
			// but the starting historical years are 1995 - 1998
			// (4 traces), the second trace (historical years
			// 1996-1997) will have 28 days in February in the data
			// file, even though 2004 in the forecast period has 29.
			// Therefore, calculate the number of data values in
			// the file based on the historical year and only
			// increment the date for the time series as values are
			// transferred.
			hdate = new DateTime(DateTime.PRECISION_MONTH);
			// Set the year to the historical year...
			hdate.setYear ( __iy + its );
			hdate.setMonth ( __im );
			// Now add the number of months that have been
			// processed...
			hdate.addMonth ( icm );
			// Now get the number of days in the month.  This does
			// not look at the hour so an hour of 24 is OK...
			ndays = TimeUtil.numDaysInMonth ( hdate );
			// Now compute the number of data that will need to be
			// transferred.  It may be less than the number read
			// because of the number of days in the month...
			ntran = ndays*24/__ts_dt;
			// Now loop through the data, using the actual 0-23
			// hour and the number of intervals.  It is OK to
			// attempt transferring data outside the actual TS
			// period because data outside the period will be
			// ignored (and should be missing).
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine,
				"Transferring " + ndays + " days, " + ntran +
				" values for historical " + hdate +
				" starting at " + date );
				Message.printDebug(1, routine,
				"The first trace data values are:");
			}
			for (	idata = 0; idata < ntran;
				idata++, date.addHour(__ts_dt) ) {
				
				// Add a debug statement to check the first
				// trace!
				if(Message.isDebugOn && its == 0) {
					Message.printDebug(1, routine,
					date.toString(
					DateTime.FORMAT_YYYY_MM_DD_HHmm)+
					" "+__ts[its].getDataValue(date));
				}
				
				data[idata] =
				(float)__ts[its].getDataValue(date);
			}
			// Fill in the rest of the array if necessary...
			ntran2 = 31*24/__ts_dt;
			for ( ; idata < ntran2; idata++ ) {
				data[idata] = missing_float;
			}
			idata = 0;	// Reset array position.
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine,
				"Writing trace [" + its + "] " +
				__ts[its].getSequenceNumber() +
				" conditional month [" + icm + "]" );
			}

			// Loop through the data in the month,
			// incrementing the hour to assign the data...
			for ( i = 0; i < ntran2; i++ ) {
				fp.writeEndianFloat(data[i]); 
			}
		}
	}
	}
	catch ( Exception e ) {
		// Should not happen if loops above are correct...
		Message.printWarning ( 2, routine, "Unexpected write error." );
		Message.printWarning ( 2, routine, e );
		throw new Exception ( "Unexpected write error" );
	}

	// Close the file...
	fp.close();
	fp = null;
}

// REVISIT SAM 2004-12-01 The byte conversions below seem to work like this:
// The file is always big-endian because of the defaults.  The byte shifts
// convert to little endian in memory.  The write method writes as big-endian,
// which essentially passes through the data.  Therefore, the in-memory little
// endian values are passed through to the file.  I think that the method above
// works correctly and is less convoluted since the conversions occur in the
// low-level code.
/**
This method appears to work but does not use the EndianDataOutputStream.
Retain the method for comparison until the other version is verified to work.

Write the ESP trace ensemble to an ESP trace ensemble binary file.  The trace
ensemble in memory must be complete.  This method was ported from the C++
ESPTraceEns.writeBinOutput() method.  Note that a new file is created, even if
the data were read from the same file originally.  Because ESP trace files are
not specifically little- or big-endian, write using the endianness of the
current machine.
@param fname Name of file to write.  This MUST follow the format
FGID.SEGID.DataType.Interval.CS (e.g., PRLI.PRLI.QINE.06.CS).  This is used
internally to determine some information about the trace.
@exception Exception if there is an error writing the file.
*/
public void writeESPTraceEnsembleFileUsingRandomAccessFile ( String fname )
throws Exception
{
	// Local variables
	int i,recordIndex;
	String routine = "NWSRFS_ESPTraceEnsemble.writeESPTraceEnsembleFile";
	byte [] record;
	byte intByte;
	byte[] stringByte = new byte[__headerLength];
	int floatByte;
	String parseString;
	String nullString = new String();
	String prsf_string;
	String full_fname = null;
	File f = null;
	EndianRandomAccessFile traceRAF;

	// Determine if the passed in filename is an absolute or relative
	// path
	if(fname.startsWith("/"))
		full_fname = fname;
	else
		full_fname = IOUtil.getPathUsingWorkingDir(fname);

	f = new File ( full_fname );

	if ( f.exists() ) {
		f.delete();
	}

	// Open RandomAccessFile
	traceRAF = new EndianRandomAccessFile(full_fname,"rw"); 

	// This is important because the read code uses the ESP file name to
	// get the TSIdent information...
	String espfname = f.getName();

	//
	// Format version. A 4 byte float converted back to bytes to write.
	// No index is needed since we are just writing bytes to the file and
	// letting the file move its position as the write is done. Sort of like
	// a stream....
	record = new byte[4];
	Message.printStatus(2,routine,"Writing version \"" + __format_ver+"\"");
	floatByte = Float.floatToIntBits(__format_ver);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// Segment ID. An 8 byte String. Convert to bytes.
	record = new byte[8];
	Message.printStatus ( 2, routine,"Writing seg_id \"" + __seg_id + "\"");
	parseString = __seg_id;
	for(i=0;i<8-__seg_id.length();i++)
		parseString += " ";

	record = parseString.getBytes();
	__dmi.write(traceRAF,__big_endian,record,8,false);

	//
	// Time series Id. An 8 byte String
	record = new byte[8];
	Message.printStatus ( 2, routine, "Writing ts_id \"" + __ts_id + "\"" );
	parseString = __ts_id;
	for(i=0;i<8-__ts_id.length();i++)
		parseString += " ";

	record = parseString.getBytes();
	__dmi.write(traceRAF,__big_endian,record,8,false);

	//
	// Time series type. A 4 byte String
	record = new byte[4];
	Message.printStatus ( 2, routine,
				"Writing ts_type \"" + __ts_type + "\"" );
	parseString = __ts_type;
	for(i=0;i<4-__ts_type.length();i++)
		parseString += " ";

	record = parseString.getBytes();
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// Time series data interval. A 4 byte integer
	record = new byte[4];
	Message.printStatus ( 2, routine,
				"Writing data interval \"" + __ts_dt + "\"" );
	floatByte = Float.floatToIntBits((float)__ts_dt);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// Simulation flag. A 4 byte integer
	record = new byte[4];
	Message.printStatus ( 2, routine,
				"Writing simflag \"" + __simflag + "\"" );
	floatByte = Float.floatToIntBits((float)__simflag);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// Time series Unit. A 4 byte String
	record = new byte[4];
	Message.printStatus ( 2, routine,
				"Writing ts_unit \"" + __ts_unit + "\"" );
	parseString = __ts_unit;
	for(i=0;i<4-__ts_unit.length();i++)
		parseString += " ";

	record = parseString.getBytes();
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// The now datetime value. This is 5 4 byte integers
	record = new byte[4];
	DateTime now = new DateTime ( DateTime.DATE_CURRENT );
	Message.printStatus ( 2, routine,
				"Writing datetime \"" + now + "\"" );
	floatByte = Float.floatToIntBits((float)now.getMonth());
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false); // Month

	floatByte = Float.floatToIntBits((float)now.getDay());
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false); // Day

	floatByte = Float.floatToIntBits((float)now.getYear());
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false); // Year

	floatByte = Float.floatToIntBits((float)now.getHour()*100+
					(float)now.getMinute());
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false); // Hour/Min

	floatByte = Float.floatToIntBits((float)now.getSecond());
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false); // Sec/Milisec


	//
	// Month of the first day of the Time series. A 4 byte integer	
	record = new byte[4];
	Message.printStatus ( 2, routine,
				"Writing im \"" + __im + "\"" );
	floatByte = Float.floatToIntBits((float)__im);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// Year of the first day of the Time series. A 4 byte integer
	record = new byte[4];
	Message.printStatus ( 2, routine,
				"Writing iy \"" + __iy + "\"" );
	floatByte = Float.floatToIntBits((float)__iy);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// Start of the traces. A 4 byte integer
	record = new byte[4];
	Message.printStatus ( 2, routine,
			"Writing start of traces \"" + __idarun + "\"" );
	floatByte = Float.floatToIntBits((float)__idarun);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// End of traces. A 4 byte integer
	record = new byte[4];
	Message.printStatus ( 2, routine,
			"Writing end of traces \"" + __ldarun + "\"" );
	floatByte = Float.floatToIntBits((float)__ldarun);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// Carryover day. A 4 byte integer
	record = new byte[4];
	Message.printStatus ( 2, routine,
				"Writing carryover day \"" + __ijdlst + "\"" );
	floatByte = Float.floatToIntBits((float)__ijdlst);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// Carryover hour. A 4 byte integer
	record = new byte[4];
	Message.printStatus ( 2, routine,
				"Writing carryover hour \"" + __ihlst + "\"" );
	floatByte = Float.floatToIntBits((float)__ihlst);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// Last day of forecast. A 4 byte integer
	record = new byte[4];
	Message.printStatus ( 2, routine,
			"Writing last day of forecast \"" + __ljdlst + "\"" );
	floatByte = Float.floatToIntBits((float)__ljdlst);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// Last hour of forecast. A 4 byte integer
	record = new byte[4];
	Message.printStatus ( 2, routine,
			"Writing last hour of forecast \"" + __lhlst + "\"" );
	floatByte = Float.floatToIntBits((float)__lhlst);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// Number of traces. A 4 byte integer
	record = new byte[4];
	Message.printStatus ( 2, routine,
			"Writing number of traces \"" + __n_traces + "\"" );
	floatByte = Float.floatToIntBits((float)__n_traces);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// Number of conditional months. A 4 byte integer
	record = new byte[4];
	Message.printStatus ( 2, routine,
		"Writing number of conditional months \"" + __ncm + "\"" );
	floatByte = Float.floatToIntBits((float)__ncm);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// NWSRFS Time zone. A 4 byte integer
	record = new byte[4];
	Message.printStatus ( 2, routine,
				"Writing time zone \"" + __nlstz + "\"" );
	floatByte = Float.floatToIntBits((float)__nlstz);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// The NWSRFS daylight savings time flag. A 4 byte integer
	record = new byte[4];
	Message.printStatus ( 2, routine,
			"Writing daylight saving time \"" + __noutds + "\"" );
	floatByte = Float.floatToIntBits((float)__noutds);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// Record number of the first trace data. A 4 byte integer
	record = new byte[4];
	Message.printStatus ( 2, routine,
		"Writing first record with trace data \"" + __irec + "\"" );
	floatByte = Float.floatToIntBits((float)__irec);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// The Unit dimensions from the NWS data units. A 4 byte String
	record = new byte[4];
	Message.printStatus ( 2, routine,
				"Writing unit dimensions \"" + __dim + "\"" );
	parseString = __dim;
	for(i=0;i<4-__dim.length();i++)
		parseString += " ";

	record = parseString.getBytes();
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// Time scale of code. A 4 byte String
	record = new byte[4];
	Message.printStatus ( 2, routine,
			"Writing time scale code \"" + __tscale + "\"" );
	parseString = __tscale;
	for(i=0;i<4-__tscale.length();i++)
		parseString += " ";

	record = parseString.getBytes();
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// Segment description. A 20 byte String
	record = new byte[20];
	Message.printStatus ( 2, routine,
			"Writing segment description \"" + __segdesc + "\"" );
	parseString = __segdesc;
	for(i=0;i<20-__segdesc.length();i++)
		parseString += " ";

	record = parseString.getBytes();
	__dmi.write(traceRAF,__big_endian,record,20,false);

	//
	// Latitude. A 4 byte float
	record = new byte[4];
	Message.printStatus ( 2, routine,
				"Writing latitude \"" + __xlat + "\"" );
	floatByte = Float.floatToIntBits(__xlat);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false); 

	//
	// Longitude. A 4 byte float
	record = new byte[4];
	Message.printStatus ( 2, routine,
				"Writing longitude \"" + __xlong + "\"" );
	floatByte = Float.floatToIntBits(__xlong);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false); 

	//
	// Forecast group. An 8 byte String
	record = new byte[8];
	Message.printStatus ( 2, routine,
				"Writing forecast group \"" + __fg + "\"" );
	parseString = __fg;
	for(i=0;i<8-__fg.length();i++)
		parseString += " ";

	record = parseString.getBytes();
	__dmi.write(traceRAF,__big_endian,record,8,false);

	//
	// Carryover group. An 8 byte String
	record = new byte[8];
	Message.printStatus ( 2, routine,
				"Writing carryover group \"" + __cg + "\"" );
	parseString = __cg;
	for(i=0;i<8-__cg.length();i++)
		parseString += " ";

	record = parseString.getBytes();
	__dmi.write(traceRAF,__big_endian,record,8,false);

	//
	// The RFC name. An 8 byte String
	record = new byte[8];
	Message.printStatus ( 2, routine,
			"Writing RFC name \"" + __hdr_id_rfcname + "\"" );
	parseString = __hdr_id_rfcname;
	for(i=0;i<8-__hdr_id_rfcname.length();i++)
		parseString += " ";

	record = parseString.getBytes();
	__dmi.write(traceRAF,__big_endian,record,8,false);

	//
	// Trace file name without path. An 80 byte String
	record = new byte[80];

	// If __espfname is missing then use the method argument fname
	if(__espfname.length() == 0 || __espfname.compareTo(" ") == 0)
		__espfname = fname; 

	Message.printStatus ( 2, routine,
				"Writing trace name \"" + __espfname + "\"" );
	parseString = __espfname;
	for(i=0;i<80-__espfname.length();i++)
		parseString += " ";

	record = parseString.getBytes();
	__dmi.write(traceRAF,__big_endian,record,80,false);

	//
	// The prsf flag string. An 80 byte String
	record = new byte[80];
	if(__prsf_flag == 1) 
	{
		prsf_string = "PRSF";
	}
	else 
	{	
		prsf_string = "";
	}
	Message.printStatus ( 2, routine,
				"Writing prsf string \"" + prsf_string + "\"" );
	parseString = prsf_string;
	for(i=0;i<80-prsf_string.length();i++)
		parseString += " ";

	record = parseString.getBytes();
	__dmi.write(traceRAF,__big_endian,record,80,false);

	//
	// User comments. An 80 byte String
	record = new byte[80];
	Message.printStatus ( 2, routine,
				"Writing trace name \"" + __esptext + "\"" );
	parseString = __esptext;
	for(i=0;i<80-__esptext.length();i++)
		parseString += " ";

	record = parseString.getBytes();
	__dmi.write(traceRAF,__big_endian,record,80,false);

	//
	// Adjustment counter. A 4 byte integer
	record = new byte[4];
	Message.printStatus ( 2, routine,
			"Writing adjustment counter \"" + __adjcount + "\"" );
	floatByte = Float.floatToIntBits((float)__adjcount);
	record[3] = (byte)(floatByte>>24);
	record[2] = (byte)(floatByte>>16);
	record[1] = (byte)(floatByte>>8);
	record[0] = (byte)(floatByte);
	__dmi.write(traceRAF,__big_endian,record,4,false);

	//
	// Words 104-124. There needs to be 84 bytes of nulls written to the
	// file
	record = new byte[84];
	for(i=0;i<84;i++)
		nullString += " ";

	record = nullString.getBytes();
	__dmi.write(traceRAF,__big_endian,record,84,false);

	
	// Write the data...

	int ndata = __rec_words/4;	// Floats per line - should be 31
	int nrecpermonth = (ndata/31)*(24/__ts_dt);
	float [] data = new float[744];	// Enough for 31 days x 24 1-hour
					// values...
	int icm;	// Loop counter for conditional months in each trace.
	int ndays;	// Number of days per month.
	int idata;	// Position in data array for month
	int ntran;	// Number of data to transfer for a month's data
	int ntran2;	// Number of data to transfer for a full month's data
	DateTime date;	// Date/time to used to transfer data array to time
			// series.
	DateTime hdate;	// Date/time to used to evaluate a historical date/time.
	float missing_float = (float)-999.0;
	try {
	// Loop through the number of time series traces...
	for ( int its = 0; its < __n_traces; its++ ) {
		// Initialize the date that will be used to transfer data to
		// the starting interval in the data file.
		// The dates in the file use the hour 1-24.  However,
		// the time series have been allocated in readHeader()
		// using hour 0-23.  Therefore, the starting date/time
		// must be properly set.  Each month of data in the file
		// corresponds to hour __ts_dt of the first day of the
		// HISTORICAL month, which will only be an issue if
		// __ts_dt == 24.  Take care to
		// set the starting date correctly and then just add the
		// interval as the data are processed.
		// First determine the hour 24 date/time, mainly to get
		// the correct month, and year.
		// Start by setting to the initial value...
		date = NWSRFS_Util.toDateTime24(__start_date,true);
		// Set the day to 1 and the hour to the interval...
		date.setDay ( 1 );
		date.setHour ( __ts_dt );
		// Convert back to 0-23 hour...
		date = NWSRFS_Util.toDateTime23(date,true);
		// Loop through the number of conditional months (the month is
		// incr...
		for ( icm = 0; icm < __ncm; icm++ ) {
			// Loop through the records in the month...
			// Transfer a complete month into the data array.
			// Determine the number of values available in the file
			// to be transferred.  The months in the file
			// correspond to the historical months, not the
			// real-time forecast years.  Therefore, for example,
			// if the forecast period is May 2002 through May 2004
			// but the starting historical years are 1995 - 1998
			// (4 traces), the second trace (historical years
			// 1996-1997) will have 28 days in February in the data
			// file, even though 2004 in the forecast period has 29.
			// Therefore, calculate the number of data values in
			// the file based on the historical year and only
			// increment the date for the time series as values are
			// transferred.
			hdate = new DateTime(DateTime.PRECISION_MONTH);
			// Set the year to the historical year...
			hdate.setYear ( __iy + its );
			hdate.setMonth ( __im );
			// Now add the number of months that have been
			// processed...
			hdate.addMonth ( icm );
			// Now get the number of days in the month.  This does
			// not look at the hour so an hour of 24 is OK...
			ndays = TimeUtil.numDaysInMonth ( hdate );
			// Now compute the number of data that will need to be
			// transferred.  It may be less than the number read
			// because of the number of days in the month...
			ntran = ndays*24/__ts_dt;
			// Now loop through the data, using the actual 0-23
			// hour and the number of intervals.  It is OK to
			// attempt transferring data outside the actual TS
			// period because data outside the period will be
			// ignored (and should be missing).
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine,
				"Transferring " + ndays + " days, " + ntran +
				" values for historical " + hdate +
				" starting at " + date );
				Message.printDebug(1, routine,
				"The first trace data values are:");
			}
			for (	idata = 0; idata < ntran;
				idata++, date.addHour(__ts_dt) ) {
					
				// Add a debug statement to check the first
				// trace!
				if(Message.isDebugOn && its == 0) {
					Message.printDebug(1, routine,
					date.toString(
					DateTime.FORMAT_YYYY_MM_DD_HHmm)+
					" "+__ts[its].getDataValue(date));
				}
				
				data[idata] =
				(float)__ts[its].getDataValue(date);
			}
			// Fill in the rest of the array if necessary...
			ntran2 = 31*24/__ts_dt;
			for ( ; idata < ntran2; idata++ ) {
				data[idata] = missing_float;
			}
			idata = 0;	// Reset array position.
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine,
				"Writing trace [" + its + "] " +
				__ts[its].getSequenceNumber() +
				" conditional month [" + icm + "]" );
			}
			
			// Loop through the data in the month,
			// incrementing the hour to assign the data...
			for ( i = 0; i < ntran2; i++ ) {
				record = new byte[4];
				floatByte = Float.floatToIntBits(data[i]);
				record[3] = (byte)(floatByte>>24);
				record[2] = (byte)(floatByte>>16);
				record[1] = (byte)(floatByte>>8);
				record[0] = (byte)(floatByte);
				__dmi.write(traceRAF,__big_endian,record,4,
				false); 
			}
		}
	}
	}
	catch ( Exception e ) {
		// Should not happen if loops above are correct...
		Message.printWarning ( 2, routine, "Unexpected write error." );
		Message.printWarning ( 2, routine, e );
		throw new Exception ( "Unexpected write error" );
	}
}

} // End class ESPTraceEnsemble
