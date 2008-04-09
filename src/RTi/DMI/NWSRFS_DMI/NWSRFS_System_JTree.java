//-----------------------------------------------------------------------------
// NWSRFS_System_JTree - an object to display the NWSRFS system
// in a JTree format that includes: carryover group, forecast group,
// segments, operations, and time series.
//-----------------------------------------------------------------------------
// History:
//
// 2004-07-08	A Morgan Love, RTii	Created class based on 	
//					NWSRFS_JTree.
//
// 2004_07_12	AML, RTi		Added flag which is used to 
//					check to see is time series 
//					has data, only upon a node
//					expansion event (when an operation
//					node is expanded). 
//
//					* If __checkTS is set to FALSE, 
//					then the nodeExpansion
//					event will make a call to check
//					each Time Series for data only
//					upon node expansion.   The
//					SimpleJTree_Listener class is
//					implemented to listen for node
//					expansion events.  Time series
//					that do not have data are still
//					displayed, but their names are
//					appended with a "No data -" flag.
//
//					** If __checkTS	is TRUE, 
//					then all the TS are checked
//					upfront as the Tree is created.  If 
//					__checkTS is true and __useAllTS flag
//					is also true, all the TS will be
//					added to the tree upfront, but the
//					time series with no data will be
//					appended with the "No data -" string.
//
// 2004_08_04	AML, RTi		Added PropList used to set 
//					many of the flags used when 
//					setting up tree.  Flags include:
//					- set whether all Time Series 
//					should be read in upfront.
//					(sets the __checkTS flag)
//					- set whether all Time Series
//					should be displayed or only those
//					with data.
//					(sets __useAllTS flag).
//					- sets flag to display or
//					hide rating curves in the system 
//					tree.
//					(sets __include_ratingCurves).
//					- sets flags to indicate which
//					operations should be included
//					in tree.
//					-added flags to indicate if tree
//					is to be used in NWSRFSGUI 
//					(__forNWSRFSGUI) or for
//					BPA Snow Updating GUI 
//					(__forSnowUpdating).  These flags
//					determine what popup menus are 
//					added and used.
//					- set whether tree show be verbose
//					(using abbreviations before nodes
//					such as FG:, SEG:, etc ) 
//					( flag is __verbose)
//					- set whether Operations should be
//					included (__include_all_operations )
//					
// 2004-11-02	Steven A. Malers, RTi	* Call NWSRFS_Util.plotTimeSeries()
//					  instead of
//					  NWSRFS_Util.plotSelectedTimeSeries()
//					  and pass null dates to display all
//					  processed database time series period.
//					* Print warnings at level 1 when time
//					  series cannot be graphed - the
//					  user-initiated actions must result
//					  in a visible warning!
//					* Remove plotSelectedTimeSeries()
//					  method used with the snow update GUI.
//					  It plotted the full period, which is
//					  now the default.
// 2004-12-06	J. Thomas Sapienza, RTi	Users can now graph more than one time
//					series at a time from the right-click
//					"Graph Selected Time Series" menu item.
//-----------------------------------------------------------------------------
// EndHeader

package RTi.DMI.NWSRFS_DMI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.File;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import RTi.DMI.NWSRFS_DMI.NWSRFS;
import RTi.DMI.NWSRFS_DMI.NWSRFS_CarryoverGroup;
import RTi.DMI.NWSRFS_DMI.NWSRFS_ForecastGroup;
import RTi.DMI.NWSRFS_DMI.NWSRFS_Operation;
import RTi.DMI.NWSRFS_DMI.NWSRFS_Segment;
import RTi.DMI.NWSRFS_DMI.NWSRFS_RatingCurve;
import RTi.DMI.NWSRFS_DMI.NWSRFS_SystemMaintenance;
import RTi.DMI.NWSRFS_DMI.NWSRFS_TimeSeries;
import RTi.DMI.NWSRFS_DMI.NWSRFS_Util;
import RTi.Util.GUI.SimpleJMenuItem;
import RTi.Util.GUI.SimpleJTree;
import RTi.Util.GUI.SimpleJTree_Node;
import RTi.Util.GUI.SimpleJTree_Listener;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.LanguageTranslator;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Time.StopWatch;
//import RTi.App.NWSRFSGUI.NwsrfsGUI_SystemMaintenance;
//import RTi.App.NWSRFSGUI.NwsrfsGUI_Util;
//import RTi.App.NWSRFSSnowUpdating.NWSRFSSnowUtil;

