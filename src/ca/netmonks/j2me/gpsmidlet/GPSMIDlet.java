/*
 * GPSMIDlet.java
 *
 * Created on January 15, 2004, 5:00 AM
 *
 * Copyright (c) 2004 Stephen Olesen <slepp@netmonks.ca>
 */

package ca.netmonks.j2me.gpsmidlet;

import ca.netmonks.iden.gps.GPS;
import ca.netmonks.j2me.ui.*;
import ca.netmonks.j2me.Debug;
import ca.netmonks.j2me.gpsmidlet.ui.*;
import com.motorola.iden.lcdui.ExternalDisplay;
import com.motorola.lwt.Component;
import com.motorola.lwt.ComponentScreen;
import com.motorola.lwt.ImageLabel;
import com.nextel.ui.OHandset;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Font;
import javax.microedition.midlet.MIDlet;

/**
 * The NetMonks GPS MIDlet for the Motorola i730 phone.
 *
 * This program is designed to monitor and send updates to a remote server
 * as to the current GPS readings for the builtin GPS in the phone.
 *
 * @author  Stephen Olesen <slepp@netmonks.ca>
 * @version $Revision: 1.16 $
 */
public final class GPSMIDlet extends MIDlet {
    
    /** The update spooling/sending thread */
    private UpdateThread updateThread = null;
    
    /** The GPS state machine */
    private GpsTrackingThread gpsTrackingThread = null;
    
    /** The GPS */
    private GPS gps = null;
    
    /** True if the app has been started */
    private boolean started = false;
    
    /** Primary Display */
    Display display = null;
    
    /** External Display */
    ExternalDisplay externalDisplay = null;
    
    /** The status screen */
    StatusScreen statusScreen = null;
    
    /** The settings screen */
    SettingsScreen settingsScreen = null;
    
    /** The menu screen */
    MenuScreen menuScreen = null;
    
    /** The external display */
    ExternalStatusCanvas exDisplay = null;
    
    /** The user ID to send in packets */
    private String userId = null;
    
    /** The user password to send in packets */
    private String userPassword = null;
    
    /** The update target (datagram://host:port) */
    private String updateTarget = null;
    
    /** The UDP sending interval */
    private int updateInterval = 120000;
    
    /** If it is okay to send data at the moment */
    private boolean canSendData = true;
    
    /** Whether or not to show the menu first thing */
    private boolean showMenu = true;
    
    /** The constructor which does nothing at the moment. */
    public GPSMIDlet() {
        // Set the midlet for the handset
        OHandset.setMIDlet(this);        
        
        if(Debug.ENABLED)
            Debug.log(5, "GPSMIDlet Created");
    }
    
    /**
     * Start up the Hello MIDlet by creating the TextBox and associating
     * the exit command and listener.
     */
    public void startApp() {
        if(Debug.ENABLED)
            Debug.log(6, "GPSMIDlet.startApp() Entered");
        
        if(!started) {
            if(Debug.ENABLED)
                Debug.log(5, "GPSMIDlet.startApp(): First run, initializing...");
            
            // We've been started
            started = true;
            
            // If this returns true, then settings are OK, so show menu
            showMenu = loadSettings();
            
            // Get a handle to the GPS
            gps = GPS.getInstance();
        
            // Start the GPS tracking thread
            gpsTrackingThread = GpsTrackingThread.getInstance();
        
            // GPS updates started, so start the UDP
            updateThread = UpdateThread.getInstance();
            updateThread.setTarget(updateTarget);
            updateThread.setUpdateInterval(updateInterval);
            
            // Initialize the screens
            initDisplays();
            
            // Remove the title screen after 3 seconds, set it to the menu
            new Timer().schedule(new TimerTask() {
                public void run() {
                    Navigator.goForward(menuScreen);
                    if(!showMenu) {
                        Navigator.goForward(settingsScreen);
                    }
                }
            }, 2000);
        } else {
            // Release the display, if the flip was opened
            if(externalDisplay != null &&
               !externalDisplay.getFlipState()) {
                   externalDisplay.releaseDisplay();
            }
        }
        
        if(Debug.ENABLED)
            Debug.log(6, "GPSMIDlet.startApp() Exited");
    }
    
    /**
     * Pause is a no-op since there are no background activities or
     * record stores that need to be closed.
     */
    public void pauseApp() {
        if(Debug.ENABLED)
            Debug.log(6, "GPSMIDlet.pauseApp() Called");
        
        // If the flip was closed, request it
        if(externalDisplay != null &&
           externalDisplay.getFlipState()) {
            externalDisplay.requestDisplay();
        }
    }
    
