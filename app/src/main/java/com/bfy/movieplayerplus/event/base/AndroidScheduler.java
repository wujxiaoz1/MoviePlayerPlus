package com.bfy.movieplayerplus.event.base;

import android.text.TextUtils;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/6/28 0028
 * @modifyDate : 2017/6/28 0028
 * @version    : 1.0
 * @desc       : Android UI线程调度器，最终是用MainThreadExecutor进行任务调度。
 * 代码大体和cache scheduler一样，为了区别两者所以分开实现，方便后期做修改
 * </pre>
 */

public class AndroidScheduler extends Scheduler {

    @Override
    public Worker createWorker(Object... args) {
        Platform platform = Platform.getInstance(Platform.TYPE_UI_THREAD_POOL);
        EventBuilder.Event event;
        Runnable work;
        if (args != null && args.length == 2) {
            if (args[0] instanceof EventBuilder.Event) {
                event = (EventBuilder.Event) args[0];
            }else {
                throw new ClassCastException("the args[0] cannot cast to Event object.");
            }
            if (args[1] instanceof Runnable) {
                work = (Runnable) args[1];
            }else {
                throw new ClassCastException("the args[1] cannot cast to Runnable object.");
            }
        }else {
            throw new NullPointerException("the args is null, or size is not 2.");
        }
        return new AndroidWorker(platform, event, work);
    }

    static class AndroidWorker extends Worker implements Subscription{

        private EventBuilder.Event mEvent = null;

        private boolean mUnsubscribe = false;

        private Platform mPlatform = null;

        private Runnable mWork = null;

        public AndroidWorker(Platform platform, EventBuilder.Event event, Runnable work) {
            mPlatform = platform;
            mEvent = event;
            mWork = work;
        }

        @Override
        public void unsubscribe() {
            mUnsubscribe = true;
            mEvent.setUnsubscribe(true);
            mPlatform.cancel(mEvent.sessionId);
        }

        @Override
        public boolean isUnsubscribed() {
            return mUnsubscribe;
        }

        @Override
        public Subscription schedule() {
            if (mUnsubscribe) {
                return new Unsubscribed();
            }

            ScheduledAction action = new ScheduledAction(mEvent, mPlatform, mWork);
            mPlatform.execute(action);
            return action;
        }

        @Override
        public Subscription schedule(long delayTime, TimeUnit unit) {
            //TODO unimpliment 延时调度方法
            return null;
        }
    }

    static class ScheduledAction implements Runnable, Subscription{

        private Platform mPlatform;
        private EventBuilder.Event mEvent;
        private boolean mUnsubscribe;
        private Runnable mWork;

        public ScheduledAction(EventBuilder.Event event, Platform platform, Runnable work) {
            mPlatform = platform;
            mEvent = event;
            mWork = work;
        }

        @Override
        public void unsubscribe() {
            mUnsubscribe = true;
            mEvent.setUnsubscribe(true);
            mPlatform.cancel(mEvent.sessionId);
        }

        @Override
        public boolean isUnsubscribed() {
            return mUnsubscribe;
        }

        @Override
        public void run() {
            if (mWork != null) {
                mWork.run();
            }
        }
    }


}
