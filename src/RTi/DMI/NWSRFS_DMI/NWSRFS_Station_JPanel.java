//-----------------------------------------------------------------------------
// NWSRFS_Station_JPanel - an object to display a simple list of stations
//-----------------------------------------------------------------------------
// History:
//
// 2004-11-01	J. Thomas Sapienza, RTi	Initial version based on 
//					NWSRFS_Station_JList.
// REVISIT:
// TO DO:
// Add station name description next to identifier.
// Once name added, will need to verify that the update cleanListItemName() 
// method to operates as expected and returns just the station ID
//-----------------------------------------------------------------------------

package RTi.DMI.NWSRFS_DMI;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import RTi.DMI.NWSRFS_DMI.NWSRFS_Util;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.SimpleJMenuItem;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.LanguageTranslator;
import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
The NWSRFS_Station_JPanel class displays a list of the NWSRFS stations in a JTree.
*/
public class NWSRFS_Station_JPanel extends JPanel
implements ActionListener, MouseListener {

/**
The font used in the system tree in the main GUI, so that all panels in the
tabbed panel share the same font.
*/
private Font __treeFont = null;

/**
Parent JFrame.
*/
private JFrame __parent = null;

/**
The popup menu that appears when a station is right-clicked on.
*/
private JPopupMenu __stationListPopup = null;

/**
The worksheet in which the list of stations is displayed.
*/
private JWorksheet __worksheet = null;

/**
NWSRFS instance.
*/
private NWSRFS __nwsrfs = null;

/**
The cell renderer used in the worksheet.
*/
private NWSRFS_Station_CellRenderer __cellRenderer = null;

/**
The table model used in the worksheet.
*/
private NWSRFS_Station_TableModel __tableModel = null;

/**
Popup menu menu items.
*/
private SimpleJMenuItem 
	__printStationMenuItem = null,
	__addStationMenuItem = null,
	__redefineStationMenuItem = null;

/**
Popup menu strings.
*/
protected String 
	_popup_addStn_string = 		"Add Station",
	_popup_center_station_on_map = 	"Center Station on Map",
	_popup_printStn_string = 	"View Current Station Definition",
	_popup_redefStn_string = 	"Redefine Station";

/**
FS5files used.
*/
private String __fs5files = null;

/**
Constructor for NWSRFS_Station_JPanel to display Contract information.
@param parent JFrame parent Calling parent class.  
@param nwsrfs NWSRFS instance containing all the data for the list.
@param fs5files  String name of fs5files used.
@param treeFont the font used in the system tree in the GUI, so that all panels
in the tabbed panel share the same font.
*/
public NWSRFS_Station_JPanel(JFrame parent, NWSRFS nwsrfs, String fs5files,
Font treeFont) {				
	__nwsrfs = nwsrfs;
	__parent = parent; 	
	__fs5files = fs5files;
	__treeFont = treeFont;

	// translate the strings if necessary
	initializaGUIStrings();

	// create popup menu and menu items
	createPopupMenu();

	// create the worksheet and fill it with data
	setupPanel();

	__worksheet.addMouseListener(this);
}

/**
Handle action events from the popup menu.
@param event ActionEvent to handle.
*/
public void actionPerformed(ActionEvent event ) {
	String routine = "NWSRFS_Station_JPanel.actionPerformed";
	String command = event.getActionCommand();

	if (command.equals(_popup_printStn_string)) {
		if (__worksheet.getSelectedRow() < 0) {
			return;
		}

		// REVISIT (JTS - 2004-11-12)
		// why not File.separator?
		String fs = IOUtil.getPropValue("FILE_SEPARATOR");
		if ((fs == null) || (fs.length() <= 0)) {
			fs = "/";
		}
		
		String editor = IOUtil.getPropValue("EDITOR");
		if ((editor == null) || (editor.length() <= 0)) {
			editor = "vi";
		}	
		
		//get selected node 
		NWSRFS_Station station = (NWSRFS_Station)__worksheet.getRowData(
			__worksheet.getSelectedRow());
		
		String outputString = NWSRFS_Util.run_dump_station_or_area( 
			station.getID(), fs, "DUMPSTN");

		// if outputString is null, the command failed

		if (outputString != null) { 
			try { 
				NWSRFS_Util.runEditor(editor, outputString,
					false);
			} 
			catch (Exception e) { 
				Message.printWarning(2, routine, e); 
			}
		 }
	}
	else if (command.equals(_popup_addStn_string)) {
		NWSRFS_SystemMaintenance system_maint 
			= new NWSRFS_SystemMaintenance();
		system_maint.create_addStations_dialog();
		refillData();
	}
	else if (command.equals(_popup_redefStn_string)) {
		NWSRFS_Station station = (NWSRFS_Station)__worksheet.getRowData(
			__worksheet.getSelectedRow());
	
		// dialog that does the Redefine RatingCurves is part of
		// System Maintenance class (this class will take
		// care of any String translations if needed)
		NWSRFS_SystemMaintenance system_maint 
			= new NWSRFS_SystemMaintenance();
		system_maint.create_redefStations_dialog(station.getID());

		refillData();
	}
	else if (command.equals(_popup_center_station_on_map)) {
		/*
		NWSRFS_Station station = (NWSRFS_Station)__worksheet.getRowData(
			__worksheet.getSelectedRow());
		__parent.centerStationOnMap(station.getID());
		*/
	}
}

/**
Clear all data from the tree.
*/
public void clear() {
	__worksheet.clear();
}

/**
Creates the JPopupMenu and SimpleJMenuItems for the PopupMenu.  
*/
private void createPopupMenu() {
	__stationListPopup = new JPopupMenu();

	__printStationMenuItem = new SimpleJMenuItem(
		_popup_printStn_string, this);

	__addStationMenuItem = new SimpleJMenuItem(_popup_addStn_string, this);

	__redefineStationMenuItem = new SimpleJMenuItem(
		_popup_redefStn_string, this);

	__stationListPopup.add(__printStationMenuItem);
	__stationListPopup.add(__redefineStationMenuItem);
	__stationListPopup.addSeparator();
	__stationListPopup.add(__addStationMenuItem);
}

/**
Reads the data for all the stations from the database and returns a Vector
of station objects.
@return a Vector station objects.  This Vector will never be null.
*/
private Vector readStations() {
	String routine = "NWSRFS_Main_JFrame.createStationList()";

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

	Message.printStatus(1, "", "The tree will be built for " + size + " stations.");
	
	Vector data = new Vector();

	for (int i = 0; i < size; i++) {
		stationID = (String)sortedStationIDs.elementAt(i);

		station = (NWSRFS_Station) hash.get(stationID);
		if (station == null) {	
			continue;
		}

		try {
			dmi.readStation(station, false);
		}
		catch (Exception e) {
			Message.printWarning(2, routine, e);
			continue;
		}

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

		data.add(station);
	}

	return data;
}

/**
This method is used to get the strings needed for labelling all the GUI
components only if a translation table is used for the application, ie, if
there is a translation file.  If the String cannot be located in the
translation file, the original string will be utilized in non-translated form.
*/
public void initializaGUIStrings()  {
	LanguageTranslator translator = null; 
	translator = LanguageTranslator.getTranslator();

        if (translator != null) {
		_popup_printStn_string = translator.translate(
			"popup_printStn_string", _popup_printStn_string );
		_popup_addStn_string = translator.translate(
			"popup_addStn_string", _popup_addStn_string );
		_popup_redefStn_string = translator.translate(
			"popup_redefStn_string", _popup_redefStn_string );
		_popup_center_station_on_map = translator.translate(
			"popup_center_station_on_map", 
			_popup_center_station_on_map);
	}
}

/**
Does nothing.
*/
public void mouseClicked(MouseEvent event) {}

/**
Does nothing.
*/
public void mouseDragged(MouseEvent event) {}

/**
Does nothing.
*/
public void mouseEntered(MouseEvent event) {}

/**
Does nothing.
*/
public void mouseExited(MouseEvent event) {}

/**
Does nothing.
*/
public void mouseMoved(MouseEvent event) {}

/**
Does nothing.
*/
public void mousePressed(MouseEvent event) {}

/**
Responds to mouse released events and shows a popup menu.
@param event the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent event) {
	if (__stationListPopup != null 
		&& __stationListPopup.isPopupTrigger(event)) {
		__stationListPopup.show(event.getComponent(), 
			event.getX(), event.getY());
	}
}

/**
Refills the worksheet with data from the database.
*/
public void refillData() {
	String routine = "NWSRFS_Main_JFrame.refillData";

	try {
		NWSRFS nwsrfs = NWSRFS.createNWSRFSFromPRD(__fs5files, false);
		if (nwsrfs != null) {
			__nwsrfs = nwsrfs;
		}
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
	}

	__worksheet.setData(readStations());

	__parent.validate();
	__parent.repaint();
}

/**
Sets the column widths for the table.
*/
public void setColumnWidths() {
	__worksheet.setColumnWidths(__cellRenderer.getColumnWidths());
}

/**
Display all the information in the NWSRFS data set.
*/
public void setupPanel() {
	__tableModel = new NWSRFS_Station_TableModel(readStations());
	__cellRenderer = new NWSRFS_Station_CellRenderer(__tableModel);

	PropList props = new PropList("JWorksheet");
	props.set("JWorksheet.CellFontName=" + __treeFont.getFontName());
	props.set("JWorksheet.CellFontSize=" + __treeFont.getSize());
	props.set("JWorksheet.SelectionMode=SingleRowSelection");
	//props.set("JWorksheet.ShowRowHeader=true");

	JScrollWorksheet jsw = new JScrollWorksheet(__cellRenderer,
		__tableModel, props);
	__worksheet = jsw.getJWorksheet();	

	setLayout(new GridBagLayout());
	JGUIUtil.addComponent(this, jsw,
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);
}

}
