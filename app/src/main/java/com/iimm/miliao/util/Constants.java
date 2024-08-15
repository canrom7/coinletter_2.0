package com.iimm.miliao.util;

import android.os.Environment;

import com.iimm.miliao.AppConfig;
import com.iimm.miliao.BuildConfig;

import java.io.File;

public class Constants {

    public static final String SELCTORPROXY = "proxy";   //选择节点
    public static final String FILE_PROVIDER_AUTHORITY = ".fileProvider";//必须和清单文件中 android.support.v4.content.FileProvider  的 authorities 保持一致
    public static final String VX_APP_ID = BuildConfig.WX_APP_KEY;  //微信AppID
    public static final String SHARE_URL = BuildConfig.SHARE_URL;     //分享链接
    public static final boolean SUPPORT_GROUP_SIGN = true;//是否支持显示群签到
    public static final boolean SUPPORT_FIND = true;//是否支持显示发现页面
    //public static final boolean SUPPORT_SHARE_QQ = false;//是否支持QQ分享
    //public static final boolean SUPPORT_SHARE_WECHAT = false;//是否支持微信分享
    //public static final boolean SUPPORT_LOCATION = false;     //是否支持位置。如果不支持，去掉聊天界面 更多功能中的 位置
//    public static final boolean SUPPORT_VOICE_AND_VIDEO = false;     //是否支持音视频通话，音视频会议。如果不支持，去掉聊天界面 更多功能中的 音视频相关项
//    public static final boolean SUPPORT_MONEY = true;     //是否支持 红包，转账。如果不支持，去掉聊天界面 更多功能中的 红包，转账
//    public static final boolean SUPPORT_READ_DEL = true;     //是否支持 阅后即焚，如果三段都上了这个新功能，且服务端也配置了，才会开启。
    public static final boolean SUPPORT_VOICE_SWICH = true;//是否支持聊天界面听筒扬声器切换
    public static final boolean SUPPORT_VOICE_CALLS_SWICH = true;//是否支持单聊时语音通话听筒扬声器切换
    public static final boolean SUPPORT_READ_PERSON_COUNT = false;//群是否显示已读人数
    public static final boolean SUPPORT_READ_PRIVILEGE_RED = false;//是否支持群的特权红包
    public static final boolean SUPPORT_SCROLL_TO_AT = false;//是否支持 有被@且未读消息时，自动跳到@消息
    public static final boolean IS_OPEN_HTTPS = false;//是否开启https 认证
    public static final boolean SUPPORT_NEW_CONTACT_UI = true;//是否是新的通讯录界面
    public static final String HTTPS_P12_PASSWORD = "chat.im"; //https p12 转 bks 密码
    public static final boolean SUPPORT_CASH_BACK = true;//是否使用新的提现后台
    public static final boolean SUPPORT_GROUP_BG = false;//是否支持群聊背景图片设置
    public static final boolean SUPPORT_OBS_LOG = false;//是否支持上传OBS日志
    public static final boolean SUPPORT_MANUAL_NODE = true;//是否使用手动节点
    public static final boolean SUPPORT_EXCLUSIVE_RED = false;//是否发红包时候，可以选择人数
    public static final boolean SUPPORT_YOU_JOB = true;//是否统一使用youjob
    public static final boolean SUPPORT_COMPRESSION = true;//是否开启压缩
    public static final boolean SUPPORT_FORCED_ACQUISITION = false;//是否强制获取
    public static final boolean SUPPORT_SECOND_CHANNEL = true;//是否开启第二通
    public static final boolean SUPPORT_BIDIRECTIONAL_WITHDRAW = true;//是否开启双向撤回通道
    public static final boolean SUPPORT_GROUP_NICKNAME = false;//是否开启实时变更群昵称
    public static final boolean SUPPORT_CONTACT_CUSTOMER_SERVICE= false;//是否开启联系客服
    public static final boolean SUPPORT_RECEIPT= false;//是否开启强制发送回执
    public static final boolean SUPPORT_DISCOVER_SEARCH= false;//是否开启朋友圈搜索
    public static final boolean SUPPORT_FLOATING_WINDOW= false;//是否开启全局悬浮窗
    public static final boolean SUPPORT_LITTLE_HELPER= false;//是否开启群内小助手

