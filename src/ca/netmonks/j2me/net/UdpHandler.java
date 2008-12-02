/*
 * UdpHandler.java
 *
 * Created on January 18, 2004, 3:13 PM
 *
 * Copyright (c) 2004 Stephen Olesen <slepp@netmonks.ca>
 */

package ca.netmonks.j2me.net;

import ca.netmonks.iden.gps.Position;
import ca.netmonks.j2me.Debug;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.UDPDatagramConnection;

/**
 * Handle all the UDP network I/O for updates.
 *
 * @author  Stephen Olesen
 * @version $Revision: 1.6 $
 */
public final class UdpHandler
extends UpdateHandlerImpl {
    /** The UDP socket */
    private UDPDatagramConnection connection = null;
    
    /** The datagram we'll use persistently */
    private Datagram datagram = null;
    
    public UdpHandler() {
        super();
        if(Debug.ENABLED)
            Debug.log(10, "UdpHandler Created ()");
    }
    
    public UdpHandler(String target) {
        super(target);
        if(Debug.ENABLED)
            Debug.log(10, "UdpHandler Created (" + target + ")");
    }
    
    public UdpHandler(String target, String username, String password) {
        super(target, username, password);
        if(Debug.ENABLED)
            Debug.log(10, "UdpHandler Created (" + target + ", " + username + ", " + password + ")");
    }
    
    /** Send a prefixed datagram */
    private void sendPrefixed(String data)
    throws IOException {
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.sendPrefixed(\"" + data + "\") Entered");
        
        try {
            // Ensure it's open
            open();
            
            StringBuffer str = new StringBuffer();
            str.append("/V1/I");
            str.append(username);
            str.append(';');
            str.append(password);
            str.append(data);
            
            // Now set the datagram buffer
            datagram.setData(str.toString().getBytes(), 0, data.length());
            
            // And send it down the pipe
            connection.send(datagram);
        } catch(IOException e) {
            if(Debug.ENABLED)
                Debug.exception(e, "UdpHandler.sendPrefixed(String)/open,send");
            
            // Our UDP Connection has just been invalidated
            try {
                close();
            } catch(IOException ignored) {
                if(Debug.ENABLED)
                    Debug.exception(ignored, "UdpHandler.sendPrefixed(String)/close");
            }
            
            throw e;
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.sendPrefixed(\"" + data + "\") Exited");
    }
    
    /** Open the UDP connection to <code>udpTarget</code> */
    public void open()
    throws IOException {
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.open() Entered");
        
        // Close if it's open
        if(connection == null) {
            if(Debug.ENABLED)
                Debug.log(5, "UdpHandler.open(): Opening UDP connection to " + target);
            
            try { // This try is really just for debugging
                // Open the UDP connection to the udpTarget, write only mode
                connection = (UDPDatagramConnection)Connector.open("datagram://" + target, Connector.WRITE);
                datagram = connection.newDatagram(connection.getMaximumLength());
            } catch(IOException e) {
                if(Debug.ENABLED)
                    Debug.exception(e, "UdpHandler.open()");
                
                throw e;
            }
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.open() Exited");
    }
    
    /** Set the UDP target (datagram://host:port)
     * @param target The URL to the target UDP socket */
    public void setTarget(String target) {
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.setTarget(String) Entered");
        
        // If they are the same, we won't bother
        if(!this.target.equals(target)) {
            if(Debug.ENABLED)
                Debug.log(9, "Set UDP target to " + target);
            
            this.target = target;
            
            // If the connection is open, retarget it
            if(connection != null) {
                // We'll catch the IO Exception for now, and not handle it
                try {
                    this.close();
                    this.open();
                } catch(IOException e) {
                    if(Debug.ENABLED)
                        Debug.exception(e, "UdpHandler.setTarget(" + target + ")/close,open");
                }
            }
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.setTarget(String) Exited");
    }
    
    public void close() throws IOException {
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.close() Entered");
        
        if(connection != null) {
            try {
                connection.close();
            } catch(IOException e) {
                if(Debug.ENABLED)
                    Debug.exception(e, "UdpHandler.close()");
                
                throw e;
            }
        }
        connection = null;
        datagram = null;
        
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.close() Exited");
    }
    
    public boolean isConnected() {
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.isConnected() Entered");
        
        if(connection != null) {
            if(Debug.ENABLED)
                Debug.log(12, "UdpHandler.isConnected(): Connected, Exited");
            
            return true;
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.isConnected() Exited");
        return false;
    }
    
    public void sendBeacon() {
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.sendBeacon() Entered");
        
        try {
            sendPrefixed("/tALIVE");
        } catch(IOException e) {
            if(Debug.ENABLED)
                Debug.exception(e,"UdpHandler.sendBeacon()");
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.sendBeacon() Exited");
    }
    
    public void sendGoodbye() {
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.sendGoodbye() Entered");
        
        try {
            sendPrefixed("/tGOODBYE");
        } catch(IOException e) {
            if(Debug.ENABLED)
                Debug.exception(e,"UdpHandler.sendGoodbye()");
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.sendGoodbye() Exited");
    }
    
    public void sendHello() {
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.sendHello() Entered");
        
        try {
            sendPrefixed("/tHELLO");
        } catch(IOException e) {
            if(Debug.ENABLED)
                Debug.exception(e,"UdpHandler.sendHello()");
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.sendHello() Exited");
    }
    
    public boolean sendMark(Position pos) {
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.sendMark(Position) Entered");
        
        try {
            sendPrefixed("/tMARK" + pos.toString());
            
            if(Debug.ENABLED)
                Debug.log(12, "UdpHandler.sendMark(Position) Exited");
            
            return true;
        } catch(IOException e) {
            if(Debug.ENABLED)
                Debug.exception(e, "UdpHandler.sendMark(" + pos.toString() + ")");
            
            return false;
        }
    }
    
    public void sendUpdates(Vector updates)
    throws UpdateException {
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.sendUpdates(Vector(" + updates.size() + ")) Entered");
        
        Enumeration e = updates.elements();
        while(e.hasMoreElements()) {
            Position pos = (Position)e.nextElement();
            
            try {
                sendPrefixed(pos.toString());
                
                if(Debug.ENABLED)
                    Debug.log(14, "Sent an update to the server");
            } catch(IOException ioe) {
                if(Debug.ENABLED)
                    Debug.exception(ioe, "UdpHandler.sendUpdates()");
                
                throw new UpdateException(ioe.getMessage());
            }
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "UdpHandler.sendUpdates(Vector(" + updates.size() + ")) Exited");
    }
    
}
