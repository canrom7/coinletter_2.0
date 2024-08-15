package com.iimm.miliao.ad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.iimm.miliao.R;
import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.ads.rewardvideo.ServerSideVerificationOptions;
import com.qq.e.comm.util.AdError;

import java.util.Locale;
import java.util.Map;

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
public class GdtRewardVideoActivity extends Activity implements RewardVideoADListener {

    public static final String TAG = "RewardVideoActivity";
    // 线上广告位id
    private static String AD_PLACE_ID = "5989414";
    //    private static final String AD_PLACE_ID = "8264685";
    public RewardVideoAD rewardVideoAD;
    private  boolean adLoaded;
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
     * 请求加载下一条激励视频广告
     */
    private void loadNextVideo() {
        rewardVideoAD = new RewardVideoAD(this, AD_PLACE_ID, this); // 有声播放
        rewardVideoAD.loadAD();
    }


    private void initView() {
    }

    private void toastText(String text) {
        Toast.makeText(GdtRewardVideoActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    // 2.设置监听器，监听广告状态

    /**
     * 广告加载成功，可在此回调后进行广告展示
     **/
    @Override
    public void onADLoad() {
        adLoaded = true;
        String msg = "load ad success ! ";
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        if (adLoaded) {//广告展示检查1：广告成功加载，此处也可以使用videoCached来实现视频预加载完成后再展示激励视频广告的逻辑
            if (!rewardVideoAD.hasShown()) {//广告展示检查2：当前广告数据还没有展示过
                //广告展示检查3：展示广告前判断广告数据未过期
                if (rewardVideoAD.isValid()) {
                    rewardVideoAD.showAD();
                } else {
                    Toast.makeText(this, "激励视频广告已过期，请再次请求广告后进行广告展示！", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "此条广告已经展示过，请再次请求广告后进行广告展示！", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "成功加载广告后再进行广告展示！", Toast.LENGTH_LONG).show();
        }


    }

    /**
     * 视频素材缓存成功，可在此回调后进行广告展示
     */
    @Override
    public void onVideoCached() {
        Log.i(TAG, "onVideoCached");
    }

    /**
     * 激励视频广告页面展示
     */
    @Override
    public void onADShow() {
        Log.i(TAG, "onADShow");
    }

    /**
     * 激励视频广告曝光
     */
    @Override
    public void onADExpose() {
        Log.i(TAG, "onADExpose");
    }

    /**
     * 激励视频触发激励（观看视频大于一定时长或者视频播放完毕）
     *
     * @param map 若选择了服务端验证，可以通过 ServerSideVerificationOptions#TRANS_ID 键从 map 中获取此次交易的 id；若未选择服务端验证，则不需关注 map 参数。
     */
    @Override
    public void onReward(Map<String, Object> map) {
        Log.i(TAG, "onReward " + map.get(ServerSideVerificationOptions.TRANS_ID));  // 获取服务端验证的唯一 ID
    }

    /**
     * 激励视频广告被点击
     */
    @Override
    public void onADClick() {
        Log.i(TAG, "onADClick");
    }

    /**
     * 激励视频播放完毕
     */
    @Override
    public void onVideoComplete() {
        Log.i(TAG, "onVideoComplete");
        Intent intent = new Intent();
        intent.putExtra("isOk",true);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 激励视频广告被关闭
     */
    @Override
    public void onADClose() {
        Log.i(TAG, "onADClose");
        finish();
    }

    /**
     * 广告流程出错
     */
    @Override
    public void onError(AdError adError) {
        String msg = String.format(Locale.getDefault(), "onError, error code: %d, error msg: %s",
                adError.getErrorCode(), adError.getErrorMsg());
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        setResult(RESULT_OK);
    }

}