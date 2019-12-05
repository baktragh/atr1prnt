package atr1prnt.fs;

import atr1prnt.AtrChecker;
import atr1prnt.AtrFile;
import atr1prnt.DumpUtilities;
import atr1prnt.SummaryReport;
import java.io.PrintStream;
import java.util.Properties;


/**
 * 
 * No file system checker
 */
public class NoFSChecker implements AtrChecker {

    @Override
    public void check(AtrFile atrFile, PrintStream pr, Properties props,SummaryReport sumReport,DumpUtilities utils) {
        utils.printHeader(pr,"NO FILESYSTEM", '=', true, true);
    }

    @Override
    public String getSectionName() {
        return "NO FILESYSTEM";
    }
    
}
