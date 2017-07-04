package com.robin.lazy.framework.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.robin.lazy.framework.BuildConfig;
import com.robin.lazy.framework.app.lifecycle.IComponentContainer;
import com.robin.lazy.framework.app.lifecycle.LifeCycleComponent;
import com.robin.lazy.framework.app.lifecycle.LifeCycleComponentManager;
import com.robin.lazy.logger.LazyLogger;
import com.robin.lazy.net.http.HttpRequestManager;
import com.robin.lazy.net.http.RequestLifecycleContext;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Fragment基类
 *
 * @author 江钰锋
 * @version [版本号, 2014年6月19日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public abstract class LazyFragment extends Fragment
        implements
        RequestLifecycleContext,
        IComponentContainer,
        FragmentResultCallback {

    /**
     * 生命周期管理者
     */
    private LifeCycleComponentManager mComponentContainer;

    /**
     * 标志位，标志已经初始化完成
     */
    private boolean isPrepared;

    /**
     * 是否第一次执行onResume()方法
     */
    private boolean isFirstResume = true;

    private Bundle bundle;

    @Override
    public void onAttach(Context context) {
        if(BuildConfig.DEBUG) {
            LazyLogger.d(getClass().getSimpleName() + " Fragment.onAttach() invoked!!");
        }
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(BuildConfig.DEBUG) {
            LazyLogger.d(getClass().getSimpleName() + " Fragment.onCreate() invoked!!");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(BuildConfig.DEBUG) {
            LazyLogger.d(getClass().getSimpleName() + " Fragment.onCreateView() invoked!!");
        }
        return inflater.inflate(getLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(BuildConfig.DEBUG) {
            LazyLogger.d(getClass().getSimpleName() + " Fragment.onViewCreated() invoked!!");
        }
        super.onViewCreated(view, savedInstanceState);
        initView(savedInstanceState);
        isPrepared = true;
        if (mComponentContainer != null) {
            mComponentContainer.onBecomesVisibleFromTotallyInvisible();
        }
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Animation anim = null;
        try {
            if (nextAnim > 0) {
                anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        } finally {
            if (anim != null) {
                anim.setAnimationListener(new AnimationListener() {

                    public void onAnimationStart(Animation animation) {
                        // 动画开始
                    }

                    public void onAnimationRepeat(Animation animation) {
                        // 动画循环
                    }

                    public void onAnimationEnd(Animation animation) {
                        // 动画结束
                        LazyFragment.this.onAnimationEnd();
                    }
                });
            } else {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (isPrepared) {
                            LazyFragment.this.onAnimationEnd();
                        }
                    }
                }, 500);
            }
        }
        return anim;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // 此方法只是在FragmentPagerAdapter或者FragmentStatePagerAdapter中显示或者不显示才会调用的方法
        if (isPrepared) {
            if (getUserVisibleHint()) {
                onVisible();
            } else {
                onInvisible();
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // 此方法在是在调用FragmentTransaction.hide或者FragmentTransaction.show的时候才会调用的方法
        if (isPrepared) {
            if (hidden) {
                onInvisible();
            } else {
                onVisible();
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if(BuildConfig.DEBUG) {
            LazyLogger.d(getClass().getSimpleName() + " Fragment.onActivityCreated() invoked!!");
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public LazyAppCompatActivity getCurrContext() {
        return (LazyAppCompatActivity) getActivity();
    }

    @Override
    public void addComponent(LifeCycleComponent component) {
        if (mComponentContainer == null) {
            mComponentContainer = new LifeCycleComponentManager();
        }
        mComponentContainer.addComponent(component);
    }

    @Override
    public void onResume() {
        if(BuildConfig.DEBUG) {
            LazyLogger.d(getClass().getSimpleName() + " Fragment.onResume() invoked!!");
        }
        super.onResume();
        if (!isFirstResume) {
            if (mComponentContainer != null) {
                mComponentContainer.onBecomesVisibleFromPartiallyInvisible();
            }
        }
        if (isFirstResume) {
            isFirstResume = false;
        }
        if (isPrepared) {
            if (isVisible()) {
                onVisible();
            } else {
                onInvisible();
            }
        }
    }

    @Override
    public void onPause() {
        if(BuildConfig.DEBUG) {
            LazyLogger.d(getClass().getSimpleName() + " Fragment.onPause() invoked!!");
        }
        super.onPause();
        if (mComponentContainer != null) {
            mComponentContainer.onBecomesPartiallyInvisible();
        }
    }

    @Override
    public void onStop() {
        if(BuildConfig.DEBUG) {
            LazyLogger.d(getClass().getSimpleName() + " Fragment.onStop() invoked!!");
        }
        super.onStop();
        if (mComponentContainer != null) {
            mComponentContainer.onBecomesTotallyInvisible();
        }
    }

    @Override
    public void onDestroyView() {
        if(BuildConfig.DEBUG) {
            LazyLogger.d(getClass().getSimpleName() + " Fragment.onDestroyView() invoked!!");
        }
        super.onDestroyView();
        isPrepared = false;
        getHttpRequestManager().cancelConetxtRequest(this);
        if (mComponentContainer != null) {
            mComponentContainer.onDestroy();
        }
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Intent data) {

    }

    /**
     * 返回htt数据请求管理者
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public HttpRequestManager getHttpRequestManager() {
        if (getActivity() != null) {
            return ((LazyAppCompatActivity) getActivity()).getHttpRequestManager();
        }
        return null;
    }

    /**
     * 根据id查找view
     *
     * @param viewId
     * @return
     * @see [类、类#方法、类#成员]
     */
    public View findViewById(int viewId) {
        if (getView() != null) {
            return getView().findViewById(viewId);
        }
        return null;
    }

    /***
     * 开启退出activity的动画
     */
    protected void openExitTransition() {
        Activity activity = getActivity();
        if (activity instanceof LazyAppCompatActivity) {
            ((LazyAppCompatActivity) activity).setOpenExitTransition(true);
        }
    }

    /**
     * finish掉当前activity
     * @param resultCode 结果码
     * @param data 数据
     */
    public void finishActivity(int resultCode,Intent data) {
        if (getActivity() != null) {
            getActivity().setResult(resultCode,data);
        }
        finishActivity();
    }

    /**
     * finish掉当前activity
     * @param resultCode
     */
    public void finishActivity(int resultCode) {
        if (getActivity() != null) {
            getActivity().setResult(resultCode);
        }
        finishActivity();
    }

    /**
     * finish掉当前activity
     */
    public void finishActivity() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().finish();
        }
    }

    /**
     * finish掉当前activity(带动画效果的)
     * @param resultCode 结果码
     * @param data
     */
    public void finishActivityAfterTransition(int resultCode,Intent data) {
        openExitTransition();
        finishActivity(resultCode,data);
    }

    /***
     * finish掉当前activity(带动画效果的)
     * @param resultCode 结果码
     */
    public void finishActivityAfterTransition(int resultCode) {
        openExitTransition();
        finishActivity(resultCode);
    }

    /**
     * finish掉当前activity(带动画效果的)
     */
    public void finishActivityAfterTransition() {
        openExitTransition();
        finishActivity();
    }

    /***
     * 设置layout
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public abstract int getLayoutId();

    /**
     * 在viewpager中第一次初始化view后时才会调用
     *
     * @param savedInstanceState activity被意外回收所保存的数据
     * @see [类、类#方法、类#成员]
     */
    public abstract void initView(Bundle savedInstanceState);

    /***
     * 动画结束执行方法
     */
    public void onAnimationEnd() {

    }

    /**
     * view初始化后时可见的情况下调用(不管是否重新加载view都会调用)
     *
     * @see [类、类#方法、类#成员]
     */
    public void onVisible() {
        if(BuildConfig.DEBUG) {
            LazyLogger.d(getClass().getSimpleName() + " Fragment.onVisible() invoked!!");
        }
        if (mComponentContainer != null) {
            mComponentContainer.onBecomesVisibleFromPartiallyInvisible();
        }
    }

    ;

    /**
     * view初始化后时不可见的情况下调用(不管是否重新加载view都会调用)
     *
     * @see [类、类#方法、类#成员]
     */
    public void onInvisible() {
        if(BuildConfig.DEBUG) {
            LazyLogger.d(getClass().getSimpleName() + " Fragment.onInvisible() invoked!!");
        }
        if (mComponentContainer != null) {
            mComponentContainer.onBecomesPartiallyInvisible();
        }
    }

    /**
     * 传值
     *
     * @param key
     * @param value 值
     * @see [类、类#方法、类#成员]
     */
    public void addValues(String key, Object value) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        if (value instanceof Boolean) {
            bundle.putBoolean(key, (Boolean) value);
        } else if (value instanceof Byte) {
            bundle.putByte(key, (Byte) value);
        } else if (value instanceof Character) {
            bundle.putChar(key, (Character) value);
        } else if (value instanceof Short) {
            bundle.putShort(key, (Short) value);
        } else if (value instanceof Integer) {
            bundle.putInt(key, (Integer) value);
        } else if (value instanceof Integer) {
            bundle.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            bundle.putLong(key, (Long) value);
        } else if (value instanceof Float) {
            bundle.putFloat(key, (Float) value);
        } else if (value instanceof Double) {
            bundle.putDouble(key, (Double) value);
        } else if (value instanceof String) {
            bundle.putString(key, (String) value);
        } else if (value instanceof CharSequence) {
            bundle.putCharSequence(key, (CharSequence) value);
        } else if (value instanceof Serializable) {
            bundle.putSerializable(key, (Serializable) value);
        } else if (value instanceof Parcelable) {
            bundle.putParcelable(key, (Parcelable) value);
        } else if (value instanceof ArrayList) {
            ArrayList list = (ArrayList) value;
            if (list.get(0) instanceof Parcelable) {
                bundle.putParcelableArrayList(key, list);
            } else if (list.get(0) instanceof CharSequence) {
                bundle.putCharSequenceArrayList(key, list);
            } else if (list.get(0) instanceof Integer) {
                bundle.putIntegerArrayList(key, list);
            } else if (list.get(0) instanceof String) {
                bundle.putStringArrayList(key, list);
            } else {
                new Exception("传的参数错误");
            }
        } else if (value instanceof SparseArray) {
            if (((SparseArray) value).get(0) instanceof Parcelable) {
                bundle.putSparseParcelableArray(key, (SparseArray) value);
            } else {
                new Exception("传的参数错误");
            }
        } else if (value instanceof Parcelable[]) {
            bundle.putParcelableArray(key, (Parcelable[]) value);
        } else if (value instanceof boolean[]) {
            bundle.putBooleanArray(key, (boolean[]) value);
        } else if (value instanceof byte[]) {
            bundle.putByteArray(key, (byte[]) value);
        } else if (value instanceof char[]) {
            bundle.putCharArray(key, (char[]) value);
        } else if (value instanceof CharSequence[]) {
            bundle.putCharSequenceArray(key, (CharSequence[]) value);
        } else if (value instanceof double[]) {
            bundle.putDoubleArray(key, (double[]) value);
        } else if (value instanceof float[]) {
            bundle.putFloatArray(key, (float[]) value);
        } else if (value instanceof int[]) {
            bundle.putIntArray(key, (int[]) value);
        } else if (value instanceof long[]) {
            bundle.putLongArray(key, (long[]) value);
        } else if (value instanceof short[]) {
            bundle.putShortArray(key, (short[]) value);
        } else if (value instanceof String[]) {
            bundle.putStringArray(key, (String[]) value);
        } else {
            new Exception("传的参数错误");
        }
    }

    /**
     * 提交要接收的值
     *
     * @see [类、类#方法、类#成员]
     */
    public void commitAddValues() {
        if (bundle != null) {
            setArguments(bundle);
        }
    }

}
