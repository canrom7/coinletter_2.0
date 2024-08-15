package com.iimm.miliao.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.RoomMember;
import com.iimm.miliao.bean.assistant.GroupAssistantDetail;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.RoomMemberDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.message.assistant.GroupAssistantDetailActivity;
import com.iimm.miliao.ui.message.assistant.SelectGroupAssistantActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DisplayUtil;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ViewHolder;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;

interface PagerGridAdapterFactory<T> {
    ListAdapter createPagerGridAdapter(List<T> data);
}

interface OnItemClickListener<T> {
    void onItemClick(T item);
}

/**
 * Created by Administrator on 2016/9/8.
 */
public class ChatToolsView extends RelativeLayout {
    private Context mContext;

    private ViewPager viewPagerTools;
    private GridPagerAdapter gridPagerAdapter;
    private ChatBottomView.ChatBottomListener listener;
    private boolean isGroup;
    private String roomId, roomJid;

    // 群助手
    private GridView groupAssistantGridView;
    private GroupAssistantAdapter groupAssistantAdapter;

    public ChatToolsView(Context context) {
        super(context);
        init(context);
    }

    public ChatToolsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChatToolsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private List<Item> loadData() {
        // 隐藏个别按钮是通过删除指定drawable对应按钮实现，
        // 所以这里的drawable应该唯一，
        List<Item> itemList = new ArrayList<>();
        itemList.add(new Item(R.drawable.im_tool_photo_button_bg, R.string.chat_poto, () -> {

            listener.clickPhoto();
        }));
        itemList.add(new Item(R.drawable.im_tool_video_button_bg, R.string.chat_video, () -> {

            listener.clickLocalVideo();
        }));
        itemList.add(new Item(R.drawable.im_tool_camera_button_bg, R.string.chat_camera_record, () -> {

            listener.clickCamera();
        }));
        // 现拍照录像ui和二为一
        /*
        itemList.add(new Item(R.drawable.im_tool_local_button_bg, R.string.video, () -> {
            Dialog bottomDialog = new Dialog(getContext(), R.style.BottomDialog);
            View contentView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_select_video, null);
            bottomDialog.setContentView(contentView);
            ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
            layoutParams.width = getResources().getDisplayMetrics().widthPixels;
            contentView.setLayoutParams(layoutParams);
            bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
            bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
            bottomDialog.show();
            contentView.findViewById(R.id.dialog_select_cancel).setOnClickListener(v -> bottomDialog.dismiss());
            contentView.findViewById(R.id.dialog_select_local_video).setOnClickListener(v -> {
                bottomDialog.dismiss();
                listener.clickLocalVideo();
            });
            contentView.findViewById(R.id.dialog_select_start_record).setOnClickListener(v -> {
                bottomDialog.dismiss();
                listener.clickStartRecord();
            });
        }));*/
        //音视频通话
        if (CoreManager.requireConfig(MyApplication.getContext()).isAudioStatus == 1) {
            itemList.add(new Item(R.drawable.im_call_button_norma, R.string.chat_call, () -> {
//                ToastUtil.showToast(mContext,mContext.getResources().getString(R.string.not_yet_open));
                Dialog bottomDialog = new Dialog(getContext(), R.style.BottomDialog);
                View contentView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_select_media, null);
                bottomDialog.setContentView(contentView);
                ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
                layoutParams.width = getResources().getDisplayMetrics().widthPixels;
                contentView.setLayoutParams(layoutParams);
                bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
                bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
                bottomDialog.show();
                if (isGroup) {
                    TextView textView = (TextView) contentView.findViewById(R.id.dialog_select_voice);
                    textView.setText(InternationalizationHelper.getString("JX_Meeting"));
                    TextView textView_02 = (TextView) contentView.findViewById(R.id.dialog_select_video);
                    textView_02.setText(InternationalizationHelper.getString("JXSettingVC_VideoMeeting"));
                } else {
                    TextView textView = (TextView) contentView.findViewById(R.id.dialog_select_voice);
                    textView.setText(InternationalizationHelper.getString("JX_VoiceChat"));
                    TextView textView_02 = (TextView) contentView.findViewById(R.id.dialog_select_video);
                    textView_02.setText(InternationalizationHelper.getString("JX_VideoChat"));
                }
                contentView.findViewById(R.id.dialog_select_cancel).setOnClickListener(v -> bottomDialog.dismiss());
                contentView.findViewById(R.id.dialog_select_voice_ll).setOnClickListener(v -> {
                    bottomDialog.dismiss();
                    listener.clickAudio();
                });
                contentView.findViewById(R.id.dialog_select_video_ll).setOnClickListener(v -> {
                    bottomDialog.dismiss();
                    listener.clickVideoChat();
                });
            }));
        }
        if (MyApplication.mCoreManager != null && !MyApplication.mCoreManager.getConfig().disableLocationServer) {
            itemList.add(new Item(R.drawable.im_tool_loc_button_bg, R.string.chat_loc, () -> {
                listener.clickLocation();
            }));
        }
        if (!CoreManager.requireConfig(MyApplication.getInstance()).displayRedPacket) {
            itemList.add(new Item(R.drawable.im_tool_redpacket_button_bg, R.string.chat_red, () -> {
                listener.clickRedpacket();
            }));
        }
        if (CoreManager.requireConfig(MyApplication.getInstance()).isWeiBaoStatus == 1) {
            itemList.add(new Item(R.drawable.im_tool_redpacket_button_bg, R.string.cloud_red_envelope, () -> {
                listener.clickCloudRedEnvelope();
            }));
        }
        if (!CoreManager.requireConfig(MyApplication.getInstance()).displayRedPacket) {
            itemList.add(new Item(R.drawable.im_tool_transfer_button_bg, R.string.transfer_money, () -> {
                listener.clickTransferMoney();
            }));
        }
        if (CoreManager.requireConfig(MyApplication.getInstance()).isWeiBaoStatus == 1) {
            itemList.add(new Item(R.drawable.im_tool_transfer_button_bg, R.string.micro_transfer, () -> {
                listener.clickCloudTransfer();
            }));
        }
        itemList.add(new Item(R.drawable.im_tool_shake, R.string.chat_shake, () -> {
            listener.clickShake();
        }));
        itemList.add(new Item(R.drawable.im_tool_card_button_bg, R.string.chat_card, () -> {
            listener.clickCard();
        }));
        itemList.add(new Item(R.drawable.im_tool_collection, R.string.chat_collection, () -> {
            listener.clickCollection();
        }));
        if (Constants.SUPPORT_BIDIRECTIONAL_WITHDRAW) {
            itemList.add(new Item(R.drawable.im_tool_withdrawal, R.string.two_way_withdrawal, () -> {
                listener.clickTwoWayWithdrawal();
            }));
        }
        itemList.add(new Item(R.drawable.im_tool_file_button_bg, R.string.chat_file, () -> {
            listener.clickFile();
        }));
        itemList.add(new Item(R.drawable.im_tool_contacts_button_bg, R.string.send_contacts, () -> {
            listener.clickContact();
        }));
       /* itemList.add(new Item(R.drawable.im_tool_group_assistant_button_norma, R.string.group_assistant, () -> {
            changeGroupAssistant();
            queryGroupAssistant();
        }));*/
        return itemList;
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.chat_tools_view_all, this);

        viewPagerTools = (ViewPager) findViewById(R.id.view_pager_tools);

        gridPagerAdapter = new GridPagerAdapter(
                loadData(), 4, 2,
                data -> new PagerGridAdapter(data, viewPagerTools)
        );
        viewPagerTools.setAdapter(gridPagerAdapter);

        initGroupAssistant();
    }

    public void init(
            ChatBottomView.ChatBottomListener listener,
            String roomId,
            String roomJid,
            boolean isEquipment,
            boolean isGroup,
            boolean disableLocationServer
    ) {
        // 关闭红包功能，移除红包与转账
        if (CoreManager.requireConfig(MyApplication.getContext()).displayRedPacket) {
            gridPagerAdapter.removeAll(
                    R.drawable.im_tool_redpacket_button_bg,
                    R.drawable.im_tool_transfer_button_bg
            );
        }
        this.roomId = roomId;
        this.roomJid = roomJid;
        this.isGroup = isGroup;
        setBottomListener(listener);
        setEquipment(isEquipment);
        setGroup(isGroup);
        setPosition(disableLocationServer);
    }

    public void setBottomListener(ChatBottomView.ChatBottomListener listener) {
        this.listener = listener;
    }

    // 事实上只在初始化ChatToolsViews时调用，所以不需要更新小红点indicator，
    public void setEquipment(boolean isEquipment) {
        if (isEquipment) {// 我的设备 需要隐藏音视频通话、红包、戳一戳
            gridPagerAdapter.removeAll(
                    R.drawable.im_call_button_norma,
                    R.drawable.im_tool_redpacket_button_bg,
                    R.drawable.im_tool_shake
            );
        }
    }

    // 事实上只在初始化ChatToolsViews时调用，所以不需要更新小红点indicator，
    public void setGroup(boolean isGroup) {
        if (isGroup) {// 群组 将通话修改为会议，隐藏戳一戳
            gridPagerAdapter.doEach(item -> {
                if (item.icon == R.drawable.im_call_button_norma) {
                    item.text = R.string.chat_conference;
                }
            });
            // 这里面notify了，
            gridPagerAdapter.removeAll(
                    R.drawable.im_tool_transfer_button_bg
                    , R.drawable.im_tool_shake);
        } else {
            gridPagerAdapter.removeAll(
                    R.drawable.im_tool_group_assistant_button_norma
                    , R.drawable.im_tool_withdrawal);
        }
        if (loadData().size() > 8) {
            ((TabLayout) findViewById(R.id.tabDots)).setupWithViewPager(viewPagerTools, true);
        } else {
            ((TabLayout) findViewById(R.id.tabDots)).setVisibility(INVISIBLE);
        }
    }

    // 事实上只在初始化ChatToolsViews时调用，所以不需要更新小红点indicator，
    public void setPosition(boolean disableLocationServer) {
        if (disableLocationServer) {
            gridPagerAdapter.removeAll(R.drawable.im_tool_loc_button_bg);
        }
    }

    // ╔═══════════════════════════════群助手 Start══════════════════════════════╗
    private void initGroupAssistant() {
        groupAssistantGridView = findViewById(R.id.im_tools_group_assistant_gv);
        groupAssistantAdapter = new GroupAssistantAdapter();
        groupAssistantGridView.setAdapter(groupAssistantAdapter);
        groupAssistantGridView.setOnItemClickListener((parent, view, position, id) -> {
            GroupAssistantDetail groupAssistantDetail = (GroupAssistantDetail) groupAssistantAdapter.getItem(position);
            if (groupAssistantDetail != null) {
                if (TextUtils.equals(groupAssistantDetail.getId(), "001")) {
                    SelectGroupAssistantActivity.start(mContext, roomId, roomJid);
                } else {
                    listener.clickGroupAssistant(groupAssistantDetail);
                }
            }
        });
    }

    private void queryGroupAssistant() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(mContext).accessToken);
        params.put("roomId", roomId);

        HttpUtils.get().url(CoreManager.requireConfig(mContext).ROOM_QUERY_GROUP_HELPER)
                .params(params)
                .build()
                .execute(new ListCallback<GroupAssistantDetail>(GroupAssistantDetail.class) {
                    @Override
                    public void onResponse(ArrayResult<GroupAssistantDetail> result) {
                        if (result != null && result.getResultCode() == 1) {
                            List<GroupAssistantDetail> data = result.getData();

                            //  群主才显示 添加群助手
                            RoomMember roomMember = RoomMemberDao.getInstance().getSingleRoomMember(roomId, CoreManager.requireSelf(mContext).getUserId());
                            if (roomMember != null && roomMember.getRole() == 1) {
                                GroupAssistantDetail groupAssistantDetail = new GroupAssistantDetail();
                                groupAssistantDetail.setId("001");
                                data.add(data.size(), groupAssistantDetail);
                            }

                            if (data.size() == 0) {
                                groupAssistantGridView.setVisibility(GONE);
                                findViewById(R.id.im_tools_group_assistant_ll2).setVisibility(View.VISIBLE);
                            } else {
                                groupAssistantAdapter.setData(data);
                                groupAssistantGridView.setVisibility(VISIBLE);
                                findViewById(R.id.im_tools_group_assistant_ll2).setVisibility(View.GONE);
                            }
                        } else {
                            groupAssistantGridView.setVisibility(VISIBLE);
                            findViewById(R.id.im_tools_group_assistant_ll2).setVisibility(View.GONE);

                            if (result != null && !TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(mContext, result.getResultMsg());
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    public boolean isGroupAssistant() {
        return findViewById(R.id.im_tools_group_assistant_ll).getVisibility() == View.VISIBLE;
    }

    public void changeGroupAssistant() {
        if (isGroupAssistant()) {
            findViewById(R.id.im_tools_group_assistant_ll).setVisibility(GONE);
            findViewById(R.id.im_tools_rl).setVisibility(VISIBLE);
        } else {
            findViewById(R.id.im_tools_group_assistant_ll).setVisibility(VISIBLE);
            findViewById(R.id.im_tools_rl).setVisibility(GONE);
        }
    }

    public void notifyAssistant() {
        queryGroupAssistant();
    }

    static class GridPagerAdapter extends PagerAdapter {
        private final int columnCount;
        private final PagerGridAdapterFactory<Item> factory;
        private final int pageSize;
        private List<Item> data;

        GridPagerAdapter(
                List<Item> data,
                int columnCount,
                int rowCount,
                PagerGridAdapterFactory<Item> factory
        ) {
            this.data = new ArrayList<>(data);
            this.columnCount = columnCount;
            this.factory = factory;

            pageSize = rowCount * columnCount;
        }

        void removeAll(@DrawableRes Integer... ids) {
            Set<Integer> idSet = new HashSet<>(ids.length);
            idSet.addAll(Arrays.asList(ids));
            final Iterator<Item> each = data.iterator();
            while (each.hasNext()) {
                if (idSet.contains(each.next().icon)) {
                    each.remove();
                }
            }
            notifyDataSetChanged();
        }

        void doEach(Function<Item> block) {
            for (Item item : data) {
                block.apply(item);
            }
        }

        private List<Item> getPageData(int page) {
            return data.subList(page * pageSize, Math.min(((page + 1) * pageSize), data.size()));
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            GridView grid = new GridView(container.getContext());
            grid.setNumColumns(columnCount);
            grid.setAdapter(factory.createPagerGridAdapter(getPageData(position)));
            container.addView(grid, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            // 没有设置itemClick和item background，但还是会有点击效果，
            return grid;
        }

        @Override
        public int getCount() {
            return (data.size() + (pageSize - 1)) / pageSize;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        public interface Function<T> {
            void apply(T t);
        }
    }
    // ╚═══════════════════════════════群助手 End══════════════════════════════╝

    static class PagerGridAdapter extends BaseAdapter {
        private final List<Item> data;
        private final ViewPager viewPager;

        PagerGridAdapter(List<Item> data, ViewPager viewPager) {
            this.data = data;
            this.viewPager = viewPager;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Item getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View ret = convertView;
            if (ret == null) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_tools_item, parent, false);
                // 高度手动铺满2行，
                // 没必要自动，还容易出意外，
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                lp.height = DisplayUtil.dip2px(parent.getContext(), 87.5f);
                view.setLayoutParams(lp);
                ret = view;
            }
            TextView tvItem = ret.findViewById(R.id.tvItem);
            Item item = getItem(position);
            tvItem.setText(item.text);
            tvItem.setCompoundDrawablesWithIntrinsicBounds(null, parent.getContext().getResources().getDrawable(item.icon), null, null);
            tvItem.setOnClickListener(v -> {
                item.runnable.run();
            });
            return ret;
        }
    }

    class GroupAssistantAdapter extends BaseAdapter {
        private List<GroupAssistantDetail> mData = new ArrayList<>();

        public void setData(List<GroupAssistantDetail> data) {
            this.mData = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_group_assistant_detail, parent, false);
            }
            ImageView avatar = ViewHolder.get(convertView, R.id.group_assistant_avatar);
            TextView name = ViewHolder.get(convertView, R.id.group_assistant_name);
            ImageView edit = ViewHolder.get(convertView, R.id.group_assistant_edit_iv);
            GroupAssistantDetail groupAssistantDetail = mData.get(position);
            if (groupAssistantDetail != null) {
                if (TextUtils.equals(groupAssistantDetail.getId(), "001")) {
                    avatar.setVisibility(GONE);
                    name.setText("+");
                    name.setGravity(Gravity.CENTER);
                } else {
                    avatar.setVisibility(VISIBLE);
                    AvatarHelper.getInstance().displayUrl(groupAssistantDetail.getHelper().getIconUrl(), avatar);
                    name.setText(groupAssistantDetail.getHelper().getName());
                    name.setGravity(Gravity.CENTER_VERTICAL);
                }

                //  群主才显示 编辑群助手
                edit.setVisibility(VISIBLE);
                RoomMember roomMember = RoomMemberDao.getInstance().getSingleRoomMember(roomId, CoreManager.requireSelf(mContext).getUserId());
                if (roomMember != null && roomMember.getRole() == 1
                        && !TextUtils.equals(groupAssistantDetail.getId(), "001")) {
                    edit.setVisibility(VISIBLE);
                } else {
                    edit.setVisibility(GONE);
                }
                edit.setOnClickListener(v -> {
                    GroupAssistantDetailActivity.start(mContext, roomId, roomJid, JSON.toJSONString(groupAssistantDetail.getHelper()));
                });
            }
            return convertView;
        }
    }
}

class Item {
    @StringRes
    int text;
    @DrawableRes
    int icon;
    Runnable runnable;

    public Item(int icon, int text, Runnable runnable) {
        this.icon = icon;
        this.text = text;
        this.runnable = runnable;
    }
}
