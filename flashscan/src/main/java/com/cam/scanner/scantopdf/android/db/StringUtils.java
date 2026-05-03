package com.cam.scanner.scantopdf.android.db;

import java.util.ArrayList;

public class StringUtils {

    public static String strSeparator = "__,__";
    
    public static String convertArrayToString(ArrayList<String> array){
        StringBuilder str = new StringBuilder();
        for (int i = 0;i<array.size(); i++) {
            str.append(array.get(i));
            if(i<array.size()-1){
                str.append(strSeparator);
            }
        }
        return str.toString();
    }

    public static String[] convertStringToArray(String str){
        String[] arr = str.split(strSeparator);
        return arr;
    }

}
