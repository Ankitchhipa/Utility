package com.cam.scanner.scantopdf.android.models;

public class ImageFolder {

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private String path;
    private String firstPic;

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    private String folderName;
    private int numberOfPics = 0;

    public void setFirstPic(String firstPic) {
        this.firstPic = firstPic;
    }

    public String getFirstPic() {
        return firstPic;
    }

    public void addpics(){
        this.numberOfPics++;
    }

    public int getNumberOfPics() {
        return numberOfPics;
    }
}
