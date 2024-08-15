package com.iimm.miliao.ui.trill;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Area;
import com.iimm.miliao.bean.UploadFileResult;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.helper.UploadService;
import com.iimm.miliao.ui.account.LoginHistoryActivity;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.circle.range.AtSeeCircleActivity;
import com.iimm.miliao.ui.circle.range.SeeCircleActivity;
import com.iimm.miliao.ui.map.MapPickerActivity;
import com.iimm.miliao.util.BitmapUtil;
import com.iimm.miliao.util.DeviceInfoUtil;
import com.iimm.miliao.util.DisplayUtil;
import com.iimm.miliao.util.RecorderUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.view.SelectionFrame;
import com.iimm.miliao.view.TipDialog;
import com.iimm.miliao.volley.Result;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 发布视频
 */
public class ReleasexActivity extends BaseActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_SELECT_LOCATE = 3;  // 位置
    private static final int REQUEST_CODE_SELECT_TYPE = 4;    // 谁可以看
    private static final int REQUEST_CODE_SELECT_REMIND = 5;  // 提醒谁看
    private static final int REQUEST_CODE_SELECT_TAG = 6; // 选择标签
    private EditText mTextEdit;
    // 所在位置
    private TextView mTVLocation;
    // 谁可以看
    private TextView mTVSee;
    // 提醒谁看
    private TextView mTVAt;
    // Video Item
    //    private FrameLayout mFloatLayout;
    private ImageView mImageView;
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case RecorderUtils.THUMB_SUCCESS: // 获取完成
                    String out = MyApplication.getInstance().mPicturesDir + "temp.jpg";
                    AvatarHelper.getInstance().displayUrl(out, mImageView);
                    break;
                case RecorderUtils.THUMB_FAILURE: // 压缩失败

                    break;
                default:
                    break;
            }

            return false;
        }
    });
    // 发布
    //private Button mReleaseBtn;
    // data
    private String mVideoFilePath;
    private long mTimeLen;
    private SelectionFrame mSelectionFrame;
    // 部分可见 || 不给谁看 有值 用于恢复谁可以看的界面
    private String str1;
    private String str2;
    private String str3;
    // 默认为公开
    private int visible = 1;
    // 谁可以看 || 不给谁看
    private String lookPeople;
    // 提醒谁看
    private String atlookPeople;
    // 默认不发位置
    private double latitude;
    private double longitude;
    private String address;
    private String mVideoData;
    private String mImageData;
    private String mThumbPath;
    private int mCurrTag = 8;
    private TextView mTvTag;
    private String mCurrBgmId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_video);
        initActionBar();
        initView();
        initEvent();
    }


    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isExitNoPublish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(InternationalizationHelper.getString("JX_SendVideo"));
        TextView tvTitleRight = findViewById(R.id.tv_title_right);
        DisplayUtil.setRightOkStyle(this, tvTitleRight, R.string.publication);
        tvTitleRight.setOnClickListener(v -> {
            Log.e(TAG, "onClick: path:" + mVideoFilePath + "  time :" + mTimeLen);
            if (TextUtils.isEmpty(mVideoFilePath) || mTimeLen <= 0) {
                Toast.makeText(ReleasexActivity.this, InternationalizationHelper.getString("JX_AddFile"), Toast.LENGTH_SHORT).show();
                return;
            }
            new UploadTask().execute();
        });
    }

    private void initView() {
        mTextEdit = (EditText) findViewById(R.id.text_edit);
        mTextEdit.setHint(InternationalizationHelper.getString("addMsgVC_Mind"));
        // 所在位置
        mTVLocation = (TextView) findViewById(R.id.tv_location);
        // 谁可以看
        mTVSee = (TextView) findViewById(R.id.tv_see);
        // 提醒谁看
        mTVAt = (TextView) findViewById(R.id.tv_at);

        mImageView = (ImageView) findViewById(R.id.image_view);

//        mReleaseBtn = (Button) findViewById(R.id.release_btn);
//        mReleaseBtn.setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());
//        mReleaseBtn.setText(InternationalizationHelper.getString("JX_Publish"));

        mVideoFilePath = getIntent().getStringExtra("video_path");
        mCurrBgmId = getIntent().getStringExtra("music_id");
        mThumbPath = getIntent().getStringExtra("video_thumb");
        mTimeLen = getIntent().getLongExtra("video_length", 0);

        Log.e(TAG, "release: " + mVideoFilePath + " timelen " + mTimeLen + "  thumb :" + mThumbPath);

        if (!TextUtils.isEmpty(mThumbPath)) {
            AvatarHelper.getInstance().displayUrl(mThumbPath, mImageView);
        } else {
            Bitmap mThumbBmp = AvatarHelper.getInstance().displayVideoThumb(mVideoFilePath, mImageView);
            mThumbPath = RecorderUtils.getRecorderPath() + "VID_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
            // 保存视频缩略图至sd卡
            if (!BitmapUtil.saveBitmapToSDCard(mThumbBmp, mThumbPath)) {// 保存缩略图失败
                Log.e(TAG, "initView: 保存缩略图失败");
                mThumbPath = "";
            }
        }

        if (mTimeLen <= 0 && !TextUtils.isEmpty(mVideoFilePath)) {
            mTimeLen = getScreenRecordFileTimeLen(mVideoFilePath);
        }


        RelativeLayout layout = findViewById(R.id.rl_lable);
        mTvTag = layout.findViewById(R.id.tv_lable);
        layout.setOnClickListener(this);
        layout.setVisibility(View.VISIBLE);
    }

    private long getScreenRecordFileTimeLen(String srf) {
        long duration;
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(srf);
            player.prepare();
            duration = player.getDuration();
        } catch (Exception e) {
            duration = 1000;
            e.printStackTrace();
        }
        player.release();
        return duration;
    }

    private void initEvent() {
        findViewById(R.id.rl_location).setOnClickListener(this);
        findViewById(R.id.rl_see).setOnClickListener(this);
        findViewById(R.id.rl_at).setOnClickListener(this);
//        mReleaseBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_location:
                // 所在位置
                Intent intent1 = new Intent(this, MapPickerActivity.class);
                startActivityForResult(intent1, REQUEST_CODE_SELECT_LOCATE);
                break;
            case R.id.rl_see:
                // 谁可以看
                Intent intent2 = new Intent(this, SeeCircleActivity.class);
                intent2.putExtra("THIS_CIRCLE_TYPE", visible - 1);
                intent2.putExtra("THIS_CIRCLE_PERSON_RECOVER1", str1);
                intent2.putExtra("THIS_CIRCLE_PERSON_RECOVER2", str2);
                intent2.putExtra("THIS_CIRCLE_PERSON_RECOVER3", str3);
                startActivityForResult(intent2, REQUEST_CODE_SELECT_TYPE);
                break;
            case R.id.rl_at:
                // 提醒谁看
                if (visible == 2) {
//                    final TipDialog tipDialog = new TipDialog(this);
//                    tipDialog.setmConfirmOnClickListener(getString(R.string.tip_private_cannot_use_this), new TipDialog.ConfirmOnClickListener() {
//                        @Override
//                        public void confirm() {
//                            tipDialog.dismiss();
//                        }
//                    });
//                    tipDialog.show();
                    ToastUtil.showToast(ReleasexActivity.this, R.string.tip_private_cannot_use_this);
                } else {
                    Intent intent3 = new Intent(this, AtSeeCircleActivity.class);
                    intent3.putExtra("REMIND_TYPE", visible);
                    intent3.putExtra("REMIND_PERSON", lookPeople);
                    startActivityForResult(intent3, REQUEST_CODE_SELECT_REMIND);
                }

                break;

//            case R.id.release_btn: // 去发布
//                Log.e(TAG, "onClick: path:" + mVideoFilePath + "  time :" + mTimeLen);
//                if (TextUtils.isEmpty(mVideoFilePath) || mTimeLen <= 0) {
//                    Toast.makeText(ReleasexActivity.this, InternationalizationHelper.getString("JX_AddFile"), Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                new UploadTask().execute();
//                break;
            case R.id.rl_lable: // 选择标签
                Intent intent3 = new Intent(this, TagPickerActivity.class);
                intent3.putExtra("THIS_CIRCLE_LABLE", mCurrTag);
                startActivityForResult(intent3, REQUEST_CODE_SELECT_TAG);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        isExitNoPublish();
    }

    private void isExitNoPublish() {
        if (!TextUtils.isEmpty(mVideoFilePath)) {
            mSelectionFrame = new SelectionFrame(ReleasexActivity.this);
            mSelectionFrame.setSomething(getString(R.string.app_name), getString(R.string.tip_has_video_no_public), new SelectionFrame.OnSelectionFrameClickListener() {
                @Override
                public void cancelClick() {

                }

                @Override
                public void confirmClick() {
                    finish();
                }
            });
            mSelectionFrame.show();
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_LOCATE) {
            // 选择位置返回
            latitude = data.getDoubleExtra(AppConstant.EXTRA_LATITUDE, 0);
            longitude = data.getDoubleExtra(AppConstant.EXTRA_LONGITUDE, 0);
            address = data.getStringExtra(AppConstant.EXTRA_ADDRESS);
            if (latitude != 0 && longitude != 0 && !TextUtils.isEmpty(address)) {
                Log.e("zq", "纬度:" + latitude + "   经度：" + longitude + "   位置：" + address);
                mTVLocation.setText(address);
            } else {
                ToastUtil.showToast(mContext, InternationalizationHelper.getString("JXLoc_StartLocNotice"));
            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_TYPE) {
            // 谁可以看返回
            visible = data.getIntExtra("THIS_CIRCLE_TYPE", 1);
            if (visible == 1) {
                mTVSee.setText(getString(R.string.publics));
            } else if (visible == 2) {
                mTVSee.setText(getString(R.string.privates));
                if (!TextUtils.isEmpty(atlookPeople)) {
                    final TipDialog tipDialog = new TipDialog(this);
                    tipDialog.setmConfirmOnClickListener(getString(R.string.tip_private_cannot_notify), new TipDialog.ConfirmOnClickListener() {
                        @Override
                        public void confirm() {
                            tipDialog.dismiss();
                            // 清空提醒谁看列表
                            atlookPeople = "";
                            mTVAt.setText("");
                        }
                    });
                    tipDialog.show();
                }
            } else if (visible == 3) {
                lookPeople = data.getStringExtra("THIS_CIRCLE_PERSON");
                String looKenName = data.getStringExtra("THIS_CIRCLE_PERSON_NAME");
                mTVSee.setText(looKenName);
            } else if (visible == 4) {
                lookPeople = data.getStringExtra("THIS_CIRCLE_PERSON");
                String lookName = data.getStringExtra("THIS_CIRCLE_PERSON_NAME");
                mTVSee.setText("除去 " + lookName);
            }
            str1 = data.getStringExtra("THIS_CIRCLE_PERSON_RECOVER1");
            str2 = data.getStringExtra("THIS_CIRCLE_PERSON_RECOVER2");
            str3 = data.getStringExtra("THIS_CIRCLE_PERSON_RECOVER3");
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_TAG) {
            // 选择标签
            mCurrTag = data.getIntExtra("THIS_CIRCLE_LABLE", 8);
            String text = tagToName(mCurrTag);
            mTvTag.setText(text);
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_REMIND) {
            // 提醒谁看返回
            atlookPeople = data.getStringExtra("THIS_CIRCLE_REMIND_PERSON");
            String atLookPeopleName = data.getStringExtra("THIS_CIRCLE_REMIND_PERSON_NAME");
            mTVAt.setText(atLookPeopleName);
        }
    }

    private String tagToName(int tag) {
        switch (tag) {
            case 1:
                return "美食";
            case 2:
                return "景点";
            case 3:
                return "文化";
            case 4:
                return "玩乐";
            case 5:
                return "酒店";
            case 6:
                return "购物";
            case 7:
                return "运动";
            case 8:
                return "其他";
        }
        return "其他";
    }

    public void sendAudio() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        // 消息类型：1=文字消息；2=图文消息；3=语音消息；4=视频消息；
        params.put("type", "4");
        // 消息标记：1：求职消息；2：招聘消息；3：普通消息；
        params.put("flag", "3");
        // 消息隐私范围：1=公开；2=私密；3=部分选中好友可见；4=不给谁看
        params.put("visible", String.valueOf(visible));
        if (visible == 3) {
            // 谁可以看
            params.put("userLook", lookPeople);
        } else if (visible == 4) {
            // 不给谁看
            params.put("userNotLook", lookPeople);
        }
        // 提醒谁看
        if (!TextUtils.isEmpty(atlookPeople)) {
            params.put("userRemindLook", atlookPeople);
        }

        // 消息内容
        params.put("text", mTextEdit.getText().toString());
        params.put("videos", mVideoData);
        if (!TextUtils.isEmpty(mImageData) && !mImageData.equals("{}") && !mImageData.equals("[{}]")) {
            params.put("images", mImageData);
        }

        /**
         * 所在位置
         */
        if (!TextUtils.isEmpty(address)) {
            // 纬度
            params.put("latitude", String.valueOf(latitude));
            // 经度
            params.put("longitude", String.valueOf(longitude));
            // 位置
            params.put("location", address);
        }
        if (!TextUtils.isEmpty(mCurrBgmId)) {
            // 使用的
            params.put("musicId", mCurrBgmId);
        }

        // 必传，之前删除该字段，发布说说，服务器返回接口内部异常
        Area area = Area.getDefaultCity();
        if (area != null) {
            params.put("cityId", String.valueOf(area.getId()));// 城市Id
        } else {
            params.put("cityId", "0");
        }

        /**
         * 附加信息
         */
        // 手机型号
        params.put("model", DeviceInfoUtil.getModel());
        // 手机操作系统版本号
        params.put("osVersion", DeviceInfoUtil.getOsVersion());
        if (!TextUtils.isEmpty(DeviceInfoUtil.getDeviceId(mContext))) {
            // 设备序列号
            params.put("serialNumber", DeviceInfoUtil.getDeviceId(mContext));
        }

        params.put("lable", Integer.toString(mCurrTag));
        DialogHelper.showDefaulteMessageProgressDialog(ReleasexActivity.this);

        HttpUtils.get().url(coreManager.getConfig().MSG_ADD_URL)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        compRelease(result.getData());
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(ReleasexActivity.this);
                    }
                });
    }

    public void compRelease(String data) {
        Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show();
        DialogHelper.dismissProgressDialog();
        Intent intent = new Intent();
        intent.putExtra(AppConstant.EXTRA_MSG_ID, data);
        setResult(RESULT_OK, intent);
        MsgBroadcast.broadcastMsgColseTrill(this);
        finish();
    }

    private class UploadTask extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DialogHelper.showDefaulteMessageProgressDialog(ReleasexActivity.this);
        }


        /**
         * 上传的结果： <br/>
         * return 1 Token过期，请重新登陆 <br/>
         * return 2 视频为空，请重新录制 <br/>
         * return 3 上传出错<br/>
         * return 4 上传成功<br/>
         */
        @Override
        protected Integer doInBackground(Void... params) {
            if (!LoginHelper.isTokenValidation()) {
                return 1;
            }
            if (TextUtils.isEmpty(mVideoFilePath)) {
                return 2;
            }


            Map<String, String> mapParams = new HashMap<String, String>();
            mapParams.put("access_token", coreManager.getSelfStatus().accessToken);
            mapParams.put("userId", coreManager.getSelf().getUserId() + "");
            mapParams.put("validTime", "-1");// 文件有效期

            List<String> dataList = new ArrayList<String>();
            dataList.add(mVideoFilePath);
            if (!TextUtils.isEmpty(mThumbPath)) {
                dataList.add(mThumbPath);
            }
            String result = new UploadService().uploadFile(ReleasexActivity.this, coreManager, mapParams, dataList);
            if (TextUtils.isEmpty(result)) {
                return 3;
            }

            UploadFileResult recordResult = JSON.parseObject(result, UploadFileResult.class);
            boolean success = Result.defaultParser(ReleasexActivity.this, recordResult, true);
            if (success) {
                if (recordResult.getSuccess() != recordResult.getTotal()) {// 上传丢失了某些文件
                    return 3;
                }
                if (recordResult.getData() != null) {
                    UploadFileResult.Data data = recordResult.getData();
                    if (data.getVideos() != null && data.getVideos().size() > 0) {
                        while (data.getVideos().size() > 1) {// 因为正确情况下只有一个视频，所以要保证只有一个视频
                            data.getVideos().remove(data.getVideos().size() - 1);
                        }
                        data.getVideos().get(0).setSize(new File(mVideoFilePath).length());
                        data.getVideos().get(0).setLength(mTimeLen);
                        mVideoData = JSON.toJSONString(data.getVideos(), UploadFileResult.sAudioVideosFilter);
                    } else {
                        return 3;
                    }
                    if (data.getImages() != null && data.getImages().size() > 0) {
                        mImageData = JSON.toJSONString(data.getImages(), UploadFileResult.sImagesFilter);
                    }
                    return 4;
                } else {// 没有文件数据源，失败
                    return 3;
                }
            } else {
                return 3;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == 1) {
                DialogHelper.dismissProgressDialog();
                startActivity(new Intent(ReleasexActivity.this, LoginHistoryActivity.class));
            } else if (result == 2) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(ReleasexActivity.this, InternationalizationHelper.getString("JXAlert_NotHaveFile"));
            } else if (result == 3) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(ReleasexActivity.this, R.string.upload_failed);
            } else {
                sendAudio();
            }
        }
    }
}
