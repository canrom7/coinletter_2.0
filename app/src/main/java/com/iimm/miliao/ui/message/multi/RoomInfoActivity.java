package com.iimm.miliao.ui.message.multi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.adapter.RecyclerViewMemberAdapter;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.Report;
import com.iimm.miliao.bean.RoomMember;
import com.iimm.miliao.bean.message.MucRoom;
import com.iimm.miliao.bean.message.MucRoomMember;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.broadcast.MucgroupUpdateUtil;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.RoomMemberDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.MainActivity;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.dialog.AskCommonBottomDialog;
import com.iimm.miliao.ui.message.search.SearchChatHistoryActivity;
import com.iimm.miliao.ui.message.single.SetChatBackActivity;
import com.iimm.miliao.ui.mucfile.MucFileListActivity;
import com.iimm.miliao.ui.other.BasicInfoActivity;
import com.iimm.miliao.util.AppExecutors;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.CameraUtil;
import com.iimm.miliao.util.CharUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.ExpandView;
import com.iimm.miliao.util.ImageCache;
import com.iimm.miliao.util.LogUtils;
import com.iimm.miliao.util.OBSUtils;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.permission.AndPermissionUtils;
import com.iimm.miliao.util.permission.OnPermissionClickListener;
import com.iimm.miliao.view.GetPictureCommonDialog;
import com.iimm.miliao.view.MsgSaveDaysDialog;
import com.iimm.miliao.view.NoDoubleClickListener;
import com.iimm.miliao.view.ReportDialog;
import com.iimm.miliao.view.TipDialog;
import com.iimm.miliao.view.VerifyDialog;
import com.iimm.miliao.volley.Result;
import com.iimm.miliao.xmpp.ListenerManager;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.suke.widget.SwitchButton;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import cz.msebera.android.httpclient.Header;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

import static com.iimm.miliao.broadcast.MsgBroadcast.ACTION_MSG_UPDATE_ROOM_INVITE;
import static com.iimm.miliao.broadcast.MsgBroadcast.EXTRA_ENABLED;
import static com.iimm.miliao.util.OBSUtils.transformObjectUrl;

/**
 * 群组信息
 */
public class RoomInfoActivity extends BaseActivity implements RecyclerViewMemberAdapter.OnItemClickListener, OnPermissionClickListener {
    private String TAG = "RoomInfoActivity";
    private static final int RESULT_FOR_ADD_MEMBER = 1;
    private static final int REQUEST_CODE_PICK_CROP_PHOTO = 2;
    private static final int REQUEST_CODE_CROP_PHOTO = 3;
    private static final int REQUEST_CODE_CAPTURE_CROP_PHOTO = 4;
    private static final int RESULT_FOR_MODIFY_NOTICE = 5;
    private static final int RESULT_FOR_DELETE_MEMBER = 6;

    private String mRoomJid;
    // 是否从聊天界面进入
    private boolean isMucChatComing;
    private String mLoginUserId;
    private Friend mRoom;
    private Map<String, String> mRemarksMap = new HashMap<>();  //存的都是备注
    private MucRoom mucRoom;
    private RecyclerView mRecyclerGridView;
    private RecyclerViewMemberAdapter mRecyclerViewMemberAdapter;
    private TextView showAllMemberText;
    private TextView mRoomNameTv;
    private TextView mRoomDescTv;
    private TextView mNoticeTv;
    private TextView mNickNameTv;
    private ConstraintLayout rlManager;
    private LinearLayout mLlBanned;
    private RelativeLayout mRlBannedVoice;
    private SwitchButton mSbBanned;
    private RelativeLayout mRlPicture;
    private RelativeLayout mRlFile;
    private SwitchButton mSbTopChat;
    private SwitchButton mSbDisturb;
    private SwitchButton mSbShield;
    private SwitchButton mSbStrongRemind;
    private RelativeLayout mRlChatHistorySearch;
    private RelativeLayout mRlChatHistoryEmpty;
    private Button mBtnQuitRoom;
    private RelativeLayout mRlMsgSaveDays;
    private TextView mTvMsgSaveDays;
    private TextView tvMemberLimit;
    private ConstraintLayout signll;
    RefreshBroadcastReceiver receiver = new RefreshBroadcastReceiver();
    private int mMemberSize;
    private List<MucRoomMember> mMembers;  //当前群成员
    private String creator;  // 群主id
    private int isNeedVerify;// 是否开启进群验证
    private MucRoomMember mGroupOwner;// 群主
    private MucRoomMember myself; //自己
    private ExpandView mExpandView;
    private ImageView mExpandIv;
    private TextView mCreatorTv;
    private TextView buileTimetv;
    private TextView mCreateTime;
    private TextView numberTopTv;
    private TextView mCountTv;
    private int role;  //我是哪个角色
    private Uri mPictureContentOrFileUri;//拍照 或 选择 图片 时，图片的路径，如果是SDK >= 24 则为ContentUri,否则是FileUri
    private Uri mPictureCropFileUri;//图片裁剪后的路径
    private int add_minus_count = 2;
    private List<MucRoomMember> mCurrentMembers = new ArrayList<>();
    private ConstraintLayout mRqCode;
    MsgSaveDaysDialog.OnMsgSaveDaysDialogClickListener onMsgSaveDaysDialogClickListener = new MsgSaveDaysDialog.OnMsgSaveDaysDialogClickListener() {
        @Override
        public void tv1Click() {
            updateChatRecordTimeOut(-1);
        }

        @Override
        public void tv2Click() {
            updateChatRecordTimeOut(0.04);
        }

        @Override
        public void tv3Click() {
            updateChatRecordTimeOut(1);
        }

        @Override
        public void tv4Click() {
            updateChatRecordTimeOut(7);
        }

        @Override
        public void tv5Click() {
            updateChatRecordTimeOut(30);
        }

        @Override
        public void tv6Click() {
            updateChatRecordTimeOut(90);
        }

        @Override
        public void tv7Click() {
            updateChatRecordTimeOut(365);
        }
    };

