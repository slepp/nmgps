/*
 * TcpHandler.java
 *
 * Created on February 20, 2004, 12:10 AM
 *
 * Copyright (c) 2004 Stephen Olesen <slepp@netmonks.ca>
 */

package ca.netmonks.j2me.net;

import ca.netmonks.iden.gps.Position;
import ca.netmonks.j2me.Debug;
import com.motorola.iden.position.AggregatePosition;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import java.util.Enumeration;
import java.util.Vector;

/**
 * An alternative to the UDP handler, this instead uses TCP sockets to send
 * the data, which offers a more robust method for determining if it was
 * actually sent.
 *
 * @author  Stephen Olesen <slepp@netmonks.ca>
 * @version $Revision: 1.10 $
 */
public final class TcpHandler
extends UpdateHandlerImpl {
    /** Current connection handle */
    protected SocketConnection connection = null;
    protected DataInputStream iStream = null;
    protected BufferedDataOutputStream oStream = null;
    
    public TcpHandler() {
        super();
        if(Debug.ENABLED)
            Debug.log(10, "TcpHandler Created ()");
    }
    
    public TcpHandler(String target) {
        super(target);
        if(Debug.ENABLED)
            Debug.log(10, "TcpHandler Created (" + target + ")");
    }
    
    public TcpHandler(String target, String username, String password) {
        super(target, username, password);
        if(Debug.ENABLED)
            Debug.log(10, "TcpHandler Created (" + target + ", " + username + ", " + password + ")");
    }
    
    public void close() throws IOException {
        if(Debug.ENABLED)
            Debug.log(12, "TcpHandler.close() Entered");
        
        if(connection != null) {
            if(Debug.ENABLED)
                Debug.log(12, "TcpHandler.close(): Closing connections");
            
            try {
                if(iStream != null)
                    iStream.close();
                
                if(oStream != null)
                    oStream.close();
                
                if(connection != null)
                    connection.close();
                
            } catch(IOException e) {
                if(Debug.ENABLED)
                    Debug.exception(e, "TcpHandler.close()");
                
                throw e;
            } finally {
                iStream = null;
                oStream = null;
                connection = null;
            }
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "TcpHandler.close() Exited");
    }
    
    public boolean isConnected() {
        if(Debug.ENABLED)
            Debug.log(12, "TcpHandler.isConnected() Entered");
        
        if(connection != null) {
            if(Debug.ENABLED)
                Debug.log(12, "TcpHandler.isConnected(): Connected, Exited");
            return true;
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "TcpHandler.isConnected(): Not Connected, Exited");
        return false;
    }
    
    public void open() throws IOException {
        if(Debug.ENABLED)
            Debug.log(12, "TcpHandler.open() Entered");
        
        if(target == null)
            setTarget("gps-update.netmonks.ca:8937");
        
        close();
        
        try {
            // Actually open the connection
            connection = (SocketConnection)Connector.open("socket://" + target);
            
            // Set some socket options
            connection.setSocketOption(SocketConnection.LINGER, 0); // Close immediately
            connection.setSocketOption(SocketConnection.KEEPALIVE, 0); // Keepalive off
            connection.setSocketOption(SocketConnection.DELAY, 1); // Nagle algo
            
            if(Debug.ENABLED)
                Debug.log(14, "TcpHandler.open(): Opened connection to " + target);
            
            iStream = connection.openDataInputStream();
            oStream = new BufferedDataOutputStream(connection.openOutputStream());
            
            if(Debug.ENABLED)
                Debug.log(15, "TcpHandler.open(): Opened Data streams");
            
        } catch(IOException e) {
            if(Debug.ENABLED)
                Debug.exception(e, "TcpHandler.open()");
            
            throw e;
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "TcpHandler.open() Exited");
    }
    
    public void sendUpdates(Vector updates)
    throws UpdateException {
        if(Debug.ENABLED)
            Debug.log(12, "TcpHandler.sendUpdates(Vector) Entered");
        
        if(updates.isEmpty()) {
            if(Debug.ENABLED)
                Debug.log(14, "TcpHandler.sendUpdates(Vector): Vector empty");
            
            return;
        }
        
        try {
            open();
            
            // Wait for a hello
            if(!("hi".equals(iStream.readUTF()))) {
                if(Debug.ENABLED)
                    Debug.log(8, "TcpHandler.sendUpdates(Vector): Didn't receive 'hi' from remote");
                
                close();
                throw new UpdateException("Bad Handshake");
            }
                        
            // Send our version and username/password
            oStream.writeUTF("V0");
            oStream.writeUTF(username);
            oStream.writeUTF(password);
            oStream.flush();
            
            if(Debug.ENABLED)
                Debug.log(14, "TcpHandler.sendUpdates(Vector): Sent Version and Username/Password");
            
            // If it's all good, then continue
            if(!("ok".equals(iStream.readUTF()))) {
                if(Debug.ENABLED)
                    Debug.log(8, "TcpHandler.sendUpdates(Vector): Didn't receive 'ok' from remote");
                
                close();
                throw new UpdateException("Bad Username/Pass");
            }
            
            if(Debug.ENABLED)
                Debug.log(14, "TcpHandler.sendUpdates(Vector): Sending " + updates.size() + " update(s) to the server");
            
            // Notify the other end of how many updates we're sending
            oStream.writeLong(updates.size());
            
            // Send all the updates in a big stream
            Enumeration e = updates.elements();
            while(e.hasMoreElements()) {
                Position pos = (Position)e.nextElement();
                AggregatePosition ap = pos.getAggregatePosition();
                
                oStream.writeInt(pos.getSequence());
                oStream.writeLong(ap.getTimeStamp());
                oStream.writeInt(ap.getLatitude());
                oStream.writeInt(ap.getLongitude());
                oStream.writeInt(ap.getLatLonAccuracy());
                oStream.writeInt(ap.getServingCellLatitude());
                oStream.writeInt(ap.getServingCellLongitude());
                oStream.writeInt(ap.getSpeed());
                oStream.writeInt(ap.getSpeedUncertainty());
                oStream.writeInt(ap.getTravelDirection());
                oStream.writeInt(ap.getAltitude());
                oStream.writeInt(ap.getAltitudeUncertainty());
                oStream.writeInt(ap.getNumberOfSatsUsed());
                oStream.writeBoolean(ap.getAssistanceUsed());
                
                if(Debug.ENABLED)
                    Debug.log(14, "TcpHandler.sendUpdates(Vector): Sent update to target");
            }
            
            // We're done with sending
            oStream.flush();
            oStream.close();
            oStream = null;
            
            if(Debug.ENABLED)
                Debug.log(14, "TcpHandler.sendUpdates(Vector): Sent updates to the server");
            
            // Wait for a response. If it doesn't match, we didn't send the updates
            if(iStream.readLong() != updates.size()) {
                if(Debug.ENABLED)
                    Debug.log(8, "TcpHandler.sendUpdates(Vector): Remote end didn't reply with the right number (" + updates.size() + ")");
                
                close();
                throw new UpdateException("Server Missed Reports");
            }
            
            close();
            
            if(Debug.ENABLED)
                Debug.log(12, "TcpHandler.sendUpdates(Vector) Exited");
            
        } catch(IOException ioe) {
            if(Debug.ENABLED)
                Debug.exception(ioe, "TcpHandler.sendUpdates()");
            
            try {
                close();
            } catch(IOException ignored) {
                if(Debug.ENABLED)
                    Debug.exception(ignored, "TcpHandler.sendUpdates()/close");
            }
            
            iStream = null;
            oStream = null;
            connection = null;
            throw new UpdateException(ioe.getMessage());
        }
    }
    
    public void sendHello() {
        if(Debug.ENABLED)
            Debug.log(12, "TcpHandler.sendHello() Entered");
        
        if(Debug.ENABLED)
            Debug.log(12, "TcpHandler.sendHello() Exited");
    }
    
    public void sendGoodbye() {
        if(Debug.ENABLED)
            Debug.log(12, "TcpHandler.sendGoodbye() Entered");
        
        if(Debug.ENABLED)
            Debug.log(12, "TcpHandler.sendGoodbye() Exited");
    }
    
    public void sendBeacon() {
        if(Debug.ENABLED)
            Debug.log(12, "TcpHandler.sendBeacon() Entered");
        
        if(Debug.ENABLED)
            Debug.log(12, "TcpHandler.sendBeacon() Exited");
    }
    
    public boolean sendMark(Position pos) {
        if(Debug.ENABLED)
            Debug.log(12, "TcpHandler.sendMark(Position) Entered");
        
        if(Debug.ENABLED)
            Debug.log(12, "TcpHandler.sendMark(Position) Exited");
        return false;
    }
}
