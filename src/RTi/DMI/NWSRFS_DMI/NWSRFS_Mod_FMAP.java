package RTi.DMI.NWSRFS_DMI;

import java.util.List;
import java.util.Vector;

import RTi.TS.HourTS;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

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

}
