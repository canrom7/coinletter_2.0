package com.iimm.miliao.helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import com.iimm.miliao.R;
import com.iimm.miliao.ui.me.redpacket.UtilWeixin;
import com.iimm.miliao.util.AppUtils;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;

public class ShareSdkHelper {

    private ShareSdkHelper() {
    }

    public static void shareQQ(
            Tencent mTencent,
            Context ctx,
            String title,
            String text,
            String url,
            String uri,
            IUiListener qqShareListener) {
        platformQQShare(mTencent, ctx, title, text, url, uri, qqShareListener);
    }

    private static void platformQQShare(Tencent mTencent, Context ctx, String title, String text, String url, String uri, IUiListener qqShareListener) {
        if (mTencent == null) return;
        Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, text);
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, url);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, uri);
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, ctx.getString(R.string.app_name));
        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
        mTencent.shareToQQ((Activity) ctx, params, qqShareListener);
    }

    public static void shareWechat(
            Context ctx,
            String title,
            String text,
            String url,
            String mWxId) {
        platformShare(ctx, SendMessageToWX.Req.WXSceneSession, title, text, url, mWxId);
    }

    public static void shareWechat(
            Context ctx,
            String title,
            String text,
            String url,
            String mWxId,
            Bitmap bitmap) {
        platformShare(ctx, SendMessageToWX.Req.WXSceneSession, title, text, url, mWxId, bitmap);
    }

    public static void shareWechatPicture(
            Context ctx,
            Bitmap bitmap,
            String mWxId) {
        platformSharePicture(ctx, SendMessageToWX.Req.WXSceneSession, bitmap, mWxId);
    }

    public static void shareWechatMoments(
            Context ctx,
            String title,
            String text,
            String url,
            String mWxId) {
        platformShare(ctx, SendMessageToWX.Req.WXSceneTimeline, title, text, url, mWxId);
    }

    private static void platformShare(
            Context ctx,
            int scene,
            String title,
            String text,
            String url,
            String mWxId
    ) {
        if (!AppUtils.isAppInstalled(ctx, "com.tencent.mm")) {
            Toast.makeText(ctx, ctx.getString(R.string.tip_no_wx_chat), Toast.LENGTH_SHORT).show();
            return;
        }
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        final WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = text;
        Bitmap thumb = BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.icon);
        if (thumb != null) {
            Bitmap logo = Bitmap.createScaledBitmap(thumb, 120, 120, true);
            thumb.recycle();
            msg.thumbData = UtilWeixin.bmpToByteArray(logo, true);
        }
        SendMessageToWX.Req req = new SendMessageToWX.Req();    //创建一个请求对象
        req.message = msg;
        req.scene = scene;
        getApi(ctx, mWxId).sendReq(req);   //如果调用成功微信,会返回true
    }

    private static void platformShare(
            Context ctx,
            int scene,
            String title,
            String text,
            String url,
            String mWxId,
            Bitmap bitmap
    ) {
        if (!AppUtils.isAppInstalled(ctx, "com.tencent.mm")) {
            Toast.makeText(ctx, ctx.getString(R.string.tip_no_wx_chat), Toast.LENGTH_SHORT).show();
            return;
        }
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        final WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = text;
        if (bitmap != null) {
            Bitmap logo = Bitmap.createScaledBitmap(bitmap, 120, 120, true);
            msg.thumbData = UtilWeixin.bmpToByteArray(logo, true);
        }
        SendMessageToWX.Req req = new SendMessageToWX.Req();    //创建一个请求对象
        req.message = msg;
        req.scene = scene;
        getApi(ctx, mWxId).sendReq(req);   //如果调用成功微信,会返回true
    }

    private static void platformSharePicture(
            Context ctx,
            int scene,
            Bitmap bitmap,
            String mWxId

    ) {
        if (!AppUtils.isAppInstalled(ctx, "com.tencent.mm")) {
            Toast.makeText(ctx, ctx.getString(R.string.tip_no_wx_chat), Toast.LENGTH_SHORT).show();
            return;
        }
        WXImageObject imgObj = new WXImageObject(bitmap);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

//设置缩略图
      /*  Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
        bitmap.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);*/

//构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.message = msg;
        req.scene = scene;
        getApi(ctx, mWxId).sendReq(req);   //如果调用成功微信,会返回true
    }

    private static IWXAPI getApi(Context ctx, String wxId) {
        IWXAPI api = WXAPIFactory.createWXAPI(ctx, wxId, true);
        api.registerApp(wxId);
        return api;
    }
}

