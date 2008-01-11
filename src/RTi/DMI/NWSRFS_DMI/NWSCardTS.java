// ----------------------------------------------------------------------------
// NWSCardTS - class for manipulating National Weather Service DATACARD TS in
//		a single time series or trace format.
// ----------------------------------------------------------------------------
// Notes:
// ----------------------------------------------------------------------------
// History:
//
// Apr 1996	Steven A. Malers, RTi	Begin to develop based on I/O code in
//					the TS library.
// 11 Nov 1996	Matthew J. Rutherford,	Added readPersistentHeader, and the
//		RTi			_input_format data member.
// 13 Jan 97	MJR, RTi		Overloaded readPersistent and
//					readPersistentHeader to accept a file
//					name.
// 20 May 1998	SAM, RTi		Overload writePersistent to take a TS.
// 11 Jun 1999	SAM, RTi		Port to Java.
// 29 Nov 2000	SAM, RTi		Add getSample().  Remove printSample().
// 05 Dec 2000	SAM, RTi		Change so that this class is
//					NOT derived from HourTS and rename to
//					NWSCardTS.  Handle I/O similar to
//					DateValueTS and other time series.
//					Update the readTimeSeries() code based
//					on Michael Thiemann's updates in the
//					C++ version.  Verify the
//					writeTimeSeries() code with C++.
//					Get rid of 12Z crap for dates.
// 09 Jan 2001	SAM, RTi		Change IO to IOUtil.
// 11 Jan 2001	SAM, RTi		Fix problem setting end of month dates.
// 21 Feb 2001	SAM, RTi		Add call to setInputName() for read.
// 25 Apr 2001	SAM, RTi		Fix bug where sequence number was
//					overflowing when greater than 9999 due
//					to I4 format.  Change so that the
//					maximum value encountered is 9999.
// 06 Sep 2001	SAM, RTi		Fix minor bug where year on card records
//					was not padded with zeros.
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
// 2002-01-31	SAM, RTi		Added isNWSCardFile().  Overload
//					readTimeSeries() to take a TSID and a
//					file name.
// 2002-02-11	SAM, RTi		Update to set the input type and name
//					in TSIdent - to go with the new
//					conventions.  Do not set the data source
//					to "NWSCard" (set to "").  The input
//					source is now used to indicate NWSCard
//					data.
// 2002-05-12	SAM, RTi		Fix so writeTimeSeries() correctly
//					prints the interval as 24 for daily
//					time series.
// 2002-05-22	SAM, RTi		Fix so that readTimeSeries() use the
//					working directory if available.
// 2002-12-06	SAM, RTi		Fix so that writeTimeSeries() uses the
//					working directory if available (why not
//					done before?).
// 2003-01-16	SAM, RTi		Change so when writing time series use
//					a FileOutputStream, not a FileWriter.
//					Using the FileWriter was resulting in
//					incomplete output.  Maybe this will get
//					fixed in Java 1.4.0 on Linux?
// 2003-02-14	SAM, RTi		Fix bug where large numbers in data were
//					overflowing the NwsCard output format.
//					Fix by checking for the largest value
//					for output and setting the format width
//					appropriately.  There is still a
//					limitation that NwsCard files that don't
//					have spaces between data values will not
//					be read in correctly.
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
//					* Change TSUnits to DataUnits.
//					* Change TS.INTERVAL* to TimeInterval.
// 2004-04-22	SAM, RTi		Fix bug where large data values that are
//					not separated by whitespace do not read
//					properly.  Data are now read with
//					StringUtil.fixedRead().
// 2005-05-09 Luiz Teixeira		Added method isNWSCardTraceFile().
//					Upgrade the main readTimeSeries ().
//					method to add the capability to read the
//					NWS Card Trace File.  The method was
//					renamed readTimeSeriesList () and now
//					returns a vector of time series.
//					To maintain backward compatibility, a
//					new method readTimeSeries () with the
// 					same parameters as the original was
//					added. This new method simply calls the
// 					readTimeSeriesList(), and extracts the
//					time series it needs to return.
// 2005-05-11 	LT, RTi			Added overload for the method
//					Vector readTimeSeriesList (...) with
//					String fileName as the second parameter.
//					The main method uses BufferedReader as
//					its second parameter.
// 2005-05-16 	LT, RTi			Clean up and documentation.
// 2005-05-18 	LT, RTi			Additional clean up and documentation
//					following per-review (SAM's 2005-05-17)
// 2005-05-22 	LT, RTi			Additional clean up and documentation.
// 2005-06-02	SAM, RTi		* Print exceptions at level 3 so they
//					  don't show up in the message viewer.
// 2005-12-12	J. Thomas Sapienza, RTi	Expanded the main readTimeSeries() call
//					so that it can read 24 hour time series
//					as daily time series.
// 2006-01-18	JTS, RTi		Moved from RTi.TS package.
// 2006-02-01	JTS, RTi		Added getFileFilters().
// 2006-02-14	JTS, RTi		Corrected bug in code where if a read
//					period longer than the data in the file
//					was selected, readTimeSeries would 
//					break.
// 2007-05-02	SAM, RTi		Handle case where run period for traces starts
//					on the last day of month, which converts to the first day
//					of the next month - DID NOT FINISH WORK.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.DMI.NWSRFS_DMI;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Vector;

import javax.swing.filechooser.FileFilter;

import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.TS.TSLimits;
import RTi.TS.TSUtil;

import RTi.Util.GUI.SimpleFileFilter;

import RTi.Util.IO.DataUnits;
import RTi.Util.IO.DataUnitsConversion;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;

