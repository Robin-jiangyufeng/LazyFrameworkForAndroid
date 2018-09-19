/**
 * 文 件 名:  LazyApplication.java
 * 版    权:  Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  江钰锋 17983
 * 修改时间:  16/6/30
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.framework;

import android.app.Application;
import android.content.Intent;
import android.widget.Toast;

import com.robin.lazy.cache.CacheLoaderManager;
import com.robin.lazy.cache.disk.naming.HashCodeFileNameGenerator;
import com.robin.lazy.framework.app.ActivityStackManager;
import com.robin.lazy.logger.AndroidLogTool;
import com.robin.lazy.logger.LazyLogger;
import com.robin.lazy.logger.LogLevel;
import com.robin.lazy.logger.PrinterType;
import com.robin.lazy.net.http.HttpRequestManager;
import com.robin.lazy.net.http.download.DownloadManager;
import com.robin.lazy.util.exception.CrashHandleListener;
import com.robin.lazy.util.exception.CrashHandler;
//                      _oo0oo_
//                     o8888888o
//                     88" . "88
//                     (| -_- |)
//                     0\  =  /0
//                   ___/'---'\___
//                .' \\\|     |// '.
//               / \\\|||  :  |||// \\
//              / _ ||||| -:- |||||_ \\
//              | |  \\\\  -  ////  | |
//              | \_|  ''\---/''  |_/ |
//              \  .-\__  '-'  __/-.  /
//            ___'. .'  /--.--\  '. .'___
//         ."" '<  '.___\_<|>_/___.' >'  "".
//        | | : '-  \'.;'\ _ /';.'/ - ' : | |
//        \  \ '_.   \_ __\ /__ _/   .-' /  /
//    ====='-.____'.___ \_____/___.-'____.-'=====
//                      '=---='
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//     佛祖保佑           永无BUG         镇类之宝
//
//
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

/**
 * 自定义Application
 *
 * @author 江钰锋 00501
 * @version [版本号, 16/6/30]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class LazyApplication extends Application implements CrashHandleListener {
    /**
     * 下载器的下载目录
     */
    private static final String DOWN_PATH = "mydown/";

    /**
     * 网络数据请求管理
     */
    private HttpRequestManager httpDataManager;

    /**
     * 下载管理
     */
    private DownloadManager downloadManager;

    /**
     * activity部分栈管理
     */
    private ActivityStackManager ssManager;

    @Override
    public void onCreate() {
        // 程序创建的时候执行
        LazyLogger.d(" Application.onCreate() invoked!!");
        super.onCreate();
        initApp();
    }

    /**
     * app初始化工作
     */
    protected void initApp(){
        initLogger();
        registerUncaughtExceptionHandler();
        initCache();
    }

    @Override
    public void onTerminate() {
        // 程序终止的时候执行
        LazyLogger.d(" Application.onTerminate() invoked!!");
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        // 低内存的时候执行
        LazyLogger.d(" Application.onLowMemory() invoked!!");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        // 程序在内存清理的时候执行
        LazyLogger.d(" Application.onTrimMemory() invoked!!");
        super.onTrimMemory(level);
    }

    /***
     * 注册crash监听器 void
     *
     * @throws
     * @see [类、类#方法、类#成员]
     */
    protected void registerUncaughtExceptionHandler() {
        CrashHandler crashHandler = new CrashHandler();
        crashHandler.init(this);
    }

    /***
     * 初始化日志系统
     *
     * @throws
     * @see [类、类#方法、类#成员]
     */
    protected void initLogger() {
        LazyLogger.init(/* PrinterType.FORMATTED */PrinterType.ORDINARY) // 打印类型
                .methodCount(3) // default 2
                .hideThreadInfo() // default shown
                .logLevel(LogLevel.ALL) // default LogLevel.ALL(设置全局日志等级)
                .methodOffset(2) // default 0
                .logTool(new AndroidLogTool()/*new Log4JTool(Level.ERROR)*/); // Log4j中的Level与本框架的LogLevel是分开设置的(Level只用来设置log4j的日志等级)
    }

    /**
     * 初始化缓存模块
     * void
     *
     * @throws
     * @see [类、类#方法、类#成员]
     */
    protected void initCache() {
        CacheLoaderManager.getInstance().init(this,
                new HashCodeFileNameGenerator(), 1024 * 1024 * 8, 50, 20);
    }

    /**
     * 获取数据请求管理者
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public synchronized HttpRequestManager getHttpDataManager() {
        if (httpDataManager == null) {
            httpDataManager = new HttpRequestManager(this);
        }
        return httpDataManager;
    }

    /**
     * 得到当前下载管理者
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public synchronized DownloadManager getDownloadManager() {
        if (downloadManager == null) {
            downloadManager = DownloadManager.getInstance();
            downloadManager.setDownPath(DOWN_PATH);
        }
        return downloadManager;
    }

    /**
     * 得到activity堆管理者
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public synchronized ActivityStackManager getActivityStackManager() {
        if (ssManager == null) {
            ssManager = new ActivityStackManager();
        }
        return ssManager;
    }

    /**
     * 隐藏app
     *
     * @see [类、类#方法、类#成员]
     */
    public void hideApp() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * 退出应用程序
     *
     * @param isBackground 是否开开启后台运行,如果为true则为后台运行
     */
    public void exitApp(Boolean isBackground) {
        if (httpDataManager != null) {
            httpDataManager.close(true);
            httpDataManager = null;
        }
        if (downloadManager != null) {
            downloadManager.close(true);
            downloadManager = null;
        }
        if (ssManager != null) {
            ssManager.appExit(this, isBackground);
            ssManager = null;
        }
    }

    @Override
    public void crashHandle() {
        Toast.makeText(this, "很抱歉,程序出现异常,即将退出...", Toast.LENGTH_SHORT).show();
    }
}
