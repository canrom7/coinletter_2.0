package com.iimm.miliao.ui.circle;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.signature.StringSignature;
import com.gyf.immersionbar.ImmersionBar;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.iimm.miliao.AppConfig;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.adapter.PublicMessageAdapter;
import com.iimm.miliao.bean.circle.Comment;
import com.iimm.miliao.bean.circle.PublicMessage;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.CircleMessageDao;
import com.iimm.miliao.db.dao.UserAvatarDao;
import com.iimm.miliao.db.dao.UserDao;
import com.iimm.miliao.downloader.Downloader;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.FileDataHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.circle.range.NewZanActivity;
import com.iimm.miliao.ui.circle.range.SendAudioActivity;
import com.iimm.miliao.ui.circle.range.SendFileActivity;
import com.iimm.miliao.ui.circle.range.SendShuoshuoActivity;
import com.iimm.miliao.ui.circle.range.SendVideoActivity;
import com.iimm.miliao.ui.circle.util.RefreshListImp;
import com.iimm.miliao.ui.emoji.BqShopActivity;
import com.iimm.miliao.ui.emoji.CustomBqManageActivity;
import com.iimm.miliao.ui.mucfile.UploadingHelper;
import com.iimm.miliao.ui.mucfile.XfileUtils;
import com.iimm.miliao.ui.other.BasicInfoActivity;
import com.iimm.miliao.util.CameraUtil;
import com.iimm.miliao.util.LogUtils;
import com.iimm.miliao.util.SkinUtils;
import com.iimm.miliao.util.StringUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.util.permission.AndPermissionUtils;
import com.iimm.miliao.util.permission.OnPermissionClickListener;
import com.iimm.miliao.view.ChatFaceView;
import com.iimm.miliao.view.CheckableImageView;
import com.iimm.miliao.view.PMsgBottomView;
import com.iimm.miliao.view.ResizeLayout;
import com.iimm.miliao.volley.ArrayResult;
import com.iimm.miliao.volley.StringJsonArrayRequest;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JVCideoPlayerStandardSecond;
import okhttp3.Call;

import static com.iimm.miliao.ui.message.ChatActivity.REQUEST_MANAGE;

/**
 * 我的商务圈
 */
