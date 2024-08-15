package com.iimm.miliao.ui.contacts;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.Space;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.iimm.miliao.BuildConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.Contact;
import com.iimm.miliao.bean.Contacts;
import com.iimm.miliao.db.dao.ContactDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.sortlist.BaseComparator;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.sortlist.SideBar;
import com.iimm.miliao.sortlist.SortHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.AnimationUtil;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.ContactsUtil;
import com.iimm.miliao.util.DisplayUtil;
import com.iimm.miliao.util.PermissionUtil;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.SkinUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ViewHolder;
import com.iimm.miliao.util.permission.AndPermissionUtils;
import com.iimm.miliao.util.permission.OnPermissionClickListener;
import com.iimm.miliao.view.PullToRefreshSlideListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 分享到联系人
 */
public class ContactsMsgInviteActivity extends BaseActivity implements OnPermissionClickListener {
    private SideBar mSideBar;
    private TextView mTextDialog;
    private PullToRefreshSlideListView mListView;
    private ContactsAdapter mContactsAdapter;
    private List<Contacts> mContactList;
    private List<BaseSortModel<Contacts>> mSortContactList;
    private BaseComparator<Contacts> mBaseComparator;
    private String mLoginUserId;
    // 全选
    private TextView tvTitleRight;
    private boolean isBatch;
    private Map<String, Contacts> mBatchAddContacts = new HashMap<>();
    private TextView mBatchAddTv;

    private Map<String, Contacts> phoneContacts;

