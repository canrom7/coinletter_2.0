package com.iimm.miliao.view.mucChatHolder;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iimm.miliao.AppConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.audio_x.VoiceManager;
import com.iimm.miliao.audio_x.VoicePlayer;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.RoomMember;
import com.iimm.miliao.bean.User;
import com.iimm.miliao.bean.collection.CollectionEvery;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.broadcast.OtherBroadcast;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.RoomMemberDao;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.message.ChatActivity;
import com.iimm.miliao.ui.message.EventMoreSelected;
import com.iimm.miliao.ui.message.InstantMessageActivity;
import com.iimm.miliao.ui.message.multi.RoomReadListActivity;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.BitmapUtil;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DES;
import com.iimm.miliao.util.FileUtil;
import com.iimm.miliao.util.HtmlUtils;
import com.iimm.miliao.util.LogUtils;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.StringUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.view.ChatBottomView;
import com.iimm.miliao.view.ChatTextClickPpWindow;
import com.iimm.miliao.view.SelectionFrame;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

import static com.iimm.miliao.MyApplication.applicationHandler;

public class MucChatContentView extends RecyclerView implements ChatBottomView.MoreSelectMenuListener {
    private static String TAG = "MucChatContentView";
    // 好友备注只有群组才有
    public Map<String, Bitmap> mCacheMap = new HashMap<>();
    private boolean isGroupChat;// 用于标记聊天界面是否为群聊界面
    private boolean isShowReadPerson;// 是否展示群已读人数
    private boolean isShowMoreSelect;// 用于标记是否显示多选框
    private boolean isScrollBottom;// 是否正在底部，如果是，来新消息时需要跳到底部，
    private int mGroupLevel = 3;// 我在当前群组的职位，用于控制群组消息能否撤回(default==3普通成员)
    private int mCurClickPos = -1;// 当前点击的position
    private String mRoomNickName; // 我在房间的昵称，只有群聊才有
    private String mToUserId;// 根据self.userId和mToUserId 唯一确定一张表
    private String mRoomId;
    private User mLoginUser;// 当前登录的用户
    private Context mContext; // 界面上下文
    private ChatListType mCurChatType; // 标记当前处于什么界面
    private LayoutInflater mInflater;// 布局填充器
    private ChatBottomView mChatBottomView; // 聊天输入框控件
    private AutoVoiceModule aVoice; // 自动播放下一条未读语音消息的模块
    private ChatContentAdapter mChatContentAdapter;// 消息适配器
    private MessageEventListener mMessageEventListener; // 消息体点击监听
    private ChatTextClickPpWindow mChatPpWindow; // 长按选择框
    // 当前适配器数据源
    private List<ChatMessage> mChatMessages;
    // 即将要删除的消息的packedId列表
    private Set<String> mDeletedChatMessageId = new HashSet<>();
    // 阅后即焚 正在倒计时缓存的时间列表
    private Map<String, CountDownTimer> mTextBurningMaps = new HashMap<>();
    // 所有阅后即焚的语音消息列表
    private Map<String, String> mFireVoiceMaps = new HashMap<>();
    // 好友备注只有群组才有
    private Map<String, String> mRemarksMap = new HashMap<>();
    // 群管理头像
    private Map<String, Integer> memberMap = new HashMap<>();
    // 群成员vip 等级 值
    private Map<String, Integer> memberVipMap = new HashMap<>();
    // 滚动到底部
    private Runnable mScrollTask = new Runnable() {
        @Override
        public void run() {
            if (mChatMessages == null) {
                return;
            }
            smoothMoveToPosition(MucChatContentView.this, mChatMessages.size() - 1, true);
        }
    };
    private Collection<OnScrollListener> onScrollListenerList = new ArrayList<>();
    private boolean secret;
    private OnMenuClickListener menuClickListener;

    public MucChatContentView(Context context) {
        this(context, null);
    }

