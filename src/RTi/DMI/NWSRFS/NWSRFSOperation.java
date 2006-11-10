//-----------------------------------------------------------------------------
// NWSRFSOperation - NWSRFS operation
//-----------------------------------------------------------------------------
// History:
//
// 2001-12-13	Steven A. Malers, RTi	Initial version.
// 2002-03-11   Morgan Sheedy, RTi      Added NWSRFSSegment parent
//                                      to constructor and added a related
//                                      getSegment() method.
//                                      Added a global variable _verbose to
//                                      use with the toString() method.  If
//                                      _verbose is set to true, the toString()
//                                      method appends "OP: " in front of the
//                                      forecastGroup ID.
// 2002-03018 AMS, RTi			Changed toString() method to pad 
//					System ID name so that all the IDs are
//					the same length and will line up when
//					laid out on tree.
//
// 2002-10-14   AML, RTi                Updated package name 
//                                      (from RTi.App.NWSRFSGUI_APP) to:
//                                      RTi.DMI.NWSRFS.   
//
// 2002-10-14   AML, RTi                Updated to include NwsrfsRatingCurve
//-----------------------------------------------------------------------------

package RTi.DMI.NWSRFS;

import java.util.Vector;

/**
The NWSRFSOperation class stores information about an NWSRFS operation
(list of time series).
@deprecated This class has been deprecated since the functionality has been replaced
by the NWSRFS_DMI package.
*/
public class NWSRFSOperation
{

/**
System identifier for the operation.
*/
private String _sysid = "";

/**
User identifier for the operation.
*/
private String _userid = "";

/**
Time series in the operation (list of TSIdent strings).
*/
private Vector	_tsids = new Vector();

/**
NWSRFSRatingCurve objects in the operation.
*/
private Vector	_ratingCurves = new Vector();

/**
Parent to this Operation.
*/
NWSRFSSegment _parent = null;

/**
Boolean used to indicate if the toString() method should
print extra information ("OP: ") with the Operation ID.
The default is to not print the extra information.
*/
boolean _verbose = false;


/**
Construct a blank NWSRFSOperation instance (no time series).
*/
public NWSRFSOperation ( String sysid, String userid, NWSRFSSegment parent )
{	if ( sysid != null ) {
		_sysid = sysid;
	}
	if ( userid != null ) {
		_userid = userid;
	}
	if ( parent != null ) {
		_parent = parent;
	}
}

/**
Add a Rating Curve identifer to the NWSRFSOperation.
@param rc NWSRFSRatingCurve object to add.
*/
public void addRatingCurve ( NWSRFSRatingCurve rc )
{	
	_ratingCurves.addElement ( rc );
}

/**
Add a time series identifer to the NWSRFSOperation.
@param tsid Time series identifier string to add.
*/
public void addTSID ( String tsid )
{	
	if ( _verbose ) {
		_tsids.addElement ( "TS: " + tsid );
	}
	else 
		_tsids.addElement ( tsid );

}

/**
Returns the NWSRFSSegment that is the parent of this operation.
*/
public NWSRFSSegment getSegment() {
	return _parent;
}

/**
Return the segment system identifier.
*/
public String getSystemID()
{	return _sysid;
}

/**
Return the segment user identifier.
*/
public String getUserID()
{	return _userid;
}

/**
Return the NWSRFSRatingCurve at an index.
@param index Index of rating curve.
@return NWSRFSRatingCurve object at an index.
*/
public NWSRFSRatingCurve getRatingCurve ( int index )
{	return (NWSRFSRatingCurve)_ratingCurves.elementAt(index);
}

/**
Return the rating curve matching the rating curve identifier 
or null if not found.
@param rcid Rating Curve identifier.
@return the Rating Curve matching the identifier.
*/
public NWSRFSRatingCurve getRatingCurve ( String rcid )
{	int size = _ratingCurves.size();
	NWSRFSRatingCurve rc= null;
	for ( int i = 0; i < size; i++ ) {
		rc = (NWSRFSRatingCurve)_ratingCurves.elementAt(i);
		if ( rc.getID().equalsIgnoreCase(rcid) ) {
			return rc;
		}
	}
	rc = null;
	return rc;
}


/**
Return the NWSRFSRatingCurve.
@return the list of NWSRFSRatingCurve objects.
*/
public Vector getRatingCurves()
{	return _ratingCurves;
}

/**
Return the time series identifier at an index.
@param index Index of time series identifier.
@return the time series identifier at an index.
*/
public String getTSID ( int index )
{	return (String)_tsids.elementAt(index);
}

/**
Return the time series identifiers.  This is guaranteed to be non-null.
@return the list of forecast groups.
*/
public Vector getTSIDs()
{	return _tsids;
}

/**
Sets the toString() method so that it prints "OP: " before the
Operation ID whenever it is called.  It also prints "TS: " before
the time series identifier.  The default is to not print 
the extra information.
@param verbose - boolean to indicate if the toString() method should
add the extra "OP: " information in front of the forecast group.
*/
public void setVerbose( boolean verbose ) {
        _verbose = verbose;
}

/**
Return a String representation of the operation (sysID,userID).
*/
public String toString ()
{	

	//trim string
	_sysid = _sysid.trim();
	//we want the operation to be a fixed width of 10 characters.
	int len = 0;
	len = _sysid.length();

	StringBuffer b_sys = new StringBuffer( _sysid );
	for ( int i=len; i<10; i++ ) {
		b_sys.append( ' ' );
	}
	_sysid = b_sys.toString();
		

	if ( _verbose ) {
		// return "OP:" + _sysid + " " +  _userid;
		return "OP: " + _sysid + _userid;
	}
	else {
		//return _sysid + " " +  _userid;
		return _sysid + _userid;
	}
}

}//end class NWSRFSOperation
