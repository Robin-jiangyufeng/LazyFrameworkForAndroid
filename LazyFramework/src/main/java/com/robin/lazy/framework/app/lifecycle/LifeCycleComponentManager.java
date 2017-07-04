package com.robin.lazy.framework.app.lifecycle;

import android.support.v4.util.LruCache;

import java.util.Iterator;
import java.util.Map.Entry;

public class LifeCycleComponentManager implements IComponentContainer {

	/**最大的数量*/
	private static final int MAX_COUNT = 10;
	/**内存缓存*/
    private LruCache<String, LifeCycleComponent> mComponentList;

    public LifeCycleComponentManager() {
    }

    /**
     * Try to add component to container
     *
     * @param component
     * @param matrixContainer
     */
    public static void tryAddComponentToContainer(LifeCycleComponent component, Object matrixContainer) {
        tryAddComponentToContainer(component, matrixContainer, true);
    }

    public static boolean tryAddComponentToContainer(LifeCycleComponent component, Object matrixContainer, boolean throwEx) {
        if (matrixContainer instanceof IComponentContainer) {
            ((IComponentContainer) matrixContainer).addComponent(component);
            return true;
        } else {
            if (throwEx) {
                throw new IllegalArgumentException("componentContainerContext should implements IComponentContainer");
            }
            return false;
        }
    }

    public void addComponent(LifeCycleComponent component) {
        if (component != null) {
            if (mComponentList == null) {
                mComponentList = new LruCache<String, LifeCycleComponent>(MAX_COUNT);
            }
            mComponentList.put(component.toString(), component);
        }
    }

    /**
     * 从不存在到创建后可见状态
     * void
     * @throws
     * @see [类、类#方法、类#成员]
     */
    public void onBecomesVisibleFromTotallyInvisible() {

        if (mComponentList == null) {
            return;
        }

        Iterator<Entry<String, LifeCycleComponent>> it = mComponentList.snapshot().entrySet().iterator();
        while (it.hasNext()) {
            LifeCycleComponent component = it.next().getValue();
            if (component != null) {
                component.onBecomesVisibleFromTotallyInvisible();
            }
        }
    }

    /**
     * 变成完全不可见状态,就是即将被销毁的状态
     * void
     * @throws
     * @see [类、类#方法、类#成员]
     */
    public void onBecomesTotallyInvisible() {
        if (mComponentList == null) {
            return;
        }
        Iterator<Entry<String, LifeCycleComponent>> it = mComponentList.snapshot().entrySet().iterator();
        while (it.hasNext()) {
            LifeCycleComponent component = it.next().getValue();
            if (component != null) {
                component.onBecomesTotallyInvisible();
            }
        }
    }

    /**
     * 变成不可见状态,但没有被销毁而是处于暂停状态
     * void
     * @throws
     * @see [类、类#方法、类#成员]
     */
    public void onBecomesPartiallyInvisible() {
        if (mComponentList == null) {
            return;
        }
        Iterator<Entry<String, LifeCycleComponent>> it = mComponentList.snapshot().entrySet().iterator();
        while (it.hasNext()) {
            LifeCycleComponent component = it.next().getValue();
            if (component != null) {
                component.onBecomesPartiallyInvisible();
            }
        }
    }

    /**
     * 从暂停状态变成可见状态的
     * void
     * @throws
     * @see [类、类#方法、类#成员]
     */
    public void onBecomesVisibleFromPartiallyInvisible() {
        if (mComponentList == null) {
            return;
        }
        Iterator<Entry<String, LifeCycleComponent>> it = mComponentList.snapshot().entrySet().iterator();
        while (it.hasNext()) {
            LifeCycleComponent component = it.next().getValue();
            if (component != null) {
                component.onBecomesVisible();
            }
        }
    }

    /**
     * 活动页被销毁
     * void
     * @throws
     * @see [类、类#方法、类#成员]
     */
    public void onDestroy() {
        if (mComponentList == null) {
            return;
        }
        Iterator<Entry<String, LifeCycleComponent>> it = mComponentList.snapshot().entrySet().iterator();
        while (it.hasNext()) {
            LifeCycleComponent component = it.next().getValue();
            if (component != null) {
                component.onDestroy();
            }
        }
        mComponentList.evictAll();
    }
    
    /**
     * 重置大小
     * @param maxSize
     * void
     * @throws
     * @see [类、类#方法、类#成员]
     */
    public void resize(int maxSize){
    	if (mComponentList == null) {
            return;
        }
    	mComponentList.resize(maxSize);
    }
}
