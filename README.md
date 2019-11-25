# ATR1PRNT
ATR Disk Image Dumping Tool

ATR1PRNT is a utility that allows you to dump ATR files. The tool can
be used for individual executions or embedded in scripts.

# Usage
    java -jar atr1prnt.jar <atr_file> [options]

# Example
    java -jar atr1prnt.jar TEST.ATR FS-DOS2 NOBOOT FS-DOS2 SUMMARY NOSECTORS

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
* DUMPFILES - File system check will dump contents of files

## File System Choice

* FS-DOS2 - Atari DOS 2 fully compatible (default). DOS 2.0, DOS 2.5, XDOS.
* FS-DOSIIP - DOS II+ (by S. Dorndorf)
* FS-NONE - No file system
