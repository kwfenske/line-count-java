/*
  Line Count #1 - Count Number of Characters and Lines in Text File
  Written by: Keith Fenske, http://kwfenske.github.io/
  Friday, 16 August 2024
  Java class name: LineCount1
  Copyright (c) 2024 by Keith Fenske.  Apache License or GNU GPL.

  This is a Java 1.4 console application to count the number of characters and
  lines in a plain text file such as source code.  Put one or more file names
  on the command line:

    java  LineCount1  x.txt

  Text lines may end with a carriage return (CR or 0x0D), a line feed (LF or
  0x0A), or CR followed by LF.  Having LF by itself is more commonly called a
  "newline" character or NL.  If a file has a different character set than your
  system's default encoding, you should put the name of a character set as the
  "-e" option.  For example, UTF-8 is very popular with web pages:

    java  LineCount1  -eUTF-8  x.txt

  There is no graphical interface (GUI) for this program; it must be run from a
  command prompt, command shell, or terminal window.  See the "-?" option for a
  help summary.

  Apache License or GNU General Public License
  --------------------------------------------
  LineCount1 is free software and has been released under the terms and
  conditions of the Apache License (version 2.0 or later) and/or the GNU
  General Public License (GPL, version 2 or later).  This program is
  distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY,
  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE.  See the license(s) for more details.  You should have
  received a copy of the licenses along with this program.  If not, see the
  http://www.apache.org/licenses/ and http://www.gnu.org/licenses/ web pages.

  Restrictions and Limitations
  ----------------------------
  Characters above U+FFFF in the Unicode standard will be counted twice,
  because they appear in Java as a pair of UTF-16 values.
*/

import java.io.*;                 // standard I/O
import java.text.*;               // number formatting

public class LineCount1
{
  /* constants */

  static final char CHAR_CR = '\r'; // carriage return (CR) character
  static final char CHAR_LF = '\n'; // line feed (LF), newline (NL)
  static final char CHAR_SP = ' '; // standard blank space character
  static final String COPYRIGHT_NOTICE =
    "Copyright (c) 2024 by Keith Fenske.  Apache License or GNU GPL.";
  static final int EXIT_FAILURE = -1; // incorrect request or errors found
  static final int EXIT_SUCCESS = 1; // request completed successfully
  static final int EXIT_UNKNOWN = 0; // don't know or nothing really done
  static final int MAX_FORMAT = 999; // maximum number of message formats
  static final String PROGRAM_TITLE =
    "Count Number of Characters and Lines in Text File - by: Keith Fenske";

  /* class variables */

