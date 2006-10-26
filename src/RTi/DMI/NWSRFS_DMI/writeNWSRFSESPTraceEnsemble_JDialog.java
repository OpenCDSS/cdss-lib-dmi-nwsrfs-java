// ----------------------------------------------------------------------------
// writeNWSRFSESPTraceEnsemble_JDialog - editor for
//					writeNWSRFSESPTraceEnsemble()
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 2003-11-24	Steven A. Malers, RTi	Initial version (copy and modify
//					writeDateValue_JDialog).
// 2004-02-17	SAM, RTi		Fix bug where directory from file
//					selection was not getting set as the
//					last dialog directory in JGUIUtil.
// 2004-05-27	SAM, RTi		* Change name from writeESP... to
//					  writeNWSRFSESP...
//					* Change to variable parameter notation.
//					* Expand comments to stress that time
//					  series must be traces.
// 2004-05-31	SAM, RTi		* Add TSList parameter to specify which
//					  time series should be written.
// 2004-06-01	SAM, RTi		* Add properties to pass to the ESP
//					  trace ensemble code to control the
//					  write.
// 2006-01-17	J. Thomas Sapienza, RTi	* Moved from TSTool package.
// ----------------------------------------------------------------------------

package RTi.DMI.NWSRFS_DMI;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFileChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.io.File;

import java.util.Vector;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;

import RTi.Util.IO.Command;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

public class writeNWSRFSESPTraceEnsemble_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{
private final String __SELECTED_TS = "SelectedTS";
private final String __ALL_TS = "AllTS";

private SimpleJButton	__cancel_JButton = null,// Cancel Button
			__browse_JButton = null,// Browse Button
			__ok_JButton = null,	// Ok Button
			__path_JButton = null;	// Button to add/remove path
private JFrame		__parent_JFrame = null;	// parent Frame GUI class
private Command		__command = null;
private String		__working_dir = null;	// Working directory.
private JTextArea	__Command_JTextArea=null;// Command as TextField
private JTextField	__OutputFile_JTextField = null; // Field for time series
						// identifier
private JTextField	__CarryoverGroup_JTextField = null;
						// Field for carryover group
private JTextField	__ForecastGroup_JTextField = null;
						// Field for forecast group
private JTextField	__Segment_JTextField = null;
						// Field for segment ID
private JTextField	__SegmentDescription_JTextField = null;
						// Field for segment description
private JTextField	__Latitude_JTextField = null;
						// Field for latitude
private JTextField	__Longitude_JTextField = null;
						// Field for longitude
private JTextField	__RFC_JTextField = null;// Field for RFC
private SimpleJComboBox	__TSList_JComboBox = null;
private boolean		__error_wait = false;	// Is there an error that we
						// are waiting to be cleared up
						// or Cancel?
private boolean		__first_time = true;
private boolean		__ok = false;			
private final String
	__REMOVE_WORKING_DIRECTORY = "Remove Working Directory",
	__ADD_WORKING_DIRECTORY = "Add Working Directory";

/**
writeNWSRFSESPTraceEnsemble_JDialog constructor.
@param parent JFrame class instantiating this class.
@param app_PropList Properties from the application.
@param command Time series command to parse.
@param tsids Time series identifiers for available time series.
*/
public writeNWSRFSESPTraceEnsemble_JDialog ( JFrame parent, Command command )
{
	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
{	Object o = event.getSource();

	if ( o == __browse_JButton ) {
		String last_directory_selected =
			JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser(
				last_directory_selected );
		}
		else {	fc = JFileChooserFactory.createJFileChooser(
				__working_dir );
		}
		fc.setDialogTitle(
			"Select NWSRFS ESP Trace Ensemble File to Write");
		SimpleFileFilter sff = new SimpleFileFilter("CS",
			"ESP Conditional Trace Ensemble File");
		fc.addChoosableFileFilter(sff);
		
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String filename = fc.getSelectedFile().getName(); 
			String path = fc.getSelectedFile().getPath(); 
	
			if (filename == null || filename.equals("")) {
				return;
			}
	
			if (path != null) {
				__OutputFile_JTextField.setText(path );
				JGUIUtil.setLastFileDialogDirectory(directory );
				refresh();
			}
		}
	}
	else if ( o == __cancel_JButton ) {
		response(false);
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response(true);
		}
	}
	else if ( o == __path_JButton ) {
		if (	__path_JButton.getText().equals(
			__ADD_WORKING_DIRECTORY) ) {
			__OutputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir,
			__OutputFile_JTextField.getText() ) );
		}
		else if ( __path_JButton.getText().equals(
			__REMOVE_WORKING_DIRECTORY) ) {
			try {	__OutputFile_JTextField.setText (
				IOUtil.toRelativePath ( __working_dir,
				__OutputFile_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,
				"writeNWSRFSESPTraceEnsemble_JDialog",
				"Error converting file to relative path." );
			}
		}
		refresh ();
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	String file = __OutputFile_JTextField.getText();
	String Latitude = __Latitude_JTextField.getText();
	String Longitude = __Longitude_JTextField.getText();
	String routine = "writeNWSRFSESPTraceEnsemble_JDialog.checkInput";
	String warning = "";
	__error_wait = false;
	// Adjust the working directory that was passed in by the specified
	// directory.  If the directory does not exist, warn the user...
	try {	String adjusted_path = IOUtil.adjustPath ( __working_dir, file);
		File f = new File ( adjusted_path );
		File f2 = new File ( f.getParent() );
		if ( !f2.exists() ) {
			warning += "\nThe NWSRFS ESP trace " +
			"ensemble parent directory does not exist:\n" +
			"    " + adjusted_path + "\n" +
		  	"Correct or Cancel.";
		}
		f = null;
		f2 = null;
	}
	catch ( Exception e ) {
		warning += "\nThe working directory:\n" +
		"    \"" + __working_dir + "\"\ncannot be adjusted using:\n" +
		"    \"" + file + "\".\n" +
		"Correct the file or Cancel.";
	}

	try {	String adjusted_path = IOUtil.adjustPath ( __working_dir, file);
		File f = new File ( adjusted_path );
		String filename = f.getName();
		Vector parts = StringUtil.breakStringList(filename, ".", 0);
		if (parts.size() != 5) {
			warning += "\nThe file name \"" + filename 
				+ "\" does not match the expected 5-part ESP "
				+ "filename format.  Correct the filename or "
				+ "cancel.";
		}
	}		
	catch ( Exception e ) {
		warning += "\nThe working directory:\n" +
		"    \"" + __working_dir + "\"\ncannot be adjusted using:\n" +
		"    \"" + file + "\".\n" +
		"Correct the file or Cancel.";
	}

	// Make sure the filename matches the 5-part ESPADP format standard.
	// Currently, nothing is checked apart from whether there are
	// 5 periods.  Invalid units and intervals can still be entered.
	String filename = file;
	
	if ( (Latitude.length() > 0) && !StringUtil.isDouble(Latitude) ) {
		warning += "\nThe latitude is not a number.";
	}
	if ( (Longitude.length() > 0) && !StringUtil.isDouble(Longitude) ) {
		warning += "\nThe longitude is not a number.";
	}
	if ( warning.length() > 0 ) {
		__error_wait = true;
		Message.printWarning ( 1, routine, warning );
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits() {
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String CarryoverGroup = __CarryoverGroup_JTextField.getText().trim();
	String ForecastGroup = __ForecastGroup_JTextField.getText().trim();
	String Segment = __Segment_JTextField.getText().trim();
	String SegmentDescription = __SegmentDescription_JTextField.getText()
		.trim();
	String Latitude = __Latitude_JTextField.getText().trim();
	String Longitude = __Longitude_JTextField.getText().trim();
	String RFC = __RFC_JTextField.getText().trim();
	String TSList = __TSList_JComboBox.getSelected().trim();

	__command.setCommandParameter("OutputFile", OutputFile);
	__command.setCommandParameter("CarryoverGroup", CarryoverGroup);
	__command.setCommandParameter("ForecastGroup", ForecastGroup);
	__command.setCommandParameter("Segment", Segment);
	__command.setCommandParameter("SegmentDescription", SegmentDescription);
	__command.setCommandParameter("Latitude", Latitude);
	__command.setCommandParameter("Longitude", Longitude);
	__command.setCommandParameter("RFC", RFC);
	__command.setCommandParameter("TSList", TSList);
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__cancel_JButton = null;
	__Command_JTextArea = null;
	__OutputFile_JTextField = null;
	__CarryoverGroup_JTextField = null;
	__ForecastGroup_JTextField = null;
	__Segment_JTextField = null;
	__SegmentDescription_JTextField = null;
	__Latitude_JTextField = null;
	__Longitude_JTextField = null;
	__RFC_JTextField = null;
	__TSList_JComboBox = null;
	__ok_JButton = null;
	__path_JButton = null;
	__browse_JButton = null;
	__parent_JFrame = null;
	__working_dir = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent Frame class instantiating this class.
@param title Dialog title.
@param app_PropList Properties from the application.
@param command Vector of String containing the time series command, which
should have a time series identifier and optionally comments.
@param tsids Time series identifiers from
TSEngine.getTSIdentifiersFromCommands().
*/
private void initialize (	JFrame parent, Command command)
{	__parent_JFrame = parent;
	__command = command;
	__working_dir = (String) __command.getCommandProcessor().
		getPropContents ( "WorkingDir" );

	addWindowListener( this );

        Insets insetsTLBR = new Insets(1,2,1,2);

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	GridBagConstraints gbc = new GridBagConstraints();
	getContentPane().add ( "North", main_JPanel );
	int y = 0;

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"This command writes in-memory time series to an NWSRFS ESP " +
		"trace ensemble file."),
		0, y, 7, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Currently, only conditional simulation (CS) trace files " +
		"can be written."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The time series to write must be traces having a consistent"+
		" period, and the sequence numbers must be defined."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"For example, traces read from a DateValue file or created " +
		"with the createTraces() command can be processed."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The file can be specified using a full or " +
		"relative path (relative to the working directory)."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);
	if ( __working_dir != null ) {
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The working directory is: " + __working_dir ), 
		0, ++y, 7, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);
	}
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The Browse button can be used to select an existing file " +
		"to overwrite (or edit the file name after selection)."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"To be understood by the NWS ESPADP program, the file name" +
		" should adhere to the format:"),
		0, ++y, 7, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"   Segment.Location.DataType.HH.CS"),
		0, ++y, 7, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"where the DataType is an NWSRFS data type and the interval "+
		"HH is padded with zeros."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"File to write:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.EAST);
	__OutputFile_JTextField = new JTextField ( 50 );
	__OutputFile_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __OutputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, gbc.HORIZONTAL, gbc.WEST);
	__browse_JButton = new SimpleJButton ( "Browse", "Browse", this );
        JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, gbc.NONE, gbc.CENTER);

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Carryover group:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.EAST);
	__CarryoverGroup_JTextField = new JTextField(10);
	__CarryoverGroup_JTextField.addKeyListener(this);
	JGUIUtil.addComponent(main_JPanel, __CarryoverGroup_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Carryover group (optional)."),
		3, y, 3, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Forecast group:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.EAST);
	__ForecastGroup_JTextField = new JTextField(10);
	__ForecastGroup_JTextField.addKeyListener(this);
	JGUIUtil.addComponent(main_JPanel, __ForecastGroup_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Forecast group (optional)."),
		3, y, 3, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Segment:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.EAST);
	__Segment_JTextField = new JTextField(10);
	__Segment_JTextField.addKeyListener(this);
	JGUIUtil.addComponent(main_JPanel, __Segment_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Segment (optional) - default is 1st part of file name."),
		3, y, 3, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Segment description:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.EAST);
	__SegmentDescription_JTextField = new JTextField(10);
	__SegmentDescription_JTextField.addKeyListener(this);
	JGUIUtil.addComponent(main_JPanel, __SegmentDescription_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Segment description (optional) - default is from first time " +
		"series."),
		3, y, 3, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Latitude:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.EAST);
	__Latitude_JTextField = new JTextField(10);
	__Latitude_JTextField.addKeyListener(this);
	JGUIUtil.addComponent(main_JPanel, __Latitude_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Latitude, decimal degrees (optional)."),
		3, y, 3, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Longitude:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.EAST);
	__Longitude_JTextField = new JTextField(10);
	__Longitude_JTextField.addKeyListener(this);
	JGUIUtil.addComponent(main_JPanel, __Longitude_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Longitude, decimal degrees (optional)."),
		3, y, 3, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel ("RFC:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.EAST);
	__RFC_JTextField = new JTextField(10);
	__RFC_JTextField.addKeyListener(this);
	JGUIUtil.addComponent(main_JPanel, __RFC_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"River Forecast Center abbreviation (optional)."),
		3, y, 3, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel ("TS list:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.EAST);
	Vector tslist_Vector = new Vector();
	tslist_Vector.addElement ( "" );
	tslist_Vector.addElement ( __SELECTED_TS );
	tslist_Vector.addElement ( __ALL_TS );
	__TSList_JComboBox = new SimpleJComboBox(false);
	__TSList_JComboBox.setData ( tslist_Vector );
	__TSList_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __TSList_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, gbc.NONE, gbc.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Indicates the time series to output.  Default is AllTS."),
		3, y, 3, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, gbc.NONE, gbc.EAST);
	__Command_JTextArea = new JTextArea(4, 55);
	__Command_JTextArea.setLineWrap ( true );
	__Command_JTextArea.setWrapStyleWord ( true );	
	__Command_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__Command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, gbc.HORIZONTAL, gbc.WEST);

	// Refresh the contents...
	refresh ();

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, gbc.HORIZONTAL, gbc.CENTER);

	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative
		// path...
		__path_JButton = new SimpleJButton(__REMOVE_WORKING_DIRECTORY,
			__REMOVE_WORKING_DIRECTORY, this);
		button_JPanel.add ( __path_JButton );
	}
	__cancel_JButton = new SimpleJButton("Cancel", "Cancel", this);
	button_JPanel.add ( __cancel_JButton );
	__ok_JButton = new SimpleJButton("OK", "OK", this);
	button_JPanel.add ( __ok_JButton );

	setTitle("Edit writeNWSRFSESPTraceEnsemble() Command");
	setResizable ( true );
        pack();
        JGUIUtil.center( this );
	refresh();	// Sets the __path_JButton status
        super.setVisible( true );
}

