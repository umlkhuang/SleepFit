package edu.uml.swin.sleepfit.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class HttpRequestTask extends AsyncTask<String, Void, String> {

	public interface HttpRequestCallback {
		void onRequestTaskCompleted(String json);
	}
	
	private HttpRequestCallback mCallback;
	private Context mContext;
	private ConnectionDetector mConnectionDetector;
	
	private static int REQUEST_TIMEOUT_SECONDS = 10;
	
	public HttpRequestTask(Context context, HttpRequestCallback callback) {
		mContext = context;
		mConnectionDetector = new ConnectionDetector(mContext);
		mCallback = callback;
	}
	
	@Override
    protected void onPreExecute() {
		super.onPreExecute();
		
		if (! mConnectionDetector.isInternetAvailable()) {
			mCallback.onRequestTaskCompleted(null); 
			cancel(true);
			return;
		}
	}
	
	@Override
	protected String doInBackground(String... params) {
		//JSONObject ret = null;
		
		int count = params.length;
		if (count != 1) {
			return null;
		}

        HttpParams paramConf = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(paramConf, REQUEST_TIMEOUT_SECONDS * 1000);
        HttpConnectionParams.setSoTimeout(paramConf, REQUEST_TIMEOUT_SECONDS * 1500);
        HttpClient httpClient = new DefaultHttpClient(paramConf);
		HttpResponse httpResponse = null;
		String url = params[0];
		try {
			HttpGet httpGet = new HttpGet(url);
			httpResponse = httpClient.execute(httpGet);
		} catch (Exception e) {
			Log.d(Constants.TAG, e.toString());
			httpClient.getConnectionManager().shutdown();
			return null;
		} 
		
		String jsonString = null;
		if (httpResponse != null && httpResponse.getStatusLine().getStatusCode() == 200) {
			try {
				HttpEntity httpEntity = httpResponse.getEntity();
				InputStream is = httpEntity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 256);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				jsonString = sb.toString();
				httpEntity.consumeContent();
			} catch (Exception e) {
				Log.e(Constants.TAG, "Response string buffer error. " + e.getMessage());
			} finally {
				httpClient.getConnectionManager().shutdown();
			}
		}
		
		return jsonString;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		mCallback.onRequestTaskCompleted(result);
	}
}
