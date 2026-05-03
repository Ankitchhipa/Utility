package com.cam.scanner.scantopdf.android.barcodereader.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Wifi implements Parcelable {

    private String ssid;
    private String password;
    private int encryptionType;

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(int encryptionType) {
        this.encryptionType = encryptionType;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ssid);
        dest.writeString(this.password);
        dest.writeInt(this.encryptionType);
    }

    public Wifi() {
    }

    protected Wifi(Parcel in) {
        this.ssid = in.readString();
        this.password = in.readString();
        this.encryptionType = in.readInt();
    }

    public static final Parcelable.Creator<Wifi> CREATOR = new Parcelable.Creator<Wifi>() {
        @Override
        public Wifi createFromParcel(Parcel source) {
            return new Wifi(source);
        }

        @Override
        public Wifi[] newArray(int size) {
            return new Wifi[size];
        }
    };
}
