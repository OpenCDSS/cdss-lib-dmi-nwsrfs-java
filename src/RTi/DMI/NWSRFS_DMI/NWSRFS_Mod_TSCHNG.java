package RTi.DMI.NWSRFS_DMI;

import java.io.FileWriter;
import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.TS.HourTS;
import RTi.TS.TSIdent;
import RTi.TS.TSIterator;
import RTi.TS.TSUtil;

/**
NWSRFS TSCHNG Mod
<p>
MOD TSCHNG
<p>
Card #1: .TSCHNG date validdate
Card #2:  segid tsid datetype timeint values (keyword/optype/opname)
 values may extend over multiple cards. An ampersand '&' 
 indicates a continuation card.
 <p>
 An operation name may appear after the values
<p>
where
date - is the date for whic the data value applies ...
validdate  - is the date after wich the MOD is no longer valid ...
tsid     - ts identifier
datatype - is the date type code
timeint - is the data time interval
vales - are the values
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
  __type = NWSRFS_ModType.TSCHNG;
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
@parm modLineStart - starting line of mod in file
*/
public static NWSRFS_Mod_TSCHNG parse ( List<String> modstrings, int modLineStart )
throws Exception
{	
  String routine = "NWSRFS_Mod_TSCHNG.parse";
	NWSRFS_Mod_TSCHNG mod = new NWSRFS_Mod_TSCHNG ();
	int nLines = modstrings.size();
	// TODO SAM 2008-01-14 Need to check for minimum size
	// Parse first line, which has the start date
	String line = (String)modstrings.get(0);
	List<String> tokens = StringUtil.breakStringList ( line, " ", StringUtil.DELIM_SKIP_BLANKS );
	if ( tokens == null ) {
		throw new Exception ( "Expecting 3 tokens on first row.");
	}
	int nTokens = tokens.size();
	if ( nTokens != 3 ) {
		throw new Exception ( "Expecting 3 tokens on first row.");
	}
	String modtype = tokens.get(0);
	
	String start_String = tokens.get(1);
	String end_String = tokens.get(2);
	
	DateTime start_DateTime = stringToDateTime(start_String);
	mod.setStart ( start_DateTime );
	
	DateTime end_DateTime = stringToDateTime(end_String);
  mod.setEnd ( end_DateTime );
  Message.printStatus ( 2, routine, modtype +
      " date/time: " + start_DateTime + " - " + end_DateTime );
  
	
	// Parse the second line:
	// <segid> <tsid> <datetype> <timeint> values [values] [&|name] 
	int jstart = 0;
	String token;
	List<String> values = new Vector<String>();	// Accumulate the data values
	for ( int i = 1; i < nLines; i++ ) 
	  {
		line = modstrings.get(i);
		tokens = StringUtil.breakStringList ( line, " ", StringUtil.DELIM_SKIP_BLANKS );
		if ( tokens == null ) {
			Message.printStatus ( 2, routine,"No tokens on line. Expecting "
			    + "\n<segid> <tsid> <datatype> <timeint> <value> [values] [opName]"
			    + "\nFile:  ?"
			    +"\n line: " + modLineStart);
		}
		int tsize = tokens.size();
		if ( i == 1 )
		  {
			  // Expecting <segid> <tsid> <datatype> <timeint> <value> [values] [opName]
		    // a token that can't be converted to a double [opName] terminates 
		    // the values.

			mod.setSegment ( tokens.get(0));
			mod.setTsid ( tokens.get(1));
			mod.setTsDataType ( tokens.get(2));
			mod.setTsInterval ( Integer.parseInt((String)tokens.get(3)));
			jstart = 4;
		}
		else 
		  {
			jstart = 0;
		  }
		for ( int j = jstart; j < tsize; j++ )
		  {
			token = tokens.get(j);
			//
			// Guard against token with trailing '&'
			if (token.charAt(token.length()-1) == '&')
			    {
			    token = token.substring(0,token.length()-1);
			    }
			if ( StringUtil.isDouble(token) )
			  {
			    values.add ( token );
			  }
			else if (token.equals("&"))
			  {
			    // continuation of values on next line
			    break;
			  }
			// else must be a keyword/optype/opname 
		}
	}
	
	// Now convert the values to floating point numbers.
	int nvals = values.size();
	double [] data = new double[nvals];
	for ( int i = 0; i < nvals; i++ ) {
		data[i] = Double.parseDouble((String)values.get(i));
	}
	
	mod.setTS(createHourTS(mod, start_DateTime, nvals, data));

	return mod;
}

