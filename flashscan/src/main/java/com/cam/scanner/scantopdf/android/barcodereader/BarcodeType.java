package com.cam.scanner.scantopdf.android.barcodereader;

import android.util.SparseArray;

public enum BarcodeType {

    CalendarEvent(11),
    ContactInfo(1),
    Email(2),
    Geo(10),
    Phone(4),
    Product(5),
    Sms(6),
    Text(7),
    Url(8),
    Wifi(9),
    Default(0);

    public int getValue() {
        return value;
    }

    private int value;
    private static SparseArray<BarcodeType> mappings;

    BarcodeType(int value) {
        this.value = value;
        getMappings().put(value, this);
    }

    private static SparseArray<BarcodeType> getMappings() {
        if (mappings == null) {
            synchronized (BarcodeType.class) {
                if (mappings == null) {
                    mappings = new SparseArray<>();
                }
            }
        }
        return mappings;
    }

    public static BarcodeType forValue(int value) {
        return getMappings().get(value);
    }

    public static boolean isBarcodeTypeExist(int barcodeType) {
        boolean isNotificationTypeExist = false;
        BarcodeType type = getMappings().get(barcodeType);
        if (type != null) {
            if (type.getValue() == barcodeType) {
                isNotificationTypeExist = true;
            }
        }
        return isNotificationTypeExist;
    }
}
