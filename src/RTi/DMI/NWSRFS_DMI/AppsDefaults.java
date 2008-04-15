/*
 * Created on Jul 10, 2003
 * 
 * Modified 12/28/04 to provide getInt() method.
 */
package RTi.DMI.NWSRFS_DMI;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;

/**
 * Provides functionality to resolve app_defaults tokens. App_defaults
 * tokens are specified as environment variables or in Apps_Defaults 
 * files as name/value pairs.
 * 
 * The search order is defined in {@link #getToken0(String)}
 * <pre>
 * Usage:
 *   AppsDefaults appsDefaults = new AppsDefaults();
 *   String tokenValue = appsDefaults.getToken("tokenName");
 * <pre>
 * 
 * This is the Java version of get_apps_defaults.c
 * @author Chip Gobs
 * 
 * This code was extracted from ICP(ohd.hseb.util.AppsDefaultsImp.java)
 */
public class AppsDefaults
{

   private static final Logger _logger = Logger.getLogger(AppsDefaults.class.getPackage().getName());

   public class NameValuePair
   {

      String name;

      String value;

      public String getName()
      {
         return name;
      }

      public void setName(String name)
      {
         this.name = name;
      }

      public String getValue()
      {
         return value;
      }

      public void setValue(String value)
      {
         this.value = value;
      }

      public NameValuePair(String tokenName, String tokenValue)
      {
         this.name = tokenName;
         this.value = tokenValue;
      }

   }

   private static final String APPS_DEFAULTS_USER = "APPS_DEFAULTS_USER";

   private static final String APPS_DEFAULTS_PROG = "APPS_DEFAULTS_PROG";

   private static final String APPS_DEFAULTS_SITE = "APPS_DEFAULTS_SITE";

   private static final String APPS_DEFAULTS = "APPS_DEFAULTS";

   private static final String RFR_OPEN = "$(";

   private static final String RFR_CLOSE = ")";

   private static final char DELIM = ':';

   private static final char COMMENT = '#';

   private static final char DOUBLE_QUOTE = '\"';

   private static final char SINGLE_QUOTE = '\'';

   private static final int RECUR_LIMIT = 40;
   
   /**
   Does the JVM have a working System.getEnv() method?
   Java 1.4.2 does not but others do.
   */
   private static boolean __jvmhas_getenv = true;    

   private int _recursionCount = 0;

//   private Properties _envProperties = new Properties();

   private String _appsDefaultsUserFilePath = null;

   private String _appsDefaultsProgramFilePath = null;

   private String _appsDefaultsSiteFilePath = null;

   private String _appsDefaultsNationalFilePath = null;

  //TODO: jdk1.5 private HashMap<String,String> cache = new HashMap<String,String>();
   private HashMap cache = new HashMap();
   private String NULL = new String("");