public class BusinessCircleActivity extends BaseActivity implements showCEView, RefreshListImp, OnPermissionClickListener {
    private static final int REQUEST_CODE_SEND_MSG = 1;
    private static final int REQUEST_CODE_PICK_PHOTO = 2;
    // 自定义的弹出框类
    SelectPicPopupWindow menuWindow;
    /**
     * 接口,调用外部类的方法,让应用不可见时停止播放声音
     */
    ListenerAudio listener;
    CommentReplyCache mCommentReplyCache = null;
    private int mType;
    /* mPageIndex仅用于商务圈情况下 */
    private int mPageIndex = 0;
    private PullToRefreshListView mPullToRefreshListView;
    /* 封面视图 */
    private View mMyCoverView;   // 封面root view
    private ImageView mCoverImg; // 封面图片ImageView
    private ImageView mAvatarImg;// 用户头像
    private TextView mNameTv; //用户名字
    private ResizeLayout mResizeLayout;
    private PMsgBottomView mPMsgBottomView;
    private List<PublicMessage> mMessages = new ArrayList<>();
    private PublicMessageAdapter mAdapter;
    private String mLoginUserId;       // 当前登陆用户的UserId
    private String mLoginNickName;// 当前登陆用户的昵称
    private boolean isdongtai;
    private String cricleid;
    private String pinglun;
    private String dianzan;
    /* 当前选择的是哪个用户的个人空间,仅用于查看个人空间的情况下 */
    private String mUserId;
    private String mNickName;
    private CheckableImageView mIvTitleLeft;
    private TextView mTvTitle;
    private CheckableImageView mIvTitleRight;
    private View mPlaceholder;
    private View mLine;
    private RelativeLayout mToolbarRl;
    // 为弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            menuWindow.dismiss();
            Intent intent = new Intent();
            switch (v.getId()) {
                case R.id.btn_send_picture:// 发表图文，
                    intent.setClass(getApplicationContext(), SendShuoshuoActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.btn_send_voice:  // 发表语音
                    intent.setClass(getApplicationContext(), SendAudioActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.btn_send_video:  // 发表视频
                    intent.setClass(getApplicationContext(), SendVideoActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.btn_send_file:   // 发表文件
                    intent.setClass(getApplicationContext(), SendFileActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.new_comment:     // 最新评论
                    Intent intent2 = new Intent(getApplicationContext(), NewZanActivity.class);
                    intent2.putExtra("OpenALL", true);
                    startActivity(intent2);
                    break;
                default:
                    break;
            }
        }
    };
    private boolean more;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_circle);
        mLoginUserId = coreManager.getSelf().getUserId();
        mLoginNickName = coreManager.getSelf().getNickName();

        if (getIntent() != null) {
            mType = getIntent().getIntExtra(AppConstant.EXTRA_CIRCLE_TYPE, AppConstant.CIRCLE_TYPE_MY_BUSINESS);// 默认的为查看我的商务圈
            mUserId = getIntent().getStringExtra(AppConstant.EXTRA_USER_ID);
            mNickName = getIntent().getStringExtra(AppConstant.EXTRA_NICK_NAME);

            pinglun = getIntent().getStringExtra("pinglun");
            dianzan = getIntent().getStringExtra("dianzan");
            isdongtai = getIntent().getBooleanExtra("isdongtai", false);
            cricleid = getIntent().getStringExtra("messageid");
        }

        if (!isMyBusiness()) {//如果查看的是个人空间的话，那么mUserId必须要有意义
            if (TextUtils.isEmpty(mUserId)) {// 没有带userId参数，那么默认看的就是自己的空间
                mUserId = mLoginUserId;
                mNickName = mLoginNickName;
            }
        }

       /* if (mUserId != null && mUserId.equals(mLoginUserId)) {
            String mLastMessage = PreferenceUtils.getString(this, "BUSINESS_CIRCLE_DATA");
            if (!TextUtils.isEmpty(mLastMessage)) {
                mMessages = JSON.parseArray(mLastMessage, PublicMessage.class);
            }
        }*/

        initActionBar();
        Downloader.getInstance().init(MyApplication.getInstance().mAppDir + File.separator + coreManager.getSelf().getUserId()
                + File.separator + Environment.DIRECTORY_MOVIES);// 初始化视频下载目录
        initView();
    }

    private boolean isMyBusiness() {
        return mType == AppConstant.CIRCLE_TYPE_MY_BUSINESS;
    }

    private boolean isMySpace() {
        return mLoginUserId.equals(mUserId);
    }

    private void initActionBar() {
        getSupportActionBar().hide();

        mLine = findViewById(R.id.line_v);
        mPlaceholder = findViewById(R.id.placeholder_v);
        ImmersionBar.with(this)
                .statusBarView(mPlaceholder)
                .keyboardEnable(true)
                .statusBarDarkFont(true, 0.2f)
                .init();
        mToolbarRl = findViewById(R.id.tool_bar_rl);
        int skinColor;
        int colorName = SkinUtils.getSkin(this).getColorName();
        if (colorName == R.string.skin_simple_white) {
            skinColor = ContextCompat.getColor(this, R.color.tb_bg_skin_simple_white);
        } else {
            skinColor = SkinUtils.getSkin(this).getPrimaryColor();
        }
        mPlaceholder.setBackgroundColor(skinColor);
        mToolbarRl.setBackgroundColor(skinColor);


        mIvTitleLeft = findViewById(R.id.iv_title_left);
        if (coreManager.getConfig().newUi) {
            //左侧返回
            mIvTitleLeft.setOnClickListener(v -> finish());
        } else {
            mIvTitleLeft.setVisibility(View.GONE);
        }

        mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(mNickName);

        mIvTitleRight = findViewById(R.id.iv_title_right);
        if (mUserId.equals(mLoginUserId)) {// 查看自己的空间才有发布按钮
            mIvTitleRight.setVisibility(View.VISIBLE);
            mIvTitleRight.setOnClickListener(v -> {
                menuWindow = new SelectPicPopupWindow(BusinessCircleActivity.this, itemsOnClick);
                // 在获取宽高之前需要先测量，否则得不到宽高
                menuWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                // +x右,-x左,+y下,-y上
                // pop向左偏移显示
                menuWindow.showAsDropDown(v,
                        -(menuWindow.getContentView().getMeasuredWidth() - v.getWidth() / 2 - 40),
                        10);
            });
        } else {
            mIvTitleRight.setVisibility(View.INVISIBLE);
        }


    }

