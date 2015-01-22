package edu.uml.swin.sleepfit.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.os.Environment;

public class SystemLoggerBak {
	
	private String mFileRoot;
	private File mSyslog;
	private FileWriter mFileOutput;
	private Context mContext;
	
	public SystemLoggerBak(Context context) {
		mContext = context;
		
		mFileRoot = Environment.getExternalStorageDirectory().toString() + "/SleepCollector/";
		File logFolder = new File(mFileRoot); 
	    if (!logFolder.exists()) {
	    	logFolder.mkdirs();
	    }
		mSyslog = new File(mFileRoot, Constants.LOG_FILE_NAME);
		if (!mSyslog.exists()) {
			try {
				mSyslog.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void addLog(String log) { 
		SimpleDateFormat dateFormatter = new SimpleDateFormat("[MM/dd HH:mm:ss]", Locale.US);
		Calendar now = Calendar.getInstance();
		StringBuilder strBuf = new StringBuilder();
		strBuf.append(dateFormatter.format(now.getTime()));
		strBuf.append(": ").append(log);
		strBuf.append("\n");
		try {
			mFileOutput = new FileWriter(mFileRoot + Constants.LOG_FILE_NAME, true);
			mFileOutput.write(strBuf.toString());
			mFileOutput.flush();
			mFileOutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
