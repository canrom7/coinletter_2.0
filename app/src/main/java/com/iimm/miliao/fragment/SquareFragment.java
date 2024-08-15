package com.iimm.miliao.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.adapter.MessageEventHongdian;
import com.iimm.miliao.adapter.SquareAdapter;
import com.iimm.miliao.bean.SquareBean;
import com.iimm.miliao.db.dao.MyZanDao;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.EasyFragment;
import com.iimm.miliao.ui.contacts.PublishNumberActivity;
import com.iimm.miliao.ui.groupchat.SelectContactsActivity;
import com.iimm.miliao.ui.life.LifeCircleActivity;
import com.iimm.miliao.ui.live.LiveActivity;
import com.iimm.miliao.ui.me.NearPersonActivity;
import com.iimm.miliao.ui.me.SignInRedActivity;
import com.iimm.miliao.ui.tool.WebViewActivityFragment;
import com.iimm.miliao.ui.trill.TriListActivity;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.PermissionUtil;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.util.permission.AndPermissionUtils;
import com.iimm.miliao.util.permission.OnPermissionClickListener;
import com.iimm.miliao.view.PullToRefreshSlideListView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 发现界面
 */
public class SquareFragment extends EasyFragment {
    private RelativeLayout life_circle_relat, douyin_relat, video_conference_relat,
            live_chat_relat, find_nearby_relat, bjnews_relat, sign_packet;
    private LinearLayoutCompat video_ll;

