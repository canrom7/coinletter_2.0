package com.iimm.miliao.view.mucChatHolder;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.util.Constants;

/**
 * holder工厂
 * <p>
 * create by xuan 2018-12-6 15:12:14
 */

public class ChatHolderFactory {
    /**
     * holder 根据消息类型 产生不同的holder
     *
     * @param holderType 消息类型
     * @return
     */
    public static AChatHolderInterface getHolder(Context context, ChatHolderType holderType, ViewGroup viewGroup) {

        AChatHolderInterface holder = null;
        switch (holderType) {
            case VIEW_FROM_TEXT: // 文字消息
            case VIEW_TO_TEXT: // 文字消息
                holder = new TextViewHolder(getView(context, holderType, viewGroup));
                holder.isMysend = holderType == ChatHolderType.VIEW_FROM_TEXT;
                break;
            case VIEW_FROM_REPLAY: // 回复消息，
            case VIEW_TO_REPLAY: // 回复消息，
                holder = new TextReplayViewHolder(getView(context, holderType, viewGroup));
                holder.isMysend = holderType == ChatHolderType.VIEW_FROM_REPLAY;
                break;
            case VIEW_FROM_IMAGE: // 图片消息
            case VIEW_TO_IMAGE: // 图片消息
                holder = new ImageViewHolder(getView(context, holderType, viewGroup));
                holder.isMysend = holderType == ChatHolderType.VIEW_FROM_IMAGE;
                break;
            case VIEW_FROM_VOICE: // 语音
            case VIEW_TO_VOICE: // 语音
                holder = new VoiceViewHolder(getView(context, holderType, viewGroup));
                holder.isMysend = holderType == ChatHolderType.VIEW_FROM_VOICE;
                break;
            case VIEW_FROM_VIDEO: // 视频
            case VIEW_TO_VIDEO: // 视频
                holder = new VideoViewHolder(getView(context, holderType, viewGroup));
                holder.isMysend = holderType == ChatHolderType.VIEW_FROM_VIDEO;
                break;
            case VIEW_FROM_LOCATION: // 链接
            case VIEW_TO_LOCATION: // 链接
            case VIEW_FROM_LINK: // 链接
            case VIEW_TO_LINK: // 链接
                holder = new LocationViewHolder(getView(context, holderType, viewGroup));
                holder.isMysend = holderType == ChatHolderType.VIEW_FROM_LOCATION || holderType == ChatHolderType.VIEW_FROM_LINK;
                break;
            case VIEW_FROM_GIF: // 动图
            case VIEW_TO_GIF: // 动图
                holder = new GifViewHolder(getView(context, holderType, viewGroup));
                holder.isMysend = holderType == ChatHolderType.VIEW_FROM_GIF;
                break;
            case VIEW_FROM_FILE: // 文件
            case VIEW_TO_FILE: // 文件
                holder = new FileViewHolder(getView(context, holderType, viewGroup));
                holder.isMysend = holderType == ChatHolderType.VIEW_FROM_FILE;
                break;
            case VIEW_FROM_CARD: // 名片
            case VIEW_TO_CARD: // 名片
                holder = new CardViewHolder(getView(context, holderType, viewGroup));
                holder.isMysend = holderType == ChatHolderType.VIEW_FROM_CARD;
                break;
            case VIEW_FROM_RED: // 红包
            case VIEW_TO_RED: // 红包
            case VIEW_FROM_RED_KEY: // 口令红包
            case VIEW_TO_RED_KEY: // 口令红包
                holder = new RedViewHolder(getView(context, holderType, viewGroup));
                holder.isMysend = holderType == ChatHolderType.VIEW_FROM_RED || holderType == ChatHolderType.VIEW_FROM_RED_KEY ||holderType==ChatHolderType.VIEW_FROM_RED_CLOUD;
                break;
            case VIEW_FROM_RED_CLOUD://云红包
            case VIEW_TO_RED_CLOUD://云红包
                holder = new CloudRedViewHolder(getView(context, holderType, viewGroup));
                holder.isMysend =holderType==ChatHolderType.VIEW_FROM_RED_CLOUD;
                break;
            case VIEW_FROM_TRANSFER:
            case VIEW_TO_TRANSFER:
                holder = new TransferViewHolder(getView(context, holderType, viewGroup));
                holder.isMysend = holderType == ChatHolderType.VIEW_FROM_TRANSFER;
                break;
            case MICRO_VIEW_FROM_TRANSFER:
            case MICRO_VIEW_TO_TRANSFER://微转账
                holder = new MicroTransferViewHolder(getView(context, holderType, viewGroup));
                holder.isMysend = holderType == ChatHolderType.MICRO_VIEW_FROM_TRANSFER;
                break;
            case VIEW_FROM_LINK_SHARE: // 分享进来的链接
            case VIEW_TO_LINK_SHARE: // 分享进来的链接
                holder = new LinkViewHolder(getView(context, holderType, viewGroup));
                holder.isMysend = holderType == ChatHolderType.VIEW_FROM_LINK_SHARE;
                break;
            case VIEW_FROM_IMAGE_TEXT: // 图文消息
            case VIEW_TO_IMAGE_TEXT: // 图文消息
                holder = new TextImgViewHolder(getView(context, holderType, viewGroup));
                holder.isMysend = holderType == ChatHolderType.VIEW_FROM_IMAGE_TEXT;
                break;
            case VIEW_FROM_IMAGE_TEXT_MANY: // 多条图文
            case VIEW_TO_IMAGE_TEXT_MANY: // 多条图文
                holder = new TextImgManyHolder(getView(context, holderType, viewGroup));
                holder.isMysend = holderType == ChatHolderType.VIEW_FROM_IMAGE_TEXT_MANY;
                break;
            case VIEW_FROM_MEDIA_CALL: // 音视频
            case VIEW_TO_MEDIA_CALL: // 音视频
                holder = new CallViewHolder(getView(context, holderType, viewGroup));
                holder.isMysend = holderType == ChatHolderType.VIEW_FROM_MEDIA_CALL;
                break;
            case VIEW_FROM_SHAKE: // 戳一戳
            case VIEW_TO_SHAKE: // 戳一戳
                holder = new ShakeViewHolder(getView(context, holderType, viewGroup));
                holder.isMysend = holderType == ChatHolderType.VIEW_FROM_SHAKE;
                break;
            case VIEW_FROM_CHAT_HISTORY: // 聊天记录
            case VIEW_TO_CHAT_HISTORY: // 聊天记录
                holder = new ChatHistoryHolder(getView(context, holderType, viewGroup));
                holder.isMysend = holderType == ChatHolderType.VIEW_FROM_CHAT_HISTORY;
                break;
            case VIEW_SYSTEM_TIP:
                holder = new SystemViewHolder(getView(context, holderType, viewGroup));
                break;
            case VIEW_SYSTEM_LIVE:
                holder = new LiveChatViewHolder(getView(context, holderType, viewGroup));
                break;
            case VIEW_READ_DEL_TIME_HINT:
                holder = new ReadDelTimeHintHolder(getView(context, holderType, viewGroup));
                break;
            default:
                holder = new SystemViewHolder(getView(context, holderType, viewGroup));
                break;
        }

        return holder;
    }

