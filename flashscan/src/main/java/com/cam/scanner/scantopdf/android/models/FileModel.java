package com.cam.scanner.scantopdf.android.models;

import com.cam.scanner.scantopdf.android.models.enums.DocumentTypeEnum;

public class FileModel {

    private int id;
    private String name;
    private String title;
    private long dateAdded;

    public boolean isPdfPasswordProtected() {
        return isPdfPasswordProtected;
    }

    public void setPdfPasswordProtected(boolean pdfPasswordProtected) {
        isPdfPasswordProtected = pdfPasswordProtected;
    }

    private boolean isPdfPasswordProtected;

    public boolean isCompressedPdf() {
        return isCompressedPdf;
    }

    public void setCompressedPdf(boolean compressedPdf) {
        isCompressedPdf = compressedPdf;
    }

    private boolean isCompressedPdf;

    public String getPdfFileName() {
        return pdfFileName;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName = pdfFileName;
    }

    private String pdfFileName;

    public boolean isAdView() {
        return isAdView;
    }

    public void setAdView(boolean adView) {
        isAdView = adView;
    }

    private boolean isAdView;

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    private long createdTime;
    private long size;
    private String folder;
    private int fileCountInFolder;

    public int getFileNumber() {
        return fileNumber;
    }

    public void setFileNumber(int fileNumber) {
        this.fileNumber = fileNumber;
    }

    private int fileNumber;

    private boolean isSavedOnGoogleDrive = false;
    private String googleDriveFolderId;

    public FileModel(String fileName, String thumbnailPath) {
        this.name = fileName;
        this.thumbnailPath = thumbnailPath;
    }

    public FileModel(String fileName, String path, String fileExtension) {
        this.name = fileName;
        this.path = path;
        this.fileExtension = fileExtension;
    }

    public FileModel() {

    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    private boolean starred;

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    private String fileExtension;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    private boolean checked;

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    private String thumbnailPath;

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }


    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }


    private String dataPath;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private String path;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDateTaken() {
        return dateAdded;
    }

    public void setDateTaken(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getFileCountInFolder() {
        return fileCountInFolder;
    }

    public void setFileCountInFolder(int fileCountInFolder) {
        this.fileCountInFolder = fileCountInFolder;
    }

    public boolean isSavedOnGoogleDrive() {
        return isSavedOnGoogleDrive;
    }

    public void setSavedOnGoogleDrive(boolean savedOnGoogleDrive) {
        isSavedOnGoogleDrive = savedOnGoogleDrive;
    }

    public String getGoogleDriveFolderId() {
        return googleDriveFolderId;
    }

    public void setGoogleDriveFolderId(String googleDriveFolderId) {
        this.googleDriveFolderId = googleDriveFolderId;
    }

    private int type = DocumentTypeEnum.Document.getValue();

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
