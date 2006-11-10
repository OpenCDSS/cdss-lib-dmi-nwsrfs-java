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
//-----------------------------------------------------------------------------

package RTi.DMI.NWSRFS;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.EOFException;
import java.io.StreamCorruptedException;
import java.util.Vector;
import RTi.Util.IO.ProcessManager;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeUtil;

/**
The NWSRFS_Util class stores NWSRFS-relateed utility functions.
@deprecated This class has been deprecated since the name has been changed
to NWSRFS_Util.java
*/
public class NWSRFSUtil {

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

	DateTime d = new DateTime(DateTime.PRECISION_HOUR);
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
{	// make sure the given date is valid
	String dateString = "" 
		+ StringUtil.formatString(month, "%02d")
		+ "/" 
		+ StringUtil.formatString(day, "%02d")
		+ "/" + 
		year;

	DateTime dt = DateTime.parse(dateString);

	int leapYear = 0;
	if (TimeUtil.isLeapYear(year)) {
		leapYear = 1;
	}

	// the following is rather odd-looking math, but it does the 
	// proper calculation.
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

// REVISIT - are hours 0-23 or 1-24?
/**
Calculates a Julian Hour from a given date and returns it.  Julian hours
start at 1 on January 1, 1900 @ 0100, and go up from there (ie, 
01/01/1900@0200 == 2, 01/01/1900@0300 == 3, 01/02/1900@0100 == 25, etc).

@return the julian hour representing the given date and time.
*/
public static int getJulianHour1900FromDate (int month, int day, int year,
	int hour) 
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
		IDSAV = 0,	// input - default to no time zone change
		CODE = 0;	// input - default to no time zone change
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

	DateTime date = new DateTime ( DateTime.PRECISION_HOUR );
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
made.  If the hour is 24, then the hour is set to 0 and the day is increased by
1.
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
		else {	// Return the instance that was passed in...
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
		// previous day, handling changes to month as usual...
		DateTime dt = new DateTime ( datetime );
		dt.addDay ( -1 );
		dt.setHour ( 24 );
		return dt;
	}
	else {	// No change needed.
		if ( always_copy ) {
			// Always make a copy...
			return new DateTime ( datetime );
		}
		else {	// Return the instance that was passed in...
			return datetime;
		}
	}
}


	/**  get_apps_defaults
	Method to resolve the value of an apps-defaults requested string.
	
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
		<resource> is any string, the value returned depends on certain file conventions:
 
		1. A valid t-r requires a valid token followed by a valid resource,
		2. the t-r relationship must be contained on a single line,
		3. no white space needs to surround <delimiter>,
		4. comments are indicated by a #,
		5. neither <token> nor <resource> can begin with a # or :,
		6. a # or a : can be embedded within <resource>,
		7. <resource> can contain white space if it is bounded by the ' or " characters,
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
	*/

	public static String get_apps_defaults(String request)
	{
		int appDFileIndex;
		String routine = "NWSRFS_Util.get_apps_defaults";
		String[] appDFile;
		String appDFileValue = null;
		String requestValue  = null;

		// Load the appDFile environment variable tokens for the various app-defaults
		// files to pull from the users enviornment.
		appDFile = new String[4];
		appDFile[0] = "APPS_DEFAULTS_USER";	/* apps-defaults personal file */
		appDFile[1] = "APPS_DEFAULTS_PROG";	/* apps-defaults for specific program */
		appDFile[2] = "APPS_DEFAULTS_SITE";	/* apps-defaults for local site file */
		appDFile[3] = "APPS_DEFAULTS";		/* apps-defaults default file */

		// Now check the environment for the request token. If it is the environment
		// then return otherwise continue.
		if((requestValue = getenv(request)) != null)
		{
			return requestValue;
		}
		else // Now check app-defaults files
		{
			// Loop through the app-defaults files  
			for(appDFileIndex=0;appDFileIndex < 4;appDFileIndex++)
			{
				if((appDFileValue = getenv(appDFile[appDFileIndex])) != null)
				{
					// Now check to see if the token value is in the app-defaults file
					// if so we return it.
					if((requestValue = get_token(request,appDFileValue)) != null)
					{
						return requestValue;
					}
				}
			}
		}

		// If we have not returned by here then the request was not found in the
		// enviornment or any of the apps-defaults files. So return a null String
		return null;
	}

