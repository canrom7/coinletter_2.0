package com.iimm.miliao.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.adapter.MessageEventRequert;
import com.iimm.miliao.audio.IMRecordController;
import com.iimm.miliao.audio.RecordListener;
import com.iimm.miliao.audio_x.VoicePlayer;
import com.iimm.miliao.bean.PublicMenu;
import com.iimm.miliao.bean.assistant.GroupAssistantDetail;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.course.ChatRecordHelper;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.mucfile.XfileUtils;
import com.iimm.miliao.ui.tool.WebViewActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.HtmlUtils;
import com.iimm.miliao.util.InputManager;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.util.filter.EmojiInputFilter;
import com.iimm.miliao.util.permission.AndPermissionUtils;
import com.iimm.miliao.util.permission.OnPermissionClickListener;
import com.iimm.miliao.xmpp.util.ImHelper;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.iimm.miliao.ui.tool.WebViewActivity.EXTRA_URL;

/**
 * @描述: 聊天界面下面输入操作的view
 */
public class ChatBottomView extends LinearLayout implements View.OnClickListener {
    private static final int RIGHT_VIEW_RECORD = 0;
    private static final int RIGHT_VIEW_SNED = 1;
    // 正在输入...
    boolean inputState = true;
    List<PublicMenu> mMenuDatas;
    private Context mContext;
    private LinearLayout mShotsLl;
    private RoundedImageView mShotsIv;
    // 主菜单
    private RelativeLayout rlChatMenu;
    private FrameLayout flPublicChatMenu;// 切换至公众号菜单
    private ImageButton mVoiceImgBtn;
    private ImageButton btnCancelReplay;
    private EditText mChatEdit;
    private Button mRecordBtn;
    private ImageButton mEmotionBtn;
    private ImageButton mMoreBtn;
    private Button mSendBtn;
    // 公众号菜单
    private LinearLayout lLTextMenu;
    private ViewStub lLTextMenuStub;
    private ImageView meunImg1;
    private ImageView meunImg2;
    private ImageView meunImg3;
    private TextView meunText1, meunText2, meunText3;
    private View light1, light2;
    // 多选菜单
    private LinearLayout lLMoreSelect;
    private ViewStub lLMoreSelectStub;
    /* Tool */
    private ChatFaceView mChatFaceView;
    private ViewStub mChatFaceViewStub;
    private ChatToolsView mChatToolsView;
    private ViewStub mChatToolsViewStub;
    private ChatBottomListener mBottomListener;
    private MoreSelectMenuListener mMoreSelectMenuListener;
    private IMRecordController mRecordController;
    private InputMethodManager mInputManager;
    private Handler mHandler = new Handler();
    private int mDelayTime;

    private boolean isGroup;
    private String roomId;
    private String roomJid;

