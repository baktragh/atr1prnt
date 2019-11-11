package atr1prnt;

import java.io.IOException;
import java.io.PrintStream;

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
        
        /*Run header checker*/
        AtrHeaderChecker  ahc = new AtrHeaderChecker();
        printSectionStart(System.out, ahc.getSectionName());
        ahc.check(atrFile,System.out,null);
        
        /*Run sector checker*/
        SectorChecker sc = new SectorChecker();
        printSectionStart(System.out, sc.getSectionName());
        sc.check(atrFile,System.out,null);
        
        /*Run boot checker*/
        BootChecker bc = new BootChecker();
        printSectionStart(System.out,bc.getSectionName());
        bc.check(atrFile,System.out,null);
        
        Dos2Checker d2c = new Dos2Checker();
        printSectionStart(System.out,d2c.getSectionName());
        d2c.check(atrFile, System.out, null);
        
    }

    private static void printUsage() {
        System.out.println("Usage: atr1prnt <disk_image> [options]");
        System.out.println("atr1prnt 0.1 - Print and verify contents of ATR disk images");
        System.out.println("(c) 2019 BAHA Software");
    }
    
    private static void printSectionStart(PrintStream ps,String sectionTitle) {
        ps.println("========================================================================");
        ps.println("SECTION: "+sectionTitle);
        ps.println("========================================================================");
    }
    
}
