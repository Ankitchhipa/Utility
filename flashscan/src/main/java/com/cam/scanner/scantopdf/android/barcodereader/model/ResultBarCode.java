package com.cam.scanner.scantopdf.android.barcodereader.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.cam.scanner.scantopdf.android.barcodereader.BarcodeType;

public class ResultBarCode implements Parcelable {

    private String rawValue;
    private int valueFormat;
    private CalendarEvent calendarEvent;
    private ContactInfo contactInfo;
    private Email email;
    private Geo geo;
    private Phone phone;
    private Product product;
    private SMS sms;
    private Text text;
    private URL url;
    private Wifi wifi;
    private Default aDefault;

    public Default getaDefault() {
        return aDefault;
    }

    public void setaDefault(Default aDefault) {
        this.aDefault = aDefault;
    }


    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    public int getValueFormat() {
        return valueFormat;
    }

    public void setValueFormat(int valueFormat) {
        this.valueFormat = valueFormat;
    }

    public CalendarEvent getCalendarEvent() {
        return calendarEvent;
    }

    public void setCalendarEvent(CalendarEvent calendarEvent) {
        this.calendarEvent = calendarEvent;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Geo getGeo() {
        return geo;
    }

    public void setGeo(Geo geo) {
        this.geo = geo;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public SMS getSms() {
        return sms;
    }

    public void setSms(SMS sms) {
        this.sms = sms;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Wifi getWifi() {
        return wifi;
    }

    public void setWifi(Wifi wifi) {
        this.wifi = wifi;
    }

    public ResultBarCode() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.rawValue);
        dest.writeInt(this.valueFormat);
        dest.writeParcelable(this.calendarEvent, flags);
        dest.writeParcelable(this.contactInfo, flags);
        dest.writeParcelable(this.email, flags);
        dest.writeParcelable(this.geo, flags);
        dest.writeParcelable(this.phone, flags);
        dest.writeParcelable(this.product, flags);
        dest.writeParcelable(this.sms, flags);
        dest.writeParcelable(this.text, flags);
        dest.writeParcelable(this.url, flags);
        dest.writeParcelable(this.wifi, flags);
        dest.writeParcelable(this.aDefault, flags);
    }

    protected ResultBarCode(Parcel in) {
        this.rawValue = in.readString();
        this.valueFormat = in.readInt();
        this.calendarEvent = in.readParcelable(CalendarEvent.class.getClassLoader());
        this.contactInfo = in.readParcelable(ContactInfo.class.getClassLoader());
        this.email = in.readParcelable(Email.class.getClassLoader());
        this.geo = in.readParcelable(Geo.class.getClassLoader());
        this.phone = in.readParcelable(Phone.class.getClassLoader());
        this.product = in.readParcelable(Product.class.getClassLoader());
        this.sms = in.readParcelable(SMS.class.getClassLoader());
        this.text = in.readParcelable(Text.class.getClassLoader());
        this.url = in.readParcelable(URL.class.getClassLoader());
        this.wifi = in.readParcelable(Wifi.class.getClassLoader());
        this.aDefault = in.readParcelable(Default.class.getClassLoader());
    }

    public static final Creator<ResultBarCode> CREATOR = new Creator<ResultBarCode>() {
        @Override
        public ResultBarCode createFromParcel(Parcel source) {
            return new ResultBarCode(source);
        }

        @Override
        public ResultBarCode[] newArray(int size) {
            return new ResultBarCode[size];
        }
    };
}
