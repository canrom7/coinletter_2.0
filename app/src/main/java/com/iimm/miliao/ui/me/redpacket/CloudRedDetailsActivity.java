package com.iimm.miliao.ui.me.redpacket;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gyf.immersionbar.ImmersionBar;
import com.payeasenet.wepay.ui.activity.RedPacketsActivity;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.RoomMember;
import com.iimm.miliao.bean.redpacket.CloudQueryRedPacket;
import com.iimm.miliao.bean.redpacket.ReceiversBean;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.RoomMemberDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * modify by zq
 * 单聊 普通云红包自己都不可领取
 * 群组 手气云红包自己可领取，普通红包不可领取
 */
public class CloudRedDetailsActivity extends BaseActivity implements View.OnClickListener {
    LayoutInflater inflater;
    DecimalFormat df = new DecimalFormat("######0.00");
    private ImageView red_head_iv;
    private TextView red_nickname_tv;
    private TextView red_words_tv;
    private TextView red_money_tv;
    private TextView red_money_bit_tv;
    private TextView red_reply_tv;
    private TextView red_resultmsg_tv;
    private ListView red_details_lsv;
    private RelativeLayout get_money_bit_rl;
    private RedAdapter mRedAdapter;
    private CloudQueryRedPacket packetEntity;
    private List<ReceiversBean> list;
    private int timeOut;    // 标记红包是否已过时
    private boolean isGroup;// 是否为群组
    private String mToUserId; // userId || 群组jid
    private Friend mFriend; // 通过该mFriend，获取备注名、获取群成员表显示群内昵称
    private String resultMsg, redMsg;
    private Map<String, String> mGroupNickNameMap = new HashMap<>();
    private String requestId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redpacket_details);
        Bundle bundle = getIntent().getExtras();
        requestId = bundle.getString("requestId");
        timeOut = bundle.getInt("timeOut");
        isGroup = bundle.getBoolean("isGroup", false);
        mToUserId = bundle.getString("mToUserId");
        inflater = LayoutInflater.from(this);
        initView();
        getRedpacket();
    }

    private void initView() {
        getSupportActionBar().hide();

        View view = findViewById(R.id.battery_bar_v);
        ImmersionBar.with(this).statusBarView(view)
                .init();
        red_head_iv = (ImageView) findViewById(R.id.red_head_iv);
        red_nickname_tv = (TextView) findViewById(R.id.red_nickname_tv);
        red_words_tv = (TextView) findViewById(R.id.red_words_tv);
        red_money_tv = (TextView) findViewById(R.id.get_money_tv);
        red_money_bit_tv = (TextView) findViewById(R.id.get_money_bit_tv);
        red_reply_tv = (TextView) findViewById(R.id.reply_red_tv);
        red_reply_tv.setVisibility(View.INVISIBLE);
        get_money_bit_rl = findViewById(R.id.get_money_bit_rl);

        red_resultmsg_tv = (TextView) findViewById(R.id.red_resultmsg_tv);
        red_details_lsv = (ListView) findViewById(R.id.red_details_lsv);

        mFriend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), mToUserId);
        if (isGroup && mFriend != null) {// 群组红包 获取群内昵称 之后显示
            List<RoomMember> mRoomMemberList = RoomMemberDao.getInstance().getRoomMember(mFriend.getRoomId());
            if (mRoomMemberList != null && mRoomMemberList.size() > 0) {
                for (int i = 0; i < mRoomMemberList.size(); i++) {
                    RoomMember mRoomMember = mRoomMemberList.get(i);
                    mGroupNickNameMap.put(mRoomMember.getUserId(), mRoomMember.getUserName());
                }
            }
        }

        red_reply_tv.setOnClickListener(this);
        findViewById(R.id.red_back_tv).setOnClickListener(this);
        findViewById(R.id.get_redlist_tv).setOnClickListener(this);
    }

    public void getRedpacket() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        final String token = CoreManager.requireSelfStatus(mContext).accessToken;
        Map<String, String> params = new HashMap<>();
        params.put("access_token", token);
        params.put("requestId", requestId);
        HttpUtils.post().url(CoreManager.requireConfig(mContext).REDPACKET_CREATE_INQUIRE)
                .params(params)
                .build()
                .execute(new BaseCallback<CloudQueryRedPacket>(CloudQueryRedPacket.class) {

                    @Override
                    public void onResponse(ObjectResult<CloudQueryRedPacket> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getData() != null) {
                            list = result.getData().getReceivers();
                            packetEntity = result.getData();
                            showData();
                        } else {
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        // 发送红包失败，
                        ToastUtil.showToast(mContext, "发红包失败");
                    }
                });


  /*      HashMap<String, String> params = new HashMap<>();
        params.put("access_token", token);
        params.put("id", redId);

        HttpUtils.get().url(CoreManager.requireConfig(mContext).RENDPACKET_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<OpenRedpacket>(OpenRedpacket.class) {

                    @Override
                    public void onResponse(ObjectResult<OpenRedpacket> result) {
                        if (result.getData() != null) {
                            // 当resultCode==1时，表示可领取
                            // 当resultCode==0时，表示红包已过期、红包已退回、红包已领完
                            int resultCode = result.getResultCode();
                            OpenRedpacket openRedpacket = result.getData();
                            Bundle bundle = new Bundle();
                            Intent intent = new Intent(mContext, RedDetailsActivity.class);
                            bundle.putSerializable("openRedpacket", openRedpacket);
                            bundle.putInt("redAction", 0);
                            if (!TextUtils.isEmpty(result.getResultMsg())) //resultMsg不为空表示红包已过期
                            {
                                bundle.putInt("timeOut", 1);
                            } else {
                                bundle.putInt("timeOut", 0);
                            }

                            bundle.putBoolean("isGroup", isGroup);
                            bundle.putString("mToUserId", mToUserId);
                            intent.putExtras(bundle);

                            // 红包不可领取, 或者我发的单聊红包直接跳转
                            if (resultCode != 1 || (!isGroup && isMysend)) {
                                mContext.startActivity(intent);
                            } else {
                                // 在群里面我领取过的红包直接跳转
                                if (isGroup && mdata.getFileSize() != 1) {
                                    mContext.startActivity(intent);
                                } else {
                                    if (mdata.getFilePath().equals("3")) {
                                        // 口令红包编辑输入框
                                        changeBottomViewInputText(mdata.getContent());
                                    } else {
                                        RedDialogBean redDialogBean = new RedDialogBean(openRedpacket.getPacket().getUserId(), openRedpacket.getPacket().getUserName(),
                                                openRedpacket.getPacket().getGreetings(), openRedpacket.getPacket().getId(), openRedpacket.getPacket().getType(), CoreManager.getSelf(mContext).getRedPacketVip(), isGroup);
                                        mRedDialog = new RedDialog(mContext, redDialogBean, new RedDialog.OnClickRedListener() {
                                            @Override
                                            public void clickRed() {
                                                openRedPacket(token, redId, null);
                                            }

                                            *//**
         * 特权红包
         * @param money
         *//*
                                            @Override
                                            public void clickPrivilege(String money) {
                                                openRedPacket(token, redId, money);
                                            }
                                        });

                                        mRedDialog.show();
                                    }
                                }
                            }
                        } else {
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        }

                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });*/
    }

    private void showData() {
        if (list == null) {
            list = new ArrayList<>();
        }
        AvatarHelper.getInstance().displayAvatar(packetEntity.getNickName(), packetEntity.getUserId(), red_head_iv, true);
        Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), packetEntity.getUserId());
        String name  = "";
        if (friend != null) {
            name = TextUtils.isEmpty(friend.getRemarkName()) ? packetEntity.getNickName() : friend.getRemarkName();
        } else {
            name = packetEntity.getNickName();
        }
        red_nickname_tv.setText(getString(R.string.someone_s_red_packet_place_holder, name));
        red_words_tv.setText(packetEntity.getGreeting());

        boolean isReceivedSelf = false;
        for (ReceiversBean entity : list) {
            if (entity.getUserId().equals(coreManager.getSelf().getUserId())) {
                isReceivedSelf = true;
                red_money_tv.setText(entity.getAmount());
//                if (!TextUtils.isEmpty(df.format(entity.getAmount()))) {
//                    red_money_bit_tv.setText(R.string.rmb);
//                    red_reply_tv.setText(TextUtils.isEmpty(entity.getReply()) ? getString(R.string.reply_red_thank) : entity.getReply());
//                }
            }
        }

        if (!isReceivedSelf) {// 没有领取红包，隐藏回复按钮
            get_money_bit_rl.setVisibility(View.GONE);
        } else {
            get_money_bit_rl.setVisibility(View.VISIBLE);
        }

        if (packetEntity.getPacketType() == 1) {//个人红包 || 群聊普通红包
            resultMsg = getString(R.string.red_packet_receipt_place_holder, list.size(), packetEntity.getPacketCount(),
                    df.format(new BigDecimal(packetEntity.getReceivedAmount())),
                    df.format(new BigDecimal(packetEntity.getPacketCount()).multiply(new BigDecimal(packetEntity.getSingleAmount()))));
        } else if (packetEntity.getPacketType() == 2) {//群聊手气红包
            resultMsg = getString(R.string.red_packet_receipt_place_holder, list.size(), packetEntity.getPacketCount(),
                    df.format(new BigDecimal(packetEntity.getReceivedAmount())),
                    df.format(new BigDecimal(packetEntity.getAmount())));
        }


        if (list.size() == packetEntity.getPacketCount()) {
            redMsg = getString(R.string.red_packet_receipt_suffix_all);
        } else if (packetEntity.getOrderStatus() == -1) {
            redMsg = getString(R.string.red_packet_receipt_suffix_over);
        } else if (packetEntity.getOrderStatus() == 3) {
            redMsg = getString(R.string.red_packet_receipt_suffix_remain);
        } else {
            redMsg = getString(R.string.red_packet_receipt_suffix_remain);
        }

        red_resultmsg_tv.setText(resultMsg + redMsg);
        mRedAdapter = new RedAdapter();