/**
The NWSCardTS class reads and writes National Weather Service Card format time
series files.  These files store data for 1-24 hour data intervals.  Times in
files go from hours 1 to 24 and an offset is applied to shift times to the
TS package standard of 0 to 23 hours.
*/
public class NWSCardTS
{

/**
Return a sample so that a user/developer knows what a file looks like.  Right
now, the samples are compiled into the code to make absolutely sure that the
programmer knows what sample is supported.
@return Sample file contents.
*/
public static Vector getSample ()
{	Vector	s = new Vector ( 50 );
	s.addElement (
"#NWSCard" );
	s.addElement (
"#" );
	s.addElement (
"# This is an example of a typical National Weather Service (NWS) CARD format");
	s.addElement (
"# time series, which can be used for hourly data (1-24 hours).  This format");
	s.addElement (
"# is commonly used by the NWS.  The NWS Card file uses hours 1 to 24 whereas");
	s.addElement (
"# in-memory time series storage uses 0-23.  The translation of date/times");
	s.addElement (
"# from the CARD file to in-memory time series occurs as follows as the file" );
	s.addElement (
"# is read (using a single 31-day month).  The inverse occurs when writing.");
	s.addElement (
"#" );
	s.addElement (
"# Data     | CARD         | Time Series | CARD          | Time Series" );
	s.addElement (
"# Interval | Start        | Start       | End           | End" );
	s.addElement (
"# ---------|--------------|-------------|---------------|--------------------" );
	s.addElement (
"# 6-Hour   | Day 1, Hr 6  | Day 1, Hr 6 | Day 31, Hr 24 | Mon 2, Day 1, Hr 0");
	s.addElement (
"# 24-Hour  | Day 1, Hr 24 | Day 2, Hr 0 | Day 31, Hr 24 | Mon 2, Day 1, Hr 0");
	s.addElement (
"#" );
	s.addElement (
"# If, for example, a DateValue time series is read and then is written as a" );
	s.addElement (
"# CARD file, then use a 1Day interval DateValue file and don't specify hour" );
	s.addElement (
"# in the dates, OR, use an hourly file and specify hours in the date/times." );
	s.addElement (
"# Otherwise, the precision of the input data may not translate correctly." );
	s.addElement (
"#" );
	s.addElement (
"# An example file is as follows and conforms to the following guidelines:");
	s.addElement (
"# * Only one time series per file." );
	s.addElement (
"# * The sequence number in data lines (field 3) has a maximum value of 9999.");
	s.addElement (
"# * Full months are included, with missing values as needed." );
	s.addElement (
"# * See the header below for more information." );
	s.addElement (
"# * Data are fixed format." );
	s.addElement (
"# * Comments in the file start with $ (these #-comments are for illustration");
	s.addElement (
"#   only." );
	s.addElement (
"# * Data lines are printed using the specified format." );
	s.addElement (
"# * Data lines have station, month, year (2 digit), count, data values." );
	s.addElement (
"#" );
	s.addElement (
"$  IDENTIFIER=STATIONX       DESCRIPTION=RIVER Y BELOW Z     " );
	s.addElement (
"$  PERIOD OF RECORD=08/1978 THRU 11/1995" );
	s.addElement (
"$  SYMBOL FOR MISSING DATA=-999.00   SYMBOL FOR ACCUMULATED DATA=-998.00" );
	s.addElement (
"$  TYPE=SQIN   UNITS=CMS    DIMENSIONS=L3/T   DATA TIME INTERVAL= 6 HOURS" );
	s.addElement (
"$  OUTPUT FORMAT=(3A4,2I2,I4,6F10.2)             " );
	s.addElement (
"DATACARD      SQIN L3/T CMS   6    26433                                  " );
	s.addElement (
" 8  1984 10   1984  6   F10.2       " );
	s.addElement (
"STATIONX     884   1     91.66     88.95     86.24     83.53     81.14     78.74" );
	s.addElement (
"STATIONX     884   2     76.35     73.96     73.00     72.04     71.07     70.11" );
	s.addElement ( "..." );
	s.addElement (
"STATIONX     884  20    299.88    296.23    273.81    251.39    228.97    206.55" );
	s.addElement (
"STATIONX     884  21    192.56    178.56    164.57    150.57" );
	s.addElement (
"STATIONX     984   1    145.28    139.99    134.70    129.41    123.45    117.50" );
	s.addElement (
"STATIONX     984   2    111.54    105.58    102.26     98.94     95.63     92.31" );
	s.addElement (
"STATIONX     984   3    163.89    235.48    307.07    378.65   1032.13   1685.60" );
	s.addElement ( "..." );

	return s;
}

/**
Determine whether a file is an NWSCARD file.  This can be used rather than
checking the source in a time series identifier.  If the file passes any of the
following conditions, it is assumed to be an NWSCARD file:
<ol>
<li>	A line starts with "$  IDENTIFIER=".</li>
<li>	A line starts with "$  TYPE=".</li>
<li>	A line starts with "DATACARD".</li>
</ol>
IOUtil.getPathUsingWorkingDir() is called to expand the filename.
*/
public static boolean isNWSCardFile ( String filename )
{
	BufferedReader in = null;
	String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
	try {
        in = new BufferedReader ( new InputStreamReader( IOUtil.getInputStream ( full_fname )) );

		// Read lines and check for common NWS Card file strings
		String string = null;
		boolean	is_nwscard = false;
		while( ( string = in.readLine() ) != null ) {

			if ( string.regionMatches( true,0,"DATACARD",0,8 ) ) {
				is_nwscard = true;
				break;
			}

			if ( string.charAt(0) == '$' &&
			     (string.regionMatches(true,0,"IDENTIFIER=",0,14)
			    ||string.regionMatches(true,0,"TYPE=",      0, 8)) )
			{
				is_nwscard = true;
				break;
			}

			if ( (string.length()==0) || (string.charAt(0)!='$') ) {
				break;
			}
		}

		in.close();
		in = null;
		string = null;
		return is_nwscard;
	}
	catch ( Exception e ) {
		return false;
	}
}

/**
Determine whether a file is a NWS Card Trace file.  If the file contains the
following strings, it is assumed to be an NWS Card Trace file:
<ol>
<li>	A line starts with "$  HISTORICAL RUN PERIOD=".</li>
<li>	A line starts with "$  NUMBER OF TRACES".</li>
<li>	A line starts with "$  MONTHS PER TRACE".</li>
</ol>
IOUtil.getPathUsingWorkingDir() is called to expand the filename.
@param filename - The file name.
*/
public static boolean isNWSCardTraceFile ( String filename )
{
	boolean is_NWSCardTraceFile = false;

	try {
		String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
		BufferedReader inBufferedReader = new BufferedReader (new InputStreamReader( IOUtil.getInputStream (full_fname)));

		is_NWSCardTraceFile = isNWSCardTraceFile ( inBufferedReader );

		inBufferedReader.close();
		inBufferedReader = null;

		return is_NWSCardTraceFile;

	}
	catch ( Exception e ) {
		return false;
	}
}

/**
Determine whether a file is a NWS Card Trace file. If the file contains all the
following strings, it is assumed to be an NWS Card Trace file:
<ol>
<li>	A line starts with "HISTORICAL RUN PERIOD=".</li>
<li>	A line starts with "NUMBER OF TRACES".</li>
<li>	A line starts with "MONTHS PER TRACE".</li>
</ol>
IOUtil.getPathUsingWorkingDir() is called to expand the filename.
@param inBufferedReader - Reference to a Buffered Reader object containing the 
file.
*/
private static boolean isNWSCardTraceFile ( BufferedReader inBufferedReader )
{
	try {
		boolean	is_nwsCardTrace  = false;
		boolean foundFirst       = false;
		boolean foundSecond      = false;
		boolean foundThird       = false;

		// Read lines and check for common NWS Card trace file strings.
		String string = null;
		while( (string = inBufferedReader.readLine()) != null ) {

			if ( string.charAt(0)=='$' ) {

				if ( StringUtil.indexOfIgnoreCase( string, "HISTORICAL RUN PERIOD=", 0) != -1 ) {
                   	foundFirst = true;
               	}

	            if ( StringUtil.indexOfIgnoreCase( string, "NUMBER OF TRACES=", 0) != -1 ) {
	               	foundSecond = true;
	            }

	            if ( StringUtil.indexOfIgnoreCase( string, "MONTHS PER TRACE=", 0) != -1 ) {
	               	foundThird = true;
	            }
	        }

    		if ( foundFirst && foundSecond && foundThird ) {
   				is_nwsCardTrace = true;
				break;
   			}

			if ( (string.length()==0) || (string.charAt(0)!='$') ) {
				break;
			}
		}

		string = null;
		return is_nwsCardTrace;
	}
	catch ( Exception e ) {
		return false;
	}
}

/**
Read a single time series from a file.
This version works with NWS Card file (single TS per file) or will return the
first trace out of NWS Card Trace files.
@param fname Name of file to read.
@return HourTS for data in the file or null if there is an error reading the
time series.
@exception IOException If an error occurs reading the file.
*/
public static TS readTimeSeries ( String fname )
throws IOException
{
	return readTimeSeries ( fname, null, null, null, true );
}

/**
Read a single time series from a file.
This version works with NWS Card file (single TS per file) or will return the
first trace out of NWS Card Trace files.
@return a pointer to a newly-allocated time series if successful, a null
pointer if not.
@param fname The input file name.
@param req_date1 Requested starting date to initialize the period (or null to
read the entire time series).  If specified, the precision must be to hour.
@param req_date2 Requested ending date to initialize the period (or null to
read the entire time series).  If specified, the precision must be to hour.
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read (false=no, true=yes).
IOUtil.getPathUsingWorkingDir() is called to expand the filename.
@exception IOException If an error occurs reading the file.
*/
public static TS readTimeSeries ( String fname,
				  DateTime req_date1,
				  DateTime req_date2,
				  String req_units,
				  boolean read_data )
throws IOException			
{
	TS	ts = null;

	String full_fname = IOUtil.getPathUsingWorkingDir ( fname );
	try {
        BufferedReader in = new BufferedReader ( new InputStreamReader(	IOUtil.getInputStream ( full_fname )) );
		// Don't have a requested time series...
		ts = readTimeSeries ( (TS)null, in,	req_date1, req_date2, req_units, read_data );
			
		// Set some time series properties.	
		ts.setInputName ( fname );
		ts.getIdentifier().setInputType( "NWSCard" );
		ts.getIdentifier().setInputName( fname );
		
		in.close();
		in = null;
	}
	catch ( Exception e ) {
		Message.printWarning( 2, "NWSCardTS.readTimeSeries(String,...)", "Unable to open file \"" + fname + "\"" );
	}
	return ts;
}

/**
Read a single time series from a file.
This version when working with NWS Card file (single TS per file) will use
the time series identifier passed in (first parameter) to set properties
for the returning time series. 
When working with NWS Card Trace file it will will return a single time 
series representing the trace that matches the time series identifier passed in
(first trace).
@return a pointer to a newly-allocated time series if successful, a null
pointer if not.
@param tsident_string Identifier of a time series to be used to customize the 
identifier of the returning time series. 
@param fname The input file name.
@param req_date1 Requested starting date to initialize the period (or null to
read the entire time series).  If specified, the precision must be to hour.
@param req_date2 Requested ending date to initialize the period (or null to
read the entire time series).  If specified, the precision must be to hour.
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read (false=no, true=yes).
@exception IOException If an error occurs reading the file.
*/
public static TS readTimeSeries ( String tsident_string,
				  String 	fname,
				  DateTime 	req_date1,
				  DateTime 	req_date2,
				  String 	req_units,
				  boolean 	read_data )
throws IOException					
{
	String routine = "NWSCardTS.readTimeSeries(String,...)";
	String msg = null;
				
	// Check if we are dealing with a NWS Trace Card file.
	boolean is_nwsCardTrace = isNWSCardTraceFile( fname );
	
	TS ts = null;	
	if ( !is_nwsCardTrace ) {
		// Dealing with NWS Card single time series file.
		// Instantiate the BufferedReader object
		BufferedReader in = null;
		String full_fname = IOUtil.getPathUsingWorkingDir ( fname );
		try {	
			in = new BufferedReader ( new InputStreamReader(
			IOUtil.getInputStream ( full_fname ) ) );
		}
		catch ( Exception e ) {
            String message = "Unable to open file \"" + fname + "\"";
			Message.printWarning( 2, routine, message );
			throw new IOException ( message );	
		}
		
		// Read a NWS Card single time series file.
		try {				
			// Don't have a requested time series...
			ts = readTimeSeries ( (TS) null, in, req_date1, req_date2, req_units, read_data );
			
			// Set some time series properties.
			ts.setInputName  ( fname );
			ts.setIdentifier ( tsident_string );
			ts.getIdentifier().setInputType( "NWSCard" );
			ts.getIdentifier().setInputName( fname );
			in.close();
			in = null;
			full_fname = null;	
		}
		catch ( Exception e ) {
			msg = "Error reading the NWS Card file \"" + fname + "\"";
			Message.printWarning ( 2, routine, msg );
			Message.printWarning ( 3, routine, e );
			in.close();
			throw new IOException ( msg );
		}	
	}
	else {
		// Dealing with NWS Card Trace file.
		try {
			Vector TSList = null;
			
			// Read all the time series in the file.
			// REVISIT [LT 2005-05-18] This could be improved if the
			// processing method is improved to return only the 
			// requested time series.  For this to happen all code 
			// setting time series properties in the higher level
			// method overloads, should be properly moved to the 
			// lower level processing overload.  
			TSList = readTimeSeriesList ( (TS) null, fname, req_date1, req_date2, req_units, read_data );
			
			// Retrieve only the requested time series from the
			// returning vector 
			if ( TSList != null ) {
				int tsCount = TSList.size();
				if ( tsCount != 0 ) {
				    for ( int i = 0; i<tsCount; i++ ) {
				    	ts = (TS) TSList.elementAt(i);
				    	if ( tsident_string.equalsIgnoreCase (
				    	    ts.getIdentifierString() ) ) {
				    	    break;
				    	}
				    } 
				}
			}
			TSList = null;
			
		}
		catch ( Exception e ) {
			msg = "Error reading the NWS Card Trace file \"" + fname + "\"";
			Message.printWarning ( 2, routine, msg );
			Message.printWarning ( 3, routine, e );
			throw new IOException ( msg );
		}
	}
	
	return ts;
}

/**
Read a single time series from a file.
This version works with NWS Card file (single TS per file) or will return the
first trace out of NWS Card Trace files.
This private method overload is called from the public overload versions
returning a single time series, where the BufferedReader object is instantiated
and additional post processing are still performed to set the returning time
series identifier.
@param req_ts Pointer to time series to fill.  If null return a new time series.
This parameter is used only when processing single time series from a NWS Card
file.  All data are reset, except for the identifier, which is assumed to have
been set in the calling code.
This parameter is ignored when processing NWS Trace Card.  In this case the
returning vector will contain several new time series, one for each trace
available in the file.
@param in Reference to open input stream.
@param fname The input file name.
@param req_date1 Requested starting date to initialize the period (or null to
read the entire time series).  If specified, the precision must be to hour.
@param req_date2 Requested ending date to initialize the period (or null to
read the entire time series).  If specified, the precision must be to hour.
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read (false=no, true=yes).
@exception IOException If an error occurs reading the file.
*/
private static TS readTimeSeries ( TS req_ts, BufferedReader in, DateTime req_date1, DateTime req_date2,
				  String req_units, boolean read_data )
throws IOException				  
{
	//String	routine = "NWSCardTS.readTimeSeries";

	TS ts         = null;
	Vector TSList = null;

	// Read the time series.
	// This version should return only one time series. When processing a 
	// NWS Card Trace only the first time series is expected in the
	// returning vector.  To ensure this behaviour, the first parameter 
	// "is_nwsCardTrace" to the processing method is always passed as
	// "false", even if the file is a NWS Card Trace file.  This ensure
	// that the file will alwasy be processed as a NWS Card single time
	// series file and the returning time series is the one expected. 
	TSList = readTimeSeriesList ( false, req_ts, in, req_date1, req_date2, req_units, read_data );

	// One time series is expected. So make sure the returned vector is not
	// null and contains one element. Retrieve the element.
	if ( TSList != null ) {
		if ( TSList.size() != 0 ) {
			ts = (TS) TSList.elementAt(0);
		}
	}

	// Return the reference to the time series or null if the time series was not properly read.
	return ts;
}

/**
Read one or more time series from a file.
It will read all the traces from a NWS Card Trace file or a single time series
from a NWS Card file.
@return a vector containing reference to one or more time series (or null in 
case of problems) when processing a NWS Card or a NWS Card Trace file.
@param fname The input file name.
@param req_date1 Requested starting date to initialize the period (or null to
read the entire time series).  If specified, the precision must be to hour.
@param req_date2 Requested ending date to initialize the period (or null to
read the entire time series).  If specified, the precision must be to hour.
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read (false=no, true=yes).
@exception IOException If an error occurs reading the file.
*/
public static Vector readTimeSeriesList ( String fname, DateTime req_date1, DateTime req_date2,
					  String req_units, boolean read_data )
throws IOException
{
	return readTimeSeriesList (	null, fname, req_date1,	req_date2, req_units, read_data );
}

/**
Read one or more time series from a file.
It will read all the traces from a NWS Card Trace file or a single time series
from a NWS Card file.
@return a vector containing reference to one or more time series (or null in 
case of problems) when processing a NWS Card or a NWS Card Trace file.
@param req_ts Pointer to the time series to fill in with data from the file. If 
null, return a new time series.  All data are reset, except for the identifier, 
which is assumed to have been set in the calling code.  This parameter is used 
only when processing NWS Card single time series file.  It will be ignored when 
processing NWS Trace Card.  In this case only the returning vector will contain 
one or more new time series, one for each trace available in the file.
@param fname The input file name.
@param req_date1 Requested starting date to initialize the period (or null to
read the entire time series).  If specified, the precision must be to hour.
@param req_date2 Requested ending date to initialize the period (or null to
read the entire time series).  If specified, the precision must be to hour.
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read (false=no, true=yes).
@exception IOException If an error occurs reading the file.
*/
public static Vector readTimeSeriesList ( TS req_ts, String fname, DateTime req_date1, DateTime req_date2,
					  String req_units, boolean read_data )
throws IOException
{
	return readTimeSeriesList(req_ts, fname, req_date1, req_date2, req_units, read_data, null);
}

/**
Read one or more time series from a file.
It will read all the traces from a NWS Card Trace file or a single time series
from a NWS Card file.
@return a vector containing reference to one or more time series (or null in 
case of problems) when processing a NWS Card or a NWS Card Trace file.
@param req_ts Pointer to the time series to fill in with data from the file. If 
null, return a new time series.  All data are reset, except for the identifier, 
which is assumed to have been set in the calling code.  This parameter is used 
only when processing NWS Card single time series file.  It will be ignored when 
processing NWS Trace Card.  In this case only the returning vector will contain 
one or more new time series, one for each trace available in the file.
@param fname The input file name.
@param req_date1 Requested starting date to initialize the period (or null to
read the entire time series).  If specified, the precision must be to hour.
@param req_date2 Requested ending date to initialize the period (or null to
read the entire time series).  If specified, the precision must be to hour.
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read (false=no, true=yes).
@param props properties to control how the file is read.  Recognized 
properties are:<p>
<table>
<tr>
<td><b>Property Name</b></td>
<td><b>Description</b></td>
<td><b>Default Value</b></td>
</tr>
<tr>
<td>Read24HourAsDay</td>
<td>Specifies whether 24 hour time series should be read as daily time series
instead (True or False).</td>
<td>False</td>
</tr>
</table><p>
This parameter can be null, and if so, defaults to false.
@exception IOException If an error occurs reading the file.
*/
public static Vector readTimeSeriesList(TS req_ts, String fname,
DateTime req_date1, DateTime req_date2, String req_units, boolean read_data,
PropList props)
throws IOException {
	String routine = "NWSCardTS.readTimeSeriesList";
	Vector TSList = null;

	try {
		String full_fname = IOUtil.getPathUsingWorkingDir(fname);
		BufferedReader in = new BufferedReader(new InputStreamReader( IOUtil.getInputStream(full_fname)));

		// Check if we are dealing with a NWS Trace Card file.
		boolean is_nwsCardTrace = isNWSCardTraceFile(in);
		// Create a new buffer to start fresh from the begining of the
		// file.
		in = new BufferedReader(new InputStreamReader( IOUtil.getInputStream(full_fname)));

		// Read the time series list
		TSList = readTimeSeriesList ( is_nwsCardTrace, req_ts, in, req_date1, req_date2, req_units, read_data, props);
				      		
		// Update the time series InputType (NWSCard) and InputName
		// (fname) properties.
		TS ts = null;
		if (TSList != null) {
			int tsCount = TSList.size();
			if (tsCount != 0) {
				for (int i = 0; i<tsCount; i++) {
					ts = (TS)TSList.elementAt(i);
					
					// Set some time series properties.
					ts.setInputName(fname);
					ts.getIdentifier().setInputType( "NWSCard");
					ts.getIdentifier().setInputName(fname);
				} 
			}
		}	
		
		// Clean up
		in.close();
		in = null;
		full_fname = null;
	}
	catch (Exception e) {
        String message = "Unable to read file \"" + fname + "\"";
		Message.printWarning(3, routine, message );
		Message.printWarning(3, routine, e );
        throw new IOException ( message );
	}

	return TSList;
}

/**
Read one or more time series from a file.
It will read all the traces from a NWS Card Trace file or a single time series
from a NWS Card file. 
This private method overload is called from the public overload versions
returning a single time series and a vector of time series, where the 
BufferedReader object is instantiated and additional post processing are still
performed.
@return a vector containing reference to one or more time series (or null in 
case of problems) when processing a NWS Card or a NWS Card Trace file.
@param is_nwsCardTrace flag indicating that the file is to be read as a NWS Card
single time series file (false) or a NWS Card Trace file (true).
Notice that even if the file is a NWS Card Trace file, this parameter can be
passed as true, to force this method to read the file as a NWS Card single
time series file (see the last version of this method returning TS above).      
@param req_ts Pointer to the time series to fill in with data from the file. If 
null, return a new time series.  All data are reset, except for the identifier, 
which is assumed to have been set in the calling code.  This parameter is used 
only when processing NWS Card single time series file.  It will be ignored when 
processing NWS Trace Card.  In this case only the returning vector will contain 
one or more new time series, one for each trace available in the file.
@param in Reference to open input stream.
@param req_date1 Requested starting date to initialize the period (or null to
read the entire time series).  If specified, the precision must be to hour.
@param req_date2 Requested ending date to initialize the period (or null to
read the entire time series).  If specified, the precision must be to hour.
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read (false=no, true=yes).
@exception IOException If an error occurs reading the file.
*/
private static Vector readTimeSeriesList ( boolean is_nwsCardTrace, TS req_ts, BufferedReader in,
					  DateTime req_date1, DateTime req_date2, String req_units, boolean read_data )
throws IOException {
	return readTimeSeriesList(is_nwsCardTrace, req_ts, in, req_date1,
		req_date2, req_units, read_data, null);
}

/**
Read one or more time series from a file.
It will read all the traces from a NWS Card Trace file or a single time series
from a NWS Card file. 
This private method overload is called from the public overload versions
returning a single time series and a vector of time series, where the 
BufferedReader object is instantiated and additional post processing are still performed.
@return a vector containing reference to one or more time series (or null in 
case of problems) when processing a NWS Card or a NWS Card Trace file.
@param is_nwsCardTrace flag indicating that the file is to be read as a NWS Card
single time series file (false) or a NWS Card Trace file (true).
Notice that even if the file is a NWS Card Trace file, this parameter can be
passed as true, to force this method to read the file as a NWS Card single
time series file (see the last version of this method returning TS above).      
@param req_ts Pointer to the time series to fill in with data from the file. If 
null, return a new time series.  All data are reset, except for the identifier, 
which is assumed to have been set in the calling code.  This parameter is used 
only when processing NWS Card single time series file.  It will be ignored 
when processing NWS Trace Card.  In this case only the returning vector will 
contain one or more new time series, one for each trace available in the file.
@param in Reference to open input stream.
@param req_date1 Requested starting date to initialize the period (or null to
read the entire time series).  If specified, the precision must be to hour.
@param req_date2 Requested ending date to initialize the period (or null to
read the entire time series).  If specified, the precision must be to hour.
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read (false=no, true=yes).
@param props properties to control how the file is read.  Recognized 
properties are:<p>
<table>
<tr>
<td><b>Property Name</b></td>
<td><b>Description</b></td>
<td><b>Default Value</b></td>
</tr>
<tr>
<td>Read24HourAsDay</td>
<td>Specifies whether 24 hour time series should be read as daily time series
instead (True or False).</td>
<td>False</td>
</tr>
</table><p>
This parameter can be null, and if so, defaults to false.
@exception IOException If an error occurs reading the file.
*/
private static Vector readTimeSeriesList(boolean is_nwsCardTrace,
TS req_ts, BufferedReader in, DateTime req_date1, DateTime req_date2, 
String req_units, boolean read_data, PropList props)
throws IOException {
	String routine = "NWSCardTS.readTimeSeriesList";
	String msg = null;
	String str = null;

	// Variable used only when processing NWS Card Traces.
	DateTime runPeriodStartDate = null;	// Used to store the start and 
	DateTime runPeriodEndDate   = null;	// end dates at hour 00.
						// These dates are read from 
						// the HISTORICAL RUN PERIOD
						// line while processing the 
						// general header of NWS Card
						// Traces files.
	DateTime runPeriodStart = null;		// The start and the end of the
	DateTime runPeriodEnd   = null;		// run period (date and time)
						// These dates are computed
						// after the Hour multiplier is
						// read from the traces first
						// header, by adding the Hour 
						// Multiplier to 
						// runPeriodStartDate and 
						// runPeriodEndDate.
	boolean  numberOfTraces_found = false;	// Flag indicating if the NUMBER
						// OF TRACES entry was found in
						// the NWS Card Trace file.
	boolean  runPeriod_found      = false;	// Flag indicating if the RUN
						// PERIOD entry was found in the
						// NWS Card Trace file.

	Vector TSList = null;

	String	string = null;
	int	dl = 10, HourMultiplier = 0, ndpl=0;
	
	boolean Read24HoursAsDay_boolean = false;

	if (props != null) {
		String propVal = props.getValue("Read24HourAsDay");
		if (propVal != null && propVal.trim().equalsIgnoreCase("true")){
			Read24HoursAsDay_boolean = true;
		}
	}
		
/*
	DateTime date1_file = new DateTime ( DateTime.PRECISION_HOUR );
					// The start date/time of the data
					// listed in the file.
	DateTime date2_file = new DateTime ( DateTime.PRECISION_HOUR );
					// The start date/time of the data
					// listed in the file.
*/

	DateTime date1_file = null;
	DateTime date2_file = null;

	// The precision was set to day if reading 24 hours as day here because
	// otherwise, the first piece of data was being lost when read from the
	// file.
	if (Read24HoursAsDay_boolean) {
		date1_file = new DateTime(DateTime.PRECISION_DAY);
			// The start date/time of the data listed in the file.
		date2_file = new DateTime(DateTime.PRECISION_DAY);
			// The start date/time of the data listed in the file.
	}
	else {
		date1_file = new DateTime(DateTime.PRECISION_HOUR);
			// The start date/time of the data listed in the file.
		date2_file = new DateTime(DateTime.PRECISION_HOUR);
			// The start date/time of the data listed in the file.
	}

	// Always read the header.  Optional is whether the data are read...
	int line_count = 0;
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Processing header..." );
	}

