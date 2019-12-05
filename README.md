# ATR1PRNT
ATR Disk Image Dumping Tool

ATR1PRNT is a utility that allows you to dump ATR disk images. The tool can
be used for individual executions or embedded in scripts. ATR1PRNT is written in the Java programming language and requires Java Runtime Environment or Java Development Kit 8 or newer.

# Usage
    java -jar atr1prnt.jar <atr_file> [options]

# Example
    java -jar atr1prnt.jar TEST.ATR NOBOOT FS-DOS2 SUMMARY NOSECTORS

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

* NOSECTORS - Skip sector dump
* NOBOOT - Skip boot information dump
* SILENT - Silent run with no output
* SUMMARY - Display summary report (even when SILENT is specified)
* DUMPFILES - File system checks will dump contents of files
* NOERASED - File system checks will skip erased directory entries

## File System Choice

* FS-DOS2 - Atari DOS 2 fully compatible (default). DOS 2.0, DOS 2.5, XDOS.
* FS-DOSIIP - DOS II+ (by S. Dorndorf)
* FS-NONE - No file system