    SwitchButton.OnCheckedChangeListener onCheckedChangeMessageListener = new SwitchButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(SwitchButton view, boolean isChecked) {
            switch (view.getId()) {
                case R.id.sb_top_chat:// 置顶聊天
                    if (isChecked) {
                        FriendDao.getInstance().updateTopFriend(mRoomJid, mRoom.getTimeSend());
                    } else {
                        FriendDao.getInstance().resetTopFriend(mRoomJid);
                    }
                    if (!isMucChatComing) {// 非聊天界面进入，需要刷新消息页面
                        MsgBroadcast.broadcastMsgUiUpdate(RoomInfoActivity.this);
                    }
                    break;
                case R.id.sb_no_disturb:// 消息免打扰
                    updateDisturbState(isChecked ? 1 : 0);
                    break;
                case R.id.sb_shield_chat:// 屏蔽群消息
                    if (isChecked) {
                        if (mRoom.getOfflineNoPushMsg() == 0) {
                            mSbDisturb.setChecked(true);
                        }
                    }
                    PreferenceUtils.putBoolean(mContext, Constants.SHIELD_GROUP_MSG + mRoomJid + mLoginUserId, isChecked);
                    mSbShield.setChecked(isChecked);
                    break;
                case R.id.sb_banned:// 全体禁言
                    if (isChecked) {
                        updateSingleAttribute("talkTime", String.valueOf(TimeUtils.time_current_time() + 24 * 60 * 60 * 15));
                    } else {
                        updateSingleAttribute("talkTime", String.valueOf(0));
                    }
                    break;
                case R.id.sb_strong_remind:
                    updateStrongRemindStatus(isChecked);
                    break;
            }
        }
    };

    /**
     * 开关强提醒状态
     *
     * @param isChecked
     */
    private void updateStrongRemindStatus(boolean isChecked) {
        FriendDao.getInstance().updateStrongRemindStatus(mRoomJid, isChecked);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_info);
        if (getIntent() != null) {
            mRoomJid = getIntent().getStringExtra(AppConstant.EXTRA_USER_ID);
            isMucChatComing = getIntent().getBooleanExtra(AppConstant.EXTRA_IS_GROUP_CHAT, false);
        }
        if (TextUtils.isEmpty(mRoomJid)) {
            LogUtils.log(getIntent());
            Reporter.post("传入的RoomJid为空，");
            Toast.makeText(this, R.string.tip_group_message_failed, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mLoginUserId = coreManager.getSelf().getUserId();
        mRoom = FriendDao.getInstance().getFriend(mLoginUserId, mRoomJid);

        if (mRoom == null || TextUtils.isEmpty(mRoom.getRoomId())) {
            // 没有toString方法，暂且转json，不能被混淆，
            Reporter.post("传入的RoomJid找不到Room，");
            Toast.makeText(this, R.string.tip_group_message_failed, Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "run: " + mRoom.getRoomId());
                    ImageCache.removeBitmap(mRoom.getRoomId());
                }
            });
        }
        //是否显示群聊背景图片设置
        if (!Constants.SUPPORT_GROUP_BG){
            findViewById(R.id.chat_background_image).setVisibility(View.GONE);
        }
        initActionBar();
        initView();
        registerRefreshReceiver();
        loadMembers();
        event();
        EventBus.getDefault().register(this);

    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView mTitleTv = (TextView) findViewById(R.id.tv_title_center);
        mTitleTv.setText(InternationalizationHelper.getString("JXRoomMemberVC_RoomInfo"));
    }

    private void initView() {
        List<Friend> mFriendList = FriendDao.getInstance().getAllFriends(mLoginUserId);
        for (int i = 0; i < mFriendList.size(); i++) {
            if (!TextUtils.isEmpty(mFriendList.get(i).getRemarkName())) {// 针对该好友进行了备注
                mRemarksMap.put(mFriendList.get(i).getUserId(), mFriendList.get(i).getRemarkName());
            }
        }
        showAllMemberText = findViewById(R.id.tv_show_all_member);//显示的 部分群成员
        showAllMemberText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMoreGroupUser(mucRoom);
            }
        });
        mRecyclerGridView = (RecyclerView) findViewById(R.id.recycler_grid_view);
        //布局管理器对象 参数1.上下文 2.规定一行显示几列的参数常量
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5);
        //设置RecycleView显示的方向是水平还是垂直 GridLayout.HORIZONTAL水平  GridLayout.VERTICAL默认垂直
        gridLayoutManager.setOrientation(GridLayout.VERTICAL);
        //设置布局管理器， 参数gridLayoutManager对象
        mRecyclerGridView.setLayoutManager(gridLayoutManager);
        mRecyclerViewMemberAdapter = new RecyclerViewMemberAdapter(this);
        mRecyclerGridView.setAdapter(mRecyclerViewMemberAdapter);
        mRecyclerViewMemberAdapter.setOnClickListener(this);

        mRoomNameTv = (TextView) findViewById(R.id.room_name_tv);//房间名称
        mRoomNameTv.setText(mRoom.getNickName());
        mRoomDescTv = (TextView) findViewById(R.id.room_desc_tv);//房间描述
        mRoomDescTv.setText(mRoom.getDescription());

        mExpandIv = (ImageView) findViewById(R.id.room_info_iv);
        mExpandView = (ExpandView) findViewById(R.id.expandView);
        mExpandView.setContentView(R.layout.layout_expand);
        mExpandView.setVisibility(View.GONE);
        mCreatorTv = (TextView) findViewById(R.id.creator_tv); //群创建者
        buileTimetv = (TextView) findViewById(R.id.create_time_text); //创建时间
        buileTimetv.setText(InternationalizationHelper.getString("JXRoomMemberVC_CreatTime"));
        mCreateTime = (TextView) findViewById(R.id.create_timer);//创建时间
        numberTopTv = (TextView) findViewById(R.id.count_text); //成员上限
        numberTopTv.setText(InternationalizationHelper.getString("MEMBER_CAP"));
        mCountTv = (TextView) findViewById(R.id.count_tv);//成员上限
        tvMemberLimit = findViewById(R.id.member_limit_tv);//群人数上限
        mNoticeTv = (TextView) findViewById(R.id.notice_tv); //群公告显示
        mRqCode = findViewById(R.id.room_qrcode);  //二维码
        mRqCode.setVisibility(View.VISIBLE);
        mNickNameTv = (TextView) findViewById(R.id.nick_name_tv); //群内昵称
        mNickNameTv.setText(mRoom.getRoomMyNickName() != null
                ? mRoom.getRoomMyNickName() : coreManager.getSelf().getNickName());

        rlManager = findViewById(R.id.rl_manager); //群管理
        //禁言 整个模块
        mLlBanned = findViewById(R.id.ll_banned);
        mRlBannedVoice = findViewById(R.id.banned_voice_rl); //禁言  跳转 单个禁言界面
        mSbBanned = findViewById(R.id.sb_banned);   // 全局禁言切换
        boolean isAllShutUp = PreferenceUtils.getBoolean(mContext, Constants.GROUP_ALL_SHUP_UP + mRoom.getUserId(), false);
        mSbBanned.setChecked(isAllShutUp);

        signll = findViewById(R.id.sign_in_ll);

        //设置群头像
        mRlPicture = findViewById(R.id.picture_rl);
        //群文件
        mRlFile = findViewById(R.id.file_rl);
        mSbTopChat = (SwitchButton) findViewById(R.id.sb_top_chat); //置顶聊天
        mSbDisturb = (SwitchButton) findViewById(R.id.sb_no_disturb); //  消息免打扰
        mSbDisturb.setChecked(mRoom.getOfflineNoPushMsg() == 1);// 消息免打扰
        mSbShield = (SwitchButton) findViewById(R.id.sb_shield_chat); // 屏蔽群消息
        mSbStrongRemind = (SwitchButton) findViewById(R.id.sb_strong_remind);//关闭强提醒
        mRlMsgSaveDays = findViewById(R.id.msg_save_days_rl);  //消息过期自动销毁
        mTvMsgSaveDays = findViewById(R.id.msg_save_days_tv);//消息过期自动销毁 消息保存天数
        mTvMsgSaveDays.setText(conversion(mRoom.getChatRecordTimeOut()));
        mBtnQuitRoom = (Button) findViewById(R.id.room_info_quit_btn); //退出群组 /解散群组
        mBtnQuitRoom.setText(InternationalizationHelper.getString("JXRoomMemberVC_OutPutRoom"));
        mSbStrongRemind.setChecked(FriendDao.getInstance().getStrongRemindStatus(mRoomJid));
        mSbStrongRemind.setOnCheckedChangeListener(onCheckedChangeMessageListener);

        signll.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                Intent intent = new Intent(RoomInfoActivity.this, GroupSignActivity.class);
                intent.putExtra("roomId", mRoomJid);
                RoomInfoActivity.this.startActivity(intent);
            }
        });
    }

    private void registerRefreshReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(com.iimm.miliao.broadcast.OtherBroadcast.REFRESH_MANAGER);
        intentFilter.addAction(ACTION_MSG_UPDATE_ROOM_INVITE);
        registerReceiver(receiver, intentFilter);
    }

    private void loadMembers() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoom.getRoomId());
        params.put("pageSize", Constants.MUC_MEMBER_SHOW_SIZE);
        HttpUtils.get().url(coreManager.getConfig().ROOM_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<MucRoom>(MucRoom.class) {
                             @Override
                             public void onResponse(ObjectResult<MucRoom> result) {
                                 if (result.getResultCode() == 1 && result.getData() != null) {
                                     mucRoom = result.getData();
                                     tvMemberLimit.setText(String.valueOf(mucRoom.getMaxUserSize()));

                                     MyApplication.getInstance().saveGroupPartStatus(mucRoom.getJid(), mucRoom.getShowRead(), mucRoom.getAllowSendCard(),
                                             mucRoom.getAllowConference(), mucRoom.getAllowSpeakCourse(), mucRoom.getTalkTime());
                                     FriendDao.getInstance().updateRoomCreateUserId(mLoginUserId, mRoom.getUserId(), mucRoom.getUserId());
                                     PreferenceUtils.putBoolean(MyApplication.getContext(),
                                             Constants.IS_NEED_OWNER_ALLOW_NORMAL_INVITE_FRIEND + mucRoom.getJid(), mucRoom.getIsNeedVerify() == 1);
                                     PreferenceUtils.putBoolean(MyApplication.getContext(),
                                             Constants.IS_ALLOW_NORMAL_SEND_UPLOAD + mucRoom.getJid(), mucRoom.getAllowUploadFile() == 1);
                                     if (mucRoom.getIsShowSignIn() == 1) {
                                         PreferenceUtils.putBoolean(MyApplication.getContext(),
                                                 Constants.IS_GROUP_SIGN + mucRoom.getJid(), true);
                                         signll.setVisibility(View.VISIBLE);
                                     } else {
                                         PreferenceUtils.putBoolean(MyApplication.getContext(),
                                                 Constants.IS_GROUP_SIGN + mucRoom.getJid(), false);
                                         signll.setVisibility(View.GONE);
                                     }


                                     AsyncUtils.doAsync(this, (AsyncUtils.Function<AsyncUtils.AsyncContext<BaseCallback<MucRoom>>>) baseCallbackAsyncContext -> {
                                         for (int i = 0; i < mucRoom.getMembers().size(); i++) {// 在异步任务内存储
                                             MucRoomMember mucRoomMember = mucRoom.getMembers().get(i);
                                             if (mucRoomMember == null) {
                                                 continue;
                                             }
                                             RoomMember roomMember = new RoomMember();
                                             if (mucRoomMember.getOnLineState() == 0) {
                                                 roomMember.setLastOnLineTime(mucRoomMember.getOfflineTime());
                                             } else {
                                                 //在线
                                                 roomMember.setLastOnLineTime(0);
                                             }
                                             roomMember.setVipLevel(mucRoomMember.getVip());
                                             roomMember.setRoomId(mucRoom.getId());
                                             roomMember.setUserId(mucRoomMember.getUserId());
                                             roomMember.setUserName(mucRoomMember.getNickName());
                                             if (TextUtils.isEmpty(mucRoomMember.getRemarkName())) {
                                                 roomMember.setCardName(mucRoomMember.getNickName());
                                             } else {
                                                 roomMember.setCardName(mucRoomMember.getRemarkName());
                                             }
                                             roomMember.setTalkTime(mucRoomMember.getTalkTime());
                                             roomMember.setRole(mucRoomMember.getRole());
                                             roomMember.setCreateTime(mucRoomMember.getCreateTime());
                                             RoomMemberDao.getInstance().saveSingleRoomMember(mucRoom.getId(), roomMember);
                                         }
                                     });

                                     saveMucLastRoamingTime(mLoginUserId, mucRoom.getId(), mucRoom.getMembers().get(mucRoom.getMembers().size() - 1).getCreateTime(), false);

                                     // 更新消息界面
                                     MsgBroadcast.broadcastMsgUiUpdate(RoomInfoActivity.this);
                                     // 更新群聊界面
                                     MucgroupUpdateUtil.broadcastUpdateUi(RoomInfoActivity.this);
                                     // 更新ui
                                     updateUI(result.getData());
                                 } else {
                                     ToastUtil.showErrorData(RoomInfoActivity.this);
                                 }
                                 DialogHelper.dismissProgressDialog();

                             }

                             @Override
                             public void onError(Call call, Exception e) {
                                 DialogHelper.dismissProgressDialog();
                                 ToastUtil.showErrorNet(RoomInfoActivity.this);
                             }
                         }
                );
    }

    private void updateUI(final MucRoom mucRoom) {
        mMemberSize = mucRoom.getUserSize();
        mMembers = mucRoom.getMembers();
        creator = mucRoom.getUserId();
        isNeedVerify = mucRoom.getIsNeedVerify();
        if (mMembers != null) {
            for (int i = 0; i < mMembers.size(); i++) {
                String userId = mMembers.get(i).getUserId();
                if (mucRoom.getUserId().equals(userId)) {
                    mGroupOwner = mMembers.get(i);
                }
            }
            // 将群主移动到第一个的位置
            if (mGroupOwner != null) {
                mMembers.remove(mGroupOwner);
                mMembers.add(0, mGroupOwner);
            }
        }
        myself = mucRoom.getMember();

        if (myself == null) {
            ToastUtil.showToast(mContext, R.string.tip_kick_room);
            finish();
            return;
        }
        mRoomNameTv.setText(mucRoom.getName());
        mRoomDescTv.setText(mucRoom.getDesc());

        mCreatorTv.setText(mucRoom.getNickName());
        mCreateTime.setText(TimeUtils.s_long_2_str(mucRoom.getCreateTime() * 1000));
        mCountTv.setText(mucRoom.getUserSize() + "/" + mucRoom.getMaxUserSize());
        showAllMemberText.setText(getResources().getString(R.string.view_all_group_members) + "(" + mucRoom.getUserSize() + ")");
        List<MucRoom.Notice> notices = mucRoom.getNotices();
        if (notices != null && !notices.isEmpty()) {
            String text = getLastNoticeText(notices);
            mNoticeTv.setText(text);
        } else {
            mNoticeTv.setText(InternationalizationHelper.getString("JX_NotAch"));
        }
        String mGroupName = coreManager.getSelf().getNickName();
        if (mRoom != null) {
            mGroupName = mRoom.getRoomMyNickName() != null ?
                    mRoom.getRoomMyNickName() : mGroupName;
        }
        mNickNameTv.setText(mGroupName);

        // 更新消息免打扰状态
        mRoom.setOfflineNoPushMsg(myself.getOfflineNoPushMsg());
        FriendDao.getInstance().updateOfflineNoPushMsgStatus(mRoom.getUserId(), myself.getOfflineNoPushMsg());
        // 更新消息管理状态
        updateMessageStatus(myself.getOfflineNoPushMsg());
        // 更新消息保存天数
        mTvMsgSaveDays.setText(conversion(mucRoom.getChatRecordTimeOut()));
        FriendDao.getInstance().updateChatRecordTimeOut(mRoom.getUserId(), mucRoom.getChatRecordTimeOut());
        // 根据我在该群职位显示UI界面
        role = myself.getRole();
        if (role == 1) {// 群创建者，开放所有权限
            mBtnQuitRoom.setText(InternationalizationHelper.getString("DISSOLUTION_GROUP"));
            findViewById(R.id.room_name_rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChangeRoomNameDialog(mRoomNameTv.getText().toString().trim());
                }
            });
            // 修改群头像
            findViewById(R.id.picture_rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChangePictureDialog();
                }
            });
            findViewById(R.id.room_desc_rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChangeRoomDesDialog(mRoomDescTv.getText().toString().trim());
                }
            });
            //findViewById(R.id.msg_save_days_rl).setVisibility(View.VISIBLE);
            findViewById(R.id.msg_save_days_rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MsgSaveDaysDialog msgSaveDaysDialog = new MsgSaveDaysDialog(RoomInfoActivity.this, onMsgSaveDaysDialogClickListener);
                    msgSaveDaysDialog.show();
                }
            });
            findViewById(R.id.banned_voice_rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, GroupMoreFeaturesActivity.class);
                    intent.putExtra("roomId", mucRoom.getId());
                    intent.putExtra("isBanned", true);
                    startActivity(intent);
                }
            });
            findViewById(R.id.rl_manager).setVisibility(View.VISIBLE);
            findViewById(R.id.rl_manager).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int status_lists[] = {mucRoom.getShowRead(), mucRoom.getIsLook(), mucRoom.getIsNeedVerify(),
                            mucRoom.getShowMember(), mucRoom.getAllowSendCard(),
                            mucRoom.getAllowInviteFriend(), mucRoom.getAllowUploadFile(),
                            mucRoom.getAllowConference(), mucRoom.getAllowSpeakCourse(),
                            mucRoom.getIsAttritionNotice(), mucRoom.getAllowForceNotice(), mucRoom.getIsShowSignIn()};
                    Intent intent = new Intent(mContext, GroupManager.class);
                    intent.putExtra("roomId", mucRoom.getId());
                    intent.putExtra("roomJid", mucRoom.getJid());
                    intent.putExtra("GROUP_STATUS_LIST", status_lists);
                    startActivity(intent);
                }
            });
            mSbBanned.setOnCheckedChangeListener(onCheckedChangeMessageListener);
            updateMemberLimit(true);
        } else if (role == 2) {// 管理员，开放部分权限
            mBtnQuitRoom.setText(InternationalizationHelper.getString("JXRoomMemberVC_OutPutRoom"));
            findViewById(R.id.room_name_rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChangeRoomNameDialog(mRoomNameTv.getText().toString().trim());
                }
            });
            // 修改群头像
            findViewById(R.id.picture_rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChangePictureDialog();
                }
            });
            findViewById(R.id.room_desc_rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChangeRoomDesDialog(mRoomDescTv.getText().toString().trim());
                }
            });

            findViewById(R.id.banned_voice_rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, GroupMoreFeaturesActivity.class);
                    intent.putExtra("roomId", mucRoom.getId());
                    intent.putExtra("isBanned", true);
                    startActivity(intent);
                }
            });
            mSbBanned.setOnCheckedChangeListener(onCheckedChangeMessageListener);
            updateMemberLimit(true);
        } else {
            add_minus_count = 1;
            mBtnQuitRoom.setText(InternationalizationHelper.getString("JXRoomMemberVC_OutPutRoom"));
            findViewById(R.id.room_name_rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tip(getString(R.string.tip_cannot_change_name));
                }
            });
            // 修改群头像
            findViewById(R.id.picture_rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tip(getString(R.string.tip_cannot_change_avatar));
                }
            });
            findViewById(R.id.room_desc_rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tip(getString(R.string.tip_cannot_change_description));
                }
            });
            // 隐藏 禁言 与 全体禁言
            findViewById(R.id.banned_voice_rl).setVisibility(View.GONE);
            findViewById(R.id.banned_all_voice_rl).setVisibility(View.GONE);

            findViewById(R.id.msg_save_days_rl).setVisibility(View.GONE);
            findViewById(R.id.rl_manager).setVisibility(View.GONE);
            updateMemberLimit(false);
        }
        mCurrentMembers.clear();
        if (mucRoom.getShowMember() == 0 && role != 1 && role != 2) {// 群主已关闭 显示群成员列表功能 (群主与管理员可见) 普通成员只显示自己与+
            List<MucRoomMember> displayRoomMember = new ArrayList<>();
            for (int i = 0; i < mMembers.size(); i++) {
                MucRoomMember mucRoomMember = mMembers.get(i);
                if (mucRoomMember.getRole() == 1 || mucRoomMember.getRole() == 2 ||
                        mucRoomMember.getUserId().equals(coreManager.getSelf().getUserId())) {
                    displayRoomMember.add(mucRoomMember);//把群管理员和群主加入显示列表
                }
            }
            for (int i = 0; i < displayRoomMember.size(); i++) {
                if (i == 8) {
                    break;
                }
                mCurrentMembers.add(displayRoomMember.get(i));
            }
        } else {// 正常加载
            for (int i = 0; i < mMembers.size(); i++) {
                if (i == 8) {
                    break;
                }
                mCurrentMembers.add(mMembers.get(i));
            }
        }
        if (mRecyclerViewMemberAdapter != null) {
            mRecyclerViewMemberAdapter.setRemarkMap(mRemarksMap);
            mRecyclerViewMemberAdapter.setMemberRole(myself.getRole());
            mRecyclerViewMemberAdapter.setData(mCurrentMembers);
        }
        //禁止邀请时，如果不是管理员，不是群主，不让点击二维码
        if (mucRoom.getAllowInviteFriend() != 1 && mucRoom.getMember() != null
                && mucRoom.getMember().getRole() != Constants.ROLE_OWNER
                && mucRoom.getMember().getRole() != Constants.ROLE_MANAGER) {
            mRqCode.setOnClickListener(null);
        } else {
            mRqCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), mRoom.getUserId());
                    DialogHelper.showQRDialog2(RoomInfoActivity.this, mRoom.getNickName(), String.valueOf(mucRoom.getUserSize()), coreManager.getConfig().website, mRoom.getRoomId(), friend, coreManager);
                }
            });
        }
    }

    // 修改群组描述
    private void showChangeRoomDesDialog(final String roomDes) {
        DialogHelper.ChangeRoomNameDialog(this, InternationalizationHelper.getString("JXRoomMemberVC_UpdateExplain"), roomDes,
                7, 2, 100, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String text = ((EditText) v).getText().toString().trim();
                        if (TextUtils.isEmpty(text) || text.equals(roomDes)) {
                            return;
                        }
                        int length = 0;
                        for (int i = 0; i < text.length(); i++) {
                            String substring = text.substring(i, i + 1);
                            boolean flag = CharUtils.isChinese(substring);
                            if (flag) {
                                length += 2;
                            } else {
                                length += 1;
                            }
                        }
                        if (length > 100) {
                            ToastUtil.showToast(mContext, getString(R.string.tip_description_too_long));
                            return;
                        }
                        updateRoom(null, text);
                    }
                });
    }


    // 修改群头像
    private void showChangePictureDialog() {
        AndPermissionUtils.scanIt(RoomInfoActivity.this, this);
    }

    private void takePhoto() {
        String fileDirStr = MyApplication.getInstance().mPicturesDir;
        String filePath = MyApplication.getInstance().mPicturesDir + File.separator + UUID.randomUUID().toString() + ".jpg";

        File fileDir = new File(fileDirStr);
        if (!fileDir.mkdirs()) {
        }
        File file = new File(filePath);
        mPictureContentOrFileUri = CameraUtil.getOutputMediaContentUri(this, file);
        CameraUtil.captureImage(this, mPictureContentOrFileUri, REQUEST_CODE_CAPTURE_CROP_PHOTO);
    }

    private void selectPhoto() {
        CameraUtil.pickImageSimple(this, REQUEST_CODE_PICK_CROP_PHOTO);
    }

    // 修改群组名称
    private void showChangeRoomNameDialog(final String roomName) {
        DialogHelper.ChangeRoomNameDialog(this, InternationalizationHelper.getString("JXRoomMemberVC_UpdateRoomName"), roomName,
                2, 2, 20, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String text = ((EditText) v).getText().toString().trim();
                        if (TextUtils.isEmpty(text) || text.equals(roomName)) {
                            return;
                        }
                        int length = 0;
                        for (int i = 0; i < text.length(); i++) {
                            String substring = text.substring(i, i + 1);
                            boolean flag = CharUtils.isChinese(substring);
                            if (flag) {
                                // 中文占两个字符
                                length += 2;
                            } else {
                                length += 1;
                            }
                        }
                        if (length > 20) {
                            ToastUtil.showToast(mContext, getString(R.string.tip_name_too_long));
                            return;
                        }
                        updateRoom(text, null);
                    }
                });
    }


    /**
     * Todo Http Get
     * <p>
     * 修改群名称、描述
     */
    private void updateRoom(final String roomName, final String roomDes) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoom.getRoomId());
        if (!TextUtils.isEmpty(roomName)) {
            params.put("roomName", roomName);
        }

        if (!TextUtils.isEmpty(roomDes)) {
            params.put("desc", roomDes);
        }
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            Toast.makeText(RoomInfoActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                            if (!TextUtils.isEmpty(roomName)) {
                                mRoomNameTv.setText(roomName);
                                mRoom.setNickName(roomName);
                                FriendDao.getInstance().updateNickName(mLoginUserId, mRoom.getUserId(), roomName);
                            }

                            if (!TextUtils.isEmpty(roomDes)) {
                                mRoomDescTv.setText(roomDes);
                                mRoom.setDescription(roomDes);
                            }
                        } else {
                            Toast.makeText(RoomInfoActivity.this, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    // 更新消息状态 置顶 、免打扰、屏蔽
    private void updateMessageStatus(int disturb) {
        mSbTopChat.setChecked(mRoom.getTopTime() != 0);
        mSbDisturb.setChecked(disturb == 1);
        boolean mShieldStatus = PreferenceUtils.getBoolean(mContext, Constants.SHIELD_GROUP_MSG + mRoomJid + mLoginUserId, false);
        mSbShield.setChecked(mShieldStatus);
    }

    private String getLastNoticeText(List<MucRoom.Notice> notices) {
        MucRoom.Notice notice = new MucRoom.Notice();
        notice.setTime(0);
        for (MucRoom.Notice no : notices) {
            if (no.getTime() > notice.getTime())
                notice = no;
        }
        return notice.getText();
    }


    /**
     * 群成员分页
     * 保存当前群组该页的最后一个成员入群时间 MucRoomMember
     */
    public static void saveMucLastRoamingTime(String ownerId, String roomId, long time, boolean reset) {
        if (reset) {
            PreferenceUtils.putLong(MyApplication.getContext(), Constants.MUC_MEMBER_LAST_JOIN_TIME + ownerId + roomId, time);
        } else {
            long lastRoamingTime = PreferenceUtils.getLong(MyApplication.getContext(), Constants.MUC_MEMBER_LAST_JOIN_TIME + ownerId + roomId, 0);
            if (lastRoamingTime < time) {
                PreferenceUtils.putLong(MyApplication.getContext(), Constants.MUC_MEMBER_LAST_JOIN_TIME + ownerId + roomId, time);
            }
        }
    }

    /**
     * 监听
     */
    private void event() {
        //房间信息
        findViewById(R.id.room_info).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                if (mExpandView.isExpand()) {
                    mExpandView.collapse();
                    mExpandIv.setBackgroundResource(R.drawable.open_member);
                } else {
                    mExpandView.expand();
                    mExpandIv.setBackgroundResource(R.drawable.close_member);
                }
            }
        });

        // 二维码
        mRqCode.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                if (mucRoom == null) {
                    return;
                }
                Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), mRoom.getUserId());
                DialogHelper.showQRDialog2(RoomInfoActivity.this, mRoom.getNickName(), String.valueOf(mucRoom.getUserSize()), coreManager.getConfig().website, mRoom.getRoomId(), friend, coreManager);
            }
        });
        // 修改群内昵称
        findViewById(R.id.nick_name_rl).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                if (mucRoom.getMember().getRole() == 1) {
                    showChangeNickNameDialog(mNickNameTv.getText().toString().trim());
                } else {
                    if (mRoom.getRoomMyTalkTime() > (TimeUtils.time_current_time())) {
                        ToastUtil.showToast(RoomInfoActivity.this, "你已被禁言，无法修改昵称");
                    } else {
                        showChangeNickNameDialog(mNickNameTv.getText().toString().trim());
                    }
                }
            }
        });
        // 修改群头像
        findViewById(R.id.picture_rl).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                RoomInfoActivity.this.showChangePictureDialog();
            }
        });
        // 群共享文件
        findViewById(R.id.file_rl).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                if (myself != null && mucRoom != null) {
                    Intent intent = new Intent(RoomInfoActivity.this, MucFileListActivity.class);
                    intent.putExtra("roomId", mRoom.getRoomId());
                    intent.putExtra("role", myself.getRole());
                    intent.putExtra("allowUploadFile", mucRoom.getAllowUploadFile());
                    RoomInfoActivity.this.startActivity(intent);
                }
            }
        });

        mSbTopChat.setOnCheckedChangeListener(onCheckedChangeMessageListener);
        mSbDisturb.setOnCheckedChangeListener(onCheckedChangeMessageListener);
        mSbShield.setOnCheckedChangeListener(onCheckedChangeMessageListener);

        //群公告 跳转
        findViewById(R.id.notice_rl).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                if (myself != null && mucRoom != null) {
                    List<String> mNoticeIdList = new ArrayList<>();
                    List<String> mNoticeUserIdList = new ArrayList<>();
                    List<String> mNoticeNickNameIdList = new ArrayList<>();
                    List<Long> mNoticeTimeList = new ArrayList<>();
                    List<String> mNoticeTextList = new ArrayList<>();
                    for (MucRoom.Notice notice : mucRoom.getNotices()) {
                        mNoticeIdList.add(notice.getId());
                        mNoticeUserIdList.add(notice.getUserId());
                        mNoticeNickNameIdList.add(notice.getNickname());
                        mNoticeTimeList.add(notice.getTime());
                        mNoticeTextList.add(notice.getText());
                    }
                    Intent intent = new Intent(RoomInfoActivity.this, NoticeListActivity.class);
                    intent.putExtra("mNoticeIdList", JSON.toJSONString(mNoticeIdList));
                    intent.putExtra("mNoticeUserIdList", JSON.toJSONString(mNoticeUserIdList));
                    intent.putExtra("mNoticeNickNameIdList", JSON.toJSONString(mNoticeNickNameIdList));
                    intent.putExtra("mNoticeTimeList", JSON.toJSONString(mNoticeTimeList));
                    intent.putExtra("mNoticeTextList", JSON.toJSONString(mNoticeTextList));
                    intent.putExtra("mRole", myself.getRole());
                    intent.putExtra("mRoomId", mRoom.getRoomId());

                    RoomInfoActivity.this.startActivityForResult(intent, RESULT_FOR_MODIFY_NOTICE);
                }
            }
        });
        //设置聊天背景
        findViewById(R.id.chat_background_image).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                Intent intentBackground = new Intent(RoomInfoActivity.this, SetChatBackActivity.class);
                intentBackground.putExtra(AppConstant.EXTRA_USER_ID, mRoomJid);
                startActivity(intentBackground);
            }
        });
        //查看聊天记录
        findViewById(R.id.chat_history_search).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                Intent intent = new Intent(RoomInfoActivity.this, SearchChatHistoryActivity.class);
                intent.putExtra("isSearchSingle", false);
                intent.putExtra(AppConstant.EXTRA_USER_ID, mRoomJid);
                RoomInfoActivity.this.startActivity(intent);
            }
        });
        //清空聊天文件
        findViewById(R.id.chat_history_empty).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                new AskCommonBottomDialog(RoomInfoActivity.this, RoomInfoActivity.this.getResources().getString(R.string.clear_cache_dialog_hint), new AskCommonBottomDialog.AskCommonBottomDialogListener() {
                    @Override
                    public void onClickOk(TextView tvOk) {
                        // 清空聊天记录
                        clearChat();
                    }

                    @Override
                    public void onClickCancel(TextView tvCancel) {

                    }

                    @Override
                    public void onClickContent(TextView tvContent, String trim) {

                    }
                }).show();
            }
        });
        //举报
        findViewById(R.id.report_rl).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                ReportDialog mReportDialog = new ReportDialog(RoomInfoActivity.this, true, new ReportDialog.OnReportListItemClickListener() {
                    @Override
                    public void onReportItemClick(Report report) {
                        report(mRoom.getRoomId(), report);
                    }
                });
                mReportDialog.show();
            }
        });

        // 退出群组
        mBtnQuitRoom.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                if (mucRoom == null) {
                    return;
                }
                String dialogText;
                if (role == 1) {//发现了一种情况，群创建者的userId是自己，但是角色却是3（群成员）
                    dialogText = "你确定要解散群组，并删除此群的聊天记录";
                } else {
                    dialogText = "你确定要退出群组，并删除此群的聊天记录";
                }
                new AskCommonBottomDialog(RoomInfoActivity.this,
                        dialogText, new AskCommonBottomDialog.AskCommonBottomDialogListener() {
                    @Override
                    public void onClickOk(TextView tvOk) {
                        String desc;
                        String url;
                        Map<String, String> params = new HashMap<>();
                        params.put("access_token", coreManager.getSelfStatus().accessToken);
                        params.put("roomId", mRoom.getRoomId());
                        if (role == 1) {// 解散群组
                            desc = getString(R.string.tip_disband);
                            url = coreManager.getConfig().ROOM_DELETE;
                        } else {// 退出群组
                            params.put("userId", mLoginUserId);
                            desc = getString(R.string.tip_exit);
                            url = coreManager.getConfig().ROOM_MEMBER_DELETE;
                        }
                        quitRoom(desc, url, params);
                    }

                    @Override
                    public void onClickCancel(TextView tvCancel) {

                    }

                    @Override
                    public void onClickContent(TextView tvContent, String trim) {

                    }
                }).show();
            }
        });
    }

    /**
     * 清空聊天记录
     */
    private void clearChat() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", "3");
        params.put("roomId", mRoom.getRoomId());
        HttpUtils.get().url(coreManager.getConfig().CLEAR_GROUP_CHAT).params(params).build().execute(new BaseCallback<String>(String.class) {
            @Override
            public void onResponse(ObjectResult<String> result) {
                DialogHelper.dismissProgressDialog();
                if (result.getResultCode() == 1) {
                    FriendDao.getInstance().resetFriendMessage(mLoginUserId, mRoomJid);
                    ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, mRoomJid);
                    sendBroadcast(new Intent(Constants.CHAT_HISTORY_EMPTY));// 清空聊天界面
                    MsgBroadcast.broadcastMsgUiUpdate(RoomInfoActivity.this);
                    Toast.makeText(RoomInfoActivity.this, getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
                } else {
                    ToastUtil.showToast(RoomInfoActivity.this, TextUtils.isEmpty(result.getResultMsg())
                            ? getResources().getString(R.string.failed_to_clear_chat_history)
                            : result.getResultMsg());
                }
            }

            @Override
            public void onError(Call call, Exception e) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showErrorData(RoomInfoActivity.this);
            }
        });
    }

    private void startBasicInfo(String userId) {
        BasicInfoActivity.start(mContext, userId, BasicInfoActivity.FROM_ADD_TYPE_GROUP);
    }

    /**
     * 退出群组
     */
    private void quitRoom(String desc, final String url, final Map<String, String> params) {
        DialogHelper.showDefaulteMessageProgressDialog(RoomInfoActivity.this);
        HttpUtils.get().url(url)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            deleteFriend();
                            if (isMucChatComing) {// 如果从聊天界面进入，退出 / 解散 群组需要销毁聊天界面
                                Intent intent = new Intent(RoomInfoActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                            finish();
                        } else {
                            Toast.makeText(RoomInfoActivity.this, result.getResultMsg() + "", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(RoomInfoActivity.this);
                    }
                });
    }

    private void deleteFriend() {
        // 删除这个房间
        FriendDao.getInstance().deleteFriend(mLoginUserId, mRoom.getUserId());
        // 消息表中删除
        ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, mRoom.getUserId());
        RoomMemberDao.getInstance().deleteRoomMemberTable(mRoom.getRoomId());
        // 更新消息界面
        MsgBroadcast.broadcastMsgNumReset(this);
        MsgBroadcast.broadcastMsgUiUpdate(this);
        // 更新群聊界面
        MucgroupUpdateUtil.broadcastUpdateUi(this);
        ImHelper.exitMucChat(mRoom.getUserId());
    }

    // 修改昵称
    private void showChangeNickNameDialog(final String nickName) {
        DialogHelper.showLimitSingleInputDialog(this, InternationalizationHelper.getString("JXRoomMemberVC_UpdateNickName"), nickName, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String text = ((EditText) v).getText().toString().trim();
                if (TextUtils.isEmpty(text) || text.equals(nickName)) {
                    return;
                }
                updateNickName(text);
            }
        });
    }


    /**
     * 更改群内昵称
     */
    private void updateNickName(final String nickName) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoom.getRoomId());
        params.put("userId", mLoginUserId);
        params.put("nickname", nickName);
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(mContext, R.string.update_success);
                        mNickNameTv.setText(nickName);
                        String loginUserId = coreManager.getSelf().getUserId();
                        ChatMessageDao.getInstance().updateNickName(loginUserId, mRoom.getUserId(), loginUserId, nickName);
                        mRoom.setRoomMyNickName(nickName);
                        FriendDao.getInstance().updateRoomMyNickName(mRoom.getUserId(), nickName);
                        ListenerManager.getInstance().notifyNickNameChanged(mRoom.getUserId(), loginUserId, nickName);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }


    private String conversion(double outTime) {
        String outTimeStr;
        if (outTime == -1 || outTime == 0) {
            outTimeStr = getString(R.string.permanent);
        } else if (outTime == 0.04) {
            outTimeStr = getString(R.string.one_hour);
        } else if (outTime == 1) {
            outTimeStr = getString(R.string.one_day);
        } else if (outTime == 7) {
            outTimeStr = getString(R.string.one_week);
        } else if (outTime == 30) {
            outTimeStr = getString(R.string.one_month);
        } else if (outTime == 90) {
            outTimeStr = getString(R.string.one_season);
        } else {
            outTimeStr = getString(R.string.one_year);
        }
        return outTimeStr;
    }

    /*
       举报
        */
    private void report(String roomId, Report report) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", roomId);
        params.put("reason", String.valueOf(report.getReportId()));
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().USER_REPORT)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            ToastUtil.showToast(RoomInfoActivity.this, "举报成功");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    @Override
    public void onItemClick(int position) {
        if (myself != null && myself.getRole() != 1 && myself.getRole() != 2) {  //不是群主 和 管理员
            boolean isAllowSecretlyChat = PreferenceUtils.getBoolean(mContext, Constants.IS_SEND_CARD + mRoom.getUserId(), true);
            MucRoomMember member = mCurrentMembers.get(position);
            if (member.getRole() == 1 || member.getRole() == 2) {
                if (member != null) {
                    startBasicInfo(member.getUserId());
                }
            } else if (isAllowSecretlyChat) {
                if (member != null) {
                    startBasicInfo(member.getUserId());
                }
            } else {
                // tip(getString(R.string.tip_member_disable_privately_chat));
            }
        } else if (myself != null && myself.getRole() == 1 || myself.getRole() == 2) {// 群主与管理员
            MucRoomMember member = mCurrentMembers.get(position);
            if (member != null) {
                startBasicInfo(member.getUserId());
            }
        }
    }

    @Override
    public void onAddMemberClick() {
        if (myself != null && myself.getRole() != 1 && myself.getRole() != 2) {  //不是群主 和 管理员
            // 现在添加了群组成员折叠功能,+ -号都是一直存在的，所以需要修改下逻辑，不过add_minus_count可用做于判断权限
            if (myself.disallowInvite()) {
                tip(getString(R.string.tip_disallow_invite_role_place_holder, getString(myself.getRoleName())));
            } else if (mucRoom.getAllowInviteFriend() == 1) {
                List<String> existIds = new ArrayList<>();
                for (int i = 0; i < mMembers.size(); i++) {
                    existIds.add(mMembers.get(i).getUserId());
                }
                // 邀请
                Intent intent = new Intent(RoomInfoActivity.this, AddContactsActivity.class);
                intent.putExtra("roomId", mRoom.getRoomId());
                intent.putExtra("roomJid", mRoomJid);
                intent.putExtra("roomName", mRoomNameTv.getText().toString());
                intent.putExtra("roomDes", mRoomDescTv.getText().toString());
                intent.putExtra("exist_ids", JSON.toJSONString(existIds));
                intent.putExtra("roomCreator", creator);
                startActivityForResult(intent, RESULT_FOR_ADD_MEMBER);
            } else {
                tip(getString(R.string.tip_disable_invite));
            }
        } else if (myself != null && myself.getRole() == 1 || myself.getRole() == 2) {// 群主与管理员
            List<String> existIds = new ArrayList<>();
            for (int i = 0; i < mMembers.size(); i++) {
                existIds.add(mMembers.get(i).getUserId());
            }
            // 邀请
            Intent intent = new Intent(RoomInfoActivity.this, AddContactsActivity.class);
            intent.putExtra("roomId", mRoom.getRoomId());
            intent.putExtra("roomJid", mRoomJid);
            intent.putExtra("roomName", mRoomNameTv.getText().toString());
            intent.putExtra("roomDes", mRoomDescTv.getText().toString());
            intent.putExtra("exist_ids", JSON.toJSONString(existIds));
            intent.putExtra("roomCreator", creator);
            startActivityForResult(intent, RESULT_FOR_ADD_MEMBER);
        }
    }

    @Override
    public void onDeleteMemberClick() {
        if (myself != null && myself.getRole() != 1 && myself.getRole() != 2) {  //不是群主 和 管理员 // 群主或管理员才有权限操作。正常情况下不会走这一步，因为普通成员没有显示这个
            Toast.makeText(RoomInfoActivity.this, InternationalizationHelper.getString("JXRoomMemberVC_NotAdminCannotDoThis"), Toast.LENGTH_SHORT).show();
        } else if (myself != null && myself.getRole() == 1 || myself.getRole() == 2) {// 群主与管理员
            Intent intent = new Intent(mContext, DeleteGroupMemberActivity.class);
            intent.putExtra("roomId", mucRoom.getId());
            intent.putExtra("isDelete", true);
            startActivity(intent);
        }
    }

    @Override
    public void onSuccess() {
        new GetPictureCommonDialog(this, new GetPictureCommonDialog.GetPictureCommonDialogListener() {
            @Override
            public void onClickTakingPhotos(TextView tvTakingPhotos) {
                takePhoto();
            }

            @Override
            public void onClickChoosePicture(TextView tvChoosePicture) {
                selectPhoto();
            }

            @Override
            public void onClickCancel(TextView tvCancel) {

            }
        }).show();
    }

    @Override
    public void onFailure(List<String> data) {
        ToastUtil.showToast(R.string.please_turn_on_camera_storage_permissions);
    }

    public class RefreshBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(com.iimm.miliao.broadcast.OtherBroadcast.REFRESH_MANAGER)) {
                String roomId = intent.getStringExtra("roomId");
                String toUserId = intent.getStringExtra("toUserId");
                boolean isSet = intent.getBooleanExtra("isSet", false);
                if (roomId.equals(mRoomJid) && toUserId.equals(mLoginUserId)) {
                    TipDialog tipDialog = new TipDialog(RoomInfoActivity.this);
                    tipDialog.setmConfirmOnClickListener(isSet ? getString(R.string.tip_became_manager) : getString(R.string.tip_be_cancel_manager)
                            , new TipDialog.ConfirmOnClickListener() {
                                @Override
                                public void confirm() {
                                    finish();
                                }
                            });
                    tipDialog.show();
                }
            } else if (action.equals(ACTION_MSG_UPDATE_ROOM_INVITE)) {
                if (mucRoom != null) {
                    int enabled = intent.getIntExtra(EXTRA_ENABLED, -1);
                    if (enabled != -1) {
                        mucRoom.setAllowInviteFriend(enabled);
                    }
                }
            }
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            try {
                unregisterReceiver(receiver);
            } catch (Exception e) {
                // onCreate异常时可能没走到绑定Receiver,
                // 无论如何都不应该在destroy时崩溃，
                // 重复上报，可以加个boolean判断避免，无所谓了，
                Reporter.post("解绑Receiver异常，", e);
            }
        }
        // 这个没注册时取消注册也不会崩溃，
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_FOR_ADD_MEMBER && resultCode == RESULT_OK) {// 添加成员返回
            loadMembers();
        } else if (requestCode == RESULT_FOR_MODIFY_NOTICE && resultCode == RESULT_OK) {// 修改公告返回
            if (data != null) {
                boolean isNeedUpdate = data.getBooleanExtra("isNeedUpdate", false);
                if (isNeedUpdate) {
                    loadMembers();
                }
            }
        } else if (requestCode == REQUEST_CODE_CAPTURE_CROP_PHOTO) {// 拍照返回再去裁减
            if (resultCode == Activity.RESULT_OK) {
                if (mPictureContentOrFileUri != null) {
                    mPictureCropFileUri = CameraUtil.getOutputMediaFileUri(this, CameraUtil.MEDIA_TYPE_IMAGE);
                    CameraUtil.cropImageAfterTakePicture(this, mPictureContentOrFileUri, mPictureCropFileUri, REQUEST_CODE_CROP_PHOTO, 1, 1, 300, 300);
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_PICK_CROP_PHOTO) {// 选择一张图片,然后立即调用裁减
            Log.e("zx", "onActivityResult: 选择一张图片");
            if (resultCode == Activity.RESULT_OK) {
                Log.e("zx", "onActivityResult: RESULT_OK 选择一张图片");
                if (data != null && data.getData() != null) {
                    Uri o = data.getData();
                    mPictureCropFileUri = CameraUtil.getOutputMediaFileUri(this, CameraUtil.MEDIA_TYPE_IMAGE);
                    CameraUtil.cropImage(this, o, mPictureCropFileUri, REQUEST_CODE_CROP_PHOTO, 1, 1, 300, 300);
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_CROP_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                if (mPictureCropFileUri != null) {
                    File currentFile = new File(mPictureCropFileUri.getPath());
                    // 上传头像
                    uploadAvatar(currentFile);
                } else {
                    ToastUtil.showToast(this, R.string.c_crop_failed);
                }
            }
        }
    }


    private void uploadAvatar(File file) {
        // 显示正在上传的ProgressDialog
        DialogHelper.showDefaulteMessageProgressDialog(this);
        if (coreManager.getConfig().IS_OPEN_OBS_STATUS != 0) {
            AtomicInteger tempS = new AtomicInteger();
            AtomicInteger tempE = new AtomicInteger();
            if (mRoom == null || TextUtils.isEmpty(mRoom.getUserId())) {
                ToastUtil.showToast(mContext, R.string.upload_avatar_failed);
                return;
            }
            OBSUtils.uploadGroupHeadImg(mRoom.getUserId(), file, RoomInfoActivity.this, coreManager, result -> {
                        LogUtils.log("uploadImg---头像成功回调");
                        if (tempS.get() > 0) {
                            return;
                        }
                        tempS.getAndIncrement();
                        DialogHelper.dismissProgressDialog();
                        boolean success = false;
                        if (result != null && !TextUtils.isEmpty(transformObjectUrl(result.getObjectUrl()))) {
                            success = true;
                        }
                        if (success) {
                            ToastUtil.showToast(mContext, R.string.upload_avatar_success);
                            AvatarHelper.getInstance().updateAvatar(mRoomJid);// 更新时间
                            ImageCache.removeBitmap(mRoom.getRoomId());
                        } else {
                            ToastUtil.showToast(mContext, R.string.upload_avatar_failed);
                        }
                    }, msg -> {
                        LogUtils.log("uploadImg---头像错误回调");
                        if (tempE.get() > 0) {
                            return;
                        }
                        LogUtils.log("uploadImg----选用服务器上传");
                        uploadHeadImgToService(file);
                    }
            );
        } else {
            uploadHeadImgToService(file);
        }
    }


    public void uploadHeadImgToService(File file) {
        RequestParams params = new RequestParams();
        params.put("jid", mRoomJid);
        try {
            params.put("file", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(coreManager.getConfig().ROOM_UPDATE_PICTURE, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                DialogHelper.dismissProgressDialog();
                boolean success = false;
                if (arg0 == 200) {
                    Result result = null;
                    try {
                        result = JSON.parseObject(new String(arg2), Result.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (result != null && result.getResultCode() == Result.CODE_SUCCESS) {
                        success = true;
                    }
                }

                if (success) {
                    ToastUtil.showToast(mContext, R.string.upload_avatar_success);
                    AvatarHelper.getInstance().updateAvatar(mRoomJid);// 更新时间
                    ImageCache.removeBitmap(mRoom.getRoomId());
                } else {
                    ToastUtil.showToast(mContext, R.string.upload_avatar_failed);
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(mContext, R.string.upload_avatar_failed);
            }
        });
    }

    /**
     * 更新消息保存天数
     */
    private void updateChatRecordTimeOut(final double outTime) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoom.getRoomId());
        params.put("chatRecordTimeOut", String.valueOf(outTime));

        HttpUtils.get().url(coreManager.getConfig().ROOM_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            Toast.makeText(RoomInfoActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                            mTvMsgSaveDays.setText(conversion(outTime));
                            FriendDao.getInstance().updateChatRecordTimeOut(mRoom.getUserId(), outTime);

                            Intent intent = new Intent();
                            intent.setAction(Constants.CHAT_TIME_OUT_ACTION);
                            intent.putExtra("friend_id", mRoom.getUserId());
                            intent.putExtra("time_out", outTime);
                            mContext.sendBroadcast(intent);
                        } else {
                            Toast.makeText(RoomInfoActivity.this, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    /**
     * 消息免打扰
     */
    private void updateDisturbState(final int disturb) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoom.getRoomId());
        params.put("userId", mLoginUserId);
        params.put("offlineNoPushMsg", String.valueOf(disturb));
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_DISTURB)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            mRoom.setOfflineNoPushMsg(disturb);
                            FriendDao.getInstance().updateOfflineNoPushMsgStatus(mRoom.getUserId(), disturb);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    /**
     * 更新群组内的某个属性
     */
    private void updateSingleAttribute(final String attributeKey, final String attributeValue) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoom.getRoomId());
        params.put(attributeKey, attributeValue);
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            Toast.makeText(mContext, R.string.modify_succ, Toast.LENGTH_SHORT).show();
                            if (attributeKey.equals("talkTime")) {// 全体禁言
                            }
                            switch (attributeKey) {
                                case "talkTime":
                                    if (Long.parseLong(attributeValue) > 0) {// 开启全体禁言
                                        PreferenceUtils.putBoolean(mContext, Constants.GROUP_ALL_SHUP_UP + mRoom.getUserId(), true);
                                    } else {// 取消全体禁言
                                        PreferenceUtils.putBoolean(mContext, Constants.GROUP_ALL_SHUP_UP + mRoom.getUserId(), false);
                                    }
                                    break;
                                case "maxUserSize":
                                    mucRoom.setMaxUserSize(Integer.valueOf(attributeValue));
                                    tvMemberLimit.setText(attributeValue);
                                    break;
                            }
                        } else {
                            Toast.makeText(mContext, R.string.modify_fail, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    private void startMoreGroupUser(MucRoom mucRoom) {
        if (mucRoom == null) {
            return;
        }
        Intent intent = new Intent(mContext, GroupMoreFeaturesActivity.class);
        intent.putExtra("roomId", mucRoom.getId());
        intent.putExtra("roomJidLocal", mucRoom.getUserId());
        intent.putExtra("isLoadByService", true);
        intent.putExtra("role", role);
        intent.putExtra("allowSendCard", mucRoom.getAllowSendCard() == 1);
        intent.putExtra("isDisplayAllUser", mucRoom.getShowMember() == 1);
        startActivity(intent);
    }


    /**
     * @param isGroupManager 群主或群管理员，
     */
    private void updateMemberLimit(boolean isGroupManager) {
        View rlMemberLimit = findViewById(R.id.member_limit_rl);
        if (isGroupManager && coreManager.getSelf().isSuperManager()) {
            rlMemberLimit.setVisibility(View.VISIBLE);
            rlMemberLimit.setOnClickListener(v -> {
                DialogHelper.input(this, "设置群人数上限", "群人数上限", new VerifyDialog.VerifyClickListener() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void send(String str) {
                        if (TextUtils.isDigitsOnly(str)) {
                            updateSingleAttribute("maxUserSize", str);
                        } else {
                            Reporter.unreachable();
                            ToastUtil.showToast(RoomInfoActivity.this, "数字格式不正确");
                        }
                    }
                });
            });
        } else {
            rlMemberLimit.setVisibility(View.GONE);
        }
    }

    public void tip(String tip) {
        ToastUtil.showToast(RoomInfoActivity.this, tip);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventGroupStatus eventGroupStatus) {
        if (eventGroupStatus.getWhichStatus() == 0) {
            mucRoom.setShowRead(eventGroupStatus.getGroupManagerStatus());
        } else if (eventGroupStatus.getWhichStatus() == 1) {
            mucRoom.setIsLook(eventGroupStatus.getGroupManagerStatus());
        } else if (eventGroupStatus.getWhichStatus() == 2) {
            mucRoom.setIsNeedVerify(eventGroupStatus.getGroupManagerStatus());
        } else if (eventGroupStatus.getWhichStatus() == 3) {
            mucRoom.setShowMember(eventGroupStatus.getGroupManagerStatus());
        } else if (eventGroupStatus.getWhichStatus() == 4) {
            mucRoom.setAllowSendCard(eventGroupStatus.getGroupManagerStatus());
        } else if (eventGroupStatus.getWhichStatus() == 5) {
            mucRoom.setAllowInviteFriend(eventGroupStatus.getGroupManagerStatus());
        } else if (eventGroupStatus.getWhichStatus() == 6) {
            mucRoom.setAllowUploadFile(eventGroupStatus.getGroupManagerStatus());
        } else if (eventGroupStatus.getWhichStatus() == 7) {
            mucRoom.setAllowConference(eventGroupStatus.getGroupManagerStatus());
        } else if (eventGroupStatus.getWhichStatus() == 8) {
            mucRoom.setAllowSpeakCourse(eventGroupStatus.getGroupManagerStatus());
        } else if (eventGroupStatus.getWhichStatus() == 9) {
            mucRoom.setIsAttritionNotice(eventGroupStatus.getGroupManagerStatus());
        }
        if (eventGroupStatus.getWhichStatus() == 9) {
            mucRoom.setIsAttritionNotice(eventGroupStatus.getGroupManagerStatus());
        } else if (eventGroupStatus.getWhichStatus() == 11) {// 设置/取消 管理员, 隐身人，监控人，
            mucRoom.setIsShowSignIn(eventGroupStatus.getGroupManagerStatus());
            if (eventGroupStatus.getGroupManagerStatus() == 1) {
                signll.setVisibility(View.VISIBLE);
            } else {
                signll.setVisibility(View.GONE);
            }
        } else if (eventGroupStatus.getWhichStatus() == 10001) {// 删除群成员
            loadMembers();
            // 需通知群聊页面刷新
            MsgBroadcast.broadcastMsgRoomUpdate(mContext);
        } else if (eventGroupStatus.getWhichStatus() == 10002) {// 转让群
            loadMembers();
        } else if (eventGroupStatus.getWhichStatus() == 10003) {// 备注
            loadMembers();
            // 需通知群聊页面刷新
            MsgBroadcast.broadcastMsgRoomUpdate(mContext);
        }
    }


}
