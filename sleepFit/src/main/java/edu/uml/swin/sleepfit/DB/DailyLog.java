package edu.uml.swin.sleepfit.DB;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.j256.ormlite.field.DatabaseField;

public class DailyLog implements Serializable {
	
	private static final long serialVersionUID = 31415926535897938L;
	
	@DatabaseField(generatedId = true, columnName = "id")
	private int id; 

	@DatabaseField(columnName = "trackDate", index = true, canBeNull = false)
	private String trackDate;
	
	@DatabaseField(columnName = "createTime", canBeNull = false)
	private Date createTime;
	
	@DatabaseField(columnName = "numAwakenings", useGetSet = true, defaultValue = "0")
	private int numAwakenings;
	
	@DatabaseField(columnName = "timeAwake", useGetSet = true, defaultValue = "0")
	private int timeAwake; // in minutes 
	
	@DatabaseField(columnName = "timeToSleep", useGetSet = true, defaultValue = "0")
	private int timeToSleep; // in minutes
	
	@DatabaseField(columnName = "quality", useGetSet = true, defaultValue = "0")
	private int quality;
	
	@DatabaseField(columnName = "restored", useGetSet = true, defaultValue = "0") 
	private int restored;
	
	@DatabaseField(columnName = "stress", useGetSet = true, defaultValue = "0")
	private int stress;
	
	@DatabaseField(columnName = "depression", useGetSet = true, defaultValue = "0")
	private int depression;
	
	@DatabaseField(columnName = "fatigue", useGetSet = true, defaultValue = "0")
	private int fatigue;
	
	@DatabaseField(columnName = "sleepiness", useGetSet = true, defaultValue = "0")
	private int sleepiness;
	
	@DatabaseField(columnName = "uploaded", useGetSet = true, defaultValue = "false") 
	private boolean uploaded;
	
	DailyLog() {
		// needed by ormlite
	}
	
	public DailyLog(long millis) {
		this.createTime = new Date(millis);
		this.trackDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(this.createTime);
		this.numAwakenings = 0;
		this.timeAwake = 0;
		this.timeToSleep = 0;
		this.quality = 0;
		this.restored = 0;
		this.stress = 0;
		this.depression = 0;
		this.fatigue = 0;
		this.sleepiness = 0;
		this.uploaded = false;
	}
	
	@Override
	public String toString() {
		StringBuilder strBuf = new StringBuilder();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
		strBuf.append(", ").append("CreateTime = ").append(dateFormatter.format(this.createTime));
		strBuf.append(", ").append("TrackDate = ").append(this.trackDate);
		strBuf.append(", ").append("NumAwakenings = ").append(this.numAwakenings);
		strBuf.append(", ").append("TimeAwake = ").append(this.timeAwake);
		strBuf.append(", ").append("TimeToSleep = ").append(this.timeToSleep);
		strBuf.append(", ").append("Quality = ").append(this.quality);
		strBuf.append(", ").append("Restored = ").append(this.restored);
		strBuf.append(", ").append("Stress = ").append(this.stress);
		strBuf.append(", ").append("Depression = ").append(this.depression);
		strBuf.append(", ").append("Fatigue = ").append(this.fatigue);
		strBuf.append(", ").append("Sleepiness = ").append(this.sleepiness);
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

	public int getNumAwakenings() {
		return numAwakenings;
	}

	public void setNumAwakenings(int numAwakenings) {
		this.numAwakenings = numAwakenings;
	}

	public int getTimeAwake() {
		return timeAwake;
	}

	public void setTimeAwake(int timeAwake) {
		this.timeAwake = timeAwake;
	}

	public int getTimeToSleep() {
		return timeToSleep;
	}

	public void setTimeToSleep(int timeToSleep) {
		this.timeToSleep = timeToSleep;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public int getRestored() {
		return restored;
	}

	public void setRestored(int restored) {
		this.restored = restored;
	}

	public int getStress() {
		return stress;
	}

	public void setStress(int stress) {
		this.stress = stress;
	}

	public int getDepression() {
		return depression;
	}

	public void setDepression(int depression) {
		this.depression = depression;
	}

	public int getFatigue() {
		return fatigue;
	}

	public void setFatigue(int fatigue) {
		this.fatigue = fatigue;
	}

	public int getSleepiness() {
		return sleepiness;
	}

	public void setSleepiness(int sleepiness) {
		this.sleepiness = sleepiness;
	}

	public boolean getUploaded() {
		return uploaded;
	}

    public void setUploaded(boolean uploaded) {
		this.uploaded = uploaded;
	}
	
	
}
