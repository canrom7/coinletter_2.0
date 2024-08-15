package com.iimm.miliao.ui.me;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iimm.miliao.BuildConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.VersionInfo;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.ReceiptMsgDao;
import com.iimm.miliao.db.dao.SendMsgDao;
import com.iimm.miliao.downloader.UpdateManger;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.sp.UserSp;
import com.iimm.miliao.ui.account.ChangePasswordActivity;
import com.iimm.miliao.ui.account.LoginActivity;
import com.iimm.miliao.ui.account.LoginHistoryActivity;
import com.iimm.miliao.ui.base.ActivityStack;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.lock.DeviceLockHelper;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.FileUtil;
import com.iimm.miliao.util.GetFileSizeUtil;
import com.iimm.miliao.util.LocaleHelper;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.SkinUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.view.NoDoubleClickListener;
import com.iimm.miliao.view.SelectionFrame;
import com.iimm.miliao.view.window.MainWindowShowService;
import com.iimm.miliao.volley.Result;
import com.iimm.miliao.xmpp.XmppConnectionImpl;
import com.iimm.miliao.push.IntentWrapper;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 设置
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener {
    private TextView mCacheTv;
    private TextView cacheTv, clearRecordsTv, changeTv, privateTv, aboutTv;
    private Button mExitBtn;
    private String mLoginUserId;
    private My_BroadcastReceiver mMyBroadcastReceiver = new My_BroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting2);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.other_settings));
