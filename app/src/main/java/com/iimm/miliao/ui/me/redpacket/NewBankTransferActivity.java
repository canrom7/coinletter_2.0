package com.iimm.miliao.ui.me.redpacket;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.BankListBean;
import com.iimm.miliao.bean.MyBankCardBean;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.helper.AddBankCardDialog;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.pay.EventBankRechargeInfo;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.dialog.AskCommonBottomDialog;
import com.iimm.miliao.util.DisplayUtil;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.volley.Result;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

public class NewBankTransferActivity extends BaseActivity {
    private LinearLayout mLlAddBankCard;
    private TextView mTvTime;
    private TabLayout mTblMenu;
    private List<BankListBean> mBankListBeans;
    private TextView mTvDepositName;
    private TextView mTvBankCardName;
    private TextView mTvBranchBankName;
    private String mMoney;
    // private TextView mTvTitleRight;
    private AddBankCardDialog mAddBankCardDialog;
    private LinearLayout llMyBankCardList;
    private MyBankCardBean mMyBankCardBean;
    private boolean isDownOrder = false;
    private TextView mTvTimeHint;
    private boolean isShow = false;
    private boolean isTransferSuccess = false;
    private boolean isBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_bank_transfer);
        EventBus.getDefault().register(this);
        initView();
        event();
        getMyBankList(true);
        getBankList();
        Intent intent = getIntent();
        if (intent != null) {
            mMoney = intent.getStringExtra("money");
        }
    }


    /**
     * 提交订单
     */
    private void submitOrder() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> par = new HashMap<>();
        par.put("access_token", coreManager.getSelfStatus().accessToken);
        par.put("serialAmount", mMoney);
        HttpUtils.get().url(coreManager.getConfig().SUBMIT_BANK_INFO_PAY).params(par).build().execute(new ListCallback<BankListBean>(BankListBean.class) {
            @Override
            public void onResponse(ArrayResult<BankListBean> result) {
                DialogHelper.dismissProgressDialog();
                if (result.getResultCode() == 1) {
                    ToastUtil.showToast(NewBankTransferActivity.this, R.string.successfully_ordered);
                    startCountTime();
                } else {
                    ToastUtil.showToast(NewBankTransferActivity.this, TextUtils.isEmpty(result.getResultMsg())
                            ? getResources().getString(R.string.failed_to_submit_order)
                            : result.getResultMsg());
                }
            }

            @Override
            public void onError(Call call, Exception e) {
                ToastUtil.showErrorData(NewBankTransferActivity.this);
                DialogHelper.dismissProgressDialog();
            }
        });
    }

    private void showSuccess(String msg) {
        if (isShow) {
            return;
        }
        isShow = true;
        Dialog dialog = new Dialog(this, R.style.MyDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_bank_pay_success, null);
        ImageView ivClose = view.findViewById(R.id.iv_close);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(R.string.point);
        TextView tvContent = view.findViewById(R.id.tv_content);
        tvContent.setText(msg);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finishBottomActivityForStack();
                finish();
            }
        });
        Button bok = view.findViewById(R.id.b_ok);
        bok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finishBottomActivityForStack();
                finish();
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);
        dialog.show();
    }

    /**
     * 关闭栈下边的activity 关闭 WxPayAdd
     */
    private void finishBottomActivityForStack() {
        try {
            if (WxPayAdd.wxPayAdd != null && !WxPayAdd.wxPayAdd.isDestroyed()) {
                WxPayAdd.wxPayAdd.finish();
                WxPayAdd.wxPayAdd = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取银行列表
     */
    private void getBankList() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> par = new HashMap<>();
        par.put("access_token", coreManager.getSelfStatus().accessToken);
        HttpUtils.get().url(coreManager.getConfig().GET_BANK_LIST).params(par).build().execute(new ListCallback<BankListBean>(BankListBean.class) {
            @Override
            public void onResponse(ArrayResult<BankListBean> result) {
                DialogHelper.dismissProgressDialog();
                if (result.getResultCode() == 1 && result.getData() != null) {
                    if (mBankListBeans == null) {
                        mBankListBeans = new ArrayList<>();
                    }
                    mBankListBeans.clear();
                    mBankListBeans.addAll(result.getData());
                    mTblMenu.removeAllTabs();
                    for (int i = 0; i < mBankListBeans.size(); i++) {
                        mTblMenu.addTab(mTblMenu.newTab().setText(mBankListBeans.get(i).getBankName()));
                    }
                    if (mBankListBeans.size() > 0) {
                        mTblMenu.post(new Runnable() {
                            @Override
                            public void run() {
                                mTblMenu.getTabAt(0).select();
                            }
                        });
                    }
                } else {
                    ToastUtil.showToast(NewBankTransferActivity.this, TextUtils.isDigitsOnly(result.getResultMsg())
                            ? getResources().getString(R.string.failed_to_load_bank_card_list)
                            : result.getResultMsg());
                }
            }

            @Override
            public void onError(Call call, Exception e) {
                ToastUtil.showErrorData(NewBankTransferActivity.this);
                DialogHelper.dismissProgressDialog();
            }
        });
    }

    private void getMyBankList(boolean isComeInGet) {
        Map<String, String> par = new HashMap<>();
        par.put("access_token", coreManager.getSelfStatus().accessToken);
        HttpUtils.get()
                .url(coreManager.getConfig().GET_MY_BANK_CARD_LIST)
                .params(par)
                .build()
                .execute(new BaseCallback<MyBankCardBean>(MyBankCardBean.class) {
                    @Override
                    public void onResponse(ObjectResult<MyBankCardBean> result) {
                        if (result.getResultCode() == com.xuan.xuanhttplibrary.okhttp.result.Result.CODE_SUCCESS && result.getData() != null) {
                            mMyBankCardBean = result.getData();
                            if (mMyBankCardBean == null || mMyBankCardBean.getPageData() == null) {
                                if (isComeInGet) {
                                    showErrorDialog();
                                } else {
                                    ToastUtil.showToast(NewBankTransferActivity.this, R.string.failed_to_get_my_bank_card_list);
                                }
                                return;
                            } else if (mMyBankCardBean.getPageData().size() <= 0) {
                                if (isComeInGet) {
                                    showAddBankCardDialog();
                                    ToastUtil.showToast(NewBankTransferActivity.this, R.string.please_add_a_bank_card);
                                    return;
                                }
                            }
                            updateMyBankList();
                            //开始倒计时 并下单
                            if (isComeInGet || !isDownOrder) {
                                submitOrder();
                            }
                        } else {
                            if (isComeInGet) {
                                showErrorDialog();
                            } else {
                                ToastUtil.showToast(NewBankTransferActivity.this, R.string.failed_to_get_my_bank_card_list);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (isComeInGet) {
                            showErrorDialog();
                        }
                        ToastUtil.showNetError(NewBankTransferActivity.this);
                    }
                });
    }

    private void showErrorDialog() {
        Dialog dialog = new Dialog(this, R.style.MyDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_bank_pay_success, null);
        ImageView ivClose = view.findViewById(R.id.iv_close);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(R.string.point);
        TextView tvContent = view.findViewById(R.id.tv_content);
        tvContent.setText(R.string.failed_to_get_bank_card_list_please_try_again);

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
        Button bok = view.findViewById(R.id.b_ok);
        bok.setText(R.string.retry);
        bok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                getMyBankList(true);
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);
        dialog.show();

    }

    private void startCountTime() {
        isDownOrder = true;
        mTvTimeHint.setText(R.string.please_complete_the_transfer_within_10_minutes);
        CountDownTimer countDownTimer = new CountDownTimer(10 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long min = millisUntilFinished / 1000;
                mTvTime.setText(((min / 60) < 10 ? "0" + (min / 60) : (min / 60)) + ":" + ((min % 60) < 10 ? "0" + (min % 60) : (min % 60)));
            }

            @Override
            public void onFinish() {
                mTvTime.setText("00:00");
                showSuccess(getResources().getString(R.string.bank_pay_hint));
            }
        };
        countDownTimer.start();
    }

    private void updateMyBankList() {
        llMyBankCardList.removeViews(1, llMyBankCardList.getChildCount() - 1);
        if (mMyBankCardBean.getPageData() != null) {
            List<MyBankCardBean.PageDataBean> pageDataBeans = mMyBankCardBean.getPageData();
            for (int i = 0; i < pageDataBeans.size(); i++) {
                MyBankCardBean.PageDataBean pageDataBean = pageDataBeans.get(i);
                View lineView = new View(this);
                ViewGroup.LayoutParams lineP = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(this, 0.5f));
                lineView.setLayoutParams(lineP);
                lineView.setBackgroundColor(getResources().getColor(R.color.find_line));
                llMyBankCardList.addView(lineView);
                View view = LayoutInflater.from(NewBankTransferActivity.this).inflate(R.layout.item_my_bank_card, null);
                view.setId(i);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(NewBankTransferActivity.this, 55));
                view.setLayoutParams(layoutParams);
                TextView tvBankNameAndNum = view.findViewById(R.id.tv_bank_name_and_num);
                TextView tvName = view.findViewById(R.id.tv_name);
                String bankNum = "";
                if (!TextUtils.isEmpty(pageDataBean.getBankNumber()) && pageDataBean.getBankNumber().length() >= 4) {
                    bankNum = pageDataBean.getBankNumber().substring(pageDataBean.getBankNumber().length() - 4);
                } else if (!TextUtils.isEmpty(pageDataBean.getBankNumber())) {
                    bankNum = pageDataBean.getBankNumber();
                }
                if (TextUtils.isEmpty(bankNum)) {
                    tvBankNameAndNum.setText(pageDataBean.getBankName());
                } else {
                    tvBankNameAndNum.setText(pageDataBean.getBankName() + "(尾号" + bankNum + ")");
                }
                tvName.setText(pageDataBean.getBankUserName() + "(已绑定)");
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showDelMyBankCard(v.getId());
                        return false;
                    }
                });
                llMyBankCardList.addView(view);
            }
        } else {
            ToastUtil.showToast(NewBankTransferActivity.this, R.string.failed_to_get_my_bank_card_list);
        }
    }


    /**
     * 删除 银行卡
     *
     * @param id
     */
    private void showDelMyBankCard(int id) {
        new AskCommonBottomDialog(NewBankTransferActivity.this,
                getResources().getString(R.string.are_you_sure_you_want_to_delete_this_card), new AskCommonBottomDialog.AskCommonBottomDialogListener() {
            @Override
            public void onClickOk(TextView tvOk) {
                delMyBankCard(id);
            }

            @Override
            public void onClickCancel(TextView tvCancel) {

            }

            @Override
            public void onClickContent(TextView tvContent, String trim) {

            }
        }).show();

    }

    private void delMyBankCard(int id) {
        if (mMyBankCardBean != null && mMyBankCardBean.getPageData() != null && mMyBankCardBean.getPageData().get(id) != null) {
            String bankId = mMyBankCardBean.getPageData().get(id).getBankId();
            Map<String, String> par = new HashMap<>();
            par.put("bankId", bankId);
            par.put("access_token", coreManager.getSelfStatus().accessToken);
            HttpUtils.get().url(coreManager.getConfig().DEL_MY_BANK_CARD).params(par).build().execute(new BaseCallback<String>(String.class) {
                @Override
                public void onResponse(ObjectResult<String> result) {
                    if (result.getResultCode() == com.xuan.xuanhttplibrary.okhttp.result.Result.CODE_SUCCESS) {
                        getMyBankList(false);
                        ToastUtil.showToast(R.string.successfully_deleted);
                    } else {
                        ToastUtil.showToast(NewBankTransferActivity.this, TextUtils.isEmpty(result.getResultMsg()) ?
                                getResources().getString(R.string.deleting_bank_card_failed)
                                : result.getResultMsg());
                    }
                }

                @Override
                public void onError(Call call, Exception e) {
                    ToastUtil.showNetError(NewBankTransferActivity.this);
                }
            });
        } else {
            ToastUtil.showToast(NewBankTransferActivity.this, R.string.deleting_bank_card_failed);
        }

    }

    private void event() {
        mLlAddBankCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddBankCardDialog();
            }
        });
        mTblMenu.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateBankInfo(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                updateBankInfo(tab.getPosition());
            }
        });
