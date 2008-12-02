/*
 * UpdateException.java
 *
 * Created on February 24, 2004, 1:13 AM
 *
 * Copyright (c) 2004 Stephen Olesen <slepp@netmonks.ca>
 */

package ca.netmonks.j2me.net;

/**
 * This exception is thrown on a failure to update the server with reports.
 *
 * @author  Stephen Olesen
 * @version $Revision: 1.1 $
 */
public final class UpdateException extends java.lang.Exception {
   
    /**
     * Creates a new instance of <code>UpdateException</code> without detail message.
     */
    public UpdateException() {
    }
    
    /**
     * Constructs an instance of <code>UpdateException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public UpdateException(String msg) {
        super(msg);
    }
    
}
