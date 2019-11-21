package hexdump;


public class HexDumpStream {
    
    private final int groupSize;
    private final int dataDigits;
    private final String dataSeparator;
    private final String offsetSeparator;
    private final String charSeparator;
    private final boolean offsets;
    private final boolean chars;
    
    private final StringBuilder sb;
    private final StringBuilder lineCharBuffer;
    private final String ln;
    
    private int lineCounter;
    private int dataCounter;
    
    private final String offsetFormat;
    private final String dataFormat;
    private final int initialOffset;
    
    private final static String EXTRA_ALLOWED_CHARS = " _+-*/@!#$%^&*()[],<.>:;?\"'";
    
    public HexDumpStream(int groupSize,int dataDigits,int offsetDigits,String dataSeparator,String offsetSeparator,String charSeparator, boolean offsets,boolean chars,int initialOffset) {
        this.groupSize=groupSize;
        this.dataDigits=dataDigits;
        this.dataSeparator=dataSeparator;
        this.offsetSeparator=offsetSeparator;
        this.charSeparator=charSeparator;
        this.offsets=offsets;
        this.chars=chars;
        this.initialOffset=initialOffset;
        
        sb = new StringBuilder();
        lineCharBuffer = new StringBuilder();
        
        ln = System.getProperty("line.separator");
        lineCounter=0;
        dataCounter=initialOffset;
        
        
        dataFormat="%0"+dataDigits+"X";
        offsetFormat="%0"+offsetDigits+"X";
                
        
    }
    
    public void add(int value) {
        
        /*Deal with the initial offset*/
        if (lineCounter==0 && initialOffset>0) {
            
            /*Determine the closest lower one*/
            int origOffset = initialOffset - (initialOffset % groupSize);
            if (offsets==true) {
                sb.append(String.format(offsetFormat+offsetSeparator, origOffset));
            }
            /*Add spaces*/
            int dataSize = dataDigits + dataSeparator.length();
            for (int k = 0; k < (initialOffset%groupSize) * dataSize; k++) {
                sb.append(' ');
            }
            lineCounter = initialOffset;
            
        }
        
        /*If we are about to write first data of the new line*/
        if ((lineCounter % groupSize)==0) {
            
            if (lineCounter != 0) {
                /*Place character dump if needed*/
                if (chars == true) {
                    sb.append(charSeparator);
                    sb.append(lineCharBuffer.toString());
                }

                /*Add new line*/
                sb.append(ln);
            }
            
            /*Add the offsets if needed*/
            if (offsets==true) {
                sb.append(String.format(offsetFormat+offsetSeparator, lineCounter));
            }
            
            /*Clear the line buffer*/
            lineCharBuffer.setLength(0);
            
        }
        
        /*Now the data - hex digits*/
        sb.append(String.format(dataFormat+dataSeparator,value));
        
        if (chars==true) {
            /*Character reasonable to be displayed?*/
            char c = (char)value;
            if (c>128) c-=128;
            
            /*If displayable*/
            if (Character.isLetterOrDigit(c) || EXTRA_ALLOWED_CHARS.indexOf(c)!=-1) {
                lineCharBuffer.append(c);
            }
            else {
                lineCharBuffer.append('.');
            }
        }
        
        /*Keep counting*/
        lineCounter++;
        
        
    }
    
    public void add(int[] values) {
        for (int v:values) {
            add(v);
        }
    }
    
    
    public String flush() {
        
        if (chars == true) {

            int remainder = groupSize-lineCharBuffer.length();
            int dataSize = dataDigits + dataSeparator.length();

            for (int k = 0; k < remainder * dataSize; k++) {
                sb.append(' ');
            }

            sb.append(charSeparator);
            sb.append(lineCharBuffer);
        }
        
        sb.append(ln);
        return sb.toString();
    }
    
    public void reset() {
        lineCounter=0;
        sb.setLength(0);
        lineCharBuffer.setLength(0);
        dataCounter=initialOffset;
    }
    
}
