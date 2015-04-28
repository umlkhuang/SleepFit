package edu.uml.swin.sleepfit.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import edu.uml.swin.sleepfit.DB.DailyLog;
import edu.uml.swin.sleepfit.DB.DatabaseHelper;
import edu.uml.swin.sleepfit.DB.LifestyleRaw;
import edu.uml.swin.sleepfit.DB.LightRaw;
import edu.uml.swin.sleepfit.DB.MovementRaw;
import edu.uml.swin.sleepfit.DB.ProximityRaw;
import edu.uml.swin.sleepfit.DB.SensingData;
import edu.uml.swin.sleepfit.DB.SleepLogger;
import edu.uml.swin.sleepfit.DB.SoundRaw;
import edu.uml.swin.sleepfit.DB.SysEvents;
import edu.uml.swin.sleepfit.DB.UserEvents;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class SyncWorker extends AsyncTask<Void, Void, Void> {

	private Context mContext;
	private long mTS;
	private ConnectionDetector mConnectionDetector;
	private DatabaseHelper mDatabaseHelper;
	private Dao<LifestyleRaw, Integer> mLifestyleDao;
	private Dao<LightRaw, Integer> mLightDao;
	private Dao<MovementRaw, Integer> mMovementDao;
	private Dao<ProximityRaw, Integer> mProximityDao;
	private Dao<SensingData, Integer> mSensingDataDao;
	private Dao<SoundRaw, Integer> mSoundDao;
	private Dao<SysEvents, Integer> mSysEventsDao;
	private Dao<DailyLog, Integer> mDailyLogDao;
	private Dao<SleepLogger, Integer> mSleepLoggerDao;
    private Dao<UserEvents, Integer> mUserEventsDao;
	
	private List<LifestyleRaw> lifestyleList = null;
	private List<LightRaw> lightList = null;
	private List<MovementRaw> movementList = null;
	private List<ProximityRaw> proximityList = null;
	private List<SensingData> sensingDataList = null;
	private List<SoundRaw> soundList = null;
	private List<SysEvents> sysEventList = null;
	private List<DailyLog> dailyLogList = null;
	private List<SleepLogger> sleepLogList = null;
    private List<UserEvents> userEventsList = null;
	
	private File mJsonFile;
	
	private static int REQUEST_TIMEOUT_SECONDS = 30;
	
	public SyncWorker(Context context, long TS) {
		mContext = context;
		mTS = TS;
		mConnectionDetector = new ConnectionDetector(mContext);
	}
	
	@Override
    protected void onPreExecute() {
    	super.onPreExecute();
    	
    	if (! mConnectionDetector.isConnectingToInternet()) {
    		Log.d(Constants.TAG, "WiFi is not connected to Internet, cannot upload file");
    		cancel(true);
    	}
    	
    	mDatabaseHelper = OpenHelperManager.getHelper(mContext, DatabaseHelper.class); 
		try {
			mLifestyleDao = mDatabaseHelper.getLifestyleRawDao();
			mLightDao = mDatabaseHelper.getLightRawDao();
			mMovementDao = mDatabaseHelper.getMovementRawDao();
			mProximityDao = mDatabaseHelper.getProximityRawDao();
			mSensingDataDao = mDatabaseHelper.getSensingDataDao();
			mSoundDao = mDatabaseHelper.getSoundRawDao();
			mSysEventsDao = mDatabaseHelper.getSysEventsDao();
			mDailyLogDao = mDatabaseHelper.getDailyLogDao();
			mSleepLoggerDao = mDatabaseHelper.getSleepLoggerDao();
            mUserEventsDao = mDatabaseHelper.getUserEventsDao();
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Cannot get the DAO: " + e.toString());
			e.printStackTrace();
			cancel(true);
		}
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		String jsonStr = getDataFromDatabase(); 
		if (jsonStr != null && (!jsonStr.equals("{}"))) {
			syncDatabase(jsonStr);
		} else {
			return null;
		}
		
		Calendar deleteTS = Calendar.getInstance();
		deleteTS.setTimeInMillis(mTS);
		deleteTS.add(Calendar.DAY_OF_YEAR, -1 * Constants.DELETE_OLD_DATA_DAYS);
		//Log.d(Constants.TAG, "==========   Delete TS: " + deleteTS.getTime());
		mDatabaseHelper.deleteOldData(deleteTS.getTime());
		
		return null;
	}
	
	private String getDataFromDatabase() {
		JSONObject json = new JSONObject();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US); 
		
		sleepLogList = getUnuploadedSleepLogData();
		if (sleepLogList != null && sleepLogList.size() > 0) {
			JSONArray sleepLogJson = new JSONArray();
			for (SleepLogger record : sleepLogList) {
				JSONObject tmp = new JSONObject();
				try {
					tmp.put("trackDate", record.getTrackDate());
					tmp.put("createTime", dateFormatter.format(record.getCreateTime()));
					tmp.put("sleepTime", dateFormatter.format(record.getSleepTime()));
					tmp.put("wakeupTime", dateFormatter.format(record.getWakeupTime()));
                    tmp.put("napTime", record.getNaptime());
					sleepLogJson.put(tmp);
				} catch (JSONException e) {
					e.printStackTrace();
					return null;
				}
			}
			try {
				json.put("sleepLog", sleepLogJson);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		dailyLogList = getUnuploadedDailyLogData(); 
		if (dailyLogList != null && dailyLogList.size() > 0) {
			JSONArray dailyLogJson = new JSONArray();
			for (DailyLog record : dailyLogList) {
				JSONObject tmp = new JSONObject();
				try {
					tmp.put("id", record.getId());
					tmp.put("trackDate", record.getTrackDate());
					tmp.put("createTime", dateFormatter.format(record.getCreateTime()));
					tmp.put("numAwakenings", record.getNumAwakenings());
					tmp.put("timeAwake", record.getTimeAwake());
					tmp.put("timeToSleep", record.getTimeToSleep());
					tmp.put("quality", record.getQuality());
					tmp.put("restored", record.getRestored());
					tmp.put("stress", record.getStress());
					tmp.put("depression", record.getDepression());
					tmp.put("fatigue", record.getFatigue());
					tmp.put("sleepiness", record.getSleepiness());
					dailyLogJson.put(tmp);
				} catch (JSONException e) {
					e.printStackTrace();
					return null;
				}
			}
			try {
				json.put("dailyLog", dailyLogJson);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		lifestyleList = getUnuploadedLifestyleData();
		if (lifestyleList != null && lifestyleList.size() > 0) {
			JSONArray lifestyleJson = new JSONArray(); 
			for (LifestyleRaw record : lifestyleList) {
				JSONObject tmp = new JSONObject();
				try {
					tmp.put("id", record.getId());
					tmp.put("trackDate", record.getTrackDate());
					tmp.put("createTime", dateFormatter.format(record.getCreateTime()));
					tmp.put("typeName", record.getType());
					tmp.put("typeId", record.getTypeId());
					tmp.put("logTime", dateFormatter.format(record.getLogTime()));
					tmp.put("selection", record.getSelection());
					tmp.put("note", record.getNote());
					lifestyleJson.put(tmp); 
				} catch (JSONException e) {
					e.printStackTrace();
					return null;
				}
			}
			try {
				json.put("lifestyleRaw", lifestyleJson);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		lightList = getUnuploadedLightData();
		if (lightList != null && lightList.size() > 0) {
			JSONArray lightJson = new JSONArray(); 
			for (LightRaw record : lightList) {
				JSONObject tmp = new JSONObject();
				try {
					tmp.put("id", record.getId());
					tmp.put("createTime", dateFormatter.format(record.getCreateTime()));
					tmp.put("data", record.getData());
					lightJson.put(tmp); 
				} catch (JSONException e) {
					e.printStackTrace();
					return null;
				}
			}
			try {
				json.put("lightRaw", lightJson);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		movementList = getUnuploadedMovementData();
		if (movementList != null && movementList.size() > 0) {
			JSONArray movementJson = new JSONArray();
			for (MovementRaw record : movementList) {
				JSONObject tmp = new JSONObject();
				try {
					tmp.put("id", record.getId());
					tmp.put("createTime", dateFormatter.format(record.getCreateTime()));
					tmp.put("data", record.getData());
					movementJson.put(tmp);
				} catch (JSONException e) {
					e.printStackTrace();
					return null;
				}
			}
			try {
				json.put("movementRaw", movementJson);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		proximityList = getUnuploadedProximityData();
		if (proximityList != null && proximityList.size() > 0) {
			JSONArray proximityJson = new JSONArray();
			for (ProximityRaw record : proximityList) {
				JSONObject tmp = new JSONObject();
				try {
					tmp.put("id", record.getId());
					tmp.put("createTime", dateFormatter.format(record.getCreateTime()));
					tmp.put("data", record.getData());
					proximityJson.put(tmp);
				} catch (JSONException e) {
					e.printStackTrace();
					return null;
				}
			}
			try {
				json.put("proximityRaw", proximityJson);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		sensingDataList = getUnuploadedSensingData();
		if (sensingDataList != null && sensingDataList.size() > 0) {
			JSONArray sensingDataJson = new JSONArray();
			for (SensingData record : sensingDataList) {
				JSONObject tmp = new JSONObject();
				try {
					tmp.put("id", record.getId());
					tmp.put("createTime", dateFormatter.format(record.getCreateTime()));
					tmp.put("trackDate", record.getTrackDate());
					tmp.put("movement", record.getMovement());
					tmp.put("illuminanceMax", record.getIlluminanceMax());
					tmp.put("illuminanceMin", record.getIlluminanceMin());
					tmp.put("illuminanceAvg", record.getIlluminanceAvg());
					tmp.put("illuminanceStd", record.getIlluminanceStd());
					tmp.put("decibelMax", record.getDecibelMax());
					tmp.put("decibelMin", record.getDecibelMin());
					tmp.put("decibelAvg", record.getDecibelAvg());
					tmp.put("decibelStd", record.getDecibelStd());
					tmp.put("isCharging", record.getIsCharging() ? 1 : 0); 
					tmp.put("powerLevel", record.getPowerlevel());
					tmp.put("proximity", record.getProximity());
					tmp.put("ssid", record.getSsid());
					tmp.put("appUsage", record.getAppUsage());
					sensingDataJson.put(tmp);
				}catch (JSONException e) {
					e.printStackTrace();
					return null;
				}
			}
			try {
				json.put("sensingData", sensingDataJson);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		soundList = getUnuploadedSoundData();
		if (proximityList != null && proximityList.size() > 0) {
			JSONArray soundJson = new JSONArray();
			for (SoundRaw record : soundList) {
				JSONObject tmp = new JSONObject();
				try {
					tmp.put("id", record.getId());
					tmp.put("createTime", dateFormatter.format(record.getCreateTime()));
					tmp.put("data", record.getData());
					soundJson.put(tmp);
				} catch (JSONException e) {
					e.printStackTrace();
					return null;
				}
			}
			try {
				json.put("soundRaw", soundJson);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		sysEventList = getUnuploadedSysEventsData();
		if (sysEventList != null && sysEventList.size() > 0) {
			JSONArray sysEventJson = new JSONArray();
			for (SysEvents record : sysEventList) {
				JSONObject tmp = new JSONObject();
				try {
					tmp.put("id", record.getId());
					tmp.put("createTime", dateFormatter.format(record.getCreateTime()));
					tmp.put("trackDate", record.getTrackDate());
					tmp.put("eventType", record.getEventType());
					sysEventJson.put(tmp);
				} catch (JSONException e) {
					e.printStackTrace();
					return null;
				}
			}
			try {
				json.put("sysEvents", sysEventJson);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

        userEventsList = getUnuploadedUserEventsData();
        if (userEventsList != null && userEventsList.size() > 0) {
            JSONArray userEventsJson = new JSONArray();
            for (UserEvents record : userEventsList) {
                JSONObject tmp = new JSONObject();
                try {
                    tmp.put("id", record.getId());
                    tmp.put("createTime", dateFormatter.format(record.getCreateTime()));
                    tmp.put("trackDate", record.getTrackDate());
                    tmp.put("dataStyle", record.getDataStyle());
                    tmp.put("data", record.getData());
                    userEventsJson.put(tmp);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            try {
                json.put("userEvents", userEventsJson);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
		
		String jsonStr = json.toString();
		/*
		String tmp = jsonStr;
		while (tmp.length() > 1000) {
			Log.d(Constants.TAG, tmp.substring(0, 1000));
			tmp = tmp.substring(1000);
		}
		if (tmp.length() > 0) 
			Log.d(Constants.TAG, tmp);
		*/
		
		return jsonStr;
	}
	
	private void syncDatabase(String jsonStr) {
		String DB_PATH;
		if (android.os.Build.VERSION.SDK_INT >= 4.2) {
	    	DB_PATH = mContext.getApplicationInfo().dataDir + "/databases/";
	    } else {
	    	DB_PATH = mContext.getFilesDir().getPath() + "/" + mContext.getPackageName() + "/databases/";
	    }
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmm", Locale.US); 
		String jsonFileName = Constants.getUUID(mContext) + "_" + dateFormatter.format(mTS);
		mJsonFile = new File(DB_PATH, jsonFileName + ".txt"); 
		try {
			FileWriter fw = new FileWriter(mJsonFile);
			fw.write(jsonStr);
			fw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}

        HttpParams paramConf = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(paramConf, REQUEST_TIMEOUT_SECONDS * 1000);
        HttpConnectionParams.setSoTimeout(paramConf, REQUEST_TIMEOUT_SECONDS * 500);
        HttpClient httpClient = new DefaultHttpClient(paramConf);
		HttpResponse httpResponse = null;
		try {
			HttpPost httpPost = new HttpPost(Constants.POST_FILE_URL);
			// Build the post 
			FileBody fileBody = new FileBody(mJsonFile);
			MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();
			reqEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addTextBody("UUID", Constants.getUUID(mContext));
			reqEntity.addTextBody("accessCode", Constants.getUUID(mContext));
			reqEntity.addTextBody("fileName", jsonFileName + ".txt");
			reqEntity.addPart("file", fileBody);
			httpPost.setEntity(reqEntity.build());
			httpResponse = httpClient.execute(httpPost);
			
			int responseCode = httpResponse.getStatusLine().getStatusCode();
			Log.d(Constants.TAG, "Syncing database data received response code: " + responseCode);
			
			// If upload file success, then set the uploaded flag to true 
			if (responseCode == 200) {
				setUploadedFlag();
				Log.d(Constants.TAG, "Sync data succeeded!");
			} else {
                // Need to delete the text file!
                try {
                    mJsonFile.delete();
                } catch (Exception e) {
                    Log.e(Constants.TAG, "Delete JSON file failed: " + e.toString());
                }

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
			
			// No matter uploading succeeded or not, delete the json file
			mJsonFile.delete();
		} catch (Exception e) {
			Log.d(Constants.TAG, e.toString());
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}
	
	private void setUploadedFlag() {
		try {
			UpdateBuilder<SleepLogger, Integer> updateBuilder = mSleepLoggerDao.updateBuilder();
			updateBuilder.where().eq("finished", true)
				.and().eq("uploaded", false);
			updateBuilder.updateColumnValue("uploaded", true);
			updateBuilder.update();
		} catch (SQLException e) {
			Log.d(Constants.TAG, "Update sleeplog upload result failed: " + e.toString());
			e.printStackTrace();
		} 
		
		try {
			UpdateBuilder<DailyLog, Integer> updateBuilder = mDailyLogDao.updateBuilder();
			updateBuilder.where().le("createTime", new Date(mTS))
						.and().eq("uploaded", false);
			updateBuilder.updateColumnValue("uploaded", true);
			updateBuilder.update();
		} catch (SQLException e) {
			Log.d(Constants.TAG, "Update dailylog upload result failed: " + e.toString());
			e.printStackTrace();
		} 
		
		try {
			UpdateBuilder<LifestyleRaw, Integer> updateBuilder = mLifestyleDao.updateBuilder();
			updateBuilder.where().le("createTime", new Date(mTS))
						.and().eq("uploaded", false);
			updateBuilder.updateColumnValue("uploaded", true);
			updateBuilder.update();
		} catch (SQLException e) {
			Log.d(Constants.TAG, "Update lifestyle upload result failed: " + e.toString());
			e.printStackTrace();
		} 
		
		try {
			UpdateBuilder<LightRaw, Integer> updateBuilder = mLightDao.updateBuilder();
			updateBuilder.where().le("createTime", new Date(mTS))
						.and().eq("uploaded", false);
			updateBuilder.updateColumnValue("uploaded", true);
			updateBuilder.update();
		} catch (SQLException e) {
			Log.d(Constants.TAG, "Update lightraw upload result failed: " + e.toString());
			e.printStackTrace();
		} 
		
		try {
			UpdateBuilder<MovementRaw, Integer> updateBuilder = mMovementDao.updateBuilder();
			updateBuilder.where().le("createTime", new Date(mTS))
						.and().eq("uploaded", false);
			updateBuilder.updateColumnValue("uploaded", true);
			updateBuilder.update();
		} catch (SQLException e) {
			Log.d(Constants.TAG, "Update movementraw upload result failed: " + e.toString());
			e.printStackTrace();
		} 
		
		try {
			UpdateBuilder<ProximityRaw, Integer> updateBuilder = mProximityDao.updateBuilder();
			updateBuilder.where().le("createTime", new Date(mTS))
						.and().eq("uploaded", false);
			updateBuilder.updateColumnValue("uploaded", true);
			updateBuilder.update();
		} catch (SQLException e) {
			Log.d(Constants.TAG, "Update proximityRaw upload result failed: " + e.toString());
			e.printStackTrace();
		} 
		
		try {
			UpdateBuilder<SensingData, Integer> updateBuilder = mSensingDataDao.updateBuilder();
			updateBuilder.where().le("createTime", new Date(mTS))
						.and().eq("uploaded", false);
			updateBuilder.updateColumnValue("uploaded", true);
			updateBuilder.update();
		} catch (SQLException e) {
			Log.d(Constants.TAG, "Update sensingData upload result failed: " + e.toString());
			e.printStackTrace();
		} 
		
		try {
			UpdateBuilder<SoundRaw, Integer> updateBuilder = mSoundDao.updateBuilder();
			updateBuilder.where().le("createTime", new Date(mTS))
						.and().eq("uploaded", false);
			updateBuilder.updateColumnValue("uploaded", true);
			updateBuilder.update();
		} catch (SQLException e) {
			Log.d(Constants.TAG, "Update soundRaw upload result failed: " + e.toString());
			e.printStackTrace();
		} 
		
		try {
			UpdateBuilder<SysEvents, Integer> updateBuilder = mSysEventsDao.updateBuilder();
			updateBuilder.where().le("createTime", new Date(mTS))
						.and().eq("uploaded", false);
			updateBuilder.updateColumnValue("uploaded", true);
			updateBuilder.update();
		} catch (SQLException e) {
			Log.d(Constants.TAG, "Update sysEvents upload result failed: " + e.toString());
			e.printStackTrace();
		}

        try {
            UpdateBuilder<UserEvents, Integer> updateBuilder = mUserEventsDao.updateBuilder();
            updateBuilder.where().le("createTime", new Date(mTS))
                         .and().eq("uploaded", false);
            updateBuilder.updateColumnValue("uploaded", true);
            updateBuilder.update();
        } catch (SQLException e) {
            Log.d(Constants.TAG, "Update userEvents upload result failed: " + e.toString());
            e.printStackTrace();
        }
    }
	
	private List<SleepLogger> getUnuploadedSleepLogData() {
		List<SleepLogger> retList = null;
		
		try {
			QueryBuilder<SleepLogger, Integer> queryBuilder = mSleepLoggerDao.queryBuilder();
			queryBuilder.where().eq("uploaded", false)
				.and()
				.eq("finished", true);
			PreparedQuery<SleepLogger> preparedQuery = queryBuilder.prepare();
			retList = mSleepLoggerDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Get all unuploaded sleep log data failed: " + e.toString());
			e.printStackTrace();
		}
		
		return retList;
	}
	
	private List<DailyLog> getUnuploadedDailyLogData() {
		List<DailyLog> retList = null;
		
		try {
			QueryBuilder<DailyLog, Integer> queryBuilder = mDailyLogDao.queryBuilder();
			queryBuilder.where().eq("uploaded", false)
				.and()
				.le("createTime", new Date(mTS));
			PreparedQuery<DailyLog> preparedQuery = queryBuilder.prepare();
			retList = mDailyLogDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Get all unuploaded daily log data failed: " + e.toString());
			e.printStackTrace();
		}
		
		return retList;
	}

	private List<LifestyleRaw> getUnuploadedLifestyleData() {
		List<LifestyleRaw> retList = null;
		
		try {
			QueryBuilder<LifestyleRaw, Integer> queryBuilder = mLifestyleDao.queryBuilder();
			queryBuilder.where().eq("uploaded", false)
						.and()
						.le("createTime", new Date(mTS));
			PreparedQuery<LifestyleRaw> preparedQuery = queryBuilder.prepare();
			retList = mLifestyleDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Get all unuploaded lifestyle raw data failed: " + e.toString());
			e.printStackTrace();
		}
		
		return retList;
	}
	
	private List<LightRaw> getUnuploadedLightData() {
		List<LightRaw> retList = null;
		
		try {
			QueryBuilder<LightRaw, Integer> queryBuilder = mLightDao.queryBuilder();
			queryBuilder.where().eq("uploaded", false)
						.and()
						.le("createTime", new Date(mTS));
			PreparedQuery<LightRaw> preparedQuery = queryBuilder.prepare();
			retList = mLightDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Get all unuploaded light raw data failed: " + e.toString());
			e.printStackTrace();
		}
		
		return retList;
	}
	
	private List<MovementRaw> getUnuploadedMovementData() {
		List<MovementRaw> retList = null;
		
		try {
			QueryBuilder<MovementRaw, Integer> queryBuilder = mMovementDao.queryBuilder();
			queryBuilder.where().eq("uploaded", false)
						.and()
						.le("createTime", new Date(mTS));
			PreparedQuery<MovementRaw> preparedQuery = queryBuilder.prepare();
			retList = mMovementDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Get all unuploaded movement raw data failed: " + e.toString());
			e.printStackTrace();
		}
		
		return retList;
	}
	
	private List<ProximityRaw> getUnuploadedProximityData() {
		List<ProximityRaw> retList = null;
		
		try {
			QueryBuilder<ProximityRaw, Integer> queryBuilder = mProximityDao.queryBuilder();
			queryBuilder.where().eq("uploaded", false)
						.and()
						.le("createTime", new Date(mTS));
			PreparedQuery<ProximityRaw> preparedQuery = queryBuilder.prepare();
			retList = mProximityDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Get all unuploaded proximity raw data failed: " + e.toString());
			e.printStackTrace();
		}
		
		return retList;
	}
	
	private List<SensingData> getUnuploadedSensingData() {
		List<SensingData> retList = null;
		
		try {
			QueryBuilder<SensingData, Integer> queryBuilder = mSensingDataDao.queryBuilder();
			queryBuilder.where().eq("uploaded", false)
						.and()
						.le("createTime", new Date(mTS));
			PreparedQuery<SensingData> preparedQuery = queryBuilder.prepare();
			retList = mSensingDataDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Get all unuploaded sensing data failed: " + e.toString());
			e.printStackTrace();
		}
		
		return retList;
	}
	
	private List<SoundRaw> getUnuploadedSoundData() {
		List<SoundRaw> retList = null;
		
		try {
			QueryBuilder<SoundRaw, Integer> queryBuilder = mSoundDao.queryBuilder();
			queryBuilder.where().eq("uploaded", false)
						.and()
						.le("createTime", new Date(mTS));
			PreparedQuery<SoundRaw> preparedQuery = queryBuilder.prepare();
			retList = mSoundDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Get all unuploaded sound raw data failed: " + e.toString());
			e.printStackTrace();
		}
		
		return retList;
	}
	
	private List<SysEvents> getUnuploadedSysEventsData() {
		List<SysEvents> retList = null;
		
		try {
			QueryBuilder<SysEvents, Integer> queryBuilder = mSysEventsDao.queryBuilder();
			queryBuilder.where().eq("uploaded", false)
						.and()
						.le("createTime", new Date(mTS));
			PreparedQuery<SysEvents> preparedQuery = queryBuilder.prepare();
			retList = mSysEventsDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Get all unuploaded sysevents data failed: " + e.toString());
			e.printStackTrace();
		}
		
		return retList;
	}

    private List<UserEvents> getUnuploadedUserEventsData() {
        List<UserEvents> retList = null;

        try {
            QueryBuilder<UserEvents, Integer> queryBuilder = mUserEventsDao.queryBuilder();
            queryBuilder.where().eq("uploaded", false)
                        .and()
                        .le("createTime", new Date(mTS));
            PreparedQuery<UserEvents> preparedQuery = queryBuilder.prepare();
            retList = mUserEventsDao.query(preparedQuery);
        } catch (SQLException e) {
            Log.e(Constants.TAG, "Get all unuploaded uservents data failed: " + e.toString());
            e.printStackTrace();
        }

        return retList;
    }
}
