//-----------------------------------------------------------------------------
// NWSRFS_MAP_JPanel - an object to display a simple list of map areas
// in a JTree format.
//-----------------------------------------------------------------------------
// History:
//
// 2004-11-01	J. Thomas Sapienza, RTi	Initial version based on 
//					NWSRFS_MAP_JList.
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

import java.io.File;

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

/**
The NWSRFS_MAP_JPanel class displays a list of the NWSRFS stations in a JTree.
*/
public class NWSRFS_MAP_JPanel extends JPanel implements ActionListener, MouseListener
{

/**
The font used in the system JTree -- set so that all the other panels in the
tabbed panel use the same font.
*/
private Font __treeFont = null;

/**
The parent JFrame.
*/
private JFrame __parent = null;

/**
The popup menu.
*/
private JPopupMenu __popup = null;

/**
The worksheet in which MAP areas are displayed.
*/
private JWorksheet __worksheet = null;

/**
NWSRFS instance.
*/
private NWSRFS __nwsrfs = null;

/**
The worksheet cell renderer.
*/
private NWSRFS_MAP_CellRenderer __cellRenderer = null;

/**
The worksheet table model.
*/
private NWSRFS_MAP_TableModel __tableModel = null;

/**
Popup menu menu items.
*/
private SimpleJMenuItem 
	__popup_printFMAP_JMenuItem = null,
	__popup_printMAP_JMenuItem = null;
	//__popup_printMAT_JMenuItem = null;

/**
Popup menu strings.
*/
protected String 
	_popup_printFMAP_string = 	"View Current FMAP Definition",
	_popup_printMAP_string = 	"View Current MAP Definition",
	_popup_printMAT_string = 	"View Current MAT Definition";

/**
FS5files used.
*/
private String __fs5files = null;

/**
Constructor for NWSRFS_MAP_JPanel to display Contract information.
@param parent JFrame parent Calling parent class.  
@param nwsrfs NWSRFS instance containing all the data for the list.
@param fs5files  String name of fs5files used.
@param treeFont the font used in the system tree in the gui.  Used so that
all the panels of the tabbed panel share the same font.
*/
public NWSRFS_MAP_JPanel(JFrame parent, NWSRFS nwsrfs, String fs5files, Font treeFont) {
	__nwsrfs = nwsrfs;
	__parent = parent; 	
	__fs5files = fs5files;
	__treeFont = treeFont;

	// translate GUI strings
	initializeGUIStrings();

	createPopupMenu();

	setupPanel();

	__worksheet.addMouseListener(this);
}

/**
Handle action events from the popup menu.
@param event ActionEvent to handle.
*/
public void actionPerformed(ActionEvent event ) {
	String routine = "NWSRFS_MAP_JPanel.actionPerformed";
	Object source = event.getSource();

	String fs = File.separator;
	
	String editor = IOUtil.getPropValue("EDITOR");
	if ((editor == null) || (editor.length() <= 0)) {
		editor = "vi";
	}

	if (__worksheet.getSelectedRow() < 0) {
		return;
	}

	if (source == __popup_printMAP_JMenuItem) {

		NWSRFS_MAP map = (NWSRFS_MAP)__worksheet.getRowData(__worksheet.getSelectedRow());
			
		String outputString  = NWSRFS_Util.run_dump_station_or_area(map.getID(), fs, "DUMPMAP");
			
		if (outputString != null) { 
			try { 
				NWSRFS_Util.runEditor(editor, outputString,	false);
			} 
			catch (Exception e) { 
				Message.printWarning(2, routine, e); 
			}
		}
	}
	else if (source == __popup_printFMAP_JMenuItem) {
		NWSRFS_MAP map = (NWSRFS_MAP)__worksheet.getRowData(__worksheet.getSelectedRow());

		String outputString  = NWSRFS_Util.run_dump_station_or_area(map.getMAPFMAPID(), fs, "DUMPFMAP");
			
		if (outputString != null) { 
			try { 
				NWSRFS_Util.runEditor(editor, outputString,	false);
			} 
			catch (Exception e) { 
				Message.printWarning(2, routine, e); 
			}
		}
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
	__popup = new JPopupMenu();

	//popup menu items
	__popup_printMAP_JMenuItem = new SimpleJMenuItem(_popup_printMAP_string, this );

	__popup_printFMAP_JMenuItem = new SimpleJMenuItem(_popup_printFMAP_string, this );

	//__popup_printMAT_JMenuItem = new SimpleJMenuItem(_popup_printMAT_string, this );

	__popup.add(__popup_printMAP_JMenuItem);
	__popup.add(__popup_printFMAP_JMenuItem);
//	__popup.add(__popup_printMAT_JMenuItem);
}

/**
This method is used to get the strings needed for labelling all the GUI
components only if a translation table is used for the application, ie, if
there is a translation file.  If the String cannot be located in the
translation file, the original string will be utilized in non-translated form.
*/
public void initializeGUIStrings()
{
	LanguageTranslator translator = null; 
	translator = LanguageTranslator.getTranslator();
        if (translator != null) {
		//Popupmenu
		_popup_printMAP_string = translator.translate(
			"popup_printMAP_string", _popup_printMAP_string);
		_popup_printFMAP_string = translator.translate(
			"popup_printFMAP_string", _popup_printFMAP_string);
		_popup_printMAT_string = translator.translate(
			"popup_printMAT_string", _popup_printMAT_string);
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
public void mousePressed(MouseEvent event) {
}

/**
Responds to mouse released events.
@param event the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent event) {
	if (__popup != null && __popup.isPopupTrigger(event)) {
		__popup.show(event.getComponent(), event.getX(), event.getY());
	}
}

/**
Reads the MAP Areas from the database and returns them in a Vector.
@return a Vector of MAP Areas.  This Vector will never be null.
*/
private Vector readMAPAreas() {
	String routine = "NWSRFS_MAP_JPanel.readMAPAreas";

	NWSRFS_DMI dmi = __nwsrfs.getDMI();

	Vector maps = null;

	try {
		maps = dmi.readMAPAreaList();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		return new Vector();
	}

	if (maps == null) {
		return new Vector();
	}

	NWSRFS_MAP map = null;
	Vector v = new Vector();
	for (int i = 0; i < maps.size(); i++) {
		map = (NWSRFS_MAP)maps.elementAt(i);
		try {
			dmi.readMAPArea(map, false);
		}
		catch (Exception e) {
			Message.printWarning(2, routine, "Skipping object: " + map.getID());
			Message.printWarning(2, routine, e);
			continue;
		}		
		v.add(map);
	}
	return v;
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

	__worksheet.setData(readMAPAreas());

	__parent.validate();
	__parent.repaint();
}

/**
Sets the widths of the columns in the table.
*/
public void setColumnWidths() {
	__worksheet.setColumnWidths(__cellRenderer.getColumnWidths());
}

/**
Display all the information in the NWSRFS data set.
*/
public void setupPanel() {
	__tableModel = new NWSRFS_MAP_TableModel(readMAPAreas());
	__cellRenderer = new NWSRFS_MAP_CellRenderer(__tableModel);

	PropList props = new PropList("JWorksheet");
	props.set("JWorksheet.CellFontName=" + __treeFont.getFontName());
	props.set("JWorksheet.CellFontSize=" + __treeFont.getSize());
	props.set("JWorksheet.SelectionMode=SingleRowSelection");
	//props.set("JWorksheet.ShowRowHeader=true");

	JScrollWorksheet jsw = new JScrollWorksheet(__cellRenderer,	__tableModel, props);
	__worksheet = jsw.getJWorksheet();	

	setLayout(new GridBagLayout());
	JGUIUtil.addComponent(this, jsw,
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);
}

}
