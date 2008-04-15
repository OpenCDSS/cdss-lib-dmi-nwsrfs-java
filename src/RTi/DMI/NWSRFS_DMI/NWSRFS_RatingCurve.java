//-----------------------------------------------------------------------------
// NWSRFS_RatingCurve - NWSRFS Rating Curve 
//-----------------------------------------------------------------------------
// History:
//
// 2004-04-15	Scott Townsend, RTi	Initial version.
// 2004-07-12   Anne Morgan Love, RTi   Removed setVerbose(), which
//                                      added additional identifier strings in
//                                      front of the String returned from
//                                      .toString() if was true (for example:
//                                      "RC: " for Rating Curve).
// 2004-08-19	J. Thomas Sapienza, RTi	* Revised for RTi standards.
//					* Added get*() and set*() methods.
// 2004-08-23	JTS, RTi		Added finalize().
// 2004-09-17	SAT, RTi		Decoupled Station information from
//					the rating curve.
//-----------------------------------------------------------------------------

package RTi.DMI.NWSRFS_DMI;

import RTi.DMI.NWSRFS_DMI.NWSRFS_Operation;

/**
The NWSRFS_RatingCurve class stores the organizational information about an
NWSRFS Rating Curves. This class reads and stores data from the 
FCRATING forecast component database file; 
The FCRATING file has the following definition:
<pre>

FILE NAME:  FCRATING 
 
 
Purpose 
 
File FCRATING contains the definitions of the Rating Curves 
used by the Forecast Component. One Rating Curve definition 
is held in common block FRATNG. 
 
 
Description 
 
ATTRIBUTES: fixed length 1200 byte binary records 
RECORD STRUCTURE: 
 
Variable      Type      Dimension      Word Pos.    Description 
 
RTCVID        A8        1              1            Rating Curve identifier 
 
RIVERN        A20       1              3            River name 
 
RIVSTA        A20       1              8            Station name 
 
RLAT          R*4       1              13           Latitude of station 
 
RLONG         R*4       1              14           Longitude of station 
 
FPTYPE        A4        5              15           Code for type of forecast 
							point.  
                                                    Codes indicates the 
						    forecast product 
                                                    generated for this point 
						    for each  
                                                    general type of forecast: 
                                                    Regular river forecast 
						    codes: 
                                                       'DOAP' = optional daily 
                                                       'DAMA' = mandatory daily 
                                                       'FLUD' = flood only 
                                                       'SPRT' = support (Tulsa 
						       RFC)
 
                                                    Reservoir forecast codes: 
                                                       'RSQ ' = reservoir 
						       		elevation  
                                                                and inflow 
                                                       'INF ' = reservoir inflow
                                                                only
 
                                                    Extended forecast codes: 
                                                       'SOOP' = spring outlook -
                                                                optional 
                                                       'SOMA' = spring outlook -
                                                                mandatory 
                                                       'WSUP' = water supply 

                                                    Flash flood: 
                                                       'HWFF' = headwater table 
						       and  
                                                                flash flood 
								index 
                                                                only 
                                                       'FFON' = flash flood 
						       index  
                                                                only 
                                                       'HWON' = headwater table 
                                                                only 
 
AREAT         R*4       1              20           Total drainage area (KM2) 
 
AREAL         R*4       1              21           Local drainage area (KM2) 
 
FLDSTG        R*4       1              22           Primary flood stage (M) 
                                                       -999. = not defined 
 
FLOODQ        R*4       1              23           Flood flow for primary 
							flood stage  
                                                       (CMS) -999. = not 
						       defined 
 
PVISFS        A4        1              24           Provisional flood stage  
							indicator: 
                                                       'P   ' = primary flood 
						       	stage  
                                                                is provisional 
                                                       '    ' = otherwise 
 
SCFSTG        R*4       1              25           Secondary flood stage (M) 
                                                       -999. = not defined 
 
WRNSTG        R*4       1              26           Warning or alert stage (M) 
                                                       -999. = not defined 
 
GZERO         R*4       1              27           Gage zero datum (M) 
							(elevation above  
                                                    Mean Sea Level corresponding
						    	to zero  
                                                    stage) -999. = missing 
 
NRCPTS        I*4       1              28           Number of stage versus 
							discharge  
                                                    points used to define the 
						    	rating curve 
                                                       0 = no rating curve 
						       included 
 
LOCQ          I*4       1              29           Starting location of the 
							rating curve  
                                                    discharge values in the XRC 
						    	array 
                                                       0 = none 
 
LOCH          I*4       1              30           Starting location of the 
							rating curve  
                                                    stage values in the XRC 
						    	array 
                                                       0 = none 
 
STGMIN        R*4       1              31           Minimum allowable stage (M) 
							- not  
                                                    defined if NRCPTS=0 
 
 
 
NCROSS        I*4       1              32           Number of values in 
							cross-sectional  
                                                    data table 
                                                       0 = not needed 
 
LXTOPW        I*4       1              33           Starting location of the  
                                                    cross-sectional top 
						    width values in  
                                                    the XRC array - not 
						    defined if  
                                                    NCROSS=0 
 
LXELEV        I*4       1              34           Starting location of the  
                                                    cross-sectional elevation 
						    values in  
                                                    the XRC array - not 
						    defined if  
                                                    NCROSS=0 
 
ABELOW        R*4       1              35           Area below first 
							cross-sectional  
                                                    elevation (M2) - not 
						    defined if  
                                                    NCROSS=0 
 
FLOODN        R*4       1              36           Manning's n for flood 
							plain above  
                                                    uppermost cross-sectional 
						    elevation -  
                                                    not defined if NCROSS=0 
 
SLOPE         R*4       1              37           Channel-bottom slope (M/M) -
                                                    not defined if NCROSS=0 
 
FRLOOP        R*4       1              38           r term in dynamic loop 
							computations -  
                                                    not defined if NCROSS=0 
 
SHIFT         R*4       1              39           Shift factor used during 
							log-log  
                                                    extrapolation of low flows 
						    	(M) -  
                                                    not defined if NRCPTS=0 
 
OPTION        A4        1              40           Type of units used when 
							the rating  
                                                    curve was defined: 
                                                       'ENGL' = English units 
                                                       'METR' = metric units 
 
LASDAY        R*4       1              41           Last day that rating curve 
							should be  
                                                    used (Julian day) (for 
						    calibration use
                                                    only) 
                                                       0 = no limit 
 
IPOPT         I*4       1              42           Pointer to starting 
							position in the  
                                                    XRC array where optional 
						    information  
                                                    is stored 
 
Flood of record information: 
RFSTG         R*4       1              43           o  stage (M) 
                                                       -999. = not defined 
RFQ           R*4       1              44           o  discharge (CMS) 
                                                       -999. = not defined 
 
IRFDAY        I*4       1              45           o  date (form is mmddyyyy - 
                                                       computed using 
						       yyyy+dd*10**4+mm*10**6) 
                                                       -999. = not defined 
RFCOMPT       A4        5              46           o  comment 
                                                       none = blank 
 
EMPTY         R*4       25             51           Array positions: 
                                                       1. Pointer to starting 
						       location in  
                                                          the XRC array where 
							  information  
                                                          for the computation 
							  of FRLOOP is  
                                                          stored 
                                                       2. Pointer to starting 
						       location in  
                                                          the XRC array where 
							  information  
                                                          on offset factors is 
							  stored 
                                                       3. Stage below which the 
						       shift  
                                                          factor will be used 
							  (M) -  
                                                          not defined if 
							  NRCPTS=0 
                                                       4. Indicator for rating 
						       curve  
                                                          interpolation/
							  extrapolation 
                                                          method: 
                                                             0 = logarithmic 
                                                             1 = linear 
                                                             5-25 For future use
 
XRC           R*4       225            76           Space to hold: 
                                                       o Rating Curve stage and 
						       discharge  
                                                         values (starting at 
							 location  
                                                         LOCH with NRCPTS 
							 values of stage  
                                                         followed by NRCPTS 
							 values of  
                                                         discharge starting at 
							 location  
                                                         LOCQ) 
                                                       o Cross-sectional data 
						       (starting  
                                                         at location LXELEV 
							 with NCROSS  
                                                         values of elevations 
							 followed by  
                                                         NCROSS values of 
							 channel  
                                                         topwidth starting at 
							 location  
                                                         LXTOPW) 
                                                       o FRLOOP information  1/ 
                                                       o number of offset 
						       factors and the  
                                                         stage/offset-pairs  2/ 
                                                       o optional information  
						       3/ 
 
 
NOTES: 
 
1/ Space in the XRC array starting at the location defined by EMPTY(1) is used 
to store information which is used in the computation of FRLOOP. 
 
   The number of values is 8+2*NOCS and are: 
 
              Position       Contents 
              ----------------------------------------------------------------
              1              Time to peak for typical flood in hours 
              2              Discharge at beginning of typical flood 
              3              Peak discharge for typical flood 
              4              Stage at beginning of typical flood 
              5              Peak stage for typical flood 
              6              Minimum discharge below which loop effects will be 
                             ignored 
              7              Minimum stage below which loop effects will be 
	      			ignored 
              8              Number of cross section points for off-channel 
	      			storage  
                             (NOCS) 
              9 to           Elevation-topwidth pairs to define off-channel 
              8+2*NOCS       storage cross sections (NOCS values of elevation 
	      			followed  
                             by NOCS topwidth for off-channel storage) 

   If a loop rating is not used then FRLOOP=-999., EMPTY(1)=0 and no space is  
   used in XRC to store this information. 
 
2/ Space in the XRC array starting at the location defined by EMPTY(2) is used  
   to store offset factors.  The values stored are as follows: 
 
              Position       Contents 
              -------------------------------------------------------
              1              Number of offset factors defined (NOFF) 
              2              Stage above which offset is applied (M) 
              2+NOFF         Offset factor (M) 
 
   Positions 2 is repeated NOFF times followed by position 3 repeated NOFF  
   times. 
 
3/ Space in the XRC array starting at the location defined by IPOPT is used to  
   store optional information for the forecast point.  For each piece of  
   information in this section, the following information will be stored. 
 
              Position       Contents 
              ------------------------------------------------------------------
              1              Number code for this piece of optional 
	      			information (-1  
                             indicates no more optional information in XRC) 
              2              Location of the next number code in the XRC array 
              3 to 2+L       The optional information, where L is the length 
	      			of space  
                             used to store information 
 
   The following optional information is allowed in the Rating Curve file: 
 
                                Length of	 
         Number    Character    Entry
         Code      Code         (words)     Form    Item 
         -----------------------------------------------------------------------
         1         COMMENT      Variable    A       General comment space 
         2         USGS-ID      2           A       USGS identifier 
         3         NWS-ID       2           A       NWS location identifier 
	 						(5 characters) 
         4         BANKFUL-STG  1           R       Bankfull stage (M) 
         5         RIVER-LOC    1           R       River location - distance 
	 						from mouth  
         6         MOB-STG      1           R       Mobilization stage (M) 
         7         HSA-ID       2           A       Hydrologic Service Area 
	 						identifier 
         8         E-19         1           I       Date of latest E-19 update 
	 						(mmyy) 
         9         E-19A        1           I       Date of latest E-19A update 
	 						(mmyy) 

                   IX.4.5B-FCRATING 
</pre>
*/
public class NWSRFS_RatingCurve {

/**
Identifier for the Rating Curve.
*/
private String __rcid;

/**
Parent Operation.
*/
private NWSRFS_Operation __op;

/**
Array positions.
*/
protected float[] _EMPTY;

/**
Extra space for computations.
*/
protected float[] _XRC;

/**
Area below cross-section elevation.
*/
protected float _ABELOW;

/**
Local drainage area.
*/
protected float _AREAL;

/**
Total drainage area.
*/
protected float _AREAT;

/**
Primary flood stage.
*/
protected float _FLDSTG;

/**
Manning's N for flood plain.
*/
protected float _FLOODN;

/**
Flood flow.
*/
protected float _FLOODQ;

/**
R term in dynamic loop computations.
*/
protected float _FRLOOP;

/**
Gage zero datum.
*/
protected float _GZERO;

/**
Last day for Rating Curve.
*/
protected float _LASDAY;

/**
Flood of record flow value.
*/
protected float _RFQ;

/**
Flood of record stage value.
*/
protected float _RFSTG;

/**
Latitude of Station.
*/
protected float _RLAT;

/**
Longitude of Station.
*/
protected float _RLONG;

/**
Secondary flood stage.
*/
protected float _SCFSTG;

/**
Shift factor.
*/
protected float _SHIFT;

/**
Channel-bottom slope.
*/
protected float _SLOPE;

/**
Minimum allowed stage.
*/
protected float _STGMIN;

/**
Warning stage.
*/
protected float _WRNSTG;

/**
Pointer to starting location in _XRC array.
*/
protected int _IPOPT;

/**
Flood of record date. Computed using yyyy+dd*10**4+mm*10**6.
*/
protected int _IRFDAY;

/**
Starting location of the Rating Curve in the _XRC array.
*/
protected int _LOCH;

/**
Starting location of the Rating Curve.
*/
protected int _LOCQ;

/**
Starting location of the cross-section elevation.
*/
protected int _LXELEV;

/**
Starting location of the cross-section top width.
*/
protected int _LXTOPW;

/**
Number of values in cross-section table.
*/
protected int _NCROSS;

/**
Number of stage VS flow values.
*/
protected int _NRCPTS;

/**
Code for type of forecast point.
*/
protected String[] _FPTYPE;

/**
User Comments.
*/
protected String[] _RFCOMPT;

/**
Type of units.
*/
protected String _OPTION;

/**
Provisional flood stage.
*/
protected String _PVISFS;

/**
River name.
*/
protected String _RIVERN;

/**
Station name/id.
*/
protected String _RIVSTA;

/**
Rating Curve identifier.
*/
protected String _RTCVID;

/**
Constructor.
@param id rating curve ID.  Can be null.
*/
public NWSRFS_RatingCurve(String id) {

	initialize();

	// Set the rating curve id
	if (id != null) {
		__rcid = id;
	}
}

/**
Constructor.
@param id rating curve id.
@param parent NWSRFS_Operation that this rating curve inherits from.
*/
public NWSRFS_RatingCurve (String id, NWSRFS_Operation parent) {
	initialize();

	if (id != null) {
		__rcid = id;
	}

	if (parent != null) {
		__op = parent;
	}
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize() 
throws Throwable {
	__rcid = null;
	__op = null;
	_EMPTY = null;
	_XRC = null;
	_FPTYPE = null;
	_RFCOMPT = null;
	_OPTION = null;
	_PVISFS = null;
	_RIVERN = null;
	_RIVSTA = null;
	_RTCVID = null;
}

/**
Returns the area below cross-section elevation
@return the area below cross-section elevation
*/
public float getABELOW() {
	return _ABELOW;
}

/**
Returns the local drainage area.
@return the local drainage area.
*/
public float getAREAL() {
	return _AREAL;
}

/**
Returns the total drainage area.
@return the total drainage area.
*/
public float getAREAT() {
	return _AREAT;
}

/**
Returns the array positions.
@return the array positions.
*/
public float[] getEMPTY() {
	return _EMPTY;
}

/**
Returns the primary flood stage.
@return the primary flood stage.
*/
public float getFLDSTG() {
	return _FLDSTG;
}

/**
Returns Manning's N for flood plain.
@return Manning's N for flood plain.
*/
public float getFLOODN() {
	return _FLOODN;
}

/**
Returns flood flow.
@return flood flow.
*/
public float getFLOODQ() {
	return _FLOODQ;
}

/**
Returns the code for the type of the forecast point.
@return the code for the type of the forecast point.
*/
public String[] getFPTYPE() {
	return _FPTYPE;
}

/**
Returns the R term in dynamic loop computations.
@return the R term in dynamic loop computations.
*/
public float getFRLOOP() {
	return _FRLOOP;
}

/**
Returns the gage zero datum.
@return the gage zero datum.
*/
public float getGZERO() {
	return _GZERO;
}

/**
Returns the pointer to the starting location in the _XRC array.
@return the pointer to the starting location in the _XRC array.
*/
public int getIPOPT() {
	return _IPOPT;
}

/**
Returns the flood of record date.
@return the flood of record date.
*/
public int getIRFDAY() {
	return _IRFDAY;
}

/**
Returns the last day for the rating curve.
@return the last day for the rating curve.
*/
public float getLASDAY() {
	return _LASDAY;
}

/**
Returns the starting location of the rating curve in the _XRC array.
@return the starting location of the rating curve in the _XRC array.
*/
public int getLOCH() {
	return _LOCH;
}

/**
Returns the starting location of the rating curve.
@return the starting location of the rating curve.
*/
public int getLOCQ() {
	return _LOCQ;
}

/**
Returns the starting location of the cross-section elevation.
@return the starting location of the cross-section elevation.
*/
public int getLXELEV() {
	return _LXELEV;
}

/**
Returns the starting location of the cross-section top width.
@return the starting location of the cross-section top width.
*/
public int getLXTOPW() {
	return _LXTOPW;
}

/**
Returns the number of values in the cross-section table.
@return the number of values in the cross-section table.
*/
public int getNCROSS() {
	return _NCROSS;
}

/**
Returns the number of stage vs flow values.
@return the number of stage vs flow values.
*/
public int getNRCPTS() {
	return _NRCPTS;
}

/**
Returns the NWSRFS_Operation that is the parent of this Rating Curve.
@return the NWSRFS_Operation that is the parent of this Rating Curve.
*/
public NWSRFS_Operation getOperation() {
	return __op;
}

/**
Returns the type of units.
@return the type of units.
*/
public String getOPTION() {
	return _OPTION;
}

/**
Returns the provisional flood stage.
@return the provisional flood stage.
*/
public String getPVISFS() {
	return _PVISFS;
}


/**
Returns the Rating Curve identifier.
@return the Rating Curve identifier.
*/
public String getRCID() {
	return __rcid;
}

/**
Returns the user comments.
@return the user comments.
*/
public String[] getRFCOMPT() {
	return _RFCOMPT;
}

/**
Returns the flood of record flow value.
@return the flood of record flow value.
*/
public float getRFQ() {
	return _RFQ;
}

/**
Returns the flood of record stage value.
@return the flood of record stage value.
*/
public float getRFSTG() {
	return _RFSTG;
}

/**
Returns the river name.
@return the river name.
*/
public String getRIVERN() {
	return _RIVERN;
}

/**
Returns the station name/id.
@return the station name/id.
*/
public String getRIVSTA() {
	return _RIVSTA;
}

/**
Returns the latitude of the station.
@return the latitude of the station.
*/
public float getRLAT() {
	return _RLAT;
}

/**
Returns the longitude of the station.
@return the longitude of the station.
*/
public float getRLONG() {
	return _RLONG;
}

/**
Returns the rating curve identifier.
@return the rating curve identifier.
*/
public String getRTCVID() {
	return _RTCVID;
}

/**
Returns the secondary flood stage.
@return the secondary flood stage.
*/
public float getSCFSTG() {
	return _SCFSTG;
}

/**
Returns the shift factor.
@return the shift factor.
*/
public float getSHIFT() {
	return _SHIFT;
}

/**
Returns the channel-bottom slope.
@return the channel-bottom slope.
*/
public float getSLOPE() {
	return _SLOPE;
}

/**
Returns the minimum allowed stage.
@return the minimum allowed stage.
*/
public float getSTGMIN() {
	return _STGMIN;
}

/**
Returns the warning stage.
@return the warning stage.
*/
public float getWRNSTG() {
	return _WRNSTG;
}

/**
Returns the extra space for computations.
@return the extra space for computations.
*/
public float[] getXRC() {
	return _XRC;
}

/**
Initialize global objects.
*/
private void initialize() {
	__rcid = null;
	_RTCVID = null;
	_RIVERN = null;
	_RIVSTA = null;
	_RLAT = 0;
	_RLONG = 0;
	_FPTYPE = new String[5];
	_AREAT  = 0;
	_AREAL = 0;
	_FLDSTG = 0;
	_FLOODQ = 0;
	_PVISFS = null;
	_SCFSTG = 0;
	_WRNSTG = 0;
	_GZERO = 0;
	_NRCPTS = 0;
	_LOCQ = 0;
	_LOCH = 0;
	_STGMIN = 0;
	_NCROSS = 0;
	_LXTOPW = 0;
	_LXELEV = 0;
	_ABELOW = 0;
	_FLOODN = 0;
	_SLOPE = 0;
	_FRLOOP = 0;
	_SHIFT = 0;
	_OPTION = null;
	_LASDAY = 0;
	_IPOPT = 0;
	_RFSTG = 0;
	_RFQ = 0;
	_IRFDAY = 0;
	_RFCOMPT = new String[5];
	_EMPTY = new float[25];
	_XRC = new float[225];
}

/**
Sets the area below cross-section elevation
@param ABELOW the area below cross-section elevation
*/
public void setABELOW(float ABELOW) {
	_ABELOW = ABELOW;
}

/**
Sets the local drainage area.
@param AREAL the local drainage area.
*/
public void setAREAL(float AREAL) {
	_AREAL = AREAL;
}

/**
Sets the total drainage area.
@param AREAT the total drainage area.
*/
public void setAREAT(float AREAT) {
	_AREAT = AREAT;
}

/**
Sets the array positions.
@param pos the array position to set.
@param EMPTY the array positions.
*/
public void setEMPTY(int pos, float EMPTY) {
	_EMPTY[pos] = EMPTY;
}

/**
Sets the primary flood stage.
@param FLDSTG the primary flood stage.
*/
public void setFLDSTG(float FLDSTG) {
	_FLDSTG = FLDSTG;
}

/**
Sets Manning's N for flood plain.
@param FLOODN Manning's N for flood plain.
*/
public void setFLOODN(float FLOODN) {
	_FLOODN = FLOODN;
}

/**
Sets flood flow.
@param FLOODQ flood flow.
*/
public void setFLOODQ(float FLOODQ) {
	_FLOODQ = FLOODQ;
}

/**
Sets the code for the type of the forecast point.
@param pos the array position to set.
@param FPTYPE the code for the type of the forecast point.
*/
public void setFPTYPE(int pos, String FPTYPE) {
	_FPTYPE[pos] = FPTYPE;
}

/**
Sets the R term in dynamic loop computations.
@param FRLOOP the R term in dynamic loop computations.
*/
public void setFRLOOP(float FRLOOP) {
	_FRLOOP = FRLOOP;
}

/**
Sets the gage zero datum.
@param GZERO the gage zero datum.
*/
public void setGZERO(float GZERO) {
	_GZERO = GZERO;
}

/**
Sets the pointer to the starting location in the _XRC array.
@param IPOPT the pointer to the starting location in the _XRC array.
*/
public void setIPOPT(int IPOPT) {
	_IPOPT = IPOPT;
}

/**
Sets the flood of record date.
@param IRFDAY the flood of record date.
*/
public void setIRFDAY(int IRFDAY) {
	_IRFDAY = IRFDAY;
}

/**
Sets the last day for the rating curve.
@param LASDAY the last day for the rating curve.
*/
public void setLASDAY(float LASDAY) {
	_LASDAY = LASDAY;
}

/**
Sets the starting location of the rating curve in the _XRC array.
@param LOCH the starting location of the rating curve in the _XRC array.
*/
public void setLOCH(int LOCH) {
	_LOCH = LOCH;
}

/**
Sets the starting location of the rating curve.
@param LOCQ the starting location of the rating curve.
*/
public void setLOCQ(int LOCQ) {
	_LOCQ = LOCQ;
}

/**
Sets the starting location of the cross-section elevation.
@param LXELEV the starting location of the cross-section elevation.
*/
public void setLXELEV(int LXELEV) {
	_LXELEV = LXELEV;
}

/**
Sets the starting location of the cross-section top width.
@param LXTOPW the starting location of the cross-section top width.
*/
public void setLXTOPW(int LXTOPW) {
	_LXTOPW = LXTOPW;
}

/**
Sets the number of values in the cross-section table.
@param NCROSS the number of values in the cross-section table.
*/
public void setNCROSS(int NCROSS) {
	_NCROSS = NCROSS;
}

/**
Sets the number of stage vs flow values.
@param NRCPTS the number of stage vs flow values.
*/
public void setNRCPTS(int NRCPTS) {
	_NRCPTS = NRCPTS;
}

/**
Sets the NWSRFS_Operation that is the parent of this Rating Curve.
@param op the NWSRFS_Operation that is the parent of this Rating Curve.
*/
public void setOperation(NWSRFS_Operation op) {
	__op = op;
}

/**
Sets the type of units.
@param OPTION the type of units.
*/
public void setOPTION(String OPTION) {
	_OPTION = OPTION;
}

/**
Sets the provisional flood stage.
@param PVISFS the provisional flood stage.
*/
public void setPVISFS(String PVISFS) {
	_PVISFS = PVISFS;
}


/**
Sets the Rating Curve identifier.
@param rcid the Rating Curve identifier.
*/
public void setRCID(String rcid) {
	__rcid = rcid;
}

/**
Sets the user comments.
@param pos the array position to set.
@param RFCOMPT the user comments.
*/
public void setRFCOMPT(int pos, String RFCOMPT) {
	_RFCOMPT[pos] = RFCOMPT;
}

/**
Sets the flood of record flow value.
@param RFQ the flood of record flow value.
*/
public void setRFQ(float RFQ) {
	_RFQ = RFQ;
}

/**
Sets the flood of record stage value.
@param RFSTG the flood of record stage value.
*/
public void setRFSTG(float RFSTG) {
	_RFSTG = RFSTG;
}

/**
Sets the river name.
@param RIVERN the river name.
*/
public void setRIVERN(String RIVERN) {
	_RIVERN = RIVERN;
}

/**
Sets the station name/id.
@param RIVSTA the station name/id.
*/
public void setRIVSTA(String RIVSTA) {
	_RIVSTA = RIVSTA;
}

/**
Sets the latitude of the station.
@param RLAT the latitude of the station.
*/
public void setRLAT(float RLAT) {
	_RLAT = RLAT;
}

/**
Sets the longitude of the station.
@param RLONG the longitude of the station.
*/
public void setRLONG(float RLONG) {
	_RLONG = RLONG;
}

/**
Sets the rating curve identifier.
@param RTCVID the rating curve identifier.
*/
public void setRTCVID(String RTCVID) {
	_RTCVID = RTCVID;
}

/**
Sets the secondary flood stage.
@param SCFSTG the secondary flood stage.
*/
public void setSCFSTG(float SCFSTG) {
	_SCFSTG = SCFSTG;
}

/**
Sets the shift factor.
@param SHIFT the shift factor.
*/
public void setSHIFT(float SHIFT) {
	_SHIFT = SHIFT;
}

/**
Sets the channel-bottom slope.
@param SLOPE the channel-bottom slope.
*/
public void setSLOPE(float SLOPE) {
	_SLOPE = SLOPE;
}

/**
Sets the minimum allowed stage.
@param STGMIN the minimum allowed stage.
*/
public void setSTGMIN(float STGMIN) {
	_STGMIN = STGMIN;
}

/**
Sets the warning stage.
@param WRNSTG the warning stage.
*/
public void setWRNSTG(float WRNSTG) {
	_WRNSTG = WRNSTG;
}

/**
Sets the extra space for computations.
@param pos the array position to set.
@param XRC the extra space for computations.
*/
public void setXRC(int pos, float XRC) {
	_XRC[pos] = XRC;
}

/**
Return a String representation of the Rating Curve (the ID).
*/
public String toString() {
	return __rcid;
}

}
