//-----------------------------------------------------------------------------
// NWSRFS - class to store the organization information about an NWSRFS
//		implementation
//-----------------------------------------------------------------------------
// History:
//
// 2001-12-13	Steven A. Malers, RTi	Initial version.
//
// 2001-03-11 	Morgan Sheedy, RTi	Added additional "parent" parameter 
//					to the constructors of:
//					NWSRFSForecastGroup, NWSRFSSegment,
//					NWSRFSOperation.  
//
//					Added the boolean "verbose" parameter to
//					the createNWSRFSFromSysmap(), which
//					triggers the addition of type 
//					identifiers in front of the IDs for
//					the NWSRFSCarryoverGroups, 
//					NWSRFSForecastGroups, NWSRFSSegments, 
//					and NWSRFSOperations.  
//
// 2001-03-14	AMS, RTi		Folded in method:
//					NWSRFSDMI_readTimeSeries from class
//					TSEngine.java.
//					NOT IMPLEMENTED YET!!!!!!!!!!!!
//
// 2002-10-14	AML, RTi		Updated package name 
// 					(from RTi.App.NWSRFSGUI_APP) to:
//					RTi.DMI.NWSRFS.	
//
// 2002-10-17	AML, RTi		Updated ProcessManager calls.
// 2002-10-17	AML, RTi		String passed to ProcessManager
//					to run nwsrfssh via batch mode
//					changed to an Array
//					
// 2002-10-17	AML, RTi		Added NWSRFSRatingCurves to the
//					sysmap tree under operations.
// 2003-07-23	SAM, RTi		* Change from TSDate to DateTime.
//					* Rename NWSRFSDMI_readTimeSeries() to
//					  readTimeSeries() and use DateTime.
// 2004-05-11	Scott Townsend, RTi	Modified all of this code to fit with
//					the new NWSRFS_DMI package framework.
// 2004-05-11	AML, RTi		Removed the "verbose" boolean.
// 2004-08-18	J. Thomas Saipenza, RTi	Started using set*() and get*() methods
//					for data member classes.
// 2004-10-14	SAT, RTi		Updated the code to check size of FG
//					Vector rather than the number the CG
//					says is there. Yanked it back out since
//					that yields spurious FG's!!!! Will try
//					to put in a more intelligent logic.
//-----------------------------------------------------------------------------
// EndHeader

package RTi.DMI.NWSRFS_DMI;

import java.lang.OutOfMemoryError;
import java.util.List;
import java.util.Vector;
import RTi.DMI.NWSRFS_DMI.NWSRFS_CarryoverGroup;
import RTi.DMI.NWSRFS_DMI.NWSRFS_DMI;
import RTi.DMI.NWSRFS_DMI.NWSRFS_ForecastGroup;
import RTi.TS.HourTS;
import RTi.TS.TS;
import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;

