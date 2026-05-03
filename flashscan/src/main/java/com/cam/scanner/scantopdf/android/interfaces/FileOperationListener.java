package com.cam.scanner.scantopdf.android.interfaces;

public interface FileOperationListener {
    void actionAddToDrive(Object o,int position);

    void actionShare(Object o);

    void actionRename(Object o);

    void actionDelete(Object o);

    void actionSaveAsPdf(Object o);

    void makeFavourite(Object o);

    void removeFavourite(Object o);
}
