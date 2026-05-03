package com.cam.scanner.scantopdf.android.barcodereader.model;

import android.os.Parcel;
import android.os.Parcelable;

public class URL implements Parcelable {

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
    }

    public URL() {
    }

    protected URL(Parcel in) {
        this.url = in.readString();
    }

    public static final Parcelable.Creator<URL> CREATOR = new Parcelable.Creator<URL>() {
        @Override
        public URL createFromParcel(Parcel source) {
            return new URL(source);
        }

        @Override
        public URL[] newArray(int size) {
            return new URL[size];
        }
    };
}
