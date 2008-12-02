/*
 * Position.java
 *
 * Created on January 15, 2004, 5:47 AM
 *
 * Copyright (c) 2004 Stephen Olesen <slepp@netmonks.ca>
 */

package ca.netmonks.iden.gps;

import com.motorola.iden.position.AggregatePosition;

/**
 * This class provides location information.
 *
 * @author  Stephen Olesen <slepp@netmonks.ca>
 * @version $Revision: 1.8 $
 */
public final class Position {
    // A simple 'unavailable' constant from Position2D
    //public static final int UNAVAILABLE = com.motorola.iden.position.Position2D.UNAVAILABLE;

    /** A sequence number to track uniqueness */
    private int sequenceNumber = 0;
    
    /* The AggregatePosition */
    private AggregatePosition ap = null;
    
    // If the location information is valid
    private boolean valid = false;
    
    // Create a new instance of Position
    public Position() {
    }
    
    /** Create a new instance of Position with a specific AggregatePosition */
    public Position(AggregatePosition ap) {
        this.ap = ap;
    }
    
    /** Create a new instance of Position, specifying if the AggregatePosition
     * is valid or not. */
    public Position(AggregatePosition ap, boolean valid) {
        this.ap = ap;
        this.valid = valid;
    }
    
    public void setAggregatePosition(AggregatePosition ap) {
        this.ap = ap;
    }
    
    public AggregatePosition getAggregatePosition() {
        return this.ap;
    }
    
    public void setSequence(int sequence) {
        sequenceNumber = sequence;
    }
    
    public int getSequence() {
        return sequenceNumber;
    }
    
    public boolean isValid() {
        return this.valid;
    }
    
    public void setValid(boolean v) {
        this.valid = v;
    }
    
    /** Convert the Position into a packet string for
     * transmission to the server */
    public String toString() {
        StringBuffer str = new StringBuffer(128);
        
        // Sequence number
        str.append("/N");
        str.append(sequenceNumber);
        
        // Timestamp
        str.append("/T");
        str.append(ap.getTimeStamp());
        
        // Position
        str.append("/P");
        str.append(ap.getLatitude());
        str.append(';');
        str.append(ap.getLongitude());
        str.append(';');
        str.append(ap.getLatLonAccuracy());
        
        // Altitude
        if(ap.hasAltitude()) {
            str.append("/A");
            str.append(ap.getAltitude());
            if(ap.hasAltitudeUncertainty()) {
                str.append(';');
                str.append(ap.getAltitudeUncertainty());
            }
        }
        
        // Speeed
        // If we have speed, we also have a heading
        if(ap.hasSpeed()) {
            str.append("/S");
            str.append(ap.getSpeed());
            if(ap.hasSpeedUncertainty()) { // Check if we have this
                str.append(';');
                str.append(ap.getSpeedUncertainty());
            }
            // Heading
            str.append("/H");
            str.append(ap.getTravelDirection());
        }
        
        if(ap.hasServingCellLatLon()) {
            // Cell serving
            str.append("/C");
            str.append(ap.getServingCellLatitude());
            str.append(';');
            str.append(ap.getServingCellLongitude());
        }
        
        // Other
        str.append("/O");
        str.append(ap.getNumberOfSatsUsed());
        str.append(';');
        str.append((ap.getAssistanceUsed() ? "t" : "f"));
        
        return str.toString();
    }
}
