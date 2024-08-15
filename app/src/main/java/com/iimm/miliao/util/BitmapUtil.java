package com.iimm.miliao.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.iimm.miliao.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtil {

    private static final String TAG = "BitmapUtil";

    /**
     * 保存图片到本地
     */
    public static String saveBitmap(Resources res) {
        Bitmap bitmap = BitmapFactory.decodeResource(res, R.mipmap.icon);
        File file;
        isHaveSDCard();
        if (isHaveSDCard()) {
            file = Environment.getExternalStorageDirectory();
        } else {
            file = Environment.getDataDirectory();
        }

        file = new File(file.getPath() + "/MotieReader/data/");
        if (!file.isDirectory()) {
            file.delete();
            file.mkdirs();
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        Log.d(TAG, "文件路径为：" + file.getPath());
        Log.d(TAG, "文件名称为：" + file.getName());
        return writeBitmap(file.getPath(), "qrcode.png", bitmap);
    }

    /**
     * 将要分享的图片先保存在本地
     *
     * @param path
     * @param name
     * @param bitmap
     */
    public static String writeBitmap(String path, String name, Bitmap bitmap) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        File _file = new File(path + "/" + name);
        if (_file.exists()) {
            _file.delete();
        }

        createFile(_file.getPath());

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(_file);
            if (name != null && !"".equals(name)) {
                int index = name.lastIndexOf(".");
                if (index != -1 && (index + 1) < name.length()) {
                    String extension = name.substring(index + 1).toLowerCase();
                    if ("png".equals(extension)) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    } else if ("jpg".equals(extension)
                            || "jpeg".equals(extension)) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //文件保存后，调用分享
        return _file.getPath();
    }

    //如果文件不存在，则创建文件及目录
    public static boolean createFile(String destFileName) {
        Log.d(TAG, "创建文件 " + destFileName);
        File file = new File(destFileName);
        if (file.exists()) {
            Log.d(TAG, "创建单个文件" + destFileName + "失败，目标文件已存在！");
            return false;
        }
        if (destFileName.endsWith(File.separator)) {
            Log.d(TAG, "创建单个文件" + destFileName + "失败，目标文件不能为目录！");
            return false;
        }
        //判断目标文件所在的目录是否存在
        if (!file.getParentFile().exists()) {
            //如果目标文件所在的目录不存在，则创建父目录
            Log.d(TAG, "目标文件所在目录不存在，准备创建它！");
            if (!file.getParentFile().mkdirs()) {
                Log.d(TAG, "创建目标文件所在目录失败！");
                return false;
            }
        }
        //创建目标文件
        try {
            if (file.createNewFile()) {
                Log.d(TAG, "创建单个文件" + destFileName + "成功！");
                return true;
            } else {
                Log.d(TAG, "创建单个文件" + destFileName + "失败！");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "创建单个文件" + destFileName + "失败！" + e.getMessage());
            return false;
        }
    }


    public static boolean isHaveSDCard() {
        String SDState = android.os.Environment.getExternalStorageState();
        if (SDState.equals(android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    public static boolean saveBitmapToSDCard(Bitmap bmp, String strPath) {
        if (bmp == null) {
            return false;
        }
        if (TextUtils.isEmpty(strPath)) {
            return false;
        }
        try {
            File file = new File(strPath.substring(0, strPath.lastIndexOf("/")));
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(strPath);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = BitmapUtil.bitampToByteArray(bmp);
            fos.write(buffer);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static byte[] bitampToByteArray(Bitmap bitmap) {
        byte[] array = null;
        try {
            if (null != bitmap) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                array = os.toByteArray();
                os.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return array;
    }


    /**
     * xuan  2018-12-19 19:30:51
     * 安全的加载一个bitmap
     *
     * @param pathName
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeBitmapFromFile(String pathName, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(pathName, options);
        return createScaleBitmap(src, reqWidth, reqHeight, options.inSampleSize);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    private static Bitmap createScaleBitmap(Bitmap src, int dstWidth, int dstHeight, int inSampleSize) {
        // 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响，我们这里是缩小图片，所以直接设置为false
        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        if (src != dst) { // 如果没有缩放，那么不回收
            src.recycle(); // 释放Bitmap的native像素数组
        }
        return dst;
    }

    public static String createImageCachePath() {


        return null;
    }
}
