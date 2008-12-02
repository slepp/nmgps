/*
 * GPS.java
 *
 * Created on January 15, 2004, 5:36 AM
 *
 * Copyright (c) 2004 Stephen Olesen <slepp@netmonks.ca>
 */

package ca.netmonks.iden.gps;

import com.motorola.iden.position.AggregatePosition;
import com.motorola.iden.position.PositionConnection;
import com.motorola.iden.position.PositionDevice;
import java.io.IOException;
import javax.microedition.io.Connector;
import ca.netmonks.j2me.Debug;

/**
 * The primary GPS interface to the actual GPS device.
 *
 * @author  Stephen Olesen <slepp@netmonks.ca>
 * @version $Revision: 1.7 $
 */
public final class GPS {
    /** A shared instance of the GPS */
    private static GPS instance = null;
    
    /** The connection to the GPS */
    private PositionConnection connection = null;
    
    /** The current position */
    private Position position = null;
    
    /** The last status */
    private int lastResponse = 0;
    
    /** The sequence number of this request */
    private int sequenceNumber = 0;
    
    /** Create a new instance of the GPS interface. */
    public GPS() {
        instance = this;
        
        try {
            connect();
        } catch(IOException e) {
            connection = null;
        }
    }
    
    /** Get an existing instance of the GPS interface */
    public synchronized static GPS getInstance() {
        if(instance == null)
            instance = new GPS();
        return instance;
    }
    
    /** Try to connect to the GPS chip */
    public void connect(String name)
    throws IOException {
        connection = (PositionConnection)Connector.open(name == null ? "mposition:delay=low" : name);
    }
    
    /** Connect with the default connection type */
    public void connect()
    throws IOException {
        connect(null);
    }
    
    /** Get a default update quickly */
    public boolean getUpdate() {
        return getUpdate("delay=low");
    }
    
    /** Return true on good update, false on failure */
    public boolean getUpdate(String method) {
        // Do the actual update request
        AggregatePosition oap = connection.getPosition(method);
        
        // Get the status of the update
        lastResponse = connection.getStatus();
        
        // If we have a connection, and the status is OK, then we're good to go
        if(oap != null &&
           lastResponse == PositionConnection.POSITION_RESPONSE_OK) {
            // Good, create a new position
            position = new Position(oap, true);
            position.setSequence(sequenceNumber++);
            
            // Only notify on a successful update
            synchronized(this) {
                notifyAll();
            }
            
            if(Debug.ENABLED)
                Debug.log(15, "GPS.getUpdate(String): Notified all threads of new update");
            
            // The status is good
            return true;
        }
        
        // We only get here if it's all bad
        return false;
    }
    
    /** Get the last response of the GPS */
    public final int getStatus() {
        return lastResponse;
    }
    
    /** Get the last response's status string */
    public final String getStatusString() {
        return getStatusString(lastResponse);
    }
    
    /** Get the specified status string */
    public final static String getStatusString(int response) {
        String string;
        
        switch(response) {
            case PositionConnection.POSITION_RESPONSE_ERROR:
            case PositionDevice.FIX_NOT_ATTAINABLE:
            case PositionDevice.FIX_NOT_ATTAIN_ASSIST_DATA_UNAV:
                string = "No Signal";
                break;
                
            case PositionDevice.BATTERY_TOO_LOW:
                string = "Battery Too Low";
                break;
                
            case PositionDevice.ALMANAC_OUT_OF_DATE:
                string = "Almanac Out of Date";
                break;
                
            case PositionDevice.ACC_NOT_ATTAIN_ASSIST_DATA_UNAV:
            case PositionDevice.ACCURACY_NOT_ATTAINABLE:
                string = "Accuracy Not Attainable";
                break;
                
            case PositionDevice.GPS_CHIPSET_MALFUNCTION:
                string = "Chipset Malfunction";
                break;
                
            case PositionConnection.POSITION_NO_RESPONSE:
                string = "No Response";
                break;
                
            case PositionConnection.POSITION_RESPONSE_RESTRICTED:
                string = "Restricted";
                break;
                
            case PositionConnection.POSITION_RESPONSE_NO_ALMANAC_OVERRIDE:
                string = "Override Denied";
                break;
                
            default:
                string = "Unknown Response";
                break;
        } // end of switch
        
        return string;
    }
    
    /** Check if we're busy with a request */
    public final boolean busy() {
        return connection.requestPending();
    }
    
    /** Get an updated position by blocking until it returns */
    public final Position getPosition()
    throws InterruptedException {
        return getPosition(0);
    }
    
    /** Get an updated position by blocking until it returns, or a timeout */
    public final synchronized Position getPosition(long interval)
    throws InterruptedException {
        if(Debug.ENABLED)
            Debug.log(15, "GPS.getPosition(" + interval + "): Thread Waiting");
        wait(interval);
        if(Debug.ENABLED)
            Debug.log(15, "GPS.getPosition(" + interval + "): Thread Notified/Timed Out");
        return position;
    }
    
    /** Get the last known position */
    public final Position getLastPosition() {
        return position;
    }
    
    /** Attempt to disconnect the GPS */
    public final void disconnect()
    throws IOException {
        if(connection != null)
            connection.close();
    }
}
