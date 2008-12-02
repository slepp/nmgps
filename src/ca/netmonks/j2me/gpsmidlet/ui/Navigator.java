/*
 * Navigator.java
 *
 * Created on January 26, 2004, 7:26 PM
 *
 * Copyright (c) 2004 Stephen Olesen <slepp@netmonks.ca>
 */

package ca.netmonks.j2me.gpsmidlet.ui;

import ca.netmonks.j2me.Debug;
import com.nextel.ui.OHandset;
import java.util.Hashtable;
import java.util.Stack;
import javax.microedition.lcdui.Displayable;

/**
 * A static class to navigate between the Displayables.
 *
 * This operates on either a direct reference to a screen, or you can
 * register screens by name to simplify intercomponent navigation. This
 * allows for a Back button to be provided for the user.
 *
 * @author  Stephen Olesen <slepp@netmonks.ca>
 * @version $Revision: 1.5 $
 */
public final class Navigator {
    /** The history of the screens we've been to */
    private static Stack screens = new Stack();
    
    /** The registry of named screens */
    private static Hashtable registry = new Hashtable();
    
    /** Creates a new instance of Navigator */
    protected Navigator() {
    }
    
    /** Register a screen by name.
     * @param name The name of the screen
     * @param screen The Displayable object
     */
    public static final void registerScreen(String name, Displayable screen) {
        if(Debug.ENABLED)
            Debug.log(14, "Navigator.RegisterScreen(" + name + ") Called");
        registry.put(name, screen);
    }
    
    /** Go forward to a named screen.
     * @param next The name of the registered screen.
     */
    public static final void goForward(String next) {
        if(Debug.ENABLED)
            Debug.log(14, "Navigator.goForward(" + next + ") Called");
        
        if(registry.containsKey(next))
            goForward((Displayable)registry.get(next));
    }
    
    /** Go forward to a specific object screen.
     * @param next The Displayable object
     */
    public static final void goForward(Displayable next) {
        if(Debug.ENABLED)
            Debug.log(14, "Navigator.goForward(Displayable) Called");
        
        screens.push(next);
        OHandset.getDisplay().setCurrent(next);
    }
    
    /** Go back to the top level of the navigation. */
    public static final void goHome() {
        if(Debug.ENABLED)
            Debug.log(14, "Navigator.goHome() Called");
        
        Displayable home = null;
        while(!screens.empty()) {
            home = (Displayable) screens.pop();
        }
        OHandset.getDisplay().setCurrent(home);
        screens.push(home);
    }
    
    /** Go back one screen. */
    public static final void goBack() {
        if(Debug.ENABLED)
            Debug.log(14, "Navigator.goBack() Called");
        
        screens.pop();
        OHandset.getDisplay().setCurrent( (Displayable)screens.peek() );
    }
    
    /** Get the current screen we're on.
     * @return Displayable object we're looking at.
     */
    public static final Displayable getCurrent() {
        if(Debug.ENABLED)
            Debug.log(14, "Navigator.getCurrent() Called");
        
        if(!screens.empty())
            return (Displayable)screens.peek();
        return null;
    }
    
}
