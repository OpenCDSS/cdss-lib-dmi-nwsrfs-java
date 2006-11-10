//-----------------------------------------------------------------------------
// NWSRFSRatingCurve - NWSRFS Rating Curve 
//-----------------------------------------------------------------------------
// History:
//
// 2002-10-25	Morgan Love, RTi 	Initial version.
//-----------------------------------------------------------------------------

package RTi.DMI.NWSRFS;

import java.util.Vector;

/**
The NWSRFSRatingCurve class stores the organizational information about an
NWSRFS Rating Curves.
@deprecated This class has been deprecated since the functionality has been replaced
by the NWSRFS_DMI package.
*/
public class NWSRFSRatingCurve
{

/**
Identifier for the Rating Curve.
*/
private String _id = "";

/**
Parent to the Rating Curve .
*/
NWSRFSOperation _parent = null;

/**
Boolean used to indicate if the toString() method should
print extra information with the Rating Curve ID.
The default is to not print the extra information.
*/
boolean _verbose = false;

/**
Construct a blank NWSRFSRatingCurve instance 
@param id - ID - ID.
@param parent - NWSRFSOperation that this Operation inherits from.
*/
public NWSRFSRatingCurve ( String id, NWSRFSOperation parent )
{	if ( id != null ) {
		_id = id;
	}
	if ( parent != null ) {
		_parent = parent;
	}
}

/**
Return the Rating Curve identifier.
*/
public String getID()
{	return _id;
}

/**
Returns the NWSRFSOperation that is the parent of this Rating Curve.
*/
public NWSRFSOperation getOperation() {
	return _parent;
}


/**
Sets the toString() method so that it prints "RC: " before the 
Rating Curve ID whenever it is called.  The default is to not print 
the extra information.
@param verbose - boolean to indicate if the toString() method should
add the extra "RC: " information in front of the Rating Curve id.
*/
public void setVerbose( boolean verbose ) {
	_verbose = verbose;
}

/**
Return a String representation of the Rating Curve (the ID).
*/
public String toString ()
{	
	if ( _verbose ) {
		return "RC: "+ _id;
	}
	else 
		return _id;
}

}//end class
