package com.iimm.miliao.util;

import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.iimm.miliao.MyApplication;

import java.util.List;
import java.util.UUID;

/**
 * @author WangQX
 * @date 2019/8/2 0002 18:44
 * description:
 */
public class ToolUtils {

    /**
     * 判断服务是否运行
     *
     * @param className 完整包名的服务类名
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isServiceRunning(Context context, final String className) {
        ActivityManager am =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return false;
        List<ActivityManager.RunningServiceInfo> info = am.getRunningServices(0x7FFFFFFF);
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningServiceInfo aInfo : info) {
            if (className.equals(aInfo.service.getClassName())) return true;
        }
        return false;
    }

    public static boolean isEquals(String v1, String v2) {
        if (TextUtils.isEmpty(v1) || TextUtils.isEmpty(v2)) {
            return false;
        }
        return v1.equals(v2);
    }

    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static void copyContent(String content) {
        if (TextUtils.isEmpty(content)) {
            return;
        }
        //获取剪贴板管理器：
        ClipboardManager cmb = (ClipboardManager) MyApplication.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
        try {
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("Label", content);
            // 将ClipData内容放到系统剪贴板里。
            cmb.setPrimaryClip(mClipData);
            MyApplication.applicationHandler.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast(MyApplication.getInstance(), "已复制到剪切板");
                }
            });
        } catch (Exception e) {
            ClipData mClipData = ClipData.newPlainText("Label", content);
            if (null != cmb) {
                cmb.setPrimaryClip(mClipData);
            }
        }
    }

    /**
     * 转义sqlite中的要搜索的特殊字符
     *
     * @param keyWord
     * @return
     */
    public static String sqliteEscape(String keyWord) {
        keyWord = keyWord.replace("/", "//");
        keyWord = keyWord.replace("'", "''");
        keyWord = keyWord.replace("[", "/[");
        keyWord = keyWord.replace("]", "/]");
        keyWord = keyWord.replace("%", "/%");
        keyWord = keyWord.replace("&", "/&");
        keyWord = keyWord.replace("_", "/_");
        keyWord = keyWord.replace("(", "/(");
        keyWord = keyWord.replace(")", "/)");
        return keyWord;
    }

    public static int getIsEncrype(JSONObject jObject) {
        return getIsEncrype(jObject, "isEncrypt");
    }

    public static int getIsEncrype(JSONObject jObject, String key) {
        if (jObject == null || TextUtils.isEmpty(key)) {
            return 0;
        }
        return jObject.getIntValue(key);
    }

    public static String getStringValueFromJSONObject(JSONObject jObject, String key) {
        if (jObject == null || TextUtils.isEmpty(key)) {
            return "";
        }

        String value = "";
        try {
            value = jObject.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (value == null) {
                value = "";
            }
            return value;
        }
    }

    public static int getIntValueFromJSONObject(JSONObject jObject, String key) {
        if (jObject == null || TextUtils.isEmpty(key)) {
            return 0;
        }
        int value = 0;
        try {
            value = jObject.getIntValue(key);
        } catch (Exception e) {
            e.printStackTrace();
            String v = jObject.getString(key);
            if ("true".equals(v)) {
                value = 1;
            } else {
                value = 0;
            }
        } finally {
            return value;
        }
    }

    public static long getLongValueFromJSONObject(JSONObject jObject, String key) {
        long value = 0;
        try {
            if (jObject != null) {
                value = jObject.getLongValue(key);
            } else {
                value = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            value = 0;
        }
        return value;
    }

    public static double getDoubleFromJSONObject(JSONObject jObject, String key) {
        double value = 0;
        try {
            if (jObject != null) {
                value = jObject.getDoubleValue(key);
            } else {
                value = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            value = 0;
        }
        return value;
    }
}
