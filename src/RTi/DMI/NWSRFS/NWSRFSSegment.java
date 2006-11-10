//-----------------------------------------------------------------------------
// NWSRFSSegment - NWSRFS segment
//-----------------------------------------------------------------------------
// History:
//
// 2001-12-13	Steven A. Malers, RTi	Initial version.
//
// 2002-03-11   Morgan Sheedy, RTi      Added NWSRFSForecastGroup parent
//                                      to constructor and added a related
//                                      getForecastGroup() method.
//                                      Added a global variable _verbose to
//                                      use with the toString() method.  If
//                                      _verbose is set to true, the toString()
//                                      method appends "SEG: " in front of the
//                                      Segment ID.
//
// 2002-10-14   AML, RTi                Updated package name 
//                                      (from RTi.App.NWSRFSGUI_APP) to:
//                                      RTi.DMI.NWSRFS.   
//-----------------------------------------------------------------------------


package RTi.DMI.NWSRFS;

import java.util.Vector;

/**
The NWSRFSSegment class stores the organizational information about an
NWSRFS segment group (list of operations).
@deprecated This class has been deprecated since the functionality has been replaced
by the NWSRFS_DMI package.
*/
public class NWSRFSSegment
{

/**
Identifier for the segment.
*/
private String _id = "";

/**
Operations in the segment.
*/
private Vector	_operations = new Vector();

/**
Parent to this Operation.
*/
NWSRFSForecastGroup _parent = null;

/**
Boolean used to indicate if the toString() method should
print extra information ("SEG: ") with the Operation ID.
The default is to not print the extra information.
*/
boolean _verbose = false;

/**
Construct a blank NWSRFSSegment instance (no operations).
*/
public NWSRFSSegment ( String id, NWSRFSForecastGroup parent )
{	if ( id != null ) {
		_id = id;
	}
	if ( parent != null ) {
		_parent = parent;
	}
}

/**
Add an NWSRFSOperation to the NWSRFSSegment.
@param op NWSRFSOperation to add.
*/
public void addOperation ( NWSRFSOperation op )
{	_operations.addElement ( op );
}

/**
Return the segment identifier.
*/
public String getID()
{	return _id;
}

/**
Return the operation at an index.
@param index Index of operation.
@return the operation at an index.
*/
public NWSRFSOperation getOperation ( int index )
{	return (NWSRFSOperation)_operations.elementAt(index);
}

/**
Return the operation matching the operation identifier or null if not found.
@param sysid Operation system identifier.
@param userid Operation user identifier.
@return the operation matching the identifer.
*/
public NWSRFSOperation getOperation ( String sysid, String userid )
{	int size = _operations.size();
	NWSRFSOperation op= null;
	for ( int i = 0; i < size; i++ ) {
		op = (NWSRFSOperation)_operations.elementAt(i);
		if (	op.getSystemID().equalsIgnoreCase(sysid) &&
			op.getUserID().equalsIgnoreCase(userid) ) {
			return op;
		}
	}
	op = null;
	return op;
}

/**
Return the operations.  This is guaranteed to be non-null.
@return the list of operations.
*/
public Vector getOperations()
{	return _operations;
}


/**
Returns the NWSRFSForecastGroup that is the parent of this segment.
*/
public NWSRFSForecastGroup getForecastGroup() {
	return _parent;
}

/**
Sets the toString() method so that it prints "SEG: " before the
Operation ID whenever it is called.  The default is to not print
the extra information.
@param verbose - boolean to indicate if the toString() method should
add the extra "SEG: " information in front of the segment id.
*/
public void setVerbose( boolean verbose ) {
        _verbose = verbose;
}

/**
Return a String representation of the segment (the ID).
*/
public String toString () {
        if ( _verbose ) {
                return "SEG: " + _id;
        }
   	else  
		return _id;

}
} //end class
