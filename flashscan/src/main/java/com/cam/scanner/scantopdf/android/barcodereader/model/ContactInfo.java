package com.cam.scanner.scantopdf.android.barcodereader.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ContactInfo implements Parcelable {

    private String name;
    private String phoneNumber;
    private String organization;
    private String email;

    public ArrayList<String> getEmails() {
        return emails;
    }

    public void setEmails(ArrayList<String> emails) {
        this.emails = emails;
    }

    private ArrayList<String> emails;

    public String[] getUrls() {
        return urls;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    private String[] urls;

    public ArrayList<MultiplePhones> getMultiplePhones() {
        return multiplePhones;
    }

    public void setMultiplePhones(ArrayList<MultiplePhones> multiplePhones) {
        this.multiplePhones = multiplePhones;
    }

    private ArrayList<MultiplePhones> multiplePhones;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    private String address;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private String title;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ContactInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.phoneNumber);
        dest.writeString(this.organization);
        dest.writeString(this.email);
        dest.writeStringList(this.emails);
        dest.writeStringArray(this.urls);
        dest.writeTypedList(this.multiplePhones);
        dest.writeString(this.address);
        dest.writeString(this.title);
    }

    protected ContactInfo(Parcel in) {
        this.name = in.readString();
        this.phoneNumber = in.readString();
        this.organization = in.readString();
        this.email = in.readString();
        this.emails = in.createStringArrayList();
        this.urls = in.createStringArray();
        this.multiplePhones = in.createTypedArrayList(MultiplePhones.CREATOR);
        this.address = in.readString();
        this.title = in.readString();
    }

    public static final Creator<ContactInfo> CREATOR = new Creator<ContactInfo>() {
        @Override
        public ContactInfo createFromParcel(Parcel source) {
            return new ContactInfo(source);
        }

        @Override
        public ContactInfo[] newArray(int size) {
            return new ContactInfo[size];
        }
    };
}
