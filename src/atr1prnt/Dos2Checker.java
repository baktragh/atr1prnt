package atr1prnt;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

public class Dos2Checker implements AtrChecker {

    private PrintStream pr;
    private AtrFile atrFile;
    private boolean dumpBitmap;
    
    
    private static class DirEntry {
        int flag;
        String humanName;
        String hexName;
        int startSector;
        int numSectors;
    }
    

    @Override
    public void check(AtrFile atrFile, PrintStream pr, Properties props) {
        this.pr = pr;
        this.atrFile = atrFile;
        dumpBitmap = props.containsKey("DOS2-BITMAP");

        boolean validDensity = checkDensity();

        if (!validDensity) {
            pr.println("DOS2: Error: Unsupported density");
            return;
        }
        
        ArrayList<DirEntry> dirEntries = new ArrayList<>();
        HashMap<Integer,Boolean> bitmap1 = new HashMap<>();
        HashMap<Integer,Boolean> bitmap2 = new HashMap<>();

        checkDirectory(dirEntries);
        checkVTOC(bitmap1,bitmap2);
        checkFiles(dirEntries);
    }

    @Override
    public String getSectionName() {
        return "DOS 2 COMPATIBLE FILE SYSTEM";
    }

    private boolean checkDensity() {

        int secSize = atrFile.getSectorSize();
        int sectors = atrFile.getSectors().size();

        if (secSize == 128 && sectors == 720) {
            pr.println("Density: SINGLE (90K)");
            return true;
        }
        else if (secSize == 128 && sectors < 720 && sectors >= 368) {
            pr.println("Density: SINGLE (Truncated)");
            return true;
        }
        else if (secSize == 128 && sectors == 1040) {
            pr.println("Density: MEDIUM (130K)");
            return true;
        }
        else if (secSize == 128 && sectors < 1040 && sectors >= 1024) {
            pr.println("Density: MEDIUM (Truncated)");
            return true;
        }
        else {
            pr.println(String.format("Density: NOT SUPPORTED. Sector size: %d Number of sectors: %d", atrFile.getSectorSize(), atrFile.getSectors().size()));
            return false;
        }
    }

    private void checkDirectory(List<DirEntry> dirEntries) {

        pr.println();
        pr.println("Directory listing:");

        for (int k = 361; k <= 368; k++) {
            listDirSector(atrFile.getSectorData(k), pr,dirEntries);
        }

    }

    private void listBitmapSector(int[] sector, int firstSector, int numSectors, int bitmapOffset,HashMap<Integer,Boolean> bitmap) {

        int curSector = firstSector;
        int bytePos = bitmapOffset;
        int sectorCount = 0;

        while (sectorCount < numSectors) {
            int mask = 128;
            for (int k = 0; k < 8 && sectorCount < numSectors; k++) {
                boolean b = ((sector[bytePos] & mask) == mask);
                mask = mask >> 1;
                pr.println(String.format("S: #%06d $%06x F: %01d", curSector, curSector, b ? 1 : 0));
                bitmap.put(curSector, b);
                curSector++;
                sectorCount++;
            }
            bytePos++;
        }

    }

    private void listDirSector(int[] sector, PrintStream pr,List<DirEntry> dirEntries) {

        int pos = 0;
        for (int i = 0; i < 8; i++) {

            int dFlag = sector[pos];
            pos++;
            int dNumSectors = sector[pos] + 256 * sector[pos + 1];
            pos += 2;
            int dStartSector = sector[pos] + 256 * sector[pos + 1];
            pos += 2;

            StringBuilder sbHuman = new StringBuilder();
            StringBuilder sbHexa = new StringBuilder();
            for (int z = 0; z < 11; z++) {
                char c = (char) sector[pos + z];
                if (Character.isLetterOrDigit(c)) {
                    sbHuman.append(c);
                }
                else {
                    sbHuman.append('.');
                }
                sbHexa.append(String.format("%02X ", (int) c));
            }
            pos += 11;

            /*Now print it*/
            pr.println(String.format("F: $%02X SS: #%05d $%06X NS: #%06d $%06X NAME: %s %s ", dFlag, dStartSector, dStartSector, dNumSectors, dNumSectors, sbHuman.toString(), sbHexa.toString()));
            
            /*Add to the collection*/
            DirEntry de = new DirEntry();
            de.flag=dFlag;
            de.startSector=dStartSector;
            de.numSectors=dNumSectors;
            de.humanName=sbHuman.toString();
            de.hexName=sbHexa.toString();
            dirEntries.add(de);
        }
    }

