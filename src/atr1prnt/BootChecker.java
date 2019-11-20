package atr1prnt;

import java.io.PrintStream;
import java.util.Properties;


class BootChecker implements AtrChecker {

    @Override
    public void check(AtrFile atrFile, PrintStream pr, Properties props,SummaryReport sumReport) {
        
        if (props.containsKey("NOBOOT")) return;
        
        int[] data = atrFile.getSectorData(1);
        
        int bFlags = data[0];
        int bSectors = data[1];
        int bLoad = data[2]+256*data[3];
        int bInit = data[4]+256*data[5];
        
        pr.println(String.format("Boot flag: $%02X, Sectors: $%02X",bFlags,bSectors));
        pr.println(String.format("Load address: $%04X, Init address: $%04X",bLoad,bInit));
        
    }

    @Override
    public String getSectionName() {
        return "BOOT SECTOR";
    }
    
}
