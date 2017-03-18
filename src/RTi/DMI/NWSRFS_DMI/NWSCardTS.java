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
import java.util.ArrayList;
import java.util.List;

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
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.DateTimeRange;
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
public static List<String> getSample ()
{	List<String> s = new ArrayList<String>();
	s.add ( "#NWSCard" );
	s.add ( "#" );
	s.add ( "# This is an example of a typical National Weather Service (NWS) CARD format");
	s.add ( "# time series, which can be used for hourly data (1-24 hours).  This format");
	s.add ( "# is commonly used by the NWS.  The NWS Card file uses hours 1 to 24 whereas");
	s.add ( "# in-memory time series storage uses 0-23.  The translation of date/times");
	s.add ( "# from the CARD file to in-memory time series occurs as follows as the file" );
	s.add ( "# is read (using a single 31-day month).  The inverse occurs when writing.");
	s.add ( "#" );
	s.add ( "# Data     | CARD         | Time Series | CARD          | Time Series" );
	s.add ( "# Interval | Start        | Start       | End           | End" );
	s.add ( "# ---------|--------------|-------------|---------------|--------------------" );
	s.add ( "# 6-Hour   | Day 1, Hr 6  | Day 1, Hr 6 | Day 31, Hr 24 | Mon 2, Day 1, Hr 0");
	s.add ( "# 24-Hour  | Day 1, Hr 24 | Day 2, Hr 0 | Day 31, Hr 24 | Mon 2, Day 1, Hr 0");
	s.add ( "#" );
	s.add ( "# If, for example, a DateValue time series is read and then is written as a" );
	s.add ( "# CARD file, then use a 1Day interval DateValue file and don't specify hour" );
	s.add ( "# in the dates, OR, use an hourly file and specify hours in the date/times." );
	s.add ( "# Otherwise, the precision of the input data may not translate correctly." );
	s.add ( "#" );
	s.add ( "# An example file is as follows and conforms to the following guidelines:");
	s.add ( "# * Only one time series per file." );
	s.add ( "# * The sequence number in data lines (field 3) has a maximum value of 9999.");
	s.add ( "# * Full months are included, with missing values as needed." );
	s.add ( "# * See the header below for more information." );
	s.add ( "# * Data are fixed format." );
	s.add ( "# * Comments in the file start with $ (these #-comments are for illustration");
	s.add ( "#   only." );
	s.add ( "# * Data lines are printed using the specified format." );
	s.add ( "# * Data lines have station, month, year (2 digit), count, data values." );
	s.add ( "#" );
	s.add ( "$  IDENTIFIER=STATIONX       DESCRIPTION=RIVER Y BELOW Z     " );
	s.add ( "$  PERIOD OF RECORD=08/1978 THRU 11/1995" );
	s.add ( "$  SYMBOL FOR MISSING DATA=-999.00   SYMBOL FOR ACCUMULATED DATA=-998.00" );
	s.add ( "$  TYPE=SQIN   UNITS=CMS    DIMENSIONS=L3/T   DATA TIME INTERVAL= 6 HOURS" );
	s.add ( "$  OUTPUT FORMAT=(3A4,2I2,I4,6F10.2)             " );
	s.add ( "DATACARD      SQIN L3/T CMS   6    26433                                  " );
	s.add ( " 8  1984 10   1984  6   F10.2       " );
	s.add ( "STATIONX     884   1     91.66     88.95     86.24     83.53     81.14     78.74" );
	s.add ( "STATIONX     884   2     76.35     73.96     73.00     72.04     71.07     70.11" );
	s.add ( "..." );
	s.add ( "STATIONX     884  20    299.88    296.23    273.81    251.39    228.97    206.55" );
	s.add ( "STATIONX     884  21    192.56    178.56    164.57    150.57" );
	s.add ( "STATIONX     984   1    145.28    139.99    134.70    129.41    123.45    117.50" );
	s.add ( "STATIONX     984   2    111.54    105.58    102.26     98.94     95.63     92.31" );
	s.add ( "STATIONX     984   3    163.89    235.48    307.07    378.65   1032.13   1685.60" );
	s.add ( "..." );
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
			if ( string.charAt(0) == '$' && (string.regionMatches(true,0,"IDENTIFIER=",0,14)
			    ||string.regionMatches(true,0,"TYPE=", 0, 8)) ) {
				is_nwscard = true;
				break;
			}

			if ( (string.length()==0) || (string.charAt(0)!='$') ) {
				break;
			}
		}
		return is_nwscard;
	}
	catch ( Exception e ) {
		return false;
	}
	finally {
	    if ( in != null ) {
	        try {
	            in.close();
	        }
	        catch ( IOException e ) {
	            // Don't do anything.
	        }
	    }
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
	BufferedReader in = null;
	try {
		String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
		in = new BufferedReader (new InputStreamReader( IOUtil.getInputStream (full_fname)));
		is_NWSCardTraceFile = isNWSCardTraceFile ( in );
		in.close();
		in = null;
		return is_NWSCardTraceFile;
	}
	catch ( Exception e ) {
		return false;
	}
    finally {
        if ( in != null ) {
            try {
                in.close();
            }
            catch ( IOException e ) {
                // Don't do anything.
            }
        }
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
@param inBufferedReader - Reference to a Buffered Reader object containing the file.
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
@return HourTS for data in the file or null if there is an error reading the time series.
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
@return a pointer to a newly-allocated time series if successful, a null pointer if not.
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
public static TS readTimeSeries ( String fname, DateTime req_date1, DateTime req_date2,
    String req_units,  boolean read_data )
throws IOException			
{
	TS	ts = null;

	String full_fname = IOUtil.getPathUsingWorkingDir ( fname );
	BufferedReader in = null;
	try {
        in = new BufferedReader ( new InputStreamReader(	IOUtil.getInputStream ( full_fname )) );
		// Don't have a requested time series...
		ts = readTimeSeries ( (TS)null, in,	req_date1, req_date2, req_units, read_data );
			
		// Set some time series properties.	
		ts.setInputName ( fname );
		ts.getIdentifier().setInputType( "NWSCard" );
		ts.getIdentifier().setInputName( fname );
	}
	catch ( Exception e ) {
		Message.printWarning( 2, "NWSCardTS.readTimeSeries(String,...)", "Unable to open file \"" + fname + "\"" );
	}
    finally {
        if ( in != null ) {
            try {
                in.close();
            }
            catch ( IOException e ) {
                // Don't do anything.
            }
        }
    }
	return ts;
}

/**
Read a single time series from a file.
This version when working with NWS Card file (single TS per file) will use
the time series identifier passed in (first parameter) to set properties
for the returning time series. 
When working with NWS Card Trace file it will will return a single time 
series representing the trace that matches the time series identifier passed in (first trace).
@return a pointer to a newly-allocated time series if successful, null if not.
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
public static TS readTimeSeries ( String tsident_string, String fname, DateTime req_date1, DateTime req_date2,
    String req_units, boolean read_data )
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
			List<TS> TSList = null;
			
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
				    	ts = TSList.get(i);
				    	if ( tsident_string.equalsIgnoreCase ( ts.getIdentifierString() ) ) {
				    	    break;
				    	}
				    } 
				}
			}
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
and additional post processing are still performed to set the returning time series identifier.
@param req_ts Pointer to time series to fill.  If null return a new time series.
This parameter is used only when processing single time series from a NWS Card
file.  All data are reset, except for the identifier, which is assumed to have been set in the calling code.
This parameter is ignored when processing NWS Trace Card.  In this case the
returning vector will contain several new time series, one for each trace available in the file.
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
	//String routine = "NWSCardTS.readTimeSeries";

	TS ts = null;
	List<TS> tslist = null;

	// Read the time series.
	// This version should return only one time series. When processing a 
	// NWS Card Trace only the first time series is expected in the
	// returning vector.  To ensure this behavior, the first parameter 
	// "is_nwsCardTrace" to the processing method is always passed as
	// "false", even if the file is a NWS Card Trace file.  This ensure
	// that the file will always be processed as a NWS Card single time
	// series file and the returning time series is the one expected. 
	tslist = readTimeSeriesList ( false, req_ts, in, req_date1, req_date2, req_units, read_data );

	// One time series is expected. So make sure the returned vector is not
	// null and contains one element. Retrieve the element.
	if ( tslist != null ) {
		if ( tslist.size() != 0 ) {
			ts = tslist.get(0);
		}
	}

	// Return the reference to the time series or null if the time series was not properly read.
	return ts;
}

/**
Read one or more time series from a file.
It will read all the traces from a NWS Card Trace file or a single time series from a NWS Card file.
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
public static List<TS> readTimeSeriesList ( String fname, DateTime req_date1, DateTime req_date2,
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
public static List<TS> readTimeSeriesList ( TS req_ts, String fname, DateTime req_date1, DateTime req_date2,
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
public static List<TS> readTimeSeriesList(TS req_ts, String fname,
DateTime req_date1, DateTime req_date2, String req_units, boolean read_data, PropList props)
throws IOException {
	String routine = "NWSCardTS.readTimeSeriesList";
	List<TS> TSList = null;

	BufferedReader in = null;
	try {
		String full_fname = IOUtil.getPathUsingWorkingDir(fname);
		// Check if we are dealing with a NWS Trace Card file.
		boolean is_nwsCardTrace = isNWSCardTraceFile(full_fname);
		// Create a new buffer to start fresh from the beginning of the file.
		in = new BufferedReader(new InputStreamReader( IOUtil.getInputStream(full_fname)));

		// Read the time series list
		TSList = readTimeSeriesList ( is_nwsCardTrace, req_ts, in, req_date1, req_date2, req_units, read_data, props);
				      		
		// Update the time series InputType (NWSCard) and InputName (fname) properties.
		TS ts = null;
		if (TSList != null) {
			int tsCount = TSList.size();
			if (tsCount != 0) {
				for (int i = 0; i<tsCount; i++) {
					ts = TSList.get(i);
					
					// Set some time series properties.
					ts.setInputName(fname);
					ts.getIdentifier().setInputType( "NWSCard");
					ts.getIdentifier().setInputName(fname);
				} 
			}
		}	
	}
	catch (Exception e) {
        String message = "Error reading file \"" + fname + "\" (" + e + ")";
		Message.printWarning(3, routine, message );
		Message.printWarning(3, routine, e );
        throw new IOException ( message );
	}
	finally {
        if ( in != null ) {
            try {
                in.close();
            }
            catch ( IOException e ) {
                // Don't do anything.
            }
        }
	}

	return TSList;
}

/**
Read one or more time series from a file.
It will read all the traces from a NWS Card Trace file or a single time series from a NWS Card file. 
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
private static List<TS> readTimeSeriesList ( boolean is_nwsCardTrace, TS req_ts, BufferedReader in,
					  DateTime req_date1, DateTime req_date2, String req_units, boolean read_data )
throws IOException {
	return readTimeSeriesList(is_nwsCardTrace, req_ts, in, req_date1,
		req_date2, req_units, read_data, null);
}

// TODO SAM 2008-04-06 This method still has some redundant code that needs
// refactored, e.g., adding of time series to the list
/**
Read one or more time series from a file.
It will read all the traces from a NWS Card Trace file or a single time series from a NWS Card file. 
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
@param props properties to control how the file is read.  Recognized properties are:<p>
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
private static List<TS> readTimeSeriesList(boolean is_nwsCardTrace,
TS req_ts, BufferedReader in, DateTime req_date1, DateTime req_date2, 
String req_units, boolean read_data, PropList props)
throws IOException
{
	String routine = "NWSCardTS.readTimeSeriesList";
	String msg = null;
	String str = null;

	// Start of variables used only when processing NWS Card Traces...
	
	// Used to store the start and end dates from the HISTORICAL RUN PERIOD in the main header of the trace file.
	// The hour from the file is ignored and the hour is set to zero.
	DateTime runPeriodStartDate24 = null;
	DateTime runPeriodEndDate24 = null;

	// The start and the end of the ESP run period (date and time).  These date/times are computed
    // after the hour multiplier is read from the header of the first trace (not the main header),
	// by adding the hour multiplier to runPeriodStartDateHistorical and runPeriodEndDateHistorical.
	// These dates are used for all traces.  The iterator on data will use full months and therefore some
	// data in each trace may be ignored (often missing values) when the ESP run does not start at the
	// beginning of the month.
    DateTime runPeriodStartDate = null;
    DateTime runPeriodEndDate = null;	

	// Flag indicating whether the "NUMBER OF TRACES" entry was found in the NWS Card Trace file main header.
	boolean numberOfTraces_found = false;
	
	// Flag indicating if the HISTORICAL RUN PERIOD entry was found in the NWS Card Trace file main header.
	boolean historicalRunPeriod_found = false;
	
	// ...end of trace file only variables

	List<TS> TSList = null; // Time series list to return

	String string = null; // Line read from the file
	int	dl = 10; // Debug level
	int hourMultiplier = 0; // Hour multiplier (e.g., 24 for 24-hour data)
	int ndpl = 0; // Number of data values per line, in data section of file
	
	boolean Read24HoursAsDay_boolean = false;

	if (props != null) {
		String propVal = props.getValue("Read24HourAsDay");
		if (propVal != null && propVal.trim().equalsIgnoreCase("true")){
			Read24HoursAsDay_boolean = true;
		}
	}
	
	// Always read the header.  Optional is whether the data are read...
	int line_count = 0;
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Processing header..." );
	}

	String datatype = "";
	String description = "";
	String location = "";
	String units = "";
	String fixed_format = "";	// Format to read data, using strings.
	List<String> tokens = null;
	boolean	header1_found = false;
	boolean header2_found = false;

	// This member defaults to 1 for the _NWS Card single time series format
	// file and will be updated to the number of traces if dealing with NWS Card Trace format.
	int numberOfTimeSeries = 1;

	// If dealing with traces, retrieve the run period dates and the number
	// of traces from the $ commented part of the general header.
	if ( is_nwsCardTrace ) {

	    try {

	    	// Make sure to break out of this while loop as soon as the required information is retrieved.
	        while ( true ) {
	            string = in.readLine();
	            if ( string == null ) {
	                throw new IOException(
	                    "End of file while parsing the general header at line \"" + string +
	                    "\" of a NWS Trace Card file.");
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

                       	if ( index != -1 && !historicalRunPeriod_found ) {
                       	    // Start of the historical period.  Skip over HISTORICAL RUN PERIOD=
                        	index = index+22;
                        	// Get the date MM/DD/YYYY
                        	str = string.substring(index,index+10);
                        	try {
                        	    // Want to keep the original date without being impacted by an hour of 24
                        	    // so force zero as the hour.
                        		runPeriodStartDate24 = DateTime.parse ( str + " 00");
                        	}
                        	catch ( Exception e ) {
                        		msg= "Error parsing \""	+ str + "\" as the HISTORICAL RUN PERIOD start.";
                        		Message.printWarning(2, routine,msg);
                        		throw new IOException(msg );
                        	}
                        	if ( (runPeriodStartDate24.getMonth() == 2) &&
                        	        (runPeriodStartDate24.getDay() == 29) &&
                        	        TimeUtil.isLeapYear(runPeriodStartDate24.getYear()) ) {
                        	    // FIXME SAM 2008-04-03 is there a cleaner way to deal with it?
                        	    // Cannot handle because the historical traces would have to jump back and forth
                        	    // between Feb 29 and Mar 1 ??? and they typically always have consistent months.
                        	    msg = "Cannot handle NWS Card trace files that have ESP run date" +
                        	    		" starting on Feb 29 of a leap year.";
                        	    Message.printWarning(2, routine, msg);
                        	    throw new Exception ( msg );
                        	}
                        	// End date (skip over the start date, start hour, and 3 spaces)
                        	index = index + 13 + 3;
                        	// Get the date as MM/DD/YYYY
                        	str = string.substring (index,index+10);
                        	try {
                        	    // Want to keep the original date without being impacted by an hour of 24
                                // so force zero as the hour.
                        		runPeriodEndDate24 = DateTime.parse ( str + " 00");
                        	}
                        	catch ( Exception e ) {
                        		msg= "Error parsing \""	+ str + "\" as the HISTORICAL RUN PERIOD end.";
                        		Message.printWarning (2, routine,msg);
                        		throw new IOException ( msg );
                        	}
                        	historicalRunPeriod_found = true;
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
                        			
                       	    msg = "Number of traces " + numberOfTimeSeries;
                       	    Message.printStatus ( 2, routine, msg );

                        	if ( historicalRunPeriod_found ) {
                        		// All needed information was found, break out.
                        		break;
                       		}
                       	}
	                }
	            }
	        }
	    }
	    catch ( Exception e ) {
    		msg = "Unexpected error while processing line " + line_count + ": \"" + string + "\"";	
    		Message.printWarning ( 2, routine, msg );
    		Message.printWarning ( 3, routine, e );
    		throw new IOException ( msg );
	    }
	}
	
	// The start and end date/time of the data listed in the file, from the header before
    // the time series, using hour 1-24.  In particular this is useful for getting the original
    // month and year, in cases where traces are read and the run period may be on the last day
    // of the month (causing a shift to the next month when converting to hour 0-23 notation).
    // The following are reset for each time series (trace) in the card file.
    
    DateTime date1_fileHeader = new DateTime ( DateTime.PRECISION_MONTH );
    DateTime date2_fileHeader = new DateTime ( DateTime.PRECISION_MONTH );

    // The start and end date/time of the data listed in the file, from the header before
    // the time series, using hour 0-23.  The date/times are read as hour 24 and then adjusted
    // to hour 23.  For trace files, the following are the dates from the individual headers,
    // which are for full months.
    
    DateTime date1_file = null; 
    DateTime date2_file = null;

    // The precision is set to day if reading 24 hours.
    if (Read24HoursAsDay_boolean) {
        date1_file = new DateTime(DateTime.PRECISION_DAY);
        date2_file = new DateTime(DateTime.PRECISION_DAY);
    }
    else {
        date1_file = new DateTime(DateTime.PRECISION_HOUR);
        date2_file = new DateTime(DateTime.PRECISION_HOUR);
    }

	DateTime idate_file = null; // Date/time used to iterate through values in the file (always historical)
	DateTime idate_ts = null; // Date used to iterate through the time series data (historical or real-time for traces)
	DateTime date1_ts = null; // Run period date1 used for iteration with idate_ts
	DateTime date2_ts = null; // Run period date2 used for iteration with idate_ts
	boolean doneWithThisTrace = false; // Flag used to control to processing flow after each trace in the file.
	boolean premature_trace_end = false; // Used when a trace does not have enough data

	int warning_count = 0;
	String warning_message = "";

	// Instantiate the vector that will contain the time series.
	TSList = new ArrayList<TS> ( numberOfTimeSeries );
	if ( is_nwsCardTrace ) {
		msg = "Processing NWS Card Traces file.";
	}
	else {
		msg = "Processing NWS Card single time series file.";
	}	
	Message.printStatus(2, routine, msg);
	Message.printStatus(2, routine, "Number of time series in the file is "	+ numberOfTimeSeries);

	// Process all the time series in the file.  
	// One time series will be processed if dealing with NWS Card single time series file format.
	// One or more time series will be processed if dealing with NWS Trace Card format.

	int trace_start_year;  // Trace start year, used for messages
	for ( int its=0; its < numberOfTimeSeries; its++ ) {
	    if ( is_nwsCardTrace ) {
	        Message.printStatus(2, routine, "Reading trace [" + its + "]" );
	    }
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
		        if ( is_nwsCardTrace && premature_trace_end ) {
		            // The comment line that was read to indicate a premature trace end should be processed
		            // (do not read a new line).
		            premature_trace_end = false; // Reset for next trace (also avoid infinite loop)
		        }
		        else {
		            // Read a new line to process.
		            string = in.readLine();
		            ++line_count;
		        }
    			if ( string == null ) {
    				throw new IOException(
    					"EOF while processing general header in line " + line_count + ": \"" + string + "\"");
    			}
    
    			// Don't trim the actual line because the data is fixed format!
    			if ( Message.isDebugOn ) {
    				Message.printDebug(dl, routine, "Processing: \"" + string + "\"");
    			}
    
    			// Skipping blank and commented lines.  No time series
    			// information is currently retrieved from the commented lines (other than the ESP trace file header
    			// read above).
    			if ( ( string.trim().equals("")) || ((string.length() > 0) &&
    			      ((string.charAt(0) == '#') || (string.charAt(0) == '$')) ) ) {
    				continue;
    			}
    
    			tokens = StringUtil.breakStringList( string, " ", StringUtil.DELIM_SKIP_BLANKS );
    
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
    				
    				// TODO [LT 2005-05-22] Make sure this is a DATACARD header line.
    				
    				header1_found = true;
    				
    				int len = string.length();
    				// Check substrings depending on length (remember substring returns start to end-1).
    				if ( len >= 15 ) {
    					datatype=string.substring(14,18).trim();
    				}
    				// Skip dimension...
    				if ( len >= 25 ) {
    					units = string.substring(24,28).trim();
    				}
    				if ( len >= 30 ) {
    					hourMultiplier = StringUtil.atoi( string.substring(29,31).trim());
    				}
    				// The location and description are sometimes optional...
    				if ( len >= 35 ) {
    					if ( len < 46 ) {
    						location = string.substring(34,len).trim();
    					}
    					else {
    						location = string.substring(34,46).trim();
    					}
    				}
    				if ( len >= 50 ) {
    					if ( len < 69 ) {
    						description = string.substring(49,len).trim();
    					}
    					else {
    						description = string.substring(49,69).trim();
    					}
    				}
    				
    				if ( Message.isDebugOn ) {
    					Message.printDebug(2, routine, "\n" );
    					Message.printDebug(2, routine, "DATACARD header 1 content is: '");
    					Message.printDebug(2, routine, "datatype       = '" + datatype + "'");
    					Message.printDebug(2, routine, "units          = '" + units + "'");
    					Message.printDebug(2, routine, "interval       = '" + hourMultiplier + "'");
    					Message.printDebug(2, routine, "location       = '" + location + "'");
    					Message.printDebug(2, routine, "description    = '" + description + "'"); 
    				}
    			}
    			else if ( header1_found && !header2_found ) {
    				
    				// DATACARD Header Format: Line 2
    				//       1- 2: start month (1-12)
    		    	//       5- 8: start year (YYYY)
    		    	//      10-11: end month (1-12)
    		    	//      15-18: end year (YYYY)
    		    	//      20-21: number of data per line
    		   		//      25-32: FORTRAN format for each data value (e.g., F9.3)
    				
    				// Make sure this is the second header line.
                    int month1_file = 0;
                    int year1_file = 0;
                    int month2_file = 0;
                    int year2_file = 0;
                    String fformat = null;  // Format for the data line
    				if ( tokens.size() == 6 ) {
    				    month1_file = StringUtil.atoi((String)tokens.get(0));
                        year1_file = StringUtil.atoi((String)tokens.get(1));
                        month2_file = StringUtil.atoi((String)tokens.get(2));
                        year2_file = StringUtil.atoi((String)tokens.get(3));
                        // Number of data values per line...
                        ndpl = StringUtil.atoi( (String)tokens.get(4) );
                        // Format for the data line...
                        fformat = (String)tokens.get(5);
    				}
    				else {
    				    if ( !is_nwsCardTrace ) {
    				        // Definitely a problem...
    				        throw new IOException(
    						"Expecting second header line but number of tokens (" + tokens.size() + ") != 6");
    				    }
    				    else {
    				        // For traces ESPADP does not generate standard trace files so try to read the
    				        // nonstandard ones.  Prior to 2008-04-04 this correction was required outside of
    				        // this code assuming that ESPADP would at some point be corrected but it never
    				        // has been and appears likely not to be fixed.  Therefore, try to handle the
    				        // problem here.  The format is likely of the form (^ indicates start of line):
    				        // ^ 2  197912  1979   6F9.3
    				        // Try to parse read fixed format...
    				        Message.printStatus(2, routine,
    				                "Non-standard NWS Card header 2 record found.  Trying to handle..." );
    				        List<Object> v = StringUtil.fixedRead ( string, "i2x2i4i2x2i4i4s10" );
    				        month1_file = ((Integer)v.get(0)).intValue();
    				        year1_file = ((Integer)v.get(1)).intValue();
    				        month2_file = ((Integer)v.get(2)).intValue();
                            year2_file = ((Integer)v.get(3)).intValue();
                            ndpl = ((Integer)v.get(4)).intValue();
                            fformat = ((String)v.get(5)).trim();
                            Message.printStatus(2, routine, "Non-standard header values after parsing: month1=" +
                                    month1_file + " year1=" + year1_file + " month2=" + month2_file +
                                    " year2=" + year2_file + " ndpl=" + ndpl + " format=\"" + fformat + "\"" );
    				    }
    				}
    				
    				header2_found = true;
    				
    				// Card files always have full months of data
    				if ( (hourMultiplier == 24) && Read24HoursAsDay_boolean ) {
    				    // Daily data - hour is not used but set to zero for iteration purposes
    				    date1_file.setHour(0);
    					date1_file.setDay(1);
    				}
    				else {
    				    // Hourly data - set hour to data interval
    					if (hourMultiplier == 24) {
    						date1_file.setHour(0);
    						date1_file.setDay(2);
    					}
    					else {	
    						date1_file.setHour(	hourMultiplier );
    						date1_file.setDay(1);
    					}
    				}
    				date1_file.setMonth( month1_file );
    				trace_start_year = year1_file;
    				date1_file.setYear( year1_file );
    				
    				if (Read24HoursAsDay_boolean) {
    					// The end date is always the last day of the last month, regardless of hour specified.
    					date2_file.setHour(0);
    					date2_file.setDay( TimeUtil.numDaysInMonth( month2_file, year2_file ) );
    					date2_file.setMonth(month2_file);
    					date2_file.setYear(year2_file);
    				}
    				else {
    					// The end date is always hour 24 of the last month, which ends up being hour 
    					// 0 of the first day in the next month. Accomplish by setting to hour 0 of 
    					// the last day in the file's ending month and then add a day.
    					date2_file.setHour(0);
    					date2_file.setDay( TimeUtil.numDaysInMonth(month2_file,year2_file));
    					date2_file.setMonth(month2_file);
    					date2_file.setYear(year2_file);
    					date2_file.addDay(1);
    				}
    				
                    // Also set in the objects used for some checks below.  The following is the information
                    // in the file header.  The start will be the same as date1_file.
                    date1_fileHeader.setMonth ( month1_file );
                    date1_fileHeader.setYear ( year1_file );
                    date2_fileHeader.setMonth ( month2_file );
                    date2_fileHeader.setYear ( year2_file );

    				Message.printStatus ( 2, routine, "[" + trace_start_year + "] Period from file (months) is " +
                            date1_fileHeader + " to " + date2_fileHeader );
    				Message.printStatus ( 2, routine, "[" + trace_start_year + "] Period from file (hour 0-23) is " +
    				        date1_file + " to " + date2_file );
    				
    				if ( is_nwsCardTrace && (its == 0) ) {
    				    // Set the 0-23 hour notation date/time for the start and end of the trace.  Do this here
    				    // because the hour multiplier is only available when the trace headers are read.
    				    // Only need to do this once since the date is reused when setting up the time series.
    				    // Trace start...
                        runPeriodStartDate = new DateTime ( runPeriodStartDate24 );
                        if ( (hourMultiplier == 24) && Read24HoursAsDay_boolean ) {
                            // Daily data - the file date is OK so just set the precision
                            runPeriodStartDate.setPrecision ( DateTime.PRECISION_DAY );
                        }
                        else {
                            // Hourly data - set hour to one interval after the start of the day
                            // For 24-hour data, this results in hour 0 of the next day, which is correct
                            // for 0-23 hour notation.
                            runPeriodStartDate.addHour ( hourMultiplier );
                        }
                        // Trace end...
                        runPeriodEndDate = new DateTime ( runPeriodEndDate24 );
                        if ( (hourMultiplier == 24) && Read24HoursAsDay_boolean ) {
                            // Daily data - the file date is OK so just set the precision
                            runPeriodEndDate.setPrecision ( DateTime.PRECISION_DAY );
                        }
                        else {
                            // Hourly data - set hour to one interval after the start of the day
                            // For 24-hour data, this results in hour 0 of the next day, which is correct
                            // for 0-23 hour notation.
                            runPeriodEndDate.addHour ( hourMultiplier );
                        }                           
                        msg= "ESP run period (0-23 hour) start at " + runPeriodStartDate;
                        Message.printStatus ( 2, routine, msg );
                        msg = "ESP run period (0-23 hour) end at " + runPeriodEndDate;
                        Message.printStatus ( 2, routine, msg );
        			}
    				
    				// Now put together a format string for StringUtil.fixedRead()...  The value from the
    				// file will be something like F9.3 so need to throw away what is after the period...
    				String fformat2 = fformat;  // Make a copy so original can be used for logging below
    				if ( fformat2.indexOf(".") >= 0 ) {
    					// Remove the trailing ".N"...
    					fformat2 = fformat2.substring( 0,fformat2.indexOf("."));
    					// Remove the leading "F"...
    					fformat2 = fformat2.substring(1);
    				}
    				for ( int iformat=0; iformat<ndpl; iformat++ ) {
    					fixed_format += "s" + fformat2;
    				}
    
    				// At this point fixed_format should not be empty. 
    				if ( fixed_format.length() == 0 ) {
    					// Did not figure out how to put together the format...
    					msg = "Unable to determine data format for file (format string=\"" + fformat +
    					"\"), line number: " + line_count;
    					Message.printWarning ( 2, routine, msg);
    					throw new IOException ( msg );
    				}
    		
    				if ( Message.isDebugOn ) {
    					Message.printDebug(dl, routine, "\n");
    					Message.printDebug(dl, routine, "DATACARD header 2 content is: '");
    					Message.printDebug(dl, routine, "date1 file (month) = '" + date1_fileHeader + "'");
    					Message.printDebug(dl, routine, "date1 file (0-23 hour) = '" + date1_file + "'");
    					Message.printDebug(dl, routine, "date2 file (month) = '" + date2_fileHeader + "'");
    					Message.printDebug(dl, routine,	"date2 file (0-23 hour) = '" + date2_file + "'");
    					Message.printDebug(dl, routine, "fixed_format = '" + fixed_format + "'"); 
    				}	
    
    				break;	// last line of header
    			}
		    }  // End of the internal while  (header1 and header2)
		}
		catch ( Exception e ) {
			msg = "Error processing line " + line_count + ": \"" + string + "\"";
			Message.printWarning ( 2, routine, msg );
			Message.printWarning ( 3, routine, e );
			throw new IOException ( msg );
		}

		// Declare the time series of the proper type based on the interval.
		
        if ( Read24HoursAsDay_boolean && (hourMultiplier != 24) ) {
            msg = "Requesting that data be read as daily but time series interval (" +
            hourMultiplier + ") is not 24.";
            Message.printWarning ( 2, routine, msg);
            throw new IOException ( msg );
        }

		TSIdent ident = null;
		int data_interval_base = -1;
		int data_interval_mult = -1;
		String tsident_string = "";
		if (Read24HoursAsDay_boolean) {
			data_interval_base = TimeInterval.DAY;
			data_interval_mult = 1;
			tsident_string = location + ".." + datatype + "." + "Day";
		}
		else {
			data_interval_base = TimeInterval.HOUR;	//always hour
			data_interval_mult = hourMultiplier;
			tsident_string = location + ".." + datatype + "." + hourMultiplier + "Hour";
		}
        try {   
            ident = new TSIdent(tsident_string);
        }
        catch (Exception e) {
            // Should not happen...
            msg = "Unable to create new TSIdent using using \"" + tsident_string + "\".";
            Message.printWarning(2, routine, msg);
            throw new IOException(msg);
        }

		TS ts = null;
		// Set the time series pointer to either the requested time series or a newly-created time series.
		// Currently this parameter is only used, if available, when processing NWS Card single time series file.
		if ( req_ts != null && !is_nwsCardTrace ) {
			ts = req_ts;
			// Identifier is assumed to have been set previously.
		}
		else {
			try {
				ts = TSUtil.newTimeSeries (	ident.toString(), true );
			}
			catch ( Exception e ) {
				ts = null;
			}
		}
		if ( ts == null ) {
			msg = "Unable to create new time series for \"" + ident.toString() + "\"";
			Message.printWarning ( 2, routine, msg );
			throw new IOException ( msg );
		}

		// Only set the identifier if a new time series.  Otherwise
		// assume the existing identifier is to be used (e.g., from a file name).
		if ( req_ts == null ) {
			try {
			    ts.setIdentifier ( ident );
			}
			catch ( Exception e ) {
				msg = "Unable to set identifier to: \""	+ ident + "\"";
				Message.printWarning ( 2, routine, msg );
				throw new IOException ( msg );
			}
		}

		// Set time series properties.
		ts.setDataType ( datatype );
		ts.setDataUnits ( units );
		ts.setDescription ( description );
		ts.setDataUnitsOriginal ( units );
		
		if ( is_nwsCardTrace ) {
		    // Set additional information for the trace
	        // Set the sequence number.
	        ts.setSequenceID ( "" + date1_fileHeader.getYear() );
		}
		
        // Get the original dates as that of the full-month period in the file, accounting for ESP shift to
		// real-time date/times.  This is what will be iterated on to read the file.
		DateTimeRange range_file = readTimeSeries_CalculateFilePeriod ( ts, is_nwsCardTrace,
		        Read24HoursAsDay_boolean, runPeriodStartDate, runPeriodEndDate, date1_file, date2_file );
		DateTime date1_read = range_file.getStart();
        ts.setDate1Original ( date1_read );
        DateTime date2_read = range_file.getEnd();
        ts.setDate2Original ( date2_read );
		
		// Get the data period for the time series trace.  This is the period for which data will be available
		// in the time series.  All the values in the file will be read and those outside of the period
		// will be ignored.
		DateTimeRange range = readTimeSeries_CalculateTimeSeriesDataPeriod (
		        is_nwsCardTrace, Read24HoursAsDay_boolean,
		        hourMultiplier, runPeriodStartDate, runPeriodEndDate,
		        date1_file, date2_file, req_date1, req_date2 );
		
    	// Set the time series date1 and date2.
		date1_ts = range.getStart();
		ts.setDate1 ( date1_ts );
		date2_ts = range.getEnd();
		ts.setDate2 ( date2_ts );
		if ( is_nwsCardTrace ) {
		    Message.printStatus ( 2, routine, "[" + trace_start_year + "] Time series trace data period is " +
                date1_ts + " to " + date2_ts );
		}
		else {
		    Message.printStatus ( 2, routine, "Time series data period is " + date1_ts + " to " + date2_ts ); 
		}
		
		Message.printDebug ( dl, routine, "[" + trace_start_year + "] File period to read is " +
		        date1_read + " to " + date2_read );
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Read TS header" );
		}

    	// Data may not be needed if reading header information only.
		
		if (!read_data) {
		   	TSList.add(ts);

		   	if ( is_nwsCardTrace ) {
		  		// Make sure to skip all the data lines when processing NWS Card Trace files so that the
		   	    // next header can be read.
		    		
				// Reset the header flags back to false to force the code to read the headers for the
				// next time series trace.
				header1_found = false;
				header2_found = false;
				
				// The lines after the data should be comments so loop until the next $ comment line and break out.
				while ( true ) {
					try {
						string = in.readLine();
						++line_count;
					}
					catch ( Exception e ) {
						msg = "Error processing line " + line_count + ": \"" + string + "\"";
						Message.printWarning ( 2, routine, msg );
						throw new IOException ( msg );
					}
					
					if ( (string == null) || (string.charAt(0)=='$') ) {
				     	// Done with the last trace or ready for the next trace.
						break;
					}		
				}
				continue;
			}
			else {
				// For NWS Card single time series file, simply return the vector.
				if (warning_count > 0) {
					throw new IOException(warning_message);
				}
				return TSList;
			}
		}

		// If here the data are being read.  Allocate the memory for the data array using the date/times that
		// were set in the time series above...

		if ( ts.allocateDataSpace() == 1 ) {
			msg = "Error allocating data space...";
			Message.printWarning( 2, routine, msg );
			throw new IOException ( msg );
		}

		// Read the data.
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Reading data..." );
		}

		// There are two dates/iterators of interest when processing the data.  The first is that related
		// to the data in the file, which is always in full month blocks and uses the historical dates.  The
		// second is the location in the time series, which for normal card files may be limited by a requested
		// period, and for ESP traces needs to be in the year of the ESP run period (and may be limited by a
		// requested period).  Additionally, when an ESP run has leap-year oddities, the data in the file must
		// be sequentially transferred to the in-memory time series, even if a shift occurs due to a leap
		// year.  The transfer occurs correctly by initializing two date/times at the start of each trace:
		//    idate_file = the iterator date/time corresponding to data in the file (historical date/times)
		//    idate_ts = the iterator date/time corresponding to the time series (esp date/times)
		// The date/times are then incremented as each value is processed, resulting in values correctly being
		// transferred to the traces.  If there is a leap year involved, it is possible that a value at the end
		// of a trace may be discarded or set to missing, depending on the direction of the shift.

		// The file date/time is always initialized based on what is in the file.
        idate_file = new DateTime ( date1_file );
		if ( is_nwsCardTrace ) {
		    // Set the time series iterator date to that corresponding to the start of the month in the file
		    idate_ts = new DateTime ( date1_read );
		}
		else {
		    // Reading a normal card file so initialize the iterator to the file start.
			idate_ts = new DateTime ( date1_file );
		}

		int size, i;
		Object otoken;	// Individual data token as Object
		String token;	// Individual data token as String
		int blanks = 0;	// Number of blank data values on a line
		premature_trace_end = false; // Reset to false - set to true if not enough data in a trace
		
		// Now loop on data records within the time series (trace)

		try {
			// If dealing with traces, the next line after the end of one time series trace should be some
		    // comment lines followed by the the new header for the next trace.  If comments are found
		    // prematurely it is likely because of leap year issues so allow missing data in the trace.
		    // If extra data are found, it is also likely due to leap year issues and just ignore the value.
			doneWithThisTrace = false;
			while ( !doneWithThisTrace ) {

				// Don't trim the line because the data are fixed-format.
				string = in.readLine();
                ++line_count;
                if ( Message.isDebugOn ) {
                    Message.printDebug ( dl, routine, "Processing data line " + line_count + ": \"" + string + "\"" );
                }
				// If a comment is read (or end of file for last trace), then assume the end of the trace.  This
				// sometimes happens, for example in a leap year when Feb 29 absorbs a
				// value and an expected value at the end is not read.
                // Allow $ in normal files as comments on any line.
                if ( is_nwsCardTrace && (string == null) ) {
                    Message.printStatus(2, routine, "Detected end of file.  Assume end of trace." );
                    premature_trace_end = true;
                }
                else if ( is_nwsCardTrace && string.startsWith("$") ) {
                    Message.printStatus(2, routine, "Detected comment before end of data.  Assume end of trace." );
                    premature_trace_end = true;
                    // Now handle below as if end of data.  Do not set "string" to null.
				}
				if ( premature_trace_end || (string == null) ) {
				    // If in here then not enough data lines are available.  If it is a trace file, allow this
				    // due to known possible issues with leap years.  If a normal card file, throw an exception.
				    // In any case, process the time series contents and add to the list.
					if (is_nwsCardTrace) {
                    	Message.printStatus(2, routine, "[" + trace_start_year +
                    	    "] finished reading trace data at: " + idate_file );
                    	// Process the time series and add to the list.  If units cannot be converted, keep trying to
                    	// process and generate one exception at the end.
                    	try {
                    		// Convert units (error will be printed in this method).
                    	    if ( read_data ) {
                    	        readTimeSeriesList_ConvertDataUnits ( ts, req_units );
                    	    }
                    	}
                    	catch (Exception e) {
                   			warning_count++;
                    	}    
                    	TSList.add(ts);
                    	if ( string == null ) {
                    	    // At the end of the file.
                        	if (warning_count > 0) {
                        	    // Some serious errors have occurred.
                        		throw new IOException(warning_message);
                        	}
                        	else {
                        	    // OK to return the list
                        	    return TSList;
                        	}
                    	}
                    	else {
                    	    // Not at the end of the file.  Need to break out of the while loop that is reading data and
                    	    // read another header.  The comment read above to trigger the premature trace end is still
                    	    // in "string" and will be processed above rather than reading the next line.
                    	    header1_found = false;
                            header2_found = false;
                    	    doneWithThisTrace = true;
                    	    break;
                    	}
                    }
                    else if ( !is_nwsCardTrace && (string == null) ){
                        // A normal single time series file that is at an end.  Process and then check whether the
                        // file has prematurely ended.
                    	Message.printStatus(2, routine,
                    		"Finished reading single time series card file data at: " + idate_file );
                    	// Convert the data units if requested...
                    	if ( read_data ) {
                    	    readTimeSeriesList_ConvertDataUnits ( ts, req_units );
                    	}
                    	TSList.add(ts);
                    	// Since we are processing NWS Card single time series file there is nothing else to do,
                    	// so just return the TSList with the single time series. 
                    	return TSList;
                    }
				    if (idate_file.lessThan(date2_read)) {
				        // Only quit reading the file with an exception if a serious file truncation.
						msg = "EOF processing time series data at line "
						    + line_count + ".  Possible corrupt data file.";
						Message.printWarning(2, routine, msg);
						throw new IOException(msg);
				    }
				}

				if ( (string.trim().equals("")) ||
					((string.length() > 0) && ((string.charAt(0) == '#') || (string.charAt(0) == '$') ) ) ) {
				    // Skip blank lines and comments.
					continue;
				}

				// Only deal with the data values (not leading station ID or record count at the beginning of a
				// line.  It is possible that data values can be smashed together so parse using the fixed
				// format width.  The format uses strings so that different numbers of values in a month do
				// not cause conversion errors.

				List<Object> oTokens = StringUtil.fixedRead ( string.substring(20), fixed_format );

				// Size can be less if at end of month.  Since the values are read as strings, check for
				// blanks and reduce the length accordingly...

				size = oTokens.size();

				blanks = 0;
				for ( i = 0; i < size; i++ ) {
					otoken = oTokens.get(i);
					if ( (otoken == null) || (((String)otoken).trim().length()==0)) {
						++blanks;
					}
				}
				size -= blanks;

				if ( (size > ndpl) || (size < 1) ) {
					// Can't continue because date sequence may be off...
					msg = "Error reading data at line " + line_count + ".  File is corrupt.";
					Message.printWarning ( 2, routine, msg);
					throw new IOException ( msg );
				}

				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine, "#data on line =" + size );
					Message.printDebug ( dl, routine, "date (file) = " + idate_file );
					Message.printDebug ( dl, routine, "date (ts data) = " + idate_ts );
					Message.printDebug ( dl, routine, "Date1 = " + date1_read );
					Message.printDebug ( dl, routine, "Date2 = " + date2_read );
				}

				// Processing data.
				 
			    for ( i = 0; i < size; i++ ) {
			        // Only set values in the data period (skip over others).  This is a bit of a performance
			        // hit but can prevent a bunch of logging low-level messages about trying to set values outside
			        // the time series period.
					if( idate_ts.greaterThanOrEqualTo( date1_ts ) && idate_ts.lessThanOrEqualTo(date2_ts) ) {
						// In the requested period so set the data...
						token = ( (String)oTokens.get(i)).trim();
						ts.setDataValue(idate_ts, StringUtil.atod(token));
						if ( Message.isDebugOn ) {
							Message.printDebug ( dl, routine, "Setting value at " + idate_ts + ": " + token );
						}
					}
					if ( idate_file.lessThan(date2_file) ) {
					    // Data being processed may have corresponding data in file so 
						// advance the iterator pointers of the time series and the file data period. 
						idate_file.addInterval ( data_interval_base, data_interval_mult );
						idate_ts.addInterval ( data_interval_base, data_interval_mult );
					}
					else {
						Message.printStatus ( 2, routine, "Finished reading data at file date: " + idate_file );

						try {
						    // Convert units
						    if ( read_data ) {
						        readTimeSeriesList_ConvertDataUnits ( ts, req_units );
						    }
						}
						catch (Exception e) {
						    // Warning is printed in above method.  Add to list
							warning_count++;
						}		

						TSList.add(ts);
						if ( is_nwsCardTrace ) {
    						// Done with this time series. The next lines should be comments or EOF since dealing
    						// with with NWS Card Trace files. Reset the header flags back to false to force the
    						// code to try reading the headers for the new traces in the file, if any.
    						header1_found = false;
    						header2_found = false;
    						doneWithThisTrace = true;
    						// No need to continue processing values on the line but will need to read more data
    						// in case the requested period for traces is shorter than what is available in the file.
    						break;
    					}
						else {
						    // Processing data from a normal NWS Card file (not a trace file)
    						// Since processing NWS Card single time series file there is nothing else to do,
    						// so just return the TSList with the single time series. 
    						return TSList;
    					}
				    }
				}
			}
		}
		catch ( Exception e ) {
			Message.printWarning ( 3, routine, e );
			msg = "Unexpected error processing line " + line_count	+ ": \"" + string + "\"";
			Message.printWarning ( 2, routine, msg );
			throw new IOException ( msg );
		}
	}

	if (warning_count > 0) {
		throw new IOException(warning_message);
	}

	// The time series should be added to the TSList during the processing inside the while loops.
	return TSList;
}

/**
Calculate the data period for a time series trace.  This is the period for which data will be available
in the time series after reading.  All the values in the file will be read and those outside of the period
will be ignored.
@param is_nwsCardTrace If true then a tracefile is being read, in which case the length of the trace
will be controlled by the runPeriod*DateHistorical24 parameters.
@param Read24HoursAsDay_boolean Indicates whether 24-hour data should be read as Day interval.
@param hourMultiplier data interval as hour multiplier from original file.
@param runPeriodStartDate The ESP run period start, in 0-23 hour notation.
@param runPeriodStartEnd The ESP run period end, in 0-23 hour notation.
@param date1_file The date/time for the first data file value, using the historical date, 23-hour notation.
@param date2_file The date/time for the last data file value, using the historical date, 23-hour notation.
@param req_date1 The date/time for the first requested value, using the historical date, 23-hour notation.
@param req_date2 The date/time for the last requested value, using the historical date, 23-hour notation.
*/
private static DateTimeRange readTimeSeries_CalculateTimeSeriesDataPeriod (
        boolean is_nwsCardTrace, boolean Read24HoursAsDay_boolean, int hourMultiplier,
        DateTime runPeriodStartDate, DateTime runPeriodEndDate,
        DateTime date1_file, DateTime date2_file, DateTime req_date1, DateTime req_date2 )
{
    String routine = "NWSCardTS.readTimeSeries_CalculateTimeSeriesDataPeriod";
    DateTime date1 = null;  // The start date/time for the time series data
    DateTime date2 = null;  // The end date/time for the time series data
    
    if ( is_nwsCardTrace ) {
        // The runPeriodStartDateHistorical and runPeriodEndDateHistorical contains only the dates (hour set to
        // zero), based on the ESP "HISTORICAL RUN PERIOD" information in the main header, and has not been
        // adjusted for hour 0-23.
        // Card files are written with complete months; however, the historical run start may be anywhere
        // in the first month.  For example, the start of the ESP run may be on 3/31, with missing values for
        // all but the last day of the month.
        // The first time in the file is simply defined by adding the hourMultiplier to the header value,
        // since the first date time should always be one interval after the beginning of the day (end of period
        // values).  For 24-hour data, this adjusts to the next day.  For example a value on 3/31 hour 24 would
        // convert to 4/1 hour 0.
        date1 = new DateTime ( runPeriodStartDate );
        date2 = new DateTime ( runPeriodEndDate );
    }
    else {
        // A normal card file so the time series dates are just those from the file
        date1 = new DateTime ( date1_file );
        date2 = new DateTime ( date2_file );
    }
    
    // The dates can be overruled by the requested dates.  If a normal card file, the requested period should
    // be the historical period.  If a trace file, the requested period should be consistent with the ESP run period,
    // not the historical period.
    // Throw an exception if the dates are invalid because the hour is not divisible by the interval
    
    if ( req_date1 != null ) {
        date1 = new DateTime(req_date1);
        if ( Read24HoursAsDay_boolean ) {
            date1.setPrecision(DateTime.PRECISION_DAY);
        }
        else if ( (req_date1.getHour()%hourMultiplier) != 0 ) {
            Message.printWarning( 2, routine, "Requested start date/time " + req_date1 +
                " does not align with file hour " + date1_file + " and data interval " + hourMultiplier + "Hour" );
        }
    }
    if ( req_date2 != null ) {
        date2 = new DateTime(req_date2);
        if ( Read24HoursAsDay_boolean ) {
            date1.setPrecision(DateTime.PRECISION_DAY);
        }
        else if ( (req_date2.getHour()%hourMultiplier) != 0 ) {
            Message.printWarning( 2, routine, "Requested end date/time " + req_date1 +
                " does not align with file hour " + date2_file + " and data interval " + hourMultiplier + "Hour" );
        }
    }
    // Return the request as a range of two date/times.
    DateTimeRange range = new DateTimeRange ( date1, date2 );
    return range;
}

/**
Calculate the file period for a time series trace.  This is the period for which data will be available
in the file but in time series timeframe, and can be used for reading.  For a normal card file the period will
be the historical period.  For a trace file, the period will be the real-time period, for the full months
corresponding to the run period (which map to the full historical months).  The start date/time is suitable to
start iterating through data.  The start and end are suitable for setting the original data period on the
time series.
@return the date/time range for the data in the time series for full months.
@param ts Time series being processed.
@param is_nwsCardTrace If true then a tracefile is being read, in which case the length of the trace
will be controlled by the runPeriod*DateHistorical24 parameters.
@param Read24HoursAsDay_boolean Indicates whether 24-hour data should be read as Day interval.
@param runPeriodStartDate The ESP run period start, in 0-23 hour notation.
@param runPeriodStartEnd The ESP run period end, in 0-23 hour notation.
@param date1_file The date/time for the first data file value, using the historical date, 23-hour notation.
@param date2_file The date/time for the last data file value, using the historical date, 23-hour notation.
*/
private static DateTimeRange readTimeSeries_CalculateFilePeriod (
        TS ts, boolean is_nwsCardTrace, boolean Read24HoursAsDay_boolean,
        DateTime runPeriodStartDate, DateTime runPeriodEndDate,
        DateTime date1_file, DateTime date2_file )
{   // Start with the file date/time for full months...
    DateTime date1 = new DateTime ( date1_file );  // The start date/time for the time series data
    DateTime date2 = new DateTime ( date2_file );  // The end date/time for the time series data
    // FIXME SAM 2008-04-03 What happens when the run start is Feb 29 of a leap year?
    // What start months are in the card file for each trace and how is that handled here?
    // Currently should not get here because a check is done in the main read method.
    if ( is_nwsCardTrace ) {
        // The runPeriodStartDateHistorical and runPeriodEndDateHistorical contains only the dates (hour set to
        // zero), based on the ESP "HISTORICAL RUN PERIOD" information in the main header, and has not been
        // adjusted for hour 0-23.
        // Card files are written with complete months; however, the historical run start may be anywhere
        // in the first month.  For example, the start of the ESP run may be on 3/31, with missing values for
        // all but the last day of the month.
        // The first time in the file is simply defined by adding the hourMultiplier to the header value,
        // since the first date time should always be one interval after the beginning of the day (end of period
        // values).  For 24-hour data, this adjusts to the next day.  For example a value on 3/31 hour 24 would
        // convert to 4/1 hour 0.  The year and month are those from the ESP run, not the header for each time
        // series trace.
        date1.setYear( runPeriodStartDate.getYear() );
        // Get the number of intervals in the run period and increment from the start.  Do this in case there
        // are leap years involved.  We can do a simple shift of the start date/time (because an ESP run start of
        // Feb 29 is not supported by the read method) but setting the end is a little more trouble.
        int nvals = TimeUtil.getNumIntervals(runPeriodStartDate, runPeriodEndDate,
                ts.getDataIntervalBase(), ts.getDataIntervalMult() ) - 1;
        date2 = new DateTime ( date1 );
        for ( int i = 0; i < nvals; i++ ) {
            date2.addInterval( ts.getDataIntervalBase(), ts.getDataIntervalMult() );
        }
    }
    
    // Return the request as a range of two date/times.
    DateTimeRange range = new DateTimeRange ( date1, date2 );
    return range;
}

/**
Helper method for read method to convert the data units for a time series, as requested.
@param ts Time series to process.
@param req_units Requested units.
*/
private static void readTimeSeriesList_ConvertDataUnits ( TS ts, String req_units )
throws Exception
{   String routine = "NWSCardTS.readTimeSeriesList_ConvertDataUnits";
    if ( (ts != null) && (req_units != null) && !req_units.equalsIgnoreCase(ts.getDataUnits())) {
        // Convert units
        try {
            TSUtil.convertUnits(ts, req_units);
        }
        catch (Exception e) {
            String msg = "Could not convert time series units from \"" + ts.getDataUnits() +
            "\" to requested \"" + req_units + "\".";
            Message.printWarning(3, routine, msg);
            Message.printWarning(3, routine, e);
            throw new Exception(msg);
        }
    }
}

/**
Write a time series to the open PrintWriter.
@param ts Time series to write.
@param fp PrintWrite to write to.
@exception IOException if there is an error writing the file.
*/
public static void writeTimeSeries ( TS ts, PrintWriter fp )
throws IOException
{	// Call the method that takes multiple time series...
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
@param req_units Units to write.  If different than the current units the units will be converted on output.
@param write_data Indicates whether data should be written.
@exception IOException if there is an error writing the file.
*/
public static void writeTimeSeries ( TS ts, PrintWriter fp,	DateTime req_date1, DateTime req_date2,
					String req_units, boolean write_data )
throws IOException
{	String	cfmt = "%10.3f", dimension, nfmt = "F10.3", message, routine="NWSHourTS.writePersistent";
    int ndpl = 6; // Number of data per line.

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
        int ndigits = (int)Math.log10(Math.max(Math.abs(max),Math.abs(min))) + 3;
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
	
    // Card files need to use -999 or -998 for missing.  If the value is not in this range, set it to -999.
	double missing = ts.getMissing();   // used when printing outside requested period
    if ( (missing >= -997.99999) || (missing < -999.00001) ) {
        Message.printStatus ( 2, routine, "Will output missing values as recognized CARD value -999 " +
            "rather than internal value " + missing );
        missing = -999.0;
    }

	// The above dates should not be printed anywhere else, although the
	// 24-hour issue is addressed below.

	fp.println ( "$  PERIOD OF RECORD=" + date1_file.toString(DateTime.FORMAT_MM_SLASH_YYYY) + " THRU " +
	date2_file.toString(DateTime.FORMAT_MM_SLASH_YYYY));

	fp.println ( "$  SYMBOL FOR MISSING DATA=" + StringUtil.formatString(missing,"%.2f") +
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
	fp.println ( "$  TYPE=" + StringUtil.formatString (ts.getDataType(),"%-4.4s") +
	"   UNITS=" + StringUtil.formatString(data_units,"%-4.4s") +
	"   DIMENSIONS=" + StringUtil.formatString(dimension,"%-4.4s") +
	"   DATA TIME INTERVAL=" + StringUtil.formatString(hours,"%2d") + " HOURS" );

	fp.println ( "$  OUTPUT FORMAT=(3A4,2I2,I4," + ndpl + nfmt + ")" );

    // Print the 2 non-comment header cards...

	String interval_mult_string = "";
	if ( data_interval_base == TimeInterval.HOUR ) {
		interval_mult_string = StringUtil.formatString(	ts.getDataIntervalMult(),"%2d");
	}
	else if ( data_interval_base == TimeInterval.DAY ) {
		interval_mult_string = "24";
	}
	fp.println ( "DATACARD      " + StringUtil.formatString(ts.getDataType(),"%-4.4s") + " " +
	StringUtil.formatString(dimension,"%-4.4s") + " " +
	StringUtil.formatString(data_units,"%-4.4s") + " " + interval_mult_string + "   " +
	StringUtil.formatString(id.getLocation(),"%-12.12s") + "   " + ts.getDescription() );

	fp.println ( StringUtil.formatString(date1_file.getMonth(),"%2d") + "  " +
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
        else if ( ts.isDataMissing(value)) {
            // Use missing value that may have been adjusted to CARD missing value
            value = missing;
        }
        else if ( convert_units ) {
    		 // Need to convert units if not missing
     		 value = value*mult + add;
        }
        // Now append the value to the buffer
        buffer.append( StringUtil.formatString(value,cfmt) );

		// Determine whether the line should be printed.  The line is printed if the number of data values is
		// evenly divisible by the number of values for the line or if all values for the month have been printed.
		//
		// If read to print, print the line information and then the StringBuffer data contents from above.
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
		// since internal data will be stored in the future (period ending at end of day),
		if ( (data_interval_base == TimeInterval.HOUR) && (date.getDay() == 1) && (date.getHour() == 0) ) {
			// Last value of NWSCARD month is actually in next month
			// in internal data so we need to start a new month for the next data value...

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

}