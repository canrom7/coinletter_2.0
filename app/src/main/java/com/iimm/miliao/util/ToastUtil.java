package com.iimm.miliao.util;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;

import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;

/**
 * @auther WangQX
 * created at 2019/3/7 0007
 * 描述：Toast工具类
 */

public class ToastUtil {
    // Toast
    private static Toast toast;
    private static Toast longToast;
    private static Resources resources = MyApplication.getInstance().getResources();
    private static Context context = MyApplication.getInstance().getApplicationContext();

    /**
     * 短时间显示Toast
     *
     * @param message
     */
    public static void showToast(final String message) {
        MyApplication.applicationHandler.post(new Runnable() {
            @Override
            public void run() {
                if (longToast != null) {
                    longToast.cancel();
                }
                if (toast == null) {
                    toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                } else {
                    toast.setText(message);
                }
                toast.show();
            }
        });
    }

    /**
     * 短时间显示Toast
     *
     * @param message
     */
    public static void showLongToast(final String message) {
        MyApplication.applicationHandler.post(new Runnable() {
            @Override
            public void run() {
                if (toast != null) {
                    toast.cancel();
                }
                if (longToast == null) {
                    longToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
                } else {
                    longToast.setText(message);
                }
                longToast.show();
            }
        });
    }

    /**
     * 短时间显示Toast
     *
     * @param stringId
     */
    public static void showToast(final int stringId) {
        String content = resources.getString(stringId);
        showToast(content);
    }

    /**
     * 短时间显示Toast
     *
     * @param stringId
     */
    public static void showToast(Context context,final int stringId) {
        String content = resources.getString(stringId);
        showToast(content);
    }


    /**
     * 短时间显示Toast
     */
    public static void showToast(Context context,String msg) {
        showToast(msg);
    }

    /**
     * 长时间显示Toast
     *
     * @param stringId
     */
    public static void showLongToast(final int stringId) {
        String content = resources.getString(stringId);
        showLongToast(content);
    }

    /**
     * 长时间显示Toast
     *
     * @param
     */
    public static void showLongToast(Context context,String str) {
        showLongToast(str);
    }
    public static void showErrorNet(Context context) {
        showToast(R.string.net_exception);
    }


    public static void showErrorData(Context context) {
        showToast( context.getString(R.string.data_exception));
    }


    public static void showNetError(Context context) {
        showToast(R.string.net_exception);
    }

    /**
     * Hide the toast, if any.
     */
    /**
     * 取消吐司显示
     */
    public static void cancel() {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        if (longToast != null) {
            longToast.cancel();
            longToast = null;
        }
    }
}
