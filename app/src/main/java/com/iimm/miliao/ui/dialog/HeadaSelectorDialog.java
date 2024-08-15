package com.iimm.miliao.ui.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.iimm.miliao.R;
import com.iimm.miliao.adapter.DefaultAvatarAdapter;
import com.iimm.miliao.bean.ImgBean;
import com.iimm.miliao.ui.dialog.base.BaseCommonAllDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class HeadaSelectorDialog extends BaseCommonAllDialog {
    RecyclerView defaultavatar;
    ConstraintLayout photoselector;
    List<ImgBean> list;
    DefaultAvatarAdapter avatarAdapter;
    Context context;
    Button sure;
    File file;
    ImageView back;
    HeadSelectorListener headSelectorListener;

    public void setHeadSelectorListener(HeadSelectorListener headSelectorListener) {
        this.headSelectorListener = headSelectorListener;
    }


    public HeadaSelectorDialog(Context context) {
        super(context, R.style.BottomDialog);
        this.context = context;
    }

    int old = -1;

    @Override
    protected void initView() {
        photoselector = findViewById(R.id.photoselector);
        defaultavatar = findViewById(R.id.defaultavatar);
        back = findViewById(R.id.back);
        sure = findViewById(R.id.sure);
        list = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            ImgBean imgBean = new ImgBean();
            imgBean.setUrl("head" + (i + 1));
            list.add(imgBean);
        }
        Log.e("sssss", "slis" + list.size());
        avatarAdapter = new DefaultAvatarAdapter(list, context);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 4);
        defaultavatar.setAdapter(avatarAdapter);
        defaultavatar.setLayoutManager(layoutManager);

        avatarAdapter.setOnClickLisener(new DefaultAvatarAdapter.OnClickLisener() {
            @Override
            public void onClick(int i) {
                if (i == old) {
                    return;
                }
                list.get(i).setSelector(true);
                avatarAdapter.notifyItemChanged(i);
                if (old != -1) {
                    list.get(old).setSelector(false);
                    avatarAdapter.notifyItemChanged(old);
                }
                old = i;

            }
        });
        sure.setOnClickListener(v -> {
            Class drawable = R.drawable.class;
            Field field = null;
            try {
                field = drawable.getField(list.get(old).getUrl());
                int res_ID = field.getInt(field.getName());

                file = drawableToFile(context, res_ID, list.get(old).getUrl());
                if (headSelectorListener != null) {
                    headSelectorListener.onSureClickListener(file);
                }
            } catch (Exception e) {
            }


        });
        photoselector.setOnClickListener(v -> {
            dismiss();
            if (headSelectorListener != null) {
                headSelectorListener.onPohto();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_headselector;
    }

    public interface HeadSelectorListener {
        void onSureClickListener(File file);

        void onPohto();
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
}
