package com.iimm.miliao.ui.mucfile;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.cjt2325.cameralibrary.util.LogUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.bean.UploadFileResult;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.OBSUtils;
import com.iimm.miliao.util.TanX;
import com.iimm.miliao.util.ThreadManager;
import com.iimm.miliao.volley.Result;

import cz.msebera.android.httpclient.Header;

import java.io.File;

/**
 * Created by Administrator on 2017/7/10.
 */

public class UploadingHelper {


    public static void upFile(Context context, CoreManager coreManager, String accessToken, String userId, final File file, final OnUpFileListener onUpFileListener) {
        if (coreManager.getConfig().IS_OPEN_OBS_STATUS != 0) {
            if (!file.exists()) {
                onUpFileListener.onFailure("文件不存在", file.getAbsolutePath());
                return;
            }

            if (TextUtils.isEmpty(userId)) {
                onUpFileListener.onFailure("用户ID为空", file.getAbsolutePath());
                return;
            }

            if (onUpFileListener == null) {
                onUpFileListener.onFailure("上传监听不能为空", file.getAbsolutePath());
                return;
            }
            Handler handler = new Handler();
            ThreadManager.getPool().execute(() -> {
                Runnable runnable = () -> upfile(accessToken, userId, file, onUpFileListener);
                try {
                    String result = OBSUtils.upLoadFile(context, coreManager, file, null, msg -> {
                        handler.post(runnable);
                    });
                    if (onUpFileListener != null) {
                        if (!TextUtils.isEmpty(result)) {
                            LogUtil.i("上传OBS成功");
                            onUpFileListener.onSuccess(result, file.getAbsolutePath());
                        } else {
                            handler.post(runnable);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(runnable);
                }
            });
        } else {
            upfile(accessToken, userId, file, onUpFileListener);
        }
    }

    public static void upfile(String accessToken, String userId, final File file, final OnUpFileListener onUpFileListener) {
        try {
            if (!file.exists()) {
                onUpFileListener.onFailure("文件不存在", file.getAbsolutePath());
            }

            if (TextUtils.isEmpty(userId)) {
                onUpFileListener.onFailure("用户ID为空", file.getAbsolutePath());
            }

            if (onUpFileListener == null) {
                onUpFileListener.onFailure("上传监听不能为空", file.getAbsolutePath());
            }

            AsyncHttpClient client = new AsyncHttpClient();
            TanX.Log("上传文件：" + file.getAbsolutePath());
            RequestParams params = new RequestParams();
            params.put("access_token", accessToken);
            params.put("userId", userId);
            params.put("file1", file);
            params.put("validTime", "-1");// 文件有效期

            client.post(CoreManager.requireConfig(MyApplication.getInstance()).UPLOAD_URL, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {//数据发送成功，并且有返回值<不是上传成功。。。>
                    TanX.Log("相应码：" + arg0);
                    String url = null;
                    if (arg0 == 200) {
                        UploadFileResult result = JSON.parseObject(new String(arg2), UploadFileResult.class);
                        UploadFileResult.Data data = result.getData();
                        //上传成功
                        if (result == null || result.getResultCode() != Result.CODE_SUCCESS || result.getData() == null
                                || result.getSuccess() != result.getTotal()) {
                            onUpFileListener.onFailure("上传失败", file.getAbsolutePath());
                        } else {

                            if (!TextUtils.isEmpty(url = getImagesUrl(data))) {

                            } else if (!TextUtils.isEmpty(url = getVideosUrl(data))) {

                            } else if (!TextUtils.isEmpty(url = getAudiosUrl(data))) {

                            } else if (!TextUtils.isEmpty(url = getFilesUrl(data))) {

                            } else if (!TextUtils.isEmpty(url = getOthersUrl(data))) {
                                url = getOthersUrl(data);
                            }
                        }
                    }
                    if (TextUtils.isEmpty(url)) {
                        // 返回成功，但是却获取不到对应的URL，服务器返回值异常<概率极小>
                        Log.i("roamer", "上传文件成功了 但是URL 是空的");
                        onUpFileListener.onFailure("上传文件成功了 但是URL 是空的", file.getAbsolutePath());
                    } else {
                        Log.i("roamer", "上传文件成功了");
                        onUpFileListener.onSuccess(url, file.getAbsolutePath());
                    }
                }

                @Override
                public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            onUpFileListener.onFailure("上传异常", file.getAbsolutePath());
        }
    }

    private static String getAudiosUrl(UploadFileResult.Data data) {
        TanX.Log("语音格式");
        if (data.getAudios() != null && data.getAudios().size() > 0) {
            return data.getAudios().get(0).getOriginalUrl();
        } else {
            return "";
        }
    }

    private static String getFilesUrl(UploadFileResult.Data data) {
        TanX.Log("文件格式");
        if (data.getFiles() != null && data.getFiles().size() > 0) {
            return data.getFiles().get(0).getOriginalUrl();
        } else {
            return "";
        }
    }

    private static String getVideosUrl(UploadFileResult.Data data) {
        TanX.Log("视频格式");
        if (data.getVideos() != null && data.getVideos().size() > 0) {
            return data.getVideos().get(0).getOriginalUrl();
        } else {
            return "";
        }
    }

    private static String getImagesUrl(UploadFileResult.Data data) {
        TanX.Log("图片格式");
        if (data.getImages() != null && data.getImages().size() > 0) {
            return data.getImages().get(0).getOriginalUrl();
        } else {
            return "";
        }
    }

    private static String getOthersUrl(UploadFileResult.Data data) {
        TanX.Log("其他格式");
        if (data.getOthers() != null && data.getOthers().size() > 0) {
            return data.getOthers().get(0).getOriginalUrl();
        } else {
            return "";
        }
    }

    public interface OnUpFileListener {
        void onSuccess(String url, String filePath);

        void onFailure(String err, String filePath);
    }
}
