package com.iimm.miliao.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Code;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.CoreManager;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;

public class CommonalityTools {

    private static String TAG = "CommonalityTools";
    private static Button codeButton;
    public static CountDownTimer timer = new CountDownTimer(60 * 1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            codeButton.setText(millisUntilFinished / 1000 + "s重新获取");
        }

        @Override
        public void onFinish() {
            codeButton.setEnabled(true);
            codeButton.setText("获取验证码");
        }
    };
    public static String mRandCode;

    /**
     * 请求图形验证码
     * phone:区号+电话
     */
    public static void requestImageCode(Context mContext, CoreManager coreManager, String phone, ImageView mImageCodeIv) {

        String url = coreManager.getConfig().USER_GETCODE_IMAGE + "?telephone=" + phone;
        Log.d(TAG, "requestImageCode: " + url);
        Glide.with(mContext).load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        mImageCodeIv.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        Toast.makeText(mContext, R.string.tip_verification_code_load_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /*
     * 获取验证码
     * */
    public static void getCode(Context context, String phoneStr, String imageCodeStr, CoreManager coreManager, Button bt,String phoneCode) {
        codeButton = bt;
        Map<String, String> params = new HashMap<>();
        String language = Locale.getDefault().getLanguage();
        params.put("language", language);
        if (TextUtils.isEmpty(phoneCode)){
            params.put("areaCode", PreferenceUtils.getString(context, "areaCode"));
        }else {
            params.put("areaCode", phoneCode);
        }
        params.put("telephone", phoneStr);
        params.put("imgCode", imageCodeStr);
        params.put("isRegister", String.valueOf(0));
        params.put("version", "1");

        DialogHelper.showDefaulteMessageProgressDialog(context);

        HttpUtils.get().url(coreManager.getConfig().SEND_AUTH_CODE)
                .params(params)
                .build()
                .execute(new BaseCallback<Code>(Code.class) {

                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            Log.e(TAG, "onResponse: " + result.getData().getCode());
                            bt.setEnabled(false);
                            mRandCode = result.getData().getCode();// 记录验证码
                            // 开始倒计时
                            timer.start();
                        } else {
                            ToastUtil.showToast(context, TextUtils.isEmpty(result.getResultMsg()) ? "发送验证码失败！" : result.getResultMsg());

                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(context);
                        timer.cancel();
                        codeButton.setEnabled(true);
                    }
                });
    }

    /*
     * 取消时间计时器
     * */
    public static void closeTimer() {
        try {
            if (timer != null) {
                timer.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 改变String中某段字体颜色
     * SPAN_INCLUSIVE_EXCLUSIVE:包括开始下标，但不包括结束下标
     *SPAN_EXCLUSIVE_INCLUSIVE：不包括开始下标，但包括结束下标
     * SPAN_INCLUSIVE_INCLUSIVE：既包括开始下标，又包括结束下标
     * SPAN_EXCLUSIVE_EXCLUSIVE：不包括开始下标，也不包括结束下标
     * */
    public static SpannableString setStringAreaColor(Context context, String content, int star, int end, int colorid) {
        SpannableString spannableString = new SpannableString(content);
        spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(colorid)), star, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        return spannableString;
    }

    /***
     * 获取url 指定name的value;
     * @param url
     * @param name
     * @return
     */
    public static String getValueByName(String url, String name) {
        String result = "";
        int index = url.indexOf("?");
        String temp = url.substring(index + 1);
        String[] keyValue = temp.split("&");
        for (String str : keyValue) {
            if (str.contains(name)) {
                result = str.replace(name + "=", "");
                break;
            }
        }
        return result;
    }

    public static boolean isLetterDigit(String str) {
        String regex = "^[0-9]*$";
        boolean matches = str.matches(regex);

        return matches;
    }
}
