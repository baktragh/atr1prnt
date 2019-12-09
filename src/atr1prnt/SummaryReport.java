
package atr1prnt;

import java.io.PrintStream;
import java.util.ArrayList;

public class SummaryReport {

    private final String fileName;
    
    private static class SummaryItemCrate {
        SummaryInfoItem item;
        int count;
        SummaryItemCrate(SummaryInfoItem it,int count) {
            item=it;
            this.count=count;
        }
    } 
    
    private final ArrayList<SummaryItemCrate> summaryItems;
    
    
    public SummaryReport(String fileName) {
        summaryItems = new ArrayList<>();
        this.fileName=fileName;
    }
    
    public void addItem(SummaryInfoItem it) {
        
        /*If the item already exists, just increment count*/
        for(SummaryItemCrate ii:summaryItems) {
            if (it.equals(ii.item)) {
                ii.count++;
                return;
            }
        }
        
        /*Otherwise add the item*/
        SummaryItemCrate ii = new SummaryItemCrate(it,1);
        summaryItems.add(ii);
    }
    
    public Severity getMaxSeverity() {
        
        Severity max = Severity.SEV_INFO;
        
        for (SummaryItemCrate ic:summaryItems) {
            if (ic.item.getSeverity().isGreaterThan(max)) {
                max=ic.item.getSeverity();
            }
        }
        
        return max;
    }
    
    public void printSummary(PrintStream ps,DumpUtilities utils) {
        
        ps.println();
        String fn = fileName;
        if (fn.length()>48) fn=fn.substring(0,44)+"...";
        
        utils.printHeader(ps, "SUMMARY REPORT "+fn, '=', true, true);
        
        for (SummaryItemCrate ic:summaryItems) {
            ps.println(ic.item.toString()+" ["+ic.count+"]");
        }
        
        if (summaryItems.isEmpty()==true) {
            ps.println("Nothing to report");
        }
    }
    
    
}
