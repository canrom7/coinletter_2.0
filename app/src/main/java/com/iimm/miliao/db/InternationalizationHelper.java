package com.iimm.miliao.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Prefix;
import com.iimm.miliao.util.LocaleHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2017/4/12 0012.
 * 与ios统一，使用数据库进行国际化
 */

public class InternationalizationHelper {

    private final int BUFFER_SIZE = 400000;
    public static final String DB_NAME = "constant.db"; //保存的数据库文件名

    private InternationalizationHelper() {
    }

    private static SQLiteDatabase mInstance = null;
    private static InternationalizationHelper helper = new InternationalizationHelper();


    public static SQLiteDatabase get() {
        if (mInstance == null) {
            synchronized (SQLiteDatabase.class) {
                if (mInstance == null) {
                    mInstance = helper.openDatabase();
                }
            }
        }
        return mInstance;
    }

    public static InternationalizationHelper getInternationalizationHelper() {
        return helper;
    }

    /**
     * 国际化
     *
     * @param iso
     * @return
     */
    public static String getString(String iso) {
        SQLiteDatabase db = helper.openDatabase();
        if (db != null) {
            String table = "lang";
            String[] columns = new String[]{"zh", "en", "big5"};
            String selection = "ios=?";
            String[] selectionArgs = new String[]{iso};
            Cursor cursor = db.query(table, columns, selection, selectionArgs, null, null, null, null);

            String ms = LocaleHelper.getPersistedData(MyApplication.getContext(), Locale.getDefault().getLanguage());
            String language = " ";

            if (cursor.moveToNext()) {
                if (ms.equals("zh")) {
                    language = cursor.getString(cursor.getColumnIndex("zh"));
                } else if (ms.equals("HK") || ms.equals("TW")) {
                    language = cursor.getString(cursor.getColumnIndex("big5"));
                } else {
                    language = cursor.getString(cursor.getColumnIndex("en"));
                }
            }
            cursor.close();
            db.close();
            return language;
        }
        return null;
    }

    /*
    查询SMS_country,返回所有数据
     */
    public static List<Prefix> getPrefixList() {
        List<Prefix> prefixList = new ArrayList<>();
        SQLiteDatabase db = helper.openDatabase();
        if (db != null) {
            String table = "SMS_country";
            Cursor cursor = db.query(table, null, null, null, null, null, null, null);

            while (cursor.moveToNext()) {
                Prefix preFix = new Prefix();
                String country = cursor.getString(cursor.getColumnIndex("country"));
                String enName = cursor.getString(cursor.getColumnIndex("enName"));
                int prefix = cursor.getInt(cursor.getColumnIndex("prefix"));
                preFix.setCountry(country);
                preFix.setEnName(enName);
                preFix.setPrefix(prefix);
                prefixList.add(preFix);
            }
            cursor.close();
            db.close();
        }
        return prefixList;
    }

    /*
    查询SMS_country,返回所有查询数据
     */
    public static List<Prefix> getSearchPrefix(String Selection) {
        List<Prefix> prefixList = new ArrayList<>();
        SQLiteDatabase db = helper.openDatabase();
        if (db != null) {
            if (Locale.getDefault().getLanguage().equals("zh")) {
                String table = "SMS_country";
                String selection = "country like ?";
                Selection = "%" + Selection + "%";
                String[] selectionArgs = new String[]{Selection};
                Cursor cursor = db.query(table, null, selection, selectionArgs, null, null, null, null);

                while (cursor.moveToNext()) {
                    Prefix preFix = new Prefix();
                    String country = cursor.getString(cursor.getColumnIndex("country"));
                    String enName = cursor.getString(cursor.getColumnIndex("enName"));
                    int prefix = cursor.getInt(cursor.getColumnIndex("prefix"));
                    preFix.setCountry(country);
                    preFix.setEnName(enName);
                    preFix.setPrefix(prefix);
                    prefixList.add(preFix);
                }
                cursor.close();
                db.close();
            } else if (Locale.getDefault().getLanguage().equals("en")) {
                String table = "SMS_country";
                String selection = "enName like ?";
                Selection = "%" + Selection + "%";
                String[] selectionArgs = new String[]{Selection};
                Cursor cursor = db.query(table, null, selection, selectionArgs, null, null, null, null);

                while (cursor.moveToNext()) {
                    Prefix preFix = new Prefix();
                    String country = cursor.getString(cursor.getColumnIndex("country"));
                    String enName = cursor.getString(cursor.getColumnIndex("enName"));
                    int prefix = cursor.getInt(cursor.getColumnIndex("prefix"));
                    preFix.setCountry(country);
                    preFix.setEnName(enName);
                    preFix.setPrefix(prefix);
                    prefixList.add(preFix);
                }
                cursor.close();
                db.close();
            } else {
                // 其他国家默认使用英文
                String table = "SMS_country";
                String selection = "enName like ?";
                Selection = "%" + Selection + "%";
                String[] selectionArgs = new String[]{Selection};
                Cursor cursor = db.query(table, null, selection, selectionArgs, null, null, null, null);

                while (cursor.moveToNext()) {
                    Prefix preFix = new Prefix();
                    String country = cursor.getString(cursor.getColumnIndex("country"));
                    String enName = cursor.getString(cursor.getColumnIndex("enName"));
                    int prefix = cursor.getInt(cursor.getColumnIndex("prefix"));
                    preFix.setCountry(country);
                    preFix.setEnName(enName);
                    preFix.setPrefix(prefix);
                    prefixList.add(preFix);
                }
                cursor.close();
                db.close();
            }

        }
        return prefixList;
    }

    private SQLiteDatabase openDatabase() {
        try {
            File dbfile = MyApplication.getContext().getDatabasePath(DB_NAME);
            if (!(dbfile.exists())) {
                //判断数据库文件是否存在，若不存在则执行导入，否则直接打开数据库
                InputStream is = MyApplication.getContext().getResources().openRawResource(
                        R.raw.constant); //欲导入的数据库

                FileOutputStream fos = new FileOutputStream(dbfile);
                byte[] buffer = new byte[BUFFER_SIZE];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            }

            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
            return db;

        } catch (FileNotFoundException e) {
            Log.e("Database", "File not found");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Database", "IO exception");
            e.printStackTrace();
        } catch (Exception e){
            Log.e("Database", "Exception");
            e.printStackTrace();
        }
        return null;
    }
}
