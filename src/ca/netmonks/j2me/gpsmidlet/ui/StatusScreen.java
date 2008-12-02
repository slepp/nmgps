/*
 * StatusScreen.java
 *
 * Created on January 27, 2004, 8:27 PM
 *
 * Copyright (c) 2004 Stephen Olesen <slepp@netmonks.ca>
 */

package ca.netmonks.j2me.gpsmidlet.ui;

import ca.netmonks.iden.gps.GPS;
import ca.netmonks.iden.gps.Position;
import ca.netmonks.j2me.Debug;
import ca.netmonks.j2me.gpsmidlet.GpsTrackingThread;
import ca.netmonks.j2me.gpsmidlet.UpdateThread;
import com.motorola.iden.position.AggregatePosition;
import com.nextel.ui.OAbstractScreen;
import com.nextel.ui.OCommandAction;
import com.nextel.ui.OSoftKey;
import com.nextel.ui.OUILook;
import javax.microedition.lcdui.Graphics;

/**
 * The current application/GPS/UDP status.
 *
 * @author  Stephen Olesen <slepp@netmonks.ca>
 * @version $Revision: 1.7 $
 */
public final class StatusScreen
extends OAbstractScreen {
    /** The status screen update thread */
    private StatusUpdateThread statusUpdateThread = null;
    
    /** The actual strings to print */
    private String[] strings = { "GPS", "UDP", "Lat", "Lon", "Spd", "Alt", "Sat" };
    
    /** Creates a new instance of StatusScreen */
    public StatusScreen() {
        // Set our title and layout
        super("NetMonks GPS Status", OUILook.BOLD_SMALL);
        
        if(Debug.ENABLED)
            Debug.log(10, "StatusScreen Created");
        
        // Initialize the components
        init();
    }
    
    /** Initialize the displays and threads */
    private void init() {
        if(Debug.ENABLED)
            Debug.log(12, "StatusScreen.init() Entered");
        
        // Go back function
        OSoftKey backKey = new OSoftKey("BACK");
        backKey.setAction(new OCommandAction() {
            public void performAction() {
                Navigator.goBack();
            }
        });
        
        // Allow the user to mark their position with the server
        OSoftKey markKey = new OSoftKey("MARK");
        markKey.setAction(new OCommandAction() {
            public void performAction() {
                UpdateThread updateThread = UpdateThread.getInstance();
                if(updateThread != null) {
                    updateThread.markPosition();
                }
            }
        });
        
        // Add the two buttons
        addSoftKey(backKey, Graphics.RIGHT);
        addSoftKey(markKey, Graphics.LEFT);
        
        if(Debug.ENABLED)
            Debug.log(12, "StatusScreen.init() Exited");
    }
    
    /** When shown, start the update thread. */
    protected void showNotify() {
        if(Debug.ENABLED)
            Debug.log(12, "StatusScreen.showNotify() Entered");
        
        statusUpdateThread = new StatusUpdateThread(2500);
        super.showNotify();
        
        if(Debug.ENABLED)
            Debug.log(12, "StatusScreen.showNotify() Exited");
    }
    
    /** When hidden, kill the update thread. */
    protected void hideNotify() {
        if(Debug.ENABLED)
            Debug.log(12, "StatusScreen.hideNotify() Entered");
        
        if(statusUpdateThread != null) {
            statusUpdateThread.keepRunning = false;
            statusUpdateThread = null;
        }
        super.hideNotify();
        
        if(Debug.ENABLED)
            Debug.log(12, "StatusScreen.hideNotify() Exited");
    }
    
    protected int getScrollDirections() {
        return 0;
    }
    
    protected void paintBody(Graphics g) {
        if(Debug.ENABLED)
            Debug.log(12, "StatusScreen.paintBody(Graphics) Entered");
        
        g.setColor(0xffffff);
        g.fillRect(0, 0, getWidth(), getBodyHeight());
        
        int yInc = OUILook.BOLD_SMALL.getHeight();
        int y = getBodyRow();
        
        g.setColor(0x000000);
        g.setFont(OUILook.BOLD_SMALL);
        
        g.drawString("GPS:", 1, y, Graphics.LEFT | Graphics.TOP);
        y += yInc + 2;
        g.drawString("UDP:", 1, y, Graphics.LEFT | Graphics.TOP);
        y += yInc + 2;
        g.drawString("Lat:", 1, y, Graphics.LEFT | Graphics.TOP);
        y += yInc + 2;
        g.drawString("Lon:", 1, y, Graphics.LEFT | Graphics.TOP);
        y += yInc + 2;
        g.drawString("Spd:", 1, y, Graphics.LEFT | Graphics.TOP);
        y += yInc + 2;
        g.drawString("Alt:", 1, y, Graphics.LEFT | Graphics.TOP);
        y += yInc + 2;
        g.drawString("Sat:", 1, y, Graphics.LEFT | Graphics.TOP);
        
        g.setFont(OUILook.PLAIN_SMALL);
        y = getBodyRow();
        int x = OUILook.BOLD_SMALL.stringWidth("WWW: ");
        for(int i = 0; i < 7; i++) {
            g.drawString(strings[i], x, y, Graphics.LEFT | Graphics.TOP);
            y += yInc + 2;
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "StatusScreen.paintBody(Graphics) Exited");
    }
    
    /** Provide for updates to the screen periodically. */
    private class StatusUpdateThread
    extends Thread {
        /** Whether or not to keep running */
        protected volatile boolean keepRunning = true;
        
        /** The update thread */
        private UpdateThread updateThread;
        
        /** The GPS thread */
        private GpsTrackingThread gpsThread;
        
        /** The GPS interface */
        private GPS gps;
        
        /** The interval to produce updates at */
        private int interval;
        
        /** Create a new instance of the screen updater. */
        public StatusUpdateThread(int interval) {
            if(Debug.ENABLED)
                Debug.log(12, "StatusScreen.StatusUpdateThread Created");
            
            this.interval = interval;
            
            // Get the GPS thread handle
            gpsThread = GpsTrackingThread.getInstance();
            
            // Get the UDP thread handle
            updateThread = UpdateThread.getInstance();
            
            // Get the GPS controller handle
            gps = GPS.getInstance();
            
            // Start the thread running
            start();
            
            if(Debug.ENABLED)
                Debug.log(8, "StatusScreen.StatusUpdateThread Started");
        }
        
        /** Run the thread itself */
        public void run() {
            Position pos = null;
            while(keepRunning) {
                if(Debug.ENABLED)
                    Debug.log(15, "StatusScreen.StatusUpdateThread Updated");
                
                strings[0] = gpsThread.getStatus();
                strings[1] = updateThread.getStatus();
                
                // Wait for the next update
                try {
                    if(Debug.ENABLED)
                        Debug.log(16, "StatusScreen.StatusUpdateThread.run(): Waiting for new position");
                    pos = gps.getPosition(interval);
                    if(Debug.ENABLED)
                        Debug.log(16, "StatusScreen.StatusUpdateThread.run(): Received new position");
                } catch(InterruptedException e) {
                    if(Debug.ENABLED)
                        Debug.exception(e, "StatusScreen.StatusUpdateThread.run()/getPosition");
                    
                    // Upon interruption, let's just exit
                    keepRunning = false;
                }
                
                if(pos != null) {
                    AggregatePosition ap = pos.getAggregatePosition();
                    strings[2] = ap.getLatitude(AggregatePosition.DEG_MIN_SEC);
                    strings[3] = ap.getLongitude(AggregatePosition.DEG_MIN_SEC);
                    strings[4] = String.valueOf(ap.getSpeed()) + " km/h";
                    strings[5] = String.valueOf(ap.getAltitude()) + " m";
                    strings[6] = String.valueOf(ap.getNumberOfSatsUsed()) + " satellites";
                    pos = null;
                }
                
                repaint();
                
            } // end of while
            
        } // end of run
        
    } // end of StatusUpdateThread class
    
} // end of StatusScreen class
