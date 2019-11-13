package atr1prnt;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class Dos2Checker implements AtrChecker {

    private PrintStream pr;
    private AtrFile atrFile;
    private boolean dumpBitmap;
    private boolean dumpFiles;

    
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
        dumpFiles = props.containsKey("DOS2-DUMPFILES");

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
                
                if (sectorCount % 32 == 0) {
                    if (sectorCount!=0) pr.println();
                    pr.print(String.format("%04X: ",curSector));
                }
                pr.print(String.format("%01d", b ? 1 : 0));
                bitmap.put(curSector, b);
                
                mask = mask >> 1;
                curSector++;
                sectorCount++;
            }
            bytePos++;
        }
        pr.println();

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
                char nc = (c>=128)?(char)(c-128):c;
                if (Character.isLetterOrDigit(c) || c=='-' || c=='.' || c=='_' ) {
                    sbHuman.append(nc);
                }
                else {
                    sbHuman.append('.');
                }
                sbHexa.append(String.format("%02X ", (int) c));
            }
            pos += 11;

            /*Now print it*/
            pr.println(String.format("F:$%02X S:$%06X L:$%06X N:%s %s", dFlag, dStartSector, dNumSectors, sbHuman.toString(), sbHexa.toString()));
            
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
        pr.println("VTOC 1 Sector (#360):");
        checkVTOC1Header(atrFile.getSectorData(360));
        if (dumpBitmap) {
            pr.println("Bitmap 1:");
            listBitmapSector(atrFile.getSectorData(360), 0, 720, 10,bitmap1);
        }

        if (atrFile.getSectors().size() > 720) {
            pr.println();
            pr.println("VTOC 2 Sector (#1024):");
            checkVTOC2Header(atrFile.getSectorData(1024));
            if (dumpBitmap) {
                pr.println("Bitmap 2:");
                listBitmapSector(atrFile.getSectorData(1024), 48, 976, 0,bitmap2);
            }
        }
    }

    private void checkVTOC1Header(int[] data) {

        int dosCode = data[0];
        int totalSectors = data[1] + 256 * data[2];
        int unusedSectors = data[3] + 256 * data[4];

        pr.println(String.format("DOS Code: $%02X Total Sectors: $%06X Unused Sectors: $%06X", dosCode, totalSectors, unusedSectors));

    }

    private void checkVTOC2Header(int[] data) {
        int unusedSectorsAbove = data[122] + 256 * data[123];
        pr.println(String.format("Unused Sectors above sector 719: $%06X", unusedSectorsAbove));
    }
    
    private void checkFiles(ArrayList<DirEntry> dirEntries) {
        
        String linesp = System.getProperty(("line.separator"));
        
        pr.println();
        pr.println("Filesystem integrity: ");
        HashSet<Integer> usedByFS = new HashSet<>();
        
        for(int i=0;i<dirEntries.size();i++) {
           
            DirEntry entry = dirEntries.get(i);
            HashSet<Integer> usedByEntry = new HashSet<>();
            ArrayList<Integer> entrySectorList = new ArrayList<>();
            
            /*Each entry has three string builders*/
            StringBuilder headerSb = new StringBuilder();
            StringBuilder bodySb = new StringBuilder();
            
            pr.println();
            
            /*Entry header*/
            headerSb.append(
                    String.format("Number: $%02X Flag: $%02X Name:%s %s \nStart Sector: $%06X Sectors: $%06X ",i,entry.flag,entry.humanName,entry.hexName,entry.startSector,entry.numSectors)
            );
            
            /*If empty or unused, just print header and proceed to the next entry*/
            if (entry.flag==0 || entry.numSectors==0) {
                pr.println(headerSb.toString().trim());
                continue;
            }
            
            
            int sectorCount=0;
            int currSector=entry.startSector;
            int lineCount=0;
            
            
            /*Try all sectors of the entry*/
            while(sectorCount<entry.numSectors) {
                
                boolean sectorGood=true;
                boolean halt=false;
                
                ArrayList<String> errorList = new ArrayList<>();
                
            
                /*Check if sector exists*/
                if (!existsSector(currSector)) {
                    errorList.add("NO_SUCH_SECTOR");
                    sectorGood=false;
                    halt=true;
                }
                
                entrySectorList.add(currSector);
                
                /*Check for loop*/
                if (halt==false && usedByEntry.add(currSector)==false) {
                    errorList.add("SECTOR_LOOP");
                    sectorGood=false;
                    halt=true;
                }
                
                /*Check if two or more files using the same sector*/
                if ((halt==false && usedByFS.add(currSector)==false)) {
                    errorList.add("USED_BY_OTHER_DIR_ENTRY");
                    sectorGood=false;
                    halt=false;
                }
                
                int nextSect=-1;
                
                if (halt==false) {
                    /*Check if sector belongs to the directory entry*/
                    int[] data = atrFile.getSectorData(currSector);
                    if (((data[125]>>2))!=i) {
                        errorList.add(String.format("BELONGS_TO_DIFFERENT_ENTRY $%02X.",data[0]));
                        sectorGood=false;
                    }
                
                    /*Check what is the next sector*/
                    int hiSect = data[125] & 0x03;
                    int loSect = data[126];
                    nextSect = hiSect * 256 + loSect;
                }
                
                /*If the sector is not good, report error message*/
                if (sectorGood==false) {
                    
                    bodySb.append(linesp);
                    bodySb.append(String.format("%06X ",currSector));
         
                    bodySb.append(" ");
                    bodySb.append("Error(s) in sector");
                    bodySb.append(linesp);
                    
                    for (String errMsg:errorList) {
                        bodySb.append("        ");
                        bodySb.append(errMsg);
                        bodySb.append(linesp);
                    }
                    lineCount=0;
                }
                
                /*If sector is good, continue formatted printout*/
                else {
                    if ((lineCount % 8)==0) {
                        if (lineCount!=0) {
                            bodySb.append(linesp);
                            lineCount=0;
                        } 
                        if (sectorCount % 8 ==0 ) {
                            bodySb.append((String.format("%06X: ",sectorCount)));
                        }
                        else {
                            int gap = sectorCount % 8;
                            bodySb.append((String.format("%06X: ",sectorCount-gap)));
                            for(int g=0;g<gap;g++) {
                                bodySb.append("       ");
                            }
                            lineCount=gap;
                        }
                        
                    }
                    bodySb.append(String.format("%06X ",currSector));
                    lineCount++;
                    
                }
                
                if (halt==true) break;
                
                currSector=nextSect;
                sectorCount++;
                
                
                
            }
            
            /*Now the whole directory entry is processed*/
            
            /*Check continuity and flag it in the header*/
            int discSector = getFirstDicontinuitySector(entrySectorList);
            
            if (discSector==-1) {
                headerSb.append("Contiguous");
            }
            else {
                headerSb.append(String.format("Non-contiguous at: $%06X",discSector));
            }
            
            /*Print the entry*/
            pr.println(headerSb.toString());
            pr.println(bodySb.toString());
            
        }
        
        
    }
    
    private boolean existsSector(int number) {
        if (number<1 || number>atrFile.getSectors().size()) return false;
        return true;
    }
    
    private int getFirstDicontinuitySector(List<Integer> sectorList) {
        
        int prev=sectorList.get(0);
        for(int i=1;i<sectorList.size();i++) {
            if (sectorList.get(i)!=prev+1) return prev;
            prev=sectorList.get(i);
            
        }
        return -1;
        
    }
    

}
