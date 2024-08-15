package com.iimm.miliao.ui.account;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.adapter.MessageLogin;
import com.iimm.miliao.bean.Area;
import com.iimm.miliao.bean.LoginRegisterResult;
import com.iimm.miliao.bean.QuestionsBean;
import com.iimm.miliao.bean.SecurityQuestion;
import com.iimm.miliao.bean.User;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.AreasDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.map.MapHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.dialog.HeadaSelectorDialog;
import com.iimm.miliao.ui.me.SecurityQuestionActivity;
import com.iimm.miliao.ui.tool.SelectAreaActivity;
import com.iimm.miliao.util.CameraUtil;
import com.iimm.miliao.util.DateSelectHelper;
import com.iimm.miliao.util.DeviceInfoUtil;
import com.iimm.miliao.util.EventBusHelper;
import com.iimm.miliao.util.LogUtils;
import com.iimm.miliao.util.OBSUtils;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.StringUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.util.permission.AndPermissionUtils;
import com.iimm.miliao.util.permission.OnPermissionClickListener;
import com.iimm.miliao.util.permission.PermissionDialog;
import com.iimm.miliao.view.GetPictureCommonDialog;
import com.iimm.miliao.view.TipDialog;
import com.iimm.miliao.volley.Result;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import cz.msebera.android.httpclient.Header;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 注册-3.基本资料
 */
public class RegisterUserBasicInfoActivity extends BaseActivity implements View.OnClickListener, OnPermissionClickListener {

    private static final int REQUEST_CODE_CAPTURE_CROP_PHOTO = 1;
    private static final int REQUEST_CODE_PICK_CROP_PHOTO = 2;
    private static final int REQUEST_CODE_CROP_PHOTO = 3;
    private RoundedImageView mAvatarImg;
    private EditText mNameEdit;
    private TextView mSexTv;
    private TextView mBirthdayTv;
    private TextView mCityTv;
    private TextView mSecureSettingTv;
    private Button mNextStepBtn;
    private TextView nickNameTv, sexTv, birthdayTv, cityTv;
    /* 前面页面传递进来的四个参数，都是必填 */
    private String mobilePrefix;
    private String mPhoneNum;
    private String mPassword;
    // 可能empty但不会null,
    private String mInviteCode;
    // Temp
    private User mTempData;
    // 选择头像的数据
    private File mCurrentFile;
    private boolean isSelectAvatar;
    private EditText mInviteCodeEdit;
    private LinearLayout mLlMavatarImg;
    private TextView mGirlTv;
    private Uri mPictureContentOrFileUri;//拍照 或 选择 图片 时，图片的路径，如果是SDK >= 24 则为ContentUri,否则是FileUri
    private Uri mPictureCropFileUri;//图片裁剪后的路径
    private List<QuestionsBean> mData = new ArrayList<>();
    private int type;
    private String code;
    private boolean isOpen;

    public RegisterUserBasicInfoActivity() {
        noLoginRequired();
    }

