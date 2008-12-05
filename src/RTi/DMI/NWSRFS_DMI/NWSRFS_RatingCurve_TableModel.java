// ----------------------------------------------------------------------------
// NWSRFS_RatingCurve_TableModel - table model for a list of NWSRFS rating 
//	curves
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
This class is a table model for displaying rating curves in the NWSRFS GUI.
*/
public class NWSRFS_RatingCurve_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
Number of columns in the table model.
*/
private final static int __COLUMNS = 1;

/**
References to the columns.
*/
public static final int 
	COL_ID = 0;

/**
Constructor.  
@param rcs the rating curves that will be displayed in the table.
*/
public NWSRFS_RatingCurve_TableModel(List rcs) {
	if (rcs == null) {
		_data = new Vector();
	}
	else {
		_data = rcs;
	}
	
	_rows = rcs.size();
}

/**
Returns the class of the data stored in a given column.  
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_ID:	return String.class;
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
		case COL_ID:	return "ID";
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
		case COL_ID:	return "%-20s";
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

	NWSRFS_RatingCurve rc = (NWSRFS_RatingCurve)_data.get(row);

	switch (col) {
		case COL_ID:	return rc.getRCID();
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

	widths[COL_ID] = 	10;
	
	return widths;
}

}
