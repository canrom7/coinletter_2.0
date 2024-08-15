package com.iimm.miliao.db.dao;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.bean.message.SendMsg;
import com.iimm.miliao.db.SQLiteHelper;

import java.sql.SQLException;

public class SendMsgDao {

    private static SendMsgDao instance;
    private  Dao<SendMsg, Integer> mSendMsgIntegerDao;
    private  SQLiteHelper mHelper;

    private SendMsgDao() {
        try {
            mHelper = OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class);
            mSendMsgIntegerDao = DaoManager.createDao(mHelper.getConnectionSource(), SendMsg.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static SendMsgDao getInstance() {
        if (instance == null) {
            synchronized (SendMsgDao.class) {
                if (instance == null) {
                    instance = new SendMsgDao();
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

    public void insertMsg(SendMsg sendMsg) {
        if (mSendMsgIntegerDao != null) {
            try {
                mSendMsgIntegerDao.create(sendMsg);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public void clearMsg() {
        if (mSendMsgIntegerDao != null) {
            DeleteBuilder<SendMsg, Integer> builder = mSendMsgIntegerDao.deleteBuilder();
            try {
                builder.delete();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
