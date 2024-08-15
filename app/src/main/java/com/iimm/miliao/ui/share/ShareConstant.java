package com.iimm.miliao.ui.share;

/**
 * 登录、分享sdk
 */
public class ShareConstant {
    // 与外部通信使用
    public static final String EXTRA_SHARE_CONTENT = "extra_share_content";
    public static final String EXTRA_AUTHORIZATION_RESULT = "extra_authorization_result";

    //交互数据
    public static final String TIG_TYPE = "tig_type";
    public static final String TIG_NAME = "tig_name";
    public static final String TIG_APP_ID = "tig_app_id";
    public static final String TIG_APP_SECRET = "tig_app_secret";
    public static final String TIG_BUNDLE = "tig_bundle";
    public static final String TIG_CALLBACK_URL = "tig_callbackURL";
    public static final String TIG_IMAGE = "tig_image";
    public static final String TIG_TITLE = "tig_title";
    public static final String TIG_SUBTITLE = "tig_subtitle";
    public static final String TIG_URL = "tig_url";
    public static final String TIG_IMAGE_URL = "tig_image_url";

    // 仅限本地使用
    public static boolean IS_SHARE_L_COME = false;
    public static boolean IS_SHARE_S_COME = false;

    public static String ShareContent;

    // 外部浏览器调起 授权、支付
    public static boolean IS_SHARE_QL_COME = false;
    public static boolean IS_SHARE_QP_COME = false;
}
