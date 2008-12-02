/*
 * ExternalStatusCanvas.java
 *
 * Created on January 27, 2004, 6:42 PM
 *
 * Copyright (c) 2004 Stephen Olesen <slepp@netmonks.ca>
 */

package ca.netmonks.j2me.gpsmidlet.ui;

import ca.netmonks.iden.gps.GPS;
import ca.netmonks.iden.gps.Position;
import ca.netmonks.j2me.Debug;
import ca.netmonks.j2me.gpsmidlet.GpsTrackingThread;
import ca.netmonks.j2me.gpsmidlet.UpdateThread;
import com.motorola.iden.lcdui.ExternalDisplayCanvas;
import com.motorola.iden.position.AggregatePosition;
import com.nextel.ui.OHandset;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * The external display status screen.
 *
 * This handles displaying a list of strings to the external display. It is
 * scrollable by the user with the side up/down buttons.
 *
 * @author  Stephen Olesen <slepp@netmonks.ca>
 * @version $Revision: 1.9 $
 */
public final class ExternalStatusCanvas
extends ExternalDisplayCanvas {
    /** Motorola i730 Volume Up button */
    private static final int VOL_UP_KEY   = -51;
    /** Motorola i730 Volume Down button */
    private static final int VOL_DOWN_KEY = -52;
    /** Motorola i730 PTT Button */
    private static final int PTT_KEY      = -50;
    
    /** The maximum number of strings we'll support on the external display */
    private static final int MAX_ITEMS = 4;
    /** The current position in the list */
    private int position = 0;
    
    /** Whether we should do repaints when we get a new string */
    private boolean doRepaints = false;
    
    /** The possible text strings on the display */
    private String textStrings[] = { "GPS: No Status", "UDP: No Status", "Latitude Unavail.", "Longitude Unavail." };
    
    /** Font to use on the external display */
    private static final Font font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    
    /** The external update thread */
    private ExternalUpdateThread updates = null;
    
    /** Creates a new instance of ExternalStatusCanvas */
    public ExternalStatusCanvas() {
        if(Debug.ENABLED)
            Debug.log(10, "ExternalStatusCanvas Created");
    }
    
    /** Paint the screen to the graphics context.
     * @param g Graphics context
     */
    protected void paint(Graphics g) {
        if(Debug.ENABLED)
            Debug.log(14, "ExternalStatusCanvas.paint(Graphics) Entered");
        
        // Get the width and height
        int w = getWidth();
        int h = getHeight();
        
        // Fill in the background as blank
        g.setColor(0xffffff);
        g.fillRect(0,0, getWidth(), getHeight());
        
        // Draw the strings
        g.setColor(0x000000);
        g.setFont(font);
        g.drawString(textStrings[position], w/2, 0, Graphics.TOP | Graphics.HCENTER);
        g.drawString(textStrings[position+1], w/2, h/2, Graphics.TOP | Graphics.HCENTER);
        
        if(Debug.ENABLED)
            Debug.log(14, "ExternalStatusCanvas.paint(Graphics) Exited");
    }
    
    /** Set a string for display, repaint if necessary.
     * @param i Line to replace (0..MAX_ITEMS-1)
     * @param text The string itself
     */
    public void setString(int i, String text) {
        if(Debug.ENABLED)
            Debug.log(12, "ExternalStatusCanvas.setString(" + i + ", \"" + text + "\") Entered");
        
        // This verifies we really need to change the text
        if(this.textStrings[i] != text) {
            this.textStrings[i] = text;
            
            if(doRepaints)
                repaint();
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "ExternalStatusCanvas.setString(" + i + ", \"" + text + "\") Exited");
    }
    
    /** Handle keypresses to scroll the view
     * @param keyCode The keycode we received
     */
    protected void keyPressed(int keyCode) {
        if(Debug.ENABLED)
            Debug.log(12, "ExternalStatusCanvas.keyPressed(" + keyCode + ") Entered");
        
        if(keyCode == VOL_UP_KEY) {
            if(Debug.ENABLED)
                Debug.log(11, "ExternalStatusCanvas.keyPressed(): Scrolling UP");
            
            // Don't walk off the top of the list
            if(position > 0) {
                --position;
                repaint();
            } else OHandset.beep();
        } else if(keyCode == VOL_DOWN_KEY) {
            if(Debug.ENABLED)
                Debug.log(11, "ExternalStatusCanvas.keyPressed(): Scrolling DOWN");
            
            // Ensure at least two items are shown at all times
            if(position < (MAX_ITEMS-2)) {
                ++position;
                repaint();
            } else OHandset.beep();
        } else if(keyCode == PTT_KEY) {
            if(Debug.ENABLED)
                Debug.log(11, "ExternalStatusCanvas.keyPressed(): Sending Mark");
            
            // If the PTT is pressed, mark the position
            UpdateThread updateThread = UpdateThread.getInstance();
            if(updateThread != null) {
                updateThread.markPosition();
                OHandset.beep();
            }
        } else super.keyPressed(keyCode); // Call the parent
        
        if(Debug.ENABLED)
            Debug.log(12, "ExternalStatusCanvas.keyPressed(" + keyCode + ") Exited");
    }
    
    /** Handle when we are shown on the display. */
    protected void showNotify() {
        if(Debug.ENABLED)
            Debug.log(12, "ExternalStatusCanvas.showNotify() Entered");
        
        // Start the status update thread
        updates = new ExternalUpdateThread();
        
        // Enable repaints and do so
        doRepaints = true;
        repaint();
        
        super.showNotify();
        
        if(Debug.ENABLED)
            Debug.log(12, "ExternalStatusCanvas.showNotify() Exited");
    }
    
    /** Handle when we lose focus of the display */
    protected void hideNotify() {
        if(Debug.ENABLED)
            Debug.log(12, "ExternalStatusCanvas.hideNotify() Entered");
        
        // Stop the update thread
        if(updates != null) {
            updates.requestStop();
            updates = null;
        }
        
        // And disallow repaints
        doRepaints = false;
        
        super.hideNotify();
        
        if(Debug.ENABLED)
            Debug.log(12, "ExternalStatusCanvas.hideNotify() Exited");
    }
    
    /** This class handles updating the external panel when it is shown. */
    protected class ExternalUpdateThread
    extends Thread {
        /** The GPS thread, for status */
        private GpsTrackingThread gpsThread = null;
        /** The UDP thread, for status */
        private UpdateThread updateThread = null;
        /** The GPS interface to get our position */
        private GPS gps = null;
        
        /** Keep running or not */
        private volatile boolean keepRunning = true;
        
        /** Create an instance of the thread, and get our references */
        protected ExternalUpdateThread() {
            if(Debug.ENABLED)
                Debug.log(10, "ExternalStatusCanvas.ExternalUpdateThread Created");
            
            gpsThread = GpsTrackingThread.getInstance();
            
            updateThread = UpdateThread.getInstance();
            
            gps = GPS.getInstance();
            
            start();
        }
        
        /** Tick the timer here, and update the external panel display */
        public void run() {
            Position pos = null;
            while(keepRunning) {               
                if(Debug.ENABLED)
                    Debug.log(14, "ExternalStatusCanvas.ExternalUpdateThread Ticked");
            
                // First, GPS and UDP string status
                setString(0, gpsThread.getStatus());
                setString(1, updateThread.getStatus());
            
                try {
                    pos = gps.getPosition(10000);
                } catch(InterruptedException e) {
                    pos = null;
                    keepRunning = false;
                }
                
                // And then, if we have a position, update the current position
                if(pos != null) {
                    AggregatePosition ap = pos.getAggregatePosition();
                    setString(2, ap.getLatitude(AggregatePosition.DEG_MIN_SEC));
                    setString(3, ap.getLongitude(AggregatePosition.DEG_MIN_SEC));
                    pos = null; // Get rid of the reference
                }
            }
        } // end of run()
        
        /** Request that the thread terminates */
        public void requestStop() {
            keepRunning = false;
        }
    }
}
