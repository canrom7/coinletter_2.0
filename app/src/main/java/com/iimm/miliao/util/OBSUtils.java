package com.iimm.miliao.util;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.alibaba.fastjson.JSONObject;
import com.cjt2325.cameralibrary.util.LogUtil;
import com.obs.services.ObsClient;
import com.obs.services.ObsConfiguration;
import com.obs.services.exception.ObsException;
import com.obs.services.model.AccessControlList;
import com.obs.services.model.BucketStoragePolicyConfiguration;
import com.obs.services.model.CompleteMultipartUploadRequest;
import com.obs.services.model.CompleteMultipartUploadResult;
import com.obs.services.model.HeaderResponse;
import com.obs.services.model.InitiateMultipartUploadRequest;
import com.obs.services.model.InitiateMultipartUploadResult;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsBucket;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PartEtag;
import com.obs.services.model.ProgressListener;
import com.obs.services.model.ProgressStatus;
import com.obs.services.model.PutObjectRequest;
import com.obs.services.model.PutObjectResult;
import com.obs.services.model.StorageClassEnum;
import com.obs.services.model.UploadPartRequest;
import com.obs.services.model.UploadPartResult;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.bean.ConfigBean;
import com.iimm.miliao.bean.MonitorBean;
import com.iimm.miliao.bean.UploadFileResult;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.rsa.RSAUtils;
import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.common.COSACL;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.object.CompleteMultiUploadRequest;
import com.tencent.cos.xml.model.object.CompleteMultiUploadResult;
import com.tencent.cos.xml.model.object.GetObjectRequest;
import com.tencent.cos.xml.model.object.InitMultipartUploadRequest;
import com.tencent.cos.xml.model.object.InitMultipartUploadResult;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;
import com.tencent.qcloud.core.auth.SessionCredentialProvider;
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider;
import com.tencent.qcloud.core.http.HttpRequest;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

import static com.iimm.miliao.util.Constants.HUIWEI_OBS;
import static com.iimm.miliao.util.Constants.TENTENT_COS;

/**
 * OBS 上传工具
 */
public class OBSUtils {

    private static final String OBS_OS_APP_ID = "obs_app_id";
    public static long m1000 = 1000 * 1024 * 1024;//1000M   //TODO 将 分段上传阈值开到尽可能大，为保持稳定 只采用简单上传
    private double m1 = 1 * 1024 * 1024;
    private static final int TIME_OUT = 60000;  //连接超时时间
    private static final String TAG = "OBSUtils";
    private static ObsClient obsClient;
    private static OBSUtils obsUtils;
    private static CosXmlService cosXmlService;
    private Handler mHandler;
    public static double m5 = 5;
    private static final String OBS_AK = "obs_ak";
    private static final String OBS_SK = "obs_sk";
    private static final String OBS_BUCKET_NAME = "obs_bucket_name";
    private static final String OBS_END_POINT = "obs_end_point";
    private static final String OBS_LOCATION = "obs_location";


    public static OBSUtils getInstance() {
        synchronized (OBSUtils.class) {
            if (obsUtils != null) {
                return obsUtils;
            } else {
                return obsUtils = new OBSUtils();
            }
        }
    }


    public static void saveObsInfo(Context ctx, ConfigBean configBean) {
        if (configBean.getIsOpenOSStatus() != 0) {
            if (configBean.getAccessKeyId() != null) {
                PreferenceUtils.putString(ctx, OBS_AK, configBean.getAccessKeyId());
            }
            if (configBean.getAccessSecretKey() != null) {
                PreferenceUtils.putString(ctx, OBS_SK, configBean.getAccessSecretKey());
            }
            if (configBean.getBucketName() != null) {
                PreferenceUtils.putString(ctx, OBS_BUCKET_NAME, configBean.getBucketName());
            }
            if (configBean.getEndPoint() != null) {
                PreferenceUtils.putString(ctx, OBS_END_POINT, configBean.getEndPoint());
            }
            if (configBean.getLocation() != null) {
                PreferenceUtils.putString(ctx, OBS_LOCATION, configBean.getLocation());
            }
            if (configBean.getOsAppId() != null) {
                PreferenceUtils.putString(ctx, OBS_OS_APP_ID, configBean.getOsAppId());
            }
        }
    }


