/*
 * 文 件 名:  ActivityBase.java
 * 版    权:  Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  jiangyufeng
 * 修改时间:  2015年12月7日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.framework.app;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.robin.lazy.framework.BuildConfig;
import com.robin.lazy.framework.LazyApplication;
import com.robin.lazy.framework.R;
import com.robin.lazy.framework.app.lifecycle.IComponentContainer;
import com.robin.lazy.framework.app.lifecycle.LifeCycleComponent;
import com.robin.lazy.framework.app.lifecycle.LifeCycleComponentManager;
import com.robin.lazy.logger.LazyLogger;
import com.robin.lazy.net.http.HttpRequestManager;
import com.robin.lazy.net.http.RequestLifecycleContext;

import java.util.List;


/**
 * activity基类
 * 
 * @author jiangyufeng
 * @version [版本号, 2015年12月7日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public abstract class LazyAppCompatActivity extends AppCompatActivity
		implements
		RequestLifecycleContext,
		IComponentContainer,
		FragmentResultCallback{
	/** 生命周期管理者 */
	private LifeCycleComponentManager mComponentContainer;
	/**
	 * 是否打开进入activity动画
	 */
	private boolean isOpenEnterTransition;

	/**
	 * 是否打开退出activity动画
	 */
	private boolean isOpenExitTransition;

	/**
	 * activity切换动画(0:当我们从A1启动A2时,A2出现在屏幕上的动画;1:当我们从A1启动A2时,A1从屏幕上消失动画;2:
	 * 当我们从A2退出回到A1时,A1出现在屏幕上的动画;3:当我们从A2退出回到A1时,A2从屏幕上消失的动画)
	 */
	private int activityAnimations[] = new int[]{R.anim.push_left_in,
			R.anim.push_left_out, R.anim.push_right_in, R.anim.push_right_out};

	/** 进程是否是活跃的(false说明进程进去后台) */
	private boolean isActive;

	/**
	 * 得到当前的Application
	 * 
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public LazyApplication getLazyApplication() {
		return (LazyApplication) getApplication();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(BuildConfig.DEBUG) {
			LazyLogger.d(getClass().getSimpleName() + " Activity.onCreate() invoked!!");
		}
		super.onCreate(savedInstanceState);
		isActive = true;
		getLazyApplication().getActivityStackManager().pushActivity(this);
		doCreate(savedInstanceState);
		initView();
	}

	@Override
	protected void onStart() {
		if(BuildConfig.DEBUG) {
			LazyLogger.d(getClass().getSimpleName() + " Activity.onStart() invoked!!");
		}
		super.onStart();
	}

	@Override
	protected void onRestart() {
		if(BuildConfig.DEBUG){
			LazyLogger.d(getClass().getSimpleName()+" Activity.onRestart() invoked!!");
		}
		super.onRestart();
		if (mComponentContainer != null) {
			mComponentContainer.onBecomesVisibleFromTotallyInvisible();
		}
	}

	@Override
	protected void onResume() {
		if(BuildConfig.DEBUG){
			LazyLogger.d(getClass().getSimpleName()+" Activity.onResume() invoked!!");
		}
		super.onResume();
		if (!isActive) {
			isActive = true;
		}
		if (mComponentContainer != null) {
			mComponentContainer.onBecomesVisibleFromPartiallyInvisible();
		}
	}

	@Override
	protected void onPause() {
		if(BuildConfig.DEBUG) {
			LazyLogger.d(getClass().getSimpleName() + " Activity.onPause() invoked!!");
		}
		super.onPause();
		isActive = false;
		if (mComponentContainer != null) {
			mComponentContainer.onBecomesPartiallyInvisible();
		}
	}

	@Override
	protected void onStop() {
		if(BuildConfig.DEBUG) {
			LazyLogger.d(getClass().getSimpleName() + " Activity.onStop() invoked!!");
		}
		super.onStop();
		if (!isAppOnForeground()) {
			// app 进入后台
			isActive = false;// 记录当前已经进入后台
		} else {
			isActive = true;
		}
		if (mComponentContainer != null) {
			mComponentContainer.onBecomesTotallyInvisible();
		}
	}

	@Override
	protected void onDestroy() {
		if(BuildConfig.DEBUG) {
			LazyLogger.d(getClass().getSimpleName() + " Activity.onDestroy() invoked!!");
		}
		super.onDestroy();
		getLazyApplication().getActivityStackManager().removeActivity(this);
		if (mComponentContainer != null) {
			mComponentContainer.onDestroy();
		}
	}

	@Override
	public LazyAppCompatActivity getCurrContext() {
		return this;
	}

	@Override
	public void addComponent(LifeCycleComponent component) {
		if (mComponentContainer == null) {
			mComponentContainer = new LifeCycleComponentManager();
		}
		mComponentContainer.addComponent(component);
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
		super.startActivityForResult(intent, requestCode, options);
		if (isOpenEnterTransition) {
			overridePendingTransition(activityAnimations[0],
					activityAnimations[1]);
		}
	}

	@Override
	public void finish() {
		super.finish();
		if (isOpenExitTransition) {
			overridePendingTransition(activityAnimations[2],
					activityAnimations[3]);
		}
		getLazyApplication().getActivityStackManager().removeActivity(this);
		getHttpRequestManager().cancelConetxtRequest(this);
	}

	@Override
	public void finishActivity(int requestCode) {
		super.finishActivity(requestCode);
		if (isOpenExitTransition) {
			overridePendingTransition(activityAnimations[2],
					activityAnimations[3]);
		}
		getLazyApplication().getActivityStackManager().removeActivity(this);
		getHttpRequestManager().cancelConetxtRequest(this);
	}

	/***
	 * 设置activity切换动画
	 * 
	 * @param activityOpenEnterAnimation
	 *            当我们从A1启动A2时,A2,出现在屏幕上的动画
	 * @param activityOpenExitAnimation
	 *            当我们从A1启动A2时,A1从屏幕上消失动画
	 * @param activityCloseEnterAnimation
	 *            当我们从A2退出回到A1时,A1出现在屏幕上的动画
	 * @param activityCloseExitAnimation
	 *            当我们从A2退出回到A1时,A2从屏幕上消失的动画
	 */
	public void setActivityAnimations(int activityOpenEnterAnimation,
			int activityOpenExitAnimation, int activityCloseEnterAnimation,
			int activityCloseExitAnimation) {
		this.activityAnimations[0] = activityOpenEnterAnimation;
		this.activityAnimations[1] = activityOpenExitAnimation;
		this.activityAnimations[2] = activityCloseEnterAnimation;
		this.activityAnimations[3] = activityCloseExitAnimation;
	}

	/***
	 * 设置是否打开进入activity的动画
	 * 
	 * @param isOpenEnterTransition
	 */
	public void setOpenEnterTransition(boolean isOpenEnterTransition) {
		this.isOpenEnterTransition = isOpenEnterTransition;
	}

	/***
	 * 设置是否开启退出activity的动画
	 * 
	 * @param isOpenExitTransition
	 */
	public void setOpenExitTransition(boolean isOpenExitTransition) {
		this.isOpenExitTransition = isOpenExitTransition;
	}

	/**
	 * 返回htt数据请求管理者
	 * 
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public HttpRequestManager getHttpRequestManager() {
		return getLazyApplication().getHttpDataManager();
	}

	@Override
	public void onFragmentResult(int requestCode, int resultCode, Intent data) {

	}

	/**
	 * 程序是否在前台运行
	 * 
	 * @return
	 */
	public boolean isAppOnForeground() {
		// Returns a list of application processes that are running on the
		// device
		ActivityManager activityManager = (ActivityManager) getApplicationContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		String packageName = getApplicationContext().getPackageName();
		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		if (appProcesses == null)
			return false;

		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(packageName)
					&& appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 退出应用程序
	 * 
	 * @param isBackground
	 *            是否开开启后台运行,如果为true则为后台运行
	 */
	public void exitApp(Boolean isBackground) {
		getLazyApplication().exitApp(isBackground);
	}

	/**
	 * 退出应用程序
	 * 
	 */
	public void exitApp() {
		exitApp(false);
	}

	/**
	 * 退出应用程序，且在后台运行
	 * 
	 */
	public void exitAppToBackground() {
		exitApp(true);
	}

	/***
	 * 判断当然进程是否活跃的，是否在前台的
	 * 
	 * @return
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * 创建activity
	 *
	 * @param savedInstanceState
	 * @see [类、类#方法、类#成员]
	 */
	public abstract void doCreate(Bundle savedInstanceState);

	/**
	 * 初始化
	 *
	 * @see [类、类#方法、类#成员]
	 */
	public abstract void initView();

}
