# ATR1PRNT
ATR Disk Image Dumping Tool

ATR1PRNT is a utility that allows you to dump ATR files.

#Usage
    java -jar atr1prnt.jar <atr_file> [options]

#Example
    java -jar atr1prnt.jar TEST.ATR FS-DOS2 NOBOOT DOS2-BITMAP


# Dumps

## General dumps

* ATR header dump
* Full sector dump
* Boot information dump

## FMS Specific dumps

* DOS 2 Compatible file system dump (directory, bitmaps, file chain tracking) with support for medium density
* No file system dump (effectively skips file system dump)
* Possibly More in the future (MyDOS, SpartaDOS)

# Options

## General Options

* NOSECTORS - Skip sector dump
* NOBOOT - Skip boot information dump

## File system choice

* FS-DOS2 - DOS 2 compatible (default)
* FS-NONE - No file system

## DOS2 options

* DOS2-BITMAP - Include DOS 2 bitmap dump