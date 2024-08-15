package com.iimm.miliao.ui.live.view;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.ui.live.bean.Member;
import com.iimm.miliao.ui.other.BasicInfoActivity;


/**
 * 直播间-用户详情
 */
public class FragmentDialog extends DialogFragment {
    public OnClickBottomListener listener;
    /**
     * 管理
     */
    private TextView managerTv;
    /**
     * 头像
     */
    private ImageView imageIv;
    /**
     * 昵称
     */
    private TextView titleTv;
    /**
     * 描述
     */
    private TextView messageTv;
    /**
     * 取消和确认按钮
     */
    private Button positiveBn;
    private Member self;
    private Member member;
    private Dialog dialog;

    public static FragmentDialog newInstance(
            Member self, Member member,
            OnClickBottomListener onClickBottomListener
    ) {
        FragmentDialog fragment = new FragmentDialog();
        fragment.init(self, member, onClickBottomListener);
        return fragment;
    }

    private void init(Member self, Member member, OnClickBottomListener onClickBottomListener) {
        this.self = self;
        this.member = member;
        this.listener = onClickBottomListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.common_dialog_layout, null, false);
        // 使用不带Theme的构造器, 获得的dialog边框距离屏幕仍有几毫米的缝隙
        initDialogStyle(rootView);
        initView(rootView);
        initEvent();
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshView();
    }

    private void initDialogStyle(View view) {
        dialog = new Dialog(getActivity(), R.style.CustomDialog);
        // 设置Content前设定,(自定义标题,当需要自定义标题时必须指定)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        // 外部点击取消
        dialog.setCanceledOnTouchOutside(true);
        // 设置宽度为屏宽, 靠近屏幕底部。
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.Buttom_Popwindow);
        WindowManager.LayoutParams lp = window.getAttributes();
        // 中间显示
        lp.gravity = Gravity.CENTER;
        //  宽度持平
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
    }

    private void initView(View rootView) {
        managerTv = (TextView) rootView.findViewById(R.id.manager_tv);
        imageIv = (ImageView) rootView.findViewById(R.id.image);
        titleTv = (TextView) rootView.findViewById(R.id.title);
        messageTv = (TextView) rootView.findViewById(R.id.message);
        positiveBn = (Button) rootView.findViewById(R.id.positive);
    }

    /**
     * 初始化界面控件的显示数据
     */
    private void refreshView() {
        if (isManager()) {
            managerTv.setVisibility(View.VISIBLE);
        } else {
            managerTv.setVisibility(View.INVISIBLE);
        }
        AvatarHelper.getInstance().displayAvatar(String.valueOf(member.getUserId()), imageIv, false);
        titleTv.setText(member.getNickName());
    }

    private boolean isManager() {
        // 主播或者管理员可以对成员进行管理，
        // 主播可以对管理员进行管理，
        if (self.getType() == Member.TYPE_OWNER) {
            return member.getUserId() != self.getUserId();
        }
        return self.getType() == Member.TYPE_MANAGER
                && member.getType() == Member.TYPE_MEMBER;
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        managerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onManagerClick();
                }
            }
        });

        positiveBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                // 查看详情
                Intent intent = new Intent(getActivity(), BasicInfoActivity.class);
                intent.putExtra(AppConstant.EXTRA_USER_ID, String.valueOf(member.getUserId()));
                startActivity(intent);
            }
        });
    }

    public interface OnClickBottomListener {
        void onManagerClick();
    }
}
