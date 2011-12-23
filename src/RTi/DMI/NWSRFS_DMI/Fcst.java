// ----------------------------------------------------------------------------
// Fcst - class for interacting with the NWSRFS fcst program (e.g., process
//			output files).
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2002-08-28	J. Thomas Sapienza, RTi	Initial version.
// 2002-08-29	JTS, RTi		Data structure used for holding data
//					as it is read in from the files is
//					now a IrregularTS of TSData objects.
//					All Dates turned into TSDates.  Lots
//					of code ripped out, lots more 
//					re-arranged and refactored.  Lots of
//					method calls in-lined.
// 2002-09-03	JTS, RTi		main() removed to test class.  Misc
//					methods compacted into a processMAP
//					method.  Lots more refactoring.  
//					Unneeded methods removed, code 
//					better commented.
// 2002-09-04	JTS, RTi		More code cleanup.  Units were not
//					getting in the file properly, now they
//					do.  Files are now named after the 
//					time series' TSIdent.toString() value.
//					Genesis values are created for time
//					series.
// 2002-09-12	JTS, RTi		MAPArea renamed to Fcst.
// 2002-10-31	JTS, RTi		Changed a formatting problem in the
//					less-than-24 hour line.  s5x1d8 became
//					s8d6. 
//					Added code to keep
//					track of the current line number and
//					the last read line of text, for access
//					from an external program.  Added
//					getCurrentLine and getCurrentLineNUmber
//					methods.  Exceptions are no longer
//					caught inside this method, but instead
//					passed outside of it to the calling
//					method.  Also, tsids ids are trimmed
//					to prevent spaces in the TSIdent code
//					and the filename.  The data flag gets
//					written out now, also.
// 2003-01-16	JTS, RTi		Changed the format of the formatDaily
//					line to read in station ids up to 8
//					characters in length.
// 2003-07-23	Steven A. Malers, RTi	* Change TSDate to DateTime.
//					* Change TS.INTERVAL to TimeInterval.
//					* Remove redundant calls to
//					  TS.setDataType() since this is in
//					  TSIdent now.
//					* Change class method from static, thus
//					  requiring an instance of Fcst() to
//					  be declared.  This is done to
//					  minimize public visibility of methods
//					  and minimize the footprint of the
//					  class.
//					* Remove some debug code.
//					* Clean up javadoc throughout to be more
//					  complete and accurate.
//					* Change buildDayTS() to not take units
//					  and identifier as parameter - these
//					  can be taken from the IrregularTS that
//					  is passed as a parameter.
//					* Update fillDayTS() - does not look
//					  like the data flag was being properly
//					  transferred.  Also make sure the data
//					  flags are turned on in the resulting
//					  daily time series.
//					* Change writeFile() to check for the
//					  existence of a file rather than
//					  relying on a null time series from the
//					  read to know if the time series does
//					  not exist.  Only throw one exception
//					  at the end instead of when the first
//					  error occurs with a file.  Remove the
//					  units as a parameter to this method.  
//					  Make sure that existing files that are
//					  read have a daily interval.
//					* processMAPOutput() - check for an
//					  empty file list.  Only throw one
//					  exception at the end instead of for
//					  the first input file with an error.
//					  This allows as much data as possible
//					  to be processed.  Because of this,
//					  the use of getCurrentLine() and
//					  getCurrentLineNumber() does not make
//					  as much sense.  Important data were
//					  not being reinitialized for each file
//					  because code was commented out - fix.
//					  Change from using Vector to an integer
//					  array for field positions - there is
//					  no reason to incur the overhead of
//					  conversions and casts with Vector
//					  since the integer arrays are of fixed
//					  length.  Change "code" to "id", to
//					  avoid confusion.
//					* processData() - change to receive
//					  arrays instead of Vectors.  No need
//					  to return a Vector of line number and
//					  hashtable because line number is a
//					  member of the class and hashtable is
//					  passed by reference.  Trim character
//					  information as it is read.  Change
//					  "code" to "id", to avoid confusion.
//					  Remove inaccurate check for blank
//					  name to indicate partial data - fixed
//					  read should correctly return the
//					  correct number of fields - maybe the
//					  other corrections fix the error the
//					  check was meant to address.  The
//					  time series was being reset in the
//					  hashtable for each data value - only
//					  call put() when a new time series is
//					  added.
// 2003-10-01	Anne Morgan Love, RTi	* The file names output from the 
//					processMap() map were created from the
//					tsident of the TS when it was still
//					an Irregular TS and not a Day TS, so
//					the interval part of the TS file name 
//					itself was still "Irregular" even
//					though the TS was "Day". Change filename
//					to have the "Day" extension.
// 2004-11-02	SAM, RTi		* Fix bug where reading the daily data
//					  caused the header line for less than
//					  daily data to be consumed, resulting
//					  in less than daily data to be ignored.
//					* Rename processData() to
//					  processMAPDataSection() to clarify
//					  code.
//					* Remove the deprecation tags for this
//					  class since it was never moved to the
//					  NWSRFS_DMI package (and probably
//					  should not be).
// ----------------------------------------------------------------------------
// EndHeader

