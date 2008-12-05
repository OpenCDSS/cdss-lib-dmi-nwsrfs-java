// ----------------------------------------------------------------------------
// NWSRFS_MAP_TableModel - table model for a list of NWSRFS MAP areas.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2004-11-01	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.DMI.NWSRFS_DMI;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This class is a table model for displaying MAP areas in the NWSRFS GUI.
*/
public class NWSRFS_MAP_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
Number of columns in the table model.
*/
private final static int __COLUMNS = 2;

/**
References to the columns.
*/
public static final int 
	COL_MAP = 0,
	COL_FMAP = 1;

/**
Constructor.  
@param maps the map areas that will be displayed in the table.
*/
public NWSRFS_MAP_TableModel(List maps) {
	if (maps == null) {
		_data = new Vector();
	}
	else {
		_data = maps;
	}
	
	_rows = maps.size();
}

/**
Returns the class of the data stored in a given column.  
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_MAP:	return String.class;
		case COL_FMAP:	return String.class;
		default:	return String.class;
	}
}

/**
Returns the number of columns of data.
@return the number of columns of data.
*/
public int getColumnCount() {
	return __COLUMNS;
}

/**
Returns the name of the column at the given position.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	switch (columnIndex) {
		case COL_MAP:	return "MAP";
		case COL_FMAP:	return "FMAP";
		default:	return " ";
	}	
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the
column.
*/
public String getFormat(int column) {
	switch (column) {
		case COL_MAP:	return "%-20s";
		case COL_FMAP:	return "%-20s";
		default:	return "%-8s";
	}
}

/**
From AbstractTableMode.  Returns the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
From AbstractTableMode.  Returns the data that should be placed in the JTable at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	NWSRFS_MAP map = (NWSRFS_MAP)_data.get(row);

	switch (col) {
		case COL_MAP:	return map.getID();
		case COL_FMAP:	return map.getMAPFMAPID();
		default:	return "";
	}
}

/**
Returns an array containing the widths (in number of characters) that the 
fields in the table should be sized to.
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
	int[] widths = new int[__COLUMNS];
	for (int i = 0; i < __COLUMNS; i++) {
		widths[i] = 0;
	}

	widths[COL_MAP] = 	10;
	widths[COL_FMAP] =	10;
	
	return widths;
}

}
