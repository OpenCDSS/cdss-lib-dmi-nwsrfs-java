//-----------------------------------------------------------------------------
// NWSRFS_Util - class to store NWSRFS-related utility functions.
//-----------------------------------------------------------------------------
// History:
//
// 2003-01-24	J. Thomas Sapienza, RTi	Initial version, taken from SAM's 
//					C code.
// 2003-11-21	Steven A. Malers, RTi	Set the precision to hour for DateTime
//					determined from Julian hour.
// 2003-12-03	SAM, RTi		Add julda and mdyh1 methods ported
//					"exactly" from Fortran, due to seeing
//					differences in results between Fortran
//					code and the existing JulianHour1900
//					methods previously included in this
//					class.  Adding these methods required
//					pulling in a lot of other small routines
//					but do it consistent with NWSRFS so that
//					comparisons can be made.  Methods added
//					during this effort:
//					julda
// 2003-12-05	SAM, RTi		* Add toDateTime24().
// 2003-12-13	SAM, RTi		* Add toDateTime23().
// 2004-03-10	Scott Townsend, RTi	* Add get_apps_defaults().
// 2004-07-26	A. Morgan Love, RTi	* Moved from package: DMI.NWSRFS.
//					NWSRFSUtil.java to this package 
//					(DMI.NWSRFS_DMI) and renamed to:
//					NWSRFS_Util.java.
// 2004-09-01	Steven A. Malers, RTi	* Add getDataTypeIntervals(), similar
//					  to other DMI packages.
//					* Add getTimeSeriesDataTypes(), similar
//					  to other DMI packages.
//					* Clean up some code from NWSRFSUtil -
//					  not justified to left, etc. and
//					  added a few REVISITS for later.
// 2004-09-14	SAM, RTi		* Add a few more data types to
//					  getTimeSeriesDataTypes() for
//					  stand-alone (no Apps Defaults) use.
// 2004-10-06	SAT, RTi		Added keyFromAppsDefaults that was in
//					NwsrfsGUI_Util.java to read apps_defaults
//					tokens first before going to the 
//					environment and finally going to the
//					APPS_DEFAULTS files directly.
// 2004-10-06	SAT, RTi		Starting to migrate NwsrfsGUI_Util
//					methods to here to remove the dependance
//					of the DMI on application code! The
//					methods migrated are:
//						run_dump_station_or_area
//						runEditor
//						run_print_ratingCurves
//						run_delete_ratingCurves
//						run_print_cgs_or_fgs
//						run_print_segs
//						plotSelectedTimeSeries
// 2004-10-14	SAM, RTi		Overload getTimeSeriesDataTypes() to
//					handle preprocessor and processed
//					database types.
// 2004-11-02	SAM, RTi		* Deprecate plotSelectedTimeSeries().
//					* Add plotTimeSeries() as a copy of the
//					  above, but clean up the functionality
//					  and documentation.
//-----------------------------------------------------------------------------
// EndHeader

package RTi.DMI.NWSRFS_DMI;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import RTi.GRTS.TSViewJFrame;
import RTi.TS.TS;
import RTi.Util.IO.DataType;
import RTi.Util.IO.ProcessManager;
import RTi.Util.IO.PropList;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.LanguageTranslator;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeUtil;

/**
The NWSRFS_Util class stores NWSRFS-related utility functions.
*/
public class NWSRFS_Util {

/**
Used in logging.
*/
private static String _class = "NWSRFS_Util";

/**
The number of days from January 1, 01 to December 31, 1899
*/
public final static int JULIAN_1900_DAYS = 693960;
/**
The number of days that have come before each month of the year.
*/
public final static int[] monthYearDays = 
	{ 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334 };

/**
The single AppsDefaults instance used to get Apps Defaults.
*/
private static AppsDefaults __AppsDefaults = new AppsDefaults();

/**
NWSRFS routine to return the number of days in the month.
@return the number of days in the month.
@param Y1 4-digit year.
@param M1 month (1-12).
*/
private static int ddgcdm ( int Y1, int M1 )
{
/*
Ported from pamlico 2003-12-03
/awips/hydroapps/lx/rfc/nwsrfs/util/src/date_time/TEXT/ddgcdm.f
C  =====================================================================
C  pgm: DDGCDM ..  Get frm cal-dt, num-of-days in month
C
C  use:     CALL DDGCDM(Y1,M1,NODIM)
C
C   in: Y1 ...... 4-digit year number - INT
C   in: M1 ...... month number (01-12) - INT
C  out: NODIM ... number of days in month (19,28-31) - INT
C
C  lvl: DD1
C  =====================================================================
      SUBROUTINE DDGCDM(Y1,M1,NODIM)

      INTEGER    Y1,M1,NODIM,NODIMT(13),Y1T,M1T,NN
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /APN/util/src/date_time/RCS/ddgcdm.f,v $
     . $',                                                             '
     .$Id: ddgcdm.f,v 1.1 1998/07/06 $
     . $' /
C    ===================================================================
C
*/
      // Assign an extra integer at front to keep Fortran 1+ index notation...
      int [] NODIMT = { 0, 31,28,31,30,31,30,31,31,30,31,30,31,0 };

        int Y1T = Y1;
        int M1T = M1;

        if ( M1T < 1 || M1T > 12 ) M1T = 13;
        int NN = NODIMT[M1T];
        if (         M1T == 2
             &&      Y1T == (Y1T/4)*4
             &&      Y1T != 1800
             &&      Y1T != 1900
             &&    ( Y1T < 2100 || Y1T == (Y1T/400)*400
                                 || Y1T != (Y1T/100)*100 )
           ) NN = 29;
        if ( Y1T == 1752 && M1T == 9 ) NN = 19;

        int NODIM = NN;
	return NODIM;
}

/**
Return the number of days since Jan 1, 1900.
@return the number of days since Jan 1, 1900.
*/
private static int ddgcd2 ( int Y1, int M1, int D1 )
{
/*
Ported from pamlico 2003-12-03
/awips/hydroapps/lx/rfc/nwsrfs/util/src/date_time/TEXT/ddgcd2.f
C  =====================================================================
C  pgm: DDGCD2 .. Get frm cal-dt, da-sum (frm yr-1900)
C
C  use:     CALL DDGCD2(DU1,Y1,M1,D1)
C
C  out: DU1 ..... day-sum since Jan 1, 1900 - INT
C   in: Y1 ...... 4-digit year number (1900 plus) - INT
C   in: M1 ...... month number (01-12) - INT
C   in: D1 ...... day number (01-31) - INT
C
C  rqd: DDGJD2,DDGCJ
C
C  lvl: DD2
C  =====================================================================
      SUBROUTINE DDGCD2(DU1,Y1,M1,D1)

      EXTERNAL   DDGJD2,DDGCJ

      INTEGER    DU1,Y1,M1,D1,J1T
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /APN/util/src/date_time/RCS/ddgcd2.f,v $
     . $',                                                             '
     .$Id: ddgcd2.f,v 1.1 1998/07/02 $
     . $' /
C    ===================================================================
C

        CALL DDGCJ(J1T,Y1,M1,D1)
        CALL DDGJD2(DU1,J1T,Y1)
*/
	int J1T = ddgcj ( Y1, M1, D1 );
	int DU1 = ddgjd2 ( J1T, Y1 );

	return DU1;
}

/**
Return the day of the year.
@return the day of the year.
@param Y1 4-digit year.
@param M1 month (1-12).
@param D1 day of month (1-31).
*/
private static int ddgcj ( int Y1, int M1, int D1 )
{
/*
Ported from pamlico 2003-12-03
/awips/hydroapps/lx/rfc/nwsrfs/util/src/date_time/TEXT/ddgcj.f
C  =====================================================================
C  pgm: DDGCJ .. Get frm cal-dt, jul-dt (get julda)
C
C  use:     CALL DDGCJ(J1,Y1,M1,D1)
C
C  out: J1 ...... day of year (001-366) - INT
C   in: Y1 ...... 4-digit year number - INT
C   in: M1 ...... month number (01-12) - INT
C   in: D1 ...... day number (01-31) - INT
C
C  lvl: DD1
C  =====================================================================
      SUBROUTINE DDGCJ(J1,Y1,M1,D1)

      INTEGER    J1,Y1,M1,D1,J1T,Y1T,M1T,D1T,NODIYT(12)
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /APN/util/src/date_time/RCS/ddgcj.f,v $
     . $',                                                             '
     .$Id: ddgcj.f,v 1.2 1998/07/02 $
     . $' /
C    ===================================================================
C
*/
	// Add an extra integer at the front to preserve the Fortran 1+ index...
      int [] NODIYT = { 0, 0,31,59,90,120,151,181,212,243,273,304,334 };

        int Y1T = Y1;
        int M1T = M1;
        int D1T = D1;

        if ( M1T < 1 || M1T > 12 ) M1T = 1;
        int J1T = NODIYT[M1T] + D1T;
        if (         M1T > 2
             &&      Y1T == (Y1T/4)*4
             &&      Y1T != 1800
             &&      Y1T != 1900
             &&    ( Y1T < 2100 || Y1T == (Y1T/400)*400
                                 || Y1T != (Y1T/100)*100 )
           ) J1T=J1T + 1;
        if ( Y1T == 1752 && J1T > 246 ) J1T = J1T - 11;

        int J1 = J1T;

	return J1;
}

/**
Return the days since Jan 1, 1900.
@return the days since Jan 1, 1900.
@param J1 Day of year (1-366).
@param Y1 4-digit year.
*/
private static int ddgjd2 ( int J1, int Y1 )
{
/*
Ported from pamlico 2003-12-03
/awips/hydroapps/lx/rfc/nwsrfs/util/src/date_time/TEXT/ddgjd2.f
C  =====================================================================
C  pgm: DDGJD2 .. Get frm jul-dt, da-sum (frm yr-1900)
C
C  use:     CALL DDGJD2(DU1,J1,Y1)
C
C  out: DU1 ..... day-sum since Jan 1, 1900 - INT
C   in: J1 ...... day of year (001-366) - INT
C   in: Y1 ...... 4-digit year number (1900 plus) - INT
C
C  lvl: DD1
C  =====================================================================
      SUBROUTINE DDGJD2(DU1,J1,Y1)

      INTEGER    DU1,J1,Y1,DU1T,J1T,Y1T,YII,YJJ
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /APN/util/src/date_time/RCS/ddgjd2.f,v $
     . $',                                                             '
     .$Id: ddgjd2.f,v 1.1 1998/07/02 $
     . $' /
C    ===================================================================
C
*/

        int J1T = J1;
        int Y1T = Y1;

        int DU1T = 0;
        if ( Y1T >= 1900) {

          int YII = Y1T - 1900;
          if ( YII > 0 ) {
            DU1T = 365*YII + (YII-1)/4;
            if ( YII > 200 ) {
              int YJJ = YII - 101;
              DU1T = DU1T - YJJ/100 + YJJ/400;
	    }
	  }

          DU1T = DU1T + J1T;
        }

        int DU1 = DU1T;
	return DU1;
}

/**
Ported DDYCDL from NWSRFS to support NWSRFS date/time routines.
Return a 4-digit year given the year, month and day.
@return a 4-digit year given the year, month and day.
@param Y1 year to check 2 or 4 digits.
@param M1 month to check (1-12).
@param D1 day to check (1-31).
*/
private static int ddycdl ( int Y1, int M1, int D1 )
{
/*
Ported from pamlico 2003-12-03
/awips/hydroapps/lx/rfc/nwsrfs/util/src/date_time/TEXT/ddycdl.f
FORTRAN code...
C$PRAGMA C (DDRMCL)
C  =====================================================================
C  pgm: DDYCDL .. Updt yr for cal-dt-da by lcl 90/10
C
C  use:     CALL DDYCDL(Y1,M1,D1)
C
C  i/o: Y1 ...... 4-digit year number (may updt by lcl 90/10 rule) - INT
C  i/o:             (if input is 2-digits, it is converted to 4-digits)
C   in: M1 ...... month number (01-12) - INT
C   in: D1 ...... day number (01-31) - INT
C
C  rqd: DDRMCL
C
C  lvl: DD1
C
C  cmt: The century is determined by 90 yrs in past, 10 yrs in future.
C  =====================================================================
      SUBROUTINE DDYCDL(Y1,M1,D1)

cfan $pgf90 port 7/3/01      EXTERNAL   DDRMCL

      INTEGER    Y1,M1,D1,Y1T,M1T,D1T,YC,MC,DC,HC,NC,SC,DF,CC,AD
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /APN/util/src/date_time/RCS/ddycdl.f,v $
     . $',                                                             '
     .$Id: ddycdl.f,v 1.2 2002/02/11 $
     . $' /
C    ===================================================================
C
*/

        int Y1T = Y1;
        int M1T = M1;
        int D1T = D1;

        if ( Y1T >= 0 &&  Y1T <= 136 ) {
          //CALL DDRMCL(YC,MC,DC,HC,NC,SC)
	  DateTime dt = new DateTime();
	  int YC = dt.getYear();
	  int MC = dt.getMonth();
	  int DC = dt.getDay();

          int AD = 0;
          int CC = (YC/100)*100;
          YC = YC - CC;
          int DF = Y1T - YC;
          if ( Y1T >= 100 && CC > 1900                     ) {
              AD = -100;
	  }
          else if( Y1T < 100) {
            if     ( DF > 10                                 ) {
              AD = -100;
	    }
            else if ( DF == 10 && MC < M1T                 ) {
              AD = -100;
	    }
            else if ( DF == 10 && MC == M1T && DC < D1T ) {
              AD = -100;
	    }
            else if ( DF < -90                                 ) {
              AD =  100;
	    }
            else if ( DF == -90 && MC > M1T                 ) {
              AD =  100;
	    }
            else if ( DF == -90 && MC == M1T && DC > D1T ) {
              AD =  100;
	    }
	  }

          Y1 = Y1T + CC + AD;
	}

      return Y1;
}

/**
Returns a DateTime filled in with the year, month, day and hour calculated
from a Julian Hour.
@param julianHour the julian hour from which to create a DateTime.
*/
public static DateTime getDateFromJulianHour1900 (int julianHour)
{	int jDay = (julianHour / 24) + 1;
	int jHour = (julianHour % 24);

	// Compute year and check for leap year.  Note that there are 146097
	// days in 400 years -- exactly.  This accounts for leap year, plus
	// every 100th year not being a leap year unless it is a 400th year
	// as well.

	int year = ((jDay * 400) / 146097) + 1900;

	int id1 = 0;
	int leapYear = 0;
	while (true) {
		leapYear = 0;
		if (TimeUtil.isLeapYear(year)) {
			leapYear = 1;
		}
		
		// id1 is the Julian day for the guessed year, including days
		// in previous years but not the current year.
		//
		// id1 for julianHour 0 == 0, == 1/1/1900:0
		//
		//					diff from prev
		// Consider 1900, which gives id1 = 0		0
		//      and 1901, which gives id1 = 365		365
		//      and 1902, which gives id1 = 730		365
		//      and 1903, which gives id1 = 1095	365
		//      and 1904, which gives id1 = 1460	365
		//                 . . . . . . . . . .
		//      and 1995, which gives id1 = 34698
		//      and 1996, which gives id1 = 35063	365
	
		id1 = 	(365 * year) 
				+ (year / 4)
				- (year / 100)
				+ (year / 400)
				- JULIAN_1900_DAYS
				- leapYear;
		if (id1 < jDay) {
			// for the current year guessed, the total number of 
			// Julian days is less than that of the Julian year for
			// the requested Julian hour.  So, we are either in the 
			// correct year or one less than the correct year (with
			// too many days).
			break;
		} else {
			// We need to decrement the year.  id1 will be
			// recalculated.
			--year;
		}
	}
					
	// Guess at the correct total day for the year being the difference
	// between the initial Julian day calculated and the one calculated
	// for our guessed year.
	int day = jDay - id1;
	while (true) {
		if (day <= (365 + leapYear)) {
			// We are in the middle of the year and so our day is OK
			break;
		} else {
			// we need to increment the year
			++year;
			// since we are incrementing the year, the number of 
			// days left decreases by the number of days in the 
			// year that we had last ...
			day = day - 365 - leapYear;
		}
	}

	// Find the month

	int month = 0;
	if (day <= 31) {
		month = 1;
	}

	if (month <= 0) {
		boolean flag = false;
		for (int i = 3; i <= 12; i++) {
			month = i - 1;
			if (day <= (monthYearDays[i - 1] + leapYear)) {
				flag = true;
				break;
			}
		}
		if (flag == false) {
			month = 12;
		}
	}

	// Month known, computer day offset from month
	day -= monthYearDays[month - 1];
	if (month >= 3) {
		day -= leapYear;
	}

	DateTime d = new DateTime(DateTime.PRECISION_TIME_ZONE);
	d.setDay(day);
	d.setMonth(month);
	d.setYear(year);
	d.setHour(jHour);

	return d;
}

/**
Calculates a Julian day from a given month, day and year.  Julian Days start
at 1 on January 1, 1900, and count up from there (ie, 01/02/1900 == 2, 
01/03, 1900 == 3, etc).

@return the Julian Day value that represents the given date.  If the month, 
day and year don't parse to a valid date (ie, January 92, 2002), then 
the return value is -999.
*/
public static int getJulianDay1900FromDate (int month, int day, int year) 
throws Exception
{	int leapYear = 0;
	if (TimeUtil.isLeapYear(year)) {
		leapYear = 1;
	}

	// The following is rather odd-looking math, but it does the proper calculation.
	int julianDay = day + 
			TimeUtil.numDaysInMonths (1, year, (month - 1)) + 
			(year * 365) + 
			(year / 4) -
			(year / 100) +
			(year / 400) -
			JULIAN_1900_DAYS - 
			leapYear;

	return julianDay;
}

// TODO - are hours 0-23 or 1-24?
/**
Calculates a Julian Hour from a given date and returns it.  Julian hours
start at 1 on January 1, 1900 @ 0100, and go up from there (ie, 
01/01/1900@0200 == 2, 01/01/1900@0300 == 3, 01/02/1900@0100 == 25, etc).

@return the julian hour representing the given date and time.
*/
public static int getJulianHour1900FromDate (int month, int day, int year,int hour) 
throws Exception {
	// check to make sure the hour is valid
	if (hour > 24 || hour < 0) {
		throw new Exception ("Invalid hour in getJulianHour1900FromDate"
			+ ": " + hour);
	}

	// getJulianDay1900FromDate checks the validity of the rest of the
	// date values.
	int julianDay = NWSRFS_Util.getJulianDay1900FromDate(month, day, year);

	int julianHour = ((julianDay - 1) * 24) + hour;

	return julianHour;
}

/**
Return the valid data intervals for a time series data type.
Currently this is hard-coded to return possible hourly intervals.  In the
future, it may actually perform a query for data that are present in the
database files.
@return the valid data intervals for a time series data type.
*/
public static Vector getDataTypeIntervals ( NWSRFS_DMI dmi, String datatype )
{	// TODO SAM 2004-09-01 - need to determine if performance will
	// allow determining the intervals that are actually defined for the data type.
	Vector intervals = new Vector ( 8 );
	//intervals.addElement ( "*" );
	intervals.addElement ( "1Hour" );
	intervals.addElement ( "3Hour" );
	intervals.addElement ( "4Hour" );
	intervals.addElement ( "6Hour" );
	intervals.addElement ( "8Hour" );
	intervals.addElement ( "12Hour" );
	intervals.addElement ( "24Hour" );
	return intervals;
}

/**
Return the valid data types for NWSRFS FS5Files.
Currently this is hard-coded to return all data types in the DATATYPE system
file and preprocessor data types.  In the future, it may actually perform a
query for data that are present in the database files.
@return the valid data intervals for a time series data type.
@param dmi An open NWSRFS_DMI instance.
@param include_desc If true, include the data type description using the format "Type - Description".
*/
public static Vector getTimeSeriesDataTypes ( NWSRFS_DMI dmi, boolean include_desc )
{	return getTimeSeriesDataTypes ( dmi, include_desc,
					true,	// Preprocessor DB
					true );	// Processed DB
}

/**
Return the valid data types for NWSRFS FS5Files.
Currently this is hard-coded to return all data types in the DATATYPE system
file and preprocessor data types.  In the future, it may actually perform a
query for data that are present in the database files.
@return the valid data intervals for a time series data type.
@param dmi An open NWSRFS_DMI instance.
@param include_desc If true, include the data type description using the format "Type - Description".
@param include_preprocessor_db If true, time series data types for the
preprocessor database will be included in the returned list.  These data types,
if in conflict with the processed database, have a sub-datatype of "-PPDB".
@param include_preprocessor_db If true, time series data types for the
processed database will be included in the returned list.
*/
public static Vector getTimeSeriesDataTypes ( NWSRFS_DMI dmi, boolean include_desc,
						boolean include_preprocessor_db, boolean include_processed_db )
{	// TODO SAM 2004-09-01 - need to determine if performance will
	// allow determining the data types that are actually defined in the database.
	String routine = "NWSRFS_Util.getTimeSeriesDataTypes";
	Vector datatypes = new Vector ( 100 );
	int size = 0;
	if ( include_preprocessor_db ) {
		// The simplest way to add these is manually...
		// Items that are commented out are not yet supported in
		// NWSRFS_DMI.readTimeSeries() and can be uncommented later (see
		// the TSTool NWSRFS FS5Files Input Type appendix).
		//datatypes.addElement ( "APIG-PPDB - Grid Point API (PPDB)" );
		datatypes.addElement ( "*-PPDB - All Preprocessor Database Types (PPDB)" );
		datatypes.addElement ( "DQIN-PPDB - Diversion Instantaneous Flow (PPDB)" );
		datatypes.addElement ( "DQME-PPDB - Diversion Mean Flow (PPDB)");
		//datatypes.addElement ( "EA24-PPDB - Potential Evaporation (PPDB)" );
		//datatypes.addElement ( "MDR6-PPDB - Manually Digitized Radar (PPDB)" );
		datatypes.addElement ( "PELV-PPDB - Reservoir Pool (PPDB)" );
		//datatypes.addElement ( "PG24-PPDB - Grid Point 24-hour Precipitation (PPDB)" );
		//datatypes.addElement ( "PP01-PPDB - 1-hour Precipitation Accumulation (PPDB)" );
		//datatypes.addElement ( "PP03-PPDB - 3-hour Precipitation Accumulation (PPDB)" );
		//datatypes.addElement ( "PP06-PPDB - 6-hour Precipitation Accumulation (PPDB)" );
		//datatypes.addElement ( "PP24-PPDB - 24-hour Precipitation Accumulation (PPDB)" );
		//datatypes.addElement ( "PPSR-PPDB - Stranger Precipitation Reports (PPDB)" );
		//datatypes.addElement ( "PPST-PPDB - Satellite Precipitation Estimates (PPDB)" );
		//datatypes.addElement ( "PPVR-PPDB - Less Than 24-hour Precipitation (PPDB)" );
		datatypes.addElement( "QIN-PPDB - River Discharge (PPDB)" );
		datatypes.addElement( "QME-PPDB - River Discharge, Mean (PPDB)" );
		//datatypes.addElement( "RC24-PPDB - Reservoir capacity (PPDB)");
		//datatypes.addElement( "RP24-PPDB - Reservoir Pool (PPDB)");
		//datatypes.addElement( "RI24-PPDB - Reservoir Inflow (PPDB)");
		datatypes.addElement( "RQIM-PPDB - Reservoir Inflow, Mean (PPDB)" );
		datatypes.addElement( "RQIN-PPDB - Reservoir Inflow (PPDB)" );
		datatypes.addElement( "RQME-PPDB - Reservoir Outflow, Mean (PPDB)" );
		datatypes.addElement( "RQOT-PPDB - Reservoir Outflow (PPDB)" );
		datatypes.addElement( "RSTO-PPDB - Reservoir Storage (PPDB)" );
		datatypes.addElement( "SNOG-PPDB - Observed Cover Depth (PPDB)");
		datatypes.addElement( "SNWE-PPDB - Observed Snow Water Equivalent (PPDB)" );
		datatypes.addElement( "STG-PPDB - River Stage (PPDB)" );
		//datatypes.addElement ( "TA01-PPDB - Air Temperature (PPDB)");
		//datatypes.addElement ( "TA03-PPDB - Air Temperature (PPDB)");
		//datatypes.addElement ( "TA06-PPDB - Air Temperature (PPDB)");
		//datatypes.addElement ( "TA24-PPDB - Air Temperature (PPDB)");
		//datatypes.addElement ( "TAVR-PPDB - Air Temperature (PPDB)");
		//datatypes.addElement ( "TD24-PPDB - ?? (PPDB)");
		//datatypes.addElement ( "TF24-PPDB - Forecast Temperature (PPDB)");
		//datatypes.addElement ( "TFMN-PPDB - Forecast Minimum Temperature (PPDB)");
		//datatypes.addElement ( "TFMX-PPDB - Forecast Maximum Temperature (PPDB)");
		//datatypes.addElement ( "TM24-PPDB - 24-hour Max/Min Temperature (PPDB)");
		//datatypes.addElement ( "TN24-PPDB - Previous 24-hour Minimum Temperature (PPDB)");
		//datatypes.addElement ( "TX24-PPDB - Previous 24-hour Maximum Temperature (PPDB)");
		datatypes.addElement( "TWEL-PPDB - Tailwater Stage (PPDB)" );
		//datatypes.addElement ( "US24-PPDB - ?? (PPDB)");
		datatypes.addElement( "ZELV-PPDB - Freezing Level (PPDB)" );
	}
	if ( include_processed_db ) {
		// The DATATYPE file contains mainly processed database data
		// types.  Where preprocessor data types are included, they
		// match processed database and special care is taken above for preprocessor data types.
		try {
		    // This will place the data types in the global IOUtil.DataType space...
			// TODO SAM 2004-09-07 - need to initialize the data types once and not read here each time.
			dmi.readDataTypeList ();
			Vector v = DataType.getDataTypesData();
			if ( v != null ) {
				size = v.size();
			}
			Message.printStatus ( 2, routine, "Have " + size + " data types to list." );
			DataType dt = null;
			datatypes.addElement( "* - All Processed Database Types" );
			for ( int i = 0; i < size; i++ ) {
				dt = (DataType)v.elementAt(i);
				if ( include_desc ) {
					Message.printStatus ( 2, routine,
						"Data type is " + dt.getAbbreviation() + " - " + dt.getDescription() );
					datatypes.addElement ( dt.getAbbreviation() + " - " + dt.getDescription() );
				}
				else {
				    datatypes.addElement ( dt.getAbbreviation() );
				}
			}
		}
		catch ( Exception e ) {
			// TODO SAM 2004-09-01 will get an exception if apps defaults are NOT used.  Need to configure the
			// DATATYPE file in the TSTool.cfg for this case!
		}
		if ( size == 0 ) {
			// Unable to get data types from the DATATYPE FILE.  Add some common types.
			datatypes.addElement( "* - All Processed Database Types" );
			datatypes.addElement( "AESC - Areal Extent of Snow Cover" );
			datatypes.addElement( "AQME - River Discharge, Adjusted, Mean");
			datatypes.addElement( "DQIN - Diversion Instantaneous Flow" );
			datatypes.addElement( "DQME - Diversion Mean Flow" );
			datatypes.addElement( "FMAP - Future Mean Areal Precipitation");
			datatypes.addElement( "MAP - Mean Areal Precipitation");
			datatypes.addElement( "MAPX - Mean Areal Precipitation (Gridded)" );
			datatypes.addElement( "MAT - Mean Areal Temperature" );
			datatypes.addElement( "PELE - Reservoir Pool, Adjusted");
			datatypes.addElement( "PELV - Reservoir Pool" );
			datatypes.addElement( "PTPX - Point Precipitation" );
			datatypes.addElement( "QIN - River Discharge" );
			datatypes.addElement( "QINE - River Discharge, Adjusted" );
			datatypes.addElement( "QME - River Discharge, Mean" );
			datatypes.addElement( "RAIM - Rain + Melt" );
			datatypes.addElement( "RQIM - Reservoir Inflow, Mean" );
			datatypes.addElement( "RQIN - Reservoir Inflow" );
			datatypes.addElement( "RQME - Reservoir Outflow, Mean" );
			datatypes.addElement( "RQOT - Reservoir Outflow" );
			datatypes.addElement( "RSEL - Rain/Snow Elevation" );
			datatypes.addElement( "RSTE - Reservoir Storage, Adjusted" );
			datatypes.addElement( "RSTO - Reservoir Storage" );
			datatypes.addElement( "SASC - Simulated Snow Cover Areal Extent" );
			datatypes.addElement( "SDQI - Simulated Diversion Flow" );
			datatypes.addElement( "SDQM - Simulated Diversion Flow, Mean" );
			datatypes.addElement( "SNOG - Observed Cover Depth" );
			datatypes.addElement( "SNWE - Observed Snow Water Equivalent" );
			datatypes.addElement( "SPEL - Simulated Reservoir Pool" );
			datatypes.addElement( "SQIN - Simulated River Discharge" );
			datatypes.addElement( "SQME - Simulated River Discharge, Mean");
			datatypes.addElement( "SSTG - Simulated River Stage" );
			datatypes.addElement( "STG - River Stage" );
			datatypes.addElement( "SWE - Simulated Snow Water Equivalent" );
			datatypes.addElement( "TAIN - Air Temperature" );
			datatypes.addElement( "TAMN - Air Temperature, Minimum");
			datatypes.addElement( "TAMX - Air Temperature, Maximum");
			datatypes.addElement( "TAVG - Air Temperature, Mean" );
			datatypes.addElement( "TWEL - Tailwater Stage" );
			datatypes.addElement( "ZELV - Freezing Level" );
		}
	}
	
	// Remove the descriptions if not requested...

	if ( !include_desc ) {
		size = datatypes.size();
		for ( int i = 0; i < size; i++ ) {
			datatypes.setElementAt( StringUtil.getToken( (String)datatypes.elementAt(i), " ", 0, 0 ), i );
		}
	}

	// If preprocessor and processed database time series are being returned, sort...
	if ( include_preprocessor_db && include_processed_db ) {
		return StringUtil.sortStringList ( datatypes );
	}
	return datatypes;
}

/**
Convert from a date to Julian day and hour.  The Julian day and hour are as per
NWSRFS conventions, measured from Jan 1, 1900.  This version differs from the
NWS original in that time zone conversions cannot be done.  It is assumed that
the original date/time is in Z time and the result will be in Z-time Julian
day and hour (processed database time).  Time zone conversions should be done
external to this call.
@return an integer array containing the Julian day and Julian hour.
@param M Month of interest (1-12).
@param D Day of interest (1-31).
@param Y Year of interest (4 digits).
@param H Hour of interest (0-23 or 24).
*/
public static int [] julda ( int M, int D, int Y, int H )
{	// Default some arguments to bypass the time zone conversion...
	int	JDAY = 0,	// will be output
		INTHR = 0,	// will be output
		ITZ = 0,	// input - default to no time zone change
		IDSAV = 0;	// input - default to no time zone change
		//CODE = 0;	// input - default to no time zone change
	int	LOCAL = 0,	// local - default to same as ITZ for no time
		NLSTZ = 0;	// zone change.
	String routine = "NWSRFS_Util.julda";
/* Original NWS comments, from code taken from pamlico 2003-12-03:
/awips/hydroapps/lx/rfc/nwsrfs/ofs/src/shared_util/TEXT/julda.f
C MODULE JULDA
C-----------------------------------------------------------------------
C  ROUTINE JULDA CONVERTS FROM MONTH, DAY, YEAR, HOUR FOR A SPECIFIED
C  TIME ZONE TO INTERNAL CLOCK TIME
C          (JULIAN DAY RELATIVE TO JAN 1, 1900)
C-----------------------------------------------------------------------
      SUBROUTINE JULDA (JDAY,INTHR,M,D,Y,H,ITZ,IDSAV,CODE)

      EXTERNAL    DDYCDL,DDGCDM,DDGCD2,WARN

      INTEGER D,Y,H,CODE

      INCLUDE 'common/ionum'
      INCLUDE 'common/fdbug'
      INCLUDE 'common/fctime'
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /APN/ofs/src/shared_util/RCS/julda.f,v $
     . $',                                                             '
     .$Id: julda.f,v 1.1 1998/07/02 $
     . $' /
C    ===================================================================
C

C  J1 IS JULIAN DAY OF DEC 31,1899
      DATA INTL / 'INTL' /

      IF (ITRACE.GT.2) WRITE (IODBUG,*) ' **JULDA ENTERED'

C  CAN ONLY CONVERT TO INTERNAL TIME WHEN NLSTZ IS DEFINED
      IF ( (NLSTZ.LT.-12 .OR.  NLSTZ.GT.12)  .AND.
     *     (ITZ.GE.-12   .AND. ITZ.LE.12  )        ) THEN
        WRITE (IPR,40) CODE
40      FORMAT (1H0,10X,'**WARNING** JULDA UNABLE TO CONVERT ',
     *    'FROM INTERMAL TIME TO REQUESTED TIME ZONE ',A4,' BECAUSE' /
     *    11X,'VARIABLE NLSTZ IN COMMON BLOCK FCTIME IS OUTSIDE ',
     *    'THE RANGE -12 TO 12.')
        CALL WARN()
        ITZ=100
        IDSAV=0
        CODE=INTL
      ENDIF

C  REPLACE ARGUMENTS M,D,Y,H WITH IM,ID,IY, AND IH
*/
      int IY=Y;
      int IM=M;
      int ID=D;
      int IH=H;

      if (IM < 1)  IM=1;
      if (IM > 12) IM=12;
      if (ID < 1)  ID=1;

//C         Make sure the year is four digits using the 90/10 year rule
//C         Get number of days in month, NODIM

            //CALL DDYCDL(IY,IM,ID)
            //CALL DDGCDM(IY,IM,NODIM)
            ddycdl(IY,IM,ID);
            int NODIM = ddgcdm(IY,IM);

      if (ID > NODIM) ID=NODIM;
      if (IH > 24) IH=24;
      if (IH < 0)  IH=0;

//C  COMPUTE JULIAN DAY
      //CALL DDGCD2(JDAY,IY,IM,ID)
      JDAY = ddgcd2(IY,IM,ID);

/* Time zone conversions are not supported but need to compute INTHR
C  CONVERT IH TO INTERNAL TIME

C  IH IS IN TIME ZONE ITZ
C  INTERNAL CLOCK IS IN TIME ZONE (NLSTZ-LOCAL)
C  TIME ZONE DIFFERENCE BETWEEN THEM IS (NLSTZ-LOCAL)-ITZ
C  THEREFORE INTHR=IH+TIME ZONE DIFFERENCE
C                 =IH+NLSTZ-LOCAL-ITZ
C  FOR EXAMPLE, PROCESSED DATA FILE HOUR 1 IS 13Z
C      IN EST TIME ZONE, NLSTZ=-5
C      AND 8 AM EST IS HOUR 1 OF THE INTERNAL CLOCK
C      SO LOCAL=7
C      (NLSTZ-LOCAL)=-12 FOR THIS CASE WHICH IS THE TIME
C                        ZONE NUMBER OF THE TIME ZONE WHERE 13Z
C                        IS 1 O'CLOCK AM
*/

      INTHR=IH;
      if (ITZ >= -12 && ITZ < 12) {
        INTHR=IH+NLSTZ-LOCAL-ITZ;

//C  DAYLIGHT SAVINGS TIME CORRECTION
        if (IDSAV == 1) INTHR=INTHR-1;
      //ENDIF
	}

//C  DAY CORRECTION TO PUT INTHR IN THE RANGE 1-24
      int NDOFF=(INTHR-24)/24;
      if (INTHR > 0) NDOFF=INTHR/24;
	// Changed to below
      //IF (NDOFF.GT.0.AND.MOD(INTHR,24).EQ.0) NDOFF=NDOFF-1
      if ((NDOFF > 0) && ((INTHR%24) == 0)) NDOFF=NDOFF-1;
      JDAY=JDAY+NDOFF;
      INTHR=INTHR-NDOFF*24;

//C  CHECK IF ARGUMENTS WERE OUT OF RANGE AND RETURN
/* Reorganized below
      IF (M.EQ.IM.AND.D.EQ.ID.AND.Y.EQ.IY.AND.H.EQ.IH) GO TO 80
      IF (M.EQ.IM.AND.D.EQ.ID.AND.IY-Y.EQ.1900.AND.H.EQ.IH) GO TO 80
      IF (M.EQ.IM.AND.D.EQ.ID.AND.IY-Y.EQ.2000.AND.H.EQ.IH) GO TO 80
        WRITE (IPR,70) M,D,Y,H,IM,ID,IY,IH
70      FORMAT (1H0,10X,'**WARNING** JULDA CALLED WITH ',
     *    'ARGUMENTS OUT OF RANGE WERE RESET TO INDICATED VALUES.' /
     *    1H ,20X,5X,5HMONTH,7X,3HDAY,6X,4HYEAR,6X,4HHOUR /
     *    1H ,11X,9HAS CALLED,4I10/
     *    1H ,11X,9HRESET TO ,4I10/)
        CALL WARN()

80    IF (ITRACE.GT.2) WRITE (IODBUG,*) ' **EXIT JULDA'
*/

	if (	((M == IM) && (D == ID) && (Y == IY) && (H == IH)) ||
		((M == IM) && (D == ID) && (IY-Y == 1900) && (H == IH)) ||
		((M == IM) && (D == ID) && (IY-Y == 2000) && (H == IH)) ) {
		// All is OK.
	}
	else {	Message.printWarning ( 2, routine,
		"**WARNING** JULDA CALLED WITH ARGUMENTS OUT OF RANGE WERE " +
		"RESET TO INDICATED VALUES.\n" +
		"M, D, Y, H as called = " + M + ", " + D + ", " + Y +","+H+"\n"+
		"M, D, Y, H reset to  = " + IM + ", " + ID +", "+IY+","+IH );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 2, routine, " **EXIT JULDA" );
	}