    public static final String NOT_AUTHORIZED = AppConfig.sPackageName + "not_authorized";// XMPP登录时密码错误(密码与服务端不匹配)
    public static final String PING_FAILED = AppConfig.sPackageName + "ping_failed";// XMPP Ping服务器ping失败了
    public static final String CLOSED_ON_ERROR_NORMAL = AppConfig.sPackageName + "closed_on_error_normal";// XMPP回调了connectionClosedOnError
    public static final String CLOSED_ON_ERROR_END_DOCUMENT = AppConfig.sPackageName + "closed_on_error_end_document";// XMPP回调了connectionClosedOnError 并且Exception为Parser got END_DOCUMENT event. This could happen e.g. if the server closed the connection without sending a closing stream element
    public static final String FILE_DOWN_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "download_temp";

    public static int DEFAULT_TEXT_SIZE = 16;//单位 sp
    public static int LOGIN_NODE_SUPPORT = 1;//支持多节点登录 0 不支持
    /*
    Other
     */
    // 国家区号
    public static final String MOBILE_PREFIX = "MOBILE_PREFIX";
    // 登录冲突，否，退出app，记录，下次进入历史登录界面
    public static final String LOGIN_CONFLICT = "login_conflict";
    // 当前设备离线时间
    public static final String OFFLINE_TIME = "offline_time";
    public static final String IS_FRIEND_ADD = "is_friend_add";//是否允许添加好友
    public static final String IS_CREATE_ROOM = "is_create_room";//是否允许此账号创建群组 isCreateRoom
    // App启动次数
    public static final String APP_LAUNCH_COUNT = "app_launch_count";
    public static final String IS_AUDIO_CONFERENCE = "is_audio_conference";
    public static final String LOCAL_CONTACTS = "local_contacts";
    public static final String NEW_CONTACTS_NUMBER = "new_contacts_number";
    public static final String NEW_CONTACTS_IDS = "new_contacts_ids";
    // 新消息数量
    public static final String NEW_MSG_NUMBER = "new_msg_number";
    // 通知栏进入
    public final static String IS_NOTIFICATION_BAR_COMING = "is_notification_bar_coming";
    // 刷新"消息"角标
    public final static String NOTIFY_MSG_SUBSCRIPT = AppConfig.sPackageName + "notify_msg_subscript";
    public final static String AREA_CODE_KEY = "areCode";
    public final static String UPDATE_ROOM = AppConfig.sPackageName + "update_room";
    public final static String BROWSER_SHARE_MOMENTS_CONTENT = "browser_share_moments_content";
    /*
    Chat Publish
     */
    // 最近一张屏幕截图的路径
    public final static String SCREEN_SHOTS = "screen_shots";
    // 删除
    public final static String CHAT_MESSAGE_DELETE_ACTION = AppConfig.sPackageName + "chat_message_delete";
    public final static String CHAT_REMOVE_MESSAGE_POSITION = "CHAT_REMOVE_MESSAGE_POSITION";
    // 多选
    public final static String SHOW_MORE_SELECT_MENU = AppConfig.sPackageName + "show_more_select_menu";
    public final static String CHAT_SHOW_MESSAGE_POSITION = "CHAT_SHOW_MESSAGE_POSITION";
    public final static String IS_MORE_SELECTED_INSTANT = "IS_MORE_SELECTED_INSTANT";// 是否为多选转发
    public final static String IS_SINGLE_OR_MERGE = "IS_SINGLE_OR_MERGE";// 逐条还是合并转发
    // 单、群聊 清空聊天记录
    public final static String CHAT_HISTORY_EMPTY = AppConfig.sPackageName + "chat_history_empty";
    // 更新消息过期时间的通知
    public final static String CHAT_TIME_OUT_ACTION = AppConfig.sPackageName + "chat_time_out_action";
    /*
    Person Set
     */
    // 阅后即焚
    public final static String MESSAGE_READ_FIRE = "message_read_fire";
    // 聊天背景
    public final static String SET_CHAT_BACKGROUND = "chat_background";
    public final static String SET_CHAT_BACKGROUND_PATH = "chat_background_path";
    /*
    Group Set
     */
    public final static String GROUP_JOIN_NOTICE = "group_join_notice";
    // 屏蔽群组消息
    public final static String SHIELD_GROUP_MSG = "shield_group_msg";
    // 全体禁言
    public final static String GROUP_ALL_SHUP_UP = "group_all_shut_up";
    // 是否开启群已读
    public final static String IS_SHOW_READ = "is_show_read";
    //是否允许普通群成员私聊
    public final static String IS_SEND_CARD = "is_send_card";
    // 是否允许普通成员召开会议
    public final static String IS_ALLOW_NORMAL_CONFERENCE = "is_allow_normal_conference";
    // 是否允许普通成员发送讲课
    public final static String IS_ALLOW_NORMAL_SEND_COURSE = "is_allow_normal_send_course";
    // 是否需要群主确认进群
    public final static String IS_NEED_OWNER_ALLOW_NORMAL_INVITE_FRIEND = "is_need_owner_allow_normal_invite_friend";
    // 是否允许普通成员发送文件、上传群共享
    public final static String IS_ALLOW_NORMAL_SEND_UPLOAD = "is_allow_normal_send_upload";
    //是否允许此群发送强提醒
    public static final String IS_ALLOW_SEND_STRONG_REMIND = "is_allow_send_strong_remind_";
    //    是否开启群签到
    public static final String IS_GROUP_SIGN = "is_group_sign";
    /*
    Set
     */
    // 字体大小
    public final static String FONT_SIZE_TYPE = "font_size_type";
    public final static String IS_PAY_PASSWORD_SET = "isPayPasswordSet";
    @SuppressWarnings("WeakerAccess")
    public static final String KEY_SKIN_COLOR = "KEY_SKIN_COLOR";
    /*
    收款 设置的金额与转账说明
     */
    public final static String RECEIPT_SETTING_MONEY = "receipt_setting_money";
    public final static String RECEIPT_SETTING_DESCRIPTION = "receipt_setting_description";
    // 与服务器的时间差，用于校准时间，
    public static final String KEY_TIME_DIFFERENCE = "KEY_TIME_DIFFERENCE";
    public static boolean IS_CLOSED_ON_ERROR_END_DOCUMENT;
    public static boolean OFFLINE_TIME_IS_FROM_SERVICE = false;// 离线时间是否为服务端获取的
    public static boolean IS_SENDONG_COURSE_NOW = false;// 现在是否正在发送课程
    // 群成员分页
    public static String MUC_MEMBER_PAGE_SIZE = "500";
    public static String MUC_MEMBER_SIZE = "20000";
    public static String MUC_MEMBER_SIZE_GROUP = "2000";
    public static String MUC_MEMBER_SHOW_SIZE = "10";
    public static String MUC_MEMBER_LAST_JOIN_TIME = "muc_member_last_join_time";

