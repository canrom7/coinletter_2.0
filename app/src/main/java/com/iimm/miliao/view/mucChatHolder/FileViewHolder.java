package com.iimm.miliao.view.mucChatHolder;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.helper.UploadEngine;
import com.iimm.miliao.ui.mucfile.DownManager;
import com.iimm.miliao.ui.mucfile.MucFileDetails;
import com.iimm.miliao.ui.mucfile.XfileUtils;
import com.iimm.miliao.ui.mucfile.bean.MucFileBean;
import com.iimm.miliao.util.FileUtil;
import com.iimm.miliao.view.FileProgressPar;
import com.iimm.miliao.view.SelectionFrame;

class FileViewHolder extends AChatHolderInterface {

    private String TAG = "FileViewHolder";
    ImageView ivCardImage;
    TextView tvPersonName;
    TextView tvFileSize;
    FileProgressPar progressPar;
    ImageView ivUploadCancel;

    public FileViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_file : R.layout.chat_to_item_file;
    }

    @Override
    public void initView(View view) {
        ivCardImage = view.findViewById(R.id.iv_card_head);
        tvPersonName = view.findViewById(R.id.person_name);
        tvFileSize = view.findViewById(R.id.person_sex);
        progressPar = view.findViewById(R.id.chat_card_light);
        ivUploadCancel = view.findViewById(R.id.chat_upload_cancel_iv);
        mRootView = view.findViewById(R.id.chat_warp_view);
        progressPar.setVisibility(View.GONE);
    }

    @Override
    public void fillData(ChatMessage message) {
        String filePath = FileUtil.isExist(message.getFilePath()) ? message.getFilePath() : message.getContent();
        // 设置图标
        if (message.getTimeLen() > 0) { // 有文件类型
            if (message.getTimeLen() == 1) {
                Glide.with(mContext).load(filePath).override(100, 100).into(ivCardImage);
            } else {
                XfileUtils.setFileInco(message.getTimeLen(), ivCardImage);
            }
        } else {// 没有文件类型，取出后缀
            int pointIndex = filePath.lastIndexOf(".");
            if (pointIndex != -1) {
                String type = filePath.substring(pointIndex + 1).toLowerCase();
                if (type.equals("png") || type.equals("jpg") || type.equals("gif")) {
                    Glide.with(mContext).load(filePath).override(100, 100).into(ivCardImage);
                    message.setTimeLen(1);
                } else {
                    fillFileInco(type, ivCardImage, message);
                }
            }
        }

        // 设置文件名称
        String fileName = TextUtils.isEmpty(message.getFilePath()) ? message.getContent() : message.getFilePath();
        int start = fileName.lastIndexOf("/");
        String name = fileName.substring(start + 1).toLowerCase();
        tvPersonName.setText(name);
        tvFileSize.setText("文件 " + XfileUtils.fromatSize(message.getFileSize()));
        message.setObjectId(name);
        // 设置进度条显示 不是我发的，或者进度到了100，或者上传了，都不显示 ——zq
        boolean hide = !isMysend || message.getUploadSchedule() == 100 || message.isUpload();
        if (isMysend) {
            // 没有上传或者 进度小于100
            boolean show = !message.isUpload() && message.getUploadSchedule() < 100;
            if (show) {
                if (ivUploadCancel != null) {
                    ivUploadCancel.setVisibility(View.VISIBLE);
                    progressPar.setVisibility(View.VISIBLE);
                    progressPar.visibleMode(!hide);

                }
            } else {
                if (ivUploadCancel != null) {
                    ivUploadCancel.setVisibility(View.GONE);
                    progressPar.setVisibility(View.GONE);
                }
            }
        }
        progressPar.update(message.getUploadSchedule());
        mSendingBar.setVisibility(View.GONE);

        if (ivUploadCancel != null) {
            ivUploadCancel.setOnClickListener(v -> {
                SelectionFrame selectionFrame = new SelectionFrame(mContext);
                selectionFrame.setSomething(getString(R.string.cancel_upload), getString(R.string.sure_cancel_upload), new SelectionFrame.OnSelectionFrameClickListener() {
                    @Override
                    public void cancelClick() {
                        progressPar.setVisibility(View.GONE);
                    }

                    @Override
                    public void confirmClick() {
                        // 用户可能在弹窗弹起后停留很久，所以点击确认的时候还需要判断一下
                        if (!mdata.isUpload()) {
                            UploadEngine.cancel(mdata.getPacketId());
                        }
                    }
                });
                selectionFrame.show();
            });
        }
    }

    @Override
    protected void onRootClick(View v) {
        sendReadMessage(mdata);
        ivUnRead.setVisibility(View.GONE);
        setRead();

        MucFileBean data = new MucFileBean();
        String url = mdata.getContent();
        String filePath = mdata.getFilePath();
        if (TextUtils.isEmpty(filePath)) {
            filePath = url;
        }

        int size = mdata.getFileSize();
        // 取出文件名称
        int start = filePath.lastIndexOf("/");
        String name = filePath.substring(start + 1).toLowerCase();
        data.setNickname(name);
        data.setUrl(url);
        data.setName(name);
        data.setSize(size);
        data.setState(DownManager.STATE_UNDOWNLOAD);
        data.setType(mdata.getTimeLen());
        Intent intent = new Intent(mContext, MucFileDetails.class);
        intent.putExtra("data", data);
        mContext.startActivity(intent);
    }

    private void fillFileInco(String type, ImageView v, ChatMessage chat) {
        if (type.equals("mp3")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_y);
            chat.setTimeLen(2);
        } else if (type.equals("mp4") || type.equals("avi")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_v);
            chat.setTimeLen(3);
        } else if (type.equals("xls")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_x);
            chat.setTimeLen(5);
        } else if (type.equals("doc")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_w);
            chat.setTimeLen(6);
        } else if (type.equals("ppt")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_p);
            chat.setTimeLen(4);
        } else if (type.equals("pdf")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_f);
            chat.setTimeLen(10);
        } else if (type.equals("apk")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_a);
            chat.setTimeLen(11);
        } else if (type.equals("txt")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_t);
            chat.setTimeLen(8);
        } else if (type.equals("rar") || type.equals("zip")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_z);
            chat.setTimeLen(7);
        } else {
            v.setImageResource(R.drawable.ic_muc_flie_type_what);
            chat.setTimeLen(9);
        }
    }

    @Override
    public boolean enableUnRead() {
        return true;
    }
}
