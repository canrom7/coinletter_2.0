package com.iimm.miliao.util;

import com.iimm.miliao.MyApplication;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.xmpp.util.ImHelper;

import java.util.List;

public class ThreadJoinGroupUtil {
    private List<Friend> allRoom;
//    private int poolcount = 5;
//    private int poolingcount = 1;
//    private int position;
    private String TAG="ThreadJoinGroupUtil";
    private int lastOffLineTime;
    public ThreadJoinGroupUtil(List<Friend> allRoom, int lastOffLineTime) {
        this.lastOffLineTime = lastOffLineTime;
        this.allRoom = allRoom;
    }
    public void start(){
        for (Friend friend : allRoom) {
            AppExecutors.getInstance().networkIO().execute(new Runnable() {
                @Override
                public void run() {
                    ChatMessage mLastChatMessage = ChatMessageDao.getInstance().getLastChatMessage(MyApplication.getLoginUserId(), friend.getUserId());
                    if (mLastChatMessage != null) {// 如果该群组的最后一条消息不为空，将该条消息的timeSend作为当前群组的离线时间，这样比上面全局的离线时间更加准确
                        int lastMessageTimeSend = (int) (TimeUtils.time_current_time() - mLastChatMessage.getTimeSend());
//                    ImHelper.joinMucChat(friend.getUserId(), lastMessageTimeSend + 30);
                        ImHelper.joinMucChat(friend.getUserId(), lastMessageTimeSend );
                    } else {// 该群组本地无消息记录，取全局的离线时间
                        ImHelper.joinMucChat(friend.getUserId(), lastOffLineTime);
                    }
                }
            });
        }
    }
//    private void  logic(){
//
//        if(poolingcount>poolcount){
//            position--;
//            return;
//        }else {
//            if(position>=list.size()){
//                return;
//            }
//        }
//        Friend friend = list.get(position);
//        AppExecutors.getInstance().networkIO().execute(new Runnable() {
//            @Override
//            public void run() {
//                ChatMessage mLastChatMessage = ChatMessageDao.getInstance().getLastChatMessage(MyApplication.getLoginUserId(), friend.getUserId());
//                if (mLastChatMessage != null) {// 如果该群组的最后一条消息不为空，将该条消息的timeSend作为当前群组的离线时间，这样比上面全局的离线时间更加准确
//                    int lastMessageTimeSend = (int) (TimeUtils.time_current_time() - mLastChatMessage.getTimeSend());
////                    ImHelper.joinMucChat(friend.getUserId(), lastMessageTimeSend + 30);
//                    ImHelper.joinMucChat(friend.getUserId(), lastMessageTimeSend );
//                } else {// 该群组本地无消息记录，取全局的离线时间
//                    ImHelper.joinMucChat(friend.getUserId(), lastSeconds);
//                }
//
//                poolingcount--;
//                logic();
//            }
//        });
//        position++;
//        poolingcount++;
//    }
}
