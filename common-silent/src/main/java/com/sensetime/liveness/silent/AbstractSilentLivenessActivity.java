package com.sensetime.liveness.silent;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import com.sensetime.liveness.silent.ui.camera.SenseCamera;
import com.sensetime.liveness.silent.ui.camera.SenseCameraPreview;
import com.sensetime.sample.common.R;
import com.sensetime.senseid.sdk.liveness.silent.SilentLivenessApi;
import com.sensetime.senseid.sdk.liveness.silent.common.util.FileUtil;

abstract class AbstractSilentLivenessActivity extends Activity
        implements Camera.PreviewCallback, SenseCameraPreview.StartListener {
    public static final String FILES_PATH = Environment.getExternalStorageDirectory().getPath() + "/sensetime/";
    public static final String LICENSE_FILE_NAME = "SenseID_Liveness_Silent.lic";
    public static final String MODEL_FILE_NAME = "SenseID_Silent_Liveness.model";
    public static final String FILE_IMAGE = FILES_PATH + "silent_liveness/silent_liveness_image.jpg";

    protected ImageView mNoticeImage = null;
    protected View mLoadingView = null;
    protected TextView mNoteTextView = null;

    protected SenseCameraPreview mCameraPreviewView = null;
    protected SenseCamera mSenseCamera;

    protected boolean mInputData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            setResult(ActivityUtils.RESULT_CODE_NO_PERMISSIONS);
            finish();
            return;
        }

        setContentView(R.layout.common_activity_liveness_silent);

        findViewById(R.id.linkface_txt_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        mNoteTextView = (TextView) findViewById(R.id.linkface_txt_note);
        mNoteTextView.setText(R.string.common_tracking_missed);
        mLoadingView = findViewById(R.id.pb_loading);
        mNoticeImage = (ImageView) findViewById(R.id.img_notice);
        mNoteTextView.setText(R.string.common_tracking_missed);
        mNoticeImage.setImageResource(R.drawable.common_ic_notice_silent);

        mCameraPreviewView = (SenseCameraPreview) findViewById(R.id.camera_preview);
        this.mCameraPreviewView.setStartListener(this);
        mSenseCamera = new SenseCamera.Builder(this).setFacing(SenseCamera.CAMERA_FACING_FRONT)
                .setRequestedPreviewSize(640, 480)
                .build();

        File dir = new File(FILES_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File image = new File(FILE_IMAGE);
        if (image.exists()) {
            //noinspection ResultOfMethodCallIgnored
            image.delete();
        }

        FileUtil.copyAssetsToFile(this, LICENSE_FILE_NAME, FILES_PATH + LICENSE_FILE_NAME);
        FileUtil.copyAssetsToFile(this, MODEL_FILE_NAME, FILES_PATH + MODEL_FILE_NAME);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            this.mCameraPreviewView.start(this.mSenseCamera);
            this.mSenseCamera.setOnPreviewFrameCallback(this);
        } catch (Exception e) {
            setResult(ActivityUtils.RESULT_CODE_CAMERA_ERROR);
            this.finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.mInputData = false;

        SilentLivenessApi.cancel();

        mLoadingView.setVisibility(View.GONE);

        this.mCameraPreviewView.stop();
        this.mCameraPreviewView.release();

        setResult(RESULT_CANCELED);

        if (!this.isFinishing()) {
            finish();
        }
    }

    @Override
    public void onFail() {
        setResult(ActivityUtils.RESULT_CODE_CAMERA_ERROR);
        if (!isFinishing()) {
            this.finish();
        }
    }

    protected boolean checkPermission(String... permissions) {
        if (permissions == null || permissions.length < 1) {
            return true;
        }
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
