package RTi.DMI.NWSRFS_DMI;

/**
NWSRFS runtime modification (MOD) types.
TODO SAM 2008-01-13 Need to put into an enumeration when updating to Java 1.5+
*/
public class NWSRFS_ModType
{

    /**
     * UNKNOWN indicates that the MOD type is unknown. 
     */
    public static NWSRFS_ModType UNKNOWN = new NWSRFS_ModType(-1, "UNKNOWN");
    
	/**
	 * Future Mean Areal Precipitation (FMAP) Mod.
	 */
	public static NWSRFS_ModType FMAP = new NWSRFS_ModType(0, "FMAP");
	
	/**
	 * Time series change Mod.
	 */
	public static NWSRFS_ModType TSCHNG = new NWSRFS_ModType(1, "TSCHNG");
	
    /**
	 * Internal integer key
	 * @uml.property  name="__type"
	 */
	private int __type;
	/**
	 * Type name, e.g., "TSCHNG".
	 * @uml.property  name="__typename"
	 */
	private String __typename;
	
	/**
	 * Construct the Mod type using the type and name.  It is
	 * private because other code should use the predefined instances.
	 * @param type
	 * @param typename
	 */
	private NWSRFS_ModType ( int type, String typename ){
		__type = type;
		__typename = typename;
	}
	
	/**
	 * Determine if two types are equal.
	 */
	public boolean equals ( NWSRFS_ModType type )
	{
		if ( __type == type.getType() ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Return the Mod type.
	 * @return the Mod type.
	 */
    public int getType()
      {
        return __type;
      }

	/**
	 * Return a String representation of the Mod - the type.
	 */
	public String toString () {
		return __typename;
	}
	
}
