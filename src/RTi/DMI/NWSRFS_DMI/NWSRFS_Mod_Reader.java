package RTi.DMI.NWSRFS_DMI;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import RTi.Util.IO.FileCollector;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;

/**
Class to read Mod files and return Mod instances.
*/
public class NWSRFS_Mod_Reader
{

/**
Mod file to be read.
*/
private String _filename = null;
private List mods = new ArrayList();	
/**
Constructor that takes a file name.
*/
public NWSRFS_Mod_Reader ( String filename )
throws IOException
{
	_filename = filename;
	File f = new File ( filename );
	if ( !f.canRead() )
	  {
	    throw new IOException ( "File is not readable: \"" + filename + "\"");
	  }
	else 
	  {
	    read(mods);
	  }
}

/**
Parse a List of strings for a Mod.  The first line will contain the mod type.
TODO SAM 2008-01-14 Evaluate moving to factory or other.  This is good enough for now.
@param modstrings List of strings for the mod.
*/
private NWSRFS_Mod parseMod ( List modstrings )
{	String routine = "NWSRFS_Mod_Reader.parseMod";
	String line = (String)modstrings.get(0);
	try {
		// TODO: needs dot in  front! if ( line.startsWith(NWSRFS_ModType.TSCHNG.toString()) ) {
    if ( line.startsWith(".TSCHNG")){
			// TSCHNG Mod...
			return NWSRFS_Mod_TSCHNG.parse ( modstrings );
		}
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, "Error parsing mod.");
		Message.printWarning ( 3, routine, e );
	}
	return null;
}

/**
Read a Mod file.
@param list List of Mod instances to append to.  If null, a new instance will be created
and returned.
@return list of Mod instances.
*/
public List read ( List list )
throws IOException
{	
  if ( list == null ) 
    {
		list = new Vector();
	}
	BufferedReader f = null;
	f = new BufferedReader ( new InputStreamReader( IOUtil.getInputStream ( _filename )) );
	String line;
	Vector modstrings= new Vector();

	while ( true ) 
	  {
	    line = f.readLine();
	    if ( line != null )
	      {
	        line = line.trim();
	        if ( line.length() == 0 )
	          {
	            // Blank line
	            continue;
	          }
	      }
		if ( (line != null) || line.startsWith(".") )
		  {
			// Start of a Mod (and end of previous mod)
			// If have other lines for the Mod, parse to get an instance
			if ( modstrings.size() > 0 ) 
			  {
				// Have a list of strings from before.  Parse the mods
				NWSRFS_Mod mod = parseMod(modstrings);
				if ( mod != null ) 
				  {
					list.add ( mod );
				  }
			  }
			if ( line == null ) 
			  {
			    // Done.
			    break;
			  }
			else
			  {
				// Create a new list of mods and append...
				modstrings = new Vector();
				modstrings.add ( line );
			}
		}
	}
	f.close();
	return list;
}

/** 
 * Test harness
 * @param args
 */
public static void main(String args[])
{
  String OFS_MODS_DIR = "ofs_mods_dir";
  System.out.println("Test convertTSCHNG_MAP_ModsToFMAPMods");
  
  // Get App defaults
  String modsDirString = NWSRFS_Util.getAppsDefaults(OFS_MODS_DIR);
  System.out.println("AppsDefault " +OFS_MODS_DIR + ": "+ modsDirString);
  
  // check !null check exists, check readable, check directory
  if(modsDirString == null)throw new RuntimeException("AppsDefaultNull: " 
      + OFS_MODS_DIR);
  File modsDir = new File(modsDirString);
  if (!modsDir.isDirectory()) throw new RuntimeException("NotADirectory: modsDirString");
  if (!modsDir.canRead()) throw new RuntimeException("NotADirectory: modsDirString");
  
  // Get files
  FileCollector fileCollector =new FileCollector(modsDirString, "", false);
  List fileNames =fileCollector.getFiles();

  NWSRFS_Mod_Reader _modReader;
  //iterate over the files
  for (int i = 0; i < fileNames.size(); i++)
    {
      try
        {
          _modReader = new NWSRFS_Mod_Reader((String)fileNames.get(i));
        }
      catch (IOException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
    }
  
}
}