    public MucChatContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    // 输入法弹起时让界面跟着上去
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (oldh > h) {
            removeCallbacks(mScrollTask);
            //  int delay = getResources().getInteger(android.R.integer.config_shortAnimTime); // 200
            postDelayed(mScrollTask, 150);

        }
    }

    private void init(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        ReadDelManager.getInstants().setListener(new ReadDelManager.ReadDelListener() {
            @Override
            public void downOver(ReadDelManager.ReadDelInfo info) {
                removeItemMessage(info.getChatMessage());
            }
        });

        mLoginUser = CoreManager.requireSelf(context);
        mRoomNickName = mLoginUser.getNickName();
        aVoice = new AutoVoiceModule();
        VoicePlayer.instance().addVoicePlayListener(new VoicePlayListener());
        //setItemViewCacheSize(20);
        //setDrawingCacheEnabled(true);
        //setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                for (OnScrollListener listener : onScrollListenerList) {
                    listener.onScrollStateChanged(recyclerView, newState);
                }

               /* if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Glide.with(mContext).resumeRequests();//恢复Glide加载图片
                }else {
                    Glide.with(mContext).pauseRequests();//禁止Glide加载图片
                }*/
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!canScrollVertically(1)) {//1的值表示是否能向上滚动，false表示已经滚动到底部
                    isScrollBottom = true;
                } else {
                    isScrollBottom = false;
                }
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(context);
        setLayoutManager(manager);
        mChatContentAdapter = new ChatContentAdapter();
        setAdapter(mChatContentAdapter);
    }

    // 返回当前是否在底部
    public boolean shouldScrollToBottom() {
        return isScrollBottom;
    }

    // 点击空位置收起输入法软键盘
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mMessageEventListener != null) {
                mMessageEventListener.onEmptyTouch();
            }
        }
        return super.onTouchEvent(event);
    }

    // 设置对方的userid
    public void setToUserId(String toUserId) {
        mToUserId = toUserId;
    }

    // 设置对方的userid
    public void setRoomId(String roomid) {
        mRoomId = roomid;
    }

    // 修改为多选
    public void setIsShowMoreSelect(boolean isShowMoreSelect) {
        this.isShowMoreSelect = isShowMoreSelect;
    }

    // 设置当前是否是群组，群组中显示的昵称
    public void setCurGroup(boolean group, String nickName) {
        isGroupChat = group;
        if (!TextUtils.isEmpty(nickName)) {
            mRoomNickName = nickName;
        }
        if (mRemarksMap.size() == 0) {
            AsyncUtils.doAsync(mContext, contextAsyncContext -> {
                List<Friend> friendList = FriendDao.getInstance().getAllFriends(mLoginUser.getUserId());
                for (int i = 0; i < friendList.size(); i++) {
                    if (!TextUtils.isEmpty(friendList.get(i).getRemarkName())) {
                        mRemarksMap.put(friendList.get(i).getUserId(), friendList.get(i).getRemarkName());
                    }
                }
            });
        }
    }

    public boolean isGroupChat() {
        return isGroupChat;
    }

    // 设置当前用户在群组的的权限
    public void setRole(int role) {
        this.mGroupLevel = role;
    }

    public void setChatBottomView(ChatBottomView chatBottomView) {
        this.mChatBottomView = chatBottomView;
        if (mChatBottomView != null) {
            mChatBottomView.setMoreSelectMenuListener(this);
        }
    }

    public void setData(List<ChatMessage> chatMessages) {
        mChatMessages = chatMessages;
        if (mChatMessages == null) {
            mChatMessages = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    // 设置管理员头像控件
    public void setRoomMemberList(List<RoomMember> memberList) {
        memberMap.clear();
        memberVipMap.clear();
        for (RoomMember member : memberList) {
            memberMap.put(member.getUserId(), member.getRole());
            memberVipMap.put(member.getUserId(), member.getVipLevel());
        }
        notifyDataSetChanged();
    }

    //vip 等级
    public void setAllMemberList(List<RoomMember> memberList) {
        memberVipMap.clear();
        for (RoomMember member : memberList) {
            memberVipMap.put(member.getUserId(), member.getVipLevel());
        }
        notifyDataSetChanged();
    }

    // 使用动画删除某一条消息
    long lastUpdateTime = 0; //上次播放 删除动画的时间   坐下处理 否则发的快的时候  一直在更新列表

    public void removeItemMessage(final ChatMessage chatMessage) {
        mChatMessages.remove(chatMessage);
        mChatContentAdapter.notifyDataSetChanged();

//        mDeletedChatMessageId.add(packedId);
//        if (System.currentTimeMillis() - lastUpdateTime < 300) {
//            return;
//        }
//        lastUpdateTime = System.currentTimeMillis();
//        mChatContentAdapter.notifyDataSetChanged();
    }

    // 界面更新
    public void notifyDataSetInvalidated(final int position) {
        if (mChatMessages.size() > position) {
            this.post(new Runnable() {
                @Override
                public void run() {
                    smoothMoveToPosition(MucChatContentView.this, position, false);
                }
            });
        }
    }

    public void notifyDataSetInvalidated(boolean scrollToBottom) {
        if (mChatMessages != null && mChatMessages.size() > 0) {
            if (mChatContentAdapter != null) {
                mChatContentAdapter.notifyDataSetChanged();
            }
            if (scrollToBottom) {
                smoothMoveToPosition(MucChatContentView.this, mChatMessages.size() - 1, true);
            }
        }
    }

    public void notifyItemChanged(int postion) {
        if (mChatContentAdapter != null) {
            mChatContentAdapter.notifyItemChanged(postion);
        }
    }

    public void notifyItemRemoved(int postion) {
        if (mChatContentAdapter != null) {
            mChatContentAdapter.notifyItemRemoved(postion);
            mChatContentAdapter.notifyItemRangeChanged(postion, (mChatMessages.size() - postion - 1));
        }
    }

    /**
     * 有新消息了并且是在最底部
     *
     * @param postion 最新的消息
     */
    public void notifyItemInserted(int postion) {
        if (mChatContentAdapter != null) {
            mChatContentAdapter.notifyItemInserted(postion);

            if (!canScrollVertically(1)) {
                smoothMoveToPosition(MucChatContentView.this, postion, true);
            }

        }
    }

    /**
     * @param start 起始位置，插入数据的位置
     * @param count 插入的个数
     */
    public void notifyItemInserted(int start, int count) {
        if (mChatContentAdapter != null) {
            mChatContentAdapter.notifyItemRangeInserted(start, count);
        }
    }

    public void notifyDataSetChanged() {
        applicationHandler.post(() -> {
            if (mChatContentAdapter != null) {
                mChatContentAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 滑动到指定位置
     */
    public void smoothMoveToPosition(RecyclerView mRecyclerView, final int position, boolean isScrollBottom) {
        if (position != -1) {
            mRecyclerView.scrollToPosition(position);
            LinearLayoutManager mLayoutManager =
                    (LinearLayoutManager) mRecyclerView.getLayoutManager();

            if (isScrollBottom) {
                int lastItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1));
                if (position <= lastItem) {
                    int firstItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(0));
                    int movePosition = position - firstItem;
                    if (movePosition >= 0 && movePosition < mRecyclerView.getChildCount()) {
                        int top = mRecyclerView.getChildAt(movePosition).getBottom();
                        mRecyclerView.smoothScrollBy(0, top);
                    } else {
                        mLayoutManager.scrollToPositionWithOffset(position, -getBottom());
                    }
                } else {
                    mRecyclerView.post(() -> {
                        int firstItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(0));
                        int movePosition = position - firstItem;
                        if (movePosition >= 0 && movePosition < mRecyclerView.getChildCount()) {
                            int top = mRecyclerView.getChildAt(movePosition).getBottom();
                            mRecyclerView.smoothScrollBy(0, top);
                        } else {
                            mLayoutManager.scrollToPositionWithOffset(position, -getBottom());
                        }
                    });
                }
            } else {
                mLayoutManager.scrollToPositionWithOffset(position, 0);
            }
        }
    }

    // 设置消息解监听
    public void setMessageEventListener(MessageEventListener listener) {
        mMessageEventListener = listener;
    }

    // 指定当前界面是在哪 直播 还是 课程
    public void setChatListType(ChatListType type) {
        mCurChatType = type;
    }

    // 一条消息的删除动画
    private void startRemoveAnim(View view, ChatMessage message, int position) {
        //  mChatMessages.remove(message);
        //mChatContentAdapter.notifyItemRemoved(position);
//        mChatContentAdapter.notifyItemRangeChanged(position,mChatMessages.size()-1);
//
//
//
//        Animation anim = new Animation() {
//            @Override
//            protected void applyTransformation(float interpolatedTime, Transformation t) {
//                view.setAlpha(1f - interpolatedTime);
//            }
//        };
//
//        anim.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                mChatMessages.remove(message);
//                mDeletedChatMessageId.remove(message);
//                view.clearAnimation();
//                if (mChatContentAdapter != null) {
//                    mChatContentAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//
//        anim.setDuration(300);
//        view.startAnimation(anim);
    }

    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ 消息业务具体实现 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓多选业务具体实现 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    // 判断 是否 一条消息也没有选择
    private boolean isNullSelectMore(List<ChatMessage> list) {
        if (list == null || list.size() == 0) {
            return true;
        }

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isMoreSelected) {
                return false;
            }
        }
        return true;
    }

    // 点击了 多选转发按钮
    @Override
    public void clickForwardMenu() {
        final Dialog mForwardDialog = new Dialog(mContext, R.style.BottomDialog);
        View contentView = mInflater.inflate(R.layout.forward_dialog, null);
        mForwardDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels;
        contentView.setLayoutParams(layoutParams);
        mForwardDialog.setCanceledOnTouchOutside(true);
        mForwardDialog.getWindow().setGravity(Gravity.BOTTOM);
        mForwardDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        mForwardDialog.show();
        mForwardDialog.findViewById(R.id.single_forward).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {// 逐条转发
                mForwardDialog.dismiss();
                if (isNullSelectMore(mChatMessages)) {
                    ToastUtil.showToast(mContext, mContext.getString(R.string.name_connot_null));
                    return;
                }
                // 跳转至转发页面
                ArrayList<ChatMessage> selectMessage = getSelectedMessage();
                Intent intent = new Intent(mContext, InstantMessageActivity.class);
                intent.putExtra("SelectMessageList", selectMessage);
                intent.putExtra(Constants.IS_MORE_SELECTED_INSTANT, true);
                mContext.startActivity(intent);
                if (null != menuClickListener) {
                    menuClickListener.clickForwardMenu(view);
                }
            }
        });
        mForwardDialog.findViewById(R.id.sum_forward).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {// 合并转发
                mForwardDialog.dismiss();
                if (isNullSelectMore(mChatMessages)) {
                    ToastUtil.showToast(mContext, mContext.getString(R.string.name_connot_null));
                    return;
                }
                // 跳转至转发页面
                ArrayList<ChatMessage> selectMessage = getSelectedMessage();
                Intent intent = new Intent(mContext, InstantMessageActivity.class);
                intent.putExtra("SelectMessageList", selectMessage);
                intent.putExtra(Constants.IS_MORE_SELECTED_INSTANT, true);
                intent.putExtra(Constants.IS_SINGLE_OR_MERGE, true);
                mContext.startActivity(intent);
                if (null != menuClickListener) {
                    menuClickListener.clickForwardMenu(view);
                }
            }
        });
        mForwardDialog.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mForwardDialog.dismiss();
            }
        });
    }

    /**
     * 获取当前选中的Message
     *
     * @return
     */
    private ArrayList<ChatMessage> getSelectedMessage() {
        ArrayList<ChatMessage> selectMessage = new ArrayList<>();
        for (ChatMessage message : mChatMessages) {
            if (message.isMoreSelected) {
                selectMessage.add(message);
            }
        }
        return selectMessage;
    }

    // 点击了多选收藏按钮
    @Override
    public void clickCollectionMenu() {
        if (isNullSelectMore(mChatMessages)) {
            ToastUtil.showToast(mContext, mContext.getString(R.string.name_connot_null));
            return;
        }
        SelectionFrame selectionFrame = new SelectionFrame(mContext);
        selectionFrame.setSomething(null, "选中消息中，文字 / 图片 / 语音 / 视频 / 文件 消息才能被收藏", "取消", "收藏",
                new SelectionFrame.OnSelectionFrameClickListener() {
                    @Override
                    public void cancelClick() {

                    }

                    @Override
                    public void confirmClick() {
                        List<ChatMessage> temp = new ArrayList<>();
                        for (int i = 0; i < mChatMessages.size(); i++) {
                            if (mChatMessages.get(i).isMoreSelected
                                    && (mChatMessages.get(i).getType() == Constants.TYPE_TEXT
                                    || mChatMessages.get(i).getType() == Constants.TYPE_IMAGE
                                    || mChatMessages.get(i).getType() == Constants.TYPE_VOICE
                                    || mChatMessages.get(i).getType() == Constants.TYPE_VIDEO
                                    || mChatMessages.get(i).getType() == Constants.TYPE_FILE)) {
                                temp.add(mChatMessages.get(i));
                            }
                        }
                        moreSelectedCollection(temp);
                        // 发送EventBus，通知聊天页面解除多选状态
                        EventBus.getDefault().post(new EventMoreSelected("MoreSelectedCollection", false, isGroupChat()));
                    }
                });
        selectionFrame.show();
    }

    // 点击了多选删除按钮
    @Override
    public void clickDeleteMenu() {
        if (isNullSelectMore(mChatMessages)) {
            ToastUtil.showToast(mContext, mContext.getString(R.string.name_connot_null));
            return;
        }
        final Dialog mDeleteDialog = new Dialog(mContext, R.style.BottomDialog);
        View contentView = mInflater.inflate(R.layout.delete_dialog, null);
        mDeleteDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels;
        contentView.setLayoutParams(layoutParams);
        mDeleteDialog.setCanceledOnTouchOutside(true);
        mDeleteDialog.getWindow().setGravity(Gravity.BOTTOM);
        mDeleteDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        mDeleteDialog.show();
        mDeleteDialog.findViewById(R.id.delete_message).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mDeleteDialog.dismiss();
                EventBus.getDefault().post(new EventMoreSelected("MoreSelectedDelete", false, isGroupChat()));
            }
        });

        mDeleteDialog.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mDeleteDialog.dismiss();
            }
        });
    }

    // 点击了 更多按钮
    @Override
    public void clickEmailMenu() {
        if (isNullSelectMore(mChatMessages)) {
            ToastUtil.showToast(mContext, mContext.getString(R.string.name_connot_null));
            return;
        }
        final Dialog mEmailDialog = new Dialog(mContext, R.style.BottomDialog);
        View contentView = mInflater.inflate(R.layout.email_dialog, null);
        mEmailDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels;
        contentView.setLayoutParams(layoutParams);
        mEmailDialog.setCanceledOnTouchOutside(true);
        mEmailDialog.getWindow().setGravity(Gravity.BOTTOM);
        mEmailDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        mEmailDialog.show();
        mEmailDialog.findViewById(R.id.save_message).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mEmailDialog.dismiss();
                SelectionFrame selectionFrame = new SelectionFrame(mContext);
                selectionFrame.setSomething(null, getContext().getString(R.string.save_only_image), getContext().getString(R.string.cancel), getContext().getString(R.string.save),
                        new SelectionFrame.OnSelectionFrameClickListener() {
                            @Override
                            public void cancelClick() {

                            }

                            @Override
                            public void confirmClick() {
                                for (int i = 0; i < mChatMessages.size(); i++) {
                                    if (mChatMessages.get(i).isMoreSelected && mChatMessages.get(i).getType() == Constants.TYPE_IMAGE) {
                                        FileUtil.downImageToGallery(mContext, mChatMessages.get(i).getContent());
                                    }
                                }
                                EventBus.getDefault().post(new EventMoreSelected("MoreSelectedEmail", false, isGroupChat()));
                            }
                        });
                selectionFrame.show();
            }
        });

        mEmailDialog.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mEmailDialog.dismiss();
            }
        });
    }

    /**
     * 收藏和存表情共用一个接口，参数也基本相同，
     *
     * @param flag 是收藏就为true, 是存表情就为false,
     */
    private String collectionParam(List<ChatMessage> messageList, boolean flag) {
        JSONArray array = new JSONArray();
        for (ChatMessage message : messageList) {
            int type = 6;
            if (flag) {// 收藏
                if (message.getType() == Constants.TYPE_IMAGE) {
                    type = CollectionEvery.TYPE_IMAGE;
                } else if (message.getType() == Constants.TYPE_VIDEO) {
                    type = CollectionEvery.TYPE_VIDEO;
                } else if (message.getType() == Constants.TYPE_FILE) {
                    type = CollectionEvery.TYPE_FILE;
                } else if (message.getType() == Constants.TYPE_VOICE) {
                    type = CollectionEvery.TYPE_VOICE;
                } else if (message.getType() == Constants.TYPE_TEXT) {
                    type = CollectionEvery.TYPE_TEXT;
                }
            }
            JSONObject json = new JSONObject();
            json.put("type", String.valueOf(type));
            json.put("msg", message.getContent());
            if (flag) {
                // 收藏消息id
                json.put("msgId", message.getPacketId());
                if (isGroupChat()) {
                    // 群组收藏需要添加jid
                    json.put("roomJid", mToUserId);
                }
            } else {
                // 表情url
                json.put("url", message.getContent());
            }
            array.add(json);
        }
        return JSON.toJSONString(array);
    }

    /**
     * 添加为表情 && 收藏
     * 添加为表情Type 6.表情
     * 收藏Type    1.图片 2.视频 3.文件 4.语音 5.文本
     */
    public void collectionEmotion(ChatMessage message, final boolean flag) {
        if (TextUtils.isEmpty(message.getContent())) {
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(getContext()).accessToken);
        params.put("emoji", collectionParam(Collections.singletonList(message), flag));

        HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getInstance()).Collection_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            ToastUtil.showToast(mContext, InternationalizationHelper.getString("JX_CollectionSuccess"));
                            if (!flag) { // 添加为表情
                                // 收藏成功后将对应的url存入内存中，以防下次再次收藏该链接
                                // PreferenceUtils.putInt(mContext, self.getUserId() + message.getContent(), 1);
                                // 发送广播更新收藏列表
                                MyApplication.getInstance().sendBroadcast(new Intent(OtherBroadcast.CollectionRefresh));
                            }
                        } else if (!TextUtils.isEmpty(result.getResultMsg())) {
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        } else {
                            ToastUtil.showToast(mContext, R.string.tip_server_error);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(mContext);
                    }
                });
    }

    /**
     * 多选 收藏
     */
    public void moreSelectedCollection(List<ChatMessage> chatMessageList) {
        if (chatMessageList == null || chatMessageList.size() <= 0) {
            ToastUtil.showToast(mContext, mContext.getString(R.string.name_connot_null));
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(getContext()).accessToken);
        params.put("emoji", collectionParam(chatMessageList, true));

        HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getInstance()).Collection_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            ToastUtil.showToast(mContext, InternationalizationHelper.getString("JX_CollectionSuccess"));
                        } else if (!TextUtils.isEmpty(result.getResultMsg())) {
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        } else {
                            ToastUtil.showToast(mContext, R.string.tip_server_error);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(mContext);
                    }
                });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        for (Map.Entry<String, Bitmap> entry : mCacheMap.entrySet()) {
            Bitmap bitmap = entry.getValue();
            bitmap.recycle();
            bitmap = null;
        }
        VoicePlayer.instance().addVoicePlayListener(null);
        mCacheMap.clear();
        System.gc();
    }

    public void setRecyclerViewOnScrollListener(OnScrollListener onScrollListener) {
        onScrollListenerList.add(onScrollListener);
    }

    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    public void setOnMenuClickListener(OnMenuClickListener listener) {
        this.menuClickListener = listener;
    }

    public interface OnMenuClickListener {

        void clickForwardMenu(View view);
    }

    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ 实体类 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    public enum ChatListType {
        // 单聊 直播 课程 设备
        SINGLE, LIVE, COURSE, DEVICE
    }

    // 适配器接口
    public interface MessageEventListener {
        // 点击空白处，让输入框归位
        void onEmptyTouch();

        void onTipMessageClick(ChatMessage message);

        /**
         * @param message 这是点击的消息，不是被回复的消息，
         */
        default void onReplayClick(ChatMessage message) {
        }

        void onMyAvatarClick();

        void onFriendAvatarClick(String friendUserId);

        void LongAvatarClick(ChatMessage chatMessage);

        void onNickNameClick(String friendUserId);

        void onMessageClick(ChatMessage chatMessage);

        void onMessageLongClick(ChatMessage chatMessage);

        void onSendAgain(ChatMessage chatMessage);

        void onMessageBack(ChatMessage chatMessage, int position);

        default void onMessageReplay(ChatMessage chatMessage) {
        }

        void onCallListener(int type);

        void onChangeReadDelTimeClick(View v, AChatHolderInterface holder, ChatMessage message);
    }

    // 消息适配器
    public class ChatContentAdapter extends Adapter<AChatHolderInterface> implements ChatHolderListener {
        public int nowPostion;

        @Override
        public int getItemCount() {
            if (mChatMessages != null) {
                return mChatMessages.size();
            }
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            nowPostion = position;
            return getItemViewTypes(position);
        }

        @NonNull
        @Override
        public AChatHolderInterface onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            ChatHolderFactory.ChatHolderType holderType = ChatHolderFactory.ChatHolderType.valueOf(i);
            return createHolder(holderType, viewGroup);
        }

        @Override
        public void onViewAttachedToWindow(@NonNull AChatHolderInterface holder) {
            super.onViewAttachedToWindow(holder);
        }