    /**
     * 获取ak
     *
     * @param ctx
     * @param cor
     * @return
     */
    public static String getObsAk(Context ctx, CoreManager cor) {
        if (TextUtils.isEmpty(cor.getConfig().ACCESS_KEY_ID)) {
            String oAk = PreferenceUtils.getString(ctx, OBS_AK);
            try {
                // LogUtil.i("AK:" + RSAUtils.decryptPublicWithBase64(oAk));
                return RSAUtils.decryptPublicWithBase64(oAk);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        } else {
            String oAk = cor.getConfig().ACCESS_KEY_ID;
            try {
                LogUtil.i("OAK:" + oAk);
                //LogUtil.i("AK:" + RSAUtils.decryptPublicWithBase64(oAk));
                return RSAUtils.decryptPublicWithBase64(oAk);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
    }


    /**
     * 获取sk
     *
     * @param ctx
     * @param cor
     * @return
     */
    public static String getObsSk(Context ctx, CoreManager cor) {
        if (TextUtils.isEmpty(cor.getConfig().ACCESS_SECRET_KEY)) {
            String osk = PreferenceUtils.getString(ctx, OBS_SK);
            try {
                return RSAUtils.decryptPublicWithBase64(osk);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        } else {
            String osk = cor.getConfig().ACCESS_SECRET_KEY;
            try {
                LogUtil.i("OAK:" + osk);
                return RSAUtils.decryptPublicWithBase64(osk);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    /**
     * 获取BucketName
     *
     * @param ctx
     * @param cor
     * @return
     */
    public static String getObsBucketName(Context ctx, CoreManager cor) {
        if (TextUtils.isEmpty(cor.getConfig().OBS_BUCKET_NAME)) {
            return PreferenceUtils.getString(ctx, OBS_BUCKET_NAME);
        } else {
            return cor.getConfig().OBS_BUCKET_NAME;
        }
    }


    /**
     * 获取BucketName
     *
     * @param ctx
     * @param cor
     * @return
     */
    public static String getCosBucketName(Context ctx, CoreManager cor) {
        if (TextUtils.isEmpty(cor.getConfig().OBS_BUCKET_NAME)) {
            return PreferenceUtils.getString(ctx, OBS_BUCKET_NAME) + "-" + getCosAppId(ctx, cor);
        } else {
            return cor.getConfig().OBS_BUCKET_NAME + "-" + getCosAppId(ctx, cor);
        }
    }

    private static String getCosAppId(Context ctx, CoreManager cor) {
        if (TextUtils.isEmpty(cor.getConfig().OBS_OS_APP_ID)) {
            String osk = PreferenceUtils.getString(ctx, OBS_OS_APP_ID);
            try {
                return osk;
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        } else {
            String osk = cor.getConfig().OBS_OS_APP_ID;
            try {
                LogUtil.i("OAK:" + osk);
                LogUtil.i("SK:" + osk);
                return osk;
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
    }


    /**
     * 获取ObsEndPoint
     *
     * @param ctx
     * @param cor
     * @return
     */
    public static String getObsEndPoint(Context ctx, CoreManager cor) {
        if (TextUtils.isEmpty(cor.getConfig().OBS_END_POINT)) {
            return PreferenceUtils.getString(ctx, OBS_END_POINT, "");
        } else {
            return cor.getConfig().OBS_END_POINT;
        }
    }


    /**
     * 获取ObsLocation
     *
     * @param ctx
     * @param cor
     * @return
     */
    public static String getObsLocation(Context ctx, CoreManager cor) {
        if (TextUtils.isEmpty(cor.getConfig().OBS_LOCATION)) {
            return PreferenceUtils.getString(ctx, OBS_LOCATION);
        } else {
            return cor.getConfig().OBS_LOCATION;
        }
    }


    /**
     * 使用前在主线程初始化
     *
     * @param context     上下文
     * @param coreManager 核管理
     */
    public static void init(Context context, CoreManager coreManager, Handler handler) {
        if (coreManager.getConfig().IS_OPEN_OBS_STATUS == TENTENT_COS) { //腾讯COS
            String region = getObsLocation(context, coreManager);
            if (TextUtils.isEmpty(region)) {
                coreManager.getConfig().IS_OPEN_OBS_STATUS = 0;
                return;
            }
            //创建 CosXmlServiceConfig 对象，根据需要修改默认的配置参数
            CosXmlServiceConfig serviceConfig = getServiceConfigForCos(region);
            QCloudCredentialProvider qCloudCredentialProvider = authorizeCosKey(context, coreManager);
            if (qCloudCredentialProvider == null) {
                coreManager.getConfig().IS_OPEN_OBS_STATUS = 0;
                return;
            }
            OBSUtils.getInstance().init(serviceConfig, qCloudCredentialProvider, handler);
            LogUtil.i("初始化COS");
        } else if (coreManager.getConfig().IS_OPEN_OBS_STATUS == HUIWEI_OBS) {
            String endPoint = getObsEndPoint(context, coreManager);
            String ak = getObsAk(context, coreManager);
            String sk = getObsSk(context, coreManager);
            if (TextUtils.isEmpty(endPoint)
                    || TextUtils.isEmpty(ak)
                    || TextUtils.isEmpty(sk)) {
                coreManager.getConfig().IS_OPEN_OBS_STATUS = 0;
                return;
            }
            ObsConfiguration configuration = new ObsConfiguration();
            configuration.setEndPoint(endPoint);
            OBSUtils.getInstance().init(ak,
                    sk,
                    null, configuration, handler);
            LogUtil.i("初始化OBS");

        }
    }


    /**
     * COS 配置
     *
     * @param region
     * @return
     */
    private static CosXmlServiceConfig getServiceConfigForCos(String region) {
        return new CosXmlServiceConfig.Builder()
                .setRegion(region)
                .isHttps(false) // 使用 https 请求, 默认 http 请求
                .setDebuggable(true)
                .builder();
    }


    /**
     * 授权key
     *
     * @param context
     * @param coreManager
     * @return
     */
    private static QCloudCredentialProvider authorizeCosKey(Context context, CoreManager coreManager) {
//        QCloudCredentialProvider qCloudCredentialProvider = temporaryAuthorization(context, coreManager);//临时授权
        QCloudCredentialProvider qCloudCredentialProvider = permanentAuthorization(context, coreManager);//永久授权
        return qCloudCredentialProvider;
    }


    /**
     * 永久授权
     *
     * @param context
     * @param coreManager
     */
    private static QCloudCredentialProvider permanentAuthorization(Context context, CoreManager coreManager) {
        String secretId = getSecretIdByCos(context, coreManager); //永久密钥 secretId
        String secretKey = getSecretKeyByCos(context, coreManager); //永久密钥 secretKey
        Log.i(TAG, secretId);
        Log.i(TAG, secretKey);
        Log.i(TAG, getCosAppId(context, coreManager));
        if (TextUtils.isEmpty(secretId) || TextUtils.isEmpty(secretKey)) {
            return null;
        }
        QCloudCredentialProvider credentialProvider = new ShortTimeCredentialProvider(secretId,
                secretKey, 30000);
        return credentialProvider;
    }

    private static String getSecretKeyByCos(Context ctx, CoreManager cor) {
        return getObsSk(ctx, cor);
    }

    private static String getSecretIdByCos(Context ctx, CoreManager cor) {
        return getObsAk(ctx, cor);
    }

    /**
     * 临时授权
     *
     * @param context
     * @param coreManager
     */
    private static QCloudCredentialProvider temporaryAuthorization(Context context, CoreManager coreManager) {
        try {
            URL url = null; // 后台授权服务的 url 地址
            url = new URL(coreManager.getConfig().GET_COS_KEY);
            QCloudCredentialProvider credentialProvider = new SessionCredentialProvider(new HttpRequest.Builder<String>()
                    .url(url)
                    .method("GET")
                    .build());
            return credentialProvider;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取COS 访问区域
     *
     * @return
     */
    private static String getRegionByCOS(Context ctx, CoreManager cor) {
        if (TextUtils.isEmpty(cor.getConfig().OBS_END_POINT)) {
            return PreferenceUtils.getString(ctx, OBS_END_POINT, "");
        } else {
            return cor.getConfig().OBS_END_POINT;
        }
    }

    /**
     * 上传IM 文件
     *
     * @param context
     * @param cor
     * @param message
     * @param obsSuccessListener
     * @param obsErrorListener
     * @param uploadListener
     * @return
     */
    public static String uploadImFile(Context context, CoreManager cor, ChatMessage message, ObsSuccessListener<List<?>> obsSuccessListener, ObsErrorListener obsErrorListener, UploadListener uploadListener) {
        //File file = new File(message.getFilePath());
        List<String> pathList = new ArrayList<>();
        pathList.add(message.getFilePath());
        String result = uploadFileList(pathList, obsSuccessListener, obsErrorListener, uploadListener, context, cor);
        return result;
    }


    /**
     * 上传文件
     *
     * @param context
     * @param coreManager
     * @param file
     * @param uploadListener
     * @return objectUrl  文件访问地址
     */
    public static String upLoadFile(Context context, CoreManager coreManager, File file, UploadListener uploadListener, ObsErrorListener obsErrorListener) {
        if (coreManager.getConfig().IS_OPEN_OBS_STATUS == TENTENT_COS) {
            if (file.length() > m1000) {
                CompleteMultiUploadResult objectWithMultipartUploadByCos = OBSUtils.getInstance().createObjectWithMultipartUploadByCos(context, coreManager, file, uploadListener);
                if (objectWithMultipartUploadByCos != null) {
                    return transformObjectUrl(objectWithMultipartUploadByCos.accessUrl);
                } else {
                    return "";
                }
            } else {
                com.tencent.cos.xml.model.object.PutObjectResult objectByCos = OBSUtils.getInstance().createObjectByCos(context, coreManager, file, uploadListener, obsErrorListener);
                if (objectByCos != null) {
                    return transformObjectUrl(objectByCos.accessUrl);
                } else {
                    return "";
                }
            }
        } else {
            if (file.length() > m1000) {
                CompleteMultipartUploadResult objectWithMultipartUpload = OBSUtils.getInstance().createObjectWithMultipartUpload(context, coreManager, file, uploadListener);
                if (objectWithMultipartUpload != null) {
                    return transformObjectUrl(objectWithMultipartUpload.getObjectUrl());
                } else {
                    return "";
                }
            } else {
                PutObjectResult object = OBSUtils.getInstance().createObject(context, coreManager, file, uploadListener);
                if (object != null) {
                    return transformObjectUrl(object.getObjectUrl());
                } else {
                    return "";
                }
            }
        }
    }

    public static void release() {
        OBSUtils.getInstance().releaseObs(msg -> {
        });
    }

    private void releaseObs(ObsErrorListener obsErrorListener) {
        if (obsClient != null) {
            try {
                obsClient.close();
            } catch (IOException e) {
                e.printStackTrace();
                if (obsErrorListener != null) {
                    obsErrorListener.error("ObsClient 关闭异常");
                }
            } finally {
                obsClient = null;
                obsUtils = null;
                mHandler = null;
            }
        } else {
            obsUtils = null;
            mHandler = null;
        }
    }


    /**
     * 初始化COS
     *
     * @param serviceConfig
     * @param qCloudCredentialProvider
     * @param handler
     */
    private void init(CosXmlServiceConfig serviceConfig, QCloudCredentialProvider qCloudCredentialProvider, Handler handler) {
        synchronized (OBSUtils.class) {
            if (mHandler == null) {
                mHandler = handler;
            }
            if (cosXmlService == null) {
                cosXmlService = new CosXmlService(MyApplication.getContext(), serviceConfig, qCloudCredentialProvider);
            }
        }
    }

    /**
     * 初始化 ObsClient
     *
     * @param accessKey     ak
     * @param secretKey     sk
     * @param securityToken 访问令牌
     * @param config        配置
     * @param handler
     */
    public void init(String accessKey, String secretKey, String securityToken, ObsConfiguration config, Handler handler) {
        synchronized (OBSUtils.class) {
            if (mHandler == null) {
                mHandler = handler;
            }
            if (obsClient == null) {
                obsClient = new ObsClient(accessKey, secretKey, securityToken, config);
            } else {
                obsClient.refresh(accessKey, secretKey, securityToken);
            }
        }
    }

    /**
     * 创建 桶
     *
     * @param bucketName 桶 名称
     * @param location   桶 区域
     *                   东北-大连	cn-northeast-1	az1.cnnortheast1	可用区1
     *                   华北-北京四	cn-north-4	cn-north-4a	可用区1
     *                   cn-north-4b	可用区2
     *                   cn-north-4c	可用区3
     *                   华北-北京一	cn-north-1	cn-north-1a	可用区1
     *                   cn-north-1b	可用区2
     *                   cn-north-1c	可用区3
     * @param listener   obsClient 为空 的监听
     * @return 桶信息
     */
    public void createBucket(String bucketName, String location, ObsSuccessListener<ObsBucket> successListener, @Nullable ObsErrorListener listener) {
        if (requiredObsClient(listener)) {
            ThreadManager.getPool().execute(() -> {
                try {
                    ObsBucket bucket = obsClient.createBucket(bucketName, location);
                    if (successListener != null) {
                        mHandler.post(() -> successListener.success(bucket));
                    }
                } catch (ObsException e) {
                    e.printStackTrace();
                    if (listener != null) {
                        mHandler.post(() -> listener.error(e.getErrorMessage()));
                    }
                }

            });
        }
    }


    /**
     * 设置  桶 的存储类型
     *
     * @param bucketName       桶名称
     * @param storageClassEnum 桶存储类型
     * @param listener         obsClient 为空 的监听
     * @return
     */
    public void setBucketStrogeInfo(String bucketName, StorageClassEnum storageClassEnum, ObsSuccessListener<HeaderResponse> successListener, @Nullable ObsErrorListener listener) {
        if (requiredObsClient(listener)) {
            ThreadManager.getPool().execute(() -> {
                try {
                    BucketStoragePolicyConfiguration bucketStorage = new BucketStoragePolicyConfiguration();
                    bucketStorage.setBucketStorageClass(storageClassEnum);
                    HeaderResponse response = obsClient.setBucketStoragePolicy(bucketName, bucketStorage);
                    if (successListener != null) {
                        mHandler.post(() -> successListener.success(response));
                    }
                } catch (ObsException e) {
                    e.printStackTrace();
                    if (listener != null) {
                        mHandler.post(() -> listener.error(e.getErrorMessage()));
                    }
                }
            });
        }
    }

    /**
     * 创建 桶
     *
     * @param bucketName 桶 名称
     * @param listener   obsClient 为空 的监听
     * @return 桶信息
     */
    public void createBucket(String bucketName, ObsSuccessListener<ObsBucket> successListener, @Nullable ObsErrorListener listener) {
        if (requiredObsClient(listener)) {
            ThreadManager.getPool().execute(() -> {
                try {
                    ObsBucket bucket = obsClient.createBucket(bucketName);
                    mHandler.post(() -> successListener.success(bucket));
                } catch (ObsException e) {
                    e.printStackTrace();
                    if (listener != null) {
                        mHandler.post(() -> listener.error(e.getErrorMessage()));
                    }
                }
            });
        }
    }


    /**
     * 上传对象
     *
     * @param file            要上传的文件
     * @param bucketName      要上传的桶
     * @param successListener 成功 回调
     * @param listener        错误回调
     */
    public void createObject(File file, String bucketName, ObsSuccessListener<PutObjectResult> successListener, @Nullable ObsErrorListener listener) {
        createObjectWithProgress(file, null, bucketName, null, null, successListener, listener);
    }


    /**
     * 上传对象
     *
     * @param file            要上传的文件
     * @param bucketName      要上传的桶
     * @param successListener 成功 回调
     * @param listener        错误回调
     */
    public void createObject(File file, String bucketName, ObjectMetadata objectMetadata, ObsSuccessListener<PutObjectResult> successListener, @Nullable ObsErrorListener listener) {
        createObjectWithProgress(file, null, bucketName, objectMetadata, null, successListener, listener);
    }


    /**
     * 上传对象
     *
     * @param file            要上传的文件
     * @param bucketName      要上传的桶
     * @param successListener 成功 回调
     * @param listener        错误回调
     */
    public void createObject(File file, String objectKey, String bucketName, ObjectMetadata objectMetadata, ObsSuccessListener<PutObjectResult> successListener, @Nullable ObsErrorListener listener) {
        createObjectWithProgress(file, objectKey, bucketName, objectMetadata, null, successListener, listener);
    }


    /**
     * 上传对象
     *
     * @param file            要上传的文件
     * @param bucketName      要上传的桶
     * @param successListener 成功 回调
     * @param listener        错误回调
     */
    public void createObjectWithProgress(File file, String objectKey, String bucketName, ObjectMetadata objectMetadata, UploadListener uploadListener, ObsSuccessListener<PutObjectResult> successListener, @Nullable ObsErrorListener listener) {
        try {
            if (file != null) {
                if (requiredObsClient(listener)) {
                    ThreadManager.getPool().execute(() -> {
                        try {
                            long time = System.currentTimeMillis();
                            double size = file.length() / m1;
                            String nameKey;
                            if (TextUtils.isEmpty(objectKey)) {
                                nameKey = getNameKey(file);
                            } else {
                                nameKey = objectKey;
                            }
                            PutObjectRequest request = new PutObjectRequest(bucketName, nameKey);
                            if (objectMetadata != null) {
                                request.setMetadata(objectMetadata);
                            }
                            request.setFile(file);
                            request.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
                            // 每上传512kb数据反馈上传进度
//                            request.setProgressListener(status -> {
//                                // 获取上传平均速率
//                                Log.i("PutObject", "AverageSpeed:" + status.getAverageSpeed());
//                                // 获取上传进度百分比
//                                Log.i("PutObject", "TransferPercentage:" + status.getTransferPercentage());
////                                if (null != uploadListener) {
////                                    mHandler.post(() -> uploadListener.progress(status.getTransferPercentage()));
////                                }
//                            });
                            request.setProgressInterval(512 * 1024L);
                            PutObjectResult putObjectResult = obsClient.putObject(request);
                            if (successListener != null) {
                                if (mHandler != null) {
                                    mHandler.post(() -> successListener.success(putObjectResult));
                                }
                            }
                            LogUtils.log(TAG, "上传头像成功：" + transformObjectUrl(putObjectResult.getObjectUrl()));


                            if (size > m5 && Constants.SUPPORT_OBS_LOG) {
                                String sOBSPath = MyApplication.getContext().getExternalCacheDir().getPath() + File.separator + "obsLog/" + MyApplication.getLoginUserId() + "_log.txt";
                                if (!TextUtils.isEmpty(sOBSPath)) {
                                    boolean oooo = FileUtils.createOrExistsFile(sOBSPath);
                                    if (oooo) {
                                        long sTime = System.currentTimeMillis();
                                        String ip = "用户IP:" + HttpUtil.getIPAddress(MyApplication.getContext()) + "\n";
                                        String netSpeed = "用户网速:" + getNetSpeed(MyApplication.getContext().getApplicationInfo().uid) + "\n";
                                        String obsPath = "上传的OBS文件路径:" + transformObjectUrl(putObjectResult.getObjectUrl()) + "\n";
                                        String mSize = "上传文件大小:" + size + "M\n";
                                        String starttime = "上传文件开始时间:" + time + "\n";
                                        String endtime = "上传文件结束时间:" + sTime + "\n";
                                        String timeConsuming = "上传耗时:" + (sTime - time) + "毫秒";
                                        boolean aaaa = FileIOUtils.writeFileFromString(sOBSPath, ip + netSpeed + obsPath + mSize + starttime + endtime + timeConsuming);
                                        if (aaaa) {
                                            EventBus.getDefault().post(new MonitorBean(sOBSPath));
                                        }
                                    }
                                }
                            }
                        } catch (ObsException e) {
                            e.printStackTrace();
                            if (listener != null) {
                                if (mHandler != null) {
                                    mHandler.post(() -> listener.error(e.getErrorMessage()));
                                }
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mHandler != null) {
                mHandler.post(() -> {
                    if (listener != null) {
                        listener.error(e.getMessage());
                    }
                });
            }
        }

    }


    /**
     * 分段上传文件
     *
     * @param file                      文件
     * @param bucketName                桶名
     * @param objectKey                 文件名
     * @param objectMetadata            类型信息
     * @param uploadListener            上传进度监听
     * @param obsSuccessListener        上传成功监听
     * @param errorListener             错误监听
     * @param whetherToUploadInParallel 是否并行上传
     */
    public void createObjectWithMultipartUpload(@NotNull File file, @NotNull String bucketName, String objectKey, ObjectMetadata objectMetadata, MultipartUploadProgressListener uploadListener, ObsSuccessListener<CompleteMultipartUploadResult> obsSuccessListener, ObsErrorListener errorListener, boolean whetherToUploadInParallel) {
        try {
            if (file != null) {
                if (requiredObsClient(errorListener)) {
                    ThreadManager.getPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            String nameKey;
                            if (TextUtils.isEmpty(objectKey)) {
                                nameKey = getNameKey(file);
                            } else {
                                nameKey = objectKey;
                            }
                            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, nameKey);
                            if (objectMetadata != null) {
                                request.setMetadata(objectMetadata);
                            }
                            InitiateMultipartUploadResult result = obsClient.initiateMultipartUpload(request);
                            String uploadId = result.getUploadId();
                            if (whetherToUploadInParallel) {
                                //是并行上传
                                // 初始化线程池
                                ExecutorService executorService = Executors.newFixedThreadPool(20);
                                // 初始化分段上传任务
                                long fileSize = file.length();
                                // 计算需要上传的段数
                                long partCount = fileSize % m1000 == 0 ? fileSize / m1000 : fileSize / m1000 + 1;
                                final List<PartEtag> partEtags = Collections.synchronizedList(new ArrayList<PartEtag>());
                                // 执行并发上传段
                                for (int i = 0; i < partCount; i++) {
                                    // 分段在文件中的起始位置
                                    final long offset = i * m1000;
                                    // 分段大小
                                    final long currPartSize = (i + 1 == partCount) ? fileSize - offset : m1000;
                                    // 分段号
                                    final int partNumber = i + 1;
                                    executorService.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            UploadPartRequest uploadPartRequest = new UploadPartRequest();
                                            uploadPartRequest.setBucketName(bucketName);
                                            uploadPartRequest.setObjectKey(objectKey);
                                            uploadPartRequest.setUploadId(uploadId);
                                            uploadPartRequest.setFile(file);
                                            uploadPartRequest.setPartSize(currPartSize);
                                            uploadPartRequest.setOffset(offset);
                                            uploadPartRequest.setPartNumber(partNumber);
                                            uploadPartRequest.setProgressListener(new ProgressListener() {
                                                @Override
                                                public void progressChanged(ProgressStatus status) {
                                                    int partNumber = uploadPartRequest.getPartNumber();
                                                    int currentProgress = status.getTransferPercentage();
                                                    if (uploadListener != null) {
                                                        mHandler.post(() -> uploadListener.progress(partNumber, currentProgress));
                                                    }
                                                }
                                            });
                                            UploadPartResult uploadPartResult;
                                            try {
                                                uploadPartResult = obsClient.uploadPart(uploadPartRequest);
                                                Log.i("UploadPart", "Part#" + partNumber + " done\n");
                                                partEtags.add(new PartEtag(uploadPartResult.getEtag(), uploadPartResult.getPartNumber()));
                                            } catch (ObsException e) {
                                                Log.e("UploadPart", e.getMessage(), e);
                                            }
                                        }
                                    });
                                }
                                // 等待上传完成
                                executorService.shutdown();
                                while (!executorService.isTerminated()) {
                                    try {
                                        executorService.awaitTermination(5, TimeUnit.SECONDS);
                                    } catch (InterruptedException e) {
                                        Log.e("UploadPart", e.getMessage(), e);
                                    }
                                }
                                // 合并段
                                CompleteMultipartUploadRequest completeMultipartUploadRequest =
                                        new CompleteMultipartUploadRequest(bucketName, objectKey, uploadId, partEtags);
                                CompleteMultipartUploadResult uploadResult = obsClient.completeMultipartUpload(completeMultipartUploadRequest);
                                if (obsSuccessListener != null) {
                                    mHandler.post(() -> obsSuccessListener.success(uploadResult));
                                }
                            } else {
                                //串行上传
                                List<PartEtag> partEtags = new ArrayList<PartEtag>();

                                long numberOfStages = file.length() / m1000; //5M 一段
                                if (file.length() % m1000 > 0) {
                                    numberOfStages = numberOfStages + 1;
                                }

                                for (int i = 1; i <= numberOfStages; i++) {
                                    // 上传第一段
                                    UploadPartRequest uploadPartRequest = new UploadPartRequest(bucketName, nameKey);
                                    uploadPartRequest.setProgressListener(new ProgressListener() {
                                        @Override
                                        public void progressChanged(ProgressStatus status) {
                                            int partNumber = uploadPartRequest.getPartNumber();
                                            int currentProgress = status.getTransferPercentage();
                                            if (uploadListener != null) {
                                                mHandler.post(() -> uploadListener.progress(partNumber, currentProgress));
                                            }
                                        }
                                    });
                                    // 设置Upload ID
                                    uploadPartRequest.setUploadId(uploadId);
                                    // 设置分段号，范围是1~10000，
                                    uploadPartRequest.setPartNumber(i);
                                    // 设置将要上传的大文件，其中localfile为待上传的本地文件路径，需要指定到具体的文件名
                                    uploadPartRequest.setFile(file);
                                    // 设置段偏移量
                                    long offset = (i - 1) * m1000;
                                    final long currPartSize = (i == numberOfStages) ? file.length() - offset : m1000;
                                    uploadPartRequest.setOffset(currPartSize);
                                    // 设置分段大小
                                    uploadPartRequest.setPartSize(m1000);
                                    UploadPartResult uploadPartResult = obsClient.uploadPart(uploadPartRequest);
                                    partEtags.add(new PartEtag(uploadPartResult.getEtag(), uploadPartResult.getPartNumber()));
                                }
                                CompleteMultipartUploadRequest uploadRequest = new CompleteMultipartUploadRequest(bucketName, nameKey, uploadId, partEtags);
                                CompleteMultipartUploadResult uploadResult = obsClient.completeMultipartUpload(uploadRequest);
                                if (obsSuccessListener != null) {
                                    mHandler.post(() -> obsSuccessListener.success(uploadResult));
                                }
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorListener.error(
                    e.getMessage()
            );
        }

    }


    /**
     * 获取文件夹名称
     *
     * @param file
     * @return
     */
    public static String getNameKey(File file) {
        String path = file.getAbsolutePath();
        if (TextUtils.isEmpty(path)) {
            return UUID.randomUUID().toString();
        }
       /* int indexOf = path.lastIndexOf("/");
        if (indexOf + 1 == path.length()) {
            return UUID.randomUUID().toString();
        } else {
            return System.currentTimeMillis() + "_" + path.substring(indexOf + 1);
        }*/

        int one = path.lastIndexOf(".");
        String namePath = path.substring((one));
        if (TextUtils.isEmpty(namePath)) {
            return UUID.randomUUID().toString();
        } else {
            return System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + namePath;
        }
    }


    /**
     * 获取文件名称
     *
     * @param path
     * @return
     */
    public static String getNameKey(String path) {
        if (TextUtils.isEmpty(path)) {
            return UUID.randomUUID().toString();
        }
       /* int indexOf = path.lastIndexOf("/");
        if (indexOf + 1 == path.length()) {
            return UUID.randomUUID().toString();
        } else {
            return System.currentTimeMillis() + "_" + path.substring(indexOf + 1);
        }*/
        int one = path.lastIndexOf(".");
        String namePath = path.substring((one));
        if (TextUtils.isEmpty(namePath)) {
            return UUID.randomUUID().toString();
        } else {
            return System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + namePath;
        }
    }


    /**
     * 检测ObsClient
     *
     * @param listener 错误回调
     */
    private boolean requiredObsClient(ObsErrorListener listener) {
        if (obsClient == null) {
            obsClient = rebuildObsClient();
            if (obsClient == null) {
                if (listener != null) {
                    listener.error("obsClient 为空");
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }


    /**
     * 根据实现的 Config  重新创建  obs
     *
     * @return
     */
    private static ObsClient rebuildObsClient() {
        if (mSetObsConfigInfo != null) {
            obsClient = new ObsClient(mSetObsConfigInfo.getAk(),
                    mSetObsConfigInfo.getSk(),
                    mSetObsConfigInfo.getSecurityToken(),
                    mSetObsConfigInfo.getObsConfig());
        }
        return obsClient;
    }

    private static SetObsConfigInfo mSetObsConfigInfo;

    public static void setObsConfigInfo(SetObsConfigInfo setObsConfigInfo) {
        mSetObsConfigInfo = setObsConfigInfo;
    }


    /**
     * 上传文件列表
     *
     * @param list
     * @param obsSuccessListener
     * @param obsErrorListener
     * @param uploadListener
     * @param context
     * @param coreManager
     * @return
     */
    public static String uploadFileList(List<String> list, ObsSuccessListener<List<?>> obsSuccessListener, ObsErrorListener obsErrorListener, UploadListener uploadListener, Context context, CoreManager coreManager) {
        return OBSUtils.getInstance().createListObject(list, obsSuccessListener, obsErrorListener, uploadListener, context, coreManager);
    }

    /**
     * 上传文件列表
     *
     * @param list
     * @param obsSuccessListener
     * @param obsErrorListener
     * @param context
     * @param coreManager
     * @return
     */
    public static String uploadFileList(List<String> list, ObsSuccessListener<List<?>> obsSuccessListener, ObsErrorListener obsErrorListener, Context context, CoreManager coreManager) {
        return OBSUtils.getInstance().createListObject(list, obsSuccessListener, obsErrorListener, null, context, coreManager);
    }


    /**
     * 文件 列表 串行 上传
     *
     * @param photoList
     * @param obsSuccessListener
     * @param obsErrorListener
     * @param uploadListener
     * @param context
     * @param coreManager
     * @return
     */
    private String createListObject(List<String> photoList, ObsSuccessListener<List<?>> obsSuccessListener, ObsErrorListener obsErrorListener, UploadListener uploadListener, Context context, CoreManager coreManager) {
        if (coreManager.getConfig().IS_OPEN_OBS_STATUS == TENTENT_COS) {
            if (requiredCosClient(obsErrorListener)) {
                Log.d(TAG, "上传到COS");
                return handlerUploadFileList(photoList, obsSuccessListener, obsErrorListener, uploadListener, context, coreManager, false);
            } else {
                return "";
            }
        } else if (coreManager.getConfig().IS_OPEN_OBS_STATUS == HUIWEI_OBS) {
            if (requiredObsClient(obsErrorListener)) {
                return handlerUploadFileList(photoList, obsSuccessListener, obsErrorListener, uploadListener, context, coreManager, true);
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @NotNull
    private String handlerUploadFileList(List<String> photoList, ObsSuccessListener<List<?>> obsSuccessListener, ObsErrorListener obsErrorListener, UploadListener uploadListener, Context context, CoreManager coreManager, boolean isHuaWei) {
        try {
            String path = context.getExternalCacheDir().getPath() + File.separator + "obsLog/" + MyApplication.getLoginUserId() + "_log.txt";

            List<Object> results = new ArrayList<>();
            UploadFileResult.Data data = new UploadFileResult.Data();
            List<UploadFileResult.Sources> images = new ArrayList<>();
            List<UploadFileResult.Sources> videos = new ArrayList<>();
            List<UploadFileResult.Sources> audios = new ArrayList<>();
            List<UploadFileResult.Sources> others = new ArrayList<>();
            int temp = 0;
            for (int i = 0; i < photoList.size(); i++) {
                //Tencent
                com.tencent.cos.xml.model.object.PutObjectResult result = null;
                CompleteMultiUploadResult uploadResult = null;
                //HuaWei
                PutObjectResult result_hua_wei = null;
                CompleteMultipartUploadResult uploadResult_hua_wei = null;
                File file = new File(photoList.get(i));
                String mime = getMimeType(photoList.get(i));
                double size = file.length() / m1;
                long time = System.currentTimeMillis();

                if (file.length() > m1000) {
                    //大于10兆
                    if (isHuaWei) {
                        uploadResult_hua_wei = createObjectWithMultipartUpload(context, coreManager, file, uploadListener);
                        if (uploadResult_hua_wei != null) {
                            results.add(uploadResult_hua_wei);
                        }
                    } else {
                        uploadResult = createObjectWithMultipartUploadByCos(context, coreManager, file, uploadListener);
                        if (!TextUtils.isEmpty(transformObjectUrl(uploadResult.accessUrl))) {
                            Log.i(TAG, "上传完成：" + transformObjectUrl(uploadResult.accessUrl));
                        }
                        if (uploadResult != null) {
                            results.add(uploadResult);
                        }
                    }
                } else {
                    if (isHuaWei) {
                        result_hua_wei = createObject(context, coreManager, file, uploadListener);
                        if (result_hua_wei != null) {
                            results.add(result_hua_wei);
                        }
                    } else {
                        result = createObjectByCos(context, coreManager, file, uploadListener, obsErrorListener);
                        if (!TextUtils.isEmpty(transformObjectUrl(result.accessUrl))) {
                            Log.i(TAG, "上传完成：" + transformObjectUrl(result.accessUrl));
                        }
                        if (result != null) {
                            results.add(result);
                        }
                    }
                }
                if (result != null || result_hua_wei != null) {
                    if (isHuaWei) {
                        setFileResultSources(images, audios, videos, others, result_hua_wei.getObjectKey(), transformObjectUrl(result_hua_wei.getObjectUrl()), mime, path, size, context, time);
                    } else {
                        //TODO 需要更改  一个是key  一个url 腾讯没有返回
                        setFileResultSources(images, audios, videos, others, getNameKey(transformObjectUrl(result.accessUrl)), transformObjectUrl(result.accessUrl), mime, path, size, context, time);
                    }
                    temp++;
                } else if (uploadResult != null || uploadResult_hua_wei != null) {
                    if (isHuaWei) {
                        setFileResultSources(images, audios, videos, others, uploadResult_hua_wei.getObjectKey(), transformObjectUrl(uploadResult_hua_wei.getObjectUrl()), mime, path, size, context, time);
                    } else {
                        //TODO 需要更改  一个是key  一个url  腾讯没有返回
                        setFileResultSources(images, audios, videos, others, uploadResult.completeMultipartUpload.key, transformObjectUrl(uploadResult.accessUrl), mime, path, size, context, time);
                    }
                    temp++;
                } else {
                    continue;
                }
            }
            data.setImages(images);
            data.setAudios(audios);
            data.setVideos(videos);
            data.setOthers(others);
            // data.setFiles(others);
            UploadFileResult uploadFileResult = new UploadFileResult();
            uploadFileResult.setSuccess(temp);
            uploadFileResult.setFailure(0);
            uploadFileResult.setTotal(photoList.size());
            uploadFileResult.setResultCode(1);
            uploadFileResult.setData(data);
            if (obsSuccessListener != null) {
                obsSuccessListener.success(results);
            }
            return JSONObject.toJSON(uploadFileResult).toString();
        } catch (Exception e) {
            e.printStackTrace();
            if (obsErrorListener != null) {
                obsErrorListener.error(e.getMessage());
            }
            return "";
        }
    }

    private com.tencent.cos.xml.model.object.PutObjectResult createObjectByCos(Context context, CoreManager coreManager, File file, UploadListener uploadListener, ObsErrorListener obsErrorListener) {
        com.tencent.cos.xml.model.object.PutObjectRequest putObjectRequest = new com.tencent.cos.xml.model.object.PutObjectRequest(getCosBucketName(context, coreManager), getNameKey(file.getAbsolutePath()), file.getAbsolutePath());
        if (uploadListener != null) {
            putObjectRequest.setProgressListener((progress, max) -> {
                // todo Do something to update progress...
                float result = (float) (progress * 100.0 / max);
                uploadListener.progress((int) result);
            });
        }
        // 使用同步方法上传
        try {
            return cosXmlService.putObject(putObjectRequest);
        } catch (CosXmlClientException e) {
            e.printStackTrace();
        } catch (CosXmlServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

    private CompleteMultiUploadResult createObjectWithMultipartUploadByCos(Context context, CoreManager coreManager, File file, UploadListener uploadListener) {
        try {
            String bucketName = getCosBucketName(context, coreManager);
            String cosPath = getNameKey(file.getAbsolutePath());
            InitMultipartUploadRequest initMultipartUploadRequest = new InitMultipartUploadRequest(bucketName, cosPath);
            InitMultipartUploadResult initMultipartUploadResult = cosXmlService.initMultipartUpload(initMultipartUploadRequest);
            int partNumber = 1; //分片块编号，必须从1开始递增
            String uploadId = initMultipartUploadResult.initMultipartUpload.uploadId;
            com.tencent.cos.xml.model.object.UploadPartRequest uploadPartRequest = new com.tencent.cos.xml.model.object.UploadPartRequest(bucketName, cosPath, partNumber, file.getAbsolutePath(), uploadId);
            uploadPartRequest.setProgressListener(new CosXmlProgressListener() {
                @Override
                public void onProgress(long progress, long max) {
                    float result = (float) (progress * 100.0 / max);
                    if (uploadListener != null) {
                        uploadListener.progress((int) result);
                    }
                    Log.w("TEST", "progress =" + (long) result + "%");
                }
            });
            com.tencent.cos.xml.model.object.UploadPartResult uploadPartResult = cosXmlService.uploadPart(uploadPartRequest);
            String eTag = uploadPartResult.eTag; // 获取分片块的 eTag
            Map<Integer, String> partNumberAndETag = new HashMap<>();
            partNumberAndETag.put(partNumber, eTag);
            CompleteMultiUploadRequest completeMultiUploadRequest = new CompleteMultiUploadRequest(bucketName, cosPath, uploadId, partNumberAndETag);
            return cosXmlService.completeMultiUpload(completeMultiUploadRequest);
        } catch (CosXmlServiceException e) {
            e.printStackTrace();
        } catch (CosXmlClientException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setFileResultSources(List<UploadFileResult.Sources> images, List<UploadFileResult.Sources> audios,
                                      List<UploadFileResult.Sources> videos,
                                      List<UploadFileResult.Sources> others, String objectKey, String objectUrl, String mime,
                                      String path, double size, Context context, long time) {
        if (mime.contains("image")) {
            UploadFileResult.Sources sources = new UploadFileResult.Sources();
            sources.setOriginalFileName(objectKey);
            sources.setOriginalUrl(objectUrl);
            sources.setThumbnailUrl(objectUrl);
            sources.setStatus(1);
            images.add(sources);
        } else if (mime.contains("video")) {
            UploadFileResult.Sources sources = new UploadFileResult.Sources();
            sources.setOriginalFileName(objectKey);
            sources.setOriginalUrl(objectUrl);
            sources.setStatus(1);
            videos.add(sources);
        } else if (mime.contains("audio")) {
            UploadFileResult.Sources sources = new UploadFileResult.Sources();
            sources.setOriginalFileName(objectKey);
            sources.setOriginalUrl(objectUrl);
            sources.setStatus(1);
            audios.add(sources);
        } else {
            UploadFileResult.Sources sources = new UploadFileResult.Sources();
            sources.setOriginalFileName(objectKey);
            sources.setOriginalUrl(objectUrl);
            sources.setStatus(1);
            others.add(sources);
        }
        Log.i("OBSUtils", "上传的URL:" + objectUrl);
        if (size > m5 && Constants.SUPPORT_OBS_LOG) {
            if (!TextUtils.isEmpty(path)) {
                boolean oooo = FileUtils.createOrExistsFile(path);
                if (oooo) {
                    long sTime = System.currentTimeMillis();
                    String ip = "用户IP:" + HttpUtil.getIPAddress(context) + "\n";
                    String netSpeed = "用户网速:" + getNetSpeed(context.getApplicationInfo().uid) + "\n";
                    String obsPath = "上传的OBS文件路径:" + objectUrl + "\n";
                    String mSize = "上传文件大小:" + size + "M\n";
                    String starttime = "上传文件开始时间:" + time + "\n";
                    String endtime = "上传文件结束时间:" + sTime + "\n";
                    String timeConsuming = "上传耗时:" + (sTime - time) + "毫秒";
                    boolean aaaa = FileIOUtils.writeFileFromString(path, ip + netSpeed + obsPath + mSize + starttime + endtime + timeConsuming);

                    if (aaaa) {
                        EventBus.getDefault().post(new MonitorBean(path));
                    }
                }
            }
        }
    }


    /**
     * 分段并行上传文件
     *
     * @param context
     * @param coreManager
     * @param file
     * @param uploadListener
     * @return
     */
    private CompleteMultipartUploadResult createObjectWithMultipartUpload(Context context, CoreManager coreManager, File file, UploadListener uploadListener) {
        String nameKey = getNameKey(file);
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(getObsBucketName(context, coreManager), nameKey);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(getMimeType(file.getAbsolutePath()));
        objectMetadata.setContentLength(file.length());
        request.setMetadata(objectMetadata);
        // 设置对象访问权限为公共读
        request.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
        InitiateMultipartUploadResult result = obsClient.initiateMultipartUpload(request);
        String uploadId = result.getUploadId();
        //是并行上传
        // 初始化线程池
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        // 初始化分段上传任务
        long fileSize = file.length();
        // 计算需要上传的段数
        long partCount = fileSize % m1000 == 0 ? fileSize / m1000 : fileSize / m1000 + 1;
        final List<PartEtag> partEtags = Collections.synchronizedList(new ArrayList<PartEtag>());
        // 执行并发上传段
        for (int i = 0; i < partCount; i++) {
            // 分段在文件中的起始位置
            final long offset = i * m1000;
            // 分段大小
            final long currPartSize = (i + 1 == partCount) ? fileSize - offset : m1000;
            // 分段号
            final int partNumber = i + 1;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    UploadPartRequest uploadPartRequest = new UploadPartRequest();
                    uploadPartRequest.setBucketName(getObsBucketName(context, coreManager));
                    uploadPartRequest.setObjectKey(nameKey);
                    uploadPartRequest.setUploadId(uploadId);
                    uploadPartRequest.setFile(file);
                    uploadPartRequest.setPartSize(currPartSize);
                    uploadPartRequest.setOffset(offset);
                    uploadPartRequest.setPartNumber(partNumber);
                    if (uploadListener != null) {
                        uploadPartRequest.setProgressListener(new ProgressListener() {
                            @Override
                            public void progressChanged(ProgressStatus status) {
                                mHandler.post(() -> uploadListener.progress(status.getTransferPercentage()));
                            }
                        });
                    }
                    UploadPartResult uploadPartResult;
                    try {
                        uploadPartResult = obsClient.uploadPart(uploadPartRequest);
                        Log.i("UploadPart", "Part#" + partNumber + " done\n");
                        partEtags.add(new PartEtag(uploadPartResult.getEtag(), uploadPartResult.getPartNumber()));
                    } catch (ObsException e) {
                        Log.e("UploadPart", e.getMessage(), e);
                    }
                }
            });
        }
        // 等待上传完成
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            try {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Log.e("UploadPart", e.getMessage(), e);
            }
        }
        // 合并段
        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                new CompleteMultipartUploadRequest(getObsBucketName(context, coreManager), nameKey, uploadId, partEtags);
        return obsClient.completeMultipartUpload(completeMultipartUploadRequest);

    }


    /**
     * 上传文件
     *
     * @param context
     * @param coreManager
     * @param file
     * @return
     */
    public PutObjectResult createObject(Context context, CoreManager coreManager, File file, UploadListener uploadListener) {
        LogUtils.log(TAG, Thread.currentThread());
        String mime = getMimeType(file.getAbsolutePath());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        if (!TextUtils.isEmpty(mime)) {
            objectMetadata.setContentType(mime);
        }
        objectMetadata.setContentLength(file.length());
        PutObjectRequest request = new PutObjectRequest();
        request.setBucketName(getObsBucketName(context, coreManager));
        request.setObjectKey(getNameKey(file.getAbsolutePath()));
        request.setFile(file);
        request.setMetadata(objectMetadata);
        if (uploadListener != null) {
            request.setProgressListener(new ProgressListener() {
                @Override
                public void progressChanged(ProgressStatus status) {
                    mHandler.post(() -> uploadListener.progress(status.getTransferPercentage()));
                }
            });
        }
        // 设置对象访问权限为公共读
        request.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
        PutObjectResult result = obsClient.putObject(request);
        return result;
    }


    /**
     * 上传头像
     *
     * @param userId
     * @param file
     * @param context
     * @param cor
     * @param successListener
     * @param errorListener
     */
    public static void uploadHeadImg(String userId, File file, Context context, CoreManager cor, ObsSuccessListener<PutObjectResult> successListener, ObsErrorListener errorListener) {
        LogUtils.log(TAG, "进入OBS 头像上传");
        int userIdInt = -1;
        try {
            userIdInt = Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            if (errorListener != null) {
                errorListener.error("userId 不正确");
            }
        }
        if (userIdInt == -1 || userIdInt == 0) {
            return;
        }
        int dirName = userIdInt % 10000;
        String pathO = "avatar/o" + "/" + dirName + "/";
        String pathT = "avatar/t" + "/" + dirName + "/";

        if (cor.getConfig().IS_OPEN_OBS_STATUS == TENTENT_COS) {
            createHeadFolderByCos(userId, file, context, cor, successListener, errorListener, pathO);
            createHeadFolderByCos(userId, file, context, cor, successListener, errorListener, pathT);
        } else {
            createHeadFolder(userId, file, context, cor, successListener, errorListener, pathO);
            createHeadFolder(userId, file, context, cor, successListener, errorListener, pathT);
        }
    }


    /**
     * 上传头像
     *
     * @param userId
     * @param file
     * @param context
     * @param cor
     * @param successListener
     * @param errorListener
     */
    public static void uploadGroupHeadImg(String userId, File file, Context context, CoreManager cor, ObsSuccessListener<PutObjectResult> successListener, ObsErrorListener errorListener) {
        int jidHashCode = userId.hashCode();
        int oneLevelName = Math.abs(jidHashCode % 10000);
        int twoLevelName = Math.abs(jidHashCode % 20000);
        int dirName = oneLevelName;
        String pathO = "avatar/o/" + dirName + "/" + twoLevelName + "/";
        String pathT = "avatar/t/" + dirName + "/" + twoLevelName + "/";
        if (cor.getConfig().IS_OPEN_OBS_STATUS == TENTENT_COS) {
            createHeadFolderByCos(userId, file, context, cor, successListener, errorListener, pathO);
            createHeadFolderByCos(userId, file, context, cor, successListener, errorListener, pathT);
        } else {
            createHeadFolder(userId, file, context, cor, successListener, errorListener, pathO);
            createHeadFolder(userId, file, context, cor, successListener, errorListener, pathT);
        }
    }


    /**
     * 腾讯云创建头像目录
     *
     * @param userId
     * @param file
     * @param context
     * @param cor
     * @param successListener
     * @param errorListener
     * @param path
     */
    private static void createHeadFolderByCos(String userId, File file, Context context, CoreManager cor, ObsSuccessListener<PutObjectResult> successListener, ObsErrorListener errorListener, String path) {
        OBSUtils.getInstance().createObjectByCos(userId, path, context, cor, file, successListener, errorListener);
//        ObsSuccessListener<String> listener = result ->
//
//                OBSUtils.getInstance().createHeadPath(userId, file, context, cor,
//                        listener,
//                        msg ->
//                                OBSUtils.getInstance().createFolderByCos(path, context, cor, listener, errorListener), path);
    }


    /**
     * 创建Cos 对象
     *
     * @param successListener
     * @param errorListener
     */
    private void createObjectByCos(String userId, String path, Context context, CoreManager cor, File file, ObsSuccessListener<PutObjectResult> successListener, ObsErrorListener errorListener) {
        if (requiredCosClient(errorListener)) {
            String objectKey = path + userId + ".jpg";
            long time = System.currentTimeMillis();
            double size = file.length() / m1;
            com.tencent.cos.xml.model.object.PutObjectRequest objectRequest = new com.tencent.cos.xml.model.object.PutObjectRequest(getCosBucketName(context, cor), objectKey, file.getAbsolutePath());
            cosXmlService.putObjectAsync(objectRequest, new CosXmlResultListener() {
                @Override
                public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                    if (result.httpCode == 200 && !TextUtils.isEmpty(transformObjectUrl(result.accessUrl))) {
                        Log.i(TAG, "上传头像成功");
                        if (mHandler != null) {
                            mHandler.post(() -> successListener.success(new PutObjectResult(getCosBucketName(context, cor), objectKey, "", "", StorageClassEnum.STANDARD, transformObjectUrl(result.accessUrl))));
                        }
                        if (size > m5 && Constants.SUPPORT_OBS_LOG) {
                            String sOBSPath = context.getExternalCacheDir().getPath() + File.separator + "obsLog/" + MyApplication.getLoginUserId() + "_log.txt";
                            if (!TextUtils.isEmpty(sOBSPath)) {
                                boolean oooo = FileUtils.createOrExistsFile(sOBSPath);
                                if (oooo) {
                                    long sTime = System.currentTimeMillis();
                                    String ip = "用户IP:" + HttpUtil.getIPAddress(context) + "\n";
                                    String netSpeed = "用户网速:" + getNetSpeed(context.getApplicationInfo().uid) + "\n";
                                    String obsPath = "上传的OBS文件路径:" + transformObjectUrl(result.accessUrl) + "\n";
                                    String mSize = "上传文件大小:" + size + "M\n";
                                    String starttime = "上传文件开始时间:" + time + "\n";
                                    String endtime = "上传文件结束时间:" + sTime + "\n";
                                    String timeConsuming = "上传耗时:" + (sTime - time) + "毫秒";
                                    boolean aaaa = FileIOUtils.writeFileFromString(sOBSPath, ip + netSpeed + obsPath + mSize + starttime + endtime + timeConsuming);
                                    if (aaaa) {
                                        EventBus.getDefault().post(new MonitorBean(sOBSPath));
                                    }
                                }
                            }
                        }
                    }

                }

                @Override
                public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
//                    if (exception != null) {
//                        Log.i(TAG, exception.errorMessage);
//                    }
//                    if (serviceException != null) {
//                        Log.i(TAG, serviceException.getMessage());
//                    }
                    if (mHandler != null) {
                        mHandler.post(() -> errorListener.error("上传头像::创建COS文件失败"));
                    }

                }
            });
        }
    }

    /**
     * 创建Cos文件夹
     *
     * @param path
     * @param context
     * @param cor
     * @param listener
     * @param errorListener
     */
    private void createFolderByCos(String path, Context context, CoreManager cor, ObsSuccessListener<String> listener, ObsErrorListener errorListener) {
        if (requiredCosClient(errorListener)) {
            com.tencent.cos.xml.model.object.PutObjectRequest putObjectRequest = new com.tencent.cos.xml.model.object.PutObjectRequest(getCosBucketName(context, cor), path, new byte[0]);
            putObjectRequest.setXCOSACL(COSACL.PUBLIC_READ_WRITE);
            cosXmlService.putObjectAsync(putObjectRequest, new CosXmlResultListener() {
                @Override
                public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                    listener.success(null);
                    LogUtils.log(TAG, "创建目录头像文件夹成功");

                }

                @Override
                public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
                    errorListener.error("创建文件夹失败");
                    LogUtils.log(TAG, "创建目录头像文件夹失败");

                }
            });
        }
    }


    /**
     * 创建COS文件存储路径
     *
     * @param userId
     * @param file
     * @param context
     * @param cor
     * @param successListener
     * @param obsErrorListener
     * @param path
     */
    private void createHeadPath(String userId, File file, Context context, CoreManager cor, ObsSuccessListener<String> successListener, ObsErrorListener obsErrorListener, String path) {
        if (requiredCosClient(obsErrorListener)) {
            isHaveCosObject(path, context, cor, result -> successListener.success(null), obsErrorListener);
        }
    }


    /**
     * 是否有 cos 目录对象
     *
     * @param path
     * @return
     */
    private void isHaveCosObject(String path, Context context, CoreManager cor, ObsSuccessListener<CosXmlResult> obsSuccessListener, ObsErrorListener errorListener) {
        try {
            String tempFileName = "temp.temp";
            File file = new File(MyApplication.getInstance().getCacheDir().getAbsolutePath() + tempFileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            GetObjectRequest getObjectRequest = new GetObjectRequest(getCosBucketName(context, cor), path, file.getAbsolutePath());
            cosXmlService.getObjectAsync(getObjectRequest, new CosXmlResultListener() {
                @Override
                public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                    if (result.httpCode == 200) {
                        obsSuccessListener.success(result);
                        LogUtils.log(TAG, "有目录头像文件夹");
                    }
                }

                @Override
                public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
                    errorListener.error("检查目录对象失败");
                    com.iimm.miliao.util.log.LogUtils.e(TAG, "没有头像目录文件夹onFail");

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            errorListener.error("检查目录对象出错");
            com.iimm.miliao.util.log.LogUtils.e(TAG, "没有头像目录文件夹 出错");

        }
    }

    private boolean requiredCosClient(ObsErrorListener obsErrorListener) {
        if (cosXmlService == null) {
            CosXmlService cosXmlService = rebuildCosClient();
            if (cosXmlService == null) {
                mHandler.post(() -> {
                    if (obsErrorListener != null) {
                        obsErrorListener.error("重建COS client 失败");
                    }
                });
                return false;
            }
            return true;
        } else {
            return true;
        }
    }


    /**
     * 重建COs
     *
     * @return
     */
    private CosXmlService rebuildCosClient() {
        if (mSetCosConfigInfo != null) {
            String regionByCOS = mSetCosConfigInfo.getRegionByCOS();
            if (TextUtils.isEmpty(regionByCOS)) {
                return null;
            }
            QCloudCredentialProvider qCloudCredentialProvider = mSetCosConfigInfo.authorizeCosKey(mSetCosConfigInfo.getServiceConfigForCos(regionByCOS));
            if (qCloudCredentialProvider == null) {
                return null;
            }
            return new CosXmlService(MyApplication.getContext(), mSetCosConfigInfo.getServiceConfigForCos(regionByCOS), qCloudCredentialProvider);
        } else {
            return null;
        }
    }


    /**
     * 创建头像目录
     *
     * @param userId
     * @param file
     * @param context
     * @param cor
     * @param successListener
     * @param errorListener
     * @param pathO
     */
    private static void createHeadFolder(String userId, File file, Context context, CoreManager cor, ObsSuccessListener<PutObjectResult> successListener, ObsErrorListener errorListener, String pathO) {
        String objectKey = pathO + userId + ".jpg";
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.length());
        objectMetadata.setContentType("image/jpeg");
        OBSUtils.getInstance().createObject(file, objectKey, getObsBucketName(context, cor), objectMetadata,
                result1 -> {
                    LogUtils.log(TAG, transformObjectUrl(result1.getObjectUrl()));
                    getInstance().mHandler.post(() -> successListener.success(result1));
                }, errorListener);
//        OBSUtils.createHeadFolder(pathO, context, cor,
//                result -> {
//
//                }
//                , errorListener);
    }


    private static void createHeadFolder(String path, Context context, CoreManager cor, ObsSuccessListener<PutObjectResult> successListener, ObsErrorListener errorListener) {
        ThreadManager.getPool().execute(new Runnable() {
            @Override
            public void run() {
                if (!OBSUtils.getInstance().isHaveObsObject(path, context, cor)) {
                    LogUtils.log(TAG, "创建目录头像文件夹");
                    OBSUtils.getInstance().createFolder(path, context, cor, successListener, errorListener);
                } else {
                    successListener.success(null);
                }
            }
        });
    }

    private void createFolder(String path, Context context, CoreManager cor, ObsSuccessListener<PutObjectResult> successListener, ObsErrorListener errorListener) {
        if (requiredObsClient(null)) {
            try {
                PutObjectResult putObjectResult = obsClient.putObject(getObsBucketName(context, cor), path, new ByteArrayInputStream(new byte[0]));
                obsClient.setObjectAcl(getObsBucketName(context, cor), path, AccessControlList.REST_CANNED_PUBLIC_READ);
                if (successListener != null) {
                    mHandler.post(() -> successListener.success(putObjectResult));
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (errorListener != null) {
                    mHandler.post(() -> errorListener.error("创建文件夹失败"));
                }
            }
        }
    }

    private boolean isHaveObsObject(String path, Context context, CoreManager cor) {
        if (requiredObsClient(null)) {
            try {
                ObsObject obsObject = obsClient.getObject(getObsBucketName(context, cor), path);
                if (obsObject == null) {
                    return false;
                } else {
                    return true;
                }
            } catch (ObsException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 重建cos 信息
     */
    private static SetCosConfigInfo mSetCosConfigInfo;

    public static void setObsConfigInfo(SetCosConfigInfo setObsConfigInfo) {
        mSetCosConfigInfo = setObsConfigInfo;
    }

    public interface SetCosConfigInfo {

        String getRegionByCOS();

        CosXmlServiceConfig getServiceConfigForCos(@NonNull String regionByCOS);

        QCloudCredentialProvider authorizeCosKey(@NonNull CosXmlServiceConfig serviceConfigForCos);
    }

    public interface SetObsConfigInfo {

        String getAk();

        String getSk();

        String getSecurityToken();

        ObsConfiguration getObsConfig();
    }


    public interface ObsErrorListener {

        void error(String msg);
    }

    public interface ObsSuccessListener<T> {
        void success(T result);
    }

    public interface UploadListener {
        void progress(int progress);
    }

    public interface MultipartUploadProgressListener {
        void progress(int partNum, int progress);
    }

    private String getMimeType(String filePath) {
        String ext = MimeTypeMap.getFileExtensionFromUrl(filePath);
        String result = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        return result != null ? result : "";
    }


    public static String transformObjectUrl(String url) {
        try {
            String downloadUrl = MyApplication.mCoreManager.getConfig().downloadUrl;
            if (TextUtils.isEmpty(downloadUrl)) {
                return url;
            } else if (downloadUrl.endsWith("/")) {
                downloadUrl = downloadUrl.substring(0, downloadUrl.length() - 1);
            }
            URL u = new URL(url);
            return downloadUrl + u.getPath();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }
    }


    //---------------------------------------------------------------------------------------------------------

    /**
     * 上传文件列表
     *
     * @param list
     * @param obsSuccessListener
     * @param obsErrorListener
     * @param uploadListener
     * @param context
     * @param coreManager
     * @return
     */
    public static String uploadFileListLog(List<String> list, ObsSuccessListener<List<?>> obsSuccessListener, ObsErrorListener obsErrorListener, UploadListener uploadListener, Context context, CoreManager coreManager) {
        return OBSUtils.getInstance().createListObjectLog(list, obsSuccessListener, obsErrorListener, uploadListener, context, coreManager);
    }

    /**
     * 文件 列表 串行 上传 LOG
     *
     * @param photoList
     * @param obsSuccessListener
     * @param obsErrorListener
     * @param uploadListener
     * @param context
     * @param coreManager
     * @return
     */
    private String createListObjectLog(List<String> photoList, ObsSuccessListener<List<?>> obsSuccessListener, ObsErrorListener obsErrorListener, UploadListener uploadListener, Context context, CoreManager coreManager) {
        if (coreManager.getConfig().IS_OPEN_OBS_STATUS == TENTENT_COS) {
            if (requiredCosClient(obsErrorListener)) {
                Log.d(TAG, "上传到COS");
                return handlerUploadFileListLog(photoList, obsSuccessListener, obsErrorListener, uploadListener, context, coreManager, false);
            } else {
                return "";
            }
        } else if (coreManager.getConfig().IS_OPEN_OBS_STATUS == HUIWEI_OBS) {
            if (requiredObsClient(obsErrorListener)) {
                return handlerUploadFileListLog(photoList, obsSuccessListener, obsErrorListener, uploadListener, context, coreManager, true);
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @NotNull
    private String handlerUploadFileListLog(List<String> photoList, ObsSuccessListener<List<?>> obsSuccessListener, ObsErrorListener obsErrorListener, UploadListener uploadListener, Context context, CoreManager coreManager, boolean isHuaWei) {
        try {
            String path = context.getExternalCacheDir().getPath() + File.separator + "obsLog/" + MyApplication.getLoginUserId() + "_log.txt";

            List<Object> results = new ArrayList<>();
            UploadFileResult.Data data = new UploadFileResult.Data();
            List<UploadFileResult.Sources> images = new ArrayList<>();
            List<UploadFileResult.Sources> videos = new ArrayList<>();
            List<UploadFileResult.Sources> audios = new ArrayList<>();
            List<UploadFileResult.Sources> others = new ArrayList<>();
            int temp = 0;
            for (int i = 0; i < photoList.size(); i++) {
                //Tencent
                com.tencent.cos.xml.model.object.PutObjectResult result = null;
                CompleteMultiUploadResult uploadResult = null;
                //HuaWei
                PutObjectResult result_hua_wei = null;
                CompleteMultipartUploadResult uploadResult_hua_wei = null;
                File file = new File(photoList.get(i));
                String mime = getMimeType(photoList.get(i));
                double size = file.length() / m1;
                long time = System.currentTimeMillis();

                if (file.length() > m1000) {
                    //大于10兆
                    if (isHuaWei) {
                        uploadResult_hua_wei = createObjectWithMultipartUploadLog(context, coreManager, file, uploadListener);
                        if (uploadResult_hua_wei != null) {
                            results.add(uploadResult_hua_wei);
                        }
                    } else {
                        uploadResult = createObjectWithMultipartUploadByCosLog(context, coreManager, file, uploadListener);
                        if (!TextUtils.isEmpty(transformObjectUrl(uploadResult.accessUrl))) {
                            Log.i(TAG, "上传完成：" + transformObjectUrl(uploadResult.accessUrl));
                        }
                        if (uploadResult != null) {
                            results.add(uploadResult);
                        }
                    }
                } else {
                    if (isHuaWei) {
                        result_hua_wei = createObjectLog(context, coreManager, file, uploadListener);
                        if (result_hua_wei != null) {
                            results.add(result_hua_wei);
                        }
                    } else {
                        result = createObjectByCosLog(context, coreManager, file, uploadListener, obsErrorListener);
                        if (!TextUtils.isEmpty(transformObjectUrl(result.accessUrl))) {
                            Log.i(TAG, "上传完成：" + transformObjectUrl(result.accessUrl));
                        }
                        if (result != null) {
                            results.add(result);
                        }
                    }
                }
                if (result != null || result_hua_wei != null) {
                    if (isHuaWei) {
                        setFileResultSources(images, audios, videos, others, result_hua_wei.getObjectKey(), transformObjectUrl(result_hua_wei.getObjectUrl()), mime, path, size, context, time);
                    } else {
                        //TODO 需要更改  一个是key  一个url 腾讯没有返回
                        setFileResultSources(images, audios, videos, others, getNameKey(transformObjectUrl(result.accessUrl)), transformObjectUrl(result.accessUrl), mime, path, size, context, time);
                    }
                    temp++;
                } else if (uploadResult != null || uploadResult_hua_wei != null) {
                    if (isHuaWei) {
                        setFileResultSources(images, audios, videos, others, uploadResult_hua_wei.getObjectKey(), transformObjectUrl(uploadResult_hua_wei.getObjectUrl()), mime, path, size, context, time);
                    } else {
                        //TODO 需要更改  一个是key  一个url  腾讯没有返回
                        setFileResultSources(images, audios, videos, others, uploadResult.completeMultipartUpload.key, transformObjectUrl(uploadResult.accessUrl), mime, path, size, context, time);
                    }
                    temp++;
                } else {
                    continue;
                }
            }
            data.setImages(images);
            data.setAudios(audios);
            data.setVideos(videos);
            data.setOthers(others);
//             data.setFiles(others);
            UploadFileResult uploadFileResult = new UploadFileResult();
            uploadFileResult.setSuccess(temp);
            uploadFileResult.setFailure(0);
            uploadFileResult.setTotal(photoList.size());
            uploadFileResult.setResultCode(1);
            uploadFileResult.setData(data);
            if (obsSuccessListener != null) {
                obsSuccessListener.success(results);
            }
            return JSONObject.toJSON(uploadFileResult).toString();
        } catch (Exception e) {
            e.printStackTrace();
            if (obsErrorListener != null) {
                obsErrorListener.error(e.getMessage());
            }
            return "";
        }
    }

    /**
     * 分段并行上传文件
     *
     * @param context
     * @param coreManager
     * @param file
     * @param uploadListener
     * @return
     */
    private CompleteMultipartUploadResult createObjectWithMultipartUploadLog(Context context, CoreManager coreManager, File file, UploadListener uploadListener) {
        String nameKey = MyApplication.getLoginUserId() + "_log.txt";
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(getObsBucketName(context, coreManager), nameKey);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(getMimeType(file.getAbsolutePath()));
        objectMetadata.setContentLength(file.length());
        request.setMetadata(objectMetadata);
        // 设置对象访问权限为公共读
        request.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
        InitiateMultipartUploadResult result = obsClient.initiateMultipartUpload(request);
        String uploadId = result.getUploadId();
        //是并行上传
        // 初始化线程池
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        // 初始化分段上传任务
        long fileSize = file.length();
        // 计算需要上传的段数
        long partCount = fileSize % m1000 == 0 ? fileSize / m1000 : fileSize / m1000 + 1;
        final List<PartEtag> partEtags = Collections.synchronizedList(new ArrayList<PartEtag>());
        // 执行并发上传段
        for (int i = 0; i < partCount; i++) {
            // 分段在文件中的起始位置
            final long offset = i * m1000;
            // 分段大小
            final long currPartSize = (i + 1 == partCount) ? fileSize - offset : m1000;
            // 分段号
            final int partNumber = i + 1;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    UploadPartRequest uploadPartRequest = new UploadPartRequest();
                    uploadPartRequest.setBucketName(getObsBucketName(context, coreManager));
                    uploadPartRequest.setObjectKey(nameKey);
                    uploadPartRequest.setUploadId(uploadId);
                    uploadPartRequest.setFile(file);
                    uploadPartRequest.setPartSize(currPartSize);
                    uploadPartRequest.setOffset(offset);
                    uploadPartRequest.setPartNumber(partNumber);
                    if (uploadListener != null) {
                        uploadPartRequest.setProgressListener(new ProgressListener() {
                            @Override
                            public void progressChanged(ProgressStatus status) {
                                mHandler.post(() -> uploadListener.progress(status.getTransferPercentage()));
                            }
                        });
                    }
                    UploadPartResult uploadPartResult;
                    try {
                        uploadPartResult = obsClient.uploadPart(uploadPartRequest);
                        Log.i("UploadPart", "Part#" + partNumber + " done\n");
                        partEtags.add(new PartEtag(uploadPartResult.getEtag(), uploadPartResult.getPartNumber()));
                    } catch (ObsException e) {
                        Log.e("UploadPart", e.getMessage(), e);
                    }
                }
            });
        }
        // 等待上传完成
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            try {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Log.e("UploadPart", e.getMessage(), e);
            }
        }
        // 合并段
        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                new CompleteMultipartUploadRequest(getObsBucketName(context, coreManager), nameKey, uploadId, partEtags);
        return obsClient.completeMultipartUpload(completeMultipartUploadRequest);

    }


    private CompleteMultiUploadResult createObjectWithMultipartUploadByCosLog(Context context, CoreManager coreManager, File file, UploadListener uploadListener) {
        try {
            String bucketName = getCosBucketName(context, coreManager);
            String cosPath = MyApplication.getLoginUserId() + "_log.txt";
            InitMultipartUploadRequest initMultipartUploadRequest = new InitMultipartUploadRequest(bucketName, cosPath);
            InitMultipartUploadResult initMultipartUploadResult = cosXmlService.initMultipartUpload(initMultipartUploadRequest);
            int partNumber = 1; //分片块编号，必须从1开始递增
            String uploadId = initMultipartUploadResult.initMultipartUpload.uploadId;
            com.tencent.cos.xml.model.object.UploadPartRequest uploadPartRequest = new com.tencent.cos.xml.model.object.UploadPartRequest(bucketName, cosPath, partNumber, file.getAbsolutePath(), uploadId);
            uploadPartRequest.setProgressListener(new CosXmlProgressListener() {
                @Override
                public void onProgress(long progress, long max) {
                    float result = (float) (progress * 100.0 / max);
                    if (uploadListener != null) {
                        uploadListener.progress((int) result);
                    }
                    Log.w("TEST", "progress =" + (long) result + "%");
                }
            });
            com.tencent.cos.xml.model.object.UploadPartResult uploadPartResult = cosXmlService.uploadPart(uploadPartRequest);
            String eTag = uploadPartResult.eTag; // 获取分片块的 eTag
            Map<Integer, String> partNumberAndETag = new HashMap<>();
            partNumberAndETag.put(partNumber, eTag);
            CompleteMultiUploadRequest completeMultiUploadRequest = new CompleteMultiUploadRequest(bucketName, cosPath, uploadId, partNumberAndETag);
            return cosXmlService.completeMultiUpload(completeMultiUploadRequest);
        } catch (CosXmlServiceException e) {
            e.printStackTrace();
        } catch (CosXmlClientException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 上传文件
     *
     * @param context
     * @param coreManager
     * @param file
     * @return
     */
    public PutObjectResult createObjectLog(Context context, CoreManager coreManager, File file, UploadListener uploadListener) {
        LogUtils.log(TAG, Thread.currentThread());
        String mime = getMimeType(file.getAbsolutePath());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        if (!TextUtils.isEmpty(mime)) {
            objectMetadata.setContentType(mime);
        }
        objectMetadata.setContentLength(file.length());
        PutObjectRequest request = new PutObjectRequest();
        request.setBucketName(getObsBucketName(context, coreManager));
        request.setObjectKey(MyApplication.getLoginUserId() + "_log.txt");
        request.setFile(file);
        request.setMetadata(objectMetadata);
        if (uploadListener != null) {
            request.setProgressListener(new ProgressListener() {
                @Override
                public void progressChanged(ProgressStatus status) {
                    mHandler.post(() -> uploadListener.progress(status.getTransferPercentage()));
                }
            });
        }
        // 设置对象访问权限为公共读
        request.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
        PutObjectResult result = obsClient.putObject(request);
        return result;
    }

    private com.tencent.cos.xml.model.object.PutObjectResult createObjectByCosLog(Context context, CoreManager coreManager, File file, UploadListener uploadListener, ObsErrorListener obsErrorListener) {
        com.tencent.cos.xml.model.object.PutObjectRequest putObjectRequest = new com.tencent.cos.xml.model.object.PutObjectRequest(getCosBucketName(context, coreManager), MyApplication.getLoginUserId() + "_log.txt", file.getAbsolutePath());
        if (uploadListener != null) {
            putObjectRequest.setProgressListener((progress, max) -> {
                // todo Do something to update progress...
                float result = (float) (progress * 100.0 / max);
                uploadListener.progress((int) result);
            });
        }
        // 使用同步方法上传
        try {
            return cosXmlService.putObject(putObjectRequest);
        } catch (CosXmlClientException e) {
            e.printStackTrace();
        } catch (CosXmlServiceException e) {
            e.printStackTrace();
        }
        return null;
    }


    private long lastTotalRxBytes = 0;
    private long lastTimeStamp = 0;

    public String getNetSpeed(int uid) {
        long nowTotalRxBytes = getTotalRxBytes(uid);
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        return String.valueOf(speed) + " kb/s";
    }

    //getApplicationInfo().uid
    public long getTotalRxBytes(int uid) {
        return TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
    }
}
