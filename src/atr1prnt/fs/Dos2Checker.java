package atr1prnt.fs;

import atr1prnt.AtrChecker;
import atr1prnt.AtrFile;
import atr1prnt.Severity;
import atr1prnt.SummaryInfoItem;
import atr1prnt.SummaryReport;
import hexdump.HexDumpStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class Dos2Checker implements AtrChecker {

    private PrintStream pr;
    private AtrFile atrFile;
    private boolean dumpFiles;
    private final int vtocStyle;
    private final String messagePrefix;
    
    public static final int VTOC_DOS2 = 0;
    public static final int VTOC_DOSIIP =1;
    
    private static final int NO_NEXT_SECT=-1;
    private static final int NO_DISCONT_SECT=-1;

    private SummaryReport sumReport;
    
    private static class DirEntry {
        int flag;
        String humanName;
        String hexName;
        int startSector;
        int numSectors;
        
    }
    
    private static class DirEntryError {
        
        int sector;
        String errorMessage;
        
        DirEntryError(int sector,String errorMsg) {
            this.sector=sector;
            this.errorMessage=errorMsg;
        }
    }
    
    public Dos2Checker(int vtocStyle) {
        this.vtocStyle=vtocStyle;
        if (vtocStyle==VTOC_DOS2) {
            messagePrefix="DOS2";
        }
        else {
            messagePrefix="DOSIIP";
        }
        
    }

    @Override
    public void check(AtrFile atrFile, PrintStream pr, Properties props,SummaryReport sumReport) {
        this.pr = pr;
        this.atrFile = atrFile;
        dumpFiles = props.containsKey("DUMPFILES");
        this.sumReport=sumReport;

        boolean validDensity = checkDensity();

        if (!validDensity) {
            pr.println(messagePrefix+": Error: Unsupported density");
            sumReport.addItem(new SummaryInfoItem(Severity.SEV_FATAL,messagePrefix,"Unsupported density"));
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
        if (vtocStyle==VTOC_DOS2) {
            return "DOS 2 COMPATIBLE FILE SYSTEM";
        }
        else {
            return "DOS II+ FILE SYSTEM";
        }
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
            pr.println("Density: ENHANCED (130K)");
            return true;
        }
        else if (secSize == 128 && sectors < 1040 && sectors >= 1024) {
            pr.println("Density: ENHANCED (Truncated)");
            return true;
        }
        else if (secSize == 256 && sectors==720) {
            pr.println("Density: DOUBLE (180K)");
            return true;
        }
        else if (secSize == 256 && sectors<720) {
            pr.println("Density: DOUBLE (Truncated)");
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

        int bytePos = bitmapOffset;
        int sectorCount = 0;
        
        HexDumpStream bitmapDump = new HexDumpStream(32, 1, 4, "", ":", "|", true, false,firstSector);

        while (sectorCount < numSectors) {
            int mask = 128;
            for (int k = 0; k < 8 && sectorCount < numSectors; k++) {
                boolean b = ((sector[bytePos] & mask) == mask);
                bitmapDump.add(b ? 1 : 0);
                bitmap.put(firstSector+sectorCount,b);
                mask = mask >> 1;
                sectorCount++;
            }
            bytePos++;
        }
        pr.print(bitmapDump.flush());

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
                if (Character.isLetterOrDigit(c) || c=='-' || c=='.' || c=='_' || c==' ' ) {
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
        
        int numFreeBitmap1 = 0;
        int numFreeBitmap2 = 0;
        
        int numFreeCounter1 =0;
        int numFreeCounter2 =0;
        
        int freeSectorCorrection=0;
        
        /*Check VTOC 1*/
        pr.println();
        pr.println("VTOC 1 Sector (#360):");
        numFreeCounter1=checkVTOC1Header(atrFile.getSectorData(360));
        
        pr.println("Bitmap 1:");
        switch (vtocStyle) {
            case VTOC_DOS2: {
                listBitmapSector(atrFile.getSectorData(360), 0, 720, 10, bitmap1);
                numFreeBitmap1 = getFreeSectorsFromBitmap(bitmap1,0);
                freeSectorCorrection=0;
                break;
            }
            case VTOC_DOSIIP: {
                listBitmapSector(atrFile.getSectorData(360), 0, 936, 10, bitmap1);
                numFreeBitmap1 = getFreeSectorsFromBitmap(bitmap1,0);
                freeSectorCorrection=8;
                break;
            }
        }
        
        pr.println(String.format("Available sectors in bitmap 1: $%06X", numFreeBitmap1));
        
        /*Check VTOC 2*/
        if (atrFile.getSectors().size() > 720) {
            
            pr.println();
            switch (vtocStyle) {
                case (VTOC_DOS2): {
                    pr.println("VTOC 2 Sector (#1024):");
                    numFreeCounter2 = checkVTOC2Header(atrFile.getSectorData(1024));
                    break;
                }
                case (VTOC_DOSIIP): {
                    pr.println("VTOC 2 Sector (#359):");
                    break;
                }
            }
            
            pr.println("Bitmap 2:");
            switch (vtocStyle) {
                case VTOC_DOS2: {
                    listBitmapSector(atrFile.getSectorData(1024), 48, 976, 0, bitmap2);
                    numFreeBitmap2 = getFreeSectorsFromBitmap(bitmap2,720);
                    break;
                }
                case VTOC_DOSIIP: {
                    listBitmapSector(atrFile.getSectorData(359), 936, 80, 0, bitmap2);
                    numFreeBitmap2 = getFreeSectorsFromBitmap(bitmap2,936);
                    break;
                }
            }
            
            pr.println(String.format("Available sectors in bitmap 2: $%06X", numFreeBitmap2));
        }
        
        /*System.out.println(numFreeBitmap1);
        System.out.println(numFreeBitmap2);
        System.out.println(numFreeCounter1);
        System.out.println(numFreeCounter2);*/
        
        
        
        /*Check consistency of the bitmap*/
        if (numFreeBitmap1+numFreeBitmap2 != (numFreeCounter1+numFreeCounter2-freeSectorCorrection)) {
            pr.println("Error: Free sector counts in VTOC headers do not match the bits in the bitmap");
            sumReport.addItem(new SummaryInfoItem(Severity.SEV_ERROR,messagePrefix,"Inconsistent bitmap"));
        }
    }
    
    private int getFreeSectorsFromBitmap(HashMap<Integer,Boolean> bitmap,int firstValid) {
        
        int numFree=0;
        Set<Integer> keys = bitmap.keySet();
        
        Iterator<Integer> it = keys.iterator();
        while(it.hasNext()) {
            int secNo = it.next();
            
            boolean b = bitmap.get(secNo);
            if (b==true & secNo>=firstValid) numFree++;
        }
        return numFree;
    }
    

    private int checkVTOC1Header(int[] data) {

        int dosCode = data[0];
        int totalSectors = data[1] + 256 * data[2];
        int unusedSectors = data[3] + 256 * data[4];

        pr.println(String.format("DOS Code: $%02X Total Sectors: $%06X Unused Sectors: $%06X", dosCode, totalSectors, unusedSectors));
        return unusedSectors;

    }

    private int checkVTOC2Header(int[] data) {
        int unusedSectorsAbove = data[122] + 256 * data[123];
        pr.println(String.format("Unused Sectors above sector #719: $%06X", unusedSectorsAbove));
        return unusedSectorsAbove;
    }
    
    
    
    private void checkFiles(ArrayList<DirEntry> dirEntries) {
        
        pr.println();
        pr.println("Filesystem integrity: ");
        HashMap<Integer,Integer> usedByFS = new HashMap<>();
        
        int totalSectors=0;
        
        for(int i=0;i<dirEntries.size();i++) {
           
            DirEntry entry = dirEntries.get(i);
            HashSet<Integer> usedByEntry = new HashSet<>();
            ArrayList<Integer> entrySectorList = new ArrayList<>();
            ArrayList<Integer> fileData = new ArrayList<>();
            
            /*Each entry has three string builders*/
            StringBuilder headerSb = new StringBuilder();
            StringBuilder bodySb = new StringBuilder();
            
            /*Begin with empty line*/
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
            
            ArrayList<DirEntryError> errorList = new ArrayList<>();
            
            HexDumpStream chainDump = new HexDumpStream(8, 6, 6, " ", ": ", "|", true, false,0);
            
            /*Try all sectors of the entry*/
            while(sectorCount<entry.numSectors) {
                
                /*Check the sector*/
                int nextSect=checkSectorInChain(i, currSector, errorList, entrySectorList, usedByEntry, usedByFS, fileData);
                
                chainDump.add(currSector);
                
                /*Stop, when there is no sense to continue*/
                if (nextSect==NO_NEXT_SECT) break;
                
                currSector=nextSect;
                sectorCount++;
                totalSectors++;
            }
            /*Now the whole directory entry is processed*/
            bodySb.append(chainDump.flush());
            
            
            /*Check continuity and flag it in the header*/
            int discSector = getFirstDicontinuitySector(entrySectorList);
            
            
            /*If there is any error, flag file as corrupt*/
            if (!errorList.isEmpty()) {
                headerSb.append("Corrupt");
            }
            
            /*Otherwise specify as contigous or non-contiguous*/
            else {
                if (discSector == NO_DISCONT_SECT) {
                    headerSb.append("Contiguous");
                }
                else {
                    headerSb.append(String.format("Non-contiguous at: $%06X", discSector));
                }
            }
            
            /*Print the entry*/
            pr.println(headerSb.toString());
            pr.println(bodySb.toString());
            
            /*Prin errors, if there were any*/
            if (!errorList.isEmpty()) {
                pr.println("Errors in the entry");
                for (DirEntryError der:errorList) {
                    pr.println(String.format("%06X: %s", der.sector,der.errorMessage));
                }
                sumReport.addItem(new SummaryInfoItem(Severity.SEV_FATAL,messagePrefix,String.format("Directory entry $%02X error",i)));
            }
            
            if (dumpFiles==true) dumpFile(pr,fileData);
            
        }
        /*All entries are done*/
        pr.println();
        pr.println(String.format("Total sectors used by directory entries: $%06X",totalSectors));
        
        
    }
    
    
    private int checkSectorInChain(int entryNo, int currSector, ArrayList<DirEntryError> errorList, ArrayList<Integer> entrySectorList, HashSet<Integer> usedByEntry, HashMap<Integer, Integer> usedByFS, ArrayList<Integer> fileData) {
        boolean halt = false;

        int ofsDirEntryNum;
        int ofsNextSecHi;
        int ofsNextSecLo;
        int ofsNumBytes;
        int maxNumBytes;
        
        /*Check if sector exists*/
        if (!existsSector(currSector)) {
            errorList.add(new DirEntryError(currSector, "No such sector"));
            halt = true;
            return NO_NEXT_SECT;
        }

        if (atrFile.getSectorData(currSector).length == 128) {

            ofsDirEntryNum = 125;
            ofsNextSecHi = 125;
            ofsNextSecLo = 126;
            ofsNumBytes = 127;
            maxNumBytes = 125;
        }
        else {
            ofsDirEntryNum = 125 + 128;
            ofsNextSecHi = 125 + 128;
            ofsNextSecLo = 126 + 128;
            ofsNumBytes = 127 + 128;
            maxNumBytes = 125 + 128;
        }

        

        entrySectorList.add(currSector);

        /*Check for loop*/
        if (halt == false && usedByEntry.add(currSector) == false) {
            errorList.add(new DirEntryError(currSector, "Sector loop"));
            halt = true;
        }

        /*Check if two or more files using the same sector*/
        if (halt == false) {

            if (usedByFS.containsKey(currSector)) {
                errorList.add(new DirEntryError(currSector, String.format("Sector used by other entry %02X", usedByFS.get(currSector))));
            }
            else {
                usedByFS.put(currSector, entryNo);
            }

        }

        int nextSect = NO_NEXT_SECT;

        /*If not halt, probe the sector and determine what is the next one*/
        if (halt == false) {
            /*Check if sector belongs to the directory entry*/
            int[] data = atrFile.getSectorData(currSector);
            int dirEntryNum = data[ofsDirEntryNum] >> 2;
            if (dirEntryNum != entryNo) {
                errorList.add(new DirEntryError(currSector, String.format("Sector belongs to different entry $%02X.", dirEntryNum)));
            }

            /*Check what is the next sector*/
            int hiSect = data[ofsNextSecHi] & 0x03;
            int loSect = data[ofsNextSecLo];
            nextSect = hiSect * 256 + loSect;

            /*Check how much data in the sector*/
            int numBytes = data[ofsNumBytes];
            if (numBytes > maxNumBytes) {
                errorList.add(new DirEntryError(currSector, String.format("Number of bytes in the sector exceeds maximum: $%02X.", numBytes)));
            }

            /*Collect data for file dump*/
            if (dumpFiles) {
                int dumpBytes = numBytes;
                if (dumpBytes > maxNumBytes) {
                    dumpBytes = maxNumBytes;
                }
                for (int k = 0; k < dumpBytes; k++) {
                    fileData.add(data[k]);
                }
            }

        }
        return halt==true?NO_NEXT_SECT:nextSect;
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
        return NO_DISCONT_SECT;
        
    }
    
    
    private void dumpFile(PrintStream pr, ArrayList<Integer> fileData) {
        pr.println("File dump:");
        
        HexDumpStream hexDump = new HexDumpStream(16, 2, 6, " ", ":", "|", true, true,0);
        for (int val:fileData) {
            hexDump.add(val);
        }
        
        pr.print(hexDump.flush());
        
    }

    

}