    private int mobilePrefix;
    private boolean mBatch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_msg_invite);

        mLoginUserId = coreManager.getSelf().getUserId();
        mContactList = new ArrayList<>();
        mSortContactList = new ArrayList<>();
        mBaseComparator = new BaseComparator<>();
        mContactsAdapter = new ContactsAdapter();

        mobilePrefix = PreferenceUtils.getInt(MyApplication.getContext(), Constants.AREA_CODE_KEY, 86);

        initActionBar();
        boolean isReadContacts = PermissionUtil.checkSelfPermissions(this, new String[]{Manifest.permission.READ_CONTACTS});
        if (!isReadContacts) {
            AndPermissionUtils.addressbookPermission(this, this);
            return;
        }

        initView();
        dataLayering();
        initEvent();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText("邀请好友");

        tvTitleRight = (TextView) findViewById(R.id.tv_title_right);
        tvTitleRight.setText("批量邀请");
    }

    public void initView() {
        mListView = (PullToRefreshSlideListView) findViewById(R.id.pull_refresh_list);
        mListView.getRefreshableView().setAdapter(mContactsAdapter);
        mListView.setMode(PullToRefreshBase.Mode.DISABLED);

        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar.setTextView(mTextDialog);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = mContactsAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.getRefreshableView().setSelection(position);
                }
            }
        });

        mBatchAddTv = (TextView) findViewById(R.id.sure_add_tv);
        mBatchAddTv.setTextColor(SkinUtils.getSkin(this).getAccentColor());

    }

    private void dataLayering() {
        phoneContacts = ContactsUtil.getPhoneContacts(this);

        List<Contact> allContacts = ContactDao.getInstance().getAllContacts(mLoginUserId);
        // 现在数据库内在创建联系人的时候已经去重了，按理说这里已经不需要处理了，但是一些老用户联系人表内已经生成了一些重复的数据，所以这里在去下重
        Set<Contact> set = new TreeSet<>(new Comparator<Contact>() {
            @Override
            public int compare(Contact o1, Contact o2) {
                return o1.getToUserId().compareTo(o2.getToUserId());
            }
        });
        set.addAll(allContacts);
        allContacts = new ArrayList<>(set);

        // 移除已注册IM的联系人，显示未注册IM的联系人
        for (int i = 0; i < allContacts.size(); i++) {
            phoneContacts.remove(allContacts.get(i).getToTelephone());
        }

        Collection<Contacts> values = phoneContacts.values();
        mContactList = new ArrayList<>(values);

        DialogHelper.showDefaulteMessageProgressDialog(this);
        try {
            AsyncUtils.doAsync(this, e -> {
                Reporter.post("加载数据失败，", e);
                AsyncUtils.runOnUiThread(this, ctx -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(ctx, R.string.data_exception);
                });
            }, c -> {
                Map<String, Integer> existMap = new HashMap<>();
                List<BaseSortModel<Contacts>> sortedList = SortHelper.toSortedModelList(mContactList, existMap, Contacts::getName);
                c.uiThread(r -> {
                    DialogHelper.dismissProgressDialog();
                    mSideBar.setExistMap(existMap);
                    mSortContactList = sortedList;
                    mContactsAdapter.setData(sortedList);
                });
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initEvent() {
        tvTitleRight.setOnClickListener(v -> isControlBatchStatus(mBatch));

        TextView shareAppTv = findViewById(R.id.share_app_tv);
        shareAppTv.setText(getString(R.string.share_app, getString(R.string.app_name)));
        findViewById(R.id.invited_friend_ll).setVisibility(View.GONE);
        findViewById(R.id.invited_friend_ll).setOnClickListener(v -> {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            if (TextUtils.isEmpty(coreManager.getConfig().website)) {
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.sms_content, getString(R.string.app_name))
                        + BuildConfig.SHARE_URL);
                shareIntent = Intent.createChooser(shareIntent, getString(R.string.sms_content, getString(R.string.app_name))
                        + BuildConfig.SHARE_URL);
            } else {
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.sms_content, getString(R.string.app_name))
                        + coreManager.getConfig().website);
                shareIntent = Intent.createChooser(shareIntent, getString(R.string.sms_content, getString(R.string.app_name))
                        + coreManager.getConfig().website);
            }
            startActivity(shareIntent);
        });

        mListView.getRefreshableView().setOnItemClickListener((parent, view, position, id) -> {
            position = (int) id;
            Contacts contact = mSortContactList.get(position).getBean();
            if (contact != null && !mBatch) {
                if (mBatchAddContacts.containsKey(contact.getTelephone())) {
                    mBatchAddContacts.remove(contact.getTelephone());
//                    isControlBatchStatus(false);
                } else {
                    mBatchAddContacts.put(contact.getTelephone(), contact);
                }
                mContactsAdapter.notifyDataSetChanged();
            }
        });

        /**
         * 确定点击事件
         */
        mBatchAddTv.setOnClickListener(v -> {
            Collection<Contacts> values = mBatchAddContacts.values();
            List<Contacts> contactList = new ArrayList<>(values);
            if (contactList.size() == 0) {
                return;
            }
            String telStr = "";
            for (int i = 0; i < contactList.size(); i++) {
                String tel = contactList.get(i).getTelephone().substring(String.valueOf(mobilePrefix).length());
                telStr += tel + ";";
            }
            sendSms(telStr);
        });
    }

    /**
     * 批量选择
     *
     * @param isChangeAll
     */
    private void isControlBatchStatus(boolean isChangeAll) {
        if (isChangeAll) {
            tvTitleRight.setText(getString(R.string.cancel));
            mBatch = false;
            mBatchAddContacts.clear();
            AnimationUtil.setVisibleChangeSize(mBatchAddTv, DisplayUtil.dip2px(ContactsMsgInviteActivity.this, 49), true);
//            isBatch = !isBatch;
//            if (isBatch) {
//                tvTitleRight.setText(getString(R.string.cancel));
//                for (int i = 0; i < mContactList.size(); i++) {
//                    mBatchAddContacts.put(mContactList.get(i).getTelephone(), mContactList.get(i));
//                }
//            } else {
//                tvTitleRight.setText(getString(R.string.select_all));
//                mBatchAddContacts.clear();
//            }
//            mContactsAdapter.notifyDataSetChanged();
        } else {
//            isBatch = false;
//            tvTitleRight.setText(getString(R.string.select_all));
            mBatch = true;
            tvTitleRight.setText("批量选择");
            AnimationUtil.setGoneChangeSize(mBatchAddTv, DisplayUtil.dip2px(ContactsMsgInviteActivity.this, 49), true);
        }
        for (int i = 0; i < mContactList.size(); i++) {
            mContactList.get(i).setSelect(mBatch);
        }
        mContactsAdapter.notifyDataSetChanged();
    }

    private void sendSms(String number) {
        Intent sendIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + number));
        if (TextUtils.isEmpty(coreManager.getConfig().website)) {
            sendIntent.putExtra("sms_body", getString(R.string.sms_content, getString(R.string.app_name))
                    + BuildConfig.SHARE_URL);
        } else {
            sendIntent.putExtra("sms_body", getString(R.string.sms_content, getString(R.string.app_name))
                    + coreManager.getConfig().website);
        }
        startActivity(sendIntent);
    }

    class ContactsAdapter extends BaseAdapter implements SectionIndexer {
        List<BaseSortModel<Contacts>> mSortContactList;

        public ContactsAdapter() {
            mSortContactList = new ArrayList<>();
        }

        public void setData(List<BaseSortModel<Contacts>> sortContactList) {
            mSortContactList = sortContactList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mSortContactList.size();
        }

        @Override
        public Object getItem(int position) {
            return mSortContactList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_contacts_msg_invite, parent, false);
            }
            TextView categoryTitleTv = ViewHolder.get(convertView, R.id.catagory_title);
            CheckBox checkBox = ViewHolder.get(convertView, R.id.check_box);
            ImageView avatarImg = ViewHolder.get(convertView, R.id.avatar_img);
            TextView contactNameTv = ViewHolder.get(convertView, R.id.contact_name_tv);
            TextView userNameTv = ViewHolder.get(convertView, R.id.user_name_tv);
            TextView invite_tv = ViewHolder.get(convertView, R.id.invite_tv);
            processingPlaceSpace(position, convertView);
            // 根据position获取分类的首字母的Char ascii值
            int section = getSectionForPosition(position);
            // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (position == getPositionForSection(section)) {
                categoryTitleTv.setVisibility(View.VISIBLE);
                categoryTitleTv.setText(mSortContactList.get(position).getFirstLetter());
            } else {
                categoryTitleTv.setVisibility(View.GONE);
            }

            final Contacts contact = mSortContactList.get(position).getBean();
            if (contact != null) {
                checkBox.setChecked(mBatchAddContacts.containsKey(contact.getTelephone()));
                AvatarHelper.getInstance().displayAddressAvatar(contact.getName(), avatarImg);
                contactNameTv.setText(contact.getName());
                // 因为存储的时候默认拼上了区号，这里将区号截掉显示
                String tel = contact.getTelephone().substring(String.valueOf(mobilePrefix).length());
                userNameTv.setText(tel);
            }


            if (mSortContactList.get(position).bean.isSelect()) {
                invite_tv.setVisibility(View.VISIBLE);
                checkBox.setVisibility(View.GONE);
            } else {
                invite_tv.setVisibility(View.GONE);
                checkBox.setVisibility(View.VISIBLE);
            }

            invite_tv.setOnClickListener(v -> {

                String telStr = "";
                String tel = contact.getTelephone().substring(String.valueOf(mobilePrefix).length());
                telStr += String.format("%s;", tel);

                sendSms(telStr);
            });

            return convertView;
        }

        private void processingPlaceSpace(int position, View convertView) {
            if (position == getCount() - 1) {
                LinearLayout llRoot = ViewHolder.get(convertView, R.id.ll_root);
                if (llRoot != null) {
                    View view = llRoot.findViewById(R.id.space);
                    if (view != null) {
                        llRoot.removeView(view);
                    }
                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(ContactsMsgInviteActivity.this, 49));
                    Space space = new Space(ContactsMsgInviteActivity.this);
                    space.setId(R.id.space);
                    space.setLayoutParams(layoutParams);
                    llRoot.addView(space);
                }
            }
        }

        @Override
        public Object[] getSections() {
            return null;
        }

        @Override
        public int getPositionForSection(int section) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = mSortContactList.get(i).getFirstLetter();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getSectionForPosition(int position) {
            return mSortContactList.get(position).getFirstLetter().charAt(0);
        }
    }

    @Override
    public void onSuccess() {
        initView();
        dataLayering();
        initEvent();
    }

    @Override
    public void onFailure(List<String> data) {
        DialogHelper.tip(this, "请开启通讯录权限");
    }
}
