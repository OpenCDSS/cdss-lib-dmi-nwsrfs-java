////////////////////////////////////////////////////////////////////////////
// NWSRFSStation.java - Class used to define NWSRFS Station objects.
////////////////////////////////////////////////////////////////////////////
/*
History
 15 Mar, 2002	Morgan Sheedy, RTi	Initial Implementation

// 2002-10-14   AML, RTi                Updated package name 
//                                      (from RTi.App.NWSRFSGUI_APP) to:
//                                      RTi.DMI.NWSRFS.   
*/
////////////////////////////////////////////////////////////////////////////

package RTi.DMI.NWSRFS;

import java.util.Vector;
/**
@deprecated This class has been deprecated since the functionality has been replaced
by the NWSRFS_DMI package.
*/
public class NWSRFSStation {

//Station ID
private String _id = "";

boolean _verbose = false;

/**
Constructor.
*/
public NWSRFSStation( String id ) {
	if ( id != null ) {
		_id = id;
	}
} //end constructor

/**
Returns ID for station.
*/
public String getID() {
	return _id;
}

/**
Sets the "toString()" method to be verbose.
*/
public void setVerbose( boolean verbose ) {
	_verbose = verbose;
}

/**
toString method prints name of ID.
*/
public String toString() {
	if ( _verbose ) {
		return "STN: " + _id;
	}
	else
		return _id;
}

}//end NWSRFSStation class
