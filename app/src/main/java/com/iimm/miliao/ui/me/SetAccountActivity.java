package com.iimm.miliao.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.db.dao.UserDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.CommonalityTools;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 设置app_name号
 */
public class SetAccountActivity extends BaseActivity {
    private String mUserId, mUserName;
    private ImageView mAccountAvatarIv;
    private TextView mAccountNameTv;
    private EditText mAccountInputEt;
    private Button mAccountSureBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_account);
        mUserId = getIntent().getStringExtra(AppConstant.EXTRA_USER_ID);
        mUserName = getIntent().getStringExtra(AppConstant.EXTRA_NICK_NAME);
        initActionBar();
        initView();
        initData();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.easy_account_set, getString(R.string.easy_account_code)));
    }

    private void initView() {
        TextView singleDesc = findViewById(R.id.easy_account_single_tv);
        singleDesc.setText(getString(R.string.easy_account_single, getString(R.string.easy_account_code)));
        mAccountAvatarIv = findViewById(R.id.a_avatar_iv);
        if (mAccountAvatarIv instanceof RoundedImageView) {
            if (Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR) {
                ((RoundedImageView) mAccountAvatarIv).setOval(true);
            } else {
                ((RoundedImageView) mAccountAvatarIv).setOval(false);
                ((RoundedImageView) mAccountAvatarIv).setCornerRadius(Constants.AVATAR_CORNER_RADIUS);
            }
        }
        mAccountNameTv = findViewById(R.id.a_name_tv);
        mAccountInputEt = findViewById(R.id.a_input_et);
        mAccountSureBtn = findViewById(R.id.a_sure_btn);
//        mAccountSureBtn.setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());

    }

    private void initData() {
        AvatarHelper.getInstance().displayAvatar(mUserId, mAccountAvatarIv, true);
        mAccountNameTv.setText(mUserName);
        mAccountSureBtn.setOnClickListener(v -> {
            String account = mAccountInputEt.getText().toString();
            if (TextUtils.isEmpty(account)) {
                ToastUtil.showToast(mContext, getString(R.string.name_connot_null));
                return;
            }
            String userId = mAccountInputEt.getText().toString().trim();
            if (CommonalityTools.isLetterDigit(userId)) {
                ToastUtil.showToast(SetAccountActivity.this, "用户名不能全为数字！");
                return;
            }
            updateAccount(account);
        });
    }

    private void updateAccount(String account) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getSelfStatus(mContext).accessToken);
        params.put("account", account);

        HttpUtils.get().url(coreManager.getConfig().USER_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            coreManager.getSelf().setAccount(account);
                            coreManager.getSelf().setSetAccountCount(1);
                            // 更新数据库
                            UserDao.getInstance().updateAccount(mUserId, account);

                            Intent intent = new Intent();
                            intent.putExtra(AppConstant.EXTRA_USER_ACCOUNT, account);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            ToastUtil.showToast(SetAccountActivity.this, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(SetAccountActivity.this);
                    }
                });
    }
}
