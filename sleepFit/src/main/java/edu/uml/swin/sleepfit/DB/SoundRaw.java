package edu.uml.swin.sleepfit.DB;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "soundraw")
public class SoundRaw implements Serializable {

private static final long serialVersionUID = 31415926535897935L;
	
	@DatabaseField(generatedId = true, columnName = "id")
	private int id; 
	
	@DatabaseField(columnName = "createTime", index = true, canBeNull = false)
	private Date createTime;
	
	@DatabaseField(columnName = "data", useGetSet = true)
	private String data;
	
	@DatabaseField(columnName = "uploaded", useGetSet = true, defaultValue = "false") 
	private boolean uploaded;
	
	SoundRaw() {
		// needed by ormlite
	}
	
	public SoundRaw(long mills) {
		this.createTime = new Date(mills);
		this.data = "";
		this.uploaded = false;
	}
	
	public SoundRaw(long mills, ArrayList<Integer> rawData) {
		this.createTime = new Date(mills);
		StringBuilder strBuf = new StringBuilder();
		int rawLen = rawData.size();
		for (int i = 0; i < rawLen; i++) {
			strBuf.append(rawData.get(i).intValue());
			if (i < rawLen - 1) strBuf.append("|");
		}
		this.data = strBuf.toString();
		this.uploaded = false;
	}
	
	@Override
	public String toString() {
		StringBuilder strBuf = new StringBuilder();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
		strBuf.append("CreateTime = ").append(dateFormatter.format(createTime));
		strBuf.append(", ").append("SoundData = ").append(data);
		return strBuf.toString();
	}
	
	public int getId() {
		return id;
	}
	
	public Date getCreateTime() {
		return createTime;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public boolean getUploaded() {
		return uploaded;
	}

	public void setUploaded(boolean uploaded) {
		this.uploaded = uploaded;
	}
	
	
}
