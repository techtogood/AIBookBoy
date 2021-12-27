package ai.aistem.xbot.framework.utils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;

import ai.aistem.xbot.framework.application.DCApplication;

public class LogFileUtil {
	
	/**
	 * 默认开启打印
	 */
	private static boolean isWriteLogFile=false;
	
	
	/**
	 * 设置是否能打印,True开启,false关闭
	 * @param isWriteLogFile
	 */
	public static void setWriteLogFile(boolean isWriteLogFile) {
		LogFileUtil.isWriteLogFile = isWriteLogFile;
	}



	/**
	 * 写入日志,这是一个临时的方法,后期可能会去掉.
	 */
	public static void writeFileSdcard(String message) {
		if(!isWriteLogFile){
			return;
		}
		String path = Environment.getExternalStorageDirectory().getPath() + "/" + DCApplication.app.getPackageName() + "/cache/temp_log.txt";
		SimpleDateFormat sDateFormat= new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String date    =    sDateFormat.format(new    java.util.Date());
		String msg="\r\n"+date+"  "+message;
		try {
			File file = new File(path);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}else {
				long length = file.length();
				if (length>1048576)
				{
					file.delete();
					file.getParentFile().mkdirs();
					file.createNewFile();
				}
			}
			BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, true),"GB2312"));
			writer.write(msg);
			writer.close();

		}

		catch (Exception e) {

			e.printStackTrace();

		}
	}

}
