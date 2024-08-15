package com.iimm.miliao.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.Prefix;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.sortlist.SideBar;
import com.iimm.miliao.sortlist.SortHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.UiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Created by Administrator on 2017/4/21 0021.
 * 选择国家区号
 */
public class SelectPrefixActivity extends BaseActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    public static final int RESULT_MOBILE_PREFIX_SUCCESS = 110;
    public static final int REQUEST_MOBILE_PREFIX_LOGIN = 11123;
    private boolean isSearch = false;// 当前的状态
    private EditText searchEdt;
    private ListView mLv;
    private PrefixAdapter prefixAdapter;
    private int mobilePrefix = 86;
    private SideBar mSideBar;
    private TextView mTvDialog;
    private List<BaseSortModel<Prefix>> sortPrefixList = new ArrayList<>();
    private Map<String, Integer> mExistMap;

    public SelectPrefixActivity() {
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_act_selectaddr);
        initView();
    }

    private void initView() {
        getSupportActionBar().hide();
        ((ImageView) findViewById(R.id.iv_title_left)).setImageResource(R.drawable.icon_close_circle);
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.select_county_area));
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mTvDialog = (TextView) findViewById(R.id.text_dialog);

        mSideBar.setTextView(mTvDialog);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = prefixAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mLv.setSelection(position);
                }
            }
        });
        searchEdt = (EditText) findViewById(R.id.search_edit);
        searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable str) {
                if (str.length() > 0) {
                    search(str.toString());
                } else {
                    isSearch = false;
                    prefixAdapter.setData(sortPrefixList);
                }
            }
        });

        mLv = (ListView) findViewById(R.id.lv_addr);
        mExistMap = new HashMap<>();
        AsyncUtils.doAsync(this, c -> {
            List<Prefix> prefixList = InternationalizationHelper.getPrefixList();
            sortPrefixList.clear();
            sortPrefixList.addAll(sort(prefixList, mExistMap));
            c.uiThread(r -> {
                prefixAdapter.notifyDataSetChanged();
            });
        });
        mSideBar.setExistMap(mExistMap);
        prefixAdapter = new PrefixAdapter(sortPrefixList);
        mLv.setAdapter(prefixAdapter);
        mLv.setOnItemClickListener(this);
    }

    private List<BaseSortModel<Prefix>> sort(List<Prefix> prefixList, Map<String, Integer> existMap) {
        List<BaseSortModel<Prefix>> sortedList = SortHelper.toSortedModelList(prefixList, existMap, Prefix::getCountry);
        BaseSortModel<Prefix> prefixBaseSortModel = null;
        for (BaseSortModel<Prefix> prefix : sortedList) {
            if (prefix.getBean().getCountry().contains("中国")) {
                prefixBaseSortModel = prefix;
                sortedList.remove(prefix);
            }
        }
        if (prefixBaseSortModel != null) {
            sortedList.add(0, prefixBaseSortModel);
        }
        return sortedList;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        BaseSortModel<Prefix> prefixbean = (BaseSortModel<Prefix>) prefixAdapter.getItem(i);
        int prefix = prefixbean.getBean().getPrefix();
        sendResult(prefix);
    }

    @Override
    public void onClick(View view) {
        if (!UiUtils.isNormalClick(view)) {
            return;
        }
        switch (view.getId()) {
            case R.id.title_iv_back:
                back();
                break;
        }
    }

    private void back() {
        if (isSearch) {
            isSearch = false;
            prefixAdapter.setData(sortPrefixList);
        } else {
            finish();
        }
    }

    private void search(String key) {
        if (!TextUtils.isEmpty(key)) {
            // 变为搜索状态
            isSearch = true;
            List<Prefix> searchResult = InternationalizationHelper.getSearchPrefix(key);
            prefixAdapter.setData(sort(searchResult, mExistMap));
        }
    }

    private void sendResult(int mobliePrefix) {
        Intent intent = new Intent();
        intent.putExtra(Constants.MOBILE_PREFIX, mobliePrefix);
        setResult(RESULT_MOBILE_PREFIX_SUCCESS, intent);
        finish();
    }

    private class PrefixAdapter extends BaseAdapter implements SectionIndexer {
        private List<BaseSortModel<Prefix>> data;

        public PrefixAdapter(List<BaseSortModel<Prefix>> data) {
            this.data = data;
        }

        public void setData(List<BaseSortModel<Prefix>> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(mContext, R.layout.a_item_resume_fnid, null);
                holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title_fnid_name);
                holder.tvPinYin = convertView.findViewById(R.id.tv_pin_yin);
                holder.tvAreaCode = convertView.findViewById(R.id.tv_area_code);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvAreaCode.setText("+" + data.get(position).getBean().getPrefix());
            if (Locale.getDefault().getLanguage().equals("en")) {
                holder.tvTitle.setText(data.get(position).getBean().getEnName());
            } else {
                holder.tvTitle.setText(data.get(position).getBean().getCountry());
            }
            if (position == 0) {
                if (!isSearch) {
                    holder.tvPinYin.setVisibility(View.VISIBLE);
                    holder.tvPinYin.setText(R.string.toleration);
                } else {
                    holder.tvPinYin.setVisibility(View.GONE);
                    holder.tvPinYin.setText(R.string.toleration);
                }
            } else {
                if (data.get(position - 1).getFirstLetter().equals(data.get(position).getFirstLetter())) {
                    holder.tvPinYin.setVisibility(View.GONE);
                } else {
                    if (!isSearch) {
                        holder.tvPinYin.setVisibility(View.VISIBLE);
                        holder.tvPinYin.setText(data.get(position).getFirstLetter());
                    } else {
                        holder.tvPinYin.setVisibility(View.GONE);
                    }

                }
            }
            return convertView;
        }

        @Override
        public Object[] getSections() {
            return null;
        }

        /**
         * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
         */
        public int getPositionForSection(int section) {
            for (int i = 0; i < getCount(); i++) {
                if (i == 0) {
                    continue;
                }
                String sortStr = data.get(i).getFirstLetter();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * 根据ListView的当前位置获取分类的首字母的Char ascii值
         */
        public int getSectionForPosition(int position) {
            return data.get(position).getFirstLetter().charAt(0);
        }

        private class ViewHolder {
            TextView tvTitle;
            TextView tvPinYin;
            TextView tvAreaCode;
        }
    }
}

