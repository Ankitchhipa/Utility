package com.cam.scanner.scantopdf.android.models;

public class GoogleDriveChildFileModel {

    private String fileId;
    private String fileName;
    /*private DateTime filemodifiedTime;
    private long size;
    private DateTime createdTime;
    private Boolean starred;
    private String mimeType;*/
    private boolean isFileAlreadyExist = false;

    public GoogleDriveChildFileModel(String subChildFileId, String subChildFileName, boolean isFileAlreadyExist) {
        this.fileId = subChildFileId;
        this.fileName = subChildFileName;
        this.isFileAlreadyExist =  isFileAlreadyExist;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isFileAlreadyExist() {
        return isFileAlreadyExist;
    }

    public void setFileAlreadyExist(boolean fileAlreadyExist) {
        isFileAlreadyExist = fileAlreadyExist;
    }
}