   // --------------------------------------------------------------
   public AppsDefaults()
   {
	  String routine = "AppsDefaults";
	  String version = System.getProperty("java.version");
	  System.out.println ( "version is " + version );
	  if ( version.startsWith("1.4.2")) {
	      __jvmhas_getenv = false;
	  }
	  if ( __jvmhas_getenv ) {
	      _appsDefaultsUserFilePath = System.getenv(APPS_DEFAULTS_USER);
	  }
	  else {
	      _appsDefaultsUserFilePath = NWSRFS_Util.getenv(APPS_DEFAULTS_USER);
	  }
      if (_appsDefaultsUserFilePath == null)
      {
         _logger.info("SystemEnvironmentVariableNULL: The system environment variable APPS_DEFAULTS_USER is null");
         Message.printStatus ( 2, routine, "SystemEnvironmentVariableNULL: The system environment variable APPS_DEFAULTS_USER is null" );
      }
      else
      	{
    	  _appsDefaultsUserFilePath = IOUtil.getPathUsingWorkingDir(_appsDefaultsUserFilePath);
      		_logger.info("APPS_DEFAULTS_USER: " + _appsDefaultsUserFilePath);
      		Message.printStatus ( 2, routine, "APPS_DEFAULTS_USER: " + _appsDefaultsUserFilePath );
      	}
      if ( __jvmhas_getenv ) {
          _appsDefaultsProgramFilePath = System.getenv(APPS_DEFAULTS_PROG);
      }
      else {
          _appsDefaultsProgramFilePath = NWSRFS_Util.getenv(APPS_DEFAULTS_PROG);
      }
      if (_appsDefaultsProgramFilePath == null)
      {
    	  Message.printStatus ( 2, routine, "SystemEnvironmentVariableNULL: The system environment variable APPS_DEFAULTS_PROG is null");
         _logger.info("SystemEnvironmentVariableNULL: The system environment variable APPS_DEFAULTS_PROG is null");
      }
      else {
    	  _appsDefaultsProgramFilePath = IOUtil.getPathUsingWorkingDir(_appsDefaultsProgramFilePath);
    	  _logger.info("APPS_DEFAULTS_PROG: " + _appsDefaultsProgramFilePath);
    	  Message.printStatus ( 2, routine,"APPS_DEFAULTS_PROG: " + _appsDefaultsProgramFilePath);
      }
      
      if ( __jvmhas_getenv ) {
          _appsDefaultsSiteFilePath = System.getenv(APPS_DEFAULTS_SITE);
      }
      else {
          _appsDefaultsSiteFilePath = NWSRFS_Util.getenv(APPS_DEFAULTS_SITE);
      }
      if(_appsDefaultsSiteFilePath == null) 
      {	 Message.printStatus ( 2, routine,"SystemEnvironmentVariableNULL: The system environment variable APPS_DEFAULTS_SITE is null");
         _logger.info("SystemEnvironmentVariableNULL: The system environment variable APPS_DEFAULTS_SITE is null");
      }
      else {
    	  _appsDefaultsSiteFilePath = IOUtil.getPathUsingWorkingDir(_appsDefaultsSiteFilePath);
    	  _logger.info("APPS_DEFAULTS_SITE: " + _appsDefaultsSiteFilePath);
    	  Message.printStatus ( 2, routine,"APPS_DEFAULTS_SITE: " + _appsDefaultsSiteFilePath);
      }
     
      if ( __jvmhas_getenv ) {
          _appsDefaultsNationalFilePath = System.getenv(APPS_DEFAULTS);
      }
      else {
          _appsDefaultsNationalFilePath = NWSRFS_Util.getenv(APPS_DEFAULTS);
      }
      if (_appsDefaultsNationalFilePath == null)
      {Message.printStatus ( 2, routine,"SystemEnvironmentVariableNULL: The system environment variable APPS_DEFAULTS is null");
         _logger.info("SystemEnvironmentVariableNULL: The system environment variable APPS_DEFAULTS is null");
      }
      else {
    	  _appsDefaultsNationalFilePath = IOUtil.getPathUsingWorkingDir(_appsDefaultsNationalFilePath);
    	  _logger.info("APPS_DEFAULTS: " + _appsDefaultsNationalFilePath);
    	  Message.printStatus ( 2, routine,"APPS_DEFAULTS: " + _appsDefaultsNationalFilePath);
      }

      return;
   }

   // -----------------------------------------------------
   /* (non-Javadoc)
    * @see ohd.hseb.util.AppsDefaultsDufas#getInt(java.lang.String, int)
    */
   public int getInt(String tokenName, int defaultValue)
   {
      int returnValue = defaultValue;

      try
      {
         String tokenValue = getToken(tokenName);
         if (tokenValue != null)
         {
            returnValue = Integer.parseInt(tokenValue);
         }
      } catch (Throwable e)
      {
         returnValue = defaultValue;
      }

      return returnValue;
   }
   