package RTi.DMI.NWSRFS_DMI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import RTi.TS.DateValueTS;
import RTi.TS.DayTS;
import RTi.TS.IrregularTS;
import RTi.TS.TS;
import RTi.TS.TSData;
import RTi.TS.TSIdent;
import RTi.TS.TSIterator;

import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
The Fcst class interacts with the NWSRFS fcst program.  At this time the
following capabilities are available:
<ol>
<li>	Read data from MAP output files and store it in DateValue time series
	files, including allowing the data files to be appended.  This is
	used to maintain in a persistent way the MAP input values and data
	flags, which are otherwise lost because they are not available in the
	NWSRFS database files and the MAP output files are discarded.
</li>
to NWSRFS_Fcst.java
*/
public class Fcst
{

					// May need to make these specific to
					// the public method that is called but
					// for now assume the line number and
					// string will not be in conflict with
					// the class features...

private int __lineNumber;		// Current line number being read from
					// the file (1 = first line).  This can
					// be requested if an error occurs, for
					// use in warnings.

private String __line;			// Current line of text read from the
					// file.  This can be requested if an
					// error occurs, for use in warnings.

/**
Build a DayTS object from the data stored in the IrregularTS passed in.  It is
assumed that each data value is a daily value and can therefore be transferred
into values in the DayTS.
@param its the IrregularTS containing data to be placed in the DayTS object.
@return a DayTS object filled with the data from the IrregularTS passed in.
*/
private DayTS buildDayTS ( IrregularTS its )
{	DateTime date1 = its.getDate1();
	DateTime date2 = its.getDate2();

	DayTS dts = new DayTS();
	TSIdent tsid = null;
	try {
	    tsid = new TSIdent ( its.getLocation(), "NWSRFS", "PTPX", "Day", "" );
		dts.setIdentifier(tsid);
	}
	catch ( Exception e ) {
		// Should not happen...
		Message.printWarning ( 2, "Fcst.buildDayTS", e );
	}
	dts.setDate1(date1);
	dts.setDate2(date2);
	dts.setDataUnits(its.getDataUnits());
	dts.hasDataFlags(true,true);
	dts.allocateDataSpace();
	dts.setDescription(its.getDescription());

	TSIterator itsi = null;
	try {
	    itsi = its.iterator();
	}
	catch ( Exception e ) {
		// Should not happen...
		Message.printWarning ( 2, "Fcst.buildDayTS", e );
	}

	TSData tsd;
	for ( tsd = itsi.next(); tsd != null; tsd = itsi.next() ) {
		dts.setDataValue(tsd.getDate(), tsd.getDataValue(), tsd.getDataFlag(), 0);
	}

	return dts;
}

/**
Fill an existing DayTS object with data stored inside an IrregularTS object.
@param dts the DayTS to fill with data,
@param its the IrregularTS from which the data are pulled.  It is assumed that
the irregular data are daily values that can be placed in the corresponding
positions in the daily time series.
@return a DayTS object filled with the data from the IrregularTS.
@throws Exception thrown by DayTS.changePeriodOfRecord()
*/
private DayTS fillDayTS ( DayTS dts, IrregularTS its ) 
throws Exception
{	DateTime iDate1 = its.getDate1();
	DateTime iDate2 = its.getDate2();
	DateTime dDate1 = dts.getDate1();
	DateTime dDate2 = dts.getDate2();

	DateTime date1;
	DateTime date2;

	// The DayTS object needs to accommodate all the dates from earliest to
	// latest, so check to see if the date extremes read in the existing
	// time series lie outside of the dates in the DayTS.  If so, resize the
	// DayTS to accommodate them.

	if (iDate1.lessThan(dDate1)) {
		date1 = iDate1;
	} else {
		date1 = dDate1;
	}
	
	if (iDate2.greaterThan(dDate2)) {
		date2 = iDate2;
	} else {
		date2 = dDate2;
	}

	dts.changePeriodOfRecord(date1, date2);

	// Now transfer the data from the irregular to regular time series...

	TSIterator itsi = its.iterator();

	// Make sure the daily time series has data flags turned on.  Data read
	// from an existing file may not have data flags, for some reason.

	dts.hasDataFlags(true,true);
	
	TSData tsd;
	for ( tsd = itsi.next(); tsd != null; tsd = itsi.next() ) {
		dts.setDataValue(tsd.getDate(), tsd.getDataValue(), tsd.getDataFlag(), 0 );
	}

	return dts;
}

/**
Returns the last line read from the file.  This is useful for warning messages
if there has been an error processing a file.
@return the last line read from the file.
@deprecated Information is not useful now the multiple errors are ignored during
processing in order to let as much processing occur as possible.
*/
public String getCurrentLine()
{	return __line;
}

/**
Returns the line number of the line last read from the file.  This is useful for
warning messages if there has been an error processing a file.
@return the line number of the line last read from the file.
@deprecated Information is not useful now the multiple errors are ignored during
processing in order to let as much processing occur as possible.
*/
public int getCurrentLineNumber()
{	return __lineNumber;
}

/**
Processes a data section block from a MAP function output file and reads all the
data into IrregularTS objects, which are stored in a Hashtable using the station
identifier as the key.
A data section block is a segment of a MAP file in which either data for
stations with daily data, or stations with less then 24 hour data are stored.
It consists of a number of lines with 1, 2 or 3 stations, their name and
identifier, and data values on it.
@param br the BufferedReader opened on a file to use for reading through 
the lines of the MAP file.
@param ht the Hashtable into which to read the data.
@param date the DateTime for which the data in this section of the MAP file
is valid.  Taken from the line that delineates the beginning of a MAP
data section.
@param last_header_string A string at the beginning of a line that indicates
that the last header line has been read.  Subsequent lines will then be data
lines.
@param format the format in which the data are stored (for use by 
StringUtil.fixedRead().
@param nameFields an array containing the position of all the fields in the
Vector returned by StringUtil.fixedRead() in which a Station name is stored.
@param idFields an array containing the position of all the fields in the
Vector returned by StringUtil.fixedRead() in which a Station identifier is
stored.
@param dataFields an array containing the position of all the data fields in the
Vector returned by StringUtil.fixedRead() in which a data value is stored.
@return the last line read, which will be evaluated to determine if another
data section is starting.
@throws Exception thrown if the end of file is encountered unexpectedly.
*/
private String processMAPDataSection (	BufferedReader br, Hashtable ht, 
					DateTime date,
					String last_header_string,
					String format,
					int [] nameFields, int [] idFields,
					int [] dataFields, String filename,
					String units )
throws Exception
{	String routine = "Fcst.processMAPDataSection";
	int dl = 10;
	// Skip all the lines that occur between the header for this section of
	// data and the actual start of the data values.  Do so by searching
	// for a specific string at the start of the line.

	while (true) {
		String line = br.readLine();
		if(line == null) {
			throw new Exception ("EOF Encountered early while "
				+ "reading file");
		}
		__line = line;
		__lineNumber++;
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,
			"MAP section output line " + __line + ": \"" +
			line + "\"");
		}

