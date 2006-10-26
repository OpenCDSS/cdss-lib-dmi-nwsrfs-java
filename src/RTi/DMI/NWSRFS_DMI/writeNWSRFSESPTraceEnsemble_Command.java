//------------------------------------------------------------------------------
// writeNWSRFSESPTraceEnsemble_Command - handle the 
//	writeNWSRFSESPTraceEnsemble() command
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2006-01-17	J. Thomas Sapienza, RTi	Initial version.
//------------------------------------------------------------------------------

package RTi.DMI.NWSRFS_DMI;

import java.io.File;

import java.util.Vector;

import javax.swing.JFrame;

import RTi.TS.TS;
import RTi.TS.TSCommandProcessor;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;

import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.IO.SkeletonCommand;

import RTi.Util.String.StringUtil;

import RTi.Util.Time.DateTime;

/**
<p>
This class initializes, checks, and runs the writeNWSRFSESPTraceEnsemble() 
command.
</p>
<p>The CommandProcessor must return the following properties:  
TSResultsList and WorkingDir.
</p>
*/
public class writeNWSRFSESPTraceEnsemble_Command 
extends SkeletonCommand
implements Command {

protected static final String
	_FALSE = "False",
	_TRUE = "True";

/**
Constructor.
*/
public writeNWSRFSESPTraceEnsemble_Command ()
{
	super();
	setCommandName ( "writeNWSRFSESPTraceEnsemble" );
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
	String OutputFile = parameters.getValue("OutputFile");
	String CarryoverGroup = parameters.getValue("CarryoverGroup");
	String ForecastGroup = parameters.getValue("ForecastGroup");
	String Segment = parameters.getValue("Segment");
	String SegmentDescription = parameters.getValue("SegmentDescription");
	String Latitude = parameters.getValue("Latitude");
	String Longitude = parameters.getValue("Longitude");
	String RFC = parameters.getValue("RFC");
	String TSList = parameters.getValue("TSList");
	
	// OutputFile
	if (OutputFile != null && OutputFile.length() != 0 ) {
		OutputFile = IOUtil.getPathUsingWorkingDir(OutputFile);
		File f = new File(OutputFile);
	} 
	else {
		warning += "\nThe Output File must be specified.";
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
public boolean editCommand(JFrame parent) {
	// The command will be modified if changed...
	return ( new writeNWSRFSESPTraceEnsemble_JDialog(parent, this)).ok();
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable {
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
	// This is the new format of parsing, where parameters are
	// specified as "InputFilter=", etc.
	String routine = "writeNWSRFSESPTraceEnsemble_Command.parseCommand", 
	       message = null;

	int warning_count = 0;

	Vector tokens = StringUtil.breakStringList(command, "()", 
		StringUtil.DELIM_SKIP_BLANKS);
	if ((tokens == null) || tokens.size() < 2) {
		// Must have at least the command name and the InputFile
		message = "Syntax error in \"" + command 
			+ "\".  Not enough tokens.";
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
}

/**
Run the command:
<pre>
writeNWSRFSESPTraceEnsemble(OutputFile="x",CarryoverGroup="x",ForecastGroup="x",
Segment="x",SegmentDescription="x",Latitude="x",Longitude="x",RFC="x",
TSList="x")
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
	String routine = "TSEngine.do_writeNWSRFSESPTraceEnsemble",
	       message = null;
	
	int warning_count = 0;

	// Get the command properties not already stored as members.
	String OutputFile = _parameters.getValue ( "OutputFile" );
	String CarryoverGroup = _parameters.getValue ( "CarryoverGroup" );
	String ForecastGroup = _parameters.getValue ( "ForecastGroup" );
	String Segment = _parameters.getValue ( "Segment" );
	String SegmentDescription = _parameters.getValue("SegmentDescription");
	String Latitude = _parameters.getValue ( "Latitude" );
	String Longitude = _parameters.getValue ( "Longitude" );
	String RFC = _parameters.getValue ( "RFC" );
	String TSList = _parameters.getValue ( "TSList" );
	
	Vector tslist = null;
	if ((TSList != null) && TSList.equalsIgnoreCase("SelectedTS") ) {
		tslist = ((TSCommandProcessor)getCommandProcessor())
			.getTimeSeriesToProcess("SelectedTS", null);
	}
	else {
		// Default is to output all time series...
		tslist = ((TSCommandProcessor)getCommandProcessor())
			.getTimeSeriesToProcess("AllTS", null);
	}

	Vector tssublist = (Vector)tslist.elementAt(0);
	if ((tssublist == null) && (tssublist.size() == 0)) {
		message = "Unable to find time series to write using TSID \"" 
			+ TSList + "\".";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count), routine, message );
	}

	PropList props = new PropList ( "writeNWSRFSESPTraceEnsemble" );
	// Transfer properties to that needed by ESP...
	if ( CarryoverGroup != null ) {
		props.set ( "CarryoverGroup", CarryoverGroup );
	}
	if ( ForecastGroup != null ) {
		props.set ( "ForecastGroup", ForecastGroup );
	}
	if ( Segment != null ) {
		props.set ( "Segment", Segment );
	}
	if ( SegmentDescription != null ) {
		props.set ( "SegmentDescription", SegmentDescription );
	}
	if ( Latitude != null ) {
		props.set ( "Latitude", Latitude );
	}
	if ( Longitude != null ) {
		props.set ( "Longitude", Longitude );
	}
	if ( RFC != null ) {
		props.set ( "RFC", RFC );
	}
	
	try {
		NWSRFS_ESPTraceEnsemble esp = new NWSRFS_ESPTraceEnsemble( 
			(Vector)(tslist.elementAt(0)), props );
		esp.writeESPTraceEnsembleFile ( OutputFile );
	}
	catch (Exception e) {
		message = "An error occurred while writing to the file \""
			+ OutputFile + "\".";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag(command_tag, 
				++warning_count), routine, message);
	}
	
	// Throw CommandWarningException in case of problems.
	if (warning_count > 0) {
		String plural_s = "s";
		if (warning_count == 1) {
			plural_s = "";
		}
		String plural_verb = "were";
		if (warning_count == 1) {
			plural_verb = "was";
		}
		
		message = "There " + plural_verb + " " + warning_count +
			" warning" + plural_s + " processing the command.";
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

	String OutputFile = props.getValue ( "OutputFile" );
	String CarryoverGroup = props.getValue ( "CarryoverGroup" );
	String ForecastGroup = props.getValue ( "ForecastGroup" );
	String Segment = props.getValue ( "Segment" );
	String SegmentDescription = props.getValue ( "SegmentDescription" );
	String Latitude = props.getValue ( "Latitude" );
	String Longitude = props.getValue ( "Longitude" );
	String RFC = props.getValue ( "RFC" );
	String TSList = props.getValue ( "TSList" );

	StringBuffer b = new StringBuffer ();

	// Input File
	if ((OutputFile != null) && (OutputFile.length() > 0)) {
		b.append("OutputFile=\"" + OutputFile + "\"");
	}

	// CarryoverGroup
	if ((CarryoverGroup != null) && (CarryoverGroup.length() > 0)) {
		if (b.length() > 0) {
			b.append(",");
		}
		b.append("CarryoverGroup=\"" + CarryoverGroup + "\"");
	}

	// ForecastGroup
	if ((ForecastGroup != null) && (ForecastGroup.length() > 0)) {
		if (b.length() > 0) {
			b.append(",");
		}
		b.append("ForecastGroup=\"" + ForecastGroup + "\"");
	}

	// Segment
	if ((Segment != null) && (Segment.length() > 0)) {
		if (b.length() > 0) {
			b.append(",");
		}
		b.append("Segment=\"" + Segment + "\"");
	}

	// SegmentDescription
	if ((SegmentDescription != null) && (SegmentDescription.length() > 0)) {
		if (b.length() > 0) {
			b.append(",");
		}
		b.append("SegmentDescription=\"" + SegmentDescription + "\"");
	}

	// Latitude
	if ((Latitude != null) && (Latitude.length() > 0)) {
		if (b.length() > 0) {
			b.append(",");
		}
		b.append("Latitude=\"" + Latitude + "\"");
	}

	// Longitude
	if ((Longitude != null) && (Longitude.length() > 0)) {
		if (b.length() > 0) {
			b.append(",");
		}
		b.append("Longitude=\"" + Longitude + "\"");
	}

	// RFC
	if ((RFC != null) && (RFC.length() > 0)) {
		if (b.length() > 0) {
			b.append(",");
		}
		b.append("RFC=\"" + RFC + "\"");
	}

	// TSList
	if ((TSList != null) && (TSList.length() > 0)) {
		if (b.length() > 0) {
			b.append(",");
		}
		b.append("TSList=\"" + TSList + "\"");
	}

	return getCommandName() + "(" + b.toString() + ")";
}

} // end writeNWSRFSESPTraceEnsemble_Command
