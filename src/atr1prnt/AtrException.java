package atr1prnt;


public class AtrException extends Exception {

    private final String message;
    private final Severity severity;
    
    public AtrException(String message,Severity sev) {
        this.message=message;
        this.severity=sev;
    }
    public AtrException(String message) {
        this(message,Severity.SEV_FATAL);
    }

    
    @Override
    public String getMessage() {
       
        String pfx;
        
        switch (severity) {
            case SEV_INFO: {
                pfx="INFO";
                break;
            }
            case SEV_WARNING: {
                pfx="WARNING";
                break;
            }
            case SEV_ERROR: {
                pfx="ERROR";
                break;
            }
            case SEV_FATAL: {
                pfx="FATAL";
                break;
            }
            default: {
                pfx="UNACT";
            }
        }
        
        return pfx+": "+message;
    }
    
}