//        mRedAdapter.preload();
        red_details_lsv.setAdapter(mRedAdapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_title_left || v.getId() == R.id.red_back_tv) {
            finish();
        } else if (v.getId() == R.id.get_redlist_tv) {
            Intent intent = new Intent(CloudRedDetailsActivity.this, RedPacketsActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.reply_red_tv) {
            DialogHelper.ChangeRoomNameDialog(this, getString(R.string.replay),
                    getString(R.string.reply_red_thank) + getString(R.string.input_most_length, 10), 1, 1, 10, v1 -> {
                        final String text = ((EditText) v1).getText().toString().trim();
                        if (TextUtils.isEmpty(text)) {
                            return;
                        }
//                        replyRed(text);
                    });
        }
    }

    // 查看红包领取详情
   /* private void replyRed(String reply) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(mContext).accessToken);
        params.put("id", openRedpacket.getPacket().getId());
        params.put("reply", reply);

        HttpUtils.get().url(CoreManager.requireConfig(mContext).RENDPACKET_REPLY)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            for (int i = 0; i < openRedpacket.getList().size(); i++) {
                                if (openRedpacket.getList().get(i).getUserId().equals(coreManager.getSelf().getUserId())) {
                                    red_reply_tv.setText(reply);
                                    openRedpacket.getList().get(i).setReply(reply);
                                    mRedAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }*/

    private class RedAdapter extends BaseAdapter {
        View view;
        private String lucklyUserId;

        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ReceiversBean listEntity = list.get(position);

            view = inflater.inflate(R.layout.reditem_layout, null);
            String name;
            if (isGroup) {
                if (mGroupNickNameMap.size() > 0 && mGroupNickNameMap.containsKey(listEntity.getUserId())) {
                    Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), listEntity.getUserId());
                    if (friend != null) {
                        name = TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName();
                    } else {
                        name = mGroupNickNameMap.get(listEntity.getUserId());
                    }
                } else {
                    Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), listEntity.getUserId());
                    if (friend != null) {
                        name = TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName();
                    } else {
                        name = listEntity.getNickname();
                    }
                }
            } else {
                if (listEntity.getUserId().equals(coreManager.getSelf().getUserId())) {// 自己领取了
//                    name = listEntity.getNickname();
                    name = coreManager.getSelf().getNickName();
                } else {
                    if (mFriend != null) {
                        name = TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName();
                    } else {
                        name = listEntity.getNickname();
                    }
                }
            }
            // 手气红包 && 红包已领完 显示手气最佳
            if (packetEntity.getPacketType() == 2
                    && (packetEntity.getPacketCount() == packetEntity.getReceivedCount())
                    && listEntity.isLucky()) {//&& TextUtils.equals(lucklyUserId, listEntity.getUserId())
                view.findViewById(R.id.best_lucky_ll).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.best_lucky_ll).setVisibility(View.GONE);
            }
            AvatarHelper.getInstance().displayAvatar(name, listEntity.getUserId(), (ImageView) view.findViewById(R.id.red_head_iv), true);
            ((TextView) view.findViewById(R.id.username_tv)).setText(name);
            ((TextView) view.findViewById(R.id.opentime_tv)).setText(listEntity.getCompleteDateTime());
            ((TextView) view.findViewById(R.id.money_tv)).setText(df.format(new BigDecimal(listEntity.getAmount())) + getString(R.string.rmb));
//            if (!TextUtils.isEmpty(listEntity.getReply())) {
//                ((TextView) view.findViewById(R.id.reply_tv)).setText(listEntity.getReply());
//            }
            return view;
        }

       /* public void preload() {
            // 按时间排序，避免手气最佳固定在最上面，
            Collections.sort(list, (o1, o2) ->
                    // 时间大的排上面，
                    -Integer.compare(o1.getCompleteDateTime(), o2.getCompleteDateTime())
            );
            if (openRedpacket.getPacket().getCount() == openRedpacket.getList().size()) {
                // 计算手气最佳，
                OpenRedpacket.ListEntity max = Collections.max(list, (o1, o2) -> {
                            // 计算出领取金额最大的用户，
                            int dMoney = Double.compare(o1.getMoney(), o2.getMoney());
                            // 如果存在领取金额一样的，取时间小的，
                            if (dMoney == 0) {
                                return -Integer.compare(o1.getTime(), o2.getTime());
                            }
                            return dMoney;
                        }

                );
                lucklyUserId = max.getUserId();
            }
        }*/
    }
}
