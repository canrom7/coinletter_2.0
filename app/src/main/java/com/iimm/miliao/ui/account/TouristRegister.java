package com.iimm.miliao.ui.account;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.github.faucamp.simplertmp.packets.ContentData;
import com.google.gson.Gson;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.LoginRegisterResult;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.helper.UsernameHelper;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.DeviceInfoUtil;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

public class TouristRegister {
    private static final String  TAG="TouristRegister";

    public void register(Activity context, CoreManager coreManager) {
        DialogHelper.showDefaulteMessageProgressDialog(context);
        String phoneNumber = RandomIDGenerator.generateRandomString(16);
        Log.i(TAG,"phoneNum: "+phoneNumber);
        Map<String, String> params = new HashMap<>();
        params.put("telephone", phoneNumber);
        params.put("areaCode", "" + 86);
        params.put("serial", DeviceInfoUtil.getDeviceId(context));
        params.put("registerType", "" + 1);

        HttpUtils.get().url(coreManager.getConfig().VERIFY_TELEPHONE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result == null) {
                            ToastUtil.showToast(context, R.string.data_exception);
                            return;
                        }

                        if (result.getResultCode() == 1) {
                            Log.i(TAG,"注册成功: ");
                            register2(context,coreManager,phoneNumber);
                        } else {
                            // 手机号已经被注册
                            if (!TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(context, result.getResultMsg());
                            } else {
                                ToastUtil.showToast(context, R.string.tip_server_error);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(context);
                    }
                });
    }




    private void register2(Context context,CoreManager coreManager,String mPhoneNum) {
        Map<String, String> params = new HashMap<>();
        String pwd= Md5Util.toMD5(RandomIDGenerator.generateRandomString(16));
        Log.i(TAG,"pwd="+pwd);


        // 前面页面传递的信息
        params.put("userType", "1");
        params.put("telephone", mPhoneNum);
        params.put("password", pwd);
        if (coreManager.getConfig().isQestionOpen) {
            String question="\"questions\":[{\"a\":\"001\"},{ \"a\":\"001\"},{\"a\":\"001\"}]";
            String json = new Gson().toJson(question);
           // params.put("questions", TextUtils.isEmpty(json) ? "" : json);
        }
        params.put("areaCode", "86");//TODO AreaCode 区号暂时不带
        String s=context.getString(R.string.register_tourist);
        String nickName=s+RandomIDGenerator.generateRandomString(10);
        // 本页面信息
        params.put("nickname", nickName);
        params.put("sex", "1");
        params.put("birthday", "1685200576");
        params.put("xmppVersion", "1");
        params.put("countryId", "0");
        params.put("provinceId", "0");
        params.put("cityId", "0");
        params.put("areaId", "0");
        params.put("registerType",   "1");

        params.put("isSmsRegister", String.valueOf(RegisterActivity.isSmsRegister));

        // 附加信息
        params.put("apiVersion", DeviceInfoUtil.getVersionCode(context) + "");
        params.put("model", DeviceInfoUtil.getModel());
        params.put("osVersion", DeviceInfoUtil.getOsVersion());
        params.put("serial", DeviceInfoUtil.getDeviceId(context));
        params.put("appBrand", DeviceInfoUtil.getBrand());////获取手机品牌
        if (!coreManager.getConfig().disableLocationServer) {
            // 地址信息
            double latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
            double longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();
            String location = MyApplication.getInstance().getBdLocationHelper().getAddress();
            if (latitude != 0) {
                params.put("latitude", String.valueOf(latitude));
            }
            if (longitude != 0) {
                params.put("longitude", String.valueOf(longitude));
            }
            if (!TextUtils.isEmpty(location)) {
                params.put("location", location);
            }
        }
        String url = coreManager.getConfig().USER_REGISTER;
        DialogHelper.showDefaulteMessageProgressDialog(context);
        HttpUtils.get().url(url)
                .params(params)
                .build()
                .execute(new BaseCallback<LoginRegisterResult>(LoginRegisterResult.class) {

                    @Override
                    public void onResponse(ObjectResult<LoginRegisterResult> result) {
                        DialogHelper.dismissProgressDialog();
                        if (!com.xuan.xuanhttplibrary.okhttp.result.Result.checkSuccess(context.getApplicationContext(), result)) {
                            if (result == null) {
                                Reporter.post("注册失败，result为空");
                            } else {
                                Reporter.post("注册失败" + result.toString());
                            }
                            return;
                        }

                        PreferenceUtils.putInt(context, AppConstant.LOGIN_TYPE, 1);
                        PreferenceUtils.putInt(context, AppConstant.LOGIN_other, 0);
                        PreferenceUtils.putBoolean(context, AppConstant.LOGINSTATU, true);


                        // 注册成功
                        boolean success = LoginHelper.setLoginUser(context, coreManager, mPhoneNum, pwd, result);
                        if (success) {
                            // 新注册的账号没有支付密码，
                            MyApplication.getInstance().initPayPassword(result.getData().getUserId(), 0);

                            ToastUtil.showToast(context, R.string.register_success);
                            int isupdate=result.getData().getIsupdate();
                            DataDownloadActivity.start(context, isupdate);
//                            if (mCurrentFile != null && mCurrentFile.exists()) {
//                                // 选择了头像，那么先上传头像
//                                uploadAvatar(result.getData().getIsupdate(), mCurrentFile);
//                            }

                        } else {
                            // 失败
                            if (TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(context, R.string.register_error);
                            } else {
                                ToastUtil.showToast(context, result.getResultMsg());
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(context);
                    }
                });
    }









    public static class RandomIDGenerator {

        private static final String ALLOWED_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        public static String generateRandomString(int length) {
            SecureRandom random = new SecureRandom();
            StringBuilder sb = new StringBuilder(length);

            // 生成至少一个数字
            int randomIndex = random.nextInt(10);
            char randomChar = ALLOWED_CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);

            // 生成至少一个字母
            randomIndex = random.nextInt(52) + 10; // 从字母字符开始
            randomChar = ALLOWED_CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);

            // 生成剩余字符
            for (int i = 2; i < length; i++) {
                randomIndex = random.nextInt(ALLOWED_CHARACTERS.length());
                randomChar = ALLOWED_CHARACTERS.charAt(randomIndex);
                sb.append(randomChar);
            }

            // 随机打乱字符串
            for (int i = length - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                char temp = sb.charAt(i);
                sb.setCharAt(i, sb.charAt(j));
                sb.setCharAt(j, temp);
            }

            return sb.toString();
        }
    }


}