  static String encodeName;       // character set name or text encoding
  static NumberFormat formatComma; // formats with commas (digit grouping)
  static NumberFormat formatZeros; // formats with many leading zeros
  static boolean mswinFlag;       // true if running on Microsoft Windows
  static int outputStyle;         // output message format, arbitrary index

/*
  main() method

  We run as a console application.  There is no graphical interface.
*/
  public static void main(String[] args)
  {
    int fileCount;                // number of files given on command line
    int i;                        // index variable
    String word;                  // one parameter from command line

    /* Initialize variables used by both console and GUI applications. */

    encodeName = null;            // by default, use local encoding
    fileCount = 0;                // no files found yet
    formatComma = NumberFormat.getInstance(); // current locale
    formatComma.setGroupingUsed(true); // use commas or digit groups
    formatZeros = NumberFormat.getInstance(); // format with leading zeros
    formatZeros.setGroupingUsed(true); // may or may not be necessary
    formatZeros.setMinimumIntegerDigits(12); // <long> has up to 19 digits
    mswinFlag = System.getProperty("os.name").startsWith("Windows");
    outputStyle = 0;              // assume zero means default format

    /* Check command-line parameters for options. */

    for (i = 0; i < args.length; i ++)
    {
      word = args[i].toLowerCase(); // easier to process if consistent case
      if (word.length() == 0)
      {
        /* Ignore empty parameters, which are more common than you might think,
        when programs are being run from inside scripts (command files). */
      }

      else if (word.equals("?") || word.equals("-?") || word.equals("/?")
        || word.equals("-h") || (mswinFlag && word.equals("/h"))
        || word.equals("-help") || (mswinFlag && word.equals("/help")))
      {
        showHelp();               // show help summary
        System.exit(EXIT_UNKNOWN); // exit application after printing help
      }

      else if (word.startsWith("-e") || (mswinFlag && word.startsWith("/e")))
      {
        encodeName = args[i].substring(2).trim(); // get character set name
        if (encodeName.length() == 0)
          encodeName = null;      // default back to local system
      }

      else if (word.startsWith("-m") || (mswinFlag && word.startsWith("/m")))
      {
        try { outputStyle = Integer.parseInt(word.substring(2)); }
        catch (NumberFormatException nfe) { outputStyle = -1; }
        if ((outputStyle < 0) || (outputStyle > MAX_FORMAT))
        {
          System.err.println("Output message format must be index from 0 to "
            + MAX_FORMAT + ": " + args[i]);
          showHelp();
          System.exit(EXIT_FAILURE);
        }
      }

      else if (word.startsWith("-") || (mswinFlag && word.startsWith("/")))
      {
        System.err.println("Option not recognized: " + args[i]);
        showHelp();
        System.exit(EXIT_FAILURE);
      }

      else                        // assume this is a file name
      {
        fileCount ++;             // one more file found
        processFile(args[i]);     // easier to read if method called
      }
    }

    if (fileCount == 0)
    {
      System.err.println("Missing file name(s) on command line.");
      showHelp();
      System.exit(EXIT_FAILURE);
    }
    System.exit(EXIT_SUCCESS);    // if we get here, everything is good

  } // end of main() method


/*
  padLeft() method

  Insert leading spaces on the left of a string until it has the desired width,
  measured in characters.
*/
  static String padLeft(String input, int width)
  {
    StringBuffer buffer;          // faster than String for multiple appends
    int fillCount;                // number of spaces to add
    int i;                        // index variable
    String result;                // our modified string

    fillCount = width - input.length(); // spaces to add, may be negative
    if (fillCount <= 0)           // already at correct size or bigger?
      result = input;             // yes, return input text unchanged
    else                          // need to insert leading spaces (on left)
    {
      buffer = new StringBuffer(width); // empty string buffer for result
      for (i = 0; i < fillCount; i ++)
        buffer.append(CHAR_SP);   // insert one blank space character
      buffer.append(input);       // end with caller's text
      result = buffer.toString(); // change buffer back to a string
    }
    return(result);               // give caller our converted string

  } // end of padLeft() method


/*
  processFile() method

  Count the number of characters and lines in a given text file.

  How many lines are in an empty file (zero bytes)?  If you open a new file in
  a text editor, the cursor is sitting on the first line, but does that count
  as having one line?  Now apply the same logic to the end of a file.
*/
  static void processFile(String givenName)
  {
    File canonFile;               // fully-resolved File object
    int ch;                       // current input character as an integer
    long fileChars;               // number of characters in this file
    long fileLines;               // number of lines in this file
    String fileName;              // exactly correct file name (no path)
    String filePath;              // exactly correct file path (with name)
    boolean foundCr;              // found carriage return (CR), waiting for LF
    BufferedReader inputStream;   // input stream of characters
    long lineChars;               // number of characters on this line

    try                           // allow for I/O errors
    {
      /* While we don't have to, get the officially correct name and path for
      this file ... if it really is a file. */

      canonFile = (new File(givenName)).getCanonicalFile(); // may fail
      if (canonFile.isFile() == false) // need a normal file that exists
      {
        System.err.println(givenName + " - not a file");
//      System.exit(EXIT_FAILURE);
        return;
      }
      fileName = canonFile.getName(); // now absolute and unique
      filePath = canonFile.getPath(); // path includes file name

      /* Open the file for reading, maybe with a given character set. */

      if (encodeName == null)     // use local system's encoding?
      {
        inputStream = new BufferedReader(new FileReader(canonFile));
      }
      else                        // given character set name
      {
        inputStream = new BufferedReader(new InputStreamReader(new
          FileInputStream(canonFile), encodeName));
      }

      /* Read one character at a time, recognizing CR/LF pairs as a combined
      newline separator or terminator. */

      fileChars = fileLines = lineChars = 0; // clear totals
      foundCr = false;            // no pending carriage return
      while ((ch = inputStream.read()) >= 0) // read until end-of-file (-1)
      {
        fileChars ++;             // count all characters, including newlines
        if (ch == CHAR_CR)        // carriage return (CR)?
        {
          fileLines ++;           // one more line
          foundCr = true;         // CR may be followed by LF
          lineChars = 0;          // start next line as empty
        }
        else if (ch != CHAR_LF)   // everything except line feed (LF)
        {
          foundCr = false;        // not a carriage return
          lineChars ++;           // one more character on this line
        }
        else if (foundCr)         // does this LF follow CR?
        {
          foundCr = false;        // not a carriage return
          lineChars = 0;          // start next line as empty
        }
        else                      // LF without CR (newline character)
        {
          fileLines ++;           // one more line
          foundCr = false;        // not a carriage return
          lineChars = 0;          // start next line as empty
        }
      }

      /* Close the input file and print a summary. */

      inputStream.close();        // try to close input file
      if (lineChars > 0)          // last line counts if not empty
        fileLines ++;             // one more line
      switch (outputStyle)        // index is arbitrary and customized
      {
        case (1):                 // better for sorting (Linux columns)
          System.out.println(fileLines + "  " + fileChars + "  " + fileName);
          break;
        case (2):                 // better for sorting (Windows literal)
//        System.out.printf("%,15d  %,15d  %s%n", fileLines, fileChars,
//          fileName);            // Java 5.0 or later
          System.out.println(padLeft(formatComma.format(fileLines), 15) + "  "
            + padLeft(formatComma.format(fileChars), 15) + "  " + fileName);
          break;
        case (3):                 // for mind-numbing moronic sorts
          System.out.println(formatZeros.format(fileLines) + "  "
            + formatZeros.format(fileChars) + "  " + filePath);
          break;
        default:                  // also assumed to have index zero
          System.out.println(fileName + " has " + formatComma.format(fileChars)
            + " characters in " + formatComma.format(fileLines) + " lines.");
      }
    }
    catch (UnsupportedEncodingException uee) // subclass of IOException
    {
      System.err.println(givenName + " - unknown character set: "
        + encodeName);
      System.exit(EXIT_FAILURE);
    }
    catch (IOException ioe)       // all other file I/O errors
    {
      System.err.println(givenName + " - " + ioe.getMessage());
//    System.exit(EXIT_FAILURE);
    }
  } // end of processFile() method


/*
  showHelp() method

  Show the help summary.  This is a UNIX standard and is expected for all
  console applications, even very simple ones.
*/
  static void showHelp()
  {
    System.err.println();
    System.err.println(PROGRAM_TITLE);
    System.err.println();
    System.err.println("This is a Java console application to count the number of characters and lines");
    System.err.println("in a plain text file such as source code.");
    System.err.println();
    System.err.println("  java  LineCount1  [options]  fileNames");
    System.err.println();
    System.err.println("You may give options on the command line:");
    System.err.println();
    System.err.println("  -? = -help = show summary of command-line syntax");
    System.err.println("  -e# = character set name or text encoding; default is local system;");
    System.err.println("      example: -eUTF-8");
    System.err.println("  -m# = output message format, arbitrary index from 0 to "
      + MAX_FORMAT + " (customized)");
    System.err.println();
    System.err.println(COPYRIGHT_NOTICE);
//  System.err.println();

  } // end of showHelp() method

} // end of LineCount1 class

/* Copyright (c) 2024 by Keith Fenske.  Apache License or GNU GPL. */