		if ( line.startsWith(last_header_string) ) {
			// Last header line has been read...
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				"Last line of MAP section header." );
			}
			break;
		}
	}
	// The next line will be a data line to parse.
	
	// get the number of data fields stored on one line of the file
	int num = nameFields.length;
	IrregularTS its;

	int namePos = 0;
	int idPos = 0;
	int dataPos = 0;
	int flagPos = 0;
	String name;
	String id;
	double data;
	String flag;
	int i = 0;
	List read = null;
	String line = null;

	// start looping through and reading the lines of text
	while (true) {
		line = br.readLine();
		__lineNumber++;
		__line = line;
		
		// lines should never be null while reading this section
		// of the file.  A null line means EOF encountered early.
		if (line == null) {
			throw new Exception("Error reading " + filename + 
				"at line: " + __lineNumber);
		}

		// List of occurences that mark the valid end of the data.  This
		// line must be returned to the calling code to evaluate for
		// another section.

		if ( line.startsWith("1") || line.startsWith("0")) {
			Message.printDebug ( dl, routine,
			"Found MAP section output line " + __line +
			": \".  Returning to main processor." );
			return line;
		}

		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,
			"Parsing MAP section output line " + __lineNumber +
			": \"" + line + "\"...");
		}

		// split up the line of text according to the formatting rules.
		read = StringUtil.fixedRead(line, format);

		// there can from 1 to 3 data values on a line
		for ( i = 0; i < num; i++) {
			namePos = nameFields[i];
			idPos = idFields[i];
			dataPos = dataFields[i];
			// Data flag is always one after the data value...
			flagPos = dataPos + 1;

			name = ((String)read.get(namePos)).trim();
			id = ((String)read.get(idPos)).trim();
			data  = ((Double)read.get(dataPos)).doubleValue();
			flag = ((String)read.get(flagPos)).trim();
			if ( id.equals("") && (i == 0) ) {
				// Unexpected.  Assume that the data section
				// is incomplete.  This normally occurs when
				// one of the columns in a multi-column
				// output is blank because the number of
				// stations is not evenly divisible by the
				// number of columns.  It should not happen for
				// the first column.
				Message.printWarning ( 2,
				"Fcst.processMAPDataSection",
				"No ID in data section.  " +
				"File/software problem?  File: \"" + filename +
				"\" line: " + __lineNumber + ":" );
				Message.printWarning ( 2, routine, line );
				return line;
			}

			// Get the IrregularTS stored in the Hashtable and
			// associated with the identifier value just read out.
			its = (IrregularTS)ht.get(id);
			if (its == null) {
				// A new time series needs to be created and populated with the base information.
				its = new IrregularTS();
				TSIdent tsid = new TSIdent(id,"NWSRFS","PTPX","Irregular", "");
				its.setIdentifier(tsid);
				its.setDataUnits(units);
				its.setDescription(name);
				its.hasDataFlags(true,true);
				ht.put(id, its);
			} 
			// The date has a precision of day so that data later can be transferred to a DayTS.
			its.setDataValue(date, data, flag, 0);
		}
	}
}

