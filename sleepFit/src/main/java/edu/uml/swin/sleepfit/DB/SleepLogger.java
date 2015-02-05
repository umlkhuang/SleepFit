package edu.uml.swin.sleepfit.DB;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "sleeplogger")
public class SleepLogger implements Serializable {
	
	private static final long serialVersionUID = 31415926535897931L;
	
	@DatabaseField(generatedId = true, columnName = "id")
	private int id; 
	
	@DatabaseField(columnName = "createTime", canBeNull = false)
	private Date createTime; 
	
	@DatabaseField(columnName = "trackDate", index = true, useGetSet = true)
	private String trackDate;
	
	@DatabaseField(columnName = "sleepTime", useGetSet = true)
	private Date sleepTime;
	
	@DatabaseField(columnName = "wakeupTime", useGetSet = true)
	private Date wakeupTime;
	
	@DatabaseField(columnName = "quality", useGetSet = true)
	private int quality;

    @DatabaseField(columnName = "naptime", useGetSet = true, defaultValue = "0")
    private int naptime;
	
	@DatabaseField(columnName = "finished", useGetSet = true)
	private boolean finished;
	
	@DatabaseField(columnName = "uploaded", useGetSet = true, defaultValue = "false") 
	private boolean uploaded;
	
	SleepLogger() {
		// Needed by ormLite
	}
	
	public SleepLogger(long millis, String trackDate) {
		this.createTime = new Date(millis);
		this.trackDate = trackDate;
		this.sleepTime = null;
		this.wakeupTime = null;
		this.quality = 3;
        this.naptime = 0;
		this.finished = false;
		this.uploaded = false;
	}
	
	public SleepLogger(long millis, String trackDate, Date sleepTime, Date wakeupTime, int naptime, int quality, boolean finished, boolean uploaded) {
		this.createTime = new Date(millis);
		this.trackDate = trackDate;
		this.sleepTime = sleepTime;
		this.wakeupTime = wakeupTime;
		this.quality = quality;
        this.naptime = naptime;
		this.finished = finished;
		this.uploaded = uploaded;
	}
	
	@Override 
	public String toString() {
		StringBuilder strBuf = new StringBuilder();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
		strBuf.append("CreateTime = ").append(dateFormatter.format(this.createTime));
		strBuf.append(", ").append("TrackDate = ").append(trackDate);
		strBuf.append(", ").append("SleepTime = ").append(dateFormatter.format(this.sleepTime));
		strBuf.append(", ").append("WakeupTime = ").append(dateFormatter.format(this.wakeupTime));
		strBuf.append(", ").append("Quality = ").append(this.quality);
        strBuf.append(", ").append("NapTime = ").append(this.naptime);
		strBuf.append(", ").append("Finished = ").append(this.finished);
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

	public void setTrackDate(String trackDate) {
		this.trackDate = trackDate;
	}

	public Date getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(Date sleepTime) {
		this.sleepTime = sleepTime;
	}

	public Date getWakeupTime() {
		return wakeupTime;
	}

	public void setWakeupTime(Date wakeupTime) {
		this.wakeupTime = wakeupTime;
	}
	
	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

    public int getNaptime() {
        return naptime;
    }

    public void setNaptime(int naptime) {
        this.naptime = naptime;
    }

    public boolean getFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}
	
	public boolean getUploaded() {
		return uploaded;
	}

	public void setUploaded(boolean uploaded) {
		this.uploaded = uploaded;
	}
}
