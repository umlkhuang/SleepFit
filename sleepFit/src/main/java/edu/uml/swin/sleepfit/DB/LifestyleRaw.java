package edu.uml.swin.sleepfit.DB;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

public class LifestyleRaw implements Serializable {

	private static final long serialVersionUID = 31415926535897937L;
	
	@DatabaseField(generatedId = true, columnName = "id")
	private int id; 

	@DatabaseField(columnName = "trackDate", index = true, canBeNull = false)
	private String trackDate;
	
	@DatabaseField(columnName = "createTime", index = true, canBeNull = false)
	private Date createTime;

	@DatabaseField(columnName = "type", canBeNull = false)
	private String type;
	
	@DatabaseField(columnName = "typeId", canBeNull = false)
	private int typeId;
	
	@DatabaseField(columnName = "logTime", canBeNull = false)
	private Date logTime;
	
	@DatabaseField(columnName = "selection", canBeNull = false, defaultValue = "0")
	private int selection;
	
	@DatabaseField(columnName = "note", defaultValue = "")
	private String note;

	@DatabaseField(columnName = "uploaded", useGetSet = true, defaultValue = "false") 
	private boolean uploaded;
	
	LifestyleRaw() {
		// needed by ormlite
	}
	
	public LifestyleRaw(long millis, long whenMillis, String type, int typeId, int selection, String note) {
		this.createTime = new Date(millis);
		this.logTime = new Date(whenMillis);
		this.trackDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(this.logTime);
		this.type = type;
		this.typeId = typeId;
		this.selection = selection;
		this.note = note;
		this.uploaded = false;
	}
	
	@Override
	public String toString() {
		StringBuilder strBuf = new StringBuilder();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
		strBuf.append("CreateTime = ").append(dateFormatter.format(this.createTime));
		strBuf.append(", ").append("When = ").append(dateFormatter.format(this.logTime));
		strBuf.append(", ").append("Type = ").append(this.type);
		strBuf.append(", ").append("Selection = ").append(this.selection);
		strBuf.append(", ").append("Note = ").append(this.note);
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public Date getLogTime() {
		return logTime;
	}

	public void setLogTime(Date logTime) {
		this.logTime = logTime;
	}

	public int getSelection() {
		return selection;
	}

	public void setSelection(int selection) {
		this.selection = selection;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public boolean getUploaded() {
		return uploaded;
	}

	public void setUploaded(boolean uploaded) {
		this.uploaded = uploaded;
	}
	
}