    public static final int CIRCLE_AVATAR = 0;//圆形头像
    public static final int SQUARE_AVATAR = 1;//方形头像
    public static int AVATAR_TYPE = SQUARE_AVATAR;
    public static final String DEVICE_ANDROID = "1";
    public static final int UPDATE_FORCE = 1;
    public static final float AVATAR_CORNER_RADIUS = 5.0f;
    public static final int TENTENT_COS = 2;//腾讯COS
    public static final int HUIWEI_OBS = 1;//华为OBS

    // 消息漫游条数
    public static int MSG_ROMING_PAGE_SIZE = 50;

    public static final String TYPE_XMPP_ACTION = "type_xmpp_action";
    public static final String TYPE_XMPP_CONNECT = "xmpp_connect";
    public static final String TYPE_XMPP_LOGIN = "xmpp_login";
    public static final String TYPE_XMPP_JOIN_GROUP = "xmpp_join_group";
    public static final String TYPE_XMPP_AUTO_LOGIN = "xmpp_auto_login";
    public static String XMPP_ACCOUNT = "xmpp_account";
    public static String XMPP_PASSWORD = "xmpp_password";
    public static String XMPP_RESOURCE = "xmpp_resource";
    public static String XMPP_DOMAIN = "xmpp_domain";
    public static String XMPP_HOSTNAME = "xmpp_hostname";
    public static String XMPP_PORT = "xmpp_port";

