//-----------------------------------------------------------------------------
// NWSRFS_Station_JTree - an object to display a simple list of rating
// curves in a JTree format.
//-----------------------------------------------------------------------------
// History:
//
// 2004-07-13	A Morgan Love, RTii	Created class based on 	
//					NWSRFS_JTree.
// 2004-09-21	AML, RTi  		Updated to use NWSRFS_DMI.  
//
// 2004-09-30	Scott Townsend, RTi	Added the station description
//					using the dmi.readStation(...) call.
// REVISIT:
// TO DO:
// Add station name description next to identifier.
// Once name added, will need to verify that the update clean_node_name() 
// method to operates as expected and returns just the station ID
//-----------------------------------------------------------------------------

package RTi.DMI.NWSRFS_DMI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Component;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import RTi.DMI.NWSRFS_DMI.NWSRFS_Station;
import RTi.DMI.NWSRFS_DMI.NWSRFS_Util;

import RTi.Util.GUI.ReportJFrame;
import RTi.Util.GUI.SimpleJMenuItem;
import RTi.Util.GUI.SimpleJTree;
import RTi.Util.GUI.SimpleJTree_Node;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.LanguageTranslator;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

//import RTi.App.NWSRFSGUI.NwsrfsGUI_SystemMaintenance;
//import RTi.App.NWSRFSGUI.NwsrfsGUI_Util;

import RTi.Util.Time.StopWatch;


/**
The NWSRFS_Station_JTree class displays a list of the NWSRFS
stations in a JTree.
*/
public class NWSRFS_Station_JTree extends SimpleJTree
implements ActionListener, MouseListener
{

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

//if the Tree nodes can be edited
private boolean __canEdit = false;

//String for top node
private String __top_node_str = null;
//Top node of tree 
private SimpleJTree_Node __top_node = null;

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


/**
Constructor for NWSRFS_Station_JTree to display Contract information.
@param parent JFrame parent Calling parent class.  
@param nwsrfs NWSRFS instance containing all the data for the JTree.
@param top_node_str String to use for top tree node.
@param fs5files  String name of fs5files used.
@param verbose Boolean indicating if extra abbreviations should be
used before each tree node's name to indicate data type of node (true).
For example: "STN:" for station.
@param canEdit Boolean indicating if the JTree nodes can be edited. 
@deprecated do not use this class any longer use NWSRFS_Station_JPanel
*/
public NWSRFS_Station_JTree ( JFrame parent, NWSRFS nwsrfs,
				String top_node_str,
				String fs5files,
				boolean verbose,
				boolean canEdit ) {
				
	String routine = "NWSRFS_Station_JTree.constructor";
	
	__parent = parent; 	
	__top_node_str = top_node_str; 	
	__verbose = verbose; 	
	__folderIcon = getClosedIcon();
	__canEdit = canEdit;
	__fs5files = fs5files;

	if ( Message.isDebugOn ) {
		Message.printDebug( 4, routine,
		"Program user: \"" + IOUtil.getProgramUser() + "\" has " +
		"edit permissions (T/F)? --> " + __canEdit );
	}

	showRootHandles ( true );
	addMouseListener(this);
	setLeafIcon(null);
	setTreeTextEditable ( false );


	//sets the nwsrfs instance to the global: __nwsrfs
	setTreeData(nwsrfs);

	//initialize GUI strings (translate if needed)
	initialize_gui_strings();

	//create popup menu and menu items
	createPopupMenu();

	//populate tree
	displayTreeData();

} //end constructor

/**
Constructor for NWSRFS_Station_JTree to display Contract information.
By default, the JTree will not display extra ("verbose") strings 
before each node name and is not editable.
@param parent JFrame parent Calling parent class.  
@param nwsrfs NWSRFS instance containing all the data for the JTree.
@param top_node_str String to use for top tree node.
@param fs5files  String name of fs5files used.
*/
public NWSRFS_Station_JTree ( JFrame parent, NWSRFS nwsrfs, 
			String top_node_str, String fs5files ) {
		
	 this ( parent, nwsrfs, top_node_str, fs5files, false, false);

} //end constructor


/**
Removes the extra information added to the beginning of the node name
if running in verbose mode (__verbose = true ).  Assumes the prefix
ends in a colon ":".
@param name Name to parse out additional prefix information if exists.
*/
protected String clean_node_name ( String name ) {

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
	String routine = "NWSRFS_Station_JTree.clear";
	SimpleJTree_Node node = getRoot();
	Vector v = getChildrenVector(node);
	int size = 0;
	if ( v != null ) {
		size = v.size();
	}
	for ( int i = 0; i < size; i++ ) {
		try {	removeNode (
			(SimpleJTree_Node)v.elementAt(i), false );
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, routine,
			"Cannot remove node " + node.toString() );
			Message.printWarning ( 2, routine, e );
		}
	}
}

