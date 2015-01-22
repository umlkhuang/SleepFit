package edu.uml.swin.sleepfit.DB;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import edu.uml.swin.sleepfit.R;
import edu.uml.swin.sleepfit.util.Constants;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	
	private static final String DATABASE_NAME = Constants.DB_NAME;
	private static final int DATABASE_VERSION = 1;
	
	private Dao<SensingData, Integer> sensingDataDao = null;
	private Dao<SleepLogger, Integer> sleepLoggerDao = null;
	private Dao<SysEvents, Integer> sysEventsDao = null;
	private Dao<MovementRaw, Integer> movementRawDao = null;
	private Dao<LightRaw, Integer> lightRawDao = null;
	private Dao<SoundRaw, Integer> soundRawDao = null;
	private Dao<ProximityRaw, Integer> proximityRawDao = null;
	private Dao<LifestyleRaw, Integer> lifestyleRawDao = null;
	private Dao<DailyLog, Integer> dailyLogDao = null;
	
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

	@Override
    public void onCreate(SQLiteDatabase arg0, ConnectionSource arg1) {
		Log.d(Constants.TAG, "DatabaseHelper onCreate()");
	    
		try {
			TableUtils.createTable(connectionSource, SensingData.class);
			TableUtils.createTable(connectionSource, SleepLogger.class);
			TableUtils.createTable(connectionSource, SysEvents.class);
			TableUtils.createTable(connectionSource, MovementRaw.class);
			TableUtils.createTable(connectionSource, LightRaw.class);
			TableUtils.createTable(connectionSource, SoundRaw.class);
			TableUtils.createTable(connectionSource, ProximityRaw.class);
			TableUtils.createTable(connectionSource, LifestyleRaw.class);
			TableUtils.createTable(connectionSource, DailyLog.class);
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Cannot create database: " + e.toString());
			throw new RuntimeException(e);
		}
    }
	
	public void resetTables() {
		Log.d(Constants.TAG, "DatabaseHelper resetTables()");
		
		/*
		 * The bedtime and waketime will be calculated on server, user will download
		 * the bedtime/waketime periodically and if user updated the time, the finished
		 * flag will be set to be true. It is not the same as SleepCollector. 
		 */
		try {
			TableUtils.dropTable(connectionSource, SensingData.class, true);
			//TableUtils.dropTable(connectionSource, SleepLogger.class, true);
			TableUtils.dropTable(connectionSource, SysEvents.class, true);
			TableUtils.dropTable(connectionSource, MovementRaw.class, true);
			TableUtils.dropTable(connectionSource, LightRaw.class, true);
			TableUtils.dropTable(connectionSource, SoundRaw.class, true);
			TableUtils.dropTable(connectionSource, ProximityRaw.class, true);
			TableUtils.dropTable(connectionSource, LifestyleRaw.class, true);
			TableUtils.dropTable(connectionSource, DailyLog.class, true);
			
			TableUtils.createTable(connectionSource, SensingData.class);
			//TableUtils.createTable(connectionSource, SleepLogger.class);
			TableUtils.createTable(connectionSource, SysEvents.class); 
			TableUtils.createTable(connectionSource, MovementRaw.class);
			TableUtils.createTable(connectionSource, LightRaw.class);
			TableUtils.createTable(connectionSource, SoundRaw.class);
			TableUtils.createTable(connectionSource, ProximityRaw.class);
			TableUtils.createTable(connectionSource, LifestyleRaw.class);
			TableUtils.createTable(connectionSource, DailyLog.class);
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Cannot reset database: " + e.toString());
			throw new RuntimeException(e);
		}
	}
	
	public void deleteOldData(Date TS) {
		String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(TS);
		Log.d(Constants.TAG, "Last delete TS: " + dateStr);
		
		try {
			if (lifestyleRawDao == null) {
				lifestyleRawDao = getDao(LifestyleRaw.class);
			}
			DeleteBuilder<LifestyleRaw, Integer> builder1 = lifestyleRawDao.deleteBuilder();
			builder1.where().le("logTime", TS);
			lifestyleRawDao.delete(builder1.prepare());
			
			if (lightRawDao == null) {
				lightRawDao = getDao(LightRaw.class);
			}
			DeleteBuilder<LightRaw, Integer> builder2 = lightRawDao.deleteBuilder();
			builder2.where().le("createTime", TS);
			lightRawDao.delete(builder2.prepare());
			
			if (movementRawDao == null) {
				movementRawDao = getDao(MovementRaw.class);
			}
			DeleteBuilder<MovementRaw, Integer> builder3 = movementRawDao.deleteBuilder();
			builder3.where().le("createTime", TS);
			movementRawDao.delete(builder3.prepare());
			
			if (proximityRawDao == null) {
				proximityRawDao = getDao(ProximityRaw.class);
			}
			DeleteBuilder<ProximityRaw, Integer> builder4 = proximityRawDao.deleteBuilder();
			builder4.where().le("createTime", TS);
			proximityRawDao.delete(builder4.prepare());
			
			if (sensingDataDao == null) {
				sensingDataDao = getDao(SensingData.class);
			}
			DeleteBuilder<SensingData, Integer> builder5 = sensingDataDao.deleteBuilder();
			builder5.where().le("createTime", TS);
			sensingDataDao.delete(builder5.prepare());
			
			if (sysEventsDao == null) {
				sysEventsDao = getDao(SysEvents.class);
			}
			DeleteBuilder<SysEvents, Integer> builder6 = sysEventsDao.deleteBuilder();
			builder6.where().le("createTime", TS);
			sysEventsDao.delete(builder6.prepare());
			
			if (soundRawDao == null) {
				soundRawDao = getDao(SoundRaw.class);
			}
			DeleteBuilder<SoundRaw, Integer> builder7 = soundRawDao.deleteBuilder();
			builder7.where().le("createTime", TS);
			soundRawDao.delete(builder7.prepare());
			
			if (dailyLogDao == null) {
				dailyLogDao = getDao(DailyLog.class);
			}
			DeleteBuilder<DailyLog, Integer> builder8 = dailyLogDao.deleteBuilder();
			builder8.where().le("createTime", TS);
			dailyLogDao.delete(builder8.prepare());
			
			if (sleepLoggerDao == null) {
				sleepLoggerDao = getDao(SleepLogger.class);
			}
			DeleteBuilder<SleepLogger, Integer> builder9 = sleepLoggerDao.deleteBuilder();
			builder9.where().le("wakeupTime", TS);
			sleepLoggerDao.delete(builder9.prepare());
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Delete old data failed: " + e.toString());
			throw new RuntimeException(e);
		}
	}

	@Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		// Nothing here 
    }
	
	@Override 
	public void close() {
		super.close();
		sensingDataDao = null;
		sleepLoggerDao = null;
		sysEventsDao = null;
		// No need to save all raw data (Sep 2, 2014) 
		
		movementRawDao = null;
		lightRawDao = null;
		soundRawDao = null;
		proximityRawDao = null;
		lifestyleRawDao = null;
		dailyLogDao = null;
	}
	
	public Dao<SensingData, Integer> getSensingDataDao() throws SQLException {
		if (sensingDataDao == null) {
			sensingDataDao = getDao(SensingData.class);
		}
		return sensingDataDao;
	}
	
	public Dao<SleepLogger, Integer> getSleepLoggerDao() throws SQLException {
		if (sleepLoggerDao == null) {
			sleepLoggerDao = getDao(SleepLogger.class);
		}
		return sleepLoggerDao;
	}
	
	public Dao<SysEvents, Integer> getSysEventsDao() throws SQLException {
		if (sysEventsDao == null) {
			sysEventsDao = getDao(SysEvents.class);
		}
		return sysEventsDao;
	}
	
	// No need to save all raw data (Sep 2, 2014) 
	
	public Dao<MovementRaw, Integer> getMovementRawDao() throws SQLException {
		if (movementRawDao == null) {
			movementRawDao = getDao(MovementRaw.class);
		}
		return movementRawDao;
	}
	
	public Dao<LightRaw, Integer> getLightRawDao() throws SQLException {
		if (lightRawDao == null) {
			lightRawDao = getDao(LightRaw.class);
		}
		return lightRawDao;
	}
	
	public Dao<SoundRaw, Integer> getSoundRawDao() throws SQLException {
		if (soundRawDao == null) {
			soundRawDao = getDao(SoundRaw.class);
		}
		return soundRawDao;
	}
	
	public Dao<ProximityRaw, Integer> getProximityRawDao() throws SQLException {
		if (proximityRawDao == null) {
			proximityRawDao = getDao(ProximityRaw.class);
		}
		return proximityRawDao;
	}
	
	public Dao<LifestyleRaw, Integer> getLifestyleRawDao() throws SQLException {
		if (lifestyleRawDao == null) {
			lifestyleRawDao = getDao(LifestyleRaw.class);
		}
		return lifestyleRawDao;
	}
	
	public Dao<DailyLog, Integer> getDailyLogDao() throws SQLException {
		if (dailyLogDao == null) {
			dailyLogDao = getDao(DailyLog.class);
		}
		return dailyLogDao;
	}
}
