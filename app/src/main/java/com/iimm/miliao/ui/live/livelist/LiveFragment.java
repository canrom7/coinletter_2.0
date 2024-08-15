package com.iimm.miliao.ui.live.livelist;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.iimm.miliao.R;
import com.iimm.miliao.broadcast.MucgroupUpdateUtil;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.EasyFragment;
import com.iimm.miliao.ui.live.LiveConstants;
import com.iimm.miliao.ui.live.LivePlayingActivity;
import com.iimm.miliao.ui.live.PushFlowActivity;
import com.iimm.miliao.ui.live.bean.LiveRoom;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ViewHolder;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 全部直播的列表
 */
public class LiveFragment extends EasyFragment {
    private PullToRefreshListView mPullToRefreshListView;
    private List<LiveRoom> mMucRoomS;
    private LiveRoomAdapter mAdapter;
    private String mAccessToken;
    private String mLoginUserId;
    private int mPageIndex = 0;
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MucgroupUpdateUtil.ACTION_UPDATE)) {
                requestData(true);
            }
        }
    };

    public LiveFragment() {
        mMucRoomS = new ArrayList<>();
        mAdapter = new LiveRoomAdapter();
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.layout_address;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            initView();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mUpdateReceiver);
    }

    @SuppressLint("InflateParams")
    private void initView() {
        mAccessToken = coreManager.getSelfStatus().accessToken;
        mLoginUserId = coreManager.getSelf().getUserId();
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        mPullToRefreshListView.setAdapter(mAdapter);
        View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_list_empty_view, null);
        mPullToRefreshListView.setEmptyView(emptyView);
        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                requestData(true);
            }

            @Override
            public void onPullUpToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                requestData(false);
            }
        });

        mPullToRefreshListView.getRefreshableView().setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        LiveRoom room = mMucRoomS.get((int) id);
                        if (String.valueOf(room.getUserId()).equals(mLoginUserId)) {
                            // 开启直播
                            gotoLiveRoom(room, true);
                        } else {
                            // 观看直播
                            gotoLiveRoom(room, false);
                        }
                    }
                });

        getActivity().registerReceiver(mUpdateReceiver, MucgroupUpdateUtil.getUpdateActionFilter());

        requestData(true);
    }

    // 获取直播间列表
    private void requestData(final boolean isPullDwonToRefersh) {
        if (isPullDwonToRefersh) {
            mPageIndex = 0;
        }
        Map<String, String> params = new HashMap<>();
        params.put("access_token", mAccessToken);
        params.put("pageIndex", String.valueOf(mPageIndex));
        params.put("pageSize", String.valueOf(10));
        HttpUtils.get().url(coreManager.getConfig().GET_LIVE_ROOM_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<LiveRoom>(LiveRoom.class) {
                    @Override
                    public void onResponse(ArrayResult<LiveRoom> result) {
                        mPageIndex++;
                        if (isPullDwonToRefersh) {
                            mMucRoomS.clear();
                        }
                        List<LiveRoom> data = result.getData();
                        if (data != null && data.size() > 0) {
                            mMucRoomS.addAll(data);
                            for (int i = 0; i < mMucRoomS.size(); i++) {
                                LiveRoom member = mMucRoomS.get(i);
                                if (String.valueOf(member.getUserId()).equals(mLoginUserId)) {
                                    PreferenceUtils.putString(getActivity(), mLoginUserId + "Exists", member.getJid());
                                    PreferenceUtils.putString(getActivity(), member.getJid(), member.getRoomId());
                                    PreferenceUtils.putString(getActivity(), member.getRoomId(), member.getUrl());
                                    PreferenceUtils.putString(getActivity(), member.getUrl(), member.getName());
                                }
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                        mPullToRefreshListView.onRefreshComplete();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getActivity());
                        mPullToRefreshListView.onRefreshComplete();
                    }
                });
    }

    private void gotoLiveRoom(final LiveRoom room, final boolean liver) {
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());
        Map<String, String> params = new HashMap<>();
        params.put("access_token", mAccessToken);
        params.put("roomId", room.getRoomId());
        params.put("userId", mLoginUserId);

        HttpUtils.get().url(coreManager.getConfig().JOIN_LIVE_ROOM)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            if (liver) {
                                Intent intent = new Intent(getActivity(), PushFlowActivity.class);
                                intent.putExtra(LiveConstants.LIVE_PUSH_FLOW_URL, room.getUrl());
                                intent.putExtra(LiveConstants.LIVE_ROOM_ID, room.getRoomId());
                                intent.putExtra(LiveConstants.LIVE_CHAT_ROOM_ID, room.getJid());
                                intent.putExtra(LiveConstants.LIVE_ROOM_NAME, room.getName());
                                intent.putExtra(LiveConstants.LIVE_ROOM_PERSON_ID, String.valueOf(room.getUserId()));
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(getActivity(), LivePlayingActivity.class);
                                intent.putExtra(LiveConstants.LIVE_GET_FLOW_URL, room.getUrl());
                                intent.putExtra(LiveConstants.LIVE_ROOM_ID, room.getRoomId());
                                intent.putExtra(LiveConstants.LIVE_CHAT_ROOM_ID, room.getJid());
                                intent.putExtra(LiveConstants.LIVE_ROOM_NAME, room.getName());
                                intent.putExtra(LiveConstants.LIVE_ROOM_PERSON_ID, String.valueOf(room.getUserId()));
                                intent.putExtra(LiveConstants.LIVE_STATUS, room.getStatus());
                                startActivity(intent);
                            }
                        } else if (!String.valueOf(room.getUserId()).equals(mLoginUserId)) {
                            // 已被踢出该直播间
                            Toast.makeText(getActivity(), InternationalizationHelper.getString("KICKED_NOT_IN"), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(getActivity());
                    }
                });
    }

    /**
     * 修改直播间资料
     */
    public void modifyLiveRoom(LiveRoom room, String roomName, String roomNotice) {
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());
        Map<String, String> params = new HashMap<>();
        params.put("access_token", mAccessToken);
        params.put("roomId", room.getRoomId());
        params.put("name", roomName);
        params.put("notice", roomNotice);
        HttpUtils.get().url(coreManager.getConfig().LIVE_ROOM_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        MucgroupUpdateUtil.broadcastUpdateUi(getActivity());
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(getActivity());
                    }
                });
    }

    // 删除直播间
    public void deleteRoom(String roomId) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", mAccessToken);
        params.put("roomId", roomId);
        HttpUtils.get().url(coreManager.getConfig().DELETE_LIVE_ROOM)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        // 直播间已被删除，更新状态
                        PreferenceUtils.putString(getActivity(), mLoginUserId + "Exists", "Not");
                        // 刷新界面
                        MucgroupUpdateUtil.broadcastUpdateUi(getActivity());
                        Toast.makeText(getActivity(), InternationalizationHelper.getString("JXAlert_DeleteOK"), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getActivity());
                    }
                });
    }

    class LiveRoomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mMucRoomS.size();
        }

        @Override
        public Object getItem(int position) {
            return mMucRoomS.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.row_live_room, parent, false);
            }
            final LiveRoom room = mMucRoomS.get(position);
            ImageView live_default = ViewHolder.get(convertView, R.id.live_default);
            ImageView avatar_img = ViewHolder.get(convertView, R.id.live_avatar_img);
            AvatarHelper.getInstance().displayAvatar(String.valueOf(room.getUserId()), live_default, false);
            AvatarHelper.getInstance().displayAvatar(String.valueOf(room.getUserId()), avatar_img, false);
            TextView tv1 = ViewHolder.get(convertView, R.id.live_title);
            TextView tv2 = ViewHolder.get(convertView, R.id.live_nick_name);
            TextView tv4 = ViewHolder.get(convertView, R.id.live_notice);
            TextView tv5 = ViewHolder.get(convertView, R.id.islive);
            tv1.setText(room.getName());
            tv2.setText(room.getNickName());
            tv4.setText(room.getNotice());
            tv5.setText(InternationalizationHelper.getString("JXLive_inLiving"));
            if (room.getStatus() == 1) {
                tv5.setVisibility(View.VISIBLE);
            } else {
                tv5.setVisibility(View.GONE);
            }
            avatar_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (String.valueOf(room.getUserId()).equals(mLoginUserId)) {
                        deleteRoom(room.getRoomId());
                    }
                }
            });
            return convertView;
        }
    }
}
