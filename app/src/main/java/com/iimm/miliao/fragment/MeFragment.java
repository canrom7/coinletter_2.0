package com.iimm.miliao.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.iimm.miliao.ui.me.SignInRedActivity;
import com.payeasenet.wepay.ui.activity.PayWebViewActivity;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.EventBusMsg;
import com.iimm.miliao.course.LocalCourseActivity;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.MainActivity;
import com.iimm.miliao.ui.base.EasyFragment;
import com.iimm.miliao.ui.circle.BusinessCircleActivity;
import com.iimm.miliao.ui.contacts.RoomActivity;
import com.iimm.miliao.ui.me.BasicInfoEditActivity;
import com.iimm.miliao.ui.me.MyCollection;
import com.iimm.miliao.ui.me.PrivacySettingActivity;
import com.iimm.miliao.ui.me.SecureSettingActivity;
import com.iimm.miliao.ui.me.SettingActivity;
import com.iimm.miliao.ui.me.redpacket.WxPayBlance;
import com.iimm.miliao.ui.tool.MyWebViewActivity;
import com.iimm.miliao.ui.tool.SingleImagePreviewActivity;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.SkinUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.view.NoDoubleClickListener;
import com.iimm.miliao.view.SelectionFrame;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 我的界面
 */
public class MeFragment extends EasyFragment implements View.OnClickListener {

    private ImageView mAvatarImg;
    private TextView mNickNameTv;
    private TextView mPhoneNumTv;
    private TextView skyTv, setTv;

