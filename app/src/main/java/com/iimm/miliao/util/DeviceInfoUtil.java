package com.iimm.miliao.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.DeviceUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Environment.DIRECTORY_DOCUMENTS;

/**
 * 获取系统设备信息的工具类
 *
 * @author dty
 */
public class DeviceInfoUtil {

    private static final String TAG = "DeviceInfoUtil";


    public static void save(Context context, String androidID) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            File file = new File(context.getExternalFilesDir(DIRECTORY_DOCUMENTS), "deviceInfo");
            try {
                FileIOUtils.writeFileFromString(file, androidID);
            } catch (Exception e) {
                e.printStackTrace();
                PreferenceUtils.putString(context, "deviceInfo", androidID);
            }
        }else{
            PreferenceUtils.putString(context, "deviceInfo", androidID);
        }
    }

    public static String getDeviceId(Context context) {
        if (Build.VERSION.SDK_INT <= P) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return createDeviceId(context);
            }
            String deviceId = tm.getDeviceId();// 手机设备ID，这个ID会被用为用户访问统计
            if (deviceId == null) {
                deviceId = createDeviceId(context);
            }
            Log.i(TAG,"deviceId:" + deviceId);
            return deviceId;
        } else {
            return createDeviceId(context);
        }
    }

    private static String createDeviceId(Context context) {
        File file = new File(Environment.getRootDirectory().getAbsolutePath()+"/device", "deviceInfo.txt");
        if (file.exists()) {
            try {
                String s = FileIOUtils.readFile2String(file);
                Log.i(TAG,"deviceId:" + s);
                return s;
            } catch (Exception e) {
                e.printStackTrace();
                String deviceInfo = PreferenceUtils.getString(context,"deviceInfo");
                if (TextUtils.isEmpty(deviceInfo)) {
//                    String deviceId = getDeviceId();
                    String deviceId = DeviceUtils.getUniqueDeviceId();
                    save(context, deviceId);
                    Log.i(TAG,"deviceId:" + deviceId);
                    return deviceId;
                } else {
                    Log.i(TAG,"deviceId:" + deviceInfo);
                    return deviceInfo;
                }
            }
        } else {
            String deviceInfo = PreferenceUtils.getString(context,"deviceInfo");
            if (TextUtils.isEmpty(deviceInfo)) {
//                String deviceId = getDeviceId();
                String deviceId = DeviceUtils.getUniqueDeviceId();
                save(context, deviceId);
                Log.i(TAG,"deviceId:" + deviceId);
                return deviceId;
            } else {
                Log.i(TAG,"deviceId:" + deviceInfo);
                return deviceInfo;
            }
        }
    }


    @SuppressLint("MissingPermission")
    private static String getDeviceId() {
        String serial = null;
        String m_szDevIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +

                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +

                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +

                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +

                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +

                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +

                Build.USER.length() % 10; //13 位

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                serial = android.os.Build.getSerial();
            } else {
                serial = Build.SERIAL;
            }
            //API>=9 使用serial号
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            //serial需要一个初始化
            serial = "serial"; // 随便一个初始化
        }
        //使用硬件信息拼凑出来的15位号码
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }


    /*
     *//* 获取手机唯一序列号 *//*
    public static String getDeviceId(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            String deviceID = getDeviceID(context);
            Log.i(TAG, "当前的DeviceId" + deviceID);
            return deviceID;
        }else{
            if (PermissionUtil.checkSelfPermissions(context, Manifest.permission.READ_PHONE_STATE)){
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                String deviceId = tm.getDeviceId();// 手机设备ID，这个ID会被用为用户访问统计
                if (deviceId == null) {
                    deviceId = ToolUtils.getUUID();
                }
                return deviceId;
            }else {
                String deviceID = getDeviceID(context);
                return deviceID;
            }
        }
    }*/


    /**
     * 适配 Android Q 拿不到手机设备号
     * @param context
     * @return
     *//*
    public static String getDeviceID(Context context) {
        String device_unique_identifier = PreferenceUtils.getString(context, "device_unique_identifier", "");
        if (TextUtils.isEmpty(device_unique_identifier)) {
            String deviceID = "";
            try {
                //一共13位  如果位数不够可以继续添加其他信息
                deviceID = "" + Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                        Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                        Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                        Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                        Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                        Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                        Build.USER.length() % 10;
            } catch (Exception e) {
                String s = UUID.randomUUID().toString();
                PreferenceUtils.putString(context, "device_unique_identifier", s);
                return s;
            }
            PreferenceUtils.putString(context, "device_unique_identifier", deviceID);
            return deviceID;
        }else{
            return device_unique_identifier;
        }
    }*/

    /* 获取操作系统版本号 */
    public static String getOsVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /* 获取手机型号 */
    public static String getModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机品牌
     */
    public static String getBrand() {
        return android.os.Build.BRAND;
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
