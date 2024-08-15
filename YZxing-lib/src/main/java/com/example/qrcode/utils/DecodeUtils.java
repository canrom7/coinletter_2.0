package com.example.qrcode.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.example.qrcode.ScannerActivity;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.Map;

/**
 * Created by yangyu on 2017/11/27.
 */

public class DecodeUtils {
    private static final String TAG = "DecodeUtils";

    /**
     * 从开源扫码app复制来的压缩图片方法，
     */
    public static Bitmap compressPicture(Context ctx, Uri decodeUri) throws FileNotFoundException {
        // 做些预处理提升扫码成功率，
        // 预读一遍获取图片比例，使用inSampleSize压缩图片分辨率到恰到好处，
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        ContentResolver resolver = ctx.getContentResolver();

        InputStream in = null;
        try {
            in = resolver.openInputStream(decodeUri);
            BitmapFactory.decodeStream(in, null, options);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    Log.w(TAG, ioe);
                }
            }
        }

        int height = options.outHeight;
        int width = options.outWidth;
        options.inJustDecodeBounds = false;
        options.inSampleSize = (int) Math.round(Math.sqrt(height * width / (double) (320 * 240)));

        in = null;
        Bitmap bitmap;
        try {
            in = resolver.openInputStream(decodeUri);
            bitmap = BitmapFactory.decodeStream(in, null, options);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    Log.w(TAG, ioe);
                }
            }
        }
        return bitmap;
    }

    public static Bitmap compressPicture1(Context ctx, Uri decodeUri) throws FileNotFoundException {
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        InputStream input = null;
        try {
            input = ctx.getContentResolver().openInputStream(decodeUri);
            onlyBoundsOptions.inJustDecodeBounds = true;
            onlyBoundsOptions.inDither = true;//optional
            onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ioe) {
                    Log.w(TAG, ioe);
                }
            }
        }
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1)){
            return null;
        }
        //图片分辨率以480x800为标准
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (originalHeight / hh);
        }
        if (be <= 0){
            be = 1;
        }
        //比例压缩
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;//设置缩放比例
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        Bitmap bitmap;
        try {
            input = ctx.getContentResolver().openInputStream(decodeUri);
            bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ioe) {
                    Log.w(TAG, ioe);
                }
            }
        }
        return bitmap;
    }

    public static Bitmap compressPicture2(Context ctx, Uri decodeUri) throws FileNotFoundException {
        ContentResolver resolver = ctx.getContentResolver();
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(resolver, decodeUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }


    public static Result decodeFromPicture(Bitmap bitmap) {
        if (bitmap == null)
            return null;
        int picWidth = bitmap.getWidth();
        int picHeight = bitmap.getHeight();
        int[] pix = new int[picWidth * picHeight];
        Log.e(TAG, "decodeFromPicture:图片大小： " + bitmap.getByteCount() / 1024 / 1024 + "M");
        bitmap.getPixels(pix, 0, picWidth, 0, 0, picWidth, picHeight);
        //构造LuminanceSource对象
        RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(picWidth
                , picHeight, pix);
        BinaryBitmap bb = new BinaryBitmap(new HybridBinarizer(rgbLuminanceSource));
        //因为解析的条码类型是二维码，所以这边用QRCodeReader最合适。
        QRCodeReader qrCodeReader = new QRCodeReader();
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        hints.put(DecodeHintType.TRY_HARDER, true);
        Result result;
        try {
            result = qrCodeReader.decode(bb, hints);
            return result;
        } catch (NotFoundException | ChecksumException | FormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class DecodeAsyncTask extends AsyncTask<Bitmap, Integer, Result> {

        private WeakReference<ScannerActivity> activity;
        private Result result;

        public DecodeAsyncTask(ScannerActivity activity) {
            this.activity = new WeakReference<ScannerActivity>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Result doInBackground(Bitmap... bitmaps) {
            result = decodeFromPicture(bitmaps[0]);
            return result;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            if (result != null) {
                if (activity.get() != null) {
                    activity.get().handDecode(result);
                }
            } else {
                activity.get().handDecode(1);
            }

        }
    }

    public static class DecodeAsyncTask1 extends AsyncTask<Bitmap, Integer, Result> {

        private WeakReference<ScannerActivity> activity;
        private Result result;

        public DecodeAsyncTask1(ScannerActivity activity) {
            this.activity = new WeakReference<ScannerActivity>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Result doInBackground(Bitmap... bitmaps) {
            result = decodeFromPicture(bitmaps[0]);
            return result;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            if (result != null) {
                if (activity.get() != null) {
                    activity.get().handDecode(result);
                }
            } else {
                if (activity.get() != null) {
                    activity.get().handDecode(2);
                }
            }

        }
    }


    public static class DecodeAsyncTask2 extends AsyncTask<Bitmap, Integer, Result> {

        private WeakReference<ScannerActivity> activity;
        private Result result;

        public DecodeAsyncTask2(ScannerActivity activity) {
            this.activity = new WeakReference<ScannerActivity>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Result doInBackground(Bitmap... bitmaps) {
            result = decodeFromPicture(bitmaps[0]);
            return result;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            if (result != null) {
                if (activity.get() != null) {
                    activity.get().handDecode(result);
                }
            } else {
                if (activity.get() != null) {
                    activity.get().handDecode(3);
                }
            }

        }
    }
}


