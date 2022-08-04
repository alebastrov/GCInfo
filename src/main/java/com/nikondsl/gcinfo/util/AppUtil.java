package com.nikondsl.gcinfo.util;

public class AppUtil {
    private AppUtil() {
    }
    
    public static int getIntValue(String value) {
        return getIntValue(value, 0);
    }

    public static int getIntValue(String value, int defaultValue) {
        if (isStringEmpty(value)) {
            return defaultValue;
        }
        value = value.trim();
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public static boolean isStringEmpty(CharSequence src) {
        try {
            return src == null || src.toString().trim().length() == 0;
        }
        catch (Exception ex) {
            throw new RuntimeException("Could not check whether [" + src + "] is empty ", ex);
        }
    }
}
