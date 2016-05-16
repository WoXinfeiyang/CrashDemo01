package com.fxj.crashdemo;

import android.app.Application;
import android.util.Log;

public class MyApplication extends Application {
	private static final String tag="MyApplication";
	private static MyApplication sInstance;
		
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(tag,"onCreate");
		sInstance=this;
		CrashHandler crashHandler=CrashHandler.getInstance();
		crashHandler.init(this);
	}

	public MyApplication getMyApplicationInstance(){
		return sInstance;
	}
	
	
}
