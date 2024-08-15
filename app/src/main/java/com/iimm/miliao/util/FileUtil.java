package com.iimm.miliao.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.User;
import com.iimm.miliao.sortlist.PinYinUtil;
import com.iimm.miliao.ui.base.CoreManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class FileUtil {
    private static String TAG = "FileUtil";
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_ADUIO = 2;
    private static final int TYPE_VIDEO = 3;

    /**
     * {@link #TYPE_IMAGE}<br/>
     * {@link #TYPE_ADUIO}<br/>
     * {@link #TYPE_VIDEO} <br/>
     *
     * @param type
     * @return
     */
    private static String getPublicFilePath(int type) {
        String fileDir = null;
        String fileSuffix = null;
        switch (type) {
            case TYPE_ADUIO:
                fileDir = MyApplication.getInstance().mVoicesDir;
                fileSuffix = ".mp3";
                break;
            case TYPE_VIDEO:
                fileDir = MyApplication.getInstance().mVideosDir;
                fileSuffix = ".mp4";
                break;
            case TYPE_IMAGE:
                fileDir = MyApplication.getInstance().mPicturesDir;
                fileSuffix = ".jpg";
                break;
        }
        if (fileDir == null) {
            return null;
        }
        File file = new File(fileDir);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return null;
            }
        }
        return fileDir + File.separator + UUID.randomUUID().toString().replaceAll("-", "") + fileSuffix;
    }

    /**
     * {@link #TYPE_ADUIO}<br/>
     * {@link #TYPE_VIDEO} <br/>
     *
     * @param type
     * @return
     */
    private static String getPrivateFilePath(int type, String userId) {
        String fileDir = null;
        String fileSuffix = null;
        switch (type) {
            case TYPE_ADUIO:
                fileDir = MyApplication.getInstance().mAppDir + File.separator + userId + File.separator + Environment.DIRECTORY_MUSIC;
                fileSuffix = ".mp3";
                break;
            case TYPE_VIDEO:
                fileDir = MyApplication.getInstance().mAppDir + File.separator + userId + File.separator + Environment.DIRECTORY_MOVIES;
                fileSuffix = ".mp4";
                break;
        }
        if (fileDir == null) {
            return null;
        }
        File file = new File(fileDir);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return null;
            }
        }
        return fileDir + File.separator + UUID.randomUUID().toString().replaceAll("-", "") + fileSuffix;
    }

    public static String getRandomImageFilePath() {
        return getPublicFilePath(TYPE_IMAGE);
    }

    public static String getRandomAudioFilePath() {
        User user = CoreManager.requireSelf(MyApplication.getInstance());
        if (user != null && !TextUtils.isEmpty(user.getUserId())) {
            return getPrivateFilePath(TYPE_ADUIO, user.getUserId());
        } else {
            return getPublicFilePath(TYPE_ADUIO);
        }
    }

    public static String getRandomAudioAmrFilePath() {
        User user = CoreManager.requireSelf(MyApplication.getInstance());
        String filePath = null;
        if (user != null && !TextUtils.isEmpty(user.getUserId())) {
            filePath = getPrivateFilePath(TYPE_ADUIO, user.getUserId());
        } else {
            filePath = getPublicFilePath(TYPE_ADUIO);
        }
        if (!TextUtils.isEmpty(filePath)) {
            return filePath.replace(".mp3", ".amr");
        } else {
            return null;
        }
    }

    public static String getRandomVideoFilePath() {
        User user = CoreManager.requireSelf(MyApplication.getInstance());
        if (user != null && !TextUtils.isEmpty(user.getUserId())) {
            return getPrivateFilePath(TYPE_VIDEO, user.getUserId());
        } else {
            return getPublicFilePath(TYPE_VIDEO);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////

    public static void createFileDir(String fileDir) {
        File fd = new File(fileDir);
        if (!fd.exists()) {
            fd.mkdirs();
        }
    }

    /**
     * @param fullName
     */
    public static void delFile(String fullName) {
        File file = new File(fullName);
        if (file.exists()) {
            if (file.isFile()) {
                try {
                    file.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 删除文件夹里面的所有文件
     *
     * @param path String 文件夹路径 如 /sdcard/data/
     */
    public static void delAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            return;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            System.out.println(path + tempList[i]);
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]); // 先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]); // 再删除空文件夹
            }
        }
    }

    /**
     * 删除文件夹
     * <p>
     * String 文件夹路径及名称 如/sdcard/data/
     * String
     *
     * @return boolean
     */
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); // 删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            myFilePath.delete(); // 删除空文件夹
        } catch (Exception e) {
            System.out.println("删除文件夹操作出错");
            e.printStackTrace();
        }
    }

    public static File saveFileByBitmap(Bitmap bitmap, String fileDir, String fileName) {
        File dirFile = new File(fileDir);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }

        File myCaptureFile = new File(fileName);
        BufferedOutputStream bufferedOutputStream;
        try {
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bufferedOutputStream);
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e(TAG, "saveFileByBitmap: " + myCaptureFile.getAbsolutePath());
        return myCaptureFile;
    }

    public static String getSaveDirectory(String str) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + str + "/";
            File file = new File(rootDir);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return MyApplication.getContext().getFilesDir().getAbsolutePath();
                }
            }
            return rootDir;
        } else {
            return MyApplication.getContext().getFilesDir().getAbsolutePath();
        }
    }

    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private static void saveImageToGallery(Context content, Bitmap bitmap) {
        File appDir = new File(FileUtil.getFileDownDir());
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(MyApplication.getInstance().getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // 通知图库更新
        content.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
    }

    public static void saveImageToGallery2(Context context, Bitmap bitmap) {
        if (bitmap == null) {
            ToastUtil.showToast(context, context.getString(R.string.creating_qr_code));
        }
        // 1.保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "image");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 2.把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
            Toast.makeText(context, R.string.tip_saved_qr_code, Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 3.通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
    }

    /**
     * 将图片(本地/网络)保存至相册
     */
    public static void downImageToGallery(final Context context, String url) {
        if (url.toLowerCase().endsWith("gif")) {
            File file = new File(url);
            if (file.exists()) {
                // 将Gif拷贝到指定路径
                File file1 = new File(FileUtil.getFileDownDir());
                if (!file1.exists()) {
                    file1.mkdir();
                }
                String imagePath = FileUtil.getFileDownDir() + "/" + System.currentTimeMillis() + ".gif";
                copyFile(url, imagePath);
                Toast.makeText(context, R.string.tip_save_gif_success, Toast.LENGTH_SHORT).show();
                //把文件插入到系统图库
                try {
                    MediaStore.Images.Media.insertImage(MyApplication.getInstance().getContentResolver(),
                            file.getAbsolutePath(),  System.currentTimeMillis() + ".gif", null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Intent intentBroadcast = new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File mGifFile = new File(imagePath);
                intentBroadcast.setData(Uri.fromFile(mGifFile));
                context.sendBroadcast(intentBroadcast);
            } else {
                ToastUtil.showToast(context, context.getString(R.string.tip_save_gif_failed));
            }
        } else {
            Glide.with(context)
                    .load(url)
                    .asBitmap()
                    .dontAnimate()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            saveImageToGallery(context, resource);
                            Toast.makeText(context, R.string.tip_save_image_success, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            super.onLoadFailed(e, errorDrawable);
                            ToastUtil.showToast(context, context.getString(R.string.tip_save_image_failed));
                        }
                    });
        }
    }

    /**
     * 保存bitmap到本地
     */
    public static String saveBitmap(Bitmap bitmap) {
        File imageDir = new File(Environment.getExternalStorageDirectory(), "image");
        if (!imageDir.exists()) {
            imageDir.mkdir();
        }

        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(imageDir, fileName);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file.getAbsolutePath();
    }

    //添加到图库
    public static void updateGallery(Context context, String path) {

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(path);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    public static File createImageFileForEdit() {
        File imageDir = new File(Environment.getExternalStorageDirectory(), "image");
        if (!imageDir.exists()) {
            imageDir.mkdir();
        }

        String fileName = System.currentTimeMillis() + ".jpg";
        return new File(imageDir, fileName);
    }


    public static boolean isExist(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        File file = new File(path);
        return file.exists();
    }

    public static String getFileDir() {
        //注意事项：此处获取路径时不带 + File.separator ，在使用路径时需要 + File.separator
        String fileDirName = MyApplication.getInstance().getResources().getString(R.string.app_name);
        String fileDirNameStr = PinYinUtil.getPingYin(fileDirName).toLowerCase();
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // SD卡不存在
            File file = new File(Environment.getDataDirectory() + File.separator + fileDirNameStr);
            if (!file.exists()) {
                file.mkdirs();
            }
            return file.getAbsolutePath();
        } else {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + fileDirNameStr);
            if (!file.exists()) {
                file.mkdirs();
            }
            return file.getAbsolutePath();
        }
    }

    /**
     * 向不需要被 相册扫描的文件夹中添加忽略文件
     *
     * @param filePath
     */
    private static synchronized void addIgnore(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }

        File ignoreFile = new File(filePath + "/.nomedia");
        if (ignoreFile.exists() && ignoreFile.isFile()) {
            return;
        }

        try {
            ignoreFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFilePathForN(Uri uri, Context context) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getFilesDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }


    public static String getFilePath(Context context, Uri uri) {
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            String[] projection = {MediaStore.MediaColumns.DATA};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                if (cursor.moveToFirst()) {
                    String filePath = cursor.getString(column_index);
                    cursor.close();
                    return filePath;
                }
            } catch (Exception e) {
            }
        } else if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getAppFileDir() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    //更新图库
    public static void updatePhotoMedia(File file) {
        try {
            Context context = MyApplication.getContext();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(file));
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveImageToGallery(File file) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    MediaStore.Images.Media.insertImage(MyApplication.getInstance().getContentResolver(),
                            file.getAbsolutePath(), file.getName(), null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                MyApplication.getContext().sendBroadcast(intent);
            }
        });
    }

    public static String getFileNameWithEx(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        int start = filePath.lastIndexOf("/");
        //int end = filePath.lastIndexOf(".");
        if (start != -1) {
            return filePath.substring(start + 1);
        } else {
            return String.format("%d.png", System.currentTimeMillis());
        }
    }

    public static String getFileDownDir() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // 内存卡不存在
            File file = new File(Environment.getDataDirectory() + File.separator + "download_temp");
            if (!file.exists()) {
                file.mkdirs();
            }
            return file.getAbsolutePath();
        } else {
            File file = new File(Constants.FILE_DOWN_PATH);
            if (!file.exists()) {
                file.mkdirs();
            }
            return file.getAbsolutePath();
        }
    }

    public static String getLastName(String path) {
        if (TextUtils.isEmpty(path)) {
            return System.currentTimeMillis() + "";
        }
        int indexOf = path.lastIndexOf("/");
        if (indexOf + 1 == path.length()) {
            return System.currentTimeMillis() + "";
        } else {
            return path.substring(indexOf + 1);
        }
    }
}