    private View mMyCoverView;
    private PullToRefreshSlideListView mListView;
    private SquareAdapter mSquareAdapter;
    private List<SquareBean.DataBean> mSquareBeans;
    WebView mWebView;
    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_square;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        ((TextView) (findViewById(R.id.tv_title_center))).setText("我的代理");
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        EventBus.getDefault().register(this);
        initView();
         mWebView = findViewById(R.id.webview);
        AsyncUtils.doAsync(this, throwable -> {
            Reporter.post("获取生活圈新消息数量失败，", throwable);
            Activity ctx = getActivity();
            if (ctx != null) {
                ctx.runOnUiThread(() -> ToastUtil.showToast(requireContext(), R.string.tip_get_life_circle_number_failed));
            }
        }, squareFragmentAsyncContext -> {
            final int lifeCircleNumber = MyZanDao.getInstance().getZanSize(coreManager.getSelf().getUserId());
            UiUtils.updateNum(mMyCoverView.findViewById(R.id.main_tab_three_tv), lifeCircleNumber);
            squareFragmentAsyncContext.uiThread(squareFragment -> squareFragment.updateLifeCircleNumber(lifeCircleNumber));
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        initWeb(coreManager.getConfig().apiUrl+"pages/console/adagent.html?userId="+coreManager.getSelf().getUserId());
    }

    private void initWeb(String ip) {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.startsWith("weixin://")
                        || url.startsWith("alipays://")
                        || url.startsWith("mqqapi://")) {
                    //打开本地App进行支付
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                }

                return false;
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.getSettings().setAllowFileAccess(true);// 设置允许访问文件数据
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setGeolocationEnabled(true);
        mWebView.getSettings().setDatabaseEnabled(true);

        mWebView.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView view, int progress) {
//                if(progressBar!=null){
//                    progressBar.show();
//                }
//                if (progress == 100) {
//                    if(progressBar!=null) {
//                        progressBar.dismiss();
//                        progressBar = null;
//                    }
//                    spf.setRefreshing(false);
//                }
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
//                mUploadCallbackAboveL = filePathCallback;
//                requestWritePermission();
//                selectPhoto(1);
                return true;
            }


            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, true);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }


        });
        mWebView.loadUrl(ip);
    }

    private void updateSingle(int position) {

        mSquareAdapter.notifyDataSetChanged(mListView.getRefreshableView(), position);
    }
    //xiugai
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if(fragments!=null){
            for(Fragment fragment :fragments){
                if(fragment instanceof  WebViewActivityFragment){
                    fragment.onActivityResult(requestCode,resultCode,data);
                    break;
                }
            }
        }
    }

    public void initView() {
        mMyCoverView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_square_top, null);

        life_circle_relat = mMyCoverView.findViewById(R.id.life_circle_relat);
        douyin_relat = mMyCoverView.findViewById(R.id.douyin_relat);
        video_conference_relat = mMyCoverView.findViewById(R.id.video_conference_relat);
        live_chat_relat = mMyCoverView.findViewById(R.id.live_chat_relat);
        video_ll = mMyCoverView.findViewById(R.id.video_ll);
        find_nearby_relat = mMyCoverView.findViewById(R.id.find_nearby_relat);
        if (coreManager.getConfig().disableLocationServer) {
            find_nearby_relat.setVisibility(View.GONE);
        }
        bjnews_relat = mMyCoverView.findViewById(R.id.bjnews_relat);
        sign_packet = mMyCoverView.findViewById(R.id.sign_in_red_packet_rl);

        mListView = findViewById(R.id.square_ptrslv);
        mListView.getRefreshableView().addHeaderView(mMyCoverView);
        mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mSquareBeans = new ArrayList<>();
        mSquareAdapter = new SquareAdapter(mSquareBeans, getContext());
        mListView.getRefreshableView().setAdapter(mSquareAdapter);

        life_circle_relat.setOnClickListener(this);
        douyin_relat.setOnClickListener(this);
        video_conference_relat.setOnClickListener(this);
        live_chat_relat.setOnClickListener(this);
        find_nearby_relat.setOnClickListener(this);
        bjnews_relat.setOnClickListener(this);
        sign_packet.setOnClickListener(this);

        mListView.setOnRefreshListener(refreshView -> requestServiceNumber());
        mListView.getRefreshableView().setOnItemClickListener((parent, view, position, id) -> {
            if (mSquareBeans.size() > 0 && position >= 2 && mSquareBeans.get(position - 2).getDiscoverNum() > 7) {
                //xiugai
                WebViewActivityFragment webViewActivityFragment = new WebViewActivityFragment();
                Bundle bundle = new Bundle();
                bundle.putString(WebViewActivityFragment.EXTRA_URL,mSquareBeans.get(position - 2).getDiscoverLinkURL());
                webViewActivityFragment.setArguments(bundle);
                getChildFragmentManager().beginTransaction().replace(R.id.mContaoiner,webViewActivityFragment ).addToBackStack(null).commitAllowingStateLoss();
            }
        });
        requestServiceNumber();
    }


    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        switch (v.getId()) {
            case R.id.life_circle_relat://朋友圈
                toStartActivity(LifeCircleActivity.class).run();
                break;
            case R.id.video_conference_relat://视频会议
                SelectContactsActivity.startQuicklyInitiateMeeting(requireContext());
                break;
            case R.id.find_nearby_relat://附近的人
                startActivity(new Intent(getActivity(), NearPersonActivity.class));
                break;
            case R.id.live_chat_relat://视频直播
                startActivity(new Intent(getActivity(), LiveActivity.class));
                break;
            case R.id.douyin_relat://短视频
                AndPermissionUtils.shootVideo(getActivity(), new OnPermissionClickListener() {
                    @Override
                    public void onSuccess() {
                        startActivity(new Intent(getActivity(), TriListActivity.class));
                    }

                    @Override
                    public void onFailure(List<String> data) {
                        ToastUtil.showToast(getContext(), R.string.please_open_some_permissions);
                        PermissionUtil.startApplicationDetailsSettings(getActivity());
                    }
                });
                break;
            case R.id.bjnews_relat://公众号
                Intent intent = new Intent(getActivity(), PublishNumberActivity.class);
                getActivity().startActivity(intent);
                break;
            case R.id.sign_in_red_packet_rl://签到红包
                startActivity(new Intent(getActivity(), SignInRedActivity.class));
                break;
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    // 更新发现模块新消息数量
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageEventHongdian message) {
        updateLifeCircleNumber(message.number);
        UiUtils.updateNum(mMyCoverView.findViewById(R.id.main_tab_three_tv), message.number);
    }

    private void updateLifeCircleNumber(int number) {

    }


    private Runnable toStartActivity(final Class<? extends Activity> clazz) {
        return () -> {
            Intent intent = new Intent(requireContext(), clazz);
            startActivity(intent);
        };
    }

    @SuppressWarnings("unused")
    private Runnable toToast() {
        return () -> ToastUtil.showToast(requireContext(), "即将上线，敬请期待！");
    }

    private void requestServiceNumber() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("user", "1");
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        DialogHelper.showDefaulteMessageProgressDialogAddCancel(requireActivity(), null);
        HttpUtils.get().url(coreManager.getConfig().APP_DISCOVER_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<SquareBean.DataBean>(SquareBean.DataBean.class) {

                    @Override
                    public void onResponse(ArrayResult<SquareBean.DataBean> result) {
                        DialogHelper.dismissProgressDialog();
                        mListView.onRefreshComplete();
                        if (Result.checkSuccess(getContext(), result) && result.getData() != null) {
                            dataInput(result.getData());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        mListView.onRefreshComplete();

                    }
                });


    }

    private void dataInput(List<SquareBean.DataBean> dataBeans) {

        mSquareBeans.clear();
        List<SquareBean.DataBean> mList = dataBeans;
        life_circle_relat.setVisibility(View.GONE);
        douyin_relat.setVisibility(View.GONE);
        video_conference_relat.setVisibility(View.GONE);
        live_chat_relat.setVisibility(View.GONE);
        video_ll.setVisibility(View.GONE);
        find_nearby_relat.setVisibility(View.GONE);
        bjnews_relat.setVisibility(View.GONE);
        sign_packet.setVisibility(View.GONE);
        for (int size = 0; size < dataBeans.size(); size++) {

            switch (dataBeans.get(size).getDiscoverNum()) {
                case 1:
                    life_circle_relat.setVisibility(View.VISIBLE);
                    setView(dataBeans.get(size), findViewById(R.id.life_circle_image), findViewById(R.id.life_circle2_tv), R.drawable.life_circle);
                    break;
                case 2:
                    douyin_relat.setVisibility(View.VISIBLE);
                    video_ll.setVisibility(View.VISIBLE);
                    setView(dataBeans.get(size), findViewById(R.id.douyin_image), findViewById(R.id.douyin_tv), R.drawable.short_video);
                    break;
                case 3:
                    video_conference_relat.setVisibility(View.VISIBLE);
                    video_ll.setVisibility(View.VISIBLE);
                    setView(dataBeans.get(size), findViewById(R.id.video_conference_image), findViewById(R.id.video_conference_tv), R.drawable.tv_meeting);
                    break;
                case 4:
                    live_chat_relat.setVisibility(View.VISIBLE);
                    video_ll.setVisibility(View.VISIBLE);
                    setView(dataBeans.get(size), findViewById(R.id.live_chat_image), findViewById(R.id.live_chat_tv), R.drawable.live_video);
                    break;
                case 5:
                    find_nearby_relat.setVisibility(View.VISIBLE);
                    setView(dataBeans.get(size), findViewById(R.id.find_nearby_image), findViewById(R.id.find_nearby_tv), R.drawable.find_nearby);
                    break;
                case 6:
                    bjnews_relat.setVisibility(View.VISIBLE);
                    setView(dataBeans.get(size), findViewById(R.id.bjnews_image), findViewById(R.id.bjnews_tv), R.drawable.bjnews);
                    break;
                case 7:
//                    sign_packet.setVisibility(View.VISIBLE);
                    setView(dataBeans.get(size), findViewById(R.id.my_envelope_iv), findViewById(R.id.my_envelope_tv), R.drawable.sign_in_red_packet);
                    break;
                default:
                    mSquareBeans.add(mList.get(size));
                    break;
            }
        }
        mSquareAdapter.setData(mSquareBeans);
    }

    private void setView(SquareBean.DataBean dataBean, ImageView viewById, TextView viewById1, int image) {

        if (getContext()!=null){
            Glide.with(getContext())
                    .load(dataBean.getDiscoverImg())
                    .placeholder(image)
                    .error(image)
                    .into(viewById);
        }
        viewById1.setText(dataBean.getDiscoverName());
    }
}
