/*
 * Debug.java
 *
 * Created on February 21, 2004, 7:28 PM
 *
 * Copyright (c) 2004 Stephen Olesen <slepp@netmonks.ca>
 */

package ca.netmonks.j2me;

/**
 * A static debugging class for J2ME applications.
 *
 * @author  Stephen Olesen
 * @version $Revision: 1.5 $
 */
public final class Debug {
    
    /** Whether or not to even enable debugging */
    public static final boolean ENABLED = false;
    
    /** Whether or not to send logs via IP */
    private static final boolean LOG_INTERNET = false;
    
    /** Whether or not to log to stdout */
    private static final boolean LOG_STDOUT = true;
    
    /** Log level */
    private static int logLevel = 15;
    
    public static void setLogLevel(int level) {
        logLevel = level;
    }
    
    public static void log(String msg) {
        log(0, msg);
    }
    
    public static void log(int level, String msg) {
        if(ENABLED && level <= logLevel) {
            if(LOG_INTERNET) {
                // Do IP logging here
            }
            
            if(LOG_STDOUT) {
                System.out.println("[" + Thread.currentThread().toString() + " " + level + " " + System.currentTimeMillis() + "] " + msg);
            }
        }
    }
    
    public static void exception(Exception e, String msg) {
        if(!ENABLED)
            return;
        
        log(0, "[Exception] " + msg + ": " + e.toString() + ": " + e.getMessage());
        
        if(LOG_STDOUT)
            e.printStackTrace();
    }
}
