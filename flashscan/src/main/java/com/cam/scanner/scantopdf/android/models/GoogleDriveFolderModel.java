package com.cam.scanner.scantopdf.android.models;

import java.util.List;

public class GoogleDriveFolderModel {
    String id;
    String folderName;
    List<GoogleDriveChildFileModel> googleDriveChildFileModelList;


    public GoogleDriveFolderModel(String id, String folderName, List<GoogleDriveChildFileModel> googleDriveChildFileModelList) {
        this.id = id;
        this.folderName = folderName;
        this.googleDriveChildFileModelList = googleDriveChildFileModelList;
    }

    public GoogleDriveFolderModel() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public List<GoogleDriveChildFileModel> getGoogleDriveChildFileModelList() {
        return googleDriveChildFileModelList;
    }

    public void setGoogleDriveChildFileModelList(List<GoogleDriveChildFileModel> googleDriveChildFileModelList) {
        this.googleDriveChildFileModelList = googleDriveChildFileModelList;
    }
}
