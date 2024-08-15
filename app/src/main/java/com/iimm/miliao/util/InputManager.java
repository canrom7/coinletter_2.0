package com.iimm.miliao.util;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2016/4/10.
 */
public class InputManager {

    public static InputMethodManager getImm(Context mContext) {
        return (InputMethodManager) mContext.getSystemService(mContext.INPUT_METHOD_SERVICE);
    }

    public static void hideImm(Context mContext, View v) {
        InputMethodManager imm = InputManager.getImm(mContext);
        if (imm.isActive())
            imm.hideSoftInputFromInputMethod(v.getApplicationWindowToken(), 0);
    }

    public static void backSpaceChatEdit(EditText mChatEdit) {
        int index = -1;
        String str = mChatEdit.getText().toString();
        if (mChatEdit.hasFocus()) {
            index = mChatEdit.getSelectionStart();
        } else {
            index = str.length();
        }

        if (!TextUtils.isEmpty(str) && index > 0) {
            String sss = str.substring(0, index);
            if (sss.substring(index - 1, index).equals("]")) {
                int st = sss.lastIndexOf("[");
                mChatEdit.getText().delete(st, index);
            } else {
                mChatEdit.getText().delete(index - 1, index);
            }
        }
    }

    public static void backSpaceChatEdit(EditText mChatEdit, LinkedList<String> sData, boolean isGroup, List<String> mNameList) {
        int index = -1;
        String str = mChatEdit.getText().toString();
        if (mChatEdit.hasFocus()) {
            index = mChatEdit.getSelectionStart();
        } else {
            index = str.length();
        }

        if (!TextUtils.isEmpty(str) && index > 0) {
            String sss = str.substring(0, index);
            if (sss.substring(index - 1, index).equals("]")) {
                int st = sss.lastIndexOf("[");
                mChatEdit.getText().delete(st, index);
            } else if (isGroup && sData != null && sData.size() > 0 && mNameList != null && mNameList.size() > 0 && mNameList.toString().length() >= 2 && str.length() >= 2 && mNameList.toString().contains(str.substring(str.length() - 2))) {
                int at = sss.lastIndexOf("@");
                mChatEdit.getText().delete(at, index);
                try {
                    if (sData != null && sData.size() > 0) {
                        sData.removeLast();
                    }
                } catch (Exception e) {

                }
            } else {
                mChatEdit.getText().delete(index - 1, index);
            }
        }
    }
}
