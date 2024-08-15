package com.iimm.miliao.util;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by 魏正旺 on 2016/9/9.
 * <p>
 * 用于监听红包和充值的输入字符，检测输入的
 * 数字只能是小数点后两位
 */
public class InputChangeListenerNew implements TextWatcher {
    private EditText editRecharge;
    private TextView money_tv;

    public InputChangeListenerNew(EditText editRecharge, TextView money_tv) {
        this.editRecharge = editRecharge;
        this.money_tv = money_tv;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before,
                              int count) {
        if (s.toString().contains(".")) {
            if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                s = s.toString().subSequence(0,
                        s.toString().indexOf(".") + 3);
                editRecharge.setText(s);
                editRecharge.setSelection(s.length());
                money_tv.setText(String.format("￥%s", s));
            }
        }
        if (s.toString().trim().substring(0).equals(".")) {
            s = "0" + s;
            editRecharge.setText(s);
            editRecharge.setSelection(2);
            money_tv.setText(String.format("￥%s", s));
        }

        if (s.toString().startsWith("0")
                && s.toString().trim().length() > 1) {
            if (!s.toString().substring(1, 2).equals(".")) {
                editRecharge.setText(s.subSequence(0, 1));
                editRecharge.setSelection(1);
                money_tv.setText(String.format("￥%s", s.subSequence(0, 1)));
                return;
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        // TODO Auto-generated method stub
        String str = editRecharge.getText().toString();
        if (TextUtils.isEmpty(str)) {
            money_tv.setText("￥0.00");
        } else {
            money_tv.setText(String.format("￥%s", str));
        }

    }
}
