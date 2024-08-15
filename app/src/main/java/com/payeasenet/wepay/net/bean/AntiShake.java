package com.payeasenet.wepay.net.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 类 <code>AntiShake</code>
 * <p>
 * 描述：
 * </p>
 * 创建日期：2019年10月31日
 *
 * @author zhaoyong.chen@ehking.com
 * @version 1.0
 */


public class AntiShake {
        private static List<OneClick> mOneClickList = new ArrayList<>();

        private static long mDelayTime =0;

        /**
         * 检查是否在短时间内连续点击
         *
         * @param obj
         * @return true 是
         */
        public static boolean check(Object obj) {
            String flag;
            if (obj == null) {
                flag = Thread.currentThread().getStackTrace()[0].getMethodName();
            } else {
                flag = obj.toString();
            }
            for (OneClick oneClick : mOneClickList) {
                if (oneClick.getMethodName().equals(flag)) {
                    return oneClick.check();
                }
            }
            OneClick oneClick = new OneClick(flag);
            if (mDelayTime > 0) {
                oneClick.setClickDelayTime(mDelayTime);
            }
            mOneClickList.add(oneClick);
            return oneClick.check();
        }

        /**
         * 检查是否在设定时间内连续点击
         *
         * @param delayTime 不在再次点击的时间间隔
         * @param obj
         * @return
         */
        public static boolean check(long delayTime, Object obj) {
            setClickDelayTime(delayTime);
            return check(obj);
        }

        public static void setClickDelayTime(long delayTime) {
            mDelayTime = delayTime;
        }

        static class OneClick {

            private long clickDelayTime = 1000L;

            private long lastTime;

            private final String methodName;

            public OneClick(final String methodName) {
                this.methodName = methodName;
            }

            /**
             * 当前时间与上一次时间间隔大于延迟返回false
             *
             * @return
             */
            boolean check() {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastTime > clickDelayTime) {
                    lastTime = currentTime;
                    return false;
                }
                return true;
            }

            String getMethodName() {
                return methodName;
            }

            void setClickDelayTime(long delayTime) {
                clickDelayTime = delayTime;
            }
        }
    }

