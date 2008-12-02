/*
 * UpdateHandler.java
 *
 * Created on February 21, 2004, 3:42 PM
 *
 * Copyright (c) 2004 Stephen Olesen <slepp@netmonks.ca>
 */

package ca.netmonks.j2me.net;

import ca.netmonks.iden.gps.Position;
import java.io.IOException;
import java.util.Vector;

/**
 * Provides a generic interface to updating things over the Internet to the
 * server, to allow for simple usage of either UDP or TCP.
 *
 * @author  Stephen Olesen <slepp@netmonks.ca>
 * @version $Revision: 1.5 $
 */
public interface UpdateHandler {
    /** Open a connection to the current target */
    public void open() throws IOException;
    /** Open a connection to a new target */
    public void open(String target) throws IOException;
    
    /** Close the current connection */
    public void close() throws IOException;
    
    /** Set username */
    public void setUsername(String username);
    
    /** Set password */
    public void setPassword(String password);

    /** Send a hello to the server */
    public void sendHello();
    
    /** Send a beacon */
    public void sendBeacon();
    
    /** Send a goodbye to the server */
    public void sendGoodbye();
    
    /** Send a mark of a position */
    public boolean sendMark(Position pos);
    
    /** Send a single update */
    public void sendUpdate(Position updatePos) throws UpdateException;
    
    /** Send a vector of updates to the server */
    public void sendUpdates(Vector updates) throws UpdateException;
    
    /** Set the connection target */
    public void setTarget(String target);
    /** Get the connection target */
    public String getTarget();
    
    /** Determine if we're connected */
    public boolean isConnected();
}
