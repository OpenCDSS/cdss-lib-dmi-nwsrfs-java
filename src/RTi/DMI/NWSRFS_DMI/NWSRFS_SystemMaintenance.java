////////////////////////////////////////////////////////////////////////////
/*
NWSRFS_SystemMaintenance.java

Copyright:

History:
 05 March 2002    Morgan Sheedy, RTi      Initial Implementation 
 27 Mar 2002	  AMS, RTi		Added setMinimumSize() and 
					setMaximumSize() to the 
					JScrollPane to prevent it from 
					completely collapsing.

 25 Aug, 2002   AMS                     Updated updateOutputWindow( ) method
                                        to take into account that the
                                        "-u xxx" flag is now being added to
                                        the ofs commands. The commands run,
                                        now are in format:
                            "ofs -p ppinit -i PUNCH.etc -o PUNCH.out -u pccofs"
                                        vs.
                                "ofs -p ppinit -i PUNCH.etc -o PUNCH.out"

 14 Oct, 2002   AML                     Updated name of calling application
                                        (from NWSRFSGUI_app) to:
                                        NWSRFS.

                                        Updated package name
                                        (from RTi.App.NWSRFSGUI_APP) to:
                                        RTi.App.NWSRFSGUI.

                                        Changed name from:
                                        NwsrfsSystemMaintenance to:
                                        NWSRFS_SystemMaintenance.

 22 Oct, 2002 AML                       Updated updateOutputWindow() since
                                        the ProcessManager no longer is run
                                        with the pm_unix wrapper script 
                                        which Had appended a "stop x" at
                                        the end of any output run thru the
					ProcessManager
 
 03 Apr, 2003 AML			Changed redefine stations method
 					since station ID now passed in
					to class and no string parsing/
					clipping needs to be done to get
					just the station ID.

 19 Jun, 2003 AML			Added call to:
					NWSRFS_Util.update_resegdef_file()
					to add "RESEGDEF" to top of 
					file used to redefine a segment.

 25 Jun, 2003 AML			Made _dialog(s) non-resizable.

 24 Jul, 2003 AML			Cleaned up code to compile with new
 					libraries.
2004-09-20	J. Thomas Sapienza, RTi	Changed how popup triggers are detected
					for showing popup menus, in order to 
					work better on Linux.
06 Oct, 2004 Scott Townsend, RTi	Moved from NWSRFS to NWSRFS_DMI
					to allow for removing app code from
					the DMI.
2004-11-16	JTS, RTi		* Constructor now takes an optional
					  NWSRFS_System_JTree, so that 
					  after certain operations (redefining
					  a segment or rating curve, adding a
					  rating curve) a GUI's system tree
					  can be rebuilt.
					* After certain operations (redefining
					  a segment or rating curve, adding a 
					  rating curve) if a system tree has
					  been specified to the system 
					  maintenance constructor, the tree will
					  be rebuilt.
2006-01-18	JTS, RTi		NWSCardTS now used from current package.
*/
/////////////////////////////////////////////////////////////////////////////
package RTi.DMI.NWSRFS_DMI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.String;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import RTi.DMI.NWSRFS_DMI.NWSRFS_Util;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJMenuItem;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.LanguageTranslator;
import RTi.Util.IO.ProcessManager;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

//public class NWSRFS_SystemMaintenance extends JFrame 
public class NWSRFS_SystemMaintenance extends JDialog implements ActionListener
{

String _class = "NWSRFS_SystemMaintenance";

//file seperator - set default to Unix and then 
//try and get it with a system call
String _fs = "/";

//editor to use for application - can be "vi", "nedit", or null
String _editor = null;

//dimension to size gui components
Dimension _comp_dim = new Dimension( 175, 25 );

//filler for making panels same size in horizontal direction
Dimension _filler_left_dim = new Dimension( 375, 0 );

//dimesion for output panel
Dimension _output_dim = new Dimension( 700, 400 );

//Font for titles
//Font _title_Font = new Font( "Monospaced", Font.BOLD, 12 );
Font _title_Font = new Font( "Arial", Font.BOLD, 12 );

//Strings and Buttons for GUI components
//Redefine Stations
String _redefStations_title_string = "Redefine Stations";
String _redefStations_subtitle_string = null;
String _redefStations_select_string = "Select Station to Redefine";
String _redefStations_edit_string = "Edit Station";
String _redefStations_run_string = "Run";
String _redefStations_close_string = "Close";
SimpleJButton _redefStations_edit_JButton = null;
SimpleJButton _redefStations_run_JButton = null;

//add RatingCurve
String _addRatingCurve_title_string = "Add Rating Curve";
String _addRatingCurve_add_string = "Add New Rating Curve";
String _addRatingCurve_run_string = "Run";
String _addRatingCurve_close_string = "Close";
SimpleJButton _addRatingCurve_add_JButton = null;
SimpleJButton _addRatingCurve_run_JButton = null;

//add Stations
String _addStations_title_string = "Add Stations";
String _addStations_add_string = "Add New Station";
String _addStations_run_string = "Run";
String _addStations_close_string = "Close";
SimpleJButton _addStations_add_JButton = null;
SimpleJButton _addStations_run_JButton = null;

//Redefine Segments
String _redefSegments_title_string = "Redefine Segment";
//The subtitle will be filled in using the item selected on the JTREE
String _redefSegments_subtitle_string = null;
String _redefSegments_label_string = "Selected in Tree";
String _redefSegments_edit_string = "Edit File";
String _redefSegments_viewCurrent_string = "View Current File";
String _redefSegments_run_string = "Run";
String _redefSegments_close_string = "Close";
SimpleJButton _redefSegments_edit_JButton = null;
SimpleJButton _redefSegments_run_JButton = null;

//Redefine Rating Curves
//do later
String _redefRatingCurves_title_string = "Redefine Rating Curves";
String _redefRatingCurves_subtitle_string = null;
String _redefRatingCurves_label_string = "Selected in Tree";
String _redefRatingCurves_edit_string = "Edit File";
String _redefRatingCurves_viewCurrent_string = "View Current File";
String _redefRatingCurves_run_string = "Run";
String _redefRatingCurves_close_string = "Close";
SimpleJButton _redefRatingCurves_edit_JButton = null;
SimpleJButton _redefRatingCurves_run_JButton = null;

//Preprocessors Database Status
String _preprocessDB_title_string = "Preprocessors Database Status";
String _preprocessDB_run_string = "Run";
String _preprocessDB_close_string = "Close";
SimpleJButton _preprocessDB_run_JButton = null;

//Forecast Database Status
String _forecastDB_title_string = "Forecast Database Status";
String _forecastDB_run_string = "Run";
String _forecastDB_close_string = "Close";
SimpleJButton _forecastDB_run_JButton = null;

//dump observations
String _dumpObs_title_string = "Dump Observations";
String _dumpObs_edit_string = "Edit File";
String _dumpObs_run_string = "Run";
String _dumpObs_close_string = "Close";
SimpleJButton _dumpObs_edit_JButton = null;
SimpleJButton _dumpObs_run_JButton = null;

//dump time series
String _dumpTS_title_string = "Dump Time Series";
String _dumpTS_edit_string = "Edit File";
String _dumpTS_run_string = "Run";
String _dumpTS_close_string = "Close";
SimpleJButton _dumpTS_edit_JButton = null;
SimpleJButton _dumpTS_run_JButton = null;

//output panel pieces
String _output_string = "Output from ofs Commands";
DefaultListModel _ListModel = null;
JList _output_JList = null;

//Popup menu
String _popup_view_string = "View File";
String _popup_clear_string = "Clear Output";
JPopupMenu  _popup_JPopupMenu = null;
SimpleJMenuItem _popup_view_JMenuItem = null;
SimpleJMenuItem _popup_clear_JMenuItem = null;

//make combo box global 
JComboBox _comboBox_JComboBox = null;

//make one generic close button
SimpleJButton _close_JButton = null;

//make the DIALOGs global. Only one is open at at time,
//and once it is closed, it is destroyed.
JDialog _dialog = null;

private NWSRFS_System_JTree __systemJTree = null;

/**
Constructor.
*/
public NWSRFS_SystemMaintenance() {
	this(null);
}

/**
Constructor.
@param systemJTree a system JTree that can be specified that will be rebuilt
after certain commands are run.
*/
public NWSRFS_SystemMaintenance(NWSRFS_System_JTree systemJTree)
{
	__systemJTree = systemJTree;

	_fs = File.separator;

 	//editor can be null - if it is null, use Java Text Editor,
        //SimpleJFileEditor... Other options for editor: "vi" or "nedit"
        _editor = IOUtil.getPropValue( "Editor" );

	//set up Strings to use for the GUI components if using a translation
	if ( LanguageTranslator.getTranslator() != null ) {	
		initialize_gui_strings();
	}

        //set up frame to do nothing on close so that we can
        //take total control of window closing events.
        //setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
	
} //end constructor

/**
Creates the JDialog box for Adding RatingCurves. 
*/
public void create_addRatingCurve_dialog() {
	String routine = _class + ".create_addRatingCurve_dialog";

	//_dialog = new JDialog( this, _addRatingCurve_title_string, true);
	_dialog = new JDialog();
	_dialog.setTitle( _addRatingCurve_title_string );
	_dialog.setModal( true );

	Insets insets = new Insets( 5, 5, 5, 5 );

	//make main panel to hold other panels
	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );

	//make title panel
	JPanel title_JPanel = null;
	title_JPanel = make_title_panel( _addRatingCurve_title_string );

	//make button panel
	JPanel button_JPanel = null;
	//make buttons
	_addRatingCurve_add_JButton = new SimpleJButton(
		_addRatingCurve_add_string,
		_addRatingCurve_add_string, this );
	
	_addRatingCurve_run_JButton = new SimpleJButton(
		_addRatingCurve_run_string,
		_addRatingCurve_run_string, this );
	_addRatingCurve_run_JButton.setEnabled( false );
	
	//make panel
	button_JPanel = make_button_panel(
		_addRatingCurve_add_JButton,
		_addRatingCurve_run_JButton,
		null );
		
	//make output panel
	JPanel output_JPanel = null;
	output_JPanel = make_output_panel();

	//make close panel
	JPanel close_JPanel = null;
	close_JPanel = make_close_panel( _addRatingCurve_close_string );
	
