package RTi.DMI.NWSRFS_DMI;

import RTi.Util.Time.DateTime;

/**
Base class for NWSRFS runtime modifications (MODS).
*/
public abstract class NWSRFS_Mod
{

/**
Mod type (e.g., TSCHNG)
*/
private NWSRFS_ModType __type = null;

/**
Segment to which the Mod applies.
*/
private String __segment = null;

/**
Start date/time for the mod.
*/
private DateTime __start = null;

/**
End date/time for the mod.
*/
private DateTime __end = null;

/**
Time series identifier to which the Mod applies.
*/
private String __tsid = null;

/**
Time series data type which the Mod applies.
*/
private String __tstype = null;

/**
Time series interval to which the Mod applies.
*/
private int __tsint = 0;

/**
Return the end date/time (for last data value).
*/
public DateTime getEnd ( )
{
	return __end;
}

/**
Return the segment.
*/
public String getSegment ( )
{
	return __segment;
}

/**
Return the start date/time (for first data value).
*/
public DateTime getStart ( )
{
	return __start;
}

/**
Return the time series identifier (location).
*/
public String getTsid ( )
{
	return __tsid;
}

/**
Return the time series interval.
*/
public int getTsint ( )
{
	return __tsint;
}

/**
Return the time series data type.
*/
public String getTstype ( )
{
	return __tstype;
}

/**
Set the end date/time (for last data value).
*/
public void setEnd ( DateTime end )
{
	__end = end;
}

/**
Set the segment.
*/
public void setSegment ( String segment )
{
	__segment = segment;
}

/**
Set the start date/time (for first data value).
*/
public void setStart ( DateTime start )
{
	__start = start;
}

/**
Set the time series identifier (location).
*/
public void setTsid ( String tsid )
{
	__tsid = tsid;
}

/**
Set the time series data type.
*/
public void setTsint ( int tsint )
{
	__tsint = tsint;
}

/**
Set the time series data type.
*/
public void setTstype ( String tstype )
{
	__tstype = tstype;
}

}
