package com.cam.scanner.scantopdf.android.barcodereader.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CalendarEvent implements Parcelable {

    private String description;
    private String location;
    private String organizer;
    private String status;
    private String summary;
    private int startDay;
    private int startMonth;
    private int startYear;
    private int endDay;
    private int endMonth;
    private int endYear;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public int getStartDay() {
        return startDay;
    }

    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }

    public int getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(int startMonth) {
        this.startMonth = startMonth;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getEndDay() {
        return endDay;
    }

    public void setEndDay(int endDay) {
        this.endDay = endDay;
    }

    public int getEndMonth() {
        return endMonth;
    }

    public void setEndMonth(int endMonth) {
        this.endMonth = endMonth;
    }

    public int getEndYear() {
        return endYear;
    }

    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.description);
        dest.writeString(this.location);
        dest.writeString(this.organizer);
        dest.writeString(this.status);
        dest.writeString(this.summary);
        dest.writeInt(this.startDay);
        dest.writeInt(this.startMonth);
        dest.writeInt(this.startYear);
        dest.writeInt(this.endDay);
        dest.writeInt(this.endMonth);
        dest.writeInt(this.endYear);
    }

    public CalendarEvent() {
    }

    protected CalendarEvent(Parcel in) {
        this.description = in.readString();
        this.location = in.readString();
        this.organizer = in.readString();
        this.status = in.readString();
        this.summary = in.readString();
        this.startDay = in.readInt();
        this.startMonth = in.readInt();
        this.startYear = in.readInt();
        this.endDay = in.readInt();
        this.endMonth = in.readInt();
        this.endYear = in.readInt();
    }

    public static final Parcelable.Creator<CalendarEvent> CREATOR = new Parcelable.Creator<CalendarEvent>() {
        @Override
        public CalendarEvent createFromParcel(Parcel source) {
            return new CalendarEvent(source);
        }

        @Override
        public CalendarEvent[] newArray(int size) {
            return new CalendarEvent[size];
        }
    };
}