/**
Process fcst program MAP function output file(s) into daily DateValue TS files
(one per time series), saving the time series in the directory indicated by the
"outputDir" parameter.  Available daily values and data flags are
written to the time series files.  Time series file names are the same as
the time series identifiers.  Note that no special care is taken to deal with
the start of the hydrologic day - the dates in the daily files do not have the
hour and simply correspond to the daily values available in the MAP output files.
@param fileList a Vector of MAP function output files to process (not to be
confused with MAP time series files).
@param outputDir the directory to which time series data files should be
written (typically the OFS or user output directory).
@param append if true, the new data will be appended to the existing time series
files data.  If false, old time series files will be overwritten with new data. 
@throws Exception thrown if there is a problem writing out the time series to a file.
*/
public void processMAPOutput ( List fileList, String outputDir, boolean append )
throws Exception
{	String routine = "Fcst.processMAPOutput";
	int dl = 10;
	/////////////////////////////////////////////////////////////
	// Constants
	/////////////////////////////////////////////////////////////
	final int __VERSION_5_2_1r20 	= 1000;
	final int __VERSION_5_4_28 	= 1001;
	final int __VERSION_UNKNOWN	= 0;

	/////////////////////////////////////////////////////////////
	// control and value storage variables
	/////////////////////////////////////////////////////////////
	int fileCount = 0;
	if ( fileList != null ) {
		fileCount = fileList.size();
	}
	boolean processing = false;
	boolean done = false;
	String units = "";
	boolean unitsSet = false;
	String filename = "";

	/////////////////////////////////////////////////////////////
	// Hashtables
	// The hashtables store the information from the MAP files as it
	// is read in.  The hastables use as a key the station identifier
	// (usually a 5-character Handbook 5 identifier) from the MAP file.  The
	// data stored with that key is an IrregularTS that contains daily data
	// values (not 6-hour, etc.!!!).  IrregularTS are used because they
	// more easily allow data points to be inserted dynamically.
	//
	// The data that are will fill the IrregularTS are spread out inside of
	// a MAP file, and possibly multiple MAP files if more than one file is
	// provided.
	//
	// The hashtables are used because that way a value can be read from
	// the MAP file, the identifier key can be read, and from that the
	// time series to hold the data value can be brought up easily 
	// and the value inserted.

	Hashtable htDaily = new Hashtable();	// Hash table for daily stations
	Hashtable htLess24 = new Hashtable();	// Hash table for stations
						// reporting < 24 hours.  Note
						// than in this case, the values
						// in the MAP output are still
						// daily values (the values that
						// would result from applying
						// the normalize coefficients
						// are not saved).

	/////////////////////////////////////////////////////////////
	// version-specific parse-helper variables
	/////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////
	// SPECIFIC TO THE DAILY REPORTS SECTION
	String formatDaily = "";		// The format of the lines in
						// which the data are stored.
	String formatDailyLine = "";		// The format of the line on
						// which the date of the data
						// are stored.
	int [] dailyNameFields = new int[3];	// An array indicating the
						// positions for the station
						// names in the Vector returned
						// from StringUtil.fixedRead()
						// using formatDaily.
	int [] dailyIdFields = new int [3];	// An array indicating the
						// positions for the station
						// identifiers in the Vector
						// returned from
						// StringUtil.fixedRead() using
						// formatDaily.
	int [] dailyDataFields = new int[3];	// An array indicating the
						// positions for the the data
						// values in the Vector returned
						// from StringUtil.fixedRead()
						// using formatDaily.
	String dailyLine = "";			// The first part of the line
						// that starts off the section
						// containing daily data.  The
						// actual date is not in this
						// string.
	String dailyLineSpace = "";		// String indicating the last
						// line in the daily header
						// (next line is data).

	/////////////////////////////////////////////////////////////
	// SPECIFIC TO THE LESS THAN 24 HOUR SECTION

	String formatLess24 = "";		// The format of the lines in
						// which the data are stored.
	String formatLess24Line = "";		// The format of the line on
						// which the date of the data
						// are stored.
	int [] less24NameFields = new int[2];	// An array indicating the
						// positions for the station
						// names in the Vector returned
						// from StringUtil.fixedRead()
						// using formatLess24.
	int [] less24IdFields = new int[2];	// An array indicating the
						// positions for the station
						// identifiers in the Vector
						// returned from
						// StringUtil.fixedRead() using
						// formatLess24.
	int [] less24DataFields = new int[2];	// An array indicating the
						// positions for the data values
						// in the Vector returned from
						// StringUtil.fixedRead() using
						// formatLess24.
	String less24Line = "";			// The first part of the line
						// that starts off the section
						// containing less than 24 hour
						// data.  The actual date is not
						// in this string.
	String less24LineSpace = "";		// String indicating the last
						// line for less than 24-hour
						// data (next line is data).

	String mapFunctionLine = "";		// The line that starts off the
						// MAP FUNCTION section of the
						// MAP file
	String errorLine = "";			// The line that denotes the end
						// of the data section of the
						// file.
	String MAP_line = null;			// Last line of data from an
						// MAP section.

	String unitsLine = "";
	String unitsFormat = "";

	String warning = "";			// Keep track of warnings.

	// Loop through each file.  If an error occurs for a specific file,
	// save a warning and continue to the next file.

	BufferedReader br = null;

	for (int i = 0; i < fileCount; i++) {

		// Open the file...

		filename = (String)fileList.get(i);

		try {	br = new BufferedReader( new FileReader(filename));
		}
		catch ( Exception e ) {
			Message.printWarning(2, "processMAPOutput",
			"File Not Found: " + filename);
			warning += "\nError opening \"" + filename + "\"";
			continue;
		}

		// Initialize variables for the file...

		__lineNumber = 0;
		__line = "{NO LINE READ FROM FILE YET}";
		processing = false;
		done = false;
		units = "";
		unitsSet = false;

		// Determine Version information which impacts the format of the
		// file - this is included in case file formats change in the
		// future...

		String firstLine = br.readLine();
		__lineNumber++;
		__line = firstLine;
		if (firstLine == null) {
			warning += "\nError reading first line of \""+
				filename + "\"";
			br.close();
			br = null;
			continue;
		}

		int version = __VERSION_UNKNOWN;
		List fixedRead = StringUtil.fixedRead(firstLine, "s53s25");
		String ver = ((String)fixedRead.get(1)).trim();
		if (ver.endsWith(")")) {
	 		ver = ver.substring(0, ver.length() -1);
			int intver = __VERSION_UNKNOWN;
	
			if (ver.equals("5.2.1r20   - 02/25/02")) {
				intver = __VERSION_5_2_1r20;
			} 
			else if (ver.equals("5.4.28   - 07/20/99")) {
				intver = __VERSION_5_4_28;
			}	
			version = intver;
		}

		/////////////////////////////////////////////////////////////
		// Set up version-specific information.
		// If the version could not be determined above (and it is still
		// __VERSION_UNKNOWN) it will not cause a problem below.  It is
		// assumed that unknown versions are caused by updated software
		// versions, and that the version is still backwards compatible
		// with the latest version of the MAP output file.
		/////////////////////////////////////////////////////////////

		if (	(version == __VERSION_5_2_1r20) ||
			(version == __VERSION_5_4_28) ||
			(version == __VERSION_UNKNOWN) ) {
			mapFunctionLine = "0                        " 
				+ "                     MAP FUNCTION";
			errorLine = "0               SUMMARY "
				+ "OF ERRORS, "
				+ "WARNINGS AND TIME USED FOR "
				+ "FUNCTION MAP     :";

			// The following is used to parse a line such as:
			//  CASAS GRANDES        CH  CAGCH        0.       CD. CAMARGO          CH  CCOCH        0.       CD. DELICIAS         CH  CDLCH        0. 
			//	formatDaily =	"x1s20s3x2s5x1d9s1" + 
			//			"x6s20s3x2s5x1d9s1" + 
			//			"x6s20s3x2s5x1d9s1";
			// THE ABOVE CHANGED TO THE FOLLOWING AFTER CONSULTING
			// WITH MDK AS TO THE LENGTH OF STATION ID VALUES.
			// JTS 2003-01-16
			formatDaily =  
			"x1s20s3x2s8d7s1x6s20s3x2s8d7s1x6s20s3x2s8d7s1";
			// The following is used to parse a line such as:
			// 1                PRECIPITATION TOTALS FOR STATIONS WITH ONLY DAILY REPORTS FOR DAY ENDING ON  5/ 9/2002- 6CST 
			formatDailyLine = "s93i2s1i2s1i4";

			// The following arrays store the position information
			// of data that will be used with the time series, in
			// regard to the location of that data in the vectors
			// returned from calling StringUtil.fixedRead() with the
			// format above (formatDaily) on a line of data from the
			// file.
			//
			// They are used in order to iterate through the
			// stations stored on a line in the file.  Since every
			// line can hold one, two, or three stations it is easy
			// to duplicate code and use the same section of code to
			// pull out each station's information.  The first
			// station on the line uses the 0 position in each
			// vector to know where its values are stored, the
			// second uses the 1 position, and the third uses the 2
			// position.  

			dailyNameFields[0] = 0;
			dailyNameFields[1] = 5;
			dailyNameFields[2] = 10;
				
			dailyIdFields[0] = 2;
			dailyIdFields[1] = 7;
			dailyIdFields[2] = 12;

			dailyDataFields[0] = 3;
			dailyDataFields[1] = 8;
			dailyDataFields[2] = 13;
				
			// This is the string that starts off the line
			// containing the date for which the data are stored.

			dailyLine =	"1                PRECIPITATION "
					+ "TOTALS "
					+ "FOR STATIONS WITH ONLY DAILY "
					+ "REPORTS FOR "
					+ "DAY ENDING ON";
			dailyLineSpace = " -----------";

			// The following is used to parse a line like:
			// LUIS_L.LEON_(FT_QUI) CH  LSLCH       0.E   .25 .25 .25 .25   U       EL MULATO            CH  MLTCH       0.E   .25 .25 .25 .25   U
			formatLess24 =
			"x1s20s3x2s8d6s1x3d3x1d3x1d3x1d3x3s1x7s20s3x2s8d6s1";
			// The following is used to parse a line like:
			//1          PRECIPITATION DISPLAY FOR STATIONS WITH LESS THAN 24 HOUR REPORTS FOR DAY ENDING ON  5/ 9/2002- 6CST 
			formatLess24Line = "s95i2s1i2s1i4";
				
			// The following arrays store the position information
			// of data that will be used with the time series, in
			// regard to the location of that data in the vectors
			// returned from calling StringUtil.fixedRead() with the
			// format above (formatLess24) on a line of data from
			// the file.
			//
			// They are used in order to iterate through the
			// stations stored on a line in the file.  Since every
			// line can hold one, two, or three stations it is easy
			// to duplicate code and use the same section of code to
			// pull out each station's information.  The first
			// station on the line uses the 0 position in each
			// vector to know where its values are stored, the
			// second uses the 1 position, and the third uses the 2
			// position.  

			less24NameFields[0] = 0;
			less24NameFields[1] = 10;
				
			less24IdFields[0] = 2;
			less24IdFields[1] = 12;
				
			less24DataFields[0] = 3;
			less24DataFields[1] = 13;

			// This is the string that starts off the line
			// containing the date for which the data are stored.				
			less24Line =	"1          PRECIPITATION "
					+ "DISPLAY FOR " 
					+ "STATIONS WITH LESS THAN 24 "
					+ "HOUR REPORTS FOR "
					+ "DAY ENDING ON";			
			less24LineSpace = " -----------";
				
			unitsLine =	"                     STATION "
					+ "AND MAP TIME SERIES DISPLAYS "
					+ "ARE IN ";
			unitsFormat = "s65s2";
		}
		
		// Set up some variables for increased performance.
		// They will be reused a lot.

		List read;
		DateTime tsd;
		String line;

		/////////////////////////////////////////////////////////////
		// process the file
		/////////////////////////////////////////////////////////////

		MAP_line = null;	// Last line read from MAP section,
					// which needs to be evaluated here.
		while (true) {
			if ( MAP_line != null ) {
				// Use the line of data read from a MAP data
				// section...
				line = MAP_line;
				MAP_line = null;	//To avoid infinite loop
			}
			else {	line = br.readLine();	
				__lineNumber++;
			}
			if (line == null) {
				break;
			}
			__line = line;
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				"FCST output line " + __line + ": \"" +
				line + "\"");
			}

			// Boolean control variables used below are:
			// processing
			// done
			//
			// Both are initially set to false.
			// processing is set to true if the mapFunctionLine
			// has been found in a file.  This means that the
			// file's data will be showing up soon.
			// done is set to true when the end of the data
			// section of the file has been located.  
			//
			// The two will never be true at the same time.
			// If processing is false and done is false, then
			// lines are skipped over until the header line is 
			// found and processing is set to true.
			// If processing is true and done is false, lines
			// will be checked to see if they start a section 
			// of data or if they mark the end of the file's 
			// data sections.  If the end has been found,
			// processing is set to false and done is set
			// to true.
			// If done is true and processing is false, lines
			// are skipped over until EOF is reached.
			/////////////////////////////////////////////////////
			// the header line has not been found yet
			/////////////////////////////////////////////////////
			if (!processing && !done) {
				if (line.startsWith(mapFunctionLine)) {
					processing = true;
				}
			}
			/////////////////////////////////////////////////////
			// the header line has been found, so currently in 
			// the section with data to parse
			/////////////////////////////////////////////////////
			else if (processing && !done) {		
				// Boolean is checked to increase performance
				// a little - units should be consistent
				// throughout the file...
				if (!unitsSet && line.startsWith(unitsLine)) {
					read = StringUtil.fixedRead(line, unitsFormat);
					units = ((String)read.get(1)).trim();
					unitsSet = true;
				}
				else if (line.startsWith(dailyLine)) {
					read =	StringUtil.fixedRead(
						line, formatDailyLine);
					tsd = new DateTime();
					tsd.setMonth(((Integer)read.get(1)).intValue());
					tsd.setDay(((Integer)read.get(3)).intValue());
					tsd.setYear(((Integer)read.get(5)).intValue());
					tsd.setPrecision(DateTime.PRECISION_DAY);

					// Process a data section...

					MAP_line = processMAPDataSection(
						br, htDaily, tsd,
						dailyLineSpace, formatDaily, 
						dailyNameFields, 
						dailyIdFields, 
						dailyDataFields, 
						filename, units);
				}
				else if (line.startsWith(less24Line)) {
					read = StringUtil.fixedRead(
						line, formatLess24Line);
					tsd = new DateTime();
					tsd.setMonth(((Integer)read.get(1)).intValue());
					tsd.setDay(((Integer)read.get(3)).intValue());
					tsd.setYear(((Integer)read.get(5)).intValue());
					tsd.setPrecision( DateTime.PRECISION_DAY);
		
					MAP_line = processMAPDataSection (
						br, htLess24, tsd, 
						less24LineSpace, 
						formatLess24, 
						less24NameFields, 
						less24IdFields, 
						less24DataFields,
						filename, units);
				}
				else if (line.startsWith(errorLine)) {
					done = true;
					processing = false;
				}
			}
			/////////////////////////////////////////////////////
			// the footer line has been found, so the rest of 
			// the file can be skipped over.  
			/////////////////////////////////////////////////////
			else if (!processing && done) {
				// skip to end of file
				break;
			} 
		}
		// Close the file...
		br.close();
	}

	// The data are in the hashtables, so write the output

	writeFile ( htDaily, outputDir, append, fileList, warning );
	writeFile ( htLess24, outputDir, append, fileList, warning );	

	if ( warning.length() > 0 ) {
		Message.printWarning ( 2, routine,
		"Error processing or more MAP output time series:" + warning );
		throw new Exception (
		"Error processing one or more MAP output time series:" +
		warning );
	}
}