	String datatype     = "";
	String description  = "";
	String location     = "";
	String units        = "";
	String fixed_format = "";	// Format to read data, using strings.
	Vector tokens       = null;
	boolean	header1_found = false;
	boolean header2_found = false;

	// This member defaults to 1 for the _NWS Card single time series format
	// file and will be updated to the number of traces if dealing with NWS
	// Card Trace format.
	int numberOfTimeSeries = 1;

	// If dealing with traces, retrieve the run period dates and the number
	// of traces from the $ commented part of the general header.
	if ( is_nwsCardTrace ) {

	    try {

	    	// Make sure to break out of this while loop as soon as the
	        // required information is retrieved.
	        while ( true ) {

	            string = in.readLine();
	            if ( string == null ) {
	                throw new IOException(
	                    "EOF while parsing the general header at line \"" + string + "\" of a NWS Trace Card file.");
	            }
	            ++line_count;

	            // Ignore blank lines.
	            if ( string.trim().equals("") ) {
	                continue;
	            }

	            if ( string.length() > 0) {
	                // Ignore lines commented with #
	                if ( string.charAt(0) == '#' ) {
	                    continue;
	                }

	                // Process lines commented with $
	                if ( string.charAt(0) == '$' ) {

	                    // HISTORICAL RUN PERIOD line Format:
       					//(25) 1-25: "$  HISTORICAL RUN PERIOD="
       					//(13)26-38: "mm/dd/yyyy hh"
       					//(03)39-41: " - "
       					//(13)42-54: "mm/dd/yyyy hh"
	                    int index = StringUtil.indexOfIgnoreCase( string,"HISTORICAL RUN PERIOD=", 0 );

                       	if ( index != -1 && !runPeriod_found ) {

                       		// RunPeriodStartDate (only the date)
                        	index = index+22; // 25-3($  )
                        	str = string.substring(index,index+10);//13-3( hh)
                        	try {
                        		runPeriodStartDate = DateTime.parse ( str + " 00");
                        		// DateTime.FORMAT_YYYY_MM_DD_HH);
                        	}
                        	catch ( Exception e ) {
                        		msg= "Error parsing \""	+ str + "\" as the start of the Run Period.";
                        		Message.printWarning(2, routine,msg);
                        		throw new IOException(msg );
                        	}

                        	// RunPeriodEndDate (only the date)
                        	index = index + 13 + 3;
                        	str = string.substring (index,index+10);//13-3( hh)
                        	try {
                        		runPeriodEndDate = DateTime.parse (	str + " 00");
                        		// DateTime.FORMAT_YYYY_MM_DD_HH);
                        	}
                        	catch ( Exception e ) {
                        		msg= "Error parsing \""	+ str + "\" as the end of the Run Period.";
                        		Message.printWarning (2, routine,msg);
                        		throw new IOException ( msg );
                        	}
                        	runPeriod_found = true;
                        			
                        	if ( Message.isDebugOn ) {
                        	    msg= "Run Period start at "	+ runPeriodStartDate;
                        	    Message.printDebug ( 2, routine, msg );
                        	    msg = "Run Period end at " + runPeriodEndDate;
                        	    Message.printDebug ( 2, routine, msg );	
                        	}
						
                        	if ( numberOfTraces_found ) {
                        		// All needed information was found, break out.
                        		break;
                        	}
                       	}

                       	// NUMBER OF TRACES line Format:
       					//(20) 1-20: "$  NUMBER OF TRACES="
       					//( 3)21-23: "###"
                       	index = StringUtil.indexOfIgnoreCase( string, "NUMBER OF TRACES=" , 0 );

                       	if(index!=-1 && !numberOfTraces_found) {

                       		// Number of traces.
                       		index = index + 17; // 20-3($  )
                       		int size = string.length();
                       		str = string.substring( index, size ).trim();
                      		if (StringUtil.isInteger(str)){	
                        		numberOfTimeSeries = StringUtil.atoi( str );
                        	}
                      		else {
                        		msg= "Error parsing \""	+ str + "\" as the number of traces.";
                        		Message.printWarning (2, routine,msg);
                        		throw new IOException (	msg );
                        	}
                        	numberOfTraces_found = true;
                        			
                        	if ( Message.isDebugOn ) {
                        	    msg = "Number of traces " + numberOfTimeSeries;
                        	    Message.printDebug ( 2, routine, msg );
                        	}

                        	if ( runPeriod_found ) {
                        		// All needed information was found, break out.
                        		break;
                       		}
                       	}
	                }
	            }
	        }
	    }
	    catch ( Exception e ) {
		msg = "Error while processing line " + line_count + ": \"" + string + "\"";	
		Message.printWarning ( 2, routine, msg );
		Message.printWarning ( 3, routine, e );
		throw new IOException ( msg );
	    }
	}

