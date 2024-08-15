package com.iimm.miliao.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.signature.StringSignature;
import com.gyf.immersionbar.ImmersionBar;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.adapter.MessageEventHongdian;
import com.iimm.miliao.adapter.PublicMessageAdapter;
import com.iimm.miliao.bean.EventAvatarUploadSuccess;
import com.iimm.miliao.bean.MyZan;
import com.iimm.miliao.bean.circle.Comment;
import com.iimm.miliao.bean.circle.PublicMessage;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.CircleMessageDao;
import com.iimm.miliao.db.dao.MyZanDao;
import com.iimm.miliao.db.dao.UserAvatarDao;
import com.iimm.miliao.db.dao.UserDao;
import com.iimm.miliao.downloader.Downloader;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.EasyFragment;
import com.iimm.miliao.ui.circle.BusinessCircleSearchActivity;
import com.iimm.miliao.ui.circle.MessageEventComment;
import com.iimm.miliao.ui.circle.MessageEventNotifyDynamic;
import com.iimm.miliao.ui.circle.MessageEventReply;
import com.iimm.miliao.ui.circle.SelectPicPopupWindow;
import com.iimm.miliao.ui.circle.range.NewZanActivity;
import com.iimm.miliao.ui.circle.range.SendAudioActivity;
import com.iimm.miliao.ui.circle.range.SendFileActivity;
import com.iimm.miliao.ui.circle.range.SendShuoshuoActivity;
import com.iimm.miliao.ui.circle.range.SendVideoActivity;
import com.iimm.miliao.ui.mucfile.UploadingHelper;
import com.iimm.miliao.ui.mucfile.XfileUtils;
import com.iimm.miliao.ui.other.BasicInfoActivity;
import com.iimm.miliao.util.CameraUtil;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.LogUtils;
import com.iimm.miliao.util.SkinUtils;
import com.iimm.miliao.util.StringUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.util.permission.AndPermissionUtils;
import com.iimm.miliao.util.permission.OnPermissionClickListener;
import com.iimm.miliao.view.CheckableImageView;
import com.iimm.miliao.view.TrillCommentInputDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.MessageEvent;
import fm.jiecao.jcvideoplayer_lib.MessageRefreshEvent;
import okhttp3.Call;

/**
 * 朋友圈的Fragment search
 * Created by Administrator
 */