/**
The NWSRFS_System_JTree class displays the NWSRFS "sysmap" which
includes: carryover group, forecast groups, segments, operations, and time series.
*/
public class NWSRFS_System_JTree extends SimpleJTree
implements ActionListener, MouseListener, SimpleJTree_Listener {

//parent JFrame
private JFrame __parent;

//NWSRFS instance
private NWSRFS __nwsrfs;

//string for top tree node
private String __top_node_str;

//Top node of tree 
private SimpleJTree_Node __top_node = null;

//fs5files
private String __fs5files;

//flags that can be re-set by PropList passed in to constructor.

//If true, reads in all Time series and tests to see if each 
//Time Series added to the JTree has data.  If the TS has no data and
//__useAllTS flag is true, the node is added to the JTree,
//but a "no data" message is attached to it.  If the 
//__useAllTS flag is false, time series that do not have
//data are simply not added to the JTree. 
//If __checkTS if false, time series are *not* read in up
//front, but are read in only on an event kicked off by
//the expansion of the operation node under which the TS falls.
private boolean __checkTS =true;

//if we just want to add ALL time series (with an 
//indication of if they do not have any data or not).
private boolean __useAllTS =true;

//For SnowGUI, we only display operations of type:  SNOW-17 and time series of type: SWE
private boolean __useOnlySnowTSandOperations = false;
private boolean __use_all_operations = true;

//if we are making NWSRFSGUI or SnowUpdating GUI
private boolean __forNWSRFSGUI =true;
private boolean __forSnowUpdate =false;

//indicate if operations should be included in JTree
private boolean __include_all_operations = true;	

//include rating curves in system tree
private boolean __include_ratingCurves= false;

//Indicates whether the nodes in the JTree should be 
//preceded by an abbreviation indicating data type or not.
private boolean __verbose = true;	

//folder icon
private Icon __folderIcon;

//if the Tree nodes can be edited --currently not used.
//private boolean __canEdit = false;

// A single popup menu that is used to provide access to other features 
//from the tree.  The single menu has its items added/removed as necessary 
//based on the state of the tree.
private JPopupMenu __popup_JPopupMenu;		

//pop menu items 
// Define STRINGs here used for NWSRFSGUI menus in case we need
// to translate the strings.  Do not need to add popup menu
// items for the Snow Updating GUI since this GUI is not translated.
protected String _popup_graphTS_string = "Graph Selected Time Series";
protected String _popup_printCG_string = "View Current Carryover Group Definition";
protected String _popup_printFG_string = "View Current Forecast Group Definition";
protected String _popup_printSegs_string = "View Current Segment Definition";
protected String _popup_redefSegs_string = "Redefine Segment";

//menus 
private SimpleJMenuItem __popup_graphTS_JMenuItem = null;
private SimpleJMenuItem __popup_printCG_JMenuItem = null;
private SimpleJMenuItem __popup_printFG_JMenuItem = null;
private SimpleJMenuItem __popup_printSegs_JMenuItem = null;
private SimpleJMenuItem __popup_redefSegs_JMenuItem = null;

//SnowGUI only has 1 popup menu
private SimpleJMenuItem __popup_snow_selectSubareas_JMenuItem = null;

/**
Constructor for NWSRFS_System_JTree to display NWSRFS system information.
By default, the tree is created as a NWSRFSGUI JTree, with verbose
nodes (additional abbreviations before node name to indicate node 
type), operations and rating curves are not included in the JTree. Also,
by default, all Time Series are read in up front and all time series
are included in the tree, whether they have data or not.
@param parent JFrame parent Calling parent class.  
@param nwsrfs NWSRFS instance containing all the data for the JTree.
@param top_node_str String to use for top tree node.
@param fs5files String for fs5files used.
@param properties PropList containing any of the following properties:
<table>
<tr>
<th>Property Name</th> <th>Property Description</th> <th>Default Value (if not defined)</th>
</tr>
<tr>
<td>SystemJTree.checkTS</td>
<td>boolean indicates if all Time Series should be read in up front as
tree is being created and checked to see if they actually contain data.
If the time series does not have data, it is added to the JTree with
an indication ("no data" text appended to the name) that the time series
is defined, but has no data), unless the flag below, useAllTS, is set 
to false. If useAllTS is false, then if the Time series node does not
have data, it is not added to the tree.</td>
<td>true</td>
</tr>

<td>SystemJTree.useOnlySnowTSandOperations</td>
<td>boolean indicating that only time series of type SWE and
operation: SNOW-17 are displayed on the tree. 
Used only if checkTS set to True. If this is true, __include_all_operations is set to False </td>
<td>false</td>
</tr>

<tr>
<td>SystemJTree.forNWSRFSGUI</td>
<td>boolean to indicate if System JTree is for NWSRFSGUI, which will include popups to print
current definitions, redefine, and graph time series.  If this is set to false, then popups will
provide display but not redefine choices).</td>
<td>true</td>
</tr>

<tr>
<td>SystemJTree.forSnowUpdate</td>
<td>boolean to indicate if System JTree is for SnowUpdatingGUI.  If true, only SNOW-17 operations
are displayed and popups will not include redefine.</td>
<td>false</td>
</tr>

<tr>
<td>SystemJTree.include_all_operations</td>
<td>boolean indicating if Operations should be included in System Tree (false means no
operations will be shown).</td>
<td>true</td>
</tr>

<tr>
<td>SystemJTree.include_ratingCurves</td>
<td>boolean indicating if Rating Curves should be included in System Tree</td>
<td>false</td>
</tr>

<tr>
<td>SystemJTree.useAllTS</td>
<td>boolean indicating if all Time Series should be included in JTree 
whether they have data or not.
If time series has no data, but this 
flag is true, the time series is added to the JTree, with "no data" 
appended to its name to indicate it has no data.  If this flag is false
and a time series has no data, it is not added to the JTree.</td>
<td>true</td>
</tr>

<tr>
<td>SystemJTree.verbose</td>
<td>boolean to indicate if extra abbreviations should be used before 
each tree node's name to indicate data type of node ( for example: "seg:" 
for segment, "fg:" for forecast group, etc.)
</td>
<td>true</td>
</tr>
</table>
*/
public NWSRFS_System_JTree ( JFrame parent, NWSRFS nwsrfs, String top_node_str, String fs5files,
				PropList properties ) {
				
	String routine = "NWSRFS_System_JTree.constructor";
	if ( Message.isDebugOn ) {
		Message.printDebug( 2, routine, "Routine " + routine + " called." );
	}
	
	__parent = parent;
	__top_node_str = top_node_str;
	__folderIcon = getClosedIcon();
	__fs5files = fs5files;

	//set Properties
	if ( Message.isDebugOn ) {
		Message.printDebug( 2, routine, "Initializing properties for NWSRFS System Tree.");
	}
	initialize_properties( properties );

	//initialize GUI strings (translate if needed)
	initialize_gui_strings();

	//sets the nwsrfs instance to the global: __nwsrfs
	setTreeData(nwsrfs);

	showRootHandles ( true );
	addMouseListener(this);
	setLeafIcon(null);
	setTreeTextEditable ( false );

	//create popup menu and menu items
	createPopupMenu();

	//populate tree
	if ( Message.isDebugOn ) {
		Message.printDebug( 2, routine, "Populating NWSRFS System Tree.");
	}
	displayTreeData();

	if ( ! __checkTS ) {
		addSimpleJTreeListener(this );
	}

} //end constructor

/**
Constructor for NWSRFS_System_JTree to display NWSRFS system information.
By default, the tree is created as a NWSRFSGUI JTree, without verbose
nodes (no additional abbreviations before node name to indicate node 
type), operations and rating curves are not included in the JTree. Also,
by default, all Time Series are read in up front and all time series
are included in the tree, whether they have data or not.
@param parent JFrame parent Calling parent class.  
@param nwsrfs NWSRFS instance containing all the data for the JTree.
@param top_node_str String to use for top tree node.
@param fs5files String for fs5files used.
*/
public NWSRFS_System_JTree ( JFrame parent, NWSRFS nwsrfs, String top_node_str, String fs5files )
{
	 this ( parent, nwsrfs, top_node_str, fs5files, new PropList("") );
} //end constructor

/**
Removes the extra information added to the beginning of the node name
if running in verbose mode (__verbose = true ).  Assumes the prefix ends in a colon ":".
@param name Name to parse out additional prefix information if exists.
*/
protected String clean_node_name ( String name ) {
	if ( ! __verbose ) {
		return name;
	}
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
	String routine = "NWSRFS_System_JTree.clear";
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
			Message.printWarning ( 2, routine, "Cannot remove node " + node.toString() );
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

	StopWatch stopwatch = new StopWatch();
	stopwatch.start();

	//create Popupu Menu
	__popup_JPopupMenu = new JPopupMenu();
	
	if ( __forNWSRFSGUI ) {
		//popup menu items
		__popup_printCG_JMenuItem = new SimpleJMenuItem(_popup_printCG_string, this );
		__popup_printFG_JMenuItem = new SimpleJMenuItem(_popup_printFG_string, this );
		__popup_printSegs_JMenuItem = new SimpleJMenuItem(_popup_printSegs_string, this );
		__popup_redefSegs_JMenuItem = new SimpleJMenuItem(_popup_redefSegs_string, this );
		__popup_graphTS_JMenuItem = new SimpleJMenuItem(_popup_graphTS_string, this );
	}
	else if ( __forSnowUpdate ) {
		// menu item to select all children nodes
		__popup_snow_selectSubareas_JMenuItem = new SimpleJMenuItem("Select SubAreas", this );
		__popup_graphTS_JMenuItem = new SimpleJMenuItem(_popup_graphTS_string, this );
	}
	else {
		// Default is basic read-only operations but not maintenance.
		__popup_printCG_JMenuItem = new SimpleJMenuItem(_popup_printCG_string, this );
		__popup_printFG_JMenuItem = new SimpleJMenuItem(_popup_printFG_string, this );
		__popup_printSegs_JMenuItem = new SimpleJMenuItem(_popup_printSegs_string, this );
		__popup_graphTS_JMenuItem = new SimpleJMenuItem(_popup_graphTS_string, this );
	}

	stopwatch.stop();
	if ( Message.isDebugOn ) {
		Message.printDebug( 3, "", "Time to create popup menu, stopwatch seconds =" + stopwatch.getSeconds() );
	}
}// end createPopupMenu() 
	
/**
Display all the information in the NWSRFS data set.
*/
public void displayTreeData() {
	String routine = "NWSRFS_System_JTree.displayTreeData";

	StopWatch stopwatch = new StopWatch();
	stopwatch.start();

	Message.printStatus( 3, routine, routine + " called to create system tree." );

	//optimize tree creation
	setLargeModel( true );

	NWSRFS_CarryoverGroup cg = null;
	NWSRFS_ForecastGroup fg = null;
	NWSRFS_Segment seg = null;
	NWSRFS_Operation op = null;
	NWSRFS_RatingCurve rc = null;

	String main_cg = null;
	if ( __forNWSRFSGUI ) {
		// Get carryover group user picked when GUI initiated
		main_cg = IOUtil.getPropValue("CARRYOVERGROUP");
		if ( Message.isDebugOn ) {
			Message.printDebug( 2, routine,	"Carryover Group for NWSRFSGUI Session= " + main_cg );
		}
	}

	setFastAdd( true );
	//make top node
	__top_node = new SimpleJTree_Node( __top_node_str );
	try {
		addNode( __top_node );
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, "Error adding top node " +
		"to system tree ( " + __top_node_str + " ).  Tree will " +
		"not display.  See log file for more details." );
		Message.printWarning( 2, routine, e );
	}
	if ( Message.isDebugOn ) {	
		Message.printDebug( 5, routine,	"Top Node = " + __top_node_str );
	}

	//make individual objects
	int numb_cgs = __nwsrfs.getNumberOfCarryoverGroups();
	SimpleJTree_Node cg_node;
	if ( Message.isDebugOn ) {	
		Message.printDebug( 5, routine,	"Number of Carryover Groups = " + numb_cgs );
	}

	int numb_fgs = -99;
	SimpleJTree_Node fg_node;

	int numb_segs = -99;
	SimpleJTree_Node seg_node;

	int numb_ops = -99;
	SimpleJTree_Node op_node;

	int numb_tsids = -99;
	SimpleJTree_Node tsid_node;

	int numb_rcs = -99;
	SimpleJTree_Node rc_node;

	NWSRFS_DMI dmi = __nwsrfs.getDMI();

	Vector nodes_to_expand_vect = new Vector();

	try {
	for (int icg=0; icg<numb_cgs; icg++ ) {
		cg = __nwsrfs.getCarryoverGroup(icg);
		if ( cg == null ) {
			Message.printWarning( 2, routine, "Carryover Group null. Unable to create System Tree.");
			break;
		}

//		Message.printStatus(2, routine, "CGID: " + cg.getCGID());
		//create CG node
		if ( ! __verbose ) {
			cg_node = new SimpleJTree_Node( cg.getCGID() );
		}
		else {
			cg_node = new SimpleJTree_Node( "CG: " + cg.getCGID() );
		}

		cg_node.setData( cg );
		cg_node.setIcon( __folderIcon );

		//add node
		try {
			addNode( cg_node, __top_node );
		}
		catch ( Exception e ) {
			Message.printWarning( 2, routine, e );
		}

		// Forecast Group
		numb_fgs = cg.getNumberOfForecastGroups();
		if ( Message.isDebugOn ) {	
			Message.printDebug( 5, routine,	"Number of forecast groups = " + numb_fgs );
		}
		for (int ifg=0; ifg< numb_fgs; ifg++ ) {
			fg = cg.getForecastGroup(ifg);		
			//add fg node to tree
			if ( ! __verbose ) {
				fg_node = new SimpleJTree_Node( fg.getFGID() );
			}
			else {
				fg_node = new SimpleJTree_Node( "FG:" + fg.getFGID() );
			}
// 			Message.printStatus(2, routine, "FGID: " + fg.getFGID());
			fg_node.setData( fg );
			fg_node.setIcon( __folderIcon );
			try {
				addNode( fg_node, cg_node);
			}
			catch ( Exception e ) {
				Message.printWarning( 2, routine, e );
			}

			nodes_to_expand_vect.addElement( fg_node );

			//Segments
			numb_segs = fg.getNumberOfSegmentIDs();
			if ( Message.isDebugOn ) {	
				Message.printDebug( 5, routine, "Number of segments for forecast " +
				"group: \"" + fg_node.toString() + "\" = " + numb_segs );
			}
			for (int sfg=0; sfg<numb_segs; sfg++ ) {
				seg=dmi.readSegment(fg.getSegmentID(sfg),fg,false);

			StopWatch tsw = new StopWatch();
			tsw.start();
				//add node
				if ( ! __verbose ) {
					seg_node = new SimpleJTree_Node(seg.getSegID());
				}
				else {
					seg_node = new SimpleJTree_Node( "SEG: " + seg.getSegID());
				}
				seg_node.setData( seg) ;
				seg_node.setIcon( __folderIcon );

				try {
					addNode( seg_node, fg_node );
				}
				catch ( Exception e ) {
					Message.printWarning( 2, routine, e );
				}

				// If checking TS for data only when operation node is expanded, 
				// do not expand tree nodes passed operation level at creation time.
				if ( ! __checkTS ) {
					setExpandAllowed( false );
				}

				//Operations
				numb_ops = seg.getNumberOfOperations();
				if ( Message.isDebugOn ) {	
					Message.printDebug( 5, routine,	"Number of operations for segment \"" +
					seg.toString() +  "\" = " + numb_ops );
				}
				StopWatch opsw = new StopWatch();
				opsw.start();
				for (int ofg=0; ofg<numb_ops;ofg++) {
					op=seg.getOperation(ofg);
					//add node 
					String op_type = op.getSystemID();
					if ( ! __verbose ) {
						op_node = new SimpleJTree_Node(	op_type );
				 		//op.getSystemID());
					}
					else {
						op_node = new SimpleJTree_Node(	"OP: " + op.getSystemID());
					}
					op_node.setData( op );
					op_node.setIcon( __folderIcon );

//					Message.printStatus(2, routine, "OPID: " + op.getSystemID());

					//flag to exclude operations.
					if ( __include_all_operations ) {
						try {
							addNode( op_node, seg_node );
						}
						catch ( Exception e ) {
							Message.printWarning( 2, routine, e );
						}
					}
					else {
						//not including operations, set node to null.
						op_node = null;
					}

					//Time series
					numb_tsids=op.getNumberOfTSIDs();
					if ( Message.isDebugOn ) {
						Message.printDebug( 5, routine,
						"Number of time series = " + numb_tsids  + " for operation " + numb_tsids );
					}

					for( int tsg=0;tsg<numb_tsids;tsg++ ) {
						if ( ! __verbose ) {
							tsid_node = new SimpleJTree_Node(op.getTSID(tsg)+".NWSRFS."+
									op.getTSDT(tsg)+"."+ (op.getTimeSeries(tsg)).getTSDTInterval()+	"Hour") ;
						}
						else {
							tsid_node = new SimpleJTree_Node( "TS: " + op.getTSID(tsg)+	".NWSRFS."+
									op.getTSDT(tsg)+"."+ (op.getTimeSeries(tsg)).getTSDTInterval()+	"Hour") ;
						}
						tsid_node.setData(op.getTimeSeries(tsg));

						// If __checkTS flag is true, then read in all time series
						// If _checkTS is False, do not read in the Time series up front
						if ( __checkTS ) {
							if ( __useAllTS ) {
								//see if operations are included.
								if ( op_node != null ) {
									try {
										addNode( tsid_node, op_node );
									}
									catch ( Exception e ) {
										Message.printWarning( 2, routine, e );
									}
								}
								else { //add node to seg because operation node is null	
									if( ( __useOnlySnowTSandOperations ) &&	(op_type.indexOf("SNOW-17") >=0 ) &&
									( op.getTSDT(tsg).indexOf( "SWE") > 0 )) {
										try {
											addNode( tsid_node, seg_node );
										}
										catch ( Exception e ) {
											Message.printWarning( 2, routine, e );
										}
									}
									else if ( !__useOnlySnowTSandOperations ) {
										try {
											addNode( tsid_node, seg_node );
										}
										catch ( Exception e ) {
											Message.printWarning( 2, routine, e );
										}
									}
								}
								//check and indicate TS has no data 
								try {
									if(!dmi.checkTimeSeriesExists(op.getTimeSeries(tsg), true )) {
										tsid_node.setText( tsid_node.getText() + " - No Data" );
									}
								}
								catch (Exception e ) { 
									Message.printWarning( 2, routine, e); 
								}
							}//end if useAllTS
							else { 	//only add TS that have data
								try { //if you don't add this checkTimeSeriesExists to its
									//own try/catch statement, the entire loop thru the
									//system will end and the tree will be truncated
								if(dmi.checkTimeSeriesExists(op.getTimeSeries(tsg), true )) {
									if ( op_node != null ) {
										try {
											addNode( tsid_node, op_node );
										}
										catch ( Exception e ) {
											Message.printWarning( 2, routine, e );
										}
									}
									else {
										//add node to seg because op node null	
										if( ( __useOnlySnowTSandOperations ) &&
										( op_type.indexOf( "SNOW-17") >=0 ) &&
										( op.getTSDT(tsg).indexOf( "SWE") >= 0 )){
											try {
												addNode( tsid_node, seg_node );
											}
											catch ( Exception e ) {
												Message.printWarning( 2, routine, e );
											}
										}
										else if ( !__useOnlySnowTSandOperations ) {
											try {
												addNode( tsid_node, seg_node );
											}
											catch ( Exception e ) {
												Message.printWarning( 2, routine, e );
											}
										}
									}
								} //end check to see if TS has data
								}
								catch (Exception e ) { 
									Message.printWarning( 2, routine, e); 
								}
							}//end useAllTS=false
						}//end if __checkTS
						else { // __checkTS = false and 
							// Check TS for data when NodeExpanding listener called.
							// ALL time series are added if this flag is set to false

							// (If useAllTS is false, at NOdeExpandingtime, the node will be
							// removed from the JTree. IF useALLTS is true, the
							// node name will be appended with "NO Data")

							// Add all TS and on the NodeExpanding
							// Event will determine if there is data or not.
							if ( op_node != null ) {
								// op_node will be null if useOnlySnow... is true
								try {
									addNode( tsid_node, op_node );
								}
								catch ( Exception e ) {
									Message.printWarning( 2, routine, e );
								}
							}
							else {
								//add node to seg	
								if( ( __useOnlySnowTSandOperations ) &&
								( op_type.indexOf("SNOW-17") >=0 ) &&
								( op.getTSDT(tsg).indexOf( "SWE") >= 0 )) {
									try {
										addNode( tsid_node, seg_node );
									}
									catch ( Exception e ) {
										Message.printWarning( 2, routine, e );
									}
								}
								else if ( !__useOnlySnowTSandOperations ) {
									try {
										addNode( tsid_node, seg_node );
									}
									catch ( Exception e ) {
										Message.printWarning( 2, routine, e );
									}
								}
							}
						} //end !__checkTS
					} //end for tsg loop

					//RATING CURVES 
					if ( __include_ratingCurves ) {
						numb_rcs = op.getNumberOfRatingCurves();
						if ( Message.isDebugOn ) {	
							Message.printDebug( 5, routine,	"Number of rating curves= " + numb_rcs );
						}
						for (int rfg=0; rfg<numb_rcs; rfg++ ) {
							rc= op.getRatingCurve(rfg);
							//create a new node 
							if ( ! __verbose ) {
								rc_node = new SimpleJTree_Node( rc.getRCID() );
							}
							else {
								rc_node = new SimpleJTree_Node( "RC: " + rc.getRCID() );
							}
							rc_node.setData( rc );
						
							if ( __include_all_operations) {
								//add node
								try {
									addNode( rc_node, op_node );
								}
								catch ( Exception e ) {
									Message.printWarning( 2, routine, e );
								}
							}
							else {
								try {
									addNode( rc_node, seg_node );
								}
								catch ( Exception e ) {
									Message.printWarning( 2, routine, e );
								}
							}
						} //end for rfg
					}//end if include rating curves

				} //end for ofg
				opsw.stop();
//				Message.printStatus(1, "", "Time to create operation: "	+ opsw.getSeconds());
			} //end for sfg	
//			ssw.stop();
//			Message.printStatus(1, "", "Time to create segment: " + ssw.getSeconds());
		} //end for fgs
//		fgsw.stop();
//		Message.printStatus(1, "", "Time to create forecast groups: " + fgsw.getSeconds());
	} //end for cg

	}//end try
	catch( Exception e) {
		Message.printWarning( 2, routine, "Unable to create Sysmap tree." );
		Message.printWarning( 2, routine, e );
	}

	setFastAdd(false);
	setExpandAllowed( true );
	int s = nodes_to_expand_vect.size();
	for ( int i=0; i<s; i++ ) {
		//expand tree to this level
		expandNode( (SimpleJTree_Node)nodes_to_expand_vect.elementAt(i) );
	}

	//clean up
	nodes_to_expand_vect = null;
	cg = null;
	fg = null;
	seg = null;
	op = null;
	rc = null;

	setFastAdd( false );
	
	stopwatch.stop();
	Message.printStatus( 2, routine, "Created system tree in " + stopwatch.getSeconds() + " seconds." );
	stopwatch = null;
}//end displayTreeData()