	DateTime idate = null;
	DateTime date1 = null;
	DateTime date2 = null;
	TSIdent ident  = null;

	// These variables are used only with the NWS Card Traces
	DateTime idate_ts = null;		// Date used to iterater the run
						// period from date1_ts to 
						// date2_ts ( see below )=
	DateTime date1_ts = null;		// Run period date1 used for
						// iteration 
	DateTime date2_ts = null;		// Run period date2 used for
						// iteration 
	boolean doneWithThisTrace = false;	// Flag used to control
						// to processing flow after
						// each trace in the file.

	int warning_count = 0;
	String warning_message = "";

	// Instantiate the vector that will contain the time series.
	TSList = new Vector ( numberOfTimeSeries );
	if ( Message.isDebugOn ) {
		if ( is_nwsCardTrace ) {
			msg = "Processing NWS Card Traces file.";
		}
		else {
			msg = "Processing NWS Card single time series file.";
		}	
		Message.printDebug(dl, routine, msg);
		Message.printDebug(dl, routine, "Number of time series in the file is "	+ numberOfTimeSeries);
	}

	// Process all the time series in the file.  
	// One time series will be processed if dealing with NWS Card single time series file format.
	// One or more time series will be processed if dealing with NWS Trace Card format.

	for ( int nTS=0; nTS < numberOfTimeSeries; nTS++ ) {

		try {

		    // Read the header1 and header2 of the time series:
		    // DATACARD Header Format: Line 1
	       	    //       1-12: "DATACARD    "
	       	    //      15-18: data type
	       	    //      20-23: data dimension
	       	    //      25-28: data unit
	       	    //      30-31: ts time interval
	       	    //      35-46: ts id
	       	    //      50-69: TS description
	       	    // DATACARD Header Format: Line 2
		    //       1- 2: start month
		    //       5- 8: start year
		    //      10-11: end month
		    //      15-18: end year
		    //      20-21: number of data per line
		    //      25-32: format for each data value
		    while ( true ) {

			string = in.readLine();
			if ( string == null ) {
				throw new IOException(
					"EOF while processing general header "
					+ " in line " + line_count + ": \""
					+ string + "\"");
			}
			++line_count;

			// Don't trim the actual line because the data is fixed
			// format!
			if ( Message.isDebugOn ) {
				Message.printDebug(dl, routine,
					"Processing: \"" + string + "\"");
			}

			// Skipping blank and commented lines. No time series
			// information is currently retrieved from the commented
			// lines. 
			if ( ( string.trim().equals("")) ||
			     ((string.length() > 0) &&
			      ((string.charAt(0) == '#') ||
			       (string.charAt(0) == '$')) ) ) {
				continue;
			}

			tokens = StringUtil.breakStringList( string, " ",
				StringUtil.DELIM_SKIP_BLANKS );

			if ( !header1_found ) {
	       			
	       			// DATACARD Header Format: Line 1
	       			//       1-12: "DATACARD    "
	       			//      15-18: data type
	       			//      20-23: data dimension
	       			//      25-28: data unit
	       			//      30-31: ts time interval
	       			//      35-46: ts id
	       			//      50-69: TS description
	       			//
				// Identifier and description are sometimes
				// omitted so use fixed format read...
				
				// REVISIT [LT 2005-05-22] 
				// Make sure this is a DATACARD header line.
				
				header1_found = true;
				
				int len = string.length();
				// Check substrings depending on length
				// (remember substring returns start to end-1).
				if ( len >= 15 ) {
					datatype=string.substring(14,18).trim();
				}
				// Skip dimension...
				if ( len >= 25 ) {
					units = string.substring(24,28).trim();
				}
				if ( len >= 30 ) {
					HourMultiplier = StringUtil.atoi(
						string.substring(29,31).trim());
				}
				// These are sometimes optional...
				if ( len >= 35 ) {
					if ( len < 46 ) {
						location =
						string.substring(34,len).trim();
					}
					else {
						location =
						string.substring(34,46).trim();
					}
				}
				if ( len >= 50 ) {
					if ( len < 69 ) {
						description =
						string.substring(49,len).trim();
					}
					else {
						description =
						string.substring(49,69).trim();
					}
				}
				
				if ( Message.isDebugOn ) {
					Message.printDebug(2, routine, "\n" );
					Message.printDebug(2, routine, 
						"DATACARD header 2 content is: '");
					Message.printDebug(2, routine, 
						"datatype       = '" 
						+ datatype        + "'");
					Message.printDebug(2, routine, 
						"units          = '" 
						+ units          + "'");
					Message.printDebug(2, routine, 
						"HourMultiplier = '" 
						+ HourMultiplier + "'");
					Message.printDebug(2, routine, 
						"location       = '" 
						+ location       + "'");
					Message.printDebug(2, routine, 
						"description    = '" 
						+ description    + "'"); 
				}

			}
			else if ( header1_found && !header2_found ) {
				
				// DATACARD Header Format: Line 2
				//       1- 2: start month
		    		//       5- 8: start year
		    		//      10-11: end month
		    		//      15-18: end year
		    		//      20-21: number of data per line
		   		//      25-32: format for each data value
				//
				
				// Make sure this is the second header line. 
				if ( tokens.size() != 6 ) {
					throw new IOException(
						"Expecting second header line "
						+ " but number of tokens ("
						+ tokens.size()
						+ ") != 6");
				}
				
				header2_found = true;
				
				// The first value is always at the interval
				// past the start of the day.  If 24-hour data,
				// use the next day, hour zero to correct for
				// NWS using hour 24.
				/*
				if ( HourMultiplier == 24 ) {
					date1_file.setHour ( 0 );
					date1_file.setDay( 2 );
				}
				else {	date1_file.setHour ( HourMultiplier );
					date1_file.setDay( 1 );
				}
				*/
				
				if (Read24HoursAsDay_boolean) {
					date1_file.setHour(0);
					date1_file.setDay(1);
				}
				else {
					if (HourMultiplier == 24) {
						date1_file.setHour(0);
						date1_file.setDay(2);
					}
					else {	
						date1_file.setHour(
							HourMultiplier );
						date1_file.setDay(1);
					}
				}
				
				
				date1_file.setMonth( StringUtil.atoi(
					(String)tokens.elementAt(0) ) );
				date1_file.setYear(  StringUtil.atoi(
					(String)tokens.elementAt(1) ) );
/*
				date2_file.setHour ( 0 );
				date2_file.setDay ( TimeUtil.numDaysInMonth(
					StringUtil.atoi(
					(String)tokens.elementAt(2) ),
					StringUtil.atoi(
					(String)tokens.elementAt(3) ) ) );
				date2_file.setMonth( StringUtil.atoi(
					(String)tokens.elementAt(2) ) );
				date2_file.setYear( StringUtil.atoi(
					(String)tokens.elementAt(3) ) );
				date2_file.addDay ( 1 );
*/				

				if (Read24HoursAsDay_boolean) {
					// The end date is always the last day
					// of the last month, regardless of 
					// hour specified.
					date2_file.setHour(0);
					date2_file.setDay( 
						TimeUtil.numDaysInMonth(
						StringUtil.atoi(
						(String)tokens.elementAt(2)),
						StringUtil.atoi(
						(String)tokens.elementAt(3))));
					date2_file.setMonth(StringUtil.atoi(
						(String)tokens.elementAt(2)));
					date2_file.setYear(StringUtil.atoi(
						(String)tokens.elementAt(3)));
				}
				else {
					// The end date is always hour 24 of the
					// last month, which ends up being hour 
					// 0 of the first day in the next month.
					// Accomplish by setting to hour 0 of 
					// the last day in the file's ending 
					// month and then add a day.
					date2_file.setHour(0);
					date2_file.setDay( 
						TimeUtil.numDaysInMonth(
						StringUtil.atoi(
						(String)tokens.elementAt(2)),
						StringUtil.atoi(
						(String)tokens.elementAt(3))));
					date2_file.setMonth(StringUtil.atoi(
						(String)tokens.elementAt(2)));
					date2_file.setYear(StringUtil.atoi(
						(String)tokens.elementAt(3)));
					date2_file.addDay(1);
				}

				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
						"Period from file is "
						+ date1_file.toString()
						+ " to " +
						date2_file.toString() );
				}
				
				// Number of data values per line...
				ndpl = StringUtil.atoi(
					(String)tokens.elementAt(4) );
				
				// Format for the data line...
				String fformat = (String)tokens.elementAt(5);
				// Now put together a format string for
				// StringUtil.fixedRead()...  The value from the
				// datevalue file will be something like F9.3 so
				// need to throw away what is after the period...
				if ( fformat.indexOf(".") >= 0 ) {
					// Remove the trailing ".N"...
					fformat =
						fformat.substring(
							0,fformat.indexOf("."));
					// Remove the leading "F"...
					fformat = fformat.substring(1);
				}
				for ( int iformat=0; iformat<ndpl; iformat++ ) {
					fixed_format += "s" + fformat;
				}

				// At this point fixed_format should not be
				// empty. 
				if ( fixed_format.length() == 0 ) {
					// Did not figure out how to put 
					// together the format...
					msg = "Unable to determine data format"
						+ " for file.  Line number: "
						+ line_count;
					Message.printWarning ( 2, routine, msg);
					throw new IOException ( msg );
				}
		
				if ( Message.isDebugOn ) {
					Message.printDebug(dl, routine, "\n");
					Message.printDebug(dl, routine, 
						"DATACARD header 2 content is: '");
					Message.printDebug(dl, routine, 
						"date1_file       = '" 
						+ date1_file.toString() + "'");
					Message.printDebug(dl, routine, 
						"date2_file       = '" 
						+ date2_file.toString() + "'");
					Message.printDebug(dl, routine, 
						"fixed_format     = '" 
						+ fixed_format         + "'"); 
				}	

				break;	// last line of header
			}

		    }  // End of the Internal while  (header1 and header2)

		}
		catch ( Exception e ) {
			msg = "Error processing line "
				+ line_count + ": \"" + string + "\"";
			Message.printWarning ( 2, routine, msg );
			Message.printWarning ( 3, routine, e );
			throw new IOException ( msg );
		}

		// Declare the time series of the proper type based on the
		// interval.  Use a TSIdent to parse out the interval
		// information...
