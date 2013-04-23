package com.example.grantmobile;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.example.grantmobile.GrantService.GrantData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
	
	Map<GrantService.GrantData, Map<String, double[]>> cache;
    
	// TODO: so thread-unsafe it's not even funny
    public DBAdapter () { }
    
    private Map<String, double[]> getCacheEntry(GrantData data) {
    	if (cache == null)
    		cache = new HashMap<GrantData, Map<String, double[]>>();
    	Map<String, double[]> entry = cache.get(data);
    	if (entry == null) {
    		entry = new HashMap<String, double[]>();
    		cache.put(data, entry);
    	}
    	return entry;
    }
    
    public boolean saveEntry(GrantData data, String grant, double[] time) {
    	Map<String, double[]> entry = getCacheEntry(data);
    	entry.put(grant, time);
    	return true;
    }
    
//    private int getRowId(GrantData data, String grant) {
//    	Cursor c = getTimeCursor(data, new String[] { "_id" }, new String[] { grant });
//    	if (c != null && c.moveToFirst()) {
//    		return c.getInt(0);
//    	}
//    	return -1;
//    }
    
    public Map<String, double[]> getTimes(GrantData data, String[] grants) {
    	Map<String, double[]> entry = getCacheEntry(data);
    	Map<String, double[]> ret = new HashMap<String, double[]>(entry);
    	ret.keySet().retainAll(Arrays.asList(grants));
    	
    	return ret;
	}
    
    
    public int deleteEntries(GrantData data, String[] grants) {
    	Map<String, double[]> entry = getCacheEntry(data);
    	int count = 0;
    	for (String grant: grants) {
    		if (entry.remove(grant) != null) count++;
    	}
    	return count;
    }
    
    public static String getArrayQueryString(String[] array) {
    	return mkString(array, "\", \"", "(\"", "\")");
    }

    public static String mkString(Object[] array, String sep, String start, String end) {
    	return mkString(Arrays.asList(array), sep, start, end);
    }

    public static String mkString(Collection<?> array, String sep, String start, String end) {
    	if (array.isEmpty())
    		return start + end;
    	StringBuilder b = new StringBuilder(start);
    	Iterator<?> iter = array.iterator();
    	while (true) {
    		b.append(iter.next());
    		if (!iter.hasNext())
    			break;
    		b.append(sep);
    	}
    	b.append(end);
    	return b.toString();
    }
    
}