      //RETURN
      //END
	int [] out = new int[2];
	out[0] = JDAY;
	out[1] = INTHR;
	return out;
}

/**
Convert a Julian day and hour to date/time.  No time zone conversions are done
so all data should be treated as Z-time with conversions occurring before or
after calling this method.
@return a DateTime corresponding to the Julian hour.  The hour will be 0-23 with
the day adjusted accordingly, even though the NWS uses hour 24.
@param JDAY Julian day = (JulianHour - TimeHour)/24 + 1.
@param INTHR Hour in the Julian day = JulianHour%24.
*/
public static DateTime mdyh1 ( int JDAY, int INTHR )
{
/* Original NWS comments, from code taken from pamlico 2003-12-03:
/awips/hydroapps/lx/rfc/nwsrfs/ofs/src/shared_util/TEXT/mdyh1.f
C MODULE MDYH1
C-----------------------------------------------------------------------
C  ROUTINE MDYH1 AND ENTRY MDYH2 CONVERT FROM THE INTERNAL CLOCK TIME
C          TO MONTH, DAY, YEAR, AND HOUR FOR A SPECIFIED TIME ZONE
C
C  FOR MDYH1 THE TIME ZONE IS SPECIFIED BY ITZ AND IDSAV
C  FOR MDYH2 THE TIME ZONE IS SPECIFIED BY CODE
C
C     ARGUMENT  IN/OUT  DESCRIPTION
C     --------  ------  ------------------------------------------------
C     JDAY       IN     JULIAN DAY (01JAN1900=1)
C     INTHR      IN     INTERNAL CLOCK HOUR
C     M          OUT    MONTH
C     D          OUT    DAY
C     Y          OUT    YEAR (4 DIGIT)
C     H          OUT    HOUR (1-24)
C     ITZ               TIME ZONE NUMBER.  IN FOR MDYH1,
C                                          OUT FOR MDYH2
C     IDSAV             DAYLIGHT SAVING SWITCH.
C                       IDSAV=1 FOR DAYLIGHT SAVINGS TIME (ANY
C                       OTHER VALUE IGNORED).  IN FOR MDYH1,
C                       OUT FOR MDYH2.
C     CODE              FOUR CHARACTER TIME ZONE CODE.
C                       IN FOR MDYH2, OUT FOR MDYH1.
C
      SUBROUTINE MDYH1 (JDAY,INTHR,M,D,Y,H,ITZ,IDSAV,CODE)

      INTEGER D,Y,H,DAYS(12),CODE

      INCLUDE 'common/ionum'
      INCLUDE 'common/fdbug'
      INCLUDE 'common/fctime'

      EQUIVALENCE (IJDAYT,XIJDAY),(IHT,XIH)
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /APN/ofs/src/shared_util/RCS/mdyh1.f,v $
     . $',                                                             '
     .$Id: mdyh1.f,v 1.2 1998/07/02 $
     . $' /
C    ===================================================================
C
*/

	String routine = "NWSRFS_Util.mdyh1";
	int Y = 0, M = 0, D = 0, H = 0;

	// Days at start of each month.
	// Add an extra 0 at the start to keep Fortran index notation.
      int [] DAYS = { 0, 0,31,59,90,120,151,181,212,243,273,304,334 };
      //DATA DEBG/4HMDYH/
//C  J1 IS THE JULIAN DAY OF DEC 31, 1899 RELATIVE TO JAN 1, 0AD
      //DATA J1/693960/,INTL/4HINTL/
	int J1 = 693960;
	String INTL = "INTL";	// Internal time zone = Z

      //IF (ITRACE.GT.2) WRITE (IODBUG,*) ' **MDYH1 ENTERED'
	if ( Message.isDebugOn ) {
		Message.printDebug ( 2, routine, " **MDYH1 ENTERED" );
	}

//C  GET TIME ZONE CODE
      //CALL FCTZC (ITZ,IDSAV,CODE)
	// Set to zero because no time zone conversions take place...
	int ITZ = 0, IDSAV = 0, NLSTZ = 0, LOCAL = 0;
	String CODE = INTL;

      //LDEBUG=0
      //IF (IFBUG(DEBG).EQ.1) LDEBUG=1

//      IF (LDEBUG.EQ.1) WRITE (IPR,40) JDAY,INTHR,M,D,H,Y,ITZ,IDSAV,CODE
//40    FORMAT (' JDAY,INTHR,M,D,H,Y,ITZ,IDSAV,CODE=',8I11,1X,A4)
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine,
		"JDAY=" + JDAY + ", INTHR=" + INTHR + ", M=" + M +
		", D=" + D + ", H=" + H + ", Y=" + Y + ", ITZ=" + ITZ +
		", IDSAV=" + IDSAV + ", CODE=" + CODE );
	}

//C  CAN ONLY CONVERT TO INTERNAL TIME WHEN NLSTZ IS UNDEFINED
//      IF (NLSTZ.GE.-12.AND.NLSTZ.LE.12) GO TO 60
//      IF (ITZ.LT.-12.OR.ITZ.GT.12) GO TO 60
      if ((NLSTZ >= -12 && NLSTZ <= 12) ||
      	(ITZ < -12 || ITZ > 12) ) {
		// Do nothing
	}
	else {
/*
      WRITE (IPR,50) CODE
50    FORMAT (1H0,10X,'**WARNING** MDYH1 UNABLE TO CONVERT ',
     *  'FROM INTERMAL TIME TO REQUESTED TIME ZONE ',A4,' BECAUSE' /
     *  ' TIME TO REQUESTED TIME ZONE ',A4,' BECAUSE' /
     *  11X,'VARIABLE NLSTZ IN COMMON BLOCK FCTIME IS OUTSIDE ',
     *  'THE RANGE -12 TO 12.')
      CALL WARN()
*/
	Message.printWarning ( 2, routine,
		"WARNING** MDYH1 UNABLE TO CONVERT " +
		"FROM INTERMAL TIME TO REQUESTED TIME ZONE " +CODE+" BECAUSE\n"+
		"TIME TO REQUESTED TIME ZONE " + CODE + " BECAUSE\n" +
		"VARIABLE NLSTZ IN COMMON BLOCK FCTIME IS OUTSIDE " +
		"THE RANGE -12 TO 12." );
      ITZ=100;
      IDSAV=0;
      CODE=INTL;
	}

//C  CONVERT INTERNAL CLOCK HOUR TO SELECTED TIME ZONE
//C  (SEE DESCRIPTION OF OPPOSITE CONVERSION IN ROUTINE JULDA1)
//60    IJDAY=JDAY
      int IJDAY=JDAY;
      int IH=INTHR;
      if (JDAY < 0) JDAY=0;
      if (INTHR < 0) INTHR=0;
      if (INTHR > 24) INTHR=24;
//      if (JDAY == IJDAY && INTHR == IH) GO TO 90
      if (JDAY == IJDAY && INTHR == IH) {
		// Put in to jump to 90 below
	}
	else {
	
/*
      WRITE (IPR,70) IJDAY,IH,JDAY,INTHR
70    FORMAT (1H0,10X,'**WARNING** MDYH1 CALLED WITH ',
     *  'ARGUMENTS OUT OF RANGE WERE RESET TO INDICATED VALUES.' /
     *  11X,'JULIAN DAY     HOUR'/7X,I11,7X,I4/7X,I11,7X,I4)
      IJDAYT=IJDAY
      IHT=IH
      WRITE (IPR,80) IJDAY,IH,XIJDAY,XIH
80    FORMAT (1H0,10X,'** JULIAN DAY AND HOUR INPUT IN A4 FORMAT ',
     *  A4,1X,A4/11X,'** JULIAN DAY AND HOUR INPUT IN G15.7 FORMAT ',
     *  G15.7,1X,G15.7)
      CALL WARN()
*/
//      WRITE (IPR,70) IJDAY,IH,JDAY,INTHR
	Message.printWarning ( 2, routine,
	"WARNING** MDYH1 CALLED WITH " +
	"ARGUMENTS OUT OF RANGE WERE RESET TO INDICATED VALUES.\n" +
	"JULIAN DAY=" + IJDAY + " HOUR=" + IH +
	"\nReset JULIAN DAY=" + JDAY + " HOUR=" + INTHR );
	// Not needed - in original code used to convert between int, real
      //int IJDAYT=IJDAY;
      //int IHT=IH;
	}

//90    H=INTHR
      H=INTHR;
//      IF (ITZ.LT.-12.OR.ITZ.GT.12) GO TO 100
      if (ITZ < -12 || ITZ > 12) {
	// Put in to cause jump to 100
	}
	else {
      H=INTHR-NLSTZ+LOCAL+ITZ;
      if (IDSAV == 1) H=H+1;
	}

//C  CORRECT VALUE OF H TO RANGE 1-24
//100   NDOFF=(H-24)/24
      int NDOFF=(H-24)/24;
      if (H > 0) NDOFF=H/24;
//    if (NDOFF > 0 && MOD(H,24).EQ.0) NDOFF=NDOFF-1;
      if (NDOFF > 0 && (H%24) == 0) NDOFF=NDOFF-1;
      int JD=JDAY+NDOFF;
      H=H-NDOFF*24;

/* Original code - unwind it below..
C  COMPUTE YEAR AND CHECK FOR LEAP YEAR.
C  NOTE THAT THERE ARE EXACTLY 146097 DAYS IN 400 YEARS
C  THIS ACCOUNTS FOR LEAP YEAR, PLUS EVERY 100-TH YEAR NOT
C  BEING A LEAP YEAR UNLESS IT A 400-TH YEAR AS WELL.
      Y=(JD*400)/146097+1900;
110   LEAP=1
      IF (MOD(Y,4).NE.0) LEAP=0
      IF (MOD(Y,100).EQ.0.AND.MOD(Y,400).NE.0) LEAP=0
      ID1=365*Y+Y/4-Y/100+Y/400-J1-LEAP
      IF (ID1.LT.JD) GO TO 120
         Y=Y-1
         GO TO 110
120   D=JD-ID1
130   LEAP=1
      IF (MOD(Y,4).NE.0) LEAP=0
      IF (MOD(Y,100).EQ.0.AND.MOD(Y,400).NE.0) LEAP=0
      IF (D.LE.365+LEAP) GO TO 140
      Y=Y+1
      D=D-365-LEAP
      GO TO 130

C  FIND MONTH
140   M=0
      IF (D.LE.31) M=1
      IF (M.GT.0) GO TO 160
      DO 150 I=3,12
         M=I-1
         IF (D.LE.(DAYS(I)+LEAP)) GO TO 160
150      CONTINUE
      M=12

C  MONTH KNOWN - COMPUTE DAY OFFSET FROM MONTH
160   D=D-DAYS(M)
*/

// The following code unwinds the Fortran by using while () loops and if
// statements to mimic the logic of the original GO TO statements.  The code
// is not optimized by matches the original logic relatively closely.

//C  COMPUTE YEAR AND CHECK FOR LEAP YEAR.
//C  NOTE THAT THERE ARE EXACTLY 146097 DAYS IN 400 YEARS
//C  THIS ACCOUNTS FOR LEAP YEAR, PLUS EVERY 100-TH YEAR NOT
//C  BEING A LEAP YEAR UNLESS IT A 400-TH YEAR AS WELL.
	Y=(JD*400)/146097+1900;
	int LEAP=1;
	int ID1 = 0;
	boolean break_flag = false;
	while ( true ) {	// 110
		//Message.printStatus ( 2, routine,
		//"In while Y=" + Y + " D=" + D +
		//" LEAP=" + LEAP + " ID1=" + ID1 );
		LEAP=1;
		if ((Y%4) != 0) LEAP=0;
		if ((Y%100) == 0 && (Y%400) != 0) LEAP=0;
		ID1=365*Y+Y/4-Y/100+Y/400-J1-LEAP;
		if (ID1 < JD)  {
			// Cause jump to 120 in original code...
		}
		else {
			Y=Y-1;
			continue;	// Jump to 110
		}
		// 120
		D=JD-ID1;
		while ( true ) {	// 130
			LEAP=1;
			if ((Y%4) != 0) LEAP=0;
			if ((Y%100) == 0 && (Y%400) != 0) LEAP=0;
			break_flag = false;
			if (D <= 365+LEAP) {
				break_flag = true;
				break;	// go to 140
			}
			Y=Y+1;
			D=D-365-LEAP;
		}
		if ( break_flag ) {
			break;
		}
	}

	//Message.printStatus ( 2, routine,
	//"Before finding month Y=" + Y + " D=" + D +
	//" LEAP=" + LEAP + " ID1=" + ID1 );

//C  FIND MONTH
	// 140
	M=0;
	if (D <=31) M=1;
	if (M > 0) {
		// Force jump to 160
	}
	else {	for ( int I=3; I <= 12; I++ ) {
			M=I-1;
			break_flag = false;
			if (D <= (DAYS[I]+LEAP)) {
				break_flag = true;
				break;	// go to 160
			}
		}
		if ( !break_flag ) {
			M=12;
		}
	}

	// 160

//C  MONTH KNOWN - COMPUTE DAY OFFSET FROM MONTH
	//Message.printStatus ( 2, routine,
	//"After finding month Y=" + Y + " D=" + D +
	//" LEAP=" + LEAP + " M=" + M );
      D=D-DAYS[M];

      if (M >= 3) D=D-LEAP;

//      IF (LDEBUG.EQ.1) WRITE (IPR,40) JDAY,INTHR,M,D,H,Y,ITZ,IDSAV,CODE
//      IF (LDEBUG.EQ.1) WRITE (IPR,180) LEAP,NDOFF,ID1,JD,J1
//180   FORMAT (' LEAP,NDOFF,ID1,JD,J1=',5I11)
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine,
		"JDAY=" + JDAY + ", INTHR=" + INTHR + ", M=" + M +
		", D=" + D + ", H=" + H + ", Y=" + Y + ", ITZ=" + ITZ +
		", IDSAV=" + IDSAV + ", CODE=" + CODE );
	}

	if ( Message.isDebugOn ) {
      		Message.printDebug ( 2, routine, " **EXIT MDYH1" );
	}

	DateTime date = new DateTime ( DateTime.PRECISION_HOUR | DateTime.DATE_FAST );
	date.setYear ( Y );
	date.setMonth ( M );
	date.setDay ( D );
	if ( H == 24 ) {
		// Set to hour 0 of the next day...
		date.setHour ( 0 );
		date.addDay ( 1 );
	}
	else {	date.setHour ( H );
	}
	return date;
}

/**
Return a DateTime that has hours 0-23, using as input a date/time that uses
hours 0-24.  A copy of the supplied DateTime is first
made.  If the hour is 24, then the hour is set to 0 and the day is increased by 1.
@param datetime a DateTime instance to copy and evaluate.
@param always_copy If true, always create a copy, even if no adjustment is
made.  If false, only create a copy if an adjustment is required.
*/
public static DateTime toDateTime23 ( DateTime datetime, boolean always_copy )
{	if ( datetime.getHour() == 24 ) {
		// Need convert hour 24 of the current day to hour 0 of the
		// next day, handling changes to month as usual...
		DateTime dt = new DateTime ( datetime );
		dt.setHour ( 0 );
		dt.addDay ( 1 );
		return dt;
	}
	else {	// No change needed.
		if ( always_copy ) {
			// Always make a copy...
			return new DateTime ( datetime );
		}
		else {
            // Return the instance that was passed in...
			return datetime;
		}
	}
}

/**
Return a DateTime that has hours 1-24, using as input a date/time that uses
hours 0-23.  A copy of the supplied DateTime is first
made.  If the hour is 0, then the day is decreased by 1 and the hour is set to
24.  The resulting DateTime should not be used for iterations or other
purposes, but may be used for output or to save date information.
@param datetime a DateTime instance to copy and evaluate.
@param always_copy If true, always create a copy, even if no adjustment is
made.  If false, only create a copy if an adjustment is required.
*/
public static DateTime toDateTime24 ( DateTime datetime, boolean always_copy )
{	if ( datetime.getHour() == 0 ) {
		// Need convert hour 0 of the current day to hour 24 of the
		// previous day, handling changes to month as usual.
        // DO NOT use strict DateTime because hour 24 is not normally accepted.
		DateTime dt = new DateTime ( datetime, DateTime.DATE_FAST );
		dt.addDay ( -1 );
		dt.setHour ( 24 );
		return dt;
	}
	else {
        // No change needed.
		if ( always_copy ) {
			// Always make a copy...
			return new DateTime ( datetime, DateTime.DATE_FAST );
		}
		else {
            // Return the instance that was passed in...
			return datetime;
		}
	}
}

/** 
Resolve the value of an NWSRFS apps-defaults requested string.
	
<pre> 
The requested string to be resolved is supplied as the string
variable <request>, the resolved request is returned as the string
variable <reply>.
 
Request resolution occurs in one of three ways:
 
1. an environment variable matching in name to <request> is found;
<reply>  is then the value of that environment variable,
 
2. <request> is found as a match in a file that establishes
token - resource (t-r) relationships.  Three files may be scanned in
this order:
	APPS_DEFAULTS_USER ..... a personal user's set of tokens (typically $HOME/.Apps_defaults)
	APPS_DEFAULTS_PROG ..... a program specific set of tokens
	APPS_DEFAULTS_SITE ..... a site wide set of tokens
	APPS_DEFAULTS .......... a system-wide (national) set of tokens
		to find the first token match to get a request.

3. if <request> can not be resolved, <reply> is assigned as the null string.

Each file is scanned from top to bottom looking for the first match
between <request> and a defined token.  The syntax needed in either file is:
 
	<token> <delimiter> <resource>
 
where:
	<token> is defined as a string delimited by white space or <delimiter>,
	<delimiter>  is the : (colon),
	<resource> is any string, the value returned depends on certain file
	conventions:

	1. A valid t-r requires a valid token followed by a valid resource,
	2. the t-r relationship must be contained on a single line,
	3. no white space needs to surround <delimiter>,
	4. comments are indicated by a #,
	5. neither <token> nor <resource> can begin with a # or :,
	6. a # or a : can be embedded within <resource>,
	7. <resource> can contain white space if it is bounded by the ' or "
	   characters,
	8. blank lines are allowed in the file,
	9. referbacks are indicated by $(...). The '...' is resolved
		the same way any other token is, and is substituted for
		the $(...) string to compose the final resource value.
	10. Multiple referbacks are allowed in <resource>, but embedded
		referbacks are not allowed (i.e. no $($(...)) allowed).
	11. First in wins.  That is, first finding of <token>
		matching <request> uses that resource value, even if null.

A sample of a t-r file:
#-----------------------------------------------------------------------
#  This is a comment line; so was previous line. Blank lines are
#   intentional and are allowed in file.

ofs_level     : testcase	# this is a comment on valid t-r
ofs_reor_lvl  : test:reor	# ':' allowed in body of <resource>
ofs_inpt_grp  : "test  case"	# white space allowed in <resource>

ofs_file_grp  : /home/$(ofs_level)/files # referback to prior token;
				# returned resource will be
				#  /home/testcase/files

ofs_xxx       xxx		# invalid t-r, no delimiter
ofs_yyy    : #yyy		# invalid t-r, no resource
</pre> 

@param request is the requested apps-defaults String token to find.
@return the value of the apps-defaults String token if found or null if not found.
*/
public static String getAppsDefaults ( String request )
{
    return __AppsDefaults.getToken( request );
}
	
/** 
Resolve the value of an NWSRFS apps-defaults requested string.
	
<pre> 
The requested string to be resolved is supplied as the string
variable <request>, the resolved request is returned as the string
variable <reply>.
 
Request resolution occurs in one of three ways:
 
1. an environment variable matching in name to <request> is found;
<reply>  is then the value of that environment variable,
 
2. <request> is found as a match in a file that establishes
token - resource (t-r) relationships.  Three files may be scanned in
this order:
	APPS_DEFAULTS_USER ..... a personal users set of tokens
	APPS_DEFAULTS_PROG ..... a program specific set of tokens
	APPS_DEFAULTS_SITE ..... a site wide set of tokens
	APPS_DEFAULTS .......... a system-wide (national) set of tokens
		to find the first token match to get a request.

3. if <request> can not be resolved, <reply> is assigned as the null string.

Each file is scanned from top to bottom looking for the first match
between <request> and a defined token.  The syntax needed in either file is:
 
	<token> <delimiter> <resource>
 
where:
	<token> is defined as a string delimited by white space or <delimiter>,
	<delimiter>  is the : (colon),
	<resource> is any string, the value returned depends on certain file
	conventions:

	1. A valid t-r requires a valid token followed by a valid resource,
	2. the t-r relationship must be contained on a single line,
	3. no white space needs to surround <delimiter>,
	4. comments are indicated by a #,
	5. neither <token> nor <resource> can begin with a # or :,
	6. a # or a : can be embedded within <resource>,
	7. <resource> can contain white space if it is bounded by the ' or "
	   characters,
	8. blank lines are allowed in the file,
	9. referbacks are indicated by $(...). The '...' is resolved
		the same way any other token is, and is substituted for
		the $(...) string to compose the final resource value.
	10. Multiple referbacks are allowed in <resource>, but embedded
		referbacks are not allowed (i.e. no $($(...)) allowed).
	11. First in wins.  That is, first finding of <token>
		matching <request> uses that resource value, even if null.

A sample of a t-r file:
#-----------------------------------------------------------------------
#  This is a comment line; so was previous line. Blank lines are
#   intentional and are allowed in file.

ofs_level     : testcase	# this is a comment on valid t-r
ofs_reor_lvl  : test:reor	# ':' allowed in body of <resource>
ofs_inpt_grp  : "test  case"	# white space allowed in <resource>

ofs_file_grp  : /home/$(ofs_level)/files # referback to prior token;
				# returned resource will be
				#  /home/testcase/files

ofs_xxx       xxx		# invalid t-r, no delimiter
ofs_yyy    : #yyy		# invalid t-r, no resource
</pre> 

@param request is the requested apps-defaults String token to find.
@return is the value of the apps-defaults String token if found or null.
@deprecated Use getAppsDefaults().
*/
public static String get_apps_defaults(String request)
{
	int appDFileIndex;
	String[] appDFile;
	String appDFileValue = null;
	String requestValue  = null;

	// Load the appDFile environment variable tokens for the various
	// app-defaults files to pull from the users enviornment.
	appDFile = new String[4];
	appDFile[0] = "APPS_DEFAULTS_USER";	// apps-defaults personal file
	appDFile[1] = "APPS_DEFAULTS_PROG";	// apps-defaults for specific program
	appDFile[2] = "APPS_DEFAULTS_SITE";	// apps-defaults for local site	file
	appDFile[3] = "APPS_DEFAULTS";		// apps-defaults default file

	// Check to see if this is a UNIX/Linux machine. If not return null.
	if ( !IOUtil.isUNIXMachine() ) {
		// This is not UNIX/Linux
		return null;
	}
	
	// Calls the program "get_apps_defaults" first from the system to see
	// if it can get the value then if it has problems try the environment.
	// If it still can not find a value it tries to look at the APPS_DEFAULTS
	// files directly.
	if((requestValue = keyFromAppsDefaults(request)) != null)
	{
		return requestValue;
	}
	else if((requestValue = getenv(request)) != null)
	{
		// Now check the environment for the request token. If it is the
		// environment then return otherwise continue.
		return requestValue;
	}
	else // Now check app-defaults files directly.
	{
		// Loop through the app-defaults files  
		for(appDFileIndex=0;appDFileIndex < 4;appDFileIndex++)
		{
			if((appDFileValue = getenv(appDFile[appDFileIndex]))!= null)
			{
				// Now check to see if the token value is in the
				// app-defaults file if so we return it.
				if((requestValue = get_token(request,appDFileValue)) != null)
				{
					return requestValue;
				}
			}
		}
	}

	// If have not returned by here then the request was not found in the
	// environment or any of the apps-defaults files. So return a null
	// String
	return null;
}

/**
Runs 'get_apps_defaults' using the ProcessManager with the token passed 
in and returns the results.
@param token  apps defaults token to run.
@return  Value returned from running "get_apps_defaults" with the token
passed in.  If nothing is returned from the get_apps_defaults command, 
the method will return null.
@deprecated Use getAppsDefaults().
*/
public static String keyFromAppsDefaults( String token )
{
	return __AppsDefaults.getToken( token );
	/* FIXME SAM 2008-01-07 Rely on the AppsDefaults class and evaluate whether 
	 * old RTi code can be phased out
	//string to return	
	String result = null;

	String cmd = "get_apps_defaults " + token;
	
	if ( Message.isDebugOn ) {
		Message.printDebug( 15, routine, "Command to run: \"" + cmd + "\"." );
	}

	//set up process manager to run it...
	Vector v = null;
	int exitstat = -99;
	try {
        ProcessManager pm = new ProcessManager( cmd );
        pm.setCommandInterpreter(null);
		pm.saveOutput( true );
		pm.run();
		v = pm.getOutputVector();
		//v = pm.runUntilFinished();
		exitstat = pm.getExitStatus();
		if (( exitstat == 0 ) && ( v != null ) && ( v.size() > 0 )) {
			//then command ran successfully
			result = (String)v.elementAt( 0 );
			if ( Message.isDebugOn ) {
				Message.printDebug( 25, routine, "Value returned from running: \"" + cmd + "\" is: \"" + result + "\"." );
			}
		}
		else {
			//there was an error running the command.
			Message.printWarning( 3, routine,
			"Unable to run command: \"" + cmd +	"\" successfully.  Please check the apps defaults.");
			//returns null
		}
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, 	
		"Unable to run command: \"" + cmd + "\" successfully.  Please check the apps defaults.");
		Message.printWarning( 3, routine, e );
	}
	//clean up
	v = null;
	
	return result;
	*/
}