    public MeFragment() {
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_me;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            initView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void initView() {
        ((TextView) (findViewById(R.id.tv_title_center))).setText(getString(R.string.my));
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        skyTv = (TextView) findViewById(R.id.MySky);
        setTv = (TextView) findViewById(R.id.SettingTv);
        setTv.setText(InternationalizationHelper.getString("JXSettingVC_Set"));
        findViewById(R.id.info_rl).setOnClickListener(this);

        findViewById(R.id.micro_wallet_monry).setOnClickListener(this);
        findViewById(R.id.secure_settings).setOnClickListener(this);
        findViewById(R.id.private_settings).setOnClickListener(this);
        findViewById(R.id.other_settings).setOnClickListener(this);
        findViewById(R.id.my_contact_customer_service_rl).setOnClickListener(this);

        // 切换新旧两种ui对应我的页面是否显示视频会议、直播、短视频，
//        if (coreManager.getConfig().newUi) {
//            findViewById(R.id.ll_more).setVisibility(View.GONE);
//        }

        if (Constants.SUPPORT_CONTACT_CUSTOMER_SERVICE && coreManager.getConfig().isEnableCusServer == 1) {
            findViewById(R.id.my_contact_customer_service_rl).setVisibility(View.VISIBLE);
            findViewById(R.id.my_contact_customer_service_v).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.my_contact_customer_service_rl).setVisibility(View.GONE);
            findViewById(R.id.my_contact_customer_service_v).setVisibility(View.GONE);
        }
        findViewById(R.id.my_monry).setOnClickListener(this);
        findViewById(R.id.sign_in_red_packet_rl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SignInRedActivity.class));
            }
        });
        // 关闭红包功能，隐藏我的钱包
        if (coreManager.getConfig().displayRedPacket) { // 切换新旧两种ui对应我的页面是否显示视频会议、直播、短视频，
            findViewById(R.id.my_monry).setVisibility(View.GONE);
        }
        if (coreManager.getConfig().isWeiBaoStatus != 1) {
            findViewById(R.id.micro_wallet_monry).setVisibility(View.GONE);
        }
        findViewById(R.id.my_space_rl).setOnClickListener(this);
        findViewById(R.id.my_collection_rl).setOnClickListener(this);
        findViewById(R.id.local_course_rl).setOnClickListener(this);
        findViewById(R.id.local_tuiguang_rl).setOnClickListener(this);
        findViewById(R.id.setting_rl).setOnClickListener(this);

        mAvatarImg = (ImageView) findViewById(R.id.avatar_img);
        mNickNameTv = (TextView) findViewById(R.id.nick_name_tv);
        mPhoneNumTv = (TextView) findViewById(R.id.phone_number_tv);
        String loginUserId = coreManager.getSelf().getUserId();
        AvatarHelper.getInstance().displayAvatar(coreManager.getSelf().getNickName(), loginUserId, mAvatarImg, false);
        mNickNameTv.setText(coreManager.getSelf().getNickName());

        mAvatarImg.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                Intent intent = new Intent(getActivity(), SingleImagePreviewActivity.class);
                intent.putExtra(AppConstant.EXTRA_IMAGE_URI, coreManager.getSelf().getUserId());
                startActivity(intent);
            }
        });

        findViewById(R.id.llFriend).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                MainActivity activity = (MainActivity) MeFragment.this.requireActivity();
                activity.changeTab(R.id.rb_tab_2);
            }
        });
        findViewById(R.id.llGroup).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                RoomActivity.start(MeFragment.this.requireContext());
            }
        });
        initTitleBackground();
        if (coreManager.getConfig().IS_OPEN_TWO_BAR_CODE == 0) {
            findViewById(R.id.imageView3).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.imageView3).setVisibility(View.VISIBLE);
        }
    }

    private void initTitleBackground() {
        SkinUtils.Skin skin = SkinUtils.getSkin(requireContext());
        int primaryColor = skin.getPrimaryColor();
        findViewById(R.id.tool_bar).setBackgroundColor(primaryColor);
    }

    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        int id = v.getId();
        switch (id) {
            case R.id.info_rl:
                // 我的资料
                startActivityForResult(new Intent(getActivity(), BasicInfoEditActivity.class), 1);
                break;

            case R.id.secure_settings:
                // 安全设置，
                startActivity(new Intent(getContext(), SecureSettingActivity.class));
                break;
            case R.id.private_settings://隐私设置
                startActivity(new Intent(getContext(), PrivacySettingActivity.class));
                break;
            case R.id.other_settings:
                // 其他设置
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
            case R.id.my_monry:
                // 我的钱包
                startActivity(new Intent(getActivity(), WxPayBlance.class));
                break;
            case R.id.micro_wallet_monry://微钱包
                getCloudWallet();
                break;
            case R.id.my_space_rl:
                // 我的动态
                Intent intent = new Intent(getActivity(), BusinessCircleActivity.class);
                intent.putExtra(AppConstant.EXTRA_CIRCLE_TYPE, AppConstant.CIRCLE_TYPE_PERSONAL_SPACE);
                startActivity(intent);
                break;
            case R.id.my_collection_rl:
                // 我的收藏
                startActivity(new Intent(getActivity(), MyCollection.class));
                break;
            case R.id.my_contact_customer_service_rl://联系客服
                MyWebViewActivity.start(getActivity(), coreManager.getConfig().cusServerUrl);
                break;
            case R.id.local_course_rl:
                // 我的课件
                startActivity(new Intent(getActivity(), LocalCourseActivity.class));
                break;
            case R.id.local_tuiguang_rl:
                // 我的推广
                //startActivity(new Intent(getActivity(), TuiGuangActivity.class));
                break;
            case R.id.setting_rl:
                // 设置
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 || resultCode == Activity.RESULT_OK) {// 个人资料更新了
            updateUI();
        }
    }

    /**
     * 用户的信息更改的时候，ui更新
     */
    private void updateUI() {
        if (mAvatarImg != null) {
            AvatarHelper.getInstance().displayAvatar(coreManager.getSelf().getUserId(), mAvatarImg, true);
        }
        if (mNickNameTv != null) {
            mNickNameTv.setText(coreManager.getSelf().getNickName());
        }

        if (mPhoneNumTv != null) {

            mPhoneNumTv.setText("账号：" + coreManager.getSelf().getAccount());
        }

        AsyncUtils.doAsync(this, t -> {
            Reporter.post("获取好友数量失败，", t);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    ToastUtil.showToast(requireContext(), R.string.tip_me_query_friend_failed);
                });
            }
        }, ctx -> {
            long count = FriendDao.getInstance().getFriendsCount(coreManager.getSelf().getUserId());
            ctx.uiThread(ref -> {
                TextView tvColleague = findViewById(R.id.tvFriend);
                tvColleague.setText(String.valueOf(count));
            });
        });

        AsyncUtils.doAsync(this, t -> {
            Reporter.post("获取群组数量失败，", t);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    ToastUtil.showToast(requireContext(), R.string.tip_me_query_friend_failed);
                });
            }
        }, ctx -> {
            long count = FriendDao.getInstance().getGroupsCount(coreManager.getSelf().getUserId());
            ctx.uiThread(ref -> {
                TextView tvGroup = findViewById(R.id.tvGroup);
                tvGroup.setText(String.valueOf(count));
            });
        });
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void processEventBusMsg(EventBusMsg eventBusMsg) {
        if (eventBusMsg.getMessageType() == Constants.EVENTBUS_MSG_CURRENT_USER_INFO_UPDATE_UI) {
            updateUI();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void getCloudWallet() {
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        HttpUtils.get().url(com.payeasenet.wepay.constant.Constants.WHETHER_TO_OPEN)
                .params(params)
                .build()
                .execute(new BaseCallback<Boolean>(Boolean.class) {
                    @Override
                    public void onResponse(ObjectResult<Boolean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            if (result.getData()) {
                                startActivity(new Intent(getActivity(), com.payeasenet.wepay.ui.activity.MainActivity.class));
                            } else {
                                showExitDialog();
                            }
                        } else {
                            ToastUtil.showToast(getContext(), "获取数据失败");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(getContext());
                    }
                });
    }

    private void showExitDialog() {
        SelectionFrame mSF = new SelectionFrame(getContext());
        mSF.setSomething(null, "您还未开通微钱包，是否开通", "取消", "开通", new SelectionFrame.OnSelectionFrameClickListener() {
            @Override
            public void cancelClick() {

            }

            @Override
            public void confirmClick() {
                startActivity(new Intent(getActivity(), PayWebViewActivity.class));
            }
        });
        mSF.show();
    }
}