//        tvTitle.setText(InternationalizationHelper.getString("JXSettingVC_Set"));

        mLoginUserId = coreManager.getSelf().getUserId();
        initView();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(com.iimm.miliao.broadcast.OtherBroadcast.SEND_MULTI_NOTIFY);
        intentFilter.addAction(com.iimm.miliao.broadcast.OtherBroadcast.NO_EXECUTABLE_INTENT);
        registerReceiver(mMyBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMyBroadcastReceiver != null) {
            unregisterReceiver(mMyBroadcastReceiver);
        }
    }

    private void initView() {
        cacheTv = (TextView) findViewById(R.id.cache_text);
        mCacheTv = (TextView) findViewById(R.id.cache_tv);
        clearRecordsTv = (TextView) findViewById(R.id.tv_cencel_chat);
        changeTv = (TextView) findViewById(R.id.passwoedtv);
        privateTv = (TextView) findViewById(R.id.privacySetting_text);
        aboutTv = (TextView) findViewById(R.id.aboutUs_text);
        mExitBtn = (Button) findViewById(R.id.exit_btn);
//        mExitBtn.setBackground(new ColorDrawable(MyApplication.getContext().getResources().getColor(R.color.hint_dian_color)));
        mExitBtn.setText(InternationalizationHelper.getString("JXSettingVC_LogOut"));

        cacheTv.setText(InternationalizationHelper.getString("JXSettingVC_ClearCache"));
        long cacheSize = GetFileSizeUtil.getFileSize(new File(MyApplication.getInstance().mAppDir));
        mCacheTv.setText(GetFileSizeUtil.formatFileSize(cacheSize));
        clearRecordsTv.setText(InternationalizationHelper.getString("EMPTY_RECORDS"));
        changeTv.setText(InternationalizationHelper.getString("JX_UpdatePassWord"));
        privateTv.setText(InternationalizationHelper.getString("JX_PrivacySettings"));
        aboutTv.setText(InternationalizationHelper.getString("JXAboutVC_AboutUS"));
        TextView mSwitchL = (TextView) findViewById(R.id.switch_language_tv);
        TextView mSwitchS = (TextView) findViewById(R.id.switch_skin_tv);
        mSwitchL.setText(InternationalizationHelper.getString("JX_LanguageSwitching"));
        mSwitchS.setText(InternationalizationHelper.getString("JXTheme_switch"));
        findViewById(R.id.clear_cache_rl).setOnClickListener(this);
        findViewById(R.id.rl_cencel_chat).setOnClickListener(this);
//        if (coreManager.getConfig().registerUsername) {
//            // 用户名注册的暂不支持找回密码，
//            findViewById(R.id.change_password_rl).setVisibility(View.GONE);
//        } else {
//        }
        findViewById(R.id.change_password_rl).setOnClickListener(this);
        findViewById(R.id.switch_language).setOnClickListener(this);
        findViewById(R.id.skin_rl).setOnClickListener(this);
        findViewById(R.id.chat_font_size_rl).setOnClickListener(this);
        findViewById(R.id.send_gMessage_rl).setOnClickListener(this);
        findViewById(R.id.privacy_settting_rl).setOnClickListener(this);
        findViewById(R.id.secure_setting_rl).setOnClickListener(this);
//        if (Constants.thirdLogin) {
//            findViewById(R.id.bind_account_rl).setOnClickListener(this);
//        } else {
        findViewById(R.id.bind_account_rl).setVisibility(View.GONE);
//        }
        findViewById(R.id.tuisongmsg).setOnClickListener(this);
        findViewById(R.id.about_us_rl).setOnClickListener(this);
        findViewById(R.id.about_us_rl).setVisibility(View.VISIBLE);
        findViewById(R.id.new_version_rl).setOnClickListener(this);
        findViewById(R.id.log_rl).setOnClickListener(this);
        findViewById(R.id.rl_clear_debug_data).setOnClickListener(this);
        if (BuildConfig.DEBUG) {
            findViewById(R.id.rl_clear_debug_data).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.rl_clear_debug_data).setVisibility(View.GONE);
        }
        if (BuildConfig.LOG_DEBUG) {
            findViewById(R.id.log_rl).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.log_rl).setVisibility(View.GONE);
        }
        mExitBtn.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                showExitDialog();
            }
        });

        List<IntentWrapper> intentWrapperList = IntentWrapper.getWhiteListMatters(this, "");
        if (intentWrapperList.size() == 0) {
            findViewById(R.id.tuisongmsg).setVisibility(View.GONE);
        }

        String currentLanguage = LocaleHelper.getLanguage(this);
        TextView switch_language_name = findViewById(R.id.switch_language_name);
        if (TextUtils.equals(currentLanguage, "zh")) {
            switch_language_name.setText("简体中文");
        } else if (TextUtils.equals(currentLanguage, "TW")) {
            switch_language_name.setText("繁體中文");
        } else if (TextUtils.equals(currentLanguage, "en")) {
            switch_language_name.setText("English");
        }
        TextView switch_skin_name = findViewById(R.id.switch_skin_name);
        switch_skin_name.setText(SkinUtils.getSkin(this).getColorName());
    }

    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        switch (v.getId()) {
            case R.id.clear_cache_rl:
                // 清除缓存
                clearCache();
                break;
            case R.id.rl_cencel_chat:
                SelectionFrame selectionFrame = new SelectionFrame(this);
                selectionFrame.setSomething(null, getString(R.string.is_empty_all_chat), new SelectionFrame.OnSelectionFrameClickListener() {
                    @Override
                    public void cancelClick() {

                    }

                    @Override
                    public void confirmClick() {
                        emptyServerMessage();

                        // 清除所有聊天记录
                        delAllChatRecord();
                    }
                });
                selectionFrame.show();
                break;
            case R.id.change_password_rl:
                // 修改密码
                startActivity(new Intent(mContext, ChangePasswordActivity.class));
                break;
            case R.id.switch_language:
                // 切换语言
                startActivity(new Intent(this, SwitchLanguage.class));
                break;
            case R.id.skin_rl:
                // 更换皮肤
                startActivity(new Intent(this, SkinStore.class));
                break;
            case R.id.chat_font_size_rl:
                // 更换聊天字体
                startActivity(new Intent(this, FontSizeActivity.class));
                break;
            case R.id.send_gMessage_rl:
                // 群发消息
                startActivity(new Intent(this, SelectFriendsActivity.class));
                break;
            case R.id.privacy_settting_rl:
                // 开启验证
                startActivity(new Intent(mContext, PrivacySettingActivity.class));
                break;
            case R.id.secure_setting_rl:
                // 安全设置，
                startActivity(new Intent(mContext, SecureSettingActivity.class));
                break;
            case R.id.bind_account_rl:
                // 绑定第三方
                startActivity(new Intent(mContext, BandAccountActivity.class));
                break;
            case R.id.tuisongmsg:
                IntentWrapper.whiteListMatters(this, "");
                break;
            case R.id.about_us_rl:
                // 关于我们
                startActivity(new Intent(mContext, AboutActivity.class));
                break;
            case R.id.log_rl:
                // 日志
                startActivity(new Intent(mContext, LogActivity.class));
                break;
            case R.id.rl_clear_debug_data:
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        if (BuildConfig.DEBUG) {
                            ReceiptMsgDao.getInstance().clearMsg();
                            SendMsgDao.getInstance().clearMsg();
                            runOnUiThread(() -> ToastUtil.showToast(R.string.cleared_successfully));
                        }
                    }
                }.start();
                break;
            case R.id.new_version_rl:
                if (TimeUtils.isFastClick(5 * 1000L)) {
                    ToastUtil.showToast(this, "您的操作过于频繁，请稍后!");
                }
                getNewVersion();
                break;
        }
    }

    /**
     * 清楚缓存
     */
    private void clearCache() {
        String filePath = MyApplication.getInstance().mAppDir;
        new ClearCacheAsyncTaska(filePath).execute(true);
    }

    // 服务器上所有的单人聊天记录也需要删除
    private void emptyServerMessage() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", String.valueOf(1));// 0 清空单人 1 清空所有

        HttpUtils.get().url(coreManager.getConfig().EMPTY_SERVER_MESSAGE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {

                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    /**
     * 清空所有聊天记录
     */
    private void delAllChatRecord() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        AsyncUtils.doAsync(this, settingActivityAsyncContext -> {
            // 不需要查询出所有好友，只需查询出最近聊天的好友即可
/*
            List<Friend> mAllFriend = new ArrayList<>();
            // 我的设备
            List<Friend> allDevices = FriendDao.getInstance().getDevice(mLoginUserId);
            mAllFriend.addAll(allDevices);
            // 公众号
            List<Friend> allSystems = FriendDao.getInstance().getAllSystems(mLoginUserId);
            mAllFriend.addAll(allSystems);
            // 我的好友
            List<Friend> allFriends = FriendDao.getInstance().getAllFriends(mLoginUserId);
            mAllFriend.addAll(allFriends);
            // 我的群组
            List<Friend> allRooms = FriendDao.getInstance().getAllRooms(mLoginUserId);
            mAllFriend.addAll(allRooms);
*/
            List<Friend> mNearChatFriendList = FriendDao.getInstance().getNearlyFriendMsg(mLoginUserId);
            for (int i = 0; i < mNearChatFriendList.size(); i++) {
                FriendDao.getInstance().resetFriendMessage(mLoginUserId, mNearChatFriendList.get(i).getUserId());
                ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, mNearChatFriendList.get(i).getUserId());
            }

            runOnUiThread(() -> {
                // 更新消息界面
                DialogHelper.dismissProgressDialog();
                MsgBroadcast.broadcastMsgUiUpdate(SettingActivity.this);
                MsgBroadcast.broadcastMsgNumReset(SettingActivity.this);
                ToastUtil.showToast(SettingActivity.this, InternationalizationHelper.getString("JXAlert_DeleteOK"));
            });
        });
    }

    // 退出当前账号
    private void showExitDialog() {
        SelectionFrame mSF = new SelectionFrame(this);
        mSF.setSomething(null, getString(R.string.sure_exit_account), new SelectionFrame.OnSelectionFrameClickListener() {
            @Override
            public void cancelClick() {

            }

            @Override
            public void confirmClick() {
                if (Constants.SUPPORT_FLOATING_WINDOW){
                    stopService(new Intent(SettingActivity.this, MainWindowShowService.class));
                    ActivityStack.getInstance().removeAlertWindowActivity();
                }


                logout();
                // 退出时清除设备锁密码，
                DeviceLockHelper.clearPassword();
                UserSp.getInstance(mContext).clearUserInfo();
                MyApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_SIMPLE_TELPHONE;
                XmppConnectionImpl.getInstance().logoutXmpp();
                LoginHelper.broadcastLogout(mContext);
                if (coreManager.getSelf() != null
                        && !TextUtils.isEmpty(coreManager.getSelf().getTelephone())) {
                    LoginHistoryActivity.start(SettingActivity.this);
                } else {
                    startActivity(new Intent(SettingActivity.this, LoginActivity.class));
                }
                finish();
            }
        });
        mSF.show();
    }

    private void logout() {
        HashMap<String, String> params = new HashMap<String, String>();
        // 得到电话
        String phoneNumber = coreManager.getSelf().getTelephone();
        // 去掉区号,
        String mobilePrefix = String.valueOf(PreferenceUtils.getInt(MyApplication.getContext(), Constants.AREA_CODE_KEY, 86));
        String phoneNumberRel;
        if (phoneNumber.startsWith(mobilePrefix)) {
            phoneNumberRel = phoneNumber.substring(mobilePrefix.length());
        } else {
            phoneNumberRel = phoneNumber;
        }
        String digestTelephone = Md5Util.toMD5(phoneNumberRel);
        params.put("telephone", digestTelephone);
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        // 默认为86
        params.put("areaCode", String.valueOf(86));
        params.put("deviceKey", "android");

        HttpUtils.get().url(coreManager.getConfig().USER_LOGOUT)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    private class ClearCacheAsyncTaska extends AsyncTask<Boolean, String, Integer> {

        private File rootFile;
        private ProgressDialog progressDialog;

        private int filesNumber = 0;
        private boolean canceled = false;
        private long notifyTime = 0;

        public ClearCacheAsyncTaska(String filePath) {
            this.rootFile = new File(filePath);
        }

        @Override
        protected void onPreExecute() {
            filesNumber = GetFileSizeUtil.getFolderSubFilesNumber(rootFile);
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getString(R.string.deleteing));
            progressDialog.setMax(filesNumber);
            progressDialog.setProgress(0);
            // 设置取消按钮
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, InternationalizationHelper.getString("JX_Cencal"), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int i) {
                    canceled = true;
                }
            });
            progressDialog.show();
        }

        /**
         * 返回true代表删除完成，false表示取消了删除
         */
        @Override
        protected Integer doInBackground(Boolean... params) {
            if (filesNumber == 0) {
                return 0;
            }
            // 是否删除已清空的子文件夹
            boolean deleteSubFolder = params[0];
            return deleteFolder(rootFile, true, deleteSubFolder, 0);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            // String filePath = values[0];
            int progress = Integer.parseInt(values[1]);
            // progressDialog.setMessage(filePath);
            progressDialog.setProgress(progress);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (!canceled && result == filesNumber) {
                ToastUtil.showToast(mContext, R.string.clear_completed);
            }
            long cacheSize = GetFileSizeUtil.getFileSize(rootFile);
            mCacheTv.setText(GetFileSizeUtil.formatFileSize(cacheSize));
        }

        /**
         * 是否删除完毕
         *
         * @param file
         * @param deleteSubFolder
         * @return
         */
        private int deleteFolder(File file, boolean rootFolder, boolean deleteSubFolder, int progress) {
            if (file == null || !file.exists() || !file.isDirectory()) {
                return 0;
            }
            File flist[] = file.listFiles();
            for (File subFile : flist) {
                if (canceled) {
                    return progress;
                }
                if (subFile.isFile()) {
                    subFile.delete();
                    progress++;
                    long current = System.currentTimeMillis();
                    if (current - notifyTime > 200) {// 200毫秒更新一次界面
                        notifyTime = current;
                        publishProgress(subFile.getAbsolutePath(), String.valueOf(progress));
                    }
                } else {
                    progress = deleteFolder(subFile, false, deleteSubFolder, progress);
                    if (deleteSubFolder) {
                        subFile.delete();
                    }
                }
            }
            return progress;
        }
    }

    private class My_BroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                if (action.equals(com.iimm.miliao.broadcast.OtherBroadcast.SEND_MULTI_NOTIFY)) {// 群发消息结束，关闭当前界面
                    finish();
                } else if (action.equals(com.iimm.miliao.broadcast.OtherBroadcast.NO_EXECUTABLE_INTENT)) {// 无可执行的intent 需提醒用户
                    DialogHelper.tip(SettingActivity.this, getString(R.string.no_executable_intent));
                }
            }
        }
    }

    private String fileDirectoryPath = FileUtil.getFileDir();
    public String apkFilePath;
    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;
    private static final String PACKAGE_URL_SCHEME = "package:";
    UpdateManger.updateListener mUpdateListener;
    // 下载安装包的网络路径
    private String apkUrl;
    private Dialog noticeDialog;// 提示有软件更新的对话框
    private Dialog downloadDialog;// 下载对话框
    // 进度条与通知UI刷新的handler和msg常量
    private ProgressBar mProgress;
    private int progress;// 当前进度
    private static final int REQUEST_CODE_INSTALL_PERMISSION = 1124;
    private Thread downLoadThread; // 下载线程
    private boolean interceptFlag = false;// 用户取消下载
    // 通知处理刷新界面的handler
    private Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    mProgress.setProgress(progress);
                    break;
                case DOWN_OVER:
                    if (downloadDialog != null && downloadDialog.isShowing()) {
                        downloadDialog.dismiss();
                    }
                    downloadDialog = null;
                    if (noticeDialog != null && noticeDialog.isShowing()) {
                        noticeDialog.dismiss();
                    }
                    noticeDialog = null;
                    installApk();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient.Builder()
                        .followRedirects(true)
                        .followSslRedirects(true)
                        .build();
                Request request = new Request.Builder()
                        .url(apkUrl)
                        .build();
                Response response = client.newCall(request)
                        .execute();
                response.body().contentLength();
                long length = response.body().contentLength();
                InputStream ins = response.body().byteStream();
                File file = new File(fileDirectoryPath);
                if (!file.exists()) {
                    file.mkdir();
                }
                File ApkFile = new File(apkFilePath);
                FileOutputStream outStream = new FileOutputStream(ApkFile);
                int count = 0;
                byte buf[] = new byte[1024];
                do {
                    int numread = ins.read(buf);
                    count += numread;
                    progress = (int) (((float) count / length) * 100);
                    // 下载进度
                    handler.sendEmptyMessage(DOWN_UPDATE);
                    if (numread <= 0) {
                        // 下载完成通知安装
                        handler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    outStream.write(buf, 0, numread);
                } while (!interceptFlag);// 点击取消停止下载
                outStream.close();
                ins.close();
            } catch (Exception e) {
                Reporter.unreachable(e);
                callNoUpdate();
            }
        }
    };

    /**
     * 获取版本信息
     */
    private void getNewVersion() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("appType", Constants.DEVICE_ANDROID);
        HttpUtils.get().url(coreManager.getConfig().GET_NEW_VERSION)
                .params(params)
                .build()
                .execute(new BaseCallback<VersionInfo>(VersionInfo.class) { //获取通证成功
                    @Override
                    public void onResponse(ObjectResult<VersionInfo> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == Result.CODE_SUCCESS && result.getData() != null) {
                            checkUpdate(result.getData());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    public void checkUpdate(VersionInfo versionInfo) {
        if (versionInfo == null || versionInfo.getVersionNum() == 0 || TextUtils.isEmpty(versionInfo.getApkLoadUrl())) {
            // 服务器没有配置新版本，
            return;
        }
        try {
            checkUpdateInfo(versionInfo);
        } catch (Throwable t) {
            // 无论如何不能因为这个崩溃，
            Reporter.post("检查更新失败，", t);
        }
    }

    // 显示更新程序对话框，供主程序调用
    public void checkUpdateInfo(VersionInfo versionInfo) {//添加检查服务器更新的代码
        PackageManager mPackageManager = getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = mPackageManager.getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Reporter.unreachable(e);
            callNoUpdate();
            return;
        }
        int versionCode = packageInfo.versionCode;
        int serverVersionCode = 0;
        try {
            serverVersionCode = versionInfo.getVersionNum();
            if (serverVersionCode <= versionCode) {// 版本号不低 不需要更新
                ToastUtil.showToast(this, getString(R.string.is_new_version));
                callNoUpdate();
            } else {
                showNoticeDialog(versionInfo);
            }
        } catch (Exception e) {

        }
    }

    private String getStringById(int sid) {
        return MyApplication.getContext().getString(sid);
    }

    private void callNoUpdate() {
        if (mUpdateListener != null) {
            handler.post(() -> {
                if (mUpdateListener != null) {
                    mUpdateListener.noUpdate();
                    // 确保只调用一次，
                    mUpdateListener = null;
                }
            });
        }
    }

    private void showNoticeDialog(VersionInfo versionInfo) {
        String content = getStringById(R.string.application_version_update_down);
        if (!TextUtils.isEmpty(versionInfo.getUpdateContent())) {
            content = versionInfo.getUpdateContent();
        }
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
                this);// Builder，可以通过此builder设置改变AleartDialog的默认的主题样式及属性相关信息
        builder.setTitle(getStringById(R.string.application_update));
        // 提示消息
        String updateMsg = MyApplication.getContext().getString(R.string.new_apk_download);
        if (TextUtils.isEmpty(content)) {
            builder.setMessage(updateMsg);
        } else {
            builder.setMessage(content);
        }
        builder.setPositiveButton(MyApplication.getContext().getString(R.string.download), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();// 当取消对话框后进行操作一定的代码？取消对话框
                try {
                    showDownloadDialog(versionInfo);
                } catch (Throwable t) {
                    Reporter.unreachable(t);
                    callNoUpdate();
                }
            }
        });
        if (versionInfo.getForceStatus() == Constants.UPDATE_FORCE) {

        } else {
            builder.setNegativeButton(MyApplication.getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        builder.setOnDismissListener(dialog -> {
            callNoUpdate();
        });
        if (noticeDialog == null) {
            noticeDialog = builder.create();
        }
        noticeDialog.setCanceledOnTouchOutside(false);
        if (versionInfo.getForceStatus() == Constants.UPDATE_FORCE) {
            noticeDialog.setCancelable(false);
        }
        if ((downloadDialog == null || !downloadDialog.isShowing()) && !noticeDialog.isShowing()) {
            noticeDialog.show();
        }
    }

    protected void showDownloadDialog(VersionInfo versionInfo) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
        builder.setTitle(R.string.application_update);
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.progress);
        builder.setView(v);// 设置对话框的内容为一个View
        if (versionInfo.getForceStatus() == Constants.UPDATE_FORCE) {

        } else {
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    interceptFlag = true;
                    callNoUpdate();
                }
            });
        }
        if (downloadDialog == null) {
            downloadDialog = builder.create();
        }
        downloadDialog.setCanceledOnTouchOutside(false);
        if (versionInfo.getForceStatus() == Constants.UPDATE_FORCE) {
            downloadDialog.setCancelable(false);
        }
        if (!downloadDialog.isShowing()) {
            downloadDialog.show();
        }
        downloadApk(versionInfo);
    }

    private void downloadApk(VersionInfo versionInfo) {
        apkUrl = versionInfo.getApkLoadUrl();
        String appName = getPackageName();
        String apkVersionName = TimeUtils.getCurrentTime();
        if (!TextUtils.isEmpty(versionInfo.getVersionName())) {
            apkVersionName = versionInfo.getVersionName();
        }
        apkFilePath = fileDirectoryPath + appName + apkVersionName + ".apk";
        File apkFile = new File(apkFilePath);
        if (apkFile.exists()) {
            apkFile.delete();
        }
        if (downLoadThread == null) {
            downLoadThread = new Thread(mdownApkRunnable);
        }
        if (!downLoadThread.isAlive()) {
            downLoadThread.start();
        }
    }

    protected void installApk() {
        openAPKFile(this, apkFilePath);
    }

    /**
     * 打开安装包
     */
    public void openAPKFile(Context ctx, String fileUri) {
        if (null != fileUri) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                File apkFile = new File(fileUri);
                if (!apkFile.exists()) {
                    Reporter.unreachable();
                    return;
                }
                //兼容7.0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri contentUri = FileProvider.getUriForFile(ctx.getApplicationContext(), ctx.getPackageName() + Constants.FILE_PROVIDER_AUTHORITY, apkFile);
                    intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                    //兼容8.0
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        boolean hasInstallPermission = ctx.getPackageManager().canRequestPackageInstalls();
                        if (!hasInstallPermission) {
                            startInstallPermissionSettingActivity();
                        }
                    }
                } else {
                    intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                if (ctx.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
                    ctx.startActivity(intent);
                }
            } catch (Throwable e) {
                Reporter.unreachable(e);
            }
        }
    }

    /**
     * 跳转到设置-允许安装未知来源-页面
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startInstallPermissionSettingActivity() {
        //这个是8.0新API
        Uri packageURI = Uri.parse(PACKAGE_URL_SCHEME + MyApplication.getInstance().getPackageName());
        //注意这个是8.0新API
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
        startActivityForResult(intent, REQUEST_CODE_INSTALL_PERMISSION);
    }
}
