package ai.aistem.xbot.framework.network.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.DisplayMetrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author MC 小方法合集
 * @see屏幕分辨率,IP地址检查,网络状况检查
 */
public class Tools {
	private final String TAG;
	private Context context;

	public Tools(Context context) {
		this.context = context;
		this.TAG = getClass().getSimpleName();
	}

	// TODO 初始化数据
	public void init() {
		// 获取分辨率
		getScreenSize();
		// 检查网络状态
		checkNet();
	};

	// TODO 获取屏幕的分辨率 [0]是短的 [1]是长的
	public int[] getScreenSize() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((Activity) (context)).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		// 屏幕分辨率数组
		int[] result = new int[4];
		result[0] = displayMetrics.widthPixels;
		result[1] = displayMetrics.heightPixels;
		result[2] = pxToDip(context, displayMetrics.widthPixels);
		result[3] = pxToDip(context, displayMetrics.heightPixels);
		return result;
	}

	// TODO 检查输入的IP地址是否合法 合法返回String,否则null
	public String checkAddress(String address) {
		try {
			// 字符转化,全角转化为半角,去空格
			address = address.replaceAll(" ", "");
			address = address.replaceAll("　", "");
			address = address.replaceAll("。", ".");
			address = address.replaceAll("：", ":");
			// 判断长度是否为9-21
			if (address.length() < 9 || address.length() > 21) {
				return null;
			}
			// 判断是否包含3个"."和一个":"
			int counterPoint = 0;
			for (int i = 0; i < address.length(); i++) {
				if (address.substring(i, i + 1).equals(".")) {
					counterPoint++;
				}
			}
			if (counterPoint != 3 || !address.contains(":")) {
				return null;
			}
			// 按":"分割进行判断
			String[] addressAll = address.split(":");
			String[] addressIP = addressAll[0].split("\\.");
			if (addressIP.length < 4 || addressAll[1].length() < 1 || addressAll[1].length() > 5) {
				return null;
			}
			for (int i = 0; i < addressIP.length; i++) {
				if (addressIP[i].length() == 0) {
					return null;
				}
			}
		} catch (Exception e) {
			return null;
		}
		return address;
	}

	// TODO 检测网络状态 返回当前的网络连接情况
	public String[] checkNet() {
		String[] result = null;
		NetworkInfo netInfo = null;
		try {
			netInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();

			String wifiSSID = wifiInfo.getSSID();
			if (netInfo == null) {
			} else {
				result = new String[2];
				if (wifiSSID != null) {
					result[0] = "WIFI";
					result[1] = wifiSSID;
				} else {
					result[0] = "GPRS";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 判断是否有网络
	 *
	 * @param context
	 * @return
	 */
	public boolean isNetworkConnected() {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				Boolean isNet=mNetworkInfo.isAvailable();
				mConnectivityManager=null;
				mNetworkInfo=null;
				return isNet;
			}
			mConnectivityManager=null;
			mNetworkInfo=null;
		}
		return false;
	}

	/**
	 * 判断wifi网络是否可用
	 *
	 * @param context
	 * @return
	 */
	public boolean isWifiConnected() {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWiFiNetworkInfo != null) {
				return mWiFiNetworkInfo.isAvailable();
			}
		}
		return false;
	}
	/**
	 * 判断Mobile网络是否可用
	 *
	 * @param
	 * @return
	 */
	public boolean isMobileConnected() {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mMobileNetworkInfo != null) {
				return mMobileNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	// TODO Ping ip是否正常
	public String pingAddress(String ip, int timeOut) {
		String result = null;
		if (checkNet() == null) {
			return result;
		}
		try {
			String command = "ping -w 100 " + ip;
			Process process = Runtime.getRuntime().exec(command);
			int status = process.waitFor();
			if (status == 0) {
				result = "SUCCESS";
			} else {
				result = "FAIL";
			}
		} catch (IOException e) {
		} catch (InterruptedException e) {
		}
		return result;
	}

	// TODO 手机号码判断,错误返回null,否则返回处理之后的手机号码
	public String checkPhoneNumber(String number) {
		if (number == null) {
			return null;
		}
		try {
			number = number.replaceAll(" |-", "");
			boolean result = number.matches("^(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
			if (result) {
				return number;
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	// TODO 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	public int dipToPx(Context context, int dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	// TODO 手机屏幕密度
	public int pxToDip(Context context, int pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	// TODO 获取当前包名
	public String getSoftPackage() {
		String result = null;
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			result = info.packageName;
		} catch (NameNotFoundException e) {
		}
		return result;
	}

	// TODO 获取当前版本
	public String getSoftVersion() {
		String result = "";
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			result = info.versionName;
		} catch (NameNotFoundException e) {
		}
		return result;
	}
	// TODO 创建本地文件目录
	public boolean createFolders() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			String sdPath = Environment.getExternalStorageDirectory().toString();
			String logPath = sdPath + "/" + context.getPackageName() + "/" + "log";
			String apkPath = sdPath + "/" + context.getPackageName() + "/" + "apk";
			String[] pathArray = { logPath, apkPath };
			File fileFolder;
			for (int i = 0; i < pathArray.length; i++) {
				fileFolder = new File(pathArray[i]);
				if (!fileFolder.exists()) {
					fileFolder.mkdirs();
				}
			}
		}
		return true;
	}
	// TODO 获取当前程序名
	public String getSoftName() {
		String applicationName = null;
		PackageManager packageManager = null;
		ApplicationInfo applicationInfo = null;
		try {
			packageManager = context.getApplicationContext().getPackageManager();
			applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
			applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
		} catch (NameNotFoundException e) {
		}
		return applicationName;
	}

	// TODO 获取可用运存大小
	public int getAvailMemory() {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		int result = (int) (mi.availMem / (1024 * 1024));
		return result;
	}

	// TODO 获取总运存大小
	public long getTotalMemory() {
		// 系统内存信息文件
		String str1 = "/proc/meminfo";
		String str2;
		String[] arrayOfString;
		long totalMemory = 0;
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			totalMemory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;
			localBufferedReader.close();
		} catch (IOException e) {
		}
		int result = (int) (totalMemory / (1024 * 1024));
		return result;
	}
}

