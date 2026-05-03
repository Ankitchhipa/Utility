package com.cam.scanner.scantopdf.android.barcodereader.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Phone implements Parcelable {

    private String number;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.number);
    }

    public Phone() {
    }

    protected Phone(Parcel in) {
        this.number = in.readString();
    }

    public static final Parcelable.Creator<Phone> CREATOR = new Parcelable.Creator<Phone>() {
        @Override
        public Phone createFromParcel(Parcel source) {
            return new Phone(source);
        }

        @Override
        public Phone[] newArray(int size) {
            return new Phone[size];
        }
    };
}
