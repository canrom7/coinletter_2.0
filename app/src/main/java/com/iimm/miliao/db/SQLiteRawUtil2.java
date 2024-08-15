package com.iimm.miliao.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 对原生SQL语句的一些支持
 *
 * @author zq
 */
public class SQLiteRawUtil2 {
    public static final int CURRENT_TABLE_FIELD_NUM = 10;  //当前表中字段的数据
    public static final String MEMBER_TABLE_PREFIX = "member_";
    private static final String TAG = "SQLiteRawUtil2";

    // 一个实体类创建多张表格,不能使用TableUtils创建表格,需使用原生sql语句
    public static String getCreateRoomMemberTableSql(String tableName) {
        String sql = "CREATE TABLE IF NOT EXISTS "
                + tableName
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,roomId VARCHAR,userId VARCHAR,userName VARCHAR,cardName VARCHAR,role INTEGER,createTime INTEGER,lastOnLineTime INTEGER,vipLevel INTEGER,talkTime INTEGER)";
        return sql;
    }

    // 创建表格
    public static void createTableIfNotExist(SQLiteDatabase db, String tableName, String createTableSql) {
        onUpgrade(db, tableName);
        if (isTableExist(db, tableName)) {
            return;
        }
        db.execSQL(createTableSql);
    }

    /**
     * 检测当前表中是否包含升级字段 包含就略过  ，不包含 删除此表
     *
     * @param db
     * @param tableName
     */
    private static void onUpgrade(SQLiteDatabase db, String tableName) {
//        String sql = "select 1 from sqlite_master  where tbl_name=? and sql like ? and sql like ? and sql like ?";
//        Cursor cursor = db.rawQuery(sql, new String[]{tableName, "'%,lastOnLineTime%'", "'%,vipLevel%'", "'%,talkTime%'"});
        String sql = "PRAGMA table_info('" + tableName + "')";
        Cursor cursor = db.rawQuery(sql, new String[0]);
        if (cursor != null) {
            if (cursor.getCount() != CURRENT_TABLE_FIELD_NUM) {
                dropTable(db, tableName);
                cursor.close();
            } else {
                cursor.close();
                return;
            }
        } else {
            dropTable(db, tableName);
        }
    }

    // 表格是否存在
    public static boolean isTableExist(SQLiteDatabase db, String tableName) {
        boolean result = false;
        if (TextUtils.isEmpty(tableName.trim())) {
            return false;
        }
        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from Sqlite_master  where type ='table' and name ='" + tableName.trim() + "' ";
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }


    // 清除指定的表格
    public static void dropTable(SQLiteDatabase db, String tableName) {
        try {
            if (isTableExist(db, tableName)) {
                String sql = "drop table " + tableName;
                db.execSQL(sql);
            }
        } catch (Exception e) {

        }
    }

    /**
     * 获取当前用户的群组表
     *
     * @param db
     * @param roomId
     * @return
     */
    public static List<String> getUserRoomMemberTables(SQLiteDatabase db, String roomId) {
        String tablePrefix = MEMBER_TABLE_PREFIX + roomId;
        Cursor cursor = null;
        try {
            String sql = "select name from Sqlite_master where type ='table' and name like '" + tablePrefix + "%'";
            cursor = db.rawQuery(sql, null);
            if (cursor != null) {
                List<String> tables = new ArrayList<String>();
                while (cursor.moveToNext()) {
                    String name = cursor.getString(0);
                    if (!TextUtils.isEmpty(name)) {
                        tables.add(name);
                    }
                }
                return tables;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}