/**
This method is used to get the strings needed for labelling all the GUI
components only if a translation table is used for the application, ie, if
there is a translation file.  If the String cannot be located in the
translation file, the original string will be utilized in non-translated form.
*/
public void initialize_gui_strings()
{
	LanguageTranslator translator = null; 
	translator = LanguageTranslator.getTranslator();
        if ( translator != null ) {
		//Popupmenu
		_popup_printCG_string = translator.translate("popup_printCG_string", _popup_printCG_string );
		_popup_printFG_string = translator.translate("popup_printFG_string", _popup_printFG_string );
		_popup_graphTS_string = translator.translate("popup_graphTS_string", _popup_graphTS_string );
		_popup_printSegs_string = translator.translate("popup_printSegs_string", _popup_printSegs_string );
		_popup_redefSegs_string = translator.translate("popup_redefSegs_string", _popup_redefSegs_string );
/*
		_popup_printStn_string = translator.translate(
			"popup_printStn_string", _popup_printStn_string );
		_popup_redefStn_string = translator.translate(
			"popup_redefStn_string", _popup_redefStn_string );
		_popup_addRatingCurve_string = translator.translate(
			"popup_addRatingCurve_string",
			_popup_addRatingCurve_string );
		_popup_deleteRatingCurve_string = translator.translate(
			"popup_deleteRatingCurve_string",
			_popup_deleteRatingCurve_string );
		_popup_redefRatingCurve_string = translator.translate(
			"popup_redefRatingCurve_string",
			_popup_redefRatingCurve_string );
		_popup_addStn_string = translator.translate(
			"popup_addStn_string", _popup_addStn_string );
		_popup_printRatingCurve_string = translator.translate(
			"popup_printRatingCurve_string",
			_popup_printRatingCurve_string );
		_popup_printMap_string = translator.translate(
			"popup_printMap_string",
			_popup_printMap_string );
		_popup_printFmap_string = translator.translate(
			"popup_printFmap_string",
			_popup_printFmap_string );
*/
	}
}//end initialize_gui_strings()  

