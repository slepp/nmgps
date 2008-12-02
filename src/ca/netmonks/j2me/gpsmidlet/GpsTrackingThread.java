/*
 * GpsTrackingThread.java
 *
 * Created on January 27, 2004, 9:06 PM
 *
 * Copyright (c) 2004 Stephen Olesen <slepp@netmonks.ca>
 */

package ca.netmonks.j2me.gpsmidlet;

import ca.netmonks.iden.gps.GPS;
import ca.netmonks.j2me.Debug;
import com.motorola.iden.position.PositionConnection;
import com.motorola.iden.position.PositionDevice;

/**
 * This is the state machine that controls the GPS unit and provides
 * status about what is happening. It is responsible for causing the
 * GPS to provide new updates and for timing those updates.
 *
 * @author  Stephen Olesen <slepp@netmonks.ca>
 * @version $Revision: 1.10 $
 */
public final class GpsTrackingThread extends Thread {
    /** This thread instance */
    private static GpsTrackingThread instance = null;
    
    /** The GPS Interface */
    private GPS gps;
    
    /** Constant execution flag */
    private volatile boolean keepRunning = true;
    
    /** Status ImageLabel */
    private String status = "N/A";
    
    /** States */
    private static final char ACQUIRE = 1;
    private static final char TRACK = 2;
    private static final char LOST = 3;
    private static final char BATTERY_LOW = 4;
    
    /** Current state */
    private char state = ACQUIRE;
    
    /** Number of lost tracking updates */
    private short lost = 0;
    
    /** Whether or not we're really tracking */
    private boolean isTracking = true;
    
    /** Create a new instance of the GPS tracking state machine */
    public GpsTrackingThread() {
        if(Debug.ENABLED)
            Debug.log(10, "GpsTrackingThread Created");
        
        instance = this;
        
        gps = GPS.getInstance();
        
        start();
        
        if(Debug.ENABLED)
            Debug.log(8, "GpsTrackingThread Started");
    }
    
