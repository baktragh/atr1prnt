package atr1prnt;

import java.io.File;
import java.io.PrintStream;
import java.util.Properties;

public class AtrHeaderChecker implements AtrChecker {

    @Override
    public void check(AtrFile atrFile, PrintStream pr, Properties props) {
       
       pr.println(String.format("File name: %s",new File(atrFile.getPathname()).getAbsolutePath()));
       pr.println(String.format("File size: %08d",atrFile.getFileSize()));
       pr.println(String.format("Sectors: %06d Lo-Paragrahs: %06d Hi-Paragraphs: %06d", atrFile.getSectors().size(),atrFile.getParagraphsLo(),atrFile.getParahraphsHi()));
       pr.println(String.format("Sector size: %04d",atrFile.getSectorSize()));
       pr.println(String.format("CRC: $%08X Flag byte: $%02X ",atrFile.getCrc(),atrFile.getFlagByte()));
    }

    @Override
    public String getSectionName() {
        return "ATR HEADER";
    }

    
    
}
