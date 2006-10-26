//-----------------------------------------------------------------------------
// NWSRFS_MAP_JList - an object to display a simple list of rating
// curves in a JList format.
//-----------------------------------------------------------------------------
// History:
//
// 2004-10-11	J. Thomas Sapienza, RTi	Initial version based on 
//					NWSRFS_MAP_JTree.
// 2004-10-12	JTS, RTi		Overhauled the cleanNodeName() method
//					since it wasn't working.
// 2006-01-18	JTS, RTi		Changed from MutableJList to 
//					SimpleJList.
// REVISIT:
// TO DO:
// Add FMAP area that MAP belongs too... in old gui, each node looked like:
//      MAP: xyz FMAP: abc
// and each node had 2 popup menu items:
//      1) view current MAP area and
//      2) view current FMAP area and
// Will need to update displayListData() method once
// have a method that returns the FMAP area for the
// MAP area just added to the list.

// When FMAP has been added to list, will need to verify 
// the cleanNodeName() method operates as expected and 
// returns just the FMAP name or just the MAP name.
//-----------------------------------------------------------------------------

package RTi.DMI.NWSRFS_DMI;

import java.awt.Component;
import java.awt.Font;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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

/**
The NWSRFS_MAP_JList class displays a list of the NWSRFS stations in a list.
*/
public class NWSRFS_MAP_JList 
extends SimpleJList
implements ActionListener, MouseListener {

//NWSRFS instance
private NWSRFS __nwsrfs;

// the font used in the main jtree in the application, used so that all the
// tabbed items look the same
private Font __listFont;

//fs5files used
private String __fs5files;

//JFrame parent
private JFrame __parent;

//Indicates whether the lines in the list should be 
//preceeded by an abbreviation indicating data type or not.
private boolean __verbose = false;	

//if the list lines can be edited
private boolean __canEdit = false;

//String for top node
private String __top_node_str = null;

// A single popup menu that is used to provide access to  other features 
//from the list.  
private JPopupMenu __popup_JPopupMenu;		

//pop menu items 
// Define STRINGs here used for menus in case we need
// to translate the strings.
protected String _popup_printMAP_string = "View Current MAP Definition";
protected String _popup_printFMAP_string = "View Current FMAP Definition";
protected String _popup_printMAT_string = "View Current MAT Definition";

//menus 
private SimpleJMenuItem __popup_printMAP_JMenuItem = null;
private SimpleJMenuItem __popup_printFMAP_JMenuItem = null;
private SimpleJMenuItem __popup_printMAT_JMenuItem = null;


/**
Constructor for NWSRFS_MAP_JList to display Contract information.
@param parent JFrame parent Calling parent class.  
@param nwsrfs NWSRFS instance containing all the data for the list.
@param top_node_str String to use for top list line.
@param fs5files  String name of fs5files used.
@param verbose Boolean indicating if extra abbreviations should be
used before each list line's name to indicate data type of node (true).
For example: "MAP: " for MAP area and "FMAP: " for FMAP area.
@param canEdit Boolean indicating if the list lines can be edited. 
@deprecated this class should not be used any longer -- use
NWSRFS_MAP_JPanel instead.
*/
public NWSRFS_MAP_JList ( JFrame parent, NWSRFS nwsrfs,
				String top_node_str,
				String fs5files,
				boolean verbose,
				boolean canEdit,
				Font listFont) {
				
	String routine = "NWSRFS_MAP_JList.constructor";
	
	__parent = parent; 	
	__top_node_str = top_node_str; 	
	__verbose = verbose; 	
	__canEdit = canEdit;
	__fs5files = fs5files;
	__listFont = listFont;

	//assume this is always verbose!  so you get
	//the MAP: prefix ( and FMAP: prefix).
	__verbose = true;

	if ( Message.isDebugOn ) {
		Message.printDebug( 4, routine,
		"Program user: \"" + IOUtil.getProgramUser() + "\" has " +
		"edit permissions (T/F)? --> " + __canEdit );
	}

	addMouseListener(this);

	//sets the nwsrfs instance to the global: __nwsrfs
	setListData(nwsrfs);

	//initialize GUI strings (translate if needed)
	initializeGUIStrings();

	//create popup menu and menu items
	createPopupMenu();

	//populate list
	displayListData();

} //end constructor

/**
Constructor for NWSRFS_MAP_JList to display Contract information.
By default, the list will not display extra ("verbose") strings 
before each line and is not editable.
@param parent JFrame parent Calling parent class.  
@param nwsrfs NWSRFS instance containing all the data for the list.
@param top_node_str String to use for top list line.
@param fs5files  String name of fs5files used.
*/
public NWSRFS_MAP_JList ( JFrame parent, NWSRFS nwsrfs, 
			String top_node_str, String fs5files ) {
		
	 this ( parent, nwsrfs, top_node_str, fs5files, false, false, null);

} //end constructor

/**
@deprecated use cleanNodeName()
*/
protected String clean_node_name(String name, String type) {
	return cleanNodeName(name, type);
}

/**
Removes the extra information added to the beginning of the node name
if running in verbose mode (__verbose = true ).  Assumes the prefix
ends in a colon ":".  Node should look like: "MAP: xxx FMAP: yyy" if
verbose ( or "xxx; yyy" if not verbose )
@param name Name to parse out additional prefix information if exists.
@param type Either "MAP" or "FMAP" to indicate which name to return.
*/
protected String cleanNodeName ( String name, String type ) {
	String s = null;
	int ind = -999;

	if (__verbose) {
		if (type.equalsIgnoreCase( "MAP" )) {
			//return the MAP name.  line formatted
			//as: "MAP: xxx FMAP: yyy"
		
			//parse out 1st prefix ("MAP:", assuming ends with ":"
			ind = name.indexOf(":");
			int index2 = name.indexOf("FMAP:");
			if (ind > 0 && index2 > 0) {
				s = name.substring(ind + 1, index2).trim();
			}
		}
		else if (type.equalsIgnoreCase("FMAP")) {
			//get everything after the "FMAP:" part of the string
			ind = name.indexOf("FMAP:");
			if (ind > 0) {
				s = name.substring(ind).trim();
				ind = s.indexOf(":");
				if (ind > 0) {
					s = s.substring(ind + 1).trim();
				}
			}
		}
	}
	else { 
		if (type.equalsIgnoreCase("MAP")) {
			//return the MAP name.  line formatted
			//as: "xxx; yyyy "
		
			//parse out 1st name 
			ind = name.indexOf(";");
			if (ind > 0) {
				s = name.substring(0, ind).trim();
			}
			
		}
		else if (type.equalsIgnoreCase("FMAP")) {
			//get everything after the ";" part of the string
			ind = name.indexOf(";");
			if (ind > 0) {
				s = name.substring( ind + 1 ).trim();
			}
		}
	}
	// Message.printStatus(1, "", "----- CNN: " + name + "  '" + s + "'");
	return s;
}

/**
Clear all data from the list.
@deprecated just use removeAll().
*/
public void clear () {
	removeAll();
}

/**
Creates the JPopupMenu and SimpleJMenuItems for the PopupMenu.  All 
Menus are created up front and they are added and removed depending
on what item is selected in the JList.
*/
private void createPopupMenu() {
	//create Popupu Menu
	__popup_JPopupMenu = new JPopupMenu();

	//popup menu items
	__popup_printMAP_JMenuItem = new SimpleJMenuItem(
	_popup_printMAP_string, this );

	__popup_printFMAP_JMenuItem = new SimpleJMenuItem(
	_popup_printFMAP_string, this );

	__popup_printMAT_JMenuItem = new SimpleJMenuItem(
	_popup_printMAT_string, this );

	__popup_JPopupMenu.add(__popup_printMAP_JMenuItem);
	__popup_JPopupMenu.add(__popup_printFMAP_JMenuItem);
//	__popup_JPopupMenu.add(__popup_printMAT_JMenuItem);
}// end createPopupMenu() 

/**
Display all the information in the NWSRFS data set.
*/
public void displayListData() {
	String routine = "NWSRFS_MAP_JList.displayListData";

	Message.printStatus(3, routine, 
		routine + " called to create MAP list.");

	removeAll();

	NWSRFS_DMI dmi = __nwsrfs.getDMI();

	//get list of MAP IDs
	Vector map_vect = new Vector();

	try {
		Vector map_obj_vect = dmi.readMAPAreaList();
		
		// Loop to pull MAP ID strings out
		for(int i = 0; i < map_obj_vect.size(); i++) {
			// Now populate the NWSRFS_MAP objects in the Vector
			// map_obj_vect to get the fmap identifier string.
			dmi.readMAPArea((NWSRFS_MAP)map_obj_vect.
			elementAt(i), false);
			if(((NWSRFS_MAP)map_obj_vect.elementAt(i)).
				getID().equalsIgnoreCase("????") || 
				((NWSRFS_MAP)map_obj_vect.elementAt(i)).
				getMAPFMAPID() == null) {
				continue;
			}
			
			if ( ! __verbose ) {
				if ( ((NWSRFS_MAP)map_obj_vect.elementAt(i)).
					getMAPFMAPID() != null ) {
					map_vect.addElement((
					(NWSRFS_MAP)map_obj_vect.elementAt(i)).
					 getID()+"; "+((NWSRFS_MAP)map_obj_vect.
					 elementAt(i)).getMAPFMAPID());
				}
				else {
					map_vect.addElement((
					 (NWSRFS_MAP)map_obj_vect.elementAt(i)).
					 getID());
				}
			}
			else {
				if ( ((NWSRFS_MAP)map_obj_vect.elementAt(i)).
					getMAPFMAPID() != null ) {
					map_vect.addElement("MAP: "+
					((NWSRFS_MAP)map_obj_vect.elementAt(i)).
					 getID()+" FMAP: "+
					 ((NWSRFS_MAP)map_obj_vect.elementAt(i))
					 .getMAPFMAPID());
				}
				else {
					map_vect.addElement("MAP: "+
					 ((NWSRFS_MAP)map_obj_vect.elementAt(i))
					 .getID());
				}
			}

		}
		
		map_vect = StringUtil.sortStringList( map_vect );
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, "Error getting list of "+
		"MAP areas to create MAP list.  " +
		"See log file for more details." );

		Message.printWarning( 2, routine, e );

		return;
	}

	int numb_maps = 0;
        if ( map_vect != null ) {
                numb_maps = map_vect.size();
        }

        String map_str = null;

	Vector data = new Vector();
        for (int i=0; i<numb_maps; i++ ) {
                map_str = (String) map_vect.elementAt(i);
                if ( ( map_str == null ) || ( map_str.length() <=0 ) ) {
                        continue;
                }
		data.add(map_str);
	}

	setListData(data);

	setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);

	// make sure the list looks similar to all the other tabbed things
	if (__listFont != null) {
		setFont(__listFont);
	}
}//end displayListData()

