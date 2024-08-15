-optimizationpasses 5                                                    # 指定代码的压缩级别
-dontwarn
-dontusemixedcaseclassnames                                              # 是否使用大小写混合
-dontskipnonpubliclibraryclasses                                         # 是否混淆第三方jar
-dontpreverify                                                           # 混淆时是否做预校验
-verbose                                                                 # 混淆时是否记录日志
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/* # 混淆时所采用的算法

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
# 保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
 # 保持自定义控件类不被混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
 # 保持自定义控件类不被混淆
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
 # 保持枚举 enum 类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
 # 保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
# 保持自己定义的类不被混淆
-keep class MyClass

#如果有引用v4包可以添加下面这行
-keep class android.support.v4.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

#如果引用了v4或者v7包，可以忽略警告，因为用不到android.support
-dontwarn android.support.**

#保持自定义组件不被混淆
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

#保持 Serializable 不被混淆
-keep class * implements java.io.Serializable

#保持 Serializable 不被混淆并且enum 类也不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#保持枚举 enum 类不被混淆 如果混淆报错，建议直接使用上面的 -keepclassmembers class * implements java.io.Serializable即可
-keepclassmembers enum * {
  public static **[] values();
 public static ** valueOf(java.lang.String);
}

-keepclassmembers class * {
    public void *ButtonClicked(android.view.View);
}

#不混淆资源类
-keepclassmembers class **.R$* {
    public static <fields>;
}

#xUtils(保持注解，及使用注解的Activity不被混淆，不然会影响Activity中你使用注解相关的代码无法使用)
-keep class * extends java.lang.annotation.Annotation {*;}
-keep class com.iimm.miliao.activity.** {*;}

# 以libaray的形式引用的图片加载框架,不想混淆（注意，此处不是jar包形式，想混淆去掉"#"）
-keep class org.doubango.ngn.events.** { *; }
-keep class org.doubango.ngn.events.**
-keep class org.doubango.ngn.media.** { *; }
-keep class org.doubango.ngn.media.**
-keep class org.doubango.ngn.model.** { *; }
-keep class org.doubango.ngn.model.**
-keep class org.doubango.ngn.services.** { *; }
-keep class org.doubango.ngn.services.**
-keep class org.doubango.ngn.sip.** { *; }
-keep class org.doubango.ngn.sip.**
-keep class org.doubango.ngn.utils.** { *; }
-keep class org.doubango.ngn.utils.**
-keep class org.doubango.ngn.** { *; }
-keep class org.doubango.ngn.**
-keep class org.doubango.tinyWRAP.** { *; }
-keep class org.doubango.tinyWRAP.**
-keep class de.greenrobot.event.** { *; }
-keep class de.greenrobot.event.**
-keep class de.greenrobot.event.util.** { *; }
-keep class de.greenrobot.event.util.**
-keep class org.jivesoftware.smack.initializer**
-keep class org.jivesoftware.smack.**
-keep class org.jivesoftware.smackx.**
-keep class org.jivesoftware.smackx.**{*;}
-keep class org.jivesoftware.smack.compression.**
-keepattributes EnclosingMethod
-keep class org.jivesoftware.smack.initializer.** { *; }
-keep class org.jivesoftware.smack.** { *; }
-keep class com.github.siyamed.shapeimageview.**
-dontwarn  com.github.siyamed.shapeimageview.**
-keep class org.kxml2.io.**
-keep class org.kxml2.io.KXmlParser
-dontwarn  org.kxml2.io.KXmlParser
-keep class org.kxml2.io.**{*;}
-keep class com.github.siyamed.shapeimageview.**{*;}
# ormlite
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }
###-------- Gson 相关的混淆配置--------
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }

-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}
-keepclassmembers class ** {
    public void onEvent*(**);
    void onEvent*(**);
}
-dontwarn net.sourceforge.**
-keep class net.sourceforge.** { *;}

###-------- pulltorefresh 相关的混淆配置---------
-dontwarn com.handmark.pulltorefresh.library.**
-keep class com.handmark.pulltorefresh.library.** { *;}
-dontwarn com.handmark.pulltorefresh.library.extras.**
-keep class com.handmark.pulltorefresh.library.extras.** { *;}
-dontwarn com.handmark.pulltorefresh.library.internal.**
-keep class com.handmark.pulltorefresh.library.internal.** { *;}

###-------- ShareSDK 相关的混淆配置---------
-keep class cn.sharesdk.**{*;}
-keep class com.sina.**{*;}
-keep class **.R$* {*;}
-keep class **.R{*;}
-keep class com.mob.**{*;}
-keep class m.framework.**{*;}
-dontwarn cn.sharesdk.**
-dontwarn com.sina.**
-dontwarn com.mob.**
-dontwarn **.R$*

###--------------umeng 社会化分享相关的混淆配置-----------
-dontshrink
-dontoptimize
-dontwarn com.google.android.maps.**
-dontwarn android.webkit.WebView
-dontwarn com.umeng.**
-dontwarn com.tencent.weibo.sdk.**
-dontwarn com.facebook.**
-dontwarn java.lang.invoke**
-dontwarn org.apache.lang.**
-dontwarn org.apache.commons.**
-dontwarn com.nhaarman.**
-dontwarn se.emilsjolander.**

-keep enum com.facebook.**
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

-keep public interface com.facebook.**
-keep public interface com.tencent.**
-keep public interface com.umeng.socialize.**
-keep public interface com.umeng.socialize.sensor.**
-keep public interface com.umeng.scrshot.**

-keep public class com.umeng.socialize.* {*;}
-keep public class javax.**
-keep public class android.webkit.**

-keep class com.facebook.**
-keep class com.facebook.** { *; }
-keep class com.umeng.scrshot.**
-keep public class com.tencent.** {*;}
-keep class com.umeng.socialize.sensor.**
-keep class com.umeng.socialize.handler.**
-keep class com.umeng.socialize.handler.*
-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.modelmsg.** implements com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}

-keep class im.yixin.sdk.api.YXMessage {*;}
-keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{*;}

-dontwarn twitter4j.**
-keep class twitter4j.** { *; }

-keep class com.tencent.** {*;}
-dontwarn com.tencent.**
-keep public class com.umeng.soexample.R$*{
    public static final int *;
}
-keep public class com.umeng.soexample.R$*{
    public static final int *;
}
-keep class com.tencent.open.TDialog$*
-keep class com.tencent.open.TDialog$* {*;}
-keep class com.tencent.open.PKDialog
-keep class com.tencent.open.PKDialog {*;}
-keep class com.tencent.open.PKDialog$*
-keep class com.tencent.open.PKDialog$* {*;}

-keep class com.sina.** {*;}
-dontwarn com.sina.**
-keep class  com.alipay.share.sdk.** {
   *;
}
###--------------umeng 统计相关的混淆配置-----------
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keep public class **.R$*{
public static final int *;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
###-------Parcelable------------
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
###--------百度地图----------
#-libraryjars libs/BaiduLBS_Android.jar
-keep class com.baidu.** { *; }
-keep class vi.com.gdi.bgl.android.**{*;}
-keep class com.baidu.location.** { *; }
-keep class com.linkedin.** { *; }
-keep class mapsdkvi.com.** {*;}

-keepattributes Signature
 ###--------百度推送----------
#-libraryjars libs/pushservice-6.1.1.21.jar
-dontwarn com.baidu.**
-keep class com.baidu.**{*; }
###--------eventbus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
###-----------okhttp
-dontwarn com.squareup.okhttp.**
-dontwarn org.xbill.DNS.spi.**
-dontwarn org.simpleframework.xml.stream.**
-dontwarn demo.**
-dontwarn com.alibaba.fastjson.util.**
-dontwarn com.alibaba.fastjson.support.spring.**
-dontwarn com.alibaba.fastjson.serializer.**

-keep class com.squareup.okhttp.** { }
-dontwarn okio.*
-keep class com.squareup.okhttp3.** {
*;
}
###------------twitter
-dontwarn com.google.appengine.api.urlfetch.**
-dontwarn rx.**
-dontwarn retrofit.**
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep class retrofit.** { *; }
-keepclasseswithmembers class * { @retrofit.http.* <methods>; }
-ignorewarnings

#部分library不混淆
-keep class org.wysaid.** {*;}
-keep class com.dubu.livecamera.** {*;}
-keep class com.coremedia.** {*;}
-keep class com.github.** {*;}
-keep class com.googlecode.** {*;}
-keep class com.seu.** {*;}
-keep class net.ossrs.** {*;}
-keep class com.dou361.** {*;}
-keep class tv.** {*;}

# bean类，主要用于json to bean, 不能混淆，
-keep class com.xuan.xuanhttplibrary.okhttp.result.** { *; }
-keep class com.iimm.miliao.util.SkinUtils.Skin { *; }
-keep class com.iimm.miliao.bean.** { *; }
-keep class com.iimm.miliao.ui.live.bean.** { *; }
-keep class com.iimm.miliao.ui.mucfile.bean.** { *; }

-keep,allowobfuscation @interface com.facebook.proguard.annotations.DoNotStrip
-keep,allowobfuscation @interface com.facebook.proguard.annotations.KeepGettersAndSetters
-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip

# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.proguard.annotations.DoNotStrip class *
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.proguard.annotations.DoNotStrip *;
    @com.facebook.common.internal.DoNotStrip *;
}

-keepclassmembers @com.facebook.proguard.annotations.KeepGettersAndSetters class * {
  void set*(***);
  *** get*();
}

-keep class * extends com.facebook.react.bridge.JavaScriptModule { *; }
-keep class * extends com.facebook.react.bridge.NativeModule { *; }
-keepclassmembers,includedescriptorclasses class * { native <methods>; }
-keepclassmembers class *  { @com.facebook.react.uimanager.UIProp <fields>; }
-keepclassmembers class *  { @com.facebook.react.uimanager.annotations.ReactProp <methods>; }
-keepclassmembers class *  { @com.facebook.react.uimanager.annotations.ReactPropGroup <methods>; }

-dontwarn com.facebook.react.**

# TextLayoutBuilder uses a non-public Android constructor within StaticLayout.
# See libs/proxy/src/main/java/com/facebook/fbui/textlayoutbuilder/proxy for details.
-dontwarn android.text.StaticLayout

# okhttp

-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# okio

-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**

# WebRTC

-keep class org.webrtc.** { *; }
-dontwarn org.chromium.build.BuildHooksAndroid

# Jisti Meet SDK

-keep class org.jitsi.meet.sdk.** { *; }

# jpush, https://docs.jiguang.cn/jpush/client/Android/android_guide/
-dontoptimize
-dontpreverify
-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }
-keep class * extends cn.jpush.android.helpers.JPushMessageReceiver { *; }
-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }

# 华为推送，https://developer.huawei.com/consumer/cn/service/hms/catalog/huaweipush_agent.html?page=hmssdk_huaweipush_devprepare_agent#6%20%E9%85%8D%E7%BD%AE%E6%B7%B7%E6%B7%86%E8%84%9A%E6%9C%AC
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.hianalytics.android.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}
-keep class com.huawei.android.hms.agent.**{*;}

# mipush, https://dev.mi.com/console/doc/detail?pId=41#_3_3
-keep public class * extends com.xiaomi.mipush.sdk.PushMessageReceiver {*;}
#可以防止一个误报的 warning 导致无法成功编译，如果编译使用的 Android 版本是 23。
-dontwarn com.xiaomi.push.**

# 支付宝，https://docs.open.alipay.com/54/104509/
-keep class com.iimm.miliao.ui.me.redpacket.alipay.** { *; }
-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}
-keep class com.alipay.sdk.app.H5PayCallback {
    <fields>;
    <methods>;
}
-keep class com.alipay.android.phone.mrpc.core.** { *; }
-keep class com.alipay.apmobilesecuritysdk.** { *; }
-keep class com.alipay.mobile.framework.service.annotation.** { *; }
-keep class com.alipay.mobilesecuritysdk.face.** { *; }
-keep class com.alipay.tscenter.biz.rpc.** { *; }
-keep class org.json.alipay.** { *; }
-keep class com.alipay.tscenter.** { *; }
-keep class com.ta.utdid2.** { *;}
-keep class com.ut.device.** { *;}

# 魅族推送，https://github.com/MEIZUPUSH/PushDemo/blob/3367bbc290de17487fdeba66e44c56cacaa6141f/PushdemoInternal/artifactory-proguard-rules.pro#L40-L39
-keep public class * extends com.google.protobuf.GeneratedMessage { *; }
-keep class com.google.protobuf.** { *; }
-keep public class * extends com.google.protobuf.** { *; }
-keep class com.meizu.cloud.pushsdk.** { *; }
-dontwarn  com.meizu.cloud.pushsdk.**
-keep class com.meizu.nebula.** { *; }
-dontwarn com.meizu.nebula.**
-keep class com.meizu.push.** { *; }
-dontwarn com.meizu.push.**

# VIVO推送，
-dontwarn com.vivo.push.**
-keep class com.vivo.push.**{*; }
-keep class com.vivo.vms.**{*; }
-keep  class * extends com.vivo.push.sdk.OpenClientPushMessageReceiver {*;}

# Oppo推送，看起来是多余的，https://open.oppomobile.com/wiki/doc#id=10196
-keep public class * extends android.app.Service

# slf4j, 有出现过slf4j混淆导致slf4j-android崩溃的情况，
-keep class org.slf4j.**{*; }

#透明状态栏
-keep class com.gyf.immersionbar.* {*;}
-dontwarn com.gyf.immersionbar.**

#webview
-keep class com.just.agentweb.** {
    *;
}
-dontwarn com.just.agentweb.**
-keepclassmembers class com.just.agentweb.sample.common.AndroidInterface{ *; }
#Glide
-dontwarn com.bumptech.glide.**
-keep class com.bumptech.glide.**{*;}
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#不混淆 design 包
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }
#不混淆 gif  包
-dontwarn pl.droidsonroids.gif.**
#新版微信sdk混淆
-keep class com.tencent.mm.opensdk.** {
    *;
}

-keep class com.tencent.wxop.** {
    *;
}

-keep class com.tencent.mm.sdk.** {
    *;
}


#指定代码的压缩级别
 -optimizationpasses 7

 #包明不混合大小写
 -dontusemixedcaseclassnames

 #不去忽略非公共的库类
 -dontskipnonpubliclibraryclasses

 #预校验
 -dontpreverify

 #混淆时是否记录日志
 -verbose

 # 混淆时所采用的算法
 -optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

 #保护注解
 -keepattributes *Annotation*
 #如果引用了v4或者v7包
 -dontwarn android.support.**
 -keep class android.support.**{*;}
 #保持 native 方法不被混淆
 -keepclasseswithmembernames class * {
 native <methods>;
 }

 #保持自定义控件类不被混淆
 -keepclasseswithmembers class * {
 public <init>(android.content.Context, android.util.AttributeSet);
 }

 #排除所有注解类
 -keep class * extends java.lang.annotation.Annotation { *; }
 -keep interface * extends java.lang.annotation.Annotation { *; }

 #保持自定义控件类不被混淆
 -keepclassmembers class * extends android.app.Activity {
 public void *(android.view.View);
 }

 #保持 Parcelable 不被混淆
 -keep class * implements android.os.Parcelable {
 public static final android.os.Parcelable$Creator *;
 }

 #保持 Serializable 不被混淆
 -keepnames class * implements java.io.Serializable

 #保持 Serializable 不被混淆并且enum 类也不被混淆
 -keepclassmembers class * implements java.io.Serializable {
 static final long serialVersionUID;
 private static final java.io.ObjectStreamField[] serialPersistentFields;
 !static !transient <fields>;
 !private <fields>;
 !private <methods>;
 private void writeObject(java.io.ObjectOutputStream);
 private void readObject(java.io.ObjectInputStream);
 java.lang.Object writeReplace();
 java.lang.Object readResolve();
 }

 #log
 -assumenosideeffects class android.util.Log {
 public static boolean isLoggable(java.lang.String, int);
 public static int v(...);
 public static int i(...);
 public static int w(...);
 public static int d(...);
 public static int e(...);
 }


 #保持枚举 enum 类不被混淆 如果混淆报错，建议直接使用上面的 -keepclassmembers class * implements java.io.Serializable即可
 -keepclassmembers enum * {
   public static **[] values();
   public static ** valueOf(java.lang.String);
 }

 -keepclassmembers class * {
 public void *ButtonClicked(android.view.View);
 }

 #不混淆资源类
 -keepclassmembers class **.R$* {
 public static <fields>;
 }

 -keepclassmembers class fqcn.of.javascript.interface.for.webview {
    public *;
 }

 -keepattributes EnclosingMethod

 #ehking common lib
 -dontwarn com.ehking.common.**
 -keep class com.ehking.common.** {
     public <methods>;
     public <fields>;
 }

 # for DexGuard only
 #-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

 #retrofit
 # Platform calls Class.forName on types which do not exist on Android to determine platform.

 -dontwarn retrofit2.**
 -dontnote retrofit2.Platform
 # Platform used when running on Java 8 VMs. Will not be used at runtime.
 -dontwarn retrofit2.Platform$Java8
 # Retain generic type information for use by reflection by converters and adapters.
 -keepattributes Signature
 # Retain declared checked exceptions for use by a Proxy instance.
 -keepattributes Exceptions

 #okio
 -dontwarn okio.**
 #glide
 -keep public class * implements com.bumptech.glide.module.GlideModule
 -keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
   **[] $VALUES;
   public *;
 }

 #zxing
 -dontwarn com.google.zxing.**
 -keep class com.google.zxing.**{
     *;
 }

 #rxandroid
 -dontwarn io.reactivex.android.**
 -keep class io.reactivex.android.**{
     public *;
 }

 #gson混淆
 -dontwarn com.google.gson.**
 -keep class sun.misc.Unsafe { *; }
 -keep class com.google.gson.** { *; }
 -keep class com.google.gson.examples.android.model.** { *; }
 -keep class com.google.** {
     <fields>;
     <methods>;
 }

 #eventbus混淆
 -keepclassmembers class ** {
     @org.greenrobot.eventbus.Subscribe <methods>;
 }
 -keep enum org.greenrobot.eventbus.ThreadMode { *; }

 #rxjava混淆
 -dontwarn sun.misc.**
 -keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
  long producerIndex;
  long consumerIndex;
 }
 -keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
  rx.internal.util.atomic.LinkedQueueNode producerNode;
 }
 -keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
  rx.internal.util.atomic.LinkedQueueNode consumerNode;
 }

 #fastjson
 -dontwarn com.alibaba.fastjson.**
 -keep class com.alibaba.fastjson.**{
     *;
 }

 #ok http
 -dontwarn okhttp3.**
 -keep class okhttp3.**{
     *;
 }



 #app
 -keep class com.ehking.wepay.net.bean.**{
     *;
 }

 -keep class com.ehking.crypt.**{
     *;
 }

 -keep class com.ehking.sdk.wepay.net.bean.**{
      *;
  }

 -keep class  com.ehking.sdk.wepay.net.**{
     *;
 }

 -keep class  com.ehking.sdk.wepay.net.exception.**{
     *;
 }
 -keep class net.bean.**{
     *;
 }

 -keep class com.ehking.wepay.ui.**{
      *;
  }

 -dontwarn com.livedetect.**
 -keep class com.livedetect.**{
        *;
    }
 -dontwarn ccom.sensetime..**
 -keep class com.sensetime.**{
        *;
    }

 -keep class com.hisign.**{
         *;
  }
  -keep class com.ehking.sdk.wepay.ui.**{
      *;
  }
 -keep class com.ehking.sdk.wepay.interfaces.**{
      *;
  }
  -keep class com.ehking.sdk.wepay.interfaces.WalletPay$Companion{
      *;
  }
  -keep class com.ehking.sdk.wepay.interfaces.WalletPay$WalletPayCallback{
       *;
   }
 -dontwarn android.databinding.**
 -keep class com.ehking.sdk.wepay.databinding.** {
     <fields>;
     <methods>;
 }

 -keep class **.R$* { *; }


 -keepattributes *Annotation*
 -keep class kotlin.** { *; }
 -keep class org.jetbrains.** { *; }

 -keep class com.ehking.sdk.wepay.utlis.**{
       *;
   }
 -keep class com.ehking.wepay.utlis.ToastUtil {
       <fields>;
       <methods>;
   }
 -keep class android.databinding.DataBindingComponent {
         <fields>;
         <methods>;
     }
 -keep class com.ehking.sdk.wepay.DataBinderMapperImpl {
               <fields>;
               <methods>;
    }
 -keep class  com.ehking.sdk.wepay.BR {
                <fields>;
                <methods>;
     }







     #retrofit2  混淆
     -dontwarn javax.annotation.**
     -dontwarn javax.inject.**
     # OkHttp3
     -dontwarn okhttp3.logging.**
     -keep class okhttp3.internal.**{*;}
     -dontwarn okio.**
     # Retrofit
     -dontwarn retrofit2.**
     -keep class retrofit2.** { *; }
     -keepattributes Signature
     -keepattributes Exceptions
     # RxJava RxAndroid
     -dontwarn sun.misc.**
     -keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
         long producerIndex;
         long consumerIndex;
     }
     -keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
         rx.internal.util.atomic.LinkedQueueNode producerNode;
     }
     -keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
         rx.internal.util.atomic.LinkedQueueNode consumerNode;
     }

     # Gson
     -keep class com.google.gson.stream.** { *; }
     -keepattributes EnclosingMethod
     -keep class com.mvp.entitry.**{*;}#改成自己的实体类包


# 优化 不优化输入的类文件
-dontoptimize

# androidx

-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**

# android.support.v4

-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-keep public class * extends android.support.v4.**

# WindAd

-keep class com.sigmob.sdk.**{ *;}
-keep interface com.sigmob.sdk.**{ *;}
-keep class com.sigmob.windad.**{ *;}
-keep interface com.sigmob.windad.**{ *;}
-keep class com.czhj.**{ *;}
-keep interface com.czhj.**{ *;}
-keep class com.tan.mark.**{*;}

# miitmdid

-dontwarn com.bun.**
-keep class com.bun.** {*;}
-keep class a.**{*;}
-keep class XI.CA.XI.**{*;}
-keep class XI.K0.XI.**{*;}
-keep class XI.XI.K0.**{*;}
-keep class XI.vs.K0.**{*;}
-keep class XI.xo.XI.XI.**{*;}
-keep class com.asus.msa.SupplementaryDID.**{*;}
-keep class com.asus.msa.sdid.**{*;}
-keep class com.huawei.hms.ads.identifier.**{*;}
-keep class com.samsung.android.deviceidservice.**{*;}
-keep class com.zui.opendeviceidlibrary.**{*;}
-keep class org.json.**{*;}
-keep public class com.netease.nis.sdkwrapper.Utils {public <methods>;}


# Gson
-keep class com.payeasenet.wepay.net.bean.** { *; } # 自定义数据模型的bean目录


-keep class bykvm*.**
-keep class com.bytedance.msdk.adapter.**{ public *; }
-keep class com.bytedance.msdk.api.** {
 public *;
}
-keep class com.bytedance.msdk.base.TTBaseAd{*;}
-keep class com.bytedance.msdk.adapter.TTAbsAdLoaderAdapter{
    public *;
    protected <fields>;
}

# baidu sdk 不接入baidu sdk可以不引入
-ignorewarnings
-dontwarn com.baidu.mobads.sdk.api.**
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.baidu.mobads.** { *; }
-keep class com.style.widget.** {*;}
-keep class com.component.** {*;}
-keep class com.baidu.ad.magic.flute.** {*;}
-keep class com.baidu.mobstat.forbes.** {*;}

#ks  不接入ks sdk可以不引入
-keep class org.chromium.** {*;}
-keep class org.chromium.** { *; }
-keep class aegon.chrome.** { *; }
-keep class com.kwai.**{ *; }
-dontwarn com.kwai.**
-dontwarn com.kwad.**
-dontwarn com.ksad.**
-dontwarn aegon.chrome.**

# Admob 不接入admob sdk可以不引入
-keep class com.google.android.gms.ads.MobileAds {
 public *;
}

#sigmob  不接入sigmob sdk可以不引入
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-keep public class * extends android.support.v4.**

-keep class sun.misc.Unsafe { *; }
-dontwarn com.sigmob.**
-keep class com.sigmob.**.**{*;}

#oaid 不同的版本混淆代码不太一致，你注意你接入的oaid版本 ，不接入oaid可以不添加
-dontwarn com.bun.**
-keep class com.bun.** {*;}
-keep class a.**{*;}
-keep class XI.CA.XI.**{*;}
-keep class XI.K0.XI.**{*;}
-keep class XI.XI.K0.**{*;}
-keep class XI.vs.K0.**{*;}
-keep class XI.xo.XI.XI.**{*;}
-keep class com.asus.msa.SupplementaryDID.**{*;}
-keep class com.asus.msa.sdid.**{*;}
-keep class com.huawei.hms.ads.identifier.**{*;}
-keep class com.samsung.android.deviceidservice.**{*;}
-keep class com.zui.opendeviceidlibrary.**{*;}
-keep class org.json.**{*;}
-keep public class com.netease.nis.sdkwrapper.Utils {public <methods>;}


#klevin 游可赢
-keep class com.tencent.tgpa.**{*;}
-keep class com.tencent.klevin.**{*;}


#Mintegral 不接入Mintegral sdk，可以不引入
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mbridge.** {*; }
-keep interface com.mbridge.** {*; }
-keep class android.support.v4.** { *; }
-dontwarn com.mbridge.**
-keep class **.R$* { public static final int mbridge*; }
