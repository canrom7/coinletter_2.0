package cn.imeina.screeencaptureshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.androidyuan.androidscreenshot_sysapi.ExampleActivity;
import com.androidyuan.lib.screenshot.ScreenShotActivity;
import com.androidyuan.lib.screenshot.Shooter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 截图编辑分享
 * Created by AKid on 2017/6/6.
 */

public class ScreenCaptureShareActivity extends Activity implements View.OnClickListener, ScreenCaptureEditerView.ScreenCaptureCallback {

    private static final String TAG = ScreenCaptureEditerView.class.getSimpleName();
    private static String SCREENSHOT_PATH = Environment.getExternalStorageDirectory() + File.separator + "screenshot.png";
    private static String SCREENSHOT_SHARE_PATH = Environment.getExternalStorageDirectory() + File.separator + "share_screenshot.png";
    private ScreenCaptureEditerView editerView;
    private LinearLayout llClose, llArrows, llBight, llRevocation, llShare;
    private LinearLayout llPalette;
    private PopupWindow popStrokeWidth;
    private PopupWindow popColorPalette;
    private List<ImageView> popStrokeWidthList;
    private List<RadioButton> popColorPaletteList;
    private int previousCheckedStrokeWidthIndex, currentCheckedStrokeWidthIndex, currentCheckedColorIndex;
    private View strokeWidthContentView, colorPaletteContentView;
    private ImageView ivArrows, ivBight, ivPalette, ivRevocation;
    private boolean isNoOperate = true;

    private static final int REQ_CODE_PER = 0x2304;
    private static final int REQ_CODE_ACT = 0x2305;



    /**
     * This is an example for using Shooter.
     * This method will request permission and take screenshot on this Activity.
     */
    public void onClickReqPermission() {
        if (Build.VERSION.SDK_INT >= 21) {
            startActivityForResult(createScreenCaptureIntent(), REQ_CODE_PER);
        }
    }

//    /**
//     * using {@see ScreenShotActivity} to take screenshot on current Activity directly.
//     * If you press home it will take screenshot on another app.
//     * @param view
//     */
    public void onClickShot() {
        startActivityForResult(ScreenShotActivity.createIntent(this, null,0), REQ_CODE_ACT);
        toast("Press home key,open another app.");//if you want to take screenshot on another app.
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Intent createScreenCaptureIntent() {
        //Here using media_projection instead of Context.MEDIA_PROJECTION_SERVICE to  make it successfully build on low api.
        return ((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE)).createScreenCaptureIntent();
    }

    private String getSavedPath() {
        return getExternalFilesDir("screenshot").getAbsoluteFile() + "/"
                + SystemClock.currentThreadTimeMillis() + ".png";
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case REQ_CODE_ACT: {
                if (resultCode == RESULT_OK && data != null) {
                    toast("Screenshot saved at " + data.getData().toString());

                    //获取状态栏高度
                    Rect rectangle = new Rect();
                    getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
                    int statusHeight = rectangle.top;
                    //开启截屏编辑

                    Uri scUri = data.getData();
                    int offset = getIntent().getIntExtra("offset", 0);
                    SCREENSHOT_SHARE_PATH = getFilesDir().getAbsolutePath() + File.separator + System.currentTimeMillis() + "shot.png";
                    editerView.init(scUri, SCREENSHOT_SHARE_PATH, statusHeight, offset);
                    editerView.setCallback(ScreenCaptureShareActivity.this);
                } else {
                    toast("You got wrong.");
                }
            }
            break;
            case REQ_CODE_PER: {
                if (resultCode == RESULT_OK && data != null) {
                    Shooter shooter = new Shooter(ScreenCaptureShareActivity.this, resultCode, data);
                    shooter.startScreenShot(getSavedPath(), new Shooter.OnShotListener() {
                                @Override
                                public void onFinish(String path) {
                                    //here is done status.
//                                    toast("Screenshot saved at " + path);

                                    //获取状态栏高度
                                    Rect rectangle = new Rect();
                                    getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
                                    int statusHeight = rectangle.top;
                                    //开启截屏编辑

                                    Uri scUri =getUri(ScreenCaptureShareActivity.this,new File(path));
                                    int offset = getIntent().getIntExtra("offset", 0);
                                    SCREENSHOT_SHARE_PATH = getFilesDir().getAbsolutePath() + File.separator + System.currentTimeMillis() + "shot.png";
                                    editerView.init(scUri, SCREENSHOT_SHARE_PATH, statusHeight, offset);
                                    editerView.setCallback(ScreenCaptureShareActivity.this);
                                }

                                @Override
                                public void onError() {
                                    toast("You got wrong.");
                                }
                            }
                    );
                } else if (resultCode == RESULT_CANCELED) {
                    //user canceled.
                } else {

                }
            }
        }
    }