/*		
		try {	ident = new TSIdent ( location, "", datatype,
			"" + HourMultiplier + "Hour", "");
		}
		catch ( Exception e ) {
			// Should not happen...
			msg = "Unable to create new TSIdent.";
			Message.printWarning ( 2, routine, msg );
			throw new IOException ( msg );
		}
*/
/*
		int data_interval_base = TimeInterval.HOUR;	//always hour
		int data_interval_mult = HourMultiplier;
*/

		ident = null;
		int data_interval_base = -1;
		int data_interval_mult = -1;
		if (Read24HoursAsDay_boolean) {
			try {	
				ident = new TSIdent(location, "", datatype,
					"" + "Day", "");
			}
			catch (Exception e) {
				// Should not happen...
				msg = "Unable to create new TSIdent.";
				Message.printWarning(2, routine, msg);
				throw new IOException(msg);
			}

			data_interval_base = TimeInterval.DAY;
			data_interval_mult = 1;
		}
		else {
			try {	
				ident = new TSIdent(location, "", datatype,
					"" + HourMultiplier + "Hour", "");
			}
			catch (Exception e) {
				// Should not happen...
				msg = "Unable to create new TSIdent.";
				Message.printWarning(2, routine, msg);
				throw new IOException(msg);
			}
			
			data_interval_base = TimeInterval.HOUR;	//always hour
			data_interval_mult = HourMultiplier;
		}

		TS ts = null;
		// Set the time series pointer to either the requested time
		// series or a newly-created time series. Currently this 
		// parameter is only used, if available, when processing NWC
		// Card single time series file.
		if ( req_ts != null && !is_nwsCardTrace ) {
			ts = req_ts;
			// Identifier is assumed to have been set previously.
		}
		else {
			try {
				ts = TSUtil.newTimeSeries (
					ident.toString(), true );
			}
			catch ( Exception e ) {
				ts = null;
			}
		}
		if ( ts == null ) {
			msg = "Unable to create new time series for \""
				+ ident.toString() + "\"";
			Message.printWarning ( 2, routine, msg );
			throw new IOException ( msg );
		}

		// Only set the identifier if a new time series.  Otherwise
		// assume the existing identifier is to be used (e.g., from
		// a file name).
		if ( req_ts == null ) {
			try {	ts.setIdentifier ( ident.toString() );
			}
			catch ( Exception e ) {
				msg = "Unable to set identifier to: \""
					+ ident + "\"";
				Message.printWarning ( 2, routine, msg );
				throw new IOException ( msg );
			}
		}

		// Set time series properties.
		ts.setDataType    ( datatype );
		ts.setDataUnits   ( units );
		ts.setDescription ( description );
		ts.setDataUnitsOriginal ( units );

		// Set dates
		/*
		date1 = new DateTime(DateTime.PRECISION_HOUR);
		date2 = new DateTime(DateTime.PRECISION_HOUR);		
		*/

		if (Read24HoursAsDay_boolean) {
			date1 = new DateTime(DateTime.PRECISION_DAY);
			date2 = new DateTime(DateTime.PRECISION_DAY);
		}
		else {
			date1 = new DateTime(DateTime.PRECISION_HOUR);
			date2 = new DateTime(DateTime.PRECISION_HOUR);
		}

		// REVISIT [LT 2005-05-17] SAM's commensts: "Seems circular.....
		if ( is_nwsCardTrace ) {
	
			// At this point the RunPeriodStartDate and
			// RunPeriodEndDate contains only the dates
			// (no time yet).  The time should should no be
			// determined from the general header, because it may 
			// not always be accurate. The time is simple defined
			// by adding the hourMultiplier, since the first date
			// time should always be one interval after the
			// begining of the day.
			// Set the runPeriodStart and the RunPeriodEnd to 
			// the correct start time, which should be 1 time step
			// (addHour(HourMultiplier)) ahead of the start of the
			// day.
			runPeriodStart = new DateTime ( runPeriodStartDate );
			runPeriodEnd   = new DateTime ( runPeriodEndDate   );
/*
			runPeriodStart.addHour ( HourMultiplier ); 
			runPeriodEnd.addHour   ( HourMultiplier ); 
*/
			if (Read24HoursAsDay_boolean) {
				runPeriodStart.setPrecision(
					DateTime.PRECISION_DAY);
				runPeriodEnd.setPrecision(
					DateTime.PRECISION_DAY);
			}
			else {
				runPeriodStart.addHour ( HourMultiplier ); 
				runPeriodEnd.addHour   ( HourMultiplier ); 
			}
			 
			// Set Original dates using the Run Period Start and
			// End dates ...
			ts.setDate1Original ( new DateTime( runPeriodStart ) );
			ts.setDate1Original ( new DateTime( runPeriodEnd   ) );

			// Now set dates to read data to.  These are the dates
			// we will assing data to from the traces. The default
			// is to use the Run Period dates; however, these can be
			// overruled by the requested dates...
			// date1_ts, date2_ts and idate_ts (see below) are used
			// to iterate.
			// date1_ts and date2_ts are also used to allocate data
			// space for the returning time series.
			if ( req_date1 != null ) {
				date1_ts = req_date1;
			}
			else {
				date1_ts = runPeriodStart;
			}
			if ( req_date2 != null ) {
				date2_ts = req_date2;
			}
			else { date2_ts = runPeriodEnd;
			}

			// Set the time series date1 and date2.
			ts.setDate1 ( new DateTime( date1_ts ) );
			ts.setDate2 ( new DateTime( date2_ts ) );
			
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Time series trace period is " +
					date1_ts + " to " + date2_ts );
			}

			// Now set dates used to read from the file traces.
			// date1, date2 and idate (see below) are only used to
			// iterate over the time series traces.  
			date1 = date1_file;
			date2 = date2_file;

			// Set the sequence number.
			ts.setSequenceNumber ( date1_file.getYear() );
		}
		else {
			// Original dates are what is in the file...
			ts.setDate1Original ( date1_file );
			ts.setDate2Original ( date2_file );

			// Now set dates to read, based on the method
			// parameters.  The default is to use the dates in the
			// file; however, these can be overruled by the
			// requested dates...
			if ( req_date1 != null ) { date1 = req_date1;  }
			else                     { date1 = date1_file; }
			if ( req_date2 != null ) { date2 = req_date2;  }
			else                     { date2 = date2_file; }

			// Set the time series date1 and date2.
			ts.setDate1 ( new DateTime( date1 ) );
			ts.setDate2 ( new DateTime( date2 ) );
		}

		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Period to read is " +
				date1.toString() + " to " + date2.toString() );
			Message.printDebug ( dl, routine, "Read TS header" );
		}

		// REVISIT [LT 2005-05-11]
		// SAM's comments: "How about handle offset - start of day?"
		// REVISIT [LT 2005-05-17]
		// SAM's comments: "Did you resolve?".
		// REVISIT [LT 2005-05-17]
		// LT's comments: "No. I do not know what this is."

		// Data may not be needed if reading header information only.
		
		if (!read_data) {
			if (ts != null && req_units != null 
			    && !req_units.equalsIgnoreCase(ts.getDataUnits())) {
			    	// Convert units
				try {
					TSUtil.convertUnits(ts,
						req_units);
				}
				catch (Exception e) {
					msg = "Could not convert time "
						+ "series units to \""
						+ req_units + "\".";
					warning_message += "\n" + msg;
					Message.printWarning(2, 
						routine, msg);
				}
			}			

		    	TSList.addElement(ts);

		    	if ( is_nwsCardTrace ) {
		    		// Make sure to skip all the data lines when
		    		// processing NWS Card Trace files
		    		
				// Reset the header flags back to false to 
				// force the code to read the headers for the
				// next time series trace.
				header1_found = false;
				header2_found = false;
				
				// The lines after the data should be comments.
				// So loop until the next $ comment line and 
				// break out.
				while ( true ) {
					try {
						string = in.readLine();
						++line_count;
					}
					catch ( Exception e ) {
						msg = "Error processing line "
							+ line_count
							+ ": \"" + string
							+ "\"";
						Message.printWarning (
							2, routine, msg );
						throw new IOException ( msg );
					}
					
					if ( (string == null) ||
					     (string.charAt(0)=='$') ) {
					     	// Done with the last trace or
					     	// ready for the next trace.
						break;
					}		
				}
				continue;
			}
			else  {
				// For NWS Card single time series file, simple
				// return the vector.
				if (warning_count > 0) {
					throw new IOException(warning_message);
				}
				return TSList;
			}
		}

		// Allocate the memory for the data array...

		if ( ts.allocateDataSpace() == 1 ) {
			msg = "Error allocating data space...";
			Message.printWarning( 2, routine, msg );
			ts = null;
			throw new IOException ( msg );
		}

		// Read the data.
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Reading data..." );
		}

		// Set iterator 'pointer'
		if ( is_nwsCardTrace ) {
			// The runPeriodStart may start at any time in the 
			// month, but the traces should always start at the 
			// first interval past the start of the day. -999 are 
			// used as values between the first interval in the 
			// trace data and the runPeriodStart dateTime.
			idate_ts = new DateTime ( runPeriodStart );
		/*
			if ( runPeriodStart.getHour() == 0 ) {
				idate_ts.setDay(2);
			} else {
				idate_ts.setDay(1);
			}
*/
			if (Read24HoursAsDay_boolean) {
				idate_ts.setDay(1);
			}
			else {
				// Initialize "idate_ts" to the first interval 
				// past the start of the first day.  If the 
				// start hour of the run period is 0, it is 
				// necessary to set the day to 2, because the 
				// day 2 at 00:00 will be the first interval
				// past the first day in this case.
				if (runPeriodStart.getHour() == 0) {
					idate_ts.setDay(2);
				} 
				else {
					idate_ts.setDay(1);
				}
			}

			// Initialize the idate to the file date1 (trace).
			idate = new DateTime ( date1_file );
		}
		else {
			// Initialize the idate to the file date1.
			idate = new DateTime ( date1_file );
		}

		int size, i;
		Object otoken;	// Individual data token as Object
		String token;	// Individual data token as String
		int blanks = 0;	// Number of blank data values on a line

		try {
			// If dealing with traces, the next line after the end
			// of one time series trace should be some comment lines
			// followed by the the new header for the next trace.
			doneWithThisTrace = false;
			while ( !doneWithThisTrace ) {

				// Don't trim the line because the data are
				// fixed-format.
				string = in.readLine();
				if (string == null) {
					if (idate.lessThan(date2)) {
						// end of data.  Do not throw
						// an error.  Instead, short-
						// circuit to the code that
						// is called when the file 
						// is done being read.  

// Removed 5 tab stops for clarity.
if (is_nwsCardTrace) {
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "Finished reading data at: "
			+ idate.toString());
	}

	if (ts != null && req_units != null 
	    && !req_units.equalsIgnoreCase(ts.getDataUnits())) {
	    	// Convert units
		try {
			TSUtil.convertUnits(ts, req_units);
		}
		catch (Exception e) {
			msg = "Could not convert time series units to \""
				+ req_units + "\".";
			warning_message += "\n" + msg;
			warning_count++;
			Message.printWarning(2, routine, msg);
		}
	}			

	TSList.addElement(ts);
	if (warning_count > 0) {
		throw new IOException(warning_message);
	}	
	return TSList;
}
else {
	// Done with data.
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "Finished reading data at: "
			+ idate.toString());
	}

	if (ts != null && req_units != null 
	    && !req_units .equalsIgnoreCase(ts.getDataUnits())) {
	    	// Convert units
		try {
			TSUtil.convertUnits(ts, req_units);
		}
		catch (Exception e) {
			msg = "Could not convert time series units to \""
				+ req_units + "\".";
			
			Message.printWarning(2, routine, msg);
			throw new Exception(msg);
		}
	}			

	TSList.addElement(ts);
	// Since we are processing NWS
	// Card single time series file
	// there is nothing else to do,
	// so just return the TSList
	// with the single time series. 
	return TSList;
}
					}
					else {
						msg = "EOF processing time "
						+ "series data at line " 
						+ line_count 
						+ ".  Possible corrupt data "
						+ "file.";
						Message.printWarning(2, routine,
							msg);
						throw new IOException(msg);
					}
				}
				++line_count;

				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine, 
						"Processing: \"" + string 
						+ "\"");
				}
				
				if (	(string.trim().equals("")) ||
					((string.length() > 0) &&
					((string.charAt(0) == '#') ||
					 (string.charAt(0) == '$') ) ) ) {
					continue;

				}
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"Processing data string: \"" +
					string.substring(20) + "\"" );
				}

				// Only deal with the data values.  It is
				// possible that data values can be smashed
				// together so parse using the fixed format
				// width.  The format uses strings so that
				// different numbers of values in a month do not
				// cause conversion errors.

				tokens = StringUtil.fixedRead (	string.substring(20), fixed_format );

				// Size can be less if at end of month.  Since
				// the values are read as strings, check for
				// blanks and reduce the length accordingly...

				size = tokens.size();

				blanks = 0;
				for ( i = 0; i < size; i++ ) {
					otoken = tokens.elementAt(i);
					if (	(otoken == null) ||
					(((String)otoken).trim().length()==0)) {
						++blanks;
					}
				}
				size -= blanks;

				if ( (size > ndpl) || (size < 1) ) {
					// Can't continue because date sequence
					// may be off...
					msg = "Error reading data at line " +
						line_count
						+ ".  File is corrupt.";
					Message.printWarning ( 2, routine, msg);
					throw new IOException ( msg );
				}

				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine, 
						"#data on line =" + size );
					Message.printDebug ( dl, routine, 
						"Idate = " + idate );
					Message.printDebug ( dl, routine, 
						"Idate_ts = " + idate_ts );
					Message.printDebug ( dl, routine, 
						"Date1 = " + date1 );
					Message.printDebug ( dl, routine, 
						"Date2 = " + date2 );
				}

				// Processing data.
				 
				if ( is_nwsCardTrace ) {
					
				    // When dealing with NWS Card Trace file it 
				    // is necessary to iterate over the trace
				    // data (as it is done when processing NWS
				    // Card single time series file) and also
				    // over the run period.  The determination
				    // of what data to process is defined by the
				    // iterations within the run period.	

				    // Processing data from the NWS Card Trace.
				    for ( i = 0; i < size; i++ ) {
					if( idate_ts.greaterThanOrEqualTo(
						date1_ts ) ) {
						// In the requested period so
						// set the data...
						token = ( (String)
						    tokens.elementAt(i)).trim();
						ts.setDataValue(idate_ts,
							StringUtil.atod(token));
						if ( Message.isDebugOn ) {
							Message.printDebug (
								dl, routine,
							"Value found at "
							+ idate_ts.toString()
							+ ": " + token );
						}

					}
					if ( idate.lessThan(date2) ) {
						// Advance the iterator pointers
						// of the trace and the run
						// period. 
						idate.addInterval (
							data_interval_base,
							data_interval_mult);
						idate_ts.addInterval (
							data_interval_base,
							data_interval_mult);
					}
					else {
						if ( Message.isDebugOn ) {
							Message.printDebug (
							dl, routine,
							"Finished reading data"
							+ " at: "
							+ idate.toString() );
						}

						if (ts != null && 
						    req_units != null 
						    && !req_units
						    .equalsIgnoreCase(
						    ts.getDataUnits())) {
						    	// Convert units
							try {
								TSUtil
								.convertUnits(
								ts, req_units);
							}
							catch (Exception e) {
								msg = 
								"Could not "
								+ "convert "
								+ "time series "
								+ "units to \""
								+ req_units 
								+ "\".";
								warning_message 
								+= "\n" + msg;
								warning_count++;
								Message
								.printWarning(
								2, routine, 
								msg);
							}
						}			

						TSList.addElement(ts);
						// Here we are done with this
						// time series. The next lines
						// should be comments or EOF 
						// since we are dealing with
						// with NWS Card Trace files.
						// Reset the header flags back
						// to false to force the code to
						// try reading the headers for
						// the new traces in the file,
						// if any.
						header1_found = false;
						header2_found = false;
						doneWithThisTrace = true;
						break;
					}
				    }

				} 
				else {
				    // Processing data from the NWS Card.
				    for ( i = 0; i < size; i++ ) {
					if( idate.greaterThanOrEqualTo(date1)) {
						// In the requested period so
						// set the data...
						token = ( (String)
						    tokens.elementAt(i)).trim();
						ts.setDataValue(idate,
							StringUtil.atod(token));
						if ( Message.isDebugOn ) {
							Message.printDebug (
								dl, routine,
							"Value found at "
							+ idate.toString()
							+ ": " + token );
						}
					}
					if ( idate.lessThan(date2) ) {
						// Add interval
						idate.addInterval (
							data_interval_base,
							data_interval_mult);
					}
					else {
						// Done with data.
						if ( Message.isDebugOn ) {
							Message.printDebug (
							dl, routine,
							"Finished reading data"
							+ " at: "
							+ idate.toString() );
						}

						if (ts != null && 
						    req_units != null 
						    && !req_units
						    .equalsIgnoreCase(
						    ts.getDataUnits())) {
						    	// Convert units
							try {
								TSUtil
								.convertUnits(
								ts, req_units);
							}
							catch (Exception e) {
								msg = 
								"Could not "
								+ "convert "
								+ "time series "
								+ "units to \""
								+ req_units 
								+ "\".";
								
								Message
								.printWarning(
								2, routine, 
								msg);
								throw new
								Exception(msg);
							}
						}			
						
						TSList.addElement(ts);
						// Since we are processing NWS
						// Card single time series file
						// there is nothing else to do,
						// so just return the TSList
						// with the single time series. 
						return TSList;
					}
				    }
				}
			}
		}
		catch ( Exception e ) {
			Message.printWarning ( 3, routine, e );
			msg = "Error processing line " + line_count
				+ ": \"" + string + "\"";
			Message.printWarning ( 2, routine, msg );
			throw new IOException ( msg );
		}
	}

	if (warning_count > 0) {
		throw new IOException(warning_message);
	}

	routine        = null;
	string      = null;
	date1_file  = null;
	date2_file  = null;
	datatype    = null;
	description = null;
	location    = null;
	units       = null;
	tokens      = null;
	ident       = null;
	date1       = null;
	date2       = null;
	idate       = null;

	// The time series should be added to the TSList during the processing
	// inside the while loops.
	return TSList;
}

