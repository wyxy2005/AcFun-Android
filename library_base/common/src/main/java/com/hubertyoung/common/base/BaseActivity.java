package com.hubertyoung.common.base;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.facebook.stetho.common.LogUtil;
import com.hubertyoung.base.bean.EnvironmentBean;
import com.hubertyoung.base.bean.ModuleBean;
import com.hubertyoung.base.listener.OnEnvironmentChangeListener;
import com.hubertyoung.baseplatform.AuthorizeSDK;
import com.hubertyoung.baseplatform.ShareSDK;
import com.hubertyoung.common.BuildConfig;
import com.hubertyoung.common.baserx.RxManager;
import com.hubertyoung.common.utils.activitymanager.AppActivityManager;
import com.hubertyoung.common.utils.bar.BarUtils;
import com.hubertyoung.common.utils.log.CommonLog;
import com.hubertyoung.common.utils.os.AppUtils;
import com.hubertyoung.environmentswitcher.EnvironmentSwitcher;



public abstract class BaseActivity extends AppCompatActivity implements OnEnvironmentChangeListener {
	public String TAG = this.getClass().getSimpleName();
	private static final String SAVED_STATE_STATUS_BAR_TRANSLUCENT = "saved_state_status_bar_translucent";
	public Context mContext;
	public RxManager mRxManager;
	private boolean isConfigChange = false;
	private boolean statusBarTranslucent;

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		isConfigChange = false;
		mRxManager = new RxManager();
		if ( isRegisterEvent() ) {
			mRxManager.mRxBus.register( this );
		}
		// 把actvity放到application栈中管理
		AppActivityManager.getAppManager().addActivity( this );
		// 无标题
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		// 设置竖屏
		setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

		if ( savedInstanceState != null ) {
			statusBarTranslucent = savedInstanceState.getBoolean( SAVED_STATE_STATUS_BAR_TRANSLUCENT );
			BarUtils.setStatusBarTranslucent( getWindow(), statusBarTranslucent );
		}

		doBeforeSetContentView();
		if ( getLayoutId() != 0 ) {
			setContentView( getLayoutId() );
			mContext = this;
			initView( savedInstanceState );
			//初始化ToolBar
			initToolBar();

			EnvironmentSwitcher.addOnEnvironmentChangeListener( this );
		} else {
			LogUtil.e( "--->bindLayout() return 0" );
		}
	}

	/**
	 * 设置layout前配置
	 */
	public void doBeforeSetContentView() {

	}

	/*********************子类实现*****************************/
	/**
	 *获取布局文件
	 */
	public abstract int getLayoutId();

	/**
	 *	初始化view
	 */
	public abstract void initView( Bundle savedInstanceState );

	protected abstract void loadData();

	/**
	 *	初始化toolBar
	 */
	public abstract void initToolBar();

	/**
	 * 是否开启事件订阅
	 *
	 * @return
	 */
	protected boolean isRegisterEvent() {
		return false;
	}

//	protected  void onStateRefresh(){
//
//	}

	/**
	 * 通过Class跳转界面
	 **/
	public void startActivity( Class< ? > cls ) {
		startActivity( cls, null );
	}

	/**
	 * 通过Class跳转界面
	 **/
	public void startActivityForResult( Class< ? > cls, int requestCode ) {
		startActivityForResult( cls, null, requestCode );
	}

	/**
	 * 含有Bundle通过Class跳转界面
	 **/
	public void startActivityForResult( Class< ? > cls, Bundle bundle, int requestCode ) {
		Intent intent = new Intent();
		intent.setClass( this, cls );
		if ( bundle != null ) {
			intent.putExtras( bundle );
		}
		startActivityForResult( intent, requestCode );
	}

	/**
	 * 含有Bundle通过Class跳转界面
	 **/
	public void startActivity( Class< ? > cls, Bundle bundle ) {
		Intent intent = new Intent();
		intent.setClass( this, cls );
		if ( bundle != null ) {
			intent.putExtras( bundle );
		}
		startActivity( intent );
	}

	@Override
	protected void onResume() {
		super.onResume();
		//debug版本不统计crash
		if ( !BuildConfig.DEBUG ) {
			//友盟统计
//			MobclickAgent.onResume(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		//debug版本不统计crash
		if ( !BuildConfig.DEBUG ) {
			//友盟统计
//			MobclickAgent.onPause(this);
		}
	}

	@Override
	public void onConfigurationChanged( Configuration newConfig ) {
		super.onConfigurationChanged( newConfig );
		isConfigChange = true;
	}

	@Override
	protected void onDestroy() {
		EnvironmentSwitcher.removeOnEnvironmentChangeListener( this );
		super.onDestroy();
		CommonLog.loge( "activity: " + getClass().getSimpleName() + " onDestroy()" );
		if ( mRxManager != null ) {
			if ( isRegisterEvent() ) mRxManager.mRxBus.unregister( this );
			mRxManager.clear();
		}
		if ( !isConfigChange ) {
			AppActivityManager.getAppManager().finishActivity( this );
		}

	}

	@Override
	protected void onSaveInstanceState( Bundle outState ) {
		super.onSaveInstanceState( outState );
		outState.putBoolean( SAVED_STATE_STATUS_BAR_TRANSLUCENT, statusBarTranslucent );
	}

	@Override
	public void onEnvironmentChanged( ModuleBean module, EnvironmentBean oldEnvironment, EnvironmentBean newEnvironment ) {
		if ( AppUtils.isDebuggable() ) {
			Log.e( TAG, module.getName() + "由" + oldEnvironment.getName() + "环境，Url=" + oldEnvironment.getUrl() + ",切换为" + newEnvironment.getName() + "环境，Url=" + newEnvironment.getUrl() );
			Toast.makeText( this, module.getName() + "由" + oldEnvironment.getName() + "环境，Url=" + oldEnvironment.getUrl() + "切换为" + newEnvironment.getName() + "环境，Url=" + newEnvironment.getUrl(),
					Toast.LENGTH_SHORT )
					.show();
		}
	}

	@Override
	protected void onActivityResult( int requestCode, int resultCode, @Nullable Intent data ) {
		AuthorizeSDK.onHandleResult( this, requestCode, resultCode, data );
		ShareSDK.onHandleResult( this, requestCode, resultCode, data );
		super.onActivityResult( requestCode, resultCode, data );
	}
}
