// NWSRFS_NTWK - class to store the organizational information about an NWSRFS Network parameters.

package RTi.DMI.NWSRFS_DMI;

import RTi.Util.Time.DateTime;

/**
The NWSRFS_NTWK class stores the organizational information about an
NWSRFS Network parameters. This class holds the information stored in the 
preprocesseor parameteric database for network information. It holds the parameters 
from the NTWK parameteric datatype which is a network parameter
type. All of the parameters are read from the FS5Files binary DB file: 
PPPPARM<i>n</i> where <i>n</i> is determined from the preproccesed database index
file PPPINDEX. All PPDB (Preprocessed Parameteric DataBase) parameters actually 
reside in an array on the DB. This array must be parsed according to the type of 
parameter requested. For more information see below:
<pre>
    IX.4.3B-PPPPARMn  PREPROCESSOR PARAMETRIC DATA BASE FILE PPPPARMn

Purpose
Files PPPPARMn contain the Preprocessor Parametric Data Base parameter records.

Description
ATTRIBUTES: fixed length 64 byte binary records
RECORD STRUCTURE:

            Word
Variable    Type    Dimension    Position    Description

The first record in the file is a file control record:

MAXREC      I*4     1            1           Maximum records

LASTRC      I*4     1            2           Last record used

NUMPRM      I*4     1            3           Number of parameter 
                                             records in file

                                 4+          Unused

The remaining records in the file are the Parameter Records.

For regular parameter types:
NWRDS       I*4     1            1           Number of words in record

ID          A8      1            2-3         Identifier

ITYPE       A4      1            4           Parameter type

IRECNX      I*4     1            5           Record number of next 
                                             parameter record of this 
                                             type

PARMS       R*4     NWRDS-5      6+          Parameters

For special parameter types: 1/
Special parameter type control record: 2/
NWORDS      I*4     1            1           Number of words in 
                                             control record

FTYPE       I*4     1            2           Record number of first 
                                             entry

NTYPER      I*4     1            3           Number of records for 
                                             each entry
 
NTYPES      I*4     1            4           Number of values per 
                                             entry

TYPE        A4      1            5           Parameter type

NENTRY      I*4     1            6           Number of entries per 
                                             station

NSTAS       I*4     1            7           Number of stations 
                                             defined

MAXSTA      I*4     1            8           Maximum number of 
                                             stations

NXTSTA      I*4     1            9           Last station slot used

Special parameter type record: 3/
PARMS       I*4     ?            1           Special parameters 4/

Notes:

1/  Special parameter types have records that hold the same information for 
    each station (or other entity) for each month (or other key).  For these 
    types space is reserved for all possible entries when the files are 
    created.  These records are for station precipitation characteristics 
    (CHAR) and mean monthly maximum/minimum temperatures (MMMT) which are 
    stored by month for all stations.

2/  This record design is flexible and can accept additional special parameter 
    types if necessary.  This record provides the information needed to compute 
    the record number needed for a special parameter type and will precede 
    the set of special parameter records.  By using that first record of the 
    type and the number of physical records for each entry the record for the 
    appropriate month (or other key) is computed.  The Parameter Type Directory 
    points to this record for CHAR and MMMT. 
    
    Any previously deleted station slots will be reused before using the 
    next available slot.

3/  The special parameter type records immediately follow the special 
    parameter type control record.

4/  If the special parameter type is 'CHAR' then the values stored are in 
    units of hundredths of an IN.

    If the special parameter type is 'MMMT' then the values stored are in 
    units of tenths of DEGF.
    
                IX.4.3B-PPPPARMn
</pre>
<p>
<pre>
IX.4.3C-NTWK     PREPROCESSOR PARAMETRIC DATA BASE PARAMETER ARRAY NTWK:
                 NETWORK COMPUTATION INDICATOR PARAMETERS

Purpose
Parameter array NTWK contains parameters that indicate which parts of
the program PPINIT command NETWORK need to be run.

Array Contents
  Starting                         Input/
  Position     Dimension   Type   Generated Description

      1            1       I*4        G      Parameter array version number

      2            4       I*4        G      Date parameters last updated:
                                                (1) = month
                                                (2) = day
                                                (3) = year; 4 digits
                                                (4) = hour and minutes;
                                                      military time

      6            1       I*4        G      Number of indicators

  Positions 7 through 23 contain the indicators: 1/

      7            1       I*4        G      Update 5 closest PCPN stations
                                             per quadrant and check
                                             identifiers of stations with
                                             significant weights

      8            1       I*4        G      Update 3 closest 6 hour PCPN
                                             stations per quadrant

      9            1       I*4        G      Update 3 closest
                                             maximum/minimum TEMP stations
                                             per quadrant

     10            1       I*4        G      Update 3 closest instantaneous
                                             TEMP stations per quadrant

     11            1       I*4        G      Update 2 closest forecast TEMP
                                             stations per quadrant

     12            1       I*4        G      Update MAP time distribution
                                             l/D**2 or l/D**POWER weights

     13            1       I*4        G      Update MAP grid point, Thiessen
                                             or l/D**POWER weights

     14            1       I*4        G      Update MAT grid point or
                                             1/D**POWER weights

     15            1       I*4        G      Update MAP area parameters due
                                             to network boundary change

     16            1       I*4        G      Update MAT area parameters due
                                             to network boundary change

     17            1       I*4        G      Update MAPE l/D**POWER weights

     18            1       I*4        G      Update 24 hour precipitation
                                             station alphabetical order list
                                             (parameter array OP24)

     19            1       I*4        G      Update less than 24 hour
                                             precipitation station
                                             alphabetical order list
                                             (parameter array OPVR)

     20            1       I*4        G      Update temperature station
                                             alphabetical order list
                                             (parameter array OT24)

     21            1       I*4        G      Update evaporation station
                                             alphabetical order list
                                             (parameter array OE24)

     22            1       I*4        G      Update river, reservoir and
                                             snow station alphabetical order
                                             list (parameter array ORRS)

     23            1       I*4        G      Update station grid point
                                             locations (parameter array
                                             GP24) and grid station
                                             alphabetical order list
                                             (parameter array OG24)

     24            2       R*4        G      Unused

Notes:
1/ The value of the indicators are as follows:
      0 = no (do not do computations)
      1 = yes (do computations)


                    IX.4.3C-NTWK-2
</pre>
*/
public class NWSRFS_NTWK {

// General parameters set for all Network parameters.
/**
Identifier for the Network.
*/
protected String _ID;

/**
This logical unit number corresponds to the <i>n</i> in the file
PPPPARM<i>n</i> and is used to pull all of the Network information.
*/
protected int _logicalUnitNum;

/**
The record number in the PPPINDEX file for retrieving data from the 
PPPPARM<i>n</i> file.
*/
protected int _recordNum;

/**
Network DateTime Value of last update.
*/
protected DateTime _networkDateTime;

/**
Network Month of last update.
*/
protected int _networkMonth;

/**
Network Day of last update.
*/
protected int _networkDay;

/**
Network Year of last update
*/
protected int _networkYear;

/**
Network Hour and Minutes of last update
*/
protected int _networkHourMin;

/**
Network Number of indicators.
*/
protected int _networkNumberInd;

/**
Network Update 5 closest PCPN stations.
*/
protected int _networkFivePCPNInd;

/**
Network Update 3 closest 6 Hour PCPN stations.
*/
protected int _networkThreePCPNInd;

/**
Network Update 3 closest Max/Min TEMP stations.
*/
protected int _networkThreeMaxMinTEMPInd;

/**
Network Update 3 closest Instantaneous TEMP stations.
*/
protected int _networkThreeInstTEMPInd;

/**
Network Update 2 closest forecast TEMP stations.
*/
protected int _networkTwoForecastTEMPInd;

/**
Network Update MAP time distribution weights.
*/
protected int _networkMAPTimeDistWeights;

/**
Network Update MAP grid point weights.
*/
protected int _networkMAPGridPointWeights;

/**
Network Update MAT grid point weights.
*/
protected int _networkMATGridPointWeights;

/**
Network Update MAP parameters due to basin boundary changes.
*/
protected int _networkMAPParamBasnBound;

/**
Network Update MAT parameters due to basin boundary changes.
*/
protected int _networkMATParamBasnBound;

/**
Network Update MAPE weights.
*/
protected int _networkMAPEWeights;

/**
Network Update precip station alphebetical order list (OP24).
*/
protected int _networkOP24UpdateInd;

/**
Network Update less than 24 hour precip station alphebetical order list (OPVR).
*/
protected int _networkOPVRUpdateInd;

/**
Network Update temperature station alphebetical order list (OT24).
*/
protected int _networkOT24UpdateInd;

/**
Network Update evaporation station alphebetical order list (OE24).
*/
protected int _networkOE24UpdateInd;

/**
Network Update RRS station alphebetical order list (ORRS).
*/
protected int _networkORRSUpdateInd;

/**
Network Update grid point locations (GP24) and grid station alphebetical 
order list (OG24).
*/
protected int _networkGPOG24UpdateInd;

/**
Constructor.
@param id Network ID.  Can not be null
*/
public NWSRFS_NTWK(String id) {
	initialize();

	if (id != null) {
		_ID = id;
	}
}

// GET Member methods for general Network variables
/**
Returns the Network identifier.
Member method for general Network variables
@return the Network identifier.
*/
public String getID() {
	return _ID;
}

/**
Returns the logical unit number for the Network.
@return the logical unit number for the Network.
*/
public int getLogicalUnitNum() {
	return _logicalUnitNum;
}

/**
Returns the Network update DateTime object.
@return the Network update DateTime.
*/
public DateTime getNTWKDateTime() {
	return _networkDateTime;
}

/**
Returns the Network update month value.
@return the Network update month.
*/
public int getNTWKMonth() {
	return _networkMonth;
}

/**
Returns the Network update day value.
@return the Network update day.
*/
public int getNTWKDay() {
	return _networkDay;
}

/**
Returns the Network update 4 digit year value.
@return the Network update year.
*/
public int getNTWKYear() {
	return _networkYear;
}

/**
Returns the Network update Hour and Minute military time value.
@return the Network update Hour and Minute.
*/
public int getNTWKHourMin() {
	return _networkHourMin;
}

/**
Returns the Network number of indicator values.
@return the Network number of indicators.
*/
public int getNTWKNumberInd() {
	return _networkNumberInd;
}

/**
Returns the Network update 5 closest PCPN stations.
@return the Network update 5 closest PCPN stations indicator.
*/
public int getNTWKFivePCPNInd() {
	return _networkFivePCPNInd;
}

/**
Returns the Network update 3 closest PCPN 6 hour stations.
@return the Network update 3 closest PCPN 6 hour stations indicator.
*/
public int getNTWKThreePCPNInd() {
	return _networkThreePCPNInd;
}

/**
Returns the Network update 3 closest Max/Min TEMP stations.
@return the Network update 3 closest Max/Min TEMP stations indicator.
*/
public int getNTWKThreeMaxMinTEMPInd() {
	return _networkThreeMaxMinTEMPInd;
}

/**
Returns the Network update 3 closest instantaneous TEMP stations.
@return the Network update 3 closest instantaneous stations indicator.
*/
public int getNTWKThreeInstTEMPInd() {
	return _networkThreeInstTEMPInd;
}

/**
Returns the Network update 2 closest forecast TEMP stations.
@return the Network update 2 closest forecast TEMP stations indicator.
*/
public int getNTWKTwoForecastTEMPInd() {
	return _networkTwoForecastTEMPInd;
}

/**
Returns the Network update MAP distribution weights.
@return the Network update MAP distribution weights indicator.
*/
public int getNTWKMAPTimeDistWeights() {
	return _networkMAPTimeDistWeights;
}

/**
Returns the Network update MAP grid point weights.
@return the Network update MAP grid point weights indicator.
*/
public int getNTWKMAPGridPointWeights() {
	return _networkMAPGridPointWeights;
}

/**
Returns the Network update MAT grid point weights.
@return the Network update MAT grid point weights indicator.
*/
public int getNTWKMATGridPointWeights() {
	return _networkMATGridPointWeights;
}

/**
Returns the Network update MAP parameters due to basin boundry change.
@return the Network update MAP parameters due to basin boundry change indicator.
*/
public int getNTWKMAPParamBasnBound() {
	return _networkMAPParamBasnBound;
}

/**
Returns the Network update MAT parameters due to basin boundry change.
@return the Network update MAT parameters due to basin boundry change indicator.
*/
public int getNTWKMATParamBasnBound() {
	return _networkMATParamBasnBound;
}

/**
Returns the Network update MAPE weights.
@return the Network update MAPE weights indicator.
*/
public int getNTWKMAPEWeights() {
	return _networkMAPEWeights;
}

/**
Returns the Network update precip station alphabetical order list (OP24).
@return the Network update precip station alphabetical order list (OP24) 
indicator.
*/
public int getNTWKOP24UpdateInd() {
	return _networkOP24UpdateInd;
}

/**
Returns the Network update less 24 hour precip station alphabetical order list 
(OPVR).
@return the Network update less 24 hour precip station alphabetical order list 
(OP24) indicator.
*/
public int getNTWKOPVRUpdateInd() {
	return _networkOPVRUpdateInd;
}

/**
Returns the Network update temp station alphabetical order list (OT24).
@return the Network update temp station alphabetical order list (OT24) 
indicator.
*/
public int getNTWKOT24UpdateInd() {
	return _networkOT24UpdateInd;
}

/**
Returns the Network update evap station alphabetical order list (OE24).
@return the Network update evap station alphabetical order list (OE24) 
indicator.
*/
public int getNTWKOE24UpdateInd() {
	return _networkOE24UpdateInd;
}

/**
Returns the Network update RRS station alphabetical order list (ORRS).
@return the Network update RRS station alphabetical order list (ORRS) 
indicator.
*/
public int getNTWKORRSUpdateInd() {
	return _networkORRSUpdateInd;
}

/**
Returns the Network update Grid Point location (GP24) and Grid station 
alphabetical order list (OG24).
@return the Network update Grid point locator (GP24) and Grid station 
alphabetical order list (OG24) indicator.
*/
public int getNTWKGPOG24UpdateInd() {
	return _networkGPOG24UpdateInd;
}

/**
Returns the record number for the Network parameteric data.
@return the record number for the Network.
*/
public int getRecordNum() {
	return _recordNum;
}

/**
Initialize data members.
*/
private void initialize() {
	_ID				= null;
	_logicalUnitNum			= -1;
	_networkDateTime 		= new DateTime();
	_networkMonth			= -1;
	_networkDay			= -1;
	_networkYear			= -1;
	_networkHourMin			= -1;
	_networkNumberInd		= -1;
	_networkFivePCPNInd		= -1;
	_networkThreePCPNInd		= -1;
	_networkThreeMaxMinTEMPInd	= -1;
	_networkThreeInstTEMPInd	= -1;
	_networkTwoForecastTEMPInd	= -1;
	_networkMAPTimeDistWeights	= -1;
	_networkMAPGridPointWeights	= -1;
	_networkMATGridPointWeights	= -1;
	_networkMAPParamBasnBound	= -1;
	_networkMATParamBasnBound	= -1;
	_networkMAPEWeights		= -1;
	_networkOP24UpdateInd		= -1;
	_networkOPVRUpdateInd		= -1;
	_networkOT24UpdateInd		= -1;
	_networkOE24UpdateInd		= -1;
	_networkORRSUpdateInd		= -1;
	_networkGPOG24UpdateInd		= -1;
	_recordNum			= -1;
}

// SET Member methods for general Network variables
/**
Sets the Network identifier.
Member method for general Network variables
*/
public void setID(String id) {
	_ID = id;
}

/**
Sets a logical unit number to the _logicalUnitNum int. This value will be
the <i>n</i> in the file PPPPARM<i>n</i>.
@param logicalUnitNum is the actual file unit number to set.
*/
public void setLogicalUnitNum(int logicalUnitNum) {
	_logicalUnitNum = logicalUnitNum;
}

/**
Sets the Network update DateTime object with a DateTime object.
@param networkDateTime the Network update DateTime.
*/
public void setNTWKDataTime(DateTime networkDateTime) {
	_networkDateTime = networkDateTime;
}

/**
Sets the Network update DateTime object with a MM/DD/YYYY HH:Min values.
@param networkMonth the Network update Month.
@param networkDay the Network update Day.
@param networkYear the Network update Year.
@param networkHourMin the Network update HourMin.
*/
public void setNTWKDateTime(
	int networkMonth,
	int networkDay,
	int networkYear,
	int networkHourMin) {
		
	if(_networkDateTime == null)
		_networkDateTime = new DateTime();
	
	_networkDateTime.setMonth(networkMonth);
	_networkDateTime.setDay(networkDay);
	_networkDateTime.setYear(networkYear);
	String HourMin = String.valueOf(networkHourMin);
	int hour = Integer.valueOf(HourMin.substring(0,2)).intValue();
	int min = Integer.valueOf(HourMin.substring(2)).intValue();
	_networkDateTime.setHour(hour);
	_networkDateTime.setMinute(min);
}

/**
Sets the Network update month value.
@param networkMonth the Network update month.
*/
public void setNTWKMonth(int networkMonth) {
	_networkMonth = networkMonth;
}

/**
Sets the Network update day value.
@param networkDay the Network update day.
*/
public void setNTWKDay(int networkDay) {
	_networkDay = networkDay;
}

/**
Sets the Network update 4 digit year value.
@param networkYear the Network update year.
*/
public void setNTWKYear(int networkYear) {
	_networkYear = networkYear;
}

/**
Sets the Network update Hour and Minute military time value.
@param networkHourMin the Network update Hour and Minute.
*/
public void setNTWKHourMin(int networkHourMin) {
	_networkHourMin = networkHourMin;
}

/**
Sets the Network number of indicator values.
@param networkNumberInd the Network number of indicators.
*/
public void setNTWKNumberInd(int networkNumberInd) {
	_networkNumberInd = networkNumberInd;
}

/**
Sets the Network update 5 closest PCPN stations.
@param networkFivePCPNInd the Network update 5 closest PCPN stations indicator.
*/
public void setNTWKFivePCPNInd(int networkFivePCPNInd) {
	_networkFivePCPNInd = networkFivePCPNInd;
}

/**
Sets the Network update 3 closest PCPN 6 hour stations.
@param networkThreePCPNInd the Network update 3 closest PCPN 6 hour 
stations indicator.
*/
public void setNTWKThreePCPNInd(int networkThreePCPNInd) {
	_networkThreePCPNInd = networkThreePCPNInd;
}

/**
Sets the Network update 3 closest Max/Min TEMP stations.
@param networkThreeMaxMinTEMPInd the Network update 3 closest Max/Min TEMP 
stations indicator.
*/
public void setNTWKThreeMaxMinTEMPInd(int networkThreeMaxMinTEMPInd) {
	_networkThreeMaxMinTEMPInd = networkThreeMaxMinTEMPInd;
}

/**
Sets the Network update 3 closest instantaneous TEMP stations.
@param networkThreeInstTEMPInd the Network update 3 closest instantaneous 
stations indicator.
*/
public void setNTWKThreeInstTEMPInd(int networkThreeInstTEMPInd) {
	_networkThreeInstTEMPInd = networkThreeInstTEMPInd;
}

/**
Sets the Network update 2 closest forecast TEMP stations.
@param networkTwoForecastTEMPInd the Network update 2 closest forecast TEMP 
stations indicator.
*/
public void setNTWKTwoForecastTEMPInd(int networkTwoForecastTEMPInd) {
	_networkTwoForecastTEMPInd = networkTwoForecastTEMPInd;
}

/**
Sets the Network update MAP distribution weights.
@param networkMAPTimeDistWeights the Network update MAP distribution weights 
indicator.
*/
public void setNTWKMAPTimeDistWeights(int networkMAPTimeDistWeights) {
	_networkMAPTimeDistWeights = networkMAPTimeDistWeights;
}

/**
Sets the Network update MAP grid point weights.
@param networkMAPGridPointWeights the Network update MAP grid point weights 
indicator.
*/
public void setNTWKMAPGridPointWeights(int networkMAPGridPointWeights) {
	_networkMAPGridPointWeights = networkMAPGridPointWeights;
}

/**
Sets the Network update MAT grid point weights.
@param networkMATGridPointWeights the Network update MAT grid point weights 
indicator.
*/
public void setNTWKMATGridPointWeights(int networkMATGridPointWeights) {
	_networkMATGridPointWeights = networkMATGridPointWeights;
}

/**
Sets the Network update MAP parameters due to basin boundry change.
@param networkMAPParamBasnBound the Network update MAP parameters due to basin 
boundry change indicator.
*/
public void setNTWKMAPParamBasnBound(int networkMAPParamBasnBound) {
	_networkMAPParamBasnBound = networkMAPParamBasnBound;
}

/**
Sets the Network update MAT parameters due to basin boundry change.
@param networkMATParamBasnBound the Network update MAT parameters due to 
basin boundry change indicator.
*/
public void setNTWKMATParamBasnBound(int networkMATParamBasnBound) {
	_networkMATParamBasnBound = networkMATParamBasnBound;
}

/**
Sets the Network update MAPE weights.
@param networkMAPEWeights the Network update MAPE weights indicator.
*/
public void setNTWKMAPEWeights(int networkMAPEWeights) {
	_networkMAPEWeights = networkMAPEWeights;
}

/**
Sets the Network update precip station alphabetical order list (OP24).
@param networkOP24UpdateInd the Network update precip station alphabetical 
order list (OP24) indicator.
*/
public void setNTWKOP24UpdateInd(int networkOP24UpdateInd) {
	_networkOP24UpdateInd = networkOP24UpdateInd;
}

/**
Sets the Network update less 24 hour precip station alphabetical order list 
(OPVR).
@param networkOPVRUpdateInd the Network update less 24 hour precip station 
alphabetical order list (OP24) indicator.
*/
public void setNTWKOPVRUpdateInd(int networkOPVRUpdateInd) {
	_networkOPVRUpdateInd = networkOPVRUpdateInd;
}

/**
Sets the Network update temp station alphabetical order list (OT24).
@param networkOT24UpdateInd the Network update temp station alphabetical 
order list (OT24) indicator.
*/
public void setNTWKOT24UpdateInd(int networkOT24UpdateInd) {
	_networkOT24UpdateInd = networkOT24UpdateInd;
}

/**
Sets the Network update evap station alphabetical order list (OE24).
@param networkOE24UpdateInd the Network update evap station alphabetical 
order list (OE24) indicator.
*/
public void setNTWKOE24UpdateInd(int networkOE24UpdateInd) {
	_networkOE24UpdateInd = networkOE24UpdateInd;
}

/**
Sets the Network update RRS station alphabetical order list (ORRS).
@param networkORRSUpdateInd the Network update RRS station alphabetical 
order list (ORRS) indicator.
*/
public void setNTWKORRSUpdateInd(int networkORRSUpdateInd) {
	_networkORRSUpdateInd = networkORRSUpdateInd;
}

/**
Sets the Network update Grid Point location (GP24) and Grid station 
alphabetical order list (OG24).
@param networkGPOG24UpdateInd the Network update Grid point locator (GP24) and 
Grid station alphabetical order list (OG24) indicator.
*/
public void setNTWKGPOG24UpdateInd(int networkGPOG24UpdateInd) {
	_networkGPOG24UpdateInd = networkGPOG24UpdateInd;
}

/**
Sets a record number to the _recordNum int. This value will be
the record in the file PPPPARM<i>n</i> file to find data for the given Network 
Boundary ID.
@param recordNum is the actual record number.
*/
public void setRecordNum(int recordNum) {
	_recordNum = recordNum;
}

/**
toString method prints name of ID.
@return a String value with _ID value or "NTWK:"_ID value
*/
public String toString() {
	return _ID;
}

}