    /**
     * Destroy must cleanup everything not handled by the garbage collector.
     * In this case there is nothing to cleanup.
     */
    public void destroyApp(boolean unconditional) {
        if(Debug.ENABLED)
            Debug.log(6, "GPSMIDlet.destroyApp(" + unconditional + ") Entered");
        if(gpsTrackingThread != null) {
            if(Debug.ENABLED)
                Debug.log(7, "GPSMIDlet.destroyApp(" + unconditional + "): Stopping GPS thread");
            gpsTrackingThread.requestStop();
        }
        
        if(updateThread != null) {
            if(Debug.ENABLED)
                Debug.log(7, "GPSMIDlet.destroyApp(" + unconditional + "): Stopping Update thread");
            updateThread.getHandler().sendGoodbye();
            updateThread.requestStop();
        }
        
        if(Debug.ENABLED)
            Debug.log(6, "GPSMIDlet.destroyApp(" + unconditional + ") Exited");
    }
    
    public boolean loadSettings() {
        if(Debug.ENABLED)
            Debug.log(12, "GPSMIDlet.loadSettings() Entered");
        
        boolean valid = false;
        /* Handle the settings */
        if(Settings.areValid()) {
            if(Debug.ENABLED)
                Debug.log(8, "GPSMIDlet.loadSettings(): Settings are valid");
            valid = true;
        }
        
        /* And set the local variables for each setting */
        updateInterval = Settings.getInt(Settings.UPDATE_INTERVAL, 120) * 1000;
        
        Settings.get(Settings.UPDATE_METHOD, "tcp");
        updateTarget = Settings.get(Settings.UPDATE_TARGET, "gps-update.netmonks.ca:8937");
        
        userId = Settings.get(Settings.USER_ID, "anonymous");
        userPassword = Settings.get(Settings.USER_PASSWORD, "anonymous");
        
        if(updateThread != null) {
            if(Debug.ENABLED)
                Debug.log(8, "GPSMIDlet.loadSettings(): Setting target/timing of update thread");
            updateThread.setTarget(updateTarget);
            updateThread.setUpdateInterval(updateInterval);
            if(updateThread.getHandler() != null) {
                updateThread.getHandler().setUsername(userId);
                updateThread.getHandler().setPassword(userPassword);
            }
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "GPSMIDlet.loadSettings() Exited");
        return valid;
    }
    
    /** Initialize all the displays */
    public void initDisplays() {
        if(Debug.ENABLED)
            Debug.log(12, "GPSMIDlet.initDisplays() Entered");
        
        display = Display.getDisplay(this);
        externalDisplay = ExternalDisplay.getDisplay(this);
        
        exDisplay = new ExternalStatusCanvas();
        externalDisplay.setCurrent(exDisplay);
        
        /* Title screen */
        ComponentScreen titleScreen = new ComponentScreen();
        
        ImageLabel label = new ImageLabel(null, null, "NetMonks GPS");
        label.setLeftEdge(Component.SCREEN_HCENTER, -label.getPreferredWidth()/2);
        label.setTopEdge(Component.SCREEN_TOP, (titleScreen.getHeight() - label.getPreferredHeight())/2);
        titleScreen.add(label);
        
        Font smallBold = Font.getFont(Font.FACE_PROPORTIONAL,Font.STYLE_BOLD,Font.SIZE_SMALL);
        Font smallFont = Font.getFont(Font.FACE_PROPORTIONAL,Font.STYLE_PLAIN,Font.SIZE_SMALL);
        
        label = new ImageLabel(null, null, "NetMonks Consulting");
        label.setFont(smallBold);
        label.setLeftEdge(Component.SCREEN_HCENTER, -label.getPreferredWidth()/2);
        label.setTopEdge(Component.PREVIOUS_COMPONENT_BOTTOM, 6);
        titleScreen.add(label);
        
        label = new ImageLabel(null, null, "Copyright 2004");
        label.setFont(smallFont);
        label.setLeftEdge(Component.SCREEN_HCENTER, -label.getPreferredWidth()/2);
        label.setTopEdge(Component.PREVIOUS_COMPONENT_BOTTOM, 3);
        titleScreen.add(label);
        
/*        String[] tmptmp = { "Testing", "yellow", "FISH", "Slepp", "Line 5", "Line 6" , "Line 7", "Line 8", "Line 9", "Line 10", "Line 11", "Line 12", "Line 13", "Line 14", "Line 15", "Line 16" };
        titleScreen.add(new NDropDownList("Interval", tmptmp));*/
        
        display.setCurrent(titleScreen);
        
        /* Menu screen */
        menuScreen = new MenuScreen();
        Navigator.registerScreen("menu", menuScreen);
        
        /* Status screen */
        statusScreen = new StatusScreen();
        Navigator.registerScreen("status", statusScreen);
        
        /* Settings screen */
        settingsScreen = new SettingsScreen();
        Navigator.registerScreen("settings", settingsScreen);
        
        if(Debug.ENABLED)
            Debug.log(12, "GPSMIDlet.initDisplays() Exited");
    }    
}
