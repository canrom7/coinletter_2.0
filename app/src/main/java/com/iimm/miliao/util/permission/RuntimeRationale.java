package com.iimm.miliao.util.permission;

import android.content.Context;

import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;

import java.util.List;

/**
 * MrLiu253@163.com
 *
 * @time 2018/8/13
 */

public class RuntimeRationale implements Rationale<List<String>> {

    @Override
    public void showRationale(Context context, List<String> permissions, final RequestExecutor executor) {
        executor.execute();
    }
}

