package com.payeasenet.wepay.constant;


import com.iimm.miliao.MyApplication;
import com.iimm.miliao.ui.base.CoreManager;

/**
 * 类 <code>Constants</code>
 * <p>
 * 描述：
 * </p>
 * 创建日期：2019年11月14日
 *
 * @author zhaoyong.chen@ehking.com
 * @version 1.0
 */
public class Constants {


    public static  String toolBarColor = "#f3f4f6";
    public static  String buttonColor = "#FF5252";

    public static String BASE_URL = CoreManager.requireConfig(MyApplication.getInstance()).apiUrl;

    public static final String toolBarTitle = "toolBarTitle";

    public static String WHETHER_TO_OPEN =BASE_URL+"webox/account/query";


    public static String walletId = "33704400121831684";
    public static String merchantId = "890000595";
    public static final String type= "type";
    public static final  String wallet = "walletId";
    public static final String amount = "amount";

    public static final String notifyUrl = "https://sdk.5upay.com/webox/rechargeNotifyServlet";

    public static final String currency = "CNY";

    public static final String version = "3.0";

    public static String environment = "pro";
    public static final int money = 1;
    public static final boolean isTest = false;
    public static final  String env = "environment";

    public static final String memberId ="memberId";

    public static final String userID = "10000016";
}
