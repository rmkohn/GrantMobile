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
	// table requestTime
	// _id year month userid grant
	// table grantTime
	// _id id json
	// table grants
	// _id grantid grantname etc
	
    static final String KEY_ROWID           = "_id";
    static final String TAG                 = "DBAdapter";
    static final String DATABASE_NAME       = "grantDB";
    static final String TABLE_SAVEDREQUESTS = "savedGrantRequests";
    static final int    DATABASE_VERSION    = 1;
    
    static final String savedGrantsCreate = "create table savedGrantRequests (_id integer primary key autoincrement," 
                                         + " year integer not null, month integer not null, userid integer not null, grant text not null,"
                                         + " time blob not null)";
    
    final Context context;
    
    DatabaseHelper DBHelper;
    SQLiteDatabase db;
    
    public DBAdapter(Context ctx) {
        context = ctx;
        DBHelper = new DatabaseHelper(ctx);
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context ctx) {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        }
        
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(savedGrantsCreate);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, String.format("upgrading db version from %d to %d, say goodbye to your data", oldVersion, newVersion));
            db.execSQL("DROP TABLE IF EXISTS contacts");
            onCreate(db);
        }
    }
    
    public DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        DBHelper.close();
    }
    
    public boolean saveEntry(GrantData data, String grant, double[] time) {
    	boolean success;
    	ContentValues cv = getContentValues(data, grant);
    	addTime(cv, time);
    	int oldrow = getRowId(data, grant);
    	if (oldrow == -1) {
    		long newrow = db.insert(TABLE_SAVEDREQUESTS, null, cv);
    		success = newrow != -1;
    	} else {
    		int updatedRows = db.update(TABLE_SAVEDREQUESTS, cv, "_id = " + oldrow, null);
    		success = updatedRows == 1;
    	}
    	return success;
    }
    
    private void addTime(ContentValues cv, double[] time) {
    	ByteBuffer buf = ByteBuffer.allocate(time.length*8);
    	for (double t: time) {
	    	buf.putDouble(t);
    	}
    	cv.put("time", buf.array());
    }
    
    private double[] retrieveTime(byte[] time) {
    	DoubleBuffer buf = ByteBuffer.wrap(time).asDoubleBuffer();
    	double[] ret = new double[buf.capacity()];
    	buf.get(ret);
    	return ret;
    }
    
    private ContentValues getContentValues(GrantData data, String grant) {
    	ContentValues cv = new ContentValues();
    	cv.put("year", data.year);
    	cv.put("month", data.month);
    	cv.put("userid", data.employeeid);
    	cv.put("grant", grant);
    	return cv;
    }
    
    private int getRowId(GrantData data, String grant) {
    	Cursor c = getTimeCursor(data, new String[] { "_id" }, new String[] { grant });
    	if (c != null && c.moveToFirst()) {
    		return c.getInt(0);
    	}
    	return -1;
    }
    
    private Cursor getTimeCursor(GrantData data, String[] columns, String[] grants) {
    	String query = getQueryString(data, grants);
    	Log.i("query", query);
    	Cursor c = db.query(TABLE_SAVEDREQUESTS, columns, query, null, null, null, null);
    	return c;
    }
    
    public Map<String, double[]> getTimes(GrantData data, String[] grants) {
    	Cursor c = getTimeCursor(data, new String[] { "grant", "time"}, grants);
    	Map<String, double[]> ret = new HashMap<String, double[]>();
    	c.moveToFirst();
    	while (!c.isAfterLast()) {
    		ret.put(c.getString(0), retrieveTime(c.getBlob(1)));
    		c.moveToNext();
    	}
    	return ret;
    }
    
    private String getQueryString(GrantData data, String[] grants) {
    	return String.format("year = %d and month = %d and userid = %d and grant in "
    			+ getArrayQueryString(grants), data.year, data.month, data.employeeid);
    }
    
    public int deleteEntries(GrantData data, String[] grants) {
    	return db.delete(TABLE_SAVEDREQUESTS, getQueryString(data, grants), null);
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
