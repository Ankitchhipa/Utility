package com.cam.scanner.scantopdf.android.barcodereader.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SMS implements Parcelable {
    private String phoneNumber;
    private String message;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.phoneNumber);
        dest.writeString(this.message);
    }

    public SMS() {
    }

    protected SMS(Parcel in) {
        this.phoneNumber = in.readString();
        this.message = in.readString();
    }

    public static final Parcelable.Creator<SMS> CREATOR = new Parcelable.Creator<SMS>() {
        @Override
        public SMS createFromParcel(Parcel source) {
            return new SMS(source);
        }

        @Override
        public SMS[] newArray(int size) {
            return new SMS[size];
        }
    };
}