    public static View getView(Context context, ChatHolderType holderType, ViewGroup viewGroup) {
        LayoutInflater from = LayoutInflater.from(context);
        View view;
        int id;
        switch (holderType) {
            case VIEW_FROM_TEXT: // 文字消息
            case VIEW_TO_TEXT: // 文字消息
                id = holderType == ChatHolderType.VIEW_FROM_TEXT ? R.layout.chat_from_item_text : R.layout.chat_to_item_text;
                break;
            case VIEW_FROM_REPLAY: // 回复消息，
            case VIEW_TO_REPLAY: // 回复消息，
                id = holderType == ChatHolderType.VIEW_FROM_REPLAY ? R.layout.chat_from_item_text_replay : R.layout.chat_to_item_text_replay;
                break;
            case VIEW_FROM_IMAGE: // 图片消息
            case VIEW_TO_IMAGE: // 图片消息
                id = holderType == ChatHolderType.VIEW_FROM_IMAGE ? R.layout.chat_from_item_image : R.layout.chat_to_item_image;
                break;
            case VIEW_FROM_VOICE: // 语音
            case VIEW_TO_VOICE: // 语音
                id = holderType == ChatHolderType.VIEW_FROM_VOICE ? R.layout.chat_from_item_voice : R.layout.chat_to_item_voice;
                break;
            case VIEW_FROM_VIDEO: // 视频
            case VIEW_TO_VIDEO: // 视频
                id = holderType == ChatHolderType.VIEW_FROM_VIDEO ? R.layout.chat_from_item_video : R.layout.chat_to_item_video;
                break;
            case VIEW_FROM_LOCATION: // 链接
            case VIEW_TO_LOCATION: // 链接
            case VIEW_FROM_LINK: // 链接
            case VIEW_TO_LINK: // 链接
                id = holderType == ChatHolderType.VIEW_FROM_LOCATION || holderType == ChatHolderType.VIEW_FROM_LINK ? R.layout.chat_from_item_location : R.layout.chat_to_item_location;
                break;
            case VIEW_FROM_GIF: // 动图
            case VIEW_TO_GIF: // 动图
                id = holderType == ChatHolderType.VIEW_FROM_GIF ? R.layout.chat_from_item_gif : R.layout.chat_to_item_gif;
                break;
            case VIEW_FROM_FILE: // 文件
            case VIEW_TO_FILE: // 文件
                id = holderType == ChatHolderType.VIEW_FROM_FILE ? R.layout.chat_from_item_file : R.layout.chat_to_item_file;
                break;
            case VIEW_FROM_CARD: // 名片
            case VIEW_TO_CARD: // 名片
                id = holderType == ChatHolderType.VIEW_FROM_CARD ? R.layout.chat_from_item_card : R.layout.chat_to_item_card;
                break;
            case VIEW_FROM_RED: // 红包
            case VIEW_TO_RED: // 红包
            case VIEW_FROM_RED_KEY: // 口令红包
            case VIEW_TO_RED_KEY: // 口令红包
            case VIEW_FROM_RED_CLOUD://云红包
            case VIEW_TO_RED_CLOUD://云红包
                id = holderType == ChatHolderType.VIEW_FROM_RED || holderType == ChatHolderType.VIEW_FROM_RED_KEY || holderType ==ChatHolderType.VIEW_FROM_RED_CLOUD? R.layout.chat_from_item_redpacket : R.layout.chat_to_item_redpacket;
                break;
            case VIEW_FROM_TRANSFER:
            case VIEW_TO_TRANSFER:
                id = holderType == ChatHolderType.VIEW_FROM_TRANSFER ? R.layout.chat_from_item_transfer : R.layout.chat_to_item_transfer;
                break;
            case MICRO_VIEW_FROM_TRANSFER:
            case MICRO_VIEW_TO_TRANSFER://微转账
                id = holderType == ChatHolderType.MICRO_VIEW_FROM_TRANSFER ? R.layout.chat_from_item_transfer : R.layout.chat_to_item_transfer;
                break;
            case VIEW_FROM_LINK_SHARE: // 分享进来的链接
            case VIEW_TO_LINK_SHARE: // 分享进来的链接
                id = holderType == ChatHolderType.VIEW_FROM_LINK_SHARE ? R.layout.chat_from_item_link : R.layout.chat_to_item_link;
                break;
            case VIEW_FROM_IMAGE_TEXT: // 图文消息
            case VIEW_TO_IMAGE_TEXT: // 图文消息
                id = holderType == ChatHolderType.VIEW_FROM_IMAGE_TEXT ? R.layout.chat_from_item_text_img : R.layout.chat_to_item_text_img;
                break;
            case VIEW_FROM_IMAGE_TEXT_MANY: // 多条图文
            case VIEW_TO_IMAGE_TEXT_MANY: // 多条图文
                id = holderType == ChatHolderType.VIEW_FROM_IMAGE_TEXT_MANY ? R.layout.chat_from_item_text_img_many : R.layout.chat_to_item_text_img_many;
                break;
            case VIEW_FROM_MEDIA_CALL: // 音视频
            case VIEW_TO_MEDIA_CALL: // 音视频
                id = holderType == ChatHolderType.VIEW_FROM_MEDIA_CALL ? R.layout.chat_from_item_call : R.layout.chat_to_item_call;
                break;
            case VIEW_FROM_SHAKE: // 戳一戳
            case VIEW_TO_SHAKE: // 戳一戳
                id = holderType == ChatHolderType.VIEW_FROM_SHAKE ? R.layout.chat_from_item_shake : R.layout.chat_to_item_shake;
                break;
            case VIEW_FROM_CHAT_HISTORY: // 聊天记录
            case VIEW_TO_CHAT_HISTORY: // 聊天记录
                id = holderType == ChatHolderType.VIEW_FROM_CHAT_HISTORY ? R.layout.chat_from_item_history : R.layout.chat_to_item_history;
                break;
            case VIEW_SYSTEM_TIP:
                id = R.layout.chat_item_system;
                break;
            case VIEW_SYSTEM_LIVE:
                id = R.layout.chat_item_live_system;
                break;
            case VIEW_READ_DEL_TIME_HINT:
                id = R.layout.chat_read_del_time_hint;
                break;
            default:
                id = R.layout.chat_item_system;
                break;
        }
        view = from.inflate(id, viewGroup, false);
        return view;
    }

