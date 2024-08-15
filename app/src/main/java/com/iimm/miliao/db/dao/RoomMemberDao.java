package com.iimm.miliao.db.dao;

import android.text.TextUtils;
import android.util.Log;

import com.j256.ormlite.android.DatabaseTableConfigUtil;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.RoomMember;
import com.iimm.miliao.db.SQLiteHelper;
import com.iimm.miliao.db.SQLiteRawUtil;
import com.iimm.miliao.db.SQLiteRawUtil2;
import com.iimm.miliao.db.UnlimitDaoManager;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.ToolUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Create by zq
 */
//访问用户群组的Dao
public class RoomMemberDao {
    private static RoomMemberDao instance = null;
    private SQLiteHelper mHelper;
    private Map<String, Dao<RoomMember, Integer>> mDaoMap;

    private RoomMemberDao() {
        mHelper = OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class);
        mDaoMap = new HashMap<String, Dao<RoomMember, Integer>>();
    }

    public static final RoomMemberDao getInstance() {
        if (instance == null) {
            synchronized (RoomMemberDao.class) {
                if (instance == null) {
                    instance = new RoomMemberDao();
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

    public Dao<RoomMember, Integer> getDao(String roomId) {
        if (TextUtils.isEmpty(roomId)) {
            return null;
        }
        String tableName = SQLiteRawUtil2.MEMBER_TABLE_PREFIX + roomId;
        if (mDaoMap.containsKey(tableName)) {
            return mDaoMap.get(tableName);
        }
        Dao<RoomMember, Integer> dao = null;
        try {
            DatabaseTableConfig<RoomMember> config = DatabaseTableConfigUtil.fromClass(mHelper.getConnectionSource(), RoomMember.class);
            config.setTableName(tableName);
            SQLiteRawUtil2.createTableIfNotExist(mHelper.getWritableDatabase(), tableName, SQLiteRawUtil2.getCreateRoomMemberTableSql(tableName));
            dao = UnlimitDaoManager.createDao(mHelper.getConnectionSource(), config);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (dao != null)
            mDaoMap.put(tableName, dao);
        return dao;
    }
    /***************************
     * 增
     * *************************
     */
    /**
     * 添加 || 更新 一个群成员
     */
    public void saveSingleRoomMember(String roomId, RoomMember roomMember) {
        Dao<RoomMember, Integer> dao = getDao(roomId);
        if (dao == null) {
            // 暂无该表
            return;
        }
        try {
            RoomMember member = getSingleRoomMember(roomId, roomMember.getUserId());
            if (member != null) {
                roomMember.set_id(member.get_id());
                dao.update(roomMember);
            } else {
                // 保存该成员信息
                dao.create(roomMember);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /***************************
     * 删
     * *************************
     */

    /**
     * 删除某个成员
     */
    public boolean deleteRoomMember(String roomId, String userId) {
        Dao<RoomMember, Integer> dao = getDao(roomId);
        if (dao == null) {
            return false;
        }
        try {
            List<RoomMember> roomMembers = dao.queryForEq("userId", userId);
            if (roomMembers != null && roomMembers.size() > 0) {
                dao.delete(roomMembers);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 删除表
     */
    public void deleteRoomMemberTable(String roomId) {
        String tableName = SQLiteRawUtil2.MEMBER_TABLE_PREFIX + roomId;
        if (mDaoMap.containsKey(tableName)) {
            mDaoMap.remove(tableName);
        }
        if (SQLiteRawUtil.isTableExist(mHelper.getWritableDatabase(), tableName)) {
            SQLiteRawUtil.dropTable(mHelper.getWritableDatabase(), tableName);
        }
        // 删除表只在刷新群成员列表时调用，所以直接在这里标记群头像过期，
        UserAvatarDao.getInstance().saveUpdateTime(roomId);
    }

    /***************************
     * 改
     * *************************
     */
    /**
     * 更新职位
     */
    public void updateRoomMemberRole(String roomId, String userId, int role) {
        Dao<RoomMember, Integer> dao = getDao(roomId);
        if (dao == null) {
            Log.e("zq", "更新失败");
            return;
        }
        UpdateBuilder<RoomMember, Integer> builder = dao.updateBuilder();
        try {
            Log.e("zq", "更改表：member_" + roomId);
            builder.updateColumnValue("role", role);
            builder.where().eq("userId", userId);
            dao.update(builder.prepare());
            Log.e("zq", "更新完成");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新备注名
     */
    public void updateRoomMemberCardName(String roomId, String userId, String cardName) {
        Dao<RoomMember, Integer> dao = getDao(roomId);
        if (dao == null) {
            Log.e("zq", "更新失败");
            return;
        }
        UpdateBuilder<RoomMember, Integer> builder = dao.updateBuilder();
        try {
            Log.e("zq", "更改表：member_" + roomId);
            builder.updateColumnValue("cardName", cardName);
            builder.where().eq("userId", userId);
            dao.update(builder.prepare());
            Log.e("zq", "更新完成");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /***************************
     * 查
     * *************************
     */
    /**
     * 获取表内某条数据
     */
    public RoomMember getSingleRoomMember(String roomId, String userId) {
        Dao<RoomMember, Integer> dao = getDao(roomId);
        if (dao == null) {
            return null;
        }
        QueryBuilder<RoomMember, Integer> builder = dao.queryBuilder();
        RoomMember mRoomMember = null;
        try {
            if (!TextUtils.isEmpty(userId)) {
                builder.where().eq("userId", userId);
            }
            mRoomMember = dao.queryForFirst(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mRoomMember;
    }

    /**
     * 获取表内所有数据
     */
    public List<RoomMember> getRoomMember(String roomId) {
        List<RoomMember> roomMembers = new ArrayList<>();
        Dao<RoomMember, Integer> dao = getDao(roomId);
        if (dao == null) {
            return roomMembers;
        }
        try {
            roomMembers = dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roomMembers;
    }
    /**
     * 获取表内所有数据
     */
    public int getMyRole(String roomId) {
        List<RoomMember> roomMembers = new ArrayList<>();
        Dao<RoomMember, Integer> dao = getDao(roomId);
        if (dao == null) {
            return -1;
        }
        try {
            roomMembers = dao.queryForAll();
            for (RoomMember roomMember : roomMembers) {
                if (roomMember.getUserId().equals(MyApplication.getLoginUserId())) {
                    return roomMember.getRole();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    /**
     * 获取成员用于群转让群主，
     * 所以要过滤掉群主，
     */
    public List<RoomMember> getRoomMemberForTransfer(String roomId, String selfId) throws SQLException {
        Dao<RoomMember, Integer> dao = getDao(roomId);
        if (dao == null) {
            return new ArrayList<>();
        }
        QueryBuilder<RoomMember, Integer> builder = dao.queryBuilder();
        builder.where().ne("userId", selfId);
        return builder.query();
    }

    public List<String> getRoomMemberForAvatar(String roomId, String selfId) throws SQLException {
        Objects.requireNonNull(roomId);
        Objects.requireNonNull(selfId);
        Dao<RoomMember, Integer> dao = getDao(roomId);
        if (dao == null) {
            throw new IllegalStateException("获取DAO失败<" + roomId + ", " + selfId + ">");
        }
        // select userId from table where userId != :selfId limit 5;
        QueryBuilder<RoomMember, Integer> builder = dao.queryBuilder()
                .selectColumns("userId");
        // 群组头像最多为9人组合
        if (Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR) {
            builder.limit(5L);//圆形头像
        } else {
            builder.limit(9L);//方形形头像
        }
        return dao.queryRaw(builder.prepareStatementString(), (columnNames, resultColumns) -> resultColumns[0]).getResults();
    }

    /**
     * 群主优先显示群主备注
     */
    public String getRoomRemarkName(String roomJid, String userId) {
        Friend friend = FriendDao.getInstance().getFriend(CoreManager.requireSelf(MyApplication.getContext()).getUserId(), roomJid);
        if (friend != null) {
            RoomMember mMember = getSingleRoomMember(friend.getRoomId(), CoreManager.requireSelf(MyApplication.getContext()).getUserId());
            if (mMember != null && mMember.getRole() == 1) {
                RoomMember member = getSingleRoomMember(friend.getRoomId(), userId);
                if (member != null && !TextUtils.equals(member.getUserName(), member.getCardName())) {
                    // 当userName与cardName不一致时，我们认为群主有设置群内备注(也只有群主的才会不一样)
                    return member.getCardName();
                }
            }
        }
        return null;
    }

    public List<RoomMember> getRoomMembersByKey(String str, String roomId) {
        try {
            str = ToolUtils.sqliteEscape(str);
            Dao<RoomMember, Integer> memberIntegerDao = getDao(roomId);
            QueryBuilder<RoomMember, Integer> queryBuilder = memberIntegerDao.queryBuilder();
            return queryBuilder.where().like("userName", "%" + str + "%").or().like("cardName", "%" + str + "%").query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public SQLiteHelper getHelper() {
        return mHelper;
    }

    /**
     * 把群主变成普通成员
     * @param roomId
     * @param role
     */
    public void updateRoomMemberRole(String roomId, int role) {
        Dao<RoomMember, Integer> dao = getDao(roomId);
        if (dao == null) {
            Log.e("zq", "更新失败");
            return;
        }
        UpdateBuilder<RoomMember, Integer> builder = dao.updateBuilder();
        try {
            Log.e("zq", "更改表：member_" + roomId);
            builder.updateColumnValue("role", role);
            builder.where().eq("role", 1);
            dao.update(builder.prepare());
            Log.e("zq", "更新完成");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
