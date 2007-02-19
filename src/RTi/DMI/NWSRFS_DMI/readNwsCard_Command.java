//------------------------------------------------------------------------------
// readNwsCard_Command - handle the readNwsCard() command
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2005-05-11	Luiz Teixeira, RTi	Initial version.
// 2005-05-16	Luiz Teixeira, RTi	Clean up and documentation.
// 2005-05-19	SAM, RTi		Move from TSTool package to TS.
// 2005-12-06	J. Thomas Sapienza, RTi	Added Read24HourAsDay parameter.
// 2005-12-12	JTS, RTi		readTimeSeries() call now passes in
//					a control PropList.
// 2006-01-04	JTS, RTi		Corrected many problems after review by
//					SAM.
// 2006-01-18	JTS, RTi		Moved from RTi.TS package.
// 2007-02-16	SAM, RTi		Use new CommandProcessor interface.
//					Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------

package RTi.DMI.NWSRFS_DMI;

import javax.swing.JFrame;
import java.util.Vector;
import java.io.File;

import RTi.TS.TS;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.IO.SkeletonCommand;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
<p>
This class initializes, checks, and runs the TS Alias and non-TS Alias 
readNwsCard() commands.
</p>
<p>The CommandProcessor must return the following properties:  
TSResultsList and WorkingDir.
</p>
*/
public class readNwsCard_Command 
extends SkeletonCommand
implements Command {

protected static final String
	_FALSE = "False",
	_TRUE = "True";

/**
Private data members shared between the checkCommandParameter() and the 
runCommand() methods (prevent code duplication parsing dateTime strings).  
*/
private DateTime __InputStart = null;
private DateTime __InputEnd   = null; 

private String __Alias = null;

/**
Constructor.
*/
public readNwsCard_Command ()
{
	super();
	setCommandName ( "readNwsCard" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor
dialogs).
*/
public void checkCommandParameters ( PropList parameters,
				     String command_tag,
				     int warning_level )
throws InvalidCommandParameterException {
	String warning = "";
	
	// Get the property values. 
	String InputFile = parameters.getValue("InputFile");
	String NewUnits  = parameters.getValue("NewUnits");
	String InputStart = parameters.getValue("InputStart");
	String InputEnd   = parameters.getValue("InputEnd");
	String Read24HourAsDay = parameters.getValue("Read24HourAsDay");
	String Alias = parameters.getValue("Alias");

	// Input file
	if ( InputFile != null && InputFile.length() != 0 ) {
		InputFile = IOUtil.getPathUsingWorkingDir(InputFile);
		File f = new File(InputFile);
		if (!f.exists()) {
			warning += "\nThe NWS Card File \"" + InputFile
				+ "\" does not exist.";
		}
		/*
		String working_dir = (String)
			_processor.getPropContents ( "WorkingDir" );
		try {
		  	String adjusted_path = IOUtil.adjustPath (
				 working_dir, InputFile );	 
					 
			File f  = new File ( adjusted_path );
			if ( !f.exists() ) {
				warning += "\nThe NWS Card file \""
				+ adjusted_path
				+ "\' does not exist.";
			}
			f  = null;
		}
		catch ( Exception e ) {
			warning += "\nThe working directory:\n"
				+ "    \"" + working_dir
				+ "\"\ncannot be adjusted using:\n"
				+ "    \"" + InputFile + "\".";
		}
		*/
	} 
	else {
		warning += "\nThe Input File must be specified.";
	}

	if (Alias != null && !Alias.equals("")) {
		if (Alias.indexOf(" ") > -1) {
			// do not allow spaces in the alias
			warning += "\nThe Alias value cannot contain any "
				+ "space.";
		}
	}

	if ( NewUnits != null ) {
		// Will check at run time
	}

	boolean read24HourAsDay = false;

	// Read24HourAsDay
	if ((Read24HourAsDay != null) && !Read24HourAsDay.equals("")) {
		if (Read24HourAsDay.equalsIgnoreCase("true")) {
			// valid entry
			read24HourAsDay = true;
		}
		else if (Read24HourAsDay.equalsIgnoreCase("false")
		    || Read24HourAsDay.trim().equals("")) {
		    	// valid entry
		    	read24HourAsDay = false;
		}
		else {
			// invalid value -- will default to false, but report
			// a warning.
			warning += "\nThe value to specify whether to convert "
				+ "24 Hour data to Daily should be blank, or "
				+ "one of \"true\" or \"false\", not \""
				+ Read24HourAsDay + "\"";
		}
	}

	// InputStart
	if ((InputStart != null) && !InputStart.equals("")) {
		try {
			__InputStart = DateTime.parse(InputStart);
		} 
		catch (Exception e) {
			warning += "\nThe input start date/time \""
				+ InputStart + "\" is not valid.";
		}
		
		if (__InputStart != null) {
			if (__InputStart.getPrecision() 
			    != DateTime.PRECISION_HOUR) {
				warning += "\nThe input start date/time \""
					+ InputStart
					+ "\" precision is not hour.";
			}		

			// When reading 24 hour as day, 24 is a valid hour
			// for the current day (because of how the NWS does
			// things), but RTi's DateTime class automatically 
			// parses hour 24 as being hour 0 of the next day, so
			// decrement the __InputStart day by 1 to account for
			// this.			
			if (read24HourAsDay) {
				if (InputStart.endsWith(" 24")) {
					__InputStart.addDay(-1);
				}
			}
			/*
			if (!read24HourAsDay && __InputStart.getPrecision() 
			    != DateTime.PRECISION_HOUR) {
				warning += "\nThe input start date/time \""
					+ InputStart
					+ "\" precision is not hour.";
			}
			else if (read24HourAsDay && __InputStart.getPrecision() 
			    != DateTime.PRECISION_DAY) {
				warning += "\nThe input start date/time \""
					+ InputStart
					+ "\" precision is not day.";
			}
			*/
		}
	}

	// InputEnd
	if ((InputEnd != null) && !InputEnd.equals("")) {
		try {
			__InputEnd = DateTime.parse(InputEnd);
		} 
		catch (Exception e) {
			warning += "\nThe input end date/time \""
				+ InputEnd + "\" is not valid.";
		}
		
		if (__InputEnd != null) {
			if (__InputEnd.getPrecision() 
			    != DateTime.PRECISION_HOUR) {
				warning += "\nThe input end date/time \""
					+ InputStart
					+ "\" precision is not hour.";
			}		

			// When reading 24 hour as day, 24 is a valid hour
			// for the current day (because of how the NWS does
			// things), but RTi's DateTime class automatically 
			// parses hour 24 as being hour 0 of the next day, so
			// decrement the __InputEnd day by 1 to account for
			// this.
			if (read24HourAsDay) {
				if (InputEnd.endsWith(" 24")) {
					__InputEnd.addDay(-1);
				}
			}			
/*		
			if (!read24HourAsDay && __InputEnd.getPrecision() 
			    != DateTime.PRECISION_HOUR) {
				warning += "\nThe input end date/time \""
					+ InputEnd
					+ "\" precision is not hour.";
			}
			else if (read24HourAsDay && __InputEnd.getPrecision() 
			    != DateTime.PRECISION_DAY) {
				warning += "\nThe input end date/time \""
					+ InputEnd
					+ "\" precision is not day.";
			}			
*/			
		}
	}

	// Make sure __InputStart precedes __InputEnd
	if ( __InputStart != null && __InputEnd != null ) {
		if ( __InputStart.greaterThanOrEqualTo( __InputEnd ) ) {
			warning += "\n InputStart (" + __InputStart 
				+ ") should be less than InputEnd (" 
				+ __InputEnd + ").";
		}
	}

	// Throw an InvalidCommandParameterException in case of errors.
	if ( warning.length() > 0 ) {		
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
				command_tag, warning_level ),
			warning );
		throw new InvalidCommandParameterException ( warning );
	}
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed).
*/
public boolean editCommand ( JFrame parent )
{	
	// The command will be modified if changed...
	return ( new readNwsCard_JDialog ( parent, this ) ).ok();
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{
	__InputStart = null;
	__InputEnd   = null;
	super.finalize();
}

/**
Parse the command string into a PropList of parameters.
@param command A string command to parse.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2).
@exception InvalidCommandSyntaxException if during parsing the command is
determined to have invalid syntax.
syntax of the command are bad.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void parseCommand(String command, String command_tag, int warning_level)
throws InvalidCommandSyntaxException, InvalidCommandParameterException {
	// The old format of parsing is still supported for the 
	//	"TS alias = readNwsCard(.....)
	// version of the command.  In old format of the TS Alias version of 
	// the command, parameter order is important. In the new format, 
	// parameters are specified as "InputStart=....,InputEnd=...", and so
	// on.  If an equals sign ("=") is found after the parenthesis that
	// signals the start of the function arguments, then it is the 
	// new format.
	
	int index = command.indexOf("(");
	String str = command.substring(index);
	index = str.indexOf("=");

	// if no = is found, then this is the old format of parsing (see
	// above).
	if (index < 0) {
		// Reparse to strip quotes from file name...
		Vector tokens = StringUtil.breakStringList ( command, "=(,)",
				StringUtil.DELIM_ALLOW_STRINGS);
		__Alias = ((String)tokens.elementAt(0)).trim();
		index = __Alias.indexOf(" ");
		if (index > -1) {
			__Alias = __Alias.substring(index).trim();
		}
		String InputFile = ((String)tokens.elementAt(2)).trim();
		String NewUnits = ((String)tokens.elementAt(3)).trim();
		String InputStart = ((String)tokens.elementAt(4)).trim();
		String InputEnd = ((String)tokens.elementAt(5)).trim();
		_parameters = new PropList("Command Parameters");
		_parameters.set("InputFile", InputFile);
		if (!NewUnits.equals("*")) {
			_parameters.set("NewUnits", NewUnits);
		}
		if (!InputStart.equals("*")) {
			_parameters.set("InputStart", InputStart);
		}
		if (!InputEnd.equals("*")) {
			_parameters.set("InputEnd", InputEnd);
		}
		if (!__Alias.trim().equals("")) {
			_parameters.set("Alias", __Alias.trim());
		}
		_parameters.set("Read24HourAsDay", "false");
	}
	else {
		// This is the new format of parsing, where parameters are
		// specified as "InputFilter=", etc.
		String routine = "readNwsCard_Command.parseCommand", message;
	
		int warning_count = 0;

		Vector tokens = StringUtil.breakStringList ( command,
			"()", StringUtil.DELIM_SKIP_BLANKS );
		if ( (tokens == null) || tokens.size() < 2 ) {
			// Must have at least the command name and the InputFile
			message = "Syntax error in \"" + command +
				"\".  Not enough tokens.";
			Message.printWarning ( warning_level,
				MessageUtil.formatMessageTag(
					command_tag,++warning_count),
				routine, message);
			throw new InvalidCommandSyntaxException ( message );
		}
	
		// Get the input needed to process the file...
		try {
			_parameters = PropList.parse ( Prop.SET_FROM_PERSISTENT,
				(String)tokens.elementAt(1), routine, "," );
		}
		catch ( Exception e ) {
			message = "Syntax error in \"" + command +
				"\".  Not enough tokens.";
			Message.printWarning ( warning_level,
				MessageUtil.formatMessageTag(
					command_tag,++warning_count),
				routine, message);
			throw new InvalidCommandSyntaxException ( message );
		}

		// The following is for backwards compatability with old 
		// commands files.
		if (_parameters.getValue("InputStart") == null) {
			_parameters.set("InputStart", _parameters.getValue(
				"ReadStart"));
		}
		if (_parameters.getValue("InputEnd") == null) {
			_parameters.set("InputEnd", _parameters.getValue(
				"ReadEnd"));
		}

		if (StringUtil.startsWithIgnoreCase(command, "TS ")) {
			// there is an alias specified.  Extract the alias
			// from the full command.
			str = command.substring(3);	
			index = str.indexOf("=");
			int index2 = str.indexOf("(");
			if (index2 < index) {
				// no alias specified -- badly-formed command
				__Alias = "Invalid_Alias";
				message = "No alias was specified, although "
					+ "the command started with \"TS ...\"";
				Message.printWarning(warning_level, 
					MessageUtil.formatMessageTag(
					command_tag, ++warning_count),
					routine, message);
				throw new InvalidCommandSyntaxException(
					message);
			}

			__Alias = str.substring(0, index);
		}
		else {
			__Alias = null;
		}

		if (__Alias != null) {
			_parameters.set("Alias", __Alias.trim());
		}
	}
}

/**
Run the command:
<pre>
readNWSCard (InputFile="x",InputStart="x",InputEnd="x",Read24HourAsDay="x",
NewUnits="x")
</pre>
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2).
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could
not produce output).
@exception InvalidCommandParameterException Thrown if parameter one or more
parameter values are invalid.
*/
public void runCommand ( String command_tag,
			 int warning_level )
throws InvalidCommandParameterException,
       CommandWarningException,
       CommandException
{
	String routine = "readNwsCard_Command.runCommand", message;

	int warning_count = 0;
	Vector TSList     = null;	// Keep the list of time series	

	// Get the command properties not already stored as members.
	String InputFile = _parameters.getValue("InputFile");
	String NewUnits = _parameters.getValue("NewUnits");
	// TODO SAM 2007-02-18 Need to enable InputStart and InputEnd handling.
	//String InputStart = _parameters.getValue("InputStart");
	//String InputEnd = _parameters.getValue("InputEnd");
	String Read24HourAsDay = _parameters.getValue("Read24HourAsDay");
	String Alias = _parameters.getValue("Alias");

	// Set the properties for NWSCardTS.readTimeSeries().
	PropList props = new PropList("NWSCardTS.readTimeSeries");
	props.set("Read24HourAsDay=" + Read24HourAsDay);

	// Read the NWS Card file.
	int TSCount = 0;
	try {
		// InputFile is adjusted in the readTimeSeriesList using the
		// method IOUtil.getPathUsingWorkingDir().
		// REVISIT [LT 2005-05-17] Ask SAM if the above adjust is the
		// same as the adjust done here.  The code for the adjust done
		// here is much elaborated then the code in method
		// getPathUsingWorkingDir
		TSList = NWSCardTS.readTimeSeriesList (
			// REVISIT [LT 2005-05-17] May add the TSID parameter 
			//	(1st parameter here) in the future.
			(TS) null,    		// Currently not used.
			InputFile, 		// String 	fname
			__InputStart,		// DateTime 	date1
			__InputEnd, 		// DateTime 	date2
			NewUnits,		// String 	units
			true,			// boolean 	read_data
			props);			// whether to read 24 hour 
						// as day.
			
		if ( TSList != null ) {
			TSCount = TSList.size();
			message = "Read \"" + TSCount
				+ "\" time series from \""
				+ InputFile + "\"";
			Message.printStatus ( 2, routine, message );
			TS ts = null;
			for (int i = 0; i < TSCount; i++) {
				ts = (TS)TSList.elementAt(i);
				ts.setAlias(Alias);
			}
		}
	} 
	catch ( Exception e ) {
		message = "Error reading NWS Card File. \""
			+ InputFile + "\"";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
				command_tag, ++warning_count ),
			routine, message );
		Message.printWarning ( 3, routine, e );
		throw new CommandException ( message );
	}

	// Add the new time series to the TSResultsList.
	if ( TSList != null && TSCount > 0 ) {
		// Get the list of time series currently in the command
		// processor.
		Vector TSResultsList_Vector = null;
		try { Object o = _processor.getPropContents( "TSResultsList" );
				TSResultsList_Vector = (Vector)o;
		}
		catch ( Exception e ){
			message = "Cannot get time series list to add new time series.  Creating new list.";
			Message.printDebug ( 10,routine,message);
		}
		if ( TSResultsList_Vector == null ) {
			// Not a warning because this can be the first command.
			TSResultsList_Vector = new Vector();
		}
		// Add to the time series...
		for ( int i=0; i<TSCount; i++ ) {
			TSResultsList_Vector.addElement ( TSList.elementAt(i) );
		}
		// Now set back in the processor...
		try {	_processor.setPropContents ( "TSResultsList", TSResultsList_Vector );
		}
		catch ( Exception e ){
			message = "Cannot set updated time series list.  Skipping.";
			Message.printWarning ( warning_level,
				MessageUtil.formatMessageTag(
				command_tag, ++warning_count),
				routine,message);
		}
	} 
	else {
		message = "Read zero time series from the NWS Card file \"" 
			+ InputFile + "\"";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
				command_tag, ++warning_count ),
			routine, message );
	}

	// Throw CommandWarningException in case of problems.
	if ( warning_count > 0 ) {
		message = "There were " + warning_count +
			" warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
				command_tag, ++warning_count ),
			routine, message );
		throw new CommandWarningException ( message );
	}
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{
	if ( props == null ) {
		return getCommandName() + "()";
	}

	String Alias = props.getValue("Alias");
	String InputFile = props.getValue("InputFile" );
	String NewUnits = props.getValue("NewUnits");
	String InputStart = props.getValue("InputStart");
	String InputEnd = props.getValue("InputEnd");
	String Read24HourAsDay = props.getValue("Read24HourAsDay");

	StringBuffer b = new StringBuffer ();

	// Input File
	if ((InputFile != null) && (InputFile.length() > 0)) {
		b.append("InputFile=\"" + InputFile + "\"");
	}

	// New Units
	if ((NewUnits != null) && (NewUnits.length() > 0)) {
		if (b.length() > 0) {
			b.append(",");
		}
		b.append("NewUnits=\"" + NewUnits + "\"");
	}

	// Input Start
	if ((InputStart != null) && (InputStart.length() > 0)) {
		if (b.length() > 0) {
			b.append(",");
		}
		b.append("InputStart=\"" + InputStart + "\"");
	}

	// Input End
	if ((InputEnd != null) && (InputEnd.length() > 0)) {
		if (b.length() > 0) {
			b.append(",");
		}
		b.append("InputEnd=\"" + InputEnd + "\"");
	}

	if (Read24HourAsDay != null && Read24HourAsDay.length() > 0) {
		if (b.length() > 0) {
			b.append(",");
		}
		b.append("Read24HourAsDay=" + Read24HourAsDay + "");
	}

	if (Alias != null && Alias.length() > 0) {
		Alias = "TS " + Alias + " = ";
	}
	else {
		Alias = "";
	}

	return Alias + getCommandName() + "(" + b.toString() + ")";
}

} // end readNwsCard_Command
