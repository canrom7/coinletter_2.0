package com.iimm.miliao.ui.account;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.dhh.easy.chat.wxapi.WXEntryActivity;
import com.iimm.miliao.AppConfig;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.BuildConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.adapter.MessageLogin;
import com.iimm.miliao.adapter.NodeAdapter;
import com.iimm.miliao.bean.LoginRegisterResult;
import com.iimm.miliao.bean.NodeInfo;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.helper.PasswordHelper;
import com.iimm.miliao.helper.PrivacySettingHelper;
import com.iimm.miliao.helper.UsernameHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.BaseUiListener;
import com.iimm.miliao.ui.me.SetConfigActivity;
import com.iimm.miliao.util.AppUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DeviceInfoUtil;
import com.iimm.miliao.util.EventBusHelper;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.view.TitleErrorDialog;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 登陆界面
 *
 * @author Dean Tao
 * @version 1.0
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private EditText mPhoneNumberEdit;
    private EditText mPasswordEdit;
    private TextView tv_prefix;
    private View ll_login_node;
    private TextView tv_login_node;
    private int mobilePrefix = 86;
    private Tencent mTencent;
    private String mOpenID;//QQ的是OpenId,微信的是code
    private String mType;//1QQ   2微信    3支付宝
    private String mQQToken;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    private Button forgetPasswordBtn, registerBtn, loginBtn, switch_btn,btTourist;
    private NodeAdapter mNodeAdapter;
    private List<NodeInfo> mNodeList = new ArrayList<>();
    private PopupWindow popupWindow;
    public static String LOGIN_NODE_INFO = "node_info";
    private int loginType = 1;//1:用户名密码 0：手机号

    public LoginActivity() {
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initActionBar();
        initView();
        IntentFilter filter = new IntentFilter();
        filter.addAction("CHANGE_CONFIG");
        registerReceiver(broadcastReceiver, filter);
        EventBusHelper.register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 如果没有保存用户定位信息，那么去地位用户当前位置
        if (!coreManager.getConfig().disableLocationServer && !MyApplication.getInstance().getBdLocationHelper().isLocationUpdate()) {
            MyApplication.getInstance().getBdLocationHelper().requestLocation();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.login));
        TextView tvRight = (TextView) findViewById(R.id.tv_title_right);
        if (BuildConfig.LOG_DEBUG) {
            tvRight.setVisibility(View.VISIBLE);
        } else {
            tvRight.setVisibility(View.GONE);
        }
        //隐藏
        tvRight.setVisibility(View.GONE);

        tvRight.setText(R.string.settings_server_address);
        tvRight.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SetConfigActivity.class);
            startActivity(intent);
        });
    }

    private void initView() {
        mPhoneNumberEdit = (EditText) findViewById(R.id.phone_numer_edit);
        mPasswordEdit = (EditText) findViewById(R.id.password_edit);
        ll_login_node = findViewById(R.id.ll_login_node);
        tv_login_node = findViewById(R.id.tv_login_node);
        switch_btn = findViewById(R.id.switch_btn);
        if (Constants.SUPPORT_MANUAL_NODE){
            //        if (coreManager.getConfig().isNodesStatus == Constants.LOGIN_NODE_SUPPORT) {
            if (coreManager.getConfig().nodesInfoList != null && coreManager.getConfig().nodesInfoList.size() > 0) {
                ll_login_node.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(PreferenceUtils.getString(LoginActivity.this, AppConstant.VALID_XMPP_NAME, ""))) {
                    tv_login_node.setText(PreferenceUtils.getString(LoginActivity.this, AppConstant.VALID_XMPP_NAME, ""));
                } else {
                    if (coreManager.getConfig().nodesInfoList != null && coreManager.getConfig().nodesInfoList.get(0) != null && !TextUtils.isEmpty(coreManager.getConfig().nodesInfoList.get(0).getNodeName())) {
                        tv_login_node.setText(coreManager.getConfig().nodesInfoList.get(0).getNodeName());
                    }
                }

            } else {
                ll_login_node.setVisibility(View.GONE);
            }
//        } else {
//            ll_login_node.setVisibility(View.GONE);
//        }
        }else {
            ll_login_node.setVisibility(View.GONE);
        }
        ll_login_node.setOnClickListener(this);
        PasswordHelper.bindPasswordEye(mPasswordEdit, findViewById(R.id.tbEye));
        tv_prefix = (TextView) findViewById(R.id.tv_prefix);
        if (coreManager.getConfig().registerUsername == 2) {
            switch_btn.setText("手机号登录");
            switch_btn.setVisibility(View.VISIBLE);
        } else {
            switch_btn.setVisibility(View.GONE);
        }
        if (coreManager.getConfig().registerUsername == 1 || coreManager.getConfig().registerUsername == 2) {
            loginType = 1;
            tv_prefix.setVisibility(View.GONE);
        } else {
            loginType = 0;
            tv_prefix.setOnClickListener(this);
        }
        mobilePrefix = PreferenceUtils.getInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        tv_prefix.setText("+" + mobilePrefix);
        tv_prefix.setOnClickListener(this);
        // 登陆账号
        loginBtn = (Button) findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(this);
        // 注册账号
        registerBtn = (Button) findViewById(R.id.register_account_btn);
        btTourist = (Button) findViewById(R.id.register_tourist);
        registerBtn.setOnClickListener(this);
        btTourist.setOnClickListener(this);
        //是否开放注册
        if (coreManager.getConfig().isOpenRegister) {
            registerBtn.setVisibility(View.VISIBLE);
        } else {
            registerBtn.setVisibility(View.GONE);
        }
        // 忘记密码
        forgetPasswordBtn = (Button) findViewById(R.id.forget_password_btn);
        if (loginType == 1 && !coreManager.getConfig().isQestionOpen) {
            forgetPasswordBtn.setVisibility(View.GONE);
        }
        forgetPasswordBtn.setOnClickListener(this);
        UsernameHelper.initEditText(mPhoneNumberEdit, loginType);
        loginBtn.setText(InternationalizationHelper.getString("JX_Login"));
        switch_btn.setOnClickListener(this);
        findViewById(R.id.main_content).setOnClickListener(this);

        if (coreManager.getConfig().wechatLoginStatus == 1) {
            findViewById(R.id.wx_login_btn).setVisibility(View.VISIBLE);
            findViewById(R.id.wx_login_btn).setOnClickListener(this);
        } else {
            findViewById(R.id.wx_login_btn).setVisibility(View.GONE);
        }
        if (coreManager.getConfig().qqLoginStatus == 1) {
            findViewById(R.id.qq_login_btn).setVisibility(View.VISIBLE);
            findViewById(R.id.qq_login_btn).setOnClickListener(this);
        } else {
            findViewById(R.id.qq_login_btn).setVisibility(View.GONE);
        }
        if (coreManager.getConfig().qqLoginStatus == 1 || coreManager.getConfig().wechatLoginStatus == 1) {
            findViewById(R.id.prompt_login).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.prompt_login).setVisibility(View.GONE);
        }
    }

    /**
     * 登录节点选择
     *
     * @param v
     */
    public void showPopUpWindow(View v) {
        // 找到布局文件
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.layout_list_pop, null);
        // 实例化listView
        ListView listView = (ListView) layout.findViewById(R.id.listView_view_node);
        mNodeList.clear();
        mNodeList.addAll(coreManager.getConfig().nodesInfoList);
        mNodeAdapter = new NodeAdapter(this, mNodeList);
        // 设置recyclerView的适配器
        listView.setAdapter(mNodeAdapter);
        if (popupWindow == null) {
            // 实例化一个PopuWindow对象
            popupWindow = new PopupWindow(v,
                    v.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        }
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        // 设置点击弹框外部，弹框消失
        popupWindow.setOutsideTouchable(true);
        // 设置焦点
        popupWindow.setFocusable(true);
        // 设置所在布局
        popupWindow.setContentView(layout);
        // 设置弹框出现的位置，在v的正下方横轴偏移textview的宽度
        popupWindow.showAsDropDown(v, 0, 0);
        // listView的item点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                NodeInfo nodeInfo = mNodeList.get(position);
                if (nodeInfo == null) {
                    return;
                }
                String nodeName = nodeInfo.getNodeName();
                String nodeIp = nodeInfo.getNodeIp();
                String nodePort = nodeInfo.getNodePort();
                if (!TextUtils.isEmpty(nodeIp)) {
                    MyApplication.validXmppHost = nodeIp;
                    PreferenceUtils.putString(LoginActivity.this, AppConstant.VALID_XMPP_HOST, nodeIp);
                }
                if (!TextUtils.isEmpty(nodePort)) {
                    try {
                        int xmppPort = Integer.parseInt(nodePort);
                        MyApplication.validXmppPort = xmppPort;
                        PreferenceUtils.putInt(LoginActivity.this, AppConstant.VALID_XMPP_PORT, xmppPort);
                    } catch (Exception e) {
                        PreferenceUtils.putInt(LoginActivity.this, AppConstant.VALID_XMPP_PORT, AppConfig.mXMPPPort);
                    }
                }
                if (!TextUtils.isEmpty(nodeName)) {
                    PreferenceUtils.putString(LoginActivity.this, AppConstant.VALID_XMPP_NAME, nodeName);
                }
                if (!TextUtils.isEmpty(nodeName)) {
                    tv_login_node.setText(nodeName);
                }
                // 弹框消失
                popupWindow.dismiss();
                popupWindow = null;
                String nodeInfoStr = JSON.toJSONString(nodeInfo);
                PreferenceUtils.putString(LoginActivity.this, LOGIN_NODE_INFO, nodeInfoStr);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        switch (v.getId()) {
            case R.id.tv_prefix:// 选择国家区号
                Intent intent = new Intent(this, SelectPrefixActivity.class);
                startActivityForResult(intent, SelectPrefixActivity.REQUEST_MOBILE_PREFIX_LOGIN);
                break;
            case R.id.login_btn:// 登陆
                if (verification()) {
                    login(false);
                }
                break;
            case R.id.wx_login_btn://微信登陆
                if (!AppUtils.isAppInstalled(mContext, "com.tencent.mm")) {
                    ToastUtil.showToast(mContext, getString(R.string.tip_no_wx_chat));
                } else {
                    blanKing();
                    WXEntryActivity.wxLogin(this, coreManager.getConfig().wechatAppId);
                }
                break;
            case R.id.qq_login_btn://QQ登陆
                blanKing();
                loginQQ();
                break;
            case R.id.register_account_btn:// 注册
                register();
                break;
            case R.id.register_tourist:// 游客
                new TouristRegister().register(this,coreManager);
                break;
            case R.id.forget_password_btn:// 忘记密码
                Intent intent1 = new Intent(mContext, FindPwdActivity.class);
                intent1.putExtra("type", loginType);
                startActivity(intent1);
                break;
            case R.id.switch_btn:
                if (loginType == 1) {
                    loginType = 0;
                    tv_prefix.setVisibility(View.VISIBLE);
                    switch_btn.setText("用户名登录");
                    if (forgetPasswordBtn.getVisibility() == View.GONE) {
                        forgetPasswordBtn.setVisibility(View.VISIBLE);
                    }
                } else {
                    loginType = 1;
                    if (!coreManager.getConfig().isQestionOpen) {
                        forgetPasswordBtn.setVisibility(View.GONE);
                    }
                    tv_prefix.setVisibility(View.GONE);
                    switch_btn.setText("手机号登录");
                }
                mPhoneNumberEdit.setText("");
                mPasswordEdit.setText("");
                UsernameHelper.initEditText(mPhoneNumberEdit, loginType);
                break;
            case R.id.main_content:// 点击空白区域隐藏软键盘
                InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (inputManager != null) {
                    inputManager.hideSoftInputFromWindow(findViewById(R.id.main_content).getWindowToken(), 0); //强制隐藏键盘
                }
                break;
            case R.id.ll_login_node://设置节点
                if (coreManager.getConfig().isNodesStatus == Constants.LOGIN_NODE_SUPPORT) {
                    if (coreManager.getConfig().nodesInfoList != null && coreManager.getConfig().nodesInfoList.size() > 0) {
                        showPopUpWindow(tv_login_node);
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == com.tencent.connect.common.Constants.REQUEST_LOGIN ||
                requestCode == com.tencent.connect.common.Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode, resultCode, data, loginListener);
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == SelectPrefixActivity.RESULT_MOBILE_PREFIX_SUCCESS && data != null) {
            mobilePrefix = data.getIntExtra(Constants.MOBILE_PREFIX, 86);
            tv_prefix.setText("+" + mobilePrefix);
        }
    }

    //验证账号密码是否输入
    private boolean verification() {
        if (TextUtils.isEmpty(mPhoneNumberEdit.getText().toString().trim()) && TextUtils.isEmpty(mPasswordEdit.getText().toString().trim())) {
            ToastUtil.showToast(mContext, getString(R.string.please_input_account_and_password));
            return false;
        } else if (TextUtils.isEmpty(mPhoneNumberEdit.getText().toString().trim())) {
            ToastUtil.showToast(mContext, getString(R.string.please_input_account));
            return false;
        } else if (TextUtils.isEmpty(mPasswordEdit.getText().toString().trim())) {
            ToastUtil.showToast(mContext, InternationalizationHelper.getString("JX_InputPassWord"));
            return false;
        }
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageLogin message) {
        finish();
    }

    //微信授权成功回调
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(LoginWXBean loginWXBean) {
        mOpenID = loginWXBean.wxCode;
        mType = "2";
        login(true);
    }

    public void loginQQ() {
        mTencent = Tencent.createInstance(coreManager.getConfig().qqLoginAppId, this);
        mTencent.login(this, "all", loginListener);
    }

    IUiListener loginListener = new BaseUiListener() {
        @Override
        protected void doComplete(JSONObject jsonObject) {
            try {
                String qQToken = jsonObject.getString(com.tencent.connect.common.Constants.PARAM_ACCESS_TOKEN);
                String expires = jsonObject.getString(com.tencent.connect.common.Constants.PARAM_EXPIRES_IN);
                String openId = jsonObject.getString(com.tencent.connect.common.Constants.PARAM_OPEN_ID);
                if (!TextUtils.isEmpty(qQToken) && !TextUtils.isEmpty(expires)
                        && !TextUtils.isEmpty(openId)) {
                    mTencent.setAccessToken(qQToken, expires);
                    mTencent.setOpenId(openId);
                    mOpenID = openId;
                    mQQToken = qQToken;
                    mType = "1";
                    login(true);
                }
            } catch (Exception e) {
            }
        }
    };

    private void register() {
        RegisterActivity.registerFromThird(
                this,
                mobilePrefix,
                mPhoneNumberEdit.getText().toString(),
                mPasswordEdit.getText().toString(),
                loginType

        );
    }

    /**
     * @param third 第三方自动登录， true
     */
    private void login(boolean third) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        PreferenceUtils.putInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        final String phoneNumber = !third ? mPhoneNumberEdit.getText().toString().trim() : "暂无";
        String password = !third ? mPasswordEdit.getText().toString().trim() : "暂无";
        // 加密后的手机号码
        String digestPhoneNumber = "";
        if (loginType == 0) {
            digestPhoneNumber = Md5Util.toMD5(phoneNumber);
        } else {
            digestPhoneNumber = Md5Util.toMD5(phoneNumber);
        }
        // 加密之后的密码
        final String digestPwd = Md5Util.toMD5(password);
        // 地址信息
        double latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
        double longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();

        Map<String, String> params = new ArrayMap<>();
        params.put("xmppVersion", "1");
        params.put("model", DeviceInfoUtil.getModel());//// 附加信息+
        params.put("osVersion", DeviceInfoUtil.getOsVersion());
        params.put("serial", DeviceInfoUtil.getDeviceId(mContext));
        params.put("appBrand", DeviceInfoUtil.getBrand());////获取手机品牌
        // 服务端集群需要
        if (MyApplication.IS_OPEN_CLUSTER) {
            String area = PreferenceUtils.getString(this, AppConstant.EXTRA_CLUSTER_AREA);
            if (!TextUtils.isEmpty(area)) {
                params.put("area", area);
            }
        }
        if (latitude != 0) {
            params.put("latitude", String.valueOf(latitude));
        }
        if (longitude != 0) {
            params.put("longitude", String.valueOf(longitude));
        }
        String url;
        //新接口第三方登陆不需要这些数据
        if (!third) {
            url = coreManager.getConfig().USER_LOGIN;
            params.put("areaCode", String.valueOf(mobilePrefix));
            params.put("telephone", digestPhoneNumber); // 账号登陆的时候需要MD5加密，服务器需求
            params.put("password", digestPwd);// 账号登陆的时候需要MD5加密，服务器需求
            params.put("registerType", loginType + "");
        } else {
            loginType = 7;
            url = coreManager.getConfig().USER_THIRD_LOGIN_NEW;
            params.put("otherType", mType);
            params.put("code", mOpenID);
            if (mType.equals("1")) {
                params.put("otherToken", mQQToken);
            }
        }
        HttpUtils.get().url(url)
                .params(params)
                .build()
                .execute(new BaseCallback<LoginRegisterResult>(LoginRegisterResult.class) {

                    @Override
                    public void onResponse(com.xuan.xuanhttplibrary.okhttp.result.ObjectResult<LoginRegisterResult> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            //如果需要邀请码，并且第三登陆时，没有邀请码，需要填写才能进入App
                            if (coreManager.getConfig().registerInviteCode == 1 && result.getData().getInviteStatus() == 2) {
                                inputInviteCode(phoneNumber, digestPwd, result);
                            } else if (coreManager.getConfig().registerInviteCode == 2 && result.getData().getRegisterStatus() == 1) {
                                inputInviteCode(phoneNumber, digestPwd, result);
                            } else {
                                loginSuccess(phoneNumber, digestPwd, result);
                            }
                        } else {
                            String message = TextUtils.isEmpty(result.getResultMsg()) ? getString(R.string.tip_incomplete_information) : result.getResultMsg();
                            ToastUtil.showToast(mContext, message);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private void loginSuccess(String phoneNumber, String digestPwd, com.xuan.xuanhttplibrary.okhttp.result.ObjectResult<LoginRegisterResult> result) {
        boolean success = LoginHelper.setLoginUser(mContext, coreManager, phoneNumber, digestPwd, result);
        if (success) {
            PreferenceUtils.putString(mContext, "areaCode", String.valueOf(mobilePrefix));
            if(loginType==7) {
                PreferenceUtils.putInt(mContext, AppConstant.LOGIN_other, loginType);
            }else {
                PreferenceUtils.putInt(mContext, AppConstant.LOGIN_other, 0);
                PreferenceUtils.putInt(mContext, AppConstant.LOGIN_TYPE, loginType);
            }
            PreferenceUtils.putBoolean(mContext, AppConstant.LOGINSTATU, true);
            LoginRegisterResult.Settings settings = result.getData().getSettings();
            MyApplication.getInstance().initPayPassword(result.getData().getUserId(), result.getData().getPayPassword());
            PrivacySettingHelper.setPrivacySettings(LoginActivity.this, settings);
            MyApplication.getInstance().initMulti();

            DataDownloadActivity.start(mContext, result.getData().getIsupdate());
            finish();
        } else { //  登录出错 || 用户资料不全
            String message = TextUtils.isEmpty(result.getResultMsg()) ? getString(R.string.tip_incomplete_information) : result.getResultMsg();
            ToastUtil.showToast(mContext, message);
        }
    }

    TitleErrorDialog mVerifyDialog;

    private void inputInviteCode(String phoneNumber, String digestPwd, com.xuan.xuanhttplibrary.okhttp.result.ObjectResult<LoginRegisterResult> result) {
        String title = coreManager.getConfig().registerInviteCode == 2 ? "请填写邀请码" : "初次登录必须填写邀请码";
        String txt = coreManager.getConfig().registerInviteCode == 2 ? "请输入邀请码(选填)" : "请输入邀请码";
        mVerifyDialog = DialogHelper.inputTitle(LoginActivity.this, title, txt, new TitleErrorDialog.VerifyClickListener() {
            @Override
            public void cancel() {
                if (coreManager.getConfig().registerInviteCode == 2) {
                    loginSuccess(phoneNumber, digestPwd, result);
                } else {
                    blanKing();
                }
            }

            @Override
            public void send(String str) {
                if (TextUtils.isEmpty(str) && coreManager.getConfig().registerInviteCode == 2) {
                    loginSuccess(phoneNumber, digestPwd, result);
                } else {
                    if (TextUtils.isEmpty(str)) {
                        mVerifyDialog.showError(mContext.getString(R.string.invitation_code_cannot_be_empty));
                    } else {
                        mhirdPartyLogin(str, phoneNumber, digestPwd, result);
                    }
                }
            }
        });
    }

    private void blanKing() {
        mOpenID = "";
        mType = "";
        mQQToken = "";
        mPhoneNumberEdit.setText("");
        mPasswordEdit.setText("");
    }

    private void mhirdPartyLogin(String str, String phoneNumber, String digestPwd, com.xuan.xuanhttplibrary.okhttp.result.ObjectResult<LoginRegisterResult> loginRegisterResultObjectResult) {
        String time = String.valueOf(TimeUtils.time_current_time());
        String secret = Md5Util.toMD5(AppConfig.apiKey + time + loginRegisterResultObjectResult.getData().getUserId() + loginRegisterResultObjectResult.getData().getAccess_token());
        ArrayMap<String, String> params = new ArrayMap<String, String>();
        params.put("inviteCode", str);
        params.put("access_token", loginRegisterResultObjectResult.getData().getAccess_token());
        params.put("secret", secret);
        HttpUtils.get().url(coreManager.getConfig().OTHER_SET_INVITE_CODE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            loginSuccess(phoneNumber, digestPwd, loginRegisterResultObjectResult);
                        } else {
                            if (result.getData() != null) {
                                String msg = TextUtils.isEmpty(result.getResultMsg()) ? getString(R.string.error_binding_the_invitation_code) : result.getResultMsg();
                                mVerifyDialog.showError(msg);
                            } else {
                                mVerifyDialog.showError(TextUtils.isEmpty(result.getResultMsg()) ? getString(R.string.error_binding_the_invitation_code) : result.getResultMsg());
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        mVerifyDialog.showError(e.getMessage());
                    }
                });
    }

}
