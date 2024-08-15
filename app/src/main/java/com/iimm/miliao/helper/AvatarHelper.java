package com.iimm.miliao.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;
import com.makeramen.roundedimageview.RoundedImageView;
import com.othershe.combinebitmap.CombineBitmap;
import com.othershe.combinebitmap.layout.WechatLayoutManager;
import com.othershe.combinebitmap.listener.OnProgressListener;
import com.iimm.miliao.BuildConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.RoomMemberDao;
import com.iimm.miliao.db.dao.UserAvatarDao;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.AppExecutors;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.AvatarUtil;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DisplayUtil;
import com.iimm.miliao.util.ImageCache;
import com.iimm.miliao.util.ImageUtils;
import com.iimm.miliao.util.SkinUtils;
import com.iimm.miliao.view.HeadView;
import com.iimm.miliao.view.circularImageView.JoinBitmaps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户头像的上传和获取
 */
public class AvatarHelper {
    private static final String TAG = "AvatarHelper";
    public static AvatarHelper INSTANCE;
    public Context mContext;
    private Map<String, Bitmap> mVideoThumbMap = new HashMap<>();
    public Map<String, String> timeMap = new ConcurrentHashMap<>(); //头像更新缓存

    private AvatarHelper(Context ctx) {
        this.mContext = ctx;
    }

