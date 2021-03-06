package RTi.DMI.NWSRFS_DMI;

import java.io.FileWriter;

import RTi.TS.HourTS;
import RTi.TS.TSIterator;

/**
NWSRFS TSCHNG Mod
*/
public class NWSRFS_Mod_FMAP extends NWSRFS_Mod
{
	
/**
Time series to hold the data values, starting on the mod start date.  The first value is recorded at the
start (see base class).
*/
HourTS __ts = null;
	
/**
Constructor.
*/
public NWSRFS_Mod_FMAP ()
{
  __type = NWSRFS_ModType.FMAP;
}

/**
Return the time series used for data.
*/
public HourTS getTS ()
{
	return __ts;
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
  String s = "NWSRFS_ModType:" + __type + " start:" + __start
  +" tsid:"+ __tsid +" " + __ts;
  return s;
}
/**
 * Writes FMAP section to specified writer
 * @param fileWriter
 * @return 
 */
public void write(FileWriter fileWriter)
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
    fileWriter.write(".FMAP6" + __start + "\n");
    fileWriter.write(__tsid + valueAccu.toString()+ "\n");
  }
catch (Exception e)
  {
    e.printStackTrace();
  }
}
}