/**
Creates the JPopupMenu and SimpleJMenuItems for the PopupMenu.  All 
Menus are created up front and they are added and removed depending
on what item is selected in the JTree.
*/
private void createPopupMenu() {
	//create Popupu Menu
	__popup_JPopupMenu = new JPopupMenu();

	//popup menu items
	__popup_printStn_JMenuItem = new SimpleJMenuItem(
	_popup_printStn_string, this );

	__popup_addStn_JMenuItem = new SimpleJMenuItem(
	_popup_addStn_string, this );

	__popup_redefStn_JMenuItem = new SimpleJMenuItem(
	_popup_redefStn_string, this );

}// end createPopupMenu() 

/**
Display all the information in the NWSRFS data set.
*/
public void displayTreeData() {
	String routine = "NWSRFS_Station_JTree.displayTreeData";

	Message.printStatus( 3, routine, routine + " called to create station tree." );

	//make top node with String passed in and Add it immediately
	__top_node = new SimpleJTree_Node( __top_node_str );
	try {
		addNode( __top_node );
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, "Error adding top node " +
		"to rating curve tree ( " + __top_node_str + " ).  Tree will " +
		"not display.  See log file for more details." );

		Message.printWarning( 2, routine, e );

		return;
	}

	NWSRFS_DMI dmi = __nwsrfs.getDMI();
	
	Hashtable hash = null;
	try {
		hash = dmi.readStationHashtable();
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, "Error reading in " +
		"list of stations to create station tree.  Please see " +
		"log file for more details." );
		Message.printWarning( 2, routine, e );	
		return;
	}
	NWSRFS_Station station = null;
	String stn_id = null;
	SimpleJTree_Node stn_node = null;

	// pull the IDs out of the hash table in whatever order they're in
	// and put them in a separate Vector -- this Vector is going to 
	// be sorted
	Vector stationIDs = new Vector();
	for ( Enumeration e = hash.keys(); e.hasMoreElements(); ) {
		stn_id = (String)e.nextElement();
		stationIDs.add(stn_id);
	}

	// take the Vector of station IDs and sort it alphabetically.
	Vector sortedStationIDs = StringUtil.sortStringList(stationIDs);

	// iterate through the sorted station ID list and use the station 
	// IDs to pull out station objects from the hash table
	int size = sortedStationIDs.size();
	for (int i = 0; i < size; i++) {
		stn_id = (String)sortedStationIDs.elementAt(i);

		station = (NWSRFS_Station) hash.get(stn_id);

		if ( station == null || !StringUtil.isASCII(station.getID())) {
			continue;
		}

		//Define the station type.
		String type = "  ";

		// Get the Station description. To do this we need to populate
		// the station object from the PPPPARMn preproccessed parameteric
		// database with a call to dmi.reasStation(...). We do not do a 
		// deepRead (set to false) to just get the description!
		String stn_desc = null;
		try {
			dmi.readStation(station,false);
			stn_desc = station.getDescription();
		}
		catch(Exception e)
		{
			Message.printWarning( 10, routine, "Error reading in "+
			"the station description to create station tree." );
			Message.printWarning( 10, routine, e );	
			continue;
		}
		if ( stn_desc != null ) {
			//add description in front the the station 
			//type and surround it with quotes.
			type = type + "\"" + stn_desc + "\"";
		}	

		//get Station ID put together full Station node text,
		//which includes an indication of station type.
		type = type + "  " + _type_string + ": ";
		//PCPN
		if ( station.getIsPCPN() ) {
			type = type + "PCPN=Y";
		}
		else {
			type = type + "PCPN=N";
		}
		//PE
		if ( station.getIsPE() ) {
			type = type + " PE=Y";
		}
		else {
			type = type + " PE=N";
		}
		//RRS
		if ( station.getIsRRS() ) {
			type = type + " RRS=Y";
		}
		else {
			type = type + " RRS=N";
		}
		//TEMP
		if ( station.getIsTEMP() ) {
			type = type + " TEMP=Y";
		}
		else {
			type = type + " TEMP=N";
		}

		if ( ! __verbose ) {
			stn_node = new SimpleJTree_Node( station.getID() +
			" " + type );
		}
		else {
			stn_node = new SimpleJTree_Node( "STN: " + 
			station.getID() + " " + type );
		}
		
		

		//add node to tree
    		try {
                        addNode( stn_node, __top_node );
                }
                catch ( Exception ex ) {
                        Message.printWarning( 2, routine, ex );
                }
	}

	setFastAdd(false);
	//expand all nodes
	expandAllNodes();

}//end displayTreeData()