   public String getToken(String tokenName) {
       String token = (String)cache.get(tokenName);
       if (token == null) {
           token = getToken0(tokenName);
           if (token == null) {
               token = NULL;
           }
           cache.put(tokenName,token);
       } else {
           if (token == NULL) {
               token = null;
           }
       }
       if (token == null){
         _logger.fine("Null token for the token name, " + tokenName);
       }
       return token;
   }

   // -----------------------------------------------------

   /**
    * Returns the value found for the specified token name.
    * 
    * An attempt to resolve the token is made in the following order:
    * <ul>
    * <li>Check if a system property (nearly equivalent to a environment
    *  variable)
    * <li>Check if defined in "APPS_DEFAULTS_USER"
    * <li>Check if defined in "APPS_DEFAULTS_PROG"
    * <li>Check if defined in "APPS_DEFAULTS_SITE"
    * <li>Check if defined in "APPS_DEFAULTS"
    * </ul>
    * The search will terminate as soon as the token is found and the 
    * token value will be returned.
    * <p>
    * If the token is not found null will be returned.
    * 
    * @param tokenName Name of token
    * @return token value associated with token name, or null if the
    *  token was not found
    */
   public String getToken0(String tokenName)
   {
      String tokenValue = null;

      // System.out.println("recursion count = " + _recursionCount);

      // Attempt to get the value directly from the system environment.
      String envValue = null;
      if ( __jvmhas_getenv ) {
          envValue = System.getenv(tokenName);
      }
      else {
          envValue = NWSRFS_Util.getenv(tokenName);
      }
      
      // if token is available as an environment variable, use its value
      if (envValue != null)
      {
         tokenValue = envValue;
      } else
      // look for the token in each the files (if they are defined), until
      // find the token, then stop looking as soon as it is found
      {
         tokenValue = getTokenFromFile(tokenName, _appsDefaultsUserFilePath);
         if (tokenValue == null)
         {
            tokenValue = getTokenFromFile(tokenName, _appsDefaultsProgramFilePath);
            if (tokenValue == null)
            {
               tokenValue = getTokenFromFile(tokenName, _appsDefaultsSiteFilePath);
               if (tokenValue == null)
               {
                  tokenValue = getTokenFromFile(tokenName, _appsDefaultsNationalFilePath);
               }
            }
         }
      } // end else

      tokenValue = expandReferBacks(tokenValue);
      
      /* null is an acceptable value

      if (tokenValue.contains("null"))
      {
         String s = "AppsDefaultsKeyNotFound: The appsDefaults token key '"
              + tokenName
              + "' cound not be found.\n"
              + "      APPS_DEFAULTS_USER : " + _appsDefaultsUserFilePath
              + "      APPS_DEFAULTS_PROG : " + _appsDefaultsProgramFilePath
              + "      APPS_DEFAULTS_SITE : " + _appsDefaultsSiteFilePath
              + "      APPS_DEFAULTS_SITE : " + _appsDefaultsNationalFilePath;
               
         _logger.severe(s);

         throw new ICPRuntimeException(s);
      }
       */

      return tokenValue;

   } // end getToken()

  
   // --------------------------------------------------------------

   private String getTokenFromFile(String tokenName, String filePath)
   {
      String tokenValue = null;
      boolean tokenFound = false;
      String line = "";

      NameValuePair nameValuePair = null;

      if (filePath != null)
      {

         BufferedReader _reader = null;
         try
         {
            _reader = new BufferedReader(new FileReader(filePath));
         } catch (FileNotFoundException e)
         {
            _logger.info("The AppsDefaults file '" + filePath + "' was not found.");
            return null;
         }

         while ((!tokenFound) && (line != null))
         {
            line = getLine(_reader);
            if (line == null)
            {
               break;
            }
            nameValuePair = parseLine(line);

            if (nameValuePair != null)
            {
               // System.out.println("nameValuePair.getName() = |" +
               // nameValuePair.getName() + "|" );

               // System.out.println("nameValuePair.getValue() = |" +
               // nameValuePair.getValue() + "|" );

               if ((nameValuePair.getName().equals(tokenName)))

               {
                  tokenFound = true;
                  tokenValue = nameValuePair.getValue();
               }
            }
         }

         try
         {
            _reader.close();
         } catch (IOException e)
         {
            throw new RuntimeException(e);
         }

      }
      return tokenValue;
   }

