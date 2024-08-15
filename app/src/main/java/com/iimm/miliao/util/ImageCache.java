package com.iimm.miliao.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.LruCache;
import android.widget.ImageView;

import com.jakewharton.disklrucache.DiskLruCache;
import com.jakewharton.disklrucache.DiskLruCache.Snapshot;
import com.iimm.miliao.MyApplication;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by wangqx on 2017/11/21.
 */

public class ImageCache {
    private static final String TAG = "ImageCache";
    private ImageView imgView;
    private String path;

    //创建cache
    private static LruCache<String, Bitmap> mLruCache;

    private static DiskLruCache mDiskLruCache;
    //磁盘缓存大小
    private static final int DISKLRUCACHE_MAXSIZE = 10 * 1024 * 1024;

    static {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();//获取最大的运行内存
        int maxSize = maxMemory / 16;//拿到缓存的内存大小
        mLruCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //这个方法会在每次存入缓存的时候调用
                int byteCount = value.getRowBytes() * value.getHeight();
                return byteCount;
            }
        };
        try {
            // 获取DiskLruCahce对象
            mDiskLruCache = DiskLruCache.open(getDiskCacheDir(MyApplication.getContext().getApplicationContext(), "thumb"),
                    getAppVersion(MyApplication.getContext()), 1, DISKLRUCACHE_MAXSIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 存入缓存（内存缓存，磁盘缓存）
     */
    public static void putBitmap(String key, Bitmap bitmap) {
        // 存入LruCache缓存
        mLruCache.put(key, bitmap);
        // 判断是否存在DiskLruCache缓存，若没有存入
        try {
            if (mDiskLruCache.get(key) == null) {
                DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                if (editor != null) {
                    OutputStream outputStream = editor.newOutputStream(0);
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                        editor.commit();
                    } else {
                        editor.abort();
                    }
                }
                mDiskLruCache.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeBitmap(String key) {
        if (mLruCache != null && mLruCache.get(key) != null) {
            mLruCache.remove(key);
        }
        try {
            if (mDiskLruCache != null && mDiskLruCache.get(key) != null) {
                mDiskLruCache.remove(key);
                mDiskLruCache.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从内存获取缓存
     *
     * @param key
     * @return
     */
    public static Bitmap getBitmap(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        if (mDiskLruCache!=null&&mLruCache.get(key) != null) {
            // 从LruCache缓存中取
            return mLruCache.get(key);
        } else {
            try {
                if (mDiskLruCache!=null&&mDiskLruCache.get(key) != null) {
                    // 从DiskLruCahce取
                    Snapshot snapshot = mDiskLruCache.get(key);
                    Bitmap bitmap = null;
                    if (snapshot != null) {
                        bitmap = BitmapFactory.decodeStream(snapshot.getInputStream(0));
                        // 存入LruCache缓存
                        mLruCache.put(key, bitmap);
                    }
                    return bitmap;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 该方法会判断当前sd卡是否存在，然后选择缓存地址
     *
     * @param context
     * @param uniqueName
     * @return
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * 获取应用版本号
     *
     * @param context
     * @return
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }
}

