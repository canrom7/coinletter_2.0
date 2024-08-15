package com.iimm.miliao.xmpp.util;

import android.text.TextUtils;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.FileUtil;
import com.iimm.miliao.util.ToastUtil;

import java.io.File;

public class ImFileDownload {

    public static String TAG = "ImFileDownload";

    /**
     * @param chatMessage
     * @param fileDirectory        下载完成后保存的文件夹路径
     * @param shouldRefreshGallery 是否需要刷新图库
     */
    public static void downloadFileIfNeeded(ChatMessage chatMessage, String fileDirectory, boolean shouldRefreshGallery) {
        if (TextUtils.isEmpty(chatMessage.getContent())) {
            return;
        }
        if (TextUtils.isEmpty(fileDirectory)) {
            fileDirectory = FileUtil.getFileDir();
        } else {
            File file = new File(fileDirectory);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        String fileName = chatMessage.getFilePath();//对方发送的文件，对方的文件本地路径保存在了 filePaht中
        if (TextUtils.isEmpty(fileName)) {
            fileName = FileUtil.getFileNameWithEx(chatMessage.getContent());
        } else {
            fileName = FileUtil.getFileNameWithEx(chatMessage.getFilePath());
        }
        String filePath = fileDirectory + File.separator + fileName;
        FileDownloader.setupOnApplicationOnCreate(MyApplication.getInstance());
        FileDownloader.getImpl().create(chatMessage.getContent())
                .setPath(filePath)
                .setForceReDownload(true)
                .setListener(new FileDownloadListener() {
                    //等待
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    //下载进度回调
                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        //progressDialog.setProgress((soFarBytes * 100 / totalBytes));
                    }

                    //完成下载
                    @Override
                    protected void completed(BaseDownloadTask task) {
                        chatMessage.setFilePath(filePath);
                        ChatMessageDao.getInstance().updateMessageDownloadState(MyApplication.getLoginUserId(), chatMessage.getToUserId(), chatMessage.get_id(), true, filePath);
                        if (chatMessage.getType() == Constants.TYPE_IMAGE) {
                            ImHelper.saveImageSize(chatMessage, filePath);
                        }
                        if (shouldRefreshGallery) {
                            MyApplication.applicationHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    FileUtil.updatePhotoMedia(new File(filePath));
                                    String content = "已保存至" + filePath;
                                    ToastUtil.showLongToast(content);
                                }
                            }, 300);
                        }
                    }

                    //暂停
                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    //下载出错
                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                    }

                    //已存在相同下载
                    @Override
                    protected void warn(BaseDownloadTask task) {
                    }
                }).start();
    }
}