	//if any panel is null, don't assemble 
	if ( ( title_JPanel == null ) || ( button_JPanel == null ) ||
		( output_JPanel == null ) || ( close_JPanel == null ) )  {

		Message.printWarning( 2, routine, "Unable to create \"Add RatingCurve panel\"." );
	}
	else {
		try {
			//layout GUI
			int cnt = 0;
			//title
			JGUIUtil.addComponent(
				main_JPanel, title_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//button
			JGUIUtil.addComponent(
				main_JPanel, button_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//output
			JGUIUtil.addComponent(
				main_JPanel, output_JPanel,
				0, cnt, 1, 1, 1, 1, insets,
				GridBagConstraints.BOTH,
				GridBagConstraints.CENTER );
			cnt++;

			//close
			JGUIUtil.addComponent(
				main_JPanel, close_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;
		
			//now add main panel to dialog
			_dialog.getContentPane().add( "Center", main_JPanel );
			_dialog.pack();
			_dialog.setResizable( false );
			_dialog.setVisible( true );
			_dialog.validate();
		}
		catch ( Exception e ) {
			Message.printWarning( 2, routine, "Unable to create \"Add RatingCurve panel\"." );
			if ( Message.isDebugOn ) {
				Message.printWarning( 2, routine, e );
			}
		}
	}
	//clean up
	insets = null;
	
} //end create_addRatingCurve_dialog

/**
Creates the JDialog box for Adding Stations. 
*/
public void create_addStations_dialog() {
	String routine = _class + ".create_addStations_dialog";

	//_dialog = new JDialog( this, _addStations_title_string, true);
	_dialog = new JDialog();
	_dialog.setTitle( _addStations_title_string );
	_dialog.setModal( true );

	Insets insets = new Insets( 5, 5, 5, 5 );

	//make main panel to hold other panels
	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );

	//make title panel
	JPanel title_JPanel = null;
	title_JPanel = make_title_panel( _addStations_title_string );

	//make button panel
	JPanel button_JPanel = null;
	//make buttons
	_addStations_add_JButton = new SimpleJButton(
		_addStations_add_string,
		_addStations_add_string, this );
	
	_addStations_run_JButton = new SimpleJButton(
		_addStations_run_string,
		_addStations_run_string, this );
	_addStations_run_JButton.setEnabled( false );
	
	//make panel
	button_JPanel = make_button_panel(
		_addStations_add_JButton,
		_addStations_run_JButton,
		null );
		
	//make output panel
	JPanel output_JPanel = null;
	output_JPanel = make_output_panel();

	//make close panel
	JPanel close_JPanel = null;
	close_JPanel = make_close_panel( _addStations_close_string );
	
	//if any panel is null, don't assemble 
	if ( ( title_JPanel == null ) || ( button_JPanel == null ) ||
		( output_JPanel == null ) || ( close_JPanel == null ) )  {

		Message.printWarning( 2, routine, "Unable to create \"Add Stations panel\"." );
	}
	else {
		try {
			//layout GUI
			int cnt = 0;
			//title
			JGUIUtil.addComponent(
				main_JPanel, title_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//button
			JGUIUtil.addComponent(
				main_JPanel, button_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//output
			JGUIUtil.addComponent(
				main_JPanel, output_JPanel,
				0, cnt, 1, 1, 1, 1, insets,
				GridBagConstraints.BOTH,
				GridBagConstraints.CENTER );
			cnt++;

			//close
			JGUIUtil.addComponent(
				main_JPanel, close_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;
		
			//now add main panel to dialog
			_dialog.getContentPane().add( "Center", main_JPanel );
			_dialog.pack();
			_dialog.setResizable( false );
			_dialog.setVisible( true );
			_dialog.validate();
		}
		catch ( Exception e ) {
			Message.printWarning( 2, routine, "Unable to create \"Add Stations panel\"." );
			if ( Message.isDebugOn ) {
				Message.printWarning( 2, routine, e );
			}
		}
	}
	//clean up
	insets = null;
	
} //end create_addStations_dialog

/**
Creates the JDialog box for Dumping Observations.
*/
public void create_dumpObs_dialog() {
	String routine = _class + ".create_dumpObs_dialog";

	_dialog = new JDialog();
	_dialog.setTitle( _dumpObs_title_string );
	_dialog.setModal( true );

	Insets insets = new Insets( 5, 5, 5, 5 );

	//make main panel to hold other panels
	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );

	//make title panel
	JPanel title_JPanel = null;
	title_JPanel = make_title_panel( _dumpObs_title_string );

	//make button panel
	JPanel button_JPanel = null;
	//make buttons
	_dumpObs_edit_JButton = new SimpleJButton(
		_dumpObs_edit_string,
		_dumpObs_edit_string, this );
	_dumpObs_run_JButton = new SimpleJButton(
		_dumpObs_run_string,
		_dumpObs_run_string, this );
	//_dumpObs_run_JButton.setEnabled( true );

	//make panel
	button_JPanel = make_button_panel( _dumpObs_edit_JButton, _dumpObs_run_JButton, null );
		
	//make output panel
	JPanel output_JPanel = null;
	output_JPanel = make_output_panel();

	//make close panel
	JPanel close_JPanel = null;
	close_JPanel = make_close_panel( _dumpObs_close_string );
	
	//if any panel is null, don't assemble 
	if ( ( title_JPanel == null ) || ( button_JPanel == null ) ||
		( output_JPanel == null ) || ( close_JPanel == null ) )  {

		Message.printWarning( 2, routine, "Unable to create \"Dump Observations panel\"." );
	}
	else {
		try {
			//layout GUI
			int cnt = 0;
			//title
			JGUIUtil.addComponent(
				main_JPanel, title_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//button
			JGUIUtil.addComponent(
				main_JPanel, button_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//output
			JGUIUtil.addComponent(
				main_JPanel, output_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//close
			JGUIUtil.addComponent(
				main_JPanel, close_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;
		
			//now add main panel to dialog
			_dialog.getContentPane().add( "Center", main_JPanel );
			_dialog.pack();
			_dialog.setResizable( false );
			_dialog.setVisible( true );
			_dialog.validate();
		}
		catch ( Exception e ) {
			Message.printWarning( 2, routine,
			"Unable to create " +
			"\"Dump Observations panel\"." );
			if ( Message.isDebugOn ) {
				Message.printWarning( 2, routine, e );
			}
		}
	}
	//clean up
	insets = null;
	
} //end create_dumpObs_dialog

/**
Creates the JDialog box for Dumping TimeSeries.
*/
public void create_dumpTS_dialog() {
	String routine = _class + ".create_dumpTS_dialog";

	_dialog = new JDialog();
	_dialog.setTitle( _dumpTS_title_string );
	_dialog.setModal( true );

	Insets insets = new Insets( 5, 5, 5, 5 );

	//make main panel to hold other panels
	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );

	//make title panel
	JPanel title_JPanel = null;
	title_JPanel = make_title_panel( _dumpTS_title_string );

	//make button panel
	JPanel button_JPanel = null;
	//make buttons
	_dumpTS_edit_JButton = new SimpleJButton(
		_dumpTS_edit_string,
		_dumpTS_edit_string, this );
	_dumpTS_run_JButton = new SimpleJButton(
		_dumpTS_run_string,
		_dumpTS_run_string, this );
	//_dumpTS_run_JButton.setEnabled( true );

	//make panel
	button_JPanel = make_button_panel( _dumpTS_edit_JButton, _dumpTS_run_JButton, null );
		
	//make output panel
	JPanel output_JPanel = null;
	output_JPanel = make_output_panel();

	//make close panel
	JPanel close_JPanel = null;
	close_JPanel = make_close_panel( _dumpTS_close_string );
	
	//if any panel is null, don't assemble 
	if ( ( title_JPanel == null ) || ( button_JPanel == null ) ||
		( output_JPanel == null ) || ( close_JPanel == null ) )  {

		Message.printWarning( 2, routine, "Unable to create \"Dump Time Series panel\"." );
	}
	else {
		try {
			//layout GUI
			int cnt = 0;
			//title
			JGUIUtil.addComponent(
				main_JPanel, title_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//button
			JGUIUtil.addComponent(
				main_JPanel, button_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//output
			JGUIUtil.addComponent(
				main_JPanel, output_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//close
			JGUIUtil.addComponent(
				main_JPanel, close_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;
		
			//now add main panel to dialog
			_dialog.getContentPane().add( "Center", main_JPanel );
			_dialog.pack();
			_dialog.setResizable( false );
			_dialog.setVisible( true );
			_dialog.validate();
		}
		catch ( Exception e ) {
			Message.printWarning( 2, routine, "Unable to create \"Dump Time Series panel\"." );
			if ( Message.isDebugOn ) {
				Message.printWarning( 2, routine, e );
			}
		}
	}
	//clean up
	insets = null;
	
} //end create_dumpTS_dialog

/**
Creates the JDialog box for Running the Forecast Database Status.
*/
public void create_forecastDB_dialog() {
	String routine = _class + ".create_forecastDB_dialog";

	_dialog = new JDialog();
	_dialog.setTitle( _forecastDB_title_string );
	_dialog.setModal( true );

	Insets insets = new Insets( 5, 5, 5, 5 );

	//make main panel to hold other panels
	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );

	//make title panel
	JPanel title_JPanel = null;
	title_JPanel = make_title_panel( _forecastDB_title_string );

	//make button panel
	JPanel button_JPanel = null;
	//make buttons
	_forecastDB_run_JButton = new SimpleJButton(
		_forecastDB_run_string,
		_forecastDB_run_string, this );
	//make panel
	button_JPanel = make_button_panel( _forecastDB_run_JButton, null, null );
		
	//make output panel
	JPanel output_JPanel = null;
	output_JPanel = make_output_panel();

	//make close panel
	JPanel close_JPanel = null;
	close_JPanel = make_close_panel( _forecastDB_close_string );
	
	//if any panel is null, don't assemble 
	if ( ( title_JPanel == null ) || ( button_JPanel == null ) ||
		( output_JPanel == null ) || ( close_JPanel == null ) )  {

		Message.printWarning( 2, routine, "Unable to create \"Forecast DataBase Status panel\"." );
	}
	else {
		try {
			//layout GUI
			int cnt = 0;
			//title
			JGUIUtil.addComponent(
				main_JPanel, title_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//button
			JGUIUtil.addComponent(
				main_JPanel, button_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//output
			JGUIUtil.addComponent(
				main_JPanel, output_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//close
			JGUIUtil.addComponent(
				main_JPanel, close_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;
		
			//now add main panel to dialog
			_dialog.getContentPane().add( "Center", main_JPanel );
			_dialog.pack();
			_dialog.setResizable( false );
			_dialog.setVisible( true );
			_dialog.validate();
		}
		catch ( Exception e ) {
			Message.printWarning( 2, routine, "Unable to create \"Forecast Database Status panel\"." );
			if ( Message.isDebugOn ) {
				Message.printWarning( 2, routine, e );
			}
		}
	}
	//clean up
	insets = null;
	
} //end create_forecastDB_dialog

/**
Creates the JDialog box for Running the Preprocessor Database Status.
*/
public void create_preprocessDB_dialog() {
	String routine = _class + ".create_preprocessDB_dialog";

	_dialog = new JDialog();
	_dialog.setTitle( _preprocessDB_title_string );
	_dialog.setModal( true );

	Insets insets = new Insets( 5, 5, 5, 5 );

	//make main panel to hold other panels
	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );

	//make title panel
	JPanel title_JPanel = null;
	title_JPanel = make_title_panel( _preprocessDB_title_string );

	//make button panel
	JPanel button_JPanel = null;
	//make buttons
	_preprocessDB_run_JButton = new SimpleJButton(
		_preprocessDB_run_string,
		_preprocessDB_run_string, this );
	//make panel
	button_JPanel = make_button_panel( _preprocessDB_run_JButton, null, null );
		
	//make output panel
	JPanel output_JPanel = null;
	output_JPanel = make_output_panel();

	//make close panel
	JPanel close_JPanel = null;
	close_JPanel = make_close_panel( _preprocessDB_close_string );
	
	//if any panel is null, don't assemble 
	if ( ( title_JPanel == null ) || ( button_JPanel == null ) ||
		( output_JPanel == null ) || ( close_JPanel == null ) )  {

		Message.printWarning( 2, routine, "Unable to create \"Preprocessor DataBase Status panel\"." );
	}
	else {
		try {
			//layout GUI
			int cnt = 0;
			//title
			JGUIUtil.addComponent(
				main_JPanel, title_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//button
			JGUIUtil.addComponent(
				main_JPanel, button_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//output
			JGUIUtil.addComponent(
				main_JPanel, output_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//close
			JGUIUtil.addComponent(
				main_JPanel, close_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;
		
			//now add main panel to dialog
			_dialog.getContentPane().add( "Center", main_JPanel );
			_dialog.pack();
			_dialog.setResizable( false );
			_dialog.setVisible( true );
			_dialog.validate();
		}
		catch ( Exception e ) {
			Message.printWarning( 2, routine, "Unable to create \"Preprocessor Database Status panel\"." );
			if ( Message.isDebugOn ) {
				Message.printWarning( 2, routine, e );
			}
		}
	}
	//clean up
	insets = null;
	
} //end create_preprocessDB_dialog

///////////////
/**
Creates the JDialog box for Redefining Rating Curves.  
@param rc_to_redefine  Name of rating curve to redefine.
*/
public void create_redefRatingCurves_dialog( String ratingCurve_to_redefine ) {
	String routine = _class + ".create_redefRatingCurves";

	_dialog = new JDialog();
	_dialog.setTitle( _redefRatingCurves_title_string );
	_dialog.setModal( true );

	Insets insets = new Insets( 5, 5, 5, 5 );

	//make main panel to hold other panels
	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );

	//make title panel
	JPanel title_JPanel = null;
	//we have defined the title already,
	//define the SUBTITLE as a global.  This way, you can know which
	//rating curve was selected in the JTree by looking at the subtitle
	_redefRatingCurves_subtitle_string = ratingCurve_to_redefine;
	//make title panel with subtitle
	title_JPanel = make_title_panel( 
		_redefRatingCurves_title_string, 
		_redefRatingCurves_subtitle_string );

	//make button panel
	JPanel button_JPanel = null;
	//make buttons
	_redefRatingCurves_edit_JButton = new SimpleJButton(
		_redefRatingCurves_edit_string,
		_redefRatingCurves_edit_string, this );

	_redefRatingCurves_run_JButton = new SimpleJButton(
		_redefRatingCurves_run_string,
		_redefRatingCurves_run_string, this );

	//make panel
	button_JPanel = make_button_panel(
		_redefRatingCurves_edit_JButton,
		_redefRatingCurves_run_JButton, null );
		
	//make output panel
	JPanel output_JPanel = null;
	output_JPanel = make_output_panel();

	//make close panel
	JPanel close_JPanel = null;
	close_JPanel = make_close_panel( _redefRatingCurves_close_string );
	
	//if any panel is null, don't assemble 
	if ( ( title_JPanel == null ) || ( button_JPanel == null ) ||
		( output_JPanel == null ) || ( close_JPanel == null ) )  {

		Message.printWarning( 2, routine,
		"Unable to create \"Redefine Rating Curve panel\""  +
		"for Rating Curve: \"" + ratingCurve_to_redefine + "\"." );
	}
	else {
		try {
			//layout GUI
			int cnt = 0;
			//title
			JGUIUtil.addComponent(
				main_JPanel, title_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//button
			JGUIUtil.addComponent(
				main_JPanel, button_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//output
			JGUIUtil.addComponent(
				main_JPanel, output_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//close
			JGUIUtil.addComponent(
				main_JPanel, close_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;
		
			//now add main panel to dialog
			_dialog.getContentPane().add( "Center", main_JPanel );
			_dialog.pack();
			_dialog.setResizable( false );
			_dialog.setVisible( true );
			_dialog.validate();
		}
		catch ( Exception e ) {
			e.printStackTrace();
			Message.printWarning( 2, routine,
			"Unable to create \"Redefine Rating Curve panel\" for Rating Curve: \"" +
			ratingCurve_to_redefine + "\"." );
			if ( Message.isDebugOn ) {
				Message.printWarning( 2, routine, e );
			}
		}
	}
	//clean up
	insets = null;
} //end create_redefRatingCurves_dialog
	
/**
Creates the JDialog box for Redefining the Segment selected.
@param segment_to_redefine  Name of the Segment that needs to be redefined.
*/
public void create_redefSegment_dialog( String segment_to_redefine ) {
	String routine = _class + ".create_redefSegment_dialog";

	_dialog = new JDialog();
	_dialog.setTitle( _redefSegments_title_string );
	_dialog.setModal( true );

	Insets insets = new Insets( 5, 5, 5, 5 );

	//make main panel to hold other panels
	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );

	//make title panel
	JPanel title_JPanel = null;
	//we have defined the title already,
	//define the SUBTITLE as a global.  This way, you can know which
	//segment was selected in the JTree by looking at the subtitle
	_redefSegments_subtitle_string = segment_to_redefine;
	//make title panel with subtitle
	title_JPanel = make_title_panel( _redefSegments_title_string, _redefSegments_subtitle_string );

	//make button panel
	JPanel button_JPanel = null;
	//make buttons
	_redefSegments_edit_JButton = new SimpleJButton(
		_redefSegments_edit_string,
		_redefSegments_edit_string, this );

	_redefSegments_run_JButton = new SimpleJButton(
		_redefSegments_run_string,
		_redefSegments_run_string, this );

	//make panel
	button_JPanel = make_button_panel(
		_redefSegments_edit_JButton,
		_redefSegments_run_JButton, null );
		
	//make output panel
	JPanel output_JPanel = null;
	output_JPanel = make_output_panel();

	//make close panel
	JPanel close_JPanel = null;
	close_JPanel = make_close_panel( _redefSegments_close_string );
	
	//if any panel is null, don't assemble 
	if ( ( title_JPanel == null ) || ( button_JPanel == null ) ||
		( output_JPanel == null ) || ( close_JPanel == null ) )  {

		Message.printWarning( 2, routine,
		"Unable to create \"Redefine Segment panel\" for Segment: \"" + segment_to_redefine + "\"." );
	}
	else {
		try {
			//layout GUI
			int cnt = 0;
			//title
			JGUIUtil.addComponent(
				main_JPanel, title_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//button
			JGUIUtil.addComponent(
				main_JPanel, button_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//output
			JGUIUtil.addComponent(
				main_JPanel, output_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//close
			JGUIUtil.addComponent(
				main_JPanel, close_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;
		
			//now add main panel to dialog
			_dialog.getContentPane().add( "Center", main_JPanel );
			_dialog.pack();
			_dialog.setResizable( false );
			_dialog.setVisible( true );
			_dialog.validate();
		}
		catch ( Exception e ) {
			Message.printWarning( 2, routine, "Unable to create \"Redefine Segment panel\" for Segment: \"" +
			segment_to_redefine + "\"." );
			if ( Message.isDebugOn ) {
				Message.printWarning( 2, routine, e );
			}
		}
	}
	//clean up
	insets = null;
	
} //end create_redefSegment_dialog

/**
Creates the JDialog box for the Redefining Stations.  This dialog 
box consists of a JComboBox containing an alphabetical list of all the
stations.  It also contains buttons to edit the station file and run 
the new station file.  
@param stn_id Station name to redefine.
*/
public void create_redefStations_dialog( String stn_id ) {
	String routine = _class + ".create_redefStations_dialog";

	_dialog = new JDialog();
	_dialog.setTitle( _redefStations_title_string );
	_dialog.setModal( true );

	Insets insets = new Insets( 5, 5, 5, 5 );

	//make main panel to hold other panels
	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );

	//make title panel
	JPanel title_JPanel = null;
	//define the SUBTITLE as a global.  This way, you can know which
	//segment was selected in the JTree by looking at the subtitle
	_redefStations_subtitle_string = stn_id;

	title_JPanel = make_title_panel( _redefStations_title_string, _redefStations_subtitle_string );

	//make button panel
	JPanel button_JPanel = null;

	//make buttons to put in it.
	_redefStations_edit_JButton = new SimpleJButton(	
		_redefStations_edit_string,
		_redefStations_edit_string, this );

	_redefStations_run_JButton = new SimpleJButton(	
		_redefStations_run_string,
		_redefStations_run_string, this );
	//since this is the second in a series of steps, do not
	//enable button until first step completes successfully.
	_redefStations_run_JButton.setEnabled( false );

	button_JPanel = make_button_panel( _redefStations_edit_JButton,	_redefStations_run_JButton, null );

	//make output panel
	JPanel output_JPanel = null;
	output_JPanel = make_output_panel();

	//make close panel
	JPanel close_JPanel = null;
	close_JPanel = make_close_panel( _redefStations_close_string );

	//if any panel is null, do not assemble GUI
	if ( ( title_JPanel == null ) || ( button_JPanel == null ) ||
		( output_JPanel == null ) || ( close_JPanel == null ) ) {
		
		Message.printWarning( 2, routine, "Unable to create \"Redefine Stations panel\"." );
	}
	else {
		//layout GUI
		try {
			int cnt = 0;
			//title 
			JGUIUtil.addComponent(
				main_JPanel, title_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

		/*
			//combo 
			JGUIUtil.addComponent(
				main_JPanel, combo_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;
		*/

			//button 
			JGUIUtil.addComponent(
				main_JPanel, button_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//output 
			JGUIUtil.addComponent(
				main_JPanel, output_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;

			//close 
			JGUIUtil.addComponent(
				main_JPanel, close_JPanel,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;
				
			//now add main panel to dialog
			_dialog.getContentPane().add( "Center", main_JPanel );
			_dialog.pack();
			_dialog.setResizable( false );
			_dialog.setVisible( true );
			_dialog.validate();
		}
		catch ( Exception e ) {
			Message.printWarning( 2, routine, "Unable to create \"Redefine Stations panel\"." );
			if ( Message.isDebugOn ) {
				Message.printWarning( 2, routine, e );
			}
		}
	}

	//clean up
	insets = null;
	
} //end create_redefStations_dialog

/**
This method is used to get the strings needed for labelling all the GUI
components only if a lookup table is used for the application, ie, if
there is a translation file.  If the String cannot be located in the
translation file, the original string will be utilized in non-translated form.
*/
public void initialize_gui_strings()
{
	LanguageTranslator translator = null;
	translator = LanguageTranslator.getTranslator();
	if ( translator != null ) {
		//Redefine Stations
		_redefStations_title_string = 
			translator.translate( "redefStations_title_string", _redefStations_title_string );
		_redefStations_select_string = 
			translator.translate( "redefStations_select_string", _redefStations_select_string );
		_redefStations_edit_string =
			translator.translate( "redefStations_edit_string", _redefStations_edit_string );
		_redefStations_run_string =
			translator.translate( "redefStations_run_string", _redefStations_run_string );
		_redefStations_close_string = 
			translator.translate( "redefStations_close_string", _redefStations_close_string );

		//add RatingCurve
		_addRatingCurve_title_string = 
			translator.translate( "addRatingCurve_title_string", _addRatingCurve_title_string );
		_addRatingCurve_add_string = 
			translator.translate( "addRatingCurve_add_string", _addRatingCurve_add_string );
		_addRatingCurve_run_string = 
			translator.translate( "addRatingCurve_run_string", _addRatingCurve_run_string );
		_addRatingCurve_close_string = 
			translator.translate( "addRatingCurve_close_string", _addRatingCurve_close_string );
	
		//add Stations
		_addStations_title_string = 
			translator.translate( "addStations_title_string", _addStations_title_string );
		_addStations_add_string = 
			translator.translate( "addStations_add_string", _addStations_add_string );
		_addStations_run_string = 
			translator.translate( "addStations_run_string", _addStations_run_string );
		_addStations_close_string = 
			translator.translate( "addStations_close_string", _addStations_close_string );
	
		//Redefine Segments
		_redefSegments_title_string = 
			translator.translate( "redefSegments_title_string", _redefSegments_title_string );
		_redefSegments_label_string = 
			translator.translate( "redefSegments_label_string", _redefSegments_label_string );
		_redefSegments_edit_string = 
			translator.translate( "redefSegments_edit_string", _redefSegments_edit_string );
		_redefSegments_viewCurrent_string = 
			translator.translate( "redefSegments_viewCurrent_string", _redefSegments_viewCurrent_string );
		_redefSegments_run_string = 
			translator.translate( "redefSegments_run_string", _redefSegments_run_string );
		_redefSegments_close_string = 
			translator.translate( "redefSegments_close_string", _redefSegments_close_string );
	
		//Redefine Rating Curves
		_redefRatingCurves_title_string = 
			translator.translate("redefRatingCurves_title_string", _redefRatingCurves_title_string );
		_redefRatingCurves_label_string = 
			translator.translate("redefRatingCurves_label_string",_redefRatingCurves_label_string );
		_redefRatingCurves_edit_string = 
			translator.translate("redefRatingCurves_edit_string",_redefRatingCurves_edit_string );
		_redefRatingCurves_viewCurrent_string = 
			translator.translate("redefRatingCurves_viewCurrent_string",_redefRatingCurves_viewCurrent_string );
		_redefRatingCurves_run_string = 
			translator.translate("redefRatingCurves_run_string",_redefRatingCurves_run_string );
		_redefRatingCurves_close_string = 
			translator.translate("redefRatingCurves_close_string",_redefRatingCurves_close_string );
	
		//Preprocessors Database Status
		_preprocessDB_title_string = 
			translator.translate("preprocessDB_title_string",_preprocessDB_title_string );
		_preprocessDB_run_string = 
			translator.translate("preprocessDB_run_string",_preprocessDB_run_string );
		_preprocessDB_close_string = 
			translator.translate("preprocessDB_close_string",_preprocessDB_close_string );
	
		//Forecast Database Status
		_forecastDB_title_string = 
			translator.translate("forecastDB_title_string",_forecastDB_title_string );
		_forecastDB_run_string = 
			translator.translate("forecastDB_run_string",_forecastDB_run_string );
		_forecastDB_close_string = 
			translator.translate("forecastDB_close_string",_forecastDB_close_string );
	
		//dump observations
		_dumpObs_title_string = 
			translator.translate("dumpObs_title_string",_dumpObs_title_string );
		_dumpObs_edit_string = 
			translator.translate("dumpObs_edit_string",_dumpObs_edit_string );
		_dumpObs_run_string = 
			translator.translate("dumpObs_run_string",_dumpObs_run_string );
		_dumpObs_close_string = 
			translator.translate("dumpObs_close_string", _dumpObs_close_string );
	
		//dump time series
		_dumpTS_title_string = 
			translator.translate("dumpTS_title_string",_dumpTS_title_string );
		_dumpTS_edit_string = 
			translator.translate("dumpTS_edit_string",_dumpTS_edit_string );
		_dumpTS_run_string = 
			translator.translate("dumpTS_run_string",_dumpTS_run_string );
		_dumpTS_close_string = 
			translator.translate("dumpTS_close_string",_dumpTS_close_string );
	
		//popup menu
        	_popup_view_string =
			translator.translate("popup_view_string", _popup_view_string );
        	_popup_clear_string =
			translator.translate("popup_clear_string", _popup_clear_string );
	}	
} //end initialize_gui_strings

/**
Creates a panel that holds up to 3 buttons stacked vertically.
The 2nd and 3rd buttons may not be created if the objects
passed in for them are null.
@param button_1_JButton  1st SimpleJButton to layout.
@param button_2_JButton  2nd SimpleJButton to layout.
@param button_3_JButton  bottom SimpleJButton.
@return  panel containing the SimpleJButtons.
*/
public JPanel make_button_panel( SimpleJButton button_1_JButton,
					SimpleJButton button_2_JButton,	SimpleJButton button_3_JButton ) {
	String routine = _class + ".make_button_panel";

	JPanel button_JPanel = null;
	Insets insets = new Insets( 5, 3, 3, 5 );
	try { 
		button_JPanel = new JPanel(); 
		button_JPanel.setLayout( new GridBagLayout() );

		//now layout buttons 
		if ( button_1_JButton == null ) {
			Message.printWarning( 2, routine, "Unable to create button panel." );
			button_JPanel = null;
		}
		else {
			int cnt = 0;
			button_1_JButton.setPreferredSize( _comp_dim );
			JGUIUtil.addComponent( 
				button_JPanel, button_1_JButton,
				0, cnt, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
			cnt++;
			if ( button_2_JButton != null ) {
				button_2_JButton.setPreferredSize( _comp_dim );
				JGUIUtil.addComponent( 
					button_JPanel, button_2_JButton,
					0, cnt, 1, 1, 1, 0, insets,
					GridBagConstraints.NONE,
					GridBagConstraints.CENTER );
				cnt++;
				if ( button_3_JButton != null ) {
					button_3_JButton.setPreferredSize( 
					_comp_dim );
					JGUIUtil.addComponent( 
						button_JPanel, 
						button_3_JButton,
						0, cnt, 1, 1, 1, 0, insets,
						GridBagConstraints.NONE,
						GridBagConstraints.CENTER );
					cnt++;
				}
			}
		}
	}
	catch ( Exception e ) {
		button_JPanel = null;
		Message.printWarning( 2, routine, "Unable to create Button Panel." );
		if ( Message.isDebugOn ) {
			Message.printWarning( 2, routine, e );
		}
	}

	//clean up
	insets = null;
	
	return button_JPanel;
} //end make_button_panel
	
/**
Creates a panel to place at the bottom of GUIs that contains tne button- the "close" button.    
@param close_string  String to use for button.
@return  JPanel containing the "close" button.
*/
public JPanel make_close_panel( String close_string ) {
	String routine = _class + ".make_close_panel";
		
	JPanel close_JPanel = null;
	_close_JButton = null;

	//insets to use: top,left,bottom,right
	Insets insets = new Insets( 5, 3, 10, 3 );

	try {
		close_JPanel = new JPanel();
		close_JPanel.setLayout( new GridBagLayout() );

		//now create the button
		_close_JButton = new SimpleJButton( close_string, close_string, this );
		//set size
		_close_JButton.setPreferredSize( _comp_dim );

		//add button to center of panel
		JGUIUtil.addComponent(
			close_JPanel, _close_JButton,
			0, 0, 1, 1, 1, 0, insets,
			GridBagConstraints.NONE,
			GridBagConstraints.CENTER );
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, "Error laying out Close panel." );
		close_JPanel = null;
		_close_JButton = null;
		if ( Message.isDebugOn ) {
			Message.printWarning( 2, routine, e );
		}
	}

	insets = null;

	return close_JPanel;
} //end make_close_panel

/**
Creates a panel that contains a JComboBox with a JLabel beside it.
@param label_string  String for JLabel that is next to the combo box.
@param command_string  Command to run through the process manager to
get the list of items to display in the JComboBox.
return - panel containing the JLabel and JComboBox.
*/
public JPanel make_combobox_panel( String label_string, String command_string ) {
	String routine = _class + ".make_combobox_panel";
	
	JPanel combo_JPanel = null;
	Insets insets = new Insets( 3, 5, 5, 3 );
	//make sure combo box is null since it is a global.
	_comboBox_JComboBox = null;

	try { 
		combo_JPanel = new JPanel();
		combo_JPanel.setLayout( new GridBagLayout() );

		//make label
		JLabel select_JLabel = new JLabel( label_string	);
	
		//make vector that will go into combobox
		int exitstat = -99;
		Vector v = null;
		//ProcessManager pm = new ProcessManager( command_string, true, 0 );
		ProcessManager pm = new ProcessManager( command_string );
		try {
			pm.saveOutput( true );
			pm.run();
			v = pm.getOutputVector();
			exitstat = pm.getExitStatus();
			if ( exitstat == 0 ) {
				//clean up vector
				int size = 0;
				if ( v != null ) {
					size = v.size();
				}
				//if ( size > 0 ) {
					//remove last line that is the exit status line.
				//	v.removeElementAt( size - 1 );
					//get new size
				//	size = v.size();
				//}
				//now sort alphabetically	
				Vector sorted_list = null;
				sorted_list = StringUtil.sortStringList( v ); 
				
				//now create combo
				if ( sorted_list != null ) {
					size = sorted_list.size();
				}
				if ( size <= 0 ) {
					Message.printWarning( 2, routine, "Nothing returned from command: \"" +
							command_string + "\".  Combo box will not be created." );
				}
				else { //size > 0
					_comboBox_JComboBox = new JComboBox( sorted_list );	
				}
				//clean up 
				sorted_list = null;
			}
			else {
				Message.printWarning( 2, routine, "Unable to run command: \"" + command_string + "\" " +
				"which is used to create the combo box.  Will not create panel." ); 
			}
		} 
		catch ( Exception e ) {
			combo_JPanel = null;

			Message.printWarning( 2, routine, "Unable to create ComboBox Panel." );
			if ( Message.isDebugOn ) {
				Message.printWarning( 2, routine, e );
			}
		}

		//now check to see if combo box was created... if not, just exit returning a null panel.
		if ( _comboBox_JComboBox == null ) {
			combo_JPanel = null;
		}
		else {
			//assemble panel
			//add label
			JGUIUtil.addComponent(
				combo_JPanel, select_JLabel,
				0, 0, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.WEST );
			//add combo box
			JGUIUtil.addComponent(
				combo_JPanel, _comboBox_JComboBox,
				1, 0, 1, 1, 1, 0, insets,
				GridBagConstraints.NONE,
				GridBagConstraints.WEST );
		}
		
		v = null;
		pm = null;
		select_JLabel = null;

	}
	catch ( Exception e ) {
		combo_JPanel = null;

		Message.printWarning( 2, routine, "Unable to create ComboBox Panel." );
		if ( Message.isDebugOn ) {
			Message.printWarning( 2, routine, e );
		}
	}

	//clean up
	insets = null;

	return combo_JPanel;
} //end make_combobox_panel

/**
Creates a JPopupMenu and adds SimpleJMenuItems to it.
@return  JPopupMenu created.
*/
public JPopupMenu make_sysMaint_popup_menu() {
	//make popup
	JPopupMenu popup_menu = new JPopupMenu();
	JPopupMenu.setDefaultLightWeightPopupEnabled( false );
	
	//create the menu items
	_popup_view_JMenuItem = new SimpleJMenuItem(
		_popup_view_string,
		_popup_view_string, this );
	_popup_clear_JMenuItem = new SimpleJMenuItem(
		_popup_clear_string,
		_popup_clear_string, this );

	//add menu items to menu
	popup_menu.add( _popup_view_JMenuItem );
	popup_menu.add( _popup_clear_JMenuItem );

	return popup_menu;
	
} //end make_sysMaint_popup_menu

/**
Creates a panel and puts a title centered in the panel.
@param title_string  String to use for title
@return panel containing the title.
*/
public JPanel make_title_panel( String title_string ) {
	String routine = _class + ".make_title_panel";
	
	JPanel title_JPanel = null;
	try { 
		//make title panel
		title_JPanel = new JPanel();
		title_JPanel.setLayout( new GridBagLayout() );
	
		JLabel title_JLabel = new JLabel( title_string );
		title_JLabel.setFont( _title_Font );

		//put title in panel
		JGUIUtil.addComponent(
			title_JPanel, title_JLabel,
			0, 0, 1, 1, 1, 0,
			GridBagConstraints.NONE,
			GridBagConstraints.CENTER );


		//clean up
		title_JLabel = null;
	}
	catch ( Exception e ) {
		title_JPanel = null;

		Message.printWarning( 2, routine, "Unable to create Title Panel." );
		if ( Message.isDebugOn ) {
			Message.printWarning( 2, routine, e );
		}
	}
	return title_JPanel;
} //end make_title_panel

/**
Creates a panel and puts a title centered in the panel and a subtitle
below the title, also centered.
@param title_string  String to use for title.
@param subtitle_string  String to use for subtitle.
@return panel containing the title.
*/
public JPanel make_title_panel( String title_string, String subtitle_string ) {
	String routine = _class + ".make_title_panel";
	
	JPanel title_JPanel = null;
	try { 
		//make title panel
		title_JPanel = new JPanel();
		title_JPanel.setLayout( new GridBagLayout() );
	
		JLabel title_JLabel = new JLabel( title_string );
		title_JLabel.setFont( _title_Font );

		JLabel subtitle_JLabel = new JLabel( subtitle_string );
		subtitle_JLabel.setFont( _title_Font );

		//put title in panel
		JGUIUtil.addComponent(
			title_JPanel, title_JLabel,
			0, 0, 1, 1, 1, 0,
			GridBagConstraints.NONE,
			GridBagConstraints.CENTER );

		//add subtitle
		JGUIUtil.addComponent(
			title_JPanel, subtitle_JLabel,
			0, 1, 1, 1, 1, 0,
			GridBagConstraints.NONE,
			GridBagConstraints.CENTER );

		//clean up
		title_JLabel = null;
		subtitle_JLabel = null;
	}
	catch ( Exception e ) {
		title_JPanel = null;

		Message.printWarning( 2, routine, "Unable to create Title Panel." );
		if ( Message.isDebugOn ) {
			Message.printWarning( 2, routine, e );
		}
	}

	return title_JPanel;

} //end create_title_panel

/**
Creates and sets up the output JPanel where the output from 
running commands is displayed.
@return JPanel containing the output window.
*/
public JPanel make_output_panel() {
	String routine = _class + ".make_output_panel";

	JPanel output_JPanel = null;
	
	try {
		output_JPanel = new JPanel();
		//output_JPanel.setLayout( new GridBagLayout() );
		output_JPanel.setLayout( new BorderLayout() );

		//create list model
		_ListModel = new DefaultListModel();

		//create list and add model to it
		_output_JList = new JList( _ListModel );
		_output_JList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

		//make popup menu
		if (_output_JList != null ) {
			_popup_JPopupMenu = make_sysMaint_popup_menu();
		}
		if ( _popup_JPopupMenu != null ) {
			//add it to this panel
			output_JPanel.add( _popup_JPopupMenu );
		}
		else {
			Message.printWarning( 2, routine, "Unable to add a Popup Menu to the output window." );
		}

		//define a MouseListener for the output_JList window
		//to display a JPopupMenu when the popup trigger occurs
		_output_JList.addMouseListener(
		new MouseAdapter() {
			String routine = _class + ".make_output_panel"+ ".MouseAdapter";

			//mousePRESSED
			public void mousePressed( MouseEvent e ) {
				int mods = e.getModifiers();
				Component c = e.getComponent();
		
				if( c.equals( _output_JList ) && ( _output_JList.getFirstVisibleIndex() > -1 ) &&
					((mods & MouseEvent.BUTTON3_MASK) != 0 ) ) {
					if ( Message.isDebugOn ) {
						Message.printDebug ( 20, routine, "Calling show()" );
					}
	
					if ( _popup_JPopupMenu.isPopupTrigger(e) ) {
						_popup_JPopupMenu.show(	e.getComponent(), e.getX(), e.getY() );
						}//end if
				}//end if
				
			}//end mousePressed()
	
			//mouseRELEASED
			public void mouseReleased( MouseEvent e ) {
				int mods = e.getModifiers();
				Component c = e.getComponent();
				
				if( c.equals( _output_JList ) && ( _output_JList.getFirstVisibleIndex() > -1 ) &&
					((mods & MouseEvent.BUTTON3_MASK) != 0 ) ) {
					if ( Message.isDebugOn ) {
						Message.printDebug ( 20, routine, "Calling show()" );
					}
	
					if ( _popup_JPopupMenu.isPopupTrigger(e) ) {
						_popup_JPopupMenu.show(	e.getComponent(), e.getX(), e.getY() );
					}//end if
				}//end if
			}//end mousePressed()
		}//end mouseAdapter
		); //end addMouseListener		

		//make scroll pane and put list in it
		JScrollPane output_JScrollPane = new JScrollPane( _output_JList );
 		output_JScrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
        	output_JScrollPane.setPreferredSize( _output_dim );
        	output_JScrollPane.setMinimumSize( new Dimension( 500, 300) );
        	output_JScrollPane.setMaximumSize( new Dimension( 900, 500) );
	
        	output_JScrollPane.setBorder(
                	BorderFactory.createCompoundBorder(
                       		BorderFactory.createCompoundBorder(
                               		BorderFactory.createTitledBorder( _output_string ),
                                BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) ),
                	output_JScrollPane.getBorder() ) );

		//now add to layout
		output_JPanel.add( output_JScrollPane, BorderLayout.CENTER );
	/*
		JGUIUtil.addComponent( 
			output_JPanel, output_JScrollPane, 
			0, 0, 1, 1, 0, 0, insets, 
			GridBagConstraints.BOTH, 
			GridBagConstraints.CENTER );
	*/
		
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, "Error laying out Output panel." );

		output_JPanel = null;
		_output_JList = null;
		_ListModel = null;
		if ( Message.isDebugOn ) {
			Message.printWarning( 2, routine, e );
		}
	}
	return output_JPanel;
} //end make_output_panel

/////////////////////
/**
@param ratingCurve_id  ID of the Rating Curve to redefine.
@return boolean indicating if all the steps completed successfully or not.
*/
public boolean run_redefRatingCurves_edit_commands( String ratingCurve_id ) {
	String routine = _class + ".run_redefRatingCurves_edit_commands";
	String message;

	//boolean that indicates if file editing and opening was successful.
	boolean file_moved = true;

	if ( ratingCurve_id == null ) {
		return false;
	}
	else {	
		//first need to edit the PUNCHRC.GUI file- update it
		//with the selected Rating Curve and then run the PUNCHRC.GUI ofs command.
		Vector punch_vect = null;
		punch_vect = NWSRFS_Util.run_punch_ratingCurves( ratingCurve_id );

		//update output window		
		int exitstat = -999;
		exitstat = updateOutputWindow( "PUNCHRC.GUI", punch_vect );

		//make sure the PUNCHRC.GUI command was successful
		if ( exitstat == 0 ) {
			//output from that command 
			//(fcinit_pun.timestamp)
			//must be moved from the ofs_output 
			//directory to the input 
			//directory .../fcinit/  and renamed to
			//DEFRC.GUI.
		
			//In order to locate that "_pun" file, 
			//use the output we have for the 
			//"fcinit_log.timestamp" file.  The "_pun" file
			//will have same NAME, PATH, and TIMESTAMP, except
			//the "_log" is replaced with "_pun".

			//we need the line that has the 
			//log output file. This line should
			//be the 2nd to last line, but to 
			//be safe, go thru and find line with
			//"==" since the line is formatted 
			//with a "==>" at the beginning and 
			// a "<==" at the end.
			int size =0;
			size = punch_vect.size();
			String log_name = null;
			String s = null;
			for ( int i=0; i<size; i++ ) {
				s = ((String) punch_vect.elementAt(i)).trim();
				if ( s.regionMatches(true, 0, "==", 0, 2) ) {
					log_name = (String)
					punch_vect.elementAt( i );
					break;
				}
			}//end loop
			if ( log_name != null ) {
				//Remove extra "==>" and "<==" chars
				//and change "_log" to "_pun"
				String pun_path = null;
				pun_path = NWSRFS_Util.get_pun_path_from_log( log_name );

				//now we have the file we need to move and rename first get path to DEFRC.GUI
				String defrc_path = null;
				defrc_path = IOUtil.getPropValue( "DEFRC.GUI" );
				if ( defrc_path == null ) {
					file_moved = false;
				}
				else {
					// ( defrc_path != null ) 
					File pun_file = new File( pun_path );
					File redefrc_file = new File( defrc_path );
					if (( pun_file.canRead()) && ( redefrc_file.canRead() )) {

						//actually move file
						try {
							pun_file.renameTo( redefrc_file );

							Message.printStatus( 30, routine,
							"Moved file: \"" + pun_path + "\" to: \"" + defrc_path +"\"");	
						}
						catch( Exception e ) {
							file_moved = false;
							if ( Message.isDebugOn ) {
								Message.printWarning( 2, routine, e );
							}
						}
					} 
					else { // file(s) don't exist
						file_moved = false;
					}
				}
				if ( file_moved ) {
					//DEFRC.GUI is in place,

					//Before you let the user edit 
					//this file, you need to add
					//one line to the beginning of the
					//file and one line to the end:
					//first line: "DEF-RC"
					//last line: "END"
					// ADD LINES:
					File inputFile = new File( defrc_path );
					File outputFile = new File ( defrc_path + ".tmp" );

					try {
						FileInputStream fis = new FileInputStream( inputFile );
						InputStreamReader isr =	new InputStreamReader( fis );
						BufferedReader br = new BufferedReader( isr );
						FileOutputStream fos = new FileOutputStream( outputFile );
						PrintWriter pw = new PrintWriter( fos ); 

						//vector to hold all lines of file
						Vector v = new Vector();
						do {
							//store lines in vector as read them in
							s = br.readLine();
							if ( s== null ) {
								//no more lines
								break;
							}
							else {
								v.addElement( s );
							}
						} while ( s != null );
						//now have all lines.. 
						//add line to beg and end.
						v.insertElementAt("DEF-RC", 0 );
						v.addElement( "END" );
					
						//now print out.
						for(int i=0; i<v.size(); i++) {
							String line = (String)
							v.elementAt( i );

							pw.println( line );
							pw.flush(); 
						}
						br.close();
						pw.close();
						fis = null;
						isr = null;
						br = null;
						fos = null;
						pw = null;
					}
					catch( Exception e ) {
						file_moved = false;
						if ( Message.isDebugOn ) {
							Message.printWarning( 2, routine, e );
						}
					}
					//move output "tmp" file back
					try {
						outputFile.renameTo( inputFile );
					}
					catch ( Exception e ) {
						file_moved = false;
						if ( Message.isDebugOn ) {
							Message.printWarning( 2, routine, e );
						}
					}
				}	

				if ( file_moved ) { 
					//Finally Allow User to EDIT the File.
					try {
						NWSRFS_Util.runEditor( _editor, defrc_path, true );
					}
					catch ( Exception e ) {
						Message.printWarning( 2, routine, e );
					}
				}				
				else {  // if ( !file_moved )
					message = "Cannot update the file: \"DEFRC.GUI\". with " +
					"the latest punch file. Will not run the ofs command \"DEFRC.GUI\".";
					Message.printWarning ( 2, routine, message );
					_ListModel.addElement( message );
				}
			} //end if log_name != null
			else {
				Message.printWarning( 2, routine, "Unable to run the \"DEFRC.GUI\" command " );
				if ( Message.isDebugOn) {
					Message.printDebug( 5, routine,
					"Unable to locate the " +
					"\"fcinit_log.timestamp\" file " +
					"in the output from the " +
					"\"PUNCHRC.GUI\" command. " +
					"Can not run " +
					"\"DEFRC.GUI\" command " +
					"without moving the file " +
					"identified by that \"_log\" file, " +
					"but named, " +
					"\"fcinit_pun.timestamp\" to the " +
					"\"input directory \"..fcinit\" " +
					"and renaming the \"_pun\" file " +
					"\"DEFRC.GUI\"." );
				}
			}
		}
		else {
			Message.printWarning( 2, routine,
			"\"PUNCHRC.GUI\" command failed.  Will not run \"DEFRC.GUI\" command" );
			_ListModel.addElement( "PUNCHRC.GUI Failed!" );
		}
	}
	return file_moved;
} //end run_redefRatingCurve_edit_commands

/**
@param segment_id  ID of the Segment to redefine.
@return boolean indicating if all the steps completed successfully or not.
*/
public boolean run_redefSegments_edit_commands( String segment_id ) {
	String routine = _class + ".run_redefSegments_edit_commands";
	String message;

	//boolean that indicates if file editing and opening was successful.
	boolean file_moved = true;

	if ( segment_id == null ) {
		return false;
	}
	else {	
		//first need to edit the PUNCHSEGS.GUI file- update it
		//with the selected Segment and then run the PUNCHSEGS.GUI ofs command.
		Vector punch_vect = null;
		punch_vect = NWSRFS_Util.run_punch_segments( segment_id );
	/////////
		//update output window		
		int exitstat = -999;
		exitstat = updateOutputWindow( "PUNCHSEGS.GUI", punch_vect );

		//make sure the PUNCHSEGS.GUI command was successful
		if ( exitstat == 0 ) {
			//output from that command 
			//(fcinit_pun.timestamp)
			//must be moved from the ofs_output 
			//directory to the input 
			//directory .../fcinit/  and renamed to
			//RESEGDEF.GUI.
		
			//In order to locate that "_pun" file, 
			//use the output we have for the 
			//"fcinit_log.timestamp" file.  The "_pun" file
			//will have same NAME, PATH, and TIMESTAMP, except
			//the "_log" is replaced with "_pun".

			//we need the line that has the 
			//log output file. This line should
			//be the 2nd to last line, but to 
			//be safe, go thru and find line with
			//"==" since the line is formatted 
			//with a "==>" at the beginning and 
			// a "<==" at the end.
			int size =0;
			if ( punch_vect != null  ) {
				size = punch_vect.size();
			}
			String log_name = null;
			String s = null;
			for ( int i=0; i<size; i++ ) {
				s = ((String) punch_vect.elementAt(i)).trim();
				if (( s!= null ) && ( s.regionMatches(true, 0, "==", 0, 2) )) {
					log_name = (String)
					punch_vect.elementAt( i );
					break;
				}
			}//end loop
			if ( log_name != null ) {
				//Remove extra "==>" and "<==" chars and change "_log" to "_pun"
				String pun_path = null;
				pun_path = NWSRFS_Util.get_pun_path_from_log( log_name );

				// Now we have the file we need to move and rename first get path to RESEGDEF.GUI
				String redef_segs_path = null;
				redef_segs_path = IOUtil.getPropValue( "RESEGDEF.GUI" );
				if ( redef_segs_path == null ) {
					message = "Unable to locate \"RESEGDEF.GUI\" file";
					Message.printWarning( 2, routine, message );
					_ListModel.addElement( message );

					file_moved = false;
				}
				else { // ( redef_stns_path != null ) 
					File pun_file = new File( pun_path );
					File redefsegs_file = new File( redef_segs_path );
					if (( pun_file.canRead()) && ( redefsegs_file.canRead() )) {

						//actually move file
						try {
							pun_file.renameTo( redefsegs_file );

							Message.printStatus( 
							30, routine, "Moved file: \"" +	pun_path + "\" to: \"" +
							redef_segs_path +"\"");	
						}
						catch( Exception e ) {
							file_moved = false;
							message = "Unable to rename punch file to: \"RESEGDEF.GUI\" file";
							Message.printWarning( 2, routine, message );
							_ListModel.addElement( message );
							Message.printWarning( 2, routine, e );
						}
					}
					else { // file(s) don't exist
						//if (( pun_file.canRead()) && 
						//( redefsegs_file.canRead() )) {
						message = "Unable to locate punch file to rename it to: \"RESEGDEF.GUI\" file";
						Message.printWarning( 2, routine, message );
						_ListModel.addElement( message );
						file_moved = false;
					}
				}
				if ( !file_moved ) {
					message = "Cannot update the file: \"RESEGDEF.GUI\". with " +
						"the latest punch file. Will not run the ofs command \"RESEGDEF.GUI\".";
					Message.printWarning ( 2, routine, message );
					_ListModel.addElement( message );
				}
				if ( file_moved ) {
					//Need to add: "RESEGDEF" to top line of file:
					//boolean editOK = NWSRFS_Util.update_resegdef_file( redef_segs_path );
					
					//RESEGDEF.GUI is in place.  Allow User to EDIT the File.
					//Finally Allow User to EDIT the File.
					try {
						NWSRFS_Util.runEditor( _editor,	redef_segs_path, true );
					}
					catch ( Exception e ) {
						Message.printWarning( 2, routine, e );
					}
				}				
			} //end if log_name != null
			else {
				Message.printWarning( 2, routine, "Unable to run the \"RESEGDEF.GUI\" command " );
				if ( Message.isDebugOn) {
					Message.printDebug( 5, routine,
					"Unable to locate the " +
					"\"fcinit_log.timestamp\" file " +
					"in the output from the " +
					"\"PUNCHSEGS.GUI\" command. " +
					"Can not run " +
					"\"RESEGDEF.GUI\" command " +
					"without moving the file " +
					"identified by that \"_log\" file, " +
					"but named, " +
					"\"fcinit_pun.timestamp\" to the " +
					"\"input directory \"..fcinit\" " +
					"and renaming the \"_pun\" file " +
					"\"RESEGDEF.GUI\"." );
				}
			}
		}
		else {
			message = "\"PUNCHSEGS.GUI\" command failed.  Will not run \"RESEGDEF.GUI\" command";
			Message.printWarning( 2, routine, message );
			_ListModel.addElement( message );
		}
	}
	return file_moved;
} //end run_redefSegments_edit_commands

/**
Iterates through the steps needed to open up a file for the user
to edit and redefine a station.  In order to create the file that
is opened for editing, there are several steps that need to be 
completed. <P><PRE><UL> 
<LI>get Stations that the GUI was created for</LI>
<LI>edit the PUNCH.STATIONS.GUI file by adding the selected Station ID</LI>
<LI>run OFS PUNCH.STATIONS.GUI command </LI>
<LI>use the output _log information to locate the output _pun file </LI>
<LI>open the _pun file for editing by the user </LI>
</UL></P></PRE>
@return boolean indicating if all the steps completed successfully or not.
*/
public boolean run_redefStations_edit_commands() {
	String routine = _class + ".run_redefStations_edit_commands";
	String message;

	//indicates if all file manipulations went well.
	boolean file_moved = true;

	//need to get station selected by getting the SUBTITLE STRING!
	String station_sel_name = null;
	station_sel_name = _redefStations_subtitle_string;

	//now have to run some commands in the background.
	//first, edit the PUNCH.STATIONS.GUI file by 
	//replacing the Station ID in the first line of 
	//the file with the id of the stn selected in the

	String station_sel_ID = null;
	station_sel_ID = station_sel_name.trim();
	Message.printStatus( 55, routine, "ID of station selected: \"" + station_sel_ID + "\"." );

	//now we have the station id... edit the 
	//PUNCH.STATIONS.GUI file with this station ID
	//And run the OFS PUNCH.STATIONS.GUI command.
	Vector punch_vect = null;
	punch_vect = NWSRFS_Util.run_punch_stations( 
	station_sel_ID );

	//update output window		
	int exitstat = -999;
	exitstat = updateOutputWindow( "PUNCH.STATIONS.GUI", punch_vect );

	//make sure the PUNCH.STATIONS.GUI command was successful
	if ( exitstat == 0 ) {
		//output from that command 
		//(ppinit_pun.timestamp)
		//must be moved from the ofs_output 
		//directory to the input 
		//directory .../ppinit/  and renamed to
		//REDEFINE.STATIONS.GUI.
	
		//In order to locate that "_pun" file, 
		//use the output we have for the 
		//"ppinit_log.timestamp" file.  The "_pun" file
		//will have same NAME, PATH, and TIMESTAMP, except
		//the "_log" is replaced with "_pun".

		//we need the line that has the 
		//log output file. This line should
		//be the 2nd to last line, but to 
		//be safe, go thru and find line with
		//"==" since the line is formatted 
		//with a "==>" at the beginning and 
		// a "<==" at the end.
		int size =0;
		size = punch_vect.size();
		String log_name = null;
		String s = null;
		for ( int i=0; i<size; i++ ) {
			s = ((String) punch_vect.elementAt(i)).trim();
			if ( s.regionMatches(true, 0, "==", 0, 2) ) {
				log_name = (String)
				punch_vect.elementAt( i );
				break;
			}
		}//end loop
		if ( log_name != null ) {
			//Remove extra "==>" and "<==" chars and change "_log" to "_pun"
			String pun_path = null;
			pun_path = NWSRFS_Util.get_pun_path_from_log( log_name );

			//now we have the file we need to move and rename
			//first get path to REDEFINE.STATIONS.GUI
			String redef_stns_path = null;
			redef_stns_path = IOUtil.getPropValue( "REDEFINE.STATIONS.GUI" );
			if ( redef_stns_path == null ) {
				file_moved = false;
				message = "Unable to locate \"REDEFINE.STATIONS.GUI\" file";
				Message.printWarning( 2, routine, message );
				_ListModel.addElement( message );
			}
			else { // ( redef_stns_path != null ) 
				File pun_file = new File( pun_path);
				File redefstn_file = new File( redef_stns_path );
				if (( pun_file.canRead()) && ( redefstn_file.canRead() )) {

					//actually move file
					try {
						pun_file.renameTo( redefstn_file );

						Message.printStatus( 30, routine,
						"Moved file: \"" + pun_path + "\" to: \"" + redef_stns_path +"\"");	
					}
					catch( Exception e ) {
						file_moved = false;
						Message.printWarning( 2, routine, e );
						message = "Unable to rename punch file to: \"REDEFINE.STATIONS.GUI\" file";
						Message.printWarning( 2, routine, message );
						_ListModel.addElement( message );
					}
				} 
				else { // file(s) don't exist
					file_moved = false;
					message = "Unable to locate punch file to rename it to: \"REDEFINE.STATIONS.GUI\" file";
					Message.printWarning( 2, routine, message );
					_ListModel.addElement( message );
				}
			}
			if ( file_moved ) {
				//REDEFINE.STATIONS.GUI is in place,
				//Before allowing user to EDIT the 
				//file, add line to top and bottom:
				//"@DEFINE STATION OLD" and
				//"@STOP".
				// ADD LINES:
				File inputFile = new File( redef_stns_path );
				File outputFile = new File ( redef_stns_path + ".tmp" );

				try {
					FileInputStream fis = new 
					FileInputStream( inputFile );
					InputStreamReader isr = new InputStreamReader( fis );
					BufferedReader br = new BufferedReader( isr );
					FileOutputStream fos = new FileOutputStream( outputFile );
					PrintWriter pw = new PrintWriter( fos ); 

					//vector to hold all lines of file
					Vector v = new Vector();
					do {
						//store lines in vector as read them in
						s = br.readLine();
						if ( s== null ) {
							//no more lines
							break;
						}
						else {
							v.addElement( s );
						}
					} while ( s != null );
					//now have all lines.. 
					//add line to beg and end.
					v.insertElementAt( "@DEFINE STATION OLD", 0 );
					v.addElement( "@STOP" );
					//now print out.
					for(int i=0; i<v.size(); i++) {
						String line = (String)
						v.elementAt( i );
						pw.println( line );
						pw.flush(); 
					}
					br.close();
					pw.close();
					fis = null;
					isr = null;
					br = null;
					fos = null;
					pw = null;
				}
				catch( Exception e ) {
					file_moved = false;
					if ( Message.isDebugOn ) {
						Message.printWarning( 2, routine, e );
					}
				}
				//move output "tmp" file back
				try {
					outputFile.renameTo( inputFile );
				}
				catch ( Exception e ) {
					file_moved = false;
					if ( Message.isDebugOn ) {
						Message.printWarning( 2, routine, e );
					}
				}
			
			} //if file_moved
			
			//check again now...
			if ( !file_moved ) {
				message = "Cannot update the file: \"REDEFINE.STATIONS.GUI\". with " +
					"the latest punch file. Will not run the ofs command \"REDEFINE.STATIONS.GUI\".";
				Message.printWarning ( 2, routine,message );
				_ListModel.addElement( message );
			}
			if ( file_moved ) {
				//FINALLY, open for user to edit.
				//Finally Allow User to EDIT the File.
				try {
					NWSRFS_Util.runEditor( _editor,	redef_stns_path, true );
				}
				catch ( Exception e ) {
					Message.printWarning( 2, routine, e );
				}
			}		
		}
		else {
			Message.printWarning( 2, routine, "Unable to run the \"REDEFINE.STATIONS.GUI\" command " );
			if ( Message.isDebugOn) {
				Message.printDebug( 5, routine,
				"Unable to locate the " +
				"\"ppinit_log.timestamp\" file " +
				"in the output from the " +
				"\"PUNCH.STATIONS.GUI\" command. " +
				"Can not run " +
				"\"REDEFINE.STATIONS.GUI\" command " +
				"without moving the file " +
				"identified by that \"_log\" file, " +
				"but named, " +
				"\"ppinit_pun.timestamp\" to the " +
				"\"input directory \"..ppinit\" " +
				"and renaming the \"_pun\" file " +
				"\"REDEFINE.STATIONS.GUI\"." );
			}
		}
	}
	else {
		message = "\"PUNCH.STATIONS.GUI\" command failed.  Will not run \"REDEFINE.STATIONS.GUI\" command";
		Message.printWarning( 2, routine, message );
		_ListModel.addElement( "PUNCH.STATIONS.GUI Failed!" );
	}

	return file_moved;
		
} //end run_redefStations_edit_commands

/**
Updates the output window by adding all lines of the vector
passed in to the window for display.  The vectors passed in 
contain the output from running ofs commands. Also returns the exit
status, found in the last line of the vector (due to running the ofs
command via the ProcessManager).  Appends lines to the output window that
contains: "OUTPUT FILE:" followed by the name of the output file created.
The output file created is found by using the path of the output log file, 
which is found in the vector passed in, identified by the "==>" and "<=="
arrows, plus the name of the output file, which is the key that
follows the "-o" flag in the ofs command itself (in the command 
passed in), followed by the time stamp from the log file.
@param cmd_run  OFS command run to produce the output.
@param vect_to_display Vector to update the output window with.
@return  exit status returned by running the command through the
process manager.  The exit status is in the last line of the vector.
*/
public int updateOutputWindow( String cmd_run, Vector vect_to_display )
{

	//int to return
	int exitstat =0;

	//get size of vector
	int size = 0;
	if ( vect_to_display != null ) {
		size = vect_to_display.size();
	}

	// Set the exitstat to be 0 unless an error is encountered in the output
        String error_str =null;
        for ( int i=0; i<size; i++ ){
                error_str = ((String)vect_to_display.elementAt(i)).toLowerCase();
                if ( error_str.indexOf( "fail" ) > -1 )  {
                        exitstat = 99;
                        break;
                }
        }

/*
	//first need to clean up vector.
	//It does still contain the exit status as the last line.
	//we need to remove this line before displaying the rest of the
	//vectors contents in the output window.  Also, we return the 
	//exit status from this method since it is used to determine if the
	//checkbox by the command run should be checked or not.
	String last_line = null;
	if ( size > 0 ) {
		last_line =  (String)vect_to_display.elementAt( size-1 );
		//now we have exit line, remove it from the vector so that
		//it won't be printed to output.
		vect_to_display.removeElementAt( size -1 );

		//now update SIZE again!
		size = vect_to_display.size();
	}
	//this line will be in format: "Stop #".  We need just the number.
	String exit_string = "-999";
	int exitstat = -999;
	exit_string = last_line.substring( 5 );
	try {
		exitstat = Integer.parseInt( exit_string );
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, "Unable to determine " +
		"the exit status of the ofs command: \"" + 
		cmd_run + "\"." );
		if ( Message.isDebugOn ) {
			Message.printWarning( 2, routine, e );
		}
	}
*/

	//add new lines to vector before displaying it.  
	//These lines are added to the end to display the Newly created
	//output file.  To get output file name, use path of "log" file,
	//plus name of output file ( from ofs command ), plus time stamp
	//from log file.
	String full_output_file = null;
	String path = null;
	String timestamp = null;
	String outputfile_name = null;
	String ofs_command = null;
	String log_path = null;
	//go thru vector and find 2 lines:
	//one has the ofs_command,
	//the other has the output log file name.
	String s = null;
	for ( int i=0; i< vect_to_display.size(); i++ ) {
		s = (String)vect_to_display.elementAt( i );
		if ( s.indexOf( "-o" ) > 0 ) {
			ofs_command = s;
		}
		if ( s.indexOf( "==" ) > 0 ) {
			log_path = s;
		}
	}
	//ofs command in format:
	//Command Run: "ofs -p ppinit -i PUNCH.etc -o PUNCH.out"
	//NOTICE THE QUOTES
	if ( ofs_command != null ) {
		//parse command to get output file name- it follows the -o flag
		int flag = -999;
		//int quote = -999;
		int minus_u = -999;
		flag = ofs_command.indexOf("-o" );
		minus_u = ofs_command.lastIndexOf("-u");
		//quote = ofs_command.lastIndexOf("\"");
		if (( flag > 0 ) && ( minus_u > 0 )) {
			outputfile_name = (ofs_command.substring( flag + 2, minus_u )).trim();
		}
	/*
		if (( flag > 0 ) && ( quote > 0 )) {
			outputfile_name = 
			(ofs_command.substring( flag + 2, quote )).trim();
		}
	*/
	}
	if ( log_path != null ) {
		//format: "==> /projects/ahps/panama/ofs/output/
		//ams/ppinit_log.20020106.180020 <=="

		//break it up to get path and time stamp
		Vector v = null;
		v = StringUtil.breakStringList( log_path, " ", StringUtil.DELIM_SKIP_BLANKS );
		int p = 0;
		if ( v != null ) {
			p = v.size();
		}
		//should be 3 pieces -middle one is path.
		if ( p == 3 ) {
			//log_path = /projects/ahps/.../filename.timestamp
			log_path = (String)v.elementAt(1);
		
			//now get path and timestamp, do not include 
			//break it up again based on file separator.
			v = null;
			v = StringUtil.breakStringList( log_path, _fs, StringUtil.DELIM_SKIP_BLANKS );
			p = -999;
			if ( v != null ) {
				p = v.size();
			}
			
			//path should be everything up to last piece.
			StringBuffer b= new StringBuffer();
			for ( int i=0; i<p-1; i++ ) {
				if ( i == 0 ) {	
					b.append( _fs);
				}
				b.append( (String)v.elementAt(i) + _fs );
			}
			path = b.toString();
			b = null;
		
			//time stamp is found in last piece
			//format: filename.timestamp
			String last_piece = (String)v.elementAt( p-1 );
			//get everything after the "."
			int per_index = -999;
			per_index = last_piece.indexOf(".");
			timestamp = last_piece.substring( per_index );
		}
		
	} //if log_path !=null
	
	//now concatenate
	if (( timestamp != null ) && ( path != null ) && ( outputfile_name != null ) ) {
		full_output_file = "==> " + path + outputfile_name + timestamp + " <=="; 
	}
	//add this to end of vector
	vect_to_display.addElement( "OUTPUT FILE: " );
	vect_to_display.addElement( full_output_file );

	//now update SIZE again!
	size = vect_to_display.size();

	//add command to output
	_ListModel.addElement( cmd_run + ":" );
	String line = null;
	for( int i=0; i<size; i++ ) {
		line = (String)vect_to_display.elementAt( i );
		//update list model
		_ListModel.addElement( line ); 
	}
	//update the scroll bar to scroll to the bottom
	_output_JList.ensureIndexIsVisible( (_ListModel.size() -1) );
	_output_JList.setSelectedIndex( (_ListModel.size() -1) );
	
	//add empty line
	_ListModel.addElement( " " );

	//clean up
	vect_to_display = null;

	return exitstat;

} //end updateOutputWindow

//////////////////////* ACTIONS *///////////////////////////////
/**
Event handler for action events.
@param event  Event to handle.
*/
public void actionPerformed( ActionEvent event ) {
	String routine = _class + ".actionPerformed";
	Object source = null;

	try {
		source = event.getSource();

		//////////////////////////////////////
		///////////* CLOSE BUTTON *///////////
		//////////////////////////////////////
		//Close Button works on any dialog...
		//if ( command.equals( _redefStations_close_string ) ) {}
		if ( source.equals( _close_JButton ) ) {
			_dialog.setVisible(false);
			_dialog.dispose();
		}
		
		//////////////////////////////////////
		///////////* REDEFINE STATIONS *///////////
		//////////////////////////////////////
		else if ( source.equals( _redefStations_edit_JButton ) ) {
			//seperate method since multi-stepped
			//boolean indicates if the REDEFINE.STATIONS.GUI
			//file was successfully edited.  If not, don't
			//run next command...
			boolean ran_well = false;
			ran_well = run_redefStations_edit_commands();
		
			if ( ran_well ) {
				//enable
				_redefStations_run_JButton.setEnabled( true );
			}
			else {
				//disable RUN button
				_redefStations_run_JButton.setEnabled( false );
			}
				
		} //end _redefStations_edit_JButton

		else if ( source.equals( _redefStations_run_JButton ) ) {
			//execute the ofs command
			//ofs ppinit REDEFINE.STATIONS.GUI 
			Vector redefstn_vect = null;
			redefstn_vect = NWSRFS_Util.run_redefine_stations(); 
			//update output window		
			int exitstat = -999;
			exitstat = updateOutputWindow( "REDEFINE.STATIONS.GUI", redefstn_vect );
			if ( exitstat != 0 ) {
				_ListModel.addElement( "REDEFINE.STTATIONS.GUI failed!" );
			}

			redefstn_vect = null;
		}

		//////////////////////////////////////
		///////////* ADD STATIONS *///////////
		//////////////////////////////////////
		else if ( source.equals( _addStations_add_JButton ) ) {
			//need to copy ADDSTATION.GUI to 
			//NEWSTATION.GUI and then open
			//NEWSTATION.GUI for editing by user.
			boolean ran_well = true;
			ran_well = NWSRFS_Util.copy_addStn_to_newStn();
			if ( ran_well ) {
				String newStn_path = null;
				newStn_path = IOUtil.getPropValue( "NEWSTATION.GUI" );
				if (( newStn_path != null ) && (IOUtil.fileExists( newStn_path ) )) {
					//open up file in editor.

					//Finally Allow User to EDIT the File.
					try {
						NWSRFS_Util.runEditor( _editor, newStn_path, true );
					}
					catch ( Exception e ) {
						Message.printWarning( 2, routine, e );
					}

					//enable run button
					_addStations_run_JButton.setEnabled( true );
				}
				else {
					Message.printWarning( 2, routine,
					"Unable to open \"NEWSTATION.GUI\" file for editing.  Path to file: \"" +
					newStn_path + "\"." );
				}
			}
		} //end if _addStations_add_JButton 

		else if ( source.equals( _addStations_run_JButton ) ) {
			//run the ofs commands:
			//ofs -p ppinit -i NEWSTATION.GUI, etc
			//ofs -p ppinit -i NETWORK_ORDER.GUI, etc
			Vector newstn_vect = null;
			newstn_vect = NWSRFS_Util.run_newstation();
			//update output window		
			int exitstat = -999;
			exitstat = updateOutputWindow( "NEWSTATION.GUI", newstn_vect );
			if ( exitstat != 0 ) {
				_ListModel.addElement( "NEWSTATION.GUI failed! Will not run \"NEWTORK_ORDER.GUI\"" );
			}
			else { //exitstat == 0, so run next command
				exitstat = -999;
				Vector network_vect = null;
				network_vect = NWSRFS_Util.run_network_order();
				exitstat = updateOutputWindow( "NETWORK_ORDER.GUI", network_vect );
				if ( exitstat != 0 ) {
					_ListModel.addElement( "NETWORK_ORDER.GUI failed!" );
				}
				network_vect = null;
			}		
			newstn_vect = null;
			
		} //end if _addStations_run_JButton 
		
		//////////////////////////////////////
		/////* Redefine Segments *///////
		//////////////////////////////////////
		
		else if (source.equals(_redefSegments_edit_JButton)) {
			// separate method since multi-stepped boolean 
			// indicates if the RESEGDEF.GUI file was 
			// successfully edited.  If not, don't run next 
			// command
			boolean successful = run_redefSegments_edit_commands( _redefSegments_subtitle_string);
		
			if (successful) {
				_redefSegments_run_JButton.setEnabled(true);
			}
			else {
				//disable RUN button
				_redefSegments_run_JButton.setEnabled(false);
			}
		}
		else if (source.equals( _redefSegments_run_JButton ) ) {
			// execute the ofs command:
			//	ofs fcinit RESEGDEF.GUI 
			Vector redefinedSegments = null;
			redefinedSegments = NWSRFS_Util.run_redefine_segments();
			// update output window		
			int status = updateOutputWindow("RESEGDEF.GUI", redefinedSegments);
			if (status != 0) {
				_ListModel.addElement("RESEGDEF.GUI failed!");
			}
			else {
				// completed successfuly, so rebuild the main system JTree
				if (__systemJTree != null) {
					__systemJTree.rebuild();
				}
			}
		}
		
		//////////////////////////////////////
		///////////* ADD RatingCurve *///////////
		//////////////////////////////////////
		else if (source.equals(_addRatingCurve_add_JButton)) {
			// need to copy DEFNEWRC.GUI to NEWRC.GUI and then open
			// NEWRC.GUI for editing by user.
			
			// returns null if couldn't move file
			String newRCFilename = NWSRFS_Util.copy_addRC_to_newRC();
			if ((newRCFilename != null) && (IOUtil.fileExists(newRCFilename))) {
				// open up file in editor.
				// Finally Allow User to EDIT the File.
				try {
					NWSRFS_Util.runEditor(_editor, newRCFilename, true);
				}
				catch (Exception e) {
					Message.printWarning(2, routine, e);
				}

				//enable run button
				_addRatingCurve_run_JButton.setEnabled(true);
				if (__systemJTree != null) {
					__systemJTree.rebuild();
				}
			}
			else {
				Message.printWarning(2, routine, "Unable to open \"NEWRC.GUI\" " 
					+ "file for editing.  Path to file: \"" + newRCFilename + "\"." );
			}
		}
		else if (source.equals(_addRatingCurve_run_JButton)) {
			// run the ofs commands:
			//	ofs -p fcinit -i NEWRC.GUI, etc
			Vector newRatingCurves = NWSRFS_Util.run_newRatingCurve();
			// update output window		
			int status = updateOutputWindow("NEWRC.GUI", newRatingCurves);
			if (status != 0) {
				_ListModel.addElement("NEWRC.GUI failed! ");
			}
			else {
				if (__systemJTree != null) {
					__systemJTree.rebuild();
				}
			}
		}

		//////////////////////////////////////
		/////* Redefine Rating Curve *///////
		//////////////////////////////////////
		else if ( source.equals( _redefRatingCurves_edit_JButton ) ) {
			//seperate method since multi-stepped
			//boolean indicates if the DEFRC.GUI
			//file was successfully edited.  If not, don't
			//run next command...
			boolean ran_well = false;
			ran_well = run_redefRatingCurves_edit_commands(	_redefRatingCurves_subtitle_string );
		
			if ( ran_well ) {
				//enable
				_redefRatingCurves_run_JButton.setEnabled( true );
			}
			else {
				//disable RUN button
				_redefRatingCurves_run_JButton.setEnabled( false );
			}

		} //end if _redefRatingCurves_edit_JButton 

		else if ( source.equals( _redefRatingCurves_run_JButton ) ) {
			//execute the ofs command
			//ofs fcinit DEFRC.GUI 
			Vector redefrc_vect = null;

			redefrc_vect = NWSRFS_Util.run_redefine_ratingCurves(); 
			//update output window		
			int exitstat = -999;
			exitstat = updateOutputWindow( "DEFRC.GUI", redefrc_vect );
			if ( exitstat != 0 ) {
				_ListModel.addElement( "DEFRC.GUI failed!" );
			}

			redefrc_vect = null;
		}// end _redefRatingCurves_run_JButton

		//////////////////////////////////////
		/////* PREPROCESSOR DB STATUS *///////
		//////////////////////////////////////
		else if ( source.equals( _preprocessDB_run_JButton ) ) {
			//run ofs command:
			//ofs -p ppinit -i PPINIT.STATUS.GUI, etc.
			Vector preproc_vect = null;
			preproc_vect = NWSRFS_Util.run_preprocessDB_status();
			//update output window		
			int exitstat = -999;
			exitstat = updateOutputWindow( "PPINIT.STATUS.GUI", preproc_vect );
			if ( exitstat != 0 ) {
				_ListModel.addElement( "PPINIT.STATUS.GUI failed!" );
			}
			preproc_vect = null;
		} //end if _preprocessDB_run_JButton 

		//////////////////////////////////////
		/////* FORECAST DB STATUS *///////
		//////////////////////////////////////
		else if ( source.equals( _forecastDB_run_JButton ) ) {
			//run ofs command:
			//ofs -p fcinit -i FCINIT.STATUS.GUI, etc
			Vector forec_vect = null;
			forec_vect = NWSRFS_Util.run_forecastDB_status();
			//update output window		
			int exitstat = -999;
			exitstat = updateOutputWindow( "FCINIT.STATUS.GUI", forec_vect );
			if ( exitstat != 0 ) {
				_ListModel.addElement( "FCINIT.STATUS.GUI failed!" );
			}
			forec_vect = null;
		} //end if _forecastDB_run_JButton 

		//////////////////////////////////////
		/////* DUMP OBSERVATIONS STATUS */////
		//////////////////////////////////////
		else if ( source.equals( _dumpObs_edit_JButton ) ) {
			//opens DUMPOBS.GUI for editing.
			//get path to file.
			String dumpObs_path = null;
			dumpObs_path = IOUtil.getPropValue( "DUMPOBS.GUI" );
			if (( dumpObs_path != null ) && ( IOUtil.fileExists( dumpObs_path ) )) {

				//open it
				//Finally Allow User to EDIT the File.
				try {
					NWSRFS_Util.runEditor( _editor, dumpObs_path, true );
				}
				catch ( Exception e ) {
					Message.printWarning( 2, routine, e );
				}
			}
			else {
				//disable the run button
				_dumpObs_run_JButton.setEnabled( false );

				_ListModel.addElement( "DUMPOBS.GUI file can not be edited." );
				_ListModel.addElement( "Can not run \"ppdutil DUMPOBS.GUI\"." );
			}
		} //end  _dumpObs_edit_JButton

		else if ( source.equals( _dumpObs_run_JButton ) ) {
			//runs ofs command:
			//ofs -p ppdutil -i DUMPOBS.GUI, etc
			Vector dumpObs_vect = null;
			dumpObs_vect = NWSRFS_Util.run_dump_obs();
			//update output window		
			int exitstat = -999;
			exitstat = updateOutputWindow ( "DUMPOBS.GUI", dumpObs_vect );
			if ( exitstat != 0 ) {
				_ListModel.addElement( "DUMPOBS.GUI failed!" );
			}
			dumpObs_vect = null;
		} //end  _dumpObs_run_JButton 

		//////////////////////////////////////
		/////* DUMP TS STATUS */////
		//////////////////////////////////////
		else if ( source.equals( _dumpTS_edit_JButton ) ) {
			//opens DUMPTS.GUI for editing.
			//get path to file.
			String dumpTS_path = null;
			dumpTS_path = IOUtil.getPropValue( "DUMPTS.GUI" );
			if (( dumpTS_path != null ) && ( IOUtil.fileExists( dumpTS_path ) )) {

				//open it
				//Finally Allow User to EDIT the File.
				try {
					NWSRFS_Util.runEditor( _editor, dumpTS_path, true );
				}
				catch ( Exception e ) {
					Message.printWarning( 2, routine, e );
				}
			}
			else {
				//disable the run button
				_dumpTS_run_JButton.setEnabled( false );

				_ListModel.addElement( "DUMPTS.GUI file can not be edited." );
				_ListModel.addElement( "Can not run \"prdutil DUMPTS.GUI\"." );
			}

		} //end  _dumpTS_edit_JButton

		else if ( source.equals( _dumpTS_run_JButton ) ) {
			//runs ofs command:
			//ofs -p prdutil -i DUMPTS.GUI, etc
			Vector dumpTS_vect = null;
			dumpTS_vect = NWSRFS_Util.run_dump_ts();
			//update output window		
			int exitstat = -999;
			exitstat = updateOutputWindow( "DUMPTS.GUI", dumpTS_vect );
			if ( exitstat != 0 ) {
				_ListModel.addElement( "DUMPTS.GUI failed!" );
			}
			dumpTS_vect = null;
		} //end  _dumpTS_run_JButton 

		//////////////////////////////////////////////////
		///////////////* POPUP MENU VIEW* /////////////////
		//////////////////////////////////////////////////
		else if ( source.equals( _popup_view_JMenuItem ) ) {
			//get selected item
			String selected_item = null;
			selected_item = ((String)_output_JList.getSelectedValue()).trim();
			if ( selected_item == null ) {
				Message.printWarning( 2, routine, "Nothing Selected in output window." );
			}
			else {
				//see if it is a file all files start with "==>" 
				if ( !selected_item.startsWith( "==" ) ) {
					Message.printWarning( 2, routine, "No file selected." );
				}
				else {
					//remove the "==> " and " <=="
					String file_sel = null;
					Vector v = null;
					//break up based on spaces
					v = StringUtil.breakStringList( selected_item, " ", StringUtil.DELIM_SKIP_BLANKS );
				
					//should be 3 pieces, with middle piece being the file path.
					if ( v.size() == 3 ) {
						file_sel = (String)v.elementAt( 1 );
						if (( file_sel != null ) && ( IOUtil.fileExists( file_sel )) ) {
							try {
							//view file only
								NWSRFS_Util.runEditor( _editor, file_sel, false );
							}
							catch ( Exception e ) {
								Message.printWarning( 2, routine, e );
							}
						}
						else {
							Message.printWarning( 2, routine,
									"Unable to view: \"" + selected_item+ "\"." ); 
						}
					}
					else {
						Message.printWarning( 
						2, routine, "Unable to view file: \"" + selected_item + "\"." ); 
					}
					
				}
			}
		}
		//////////////////////////////////////////////////
		//////////////* POPUP MENU CLEAR* ////////////////
		//////////////////////////////////////////////////
		else if ( source.equals( _popup_clear_JMenuItem ) ) {
			_output_JList.clearSelection();
			_ListModel.clear();
		}	
		
	} 
	catch ( Exception e ) {
		Message.printWarning( 2, routine, e );
	}
} //end actionPerformed

} //end class NWSRFS_SystemMaintenance
