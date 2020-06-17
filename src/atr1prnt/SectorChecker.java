package atr1prnt;

import hexdump.HexDumpStream;
import java.io.PrintStream;
import java.util.Properties;


public class SectorChecker implements AtrChecker {
    
    private PrintStream pr;
    private HexDumpStream hexDump;
    
    @Override
    public void check(AtrFile atrFile, PrintStream pr, Properties props,SummaryReport sumReport,DumpUtilities utils) {
        
        if (!props.containsKey("SECTORS")) return;
        
        utils.printHeader(pr,"SECTOR DUMP", '=', true, true);
        
        this.pr=pr;
        
        int numSects = atrFile.getSectors().size();
        
        hexDump = new HexDumpStream(16, 2, 4, " ", ":", "|", true, true,0);
        
        
        for (int k=1;k<=numSects;k++) {
            printSector(atrFile.getSectorData(k),k);
        }
        
    }
    
    private void printSector(int[] data,int number) {
        
        pr.println();
        pr.println(String.format("Sector #%06d $%06X",number,number));
        
        hexDump.reset();
        for(int i=0;i<data.length;i++) {
            hexDump.add(data[i]);
        }
        pr.print(hexDump.flush());
    }

    @Override
    public String getSectionName() {
        return "SECTOR DUMP";
    }

   
    
}
