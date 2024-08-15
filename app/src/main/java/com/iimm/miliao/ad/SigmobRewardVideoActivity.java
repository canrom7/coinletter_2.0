package com.iimm.miliao.ad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.iimm.miliao.R;
import com.iimm.miliao.util.ToastUtil;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.rewardVideo.WindRewardAdRequest;
import com.sigmob.windad.rewardVideo.WindRewardInfo;
import com.sigmob.windad.rewardVideo.WindRewardVideoAd;
import com.sigmob.windad.rewardVideo.WindRewardVideoAdListener;

import java.util.HashMap;
import java.util.Map;

public class SigmobRewardVideoActivity extends Activity {

    private WindRewardVideoAd windRewardedVideoAd;
    private String placementId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_video);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        placementId = getIntent().getStringExtra("code");

        Map<String, Object> options = new HashMap<>();
        options.put("user_id", getString(R.string.app_name));
        windRewardedVideoAd = new WindRewardVideoAd(new WindRewardAdRequest(placementId, getString(R.string.app_name), options));
        windRewardedVideoAd.setWindRewardVideoAdListener(new WindRewardVideoAdListener() {
            @Override
            public void onRewardAdLoadSuccess(final String placementId) {
                Log.d("windSDK", "------onRewardAdLoadSuccess------" + placementId);
                logCallBack("onRewardAdLoadSuccess", "");
                HashMap option = new HashMap();
                option.put(WindAds.AD_SCENE_ID, "");
                option.put(WindAds.AD_SCENE_DESC, "");
                if (windRewardedVideoAd != null && windRewardedVideoAd.isReady()) {
                    windRewardedVideoAd.show(option);
                }
            }

            @Override
            public void onRewardAdPreLoadSuccess(String s) {
                Log.d("windSDK", "------onRewardAdPreLoadSuccess------" + placementId);
                logCallBack("onRewardAdPreLoadSuccess", "");
            }

            @Override
            public void onRewardAdPreLoadFail(String s) {
                ToastUtil.showToast("windSDK onNoAD  ==" + s + " code=" + placementId);
                Log.d("windSDK", "------onRewardAdPreLoadFail------" + placementId);
                logCallBack("onRewardAdPreLoadFail", "");
            }

            @Override
            public void onRewardAdPlayEnd(final String placementId) {
                Log.d("windSDK", "------onRewardAdPlayEnd------" + placementId);
                logCallBack("onRewardAdPlayEnd", "");
                Intent intent = new Intent();
                intent.putExtra("isOk",true);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onRewardAdPlayStart(final String placementId) {
                Log.d("windSDK", "------onRewardAdPlayStart------" + placementId);
                logCallBack("onRewardAdPlayStart", "");
            }

            @Override
            public void onRewardAdClicked(final String placementId) {
                Log.d("windSDK", "------onRewardAdClicked------" + placementId);
                logCallBack("onRewardAdClicked", "");
            }

            @Override
            public void onRewardAdClosed(final String placementId) {
                Log.d("windSDK", "------onRewardAdClosed------" + placementId);
                logCallBack("onRewardAdClosed", "");
                finish();
            }

            @Override
            public void onRewardAdRewarded(WindRewardInfo rewardInfo, String placementId) {
                Log.d("windSDK", "------onRewardAdRewarded------" + rewardInfo.toString() + ":" + placementId);
                logCallBack("onRewardAdRewarded", rewardInfo.toString());

            }

            @Override
            public void onRewardAdLoadError(WindAdError error, String placementId) {
                ToastUtil.showToast("windSDK error  ==" + error.getMessage() + "; code=" + placementId);
                Log.d("windSDK", "------onRewardAdLoadError------" + error.toString() + ":" + placementId);
                logCallBack("onRewardAdLoadError", error.toString());
                setResult(RESULT_OK);

            }

            @Override
            public void onRewardAdPlayError(WindAdError error, String placementId) {
                ToastUtil.showToast("windSDK Play error  ==" + error.getMessage() + "; code=" + placementId);
                Log.d("windSDK", "------onRewardAdPlayError------" + error.toString() + ":" + placementId);
                logCallBack("onRewardAdPlayError", error.toString());
            }


        });

        if (windRewardedVideoAd != null) {
            windRewardedVideoAd.loadAd();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (windRewardedVideoAd != null) {
            windRewardedVideoAd.destroy();
            windRewardedVideoAd = null;
        }
    }


    private void resetCallBackData() {
    }

    private void logCallBack(String call, String child) {
    }

}