    public static final String ID_SYSTEM_MESSAGE = "10000";// 系统消息ID
    public static final String ID_NEW_FRIEND_MESSAGE = "10001";// 新朋友消息 ID
    public static final String ID_SK_PAY = "1100";// 支付公众号，
    public static final String ID_BLOG_MESSAGE = "10002";// 商务圈消息ID
    public static final String ID_INTERVIEW_MESSAGE = "10004";// 面试中心ID（用于职位、初试、面试的推送）
    public static final String ID_SYSTEM_NOTIFICATION = "10005";// 系统号，用于各种控制消息通知，
    // -1:黑名单；0：陌生人；1:单方关注；2:互为好友；8:显示系统号；9:非显示系统号
    public static final int STATUS_BLACKLIST = -1;// 黑名单
    public static final int STATUS_UNKNOW = 0;// 陌生人(不可能出现在好友表，只可能在新朋友消息表)
    public static final int STATUS_ATTENTION = 1;// 关注
    public static final int STATUS_FRIEND = 2;// 好友
    public static final int STATUS_SYSTEM = 8;// 显示系统号
    // 需要验证的
    public static final int STATUS_10 = 10; //显示  等待验证
    public static final int STATUS_11 = 11; //您好
    public static final int STATUS_12 = 12; //已通过验证
    public static final int STATUS_13 = 13; //验证被通过了
    public static final int STATUS_14 = 14; //别人回话
    public static final int STATUS_15 = 15; //回话
    public static final int STATUS_16 = 16; //已删除了XXX
    public static final int STATUS_17 = 17; //XXX删除了我
    public static final int STATUS_18 = 18; //已拉黑了XXX
    public static final int STATUS_19 = 19; //XXX拉黑了我
    public static final int STATUS_20 = 20; //默认值什么都不显示
    // 不需要验证的
    public static final int STATUS_21 = 21;//XXX 添加你为好友
    public static final int STATUS_22 = 22;//你添加好友 XXX
    public static final int STATUS_24 = 24;//XXX 已经取消了黑名单
    public static final int STATUS_23 = 23;//对方把我加入了黑名单
    public static final int STATUS_25 = 25;//通过手机联系人添加
    public static final int STATUS_26 = 26;//被后台删除的好友，仅用于新的朋友页面显示，

    public static final int MSG_CONNECTING = 0;// 连接中...
    public static final int MSG_CONNECTED = 1;// 已连接
    public static final int MSG_AUTHENTICATED = 2;// 已认证
    public static final int MSG_CONNECTION_CLOSED = 3;// 连接关闭
    public static final int MSG_CONNECTION_CLOSED_ON_ERROR = 4;// 连接错误

    public static final int MESSAGE_SEND_ING = 0;     // 发送中
    public static final int MESSAGE_SEND_SUCCESS = 1; // 发送成功
    public static final int MESSAGE_SEND_FAILED = 2;  // 发送失败

    //群成员角色
    public static final int ROLE_OWNER = 1;
    public static final int ROLE_MANAGER = 2;
    public static final int ROLE_MEMBER = 3;
    public static final int ROLE_INVISIBLE = 4;
    public static final int ROLE_GUARDIAN = 5;


    ////////////////////////////以下为在聊天界面显示的类型/////////////////////////////////
    public static final int TYPE_TEXT = 1; // 文字
    public static final int TYPE_IMAGE = 2;// 图片
    public static final int TYPE_VOICE = 3;// 语音
    public static final int TYPE_LOCATION = 4; // 位置
    public static final int TYPE_GIF = 5;  // gif
    public static final int TYPE_VIDEO = 6;// 视频
    public static final int TYPE_SIP_AUDIO = 7;// 音频
    public static final int TYPE_CARD = 8;// 名片
    public static final int TYPE_FILE = 9;// 文件
    public static final int TYPE_TIP = 10;// 自己添加的消息类型,代表系统的提示
    public static final int TYPE_REPLAY = 94;// 回复，

    public static final int TYPE_READ = 26;    // 是否已读的回执类型
    public static final int TYPE_READ_EXCLUSIVE = 27;//专属红包
    public static final int TYPE_RED = 28;// 红包消息
    public static final int TYPE_TRANSFER = 29;// 转账消息
    public static final int TYPE_IMAGE_TEXT = 80;     // 单条图文消息
    public static final int TYPE_IMAGE_TEXT_MANY = 81;// 多条图文消息
    public static final int TYPE_LINK = 82; // 链接
    public static final int TYPE_SHARE_LINK = 87; // 分享进来的链接
    public static final int TYPE_83 = 83;// 某个成员领取了红包
    public static final int TYPE_SHAKE = 84;// 戳一戳
    public static final int TYPE_CHAT_HISTORY = 85;// 聊天记录
    public static final int TYPE_RED_BACK = 86;// 红包退回通知
    public static final int TYPE_TRANSFER_RECEIVE = 88;// 转账已被领取

