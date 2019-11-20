package atr1prnt;

import java.io.PrintStream;
import java.util.Properties;


public class SectorChecker implements AtrChecker {
    
    private PrintStream pr;
    
    @Override
    public void check(AtrFile atrFile, PrintStream pr, Properties props,SummaryReport sumReport) {
        
        if (props.containsKey("NOSECTORS")) return;
        
        this.pr=pr;
        
        int numSects = atrFile.getSectors().size();
        
        for (int k=1;k<=numSects;k++) {
            printSector(atrFile.getSectorData(k),k);
        }
        
    }
    
    private void printSector(int[] data,int number) {
        
        pr.println();
        pr.println(String.format("Sector #%06d $%06X",number,number));
        for(int i=0;i<data.length;i++) {
            if (i%16==0) {
                if (i!=0) pr.println();
                pr.print(String.format("%04X: ",i));
            }
            
            pr.print(String.format("%02X ", data[i]));
        }
        pr.println();
    }

    @Override
    public String getSectionName() {
        return "SECTORS";
    }

   
    
}
