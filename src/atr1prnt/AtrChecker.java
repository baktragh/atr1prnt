package atr1prnt;

import java.io.PrintStream;
import java.util.Properties;

public interface AtrChecker {
    
    public void check(AtrFile atrFile,PrintStream pr,Properties props);
    public String getSectionName();
    
}
