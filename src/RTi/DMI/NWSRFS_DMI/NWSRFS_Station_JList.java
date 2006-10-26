//-----------------------------------------------------------------------------
// NWSRFS_Station_JList - an object to display a simple list of rating
// curves in a JTree format.
//-----------------------------------------------------------------------------
// History:
//
// 2004-10-04	J. Thomas Sapienza, RTi	Initial version based on 
//					NWSRFS_Station_JTree.
// 2006-01-17	JTS, RTi		Changed to use SimpleJList.
// REVISIT:
// TO DO:
// Add station name description next to identifier.
// Once name added, will need to verify that the update cleanListItemName() 
// method to operates as expected and returns just the station ID
//-----------------------------------------------------------------------------

package RTi.DMI.NWSRFS_DMI;

import java.awt.Font;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Component;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import javax.swing.tree.TreePath;

import RTi.DMI.NWSRFS_DMI.NWSRFS_Util;
import RTi.Util.GUI.SimpleJList;
import RTi.Util.GUI.ReportJFrame;
import RTi.Util.GUI.SimpleJMenuItem;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.LanguageTranslator;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

//import RTi.App.NWSRFSGUI.NwsrfsGUI_SystemMaintenance;
//import RTi.App.NWSRFSGUI.NwsrfsGUI_Util;

import RTi.Util.Time.StopWatch;