/**
Write a time series to the open PrintWriter.
@param ts Time series to write.
@param fp PrintWrite to write to.
@exception IOException if there is an error writing the file.
*/
public static void writeTimeSeries ( TS ts, PrintWriter fp )
throws IOException
{	// Call the method that takes multiple time seres...
	writeTimeSeries ( ts, fp, (DateTime)null, (DateTime)null, "", true );
}

/**
Write a time series to a NWSCard format file.  The entire period is written.
@param ts Single time series to write.
@param fname Name of file to write.
@exception IOException if there is an error writing the file.
*/
public static void writeTimeSeries ( TS ts, String fname )
throws IOException
{
	PrintWriter	out = null;

	String full_fname = IOUtil.getPathUsingWorkingDir ( fname );
	//try {	out = new PrintWriter (new FileWriter(full_fname));
	try {	out = new PrintWriter ( new FileOutputStream ( full_fname ) );
	}
	catch ( Exception e ) {
		String message =
		"Error opening \"" + full_fname + "\" for writing.";
		Message.printWarning ( 2,
		"NWSCardTS.writePersistent(TS,String)", message );
		out = null;
		throw new IOException ( message );
	}
	writeTimeSeries ( ts, out );
	out.flush ();
	out.close ();
	out = null;
}

/**
Write a time series to a NWSCard format file.
@param ts Vector of pointers to time series to write.
@param fname Name of file to write.
@param req_date1 First date to write (if null write the entire time series).
@param req_date2 Last date to write (if null write the entire time series).
@param req_units Units to write.  If different than the current units the units
will be converted on output.
@param write_data Indicates whether data should be written (as opposed to only
writing the header) (<b>currently not used</b>).
@exception IOException if there is an error writing the file.
*/
public static void writeTimeSeries ( TS ts, String fname, DateTime req_date1, DateTime req_date2,
					String req_units, boolean write_data )
