package com.cam.scanner.scantopdf.android.rest.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestUpdateEmail {
    @SerializedName("DeviceId")
    @Expose
    private String deviceId;
    @SerializedName("Email")
    @Expose
    private String email;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