    public static final int TYPE_CLOUD_RED = 2011;// 云红包消息
    public static final int TYPE_CLOUD_RECEIVE_RED = 2012;// 云红包某成员领取了红包
    public static final int TYPE_CLOUD_BACK_RED = 2013;// 云红包退回通知
    public static final int TYPE_CLOUD_TRANSFER = 2021;//微转账
    public static final int TYPE_CLOUD_TRANSFER_RECEIVE = 2022;//微转账领取
    public static final int TYPE_CLOUD_TRANSFER_RETURN = 2023;//微转账退回

    public static final int TYPE_TRANSFER_BACK = 89;// 转账已退回
    public static final int TYPE_PAYMENT_OUT = 90;// 付款码-已付款通知
    public static final int TYPE_RECEIPT_OUT = 92;// 收款码-已付款通知
    public static final int TYPE_PAYMENT_GET = 91;// 付款码-已到账通知
    public static final int TYPE_RECEIPT_GET = 93;// 收款码-已到账通知

    public static final int TYPE_SCREENSHOT = 95;// 对方ios截图，阅后即焚的时候，
    public static final int TYPE_SYNC_CLEAN_CHAT_HISTORY = 96;// 双向清除聊天记录

    public static final int TYPE_RECHARGE_GET = 98;  //银行卡  充值 结果 通知
    public static final int TYPE_RECHARGE_H5_GET = 99; //H5充值

    public static final int TYPE_SEND_ONLINE_STATUS = 200;// 在线情况
    public static final int TYPE_INPUT = 201;// 正在输入消息
    public static final int TYPE_BACK = 202; // 撤回消息
    public static final int TYPE_BACKSTAGE_DELETE = 203;//删除本地消息

    ////////////////////////////音视频通话/////////////////////////////////
    public static final int TYPE_IS_CONNECT_VOICE = 100;// 发起语音通话
    public static final int TYPE_CONNECT_VOICE = 102;// 接听语音通话
    public static final int TYPE_NO_CONNECT_VOICE = 103;// 拒绝语音通话 || 对来电不响应(30s)内
    public static final int TYPE_END_CONNECT_VOICE = 104;// 结束语音通话

    public static final int TYPE_IS_CONNECT_VIDEO = 110;// 发起视频通话
    public static final int TYPE_CONNECT_VIDEO = 112;// 接听视频通话
    public static final int TYPE_NO_CONNECT_VIDEO = 113;// 拒绝视频通话 || 对来电不响应(30s内)
    public static final int TYPE_END_CONNECT_VIDEO = 114;// 结束视频通话

    public static final int TYPE_IS_MU_CONNECT_VIDEO = 115;// 视频会议邀请
    public static final int TYPE_IS_MU_CONNECT_VOICE = 120;// 音频会议邀请

    public static final int TYPE_IN_CALLING = 123;// 通话中...
    public static final int TYPE_IS_BUSY = 124;// 忙线中...

    // 暂未用到
    public static final int TYPE_VIDEO_IN = 116;// 视频会议进入
    public static final int TYPE_VIDEO_OUT = 117;// 视频会议退出
    public static final int TYPE_OK_MU_CONNECT_VOICE = 121;// 音频会议进入了
    public static final int TYPE_EXIT_VOICE = 122;// 音频会议退出了

    ////////////////////////////朋友圈消息/////////////////////////////////
    public static final int DIANZAN = 301; // 朋友圈点赞
    public static final int PINGLUN = 302; // 朋友圈评论
    public static final int ATMESEE = 304; // 提醒我看

    ////////////////////////////新朋友消息/////////////////////////////////
    public static final int TYPE_SAYHELLO = 500;// 打招呼
    public static final int TYPE_PASS = 501;    // 同意加好友
    public static final int TYPE_FEEDBACK = 502;// 回话
    public static final int TYPE_FRIEND = 508;// 直接成为好友
    public static final int TYPE_BLACK = 507; // 黑名单
    public static final int TYPE_REFUSED = 509;// 取消黑名单
    public static final int TYPE_DELALL = 505; // 彻底删除
    public static final int TYPE_CONTACT_BE_FRIEND = 510;// 对方通过 手机联系人 添加我 直接成为好友
    public static final int TYPE_NEW_CONTACT_REGISTER = 511;// 我之前上传给服务端的联系人表内有人注册了，更新 手机联系人
    public static final int TYPE_REMOVE_ACCOUNT = 512;// 用户被后台删除，用于客户端更新本地数据 ，from是系统管理员 to是被删除人的userId，
    public static final int TYPE_BACK_BLACK = 513;// 好友用户被后台拉黑，用于客户端更新本地数据 ，from是系统管理员 to是被删除人的userId，
    public static final int TYPE_BACK_REFUSED = 514;// 好友用户被后台取消拉黑，用于客户端更新本地数据 ，from是系统管理员 to是自己的userId，objectId中有拉黑双方Id,
    public static final int TYPE_BACK_DELETE = 515;// 好友用户被后台删除，是删除好友关系 ，from是系统管理员 to是自己的userId，objectId中有删除双方Id,