public class DiscoverFragment extends EasyFragment implements OnPermissionClickListener {
    private static final int REQUEST_CODE_SEND_MSG = 1;
    private static final int REQUEST_CODE_PICK_PHOTO = 2;
    private static int PAGER_SIZE = 10;
    private String mUserId;
    private String mUserName;
    private RelativeLayout mToolbarRl;
    private View mPlaceholder;
    private View mLine;
    private TextView mTvTitle;
    private CheckableImageView mIvTitleRight, mIvightRight;
    private CheckableImageView mIvTitleLeft;
    private SelectPicPopupWindow menuWindow;
    // 头部
    private View mHeadView;
    private ImageView ivHeadBg, ivHead;
    // 通知...
    private LinearLayout mTipLl;
    private ImageView mTipIv;
    private TextView mTipTv;
    // 页面
    private PullToRefreshListView mListView;
    private PublicMessageAdapter mAdapter;
    private List<PublicMessage> mMessages = new ArrayList<>();
    private boolean more;
    private String messageId;
    // 为弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            if (menuWindow != null) {
                // 顶部一排按钮复用这个listener, 没有menuWindow,
                menuWindow.dismiss();
            }
            Intent intent = new Intent();
            switch (v.getId()) {
                case R.id.btn_send_picture:
                    // 发表图文，
                    intent.setClass(getActivity(), SendShuoshuoActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.btn_send_voice:
                    // 发表语音
                    intent.setClass(getActivity(), SendAudioActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.btn_send_video:
                    // 发表视频
                    intent.setClass(getActivity(), SendVideoActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.btn_send_file:
                    // 发表文件
                    intent.setClass(getActivity(), SendFileActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.new_comment:
                    // 最新评论&赞
                    Intent intent2 = new Intent(getActivity(), NewZanActivity.class);
                    intent2.putExtra("OpenALL", true);
                    startActivity(intent2);
                    mTipLl.setVisibility(View.GONE);
                    EventBus.getDefault().post(new MessageEventHongdian(0));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_discover;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {


        initActionBar();
        Downloader.getInstance().init(MyApplication.getInstance().mAppDir + File.separator + coreManager.getSelf().getUserId()
                + File.separator + Environment.DIRECTORY_MOVIES);// 初始化视频下载目录
        initViews();
        initData();
    }

    @Override
    public void onPause() {
        super.onPause();
        JCVideoPlayer.releaseAllVideos();
        if (mAdapter != null) {
            mAdapter.stopVoice();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 退出页面时关闭视频和语音，
        JCVideoPlayer.releaseAllVideos();
        if (mAdapter != null) {
            mAdapter.stopVoice();
        }
        EventBus.getDefault().unregister(this);
    }

    private void initActionBar() {

        mLine = findViewById(R.id.line_v);
        mPlaceholder = findViewById(R.id.placeholder_v);
        ImmersionBar.with(this)
                .statusBarView(mPlaceholder)
                .statusBarDarkFont(true, 0.2f)
                .init();
        mToolbarRl = findViewById(R.id.tool_bar_rl);
        int skinColor;
        int colorName = SkinUtils.getSkin(getContext()).getColorName();
        if (colorName == R.string.skin_simple_white) {
            skinColor = ContextCompat.getColor(getContext(), R.color.tb_bg_skin_simple_white);
        } else {
            skinColor = SkinUtils.getSkin(getContext()).getPrimaryColor();
        }
        mPlaceholder.setBackgroundColor(skinColor);
        mToolbarRl.setBackgroundColor(skinColor);
        mIvTitleLeft = findViewById(R.id.iv_title_left);

        if (coreManager.getConfig().newUi) {
            //左侧返回
            mIvTitleLeft.setOnClickListener(v -> requireActivity().finish());
        } else {
            mIvTitleLeft.setVisibility(View.GONE);
        }
        mTvTitle = ((TextView) findViewById(R.id.tv_title_center));
        mIvTitleRight = findViewById(R.id.iv_title_right);
        mIvightRight = findViewById(R.id.iv_title_right_right);

        if (Constants.SUPPORT_DISCOVER_SEARCH) {
            mIvightRight.setVisibility(View.VISIBLE);
        } else {
            mIvightRight.setVisibility(View.INVISIBLE);
        }
        //搜索朋友圈
        mIvightRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UiUtils.isNormalClick(v)){
                    BusinessCircleSearchActivity.showBusinessCircleSearchActivity(getContext());
                }
            }
        });
        //右侧发布
        mIvTitleRight.setOnClickListener(v -> {
            menuWindow = new SelectPicPopupWindow(getActivity(), itemsOnClick);
            menuWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            menuWindow.showAsDropDown(v,
                    -(menuWindow.getContentView().getMeasuredWidth() - v.getWidth() / 2 - 40),
                    0 + 10);
            menuWindow.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.popu_bg));
        });
    }

    public void initViews() {
        more = true;
        mUserId = coreManager.getSelf().getUserId();
        mUserName = coreManager.getSelf().getNickName();

        // ---------------------------初始化头部-----------------------
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mHeadView = inflater.inflate(R.layout.space_cover_view, null);
        mHeadView.findViewById(R.id.ll_btn_send).setVisibility(View.GONE);
        TextView mName = mHeadView.findViewById(R.id.name_tv);
        mName.setText(coreManager.getSelf().getNickName());
        ivHeadBg = (ImageView) mHeadView.findViewById(R.id.cover_img);
        //背景图片点击
        ivHeadBg.setOnClickListener(v -> {
            if (UiUtils.isNormalClick(v)) {
                AndPermissionUtils.albumPermission(getContext(), this);
            }
        });
        ivHead = (ImageView) mHeadView.findViewById(R.id.avatar_img);
        //头像点击
        ivHead.setOnClickListener(v -> {
            if (UiUtils.isNormalClick(v)) {
                Intent intent = new Intent(getActivity(), BasicInfoActivity.class);
                intent.putExtra(AppConstant.EXTRA_USER_ID, mUserId);
                startActivity(intent);
            }
        });

        displayAvatar();

        mTipLl = (LinearLayout) mHeadView.findViewById(R.id.tip_ll);
        mTipIv = (ImageView) mHeadView.findViewById(R.id.tip_avatar);
        mTipTv = (TextView) mHeadView.findViewById(R.id.tip_content);
        //新消息点击
        mTipLl.setOnClickListener(v -> {
            if (!UiUtils.isNormalClick(v)) {
                return;
            }
            mTipLl.setVisibility(View.GONE);
            EventBus.getDefault().post(new MessageEventHongdian(0));

            Intent intent = new Intent(getActivity(), NewZanActivity.class);
            intent.putExtra("OpenALL", false); // 是否展示全部还是单条
            startActivity(intent);
        });

        // ---------------------------初始化主视图-----------------------
        mListView = (PullToRefreshListView) findViewById(R.id.discover_listview);
        mListView.getRefreshableView().addHeaderView(mHeadView);

        mLine.getBackground().mutate().setAlpha(0);
        mPlaceholder.getBackground().mutate().setAlpha(0);
        mToolbarRl.getBackground().mutate().setAlpha(0);
        mTvTitle.setAlpha(0);
        mIvTitleLeft.setChecked(false);
        mIvTitleRight.setChecked(false);
        mIvightRight.setChecked(false);


        //(头部view高度-(状态栏+标题栏))
        float headHeight = XfileUtils.measureViewHeight(mHeadView) - (XfileUtils.measureViewHeight(mToolbarRl) + ImmersionBar.getStatusBarHeight(this));
        float mHeight = (255f / headHeight);


        mListView.getRefreshableView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {


                View view1 = mListView.getRefreshableView().getChildAt(0);
                if (view1 != null) {
                    if (firstVisibleItem == 1) {
                        int hei = Math.abs(view1.getTop());
                        if (hei > headHeight) {
                            mPlaceholder.getBackground().mutate().setAlpha(255);
                            mToolbarRl.getBackground().mutate().setAlpha(255);
                            mLine.getBackground().mutate().setAlpha(255);
                            mTvTitle.setAlpha(1);
                            mIvTitleLeft.setChecked(true);
                            mIvTitleRight.setChecked(true);
                            mIvightRight.setChecked(true);
                        } else {
                            mPlaceholder.getBackground().mutate().setAlpha((int) (mHeight * hei));
                            mToolbarRl.getBackground().mutate().setAlpha((int) (mHeight * hei));
                            mLine.getBackground().mutate().setAlpha((int) (mHeight * hei));

                            if (hei > (headHeight / 2)) {
                                mIvTitleLeft.setChecked(true);
                                mIvTitleRight.setChecked(true);
                                mIvightRight.setChecked(true);
                                mTvTitle.setAlpha(1);
                            } else {
                                mTvTitle.setAlpha(0);
                                mIvTitleLeft.setChecked(false);
                                mIvTitleRight.setChecked(false);
                                mIvightRight.setChecked(false);
                            }
                        }
                    } else if (firstVisibleItem == 0) {
                        mLine.getBackground().mutate().setAlpha(0);
                        mPlaceholder.getBackground().mutate().setAlpha(0);
                        mToolbarRl.getBackground().mutate().setAlpha(0);
                        mTvTitle.setAlpha(0);
                        mIvTitleLeft.setChecked(false);
                        mIvTitleRight.setChecked(false);
                        mIvightRight.setChecked(false);
                    } else {
                        mPlaceholder.getBackground().mutate().setAlpha(255);
                        mToolbarRl.getBackground().mutate().setAlpha(255);
                        mLine.getBackground().mutate().setAlpha(255);
                        mTvTitle.setAlpha(1);
                        mIvTitleLeft.setChecked(true);
                        mIvTitleRight.setChecked(true);
                        mIvightRight.setChecked(true);
                    }
                }
            }
        });

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestData(true);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestData(false);
            }
        });

        EventBus.getDefault().

                register(this);

        findViewById(R.id.btn_send_picture).

                setOnClickListener(itemsOnClick);

        findViewById(R.id.btn_send_voice).

                setOnClickListener(itemsOnClick);

        findViewById(R.id.btn_send_video).

                setOnClickListener(itemsOnClick);

        findViewById(R.id.btn_send_file).

                setOnClickListener(itemsOnClick);

        findViewById(R.id.new_comment).

                setOnClickListener(itemsOnClick);

    }

    private void changeBackgroundImage() {
        CameraUtil.pickImageSimple(this, REQUEST_CODE_PICK_PHOTO);
    }

    private void updateBackgroundImage(String path) {
        File bg = new File(path);
        if (!bg.exists()) {
            LogUtils.log(path);
            Reporter.unreachable();
            ToastUtil.showToast(requireContext(), R.string.image_not_found);
            return;
        }
        DialogHelper.showDefaulteMessageProgressDialog(requireActivity());
        UploadingHelper.upFile(getActivity(), coreManager, coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), new File(path), new UploadingHelper.OnUpFileListener() {
            @Override
            public void onSuccess(String url, String filePath) {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", coreManager.getSelfStatus().accessToken);
                params.put("msgBackGroundUrl", url);

                HttpUtils.get().url(coreManager.getConfig().USER_UPDATE)
                        .params(params)
                        .build()
                        .execute(new BaseCallback<Void>(Void.class) {

                            @Override
                            public void onResponse(ObjectResult<Void> result) {
                                DialogHelper.dismissProgressDialog();
                                coreManager.getSelf().setMsgBackGroundUrl(url);
                                UserDao.getInstance().updateMsgBackGroundUrl(coreManager.getSelf().getUserId(), url);
                                if (getContext() == null) {
                                    return;
                                }
                                displayAvatar();
                            }

                            @Override
                            public void onError(Call call, Exception e) {
                                DialogHelper.dismissProgressDialog();
                                if (getContext() == null) {
                                    return;
                                }
                                ToastUtil.showErrorNet(requireContext());
                            }
                        });
            }

            @Override
            public void onFailure(String err, String filePath) {
                DialogHelper.dismissProgressDialog();
                if (getContext() == null) {
                    return;
                }
                ToastUtil.showErrorNet(requireContext());
            }
        });

    }

    public void initData() {
        mAdapter = new PublicMessageAdapter(getActivity(), coreManager, mMessages);
        mListView.getRefreshableView().setAdapter(mAdapter);
        requestData(true);
    }

    private void requestData(boolean isPullDownToRefresh) {
        if (isPullDownToRefresh) {// 上拉刷新
            updateTip();
            messageId = null;
            if (mMessages != null) {
                mMessages.clear();
            }
            more = true;
        }

        if (!more) {
            // ToastUtil.showToast(getContext(), getString(R.string.tip_last_item));
            mListView.setReleaseLabel(getString(R.string.tip_last_item));
            mListView.setRefreshingLabel(getString(R.string.tip_last_item));
            refreshComplete();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("pageSize", String.valueOf(PAGER_SIZE));
        if (messageId != null) {
            params.put("messageId", messageId);
        }

        HttpUtils.get().url(coreManager.getConfig().MSG_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<PublicMessage>(PublicMessage.class) {
                    @Override
                    public void onResponse(ArrayResult<PublicMessage> result) {
                        if (getContext() != null && Result.checkSuccess(requireContext(), result)) {
                            List<PublicMessage> data = result.getData();
                            if (data != null && data.size() > 0) {
                                mMessages.addAll(data);
                                // 记录最后一条说说的id
                                messageId = data.get(data.size() - 1).getMessageId();
                                if (data.size() == PAGER_SIZE) {
                                    more = true;
                                } else {
                                    // 服务器返回未满10条，下拉不在去请求
                                    more = false;
                                }
                            } else {
                                // 服务器未返回数据，下拉不再去请求
                                more = false;
                            }
                            mAdapter.notifyDataSetChanged();
                            refreshComplete();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (getContext() != null) {
                            ToastUtil.showNetError(requireContext());
                            refreshComplete();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_SEND_MSG) {
            // 发布说说成功,刷新Fragment
            String messageId = data.getStringExtra(AppConstant.EXTRA_MSG_ID);
            CircleMessageDao.getInstance().addMessage(mUserId, messageId);
            requestData(true);
        } else if (requestCode == REQUEST_CODE_PICK_PHOTO) {
            if (data != null && data.getData() != null) {
                String path = CameraUtil.getImagePathFromUri(requireContext(), data.getData());
                updateBackgroundImage(path);
            } else {
                ToastUtil.showToast(requireContext(), R.string.c_photo_album_failed);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventAvatarUploadSuccess message) {
        if (message.event) {// 头像更新了，但该界面没有被销毁，不会去重新加载头像，所以这里更新一下
            displayAvatar();
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageRefreshEvent message) {
        if (message.isRefresh()) {
            requestData(true);
        }
    }

    public void displayAvatar() {
        // 加载小头像，
        AvatarHelper.getInstance().displayAvatar(mUserId, ivHead, true);
        // 优先加载user信息中的背景图片，失败就加载头像，
        String bg = coreManager.getSelf().getMsgBackGroundUrl();
        if (TextUtils.isEmpty(bg)) {
            realDisplayAvatar();
        }
        Glide.with(requireContext().getApplicationContext())
                .load(bg)
                .placeholder(R.drawable.avatar_normal)
                .dontAnimate()
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        ivHeadBg.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        realDisplayAvatar();
                    }
                });

    }

    private void realDisplayAvatar() {
        final String mOriginalUrl = AvatarHelper.getAvatarUrl(mUserId, false);
        if (!TextUtils.isEmpty(mOriginalUrl)) {
            String time = UserAvatarDao.getInstance().getUpdateTime(mUserId);

            Glide.with(MyApplication.getContext())
                    .load(mOriginalUrl)
                    .placeholder(R.drawable.avatar_normal)
                    .signature(new StringSignature(time))
                    .dontAnimate()
                    .error(R.drawable.avatar_normal)
                    .into(new SimpleTarget<GlideDrawable>() {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            ivHeadBg.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            Log.e("zq", "加载原图失败：" + mOriginalUrl);
                        }
                    });
        } else {
            Log.e("zq", "未获取到原图地址");// 基本上不会走这里
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEvent message) {
        if (message.message.equals("prepare")) {// 准备播放视频，关闭语音播放
            mAdapter.stopVoice();
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageEventNotifyDynamic message) {
        // 收到赞 || 评论 || 提醒我看 协议 刷新页面
        requestData(true);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventComment message) {

        Log.e("zx", "helloEventBus: " + message.pbmessage.getIsAllowComment());
        if (message.event.equals("Comment") && message.pbmessage.getIsAllowComment() == 0) {
            TrillCommentInputDialog trillCommentInputDialog = new TrillCommentInputDialog(getActivity(), InternationalizationHelper.getString("ENTER_PINLUNT"),
                    str -> {
                        Comment mComment = new Comment();
                        Comment comment = mComment.clone();
                        if (comment == null)
                            comment = new Comment();
                        comment.setBody(str);
                        comment.setUserId(mUserId);
                        comment.setNickName(mUserName);
                        comment.setTime(TimeUtils.time_current_time());
                        addComment(message, comment);
                    });
            Window window = trillCommentInputDialog.getWindow();
            if (window != null) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);// 软键盘弹起
                trillCommentInputDialog.show();
            }
        } else {
            Toast.makeText(getContext(), "禁止评论", Toast.LENGTH_SHORT).show();
        }

    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventReply message) {
        if (message.event.equals("Reply")) {
            TrillCommentInputDialog trillCommentInputDialog = new TrillCommentInputDialog(getActivity(), InternationalizationHelper.getString("JX_Reply") + "：" + message.comment.getNickName(),
                    str -> {
                        Comment mComment = new Comment();
                        Comment comment = mComment.clone();
                        if (comment == null)
                            comment = new Comment();
                        comment.setToUserId(message.comment.getUserId());
                        comment.setToNickname(message.comment.getNickName());
                        comment.setToBody(message.comment.getToBody());
                        comment.setBody(str);
                        comment.setUserId(mUserId);
                        comment.setNickName(mUserId);
                        comment.setTime(TimeUtils.time_current_time());
                        Reply(message.id, comment);
                    });
            Window window = trillCommentInputDialog.getWindow();
            if (window != null) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);// 软键盘弹起
                trillCommentInputDialog.show();
            }
        }
    }

    /**
     * 停止刷新动画
     */
    private void refreshComplete() {
        mListView.postDelayed(() -> mListView.onRefreshComplete(), 200);
    }

    public void updateTip() {
        int tipCount = MyZanDao.getInstance().getZanSize(coreManager.getSelf().getUserId());
        if (tipCount == 0) {
            mTipLl.setVisibility(View.GONE);
            EventBus.getDefault().post(new MessageEventHongdian(0));
        } else {
            List<MyZan> zanList = MyZanDao.getInstance().queryZan(coreManager.getSelf().getUserId());
            if (zanList == null || zanList.size() == 0) {
                return;
            }
            MyZan zan = zanList.get(zanList.size() - 1);
            AvatarHelper.getInstance().displayAvatar(zan.getFromUserId(), mTipIv, true);
            mTipTv.setText(tipCount + InternationalizationHelper.getString("JX_PieceNewMessage"));
            mTipLl.setVisibility(View.VISIBLE);
            EventBus.getDefault().post(new MessageEventHongdian(tipCount));
        }
    }

    private void addComment(MessageEventComment message, final Comment comment) {
        String messageId = message.id;
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", messageId);
        if (comment.isReplaySomeBody()) {
            params.put("toUserId", comment.getToUserId() + "");
            params.put("toNickname", comment.getToNickname());
            params.put("toBody", comment.getToBody());
        }
        params.put("body", comment.getBody());

        HttpUtils.get().url(coreManager.getConfig().MSG_COMMENT_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        // 评论成功
                        message.pbmessage.setCommnet(message.pbmessage.getCommnet() + 1);
                        if (message.view.getTag() == message.pbmessage) {
                            PublicMessageAdapter.CommentAdapter adapter = (PublicMessageAdapter.CommentAdapter) message.view.getAdapter();
                            adapter.addComment(comment);
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getActivity());
                    }
                });
    }

    /**
     * 回复
     */
    private void Reply(final int position, final Comment comment) {
        final PublicMessage message = mMessages.get(position);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", message.getMessageId());

        if (!TextUtils.isEmpty(comment.getToUserId())) {
            params.put("toUserId", comment.getToUserId());
        }
        if (!TextUtils.isEmpty(comment.getToNickname())) {
            params.put("toNickname", comment.getToNickname());
        }
        params.put("body", comment.getBody());

        HttpUtils.get().url(coreManager.getConfig().MSG_COMMENT_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        List<Comment> comments = message.getComments();
                        if (comments == null) {
                            comments = new ArrayList<>();
                            message.setComments(comments);
                        }
                        comment.setCommentId(result.getData());
                        comments.add(0, comment);
                        requestData(true);
                        showToCurrent(result.getData());
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getActivity());
                    }
                });
    }

    /**
     * 定位到评论位置
     */
    public void showToCurrent(String mCommentId) {
        int pos = -1;
        for (int i = 0; i < mMessages.size(); i++) {
            if (StringUtils.strEquals(mCommentId, mMessages.get(i).getMessageId())) {
                pos = i + 2;
                break;
            }
        }
        // 如果找到就定位到这条说说
        if (pos != -1) {
            mListView.getRefreshableView().setSelection(pos);
        }
    }

    @Override
    public void onSuccess() {
        changeBackgroundImage();
    }

    @Override
    public void onFailure(List<String> data) {

    }
}
