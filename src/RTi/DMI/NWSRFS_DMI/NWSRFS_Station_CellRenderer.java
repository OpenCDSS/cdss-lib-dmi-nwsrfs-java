// ----------------------------------------------------------------------------
// NWSRFS_Station_CellRenderer - class for rendering cells in the station 
// 	table in the NWSRFS GUI.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2004-11-01	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.DMI.NWSRFS_DMI;

import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;

import RTi.Util.String.StringUtil;

/**
This class is used for rendering cells for the station table in the GUI.
*/
public class NWSRFS_Station_CellRenderer
extends JWorksheet_AbstractExcelCellRenderer {

/**
Table model for which this class renders the cell.
*/
private NWSRFS_Station_TableModel __tableModel;

/**
Constructor.  Does nothing apart from throwing an exception because this
object MUST be created with a table model.
*/
public NWSRFS_Station_CellRenderer()
throws Exception {
	throw new Exception ("Do not use this constructor.");
}

/**
Constructor.  
@param model the model for which this class will render cells
*/
public NWSRFS_Station_CellRenderer(NWSRFS_Station_TableModel model) {
	__tableModel = model;
	setRenderBooleanAsCheckBox(true);
}

/**
Returns the format for a given column.
@param column the colum for which to return the format.
@return the format (as used by StringUtil.format) for a column.
*/
public String getFormat(int column) {
	return __tableModel.getFormat(column);
}

/**
Returns the widths of the columns in the table.
@return an integer array of the widths of the columns in the table.
*/
public int[] getColumnWidths() {
	return __tableModel.getColumnWidths();
}

}
