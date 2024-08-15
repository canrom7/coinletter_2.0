package com.iimm.miliao.ad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdInteractionListener;
import com.bytedance.sdk.openadsdk.TTAdLoadType;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.iimm.miliao.R;
import com.iimm.miliao.util.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * Created by bytedance on 2018/2/1.
 * 激励视频接入类
 */

public class CjsRewardVideoActivity extends Activity  {
    private static final String TAG = "RewardVideoActivity";
    private String mHorizontalCodeId;
    private String mVerticalCodeId;


    private TTAdNative mTTAdNative;
    private AdLoadListener mAdLoadListener;

    @SuppressWarnings("RedundantCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_video);
        getExtraInfo();
        initViews();
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //step1:初始化sdk

        TTAdManager ttAdManager = TTAdManagerHolder.get();
        //step2:(可选，强烈建议在合适的时机调用):申请部分权限
        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
        //step3:创建TTAdNative对象,用于调用广告请求接口

        mTTAdNative = ttAdManager.createAdNative(getApplicationContext());

        loadAd(mHorizontalCodeId);
    }

    /**
     * 加载广告
     */
    private void loadAd(final String codeId) {
        //step5:创建广告请求参数AdSlot

        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) // 广告代码位Id
                .setAdLoadType(TTAdLoadType.LOAD) // 本次广告用途：TTAdLoadType.LOAD实时；TTAdLoadType.PRELOAD预请求
                .setRewardAmount(123)
                .setUserData(getUserData())
                .setRewardName("金币")
                .build();
        //step6:注册广告加载生命周期监听，请求广告

        mAdLoadListener = new AdLoadListener(this);

