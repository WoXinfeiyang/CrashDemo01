package com.fxj.crashdemo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.util.Log;



public class CrashHandler implements UncaughtExceptionHandler {
	
	private static final String tag="com.fxj.crashdemo.CrashHandler";
	/*crash记录信息文件夹路径*/
	private static final String path=Environment.getExternalStorageDirectory().getPath()+"/CrashDemo/log/";
	/**crash文件名*/
	private static final String FileName="crash";
	/**crash文件后缀*/
	private static final String FileNameSuffix=".txt";
	
	/**CrashHandler单例对象*/
	private static CrashHandler sInstance=new CrashHandler();
	
	private UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;
	/**上下文*/
	private Context mContext;
	
	
	public CrashHandler() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**获取CrashHandler单例，单例模式*/
	public static CrashHandler getInstance()
	{
		Log.i(tag,"CrashHandler#getInstance");
		
		if(sInstance==null){
			sInstance=new CrashHandler();
		}
		return sInstance;
	}

	public void init(Context context)
	{
		Log.i(tag,"CrashHandler#init");
		
		mDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		/*给线程设置异常处理器*/
		Thread.setDefaultUncaughtExceptionHandler(this);
		mContext = context.getApplicationContext();
	}
	
	/**
	 * 函数名称:uncaughtException(Thread thread, Throwable ex)
	 * 函数说明:UncaughtExceptionHandler接口中未实现的uncaughtException
	 * 		  的方法，当有未捕获异常发生时系统就会调用uncaughtException方法。
	 * 函数参数:thread---发生未捕获异常的线程，ex----未捕获异常
	 * */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.i(tag,"CrashHandler#uncaughtException");
		
        /*将异常信息导出到SD卡中*/ 
		dumpExceptionToSDCard(ex);
		/*将异常信息上传到服务器*/
		uploadExceptionToServer(ex);
		
		if(this.mDefaultUncaughtExceptionHandler!=null){
			this.mDefaultUncaughtExceptionHandler.uncaughtException(thread, ex);
			System.out.println(tag+",使用系统默认的异常处理器来处理异常!");
			Log.i(tag,"使用系统默认的异常处理器来处理异常!");
		}else{
			Process.killProcess(Process.myPid());
			System.out.println(tag+",当前进程自己结束当前进程!");
			Log.i(tag,"当前进程自己结束当前进程!");
		}
	}
	
	/**将异常信息导出到SD卡中*/
	private void dumpExceptionToSDCard(Throwable ex) {
		
		/*检查SD卡是否存在*/
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			Log.i(tag,"SD卡不存在");
			return;
		} 
		Log.i(tag,"SD卡存在");
		
		File fileDir=new File(path);
		if(!fileDir.exists()){
			/*当指定路径的文件夹不存在时创建指定的文件夹*/
			fileDir.mkdirs();
		}
		
		long current=System.currentTimeMillis();
		String time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));
		Log.i(tag,"异常发生时间:"+time);
		File file=new File(path+FileName+time+FileNameSuffix);
        
		try {			
			if(!file.exists()){
				file.createNewFile();
			}
			
			PrintWriter pw=new PrintWriter(new BufferedWriter(new FileWriter(file)));
			pw.write(time);
			dumpPhoneInfo(pw);
			pw.println();
			/*将异常的跟踪栈信息输出到指定的输出流*/
			ex.printStackTrace(pw);
//			System.out.println(ex);
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			Log.i(tag,"dump crash infomation failed!");
		}
	}
	/**导出手机信息
	 * @throws NameNotFoundException */
	private void dumpPhoneInfo(PrintWriter pw) {
		PackageManager pm=mContext.getPackageManager();
		try {
			PackageInfo pi=pm.getPackageInfo(mContext.getPackageName(),PackageManager.GET_ACTIVITIES);
			pw.println();
			pw.println("APP Version:"+pi.versionName+"_"+pi.versionCode);
			pw.println("Android系统版本号OS Version:"+Build.VERSION.RELEASE+"_"+Build.VERSION.SDK_INT);
			pw.println("手机制造商Vendor:"+Build.MANUFACTURER);
			pw.println("手机型号Model:"+Build.MODEL);
			pw.println("CPU架构CPU ABI:"+Build.CPU_ABI);			
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			Log.i(tag,"NameNotFoundException");
			e.printStackTrace();
		}
		
	}

	/**将异常信息上传到服务器*/
	private void uploadExceptionToServer(Throwable ex) {
		// TODO Auto-generated method stub
		
	}

}
