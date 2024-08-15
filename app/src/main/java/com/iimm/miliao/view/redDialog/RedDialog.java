package com.iimm.miliao.view.redDialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.redpacket.RedDialogBean;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.Constants;

/**
 * 红包展示
 */
public class RedDialog extends Dialog {

    private RelativeLayout mRedRl, mRedPrivilegeRl;
    private ImageView mAvatarIv, mOpenIv, mCloseIv, mClosePrivilegeIv;
    private TextView mNameTv, mContentTv, mStatus, mPrivilegeTv;
    private RedDialogBean mRedDialogBean;
    private TextView mBottomOrdinaryTv, mBottomPrivilegeTv;
    private EditText mTenEt, mSingleEt, mTenEt1;
    private InputMethodManager inputmanger;
    private Context mContext;

    private FrameAnimation mFrameAnimation;
    private int[] mImgResIds = new int[]{
            R.mipmap.icon_open_red_packet1,
            R.mipmap.icon_open_red_packet2,
            R.mipmap.icon_open_red_packet3,
            R.mipmap.icon_open_red_packet4,
            R.mipmap.icon_open_red_packet5,
            R.mipmap.icon_open_red_packet6,
            R.mipmap.icon_open_red_packet7,
            R.mipmap.icon_open_red_packet7,
            R.mipmap.icon_open_red_packet8,
            R.mipmap.icon_open_red_packet9,
            R.mipmap.icon_open_red_packet4,
            R.mipmap.icon_open_red_packet10,
            R.mipmap.icon_open_red_packet11,
    };

    private OnClickRedListener mOnClickRedListener;

