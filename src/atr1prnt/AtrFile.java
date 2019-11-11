
package atr1prnt;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;


public class AtrFile {

    private final String pathname;
    private final long fileSize;
    private final ArrayList<int[]> sectors;
    private int[] headerBytes;
    private final int sectorSize;
    private final int paragraphsLo;
    private final int parahraphsHi;
    private final long crc;
    private final int flagByte;
    
    public static AtrFile getFromPathName(String pathName) throws AtrException,IOException {
        
        /*Check existence and if it is a regular file*/
        File f = new File(pathName);
        if (!f.exists()) {
            throw new AtrException("File "+pathName+" does not exist");
        }
        if (!f.isFile()) {
            throw new AtrException("File "+pathName+" is not a regular file");
        }
        
        /*Check if not too big or too small*/
        RandomAccessFile raf = new RandomAccessFile(f,"r");
        long len = f.length();
        
        if (len>64*1024*1024) {
            raf.close();
            throw new AtrException("File "+pathName+" is larger than 64 MB");
        }
        if (len<16) {
            raf.close();
            throw new AtrException("File "+pathName+" is smaller than 16 bytes");
        }
        
        /*Now process the header*/
        int b1,b2;
        
        
        /*Check the magic sequence*/
        b1=raf.read();
        b2=raf.read();
        
        if (b1!=0x96 || b2!=0x02) {
            raf.close();
            throw new AtrException("File "+pathName+" does not begin with magic sequence $96 $02");
        }
        
        /*Check the number of paragraphs - Low*/
        b1=raf.read();
        b2=raf.read();
        int parLo = b1+b2*256;
        
        /*Check sector size*/
        b1=raf.read();
        b2=raf.read();
        int sectorSize = b1+b2*256;
        
        /*Check the number of paragraphs - High*/
        b1=raf.read();
        int parHi = b1;
        
        
        long crc = 0;
        
        b1=raf.read();
        crc+=b1;
        b1=raf.read();
        crc+=b1*256;
        b1=raf.read();
        crc+=b1*65536;
        b1=raf.read();
        crc+=b1*(65536*256);
        
        /*Skip unused byte*/
        for(int i=0;i<4;i++) {
            raf.read();
        }
        
        /*Flag*/
        int flagByte=raf.read();
        
        
        /*Read all the sectors*/
        int sectorNumber = 1;
        ArrayList<int[]> sectorData = new ArrayList<>();
        
        while(true) {
            
            /*Try just one byte. If EOF between or after sector, that is ok*/
            int firstByte = raf.read();
            if (firstByte==-1) break;
            
            /*Set sector length*/
            int realSectorLength=(sectorNumber<=3?128:sectorSize);
            
            /*Read sector data*/
            int[] sector = new int[realSectorLength];
            sector[0] = firstByte;
            for (int i = 1; i < sector.length; i++) {
                int b0 = raf.read();
                if (b0 == -1) {
                    raf.close();
                    throw new AtrException("Sector " + sectorNumber + " truncated");
                }
                sector[i] = b0;
            }
            
            /*Add sector to the list*/
            sectorNumber++;
            sectorData.add(sector);
            
        }
        
        raf.close();
        return new AtrFile (pathName,len,parLo,parHi,sectorSize,crc,flagByte,sectorData);
    }
    
    
    private AtrFile(String pathname,long fileSize,int paragraphsLo,int paragraphsHi,int sectorSize,long crc,int flagByte,ArrayList<int[]> data) {
        this.pathname=pathname;
        this.fileSize=fileSize;
        this.paragraphsLo=paragraphsLo;
        this.parahraphsHi=paragraphsHi;
        this.sectorSize=sectorSize;
        this.crc=crc;
        this.flagByte=flagByte;
        this.sectors=data;
    } 

    /**
     * @return the pathname
     */
    public String getPathname() {
        return pathname;
    }

    /**
     * @return the fileSize
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * @return the sectors
     */
    public ArrayList<int[]> getSectors() {
        return sectors;
    }

    /**
     * @return the headerBytes
     */
    public int[] getHeaderBytes() {
        return headerBytes;
    }

    /**
     * @return the sectorSize
     */
    public int getSectorSize() {
        return sectorSize;
    }

    /**
     * @return the paragraphsLo
     */
    public int getParagraphsLo() {
        return paragraphsLo;
    }

    /**
     * @return the parahraphsHi
     */
    public int getParahraphsHi() {
        return parahraphsHi;
    }

    /**
     * @return the crc
     */
    public long getCrc() {
        return crc;
    }

    /**
     * @return the flagByte
     */
    public int getFlagByte() {
        return flagByte;
    }
    
    public int[] getSectorData(int number) {
        if (number<1) {
            throw new ArrayIndexOutOfBoundsException("Sector number less than 1");
        }
        else {
            return sectors.get(number-1);
        }
    }

    
}
