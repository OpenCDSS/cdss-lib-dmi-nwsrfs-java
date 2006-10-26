//-----------------------------------------------------------------------------
// NWSRFS_RatingCurve_JList - an object to display a simple list of rating
// curves in a JTree format.
//-----------------------------------------------------------------------------
// History:
//
// 2004-10-11	J. Thomas Sapienza, RTi	Initial version based on 
//					NWSRFS_RatingCurve_JTree.
// 2006-01-18	JTS, RTi		Now uses SimpleJList instead of
//					MutableJList.
//-----------------------------------------------------------------------------

package RTi.DMI.NWSRFS_DMI;

import java.awt.Component;
import java.awt.Font;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.Vector;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import javax.swing.tree.TreePath;

import RTi.DMI.NWSRFS_DMI.NWSRFS_CarryoverGroup;
import RTi.DMI.NWSRFS_DMI.NWSRFS_ForecastGroup;
import RTi.DMI.NWSRFS_DMI.NWSRFS_Operation;
import RTi.DMI.NWSRFS_DMI.NWSRFS_Segment;
import RTi.DMI.NWSRFS_DMI.NWSRFS_RatingCurve;
import RTi.DMI.NWSRFS_DMI.NWSRFS_TimeSeries;
import RTi.DMI.NWSRFS_DMI.NWSRFS_Util;

import RTi.Util.GUI.SimpleJList;
import RTi.Util.GUI.ReportJFrame;
import RTi.Util.GUI.SimpleJMenuItem;

