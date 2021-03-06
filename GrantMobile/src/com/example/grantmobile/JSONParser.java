package com.example.grantmobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

public class JSONParser {


	// constructor
	public JSONParser() {

	}
	static CookieStore cookies;
	
	// function get json from url
	// by making HTTP POST or GET mehtod
	public static JSONObject makeHttpRequest(String url, String method,
			List<NameValuePair> params) {
		InputStream is = null;
		JSONObject jObj = null;
		String json = "";
		
		if (cookies == null)
			cookies = new BasicCookieStore();

		// Making HTTP request
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			httpClient.setCookieStore(cookies);
			final HttpParams connectionParams = httpClient.getParams();

			HttpConnectionParams.setConnectionTimeout(connectionParams, 5000); // wait for connection
			HttpConnectionParams.setSoTimeout        (connectionParams, 5000); // wait for response
			
			// check for request method
			if(method == "POST"){
				// request method is POST
				// defaultHttpClient
				HttpPost httpPost = new HttpPost(url);
				httpPost.setEntity(new UrlEncodedFormEntity(params));

				HttpResponse httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntity = httpResponse.getEntity();
				is = httpEntity.getContent();
				
			}else if(method == "GET"){
				// request method is GET
				String paramString = URLEncodedUtils.format(params, "utf-8");
				url += "?" + paramString;
				HttpGet httpGet = new HttpGet(url);

				HttpResponse httpResponse = httpClient.execute(httpGet);
				HttpEntity httpEntity = httpResponse.getEntity();
				is = httpEntity.getContent();
			}			
			

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			json = sb.toString();
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		// try parse the string to a JSON object
		try {
			jObj = new JSONObject(json);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		// return JSON String
		return jObj;

	}
	
	public static class RequestBuilder {
		String url = null;
		String method = "GET";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		public RequestBuilder() { }
		public RequestBuilder(String url) {
			this.url = url;
		}
		
		public String getUrl() {
			return url;
		}
		public String getMethod() {
			return method;
		}
		public void clearParams() {
			params.clear();
		}
		public List<NameValuePair> getParams() {
			return params;
		}
		public RequestBuilder addParam(String name, String value) {
			params.add(new BasicNameValuePair(name, value));
			return this;
		}
		public RequestBuilder addAllParams(Map<String, String> values) {
			for (Map.Entry<String, String> entry: values.entrySet()) {
				params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			return this;
		}
		public RequestBuilder setUrl(String url) {
			this.url = url;
			return this;
		}
		public RequestBuilder setMethod(String method) {
			this.method = method;
			return this;
		}
		public AsyncTask<Void, Void, JSONObject> makeRequest(final ResultHandler handler) {
			return new AsyncTask<Void, Void, JSONObject>() {
				protected JSONObject doInBackground(Void... params) {
					return makeHttpRequest();
				}
				protected void onPostExecute(JSONObject result) {
//				    try {
//                        Log.i("JSONParser", result == null ? "null" : result.toString(2));
//                    } catch (JSONException e1) {
//                        // TODO Auto-generated catch block
//                        e1.printStackTrace();
//                    }
					handleResults(result, handler);
				}
				@Override
				protected void onCancelled() {
					super.onCancelled();
					handler.onCancelled();
				}
				
			}.execute();
		}
		
		public void makeRequestInCurrentThread(final ResultHandler handler) {
			JSONObject result = makeHttpRequest();
			handleResults(result, handler);
		}
		
		public JSONObject makeHttpRequest() {
			return JSONParser.makeHttpRequest(url, method, params);
		}
		
	}
	
	public static void handleResults(JSONObject result, ResultHandler handler) {
		handler.onPostExecute();
		try {
			if (result.getBoolean("success"))
				handler.onSuccess(result.get("message"));
			else
				handler.onFailure(result.getString("message"));
		} catch (JSONException e) {
			handler.onError(e);
		} catch (IOException e) {
			handler.onError(e);
		} catch (NullPointerException e) {
			handler.onError(e);
		} catch (ClassCastException e) {
			handler.onError(e);
		}
	}
	
	public static interface ResultHandler {
	    public void onPostExecute();
        public void onSuccess(Object result) throws JSONException, IOException;
        public void onFailure(String errorMessage);
        public void onError(Exception e);
        public void onCancelled();
	}
	
	public static class SimpleResultHandler implements ResultHandler {
		private Context ctx;
		public SimpleResultHandler(Context ctx) {
			this.ctx = ctx;
		}
	    public void onPostExecute() { }
        public void onSuccess(Object result) throws JSONException, IOException { }
        public void onFailure(String errorMessage) { Log.w("GrantMobile", errorMessage); }
        public void onError(Exception e) {
        	e.printStackTrace();

        	new AlertDialog.Builder(ctx)
        	.setTitle("MSTC Grant App")
        	.setMessage("Connection error")
        	.setCancelable(false)
        	.setNeutralButton("OK", new DialogInterface.OnClickListener() {

        		public void onClick(DialogInterface dialog, int which) {
        			android.os.Process.killProcess(android.os.Process.myPid());
        		}
		})
		.show();
        }
        public void onCancelled() { }
	}
	
	public static class ResultHandlerWrapper implements ResultHandler {
		ResultHandler h;
		public ResultHandlerWrapper(ResultHandler h) { this.h = h; }
	    public void onPostExecute() { h.onPostExecute(); }
        public void onSuccess(Object result) throws JSONException, IOException { h.onSuccess(result); }
        public void onFailure(String errorMessage) { h.onFailure(errorMessage); }
        public void onError(Exception e) { h.onError(e); }
        public void onCancelled() { h.onCancelled(); }
	}
}

