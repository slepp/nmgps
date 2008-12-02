/*
 * SettingsScreen.java
 *
 * Created on January 26, 2004, 7:23 PM
 *
 * Copyright (c) 2004 Stephen Olesen <slepp@netmonks.ca>
 */

package ca.netmonks.j2me.gpsmidlet.ui;

import ca.netmonks.j2me.Debug;
import com.nextel.exception.InvalidData;
import com.nextel.ui.*;
import javax.microedition.lcdui.Graphics;
import ca.netmonks.j2me.gpsmidlet.GPSMIDlet;
import ca.netmonks.j2me.gpsmidlet.Settings;

/**
 * The settings screen for the GPS application.
 *
 * This provides for setting the various values needed for smooth operation
 * of the program.
 *
 * @author  Stephen Olesen <slepp@netmonks.ca>
 * @version $Revision: 1.8 $
 */
public final class SettingsScreen
extends OCompositeScreen {
    /** Go back, don't save changes */
    private OSoftKey cancelSoftKey;
    
    /** Save settings and go  back */
    private OSoftKey saveSoftKey;
    
    /** The username field */
    private OTextField username;
    
    /** The password field */
    private OTextField password;
    
    /** The method field */
    private ODropDownList method;
    
    /** The method selection names */
    private static final String[] methodNames = { "TCP", "UDP" };
    
    /** The method actual values */
    private static final String[] methods = { "tcp", "udp" };
    
    /** The interval selection names */
    private static final String[] intervalNames = { "15 seconds", "30 seconds",
    "45 seconds", "1 minute", "1.5 minutes", "2 minutes", "2.5 minutes", "3 minutes",
    "4 minutes", "5 minutes", "7.5 minutes", "10 minutes", "15 minutes", "30 minutes",
    "1 hour" };
    
    /** The intervals themselves in seconds */
    private static final int[] intervals = { 15, 30, 45, 60, 90, 120, 150, 180,
    240, 300, 450, 600, 900, 1800, 3600 };
    
    /** The dropdown selection of intervals */
    private ODropDownList interval;
    
    /** Creates a new instance of SettingsScreen */
    public SettingsScreen() {
        // Set our title and font styles
        super("NetMonks GPS\nSettings", OUILook.PLAIN_SMALL, 3);
        
        if(Debug.ENABLED)
            Debug.log(10, "SettingsScreen Created");
        
        // Initialize the actual display
        init();
    }
    
    /** Initialize the settings screen */
    private void init() {
        if(Debug.ENABLED)
            Debug.log(12, "SettingsScreen.init() Entered");
        
        // Left soft key, to cancel changes
        cancelSoftKey = new OSoftKey("CANCEL");
        cancelSoftKey.setAction(new OCommandAction() {
            public void performAction() {
                Navigator.goBack();
            }
        });
        addSoftKey(cancelSoftKey, Graphics.LEFT);
        
        // Right soft key, to save changes
        saveSoftKey = new OSoftKey("SAVE");
        saveSoftKey.setAction(new OCommandAction() {
            public void performAction() {
                // Set all the settings
                Settings.set(Settings.USER_ID, username.getText());
                Settings.set(Settings.USER_PASSWORD, password.getText());
                Settings.set(Settings.UPDATE_INTERVAL, String.valueOf(intervals[interval.getSelectedIndex()]));
                Settings.set(Settings.UPDATE_METHOD, methods[method.getSelectedIndex()]);
                
                // Reload them into the midlet
                ((GPSMIDlet)OHandset.getMIDlet()).loadSettings();
                
                // Go back to where we were
                Navigator.goBack();
            }
        });
        addSoftKey(saveSoftKey, Graphics.RIGHT);
        
        int y = 0; // Where to add the next item
        
        // The username field
        add(new OLabel("Name:",OUILook.BOLD_SMALL), 0, y, Graphics.RIGHT);
        username = new OTextField(13, OUILook.PLAIN_SMALL, OTextField.LOWERCASE);
        add(username, 1, y, Graphics.LEFT);
        
        // The password field
        ++y;
        add(new OLabel("Pass:",OUILook.BOLD_SMALL), 0, y, Graphics.RIGHT);
        password = new OTextField(13, OUILook.PLAIN_SMALL, OTextField.ANY);
        add(password, 1, y, Graphics.LEFT);
        
        // The update period field
        ++y;
        add(new OLabel("Period:", OUILook.BOLD_SMALL), 0, y, Graphics.RIGHT);
        interval = new ODropDownList(intervalNames, OUILook.PLAIN_SMALL);
        add(interval, 1, y, Graphics.LEFT);
        
        ++y;
        add(new OLabel("Method:", OUILook.BOLD_SMALL), 0, y, Graphics.RIGHT);
        method = new ODropDownList(methodNames, OUILook.PLAIN_SMALL);
        add(method, 1, y, Graphics.LEFT);
        
        if(Debug.ENABLED)
            Debug.log(12, "StatusScreen.init() Exited");
    }
    
    /** Called whenever we get shown. Load the current settings. */
    protected void showNotify() {
        if(Debug.ENABLED)
            Debug.log(12, "SettingsScreen.showNotify() Entered");
        
        try {
            // Get the username
            username.setText(Settings.get(Settings.USER_ID));
            // Get the password
            password.setText(Settings.get(Settings.USER_PASSWORD));
        } catch(InvalidData e) {
            if(Debug.ENABLED)
                Debug.exception(e, "SettingsScreen.showNotify()/username,password");
            
            displayEx(e, "Couldn't Load Username and Password");
        }
        
        // Set the interval selection
        int intervalCurrent = Settings.getInt(Settings.UPDATE_INTERVAL);
        for(int i = 0; i < intervals.length; i++) {
            if(intervals[i] == intervalCurrent) {
                interval.setSelectedIndex(i);
                break;
            }
        }
        
        for(int i = 0; i < methods.length; i++) {
            if(methods[i].equals(Settings.get(Settings.UPDATE_METHOD))) {
                method.setSelectedIndex(i);
                break;
            }
        }
        
        // Call the parent show notify, in case it's needed
        super.showNotify();
        
        if(Debug.ENABLED)
            Debug.log(12, "SettingsScreen.showNotify() Exited");
    }
}
