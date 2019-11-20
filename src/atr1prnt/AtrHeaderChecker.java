package atr1prnt;

import java.io.File;
import java.io.PrintStream;
import java.util.Properties;

public class AtrHeaderChecker implements AtrChecker {

    @Override
    public void check(AtrFile atrFile, PrintStream pr, Properties props,SummaryReport sumReport) {
       
       pr.println(String.format("File name: %s",new File(atrFile.getPathname()).getAbsolutePath()));
       pr.println(String.format("File size: $%06X",atrFile.getFileSize()));
       pr.println(String.format("Sectors: $%06X Lo-Paragrahs: $%06X Hi-Paragraphs: $%06X", atrFile.getSectors().size(),atrFile.getParagraphsLo(),atrFile.getParahraphsHi()));
       pr.println(String.format("Sector size: $%04X",atrFile.getSectorSize()));
       pr.println(String.format("CRC: $%08X Flag byte: $%02X ",atrFile.getCrc(),atrFile.getFlagByte()));
    }

    @Override
    public String getSectionName() {
        return "ATR HEADER";
    }

    
    
}
