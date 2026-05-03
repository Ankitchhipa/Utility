package com.cam.scanner.scantopdf.android.barcodereader.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MultiplePhones implements Parcelable {

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private String number;
    private int type;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.number);
        dest.writeInt(this.type);
    }

    public MultiplePhones() {
    }

    protected MultiplePhones(Parcel in) {
        this.number = in.readString();
        this.type = in.readInt();
    }

    public static final Parcelable.Creator<MultiplePhones> CREATOR = new Parcelable.Creator<MultiplePhones>() {
        @Override
        public MultiplePhones createFromParcel(Parcel source) {
            return new MultiplePhones(source);
        }

        @Override
        public MultiplePhones[] newArray(int size) {
            return new MultiplePhones[size];
        }
    };
}
