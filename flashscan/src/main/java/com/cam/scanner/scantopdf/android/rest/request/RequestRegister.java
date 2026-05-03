package com.cam.scanner.scantopdf.android.rest.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestRegister {
    @SerializedName("DeviceId")
    @Expose
    private String deviceId;
    @SerializedName("PlanId")
    @Expose
    private Integer planId;
    @SerializedName("OrderId")
    @Expose
    private String orderId;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

}
