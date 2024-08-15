package com.iimm.miliao.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.qrcode.utils.CommonUtils;
import com.makeramen.roundedimageview.RoundedDrawable;
import com.iimm.miliao.AppConfig;
import com.iimm.miliao.BuildConfig;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.ShareSdkHelper;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.FileUtil;
import com.iimm.miliao.util.ScreenUtil;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.view.MessageAvatar;

/**
 * 二维码
 */
public class QRcodeDialogView extends Dialog {

    private Friend friend;
    private String website;
    private String number;
    private String title;
    private int screenWidth;
    private String id;
    private TextView mTitleTv;
    private ImageView mImageView;
    private MessageAvatar avatar;
    private MessageAvatar mGAva;
    private TextView scan_it;
    private Button save;
    private Button share;
    CoreManager coreManager;
    private View.OnClickListener mOnClickListener;

    public QRcodeDialogView(Activity activity) {
        super(activity, R.style.BottomDialog);
    }

    public QRcodeDialogView(Activity activity, View.OnClickListener onClickListener) {
        super(activity, R.style.BottomDialog);
        mOnClickListener = onClickListener;
    }

    /**
     * @param activity
     * @param title    群名或昵称
     * @param number   群人数  （显示用户二维码时传值-1）
     * @param website
     * @param id
     * @param friend   群组  （显示用户二维码时传值null）
     */
    public QRcodeDialogView(Activity activity, String title, String number, String website, String id, Friend friend) {
        super(activity, R.style.BottomDialog);
        screenWidth = ScreenUtil.getScreenWidth(activity);
        this.id = id;
        this.title = title;
        this.number = number;
        this.website = website;
        this.friend = friend;
    }

    public QRcodeDialogView(Activity activity, String title, String number, String website, String id, Friend friend, CoreManager coreManager) {
        super(activity, R.style.BottomDialog);
        screenWidth = ScreenUtil.getScreenWidth(activity);
        this.id = id;
        this.coreManager = coreManager;
        this.title = title;
        this.number = number;
        this.website = website;
        this.friend = friend;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initView();
        setView(title, number, website, id, screenWidth, friend);
    }

    protected void initView() {
        this.setCancelable(true);
        this.setCanceledOnTouchOutside(true);
        mTitleTv = (TextView) findViewById(R.id.title);
        mImageView = findViewById(R.id.qrcode);
        share = findViewById(R.id.share);
        save = findViewById(R.id.save);
        mGAva = findViewById(R.id.avatar_imgS);
        avatar = findViewById(R.id.avatar);
        scan_it = findViewById(R.id.scan_it);
        mTitleTv.setText("xxxx");
        share.setOnClickListener(v -> {
            String ss = BuildConfig.BUGLY_APP_CHANNEL + "://www." + BuildConfig.BUGLY_APP_CHANNEL + ".com";
            String url = AppConfig.ROOM_GROUP_QRCODE_SHARE + id + "&userId="
                    + coreManager.getSelf().getUserId()
                    + "&nickName=" + coreManager.getSelf().getNickName() + "&url=" + ss;
            String titles = coreManager.getSelf().getNickName() + "邀请你加入群聊";
            String content = coreManager.getSelf().getNickName() + "邀请你加入群聊" + title + ",进入可查看详情";
            RoundedDrawable drawable = (RoundedDrawable) mGAva.getHeadImage().getDrawable();
            Bitmap bitmap = drawable.getSourceBitmap();
            ShareSdkHelper.shareWechat(getContext(), titles, content, url, AppConfig.wechatAppId, bitmap);
        });
        save.setOnClickListener(v -> {
            String s = FileUtil.saveBitmap(shareBitmap());
            FileUtil.updateGallery(getContext(), s);
            if (!TextUtils.isEmpty(s)) {
                ToastUtil.showLongToast(getContext(), "保存成功！");
            }
        });

    }

    protected View getLayoutId() {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.dialog_qr_code, null, false);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ScreenUtil.dp2px(300), ViewGroup.LayoutParams.WRAP_CONTENT);
        inflate.setLayoutParams(layoutParams);
        return inflate;
    }

    public Bitmap shareBitmap() {
        String ss = BuildConfig.BUGLY_APP_CHANNEL + "://www." + BuildConfig.BUGLY_APP_CHANNEL + ".com";
        Bitmap bitmap = CommonUtils.createQRCode(AppConfig.ROOM_GROUP_QRCODE_SHARE + id + "&userId="
                        + coreManager.getSelf().getUserId()
                        + "&nickName=" + coreManager.getSelf().getNickName() + "&url=" + ss,

                (int) (screenWidth * 0.6), (int) (screenWidth * 0.6));
        RoundedDrawable drawable = (RoundedDrawable) mGAva.getHeadImage().getDrawable();
        Bitmap bitmap1 = drawable.getSourceBitmap();
        Bitmap logo = CommonUtils.addLogo(bitmap, bitmap1);
        return logo;

    }

    private void setView(String title, String number, String website, String id, int screenWidth, Friend friend) {
        if (!TextUtils.isEmpty(title) && !TextUtils.equals(number, "-1")) {
            mTitleTv.setText(title + "(" + number + ")");
        } else if (!TextUtils.isEmpty(title)) {
            mTitleTv.setText(title);
        }

        String action, str;
        if (friend != null) {
            action = "group";
            mGAva.fillData(friend);
            avatar.fillData(friend);
            scan_it.setText("扫一扫上面的二维码图案进群");
        } else {
            action = "user";
            scan_it.setText("扫一扫上面的二维码图案加为好友");
            AvatarHelper.getInstance().displayAvatar(id, mGAva);
            AvatarHelper.getInstance().displayAvatar(id, avatar);
        }
        str = website + "?action=" + action + "&tigId=" + id;
        // 生成二维码
        Bitmap bitmap = CommonUtils.createQRCode(str, (int) (screenWidth * 0.6), (int) (screenWidth * 0.6));

        mImageView.setImageBitmap(bitmap);
        mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (coreManager != null) {
            if (coreManager.getConfig().wechatLoginStatus==1) {
                save.setVisibility(View.VISIBLE);
                share.setVisibility(View.VISIBLE);
            } else {
                save.setVisibility(View.GONE);
                share.setVisibility(View.GONE);
            }
        } else {
            save.setVisibility(View.GONE);
            share.setVisibility(View.GONE);
        }
    }


    public void setSureClick(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public void setTitle(String title) {
        mTitleTv.setText(title);
    }

}