    private void checkVTOC(HashMap<Integer,Boolean> bitmap1,HashMap<Integer,Boolean> bitmap2) {

        /*Check VTOC 1*/
        pr.println();
        pr.println("VTOC 1 Sector (#360) Listing");
        checkVTOC1Header(atrFile.getSectorData(360));
        if (dumpBitmap) {
            pr.println("Bitmap 1 listing (sector #360). Sectors #0-#719");
            listBitmapSector(atrFile.getSectorData(360), 0, 720, 10,bitmap1);
        }

        if (atrFile.getSectors().size() > 720) {
            pr.println();
            pr.println("VTOC 2 Sector (#1024) Listing");
            checkVTOC2Header(atrFile.getSectorData(1024));
            if (dumpBitmap) {
                pr.println();
                pr.println("Bitmap 2 listing (sector #1024). Sectors #48-#1023");
                listBitmapSector(atrFile.getSectorData(1024), 48, 976, 0,bitmap2);
            }
        }
    }

    private void checkVTOC1Header(int[] data) {

        int dosCode = data[0];
        int totalSectors = data[1] + 256 * data[2];
        int unusedSectors = data[3] + 256 * data[4];

        pr.println(String.format("DOS Code: $%02X Total Sectors: %06d Unused Sectors: %06d", dosCode, totalSectors, unusedSectors));

    }

    private void checkVTOC2Header(int[] data) {
        int unusedSectorsAbove = data[122] + 256 * data[123];
        pr.println(String.format("Unused Sectors above sector 719: %06d", unusedSectorsAbove));
    }
    
    private void checkFiles(ArrayList<DirEntry> dirEntries) {
        
        pr.println("Walking directory: ");
        
        for(int i=0;i<dirEntries.size();i++) {
           
            DirEntry entry = dirEntries.get(i);
            
            pr.println(String.format("%02d %01X %s %s", i,entry.flag,entry.humanName,entry.hexName));
            pr.println(String.format("Start sector     : #%06d $%06X",entry.startSector,entry.startSector));
            pr.println(String.format("Number of sectors: #%06d $%06X",entry.numSectors,entry.numSectors));
            
            int sectorCount=0;
            int currSector=entry.startSector;
            
            HashSet<Integer> usedByFS = new HashSet<>();
            
            while(sectorCount<entry.numSectors) {
            
                HashSet<Integer> usedByEntry = new HashSet<>();
                
                StringBuilder sb = new StringBuilder();
                StringBuilder se = new StringBuilder();
                
                sb.append(String.format("Sector #%06d $%06X : ",currSector,currSector));
            
                /*Check if sector exists*/
                if (!existsSector(currSector)) {
                    se.append("Sector doesn't exist");
                    pr.println(sb.toString());
                    pr.println(se.toString());
                    break;
                }
                
                /*Check for loop*/
                if (usedByEntry.add(currSector)==false) {
                    se.append("Loop in sector chain");
                    pr.println(sb.toString());
                    pr.println(se.toString());
                    break;
                }
                
                /*Check if two or more files using the same sector*/
                if (usedByFS.add(currSector)==false) {
                    se.append("Sector already used by another directory entry");
                }
                
                
                /*Check if sector belongs to the directory entry*/
                int[] data = atrFile.getSectorData(currSector);
                if (((data[125]>>2))!=i) {
                    se.append(String.format("Sector doesn't belong to the directory entry. Belongs to entry #%02d.",data[0]));
                }
                
                /*Check what is the next sector*/
                int hiSect = data[125]&0x03;
                int loSect = data[126];
                int nextSect = hiSect*256+loSect;
                
                sb.append(String.format("Next: #%06d $%06X",nextSect,nextSect));
                pr.println(sb.toString());
                
                String errors = se.toString();
                if (!errors.isEmpty()) {
                    pr.println("Errors: "+se.toString());
                }
                
                currSector=nextSect;
                sectorCount++;
                
            }
            
        }
        
        
    }
    
    private boolean existsSector(int number) {
        if (number<1 || number>atrFile.getSectors().size()) return false;
        return true;
    }
    

}