/**
Reads in the provide Proplist and set property flags.
<table>
<tr>
<th>Property Name</th> <th>Property Description</th> <th>Default Value (if not defined)</th><th>Variable stored in</th>
</tr>
<tr>
<td>SystemJTree.checkTS</td>
<td>boolean indicates if all Time Series should be read in up front as
tree is being created and checked to see if they actually contain data.
If true, the time series does not have data, it is added to the JTree,
with a "no data" text flag added after the name to indicate that though
the time series is defined, it has no data. If set to false, the time 
series are not read in up front, but are read when the SimpleJTreeListener
detects an expansion event on an operation. When the operation node is
expanded, all the time series under the operation are read in at that time.</td>
<td>true</td>
<td>__checkTS</td>
</tr>

<tr>
<td>SystemJTree.forNWSRFSGUI</td>
<td>boolean to indicate if System JTree is for NWSRFSGUI</td>
<td>true</td>
<td>__forNWSRFSGUI</td>
</tr>

<tr>
<td>SystemJTree.forSnowUpdate</td>
<td>boolean to indicate if System JTree is for SnowUpdatingGUI</td>
<td>false</td>
<td>__forSnowUpdate</td>
</tr>

<tr>
<td>SystemJTree.include_all_operations</td>
<td>boolean indicating if Operations should be included in System Tree</td>
<td>true</td>
<td>__include_all_operations</td>
</tr>

<tr>
<td>SystemJTree.include_ratingCurves</td>
<td>boolean indicating if Rating Curves should be included in System Tree</td>
<td>false</td>
<td>__include_ratingCurves</td>
</tr>

<tr>
<td>SystemJTree.useAllTS</td>
<td>boolean indicating if all Time Series should be included in JTree 
whether they have data or not. If true, all time series are added 
to the JTree, whether they have data or not.  If false, only
time series that have data are added. This flag is only used
if checkTS = true.  (if checkTS = false, then all TS are added to the tree).
</td>
<td>true</td>
<td>__useAllTS</td>
</tr>

<tr>
<td>SystemJTree.useOnlySnowTSandOperations</td>
<td>boolean indicating that only time series of type SWE and
operation: SNOW-17 are displayed on the tree.  Used only if checkTS set to True. If this is 
true, __include_all_operations is set to False </td>
<td>false</td>
<td>__useOnlySnowTSandOperations</td>
</tr>

<tr>
<td>verbose</td>
<td>boolean to indicate if extra abbreviations should be used before 
each tree node's name to indicate data type of node ( for example: "seg:" 
for segment, "fg:" for forecast group, etc.)
</td>
<td>true</td>
<td>__verbose</td>
</tr>
</table> 
@param p PropList containing the properties.
*/
public void initialize_properties ( PropList p )
{
	String routine = "NWSRFS_System_JTree.initialize_properties";
	
	StopWatch stopwatch = new StopWatch();
	stopwatch.start();

	String s = null;
	//checkTS -default is True
	s = p.getValue( "SystemJTree.checkTS" );
	if ( s != null ) {
		if ( s.equalsIgnoreCase( "false" ) ) {
			__checkTS = false;
		}
	}
	if ( Message.isDebugOn ) {	
		Message.printDebug( 3, routine, "Property: checkTS = " + __checkTS );
	}

	//forNWSRFSGUI -default is True
	s = p.getValue( "SystemJTree.forNWSRFSGUI" );
	if ( s != null ) {
		if ( s.equalsIgnoreCase( "false" ) ) {
			__forNWSRFSGUI = false;
		}
	}
	if ( Message.isDebugOn ) {	
		Message.printDebug( 3, routine, "Property: forNWSRFSGUI = " + __forNWSRFSGUI );
	}

	//forSnowUpdate -default is False
	s = p.getValue( "SystemJTree.forSnowUpdate" );
	if ( s != null ) {
		if ( s.equalsIgnoreCase( "true" ) ) {
			__forSnowUpdate = true;
		}
	}
	if ( Message.isDebugOn ) {	
		Message.printDebug( 3, routine, "Property: forSnowUpdate = " + __forSnowUpdate );
	}

	//include_all_operations -default is True
	s = p.getValue( "SystemJTree.include_all_operations" );
	if ( s != null ) {
		if ( s.equalsIgnoreCase( "false" ) ) {
			__include_all_operations = false;
		}
		//set __useOnlySnowTSandOperations  to false if use_all_operations is true
		__useOnlySnowTSandOperations = false;
		
	}
	if ( Message.isDebugOn ) {	
		Message.printDebug( 3, routine,	"Property: useOnlySnowTSandOperations = " + __useOnlySnowTSandOperations );
	}

	// include_ratingCurves - default is False
	s = p.getValue( "SystemJTree.include_ratingCurves" );
	if ( s != null ) {
		if ( s.equalsIgnoreCase( "true" ) ) {
			__include_ratingCurves = true;
		}
	}
	if ( Message.isDebugOn ) {	
		Message.printDebug( 3, routine,	"Property: include_ratingCurves = " + __include_ratingCurves );
	}

	//useAllTS -default is True
	s = p.getValue( "SystemJTree.useAllTS" );
	if ( s != null ) {
		if ( s.equalsIgnoreCase( "false" ) ) {
			__useAllTS = false;
		}
	}
	if ( Message.isDebugOn ) {	
		Message.printDebug( 3, routine,	"Property: useAllTS = " + __useAllTS );
	}

	// useOnlySnowTSandOperations - default is False
	s = p.getValue( "SystemJTree.useOnlySnowTSandOperations" );
	if ( s != null ) {
		if ( s.equalsIgnoreCase( "true" ) ) {
			__useOnlySnowTSandOperations = true;
			__use_all_operations = false;
		}
	}
	if ( Message.isDebugOn ) {	
		Message.printDebug( 3, routine, "Property: use_all_operations = " + __use_all_operations );
	}

	//verbose - default is True
	s = p.getValue( "SystemJTree.verbose" );
	if ( s != null ) {
		if ( s.equalsIgnoreCase( "false" ) ) {
			__verbose = false;
		}
	}
	if ( Message.isDebugOn ) {	
		Message.printDebug( 3, routine,	"Property: verbose = " + __verbose );
	}

	// If running the NWSRFSGUI and are not reading in all the Time Series upfront
	// (aka, __checkTS = false, then have to have Operations added.  The
	// nodeExpanding event will then get triggered when the Operation node is
	// expanded and the time series are checked for data then.
	if( ( !__checkTS ) && ( __forNWSRFSGUI ) ) {
		Message.printWarning( 2, routine, "Adding operations to System Tree." );
		__include_all_operations = true;
	}

	// Likewise, for SNOW GUI, it is assumed that operations WILL NOT be added (since only
	// SNOW-17 operations are used).  So with _checkTS = false, the time series are
	// checked for data when a Segment node is expanded.
	if ( ( ! __checkTS ) && ( __forSnowUpdate ) ) {
		Message.printWarning( 2, routine, "No operations will be included in System Tree." );
		__include_all_operations = false;
	}

	stopwatch.stop();
	if ( Message.isDebugOn ) {
		Message.printDebug( 3, routine, "method to initialize properties, stopwatch seconds =" +
		stopwatch.getSeconds() );
	}

	stopwatch = null;
} //end initialize_properties

