package com.iimm.miliao.pay;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.xmpp.ListenerManager;
import com.iimm.miliao.xmpp.listener.ChatMessageListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PayActivity extends BaseActivity implements ChatMessageListener {
    private RecyclerView mSKPayRcy;
    private PayAdapter mSKPayAdapter;
    private List<ChatMessage> mChatMessageSource = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        initActionBar();
        initView();
        ListenerManager.getInstance().addChatMessageListener(this);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(view -> finish());
        TextView titleTv = findViewById(R.id.tv_title_center);
        titleTv.setText(getString(R.string.easy_pay));
    }

    private void initView() {
        mChatMessageSource = new ArrayList<>();
        List<ChatMessage> messages = ChatMessageDao.getInstance().getSingleChatMessages(coreManager.getSelf().getUserId(),
                Constants.ID_SK_PAY, TimeUtils.time_current_time(), 100);
        Collections.reverse(messages);// 将集合倒序
        mChatMessageSource.addAll(messages);

        mSKPayRcy = findViewById(R.id.easy_pay_rcy);
        mSKPayAdapter = new PayAdapter(mChatMessageSource);
        mSKPayRcy.setLayoutManager(new LinearLayoutManager(this));
        mSKPayRcy.setAdapter(mSKPayAdapter);
        mSKPayRcy.setItemAnimator(new DefaultItemAnimator());

        mSKPayRcy.scrollToPosition(mSKPayAdapter.getItemCount() - 1);
    }

    @Override
    public void onMessageSendStateChange(int messageState, String msgId) {

    }

    @Override
    public boolean onNewMessage(String fromUserId, ChatMessage message, boolean isGroupMsg) {
        if (fromUserId.equals(Constants.ID_SK_PAY)) {
            mChatMessageSource.add(message);
            mSKPayAdapter.notifyItemInserted(mChatMessageSource.size());
            mSKPayRcy.scrollToPosition(mSKPayAdapter.getItemCount() - 1);
        }
        return false;
    }
}