/**
 * @param mod
 * @param start_DateTime
 * @param nvals
 * @param data
 * @throws Exception
 */
private static HourTS createHourTS(NWSRFS_Mod_TSCHNG mod,
    DateTime start_DateTime,  int nvals, double[] data) throws Exception
{
  // Create TS
	HourTS ts = new HourTS ();
	int tsint = mod.getTsInterval ();
	ts.setIdentifier ( new TSIdent(mod.getTsid(),"NWSRFS",mod.getTsDataType(),""+tsint+"Hour","") );
	ts.setDate1( start_DateTime );
	
	DateTime end = new DateTime ( start_DateTime );
	end.addHour(tsint*nvals -1);
	ts.setDate2 ( end );
	
	ts.allocateDataSpace();
	// Loop through the data points and set the data
	DateTime date = new DateTime ( start_DateTime );
	for ( int i = 0; i < nvals; i++, date.addHour(tsint) ) {
		ts.setDataValue( date, data[i]);
	}
	// convert MAT values from degrees Fahrenheit to Celsius
	ts.setDataUnits("DEGF");
	TSUtil.convertUnits(ts,"DEGC");
	return ts;
}


/**
 * @param routine
 * @param modtype
 * @param start_String
 * @return
 */
private static DateTime stringToDateTime(String start_String)
{
  DateTime start_DateTime = new DateTime();

	start_DateTime.setMonth(Integer.parseInt(start_String.substring(0,2)));
	start_DateTime.setDay(Integer.parseInt(start_String.substring(2,4)));
	start_DateTime.setYear(Integer.parseInt(start_String.substring(4,6)));
	if ( start_DateTime.getYear() < 70 ) {
		start_DateTime.setYear(start_DateTime.getYear() + 2000 );
	}
	start_DateTime.setTimeZone(start_String.substring(6));
	
  return start_DateTime;
}

/**
Set the time series for the data.
*/
public void setTS ( HourTS ts )
{
	__ts = ts;
}

public String toString()
{
  String s = "NWSRFS_ModType:" + __type + " start:" + __start + " end:" + __end
  + " segid:"+ __segment +" tsid:"+ __tsid + 
  " datatype:"+ __tsDataType +" " + __ts;
  return s;
}


public void writeMAT2prdtil(FileWriter fileWriter)
{
  StringBuffer valueAccu = new StringBuffer();
  // Get TS values
  TSIterator tsi = null;
  try
    {
      tsi = __ts.iterator ( __ts.getDate1(), __ts.getDate2() );
    }
  catch (Exception e1)
    {
      e1.printStackTrace();
    }
  //DateTime date;
  double value;
  //TSData data;
  for ( ; //(data =
  	tsi.next() != null; ) {
        // The first call will set the pointer to the
        // first data value in the period.  next() will return
        // null when the last date in the processing period
        // has been passed.
    //date = tsi.getDate();
    value = tsi.getDataValue();
    System.out.println(">>>--->" + value);
    valueAccu.append(' ').append(value);
  }
  
 try
  {
    fileWriter.write(__tsid + " MAT "+ __tsInterval + "\n");
    fileWriter.write(__start+ valueAccu.toString()+ "\n");
  }
catch (Exception e)
  {
    e.printStackTrace();
  }
}
}
