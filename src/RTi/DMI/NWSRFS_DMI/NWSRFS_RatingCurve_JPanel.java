//-----------------------------------------------------------------------------
// NWSRFS_RatingCurve_JPanel - an object to display a simple list of rating
// curves in a JTree format.
//-----------------------------------------------------------------------------
// History:
//
// 2004-11-01	J. Thomas Sapienza, RTi	Initial version based on 
//					NWSRFS_RatingCurve_JList.
// 2004-11-16	JTS, RTi		Added setSystemJTree() so that
//					a system tree can be rebuilt when
//					rating curves are added, deleted, or
//					redefined.
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

import java.util.List;
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
The NWSRFS_RatingCurve_JPanel class displays a list of the NWSRFS stations in a JTree.
*/
@SuppressWarnings("serial")
public class NWSRFS_RatingCurve_JPanel extends JPanel
implements ActionListener, MouseListener {

/**
NWSRFS instance.
*/
private NWSRFS __nwsrfs;

/**
FS5files used.
*/
private String __fs5files;

//JFrame parent
private JFrame __parent;

//pop menu items 
// Define STRINGs here used for menus in case we need
// to translate the strings.
protected String _popup_printStn_string = "View Current Station Definition";
protected String _popup_addStn_string = "Add Station";
protected String _popup_redefStn_string = "Redefine Station";

//used to describe station type.
protected String _type_string = "type";

private Font __listFont = null;

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

private JWorksheet __worksheet = null;

private NWSRFS_RatingCurve_TableModel __tableModel = null;
private NWSRFS_RatingCurve_CellRenderer __cellRenderer = null;

private NWSRFS_System_JTree __systemJTree = null;

/**
Constructor for NWSRFS_RatingCurve_JPanel to display Contract information.
@param parent JFrame parent Calling parent class.  
@param nwsrfs NWSRFS instance containing all the data for the list.
@param fs5files  String name of fs5files used.
@param canEdit Boolean indicating if the list nodes can be edited. 
*/
public NWSRFS_RatingCurve_JPanel ( JFrame parent, NWSRFS nwsrfs, String fs5files, Font listFont)
{
	__nwsrfs = nwsrfs;
	__parent = parent; 	
	__fs5files = fs5files;
	__listFont = listFont;

	//initialize GUI strings (translate if needed)
	initializeGUIStrings();

	//create popup menu and menu items
	createPopupMenu();

	//populate tree
	setupPanel();

	__worksheet.addMouseListener(this);
}

/**
Handle action events from the popup menu.
@param event ActionEvent to handle.
*/
public void actionPerformed(ActionEvent event ) {
	String routine = "NWSRFS_RatingCurve_JList.actionPerformed";
	Object source = event.getSource();

	String fs = IOUtil.getPropValue("FILE_SEPARATOR");
	if ((fs == null) || (fs.length() <= 0)) {
		fs = "/";
	}
	
	String editor = IOUtil.getPropValue("EDITOR");
	//should not be null, but just in case...
	if ((editor == null) || (editor.length() <= 0)) {
		editor = "vi";
	}

	if (__worksheet.getSelectedRow() < 0) {
		return;
	}

	NWSRFS_RatingCurve rc = (NWSRFS_RatingCurve)__worksheet.getRowData(
		__worksheet.getSelectedRow());

	String id = rc.getRCID();

	if (source == __popup_printRatingCurve_JMenuItem) {
		String outputString = NWSRFS_Util.run_print_ratingCurves(id);

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
	else if (source == __popup_addRatingCurve_JMenuItem) {
		NWSRFS_SystemMaintenance system_maint = 
			new NWSRFS_SystemMaintenance(__systemJTree);
		system_maint.create_addRatingCurve_dialog();

		refillData();
	}
	else if (source == __popup_deleteRatingCurve_JMenuItem) {
		List<String> v = NWSRFS_Util.run_delete_ratingCurve(id);
		if (v != null) {
			refillData();
		}
		else  { 
			Message.printWarning(2, routine,
				"Rating curve: \"" + id + "\" not deleted.");
		}
		if (__systemJTree != null) {
			__systemJTree.rebuild();
		}
	}
	else if (source == __popup_redefRatingCurve_JMenuItem) {
		// dialog that does the Redefine RatingCurves is part of
		// System Maintenance class (this class will take
		// care of any String translations if needed)
		NWSRFS_SystemMaintenance system_maint = 
			new NWSRFS_SystemMaintenance(__systemJTree);
		system_maint.create_redefRatingCurves_dialog(id);

		refillData();
	}
}

/**
Clear all data from the tree.
*/
public void clear() {
	__worksheet.clear();
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
	__popup_printRatingCurve_JMenuItem = new SimpleJMenuItem(
		_popup_printRatingCurve_string, this);

	__popup_addRatingCurve_JMenuItem = new SimpleJMenuItem(
		_popup_addRatingCurve_string, this);

	__popup_deleteRatingCurve_JMenuItem = new SimpleJMenuItem(
		_popup_deleteRatingCurve_string, this);

	__popup_redefRatingCurve_JMenuItem = new SimpleJMenuItem(
		_popup_redefRatingCurve_string, this);

	__popup_JPopupMenu.add(__popup_addRatingCurve_JMenuItem);
	__popup_JPopupMenu.addSeparator();
	__popup_JPopupMenu.add(__popup_printRatingCurve_JMenuItem);
	__popup_JPopupMenu.add(__popup_deleteRatingCurve_JMenuItem);
	__popup_JPopupMenu.add(__popup_redefRatingCurve_JMenuItem);
}

/**
Returns a Vector of all the rating curves in the database.
@return a Vector of all the rating curves in the database.  Guaranteed to return
a non-null Vector.
*/
public List<NWSRFS_RatingCurve> getRatingCurves() {
	String routine = "NWSRFS_RatingCurve_JPanel.getRatingCurves";

	//make vector of rating curve IDs
	List<String> rc_vect = null;

	NWSRFS_DMI dmi = __nwsrfs.getDMI();
	try {
		rc_vect = StringUtil.sortStringList(dmi.readRatingCurveList());
	}
	catch (Exception e) {
		Message.printWarning(2, routine, 
			"Unable to read list of rating curves to create " 
			+ "rating curve list. Please refer to log file for "
			+ "more details.");

		Message.printWarning(2, routine, e);

		return new Vector<NWSRFS_RatingCurve>();
	}

	int numb_rcs = 0;
	if (rc_vect != null) {	
		numb_rcs = rc_vect.size();
	}
	else {
		return new Vector<NWSRFS_RatingCurve>();
	}

	NWSRFS_RatingCurve rc = null;
	String rcid = null;
	List<NWSRFS_RatingCurve> v = new Vector<NWSRFS_RatingCurve>();
	for (int i = 0; i < numb_rcs; i++ ) {
		rcid = rc_vect.get(i);
		try {
			rc = dmi.readRatingCurve(rcid);
		}
		catch (Exception e) {
			Message.printWarning(2, routine, e);
			Message.printWarning(2, routine, 
				"Skipping object: " + rcid);
			continue;
		}
		v.add(rc);
	}
	return v;
}

/**
This method is used to get the strings needed for labelling all the GUI
components only if a translation table is used for the application, ie, if
there is a translation file.  If the String cannot be located in the
translation file, the original string will be utilized in non-translated form.
*/
public void initializeGUIStrings()  {
	LanguageTranslator translator = null; 
	translator = LanguageTranslator.getTranslator();
        if (translator != null) {
		//Popupmenu
		_popup_printRatingCurve_string = translator.translate(
			"popup_printRatingCurve_string",
			_popup_printRatingCurve_string);
		_popup_addRatingCurve_string = translator.translate(
			"popup_addRatingCurve_string",
			_popup_addRatingCurve_string);
		_popup_deleteRatingCurve_string = translator.translate(
			"popup_deleteRatingCurve_string",
			_popup_deleteRatingCurve_string);
		_popup_redefRatingCurve_string = translator.translate(
			"popup_redefRatingCurve_string",
			_popup_redefRatingCurve_string);
	}
}

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

	__worksheet.setData(getRatingCurves());

	__parent.validate();
	__parent.repaint();
}

/**
Sets the column widths in the worksheet to fit what is defined in the table
model.
*/
public void setColumnWidths() {
	__worksheet.setColumnWidths(__cellRenderer.getColumnWidths());
}

/**
Sets the system tree that should be refreshed when a rating curve is
redefined.
*/
public void setSystemJTree(NWSRFS_System_JTree systemJTree) {
	__systemJTree = systemJTree;
}

/**
Display all the information in the NWSRFS data set.
*/
public void setupPanel() {
	__tableModel = new NWSRFS_RatingCurve_TableModel(getRatingCurves());
	__cellRenderer = new NWSRFS_RatingCurve_CellRenderer(__tableModel);

	PropList props = new PropList("JWorksheet");
	props.set("JWorksheet.CellFontName=" + __listFont.getFontName());
	props.set("JWorksheet.CellFontSize=" + __listFont.getSize());
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