/**
Rebuilds the JTree.  With large NWSRFS data sets, this could be time-consuming.
*/
public void rebuild() {
	String routine = "NWSRFS_System_JTree.remake_JTree";

	clear();

	try {
		setTreeData(NWSRFS.createNWSRFSFromPRD(__fs5files, false));
	}
	catch (Exception e) {
		Message.printWarning( 2, routine, e);
	}

	displayTreeData();
	__parent.validate();
	__parent.repaint();
}

/**
Set the NWSRFS object 
@param nwsrfs NWSRFS data object which is used to populate the JTree.
*/
public void setTreeData ( NWSRFS nwsrfs )
{
	String routine = "NWSRFS_System_JTree.setTreeData";
	StopWatch stopwatch = new StopWatch();
	stopwatch.start();

	if ( Message.isDebugOn ) {	
		Message.printDebug( 5, routine, "NWSRFS_System_JTree.setTreeData  called." );
	}

	if ( nwsrfs == null ) {
		Message.printWarning( 2, routine, "NWSRFS object (nwsrfs) is null.  Cannot populate Tree!" );
	}

	__nwsrfs = nwsrfs;

	stopwatch.stop();
	if ( Message.isDebugOn ) {
		Message.printDebug( 2, routine,	"method to set Tree data, stopwatch seconds =" +
		stopwatch.getSeconds() );
	}
	stopwatch = null;

}//end setTreeData