   // -----------------------------------------------------
   private String expandReferBacks(String tokenValue)
   {
      if (tokenValue != null)
      {
         while (thereAreReferBacks(tokenValue))
         {
            // System.out.println("tokenValue before expandFirstReferBack = " +
            // tokenValue);
            tokenValue = expandFirstReferBack(tokenValue);
            // System.out.println("tokenValue after expandFirstReferBack = " +
            // tokenValue);
         }
      }

      return tokenValue;
   }

   // -----------------------------------------------------

   private boolean thereAreReferBacks(String tokenValue)
   {
      boolean result = false;

      if (tokenValue.indexOf(RFR_OPEN) > -1)
      {
         result = true;
      }

      return result;
   }

   // -----------------------------------------------------

   private String expandFirstReferBack(String tokenValue)
   {

      int referBackStartIndex = tokenValue.indexOf(RFR_OPEN);
      int referBackEndIndex = tokenValue.indexOf(RFR_CLOSE);
      String beginning = "";
      String middle = null;
      String newTokenName = null;
      String end = "";

      if ((referBackStartIndex > -1) && (referBackEndIndex > -1))
      {
         if (referBackStartIndex > 0)
         {
            beginning = tokenValue.substring(0, referBackStartIndex);
         }

         newTokenName = tokenValue.substring(referBackStartIndex + RFR_OPEN.length(), referBackEndIndex);

         _recursionCount++;

         if (_recursionCount <= RECUR_LIMIT)
         {
            middle = getToken(newTokenName);
            _recursionCount--;
         } else
         {
            middle = "ERROR_ERROR_ERROR";
            System.err.println("You probably have a cycle in your Apps Defaults File's  refer backs, please check it");
         }
         if ((referBackEndIndex + RFR_CLOSE.length()) < tokenValue.length())
         {
            end = tokenValue.substring(referBackEndIndex + RFR_CLOSE.length(), tokenValue.length());
         }

         tokenValue = beginning + middle + end;
      }

      return tokenValue;
   }

   // -----------------------------------------------------
   private String getLine(BufferedReader _reader)
   {
      String line = null;

      try
      {
         line = _reader.readLine();
      } catch (IOException e)
      {
         throw new RuntimeException(e);
      }

      // System.out.println("line = " + line);

      return line;
   }

   // -----------------------------------------------------
   private NameValuePair parseLine(String line)
   {
      NameValuePair pair = null;
      int delimiterIndex = -1;

      String tokenName = null;
      String tokenValue = null;

      // find delimiter

      delimiterIndex = line.indexOf(DELIM);

      if (delimiterIndex > -1) // there is a delimiter character on the line
      {
         // find tokenName
         tokenName = findTokenName(line, delimiterIndex);

         if (tokenName != null)
         {
            tokenValue = findTokenValue(line, delimiterIndex);
            if (tokenValue != null)
            {
               pair = new NameValuePair(tokenName, tokenValue);
            }
         }
      } // end if found a delimiter
      else
      // there is no delimiter, so can't read a pair from this line
      {
         pair = null;
      }

      return pair;
   }