    public static void start(
            Context ctx,
            String mobilePrefix,
            String phoneStr,
            String password,
            int type,
            String code
    ) {
        Intent intent = new Intent(ctx, RegisterUserBasicInfoActivity.class);
        intent.putExtra(RegisterActivity.EXTRA_AUTH_CODE, mobilePrefix);
        intent.putExtra(RegisterActivity.EXTRA_PHONE_NUMBER, phoneStr);
        intent.putExtra(RegisterActivity.EXTRA_PASSWORD, password);
        intent.putExtra(RegisterActivity.EXTRA_TYPE, type);
        intent.putExtra(RegisterActivity.EXTRA_CODE, code);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user_basic_info);
        OBSUtils.init(this, coreManager, new Handler());
        if (getIntent() != null) {
            mobilePrefix = getIntent().getStringExtra(RegisterActivity.EXTRA_AUTH_CODE);
            mPhoneNum = getIntent().getStringExtra(RegisterActivity.EXTRA_PHONE_NUMBER);
            type = getIntent().getIntExtra(RegisterActivity.EXTRA_TYPE, 0);
            mPassword = getIntent().getStringExtra(RegisterActivity.EXTRA_PASSWORD);
            code = getIntent().getStringExtra(RegisterActivity.EXTRA_CODE);
        }
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doBack();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(InternationalizationHelper.getString("JX_BaseInfo"));
        initView();
        if (!coreManager.getConfig().disableLocationServer) {
            requestLocationCity();
        }
        EventBusHelper.register(this);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageLogin message) {
        finish();
    }

    private void initView() {
        mLlMavatarImg = findViewById(R.id.ll_avatar_img);
        mAvatarImg = findViewById(R.id.avatar_img);
        mNameEdit = (EditText) findViewById(R.id.name_edit);
        mSexTv = (TextView) findViewById(R.id.sex_tv);
        mGirlTv = findViewById(R.id.girl_tv);
        mBirthdayTv = (TextView) findViewById(R.id.birthday_tv);
        mCityTv = (TextView) findViewById(R.id.city_tv);
        mSecureSettingTv = findViewById(R.id.secure_setting_tv);
        mSecureSettingTv.setOnClickListener(this);
        mNextStepBtn = (Button) findViewById(R.id.next_step_btn);
        nickNameTv = (TextView) findViewById(R.id.name_text);
        sexTv = (TextView) findViewById(R.id.sex_text);
        birthdayTv = (TextView) findViewById(R.id.birthday_text);
        cityTv = (TextView) findViewById(R.id.city_text);
        mCityTv.setText("");
        nickNameTv.setText(InternationalizationHelper.getString("JX_NickName"));
        sexTv.setText(InternationalizationHelper.getString("JX_Sex"));
        birthdayTv.setText(InternationalizationHelper.getString("JX_BirthDay"));
        cityTv.setText(InternationalizationHelper.getString("JX_Address"));
        mNameEdit.setHint(InternationalizationHelper.getString("JX_InputName"));
        mNextStepBtn.setText(InternationalizationHelper.getString("JX_Confirm"));
        mInviteCodeEdit = (EditText) findViewById(R.id.etInvitationCode);
        //是否需要设置密保问题
        if (coreManager.getConfig().isQestionOpen) {
            findViewById(R.id.secure_setting_select_rl).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.secure_setting_select_rl).setVisibility(View.GONE);
        }
        mAvatarImg.setOnClickListener(this);
        mLlMavatarImg.setOnClickListener(this);
        findViewById(R.id.birthday_select_rl).setOnClickListener(this);
