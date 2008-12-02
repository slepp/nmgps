/*
 * UpdateHandlerImpl.java
 *
 * Created on February 21, 2004, 4:54 PM
 *
 * Copyright (c) 2004 Stephen Olesen <slepp@netmonks.ca>
 */

package ca.netmonks.j2me.net;

import ca.netmonks.iden.gps.Position;
import ca.netmonks.j2me.Debug;
import java.io.IOException;
import java.util.Vector;

/**
 * This is a very basic implementation of the core functionality of an update
 * handler.
 *
 * @author  Stephen Olesen
 * @version $Revision: 1.5 $
 */
public abstract class UpdateHandlerImpl
implements UpdateHandler {
    protected String target = "gps-update.netmonks.ca:8937";
    
    protected String username = "anonymous";
    protected String password = "anonymous";
    
    /** Creates a new instance of UpdateHandlerImpl */
    public UpdateHandlerImpl() {
    }
    
    public UpdateHandlerImpl(String target) {
        setTarget(target);
    }
    
    public UpdateHandlerImpl(String target, String username, String password) {
        setTarget(target);
        setUsername(username);
        setPassword(password);
    }
    
    /** Get the current target */
    public String getTarget() {
        if(Debug.ENABLED)
            Debug.log(14, "UpdateHandlerImpl.getTarget() Called");
        
        return this.target;
    }
    
    /** Open a connection with a specific target */
    public void open(String target) throws IOException {
        if(Debug.ENABLED)
            Debug.log(12, "UpdateHandlerImpl.open(String) Entered");
        
        this.target = target;
        open();
        
        if(Debug.ENABLED)
            Debug.log(12, "UpdateHandlerImpl.open(String) Exited");
    }
    
    /** Set the password */
    public void setPassword(String password) {
        if(Debug.ENABLED)
            Debug.log(14, "UpdateHandlerImpl.setPassword() Called");
        
        this.password = password;
    }
    
    /** Set the username */
    public void setUsername(String username) {
        if(Debug.ENABLED)
            Debug.log(14, "UpdateHandlerImpl.setUsername() Called");
        
        this.username = username;
    }
    
    /** Set the target */
    public void setTarget(String target) {
        if(Debug.ENABLED)
            Debug.log(14, "UpdateHandlerImpl.setTarget() Called");
        
        this.target = target;
    }
    
    /** Send a single update, by faking it as a batch */
    public void sendUpdate(Position pos) throws UpdateException {
        if(Debug.ENABLED)
            Debug.log(12, "UpdateHandlerImpl.sendUpdate(Position) Entered");
        
        if(!pos.isValid()) {
            if(Debug.ENABLED)
                Debug.log(12, "UpdateHandlerImpl.sendUpdate(Position): Invalid, exited");
            return;
        }
        
        Vector tmp = new Vector(1);
        tmp.addElement(pos);
        sendUpdates(tmp);
        
        if(Debug.ENABLED)
            Debug.log(12, "UpdateHandlerImpl.sendUpdate(Position) Exited");
    }
    
}
