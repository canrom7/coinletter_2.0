package com.iimm.miliao.db.dao;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.AttentionUser;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.RoomMember;
import com.iimm.miliao.bean.User;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.bean.message.MucRoom;
import com.iimm.miliao.bean.message.MucRoomMember;
import com.iimm.miliao.bean.message.NewFriendMessage;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.SQLiteHelper;
import com.iimm.miliao.db.SQLiteRawUtil;
import com.iimm.miliao.sp.FriendTableExpandSp;
import com.iimm.miliao.sp.TableVersionSp;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.TanX;
import com.iimm.miliao.util.ThreadManager;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToolUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 访问朋友数据的Dao
 */
public class FriendDao {
    private String TAG = "FriendDao";
    private static FriendDao instance = null;
    public Dao<Friend, Integer> friendDao;
    private SQLiteHelper mHelper;

    private FriendDao() {
        try {
            mHelper = OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class);
            friendDao = DaoManager.createDao(mHelper.getConnectionSource(), Friend.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static final FriendDao getInstance() {
        if (instance == null) {
            synchronized (FriendDao.class) {
                if (instance == null) {
                    instance = new FriendDao();
                }
            }
        }
        return instance;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        OpenHelperManager.releaseHelper();
    }

    /**
     * 生成两个系统号
     */
    public void checkSystemFriend(String ownerId) {
        try {
            Friend friend = getFriend(ownerId, Constants.ID_SYSTEM_MESSAGE);
            if (friend == null) {// 公众号
                friend = new Friend();
                friend.setOwnerId(ownerId);
                friend.setUserId(Constants.ID_SYSTEM_MESSAGE);
                friend.setNickName(MyApplication.getInstance().getString(R.string.system_public_number));
                friend.setRemarkName(MyApplication.getInstance().getString(R.string.system_public_number));
                friend.setStatus(Constants.STATUS_SYSTEM);
                //friend.setContent(MyApplication.getInstance().getString(R.string.system_public_number_welcome));
                friendDao.create(friend);
                // 添加一条系统提示
/*
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setType(Constants.TYPE_TIP);
                chatMessage.setPacketId(ToolUtils.getUUID());// 随机产生一个PacketId
                chatMessage.setFromUserId(Constants.ID_SYSTEM_MESSAGE);
                chatMessage.setMessageState(Constants.MESSAGE_SEND_SUCCESS);
                // 为了使得初始生成的系统消息排在新朋友前面，所以在时间节点上延迟一点 1s
                chatMessage.setDoubleTimeSend(TimeUtils.time_current_time() + 1);
                chatMessage.setContent(MyApplication.getInstance().getString(R.string.system_public_number_welcome));
                chatMessage.setMySend(false);// 表示不是自己发的
                // 往消息表里插入一条记录
                ChatMessageDao.getInstance().saveNewSingleChatMessage(ownerId, Constants.ID_SYSTEM_MESSAGE, chatMessage);
                // 往朋友表里面插入一条未读记录
                markUserMessageUnRead(ownerId, Constants.ID_SYSTEM_MESSAGE);
*/
            }

            friend = getFriend(ownerId, Constants.ID_NEW_FRIEND_MESSAGE);
            if (friend == null) {// 新的朋友
                friend = new Friend();
                friend.setOwnerId(ownerId);
                friend.setUserId(Constants.ID_NEW_FRIEND_MESSAGE);
                friend.setNickName(InternationalizationHelper.getString("JXNewFriendVC_NewFirend"));
                friend.setRemarkName(InternationalizationHelper.getString("JXNewFriendVC_NewFirend"));
                friend.setStatus(Constants.STATUS_SYSTEM);
                friendDao.create(friend);

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setType(Constants.TYPE_TIP);
                chatMessage.setPacketId(ToolUtils.getUUID());// 随机产生一个PacketId
                chatMessage.setFromUserId(Constants.ID_NEW_FRIEND_MESSAGE);
                chatMessage.setMessageState(Constants.MESSAGE_SEND_SUCCESS);
                chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                chatMessage.setContent("");
                chatMessage.setMySend(false);// 表示不是自己发的
                // 更新消息记录
                updateLastChatMessage(ownerId, Constants.ID_NEW_FRIEND_MESSAGE, chatMessage);
            }

            friend = getFriend(ownerId, Constants.ID_SK_PAY);
            if (friend == null) {// 支付公众号，
                friend = new Friend();
                friend.setOwnerId(ownerId);
                friend.setUserId(Constants.ID_SK_PAY);
                friend.setNickName(MyApplication.getInstance().getString(R.string.easy_pay));
                friend.setRemarkName(MyApplication.getInstance().getString(R.string.easy_pay));
                friend.setStatus(Constants.STATUS_SYSTEM);
                friendDao.create(friend);
            }

            checkDevice(ownerId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 我的设备
    public void checkDevice(String ownerId) {
        // TODO 我的设备
        if (!MyApplication.IS_SUPPORT_MULTI_LOGIN) {
            List<Friend> friendList = getDevice(ownerId);
            for (Friend f : friendList) {
                deleteFriend(ownerId, f.getUserId());
                ChatMessageDao.getInstance().deleteMessageTable(ownerId, f.getUserId());
            }
        } else {
            for (String s : MyApplication.machine) {
                Friend friend = getFriend(ownerId, s);
                if (friend == null) {
                    friend = new Friend();
                    friend.setOwnerId(ownerId);
                    friend.setUserId(s);
                    if (s.equals("ios")) {
                        friend.setNickName(MyApplication.getInstance().getString(R.string.my_iphone));
                        friend.setRemarkName(MyApplication.getInstance().getString(R.string.my_iphone));
                    } else if (s.equals("pc")) {
                        friend.setNickName(MyApplication.getInstance().getString(R.string.my_windows));
                        friend.setRemarkName(MyApplication.getInstance().getString(R.string.my_windows));
                    } else if (s.equals("mac")) {
                        friend.setNickName(MyApplication.getInstance().getString(R.string.my_mac));
                        friend.setRemarkName(MyApplication.getInstance().getString(R.string.my_mac));
                    } else {
                        friend.setNickName(MyApplication.getInstance().getString(R.string.my_web));
                        friend.setRemarkName(MyApplication.getInstance().getString(R.string.my_web));
                    }
                    friend.setIsDevice(1);// 标志该朋友为其它设备(userId本质为自己)
                    // friend.setStatus(Constants.STATUS_FRIEND);
                    friend.setStatus(Constants.STATUS_SYSTEM);// 将状态改为系统号，否则在更新朋友表的时候，因为服务器attentionList内 未存自己，在清除旧数据的时候会清除掉自己
                    try {
                        friendDao.create(friend);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 更新与某个好友的阅读状态为已读
     */
    public void markUserMessageRead(String ownerId, String friendId) {
        TanX.Log("markUserMessageRead----" + friendId + "设置为已读");
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            builder.updateColumnValue("unReadNum", 0);
            builder.where().eq("ownerId", ownerId).and().eq("userId", friendId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            Log.e("markUserMessageRead", e.getMessage() + "");
            e.printStackTrace();
        }
    }

    /**
     * 更新某个好友的阅读状态，+1条未读信息
     */
    public boolean markUserMessageUnRead(String ownerId, String friendId) {
        Log.e(TAG, "+1条未读消息");
        try {
            PreparedQuery<Friend> preparedQuery = friendDao.queryBuilder().where()
                    .eq("ownerId", ownerId).and().eq("userId", friendId)
                    .prepare();
            List<Friend> friendsList = friendDao.query(preparedQuery);
            if (friendsList != null && friendsList.size() > 0) {
                // Todo 之前发现群组收到消息但角标一直不更新，调试发现本地存在两个一模一样的Friend，而下面只是取出第一个Friend来更新，但消息界面显示的是第二个Friend(两个Friend可能由频繁切换账号引起)
                Friend friend = friendsList.get(0);
                int unReadCount = friend.getUnReadNum();
                friend.setUnReadNum(++unReadCount);
                friendDao.update(friend);
                // Todo 调用获取群组的接口，更新本地群组
                if (friendsList.size() > 1) {
                    return true;
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addNewFriendInMsgTable(String loginUserId, String friendId) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(Constants.TYPE_TIP);
        chatMessage.setFromUserId(friendId);
        chatMessage.setContent(InternationalizationHelper.getString("JXMsgViewController_StartChat"));
        chatMessage.setMySend(false);// 表示不是自己发的
        chatMessage.setPacketId(ToolUtils.getUUID());// 随机产生一个PacketId
        chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
        chatMessage.setMessageState(Constants.MESSAGE_SEND_SUCCESS);
        // 往消息表里插入一条记录
        ChatMessageDao.getInstance().saveNewSingleChatMessage(loginUserId, friendId, chatMessage);
        // 往朋友表里面插入一条未读记录
        markUserMessageUnRead(loginUserId, friendId);
        return true;
    }

    /* 获取消息模块未读数量总和 */
    public int getMsgUnReadNumTotal(String ownerId) {
        try {
            Where<Friend, Integer> builder = friendDao.queryBuilder()
                    .selectRaw("ifnull(sum(unReadNum), 0)")
                    .where().eq("ownerId", ownerId)
                    .and().in("status", Constants.STATUS_FRIEND, Constants.STATUS_SYSTEM, Constants.STATUS_UNKNOW)
                    .and().ne("userId", Constants.ID_NEW_FRIEND_MESSAGE)
                    .and().ne("userId", ownerId)
                    .and().isNotNull("content");
            return Integer.valueOf(builder.queryRawFirst()[0]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 查询所有最近聊天的好友 全部
     */
    public List<Friend> getNearlyFriendMsg(String ownerId) {
        List<Friend> friends = new ArrayList<>();
        try {
            // 过滤条件，content不为空，status == 0 ||status ==2 || status==8（陌生人||好友||系统号）
            QueryBuilder<Friend, Integer> builder = friendDao.queryBuilder();
            /*
            TODO: 过滤不应该放在查询后，但是这个方法用在多个地方，过滤条件不一样，应该改成多个查询方法，
                    .where().eq("ownerId", ownerId)
                    .and().in("status", Constants.STATUS_FRIEND, Constants.STATUS_SYSTEM, Constants.STATUS_UNKNOW)
                    .and().ne("userId", Constants.ID_NEW_FRIEND_MESSAGE)
                    .and().ne("userId", ownerId)
                    .and().isNotNull("content");
             */
            builder.where().eq("ownerId", ownerId).or()
                    .eq("status", Constants.STATUS_UNKNOW).or()
                    .eq("status", Constants.STATUS_FRIEND).and()
                  /*  .eq("status", Constants.STATUS_SYSTEM).and()*/
                    .isNotNull("content");
            builder.orderBy("topTime", false);
            builder.orderBy("timeSend", false);
            friends = builder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Map<String, Friend> mFriendMap = new LinkedHashMap<>();
        if (friends != null && friends.size() > 0) {// 偶然发现该方法有时会查询出多条重复数据，去重
            for (int i = 0; i < friends.size(); i++) {
                if(friends.get(i).getOwnerId().equalsIgnoreCase(ownerId)) {
                    mFriendMap.put(friends.get(i).getUserId(), friends.get(i));
                }
            }
            Collection<Friend> values = mFriendMap.values();
            friends = new ArrayList<>(values);
        }

        // 置顶的Friend也根据timeSend排序
        if (friends != null) {
            mFriendMap.clear();
            for (int i = 0; i < friends.size(); i++) {
                if (friends.get(i).getTopTime() != 0) {
                    if(friends.get(i).getOwnerId().equalsIgnoreCase(ownerId)) {
                        mFriendMap.put(friends.get(i).getUserId(), friends.get(i));
                    }
                }
            }
            Collection<Friend> values = mFriendMap.values();
            List<Friend> topFriends = new ArrayList<>(values);
            Comparator<Friend> comparator = (o1, o2) -> (int) (o1.getTimeSend() - o2.getTimeSend());
            Collections.sort(topFriends, comparator);

            for (int i = 0; i < topFriends.size(); i++) {
                friends.remove(topFriends.get(i));
                friends.add(0, topFriends.get(i));
            }
        }

        return friends;
    }

    /**
     * 获取备注名
     */
    public String getRemarkName(String ownerId, String userId) {
        QueryBuilder<Friend, Integer> builder = friendDao.queryBuilder();
        builder.selectRaw("remarkName");
        try {
            builder.where().eq("ownerId", ownerId).and().eq("userId", userId);
            GenericRawResults<String[]> results = friendDao.queryRaw(builder.prepareStatementString());
            if (results != null) {
                String[] first = results.getFirstResult();
                if (first != null && first.length > 0) {
                    return first[0];
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void resetFriendMessage(String loginUserId, String userId) {
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            builder.updateColumnValue("unReadNum", 0);
            builder.updateColumnValue("content", null);
            builder.where().eq("ownerId", loginUserId).and().eq("userId", userId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteFriend(String ownerId, String friendId) {
        try {
            DeleteBuilder<Friend, Integer> builder = friendDao.deleteBuilder();
            builder.where().eq("ownerId", ownerId).and().eq("userId", friendId);
            friendDao.delete(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建群组
     */
    public boolean createOrUpdateFriend(Friend friend) {
        try {
            CreateOrUpdateStatus status = friendDao.createOrUpdate(friend);
            return status.isCreated() || status.isUpdated();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 创建好友
     */
    public boolean createOrUpdateFriendByNewFriend(NewFriendMessage newFriend, int friendStatus) {
        try {
            String ownerid = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
            Log.e(">>>>","创建好友："+ownerid);

            Friend existFriend = getFriend(newFriend.getOwnerId(), newFriend.getUserId());
            if (existFriend == null) {
                existFriend = new Friend();
                existFriend.setOwnerId(newFriend.getOwnerId());
                existFriend.setUserId(newFriend.getUserId());
                existFriend.setNickName(newFriend.getNickName());
                existFriend.setTimeCreate(TimeUtils.time_current_time());
                existFriend.setCompanyId(newFriend.getCompanyId());
                existFriend.setVersion(TableVersionSp.getInstance(MyApplication.getInstance()).getFriendTableVersion(newFriend.getOwnerId()));
            }
            existFriend.setStatus(friendStatus);
            CreateOrUpdateStatus status = friendDao.createOrUpdate(existFriend);
            return status.isCreated() || status.isUpdated();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 将陌生人加入朋友表
     */
    public void createNewFriend(ChatMessage chatMessage) {
        Friend friend = new Friend();
        String ownerid = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
        Log.e(">>>>","添加新好友："+ownerid);
        friend.setOwnerId(ownerid);
        friend.setUserId(chatMessage.getFromUserId());
        friend.setNickName(chatMessage.getFromUserName());
        friend.setRemarkName(chatMessage.getFromUserName());
        friend.setTimeCreate(TimeUtils.time_current_time());
        friend.setContent(chatMessage.getContent());
        friend.setCompanyId(0);// 公司
        friend.setTimeSend(chatMessage.getTimeSend());
        friend.setRoomFlag(0);// 0朋友 1群组
        friend.setStatus(Constants.STATUS_UNKNOW);
        friend.setVersion(TableVersionSp.getInstance(MyApplication.getInstance()).getFriendTableVersion(CoreManager.requireSelf(MyApplication.getInstance()).getUserId()));// 更新版本
        try {
            friendDao.create(friend);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Friend> getDevice(String ownerId) {
        List<Friend> query = new ArrayList<>();
        try {
            PreparedQuery<Friend> preparedQuery = friendDao.queryBuilder().where()
                    .eq("ownerId", ownerId)
                    .and().eq("isDevice", 1)
                    .prepare();

            query = friendDao.query(preparedQuery);
            return query;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return query;
    }

    public List<Friend> getAllFriends(String ownerId) {
        List<Friend> query = new ArrayList<>();
        try {
            PreparedQuery<Friend> preparedQuery = friendDao.queryBuilder().where()
                    .eq("ownerId", ownerId)
                    // .and().in("status", new Object[]{Constants.STATUS_FRIEND, Constants.STATUS_SYSTEM})
                    .and().in("status", Constants.STATUS_FRIEND)// 仅限我的好友
                    .and().eq("isDevice", 0)// 移除我的设备
                    .and().eq("roomFlag", 0)// 移除房间
                    .and().eq("companyId", 0)
                    .prepare();

            query = friendDao.query(preparedQuery);
            return query;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return query;
    }

    /**
     * 获取好友用于建群，
     * <p>
     * 支持群聊的除了好友还有公众号，
     * 但是要排除系统号10000,
     * 还要新的朋友系统号10001,
     * 还要支付系统号1100,
     */
    public List<Friend> getFriendsGroupChat(String ownerId) throws SQLException {
        PreparedQuery<Friend> preparedQuery = friendDao.queryBuilder().where()
                .eq("ownerId", ownerId)
                .and().in("status", new Object[]{Constants.STATUS_FRIEND, Constants.STATUS_SYSTEM})
                .and().not().eq("userId", Constants.ID_SYSTEM_MESSAGE)
                .and().not().eq("userId", Constants.ID_NEW_FRIEND_MESSAGE)
                .and().not().eq("userId", Constants.ID_SK_PAY)
                .and().eq("isDevice", 0)// 移除我的设备
                .and().eq("roomFlag", 0)// 移除房间
                .and().eq("companyId", 0)
                .prepare();

        return friendDao.query(preparedQuery);
    }

    /**
     * 查询好友的数量，
     * 仅限好友，
     */
    public long getFriendsCount(String ownerId) throws SQLException {
        return friendDao.queryBuilder().where()
                .eq("ownerId", ownerId)
                // 仅限好友，
                .and().eq("status", Constants.STATUS_FRIEND)
                // 排除群组，
                .and().eq("roomFlag", 0)
                .countOf();
    }

    /**
     * 查询好友的数量，
     * 仅限好友，
     */
    public long getGroupsCount(String ownerId) throws SQLException {
        return friendDao.queryBuilder().where()
                .eq("ownerId", ownerId)
                // 仅限好友，
                .and().eq("status", Constants.STATUS_FRIEND)
                // 仅限群组，
                .and().ne("roomFlag", 0)
                .countOf();
    }

    public List<Friend> getAllSystems(String ownerId) {
        List<Friend> query = new ArrayList<>();
        try {
            PreparedQuery<Friend> preparedQuery = friendDao.queryBuilder().where()
                    .eq("ownerId", ownerId)
                    // .and().in("status", new Object[]{Constants.STATUS_FRIEND, Constants.STATUS_SYSTEM})
                    .and().in("status", Constants.STATUS_SYSTEM)// 仅限公众号
                    .and().eq("isDevice", 0)// 移除我的设备
                    .and().eq("roomFlag", 0)// 移除房间
                    .and().eq("companyId", 0)
                    .and().ne("userId", Constants.ID_NEW_FRIEND_MESSAGE)
                    .prepare();

            query = friendDao.query(preparedQuery);
            return query;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return query;
    }
    public List<Friend> getKefuSystems(String ownerId) {
        List<Friend> query = new ArrayList<>();
        try {
            PreparedQuery<Friend> preparedQuery = friendDao.queryBuilder().where()
                    .eq("ownerId", ownerId)
                    // .and().in("status", new Object[]{Constants.STATUS_FRIEND, Constants.STATUS_SYSTEM})
                    .and().in("status", Constants.ID_SYSTEM_MESSAGE)// 仅限公众号
                    .and().eq("isDevice", 0)// 移除我的设备
                    .and().eq("roomFlag", 0)// 移除房间
                    .and().eq("companyId", 0)
                    .and().ne("userId", Constants.ID_NEW_FRIEND_MESSAGE)
                    .prepare();

            query = friendDao.query(preparedQuery);
            return query;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return query;
    }
    public List<Friend> getAllRooms(String ownerId) {
        List<Friend> query = new ArrayList<>();
        try {
            PreparedQuery<Friend> preparedQuery = friendDao.queryBuilder().where()
                    .eq("ownerId", ownerId).and()
                    .eq("groupStatus", 0).and()
                    .in("roomFlag", 1, 510)
                    .prepare();
            query = friendDao.query(preparedQuery);
            return query;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return query;
    }

    public List<Friend> getAllBlacklists(String ownerId) {
        List<Friend> query = new ArrayList<>();
        try {
            PreparedQuery<Friend> preparedQuery = friendDao.queryBuilder().where().eq("ownerId", ownerId).and()
                    .eq("status", Constants.STATUS_BLACKLIST).and()
                    .eq("roomFlag", 0)
                    .prepare();
            query = friendDao.query(preparedQuery);
            return query;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return query;
    }

    /**
     * 获取单个好友 陌生人 || 好友 || 公众号 || 群组
     */
    public Friend getFriend(String ownerId, String friendId) {
        try {
            PreparedQuery<Friend> preparedQuery = friendDao.queryBuilder().where()
                    .eq("ownerId", ownerId).and().eq("userId", friendId)
                    .prepare();
            return friendDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取单个好友 仅限好友
     */
    public Friend getFriendAndFriendStatus(String ownerId, String friendId) {
        try {
            PreparedQuery<Friend> preparedQuery = friendDao.queryBuilder().where()
                    .eq("ownerId", ownerId).and().eq("userId", friendId).and()
                    .eq("status", Constants.STATUS_FRIEND)
                    .prepare();
            Friend existFriend = friendDao.queryForFirst(preparedQuery);
            return existFriend;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取单个好友包括公众号
     */
    public Friend getMyFriendAndSystemFriends(String ownerId, String friendId) {
        try {
            PreparedQuery<Friend> preparedQuery = friendDao.queryBuilder().where()
                    .eq("ownerId", ownerId).and().eq("userId", friendId)
                    .prepare();
            Friend existFriend = friendDao.queryForFirst(preparedQuery);
            if (existFriend != null && (existFriend.getStatus() == Constants.STATUS_FRIEND || existFriend.getStatus() == Constants.STATUS_SYSTEM)) {
                return existFriend;
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取单个好友 仅限公众号
     */
    public Friend getFriendAndSystemStatus(String ownerId, String friendId) {
        try {
            PreparedQuery<Friend> preparedQuery = friendDao.queryBuilder().where()
                    .eq("ownerId", ownerId).and().eq("userId", friendId).and()
                    .eq("status", Constants.STATUS_SYSTEM)
                    .prepare();
            Friend existFriend = friendDao.queryForFirst(preparedQuery);
            return existFriend;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过roomId获取群组Friend
     */
    public Friend getMucFriendByRoomId(String ownerId, String roomId) {
        try {
            PreparedQuery<Friend> preparedQuery = friendDao.queryBuilder().where().eq("ownerId", ownerId).and().eq("roomId", roomId).prepare();
            return friendDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 更新好友的状态
    public void updateFriendStatus(String loginUserId, String userId, int status) {
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            builder.updateColumnValue("status", status);
            builder.where().eq("ownerId", loginUserId).and().eq("userId", userId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 更新昵称
    public void updateNickName(String loginUserId, String userId, String nickName) {
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            builder.updateColumnValue("nickName", nickName);
            builder.where().eq("ownerId", loginUserId).and().eq("userId", userId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 更新备注名
    public void updateRemarkName(String loginUserId, String userId, String remarkName) {
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            builder.updateColumnValue("remarkName", remarkName);
            builder.where().eq("ownerId", loginUserId).and().eq("userId", userId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 更新描述
    public void updateDescribe(String loginUserId, String userId, String remarkName) {
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            builder.updateColumnValue("describe", remarkName);
            builder.where().eq("ownerId", loginUserId).and().eq("userId", userId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 更新备注名与描述
    public void updateRemarkNameAndDescribe(String loginUserId, String userId, String remarkName,
                                            String describe) {
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            builder.updateColumnValue("remarkName", remarkName);
            builder.updateColumnValue("describe", describe);
            builder.where().eq("ownerId", loginUserId).and().eq("userId", userId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 更新群组昵称
    public void updateMucFriendRoomName(String roomId, String roomName) {
        try {
            UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
            builder.updateColumnValue("nickName", roomName).where().eq("userId", roomId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 更新群内昵称
    public void updateRoomMyNickName(String roomId, String roomMyNickName) {
        try {
            UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
            builder.updateColumnValue("roomMyNickName", roomMyNickName).where().eq("userId", roomId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 更新群组状态
    public void updateFriendGroupStatus(String loginUserId, String userId, int groupStatus) {
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            builder.updateColumnValue("groupStatus", groupStatus);
            builder.where().eq("ownerId", loginUserId).and().eq("userId", userId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 更新群创建者id
    public void updateRoomCreateUserId(String ownerId, String friendId, String roomCreateUserId) {
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            builder.where().eq("ownerId", ownerId).and().eq("userId", friendId);
            builder.updateColumnValue("roomCreateUserId", roomCreateUserId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 更新群禁言时间
    public void updateRoomTalkTime(String ownerId, String friendId, long roomTalkTime) {
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            builder.where().eq("ownerId", ownerId).and().eq("userId", friendId);
            builder.updateColumnValue("roomTalkTime", roomTalkTime);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 更新我在这个群的禁言时间
    public void updateRoomMyTalkTime(String ownerId, String friendId, long roomMyTalkTime) {
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            builder.where().eq("ownerId", ownerId).and().eq("userId", friendId);
            builder.updateColumnValue("roomMyTalkTime", roomMyTalkTime);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 针对某个好友的部分设置统一更新
    public void updateFriendPartStatus(String friendId, User user) {
        FriendDao.getInstance().updateOfflineNoPushMsgStatus(friendId,
                user.getFriends().getOfflineNoPushMsg());
        FriendDao.getInstance().updateChatRecordTimeOut(friendId,
                user.getFriends().getChatRecordTimeOut());
    }

    // 更新为置顶
    public void updateTopFriend(String friendId, long time) {
        if (time == 0) {
            time = TimeUtils.time_current_time();
        }
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            String ownerId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
            builder.updateColumnValue("topTime", time);
            builder.where().eq("ownerId", ownerId).and().eq("userId", friendId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 取消置顶
    public void resetTopFriend(String friendId) {
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            String ownerId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
            builder.updateColumnValue("topTime", 0);
            builder.where().eq("ownerId", ownerId).and().eq("userId", friendId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 更新消息免打扰状态
    public void updateOfflineNoPushMsgStatus(String friendId, int offlineNoPushMsg) {
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            String ownerId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
            builder.updateColumnValue("offlineNoPushMsg", offlineNoPushMsg);
            builder.where().eq("ownerId", ownerId).and().eq("userId", friendId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 更新消息保存天数
    public void updateChatRecordTimeOut(String friendId, double chatRecordTimeOut) {
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            String ownerId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
            builder.updateColumnValue("chatRecordTimeOut", chatRecordTimeOut);
            builder.where().eq("ownerId", ownerId).and().eq("userId", friendId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新@我的状态
     */
    public void updateAtMeStatus(String friendId, int status) {
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            String ownerId = CoreManager.requireSelf(MyApplication.getContext()).getUserId();
            builder.updateColumnValue("isAtMe", status);
            builder.where().eq("ownerId", ownerId).and().eq("userId", friendId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 单聊 synchronizeChatHistory 调用成功后，将downloadTime 与 timeSend保持一致
     *
     * @param loginUserId
     * @param userId
     * @param time
     */
    public void updateDownloadTime(String loginUserId, String userId, long time) {
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            builder.updateColumnValue("downloadTime", time);
            builder.where().eq("ownerId", loginUserId).and().eq("userId", userId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新content字段
     */
    public void updateFriendContent(String loginUserId, String userId, String text, int type, long time) {
        if (type == Constants.TYPE_IMAGE) {
            text = "[" + InternationalizationHelper.getString("JX_Image") + "]";
        } else if (type == Constants.TYPE_CARD) {
            text = "[" + InternationalizationHelper.getString("JX_Card") + "]";
        } else if (type == Constants.TYPE_VOICE) {
            text = "[" + InternationalizationHelper.getString("JX_Voice") + "]";
        } else if (type == Constants.TYPE_LOCATION) {
            text = "[" + InternationalizationHelper.getString("JX_Location") + "]";
        } else if (type == Constants.TYPE_GIF) {
            text = "[" + InternationalizationHelper.getString("emojiVC_Anma") + "]";
        } else if (type == Constants.TYPE_VIDEO) {
            text = "[" + InternationalizationHelper.getString("JX_Video") + "]";
        } else if (type == Constants.TYPE_FILE) {
            text = "[" + InternationalizationHelper.getString("JX_File") + "]";
        } else if (type == Constants.TYPE_RED) {
            text = "[" + InternationalizationHelper.getString("JX_RED") + "]";
        } else if (type == Constants.TYPE_READ_EXCLUSIVE) {
            text = MyApplication.getInstance().getString(R.string.msg_red_packet_exclusive);
        } else if (type == Constants.TYPE_CLOUD_RED) {
            text = MyApplication.getInstance().getString(R.string.msg_red_cloud_packet);
        } else if (type == Constants.TYPE_LINK || type == Constants.TYPE_SHARE_LINK) {
            text = "[" + InternationalizationHelper.getString("JXLink") + "]";
        } else if (type == Constants.TYPE_IMAGE_TEXT || type == Constants.TYPE_IMAGE_TEXT_MANY) {
            text = "[" + InternationalizationHelper.getString("JXGraphic") + InternationalizationHelper.getString("JXMainViewController_Message") + "]";
        } else if (type == Constants.TYPE_SHAKE) {
            text = MyApplication.getInstance().getString(R.string.msg_shake);
        } else if (type == Constants.TYPE_CHAT_HISTORY) {
            text = MyApplication.getInstance().getString(R.string.msg_chat_history);
        }
        // 通话结束与通话取消的聊天记录在消息界面不做特殊处理
        else if (type == Constants.TYPE_END_CONNECT_VOICE || type == Constants.TYPE_END_CONNECT_VIDEO) {
            text = !TextUtils.isEmpty(text) ? text : MyApplication.getInstance().getString(R.string.msg_call_end);
        } else if (type == Constants.TYPE_NO_CONNECT_VIDEO || type == Constants.TYPE_NO_CONNECT_VOICE) {
            text = !TextUtils.isEmpty(text) ? text : MyApplication.getInstance().getString(R.string.msg_call_cancel);
        } else if (type == Constants.TYPE_OK_MU_CONNECT_VOICE || type == Constants.TYPE_EXIT_VOICE) {
            text = MyApplication.getInstance().getString(R.string.msg_voice_meeting);
        } else if (type == Constants.TYPE_VIDEO_IN || type == Constants.TYPE_VIDEO_OUT) {
            text = MyApplication.getInstance().getString(R.string.msg_video_meeting);
        } else if (type == Constants.TYPE_TRANSFER) {
            text = MyApplication.getContext().getString(R.string.tip_transfer_money);
        } else if (type == Constants.TYPE_CLOUD_TRANSFER) {
            text = MyApplication.getContext().getString(R.string.micro_tip_transfer_money);
        } else if (type == Constants.TYPE_TRANSFER_RECEIVE) {
            text = MyApplication.getContext().getString(R.string.tip_transfer_money) + MyApplication.getContext().getString(R.string.transfer_friend_sure_save);
        } else if (type == Constants.TYPE_CLOUD_TRANSFER_RECEIVE) {
            text = MyApplication.getContext().getString(R.string.micro_tip_transfer_money) + MyApplication.getContext().getString(R.string.transfer_friend_sure_save);
        } else if (type == Constants.TYPE_TRANSFER_BACK) {
            text = MyApplication.getContext().getString(R.string.transfer_back);
        } else if (type == Constants.TYPE_CLOUD_TRANSFER_RETURN) {
            text = MyApplication.getContext().getString(R.string.micro_transfer_back);
        } else if (type == Constants.TYPE_PAYMENT_OUT || type == Constants.TYPE_RECEIPT_OUT) {
            text = MyApplication.getContext().getString(R.string.payment_get_notify);
        } else if (type == Constants.TYPE_PAYMENT_GET || type == Constants.TYPE_RECEIPT_GET) {
            text = MyApplication.getContext().getString(R.string.receipt_get_notify);
        }
        Friend friend = FriendDao.getInstance().getFriend(loginUserId, userId);
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            builder.updateColumnValue("type", type);
            builder.updateColumnValue("content", text);
            builder.updateColumnValue("timeSend", time);
            if (friend != null && friend.getTimeSend() == friend.getDownloadTime()) {
                builder.updateColumnValue("downloadTime", time);
            }
            builder.where().eq("ownerId", loginUserId).and().eq("userId", userId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Todo 以下两个updateLastChatMessage 貌似都和NewFriend 有关 不知为啥要这么写

    public String updateApartDownloadTime(String loginUserId, String userId, String text, int type, long time,
                                          int isRoom, String from, String fromUserName, String toUserName) {
        if (type == Constants.TYPE_IMAGE) {
            text = "[" + InternationalizationHelper.getString("JX_Image") + "]";
        } else if (type == Constants.TYPE_CARD) {
            text = "[" + InternationalizationHelper.getString("JX_Card") + "]";
        } else if (type == Constants.TYPE_VOICE) {
            text = "[" + InternationalizationHelper.getString("JX_Voice") + "]";
        } else if (type == Constants.TYPE_LOCATION) {
            text = "[" + InternationalizationHelper.getString("JX_Location") + "]";
        } else if (type == Constants.TYPE_GIF) {
            text = "[" + InternationalizationHelper.getString("emojiVC_Anma") + "]";
        } else if (type == Constants.TYPE_VIDEO) {
            text = "[" + InternationalizationHelper.getString("JX_Video") + "]";
        } else if (type == Constants.TYPE_FILE) {
            text = "[" + InternationalizationHelper.getString("JX_File") + "]";
        } else if (type == Constants.TYPE_RED) {
            text = "[" + InternationalizationHelper.getString("JX_RED") + "]";
        } else if (type == Constants.TYPE_READ_EXCLUSIVE) {
            text = MyApplication.getInstance().getString(R.string.msg_red_packet_exclusive);
        } else if (type == Constants.TYPE_CLOUD_RED) {
            text = MyApplication.getInstance().getString(R.string.msg_red_cloud_packet);
        } else if (type == Constants.TYPE_LINK || type == Constants.TYPE_SHARE_LINK) {
            text = "[" + InternationalizationHelper.getString("JXLink") + "]";
        } else if (type == Constants.TYPE_IMAGE_TEXT || type == Constants.TYPE_IMAGE_TEXT_MANY) {
            text = "[" + InternationalizationHelper.getString("JXGraphic") + InternationalizationHelper.getString("JXMainViewController_Message") + "]";
        } else if (type == Constants.TYPE_SHAKE) {
            text = MyApplication.getInstance().getString(R.string.msg_shake);
        } else if (type == Constants.TYPE_CHAT_HISTORY) {
            text = MyApplication.getInstance().getString(R.string.msg_chat_history);
        }
        // 通话结束与通话取消的聊天记录在消息界面不做特殊处理
        else if (type == Constants.TYPE_END_CONNECT_VOICE || type == Constants.TYPE_END_CONNECT_VIDEO) {
            text = !TextUtils.isEmpty(text) ? text : MyApplication.getInstance().getString(R.string.msg_call_end);
        } else if (type == Constants.TYPE_NO_CONNECT_VIDEO || type == Constants.TYPE_NO_CONNECT_VOICE) {
            text = !TextUtils.isEmpty(text) ? text : MyApplication.getInstance().getString(R.string.msg_call_cancel);
        } else if (type == Constants.TYPE_OK_MU_CONNECT_VOICE || type == Constants.TYPE_EXIT_VOICE) {
            text = MyApplication.getInstance().getString(R.string.msg_voice_meeting);
        } else if (type == Constants.TYPE_VIDEO_IN || type == Constants.TYPE_VIDEO_OUT) {
            text = MyApplication.getInstance().getString(R.string.msg_video_meeting);
        } else if (type == Constants.TYPE_TRANSFER) {
            text = MyApplication.getContext().getString(R.string.tip_transfer_money);
        } else if (type == Constants.TYPE_CLOUD_TRANSFER) {
            text = MyApplication.getContext().getString(R.string.micro_tip_transfer_money);
        } else if (type == Constants.TYPE_TRANSFER_RECEIVE) {
            text = MyApplication.getContext().getString(R.string.tip_transfer_money) + MyApplication.getContext().getString(R.string.transfer_friend_sure_save);
        } else if (type == Constants.TYPE_CLOUD_TRANSFER_RECEIVE) {
            text = MyApplication.getContext().getString(R.string.micro_tip_transfer_money) + MyApplication.getContext().getString(R.string.transfer_friend_sure_save);
        } else if (type == Constants.TYPE_TRANSFER_BACK) {
            text = MyApplication.getContext().getString(R.string.transfer_back);
        } else if (type == Constants.TYPE_CLOUD_TRANSFER_RETURN) {
            text = MyApplication.getContext().getString(R.string.micro_transfer_back);
        } else if (type == Constants.TYPE_PAYMENT_OUT || type == Constants.TYPE_RECEIPT_OUT) {
            text = MyApplication.getContext().getString(R.string.payment_get_notify);
        } else if (type == Constants.TYPE_PAYMENT_GET || type == Constants.TYPE_RECEIPT_GET) {
            text = MyApplication.getContext().getString(R.string.receipt_get_notify);
        } else if (type == Constants.TYPE_BACK || type == Constants.TYPE_83 || type == Constants.TYPE_CLOUD_RECEIVE_RED
                || type == Constants.TYPE_RED_BACK || type == Constants.TYPE_TRANSFER_RECEIVE || type == Constants.TYPE_CLOUD_TRANSFER_RECEIVE
                || type == Constants.TYPE_CLOUD_BACK_RED) {
            text = ChatMessageDao.getInstance().handlerGetLastSpecialMessage(isRoom, type, loginUserId, from, fromUserName, toUserName);
        } else if (type == Constants.TYPE_SEND_MANAGER) {
            if (text.equals("1")) {
                text = (fromUserName + " " + InternationalizationHelper.getString("JXSettingVC_Set") + toUserName + " " + InternationalizationHelper.getString("JXMessage_admin"));
            } else if (text.equals("0")) {
                text = (fromUserName + " " + InternationalizationHelper.getString("JXSip_Canceled") + toUserName + " " + InternationalizationHelper.getString("JXMessage_admin"));
            } // 以防万一，1和0以外情况认为已经处理过了，
        } else if (type == Constants.TYPE_GROUP_ALL_SHAT_UP) {
            if (!text.equals("0")) {
                text = MyApplication.getInstance().getString(R.string.tip_now_ban_all);
            } else {
                text = MyApplication.getInstance().getString(R.string.tip_now_disable_ban_all);
            }
        }
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        if (getFriendContentInfo(loginUserId, userId, time, type) != null) {//本地删除最新消息聊天记录后，下次重新登陆，不再显示
            return null;
        }
        try {
            Friend friend = getFriend(MyApplication.getLoginUserId(), userId);
            if (friend == null) {
                return userId;
            } else if (friend.getStatus() == Constants.STATUS_23) {
                builder.updateColumnValue("type", type);
                builder.updateColumnValue("content", text);
                builder.updateColumnValue("timeSend", time);
                builder.updateColumnValue("status", Constants.STATUS_FRIEND);
                builder.where().eq("ownerId", MyApplication.getLoginUserId()).and().eq("userId", userId);
                friendDao.update(builder.prepare());
                return userId;
            } else {
                builder.updateColumnValue("type", type);
                builder.updateColumnValue("content", text);
                builder.updateColumnValue("timeSend", time);
                builder.where().eq("ownerId", MyApplication.getLoginUserId()).and().eq("userId", userId);
                friendDao.update(builder.prepare());
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 特定的 最新的聊天记录
     */
    public Friend getFriendContentInfo(String ownerId, String friendId, long time, int type) {
        try {
            PreparedQuery<Friend> preparedQuery = friendDao.queryBuilder().where()
                    .eq("ownerId", ownerId)
                    .and().eq("userId", friendId)
                    .and().eq("timeSend", time)
                    .and().eq("type", type)
                    .prepare();
            Friend existFriend = friendDao.queryForFirst(preparedQuery);
            return existFriend;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 更新朋友表里面的最后一条未读信息
     */
    public void updateLastChatMessage(String ownerId, String friendId, ChatMessage message) {
        Context context = MyApplication.getInstance();
        String content = "";
        int type = message.getType();
        if (type == Constants.TYPE_TEXT) {
            content = message.getContent();
        } else if (type == Constants.TYPE_IMAGE) {
            content = "[" + InternationalizationHelper.getString("JX_Image") + "]";
        } else if (type == Constants.TYPE_CARD) {
            content = "[" + InternationalizationHelper.getString("JX_Card") + "]";
        } else if (type == Constants.TYPE_VOICE) {
            content = "[" + InternationalizationHelper.getString("JX_Voice") + "]";
        } else if (type == Constants.TYPE_LOCATION) {
            content = "[" + InternationalizationHelper.getString("JX_Location") + "]";
        } else if (type == Constants.TYPE_GIF) {
            content = "[" + InternationalizationHelper.getString("emojiVC_Anma") + "]";
        } else if (type == Constants.TYPE_VIDEO) {
            content = "[" + InternationalizationHelper.getString("JX_Video") + "]";
        } else if (type == Constants.TYPE_FILE) {
            content = "[" + InternationalizationHelper.getString("JX_File") + "]";
        } else if (type == Constants.TYPE_RED) {
            content = "[" + InternationalizationHelper.getString("JX_RED") + "]";
        } else if (type == Constants.TYPE_READ_EXCLUSIVE) {
            content = MyApplication.getInstance().getString(R.string.msg_red_packet_exclusive);
        } else if (type == Constants.TYPE_CLOUD_RED) {
            content = MyApplication.getInstance().getString(R.string.msg_red_cloud_packet);
        } else if (type == Constants.TYPE_TIP) {
            //content = message.getContent();
        } else if (type == Constants.TYPE_NEWSEE) {// 新关注提示
            if (!message.isMySend()) {
                content = InternationalizationHelper.getString("JXFriendObject_FollowYour");
            }
        } else if (type == Constants.TYPE_SAYHELLO) {// 打招呼提示
            if (!message.isMySend()) {
                if (TextUtils.isEmpty(message.getContent())) {
                    content = context.getString(R.string.msg_be_say_hello);
                } else {
                    content = message.getContent();
                }
            }
        } else if (type == Constants.TYPE_PASS) {// 验证通过提示
            if (!message.isMySend()) {
                content = InternationalizationHelper.getString("JXFriendObject_PassGo");
                NewFriendDao.getInstance().changeNewFriendState(message.getFromUserId(), Constants.STATUS_13);
            }
        } else if (type == Constants.TYPE_FRIEND) { // 新朋友提示
            if (!message.isMySend()) {
                content = message.getFromUserName() + context.getString(R.string.add_me_as_friend);
            }
        } else if (type == Constants.TYPE_FEEDBACK) {// 回话
            if (!message.isMySend()) {
                if (!TextUtils.isEmpty(message.getContent())) {
                    content = message.getContent();
                }
            }
        } else if (type == Constants.TYPE_BLACK) {
            if (!message.isMySend()) {
                content = context.getString(R.string.be_pull_black_place_holder, message.getFromUserId());
            } else {
                content = context.getString(R.string.pull_black_place_holder, message.getFromUserId());
            }
        } else if (type == Constants.TYPE_DELALL || type == Constants.TYPE_BACK_DELETE) {
           /* if (!message.isMySend()) {
                content = context.getString(R.string.be_delete_place_holder, message.getFromUserId());
            } else {
                content = context.getString(R.string.delete_place_holder, message.getFromUserId());
            }*/
        } else if (type == Constants.TYPE_RECOMMEND) {
            content = context.getString(R.string.msg_has_new_recommend_friend);
        } else if (type == Constants.TYPE_LINK || type == Constants.TYPE_SHARE_LINK) {
            content = "[" + InternationalizationHelper.getString("JXLink") + "]";
        } else if (type == Constants.TYPE_IMAGE_TEXT || type == Constants.TYPE_IMAGE_TEXT_MANY) {
            content = "[" + InternationalizationHelper.getString("JXGraphic") + InternationalizationHelper.getString("JXMainViewController_Message") + "]";
        } else if (type == Constants.TYPE_CHAT_HISTORY) {
            content = context.getString(R.string.msg_chat_history);
        } else {
            content = message.getContent();
        }

        if (TextUtils.isEmpty(content)) {
            content = "";
        }
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            builder.updateColumnValue("type", type);
            builder.updateColumnValue("content", content);
            builder.updateColumnValue("timeSend", message.getTimeSend());
            builder.where().eq("ownerId", ownerId).and().eq("userId", friendId);
            friendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLastChatMessage(String ownerId, String friendId, String content) {
        QueryBuilder<Friend, Integer> queryBuilder = friendDao.queryBuilder();
        UpdateBuilder<Friend, Integer> builder = friendDao.updateBuilder();
        try {
            queryBuilder.where().eq("ownerId", ownerId).and().eq("userId", friendId);
            Friend friend = queryBuilder.queryForFirst();
            if (friend != null) {
                builder.updateColumnValue("type", 1);
                builder.updateColumnValue("content", content);
                builder.updateColumnValue("timeSend", TimeUtils.time_current_time());
                builder.where().eq("ownerId", ownerId).and().eq("userId", friendId);
                friendDao.update(builder.prepare());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addAttentionUsers(final String loginUserId, final List<AttentionUser> attentionUsers,
                                  final OnCompleteListener2 listener) throws SQLException {
        ThreadManager.getPool().execute(() -> {
            checkSystemFriend(loginUserId);
            int tableVersion = TableVersionSp.getInstance(MyApplication.getInstance()).getFriendTableVersion(loginUserId);
            int newVersion = tableVersion + 1;
            if (attentionUsers != null && attentionUsers.size() > 0) {
                List<Friend> friends = updateAttentionUser(attentionUsers, loginUserId, newVersion);
                try {
                    new TransactionManager(friendDao.getConnectionSource()).callInTransaction(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            for (int j = 0; j < friends.size(); j++) {
                                friendDao.createOrUpdate(friends.get(j));
                                if (listener != null) {
                                    listener.onLoading(j + 1, friends.size());
                                }
                            }
                            // 本地Sp中保存的版本号更新（+1）
                            TableVersionSp.getInstance(MyApplication.getInstance()).setFriendTableVersion(loginUserId, newVersion);
                            // 更新完成，把过期的好友数据删除
                            try {
                                DeleteBuilder<Friend, Integer> builder = friendDao.deleteBuilder();
                                builder.where()
                                        .eq("ownerId", loginUserId).and().eq("roomFlag", 0).and()
                                        .in("status", new Object[]{Constants.STATUS_FRIEND, Constants.STATUS_ATTENTION}).and()
                                        .ne("version", newVersion);
                                friendDao.delete(builder.prepare());
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            // 朋友数据更新了，在去删除不存在的消息表
                            List<String> tables = SQLiteRawUtil.getUserChatMessageTables(mHelper.getReadableDatabase(), loginUserId);
                            PreparedQuery<Friend> where = friendDao.queryBuilder().selectColumns("userId").where()
                                    .eq("ownerId", loginUserId)
                                    .prepare();
                            List<Friend> query = friendDao.query(where);
                            Set<String> par = new HashSet<>();
                            for (Friend friend : query) {
                                par.add(friend.getUserId());
                            }
                            if (tables != null && tables.size() > 0) {
                                for (int i = 0; i < tables.size(); i++) {
                                    String tableName = tables.get(i);
                                    String tablePrefix = SQLiteRawUtil.CHAT_MESSAGE_TABLE_PREFIX + loginUserId;
                                    int index = tableName.indexOf(tablePrefix);
                                    if (index == -1) {
                                        continue;
                                    }
                                    String toUserId = tableName.substring(index + tablePrefix.length(), tableName.length());
                                    if (toUserId.equals(Constants.ID_BLOG_MESSAGE) || toUserId.equals(Constants.ID_INTERVIEW_MESSAGE)
                                            || toUserId.equals(Constants.ID_NEW_FRIEND_MESSAGE) || toUserId.equals(Constants.ID_SYSTEM_MESSAGE)) {
                                        continue;
                                    }
                                    if (!par.contains(toUserId)) {// 删除这张消息表
                                        if (SQLiteRawUtil.isTableExist(mHelper.getWritableDatabase(), tableName)) {
                                            SQLiteRawUtil.dropTable(mHelper.getReadableDatabase(), tableName);
                                        }

                                    }
                                }
                            }
                            if (listener != null) {
                                listener.onCompleted();
                            }
//                // 这里没用到返回值，但是这个方法必须返回，
                            return null;
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } else {
                if (listener != null) {
                    listener.onCompleted();
                }
            }
        });
//        new TransactionManager(friendDao.getConnectionSource()).callInTransaction(() -> {
//
//        });
    }

    int threadCount = 5;//线程数  最多 threadCount+1 个线程
    int threshold = 500;//阈值

    private List<Friend> updateAttentionUser(List<AttentionUser> attentionUsers, String loginUserId, int newVersion) {
        List<Friend> friends = new ArrayList<>();
        if (attentionUsers != null && attentionUsers.size() > threshold) {
            ArrayList<AttentionUser>[] groupCount;
            int temp = attentionUsers.size() % threadCount;
            if (temp == 0) {
                groupCount = new ArrayList[threadCount];
                int singleGroup = attentionUsers.size() / threadCount;
                for (int i = 0; i < groupCount.length; i++) {
                    if (groupCount[i] == null) {
                        groupCount[i] = new ArrayList<>();
                    }
                    groupCount[i].addAll(attentionUsers.subList(i * singleGroup, (i + 1) * singleGroup));
                }
            } else {
                groupCount = new ArrayList[threadCount + 1];
                int singleGroup = attentionUsers.size() / threadCount;
                int residue = attentionUsers.size() % threadCount;
                for (int i = 0; i < groupCount.length; i++) {
                    if (groupCount[i] == null) {
                        groupCount[i] = new ArrayList<>();
                    }
                    if (i != groupCount.length - 1) {
                        groupCount[i].addAll(attentionUsers.subList(i * singleGroup, (i + 1) * singleGroup));
                    } else {
                        groupCount[i].addAll(attentionUsers.subList(i * singleGroup, i * singleGroup + residue));
                    }
                }
            }
            List<Future<ArrayList<Friend>>> futures = new ArrayList<>();
            ExecutorService executorService = Executors.newFixedThreadPool(groupCount.length);
            for (int i = 0; i < groupCount.length; i++) {
                DataProcessingRunnable dataProcessingRunnable = new DataProcessingRunnable(groupCount[i], loginUserId, newVersion);
                Future<ArrayList<Friend>> submit = executorService.submit(dataProcessingRunnable);
                futures.add(submit);
            }
            for (int i = 0; i < futures.size(); i++) {
                Future<ArrayList<Friend>> arrayListFuture = futures.get(i);
                try {
                    friends.addAll(arrayListFuture.get());

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return friends;
        } else {
            DataProcessingRunnable dataProcessingRunnable = new DataProcessingRunnable(attentionUsers, loginUserId, newVersion);
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            Future<ArrayList<Friend>> submit = executorService.submit(dataProcessingRunnable);
            try {
                return submit.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        }
    }


    private class DataProcessingRunnable implements Callable<ArrayList<Friend>> {
        private List<AttentionUser> mAttentionUsers;
        private String loginUserId;
        private int newVersion;
        private ArrayList<Friend> result = new ArrayList<>();

        public DataProcessingRunnable(List<AttentionUser> attentionUsers, String loginUserId, int newVersion) {
            this.mAttentionUsers = attentionUsers;
            this.loginUserId = loginUserId;
            this.newVersion = newVersion;
        }

        public List<Friend> getResult() {
            return result;
        }

        @Override
        public ArrayList<Friend> call() throws Exception {
            QueryBuilder<Friend, Integer> builder = friendDao.queryBuilder();
            List<String> strings = new ArrayList<>();
            for (int i = 0; i < mAttentionUsers.size(); i++) {
                strings.add(mAttentionUsers.get(i).getToUserId());
            }
            PreparedQuery<Friend> prepare = builder.where().eq("ownerId", loginUserId).and().in("userId", strings).prepare();
            List<Friend> query = friendDao.query(prepare);
            Map<String, Friend> map = new HashMap<>();
            for (int i = 0; i < query.size(); i++) {
                map.put(query.get(i).getUserId(), query.get(i));
            }
            for (int i = 0; i < mAttentionUsers.size(); i++) {
                AttentionUser attentionUser = mAttentionUsers.get(i);
                if (attentionUser == null) {
                    continue;
                }
                String userId = attentionUser.getToUserId();// 好友的Id
//                QueryBuilder<Friend, Integer> builder = friendDao.queryBuilder();
//                Friend friend = null;
//                try {
//                    builder.where().eq("ownerId", loginUserId).and().eq("userId", userId);
//                    long startTime = System.currentTimeMillis();
//                    List<Friend> friends = queryFriend(builder);
//                    if (friends != null && friends.size() > 0) {
//                        friend = friends.get(0);
//                    }
//                    Log.i("FriendDao", "查询耗时:" + (System.currentTimeMillis() - startTime));
//                } catch (SQLException e1) {
//                    e1.printStackTrace();
//                }
                Friend friend = null;
                if (map.containsKey(userId)) {
                    friend = map.get(userId);
                }
                if (friend == null) {
                    friend = new Friend();
                }
                friend.setOwnerId(attentionUser.getUserId());
                friend.setUserId(attentionUser.getToUserId());
                if (!userId.equals(Constants.ID_SYSTEM_MESSAGE)) {
                    friend.setNickName(attentionUser.getToNickName());
                    friend.setRemarkName(attentionUser.getRemarkName());
                    friend.setTimeCreate(attentionUser.getCreateTime());
                    // 公众号的status为8，服务端返回的为2，不修改
                    int status = (attentionUser.getBlacklist() == 0) ? attentionUser.getStatus() : -1;
                    friend.setStatus(status);
                }
                if (attentionUser.getToUserType() == 2) {// 公众号
                    friend.setStatus(Constants.STATUS_SYSTEM);
                }
                if (!TextUtils.isEmpty(attentionUser.getDescribe())) {
                    friend.setDescribe(attentionUser.getDescribe());
                }
                // Todo 注意 IsBeenBlack==1表示 为对方拉黑了我，不能将其状态置为STATUS_BLACKLIST
                // Todo 注意 blacklist==1才表示我将对方拉入黑名单，但是我将对方拉入黑名单之后，该接口就不在返回此人了，所以在通讯录的黑名单内需要单独调用获取黑名单列表的接口
                if (attentionUser.getBlacklist() == 1) {
                    friend.setStatus(Constants.STATUS_BLACKLIST);
                }
                if (attentionUser.getIsBeenBlack() == 1) {
                    // friend.setStatus(Constants.STATUS_BLACKLIST);
                    friend.setStatus(Constants.STATUS_19);
                }
                friend.setOfflineNoPushMsg(attentionUser.getOfflineNoPushMsg());
                friend.setChatRecordTimeOut(attentionUser.getChatRecordTimeOut());// 消息保存天数 -1/0 永久
                friend.setCompanyId(attentionUser.getCompanyId());
                friend.setRoomFlag(0);
                friend.setVersion(newVersion);// 更新版本
                result.add(friend);
            }
            Log.i("FriendDao", "线程处理完成:" + Thread.currentThread().getName());
            return result;
        }
    }

    private List<Friend> queryFriend(QueryBuilder<Friend, Integer> builder) throws SQLException {
//        synchronized (FriendDao.class) {
        // return friendDao.queryForFirst(builder.prepare());
//        }
        return null;
    }


    public void addFriends(final String loginUserId, final List<AttentionUser> attentionUsers,
                           final OnCompleteListener2 listener) throws SQLException {
        new TransactionManager(friendDao.getConnectionSource()).callInTransaction(() -> {
            checkSystemFriend(loginUserId);
            int tableVersion = TableVersionSp.getInstance(MyApplication.getInstance()).getFriendTableVersion(loginUserId);
            int newVersion = tableVersion + 1;
            if (attentionUsers != null && attentionUsers.size() > 0) {

                for (int i = 0; i < attentionUsers.size(); i++) {
                    AttentionUser attentionUser = attentionUsers.get(i);
                    if (attentionUser == null) {
                        continue;
                    }
                    String userId = attentionUser.getToUserId();// 好友的Id
                    QueryBuilder<Friend, Integer> builder = friendDao.queryBuilder();
                    Friend friend = null;
                    try {
                        builder.where().eq("ownerId", loginUserId).and().eq("userId", userId);
                        friend = friendDao.queryForFirst(builder.prepare());
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                    if (friend != null) {
                        continue;
                    } else {

                    }
                }
            }
            if (listener != null) {
                listener.onCompleted();
            }

            // 这里没用到返回值，但是这个方法必须返回，
            return Void.class;
        });
    }

    /**
     * 用户数据更新，下载进入的房间
     */
    public void reset(final Handler handler, final String loginUserId, final List<MucRoom> rooms, final OnCompleteUpdateListener listener) {
        ThreadManager.getPool().execute(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                int tableVersion = TableVersionSp.getInstance(MyApplication.getInstance()).getFriendTableVersion(loginUserId);
                int newVersion = tableVersion + 1;
                if (rooms != null && rooms.size() > 0) {
                    Map<String, List<RoomMember>> stringListHashMap = new HashMap<>();
                    for (int i = 0; i < rooms.size(); i++) {
                        MucRoom mucRoom = rooms.get(i);
                        if (mucRoom == null) {
                            continue;
                        }
                        String userId = mucRoom.getJid();// 群组的jid
                        MyApplication.getInstance().saveGroupPartStatus(userId, mucRoom.getShowRead(), mucRoom.getAllowSendCard(),
                                mucRoom.getAllowConference(), mucRoom.getAllowSpeakCourse(), mucRoom.getTalkTime());

                        QueryBuilder<Friend, Integer> builder = friendDao.queryBuilder();
                        Friend friend = null;
                        try {
                            builder.where().eq("ownerId", loginUserId).and().eq("userId", userId);
                            friend = friendDao.queryForFirst(builder.prepare());
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                        if (friend == null) {
                            friend = new Friend();
                            friend.setOwnerId(loginUserId);
                            friend.setUserId(mucRoom.getJid());

                            friend.setTimeSend((int) mucRoom.getCreateTime());
                        }
                        friend.setNickName(mucRoom.getName());
                        friend.setDescription(mucRoom.getDesc());
                        friend.setRoomId(mucRoom.getId());
                        friend.setRoomCreateUserId(mucRoom.getUserId());
                        // Todo getMember可能为空，需要做非空判断，放到下面去
                        // friend.setOfflineNoPushMsg(mucRoom.getMember().getOfflineNoPushMsg());
                        friend.setChatRecordTimeOut(mucRoom.getChatRecordTimeOut());// 消息保存天数 -1/0 永久
                        if (mucRoom.getCategory() == 510 &&
                                mucRoom.getUserId().equals(CoreManager.requireSelf(MyApplication.getInstance()).getUserId())) {
                            friend.setRoomFlag(510);// 我的手机联系人群组
                        } else {
                            friend.setRoomFlag(1);
                        }
                        friend.setStatus(Constants.STATUS_FRIEND);
                        friend.setVersion(newVersion);// 更新版本
                        MucRoomMember memberMy = mucRoom.getMember();
                        if (memberMy != null) {
                            friend.setRoomMyNickName(memberMy.getNickName());
                            friend.setRoomTalkTime(memberMy.getTalkTime());
                            friend.setOfflineNoPushMsg(memberMy.getOfflineNoPushMsg());
                        }
                        if (mucRoom.getMembers() != null && mucRoom.getMembers().size() > 0) {
                            List<RoomMember> members = new ArrayList<>();
                            for (int m = 0; m < mucRoom.getMembers().size(); m++) {// 在异步任务内存储
                                MucRoomMember mucRoomMember = mucRoom.getMembers().get(m);
                                if (mucRoomMember == null) {
                                    continue;
                                }
                                RoomMember roomMember = new RoomMember();
                                if (mucRoomMember.getOnLineState() == 0) {
                                    roomMember.setLastOnLineTime(mucRoomMember.getOfflineTime());
                                } else {
                                    //在线
                                    roomMember.setLastOnLineTime(0);
                                }
                                roomMember.setVipLevel(mucRoomMember.getVip());
                                roomMember.setRoomId(mucRoom.getId());
                                roomMember.setUserId(mucRoomMember.getUserId());
                                roomMember.setUserName(mucRoomMember.getNickName());
                                if (TextUtils.isEmpty(mucRoomMember.getRemarkName())) {
                                    roomMember.setCardName(mucRoomMember.getNickName());
                                } else {
                                    roomMember.setCardName(mucRoomMember.getRemarkName());
                                }
                                roomMember.setTalkTime(mucRoomMember.getTalkTime());
                                roomMember.setRole(mucRoomMember.getRole());
                                roomMember.setCreateTime(mucRoomMember.getCreateTime());
                                members.add(roomMember);
//                                RoomMemberDao.getInstance().saveSingleRoomMember(mucRoom.getId(), roomMember);
                            }
                            stringListHashMap.put(mucRoom.getId(), members);
                        }
                        try {
                            friendDao.createOrUpdate(friend);
                            if (listener != null) {
                                listener.onLoading(i + 1, rooms.size());
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    ThreadManager.getPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            List<RoomMember> allRoomMember = new ArrayList<>();
                            Set<String> strings = stringListHashMap.keySet();
                            Iterator<String> iterator = strings.iterator();
                            while (iterator.hasNext()) {
                                String next = iterator.next();
                                List<RoomMember> members = stringListHashMap.get(next);
                                RoomMemberDao.getInstance().getDao(next);
                                allRoomMember.addAll(members);
                            }
                            for (int i = 0; i < allRoomMember.size(); i++) {
                                RoomMemberDao.getInstance().saveSingleRoomMember(allRoomMember.get(i).getRoomId(), allRoomMember.get(i));
                            }
                            if (handler != null && listener != null) {
                                handler.post(() -> listener.update());
                            }
                        }
                    });
                }
                // 本地Sp中保存的版本号更新（+1）
                TableVersionSp.getInstance(MyApplication.getInstance()).setFriendTableVersion(loginUserId, newVersion);
                // 更新完成，把过期的房间数据删除
                try {
                    DeleteBuilder<Friend, Integer> builder = friendDao.deleteBuilder();
                    builder.where().eq("ownerId", loginUserId).and().eq("roomFlag", 1).and().eq("status", Constants.STATUS_FRIEND).and()
                            .ne("version", newVersion);
                    friendDao.delete(builder.prepare());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                // 朋友数据更新了，在去删除不存在的消息表
                List<String> tables = SQLiteRawUtil.getUserChatMessageTables(mHelper.getReadableDatabase(), loginUserId);
                if (tables != null && tables.size() > 0) {
                    for (int i = 0; i < tables.size(); i++) {
                        String tableName = tables.get(i);
                        String tablePrefix = SQLiteRawUtil.CHAT_MESSAGE_TABLE_PREFIX + loginUserId;
                        int index = tableName.indexOf(tablePrefix);
                        if (index == -1) {
                            continue;
                        }
                        String toUserId = tableName.substring(index + tablePrefix.length(), tableName.length());
                        if (toUserId.equals(Constants.ID_BLOG_MESSAGE) || toUserId.equals(Constants.ID_INTERVIEW_MESSAGE)
                                || toUserId.equals(Constants.ID_NEW_FRIEND_MESSAGE) || toUserId.equals(Constants.ID_SYSTEM_MESSAGE)) {
                            continue;
                        }
                        Friend friend = getFriend(loginUserId, toUserId);
                        if (friend == null) {// 删除这张消息表
                            if (SQLiteRawUtil.isTableExist(mHelper.getWritableDatabase(), tableName)) {
                                SQLiteRawUtil.dropTable(mHelper.getReadableDatabase(), tableName);
                            }
                        }
                    }
                }
                Log.i("FriendDao", "更新服务器群组列表时间:" + (System.currentTimeMillis() - startTime));
                if (handler != null && listener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onCompleted();
                        }
                    });
                }
            }
        });
    }


    /**
     * 用户数据更新，下载进入的房间
     */
    public void addRooms(final Handler handler, final String loginUserId, final List<MucRoom> rooms, final OnCompleteListener2 listener) {
        ThreadManager.getPool().execute(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                int tableVersion = TableVersionSp.getInstance(MyApplication.getInstance()).getFriendTableVersion(loginUserId);
                int newVersion = tableVersion + 1;
                if (rooms != null && rooms.size() > 0) {
                    for (int i = 0; i < rooms.size(); i++) {
                        MucRoom mucRoom = rooms.get(i);
                        if (mucRoom == null) {
                            continue;
                        }
                        String userId = mucRoom.getJid();// 群组的jid
                        MyApplication.getInstance().saveGroupPartStatus(userId, mucRoom.getShowRead(), mucRoom.getAllowSendCard(),
                                mucRoom.getAllowConference(), mucRoom.getAllowSpeakCourse(), mucRoom.getTalkTime());

                        QueryBuilder<Friend, Integer> builder = friendDao.queryBuilder();
                        Friend friend = null;
                        try {
                            builder.where().eq("ownerId", loginUserId).and().eq("userId", userId);
                            friend = friendDao.queryForFirst(builder.prepare());
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                        if (friend == null) {
                            friend = new Friend();
                            friend.setOwnerId(loginUserId);
                            friend.setUserId(mucRoom.getJid());

                            friend.setTimeSend((int) mucRoom.getCreateTime());
                        }
                        friend.setNickName(mucRoom.getName());
                        friend.setDescription(mucRoom.getDesc());
                        friend.setRoomId(mucRoom.getId());
                        friend.setRoomCreateUserId(mucRoom.getUserId());
                        // Todo getMember可能为空，需要做非空判断，放到下面去
                        // friend.setOfflineNoPushMsg(mucRoom.getMember().getOfflineNoPushMsg());
                        friend.setChatRecordTimeOut(mucRoom.getChatRecordTimeOut());// 消息保存天数 -1/0 永久
                        if (mucRoom.getCategory() == 510 &&
                                mucRoom.getUserId().equals(CoreManager.requireSelf(MyApplication.getInstance()).getUserId())) {
                            friend.setRoomFlag(510);// 我的手机联系人群组
                        } else {
                            friend.setRoomFlag(1);
                        }
                        friend.setStatus(Constants.STATUS_FRIEND);
                        friend.setVersion(newVersion);// 更新版本
                        MucRoomMember memberMy = mucRoom.getMember();
                        if (memberMy != null) {
                            friend.setRoomMyNickName(memberMy.getNickName());
                            friend.setRoomTalkTime(memberMy.getTalkTime());
                            friend.setOfflineNoPushMsg(memberMy.getOfflineNoPushMsg());
                        }
                        if (mucRoom.getMembers() != null && mucRoom.getMembers().size() > 0) {
                            List<RoomMember> members = new ArrayList<>();
                            for (int m = 0; m < mucRoom.getMembers().size(); m++) {// 在异步任务内存储
                                MucRoomMember mucRoomMember = mucRoom.getMembers().get(m);
                                if (mucRoomMember == null) {
                                    continue;
                                }
                                RoomMember roomMember = new RoomMember();
                                if (mucRoomMember.getOnLineState() == 0) {
                                    roomMember.setLastOnLineTime(mucRoomMember.getOfflineTime());
                                } else {
                                    //在线
                                    roomMember.setLastOnLineTime(0);
                                }
                                roomMember.setVipLevel(mucRoomMember.getVip());
                                roomMember.setRoomId(mucRoom.getId());
                                roomMember.setUserId(mucRoomMember.getUserId());
                                roomMember.setUserName(mucRoomMember.getNickName());
                                if (TextUtils.isEmpty(mucRoomMember.getRemarkName())) {
                                    roomMember.setCardName(mucRoomMember.getNickName());
                                } else {
                                    roomMember.setCardName(mucRoomMember.getRemarkName());
                                }
                                roomMember.setTalkTime(mucRoomMember.getTalkTime());
                                roomMember.setRole(mucRoomMember.getRole());
                                roomMember.setCreateTime(mucRoomMember.getCreateTime());
                                members.add(roomMember);
                                RoomMemberDao.getInstance().saveSingleRoomMember(mucRoom.getId(), roomMember);
                            }
                        }
                        try {
                            friendDao.createOrUpdate(friend);
                            if (listener != null) {
                                listener.onLoading(i + 1, rooms.size());
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // 本地Sp中保存的版本号更新（+1）
                TableVersionSp.getInstance(MyApplication.getInstance()).setFriendTableVersion(loginUserId, newVersion);
                // 更新完成，把过期的房间数据删除
                try {
                    DeleteBuilder<Friend, Integer> builder = friendDao.deleteBuilder();
                    builder.where().eq("ownerId", loginUserId).and().eq("roomFlag", 1).and().eq("status", Constants.STATUS_FRIEND).and()
                            .ne("version", newVersion);
                    friendDao.delete(builder.prepare());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                // 朋友数据更新了，在去删除不存在的消息表
                List<String> tables = SQLiteRawUtil.getUserChatMessageTables(mHelper.getReadableDatabase(), loginUserId);
                if (tables != null && tables.size() > 0) {
                    for (int i = 0; i < tables.size(); i++) {
                        String tableName = tables.get(i);
                        String tablePrefix = SQLiteRawUtil.CHAT_MESSAGE_TABLE_PREFIX + loginUserId;
                        int index = tableName.indexOf(tablePrefix);
                        if (index == -1) {
                            continue;
                        }
                        String toUserId = tableName.substring(index + tablePrefix.length(), tableName.length());
                        if (toUserId.equals(Constants.ID_BLOG_MESSAGE) || toUserId.equals(Constants.ID_INTERVIEW_MESSAGE)
                                || toUserId.equals(Constants.ID_NEW_FRIEND_MESSAGE) || toUserId.equals(Constants.ID_SYSTEM_MESSAGE)) {
                            continue;
                        }
                        Friend friend = getFriend(loginUserId, toUserId);
                        if (friend == null) {// 删除这张消息表
                            if (SQLiteRawUtil.isTableExist(mHelper.getWritableDatabase(), tableName)) {
                                SQLiteRawUtil.dropTable(mHelper.getReadableDatabase(), tableName);
                            }
                        }
                    }
                }
                Log.i("FriendDao", "更新服务器群组列表时间:" + (System.currentTimeMillis() - startTime));
                if (handler != null && listener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onCompleted();
                        }
                    });
                }
            }
        });
    }

    public List<Friend> queryFriendByKey(String ownerId, String str) {
        QueryBuilder<Friend, Integer> queryBuilder = friendDao.queryBuilder();
        try {
            List<Friend> friendList = queryBuilder.where()
                    .like("nickName", "%" + str + "%")
                    .or().like("remarkName", "%" + str + "%")
                    .and().eq("ownerId", ownerId)
                    .query();
            return friendList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<Friend> querySingleFriendByKey(String ownerId, String str) {
        QueryBuilder<Friend, Integer> queryBuilder = friendDao.queryBuilder();
        try {
            List<Friend> friendList = queryBuilder.where()
                    .like("nickName", "%" + str + "%")
                    .or().like("remarkName", "%" + str + "%")
                    .and().eq("roomFlag", 0)
                    .and().eq("ownerId", ownerId)
                    .and().eq("status", Constants.STATUS_FRIEND)
                    .query();
            return friendList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    public boolean queryFriendByFriendId(String ownerId, String friendId) {
        try {
            long l = friendDao.queryBuilder().where()
                    .eq("ownerId", ownerId).and().eq("userId", friendId)
                    .countOf();
            return l > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateStrongRemindStatus(String groupID, boolean isChecked) {
        //当前 数据库没有该 强提醒字段 ，先用Sp进行扩展存储，后续升级数据库
        FriendTableExpandSp.getInstance(MyApplication.getInstance()).setStrongRemindStatusByGroupId(groupID, isChecked);
    }

    public boolean getStrongRemindStatus(String groupID) {
        return FriendTableExpandSp.getInstance(MyApplication.getInstance()).getStrongRemindStatusByGroupId(groupID);
    }


    /**
     * 根据id 获取一组好友
     *
     * @param userId
     * @param ids
     * @return
     */
    public List<Friend> getFriends(String userId, Object[] ids) {
        if (ids == null || ids.length <= 0) {
            return new ArrayList<>();
        }
        try {
            PreparedQuery<Friend> preparedQuery = friendDao.queryBuilder().where()
                    .eq("ownerId", userId)
                    .and().in("userId", ids).prepare();
            return friendDao.query(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