// TODO SAM 2004-09-01 the PropList code works on the Apps Defaults format?
/**
Search the supplied apps-defaults file for the given request token. This
method needs to be finished to allow for referback variables (tokens) in an
apps-defaults token but currently it will not do that.
@param request is the request string to search the apps-defaults file.
@param appsDefaultsfile is the apps-defaults file to do the search.
@return is the value of the apps-defaults String token if found or null.
*/
private static String get_token(String request, String appsDefaultsFile)
{
	String routine = "NWSRFS_Util.get_token";
	String delim = ":";	/* delimiter character */
	char comment = '#';	/* comment character */
	String line = null;
	String checkString = null;
	Vector parseString;
	FileInputStream appDFS;
	InputStreamReader appDISR;
	BufferedReader appDBR;
	boolean isDone = false;

	try
	{
		// Open streams
		appDFS = new FileInputStream(appsDefaultsFile);
		appDISR = new InputStreamReader(appDFS);
		appDBR = new BufferedReader(appDISR);

		// Loop through the file looking for the request string
		while((line = appDBR.readLine()) != null)
		{ 
			// Remove beginning and ending white space
			line = line.trim();

			// Check for blank line
			if(line.length() == 0)
				continue;

			// Start to parse the line
			if(line.charAt(0) == comment)
				continue;

			parseString = StringUtil.breakStringList(line,delim,3);

			// If the line does not contain a deliminter continue;
			if(parseString.size() < 2)
				continue;

			// Now check the parseString string list for the
			// request string.
			checkString = ((String)parseString.elementAt(0)).trim();
			if(checkString.equalsIgnoreCase(request))
			{
				checkString =
				((String)parseString.elementAt(1)).trim();
				
				// If the token has a comment before it
				// continue.
				if(checkString.charAt(0) == '#')
					continue;

				parseString =
				StringUtil.breakStringList(checkString," ",3);
				checkString = ((String)
					parseString.elementAt(0)).trim();
				isDone = true;
				break;
			}
		}

		// Clean up
		appDBR.close();

		// Now if we are done return the String
		if(isDone)
			return checkString;
	}
	// TODO SAM 2004-09-01 - no need to catch separately and not much
	// value added with the message.
	catch(EOFException EOFe)
	{
		Message.printWarning(2,routine, "An Exception occured: "+EOFe.getMessage());
		return null;
	}
	catch(FileNotFoundException FNFe)
	{
		Message.printWarning(2,routine, "An Exception occured: "+FNFe.getMessage());
		return null;
	}
	catch(SecurityException SEe)
	{
		Message.printWarning(2,routine, "An Exception occured: "+SEe.getMessage());
		return null;
	}
	catch(IOException IOe)
	{
		Message.printWarning(2,routine,
		"An Exception occured: "+IOe.getMessage());
		return null;
	}

	// If we get here we return null
	return null;
}

// TODO SAM 2004-09-01 This should be in IOUtil, etc., if it is useful
/**
This method is a replacement method for the System.getenv, which was deprecated in Java 1.4.2 (back in 1.5).
On UNIX, "env" is used to get the environment.  On Windows, "set" is used.
@param request is the request string to search the users environment.
@return is the value of the return String token if found or null.
*/
public static String getenv(String request)
{
	int i, exitstat = -999;
	String routine = "NWSRFS_Util.getenv";
	int dl = 1;    // Debug level - probably want to see this because it may be config-related.
	String env_val = null;
	String cmd = null;
	Vector value_list = new Vector();
	ProcessManager pm;
	
	if ( Message.isDebugOn ) {
	    Message.printDebug(dl, routine, "Trying to find value for environment variable \"" + request + "\"");
	}

	// Try to catch NullPointerExceptions
	try {
		// Check to see if the request String is in the System
		// properties if so return it.
		if((env_val = System.getProperty(request)) != null)
		{
		    if ( Message.isDebugOn ) {
		        Message.printDebug(dl, routine, "Found value of \"" + request +
					"\"  = \"" + env_val + "\" in system properties." );
		    }
			return env_val;
		}
		else
		{   // Have to make a OS call to get the environment variable value.
			if (IOUtil.isUNIXMachine())
			{
			    cmd = "env";
			}
			else {
			    cmd = "set";
			}
			try	{
				pm = new ProcessManager(cmd);
				pm.saveOutput( true );
				pm.run();
				value_list = pm.getOutputVector();
				exitstat = pm.getExitStatus();
	        }
            catch (Exception e ) 
            {
                Message.printWarning(3,routine, "An exception occurred running \"" + cmd +
                        "\" getting env var \"" + request + "\"" );
                Message.printWarning(3, routine, e);
                return null;
            }
			if ( exitstat != 0 )
			{
			    Message.printWarning(3,routine, "Exit status for \"" + cmd + "\" is " + exitstat
			        + ".  Returning null for env var \"" + request + "\"" );
				return null;
			}
			int size = value_list.size();
	        if ( size == 0 )
            {
                Message.printWarning(3,routine, "Size of environment list is " + value_list.size() +
                    ".  Returning null for env var \"" + request + "\"" );
                return null;
            }
			// Try to parse the strings returned for the environment
	        Vector env_var_tokens;
	        String env_var;
	        for ( i=0; i<size; i++ ) {
	            if ( Message.isDebugOn ) {
	                Message.printDebug(dl, routine, "Env var " + (String)value_list.get(i) );
	            }
	            env_var_tokens = StringUtil.breakStringList(
	                    (String)value_list.get(i),"=",StringUtil.DELIM_ALLOW_STRINGS);
	            env_var = ((String)env_var_tokens.elementAt(0)).trim();
	            if ( env_var.equalsIgnoreCase(request) ) {
	                if ( env_var_tokens.size() < 2 ) {
	                    // No value on the end (why would this happen?)
	                    return null;
	                }
	                else {
        	            env_val = ((String)env_var_tokens.elementAt(1)).trim();
        	            if ( Message.isDebugOn ) {
        	                Message.printDebug(dl, routine, "Environment variable \"" + request +
        				        "\"=\"" + env_val + "\"");
        	            }
        				return env_val;
	                }
	            }
			}
	        // No match was found...
	        if ( Message.isDebugOn ) {
	            Message.printDebug(dl, routine, "No match was found for environment variable \"" + request + "\"");
	        }
	        return null;
		}
	}
	catch(NullPointerException NPe)
	{
		Message.printWarning(3,routine, "An exception occurred getting environment variable \"" + request + "\"" );
		Message.printWarning(3, routine, NPe );
		return null;
	}
}

static Vector _translated_lines = null;

static String _output_dir = null;

//format dates
//for OFS files
static SimpleDateFormat _nwsrfs_date_formatter = new SimpleDateFormat("MMdd/yyyy/" );

//for user viewing
static SimpleDateFormat _reg_date_formatter = new SimpleDateFormat(	"dd/MM/yyyy");

//Date format to match the date format used to
//compose the name of xmrg files:  yyyyMMddHH
//Sample xrmg file name: xmrg2002121817z
static SimpleDateFormat _xmrg_date_formatter = new SimpleDateFormat("yyyyMMddHH");

//This hold a single line out output from running the
//nwsrfssh codates $CARRYOVERGROUP command.  The line is
//in the format: "2000-01-23 07:00 EST"  
//The line is preserved so that the HOUR and TIME ZONE info can be used. 
static String _time_info = null;

//Get operating System
static String _os = System.getProperty( "os.name" );

/**
Makes a copy of DEFNEWRC.GUI named DEFNEWRC.GUI.tmp and then
moves DEFNEWRC.GUI.tmp to NEWRC.GUI.
@return Name with path of NEWRC.GUI file.
*/
public static String copy_addRC_to_newRC() {
	String routine = _class + ".copy_addRC_to_newRC";

	//String for filename to return
	String newRC_path = null;

	//file paths
	String addRC_path = null;
	addRC_path = IOUtil.getPropValue( "DEFNEWRC.GUI" );
	if( (  addRC_path == null ) ||
		( !IOUtil.fileExists( addRC_path )) ) {
		Message.printWarning( 2, routine, 
		"Unable to find file: \"DEFNEWRC.GUI\". " +
		"Path retreived is: \"" + addRC_path + "\"." );

		return "";
	}
	//path to NEWRC.GUI is the same
	//path to DEFNEWRC.GU - only the file
	//name needs changed.
	String full_path = addRC_path;
	String base_path =null;
	int index=-999;
	index = full_path.indexOf("DEFNEWRC");
	if ( index > 0 ) {
		base_path = full_path.substring( 0, index );
	}
	if( ( base_path != null ) && ( IOUtil.fileExists( base_path ) ) ) {
		newRC_path = base_path + "NEWRC.GUI";

		//make copy of DEFNEWRC.GUI named DEFNEWRC.GUI.tmp
		//move DEFNEWRC.GUI.tmp to NEWRC.GUI
	
		//model file input and output
		//addRCFile is DEFNEWRC.GUI
		File addRCFile = null;
		//tmpFile in this case is DEFNEWRC.GUI.tmp
		File tmpFile = null;
		//newFile is NEWRC.GUI
		File newRCFile = null;
		
		FileInputStream fis;
		InputStreamReader isr;
		BufferedReader br;
		FileOutputStream fos;
		PrintWriter pw;
		
		String s = null;
		//addRCFile is DEFNEWRC.GUI
		addRCFile = new File( addRC_path );
		//tmpFile in this case is DEFNEWRC.GUI.tmp
		tmpFile = new File( addRC_path + ".tmp" );
		//newFile is NEWRC.GUI
		newRCFile = new File( newRC_path );
		
		//if the file is a readable file
		if ( addRCFile.canRead() ) {
			try {
				fis = new FileInputStream( addRCFile );
				isr = new InputStreamReader ( fis );
				br = new BufferedReader ( isr );

				fos = new FileOutputStream( tmpFile );
				pw =  new PrintWriter( fos );
				
				do {
					s = br.readLine();
					pw.println(s );	
					pw.flush(); 
					if ( s == null ) { 
						//no more lines break; 
						break;
					}
				} 
				while ( s != null );
			
				br.close();
				pw.close();	

				//clean up
				fis = null;
				isr = null;
				br = null;

			}	
			catch (Exception e ) {
				//use debug here, not warning
				//b/c prints this message even
				//when does edit the file
				if (Message.isDebugOn) {
					Message.printDebug( 25, 
					routine,
					"Unable to open: \"" +
					addRC_path + "\" for manipulation." );

					Message.printWarning( 2, routine, e);
				}
			}
			//now we have created an extra file so 
			//move DEFNEWRC.GUI.tmp to NEWRC.GUI
			try {
				tmpFile.renameTo( newRCFile );
			}		
			catch ( Exception e ) {

				Message.printWarning( 2, routine,
					"Unable to update " +
					"\"NEWRC.GUI\"." );

				if ( Message.isDebugOn ) {
					Message.printDebug( 2, routine,
					"file: \"" + addRC_path + 
					".tmp\" could not " +
					"be moved to: \"" + 
					newRC_path + "\"." );

					Message.printWarning( 2, routine, e);
				}
			}
		} //end if fileReadable
		else {
			Message.printWarning( 2, routine,
			"Unable to update \"NEWRC.GUI\"." );
		}

		//clean up
		
		fos = null;
		pw = null;
		s = null;
		tmpFile = null;
		addRCFile = null;
		newRCFile = null;
	}
	else {
		Message.printWarning( 2, routine,
		"Unable to create NEWRC.GUI file for " +
		"editing to create a New Rating Curve." );
		newRC_path = null;
	}

	return newRC_path;

} //end copy_addRC_to_newRC


/**
Makes a copy of ADDSTATION.GUI named ADDSTATION.GUI.tmp and then
moves ADDSTATION.GUI.tmp to NEWSTATION.GUI.
@return  boolean indicating if the copying and moving all went successfully.
*/
public static boolean copy_addStn_to_newStn() {
	String routine = _class + ".copy_addStn_to_newStn";

	//boolean to indicate if files could be moved.
	boolean file_moved = true;

	//file paths
	String addStn_path = null;
	String newStn_path = null;
	addStn_path = IOUtil.getPropValue( "ADDSTATION.GUI" );
	newStn_path = IOUtil.getPropValue( "NEWSTATION.GUI" );
	if( ( addStn_path == null ) || ( !IOUtil.fileExists( addStn_path )) ) {
		Message.printWarning( 2, routine, 
		"Unable to find path for \"ADDSTATION.GUI\". " +
		"Path retreived is: \"" + addStn_path + "\"." );

		file_moved = false;
	}
	if ( ( newStn_path == null ) || 
		( !IOUtil.fileExists( newStn_path )) ) {
		Message.printWarning( 2, routine, 
		"Unable to find path for \"NEWSTATION.GUI\". " +
		"Path retreived is: \"" + newStn_path + "\"." );

		file_moved = false;
	}

	if ( file_moved ) {
		//make copy of ADDSTATION.GUI named ADDSTATION.GUI.tmp
		//move ADDSTATION.GUI.tmp to NEWSTATION.GUI
	
		//model file input and output
		//addStnFile is ADDSTATION.GUI
		File addStnFile = null;
		//tmpFile in this case is ADDSTATION.GUI.tmp
		File tmpFile = null;
		//newFile is NEWSTATION.GUI
		File newStnFile = null;
		
		FileInputStream fis;
		InputStreamReader isr;
		BufferedReader br;
		FileOutputStream fos;
		PrintWriter pw;
		
		String s = null;
		//addStnFile is ADDSTATION.GUI
		addStnFile = new File( addStn_path );
		//tmpFile in this case is ADDSTATION.GUI.tmp
		tmpFile = new File( addStn_path + ".tmp" );
		//newFile is NEWSTATION.GUI
		newStnFile = new File( newStn_path );
		
		//if the file is a readable file
		//copy ADDSTATION.GUI.tmp to NEWSTATION.GUI
		if (( addStnFile.canRead() ) && ( newStnFile.canRead() )) {
			try {
				fis = new FileInputStream( addStnFile );
				isr = new InputStreamReader ( fis );
				br = new BufferedReader ( isr );

				fos = new FileOutputStream( tmpFile );
				pw =  new PrintWriter( fos );
				
				do {
					s = br.readLine();
					pw.println(s );	
					pw.flush(); 
				} 
				while ( s != null );
			
				br.close();
				pw.close();	

				//clean up
				fis = null;
				isr = null;
				br = null;

			}	
			catch (Exception e ) {
				//use debug here, not warning
				//b/c prints this message even
				//when does edit the file
				if (Message.isDebugOn) {
					Message.printDebug( 25, 
					routine,
					"Unable to open: \"" +
					addStn_path + "\" for manipulation." );

					Message.printWarning( 2, routine, e);
				}
			}
			//now we have created an extra file so 
			//move ADDSTATION.GUI.tmp to NEWSTATION.GUI
			try {
				tmpFile.renameTo( newStnFile );
			}		
			catch ( Exception e ) {
				file_moved = false;

				Message.printWarning( 2, routine,
					"Unable to update " +
					"\"NEWSTATION.GUI\"." );

				if ( Message.isDebugOn ) {
					Message.printDebug( 2, routine,
					"file: \"" + addStn_path + 
					".tmp\" could not " +
					"be moved to: \"" + 
					newStn_path + "\"." );

					Message.printWarning( 2, routine, e);
				}
			}
		} //end if fileReadable
		else {
			file_moved = false;
			Message.printWarning( 2, routine,
			"Unable to update \"NEWSTATION.GUI\"." );
		}

		//clean up
		
		fos = null;
		pw = null;
		s = null;
		tmpFile = null;
		addStnFile = null;
		newStnFile = null;
	}

	return file_moved;

} //end copy_addStn_to_newStn


/**
Method used to copy the template files from 
their storage location (for example: /opt/RTi/NWSRFS/template_files/Spanish/)
to the input directory where NWSRFS looks for the input files. 
@param template_local Name with path of the template file.
For example: "/opt/RTi/NWSRFS/template_files/Spanish/DEFRC.GUI"
@param new_local Name with path where the template should be placed.
For example: "/projects/cna/ofs/input/cprpn/fcinit/DEFRC.GUI".
@param edit_needed Boolean edit_needed indicates if the template
file needs to be edited before placing it in the input directory defined
by the <I>new_local</I> parameter.  The only  line in the file that
may need editing is to update the CarryoverGroup.  The CarryoverGroup, 
is represented in the file by  "CGROUP" followed by the name 
group.
The four files that need the CarryoverGroup edited are: 
<PRE><P><UL>
<LI>COSAVE.GUI</LI> 
<LI>FCEXEC.GUI</LI>
<LI>PREPROCESS.GUI</LI>
<LI>ESP.GUI</LI></UL></P>.
@param return boolean to indicate if the file was successfully copied.
*/
public static boolean copy_template_file(
		String template_local,
		String new_local,
		boolean edit_needed ) {
	String routine = _class + ".copy_template_file";
	if ( Message.isDebugOn ) {
		Message.printDebug( 2, routine, routine + " called with " +
		"location of template = \"" + template_local + "\"  and " +
		"new location for template = \"" + new_local + "\"" ); 
	}

	//boolean to indicate if the template was successfully
	//copied over to the new location.
	boolean good_copy = true;

	//String to use if we need to edit the files instead 
	//of just copying them from one local to the next.
	String strCarryoverGroup = null;
	if ( edit_needed ) {
		strCarryoverGroup = IOUtil.getPropValue( "CARRYOVERGROUP" );
		if ( strCarryoverGroup == null ) {
			//set to default as XXX.  When user
			//tries to run this file with "XXX", they
			//will get an error that CarryoverGroup XXX can't be found.
			strCarryoverGroup = "XXX";
			Message.printWarning( 2, routine,
				"Please update the file: \"" +
				new_local + "\" with the correct " +
				"Carryover Group (CGROUP).");
		}
		if ( Message.isDebugOn ) {
			Message.printDebug( 5, routine,
			"CarryoverGroup determined to be: \"" +
			strCarryoverGroup + "\".  Will edit " +
			"template files accordingly." );
		}
	}

	//make original template sure file exists and is readable
	if( ( !IOUtil.fileExists( template_local ) ) ||
	 ( !IOUtil.fileReadable( template_local) ) ) {
	 	Message.printWarning( 2, routine,
		"Template File: \"" + template_local + 
		"\" does not exist!" );
		return false;
	}


	//now copy file to new location.
	//Do edits while re-writing if neccessary...

	//make copy of the template named "template".tmp
	//move "template".tmp to location defined by new_local
	//parameter.

		//model file input and output
		File template_file = null;
		//tmpFile in this case
		File temp_file = null;
		//newFile is NEWSTATION.GUI
		File new_file = null;
		
		FileInputStream fis;
		InputStreamReader isr;
		BufferedReader br;
		FileOutputStream fos;
		PrintWriter pw;
		
		String s = null;
		template_file  = new File( template_local );
		//temp file
		temp_file = new File( template_local + ".tmp" );
		//new_file
		new_file = new File( new_local );
		
		//copy template_local to temp_file
		//and make any edits if needed
		try {
			fis = new FileInputStream( template_file );
			isr = new InputStreamReader ( fis );
			br = new BufferedReader ( isr );

			fos = new FileOutputStream( temp_file );
			pw =  new PrintWriter( fos );
				
			do {
				s = br.readLine();
				if ( s == null ) {
					break;
				}
				else if ( edit_needed ) {
					//do edits and 
					//copy file at same time
					//The ONLY edits at this
					//point are to change the
					//CGROUP in the template files.
					if ( s.startsWith( "CGROUP" ) ) {
						s = "CGROUP " + strCarryoverGroup;
					}
				}
				pw.println(s );	
				pw.flush(); 
			} 
			while ( s != null );
		
			br.close();
			pw.close();	

			//clean up
			fis = null;
			isr = null;
			br = null;

		}	
		catch (Exception e ) {
			//use debug here, not warning
			//b/c prints this message even
			//when does edit the file
			if (Message.isDebugOn) {
				Message.printDebug( 25, 
				routine,
				"Unable to open: \"" +
				template_file + "\" for manipulation." );

				Message.printWarning( 2, routine, e);
			}
		}


		//now we have created an extra file so 
		//move the temp file to the real location
		try {

			temp_file.renameTo( new_file );
		}		
		catch ( Exception e ) {
			good_copy = false;

			Message.printWarning( 2, routine,
				"Unable to update file: \"" +
				new_file + "\"/" );
				
			if ( Message.isDebugOn ) {
				Message.printDebug( 2, routine,
				"file: \"" + template_file + 
				".tmp\" could not " +
				"be moved to: \"" + 
				new_local + "\"." );

				Message.printWarning( 2, routine, e);
			}
		}


		//clean up
		
		fos = null;
		pw = null;
		s = null;
		temp_file = null;
		new_file = null;
		template_file = null;

	return good_copy;
} //end copy_template_file


/**
Opens up file identified by the name passed in and 
edits the rundate and startdate  and HOUR if present.  The files that
utilize this method: FCEXEC.GUI, COSAVE.GUI, and PREPROCESS.GUI
Both StartDate and Rundate are in FCEXEC.GUI and COSAVE.GUI, and only
RunDate (SETTODAY) is in PREPROC.GUI. All three are updated with the
hour info.
The tokens in the file are as follows:
<P><PRE><UL>
	<LI>STARTRUN is equal to StartDate</LI>
	<LI>SETTODAY is equal to RunDate</LI>
	<LI>LSTCMPDY is equal to HOUR</LI>
</UL></PRE></P>
@ param fileToEdit File to be edited to update the 
STARTRUN (startdate) and SETTODAY (rundate) and LASTCMPDY (hour) tokens.
*/
public static boolean editStartandRunDates( String fileToEdit ) {
	String routine = _class + ".editStartandRunDates";
	
	boolean ran_successfully = true;
	
	//need to edit 3 lines in either file: 
	//SETTODAY and STARTRUN and LASTCMPDY, 
	//so need to get Rundate and Startdate and HOUR from propfile
	//in nwsrfs format
	String rd = null;
	String sd = null;
	rd = IOUtil.getPropValue( "nwsrfs_RUNDATE" );
	sd = IOUtil.getPropValue( "nwsrfs_STARTDATE" );

	String hr = null;
	hr = IOUtil.getPropValue( "HOUR" );

	if (( rd != null ) && ( sd != null )) {
		//then edit the files...

		//model file input
		File inputFile = null;
		FileInputStream fis;
		InputStreamReader isr;
		BufferedReader br;

		//model output going to file
		File outputFile = null;
		FileOutputStream fos;
		PrintWriter pw;
		
		String s = null;
		inputFile = new File( fileToEdit );
		outputFile = new File( fileToEdit + ".tmp" );
		
		//if the file is a readable file
		if ( IOUtil.fileReadable( fileToEdit ) ) {
	
			try {
			fis = new FileInputStream( inputFile );
			isr = new InputStreamReader ( fis );
			br = new BufferedReader ( isr );

			fos = new FileOutputStream( outputFile );
			pw = new PrintWriter( fos );
				
			do {
				s = br.readLine();
				//change settod line
				if ( s.trim().regionMatches(true, 0,
					"@SETTOD",0,7 )) {
					s = "@SETTOD " + rd;
				}
				//change startrun line
				if ( s.trim().regionMatches(true, 0,
					"STARTRUN",0,8 )) {
					s = "STARTRUN " + sd;
				}
				//change the LASTCMPDY
				if ( (s.trim()).regionMatches(true, 0,
					"LSTCMPDY",0,8 )) {
					s = "LSTCMPDY " + hr;
				}
				
				if ( s == null ) {
					//no more lines
					break;
				}

				pw.println(s );	
				pw.flush(); 
				
			} 
			while ( s != null );
			
			br.close();
			pw.close();	

			//clean up
			fis = null;
			isr = null;
			br = null;

			}	
			catch (Exception e ) {
				//use debug here, not warning
				//b/c prints this message even
				//when does edit the file
				if (Message.isDebugOn) {
					Message.printDebug( 2, routine, 
					"Unable to open: \"" +
					fileToEdit + "\" for editing. " +
					"Can NOT CHANGE Dates." );
				}
			}

			//now we have created an extra file.  Move it back
			//to original name, ie.  
			//FCEXEC.GUI.tmp moved to FCEXEC.GUI
			try {
				outputFile.renameTo( inputFile );
			}		
			catch ( Exception secex ) {
		
				ran_successfully = false;
	
				Message.printWarning( 2, routine, 
				"Unable to save changes to \"" +
				fileToEdit + "\"" +
				"Please check file permissions. " +
				"Dates NOT CHANGED."  );
			}
		} //end if fileReadable
		else {
			ran_successfully = false;

			Message.printWarning( 2, routine, 
			fileToEdit + " is not a valid file. " +
			"Please check path, name, and permissions  of: " + 
			fileToEdit );
		}

	//clean up
	fos = null;
	pw = null;
	s = null;
	outputFile = null;
	inputFile = null;
	} //if rundate and start date are not null
	else { 
		ran_successfully = false;
		Message.printWarning( 2, routine, 
		"Unable to determine rundate and startdate. Will not "+
		"change dates in *.GUI files." );
	}
Message.printStatus(1,routine,"ran_succssfully=" +ran_successfully );
	return ran_successfully;
} //end editStartandRunDates




/**
Method to find the range of xmrg files of interest ( for
converting to Shape files).  Finds the xmrgs from the startdate to
startdate -6 so that it includes the full hydrologic dates 
for the dates startdate to startdate -6.
@return String array containing strings that represent the
date (in xmrg format: yyyyMMddHH) without the preceeding "xmrg"
and following "z" used in the xrmg file naming schema.
*/
protected static String[] getDateRangeForXMRGs() {
	String routine = _class + ".getDateRangeForXMRGs";
	
	//get startdate 
	String startbase_str = null;
	startbase_str = IOUtil.getPropValue("STARTDATE");

	//hour string saved in format: "*12z" or "*6z" for 
	//instance.  We need just the "12" or "06"
	//Also, the hour can be set to just "*"
	int base_hour_int = getHourForXMRGDates();


	//make it a date in format of xmrg file names:
	//ex: xmrg2002121909z

	DateTime startdate = new DateTime();
	//Startdate we have is in format: dd/MM/yyyy
	int yr=2003;
	int mn=12;
	int day=1;
	String yr_str = null;
	String mn_str = null;
	String day_str = null;
	yr_str = startbase_str.substring(6);
	mn_str = startbase_str.substring(3,5);
	day_str = startbase_str.substring(0,2);

	if ( StringUtil.isInteger( yr_str ) ) {
		yr = StringUtil.atoi( yr_str );
	}
	if ( StringUtil.isInteger( mn_str ) ) {
		mn = StringUtil.atoi( mn_str );
	}
	if ( StringUtil.isInteger( day_str ) ) {
		day = StringUtil.atoi( day_str );
	}	
	startdate.setYear ( yr );
	startdate.setMonth ( mn );
	startdate.setDay ( day );
	startdate.setHour ( base_hour_int );


	DateTime sdate_minus6 = new DateTime( startdate );
	sdate_minus6.addDay( -6 );
	sdate_minus6.addHour( -1 );
	
	String sd_str= null;
	String sdate_minus6_str= null;

	sd_str = startdate.getYear() + 
	StringUtil.formatString( startdate.getMonth(),"%02d") +  
	StringUtil.formatString( startdate.getDay(),"%02d") +  
	StringUtil.formatString( startdate.getHour(),"%02d") ;

	sdate_minus6_str = sdate_minus6.getYear() + 
	StringUtil.formatString( sdate_minus6.getMonth(),"%02d") +  
	StringUtil.formatString( sdate_minus6.getDay(),"%02d") +  
	StringUtil.formatString( sdate_minus6.getHour(),"%02d" );
	
	if ( Message.isDebugOn ) {
		Message.printDebug( 10, routine,
		"To get a range of XMRG files to convert to " +
		"shape files, look at dates: startdate to " +
		"startdate -6." +
		"Startdate is set as: \"" + startdate.toString() + 
		"\" (" + sd_str + ") and startdate - 6 is: \"" + 
		sdate_minus6.toString() + "\" (" + sdate_minus6_str + ")." );
	}
	
	startdate = null;
	sdate_minus6 = null;

	// return the strings that represent the dates in format:
	// "yyyyMMddHH".
	String [] dates = { sdate_minus6_str, sd_str };
	return dates;

} //end getDateRangeForXMRGs

/**
Returns a String indicating the currently set StartDate and RunDate.  The
format is: "StartDate: dd/mm/yyy  RunDate: dd/mm/yyyy".
//param date_status - JTextField to update.
return String with RunDate and StartDate.
@param untranslated_sd_string  English (default) String for
start date.
@param untranslated_rd_string  English (default) String for
run date.
*/
public static String getDatesForStatusBar( 
			String untranslated_sd_string, 
			String untranslated_rd_string )
{
	String sd_name_string = untranslated_sd_string;
	String rd_name_string = untranslated_rd_string;
	//see if there is a translation:::
	LanguageTranslator translator = null;
	translator = LanguageTranslator.getTranslator();
	if ( translator != null ) {
		sd_name_string = translator.translate("startdate_name_string", "Start Date" );
		rd_name_string = translator.translate("rundate_name_string", "Run Date" );	
	}

	String sd = null;
	String rd = null;
	String date_string = null;

	sd = IOUtil.getPropValue( "STARTDATE" );
	rd = IOUtil.getPropValue( "RUNDATE" );
	
	if ( sd ==  null ) {
		sd = "Unknown";
	}
	if ( rd ==  null ) {
		rd = "Unknown";
	}
	
	date_string = sd_name_string + ": " + sd + "  " + rd_name_string + ": " + rd;
	
	return date_string;
} 

/**
Gets the HOUR key saved in the proplist and 
formats to be used in a date string in a HOUR_OF_DAY
format (HH ).  In the proplist the HOUR is in the
format that is needed for the ofs scripts- for
example: "*12z" or "*" or "*6z".  
@return an integer in format HH (eg, 06 or 12 )
to use in contructing xmrg dates.
*/
public static int getHourForXMRGDates()
{
	//The hour string is saved in format: "*12z" or "*6z" for 
	//instance.  We need just the "12" or "06"
	//Also, the hour can be set to just "*"
	int base_hour_int = 12;
	String hour_str = null;
	hour_str = IOUtil.getPropValue("HOUR");

	if( ( hour_str == null ) || ( hour_str.equals("*")) ) {
		//set default hour to 12 am
		base_hour_int = 12;
	}
	else {
		//trim off first and last characters and should
		//be left with an int
		String hr_part = null;
		hr_part = hour_str.substring( 1, hour_str.length()-1 );

		//make sure it is 2 digits long - add
		//a zero in front if need be.
		if ( hr_part.length() == 1 ) {
			hr_part = "0" + hr_part;
		}

		//make sure it is an int
		if ( StringUtil.isInteger( hr_part ) ) {
			base_hour_int = StringUtil.atoi( hr_part );
		}
		else {
			base_hour_int = 12;
		}
	}

	return base_hour_int;

} //end getHourForXMRGDates()


