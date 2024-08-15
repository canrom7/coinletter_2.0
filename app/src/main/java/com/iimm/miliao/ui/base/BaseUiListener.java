package com.iimm.miliao.ui.base;

import com.iimm.miliao.MyApplication;
import com.iimm.miliao.util.ToastUtil;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

/**
 * MrLiu253@163.com
 *
 * @time 2019-07-26
 */
public class BaseUiListener implements IUiListener {

    @Override
    public void onComplete(Object response) {
        if (null == response) {
            ToastUtil.showToast(MyApplication.getInstance(), "返回为空登录失败");
            return;
        }
        JSONObject jsonResponse = (JSONObject) response;
        if (null != jsonResponse && jsonResponse.length() == 0) {
            ToastUtil.showToast(MyApplication.getInstance(), "返回为空登录失败");
            return;
        }
        ToastUtil.showToast(MyApplication.getInstance(), "授权成功");
        doComplete((JSONObject) response);
    }

    protected void doComplete(JSONObject values) {

    }

    @Override
    public void onError(UiError e) {
        ToastUtil.showToast(MyApplication.getInstance(), "授权出错" + e.errorCode);
    }

    @Override
    public void onCancel() {
        ToastUtil.showToast(MyApplication.getInstance(), "授权取消");
    }
}