//        mTvTitleRight.setOnClickListener(v -> {
//            submitOrder();
//        });
    }

    private void updateBankInfo(int position) {
        if (mBankListBeans != null) {
            BankListBean bean = mBankListBeans.get(position);
            if (bean != null) {
                mTvDepositName.setText(bean.getAccountName());
                mTvBankCardName.setText(bean.getBankNumber());
                mTvBranchBankName.setText(bean.getAccountAddr());
            }
        }
    }

    private void showAddBankCardDialog() {
        mAddBankCardDialog = DialogHelper.showAddBankCardDialog(this, new AddBankCardDialog.AddBankCardSureListener() {
            @Override
            public void onClick(View v, String accountName, String bankCard) {
                addBankCard(accountName, bankCard);
            }
        });
    }

    private void addBankCard(String accountName, String bankCard) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> par = new HashMap<>();
        par.put("access_token", coreManager.getSelfStatus().accessToken);
        par.put("realName", accountName);
        par.put("cardNum", bankCard);
        HttpUtils.get().url(coreManager.getConfig().ADD_BANK_CARD).params(par).build().execute(new BaseCallback<String>(String.class) {
            @Override
            public void onResponse(ObjectResult<String> result) {
                DialogHelper.dismissProgressDialog();
                if (result.getResultCode() == Result.CODE_SUCCESS) {
                    try {
                        JSONObject jsonObject = new JSONObject(result.getData());
                        switch (jsonObject.getString("code")) {
                            case "1":
                                mAddBankCardDialog.mDialog.dismiss();
                                getMyBankList(false);
                                ToastUtil.showToast(NewBankTransferActivity.this, R.string.add_a_bank_card_successfully);
                                break;
                            default:
                                ToastUtil.showToast(NewBankTransferActivity.this, TextUtils.isEmpty(jsonObject.getString("msg"))
                                        ? getResources().getString(R.string.adding_a_bank_card_failed)
                                        : jsonObject.getString("msg"));
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ToastUtil.showToast(NewBankTransferActivity.this,
                                getResources().getString(R.string.adding_a_bank_card_failed)
                        );
                    }
                } else {
                    ToastUtil.showToast(NewBankTransferActivity.this,
                            TextUtils.isEmpty(result.getResultMsg())
                                    ? getResources().getString(R.string.adding_a_bank_card_failed)
                                    : result.getResultMsg());
                }
            }

            @Override
            public void onError(Call call, Exception e) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showNetError(NewBankTransferActivity.this);
            }
        });
    }

    private void initView() {
        initActionBar();
        mTvTime = findViewById(R.id.tv_time);
        mLlAddBankCard = findViewById(R.id.ll_add_bank_card);
        llMyBankCardList = findViewById(R.id.ll_my_bank_card_list);
        mTblMenu = findViewById(R.id.tbl_menu);
        mTvTimeHint = findViewById(R.id.tv_time_hint);
        mTvTimeHint.setText(R.string.please_add_a_bank_card);

        mTvDepositName = findViewById(R.id.tv_deposit_name);
        mTvBankCardName = findViewById(R.id.tv_bank_card_num);
        mTvBranchBankName = findViewById(R.id.tv_branch_bank_name);

        ImageView ivCopyName = findViewById(R.id.iv_copy_name);
        ImageView ivCopyCardNum = findViewById(R.id.iv_copy_card_num);
        ImageView ivCopyBranchBankName = findViewById(R.id.iv_copy_branch_bank_name);
        initCopy(ivCopyName, mTvDepositName);
        initCopy(ivCopyCardNum, mTvBankCardName);
        initCopy(ivCopyBranchBankName, mTvBranchBankName);
    }

    private void initCopy(View view, TextView tvContent) {
        view.setOnClickListener(v -> {
            //获取剪贴板管理器：
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("Label", tvContent.getText().toString().trim());
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);
            ToastUtil.showToast(this, R.string.successful_copy);
        });
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitleCenter = findViewById(R.id.tv_title_center);
        tvTitleCenter.setText(R.string.bank_transfer);
//        mTvTitleRight = findViewById(R.id.tv_title_right);
//        mTvTitleRight.setText(R.string.confirm);
//        mTvTitleRight.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBack = false;
        if (isTransferSuccess) {
            showSuccess(getResources().getString(R.string.payment_is_successful));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isBack = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isBack = true;
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventBankRechargeInfo message) {
        if (message != null) {
            ChatMessage chatMessage = message.getChatMessage();
            if (chatMessage != null) {
                try {
                    String content = chatMessage.getContent();
                    if (!TextUtils.isEmpty(content)) {
                        JSONObject jsonObject = new JSONObject(content);
                        String payStatus = jsonObject.getString("payStatus");
                        if (!TextUtils.isEmpty(payStatus) && payStatus.equals("1")) {
                            isTransferSuccess = true;
                            if (!isBack) { //不是在后台直接弹窗
                                showSuccess(getResources().getString(R.string.payment_is_successful));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
