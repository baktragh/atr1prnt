package atr1prnt;

import atr1prnt.fs.Dos2Checker;
import atr1prnt.fs.NoFSChecker;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class Atr1prnt {

    public static void main(String[] args) {
        
        if (args.length<1) {
            printUsage();
            return;
        }
        
        AtrFile atrFile=null;
        
        try {
            atrFile = AtrFile.getFromPathName(args[0]);
        }
        catch (AtrException ae) {
            System.err.println(ae.getMessage());
        }
        catch (IOException ioe) {
            System.err.println(ioe.getClass().getName());
            ioe.printStackTrace(System.err);
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        if (atrFile==null) {
            System.exit(-1);
        }
        
        Properties runProperties = new Properties();
        
        for (int i=1;i<args.length;i++) {
            String arg = args[i].trim().toUpperCase();
            if (!arg.isEmpty()) {
                runProperties.put(args[i], true);
            }
        }
        
        /*Check if only one file system has been selected*/
        List<String> fsPropList = getPropertiesStartingWith("FS-", runProperties);
        
        if (fsPropList.size()>1) {
            System.err.println("ERROR:  More than one filesystem specified");
            System.exit(-1);
        }
        
        /*Prepare summary report*/
        SummaryReport sr = new SummaryReport();
        PrintStream pr = System.out;
        
        if (runProperties.containsKey("SILENT")) {
            pr = new PrintStream(new NullOutputStream());
        }
        
        
        /*Run header checker*/
        AtrHeaderChecker  ahc = new AtrHeaderChecker();
        printSectionStart(pr, ahc.getSectionName());
        ahc.check(atrFile,pr,runProperties,sr);
        
        /*Run sector checker*/
        SectorChecker sc = new SectorChecker();
        printSectionStart(pr, sc.getSectionName());
        sc.check(atrFile,pr,runProperties,sr);
        
        /*Run boot checker*/
        BootChecker bc = new BootChecker();
        printSectionStart(pr,bc.getSectionName());
        bc.check(atrFile,pr,runProperties,sr);
        
        /*Decide which file system checker to run*/
        AtrChecker fsChecker = new NoFSChecker();
        
        /*If no file system specified or DOS2 then run DOS2 checker*/
        if (fsPropList.isEmpty() || runProperties.contains("FS-DOS2")) {
            fsChecker= new Dos2Checker();
            
        }
        else if (runProperties.contains("FS-NONE")) {
            fsChecker = new NoFSChecker();
        }
        
        /*Run the file system check*/
        printSectionStart(pr,fsChecker.getSectionName());
        fsChecker.check(atrFile, pr, runProperties,sr);
        
        
        /*Print summary if requested. Always to system out*/
        if (runProperties.containsKey("SUMMARY")) {
            printSectionStart(System.out,"SUMMARY REPORT");
            sr.printSummary(System.out);
        }
        
        /*Return return code*/
        Severity maxSeverity = sr.getMaxSeverity();
        int retCode = 0;
        
        if (maxSeverity.isGreaterThan(Severity.SEV_INFO)) {
            retCode = -1;
        }
        
        System.exit(retCode);
        
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar atr1prnt.jar <disk_image> [options]");
        System.out.println("atr1prnt 0.1 - Print and verify contents of ATR disk images");
        System.out.println("(c) 2019 BAHA Software");
        System.out.println();
        System.out.println("General options: ");
        System.out.println("NOSECTORS - Skip sector dump");
        System.out.println("NOBOOT    - Skip boot sector dump");
        System.out.println("SILENT    - Silent run with no output");
        System.out.println("SUMMARY   - Display summary report");
        System.out.println();
        System.out.println("File system choice: ");
        System.out.println("FS-DOS2   - DOS 2 filesystem dump (default)");
        System.out.println("FS-NONE   - No filesystem dump");
        System.out.println();
        System.out.println("File system options: ");
        System.out.println("DOS2-BITMAP - Include dump of DOS2 bitmap");
        System.out.println("DOS2-DUMPFILES - Include dump of DOS2 bitmap");

    }
    
    private static void printSectionStart(PrintStream ps,String sectionTitle) {
        ps.println("========================================================================");
        ps.println("SECTION: "+sectionTitle);
        ps.println("========================================================================");
    }
    
    private static List<String> getPropertiesStartingWith(String start,Properties props) {
        
        ArrayList<String> matchingProps = new ArrayList<>();
        
        Set<Object> keys = props.keySet();
        Iterator it = keys.iterator();
        while(it.hasNext()) {
            String prop = (String)it.next();
            if (prop.startsWith(start)) matchingProps.add(prop);
        }
        return matchingProps;
    }
    
    
    private static class NullOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            
        }
        
    }
    
}
