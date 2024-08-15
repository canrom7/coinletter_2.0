package com.iimm.miliao.course;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.iimm.miliao.AppConfig;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.adapter.MessageSendChat;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.bean.message.CourseChatBean;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.PrivacySettingHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.AppUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DES;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.SkinUtils;
import com.iimm.miliao.util.ThreadManager;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.util.permission.PermissionDialog;
import com.iimm.miliao.view.mucChatHolder.AChatHolderInterface;
import com.iimm.miliao.view.mucChatHolder.MucChatContentView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * 我的课件
 *
 * @author Dean Tao
 * @version 1.0
 */
public class CourseDateilsActivity extends BaseActivity {
    boolean isRun;
    int mPos;
    private String title;
    private String courseId;
    private MucChatContentView mChatContentView;
    private SmartRefreshLayout refresh;
    private List<ChatMessage> mChatMessages;
    private String toUserId;
    private String mLoginUserId;
    private int isReadDel;
    private boolean isGroup;
    private SuspenionWondow windowManager;
    private TextView mTvSuspen;
    private boolean isEncrypt;
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            if (msg.what == -1) {
                // loadingDialog.dismiss();
                Constants.IS_SENDONG_COURSE_NOW = false;// 课程发送完成，修改发送状态
                hideSuspensionWondow();
                ToastUtil.showToast(CourseDateilsActivity.this, getString(R.string.tip_course_send_success));
                // 更新消息界面
                MsgBroadcast.broadcastMsgUiUpdate(CourseDateilsActivity.this);
                return;
            }
            mTvSuspen.setText(getString(R.string.sending_message_index_place_holder, msg.what + 1));

