package atr1prnt;

import java.io.PrintStream;
import java.util.Properties;


public class Dos2Checker implements AtrChecker {

    @Override
    public void check(AtrFile atrFile, PrintStream pr, Properties props) {
        checkDirectory(atrFile,pr);
        if (props.containsKey("DOS2-BITMAP")) {
            checkBitmap(atrFile,pr);
        }
    }

    @Override
    public String getSectionName() {
        return "DOS 2 COMPATIBLE FILE SYSTEM";
                
    }
    
    private void checkDirectory(AtrFile atrFile, PrintStream pr) {
        
        pr.println("Directory listing");
        
        for (int k=361;k<=368;k++) {
            listDirSector(atrFile.getSectorData(k),pr);
        }
        
    }
        
        
    
    
    private void listBitmapSector(int[] sector,PrintStream pr,int firstSector,int numSectors) {
    
        int curSector = firstSector;
        int bytePos=0;
        int sectorCount=0;
        
        while (sectorCount<numSectors) {
            int mask = 128;
            for (int k=0;k<8 && sectorCount<numSectors;k++) {
                boolean b = ((sector[bytePos]&mask)==mask);
                mask=mask>>1;
                pr.println(String.format("S: #%05d $%04x F: %01d",curSector,curSector,b?1:0));
                curSector++;
                sectorCount++;
            }
            bytePos++;
        }
        
    }
    
    private void listDirSector(int[] sector,PrintStream pr) {
        
        int pos=0;
        for (int i=0;i<8;i++) {
           
            int dFlag = sector[pos];pos++;
            int dNumSectors = sector[pos]+256*sector[pos+1];pos+=2;
            int dStartSector = sector[pos]+256*sector[pos+1];pos+=2;
            
            StringBuilder sbHuman = new StringBuilder();
            StringBuilder sbHexa = new StringBuilder();
            for(int z=0;z<11;z++) {
                char c = (char)sector[pos+z];
                if (Character.isAlphabetic(c)) {
                    sbHuman.append(c);
                }
                else {
                    sbHuman.append('.');
                }
                sbHexa.append(String.format("%02X ",(int)c));
            }
            pos+=11;
            
            /*Now print it*/
            pr.println(String.format("F: $%02X SS: #%05d $%04X NS: #%05d $%04X NAME: %s %s ",dFlag,dStartSector,dStartSector,dNumSectors,dNumSectors,sbHuman.toString(),sbHexa.toString()));
            
        }
    }

    private void checkBitmap(AtrFile atrFile, PrintStream pr) {
        
        pr.println("Bitmap 1 listing");
        listBitmapSector(atrFile.getSectorData(360),pr,0,720);
        
        if (atrFile.getSectors().size()>720) {
          pr.println();
          pr.println("Bitmap 2 listing");
          listBitmapSector(atrFile.getSectorData(1024),pr,48,976);
        }
    }
    
}