/**
Checks to see if the mouse event would trigger display of the popup menu.
The popup menu does not display if it is null.
@param e the MouseEvent that happened.
*/
private void showPopupMenu (MouseEvent e) {
	//isPopupTrigger() not working on Linux, so use event.getModifiers() instead.
        if ( e.getModifiers() != 4 ) {
                return;
        }
	//popup to add depends on kind of element selected

	//selected tree node
	SimpleJTree_Node node = null;
	node = getSelectedNode();

	// First remove the menu items that are currently in the menu...
	__popup_JPopupMenu.removeAll();

	//see if we are on TOP node
	if ( node.getText().equalsIgnoreCase( __top_node.getName() ) ) {
		//no menus for top node at this point...
	}
	else {	
		Object data = null;	// Data object associated with the node

		// Now reset the popup menu based on the selected node...
		data = node.getData();
		if ( Message.isDebugOn ) {
			Message.printDebug( 2, "NWSRFS_SystemJTree.showPopupMenu",
			"Selected node text = \"" + node.getName() + "\" " +
			" and class = \"" + data.getClass().getName() + "\"");
		}

		if ( __forNWSRFSGUI ) {
			if( data instanceof NWSRFS_CarryoverGroup ) {
				__popup_JPopupMenu.add ( __popup_printCG_JMenuItem );
			}
			else if ( data instanceof NWSRFS_ForecastGroup ) {
				__popup_JPopupMenu.add ( __popup_printFG_JMenuItem );
			}
			else if ( data instanceof NWSRFS_Segment ) {
				__popup_JPopupMenu.add ( __popup_printSegs_JMenuItem );
				__popup_JPopupMenu.add ( __popup_redefSegs_JMenuItem );
			}
			else if ( data instanceof NWSRFS_TimeSeries ) {
				__popup_JPopupMenu.add ( __popup_graphTS_JMenuItem );
			}
		}
		else if ( __forSnowUpdate ) {
			//if ( ( data instanceof NWSRFS_ForecastGroup ) || ( data instanceof NWSRFS_Segment )  ) {}
			if ( ( data instanceof NWSRFS_ForecastGroup ) ||
			( data instanceof NWSRFS_CarryoverGroup ) || ( data instanceof NWSRFS_Segment )  ) {
				__popup_JPopupMenu.add ( __popup_snow_selectSubareas_JMenuItem );
			}
			else if ( data instanceof NWSRFS_TimeSeries ) {
				__popup_JPopupMenu.add ( __popup_graphTS_JMenuItem );
				__popup_JPopupMenu.add ( __popup_snow_selectSubareas_JMenuItem );
			}
		}
	}
	// Now display the popup so that the user can select the appropriate menu item...
	__popup_JPopupMenu.show(e.getComponent(), e.getX(), e.getY());

}//end showPopupMenu

