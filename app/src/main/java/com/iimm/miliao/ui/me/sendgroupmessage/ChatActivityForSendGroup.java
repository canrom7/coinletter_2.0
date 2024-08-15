package com.iimm.miliao.ui.me.sendgroupmessage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.audio_x.VoicePlayer;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.PrivacySetting;
import com.iimm.miliao.bean.VideoFile;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.downloader.Downloader;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.PrivacySettingHelper;
import com.iimm.miliao.helper.UploadEngine;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.emoji.BqShopActivity;
import com.iimm.miliao.ui.emoji.CustomBqManageActivity;
import com.iimm.miliao.ui.map.MapPickerActivity;
import com.iimm.miliao.ui.me.LocalVideoActivity;
import com.iimm.miliao.ui.me.sendgroupmessage.ChatBottomForSendGroup.ChatBottomListener;
import com.iimm.miliao.util.AppExecutors;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.ImageUtils;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.video.EasyCameraActivity;
import com.iimm.miliao.video.MessageEventGpu;
import com.iimm.miliao.view.ChatFaceView;
import com.iimm.miliao.view.SelectCardPopupWindow;
import com.iimm.miliao.view.SelectFileDialog;
import com.iimm.miliao.view.photopicker.PhotoPickerActivity;
import com.iimm.miliao.view.photopicker.SelectModel;
import com.iimm.miliao.view.photopicker.intent.PhotoPickerIntent;
import com.iimm.miliao.xmpp.util.ImHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import fm.jiecao.jcvideoplayer_lib.MessageEvent;
import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

import static com.iimm.miliao.ui.message.ChatActivity.REQUEST_MANAGE;


/**
 * 群发消息
 */
