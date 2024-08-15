package com.iimm.miliao.ui.me.redpacket;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.BankListBean;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.ToastUtil;
import com.weigan.loopview.LoopView;
import com.weigan.loopview.OnItemSelectedListener;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class BankTransferActivity extends BaseActivity {
    private TextView mTvCollectionName;
    private TextView mSBankList;
    private TextView mTvCopyCollectionName;
    private TextView mTvBankNum;
    private TextView mTvCopyBankNum;
    private TextView mTvBankName;
    private TextView mTvCopyBankName;
    private TextView mTvMoney;
    private EditText mEtInputBankPelName;
    private Button mBSubMit;
    private String money = "0.0";
    List<BankListBean> datas;
    List<String> displayDatas;
    private int index = -1;
    private boolean isReday;
    private EditText mEtInputbankNum;
    private String mBanpelName;
    private String mInputBankNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_transfer);
        datas = new ArrayList<>();
        displayDatas = new ArrayList<>();
        initView();
        event();
        getData();
    }

    private void getData() {
        Intent intent = getIntent();
        if (intent != null) {
            money = intent.getStringExtra("money");
        }
        mTvMoney.setText("￥" + money);
        getBankList(false);
    }

    /**
     * 获取银行列表
     */
    private void getBankList(boolean isShow) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> par = new HashMap<>();
        par.put("access_token", coreManager.getSelfStatus().accessToken);
        HttpUtils.get().url(coreManager.getConfig().GET_BANK_LIST).params(par).build().execute(new ListCallback<BankListBean>(BankListBean.class) {
            @Override
            public void onResponse(ArrayResult<BankListBean> result) {
                DialogHelper.dismissProgressDialog();
                if (result.getResultCode() == 1 && result.getData() != null) {
                    isReday = true;
                    List<BankListBean> bankListBeans = result.getData();
                    datas.clear();
                    datas.addAll(bankListBeans);
                    displayDatas.clear();
                    for (int i = 0; i < bankListBeans.size(); i++) {
                        displayDatas.add(bankListBeans.get(i).getBankName());
                    }
                    if (datas.size() > 0) {
                        index = 0;
                        BankListBean listBean = datas.get(0);
                        updateView(listBean);
                    }
                    if (isShow) {
                        showBankList();
                    }
                    mTvCopyCollectionName.setVisibility(View.VISIBLE);
                    mTvCopyBankNum.setVisibility(View.VISIBLE);
                    mTvCopyBankName.setVisibility(View.VISIBLE);
                } else {
                    ToastUtil.showToast(BankTransferActivity.this, TextUtils.isDigitsOnly(result.getResultMsg())
                            ? getResources().getString(R.string.failed_to_load_bank_card_list)
                            : result.getResultMsg());
                }
            }

            @Override
            public void onError(Call call, Exception e) {
                ToastUtil.showErrorData(BankTransferActivity.this);
                DialogHelper.dismissProgressDialog();
            }
        });
    }

    private void updateView(BankListBean listBean) {
        mSBankList.setText(listBean.getBankName());
        mTvCollectionName.setText(listBean.getAccountName());
        mTvBankNum.setText(listBean.getBankNumber());
        mTvBankName.setText(listBean.getAccountAddr());
    }

    private void event() {
        mBSubMit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInfo()) {
                    submit();
                }
            }
        });
        mSBankList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isReday) {
                    showBankList();
                } else {
                    getBankList(true);
                }
            }
        });
    }

    private void submit() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> par = new HashMap<>();
        par.put("access_token", coreManager.getSelfStatus().accessToken);
        par.put("serialAmount", money);
        par.put("drawee", mBanpelName);
        par.put("payNumber", mInputBankNum);
        par.put("paymentChannels", datas.get(index).getId());
        HttpUtils.get().url(coreManager.getConfig().SUBMIT_BANK_INFO_PAY).params(par).build().execute(new ListCallback<BankListBean>(BankListBean.class) {
            @Override
            public void onResponse(ArrayResult<BankListBean> result) {
                DialogHelper.dismissProgressDialog();
                if (result.getResultCode() == 1) {
                    showSuccess();
                } else {
                    ToastUtil.showToast(BankTransferActivity.this, TextUtils.isEmpty(result.getResultMsg())
                            ? getResources().getString(R.string.failed_to_submit_order)
                            : result.getResultMsg());
                }
            }

            @Override
            public void onError(Call call, Exception e) {
                ToastUtil.showErrorData(BankTransferActivity.this);
                DialogHelper.dismissProgressDialog();
            }
        });
    }

    private void showSuccess() {
        Dialog dialog = new Dialog(this, R.style.MyDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_bank_pay_success, null);
        ImageView ivClose = view.findViewById(R.id.iv_close);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
        Button bok = view.findViewById(R.id.b_ok);
        bok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);
        dialog.show();
    }

    private void showBankList() {
        Dialog dialog = new Dialog(this, R.style.MyDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.item_bank_list, null);
        LoopView loopView = view.findViewById(R.id.loopView);
        loopView.setItems(displayDatas);
        loopView.setNotLoop();
        loopView.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                BankTransferActivity.this.index = index;
                if (BankTransferActivity.this.index >= 0 && BankTransferActivity.this.index < datas.size()) {
                    updateView(datas.get(BankTransferActivity.this.index));
                }
            }
        });
        TextView tvOk = view.findViewById(R.id.tv_ok);
        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BankTransferActivity.this.index = loopView.getSelectedItem();
                if (BankTransferActivity.this.index >= 0 && BankTransferActivity.this.index < datas.size()) {
                    updateView(datas.get(BankTransferActivity.this.index));
                }
                dialog.dismiss();
            }
        });
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
        dialog.show();
    }

    /**
     * 检查输入的信息
     *
     * @return
     */
    private boolean checkInfo() {
        mBanpelName = mEtInputBankPelName.getText().toString().trim();
        mInputBankNum = mEtInputbankNum.getText().toString().trim();
        if (TextUtils.isEmpty(mBanpelName) || TextUtils.isEmpty(mInputBankNum)) {
            ToastUtil.showToast(this, R.string.please_enter_full_information);
            return false;
        }
        if (index == -1) {
            ToastUtil.showToast(this, R.string.please_select_your_bank_card_first);
            return false;
        }
        return true;
    }

    private void initView() {
        initActionBar();
        mSBankList = findViewById(R.id.s_bank_list);
        mTvCollectionName = findViewById(R.id.tv_collection_name);
        mTvCopyCollectionName = findViewById(R.id.tv_copy_name);
        mTvBankNum = findViewById(R.id.tv_bank_num);
        mTvCopyBankNum = findViewById(R.id.tv_copy_bank_num);
        mTvBankName = findViewById(R.id.tv_bank_name);
        mTvCopyBankName = findViewById(R.id.tv_copy_bank);
        mTvMoney = findViewById(R.id.tv_money);
        mEtInputBankPelName = findViewById(R.id.et_input_bank_pel_name);
        mEtInputbankNum = findViewById(R.id.et_input_bank_num);
        mBSubMit = findViewById(R.id.b_submit);

        initCopy(mTvCopyCollectionName, mTvCollectionName);
        initCopy(mTvCopyBankNum, mTvBankNum);
        initCopy(mTvCopyBankName, mTvBankName);

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
    }

}