   // -----------------------------------------------------
   private String findTokenName(String line, int delimiterIndex)
   {
      String tokenName = null;
      boolean foundTokenName = false;
      boolean foundStartOfTokenName = false;
      boolean foundComment = false;
      StringBuffer tokenNameBuffer = new StringBuffer();

      for (int i = 0; ((i < delimiterIndex) && (!foundTokenName)) && (!foundComment); i++)
      {
         char c = line.charAt(i);
         if (isWhiteSpace(c))
         {
            // check to see if this is white space at the beginning or the end
            // of the tokenName
            if (!foundStartOfTokenName) // this must beginning whitespace
            {
               // so ignore the whitespace
            } else
            // must be trailing whitespace
            {
               // the token is done;
               tokenName = tokenNameBuffer.toString();
               foundTokenName = true;
            }
         } // end if isWhiteSpace

         else if (isCommentStarter(c))
         {
            // There can't be a valid tokenName, tokenValue pair here, then
            foundComment = true;

            // clear out the tokenNameVariables
            tokenName = null;

            // works in >= java 1.2,
            // tokenNameBuffer.delete(0, tokenNameBuffer.length());

            // works in java < 1.2, but the previous line is prefered
            tokenNameBuffer = new StringBuffer();

            foundStartOfTokenName = false;
            break; // exit loop

         } // end isCommentStarter

         else
         // part of the tokenName
         {
            tokenNameBuffer.append(c);
            foundStartOfTokenName = true;
         }

      } // end for

      if (foundStartOfTokenName)
      {
         tokenName = tokenNameBuffer.toString();
      }

      return tokenName;
   }

   // ----------------------------------------------------------------------

   private String findTokenValue(String line, int delimiterIndex)
   {
      String tokenValue = null;

      boolean foundTokenValue = false;
      boolean foundStartOfTokenValue = false;
      boolean foundComment = false;

      boolean foundSingleOpenQuote = false;
      boolean foundSingleCloseQuote = false;

      boolean foundDoubleOpenQuote = false;
      boolean foundDoubleCloseQuote = false;

      boolean error = false;

      StringBuffer tokenValueBuffer = new StringBuffer();

      for (int i = delimiterIndex + 1; ((i < line.length()) && (!foundTokenValue)) && (!foundComment); i++)
      {
         char c = line.charAt(i);
         if (isWhiteSpace(c))
         {
            // check to see if this is white space at the beginning or the end
            // of the tokenValue
            if (!foundStartOfTokenValue) // this must be beginning whitespace
            {
               // so ignore the whitespace
            } else if ((foundSingleOpenQuote) && (!foundSingleCloseQuote))
            {
               tokenValueBuffer.append(c);
               foundStartOfTokenValue = true;
            } else if ((foundDoubleOpenQuote) && (!foundDoubleCloseQuote))
            {
               tokenValueBuffer.append(c);
               foundStartOfTokenValue = true;
            } else
            // must be trailing whitespace
            {
               // the token value reading is done;
               tokenValue = tokenValueBuffer.toString();
               foundTokenValue = true;
            }
         } // end if isWhiteSpace

         else if (isCommentStarter(c))
         {
            if (foundStartOfTokenValue)
            {
               // this character is allowed in a tokenValue
               tokenValueBuffer.append(c);
            } else
            { // error, there can't be a valid tokenValue
               foundComment = true;

               // clear out the tokenNameVariables
               tokenValue = null;

               // works in >= java 1.2,
               // tokenValueBuffer.delete(0, tokenValueBuffer.length());

               // works in java < 1.2, but the previous line is prefered
               tokenValueBuffer = new StringBuffer();

               error = true;
            }
         } // end isCommentStarter

         else if (isDelimiter(c))
         {
            if (foundStartOfTokenValue)
            {
               // this character is allowed in a tokenValue
               tokenValueBuffer.append(c);
            } else
            { // error, there can't be a valid tokenValue

               // clear out the tokenNameVariables
               tokenValue = null;

               // works in >= java 1.2,
               // tokenValueBuffer.delete(0, tokenValueBuffer.length());

               // works in java < 1.2, but the previous line is prefered
               tokenValueBuffer = new StringBuffer();

               error = true;
               break; // exit loop
            }

         } else if (isSingleQuote(c))
         {
            if (foundSingleOpenQuote)
            {
               foundSingleCloseQuote = true;
               foundTokenValue = true; // done
            } else
            {
               foundSingleOpenQuote = true;
            }
         }

         else if (isDoubleQuote(c))
         {
            if (foundDoubleOpenQuote)
            {
               foundDoubleCloseQuote = true;
               foundTokenValue = true; // done
            } else
            {
               foundDoubleOpenQuote = true;
            }
         }

         else
         // part of the tokenValue
         {
            tokenValueBuffer.append(c);
            // System.out.println("tokenValueBuffer =" +
            // tokenValueBuffer.toString());
            foundStartOfTokenValue = true;
         }

      } // end for

      if ((foundStartOfTokenValue) && (!error))
      {
         tokenValue = tokenValueBuffer.toString();
      }

      return tokenValue;
   }

