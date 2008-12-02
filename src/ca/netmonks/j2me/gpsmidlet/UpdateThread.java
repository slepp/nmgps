/*
 * UpdateThread.java
 *
 * Created on February 21, 2004, 5:48 PM
 *
 * Copyright (c) 2004 Stephen Olesen <slepp@netmonks.ca>
 */

package ca.netmonks.j2me.gpsmidlet;

import ca.netmonks.iden.gps.GPS;
import ca.netmonks.iden.gps.Position;
import ca.netmonks.j2me.Debug;
import java.util.*;
import ca.netmonks.j2me.net.*;
import java.io.IOException;

/**
 * The update monitoring thread which uses an UpdateHandler to send updates.
 *
 * @author  Stephen Olesen <slepp@netmonks.ca>
 * @version $Revision: 1.9 $
 */
public final class UpdateThread extends Thread {
    /** A common instance of this thread */
    private static UpdateThread instance = null;
    
    /** Flag to keep running */
    private volatile boolean keepRunning = true;
    
    /** Flag to suspend the UDP updates */
    private volatile boolean suspended = false;
    
    /** Flag to force updates on every position update */
    private volatile boolean force_update = false;
    
    /** Update interval in milliseconds */
    private volatile int updateInterval = 120000;
    
    /** Forced timeout on GPS position updates, to ensure we still do network I/O */
    private volatile int waitTimeout = updateInterval * 2;
    
    /** The actual update handler (TCP or UDP or such) */
    private UpdateHandler updateHandler;
    
    /** The status string */
    private String status = "N/A";
    
    /** Send an alive beacon every so often */
    private Timer aliveBeacon = new Timer();
    
    /** The time of the last update */
    private long lastUpdate = 0;
    
    /** The last known position from getPosition */
    private Position pos = null;
    
    /** The list of positions that have been recorded */
    private Vector positionList = new Vector(16);
    
    /** The actual IO is done in another thread yet */
    private volatile UpdateThreadIO ioThread = null;
    
    /** Create a new instance of the Update Thread for network updates of positions */
    public UpdateThread() {
        if(Debug.ENABLED)
            Debug.log(10, "UpdateThread Created");
        
        // Record this instance
        instance = this;
        
        // Use set target to create the handler
        setTarget(Settings.get(Settings.UPDATE_TARGET, "gps-netmonks.ca:8937"));
        
        // Schedule the Alive Beacon to ping every few minutes (15)
        aliveBeacon.schedule(new TimerTask() {
            public void run() {
                if(updateHandler != null)
                    updateHandler.sendBeacon();
            }
        }, 900000, 900000);
        
        if(Debug.ENABLED)
            Debug.log(12, "UpdateThread(): Scheduled beacon");
        
        // Start this thread
        start();
        
        if(Debug.ENABLED)
            Debug.log(10, "UpdateThread Started");
    }
    
    /** Set the current network target for sending updates, and open the handler */
    public void setTarget(String target) {
        if(Debug.ENABLED)
            Debug.log(12, "UpdateThread.setTarget(String) Entered");
        
        // If we have an update handler, close it off before continuing
        if(updateHandler != null) {
            try {
                updateHandler.close();
            } catch(IOException e) {
                /* ignored, since we're just replacing it */
                if(Debug.ENABLED)
                    Debug.exception(e, "UpdateThread.setTarget()/close");
            }
        }
        
        // Signal the server we're still alive and kicking every 15 minutes
        if("tcp".equals(Settings.get(Settings.UPDATE_METHOD, "tcp"))) {
            updateHandler = new TcpHandler(Settings.get(Settings.UPDATE_TARGET, "gps-update.netmonks.ca:8937"),
            Settings.get(Settings.USER_ID, "anonymous"),
            Settings.get(Settings.USER_PASSWORD, "anonymous"));
        } else {
            updateHandler = new UdpHandler(Settings.get(Settings.UPDATE_TARGET, "gps-update.netmonks.ca:8937"),
            Settings.get(Settings.USER_ID, "anonymous"),
            Settings.get(Settings.USER_PASSWORD, "anonymous"));
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "UpdateThread.setTarget(String) Exited");
    }
    
    /** Set the update interval and wait timeout */
    public void setUpdateInterval(int interval) {
        if(Debug.ENABLED)
            Debug.log(12, "UpdateThread.setUpdateInterval(int) Entered");
        
        updateInterval = interval;
        waitTimeout = interval * 2;
        
        if(Debug.ENABLED)
            Debug.log(12, "UpdateThread.setUpdateInterval(int) Exited");
    }
    
    /** Toggled forced updates */
/*    public final boolean forceUpdate() {
        if(Debug.ENABLED)
            Debug.log(12, "UpdateThread.forceUpdate() Entered");
 
        force_update = !force_update;
 
        if(Debug.ENABLED) {
            Debug.log(11, "UpdateThread.forceUpdate(): Updates are now: " + force_update);
            Debug.log(12, "UpdateThread.forceUpdate() Exited");
        }
 
        return force_update;
    }*/
    
    /** Mark the current position in the database */
    public void markPosition() {
        if(Debug.ENABLED)
            Debug.log(12, "UpdateThread.markPosition() Entered");
        
        updateHandler.sendMark(pos);
        
        if(Debug.ENABLED)
            Debug.log(12, "UpdateThread.markPosition() Exited");
    }
    