    // This is the actual state machine for the GPS tracking thread.
    // Within this, it tracks what exactly is going on with the GPS signals
    // and acts appropriately. It doesn't actually deal with the position
    // itself.
    public void run() {
        while(keepRunning) {
            if(Debug.ENABLED)
                Debug.log(10, "In State: " + (int)state);
            
            switch(state) {
                // We're tracking at the moment, we have a signal
                case TRACK:                    
                    if(Debug.ENABLED)
                        Debug.log(8, "GpsTrackingThread.run(): Beginning of TRACK");
                    
                    try {
                        Thread.sleep(7500); // Wait 7.5 seconds between requests
                    } catch(InterruptedException ie) {
                        requestStop();
                    }
                    
                    if(isTracking)
                        status = "Tracking...";
                    else
                        status = "Seeking Signal...";
                    
                    if(Debug.ENABLED)
                        Debug.log(10, "GpsTrackingThread.run(): Getting update...");
                    
                    // Debatable whether delay=high;fix=extended is better...
                    // Than delay=high or delay=low, that is
                    if(!gps.getUpdate("delay=high;fix=extended")) {
                        if(Debug.ENABLED)
                            Debug.log(10, "GpsTrackingThread.run(): getUpdate failed");
                        
                        switch(gps.getStatus()) {
                            // These states are for when we lose a signal
                            case PositionConnection.POSITION_RESPONSE_ERROR:
                            case PositionConnection.POSITION_NO_RESPONSE:
                            case PositionDevice.FIX_NOT_ATTAINABLE:
                            case PositionDevice.FIX_NOT_ATTAIN_ASSIST_DATA_UNAV:
                            case PositionDevice.ACC_NOT_ATTAIN_ASSIST_DATA_UNAV:
                            case PositionDevice.ACCURACY_NOT_ATTAINABLE:
                                if(++lost > 5) { // After 5 failures, go into LOST
                                    state = LOST;
                                }
                                isTracking = false;
                                status = "No Signal";
                                
                                if(Debug.ENABLED)
                                    Debug.log(10, "GpsTrackingThread.run(): No Position Available");
                                break;
                                
                            // The battery is too low for a GPS fix, so go into
                            // the associated state
                            case PositionDevice.BATTERY_TOO_LOW:
                                state = BATTERY_LOW;
                                status = gps.getStatusString();
                                
                                if(Debug.ENABLED)
                                    Debug.log(10, "GpsTrackingThread.run(): Battery too low");
                                break;
                                
                            // The following states make us quit
                            case PositionDevice.ALMANAC_OUT_OF_DATE:
                            case PositionDevice.GPS_CHIPSET_MALFUNCTION:
                            case PositionConnection.POSITION_RESPONSE_RESTRICTED:
                            case PositionConnection.POSITION_RESPONSE_NO_ALMANAC_OVERRIDE:
                                status = gps.getStatusString();
                                try {
                                    gps.disconnect();
                                } catch(Exception e) {
                                    if(Debug.ENABLED)
                                        Debug.exception(e, "GpsTrackingThread.run()/disconnect");
                                }
                                keepRunning = false;
                                
                                if(Debug.ENABLED)
                                    Debug.log(10, "GpsTrackingThread.run(): Permanent failure.");
                                break;
                            
                            // Something else happened
                            default:
                                if(Debug.ENABLED)
                                    Debug.log(9, "GpsTrackingThread.run(): Unknown Problem");
                                status = "Unknown Problem";
                                break;
                        }
                        
                        // Check if we've lost any signals
                        if(lost > 0) {
                            // Use static strings here for speed and to
                            // reduce automatic object instantiation
                            switch(lost) {
                                case 1: status = "No Location (1)"; break;
                                case 2: status = "No Location (2)"; break;
                                case 3: status = "No Location (3)"; break;
                                case 4: status = "No Location (4)"; break;
                                case 5: status = "No Location (5)"; break;
                                default: status = "No Location"; break;
                            }
                        }
                    } else {
                        // We're still tracking
                        status = "Located.";
                        isTracking = true;
                    }
                    
                    if(Debug.ENABLED)
                        Debug.log(8, "GpsTrackingThread.run(): End of TRACK");
                    
                    break;
                    
                // Chip is freshly reset, acquire quickly
                case ACQUIRE:
                    if(Debug.ENABLED)
                        Debug.log(8, "GpsTrackingThread.run(): Beginning of ACQUIRE");
                    
                    status = "Acquiring signal...";
                    
                    // Failure of this doesn't stop us
                    gps.getUpdate("delay=low");
                    
                    // We go into tracking state right after this
                    state = TRACK;
                    
                    if(Debug.ENABLED)
                        Debug.log(8, "GpsTrackingThread.run(): End of ACQUIRE");
                    
                    break;
                    
                // Sleep for 15 seconds, then acquire state
                case LOST:
                    if(Debug.ENABLED)
                        Debug.log(8, "GpsTrackingThread.run(): Beginning of LOST");
                    
                    status = "Lost Signal";
                    
                    lost = 0;
                    
                    try {
                        Thread.sleep(15000); // Sleep 15 seconds for GPS reset
                    } catch(InterruptedException ie) {
                        if(Debug.ENABLED)
                            Debug.exception(ie, "GpsTrackingThread.run()/LOST");
                        requestStop();
                    }
                    
                    // Return to acquire a signal
                    state = ACQUIRE;
                    
                    if(Debug.ENABLED)
                        Debug.log(8, "GpsTrackingThread.run(): End of LOST");
                    
                    break;
                    
                // The battery has gone too low
                case BATTERY_LOW:
                    if(Debug.ENABLED)
                        Debug.log(8, "GpsTrackingThread.run(): Beginning of BATTERY_LOW");
                    
                    if(!gps.getUpdate("delay=low")) {
                        // The update failed, so check if we're still dead
                        if(gps.getStatus() == PositionDevice.BATTERY_TOO_LOW) {
                            status = "Battery Too Low";
                            // Wait 60 seconds before trying again
                            try {
                                Thread.sleep(60000);
                            } catch(InterruptedException ignored) {
                                requestStop();
                            }
                        } else {
                            // Otherwise, we're going into acquire
                            state = ACQUIRE;
                        }
                    } else {
                        // But the update worked, so, go into tracking
                        state = TRACK;
                    }
                    
                    if(Debug.ENABLED)
                        Debug.log(8, "GpsTrackingThread.run(): End of BATTERY_LOW");
                    
                    break;
            }
        }
    }
    
    /** Request the thread stops now. */
    public final void requestStop() {
        if(Debug.ENABLED)
            Debug.log(14, "GpsTrackingThread.requestStop() Called");
        
        keepRunning = false;
        instance = null;
    }
    
    /** Get the current status string of the thread */
    public final String getStatus() {
        if(Debug.ENABLED)
            Debug.log(15, "GpsTrackingThread.getStatus() Called");
        
        return status;
    }
    
    /** Return the current running thread instance */
    public static final GpsTrackingThread getInstance() {
        if(Debug.ENABLED)
            Debug.log(12, "GpsTrackingThread.getInstance() Entered");
        
        if(instance == null) {
            new GpsTrackingThread();
            if(Debug.ENABLED)
                Debug.log(10, "GpsTrackingThread.getInstance(): New instance of GpsTrackingThread created");
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "GpsTrackingThread.getInstance() Exited");
        return instance;
    }
}
