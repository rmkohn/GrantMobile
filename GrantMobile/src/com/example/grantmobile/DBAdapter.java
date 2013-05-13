package com.example.grantmobile;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.example.grantmobile.GrantService.GrantData;

import android.util.Log;

public class DBAdapter {
	
	Map<GrantService.GrantData, Map<String, Hours>> cache;
	
	public static class Hours {
		public static enum GrantStatus {
			New,
			pending,
			denied,
			approved,
			none;
			public int getDrawable() {
				switch(this) {
				case New: return R.drawable.image_new;
				case approved: return R.drawable.image_approved;
				case denied: return R.drawable.image_disapproved;
				case pending: return R.drawable.image_pending;
				default: return -1;
				}
			}
		}
		public GrantStatus status;
		public double[] hours;
		
		public Hours(GrantStatus status, double[] hours) {
			this.status = status;
			this.hours = hours;
		}
	}
    
	// TODO: so thread-unsafe it's not even funny
    public DBAdapter () {
    	Log.w("DBAdapter", "newly created");
		cache = new HashMap<GrantData, Map<String, Hours>>();
    }
    
    private Map<String, Hours> getCacheEntry(GrantData data) {
    	Map<String, Hours> entry = cache.get(data);
    	if (entry == null) {
    		Log.i("DBAdapter", "created new entry for " + data);
    		entry = new HashMap<String, Hours>();
    		cache.put(data, entry);
    	}
    	return entry;
    }
    
    // save new/updated grant hours into database
    public boolean saveEntry(GrantData data, String grant, Hours time) {
    	Log.i("DBAdapter", "saving " + grant);
    	Map<String, Hours> entry = getCacheEntry(data);
    	entry.put(grant, time);
    	return true;
    }
    
    public boolean saveEntry(GrantData data, String grant, double[] time) {
    	Log.i("DBAdapter", "saving " + grant);
    	Map<String, Hours> entry = getCacheEntry(data);
    	Hours oldHours = entry.get(grant);
    	if (oldHours == null) {
    		entry.put(grant, new Hours(Hours.GrantStatus.New, time));
    	} else {
    		oldHours.hours = time;
    	}
    	return true;
    }
    
    public boolean updateStatus(GrantData data, String grant, Hours.GrantStatus status) {
    	Map<String, Hours> entry = getCacheEntry(data);
    	Hours oldHours = entry.get(grant);
    	if (oldHours == null)
    		return false;
    	oldHours.status = status;
    	return true;
    }
    
    // get a map of grant ids to hours for the supplied year/month/employee and grants
    public Map<String, Hours> getTimes(GrantData data, String[] grants) {
    	Map<String, Hours> entry = getCacheEntry(data);
    	Map<String, Hours> ret = new HashMap<String, Hours>(entry);
    	ret.keySet().retainAll(Arrays.asList(grants));
    	Log.i("DBAdapter", String.format("entry for %s contains %d granthours", data, entry.size()));
    	Log.i("DBAdapter", String.format("got %d of %d granthours",ret.size(), grants.length));
    	
    	return ret;
	}
    
    // remove all entries matching the provided keys
    public int deleteEntries(GrantData data, String[] grants) {
    	Map<String, Hours> entry = getCacheEntry(data);
    	int count = 0;
    	for (String grant: grants) {
    		if (entry.remove(grant) != null) count++;
    	}
    	Log.i("DBAdapter", "deleted " + count + " entries");
    	return count;
    }
    
    // turn array into ("elem1","elem2",...)
    public static String getArrayQueryString(String[] array) {
    	return mkString(array, "\", \"", "(\"", "\")");
    }

    // turn arrays into strings
    public static String mkString(Object[] array, String sep, String start, String end) {
    	return mkString(Arrays.asList(array), sep, start, end);
    }

    // turn collections into strings
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
