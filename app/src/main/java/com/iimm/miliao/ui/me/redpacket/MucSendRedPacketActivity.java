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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gyf.immersionbar.ImmersionBar;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.EventRedSelect;
import com.iimm.miliao.bean.MucSendRedSelectBean;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.message.ChatActivity;
import com.iimm.miliao.ui.message.MucChatActivity;
import com.iimm.miliao.ui.smarttab.SmartTabLayout;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.InputChangeListenerNew;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.UiUtils;

import org.jivesoftware.smack.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * Created by 魏正旺 on 2016/9/8.
 */
public class MucSendRedPacketActivity extends BaseActivity implements View.OnClickListener {
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

    private EditText edit_count_kl;
    private EditText edit_money_kl;
    private EditText edit_words_kl;
    private TextView tv_money_kl;

    private TextView hbgs, ge, zje, yuan, xhb;
    private Button sq;
    private View mPlaceholder;

    //专属红包使用view
    private LinearLayout mExclusiveLl1, mExclusiveLl2, mExclusiveLl3;
    private TextView mSelectTv1, mSelectTv2, mSelectTv3;
    private TextView mPeopleTv1, mPeopleTv2, mPeopleTv3;
    private String mToUserIds1, mToUserIds2, mToUserIds3;
    private String roomJid, roomSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_muc_redpacket);
        roomSize = getIntent().getStringExtra("size");
        roomJid = getIntent().getStringExtra("roomJid");
        inflater = LayoutInflater.from(this);
        initView();
        checkHasPayPassword();

        mPlaceholder = findViewById(R.id.placeholder_v);
        ImmersionBar.with(this)
                .statusBarView(mPlaceholder)
                .statusBarDarkFont(true, 0.2f)
                .init();
        EventBus.getDefault().register(this);
    }

    private void checkHasPayPassword() {
        boolean hasPayPassword = PreferenceUtils.getBoolean(this, Constants.IS_PAY_PASSWORD_SET + coreManager.getSelf().getUserId(), true);
        if (!hasPayPassword) {
            Intent intent = new Intent(this, ChangePayPasswordActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.tv_title_left).setOnClickListener(v -> finish());

        smartTabLayout = (SmartTabLayout) findViewById(R.id.muc_smarttablayout_redpacket);
        viewPager = (ViewPager) findViewById(R.id.muc_viewpagert_redpacket);
        views = new ArrayList<View>();
        mTitleList = new ArrayList<String>();
        mTitleList.add(InternationalizationHelper.getString("JX_LuckGift"));
        mTitleList.add(InternationalizationHelper.getString("JX_UsualGift"));
        mTitleList.add(InternationalizationHelper.getString("JX_MesGift"));

        views.add(inflater.inflate(R.layout.muc_redpacket_pager_pt, null));
        views.add(inflater.inflate(R.layout.muc_redpacket_pager_sq, null));
        views.add(inflater.inflate(R.layout.muc_redpacket_pager_kl, null));

        View temp_view = views.get(0);
        edit_count_pt = (EditText) temp_view.findViewById(R.id.edit_redcount);
        edit_count_pt.addTextChangedListener(new RemoveZeroTextWatcher(edit_count_pt));
        edit_money_pt = (EditText) temp_view.findViewById(R.id.edit_money);
        edit_words_pt = (EditText) temp_view.findViewById(R.id.edit_blessing);
        tv_money_pt = temp_view.findViewById(R.id.money_tv);
        hbgs = (TextView) temp_view.findViewById(R.id.hbgs);
        ge = (TextView) temp_view.findViewById(R.id.ge);
        zje = (TextView) temp_view.findViewById(R.id.zje);
        yuan = (TextView) temp_view.findViewById(R.id.yuan);
        xhb = (TextView) temp_view.findViewById(R.id.textviewtishi);
        sq = (Button) temp_view.findViewById(R.id.btn_sendRed);
        mExclusiveLl1 = temp_view.findViewById(R.id.redly1_exclusive);
        mSelectTv1 = temp_view.findViewById(R.id.edit_redcount_exclusive);
        mPeopleTv1 = temp_view.findViewById(R.id.textviewtishi_exclusive);
        mExclusiveLl1.setOnClickListener(this);
//        hbgs.setText(InternationalizationHelper.getString("NUMBER_OF_ENVELOPES"));
        ge.setText(InternationalizationHelper.getString("INDIVIDUAL"));
//        zje.setText(InternationalizationHelper.getString("TOTAL_AMOUNT"));
//        edit_money_pt.setHint(InternationalizationHelper.getString("INPUT_AMOUNT"));
        yuan.setText(InternationalizationHelper.getString("YUAN"));
//        xhb.setText(InternationalizationHelper.getString("SAME_AMOUNT"));
//        edit_words_pt.setHint(InternationalizationHelper.getString("JX_GiftText"));
        sq.setOnClickListener(this);

        temp_view = views.get(1);
        edit_count_psq = (EditText) temp_view.findViewById(R.id.edit_redcount);
        edit_count_psq.addTextChangedListener(new RemoveZeroTextWatcher(edit_count_psq));
        edit_money_psq = (EditText) temp_view.findViewById(R.id.edit_money);
        edit_words_psq = (EditText) temp_view.findViewById(R.id.edit_blessing);
        tv_money_psq = temp_view.findViewById(R.id.money_tv);
        hbgs = (TextView) temp_view.findViewById(R.id.hbgs);
        ge = (TextView) temp_view.findViewById(R.id.ge);
        zje = (TextView) temp_view.findViewById(R.id.zje);
        yuan = (TextView) temp_view.findViewById(R.id.yuan);
        xhb = (TextView) temp_view.findViewById(R.id.textviewtishi);
        sq = (Button) temp_view.findViewById(R.id.btn_sendRed);
        mExclusiveLl2 = temp_view.findViewById(R.id.redly1_exclusive);
        mSelectTv2 = temp_view.findViewById(R.id.edit_redcount_exclusive);
        mPeopleTv2 = temp_view.findViewById(R.id.textviewtishi_exclusive);
        mExclusiveLl2.setOnClickListener(this);
//        hbgs.setText(InternationalizationHelper.getString("NUMBER_OF_ENVELOPES"));
        ge.setText(InternationalizationHelper.getString("INDIVIDUAL"));
//        zje.setText(InternationalizationHelper.getString("TOTAL_AMOUNT"));
//        edit_money_psq.setHint(InternationalizationHelper.getString("INPUT_AMOUNT"));
        yuan.setText(InternationalizationHelper.getString("YUAN"));
//        xhb.setText(InternationalizationHelper.getString("RONDOM_AMOUNT"));
//        edit_words_psq.setHint(InternationalizationHelper.getString("JX_GiftText"));
        sq.setOnClickListener(this);

        temp_view = views.get(2);
        edit_count_kl = (EditText) temp_view.findViewById(R.id.edit_redcount);
        edit_count_kl.addTextChangedListener(new RemoveZeroTextWatcher(edit_count_kl));
        edit_money_kl = (EditText) temp_view.findViewById(R.id.edit_money);
        edit_words_kl = (EditText) temp_view.findViewById(R.id.edit_password);
        tv_money_kl = temp_view.findViewById(R.id.money_tv);
        EditText edit_compatible = (EditText) temp_view.findViewById(R.id.edit_compatible);
        edit_compatible.requestFocus();

        hbgs = (TextView) temp_view.findViewById(R.id.hbgs);
        ge = (TextView) temp_view.findViewById(R.id.ge);
        zje = (TextView) temp_view.findViewById(R.id.zje);
        yuan = (TextView) temp_view.findViewById(R.id.yuan);
        xhb = (TextView) temp_view.findViewById(R.id.textviewtishi);
        sq = (Button) temp_view.findViewById(R.id.btn_sendRed);
        mExclusiveLl3 = temp_view.findViewById(R.id.redly1_exclusive);
        mPeopleTv3 = temp_view.findViewById(R.id.textviewtishi_exclusive);
        mSelectTv3 = temp_view.findViewById(R.id.edit_redcount_exclusive);
        TextView kl = (TextView) temp_view.findViewById(R.id.kl);
        mExclusiveLl3.setOnClickListener(this);
//        kl.setText(InternationalizationHelper.getString("JX_Message"));
//        hbgs.setText(InternationalizationHelper.getString("NUMBER_OF_ENVELOPES"));
        ge.setText(InternationalizationHelper.getString("INDIVIDUAL"));
//        zje.setText(InternationalizationHelper.getString("TOTAL_AMOUNT"));
//        edit_money_kl.setHint(InternationalizationHelper.getString("INPUT_AMOUNT"));
        yuan.setText(InternationalizationHelper.getString("YUAN"));
//        xhb.setText(InternationalizationHelper.getString("REPLY_GRAB"));
//        edit_words_kl.setHint(InternationalizationHelper.getString("BIG_ENVELOPE"));
        sq.setOnClickListener(this);

        InputChangeListenerNew inputChangeListenerPt = new InputChangeListenerNew(edit_money_pt, tv_money_pt);
        InputChangeListenerNew inputChangeListenerPsq = new InputChangeListenerNew(edit_money_psq, tv_money_psq);
        InputChangeListenerNew inputChangeListenerKl = new InputChangeListenerNew(edit_money_kl, tv_money_kl);

        // 添加输入监听
        edit_money_pt.addTextChangedListener(inputChangeListenerPt);
        edit_money_psq.addTextChangedListener(inputChangeListenerPsq);
        edit_money_kl.addTextChangedListener(inputChangeListenerKl);
        // 只允许输入小数点和数字
        edit_money_pt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        edit_money_psq.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        edit_money_kl.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

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


        mPeopleTv1.setText("群人数" + roomSize + "人");
        mPeopleTv2.setText("群人数" + roomSize + "人");
        mPeopleTv3.setText("群人数" + roomSize + "人");
        if (Constants.SUPPORT_EXCLUSIVE_RED) {
            mExclusiveLl1.setVisibility(View.VISIBLE);
            mExclusiveLl2.setVisibility(View.VISIBLE);
            mExclusiveLl3.setVisibility(View.VISIBLE);
            mPeopleTv1.setVisibility(View.VISIBLE);
            mPeopleTv2.setVisibility(View.VISIBLE);
            mPeopleTv3.setVisibility(View.VISIBLE);
        } else {
            mExclusiveLl1.setVisibility(View.GONE);
            mExclusiveLl2.setVisibility(View.GONE);
            mExclusiveLl3.setVisibility(View.GONE);
            mPeopleTv1.setVisibility(View.GONE);
            mPeopleTv2.setVisibility(View.GONE);
            mPeopleTv3.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        if (v.getId() == R.id.btn_sendRed) {
            final int item = viewPager.getCurrentItem();
            final Bundle bundle = new Bundle();
            final Intent intent = new Intent(this, MucChatActivity.class);
            String money = null, words = null, count = null;
            int resultCode = 0;
            switch (item) {
                case 0: {
                    money = edit_money_pt.getText().toString();
                    words = StringUtils.isNullOrEmpty(edit_words_pt.getText().toString()) ?
                            edit_words_pt.getHint().toString() : edit_words_pt.getText().toString();
                    count = edit_count_pt.getText().toString();
                    // 拼手气与普通红包位置对调  修改resultCode
                    resultCode = ChatActivity.REQUEST_CODE_SEND_RED_PSQ;
                }
                break;

                case 1: {
                    money = edit_money_psq.getText().toString();
                    words = StringUtils.isNullOrEmpty(edit_words_psq.getText().toString()) ?
                            edit_words_psq.getHint().toString() : edit_words_psq.getText().toString();
                    count = edit_count_psq.getText().toString();
                    resultCode = ChatActivity.REQUEST_CODE_SEND_RED_PT;
                }
                break;

                case 2: {
                    money = edit_money_kl.getText().toString();
                    words = StringUtils.isNullOrEmpty(edit_words_kl.getText().toString()) ?
                            edit_words_kl.getHint().toString().substring(1) : edit_words_kl.getText().toString();
                    count = edit_count_kl.getText().toString();
                    resultCode = ChatActivity.REQUEST_CODE_SEND_RED_KL;
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

            if (!TextUtils.isEmpty(money) &&
                    !TextUtils.isEmpty(count) &&
                    Double.parseDouble(money) / Integer.parseInt(count) < 0.01) {
                ToastUtil.showToast(this, R.string.tip_money_too_less);
                return;
            }

            if (eqData(money, count, words)) {
                PayPasswordVerifyDialog dialog = new PayPasswordVerifyDialog(this);
                dialog.setAction(getString(R.string.chat_redpacket));
                dialog.setMoney(money);
                final String finalMoney = money;
                final String finalWords = words;
                final String finalCount = count;
                dialog.setOnInputFinishListener(new PayPasswordVerifyDialog.OnInputFinishListener() {
                    @Override
                    public void onInputFinish(final String password) {
                        // 回传信息
                        bundle.putString("money", finalMoney);
                        bundle.putString("count", finalCount);
                        bundle.putString("words", finalWords);
                        // 拼手气与普通红包位置对调，修改type
                        // bundle.putString("type", (item + 1) + "");
                        if (item == 0) {
                            bundle.putString("toUserIds",mToUserIds1);
                            bundle.putString("type", 2 + "");
                        } else if (item == 1) {
                            bundle.putString("toUserIds",mToUserIds2);
                            bundle.putString("type", 1 + "");
                        } else {
                            bundle.putString("toUserIds",mToUserIds3);
                            bundle.putString("type", (item + 1) + "");
                        }

                        bundle.putString("payPassword", password);
                        intent.putExtras(bundle);
                        setResult(item == 0 ? ChatActivity.REQUEST_CODE_SEND_RED_PSQ : ChatActivity.REQUEST_CODE_SEND_RED_KL, intent);
                        finish();
                    }
                });
                dialog.show();
            }
        } else if (v.getId() == R.id.redly1_exclusive) {
            Intent intent = new Intent(MucSendRedPacketActivity.this, MucSendRedSelectActivity.class);
            intent.putExtra("roomJid", roomJid);
            startActivity(intent);
        } else {
            int index = Integer.parseInt(v.getTag().toString());
            viewPager.setCurrentItem(index, false);
        }
    }

    private boolean eqData(String money, String count, String words) {
        if (StringUtils.isNullOrEmpty(money)) {
            ToastUtil.showToast(mContext, getString(R.string.need_input_money));
            return false;
        } else if (Double.parseDouble(money) > Double.parseDouble(coreManager.getConfig().maxSendRedPagesAmount) || Double.parseDouble(money) <= 0) {
            ToastUtil.showToast(mContext, String.format(getString(R.string.red_packet_range), coreManager.getConfig().maxSendRedPagesAmount));
            return false;
        } else if (Double.parseDouble(money) > coreManager.getSelf().getBalance()) {
            ToastUtil.showToast(mContext, getString(R.string.balance_not_enough));
            return false;
        } else if (StringUtils.isNullOrEmpty(count)) {
            ToastUtil.showToast(mContext, getString(R.string.need_red_packet_count));
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

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void processEventBusMsg(EventRedSelect eventRedSelect) {
        if (eventRedSelect != null) {
            String name = "";
            String ids = "";
            for (MucSendRedSelectBean data : eventRedSelect.getBeans()) {
                if (TextUtils.isEmpty(name)) {
                    name = data.getNickname();
                    ids = data.getUserId();
                } else {
                    name = data.getNickname() + "," + name;
                    ids = data.getUserId()+ "," + ids;
                }
            }
            switch (viewPager.getCurrentItem()) {
                case 0:
                    mToUserIds1 = ids;
                    mSelectTv1.setText(name);
                    mPeopleTv1.setText("群人数" + roomSize + "人，已选定" + eventRedSelect.getBeans().size() + "人可领");
                    break;
                case 1:
                    mToUserIds2 = ids;
                    mSelectTv2.setText(name);
                    mPeopleTv2.setText("群人数" + roomSize + "人，已选定" + eventRedSelect.getBeans().size() + "人可领");
                    break;
                case 2:
                    mToUserIds3 = ids;
                    mSelectTv3.setText(name);
                    mPeopleTv3.setText("群人数" + roomSize + "人，已选定" + eventRedSelect.getBeans().size() + "人可领");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
