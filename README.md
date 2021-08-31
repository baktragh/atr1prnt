# ATR1PRNT
ATR Disk Image Dumping Tool

ATR1PRNT is a utility that allows you to dump ATR disk images. The tool can
be used for individual executions or embedded in scripts. ATR1PRNT is written in the Java programming language and requires Java Runtime Environment or Java Development Kit 8 or newer.

# Usage
    java -jar atr1prnt.jar <atr_file> [options]

# Example
    java -jar atr1prnt.jar TEST.ATR NOBOOT FS-DOS2 SUMMARY SECTORS

# Return codes

*  0 - Everything OK
* -1 - Error(s) found

# Dumps

## General Dumps

* ATR header dump
* Full sector dump
* Boot information dump

## FMS Specific Dumps

### Atari DOS 2 and DOS II+ file systems

 * Directory dump
 * VTOC and Bitmap dump and consistency check
 * File system integrity check
 
The file system integrity check includes the following:

 * Sector link to nonexistent sector
 * Loop in sector chain
 * Sector assigned to multiple directory entries
 * Sector does not belong to the assigned directory entry
 * Number of valid bytes in the sector
 * Identify contiguous and non-contiguous files

### No file system dump

 * Skips the file system dump/check

# Options

## General Options

* SECTORS - Perform full sector dump
* NOBOOT - Skip boot information dump
* SILENT - Silent run with no output
* SUMMARY - Display summary report (even when SILENT is specified)
* DUMPFILES - File system checks will dump contents of files
* ERASED - File system checks will process directory entries flagged as erased

## File System Choice

* FS-DOS2 - Atari DOS 2 fully compatible (default). DOS 2.0, DOS 2.5, XDOS.
* FS-DOSIIP - DOS II+ (by S. Dorndorf)
* FS-NONE - No file system

## Special Options

* BOOT256 - Assume the first three sectors hold 256 bytes instead of 128

# Wildcarding

ATR1PRNT has limited support for wildcarded atr_file specification. The file
name (but not any of its parent directories) can be wildcarded with * (asterisk) and ? (question mark).
This way, you can process multiple ATR disk images.

ATR1PRNT expands the wildcards. If you are running ATR1PRNT from a shell that
expands the wildcarding characters before running the program,
use appropriate delimiters to prevent the shell from expanding the wildcards.

Note that all disk images processed with wildcarding share the same options.
Summary reports, if requested, all always present at the very end of the output.


## Example

    java -jar atr1prnt.jar "C:\UTILS\A8\DISK\TEST*.ATR" FS-DOS2 SUMMARY NOSECTORS
