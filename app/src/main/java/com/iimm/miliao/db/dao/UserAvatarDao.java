package com.iimm.miliao.db.dao;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.bean.UserAvatar;
import com.iimm.miliao.db.SQLiteHelper;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.util.TimeUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 访问用户数据的Dao，包括访问两个实体，User和UserDetail
 */
public class UserAvatarDao {
    private Map<String, String> updateCacheMap = new HashMap<>();

    private static UserAvatarDao instance = null;

    public static UserAvatarDao getInstance() {
        if (instance == null) {
            synchronized (UserAvatarDao.class) {
                if (instance == null) {
                    instance = new UserAvatarDao();
                }
            }
        }
        return instance;
    }

    public Dao<UserAvatar, String> userDao;

    private UserAvatarDao() {
        try {
            OrmLiteSqliteOpenHelper helper = OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class);
            userDao = DaoManager.createDao(helper.getConnectionSource(), UserAvatar.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        OpenHelperManager.releaseHelper();
    }

    public long saveUpdateTime(String userId) {
        long time = TimeUtils.time_current_time();
        QueryBuilder<UserAvatar, String> builder = userDao.queryBuilder();
        try {
            UserAvatar userAvatar = builder.where().eq("userId", userId).queryForFirst();
            if (userAvatar == null) {
                userAvatar = new UserAvatar();
                userAvatar.setUserId(userId);
                userAvatar.setTime(time);
                userDao.create(userAvatar);
                updateCacheMap.put(userId, time+"");
            } else {
                updateUserAvatarTime(userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return time;
    }

    // 更新群内昵称
    private void updateUserAvatarTime(String ownerId) {
        UpdateBuilder<UserAvatar, String> builder = userDao.updateBuilder();
        try {
            long time = TimeUtils.time_current_time();
            builder.where().eq("userId", ownerId);
            builder.updateColumnValue("time", time);
            userDao.update(builder.prepare());
            updateCacheMap.put(ownerId, time+"");
            AvatarHelper.getInstance().timeMap.put(ownerId, time + "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getUpdateTime(String userId) {
        if (updateCacheMap != null && updateCacheMap.containsKey(userId)) {
            return updateCacheMap.get(userId);
        }
        long time = 0L;
        QueryBuilder<UserAvatar, String> builder = userDao.queryBuilder();
        try {
            UserAvatar userAvatar = builder.where().eq("userId", userId).queryForFirst();
            if (userAvatar == null) {
                time = saveUpdateTime(userId);
            } else {
                time = userAvatar.getTime();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateCacheMap.put(userId, time+"");
        return String.valueOf(time);
    }
}