        mTTAdNative.loadRewardVideoAd(adSlot, mAdLoadListener);
    }

    private String getUserData(){
        JSONArray jsonArray = new JSONArray();
        JSONObject object = new JSONObject();
        try {
            object.put("name", "auth_reward_gold");
            object.put("value", "3000");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        jsonArray.put(object);

        return jsonArray.toString();
    }

    /**
     * 展示广告
     */
    private void showAd() {
        if (mAdLoadListener == null) {
            return;
        }
        mAdLoadListener.showAd();
    }

    /**
     * 【必须】广告加载期间生命周期监听
     */

    private  class AdLoadListener implements TTAdNative.RewardVideoAdListener {

        private final Activity mActivity;

        private TTRewardVideoAd mAd;

        private TTAdInteractionListener mInteractionListener =  new TTAdInteractionListener() {
            @Override
            public void onAdEvent(int code, Map map) {

                if (code == TTAdConstant.AD_EVENT_AUTH_DOUYIN && map != null) {
                    // 抖音授权成功状态回调, 媒体可以通过map获取抖音openuid用以判断是否下发奖励
                    String uid = (String) map.get("open_uid");
                    Log.i(TAG, "授权成功 --> uid：" + uid);
                }
            }
        };
        public AdLoadListener(Activity activity) {
            mActivity = activity;
        }

        /**
         * 广告加载过程中出错
         */
        @Override
        public void onError(int code, String message) {
            Log.e(TAG, "Callback --> onError: " + code + ", " + message);
            ToastUtil.showToast(mActivity, message);
        }

        /**
         * 广告基础信息加载完成，此方法是回调后是广告可调用展示的最早时机
         *
         * @param ad 广告对象 在一次广告生命周期中onRewardVideoAdLoad与onRewardVideoCached回调中的ad是同一个对象
         */
        @Override

        public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
            Log.e(TAG, "Callback --> onRewardVideoAdLoad");

//            ToastUtil.showToast(mActivity, "rewardVideoAd loaded 广告类型：" + getAdType(ad.getRewardVideoAdType()));
            handleAd(ad);

            showAd();
        }

        @Override

        public void onRewardVideoCached() {
            // 已废弃 请使用 onRewardVideoCached(TTRewardVideoAd ad) 方法
        }

        /**
         * 广告基础信息与素材缓存完成，此时调用广告展示流畅，是展示广告的最理想时机
         *
         * @param ad 广告对象 在一次广告生命周期中onRewardVideoAdLoad与onRewardVideoCached回调中的ad是同一个对象
         */
        @Override

        public void onRewardVideoCached(TTRewardVideoAd ad) {
            Log.e(TAG, "Callback --> onRewardVideoCached");

//            ToastUtil.showToast(mActivity, "rewardVideoAd cached 广告类型：" + getAdType(ad.getRewardVideoAdType()));
            handleAd(ad);
        }

        /**
         * 处理广告对象
         */

        public void handleAd(TTRewardVideoAd ad) {
            if (mAd != null) {
                return;
            }
            mAd = ad;
            //【必须】广告展示时的生命周期监听

            mAd.setRewardAdInteractionListener(new AdLifeListener(mActivity));

            //【可选】再看一个展示时的生命状态监听

            PlayAgainAdLifeListener playAgainAdLifeListener = new PlayAgainAdLifeListener(mActivity);
            mAd.setRewardPlayAgainInteractionListener(playAgainAdLifeListener);
            //【可选】再看一个入口与奖励显示控制器

            PlayAgainController playAgainController = new PlayAgainController();
            playAgainController.setPlayAgainAdLifeListener(playAgainAdLifeListener);
            mAd.setRewardPlayAgainController(playAgainController);
            //【可选】监听下载状态
            mAd.setDownloadListener(new DownloadStatusListener());

            /**
             * 注册广告事件监听， 目前只有授权事件定义，后续会扩展
             */
            mAd.setAdInteractionListener(mInteractionListener);
        }

        /**
         * 触发展示广告
         */
        public void showAd() {
            if (mAd == null) {
                ToastUtil.showToast(mActivity, "当前广告未加载好，请先点击加载广告");
                return;
            }

            mAd.showRewardVideoAd(mActivity);
            // 广告使用后应废弃
            mAd = null;
        }
    }

    /**
     * 【必须】广告生命状态监听器
     */

    private  class AdLifeListener implements TTRewardVideoAd.RewardAdInteractionListener {

        private final WeakReference<Context> mContextRef;

        public AdLifeListener(Context context) {
            mContextRef = new WeakReference<>(context);
        }

        @Override

        public void onAdShow() {
            // 广告展示
            Log.d(TAG, "Callback --> rewardVideoAd show");
//            ToastUtil.showToast(mContextRef.get(), "rewardVideoAd show");
        }

        @Override

        public void onAdVideoBarClick() {
            // 广告中产生了点击行为
            Log.d(TAG, "Callback --> rewardVideoAd bar click");
//            ToastUtil.showToast(mContextRef.get(), "rewardVideoAd bar click");
        }

        @Override

        public void onAdClose() {
            // 广告整体关闭
            Log.d(TAG, "Callback --> rewardVideoAd close");
//            ToastUtil.showToast(mContextRef.get(), "rewardVideoAd close");
            finish();
        }

        //视频播放完成回调
        @Override
        public void onVideoComplete() {
            // 广告素材播放完成，例如视频未跳过，完整的播放了
            Log.d(TAG, "Callback --> rewardVideoAd complete");
//            ToastUtil.showToast(mContextRef.get(), "rewardVideoAd complete");
            Intent intent = new Intent();
            intent.putExtra("isOk",true);
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void onVideoError() {
            // 广告素材展示时出错
            Log.e(TAG, "Callback --> rewardVideoAd error");
            ToastUtil.showToast(mContextRef.get(), "rewardVideoAd error");
            setResult(RESULT_OK);
        }

        @Override

        public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int errorCode, String errorMsg) {
            // 已废弃 请使用 onRewardArrived(boolean isRewardValid, int rewardType, Bundle extraInfo)
        }

        @Override

        public void onRewardArrived(boolean isRewardValid, int rewardType, Bundle extraInfo) {
            // 用户的观看行为满足了奖励条件
        }

        @Override
        public void onSkippedVideo() {
            // 用户在观看素材时点击了跳过
            Log.e(TAG, "Callback --> rewardVideoAd has onSkippedVideo");
//            ToastUtil.showToast(mContextRef.get(), "rewardVideoAd has onSkippedVideo");
            finish();
        }
    }

    /**
     * 【可选】再看广告生命状态监听器
     */

    private  class PlayAgainAdLifeListener implements TTRewardVideoAd.RewardAdInteractionListener {

        private final WeakReference<Context> mContextRef;
        private int mNowPlayAgainCount = 0;
        private int mNextPlayAgainCount = 1;

        public PlayAgainAdLifeListener(Context context) {
            mContextRef = new WeakReference<>(context);
        }

        @Override

        public void onAdShow() {
            mNowPlayAgainCount = mNextPlayAgainCount;
            Log.d(TAG, "Callback --> 第 " + mNowPlayAgainCount + " 次再看 rewardPlayAgain show");
//            ToastUtil.showToast(mContextRef.get(), "rewardVideoAd show");
        }

        @Override

        public void onAdVideoBarClick() {
            Log.d(TAG, "Callback --> 第 " + mNowPlayAgainCount + " 次再看 rewardPlayAgain bar click");
//            ToastUtil.showToast(mContextRef.get(), "rewardVideoAd bar click");
        }

        @Override

        public void onAdClose() {
            // 再看广告不会调到这个回调
        }

        //视频播放完成回调
        @Override
        public void onVideoComplete() {
            Log.d(TAG, "Callback --> 第 " + mNowPlayAgainCount + " 次再看 rewardPlayAgain complete");
//            ToastUtil.showToast(mContextRef.get(), "rewardVideoAd complete");
        }

        @Override
        public void onVideoError() {
            Log.e(TAG, "Callback --> 第 " + mNowPlayAgainCount + " 次再看 rewardPlayAgain error");
            ToastUtil.showToast(mContextRef.get(), "rewardVideoAd error");
            setResult(RESULT_OK);
        }

        @Override

        public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int errorCode, String errorMsg) {
            // 已废弃 请使用 onRewardArrived(boolean isRewardValid, int rewardType, Bundle extraInfo) 方法
        }

        @Override
        public void onRewardArrived(boolean isRewardValid, int rewardType, Bundle extraInfo) {
        }

        @Override
        public void onSkippedVideo() {
            Log.e(TAG, "Callback --> 第 " + mNowPlayAgainCount + " 次再看 rewardPlayAgain has onSkippedVideo");
//            ToastUtil.showToast(mContextRef.get(), "rewardVideoAd has onSkippedVideo");
        }

        public void setNextPlayAgainCount(int nextPlayAgainCount) {
            mNextPlayAgainCount = nextPlayAgainCount;
        }
    }

    /**
     * 【可选】再看广告入口控制器
     */

    private static class PlayAgainController implements TTRewardVideoAd.RewardAdPlayAgainController {

        private PlayAgainAdLifeListener mPlayAgainAdLifeListener;

        public void setPlayAgainAdLifeListener(PlayAgainAdLifeListener playAgainAdLifeListener) {
            mPlayAgainAdLifeListener = playAgainAdLifeListener;
        }

        @Override
        public void getPlayAgainCondition(int nextPlayAgainCount, Callback callback) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(KEY_PLAY_AGAIN_ALLOW, true);
            bundle.putString(KEY_PLAY_AGAIN_REWARD_NAME, "金币");
            bundle.putString(KEY_PLAY_AGAIN_REWARD_AMOUNT, nextPlayAgainCount + "");
            if (mPlayAgainAdLifeListener != null) {
                mPlayAgainAdLifeListener.setNextPlayAgainCount(nextPlayAgainCount);
            }
            callback.onConditionReturn(bundle);
        }
    }

    /**
     * 【可选】下载状态监听器
     */
    private static class DownloadStatusListener implements TTAppDownloadListener {

        @Override
        public void onIdle() {
        }

        @Override
        public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
            Log.d("DML", "onDownloadActive==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
        }

        @Override
        public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
            Log.d("DML", "onDownloadPaused===totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
        }

        @Override
        public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
            Log.d("DML", "onDownloadFailed==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
        }

        @Override
        public void onDownloadFinished(long totalBytes, String fileName, String appName) {
            Log.d("DML", "onDownloadFinished==totalBytes=" + totalBytes + ",fileName=" + fileName + ",appName=" + appName);
        }

        @Override
        public void onInstalled(String fileName, String appName) {
            Log.d("DML", "onInstalled==" + ",fileName=" + fileName + ",appName=" + appName);
        }
    }

    private static String getAdType(int type) {
        switch (type) {

            case TTAdConstant.AD_TYPE_COMMON_VIDEO:
                return "普通激励视频，type=" + type;

            case TTAdConstant.AD_TYPE_PLAYABLE_VIDEO:
                return "Playable激励视频，type=" + type;

            case TTAdConstant.AD_TYPE_PLAYABLE:
                return "纯Playable，type=" + type;

            case TTAdConstant.AD_TYPE_LIVE:
                return "直播流，type=" + type;
        }

        return "未知类型+type=" + type;
    }

    private void getExtraInfo() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        mHorizontalCodeId = intent.getStringExtra("code");
    }

    private void initViews() {

    }
}