   // -----------------------------------------------------

   // -----------------------------------------------------

   //
   private static boolean isWhiteSpace(char c)
   {
      boolean result = false;

      if ((c == ' ') || (c == '\t'))
      {
         result = true;
      }

      return result;

   } // isWhiteSpace

   // -----------------------------------------------------
   private static boolean isCommentStarter(char c)
   {
      boolean result = false;

      if (c == COMMENT)
      {
         result = true;
      }

      return result;
   } // isCommentStarter

   // -------------------------------------------------------------
   private static boolean isDelimiter(char c)
   {
      boolean result = false;

      if (c == DELIM)
      {
         result = true;

      }

      return result;
   } // isDelimiter

   // -----------------------------------------------------------

   private static boolean isSingleQuote(char c)
   {
      boolean result = false;

      if (c == SINGLE_QUOTE)
      {
         result = true;
      }

      return result;
   } // isSingleQuote

   // -----------------------------------------------------

   private static boolean isDoubleQuote(char c)
   {
      boolean result = false;

      if (c == DOUBLE_QUOTE)
      {
         result = true;
      }

      return result;

   } // isDoubleQuote

   // -----------------------------------------------------
   public static void main(String[] args)
   {
      AppsDefaults ad = new AppsDefaults();
      String tokenName = null;
      String tokenValue = null;

      // tokenName = "shef_procobs";
      // tokenValue = ad.getToken(tokenName);

      // System.out.println("tokenName = " + tokenName +
      // " tokenValue = " + tokenValue);

      System.setProperty("ffg_out_dir", "my_fgg_out_dir");
      System.setProperty("testToken1","testToken1_value");
      System.setProperty("testToken2","testToken2_value");
      System.setProperty("testToken3","testToken3_value");
      
      // Eclipse: set the VM arg: -DmyEnvVar=value
      tokenName = "myEnvVar";
      tokenValue = ad.getToken(tokenName);
      System.out.println("tokenName = " + tokenName + " tokenValue = " + tokenValue);
      
      tokenName = "ffg_out_dir";
      tokenValue = ad.getToken(tokenName);

      System.out.println("tokenName = " + tokenName + " tokenValue = " + tokenValue);

      tokenName = "testToken1";
      // tokenRawValue = "#hank:me:'llhank' "
      tokenValue = ad.getToken(tokenName);
      System.out.println("tokenName = " + tokenName + " tokenValue = " + tokenValue);

      tokenName = "testToken2";
      // tokenRawValue = "dd#hank:me:'llhank' "
      tokenValue = ad.getToken(tokenName);
      System.out.println("tokenName = " + tokenName + " tokenValue = " + tokenValue);

      tokenName = "testToken3";
      // tokenRawValue = 'dd#hank:me:"llhank" '
      tokenValue = ad.getToken(tokenName);
      System.out.println("tokenName = " + tokenName + " tokenValue = " + tokenValue);      
   }
} // end class AppsDefaults
