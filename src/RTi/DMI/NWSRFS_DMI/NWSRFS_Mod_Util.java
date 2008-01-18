package RTi.DMI.NWSRFS_DMI;

import java.util.List;
import java.util.Vector;
import RTi.TS.HourTS;
import RTi.TS.TSIdent;
import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;

/**
Static utility functions for NWSFS Mod classes
*/
public abstract class NWSRFS_Mod_Util
{

/**
Convert TSCHNG mods into FMAP mods.
@param mods A list of mods, not restricted to any type.
@param lastobs_DateTime The date/time of the last observation.  
       Any values beyond this will be considered future data and allowed
       for FMAP.  The date should be in Z time.
@return a List of FMAP mods resulting from the conversion.
*/
public static List convertTSCHNG_MAP_ModsToFMAPMods ( List mods, DateTime lastobs_DateTime )
{	
  String routine = "NWSRFS_Mod_Util.convertTSCHNG_MAP_ModsToFMAPMods";
	List FMAP_mods = new Vector();
	if ( mods == null ) {
		return FMAP_mods;
	}
	int size = mods.size();
	NWSRFS_Mod mod = null;
	NWSRFS_Mod_TSCHNG mod_TSCHNG = null;
	for ( int i = 0; i < size; i++ ) {
		mod = (NWSRFS_Mod)mods.get(i);
		if ( mod instanceof NWSRFS_Mod_TSCHNG ) {
			// Need to evaluate converting to an FMAP mod.  Only do so if the TSCHNG mod dates
			// extend beyond the last observation.
			mod_TSCHNG = (NWSRFS_Mod_TSCHNG)mod;
			HourTS ts = mod_TSCHNG.getTS();
			
			DateTime end = mod_TSCHNG.getTS().getDate2();
			
			if ( mod_TSCHNG.getTsDataType().equalsIgnoreCase("MAP") &&
					end.greaterThan(lastobs_DateTime)) {
				// Have some future data.  Create a new FMAP mod and transfer values
				NWSRFS_Mod_FMAP mod_FMAP = new NWSRFS_Mod_FMAP();
				mod_FMAP.setSegment( mod_TSCHNG.getSegment() );
				mod_FMAP.setTsid( mod_TSCHNG.getTsid() );
				int tsint = mod_TSCHNG.getTsInterval();
				mod_FMAP.setTsInterval( tsint );
				mod_FMAP.setStart(mod_TSCHNG.getStart());
				
				// Start by setting the date equal to the start
				DateTime date = new DateTime (ts.getDate1());
				// Iterate forward until the date is greater than the observed
				int count = 0;
				while ( date.lessThanOrEqualTo(lastobs_DateTime) ) {
					date.addHour ( tsint );
				}
				// Allocate the data for this mod
				HourTS ts2 = new HourTS ();
				try {
					ts2.setIdentifier ( new TSIdent(mod.getTsid(),"NWSRFS",
							mod.getTsDataType(),""+tsint+"Hour","") );
				}
				catch ( Exception e ) {
					Message.printWarning ( 3, routine, "Error setting identifier on new FMAP time series.");
					Message.printWarning ( 3, routine, e );
					continue;
				}
				ts2.setDate1( date );
				ts2.setDate2( ts.getDate2() );
				ts.allocateDataSpace();
				// Loop through the data points and set the data
				date = new DateTime ( ts2.getDate2() );
				end = ts2.getDate2();
				for ( ; date.lessThanOrEqualTo(end); date.addHour(tsint) ) {
					ts.setDataValue( date, ts.getDataValue(date));
				}
				mod_FMAP.setTS(ts2);
				FMAP_mods.add(mod_FMAP);
			}
		}
	}
	return FMAP_mods;
}

/**
Write TSCHNG MAT into TSEDIT control file.


@param mods A list of mods, not restricted to any type.
@param lastobs_DateTime The date/time of the last observation.
       Any values beyond this will be considered future data and allowed
        for FMAP.  The date should be in Z time.
@param outfile output file to write
@return 
*/
public static void writeTSCHNG_MAT_ModsToTSEDIT ( List mods, DateTime lastobs_DateTime, String outfile )
{	
  /*
  String routine = "NWSRFS_Mod_Util.convertTSCHNG_MAP_ModsToFMAPMods";
	List FMAP_mods = new Vector();
	
	if ( mods == null ) {
		return FMAP_mods;
	}
	int size = mods.size();
	NWSRFS_Mod mod = null;
	NWSRFS_Mod_TSCHNG mod_TSCHNG = null;
	
	for ( int i = 0; i < size; i++ ) {
		mod = (NWSRFS_Mod)mods.get(i);
		if ( mod instanceof NWSRFS_Mod_TSCHNG ) {
			// Need to evaluate converting to an FMAP mod.  Only do so if the TSCHNG mod dates
			// extend beyond the last observation.
			mod_TSCHNG = (NWSRFS_Mod_TSCHNG)mod;
			HourTS ts = mod_TSCHNG.getTS();
			DateTime end = mod_TSCHNG.getTS().getDate2();
			if ( mod_TSCHNG.getTstype().equalsIgnoreCase("MAP") &&
					end.greaterThan(lastobs_DateTime)) {
				// Have some future data.  Create a new FMAP mod and transfer values
				NWSRFS_Mod_FMAP mod_FMAP = new NWSRFS_Mod_FMAP();
				mod_FMAP.setSegment( mod_TSCHNG.getSegment() );
				mod_FMAP.setTsid( mod_TSCHNG.getTsid() );
				int tsint = mod_TSCHNG.getTsint();
				mod_FMAP.setTsint( tsint );
				// Start by setting the date equal to the start
				DateTime date = new DateTime (ts.getDate1());
				// Iterate forward until the date is greater than the observed
				int count = 0;
				while ( date.lessThanOrEqualTo(lastobs_DateTime) ) {
					date.addHour ( tsint );
				}
				// Allocate the data for this mod
				HourTS ts2 = new HourTS ();
				try {
					ts2.setIdentifier ( new TSIdent(mod.getTsid(),"NWSRFS",
							mod.getTstype(),""+tsint+"Hour","") );
				}
				catch ( Exception e ) {
					Message.printWarning ( 3, routine, "Error setting identifier on new FMAP time series.");
					Message.printWarning ( 3, routine, e );
					continue;
				}
				ts2.setDate1( date );
				ts2.setDate2( ts.getDate2() );
				ts.allocateDataSpace();
				// Loop through the data points and set the data
				date = new DateTime ( ts2.getDate2() );
				end = ts2.getDate2();
				for ( ; date.lessThanOrEqualTo(end); date.addHour(tsint) ) {
					ts.setDataValue( date, ts.getDataValue(date));
				}
				mod_FMAP.setTS(ts2);
			}
		}
	}
	return FMAP_mods;
	*/
}

}
