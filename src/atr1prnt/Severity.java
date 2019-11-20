package atr1prnt;


public enum Severity {
    
    SEV_INFO(0),SEV_WARNING(4),SEV_ERROR(8),SEV_FATAL(12);
    
    private final int severity;
    
    Severity(int sev) {
        this.severity=sev;
    }
    
    int getSeverity() {
        return severity;
    }
    
    boolean isGreaterThan(Severity sev) {
        return severity>sev.severity;
    }
    
}
