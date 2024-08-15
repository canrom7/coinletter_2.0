package com.iimm.miliao.ui.systemshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.FileUtil;
import com.iimm.miliao.util.ImageUtils;
import com.iimm.miliao.util.LogUtils;
import com.iimm.miliao.util.log.FileUtils;

import java.io.File;

public class ShareUtil {

    /**
     * @return 初始化失败需要结束activity就返回true,
     */
    public static boolean shareInit(Activity activity, ChatMessage mShareChatMessage) {
        Intent intent = activity.getIntent();
        LogUtils.log(intent);
        if (intent == null) {
            DialogHelper.tip(activity, "intent不能为空");
            return true;
        }
        String type = intent.getType();
        if (TextUtils.isEmpty(type)) {
            DialogHelper.tip(activity, "获取类型失败");
            return true;
        }
        Uri stream = parseStream(intent);
        if (stream == null) {
            if (isText(intent)) {
                mShareChatMessage.setType(Constants.TYPE_TEXT);
                mShareChatMessage.setContent(parseText(intent));
            }
        } else {
            File file = getFileFromStream(activity, intent);
            if (file == null) {
                DialogHelper.tip(activity, activity.getString(R.string.tip_file_cache_failed));
                return true;
            }
            if (isImage(intent)) {
                mShareChatMessage.setType(Constants.TYPE_IMAGE);
                int[] imageParam = ImageUtils.getImageWidthHeight(file.getPath());
                mShareChatMessage.setLocation_x(String.valueOf(imageParam[0]));
                mShareChatMessage.setLocation_y(String.valueOf(imageParam[1]));
            } else if (isVideo(intent)) {
                mShareChatMessage.setType(Constants.TYPE_VIDEO);
            } else {
                mShareChatMessage.setType(Constants.TYPE_FILE);
            }
            mShareChatMessage.setFilePath(file.getPath());
            mShareChatMessage.setFileSize((int) file.length());
        }
        if (mShareChatMessage.getType() == 0) {
            DialogHelper.tip(activity, activity.getString(R.string.tip_share_type_not_supported));
            return true;
        }
        return false;
    }

    public static String parseText(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.getStringExtra(Intent.EXTRA_TEXT);
    }

    public static Uri parseStream(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.getParcelableExtra(Intent.EXTRA_STREAM);
    }

    public static File getFileFromStream(Context ctx, Intent intent) {
        Uri stream = parseStream(intent);
        if (stream == null) {
            return null;
        }
        String filePath = FileUtils.getPath(ctx, stream);
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            Reporter.post("文件不存在");
        } else {
            return file;
        }
        return null;
    }

    public static String getFilePathFromStream(Context ctx, Intent intent) {
        Uri stream = parseStream(intent);
        if (stream == null) {
            return null;
        }
        String filePath = FileUtil.getFilePath(ctx, stream);
        return filePath;
    }

    /**
     * 判断是否是分享文字，不包括分享txt文件的情况，
     */
    public static boolean isText(Intent intent) {
        return checkType(intent, "text")
                && !TextUtils.isEmpty(parseText(intent));
    }

    public static boolean isImage(Intent intent) {
        return isFile(intent)
                && checkType(intent, "image");
    }

    public static boolean isVideo(Intent intent) {
        return isFile(intent)
                && checkType(intent, "video");
    }

    public static boolean isFile(Intent intent) {
        return parseStream(intent) != null;
    }

    private static boolean checkType(Intent intent, String prefix) {
        if (intent == null) {
            return false;
        }
        String type = intent.getType();
        if (TextUtils.isEmpty(type)) {
            return false;
        }
        return type.startsWith(prefix);
    }
}
