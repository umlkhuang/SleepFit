package edu.uml.swin.sleepfit.sensing;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import edu.uml.swin.sleepfit.DB.DatabaseHelper;
import edu.uml.swin.sleepfit.DB.LightRaw;
import edu.uml.swin.sleepfit.DB.SoundRaw;
import edu.uml.swin.sleepfit.util.Constants;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DecibelProber {
	
	private int mDecibelValue;
	private Context mContext;
	private String mOutputFile;
	private MediaRecorder mRecorder;
	private Handler mHandler;
	private ArrayList<Integer> mValues;
	private DatabaseHelper mDatabaseHelper;
	private Dao<SoundRaw, Integer> mDao;
	
	public DecibelProber(Context context) {
		mContext = context;
		mDecibelValue = 0; 
		mValues = new ArrayList<Integer>();
		mOutputFile = Environment.getExternalStorageDirectory().toString() + "/sleepFitAudio.3gp";
		
		// No need to save all decibel raw data (Sep 1, 2014) 
		
		mDatabaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
		try {
			mDao = mDatabaseHelper.getSoundRawDao();
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Cannot get the SoundRaw DAO: " + e.toString());
			e.printStackTrace();
		}
		
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case Constants.DECIBEL_RECORD_TIMEOUT_MSG:
						if (mRecorder != null) {
							int maxAmplitude = mRecorder.getMaxAmplitude();
							mDecibelValue = (int) (20 * Math.log10(maxAmplitude / 4.5));
					        mRecorder.stop();
					        mRecorder.reset();
					        mRecorder.release();
					        mRecorder = null;
						}
				        mValues.add(mDecibelValue); 
				        //Log.d(Constants.TAG, "++++++++++++++++++++++++ 2 ");
				        Log.d(Constants.TAG, "Decibel value = " + mDecibelValue);
				        mDecibelValue = 40;
				        
				        sendEmptyMessageDelayed(Constants.DECIBEL_RECORD_IDLE_FINISH_MSG, (Constants.SUB_SENSING_DUTY_INTERVAL_SECOND - Constants.DECIBEL_TIMEOUT_SECOND) * 1000);
				        break;
					case Constants.DECIBEL_RECORD_IDLE_FINISH_MSG:
					    //Log.d(Constants.TAG, "++++++++++++++++++++++++ 3 ");
						try {
						    mRecorder = new MediaRecorder();
							mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
							mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
							mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
							mRecorder.setOutputFile(mOutputFile);
					    } catch (SecurityException e) {
					    	Log.d(Constants.TAG, "DecibelMeter, SecurityException " + Log.getStackTraceString(e));
					    	mRecorder = null;
					    	if (mValues.size() >= 1)
					    		mDecibelValue = mValues.get(mValues.size() - 1);
					    	else
					    		mDecibelValue = 40;
					    } catch (Exception e) {
					    	Log.d(Constants.TAG, "DecibelMeter, Exception " + Log.getStackTraceString(e));
					    	mRecorder = null;
					    	if (mValues.size() >= 1)
					    		mDecibelValue = mValues.get(mValues.size() - 1);
					    	else
					    		mDecibelValue = 40;
					    }
					    
					    if (mRecorder != null) {
						    try {
						    	mRecorder.prepare();
						    } catch (IOException ioe) {
						    	Log.d(Constants.TAG, "DecibelMeter, Prepare() IOException " + Log.getStackTraceString(ioe));
								mRecorder.reset();
						        mRecorder.release();
						    	mRecorder = null;
						    	if (mValues.size() >= 1)
						    		mDecibelValue = mValues.get(mValues.size() - 1);
						    	else
						    		mDecibelValue = 40;
						    } catch (IllegalStateException e) {
						    	Log.d(Constants.TAG, "DecibelMeter, Prepare() IllegalStateException " + Log.getStackTraceString(e)); 
								mRecorder.reset();
						        mRecorder.release();
						    	mRecorder = null;
						    	if (mValues.size() >= 1)
						    		mDecibelValue = mValues.get(mValues.size() - 1);
						    	else
						    		mDecibelValue = 40;
						    } 
						    
						    if (mRecorder != null) {
							    try {
							    	mRecorder.start();
							    	mRecorder.getMaxAmplitude();
							    } catch (IllegalStateException e) {
							    	Log.d(Constants.TAG, "DecibelMeter, start() IllegalStateException " + Log.getStackTraceString(e)); 
									mRecorder.reset();
							        mRecorder.release();
							    	mRecorder = null;
							    	if (mValues.size() >= 1)
							    		mDecibelValue = mValues.get(mValues.size() - 1);
							    	else
							    		mDecibelValue = 40;
							    } 
						    }
					    }
					    
					    mHandler.sendEmptyMessageDelayed(Constants.DECIBEL_RECORD_TIMEOUT_MSG, Constants.DECIBEL_TIMEOUT_SECOND * 1000);
					    break;
				     default:
				    	 super.handleMessage(msg);
				    	 break;
				}
			}
		};
	}
	
	public void startService() {
		try {
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			mRecorder.setOutputFile(mOutputFile);
	    } catch (SecurityException e) {
	    	Log.d(Constants.TAG, "DecibelMeter, SecurityException " + Log.getStackTraceString(e));
	    	mRecorder = null;
	    	mDecibelValue = 40;
	    } 
		
	    if (mRecorder != null) {
		    try {
		    	mRecorder.prepare(); 
		    } catch (IOException ioe) {
		    	Log.d(Constants.TAG, "DecibelMeter, Prepare() IOException " + Log.getStackTraceString(ioe));
				mRecorder.reset();
		        mRecorder.release();
		    	mRecorder = null;
		    	mDecibelValue = 40;
		    } catch (IllegalStateException e) {
		    	Log.d(Constants.TAG, "DecibelMeter, Prepare() IllegalStateException " + Log.getStackTraceString(e)); 
				mRecorder.reset();
		        mRecorder.release();
		    	mRecorder = null;
		    	mDecibelValue = 40;
		    } 
		    
		    if (mRecorder != null) {
			    try {
			    	mRecorder.start();
			    	mRecorder.getMaxAmplitude();
			    } catch (IllegalStateException e) {
			    	Log.d(Constants.TAG, "DecibelMeter, start() IllegalStateException " + Log.getStackTraceString(e)); 
					mRecorder.reset();
			        mRecorder.release();
			    	mRecorder = null;
			    	mDecibelValue = 40;
			    }
		    }
	    }
	    
	    mHandler.sendEmptyMessageDelayed(Constants.DECIBEL_RECORD_TIMEOUT_MSG, Constants.DECIBEL_TIMEOUT_SECOND * 1000);
	    //Log.d(Constants.TAG, "++++++++++++++++++++++++ 1 ");
	} 
	
	public void stopService() {
		mHandler = null;
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.reset();
	        mRecorder.release(); 
	        mRecorder = null;
		}
	}
	
	public ArrayList<Integer> getDecibelValue() {
		ArrayList<Integer> ret = mValues;
		mValues = new ArrayList<Integer>();
		
		// No need to save all decibel raw data (Sep 1, 2014) 
		
		SoundRaw newRaw = new SoundRaw(System.currentTimeMillis(), ret);
		try {
			mDao.create(newRaw);
		} catch (SQLException e) {
			Log.d(Constants.TAG, "Add new SoundRaw data record failed: " + e.toString());
			e.printStackTrace();
		}
		
		return ret;
	}
	
}