	/**  get_token
	This method searchs the supplied apps-defaults file for the given request token. This
	method needs to be finished to allow for referback variables (tokens) in an apps-defaults
	token but currently it will not do that. That would take a considerable effort to do.
	@param request is the request string to search the apps-defaults file.
	@param appsDefaultsfile is the apps-defaults file to do the search.
	@return is the value of the apps-defaults String token if found or null.
	*/
	private static String get_token(String request, String appsDefaultsFile)
	{
		String routine = "NWSRFS_Util.get_token";
		String delim = ":";	/* delimiter character */
		char comment = '#';	/* comment character */
		char quote1 = '\"';	/* 1st valid quote character */
		char quote2 = '\'';	/* 2nd valid quote character */
		char bslash= '\\';	/* back slash */
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

				// Now check the parseString string list for the request string.
				checkString = ((String)parseString.elementAt(0)).trim();
				if(checkString.equalsIgnoreCase(request))
				{
					checkString = ((String)parseString.elementAt(1)).trim();
					
					// If the token has a comment before it continue.
					if(checkString.charAt(0) == '#')
						continue;

					parseString = StringUtil.breakStringList(checkString," ",3);
					checkString = ((String)parseString.elementAt(0)).trim();
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
		catch(EOFException EOFe)
		{
			Message.printWarning(2,routine,"An Exception occured: "+EOFe.getMessage());
			return null;
		}
		catch(FileNotFoundException FNFe)
		{
			Message.printWarning(2,routine,"An Exception occured: "+FNFe.getMessage());
			return null;
		}
		catch(SecurityException SEe)
		{
			Message.printWarning(2,routine,"An Exception occured: "+SEe.getMessage());
			return null;
		}
		catch(IOException IOe)
		{
			Message.printWarning(2,routine,"An Exception occured: "+IOe.getMessage());
			return null;
		}

		// If we get here we return null
		return null;
	}

	/** getenv
	This method is a replacement method for the System.getenv which was deprecated.
	@param request is the request string to search the users environment.
	@return is the value of the return String token if found or null.
	*/
	private static String getenv(String request)
	{
		int i, tokenIndex = -1, exitstat = -999;
		String routine = "NWSRFS_Util.getenv";
		String returnValue = null;
		String cmd = null;
		Vector value_list = new Vector();
		ProcessManager pm;

		// Try to catch NullPointerExceptions
		try {
			// Check to see if the request String is in the System properties
			// if so return it.
			if((returnValue = System.getProperty(request)) != null)
			{
				return returnValue;
			}
			else // We have to make a OS call to get the enviornment variable value
			{
				// We check to see if we are a UNIX system otherwise we just return null
				if(System.getProperty("os.name").equalsIgnoreCase("Linux"))
				{
					try 
					{
						cmd = "env";
						pm = new ProcessManager(cmd);
						pm.saveOutput( true );
						pm.run();
						value_list = pm.getOutputVector();
						exitstat = pm.getExitStatus();
						if (( exitstat != 0 ) || (value_list.size()== 0))
						{
							return null;
						}
						else  
						{
							// Parse the env and look for request String.
							for(i=0;i<value_list.size();i++)
							{
								if(StringUtil.indexOfIgnoreCase((String)value_list.elementAt(i),request,0) >= 0)
								{
									tokenIndex = i;
									break;
								}
							}

							if(tokenIndex < 0)
								return null;

							returnValue = (String)value_list.elementAt(tokenIndex);
							if(returnValue.length() == 0)
								return null;
							else
							{
								value_list = StringUtil.breakStringList(returnValue,"=",3);
								returnValue = (String)value_list.elementAt(1);
								return returnValue;
							}
						}
					}
					catch (Exception e ) 
					{
						Message.printWarning(2,routine,"An Exception occured: "+e.getMessage());
						return null;
					}
				}
				else
					return null;
			}
		}
		catch(NullPointerException NPe)
		{
			Message.printWarning(2,routine,"An Exception occured: "+NPe.getMessage());
			return null;
		}

	}
} //end class NWSRFS_Util