/////////////     *** actions ***       ////////////
/**
Handle action events from the popup menu.
@param event ActionEvent to handle.
*/
public void actionPerformed(ActionEvent event ) {
	String routine = "NWSRFS_System_JTree.actionPerformed";
	Object source = event.getSource();

	String fs = IOUtil.getPropValue("FILE_SEPARATOR" );
	if( ( fs == null ) || ( fs.length() <= 0 ) ) {
		fs = File.separator;
	}
	String editor = IOUtil.getPropValue("EDITOR");
	//should not be null, but just in case...
	if( ( editor == null ) || ( editor.length() <= 0 ) ) {
		editor = "vi";
	}

	SimpleJTree_Node node = getSelectedNode();
	String output_str = null; 

	if ( source == __popup_printCG_JMenuItem ) {
		//get selected node - do not need the NWSRFS_CarryOverGroup object, just the node name!
		String name = node.getName();
		name = clean_node_name( name );
		
		//string  to hold output from ofs command (string will be null if command failed)

		//update the PUNCCG.GUI file to fill in correct carryover group.
		output_str  = NWSRFS_Util.run_print_cgs_or_fgs( name, fs, "PUNCHCG" ); 

		if ( output_str != null ) { 
			try { 
				NWSRFS_Util.runEditor( editor, output_str, false ); 
			} 
			catch ( Exception e ) { 
				Message.printWarning( 2, routine, e ); 
			}
		 }
	}
	else if ( source == __popup_printFG_JMenuItem ) {
		// Get selected node - do not need the NWSRFS_ForecastGroup object, just the node name!
		String name = node.getName();
		name = clean_node_name( name );

		// Update the PUNHFG.GUI file to fill in correct forecast group.
		output_str  = NWSRFS_Util.run_print_cgs_or_fgs( name, fs, "PUNCHFG" ); 

		if ( output_str != null ) { 
			try { 
				NWSRFS_Util.runEditor( editor, output_str, false ); 
			} 
			catch ( Exception e ) { 
				Message.printWarning( 2, routine, e ); 
			}
		 }
	}
	else if ( source == __popup_printSegs_JMenuItem ) {

		// Get selected node - do not need the NWSRFS_Segment object, just the node name!
		String name = node.getName();
		name = clean_node_name( name );

		// Update PRINTSEGS.GUI
		output_str  = NWSRFS_Util.run_print_segs( name );

		if ( output_str != null ) { 
			try { 
				NWSRFS_Util.runEditor( editor, output_str, false ); 
			} 
			catch ( Exception e ) { 
				Message.printWarning( 2, routine, e ); 
			}
		 }
	}
	else if ( source == __popup_redefSegs_JMenuItem ) {

		// Get selected node - do not need the NWSRFS_ForecastGroup object, just the node name!
		String name = node.getName();
		name = clean_node_name( name );

		// Dialog that does the Redefine Segments is part of System Maintenance class
		// (this class will take care of any String translations if needed)
		NWSRFS_SystemMaintenance system_maint = new NWSRFS_SystemMaintenance(this);
		system_maint.create_redefSegment_dialog(name);
	}
	else if (source == __popup_graphTS_JMenuItem) {
		// Get selected node - do not need the NWSRFS_Timeseries object, just the node name
		Vector nodes = getSelectedNodes();
		int size = nodes.size();

		SimpleJTree_Node tempNode = null;
		String name = null;
		Vector errors = new Vector();
		Vector tsidVector = new Vector();
		for (int i = 0; i < size; i++) {
			tempNode = (SimpleJTree_Node)nodes.elementAt(i);
			name = tempNode.getName();
			Message.printStatus(1, "", "Node: " + name);

			if (!name.startsWith("TS: ")) {
				continue;
			}

			if (tempNode.getText().indexOf("No Data") > 0) {
				errors.add(name);
			}	
			else {
				tsidVector.add(clean_node_name(name));
			}
		}

		size = errors.size();
		if (size > 0) {
			String concat = "";
			for (int i = 0; i < size; i++) {
				concat += "\n" + (String)errors.elementAt(i);
			}

			String verb = "were";
			if (size == 1) {	
				verb = "was";
			}
			
			Message.printWarning(1, routine, 
				"The following time series had no data and " + verb + " not plotted:" + concat);
		}

		if (tsidVector.size() == 0) {
			return;
		}

		// TODO SAM 2004-11-02 - This now plots the full
		// period available in the processed database (not just
		// the run start + 7 days.  If necessary, put in the
		// more limiting period (e.g., add popup menu
		// "Graph Selected Time Series (Run Period)" and change existing
		// menu to "Graph Selected Time Series (All Available Data)",
		// but for now go with the simpler default.

		try {	
			NWSRFS_Util.plotTimeSeries(__nwsrfs.getDMI(), tsidVector, null, null);
		}
		catch (Exception e) {
			Message.printWarning(1, routine, "There is an error plotting the time series.");
			Message.printWarning(2, routine, e);
		}
	}

	else if ( source == __popup_snow_selectSubareas_JMenuItem ) {
		//select all nodes under this
		
		Vector sel_nodes_vect = getSelectedNodes(); 
		int numb_nodes = 0;
		if ( sel_nodes_vect != null ) {
			numb_nodes = sel_nodes_vect.size();
		}
		SimpleJTree_Node sel_node = null;
		//Object data = null;
		for ( int i=0; i< numb_nodes; i++ ) {
			sel_node = (SimpleJTree_Node)sel_nodes_vect.elementAt( i );
			if ( sel_node == null ) {
				continue;
			}
			Vector kids_vect = null;
			kids_vect = getAllChildrenVector( sel_node );
			int numb_kids = 0;
			
			if ( kids_vect != null ) {
				numb_kids = kids_vect.size();
			}
			for( int j=0; j<numb_kids; j++) {
				//select kids
				selectNode( (SimpleJTree_Node)kids_vect.elementAt( j ), false );
			}
			kids_vect = null;	
			sel_node = null;
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
Responds to Tree Expanding events
*/
public void nodeExpanding( SimpleJTree_Node node ) {

	String routine = "NWSRFS_System_JTree.nodeExpanding";

	Object data = null;	// Data object associated with the node
	data = node.getData();
	
	//check Time series now to see if they have data.  All time
	//series have been added to tree, but they have not been
	//"read in" at this point, so may or may not contain data.
	if( ( __forNWSRFSGUI ) && ( data instanceof NWSRFS_Operation ) ) {
		
		NWSRFS_Operation op = (NWSRFS_Operation) data;
		SimpleJTree_Node tempNode = null;

		//get Operations children nodes 
		Object[] arr = getChildrenArray(node);
		NWSRFS_DMI dmi = __nwsrfs.getDMI();
		for ( int i=0; i<arr.length; i++ ) {
				
			tempNode = ( SimpleJTree_Node) arr[i];
			try {
				if(!dmi.checkTimeSeriesExists(op.getTimeSeries(i), true )) {
					//set Text for time series node
					tempNode.setText( tempNode.getText() + " - No Data" );
		
				}
			}
			catch ( Exception e ) {
				Message.printWarning( 2, routine, e);
			}
		}
	}
	else if ( ( __forSnowUpdate) && ( data instanceof NWSRFS_Segment ) ) {
		SimpleJTree_Node tempNode = null;

		//get Segment children nodes which will be NWSRFS_TimeSeries
		Object[] arr = getChildrenArray(node);
		NWSRFS_DMI dmi = __nwsrfs.getDMI();
		for ( int i=0; i<arr.length; i++ ) {
					
			tempNode = ( SimpleJTree_Node) arr[i];
			if ( tempNode == null ) {		
				continue;
			}
			NWSRFS_TimeSeries ts = (NWSRFS_TimeSeries)tempNode.getData();
			try {
				if(!dmi.checkTimeSeriesExists( ts, true ) ) {
					//remove node
					if (  !__useAllTS ) {
						removeNode( tempNode );
					}
					else {
						//set Text for time series node
						tempNode.setText( tempNode.getText() + " - No Data" );
					}
				}
			}
			catch ( Exception e ) {
				Message.printWarning( 2, routine, e);
			}
		}
	}	
}

} // end NWSRFS_System_JTree
