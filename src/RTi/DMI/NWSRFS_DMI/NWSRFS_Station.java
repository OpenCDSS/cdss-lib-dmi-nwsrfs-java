// NWSRFS_Station - class to store the organizational information about an NWSRFS station.

package RTi.DMI.NWSRFS_DMI;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
The NWSRFS_Station class stores the organizational information about an
NWSRFS Station. This class holds the information stored in the preprocesseor
parameteric database for station information. It holds the parameters from the
GENL, PCPN, TEMP, PE, and RRS parameteric datatypes which are station parameter
types. All of the parameters are read from the FS5Files binary DB file: 
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
    IX.4.3C-GENL  PREPROCESSOR PARAMETRIC DATA BASE PARAMETER ARRAY GENL: 
                  STATION GENERAL PARAMETERS

Purpose
Parameter array GENL contains station general parameters.

Array Contents 1/

    Starting                     Input/
    Position  Dimension  Type    Generated   Description

    1         1          I*4     G           Parameter array 
                                             version number

    2         1          A8      I           Station identifier

    4         1          I*4     I           Station number

    5         1          A20     I           Description

    10        1          R*4     I           Elevation; 
                                             units of M

    11        2          R*4     I           Location; latitude 
                                             and longitude; 
                                             units of decimal 
                                             degrees

    13        2          I*4     G           NWSRFS/HRAP coordinates; 
                                             stored as (X,Y)

    15        1          I*4     G           Complete indicator: 2/
                                               0 = complete
                                               1 = incomplete

    16        1          A2      I           United States Postal 
                                             Service 2 character 
                                             state identifier

    17        1          I*4     I           Grid point address 
                                             used by the WGRFC 
                                             MARO Function:
                                               -999 = undefined or 
                                               outside of grid system

    18        1          I*4     G           Number of data 
                                             groups (NGPS)

    19        NGPS       A4      I           Data group codes 3/

 
    19+NGPS   NGPS       I*4     G           Record number of 
                                             parameter record in 
                                             Preprocessor Parametric 
                                             Data Base for each group

    IPCPN*    1          I*4     G           Array location of 
    (19+NGPS*2)                              pointers for 24 hour 
                                             PCPN data 4/
                                
    IPCPN*    1          I*4     G           Array location of
    (19+NGPS*2+IPCPN)                        pointers for less 
                                             than 24 hour PCPN data 4/

    IPCPN*    1          I*4     I           Data time interval of
    (19+NGPS*2+IPCPN*2)                      less than 24 hour PCPN data: 4/
                                               0 = data time interval is 24 hours

    IPCPN*    1          I*4     G           Array location of
    (19+NGPS*2+IPCPN*3)                      precipitation characteristics 
                                             for this station: 6/
                                               0 = characteristics not used

    ITEMP*    1          I*4     G           Array location of
    (19+NGPS*2+IPCPN*4)                      pointers for maximum/minimum 
                                             TEMP data 4/

    ITEMP*    1          I*4     G           Array location of
    (19+NGPS*2+IPCPN*4                       pointers for
    +ITEMP)                                  instantaneous TEMP data 4/

    ITEMP*    1          I*4     I           Data time interval of
    (19+NGPS*2+IPCPN*4                       instantaneous TEMP
    +ITEMP*2)                                data 4/

    ITEMP*    1          I*4     G           Array location of
    (19+NGPS*2+IPCPN*4                       pointers for Forecast
    +ITEMP*3)                                maximum/minimum 
                                             TEMP data 4/

    ITEMP*    1          R*4     I           Fe Factor for TEMP
    (19+NGPS*2+IPCPN*4                       weights; units of 
    +ITEMP*4)                                KM/1000M 4/

    IPE*      1          I*4     G           Array location of
    (19+NGPS*2+IPCPN*4                       pointers for PE
    +ITEMP*4)                                data 4/

    19+NGPS*2 1          I*4     G           Number of Data Entry
    +IPCPN*4                                 source codes (NSRC) 5/
    +ITEMP*5
    +IPE

The following 3 items are repeated NSRC times:

    20+NGPS*2 1          A4      I           Data Entry source
    +IPCPN*4                                 code 5/
    +ITEMP*5
    +IPE

              1          I*4     G           Number of words in 
                                             Data Entry source 
                                             identifier (NSID)

              NSID       A4      I           Data Entry source 
                                             identifier

    20+NGPS*2 1          I*4     I           Number of GOES data
    +ICPCN*4                                 groups or RRS data
    +ITEMP*5                                 types that are not to
    +IPE                                     be transferred by the
    +NSRC*(2+NSID)                           GOES data transfer programs (NGOES)

    21+NGPS*2 NGOES      A4      I           GOES data groups or
    +IPCPN*4                                 RRS data types not to
    +ITEMP*5                                 be transferred
    +IPE
    +NSRC(2+NSID)

    22+NGPS*2 1          I*4     I           Number of CDAS data
    +IPCPN*4                                 groups or RRS data
    +ITEMP*5                                 types that are not to
    +IPE                                     be transferred by the
    +NSRC(2+NSID)                            GOES data transfer
    +NGOES                                   programs (NCDAS)

    23+NGPS*2 NCDAS      A4      I           CDAS data groups or 
    +IPCPN*4                                 RRS data types not to
    +ITEMP*5                                 be transferred
    +IPE
    +NSRC(2+NSID)
    +NGOES

Notes:

1/  If the starting position is zero then there is no value stored for this 
    variable.

2/  Indicates whether entries were successfully made in all appropriate 
    Preprocessor Data Base and Preprocessor Parametric Data Base files for 
    this station.

3/  Valid codes are PCPN, TEMP, PE and RRS.  IPCPN, ITEMP and IPE are indicators 
    for which group of data are defined.  If the group is defined then the 
    indicator has a value of 1.  If the group is not defined then the indicator 
    has a value of zero.

4/  Variable exists only if station has this data group.  Array location is 
    the location of the pointers in the pointer array returned from the 
    Preprocessor Data Base routine RPDDLY for the given data type.

5/  Used for creating Data Entry control file entries for processing SASM, 
    GOES and other data.  The current valid codes and the corresponding data 
    source are:
        SA   -  Synoptic Airways data from NMC files
        SM   -  Synoptic Meteorological data from NMC files
        GHB5 -  Geostationary Orbit Earth Satellite data specified by 
                NWS Communications Handbook 5 identifier
        GPLT -  Geostationary Orbit Earth Satellite data specified by 
                platform identifier 
        CDAS - Centralized Automatic Data Acquisition System

6/  Array location is the location of the characteristics in the array returned 
    from the Preprocessor Parametric Data Base routine RPPCHR.

                IX.4.3C-GENL
</pre>
<p>
<pre>
    IX.4.3C-PCPN  PREPROCESSOR PARAMETRIC DATA BASE PARAMETER ARRAY PCPN: 
                  STATION PRECIPITATION PARAMETERS

Purpose
Parameter array PCPN contains station precipitation parameters used by the 
Mean Areal Precipitation (MAP) Preprocessor Function.

Array Contents

    Starting                     Input/
    Position  Dimension  Type    Generated   Description

    1         1          I*4     G           Parameter array 
                                             version number

    2         1          A8      I           Station identifier

    4         1          I*4     I           Station number

    5         1          A20     I           Description

    10        2          R*4     I           Location; latitude and 
                                             longitude; units of 
                                             decimal degrees

    12        2          I*4     G           NWSRFS/HRAP coordinates 
                                             stored as (X,Y)

    14        1          I*4     I           Processing code:
                                               0 = process normally
                                               1 = set station 
                                                   precipitation to zero 
                                                   if missing
                                               2 = synthetic station

    15        1          I*4     I           Data time interval; 
                                             units of HR 1/

    16        1          I*4     I or G      MDR box assigned 
                                             to this station:
                                               0 = MDR not to be used

    17        2          R*4     I           Precipitation correction 
                                             factors 2/

    19        1          I*4     I           Type of 24 hour 
                                             precipitation weights:
                                               0 = l/D**2
                                               >0 = number of stations 
                                               with significance weight; 
                                               maximum of 10

 
    20        1          I*4     G           Indicator whether 
                                             NETWORK has been run on 
                                             this station and whether 
                                             it can be assigned to an 
                                             MAP area:
                                               0 = no - can not use in MAP area
                                               1 = run previously - can not use 
                                                   in new MAP area 
                                               2 = yes - can use 24 hour data in 
                                                   MAP area
                                               3 = yes - can use both 24 hour
                                                   and less than 24 hour data 
                                                   in MAP area

    21        1          I*4     I or G      Indicator whether station 
                                             is to be used during 
                                             station weighting:
                                               0 = no - use only for estimation
                                               1 = yes

    22        1          A2      I           Postal Service 2-character 
                                             state identifier

    23        1          I*4     G           Array location of 
                                             characteristics for 
                                             this station: 6/
                                               0 = characteristics not used

    24        (4,5)      I*4     G           Array location of 
                                             pointers for 24 hour 
                                             PCPN data for 5 closest 
                                             stations in each quadrant 3/ 7/

    44        (4,5)      R*4     G           Station weights of 5 
                                             closest 24 hour PCPN 
                                             station in each quadrant 3/

    24        (2,10)     A4      I           Identifiers of stations 
                                             with significance 
                                             weights 4/

    44        10         I*4     G           Array locations of 
                                             pointers for PCPN data 
                                             for station 4/ 7/

    54        10         R*4     I           Weights for stations 4/

    64        (3,4)      I*4     G           Array location of 
                                             pointers for less than 
                                             24 hour PCPN data for 3 
                                             closest stations in each 
                                             quadrant 5/ 8/

    76        (3,4)      R*4     G           Weights for less 
                                             than 24 hour stations 5/

 