import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.LanguageTranslator;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
The NWSRFS_RatingCurve_JList class displays a list of the rating curves 
available.
*/
public class NWSRFS_RatingCurve_JList 
extends SimpleJList
implements ActionListener, MouseListener {

// the font used in the main system JTree in the application.  Set here so
// that all the other tabbed items will look the same
private Font __listFont;

//NWSRFS instance
private NWSRFS __nwsrfs;

//fs5files used
private String __fs5files;

//JFrame parent
private JFrame __parent;

//folder icon
private Icon __folderIcon;

//Indicates whether the nodes in the JTree should be 
//preceeded by an abbreviation indicating data type or not.
private boolean __verbose = false;	

//indicate if operations should be included in JTree
private boolean __include_operations = false;	

//if the Tree nodes can be edited
private boolean __canEdit = false;

//String for top node
private String __top_node_str = null;

// A single popup menu that is used to provide access to  other features 
//from the tree.  The single menu has its items added/removed as necessary 
//based on the state of the tree.
private JPopupMenu __popup_JPopupMenu;		

//pop menu items 
// Define STRINGs here used for menus in case we need
// to translate the strings.
protected String _popup_printRatingCurve_string 
	= "View Current Rating Curve Definition";
protected String _popup_addRatingCurve_string = "Add Rating Curve";
protected String _popup_deleteRatingCurve_string = "Delete Rating Curve";
protected String _popup_redefRatingCurve_string = "Redefine Rating Curve";

//menus 
private SimpleJMenuItem __popup_printRatingCurve_JMenuItem = null;
private SimpleJMenuItem __popup_addRatingCurve_JMenuItem = null;
private SimpleJMenuItem __popup_deleteRatingCurve_JMenuItem = null;
private SimpleJMenuItem __popup_redefRatingCurve_JMenuItem = null;

/**
Constructor for NWSRFS_RatingCurve_JList to display Contract information.
@param parent JFrame parent Calling parent class.  
@param nwsrfs NWSRFS instance containing all the data for the JTree.
@param top_node_str String to use for top tree node.
@param fs5files  String name of fs5files used.
@param verbose Boolean indicating if extra abbreviations should be
used before each tree node's name to indicate data type of node (true).
For example: "RC:" for rating curve.
@param canEdit Boolean indicating if the JTree nodes can be edited. 
@deprecated do not use this class any longer -- use NWSRFS_RatingCurve_JPanel
instead
*/
public NWSRFS_RatingCurve_JList ( JFrame parent, NWSRFS nwsrfs,
				String top_node_str,
				String fs5files,
				boolean verbose,
				boolean canEdit,
				Font listFont) {
				
	String routine = "NWSRFS_RatingCurve_JList.constructor";
	
	__parent = parent; 	
	__top_node_str = top_node_str; 	
	__verbose = verbose; 	
	__canEdit = canEdit;
	__fs5files = fs5files;
	__listFont = listFont;

	if ( Message.isDebugOn ) {
		Message.printDebug( 4, routine,
		"Program user: \"" + IOUtil.getProgramUser() + "\" has " +
		"edit permissions (T/F)? --> " + __canEdit );
	}

	addMouseListener(this);

	//sets the nwsrfs instance to the global: __nwsrfs
	setListData(nwsrfs);

	//initialize GUI strings (translate if needed)
	initialize_gui_strings();

	//create popup menu and menu items
	createPopupMenu();

	//populate list 
	displayListData();
} //end constructor

/**
Constructor for NWSRFS_RatingCurve_JList to display Contract information.
By default, the JTree will not display extra ("verbose") strings 
before each node name and is not editable.
@param parent JFrame parent Calling parent class.  
@param nwsrfs NWSRFS instance containing all the data for the list.
@param top_node_str String to use for top list line.
@param fs5files  String name of fs5files used.
*/
public NWSRFS_RatingCurve_JList ( JFrame parent, NWSRFS nwsrfs, 
			String top_node_str, String fs5files ) {
		
	 this ( parent, nwsrfs, top_node_str, fs5files, false, false, null);

} //end constructor


/**
Removes the extra information added to the beginning of the node name
if running in verbose mode (__verbose = true ).  Assumes the prefix
ends in a colon ":".
@param name Name to parse out additional prefix information if exists.
*/
protected String clean_node_name ( String name ) {
	/*
	if ( ! __verbose ) {
		return name;
	}
	*/

	//parse out prefix, assuming ends with ":"
	String s = name;
	int ind = -999;
	ind = name.indexOf(":");
	if ( ind > 0 ) {
		s = name.substring( ind + 1 ).trim();
	}
	return s;
}


/**
Clear all data from the list.
@deprecated use removeAll().
*/
public void clear () {
	removeAll();
}

/**
Creates the JPopupMenu and SimpleJMenuItems for the PopupMenu.  
*/
private void createPopupMenu() {
	//create Popupu Menu
	__popup_JPopupMenu = new JPopupMenu();

	//popup menu items
	__popup_printRatingCurve_JMenuItem = new SimpleJMenuItem(
	_popup_printRatingCurve_string, this );

	__popup_addRatingCurve_JMenuItem = new SimpleJMenuItem(
	_popup_addRatingCurve_string, this );

	__popup_deleteRatingCurve_JMenuItem = new SimpleJMenuItem(
	_popup_deleteRatingCurve_string, this );

	__popup_redefRatingCurve_JMenuItem = new SimpleJMenuItem(
	_popup_redefRatingCurve_string, this );

	__popup_JPopupMenu.add(__popup_addRatingCurve_JMenuItem);
	__popup_JPopupMenu.addSeparator();
	__popup_JPopupMenu.add(__popup_printRatingCurve_JMenuItem);
	__popup_JPopupMenu.add(__popup_deleteRatingCurve_JMenuItem);
	__popup_JPopupMenu.add(__popup_redefRatingCurve_JMenuItem);
}// end createPopupMenu() 


/**
Display all the information in the NWSRFS data set.
*/
public void displayListData() {
	String routine = "NWSRFS_RatingCurve_JList.displayListData";

	Message.printStatus( 3, routine, 
		routine + " called to create Rating Curve list." );

	removeAll();

	//make vector of rating curve IDs
	Vector rc_vect = null;

	NWSRFS_DMI dmi = __nwsrfs.getDMI();
	try {
		rc_vect = StringUtil.sortStringList( dmi.readRatingCurveList());
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, 
		"Unable to read list of rating curves to create " +
		"rating curve list. Please refer to log file for more " +
		"details." );

		Message.printWarning( 2, routine,  e );

		return;
	}

	int numb_rcs = 0;
	if ( rc_vect != null ) {	
		numb_rcs = rc_vect.size();
	}

	String rc_str = null;
	Vector data = new Vector();
	for (int i=0; i<numb_rcs; i++ ) {
		rc_str = (String) rc_vect.elementAt(i);
		if ( ( rc_str == null ) || ( rc_str.length() <=0 ) ) {
			continue;
		}
		if ( ! __verbose ) {
			data.add(rc_str);
		}
		else {
			data.add("RC: " + rc_str);
		}
	}
	setListData(data);

	setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);

	// make sure the list looks similar to all the other tabbed things
	if (__listFont != null) {
		setFont(__listFont);
	}
}//end displayListData()

/**
This method is used to get the strings needed for labelling all the GUI
components only if a translation table is used for the application, ie, if
there is a translation file.  If the String cannot be located in the
translation file, the original string will be utilized in non-translated form.
*/
public void initialize_gui_strings()  {
        String routine = "NWSRFS_RatingCurve_JList.initialize_gui_strings"; 
	LanguageTranslator translator = null; 
	translator = LanguageTranslator.getTranslator();
        if ( translator != null ) {
		//Popupmenu
		_popup_printRatingCurve_string = translator.translate(
			"popup_printRatingCurve_string",
			_popup_printRatingCurve_string );
		_popup_addRatingCurve_string = translator.translate(
			"popup_addRatingCurve_string",
			_popup_addRatingCurve_string );
		_popup_deleteRatingCurve_string = translator.translate(
			"popup_deleteRatingCurve_string",
			_popup_deleteRatingCurve_string );
		_popup_redefRatingCurve_string = translator.translate(
			"popup_redefRatingCurve_string",
			_popup_redefRatingCurve_string );
	}
}//end initialize_gui_strings()  