    // 当前右边的模式，用int变量保存，效率更高点
    private int mRightView = RIGHT_VIEW_RECORD;
    private LayoutInflater mInflater;
    private ChatFaceView.BqMustInfoListener mBqMustInfoListener;
    private LinkedList<String> mAtUserLsit;//添加的@群成员集合
    private List<String> mNameList;//添加的@群成员名字集合
    OnClickListener publicMenuClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ll_public_meun_1:
                    if (XfileUtils.isNotEmpty(mMenuDatas.get(0).getMenuList())) {
                        showPpWindow(mMenuDatas.get(0).getMenuList(), meunText1);
                    } else {
                        String url = mMenuDatas.get(0).getUrl();
                        Intent intent = new Intent(getContext(), WebViewActivity.class);
                        intent.putExtra(EXTRA_URL, url + "?access_token=" + CoreManager.requireSelfStatus(getContext()).accessToken);
                        getContext().startActivity(intent);
                    }
                    break;
                case R.id.ll_public_meun_2:
                    if (XfileUtils.isNotEmpty(mMenuDatas.get(1).getMenuList())) {
                        showPpWindow(mMenuDatas.get(1).getMenuList(), meunText2);
                    } else {
                        String url = mMenuDatas.get(1).getUrl();
                        Intent intent = new Intent(getContext(), WebViewActivity.class);
                        intent.putExtra(EXTRA_URL, url + "?access_token=" + CoreManager.requireSelfStatus(getContext()).accessToken);
                        getContext().startActivity(intent);
                    }
                    break;
                case R.id.ll_public_meun_3:
                    if (XfileUtils.isNotEmpty(mMenuDatas.get(2).getMenuList())) {
                        showPpWindow(mMenuDatas.get(2).getMenuList(), meunText3);
                    } else {
                        String url = mMenuDatas.get(2).getUrl();
                        Intent intent = new Intent(getContext(), WebViewActivity.class);
                        intent.putExtra(EXTRA_URL, url + "?access_token=" + CoreManager.requireSelfStatus(getContext()).accessToken);
                        getContext().startActivity(intent);
                    }
                    break;
            }
        }
    };
    private boolean isEquipment;
    private boolean replayMode;

    public ChatBottomView(Context context) {
        super(context);
        init(context);
    }

    public ChatBottomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChatBottomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setEquipment(boolean isEquipment) {
        this.isEquipment = isEquipment;
        if (mChatToolsView != null) {
            mChatToolsView.setEquipment(isEquipment);
        }
    }

    public void setGroup(boolean isGroup, String roomId, String roomJid) {
        this.isGroup = isGroup;
        this.roomId = roomId;
        this.roomJid = roomJid;
        if (mChatToolsView != null) {
            mChatToolsView.setGroup(isGroup);
        }
    }

    public void setChatBottomListener(ChatBottomListener listener) {
        mBottomListener = listener;
    }

    public void setMoreSelectMenuListener(MoreSelectMenuListener moreSelectMenuListener) {
        mMoreSelectMenuListener = moreSelectMenuListener;
    }

    public LinearLayout getmShotsLl() {
        return mShotsLl;
    }

    public EditText getmChatEdit() {
        return mChatEdit;
    }

    public void notifyAssistant() {
        if (mChatToolsView != null) {
            mChatToolsView.notifyAssistant();
        }
    }

    private void init(Context context) {
        mContext = context;
        mInputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        // 不要延迟了，显得卡，会导致面板出现在软键盘上一瞬间，
        // Todo  延迟还是要给的，只是别给那么长，让软键盘先关闭，在去控制其他View的显示隐藏，否则交互时会出现及其不自然的动画
        mDelayTime = 60;

        LayoutInflater.from(mContext).inflate(R.layout.chat_bottom, this);

        mShotsLl = (LinearLayout) findViewById(R.id.b_shots_ll);
        mShotsIv = (RoundedImageView) findViewById(R.id.b_shots_iv);
        mVoiceImgBtn = (ImageButton) findViewById(R.id.voice_img_btn);
        btnCancelReplay = (ImageButton) findViewById(R.id.btnCancelReplay);
        mChatEdit = (EditText) findViewById(R.id.chat_edit);
        mRecordBtn = (Button) findViewById(R.id.record_btn);// 按住说话
        mEmotionBtn = (ImageButton) findViewById(R.id.emotion_btn);
        mMoreBtn = (ImageButton) findViewById(R.id.more_btn);
        mSendBtn = (Button) findViewById(R.id.send_btn);

        mChatFaceViewStub = findViewById(R.id.chat_face_view_stub);
        mChatToolsViewStub = findViewById(R.id.chat_tools_view_stub);

        // 主菜单
        rlChatMenu = (RelativeLayout) findViewById(R.id.rl_chat_meun);
        // 切换至公众号菜单
        flPublicChatMenu = (FrameLayout) findViewById(R.id.fl_public_menu);
        // 公众号菜单
        lLTextMenuStub = findViewById(R.id.ll_show_public_meun_stub);
        flPublicChatMenu.setVisibility(GONE);

        lLMoreSelectStub = findViewById(R.id.more_select_ll_stub);

        // 切换到公众号菜单
        flPublicChatMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextMeun();
            }
        });

        mVoiceImgBtn.setOnClickListener(this);
        btnCancelReplay.setOnClickListener(this);
        mChatEdit.setOnClickListener(this);
        mEmotionBtn.setOnClickListener(this);
        mMoreBtn.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);

        mChatEdit.setOnTouchListener((v, event) -> {
            mChatEdit.requestFocus();
            return false;
        });

        mChatEdit.setFilters(new InputFilter[]{new EmojiInputFilter(context)});
        mChatEdit.addTextChangedListener(new TextWatcher() {
            int mPreviousLength;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mPreviousLength = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1 && start == s.length() - 1 && s.charAt(start) == '@') {
                    // 判断输入框内最后一位s为@时跳转
                    mBottomListener.sendAt();
                }

                int currentView = 0;
                if (s.length() <= 0) {
                    currentView = RIGHT_VIEW_RECORD;
                } else {
                    currentView = RIGHT_VIEW_SNED;
                }

                if (currentView == mRightView) {
                    return;
                }
                mRightView = currentView;
                if (mRightView == 0) {
                    mMoreBtn.setVisibility(View.VISIBLE);
                    mSendBtn.setVisibility(View.GONE);
                } else {
                    mMoreBtn.setVisibility(View.GONE);
                    mSendBtn.setVisibility(View.VISIBLE);
                }
                inputText();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str = editable.toString().trim();
                if (TextUtils.isEmpty(str)) {
                    if (mNameList != null) {
                        mNameList.clear();
                    }
                    if (mAtUserLsit != null) {
                        mAtUserLsit.clear();
                    }
                }
            }
        });
        mChatEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    String str = mChatEdit.getText().toString();
                    if (TextUtils.isEmpty(str)) {
                        return false;
                    }
                    if (isGroup && mAtUserLsit != null && mAtUserLsit.size() > 0 && mNameList != null && mNameList.size() > 0 && mNameList.toString().length() >= 2 && str.length() >= 2 && mNameList.toString().contains(str.substring(str.length() - 2))) {
                        int index = -1;
                        if (mChatEdit.hasFocus()) {
                            index = mChatEdit.getSelectionStart();
                        } else {
                            index = str.length();
                        }
                        String sss = str.substring(0, index);
                        int at = sss.lastIndexOf("@");
                        mChatEdit.getText().delete(at, index);
                        try {
                            if (mAtUserLsit != null && mAtUserLsit.size() > 0) {
                                mAtUserLsit.removeLast();
                            }
                        } catch (Exception e) {

                        }
                        return true;
                    }
                }
                return false;
            }
        });
        mRecordController = new IMRecordController(mContext);
        mRecordController.setRecordListener(new RecordListener() {
            @Override
            public void onRecordSuccess(String filePath, int timeLen) {
                // 录音成功，返回录音文件的路径
                mRecordBtn.setText(R.string.motalk_voice_chat_tip_1);
                mRecordBtn.setBackgroundResource(R.drawable.im_voice_button_normal2);
                if (timeLen < 1) {
                    ToastUtil.showToast(mContext, InternationalizationHelper.getString("JXChatVC_TimeLess"));
                    return;
                }
                if (mBottomListener != null) {
                    mBottomListener.sendVoice(filePath, timeLen);
                }
            }

            @Override
            public void onRecordStart() {
                mBottomListener.stopVoicePlay();//停止播放聊天记录里的语音
                // 录音开始
                mRecordBtn.setText(R.string.motalk_voice_chat_tip_2);
                mRecordBtn.setBackgroundResource(R.drawable.im_voice_button_pressed2);
            }

            @Override
            public void onRecordCancel() {
                // 录音取消
                mRecordBtn.setText(R.string.motalk_voice_chat_tip_1);
                mRecordBtn.setBackgroundResource(R.drawable.im_voice_button_normal2);
            }
        });
        mRecordBtn.setOnTouchListener(mRecordController);
    }


    private void inputText() {
        if (inputState) {
            inputState = false;
            if (mBottomListener != null) {
                mBottomListener.onInputState();
            }

        } else {
            new CountDownTimer(1000, 30 * 1000) {

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    inputState = true;
                    inputText();
                }
            }.start();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        mChatEdit.setFocusable(hasWindowFocus);
        mChatEdit.setFocusableInTouchMode(hasWindowFocus);
        super.onWindowFocusChanged(hasWindowFocus);
    }

    /**
     * 改变录音按钮的状态<br/>
     * 1、当处于非录音状态，显示录音按钮<br/>
     * true的状态 2、当处于录音状态，显示键盘按钮<br/>
     * false的状态
     */
    private void changeRecordBtn(boolean show) {
        boolean isShowing = mRecordBtn.getVisibility() != View.GONE;
        if (isShowing == show) {
            return;
        }
        if (show) {
            mChatEdit.setVisibility(View.GONE);
            mRecordBtn.setVisibility(View.VISIBLE);
            mVoiceImgBtn.setBackgroundResource(R.drawable.im_keyboard);
        } else {
            mChatEdit.setVisibility(View.VISIBLE);
            mRecordBtn.setVisibility(View.GONE);
            mVoiceImgBtn.setBackgroundResource(R.drawable.im_voice);
        }
    }

    private boolean isToolsShown() {
        return mChatToolsView != null && mChatToolsView.getVisibility() != View.GONE;
    }

    /**
     * 改变更多按钮的状态<br/>
     * 1、当更多布局显示时，显示隐藏按钮<br/>
     * false的状态 2、当更多布局隐藏时，显示更多按钮<br/>
     * true的状态
     */
    private void changeChatToolsView(boolean show) {
        boolean isShowing = isToolsShown();
        if (isShowing == show) {
            return;
        }

        if (show) {
            if (mChatToolsView == null) {
                mChatToolsView = (ChatToolsView) mChatToolsViewStub.inflate();
                mChatToolsView.init(mBottomListener, roomId, roomJid, isEquipment, isGroup, CoreManager.requireConfig(getContext()).disableLocationServer);
            }
            mChatToolsView.setVisibility(View.VISIBLE);
            mMoreBtn.setBackgroundResource(R.drawable.im_btn_more_bg);
        } else {
            mChatToolsView.setVisibility(View.GONE);
            mMoreBtn.setBackgroundResource(R.drawable.im_btn_more_bg);
        }
    }

    private boolean isFaceShown() {
        return mChatFaceView != null && mChatFaceView.getVisibility() != View.GONE;
    }

    /**
     * 显示或隐藏表情布局
     */
    private void changeChatFaceView(boolean show) {
        boolean isShowing = isFaceShown();
        if (isShowing == show) {
            return;
        }
        if (show) {
            if (mChatFaceView == null) {
                mChatFaceView = (ChatFaceView) mChatFaceViewStub.inflate();
                mChatFaceView.initView(mBqMustInfoListener.getFragmentManager(), new ChatFaceView.MySmallBqListener() {
                    @Override
                    public void onEmojiClick(SpannableString ss) {
                        int index = mChatEdit.getSelectionStart();
                        if ("[del]".equals(ss.toString())) {
                            InputManager.backSpaceChatEdit(mChatEdit, mAtUserLsit, isGroup, mNameList);
                        } else {
                            if (mChatEdit.hasFocus()) {
                                mChatEdit.getText().insert(index, ss);
                            } else {
                                mChatEdit.getText().insert(mChatEdit.getText().toString().length(), ss);
                            }
                        }
                    }
                }, mBqMustInfoListener.getCustomBqListener(), mBqMustInfoListener.getBqKeyBoardListener());
                mChatFaceView.initMyBqBao(false, mBqMustInfoListener.getCoreManager(), mBqMustInfoListener.getMyBqListener());
            }
            mChatFaceView.setVisibility(View.VISIBLE);
            mEmotionBtn.setBackgroundResource(R.drawable.im_btn_keyboard_bg);
        } else {
            mChatFaceView.setVisibility(View.GONE);
            mEmotionBtn.setBackgroundResource(R.drawable.im_btn_emotion_bg);
        }
    }

    @Override
    public void onClick(View v) {
        if (mChatToolsView != null && mChatToolsView.isGroupAssistant()) {
            mChatToolsView.changeGroupAssistant();
            if (v.getId() != R.id.chat_edit) {
                return;
            }
        }
        if (v.getId() == R.id.send_btn) {// 发送文字与戳一戳，不受过快点击限制影响，
            if (mBottomListener != null) {
                String msg = mChatEdit.getText().toString(); // 获取文本框的内容
                if (TextUtils.isEmpty(msg)) {
                    return;
                }
                if (msg.contains("@") && mAtUserLsit != null && mAtUserLsit.size() > 0) {
                    mBottomListener.sendAtMessage(msg);
                } else {
                    mBottomListener.sendText(msg);
                }
                mChatEdit.setText("");
            }
        } else if (UiUtils.isNormalClick(v)) {
            // UiUtils.isNormalClick防止点击过快，
            switch (v.getId()) {
                /*************************** 主菜单 Event **************************/
                case R.id.voice_img_btn:
                    if (mRecordBtn.getVisibility() != View.GONE) {// 录音布局在显示,那么点击则是隐藏录音，显示键盘
                        changeRecordBtn(false);
                        // editText隐藏时无法获得焦点，所以在要visible后调用这个弹出软键盘，
                        showKeyboard();
                    } else {// 录音布局没有显示,那么点击则是显示录音，隐藏表情、更多、键盘布局
                        AndPermissionUtils.recordingPermission(mContext, new OnPermissionClickListener() {
                            @Override
                            public void onSuccess() {
                                closeKeyboard();
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        changeChatFaceView(false);
                                        changeChatToolsView(false);
                                        changeRecordBtn(true);
                                    }
                                }, mDelayTime);
                            }

                            @Override
                            public void onFailure(List<String> data) {
                                ToastUtil.showToast(mContext, mContext.getString(R.string.please_turn_on_recording_permission));
                            }
                        });
                    }
                    break;
                case R.id.btnCancelReplay:
                    resetReplay();
                case R.id.chat_edit:// 隐藏其他所有布局，显示键盘
                    changeChatFaceView(false);
                    changeChatToolsView(false);
                    changeRecordBtn(false);
                    inputText();
                    break;
                case R.id.emotion_btn:
                    if (isFaceShown()) {// 表情布局在显示,那么点击则是隐藏表情，显示键盘
                        changeChatFaceView(false);
                        showKeyboard();
                    } else {// 表情布局没有显示,那么点击则是显示表情，隐藏键盘、录音、更多布局
                        closeKeyboard();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                changeChatFaceView(true);
                                changeChatToolsView(false);
                                changeRecordBtn(false);
                            }
                        }, mDelayTime);
                    }
                    break;
                case R.id.more_btn:
                    // 点击加号取消回复，
                    if (replayMode) {
                        resetReplay();
                        mBottomListener.cancelReplay();
                    }

                    if (isToolsShown()) {// 表情布局在显示,那么点击则是隐藏表情，显示键盘
                        changeChatToolsView(false);
                        showKeyboard();
                    } else {// 更多布局没有显示,那么点击则是显示更多，隐藏表情、录音、键盘布局
                        closeKeyboard();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                changeChatFaceView(false);
                                changeChatToolsView(true);
                                changeRecordBtn(false);
                            }
                        }, mDelayTime);
                    }
                    String shots = PreferenceUtils.getString(mContext, Constants.SCREEN_SHOTS, "No_Shots");
                    if (!shots.equals("No_Shots")) {// 有截图
                        try {
                            File file = new File(shots);
                            mShotsLl.setVisibility(View.VISIBLE);
                            Glide.with(mContext).load(file).into(mShotsIv);

                            new CountDownTimer(5000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {

                                }

                                @Override
                                public void onFinish() {
                                    mShotsLl.setVisibility(View.GONE);
                                    PreferenceUtils.putString(mContext, Constants.SCREEN_SHOTS, "No_Shots");
                                }
                            }.start();
                        } catch (Exception e) {
                            Log.e("TAG", "截图地址异常");
                        }
                    }
                    break;

                /********** MoreSelectMenu Event *********/
                case R.id.more_select_forward_iv:
                    if (mMoreSelectMenuListener != null) {
                        mMoreSelectMenuListener.clickForwardMenu();
                    }
                    break;
                case R.id.more_select_collection_iv:
                    if (mMoreSelectMenuListener != null) {
                        mMoreSelectMenuListener.clickCollectionMenu();
                    }
                    break;
                case R.id.more_select_delete_iv:
                    if (mMoreSelectMenuListener != null) {
                        mMoreSelectMenuListener.clickDeleteMenu();
                    }
                    break;
                case R.id.more_select_email_iv:
                    if (mMoreSelectMenuListener != null) {
                        mMoreSelectMenuListener.clickEmailMenu();
                    }
                    break;
            }
        }
    }

    private void closeKeyboard() {
        mInputManager.hideSoftInputFromWindow(mChatEdit.getApplicationWindowToken(), 0);
    }

    private void showKeyboard() {
        mChatEdit.requestFocus();
        mInputManager.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
    }

    public void reset() {
        changeChatFaceView(false);
        changeChatToolsView(false);
        changeRecordBtn(false);
        closeKeyboard();
    }

    public void updateAiTe(LinkedList<String> mList, List<String> mName) {
        this.mAtUserLsit = mList;
        this.mNameList = mName;
    }

    // 显示聊天输入框
    private void showChatBottom() {
        rlChatMenu.setVisibility(VISIBLE);
        if (lLTextMenu != null) {
            lLTextMenu.setVisibility(GONE);
        }
        if (lLMoreSelect != null) {
            lLMoreSelect.setVisibility(GONE);
        }
    }

    // 显示公众号菜单
    private void showTextMeun() {
        rlChatMenu.setVisibility(GONE);
        if (lLTextMenu != null) {
            lLTextMenu.setVisibility(VISIBLE);
        }
        if (lLMoreSelect != null) {
            lLMoreSelect.setVisibility(GONE);
        }
    }

    // 显示 || 隐藏 多选菜单
    public void showMoreSelectMenu(boolean isShow) {
        if (lLMoreSelect == null) {
            lLMoreSelect = (LinearLayout) lLMoreSelectStub.inflate();
            lLMoreSelect.findViewById(R.id.more_select_forward_iv).setOnClickListener(this);
            lLMoreSelect.findViewById(R.id.more_select_collection_iv).setOnClickListener(this);
            lLMoreSelect.findViewById(R.id.more_select_delete_iv).setOnClickListener(this);
            lLMoreSelect.findViewById(R.id.more_select_email_iv).setOnClickListener(this);
        }
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.translate_dialog_in);
        if (isShow) {
            reset();
            rlChatMenu.setVisibility(GONE);
            if (lLTextMenu != null) {
                lLTextMenu.setVisibility(GONE);
            }
            lLMoreSelect.startAnimation(animation);
            lLMoreSelect.setVisibility(VISIBLE);
        } else {
            rlChatMenu.startAnimation(animation);
            rlChatMenu.setVisibility(VISIBLE);
            if (lLTextMenu != null) {
                lLTextMenu.setVisibility(GONE);
            }
            lLMoreSelect.setVisibility(GONE);
        }
    }

    // 全员禁言
    public void isAllBanned(boolean isBanned) {
        isBanned(isBanned, R.string.hint_all_ban);
    }

    public void isBanned(boolean isBanned, @StringRes int hint) {
        if (isBanned) {
            rlChatMenu.setAlpha(0.5f);
            mVoiceImgBtn.setClickable(false);
            mChatEdit.setEnabled(false);
            mEmotionBtn.setClickable(false);
            mMoreBtn.setClickable(false);
            mSendBtn.setClickable(false);
            mRecordBtn.setEnabled(false);
            mChatEdit.setText("");// 需要清空EditText,否则Hint不会显示出来
            mChatEdit.setHint(hint);
            mChatEdit.setGravity(Gravity.CENTER);
        } else {
            rlChatMenu.setAlpha(1.0f);
            mVoiceImgBtn.setClickable(true);
            mChatEdit.setEnabled(true);
            mEmotionBtn.setClickable(true);
            mMoreBtn.setClickable(true);
            mSendBtn.setClickable(true);
            mRecordBtn.setEnabled(true);
            mChatEdit.setHint(R.string.please_input_chat_content);
            mChatEdit.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }
    }

    public void recordCancel() {
        if (mRecordController != null) {
            mRecordController.cancel();
        }

        VoicePlayer.instance().stop();
        ChatRecordHelper.instance().reset();
    }

    public void fillRoomMenu(List<PublicMenu> datas) {
        if (lLTextMenu == null) {
            lLTextMenu = (LinearLayout) lLTextMenuStub.inflate();
            meunImg1 = lLTextMenu.findViewById(R.id.meun_left_img1);
            meunImg2 = lLTextMenu.findViewById(R.id.meun_left_img2);
            meunImg3 = lLTextMenu.findViewById(R.id.meun_left_img3);
            meunText1 = lLTextMenu.findViewById(R.id.meunText1);
            meunText2 = lLTextMenu.findViewById(R.id.meunText2);
            meunText3 = lLTextMenu.findViewById(R.id.meunText3);
            light1 = lLTextMenu.findViewById(R.id.meun_light1);
            light2 = lLTextMenu.findViewById(R.id.meun_light2);
            lLTextMenu.findViewById(R.id.ll_public_meun_1).setOnClickListener(publicMenuClickListener);
            lLTextMenu.findViewById(R.id.ll_public_meun_2).setOnClickListener(publicMenuClickListener);
            lLTextMenu.findViewById(R.id.ll_public_meun_3).setOnClickListener(publicMenuClickListener);
            // 切换到主菜单
            lLTextMenu.findViewById(R.id.fl_text_meun).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChatBottom();
                }
            });
        }
        if (datas != null && datas.size() > 0) {
            mMenuDatas = datas;
            showTextMeun();
            flPublicChatMenu.setVisibility(VISIBLE);
            switch (datas.size()) {
                case 1:
                    meunText1.setText(datas.get(0).getName());
                    findViewById(R.id.ll_public_meun_2).setVisibility(GONE);
                    findViewById(R.id.ll_public_meun_3).setVisibility(GONE);
                    light1.setVisibility(GONE);
                    light2.setVisibility(GONE);

                    if (XfileUtils.isNotEmpty(datas.get(0).getMenuList())) {
                        meunImg1.setVisibility(VISIBLE);
                    }
                    break;
                case 2:
                    meunText1.setText(datas.get(0).getName());
                    meunText2.setText(datas.get(1).getName());
                    findViewById(R.id.ll_public_meun_3).setVisibility(GONE);
                    light2.setVisibility(GONE);

                    if (XfileUtils.isNotEmpty(datas.get(0).getMenuList())) {
                        meunImg1.setVisibility(VISIBLE);
                    }

                    if (XfileUtils.isNotEmpty(datas.get(1).getMenuList())) {
                        meunImg2.setVisibility(VISIBLE);
                    }
                    break;
                default: // 不管有多少个只显示三个
                    meunText1.setText(datas.get(0).getName());
                    meunText2.setText(datas.get(1).getName());
                    meunText3.setText(datas.get(2).getName());

                    if (XfileUtils.isNotEmpty(datas.get(1).getMenuList())) {
                        meunImg2.setVisibility(VISIBLE);
                    }
                    if (XfileUtils.isNotEmpty(datas.get(0).getMenuList())) {
                        meunImg1.setVisibility(VISIBLE);
                    }
                    if (XfileUtils.isNotEmpty(datas.get(2).getMenuList())) {
                        meunImg3.setVisibility(VISIBLE);
                    }
                    break;
            }
        } else {
            flPublicChatMenu.setVisibility(GONE);
            showChatBottom();
        }
    }

    private void showPpWindow(final List<PublicMenu.MenuListBean> menuList, View view) {
        mInflater = LayoutInflater.from(getContext());
        View list = mInflater.inflate(R.layout.dialog_list_menu, null);
        MyListView listView = (MyListView) list.findViewById(R.id.dialog_menu_lv);
        listView.setAdapter(new MyMenuAdapter(menuList));

        final PopupWindow popupWindow = new PopupWindow(list, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PublicMenu.MenuListBean bean = menuList.get(position);
                String url = menuList.get(position).getUrl();
                if (!TextUtils.isEmpty(bean.getMenuId())) {
                    // 域名+menuId+token
                    url = CoreManager.requireConfig(MyApplication.getInstance()).apiUrl + bean.getMenuId()
                            + "?access_token=" + CoreManager.requireSelfStatus(getContext()).accessToken;
                    EventBus.getDefault().post(new MessageEventRequert(url));
                    popupWindow.dismiss();
                    return;
                }
                Intent intent = new Intent(getContext(), WebViewActivity.class);
                intent.putExtra(EXTRA_URL, url + "?access_token=" + CoreManager.requireSelfStatus(getContext()).accessToken);
                getContext().startActivity(intent);
            }
        });

        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        popupWindow.getContentView().measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        // +x右,-x左,+y下,-y上
        int xoff = popupWindow.getContentView().getMeasuredHeight();
        int yoff = popupWindow.getContentView().getMeasuredWidth();

        int hegiht = view.getHeight();
        int width = view.getWidth();

        width = (int) ((width - yoff) / 2.0 + 0.5f);
        popupWindow.showAsDropDown(view, width, -xoff - hegiht - 35);
    }

    public void resetReplay() {
        replayMode = false;
        mChatEdit.setHint("");
        mVoiceImgBtn.setVisibility(VISIBLE);
        btnCancelReplay.setVisibility(GONE);
    }

    public void setReplay(ChatMessage chatMessage) {
        replayMode = true;
        String hint = getContext().getString(R.string.replay_label) + chatMessage.getFromUserName() + ": " + ImHelper.getSimpleContent(chatMessage);
        mChatEdit.setHint(HtmlUtils.addSmileysToMessage(hint, false));
        mVoiceImgBtn.setVisibility(GONE);
        btnCancelReplay.setVisibility(VISIBLE);
        // 取消发送语音状态，
        changeRecordBtn(false);
    }

    /**
     * 更新我添加的表情包
     *
     * @param b
     * @param coreManager
     * @param myBqListener
     */
    public void getMyBqBao(boolean b, CoreManager coreManager, ChatFaceView.MyBqListener myBqListener) {
        if (mChatFaceView != null) {
            mChatFaceView.getMyBqBao(b, coreManager, myBqListener);
        }
    }

    /**
     * 获取自定义的表情包
     */
    public void getCustomBq() {
        if (mChatFaceView != null) {
            mChatFaceView.updateCustomBq();
        }
    }

    public interface ChatBottomListener {
        void sendAt();

        void sendAtMessage(String text);

        void sendText(String text);

        void sendGif(String text);

        // 发送 自定义表情
        void sendCollection(String collection);

        void sendVoice(String filePath, int timeLen);

        void stopVoicePlay();

        void clickPhoto();

        void clickCamera();

        void clickAudio();

        void clickStartRecord();

        void clickLocalVideo();

        void clickVideoChat();

        void clickLocation();

        void clickRedpacket();

        void clickTransferMoney();

        void clickCollection();

        void clickTwoWayWithdrawal();

        void clickCard();

        void clickFile();

        void clickContact();

        void clickShake();

        void clickGroupAssistant(GroupAssistantDetail groupAssistantDetail);

        void onInputState();

        void clickCloudRedEnvelope();

        default void cancelReplay() {
        }

        void clickCloudTransfer();
    }

    public interface MoreSelectMenuListener {
        void clickForwardMenu();

        void clickCollectionMenu();

        void clickDeleteMenu();

        void clickEmailMenu();
    }

    class MyMenuAdapter extends BaseAdapter {

        List<PublicMenu.MenuListBean> menuList;

        public MyMenuAdapter(List<PublicMenu.MenuListBean> list) {
            menuList = list;
        }

        @Override
        public int getCount() {
            return menuList == null ? 0 : menuList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = mInflater.inflate(R.layout.item_menu_text, null);
            TextView tv = (TextView) convertView.findViewById(R.id.tv_item_number);
            tv.setText(menuList.get(position).getName());
            return convertView;
        }
    }

    public void setBqKeyBoardListener(ChatFaceView.BqMustInfoListener listener) {
        this.mBqMustInfoListener = listener;
    }
}