    /**
     * 将xmppType 转换成 ViewType
     *
     * @param mySend
     * @param message
     * @return viewType
     */
    public static int xt2vt(boolean mySend, ChatMessage message) {
        ChatHolderType eType = getChatHolderType(mySend, message);
        return eType.ordinal();
    }

    /**
     * 将int 类型的 holderType 转换成 ChatHolderType
     *
     * @param holderType
     * @return ChatHolderType
     */
    public static ChatHolderType getChatHolderType(int holderType) {
        return ChatHolderType.values()[holderType];
    }

    /**
     * 返回枚举类型的个数
     *
     * @return
     */
    public static int viewholderCount() {
        return ChatHolderType.values().length;
    }

    public static ChatHolderType getChatHolderType(boolean mySend, ChatMessage message) {

        int xmppType = message.getType();
        ChatHolderType eType;
        switch (xmppType) {
            case Constants.TYPE_TEXT:
                eType = mySend ? ChatHolderType.VIEW_FROM_TEXT : ChatHolderType.VIEW_TO_TEXT;
                break;
            case Constants.TYPE_REPLAY:
                eType = mySend ? ChatHolderType.VIEW_FROM_REPLAY : ChatHolderType.VIEW_TO_REPLAY;
                break;
            case Constants.TYPE_IMAGE:
                eType = mySend ? ChatHolderType.VIEW_FROM_IMAGE : ChatHolderType.VIEW_TO_IMAGE;
                break;
            case Constants.TYPE_VOICE:
                eType = mySend ? ChatHolderType.VIEW_FROM_VOICE : ChatHolderType.VIEW_TO_VOICE;
                break;
            case Constants.TYPE_VIDEO:
                eType = mySend ? ChatHolderType.VIEW_FROM_VIDEO : ChatHolderType.VIEW_TO_VIDEO;
                break;
            case Constants.TYPE_GIF:
                eType = mySend ? ChatHolderType.VIEW_FROM_GIF : ChatHolderType.VIEW_TO_GIF;
                break;
            case Constants.TYPE_LOCATION:
                eType = mySend ? ChatHolderType.VIEW_FROM_LOCATION : ChatHolderType.VIEW_TO_LOCATION;
                break;
            case Constants.TYPE_FILE:
                eType = mySend ? ChatHolderType.VIEW_FROM_FILE : ChatHolderType.VIEW_TO_FILE;
                break;
            case Constants.TYPE_READ_EXCLUSIVE:
                if (!TextUtils.isEmpty(message.getFilePath())) {
                    eType = mySend ? ChatHolderType.VIEW_FROM_RED_KEY : ChatHolderType.VIEW_TO_RED_KEY;
                } else {
                    eType = mySend ? ChatHolderType.VIEW_FROM_RED : ChatHolderType.VIEW_TO_RED;
                }
                break;
            case Constants.TYPE_RED:
                if (!TextUtils.isEmpty(message.getFilePath())) {
                    eType = mySend ? ChatHolderType.VIEW_FROM_RED_KEY : ChatHolderType.VIEW_TO_RED_KEY;
                } else {
                    eType = mySend ? ChatHolderType.VIEW_FROM_RED : ChatHolderType.VIEW_TO_RED;
                }
                break;
            case Constants.TYPE_CLOUD_RED:
                eType = mySend ? ChatHolderType.VIEW_FROM_RED_CLOUD : ChatHolderType.VIEW_TO_RED_CLOUD;
                break;
            case Constants.TYPE_TRANSFER:
                eType = mySend ? ChatHolderType.VIEW_FROM_TRANSFER : ChatHolderType.VIEW_TO_TRANSFER;
                break;
            case Constants.TYPE_CLOUD_TRANSFER://微转账
                eType = mySend ? ChatHolderType.MICRO_VIEW_FROM_TRANSFER : ChatHolderType.MICRO_VIEW_TO_TRANSFER;
                break;
            case Constants.TYPE_CARD:
                eType = mySend ? ChatHolderType.VIEW_FROM_CARD : ChatHolderType.VIEW_TO_CARD;
                break;
            case Constants.TYPE_LINK: // 链接
                eType = mySend ? ChatHolderType.VIEW_FROM_LINK : ChatHolderType.VIEW_TO_LINK;
                break;
            case Constants.TYPE_SHARE_LINK: // 分享进来的链接
                eType = mySend ? ChatHolderType.VIEW_FROM_LINK_SHARE : ChatHolderType.VIEW_TO_LINK_SHARE;
                break;
            case Constants.TYPE_IMAGE_TEXT:
                eType = mySend ? ChatHolderType.VIEW_FROM_IMAGE_TEXT : ChatHolderType.VIEW_TO_IMAGE_TEXT;
                break;
            case Constants.TYPE_IMAGE_TEXT_MANY:
                eType = mySend ? ChatHolderType.VIEW_FROM_IMAGE_TEXT_MANY : ChatHolderType.VIEW_TO_IMAGE_TEXT_MANY;
                break;
            case Constants.TYPE_END_CONNECT_VIDEO:
            case Constants.TYPE_END_CONNECT_VOICE:
            case Constants.TYPE_NO_CONNECT_VOICE:
            case Constants.TYPE_NO_CONNECT_VIDEO:
            case Constants.TYPE_IS_MU_CONNECT_VOICE:
            case Constants.TYPE_IS_MU_CONNECT_VIDEO:
                eType = mySend ? ChatHolderType.VIEW_FROM_MEDIA_CALL : ChatHolderType.VIEW_TO_MEDIA_CALL;
                break;
            case Constants.TYPE_SHAKE: // 戳一戳
                eType = mySend ? ChatHolderType.VIEW_FROM_SHAKE : ChatHolderType.VIEW_TO_SHAKE;
                break;
            case Constants.TYPE_CHAT_HISTORY: // 聊天记录
                eType = mySend ? ChatHolderType.VIEW_FROM_CHAT_HISTORY : ChatHolderType.VIEW_TO_CHAT_HISTORY;
                break;
            case Constants.TYPE_CUSTOM_CHANGE_READ_DEL_TIME:
                eType = ChatHolderType.VIEW_READ_DEL_TIME_HINT;
                break;
            default:
                eType = ChatHolderType.VIEW_SYSTEM_TIP;
                break;
        }
        return eType;
    }