/**
Writes out the data from a hashtable to daily DateValue time series files (one
file per time series in the hashtable).  The names of the time series files are
the same as the time series identifiers.
@param ht the hashtable containing all of the data to write.  The hashtable
consists of a key (the station identifier from the MAP function output file) and
an associated IrregularTS of the values.  The values in the IrregularTS actually
correspond to daily values and the time series is converted to a daily time
series before output.  Irregular time series are used to allow addition of
values in a dynamic way as the MAP output file is processed.
@param outputDir the directory to which to write the files.
@param append if true, existing files will have the new data in the
Hashtables appended to the existing data.  If false, old files will be
overwritten with new data. 
If append is set to true, but the file to which data would be appended doesn't
exist, it is created.
@param fileList The list of input files that were originally processed and which
may have contributed to each time series.
@param warning Multi-line warning string to accumulate processing errors.  If
the resulting string is longer than zero characters, the calling method will
print a warning.
*/
private void writeFile ( Hashtable ht, String outputDir, boolean append, List fileList, String warning )
{	String routine = "Fcst.writeFile";
	Enumeration en = ht.keys();
	IrregularTS its;
	String key;
	String filename;
	TSIdent tsident;
	TS ts;
	DayTS dts;
	int i = 0;
	int fileList_size = fileList.size();
	// "en" is an enumeration of all the keys in the hashtable.  It can 
	// be used with Enumeration's hasMoreElements() method to iterate
	// through the hashtable.
	while(en.hasMoreElements()) {
		// find the key stored in Enumeration's current element
		key = (String)en.nextElement();
		// use the key to get the IrregularTS out from the Hashtable
		its = (IrregularTS)ht.get(key);
		tsident = its.getIdentifier();
		// Create the filename for writing (or appending) the time
		// series by using the output directory and the time series
		// identifier...

		//Since we know that we are converting the TS from 
		//IRREGULAR to DAY, add the DAY interval to the
		//tsident instead of the Irregular
		//filename = outputDir + File.separator+tsident.toString();
		filename = outputDir + File.separator+tsident.toString();
		int ind = -99;
		ind = filename.indexOf("Irregular");
		if ( ind > 0 ) {
			filename = filename.substring(0, ind) + "Day";
		}

		
		// If append is set to true, then the DayTS time series
		// needs to first be filled with the current values stored
		// in the file.  The values stored in the hash table will
		// then be concatenated with these value and re-written back
		// out to the file.
		if ( append && IOUtil.fileExists(filename) ) {			
			// Appending and file exists so append...
			try {	ts = DateValueTS.readTimeSeries(filename);
			}
			catch ( Exception e ) {
				warning += "\nError reading " + tsident +
					" from \"" + filename + "\"";
				// Continue to the next time series...
				continue;
			}
			if ( ts == null ) {
				warning += "\nError reading " + tsident +
					" from \"" + filename + "\"";
				// Continue to the next time series...
				continue;
			}
			if ( ts.getDataIntervalBase() != TimeInterval.DAY ) {
				warning += "\nTime series " + tsident +
					" from \"" + filename +
					"\" is not daily data.  Skipping.";
				// Continue to the next time series...
				continue;
			}
			dts = (DayTS)ts;
			// Fill the existing time series with the new daily
			// data from the irregular time series...
			try {	dts = fillDayTS ( dts, its );
			}
			catch ( Exception e ) {
				warning += "\nError filling " + tsident +
					" from \"" + filename +
					"\" with new data";
				// Continue to the next time series...
				continue;
			}
			dts.addToGenesis(routine + " add to file from:");
		}
		else {	// File does not exist or not appending so create a new
			// output file...
			dts = buildDayTS ( its );
			dts.addToGenesis(routine + " created file from:");
		}

		// Add the list of files used as input to the process...

		for ( i = 0; i < fileList_size; i++ ) {
			dts.addToGenesis("   " + (String)fileList.get(i));
		}
		
		// Write the DayTS time series to a DateValue file...

		try {
			DateValueTS.writeTimeSeries ( dts, filename, null, null, null, true );
		}
		catch ( Exception e ) {
			warning += "\nError writing " + tsident + " to \"" +
			filename + "\"";
		}
	}
}

} // End Fcst
