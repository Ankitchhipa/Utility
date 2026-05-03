package com.cam.scanner.scantopdf.android.interfaces;

import java.util.ArrayList;
import java.util.List;

public interface CreateMultipleTempBitmapListener {

    void onCompressBitmapStart();

    void onCompressBitmapComplete(ArrayList<String> foldersList);
}
