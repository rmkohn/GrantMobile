package com.example.grantmobile;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.example.grantmobile.GrantService.GrantData;

import android.util.Log;

public class DBAdapter {
	
	Map<GrantService.GrantData, Map<String, double[]>> cache;
    
	// TODO: so thread-unsafe it's not even funny
    public DBAdapter () {
    	Log.w("DBAdapter", "newly created");
		cache = new HashMap<GrantData, Map<String, double[]>>();
    }
    
    private Map<String, double[]> getCacheEntry(GrantData data) {
    	Map<String, double[]> entry = cache.get(data);
    	if (entry == null) {
    		Log.i("DBAdapter", "created new entry for " + data);
    		entry = new HashMap<String, double[]>();
    		cache.put(data, entry);
    	}
    	return entry;
    }
    
    public boolean saveEntry(GrantData data, String grant, double[] time) {
    	Log.i("DBAdapter", "saving " + grant);
    	Map<String, double[]> entry = getCacheEntry(data);
    	entry.put(grant, time);
    	return true;
    }
    
    public Map<String, double[]> getTimes(GrantData data, String[] grants) {
    	Map<String, double[]> entry = getCacheEntry(data);
    	Map<String, double[]> ret = new HashMap<String, double[]>(entry);
    	ret.keySet().retainAll(Arrays.asList(grants));
    	Log.i("DBAdapter", String.format("entry for %s contains %d granthours", data, entry.size()));
    	Log.i("DBAdapter", String.format("got %d of %d granthours",ret.size(), grants.length));
    	
    	return ret;
	}
    
    
    public int deleteEntries(GrantData data, String[] grants) {
    	Map<String, double[]> entry = getCacheEntry(data);
    	int count = 0;
    	for (String grant: grants) {
    		if (entry.remove(grant) != null) count++;
    	}
    	Log.i("DBAdapter", "deleted " + count + " entries");
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