/**
The NWSRFS_Station_JList class displays a list of the NWSRFS
stations in a JTree.
*/
public class NWSRFS_Station_JList extends SimpleJList
implements ActionListener, MouseListener
{

//NWSRFS instance
private NWSRFS __nwsrfs;

//fs5files used
private String __fs5files;

//JFrame parent
private JFrame __parent;

//Indicates whether the nodes in the JTree should be 
//preceeded by an abbreviation indicating data type or not.
private boolean __verbose = false;	

//if the Tree nodes can be edited
private boolean __canEdit = false;

// A single popup menu that is used to provide access to  other features 
//from the tree.  The single menu has its items added/removed as necessary 
//based on the state of the tree.
private JPopupMenu __popup_JPopupMenu;		

//pop menu items 
// Define STRINGs here used for menus in case we need
// to translate the strings.
protected String _popup_printStn_string = "View Current Station Definition";
protected String _popup_addStn_string = "Add Station";
protected String _popup_redefStn_string = "Redefine Station";

//used to describe station type.
protected String _type_string = "type";

//menus 
private SimpleJMenuItem __popup_printStn_JMenuItem = null;
private SimpleJMenuItem __popup_addStn_JMenuItem = null;
private SimpleJMenuItem __popup_redefStn_JMenuItem = null;


private JPopupMenu __stationListPopup = null;
private SimpleJMenuItem 
	__printStationMenuItem = null,
	__addStationMenuItem = null,
	__redefineStationMenuItem = null;

private String __printStationString = "View Current Station Definition";
private String __addStationString = "Add Station";
private String __redefineStationString = "Redefine Station";

private Font __listFont = null;

/**
Constructor for NWSRFS_Station_JList to display Contract information.
@param parent JFrame parent Calling parent class.  
@param nwsrfs NWSRFS instance containing all the data for the list.
@param fs5files  String name of fs5files used.
@param verbose Boolean indicating if extra abbreviations should be
used before each tree node's name to indicate data type of node (true).
For example: "STN:" for station.
@param canEdit Boolean indicating if the list nodes can be edited. 
@deprecated this class should no longer be used -- use NWSRFS_Station_JPanel
instead.
*/
public NWSRFS_Station_JList ( JFrame parent, NWSRFS nwsrfs,
				String fs5files,
				boolean verbose,
				boolean canEdit,
				Font listFont) {
				
	String routine = "NWSRFS_Station_JList.constructor";

	__parent = parent; 	
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

	//populate tree
	displayListData();
} //end constructor

/**
Constructor for NWSRFS_Station_JList to display Contract information.
By default, the JTree will not display extra ("verbose") strings 
before each node name and is not editable.
@param parent JFrame parent Calling parent class.  
@param nwsrfs NWSRFS instance containing all the data for the JTree.
@param top_node_str String to use for top tree node.
@param fs5files  String name of fs5files used.
*/
public NWSRFS_Station_JList ( JFrame parent, NWSRFS nwsrfs, 
			String top_node_str, String fs5files ) {
		
	 this ( parent, nwsrfs, fs5files, false, false, null);

} //end constructor


/**
Removes the extra information added to the beginning of the node name
if running in verbose mode (__verbose = true ).  Assumes the prefix
ends in a colon ":".
@param name Name to parse out additional prefix information if exists.
*/
protected String cleanListItemName ( String name ) {

	//parse out prefix, assuming ends with ":" (this will
	//catch the first ":" which exists if using verbose method.
	String s = name;
	int ind = -999;
	ind = name.indexOf(":");
	if ( ind > 0 ) {
		s = name.substring( ind + 1 ).trim();
	}

	//if there is a description after name, the decription 
	//should start with a single quote.
	ind = -999;
	ind = name.indexOf( "\"" );
	if ( ind > 0 ) {
		//trim everything from the quote on.
		s = (name.substring( 0, ind )).trim();
	}
	else { //name does not have descriptor, but still
		//need to trim off the other identifiers 
		//that come after the station ID and name.   
		//These are the PCPN=Y, RRS=Y, etc...
		ind = -999;
		ind = name.indexOf( _type_string );
		if ( ind > 0 ) {
			s = (name.substring( 0, ind )).trim();
		}
	}	
	return s;
}


/**
Clear all data from the tree.
*/
public void clear () {
	removeAll();
}

/**
Creates the JPopupMenu and SimpleJMenuItems for the PopupMenu.  All 
Menus are created up front and they are added and removed depending
on what item is selected in the JTree.
*/
private void createPopupMenu() {
	__stationListPopup = new JPopupMenu();

	//popup menu items
	__printStationMenuItem = new SimpleJMenuItem(
		__printStationString, this);

	__addStationMenuItem = new SimpleJMenuItem(__addStationString, this);

	__redefineStationMenuItem = new SimpleJMenuItem(
		__redefineStationString, this);

	__stationListPopup.add(__printStationMenuItem);
	__stationListPopup.add(__redefineStationMenuItem);
	__stationListPopup.addSeparator();
	__stationListPopup.add(__addStationMenuItem);
}// end createPopupMenu() 

/**
Display all the information in the NWSRFS data set.
*/
public void displayListData() {
	String routine = "NWSRFS_Main_JFrame.createStationList()";

	// clear out the list to make sure it is empty
	removeAll();
	
	Vector data = new Vector();
	
	NWSRFS_DMI dmi = __nwsrfs.getDMI();	
	
	Hashtable hash = null;
	try {
		hash = dmi.readStationHashtable();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, "Error reading in " 
			+ "list of stations to create station list.  Please "
			+ "see log file for more details.");
		Message.printWarning(2, routine, e);
	}

	NWSRFS_Station station = null;
	String stationID = null;
	String listItemString = null;

	// pull the IDs out of the hash table in whatever order they're in
	// and put them in a separate Vector -- this Vector is going to 
	// be sorted
	Vector stationIDs = new Vector();
	for (Enumeration e = hash.keys(); e.hasMoreElements();) {
		stationID = (String)e.nextElement();
		stationIDs.add(stationID);
	}

	// take the Vector of station IDs and sort it alphabetically.
	Vector sortedStationIDs = StringUtil.sortStringList(stationIDs);

	// iterate through the sorted station ID list and use the station 
	// IDs to pull out station objects from the hash table
	int size = sortedStationIDs.size();

	String stationDesc = null;
	String type = null;
	
	Message.printStatus(1, "", "The tree will be built for " 
		+ size + " stations.");
	
	for (int i = 0; i < size; i++) {
		stationID = (String)sortedStationIDs.elementAt(i);

		station = (NWSRFS_Station) hash.get(stationID);

		if (station == null || !StringUtil.isASCII(station.getID())) {
			if (station == null) {
				Message.printStatus(1, "", 
					"(skipping null station)");
			}
			else {
				Message.printStatus(1, "", "ID is not ASCII: '"
					+ station.getID() + "'");
			}
			
			continue;
		}

		//Define the station type.
		type = "  ";

		// Get the Station description. To do this we need to populate
		// the station object from the PPPPARMn preproccessed parameteri
		// database with a call to dmi.reasStation(...). We do not do a 
		// deepRead (set to false) to just get the description!
		stationDesc = null;

		try {
			dmi.readStation(station,false);
			stationDesc = station.getDescription();
		}
		catch (Exception e) {
			Message.printWarning(10, routine, "Error reading in "
				+ "the station description to create "
				+ "station list.");
			Message.printWarning(10, routine, e);
			Message.printStatus(1, "", 	
				"Skipping station because of error:");
			Message.printWarning(1, routine, e);
			continue;
		}
		
		if (stationDesc != null) {
			//add description in front the the station 
			//type and surround it with quotes.
			type = type + "\"" + stationDesc + "\"";
		}	

		//get Station ID put together full Station node text,
		//which includes an indication of station type.
		type = type + "  " + _type_string + ": ";
		
		//PCPN
		if (station.getIsPCPN()) {
			type = type + "PCPN=Y";
		}
		else {
			type = type + "PCPN=N";
		}
		
		//PE
		if (station.getIsPE()) {
			type = type + " PE=Y";
		}
		else {
			type = type + " PE=N";
		}
		
		//RRS
		if (station.getIsRRS()) {
			type = type + " RRS=Y";
		}
		else {
			type = type + " RRS=N";
		}
		
		//TEMP
		if (station.getIsTEMP()) {
			type = type + " TEMP=Y";
		}
		else {
			type = type + " TEMP=N";
		}

		if (!__verbose) {
			listItemString = station.getID() + " " + type;
		}
		else {
			listItemString = "STN: " + station.getID() + " " + type;
		}
		
		data.add(listItemString);
		Message.printStatus(1, "", listItemString);
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
        String routine = "NWSRFS_Station_JList.initialize_gui_strings"; 
	LanguageTranslator translator = null; 
	translator = LanguageTranslator.getTranslator();
        if ( translator != null ) {
		//Popupmenu
		_popup_printStn_string = translator.translate(
			"popup_printStn_string", _popup_printStn_string );
		_popup_addStn_string = translator.translate(
			"popup_addStn_string", _popup_addStn_string );
		_popup_redefStn_string = translator.translate(
			"popup_redefStn_string", _popup_redefStn_string );
		_type_string = translator.translate(
			"type_string", _type_string ) + ": ";
	}
}//end initialize_gui_strings()  

/**
Destroys and recreate JTree
*/
public void remakeList( ) {
	String routine = "NWSRFS_Main_JFrame.remakeList";

	try {
		NWSRFS nwsrfs = NWSRFS.createNWSRFSFromPRD(__fs5files, false);
		if (nwsrfs != null) {
			__nwsrfs = nwsrfs;
		}
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
	}

	displayListData();

	__parent.validate();
	__parent.repaint();
}

/**
Set the NWSRFS object 
@param nwsrfs NWSRFS data object which is used to populate the JTree.
*/
public void setListData ( NWSRFS nwsrfs ) {
	if ( Message.isDebugOn ) {	
		Message.printDebug( 5, "NWSRFS_Station_JList.setListData", 
		"NWSRFS_Station_JList.setListData  called." );
	}

	if ( nwsrfs == null ) {
		Message.printWarning( 2, "NWSRFS_Station_JList.setListData",
		"NWSRFS object (nwsrfs) is null.  Cannot populate Tree!" );
	}

	__nwsrfs = nwsrfs;

}//end setListData


/////////////     *** actions ***       ////////////
/**
Handle action events from the popup menu.
@param event ActionEvent to handle.
*/
public void actionPerformed(ActionEvent event ) {
	String routine = "NWSRFS_Station_JList.actionPerformed";
	String command = event.getActionCommand();

	if (command.equals(__printStationString)) {
		String fs = IOUtil.getPropValue("FILE_SEPARATOR" );
		if( ( fs == null ) || ( fs.length() <= 0 ) ) {
			fs = "/";
		}
		String editor = IOUtil.getPropValue("EDITOR");
		//should not be null, but just in case...
		if( ( editor == null ) || ( editor.length() <= 0 ) ) {
			editor = "vi";
		}	
		//get selected node 
		String name = (String)getSelectedItem();
		name = cleanListItemName( name );
		
		//string  to hold output from ofs command 
		//(string will be null if command failed)

		String output_str 
			= NWSRFS_Util.run_dump_station_or_area( 
			name, fs, "DUMPSTN" );
			

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
	else if (command.equals(__addStationString)) {
		NWSRFS_SystemMaintenance system_maint 
			= new NWSRFS_SystemMaintenance();
		system_maint.create_addStations_dialog();
		//recreate tree
		remakeList();
	}
	else if (command.equals(__redefineStationString)) {
		//get selected node 
		String name = (String)getSelectedItem();
		name = cleanListItemName( name );

		//dialog that does the Redefine RatingCurves is part of
		//System Maintenance class (this class will take
		//care of any String translations if needed)
		NWSRFS_SystemMaintenance system_maint 
			= new NWSRFS_SystemMaintenance();
		system_maint.create_redefStations_dialog(name);

		//remake tree
		remakeList();
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
	if (__stationListPopup != null 
		&& __stationListPopup.isPopupTrigger(event)) {
		__stationListPopup.show(event.getComponent(), 
			event.getX(), event.getY());
	}
}
  
private StopWatch __sw = null;

public void startTimer() {
        startTimer("");
}

public void startTimer(String message) {
	__sw = new StopWatch();
	__sw.clear();
	__sw.start();
	Message.printStatus(1, "", "----> (" + message 
		+ ") Timer started");
}

public void stopTimer() {
	__sw.stop();
	Message.printStatus(1, "", "<---- Timer stopped: " 
		+ __sw.getSeconds());
}

}
