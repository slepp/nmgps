/*
 * BufferedDataOutputStream.java
 *
 * Created on February 24, 2004, 8:06 PM
 */

package ca.netmonks.j2me.net;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author  slepp
 */
public class BufferedDataOutputStream
extends OutputStream
implements DataOutput {
    
    private ByteArrayOutputStream byteStream = null;
    private DataOutputStream dataStream = null;
    private OutputStream outputStream = null;
    
    private boolean autoflush = true;
    private int autoflushLimit = 2842; // This magic number is equal to 49 updates, and comes in under MSS*2
    
    /** Creates a new instance of BufferedDataOutputStream */
    public BufferedDataOutputStream(OutputStream oStream) {
        byteStream = new ByteArrayOutputStream(2842);
        dataStream = new DataOutputStream(byteStream);
        this.outputStream = oStream;
    }
    
    public void setAutoFlushLimit(int length) {
        autoflushLimit = length;
    }
    
    public void setAutoFlush(boolean param) {
        autoflush = param;
    }
    
    public void reset() {
        byteStream.reset();
    }
    
    public void flush() throws IOException {
        dataStream.flush();
        byte[] arr = byteStream.toByteArray();
        outputStream.write(arr, 0, arr.length);
        outputStream.flush();
        byteStream.reset();
    }
    
    public void conditionalFlush() throws IOException {
        if(autoflush && byteStream.size() >= autoflushLimit)
            flush();
    }
    
    public void close() throws IOException {
        flush();
        dataStream.close();
        byteStream.close();
        outputStream.close();
    }
    
    public void writeBoolean(boolean param) throws IOException {
        dataStream.writeBoolean(param);
        conditionalFlush();
    }
    
    public void writeByte(int param) throws IOException {
        dataStream.writeByte(param);
        conditionalFlush();
    }
    
    public void writeChar(int param) throws IOException {
        dataStream.writeChar(param);
        conditionalFlush();
    }
    
    public void writeChars(String str) throws IOException {
        dataStream.writeChars(str);
        conditionalFlush();
    }
    
    public void writeInt(int param) throws IOException {
        dataStream.writeInt(param);
        conditionalFlush();
    }
    
    public void writeLong(long param) throws IOException {
        dataStream.writeLong(param);
        conditionalFlush();
    }
    
    public void writeShort(int param) throws IOException {
        dataStream.writeShort(param);
        conditionalFlush();
    }
    
    public void writeUTF(String str) throws IOException {
        dataStream.writeUTF(str);
        conditionalFlush();
    }
    
    public void write(int param) throws IOException {
        dataStream.write(param);
        conditionalFlush();
    }
    
}
