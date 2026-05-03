package com.cam.scanner.scantopdf.android.models;

/*import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;*/

import android.graphics.drawable.Drawable;

import com.cam.scanner.scantopdf.android.util.BaseColor;

public class WaterMark {

    private BaseColor textColor;

    public String getWaterMarkText() {
        return waterMarkText;
    }

    public void setWaterMarkText(String waterMarkText) {
        this.waterMarkText = waterMarkText;
    }

    public int getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(int rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public BaseColor getBaseColor() {
        return baseColor;
    }

    public void setBaseColor(BaseColor baseColor) {
        this.baseColor = baseColor;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(int mFontFamily) {
        this.fontFamily = mFontFamily;
    }

    public int getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(int mFontStyle) {
        this.fontStyle = mFontStyle;
    }

    private String waterMarkText;
    private int rotationAngle;
    private BaseColor baseColor;
    private int textSize;
    private int fontFamily;
    private int fontStyle;
    private byte[] logoByte;

    public BaseColor getTextColor() {
        return textColor;
    }

    public void setTextColor(BaseColor textColor) {
        this.textColor = textColor;
    }

    public byte[] getImage() {
        return logoByte;
    }

    public void setImage(byte[] logoByte) {
        this.logoByte = logoByte;
    }
}
