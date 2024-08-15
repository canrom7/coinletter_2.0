package com.iimm.miliao.ui.message;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.qrcode.utils.DecodeUtils;
import com.google.zxing.Result;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.adapter.ChatOverviewAdapter;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.broadcast.OtherBroadcast;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.FileUtil;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.view.SaveWindow;
import com.iimm.miliao.xmpp.util.ImFileDownload;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cn.imeina.screeencaptureshare.ScreenCaptureShareActivity;
import me.kareluo.imaging.IMGEditActivity;

/**
 * @author WangQX
 * @date 2019/8/6 0006 15:04
 * description:聊天界面 图片预览
 */
public class ChatOverviewActivity extends BaseActivity {
    public static final int REQUEST_IMAGE_EDIT = 1;

    private ViewPager mViewPager;
    private ChatOverviewAdapter mChatOverviewAdapter;

    private List<ChatMessage> mChatMessages = new ArrayList<>();
    private int mFirstShowPosition;
    private ChatMessage mCurrentChatMessage;

    private String mCurrentShowUrl;
    private String mEditedPath;
    private SaveWindow mSaveWindow;
    private My_BroadcastReceivers my_broadcastReceiver = new My_BroadcastReceivers();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_overview);
        String imageChatMessageListStr = getIntent().getStringExtra("imageChatMessageList");
        mChatMessages = JSON.parseArray(imageChatMessageListStr, ChatMessage.class);
        mFirstShowPosition = getIntent().getIntExtra("imageChatMessageList_current_position", 0);
        getCurrentShowUrl(mFirstShowPosition);
        initView();
        register();
    }

    private void initView() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mViewPager = findViewById(R.id.chat_overview_vp);
        mChatOverviewAdapter = new ChatOverviewAdapter(this, mChatMessages);
        mViewPager.setAdapter(mChatOverviewAdapter);
        mViewPager.setCurrentItem(mFirstShowPosition);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                getCurrentShowUrl(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    private void getCurrentShowUrl(int position) {
        mCurrentChatMessage = mChatMessages.get(position);
        if (!TextUtils.isEmpty(mCurrentChatMessage.getFilePath()) && FileUtil.isExist(mCurrentChatMessage.getFilePath())) {
            mCurrentShowUrl = mCurrentChatMessage.getFilePath();
        } else {
            mCurrentShowUrl = mCurrentChatMessage.getContent();
        }
    }

    private void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(com.iimm.miliao.broadcast.OtherBroadcast.singledown);
        filter.addAction(com.iimm.miliao.broadcast.OtherBroadcast.longpress);
        registerReceiver(my_broadcastReceiver, filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_EDIT:
                    mCurrentShowUrl = mEditedPath;
                    ChatMessage chatMessage = mChatMessages.get(mViewPager.getCurrentItem());
                    chatMessage.setFilePath(mCurrentShowUrl);
                    mChatMessages.set(mViewPager.getCurrentItem(), chatMessage);
                    mChatOverviewAdapter.refreshItem(mCurrentShowUrl, mViewPager.getCurrentItem());
                    break;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    class My_BroadcastReceivers extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(OtherBroadcast.singledown)) {
                finish();
            } else if (intent.getAction().equals(OtherBroadcast.longpress)) {
                // 长按屏幕，弹出菜单
                mSaveWindow = new SaveWindow(ChatOverviewActivity.this, mCurrentChatMessage, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSaveWindow.dismiss();
                        switch (v.getId()) {
                            case R.id.save_image:
                                if (mCurrentChatMessage != null) {
                                    String filePath = mCurrentChatMessage.getFilePath();
                                    if (mCurrentChatMessage.isMySend() || mCurrentChatMessage.isDownload()) {//本地图片路径,如果不是原图，则这个路径是 压缩图的路径
                                        if (!TextUtils.isEmpty(filePath)) {
                                            File file = new File(filePath);
                                            if (file.exists()) {
                                                //更新图库
                                                FileUtil.updatePhotoMedia(new File(filePath));
                                                String content = "已保存至" + filePath;
                                                ToastUtil.showLongToast(content);
                                            } else {
                                                if (!TextUtils.isEmpty(mCurrentChatMessage.getContent())) {
                                                    //重新下载
                                                    ImFileDownload.downloadFileIfNeeded(mCurrentChatMessage, null, true);
                                                } else {
                                                    //异常
                                                    ToastUtil.showToast(R.string.tip_save_image_failed);
                                                }
                                            }
                                        } else {
                                            if (!TextUtils.isEmpty(mCurrentChatMessage.getContent())) {
                                                //重新下载
                                                ImFileDownload.downloadFileIfNeeded(mCurrentChatMessage, null, true);
                                            } else {//异常
                                                ToastUtil.showToast(R.string.tip_save_image_failed);
                                            }
                                        }
                                    } else {
                                        if (!TextUtils.isEmpty(mCurrentChatMessage.getContent())) {
                                            //重新下载
                                            ImFileDownload.downloadFileIfNeeded(mCurrentChatMessage, null, true);
                                        } else {//异常
                                            ToastUtil.showToast(R.string.tip_save_image_failed);
                                        }
                                    }
                                }
                                break;
                            case R.id.edit_image:
                                Glide.with(ChatOverviewActivity.this)
                                        .load(mCurrentShowUrl)
                                        .downloadOnly(new SimpleTarget<File>() {
                                            @Override
                                            public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                                                mEditedPath = FileUtil.createImageFileForEdit().getAbsolutePath();
                                                IMGEditActivity.startForResult(ChatOverviewActivity.this, Uri.fromFile(resource), mEditedPath, REQUEST_IMAGE_EDIT);
                                            }
                                        });
                                break;
                            case R.id.edit_image1:
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            File file = null;
                                            try {
                                                file = Glide.with(ChatOverviewActivity.this)
                                                         .load(mCurrentShowUrl)
                                                         .downloadOnly(getScreenWidth(mContext),getScreenHeight(mContext)).get();
                                                if(file!=null){
                                                   ScreenCaptureShareActivity.screenCapture(v, file.getAbsolutePath());
                                                }
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            }


                                        }
                                    }).start();


                                break;
                            case R.id.identification_qr_code:
                                // 识别图中二维码
                                Glide.with(ChatOverviewActivity.this)
                                        .load(mCurrentShowUrl)
                                        .downloadOnly(new SimpleTarget<File>() {
                                            @Override
                                            public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {

                                                AsyncUtils.doAsync(mContext, t -> {
                                                    Reporter.post("二维码解码失败，" + resource.getCanonicalPath(), t);
                                                    runOnUiThread(() -> {
                                                        ToastUtil.showToast(ChatOverviewActivity.this, R.string.decode_failed);
                                                    });
                                                }, t -> {
                                                    // 做些预处理提升扫码成功率，
                                                    // 预读一遍获取图片比例，使用inSampleSize压缩图片分辨率到恰到好处，
                                                    Uri decodeUri = Uri.fromFile(resource);
                                                    final Result result = DecodeUtils.decodeFromPicture(DecodeUtils.compressPicture(t.getRef(), decodeUri));
                                                    t.uiThread(c -> {
                                                        if (result != null && !TextUtils.isEmpty(result.getText())) {
                                                            HandleQRCodeScanUtil.handleScanResult(mContext, result.getText());
                                                        } else {
                                                            setImage(resource);
                                                        }
                                                    });
                                                });



                                            }
                                        });
                                break;
                        }
                    }
                });
                mSaveWindow.show();
            }
        }
    }

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }
    public void setImage(File resource){
        AsyncUtils.doAsync(mContext, t -> {
            Reporter.post("二维码解码失败，" + resource.getCanonicalPath(), t);
            runOnUiThread(() -> {
                ToastUtil.showToast(ChatOverviewActivity.this, R.string.decode_failed);
            });
        }, t -> {
            // 做些预处理提升扫码成功率，
            // 预读一遍获取图片比例，使用inSampleSize压缩图片分辨率到恰到好处，
            Uri decodeUri = Uri.fromFile(resource);
            final Result result = DecodeUtils.decodeFromPicture(DecodeUtils.compressPicture1(t.getRef(), decodeUri));
            t.uiThread(c -> {
                if (result != null && !TextUtils.isEmpty(result.getText())) {
                    HandleQRCodeScanUtil.handleScanResult(mContext, result.getText());
                } else {
                    setImage1(resource);
                }
            });
        });
    }

    public void setImage1(File resource){
        AsyncUtils.doAsync(mContext, t -> {
            Reporter.post("二维码解码失败，" + resource.getCanonicalPath(), t);
            runOnUiThread(() -> {
                ToastUtil.showToast(ChatOverviewActivity.this, R.string.decode_failed);
            });
        }, t -> {
            // 做些预处理提升扫码成功率，
            // 预读一遍获取图片比例，使用inSampleSize压缩图片分辨率到恰到好处，
            Uri decodeUri = Uri.fromFile(resource);
            final Result result = DecodeUtils.decodeFromPicture(DecodeUtils.compressPicture2(t.getRef(), decodeUri));
            t.uiThread(c -> {
                if (result != null && !TextUtils.isEmpty(result.getText())) {
                    HandleQRCodeScanUtil.handleScanResult(mContext, result.getText());
                } else {
                    ToastUtil.showToast(ChatOverviewActivity.this, R.string.decode_failed);
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (my_broadcastReceiver != null) {
            unregisterReceiver(my_broadcastReceiver);
        }
    }
}
