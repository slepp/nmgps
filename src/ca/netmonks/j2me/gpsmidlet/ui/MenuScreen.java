/*
 * MenuScreen.java
 *
 * Created on January 26, 2004, 7:23 PM
 *
 * Copyright (c) 2004 Stephen Olesen <slepp@netmonks.ca>
 */

package ca.netmonks.j2me.gpsmidlet.ui;

import ca.netmonks.j2me.Debug;
import com.nextel.ui.*;
import javax.microedition.lcdui.Graphics;
import ca.netmonks.j2me.gpsmidlet.GPSMIDlet;

/**
 * The main menu for the application.
 *
 * This is a button based interface which forwards the user to the specific
 * named screens via the Navigator.
 *
 * @author  Stephen Olesen <slepp@netmonks.ca>
 * @version $Revision: 1.7 $
 */
public final class MenuScreen
extends OCompositeScreen {
    /** The status screen button */
    private OPushButton statusButton;
    
    /** The settings screen button */
    private OPushButton settingsButton;
    
    /** The exit button */
    private OPushButton exitButton;
    
    /** The over-the-air update button */
    private OPushButton otaButton;
    
    /** Creates a new instance of MenuScreen */
    public MenuScreen() {
        // Create the screen with this title
        super("NetMonks GPS", OUILook.PLAIN_LARGE, 1);
        
        if(Debug.ENABLED)
            Debug.log(10, "MenuScreen Created");
        
        // Initialize all the displays
        init();
    }
    
    /** Initialize the screen */
    private void init() {
        if(Debug.ENABLED)
            Debug.log(12, "MenuScreen.init() Entered");
        
        // The current running status screen
        statusButton = new OPushButton("Status", OUILook.PLAIN_MEDIUM, getWidth() - 20, 0, "STATUS");
        statusButton.setAction(new OCommandAction() {
            public void performAction() {
                Navigator.goForward( "status" );
            }
        });
        
        // Modify and view the settings
        settingsButton = new OPushButton("Settings", OUILook.PLAIN_MEDIUM, getWidth() - 20, 0, "SETTINGS");
        settingsButton.setAction(new OCommandAction() {
            public void performAction() {
                Navigator.goForward( "settings" );
            }
        });
        
        // Over the air update button. TODO: Make this work on i730..?
        /*otaButton = new OPushButton("OTA Update", OUILook.PLAIN_MEDIUM, getWidth() - 20, 0, "UPDATE");
        otaButton.setAction(new OCommandAction() {
            public void performAction() {
                try {
                    OHandset.getMIDlet().platformRequest("http://www.netmonks.ca:80/antenna/index.wml");
                } catch(Throwable t) {
                    displayEx(t, "platformRequest Failed");
                }
            }
        });*/
        
        // Exit the application button
        exitButton = new OPushButton("Exit", OUILook.PLAIN_MEDIUM, getWidth() - 20, 0, "EXIT");
        exitButton.setAction(new OCommandAction() {
            public void performAction() {
                ((GPSMIDlet)OHandset.getMIDlet()).destroyApp(false);
                OHandset.getMIDlet().notifyDestroyed();
            }
        });
        
        // Add everything
        int y = 0;
        add(statusButton, 0, y++, Graphics.HCENTER);
        add(settingsButton, 0, y++, Graphics.HCENTER);
        /*add(otaButton, 0, y++, Graphics.HCENTER);*/
        add(exitButton, 0, y++, Graphics.HCENTER);
        
        if(Debug.ENABLED)
            Debug.log(12, "MenuScreen.init() Exited");
    }
}