/**
Returns the time portion of a carryover date, which has been
saved as the  _time_info String after the setDefaultDates() method.
@return a string containing the hour and timezone information in format:
"05:00 MST".
*/
public static String getTimeZoneAndHour()
{
	return _time_info;
}

/**
Changes the line output from running a punch command that indicates
the name and location of the output log file to represent the 
output "_pun" file.  The file names/paths are identical, except the
"_log" is replaced by the "_pun".  These output lines are in the format 
of: <P> 
"==> /projects/ahps/panama/ofs/output/ams/ppinit_log.20020106.180020 <=="
</P> The related "_pun" file would be: <P>
"/projects/ahps/panama/ofs/output/ams/ppinit_pun.20020106.180020"</P>
@param log_file  Output from running a PUNCH command, including the 
extra characters that are output.  The format is:<P>
"==> /projects/ahps/panama/ofs/output/ams/ppinit_log.20020106.180020 <==" </P>
@return String representing the path to the "_pun" file with any extra
	characters removed.
*/
public static String get_pun_path_from_log( String log_file ) {
	String routine = _class + ".get_pun_path_from_log";
	
	//first remove "==>" and "<=="
	String log_name = null;
	String pun_name = null;
	Vector v = null;
	//break up based on spaces 
	v = StringUtil.breakStringList( log_file, " ",
		StringUtil.DELIM_SKIP_BLANKS );
	//vector should be 3 pieces.
	int size = 0;
	if ( v != null ) {
		size = v.size();
	}
	if( size == 3 ) {
		log_name = (String)v.elementAt(1);

		//now change "_log" to "_pun"
		int index = -1;
		index = log_name.indexOf( "_log" );		
		String base_path = null;
		String end_path = null;
		base_path = log_name.substring( 0, index );
		end_path  = log_name.substring( index+4 ); 

		pun_name = base_path + "_pun" + end_path;
		
	}
	else {
		Message.printWarning( 2, routine, 
		"Unable to remove extra characeters from log file name: \"" +
		log_file + "\"." );
	}
	return pun_name;
} //end get_pun_path_from_log

/**
Method used to return the text needed for the strings used to label
GUI components.  If there is a lookup table being used to translate the
gui to another language, the method will return the translated value;
otherwise, it returns the second (default) string passed in.
@param key  String to lookup in lookup table.
@param default_string  String to return if the key is not found 
in the look up table.
@return  the translated version for the key passed in or the default string
passed in if the key can not be located.
*/
/////////////////////////////////////////////
//this moved to language translator
/////////////////////////////////////////////
/*
public static String getTranslatedText( String key, String default_string ) {
	String routine = _class + ".getTranslatedText";
	
	String translated_str = null;
	translated_str = 
		NwsrfsGUI_Util.findUnicodeStringInTranslatedVector( key );

	if ( Message.isDebugOn ) {
		Message.printDebug( 15, routine,
		"translated_str= \""+translated_str+"\".");
	}

	if ( ( translated_str == null ) || ( translated_str.length() == 0 ) ) {
		Message.printWarning( 2, routine,
		"Unable to find translation for string: \"" +
		key + "\". Will use English version." );

		translated_str = default_string; 
	} 

	return translated_str;
}
*/



/**
Adds the amount of days passed in to the nwsrfs-formatted date passed in,
where the nwsrfs format is: "MMdd/yyyy/". 
@param nwsdate Date string that needs incremented by some number of days 
in nwsrfs format ("MMdd/yyyy/").
@return String representation of the incremented date in the
nwsrfs format: "MMdd/yyyy/".
*/
public static String increment_nwsrfsDays( String nwsdate, int increment )
{
	nwsdate = nwsdate.trim();

	DateTime date_to_increment = new DateTime();
	//date we have is in format: MMdd/yyyy/
	int yr=2003;
	int mn=12;
	int day=1;
	String yr_str = null;
	String mn_str = null;
	String day_str = null;
	yr_str = nwsdate.substring(5,9);
	mn_str = nwsdate.substring(0,2);
	day_str = nwsdate.substring(2,4);

	if ( StringUtil.isInteger( yr_str ) ) {
		yr = StringUtil.atoi( yr_str );
	}
	if ( StringUtil.isInteger( mn_str ) ) {
		mn = StringUtil.atoi( mn_str );
	}
	if ( StringUtil.isInteger( day_str ) ) {
		day = StringUtil.atoi( day_str );
	}	
	date_to_increment.setYear ( yr );
	date_to_increment.setMonth ( mn );
	date_to_increment.setDay ( day );

	DateTime newDate = new DateTime( date_to_increment );
	newDate.addDay( increment );

	return StringUtil.formatString(newDate.getMonth(),"%02d") +  
	StringUtil.formatString(newDate.getDay(),"%02d") + "/" + newDate.getYear() + "/";
} //end increment_nwsrfsDays

/**
Adds the amount of days passed in to the nwsrfs-formatted date passed in,
where the nwsrfs format is: "MMdd/yyyy/". 
@param nwsdate Date string that needs incremented by some number of days 
in nwsrfs format ("MMdd/yyyy/").
@return String representation of the incremented date in the
nwsrfs format: "MMdd/yyyy/".
*/
public static String increment_nwsrfsDays2( String nwsdate, int increment ) {
	String routine = _class + ".increment_nwsrfDays";

	nwsdate = nwsdate.trim();
	Date date_to_increment = new Date();
	String newDate = null;
	try {
		_nwsrfs_date_formatter.setLenient( false );
		date_to_increment = _nwsrfs_date_formatter.parse( nwsdate );
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, 
			"Date entered: \"" + nwsdate + 
			"\" could not be parsed. " );	
		if ( Message.isDebugOn ){
			Message.printWarning( 2, routine, e );
		}
	}
	//now that we have the date and it is in the format we need, 
	//add the number of days represented by the increment passed in
	//first make a calendar with the entered date
	GregorianCalendar cal = new GregorianCalendar();
	//set cal equal to date entered
	cal.setTime( date_to_increment );
	//add the # days passed in (increment variable)
	cal.add(Calendar.DATE, increment );
	//create a new date
	Date incrementedDate = cal.getTime(); 	
	//convert it to a string
	newDate = _nwsrfs_date_formatter.format( incrementedDate );
	if ( Message.isDebugOn ) {
		Message.printDebug( 3, routine, 
		"old date: " + nwsdate + " new date: " + newDate);
	}
	return newDate;

} //end increment_nwsrfsDays2

// TODO SAM 2004-11-02 Over time enhance this method to configure the plot
// based on data type, etc.  For example, display precipitation plots as
// bars.  Can also configure to plot multiple graphs on a page.  For now, just
// display everything as line graphs.
/**
Read one or more time series and display a plot of the time series.  Currently
a simple line plot is displayed in all cases.
@param dmi An NWSRFS_DMI instance with an open connection to the database.
@param tsident_string_Vector  Vector of time series identifiers to plot.
If a time series identifier does not include the "~NWSRFS_FS5Files" input name,
the string is appended to the end of the time series identifier.
@exception Exception if there is an error displaying the plot.
*/
public static void plotTimeSeries (	NWSRFS_DMI dmi,
					Vector tsident_string_Vector,
					DateTime start_DateTime,
					DateTime end_DateTime )
throws Exception
{	String routine = _class + ".plotTimeSeries";

	int size = 0;
	if ( tsident_string_Vector != null ) {
		size = tsident_string_Vector.size();
	}

	// Read the time series for each identifier...

	Vector ts_Vector = new Vector();

	TS ts = null;
	String tsident_string = null;
	for ( int i = 0; i < size; i++ ) {
		try {	tsident_string =
				(String)tsident_string_Vector.elementAt(i);
			if (	StringUtil.indexOfIgnoreCase( tsident_string,
				"~NWSRFS_FS5Files", 0 ) < 0 ) {
				// The TSID apparently does not have the input
				// type necessary to use with the NWSRFS_DMI so
				// add it...
				tsident_string += "~NWSRFS_FS5Files";
			}
			
			ts = dmi.readTimeSeries ( tsident_string,
				start_DateTime, end_DateTime, "", true );

		}
		catch ( Exception e ) {
			Message.printWarning( 2, routine, e );
			if ( Message.isDebugOn ) {
				Message.printWarning( 2, routine, 
				"Error reading time series \"" +
				tsident_string + "\" for start= " +
				start_DateTime + " and end=" + end_DateTime );
				ts = null;
			}
		}

		if ( ts == null ) {
			Message.printStatus ( 2, routine, 
			"Not plotting null time series for \"" +
			tsident_string + "\"" );
		}
		else {	ts_Vector.addElement( ts );
		}
	}

	if ( ts_Vector.size() == 0 ) {
		String message = "No time series were read.";
		throw new Exception ( message );
	}

	// Plot the time series.  If an exception occurs it will be caught in
	// the calling code...

	PropList p = new PropList( routine );	
	p.set( "InitialView=Graph" );
	p.set( "TitleString=NWSRFS Time Series" );
	new TSViewJFrame( ts_Vector, p );
}

/**
Opens up ESP.GUI file and stores the values for the following parameters:
<P><PRE><TABLE>
<TR><TD> HISTSIM(1) </TD></TR>
<TR><TD> WINDOW(1) 0728/2000/ 1001/2000/ </TD></TR>
</TABLE></PRE></P>
in order to update the same paramters in the ESP GUI display.  The values
are for HistSim, Start Date and End Date.
@param return  return a Vector that contains the following 3 Strings:
histsim("YES" or "No")startdate ("MMdd/yyyy/"), and enddate("MMdd/yyyy/").
*/
//***********editESP************
public static Vector retrieve_ESPfile_values() {

	String routine =  _class + ".retrieve_ESPfile_values";

	//Vector to hold the strings we need
	Vector esp_vect = new Vector();
	
	//get ESP.GUI local
	String fileToEdit = null;
	fileToEdit = IOUtil.getPropValue( "ESP.GUI" );
	if ( fileToEdit == null ) {
		Message.printWarning(2, routine,
		"Unable to locate the \"ESP.GUI\" file. Can not read the "+
		"file's properties.  Please Run ESP by hand.") ;
	}
	else {
	
		//model file input
		File inputFile = null;
		FileInputStream fis;
		InputStreamReader isr;
		BufferedReader br;

		String s = null;
		inputFile = new File( fileToEdit );
		
		//if the file is a readable file
		if ( IOUtil.fileReadable( fileToEdit ) ) {
			try {
				Message.printStatus( 2, routine,
				"ESP file: " + fileToEdit );	
				fis = new FileInputStream( inputFile );
				isr = new InputStreamReader ( fis );
				br = new BufferedReader ( isr );

				do {
					//read in and write out each line,
					//saving  values for the 2 lines that
					//we need:
					//HISTSIM(1) - histsim value
					// WINDOW(1) 0728/2000/ 1001/2000/ 
					//-start and end date values.

					//Read line by line.
					s = br.readLine();

					//change histsim line: the line
					//contains:
					//HISTSIM(1)
					if ( s.trim().regionMatches(true, 0,
						"HISTSIM",0,7 )) { 
					
						//store the value found
						//in parenthesis  as
						//1 == yes, 0 == no 

						//get index of "("
						String value = null;
						int ind = -99;
						ind = s.indexOf( "(" );
						if ( ind > 0 ) {
							value = s.substring(
								ind+1, ind+2);
						}
						if ( value != null ) {
							if (( value.
							equals("0")) ||
							( value.
							equals("1")) ) {

								//add HISTSIM
								//value
								esp_vect.
								addElement(
								value );
							}
							else {
								Message.
								printWarning(
								2, routine,
								"Error in " +
								"ESP.GUI file."+
								" Please " +
								"verify the " +
								"HISTSIM value"+
								" and run " +
								"ESP " +
								"manually." );
						
							}
						} //end if value!=null
					} //end HISTSIM 

					//finally look for line with 
					//both Start date and End date.
					if ( s.trim().regionMatches(true, 0,
						"WINDOWS",0,7 )) { 
						//get the start and end
						//dates.
					
						//Line is formatted like:	
					//WINDOW(1) 0728/2000/ 1001/2000/
						//Start date is the firt
						//date value and end date
						//the second.

						String start_str = null;	
						String end_str = null;

						int ind_1 = -99;

						ind_1 = s.indexOf( ")" );
						if ( ind_1 > 0 ) {
							//assume the
							//rest is formatted
							//correctly
					//WINDOW(1) 0728/2000/ 1001/2000/
							start_str = 
								(s.substring(
								(ind_1 + 1), 
								(ind_1+12) )).
								trim();

							//add start date 
							//to vector.
							esp_vect.addElement(
								start_str );
						
							end_str = (s.substring (
								ind_1+13)).
								trim();

							//add end date to vector
							esp_vect.addElement(
								end_str );

							if(Message.isDebugOn) {
							Message.printDebug(
								25,
								routine,
								"Start date "+
								"in ESP.GUI " +
								"file is: \""+
								start_str + 
								"\" and end " +
								"date is: \"" +
								end_str + 
								"\".");
							}
						}
						
					}
				} 
				while ( s != null );
			
				br.close();

				//clean up
				fis = null;
				isr = null;

			}	
			catch (Exception e ) {
				//use debug here, not warning
				//b/c prints this message even
				//when does edit the file
				if (Message.isDebugOn) {
					Message.printDebug( 20,
					routine,
					"Unable to open: " +
					fileToEdit + " for reading. " +
					"Please run ESP by hand." );
				}
			}

		} //end if fileReadable
		else {
			Message.printWarning( 2, "editESP",
			fileToEdit + " is not a valid file. " +
			"Please check path, name, and permissions of: " + 
			fileToEdit + " And run ESP by hand." );
		}

		//clean up
		s = null;
		inputFile = null;
	} //end else file!=null

	return esp_vect;
} //end retrive_ESPfile_values

/**
Opens up ESP.GUI and edits the file adding the user input
start date, end date , and Histsim() value (HISTSIM(0)=no, (1)=yes).
ESP.GUI file format and contents:
<P><PRE><TABLE>
<TR><TD> @SETOPT </TD></TR>
<TR><TD> METRIC(1) </TD></TR>
<TR><TD> PERMWRIT(1) </TD></TR>
<TR><TD> HISTSIM(1) </TD></TR>
<TR><TD> TSUNITS(1) 91 92 93 94 95 </TD></TR>
<TR><TD> $FGROUP YAQUI </TD></TR>
<TR><TD> $CGROUP 072800 </TD></TR>
<TR><TD> HISTWYRS 1962 1985 </TD></TR>
<TR><TD> STARTESP 072800 </TD></TR>
<TR><TD> WINDOW(1) 072800 100100 </TD></TR>
<TR><TD> @COMP ESP </TD></TR>
<TR><TD> @STOP </TD></TR>
</TABLE></PRE></P>
@param  startdate  Start date for ESP formatted as: MMddyy.
@param  enddate  End date for ESP formatted as:MMddyy.
@param  histsim_numb  1 for yes, 0 for no 
*/
//***********editESP************
public static void rewrite_esp_file( String startdate,
				String enddate,
				String histsim_numb ) {
	
	String routine =  _class + ".rewrite_esp_file";

	//get ESP.GUI local
	String fileToEdit = null;
	fileToEdit = IOUtil.getPropValue( "ESP.GUI" );
	if ( fileToEdit == null ) {
		Message.printWarning(2, routine,
		"Unable to locate the \"ESP.GUI\" file. Can not edit it." );
	}
	else {
	
		//model file input
		File inputFile = null;
		FileInputStream fis;
		InputStreamReader isr;
		BufferedReader br;

		//model output going to file
		File outputFile = null;
		FileOutputStream fos;
		PrintWriter pw;
			
		String s = null;
		inputFile = new File( fileToEdit );
		outputFile = new File( fileToEdit + ".tmp" );
		
		//if the file is a readable file
		if ( IOUtil.fileReadable( fileToEdit ) ) {
	
			try {
				Message.printStatus( 2, "editESP",
				"Editing file: " + fileToEdit );	
				fis = new FileInputStream( inputFile );
				isr = new InputStreamReader ( fis );
				br = new BufferedReader ( isr );

				fos = new FileOutputStream( outputFile );
				pw = new PrintWriter( fos );
				
				do {
					//read in and write out each line,
					//making changes to the 3 lines that
					//we have user input for:
					//histsim, startdate, and enddate

					s = br.readLine();
					//change histsim line: the line
					//contains:
					//HISTSIM(1)
					if ( s.trim().regionMatches(true, 0,
						"HISTSIM",0,7 )) { 
						s = "HISTSIM(" + 
						histsim_numb + ") ";
					} 

					//next look for STARTESP line
					if ( s.trim().regionMatches(true, 0,
						"STARTESP",0,8 )) { 
						s = "STARTESP " +
						startdate;
					} 

					//finally look for End date line
					if ( s.trim().regionMatches(true, 0,
						"WINDOWS",0,7 )) { 
						s = "WINDOWS(1) " +
						startdate + " " + enddate;
					} 
					
					if ( s == null ) { 
						//no more lines break; 
					}

				pw.println(s );	
				pw.flush(); 
				
				} 
				while ( s != null );
			
				br.close();
				pw.close();	

				//clean up
				fis = null;
				isr = null;
				br = null;

			}	
			catch (Exception e ) {
				//use debug here, not warning
				//b/c prints this message even
				//when does edit the file
				if (Message.isDebugOn) {
					Message.printDebug( 20,
					"editESP",
					"Unable to open: " +
					fileToEdit + " for editing." );
				}
			}

			//now we have created an extra file.  Move it back
			//to original name, ie.  
			//ESP.GUI.tmp moved to ESP.GUI
			try {
				outputFile.renameTo( inputFile );
			}			
			catch ( Exception secex ) {
				Message.printWarning( 2, "editESP",
				"tmp file: " + fileToEdit + ".tmp could not " +
				"be moved back to: " + fileToEdit + 
				". Please check permissions of: " + 
				fileToEdit );
			}
		} //end if fileReadable
		else {
			Message.printWarning( 2, "editESP",
			fileToEdit + " is not a valid file. " +
			"Please check path, name, and permissions of: " + 
			fileToEdit );
		}

		//clean up
		fos = null;
		pw = null;
		s = null;
		outputFile = null;
		inputFile = null;
	} //end else file!=null
} //end rewrite_esp_file

/**
Runs the archive script: "RFSArchive" that is assumed to be in the
user's path.
@return  Vector containing output of running the RFSArchive command:
"xterm -e RFSarchive -f".
*/
public static Vector run_archive() {
	String routine = _class + ".run_archive";
	
	String cmd = "xterm -e RFSarchive -f";
	String[] arrCmd = {"xterm -e RFSarchive -f"};
	//String cmd = "RFSarchive -f";
	int exitstat = -999;
	Vector arch_vect = null;
	ProcessManager pm = new ProcessManager( arrCmd );
	try {
		pm.saveOutput( true );
		pm.run();
		arch_vect = pm.getOutputVector();
		//arch_vect = pm.runUntilFinished();
		arch_vect.insertElementAt( "Command Run: \"" + cmd + "\"", 0 );
		exitstat = pm.getExitStatus();
		if ( exitstat != 0 ) {
			Message.printWarning( 
			2, routine,	
			"Unable to run \"RFSarchive\" ");
			
			if ( arch_vect == null ) {
				arch_vect = new Vector();
			}
			arch_vect.setElementAt( 
				"Command: \"" + cmd + "\" failed", 0 );
		}
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, 
		"Unable to run \"RFSarchive\" via command: \"" +
		cmd + "\"." );
		if ( arch_vect == null ) {
			arch_vect = new Vector();
		}
			arch_vect.setElementAt( 
				"Command: \"" + cmd + "\" failed", 0 );
		if ( Message.isDebugOn ) {
			Message.printWarning( 2, routine, e );
		}
	}
	pm = null;

	return arch_vect;
} //end run_archive

/**
Runs the detel_is_running script that is assumed to be in the user's
path.
*/
public static Vector run_delete_failed_ifp() {
	String routine = _class + ".run_delete_failed_ifp";

	//ofs command
	String cmd = "delete_is_running";
	String[] cmd_arr = {"delete_is_running"};

	//vector to hold output
	Vector delIFP_vect = null;

	int exitstat = -999;

	//Run the command
	//do not need full path- just file name for the ofs commands
	ProcessManager pm = new ProcessManager( cmd_arr );
	try {
		pm.saveOutput( true );
		pm.run();
		delIFP_vect = pm.getOutputVector();
		delIFP_vect.insertElementAt( 
			"Command Run: \"" + cmd + "\"", 0 );
		exitstat = pm.getExitStatus();
		if ( exitstat != 0 ) {
			Message.printWarning( 2, routine,
			"Command: \"" + cmd + "\" failed.");
			delIFP_vect.setElementAt( 
			"Command: \"" + cmd + "\" failed", 0 );
		}	
	}
	catch (Exception e) {
		Message.printWarning( 2, routine, 
		"Command: \"" + cmd + "\". FAILED. " ); 
		if ( delIFP_vect == null ) {
			delIFP_vect = new Vector();
			delIFP_vect.addElement( 
			"Unable to run \"" + cmd + "\"" );
		}
	}
	pm = null;

	return delIFP_vect;
} //end run_delete_failed_ifp


/**
Calls update_deleteRC_file() to update the DELETERC.GUI
file with the Rating Curve ID that needs to be deleted.
Runs the ofs script:
"ofs -p fcinit -i DELETERC.GUI -o DELETERC.GUI.out" 
@param rcid  Name of Rating Curve to delete.
@param return Null if the ofs script:
"ofs -p fcinit -i DELETERC.GUI -o DELETERC.GUI.out" fails or
a vector containing the output from the OFS comand if the command succeeds.
*/
public static Vector run_delete_ratingCurve( String rcid ) {
	String routine = _class + ".run_delete_ratingCurve";
	
	//vector to return
	Vector delRC_vect = null;

	//command to run
	String cmd = "ofs -p fcinit -i DELETERC.GUI -o DELETERC.GUI.out";
	String[] cmd_arr = {"ofs -p fcinit -i DELETERC.GUI -o DELETERC.GUI.out"};

	//exit status
	int exitstat = -999;

	//update the DELTERC.GUI file with the name
	//of the rating curve to delete.  THis will
	//return false if the File could not be edited with 
	//name of the Rating Curve to delete if it 
	//succeeds.
	boolean file_edited = true;
	file_edited = update_deleteRC_file( rcid );
	if ( Message.isDebugOn ) {
		Message.printDebug( 12, routine,
		"Value returned from update_deleteRC_file is: " +
		file_edited );
	}
	if ( !file_edited ) {
		return delRC_vect;
	}
	// we have successfully edited file and 
	//now can run it thru the OFS Script!
	//now run OFS SCRIPT

	int go_ahead = JOptionPane.showConfirmDialog(
		null, "Are you sure you want to delete " +
		"rating curve: \"" + rcid + "\"?", 
		"Confirm Deletion", JOptionPane.YES_NO_OPTION );

	if ( go_ahead == JOptionPane.YES_OPTION )  {
		//Run the command
		ProcessManager pm = new ProcessManager( cmd_arr );
		//pm.setCommandInterpreter(null);
		try {
			pm.saveOutput( true );
			pm.run();
			delRC_vect = pm.getOutputVector();
			delRC_vect.insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			exitstat = pm.getExitStatus();
			if ( exitstat != 0 ) {
				delRC_vect = null;
				/*
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				delRC_vect.setElementAt( 
				"Command: \"" + cmd + "\" failed", 0 );
				*/
			}	
		}
		catch (	Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			delRC_vect = null;
		}
		pm = null;

	}
	else {
		Message.printStatus(1,routine, 
		"rating curve: \"" + rcid + "\" not deleted." );
		delRC_vect = null;
	}

	return delRC_vect;

} //end run_delete_ratingCurve


/**
Run one of the following ofs commands, depending on the 
dump_file parameter passed in:
"ofs -p ppinit -i DUMPSTN.GUI -o DUMPSTN.GUI.out" or 
"ofs -p ppinit -i DUMPMAP.GUI -o DUMPMAP.GUI.out".
"ofs -p ppinit -i DUMPFMAP.GUI -o DUMPFMAP.GUI.out".
Creates the DUMPSTN.GUI or DUMPMAP.GUI or DUMPFMAP.GUI files on the fly.
@param id  Name of Station or MAP or FMAP to get the definition of.
@param fs  File system separator.
@param dump_file  Which file to create and run: DUMPSTN or DUMPMAP or DUMPFMAP.
@return  String with name of file output from running either command
"ofs -p fcinit -i DUMPSTN.GUI -o DUMPSTN.GUI.out"  or
"ofs -p fcinit -i DUMPMAP.GUI -o DUMPMAP.GUI.out" 
"ofs -p fcinit -i DUMPFMAP.GUI -o DUMPFMAP.GUI.out" 
or null if command was unsucessful or null if not.
*/
public static String run_dump_station_or_area( 
					String id, 
					String fs, 
					String dump_file ) {
	String routine = _class + ".run_dump_station_or_area";

	//get units ( unsed in DUMPMAP and DUMFMAP)
	String units = null;
	units = IOUtil.getPropValue( "UNITS" );
	if ( units == null ) {
		units = "METR";
	}
	
	//file name/path to return
	String output_file = null;

	//if dump_file is DUMPMAP- set do_map to true,
	//if dump_file is DUMPFMAP- set do_fmap to true,
	//if dump_file is DUMPSTN - set do_stn to true
	boolean do_map = false;
	boolean do_fmap = false;
	boolean do_stn = false;

	String cmd = null;
	String[] cmd_arr= null;
	if ( dump_file.equals( "DUMPMAP" ) ) {
		do_map = true;
	}
	if ( dump_file.equals( "DUMPFMAP" ) ) {
		do_fmap = true;
	}
	if ( dump_file.equals( "DUMPSTN" ) ) {
		do_stn = true;
	}
		
	if ( do_map ) {
		cmd = "ofs -p ppinit -i DUMPMAP.GUI -o DUMPMAP.GUI.out -u " +
		_output_dir;

		cmd_arr = new String[] {"ofs -p ppinit -i DUMPMAP.GUI -o "
			+ "DUMPMAP.GUI.out -u " + _output_dir};
	}
	if ( do_fmap ) {
		cmd = "ofs -p ppinit -i DUMPFMAP.GUI -o DUMPFMAP.GUI.out -u " +
		_output_dir;

		cmd_arr = new String[] { "ofs -p ppinit -i DUMPFMAP.GUI -o "
			+ "DUMPFMAP.GUI.out -u " + _output_dir };
	}
	if ( do_stn ) {
		cmd = "ofs -p ppinit -i DUMPSTN.GUI -o DUMPSTN.GUI.out -u " +
		_output_dir;

		cmd_arr = new String[] {"ofs -p ppinit -i DUMPSTN.GUI -o "
			+ "DUMPSTN.GUI.out -u " + _output_dir };
	}
		
	//vector to hold output
	Vector dump_vect = null;

	//boolean indicates if DUMPXXX.GUI file is edited.
	boolean file_edited = true;

	//first get location of DUMPXXX.GUI
	//find path to file that we know does exist in that same
	// ../ppinit/ directory
	String path_to_ppinit_stn = null;
	//just use "ADDSTATION.GUI" file b/c we know it is in the
	//same ppinit directory.
	path_to_ppinit_stn = IOUtil.getPropValue( "ADDSTATION.GUI" );
	StringBuffer b = new StringBuffer();
	if (( path_to_ppinit_stn != null ) && ( 
		IOUtil.fileExists( path_to_ppinit_stn ) )) {
		//cut off file name (last item)
		Vector v = null;
		v = StringUtil.breakStringList( path_to_ppinit_stn,
			fs, StringUtil.DELIM_SKIP_BLANKS );
		int size = 0;
		if ( v !=null ) {
			size = v.size();
		}
		for ( int i=0; i<size-1; i++ ) {
			b.append( (String) v.elementAt(i) + fs );
		}
		v = null;
	}
	//now should have path to ppinit dir, add DUMPXXX.GUI here
	String dump_path = null;
	if ( do_map ) {
		dump_path = fs + b.toString() + "DUMPMAP.GUI";
	}
	if ( do_fmap ) {
		dump_path = fs + b.toString() + "DUMPFMAP.GUI";
	}
	if ( do_stn ) {
		dump_path = fs + b.toString() + "DUMPSTN.GUI";
	}
	//clean up 
	b = null;

	if ( dump_path == null ) {
		Message.printWarning( 2, routine,
		"Unable to find location for " +
		"\"DUMP\" file." );

		file_edited = false;
	}
	
	if ( file_edited ) {
		//really try to create file now.

		//model output going to file
		File outputFile = null;
		FileOutputStream fos;
		PrintWriter pw;
		//NEED TO CREATE THIS FILE - do not assume it is there
		outputFile = new File( dump_path );
		try {
			if ( Message.isDebugOn ) {
				Message.printDebug( 15, routine,
				"Creating file: \"" + dump_path + "\"." );	
			}
			fos = new FileOutputStream( outputFile );
	
			//write
			pw = new PrintWriter( fos );
			if ( do_map ) {
				pw.println( "@DUMP PRINT UNITS(" +units +")" );	
				pw.flush(); 
				pw.println( "AREA MAP " + id );
				pw.flush(); 
			}
			if ( do_fmap ) {
				pw.println( "@DUMP PRINT UNITS(" +units +")" );	
				pw.flush(); 
				pw.println( "AREA FMAP " + id );
				pw.flush(); 
			}
			if( do_stn ) {
				pw.println("@DUMP PRINT STA ALLPARM " +id);	
				pw.flush(); 
			}
			pw.println( "@STOP" );
			pw.flush(); 

			pw.close();	
		}	
		catch (Exception ioe ) {
			//gets caught
			//even when does edit the file
		}

		fos = null; 
		pw = null;
		outputFile = null;
	}

	//now run the ofs command
	//do not need full path- just file name for the ofs commands
	int exitstat = -99;
	//if the file is created (ie, edited)
	if ( file_edited ) {
		ProcessManager pm = new ProcessManager( cmd_arr );
		//pm.setCommandInterpreter(null);
		try {
			pm.saveOutput( true );
			pm.run();
			dump_vect = pm.getOutputVector();
			//dump_vect = pm.runUntilFinished();
			exitstat = pm.getExitStatus();
			dump_vect.insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				dump_vect.setElementAt( 
				"Command \"" + cmd + "\" failed", 0 );
			
			 file_edited = false;
			}	
		}	
		catch (Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			if ( dump_vect == null ) {
				dump_vect = new Vector();
				dump_vect.addElement( 
				"Unable to run \"" + cmd + "\"" );
			}
		}
		pm = null;
	}
	else {
		//don't run ofs command b/c file was not edited.
	}	

	//now just need to get the output file and  return it.  The
	//calling method ( NwsrfsMainGUI ) will open it for viewing.

	if ( file_edited ) {
		//then get the string from the output that 
		//starts with "==>" that indicates path of log file that was 
		//output: ex:/projects/.../ppinit_log.20020403.17
		int size = 0;
		if ( dump_vect != null ) {
			size = dump_vect.size();
		}
		String s = null;
		for ( int i=0; i<size; i++ ) {
			s = ((String)dump_vect.elementAt( i )).trim();
			//line with output file name:
			//has this in it:
			//"==> output_file_name <=="
			if ( s.startsWith("==") ) {
				break;
			}
		}

		String logfile = null;
		//now have log file in format:
		//==> /path/file/ <==
		//get rid of the ==> and <==
		Vector v = null;
		v = StringUtil.breakStringList( s, " ",
			StringUtil.DELIM_SKIP_BLANKS );
		//vector should have 3 pieces and path is mid one	
		if (( v != null ) && ( v.size() == 3 ) ) {
			logfile = (String)v.elementAt(1);
		}
		v = null;

		
		//now we have a line like this:
		///projects/ahps/panama/ofs/output/ams/ppinit_log.20020403.17
		//Now we just need to get the DUMPSTN.GUI.out or 
		//DUMPMAP.GUI.out  or DUMPFMAP.GUI.out
		//file to open.  They have the same 
		//path as the log file:
		//ex: /projects/.../DUMPSTN.GUI.out.20020403.17
		//So, we need to replace "ppinit_log" with
		//DUMPSTN.GUI.out or DUMPMAP.GUI.out or DUMPFMAP.GUI.out and
		//keep timestamp
		//get index of "ppinit_log"
		int index = -999;
		//ppinit_log is 10 chars long
		index = logfile.indexOf( "ppinit_log" );
		if ( index > 0 ) {
			if ( do_map ) {
				output_file = 
				( logfile.substring( 0, index ) + 
				"DUMPMAP.GUI.out." + 
				logfile.substring( index+11) ); 
			}
			if ( do_fmap ) {
				output_file = 
				( logfile.substring( 0, index ) + 
				"DUMPFMAP.GUI.out." + 
				logfile.substring( index+11) ); 
			}
			if ( do_stn ) {
				output_file = 
				( logfile.substring( 0, index ) + 
				"DUMPSTN.GUI.out." + 
				logfile.substring( index+11) ); 
			}
		}

	}
	return output_file;

} //end run_dump_station_or_area


