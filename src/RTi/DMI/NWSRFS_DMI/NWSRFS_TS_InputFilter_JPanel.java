//------------------------------------------------------------------------------
// NWSRFS_TS_InputFilter_JPanel - input filter panel for TS data 
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2004-08-27	Steven A. Malers, RTi	Implement to simplify generic code that
//					can use instanceof to figure out the
//					input filter panel type.
//------------------------------------------------------------------------------

package RTi.DMI.NWSRFS_DMI;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.InputFilter;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.String.StringUtil;

public class NWSRFS_TS_InputFilter_JPanel extends InputFilter_JPanel
{

/**
Create an InputFilter_JPanel for creating where clauses for time series queries.  This is used by TSTool.
@return a JPanel containing InputFilter instances for time series queries.
@exception Exception if there is an error.
*/
public NWSRFS_TS_InputFilter_JPanel ()
throws Exception
{	List input_filters = new Vector(2);
	input_filters.add ( new InputFilter (
		"", "",
		StringUtil.TYPE_STRING,
		null, null, true ) );	// Blank to disable filter (no filter active)
	input_filters.add ( new InputFilter (
		"ID", "ID",
		StringUtil.TYPE_STRING,
		null, null, true ) );
	/* TODO SAM - enable later (is it stored with the TS?)
	input_filters.addElement ( new InputFilter (
		"Name", "Name",
		StringUtil.TYPE_STRING,
		null, null, true ) );
	*/
	setToolTipText ( "<html>NWSRFS FS5Files queries can be filtered <BR>based on time series header information.</html>" );
	setInputFilters ( input_filters, 1, -1 );
}

}