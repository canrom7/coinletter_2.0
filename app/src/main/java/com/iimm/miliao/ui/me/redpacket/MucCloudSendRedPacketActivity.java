package com.iimm.miliao.ui.me.redpacket;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 群组云红包
 */
public class MucCloudSendRedPacketActivity extends BaseActivity implements View.OnClickListener, WalletPay.WalletPayCallback {
    LayoutInflater inflater;
    private SmartTabLayout smartTabLayout;
    private ViewPager viewPager;
    private List<View> views;
    private List<String> mTitleList;
    private EditText edit_count_pt;
    private EditText edit_money_pt;
    private EditText edit_words_pt;
    private TextView tv_money_pt;

    private EditText edit_count_psq;
    private EditText edit_money_psq;
    private EditText edit_words_psq;
    private TextView tv_money_psq;


    private TextView ge, yuan;
    private Button sq;
    private View mPlaceholder;
    private String mUserId;//群组id
    private WalletPay mWalletPay;
    private String mRequestId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_muc_redpacket_cloud);
        mUserId = getIntent().getStringExtra("userId");
        inflater = LayoutInflater.from(this);
        initView();
        checkHasPayPassword();

        mPlaceholder = findViewById(R.id.placeholder_v);
        ImmersionBar.with(this)
                .statusBarView(mPlaceholder)
                .statusBarDarkFont(true, 0.2f)
                .init();
        mWalletPay = WalletPay.Companion.getInstance();
        mWalletPay.setEnvironment(com.payeasenet.wepay.constant.Constants.environment);
        mWalletPay.init(MucCloudSendRedPacketActivity.this);
    }

    private void checkHasPayPassword() {

    }

    private void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.tv_title_left).setOnClickListener(v -> finish());

        smartTabLayout = (SmartTabLayout) findViewById(R.id.muc_smarttablayout_redpacket);
        viewPager = (ViewPager) findViewById(R.id.muc_viewpagert_redpacket);
        views = new ArrayList<View>();
        mTitleList = new ArrayList<String>();
        mTitleList.add("普通群红包");
        mTitleList.add("手气群红包");

        views.add(inflater.inflate(R.layout.muc_redpacket_pager_pt_cloud, null));
        views.add(inflater.inflate(R.layout.muc_redpacket_pager_sq_cloud, null));

        View temp_view = views.get(0);
        edit_count_pt = (EditText) temp_view.findViewById(R.id.edit_redcount);
        edit_count_pt.addTextChangedListener(new RemoveZeroTextWatcher(edit_count_pt));
        edit_money_pt = (EditText) temp_view.findViewById(R.id.edit_money);
        edit_words_pt = (EditText) temp_view.findViewById(R.id.edit_blessing);
        tv_money_pt = temp_view.findViewById(R.id.money_tv);
        ge = (TextView) temp_view.findViewById(R.id.ge);
        yuan = (TextView) temp_view.findViewById(R.id.yuan);
        sq = (Button) temp_view.findViewById(R.id.btn_sendRed);
        ge.setText(InternationalizationHelper.getString("INDIVIDUAL"));
        yuan.setText(InternationalizationHelper.getString("YUAN"));
        sq.setOnClickListener(this);

        temp_view = views.get(1);
        edit_count_psq = (EditText) temp_view.findViewById(R.id.edit_redcount);
        edit_count_psq.addTextChangedListener(new RemoveZeroTextWatcher(edit_count_psq));
        edit_money_psq = (EditText) temp_view.findViewById(R.id.edit_money);
        edit_words_psq = (EditText) temp_view.findViewById(R.id.edit_blessing);
        tv_money_psq = temp_view.findViewById(R.id.money_tv);
        ge = (TextView) temp_view.findViewById(R.id.ge);
        yuan = (TextView) temp_view.findViewById(R.id.yuan);
        sq = (Button) temp_view.findViewById(R.id.btn_sendRed);
        ge.setText(InternationalizationHelper.getString("INDIVIDUAL"));
        yuan.setText(InternationalizationHelper.getString("YUAN"));
        sq.setOnClickListener(this);

        // 只允许输入小数点和数字
        edit_money_pt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        edit_money_psq.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        InputChangeListenerNew inputChangeListenerPsq = new InputChangeListenerNew(edit_money_psq, tv_money_psq);

        // 添加输入监听
        edit_money_pt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(edit_money_pt.getText().toString().trim())&&!TextUtils.isEmpty(edit_count_pt.getText().toString().trim())){
                    String str = String.valueOf(new BigDecimal(edit_money_pt.getText().toString().trim()).multiply(new BigDecimal(edit_count_pt.getText().toString().trim())));
                    tv_money_pt.setText(String.format("￥%s", str));
                }else {
                    tv_money_pt.setText("￥0.00");
                }
            }
        });
        edit_count_pt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(edit_money_pt.getText().toString().trim())&&!TextUtils.isEmpty(edit_count_pt.getText().toString().trim())){
                    String str = String.valueOf(new BigDecimal(edit_money_pt.getText().toString().trim()).multiply(new BigDecimal(edit_count_pt.getText().toString().trim())));
                    tv_money_pt.setText(String.format("￥%s", str));
                }else {
                    tv_money_pt.setText("￥0.00");
                }
            }
        });


        edit_money_psq.addTextChangedListener(inputChangeListenerPsq);

        viewPager.setAdapter(new PagerAdapter());
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
            final int item = viewPager.getCurrentItem();
            String money = null, words = null, count = null;
            switch (item) {
                case 0: {
                    money = edit_money_pt.getText().toString().trim();

                    words = StringUtils.isNullOrEmpty(edit_words_pt.getText().toString()) ?
                            edit_words_pt.getHint().toString() : edit_words_pt.getText().toString();
                    count = edit_count_pt.getText().toString();
                }
                break;

                case 1: {
                    money = edit_money_psq.getText().toString();

                    words = StringUtils.isNullOrEmpty(edit_words_psq.getText().toString()) ?
                            edit_words_psq.getHint().toString() : edit_words_psq.getText().toString();
                    count = edit_count_psq.getText().toString();
                }
                break;
            }

            if (!TextUtils.isEmpty(count) && Integer.parseInt(count) == 0) {
                ToastUtil.showToast(this, R.string.tip_red_packet_too_slow);
                return;
            }

            // 当金额过小，红包个数过多的情况下会出现不够分的情况
            if (!TextUtils.isEmpty(count) && Integer.parseInt(count) > 100) {
                ToastUtil.showToast(this, R.string.tip_red_packet_too_much);
                return;
            }

            if (item == 1) {
                if (!TextUtils.isEmpty(money) &&
                        !TextUtils.isEmpty(count) &&
                        Double.parseDouble(money) / Double.valueOf(count) < 0.01) {
                    ToastUtil.showToast(this, R.string.tip_money_too_less);
                    return;
                }
            } else {
                if (!TextUtils.isEmpty(money) && Double.valueOf(money) < 0.01) {
                    ToastUtil.showToast(this, R.string.tip_money_too_less);
                    return;
                }
            }


            if (eqData(money, count, words, item)) {
                String type;
                if (item == 0) {
                    type = "1";
                } else {//手气
                    type = "2";
                }
                sendCloudRed(money, words, type, count);

                mWalletPay.setWalletPayCallback(this);
            }
        } else {
            int index = Integer.parseInt(v.getTag().toString());
            viewPager.setCurrentItem(index, false);
        }
    }


    /**
     * 发送云红包方法
     */
    public void sendCloudRed(String money, final String words, String type, String cout) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("packetType", type);
        if (TextUtils.equals(type, "1")) {//普通红包
            params.put("singleAmount", money);
        }
        if (TextUtils.equals(type, "2")) {//手气红包
            params.put("amount", money);//总金额
        }
        params.put("packetCount", cout);
        params.put("greetings", words);
        params.put("roomJid", mUserId);
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
                            final Intent data = new Intent(MucCloudSendRedPacketActivity.this, ChatActivity.class);
                            bundle.putString("redPacket", result.getData().getRequestId());
                            bundle.putString("greetings", result.getData().getGreeting());
                            bundle.putString("type", result.getData().getPacketType() + ""); // 类型
                            bundle.putInt("status", result.getData().getOrderStatus()); //
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


    private boolean eqData(String money, String count, String words, int item) {
        if (StringUtils.isNullOrEmpty(money)) {
            ToastUtil.showToast(mContext, getString(R.string.need_input_money));
            return false;
        } else if (StringUtils.isNullOrEmpty(count)) {
            ToastUtil.showToast(mContext, getString(R.string.need_red_packet_count));
            return false;
        } else if (item == 0
                && !TextUtils.isEmpty(money)
                && !TextUtils.isEmpty(coreManager.getConfig().weiBaoMaxRedPacketAmount)
                && ((Double.valueOf(money) < 0.01) || Double.valueOf(money) * Double.valueOf(count) > Double.valueOf(coreManager.getConfig().weiBaoMaxRedPacketAmount))) {
            ToastUtil.showToast(mContext, "红包金额在0.01~" + coreManager.getConfig().weiBaoMaxRedPacketAmount + "之间哦!");
            return false;
        } else if (item == 1 && !TextUtils.isEmpty(money)
                && Double.valueOf(money) > Double.valueOf(coreManager.getConfig().weiBaoMaxRedPacketAmount)
                && (Double.valueOf(money) < 0.01 || (Double.valueOf(money) / Double.valueOf(count)) < 0.01)) {
            ToastUtil.showToast(mContext, "红包总金额在0.01~" + coreManager.getConfig().weiBaoMaxRedPacketAmount + "之间哦!");
            return false;
        } else if (StringUtils.isNullOrEmpty(words)) {
            return false;
        }
        return true;
    }

    private static class RemoveZeroTextWatcher implements TextWatcher {
        private EditText editText;

        RemoveZeroTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // 删除开头的0，
            int end = 0;
            for (int i = 0; i < editable.length(); i++) {
                char ch = editable.charAt(i);
                if (ch == '0') {
                    end = i + 1;
                } else {
                    break;
                }
            }
            if (end > 0) {
                editable.delete(0, end);
                editText.setText(editable);
            }
        }
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
        public void destroyItem(View container, int position, Object object) {
            ((ViewPager) container).removeView(views.get(position));
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
