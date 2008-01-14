package RTi.DMI.NWSRFS_DMI;

import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.TS.HourTS;
import RTi.TS.TSIdent;

/**
NWSRFS TSCHNG Mod
*/
public class NWSRFS_Mod_TSCHNG extends NWSRFS_Mod
{
	
/**
Time series to hold the data values, starting on the mod start date.  The first value is recorded at the
start (see base class).
*/
HourTS __ts = null;
	
/**
Constructor.
*/
public NWSRFS_Mod_TSCHNG ()
{
}

/**
Return the time series used for data.
*/
public HourTS getTS ()
{
	return __ts;
}

/**
Parse a mod and return an instance.
@param modstrings List of strings for the mod.
*/
public static NWSRFS_Mod_TSCHNG parse ( List modstrings )
throws Exception
{	String routine = "NWSRFS_Mod_TSCHNG.parse";
	NWSRFS_Mod_TSCHNG mod = new NWSRFS_Mod_TSCHNG ();
	int size = modstrings.size();
	// TODO SAM 2008-01-14 Need to check for minimum size
	// Parse first line, which has the start date
	String line = (String)modstrings.get(0);
	Vector tokens = StringUtil.breakStringList ( line, " ", StringUtil.DELIM_SKIP_BLANKS );
	if ( tokens == null ) {
		throw new Exception ( "Expecting 3 tokens on first row.");
	}
	size = tokens.size();
	if ( size != 3 ) {
		throw new Exception ( "Expecting 3 tokens on first row.");
	}
	String modtype = (String)tokens.elementAt(0);
	String start_String = (String)tokens.elementAt(1);
	DateTime start_DateTime = new DateTime();
	start_DateTime.setMonth(Integer.parseInt(start_String.substring(0,2)));
	start_DateTime.setDay(Integer.parseInt(start_String.substring(2,4)));
	start_DateTime.setYear(Integer.parseInt(start_String.substring(4,6)));
	if ( start_DateTime.getYear() < 70 ) {
		start_DateTime.setYear(start_DateTime.getYear() + 2000 );
	}
	start_DateTime.setTimeZone(start_String.substring(6));
	Message.printStatus ( 2, routine, modtype + " date/time: " + start_DateTime );
	mod.setStart ( start_DateTime );
	// Parse the second line
	int jstart = 0;
	String token;
	Vector values = new Vector();	// Accumulate the data values
	for ( int i = 0; i < size; i++ ) {
		line = (String)modstrings.get(i);
		tokens = StringUtil.breakStringList ( line, " ", StringUtil.DELIM_SKIP_BLANKS );
		if ( tokens == null ) {
			break;
		}
		int tsize = tokens.size();
		if ( i == 0 ) {
			// Expect identifier information
			mod.setSegment ( (String)tokens.elementAt(0));
			mod.setTsid ( (String)tokens.elementAt(1));
			mod.setTstype ( (String)tokens.elementAt(2));
			mod.setTsint ( Integer.parseInt((String)tokens.elementAt(3)));
			jstart = 3;
		}
		else {
			jstart = 3;
		}
		for ( int j = jstart; j < tsize; j++ ) {
			token = (String)tokens.get(j);
			if ( StringUtil.isInteger(token) ) {
				values.addElement ( token );
			}
		}
	}
	// Now convert the values to floating point numbers.
	int nvals = values.size();
	double [] data = new double[nvals];
	for ( int i = 0; i < nvals; i++ ) {
		data[i] = Double.parseDouble((String)values.get(i));
	}
	HourTS ts = new HourTS ();
	int tsint = mod.getTsint ();
	ts.setIdentifier ( new TSIdent(mod.getTsid(),"NWSRFS",mod.getTstype(),""+tsint+"Hour","") );
	ts.setDate1( start_DateTime );
	DateTime end = new DateTime ( start_DateTime );
	end.addHour(tsint*nvals);
	ts.setDate2 ( end );
	ts.allocateDataSpace();
	// Loop through the data points and set the data
	DateTime date = new DateTime ( start_DateTime );
	for ( int i = 0; i < nvals; i++, date.addHour(tsint) ) {
		ts.setDataValue( date, data[i]);
	}

	return mod;
}

/**
Set the time series for the data.
*/
public void setTS ( HourTS ts )
{
	__ts = ts;
}

}
