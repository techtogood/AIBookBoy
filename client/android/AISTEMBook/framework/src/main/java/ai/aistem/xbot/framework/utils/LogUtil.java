package ai.aistem.xbot.framework.utils;

import android.util.Log;

public class LogUtil {
	public static String TAG = "aistem_API" + "(V" + "1.0.0.0" + ")";
	public static int logLevel = Log.VERBOSE;

	private static LogUtil instance = null;
	
	/**
	 * 默认情况下为debug模式
	 */
	private static boolean isDebugLog=true;

	/**
	 * 设置log日志的开启和关闭,true为开启,false为关闭
	 * @param isDebugLog
	 */
	public static void setDebugLog(boolean isDebugLog) {
		LogUtil.isDebugLog = isDebugLog;
	}

	// 禁止外部实例
	private LogUtil() {
	}

	

	/**
	 * 返回LogUtil类唯一实例
	 * @return LogUtil实例
	 */
	public static synchronized LogUtil getInstance() {
		if (instance == null) {
			instance = new LogUtil();
		}
		return instance;
	}

	/**
	 * 获取错误详细信息
	 * @return 功能名称
	 */
	private String getFunctionName() {
		if(!isDebugLog){
			return "";
		}
		StackTraceElement[] sts = Thread.currentThread().getStackTrace();

		if (sts == null) {
			return "";
		}

		for (StackTraceElement st : sts) {
			if (st.isNativeMethod()) {
				continue;
			}
			if (st.getClassName().equals(Thread.class.getName())) {
				continue;
			}
			if (st.getClassName().equals(this.getClass().getName())) {
				continue;
			}
			return "[ " + Thread.currentThread().getName() + ": " + st.getFileName() + ":"
					+ st.getLineNumber() + " ]";
		}
		return "";
	}
	
	private void writeLogFile(String TAG, String content){
		LogFileUtil.writeFileSdcard(TAG+content);
	}
	

	public void i(String TAG, String str) {
		if(!isDebugLog){
			return;
		}
		if (logLevel <= Log.INFO) {
			String name = getFunctionName();
			writeLogFile(TAG, name + " - " + str);
			Log.i(TAG, name + " - " + str);
		}
	}

	public void i(String str) {
		if(!isDebugLog){
			return;
		}
		if (logLevel <= Log.INFO) {
			String name = getFunctionName();
			writeLogFile(TAG, name + " - " + str);
			Log.i(TAG, name + " - " + str);
		}
	}

	public void v(String TAG, String str) {
		if(!isDebugLog){
			return;
		}
		if (logLevel <= Log.VERBOSE) {
			String name = getFunctionName();
			writeLogFile(TAG, name + " - " + str);
			Log.v(TAG, name + " - " + str);
		}
	}

	public void v(String str) {
		if(!isDebugLog){
			return;
		}
		if (logLevel <= Log.VERBOSE) {
			String name = getFunctionName();
			writeLogFile(TAG, name + " - " + str);
			Log.v(TAG, name + " - " + str);
		}
	}

	public void w(String TAG, String str) {
		if(!isDebugLog){
			return;
		}
		if (logLevel <= Log.WARN) {
			String name = getFunctionName();
			writeLogFile(TAG, name + " - " + str);
			Log.w(TAG, name + " - " + str);
		}
	}

	public void w(String str) {
		if(!isDebugLog){
			return;
		}
		if (logLevel <= Log.WARN) {
			String name = getFunctionName();
			writeLogFile(TAG, name + " - " + str);
			Log.w(TAG, name + " - " + str);
		}
	}

	public void e(String TAG, String str) {
		if(!isDebugLog){
			return;
		}
		if (logLevel <= Log.ERROR) {
			String name = getFunctionName();
			writeLogFile(TAG, name + " - " + str);
			Log.e(TAG, name + " - " + str);
		}
	}

	public void e(String str) {
		if(!isDebugLog){
			return;
		}
		if (logLevel <= Log.ERROR) {
			String name = getFunctionName();
			writeLogFile(TAG, name + " - " + str);
			Log.e(TAG, name + " - " + str);
		}
	}

	public void e(String TAG, Exception ex) {
		if(!isDebugLog){
			return;
		}
		if (logLevel <= Log.ERROR) {
			writeLogFile(TAG, ex.toString());
			Log.e(TAG, "error", ex);
		}
	}

	public void e(Exception ex) {
		if(!isDebugLog){
			return;
		}
		if (logLevel <= Log.ERROR) {
			writeLogFile(TAG, ex.toString());
			Log.e(TAG, "error", ex);
		}
	}

	public void d(String TAG, String str) {
		if(!isDebugLog){
			return;
		}
		if (logLevel <= Log.DEBUG) {
			String name = getFunctionName();
			writeLogFile(TAG, name + " - " + str);
			Log.d(TAG, name + " - " + str);
		}
	}

	public void d(String str) {
		if(!isDebugLog){
			return;
		}
		if (logLevel <= Log.DEBUG) {
			String name = getFunctionName();
			writeLogFile(TAG, name + " - " + str);
			Log.d(TAG, name + " - " + str);
		}
	}

}
