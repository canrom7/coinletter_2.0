package com.iimm.miliao.ui.message;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.EventCreateGroupFriend;
import com.iimm.miliao.bean.EventSendVerifyMsg;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.message.MucRoom;
import com.iimm.miliao.broadcast.MucgroupUpdateUtil;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.pay.PaymentReceiptMoneyActivity;
import com.iimm.miliao.pay.ReceiptPayMoneyActivity;
import com.iimm.miliao.ui.account.PublicAccountScannerActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.other.BasicInfoActivity;
import com.iimm.miliao.ui.tool.WebViewActivity;
import com.iimm.miliao.util.HttpUtil;
import com.iimm.miliao.util.RegexUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.view.VerifyDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import okhttp3.Call;


/**
 * 处理图片预览时长按识别图中二维码的Util
 */
public class HandleQRCodeScanUtil {

    /**
     * 扫描二维码之后的处理
     */
    public static void handleScanResult(Context context, String result) {
        Log.e("zq", "二维码扫描结果：" + result);
        if (TextUtils.isEmpty(result)) {
            return;
        }
        if (result.length() == 20 && RegexUtils.checkDigit(result)) {
            // 长度为20且 && 纯数字 扫描他人的付款码 弹起收款界面
            Intent intent = new Intent(context, PaymentReceiptMoneyActivity.class);
            intent.putExtra("PAYMENT_ORDER", result);
            context.startActivity(intent);
        } else if (result.contains("userId")
                && result.contains("userName")) {
            // 扫描他人的收款码 弹起付款界面
            Intent intent = new Intent(context, ReceiptPayMoneyActivity.class);
            intent.putExtra("RECEIPT_ORDER", result);
            context.startActivity(intent);
        } else if (result.contains("pub&acc") && result.length() == 26) {
            //进入扫描公众号登陆逻辑中
            Intent intent = new Intent(context, PublicAccountScannerActivity.class);
            intent.putExtra("result", result);
            context.startActivity(intent);
        } else if (result.contains("pub&open&acc")) {
            //进入扫描公众号登陆逻辑中
            Intent intent = new Intent(context, PublicAccountScannerActivity.class);
            intent.putExtra("result", result);
            context.startActivity(intent);
        } else if (result.contains("user&login") && result.length() == 30) {
            //Pc端扫码登录
            Intent intent = new Intent(context, PublicAccountScannerActivity.class);
            intent.putExtra("result", result);
            context.startActivity(intent);
        } else {
            if (result.contains("tigId")) {
                // 二维码
                Map<String, String> map = WebViewActivity.URLRequest(result);
                String action = map.get("action");
                String userId = map.get("tigId");
                if (TextUtils.equals(action, "group")) {
                    getRoomInfo(context, userId);
                } else if (TextUtils.equals(action, "user")) {
                    Intent intent = new Intent(context, BasicInfoActivity.class);
                    intent.putExtra(AppConstant.EXTRA_USER_ID, userId);
                    context.startActivity(intent);
                } else {
                    Reporter.post("二维码无法识别，<" + result + ">");
                    ToastUtil.showToast(context, R.string.unrecognized);
                }
            } else if (!result.contains("tigId")
                    && HttpUtil.isURL(result)) {
                // 非sk二维码  访问其网页
                Intent intent = new Intent(context, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, result);
                context.startActivity(intent);
            } else {
                Reporter.post("二维码无法识别，<" + result + ">");
                ToastUtil.showToast(context, context.getString(R.string.unrecognized));
            }
        }
    }

    /**
     * 获取房间信息
     */
    private static void getRoomInfo(Context context, String roomId) {
        String mLoginUserId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
        Friend friend = FriendDao.getInstance().getMucFriendByRoomId(mLoginUserId, roomId);
        if (friend != null) {
            if (friend.getGroupStatus() == 0) {
                interMucChat(context, friend.getUserId(), friend.getNickName());
                return;
            } else {// 已被踢出该群组 || 群组已被解散
                FriendDao.getInstance().deleteFriend(mLoginUserId, friend.getUserId());
                ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, friend.getUserId());
            }
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken);
        params.put("roomId", roomId);

        HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getInstance()).ROOM_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<MucRoom>(MucRoom.class) {

                    @Override
                    public void onResponse(ObjectResult<MucRoom> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            final MucRoom mucRoom = result.getData();
                            if (mucRoom.getIsNeedVerify() == 1) {
                                VerifyDialog verifyDialog = new VerifyDialog(MyApplication.getInstance());
                                verifyDialog.setVerifyClickListener(MyApplication.getInstance().getString(R.string.tip_reason_invite_friends), new VerifyDialog.VerifyClickListener() {
                                    @Override
                                    public void cancel() {

                                    }

                                    @Override
                                    public void send(String str) {
                                        EventBus.getDefault().post(new EventSendVerifyMsg(mucRoom.getUserId(), mucRoom.getJid(), str));
                                    }
                                });
                                verifyDialog.show();
                                return;
                            }
                            joinRoom(context, mucRoom, mLoginUserId);
                        } else {
                            ToastUtil.showToast(context, TextUtils.isEmpty(result.getResultMsg()) ? context.getResources().getString(R.string.failed_to_enter_group_chat) : result.getResultMsg());

                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showToast(context,R.string.get_room_info_error_room_nothing);

                    }
                });
    }

    /**
     * 加入房间
     */
    private static void joinRoom(Context context, final MucRoom room, final String loginUserId) {
        DialogHelper.showDefaulteMessageProgressDialog(context);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken);
        params.put("roomId", room.getId());
        if (room.getUserId().equals(loginUserId))
            params.put("type", "1");
        else
            params.put("type", "2");

        MyApplication.mRoomKeyLastCreate = room.getJid();

        HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getInstance()).ROOM_JOIN)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            EventBus.getDefault().post(new EventCreateGroupFriend(room));
                            TimerTask task = new TimerTask() {
                                @Override
                                public void run() {
                                    // 延时500ms加入，防止群组还未创建好就进入群聊天界面
                                    interMucChat(context, room.getJid(), room.getName());
                                }
                            };
                            Timer timer = new Timer();
                            timer.schedule(task, 500);
                        } else {
                            MyApplication.mRoomKeyLastCreate = "compatible";
                            ToastUtil.showToast(context, result.getResultMsg() + "");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        MyApplication.mRoomKeyLastCreate = "compatible";
                        ToastUtil.showErrorNet(MyApplication.getInstance());
                    }
                });
    }

    /**
     * 进入房间
     */
    private static void interMucChat(Context context, String roomJid, String roomName) {
        Intent intent = new Intent(context, MucChatActivity.class);
        intent.putExtra(AppConstant.EXTRA_USER_ID, roomJid);
        intent.putExtra(AppConstant.EXTRA_NICK_NAME, roomName);
        intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
        context.startActivity(intent);

        MucgroupUpdateUtil.broadcastUpdateUi(context);
    }
}
