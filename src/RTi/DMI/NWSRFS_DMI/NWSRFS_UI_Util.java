package RTi.DMI.NWSRFS_DMI;

import javax.swing.JFrame;

import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.Message.Message;

/**
Useful user interface utilities for NWSRFS.
*/
public class NWSRFS_UI_Util
{
	
/**
Show important Apps defaults tokens that are being used in a simple response Dialog with
an OK button.  This display is useful for troubleshooting the configuration of NWSRFS.
@param parent Parent JFrame that is displaying this view.
*/
public static void viewAppsDefaults ( JFrame parent )
{
	String routine = "NWSRFS_UI_Util.viewAppsDefaults";

	StringBuffer output_strings = new StringBuffer();
	String nl = System.getProperty ( "line.separator" );
	output_strings.append (
			"Apps defaults are configuration properties used by NWSRFS and are defined as" + nl +
			"environment variables and properties in the sequence of files APPS_* shown below." + nl + nl );

	String [][] apps_defaults_to_view = new String [][] {
			{ "APPS_DEFAULTS_USER", "User configuration properties" },
			{ "APPS_DEFAULTS_PROG", "Program configuration properties" },
			{ "APPS_DEFAULTS_SITE", "Site configuration properties" },
			{ "APPS_DEFAULTS", "National/default configuration properties" },
			{ "geo_data", "Location of spatial data." },
			{ "ofs_fs5files", "Location of binary NWSRFS database files" },
			{ "ofs_griddb_dir", "" },
			{ "ofs_input", "Location of operational forecast system input files" },
			{ "ofs_level", "Folder name for implementation's files (under other folders)." },
			{ "ofs_mods_dir", "Location of runtime modification files" },
			{ "ofs_reorder_dir", "" },
			{ "rfs_dir", "Location of forecast system [top level]" },
			{ "rfs_sys_dir", "Location of system files [data types, etc.]" }
	};

	String value;
	for ( int i = 0; i < apps_defaults_to_view.length; i++ ) {
		value = NWSRFS_Util.getAppsDefaults ( apps_defaults_to_view[i][0] );
		if ( value == null ) {
			Message.printWarning(3, routine,
				"Null value returned for Apps Defaults token \"" + apps_defaults_to_view[i][0] +
				"\".  Verify the values of the APPS_DEFAULTS_* file contents and environment variables.");
				if ( apps_defaults_to_view[i][1].length() > 0 ) {
					// Have a description...
					output_strings.append( apps_defaults_to_view[i][0] + " (" +
						apps_defaults_to_view[i][1]+ ") = Not Set" + nl);
				}
				else {
					// No description...
					output_strings.append( apps_defaults_to_view[i][0] + " = Not Set" + nl);
				}
		}
		else {
			// Blanks will show up here...
			if ( apps_defaults_to_view[i][1].length() > 0 ) {
				// Have a description...
				output_strings.append( apps_defaults_to_view[i][0] + " (" +
						apps_defaults_to_view[i][1]+ ") = " + value + nl);
			}
			else {
				// No description.
				output_strings.append( apps_defaults_to_view[i][0] + " = " + value + nl);
			}
		}
	}

	new ResponseJDialog(parent,"Current Apps Defaults", output_strings.toString(), ResponseJDialog.OK );
}

}