    public static AvatarHelper getInstance() {
        if (INSTANCE == null) {
            synchronized (AvatarHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AvatarHelper(MyApplication.getContext());
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 更新头像
     */
    public static void updateAvatar(String userId) {
        UserAvatarDao.getInstance().saveUpdateTime(userId);
    }

    /**
     * 获取系统号的静态头像资源ID，
     *
     * @return 不是系统号就返回null,
     */
    @IdRes
    public static Integer getStaticAvatar(String userId) {
        Integer ret = null;
        switch (userId) {
            case Constants.ID_SYSTEM_MESSAGE:
                int reId = Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR ? R.drawable.im_notice : R.drawable.im_notice_square;
                ret = reId;
                break;
            case Constants.ID_NEW_FRIEND_MESSAGE:
                ret = R.drawable.im_new_friends;
                break;
            case Constants.ID_SK_PAY:
                ret = R.drawable.weixinpayyue;
                break;
            case "android":
            case "ios":
                ret = R.drawable.fdy;
                break;
            case "pc":
            case "mac":
            case "web":
                ret = R.drawable.feb;
                break;
        }
        return ret;
    }

    public static String getAvatarUrl(String userId, boolean isThumb) {
        if (TextUtils.isEmpty(userId) || userId.length() > 8) {
            return null;
        }
        int userIdInt = -1;
        try {
            userIdInt = Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (userIdInt == -1 || userIdInt == 0) {
            return null;
        }

        int dirName = userIdInt % 10000;
        String url = null;
        if (isThumb) {
            url = CoreManager.requireConfig(MyApplication.getInstance()).AVATAR_THUMB_PREFIX + "/" + dirName + "/" + userId + ".jpg";
        } else {
            url = CoreManager.requireConfig(MyApplication.getInstance()).AVATAR_ORIGINAL_PREFIX + "/" + dirName + "/" + userId + ".jpg";
        }
        return url;
    }

    public static String getGroupAvatarUrl(String userId, boolean isThumb) {
        int jidHashCode = userId.hashCode();
        int oneLevelName = Math.abs(jidHashCode % 10000);
        int twoLevelName = Math.abs(jidHashCode % 20000);

        int dirName = oneLevelName;
        String url;
//        Random random = new Random();
//        int num = random.nextInt(99) % (99 - 10 + 1) + 10;
        if (isThumb) {
            url = CoreManager.requireConfig(MyApplication.getInstance()).AVATAR_THUMB_PREFIX + "/" + dirName + "/" + twoLevelName + "/" + userId + ".jpg";
        } else {
            url = CoreManager.requireConfig(MyApplication.getInstance()).AVATAR_ORIGINAL_PREFIX + "/" + dirName + "/" + twoLevelName + "/" + userId + ".jpg";
        }
        return url;
    }

    public static void setIcon(ImageView iv, int circle, int square) {
        if (Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR) {
            iv.setImageResource(circle);
        } else {
            iv.setImageResource(square);
        }
    }

    public void displayAvatar(String userId, ImageView imageView) {
        displayAvatar(userId, imageView, true);
    }

    public void displayAvatar(String userId, HeadView headView) {
        displayAvatar(userId, headView.getHeadImage(), true);
    }

    public static boolean handlerSpecialAvatar(String userId, ImageView iv) {
        if (TextUtils.isEmpty(userId)) {
            return false;
        }
        if (userId.equals(Constants.ID_SYSTEM_MESSAGE)) {
//            iv.setImageResource(R.drawable.im_notice);
            int reId = Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR ? R.drawable.im_notice : R.drawable.im_notice_square;

            Glide.with(MyApplication.getContext()).load(getAvatarUrl(userId, true)).placeholder(reId).error(reId).diskCacheStrategy(DiskCacheStrategy.NONE).into(iv);
            return true;
        } else if (userId.equals(Constants.ID_NEW_FRIEND_MESSAGE)) {
            iv.setImageResource(R.drawable.im_new_friends);
            return true;
        } else if (userId.equals(Constants.ID_SK_PAY)) {
            iv.setImageResource(R.drawable.weixinpayyue);
            return true;
        } else if (userId.equals("android") || userId.equals("ios")) {// 我的手机
            iv.setImageResource(R.drawable.ic_phone_contact);
            return true;
        } else if (userId.equals("pc") || userId.equals("mac") || userId.equals("web")) {// 我的电脑
            iv.setImageResource(R.drawable.ic_feb);
            return true;
        }else if (userId.equals(Constants.ID_SYSTEM_NOTIFICATION)){

            int reId = Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR ? R.drawable.ic_integral_round : R.drawable.ic_integral;
            iv.setImageResource(reId);
            return true;
        }
        return false;
    }

    /**
     * 显示头像
     *
     * @param userId
     * @param imageView
     * @param isThumb
     */
    public void displayAvatar(String userId, final ImageView imageView, final boolean isThumb) {
        if (imageView == null) {
            return;
        }
        if (imageView instanceof RoundedImageView) {
            if (Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR) {
                ((RoundedImageView) imageView).setOval(true);
            } else {
                ((RoundedImageView) imageView).setOval(false);
                ((RoundedImageView) imageView).setCornerRadius(Constants.AVATAR_CORNER_RADIUS);
            }
        }
        if (handlerSpecialAvatar(userId, imageView)) {
            return;
        }
        String url = getAvatarUrl(userId, isThumb);
        if (TextUtils.isEmpty(url)) {
            return;
        }

        Friend friend = FriendDao.getInstance().getFriend(CoreManager.requireSelf(mContext).getUserId(), userId);
        Log.i(TAG, "displayAvatar: friend=" + friend);
        if (friend != null) {
            String name = TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName();
            displayAvatar(name, userId, imageView, isThumb);
        } else if (CoreManager.requireSelf(mContext).getUserId().equals(userId)) {
            displayAvatar(CoreManager.requireSelf(mContext).getNickName(), CoreManager.requireSelf(mContext).getUserId(), imageView, isThumb);
        } else {
            Log.e("zq", "friend==null,直接调用下面传nickName的display方法");
            displayUrl(getAvatarUrl(userId, isThumb), imageView);
        }

    }

    /**
     * 显示头像
     *
     * @param userId
     * @param imageView
     * @param isThumb
     */
    public void displayAvatar(String nickName, String userId, final ImageView imageView, final boolean isThumb) {
        if (imageView == null) {
            return;
        }
        if (imageView instanceof RoundedImageView) {
            if (Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR) {
                ((RoundedImageView) imageView).setOval(true);
            } else {
                ((RoundedImageView) imageView).setOval(false);
                ((RoundedImageView) imageView).setCornerRadius(Constants.AVATAR_CORNER_RADIUS);
            }
        }
        if (handlerSpecialAvatar(userId, imageView)) {
            return;
        }

        String url = getAvatarUrl(userId, isThumb);
        if (TextUtils.isEmpty(url)) {
            return;
        }
        String time = getAvatarTime(userId);
        imageView.setTag(R.id.key_avatar, url);
        Glide.with(MyApplication.getContext())
                .load(url)
                .placeholder(R.drawable.avatar_normal)
                .error(R.drawable.avatar_normal)
                .signature(new StringSignature(time))
                .dontAnimate()
                .into(imageView);
      /*  new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        if (imageView.getTag(R.id.key_avatar) != url) {
                            return;
                        }
                        imageView.setImageDrawable(resource);
                    }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            if (imageView.getTag(R.id.key_avatar) != url) {
                                return;
                            }
                            super.onLoadFailed(e, errorDrawable);
                            List<Object> bitmapList = new ArrayList();
                            bitmapList.add(nickName);
                            Bitmap avatar = AvatarUtil.getBuilder(mContext)
                                    .setShape(Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR ? AvatarUtil.Shape.CIRCLE : AvatarUtil.Shape.ROUND)
                                    .setList(bitmapList)
                                    .setTextSize(DisplayUtil.dip2px(mContext, 40))
                                    .setTextColor(R.color.white)
                                    .setTextBgColor(SkinUtils.getSkin(mContext).getAccentColor())
                                    .setBitmapSize(DisplayUtil.dip2px(mContext, 120), DisplayUtil.dip2px(mContext, 120))
                                    .create();
                            BitmapDrawable bitmapDrawable = new BitmapDrawable(avatar);
                            bitmapDrawable.setAntiAlias(true);
                            imageView.setImageDrawable(bitmapDrawable);
                        }
                    });*/

    }

    public void displayRoundAvatar(String nickName, String userId, final ImageView imageView, final boolean isThumb) {
        if (imageView == null) {
            return;
        }
        if (imageView instanceof RoundedImageView) {
            if (Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR) {
                ((RoundedImageView) imageView).setOval(true);
            } else {
                ((RoundedImageView) imageView).setOval(false);
                ((RoundedImageView) imageView).setCornerRadius(Constants.AVATAR_CORNER_RADIUS);
            }
        }
        if (handlerSpecialAvatar(userId, imageView)) {
            return;
        }

        String url = getAvatarUrl(userId, isThumb);
        if (TextUtils.isEmpty(url)) {
            return;
        }
        String time = getAvatarTime(userId);

        Glide.with(MyApplication.getContext())
                .load(url)
                .placeholder(R.drawable.avatar_normal)
                .signature(new StringSignature(time))
                .dontAnimate()
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        imageView.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        List<Object> bitmapList = new ArrayList();
                        bitmapList.add(nickName);
                        Bitmap avatar = AvatarUtil.getBuilder(mContext)
                                .setShape(AvatarUtil.Shape.ROUND)
                                .setList(bitmapList)
                                .setTextSize(DisplayUtil.dip2px(mContext, 40))
                                .setTextColor(R.color.white)
                                .setTextBgColor(SkinUtils.getSkin(mContext).getAccentColor())
                                .setBitmapSize(DisplayUtil.dip2px(mContext, 240), DisplayUtil.dip2px(mContext, 240))
                                .create();
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(avatar);
                        bitmapDrawable.setAntiAlias(true);
                        imageView.setImageDrawable(bitmapDrawable);

                    }
                });
    }

    /**
     * 手机联系人加载头像 无userId
     */
    public void displayAddressAvatar(String nickName, final ImageView imageView) {
        List<Object> bitmapList = new ArrayList();
        bitmapList.add(nickName);
        Bitmap avatar = AvatarUtil.getBuilder(mContext)
                .setShape(Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR ? AvatarUtil.Shape.CIRCLE : AvatarUtil.Shape.ROUND)
                .setList(bitmapList)
                .setTextSize(DisplayUtil.dip2px(mContext, 40))
                .setTextColor(R.color.white)
                .setTextBgColor(SkinUtils.getSkin(mContext).getAccentColor())
                .setBitmapSize(DisplayUtil.dip2px(mContext, 120), DisplayUtil.dip2px(mContext, 120))
                .create();
        BitmapDrawable bitmapDrawable = new BitmapDrawable(avatar);
        bitmapDrawable.setAntiAlias(true);
        imageView.setImageDrawable(bitmapDrawable);
    }

    /**
     * 仿微信九宫格头像
     * 封装个人头像和群头像的处理，
     * 缓存过期通过UserAvatarDao判断，
     * 群更新成员列表时{@link RoomMemberDao#deleteRoomMemberTable(java.lang.String)}里调用UserAvatarDao标记过期，
     */
    public void displayLatestMessageItemAvatar(String selfId, Friend friend, ImageView imageView) {
        if (imageView instanceof RoundedImageView) {
            if (Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR) {
                ((RoundedImageView) imageView).setOval(true);
            } else {
                ((RoundedImageView) imageView).setOval(false);
                ((RoundedImageView) imageView).setCornerRadius(Constants.AVATAR_CORNER_RADIUS);
            }
        }
        if (friend.getRoomFlag() == 0) {// 个人
            if (handlerSpecialAvatar(friend.getUserId(), imageView)) {
                return;
            }
            String url = getAvatarUrl(friend.getUserId(), false);
            if (TextUtils.isEmpty(url)) {
                return;
            }
            ImageUtils.loadSignalAvatarWithUrl(MyApplication.getContext(), url, imageView, friend.getUserId());
        } else if (friend.getRoomId() != null) {  // 群组
            String groupAvatarUrl = getGroupAvatarUrl(friend.getUserId(), false);
            Bitmap bitmap = ImageCache.getBitmap(friend.getRoomId());
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                if (TextUtils.isEmpty(groupAvatarUrl)) {
                    return;
                }
                Glide.with(MyApplication.getContext())
                        .load(groupAvatarUrl)
                        .asBitmap()
                        .signature(new StringSignature(UserAvatarDao.getInstance().getUpdateTime(friend.getUserId())))
                        .placeholder(Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR ? R.drawable.groupdefault : R.drawable.groupdefault_square)
                        .dontAnimate()
                        .listener(new RequestListener<String, Bitmap>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                try {
                                    List<String> idList = RoomMemberDao.getInstance().getRoomMemberForAvatar(friend.getRoomId(), selfId);
                                    if (idList.size() > 0) {
                                        // 可能没有刷过群成员列表，就查出空列表，
                                        if (Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR) {
                                            displayJoined(friend.getRoomId(), idList, imageView);
                                        } else {
                                            displayWeChatGroupAvatar(friend.getRoomId(), idList, imageView);//必须要在主线程，否则 onComplete不回调
                                        }
                                    } else {
                                        //imageView.setImageResource(Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR ? R.drawable.groupdefault : R.drawable.groupdefault_square);
                                        return false;
                                    }
                                } catch (Exception ee) {
                                }
                                return true;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                MyApplication.applicationHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageView.setImageBitmap(resource);
                                    }
                                });
                                ImageCache.putBitmap(friend.getRoomId(), resource);
                                return true;
                            }
                        }).error(Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR ? R.drawable.groupdefault : R.drawable.groupdefault_square)
                        .into(imageView);

            }
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {

                    MyApplication.applicationHandler.post(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }
            });
        } else {
            imageView.setImageResource(Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR ? R.drawable.groupdefault : R.drawable.groupdefault_square);
        }
    }

    /**
     * 仿微信九宫格头像
     * 封装个人头像和群头像的处理，
     * 缓存过期通过UserAvatarDao判断，
     * 群更新成员列表时{@link RoomMemberDao#deleteRoomMemberTable(java.lang.String)}里调用UserAvatarDao标记过期，
     */
    public void displayAvatar(String selfId, Friend friend, HeadView headView) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "357 displayAvatar: " + friend.getNickName());
            Log.i(TAG, "358 getRoomFlag: " + friend.getRoomFlag());
        }
        if (Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR) {
            headView.setRound(true);//圆形头像
        } else {
            headView.setRound(false);//方形头像
        }
        ImageView view = headView.getHeadImage();
        if (friend.getRoomFlag() == 0) {// 个人
            displayAvatar(friend.getUserId(), view, false);
        } else if (friend.getRoomId() != null) {  // 群组
            String url = getGroupAvatarUrl(friend.getUserId(), false);
            view.setTag(R.id.key_avatar, url);
            Glide.with(MyApplication.getContext())
                    .load(getGroupAvatarUrl(friend.getUserId(), false))
                    .placeholder(Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR ? R.drawable.groupdefault : R.drawable.groupdefault_square)
                    .dontAnimate()
                    .into(new SimpleTarget<GlideDrawable>() {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            if (view.getTag(R.id.key_avatar) != url) {
                                return;
                            }
                            view.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            super.onLoadFailed(e, errorDrawable);
                            if (e != null) {
                                Log.i(TAG, "onLoadFailed: " + e.getMessage());
                            }
                            // 该群组未上传群头像，使用合成头像
                            AsyncUtils.doAsync(this, throwable -> {
                                Reporter.post("加载群头像失败,", throwable);
                            }, c -> {
                                String time = getAvatarTime(friend.getRoomId());
                                Bitmap bitmap = ImageCache.getBitmap(friend.getRoomId());
                                if (bitmap != null) {
                                    c.uiThread(ref -> {
                                        if (view.getTag(R.id.key_avatar) != url) {
                                            return;
                                        }
                                        view.setImageBitmap(bitmap);
                                    });
                                } else {
                                    List<String> idList = RoomMemberDao.getInstance().getRoomMemberForAvatar(friend.getRoomId(), selfId);
                                    Log.e("Tag", idList.size() + "");
                                    if (idList != null) {
                                        // 可能没有刷过群成员列表，就查出空列表，
                                        if (idList.size() > 0) {
                                            c.uiThread(ref1 -> {
                                                if (Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR) {
                                                    displayJoined(friend.getRoomId(), idList, headView);
                                                } else {
                                                    displayWeChatGroupAvatar(friend.getRoomId(), time, idList, headView);
                                                }

                                            });
                                        } else {
                                            c.uiThread(ref2 -> {
                                                view.setImageResource(Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR ? R.drawable.groupdefault : R.drawable.groupdefault_square);
                                            });
                                        }
                                    } else {
                                        c.uiThread(ref3 -> {
                                            view.setImageResource(Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR ? R.drawable.groupdefault : R.drawable.groupdefault_square);
                                        });
                                    }
                                }
                            });
                        }
                    });
        } else {
            view.setImageResource(Constants.AVATAR_TYPE == Constants.CIRCLE_AVATAR ? R.drawable.groupdefault : R.drawable.groupdefault_square);
        }
    }

    /**
     * 显示仿微信九宫格头像
     *
     * @param roomId
     * @param idList
     * @param view
     */
    public void displayWeChatGroupAvatar(String roomId, List<String> idList, ImageView view) {
        List<String> urlList = new ArrayList<>();
        for (String str : idList) {
            String url = AvatarHelper.getAvatarUrl(str, true);
            urlList.add(url);
        }
        String[] array = new String[urlList.size()];
        String[] urls = urlList.toArray(array);

        CombineBitmap.init(mContext)
                .setLayoutManager(new WechatLayoutManager()) // 必选， 设置图片的组合形式，支持WechatLayoutManager、DingLayoutManager
                .setSize(60) // 必选，组合后Bitmap的尺寸，单位dp
                .setGap(1) // 单个图片之间的距离，单位dp，默认0dp
                .setGapColor(Color.parseColor("#dddee0")) // 单个图片间距的颜色，默认白色
                .setPlaceholder(R.drawable.avatar_normal) // 单个图片加载失败的默认显示图片
                .setUrls(urls) // 要加载的图片url数组
                //.setBitmaps() // 要加载的图片bitmap数组
                //.setResourceIds() // 要加载的图片资源id数组
                .setImageView(view) // 直接设置要显示图片的ImageView
                // 设置“子图片”的点击事件，需使用setImageView()，index和图片资源数组的索引对应
//                        .setOnSubItemClickListener(new OnSubItemClickListener() {
//                            @Override
//                            public void onSubItemClick(int index) {
//                            }
//                        })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onComplete(Bitmap bitmap) {
                        ImageCache.putBitmap(roomId, bitmap);
                    }
                })
                .build();

    }

    private void displayJoined(String roomId, List<String> idList, ImageView view) {
        // 当前item项的头像个数
        int size = idList.size();
        TreeMap<Integer, Bitmap> sortedBitmap = new TreeMap<>();
        // 这里url的顺序是对的
        for (int i = 0; i < idList.size(); i++) {
            final int finalIndex = i;
            String id = idList.get(i);
            Integer avatarId = AvatarHelper.getStaticAvatar(id);
            if (avatarId != null) {
                sortedBitmap.put(finalIndex, BitmapFactory.decodeResource(view.getResources(), avatarId));
                if (sortedBitmap.size() == size) {
                    displayJoinedBitmap(roomId, new ArrayList<>(sortedBitmap.values()), view);
                }
            } else {
                String url = AvatarHelper.getAvatarUrl(id, true);
                Glide.with(view.getContext().getApplicationContext())
                        .load(url)
                        .asBitmap()
                        .placeholder(R.drawable.avatar_normal)
                        .error(R.drawable.avatar_normal)
                        .dontAnimate()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                sortedBitmap.put(finalIndex, resource);
                                if (sortedBitmap.size() == size) {
                                    displayJoinedBitmap(roomId, new ArrayList<>(sortedBitmap.values()), view);
                                }
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                if (BuildConfig.DEBUG) {
                                    Log.i(TAG, "onLoadFailed: " + url);
                                }
                                // 使用默认图片
                                Bitmap resource = BitmapFactory.decodeResource(view.getResources(), R.drawable.avatar_normal);
                                sortedBitmap.put(finalIndex, resource);
                                if (sortedBitmap.size() == size) {
                                    displayJoinedBitmap(roomId, new ArrayList<>(sortedBitmap.values()), view);
                                }
                            }
                        });
            }
        }
    }

    private void displayJoinedBitmap(String roomId, List<Bitmap> bitmaps, ImageView view) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "displayJoinedBitmap: size = " + bitmaps.size());
        }
        if (bitmaps.size() == 1) {
            view.setImageBitmap(bitmaps.get(0));
            return;
        }
        // 群组组合头像不能设置为圆形，否则边角有缺，
        if (view instanceof RoundedImageView) {
            ((RoundedImageView) view).setOval(false);
            ((RoundedImageView) view).setCornerRadius(Constants.AVATAR_CORNER_RADIUS);
        }
        int width = view.getWidth();
        if (width > 0) {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.parseColor("#33000000"));
            JoinBitmaps.join(canvas, width, bitmaps, 0.15F);
            displayBitmap(roomId, bitmap, view);
        } else {
            // 加载太快可能布局还没加载出ner确保布局加载完成后再次设置头像，来，
            // 通过addOnLayoutChangeListe
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "displayJoinedBitmap: " + Integer.toHexString(view.hashCode()) + ".width = 0");
            }
            view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onLayoutChange: " + Integer.toHexString(view.hashCode()) + ".width = " + v.getWidth());
                    }
                    if (v.getWidth() > 0) {
                        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        JoinBitmaps.join(canvas, v.getWidth(), bitmaps, 0.15F);
                        displayBitmap(roomId, bitmap, view);
                        v.removeOnLayoutChangeListener(this);
                    } else {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "displayJoinedBitmap: " + Integer.toHexString(view.hashCode()) + ".width = 0");
                        }
                    }
                }
            });
        }
    }

    private void displayBitmap(String roomId, Bitmap bitmap, ImageView headView) {
        ImageCache.putBitmap(roomId, bitmap);
        headView.setImageBitmap(bitmap);
    }


    /**
     * 显示仿微信九宫格头像
     *
     * @param roomId
     * @param time
     * @param idList
     * @param headView
     */
    public void displayWeChatGroupAvatar(String roomId, String time, List<String> idList, HeadView headView) {
        ImageView view = headView.getHeadImage();
        List<String> urlList = new ArrayList<>();
        for (String str : idList) {
            String url = AvatarHelper.getAvatarUrl(str, true);
            urlList.add(url);
        }
        String[] array = new String[urlList.size()];
        String[] urls = urlList.toArray(array);
        CombineBitmap.init(mContext)
                .setLayoutManager(new WechatLayoutManager()) // 必选， 设置图片的组合形式，支持WechatLayoutManager、DingLayoutManager
                .setSize(60) // 必选，组合后Bitmap的尺寸，单位dp
                .setGap(1) // 单个图片之间的距离，单位dp，默认0dp
                .setGapColor(Color.parseColor("#dddee0")) // 单个图片间距的颜色，默认白色
                .setPlaceholder(R.drawable.avatar_normal) // 单个图片加载失败的默认显示图片
                .setUrls(urls) // 要加载的图片url数组
                //.setBitmaps() // 要加载的图片bitmap数组
                //.setResourceIds() // 要加载的图片资源id数组
                .setImageView(view) // 直接设置要显示图片的ImageView
                // 设置“子图片”的点击事件，需使用setImageView()，index和图片资源数组的索引对应
//                        .setOnSubItemClickListener(new OnSubItemClickListener() {
//                            @Override
//                            public void onSubItemClick(int index) {
//                            }
//                        })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onComplete(Bitmap bitmap) {
                        ImageCache.putBitmap(roomId, bitmap);
                    }
                })
                .build();

    }

    private void displayJoined(String roomId, List<String> idList, HeadView headView) {
        ImageView view = headView.getHeadImage();
        // 当前item项的头像个数
        int size = idList.size();
        TreeMap<Integer, Bitmap> sortedBitmap = new TreeMap<>();
        // 这里url的顺序是对的
        for (int i = 0; i < idList.size(); i++) {
            final int finalIndex = i;
            String id = idList.get(i);
            Integer avatarId = AvatarHelper.getStaticAvatar(id);
            Log.e("DisplayJoined", avatarId + "");
            if (avatarId != null) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "load resource: " + avatarId);
                }
                sortedBitmap.put(finalIndex, BitmapFactory.decodeResource(view.getResources(), avatarId));
                if (sortedBitmap.size() == size) {
                    displayJoinedBitmap(roomId, new ArrayList<>(sortedBitmap.values()), headView);
                }
            } else {
                String url = AvatarHelper.getAvatarUrl(id, true);
                Log.e("DisplayJoined", url + "");
                Glide.with(view.getContext().getApplicationContext())
                        .load(url)
                        .asBitmap()
                        .placeholder(R.drawable.avatar_normal)
                        .error(R.drawable.avatar_normal)
                        .dontAnimate()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                if (BuildConfig.DEBUG) {
                                    Log.i(TAG, "onResourceReady: " + url);
                                }
                                sortedBitmap.put(finalIndex, resource);
                                if (sortedBitmap.size() == size) {
                                    displayJoinedBitmap(roomId, new ArrayList<>(sortedBitmap.values()), headView);
                                }
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                if (BuildConfig.DEBUG) {
                                    Log.i(TAG, "onLoadFailed: " + url);
                                }
                                // 使用默认图片
                                Bitmap resource = BitmapFactory.decodeResource(view.getResources(), R.drawable.avatar_normal);
                                sortedBitmap.put(finalIndex, resource);
                                if (sortedBitmap.size() == size) {
                                    displayJoinedBitmap(roomId, new ArrayList<>(sortedBitmap.values()), headView);
                                }
                            }
                        });
            }
        }
    }

    private void displayJoinedBitmap(String roomId, List<Bitmap> bitmaps, HeadView headView) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "displayJoinedBitmap: size = " + bitmaps.size());
        }
        ImageView view = headView.getHeadImage();
        if (bitmaps.size() == 1) {
            view.setImageBitmap(bitmaps.get(0));
            return;
        }
        // 群组组合头像不能设置为圆形，否则边角有缺，
        headView.setRound(false);
        int width = view.getWidth();
        if (width > 0) {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            JoinBitmaps.join(canvas, width, bitmaps, 0.15F);
            displayBitmap(roomId, bitmap, headView);
        } else {
            // 加载太快可能布局还没加载出ner确保布局加载完成后再次设置头像，来，
            // 通过addOnLayoutChangeListe
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "displayJoinedBitmap: " + Integer.toHexString(view.hashCode()) + ".width = 0");
            }
            view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onLayoutChange: " + Integer.toHexString(view.hashCode()) + ".width = " + v.getWidth());
                    }
                    if (v.getWidth() > 0) {
                        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        JoinBitmaps.join(canvas, v.getWidth(), bitmaps, 0.15F);
                        displayBitmap(roomId, bitmap, headView);
                        v.removeOnLayoutChangeListener(this);
                    } else {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "displayJoinedBitmap: " + Integer.toHexString(view.hashCode()) + ".width = 0");
                        }
                    }
                }
            });
        }
    }

    private void displayBitmap(String roomId, Bitmap bitmap, HeadView headView) {
        ImageCache.putBitmap(roomId, bitmap);
        headView.getHeadImage().setImageBitmap(bitmap);
    }

    /**
     * 本地视频缩略图 缓存
     */
    public Bitmap displayVideoThumb(String videoFilePath, ImageView image) {
        if (TextUtils.isEmpty(videoFilePath)) {
            return null;
        }

        Bitmap bitmap = MyApplication.getInstance().getBitmapFromMemCache(videoFilePath);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = ThumbnailUtils.createVideoThumbnail(videoFilePath, MediaStore.Video.Thumbnails.MINI_KIND);
            // 视频格式不支持可能导致得到缩略图bitmap为空，LruCache不能接受空，
            // 主要是系统相册里的存着的视频不一定都是真实有效的，
            if (bitmap != null) {
                MyApplication.getInstance().addBitmapToMemoryCache(videoFilePath, bitmap);
            }
        }

        if (bitmap != null && !bitmap.isRecycled()) {
            image.setImageBitmap(bitmap);
        } else {
            image.setImageBitmap(null);
        }

        return bitmap;
    }

    /**
     * 在线视频缩略图获取显示 缓存
     */
    public void asyncDisplayOnlineVideoThumb(String url, ImageView image) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (mVideoThumbMap.containsKey(url)) {
            image.setImageBitmap(mVideoThumbMap.get(url));
            return;
        }
