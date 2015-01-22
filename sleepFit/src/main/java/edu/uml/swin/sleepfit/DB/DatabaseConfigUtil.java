package edu.uml.swin.sleepfit.DB;

import java.io.IOException;
import java.sql.SQLException;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil; 

/**
 * Database helper class used to manage the creation and upgrading of your database. 
 * This class also usually provides the DAOs used by the other classes.
 * 
 * You will need to run this utility locally on your development box (not in an Android
 * device), whenever you make a change to one of your data classes. This means that 
 * right now, this must be done by hand to keep the configuration file in sync with
 * your database classes. To run the utility you will need to use the local Java runtime 
 * environment (JRE). Under eclipse, edit the "Run Configuration" for the utility, select
 * the JRE tab, and select an alternative JRE (1.5 or 1.6). Your project's JRE should be
 * undefined since it is an Android application. You'll also need to remove the Android
 * bootstrap entry from the Classpath tab.
 * 
 * When the utility is run it should create the ormlite_config.txt configuration file 
 * in the raw resource folder. *This folder must exist before the utility is run*. 
 * The first time you create the config file in the resource folder, the Android build 
 * plugin should add it to the R.java file inside of the gen folder. This defines a 
 * unique integer value so that the application can open this resource by file-id number.
 * 
 * After the R.java file entry has been generated, you will need to enable the reading
 * of the file at runtime. Inside of your DatabaseHelper class, you will need to change
 * the constructor to add the integer file-id. The constructor will look something like 
 * the following:
 * 
 * public DatabaseHelper(Context context) {
 *     super(context, DATABASE_NAME, null, DATABASE_VERSION,
 *     R.raw.ormlite_config);
 * }
 * 
 * Notice the R.raw.ormlite_config entry at the end that passes the file-id to the super
 * class so it can be read in. You can also pass in a file-name or a Java File if you want
 * to load in the config file from another location.
 */

public class DatabaseConfigUtil extends OrmLiteConfigUtil {

	// Specify all database classes (goals) to be processed 
	private static final Class<?>[] tables = new Class[] {
		SensingData.class,
		SleepLogger.class,
		SysEvents.class,
		MovementRaw.class,
		LightRaw.class,
		SoundRaw.class,
		ProximityRaw.class,
		LifestyleRaw.class,
		DailyLog.class
	};
		
	public static void main(String[] args) throws SQLException, IOException {
		writeConfigFile("ormlite_config.txt", tables);
	}	
}
