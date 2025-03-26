//------------------------------------------------------------------------------
// NWSRFS_PDBDLY - class to contain the preprocessor 
// index/record Data for daily data
//------------------------------------------------------------------------------
// History:
//
// 2004-11-3	Scott Townsend, RTi	Initial version.
//------------------------------------------------------------------------------
// Endheader

package RTi.DMI.NWSRFS_DMI;

/**
The NWSRFS_PDBDLY - class to contain the preprocessor record data for Daily data
and is used to retrieve the data record for a specific time series. This class 
reads and stores data from the PDBDLY<i>n</i> preprocessor database file; it stores 
the contents of PDBDLY<i>n</i> for a particular station in this object. The 
PDBDLY,i>n</i> database file has the following definition:
<pre>
IX.4.2B-PDBDLYn     PREPROCESSOR DATA BASE FILE PDBDLYn


Purpose
Files PDBDLYn contain the data for daily data types (PP24, TM24,
etc.).

The data for each data type is preceded by records containing the
pointers needed by the Preprocessor. The pointers and data are stored
in Variable Length Records (VLR). There is one VLR for the pointers
and then one VLR for each day of data. The number of physical records
in a VLR depends on the maximum number of stations, the number of data
values and the number of pointer values.

The maximum number of days of data for each data type is set when the
files are created. The dates of the data must be continuous except
for forecast temperatures. The last day of data must be the same for
all data types, except forecast temperature, but the beginning date
can be different.

All pointer and data values are stored as I*2 words.

The RRS Free Pool Records are stored in one of the Daily Data Files.
See the description of file PDBRRS for the format of these records.

Description
ATTRIBUTES: fixed length 64 byte binary records

RECORD STRUCTURE:

  Daily Data Record

  Pointer Record (if needed)
  Data Record 1
     .
     .
     .
  Data Record N

  All pointer values for a station are stored together.

  Each data record contains one day of data.

  24-Hour Precipitation Pointer Record and Data Record

  The pointer record contains the following values for each station:

     o record number of PCPN parameters in the Preprocessor Parametric
       Data Base
     o location in characteristics array returned from routine RPPCHR
     o array location for less than 24-hour precipitation pointer or
       record number of station GENL parameters in the Preprocessor
       Parametric Data Base if not < 24-hour station
     o precipitation correction factors
     o MDR Box number

  The data records contain 1 value per station in hundredths of an
  inch.

  Less than 24-hour Precipitation Pointer and Data Record

  The pointer record contains the following values for each station:

     o record number of GENL parameters in the Preprocessor Parametric
       Data Base
     o array location of pointer information for 24-hour precipitation
     o data time interval (TIMINT)
     o array location of data

  The data records contain 24/TIMINT values per station in hundredths
  of an inch.

  24-Hour Maximum/Minimum Temperature Pointer and Data Record

  The pointer record contains the following values for each station:

     o record number of TEMP parameters in the Preprocessor Parametric
       Data Base
     o location of maximum/minimum temperatures returned from routine
       RPPMT
     o array location for less than 24-hour temperature pointers
     o correction factor for maximum temperatures
     o correction factor for minimum temperatures

  The data records have 2 values per station in tenths of degrees
  Fahrenheit.

  Less than 24-Hour Temperature Pointer and Data Record

  The pointer contains the following with all values for each station
  together:

     o array location of 24-hour maximum/minimum pointers
     o data time interval (TIMINT)
     o array location for less than 24-hour data

  The data records contain 24/TIMINT values per station stored in
  tenths of degrees Fahrenheit.

  Forecast Maximum/minimum Temperature Pointer and Data Record

  The pointer record contains the following with values for each
  station together:

     o array location of pointers for regular maximum/minimum data for
       this station

  Immediately following the pointer record is a special record
  containing the dates of forecast temperature data (dates do not have
  to be continuous). These are in I*4 words and the first word is the
  number of dates. Following are two words for each date that contain
  the Julian day and record number of the data for that date.

  The data records contain 2 values in tenths of degrees Fahrenheit.

  Potential Evaporation Pointer and Data Record

  The pointer record contains the following:

     o record number of station PE parameters in the Preprocessor
       Parametric Data Base

  The data records contain 6 24-hour values per station:

     o   air temperature (tenths of degrees Fahrenheit)
     o   dewpoint temperature (tenths of degrees Fahrenheit)
     o   wind (tenths of miles per hour)
     o   percent sunshine
     o   solar radiation (langleys)
     o   sky cover

  6 Hour MDR Sums Data Record

  No pointer record is needed for MDR sums. The data records contain
  6-hour MDR sums of each box in the user area for each 6-hour period.
  These data start with all MDR boxes for the period ending at 18Z
  followed by all the MDR box sums for the other periods.

  Stranger Station Precipitation Statistics and Data Record

  The statistics record contains the following stranger station
  reporting statistics (all values stored as I*2 words):

       Word
     Position     Description
        1-12      Number of new reports entered during each of the last
                  12 months (January is in location 1 and December is
                  in location 12)
          13      Month indicating which of the above 12 values was
                  most recently updated
          14      Year corresponding to month in position 13
       15-16      Julian date statistics begin
       17-18      Julian date of most recent report
          19      Largest date of most recent report
       20-12      Julian date of largest value reported
       22-23      Coordinates of largest value reported
          24      Second largest value reported (stored as value*100)
       25-26      Julian date of second largest value reported
       27-28      Coordinates of second largest value reported

  The number of stations reporting each day is stored in the first
  word of the data record.

  There are three entries for each station in the data record: the Y
  and X Polar Stereographic coordinates and a value. The coordinates
  are stored in tenths (I*2 words). The data value is in hundredths
  of an inch.


               IX.4.2B-PDBDLYn-4
</pre>
*/

// REVISIT SAT 2004-11-3 Need to finish filling this class out.
public class NWSRFS_PDBDLY {

protected String _DTYPE;

protected int _NUMID;

protected String _STAID;

/**
Constructor.
*/
public NWSRFS_PDBDLY(String ID) {
	initialize();
	setSTAID(ID);
}

// Add methods for Vector structures

// Get methods
public String getSTAID() {
	return _STAID; 
}

public int getNUMID() {
	return _NUMID; 
}

public String getDTYPE() {
	return _DTYPE; 
}

/**
Initialize global objects.
*/
private void initialize() {
	_STAID		= new String();
	_NUMID		= -1;
	_DTYPE		= new String();
}

// Set methods
public void setSTAID(String STAID) {
	_STAID = STAID; 
}

public void setNUMID(int NUMID) {
	_NUMID = NUMID; 
}

public void setDTYPE(String DTYPE) {
	_DTYPE = DTYPE; 
}

}