//        if (coreManager.getConfig().disableLocationServer) {
//            findViewById(R.id.city_select_rl).setVisibility(View.GONE);
//        } else {
//            findViewById(R.id.city_select_rl).setVisibility(View.VISIBLE);
//            findViewById(R.id.city_select_rl).setOnClickListener(this);
//        }
        findViewById(R.id.city_select_rl).setVisibility(View.GONE);
        mNextStepBtn.setOnClickListener(this);
        mSexTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTempData.setSex(1);
                mSexTv.setBackgroundResource(R.drawable.shape_info_left);
                mSexTv.setTextColor(ContextCompat.getColor(RegisterUserBasicInfoActivity.this, R.color.white));
                mGirlTv.setBackgroundResource(R.color.transparent);
                mGirlTv.setTextColor(ContextCompat.getColor(RegisterUserBasicInfoActivity.this, R.color.color_96));
            }
        });
        mGirlTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTempData.setSex(0);
                mGirlTv.setBackgroundResource(R.drawable.shape_info_right);
                mGirlTv.setTextColor(ContextCompat.getColor(RegisterUserBasicInfoActivity.this, R.color.white));
                mSexTv.setBackgroundResource(R.color.transparent);
                mSexTv.setTextColor(ContextCompat.getColor(RegisterUserBasicInfoActivity.this, R.color.color_96));
            }
        });
        mNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                logicInfo();
            }
        });
        updateUI();
        updateSecureSettingUI();
    }

    private void updateUI() {
        if (mTempData == null) {
            mTempData = new User();
            mTempData.setSex(1);
            mTempData.setBirthday(TimeUtils.time_current_time());
        }
        if (!TextUtils.isEmpty(mTempData.getNickName())) {
            mNameEdit.setText(mTempData.getNickName());
        }
        if (mTempData.getSex() == 1) {
            mSexTv.setBackgroundResource(R.drawable.shape_info_left);
            mSexTv.setTextColor(ContextCompat.getColor(this, R.color.white));
            mGirlTv.setBackgroundResource(R.color.transparent);
            mGirlTv.setTextColor(ContextCompat.getColor(this, R.color.color_96));
        } else {
            mSexTv.setBackgroundResource(R.color.transparent);
            mSexTv.setTextColor(ContextCompat.getColor(this, R.color.color_96));
            mGirlTv.setBackgroundResource(R.drawable.shape_info_right);
            mGirlTv.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
        mBirthdayTv.setText(TimeUtils.time_s_long_2_str(mTempData.getBirthday()));
        Class drawable = R.drawable.class;
        Field field = null;
        try {
            Random r = new Random(1);
            String name = "head" + r.nextInt(16);
            field = drawable.getField(name);
            int res_ID = field.getInt(field.getName());
            mCurrentFile = drawableToFile(RegisterUserBasicInfoActivity.this, res_ID, name);
            Glide.with(RegisterUserBasicInfoActivity.this)
                    .load(mCurrentFile)
                    .into(mAvatarImg);
            isSelectAvatar = true;
        } catch (Exception e) {

        }
    }

    public File drawableToFile(Context mContext, int drawableId, String fileName) {
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), drawableId);


        String defaultPath = mContext.getFilesDir()
                .getAbsolutePath() + "/defaultGoodInfo";
        File file = new File(defaultPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String defaultImgPath = defaultPath + "/" + fileName + ".png";
        file = new File(defaultImgPath);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();

            FileOutputStream fOut = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.PNG, 20, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        switch (v.getId()) {
            case R.id.secure_setting_tv://设置密保问题
                SecurityQuestionActivity.securityQuestion(RegisterUserBasicInfoActivity.this, 1, mData);
                break;
            case R.id.ll_avatar_img:
                AndPermissionUtils.Authorization(this, this);
                break;
            case R.id.avatar_img:
                AndPermissionUtils.Authorization(this, this);
                break;
            case R.id.birthday_select_rl:
                showSelectBirthdayDialog();
                break;
            case R.id.city_select_rl:
                Intent intent = new Intent(RegisterUserBasicInfoActivity.this, SelectAreaActivity.class);
                intent.putExtra(SelectAreaActivity.EXTRA_AREA_PARENT_ID, Area.AREA_DATA_CHINA_ID);// 直接为中国
                intent.putExtra(SelectAreaActivity.EXTRA_AREA_TYPE, Area.AREA_TYPE_PROVINCE);
                intent.putExtra(SelectAreaActivity.EXTRA_AREA_DEEP, Area.AREA_TYPE_CITY);// 选择的深度为城市级别
                startActivityForResult(intent, 4);
                break;
            case R.id.next_step_btn:
                register();
                break;
        }
    }

    private void showSelectAvatarDialog() {
        if (isOpen) {
            return;
        }
        isOpen = true;
        HeadaSelectorDialog dialog = new HeadaSelectorDialog(this);
        dialog.setHeadSelectorListener(new HeadaSelectorDialog.HeadSelectorListener() {
            @Override
            public void onSureClickListener(File file) {
                mCurrentFile = file;
                isSelectAvatar = true;
                Glide.with(RegisterUserBasicInfoActivity.this)
                        .load(file)
                        .into(mAvatarImg);
                dialog.dismiss();
            }

            @Override
            public void onPohto() {

                new GetPictureCommonDialog(RegisterUserBasicInfoActivity.this, new GetPictureCommonDialog.GetPictureCommonDialogListener() {
                    @Override
                    public void onClickTakingPhotos(TextView tvTakingPhotos) {
                        takePhoto();

                    }

                    @Override
                    public void onClickChoosePicture(TextView tvChoosePicture) {
                        selectPhoto();

                    }

                    @Override
                    public void onClickCancel(TextView tvCancel) {

                    }
                }).show();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isOpen = false;
            }
        });
        dialog.show();

    }

    private void takePhoto() {
        String fileDirStr = MyApplication.getInstance().mPicturesDir;
        String filePath = MyApplication.getInstance().mPicturesDir + File.separator + UUID.randomUUID().toString() + ".jpg";

        File fileDir = new File(fileDirStr);
        if (!fileDir.mkdirs()) {
        }
        File file = new File(filePath);
        mPictureContentOrFileUri = CameraUtil.getOutputMediaContentUri(this, file);
        CameraUtil.captureImage(this, mPictureContentOrFileUri, REQUEST_CODE_CAPTURE_CROP_PHOTO);
    }

    private void selectPhoto() {
        CameraUtil.pickImageSimple(this, REQUEST_CODE_PICK_CROP_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CAPTURE_CROP_PHOTO) {
            // 拍照返回再去裁减
            if (resultCode == Activity.RESULT_OK) {
                if (mPictureContentOrFileUri != null) {
                    mPictureCropFileUri = CameraUtil.getOutputMediaFileUri(this, CameraUtil.MEDIA_TYPE_IMAGE);
                    CameraUtil.cropImageAfterTakePicture(this, mPictureContentOrFileUri, mPictureCropFileUri, REQUEST_CODE_CROP_PHOTO, 1, 1, 300, 300);
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_PICK_CROP_PHOTO) {
            // 选择一张图片,然后立即调用裁减
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    Uri o = data.getData();
                    mPictureCropFileUri = CameraUtil.getOutputMediaFileUri(this, CameraUtil.MEDIA_TYPE_IMAGE);
                    CameraUtil.cropImage(this, o, mPictureCropFileUri, REQUEST_CODE_CROP_PHOTO, 1, 1, 300, 300);
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_CROP_PHOTO) {//裁剪
            if (resultCode == Activity.RESULT_OK) {
                isSelectAvatar = true;
                if (mPictureCropFileUri != null) {
                    mCurrentFile = new File(mPictureCropFileUri.getPath());
                    AvatarHelper.getInstance().displayUrl(mPictureCropFileUri.toString(), mAvatarImg);
                } else {
                    ToastUtil.showToast(this, R.string.c_crop_failed);
                }
            }
        } else if (requestCode == 4) {
            // 选择城市
            if (resultCode == RESULT_OK && data != null) {
                int countryId = data.getIntExtra(SelectAreaActivity.EXTRA_COUNTRY_ID, 0);
                int provinceId = data.getIntExtra(SelectAreaActivity.EXTRA_PROVINCE_ID, 0);
                int cityId = data.getIntExtra(SelectAreaActivity.EXTRA_CITY_ID, 0);
                int countyId = data.getIntExtra(SelectAreaActivity.EXTRA_COUNTY_ID, 0);

                String province_name = data.getStringExtra(SelectAreaActivity.EXTRA_PROVINCE_NAME);
                String city_name = data.getStringExtra(SelectAreaActivity.EXTRA_CITY_NAME);
                /*String county_name = data.getStringExtra(SelectAreaActivity.EXTRA_COUNTY_ID);*/
                mCityTv.setText(province_name + "-" + city_name);
                logicInfo();
                mTempData.setCountryId(countryId);
                mTempData.setProvinceId(provinceId);
                mTempData.setCityId(cityId);
                mTempData.setAreaId(countyId);
            }
        }
    }

    private void showSelectSexDialog() {
        String[] sexs = new String[]{getString(R.string.sex_man), getString(R.string.sex_woman)};
        new AlertDialog.Builder(this).setTitle(InternationalizationHelper.getString("GENDER_SELECTION"))
                .setSingleChoiceItems(sexs, mTempData.getSex() == 1 ? 0 : 1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            mTempData.setSex(1);
                            mSexTv.setText(R.string.sex_man);
                        } else {
                            mTempData.setSex(0);
                            mSexTv.setText(R.string.sex_woman);
                        }
                        dialog.dismiss();
                    }
                }).setCancelable(true).create().show();
    }

    @SuppressWarnings("deprecation")
    private void showSelectBirthdayDialog() {
        /*Date date = new Date(mTempData.getBirthday() * 1000);
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                GregorianCalendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                mTempData.setBirthday(TimeUtils.getSpecialBeginTime(mBirthdayTv, calendar.getTime().getTime() / 1000));
            }
        }, date.getYear() + 1900, date.getMonth(), date.getDate());

        DatePicker datePicker = dialog.getDatePicker();
        datePicker.setMaxDate(new Date().getTime());
        dialog.show();*/

        DateSelectHelper dialog = DateSelectHelper.getInstance(RegisterUserBasicInfoActivity.this);
        dialog.setDateMin("1900-1-1");
        dialog.setDateMax(System.currentTimeMillis());
        dialog.setCurrentDate(mTempData.getBirthday() * 1000);
        dialog.setOnDateSetListener(new DateSelectHelper.OnDateResultListener() {
            @Override
            public void onDateSet(long time, String dateFromat) {
                mTempData.setBirthday(time / 1000);
                mBirthdayTv.setText(dateFromat);
            }
        });

        dialog.show();
    }

    private void loadPageData() {
        mTempData.setNickName(mNameEdit.getText().toString().trim());
    }

    private void register() {
        if (!MyApplication.getInstance().isNetworkActive()) {
            ToastUtil.showToast(this, R.string.net_exception);
            return;
        }
        loadPageData();

        if (TextUtils.isEmpty(mTempData.getNickName())) {
            mNameEdit.requestFocus();
            mNameEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.name_empty_error));
            return;
        }
        if (mTempData.getNickName().length() > 16) {
            mNameEdit.requestFocus();
            mNameEdit.setError(StringUtils.editTextHtmlErrorTip(this, "昵称长度不得超过16字"));
            return;
        }
        if (!isSelectAvatar) {
            DialogHelper.tip(this, getString(R.string.must_select_avatar_can_register));
            return;
        }

        if (coreManager.getConfig().isQestionOpen && mData.size() == 0) {
            ToastUtil.showToast(mContext, getString(R.string.please_set_a_secret_question));
            return;
        }

        registers();
    }

    public boolean logicInfo() {
        if (coreManager.getConfig().isQestionOpen && mData.size() == 0) {
            mNextStepBtn.setEnabled(false);
            return false;
        }
        if (TextUtils.isEmpty(mNameEdit.getText().toString().trim())) {
            mNextStepBtn.setEnabled(false);
            return false;
        }
//        if (!coreManager.getConfig().disableLocationServer & TextUtils.isEmpty(mCityTv.getText().toString().trim())) {
//            mNextStepBtn.setEnabled(false);
//            return false;
//        }
        mNextStepBtn.setEnabled(true);
        return true;
    }

    public void registers() {
        mInviteCode = mInviteCodeEdit.getText().toString().trim();
        Map<String, String> params = new HashMap<>();
        // 前面页面传递的信息
        params.put("userType", "1");
        params.put("telephone", mPhoneNum);
        params.put("password", mPassword);
        if (coreManager.getConfig().registerInviteCode > 0) {
            if(!TextUtils.isEmpty(code)) {
                params.put("inviteCode", code);
            }
        }
        if (coreManager.getConfig().isQestionOpen) {
            String json = new Gson().toJson(mData);
            params.put("questions", TextUtils.isEmpty(json) ? "" : json);
        }
        params.put("areaCode", mobilePrefix);//TODO AreaCode 区号暂时不带
        // 本页面信息
        params.put("nickname", mTempData.getNickName());
        params.put("sex", String.valueOf(mTempData.getSex()));
        params.put("birthday", String.valueOf(mTempData.getBirthday()));
        params.put("xmppVersion", "1");
        params.put("countryId", String.valueOf(mTempData.getCountryId()));
        params.put("provinceId", String.valueOf(mTempData.getProvinceId()));
        params.put("cityId", String.valueOf(mTempData.getCityId()));
        params.put("areaId", String.valueOf(mTempData.getAreaId()));
        params.put("registerType", type + "");

        params.put("isSmsRegister", String.valueOf(RegisterActivity.isSmsRegister));

        // 附加信息
        params.put("apiVersion", DeviceInfoUtil.getVersionCode(mContext) + "");
        params.put("model", DeviceInfoUtil.getModel());
        params.put("osVersion", DeviceInfoUtil.getOsVersion());
        params.put("serial", DeviceInfoUtil.getDeviceId(mContext));
        params.put("appBrand", DeviceInfoUtil.getBrand());////获取手机品牌
        if (!coreManager.getConfig().disableLocationServer) {
            // 地址信息
            double latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
            double longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();
            String location = MyApplication.getInstance().getBdLocationHelper().getAddress();
            if (latitude != 0) {
                params.put("latitude", String.valueOf(latitude));
            }
            if (longitude != 0) {
                params.put("longitude", String.valueOf(longitude));
            }
            if (!TextUtils.isEmpty(location)) {
                params.put("location", location);
            }
        }
        String url = coreManager.getConfig().USER_REGISTER;
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HttpUtils.get().url(url)
                .params(params)
                .build()
                .execute(new BaseCallback<LoginRegisterResult>(LoginRegisterResult.class) {

                    @Override
                    public void onResponse(ObjectResult<LoginRegisterResult> result) {
                        DialogHelper.dismissProgressDialog();
                        if (!com.xuan.xuanhttplibrary.okhttp.result.Result.checkSuccess(getApplicationContext(), result)) {
                            if (result == null) {
                                Reporter.post("注册失败，result为空");
                            } else {
                                Reporter.post("注册失败" + result.toString());
                            }
                            return;
                        }

                        PreferenceUtils.putInt(mContext, AppConstant.LOGIN_TYPE, type);
                        PreferenceUtils.putInt(mContext, AppConstant.LOGIN_other, 0);
                        PreferenceUtils.putBoolean(mContext, AppConstant.LOGINSTATU, true);


                        // 注册成功
                        boolean success = LoginHelper.setLoginUser(RegisterUserBasicInfoActivity.this, coreManager, mPhoneNum, mPassword, result);
                        if (success) {
                            // 新注册的账号没有支付密码，
                            MyApplication.getInstance().initPayPassword(result.getData().getUserId(), 0);

                            ToastUtil.showToast(RegisterUserBasicInfoActivity.this, R.string.register_success);
                            if (mCurrentFile != null && mCurrentFile.exists()) {
                                // 选择了头像，那么先上传头像
                                uploadAvatar(result.getData().getIsupdate(), mCurrentFile);
                            }

                        } else {
                            // 失败
                            if (TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(RegisterUserBasicInfoActivity.this, R.string.register_error);
                            } else {
                                ToastUtil.showToast(RegisterUserBasicInfoActivity.this, result.getResultMsg());
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(RegisterUserBasicInfoActivity.this);
                    }
                });
    }

    /**
     * 自动定位...
     */
    private void requestLocationCity() {
        if (MapHelper.getInstance() == null) {
            return;
        }
        MapHelper.getInstance().requestLatLng(new MapHelper.OnSuccessListener<MapHelper.LatLng>() {
            @Override
            public void onSuccess(MapHelper.LatLng latLng) {
                MapHelper.getInstance().requestCityName(latLng, new MapHelper.OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        String cityName = MyApplication.getInstance().getBdLocationHelper().getCityName();
                        Area area = null;
                        if (!TextUtils.isEmpty(cityName)) {
                            area = AreasDao.getInstance().searchByName(cityName);
                        }
                        if (area != null) {
                            Area countryArea = null;
                            Area provinceArea = null;
                            Area cityArea = null;
                            Area countyArea = null;
                            switch (area.getType()) {
                                case Area.AREA_TYPE_COUNTRY:
                                    countryArea = area;
                                    break;
                                case Area.AREA_TYPE_PROVINCE:
                                    provinceArea = area;
                                    break;
                                case Area.AREA_TYPE_CITY:
                                    cityArea = area;
                                    break;
                                case Area.AREA_TYPE_COUNTY:
                                default:
                                    countyArea = area;
                                    break;
                            }
                            if (countyArea != null) {
                                mTempData.setAreaId(countyArea.getId());
                                cityArea = AreasDao.getInstance().getArea(countyArea.getParent_id());
                            }

                            if (cityArea != null) {
                                mTempData.setCityId(cityArea.getId());
                                mCityTv.setText(cityArea.getName());
                                logicInfo();
                                provinceArea = AreasDao.getInstance().getArea(cityArea.getParent_id());
                            }

                            if (provinceArea != null) {
                                mTempData.setProvinceId(provinceArea.getId());
                                countryArea = AreasDao.getInstance().getArea(provinceArea.getParent_id());
                            }

                            if (countryArea != null) {
                                mTempData.setCountryId(countryArea.getId());
                            }
                        } else {
                            Log.e(TAG, "获取地区失败，", new RuntimeException("找不到城市：" + cityName));
                        }
                    }
                }, new MapHelper.OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, "获取城市名称失败，", t);

                    }
                });
            }
        }, new MapHelper.OnErrorListener() {
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "定位经纬度失败，", t);
            }
        });
    }


    @Override
    public void onBackPressed() {
        doBack();
    }

    @Override
    protected boolean onHomeAsUp() {
        doBack();
        return true;
    }

    private void doBack() {
        TipDialog tipDialog = new TipDialog(this);
        tipDialog.setmConfirmOnClickListener(getString(R.string.cancel_register_prompt), new TipDialog.ConfirmOnClickListener() {
            @Override
            public void confirm() {
                finish();
            }
        });
        tipDialog.show();
        tipDialog.setCancelOnTouchOutside(true);
    }

    private void uploadAvatar(int isupdate, File file) {
        if (!file.exists()) {
            // 文件不存在
            return;
        }
        // 显示正在上传的ProgressDialog
        DialogHelper.showMessageProgressDialog(this, InternationalizationHelper.getString("UPLOAD_AVATAR"));
        if (coreManager.getConfig().IS_OPEN_OBS_STATUS != 0) {
            AtomicInteger tempS = new AtomicInteger();
            AtomicInteger tempE = new AtomicInteger();
            OBSUtils.uploadHeadImg(coreManager.getSelf().getUserId(), file, RegisterUserBasicInfoActivity.this, coreManager, result -> {
                        LogUtils.log("uploadImg---头像成功回调");
                        if (tempS.get() > 0) {
                            return;
                        }
                        tempS.getAndIncrement();
                        LogUtils.log("uploadImg----进入应用");
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(RegisterUserBasicInfoActivity.this, R.string.upload_avatar_success);
                        DataDownloadActivity.start(mContext, isupdate);
                        finish();
                    },
                    msg -> {
                        LogUtils.log("uploadImg---头像错误回调");
                        if (tempE.get() > 0) {
                            return;
                        }
                        LogUtils.log("uploadImg----选用服务器上传");
                        tempE.getAndIncrement();
                        uploadHeadImgToService(isupdate, file);
                    }
            );
        } else {
            uploadHeadImgToService(isupdate, file);
        }
    }

    private void uploadHeadImgToService(int isupdate, File file) {
        RequestParams params = new RequestParams();
        String loginUserId = coreManager.getSelf().getUserId();
        params.put("userId", loginUserId);
        try {
            params.put("file1", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(coreManager.getConfig().AVATAR_UPLOAD_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                boolean success = false;
                if (arg0 == 200) {
                    Result result = null;
                    try {
                        result = JSON.parseObject(new String(arg2), Result.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (result != null && result.getResultCode() == Result.CODE_SUCCESS) {
                        success = true;
                    }
                }

                DialogHelper.dismissProgressDialog();
                if (success) {
                    ToastUtil.showToast(RegisterUserBasicInfoActivity.this, R.string.upload_avatar_success);

                }
                DataDownloadActivity.start(mContext, isupdate);
                finish();

            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                DialogHelper.dismissProgressDialog();
//                ToastUtil.showToast(RegisterUserBasicInfoActivity.this, R.string.upload_avatar_failed);
                DataDownloadActivity.start(mContext, isupdate);
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(SecurityQuestion securityQuestion) {
        mData.clear();
        mData.addAll(securityQuestion.getData());
        updateSecureSettingUI();
    }

    public void updateSecureSettingUI() {
        if (mData.size() > 0) {
            mSecureSettingTv.setText(getString(R.string.has_been_set));
            mSecureSettingTv.setTextColor(ContextCompat.getColor(this, R.color.black));
        } else {
            mSecureSettingTv.setText(getString(R.string.set_the_secret_issue));
            mSecureSettingTv.setTextColor(ContextCompat.getColor(this, R.color.color_96));
        }
        logicInfo();
    }

    @Override
    public void onSuccess() {
        showSelectAvatarDialog();
    }

    @Override
    public void onFailure(List<String> data) {
        if (data.size() > 0) {
            PermissionDialog.show(this, data);
        }
    }
}
