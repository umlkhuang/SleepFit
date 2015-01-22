package edu.uml.swin.sleepfit.DB;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "sysevents")
public class SysEvents implements Serializable {

	private static final long serialVersionUID = 31415926535897932L;
	
	@DatabaseField(generatedId = true, columnName = "id")
	private int id;
	
	@DatabaseField(columnName = "createTime", canBeNull = false)
	private Date createTime; 
	
	@DatabaseField(columnName = "trackDate", index = true)
	private String trackDate;
	
	@DatabaseField(columnName = "eventType", useGetSet = true)
	private int eventType; 

	@DatabaseField(columnName = "uploaded", useGetSet = true, defaultValue = "false") 
	private boolean uploaded;
	
	SysEvents() {
		// Needed by ormLite
	}
	
	public SysEvents(long millis, int eventType) {
		this.createTime = new Date(millis);
		this.trackDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(this.createTime);
		this.eventType = eventType;
		this.uploaded = false;
	}
	
	@Override 
	public String toString() {
		StringBuilder strBuf = new StringBuilder();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
		strBuf.append("CreateTime = ").append(dateFormatter.format(this.createTime));
		strBuf.append(", ").append("TrackDate = ").append(trackDate);
		strBuf.append(", ").append("EventType = ").append(eventType);
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
	
	public int getEventType() {
		return eventType;
	}

	public void setEventType(int eventType) {
		this.eventType = eventType;
	}

	public boolean getUploaded() {
		return uploaded;
	}

	public void setUploaded(boolean uploaded) {
		this.uploaded = uploaded;
	}
	
}
