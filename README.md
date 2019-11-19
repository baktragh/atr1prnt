# ATR1PRNT
ATR Disk Image Dumping Tool

ATR1PRNT is a utility that allows you to dump ATR files.

# Usage
    java -jar atr1prnt.jar <atr_file> [options]

# Example
    java -jar atr1prnt.jar TEST.ATR FS-DOS2 NOBOOT DOS2-BITMAP

# Dumps

## General Dumps

* ATR header dump
* Full sector dump
* Boot information dump

## FMS Specific Dumps

### DOS 2 Compatible file system dump with support for medium density

 * Directory dump
 * VTOC and Bitmap dump
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

## File System Choice

* FS-DOS2 - DOS 2 compatible (default)
* FS-NONE - No file system

## DOS2 Options

* DOS2-BITMAP - Include DOS 2 bitmap dump
* DOS2-DUMPFILES - Dump contents of files