public class ChatActivityForSendGroup extends BaseActivity implements
        ChatBottomListener, SelectCardPopupWindow.SendCardS {
    // 相册、视频、位置
    private static final int REQUEST_CODE_PICK_PHOTO = 2;
    private static final int REQUEST_CODE_SELECT_VIDE0 = 3;
    private static final int REQUEST_CODE_SELECT_LOCATE = 5;
    private TextView mCountTv;
    private TextView mNameTv;
    private ChatBottomForSendGroup mChatBottomView;
    private String mLoginUserId;
    private String mLoginNickName;
    private List<String> userIds;
    private List<String> mCloneUserIds;
    private String userNames;
    private UploadEngine.ImFileUploadResponse mUploadResponse = new UploadEngine.ImFileUploadResponse() {

        @Override
        public void onSuccess(String toUserId, ChatMessage message) {
            message.setUpload(true);
            message.setUploadSchedule(100);
            groupSendMessage(message);
        }

        @Override
        public void onFailure(String toUserId, ChatMessage message) {
            ToastUtil.showToast(mContext, getString(R.string.upload_failed));
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_for_sg);

        String ids = getIntent().getStringExtra("USERIDS");
        userIds = JSON.parseArray(ids, String.class);
        userNames = getIntent().getStringExtra("USERNAMES");
        mCloneUserIds = new ArrayList<>();
        mCloneUserIds.addAll(userIds);
        mLoginUserId = coreManager.getSelf().getUserId();
        mLoginNickName = coreManager.getSelf().getNickName();
        EventBus.getDefault().register(this);
        Downloader.getInstance().init(MyApplication.getInstance().mAppDir + File.separator + mLoginUserId
                + File.separator + Environment.DIRECTORY_MUSIC);
        initActionBar();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.mass));
    }

    private void initView() {
        mCountTv = (TextView) findViewById(R.id.send_size_tv);
        mNameTv = (TextView) findViewById(R.id.send_name_tv);
        mCountTv.setText(getString(R.string.you_will_send_a_message_to) + userIds.size() + getString(R.string.bit) + getString(R.string.friend));
        mNameTv.setText(userNames);
        mChatBottomView = (ChatBottomForSendGroup) findViewById(R.id.chat_bottom_view);
        mChatBottomView.setBqKeyBoardListener(new ChatFaceView.BqMustInfoListener() {
            @Override
            public FragmentManager getFragmentManager() {
                return getSupportFragmentManager();
            }

            @Override
            public ChatFaceView.BqKeyBoardListener getBqKeyBoardListener() {
                return bqKeyBoardListener;
            }

            @Override
            public CoreManager getCoreManager() {
                return coreManager;
            }

            @Override
            public ChatFaceView.CustomBqListener getCustomBqListener() {
                return customBqListener;
            }

            @Override
            public ChatFaceView.MyBqListener getMyBqListener() {
                return myBqListener;
            }
        });
        mChatBottomView.setChatBottomListener(this);
    }

    /**
     * 为每条消息设置统一的参数;注：消息的packetId 应在发送时，for循环内设置，不应在外部设置
     *
     * @param message
     */
    private void setSameParams(ChatMessage message) {
        DialogHelper.showDefaulteMessageProgressDialogAddCancel(this, dialog -> dialog.dismiss());

        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setIsReadDel(0);
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(this);
        boolean isEncrypt = privacySetting.getIsEncrypt() == 1;
        if (isEncrypt) {
            message.setIsEncrypt(1);
        } else {
            message.setIsEncrypt(0);
        }
        message.setReSendCount(ChatMessageDao.fillReCount(message.getType()));
        sendRichOrTextMessage(message);
    }

    private void sendRichOrTextMessage(ChatMessage message) {
        if (message.getType() == Constants.TYPE_VOICE || message.getType() == Constants.TYPE_IMAGE
                || message.getType() == Constants.TYPE_VIDEO || message.getType() == Constants.TYPE_FILE
                || message.getType() == Constants.TYPE_LOCATION) {
            if (!message.isUpload()) {
                UploadEngine.uploadImFile(ChatActivityForSendGroup.this, coreManager, coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), userIds.get(0), message, mUploadResponse);
            } else {
                message.setUpload(true);
                message.setUploadSchedule(100);
                groupSendMessage(message);
            }
        } else {
            groupSendMessage(message);
        }
    }

    /**
     * 群发消息(选择多人发送)  不是群消息
     *
     * @param chatMessage
     */
    private void groupSendMessage(ChatMessage chatMessage) {
        for (int i = 0; i < userIds.size(); i++) {
            final String userId = userIds.get(i);
            ChatMessage message = chatMessage.clone(false);
            AppExecutors.getInstance().networkIO().execute(new Runnable() {
                @Override
                public void run() {
                    message.setToUserId(userId);
                    message.setPacketId(ToolUtils.getUUID());
                    message.setMySend(true);
                    message.setFromUserId(mLoginUserId);
                    message.setFromUserName(mLoginNickName);
                    message.setIsReadDel(PreferenceUtils.getInt(mContext, Constants.MESSAGE_READ_FIRE + message.getToUserId() + mLoginUserId, 0));
                    message.setDoubleTimeSend(TimeUtils.time_current_time_double());
                    message.setUpload(true);
                    message.setUploadSchedule(100);
                    PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(ChatActivityForSendGroup.this);
                    boolean isEncrypt = privacySetting.getIsEncrypt() == 1;
                    if (isEncrypt) {
                        message.setIsEncrypt(1);
                    } else {
                        message.setIsEncrypt(0);
                    }
                    message.setReSendCount(ChatMessageDao.fillReCount(message.getType()));
                    if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, userId, message)) {
                        ImHelper.sendChatMessage(userId, message);
                    }
                }
            });
        }
        DialogHelper.dismissProgressDialog();
    }
    int threadCount = 5;
    int count = 0;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            count++;
            if(count==threadCount){
                count = 0;
                threadCount = 5;
                DialogHelper.dismissProgressDialog();
            }
        }
    };

    public void sendThread(ChatMessage chatMessage){
        int itemsize = 0;
        if(userIds.size()<500){
            itemsize = userIds.size();
            threadCount = 1;
        }else {
            itemsize = userIds.size()/threadCount;
            if(userIds.size()/threadCount!=0){
                threadCount = threadCount+1;
            }
        }

        for(int i = 0;i<threadCount;i++){
            int finalI = i;
            int finalItemsize = itemsize;
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    List<String> list = new ArrayList<>();
                    if(finalI * finalItemsize <userIds.size()){
                        list.addAll(userIds.subList(finalI * finalItemsize,(finalI +1)* finalItemsize));
                    }else {
                        list.addAll(userIds.subList(finalI * finalItemsize,userIds.size()));
                    }
                    for(int j = 0;j<list.size();j++){
                        final String userId = userIds.get(j);
                        ChatMessage message = chatMessage.clone(false);
                        message.setToUserId(userId);
                        message.setPacketId(ToolUtils.getUUID());
                        message.setMySend(true);
                        message.setFromUserId(mLoginUserId);
                        message.setFromUserName(mLoginNickName);
                        message.setIsReadDel(PreferenceUtils.getInt(mContext, Constants.MESSAGE_READ_FIRE + message.getToUserId() + mLoginUserId, 0));
                        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
                        message.setUpload(true);
                        message.setUploadSchedule(100);
                        PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(ChatActivityForSendGroup.this);
                        boolean isEncrypt = privacySetting.getIsEncrypt() == 1;
                        if (isEncrypt) {
                            message.setIsEncrypt(1);
                        } else {
                            message.setIsEncrypt(0);
                        }
                        message.setReSendCount(ChatMessageDao.fillReCount(message.getType()));
                        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, userId, message)) {
                            ImHelper.sendChatMessage(userId, message);
                        }
                        handler.sendEmptyMessage(0);
                    }

                }
            }.start();
        }
    }
    @Override
    public void stopVoicePlay() {
        VoicePlayer.instance().stop();
    }

    @Override
    public void sendVoice(String filePath, int timeLen) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        long fileSize = file.length();
        ChatMessage message = new ChatMessage();
        message.setType(Constants.TYPE_VOICE);
        message.setContent("");
        message.setFilePath(filePath);
        message.setFileSize((int) fileSize);
        message.setTimeLen(timeLen);
        setSameParams(message);
    }

    @Override
    public void sendText(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(Constants.TYPE_TEXT);
        message.setContent(text);
        setSameParams(message);
    }

    @Override
    public void sendGif(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(Constants.TYPE_GIF);
        message.setContent(text);
        setSameParams(message);
    }

    @Override
    public void sendCollection(String collection) {
        ChatMessage message = new ChatMessage();
        message.setType(Constants.TYPE_IMAGE);
        message.setContent(collection);
        message.setUpload(true);// 已上传服务器
        setSameParams(message);
    }

    public void sendImage(File file) {
        if (!file.exists()) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(Constants.TYPE_IMAGE);
        message.setContent("");
        String filePath = file.getAbsolutePath();
        message.setFilePath(filePath);
        long fileSize = file.length();
        message.setFileSize((int) fileSize);
        int[] imageParam = ImageUtils.getImageWidthHeight(filePath);
        message.setLocation_x(String.valueOf(imageParam[0]));
        message.setLocation_y(String.valueOf(imageParam[1]));
        setSameParams(message);
    }

    public void sendVideo(File file) {
        if (!file.exists()) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(Constants.TYPE_VIDEO);
        message.setContent("");
        String filePath = file.getAbsolutePath();
        message.setFilePath(filePath);
        long fileSize = file.length();
        message.setFileSize((int) fileSize);
        setSameParams(message);
    }

    public void sendFile(File file) {
        if (!file.exists()) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(Constants.TYPE_FILE);
        message.setContent("");
        String filePath = file.getAbsolutePath();
        message.setFilePath(filePath);
        long fileSize = file.length();
        message.setFileSize((int) fileSize);
        setSameParams(message);
    }

    public void sendLocate(double latitude, double longitude, String address, String snapshot) {
        ChatMessage message = new ChatMessage();
        message.setType(Constants.TYPE_LOCATION);
        message.setContent("");
        message.setFilePath(snapshot);
        message.setLocation_x(latitude + "");
        message.setLocation_y(longitude + "");
        message.setObjectId(address);
        setSameParams(message);
    }

    public void sendCard(Friend friend) {
        ChatMessage message = new ChatMessage();
        message.setType(Constants.TYPE_CARD);
        message.setContent(friend.getNickName());
        message.setObjectId(friend.getUserId());
        setSameParams(message);
    }

    @Override
    public void clickPhoto() {
        ArrayList<String> imagePaths = new ArrayList<>();
        PhotoPickerIntent intent = new PhotoPickerIntent(ChatActivityForSendGroup.this);
        intent.setSelectModel(SelectModel.SINGLE);
        intent.setSelectedPaths(imagePaths);
        startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
        mChatBottomView.reset();
    }

    @Override
    public void clickCamera() {
        Intent intent = new Intent(this, EasyCameraActivity.class);
        startActivity(intent);
        mChatBottomView.reset();
    }

    @Override
    public void clickVideo() {
        Intent intent = new Intent(mContext, LocalVideoActivity.class);
        intent.putExtra(AppConstant.EXTRA_ACTION, AppConstant.ACTION_SELECT);
        intent.putExtra(AppConstant.EXTRA_MULTI_SELECT, false);
        startActivityForResult(intent, REQUEST_CODE_SELECT_VIDE0);
    }

    @Override
    public void clickFile() {
        SelectFileDialog dialog = new SelectFileDialog(this, new SelectFileDialog.OptionFileListener() {
            @Override
            public void option(List<File> files) {
                if (files != null && files.size() > 0) {
                    for (int i = 0; i < files.size(); i++) {
                        sendFile(files.get(i));
                    }
                }
            }

            @Override
            public void intent() {

            }
        });
        dialog.show();
    }

    @Override
    public void clickLocation() {
        Intent intent = new Intent(mContext, MapPickerActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SELECT_LOCATE);
    }

    @Override
    public void clickCard() {
        SelectCardPopupWindow mSelectCardPopupWindow = new SelectCardPopupWindow(this, this);
        mSelectCardPopupWindow.showAtLocation(findViewById(R.id.root_view),
                Gravity.CENTER, 0, 0);
    }

    @Override
    public void sendCardS(List<Friend> friends) {
        for (int i = 0; i < friends.size(); i++) {
            sendCard(friends.get(i));
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventGpu message) {// 拍照返回
        photograph(new File(message.event));
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEvent message) {
        Iterator<String> iterator = mCloneUserIds.iterator();
        while (iterator.hasNext()) {
            String userId = iterator.next();
            if (userId.equals(message.message)) { // 该条消息发送成功
                iterator.remove();
                if (mCloneUserIds.size() == 0) {// 最后一条消息也发送成功 更新消息页面
                    MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                    DialogHelper.dismissProgressDialog();
                    sendBroadcast(new Intent(com.iimm.miliao.broadcast.OtherBroadcast.SEND_MULTI_NOTIFY));
                    finish();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case BqShopActivity.BQ_CODE_REQUEST:  //表情商店回来:
                    if (data != null) {
                        boolean isRefresh = data.getBooleanExtra("isRefresh", false);
                        if (isRefresh) {
                            mChatBottomView.getMyBqBao(true, coreManager, myBqListener);
                        }
                    }
                    break;
                case REQUEST_MANAGE:
                    if (data != null) {
                        boolean isRefresh = data.getBooleanExtra("isRefresh", false);
                        if (isRefresh) {
                            mChatBottomView.getCustomBq();
                        }
                    }
                    break;
            }
        }


        if (requestCode == REQUEST_CODE_PICK_PHOTO && resultCode == RESULT_OK) {// 相册返回
            if (data != null) {
                boolean isOriginal = data.getBooleanExtra(PhotoPickerActivity.EXTRA_RESULT_ORIGINAL, false);
                album(data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT), isOriginal);
            } else {
                ToastUtil.showToast(this, R.string.c_photo_album_failed);
            }
        } else if (requestCode == REQUEST_CODE_SELECT_VIDE0 && resultCode == RESULT_OK) {// 选中视频返回
            if (data == null) {
                return;
            }
            String json = data.getStringExtra(AppConstant.EXTRA_VIDEO_LIST);
            List<VideoFile> fileList = JSON.parseArray(json, VideoFile.class);
            if (fileList == null || fileList.size() == 0) {
                // 不可到达，列表里有做判断，
                Reporter.unreachable();
            } else {
                for (VideoFile videoFile : fileList) {
                    String filePath = videoFile.getFilePath();
                    if (TextUtils.isEmpty(filePath)) {
                        // 不可到达，列表里有做过滤，
                        Reporter.unreachable();
                    } else {
                        File file = new File(filePath);
                        if (!file.exists()) {
                            // 不可到达，列表里有做过滤，
                            Reporter.unreachable();
                        } else {
                            sendVideo(file);
                        }
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_SELECT_LOCATE && resultCode == RESULT_OK) {// 选择位置的返回
            double latitude = data.getDoubleExtra(AppConstant.EXTRA_LATITUDE, 0);
            double longitude = data.getDoubleExtra(AppConstant.EXTRA_LONGITUDE, 0);
            String address = data.getStringExtra(AppConstant.EXTRA_ADDRESS);
            String snapshot = data.getStringExtra(AppConstant.EXTRA_SNAPSHOT);

            if (latitude != 0 && longitude != 0 && !TextUtils.isEmpty(address)
                    && !TextUtils.isEmpty(snapshot)) {
                sendLocate(latitude, longitude, address, snapshot);
            } else {
                ToastUtil.showToast(mContext, InternationalizationHelper.getString("JXLoc_StartLocNotice"));
            }
        }
    }

    // 单张图片压缩 拍照
    private void photograph(final File file) {
        Log.e("zq", "压缩前图片路径:" + file.getPath() + "压缩前图片大小:" + file.length() / 1024 + "KB");
        // 拍照出来的图片Luban一定支持，
        Luban.with(this)
                .load(file)
                .ignoreBy(100)     // 原图小于100kb 不压缩
                .filter(new CompressionPredicate() {
                    @Override
                    public boolean apply(String path) {
                        return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                    }
                })
                // .putGear(2)     // 设定压缩档次，默认三挡
                // .setTargetDir() // 指定压缩后的图片路径
                .setCompressListener(new OnCompressListener() { // 设置回调
                    @Override
                    public void onStart() {
                        Log.e("zq", "开始压缩");
                    }

                    @Override
                    public void onSuccess(File file) {
                        Log.e("zq", "压缩成功，压缩后图片位置:" + file.getPath() + "压缩后图片大小:" + file.length() / 1024 + "KB");
                        sendImage(file);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("zq", "压缩失败,原图上传");
                        sendImage(file);
                    }
                }).launch();// 启动压缩
    }

    // 多张图片压缩 相册
    private void album(ArrayList<String> stringArrayListExtra, boolean isOriginal) {
        if (isOriginal) {// 原图发送，不压缩
            Log.e("zq", "原图发送，不压缩，开始发送");
            for (int i = 0; i < stringArrayListExtra.size(); i++) {
                sendImage(new File(stringArrayListExtra.get(i)));
            }
            Log.e("zq", "原图发送，不压缩，发送结束");
            return;
        }

        List<File> fileList = new ArrayList<>();
        for (int i = 0; i < stringArrayListExtra.size(); i++) {
            // gif动图不压缩，
            if (stringArrayListExtra.get(i).endsWith("gif")) {
                fileList.add(new File(stringArrayListExtra.get(i)));
                stringArrayListExtra.remove(i);
            } else {
                // Luban只处理特定后缀的图片，不满足的不处理也不走回调，
                // 只能挑出来不压缩，
                List<String> lubanSupportFormatList = Arrays.asList("jpg", "jpeg", "png", "webp", "gif");
                boolean support = false;
                for (int j = 0; j < lubanSupportFormatList.size(); j++) {
                    if (stringArrayListExtra.get(i).endsWith(lubanSupportFormatList.get(j))) {
                        support = true;
                        break;
                    }
                }
                if (!support) {
                    fileList.add(new File(stringArrayListExtra.get(i)));
                    stringArrayListExtra.remove(i);
                }
            }
        }

        if (fileList.size() > 0) {
            for (File file : fileList) {// 不压缩的部分，直接发送
                sendImage(file);
            }
        }

        Luban.with(this)
                .load(stringArrayListExtra)
                .ignoreBy(100)// 原图小于100kb 不压缩
                .filter(new CompressionPredicate() {
                    @Override
                    public boolean apply(String path) {
                        return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                    }
                })
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(File file) {
                        sendImage(file);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }).launch();// 启动压缩
    }

    private ChatFaceView.CustomBqListener customBqListener = new ChatFaceView.CustomBqListener() {
        @Override
        public void onClickViewSendBq(boolean b, String custom, String customBq, String fileUrl, String s) {
           /* if (!TextUtils.isEmpty(fileUrl) && fileUrl.endsWith("gif")) {
                sendGif(fileUrl);
            } else*/
            if (!TextUtils.isEmpty(fileUrl)) {
                sendCollection(fileUrl);
            } else {
                sendCollection(fileUrl == null ? "" : fileUrl);
            }
        }

        @Override
        public void goCustomBqManageActivity() {
            Intent intent = new Intent(ChatActivityForSendGroup.this, CustomBqManageActivity.class);
            startActivityForResult(intent, REQUEST_MANAGE);
        }
    };
    private ChatFaceView.BqKeyBoardListener bqKeyBoardListener = v -> BqShopActivity.start(ChatActivityForSendGroup.this, BqShopActivity.BQ_CODE_REQUEST);
    private ChatFaceView.MyBqListener myBqListener = new ChatFaceView.MyBqListener() {
        @Override
        public void onClickViewSendBq(boolean b, String id, String emoMean, String fileUrl, String thumbnailUrl) {
           /* if (!TextUtils.isEmpty(fileUrl) && fileUrl.endsWith("gif")) {
                sendGif(fileUrl);
            } else */
            if (!TextUtils.isEmpty(fileUrl)) {
                sendCollection(fileUrl);
            } else {
                sendCollection(fileUrl == null ? "" : fileUrl);
            }
        }
    };

}
