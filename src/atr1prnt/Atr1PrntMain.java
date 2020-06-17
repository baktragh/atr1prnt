package atr1prnt;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

/* Batch interface for ATR1PRNT*/
public class Atr1PrntMain {

    public static void main(String[] args) {
        
        /*No arguments result in usage info*/
        if (args.length<1) {
            printUsage();
            return;
        }
        
        /*Get arguments as properties*/
        Properties runProperties = new Properties();
        for (int i=1;i<args.length;i++) {
            String arg = args[i].trim().toUpperCase();
            if (!arg.isEmpty()) {
                runProperties.put(args[i], true);
            }
        }
        
        /*Probe the file specifier*/
        String fileSpec = args[0];
        ArrayList<String> fileList = new ArrayList<>();
        
        /*Single file?*/
        if (fileSpec.indexOf('*')==-1 && fileSpec.indexOf('?')==-1) {
            fileList.add(fileSpec);
        }
        else {
            globFiles(fileSpec,fileList);
        }
        
        /*Prepare for message output*/
        PrintStream outStream = System.out;
        PrintStream errStream = System.err;
        
        /*Silent stream when SILENT is specified*/
        if (runProperties.containsKey("SILENT")) {
            outStream = new PrintStream(new NullOutputStream());
        }
        
        /*Print selected files and options*/
        printRunInfo(fileList,runProperties,outStream);
        
        /*Determine if a summary report is requested*/
        boolean haveSummary = runProperties.containsKey("SUMMARY");
        
        int globalRC=0;
        
        ArrayList<SummaryReport> summaryReports = new ArrayList<>();
        
        for (String fullPath : fileList) {

            int rc;
            
            File f = new File(fullPath);
            SummaryReport sr = new SummaryReport(f.getName());
            summaryReports.add(sr);
            
            Atr1Prnt ap = Atr1Prnt.getInstance(fullPath, runProperties, outStream, errStream,sr);

            try {
                rc = ap.dump();
            }
            catch (Exception e) {
                e.printStackTrace(errStream);
                rc = -1;
            }
            
            if (rc<globalRC) globalRC=rc;
        }
        
        /*Print the summary reports if needed*/
        if (haveSummary) {
            DumpUtilities du = new DumpUtilities();
            for (SummaryReport sr:summaryReports) {
                sr.printSummary(System.out, du);
            }
        }
        
        
        System.exit(globalRC);
    }

    private static void printUsage() {
        System.out.println("atr1prnt 0.6 - Print and verify contents of ATR disk images");
        System.out.println("by BAHA Software");
        System.out.println();
        System.out.println("Usage: java -jar atr1prnt.jar <disk_image> [options]");
        System.out.println();
        System.out.println("General options: ");
        System.out.println("SECTORS      Perform full sector dump");
        System.out.println("NOBOOT       Skip boot sector dump");
        System.out.println("SILENT       Silent run with no output");
        System.out.println("SUMMARY      Display summary report");
        System.out.println("DUMPFILES    Dump contents of files");
        System.out.println("ERASED       Process erased directory entries");
        System.out.println("File system choice: ");
        System.out.println("FS-DOS2      DOS 2 (default)");
        System.out.println("FS-DOSIIP    DOS II+");
        System.out.println("FS-NONE      No filesystem dump");
        System.out.println("Special options: ");
        System.out.println("BOOT256      Assume first three sectors have 256 bytes");
        System.out.println();
    }

    private static void globFiles(String fileSpec, ArrayList<String> fileList) {
        
        File f = new File(fileSpec);
        String fn = f.getName();
        
        File p = f.getParentFile();
        if (p==null) p = new File(System.getProperty("user.dir"));
        
        /*If there is a wildcard in the parent, refuse it*/
        String sParent = p.getPath();
        if (sParent.indexOf('*')!=-1 || sParent.indexOf('?')!=-1) {
            System.err.println("ERROR: Invalid wildcarding in directory");
        }
        
        File[] listing = p.listFiles(new FilenameFilter() {
           @Override
           public boolean accept(File dir,String name)  {
               PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + fn);
               return matcher.matches(new File(name).toPath());
           }
        });
        
        for(File cf:listing) {
            fileList.add(cf.getAbsolutePath());
        }
        
    }

    private static void printRunInfo(ArrayList<String> fileList, Properties runProperties, PrintStream outStream) {
        
        DumpUtilities ut = new DumpUtilities();
        ut.printHeader(outStream, "ATR1PRNT", '=', true, true);
        
        outStream.println("Files selected:");
        for (String fileSpec:fileList) {
            outStream.println(fileSpec);
        }
        
        outStream.println("Options specified:");
        
        Set<Object> keySet = runProperties.keySet();
        StringBuilder sb = new StringBuilder();
        
        for (Object o:keySet) {
            
            String s = (String)o;
            int l = s.length();
            if (l>64) {
                s = s.substring(0, 64)+"...";
                l=s.length();
            }
            
            if (sb.length()+l>72) {
                outStream.println(sb.toString());
                sb.setLength(0);
            }
            sb.append(s);
            sb.append(' ');
        }
        
        if (sb.length()!=0) {
            outStream.println(sb.toString());
        }
        
    }
    
    /**
     * Output stream that does nothing
     */
    private static class NullOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {

        }

    }
    
    
}
