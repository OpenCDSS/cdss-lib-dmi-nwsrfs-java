//-----------------------------------------------------------------------------
// NWSRFSCarryoverGroup - NWSRFS carryover group
//-----------------------------------------------------------------------------
// History:
//
// 2001-12-13	Steven A. Malers, RTi	Initial version.
//
// 2002-03-11   Morgan Sheedy, RTi      
//                                      Added a global variable _verbose to
//                                      use with the toString() method.  If
//                                      _verbose is set to true, the toString()
//                                      method appends "CG: " in front of the
//                                      Segment ID.
//
// 2002-10-14   AML, RTi                Updated package name 
//                                      (from RTi.App.NWSRFSGUI_APP) to:
//                                      RTi.DMI.NWSRFS.   
//-----------------------------------------------------------------------------

package RTi.DMI.NWSRFS;

import java.util.Vector;

/**
The NWSRFSCarryoverGroup class stores the organizational information about an
NWSRFS carryover group (list of forecast groups).
@deprecated This class has been deprecated since the functionality has been replaced
by the NWSRFS_DMI package.
*/
public class NWSRFSCarryoverGroup
{

/**
Identifier for the carryover group.
*/
private String _id = "";

/**
Forecast groups in the carryover group.
*/
private Vector	_forecast_groups = new Vector();

/**
Boolean used to indicate if the toString() method should
print extra information ("SEG: ") with the Operation ID.
The default is to not print the extra information.
*/
boolean _verbose = false;


/**
Construct a blank NWSRFSCarryoverGroup instance (no forecast groups).
*/
public NWSRFSCarryoverGroup ( String id )
{	if ( id != null ) {
		_id = id;
	}
}

/**
Add an NWSRFSForecastGroup to the NWSRFSCarryoverGroup.
@param fg NWSRFSForecastGroup to add.
*/
public void addForecastGroup ( NWSRFSForecastGroup fg )
{	_forecast_groups.addElement ( fg );
}

/**
Return the carryover group identifier.
*/
public String getID()
{	return _id;
}

/**
Return the forecast group at an index.
@param index Index of forecast group.
@return the forecast group at an index.
*/
public NWSRFSForecastGroup getForecastGroup ( int index )
{	return (NWSRFSForecastGroup)_forecast_groups.elementAt(index);
}

/**
Return the forecast group matching the forecast group identifier or null if
not found.
@param fgid Forecast group identifier.
@return the forecast group matching the identifier.
*/
public NWSRFSForecastGroup getForecastGroup ( String fgid )
{	int size = _forecast_groups.size();
	NWSRFSForecastGroup fg= null;
	for ( int i = 0; i < size; i++ ) {
		fg = (NWSRFSForecastGroup)_forecast_groups.elementAt(i);
		if ( fg.getID().equalsIgnoreCase(fgid) ) {
			return fg;
		}
	}
	fg = null;
	return fg;
}

/**
Return the forecast groups.  This is guaranteed to be non-null.
@return the list of forecast groups.
*/
public Vector getForecastGroups()
{	return _forecast_groups;
}

/**
Sets the toString() method so that it prints "CG: " before the
Operation ID whenever it is called.  The default is to not print
the extra information.
@param verbose - boolean to indicate if the toString() method should
add the extra "CG: " information in front of the Carryover group.
*/
public void setVerbose( boolean verbose ) {
        _verbose = verbose;
}


/**
Return a String representation of the carryover group (the ID).
*/
public String toString ()
{	
	if ( _verbose ) {
		return "CG: " + _id;
	}
	else 
		return _id;
}

}
