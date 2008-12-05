//------------------------------------------------------------------------------
// NWSRFS_USERPARM - class to contain the operational forecast system user
//				parameters (FS5Files USERPARM records)
//------------------------------------------------------------------------------
// History:
//
// 2006-10-03	Steven A. Malers, RTi	Initial version.
//------------------------------------------------------------------------------
// Endheader

package RTi.DMI.NWSRFS_DMI;

/**
The NWSRFS_USERPARM class contains the data for Operational Forecast System
general user parameters stored in the USERPARM FS5Files file, as per the
following definition:
<pre>

See NWSRFS IX.4-USERPARM-2

</pre>
Currently only the default time zone code is read.
*/

public class NWSRFS_USERPARM
{

/**
Default time zone code.
*/
private String __time3;

/**
Constructor.
*/
public NWSRFS_USERPARM()
{
	initialize();
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize() {
	__time3	 = null;
}

/**
Return the default time zone code TIME(3).
@return the default time zone code TIME(3).
*/
public String getTime3() {
	return __time3; 
}

/**
Initialize instance.
*/
private void initialize()
{
	__time3	= "";
}

/**
Set the default time zone code TIME(3).
@param time3 Default time zone code.  For example, use "Z", "CST" or other
standard NWSRFS time zone codes.
*/
public void setTime3(String time3)
{
	__time3 = time3;
}

}
