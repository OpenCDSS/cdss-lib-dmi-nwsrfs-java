//-----------------------------------------------------------------------------
// NWSRFS_MAP_JTree - an object to display a simple list of rating
// curves in a JTree format.
//-----------------------------------------------------------------------------
// History:
//
// 2004-07-13	A Morgan Love, RTii	Created class based on 	
//					NWSRFS_JTree.
// 2004-09-21	AML, RTi		Updated to use NWSRFS_DMI code.
//
// 2004-09-28	Scott Townsend, RTi	Modified to get NWSRFS_MAP objects
//					from a Vector instead of Strings
// REVISIT:
// TO DO:
// Add FMAP area that MAP belongs too... in old gui, each node looked like:
//      MAP: xyz FMAP: abc
// and each node had 2 popup menu items:
//      1) view current MAP area and
//      2) view current FMAP area and
// Will need to update displayTreeData() method once
// have a method that returns the FMAP area for the
// MAP area just added to the Tree.

// When FMAP has been added to tree, will need to verify 
// the clean_node_name() method operates as expected and 
// returns just the FMAP name or just the MAP name.
//-----------------------------------------------------------------------------

package RTi.DMI.NWSRFS_DMI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Component;

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

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


/**
The NWSRFS_MAP_JTree class displays a list of the NWSRFS
stations in a JTree.
*/
public class NWSRFS_MAP_JTree extends SimpleJTree
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
protected String _popup_printMAP_string = "View Current MAP Definition";
protected String _popup_printFMAP_string = "View Current FMAP Definition";
protected String _popup_printMAT_string = "View Current MAT Definition";

//menus 
private SimpleJMenuItem __popup_printMAP_JMenuItem = null;
private SimpleJMenuItem __popup_printFMAP_JMenuItem = null;
private SimpleJMenuItem __popup_printMAT_JMenuItem = null;


