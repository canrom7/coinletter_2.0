package com.sensetime.liveness.silent;

import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewParent;

import java.util.List;

import com.sensetime.sample.common.R;
import com.sensetime.senseid.sdk.liveness.silent.OnLivenessListener;
import com.sensetime.senseid.sdk.liveness.silent.SilentLivenessApi;
import com.sensetime.senseid.sdk.liveness.silent.common.type.PixelFormat;
import com.sensetime.senseid.sdk.liveness.silent.common.type.ResultCode;
import com.sensetime.senseid.sdk.liveness.silent.common.util.ImageUtil;
import com.sensetime.senseid.sdk.liveness.silent.type.FaceDistance;
import com.sensetime.senseid.sdk.liveness.silent.type.FaceOcclusion;
import com.sensetime.senseid.sdk.liveness.silent.type.FaceState;
import com.sensetime.senseid.sdk.liveness.silent.type.OcclusionState;

public class SilentLivenessActivity extends AbstractSilentLivenessActivity {

    private OnLivenessListener mLivenessListener = new OnLivenessListener() {

        private long mLastStatusUpdateTime;

        @Override
        public void onInitialized() {
            mInputData = true;
        }

        @Override
        public void onStatusUpdate(final int faceState, final FaceOcclusion faceOcclusion, final int faceDistance) {
            if (SystemClock.elapsedRealtime() - this.mLastStatusUpdateTime < 300) {
                return;
            }

            if (faceState == FaceState.MISSED) {
                mNoteTextView.setText(R.string.common_tracking_missed);
                mNoticeImage.setImageResource(R.drawable.common_ic_notice_silent);
            } else if (faceDistance == FaceDistance.TOO_CLOSE) {
                mNoteTextView.setText(R.string.common_face_too_close);
                mNoticeImage.setImageResource(R.drawable.common_ic_closeto_silent);
            } else if (faceState == FaceState.OUT_OF_BOUND) {
                mNoteTextView.setText(R.string.common_tracking_out_of_bound);
                mNoticeImage.setImageResource(R.drawable.common_ic_notice_silent);
            } else if (faceState == FaceState.OCCLUSION) {
                final StringBuilder builder = new StringBuilder();
                boolean needComma = false;
                if (faceOcclusion.getBrowOcclusionState() == OcclusionState.OCCLUSION) {
                    builder.append(SilentLivenessActivity.this.getString(R.string.common_tracking_covered_brow));
                    needComma = true;
                }
                if (faceOcclusion.getEyeOcclusionState() == OcclusionState.OCCLUSION) {
                    builder.append(needComma ? "、" : "");
                    builder.append(SilentLivenessActivity.this.getString(R.string.common_tracking_covered_eye));
                    needComma = true;
                }
                if (faceOcclusion.getNoseOcclusionState() == OcclusionState.OCCLUSION) {
                    builder.append(needComma ? "、" : "");
                    builder.append(SilentLivenessActivity.this.getString(R.string.common_tracking_covered_nose));
                    needComma = true;
                }
                if (faceOcclusion.getMouthOcclusionState() == OcclusionState.OCCLUSION) {
                    builder.append(needComma ? "、" : "");
                    builder.append(SilentLivenessActivity.this.getString(R.string.common_tracking_covered_mouth));
                }

                mNoteTextView.setText(
                        SilentLivenessActivity.this.getString(R.string.common_tracking_covered, builder.toString()));
                mNoticeImage.setImageResource(R.drawable.common_ic_notice_silent);
            } else if (faceDistance == FaceDistance.TOO_FAR) {
                mNoteTextView.setText(R.string.common_face_too_far);
                mNoticeImage.setImageResource(R.drawable.common_ic_faraway_silent);
            } else {
                mNoteTextView.setText(R.string.common_detecting);
                mNoticeImage.setImageResource(R.drawable.common_ic_detection_silent);
            }

            this.mLastStatusUpdateTime = SystemClock.elapsedRealtime();
        }

        @Override
        public void onError(ResultCode resultCode) {
            mInputData = false;
            setResult(ActivityUtils.convertResultCode(resultCode));

            finish();
        }

        @Override
        public void onDetectOver(ResultCode resultCode, byte[] result, List imageData, Rect faceRect) {
            mInputData = false;
            List<byte[]> imageResult = (List<byte[]>) imageData;

            if (imageResult != null && !imageResult.isEmpty()) {
                ImageUtil.saveBitmapToFile(
                        BitmapFactory.decodeByteArray(imageResult.get(0), 0, imageResult.get(0).length), FILE_IMAGE);
            }

            switch (resultCode) {
                case OK:
                    setResult(RESULT_OK);
                    if (imageResult != null && !imageResult.isEmpty() && faceRect != null) {
                        SilentLivenessImageHolder.setImageData(imageResult.get(0), faceRect);
                    }
                    break;
                default:
                    setResult(ActivityUtils.convertResultCode(resultCode));
                    break;
            }
            finish();
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        SilentLivenessApi.init(SilentLivenessActivity.this, FILES_PATH + LICENSE_FILE_NAME,
                FILES_PATH + MODEL_FILE_NAME, mLivenessListener);
        SilentLivenessApi.setFaceDistanceRate(0.4F, 0.8F);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (!this.mInputData) {
            return;
        }
        int limitRectWidth = mCameraPreviewView.getWidth();
        int limitRectHeight = mCameraPreviewView.getHeight();

        ViewParent parent = mCameraPreviewView.getParent();

        if (parent != null) {
            limitRectWidth = ((View) parent).getWidth();
            limitRectHeight = ((View) parent).getHeight();
        }

        final Rect rect =
                new Rect(limitRectWidth / 6, limitRectHeight / 6, limitRectWidth / 6 * 5, limitRectHeight / 6 * 5);

        SilentLivenessApi.inputData(data, PixelFormat.NV21, this.mSenseCamera.getPreviewSize(),
                this.mCameraPreviewView.convertViewRectToPicture(rect), true, mSenseCamera.getRotationDegrees());
    }
}