package com.iimm.miliao.util.permission;

import java.util.List;

/**
 * MrLiu253@163.com
 *
 * @time 2019-08-27
 */
public interface OnPermissionClickListener {

    void onSuccess();

    void onFailure(List<String> data);

}
