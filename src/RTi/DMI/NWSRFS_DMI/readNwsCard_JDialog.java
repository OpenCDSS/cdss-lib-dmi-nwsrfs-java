// ----------------------------------------------------------------------------
// readNwsCard_JDialog - editor for both the "TS x = readNwsCard()" and
//	"readNwsCard()" commands.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 2002-05-12	Steven A. Malers, RTi	Initial version (copy and modify
//					readDateValue).
// 2002-06-26	SAM, RTi		Add comment about date format.
// 2003-12-07	SAM, RTi		Update to Swing.
// 2005-12-06	J. Thomas Sapienza, RTi	* Combined code for both editors into
//					  this single class.
//					* Added support for Read24HourAsDay
//					  parameter.
// 2006-01-04	JTS, RTi		Many changes following SAM's review.
// 2006-01-08	SAM, RTi		Change some dialog notes to be more
//					clear.
// 2006-01-18	JTS, RTi		Moved from RTi.TS package.
// 2007-02-16	SAM, RTi		Use new CommandProcessor interface.
//					Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DMI.NWSRFS_DMI;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;

import RTi.Util.IO.Command;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

/**
The readNwsCard_JDialog edits the TS Alias = readNwsCard() and non-TS Alias
readNWSCard() commands.  If the "WorkingDir" command processor property is 
defined, then the dialog will allow the input file path to be truncated to a 
relative path.
*/
public class readNwsCard_JDialog extends JDialog
implements ActionListener, KeyListener, WindowListener
{
private SimpleJButton	__browse_JButton = null,// File browse button
			__path_JButton = null,	// Convert between relative and
						// absolute path.
			__cancel_JButton = null,// Cancel Button
			__ok_JButton = null;	// Ok Button
private JFrame		__parent_JFrame = null;	// parent JFrame
private Command		__command = null;
private String		__working_dir = null;	// Working directory.
private JTextField	__Alias_JTextField = null,// Alias for time series.
			__InputStart_JTextField,
			__InputEnd_JTextField,
						// Text fields for analysis
						// period.
			__InputFile_JTextField = null, // Field for time series
						// identifier
			__NewUnits_JTextField = null;
				// Units to convert to at read
private SimpleJComboBox	__Read24HourAsDay_JComboBox = null;
private JTextArea	 __Command_JTextArea = null;
private boolean		__error_wait = false;	// Is there an error that we
						// are waiting to be cleared up
						// or Cancel?
private boolean		__first_time = true;

private boolean 	__isAliasVersion = false;	
			// Whether this dialog is being opened for the version
			// of the command that returns an alias or not
private boolean		__ok = false;			
private final String
	__REMOVE_WORKING_DIRECTORY = "Remove Working Directory",
	__ADD_WORKING_DIRECTORY = "Add Working Directory";

/**
readNwsCard_JDialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public readNwsCard_JDialog ( JFrame parent, Command command )
{
	super(parent, true);

	PropList props = command.getCommandParameters();
	String alias = props.getValue("Alias");
	Message.printStatus(1, "", "Props: " + props.toString("\n"));
	if (alias == null || alias.trim().equalsIgnoreCase("")) {
		if (((readNwsCard_Command)command).getCommandString().trim()
		    .toUpperCase().startsWith("TS ")) {
		    	__isAliasVersion = true;
		}
		else {
			__isAliasVersion = false;
		}
	}
	else {
		__isAliasVersion = true;
	}
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
{	Object o = event.getSource();

	if ( o == __browse_JButton ) {
		// Browse for the file to read...
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle( "Select NWS Card Time Series File");
		SimpleFileFilter sff = new SimpleFileFilter("card",
			"NWS Card File");
		fc.addChoosableFileFilter(sff);
		sff = new SimpleFileFilter("txt",
			"NWS Card File");
		fc.addChoosableFileFilter(sff);
		
		String last_directory_selected =
			JGUIUtil.getLastFileDialogDirectory();
		if ( last_directory_selected != null ) {
			fc.setCurrentDirectory(
				new File(last_directory_selected));
		}
		else {	fc.setCurrentDirectory(new File(__working_dir));
		}
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String filename = fc.getSelectedFile().getName(); 
			String path = fc.getSelectedFile().getPath(); 
	
			if (filename == null || filename.equals("")) {
				return;
			}
	
			if (path != null) {
				__InputFile_JTextField.setText(path );
				JGUIUtil.setLastFileDialogDirectory( directory);
				refresh();
			}
		}
	}
	else if ( o == __cancel_JButton ) {
		response(false);
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
		if (!__error_wait) {
			response(true);
		}
	}
	else if ( o == __path_JButton ) {
		if (	__path_JButton.getText().equals(
			"Add Working Directory") ) {
			__InputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir,
			__InputFile_JTextField.getText() ) );
		}
		else if ( __path_JButton.getText().equals(
			"Remove Working Directory") ) {
			try {	__InputFile_JTextField.setText (
				IOUtil.toRelativePath ( __working_dir,
				__InputFile_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,
				"TSreadNWSCard_JDialog",
				"Error converting file to relative path." );
			}
		}
		refresh ();
	}
	else if (o == __Read24HourAsDay_JComboBox) {
		refresh();
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput () {
	
	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String InputFile = __InputFile_JTextField.getText().trim();
	String InputStart = __InputStart_JTextField.getText().trim();
	String InputEnd = __InputEnd_JTextField.getText().trim();
	String NewUnits = __NewUnits_JTextField.getText().trim();
	String Read24HourAsDay 
		= __Read24HourAsDay_JComboBox.getSelected().trim();
	String Alias = null;
	if (__isAliasVersion) { 
		Alias = __Alias_JTextField.getText().trim();
	}
	
	__error_wait = false;
	
	if (InputFile.length() > 0) {
		props.set("InputFile", InputFile);
	}
	if (InputStart.length() > 0 && !InputStart.equals("*")) {
		props.set("InputStart", InputStart);
	}
	if (InputEnd.length() > 0 && !InputEnd.equals("*")) {
		props.set("InputEnd", InputEnd);
	}
	if (NewUnits.length() > 0 && !NewUnits.equals("*")) {
		props.set("NewUnits", NewUnits);
	}
	if (Alias != null && Alias.length() > 0) {
		props.set("Alias", Alias);
	}
	if (Read24HourAsDay.trim().length() > 0) {
		props.set("Read24HourAsDay", Read24HourAsDay);
	}

	try {	// This will warn the user...
		__command.checkCommandParameters ( props, null, 1 );
	} 
	catch ( Exception e ) {
		// The warning would have been printed in the check 
		// code.
		__error_wait = true;
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits() {
	String InputFile = __InputFile_JTextField.getText().trim();
	String InputStart = __InputStart_JTextField.getText().trim();
	String InputEnd = __InputEnd_JTextField.getText().trim();
	String NewUnits = __NewUnits_JTextField.getText().trim();
	String Read24HourAsDay 
		= __Read24HourAsDay_JComboBox.getSelected().trim();

	__command.setCommandParameter("InputFile", InputFile);
	__command.setCommandParameter("InputStart", InputStart);
	__command.setCommandParameter("InputEnd", InputEnd);
	__command.setCommandParameter("NewUnits", NewUnits);
	__command.setCommandParameter("Read24HourAsDay", Read24HourAsDay);
	
	if (__isAliasVersion) {
		String Alias = __Alias_JTextField.getText().trim();
		__command.setCommandParameter("Alias", Alias);
	}
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable {
	__browse_JButton = null;
	__path_JButton = null;
	__cancel_JButton = null;
	__ok_JButton = null;
	__parent_JFrame = null;
	__command = null;
	__working_dir = null;
	__Alias_JTextField = null;
	__InputStart_JTextField = null;
	__InputEnd_JTextField = null;
	__InputFile_JTextField = null;
	__NewUnits_JTextField = null;
	__Command_JTextArea = null;

	super.finalize();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param app_PropList Properties from application.
@param command Command to edit.
*/
private void initialize(JFrame parent, Command command) {
	__parent_JFrame = parent;
	__command = command;
	CommandProcessor processor = __command.getCommandProcessor();
	
	try { Object o = processor.getPropContents ( "WorkingDir" );
		// Working directory is available so use it...
		if ( o != null ) {
			__working_dir = (String)o;
		}
	}
	catch ( Exception e ) {
		// Not fatal, but of use to developers.
		String message = "Error requesting WorkingDir from processor - not using.";
		String routine = __command.getCommandName() + "_JDialog.initialize";
		Message.printDebug(10, routine, message );
	}

	addWindowListener( this );

        Insets insetsTLBR = new Insets(2,2,2,2);

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = 0;

	if (__isAliasVersion) {
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Read a single time series from a NWS Card format file and " +
		"assign an alias to the time series."),
		0, y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	else {
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Read all the time series from an NWS Card format file," +
		" using information in the file to assign the" +
		" identifier and alias."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The file may contain one time series or be an ESP trace " +
		"ensemble file (in NWS Card format) with multiple time " +
		"series."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify a full path or relative path (relative to working " +
		"directory) for a NWS Card file to read." ), 
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	if ( __working_dir != null ) {
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The working directory is: " + __working_dir ), 
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specifying units causes conversion during the read." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"If reading 24Hour data as Day and the input period is " +
		"specified, specify hour 24 of the day or hour 0 of the " +
		" following day."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specifying the input period will limit data that are " +
		"available for fill commands but can increase performance." ), 
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify date/times using an hour format (e.g.," +
		" YYYY-MM-DD HH or MM/DD/YYYY HH,"),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"where HH is evenly divisible by the interval)."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"If not specified, the period defaults to the global input "
		+ "period (or all data if not specified)."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	if (__isAliasVersion) {
	        JGUIUtil.addComponent(main_JPanel, 
			new JLabel("Time Series Alias:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__Alias_JTextField = new JTextField ( 30 );
		__Alias_JTextField.addKeyListener ( this );
		JGUIUtil.addComponent(main_JPanel, __Alias_JTextField,
			1, y, 3, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	}

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"NWS Card file to read:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InputFile_JTextField = new JTextField ( 50 );
	__InputFile_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __InputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ( "Browse", this );
        JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);

        JGUIUtil.addComponent(main_JPanel, new JLabel("Units to convert to:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__NewUnits_JTextField = new JTextField ( "", 10 );
	__NewUnits_JTextField.addKeyListener ( this );
	JGUIUtil.addComponent(main_JPanel, __NewUnits_JTextField,
		1, y, 3, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, new JLabel("Read 24 hour as day:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	Vector v = new Vector();
	v.add("");
	v.add(readNwsCard_Command._TRUE);
	v.add(readNwsCard_Command._FALSE);
	__Read24HourAsDay_JComboBox = new SimpleJComboBox(v);
	__Read24HourAsDay_JComboBox.select(0);
	__Read24HourAsDay_JComboBox.addActionListener(this);
	JGUIUtil.addComponent(main_JPanel, __Read24HourAsDay_JComboBox,
		1, y, 3, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Period to read:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InputStart_JTextField = new JTextField ( "", 15 );
	__InputStart_JTextField.addKeyListener ( this );
	JGUIUtil.addComponent(main_JPanel, __InputStart_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "to" ), 
		3, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	__InputEnd_JTextField = new JTextField ( "", 15 );
	__InputEnd_JTextField.addKeyListener ( this );
	JGUIUtil.addComponent(main_JPanel, __InputEnd_JTextField,
		4, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Command_JTextArea = new JTextArea(4, 55);
	__Command_JTextArea.setLineWrap ( true );
	__Command_JTextArea.setWrapStyleWord ( true );	
	__Command_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__Command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// Refresh the contents...
	refresh ();

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative
		// path...
		__path_JButton = new SimpleJButton(
			"Remove Working Directory", this);
		button_JPanel.add ( __path_JButton );
	}
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add ( __cancel_JButton );
	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add ( __ok_JButton );

	if (__isAliasVersion) {
		setTitle("Edit TS Alias = readNwsCard() Command");
	}
	else {
		setTitle("Edit readNwsCard() Command");
	}

	setResizable ( true );
        pack();
        JGUIUtil.center( this );
//	refresh();	// Sets the __path_JButton status
	refreshPathControl();	// Sets the __path_JButton status
        super.setVisible( true );
}

/**
Respond to KeyEvents.
*/
public void keyPressed(KeyEvent event) {
	int code = event.getKeyCode();
	if (code == KeyEvent.VK_ENTER) {
		refresh();
		checkInput();
		if (!__error_wait) {
			response(true);
		}
	}
}

/**
Need this to properly capture key events, especially deletes.
*/
public void keyReleased(KeyEvent event) {
	refresh();
}

public void keyTyped(KeyEvent event) {
	refresh();
}

/**
Indicate if the user pressed OK (cancel otherwise).
@return true if the edits were committed, false if the user cancelled.
*/
public boolean ok() {
	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh() {
	String InputFile = "",
	       InputStart = "",
	       InputEnd = "",
	       NewUnits = "",
	       Alias = "",
	       Read24HourAsDay = "";

	PropList props = null;

	if (__first_time) {
		__first_time = false;

		// Get the properties from the command
		props = __command.getCommandParameters();
		InputFile = props.getValue("InputFile");
		InputStart = props.getValue("InputStart");
		InputEnd = props.getValue("InputEnd");
		NewUnits = props.getValue("NewUnits");
		Alias = props.getValue("Alias");
		Read24HourAsDay = props.getValue("Read24HourAsDay");

		// Set the control fields
		if (Alias != null && __isAliasVersion) {
			__Alias_JTextField.setText(Alias.trim());
		}
		if (InputFile != null) {
			__InputFile_JTextField.setText(InputFile);
		}
		if (InputStart != null) {
			__InputStart_JTextField.setText(InputStart);
		}
		if (InputEnd != null) {
			__InputEnd_JTextField.setText(InputEnd);
		}
		if (NewUnits != null) {
			__NewUnits_JTextField.setText(NewUnits);
		}
		if (Read24HourAsDay != null) {
			if (Read24HourAsDay.equalsIgnoreCase("true")) {
				__Read24HourAsDay_JComboBox.select(
					readNwsCard_Command._TRUE);
			}
			else if (Read24HourAsDay.equalsIgnoreCase("false")) {
				__Read24HourAsDay_JComboBox.select(
					readNwsCard_Command._FALSE);
			}
			else {
				__Read24HourAsDay_JComboBox.select(0);
			}
		}
	}

	// Regardless, reset the command from the fields.  This is only  visible
	// information that has not been committed in the command.
	InputFile = __InputFile_JTextField.getText().trim();
	InputStart = __InputStart_JTextField.getText().trim();
	InputEnd = __InputEnd_JTextField.getText().trim();
	NewUnits = __NewUnits_JTextField.getText().trim();
	Read24HourAsDay = __Read24HourAsDay_JComboBox.getSelected().trim();
	if (__isAliasVersion) {
		Alias = __Alias_JTextField.getText().trim();
	}

	props = new PropList(__command.getCommandName());
	props.add("InputFile=" + InputFile);

	props.add("InputStart=" + InputStart);
	props.add("InputEnd=" + InputEnd);
	props.add("NewUnits=" + NewUnits);
	props.add("Read24HourAsDay=" + Read24HourAsDay);
	if (Alias != null) {
		props.add("Alias=" + Alias);
	}
	
	__Command_JTextArea.setText( __command.toString(props) );

	// Refresh the Path Control text.
	refreshPathControl();
}

/**
Refresh the PathControl text based on the contents of the input text field
contents.
*/
private void refreshPathControl()
{
	String InputFile = __InputFile_JTextField.getText().trim();
	if ( (InputFile == null) || (InputFile.trim().length() == 0) ) {
		if ( __path_JButton != null ) {
			__path_JButton.setEnabled ( false );
		}
		return;
	}

	// Check the path and determine what the label on the path button should
	// be...
	if ( __path_JButton != null ) {
		__path_JButton.setEnabled ( true );
		File f = new File ( InputFile );
		if ( f.isAbsolute() ) {
			__path_JButton.setText(
				__REMOVE_WORKING_DIRECTORY);
		}
		else {	__path_JButton.setText(
				__ADD_WORKING_DIRECTORY);
		}
	}
}

/**
React to the user response.
@param ok if false, then the edit is cancelled.  If true, the edit is committed
and the dialog is closed.
*/
public void response ( boolean ok ) {
	__ok = ok;
	if ( ok ) {
		// Commit the changes...
		commitEdits ();
		if ( __error_wait ) {
			// Not ready to close out!
			return;
		}
	}
	// Now close out...
	setVisible( false );
	dispose();
}

/**
Responds to WindowEvents.
@param event WindowEvent object
*/
public void windowClosing( WindowEvent event )
{	response(false);
}

public void windowActivated( WindowEvent evt )
{
}

public void windowClosed( WindowEvent evt )
{
}

public void windowDeactivated( WindowEvent evt )
{
}

public void windowDeiconified( WindowEvent evt )
{
}

public void windowIconified( WindowEvent evt )
{
}

public void windowOpened( WindowEvent evt )
{
}

} // end readNwsCard_JDialog
