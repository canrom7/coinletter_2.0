package com.iimm.miliao.helper;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.cjt2325.cameralibrary.util.LogUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.EventUploadCancel;
import com.iimm.miliao.bean.EventUploadFileRate;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.UploadFileResult;
import com.iimm.miliao.bean.UploadingFile;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.UploadingFileDao;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.LogUtils;
import com.iimm.miliao.util.OBSUtils;
import com.iimm.miliao.util.TanX;
import com.iimm.miliao.util.ThreadManager;
import com.iimm.miliao.util.UploadCacheUtils;
import com.iimm.miliao.volley.Result;

import cz.msebera.android.httpclient.Header;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * 专门用来上传的
 */
public class UploadEngine {
    private static String TAG = "UploadEngine";
    private static Map<String, RequestHandle> requestHandleMap = new HashMap<>();

    public static void uploadImFile(Context context, CoreManager cor, String accessToken, final String loginUserId, final String toUserId, final ChatMessage message, final ImFileUploadResponse response) {
        if (TextUtils.isEmpty(message.getFilePath())) {
            return;
        }
        File file = new File(message.getFilePath());
        if (!file.exists()) {

        }
        if (cor.getConfig().IS_OPEN_OBS_STATUS != 0) {
            Handler handler = new Handler();
            ThreadManager.getPool().execute(() -> {
                String result = OBSUtils.uploadImFile(context, cor, message, result1 -> {
                }, msg -> {
                    Log.e(TAG, msg);
                    handler.post(() -> uploadFileToService(accessToken, loginUserId, toUserId, message, response, file));
                }, progress -> {
                    if (progress == 100) {
                        EventBus.getDefault().post(new EventUploadFileRate(message.getPacketId(), 100));
                        ChatMessageDao.getInstance().updateMessageUploadSchedule(loginUserId, toUserId, message.get_id(), 100);
                    } else {
                        EventBus.getDefault().post(new EventUploadFileRate(message.getPacketId(), progress));
                        ChatMessageDao.getInstance().updateMessageUploadSchedule(loginUserId, toUserId, message.get_id(), progress);
                    }
                });
                if (TextUtils.isEmpty(result)) {
                    return;
                }
                LogUtil.i("上传OBS成功");
                UploadingFileDao.getInstance().deleteUploadingFile(loginUserId, message.getPacketId());
                requestHandleMap.remove(message.getPacketId());
                String url = null;
                UploadFileResult uploadFileResult;
                try {
                    uploadFileResult = JSON.parseObject(result, UploadFileResult.class);
                } catch (Exception e) {
                    Reporter.post("上传文件响应解析失败，", e);
                    return;
                }
                if (uploadFileResult.getFailure() == 1) {//上传失败
                    if (response != null) {
                        handler.post(() -> response.onFailure(toUserId, message));
                    }
                    Reporter.post("上传文件失败，");
                    return;
                }
                //上传成功
                if (uploadFileResult.getResultCode() != Result.CODE_SUCCESS || uploadFileResult.getData() == null
                        || uploadFileResult.getSuccess() != uploadFileResult.getTotal()) {

                } else {
                    UploadFileResult.Data data = uploadFileResult.getData();
                    if (message.getType() == Constants.TYPE_IMAGE
                            || message.getType() == Constants.TYPE_LOCATION) {
                        // 位置消息的地图截图也是一样的当成图片处理，
                        url = getImagesUrl(data);
                        if (TextUtils.isEmpty(url)) {
                            //如果没有在 Images 组中 拿到图片路径 ，那可能传的不是.jpg .png 的图片 有可能是 .gif的或其他的， 后台 会 把这些图片 放到 其它 组中
                            //所以为空的时候 从 其他组中拿一下  防止 发送失败
                            url = getOthersUrl(data);
                        }
                    } else if (message.getType() == Constants.TYPE_VOICE) {
                        url = getAudiosUrl(data);
                    } else if (message.getType() == Constants.TYPE_VIDEO) {
                        url = getVideosUrl(data);
                    } else if (message.getType() == Constants.TYPE_FILE) {
                        if (TextUtils.isEmpty(url = getFilesUrl(data)))//如果获取到的文件名为空，就获取其他的
                            if (TextUtils.isEmpty(url = getVideosUrl(data)))
                                if (TextUtils.isEmpty(url = getAudiosUrl(data)))
                                    if (TextUtils.isEmpty(url = getImagesUrl(data)))
                                        url = getOthersUrl(data);
                    }
                }
                if (TextUtils.isEmpty(url)) {//返回成功，但是却获取不到对应的URL，服务器返回值异常<概率极小>
                    if (response != null) {
                        handler.post(() -> response.onFailure(toUserId, message));
                        ChatMessageDao.getInstance().updateMessageUploadState(loginUserId, toUserId, message.get_id(), false, url);
                    }
                } else {
                    // 记录本机上传，用于快速读取，
                    UploadCacheUtils.save(MyApplication.getInstance(), url, message.getFilePath());
                    ChatMessageDao.getInstance().updateMessageUploadState(loginUserId, toUserId, message.get_id(), true, url);
                    if (response != null) {
                        message.setContent(url);
                        message.setUpload(true);
                        handler.post(() -> response.onSuccess(toUserId, message));
                    }
                }
            });
        } else {
            uploadFileToService(accessToken, loginUserId, toUserId, message, response, file);
        }
    }