/**
Constructor for NWSRFS_MAP_JTree to display Contract information.
@param parent JFrame parent Calling parent class.  
@param nwsrfs NWSRFS instance containing all the data for the JTree.
@param top_node_str String to use for top tree node.
@param fs5files  String name of fs5files used.
@param verbose Boolean indicating if extra abbreviations should be
used before each tree node's name to indicate data type of node (true).
For example: "MAP: " for MAP area and "FMAP: " for FMAP area.
@param canEdit Boolean indicating if the JTree nodes can be edited. 
@deprecated do not use this class any longer -- use NWSRFS_MAP_JPanel.
*/
public NWSRFS_MAP_JTree ( JFrame parent, NWSRFS nwsrfs,
				String top_node_str,
				String fs5files,
				boolean verbose,
				boolean canEdit ) {
				
	String routine = "NWSRFS_MAP_JTree.constructor";
	
	__parent = parent; 	
	__top_node_str = top_node_str; 	
	__verbose = verbose; 	
	__folderIcon = getClosedIcon();
	__canEdit = canEdit;
	__fs5files = fs5files;

	//assume this is always verbose!  so you get
	//the MAP: prefix ( and FMAP: prefix).
	__verbose = true;

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
Constructor for NWSRFS_MAP_JTree to display Contract information.
By default, the JTree will not display extra ("verbose") strings 
before each node name and is not editable.
@param parent JFrame parent Calling parent class.  
@param nwsrfs NWSRFS instance containing all the data for the JTree.
@param top_node_str String to use for top tree node.
@param fs5files  String name of fs5files used.
*/
public NWSRFS_MAP_JTree ( JFrame parent, NWSRFS nwsrfs, 
			String top_node_str, String fs5files ) {
		
	 this ( parent, nwsrfs, top_node_str, fs5files, false, false);

} //end constructor


/**
Removes the extra information added to the beginning of the node name
if running in verbose mode (__verbose = true ).  Assumes the prefix
ends in a colon ":".  Node should look like: "MAP: xxx FMAP: yyy" if
verbose ( or "xxx; yyy" if not verbose )
@param name Name to parse out additional prefix information if exists.
@param type Either "MAP" or "FMAP" to indicate which name to return.
*/
protected String clean_node_name ( String name, String type ) {
	String s = null;
	int ind = -999;

	if ( __verbose ) {

		if ( type.equalsIgnoreCase( "MAP" ) ) {
			//return the MAP name.  line formatted
			//as: "MAP: xxx FMAP: yyy" or if not
			//verbose: "xxx; yyyy "
		
			//parse out 1st prefix ("MAP:", assuming ends with ":"
			ind = name.indexOf(":");
			if ( ind > 0 ) {
				s = name.substring( ind + 1 ).trim();
			}
			
		}
		else if ( type.equalsIgnoreCase( "FMAP" ) ) {
			//get everything after the "FMAP:" part of the string
			ind = name.indexOf( "FMAP:");
			if ( ind > 0 ) {
				s = name.substring( ind + 1 ).trim();
			}
		}
	}
	else { //not verbose
		if ( type.equalsIgnoreCase( "MAP" ) ) {
			//return the MAP name.  line formatted
			//as: "xxx; yyyy "
		
			//parse out 1st name 
			ind = name.indexOf(";");
			if ( ind > 0 ) {
				s = name.substring( ind + 1 ).trim();
			}
			
		}
		else if ( type.equalsIgnoreCase( "FMAP" ) ) {
			//get everything after the ";" part of the string
			ind = name.indexOf( ";");
			if ( ind > 0 ) {
				s = name.substring( ind + 1 ).trim();
			}
		}
	}

	return s;
}


/**
Clear all data from the tree.
*/
public void clear () {
	String routine = "NWSRFS_MAP_JTree.clear";
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
	__popup_printMAP_JMenuItem = new SimpleJMenuItem(
	_popup_printMAP_string, this );

	__popup_printFMAP_JMenuItem = new SimpleJMenuItem(
	_popup_printFMAP_string, this );

	__popup_printMAT_JMenuItem = new SimpleJMenuItem(
	_popup_printMAT_string, this );


}// end createPopupMenu() 

/**
Display all the information in the NWSRFS data set.
*/
public void displayTreeData() {
	String routine = "NWSRFS_MAP_JTree.displayTreeData";

	Message.printStatus( 3, routine, routine + " called to create MAP tree." );


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
					 ((NWSRFS_MAP)map_obj_vect.elementAt(i)).
					 getMAPFMAPID());
				}
				else {
					map_vect.addElement("MAP: "+
					 ((NWSRFS_MAP)map_obj_vect.elementAt(i)).
					 getID());
				}
			}

		}
		
		map_vect = StringUtil.sortStringList( map_vect );
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, "Error getting list of "+
		"MAP areas to create MAP tree.  " +
		"See log file for more details." );

		Message.printWarning( 2, routine, e );

		return;
	}

	int numb_maps = 0;
        if ( map_vect != null ) {
                numb_maps = map_vect.size();
        }

        SimpleJTree_Node map_node;

        String map_str = null;
        for (int i=0; i<numb_maps; i++ ) {
                map_str = (String) map_vect.elementAt(i);
                if ( ( map_str == null ) || ( map_str.length() <=0 ) ) {
                        continue;
                }

                map_node =
                new SimpleJTree_Node(map_str);

                //just read in a list of MAP IDs as strings,
                //so do not have data objects to set node data.
                //map.setData( map );

                try {
                        addNode( map_node, __top_node );
                }
                catch ( Exception e ) {
                        Message.printWarning( 2, routine, e );
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
        String routine = "NWSRFS_MAP_JTree.initialize_gui_strings"; 
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
}//end initialize_gui_strings()  


/**
Destroys and recreate JTree
*/
public void remake_JTree( ) {
		String routine = "NWSRFS_MAP_JTRee.remake_JTree";
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
		Message.printDebug( 5, "NWSRFS_MAP_JTree.setTreeData", 
		"NWSRFS_MAP_JTree.setTreeData  called." );
	}

	if ( nwsrfs == null ) {
		Message.printWarning( 2, "NWSRFS_MAP_JTree.setTreeData",
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
	String routine = "NWSRFS_MAP_JTree.actionPerformed";
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

	if ( source == __popup_printMAP_JMenuItem ) {
		//get selected node 
		String name = node.getName();
		name = clean_node_name( name, "MAP" );
		
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
		//get selected node 
		String name = node.getName();
		name = clean_node_name( name, "FMAP" );
		
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
		//no menus at the top level
	}
	else {	
		//Object data = null;	// Data object associated with the node

		// Now reset the popup menu based on the selected node...
		//data = node.getData();
		if ( Message.isDebugOn ) {
			Message.printDebug( 2, 
			"NWSRFS_MAP_JTree.showPopupMenu",
			"Selected node text = \"" + node.getName() + "\"." );
		}

		//add popup to look at MAP definition
		__popup_JPopupMenu.add ( __popup_printMAP_JMenuItem );
//REVISIT:
//TO DO:
//once FMAP info available, add this popup.
		//__popup_JPopupMenu.add ( __popup_printFMAP_JMenuItem );

	}
	// Now display the popup so that the user can select the appropriate
	// menu item...
	__popup_JPopupMenu.show(e.getComponent(), e.getX(), e.getY());

}//end showPopupMenu

} // end NWSRFS_MAP_JTree
