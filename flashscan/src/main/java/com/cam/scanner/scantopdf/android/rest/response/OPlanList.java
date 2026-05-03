package com.cam.scanner.scantopdf.android.rest.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OPlanList {
    @SerializedName("Credits")
    @Expose
    private Integer credits;
    @SerializedName("PlanId")
    @Expose
    private Integer planId;
    @SerializedName("PlanName")
    @Expose
    private String planName;
    @SerializedName("Tenure")
    @Expose
    private Integer tenure;

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public Integer getTenure() {
        return tenure;
    }

    public void setTenure(Integer tenure) {
        this.tenure = tenure;
    }
}
