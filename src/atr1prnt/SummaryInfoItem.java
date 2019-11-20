package atr1prnt;


public class SummaryInfoItem {
    
    private final Severity severity;
    private final String origin;
    private final String message;
    
    public SummaryInfoItem(Severity sev,String origin,String message) {
        severity=sev;
        this.origin=origin;
        this.message=message;
    }
    
    @Override
    public String toString() {
        return origin+":"+message+" ("+severity+")";
    }
    
    public boolean equals(SummaryInfoItem item) {
        if (origin.equals(item.origin)
                && message.equals(item.message)
                && severity == item.severity) {

            return true;
        }
        return false;
    }

    Severity getSeverity() {
        return severity;
    }
    
}
