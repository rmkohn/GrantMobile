package com.example.grantmobile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public final class UriHelper {
	// this would be so much nicer with polymorphic methods
	
	public static final String serverAddress = "http://mid-state.net/MobileClass2/android";
	
	public static HttpURLConnection getConnection(String url) throws IOException {
		HttpURLConnection http = (HttpURLConnection)(new URL(url.toString()).openConnection());
		// it's more of a hint really
		http.setConnectTimeout(20000);
		http.setReadTimeout(20000);
		return http;
	}

	public abstract static class ReadFromUri<T> {
		protected abstract T readStream(InputStream is) throws IOException;
		public T read(Uri uri, ContentResolver resolver) throws IOException {
			Log.i("readStream", uri.toString());
			T ret;
			if (uri.getScheme().equals("http")) {
				HttpURLConnection http = getConnection(uri.toString());
				try {
					ret = readStream(http.getInputStream());
				} finally {
					http.disconnect();
				}
			} else {
				ret = readStream(resolver.openInputStream(uri));
			}
			return ret;
		}
	}
	public static class PostToUri<Read> {
		final ReadFromUri<Read> read;
//		final PostToUri<Post> post;
		public PostToUri(ReadFromUri<Read> read) {
			this.read = read;
		}
		public Read post(String uri, String... params) throws IOException {
			return post(uri, makePost(params));
		}
		public Read post(String uri, byte[] content) throws IOException {
			HttpURLConnection http = getConnection(uri.toString());
			Log.i("postToUri", new String(content));
			Read ret;
		    try {
    			http.setDoOutput(true);
//    			http.setDoInput(true);
    			http.setFixedLengthStreamingMode(content.length);
//    			http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//    			http.setRequestProperty("Content-Length", Integer.toString(content.length));
    			http.getOutputStream().write(content);
    			http.getOutputStream().flush();
//    			int resp = http.getResponseCode(); // should maybe block?
    			ret = read.readStream(http.getInputStream());
		    } finally {
		    	http.disconnect();
		    }
		    return ret;
		}
	}
	
	public static final ReadFromUri<String> readString = new ReadFromUri<String>() {
		public String readStream(InputStream is) throws IOException {
			InputStreamReader isr = new InputStreamReader(is);
			char[] buf = new char[1024];
			int count = 0;
			StringBuilder sb = new StringBuilder();
			try {
				while ((count = isr.read(buf)) != -1)
					sb.append(buf, 0, count);
				return sb.toString();
			} finally {
				isr.close();
			}
		}
	};
	
	public static final ReadFromUri<JSONObject> readJson = new ReadFromUri<JSONObject>() {
		public JSONObject readStream(InputStream is) throws IOException {
			String jsonString = UriHelper.readString.readStream(is);
			Log.i("readJson", jsonString);
			try {
				return new JSONObject(jsonString);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return new JSONObject();
		}
	};
	
	// encode an array of interleaved keys and values into url query format
	public static byte[] makePost(String... params) {
		Uri.Builder b = new Uri.Builder();
		for (int i = 0; i < params.length; i+=2)
			b.appendQueryParameter(params[i], params[i+1]);
		return b.build().getEncodedQuery().getBytes();
	}
    
	public static final UriHelper.PostToUri<JSONObject> postJson = new UriHelper.PostToUri<JSONObject>(UriHelper.readJson);
	public static final UriHelper.PostToUri<String> postString = new UriHelper.PostToUri<String>(UriHelper.readString);
	
	/**
	 * AsyncTask for performing HTTP GETs and POSTs and getting JSON objects back.<br/>
	 * @param <T> Type of returned value in "message" on success
	 * <code>onSuccess()</code> or <code>onFailure()</code> will be called in the display thread, based on the
	 * boolean value <code>success</code> in the returned object.<br/>
	 * <code>onError()</code> is called instead, if anything goes wrong.<br/>
	 * Their respective default behaviors are to do nothing, log the error to the
	 * console, and print a stack trace.
	 * <br/><br/>
	 * When <code>execute()</code> is called, the request will be started on a background thread, with pairs of parameters
	 * treaded as key-value pairs for a POST form submission.  With no parameters, the task will be executed as a GET.
	 */
	abstract static class JsonLoader<T> extends WebLoader<JSONObject> {
	    public JsonLoader(String uri) { super(uri, null, readJson, postJson); }
	    protected void onPostExecute(JSONObject result) {
	    	super.onPostExecute(result);
			try {
		    	if (result.getBoolean("success"))
		    		onSuccess((T)result.get("message"));
				else
					onFailure(result.getString("message"));
			} catch (JSONException e) {
				onError(e);
			} catch (IOException e) {
				onError(e);
			} catch (NullPointerException e) {
				onError(e);
			} catch (ClassCastException e) {
				onError(e);
			}
	    }
        protected void onSuccess(T result) throws JSONException, IOException { }
        protected void onFailure(String errorMessage) { Log.w("GrantMobile", errorMessage); }
        protected void onError(Exception e) { e.printStackTrace(); }
	}
    
	/**
	 * AsyncTask for performing HTTP GETs and POSTs
	 *
	 * @param <T> Type of returned value passed to onPostExecute
	 */
    abstract static class WebLoader<T> extends AsyncTask<String, Void, T> {
    	String uri;
    	ContentResolver cr;
//    	ProgressDialog pDialog;
    	
    	protected ReadFromUri<T> reader;
    	protected PostToUri<T> poster;
    	
    	/**
    	 * @param uri The URI to load from.
    	 * @param a An activity, used to provide a content resolver.  Can be null if the request uses HTTP.
    	 * @param reader A ReadFromUri able to read an InputStream and return a &ltT&gt.
    	 * @param poster A PostToUri able to read an InputStream and return a &ltT&gt.
    	 */
    	public WebLoader(String uri, Activity a, ReadFromUri<T> reader, PostToUri<T> poster) {
    		this.uri = uri;
			this.cr = a == null ? null : a.getContentResolver(); 
			this.reader = reader;
			this.poster = poster;
			
			// ensure we can have delicious cookies
			if (CookieHandler.getDefault() == null)
			{
				CookieManager cookieManager = new java.net.CookieManager();
				CookieHandler.setDefault(cookieManager);
			}
		
    	}
//    	protected String waitMessage() { return "Loading..."; }
		protected T doInBackground(String... params) {
			T result = null;
			try {
				if (params.length == 0) { // GET
					result = reader.read(Uri.parse(uri), cr);
				} else { // POST
					result = poster.post(uri, params);
				}
			} catch (IOException e) {
                e.printStackTrace();
			}
			return result;
		}
        @Override
        protected void onPostExecute(T result) {
            super.onPostExecute(result);
            
//            pDialog.dismiss();
        }

/*        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(a);
            pDialog.setMessage(waitMessage());
            pDialog.setIndeterminate(false);
            pDialog.show();
        }*/
		
    }
	
}