    private void toast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    private void goBackground() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }


    public static void screenCapture(View view,  String path) {
        final Context context = view.getContext();
        SCREENSHOT_PATH = context.getFilesDir().getAbsolutePath() + File.separator + System.currentTimeMillis() + ".png";
        try {

            Intent intent = new Intent(context, ScreenCaptureShareActivity.class);
            intent.putExtra("scUri", path);
            intent.putExtra("offset", 0);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static Uri getUri(Context context, File file) {
        return FileProvider.getUriForFile(context, context.getPackageName()+".fileProvider", file);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
//                View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_share);
        initView();
        //获取状态栏高度
        Rect rectangle = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusHeight = rectangle.top;
        //开启截屏编辑
        String path = getIntent().getStringExtra("scUri");
        if(path!=null&&new File(path).exists()) {
             Uri scUri = getUri(ScreenCaptureShareActivity.this, new File(path));
            int offset = getIntent().getIntExtra("offset", 0);
            SCREENSHOT_SHARE_PATH = getFilesDir().getAbsolutePath() + File.separator + System.currentTimeMillis() + "shot.png";
            editerView.init(scUri, SCREENSHOT_SHARE_PATH, statusHeight, offset);
            editerView.setCallback(ScreenCaptureShareActivity.this);
        }
///storage/emulated/0/Pictures/QQ/Image_1699105671524.jpg
    }

    private void initView() {

        editerView = (ScreenCaptureEditerView) findViewById(R.id.sc_editer);
        llClose = (LinearLayout) findViewById(R.id.ll_close);
        llArrows = (LinearLayout) findViewById(R.id.ll_arrows);
        llBight = (LinearLayout) findViewById(R.id.ll_bight);
        llPalette = (LinearLayout) findViewById(R.id.ll_palette);
        llRevocation = (LinearLayout) findViewById(R.id.ll_revocation);
        llShare = (LinearLayout) findViewById(R.id.ll_screencapture_share);

        ivArrows = (ImageView) findViewById(R.id.iv_arrows);
        ivBight = (ImageView) findViewById(R.id.iv_bight);
        ivPalette = (ImageView) findViewById(R.id.iv_palette_color);
        ivRevocation = (ImageView) findViewById(R.id.iv_revocation);
        ivArrows.setSelected(true);
        ivRevocation.setEnabled(false);

        llClose.setOnClickListener(this);
        llArrows.setOnClickListener(this);
        llBight.setOnClickListener(this);
        llPalette.setOnClickListener(this);
        llRevocation.setOnClickListener(this);
        llShare.setOnClickListener(this);

//        editerView.setArrowsLine(true);
//        ivArrows.setSelected(true);
//        ivBight.setSelected(false);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.ll_close) {
            //关闭
            showCloseDialog();
        } else if (v.getId() == R.id.ll_arrows) {
            //箭头直线
            editerView.setArrowsLine(true);
            ivArrows.setSelected(true);
            ivBight.setSelected(false);
        } else if (v.getId() == R.id.ll_bight) {
            //涂鸦曲线
            editerView.setArrowsLine(false);
            ivArrows.setSelected(false);
            ivBight.setSelected(true);
            showPaintStrokeWidth();
        } else if (v.getId() == R.id.ll_palette) {
            //调色盘
            showPalette();
        } else if (v.getId() == R.id.ll_revocation) {
            //撤销
            editerView.revocation();
        } else if (v.getId() == R.id.ll_screencapture_share) {
            //合成分享图片...
            editerView.compound();
        } else if (v.getId() == R.id.iv_size_one) {
            popStrokeWidthList.get(previousCheckedStrokeWidthIndex).setSelected(false);
            previousCheckedStrokeWidthIndex = 0;
            currentCheckedStrokeWidthIndex = 0;
            editerView.setStrokeWidth(ScreenCaptureEditerView.PaintStrokeWidth.SIZE_ONE);
            popStrokeWidth.dismiss();
        } else if (v.getId() == R.id.iv_size_two) {
            popStrokeWidthList.get(previousCheckedStrokeWidthIndex).setSelected(false);
            previousCheckedStrokeWidthIndex = 1;
            currentCheckedStrokeWidthIndex = 1;
            editerView.setStrokeWidth(ScreenCaptureEditerView.PaintStrokeWidth.SIZE_TWO);
            popStrokeWidth.dismiss();
        } else if (v.getId() == R.id.iv_size_three) {
            popStrokeWidthList.get(previousCheckedStrokeWidthIndex).setSelected(false);
            previousCheckedStrokeWidthIndex = 2;
            currentCheckedStrokeWidthIndex = 2;
            editerView.setStrokeWidth(ScreenCaptureEditerView.PaintStrokeWidth.SIZE_THREE);
            popStrokeWidth.dismiss();
        } else if (v.getId() == R.id.iv_size_four) {
            popStrokeWidthList.get(previousCheckedStrokeWidthIndex).setSelected(false);
            previousCheckedStrokeWidthIndex = 3;
            currentCheckedStrokeWidthIndex = 3;
            editerView.setStrokeWidth(ScreenCaptureEditerView.PaintStrokeWidth.SIZE_FOUR);
            popStrokeWidth.dismiss();
        } else if (v.getId() == R.id.iv_size_five) {
            popStrokeWidthList.get(previousCheckedStrokeWidthIndex).setSelected(false);
            previousCheckedStrokeWidthIndex = 4;
            currentCheckedStrokeWidthIndex = 4;
            editerView.setStrokeWidth(ScreenCaptureEditerView.PaintStrokeWidth.SIZE_FIVE);
            popStrokeWidth.dismiss();
        }
    }

    /**
     * 放弃分享
     */
    private void showCloseDialog() {

        editerView.compound();
    }

    /**
     * 选择曲线画笔宽度
     */
    private void showPaintStrokeWidth() {

        if (popStrokeWidth == null) {
            popStrokeWidthList = new ArrayList<>();
            strokeWidthContentView = LayoutInflater.from(this).inflate(R.layout.layout_pop_size, null);
            ImageView ivSizeOne = (ImageView) strokeWidthContentView.findViewById(R.id.iv_size_one);
            ImageView ivSizeTwo = (ImageView) strokeWidthContentView.findViewById(R.id.iv_size_two);
            ImageView ivSizeThree = (ImageView) strokeWidthContentView.findViewById(R.id.iv_size_three);
            ImageView ivSizeFour = (ImageView) strokeWidthContentView.findViewById(R.id.iv_size_four);
            ImageView ivSizeFive = (ImageView) strokeWidthContentView.findViewById(R.id.iv_size_five);
            ivSizeOne.setOnClickListener(this);
            ivSizeTwo.setOnClickListener(this);
            ivSizeThree.setOnClickListener(this);
            ivSizeFour.setOnClickListener(this);
            ivSizeFive.setOnClickListener(this);
            popStrokeWidthList.add(ivSizeOne);
            popStrokeWidthList.add(ivSizeTwo);
            popStrokeWidthList.add(ivSizeThree);
            popStrokeWidthList.add(ivSizeFour);
            popStrokeWidthList.add(ivSizeFive);
            popStrokeWidth = new PopupWindow();
            popStrokeWidth.setContentView(strokeWidthContentView);
            popStrokeWidth.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            popStrokeWidth.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            //外部是否可以点击
            popStrokeWidth.setBackgroundDrawable(new ColorDrawable());
            popStrokeWidth.setOutsideTouchable(true);
        } else {

            if (popStrokeWidth.isShowing()) {

                popStrokeWidth.dismiss();
                return;
            }
        }
        //回显选中...
        popStrokeWidthList.get(currentCheckedStrokeWidthIndex).setSelected(true);
        int[] location = PopupWindowUtil.calculatePopWindowPos(this, llBight, strokeWidthContentView);
        popStrokeWidth.showAtLocation(llBight, Gravity.NO_GRAVITY, location[0], location[1]);
    }

    /**
     * 显示调色盘
     */
    private void showPalette() {

        int[] colors = editerView.getColors();
        if (popColorPalette == null) {
            popColorPaletteList = new ArrayList<>();
            colorPaletteContentView = LayoutInflater.from(this).inflate(R.layout.layout_pop_color, null);
            RadioGroup rgColorPalette = (RadioGroup) colorPaletteContentView.findViewById(R.id.rg_color_palette);
            RadioButton rbColorOne = (RadioButton) colorPaletteContentView.findViewById(R.id.rb_color_one);
            RadioButton rbColorTwo = (RadioButton) colorPaletteContentView.findViewById(R.id.rb_color_two);
            RadioButton rbColorThree = (RadioButton) colorPaletteContentView.findViewById(R.id.rb_color_three);
            RadioButton rbColorFour = (RadioButton) colorPaletteContentView.findViewById(R.id.rb_color_four);
            RadioButton rbColorFive = (RadioButton) colorPaletteContentView.findViewById(R.id.rb_color_five);
            popColorPaletteList.add(rbColorOne);
            popColorPaletteList.add(rbColorTwo);
            popColorPaletteList.add(rbColorThree);
            popColorPaletteList.add(rbColorFour);
            popColorPaletteList.add(rbColorFive);
            rgColorPalette.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                    if (checkedId == R.id.rb_color_one) {
                        editerView.setColor(0);
                        currentCheckedColorIndex = 0;
                        ivPalette.setImageResource(R.drawable.ic_palette_color_one);
                    } else if (checkedId == R.id.rb_color_two) {
                        editerView.setColor(1);
                        currentCheckedColorIndex = 1;
                        ivPalette.setImageResource(R.drawable.ic_palette_color_two);
                    } else if (checkedId == R.id.rb_color_three) {
                        editerView.setColor(2);
                        currentCheckedColorIndex = 2;
                        ivPalette.setImageResource(R.drawable.ic_palette_color_three);
                    } else if (checkedId == R.id.rb_color_four) {
                        editerView.setColor(3);
                        currentCheckedColorIndex = 3;
                        ivPalette.setImageResource(R.drawable.ic_palette_color_four);
                    } else if (checkedId == R.id.rb_color_five) {
                        editerView.setColor(4);
                        currentCheckedColorIndex = 4;
                        ivPalette.setImageResource(R.drawable.ic_palette_color_five);
                    }
                    popColorPalette.dismiss();
                }
            });
            popColorPalette = new PopupWindow();
            popColorPalette.setContentView(colorPaletteContentView);
            popColorPalette.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            popColorPalette.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            //外部是否可以点击
            popColorPalette.setBackgroundDrawable(new ColorDrawable());
            popColorPalette.setOutsideTouchable(true);
        } else {

            if (popColorPalette.isShowing()) {

                popColorPalette.dismiss();
                return;
            }
        }
        popColorPaletteList.get(currentCheckedColorIndex).setChecked(true);
        int[] location = PopupWindowUtil.calculatePopWindowPos(this, llPalette, colorPaletteContentView);
        popColorPalette.showAtLocation(llPalette, Gravity.NO_GRAVITY, location[0], location[1]);
    }

    @Override
    public void onAbleRepeal(boolean enableRepeal) {

        isNoOperate = !enableRepeal;
        ivRevocation.setEnabled(enableRepeal);
    }

    @Override
    public void onCompoundSuccess(final String shareImgPath) {

        //此方法中不能进行UI更新.....因为是在子线程进行的回调
        //弹出分享面板...
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(ScreenCaptureShareActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        Log.e(TAG, "图像个合并成功，路径：" + shareImgPath);
    }



//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//
//        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//            showCloseDialog();
//            //这里重写返回键
//            return true;
//        }
//        return false;
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
