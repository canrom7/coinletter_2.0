package com.iimm.miliao.ui.me.redpacket;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ehking.sdk.wepay.interfaces.WalletPay;
import com.ehking.sdk.wepay.net.bean.AuthType;
import com.gyf.immersionbar.ImmersionBar;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.redpacket.CloudQueryRedPacket;
import com.iimm.miliao.bean.redpacket.CloudRedPacket;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.message.ChatActivity;
import com.iimm.miliao.ui.smarttab.SmartTabLayout;
import com.iimm.miliao.util.InputChangeListenerNew;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.jetbrains.annotations.Nullable;
import org.jivesoftware.smack.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 个人云红包
 */
public class SendCloudRedPacketActivity extends BaseActivity implements View.OnClickListener, WalletPay.WalletPayCallback {
    LayoutInflater inflater;
    private SmartTabLayout smartTabLayout;
    private ViewPager viewPager;
    private List<View> views;
    private List<String> mTitleList;
    private EditText editTextPt;  // 普通红包的金额输入框
    private EditText editTextGre; // 祝福语输入框
    private TextView money_tv;
    private View mPlaceholder;
    private String userId;
    private WalletPay mWalletPay;
    private String mRequestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redpacket_cloud);

        inflater = LayoutInflater.from(this);
        initView();
        userId = getIntent().getStringExtra("userId");
        checkHasPayPassword();

        mPlaceholder = findViewById(R.id.placeholder_v);
        ImmersionBar.with(this)
                .statusBarView(mPlaceholder)
                .statusBarDarkFont(true, 0.2f)
                .init();
        mWalletPay = WalletPay.Companion.getInstance();
        mWalletPay.setEnvironment(com.payeasenet.wepay.constant.Constants.environment);
        mWalletPay.init(SendCloudRedPacketActivity.this);
    }

    private void checkHasPayPassword() {

    }

    /**
     * 初始化布局
     */
    private void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.tv_title_left).setOnClickListener(v -> finish());

        viewPager = (ViewPager) findViewById(R.id.viewpagert_redpacket);
        smartTabLayout = (SmartTabLayout) findViewById(R.id.smarttablayout_redpacket);
        views = new ArrayList<View>();
        mTitleList = new ArrayList<String>();
        mTitleList.add(InternationalizationHelper.getString("JX_UsualGift"));
        View v1;
        v1 = inflater.inflate(R.layout.redpacket_pager_pt_cloud, null);
        views.add(v1);

        // 获取EditText
        editTextPt = (EditText) v1.findViewById(R.id.edit_money);
        editTextGre = (EditText) v1.findViewById(R.id.edit_blessing);
        money_tv = v1.findViewById(R.id.money_tv);

        TextView yuan1;
        yuan1 = (TextView) v1.findViewById(R.id.yuanTv);
        yuan1.setText(InternationalizationHelper.getString("YUAN"));

        Button b1 = (Button) v1.findViewById(R.id.btn_sendRed);
        b1.setOnClickListener(this);

        b1.requestFocus();
        b1.setClickable(true);

        InputChangeListenerNew inputChangeListenerPt = new InputChangeListenerNew(editTextPt, money_tv);

        editTextPt.addTextChangedListener(inputChangeListenerPt);

        //设置值允许输入数字和小数点
        editTextPt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        viewPager.setAdapter(new PagerAdapter());
        inflater = LayoutInflater.from(this);
        smartTabLayout.setViewPager(viewPager);

        /**
         * 为了实现点击Tab栏切换的时候不出现动画
         * 为每个Tab重新设置点击事件
         */
        for (int i = 0; i < mTitleList.size(); i++) {
            View view = smartTabLayout.getTabAt(i);
            view.setTag(i + "");
            view.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_sendRed) {
            String money = null, words = null;

            //根据Tab的Item来判断当前发送的是那种红包
            final int item = viewPager.getCurrentItem();

            money = editTextPt.getText().toString();
            words = editTextGre.getText().toString();
            if (StringUtils.isNullOrEmpty(words)) {
                words = editTextGre.getHint().toString();
            }
            if (StringUtils.isNullOrEmpty(money)) {
                ToastUtil.showToast(mContext, InternationalizationHelper.getString("JX_InputGiftCount"));
            } else if (Double.parseDouble(money) > Double.parseDouble(coreManager.getConfig().weiBaoMaxRedPacketAmount)  || Double.parseDouble(money) <= 0) {
                ToastUtil.showToast(mContext, "红包总金额在0.01~"+coreManager.getConfig().weiBaoMaxRedPacketAmount+"之间哦!");
            } /*else if (Double.parseDouble(money) > coreManager.getSelf().getBalance()) {
                ToastUtil.showToast(mContext, InternationalizationHelper.getString("JX_NotEnough"));
            } */ else {
                /*PayPasswordVerifyDialog dialog = new PayPasswordVerifyDialog(this);
                dialog.setAction(getString(R.string.chat_redpacket));
                dialog.setMoney(money);
                final String finalMoney = money;
                final String finalWords = words;
                dialog.setOnInputFinishListener(password -> {
                    // 回传信息
                    bundle.putString("money", finalMoney);
                    bundle.putString("greetings", finalWords);
                    bundle.putString("type", "1"); // 类型
                    bundle.putString("count", "1"); // 因为是单聊，所以个数必须是一
                    bundle.putString("payPassword", password);
                    data.putExtras(bundle);
                    setResult(ChatActivity.REQUEST_CODE_SEND_RED_PT_CLOUD, data);
                    finish();
                });
                dialog.show();*/

                sendCloudRed(money, words);

                mWalletPay.setWalletPayCallback(this);
            }
        } else {
            // 根据Tab按钮传递的Tag来判断是那个页面，设置到相应的界面并且去掉动画
            int index = Integer.parseInt(v.getTag().toString());
            viewPager.setCurrentItem(index, false);
        }
    }

    public void sendCloudRed(String money, final String words) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("packetType", "1");
        params.put("singleAmount", money);
        params.put("targetUserId", userId);
        params.put("packetCount", "1");
        params.put("greetings", words);
        HttpUtils.post().url(coreManager.getConfig().REDPACKET_CREATE)
                .params(params)
                .build()
                .execute(new BaseCallback<CloudRedPacket>(CloudRedPacket.class) {

                    @Override
                    public void onResponse(ObjectResult<CloudRedPacket> result) {
                        CloudRedPacket redPacket = result.getData();
                        if (result.getResultCode() != 1) {
                            // 发送红包失败，
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        } else {
                            mRequestId = redPacket.getRequestId();
                            mWalletPay.evoke(redPacket.getMerchantId(), redPacket.getWalletId(), redPacket.getToken(), AuthType.REDPACKET.name(), "");
                        }
                        DialogHelper.dismissProgressDialog();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        // 发送红包失败，
                        ToastUtil.showToast(mContext, "发红包失败");
                    }
                });
    }

    @Override
    public void callback(@Nullable String s, @Nullable String status, @Nullable String s2) {
        if (TextUtils.equals("SUCCESS", status)||TextUtils.equals("PROCESS",status)) {
            inquireCloudRed(mRequestId);
        } else {
            ToastUtil.showToast(mContext, "发红包失败");
        }
    }

    public void inquireCloudRed(String requestId) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("requestId", requestId);
        params.put("queryType", "SIMPLE");
        HttpUtils.post().url(coreManager.getConfig().REDPACKET_CREATE_INQUIRE)
                .params(params)
                .build()
                .execute(new BaseCallback<CloudQueryRedPacket>(CloudQueryRedPacket.class) {

                    @Override
                    public void onResponse(ObjectResult<CloudQueryRedPacket> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() != 1) {
                            // 发送红包失败，
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        } else {
                            final Bundle bundle = new Bundle();
                            final Intent data = new Intent(SendCloudRedPacketActivity.this, ChatActivity.class);
                            bundle.putString("redPacket", result.getData().getRequestId());
                            bundle.putString("greetings", result.getData().getGreeting());
                            bundle.putString("type", "1"); // 类型
                            bundle.putInt("status", result.getData().getOrderStatus()); // 因为是单聊，所以个数必须是一
                            data.putExtras(bundle);
                            setResult(ChatActivity.REQUEST_CODE_SEND_RED_PT_CLOUD, data);
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        // 发送红包失败，
                        ToastUtil.showToast(mContext, "发红包失败");
                    }
                });
    }

    private class PagerAdapter extends android.support.v4.view.PagerAdapter {

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(View container, int position) {
            ((ViewGroup) container).addView(views.get(position));
            return views.get(position);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return mTitleList.get(position);
        }
    }
}