//        image.setTag(url);
        AsyncUtils.doAsync(this, t -> {
            Reporter.post("获取在线视频缩略图失败, " + url, t);
        }, c -> {
            MediaMetadataRetriever retr = new MediaMetadataRetriever();
            Uri uri = Uri.parse(url);
            if (TextUtils.equals(uri.getScheme(), "file")) {
                // 本地文件不能使用file://开头的url加载，
                retr.setDataSource(uri.getPath());
            } else {
                retr.setDataSource(url, new HashMap<>());
            }
            Bitmap bitmap = retr.getFrameAtTime();
            mVideoThumbMap.put(url, bitmap);
            c.uiThread(r -> {
                image.setImageBitmap(bitmap);
/*
                if (image.getTag() == url) {
                    image.setImageBitmap(bitmap);
                }
*/
            });
        });
    }

    /**
     * 从缓存中读取 头像更新时间
     *
     * @param userId
     * @return
     */
    private String getAvatarTime(String userId) {
        String time = "";
        if (timeMap == null) {
            timeMap = new ConcurrentHashMap<>();
        }
        if (timeMap.containsKey(userId)) {
            time = timeMap.get(userId);
        } else {
            time = UserAvatarDao.getInstance().getUpdateTime(userId);
            timeMap.put(userId, time);
        }
        return time;
    }

    /**
     * 加载网络图片
     */
    public void displayUrl(String url, ImageView imageView, int errid) {

        Glide.with(MyApplication.getContext())
                .load(url)
                .error(errid)
                .into(imageView);
    }

    /**
     * 加载网络图片
     */
    public void displayUrl(String url, ImageView imageView, int errid, int w, int h) {
        imageView.setTag(R.id.key_avatar, url);
        Glide.with(MyApplication.getContext())
                .load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(R.drawable.fez)
                .fitCenter()
                .override(w, h)
                .error(errid)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                        imageView.setImageBitmap(resource);
                    }
                });
    }

    /**
     * 加载网络图片
     */
    public void displayUrl(String url, int errid, SimpleTarget<Bitmap> listener) {

        Glide.with(MyApplication.getContext())
                .load(url)
                .asBitmap()
                .error(errid)
                .into(listener);

    }

    public void displayUrl(String url, ImageView imageView) {
        displayUrl(url, imageView, R.drawable.image_download_fail_icon);
    }

    public void displayMapUrl(String url, ImageView imageView) {
        displayUrl(url, imageView, R.drawable.icon_chat_map_bg);
    }

    // 根据文件类型填充图片
    public void fillFileView(String type, ImageView v) {
        if (type.equals("mp3")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_y);
        } else if (type.equals("mp4") || type.equals("avi")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_v);
        } else if (type.equals("xls")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_x);
        } else if (type.equals("doc")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_w);
        } else if (type.equals("ppt")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_p);
        } else if (type.equals("pdf")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_f);
        } else if (type.equals("apk")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_a);
        } else if (type.equals("txt")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_t);
        } else if (type.equals("rar") || type.equals("zip")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_z);
        } else {
            v.setImageResource(R.drawable.ic_muc_flie_type_what);
        }
    }
}
