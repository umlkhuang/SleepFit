package edu.uml.swin.sleepfit.DB;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "sensingdata")
public class SensingData implements Serializable {
	
	private static final long serialVersionUID = 31415926535897930L;
	
	@DatabaseField(generatedId = true, columnName = "id")
	private int id; 
	
	@DatabaseField(columnName = "createTime", index = true, canBeNull = false)
	private Date createTime; 
	
	@DatabaseField(columnName = "trackDate", index = true, canBeNull = false)
	private String trackDate;
	
	/*
	@DatabaseField(columnName = "month", canBeNull = false)
	private int month;
	
	@DatabaseField(columnName = "day", canBeNull = false)
	private int day;
	
	@DatabaseField(columnName = "dayOfWeek", canBeNull = false)
	private int dayOfWeek;
	
	@DatabaseField(columnName = "hour", canBeNull = false)
	private int hour;
	*/
	
	//@DatabaseField(columnName = "longitude", useGetSet = true)
	//private double longitude;
	
	//@DatabaseField(columnName = "latitude", useGetSet = true)
	//private double latitude; 
	
	@DatabaseField(columnName = "movement", useGetSet = true)
	private int movement;
	
	@DatabaseField(columnName = "illuminanceMax", useGetSet = true)
	private float illuminanceMax; 
	
	@DatabaseField(columnName = "illuminanceMin", useGetSet = true)
	private float illuminanceMin;
	
	@DatabaseField(columnName = "illuminanceAvg", useGetSet = true)
	private float illuminanceAvg;
	
	@DatabaseField(columnName = "illuminanceStd", useGetSet = true)
	private float illuminanceStd;
	
	@DatabaseField(columnName = "decibelMax", useGetSet = true)
	private float decibelMax;
	
	@DatabaseField(columnName = "decibelMin", useGetSet = true)
	private float decibelMin;
	
	@DatabaseField(columnName = "decibelAvg", useGetSet = true)
	private float decibelAvg;
	
	@DatabaseField(columnName = "decibelStd", useGetSet = true)
	private float decibelStd;
	
	@DatabaseField(columnName = "isCharging", useGetSet = true)
	private boolean isCharging;
	
	@DatabaseField(columnName = "powerLevel", useGetSet = true)
	private float powerlevel;
	
	@DatabaseField(columnName = "proximity", useGetSet = true)
	private float proximity;

	@DatabaseField(columnName = "ssid", useGetSet = true)
	private String ssid;
	
	@DatabaseField(columnName = "appUsage", useGetSet = true)
	private String appUsage; 
	
	@DatabaseField(columnName = "uploaded", useGetSet = true, defaultValue = "false") 
	private boolean uploaded;
	
	SensingData() {
		// needed by ormlite
	}
	
	public SensingData(long millis) {
		this.createTime = new Date(millis);
		this.trackDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(this.createTime); 
		/*
		Calendar cal = Calendar.getInstance();
		cal.setTime(createTime);
		this.month = cal.get(Calendar.MONTH);
		this.day = cal.get(Calendar.DAY_OF_MONTH);
		this.dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		this.hour = cal.get(Calendar.HOUR_OF_DAY);
		*/
		//this.longitude = Constants.INVALID_GEO_VALUE;
		//this.latitude = Constants.INVALID_GEO_VALUE;
		this.movement = 0;
		this.illuminanceMax = 0.0f;
		this.illuminanceMin = 0.0f;
		this.illuminanceAvg = 0.0f;
		this.illuminanceStd = 0.0f;
		this.decibelMax = 0.0f; 
		this.decibelMin = 0.0f;
		this.decibelAvg = 0.0f;
		this.decibelStd = 0.0f;
		this.isCharging = false;
		this.powerlevel = 1.0f;
		this.proximity = Float.MAX_VALUE;
		this.ssid = "";
		this.appUsage = "";
		this.uploaded = false;
	}
	
	public SensingData(long millis, int movement, float illuminanceMax, float illuminanceMin, float illuminanceAvg, float illuminanceStd, 
			float decibelMax, float decivelMin, float decibelAvg, float decibelStd, boolean isCharging, float powerLevel, 
			float proximity, String ssid, String appUsage) {
		this.createTime = new Date(millis);
		this.trackDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(this.createTime); 
		/*
		Calendar cal = Calendar.getInstance();
		cal.setTime(createTime);
		this.month = cal.get(Calendar.MONTH);
		this.day = cal.get(Calendar.DAY_OF_MONTH);
		this.dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		this.hour = cal.get(Calendar.HOUR_OF_DAY);
		*/
		//this.longitude = longitude;
		//this.latitude = latitude;
		this.movement = movement;
		this.illuminanceMax = illuminanceMax;
		this.illuminanceMin = illuminanceMin;
		this.illuminanceAvg = illuminanceAvg;
		this.illuminanceStd = illuminanceStd;
		this.decibelMax = decibelMax;
		this.decibelMin = decivelMin;
		this.decibelAvg = decibelAvg;
		this.decibelStd = decibelStd;
		this.isCharging = isCharging;
		this.powerlevel = powerLevel;
		this.proximity = proximity;
		this.ssid = ssid;
		this.appUsage = appUsage;
		this.uploaded = false;
	}
	
