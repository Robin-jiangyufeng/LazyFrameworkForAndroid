package com.robin.lazy.framework.app.lifecycle;

public interface LifeCycleComponent {

    /**
     * 变成不可见状态,但没有被销毁而是处于暂停状态
     * like {@link android.app.Activity#onPause}
     */
    public void onBecomesPartiallyInvisible();

    /**
     * 从暂停状态变成可见状态的
     * like {@link android.app.Activity#onResume}
     */
    public void onBecomesVisible();

    /**
     * 变成完全不可见状态,就是即将被销毁的状态
     * like {@link android.app.Activity#onStop}
     */
    public void onBecomesTotallyInvisible();

    /**
     * 从完全不可见状态变成可见状态
     * like {@link android.app.Activity#onRestart}
     */
    public void onBecomesVisibleFromTotallyInvisible();

    /**
     * 销毁
     * like {@link android.app.Activity#onDestroy}
     */
    public void onDestroy();
}
