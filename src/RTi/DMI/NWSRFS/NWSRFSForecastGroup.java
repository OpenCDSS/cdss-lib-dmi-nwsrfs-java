//-----------------------------------------------------------------------------
// NWSRFSForecastGroup - NWSRFS forecast group
//-----------------------------------------------------------------------------
// History:
//
// 2001-12-13	Steven A. Malers, RTi	Initial version.
//
// 2002-03-11	Morgan Sheedy, RTi	Added NWSRFSCarryoverGroup parent
//					to constructor and added a related 
//					getCarryoverGroup() method.		
//					Added a global variable _verbose to 
//					use with the toString() method.  If
//					_verbose is set to true, the toString()
//					method appends "FG: " in front of the
//					forecastGroup ID.
//
// 2002-10-14   AML, RTi                Updated package name 
//                                      (from RTi.App.NWSRFSGUI_APP) to:
//                                      RTi.DMI.NWSRFS.   
//-----------------------------------------------------------------------------

package RTi.DMI.NWSRFS;

import java.util.Vector;

/**
The NWSRFSForecastGroup class stores the organizational information about an
NWSRFS forecast group (list of segments).
@deprecated This class has been deprecated since the functionality has been replaced
by the NWSRFS_DMI package.
*/
public class NWSRFSForecastGroup
{

/**
Identifier for the forecast group.
*/
private String _id = "";

/**
Segments in the forecast group.
*/
private Vector	_segments = new Vector();

/**
Parent to the Forecast Group.
*/
NWSRFSCarryoverGroup _parent = null;

/**
Boolean used to indicate if the toString() method should
print extra information with the forecast group ID.
The default is to not print the extra information.
*/
boolean _verbose = false;

/**
Construct a blank NWSRFSForecastGroup instance (no segments groups).
@param id - ID - ID.
@param parent - NWSRFSCarryoverGroup that this ForecastGroup inherits from.
*/
public NWSRFSForecastGroup ( String id, NWSRFSCarryoverGroup parent )
{	if ( id != null ) {
		_id = id;
	}
	if ( parent != null ) {
		_parent = parent;
	}
}

/**
Add an NWSRFSSegment to the NWSRFSForecastGroup.
@param seg NWSRFSSegment to add.
*/
public void addSegment ( NWSRFSSegment seg )
{	_segments.addElement ( seg );
}

/**
Return the forecast group identifier.
*/
public String getID()
{	return _id;
}

/**
Returns the NWSRFSCarryoverGroup that is the parent of this Forecast Group.
*/
public NWSRFSCarryoverGroup getCarryoverGroup() {
	return _parent;
}

/**
Return the segment at an index.
@param index Index of segment.
@return the segment at an index.
*/
public NWSRFSSegment getSegment ( int index )
{	return (NWSRFSSegment)_segments.elementAt(index);
}

/**
Return the segment matching the segment identifier or null if not found.
@param segid Segment identifier.
@return the segment matching the identifier.
*/
public NWSRFSSegment getSegment ( String segid )
{	int size = _segments.size();
	NWSRFSSegment seg= null;
	for ( int i = 0; i < size; i++ ) {
		seg = (NWSRFSSegment)_segments.elementAt(i);
		if ( seg.getID().equalsIgnoreCase(segid) ) {
			return seg;
		}
	}
	seg = null;
	return seg;
}

/**
Return the segements groups.  This is guaranteed to be non-null.
@return the list of segements.
*/
public Vector getSegments()
{	return _segments;
}

/**
Sets the toString() method so that it prints "FG: " before the 
forecast ID whenever it is called.  The default is to not print 
the extra information.
@param verbose - boolean to indicate if the toString() method should
add the extra "FG: " information in front of the forecast group.
*/
public void setVerbose( boolean verbose ) {
	_verbose = verbose;
}

/**
Return a String representation of the forecast group (the ID).
*/
public String toString ()
{	
	if ( _verbose ) {
		return "FG: "+ _id;
	}
	else 
		return _id;
}

}