/**
Handle ItemEvent events.
@param e ItemEvent to handle.
*/
public void itemStateChanged (ItemEvent e) {
	refresh();
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();

	if ( code == event.VK_ENTER ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response(true);
		}
	}
}

public void keyReleased ( KeyEvent event )
{	refresh();
}

public void keyTyped ( KeyEvent event )
{	refresh();
}

/**
Indicate if the user pressed OK (cancel otherwise).
@return true if the edits were committed, false if the user cancelled.
*/
public boolean ok() {
	return __ok;
}

/**
Refresh the command from the other text field contents.  The command is of the
form:
<pre>
writeNWSRFSESPTraceEnsemble(OutputFile="X",CarryoverGroup="X",
ForecastGroup="X",Segment="X",SegmentDescription="X",Latitude=X,Longitude=X,
RFC="X",TSList="X")
</pre>
*/
private void refresh ()
{	String routine = "writeNWSRFSESPTraceEnsemble_JDialog.refresh";
	String OutputFile = "";
	String CarryoverGroup = "";
	String ForecastGroup = "";
	String Segment = "";
	String SegmentDescription = "";
	String Latitude = "";
	String Longitude = "";
	String RFC = "";
	String TSList = "";
	PropList props = null;
	__error_wait = false;
	if ( __first_time ) {
		__first_time = false;
		props = __command.getCommandParameters();
		OutputFile = props.getValue("OutputFile");
		CarryoverGroup = props.getValue("CarryoverGroup");
		ForecastGroup = props.getValue("ForecastGroup");
		Segment = props.getValue("Segment");
		SegmentDescription = props.getValue("SegmentDescription");
		Latitude = props.getValue("Latitude");
		Longitude = props.getValue("Longitude");
		RFC = props.getValue("RFC");
		TSList = props.getValue("TSList");
		if ( OutputFile != null ) {
			__OutputFile_JTextField.setText ( OutputFile );
		}
		if ( CarryoverGroup != null ) {
			__CarryoverGroup_JTextField.setText ( CarryoverGroup );
		}
		if ( ForecastGroup != null ) {
			__ForecastGroup_JTextField.setText ( ForecastGroup );
		}
		if ( Segment != null ) {
			__Segment_JTextField.setText ( Segment );
		}
		if ( SegmentDescription != null ) {
			__SegmentDescription_JTextField.setText (
				SegmentDescription );
		}
		if ( Latitude != null ) {
			__Latitude_JTextField.setText ( Latitude );
		}
		if ( Longitude != null ) {
			__Longitude_JTextField.setText ( Longitude );
		}
		if ( RFC != null ) {
			__RFC_JTextField.setText ( RFC );
		}
		if ( TSList == null ) {
			// Select default...
			__TSList_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__TSList_JComboBox,
				TSList, JGUIUtil.NONE, null, null ) ) {
				__TSList_JComboBox.select ( TSList );
			}
			else {	Message.printWarning ( 1, routine,
				"Existing writeDateValue() " +
				"references an invalid\nTSList value \"" +
				TSList +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
	}
	// Regardless, reset the command from the fields...
	OutputFile = __OutputFile_JTextField.getText().trim();
	CarryoverGroup = __CarryoverGroup_JTextField.getText().trim();
	ForecastGroup = __ForecastGroup_JTextField.getText().trim();
	Segment = __Segment_JTextField.getText().trim();
	SegmentDescription = __SegmentDescription_JTextField.getText().trim();
	Latitude = __Latitude_JTextField.getText().trim();
	Longitude = __Longitude_JTextField.getText().trim();
	RFC = __RFC_JTextField.getText().trim();
	TSList = __TSList_JComboBox.getSelected().trim();

	props = new PropList(__command.getCommandName());
	props.add("OutputFile=" + OutputFile);
	props.add("CarryoverGroup=" + CarryoverGroup);
	props.add("ForecastGroup=" + ForecastGroup);
	props.add("Segment=" + Segment);
	props.add("SegmentDescription=" + SegmentDescription);
	props.add("Latitude=" + Latitude);
	props.add("Longitude=" + Longitude);
	props.add("RFC=" + RFC);
	props.add("TSList=" + TSList);
	
	__Command_JTextArea.setText(__command.toString(props));

	refreshPathControl();
}

/**
Refresh the PathControl text based on the contents of the input text field
contents.
*/
private void refreshPathControl()
{
	String InputFile = __OutputFile_JTextField.getText().trim();
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
			__path_JButton.setText( __REMOVE_WORKING_DIRECTORY);
		}
		else {	
			__path_JButton.setText(__ADD_WORKING_DIRECTORY);
		}
	}
}

/**
Return the time series command as a Vector of String.
@return returns the command text or null if no command.
*/
public void response (boolean ok) {
	__ok = ok;
	if (ok) {
		// commit the changes
		commitEdits();
		if (__error_wait) {
			// not ready to close
			return;
		}
	}
	// Now close
	setVisible(false);
	dispose();
}

/**
Responds to WindowEvents.
@param event WindowEvent object
*/
public void windowClosing( WindowEvent event )
{	response(false);
}

public void windowActivated( WindowEvent evt ){;}
public void windowClosed( WindowEvent evt ){;}
public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}

} // end writeNWSRFSESPTraceEnsemble_JDialog