	@Override
	public String toString() {
		StringBuilder strBuf = new StringBuilder();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
		strBuf.append("CreateTime = ").append(dateFormatter.format(createTime));
		//strBuf.append(", ").append("Longitude = ").append(this.longitude);
		//strBuf.append(", ").append("Latitude = ").append(this.latitude);
		strBuf.append(", ").append("Movement = ").append(this.movement);
		strBuf.append(", ").append("IlluminanceMax = ").append(this.illuminanceMax);
		strBuf.append(", ").append("IlluminanceMin = ").append(this.illuminanceMin);
		strBuf.append(", ").append("IlluminanceAvg = ").append(this.illuminanceAvg);
		strBuf.append(", ").append("IlluminanceStd = ").append(this.illuminanceStd);
		strBuf.append(", ").append("DecibelMax = ").append(this.decibelMax);
		strBuf.append(", ").append("DecibelMin = ").append(this.decibelMin);
		strBuf.append(", ").append("DecibelAvg = ").append(this.decibelAvg);
		strBuf.append(", ").append("DecibelStd = ").append(this.decibelStd);
		strBuf.append(", ").append("IsCharging = ").append(this.isCharging);
		strBuf.append(", ").append("PowerLevel = ").append(this.powerlevel);
		strBuf.append(", ").append("Proximity = ").append(this.proximity);
		strBuf.append(", ").append("SSID = ").append(this.ssid);
		strBuf.append(", ").append("AppUsage = ").append(this.appUsage);
		return strBuf.toString();
	}

	public int getId() {
		return id;
	}
	
	public Date getCreateTime() {
		return createTime;
	}
	
	public String getTrackDate() {
		return trackDate;
	}
	/*
	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	*/

	public int getMovement() {
		return movement;
	}

	public void setMovement(int movement) {
		this.movement = movement;
	}
	
	public float getIlluminanceMax() {
		return illuminanceMax;
	}

	public void setIlluminanceMax(float illuminanceMax) {
		this.illuminanceMax = illuminanceMax;
	}

	public float getIlluminanceMin() {
		return illuminanceMin;
	}

	public void setIlluminanceMin(float illuminanceMin) {
		this.illuminanceMin = illuminanceMin;
	}

	public float getIlluminanceAvg() {
		return illuminanceAvg;
	}

	public void setIlluminanceAvg(float illuminanceAvg) {
		this.illuminanceAvg = illuminanceAvg;
	}
	
	public float getIlluminanceStd() {
		return illuminanceStd;
	}

	public void setIlluminanceStd(float illuminanceStd) {
		this.illuminanceStd = illuminanceStd;
	}

	public float getDecibelMax() {
		return decibelMax;
	}

	public void setDecibelMax(float decibelMax) {
		this.decibelMax = decibelMax;
	}

	public float getDecibelMin() {
		return decibelMin;
	}

	public void setDecibelMin(float decibelMin) {
		this.decibelMin = decibelMin;
	}

	public float getDecibelAvg() {
		return decibelAvg;
	}

	public void setDecibelAvg(float decibelAvg) {
		this.decibelAvg = decibelAvg;
	}
	
	public float getDecibelStd() {
		return decibelStd;
	}

	public void setDecibelStd(float decibelStd) {
		this.decibelStd = decibelStd;
	}

	public float getProximity() {
		return proximity;
	}

	public void setProximity(float proximity) {
		this.proximity = proximity;
	}

	public String getSsid() {
		return ssid;
	}
	
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}
	
	public String getAppUsage() {
		return appUsage;
	}

	public void setAppUsage(String appUsage) {
		this.appUsage = appUsage;
	}

	public boolean getIsCharging() {
		return isCharging;
	}

	public void setIsCharging(boolean isCharging) {
		this.isCharging = isCharging;
	}

	public float getPowerlevel() {
		return powerlevel;
	}

	public void setPowerlevel(float powerlevel) {
		this.powerlevel = powerlevel;
	}

	public boolean getUploaded() {
		return uploaded;
	}

	public void setUploaded(boolean uploaded) {
		this.uploaded = uploaded;
	}
	
}
