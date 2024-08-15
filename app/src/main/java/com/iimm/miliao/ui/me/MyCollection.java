package com.iimm.miliao.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.adapter.PublicMessageAdapterCollection;
import com.iimm.miliao.audio_x.VoicePlayer;
import com.iimm.miliao.bean.circle.PublicMessage;
import com.iimm.miliao.bean.collection.CollectionEvery;
import com.iimm.miliao.downloader.Downloader;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.view.SelectionFrame;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import okhttp3.Call;

/**
 * Created by Administrator on 2017/10/20 0020.
 * 我的收藏
 */
public class MyCollection extends BaseActivity {
    private boolean isSendCollection;

    private PullToRefreshListView mPullToRefreshListView;
    private PublicMessageAdapterCollection mPublicMessageAdapter;
    private List<PublicMessage> mPublicMessage;
    private List<CollectionEvery> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_activity_my_collection);
        if (getIntent() != null) {
            isSendCollection = getIntent().getBooleanExtra("IS_SEND_COLLECTION", false);
        }
        mPublicMessage = new ArrayList<>();
        Downloader.getInstance().init(MyApplication.getInstance().mAppDir + File.separator + coreManager.getSelf().getUserId()
                + File.separator + Environment.DIRECTORY_MUSIC);
        initActionBar();
        initView();
        getMyCollectionList();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopVoiceOrVideo();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        //  tvTitle.setText(InternationalizationHelper.getString("JX_MyCollection"));
        tvTitle.setText(R.string.collection);
    }

    @Override
    public void onBackPressed() {
        stopVoiceOrVideo();
        super.onBackPressed();
    }

    private void stopVoiceOrVideo() {
        VoicePlayer.instance().stop();
        JCVideoPlayer.releaseAllVideos();
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        JCVideoPlayer.releaseAllVideos();
        if (mPublicMessageAdapter != null) {
            mPublicMessageAdapter.stopVoice();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 退出页面时关闭视频和语音，
        JCVideoPlayer.releaseAllVideos();
        if (mPublicMessageAdapter != null) {
            mPublicMessageAdapter.stopVoice();
        }
    }

    private void initView() {
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        mPublicMessageAdapter = new PublicMessageAdapterCollection(MyCollection.this, coreManager, mPublicMessage);
        if (isSendCollection) {
            mPublicMessageAdapter.setCollectionType(2);
        } else {
            mPublicMessageAdapter.setCollectionType(1);
        }

        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                getMyCollectionList();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });

        mPullToRefreshListView.getRefreshableView().setAdapter(mPublicMessageAdapter);
        mPullToRefreshListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isSendCollection) {
                    // 可能是因为HeaderView需要减1，
                    CollectionEvery collection = data.get(position - 1);
                    if (collection != null) {
                        SelectionFrame dialog = new SelectionFrame(MyCollection.this);
                        dialog.setSomething(null, getString(R.string.tip_confirm_send), new SelectionFrame.OnSelectionFrameClickListener() {
                            @Override
                            public void cancelClick() {
                            }

                            @Override
                            public void confirmClick() {
                                Intent intent = new Intent();
                                intent.putExtra("data", JSON.toJSONString(collection));
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        });
                        dialog.show();
                    } else {
                        Reporter.unreachable();
                        ToastUtil.showToast(mContext, R.string.tip_server_error);
                    }
                }
            }
        });
    }

    /**
     * 将url传给服务端，服务端
     */

    /**
     * 1.获取收藏列表
     * 2.将msg字段解析，变成List<ChatMessage>
     * 3.将List<ChatMessage>转换为List<PublishMessage>
     */
    public void getMyCollectionList() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());

        HttpUtils.get().url(coreManager.getConfig().Collection_LIST_OTHER)
                .params(params)
                .build()
                .execute(new ListCallback<CollectionEvery>(CollectionEvery.class) {
                    @Override
                    public void onResponse(ArrayResult<CollectionEvery> result) {
                        DialogHelper.dismissProgressDialog();
                        mPublicMessage.clear();
                        mPullToRefreshListView.onRefreshComplete();
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            afterGetData(result.getData());
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
     * 坑太大，不应该和朋友圈共用adapter, 共用实体类，
     */
    public void afterGetData(List<CollectionEvery> data) {
        this.data = data;
        for (int i = 0; i < data.size(); i++) {
            CollectionEvery collection = data.get(i);
            PublicMessage publicMessage = new PublicMessage();
            publicMessage.setUserId(coreManager.getSelf().getUserId());
            publicMessage.setNickName(coreManager.getSelf().getNickName());
            // 显示的时间应该为收藏这条消息的时间，而不是发送这条消息的时间
            publicMessage.setTime(data.get(i).getCreateTime());
            // 播放语音需要用到messageId,否则不需要设置
            publicMessage.setMessageId(collection.getEmojiId());
            // 文件名，
            publicMessage.setFileName(collection.getFileName());
            String name = collection.getFileName();
            try {
                // 服务器给的文件名可能包含路径，
                // TODO: PC端可能给反斜杠\, 没测，
                int lastIndex = name.lastIndexOf('/');
                publicMessage.setFileName(name.substring(lastIndex + 1));
            } catch (Exception e) {
                publicMessage.setFileName(name);
            }
            // 我的收藏专属id
            publicMessage.setEmojiId(data.get(i).getEmojiId());

            PublicMessage.Body body = new PublicMessage.Body();
            // 朋友圈收藏来的所有消息类型都有collectContent文本内容字段，
            body.setText(collection.getCollectContent());
            if (collection.getType() == CollectionEvery.TYPE_TEXT) {
                // 文本
                body.setType(PublicMessage.TYPE_TEXT);
                // 聊天收藏来的文字消息没有collectCntent字段，
                // 聊天和朋友圈收藏来的文字消息都有msg字段，
                collection.setCollectContent(collection.getMsg());
                body.setText(collection.getCollectContent());
            } else if (collection.getType() == CollectionEvery.TYPE_IMAGE) {
                // 图片
                body.setType(PublicMessage.TYPE_IMG);
                List<PublicMessage.Resource> images = new ArrayList<>();
                String allUrl = collection.getUrl();
                if (!TextUtils.isEmpty(allUrl)) {
                    for (String url : allUrl.split(",")) {
                        PublicMessage.Resource resource = new PublicMessage.Resource();
                        resource.setOriginalUrl(url);
                        images.add(resource);
                    }
                }
                body.setImages(images);
            } else if (collection.getType() == CollectionEvery.TYPE_VOICE) {
                // 语音
                body.setType(PublicMessage.TYPE_VOICE);
                List<PublicMessage.Resource> audios = new ArrayList<>();
                PublicMessage.Resource resource = new PublicMessage.Resource();
                resource.setLength(collection.getFileLength());
                resource.setSize(collection.getFileSize());
                resource.setOriginalUrl(collection.getUrl());
                audios.add(resource);
                body.setAudios(audios);
            } else if (collection.getType() == CollectionEvery.TYPE_VIDEO) {
                // 视频
                body.setType(PublicMessage.TYPE_VIDEO);
                // 视频封面
                /*List<PublicMessage.Resource> images = new ArrayList<>();
                PublicMessage.Resource resource1 = new PublicMessage.Resource();
                resource1.setOriginalUrl(message.getContent());
                images.add(resource1);
                body.setImages(images);*/
                // 视频源
                List<PublicMessage.Resource> videos = new ArrayList<>();
                PublicMessage.Resource resource2 = new PublicMessage.Resource();
                resource2.setOriginalUrl(collection.getUrl());
                resource2.setLength(collection.getFileLength());
                resource2.setSize(collection.getFileSize());
                videos.add(resource2);
                body.setVideos(videos);
            } else if (collection.getType() == CollectionEvery.TYPE_FILE) {
                // 文件
                body.setType(PublicMessage.TYPE_FILE);
                List<PublicMessage.Resource> files = new ArrayList<>();
                PublicMessage.Resource resource2 = new PublicMessage.Resource();
                resource2.setOriginalUrl(collection.getUrl());
                resource2.setLength(collection.getFileLength());
                resource2.setSize(collection.getFileSize());
                files.add(resource2);
                body.setFiles(files);
            }
            publicMessage.setBody(body);
            mPublicMessage.add(publicMessage);
        }
        mPublicMessageAdapter.setData(mPublicMessage);
    }
}
