
### Line Count (Java)

by: Keith Fenske, https://kwfenske.github.io/

LineCount is a Java 1.4 console application to count the number of characters
and lines in a plain text file such as source code. Put one or more file names
on the command line:

	java  LineCount1  x.txt

Text lines may end with a carriage return (CR or 0x0D), a line feed (LF or
0x0A), or CR followed by LF. Having LF by itself is more commonly called a
"newline" character or NL. If a file has a different character set than your
system's default encoding, you should put the name of a character set as the
"-e" option. For example, UTF-8 is very popular with web pages:

	java  LineCount1  -eUTF-8  x.txt

There is no graphical interface (GUI) for this program; it must be run from a
command prompt, command shell, or terminal window. See the "-?" option for a
help summary.

Download the ZIP file here: https://kwfenske.github.io/line-count-java.zip

Released under the terms and conditions of the Apache License (version 2.0 or
later) and/or the GNU General Public License (GPL, version 2 or later).
