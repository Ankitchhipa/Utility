package com.cam.scanner.scantopdf.android.rest.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetPlans {
    @SerializedName("RespDetail")
    @Expose
    private String respDetail;
    @SerializedName("RespEnum")
    @Expose
    private Integer respEnum;
    @SerializedName("oPlanList")
    @Expose
    private List<OPlanList> oPlanList = null;

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

    public List<OPlanList> getOPlanList() {
        return oPlanList;
    }

    public void setOPlanList(List<OPlanList> oPlanList) {
        this.oPlanList = oPlanList;
    }
}
