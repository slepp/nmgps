/*
 * Settings.java
 *
 * Created on January 18, 2004, 4:33 PM
 *
 * Copyright (c) 2004 Stephen Olesen <slepp@netmonks.ca>
 */

package ca.netmonks.j2me.gpsmidlet;

import ca.netmonks.j2me.Debug;
import javax.microedition.rms.RecordStore;
import java.util.Enumeration;
import java.util.Hashtable;
import com.mot.iden.util.Base64;
import com.mot.iden.util.StringTokenizer;

/**
 * Static access to the RMS settings store.
 *
 * @author  Stephen Olesen <slepp@netmonks.ca>
 * @version $Revision: 1.8 $
 */
public final class Settings {
    private static final boolean DEBUG = true;
    
    private static Hashtable s = new Hashtable(6);
    private static RecordStore rs = null;
    
    public static final String USER_ID = "userId";
    public static final String USER_PASSWORD = "userPassword";
    public static final String UPDATE_INTERVAL = "updateInterval";
    public static final String UPDATE_TARGET = "updateTarget";
    public static final String UPDATE_METHOD = "updateMethod";
    
    private static final int SETTINGS_RECORD = 1;
    
    public static final String get(String key) {
        if(Debug.ENABLED)
            Debug.log(12, "Settings.get(String = \"" + key + "\") Entered");
        
        if(rs == null)
            open();
        
        if(Debug.ENABLED)
            Debug.log(12, "Settings.get(String = \"" + key + "\") Exited");
        
        return (String)s.get(key);
    }
    
    public static final String get(String key, String def) {
        if(Debug.ENABLED)
            Debug.log(12, "Settings.get(String = \"" + key + "\", String = \"" + def + "\") Entered");
        
        String res = get(key);
        if(res == null) {
            set(key, def);
            res = def;
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "Settings.get(String = \"" + key + "\", String = \"" + def + "\") Exited");
        
        return res;
    }
    
    public static final int getInt(String key) {
        if(Debug.ENABLED)
            Debug.log(14, "Settings.getInt(String) Called");
        
        return getInt(key, 0);
    }
    
    public static final int getInt(String key, int def) {
        if(Debug.ENABLED)
            Debug.log(12, "Settings.getInt(String = \"" + key + "\", int = " + def + ") Entered");
        
        int res = def;
        try {
            res = Integer.parseInt(get(key, String.valueOf(def)));
        } catch(Exception e) {
            if(Debug.ENABLED)
                Debug.exception(e, "Settings.getInt(String,int)");
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "Settings.getInt(String = \"" + key + "\", int = " + def + ") Exietd");
        
        return res;
    }
    
    public static final void set(String key, String str) {
        if(Debug.ENABLED)
            Debug.log(12, "Settings.set(String, String) Entered");
        
        if(rs == null)
            open();
        
        s.put(key, str);
        
        save();
        
        if(Debug.ENABLED)
            Debug.log(12, "Settings.set(String, String) Exited");
    }
    
    public static final boolean areValid() {
        if(Debug.ENABLED)
            Debug.log(12, "Settings.areValid() Entered");
        
        if(rs == null)
            open();
        
        if("true".equals((String)s.get("validity"))) {
            if(Debug.ENABLED)
                Debug.log(12, "Settings.areValid(): Are valid, Exited");
            return true;
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "Settings.areValid(): Not valid, Exited");
        
        return false;
    }
    
    public static final void open() {
        if(Debug.ENABLED)
            Debug.log(12, "Settings.open() Entered");
        
        try {
            if(s == null)
                s = new Hashtable(5);
            rs = RecordStore.openRecordStore("nmGPSsettings", true);
            String str = new String(rs.getRecord(SETTINGS_RECORD));
            StringTokenizer st = new StringTokenizer(str);
            
            while(st.hasMoreElements()) {
                String key = new String(st.nextToken().getBytes());
                String value = new String(Base64.decode(st.nextToken().getBytes()));
                s.put(key, value);
            }
        } catch(Exception e) {
            if(Debug.ENABLED)
                Debug.exception(e, "Settings.open()");
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "Settings.open() Exited");
    }
    
    public static final void save() {
        if(Debug.ENABLED)
            Debug.log(12, "Settings.save() Entered");
        
        StringBuffer str = new StringBuffer();
        s.put("validity", "true");
        Enumeration e = s.keys();
        while(e.hasMoreElements()) {
            String key = (String)e.nextElement();
            String value = (String)s.get(key);
            
            str.append(key);
            str.append(' ');
            str.append(new String(Base64.encode(value.getBytes())));
            if(e.hasMoreElements())
                str.append(' ');
        }
        
        try {
            if(rs.getNextRecordID() == SETTINGS_RECORD) {
                rs.addRecord(str.toString().getBytes(), 0, str.length());
            } else {
                rs.setRecord(SETTINGS_RECORD, str.toString().getBytes(), 0, str.length());
            }
        } catch(Exception ex) {
            if(Debug.ENABLED)
                Debug.exception(ex, "Settings.save()");
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "Settings.save() Exited");
    }
    
    public static final void close() {
        if(Debug.ENABLED)
            Debug.log(12, "Settings.close() Entered");
        
        try {
            rs.closeRecordStore();
        } catch(Exception e) {
            if(Debug.ENABLED)
                Debug.exception(e, "Settings.close()");
        }
        
        if(Debug.ENABLED)
            Debug.log(12, "Settings.close() Exited");
    }
}
