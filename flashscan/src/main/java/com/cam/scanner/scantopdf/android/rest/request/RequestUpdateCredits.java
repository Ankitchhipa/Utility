package com.cam.scanner.scantopdf.android.rest.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestUpdateCredits {
    @SerializedName("SubscriptionId")
    @Expose
    private String subscriptionId;
    @SerializedName("Credits")
    @Expose
    private Integer credits;
    @SerializedName("DeviceId")
    @Expose
    private String deviceId;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
