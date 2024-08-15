package com.sensetime.liveness.silent;

import android.graphics.Rect;

import java.util.Arrays;

/**
 * Created on 2016/12/06 13:18.
 *
 * @author Han Xu
 */
public final class SilentLivenessImageHolder {

    private static byte[] mImageData = null;
    private static Rect mFaceRect = null;

    public static byte[] getImageData() {
        return mImageData;
    }

    public static Rect getFaceRect() {
        return mFaceRect;
    }

    static void setImageData(byte[] imageData, Rect rect) {
        mImageData = Arrays.copyOf(imageData, imageData.length);
        mFaceRect = rect;
    }

    private SilentLivenessImageHolder() {
        // Do nothing.
    }
}