throws IOException
{
	PrintWriter	out = null;

	String full_fname = IOUtil.getPathUsingWorkingDir ( fname );
	//try {	out = new PrintWriter (new FileWriter(full_fname));
	try {
        out = new PrintWriter ( new FileOutputStream ( full_fname ) );
	}
	catch ( Exception e ) {
		String message = "Error opening \"" + full_fname + "\" for writing.";
		Message.printWarning ( 2, "NWSCardTS.writeTimeSeries", message);
		out = null;
		throw new IOException ( message );
	}
	writeTimeSeries ( ts, out, req_date1, req_date2, req_units, write_data);
	out.flush ();
	out.close ();
	out = null;
}

/**
Write a time series to a NWSCard format file.
@param ts Time series to write.
@param fp PrintWriter to write to.
@param req_date1 First date to write (if null write the entire time series).
@param req_date2 Last date to write (if null write the entire time series).
@param req_units Units to write.  If different than the current units the units
will be converted on output.
@param write_data Indicates whether data should be written.
@exception IOException if there is an error writing the file.
*/
public static void writeTimeSeries ( TS ts, PrintWriter fp,	DateTime req_date1, DateTime req_date2,
					String req_units, boolean write_data )
throws IOException
{	String	cfmt = "%10.3f", dimension, nfmt = "F10.3", message, routine="NWSHourTS.writePersistent";
    int ndpl = 6;           // Number of data per line.

	if ( ts == null ) {
		message = "Time series is null, cannot continue.";
		Message.printWarning( 2, routine, message );
		throw new IOException ( message );
	}

	if ( fp == null ) {
		message = "Output stream is null, cannot continue.";
		Message.printWarning( 2, routine, message );
		throw new IOException ( message );
	}
    if ( Message.isDebugOn ) {
        Message.printDebug ( 2, routine, "Requesting to write NWS card file for period " + req_date1 +
            " to " + req_date2 + " units=" + req_units );
    }

	// The default output format is OK for normal values up to -9999.999 or
	// 99999.999.  However, sometimes very large numbers are encountered.
	// In this case we want the output values to be separated by spaces so
	// they are readable and it is OK to change the precision on output.
	// Use the following table...
	//
	// Max/Min	Format
	// Value
	//
	// <= 99999.xxx	F10.3
	// and
	// >= -9999.xxx
	//
	// <= 999999.xx	F10.2
	// and
	// >= -99999.xx
	//
	// <= 9999999.x	F10.1
	// and
	// >= -999999.x
	//
	// <= 99999999.	F10.0
	// and
	// >= -9999999.
	//
	// Else if > 99999999 pick a format that will allow one space beyond what is needed

	TSLimits limits = ts.getDataLimits();
	double min = limits.getMinValue();
	double max = limits.getMaxValue();
	if ( (max <= 99999) && (min >= -9999) ) {
		nfmt = "F10.3";
		cfmt = "%10.3f";
	}
	else if ( (max <= 999999) && (min >= -99999) ) {
		nfmt = "F10.2";
		cfmt = "%10.2f";
	}
	else if ( (max <= 9999999) && (min >= -999999) ) {
		nfmt = "F10.1";
		cfmt = "%10.1f";
	}
	else if ( (max <= 99999999) && (min >= -9999999) ) {
		nfmt = "F10.0";
		cfmt = "%10.0f";
	}
    else if ( max > 99999999 || (min < -999999999) ) {
        // Make the format enough to display the value and have one space extra and allow for negative.
        int ndigits = (int)MathUtil.log10(Math.max(Math.abs(max),Math.abs(min))) + 3;
        nfmt = "F" + ndigits + ".0";
        cfmt = "%" + ndigits + ".0f";
    }
	// Else use the 10.3 default originally defined.

	// Get the interval information.  This is used primarily for iteration.
	// The input time series must be hourly or 1Day.

	int data_interval_base = ts.getDataIntervalBase();
	int data_interval_mult = ts.getDataIntervalMult();

	if ( !((data_interval_base == TimeInterval.HOUR) ||
		((data_interval_base == TimeInterval.DAY) && (data_interval_mult == 1))) ) {
		message = "Only hourly or 1Day time series can be saved as NWS DATACARD";
		Message.printWarning( 2, routine, message );
		throw new IOException ( message );
	}

	// Write header, the output format for DATACARD depends on the min/max values.

	TSIdent id = ts.getIdentifier();
	fp.println (
	"$  IDENTIFIER=" + StringUtil.formatString(id.getLocation(),"%-12.12s")+
	"   DESCRIPTION=" + ts.getDescription() );

	// Set dates to write...
	DateTime date1 = new DateTime ( DateTime.PRECISION_HOUR );
	DateTime date2 = new DateTime ( DateTime.PRECISION_HOUR );
    boolean req_date1_boolean = false;
    boolean req_date2_boolean = false;
	if ( req_date1 != null ) {
		date1 = new DateTime(req_date1);
        req_date1_boolean = true;
	}
	else {
        date1 = new DateTime(ts.getDate1());
	}
	if ( req_date2 != null ) {
		date2 = new DateTime(req_date2);
        req_date2_boolean = true;
	}
	else {
        date2 = new DateTime(ts.getDate2());
	}

	// Adjust the dates to make sure they line up with even months.  Use
	// if statements (and don't just enforce) in case we need to add
	// additional checks or messages.

	int ndays_in_month = 0;		// Number of days in month.
	if ( data_interval_base == TimeInterval.DAY ) {
		// Make sure the start day is 1 and the end day is the number of days in the month...
		if ( date1.getDay() != 1 ) {
			date1.setDay ( 1 );
		}
        ndays_in_month = TimeUtil.numDaysInMonth( date2.getMonth(), date2.getYear() );
		if ( date2.getDay() != ndays_in_month ) {
			// No need to go into first position in next month with daily data...
			date2.setDay ( ndays_in_month );
		}
	}
	else if ( data_interval_base == TimeInterval.HOUR ) {
		// If 24-hour, the first value will be at hour 0 of the 2nd
		// day and the last will be at hour 0 of the 1st day of the next
		// month.  If less than 24-hour, the first value will be at the
		// interval hour of the first day and the last will be at hour 0
		// of the first day of the next month.
		if ( data_interval_mult == 24 ) {
			date1.setDay(2);
			date1.setHour(0);
		}
		else {
            date1.setDay(1);
			date1.setHour(data_interval_mult);
		}
		// If the end date is not already hour 0 of the first day,
		// then increment the month to include the partial last month...
		if ( (date2.getDay() == 1) && (date2.getHour() == 0) ) {
			; // do nothing
		}
		else {
            date2.addMonth ( 1 );
			date2.setDay(1);
			date2.setHour(0);
		}
	}
	// If outputting one month, the above may adjust the end time back to the start of the month...
	if ( date2.lessThan ( date1 ) ) {
		date2.addMonth ( 1 );
	}
	Message.printStatus ( 2, routine, "Dates for NWS Card output file (complete month) are " + date1.toString() + " to " +	date2.toString() );

    // Construct as DATE_FAST.  Otherwise, setting the hour to 24 below will throw an exception.
    
	DateTime date1_file = new DateTime ( date1, DateTime.DATE_FAST );
	DateTime date2_file = new DateTime ( date2, DateTime.DATE_FAST );

	// If hourly data, the output period in the file header needs to be
	// adjusted because NWS CARD files use hours 1-24 whereas the in-memory
	// dates use 0-23.  To allow for the
	// file start and end dates to be specified, only adjust if the day is
	// 1 and the hour is 0, which will actually indicate hour 24 of the
	// previous day.  In this case, subtract one interval and set the hour
	// to 24.  The *_file dates are just used for header information.

	if ( data_interval_base == TimeInterval.HOUR ) {
		if ( (date1_file.getDay() == 1) && (date1_file.getHour() == 0) ) {
			date1_file.addInterval(	data_interval_base,-data_interval_mult);
			date1_file.setHour(24);
		}
		if ( (date2_file.getDay() == 1) && (date2_file.getHour() == 0) ) {
			date2_file.addInterval(	data_interval_base,-data_interval_mult);
			date2_file.setHour(24);
		}
	}

	// The above dates should not be printed anywhere else, although the
	// 24-hour issue is addressed below.

	fp.println (
	"$  PERIOD OF RECORD=" +
	date1_file.toString(DateTime.FORMAT_MM_SLASH_YYYY) + " THRU " +
	date2_file.toString(DateTime.FORMAT_MM_SLASH_YYYY));

	fp.println ( "$  SYMBOL FOR MISSING DATA=" +
		StringUtil.formatString(ts.getMissing(),"%.2f") +
		"   SYMBOL FOR ACCUMULATED DATA=-998.00" );

	DataUnits units;
	String data_units = ts.getDataUnits();
	try {
        units = DataUnits.lookupUnits ( data_units );
		dimension = units.getDimension().getAbbreviation();
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, "Unable to find dimension of \"" + data_units + "\"." );
		dimension = "";
	}

	int hours = 0;
	if ( ts.getDataIntervalBase() == TimeInterval.HOUR ) {
		hours = ts.getDataIntervalMult();
	}
	else {
        hours = 24;	// Checked for daily above.
	}
	fp.println (
	"$  TYPE=" +
	StringUtil.formatString (ts.getDataType(),"%-4.4s") +
	"   UNITS=" +
	StringUtil.formatString(data_units,"%-4.4s") +
	"   DIMENSIONS=" + StringUtil.formatString(dimension,"%-4.4s") +
	"   DATA TIME INTERVAL=" +
	StringUtil.formatString(hours,"%2d") + " HOURS" );

	fp.println ( "$  OUTPUT FORMAT=(3A4,2I2,I4," + ndpl + nfmt + ")" );

    // Print the 2 non-comment header cards...

	String interval_mult_string = "";
	if ( data_interval_base == TimeInterval.HOUR ) {
		interval_mult_string = StringUtil.formatString(	ts.getDataIntervalMult(),"%2d");
	}
	else if ( data_interval_base == TimeInterval.DAY ) {
		interval_mult_string = "24";
	}
	fp.println (
        "DATACARD      " +
	StringUtil.formatString(ts.getDataType(),"%-4.4s") + " " +
	StringUtil.formatString(dimension,"%-4.4s") + " " +
	StringUtil.formatString(data_units,"%-4.4s") + " " +
	interval_mult_string + "   " +
	StringUtil.formatString(id.getLocation(),"%-12.12s") + "   " +
	ts.getDescription() );

	fp.println (
        StringUtil.formatString(date1_file.getMonth(),"%2d") + "  " +
	StringUtil.formatString(date1_file.getYear(),"%4d") + " " +
	StringUtil.formatString(date2_file.getMonth(),"%2d") + "   " +
	StringUtil.formatString(date2_file.getYear(),"%4d") + " " +
	StringUtil.formatString(ndpl,"%2d") + "   " + nfmt );

	if ( !write_data ) {
		// Only header is requested...
		return;
	}

	// Get the conversion factors to use for output.  Don't call
	// TSUtil.convertUnits because we don't want to alter the time series itself...
	double mult = 1.0;
	double add = 0.0;
	boolean convert_units = false;
	if ( (req_units != null) && (req_units.length() != 0) && !req_units.equalsIgnoreCase(ts.getDataUnits()) ) {
		try {
            DataUnitsConversion conversion=DataUnits.getConversion ( ts.getDataUnits(), req_units );
			mult = conversion.getMultFactor();
			add = conversion.getAddFactor();
			convert_units = true;
			conversion = null;
		}
		catch ( Exception e ) {
			Message.printWarning( 2, routine,
			"Unable to convert units to \"" + req_units + "\" leaving units as \"" + ts.getDataUnits() + "\"" );
		}
	}

	DateTime date = new DateTime ( date1 );

	StringBuffer buffer = new StringBuffer();

    int card = 0;			// Counter for output
    int card_out = 0;		// Counter for output that is actually printed (maximum value of 9999).
	int month_card = 0;		// Month printed to card file.
	int month_prev = -1;		// Previous "date" month
	int month = 0;			// Month in DateTime.
	int month_data_count = 0;	// Data output in month
	int ndata_in_month = 0;		// Number of data in month.
	double value = 0.0;		// Data value.
	int year_card = 0;		// Year in card file.

	// The only trick below is that dates with day 1 and hour 0 need to be
	// saved with the previous month because NWS treats days as hour 1-24.
	// This impacts where the check for the new month occurs.

	// The starting date must always results in a CARD file date at the
	// first position of the month, even if missing data need to be inserted.

	// If hourly, need to initialize this since in general will only be
	// initialized after processing a data value...

	if ( data_interval_base == TimeInterval.HOUR ) {
        // Num of intervals=[NumberOfDays] * [NumberOfData/Day]
        ndays_in_month = TimeUtil.numDaysInMonth( date.getMonth(), date.getYear() );
		ndata_in_month = ndays_in_month*(24/data_interval_mult);
		month_prev = date.getMonth();
		month_data_count = 0;
	}

	String location = StringUtil.formatString(id.getLocation(),"%-12.12s");
    double missing = ts.getMissing();   // used when printing outside requested period
	for ( ; date.lessThanOrEqualTo(date2);
		date.addInterval(data_interval_base,data_interval_mult) ) {
		// For hour data:
		//
		// If the day is 1 and the hour is 0, we actually need to treat
		// like the previous day, hour 24 in order to get the output
		// correct.  Therefore, only treat like a new month if the
		// month is not the same and the hour != 0 (that way the zero
		// hour is included in the previous month).  Check for a new
		// month AFTER processing the data.
		//
		// For day data:
		//
		// Just ignore the whole hour issue and things are simpler.

		month = date.getMonth();

		if ( (data_interval_base == TimeInterval.DAY) && (month != month_prev) ) {
			// Month is different than previous date so get
			// data about the month (number of days, etc.)...

            // Num of intervals=[NumberOfDays] * [NumberOfData/Day]
            ndays_in_month = TimeUtil.numDaysInMonth(date.getMonth(), date.getYear() );
			ndata_in_month = ndays_in_month;
			month_prev = month;
			month_data_count = 0;
		}

		++month_data_count;	// Each value on a line is counted.

		// Append the data to the buffer...

        value = ts.getDataValue(date);
        if ( (req_date1_boolean && date.lessThan(req_date1)) || (req_date2_boolean && date.greaterThan(req_date2)) ) {
            // Date being processed is outside the requested period so use missing.
            value = missing;
        }
		if ( convert_units ) {
			if ( !ts.isDataMissing(value) ) {
				value = value*mult + add;
			}
			buffer.append( StringUtil.formatString(value,cfmt));
		}
		else {
            buffer.append( StringUtil.formatString(value,cfmt) );
		}

		// Determine whether the line should be printed.
		// The line is printed if the number of data values
		// is evenly divisible by the number of values for the line or
		// if all values for the month have been printed.
		//
		// If read to print, print the line information and then the
		// StringBuffer data contents from above.
		//

		if ( (month_data_count%ndpl == 0) ||(ndata_in_month == month_data_count) ) {
			++card;	// Count of output lines
			month_card = month;
			year_card = date.getYear();
			if ( (data_interval_base == TimeInterval.HOUR) && (date.getDay() == 1) && (date.getHour() == 0) ) {
				// Might have situation where 1 day is left over at the end of month.
                // Internally it is in the 0th hour of the first day of the next month
				// so need to set the month right.
				--month_card;
				if ( month_card == 0 ) {
					month_card = 12;
					--year_card;
				}
			}
			card_out = card;
			if ( card_out > 9999 ) {
				card_out = 9999;
			}
			fp.println ( location +
			StringUtil.formatString( month_card, "%2d") +
			StringUtil.formatString( year_card%100, "%02d") +
			StringUtil.formatString (card_out,"%4d") +
			buffer.toString() );
			// Clear the buffer...
			buffer.setLength ( 0 );
		}

		// If hourly data, check for new month AFTER processing the data
		// since internal data will be stored in the future (period
		// ending at end of day),
		if ( (data_interval_base == TimeInterval.HOUR) && (date.getDay() == 1) && (date.getHour() == 0) ) {
			// Last value of NWSCARD month is actually in next month
			// in internal data so we need to start a new month for
			// the next data value...

            // Num of intervals=[NumberOfDays] * [NumberOfData/Day]
            ndays_in_month = TimeUtil.numDaysInMonth( date.getMonth(), date.getYear() );
			ndata_in_month = ndays_in_month*(24/data_interval_mult);
			month_prev = month;
			month_data_count = 0;
		}
	}
	dimension = null;
	nfmt = null;
	routine = null;
	id = null;
	date1 = null;
	date2 = null;
	date1_file = null;
	date2_file = null;
	units = null;
	data_units = null;
	date = null;
	buffer = null;
	location = null;
}

/**
Returns an array of SimpleFileFilters suitable for use in JFileChoosers. 
@return an array of SimpleFileFilters suitable for use in JFileChoosers. 
The last filter in the array is a duplicate of one of the other filters in the
array, and -- if an file filter needs to be selected as the default filter 
in a JFileChooser -- is the default one.  
*/
public static FileFilter[] getFileFilters() {
	SimpleFileFilter[] filters = new SimpleFileFilter[4];

	filters[0] = new SimpleFileFilter("*", "NWS Card Time Series");
	filters[1] = new SimpleFileFilter("card", "NWS Card Time Series");
	filters[2] = new SimpleFileFilter("txt", "NWS Card Time Series");
	filters[3] = filters[1];

	return filters;
}

} // Endof NWSCardTS class
