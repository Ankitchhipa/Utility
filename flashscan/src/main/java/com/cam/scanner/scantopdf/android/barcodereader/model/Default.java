package com.cam.scanner.scantopdf.android.barcodereader.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Default implements Parcelable {
    private String defaultText;

    public String getDefaultText() {
        return defaultText;
    }

    public void setDefaultText(String defaultText) {
        this.defaultText = defaultText;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.defaultText);
    }

    public Default() {
    }

    protected Default(Parcel in) {
        this.defaultText = in.readString();
    }

    public static final Parcelable.Creator<Default> CREATOR = new Parcelable.Creator<Default>() {
        @Override
        public Default createFromParcel(Parcel source) {
            return new Default(source);
        }

        @Override
        public Default[] newArray(int size) {
            return new Default[size];
        }
    };
}