Notes:

1/  Can be only 1, 3, 6 or 24 hours.

2/  There can be 0, 1 or 2 correction factors defined.  If there is one, it is 
    used when processing data for any month of the year.  If there are two, the 
    first is used when processing data that is in the winter season and the 
    second is used when processing data that is in the summer season.  Undefined 
    correction factors are stored as -999.

3/  Defined only if l/D**2 weights are being used.

4/  Defined only if significance weights are being used.

5/  Defined only if station has a less than 24 hour reporting time interval.

6/  Location of the characteristics in the array returned from the Preprocessor 
    Parametric Data Base routine RPPCHR.

7/  Location of the pointers in the pointer array returned from the Preprocessor 
    Data Base routine RPDDLY for the data type PP24.

8/  Location of the pointers in the pointer array returned from the Preprocessor 
    Data Base routine RPDDLY for the data type PPVR.

                IX.4.3C-PCPN
</pre>
<p>
<pre>
    IX.4.3C-TEMP  PREPROCESSOR PARAMETRIC DATA BASE PARAMETER ARRAY TEMP: 
                  STATION TEMPERATURE PARAMETERS

Purpose
Parameter array TEMP contains station parameters used by the Mean Areal 
Temperature (MAT) Preprocessor Function.

Array Contents

    Starting                     Input/
    Position  Dimension Type     Generated   Description

    1         1          I*4     G           Parameter array 
                                             version number

    2         1          A8      I           Station identifier

    4         1          I*4     I           Station number

    5         1          A20     I           Description

    10        1          I*4     I           Indicator as to which 
                                             types of temperature 
                                             data are observed by station:
                                               1 = maximum/minimum only
                                               2 = instantaneous only
                                               3 = maximum/minimum and 
                                                   instantaneous
                                               4 = none; synthetic station

    11        1          I*4     I           Mountainous indicator:
                                               0 = non-mountainous
                                               1 = mountainous

    12        2          R*4     I           Maximum and minimum 
                                             correction factors; 
                                             units of DEGF 1/

    14        1          I*4     I           Indicator whether 
                                             station has Forecast 
                                             maximum/minimum 
                                             temperature data:
                                               0 = no
                                               1 = yes

    15        1          R*4     G           Elevation weighting 
                                             factor (Fe); 
                                             units of KM/1000M

    16        1          I*4     G           Indicator whether 
                                             NETWORK has been run 
                                             on this station and 
                                             whether it can be 
                                             assigned to a MAT area:
                                               0 = no; can not use in an MAT area
                                               1 = run previously; can not use in 
                                                   a new MAT area
                                               2 = yes; can use in an MAT area

    17        1          I*4     G           Array location of mean 
                                             monthly maximum/minimum 
                                             temperatures 2/

    18        (3,4)      I*4     G           Array location of 
                                             pointers for 3 closest 
                                             stations with maximum/minimum 
                                             temperature data in each 
                                             quadrant 3/

    30        (3,4)      R*4     G           Weights for stations with 
                                             maximum/minimum temperature data

    42        (3,4)      I*4     G           Array location of 
                                             pointers for closest 
                                             stations with instantaneous 
                                             temperature data in 
                                             each quadrant 4/

    54        (3,4)      R*4     G           Weights for stations 
                                             with instantaneous 
                                             temperature data

    66        (2,4)      I*4     G           Array locations of 
                                             pointers for 2 closest 
                                             stations with forecast 
                                             temperature data in 
                                             each quadrant 5/

    74        (2,4)      R*4     G           Weights for stations with 
                                             forecast temperature data

    82        1          I*4     I           Time interval of 
                                             instantaneous temperature 
                                             data:
                                               0 = instantaneous data not used

    83        1          A2      I           Postal Service 
                                             2 character state 
                                             identifier

    84        1          R*4     G           Unused

Notes:

1/  There are either 0 or 2 correction factors.  Undefined correction factors 
    are stored as -999.

2/  Array location is the location of the pointers in the pointer array 
    returned from the Preprocessor Parametric Data Base routine RPPMT.

 
3/  Array location is the location of the pointers in the pointer array 
    returned from the Preprocessor Data Base routine RPDDLY for the  data 
    type TM24.

4/  Array location is the location of the pointers in the pointer array 
    returned from the Preprocessor Data Base RPDDLY for the data type TAVR.

5/  Array location is the location of the pointers in the pointer array 
    returned from the Preprocessor Data Base routine RPDDLY for the data 
    type TF24.

                IX.4.3C-TEMP
</pre>
<p>
<pre>
    IX.4.3C-PE  PREPROCESSOR PARAMETRIC DATA BASE PARAMETER ARRAY PE: 
                STATION POTENTIAL EVAPORATION PARAMETERS

Purpose
Parameter array PE contains station potential evaporation parameters used by the 
Mean Areal Potential Evaporation (MAPE) Preprocessor Function.

Array Contents

    Starting                     Input/
    Position  Dimension  Type    Generated   Description

    1         1          I*4     G           Parameter array 
                                             version number

    2         1          A8      I           Station identifier

    4         1          I*4     I           Station number

    5         1          A20     I           Description

    10        1          R*4     I           Latitude; units of 
                                             decimal degrees

    11        1          R*4     I           Anemometer height; 
                                             units of M

    12        1          R*4     G           P factor 1/

    13        1          I*4     I           Primary type of 
                                             radiation data 
                                             to be used:
                                               1 = sky cover
                                               2 = percent sunshine
                                               3 = radiation

    14        1          R*4     I           Correction factor

    15        1          R*4     I           B3 parameter

    16        1          A2      I           United States Postal Service 
                                             2 character state identifier

    17        1          R*4     G           Unused

    18        6          R*4     G           Fourier series 
                                             coefficients a0, a1, 
                                             a2, a3, b1 and b2

    24        12         R*4     G           PE sum for each 
                                             of the last 12 
                                             months (SUMPE) 2/

    36        12         I*4     G           Number of values 
                                             in SUMPE for each 
                                             month 2/

 
    48        1          I*4     G           Julian date of last 
                                             day included in 
                                             SUMPE(12)

Notes:

1/  Computed from anemometer height.

2/  For PE consistency analysis.

                IX.4.3C-PE
</pre>
<p>
<pre>
    IX.4.3C-RRS  PREPROCESSOR PARAMETRIC DATA BASE PARAMETER ARRAY RRS: 
                 STATION RIVER, RESERVOIR AND SNOW PARAMETERS

Purpose
Parameter array RRS contains station parameters used by the River, Reservoir and 
Snow (RRS) Preprocessor Function.

Array Contents

    Starting                     Input/
    Position  Dimension  Type    Generated   Description

    1         1          I*4     G           Parameter array 
                                             version number

    2         1          A8      I           Station identifier

    4         1          I*4     I           Station number

    5         1          A20     I           Description

    10        1          A2      I           United States Postal 
                                             Service 2 character 
                                             state identifier

    11        1          R*4     G           Unused

    12        1          I*4     G           Number of data types (NTYPE)

    13        1          I*4     G           Number of data types 
                                             for which missing is 
                                             not allowed (NMISS)

    14        1          I*4     G           Number of data types 
                                             that allow mean discharge 
                                             distribution parameters (NDIST)

    15        NTYPE      A4      I           Data type codes

    15+NTYPE  NTYPE      A4      I or G      Indicator whether missing 
                                             data is allowed:
                                               'SAME' = missing allowed
                                               other  = missing not allowed; 
                                                        code for output data type

    15+       NTYPE      I*4     I           Data time interval; 
    NTYPE*2                                  units of HR: 1/
                                               <0 = no time series created 
                                                    for this type

    15+       NTYPE      I*4     G           Number of values 
    NTYPE*3                                  per observation

    15+       NTYPE      I*4     I           Minimum number of 
    NTYPE*4                                  days of data to be 
                                             retained in Preprocessor 
                                             Data Base

    15+       NTYPE      I*4     I           Typical number of 
    NTYPE*5                                  observations held in 
                                             the Preprocessor Data Base

    15+       NTYPE      I*4     G           Record number of time 
    NTYPE*6                                  series header in Processed 
                                             Data Base

    15+       NMISS      I*4     I           Interpolation option 2/
    NTYPE*7
    15+       NMISS      R*4     I           Extrapolation recession
    NTYPE*7+NMISS                            constant 2/    

    15+       NTYPE      R*4     I or G      Minimum discharge below 
    NTYPE*7+NMISS*2                          which distribution is 
                                             applied; units of CFS:
                                               <0 = time distribution 
                                                    parameters not defined 
                                                    for this type

    15+       (24,NDIST) R*4     I           Fraction of flow 
    NTYPE*8+NMISS*2                          typically occurring 
                                             during each hour of the 
                                             hydrologic day

Notes:

1/  The data time interval is used to create the time series in the Processed 
    Data Base.  If the data time interval is negative then no time series is 
    created.