    // 未用到
    public static final int TYPE_NEWSEE = 503;// 新关注
    public static final int TYPE_DELSEE = 504;// 删除关注
    public static final int TYPE_RECOMMEND = 506;// 新推荐好友

    ////////////////////////////群组协议/////////////////////////////////
    public static final int TYPE_MUCFILE_ADD = 401; // 群文件上传
    public static final int TYPE_MUCFILE_DEL = 402; // 群文件删除
    public static final int TYPE_MUCFILE_DOWN = 403;// 群文件下载

    public static final int TYPE_CHANGE_NICK_NAME = 901; // 修改昵称
    public static final int TYPE_CHANGE_ROOM_NAME = 902; // 修改房间名
    public static final int TYPE_DELETE_ROOM = 903;// 删除房间
    public static final int TYPE_DELETE_MEMBER = 904;// 退出、被踢出群组
    public static final int TYPE_NEW_NOTICE = 905; // 新公告
    public static final int TYPE_GAG = 906;// 禁言/取消禁言 个人禁言
    public static final int NEW_MEMBER = 907; // 增加新成员
    public static final int TYPE_SEND_MANAGER = 913;// 设置/取消管理员

    public static final int TYPE_CHANGE_SHOW_READ = 915; // 设置群已读消息
    public static final int TYPE_GROUP_VERIFY = 916; // 群组验证消息
    public static final int TYPE_GROUP_LOOK = 917; // 群组是否公开
    public static final int TYPE_GROUP_SHOW_MEMBER = 918; // 群组是否显示群成员列表
    public static final int TYPE_GROUP_SEND_CARD = 919; // 群组是否允许发送名片
    public static final int TYPE_GROUP_ALL_SHAT_UP = 920; // 全体禁言/取消全体禁言
    public static final int TYPE_GROUP_ALLOW_NORMAL_INVITE = 921; // 允许普通成员邀请人入群
    public static final int TYPE_GROUP_ALLOW_NORMAL_UPLOAD = 922; // 允许普通成员上传群共享
    public static final int TYPE_GROUP_ALLOW_NORMAL_CONFERENCE = 923; // 允许普通成员发起会议
    public static final int TYPE_GROUP_ALLOW_NORMAL_SEND_COURSE = 924;// 允许普通成员发送讲课
    public static final int TYPE_GROUP_TRANSFER = 925; // 转让群组

    ////////////////////////////直播协议/////////////////////////////////
    public static final int TYPE_SEND_DANMU = 910;// 弹幕
    public static final int TYPE_SEND_GIFT = 911; // 礼物
    public static final int TYPE_SEND_HEART = 912;// 点赞
    public static final int TYPE_SEND_ENTER_LIVE_ROOM = 914;// 加入直播间
    public static final int TYPE_GROUP_SIGN = 934;// 加入直播间
    // 以前直播间和群组共用了部分协议，现独立出来
    public static final int TYPE_LIVE_LOCKING = 926; // 锁定直播间(后台可锁定用户直播间)
    public static final int TYPE_LIVE_EXIT_ROOM = 927;// 退出、被踢出直播间
    public static final int TYPE_LIVE_SHAT_UP = 928;// 禁言/取消禁言
    public static final int TYPE_LIVE_SET_MANAGER = 929;// 设置/取消管理员