    private void initView() {
        initCoverView();
        mResizeLayout = (ResizeLayout) findViewById(R.id.resize_layout);
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        mPMsgBottomView = (PMsgBottomView) findViewById(R.id.bottom_view);
        mPMsgBottomView.setBqKeyBoardListener(new ChatFaceView.BqMustInfoListener() {
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
       /* mResizeLayout.setOnResizeListener(new ResizeLayout.OnResizeListener() {
            @Override
            public void OnResize(int w, int h, int oldw, int oldh) {
                if (oldh < h) {// 键盘被隐藏
                    mCommentReplyCache = null;
                    mPMsgBottomView.setHintText("");
                    mPMsgBottomView.reset();
                }
            }
        });*/

        mPMsgBottomView.setPMsgBottomListener(new PMsgBottomView.PMsgBottomListener() {
            @Override
            public void sendText(String text) {
                if (mCommentReplyCache != null) {
                    mCommentReplyCache.text = text;
                    addComment(mCommentReplyCache);
                    mPMsgBottomView.hide();
                }
            }
        });

        if (isdongtai) {
            // 如果是动态，不添加HeadView
        } else {
            mPullToRefreshListView.getRefreshableView().addHeaderView(mMyCoverView, null, false);
        }

        mAdapter = new PublicMessageAdapter(this, coreManager, mMessages);
        setListenerAudio(mAdapter);
        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);

        if (isdongtai) {
            mPullToRefreshListView.setReflashable(false);
        }
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestData(true);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestData(false);
            }
        });
        //(头部view高度-(状态栏+标题栏))
        float headHeight = XfileUtils.measureViewHeight(mMyCoverView) - (XfileUtils.measureViewHeight(mToolbarRl) + ImmersionBar.getStatusBarHeight(this));
        float mHeight = (255f / headHeight);
        mPullToRefreshListView.getRefreshableView().setOnScrollListener(
                new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        if (mPMsgBottomView.getVisibility() != View.GONE) {
                            mPMsgBottomView.hide();
                        }


                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {


                        View view1 = mPullToRefreshListView.getRefreshableView().getChildAt(0);
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
                                } else {
                                    mPlaceholder.getBackground().mutate().setAlpha((int) (mHeight * hei));
                                    mToolbarRl.getBackground().mutate().setAlpha((int) (mHeight * hei));
                                    mLine.getBackground().mutate().setAlpha((int) (mHeight * hei));

                                    if (hei > (headHeight / 2)) {
                                        mIvTitleLeft.setChecked(true);
                                        mIvTitleRight.setChecked(true);
                                        mTvTitle.setAlpha(1);
                                    } else {
                                        mTvTitle.setAlpha(0);
                                        mIvTitleLeft.setChecked(false);
                                        mIvTitleRight.setChecked(false);
                                    }
                                }
                            } else if (firstVisibleItem == 0) {
                                mLine.getBackground().mutate().setAlpha(0);
                                mPlaceholder.getBackground().mutate().setAlpha(0);
                                mToolbarRl.getBackground().mutate().setAlpha(0);
                                mTvTitle.setAlpha(0);
                                mIvTitleLeft.setChecked(false);
                                mIvTitleRight.setChecked(false);
                            } else {
                                mPlaceholder.getBackground().mutate().setAlpha(255);
                                mToolbarRl.getBackground().mutate().setAlpha(255);
                                mLine.getBackground().mutate().setAlpha(255);
                                mTvTitle.setAlpha(1);
                                mIvTitleLeft.setChecked(true);
                                mIvTitleRight.setChecked(true);

                            }
                        }
                    }
                });

        if (isMyBusiness()) {
            readFromLocal();
        } else {
            requestData(true);
        }
    }

    private void initCoverView() {
        mMyCoverView = LayoutInflater.from(this).inflate(R.layout.space_cover_view, null);
        mMyCoverView.findViewById(R.id.ll_btn_send).setVisibility(View.GONE);
        mCoverImg = (ImageView) mMyCoverView.findViewById(R.id.cover_img);
        mAvatarImg = (ImageView) mMyCoverView.findViewById(R.id.avatar_img);
        mNameTv = mMyCoverView.findViewById(R.id.name_tv);
        mNameTv.setText(mNickName);
        mCoverImg.setOnClickListener(v -> {
            if (UiUtils.isNormalClick(v)) {
                if (TextUtils.equals(mUserId, coreManager.getSelf().getUserId())) {
                    AndPermissionUtils.albumPermission(BusinessCircleActivity.this, BusinessCircleActivity.this);
                }
            }
        });
        // 头像
        if (isMyBusiness() || isMySpace()) {
            AvatarHelper.getInstance().displayAvatar(mLoginNickName, mLoginUserId, mAvatarImg, true);
            // 优先加载user信息中的背景图片，失败就加载头像，
            String bg = coreManager.getSelf().getMsgBackGroundUrl();
            if (!TextUtils.isEmpty(bg)) {
                Glide.with(this)
                        .load(bg)
                        .placeholder(R.drawable.avatar_normal)
                        .dontAnimate()
                        .into(new SimpleTarget<GlideDrawable>() {
                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                mCoverImg.setImageDrawable(resource);
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                AvatarHelper.getInstance().displayRoundAvatar(mLoginNickName, mLoginUserId, mCoverImg, false);
                            }
                        });
            } else {
                AvatarHelper.getInstance().displayRoundAvatar(mLoginNickName, mLoginUserId, mCoverImg, false);
            }
        } else {
            AvatarHelper.getInstance().displayAvatar(mNickName, mUserId, mAvatarImg, true);
            AvatarHelper.getInstance().displayRoundAvatar(mNickName, mUserId, mCoverImg, false);
        }
        mAvatarImg.setOnClickListener(v -> {// 进入个人资料页
            Intent intent = new Intent(getApplicationContext(), BasicInfoActivity.class);
            if (isMyBusiness() || isMySpace()) {
                intent.putExtra(AppConstant.EXTRA_USER_ID, mLoginUserId);
            } else {
                intent.putExtra(AppConstant.EXTRA_USER_ID, mUserId);
            }
            startActivity(intent);
        });
    }

    private void readFromLocal() {
        FileDataHelper.readArrayData(getApplicationContext(), mLoginUserId, FileDataHelper.FILE_BUSINESS_CIRCLE, new StringJsonArrayRequest.Listener<PublicMessage>() {
            @Override
            public void onResponse(ArrayResult<PublicMessage> result) {
                if (result != null && result.getData() != null) {
                    mMessages.clear();
                    mMessages.addAll(result.getData());
                    mAdapter.notifyDataSetInvalidated();
                }
                requestData(true);
            }
        }, PublicMessage.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (mPMsgBottomView != null && mPMsgBottomView.getVisibility() == View.VISIBLE) {
            mPMsgBottomView.hide();
        } else {
            // 点返回键退出全屏视频，
            // 如果PublicMessageAdapter用在其他activity, 也要加上，
            if (JVCideoPlayerStandardSecond.backPress()) {
                JCMediaManager.instance().recoverMediaPlayer();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) {
            listener.ideChange();
        }
        listener = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       /* if (mUserId.equals(mLoginUserId)) {
            if (mMessages != null && mMessages.size() > 0) {
                PreferenceUtils.putString(this, "BUSINESS_CIRCLE_DATA", JSON.toJSONString(mMessages));
            }
        }*/
    }

    public void setListenerAudio(ListenerAudio listener) {
        this.listener = listener;
    }

    private void changeBackgroundImage() {
        CameraUtil.pickImageSimple(this, REQUEST_CODE_PICK_PHOTO);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SEND_MSG) {
                // 发说说成功
                String messageId = data.getStringExtra(AppConstant.EXTRA_MSG_ID);
                CircleMessageDao.getInstance().addMessage(mLoginUserId, messageId);
                requestData(true);
                removeNullTV();
            } else if (requestCode == REQUEST_CODE_PICK_PHOTO) {
                if (data != null && data.getData() != null) {
                    String path = CameraUtil.getImagePathFromUri(this, data.getData());
                    updateBackgroundImage(path);
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        }
    }


    private void updateBackgroundImage(String path) {
        File bg = new File(path);
        if (!bg.exists()) {
            LogUtils.log(path);
            Reporter.unreachable();
            ToastUtil.showToast(this, R.string.image_not_found);
            return;
        }
        DialogHelper.showDefaulteMessageProgressDialog(this);
        UploadingHelper.upFile(BusinessCircleActivity.this, coreManager, coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), new File(path), new UploadingHelper.OnUpFileListener() {
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
                                displayAvatar();
                            }

                            @Override
                            public void onError(Call call, Exception e) {
                                DialogHelper.dismissProgressDialog();
                                ToastUtil.showErrorNet(mContext);
                            }
                        });
            }

            @Override
            public void onFailure(String err, String filePath) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showErrorNet(mContext);
            }
        });

    }


    public void displayAvatar() {
        // 加载小头像，
        AvatarHelper.getInstance().displayAvatar(mUserId, mAvatarImg, true);
        // 优先加载user信息中的背景图片，失败就加载头像，
        String bg = coreManager.getSelf().getMsgBackGroundUrl();
        if (TextUtils.isEmpty(bg)) {
            realDisplayAvatar();
        }
        Glide.with(mContext.getApplicationContext())
                .load(bg)
                .placeholder(R.drawable.avatar_normal)
                .dontAnimate()
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        mCoverImg.setImageDrawable(resource);
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
                            mCoverImg.setImageDrawable(resource);
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


    /********** 公共消息的数据请求部分 *********/

    /**
     * 请求公共消息
     *
     * @param isPullDwonToRefersh 是下拉刷新，还是上拉加载
     */
    private void requestData(boolean isPullDwonToRefersh) {
        if (isMyBusiness()) {
            requestMyBusiness(isPullDwonToRefersh);
        } else {
            if (isdongtai) {
                if (isPullDwonToRefersh) {
                    more = true;
                }
                if (!more) {
                    // ToastUtil.showToast(getContext(), getString(R.string.tip_last_item));
                    mPullToRefreshListView.setReleaseLabel(getString(R.string.tip_last_item));
                    mPullToRefreshListView.setRefreshingLabel(getString(R.string.tip_last_item));
                    refreshComplete();
                } else {
                    requestSpacedongtai(isPullDwonToRefersh);
                }
            } else {
                requestSpace(isPullDwonToRefersh);
            }
        }
    }

    /**
     * 停止刷新动画
     */
    private void refreshComplete() {
        mPullToRefreshListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPullToRefreshListView.onRefreshComplete();
            }
        }, 200);
    }

    private void requestMyBusiness(final boolean isPullDwonToRefersh) {
        if (isPullDwonToRefersh) {
            mPageIndex = 0;
        }
        List<String> msgIds = CircleMessageDao.getInstance().getCircleMessageIds(mLoginUserId, mPageIndex, AppConfig.PAGE_SIZE);

        if (msgIds == null || msgIds.size() <= 0) {
            mPullToRefreshListView.onRefreshComplete(200);
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("ids", JSON.toJSONString(msgIds));

        HttpUtils.get().url(coreManager.getConfig().MSG_GETS)
                .params(params)
                .build()
                .execute(new ListCallback<PublicMessage>(PublicMessage.class) {
                    @Override
                    public void onResponse(com.xuan.xuanhttplibrary.okhttp.result.ArrayResult<PublicMessage> result) {
                        List<PublicMessage> data = result.getData();
                        if (isPullDwonToRefersh) {
                            mMessages.clear();
                        }
                        if (data != null && data.size() > 0) {// 没有更多数据
                            mPageIndex++;
                            if (isPullDwonToRefersh) {
                                FileDataHelper.writeFileData(getApplicationContext(), mLoginUserId, FileDataHelper.FILE_BUSINESS_CIRCLE, result);
                            }
                            mMessages.addAll(data);
                        }
                        mAdapter.notifyDataSetChanged();

                        mPullToRefreshListView.onRefreshComplete();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getApplicationContext());
                        mPullToRefreshListView.onRefreshComplete();
                    }
                });
    }

    private void requestSpace(final boolean isPullDwonToRefersh) {
        String messageId = null;
        if (!isPullDwonToRefersh && mMessages.size() > 0) {
            messageId = mMessages.get(mMessages.size() - 1).getMessageId();
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mUserId);
        params.put("flag", PublicMessage.FLAG_NORMAL + "");

        if (!TextUtils.isEmpty(messageId)) {
            if (isdongtai) {
                params.put("messageId", cricleid);
            } else {
                params.put("messageId", messageId);
            }
        }
        params.put("pageSize", String.valueOf(AppConfig.PAGE_SIZE));

        HttpUtils.get().url(coreManager.getConfig().MSG_USER_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<PublicMessage>(PublicMessage.class) {
                    @Override
                    public void onResponse(com.xuan.xuanhttplibrary.okhttp.result.ArrayResult<PublicMessage> result) {
                        List<PublicMessage> data = result.getData();
                        if (isPullDwonToRefersh) {
                            mMessages.clear();
                        }
                        if (data != null && data.size() > 0) {
                            mMessages.addAll(data);
                        }
                        more = !(data == null || data.size() < AppConfig.PAGE_SIZE);
                        mAdapter.notifyDataSetChanged();

                        mPullToRefreshListView.onRefreshComplete();
                        if (mAdapter.isEmpty())
                            addNullTV2LV();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getApplicationContext());
                        mPullToRefreshListView.onRefreshComplete();
                    }
                });
    }

    // 最近评论&赞进入
    private void requestSpacedongtai(final boolean isPullDwonToRefersh) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", cricleid);

        HttpUtils.get().url(coreManager.getConfig().MSG_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<PublicMessage>(PublicMessage.class) {
                    @Override
                    public void onResponse(com.xuan.xuanhttplibrary.okhttp.result.ObjectResult<PublicMessage> result) {

                        PublicMessage datas = result.getData();
                        List<PublicMessage> datass = new ArrayList<>();
                        datass.add(datas);
                        if (isPullDwonToRefersh) {
                            mMessages.clear();
                        }
                        mMessages.addAll(datass);
                        mAdapter.notifyDataSetChanged();

                        mPullToRefreshListView.onRefreshComplete();
                        if (mAdapter.isEmpty())
                            addNullTV2LV();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getApplicationContext());
                        mPullToRefreshListView.onRefreshComplete();
                    }
                });
    }

    public void showCommentEnterView(int messagePosition, String toUserId, String toNickname, String toShowName) {
        mCommentReplyCache = new CommentReplyCache();
        mCommentReplyCache.messagePosition = messagePosition;
        mCommentReplyCache.toUserId = toUserId;
        mCommentReplyCache.toNickname = toNickname;
        if (TextUtils.isEmpty(toUserId) || TextUtils.isEmpty(toNickname) || TextUtils.isEmpty(toShowName)) {
            mPMsgBottomView.setHintText("");
        } else {
            mPMsgBottomView.setHintText(getString(R.string.replay_text, toShowName));
        }
        mPMsgBottomView.show();
    }

    private void addComment(CommentReplyCache cache) {
        Comment comment = new Comment();
        comment.setUserId(mLoginUserId);
        comment.setNickName(mLoginNickName);
        comment.setToUserId(cache.toUserId);
        comment.setToNickname(cache.toNickname);
        comment.setBody(cache.text);
        addComment(cache.messagePosition, comment);
    }

    private void addComment(final int position, final Comment comment) {
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
                    public void onResponse(com.xuan.xuanhttplibrary.okhttp.result.ObjectResult<String> result) {
                        List<Comment> comments = message.getComments();
                        if (comments == null) {
                            comments = new ArrayList<>();
                            message.setComments(comments);
                        }
                        comment.setCommentId(result.getData());
                        comments.add(comment);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getApplicationContext());
                    }
                });
    }

    @Override
    public void showView(int messagePosition, String toUserId, String toNickname, String toShowName) {
        showCommentEnterView(messagePosition, toUserId, toNickname, toShowName);
    }

    @Override
    public void refreshAfterOperation(PublicMessage message) {
        int size = mMessages.size();
        for (int i = 0; i < size; i++) {
            if (StringUtils.strEquals(mMessages.get(i).getMessageId(), message.getMessageId())) {
                mMessages.set(i, message);
                mAdapter.setData(mMessages);
            }
        }
    }

    public void addNullTV2LV() {
        TextView nullTextView = new TextView(this);
        nullTextView.setTag("NullTV");
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int paddingSize = getResources().getDimensionPixelSize(R.dimen.NormalPadding);
        nullTextView.setPadding(0, paddingSize, 0, paddingSize);
        nullTextView.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        nullTextView.setGravity(Gravity.CENTER);

        nullTextView.setLayoutParams(lp);
        nullTextView.setText(InternationalizationHelper.getString("JX_NoData"));
        mPullToRefreshListView.getRefreshableView().addFooterView(nullTextView);
        mPullToRefreshListView.setReflashable(false);
    }

    public void removeNullTV() {
        mPullToRefreshListView.getRefreshableView().removeFooterView(mPullToRefreshListView.findViewWithTag("NullTV"));
        mPullToRefreshListView.setReflashable(true);
    }

    public interface ListenerAudio {
        void ideChange();
    }

    class CommentReplyCache {
        int messagePosition;// 消息的Position
        String toUserId;
        String toNickname;
        String text;
    }

    private ChatFaceView.CustomBqListener customBqListener = new ChatFaceView.CustomBqListener() {
        @Override
        public void onClickViewSendBq(boolean b, String custom, String customBq, String defineEmoUrl, String s) {
//            sendCollection(defineEmoUrl);
        }

        @Override
        public void goCustomBqManageActivity() {
            Intent intent = new Intent(BusinessCircleActivity.this, CustomBqManageActivity.class);
            startActivityForResult(intent, REQUEST_MANAGE);
        }
    };
    private ChatFaceView.BqKeyBoardListener bqKeyBoardListener = v -> BqShopActivity.start(BusinessCircleActivity.this, BqShopActivity.BQ_CODE_REQUEST);
    private ChatFaceView.MyBqListener myBqListener = new ChatFaceView.MyBqListener() {
        @Override
        public void onClickViewSendBq(boolean b, String id, String emoMean, String fileUrl, String thumbnailUrl) {
            // sendCollection(thumbnailUrl);
        }
    };

    @Override
    public void onSuccess() {
        changeBackgroundImage();
    }

    @Override
    public void onFailure(List<String> data) {

    }
}
