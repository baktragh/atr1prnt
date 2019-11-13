package atr1prnt;

import java.io.PrintStream;
import java.util.Properties;


public class NoFSChecker implements AtrChecker {

    @Override
    public void check(AtrFile atrFile, PrintStream pr, Properties props) {
        
    }

    @Override
    public String getSectionName() {
        return "NO FILESYSTEM";
    }
    
}
