package com.cam.scanner.scantopdf.android.rest.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Register {
    @SerializedName("Credits")
    @Expose
    private Integer credits;
    @SerializedName("Email")
    @Expose
    private String email;
    @SerializedName("IsOCR")
    @Expose
    private Boolean isOCR;
    @SerializedName("PlanId")
    @Expose
    private Integer planId;
    @SerializedName("RespDetail")
    @Expose
    private String respDetail;
    @SerializedName("RespEnum")
    @Expose
    private Integer respEnum;
    @SerializedName("SubscriptionId")
    @Expose
    private String subscriptionId;

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIsOCR() {
        return isOCR;
    }

    public void setIsOCR(Boolean isOCR) {
        this.isOCR = isOCR;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public String getRespDetail() {
        return respDetail;
    }

    public void setRespDetail(String respDetail) {
        this.respDetail = respDetail;
    }

    public Integer getRespEnum() {
        return respEnum;
    }

    public void setRespEnum(Integer respEnum) {
        this.respEnum = respEnum;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

}
