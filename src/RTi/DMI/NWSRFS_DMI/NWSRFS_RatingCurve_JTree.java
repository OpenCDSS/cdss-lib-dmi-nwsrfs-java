//-----------------------------------------------------------------------------
// NWSRFS_RatingCurve_JTree - an object to display a simple list of rating
// curves in a JTree format.
//-----------------------------------------------------------------------------
// History:
//
// 2004-07-09	A Morgan Love, RTii	Created class based on 	
//					NWSRFS_JTree using NWSRFS_DMI.
// 2004-09-21	AML, RTi		Updated to use NWSRFS_DMI code.
//-----------------------------------------------------------------------------

package RTi.DMI.NWSRFS_DMI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Component;

import java.util.Vector;

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
The NWSRFS_RatingCurve_JTree class displays the NWSRFS "sysmap" which
includes: carryover group, forecast groups, segements, operations,
and time series.
*/
public class NWSRFS_RatingCurve_JTree extends SimpleJTree
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

//indicate if operations should be included in JTree
private boolean __include_operations = false;	

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
protected String _popup_printRatingCurve_string = "View Current Rating Curve Definition";
protected String _popup_addRatingCurve_string = "Add Rating Curve";
protected String _popup_deleteRatingCurve_string = "Delete Rating Curve";
protected String _popup_redefRatingCurve_string = "Redefine Rating Curve";

//menus 
private SimpleJMenuItem __popup_printRatingCurve_JMenuItem = null;
private SimpleJMenuItem __popup_addRatingCurve_JMenuItem = null;
private SimpleJMenuItem __popup_deleteRatingCurve_JMenuItem = null;
private SimpleJMenuItem __popup_redefRatingCurve_JMenuItem = null;