/**
Runs the ofs command:
"ofs -p ppdutil -i DUMPOBS.GUI -o DUMPOBS.GUI.out".
@return  Vector of line output from running:
"ofs -p ppdutil -i DUMPOBS.GUI -o DUMPOBS.GUI.out".
*/
public static Vector run_dump_obs() {
	String routine = _class + ".run_dump_obs";

	//ofs command
	String cmd = "ofs -p ppdutil -i DUMPOBS.GUI -o DUMPOBS.GUI.out -u " +
	_output_dir;

	String[] cmd_arr = {"ofs -p ppdutil -i DUMPOBS.GUI -o DUMPOBS.GUI.out -u " + _output_dir };

	//vector to hold output
	Vector obs_vect = null;
	
	String obs_gui_file = null;
	obs_gui_file = IOUtil.getPropValue( "DUMPOBS.GUI" );
	if ( ( obs_gui_file != null ) && ( 
		IOUtil.fileExists( obs_gui_file )) ) {

		//the ofs command should be run.
		//exitstat
		int exitstat = -999;

		//Run the command
		//do not need full path- just file name for the ofs commands
		ProcessManager pm = new ProcessManager( cmd_arr );
		try {
			pm.saveOutput( true );
			pm.run();
			obs_vect = pm.getOutputVector();
			//obs_vect = pm.runUntilFinished();
			obs_vect.insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			exitstat = pm.getExitStatus();
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				obs_vect.setElementAt( 
				"Command \"" + cmd + "\" failed", 0 );
			}	
		}
		catch (Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			if ( obs_vect == null ) {
				obs_vect = new Vector();
				obs_vect.addElement( 
				"Unable to run \"" + cmd + "\"" );
			}
		}
		pm = null;
	}
	else {
		Message.printWarning( 2, routine, 
		"The \"DUMPOBS.GUI\" file: \"" + obs_gui_file +
		"\" can not be used.  The ofs command: \"" + cmd + 
		"\" will not be run." ); 
		obs_vect = new Vector();
		obs_vect.addElement( "Unable to run \"" + cmd + "\"" );
	}

	return obs_vect;
} //end run_dump_obs


/**
Runs the ofs command:
"ofs -p prdutil -i DUMPTS.GUI -o DUMPTS.GUI.out".
@return  Vector of line output from running:
"ofs -p prdutil -i DUMPTS.GUI -o DUMPTS.GUI.out".
*/
public static Vector run_dump_ts() {
	String routine = _class + ".run_dump_ts";

	//ofs command
	String cmd = "ofs -p prdutil -i DUMPTS.GUI -o DUMPTS.GUI.out -u " +
	_output_dir;

	String[] cmd_arr = {"ofs -p prdutil -i DUMPTS.GUI -o DUMPTS.GUI.out -u " + _output_dir};

	//vector to hold output
	Vector ts_vect = null;
	
	String ts_gui_file = null;
	ts_gui_file = IOUtil.getPropValue( "DUMPTS.GUI" );
	if ( ( ts_gui_file != null ) && ( 
		IOUtil.fileExists( ts_gui_file )) ) {

		//the ofs command should be run.
		//exitstat
		int exitstat = -999;

		//Run the command
		//do not need full path- just file name for the ofs commands
		ProcessManager pm = new ProcessManager( cmd_arr );
		try {
			pm.saveOutput( true );
			pm.run();
			ts_vect = pm.getOutputVector();
			//ts_vect = pm.runUntilFinished();
			ts_vect.insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			exitstat = pm.getExitStatus();
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				ts_vect.setElementAt( 
				"Command \"" + cmd + "\" failed", 0 );
			}	
		}
		catch (Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			if ( ts_vect == null ) {
				ts_vect = new Vector();
				ts_vect.addElement( 
				"Unable to run \"" + cmd + "\"" );
			}
		}
		pm = null;
	}
	else {
		Message.printWarning( 2, routine, 
		"The \"DUMPTS.GUI\" file: \"" + ts_gui_file +
		"\" can not be used.  The ofs command: \"" + cmd + 
		"\" will not be run." ); 
		ts_vect = new Vector();
		ts_vect.addElement( "Unable to run \"" + cmd + "\"" );
	}

	return ts_vect;
} //end run_dump_ts

/**
Runs esp via the ofs script:
"ofs -p fcst -i ESP.GUI -o ESP.GUI.out"
@return  Vector of line output from running:
"ofs -p fcst -i ESP.GUI -o ESP.GUI.out"
*/
public static Vector run_esp() {
	String routine = _class + ".run_esp";

	//ofs command
	String cmd = "ofs -p fcst -i ESP.GUI -o ESP.GUI.out -u " +
	_output_dir;

	String[] cmd_arr = {"ofs -p fcst -i ESP.GUI -o ESP.GUI.out -u " + _output_dir};

	//vector to hold output
	Vector esp_vect = null;
	
	String esp_gui_file = null;
	esp_gui_file = IOUtil.getPropValue( "ESP.GUI" );
	if ( ( esp_gui_file != null ) && ( 
		IOUtil.fileExists( esp_gui_file )) ) {

		//the ofs command should be run.
		//exitstat
		int exitstat = -999;

		//Run the command
		//do not need full path- just file name for the ofs commands
		//ProcessManager pm = new ProcessManager( cmd, false, 0 );
		ProcessManager pm = new ProcessManager( cmd_arr );
		try {
			pm.saveOutput( true );
			pm.run();
			esp_vect = pm.getOutputVector();
			//esp_vect = pm.runUntilFinished();
			esp_vect.insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			exitstat = pm.getExitStatus();
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				esp_vect.setElementAt( 
				"Command \"" + cmd + "\" failed", 0 );
			}	
		}
		catch (Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			if ( esp_vect == null ) {
				esp_vect = new Vector();
				esp_vect.addElement( 
				"Unable to run \"" + cmd + "\"" );
			}
		}
		pm = null;
	}
	else {
		Message.printWarning( 2, routine, 
		"The \"ESP.GUI\" file: \"" + esp_gui_file +
		"\" can not be used.  The ofs command: \"" + cmd + 
		"\" will not be run." ); 
		esp_vect = new Vector();
		esp_vect.addElement( "Unable to run \"" + cmd + "\"" );
	}

	return esp_vect;
} //end run_esp


/**
Opens up a new xterm window and runs ESPADP from there.
@return  Vector containing lines output from running command: 
"xterm -e espadp" .
*/
public static Vector run_espadp() {
	String routine = _class + ".run_espadp";

	String cmd = "xterm -e espadp"; 
	//String[] arrCmd = {"xterm -e espadp"}; 
	//Does not help the Strings if you use: +ls or -ls 
	String[] arrCmd = {"xterm -e espadp"}; 
	int exitstat = -999;
	Vector espadp_vect = null;

	ProcessManager pm = new ProcessManager( arrCmd );
	//ProcessManager pm = new ProcessManager( cmd );
	try {
		pm.saveOutput( true );
		pm.run();
		espadp_vect = pm.getOutputVector();
		//espadp_vect = pm.runUntilFinished();
		exitstat = pm.getExitStatus();
		espadp_vect.insertElementAt( "Command Run: \"" + 
			cmd + "\"", 0 );
		if ( exitstat != 0 ) {
			Message.printWarning( 
			2, routine,	
			"Unable to run espadp via command: \"" +
			cmd + "\"." );
			espadp_vect.setElementAt( 
				"Command: \"" + cmd + "\" failed", 0 );
		}
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, 
		"Unable to run espadp via command: \"" +
		cmd + "\"." );
		if ( espadp_vect == null ) {
			espadp_vect = new Vector();
			espadp_vect.addElement( 
			"Unable to run command: \"" + cmd + "\"" );
		}
		if ( Message.isDebugOn ) {
			Message.printWarning( 2, routine, e );
		}
	}
	pm = null;

	return espadp_vect;
} //end run_espadp

/**
First edits the FCEXEC.GUI file to put the selected start and run dates in.
Runs the OFS fcst command with the FCEXEC.GUI file.  Returns the vector
created running the command through the ProcessManager.
@return  Vector containing output of running the ofs command:
"ofs -p fcst -i FCEXEC.GUI -o FCEXEC.GUI.out".
*/
public static Vector run_forecast() {
	String routine = _class + ".run_forecast";

	//ofs command
	String cmd = "ofs -p fcst -i FCEXEC.GUI -o FCEXEC.GUI.out -u " +
	_output_dir;

	String[] cmd_arr = {"ofs -p fcst -i FCEXEC.GUI -o FCEXEC.GUI.out -u " + _output_dir };

	//String[] arrCmd = { "ofs", "-p", "fcst", "-i", "FCEXEC.GUI", "-o", "FCEXEC.GUI.out", "-u", _output_dir };

	//vector to hold output
	Vector fcexec_vect = null;
	
	//change start and run dates in FCEXEC.GUI
	//pass the full filename into editStartandRunDates( name );
	boolean file_edited = false;
	String fcexec_gui_file = null;
	fcexec_gui_file = IOUtil.getPropValue( "FCEXEC.GUI" );
	if ( fcexec_gui_file != null ) {
		file_edited = editStartandRunDates( fcexec_gui_file );
	}	
	if ( file_edited ) {
		//then the FCEXEC.GUI file has been edited with the new
		//dates... and the ofs command should be run.

		//exitstat
		int exitstat = -999;

		//Run the command
		//do not need full path- just file name for the ofs commands
		ProcessManager pm = new ProcessManager( cmd_arr );
		//pm.setCommandInterpreter(null);
		try {
			pm.saveOutput( true );
			pm.run();
			fcexec_vect = pm.getOutputVector();
			fcexec_vect.insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			exitstat = pm.getExitStatus();
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				fcexec_vect.setElementAt( 
				"Command \"" + cmd + "\" failed", 0 );
			}	
		}
		catch (Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			if ( fcexec_vect == null ) {
				fcexec_vect = new Vector();
				fcexec_vect.addElement( 
				"Unable to run \"" + cmd + "\"" );
			}
		}
		pm = null;
	}
	else {
		Message.printWarning( 2, routine, 
		"Start and Run dates were not edited in the FCEXEC.GUI " +
		"file so the ofs command: \"" + cmd + "\" will not be run." ); 
		fcexec_vect = new Vector();
		fcexec_vect.addElement( "Unable to run \"" + cmd + "\"" );
	}

	return fcexec_vect;

} //end run_forecast


/**
Runs the ofs command:
"ofs -p fcinit -i FCINIT.STATUS.GUI -o FCINIT.STATUS.GUI.out".
@return  Vector of line output from running:
"ofs -p fcinit -i FCINIT.STATUS.GUI -o FCINIT.STATUS.GUI.out".
*/
public static Vector run_forecastDB_status() {
	String routine = _class + ".run_forecastDB_status";

	//ofs command
	String cmd = 
	"ofs -p fcinit -i FCINIT.STATUS.GUI -o FCINIT.STATUS.GUI.out -u "+
	_output_dir;

	String[] cmd_arr = { "ofs -p fcinit -i FCINIT.STATUS.GUI -o FCINIT.STATUS.GUI.out -u "+ _output_dir };


	//vector to hold output
	Vector fcstDB_vect = null;
	
	//the ofs command should be run.
	//exitstat
	int exitstat = -999;

	//Run the command
	//do not need full path- just file name for the ofs commands
	ProcessManager pm = new ProcessManager( cmd_arr );
	try {
		pm.saveOutput( true );
		pm.run();
		fcstDB_vect = pm.getOutputVector();
		//fcstDB_vect = pm.runUntilFinished();
		fcstDB_vect.insertElementAt( 
			"Command Run: \"" + cmd + "\"", 0 );
		exitstat = pm.getExitStatus();
		if ( exitstat != 0 ) {
			Message.printWarning( 2, routine,
			"Command: \"" + cmd + "\" failed.");
			fcstDB_vect.setElementAt( 
			"Command \"" + cmd + "\" failed", 0 );
		}	
	}
	catch (Exception e) {
		Message.printWarning( 2, routine, 
		"Command: \"" + cmd + "\". FAILED. " ); 
		if ( fcstDB_vect == null ) {
			fcstDB_vect = new Vector();
			fcstDB_vect.addElement( 
			"Unable to run \"" + cmd + "\"" );
		}
	}
	pm = null;

	return fcstDB_vect;

} //end run_forecastDB_status

/**
Opens up a new xterm window and runs IFP from there.
IFP must be the user's path (.profile).
@return  Vector containing lines output from running command: 
"xterm -e IFP_Map.
*/
public static Vector run_ifp() {
	String routine = _class + ".run_ifp";

	//// A  -using XENVIRONMENT and 1 file alone, does not
	//set up all the resource files needed by IFP
	//String cmd = "XENVIRONMENT=/usr/lib/X11/app-defaults/IFP_map ; IFP_Map"; 
	//String[] arrCmd = {"XENVIRONMENT=/usr/lib/X11/app-defaults/IFP_map ; IFP_Map"}; 

	String cmd = 
	"xrdb -merge /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/Delete_atoms; " +
	"xrdb -merge /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/Forecast_Program; " +
	"xrdb -merge /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/NWSRFS_cant_run; " +
	"xrdb -merge /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/Working_Dialog; " +
	"xrdb -merge /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/Set_dates; " +
	"xrdb -merge /awips/hydroapps/lx/rfc/nwsrfs/ifp/apps-defaults/IFP_map; " +
	"IFP_Map;" +
	"xrdb -remove /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/Delete_atoms; " +
	"xrdb -remove /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/NWSRFS_cant_run; " +
	"xrdb -remove /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/Forecast_Program; " +
	"xrdb -remove /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/Working_Dialog; " +
	"xrdb -remove /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/Set_dates; " +
	"xrdb -remove /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/IFP_map"; 

	String[] arrCmd = {
	"xrdb -merge /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/Delete_atoms; " +
	"xrdb -merge /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/NWSRFS_cant_run; " +
	"xrdb -merge /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/Forecast_Program; " +
	"xrdb -merge /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/Working_Dialog; " +
	"xrdb -merge /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/Set_dates; " +
	"xrdb -merge /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/IFP_map; "+
	"IFP_Map;"+
	"xrdb -remove /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/Delete_atoms; " +
	"xrdb -remove /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/NWSRFS_cant_run; " +
	"xrdb -remove /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/Forecast_Program; " +
	"xrdb -remove /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/Working_Dialog; " +
	"xrdb -remove /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/Set_dates; " +
	"xrdb -remove /awips/hydroapps/lx/rfc/nwsrfs/ifp/app-defaults/IFP_map"}; 

	int exitstat = -999;
	Vector ifp_vect = null;

	ProcessManager pm = new ProcessManager( arrCmd );
	try {
		pm.saveOutput( true );
		pm.run();
		ifp_vect = pm.getOutputVector();
		exitstat = pm.getExitStatus();
		ifp_vect.insertElementAt( "Command Run: \"" + cmd + "\"", 0 );
		if ( exitstat != 0 ) {
			Message.printWarning( 
			2, routine,	
			"Unable to run IFP via command: \"" +
			cmd + "\"." );
			ifp_vect.setElementAt( 
				"Command: \"" + cmd + "\" failed", 0 );
		}
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, 
		"Unable to run IFP via command: \"" +
		cmd + "\"." );
		if ( ifp_vect == null ) {
			ifp_vect = new Vector();
			ifp_vect.addElement( 
			"Unable to run command: \"" + cmd + "\"" );
		}
		if ( Message.isDebugOn ) {
			Message.printWarning( 2, routine, e );
		}
	}
	pm = null;

	return ifp_vect;
} //end run_ifp


/**
Runs the ofs command:
"ofs -p ppinit -i NETWORK_ORDER.GUI -o NETWORK_ORDER.GUI.out"
@return  Vector of line output from running:
"ofs -p ppinit -i NETWORK_ORDER.GUI -o NETWORK_ORDER.GUI.out"
*/
public static Vector run_network_order() {
	String routine = _class + ".run_network_order";

	//ofs command
	String cmd = 
	"ofs -p ppinit -i NETWORK_ORDER.GUI -o NETWORK_ORDER.GUI.out -u " +
	_output_dir;

	String[] cmd_arr = {"ofs -p ppinit -i NETWORK_ORDER.GUI -o NETWORK_ORDER.GUI.out -u " + _output_dir};

	//vector to hold output
	Vector ntw_vect = null;
	
	//just try and run this command.  File should be present.
	//the ofs command should be run.
	//exitstat
	int exitstat = -999;

	//Run the command
	ProcessManager pm = new ProcessManager( cmd_arr );
	try {
		pm.saveOutput( true );
		pm.run();
		ntw_vect = pm.getOutputVector();
		//ntw_vect = pm.runUntilFinished();
		ntw_vect.insertElementAt( 
			"Command Run: \"" + cmd + "\"", 0 );
		exitstat = pm.getExitStatus();
		if ( exitstat != 0 ) {
			Message.printWarning( 2, routine,
			"Command: \"" + cmd + "\" failed.");
			ntw_vect.setElementAt( 
			"Command \"" + cmd + "\" failed", 0 );
		}	
	}
	catch (Exception e) {
		Message.printWarning( 2, routine, 
		"Command: \"" + cmd + "\". FAILED. " ); 
		if ( ntw_vect == null ) {
			ntw_vect = new Vector();
			ntw_vect.addElement( 
			"Unable to run \"" + cmd + "\"" );
		}
	}
	pm = null;

	return ntw_vect;

} //end run_network_order

/**
Runs the ofs command:
"ofs -p fcnint -i NEWRC.GUI -o NEWRC.GUI.out".
@return  Vector of line output from running:
"ofs -p fcinit -i NEWRC.GUI -o NEWRC.GUI.out".
*/
public static Vector run_newRatingCurve() {
	String routine = _class + ".run_newRatingCurve";

	//ofs command
	String cmd = "ofs -p fcinit -i NEWRC.GUI -o NEWRC.GUI.out " +
	"-u "+ _output_dir;

	String[] cmd_arr = {"ofs -p fcinit -i NEWRC.GUI -o NEWRC.GUI.out " + "-u "+ _output_dir};

	//vector to hold output
	Vector newrc_vect = null;
	
		//exitstat for ofs command
		int exitstat = -999;

		//Run the command
		//do not need full path- just file name for the ofs commands
		ProcessManager pm = new ProcessManager( cmd_arr );
		//pm.setCommandInterpreter(null);
		try {
			pm.saveOutput( true );
			pm.run();
			newrc_vect = pm.getOutputVector();
			newrc_vect.insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			exitstat = pm.getExitStatus();
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				newrc_vect.setElementAt( 
				"Command \"" + cmd + "\" failed", 0 );
			}	
		}
		catch (Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			if ( newrc_vect == null ) {
				newrc_vect = new Vector();
				newrc_vect.addElement( 
				"Unable to run \"" + cmd + "\"" );
			}
		}
		pm = null;
	return newrc_vect;
} //end run_newRatingCurve


/**
Runs the ofs command:
"ofs -p ppinit -i NEWSTATION.GUI -o NEWSTATION.GUI.out".
@return  Vector of line output from running:
"ofs -p ppinit -i NEWSTATION.GUI -o NEWSTATION.GUI.out".
*/
public static Vector run_newstation() {
	String routine = _class + ".run_newstation";

	//ofs command
	//String[] cmd_arr = {"ofs -p ppinit -i NEWSTATION.GUI -o NEWSTATION.GUI.out -u "+ _output_dir};
	/*
	String[] cmd_arr = new String[] {
	"ofs", 
	"-p", 
	"ppinit",
	"-i",
	"NEWSTATION.GUI",
	"-o",
	"NEWSTATION.GUI.out",
	"-u",
	 _output_dir
	 };
	 */

	String cmd = "ofs -p ppinit -i NEWSTATION.GUI -o NEWSTATION.GUI.out " +
	"-u "+ _output_dir;

	String[] cmd_arr = {"ofs -p ppinit -i NEWSTATION.GUI -o NEWSTATION.GUI.out " + "-u "+ _output_dir};

	//vector to hold output
	Vector newstn_vect = null;
	
	String newstn_gui_file = null;
	newstn_gui_file = IOUtil.getPropValue( "NEWSTATION.GUI" );
	if ( ( newstn_gui_file != null ) && ( 
		IOUtil.fileExists( newstn_gui_file )) ) {

		//the ofs command should be run.
		//exitstat
		int exitstat = -999;

		//Run the command
		//do not need full path- just file name for the ofs commands
		ProcessManager pm = new ProcessManager( cmd_arr );
		//pm.setCommandInterpreter(null);
		try {
			pm.saveOutput( true );
			pm.run();
			newstn_vect = pm.getOutputVector();
			newstn_vect.insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			exitstat = pm.getExitStatus();
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				newstn_vect.setElementAt( 
				"Command \"" + cmd + "\" failed", 0 );
			}	
		}
		catch (Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			if ( newstn_vect == null ) {
				newstn_vect = new Vector();
				newstn_vect.addElement( 
				"Unable to run \"" + cmd + "\"" );
			}
		}
		pm = null;
	}
	else {
		Message.printWarning( 2, routine, 
		"The \"NEWSTATION.GUI\" file: \"" + newstn_gui_file +
		"\" can not be used.  The ofs command: \"" + cmd + 
		"\" will not be run." ); 
		newstn_vect = new Vector();
		newstn_vect.addElement( "Unable to run \"" + cmd + "\"" );
	}

	return newstn_vect;
} //end run_newstation


/**
Runs the ofs command:
"ofs -p ppinit -i PPINIT.STATUS.GUI -o PPNINT.STATUS.GUI.out".
@return  Vector of line output from running:
"ofs -p ppinit -i PPINIT.STATUS.GUI -o PPNINT.STATUS.GUI.out".
*/
public static Vector run_preprocessDB_status() {
	String routine = _class + ".run_preprocessDB_status";

	//ofs command
	String cmd = 
	"ofs -p ppinit -i PPINIT.STATUS.GUI -o PPNINT.STATUS.GUI.out -u " +
	_output_dir;

	String [] cmd_arr = {"ofs -p ppinit -i PPINIT.STATUS.GUI -o PPNINT.STATUS.GUI.out -u " + _output_dir };

	//vector to hold output
	Vector preDB_vect = null;
	
	//the ofs command should be run.
	//exitstat
	int exitstat = -999;

	//Run the command
	//do not need full path- just file name for the ofs commands
	ProcessManager pm = new ProcessManager( cmd_arr );
	//pm.setCommandInterpreter(null);

	try {
		pm.saveOutput( true );
		pm.run();
		preDB_vect = pm.getOutputVector();
		preDB_vect.insertElementAt( 
			"Command Run: \"" + cmd + "\"", 0 );
		exitstat = pm.getExitStatus();
		if ( exitstat != 0 ) {
			Message.printWarning( 2, routine,
			"Command: \"" + cmd + "\" failed.");
			preDB_vect.setElementAt( 
			"Command \"" + cmd + "\" failed", 0 );
		}	
	}
	catch (Exception e) {
		Message.printWarning( 2, routine, 
		"Command: \"" + cmd + "\". FAILED. " ); 
		if ( preDB_vect == null ) {
			preDB_vect = new Vector();
			preDB_vect.addElement( 
			"Unable to run \"" + cmd + "\"" );
		}
	}
	pm = null;

	return preDB_vect;

} //end run_preprocessDB_status

/**
Called by Daily Operations- PREPROCESSORS- RUN.
Edits the PREPROCESS.GUI file by updating the RUNDATE in it.
Before this is called, the FMAPMODS.GUI should have been written out.
Even if the user does not input any FMAP data, the default is to 
write a new FMAPMODS.GUI file containing 0s.  The FMAPMODS.GUI
file is included in the PREPROCESS.GUI by the line:
".INCLUDE FMAPMODS.GUI".  Finally, the fcst PREPROCESS.GUI command is run:
"ofs -p fcst -i PREPROCESS.GUI -o PREPROCESS.GUI.out".
@return  Vector containing output of running the ofs command:
"ofs -p fcst -i PREPROCESS.GUI -o PREPROCESS.GUI.out".
*/
public static Vector run_preprocessors()  {
	String routine = _class + ".run_preprocessors";

	//ofs command
	String cmd = "ofs -p fcst -i PREPROCESS.GUI -o PREPROCESS.GUI.out -u "+
	_output_dir;

	String[] cmd_arr = {"ofs -p fcst -i PREPROCESS.GUI -o PREPROCESS.GUI.out -u "+ _output_dir};

	//vector to hold output
	Vector preproc_vect = null;

	//boolean indicates whether the ofs command should be run
	boolean ran_successfully = true;
	
	//change the RUNDATE in PREPROCESS.GUI
	//pass the full filename into editStartandRunDates( name );
	boolean file_edited = false;
	String preproc_gui_file = null;
	preproc_gui_file = IOUtil.getPropValue( "PREPROCESS.GUI" );
	if ( preproc_gui_file != null ) {
		//returns a boolean
		file_edited = editStartandRunDates( preproc_gui_file );
	}	
	else { //preproc_gui_file==null
		//unable to find PREPROCESS.GUI file
		Message.printWarning( 2, routine, 
			"Unable to locate file \"PREPROCESS.GUI\"." );
		if ( preproc_vect == null ) {
			preproc_vect = new Vector();
		}
		preproc_vect.addElement( 
			"Unable to locate file \"PREPROCESS.GUI\"." );

		ran_successfully = false;
	}
	if ( file_edited ) {
		//assume FMAPMODS file is updated

		//run the preprocessor command
		//run ofs command if ok
		int exitstat = -999;
		if ( ran_successfully ) {
			ProcessManager pm = new ProcessManager( cmd_arr );
			//pm.setCommandInterpreter(null);
			try {
				pm.saveOutput( true );
				pm.run();
				preproc_vect = pm.getOutputVector();
				//preproc_vect = pm.runUntilFinished();
				preproc_vect.insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
				exitstat = pm.getExitStatus();
				if ( exitstat != 0 ) {
					Message.printWarning( 2, routine,
					"Command: \"" + cmd + "\" failed.");
					preproc_vect.setElementAt( 
					"Command: \"" + cmd + "\" failed", 0 );
				}	
			}
			catch (Exception e) {
				Message.printWarning( 2, routine, 
				"Command: \"" + cmd + "\". FAILED. " ); 
				if ( preproc_vect == null ) {
					preproc_vect = new Vector();
				}
				preproc_vect.addElement( 
					"Unable to run \"" + cmd + "\"" );
			}
			pm = null;
		}
	}
	else { //could not edit PREPROCESS.GUI
		Message.printWarning( 2, routine,
		"Unable to edit \"PREPROCESS.GUI\" file: \"" + 
		preproc_gui_file + 
		"\".  Will not run command: \"" + cmd + "\"." );
		if ( preproc_vect == null ) {
			preproc_vect = new Vector();
		}
		preproc_vect.addElement( 
			"Unable to run Preprocessors" );

		ran_successfully = false;
	}


	return preproc_vect;
} //end run_preprocessors