    public RedDialog(Context context, RedDialogBean redDialogBean, OnClickRedListener onClickRedListener) {
        super(context, R.style.MyDialog);
        this.mContext = context;
        this.mRedDialogBean = redDialogBean;
        this.mOnClickRedListener = onClickRedListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_red_packet);
        initView();
        initData();
        initEvent();
        inputmanger = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        Window window = getWindow();
        assert window != null;
        WindowManager.LayoutParams lp = window.getAttributes();
        // lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        // lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.setGravity(Gravity.CENTER);
    }

    private void initView() {
        mRedRl = findViewById(R.id.rl_red);
        mAvatarIv = findViewById(R.id.iv_avatar);
        mNameTv = findViewById(R.id.tv_name);
        mContentTv = findViewById(R.id.tv_msg);
        mOpenIv = findViewById(R.id.iv_open);
        mCloseIv = findViewById(R.id.iv_close);
        mStatus = findViewById(R.id.status_tv);
        mPrivilegeTv = findViewById(R.id.red_packet_privilege_tv);
        if (mRedDialogBean.isShowGroup() && mRedDialogBean.getRedVip() == 1 && mRedDialogBean.getRedType() == 2 && Constants.SUPPORT_READ_PRIVILEGE_RED) {
            mPrivilegeTv.setVisibility(View.VISIBLE);
        } else {
            mPrivilegeTv.setVisibility(View.GONE);
        }
        mRedPrivilegeRl = findViewById(R.id.rl_red_privilege);
        mClosePrivilegeIv = findViewById(R.id.iv_close_privilege);
        mBottomOrdinaryTv = findViewById(R.id.bottom_ordinary_opening_tv);
        mTenEt = findViewById(R.id.bottom_ten_et);
        mTenEt1 = findViewById(R.id.bottom_ten_et1);
        mSingleEt = findViewById(R.id.bottom_single_digit_et);
        mBottomPrivilegeTv = findViewById(R.id.bottom_privilege_tv);
    }

    private void initData() {
        AvatarHelper.getInstance().displayAvatar(mRedDialogBean.getUserName(), mRedDialogBean.getUserId(),
                mAvatarIv, true);
        Friend friend = FriendDao.getInstance().getFriend(CoreManager.getSelf(mContext).getUserId(), mRedDialogBean.getUserId());
        String name  = "";
        if (friend != null) {
            name = TextUtils.isEmpty(friend.getRemarkName()) ? mRedDialogBean.getUserName() : friend.getRemarkName();
        } else {
            name = mRedDialogBean.getUserName();
        }
        mNameTv.setText(name);

        mContentTv.setText(mRedDialogBean.getWords());

        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_red);
        mRedRl.setAnimation(animation);
    }

    private void startAnim() {
        mFrameAnimation = new FrameAnimation(mOpenIv, mImgResIds, 125, true);
        mFrameAnimation.setAnimationListener(new FrameAnimation.AnimationListener() {
            @Override
            public void onAnimationStart() {

            }

            @Override
            public void onAnimationEnd() {

            }

            @Override
            public void onAnimationRepeat() {

            }

            @Override
            public void onAnimationPause() {
                mOpenIv.setBackgroundResource(R.mipmap.icon_open_red_packet1);
            }
        });
    }

    private void initEvent() {
        mOpenIv.setOnClickListener(v -> {
            if (mFrameAnimation != null) {
                return;
            }
            startAnim();


            if(mRedDialogBean.isShowGroup()){
                if (mOnClickRedListener != null) {
                    mOnClickRedListener.clickAd();
                }
                return;
            }
            if (mOnClickRedListener != null) {
                mOnClickRedListener.clickRed();
            }
        });

        mCloseIv.setOnClickListener(v -> {
            stopAnim();
            dismiss();
        });
        mClosePrivilegeIv.setOnClickListener(v -> {
            stopAnim();
            dismiss();
        });
        mPrivilegeTv.setOnClickListener(v -> {
            mRedRl.setVisibility(View.GONE);
            mRedPrivilegeRl.setVisibility(View.VISIBLE);
            mTenEt1.setFocusable(true);
            mTenEt1.setFocusableInTouchMode(true);
            mTenEt1.requestFocus();
            if (inputmanger != null) {
                inputmanger.showSoftInput(mTenEt1, 0);
            }
        });
        mBottomPrivilegeTv.setOnClickListener(v -> {
//            if (TextUtils.isEmpty(mTenEt1.getText().toString().trim()) && TextUtils.isEmpty(mTenEt.getText().toString().trim()) && TextUtils.isEmpty(mSingleEt.getText().toString().trim())) {
//                ToastUtil.showLongToast(mContext, "请输入金额");
//            } else {
            if (mOnClickRedListener != null) {
                String et1 = TextUtils.isEmpty(mTenEt1.getText().toString().trim()) ? "0" : mTenEt1.getText().toString().trim();
                String et2 = TextUtils.isEmpty(mTenEt.getText().toString().trim()) ? "0" : mTenEt.getText().toString().trim();
                String et3 = TextUtils.isEmpty(mSingleEt.getText().toString().trim()) ? "0" : mSingleEt.getText().toString().trim();
                mOnClickRedListener.clickPrivilege(et1 + "." + et2 + et3);
            }
//            }
        });
        mBottomOrdinaryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFrameAnimation != null) {
                    return;
                }
                startAnim();
                if (mOnClickRedListener != null) {
                    mOnClickRedListener.clickRed();
                }
            }
        });

        mTenEt1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!TextUtils.isEmpty(s.toString().trim())) {
                    mTenEt.setFocusable(true);
                    mTenEt.setFocusableInTouchMode(true);
                    mTenEt.requestFocus();
                }

            }
        });
        mTenEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s.toString().trim())) {
                    mSingleEt.setFocusable(true);
                    mSingleEt.setFocusableInTouchMode(true);
                    mSingleEt.requestFocus();
                }
            }
        });
        mSingleEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void stopAnim() {
        if (inputmanger != null) {
            inputmanger.hideSoftInputFromWindow(mTenEt.getWindowToken(), 0);
            inputmanger.hideSoftInputFromWindow(mSingleEt.getWindowToken(), 0);
        }

        if (mFrameAnimation != null) {
            mFrameAnimation.release();
            mFrameAnimation = null;
        }
    }

    public interface OnClickRedListener {
        void clickRed();

        void clickPrivilege(String money);

        void clickAd();
    }
}
