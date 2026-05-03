package com.cam.scanner.scantopdf.android.barcodereader.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Text implements Parcelable {

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.text);
    }

    public Text() {
    }

    protected Text(Parcel in) {
        this.text = in.readString();
    }

    public static final Parcelable.Creator<Text> CREATOR = new Parcelable.Creator<Text>() {
        @Override
        public Text createFromParcel(Parcel source) {
            return new Text(source);
        }

        @Override
        public Text[] newArray(int size) {
            return new Text[size];
        }
    };
}
