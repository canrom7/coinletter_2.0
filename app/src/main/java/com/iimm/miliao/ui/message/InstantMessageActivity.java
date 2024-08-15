package com.iimm.miliao.ui.message;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.EventBusMsg;
import com.iimm.miliao.bean.ForwardResultOfGroup;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.bean.message.MucRoom;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.RoomMemberDao;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.view.MessageAvatar;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * 转发 最近联系人
 */
public class InstantMessageActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "InstantMessageActivity";
    private static final int REQUEST_CODE_SELECT_NEW_C = 1001;
    private TextView mCreateChat;
    private ListView mLvRecentlyMessage;
    private List<Friend> friends = new ArrayList<>();

    private boolean isMoreSelected; // 是否为多选转发
    private boolean isSingleOrMerge; // 逐条还是合并转发
    // 在ChatActivity || MucChatActivity内，通过toUserId与messageId获得该条转发消息 在进行封装
    private String toUserId;
    private String messageId;

    private String mLoginUserId;

    private InstantMessageConfirm menuWindow;
    private String[] mFriendIds;
    private String[] mGroupIds = new String[]{};
    private ArrayList<ChatMessage> mSelectMessageLists;
    private boolean isGroupHandleOk;
    private boolean isSingleFriendHandleOk;
    private boolean isSucceeded = false;  //是否 已经成功 防止多线程 多次调用

    private List<ForwardResultOfGroup> forwardingTheResultSetOfAGroup;  //群组转发的结果
    private Thread mGroupHandleThread;
    private Thread mSingleFriendHandleThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messageinstant);
        isMoreSelected = getIntent().getBooleanExtra(Constants.IS_MORE_SELECTED_INSTANT, false);
        isSingleOrMerge = getIntent().getBooleanExtra(Constants.IS_SINGLE_OR_MERGE, false);
        // 在ChatContentView内长按转发才需要以下参数
        toUserId = getIntent().getStringExtra("fromUserId");
        messageId = getIntent().getStringExtra("messageId");
        mSelectMessageLists = getIntent().getParcelableArrayListExtra("SelectMessageList");

        mLoginUserId = coreManager.getSelf().getUserId();

        initActionBar();
        loadData();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.most_recent_contact));
        TextView tvTitleRight = findViewById(R.id.tv_title_right);
        tvTitleRight.setText(R.string.confirm);
        tvTitleRight.setVisibility(View.VISIBLE);
        tvTitleRight.setOnClickListener(v -> {
            DialogHelper.showDefaulteMessageProgressDialog(InstantMessageActivity.this);
            Set<String> ids = new HashSet<>();
            if (mFriendIds != null) {
                for (String id : mFriendIds) {
                    ids.add(id);
                }
            }
            if (mGroupIds != null) {
                for (String id : mGroupIds) {
                    ids.add(id);
                }
            }
            for (Friend friend : friends) {
                if (friend.isSelect()) {
                    ids.add(friend.getUserId());
                }
            }
            if (ids.size() > 0) {
                List<Friend> friends = FriendDao.getInstance().getFriends(coreManager.getSelf().getUserId(), ids.toArray());
                if (friends == null) {
                    ToastUtil.showToast(InstantMessageActivity.this, R.string.querying_a_friend_error);
                    DialogHelper.dismissProgressDialog();
                } else if (friends.size() > 0) {
                    //检测数据进行转发
                    handleFriends(friends);
                } else {
                    ToastUtil.showToast(InstantMessageActivity.this, R.string.please_select_a_forwarded_friend);
                    DialogHelper.dismissProgressDialog();

                }
            } else {
                ToastUtil.showToast(InstantMessageActivity.this, R.string.please_select_a_forwarded_friend);
                DialogHelper.dismissProgressDialog();

            }
        });

    }


    /**
     * 处理好友组，检测是否可以转发消息
     *
     * @param friends
     */
    private void handleFriends(List<Friend> friends) {
        if (friends.size() == 1) {
            //就选择了一个好友 直接按原来的流程 走
            showPopuWindow(mCreateChat, friends.get(0));
            DialogHelper.dismissProgressDialog();

        } else {
            //选择了多个好友
            //分离 群组 和 好友
            List<Friend> groupFriend = new ArrayList<>();
            List<Friend> sFriend = new ArrayList<>();
            for (Friend friend : friends) {
                if (friend.getRoomFlag() != 0) {// 群组，调接口判断一些群属性状态
                    groupFriend.add(friend);
                } else {
                    sFriend.add(friend);
                }
            }
            Log.i(TAG, "最终选择的好友：" + JSON.toJSONString(sFriend));
            Log.i(TAG, "最终选择的群组：" + JSON.toJSONString(groupFriend));
            forceInterruptThread();

            //启动线程 进行处理 群组 的
            mGroupHandleThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    isGroupHandleOk = false;
                    forwardingTheResultSetOfAGroup = new ArrayList<>();
                    handleGroupFriend(groupFriend);
                }
            };
            mGroupHandleThread.start();
            //启动线程 进行处理好友 的
            mSingleFriendHandleThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    isSingleFriendHandleOk = false;
                    handleSingleFriend(sFriend);
                }
            };
            mSingleFriendHandleThread.start();
        }


    }

    /**
     * 转发单人
     *
     * @param sFriend
     */
    private void handleSingleFriend(List<Friend> sFriend) {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
        if (sFriend != null && sFriend.size() > 0) {
            Friend currentSendFriend = sFriend.remove(0);
            Log.i(TAG, "进入处理这个好友：" + JSON.toJSONString(currentSendFriend));
            if (!isMoreSelected && messageId != null) {
                //单选转发多个好友
                if (radioForwarding(currentSendFriend)) {
                    handleSingleFriend(new ArrayList<>());
                } else {
                    handleSingleFriend(sFriend);
                }
            } else if (isMoreSelected && mSelectMessageLists != null) {
                //多选转发多个好友
                if (isSingleOrMerge) {
                    //多人合并转发
                    multiPersonMergeForwardingMessage(currentSendFriend);
                    handleSingleFriend(sFriend);
                } else {
                    //多人逐条转发
                    multiPersonForwardingOneByOne(currentSendFriend);
                    handleSingleFriend(sFriend);
                }
            }
        } else {
            isSingleFriendHandleOk = true;
            Log.i(TAG, "好友转发完成");
            if (isGroupHandleOk) {
                //群组已经处理完了  单聊也处理完了
                runOnUiThread(this::allForwardProcessingSucceeded);
            }
        }
    }


    /**
     * 多人 逐条转发
     *
     * @param currentSendFriend
     */
    private void multiPersonForwardingOneByOne(Friend currentSendFriend) {
        for (int i = 0; i < mSelectMessageLists.size(); i++) {
            ChatMessage chatMessage = mSelectMessageLists.get(i);
            if (chatMessage == null) { //这条是空消息 略过
                continue;
            } else {
                //不是空的而且还是ChatMessage
                if (chatMessage.getType() == Constants.TYPE_RED) {
                    chatMessage.setType(Constants.TYPE_TEXT);
                    chatMessage.setContent(getString(R.string.msg_red_packet));
                } else if (chatMessage.getType() == Constants.TYPE_READ_EXCLUSIVE) {
                    chatMessage.setType(Constants.TYPE_TEXT);
                    chatMessage.setContent(getString(R.string.msg_red_packet_exclusive));
                } else  if (chatMessage.getType() == Constants.TYPE_CLOUD_RED) {
                    chatMessage.setType(Constants.TYPE_TEXT);
                    chatMessage.setContent(getString(R.string.msg_red_cloud_packet));
                } else if (chatMessage.getType() == Constants.TYPE_TRANSFER) {
                    chatMessage.setType(Constants.TYPE_TEXT);
                    chatMessage.setContent(getString(R.string.msg_red_transfer));
                } else if (chatMessage.getType() == Constants.TYPE_CLOUD_TRANSFER) {
                    chatMessage.setType(Constants.TYPE_TEXT);
                    chatMessage.setContent(getString(R.string.micro_tip_transfer_money));
                } else if (chatMessage.getType() >= Constants.TYPE_IS_CONNECT_VOICE
                        && chatMessage.getType() <= Constants.TYPE_EXIT_VOICE) {
                    chatMessage.setType(Constants.TYPE_TEXT);
                    chatMessage.setContent(getString(R.string.msg_video_voice));
                } else if (chatMessage.getType() == Constants.TYPE_SHAKE) {
                    chatMessage.setType(Constants.TYPE_TEXT);
                    chatMessage.setContent(getString(R.string.msg_shake));
                }
                chatMessage.setFromUserId(mLoginUserId);
                chatMessage.setFromUserName(coreManager.getSelf().getNickName());
                chatMessage.setToUserId(currentSendFriend.getUserId());
                chatMessage.setUpload(true);
                chatMessage.setMySend(true);
                chatMessage.setReSendCount(0);
                chatMessage.setSendRead(false);
                chatMessage.setIsEncrypt(0);
                chatMessage.setIsReadDel(PreferenceUtils.getInt(mContext, Constants.MESSAGE_READ_FIRE + currentSendFriend.getUserId() + mLoginUserId, 0));
                chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                chatMessage.setPacketId(ToolUtils.getUUID());
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, currentSendFriend.getUserId(), chatMessage);
                if (currentSendFriend.getRoomFlag() != 0) {
                    ImHelper.sendMucChatMessage(currentSendFriend.getUserId(), chatMessage);
                } else {
                    ImHelper.sendChatMessage(currentSendFriend.getUserId(), chatMessage);
                }
            }
        }
    }


    /**
     * 多人合并转发
     *
     * @param currentSendFriend
     */
    private void multiPersonMergeForwardingMessage(Friend currentSendFriend) {
        List<String> mStringHistory = new ArrayList<>();
        for (int i = 0; i < mSelectMessageLists.size(); i++) {
            ChatMessage message = mSelectMessageLists.get(i);
            if (message == null) { //这条是空消息 略过
                continue;
            } else {
                //不是空的而且还是ChatMessage
                String body = message.toJsonString();
                mStringHistory.add(body);
            }

        }
        String detail = JSON.toJSONString(mStringHistory);
        ChatMessage chatMessage = new ChatMessage();  //生成一条 合并的转发 消息
        chatMessage.setType(Constants.TYPE_CHAT_HISTORY);
        chatMessage.setFromUserId(mLoginUserId);
        chatMessage.setFromUserName(coreManager.getSelf().getNickName());
        chatMessage.setToUserId(currentSendFriend.getUserId());
        chatMessage.setContent(detail);
        chatMessage.setMySend(true);
        chatMessage.setReSendCount(0);
        chatMessage.setSendRead(false);
        chatMessage.setIsEncrypt(0);
        chatMessage.setIsReadDel(PreferenceUtils.getInt(mContext, Constants.MESSAGE_READ_FIRE + currentSendFriend.getUserId() + mLoginUserId, 0));
        String s = TextUtils.isEmpty(currentSendFriend.getRemarkName()) ? currentSendFriend.getNickName() : currentSendFriend.getRemarkName();
        chatMessage.setObjectId(getString(R.string.chat_history_place_holder, s, coreManager.getSelf().getNickName()));
        chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
        chatMessage.setPacketId(ToolUtils.getUUID());
        ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, currentSendFriend.getUserId(), chatMessage);
        if (currentSendFriend.getRoomFlag() != 0) {
            ImHelper.sendMucChatMessage(currentSendFriend.getUserId(), chatMessage);
        } else {
            ImHelper.sendChatMessage(currentSendFriend.getUserId(), chatMessage);
        }
    }


    /**
     * 单条消息 多人 转发
     *
     * @param currentSendFriend 要发送的人
     * @return 这个消息 查询不到true   否则 false
     */
    private boolean radioForwarding(Friend currentSendFriend) {
        ChatMessage chatMessage = ChatMessageDao.getInstance().findMsgById(mLoginUserId, toUserId, messageId);
        if (chatMessage == null) {  //这条消息没了，接下来要发送的人 也不要发送了
            return true;
        }
        chatMessage.setFromUserId(mLoginUserId);
        chatMessage.setFromUserName(coreManager.getSelf().getNickName());
        chatMessage.setToUserId(currentSendFriend.getUserId());
        chatMessage.setUpload(true);
        chatMessage.setMySend(true);
        chatMessage.setReSendCount(5);
        chatMessage.setSendRead(false);
        // 因为该消息的原主人可能开启了消息传输加密，我们对于content字段解密后存入了数据库，但是isEncrypt字段并未改变
        // 如果我们将此消息转发给另一人，对方可能会对我方已解密的消息再次进行解密
        chatMessage.setIsEncrypt(0);
        chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
        chatMessage.setPacketId(ToolUtils.getUUID());
        ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, currentSendFriend.getUserId(), chatMessage);
        if (currentSendFriend.getRoomFlag() != 0) {
            ImHelper.sendMucChatMessage(currentSendFriend.getUserId(), chatMessage);
        } else {
            ImHelper.sendChatMessage(currentSendFriend.getUserId(), chatMessage);
        }
        return false;
    }


    /**
     * 所有的转发消息都处理成功了   （这里两个线程可能会同时调用 ）
     */
    private void allForwardProcessingSucceeded() {
        synchronized (InstantMessageActivity.class) {
            if (!isSucceeded) {
                isSucceeded = true;
                DialogHelper.dismissProgressDialog();
                Log.i(TAG, "全部处理成功");
                EventBusMsg eventBusMsg = new EventBusMsg();
                eventBusMsg.setMessageType(Constants.EVENTBUS_TRANSPOND_REFRESH_UI);
                if (forwardingTheResultSetOfAGroup != null && forwardingTheResultSetOfAGroup.size() > 0) {
                    //有错误
                    Log.i(TAG, "没有转发的群组：" + JSON.toJSONString(forwardingTheResultSetOfAGroup));
                    ToastUtil.showToast(this, R.string.forward_message_successfully);
                    EventBus.getDefault().post(eventBusMsg);
                    finish();
                } else {
                    //全部转发成功
                    ToastUtil.showToast(this, R.string.forward_message_successfully);
                    EventBus.getDefault().post(eventBusMsg);
                    finish();
                }
            }
        }
    }

    /**
     * 转发 群聊
     */
    private void handleGroupFriend(List<Friend> groupFriend) {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
        if (groupFriend != null && groupFriend.size() > 0) {
            Friend currentSendFriend = groupFriend.remove(0);
            Log.i(TAG, "进入处理这个群组：" + JSON.toJSONString(currentSendFriend));
            if (!isMoreSelected && messageId != null) {
                //单选转发多个群聊  逐条 转发
                isSendThisGroup(currentSendFriend, (isSend, friend, message) -> {
                    if (isSend) {
                        if (InstantMessageActivity.this.radioForwarding(currentSendFriend)) {
                            InstantMessageActivity.this.handleGroupFriend(new ArrayList<>());
                        } else {
                            InstantMessageActivity.this.handleGroupFriend(groupFriend);
                        }
                    } else {
                        createGroupForwardFailureInfo(currentSendFriend, message, null);
                        InstantMessageActivity.this.handleGroupFriend(groupFriend);
                    }
                });
            } else if (isMoreSelected && mSelectMessageLists != null) {
                //选择了多条消息 并且选择的消息不为空
                if (isSingleOrMerge) {
                    //合并转发
                    isSendThisGroup(currentSendFriend, (isSend, friend, message) -> {
                        if (isSend) {
                            multiPersonMergeForwardingMessage(currentSendFriend);
                            handleGroupFriend(groupFriend);
                        } else {
                            createGroupForwardFailureInfo(currentSendFriend, message, mSelectMessageLists);
                            handleGroupFriend(groupFriend);
                        }
                    });
                } else {
                    //逐条转发
                    isSendThisGroup(currentSendFriend, (isSend, friend, message) -> {
                        if (isSend) {
                            multiPersonForwardingOneByOne(currentSendFriend);
                            handleGroupFriend(groupFriend);
                        } else {
                            createGroupForwardFailureInfo(currentSendFriend, message, mSelectMessageLists);
                            handleGroupFriend(groupFriend);
                        }
                    });
                }
            }
        } else {
            isGroupHandleOk = true;
            Log.i(TAG, "群组转发完成");
            if (isSingleFriendHandleOk) { //单个好友也处理完了
                runOnUiThread(this::allForwardProcessingSucceeded);
            }
        }
    }


    /**
     * 生成 转发群组失败 信息
     *
     * @param currentSendFriend  当前群组
     * @param failureMessage     失败的信息
     * @param selectMessageLists 失败的消息 可能 为 null
     */
    private void createGroupForwardFailureInfo(Friend currentSendFriend, String failureMessage, ArrayList<ChatMessage> selectMessageLists) {
        ForwardResultOfGroup forwardResultOfGroup = new ForwardResultOfGroup();
        forwardResultOfGroup.setFriend(currentSendFriend);
        forwardResultOfGroup.setFailureMessage(failureMessage);
        List<ChatMessage> messages = new ArrayList<>();
        if (selectMessageLists != null) {
            for (int i = 0; i < selectMessageLists.size(); i++) {
                ChatMessage selectMessageList = selectMessageLists.get(i);
                if (selectMessageList != null) {
                    messages.add(selectMessageList);
                }
            }
        }
        forwardResultOfGroup.setMessages(messages);
        forwardingTheResultSetOfAGroup.add(forwardResultOfGroup);
    }

    private void isSendThisGroup(Friend currentSendFriend, GroupInfoListener groupInfoListener) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", currentSendFriend.getRoomId());
        HttpUtils.get().url(coreManager.getConfig().ROOM_GET_ROOM)
                .params(params)
                .build()
                .execute(new BaseCallback<MucRoom>(MucRoom.class) {
                             @Override
                             public void onResponse(ObjectResult<MucRoom> result) {// 数据结果与room/get接口一样，只是服务端没有返回群成员列表的数据
                                 if (result.getResultCode() == 1 && result.getData() != null) {
                                     final MucRoom mucRoom = result.getData();
                                     if (mucRoom.getMember() == null) {// 被踢出该群组
                                         FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, mucRoom.getJid(), 1);// 更新本地群组状态
                                         groupInfoListener.isSend(false, currentSendFriend, getString(R.string.tip_forward_kick));
                                     } else {// 正常状态
                                         if (mucRoom.getS() == -1) {// 该群组已被锁定
                                             FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, mucRoom.getJid(), 3);// 更新本地群组状态
                                             groupInfoListener.isSend(false, currentSendFriend, getString(R.string.tip_group_disable_by_service));
                                             return;
                                         }
                                         int role = mucRoom.getMember().getRole();
                                         // 更新禁言状态
                                         FriendDao.getInstance().updateRoomTalkTime(mLoginUserId, mucRoom.getJid(), mucRoom.getMember().getTalkTime());

                                         // 更新部分群属性
                                         MyApplication.getInstance().saveGroupPartStatus(mucRoom.getJid(), mucRoom.getShowRead(),
                                                 mucRoom.getAllowSendCard(), mucRoom.getAllowConference(),
                                                 mucRoom.getAllowSpeakCourse(), mucRoom.getTalkTime());

                                         // 更新个人职位
                                         RoomMemberDao.getInstance().updateRoomMemberRole(mucRoom.getId(), mLoginUserId, role);
                                         if (role == 1 || role == 2) {// 群组或管理员 直接转发出去
                                             groupInfoListener.isSend(true, null, null);
                                         } else {
                                             if (mucRoom.getTalkTime() > 0) {// 全体禁言
                                                 groupInfoListener.isSend(false, currentSendFriend, getString(R.string.tip_now_ban_all));
                                             } else if (mucRoom.getMember().getTalkTime() > System.currentTimeMillis() / 1000) {// 禁言
                                                 groupInfoListener.isSend(false, currentSendFriend, getString(R.string.tip_forward_ban));
                                             } else {
                                                 groupInfoListener.isSend(true, null, null);
                                             }
                                         }
                                     }
                                 } else {// 群组已解散
                                     FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, currentSendFriend.getUserId(), 2);// 更新本地群组状态
                                     groupInfoListener.isSend(false, currentSendFriend, getString(R.string.tip_forward_disbanded));
                                 }
                             }

                             @Override
                             public void onError(Call call, Exception e) {
                                 ToastUtil.showNetError(mContext);
                                 groupInfoListener.isSend(false, currentSendFriend, getString(R.string.net_exception));
                             }
                         }
                );
    }

    private void loadData() {
        friends = FriendDao.getInstance().getNearlyFriendMsg(coreManager.getSelf().getUserId());
        Iterator<Friend> it = friends.iterator();
        while (it.hasNext()) {
            Friend friend = it.next();
            if (TextUtils.equals(friend.getUserId(), Constants.ID_NEW_FRIEND_MESSAGE) || TextUtils.equals(friend.getUserId(), coreManager.getSelf().getUserId())) {
                it.remove();
            }
        }
    }

    private void initView() {
        mCreateChat = findViewById(R.id.tv_create_newmessage);
        mCreateChat.setOnClickListener(this);
        mLvRecentlyMessage = findViewById(R.id.lv_recently_message);
        MessageRecentlyAdapter messageRecentlyAdapter = new MessageRecentlyAdapter();
        mLvRecentlyMessage.setAdapter(messageRecentlyAdapter);
        mLvRecentlyMessage.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                friends.get(position).setSelect(!friends.get(position).isSelect());
                messageRecentlyAdapter.notifyDataSetChanged();
//                Friend friend = friends.get(position).getFriend();
//                showPopuWindow(view, friend);
            }
        });
    }

    private void showPopuWindow(View view, Friend friend) {
        menuWindow = new InstantMessageConfirm(InstantMessageActivity.this, new ClickListener(friend), friend);
        menuWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tv_create_newmessage:
                Intent intent = new Intent(this, SelectNewContactsActivity.class);
                intent.putExtra(Constants.IS_MORE_SELECTED_INSTANT, isMoreSelected);
                intent.putExtra(Constants.IS_SINGLE_OR_MERGE, isSingleOrMerge);
                intent.putExtra("friendIds", mFriendIds);
                intent.putExtra("groupIds", mGroupIds);
                intent.putExtra("fromUserId", toUserId);
                intent.putExtra("messageId", messageId);
                startActivityForResult(intent, REQUEST_CODE_SELECT_NEW_C);
                break;
            default:
                break;
        }
    }

    private void forwardingStep(Friend friend) {
        if (isMoreSelected) {// 多选转发 通知多选页面(即多选消息的单聊 || 群聊页面，在该页面获取选中的消息在发送出去)
            EventBus.getDefault().post(new EventMoreSelected(friend.getUserId(), isSingleOrMerge, friend.getRoomFlag() != 0));
            finish();
        } else {// 普通转发
            if (friend.getRoomFlag() == 0) {// 单聊
                Intent intent = new Intent(InstantMessageActivity.this, ChatActivity.class);
                intent.putExtra(ChatActivity.FRIEND, friend);
                intent.putExtra("fromUserId", toUserId);
                intent.putExtra("messageId", messageId);
                startActivity(intent);
            } else {  // 群聊
                Intent intent = new Intent(InstantMessageActivity.this, MucChatActivity.class);
                intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
                intent.putExtra(AppConstant.EXTRA_NICK_NAME, friend.getNickName());
                intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
                intent.putExtra("fromUserId", toUserId);
                intent.putExtra("messageId", messageId);
                startActivity(intent);
            }
            setResult(RESULT_OK);
            finish();
        }
    }

    /**
     * 获取自己在该群组的信息(职位、昵称、禁言时间等)以及群属性
     */
    private void isSupportForwarded(final Friend friend) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", friend.getRoomId());
        HttpUtils.get().url(coreManager.getConfig().ROOM_GET_ROOM)
                .params(params)
                .build()
                .execute(new BaseCallback<MucRoom>(MucRoom.class) {

                             @Override
                             public void onResponse(ObjectResult<MucRoom> result) {// 数据结果与room/get接口一样，只是服务端没有返回群成员列表的数据
                                 if (result.getResultCode() == 1 && result.getData() != null) {
                                     final MucRoom mucRoom = result.getData();
                                     if (mucRoom.getMember() == null) {// 被踢出该群组
                                         FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, mucRoom.getJid(), 1);// 更新本地群组状态
                                         DialogHelper.tip(InstantMessageActivity.this, getString(R.string.tip_forward_kick));
                                     } else {// 正常状态
                                         if (mucRoom.getS() == -1) {// 该群组已被锁定
                                             FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, mucRoom.getJid(), 3);// 更新本地群组状态
                                             DialogHelper.tip(InstantMessageActivity.this, getString(R.string.tip_group_disable_by_service));
                                             return;
                                         }
                                         int role = mucRoom.getMember().getRole();
                                         // 更新禁言状态
                                         FriendDao.getInstance().updateRoomTalkTime(mLoginUserId, mucRoom.getJid(), mucRoom.getMember().getTalkTime());

                                         // 更新部分群属性
                                         MyApplication.getInstance().saveGroupPartStatus(mucRoom.getJid(), mucRoom.getShowRead(),
                                                 mucRoom.getAllowSendCard(), mucRoom.getAllowConference(),
                                                 mucRoom.getAllowSpeakCourse(), mucRoom.getTalkTime());

                                         // 更新个人职位
                                         RoomMemberDao.getInstance().updateRoomMemberRole(mucRoom.getId(), mLoginUserId, role);

                                         if (role == 1 || role == 2) {// 群组或管理员 直接转发出去
                                             forwardingStep(friend);
                                         } else {
                                             if (mucRoom.getTalkTime() > 0) {// 全体禁言
                                                 DialogHelper.tip(InstantMessageActivity.this, getString(R.string.tip_now_ban_all));
                                             } else if (mucRoom.getMember().getTalkTime() > System.currentTimeMillis() / 1000) {// 禁言
                                                 DialogHelper.tip(InstantMessageActivity.this, getString(R.string.tip_forward_ban));
                                             } else {
                                                 forwardingStep(friend);
                                             }
                                         }
                                     }
                                 } else {// 群组已解散
                                     FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, friend.getUserId(), 2);// 更新本地群组状态
                                     DialogHelper.tip(InstantMessageActivity.this, getString(R.string.tip_forward_disbanded));
                                 }
                             }

                             @Override
                             public void onError(Call call, Exception e) {
                                 ToastUtil.showNetError(mContext);
                             }
                         }
                );
    }

    /**
     * 事件的监听
     */
    class ClickListener implements OnClickListener {
        private Friend friend;

        public ClickListener(Friend friend) {
            this.friend = friend;
        }

        @Override
        public void onClick(View v) {
            menuWindow.dismiss();
            switch (v.getId()) {
                case R.id.btn_send:// 发送
                    if (friend.getRoomFlag() != 0) {// 群组，调接口判断一些群属性状态
                        isSupportForwarded(friend);
                        return;
                    }
                    forwardingStep(friend);
                    break;
                case R.id.btn_cancle:
                    break;
                default:
                    break;
            }
        }
    }

    class MessageRecentlyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (friends != null) {
                return friends.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (friends != null) {
                return friends.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            if (friends != null) {
                return position;
            }
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(InstantMessageActivity.this, R.layout.item_recently_contacts, null);
                holder = new ViewHolder();
                holder.mIvHead = (MessageAvatar) convertView.findViewById(R.id.iv_recently_contacts_head);
                holder.mTvName = (TextView) convertView.findViewById(R.id.tv_recently_contacts_name);
                holder.mCheckBox = convertView.findViewById(R.id.cb_box);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Friend friend = friends.get(position);
            holder.mIvHead.fillData(friend);
            holder.mTvName.setText(TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName());
            holder.mCheckBox.setChecked(friend.isSelect());
            return convertView;
        }
    }

    class ViewHolder {
        MessageAvatar mIvHead;
        TextView mTvName;
        CheckBox mCheckBox;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SELECT_NEW_C:
                    if (data != null) {
                        String[] friendIds = data.getStringArrayExtra("friendIds");
                        this.mFriendIds = friendIds;
                        String[] groupIds = data.getStringArrayExtra("groupIds");
                        this.mGroupIds = groupIds;
                        Log.i(TAG, "现在选择好友的：" + JSON.toJSONString(mFriendIds));
                        Log.i(TAG, "现在选择群组的：" + JSON.toJSONString(mGroupIds));
                    }
                    break;
            }
        }
    }

    public interface GroupInfoListener {
        /**
         * 群信息的回调
         *
         * @param isSend  是否能将这条消息转发给 此群组
         * @param friend  此群组
         * @param message 不能转发的原因
         */
        void isSend(boolean isSend, Friend friend, String message);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        DialogHelper.dismissProgressDialog();
        forceInterruptThread();
    }

    private void forceInterruptThread() {
        try {
            if (mGroupHandleThread != null) {
                if (mGroupHandleThread.isAlive()) {
                    mGroupHandleThread.interrupt();
                    mGroupHandleThread = null;
                }
            }
            if (mSingleFriendHandleThread != null) {
                if (mSingleFriendHandleThread.isAlive()) {
                    mSingleFriendHandleThread.interrupt();
                    mSingleFriendHandleThread = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
