package com.cam.scanner.scantopdf.android.models;

public class PdfModel extends FileModel {

    public boolean isPasswordProtected() {
        return isPasswordProtected;
    }

    public void setPasswordProtected(boolean passwordProtected) {
        isPasswordProtected = passwordProtected;
    }

    private boolean isPasswordProtected;
}