/**
Destroys and recreates the list
@deprecated use remakeList()
*/
public void remake_JTree( ) {
	remakeList();
}

/**
Clears and repopulates the list.
*/
public void remakeList() {
	String routine = "NWSRFS_RatingCurve_JTRee.remakeList";
	Message.printStatus( 3, routine, routine + " called." );
	try {
		setListData( NWSRFS.createNWSRFSFromPRD( __fs5files, false ) );
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, e );
	}
	displayListData( );
	__parent.validate();
	__parent.repaint();
}


/**
Set the NWSRFS object 
@param nwsrfs NWSRFS data object which is used to populate the list.
*/
public void setListData ( NWSRFS nwsrfs ) {
	if ( Message.isDebugOn ) {	
		Message.printDebug( 5, "NWSRFS_RatingCurve_JList.setListData", 
		"NWSRFS_RatingCurve_JList.setListData called." );
	}

	if ( nwsrfs == null ) {
		Message.printWarning( 2, "NWSRFS_RatingCurve_JList.setListData",
		"NWSRFS object (nwsrfs) is null.  Cannot populate list!" );
	}

	__nwsrfs = nwsrfs;

}//end setListData


/////////////     *** actions ***       ////////////
/**
Handle action events from the popup menu.
@param event ActionEvent to handle.
*/
public void actionPerformed(ActionEvent event ) {
	String routine = "NWSRFS_RatingCurve_JList.actionPerformed";
	Object source = event.getSource();

	String fs = IOUtil.getPropValue("FILE_SEPARATOR" );
	if( ( fs == null ) || ( fs.length() <= 0 ) ) {
		fs = "/";
	}
	String editor = IOUtil.getPropValue("EDITOR");
	//should not be null, but just in case...
	if( ( editor == null ) || ( editor.length() <= 0 ) ) {
		editor = "vi";
	}

	String name = (String)getSelectedItem();
	String output_str = null; 

	if ( source == __popup_printRatingCurve_JMenuItem ) {
		name = clean_node_name( name );
		
		//string  to hold output from ofs command 
		//(string will be null if command failed)

		output_str  = NWSRFS_Util.run_print_ratingCurves( name );

		if ( output_str != null ) { 
			try { 
				NWSRFS_Util.runEditor( editor, 
				output_str, false ); 
			} 
			catch ( Exception e ) { 
				Message.printWarning( 2, routine, e ); 
			}
		 }
	}
	else if ( source == __popup_addRatingCurve_JMenuItem ) {
		name = clean_node_name( name );

		NWSRFS_SystemMaintenance system_maint = new 
		NWSRFS_SystemMaintenance();
		system_maint.create_addRatingCurve_dialog();

		//recreate tree
		remake_JTree();

	}
	else if ( source == __popup_deleteRatingCurve_JMenuItem ) {
		name = clean_node_name( name );


		Vector v = null;
		v  = NWSRFS_Util.run_delete_ratingCurve( name );
		if ( v != null ) {
			//rating curve deleted
			//recreate tree
			remake_JTree();
				
		}
		else  { //not deleted
			Message.printWarning( 2, routine,
			"Rating curve: \"" + name + "\" not deleted." );
		}
	}
	else if ( source == __popup_redefRatingCurve_JMenuItem ) {
		name = clean_node_name( name );

		//dialog that does the Redefine RatingCurves is part of
		//System Maintenance class (this class will take
		//care of any String translations if needed)
		NWSRFS_SystemMaintenance system_maint = new 
		NWSRFS_SystemMaintenance();
		system_maint.create_redefRatingCurves_dialog(name);

		//remake tree
		remake_JTree();
	}
}//end actionPerformed

/**
Responds to mouse clicked events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseClicked ( MouseEvent event ) {}

/**
Responds to mouse dragged events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseDragged(MouseEvent event) {}

/**
Responds to mouse entered events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseEntered(MouseEvent event) {}

/**
Responds to mouse exited events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseExited(MouseEvent event) {}

/**
Responds to mouse moved events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseMoved(MouseEvent event) {}

/**
Responds to mouse pressed events; does nothing.
@param event the MouseEvent that happened.
*/
public void mousePressed(MouseEvent event) {
}

/**
Responds to mouse released events.
@param event the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent event) {
	if (__popup_JPopupMenu != null 
		&& __popup_JPopupMenu.isPopupTrigger(event)) {
		__popup_JPopupMenu.show(event.getComponent(), 
			event.getX(), event.getY());
	}
}

} // end NWSRFS_RatingCurve_JList
