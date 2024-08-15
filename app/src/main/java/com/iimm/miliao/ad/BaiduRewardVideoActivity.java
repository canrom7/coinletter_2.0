package com.iimm.miliao.ad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baidu.mobads.sdk.api.ArticleInfo;
import com.baidu.mobads.sdk.api.BiddingListener;
import com.baidu.mobads.sdk.api.RequestParameters;
import com.baidu.mobads.sdk.api.RewardVideoAd;
import com.iimm.miliao.R;
import com.iimm.miliao.util.ToastUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;

/*
1. 激励视频集成参考类：RewardVideoActivity
2. 激励视频广告推荐缓存成功后再播放，避免卡顿。
   需提前预加载load，缓存成功后，调用show播放。
   可以通过isReady接口判断是否缓存成功。
3. 单次请求的广告不支持多次展现。下次展现前需要重新预加载，可以在点击关闭操作后重新预加载新广告。
4. 广告存在有效期，需要一定时间（2小时，该值为非固定值）内展现。可以通过isReady判断是否过期。
5. 监听展现失败onAdFailed做异常流程处理。
6. 检查API是否发生更改。
* */
public class BaiduRewardVideoActivity extends Activity {

    public static final String TAG = "RewardVideoActivity";
    // 线上广告位id
    private static String AD_PLACE_ID = "5989414";
    //    private static final String AD_PLACE_ID = "8264685";
    public RewardVideoAd mRewardVideoAd;
    // 测试环境的广告位id
    //    private String mAdPlaceId = "2411590";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_video);
        AD_PLACE_ID = getIntent().getStringExtra("code");
        initView();
        loadNextVideo();
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 激励视频加载监听
     */
    class CustomRewardListener implements RewardVideoAd.RewardVideoAdListener {

        @Override
        public void onVideoDownloadSuccess() {
            // 视频缓存成功
            // 建议：可以在收到该回调后，再调用show展示激励视频
            Log.i(TAG, "onVideoDownloadSuccess,isReady=" + mRewardVideoAd.isReady());
            showLoadedVideo();
        }

        @Override
        public void onVideoDownloadFailed() {
            // 视频缓存失败，可以在这儿重新load下一条广告，最好限制load次数（4-5次即可）。
            Log.i(TAG, "onVideoDownloadFailed");
            ToastUtil.showToast("baidu onVideoDownloadFailed  ==" +  " code=" + AD_PLACE_ID);
            setResult(RESULT_OK);
        }

        @Override
        public void playCompletion() {
            Log.i(TAG, "playCompletion");
            Intent intent = new Intent();
            intent.putExtra("isOk",true);
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void onRewardVerify(boolean rewardVerify) {
            // 激励视频奖励回调
            Log.i(TAG, "onRewardVerify: " + rewardVerify);
        }

        @Override
        public void onAdSkip(float playScale) {
            // 用户点击跳过, 展示尾帧
            Log.i(TAG, "onSkip: " + playScale);
            finish();
        }

        @Override
        public void onAdLoaded() {

            // 请求成功回调
            Log.i(TAG, "onAdLoaded");
            // 【可选】模拟竞价，广告返回后，媒体可以自行进行客户端竞价，反馈竞价结果
        }

        @Override
        public void onAdShow() {
            // 视频开始播放时候的回调
            Log.i(TAG, "onAdShow");
        }

        @Override
        public void onAdClick() {
            // 广告被点击的回调
            Log.i(TAG, "onAdClick");
        }

        @Override
        public void onAdClose(float playScale) {
            // 用户关闭了广告，直到开始下一次load前，将不会再收到任何回调
            // 说明：关闭按钮在mssp上可以动态配置，媒体通过mssp配置，可以选择广告一开始就展示关闭按钮，还是播放结束展示关闭按钮
            // 建议：收到该回调之后，可以重新load下一条广告,最好限制load次数（4-5次即可）
            // playScale[0.0-1.0],1.0表示播放完成，媒体可以按照自己的设计给予奖励
            Log.i(TAG, "onAdClose" + playScale);
            finish();
        }

        @Override
        public void onAdFailed(String arg0) {
            // 广告失败回调，直到开始下一次load前，将不会再收到任何回调
            // 失败可能原因：广告内容填充为空；网络原因请求广告超时等
            // 建议：收到该回调之后，可以重新load下一条广告，最好限制load次数（4-5次即可）
            Log.i(TAG, "onAdFailed" + arg0);
            ToastUtil.showToast("baidu onAdFailed  ==" + arg0+ " code=" + AD_PLACE_ID);
            setResult(RESULT_OK);
        }
    }

    /**
     * 请求加载下一条激励视频广告
     */
    private void loadNextVideo() {
        // 激励视屏产品可以选择是否使用SurfaceView进行渲染视频
        mRewardVideoAd = new RewardVideoAd(BaiduRewardVideoActivity.this,
                AD_PLACE_ID, new CustomRewardListener());
        mRewardVideoAd.setUserId("user123456");
        mRewardVideoAd.setExtraInfo("aa?=bb&cc?=dd");
        // 【可选】【Bidding】设置广告的底价，单位：分
        mRewardVideoAd.setBidFloor(100);
        // 自定义传参
        final RequestParameters requestParameters = new RequestParameters.Builder()
                /**
                 * 【激励视频传参】传参功能支持的参数见ArticleInfo类，各个参数字段的描述和取值可以参考如下注释
                 * 注意：所有参数的总长度(不包含key值)建议控制在150字符内，避免因超长发生截断，影响信息的上报
                 */
                // 通用信息：用户性别，取值：0-unknown，1-male，2-female
                .addCustExt(ArticleInfo.USER_SEX, "1")
                // 最近阅读：小说、文章的名称
                .addCustExt(ArticleInfo.PAGE_TITLE, "测试书名")
                // 自定义传参，参考如下接入
                .addCustExt("cust_这是Key", "cust_这是Value" + System.currentTimeMillis())
                .addCustExt("Key2", "Value2")
                .build();
        // 若传参，如下设置
        mRewardVideoAd.setRequestParameters(requestParameters);
        // 请求广告，展示前必须调用
        mRewardVideoAd.load();
    }

