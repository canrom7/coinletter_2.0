package com.iimm.miliao.db.dao;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.bean.message.ReceiptMsg;
import com.iimm.miliao.db.SQLiteHelper;

import org.jivesoftware.smack.packet.Message;

import java.sql.SQLException;

public class ReceiptMsgDao {
    private static ReceiptMsgDao instance;
    private SQLiteHelper mHelper;
    private Dao<ReceiptMsg, Integer> mMsgIntegerDao;

    private ReceiptMsgDao() {
        try {
            mHelper = OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class);
            mMsgIntegerDao = DaoManager.createDao(mHelper.getConnectionSource(), ReceiptMsg.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ReceiptMsgDao getInstance() {
        if (instance == null) {
            synchronized (ReceiptMsgDao.class) {
                if (instance == null) {
                    instance = new ReceiptMsgDao();
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

    public void insertMsg(Message message) {
        if (mMsgIntegerDao != null) {
            ReceiptMsg receiptMsg = new ReceiptMsg();
            receiptMsg.setMsgContent(message.toString());
            try {
                mMsgIntegerDao.create(receiptMsg);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void clearMsg() {
        if (mMsgIntegerDao != null) {
            DeleteBuilder<ReceiptMsg, Integer> builder = mMsgIntegerDao.deleteBuilder();
            try {
                builder.delete();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