////////////////
/**
Method runs one of the two the ofs commands:
"ofs -p fcinit -i PUNCHFG.GUI -o PUNCHFG.GUI.out" to
"ofs -p fcinit -i PUNCHCG.GUI -o PUNCHCG.GUI.out" 
create a display of the current ForecastGroup definition or
to create a display for the current CarryoverGroup definiitaion, using 
the _pun file output from the ofs command run. 
Creates the PUNCHFG.GUI or PUNCHCG.GUI file on the fly.
@param id  Name of forecast group or carryover group to see definition.
@param fs  File system separator.
@param punch_file  Which file to create and run: PUNCHFG or PUNCHCG.
@return  String with name of file output from running either command
"ofs -p fcinit -i PUNCHFG.GUI -o PUNCHFG.GUI.out"  or
"ofs -p fcinit -i PUNCHCG.GUI -o PUNCHCG.GUI.out" 
if command was unsucessful or null if not.
*/
public static String run_print_cgs_or_fgs( String id, 
			String fs, String punch_file ) {
	String routine = _class + ".run_print_cgs_or_fgs";
	
	//Can be set up to return the vector if we want 
	//@return - Vector containing output from running:
	//"ofs -p fcinit -i PUNCHFG.GUI -o PUNCHFG.GUI.out".

	//if punch_file is PUNCHFG- set do_fg to true,
	//if punch_file is PUNCHCG - set do_cg to true
	boolean do_fg = false;
	boolean do_cg = false;

	//ofs command
	String cmd = null;
	String[] cmd_arr = null;
	if ( punch_file.equals( "PUNCHFG" ) ) {
		do_fg = true;
	}
	if ( punch_file.equals( "PUNCHCG" ) ) {
		do_cg = true;
	}
		
	if ( do_fg ) {
		cmd = "ofs -p fcinit -i PUNCHFG.GUI -o PUNCHFG.GUI.out -u " +
		_output_dir;

		cmd_arr = new String[] {"ofs -p fcinit -i PUNCHFG.GUI -o PUNCHFG.GUI.out -u " + _output_dir};
	}
	if ( do_cg ) {
		cmd = "ofs -p fcinit -i PUNCHCG.GUI -o PUNCHCG.GUI.out -u " +
		_output_dir;

		cmd_arr = new String[] {"ofs -p fcinit -i PUNCHCG.GUI -o PUNCHCG.GUI.out -u " + _output_dir };
	}
		
	//vector to hold output
	Vector print_vect = null;

	//boolean indicates if PUNCHXG.GUI file is edited.
	boolean file_edited = true;

	//first get location of PUNCHXG.GUI
	//find path to file that we know does exist in that same
	// ../fcnint/ directory
	String path_to_fcinit_seg = null;
	path_to_fcinit_seg = IOUtil.getPropValue( "RESEGDEF.GUI" );
	StringBuffer b = new StringBuffer();
	if (( path_to_fcinit_seg != null ) && ( 
		IOUtil.fileExists( path_to_fcinit_seg ) )) {
		//cut off file name (last item)
		Vector v = null;
		v = StringUtil.breakStringList( path_to_fcinit_seg,
			fs, StringUtil.DELIM_SKIP_BLANKS );
		int size = 0;
		if ( v !=null ) {
			size = v.size();
		}
		for ( int i=0; i<size-1; i++ ) {
			b.append( (String) v.elementAt(i) + fs );
		}
		v = null;
	}
	//now should have path to fcinit dir, add PUNCHSEGS.GUI here
	String print_path = null;
	if ( do_fg ) {
		print_path = fs + b.toString() + "PUNCHFG.GUI";
	}
	if ( do_cg ) {
		print_path = fs + b.toString() + "PUNCHCG.GUI";
	}
	//clean up 
	b = null;

	if ( print_path == null ) {
		Message.printWarning( 2, routine,
		"Unable to find location for " +
		"\"PUNCH\" file." );

		file_edited = false;
	}
	
	if ( file_edited ) {
		//really try to create file now.

		//model output going to file
		File outputFile = null;
		FileOutputStream fos;
		PrintWriter pw;
		//NEED TO CREATE THIS FILE - do not assume it is there
		outputFile = new File( print_path );
		try {
			if ( Message.isDebugOn ) {
				Message.printDebug( 15, routine,
				"Creating file: \"" + print_path + "\"." );	
			}
			fos = new FileOutputStream( outputFile );
	
			//write
			pw = new PrintWriter( fos );
			if ( do_fg ) {
				pw.println( "PUNCHFG" );	
				pw.flush(); 
			}
			if( do_cg ) {
				pw.println( "PUNCHCG" );	
				pw.flush(); 
			}
			pw.println( "ID " + id );	
			pw.flush(); 
			pw.println( "END" );
			pw.flush(); 

			pw.close();	
		}	
		catch (Exception ioe ) {
			//gets caught
			//even when does edit the file
		}

		fos = null; 
		pw = null;
		outputFile = null;
	}

	//now run the ofs command
	//do not need full path- just file name for the ofs commands
	int exitstat = -99;
	//if the file is created (ie, edited)
	if ( file_edited ) {
		ProcessManager pm = new ProcessManager( cmd_arr );
		//pm.setCommandInterpreter(null);
		try {
			pm.saveOutput( true );
			pm.run();
			print_vect = pm.getOutputVector();
			//print_vect = pm.runUntilFinished();
			exitstat = pm.getExitStatus();
			print_vect.insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				print_vect.setElementAt( 
				"Command \"" + cmd + "\" failed", 0 );
			
			 file_edited = false;
			}	
		}	
		catch (Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			if ( print_vect == null ) {
				print_vect = new Vector();
				print_vect.addElement( 
				"Unable to run \"" + cmd + "\"" );
			}
		}
		pm = null;
	}
	else {
		//don't run ofs command b/c file was not edited.
	}	

	//if we do have an output panel to put this info in, 
	//could return the vector printsegs_vect at this point.

	if ( file_edited ) {
		//then get the string from the output that 
		//starts with "==>" that indicates path of output file.
		int size = 0;
		if ( print_vect != null ) {
			size = print_vect.size();
		}
		String output_file = null;
		String fcinit_file = null;
		String s = null;
		Vector v = null;
		for ( int i=0; i<size; i++ ) {
			s = ((String)print_vect.elementAt( i )).trim();
			//line with output file name:
			//has this in it:
			//"==> output_file_name <=="
			if ( s.startsWith("==") ) {
				v = StringUtil.breakStringList(
					s, " ", StringUtil.DELIM_SKIP_BLANKS );
				//should be 3 pieces -we need middle one.
				if ( v.size() == 3 ) {
					fcinit_file = (String)v.elementAt(1);

					//now we have the output_log file:
					//ex, .../fcinit_log.20020314.132919 

					//need to replace "fcninit_log" with
					//PUNCHFG.GUI.out 
					//(keep timestamp)
//REPLACE with fcinit_pun ( not PUNCHFG.out )
					
					//get index of "fcinit_log"
					int index = -999;
					index = fcinit_file.indexOf(
					"fcinit_log" );
					if ( index > 0 ) {
						if ( do_fg ) {
							output_file = 
							( fcinit_file.
							substring( 
							0, index ) + 
							"fcinit_pun." +
							fcinit_file.substring( 
							index+11) );
			/*
							output_file = 
							( fcinit_file.
							substring( 
							0, index ) + 
							"PUNCHFG.GUI.out." +
							fcinit_file.substring( 
							index+11) );
			*/

						}
						if ( do_cg ) {
							output_file = 
							( fcinit_file.
							substring( 
							0, index ) + 
							"fcinit_pun." +
							fcinit_file.substring( 
							index+11) );
			/*
							output_file = 
							( fcinit_file.
							substring( 
							0, index ) + 
							"PUNCHCG.GUI.out." +
							fcinit_file.substring( 
							index+11) );
			*/
						}

					}
					else {
						Message.printWarning(
						2, routine,
						"Unable to find output file: "+
						"from running command: \"" +
						cmd + "\"." );
					}
					break;
				}
			}		
		}	
		//now have output file which in fact, is 
		//the fcinit_log file.  Use this to find the 
		//PUNCHFG.GUI.out. Find the 
		//PUNCHFG.GUI.out using the path and timestamp
		//from this file.

		return output_file;
	}
	else {
		return null;
	}
}  //end run_print_cgs_fgs


/**
Finds the PRINTRC.GUI file and edits it by updating the 
rating curve  name with the rating curve that is currently selected on the
rating curve JTree.  Runs the ofs command:
"ofs -p fcinit -i PRINTRC.GUI -o PRINTRC.GUI.out".
@param ratingCurve_id  Rating Curve id to place in the PRINTRC.GUI file.
@return  String with name of file output from running command
	"ofs -p fcinit -i PRINTRC.GUI -o PRINTRC.GUI.out" or null,
	if command was unsucessful.
*/
public static String run_print_ratingCurves( String ratingCurve_id ) {
	String routine = _class + ".run_print_ratingCurves";

	//ofs command
	String cmd = 
	"ofs -p fcinit -i PRINTRC.GUI -o PRINTRC.GUI.out -u "+ _output_dir;

	String[] cmd_arr = { "ofs -p fcinit -i PRINTRC.GUI -o PRINTRC.GUI.out -u "+ _output_dir};

	//vector to hold output of running ofs command
	Vector printrc_vect = null;

	//boolean to indicate if ofs command should be run- it will
	//not be run if the editing of the file fails.
	boolean file_edited = true;

	//First edit PRINTTC.GUI file
	//get path to PRINTTC.GUI
	String print_rc_path = null;
	print_rc_path = IOUtil.getPropValue( "PRINTRC.GUI" );

	//first we need to edit/update PRINTRC.GUI file 
	if ( print_rc_path != null ) {
		//now we just need to read and write the file 
		//with the new stn id model file input
		File inputFile = null;
		FileInputStream fis;
		InputStreamReader isr;
		BufferedReader br;

		//model output going to file
		File outputFile = null;
		FileOutputStream fos;
		PrintWriter pw;
		
		String s = null;
		inputFile = new File( print_rc_path );
		outputFile = new File( print_rc_path + ".tmp" );
		
		//if the file is a readable file
		if ( IOUtil.fileReadable( print_rc_path ) ) {
			try {
				fis = new FileInputStream( inputFile );
				isr = new InputStreamReader ( fis );
				br = new BufferedReader ( isr );

				fos = new FileOutputStream( outputFile );
				pw = new PrintWriter( fos );
				
				do {
					s = br.readLine();
					//change line that starts with "ID"
					//need to change the Rating Curve id
					if ( s.trim().regionMatches(true, 0,
						"ID",0,2 )) {
						s = "ID " + ratingCurve_id;
					}

					if ( s == null ) {
						//no more lines
						break;
					}

					pw.println(s );	
					pw.flush(); 
				} 
				while ( s != null );
			
				br.close();
				pw.close();	

				//clean up
				fis = null;
				isr = null;
				br = null;
				s = null;

			}	
			catch (Exception e ) {
				//use debug here, not warning
				//b/c prints this message even
				//when does edit the file
				if (Message.isDebugOn) {
					Message.printDebug( 2, routine, 
					"Unable to open: \"" +
					print_rc_path + "\" for editing." );
				}
			}
			//now we have created an extra file.  
			//Move it back to original name, ie.  
			//PRINTRC.GUI.tmp moved to 
			//PRINTRC.GUI
			if ( outputFile.canRead() ) {
				try {
					outputFile.renameTo( inputFile );
				}		
				catch ( Exception e ) {
					Message.printWarning( 2, routine,
					"Unable to edit file \"" +
					print_rc_path + "\"." );
					if ( Message.isDebugOn ){
						Message.printDebug( 4, routine,
						"Unable to move: \"" + 
						print_rc_path + 
						".tmp\" back to: "+
						print_rc_path + "\"." );
				
						Message.printWarning( 2, 
						routine, e );
					}
						file_edited = false;
				}
			}
			else { //can't read .tmp file
				file_edited = false;
			}

		} //end if fileReadable
		else {
			Message.printWarning( 2, routine,
			"Unable to edit file: \"" +
			print_rc_path + "\"." );

			file_edited = false;
		}

	}
	else { //  print_rc_path == null 
		Message.printWarning( 2, routine, 
		"Unable to locate \"PRINTRC.GUI\" file for editing." );	

		file_edited = false;
	}

	//now that the file has been updated, run the PRINTRC command
	int exitstat = -99;
	if ( file_edited ) {
		ProcessManager pm = new ProcessManager( cmd_arr );
		//pm.setCommandInterpreter(null);
		try {
			pm.saveOutput( true );
			pm.run();
			printrc_vect = pm.getOutputVector();
			exitstat = pm.getExitStatus();
			printrc_vect.insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				printrc_vect.setElementAt( 
				"Command \"" + cmd + "\" failed", 0 );
			
			 file_edited = false;
			}	
		}	
		catch (Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			if ( printrc_vect == null ) {
				printrc_vect = new Vector();
				printrc_vect.addElement( 
				"Unable to run \"" + cmd + "\"" );
			}
		}
		pm = null;
	}
	else {
		//don't run ofs command b/c file was not edited.
	}
	if ( file_edited ) {
		//then get the string from the output that 
		//starts with "==>" that indicates path of output file.
		int size = 0;
		if ( printrc_vect != null ) {
			size = printrc_vect.size();
		}
		String output_file = null;
		String fcinit_file = null;
		String s = null;
		Vector v = null;
		for ( int i=0; i<size; i++ ) {
			s = ((String)printrc_vect.elementAt( i )).trim();
			//line with output file name:
			//has this in it: "The output file"
			//"==> output_file_name <=="
			if ( s.startsWith("==") ) {
				v = StringUtil.breakStringList(
					s, " ", StringUtil.DELIM_SKIP_BLANKS );
				//should be 3 pieces -we need middle one.
				if ( v.size() == 3 ) {
					fcinit_file = (String)v.elementAt(1);

					//now we have the output_log file:
					//ex, .../fcinit_log.20020314.132919 
					//need to replace "fcninit_log" with
					//PRINTRC.GUI.out (keep timestamp)
					
					//get index of "fcinit_log"
					int index = -999;
					index = fcinit_file.indexOf(
					"fcinit_log" );
					if ( index > 0 ) {
						output_file = 
						( fcinit_file.substring( 
						0, index ) + 
						"PRINTRC.GUI.out." +
						fcinit_file.substring( 
						index+11) );

					}
					else {
						Message.printWarning(
						2, routine,
						"Unable to find output file: "+
						"from running command: \"" +
						cmd + "\"." );
					}
					break;
				}
			}		
		}	
		//now have output file which in fact, is 
		//the fcinit_log file.  Use this to find the PRINTRC.GUI.out
		//find the PRINTRC.GUI.out using the path and timestamp
		//from this file.

		return output_file;
	}
	else {
		return null;
	}

	
} //end run_print_ratingCurves

//////////////////

/**
Finds the PRINTSEGS.GUI file and edits it by updating the 
segment name with the segment that is currently selected on the
JTree.  Runs the ofs command:
"ofs -p fcinit -i PRINTSEGS.GUI -o PRINTSEGS.GUI.out".
@param segment_id  Segment id to place in the PRINTSEGS.GUI file.
@return  String with name of file output from running command
	"ofs -p fcinit -i PRINTSEGS.GUI -o PRINTSEGS.GUI.out" or null,
	if command was unsucessful.
*/
public static String run_print_segs( String segment_id ) {
	String routine = _class + ".run_print_segs";
	
	/*
	Can be set up to return the vector if we want 
	@return - Vector containing output from running:
	"ofs -p fcinit -i PRINTSEGS.GUI -o PRINTSEGS.GUI.out".
	*/

	//ofs command
	String cmd = 
	"ofs -p fcinit -i PRINTSEGS.GUI -o PRINTSEGS.GUI.out -u " + 
	_output_dir;

	String[] cmd_arr = {"ofs -p fcinit -i PRINTSEGS.GUI -o PRINTSEGS.GUI.out -u " + _output_dir };
		
	//vector to hold output
	Vector printsegs_vect = null;

	//boolean indicates if PRINTSEGS.GUI file is edited.
	boolean file_edited = true;

	//first get location of PRINTSEGS.GUI
	String printsegs_path = null;	
	printsegs_path = IOUtil.getPropValue( "PRINTSEGS.GUI" );

	//can't be null or GUI would not have started
	if ( printsegs_path == null ) {
		Message.printWarning( 2, routine,
		"Unable to retrieve location for " +
		"\"PRINTSEGS.GUI\" file. Can not show " +
		"current Segment Definition." );

		file_edited = false;
	}
	else {
		//make sure is readable
		if ( !IOUtil.fileExists( printsegs_path ) ) {
			Message.printWarning( 2, routine,
			"Unable to read \"PRINTSEGS.GUI\" " +
			"file: \"" + printsegs_path + "\"." );

			file_edited = false;
		}		
	}
	
	if ( file_edited ) {
		//really try and edit file now.

		//model file input
		File inputFile = null;
		FileInputStream fis;
		InputStreamReader isr;
		BufferedReader br;

		//model output going to file
		File outputFile = null;
		FileOutputStream fos;
		PrintWriter pw;
		
		String s = null;
		inputFile = new File( printsegs_path );
		outputFile = new File( printsegs_path + ".tmp" );
		
		try {
			if ( Message.isDebugOn ) {
				Message.printDebug( 15, routine,
				"Editing file: \"" + printsegs_path + "\"." );	
			}
			fis = new FileInputStream( inputFile );
			isr = new InputStreamReader ( fis );
			br = new BufferedReader ( isr );

			fos = new FileOutputStream( outputFile );
			pw = new PrintWriter( fos );
			
			do {
				s = br.readLine();
				//change line that contains: SEGS,
				//SEGMENTS, SEGMENT, S
				if ( (s.trim().regionMatches(true,
					0, "SEGS", 0, 4 )) ||
					(s.trim().regionMatches(true,
					0, "SEGMENTS", 0, 8 )) ||
					(s.trim().regionMatches(true,
					0, "SEGMENT", 0, 7 )) ||
					(s.equals("S") ) ) { 
					
					s="SEGS " + segment_id;
				}
				if ( s == null ) {
					//no more lines
					break;
				}

				pw.println(s );	
				pw.flush(); 
				
			} 
			while ( s != null );

			br.close();
			pw.close();	

			//clean up
			fis = null;
			isr = null;
			br = null;

		}	
		catch (Exception ioe ) {
			//gets caught
			//even when does edit the file
		}

		//now we have created an extra file.  Move it back
		//to original name, ie.  
		try {
			outputFile.renameTo( inputFile );
		}		
		catch ( Exception secex ) {
			file_edited = false;

			Message.printWarning( 2, routine,
			"Unable to edit file: \"" +
			printsegs_path + "\"." );
			if ( Message.isDebugOn ) {
				Message.printWarning( 2, routine,
				"tmp file: \"" + printsegs_path + 
				".tmp\" could not " +
				"be moved back to: \"" + printsegs_path + 
				"\"." ); 
			}
		}

		//clean up
		fos = null; 
		pw = null;
		s = null;
		outputFile = null;
		inputFile = null;
	}

	//now run the ofs command
	//do not need full path- just file name for the ofs commands
	int exitstat = -99;
	if ( file_edited ) {
		ProcessManager pm = new ProcessManager( cmd_arr );
		//pm.setCommandInterpreter(null);
		try {
			pm.saveOutput( true );
			pm.run();
			printsegs_vect = pm.getOutputVector();
			exitstat = pm.getExitStatus();
			printsegs_vect.insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				printsegs_vect.setElementAt( 
				"Command \"" + cmd + "\" failed", 0 );
			
			 file_edited = false;
			}	
		}	
		catch (Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			if ( printsegs_vect == null ) {
				printsegs_vect = new Vector();
				printsegs_vect.addElement( 
				"Unable to run \"" + cmd + "\"" );
			}
		}
		pm = null;
	}
	else {
		//don't run ofs command b/c file was not edited.
	}	

	//if we do have an output panel to put this info in, 
	//could return the vector printsegs_vect at this point.

	if ( file_edited ) {
		//then get the string from the output that 
		//starts with "==>" that indicates path of output file.
		int size = 0;
		if ( printsegs_vect != null ) {
			size = printsegs_vect.size();
		}
		String output_file = null;
		String fcinit_file = null;
		String s = null;
		Vector v = null;
		for ( int i=0; i<size; i++ ) {
			s = ((String)printsegs_vect.elementAt( i )).trim();
			//line with output file name:
			//has this in it: "The output file"
			//"==> output_file_name <=="
			if ( s.startsWith("==") ) {
				v = StringUtil.breakStringList(
					s, " ", StringUtil.DELIM_SKIP_BLANKS );
				//should be 3 pieces -we need middle one.
				if ( v.size() == 3 ) {
					fcinit_file = (String)v.elementAt(1);

					//now we have the output_log file:
					//ex, .../fcinit_log.20020314.132919 
					//need to replace "fcninit_log" with
					//PRINTSEGS.GUI.out (keep timestamp)
					
					//get index of "fcinit_log"
					int index = -999;
					index = fcinit_file.indexOf(
					"fcinit_log" );
					if ( index > 0 ) {
						output_file = 
						( fcinit_file.substring( 
						0, index ) + 
						"PRINTSEGS.GUI.out." +
						fcinit_file.substring( 
						index+11) );

					}
					else {
						Message.printWarning(
						2, routine,
						"Unable to find output file: "+
						"from running command: \"" +
						cmd + "\"." );
					}
				
					break;
				}
			}		
		}	
		//now have output file which in fact, is 
		//the fcinit_log file.  Use this to find the PRINTSEGS.GUI.out
		//find the PRINTSEGS.GUI.out using the path and timestamp
		//from this file.

		return output_file;
	}
	else {
		return null;
	}

}  //end run_print_segs

///////////////////////////////
/**
Edits the PUNCHRC.GUI file to replace the rating curve ID found
in the third line of that file with the Rating Curve ID passed in to this
method.  Once the file is edited, the ofs command is run
"ofs -p fcinit -i PUNCHRC.GUI -o PUNCHRC.GUI.out".
@param ratingCurve_id  Rating Curve ID to place in the PUNCHRC.GUI file.
@return vector containing the output from running command:
"ofs -p fcinit -i PUNCHRC.GUI -o PUNCHRC.GUI.out".
*/
public static Vector run_punch_ratingCurves( String ratingCurve_id ) {
	String routine = _class + ".run_punch_ratingCurves";

	//ofs command
	String cmd = 
	"ofs -p fcinit -i PUNCHRC.GUI -o PUNCHRC.GUI.out -u " +
	_output_dir;

	String[] cmd_arr = { "ofs -p fcinit -i PUNCHRC.GUI -o PUNCHRC.GUI.out -u " + _output_dir};

	//vector to hold output
	Vector punch_vect = null;

	//boolean to indicate if ofs command should be run- it will
	//not be run if the editing of the file fails.
	boolean file_edited = true;

	//First edit PUNCHRC.GUI file
	//get path to PUNCHRC.GUI
	String punch_rc_path = null;
	punch_rc_path = IOUtil.getPropValue( "PUNCHRC.GUI" );

	if ( punch_rc_path != null ) {
		//now we just need to read and write the file 
		//with the new stn id model file input
		File inputFile = null;
		FileInputStream fis;
		InputStreamReader isr;
		BufferedReader br;

		//model output going to file
		File outputFile = null;
		FileOutputStream fos;
		PrintWriter pw;
		
		String s = null;
		inputFile = new File( punch_rc_path );
		outputFile = new File( punch_rc_path + ".tmp" );
		
		//if the file is a readable file
		if ( IOUtil.fileReadable( punch_rc_path ) ) {
			try {
				fis = new FileInputStream( inputFile );
				isr = new InputStreamReader ( fis );
				br = new BufferedReader ( isr );

				fos = new FileOutputStream( outputFile );
				pw = new PrintWriter( fos );
				
				do {
					s = br.readLine();
					//change line that starts with "ID"
					//need to change the Rating Curve id
					if ( s.trim().regionMatches(true, 0,
						"ID",0,2 )) {
						s = "ID " + ratingCurve_id;
					}

					if ( s == null ) {
						//no more lines
						break;
					}

					pw.println(s );	
					pw.flush(); 
				
				} 
				while ( s != null );
			
				br.close();
				pw.close();	

				//clean up
				fis = null;
				isr = null;
				br = null;
				s = null;

			}	
			catch (Exception e ) {
				//use debug here, not warning
				//b/c prints this message even
				//when does edit the file
				if (Message.isDebugOn) {
					Message.printDebug( 2, routine, 
					"Unable to open: \"" +
					punch_rc_path + "\" for editing." );
				}
			}
			//now we have created an extra file.  
			//Move it back to original name, ie.  
			//PUNCHRC.GUI.tmp moved to 
			//PUNCHRC.GUI
			if ( outputFile.canRead() ) {
				try {
					outputFile.renameTo( inputFile );
				}		
				catch ( Exception e ) {
					Message.printWarning( 2, routine,
					"Unable to edit file \"" +
					punch_rc_path + "\"." );
					if ( Message.isDebugOn ){
						Message.printDebug( 4, routine,
						"Unable to move: \"" + 
						punch_rc_path + 
						".tmp\" back to: "+
						punch_rc_path + "\"." );
				
						Message.printWarning( 2, 
						routine, e );
					}
						file_edited = false;
				}
			}
			else { //can't read .tmp file
				file_edited = false;
			}

		} //end if fileReadable
		else {
			Message.printWarning( 2, routine,
			"Unable to edit file: \"" +
			punch_rc_path + "\"." );

			file_edited = false;
		}

	}
	else { //  punch_rc_path == null 
		Message.printWarning( 2, routine, 
		"Unable to locate \"PUNCHRC.GUI\" file for editing." );	

		file_edited = false;
	}


	//now Rating Curve ID should have been changed in PUNCHRC.GUI
	if ( file_edited ) {
		//then the PUNCHRC.GUI file has been edited with the new
		//rating curve ID... and the ofs command should be run.

		//exitstat
		int exitstat = -999;

		//Run the command
		//do not need full path- just file name for the ofs commands
		ProcessManager pm = new ProcessManager( cmd_arr );
		//pm.setCommandInterpreter(null);
		try {
			pm.saveOutput( true );
			pm.run();
			punch_vect = pm.getOutputVector();
			//punch_vect = pm.runUntilFinished();
			punch_vect.insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			exitstat = pm.getExitStatus();
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				punch_vect.setElementAt( 
				"Command: \"" + cmd + "\" failed", 0 );
			}	
		}
		catch (Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			if ( punch_vect == null ) {
				punch_vect = new Vector();
				punch_vect.addElement( 
				"Unable to run \"" + cmd + "\"" );
			}
		}
		pm = null;
	}
	else {
		Message.printWarning( 2, routine, 
		"Rating Curve ID was not edited in the PUNCHRC.GUI " +
		"file so the ofs command: \"" + cmd + "\" will not be run." ); 
		punch_vect = new Vector();
		punch_vect.addElement( "Unable to run \"" + cmd + "\"" );
	}

	return punch_vect;
} //end run_punch_ratingCurves

///////////////////////////


/**
Edits the PUNCHSEGS.GUI file to replace the segment ID found
in the third line of that file with the Segment ID passed in to this
method.  Once the file is edited, the ofs command is run
"ofs -p fcinit -i PUNCHSEGS.GUI -o PUNCHSEGS.GUI.out".
@param segment_id  Station ID to place in the PUNCHSEGS.GUI file.
@return vector containing the output from running command:
"ofs -p fcinit -i PUNCHSEGS.GUI -o PUNCHSEGS.GUI.out".
*/
public static Vector run_punch_segments( String segment_id ) {
	String routine = _class + ".run_punch_segments";

	//ofs command
	String cmd = 
	"ofs -p fcinit -i PUNCHSEGS.GUI -o PUNCHSEGS.GUI.out -u " + 
	_output_dir;

	String[] cmd_arr = { "ofs -p fcinit -i PUNCHSEGS.GUI -o PUNCHSEGS.GUI.out -u " + _output_dir };

	//vector to hold output
	Vector punch_vect = null;

	//boolean to indicate if ofs command should be run- it will
	//not be run if the editing of the file fails.
	boolean file_edited = true;

	//First edit PUNCHSEGS.GUI file
	//get path to PUNCHSEGS.GUI
	String punch_segs_path = null;
	punch_segs_path = IOUtil.getPropValue( "PUNCHSEGS.GUI" );

	if ( punch_segs_path != null ) {
		//now we just need to read and write the file 
		//with the new stn id model file input
		File inputFile = null;
		FileInputStream fis;
		InputStreamReader isr;
		BufferedReader br;

		//model output going to file
		File outputFile = null;
		FileOutputStream fos;
		PrintWriter pw;
		
		String s = null;
		inputFile = new File( punch_segs_path );
		outputFile = new File( punch_segs_path + ".tmp" );
		
		//if the file is a readable file
		if ( IOUtil.fileReadable( punch_segs_path ) ) {
			try {
				fis = new FileInputStream( inputFile );
				isr = new InputStreamReader ( fis );
				br = new BufferedReader ( isr );

				fos = new FileOutputStream( outputFile );
				pw = new PrintWriter( fos );
				
				do {
					s = br.readLine();
					//change line that contains:
					// "SEGMENTS"
					//need to change the Segment id
					if ( s.trim().regionMatches(true, 0,
						"SEGMENTS",0,8 )) {
						s = "SEGMENTS " + segment_id;
					}

					if ( s == null ) {
						//no more lines
						break;
					}

					pw.println(s );	
					pw.flush(); 
				
				} 
				while ( s != null );
			
				br.close();
				pw.close();	

				//clean up
				fis = null;
				isr = null;
				br = null;
				s = null;

			}	
			catch (Exception e ) {
				//use debug here, not warning
				//b/c prints this message even
				//when does edit the file
				if (Message.isDebugOn) {
					Message.printDebug( 2, routine, 
					"Unable to open: \"" +
					punch_segs_path + "\" for editing." );
				}
			}
			//now we have created an extra file.  
			//Move it back to original name, ie.  
			//PUNCHSEGS.GUI.tmp moved to 
			//PUNCHSEGS.GUI
			if ( outputFile.canRead() ) {
				try {
					outputFile.renameTo( inputFile );
				}		
				catch ( Exception e ) {
					Message.printWarning( 2, routine,
					"Unable to edit file \"" +
					punch_segs_path + "\"." );
					if ( Message.isDebugOn ){
						Message.printDebug( 4, routine,
						"Unable to move: \"" + 
						punch_segs_path + 
						".tmp\" back to: "+
						punch_segs_path + "\"." );
				
						Message.printWarning( 2, 
						routine, e );
					}
						file_edited = false;
				}
			}
			else { //can't read .tmp file
				file_edited = false;
			}

		} //end if fileReadable
		else {
			Message.printWarning( 2, routine,
			"Unable to edit file: \"" +
			punch_segs_path + "\"." );

			file_edited = false;
		}

	}
	else { //  punch_segs_path == null 
		Message.printWarning( 2, routine, 
		"Unable to locate \"PUNCHSEGS.GUI\" file for editing." );	

		file_edited = false;
	}


	//now Station ID should have been changed in PUNCHSEGS.GUI
	if ( file_edited ) {
		//then the PUNCHSEGS.GUI file has been edited with the new
		//station ID... and the ofs command should be run.

		//exitstat
		int exitstat = -999;

		//Run the command
		//do not need full path- just file name for the ofs commands
		ProcessManager pm = new ProcessManager( cmd_arr );
		//pm.setCommandInterpreter(null);
		try {
			pm.saveOutput( true );
			pm.run();
			punch_vect = pm.getOutputVector();
			//punch_vect = pm.runUntilFinished();
			punch_vect.insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			exitstat = pm.getExitStatus();
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				punch_vect.setElementAt( 
				"Command: \"" + cmd + "\" failed", 0 );
			}	
		}
		catch (Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			if ( punch_vect == null ) {
				punch_vect = new Vector();
				punch_vect.addElement( 
				"Unable to run \"" + cmd + "\"" );
			}
		}
		pm = null;
	}
	else {
		Message.printWarning( 2, routine, 
		"Station ID was not edited in the PUNCHSEGS.GUI " +
		"file so the ofs command: \"" + cmd + "\" will not be run." ); 
		punch_vect = new Vector();
		punch_vect.addElement( "Unable to run \"" + cmd + "\"" );
	}

	return punch_vect;
} //end run_punch_segments