    private static void uploadFileToService(String accessToken, String loginUserId, String toUserId, ChatMessage message, ImFileUploadResponse response, File file) {
        LogUtil.i("未配置上传OBS 或上传出错 进入服务器上传");
        RequestParams params = new RequestParams();
        params.put("access_token", accessToken);
        params.put("userId", loginUserId);
        try {
            params.put("file1", file);
        } catch (FileNotFoundException e) {
            Reporter.post("文件<" + message.getFilePath() + ">找不到，", e);
        }

        // 目前文件有效期只用于聊天内产生的文件，其他文件如群共享文件、朋友圈上传的文件...有效期为永久 (-1 永久 default==7天)
        Friend friend = FriendDao.getInstance().getFriend(loginUserId, toUserId);
        if (friend != null) {
            params.put("validTime", String.valueOf(friend.getChatRecordTimeOut()));// 文件有效时长为用户的消息过期时长
        } else {
            params.put("validTime", "7");
        }

        // 将正在上传的消息存入本地
        UploadingFile mUploadingFile = new UploadingFile();
        mUploadingFile.setUserId(loginUserId);
        mUploadingFile.setToUserId(message.getToUserId());
        mUploadingFile.setMsgId(message.getPacketId());
        UploadingFileDao.getInstance().createUploadingFile(mUploadingFile);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestHandle requestHandle = client.post(CoreManager.requireConfig(MyApplication.getInstance()).UPLOAD_URL, params, new AsyncHttpResponseHandler() {
            int mCurrentBytesWritten = 0;

            public void onProgress(int bytesWritten, int totalSize) {
                super.onProgress(bytesWritten, totalSize);
                if (bytesWritten == totalSize) {
                    EventBus.getDefault().post(new EventUploadFileRate(message.getPacketId(), 100));
                    ChatMessageDao.getInstance().updateMessageUploadSchedule(loginUserId, toUserId, message.get_id(), 100);
                } else {
                    int mOnePercentage = totalSize / 100;
                    if (bytesWritten - mCurrentBytesWritten >= mOnePercentage) {
                        mCurrentBytesWritten = bytesWritten;
                        EventBus.getDefault().post(new EventUploadFileRate(message.getPacketId(), bytesWritten / mOnePercentage));
                        ChatMessageDao.getInstance().updateMessageUploadSchedule(loginUserId, toUserId, message.get_id(), bytesWritten / mOnePercentage);
                    }
                }
            }

            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {// 数据发送成功，并且有返回值<不是上传成功。。。>
                // 无论上传成功或失败，回调后删除"正在上传.."状态
                UploadingFileDao.getInstance().deleteUploadingFile(loginUserId, message.getPacketId());
                requestHandleMap.remove(message.getPacketId());

                String url = null;
                if (arg0 == 200) {
                    UploadFileResult result = null;
                    try {
                        String res = new String(arg2);
                        LogUtils.log("上传文件<" + message.getFilePath() + ">返回：" + res);
                        result = JSON.parseObject(res, UploadFileResult.class);
                    } catch (Exception e) {
                        Reporter.post("上传文件响应解析失败，", e);
                    }
                    if (result.getFailure() == 1) {//上传失败
                        if (response != null) {
                            response.onFailure(toUserId, message);
                        }
                        Reporter.post("上传文件失败，");
                        return;
                    }
                    //上传成功
                    if (result.getResultCode() != Result.CODE_SUCCESS || result.getData() == null
                            || result.getSuccess() != result.getTotal()) {

                    } else {
                        UploadFileResult.Data data = result.getData();
                        if (message.getType() == Constants.TYPE_IMAGE
                                || message.getType() == Constants.TYPE_LOCATION) {
                            // 位置消息的地图截图也是一样的当成图片处理，
                            url = getImagesUrl(data);
                        } else if (message.getType() == Constants.TYPE_VOICE) {
                            url = getAudiosUrl(data);
                        } else if (message.getType() == Constants.TYPE_VIDEO) {
                            url = getVideosUrl(data);
                        } else if (message.getType() == Constants.TYPE_FILE) {
                            if (TextUtils.isEmpty(url = getFilesUrl(data)))//如果获取到的文件名为空，就获取其他的
                                if (TextUtils.isEmpty(url = getVideosUrl(data)))
                                    if (TextUtils.isEmpty(url = getAudiosUrl(data)))
                                        if (TextUtils.isEmpty(url = getImagesUrl(data)))
                                            url = getOthersUrl(data);
                        }
                    }
                }

                if (TextUtils.isEmpty(url)) {//返回成功，但是却获取不到对应的URL，服务器返回值异常<概率极小>
                    if (response != null) {
                        response.onFailure(toUserId, message);
                        ChatMessageDao.getInstance().updateMessageUploadState(loginUserId, toUserId, message.get_id(), false, url);
                    }
                } else {
                    // 记录本机上传，用于快速读取，
                    UploadCacheUtils.save(MyApplication.getInstance(), url, message.getFilePath());
                    ChatMessageDao.getInstance().updateMessageUploadState(loginUserId, toUserId, message.get_id(), true, url);
                    if (response != null) {
                        message.setContent(url);
                        message.setUpload(true);
                        response.onSuccess(toUserId, message);
                    }
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                // 无论上传成功或失败，回调后删除"正在上传.."状态
                UploadingFileDao.getInstance().deleteUploadingFile(loginUserId, message.getPacketId());
                requestHandleMap.remove(message.getPacketId());

                Reporter.post("上传文件<" + message.getFilePath() + ">失败，", arg3);
                if (response != null) {
                    response.onFailure(toUserId, message);
                }
            }
        });
        requestHandleMap.put(message.getPacketId(), requestHandle);
    }


    public static void cancel(String msgId) {
        EventBus.getDefault().post(new EventUploadCancel(msgId));

        RequestHandle requestHandle = requestHandleMap.get(msgId);
        if (requestHandle != null) {
            requestHandle.cancel(true);
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
            if (data.getVideos().size() > 0) {
                return data.getVideos().get(0).getOriginalUrl();
            } else {
                return data.getOthers().get(0).getOriginalUrl();
            }
        } else {
            return "";
        }
    }

    private static String getImagesUrl(UploadFileResult.Data data) {
        TanX.Log("图片格式");
        if (data.getImages() != null && data.getImages().size() > 0) {
            if (data.getImages().size() > 0) {
                return data.getImages().get(0).getOriginalUrl();
            } else {
                return data.getOthers().get(0).getOriginalUrl();
            }
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

    public interface ImFileUploadResponse {
        void onSuccess(String toUserId, ChatMessage message);

        void onFailure(String toUserId, ChatMessage message);
    }
}
