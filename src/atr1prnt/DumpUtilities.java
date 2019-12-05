package atr1prnt;

import java.io.PrintStream;


public class DumpUtilities {
    
    public void printHeader(PrintStream pr,String header,char separator,boolean top,boolean bottom) {
        
        if (top==true) {
            for (int i=0;i<72;i++) {
                pr.print(separator);
            }
        }
        pr.println();
        pr.println(header);
        
        if (bottom==true) {
            for (int i=0;i<72;i++) {
                pr.print(separator);
            }
        }
        pr.println();
        
        
    }
    
}