    /** Run the update thread in a loop until keepRunning is false */
    public void run() {
        if(Debug.ENABLED)
            Debug.log(12, "UpdateThread.run() Entered");
        
        // A handle to the GPS controller
        GPS gps = GPS.getInstance();
        
        // Acknowledge that we started
        updateHandler.sendHello();
        
        status = "First update pending...";
        
        // Last sequence number
        int lastSequence = -1;
        
        while(keepRunning) {
            if(Debug.ENABLED)
                Debug.log(10, "UpdateThread.run(): Getting new position...");
            
            // Get the position, or timeout.. Whatever happens first
            try {
                pos = gps.getPosition(waitTimeout);
                if(Debug.ENABLED)
                    Debug.log(10, "UpdateThread.run(): Received new position notification.");
                
                // Otherwise, append it to the queue
                // Be sure to check that the new position isn't the same as the last one
                // We used to check if pos.isValid(), but it is now guaranteed to be valid
                if(pos != null && pos.getSequence() != lastSequence) {
                    if(Debug.ENABLED)
                        Debug.log(10, "UpdateThread.run(): Valid position added to list");
                    
                    positionList.addElement(pos);
                    lastSequence = pos.getSequence(); // Update the last appended sequence
                }
                
                // If we have updates, and the time has expired (or forced)                
                if(!positionList.isEmpty() &&
                   ((System.currentTimeMillis() - lastUpdate) >= updateInterval || force_update)) {
                    if(Debug.ENABLED)
                        Debug.log(10, "UpdateThread.run(): Sending position updates");
                    
                    // Make a new ioThread if we can... Otherwise, updates remain pending
                        if(Debug.ENABLED)
                            Debug.log(12, "Checking ioThread existence...");
                        if(ioThread == null)
                            ioThread = new UpdateThreadIO();
                    
                        if(!ioThread.isBusy()) {
                            // Create the new IO background thread to handle persistent/long updates
                            ioThread.setList(positionList);
                            
                            if(Debug.ENABLED)
                                Debug.log(12, "ioThread created, clearing list.");
                            
                            // We've passed off the list of updates, so drop it now
                            positionList = new Vector(positionList.size());
                            
                            // We'll record lastUpdate here, so we don't try to create a thread
                            // too soon afterwards.
                            lastUpdate = System.currentTimeMillis();
                        }
                }
            } catch (InterruptedException ie) {
                if(Debug.ENABLED)
                    Debug.log(10, "UpdateThread.run(): Thread sleep interrupted");
            }
            
        } // End of while keepRunning
        
        if(Debug.ENABLED)
            Debug.log(10, "UpdateThread.run(): Left the main loop");
        
        // Notify our exit, just for fun
        updateHandler.sendGoodbye();
        
        if(Debug.ENABLED)
            Debug.log(12, "UpdateThread.run() Exited");
    }
    
    private class UpdateThreadIO
    extends Thread {
        private Vector positions = null;
        private boolean success = false;
        private boolean busy = false;
        private volatile boolean keepRunning = true;
        
        private UpdateThreadIO() {
            start();
        }
        
        public boolean isBusy() {
            return busy;
        }
        
        public void setList(Vector list) {
            this.positions = list;
            synchronized(this) {
                notify();
            }
        }
        
        public void run() {
            while(keepRunning) {
                try {
                    synchronized(this) {
                        wait();
                    }
                    if(positions != null && !positions.isEmpty())
                        performUpdate();
                } catch(InterruptedException ie) {
                    if(Debug.ENABLED)
                        Debug.exception(ie, "UpdateThread.UpdateThreadIO.run()");
                }
            }
        }
        
        private void performUpdate() {
            // Mark us as busy
            busy = true;
            
            int sleepTime = 500;
            do {
                try {
                    status = "Sending updates...";

                    // Attempt to send all our updates
                    updateHandler.sendUpdates(positions);

                    // Remove everything if it worked fine
                    positions = null;

                    // Update our status strings
                    Calendar c = Calendar.getInstance(TimeZone.getDefault());
                    status = c.toString();
                    
                    success = true;
                } catch(UpdateException ue) {
                    status = (ue.getMessage() != null ? ue.getMessage() : "Update Failed");
                    
                    try {
                        Thread.sleep(sleepTime);
                    } catch(InterruptedException ie) {
                        /* ignored */
                    }
                    
                    // Backoff exponentionally, or until we reach waitTimeout
                    sleepTime = sleepTime * 2;
                    if(sleepTime > waitTimeout)
                        sleepTime = waitTimeout;
                    
                    if(Debug.ENABLED)
                        Debug.log(8, "Failed to connect. Sleep time now " + sleepTime + "ms");
                }
            } while(!success);
            busy = false;
        }
    }
    
    /** Request that the instance ends, and stop the beacons,
     * and set the instance to null */
    public final void requestStop() {
        if(Debug.ENABLED)
            Debug.log(12, "UpdateThread.requestStop() Entered");
        
        aliveBeacon.cancel();
        keepRunning = false;
        instance = null;
        
        if(Debug.ENABLED)
            Debug.log(12, "UpdateThread.requestStop() Exited");
    }
    
    /** Get the current UDP update thread status string. */
    public final String getStatus() {
        if(Debug.ENABLED)
            Debug.log(15, "UpdateThread.getStatus() Called");
        
        return status;
    }
    
    /** Return the current running instance */
    public final static UpdateThread getInstance() {
        if(Debug.ENABLED)
            Debug.log(12, "UpdateThread.getInstance() Entered");
        
        if(instance == null) {
            if(Debug.ENABLED)
                Debug.log(10, "UpdateThread.getInstance(): Creating a new instance");
            new UpdateThread();
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "UpdateThread.getInstance() Exited");
        return instance;
    }
    
    /** Get a handle to the UpdateHandler instance we're using */
    public final UpdateHandler getHandler() {
        if(Debug.ENABLED)
            Debug.log(15, "UpdateThread.getHandler() Called");
        
        return this.updateHandler;
    }
}
