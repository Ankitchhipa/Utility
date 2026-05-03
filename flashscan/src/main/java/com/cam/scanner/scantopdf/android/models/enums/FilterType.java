package com.cam.scanner.scantopdf.android.models.enums;

public enum FilterType {
    Original(1),
    Magic(2),
    BW1(3),
    BW2(4),
    GRAY(5);

    private int intValue;

    private FilterType(int i) {
        intValue = i;
        getMappings().put(i, this);
    }

    public int getValue() {
        return intValue;
    }

    private static java.util.HashMap<Integer, FilterType> mappings;

    private static java.util.HashMap<Integer, FilterType> getMappings() {
        if (mappings == null) {
            synchronized (FilterType.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<>();
                }
            }
        }
        return mappings;
    }

    public static FilterType forValue(int value) {
        return getMappings().get(value);
    }
}
