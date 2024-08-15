package com.iimm.miliao.ui.me.redpacket;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.view.View;

import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.adapter.MucSendRedSelectAdapter;
import com.iimm.miliao.bean.EventRedSelect;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.MucSendRedSelectBean;
import com.iimm.miliao.databinding.ActivityMucSendRedBinding;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.sortlist.PinYinUtil;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.CountryComparatorCode;
import com.iimm.miliao.util.NormalDecoration;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.UiUtils;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * MrLiu253@163.com
 * 群红包指定人数
 *
 * @time 2020-02-27
 */
public class MucSendRedSelectActivity extends BaseActivity {

    private List<MucSendRedSelectBean> mSelectSize;
    private List<MucSendRedSelectBean> mSearchList;

    private List<MucSendRedSelectBean> mMucRoomMembers;
    private MucSendRedSelectAdapter mRedSelectAdapter;
    private ActivityMucSendRedBinding mRedBinding;
    private String mRoomJid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRedBinding = DataBindingUtil.setContentView(this, R.layout.activity_muc_send_red);
        getSupportActionBar().hide();
        if (getIntent() != null) {
            mRoomJid = getIntent().getStringExtra("roomJid");
        }
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initdata();
        loData();
        initClick();
    }

    private void initdata() {
        mSelectSize = new ArrayList<>();
        mSearchList = new ArrayList<>();
        mMucRoomMembers = new ArrayList<>();
        mRedSelectAdapter = new MucSendRedSelectAdapter(mMucRoomMembers);
        mRedBinding.listView.setLayoutManager(new LinearLayoutManager(this));
        final NormalDecoration decoration = new NormalDecoration() {
            @Override
            public String getHeaderName(int i) {
                try {
                    return String.valueOf(PinYinUtil.getSortLetterBySortKey(PinYinUtil.getPingYin(mMucRoomMembers.get(i).getNickname())));
                } catch (Exception e) {

                }
                return "";

            }
        };
        mRedBinding.listView.addItemDecoration(decoration);
        mRedBinding.listView.setAdapter(mRedSelectAdapter);

    }

    private void loData() {
        DialogHelper.showDefaulteMessageProgressDialog(MucSendRedSelectActivity.this);
        Map<String, String> params = new ArrayMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomJid);
        params.put("joinTime", String.valueOf(0));
        params.put("pageSize", "20000");
        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<MucSendRedSelectBean>(MucSendRedSelectBean.class) {
                    @Override
                    public void onResponse(ArrayResult<MucSendRedSelectBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            mMucRoomMembers.clear();
                            mMucRoomMembers.addAll(result.getData());
                            Collections.sort(mMucRoomMembers, new CountryComparatorCode());
                            mRedSelectAdapter.setData(mMucRoomMembers);
                        } else {
                            ToastUtil.showToast(MucSendRedSelectActivity.this, "请求失败请重试");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(getApplicationContext());
                    }
                });
    }

    private void initClick() {
        mRedBinding.tvTitleRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UiUtils.isNormalClick(v)) {
                    return;
                }
                mSelectSize.clear();
                for (MucSendRedSelectBean data : mMucRoomMembers) {
                    if (data.isSelect()) {
                        mSelectSize.add(data);
                    }
                }
                if (mSelectSize.size()==0){
                    ToastUtil.showToast(MucSendRedSelectActivity.this,"请指定群成员");
                }else {
                    EventBus.getDefault().post(new EventRedSelect(mSelectSize));
                    finish();
                }
            }
        });
        mRedBinding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mSearchList.clear();
                String str = mRedBinding.searchEt.getText().toString();
                if (TextUtils.isEmpty(str)) {
                    mRedSelectAdapter.setData(mMucRoomMembers);
                    return;
                }

                for (int i = 0; i < mMucRoomMembers.size(); i++) {
                    String name = "";
                    Friend friend = FriendDao.getInstance().getFriend(CoreManager.getSelf(MyApplication.getContext()).getUserId(), mMucRoomMembers.get(i).getUserId());
                    if (friend != null && !TextUtils.isEmpty(friend.getRemarkName())) {
                        name = mMucRoomMembers.get(i).getNickname() + friend.getRemarkName();
                    } else {
                        name = mMucRoomMembers.get(i).getNickname();
                    }
                    if (name.contains(str)) {
                        // 符合搜索条件的好友
                        mSearchList.add((mMucRoomMembers.get(i)));
                    }
                }
                mRedSelectAdapter.setData(mSearchList);
            }
        });
    }

}
