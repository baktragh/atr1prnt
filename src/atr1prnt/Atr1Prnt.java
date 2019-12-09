package atr1prnt;

import atr1prnt.fs.Dos2Checker;
import atr1prnt.fs.NoFSChecker;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class Atr1Prnt {

    private String fullPath;
    private Properties runProperties;
    
    private PrintStream outStream;
    private PrintStream errStream;
    private PrintStream summaryStream;
    private SummaryReport summaryReport;

    public static final int RC_OK = 0;
    public static final int RC_ERROR = -1;
    

    private Atr1Prnt() {

    }

    private Atr1Prnt(String fullPath, Properties runProperties, PrintStream out, PrintStream err,SummaryReport sr) {

        this.fullPath = fullPath;
        this.runProperties = runProperties;
        this.outStream = out;
        this.errStream = err;
        this.summaryReport = sr;
    }

    public int dump() throws Exception {

        AtrFile atrFile = AtrFile.getFromPathName(fullPath,runProperties);
        
        /*Get properties related to file system selection*/
        List<String> fsPropList = getPropertiesStartingWith("FS-", runProperties);
        
        /*Decide which file system checker to run*/
        AtrChecker fsChecker;
        
        /*If no file system specified or DOS2 then run DOS2 checker*/
        if (fsPropList.isEmpty() || runProperties.containsKey("FS-DOS2")) {
            fsChecker = new Dos2Checker(Dos2Checker.VTOC_DOS2);

        }
        else if (runProperties.containsKey("FS-DOSIIP")) {
            fsChecker = new Dos2Checker(Dos2Checker.VTOC_DOSIIP);
        }
        else if (runProperties.containsKey("FS-NONE")) {
            fsChecker = new NoFSChecker();
        }
        else {
            errStream.println("Unknown file system was specified: " + fsPropList.get(0));
            return RC_ERROR;
        }

        /*Prepare summary report*/
        
        PrintStream pr = outStream;
        DumpUtilities dumpUtils = new DumpUtilities();
        
        /*Run header checker*/
        AtrHeaderChecker ahc = new AtrHeaderChecker();
        ahc.check(atrFile, pr, runProperties, summaryReport, dumpUtils);

        /*Run boot checker*/
        BootChecker bc = new BootChecker();
        bc.check(atrFile, pr, runProperties, summaryReport, dumpUtils);

        /*Run the file system check*/
        fsChecker.check(atrFile, pr, runProperties, summaryReport, dumpUtils);

        /*Run sector checker/dumper*/
        SectorChecker sc = new SectorChecker();
        sc.check(atrFile, pr, runProperties, summaryReport, dumpUtils);

       

        /*Return return code*/
        Severity maxSeverity = summaryReport.getMaxSeverity();
        int retCode = 0;

        if (maxSeverity.isGreaterThan(Severity.SEV_INFO)) {
            retCode = -1;
        }

        return retCode;

    }

    public static Atr1Prnt getInstance(String fullPath, Properties runProperties, PrintStream out, PrintStream err,SummaryReport sr) {
        return new Atr1Prnt(fullPath, runProperties, out, err,sr);
    }

   
    
    private List<String> getPropertiesStartingWith(String start, Properties props) {

        ArrayList<String> matchingProps = new ArrayList<>();

        Set<Object> keys = props.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            String prop = (String) it.next();
            if (prop.startsWith(start)) {
                matchingProps.add(prop);
            }
        }
        return matchingProps;
    }

}
