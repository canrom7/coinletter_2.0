package com.iimm.miliao.util.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.List;

/**
 * MrLiu253@163.com
 *
 * @time 2019-08-27
 */
public class AndPermissionUtils {

    public static void setting(Context context) {
        AndPermission.with(context)
                .runtime()
                .setting()
                .start(0x01);
    }

    /**
     * 拍照
     * 相册
     *
     * @param activity
     */
    public static void Authorization(Activity activity, OnPermissionClickListener onPermissionClickListener) {
        AndPermission.with(activity).runtime()
                .permission(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .rationale(new RuntimeRationale())
                .onGranted(data -> {//权限允许
                    if (onPermissionClickListener != null) {
                        onPermissionClickListener.onSuccess();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) { // 权限拒绝
                        if (AndPermission.hasAlwaysDeniedPermission(activity, data)) {
                            if (onPermissionClickListener != null) {
                                onPermissionClickListener.onFailure(Permission.transformText(activity, data));
                            }
                        }
                    }
                }).start();
    }

    /**
     * 扫一扫
     * 拍照
     * 相册
     *
     * @param activity
     */
    public static void scanIt(Activity activity, OnPermissionClickListener onPermissionClickListener) {
        AndPermission.with(activity).runtime()
                .permission(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .rationale(new RuntimeRationale())
                .onGranted(data -> {//权限允许
                    if (onPermissionClickListener != null) {
                        onPermissionClickListener.onSuccess();
                    }
                })
                .onDenied(data -> { // 权限拒绝
                    if (onPermissionClickListener != null) {
                        onPermissionClickListener.onFailure(Permission.transformText(activity, data));
                    }
                }).start();
    }

    /**
     * 存储权限
     */
    public static void AuthorizationStorage(Activity activity, OnPermissionClickListener onPermissionClickListener) {
        AndPermission.with(activity).runtime()
                .permission(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .rationale(new RuntimeRationale())
                .onGranted(data -> {//权限允许
                    if (onPermissionClickListener != null) {
                        onPermissionClickListener.onSuccess();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) { // 权限拒绝
                        if (AndPermission.hasAlwaysDeniedPermission(activity, data)) {
                            if (onPermissionClickListener != null) {
                                onPermissionClickListener.onFailure(Permission.transformText(activity, data));
                            }
                        }
                    }
                }).start();
    }

    /**
     * 位置
     *
     * @param context
     * @param onPermissionClickListener
     */
    public static void position(Context context, OnPermissionClickListener onPermissionClickListener) {
        AndPermission.with(context).runtime()
                .permission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .rationale(new RuntimeRationale())
                .onGranted(data -> {//权限允许
                    if (onPermissionClickListener != null) {
                        onPermissionClickListener.onSuccess();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) { // 权限拒绝
                        if (AndPermission.hasAlwaysDeniedPermission(context, data)) {
                            if (onPermissionClickListener != null) {
                                onPermissionClickListener.onFailure(Permission.transformText(context, data));
                            }
                        }
                    }
                }).start();
    }

    /**
     * 相册或储存权限
     *
     * @param activity
     * @param onPermissionClickListener
     */
    public static void albumPermission(Activity activity, OnPermissionClickListener onPermissionClickListener) {
        AndPermission.with(activity).runtime()
                .permission(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .rationale(new RuntimeRationale())
                .onGranted(data -> {//权限允许
                    if (onPermissionClickListener != null) {
                        onPermissionClickListener.onSuccess();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) { // 权限拒绝
                        if (AndPermission.hasAlwaysDeniedPermission(activity, data)) {
                            if (onPermissionClickListener != null) {
                                onPermissionClickListener.onFailure(Permission.transformText(activity, data));
                            }
                        }
                    }
                }).start();
    }

    /**
     * 相册或储存权限
     *
     * @param context
     * @param onPermissionClickListener
     */
    public static void albumPermission(Context context, OnPermissionClickListener onPermissionClickListener) {
        AndPermission.with(context).runtime()
                .permission(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .rationale(new RuntimeRationale())
                .onGranted(data -> {//权限允许
                    if (onPermissionClickListener != null) {
                        onPermissionClickListener.onSuccess();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) { // 权限拒绝
                        if (AndPermission.hasAlwaysDeniedPermission(context, data)) {
                            if (onPermissionClickListener != null) {
                                onPermissionClickListener.onFailure(Permission.transformText(context, data));
                            }
                        }
                    }
                }).start();
    }

    /**
     * 通讯录
     *
     * @param activity
     * @param onPermissionClickListener
     */
    public static void addressbookPermission(Activity activity, OnPermissionClickListener onPermissionClickListener) {
        AndPermission.with(activity).runtime()
                .permission(Manifest.permission.READ_CONTACTS)
                .rationale(new RuntimeRationale())
                .onGranted(data -> {//权限允许
                    if (onPermissionClickListener != null) {
                        onPermissionClickListener.onSuccess();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) { // 权限拒绝
                        if (AndPermission.hasAlwaysDeniedPermission(activity, data)) {
                            if (onPermissionClickListener != null) {
                                onPermissionClickListener.onFailure(Permission.transformText(activity, data));
                            }
                        }
                    }
                }).start();
    }

    /**
     * 录音权限
     *
     * @param activity
     * @param onPermissionClickListener
     */
    public static void recordingPermission(Activity activity, OnPermissionClickListener onPermissionClickListener) {
        AndPermission.with(activity).runtime()
                .permission(Manifest.permission.RECORD_AUDIO)
                .rationale(new RuntimeRationale())
                .onGranted(data -> {//权限允许
                    if (onPermissionClickListener != null) {
                        onPermissionClickListener.onSuccess();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) { // 权限拒绝
                        if (AndPermission.hasAlwaysDeniedPermission(activity, data)) {
                            if (onPermissionClickListener != null) {
                                onPermissionClickListener.onFailure(Permission.transformText(activity, data));
                            }
                        }
                    }
                }).start();
    }

    /**
     * 录音权限
     *
     * @param context
     * @param onPermissionClickListener
     */
    public static void recordingPermission(Context context, OnPermissionClickListener onPermissionClickListener) {
        AndPermission.with(context).runtime()
                .permission(Manifest.permission.RECORD_AUDIO)
                .rationale(new RuntimeRationale())
                .onGranted(data -> {//权限允许
                    if (onPermissionClickListener != null) {
                        onPermissionClickListener.onSuccess();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) { // 权限拒绝
                        if (AndPermission.hasAlwaysDeniedPermission(context, data)) {
                            if (onPermissionClickListener != null) {
                                onPermissionClickListener.onFailure(Permission.transformText(context, data));
                            }
                        }
                    }
                }).start();
    }

    /**
     * 拍照
     * 相册
     * 录音
     *
     * @param activity
     */
    public static void shootVideo(Activity activity, OnPermissionClickListener onPermissionClickListener) {
        AndPermission.with(activity).runtime()
                .permission(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .rationale(new RuntimeRationale())
                .onGranted(data -> {//权限允许
                    if (onPermissionClickListener != null) {
                        onPermissionClickListener.onSuccess();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) { // 权限拒绝
                        if (AndPermission.hasAlwaysDeniedPermission(activity, data)) {
                            if (onPermissionClickListener != null) {
                                onPermissionClickListener.onFailure(Permission.transformText(activity, data));
                            }
                        }
                    }
                }).start();
    }

    /**
     * 强制获取
     */
    public static void forcedAcquisition(Activity activity, OnPermissionClickListener onPermissionClickListener) {
        String[] permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (android.os.Build.VERSION.SDK_INT >= 29) {  //大于 版本29
            permissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
        AndPermission.with(activity).runtime()
                .permission(permissions)
                .rationale(new RuntimeRationale())
                .onGranted(data -> {//权限允许
                    if (onPermissionClickListener != null) {
                        onPermissionClickListener.onSuccess();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) { // 权限拒绝
                        if (onPermissionClickListener != null) {
                            onPermissionClickListener.onFailure(Permission.transformText(activity, data));
                        }
                    }
                }).start();
    }

}
