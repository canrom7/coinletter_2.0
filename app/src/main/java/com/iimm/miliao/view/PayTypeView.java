package com.iimm.miliao.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.PayType;
import com.iimm.miliao.util.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;

public class PayTypeView extends LinearLayout {

    public static final int ALI_PAY = 1;
    public static final int WX_PAY = 2;
    public static final int BANK_PAY = 3;
    private View mRootView;
    private LinearLayout mAliPay;
    private LinearLayout mWxPay;
    private LinearLayout mBankCard;
    private ImageView mIvAliCheck;
    private ImageView mIvWxCheck;
    private ImageView mIvCardCheck;
    private FrameLayout mFlAliPay;
    private FrameLayout mFlWxPay;
    private FrameLayout mFlBankCardPay;
    private int current_status = 0;
    private View inputView;
    private EditText mEtInputAccountNum;
    private TextView mTvInputHint;
    private String mAliAcountHis;
    private String mWxAccountHis;
    private String mBankCardAccountHis;
    private List<PayType> payTypeList = new ArrayList<>();

    public PayTypeView(Context context) {
        super(context);
        init();
    }

    public PayTypeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        mRootView = LayoutInflater.from(getContext()).inflate(R.layout.custom_pay_type_view, this, true);
        mAliPay = mRootView.findViewById(R.id.ll_ali_pay);
        mWxPay = mRootView.findViewById(R.id.ll_wx_pay);
        mBankCard = mRootView.findViewById(R.id.ll_bank_card);
        mIvAliCheck = mRootView.findViewById(R.id.iv_ali);
        mIvWxCheck = mRootView.findViewById(R.id.iv_wx);
        mIvCardCheck = mRootView.findViewById(R.id.iv_card);
        mFlAliPay = mRootView.findViewById(R.id.fl_ali_pay);
        mFlWxPay = mRootView.findViewById(R.id.fl_wx_pay);
        mFlBankCardPay = mRootView.findViewById(R.id.fl_bank_card_pay);
        event();
        mAliPay.setVisibility(GONE);
        mFlAliPay.setVisibility(GONE);
        mWxPay.setVisibility(GONE);
        mFlWxPay.setVisibility(GONE);
        mBankCard.setVisibility(GONE);
        mFlBankCardPay.setVisibility(GONE);
    }

    private void event() {
        mAliPay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStatus(v);
            }
        });
        mWxPay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStatus(v);
            }
        });
        mBankCard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStatus(v);
            }
        });
        mIvAliCheck.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStatus(v);
            }
        });
        mIvWxCheck.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStatus(v);
            }
        });
        mIvCardCheck.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStatus(v);
            }
        });
    }

    private void changeStatus(View v) {
        switch (v.getId()) {
            case R.id.ll_ali_pay:
            case R.id.iv_ali:
                change(ALI_PAY);
                break;
            case R.id.ll_wx_pay:
            case R.id.iv_wx:
                change(WX_PAY);
                break;
            case R.id.ll_bank_card:
            case R.id.iv_card:
                change(BANK_PAY);
                break;
        }

    }

    private void change(int type) {
        if (current_status == type) {
            return;
        }
        current_status = type;
        switch (current_status) {
            case ALI_PAY:
                mIvAliCheck.setImageResource(R.drawable.sel_check_wx_new);
                mIvWxCheck.setImageResource(R.drawable.sel_nor_wx2);
                mIvCardCheck.setImageResource(R.drawable.sel_nor_wx2);
                inflateInputView(current_status);
                break;
            case WX_PAY:
                mIvAliCheck.setImageResource(R.drawable.sel_nor_wx2);
                mIvWxCheck.setImageResource(R.drawable.sel_check_wx_new);
                mIvCardCheck.setImageResource(R.drawable.sel_nor_wx2);
                inflateInputView(current_status);
                break;
            case BANK_PAY:
                mIvAliCheck.setImageResource(R.drawable.sel_nor_wx2);
                mIvWxCheck.setImageResource(R.drawable.sel_nor_wx2);
                mIvCardCheck.setImageResource(R.drawable.sel_check_wx_new);
                inflateInputView(current_status);
                break;
        }
    }

    private void inflateInputView(int status) {
        if (inputView == null) {
            inputView = LayoutInflater.from(getContext()).inflate(R.layout.pay_input_view, null);
            mEtInputAccountNum = inputView.findViewById(R.id.et_input_coin_address);
            mTvInputHint = inputView.findViewById(R.id.tv_input_hint);
        }
        switch (status) {
            case ALI_PAY:
                if (mFlWxPay.getChildCount() != 0) {
                    mFlWxPay.removeAllViews();
                    mWxAccountHis = mEtInputAccountNum.getText().toString().trim();
                }
                if (mFlBankCardPay.getChildCount() != 0) {
                    mFlBankCardPay.removeAllViews();
                    mBankCardAccountHis = mEtInputAccountNum.getText().toString().trim();
                }
                if (mFlAliPay.getChildCount() == 0) {
                    mFlAliPay.addView(inputView);
                    if (!TextUtils.isEmpty(mAliAcountHis)) {
                        mEtInputAccountNum.setText(mAliAcountHis);
                        mEtInputAccountNum.setSelection(mAliAcountHis.length());
                    } else {
                        mEtInputAccountNum.setText("");
                    }
                    mEtInputAccountNum.setHint(R.string.fill_in_the_alipay_account);
                    mTvInputHint.setText(R.string.ali_pay_input_hint);
                }
                break;
            case WX_PAY:
                if (mFlAliPay.getChildCount() != 0) {
                    mFlAliPay.removeAllViews();
                    mAliAcountHis = mEtInputAccountNum.getText().toString().trim();
                }

                if (mFlBankCardPay.getChildCount() != 0) {
                    mFlBankCardPay.removeAllViews();
                    mBankCardAccountHis = mEtInputAccountNum.getText().toString().trim();
                }
                if (mFlWxPay.getChildCount() == 0) {
                    mFlWxPay.addView(inputView);
                    if (!TextUtils.isEmpty(mWxAccountHis)) {
                        mEtInputAccountNum.setText(mWxAccountHis);
                        mEtInputAccountNum.setSelection(mWxAccountHis.length());
                    } else {
                        mEtInputAccountNum.setText("");
                    }
                    mEtInputAccountNum.setHint(R.string.fill_in_the_wx_account);
                    mTvInputHint.setText(R.string.wx_pay_input_hint);
                }
                break;
            case BANK_PAY:
                if (mFlAliPay.getChildCount() != 0) {
                    mFlAliPay.removeAllViews();
                    mAliAcountHis = mEtInputAccountNum.getText().toString().trim();
                }
                if (mFlWxPay.getChildCount() != 0) {
                    mFlWxPay.removeAllViews();
                    mWxAccountHis = mEtInputAccountNum.getText().toString().trim();
                }
                if (mFlBankCardPay.getChildCount() == 0) {
                    mFlBankCardPay.addView(inputView);
                    if (!TextUtils.isEmpty(mBankCardAccountHis)) {
                        mEtInputAccountNum.setText(mBankCardAccountHis);
                        mEtInputAccountNum.setSelection(mBankCardAccountHis.length());
                    } else {
                        mEtInputAccountNum.setText("");
                    }
                    mEtInputAccountNum.setHint(R.string.fill_in_the_bank_account);
                    mTvInputHint.setText(R.string.bank_pay_input_hint);
                }
                break;
        }

    }


    public String[] getInputData() {
        String[] data = new String[2];
        data[0] = current_status + "";
        data[1] = mEtInputAccountNum.getText().toString().trim();
        return data;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAliAcountHis = PreferenceUtils.getString(getContext(), "PAY_HISTORY_" + ALI_PAY);
        mWxAccountHis = PreferenceUtils.getString(getContext(), "PAY_HISTORY_" + WX_PAY);
        mBankCardAccountHis = PreferenceUtils.getString(getContext(), "PAY_HISTORY_" + BANK_PAY);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        PreferenceUtils.putString(getContext(), "PAY_HISTORY_" + ALI_PAY, mAliAcountHis);
        PreferenceUtils.putString(getContext(), "PAY_HISTORY_" + WX_PAY, mWxAccountHis);
        PreferenceUtils.putString(getContext(), "PAY_HISTORY_" + BANK_PAY, mBankCardAccountHis);
    }

    public void setData(List<PayType> payTypeList) {
        if (payTypeList != null) {
            this.payTypeList = payTypeList;
            mAliPay.setVisibility(GONE);
            mFlAliPay.setVisibility(GONE);
            mWxPay.setVisibility(GONE);
            mFlWxPay.setVisibility(GONE);
            mBankCard.setVisibility(GONE);
            mFlBankCardPay.setVisibility(GONE);
            for (int i = 0; i < payTypeList.size(); i++) {
                PayType payType = payTypeList.get(i);
                if (payType.getZfid() == 1) {
                    if (i == 0) {
                        changeStatus(mWxPay);
                    }
                    mWxPay.setVisibility(VISIBLE);
                    mFlWxPay.setVisibility(VISIBLE);
                } else if (payType.getZfid() == 2) {
                    if (i == 0) {
                        changeStatus(mAliPay);
                    }
                    mAliPay.setVisibility(VISIBLE);
                    mFlAliPay.setVisibility(VISIBLE);
                } else if (payType.getZfid() == 3) {
                    if (i == 0) {
                        changeStatus(mBankCard);
                    }
                    mBankCard.setVisibility(VISIBLE);
                    mFlBankCardPay.setVisibility(VISIBLE);
                }
            }

        }
    }


}
