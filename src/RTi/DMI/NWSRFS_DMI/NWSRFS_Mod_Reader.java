package RTi.DMI.NWSRFS_DMI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import RTi.Util.IO.DataUnits;
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
private List<NWSRFS_Mod> _modList = new ArrayList<>();	

/**
Constructor that takes a file name.
*/
public NWSRFS_Mod_Reader ( String filename )
throws IOException {
  _filename = filename;
  File f = new File ( filename );
  if ( !f.canRead() ) {
      throw new IOException ( "File is not readable: \"" + filename + "\"");
  }
  else {
      read();
  }
}

/**
Parse a List of strings for a Mod.  The first line will contain the mod type.
TODO SAM 2008-01-14 Evaluate moving to factory or other.  This is good enough for now.
@param modstrings List of strings for the mod.
*/
private NWSRFS_Mod parseMod ( List<String> modstrings, int modLineStart ) {	
  String routine = getClass().getSimpleName() + ".parseMod";
	String line = modstrings.get(0);
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
	List<String> modstrings= new ArrayList<>();
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

private void emitMod(List<String> modStrings, int line)
{
  // Only able to process TSCHNG mods so...
  if (!modStrings.get(0).startsWith(".TSCHNG")) 
    {
      return;
    }
      
  NWSRFS_Mod mod = parseMod(modStrings, line);
  
  if ( mod != null) 
    {
      System.out.println ("==> " + mod);
      // Only interested in "MAP" & MAT datatype
      if (mod.getTsDataType().equals("MAP")
          || mod.getTsDataType().equals("MAT"))
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
  System.out.println("*** Test convertTSCHNG_MAP_ModsToFMAPMods");
  System.out.println("***  Be sure to check temperature conversion...");
  System.out.println("*** - Uses test/unit/data/TSCHNG.txt as input test");
  System.out.println("*** - Outputs files to the test/unit/data directory");
  
  Message.printWarning(1, "teest", "printWarning 1");
  try
    {
      readDataUnitsFile ();
    }
  catch (Exception e1)
    {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
  
  
  // Get App defaults
  // TODO String modsDirString = NWSRFS_Util.getAppsDefaults(OFS_MODS_DIR);
  String modsDirString = "test/unit/data";
  System.out.println("AppsDefault " +OFS_MODS_DIR + ": "+ modsDirString);
  
  // check !null check exists, check readable, check directory
  //if(modsDirString == null) {
	  //throw new RuntimeException("AppsDefaultNull: " + OFS_MODS_DIR);
  //}
  File modsDir = new File(modsDirString);
  if (!modsDir.isDirectory()) throw new RuntimeException("NotADirectory: modsDirString");
  if (!modsDir.canRead()) throw new RuntimeException("NotADirectory: modsDirString");
  
  // Get files
  FileCollector fileCollector = new FileCollector(modsDirString, "", false);
  List<String> fileNames = fileCollector.getFiles();
List<NWSRFS_Mod> modList = new ArrayList<>();
  NWSRFS_Mod_Reader _modReader = null;
  //iterate over the files
  for (int i = 0; i < fileNames.size(); i++)
    {
      try
        {
          _modReader = new NWSRFS_Mod_Reader(fileNames.get(i));
          modList.addAll(_modReader.getMods());
        }
      catch (IOException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
    }
  
  // Convert the .TSCHNG mods to FMAP
  //TODO: dre: I don't know where to get this from
  /* TODO Uncomment when code is needed
  DateTime now = null;
  try
    {
      now = new DateTime(DateTime.parse("2005-02-10 00:00:00 08Z"));
    }
  catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    */
//  List fMAPMods = NWSRFS_Mod_Util.convertTSCHNG_MAP_ModsToFMAPMods(modList, now);  
//  
//  for(int i = 0; i < fMAPMods.size(); i++)
//    {
//      System.out.println(fMAPMods.get(i).toString());
//    }
//  
//  // capture fMAPMods in "FMAPMODS.IFP"
//  outputFMAP(modsDirString, fMAPMods);
 
  // capture .TSCHNG MAT to
  outputXXX(modsDirString, modList);
  //NWSRFS_Mod_Util.writeTSCHNG_MAT_ModsToTSEDIT(fMAPMods, now,
  //"test/unit/results/FMAPMODS.IFP");
}
private static void outputXXX(String modsDirString, List<NWSRFS_Mod> modList) {
  String outputFile = modsDirString + "/TSEDIT.MAT"; 
  File f = new File(outputFile);
  //if (!f.canWrite())throw new RuntimeException("NotWritable: " + outputFile);
  FileWriter fileWriter = null;
  try
    {
      fileWriter = new FileWriter(f);
      
      fileWriter.write("@TSEDIT\n");
      for(int i = 0; i < modList.size(); i++)
        { 
         System.out.println ("??" +(NWSRFS_Mod_TSCHNG)modList.get(i));
          if (((NWSRFS_Mod)modList.get(i)).getTsDataType().equals("MAT"))
            {
              ((NWSRFS_Mod_TSCHNG)modList.get(i)).writeMAT2prdtil(fileWriter);
            }
        }
    }
  catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
 finally
 {   
   try
    {
      fileWriter.close();
    }
  catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
 }
}
/**
 * @param modsDirString
 * @param fMAPMods
 */
/* TODO Enable code when needed
private static void outputFMAP(String modsDirString, List fMAPMods)
{
  String outputFile = modsDirString + "/FMAPMODS.IFP";
  File f = new File(outputFile);
  //if (!f.canWrite())throw new RuntimeException("NotWritable: " + outputFile);
  FileWriter fileWriter = null;
  try
    {
      fileWriter = new FileWriter(f);
      for(int i = 0; i < fMAPMods.size(); i++)
        { 
         
      // TODO?    if (((NWSRFS_Mod_FMAP)fMAPMods.get(i)).getTsDataType().equals("MAP"))
            {
             ((NWSRFS_Mod_FMAP)fMAPMods.get(i)).write(fileWriter);
            }
        }
    }
  catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
 finally
 {   
   try
    {
      fileWriter.close();
    }
  catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
 }
}
*/

/**
 * @return
 */
private List<NWSRFS_Mod> getMods() {
  return _modList;
}

/**
Read the DATAUNIT NWSRFS system file, located with the apps defaults token: rfs_sys_dir.
The data units are then stored in memory and are used for displays.
@exception if an error occurs reading the data units.
*/
public static void readDataUnitsFile ()
throws Exception
{
  String __CLASS  = "NWSRFS_Mod_Reader";
  String routine = __CLASS + ".readDataUnitsFile";
  String rfs_sys_dir = IOUtil.getPathUsingWorkingDir ( NWSRFS_Util.getAppsDefaults("rfs_sys_dir"));

  if ( rfs_sys_dir == null || rfs_sys_dir.length() < 1) {
    String message = "Unable to use to determine the value for the apps defaults token: \"" +
      "rfs_sys_dir\".  Can't read data units.  Verify that the apps defaults are set. ";
    Message.printWarning( 2, routine, message );
    throw new Exception ( message );
  }
  
  String data_units_path = null;
  data_units_path = rfs_sys_dir + File.separator + "DATAUNIT";

  //now read it in
  try {
    Message.printStatus( 2, routine, "Reading the data units from \"" + data_units_path + "\".");
    DataUnits.readNWSUnitsFile( data_units_path );
  }
  catch ( Exception e) {
    if ( Message.isDebugOn ) {
      Message.printWarning( 2, routine, e );
    }
    String message = "Unable to read in system's DATAUNIT file: \"" + data_units_path + "\".";
    Message.printWarning( 2, routine, message );
    throw new Exception ( message );
  }
}
}