    public enum ChatHolderType {
        // 提示消息和 直播间消息
        VIEW_SYSTEM_TIP,
        VIEW_SYSTEM_LIVE,
        // 文字消息
        VIEW_FROM_TEXT,
        VIEW_TO_TEXT,
        // 回复消息，
        VIEW_FROM_REPLAY,
        VIEW_TO_REPLAY,
        // 图片消息
        VIEW_FROM_IMAGE,
        VIEW_TO_IMAGE,
        // 语音消息
        VIEW_FROM_VOICE,
        VIEW_TO_VOICE,
        // 视频消息
        VIEW_FROM_VIDEO,
        VIEW_TO_VIDEO,
        // gif消息
        VIEW_FROM_GIF,
        VIEW_TO_GIF,
        // 位置消息
        VIEW_FROM_LOCATION,
        VIEW_TO_LOCATION,
        // 文件消息
        VIEW_FROM_FILE,
        VIEW_TO_FILE,
        // 名片消息
        VIEW_FROM_CARD,
        VIEW_TO_CARD,
        // 红包消息(普通)
        VIEW_FROM_RED,
        VIEW_TO_RED,
        // 云红包消息(普通)
        VIEW_FROM_RED_CLOUD,
        VIEW_TO_RED_CLOUD,
        // 红包消息(口令)
        VIEW_FROM_RED_KEY,
        VIEW_TO_RED_KEY,
        // 转账消息
        VIEW_FROM_TRANSFER,
        VIEW_TO_TRANSFER,
        // 微转账消息
        MICRO_VIEW_FROM_TRANSFER,
        MICRO_VIEW_TO_TRANSFER,
        // 链接消息 （卡片）
        VIEW_FROM_LINK,
        VIEW_TO_LINK,
        // 链接消息（第三方分享）
        VIEW_FROM_LINK_SHARE,
        VIEW_TO_LINK_SHARE,
        // 图文消息（单条）
        VIEW_FROM_IMAGE_TEXT,
        VIEW_TO_IMAGE_TEXT,
        // 图文消息（多条）
        VIEW_FROM_IMAGE_TEXT_MANY,
        VIEW_TO_IMAGE_TEXT_MANY,
        // 语音邀请挂断消息
        VIEW_FROM_MEDIA_CALL,
        VIEW_TO_MEDIA_CALL,
        // 抖一抖消息
        VIEW_FROM_SHAKE,
        VIEW_TO_SHAKE,
        // 历史纪录消息
        VIEW_FROM_CHAT_HISTORY,
        VIEW_TO_CHAT_HISTORY,
        //自定义的消息（只在Android 中使用的）
        VIEW_READ_DEL_TIME_HINT;

        public static ChatHolderType valueOf(int ordinal) {
            if (ordinal < 0 || ordinal >= values().length) {
                throw new IndexOutOfBoundsException("Invalid ordinal");
            }
            return values()[ordinal];
        }
    }
}
