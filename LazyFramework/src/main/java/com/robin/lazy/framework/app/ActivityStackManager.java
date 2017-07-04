/*
 * 文 件 名:  AppManager.java
 * 版    权:  jiang yu feng 
 * 描    述:  <描述>
 * 修 改 人:  江钰锋
 * 修改时间:  2013-11-29
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.robin.lazy.framework.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import java.util.Iterator;
import java.util.Stack;

/**
 * Activity管理类
 *
 * @author 江钰锋
 * @version [版本号, 2013-11-29]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class ActivityStackManager {
    private static Stack<Activity> activityStack = new Stack<Activity>();

    /**
     * 添加Activity到堆栈栈顶
     */
    public synchronized void pushActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        if (activityStack != null) {
            activityStack.push(activity);//把项压入堆栈顶部
        }
    }

    /**
     * 删除对应的activity
     */
    public void removeActivity(Activity activity) {
        if (activityStack != null && activityStack.contains(activity)) {
            activityStack.remove(activity);
        }
    }

    /***
     * 返回堆栈Activity数量
     *
     * @return
     */
    public int getActivityCount() {
        if (activityStack != null) {
            return activityStack.size();
        }
        return 0;
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的,最顶部的）
     */
    public Activity topActivity() {
        Activity activity = null;
        if (activityStack != null && !activityStack.isEmpty()) {
            activity = activityStack.peek();//查看堆栈顶部的对象，但不从堆栈中移除它。
        }
        return activity;
    }

    /**
     * 结束当前acitivity（堆栈中最后一个）
     */
    public void finishActivity() {
        if (activityStack == null) return;
        if (activityStack != null && !activityStack.isEmpty()) {
            activityStack.pop().finish();//移除堆栈顶部的对象，并作为此函数的值返回该对象
        }
    }

    /**
     * 查找到制定类名的acitivity(如果有多个则返回第一个)
     *
     * @param clazz
     * @see [类、类#方法、类#成员]
     */
    public <T extends Activity> T findActivity(Class<T> clazz) {
        if (activityStack != null) {
            for (Activity activity : activityStack) {
                if (activity.getClass().equals(clazz)) {
                    return (T) activity;
                }
            }
        }
        return null;
    }

    /**
     * 结束指定类名的acitivity
     *
     * @param cls
     * @see [类、类#方法、类#成员]
     */
    public void finishActivity(Class<?> cls) {
        if (activityStack == null) return;
        for (Iterator iter = activityStack.iterator(); iter.hasNext(); ) {
            Activity activity = (Activity) iter.next();
            if (activity.getClass().equals(cls)) {
                iter.remove();
                activity.finish();
            }
        }
    }

    /**
     * 结束指定类名的acitivity
     *
     * @param cls
     * @param requestCode 返回码
     * @see [类、类#方法、类#成员]
     */
    public void finishActivity(Class<?> cls, int requestCode) {
        if (activityStack == null) return;
        for (Iterator iter = activityStack.iterator(); iter.hasNext(); ) {
            Activity activity = (Activity) iter.next();
            if (activity.getClass().equals(cls)) {
                iter.remove();
                activity.finishActivity(requestCode);
            }
        }
    }

    /**
     * 结束指定的acitivity
     *
     * @param activity
     * @see [类、类#方法、类#成员]
     */
    public void finishActivity(Activity activity) {
        if (activityStack == null) return;
        if (activity != null && activityStack.contains(activity)) {
            activityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    /**
     * 结束指定的acitivity(带返回值的)
     *
     * @param activity
     * @see [类、类#方法、类#成员]
     */
    public void finishActivity(Activity activity, int requestCode) {
        if (activityStack == null) return;
        if (activity != null && activityStack.contains(activity)) {
            activityStack.remove(activity);
            activity.finishActivity(requestCode);
            activity = null;
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        if (activityStack == null) return;
        for (Iterator iter = activityStack.iterator(); iter.hasNext(); ) {
            Activity activity = (Activity) iter.next();
            iter.remove();
            activity.finish();
        }
        activityStack.clear();
    }

    /**
     * 的到activity栈
     *
     * @return
     */
    public Stack<Activity> getActivityStack() {
        return activityStack;
    }

    /**
     * 清理所有activity,但并不finish掉
     */
    public void clear() {
        if (activityStack != null&&!activityStack.isEmpty()) {
            activityStack.clear();
        }
    }

    /**
     * 退出应用程序
     *
     * @param context      上下文
     * @param isBackground 是否开开启后台运行(是否有后台程序)
     */
    public void appExit(Context context, Boolean isBackground) {
        try {
            finishAllActivity();
            ActivityManager activityMgr = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            activityMgr.killBackgroundProcesses(context.getPackageName());// 结束整个app
        } catch (Exception e) {

        } finally {
            // 注意，如果您有后台程序运行，请不要支持此句子
            if (!isBackground) {
                System.exit(0);
            }
        }
    }
}
