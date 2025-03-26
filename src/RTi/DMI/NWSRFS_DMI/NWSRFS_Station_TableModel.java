// NWSRFS_Station_TableModel - table model for a list of NWSRFS stations.

package RTi.DMI.NWSRFS_DMI;

import java.util.ArrayList;
import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This class is a table model for displaying stations in the NWSRFS GUI.
*/
@SuppressWarnings({ "serial", "rawtypes" })
public class NWSRFS_Station_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
Number of columns in the table model.
*/
private final static int __COLUMNS = 6;

/**
References to the columns.
*/
public static final int 
	COL_STATION_ID = 0,
	COL_PCPN = 1,
	COL_RRS = 2,
	COL_TEMP = 3,
	COL_PE = 4,
	COL_STATION_DESC = 5;

/**
Constructor.  
@param stations the stations that will be displayed in the table.
*/
@SuppressWarnings("unchecked")
public NWSRFS_Station_TableModel(List<NWSRFS_Station> stations) {
	if (stations == null) {
		_data = new ArrayList<>();
	}
	else {
		_data = stations;
	}
	
	_rows = stations.size();
}

/**
Returns the class of the data stored in a given column.  
@param columnIndex the column for which to return the data class.
*/
@SuppressWarnings({ "unchecked" })
public Class getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_STATION_ID:	return String.class;
		case COL_STATION_DESC:	return String.class;
		case COL_PCPN:		return Boolean.class;
		case COL_PE:		return Boolean.class;
		case COL_RRS:		return Boolean.class;
		case COL_TEMP:		return Boolean.class;
		default:		return String.class;
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
		case COL_STATION_ID:	return "ID";
		case COL_PCPN:		return "PCPN";
		case COL_PE:		return "PE";
		case COL_RRS:		return "RRS";
		case COL_TEMP:		return "TEMP";
		case COL_STATION_DESC:	return "DESCRIPTION";
		default:		return " ";
	}	
}

/**
Returns the tooltips for the columns.
*/
public String[] getColumnToolTips() {
	String[] tips = {
		null,
		"Precipitation Station",
		"River, Reservoir, or Snow Station",
		"Temperature Station",
		"Potential Evaporation Station",
		null
	};
	return tips;
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
		case COL_STATION_ID:	return "%-20s";
		case COL_STATION_DESC:	return "%-40s";
		case COL_PCPN:		return "";
		case COL_PE:		return "";
		case COL_RRS:		return "";
		case COL_TEMP:		return "";
		default:		return "%-8s";
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

	NWSRFS_Station station = (NWSRFS_Station)_data.get(row);

	switch (col) {
		case COL_STATION_ID:	return station.getID();
		case COL_STATION_DESC:	return station.getDescription();
		case COL_PCPN:		return Boolean.valueOf(station.getIsPCPN());
		case COL_PE:		return Boolean.valueOf(station.getIsPE());
		case COL_RRS:		return Boolean.valueOf(station.getIsRRS());
		case COL_TEMP:		return Boolean.valueOf(station.getIsTEMP());
		default:		return "";
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

	widths[COL_STATION_ID] = 	7;
	widths[COL_STATION_DESC] =	20;
	widths[COL_PCPN] =		3;
	widths[COL_PE] =		2;
	widths[COL_RRS] =		2;
	widths[COL_TEMP] =		3;
	
	return widths;
}

}
