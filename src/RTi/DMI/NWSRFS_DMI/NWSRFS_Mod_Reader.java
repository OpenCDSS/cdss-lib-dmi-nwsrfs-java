package RTi.DMI.NWSRFS_DMI;

import java.awt.font.LineMetrics;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import RTi.Util.IO.FileCollector;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;

/**
Class to read Mod files and return Mod instances.
*/
public class NWSRFS_Mod_Reader
{

/**
Mod file to be read.
*/
private String _filename = null;
private List _modList = new ArrayList();	
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
      read();
    }
}

/**
Parse a List of strings for a Mod.  The first line will contain the mod type.
TODO SAM 2008-01-14 Evaluate moving to factory or other.  This is good enough for now.
@param modstrings List of strings for the mod.
*/
private NWSRFS_Mod parseMod ( List modstrings, int modLineStart )
{	
  String routine = "NWSRFS_Mod_Reader.parseMod";
	String line = (String)modstrings.get(0);
	try {
		// TODO: needs dot in  front! if ( line.startsWith(NWSRFS_ModType.TSCHNG.toString()) ) {
    if ( line.startsWith(".TSCHNG")){
			// TSCHNG Mod...
			return NWSRFS_Mod_TSCHNG.parse ( modstrings, modLineStart);
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
public void read ()
throws IOException
{	
	LineNumberReader f = null;
	f = new LineNumberReader( new InputStreamReader( IOUtil.getInputStream ( _filename )));
	String line;
  int modLineStart = 1; // Line # for start of mod
	Vector modstrings= new Vector();
  boolean inMod = false;
	/*
	 * Scan for mod, all mods begin w/ dot
	 * For now only ".TSCHNG mods are of interest
	 * A mod is terminated by another mod, a blank line or EOF
	 */
	while ( true) 
	  {
	    line = f.readLine();
	    System.out.println(f.getLineNumber() +" >> " + line);
	    if (line == null)
	      {
	        if (inMod)
	          {
	            emitMod(modstrings, modLineStart);
	          }
	        break;
	      }
	    if ( line.startsWith(".") )
	      {
	        if (inMod)
	          {
	            emitMod(modstrings, modLineStart);
	            modstrings.clear();
	          }
	        modLineStart = f.getLineNumber();
	        line = line.trim();
	        modstrings.add ( line );
	        inMod = true;
	      }
	    else if (line.length() == 0)
	      {
	        if (inMod)
	          {
	           
	            // Create new NWSRFS_Mod & emit
	            emitMod(modstrings, modLineStart);
	            modstrings.clear();
	            inMod = false;
	          }
	        else
	          {/* skip */}
	      }
		else
		  {
		    modstrings.add ( line );
		  }
	  } // eof while
	
	// TODO f.close();

} // eof read

private void emitMod(Vector modStrings, int line)
{
  // Only able to process TSCHNG mods so...
  if (!((String)modStrings.elementAt(0)).startsWith(".TSCHNG")) 
    {
      return;
    }
      
  NWSRFS_Mod mod = parseMod(modStrings, line);
  
  if ( mod != null) 
    {
      System.out.println ("==> " + mod);
      // Only interested in "MAP" datatype
      if (mod.getTsDataType().equals("MAP"))
        {
          _modList.add ( mod );
        }
    }
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
  // TODO String modsDirString = NWSRFS_Util.getAppsDefaults(OFS_MODS_DIR);
  String modsDirString = "test/unit/data";
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

  NWSRFS_Mod_Reader _modReader = null;
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
  
  // Convert the .TSCHNG mods to FMAP
  //TODO: dre: I don't know where to get this from
  DateTime now = new DateTime(DateTime.DATE_CURRENT);
  List fMAPMods = NWSRFS_Mod_Util.convertTSCHNG_MAP_ModsToFMAPMods(_modReader.getMods(), now);  
  
  NWSRFS_Mod_Util.writeTSCHNG_MAT_ModsToTSEDIT(fMAPMods, now,
  "test/unit/results/FMAPMODS.IFP");
}

/**
 * @return
 */
private List getMods()
{
  return _modList;
}
}
