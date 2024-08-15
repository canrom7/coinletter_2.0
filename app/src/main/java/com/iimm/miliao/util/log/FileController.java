package com.iimm.miliao.util.log;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class FileController {
    private Context mContext;
    private volatile static FileController m_instance;
    private final String LOG_FILENAME = "LOG_FILE";

    private FileController() {
    }

    public static synchronized FileController getFileControl() {
        if (m_instance == null) {
            synchronized (FileController.class) {
                if (m_instance == null) {
                    m_instance = new FileController();
                }
            }
        }
        return m_instance;
    }

    public void init(Context context) {
        mContext = context;
    }


    public void deleteLogFile() {
        try {
            String dir = getSaveDirectory("IMLogs");
            File file = new File(dir, LOG_FILENAME);

            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readFromLogFile() {
        String dir = getSaveDirectory("IMLogs");
        File file = new File(dir, LOG_FILENAME);
        String data = null;
        BufferedReader br = null;
        if (!file.exists())
            return null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n\r");
                line = br.readLine();
            }
            data = sb.toString();
            if (!data.isEmpty()) {
                return data;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return data;
        }
    }

    public static String getSaveDirectory(String str) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + str + "/";
            File file = new File(rootDir);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return null;
                }
            }
            return rootDir;
        } else {
            return null;
        }
    }

    public void writeToLogFile(String s) {
        if (s == null) return;
        FileOutputStream outputStream = null;
        try {
            String dir = getSaveDirectory("IMLogs");
            File file = new File(dir, LOG_FILENAME);
            outputStream = new FileOutputStream(file, true); // will overwrite existing data

            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outputStream.write(s.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