/**
The NWSRFS class stores the organizational information about an NWSRFS implementation.
*/
public class NWSRFS {

/**
Carryover groups in the NWSRFS (list of NWSRFS_CarryoverGroup).
*/
private List __carryover_groups = new Vector();

/**
The DMI object used in this NWSRFS instance.
*/
private NWSRFS_DMI __dmi;

/**
Construct a blank NWSRFS instance (no carryover groups).
*/
public NWSRFS ()
{
	__dmi = null;
}

/**
Add an NWSRFSCarryoverGroup to the NWSRFS.
@param cg NWSRFS_CarryoverGroup to add.
*/
public void addCarryoverGroup ( NWSRFS_CarryoverGroup cg )
{
	__carryover_groups.add ( cg );
}

/**
Construct an NWSRFS instance using the processed database binary files.
These objects will be the fully fleshed out objects with all of the
data coming directly from the PRD without an intermediate step. It will
fill the Carryover group vector list and build the tree.
@param ofs_fs5files a String holding the directory location of the processed
database files. If null or empty then the user must have an NWSRFS token
called <code>ofs_fs5files</code> set either in an APPS_DEFAULTS file or
the OS environment.
@return a NWSRFS instance.
@exception Exception if there is an error reading the time series.
*/
public static NWSRFS createNWSRFSFromPRD(String ofs_fs5files) throws Exception
{
	return createNWSRFSFromPRD(ofs_fs5files,false);
}

/**
Construct an NWSRFS instance using the processed database binary files.
These objects will be the fully fleshed out objects with all of the
data coming directly from the PRD without an intermediate step. It will
fill the Carryover group vector list and build the tree.
@param ofs_fs5files a String holding the directory location of the processed
database files. If null or empty then the user must have an NWSRFS token
called <code>ofs_fs5files</code> set either in an APPS_DEFAULTS file or
the OS environment.
@param deepRead a boolean specifying whether or not just header or id's are read 
from the Segment, Operations, and TimeSeries objects. If true read all data.
@return a NWSRFS instance.
@exception Exception if there is an error reading the time series.
*/
public static NWSRFS createNWSRFSFromPRD(String ofs_fs5files, boolean deepRead) 
throws Exception
{
	// Local variables
	String routine = "NWSRFS.createNWSRFSFromPRD";
	int cgIndex, fgIndex, fgSize=0;
	NWSRFS nwsrfs = new NWSRFS();
	NWSRFS_CarryoverGroup cg  = null;
	NWSRFS_ForecastGroup  fg  = null;
	List cgIDs;
	List fgIDs;

	try
	{
		// Print OFS_FS5FILES value for this NWSRFS.
		Message.printStatus(10,routine,"ofs_fs5files: " + ofs_fs5files);
		
		NWSRFS_DMI dmi = nwsrfs.getDmiInternal ( ofs_fs5files );

		// Get the list of carryover group identifiers
		cgIDs = dmi.readCarryoverGroupList();
		int cg_size = cgIDs.size();

		// Print status on number of Carryover groups available for this NWSRFS.
		Message.printStatus(10,routine,"Number of Carryover groups: " + cg_size );

		// Now loop through the cgID's and build the tree!
		for(cgIndex=0;cgIndex<cg_size;cgIndex++)
		{
			try
			{
				// Get the full carryover group
				cg = dmi.readCarryoverGroup((String)cgIDs.get(cgIndex), deepRead);

				// Get forecast groups
				fgIDs = new Vector();
				fgIDs = cg.getForecastGroupIDs();
				fgSize = cg.getNFG();
				if(fgSize <= 0) {
					fgSize = fgIDs.size();
				}

				// If we really do not have any FGs even though 
				// the CG says there are FGs just continue to next CG.
				if(fgIDs.size() == 0) {
					continue;
				}
				
				for(fgIndex=0;fgIndex<fgSize;fgIndex++)
				{
					try
					{
					// Check to see if the FG ID is "OBSOLETE". If so skip.
					if(fgIDs.get(fgIndex) == null || ((String)fgIDs.get(fgIndex)).equalsIgnoreCase("OBSOLETE")) {
						continue;
					}
	
					// Get the complete forecast group
					fg = dmi.readForecastGroup((String)fgIDs.get(fgIndex),deepRead);

					// Set the CG to be the FG parent
					fg.setCarryoverGroup(cg);

					// Add the Forecast group to the Carryover Group object
					cg.addForecastGroup(fg);

					}
					catch(OutOfMemoryError OOMe)
					{
						// Create a RunTime object to call GC
						Message.printWarning(2,routine,
						"NWSRFS adding Forecast Groups ran out of memory (calling Garbage Collector):"+
						(Runtime.getRuntime()).freeMemory());
						Runtime.getRuntime().gc();

						// Add the incomplete Forecast group to the Carryover Group object
						cg.addForecastGroup(fg);
					}
				}

				// Add the carryover group to the NWSRFS object
				nwsrfs.addCarryoverGroup(cg);
			}
			catch(OutOfMemoryError OOMe)
			{
				// Create a RunTime object to call GC
				Message.printWarning(2,routine,"NWSRFS adding "+
				"Carrover Groups ran out of memory (calling Garbage Collector):"+
				(Runtime.getRuntime()).freeMemory());
				Runtime.getRuntime().gc();
			}
		}
	} 
	catch(Exception e)
	{
		Message.printWarning( 2, routine, e );
		throw e;
	}

	// Return the NWSRFS instance
	return nwsrfs;
}

/**
Return the carryover group at an index.
@param index Index of carryover group.
@return the carryover group at an index.
*/
public NWSRFS_CarryoverGroup getCarryoverGroup ( int index )
{	
	return (NWSRFS_CarryoverGroup)__carryover_groups.get(index);
}

/**
Return the carryover group matching the carryover group identifier or null if not found.
@param cgid Carryover group identifier.
@return the carryover group matching the identifier.
*/
public NWSRFS_CarryoverGroup getCarryoverGroup ( String cgid )
{	
	int size = __carryover_groups.size();
	NWSRFS_CarryoverGroup cg= null;
	for ( int i = 0; i < size; i++ ) {
		cg = (NWSRFS_CarryoverGroup)__carryover_groups.get(i);
		if ( cg.getCGID().equalsIgnoreCase(cgid) ) {
			return cg;
		}
	}
	cg = null;
	return cg;
}

/**
Return the carryover groups.  This is guaranteed to be non-null.
@return the list of carryover groups as NWSRFS_CarryoverGroup.
*/
public List getCarryoverGroups()
{
	return __carryover_groups;
}

/**
Return the NWSRFS_DMI object.  This is guaranteed to be non-null.
@return the DMI object.
*/
public NWSRFS_DMI getDMI()
{
	return __dmi;
}

/**
Get the internal NWSRFS_DMI that can be used for database queries.  If null and
the path to the ofs_fs5files is specified, create a new DMI, save it, and pass back
for future calls.
@param ofs_fs5files a String holding the directory location of the processed
database files. If null or empty then the user must have an NWSRFS token
called <code>ofs_fs5files</code> set either in an APPS_DEFAULTS file or
the OS environment.
*/
private NWSRFS_DMI getDmiInternal ( String ofs_fs5files )
throws Exception
{
	if(__dmi == null)
	{
		// Check to see if the argument ofs_fs5files is null or empty
		if(ofs_fs5files == null || ofs_fs5files.equals(""))
		{
			__dmi = new NWSRFS_DMI();
		}
		else
		{
			__dmi = new NWSRFS_DMI(ofs_fs5files);
		}
	}
	// Else use the previous __dmi instance
	return __dmi;
}

/**
Get the local time zone used by the system.  This corresponds to the USERPARM.time(3) value.
For example, carryover dates are always stored at 12 Z but may be 5 MST, where MST is the
local time zone used by the system.
@return The local time zone used by the system.
*/
public String getLocalTimeZone ()
throws Exception
{
	NWSRFS_DMI dmi = getDmiInternal(null);
	NWSRFS_USERPARM u = dmi.readUSERPARM();
	return u.getTime3();
}

/**
Return the number of Carryover Groups in this NWSRFS object.
@return the number of Carryover Groups defined on this NWSRFS object.
*/
public int getNumberOfCarryoverGroups()
{
	return __carryover_groups.size();
}

/**
Time series is generated by calling the readTimeSeries method from the NWSRFS_DMI. 
@param tsident_string Time series identifier string.
@param req_date1 Requested start date for data.
@param req_date2 Requested end date for data.
@param req_units Requested data units.
@param deepRead Indicates whether data should be read (true) or only header
information (false).
@return a time series matching the identifier.
@exception Exception if there is an error reading the time series.
*/
public TS readTimeSeries ( String tsident_string,
				DateTime req_date1,
				DateTime req_date2,
				String req_units,
				boolean deepRead) throws Exception
{
	// Return a call to the overloaded method with ofs_fs5files String as null.
	// This means that the location to the fs5files must be set in the environment.
	return readTimeSeries( tsident_string, req_date1, req_date2, req_units, deepRead, null);
}

/**
Time series is generated by calling the readTimeSeries method from the NWSRFS_DMI. 
@param tsident_string Time series identifier string.
@param req_date1 Requested start date for data.
@param req_date2 Requested end date for data.
@param req_units Requested data units.
@param deepRead Indicates whether data should be read (true) or only header
information (false).
@param ofs_fs5files a String holding the directory location of the processed
database files. If null or empty then the user must have an NWSRFS token
called <code>ofs_fs5files</code> set either in an APPS_DEFAULTS file or
the OS environment.
@return a time series matching the identifier.
@exception Exception if there is an error reading the time series.
*/
public TS readTimeSeries ( String tsident_string,
					DateTime req_date1,
					DateTime req_date2,
					String req_units,
					boolean deepRead,
					String ofs_fs5files) throws Exception
{	
	NWSRFS_DMI dmi = getDmiInternal ( ofs_fs5files );
	
	// Call the readTimeSeries method in the DMI.
	TS ts = (HourTS)dmi.readTimeSeries(tsident_string, req_date1, req_date2, req_units, deepRead);
	return ts;
}

} // End NWSRFS
