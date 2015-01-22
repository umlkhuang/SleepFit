package edu.uml.swin.sleepfit.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class FileUploader extends AsyncTask<Void, Void, Void> {

	private Context mContext;
	//private SystemLogger mSysLogger;
	private ConnectionDetector mConnectionDetector;
	private String mDBName;
	
	private static int BUFFER_SIZE = 1024;
	
	private class GenericExtFilter implements FilenameFilter {
		private String ext;
		
		public GenericExtFilter(String ext) {
			this.ext = ext;
		}
		
		@Override
		public boolean accept(File dir, String filename) {
			return (filename.endsWith(ext));
		}
	}
	
	public FileUploader(Context context, String fileName) {
		mContext = context;
    	//mSysLogger = new SystemLogger(mContext);
    	mDBName = fileName;
    	mConnectionDetector = new ConnectionDetector(mContext);
	}
	
	@Override
    protected void onPreExecute() {
    	super.onPreExecute();
    	
    	if (mDBName.equals("")) {
    		if (! mConnectionDetector.isConnectingToInternet()) cancel(true);
    		return;
    	}
    	
		String DB_PATH;
		if (android.os.Build.VERSION.SDK_INT >= 4.2) {
	    	DB_PATH = mContext.getApplicationInfo().dataDir + "/databases/";
	    } else {
	    	DB_PATH = mContext.getFilesDir().getPath() + "/" + mContext.getPackageName() + "/databases/";
	    }
		
		String[] files = {DB_PATH + mDBName + ".db"};
		try {
			zip(files, DB_PATH + mDBName + ".zip"); 
			File dbFile = new File(DB_PATH, mDBName + ".db");
			while (!dbFile.delete())
				Log.d(Constants.TAG, "Delete DB file " + mDBName + ".db failed! Will try again. "); 
		} catch (IOException e1) {
			e1.printStackTrace(); 
		}
		Log.d(Constants.TAG, "Database file is zipped.");
		
    	if (! mConnectionDetector.isConnectingToInternet()) {
    		Log.d(Constants.TAG, "WiFi is not connected to Internet, cannot upload file");
    		cancel(true);
    	}
    }
	
	@Override
	protected Void doInBackground(Void... params) {
		String DB_PATH;
		if (android.os.Build.VERSION.SDK_INT >= 4.2) {
	    	DB_PATH = mContext.getApplicationInfo().dataDir + "/databases/";
	    } else {
	    	DB_PATH = mContext.getFilesDir().getPath() + "/" + mContext.getPackageName() + "/databases/";
	    }
		
		File dir = new File(DB_PATH);
		GenericExtFilter filter = new GenericExtFilter(".zip"); 
		String[] zipFiles = dir.list(filter);
		
		if (zipFiles == null) return null;
		
		for (String zipFile : zipFiles) {
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(Constants.POST_FILE_URL);
				File file = new File(DB_PATH + zipFile);
				// Check if the database has already been created 
				if (!file.exists()) {
					Log.d(Constants.TAG, "Database has not been created, cancel uploading");
					httpClient.getConnectionManager().shutdown();
					return null;
				}
				FileBody fileBody = new FileBody(file);
				
				// Build the post 
				MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();
				reqEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				reqEntity.addPart("file", fileBody);
				reqEntity.addTextBody("newFileName", zipFile);
				reqEntity.addTextBody("UUID", Constants.getUUID(mContext));
				reqEntity.addTextBody("accessCode", Constants.getUUID(mContext));
				httpPost.setEntity(reqEntity.build());
				
				// execute HTTP post request
				HttpResponse response = httpClient.execute(httpPost);
				int responseCode = response.getStatusLine().getStatusCode(); 

				Log.d(Constants.TAG, "Upload file received response code: " + responseCode);
				//mSysLogger.addLog("Upload data file received response code: " + responseCode);
				if (responseCode == 200) {
					file.delete();
				} else {
					Log.d(Constants.TAG, "Upload file failed");
					//mSysLogger.addLog("Upload data file failed");
					HttpEntity resEntity = response.getEntity();
					Log.d(Constants.TAG, "Response String: " + EntityUtils.toString(resEntity).trim());
					resEntity.consumeContent();
				}
				
				httpClient.getConnectionManager().shutdown();
			} catch (NullPointerException e) {
	        	e.printStackTrace();
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
		}
		
		SharedPreferences preferences = mContext.getSharedPreferences(Constants.UPDATE_TIME_FILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong("lastUploadTS", System.currentTimeMillis()); 
		editor.commit();
		
		return null;
	}
	
	private void zip(String[] files, String zipFile) throws IOException {
		BufferedInputStream origin = null;
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
		try {
			byte data[] = new byte[BUFFER_SIZE];
			for (int i = 0; i < files.length; i++) {
				Log.d(Constants.TAG, "Zipping file: " + files[i]);
				FileInputStream fi = new FileInputStream(files[i]);
				origin = new BufferedInputStream(fi, BUFFER_SIZE);
				try {
					ZipEntry entry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1));
					out.putNextEntry(entry);
					int count;
					while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
						out.write(data, 0, count);
					}
				} finally {
					origin.close();
				}
			}
		} finally  {
			out.close();
		}
	}
	
}
