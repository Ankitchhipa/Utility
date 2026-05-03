package com.cam.scanner.scantopdf.android.models;

public class FeatureModel {
    String feature;
    String basic_val;
    String premium_val;
    int basic_icon;
    int premium_icon;

    public FeatureModel() {
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getBasic_val() {
        return basic_val;
    }

    public void setBasic_val(String basic_val) {
        this.basic_val = basic_val;
    }

    public String getPremium_val() {
        return premium_val;
    }

    public void setPremium_val(String premium_val) {
        this.premium_val = premium_val;
    }

    public int getBasic_icon() {
        return basic_icon;
    }

    public void setBasic_icon(int basic_icon) {
        this.basic_icon = basic_icon;
    }

    public int getPremium_icon() {
        return premium_icon;
    }

    public void setPremium_icon(int premium_icon) {
        this.premium_icon = premium_icon;
    }
}