            ChatMessage chatMessage = mChatMessages.get(msg.what);
            // 数据解密
            if (chatMessage.getIsEncrypt() == 1) { // == 1
                try {
                    String decryptKey = Md5Util.toMD5(AppConfig.apiKey + chatMessage.getTimeSend() + chatMessage.getPacketId());
                    String decryptContent = DES.decryptDES(chatMessage.getContent(), decryptKey);
                    // 为chatMessage重新设值
                    chatMessage.setContent(decryptContent);
                    chatMessage.setIsEncrypt(0);
                } catch (Exception e) {
                    Reporter.unreachable(e);
                }
            }
            // 重新加密，
            if (isEncrypt) {
                chatMessage.setIsEncrypt(1);
            } else {
                chatMessage.setIsEncrypt(0);
            }
            chatMessage.setFromUserId(mLoginUserId);
            chatMessage.setToUserId(toUserId);
            chatMessage.setIsReadDel(isReadDel);
            chatMessage.setMySend(true);
            chatMessage.setPacketId(ToolUtils.getUUID());
            chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());

            ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, toUserId, chatMessage);
            MessageSendChat chat = new MessageSendChat(isGroup, toUserId, chatMessage);
            EventBus.getDefault().post(chat);
        }
    };
    Runnable sendMessageTask = new Runnable() {
        @Override
        public void run() {
            while (isRun) {
                mHandler.sendEmptyMessage(mPos);
                mPos++;
                if (mPos == mChatMessages.size()) {
                    // 最后一条已发送完成
                    isRun = false;
                    SystemClock.sleep(400);
                    mHandler.sendEmptyMessage(-1);
                } else {
                    ChatMessage message = mChatMessages.get(mPos);
                    long sleepTime = 1000;
/*
                    if (message.getType() == Constants.TYPE_TEXT) {
                        sleepTime = message.getContent().length() * 500 + sleepTime;
                    } else if (message.getType() == Constants.TYPE_VOICE) {
                        sleepTime = message.getTimeLen() * 1000 + sleepTime;
                    }
*/
                    SystemClock.sleep(sleepTime);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_course_dateils);
        courseId = getIntent().getStringExtra("data");
        title = getIntent().getStringExtra("title");
        mChatMessages = new ArrayList<>();
        mLoginUserId = coreManager.getSelf().getUserId();
        initView();

        isEncrypt = PrivacySettingHelper.getPrivacySettings(this).getIsEncrypt() == 1;
    }

    private void initView() {
        initActionBar();
        refresh = (SmartRefreshLayout) findViewById(R.id.refresh);
        refresh.setRefreshHeader(new ClassicsHeader(this).setEnableLastTime(false));
        mChatContentView = (MucChatContentView) findViewById(R.id.chat_content_view);
        mChatContentView.setToUserId("123");
        mChatContentView.setMessageEventListener(new MucChatContentView.MessageEventListener() {

            @Override
            public void onEmptyTouch() {

            }

            @Override
            public void onTipMessageClick(ChatMessage message) {

            }

            @Override
            public void onMyAvatarClick() {

            }

            @Override
            public void onFriendAvatarClick(String friendUserId) {

            }

            @Override
            public void onMessageClick(ChatMessage chatMessage) {
                // 删除
                deleteMessage(chatMessage);
            }

            @Override
            public void onMessageLongClick(ChatMessage chatMessage) {

            }

            @Override
            public void onSendAgain(ChatMessage chatMessage) {

            }

            @Override
            public void onMessageBack(ChatMessage chatMessage, int position) {

            }

            @Override
            public void onNickNameClick(String friendUserId) {

            }

            @Override
            public void onCallListener(int type) {

            }

            @Override
            public void onChangeReadDelTimeClick(View v, AChatHolderInterface holder, ChatMessage message) {

            }

            @Override
            public void LongAvatarClick(ChatMessage chatMessage) {

            }
        });

        mChatContentView.setChatListType(MucChatContentView.ChatListType.COURSE);
        findViewById(R.id.sure_btn).setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());
        findViewById(R.id.sure_btn).setOnClickListener(v -> {
            if (mChatMessages == null || mChatMessages.size() == 0) {
                Toast.makeText(CourseDateilsActivity.this, "讲课获取失败", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(mContext, SelectFriendsActivity.class);
            startActivityForResult(intent, 1);
        });
        loadData();
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
        tvTitle.setText(title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            toUserId = data.getStringExtra("toUserId");
            isReadDel = PreferenceUtils.getInt(mContext, Constants.MESSAGE_READ_FIRE + toUserId + mLoginUserId, 0);
            isGroup = data.getBooleanExtra("isGroup", false);
            Log.e(TAG, "onActivityResult: " + toUserId + ",   group: " + isGroup);
            sendRecordMessage();
        }
    }

    /**
     * 隐藏悬浮窗
     */
    private void hideSuspensionWondow() {
        windowManager.hide();
    }

    /**
     * 显示悬浮窗
     */
    private void showSuspensionWondow() {
        mTvSuspen = new TextView(this);
        mTvSuspen.setGravity(Gravity.CENTER);
        mTvSuspen.setBackgroundResource(R.drawable.course_connors);
        mTvSuspen.setTextAppearance(this, R.style.TextStyle);
        mTvSuspen.setText(R.string.sending_course);
        windowManager = new SuspenionWondow(CourseDateilsActivity.this);
        windowManager.show(mTvSuspen);
    }

    /**
     * 发送课程
     */
    private void sendRecordMessage() {
        if (AppUtils.checkAlertWindowsPermission(this)) { // 已开启悬浮窗权限
            showSuspensionWondow();
            mPos = 0;
            isRun = true;
            ThreadManager.getPool().execute(sendMessageTask);
        } else {
            Constants.IS_SENDONG_COURSE_NOW = false;
            PermissionDialog.show(this, null, getString(R.string.av_no_float), null, null);
        }
    }

    /**
     * 从课件里删除某条消息
     *
     * @param chatMessage 要删除的消息
     */
    private void deleteMessage(final ChatMessage chatMessage) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("courseMessageId", chatMessage.getPacketId());
        params.put("courseId", courseId);
        params.put("updateTime", TimeUtils.time_current_time() + "");
        HttpUtils.get().url(coreManager.getConfig().USER_EDIT_COURSE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(CourseDateilsActivity.this, getString(R.string.delete_success));
                        deleteAdapterData(chatMessage);
                        mChatContentView.setData(mChatMessages);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(CourseDateilsActivity.this);
                    }
                });
    }

    private void deleteAdapterData(ChatMessage chatMessage) {
        for (int i = 0; i < mChatMessages.size(); i++) {
            ChatMessage chat = mChatMessages.get(i);
            if (chatMessage.getPacketId().equals(chat.getPacketId())) {
                mChatMessages.remove(i);
                return;
            }
        }
    }

    /**
     * 加载数据
     */
    private void loadData() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("courseId", courseId);
        HttpUtils.get().url(coreManager.getConfig().USER_COURSE_DATAILS)
                .params(params)
                .build()
                .execute(new ListCallback<CourseChatBean>(CourseChatBean.class) {
                    @Override
                    public void onResponse(ArrayResult<CourseChatBean> result) {
                        DialogHelper.dismissProgressDialog();
                        fromatDatas(result.getData());
                        mChatContentView.setData(mChatMessages);
                        refresh.finishRefresh();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(CourseDateilsActivity.this);
                        refresh.finishRefresh();
                    }
                });
    }

    private void fromatDatas(List<CourseChatBean> result) {
        mChatMessages.clear();
        for (int i = 0; i < result.size(); i++) {
            try {
                CourseChatBean data = result.get(i);
                String messageBody = data.getMessage();
                org.json.JSONObject json = new org.json.JSONObject(messageBody);
                String body = json.getString("body");
                body = Html.fromHtml(body).toString();
                String packedId = data.getCourseMessageId();
                Log.e(TAG, "fromatDatas: " + body);
                ChatMessage chatMessage = new ChatMessage(body);
                chatMessage.setMySend(true);
                chatMessage.setUpload(true);
                chatMessage.setUploadSchedule(100);
                chatMessage.setMessageState(Constants.MESSAGE_SEND_SUCCESS);
                mChatMessages.add(chatMessage);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
