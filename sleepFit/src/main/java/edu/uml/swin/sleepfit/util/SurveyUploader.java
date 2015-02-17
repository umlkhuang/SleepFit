package edu.uml.swin.sleepfit.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class SurveyUploader extends AsyncTask<Void, Void, Void> {

	private Context mContext;
	private ConnectionDetector mConnectionDetector;
	
	public SurveyUploader(Context context) {
		mContext = context;
    	mConnectionDetector = new ConnectionDetector(mContext);
	}
	
	@Override
    protected void onPreExecute() {
    	super.onPreExecute();
    	
    	if (! mConnectionDetector.isConnectingToInternet()) {
    		Log.d(Constants.TAG, "WiFi is not connected to Internet, cannot upload file");
    		cancel(true);
    	}
    }
	
	@Override
	protected Void doInBackground(Void... params) {
		String UUID;
		String CID;
		String age;
		String gender;
		String racial;
		String sleepHours;
		
		SharedPreferences preferences = mContext.getSharedPreferences(Constants.SURVEY_FILE_NAME, Context.MODE_PRIVATE);
		UUID = preferences.getString("UUID", Constants.getUUID(mContext));
		CID = preferences.getString("CID", Constants.getUUID(mContext));
		age = preferences.getString("age", "");
		gender = preferences.getString("gender", "");
		racial = preferences.getString("racial", "");
		sleepHours = preferences.getString("sleepHours", ""); 

        HttpParams paramConf = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(paramConf, 5 * 1000);
        HttpConnectionParams.setSoTimeout(paramConf, 10 * 1000);
        HttpClient httpClient = new DefaultHttpClient(paramConf);
		HttpResponse httpResponse = null;
		try {
			HttpPost httpPost = new HttpPost(Constants.POST_SURVEY_URL);
			
			MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();
			reqEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addTextBody("UUID", UUID);
			reqEntity.addTextBody("accessCode", CID);
			reqEntity.addTextBody("age", age);
			reqEntity.addTextBody("gender", gender);
			reqEntity.addTextBody("racial", racial);
			reqEntity.addTextBody("sleepHours", sleepHours);
			httpPost.setEntity(reqEntity.build());
			httpResponse = httpClient.execute(httpPost); 
			
			int responseCode = httpResponse.getStatusLine().getStatusCode();
			Log.d(Constants.TAG, "Upload user survey received response code: " + responseCode);
			
			if (responseCode == 200) {
				updatePreferenceFile();
			} else {
				HttpEntity resEntity = httpResponse.getEntity();
				String resStr = EntityUtils.toString(resEntity).trim();
				while (resStr.length() > 1000) {
					Log.d(Constants.TAG, resStr.substring(0, 1000));
					resStr = resStr.substring(1000);
				}
				if (resStr.length() > 0) 
					Log.d(Constants.TAG, resStr);
				resEntity.consumeContent();
			}
		} catch (Exception e) {
			Log.d(Constants.TAG, e.toString());
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		
		return null;
	}

	private void updatePreferenceFile() {
		SharedPreferences preferences = mContext.getSharedPreferences(Constants.SURVEY_FILE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("uploaded", true);
		editor.commit();
	}

}
