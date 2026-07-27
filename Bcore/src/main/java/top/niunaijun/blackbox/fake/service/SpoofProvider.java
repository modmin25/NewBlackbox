package top.niunaijun.blackbox.fake.service;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.utils.Slog;

import java.security.MessageDigest;

public class SpoofProvider {
    private static final String TAG = "SpoofProvider";
    private static final String PREFS_NAME = "spoof_manager_prefs";
    private static final String KEY_SPOOF_DATA = "spoof_data";

    public static String getSpoofedImei(String pkgName) {
        String val = getField(pkgName, "imei");
        if (val != null && !val.isEmpty()) return val;
        return generateDeterministicImei(pkgName);
    }

    public static String getSpoofedAndroidId(String pkgName) {
        String val = getField(pkgName, "androidId");
        if (val != null && !val.isEmpty()) return val;
        return generateDeterministicAndroidId(pkgName);
    }

    public static String getSpoofedMac(String pkgName) {
        String val = getField(pkgName, "macAddress");
        if (val != null && !val.isEmpty()) return val;
        return "ac:62:5a:82:65:c4";
    }

    public static String getSpoofedSerial(String pkgName, Object defaultValue) {
        String val = getField(pkgName, "serialNumber");
        if (val != null && !val.isEmpty()) return val;
        return defaultValue != null ? defaultValue.toString() : generateDeterministicId(pkgName);
    }

    public static String getSpoofedSimSerial(String pkgName) {
        String val = getField(pkgName, "simSerial");
        if (val != null && !val.isEmpty()) return val;
        return generateDeterministicId(pkgName + "_sim");
    }

    public static String getSpoofedPhoneNumber(String pkgName) {
        String val = getField(pkgName, "phoneNumber");
        return (val != null && !val.isEmpty()) ? val : null;
    }

    public static String getSpoofedModel(String pkgName, Object defaultValue) {
        String val = getField(pkgName, "model");
        if (val != null && !val.isEmpty()) return val;
        return defaultValue != null ? defaultValue.toString() : android.os.Build.MODEL;
    }

    public static String getSpoofedManufacturer(String pkgName, Object defaultValue) {
        String val = getField(pkgName, "manufacturer");
        if (val != null && !val.isEmpty()) return val;
        return defaultValue != null ? defaultValue.toString() : android.os.Build.MANUFACTURER;
    }

    public static String getSpoofedBrand(String pkgName, Object defaultValue) {
        String val = getField(pkgName, "brand");
        if (val != null && !val.isEmpty()) return val;
        return defaultValue != null ? defaultValue.toString() : android.os.Build.BRAND;
    }

    public static String getSpoofedDevice(String pkgName, Object defaultValue) {
        String val = getField(pkgName, "device");
        if (val != null && !val.isEmpty()) return val;
        return defaultValue != null ? defaultValue.toString() : android.os.Build.DEVICE;
    }

    public static String getSpoofedProduct(String pkgName, Object defaultValue) {
        String val = getField(pkgName, "product");
        if (val != null && !val.isEmpty()) return val;
        return defaultValue != null ? defaultValue.toString() : android.os.Build.PRODUCT;
    }

    public static String getSpoofedFingerprint(String pkgName, Object defaultValue) {
        String val = getField(pkgName, "fingerprint");
        if (val != null && !val.isEmpty()) return val;
        return defaultValue != null ? defaultValue.toString() : android.os.Build.FINGERPRINT;
    }

    public static String getSpoofedHardware(String pkgName, Object defaultValue) {
        String val = getField(pkgName, "hardware");
        if (val != null && !val.isEmpty()) return val;
        return defaultValue != null ? defaultValue.toString() : android.os.Build.HARDWARE;
    }

    public static String getSpoofedBoard(String pkgName, Object defaultValue) {
        String val = getField(pkgName, "board");
        if (val != null && !val.isEmpty()) return val;
        return defaultValue != null ? defaultValue.toString() : android.os.Build.BOARD;
    }

    public static String getSpoofedBootloader(String pkgName, Object defaultValue) {
        String val = getField(pkgName, "bootloader");
        if (val != null && !val.isEmpty()) return val;
        return defaultValue != null ? defaultValue.toString() : android.os.Build.BOOTLOADER;
    }

    public static String getSpoofedDisplay(String pkgName, Object defaultValue) {
        String val = getField(pkgName, "display");
        if (val != null && !val.isEmpty()) return val;
        return defaultValue != null ? defaultValue.toString() : android.os.Build.DISPLAY;
    }

    public static String getSpoofedHost(String pkgName, Object defaultValue) {
        String val = getField(pkgName, "host");
        if (val != null && !val.isEmpty()) return val;
        return defaultValue != null ? defaultValue.toString() : android.os.Build.HOST;
    }

    public static String getSpoofedType(String pkgName, Object defaultValue) {
        String val = getField(pkgName, "type");
        if (val != null && !val.isEmpty()) return val;
        return defaultValue != null ? defaultValue.toString() : android.os.Build.TYPE;
    }

    public static String getSpoofedTags(String pkgName, Object defaultValue) {
        String val = getField(pkgName, "tags");
        if (val != null && !val.isEmpty()) return val;
        return defaultValue != null ? defaultValue.toString() : android.os.Build.TAGS;
    }

    public static String getSpoofedUser(String pkgName, Object defaultValue) {
        String val = getField(pkgName, "user");
        if (val != null && !val.isEmpty()) return val;
        return defaultValue != null ? defaultValue.toString() : android.os.Build.USER;
    }

    private static String getField(String pkgName, String fieldName) {
        if (pkgName == null || pkgName.isEmpty()) return null;
        try {
            Context context = BlackBoxCore.getContext();
            if (context == null) return null;
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String rawData = prefs.getString(KEY_SPOOF_DATA, null);
            if (rawData == null) return null;

            JSONObject json = new JSONObject(rawData);
            if (!json.has(pkgName)) return null;
            JSONObject pkgObj = json.getJSONObject(pkgName);
            boolean enabled = pkgObj.optBoolean("enabled", false);
            if (!enabled) return null;

            String val = pkgObj.optString(fieldName, null);
            return (val != null && !val.isEmpty()) ? val : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String generateDeterministicImei(String pkgName) {
        long hash = pkgName.hashCode() & 0x7FFFFFFFFFFFFFFFL;
        String imei = String.valueOf(350000000000000L + (hash % 1000000000000L));
        while (imei.length() < 14) {
            imei = "0" + imei;
        }
        int sum = 0;
        for (int i = 0; i < imei.length(); i++) {
            int digit = imei.charAt(i) - '0';
            if (i % 2 == 1) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return imei + checkDigit;
    }

    private static String generateDeterministicAndroidId(String pkgName) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(pkgName.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().substring(0, 16).toUpperCase();
        } catch (Exception e) {
            return "0000000000000000";
        }
    }

    private static String generateDeterministicId(String pkgName) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(pkgName.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().substring(0, 16).toUpperCase();
        } catch (Exception e) {
            return "0000000000000000";
        }
    }
}
