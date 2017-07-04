package com.robin.lazy.framework.app;

import android.content.Intent;

/**
 * fragemnt结果反馈接口
 * 
 * @author  江钰锋 0152
 * @version  [版本号, 2015年8月20日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public interface FragmentResultCallback {

	/**
	 * 结果反馈回调
	 * @param requestCode 请求编码
	 * @param resultCode 结果编码
	 * @param data 
	 * @see [类、类#方法、类#成员]
	 */
	void onFragmentResult(int requestCode, int resultCode, Intent data);
}