/**
@deprecated use initializeGUIStrings()
*/
public void initialize_gui_strings() {
	initializeGUIStrings();
}

/**
This method is used to get the strings needed for labelling all the GUI
components only if a translation table is used for the application, ie, if
there is a translation file.  If the String cannot be located in the
translation file, the original string will be utilized in non-translated form.
*/
public void initializeGUIStrings()  {
        String routine = "NWSRFS_MAP_JList.initializeGUIStrings"; 
	LanguageTranslator translator = null; 
	translator = LanguageTranslator.getTranslator();
        if ( translator != null ) {
		//Popupmenu
		_popup_printMAP_string = translator.translate(
			"popup_printMAP_string", _popup_printMAP_string );
		_popup_printFMAP_string = translator.translate(
			"popup_printFMAP_string", _popup_printFMAP_string );
		_popup_printMAT_string = translator.translate(
			"popup_printMAT_string", _popup_printMAT_string );
	}
}//end initializeGUIStrings()  


/**
Destroys and recreates list
@deprecated use remakeList().
*/
public void remake_JTree( ) {
	remakeList();
}

/**
Clears and recreates the list.
*/
public void remakeList() {
	removeAll();
	String routine = "NWSRFS_MAP_JTRee.remakeList";
	Message.printStatus( 3, routine, routine + " called." );
	try {
		setListData( NWSRFS.createNWSRFSFromPRD( __fs5files, false ) );
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, e );
	}
	displayListData();
	__parent.validate();
	__parent.repaint();
}


/**
Set the NWSRFS object 
@param nwsrfs NWSRFS data object which is used to populate the list.
*/
public void setListData ( NWSRFS nwsrfs ) {
	if ( Message.isDebugOn ) {	
		Message.printDebug( 5, "NWSRFS_MAP_JList.setListData", 
		"NWSRFS_MAP_JList.setListData  called." );
	}

	if ( nwsrfs == null ) {
		Message.printWarning( 2, "NWSRFS_MAP_JList.setListData",
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
	String routine = "NWSRFS_MAP_JList.actionPerformed";
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

	if ( source == __popup_printMAP_JMenuItem ) {
		name = cleanNodeName( name, "MAP" );
		
		//string  to hold output from ofs command 
		//(string will be null if command failed)

		output_str  = NWSRFS_Util.run_dump_station_or_area( name,
		fs, "DUMPMAP" );
			

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

	if ( source == __popup_printFMAP_JMenuItem ) {
		name = cleanNodeName( name, "FMAP" );
	
		//string  to hold output from ofs command 
		//(string will be null if command failed)

		output_str  = NWSRFS_Util.run_dump_station_or_area( name,
		fs, "DUMPFMAP" );
			
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

} // end NWSRFS_MAP_JList