    /**
     * 展示已加载的激励视频广告，需要提前进行load
     */
    private void showLoadedVideo() {
        if (mRewardVideoAd == null ) {
            toastText("请在加载成功后进行广告展示！");
            return;
        }
        // 1. 强烈建议在收到onVideoDownloadSuccess回调、视频物料缓存完成后再展示广告，
        //    提升激励视频的播放体验，否则有播放卡顿的风险。
        // 2. 在展示前可以调用isReady接口判断广告是否就绪：
        //    此接口会判断本地是否存在【未展示 & 未过期 & 已缓存】的广告
        if (!mRewardVideoAd.isReady()) {
            toastText("视频广告未缓存/已展示/已过期");
            return;
        }
        // 是否在跳过按钮后展示弹框 (默认点击跳过不展示弹框) , 可在广告配置页面配置
        mRewardVideoAd.setShowDialogOnSkip(false);
        // show之前必须调用load请求广告，否则无效
        mRewardVideoAd.show();
    }

    private void initView() {
    }

    private void toastText(String text) {
        Toast.makeText(BaiduRewardVideoActivity.this, text, Toast.LENGTH_SHORT).show();
    }


    /**
     * 竞价结果回传
     */
    private void clientBidding() {

        Log.e(TAG, "ecpm=" + mRewardVideoAd.getECPMLevel());
        // 媒体自行设置竞价逻辑，并根据竞价结果上报
        String biddingPrice = mRewardVideoAd.getECPMLevel();
        boolean biddingResult = false;
        // 模拟竞价
        int firstPrice = 200;
        if (!TextUtils.isEmpty(biddingPrice)) {
            biddingResult = Integer.parseInt(biddingPrice) > firstPrice;
        } else {
            // 取竞胜价格
            biddingPrice = String.valueOf(firstPrice);
        }

        if (biddingResult) {
            // 百青藤竞胜，排名第二的竞价方信息
            LinkedHashMap<String, Object> secondInfo = new LinkedHashMap<>();
            // 百青藤竞胜，各家比价广告中，排名第二的价格，如果百青藤竞胜，则回传百青藤的二价即可
            secondInfo.put("ecpm", biddingPrice);
            // 竞价排名第二的DSP id，参考文档获取
            secondInfo.put("adn", 1);
            // 竞价排名第二的物料类型，参考文档获取
            secondInfo.put("ad_t", 3);
            // 竞价排名第二的广告主名称，物料中获取
            secondInfo.put("ad_n", "广告主名称");
            // 竞价时间，秒级时间戳
            secondInfo.put("ad_time", System.currentTimeMillis() / 1000);
            // 竞价排名第二的DSP的竞价类型，（百度竞价结果参数：1：分层保价；2：价格标签；3：bidding;4:其他)
            secondInfo.put("bid_t", 1);
            // 竞价排名第二的广告主标题，物料中获取
            secondInfo.put("ad_ti", "title");

            // 调用反馈竞价成功及二价
            BiddingListener winBiddingListener = new BiddingListener() {
                @Override
                public void onBiddingResult(boolean result, String message, HashMap<String, Object> ext) {
                    Log.i(TAG, "onBiddingResult-win: " + result + " msg信息：" + message);
                }
            };
            mRewardVideoAd.biddingSuccess(secondInfo, winBiddingListener);
        } else {
            // 竞胜方信息
            LinkedHashMap<String, Object> winInfo = new LinkedHashMap<>();
            // 竞胜方出价
            winInfo.put("ecpm", biddingPrice);
            // 竞胜方的DSP id，参考文档获取
            winInfo.put("adn", 1);
            // 竞胜方的物料类型，参考文档获取
            winInfo.put("ad_t", 3);
            // 竞胜方的广告主名称，物料中获取
            winInfo.put("ad_n", "广告主名称");
            // 竞价时间，秒级时间戳
            winInfo.put("ad_time", System.currentTimeMillis() / 1000);
            // 竞价类型，（百度竞价结果参数：1：分层保价；2：价格标签；3：bidding;4:其他)
            winInfo.put("bid_t", 3);
            // 竞价失败原因，203：输给其他竞价方，其他见联盟文档平台
            winInfo.put("reason", 203);
            // 指的是此次竞胜pv是否曝光，而非百青藤广告是否曝光
            winInfo.put("is_s", 1);
            // 指的是此次竞胜pv是否点击，而非局限于百青藤广告是否点击
            winInfo.put("is_c", 0);
            // 竞价排名第二的广告主标题，物料中获取
            winInfo.put("ad_ti", "title");

            // 调用反馈竞价失败及原因
            BiddingListener lossBiddingListener = new BiddingListener() {
                @Override
                public void onBiddingResult(boolean result, String message, HashMap<String, Object> ext) {
                    Log.i(TAG, "onBiddingResult-loss: " + result + " msg信息：" + message);
                }
            };
            mRewardVideoAd.biddingFail(winInfo, lossBiddingListener);
        }
    }
}