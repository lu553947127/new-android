package com.ktw.fly.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

/**
 * 获取系统设备信息的工具类
 *
 * @author dty
 */
public class DeviceInfoUtil {
    private static final String TAG = "DeviceInfoUtil";

    /* 获取手机唯一序列号 */
    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        String deviceId = PreferenceUtils.getString(context, Constants.KEY_DEVICE_ID);
        if (!TextUtils.isEmpty(deviceId)) {
            Log.d(TAG, "getDeviceId() returned: " + deviceId);
            return deviceId;
        }
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                deviceId = tm.getDeviceId();// 手机设备ID，这个ID会被用为用户访问统计
            }
        } catch (Exception ignored) {
            // deviceId越来越严格了，以防万一，
        }
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = UUID.randomUUID().toString().replaceAll("-", "");
        }
        PreferenceUtils.putString(context, Constants.KEY_DEVICE_ID, deviceId);
        Log.d(TAG, "getDeviceId() put and returned: " + deviceId);
        return deviceId;
    }

    /* 获取操作系统版本号 */
    public static String getOsVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /* 获取手机型号 */
    public static String getModel() {
        return android.os.Build.MODEL;
    }

    /* 获取手机厂商 */
    public static String getManufacturers() {
        return android.os.Build.MANUFACTURER;
    }

    /* 获取app的版本信息 */
    public static int getVersionCode(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;// 系统版本号
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;// 系统版本名
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /*
    判断手机Rom
     */
    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        return line;
    }

    /**
     * 判断 miui emui meizu oppo vivo Rom
     *
     * @return
     */
    public static boolean isMiuiRom() {
        String property = getSystemProperty("ro.miui.ui.version.name");
        return !TextUtils.isEmpty(property);
    }

    public static boolean isEmuiRom() {
        String property = getSystemProperty("ro.build.version.emui");
        return !TextUtils.isEmpty(property);
    }

    public static boolean isMeizuRom() {
        String property = getSystemProperty("ro.build.display.id");
        return property != null && property.toLowerCase().contains("flyme");
    }

    public static boolean isOppoRom() {
        String property = getSystemProperty("ro.build.version.opporom");
        return !TextUtils.isEmpty(property);
    }

    public static boolean isVivoRom() {
        String property = getSystemProperty("ro.vivo.os.version");
        return !TextUtils.isEmpty(property);
    }

    public static boolean is360Rom() {
        return Build.MANUFACTURER != null
                && (Build.MANUFACTURER.toLowerCase().contains("qiku")
                || Build.MANUFACTURER.contains("360"));
    }
}
