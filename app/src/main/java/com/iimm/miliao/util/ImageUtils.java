package com.iimm.miliao.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;

import pl.droidsonroids.gif.GifImageView;

import com.bumptech.glide.signature.StringSignature;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.db.dao.UserAvatarDao;


import java.io.File;


public class ImageUtils {
    /**
     * 在不加载图片到内存的情况下，获取图片宽高
     */
    public static int[] getImageWidthHeight(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options); // 此时返回的bitmap为null
        /**
         * options.outHeight为原始图片的高
         */
        return new int[]{options.outWidth, options.outHeight};
    }

    public static void loadGifImageWithUrl(Context context, String url, final ImageView imageView, int width, int height, RequestListener<String, GifDrawable> listener) {
        Glide.with(context)
                .load(url)
                .asGif()
                .listener(listener)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.fez)
                .override(width, height)
                .error(R.drawable.pic_error)
                .into(imageView);
    }

    public static void loadGifImageWithUrl(Context context, String url, final ImageView imageView, int width, int height) {
        Glide.with(context)
                .load(url)
                .asGif()
                .skipMemoryCache(true)
                .thumbnail(0.1f)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(R.drawable.fez)
                .override(width, height)
                .error(R.drawable.pic_error)
                .into(imageView);
    }

    public static void loadGifImageWithUrl(Context context, String url, SimpleTarget<GifDrawable> listener) {
        Glide.with(context)
                .load(url)
                .asGif()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(R.drawable.fez)
                .error(R.drawable.pic_error)
                .into(listener);
    }

    public static void loadGifImageWithUrl(Context context, String url, int w, int h, GifImageView imageView) {
        Glide.with(context)
                .load(url)
                .asGif()
                .placeholder(R.drawable.fez)
                .skipMemoryCache(true)
                .override(w, h)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.fez)
                .error(R.drawable.pic_error)
                .into(imageView);
    }

    public static void loadGifImageWithFile(Context context, File file, int w, int h, GifImageView imageView) {
        Glide.with(context)
                .load(file)
                .asGif()
                .skipMemoryCache(true)
                .override(w, h)
                .placeholder(R.drawable.fez)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(R.drawable.fez)
                .error(R.drawable.pic_error)
                .into(imageView);
    }

    public static void loadGifImageWithFile(Context context, File file, SimpleTarget<GifDrawable> listener) {
        Glide.with(context)
                .load(file)
                .asGif()
                .skipMemoryCache(true)
                .placeholder(R.drawable.fez)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(R.drawable.fez)
                .error(R.drawable.pic_error)
                .into(listener);
    }

    public static void loadGifImageWithUrl(Context context, String url, final ImageView imageView) {
        Glide.with(context)
                .load(url)
                .asGif()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(R.drawable.fez)
                .error(R.drawable.pic_error)
                .into(imageView);
    }


    public static void loadSignalAvatarWithUrl(Context context, String url, final ImageView imageView,String userId) {
        String time = UserAvatarDao.getInstance().getUpdateTime(userId);
        Glide.with(context)
                .load(url)
                .asBitmap()
                .signature(new StringSignature(time))
                .error(R.drawable.avatar_normal)
                .placeholder(R.drawable.avatar_normal)
                .into(imageView);
    }

    public static void loadGroupAvatarWithUrl(Context context, String url, final ImageView imageView) {
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR ? R.drawable.groupdefault : R.drawable.groupdefault_square)
                .error(Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR ? R.drawable.groupdefault : R.drawable.groupdefault_square)
                .into(imageView);
    }

    public static void loadImageWithUrl(Context context, String url, final ImageView imageView) {
        Glide.with(MyApplication.getContext())
                .load(url)
                .error(R.drawable.fez)
                .into(imageView);
    }

    /**
     * 加载网络图片
     */
    public static void loadImageWithUrl(String url, ImageView imageView, int w, int h) {
        Glide.with(MyApplication.getContext())
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(R.drawable.fez)
                .centerCrop()
                .override(w, h)
                .into(imageView);
    }

    /**
     * 加载网络图片
     */
    public static void loadImageWithUrl(String url, SimpleTarget<Bitmap> listener) {

        Glide.with(MyApplication.getContext())
                .load(url)
                .asBitmap()
                .error(R.drawable.fez)
                .into(listener);

    }
}