/**
Edits the PUNCH.STATIONS.GUI file to replace the station ID found
in the first line of that file with the Station ID passed in to this
method.  Once the file is edited, the ofs command is run:
"ofs -p ppinit -i PUNCH.STATIONS.GUI -o PUNCH.STATIONS.GUI.out".
@param station_id  Station ID to place in the PUNCH.STATIONS.GUI file.
@return vector containing the output from running command:
"ofs -p ppinit -i PUNCH.STATIONS.GUI -o PUNCH.STATIONS.GUI.out".
*/
public static Vector run_punch_stations( String station_id ) {
	String routine = _class + ".run_punch_stations";

	//units to use in input file
	String units = null;
	units = IOUtil.getPropValue( "UNITS" );
	if ( units == null ) {
		units = "METR";
	}

	//ofs command
	String cmd = 
	"ofs -p ppinit -i PUNCH.STATIONS.GUI -o PUNCH.STATIONS.GUI.out -u " +
	_output_dir;

	String[] cmd_arr = { "ofs -p ppinit -i PUNCH.STATIONS.GUI -o PUNCH.STATIONS.GUI.out -u " + _output_dir };

	//vector to hold output
	Vector punch_vect = null;

	//boolean to indicate if ofs command should be run- it will
	//not be run if the editing of the file fails.
	boolean file_edited = true;

	//First edit PUNCH.STATIONS.GUI file
	//get path to PUNCH.STATIONS.GUI
	String punch_stns_path = null;
	punch_stns_path = IOUtil.getPropValue( "PUNCH.STATIONS.GUI" );

	if ( punch_stns_path != null ) {
		//now we just need to read and write the file 
		//with the new stn id model file input
		File inputFile = null;
		FileInputStream fis;
		InputStreamReader isr;
		BufferedReader br;

		//model output going to file
		File outputFile = null;
		FileOutputStream fos;
		PrintWriter pw;

		if (Message.isDebugOn) {
			Message.printDebug(5, "", "Punch station file path: " 
				+ punch_stns_path);
		}
		
		String s = null;
		inputFile = new File( punch_stns_path );
		outputFile = new File( punch_stns_path + ".tmp" );
		
		//if the file is a readable file
		if ( IOUtil.fileReadable( punch_stns_path ) ) {
			try {
				fis = new FileInputStream( inputFile );
				isr = new InputStreamReader ( fis );
				br = new BufferedReader ( isr );

				fos = new FileOutputStream( outputFile );
				pw = new PrintWriter( fos );
				
				do {
					s = br.readLine();
					//change first line 
					//it contains:
					//DUMP PUNCH UNITS(ENGL) STATION
					//STATIONID PCPN RRS 
					//CURRENTLY WE DO NOT WANT TO USE
					//the parameters PCPN RRS
					//need to change the StationID
					if ( s.trim().regionMatches(true, 0,
						"@DUMP",0,4 )) {
						s = "@DUMP PUNCH UNITS(" +units+") "+
							"STATION ALLPARM " + 
							station_id;
					}

					if ( s == null ) {
						//no more lines
						break;
					}

					pw.println(s );	
					pw.flush(); 
				
				} 
				while ( s != null );
			
				br.close();
				pw.close();	

				//clean up
				fis = null;
				isr = null;
				br = null;
				s = null;

			}	
			catch (Exception e ) {
				//use debug here, not warning
				//b/c prints this message even
				//when does edit the file
				if (Message.isDebugOn) {
					Message.printDebug( 2, routine, 
					"Unable to open: \"" +
					punch_stns_path + "\" for editing." );
				}
			}
			//now we have created an extra file.  
			//Move it back to original name, ie.  
			//PUNCH.STATION.GUI.tmp moved to 
			//PUNCH.STATION.GUI
			if ( outputFile.canRead() ) {
				try {
					outputFile.renameTo( inputFile );
				}		
				catch ( Exception e ) {
					Message.printWarning( 2, routine,
					"Unable to edit file \"" +
					punch_stns_path + "\"." );
					if ( Message.isDebugOn ){
						Message.printDebug( 4, routine,
						"Unable to move: \"" + 
						punch_stns_path + 
						".tmp\" back to: "+
						punch_stns_path + "\"." );
				
						Message.printWarning( 2, 
						routine, e );
					}
						file_edited = false;
				}
			}
			else { //can't read .tmp file
				file_edited = false;
			}

		} //end if fileReadable
		else {
			Message.printWarning( 2, routine,
			"Unable to edit file: \"" +
			punch_stns_path + "\"." );

			file_edited = false;
		}

	}
	else { //  punch_stns_path == null 
		Message.printWarning( 2, routine, 
		"Unable to locate \"PUNCH.STATIONS.GUI\" file for editing." );	

		file_edited = false;
	}

	//now Station ID should have been changed in PUNCH.STATIONS.GUI
	if ( file_edited ) {
		//then the PUNCH.STATIONS.GUI file has been edited with the new
		//station ID... and the ofs command should be run.

		//exitstat
		int exitstat = -999;

		//Run the command
		//do not need full path- just file name for the ofs commands
		ProcessManager pm = new ProcessManager( cmd_arr );
		//pm.setCommandInterpreter(null);
		try {
			pm.saveOutput( true );
			pm.run();
			punch_vect = pm.getOutputVector();
			//punch_vect = pm.runUntilFinished();
			punch_vect.insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			exitstat = pm.getExitStatus();
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				punch_vect.setElementAt( 
				"Command: \"" + cmd + "\" failed", 0 );
			}	
		}
		catch (Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			if ( punch_vect == null ) {
				punch_vect = new Vector();
				punch_vect.addElement( 
				"Unable to run \"" + cmd + "\"" );
			}
		}
		pm = null;
	}
	else {
		Message.printWarning( 2, routine, 
		"Station ID was not edited in the PUNCH.STATIONS.GUI " +
		"file so the ofs command: \"" + cmd + "\" will not be run." ); 
		punch_vect = new Vector();
		punch_vect.addElement( "Unable to run \"" + cmd + "\"" );
	}

	return punch_vect;
} //end run_punch_stations

////////////////////
/**
Runs the ofs command:
"ofs -p fcinit -i DEFRC.GUI -o DEFRC.GUI.out" 
@return Vector containing output of running the ofs command:
"ofs -p fcinit -i DEFRC.GUI -o DEFRC.GUI.out" 
*/
public static Vector run_redefine_ratingCurves() {
	String routine = _class + ".run_redefine_ratingCurves";

	//ofs command
	String cmd = 
	"ofs -p fcinit -i DEFRC.GUI -o DEFRC.GUI.out -u " + 
	_output_dir;

	String[] cmd_arr = { "ofs -p fcinit -i DEFRC.GUI -o DEFRC.GUI.out -u " + _output_dir };

	//vector to hold output
	Vector redefrc_vect = null;
	
		//exitstat
		int exitstat = -999;

		//Run the command
		//do not need full path- just file name for the ofs commands
		ProcessManager pm = new ProcessManager( cmd_arr );
		//pm.setCommandInterpreter(null);
		try {
			pm.saveOutput( true );
			pm.run();
			redefrc_vect = pm.getOutputVector();
			//redefrc_vect  = pm.runUntilFinished();
			redefrc_vect .insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			exitstat = pm.getExitStatus();
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				redefrc_vect .setElementAt( 
				"Command \"" + cmd + "\" failed", 0 );
			}	
		}
		catch (Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			if ( redefrc_vect  == null ) {
				redefrc_vect  = new Vector();
				redefrc_vect .addElement( 
				"Unable to run \"" + cmd + "\"" );
			}
		}
		pm = null;

	return redefrc_vect ;
} //end run_redefine_ratingCurves


////////////////////////

/**
Runs the ofs command:
"ofs -p fcinit -i RESEGDEF.GUI -o RESEGDEF.GUI.out" 
@return Vector containing output of running the ofs command:
"ofs -p fcinit -i RESEGDEF.GUI -o RESEGDEF.GUI.out" 
*/
public static Vector run_redefine_segments() {
	String routine = _class + ".run_redefine_segments";

	//ofs command
	String cmd = 
	"ofs -p fcinit -i RESEGDEF.GUI -o RESEGDEF.GUI.out -u " +
	_output_dir;

	String[] cmd_arr = { "ofs -p fcinit -i RESEGDEF.GUI -o RESEGDEF.GUI.out -u " + _output_dir };

	//vector to hold output
	Vector redefseg_vect = null;
	
		//exitstat
		int exitstat = -999;

		//Run the command
		//do not need full path- just file name for the ofs commands
		ProcessManager pm = new ProcessManager( cmd_arr );
		//pm.setCommandInterpreter(null);
		try {
			pm.saveOutput( true );
			pm.run();
			redefseg_vect = pm.getOutputVector();
			//redefseg_vect  = pm.runUntilFinished();
			redefseg_vect .insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			exitstat = pm.getExitStatus();
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				redefseg_vect .setElementAt( 
				"Command \"" + cmd + "\" failed", 0 );
			}	
		}
		catch (Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			if ( redefseg_vect  == null ) {
				redefseg_vect  = new Vector();
				redefseg_vect .addElement( 
				"Unable to run \"" + cmd + "\"" );
			}
		}
		pm = null;

	return redefseg_vect ;
} //end run_redefine_segments

/**
Runs the ofs command:
"ofs -p ppinit -i REDEFINE.STATIONS.GUI -o REDEFINE.STATIONS.GUI.out" 
@return Vector containing output of running the ofs command:
"ofs -p ppinit -i REDEFINE.STATIONS.GUI -o REDEFINE.STATIONS.GUI.out" 
*/
public static Vector run_redefine_stations() {
	String routine = _class + ".run_redefine_stations";

	//ofs command
	String cmd = 
	"ofs -p ppinit -i REDEFINE.STATIONS.GUI -o REDEFINE.STATIONS.GUI.out " +
	"-u " + _output_dir;

	String[] cmd_arr = {"ofs -p ppinit -i REDEFINE.STATIONS.GUI -o REDEFINE.STATIONS.GUI.out " + "-u " + _output_dir };

	//vector to hold output
	Vector redefstn_vect = null;
	
		//exitstat
		int exitstat = -999;

		//Run the command
		//do not need full path- just file name for the ofs commands
		ProcessManager pm = new ProcessManager( cmd_arr );
		//pm.setCommandInterpreter(null);
		try {
			pm.saveOutput( true );
			pm.run();
			redefstn_vect = pm.getOutputVector();
			//redefstn_vect  = pm.runUntilFinished();
			redefstn_vect .insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			exitstat = pm.getExitStatus();
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				redefstn_vect .setElementAt( 
				"Command \"" + cmd + "\" failed", 0 );
			}	
		}
		catch (Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			if ( redefstn_vect  == null ) {
				redefstn_vect  = new Vector();
				redefstn_vect .addElement( 
				"Unable to run \"" + cmd + "\"" );
			}
		}
		pm = null;

	return redefstn_vect ;
} //end run_redefine_stations


/**
First edits the FCEXEC.GUI file to put the selected start and run dates in.
Runs the OFS fcst command with the COSAVE.GUI file.  Returns the vector
created running the command through the ProcessManager.
@return  Vector containing output of running the ofs command:
"ofs -p fcst -i COSAVE.GUI -o COSAVE.GUI.out".
*/
public static Vector run_saveCarryover() {
	String routine = _class + ".run_saveCarryover";

	//ofs command
	String cmd = "ofs -p fcst -i COSAVE.GUI -o COSAVE.GUI.out -u " + 
	_output_dir;

	String[] cmd_arr ={ "ofs -p fcst -i COSAVE.GUI -o COSAVE.GUI.out -u " + _output_dir};

	//vector to hold output
	Vector cosave_vect = null;
	
	//change start and run dates in COSAVE.GUI
	//pass the full filename into editStartandRunDates( name );
	boolean file_edited = false;
	String cosave_gui_file = null;
	cosave_gui_file = IOUtil.getPropValue( "COSAVE.GUI" );
	if ( cosave_gui_file != null ) {
		file_edited = editStartandRunDates( cosave_gui_file );
	}	
	if ( file_edited ) {
		//then the COSAVE.GUI file has been edited with the new
		//dates... and the ofs command should be run.

		//exitstat
		int exitstat = -999;

		//Run the command
		//do not need full path- just file name for the ofs commands
		ProcessManager pm = new ProcessManager( cmd_arr );
		//pm.setCommandInterpreter(null);
		try {
			pm.saveOutput( true );
			pm.run();
			cosave_vect = pm.getOutputVector();
			//cosave_vect = pm.runUntilFinished();
			cosave_vect.insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			exitstat = pm.getExitStatus();
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				cosave_vect.setElementAt( 
				"Command: \"" + cmd + "\" failed", 0 );
			}
		}
		catch (Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			if ( cosave_vect == null ) {
				cosave_vect = new Vector();
				cosave_vect.addElement( 
				"Unable to run command: \"" + cmd + "\"" );
			}
		}
		pm = null;
	}
	else {
		Message.printWarning( 2, routine, 
		"Start and Run dates were not edited in the COSAVE.GUI " +
		"file so the ofs command: \"" + cmd + "\" will not be run." ); 
		cosave_vect = new Vector();
		cosave_vect.addElement( 
			"Unable to run command: \"" + cmd + "\"" );
	}

	return cosave_vect;

} //end run_saveCarryover

/**
Runs the OFS command: shefpars.  Before the command
is run, the shef file passed in is opened up to check is there are ^M
characters at the end of each line ( due to making the file on PC and
then putting it on the Unix box).  If there are ^M characters, the file
is simply rewritten without the characters.
@param shef_file  Shef file selected in the drop-down list to run
through the ofs command: shefpars.
@param fs  file seperator to use for concatenating paths.
@return Vector containing all the output from the ofs shefpars command.
*/
public static Vector run_shefpars( String shef_file, String fs ) {
	String routine = _class + ".run_shefpars";

	//used to determine if the ofs command should be run
	boolean run_command = true;

	//first make the full file.  Get the path to the 
	String shef_file_path = null;
	//shef files from proplist.
	String path_to_shef = null;
	path_to_shef = IOUtil.getPropValue( "SHEFDIRECTORY" );
	if ( path_to_shef != null ) {
		//check to see if the directory ends with the file seperator
		//or not. If it does, just append the file name to the path,
		//if not, add the file seperator before the filename
		String file_sep = null;
		file_sep = System.getProperty( "file.separator" );
		if ( file_sep == null ) {
			//try using default for unix
			file_sep = "/";
		}
		int index = -999;
		int length = -999;
		length = path_to_shef.length();
		index = path_to_shef.lastIndexOf( file_sep );
		if ( (index -1) == length ) {
			//then there is a file seperator at the end.
			shef_file_path = path_to_shef + shef_file;
		}
		else {
			//add file separator
			shef_file_path = path_to_shef + fs + shef_file;
		}
		if ( IOUtil.fileExists( shef_file_path )) {
			//now make file 
			//setup input and output streams and files
			//model file input
			File inputFile = null; 
			FileInputStream fis;
			InputStreamReader isr;
			BufferedReader br;

			//model output going to file
			File outputFile = null;
			FileOutputStream fos;
			PrintWriter pw;

			String s = null;
			inputFile = new File( shef_file_path );
			outputFile = new File( shef_file_path + ".tmp" );

			try {
				fis = new FileInputStream( inputFile );
				isr = new InputStreamReader ( fis );
				br = new BufferedReader ( isr );

				fos = new FileOutputStream( outputFile );
				pw = new PrintWriter( fos );
				
				//by simply reading in and writing out
				//the file line, by line, the ^M 
				//character is removed
				do {
					s = br.readLine();

					if ( s == null ) {
						//no more lines
						break;
					}

					pw.println(s );	
					pw.flush(); 
					
				}//end do 
				while ( s != null );
			
				br.close();
				pw.close();	

				//clean up
				fis = null;
				isr = null;
				br = null;

			}//end try	
			catch (Exception e ) {
				//use debug here, not warning
				//b/c prints this message even
				//when does edit the file

				if (Message.isDebugOn) {
					Message.printDebug(4, routine,
					"Unable to open: \"" +
					shef_file_path + "\" for editing." );
					Message.printWarning( 2, routine,
					"\"shefpars\" command failed. ");
				}
			}//end catch

			//now we have created an extra file.  Move it back
			//to original name, ie.  
			//.tmp moved to orig name
			try {
				outputFile.renameTo( inputFile );
			}		
			catch ( Exception secex ) {

				run_command = false;

				Message.printWarning( 2, routine, 
				"\"shefpars\" command failed." );
				if ( Message.isDebugOn ) {
					Message.printWarning( 2, routine,
					"tmp file: \"" + shef_file_path + 
					".tmp\" could not be moved to: \""+
					shef_file_path + "\"." );
				}
			}
			//clean up
			fos = null;
			pw = null;
			outputFile = null;
			inputFile = null;
		} //end if fileExists
		else {
			//file does not exist
			run_command = false;

			Message.printWarning( 2, routine, 
			"Unable to locate file: \"" +
			shef_file_path + "\" to post data. Will not " +
			"run \"shefpar\" ofs command." );
		}
	} //if not null
	else {
		run_command = false;

		Message.printWarning( 2, routine,
		"Unable to find the shef files directory.  Can not " +
		"post shef data for: \"" + shef_file + "\"." );
	}

	//now should be able to run ofs command
	//holds output of shefpars command 
	Vector shefpars_vect = null;
	if ( run_command ) {
		//run the ofs commands!
		int exitstat = -999;

		//RUN shefpars
		//do not need full path- just file name for the ofs commands
		String shefpars_cmd = 
		"ofs -p shefpars -i " + shef_file + " -o SHEFDATE.OUT -u " +
		_output_dir;

		String[] cmd_arr = { "ofs -p shefpars -i " + shef_file + " -o SHEFDATE.OUT -u " + _output_dir };

		ProcessManager pm = new ProcessManager(cmd_arr );
		//pm.setCommandInterpreter(null);
		
		try {
			pm.saveOutput( true );
			pm.run();
			shefpars_vect = pm.getOutputVector();
			//shefpars_vect = pm.runUntilFinished();
			shefpars_vect.insertElementAt( 
				"Command Run: \"" + shefpars_cmd + "\"", 0 );
			exitstat = pm.getExitStatus();
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + shefpars_cmd + "\" failed.");
				shefpars_vect.setElementAt( 
				"Command: \"" + 
				shefpars_cmd + "\" failed", 0 );
			}
		}
		catch (Exception e) {
			Message.printWarning( 2, routine, 
				"Command: \"" +
				"shefpars_cmd + \" FAILED." );
			if ( shefpars_vect == null ) {
				shefpars_vect = new Vector();
				shefpars_vect.addElement( 
				"Unable to run command: \"" + 
				shefpars_cmd + "\"" );
			}
		}
		pm = null;

	} // end if run_command

	return shefpars_vect;
} //end run_shefpars


/**
Runs the OFS command: shefpost.  Before the command
is run, it is assumed that the shefpars command has been run. 
@return Vector containing all the output from the ofs shefpost command.
*/
public static Vector run_shefpost( ) {
	String routine = _class + ".run_shefpost";

	//should be able to run ofs command
	//holds output of shefpost command 
	Vector shefpost_vect = null;

		//run the ofs command
		int exitstat = -999;

		//RUN shefpost
		//do not need full path- just file name for the ofs commands
		String shefpost_cmd = 
		"ofs -p shefpost -o shefpost.out -u " + _output_dir;

		String[] cmd_arr = { "ofs -p shefpost -o shefpost.out -u " + _output_dir };

		ProcessManager pm = new ProcessManager( cmd_arr );
		//pm.setCommandInterpreter(null);
		
		try {
			pm.saveOutput( true );
			pm.run();
			shefpost_vect = pm.getOutputVector();
			//shefpost_vect = pm.runUntilFinished();
			shefpost_vect.insertElementAt( 
				"Command Run: \"" + shefpost_cmd + "\"", 0 );
			exitstat = pm.getExitStatus();
			if (exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + shefpost_cmd + "\" failed.");
				shefpost_vect.setElementAt( 
				"Command: \"" +
				shefpost_cmd + "\" failed", 0 );
			}
		}
		catch (Exception e) {
			Message.printWarning( 2, routine, 
				"Command: \"" +
				"shefpost_cmd + \" FAILED." );
			if ( shefpost_vect == null ) {
				shefpost_vect = new Vector();
				shefpost_vect.addElement( 
				"Unable to run command: \"" +
				shefpost_cmd + "\"" );
			}
		}
		pm = null;
	return shefpost_vect;

} //end run_shefpost

/**
Edits the DUMPSHEF.GUI file by adding the correct STARTDATE and ENDDATE.
The STARTDATE and ENDDATE have already been set in the global PropList in
the setDefaultDates method.  Once the DUMPSHEF.GUI file is edited, 
the ofs prdutil DUMPSHEF.GUI command is run.
@return vector containing output lines from running command:
"ofs -p prdutil -i DUMPSHEF.GUI -o DUMPSHEF.GUI.out".
*/
public static Vector run_updateResults() {
	String routine = _class + ".run_updateResults";

	//command we ultimately want to run
	String cmd = "ofs -p prdutil -i DUMPSHEF.GUI -o DUMPSHEF.GUI.out -u "+
	_output_dir;

	String[] cmd_arr = {"ofs -p prdutil -i DUMPSHEF.GUI -o DUMPSHEF.GUI.out -u "+ _output_dir };

	//holds output of running the ofs command
	Vector update_vect = null;
	int exitstat = -999;

	//boolean to indicate if the OFS command should be run 
	//It is run only if the editing of the DUMPSHEF file was successful.
	boolean ran_successfully = true;

	//First need to edit the DUMPSHEF.GUI file to adjust the STARTDATE
	//and the ENDDATE.
	//Get rundate and startdate in nwsrfs format
	//String rd_string = null;
	String sd_string = null;
	String ed_string = null;
	//rd_string = IOUtil.getPropValue( "nwsrfs_RUNDATE" );
	sd_string = IOUtil.getPropValue( "nwsrfs_STARTDATE" );
/////
	ed_string = IOUtil.getPropValue( "nwsrfs_ENDDATE" );
/*
	if (( rd_string != null ) && ( sd_string != null )) {
		ed_string = increment_nwsrfsDays( rd_string, 7 );
		if ( ed_string == null ) { 
			Message.printWarning( 2, routine, 
			"Unable to calculate value for ENDDATE.  " +
			"ENDDATE should be equal to rundate plus " +
			"7 days.  RunDate currently is: \"" + 
			rd_string + "\"." );

			ran_successfully = false;
		}
		else { //ed_string is ok
			//set it in proplist with key "nwsrfs_ENDDATE"
			IOUtil.setProp( "nwsrfs_ENDDATE", ed_string );
		}
	}
	else {
		Message.printWarning( 2, routine, 
		"Unable to retrieve values for StartDate and RunDate. " +
		"Can not edit the DUMPSHEF.GUI file or run the command: \""+
		cmd + "\"." );
		
		ran_successfully = false;
	}
*/

	//at this point we should be able to edit DUMPSHEF.GUI file
	if ( ran_successfully ) {
		//find full file name/path
		String dumpshef_path = null;
		dumpshef_path = IOUtil.getPropValue( "DUMPSHEF.GUI" );
		if ( dumpshef_path == null ) {
			Message.printWarning( 2, routine,
			"Unable to determine path to \"DUMPSHEF.GUI\". " +
			"Can not run ofs command: \"" + cmd + "\"." );

			ran_successfully = false;
		}
		else { //dumpshef_path !=null
			//model file input
			File inputFile = null;
			FileInputStream fis;
			InputStreamReader isr;
			BufferedReader br;

			//model output going to file
			File outputFile = null;
			FileOutputStream fos;
			PrintWriter pw;

			String s = null;
			inputFile = new File( dumpshef_path );
			outputFile = new File( dumpshef_path + ".tmp" );

			//see if valid file.
			if ( inputFile.canRead() ) {
				try {
					fis = new FileInputStream( inputFile );
					isr = new InputStreamReader ( fis );
					br = new BufferedReader ( isr );

					fos = new FileOutputStream(outputFile);
					pw = new PrintWriter( fos );
				
					do {
						s = br.readLine();
						//change first line: the line
						//contains:
					//DUMPSHEF 1210/2000/ 1230/2000/BYID
						//where the first date 
						//is STARTDATE
						//and the second is ENDDATE
						if ( s.trim().regionMatches(
						true, 0, "DUMPSHEF",0,8 )) {

						s = "DUMPSHEF " + sd_string + 
						" "+ ed_string + " BYID";
						}
						if ( s == null ) {
							//no more lines
							break;
						}

						pw.println(s );	
						pw.flush(); 
				
					} while ( s != null );
			
					br.close();
					pw.close();	

					//clean up
					fis = null;
					isr = null;
					br = null;

				}	
				catch (Exception e ) {
					//use debug here, not warning
					//b/c prints this message even
					//when does edit the file
					if (Message.isDebugOn) {
						Message.printDebug( 2, routine,
						"Unable to open: \"" +
						dumpshef_path + 
						"\" for editing." );
					}
				}

				//now we have created an extra tmp file.  
				//Move it back to original name, ie.  
				//FCEXEC.GUI.tmp moved to FCEXEC.GUI
				try {
					outputFile.renameTo( inputFile );
				}		
				catch ( Exception e ) {
					Message.printWarning( 2, routine,
					"Unable to move file: \"" +
					dumpshef_path + ".tmp\" back to: \"" +
					dumpshef_path + 
					"\". Will not run ofs " +
					"command: \"" + cmd + "\"." );

					ran_successfully = false;
				}

			} //end if file.canRead
			else {
				Message.printWarning( 2, routine,
				dumpshef_path + " is not a valid file. " +
				"Please check path, and permissions of: " + 
				dumpshef_path );
				ran_successfully = false;
			}

			//clean up
			fos = null;
			pw = null;
			s = null;
			outputFile = null;
			inputFile = null;
				
		}
	} //end if (ran_successfully)
			
	//again check if everything has been successful so far.
	//Since the DUMPSHEF.GUI file has been edited, run ofs command
	if ( ran_successfully ) {
		ProcessManager pm = new ProcessManager( cmd_arr );
		//pm.setCommandInterpreter(null);
		try {
			pm.saveOutput( true );
			pm.run();
			update_vect = pm.getOutputVector();
			//update_vect = pm.runUntilFinished();
			update_vect.insertElementAt( 
				"Command Run: \"" + cmd + "\"", 0 );
			exitstat = pm.getExitStatus();
			if ( exitstat != 0 ) {
				Message.printWarning( 2, routine,
				"Command: \"" + cmd + "\" failed.");
				update_vect.setElementAt( 
				"Command: \"" + cmd + "\" failed", 0 );
			}
		}
		catch (Exception e) {
			Message.printWarning( 2, routine, 
			"Command: \"" + cmd + "\". FAILED. " ); 
			if ( update_vect == null ) {
				update_vect = new Vector();
				update_vect.addElement( 
				"Unable to run command: \"" + cmd + "\"" );
			}
		}

		pm = null;
	}

	return update_vect;
} //end run_updateResults

/**
Opens up the file passed in a text editor.  If there is a text 
editor specified in the configuration file ( nedit or vi ), that
editor is used.  If no editor is specified in the config file, then vi is used.
@param editor  Name of editor to use: either "xterm -e vi" or "nedit".  
If this is null, vi is used.
@param file_name  Name of file to open.  Can not be null.
@param for_editing  boolean to indicate if the file should be opened
for editing ( true ) or just for viewing ( false ).  This is ONLY relevant for
the VI  and NEDIT editors, since we know that vi has a -R flag 
and nedit has -READ flag (for read-only). 
@execption Error thrown if file to be edited can not be found.
*/
public static void runEditor( String editor, 
				String file_name,
				boolean for_editing ) throws Exception  {

	String routine = _class + ".runEditor";

	//check that the file is valid.
         //make sure file is readable. If not, set it to null
        if ( !IOUtil.fileReadable( file_name ) ) {
            	throw new Exception ( "file to edit: \"" + file_name + 
		"\" is not a readable file." );
	}

//Message.printStatus(1,"","ERASE:::: editor = " + editor );
	Message.printStatus( 10, routine,
	"file to edit: \"" + file_name + "\"." );

	//editor is "xterm -e vi"  or "nedit" (HP-UX only)
	String command = null;
	String[] arrCommand = new String[1];

	String xterm = "xterm -geom 142x40 -e ";
	if ( !for_editing ) { //read only
		if ( (editor.trim()).endsWith( "vi" ) ) {
			command = xterm + "vi -R " + file_name;
			arrCommand[0] = command;
		}
		else if ( (editor.trim()).equalsIgnoreCase( "nedit" ) ) {
			command = editor + " -read" + " " + file_name;
			arrCommand[0] = command;
		}				
		else {
			command = xterm + editor + " " + file_name;
			arrCommand[0] = command;
		}
	}//end if !for_editing.
	else  { //else open for read and editing...  
		//command = editor + " " + file_name;
		if ( (editor.trim()).endsWith( "vi" ) ) {
			command = xterm + "vi " + file_name;
			arrCommand[0] = command;
		}
		else {
			command = editor + " " + file_name; 
			arrCommand[0] = command;
		}
	}
	/*
	if ( !for_editing ) { //read only
		if ( (editor.trim()).endsWith( "vi" ) ) {
			command = editor + " -R " + file_name;
			arrCommand[0]=command;
		}
		else if ( (editor.trim()).equalsIgnoreCase( "nedit" ) ) {
			command = editor + " -read" + " " + file_name;
			arrCommand[0]=command;
		}				
		else {
			command = editor + " " + file_name;
			arrCommand[0]=command;
		}
	}//end if !for_editing.
	else  { //else open for read and editing...  
		//command = editor + " " + file_name;
		command = editor + " " + file_name; 
		arrCommand[0]=command;
	}
	*/
	

	//now we should be ready to edit the file.
	Vector edit_vect = null;
	int exitstat = -99;
	ProcessManager pm = new ProcessManager( arrCommand );
	Thread thread = new Thread (pm );
	try {
		pm.saveOutput( true );
		//pm.run();
		thread.start();
		edit_vect = pm.getOutputVector();
		exitstat = pm.getExitStatus();
	}
	catch (Exception e) {
		Message.printDebug(2, routine,  
			"Unable to edit file: \"" + file_name + "\"." );
	
		Message.printWarning( 2, routine, e );
	}

	//if the ProcessManger run alright, continue
	if (( exitstat == 0 ) && ( edit_vect != null )) {
		//things are good.
	}
	else {
		Message.printWarning(2, routine,  "Unable to edit file: \"" +
		file_name + "\"." );
	}

	pm = null;
		
} //end runEditor



/**
Saves the Run date passed in in the Global PropList known it IOUtil in 
both the regular and the nwsrfs formats.  
@param rd_selected  RunDate selected from the drop-down list to save.
*/
public static void saveSelectedRunDate( String rd_selected ) {
	String routine = _class + ".saveSelectedRunDate";
	
	if ( rd_selected != null ) {
		//In drop-down list, the dates are already formatted
		//in regular format dd/MM/yyyy
		IOUtil.setProp( "RUNDATE", rd_selected );
		if ( Message.isDebugOn ) {
			Message.printDebug( 5, routine, 
			"Selected RunDate from Drop-down " + 
			"list: " + rd_selected ); 
		}
		//Rundate we have is in format: dd/MM/yyyy
		String yr_str = null;
		String mn_str = null;
		String day_str = null;
		yr_str = rd_selected.substring(6);
		mn_str = rd_selected.substring(3,5);
		day_str = rd_selected.substring(0,2);
	
/*
		if ( StringUtil.isInteger( yr_str ) ) {
			yr = StringUtil.atoi( yr_str );
		}
		if ( StringUtil.isInteger( mn_str ) ) {
			mn = StringUtil.atoi( mn_str );
		}
		if ( StringUtil.isInteger( day_str ) ) {
			day = StringUtil.atoi( day_str );
		}	
*/

		//now format it in nwsrfs format MMdd/yyyy/
		IOUtil.setProp( "nwsrfs_RUNDATE", 
		mn_str + day_str + "/" + yr_str + "/" );


		if ( Message.isDebugOn ) {
			Message.printDebug( 2, routine,
			"Run date saved in nwsrfs format as: \""+ 
			mn_str + day_str + "/" + yr_str + "/\"" );
		}
		
	}
	
} //end saveSelectedRunDate

