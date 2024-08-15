package com.iimm.miliao.ui.message.multi;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.iimm.miliao.AppConfig;
import com.iimm.miliao.R;
import com.iimm.miliao.adapter.GroupSignRecordAdatper;
import com.iimm.miliao.bean.GroupSignRewardSelectror;
import com.iimm.miliao.bean.redpacket.GroupSignRecord;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.sortlist.SortHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.company.SearchFilter;
import com.iimm.miliao.ui.dialog.AlrtDialog;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class GroupSignRecordActivity extends BaseActivity {
    private String roomid;
    private List<BaseSortModel<GroupSignRecord>> mSortFriends;
    private List<BaseSortModel<GroupSignRecord>> adapterlist;
    private List<GroupSignRecord> selectorlist;
    private GroupSignRecordAdatper adatper;
    RecyclerView rcv;
    EditText search_et;
    Button exchange;
    private SearchFilter<BaseSortModel<GroupSignRecord>> nameFilter;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            adatper.notifyItemChanged(msg.what);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupsignrecord);
        roomid = getIntent().getStringExtra("roomJid");
        initAction();
        initView();
        records();
    }

    private void initAction() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText("签到记录");
    }

    public void initView() {
        mSortFriends = new ArrayList<>();
        adapterlist = new ArrayList<>();
        selectorlist = new ArrayList<>();
        rcv = findViewById(R.id.rcv);
        search_et = findViewById(R.id.search_et);
        exchange = findViewById(R.id.exchange);


        adatper = new GroupSignRecordAdatper(this, adapterlist);
        adatper.setSeleCtorListener(new GroupSignRecordAdatper.SeleCtorListener() {

            @Override
            public void selector(int i) {
                boolean ichecked = adapterlist.get(i).getBean().isIchecked();
                if (ichecked) {
                    adapterlist.get(i).getBean().setIchecked(false);
                    updateData(false, i);

                } else {
                    adapterlist.get(i).getBean().setIchecked(true);
                    updateData(true, i);

                }
//               handler.sendEmptyMessage(i);

            }
        });
        rcv.setLayoutManager(new LinearLayoutManager(this));
        rcv.setAdapter(adatper);


        search_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    adapterlist.clear();
                    adapterlist.addAll(mSortFriends);
                    adatper.notifyDataSetChanged();
                } else {

                    getFilter().setResultListener(new SearchFilter.ResultListener<BaseSortModel<GroupSignRecord>>() {
                        @Override
                        public void result(List<BaseSortModel<GroupSignRecord>> results) {
                            Log.e("sssss", "R" + results.size());
                            adapterlist.clear();
                            adapterlist.addAll(results);
                            adatper.notifyDataSetChanged();
                        }
                    });
                    getFilter().filter(s);
                }
            }
        });
        exchange.setOnClickListener(v -> {
            AlrtDialog alrtDialog = new AlrtDialog(GroupSignRecordActivity.this);
            alrtDialog.setSureListener(new AlrtDialog.SureListener() {
                @Override
                public void sure(boolean b) {
                    if (b) {
                        reward();
                    }
                }
            });
            alrtDialog.show();
        });

    }

    /*
     * 选择那个人
     * */
    public void updateData(boolean ischeck, int i) {
        for (int n = 0; n < mSortFriends.size(); n++) {
            if (mSortFriends.get(n).getBean().getUserId().equals(adapterlist.get(i).getBean().getUserId())) {
                mSortFriends.get(n).getBean().setIchecked(ischeck);
            }
        }
        if (ischeck) {
            selectorlist.add(adapterlist.get(i).getBean());
        } else {
            for (int m = 0; m < selectorlist.size(); m++) {
                if (selectorlist.get(m).getId().equals(adapterlist.get(i).getBean().getId())) {
                    selectorlist.remove(m);
                }
            }
        }
    }

    public SearchFilter getFilter() {
        if (nameFilter == null) {
            List<String> filterKeys = new ArrayList<>();
            filterKeys.add("nickName");
            filterKeys.add("firstLetter");
            nameFilter = new SearchFilter<>(mSortFriends, filterKeys, "id");
            List<String> keys = new ArrayList<>();
            keys.add("GroupSignRecord");
            nameFilter.setKeyInClassNames(keys);
        }
        return nameFilter;
    }

    public void records() {
        Map<String, String> map = new HashMap<>();
        map.put("access_token", coreManager.getSelfStatus().accessToken);
        map.put("roomJid", roomid);
        HttpUtils.post().params(map).url(AppConfig.ROOM_GROUP_SIGN_RECORD).build().execute(new BaseCallback<String>(String.class) {
            @Override
            public void onResponse(ObjectResult<String> result) {
                if (result.getResultCode() == 1) {
                    List<GroupSignRecord> list = JSON.parseArray(result.getData(), GroupSignRecord.class);
                    if (list != null) {
                        Map<String, Integer> existMap = new HashMap<>();
                        mSortFriends.clear();
                        mSortFriends.addAll(SortHelper.toSortedModelList(list, existMap, GroupSignRecord::getNickName));
                        Log.e("ssssss", mSortFriends.size() + "  ");
                        adapterlist.clear();
                        adapterlist.addAll(mSortFriends);
                        adatper.notifyDataSetChanged();
                    }
                } else {
                    ToastUtil.showToast(GroupSignRecordActivity.this, result.getResultMsg());
                }

            }

            @Override
            public void onError(Call call, Exception e) {
                ToastUtil.showToast(GroupSignRecordActivity.this, e.getMessage());
            }
        });
    }

    public void reward() {
        if (selectorlist.size() == 0) {
            ToastUtil.showToast(this, "请先选择发奖人员！");
            return;
        }
        Map<String, String> map = new HashMap<>();
        GroupSignRewardSelectror rewardSelectror = new GroupSignRewardSelectror();
        rewardSelectror.setData(selectorlist);
        rewardSelectror.setRoomJid(roomid);
        map.put("access_token", coreManager.getSelfStatus().accessToken);
        map.put("requestData", JSON.toJSONString(rewardSelectror));
        HttpUtils.post().params(map).url(AppConfig.ROOM_GROUP_SIGN_REWARD).build().execute(new BaseCallback<String>(String.class) {
            @Override
            public void onResponse(ObjectResult<String> result) {
                if (result.getResultCode() == 1) {
                    ToastUtil.showToast(GroupSignRecordActivity.this, result.getResultMsg());
                } else {
                    ToastUtil.showToast(GroupSignRecordActivity.this, result.getResultMsg());
                }
                selectorlist.clear();
                records();
            }

            @Override
            public void onError(Call call, Exception e) {

            }
        });
    }
}
