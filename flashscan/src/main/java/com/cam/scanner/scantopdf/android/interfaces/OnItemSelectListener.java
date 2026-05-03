package com.cam.scanner.scantopdf.android.interfaces;

import android.view.View;

public interface OnItemSelectListener {

    void onItemSelect(Object o);

    void onItemLongPress(Object o);

    void onItemAction(Object o, View view);
}