/**
Saves the Run date passed in in the Global PropList known it IOUtil in 
both the regular and the nwsrfs formats.  
@param rd_selected  RunDate selected from the drop-down list to save.
*/
public static void saveSelectedRunDate2( String rd_selected ) {
	String routine = _class + ".saveSelectedRunDate";
	
	if ( rd_selected != null ) {
		//In drop-down list, the dates are already formatted
		//in regular format dd/MM/yyyy
		IOUtil.setProp( "RUNDATE", rd_selected );
		if ( Message.isDebugOn ) {
			Message.printDebug( 5, routine, 
			"Selected RunDate from Drop-down " + 
			"list: " + rd_selected ); 
		}

		//now format it in nwsrfs format MMdd/yyyy/
			Date rd_date = null;
			String nws_rd_string = null;
			//now format it as nwsrfs_formatter format
			try {
				rd_date = _reg_date_formatter.parse( 
				rd_selected );
				nws_rd_string = _nwsrfs_date_formatter.format(
				rd_date );
			}
			catch ( Exception e ) {
				Message.printWarning( 2, routine,
				"Unable to convert RunDate selected " +
				"from the drop-down list: \"" +
				rd_selected + "\" to nwsrfs date format: " +
				"\"MMdd/yyyy/\". ");
//TO DO
//should we exit? no, 
//but now dates will be wrong thru-out...
				if ( Message.isDebugOn ) {
					Message.printWarning( 2, routine, e );
				}
			}
			if ( nws_rd_string != null ) {
				IOUtil.setProp( "nwsrfs_RUNDATE", 
				nws_rd_string );
				if ( Message.isDebugOn ) {			
					Message.printDebug( 5, routine,
					"Selected RunDate from " +
					"Drop-down list in nwsrfs format: " + 
					nws_rd_string );
				}
			}
			rd_date = null;
	}
	
} //end saveSelectedRunDate2



/**
Saves the Start date passed in in the Global PropList known it IOUtil in 
both the regular and the nwsrfs formats.  
@param sd_selected  StartDate selected from the drop-down list to save.
*/
public static void saveSelectedStartDate( String sd_selected ) {
	String routine = _class + ".saveSelectedStartDate";
	
	if ( sd_selected != null ) {
		//as the date is in the combo box, it is 
		//already in "regular" format dd/MM/yyyy
		IOUtil.setProp( "STARTDATE", sd_selected );
		if ( Message.isDebugOn ) {
			Message.printDebug( 5, routine, 
			"Selected StartDate from Drop-down " + 
			"list: " + sd_selected ); 
		}

		//startdate we have is in format: dd/MM/yyyy
		String yr_str = null;
		String mn_str = null;
		String day_str = null;
		yr_str = sd_selected.substring(6);
		mn_str = sd_selected.substring(3,5);
		day_str = sd_selected.substring(0,2);
	
/*
		if ( StringUtil.isInteger( yr_str ) ) {
			yr = StringUtil.atoi( yr_str );
		}
		if ( StringUtil.isInteger( mn_str ) ) {
			mn = StringUtil.atoi( mn_str );
		}
		if ( StringUtil.isInteger( day_str ) ) {
			day = StringUtil.atoi( day_str );
		}	
*/

		//now format it in nwsrfs format MMdd/yyyy/
		IOUtil.setProp( "nwsrfs_STARTDATE", 
		mn_str + day_str + "/" + yr_str + "/" );

		if ( Message.isDebugOn ) {
			Message.printDebug( 2, routine,
			"Start date saved in nwsrfs format as: \""+ 
			mn_str + day_str + "/" + yr_str + "/\"" );

		}
	}
	
} //end saveSelectedStartDate

/**
Saves the Start date passed in in the Global PropList known it IOUtil in 
both the regular and the nwsrfs formats.  
@param sd_selected  StartDate selected from the drop-down list to save.
*/
public static void saveSelectedStartDate2( String sd_selected ) {
	String routine = _class + ".saveSelectedStartDate";
	
	if ( sd_selected != null ) {
		//as the date is in the combo box, it is 
		//already in "regular" format dd/MM/yyyy
		IOUtil.setProp( "STARTDATE", sd_selected );
		if ( Message.isDebugOn ) {
			Message.printDebug( 5, routine, 
			"Selected StartDate from Drop-down " + 
			"list: " + sd_selected ); 
		}

			//now format it in nwsrfs format MMdd/yyyy/ 
			Date sd_date = null;
			String nws_sd_string = null;
			//now format it as nwsrfs_formatter format
			try {
				sd_date = _reg_date_formatter.parse( 
				sd_selected );
				nws_sd_string = _nwsrfs_date_formatter.format(
				sd_date );
			}
			catch ( Exception e ) {
				Message.printWarning( 2, routine,
				"Unable to convert StartDate selected " +
				"from the drop-down list: \"" +
				sd_selected + "\" to nwsrfs date format: " +
				"\"MMdd/yyyy/\". ");
//TO DO
//should we exit? no, 
//but now dates will be wrong thru-out...
				if ( Message.isDebugOn ) {
					Message.printWarning( 2, routine, e );
				}
			}
			if ( nws_sd_string != null ) {
				IOUtil.setProp( "nwsrfs_STARTDATE", 
				nws_sd_string );
				if ( Message.isDebugOn ) {			
					Message.printDebug( 5, routine,
					"Selected StartDate from " +
					"Drop-down list in nwsrfs format: " + 
					nws_sd_string );
				}
			}

			sd_date = null;
	}
	
} //end saveSelectedStartDate2

/**
Sets up default:
<P><UL>
<LI>StartDate</LI>
<LI>RunDate</LI>
<LI>EndDate</LI>
<LI>HOUR</LI>
<LI>TimeZone</LI>
</UL></P>
These default dates are stored in the global proplist,
 _gui_props, in two formats each:
nwsrfs date format: "MMdd/yyyy/" and regular format: "dd/MM/yyyy".
The hour is stored in the format: "*12Z" for instance.

The DEFAULT Values for these dates are first set as follows
<P><TABLE>
<TH>Date</TH><TH>How Default Set</TH><TR>
<TD>RunDate</TD><TD>Today's Date</TD><TR>
<TD>StartDate</TD><TD>RUNDATE-5</TD><TR>
<TD>EndDate</TD><TD>RUNDATE+7</TD><TR>
<TD>HOUR</TD><TD>*12Z</TD><TR>
</TABLE></P>

Afer first attempt to set the dates is made, the REAL Dates will be attained 
and the lists of dates needed to create the StartDate and RunDate drop-down 
lists for NwsrfsDailyOperations' setDates panel will be created and stored.
<PRE><TABLE>
<tr>
<TH>Date</TH><TH>How REAL Value Set </TH><TR>
</tr>
<tr>
<TD>RUNDATE</TD>
<TD>Default is today or last date before today.
</TD>
</tr>
<TR>
<TD>STARTDATE</TD>
<TD>Today-5 OR latest date in list from the NWSRFS_CarryoverGroup object's carryover dates</TD>
</tr>
<TR>
<TD>ENDDATE</TD>
<TD>RUNDATE+7</TD>
</tr>
</TABLE></PRE>
*@param NWSRFS_CarryoverGroup cg NWSRFS_CarryoverGroup object to 
get carryover dates from.  
*/
public static void setDefaultDates( NWSRFS_CarryoverGroup cg ) {
	String routine = _class + ".setDefaultDates";

	/********************************************************/
	//////// PART I -set possible dates in case below fails //////////
	/********************************************************/
	//need to set default run and start dates - once we 
	//have them, save them to the _gui_props PropList in 
	//two formats each: a nwsrfs date format: "MMdd/yyyy/" 
	//and a regular format: "dd/MM/yyyy"

	//set defaults of RUNDATE=today and STARTDATE=RUNDATE-5
	//these defaults will be changed below to:
	//STARTDATE = (today-5) OR latest date in output list from 
	//carryoverGroup's carryover dates
	//RUNDATE = today or last valid date between STARTDATE to
	//STARTDATE+20.
	//set defaults here just in case calculations below fail.

	//todays date
	String today_str = null;
	DateTime today = new DateTime( DateTime.DATE_CURRENT | DateTime.FORMAT_YYYY_MM_DD_HH_mm );

	//do nwsrfs date 1st: format:  "MMdd/yyyy/" 
	today_str =  StringUtil.formatString(today.getMonth(),"%02d") +  
	StringUtil.formatString( today.getDay(), "%02d") + "/" +
	today.getYear() + "/";

	if ( Message.isDebugOn ){
		Message.printDebug( 5, "setDefaultDates",
		"Today's date (rundate) formatted: " + today_str );
	}

	//set todays nwsrfs date in global propList using key: RUNDATE
	IOUtil.setProp( "nwsrfs_RUNDATE", today_str );

	//now do regular date format: "dd/MM/yyyy"
	today_str = StringUtil.formatString( today.getDay(), "%02d")  + "/" + 
	StringUtil.formatString( today.getMonth(), "%02d")  + "/" + 
	today.getYear();

	IOUtil.setProp("RUNDATE", today_str );
	
	/////*****  STARTDATE *****///////
	//now we need to set STARTDATE, which is today(ie,RUNDATE) - 5
	DateTime startdate = new DateTime(today, DateTime.FORMAT_YYYY_MM_DD_HH_mm);
	startdate.addDay(-5);
	String startdate_str = null;
	//nwsrs format: "MMdd/yyyy/" 
	startdate_str = StringUtil.formatString( startdate.getMonth(),"%02d") + 
	StringUtil.formatString( startdate.getDay(),"%02d" ) + "/" +
	startdate.getYear() + "/";

	// save nwsrfs as nwsrfs_STARTDATE 
	IOUtil.setProp( "nwsrfs_STARTDATE", startdate_str );

	
	//regular format: "dd/MM/yyyy"
	startdate_str = StringUtil.formatString( startdate.getDay(), "%02d" )  
	+ "/" + StringUtil.formatString( startdate.getMonth(), "%02d" )  +
	"/" + startdate.getYear();

	IOUtil.setProp("STARTDATE", startdate_str);	

	if ( Message.isDebugOn ) {
		Message.printDebug( 4, routine,
		"Original default dates set in proplist for startdate = \"" +
		startdate_str +  "\" and for rundate = \"" +
		today_str + "\". These dates should be replaced by " +
		"the actual carryover dates associated with the " +
		"carryoverGroup." );
	}
	

	/********************************************************/
	//////// PART II -set REAL dates and date LISTS //////////
	/////// Startdate, RunDate, Enddate
	/********************************************************/
	
	//use carryoverGroup object passed in to get date list.
	Vector dates_vect = null;
	if ( cg != null ) {
		dates_vect = cg.getCarryoverDates();
	}
	else {
		Message.printWarning( 2, routine, "Error with Carryover " +
		"Group used in GUI- unable to get Carryover dates from it.");
	} 
	if ( dates_vect != null ) {
		if ( Message.isDebugOn ) {
			Message.printDebug( 2, routine,
			"Vector returned from " + routine + "is : " + 
			dates_vect.toString() );
		}
	}
	else {
		Message.printWarning( 2, routine, "No carryover dates " +
		"found" );
		dates_vect = new Vector();
	}

	//now we have a vector of DateTime dates.
	int size = 0;
	DateTime d = null;
	if ( dates_vect != null ) {
		size = dates_vect.size();
	}
	if ( Message.isDebugOn ) {
		Message.printDebug( 4, routine, size + " carryover " +
		"dates returned for carryover group." );
	}

	if (size == 0) {
		Message.printWarning(1, routine, 
			"Cannot determine carryover dates -- functionality "
			+ "will be limited.", new JFrame());
		return;
	}

	Vector formatted_dates_vect = new Vector();
	boolean found_sd = false;
	for ( int i=0; i<size; i++ ) {
		d = (DateTime) dates_vect.elementAt(i);

		boolean setHr = true;
		if ( d == null ) {
			continue;
		}


		if( setHr ) {
			//save the HOUR and time zone info (used later in class)
			_time_info = d.getHour() + " " + d.getTimeZoneAbbreviation();
			//set HOUR in propList NOTE:: Hardcoded!
			IOUtil.setProp( "HOUR", "*12Z" );
			
			setHr = false;
		}

		if ( startdate.equals( d ) ) {
			//then we have found the same startdate we had
			//set above as the default start date. (it is
			//already set in the proplist)
			found_sd = true;

			Message.printStatus( 4, routine,
			"Default Start Date found: " + d.toString() );

		}

		//now format all the dates into format dd/MM/yyyy 
		formatted_dates_vect.addElement(
		StringUtil.formatString( d.getDay(), "%02d" )  + "/" + 
		StringUtil.formatString( d.getMonth(), "%02d" )  + 
		"/" + d.getYear() ); 

		IOUtil.setProp("LAST_CO_DATE", d);

		d = null;
	}

	
	if ( formatted_dates_vect.size() ==  0  ) {
		//make vector with just default startdate set
		formatted_dates_vect.addElement( startdate_str );
	}

	IOUtil.setProp("STARTDATE_LIST", formatted_dates_vect );

//Message.printStatus(1,"","erase UTIL::: formatted startdate list= " + formatted_dates_vect );


	//now let's see if the startdate was found or not.
	//if it was not, need to set start date to last date
	//in list of startdates.
	if ( !found_sd ) {
		DateTime last_date = null;
		//get last date- last thing in list
		if ( size > 1 ) {
			last_date = (DateTime) dates_vect.elementAt( size - 1);
		}
		else {
			last_date = (DateTime) dates_vect.elementAt(0);
		}
	
		//set this in the proplist
		if ( last_date != null ) {
			IOUtil.setProp( "STARTDATE", 
			StringUtil.formatString( last_date.getDay(), "%02d" ) + 
			"/" +
			StringUtil.formatString( last_date.getMonth(), "%02d" ) +
			"/" + last_date.getYear() );
	
			Message.printStatus( 2, routine,
			"Default Start Date: " + last_date );

			Message.printStatus( 2, routine, "Start Date " +
			"NOT FOUND in list of start dates - will set " +
			"start date to last date in list: " + last_date );
					
			//now format it in nwsrfs format: "MMdd/yyyy/" 
			IOUtil.setProp( "nwsrfs_STARTDATE", 
			StringUtil.formatString( last_date.getMonth(), "%02d" ) +
			StringUtil.formatString( last_date.getDay(), "%02d" ) + 
			"/" + last_date.getYear() + "/" );
		//clean up
		last_date = null;
				} 
	} //end !found_sd 


	//RUNDATE AND ENDDATES
	//now we have finally settled startdate.  Rundate list is made
	//of startdate to startdate+20.  And default Rundate is set
	//at today or latest date in rundate list.
	//lets get dates for rundate. 
	//And, enddate is RUNDATE + 7

	//Vector will hold dates for rundate dropdown list
	Vector rd_list= new Vector();

	//Again, the rundate list consists of the startdate to startdate + 20
	DateTime tmp_startdate = new DateTime( startdate, DateTime.FORMAT_YYYY_MM_DD_HH_mm );

	boolean found_rd = false;
	for ( int i=0; i<21; i++ ) {
		//see if we find the default rundate which is TODAY
		if ( tmp_startdate.equals( today ) ) {
			//already has been set in proplist
			found_rd = true;

			//set enddate too, which is rundate + 7
			DateTime end_date = new DateTime( tmp_startdate, DateTime.FORMAT_YYYY_MM_DD_HH_mm );
			end_date.addDay(7);
			IOUtil.setProp( "ENDDATE", 
			StringUtil.formatString( end_date.getDay(), "%02d" ) + 
			"/" +
			StringUtil.formatString( end_date.getMonth(), "%02d" ) + 
			"/" + end_date.getYear() );

			IOUtil.setProp( "nwsrfs_ENDDATE", 
			StringUtil.formatString( end_date.getMonth(), "%02d" ) +
			StringUtil.formatString( end_date.getDay(), "%02d" ) +
			 "/" + end_date.getYear() + "/" );

			Message.printStatus( 4, routine,
			"End Date set as (rundate +7): " + end_date.toString() );

			end_date = null;
			
		}

		//format dd/MM/yyyy
		rd_list.addElement( 
		StringUtil.formatString( tmp_startdate.getDay(), "%02d") + "/" +
		StringUtil.formatString( tmp_startdate.getMonth(), "%02d") + 
		"/" + tmp_startdate.getYear() );

		//add one day
		tmp_startdate.addDay(1);
	}
	tmp_startdate = null;

	if ( Message.isDebugOn ) {
		Message.printDebug( 4, routine,
		"Vector of rundates= " + rd_list.toString() );
	}

	//set rundate list in proplist
	IOUtil.setProp("RUNDATE_LIST", rd_list );

	//check if rd was already found
	if ( !found_rd ) {
		//select latest date in list
		DateTime tmp_last_date = new DateTime( startdate, DateTime.FORMAT_YYYY_MM_DD_HH_mm );
		tmp_last_date.addDay( 21 );
		//set it in proplist - since it came from
		//the startdate list, it is already in 
		IOUtil.setProp("RUNDATE", 
		StringUtil.formatString( tmp_last_date.getDay(), "%02d" ) + "/" +
		StringUtil.formatString( tmp_last_date.getMonth(), "%02d" ) + 
		"/" + tmp_last_date.getYear() );

		Message.printStatus( 4, routine,
		"Run Date: " + tmp_last_date.toString() );

		//set nwsrfs version too: "MMdd/yyyy/" 
		IOUtil.setProp("nwsrfs_RUNDATE", 
		StringUtil.formatString( tmp_last_date.getMonth(), "%02d" ) +
		StringUtil.formatString( tmp_last_date.getDay(), "%02d" ) + 
		"/" + tmp_last_date.getYear() + "/" );

		//set ENDDate too, which is rundate + 7
		DateTime end_date = new DateTime( tmp_startdate, DateTime.FORMAT_YYYY_MM_DD_HH_mm );
		end_date.addDay(7);
		IOUtil.setProp( "ENDDATE", 
		StringUtil.formatString( end_date.getDay(), "%02d" ) + "/" +
		StringUtil.formatString( end_date.getMonth(), "%02d" ) + 
		"/" + end_date.getYear() );
		IOUtil.setProp( "nwsrfs_ENDDATE", 
		StringUtil.formatString( end_date.getMonth(), "%02d" ) +
		StringUtil.formatString( end_date.getDay(), "%02d" )  + 
		"/" + end_date.getYear() + "/" );

		Message.printStatus( 4, routine,
		"End Date: " + end_date.toString() );

		end_date = null;


	} 

}//end setDefaultDates

/**
Sets the global variable _output_dir.  Is called by the 
initialize_props() method (called by constructor) of 
the NwsrfsMainGUI class.
@param output_dir  name of output level to use for ofs commands.
*/
public static void setOutputDir( String output_dir ) {
	_output_dir = output_dir;
}


/**
Read in the file passed in and stores each line of the file as
a String object in a vector.
@param fileToRead  file to read in.
@return Vector containing all lines of the file or null 
if can not read the file.
*/
public static Vector translationFileToVector( String fileToRead ) {
	String routine = _class + ".translationFileToVector";

	//vector to return
	_translated_lines = new Vector();

	//test to see that fileToRead is valid file
	if ( IOUtil.fileExists( fileToRead ) ) {
		//good, continue on to read it
		//open file, read in
		File inputFile = null;
		FileInputStream fis;
		InputStreamReader isr;
		BufferedReader br;
		String s = null;
		try {
			inputFile = new File( fileToRead );
			fis = new FileInputStream( inputFile );
			isr = new InputStreamReader( fis );
			br = new BufferedReader( isr );
			do {
				s = br.readLine();
				if ( s == null ) {
					//no more lines
					break;
				}
				else {
					_translated_lines.addElement(s);
				}
			} while ( s !=null );

			//clean up
			br = null;
			isr = null;
			fis = null;
		}
		catch ( Exception e ) {
			Message.printWarning( 2, routine, e );
		}
	} //if file exists
	else {
		Message.printWarning( 2, routine, "Unable to read: \"" +
		fileToRead + "\".  Can not retreive translation text. ");
		_translated_lines = null;
	}
	return _translated_lines;
} //end  translationFileToVector

/**
Creates the FMAPMODS.GUI file based on the FMAP data the user has 
entered.   The file is written out with each entered value divided into
a 6-hr interval value. Each value in the arrays, represents a value 
for a Full day, so we need to divide each value by 4 to get a value for a 
6 hr interval of that day.  If there is an error in computations, a 
"0" will be used for that value.
@param fmap_names_vect  Vector containing names for the basins.
@param fmap_values_vect  Vector containing arrays of Strings - each
	array holds the fmap values for one fmap area.  The arrays are
	order in the same way as the fmap_names_vect so that there is 
	a one-to-one correspondence.
*/
public static void write_fmap_file( Vector fmap_names_vect, 
					Vector fmap_values_vect ) {
	String routine = _class + ".write_fmap_file";

	//boolean to determine if the file should be written
	boolean write_file = true;

	//Number of days out that the user entered fmap data for
	//is determined by the length of the arrrays in the vector
	//of fmap_values_vect.
	int numb_days = 0;

	//number of areas is determined by how many arrays there are or
	//conversly, by how many names there are in the names_vect
	int numb_areas = 0;
	if ( fmap_names_vect != null ) {
		numb_areas = fmap_names_vect.size();
	}
	else { 
		Message.printWarning( 2, routine,
		"Unable to determine fmap area names." );
		write_file = false;
	}

	if ( ( fmap_values_vect == null ) || ( fmap_values_vect.size() < 1 ) ) {
		Message.printWarning( 2, routine,	
		"Unable to determine values for fmap data, will use \"0\" " );

		if ( fmap_values_vect == null ) {
			fmap_values_vect = new Vector();
		}
		for ( int i=0; i< numb_areas; i++ ){
			fmap_values_vect.addElement( "O" );
		}
	}
	else { //fmap_values_vect is not null, so get Number of days by
		//finding out array size.
		numb_days = ((String[])fmap_values_vect.elementAt(0)).length;
			
	}

	//get file name and local from proplist
	String fmap_file_path = null;
	fmap_file_path = IOUtil.getPropValue( "FMAPMODS.GUI" );
	if ( fmap_file_path == null ) {
		Message.printWarning( 2, routine,
		"Unable to get path for \"FMAPMODS.GUI\"" );
		write_file = false;
	}
	if ( !IOUtil.fileExists( fmap_file_path ) ) {
		Message.printWarning( 2, routine,
		"\"FMAPMODS.GUI\" file: \"" + fmap_file_path + 
		"\" is invalid.");
		write_file = false;
	}

	//get Start and Run dates b/f writing file out
	String sd = null;
	String rd = null;
	rd = IOUtil.getPropValue( "nwsrfs_RUNDATE" );
	sd = IOUtil.getPropValue( "nwsrfs_STARTDATE" );
	if (( rd == null ) || ( sd == null ) ) {
		Message.printWarning( 2, routine, 
		"Unable to get rundate or startdate." );
		write_file = false;
	}

	String rd_plusOne = null;
	if ( rd != null ) {
		rd_plusOne = increment_nwsrfsDays( rd, 1 );
		if ( rd_plusOne == null ) {
			Message.printWarning( 2, routine, 
			"Unable to increment Run date." );
			write_file = false;
		}
	}

	//now we are ready to write file.
	if ( write_file ) {
		//model file output
		File outputFile = null;
		FileOutputStream fos;
		PrintWriter pw;
		String s = null;		
		outputFile = new File( fmap_file_path );
		try {
			fos = new FileOutputStream( outputFile );
			pw = new PrintWriter( fos );

			s = ".FMAP6 " + rd_plusOne + " M";
			pw.println(s);
			pw.flush();

			//loop based on number of fmap areas
			for ( int i=0; i<numb_areas; i++ ) {
				//each value is divided up into 
				//a 6hr interval, so each input
				//value, is actually divided by 4
				//and printed 4 times
				//to get the number of values 
				//(in 6 hr interval), simply multiply
				//the number of days out by 4.  
			
				//get values out of array and change
				//them to doubles.
				String[] array = null;
				double day_val = 0.0;
				double interval_val = 0.0;
				String interval_string = "0.0";
				StringBuffer buffer = new StringBuffer();
				for ( int j=0; j<numb_days; j++ ) {
					array = (String[])
						fmap_values_vect.elementAt(i);
					try {
						day_val = Double.valueOf(
							array[j]).doubleValue();
					}
					catch ( Exception e ) {
						day_val = 0.0;
					}
						
					//now we have value from array,
					//divide it by 4 to get the 
					//6hr interval value.
					if ( day_val == 0 ) {
						//don't do anything
						interval_val = day_val;
					}
					else {
						//try and divide by 4
						try {
							interval_val =
								day_val/4;
						}
						catch (Exception e ) {
							interval_val = 0;
						}
					}
					//now change back to string
					//for easy storage and printing.
					interval_string = Double.toString(
						interval_val );
				
					//format interval_val to have 2 
					//decimal place
					interval_string = 
						StringUtil.formatString(
						interval_string, "%7.2f" );

					//now make StringBUffer which
					//contains all the values for 
					// a line of data to be printed:
					//Each line will have 4 values,
					//and there will One Line for
					//each Day input per fmap area.
					//EX: for 3 days
					//EASTFAMP 0.0 0.0 0.0 0.0 &
					//1.0 1.0 1.0 1. 0 &
					//0.5 0.5 0.5 0.5
					if ( (numb_days-1) == j ) {
						//then we are on the last one, 
						//so don't add the "&"
						buffer = buffer.append( " " +
							interval_string + " " + 
							interval_string + " " + 
							interval_string + " " + 
							interval_string + " " +
							 "\n" );
					}
					else {
						//we are in  not at the end, so 
						//put a continuation marker: &
						buffer = buffer.append( " " +
							interval_string + " " + 
							interval_string + " " + 
							interval_string + " " + 
							interval_string + " " +
							 "&" + "\n" );
					}

				} //end inner loop thru days out

				//now print out a line:
				//format: EASTFAMP 0.0 0.0 0.0 0.0 &
				//0.0 0.0 0.0 0.0 &
				//0.0 0.0 0.0 0.0 

				pw.println( 
					(String)fmap_names_vect.elementAt(i) +
					buffer );
				pw.flush();

			}//end loop thru fmap area names
		}
		catch ( Exception e ) {
		}


	}
	else {
		Message.printWarning( 2, routine,
		"Unable to modify FMAPMODS.GUI" );
	}
	

} //end write_fmap_file

/**
Opens the DELETERC.GUI file to update the Rating Curve ID 
in the file to match it with the paramater, rcid, passed in. 
@param rcid Rating Curve ID to be deleted.
@param return boolean to indicate if file was edited or not.
*/
public static boolean  update_deleteRC_file( String rcid ) {
	String routine = _class + ".update_deleteRC_file";

	//boolean  to return
	boolean  file_edited = true;

	//open file for editing.
	//get DELETERC.GUI local
	String fileToEdit = null;
	fileToEdit = IOUtil.getPropValue( "DELETERC.GUI" );
	if ( fileToEdit == null ) {
		Message.printWarning(2, routine,
		"Unable to locate the \"DELETERC.GUI\" file." );

		file_edited = false;
	}
	else {
		//now we just need to read and write the file 
		//with the new stn id model file input
		File inputFile = null;
		FileInputStream fis;
		InputStreamReader isr;
		BufferedReader br;

		//model output going to file
		File outputFile = null;
		FileOutputStream fos;
		PrintWriter pw;
		
		String s = null;
		inputFile = new File( fileToEdit );
		outputFile = new File( fileToEdit + ".tmp" );
		
		//if the file is a readable file
		if ( IOUtil.fileReadable( fileToEdit ) ) {
			try {
				fis = new FileInputStream( inputFile );
				isr = new InputStreamReader ( fis );
				br = new BufferedReader ( isr );

				fos = new FileOutputStream( outputFile );
				pw = new PrintWriter( fos );
				
				do {
					s = br.readLine();
					//change line that starts with "RC"
					//need to change the Rating Curve id
					if ( s.trim().regionMatches(true, 0,
						"RC",0,2 )) {
						s = "RC " + rcid;
					}
					if ( s == null ) { 
						//no more lines break; 
						break;
					}
					pw.println(s );	
					pw.flush(); 
				} 
				while ( s != null );
			
				br.close();
				pw.close();	

				//clean up
				fis = null;
				isr = null;
				br = null;
				s = null;

			}	
			catch (Exception e ) {
				//use debug here, not warning
				//b/c prints this message even
				//when does edit the file
				if (Message.isDebugOn) {
					Message.printDebug( 2, routine, 
					"Unable to open: \"" +
					fileToEdit + "\" for editing." );
				}
			}
			//now we have created an extra file.  
			//Move it back to original name, ie.  
			//DELETERC.GUI.tmp moved to 
			//DELETERC.GUI
			if ( outputFile.canRead() ) {
				try {
					outputFile.renameTo( inputFile );
				}		
				catch ( Exception e ) {
					Message.printWarning( 2, routine,
					"Unable to edit file \"" +
					fileToEdit + "\"." );
					if ( Message.isDebugOn ){
						Message.printDebug( 4, routine,
						"Unable to move: \"" + 
						fileToEdit + 
						".tmp\" back to: "+
						fileToEdit + "\"." );
				
						Message.printWarning( 2, 
						routine, e );
					}
						file_edited = false;
				}
			}
			else { //can't read .tmp file
				file_edited = false;
			}

		} //end if fileReadable
		else {
			Message.printWarning( 2, routine,
			"Unable to edit file: \"" +
			fileToEdit + "\"." );

			file_edited = false;
		}

	}

	return file_edited;
} //end update_deleteRC_file

/**
Re-writes the redef_segs_path file, adding one line to the top
of the file: RESEGDEF.
@param reseg_file Name of file to edit
@param return boolean to indicate if file was edited or not.
*/
public static boolean  update_resegdef_file( String reseg_file ) {
	String routine = _class + ".update_resegdef_file";

	//boolean  to return
	boolean  file_edited = true;

	//now we just need to read and write the file 
	//with the new stn id model file input
	File inputFile = null;
	FileInputStream fis;
	InputStreamReader isr;
	BufferedReader br;

	//model output going to file
	File outputFile = null;
	FileOutputStream fos;
	PrintWriter pw;
	
	String s = null;
	inputFile = new File( reseg_file );
	outputFile = new File( reseg_file + ".tmp" );
		
	//if the file is a readable file
	if ( IOUtil.fileReadable( reseg_file ) ) {
		try {
				fis = new FileInputStream( inputFile );
				isr = new InputStreamReader ( fis );
				br = new BufferedReader ( isr );

				fos = new FileOutputStream( outputFile );
				pw = new PrintWriter( fos );
				
				Vector v = new Vector();
				do {
					s = br.readLine();
					if ( s== null ) {
						//no more lines
						break;
					}
					else {
						v.addElement(s);
					}
				} while ( s!=null);
				//now add line at top
				v.insertElementAt( "RESEGDEF", 0 );

				//now print out file
				for (int i=0; i<v.size();i++) {
					String line = (String) v.elementAt(i);
					pw.println( line);
					pw.flush();
				}
			
				br.close();
				pw.close();	

				//clean up
				fis = null;
				isr = null;
				br = null;
				s = null;

			}	
			catch (Exception e ) {
				file_edited= false;
				if (Message.isDebugOn) {
					Message.printDebug( 2, routine, 
					"Unable to edit: \"" +
					reseg_file + "\" for editing." );
				}
			}
			//now we have created an extra file.  
			//Move it back to original name
			if ( outputFile.canRead() ) {
				try {
					outputFile.renameTo( inputFile );
				}		
				catch ( Exception e ) {
					Message.printWarning( 2, routine,
					"Unable to edit file \"" +
					reseg_file + "\"." );
					if ( Message.isDebugOn ){
						Message.printDebug( 4, routine,
						"Unable to move: \"" + 
						reseg_file + 
						".tmp\" back to: "+
						reseg_file + "\"." );
				
						Message.printWarning( 2, 
						routine, e );
					}
						file_edited = false;
				}
			}
			else { //can't read .tmp file
				file_edited = false;
			}

		} //end if fileReadable
		else {
			Message.printWarning( 2, routine,
			"Unable to edit file: \"" +
			reseg_file + "\"." );

			file_edited = false;
		}

	return file_edited;
} //end update_deleteRC_file

} //end class NWSRFS_Util
