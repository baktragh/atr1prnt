package atr1prnt;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Properties;

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
        
        
        int globalRC=0;
        
        for (String fullPath : fileList) {

            int rc=0;
            Atr1Prnt ap = Atr1Prnt.getInstance(fullPath, runProperties, System.out, System.err);

            try {
                rc = ap.dump();
            }
            catch (Exception e) {
                e.printStackTrace();
                rc = -1;
            }
            
            if (rc<globalRC) globalRC=rc;
            
        }
        
        System.exit(globalRC);
    }

    private static void printUsage() {
        System.out.println("atr1prnt 0.5 - Print and verify contents of ATR disk images");
        System.out.println("by BAHA Software");
        System.out.println();
        System.out.println("Usage: java -jar atr1prnt.jar <disk_image> [options]");
        System.out.println();
        System.out.println("General options: ");
        System.out.println("NOSECTORS      Skip sector dump");
        System.out.println("NOBOOT         Skip boot sector dump");
        System.out.println("SILENT         Silent run with no output");
        System.out.println("SUMMARY        Display summary report");
        System.out.println("DUMPFILES      Dump contents of files");
        System.out.println("NOERASED       Skip erased directory entries");
        System.out.println("File system choice: ");
        System.out.println("FS-DOS2   - DOS 2 (default)");
        System.out.println("FS-DOSIIP - DOS II+");
        System.out.println("FS-NONE   - No filesystem dump");
        System.out.println();
    }

    private static void globFiles(String fileSpec, ArrayList<String> fileList) {
        
        File f = new File(fileSpec);
        File p = f.getParentFile();
        String fn = f.getName();
        
        /*If there is a wildcard in the parent, refuse it*/
        String sParent = p.getPath();
        if (sParent.indexOf('*')!=-1 || sParent.indexOf('?')!=-1) {
            System.out.println("ERROR: Invalid wildcarding in directory");
        }
        
        File[] listing = p.listFiles(new FilenameFilter() {
           @Override
           public boolean accept(File dir,String name)  {
               System.out.println(name);
               PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + fn);
               return matcher.matches(f.toPath());
           }
        });
        
        for(File cf:listing) {
            fileList.add(cf.getAbsolutePath());
        }
        
    }
    
    
}
