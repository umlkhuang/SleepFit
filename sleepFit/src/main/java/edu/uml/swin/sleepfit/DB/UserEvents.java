package edu.uml.swin.sleepfit.DB;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserEvents implements Serializable {

    private static final long serialVersionUID = 31415926535897939L;

    @DatabaseField(generatedId = true, columnName = "id")
    private int id;

    @DatabaseField(columnName = "trackDate", index = true, canBeNull = false)
    private String trackDate;

    @DatabaseField(columnName = "createTime", canBeNull = false)
    private Date createTime;

    @DatabaseField(columnName = "dataStyle")
    private String dataStyle;

    @DatabaseField(columnName = "data")
    private String data;

    @DatabaseField(columnName = "uploaded", useGetSet = true, defaultValue = "false")
    private boolean uploaded;

    UserEvents() {
        // needed by ormlite
    }

    public UserEvents(long millis, String dataStyle, String data) {
        this.createTime = new Date(millis);
        this.trackDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(this.createTime);
        this.dataStyle = dataStyle;
        this.data = data;
        this.uploaded = false;
    }

    @Override
    public String toString() {
        StringBuilder strBuf = new StringBuilder();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
        strBuf.append("CreateTime = ").append(dateFormatter.format(this.createTime));
        strBuf.append(", ").append("TrackDate = ").append(this.trackDate);
        strBuf.append(", ").append("DataStyle = ").append(this.dataStyle);
        strBuf.append(", ").append("Data = ").append(this.data);
        return strBuf.toString();
    }

    public int getId() {
        return id;
    }

    public String getTrackDate() {
        return trackDate;
    }

    public void setTrackDate(String trackDate) {
        this.trackDate = trackDate;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getDataStyle() {
        return dataStyle;
    }

    public void setDataStyle(String dataStyle) {
        this.dataStyle = dataStyle;
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