/**
This method is used to get the strings needed for labelling all the GUI
components only if a translation table is used for the application, ie, if
there is a translation file.  If the String cannot be located in the
translation file, the original string will be utilized in non-translated form.
*/
public void initialize_gui_strings()  {
        String routine = "NWSRFS_Station_JTree.initialize_gui_strings"; 
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
public void remake_JTree( ) {
		String routine = "NWSRFS_Station_JTRee.remake_JTree";
		Message.printStatus( 3, routine, routine + " called." );
		try {
			setTreeData( NWSRFS.createNWSRFSFromPRD( __fs5files, false ) );
		}
		catch ( Exception e ) {
			Message.printWarning( 2, routine, e );
		}
		displayTreeData( );

		__parent.validate();
		__parent.repaint();
}


/**
Set the NWSRFS object 
@param nwsrfs NWSRFS data object which is used to populate the JTree.
*/
public void setTreeData ( NWSRFS nwsrfs ) {
	if ( Message.isDebugOn ) {	
		Message.printDebug( 5, "NWSRFS_Station_JTree.setTreeData", 
		"NWSRFS_Station_JTree.setTreeData  called." );
	}

	if ( nwsrfs == null ) {
		Message.printWarning( 2, "NWSRFS_Station_JTree.setTreeData",
		"NWSRFS object (nwsrfs) is null.  Cannot populate Tree!" );
	}

	__nwsrfs = nwsrfs;

}//end setTreeData


/////////////     *** actions ***       ////////////
/**
Handle action events from the popup menu.
@param event ActionEvent to handle.
*/
public void actionPerformed(ActionEvent event ) {
	String routine = "NWSRFS_Station_JTree.actionPerformed";
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

	SimpleJTree_Node node = getSelectedNode();
	String output_str = null; 

	if ( source == __popup_printStn_JMenuItem ) {
		//get selected node 
		String name = node.getName();
		name = clean_node_name( name );
		
		//string  to hold output from ofs command 
		//(string will be null if command failed)

		output_str  = NWSRFS_Util.run_dump_station_or_area( name,
		fs, "DUMPSTN" );
			

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
	else if ( source == __popup_addStn_JMenuItem ) {
		NWSRFS_SystemMaintenance system_maint = new 
		NWSRFS_SystemMaintenance();
		system_maint.create_addStations_dialog();

		//recreate tree
		remake_JTree();

	}
	else if ( source == __popup_redefStn_JMenuItem ) {
		//get selected node 
		String name = node.getName();
		name = clean_node_name( name );

		//dialog that does the Redefine RatingCurves is part of
		//System Maintenance class (this class will take
		//care of any String translations if needed)
		NWSRFS_SystemMaintenance system_maint = new 
		NWSRFS_SystemMaintenance();
		system_maint.create_redefStations_dialog(name);

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
	showPopupMenu(event);
}

/**
Checks to see if the mouse event would trigger display of the popup menu.
The popup menu does not display if it is null.
@param e the MouseEvent that happened.
*/
private void showPopupMenu (MouseEvent e) {
	//popup to add depends on kind of element selected

	//selected tree node
	SimpleJTree_Node node = null;
	node = getSelectedNode();

	// First remove the menu items that are currently in the menu...
	__popup_JPopupMenu.removeAll();

	//see if we are on TOP node
	if ( node.getText().equalsIgnoreCase( __top_node.getName() ) ) {
		//add Add rating curve menu item
		__popup_JPopupMenu.add ( __popup_addStn_JMenuItem );
	}
	else {	
		//Object data = null;	// Data object associated with the node

		// Now reset the popup menu based on the selected node...
		//data = node.getData();
		if ( Message.isDebugOn ) {
			Message.printDebug( 2, 
			"NWSRFS_Station_JTree.showPopupMenu",
			"Selected node text = \"" + node.getName() + "\".");
		}
		//if ( data instanceof NWSRFS_Station ) {
			__popup_JPopupMenu.add ( __popup_printStn_JMenuItem );
			__popup_JPopupMenu.add ( __popup_redefStn_JMenuItem );
		//}
	}
	// Now display the popup so that the user can select the appropriate
	// menu item...
	__popup_JPopupMenu.show(e.getComponent(), e.getX(), e.getY());

}//end showPopupMenu

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

} // end NWSRFS_Station_JTree