/**
Constructor for NWSRFS_RatingCurve_JTree to display Contract information.
@param parent JFrame parent Calling parent class.  
@param nwsrfs NWSRFS instance containing all the data for the JTree.
@param top_node_str String to use for top tree node.
@param fs5files  String name of fs5files used.
@param verbose Boolean indicating if extra abbreviations should be
used before each tree node's name to indicate data type of node (true).
For example: "RC:" for rating curve.
@param canEdit Boolean indicating if the JTree nodes can be edited. 
@deprecated do not use this class any longer -- use NWSRFS_RatingCurve_JPanel
*/
public NWSRFS_RatingCurve_JTree ( JFrame parent, NWSRFS nwsrfs,
				String top_node_str,
				String fs5files,
				boolean verbose,
				boolean canEdit ) {
				
	String routine = "NWSRFS_RatingCurve_JTree.constructor";
	
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
Constructor for NWSRFS_RatingCurve_JTree to display Contract information.
By default, the JTree will not display extra ("verbose") strings 
before each node name and is not editable.
@param parent JFrame parent Calling parent class.  
@param nwsrfs NWSRFS instance containing all the data for the JTree.
@param top_node_str String to use for top tree node.
@param fs5files  String name of fs5files used.
*/
public NWSRFS_RatingCurve_JTree ( JFrame parent, NWSRFS nwsrfs, 
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
Clear all data from the tree.
*/
public void clear () {
	String routine = "NWSRFS_RatingCurve_JTree.clear";
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
	__popup_printRatingCurve_JMenuItem = new SimpleJMenuItem(
	_popup_printRatingCurve_string, this );

	__popup_addRatingCurve_JMenuItem = new SimpleJMenuItem(
	_popup_addRatingCurve_string, this );

	__popup_deleteRatingCurve_JMenuItem = new SimpleJMenuItem(
	_popup_deleteRatingCurve_string, this );

	__popup_redefRatingCurve_JMenuItem = new SimpleJMenuItem(
	_popup_redefRatingCurve_string, this );

}// end createPopupMenu() 


/**
Display all the information in the NWSRFS data set.
*/
public void displayTreeData() {
	String routine = "NWSRFS_RatingCurve_JTree.displayTreeData";

	Message.printStatus( 3, routine, routine + " called to create Rating Curve tree." );


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
	}

	//make vector of rating curve IDs
	Vector rc_vect = null;

	NWSRFS_DMI dmi = __nwsrfs.getDMI();
	try {
	
		rc_vect = StringUtil.sortStringList( dmi.readRatingCurveList() );
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, 
		"Unable to read list of rating curves to create " +
		"rating curve tree. Please refer to log file for more " +
		"details." );

		Message.printWarning( 2, routine,  e );

		return;
	}

	int numb_rcs = 0;
	if ( rc_vect != null ) {	
		numb_rcs = rc_vect.size();
	}

	SimpleJTree_Node rc_node;

	String rc_str = null;
	for (int i=0; i<numb_rcs; i++ ) {
		rc_str = (String) rc_vect.elementAt(i);
		if ( ( rc_str == null ) || ( rc_str.length() <=0 ) ) {
			continue;
		}
		if ( ! __verbose ) {
			rc_node = 
			new SimpleJTree_Node( rc_str );
		}
		else {
			rc_node = 
			new SimpleJTree_Node( "RC: " + rc_str );
		}

		//just read in a list of Rating Curve IDs as strings,
		//so do not have data objects to set node data.
		//rc_node.setData( rc );

		try {
			addNode( rc_node, __top_node );
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
Display all the information in the NWSRFS data set.
*/
public void displayTreeData_old() {
	String routine = "NWSRFS_RatingCurve_JTree.displayTreeData_old";

	Message.printStatus( 3, routine, routine + " called to create Rating Curve tree." );

	NWSRFS_CarryoverGroup cg = null;
	NWSRFS_ForecastGroup fg = null;
	NWSRFS_Segment seg = null;
	NWSRFS_Operation op = null;
	NWSRFS_RatingCurve rc = null;

	//get carryover group user picked when GUI initiated
	String main_cg = IOUtil.getPropValue("CARRYOVERGROUP");

//Message.printStatus(1,"","ERASE:: __top_node_str = " + __top_node_str );
//Message.printStatus(1,"","ERASE:: main_cg = " + main_cg );

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
	}

	//make individual objects
	int numb_cgs = __nwsrfs.getNumberOfCarryoverGroups();
//Message.printStatus(1,"","ERASE:: numb_cgs = " + numb_cgs );
	SimpleJTree_Node cg_node;

	int numb_fgs = -99;
	SimpleJTree_Node fg_node;

	int numb_segs = -99;
	SimpleJTree_Node seg_node;

	int numb_ops = -99;
	SimpleJTree_Node op_node;

	String tsid = null;
	int numb_tsids = -99;
	SimpleJTree_Node tsid_node;

	int numb_rcs = -99;
	SimpleJTree_Node rc_node;

	NWSRFS_DMI dmi = __nwsrfs.getDMI();

	try {
	for (int icg=0; icg<numb_cgs; icg++ ) {
		cg = __nwsrfs.getCarryoverGroup(icg);

		// Check the carryover group chosen at the beginning
		// if null then get all carryover groups (dangerous!)
		if(!cg.getCGID().equalsIgnoreCase(main_cg) && cg != null)
		{
			continue;
		}

		//forecast Group
		numb_fgs = cg.getNumberOfForecastGroups();
//Message.printStatus(1,"","ERASE:: numb_fgs = " + numb_fgs );
		for (int ifg=0; ifg< numb_fgs; ifg++ ) {
			fg = cg.getForecastGroup(ifg);		
			//Segments
			numb_segs = fg.getNumberOfSegmentIDs();
//Message.printStatus(1,"","ERASE:: numb_segs = " + numb_segs );
			for (int sfg=0; sfg<numb_segs; sfg++ ) {
				seg=dmi.readSegment(fg.getSegmentID(sfg),
				fg,false);

				//Operations
				numb_ops = seg.getNumberOfOperations();
//Message.printStatus(1,"","ERASE:: numb_ops = " + numb_ops );
				for (int ofg=0; ofg<numb_ops;ofg++) {
					op=seg.getOperation(ofg);

					//RATING CURVES  !!!!!!!!
					//May not be a rating curve for 
					//each operation!
					numb_rcs = op.getNumberOfRatingCurves();
//Message.printStatus(1,"","ERASE:: numb_rcs = " + numb_rcs );
					for (int rfg=0; rfg<numb_rcs; rfg++ ) {
						rc= op.getRatingCurve(rfg);
						if ( ! __verbose ) {
							rc_node = 
							new SimpleJTree_Node( 
							rc.getRCID() );
						}
						else {
							rc_node = 
							new SimpleJTree_Node( 
							"RC: " + rc.getRCID() );
						}
						rc_node.setData( rc );
					
						try {
							addNode( rc_node, __top_node );
						}
						catch ( Exception e ) {
							Message.printWarning( 2, routine, e );
						}
						
					} //end for rfg

				} //end for ofg
			} //end for sfg	
		} //end for fgs
	} //end for cg

	}
	catch(Exception e) {
		Message.printWarning( 2, routine,
		"Unable to create Rating Curve tree." );
		Message.printWarning( 2, routine, e );
	}

	setFastAdd(false);
	//expand all nodes
	expandAllNodes();


}//end displayTreeData_old()

/**
This method is used to get the strings needed for labelling all the GUI
components only if a translation table is used for the application, ie, if
there is a translation file.  If the String cannot be located in the
translation file, the original string will be utilized in non-translated form.
*/
public void initialize_gui_strings()  {
        String routine = "NWSRFS_RatingCurve_JTree.initialize_gui_strings"; 
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
Destroys and recreate JTree
*/
public void remake_JTree( ) {
		String routine = "NWSRFS_RatingCurve_JTRee.remake_JTree";
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
		Message.printDebug( 5, "NWSRFS_RatingCurve_JTree.setTreeData", 
		"NWSRFS_RatingCurve_JTree.setTreeData  called." );
	}

	if ( nwsrfs == null ) {
		Message.printWarning( 2, "NWSRFS_RatingCurve_JTree.setTreeData",
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
	String routine = "NWSRFS_RatingCurve_JTree.actionPerformed";
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

	if ( source == __popup_printRatingCurve_JMenuItem ) {
		//get selected node 
		String name = node.getName();
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
		String name = node.getName();
		name = clean_node_name( name );

		NWSRFS_SystemMaintenance system_maint = new 
		NWSRFS_SystemMaintenance();
		system_maint.create_addRatingCurve_dialog();

		//recreate tree
		remake_JTree();

	}
	else if ( source == __popup_deleteRatingCurve_JMenuItem ) {

		//get selected node - do not need the NWSRFS_Segment
		//object, just the node name!
		String name = node.getName();
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

		//get selected node - do not need the NWSRFS_ForecastGroup
		//object, just the node name!
		String name = node.getName();
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
		__popup_JPopupMenu.add ( __popup_addRatingCurve_JMenuItem );
	}
	else {	
		//Object data = null;	// Data object associated with the node

		// Now reset the popup menu based on the selected node...
		//data = node.getData();
		if ( Message.isDebugOn ) {
			Message.printDebug( 2, 
			"NWSRFS_RatingCurve_JTree.showPopupMenu",
			"Selected node text = \"" + node.getName() + "\".");
		}

		//if ( data instanceof NWSRFS_RatingCurve ) {
			__popup_JPopupMenu.add ( __popup_printRatingCurve_JMenuItem );
			__popup_JPopupMenu.add ( __popup_deleteRatingCurve_JMenuItem );
			__popup_JPopupMenu.add ( __popup_redefRatingCurve_JMenuItem );
		//}
	}
	// Now display the popup so that the user can select the appropriate
	// menu item...
	__popup_JPopupMenu.show(e.getComponent(), e.getX(), e.getY());

}//end showPopupMenu

} // end NWSRFS_RatingCurve_JTree