//        @Override
//        public void onViewRecycled(@NonNull AChatHolderInterface holder) {
//            if (holder != null) {
//                if (holder instanceof ImageViewHolder) {
//                    ImageViewHolder viewHolder = (ImageViewHolder) holder;
//                    Glide.clear(viewHolder.mImageView);
//                }
//            }
//            super.onViewRecycled(holder);
//        }

        @Override
        public void onBindViewHolder(@NonNull AChatHolderInterface viewHolder, int i) {
            nowPostion = i;
            ChatMessage message = mChatMessages.get(i);
            AChatHolderInterface holder = (AChatHolderInterface) viewHolder;
            holder.chatMessages = mChatMessages;
            holder.selfGroupRole = mGroupLevel;
            holder.position = i;
            holder.setMultiple(isShowMoreSelect);
            isShowReadPerson = PreferenceUtils.getBoolean(mContext, Constants.IS_SHOW_READ + mToUserId, false);
            holder.setShowPerson(isShowReadPerson);

            // 显示时间
            changeTimeVisible(holder, message);
            // 设置备注，显示与否是在基类实现的
            changeNameRemark(holder, message);

            // 数据解密
            if (message.getIsEncrypt() == 1) { // == 1
                try {
                    String decryptKey = Md5Util.toMD5(AppConfig.apiKey + message.getTimeSend() + message.getPacketId());
                    String decryptContent = DES.decryptDES(message.getContent(), decryptKey);
                    // 为chatMessage重新设值
                    message.setContent(decryptContent);
                    message.setIsEncrypt(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            int vipLevel = memberVipMap.get(message.getFromUserId()) == null ? 0 : memberVipMap.get(message.getFromUserId());
            holder.prepare(message, memberMap.get(message.getFromUserId()), secret, vipLevel);

            if (holder.mHolderType == ChatHolderFactory.ChatHolderType.VIEW_TO_VOICE) {
                // 阅后即焚语音的处理
                if (!isGroupChat() && message.getIsReadDel()) {
                    if (!TextUtils.isEmpty(message.getFilePath()) && !mFireVoiceMaps.containsKey(message.getFilePath())) {
                        mFireVoiceMaps.put(message.getFilePath(), message.getPacketId());
                    }
                }

                // 自动播放语音的处理
                if (!message.isSendRead()) {
                    aVoice.put((VoiceViewHolder) holder);
                }
            }


//            if (mDeletedChatMessageId.contains(message.getPacketId())) {
//                startRemoveAnim(holder.itemView, message, i);
//            }

        }

        @Override
        public void onViewDetachedFromWindow(@NonNull AChatHolderInterface holder) {
            super.onViewDetachedFromWindow(holder);

        }


        @Override
        public boolean onFailedToRecycleView(@NonNull AChatHolderInterface holder) {
            return super.onFailedToRecycleView(holder);
        }

        public int getItemViewTypes(int position) {
            ChatMessage message = mChatMessages.get(position);
            // 正常情况的 mySend
            boolean mySend = message.isMySend() || mLoginUser.getUserId().equals(message.getFromUserId());
            // 兼容多点登陆
            if (message.getFromUserId().equals(message.getToUserId()) && !TextUtils.isEmpty(message.getFromId())) {
                if (message.getFromId().contains(MyApplication.MULTI_RESOURCE)) {
                    mySend = true;
                } else {
                    mySend = false;
                }
            }
            // 兼容直播间
            ChatHolderFactory.ChatHolderType holderType = ChatHolderFactory.getChatHolderType(mySend, message);
            if (mCurChatType == ChatListType.LIVE) {
                holderType = ChatHolderFactory.ChatHolderType.VIEW_SYSTEM_LIVE;
            }
            return holderType.ordinal();
        }

        public AChatHolderInterface createHolder(ChatHolderFactory.ChatHolderType holderType, ViewGroup parent) {
            AChatHolderInterface holder = ChatHolderFactory.getHolder(mContext, holderType, parent);

            holder.mContext = mContext;
            holder.mLoginUserId = mLoginUser.getUserId();
            holder.mLoginNickName = mRoomNickName;
            holder.mToUserId = mToUserId;
            holder.isGroup = isGroupChat();
            holder.mHolderType = holderType;
            isShowReadPerson = PreferenceUtils.getBoolean(mContext, Constants.IS_SHOW_READ + mToUserId, false);
            holder.setShowPerson(isShowReadPerson);
            holder.findView();
            if (holder instanceof VideoViewHolder) {
                if (holder.mIvFailed != null)
                    holder.mIvFailed.setVisibility(GONE);
            }
            holder.addChatHolderListener(this);
            return holder;
        }

        private void changeTimeVisible(AChatHolderInterface holder, ChatMessage message) {
            int position = holder.position;
            // 与上一条消息之间的间隔如果大于15分钟就显示本消息发送时间
            String timeStr = null;
            if (position >= 1) {
                long last = mChatMessages.get(position - 1).getTimeSend();
                if (message.getTimeSend() - last > 15 * 60) {// 小于15分钟，不显示
                    timeStr = TimeUtils.time_long_to_chat_time_str(message.getTimeSend());
                }
            }
            holder.showTime(timeStr);
        }

        private void changeNameRemark(AChatHolderInterface holder, ChatMessage message) {
            if (!isGroupChat || message.isMySend()) {
                return;
            }
            if (mGroupLevel == 1) {// 群主优先显示自己对群组成员的群内备注
                RoomMember member = RoomMemberDao.getInstance().getSingleRoomMember(mRoomId, message.getFromUserId());
                if (member != null
                        && !TextUtils.isEmpty(member.getCardName())
                        && !TextUtils.equals(member.getUserName(), member.getCardName())) {// 已有群内备注
                    String name = member.getCardName();
                    message.setFromUserName(name);
                    return;
                }
            }
            if (mRemarksMap.containsKey(message.getFromUserId())) {
                message.setFromUserName(mRemarksMap.get(message.getFromUserId()));
            }
        }

        public void clickRootItme(AChatHolderInterface holder, ChatMessage message) {
            mCurClickPos = holder.position;

            // 点击了一条阅后即焚的消息 非群组、阅后即焚类型、未发送已读 才可以点击
            if (!isGroupChat() && message.getIsReadDel() && !message.isSendRead()) {
                // 发送已读消息回执，对方收到后会删除该条阅后即焚消息
                if (holder.mHolderType == ChatHolderFactory.ChatHolderType.VIEW_TO_TEXT) {
                    // 通知chatactivity 等待播放完成后删除
                    // EventBus.getDefault().post(new MessageEventClickFire("delay", message.getPacketId()));
                    // 阅后即焚的文字处理
                    //clickFireText(holder, message);
                } else if (holder.mHolderType == ChatHolderFactory.ChatHolderType.VIEW_TO_VIDEO) {// 阅后即焚的视频处理
                    // 通知chatactivity 等待播放完成后删除
//                    EventBus.getDefault().post(new MessageEventClickFire("delay", message.getPacketId()));
                } else if (holder.mHolderType == ChatHolderFactory.ChatHolderType.VIEW_TO_IMAGE) {// 阅后即焚的图片处理
                    // 通知chatactivity 等待播放完成后删除
                    //   EventBus.getDefault().post(new MessageEventClickFire("delay", message.getPacketId()));
                } else if (holder.mHolderType == ChatHolderFactory.ChatHolderType.VIEW_TO_VOICE) {// 阅后即焚的声音处理
                    // 通知chatactivity 等待播放完成后删除
                    //EventBus.getDefault().post(new MessageEventClickFire("delay", message.getPacketId()));
                }
            }

            if (holder.mHolderType == ChatHolderFactory.ChatHolderType.VIEW_FROM_MEDIA_CALL || holder.mHolderType == ChatHolderFactory.ChatHolderType.VIEW_TO_MEDIA_CALL) {
                if (mMessageEventListener != null) {
                    mMessageEventListener.onCallListener(message.getType());
                }
                return;
            }

            holder.sendReadMessage(message);
        }

        @Override
        public void onCompDownVoice(ChatMessage message) {
            if (!isGroupChat() && message.getType() == Constants.TYPE_VOICE && !message.isMySend()) {
                if (message.getIsReadDel() && !TextUtils.isEmpty(message.getFilePath()) && !mFireVoiceMaps.containsKey(message.getFilePath())) {
                    mFireVoiceMaps.put(message.getFilePath(), message.getPacketId());
                }
            }
        }

        @Override
        public void onChangeInputText(String text) {
            if (mChatBottomView != null) {
                mChatBottomView.getmChatEdit().setText(text);
            }
        }

        @Override
        public Bitmap onLoadBitmap(String key, int width, int height) {
            if (mCacheMap.containsKey(key)) {
                Bitmap bitmap = mCacheMap.get(key);
                if (bitmap != null && !bitmap.isRecycled()) {
                    return bitmap;
                } else {
                    return loadBitmapImage(key, width, height);
                }
            } else {
                return loadBitmapImage(key, width, height);
            }
        }


        public Bitmap loadBitmapImage(String key, int width, int height) {
            Bitmap bitmap = null;
            try {
                bitmap = BitmapUtil.decodeBitmapFromFile(key, width, height);
            } catch (Exception e) {
                LogUtils.log(key);
                Reporter.post("图片加载失败，", e);
            }
            if (bitmap == null) {
                return null;
            }
            mCacheMap.put(key, bitmap);
            return bitmap;
        }

        @Override
        public void onItemLongClick(View v, AChatHolderInterface holder, ChatMessage message) {
            if (mCurChatType == ChatListType.LIVE) {
                return;
            }

            if (isShowMoreSelect) {
                return;
            }

            // 群组长按头像
            if (isGroupChat() && v.getId() == R.id.chat_head_iv) {
                mMessageEventListener.LongAvatarClick(message);
                return;
            }

            /*Vibrator vib = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
            vib.vibrate(40);// 只震动一秒，一次*/

            mChatPpWindow = new ChatTextClickPpWindow(mContext, new ClickListener(message, holder.getAdapterPosition()),
                    message, mToUserId, mCurChatType == ChatListType.COURSE, isGroupChat(),
                    mCurChatType == ChatListType.DEVICE, mGroupLevel);
            int offsetY = v.getHeight() + holder.dp2px(45);
            mChatPpWindow.showAsDropDown(v, 0, holder.mouseY - offsetY);
        }

        @Override
        public void onItemClick(View v, AChatHolderInterface holder, ChatMessage message) {
            if (UiUtils.isNormalClick(v)) {
                if (isShowMoreSelect) {
                    message.isMoreSelected = !message.isMoreSelected;
                    holder.setBoxSelect(message.isMoreSelected);
                    return;
                }

                if (message.getType() == Constants.TYPE_CUSTOM_CHANGE_READ_DEL_TIME) {
                    if (mMessageEventListener != null) {
                        mMessageEventListener.onChangeReadDelTimeClick(v, holder, message);
                    }
                }

                switch (v.getId()) {
                    case R.id.tv_read: // 点击了群已读人数
                        Intent intent = new Intent(mContext, RoomReadListActivity.class);
                        intent.putExtra("packetId", message.getPacketId());
                        intent.putExtra("roomId", mToUserId);
                        mContext.startActivity(intent);
                        break;
                    case R.id.iv_failed: // 点击了发送失败的消息的感叹号
                        holder.mIvFailed.setVisibility(GONE);
                        holder.mSendingBar.setVisibility(VISIBLE);
                        message.setMessageState(Constants.MESSAGE_SEND_ING);
                        mMessageEventListener.onSendAgain(message);
                        break;
                    case R.id.chat_head_iv: // 点击了头像
                        if (TextUtils.equals(message.getFromUserId(),Constants.ID_SYSTEM_NOTIFICATION)){
                            return;
                        }
                        if (message.isMySend()) {
                            mMessageEventListener.onFriendAvatarClick(mLoginUser.getUserId());
                        } else {
                            mMessageEventListener.onFriendAvatarClick(message.getFromUserId());
                        }
                        break;
                    case R.id.chat_warp_view:
                        clickRootItme(holder, message);
                        break;

                }

                if (holder.mHolderType == ChatHolderFactory.ChatHolderType.VIEW_SYSTEM_TIP) {
                    if (mMessageEventListener != null) {
                        mMessageEventListener.onTipMessageClick(message);
                    }
                    return;
                }
            }
        }

        @Override
        public void onReplayClick(View v, AChatHolderInterface holder, ChatMessage message) {
            if (isShowMoreSelect) {
                message.isMoreSelected = !message.isMoreSelected;
                holder.setBoxSelect(message.isMoreSelected);
                return;
            }

            ChatMessage replayMessage = new ChatMessage(message.getObjectId());
            int index = -1;
            for (int i = 0; i < mChatMessages.size(); i++) {
                ChatMessage m = mChatMessages.get(i);
                if (TextUtils.equals(m.getPacketId(), replayMessage.getPacketId())) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                // 内存里有被回复消息的话直接处理，
                scrollToPosition(index);
            } else {
                // 内存没有就回调出去查询数据库，
                if (mMessageEventListener != null) {
                    mMessageEventListener.onReplayClick(message);
                }
            }
        }
    }

    // 语音消息播放监听
    public class VoicePlayListener implements VoiceManager.VoicePlayListener {
        @Override
        public void onFinishPlay(String path) {
            //当前点击的播放完成了
            if (mCurClickPos < 0 || mCurClickPos > mChatMessages.size() - 1) {
                return;
            }
            ChatMessage currentMessage = mChatMessages.get(mCurClickPos);
            VoiceViewHolder currentVoiceHolder = aVoice.data.get(mCurClickPos);
            if (currentMessage != null && currentVoiceHolder != null) {
                if (currentMessage.getIsReadDel() && !currentMessage.isMySend()) {
                    ReadDelManager.getInstants().addReadMsg(currentMessage, currentVoiceHolder);  //把当前点击播放完成的别人的语音信息 添加到 阅后即焚 播放列表
                }
            }
            VoiceViewHolder holder = aVoice.next(mCurClickPos, mChatMessages);
            if (holder != null) {
                mCurClickPos = holder.position;
                ChatMessage message = mChatMessages.get(mCurClickPos);
                holder.sendReadMessage(message);
                VoicePlayer.instance().playVoice(holder.voiceView, mContext);
//                if (message.getIsReadDel()) {
//                    EventBus.getDefault().post(new MessageEventClickFire("delay", message.getPacketId()));
//                }
            }
//            if (mFireVoiceMaps.containsKey(path)) {
//                EventBus.getDefault().post(new MessageEventClickFire("delete", mFireVoiceMaps.get(path)));
//                mFireVoiceMaps.remove(path);
//            }
        }

        @Override
        public void onStopPlay(String path) {
            //当前点击的取消播放了
            ChatMessage currentMessage = mChatMessages.get(mCurClickPos);
            VoiceViewHolder currentVoiceHolder = aVoice.data.get(mCurClickPos);
            if (currentMessage != null && currentVoiceHolder != null) {
                if (currentMessage.getIsReadDel() && !currentMessage.isMySend()) {
                    ReadDelManager.getInstants().addReadMsg(currentMessage, currentVoiceHolder);  //把当前点击停止的别人的语音信息 添加到 阅后即焚 播放列表
                }
            }
            aVoice.remove(mCurClickPos);
//            if (mFireVoiceMaps.containsKey(path)) {
//                EventBus.getDefault().post(new MessageEventClickFire("delete", mFireVoiceMaps.get(path)));
//                mFireVoiceMaps.remove(path);
//            }
        }

        @Override
        public void onErrorPlay() {
        }
    }

    // 自动播放语音消息 工具类
    public class AutoVoiceModule {
        HashMap<Integer, VoiceViewHolder> data = new HashMap<>();
        HashMap<VoiceViewHolder, Integer> last = new HashMap<>();

        // 放入消息
        public void put(VoiceViewHolder key) {
            if (last.containsKey(key)) {
                int index = last.get(key);
                data.remove(index);
                last.put(key, key.position);
                data.put(key.position, key);
            } else {
                last.put(key, key.position);
                data.put(key.position, key);
            }
        }

        // 取出消息
        public VoiceViewHolder next(int position, List<ChatMessage> list) {
            if (position + 1 >= list.size()) {
                return null;
            }

            for (int i = position + 1; i < list.size(); i++) {
                ChatMessage message = list.get(i);
                if (message.getType() == Constants.TYPE_VOICE && !message.isMySend() && !message.isSendRead()) {
                    if (data.containsKey(i)) {
                        return data.get(i);
                    }
                }
            }
            return null;
        }

        // 删除消息
        public void remove(int position) {
            if (data.containsKey(position)) {
                last.remove(data.get(position));
                data.remove(position);
            }
        }
    }

    // 长按弹窗的点击事件监听
    public class ClickListener implements OnClickListener {
        private ChatMessage message;
        private int position;

        public ClickListener(ChatMessage message, int position) {
            this.message = message;
            this.position = position;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void onClick(View v) {
            mChatPpWindow.dismiss();
            switch (v.getId()) {
                case R.id.item_chat_copy_tv:
                    // 复制
                    if (message.getIsReadDel()) {
                        ToastUtil.showToast(mContext, R.string.tip_cannot_copy_burn);
                        return;
                    }
                    String s = StringUtils.replaceSpecialChar(message.getContent());
                    CharSequence charSequence = HtmlUtils.transform200SpanString(s.replaceAll("\n", "\r\n"), true);
                    // 获得剪切板管理者,复制文本内容
                    ToolUtils.copyContent(charSequence.toString());
                    break;
                case R.id.item_chat_relay_tv:
                    // 转发消息
                    if (message.getIsReadDel()) {
                        // 为阅后即焚类型的消息，不可转发
                        // Toast.makeText(mContext, "阅后即焚类型的消息不可以转发哦", Toast.LENGTH_SHORT).show();
                        ToastUtil.showToast(mContext, InternationalizationHelper.getString("CANNOT_FORWARDED"));
                        return;
                    }
                    Intent intent = new Intent(mContext, InstantMessageActivity.class);
                    intent.putExtra("fromUserId", mToUserId);
                    intent.putExtra("messageId", message.getPacketId());
                    ((Activity) mContext).startActivityForResult(intent, ChatActivity.REQUEST_CODE_INSTANTMESSAGE);

                    break;
                case R.id.item_chat_collection_tv:
                    // 添加为表情
                    if (message.getIsReadDel()) {
                        Toast.makeText(mContext, R.string.tip_cannot_save_burn_image, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    collectionEmotion(message, false);
                    break;
                case R.id.collection_other:
                    // 收藏
                    if (message.getIsReadDel()) {
                        Toast.makeText(mContext, R.string.tip_cannot_collect_burn, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    collectionEmotion(message, true);
                    break;
                case R.id.item_chat_back_tv:

                    // 撤回消息
                    mMessageEventListener.onMessageBack(message, position);
                    break;
                case R.id.item_chat_replay_tv:
                    // 回复消息
                    mMessageEventListener.onMessageReplay(message);
                    break;
                case R.id.item_chat_del_tv:
                    // 删除
                    if (mCurChatType == ChatListType.COURSE) {
                        if (mMessageEventListener != null) {
                            mMessageEventListener.onMessageClick(message);
                        }
                    } else {
                        // 发送广播去界面更新
                        Intent broadcast = new Intent(Constants.CHAT_MESSAGE_DELETE_ACTION);
                        broadcast.putExtra(Constants.CHAT_REMOVE_MESSAGE_POSITION, position);
                        mContext.sendBroadcast(broadcast);
                    }
                    break;
                case R.id.item_chat_more_select:
                    // 多选
                    Intent showIntent = new Intent(Constants.SHOW_MORE_SELECT_MENU);
                    showIntent.putExtra(Constants.CHAT_SHOW_MESSAGE_POSITION, position);
                    mContext.sendBroadcast(showIntent);
                    break;
                default:
                    break;
            }
        }
    }
}