    public static final int TYPE_UPDATE_ROLE = 930;// 设置/取消隐身人，监控人，
    public static final int TYPE_DISABLE_GROUP = 931;// 群组被后台锁定/解锁
    public static final int TYPE_FACE_GROUP_NOTIFY = 933;// 面对面建群有人加入、退出
    // 多端同步
    public static final int TYPE_CHANGE_PASSWORD = 1000;
    public static final int TYPE_SET_PAY_PASSWORD_FIRST = 1001;
    public static final int TYPE_UPDATE_PRIVATE_SETTINGS = 1002;
    public static final int TYPE_CURRENT_USER_INFO_UPDATE = 2000;// 当前账号信息发生改变，需多端同步
    public static final int TYPE_FRIEND_INFO_UPDATE = 3000;// 对好友的信息（备注、描述）发生改变，需多端同步
    public static final int TYPE_FRIEND_LABEL_INFO_UPDATE = 3001;// 对好友的信息（标签）发生改变，需多端同步
    public static final int TYPE_FRIEND_OTHER_INFO_UPDATE = 3002;// 对好友的信息（免打扰、消息过期时间）发生改变，需多端同步
    public static final int TYPE_FRIEND_READDEL_UPDATE = 3003;// 对好友的信息 阅后即焚 发生改变，需多端同步
    public static final int TYPE_FRIEND_ADD_DELETE = 3004;// 同意好友申请，需其他端同步
    public static final int TYPE_CUSTOM_CHANGE_READ_DEL_TIME = 9900; //99 开头的自己定义的消息 只在此端使用

    //userType 用户类型
    public static final int USER_ROLE_NORMAL = 1;  // 普通成员
    public static final int USER_ROLE_GONG_ZHONG_HAO = 2;  // 公众号
    public static final int USER_ROLE_JI_QI_REN = 3;  // 机器人
    public static final int USER_ROLE_KE_FU = 4;  // 客服
    /**
     * 消息服务状态 EventBus 消息
     */
    public static final int EVENTBUS_MSG_AUTH_NOT = 1000;   //未登录
    public static final int EVENTBUS_MSG_SLECTOR_PROXY = 100001;   //未登录
    public static final int EVENTBUS_MSG_AUTH_CONNECT_ING = 1001; // 连接中
    public static final int EVENTBUS_MSG_AUTH_CONNECTED = 1002; // 已连接
    public static final int EVENTBUS_MSG_AUTH_LOGIN_ING = 1003; // 登录中
    public static final int EVENTBUS_MSG_AUTH_SUCCESS = 1004;  // 登录成功

    public static final int EVENTBUS_MSG_ACTION_LOGIN = 1005;  // 登录
    public static final int EVENTBUS_MSG_ACTION_LOGOUT = 1006;  // 退出
    public static final int EVENTBUS_MSG_ACTION_NEED_UPDATE = 1007;  // 需要更新
    public static final int EVENTBUS_MSG_ACTION_LOGIN_GIVE_UP = 1008;  // 放弃登录
    public static final int EVENTBUS_MSG_ACTION_CONFLICT = 1009;  // 登录冲突
    public static final int EVENTBUS_MSG_CURRENT_USER_INFO_UPDATE = 1010;  // 用户个人信息改变的消息
    public static final int EVENTBUS_MSG_CURRENT_USER_INFO_UPDATE_UI = 1011;  // 用户个人信息改变
    public static final int EVENTBUS_MSG_FRIEND_INFO_UPDATE_UI = 1012;  // 其他端修改好友备注、描述信息，修改数据库后，需要刷新界面
    public static final int EVENTBUS_GROUP_QRCODE = 1013;  // 群二维码分享跳转到app
    public static final int EVENTBUS_ITEM_AVATAR_UPDATE = 1014;  // 头像缓存更新
    public static final boolean SEND_TO_OTHER_DEVIE = true;  // 向其他设备发送消息

    public static final int EVENTBUS_TIME_UPDATE = 1015;  // 时间需要更新
    public static final int EVENTBUS_ROOM_MEMBER_DOWNLOAD_FOR_AVATAR = 1016;  // 下载群成员
    public static final int EVENTBUS_ROOM_MEMBER_DOWNLOAD_REFRESH_UI = 1017;  // 刷新群头像
    public static final int EVENTBUS_FRIEND_DOWNLOAD_REFRESH_UI = 1018;  // 刷新好友列表
    public static final int EVENTBUS_TRANSPOND_REFRESH_UI = 1019;  // 转发多条消息后，聊天界面恢复

    public static final int EVENTBUS_GROUP_TWO_WAY_WIITHDRAWAL = 932;//群组双向撤回
    public static final int EVENTBUS_DISABLE_IP_DEVICE = 516;//禁用IP  设备
}