2/  Defined only if missing is not allowed in the time series stored in the 
    Processed Data Base.

                IX.4.3C-RRS
</pre>
*/
public class NWSRFS_Station {

// General parameters set for all stations during a station list.
/**
Identifier for the Station.
*/
protected String _ID;

/**
A boolean specifiying whether this particular 
station is a precipitation station.
*/
protected boolean _isPCPN;

/**
A boolean specifiying whether this particular 
station is a potential evaporation station.
*/
protected boolean _isPE;

/**
A boolean specifiying whether this particular 
station is a RRS (river, reservoir, and Snow) station.
*/
protected boolean _isRRS;

/**
A boolean specifiying whether this particular 
station is a temperature station.
*/
protected boolean _isTEMP;

/**
Hashtable of logical unit numbers for each of the parameter types this station
has. This logical unit number corresponds to the <i>n</i> in the file
PPPPARM<i>n</i> and is used to pull all of the station info for a specific
parameter type. The Hashtable key is the parameterType String: GENL, PCPN, etc.
*/
protected Hashtable<String,Integer> _logicalUnitNum;

/**
Hashtable of the record number in the PPPINDEX file for retrieving data from the 
PPPPARM<i>n</i> file. The Hashtable key is the parameterType String: GENL, 
PCPN, etc.
*/
protected Hashtable<String,Integer> _recordNum;

/**
Station number.
*/
protected int _stationNum;

// GENL parameters
/**
Station complete indicator. GENL parameter.
*/
protected int _completionInd;

/**
Station Data Group Codes.  Only read in if deepRead is true. GENL parameter.
*/
protected List<String> _dataGroupCodes;

/**
Station description. GENL parameter.
*/
protected String _description;

/**
Station elevation. GENL parameter.
*/
protected float _elevation;

/**
Station Grid Point Address.  Only read in if deepRead is true. GENL parameter.
*/
protected int _gridPointAddress;

/**
Station NWSRFS/HRAP X coordinate. GENL parameter.
*/
protected int _hrapX;

/**
Station NWSRFS/HRAP Y coordinate. GENL parameter.
*/
protected int _hrapY;

/**
Station record number for each parameter record in the PPDB. Only read in if 
deepRead is true. GENL parameter.
*/
protected List<Integer> _IREC;

/**
Station latitude. GENL parameter.
*/
protected float _latitude;

/**
Station longitude. GENL parameter.
*/
protected float _longitude;

/**
Station Number of Data Groups.  Only read in if deepRead is true. GENL parameter.
*/
protected int _NGPS;

/**
Station US Postal Service state identifier code. GENL parameter.
*/
protected String _PSCode;

// PCPN precipitation parameters; only read in when deepRead is true.
/**
Station PCPN (precip) data time interval.  Only read in if deepRead is true. 
PCPN parameter.
*/
protected int _pcpnDataTimeInt;

/**
Station PCPN (precip) MDR Box.  Only read in if deepRead is true. PCPN parameter.
*/
protected int _pcpnMDRBox;

/**
Station PCPN (precip) network indicator and whether can add to MAP area.
Only read in if deepRead is true. PCPN parameter.
*/
protected int _pcpnNetInd;

/**
Station PCPN (precip) precipitation correction factor 1.  
Only read in if deepRead is true. PCPN parameter.
*/
protected float _pcpnPrecipCorrect1;

/**
Station PCPN (precip) precipitation correction factor 2.  
Only read in if deepRead is true. PCPN parameter.
*/
protected float _pcpnPrecipCorrect2;

/**
Station PCPN (precip) processing code.  Only read in if deepRead is true. 
PCPN parameter.
*/
protected int _pcpnProcCode;

/**
Station PCPN (precip) weight indicator.  
Only read in if deepRead is true. PCPN parameter.
*/
protected int _pcpnWeightInd;

/**
Station PCPN (precip) Type of 24 hour precip weights.  
Only read in if deepRead is true. PCPN parameter.
*/
protected int _pcpnWeightType;

// Potential Evaporation parameters (PE); only read in when deepRead is true.
/**
Station Potential Evaporation anemometer height.
Only read in if deepRead is true. PE parameter.
*/
protected float _peAnemometerHeight;

/**
Station Potential Evaporation B3 parameter.
Only read in if deepRead is true. PE parameter.
*/
protected float _peB3;

/**
Station Potential Evaporation correction factor.
Only read in if deepRead is true. PE parameter.
*/
protected float _peCorrectFactor;

/**
Station Potential Evaporation Fourier series coefficients
a0, a1, a2, a3, b1, and b2.
Only read in if deepRead is true. PE parameter.
*/
protected List<Float> _peFourierCoef;

/**
Station Potential Evaporation Julian Date of the Last day included in SUMPE
*/
protected int _peLastJulDay;

/**
Station Potential Evaporation number of values in 
SUMPE for each month.
Only read in if deepRead is true. PE parameter.
*/
protected List<Integer> _peNumValuesSUMPE;

/**
Station Potential Evaporation P factor.
Only read in if deepRead is true. PE parameter.
*/
protected float _pePFactor;

/**
Station Potential Evaporation primary radiation data.
Only read in if deepRead is true. PE parameter.
*/
protected int _pePRadiation;

/**
Station Potential Evaporation sum for each of last 12 months (SUMPE).
Only read in if deepRead is true. PE parameter.
*/
protected List<Float> _peSUMPE;

// River, Reservoir, and Snow parameters (RRS); only read in when deepRead is 
// true.
/**
Station RRS data time interval.
Only read in if deepRead is true. RRS parameter.
*/
protected List<Integer> _rrsDataTimeInt;

/**
Station RRS data types codes.
Only read in if deepRead is true. RRS parameter.
*/
protected List<String> _rrsDataTypeCodes;

/**
Station RRS extrapolation recession constant.
Only read in if deepRead is true. RRS parameter.
*/
protected List<Float> _rrsExtrapRecessConst;

/**
Station RRS fraction of flow occurring during
each hour of the hydrologic day.
Only read in if deepRead is true. RRS parameter.
*/
protected List<Float> _rrsFractQ;

/**
Station RRS interpolation option.
Only read in if deepRead is true. RRS parameter.
*/
protected List<Integer> _rrsInterpOpt;

/**
Station RRS record number of TS header in PRDB.
Only read in if deepRead is true. RRS parameter.
*/
protected List<Integer> _rrsIREC;

/**
Station RRS minimum number of days to retain in PPDB.
Only read in if deepRead is true. RRS parameter.
*/
protected List<Integer> _rrsMinDaysToRetain;

/**
Station RRS minimum discharge allowed before distribution is applied
Only read in if deepRead is true. RRS parameter.
*/
protected List<Float> _rrsMinQAllowed;

/**
Station RRS missing data indicator.
Only read in if deepRead is true. RRS parameter.
*/
protected List<String> _rrsMissingInd;

/**
Station RRS number of data types which allow mean 
discharge distribution parameters (NDIST).
Only read in if deepRead is true. RRS parameter.
*/
protected int _rrsNDIST;

/**
Station RRS number of data types which do not allow missing data (NMISS).
Only read in if deepRead is true. RRS parameter.
*/
protected int _rrsNMISS;

/**
Station RRS number of data types (NTYPE).
Only read in if deepRead is true. RRS parameter.
*/
protected int _rrsNTYPE;

/**
Station RRS number of values per observation.
Only read in if deepRead is true. RRS parameter.
*/
protected List<Integer> _rrsNumObs;

/**
Station RRS number of of observations in PPDB.
Only read in if deepRead is true. RRS parameter.
*/
protected List<Integer> _rrsNumObsInPPDB;

// Temperature parameters; only read in when deepRead is true.
/**
Station Temperature data type indicator.
Only read in if deepRead is true. TEMP parameter.
*/
protected int _tempDataInd;

/**
Station Temperature elevation weighting factor.
Only read in if deepRead is true. TEMP parameter.
*/
protected float _tempElevationWeight;

/**
Station Temperature Array Location of mean monthly Max/Min
*/
protected int _tempLocMeanMonthMaxMin;

/**
Station Temperature Array Location of pointers for two closest stations
with forecast temperatures in each quadrant
*/
protected List<Integer> _tempLocPointForecast;

/**
Station Temperature Array Location of pointers for two closest stations
with instantaneous temperatures in each quadrant
*/
protected List<Integer> _tempLocPointInst;

/**
Station Temperature Array Location of pointers for three closest stations
with Max/Min temperatures in each quadrant
*/
protected List<Integer> _tempLocPointMaxMin;

/**
Station Temperature Max correction factor.
Only read in if deepRead is true. TEMP parameter.
*/
protected float _tempMaxCorrect;

/**
Station Temperature forecast max/min indicator.
Only read in if deepRead is true. TEMP parameter.
*/
protected int _tempMaxMinInd;

/**
Station Temperature Min correction factor.
Only read in if deepRead is true. TEMP parameter.
*/
protected float _tempMinCorrect;

/**
Station Temperature mountainous type indicator.
Only read in if deepRead is true. TEMP parameter.
*/
protected int _tempMountainInd;

/**
Station temperature network indicator and whether can add to MAT area.
Only read in if deepRead is true. PCPN parameter.
*/
protected int _tempNetInd;

/**
Station Temperature time interval of instantaneous temperature data
*/
protected int _tempTimeIntervalInst;

/**
Station Temperature weights for stations with forecast temperatures in each quadrant
*/
protected List<Float> _tempWeightForecast;

/**
Station Temperature weights for stations with instantaneous temperatures in each quadrant
*/
protected List<Float> _tempWeightInst;

/**
Station Temperature weights for stations with Max/Min temperatures in each quadrant
*/
protected List<Float> _tempWeightMaxMin;

/**
Constructor.
@param id station id.  Can not be null
*/
public NWSRFS_Station(String id) {
	initialize();

	if (id != null) {
		_ID = id;
	}
}

// ADD Member methods for general station parameter variables
/**
Adds a logical unit number to the _logicalUnitNum Hashtable. This value will be
the <i>n</i> in the file PPPPARM<i>n</i>.
@param parameterType is the parameter type the logical unit is associated with.
This could be GENL, PCPN, PE, TEMP, or RRS.
@param logicalUnitNum is the actual file unit number to add to the Hashtable.
*/
public void addLogicalUnitNum(String parameterType, Integer logicalUnitNum) {
	_logicalUnitNum.put(parameterType,logicalUnitNum);
}

/**
Adds a record number to the _recordNum Hashtable. This value will be
the record in the file PPPPARM<i>n</i> file to find data for the given stationID 
and parameter type.
@param parameterType is the parameter type the logical unit is associated with.
This could be GENL, PCPN, PE, TEMP, or RRS.
@param recordNum is the actual record number to add to the Hashtable.
*/
public void addRecordNum(String parameterType, Integer recordNum) {
	_recordNum.put(parameterType,recordNum);
}

// ADD Member methods for GENL station parameter variables
/**
Adds value to the station data group codes list.
Member method for GENL station parameter variables
@param dataGroupCode the station data group code.
*/
public void addDataGroupCodes(String dataGroupCode) {
	_dataGroupCodes.add(dataGroupCode);
}

/**
Adds value to the station record number for each parameter in the PPDB list.
Member method for GENL station parameter variables
@param irec the station record number for each parameter in the PPDB.
*/
public void addIREC(int irec) {
	_IREC.add(Integer.valueOf(irec));
}

// ADD Member methods for PE station parameter variables
/**
Adds value to the station PE Fourier series coefficients list.
Member method for PE station parameter variables
@param peFourCoef the station PE Fourier series coefficient.
*/
public void addPEFourierCoef(float peFourCoef) {
	_peFourierCoef.add(Float.valueOf(peFourCoef));
}

/**
Adds values to the station PE sum for each of the last 12 months list.
Member method for PE station parameter variables
@param SUMPE the station PE sum for each of the last 12 months.
*/
public void addPESUMPE(float SUMPE) {
	_peSUMPE.add(Float.valueOf(SUMPE));
}

/**
Adds values to the station PE number of values in SUMPE for each month list.
Member method for PE station parameter variables
@param numValuesSUMPE the station PE number of values in SUMPE for each month.
*/
public void addPENumSUMPE(int numValuesSUMPE) {
	_peNumValuesSUMPE.add(Integer.valueOf(numValuesSUMPE));
}

// ADD Member methods for RRS (River, Reservoir, and Snow) station parameter 
// variables
/**
Adds values to the station RRS data time interval list.
Member method for RRS station parameter variables
@param rrsDataTimeInt the station RRS data time interval.
*/
public void addRRSDataTimeInt(int rrsDataTimeInt) {
	_rrsDataTimeInt.add(Integer.valueOf(rrsDataTimeInt));
}

/**
Adds values to the station RRS data type codes list.
Member method for RRS station parameter variables
@param rrsDataTypeCode the station RRS data type code.
*/
public void addRRSDataTypeCodes(String rrsDataTypeCode) {
	_rrsDataTypeCodes.add(rrsDataTypeCode);
}

/**
Adds values to the station RRS extrapolation recess constant list.
Member method for RRS station parameter variables
@param rrsExRecConst the station RRS extrapolation recess constant.
*/
public void addRRSExtrapRecessConst(float rrsExRecConst) {
	_rrsExtrapRecessConst.add(Float.valueOf(rrsExRecConst));
}

/**
Adds values to the station RRS fraction of flow occurring during each hour of the hydrologic day list.
Member method for RRS station parameter variables
@param rrsFractQ the station RRS fraction of flow occurring during each hour of the hydrologic day.
*/
public void addRRSFractQ(float rrsFractQ) {
	_rrsFractQ.add(Float.valueOf(rrsFractQ));
}

/**
Adds values to the station RRS interpolation option list.
Member method for RRS station parameter variables
@param rrsIntOpt the station RRS interpolation option.
*/
public void addRRSInterpOpt(int rrsIntOpt) {
	_rrsInterpOpt.add(Integer.valueOf(rrsIntOpt));
}

/**
Adds values to the station RRS record number of TS header in PPDB list.
Member method for RRS station parameter variables
@param rrsIREC the station RRS record number of TS header in PPDB.
*/
public void addRRSIREC(int rrsIREC) {
	_rrsIREC.add(Integer.valueOf(rrsIREC));
}

/**
Adds values to the station RRS minimum days to retain in PPDB list.
Member method for RRS station parameter variables
@param rrsMinDaysToRetain the station RRS minimum days to retain in PPDB.
*/
public void addRRSMinDaysToRetain(int rrsMinDaysToRetain) {
	_rrsMinDaysToRetain.add(Integer.valueOf(rrsMinDaysToRetain));
}

/**
Adds values to the station RRS minimum flow allowed before a distribution is applied list.
Member method for RRS station parameter variables
@param rrsMinQAllowed the station RRS minimum flow allowed.
*/
public void addRRSMinQAllowed(float rrsMinQAllowed) {
	_rrsMinQAllowed.add(Float.valueOf(rrsMinQAllowed));
}

/**
Adds values to the station RRS missing data indicator list.
Member method for RRS station parameter variables
@param rrsMissInd the station RRS missing data indicator.
*/
public void addRRSMissingInd(String rrsMissInd) {
	_rrsMissingInd.add(rrsMissInd);
}

/**
Adds value to the station RRS number of values per observation list.
Member method for RRS station parameter variables
@param rrsNumObs the station RRS list of number of values per observation.
*/
public void addRRSNumObs(int rrsNumObs) {
	_rrsNumObs.add(Integer.valueOf(rrsNumObs));
}

/**
Adds values to the station RRS number of observations in PPDB per data type list.
Member method for RRS station parameter variables
@param rrsNumObsInPPDB the station RRS list of number of observations in PPDB.
*/
public void addRRSNumObsInPPDB(int rrsNumObsInPPDB) {
	_rrsNumObsInPPDB.add(Integer.valueOf(rrsNumObsInPPDB));
}

// ADD Member methods for TEMP (Temperature) station parameter 
// variables
/**
Adds values to the station TEMP array location of pointers for two closest
stations for forecast data in each quadrant. This list should hold four
values for each of the two stations or 8 4-byte integer values.
Member method for TEMP station parameter variables
@param tempLocPointForecast the station TEMP array location pointer value.
*/
public void addTEMPLocPointForecast(int tempLocPointForecast) {
	_tempLocPointForecast.add(Integer.valueOf(tempLocPointForecast));
}

/**
Adds values to the station TEMP array location of pointers for three closest
stations for instantaneous data in each quadrant. This list should hold four
values for each of the three stations or 12 4-byte integer values.
Member method for TEMP station parameter variables
@param tempLocPointInst the station TEMP array location pointer value.
*/
public void addTEMPLocPointInst(int tempLocPointInst) {
	_tempLocPointInst.add(Integer.valueOf(tempLocPointInst));
}

/**
Adds values to the station TEMP array location of pointers for three closest
stations for Max/Min data in each quadrant. This list should hold four
values for each of the three stations or 12 4-byte integer values.
Member method for TEMP station parameter variables
@param tempLocPointMaxMin the station TEMP array location pointer value.
*/
public void addTEMPLocPointMaxMin(int tempLocPointMaxMin) {
	_tempLocPointMaxMin.add(Integer.valueOf(tempLocPointMaxMin));
}

/**
Adds values to the station TEMP Weights for two closest
stations for forecast data in each quadrant. This list should hold four
values for each of the three stations or 8 4-byte integer values.
Member method for TEMP station parameter variables
@param tempWeightForecast the station TEMP weight value.
*/
public void addTEMPWeightForecast(float tempWeightForecast) {
	_tempWeightForecast.add(Float.valueOf(tempWeightForecast));
}

/**
Adds values to the station TEMP Weights for three closest
stations for instantaneous data in each quadrant. This list should hold four
values for each of the three stations or 12 4-byte integer values.
Member method for TEMP station parameter variables
@param tempWeightInst the station TEMP weight value.
*/
public void addTEMPWeightInst(float tempWeightInst) {
	_tempWeightInst.add(Float.valueOf(tempWeightInst));
}

/**
Adds values to the station TEMP Weights for three closest
stations for Max/Min data in each quadrant. This list should hold four
values for each of the three stations or 12 4-byte integer values.
Member method for TEMP station parameter variables
@param tempWeightMaxMin the station TEMP weight value.
*/
public void addTEMPWeightMaxMin(float tempWeightMaxMin) {
	_tempWeightMaxMin.add(Float.valueOf(tempWeightMaxMin));
}

// GET Member methods for general station variables
/**
Returns the station's identifier.
Member method for general station variables
@return the station's identifier.
*/
public String getID() {
	return _ID;
}

/**
Returns the station's number.
Member method for general station variables
@return the station's number.
*/
public int getStationNum() {
	return _stationNum;
}

/**
Returns a boolean whether the station contains precipitation parameters.
Member method for general station variables
@return a boolean whether the station contains precipitation parameters..
*/
public boolean getIsPCPN() {
	return _isPCPN;
}

/**
Returns a boolean whether the station contains potentional evaporation parameters.
Member method for general station variables
@return a boolean whether the station contains potentional evaporation parameters..
*/
public boolean getIsPE() {
	return _isPE;
}

/**
Returns a boolean whether the station contains RRS parameters.
Member method for general station variables
@return a boolean whether the station contains RRS parameters..
*/
public boolean getIsRRS() {
	return _isRRS;
}

/**
Returns a boolean whether the station contains temperature parameters.
Member method for general station variables
@return a boolean whether the station contains temperature parameters..
*/
public boolean getIsTEMP() {
	return _isTEMP;
}

/**
Returns the logical unit number for the station given a parameter type.
@param parameterType is the String containing the parameterType name: GENL,
PCPN,PE,TEMP,or RRS.
@return the logical unit number for the station and a parameterType.
*/
public int getLogicalUnitNum(String parameterType) {
	if(_logicalUnitNum.isEmpty() ||
		_logicalUnitNum.get(parameterType) == null) {
		return -1;
	}
	
	return (int)((Integer)_logicalUnitNum.get(parameterType)).intValue();
}

/**
Returns the record number for the station given a parameter type.
@param parameterType is the String containing the parameterType name: GENL,
PCPN,PE,TEMP,or RRS.
@return the record number at for the station and a parameterType.
*/
public int getRecordNum(String parameterType) {
	Integer recNumValue = null;
	
	if(_recordNum.isEmpty() ||
		(recNumValue = (Integer)_recordNum.get(parameterType)) == null) {
		return -1;
	}
	
	return (int)recNumValue.intValue();
}

// GET Member methods for GENL station parameter variables
/**
Returns the station completion indicator.
Member method for GENL station parameter variables
@return the station completion indicator.
*/
public int getCompleteInd() {
	return _completionInd;
}

/**
Returns the station data group codes.
Member method for GENL station parameter variables
Only read if it is a deep read.
@return the station data group codes list.
*/
public List<String> getDataGroupCodes() {
	return _dataGroupCodes;
}

/**
Returns the station data group codes at an index.
Member method for GENL station parameter variables
Only read if it is a deep read.
@param dgcIndex index to get the specific element in the data group codes list
@return the station data group codes at an index.
*/
public String getDataGroupCodes(int dgcIndex) {
	return _dataGroupCodes.get(dgcIndex);
}

/**
Returns the station description.
Member method for GENL station parameter variables
@return the station description.
*/
public String getDescription() {
	return _description;
}

/**
Returns the station elevation.
Member method for GENL station parameter variables
@return the station elevation.
*/
public float getElevation() {
	return _elevation;
}

/**
Returns the station grid point address.
Member method for GENL station parameter variables
Only read if it is a deep read.
@return the station grid point address.
*/
public int getGridPointAddress() {
	return _gridPointAddress;
}

/**
Returns the NWSRFS/HRAP X coordinate.
Member method for GENL station parameter variables
@return the NWSRFS/HRAP X coordinate.
*/
public int getHrapX() {
	return _hrapX;
}

/**
Returns the NWSRFS/HRAP Y coordinate.
Member method for GENL station parameter variables
@return the NWSRFS/HRAP Y coordinate.
*/
public int getHrapY() {
	return _hrapY;
}

/**
Returns the station record number for each parameter in the PPDB.
Member method for GENL station parameter variables
Only read if it is a deep read.
@return the station record number list.
*/
public List<Integer> getIREC() {
	return _IREC;
}

/**
Returns the station record number at an index for each parameter in the PPDB.
Member method for GENL station parameter variables
Only read if it is a deep read.
@param irecIndex index to get the specific element in the record number list
@return the station record number.
*/
public int getIREC(int irecIndex) {
	return _IREC.get(irecIndex).intValue();
}

/**
Returns the station latitude.
Member method for GENL station parameter variables
@return the station latitude.
*/
public float getLatitude() {
	return _latitude;
}

/**
Returns the station longitude.
Member method for GENL station parameter variables
@return the station longitude.
*/
public float getLongitude() {
	return _longitude;
}

/**
Returns the station number of data groups.
Member method for GENL station parameter variables
Only read if it is a deep read.
@return the station number of data groups.
*/
public int getNGPS() {
	return _NGPS;
}

/**
Returns the number of station data group codes.
Member method for GENL station parameter variables
@return the number of station data group codes.
*/
public int getNumDataGroupCodes() {
	return _dataGroupCodes.size();
}

/**
Returns the station US Postal service state identifier code.
Member method for GENL station parameter variables
@return the station US PS state code.
*/
public String getPSCode() {
	return _PSCode;
}

// GET Member methods for PCPN (precip) station parameter variables
/**
Returns the station PCPN data time interval.
Member method for PCPN station parameter variables
Only read if it is a deep read.
@return the station PCPN data time interval.
*/
public int getPCPNDataTimeInt() {
	return _pcpnDataTimeInt;
}

/**
Returns the station PCPN MDR Box.
Member method for PCPN station parameter variables
Only read if it is a deep read.
@return the station PCPN MDR Box.
*/
public int getPCPNMDRBox() {
	return _pcpnMDRBox;
}

/**
Returns the station PCPN Network Indicator to determine 
whether it can be added to a MAP area.
Member method for PCPN station parameter variables
Only read if it is a deep read.
@return the station PCPN Net Indicator.
*/
public int getPCPNNetInd() {
	return _pcpnNetInd;
}

/**
Returns the station PCPN precip correction factor 1.
Member method for PCPN station parameter variables
Only read if it is a deep read.
@return the station PCPN correction factor 1.
*/
public float getPCPNPrecipCorrect1() {
	return _pcpnPrecipCorrect1;
}

/**
Returns the station PCPN precip correction factor 2.
Member method for PCPN station parameter variables
Only read if it is a deep read.
@return the station PCPN correction factor 2.
*/
public float getPCPNPrecipCorrect2() {
	return _pcpnPrecipCorrect2;
}

/**
Returns the station PCPN processing code.
Member method for PCPN station parameter variables
Only read if it is a deep read.
@return the station PCPN processing code.
*/
public int getPCPNProcCode() {
	return _pcpnProcCode;
}

/**
Returns the station PCPN Weight Indicator.
Member method for PCPN station parameter variables
Only read if it is a deep read.
@return the station PCPN Weight Indicator.
*/
public int getPCPNWeightInd() {
	return _pcpnWeightInd;
}

/**
Returns the station PCPN Weight Type.
Member method for PCPN station parameter variables
Only read if it is a deep read.
@return the station PCPN Weight type.
*/
public int getPCPNWeightType() {
	return _pcpnWeightType;
}

// GET Member methods for PE (Potential Evaporation) station parameter variables
/**
Returns the station PE anemometer height.
Member method for PE station parameter variables
Only read if it is a deep read.
@return the station PE anemometer height.
*/
public float getPEAnemometerHeight() {
	return _peAnemometerHeight;
}

/**
Returns the station PE P factor.
Member method for PE station parameter variables
Only read if it is a deep read.
@return the station PE P factor.
*/
public float getPEPFactor() {
	return _pePFactor;
}

/**
Returns the station PE primary radiation data.
Member method for PE station parameter variables
Only read if it is a deep read.
@return the station PE primary radiation indicator.
*/
public int getPERadiation() {
	return _pePRadiation;
}

/**
Returns the station PE correction factor.
Member method for PE station parameter variables
Only read if it is a deep read.
@return the station PE correction factor.
*/
public float getPECorrectFactor() {
	return _peCorrectFactor;
}

/**
Returns the station PE B3 parameter.
Member method for PE station parameter variables
Only read if it is a deep read.
@return the station PE B3 parameter.
*/
public float getPEB3() {
	return _peB3;
}

/**
Returns the station PE Fourier series coefficients.
Member method for PE station parameter variables
Only read if it is a deep read.
@return the station PE list of Fourier coefficient.
*/
public List<Float> getPEFourierCoef() {
	return _peFourierCoef;
}

/**
Returns the station PE Fourier series coefficients at an index.
Member method for PE station parameter variables
Only read if it is a deep read.
@param peFourIndex index to get the specific element in the PE Fourier series coefficients list.
@return the station PE Fourier coefficient at an index.
*/
public float getPEFourierCoef(int peFourIndex) {
	return _peFourierCoef.get(peFourIndex).floatValue();
}

/**
Returns the station PE Julian date of last day included in the SUMPE.
Member method for PE station parameter variables
@return the station PE last julian day of SUMPE.
*/
public int getPELastJulDay() {
	return _peLastJulDay;
}

/**
Returns the station PE sum for each of the last 12 months.
Member method for PE station parameter variables
Only read if it is a deep read.
@return the station PE SUMPE value.
*/
public List<Float> getPESUMPE() {
	return _peSUMPE;
}

/**
Returns the station PE sum for a month specified.
Member method for PE station parameter variables
Only read if it is a deep read.
@param peSUMPEIndex index to get the specific element in the PE SUMPE list. This will be a month number starting at 0.
@return the station PE SUMPE at an index.
*/
public float getPESUMPE(int peSUMPEIndex) {
	return _peSUMPE.get(peSUMPEIndex).floatValue();
}

/**
Returns the station PE number of values in SUMPE for each month.
Member method for PE station parameter variables
Only read if it is a deep read.
@return the station PE number of SUMPE values.
*/
public List<Integer> getPENumSUMPE() {
	return _peNumValuesSUMPE;
}

/**
Returns the station PE number of values in SUMPE for a month specified.
Member method for PE station parameter variables
Only read if it is a deep read.
@param peNumValuesSUMPEIndex index to get the specific element in the PE SUMPE list. This will be a month number starting at 0.
@return the station PE Number of SUMPE values at an index.
*/
public int getPENumSUMPE(int peNumValuesSUMPEIndex) {
	return _peNumValuesSUMPE.get(peNumValuesSUMPEIndex).intValue();
}

// GET Member methods for RRS (River, Reservoir, and Snow) station parameter 
// variables
/**
Returns the station RRS data time interval list.
Member method for RRS station parameter variables
Only read if it is a deep read.
@return the station RRS data time list.
*/
public List<Integer> getRRSDataTimeInt() {
	return _rrsDataTimeInt;
}

/**
Returns the station RRS data time interval for a specified time series.
Member method for RRS station parameter variables
Only read if it is a deep read.
@param rrsDTIIndex index to get the specific element in the data time interval list. This will be a TS index starting at 0.
@return the station RRS data time interval at an index.
*/
public int getRRSDataTimeInt(int rrsDTIIndex) {
	return _rrsDataTimeInt.get(rrsDTIIndex).intValue();
}

/**
Returns the station RRS data type codes list.
Member method for RRS station parameter variables.
Only read if it is a deep read.
@return the station RRS data type codes list.
*/
public List<String> getRRSDataTypeCodes() {
	return _rrsDataTypeCodes;
}

/**
Returns the station RRS data type codes for a specified index.
Member method for RRS station parameter variables
Only read if it is a deep read.
@param rrsDTCIndex index to get the specific element in the data type codes list. This will be an index starting at 0.
@return the station RRS data type codes at an index.
*/
public String getRRSDataTypeCodes(int rrsDTCIndex) {
	return _rrsDataTypeCodes.get(rrsDTCIndex);
}

/**
Returns the station RRS extrapolation recess constant list.
Member method for RRS station parameter variables
Only read if it is a deep read.
@return the station RRS extrapolation recess constant list.
*/
public List<Float> getRRSExtrapRecessConst() {
	return _rrsExtrapRecessConst;
}

/**
Returns the station RRS extrapolation recess constant for a specified index.
Member method for RRS station parameter variables
Only read if it is a deep read.
@param rrsERCIndex index to get the specific element in the extrapolation
recess constant list. This will be an index starting at 0.
@return the station RRS extrapolation recess constant at an index.
*/
public float getRRSExtrapRecessConst(int rrsERCIndex) {
	return _rrsExtrapRecessConst.get(rrsERCIndex).floatValue();
}

/**
Returns the station RRS fraction of flow occurring during each hour of the hydrologic day list.
Member method for RRS station parameter variables
Only read if it is a deep read.
@return the station RRS fraction of flow list.
*/
public List<Float> getRRSFractQ() {
	return _rrsFractQ;
}

/**
Returns the station RRS fraction of flow occurring during each hour of the
hydrologic day for a specified index.
Member method for RRS station parameter variables
Only read if it is a deep read.
@param rrsFRQIndex index to get the specific element in the fraction of flow list. This will be an index starting at 0.
@return the station RRS fraction of flow at an index.
*/
public float getRRSFractQ(int rrsFRQIndex) {
	return _rrsFractQ.get(rrsFRQIndex).floatValue();
}

/**
Returns the station RRS interpolation option list.
Member method for RRS station parameter variables
Only read if it is a deep read.
@return the station RRS list of interpolation options.
*/
public List<Integer> getRRSInterpOpt() {
	return _rrsInterpOpt;
}

/**
Returns the station RRS interpolation options 
for a specified index.
Member method for RRS station parameter variables
Only read if it is a deep read.
@param rrsIntOPIndex index to get the specific element in the interpolation options list. This will be an index starting at 0.
@return the station RRS interpolation options at an index.
*/
public int getRRSInterpOpt(int rrsIntOPIndex) {
	return _rrsInterpOpt.get(rrsIntOPIndex).intValue();
}

/**
Returns the station RRS record number of TS header in PPDB list.
Member method for RRS station parameter variables
Only read if it is a deep read.
@return the station RRS list of record number of TS header in PPDB.
*/
public List<Integer> getRRSIREC() {
	return _rrsIREC;
}

/**
Returns the station RRS record number of TS header in PPDB 
for a specified index.
Member method for RRS station parameter variables
Only read if it is a deep read.
@param rrsIRECIndex index to get the specific element in the record number
of TS header in PPDB list. This will be an index starting at 0.
@return the station RRS record number of TS header in PPDB at an index.
*/
public int getRRSIREC(int rrsIRECIndex) {
	return _rrsIREC.get(rrsIRECIndex).intValue();
}

/**
Returns the station RRS minimum days to retain in PPDB list.
Member method for RRS station parameter variables
Only read if it is a deep read.
@return the station RRS list of minimum days to retain in PPDB.
*/
public List<Integer> getRRSMinDaysToRetain() {
	return _rrsMinDaysToRetain;
}

/**
Returns the station RRS minimum days to retain in PPDB 
for a specified index.
Member method for RRS station parameter variables
Only read if it is a deep read.
@param rrsMinDTRIndex index to get the specific element in the minimum days to retain list. This will be an index starting at 0.
@return the station RRS minimum days to retain in PPDB at an index.
*/
public int getRRSMinDaysToRetain(int rrsMinDTRIndex) {
	return _rrsMinDaysToRetain.get(rrsMinDTRIndex).intValue();
}

/**
Returns the station RRS minimum flow allowed before a distribution is applied list.
Member method for RRS station parameter variables
Only read if it is a deep read.
@return the station RRS minimum flow allowed list.
*/
public List<Float> getRRSMinQAllowed() {
	return _rrsMinQAllowed;
}

/**
Returns the station RRS minimum flow allowed before a distribution is applied 
for a specified index.
Member method for RRS station parameter variables
Only read if it is a deep read.
@param rrsMinQIndex index to get the specific element in the minimum flow 
allowed list. This will be an index starting at 0.
@return the station RRS minimum flow allowed at an index.
*/
public float getRRSMinQAllowed(int rrsMinQIndex) {
	return _rrsMinQAllowed.get(rrsMinQIndex).floatValue();
}

/**
Returns the station RRS missing data indicator list.
Member method for RRS station parameter variables
Only read if it is a deep read.
@return the station RRS missing data indicator list.
*/
public List<String> getRRSMissingInd() {
	return _rrsMissingInd;
}

/**
Returns the station RRS missing data indicator 
for a specified index.
Member method for RRS station parameter variables
Only read if it is a deep read.
@param rrsMissIndex index to get the specific element in the missing data
indicator list. This will be an index starting at 0.
@return the station RRS missing data indicator at an index.
*/
public String getRRSMissingInd(int rrsMissIndex) {
	return _rrsMissingInd.get(rrsMissIndex);
}

/**
Returns the station RRS data types which allow mean discharge distribution
parameters (NDIST).
Member method for RRS station parameter variables
Only read if it is a deep read.
@return the station RRS data types indicator.
*/
public int getRRSNDIST() {
	return _rrsNDIST;
}

/**
Returns the station RRS data types which do not allow missing data (NMISS).
Member method for RRS station parameter variables
Only read if it is a deep read.
@return the station RRS data types indicator.
*/
public int getRRSNMISS() {
	return _rrsNMISS;
}

/**
Returns the station RRS data types.
Member method for RRS station parameter variables
Only read if it is a deep read.
@return the station RRS data types indicator.
*/
public int getRRSNTYPE() {
	return _rrsNTYPE;
}

/**
Returns the station RRS number of values per observation list.
Member method for RRS station parameter variables
Only read if it is a deep read.
@return the station RRS list of number of values per observation.
*/
public List<Integer> getRRSNumObs() {
	return _rrsNumObs;
}

/**
Returns the station RRS number of values per observation 
for a specified index.
Member method for RRS station parameter variables
Only read if it is a deep read.
@param rrsNumObsIndex index to get the specific element in the number of
values per observation list. This will be an index starting at 0.
@return the station RRS number of values per observation at an index.
*/
public int getRRSNumObs(int rrsNumObsIndex) {
	return _rrsNumObs.get(rrsNumObsIndex).intValue();
}

/**
Returns the station RRS number of observations in PPDB per data type list.
Member method for RRS station parameter variables
Only read if it is a deep read.
@return the station RRS list of number of observations in PPDB.
*/
public List<Integer> getRRSNumObsInPPDB() {
	return _rrsNumObsInPPDB;
}

/**
Returns the station RRS number of observations in PPDB per data type 
for a specified index.
Member method for RRS station parameter variables
Only read if it is a deep read.
@param rrsNumObsPPDBIndex index to get the specific element in the number of
observations in PPDB list. This will be an index starting at 0.
@return the station RRS number of observations in PPDB at an index.
*/
public int getRRSNumObsInPPDB(int rrsNumObsPPDBIndex) {
	return _rrsNumObsInPPDB.get(rrsNumObsPPDBIndex).intValue();
}

// GET Member methods for TEMP (Temperature) station parameter variables
/**
Returns the station TEMP data type indicator.
Member method for TEMP station parameter variables
Only read if it is a deep read.
@return the station TEMP data type indicator.
*/
public int getTEMPDataInd() {
	return _tempDataInd;
}

/**
Returns the station TEMP elevation weight.
Member method for TEMP station parameter variables
Only read if it is a deep read.
@return the station TEMP elevation weight.
*/
public float getTEMPElevationWeight() {
	return _tempElevationWeight;
}

/**
Returns values to the station TEMP array location of pointers list for
forecast data
Member method for TEMP station parameter variables
@return the station TEMP list of array location pointer values.
*/
public List<Integer> getTEMPLocPointForecast() {
	return _tempLocPointForecast;
}

/**
Returns values to the station TEMP array location of pointers for
forecast data at an index.
Member method for TEMP station parameter variables
@param tempLocPointForecastIndex index to a specific element.
@return the station TEMP list of array location pointer values.
*/
public int getTEMPLocPointForecast(int tempLocPointForecastIndex) {
	return _tempLocPointForecast.get(tempLocPointForecastIndex).intValue();
}

/**
Returns values to the station TEMP array location of pointers list for instantaneous data.
Member method for TEMP station parameter variables
@return the station TEMP list of array location pointer values.
*/
public List<Integer> getTEMPLocPointInst() {
	return _tempLocPointInst;
}

/**
Returns values to the station TEMP array location of pointers for
instantaneous data at an index.
Member method for TEMP station parameter variables
@param tempLocPointInstIndex index to a specific element.
@return the station TEMP list of array location pointer values.
*/
public int getTEMPLocPointInst(int tempLocPointInstIndex) {
	return _tempLocPointInst.get(tempLocPointInstIndex).intValue();
}

/**
Returns values to the station TEMP array location of pointers list for Max/Min data
Member method for TEMP station parameter variables
@return the station TEMP list of array location pointer values.
*/
public List<Integer> getTEMPLocPointMaxMin() {
	return _tempLocPointMaxMin;
}

/**
Returns values to the station TEMP array location of pointers for
Max/Min data at an index.
Member method for TEMP station parameter variables
@param tempLocPointMaxMinIndex index to a specific element.
@return the station TEMP list of array location pointer values.
*/
public int getTEMPLocPointMaxMin(int tempLocPointMaxMinIndex) {
	return _tempLocPointMaxMin.get(tempLocPointMaxMinIndex).intValue();
}

/**
Returns the station TEMP array location of mean monthly Max/Min.
Member method for TEMP station parameter variables
@return TEMP array location of mean monthly Max/Min
*/
public int getTEMPLocMeanMonthMaxMin() {
	return _tempLocMeanMonthMaxMin;
}

/**
Returns the station TEMP Max correction factor.
Member method for TEMP station parameter variables
Only read if it is a deep read.
@return the station TEMP max correction factor.
*/
public float getTEMPMaxCorrect() {
	return _tempMaxCorrect;
}

/**
Returns the station TEMP forecast max/min indicator.
Member method for TEMP station parameter variables
Only read if it is a deep read.
@return the station TEMP forecast max/min indicator.
*/
public int getTEMPMaxMinInd() {
	return _tempMaxMinInd;
}

/**
Returns the station TEMP Min correction factor.
Member method for TEMP station parameter variables
Only read if it is a deep read.
@return the station TEMP min correction factor.
*/
public float getTEMPMinCorrect() {
	return _tempMinCorrect;
}

/**
Returns the station TEMP mountainous type indicator.
Member method for TEMP station parameter variables
Only read if it is a deep read.
@return the station TEMP mountainous type indicator.
*/
public int getTEMPMountainInd() {
	return _tempMountainInd;
}

/**
Returns the station TEMP network indicator to determine whether can add
to a MAT area.
Member method for TEMP station parameter variables
Only read if it is a deep read.
@return the station TEMP network indicator.
*/
public int getTEMPNetInd() {
	return _tempNetInd;
}

/**
Returns the station TEMP Time interval of instantaneous temperature data.
Member method for TEMP station parameter variables
@return Time Interval of inst. temperature data
*/
public int getTEMPTimeIntervalInst() {
	return _tempTimeIntervalInst;
}

/**
Returns values to the station TEMP weight list for forecast data.
Member method for TEMP station parameter variables
@return the station TEMP list of array Weight values.
*/
public List<Float> getTEMPWeightForecast() {
	return _tempWeightForecast;
}

/**
Returns values to the station TEMP weight for
forecast data at an index.
Member method for TEMP station parameter variables
@param tempWeightForecast index to a specific element.
@return the station TEMP list of array location pointer values.
*/
public float getTEMPWeightForecast(int tempWeightForecastIndex) {
	return _tempWeightForecast.get(tempWeightForecastIndex).floatValue();
}

/**
Returns values to the station TEMP weight list for instantaneous data.
Member method for TEMP station parameter variables
@return the station TEMP list of array Weight values.
*/
public List<Float> getTEMPWeightInst() {
	return _tempWeightInst;
}

/**
Returns values to the station TEMP weight for
instantaneous data at an index.
Member method for TEMP station parameter variables
@param tempWeightInst index to a specific element.
@return the station TEMP list of array location pointer values.
*/
public float getTEMPWeightInst(int tempWeightInstIndex) {
	return _tempWeightInst.get(tempWeightInstIndex).floatValue();
}

/**
Returns values to the station TEMP weight list for Max/Min data.
Member method for TEMP station parameter variables
@return the station TEMP list of array Weight values.
*/
public List<Float> getTEMPWeightMaxMin() {
	return _tempWeightMaxMin;
}

/**
Returns values to the station TEMP weight for
Max/Min data at an index.
Member method for TEMP station parameter variables
@param tempWeightMaxMin index to a specific element.
@return the station TEMP list of array location pointer values.
*/
public float getTEMPWeightMaxMin(int tempWeightMaxMinIndex) {
	return _tempWeightMaxMin.get(tempWeightMaxMinIndex).floatValue();
}

/**
Initialize data members.
*/
private void initialize() {
	_ID			= null;
	_isPCPN			= false;
	_isPE			= false;
	_isRRS			= false;
	_isTEMP			= false;
	_logicalUnitNum		= new Hashtable<>();
	_recordNum		= new Hashtable<>();
	_stationNum		= -1;
	_completionInd		= 0;
	_dataGroupCodes		= new ArrayList<>();
	_description		= null;
	_elevation		= 0;
	_gridPointAddress	= 0;
	_hrapX			= 0;
	_hrapY			= 0;
	_IREC			= new ArrayList<>();
	_latitude		= 0;
	_longitude		= 0;
	_NGPS			= 0;
	_PSCode			= null;
	_pcpnDataTimeInt	= 0;
	_pcpnMDRBox		= 0;
	_pcpnNetInd		= 0;
	_pcpnPrecipCorrect1	= 0;
	_pcpnPrecipCorrect2	= 0;
	_pcpnProcCode		= 0;
	_pcpnWeightInd		= 0;
	_pcpnWeightType		= 0;
	_peAnemometerHeight	= 0;
	_peB3			= 0;
	_peCorrectFactor	= 0;
	_peFourierCoef		= new ArrayList<>();
	_peLastJulDay		= 0;
	_peNumValuesSUMPE	= new ArrayList<>();
	_pePFactor		= 0;
	_pePRadiation		= 0;
	_peSUMPE		= new ArrayList<>();
	_rrsDataTimeInt		= new ArrayList<>();
	_rrsDataTypeCodes	= new ArrayList<>();
	_rrsExtrapRecessConst	= new ArrayList<>();
	_rrsFractQ		= new ArrayList<>();
	_rrsInterpOpt		= new ArrayList<>();
	_rrsIREC		= new ArrayList<>();
	_rrsMinDaysToRetain	= new ArrayList<>();
	_rrsMinQAllowed		= new ArrayList<>();
	_rrsMissingInd		= new ArrayList<>();
	_rrsNDIST		= 0;
	_rrsNMISS		= 0;
	_rrsNTYPE		= 0;
	_rrsNumObs		= new ArrayList<>();
	_rrsNumObsInPPDB	= new ArrayList<>();
	_tempDataInd		= 0;
	_tempElevationWeight	= 0;
	_tempLocMeanMonthMaxMin	= 0;
	_tempLocPointForecast	= new ArrayList<>();
	_tempLocPointInst	= new ArrayList<>();
	_tempLocPointMaxMin	= new ArrayList<>();
	_tempMaxCorrect		= 0;
	_tempMaxMinInd		= 0;
	_tempMinCorrect		= 0;
	_tempMountainInd	= 0;
	_tempNetInd		= 0;
	_tempTimeIntervalInst	= 0;
	_tempWeightForecast	= new ArrayList<>();
	_tempWeightInst		= new ArrayList<>();
	_tempWeightMaxMin	= new ArrayList<>();
}

// SET Member methods for general station variables
/**
Sets the station's identifier.
Member method for general station variables
*/
public void setID(String id) {
	_ID = id;
}

/**
Sets the station's number.
Member method for general station variables
*/
public void setStationNum(int stationNum) {
	_stationNum = stationNum;
}

/**
Sets a boolean whether the station contains precipitation parameters.
Member method for general station variables
*/
public void setIsPCPN(boolean isPCPN) {
	_isPCPN = isPCPN;
}

/**
Sets a boolean whether the station contains potentional evaporation parameters.
Member method for general station variables
*/
public void setIsPE(boolean isPE) {
	_isPE = isPE;
}

/**
Sets a boolean whether the station contains RRS parameters.
Member method for general station variables
*/
public void setIsRRS(boolean isRRS) {
	_isRRS = isRRS;
}

/**
Sets a boolean whether the station contains temperature parameters.
Member method for general station variables
*/
public void setIsTEMP(boolean isTemp) {
	_isTEMP = isTemp;
}

// SET Member methods for GENL station parameter variables
/**
Sets the station completion indicator.
Member method for GENL station parameter variables
*/
public void setCompleteInd(int completionInd) {
	_completionInd = completionInd;
}

/**
Sets the station description.
Member method for GENL station parameter variables
*/
public void setDescription(String description) {
	_description = description;
}

/**
Sets the station elevation.
Member method for GENL station parameter variables
*/
public void setElevation(float elevation) {
	_elevation = elevation;
}

/**
Sets the station grid point address.
Member method for GENL station parameter variables
*/
public void setGridPointAddress(int gridPointAddress) {
	_gridPointAddress = gridPointAddress;
}

/**
Sets the NWSRFS/HRAP X coordinate.
Member method for GENL station parameter variables
*/
public void setHrapX(int hrapX) {
	_hrapX = hrapX;
}

/**
Sets the NWSRFS/HRAP Y coordinate.
Member method for GENL station parameter variables
*/
public void setHrapY(int hrapY) {
	_hrapY = hrapY;
}

/**
Sets the station latitude.
Member method for GENL station parameter variables
*/
public void setLatitude(float latitude) {
	_latitude = latitude;
}

/**
Sets the station longitude.
Member method for GENL station parameter variables
*/
public void setLongitude(float longitude) {
	_longitude = longitude;
}

/**
Sets the station number of data groups.
Member method for GENL station parameter variables
*/
public void setNGPS(int NGPS) {
	_NGPS = NGPS;
}

/**
Sets the station US Postal service state identifier code.
Member method for GENL station parameter variables
*/
public void setPSCode(String PSCode) {
	_PSCode = PSCode;
}

// GET Member methods for PCPN (precip) station parameter variables
/**
Sets the station PCPN data time interval.
Member method for PCPN station parameter variables
*/
public void setPCPNDataTimeInt(int pcpnDataTimeInt) {
	_pcpnDataTimeInt = pcpnDataTimeInt;
}

/**
Sets the station PCPN MDR Box.
Member method for PCPN station parameter variables
*/
public void setPCPNMDRBox(int pcpnMDRBox) {
	_pcpnMDRBox = pcpnMDRBox;
}

/**
Sets the station PCPN Network Indicator to determine 
whether it can be added to a MAP area.
Member method for PCPN station parameter variables
*/
public void setPCPNNetInd(int pcpnNetInd) {
	_pcpnNetInd = pcpnNetInd;
}

/**
Sets the station PCPN precip correction factor 1.
Member method for PCPN station parameter variables
*/
public void setPCPNPrecipCorrect1(float pcpnPrecipCorrect1) {
	_pcpnPrecipCorrect1 = pcpnPrecipCorrect1;
}

/**
Sets the station PCPN precip correction factor 2.
Member method for PCPN station parameter variables
*/
public void setPCPNPrecipCorrect2(float pcpnPrecipCorrect2) {
	_pcpnPrecipCorrect2 = pcpnPrecipCorrect2;
}

/**
Sets the station PCPN processing code.
Member method for PCPN station parameter variables
*/
public void setPCPNProcCode(int pcpnProcCode) {
	_pcpnProcCode = pcpnProcCode;
}

/**
Sets the station PCPN Weight Indicator.
Member method for PCPN station parameter variables
*/
public void setPCPNWeightInd(int pcpnWeightInd) {
	_pcpnWeightInd = pcpnWeightInd;
}

/**
Sets the station PCPN Weight Type.
Member method for PCPN station parameter variables
*/
public void setPCPNWeightType(int pcpnWeightType) {
	_pcpnWeightType = pcpnWeightType;
}

// GET Member methods for PE (Potential Evaporation) station parameter variables
/**
Sets the station PE anemometer height.
Member method for PE station parameter variables
*/
public void setPEAnemometerHeight(float peAnemometerHeight) {
	_peAnemometerHeight = peAnemometerHeight;
}

/**
Sets the station PE P factor.
Member method for PE station parameter variables
*/
public void setPEPFactor(float pePFactor) {
	_pePFactor = pePFactor;
}

/**
Sets the station PE Julian date of last day included in the SUMPE.
Member method for PE station parameter variables
@param peLastJulDay the station PE last julian day of SUMPE.
*/
public void setPELastJulDay(int peLastJulDay) {
	_peLastJulDay = peLastJulDay;
}

/**
Sets the station PE primary radiation data.
Member method for PE station parameter variables
*/
public void setPERadiation(int peRadiation) {
	_pePRadiation = peRadiation;
}

/**
Sets the station PE correction factor.
Member method for PE station parameter variables
*/
public void setPECorrectFactor(float peCorrectFactor) {
	_peCorrectFactor = peCorrectFactor;
}

/**
Sets the station PE B3 parameter.
Member method for PE station parameter variables
*/
public void setPEB3(float peB3) {
	_peB3 = peB3;
}

// GET Member methods for RRS (River, Reservoir, and Snow) station parameter 
// variables
/**
Sets the station RRS data types which allow mean discharge distribution
parameters (NDIST).
Member method for RRS station parameter variables
*/
public void setRRSNDIST(int rrsNDIST) {
	_rrsNDIST = rrsNDIST;
}

/**
Sets the station RRS data types which do not allow missing data (NMISS).
Member method for RRS station parameter variables
*/
public void setRRSNMISS(int rrsNMISS) {
	_rrsNMISS = rrsNMISS;
}

/**
Sets the station RRS data types.
Member method for RRS station parameter variables
*/
public void setRRSNTYPE(int rrsNTYPE) {
	_rrsNTYPE = rrsNTYPE;
}

// GET Member methods for TEMP (Temperature) station parameter variables
/**
Sets the station TEMP data type indicator.
Member method for TEMP station parameter variables
@param tempDataInd Temperature data indicator
*/
public void setTEMPDataInd(int tempDataInd) {
	_tempDataInd = tempDataInd;
}

/**
Sets the station TEMP elevation weight.
Member method for TEMP station parameter variables
@param tempElevationWeight TEMP elevation weight
*/
public void setTEMPElevationWeight(float tempElevationWeight) {
	_tempElevationWeight = tempElevationWeight;
}

/**
Sets the station TEMP array location of mean monthly Max/Min.
Member method for TEMP station parameter variables
@param tempLocMeanMonthMaxMin TEMP array location of mean monthly Max/Min
*/
public void setTEMPLocMeanMonthMaxMin(int tempLocMeanMonthMaxMin) {
	_tempLocMeanMonthMaxMin	= tempLocMeanMonthMaxMin;
}

/**
Sets the station TEMP Max correction factor.
Member method for TEMP station parameter variables
@param tempMaxCorrect TEMP Max correction factor
*/
public void setTEMPMaxCorrect(float tempMaxCorrect) {
	_tempMaxCorrect = tempMaxCorrect;
}

/**
Sets the station TEMP forecast max/min indicator.
Member method for TEMP station parameter variables
@param tempMaxMinInd TEMP forecast max/min indicator
*/
public void setTEMPMaxMinInd(int tempMaxMinInd) {
	_tempMaxMinInd = tempMaxMinInd;
}

/**
Sets the station TEMP Min correction factor.
Member method for TEMP station parameter variables
@param tempMinCorrect TEMP Min correction factor
*/
public void setTEMPMinCorrect(float tempMinCorrect) {
	_tempMinCorrect = tempMinCorrect;
}

/**
Sets the station TEMP mountainous type indicator.
Member method for TEMP station parameter variables
@param tempMountainInd TEMP mountainous type indicator
*/
public void setTEMPMountainInd(int tempMountainInd) {
	_tempMountainInd = tempMountainInd;
}

/**
Sets the station TEMP network indicator to determine whether can add
to a MAT area.
Member method for TEMP station parameter variables
@param tempNetInd TEMP network indicator to determine whether can add
*/
public void setTEMPNetInd(int tempNetInd) {
	_tempNetInd = tempNetInd;
}

/**
Sets the station TEMP Time interval of instantaneous temperature data.
Member method for TEMP station parameter variables
@param tempTimeIntervalInst Time Interval of inst. temperature data
*/
public void setTEMPTimeIntervalInst(int tempTimeIntervalInst) {
	_tempTimeIntervalInst	= tempTimeIntervalInst;
}

/**
toString method prints name of ID.
@return a String value with _ID value or "STN:"_ID value
*/
public String toString() {
	return _ID;
}

}
