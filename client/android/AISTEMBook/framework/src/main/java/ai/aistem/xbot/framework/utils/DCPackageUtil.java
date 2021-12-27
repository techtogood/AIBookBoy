package ai.aistem.xbot.framework.utils;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class DCPackageUtil {
    public static String TAG = DCPackageUtil.class.getSimpleName();
    private Context mContext;

    public DCPackageUtil(Context context) {
        mContext = context.getApplicationContext();
    }

    // 静默安装，1-安装成功 2-升级安装出现异常 3-没有安装文件 -1-程序异常
    public static int installBySlient(String filePath) {
        int result = 0;
        try {
            File file = new File(filePath);
            if (filePath.isEmpty() || file.length() <= 0 || !file.exists() || !file.isFile()) {
                return 3;
            }
            String[] args = {"pm", "install", "-r", filePath};
            //String[] args = { "pm", "install","-i ","包名", "-r",filePath }; //适用7.0
            //String[] args = {"pm", "install", "-r", "包名", "--user", "0", filePath};
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            Process process = null;
            BufferedReader successResult = null;
            BufferedReader errorResult = null;
            StringBuilder successMsg = new StringBuilder();
            StringBuilder errorMsg = new StringBuilder();
            try {
                process = processBuilder.start();
                successResult = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(
                        process.getErrorStream()));
                String s;

                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                }

                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = 2;
            } catch (Exception e) {
                e.printStackTrace();
                result = 2;
            } finally {
                try {
                    if (successResult != null) {
                        successResult.close();
                    }
                    if (errorResult != null) {
                        errorResult.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (process != null) {
                    process.destroy();
                }
            }

            if (successMsg.toString().contains("Success")
                    || successMsg.toString().contains("success")) {
                result = 1;
                Log.i(TAG, "安装成功result = 1");
            } else {
                result = 2;
            }
            Log.i(TAG, "App升级信息:" + "successMsg:" + successMsg + ", ErrorMsg:" + errorMsg);
        } catch (Exception e) {
            result = -1;
        }
        return result;
    }

    // 静默安装，1-安装成功，或没有升级文件，2-升级安装出现异常，-1-程序异常
    public static int uninstallBySlient( String packageName) {
        int result = 0;
        try {
//            File file = new File(filePath);
//            if (filePath == null || filePath.length() == 0
//                    || (file = new File(filePath)) == null
//                    || file.length() <= 0 || !file.exists() || !file.isFile()) {
//                return 1;
//            }
            String[] args = {"pm", "uninstall", "-k", packageName};
            //String[] args = { "pm", "install","-i ","包名", "-r",filePath }; //适用7.0
            //String[] args = {"pm", "install", "-r", "包名", "--user", "0", filePath};
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            Process process = null;
            BufferedReader successResult = null;
            BufferedReader errorResult = null;
            StringBuilder successMsg = new StringBuilder();
            StringBuilder errorMsg = new StringBuilder();
            try {
                process = processBuilder.start();
                successResult = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(
                        process.getErrorStream()));
                String s;

                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                }

                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = 2;
            } catch (Exception e) {
                e.printStackTrace();
                result = 2;
            } finally {
                try {
                    if (successResult != null) {
                        successResult.close();
                    }
                    if (errorResult != null) {
                        errorResult.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (process != null) {
                    process.destroy();
                }
            }

            if (successMsg.toString().contains("Success")
                    || successMsg.toString().contains("success")) {
                result = 1;
                Log.i(TAG, "卸载成功result = 1");
            } else {
                result = 2;
            }
            Log.i(TAG, "App升级信息:" + "successMsg:" + successMsg + ", ErrorMsg:" + errorMsg);
        } catch (Exception e) {
            result = -1;
        }
        return result;
    }

    public int getPackageVersionCode(String packageName) {
        PackageInfo pinfo = null;
        int verCode = 0;
        try {
            pinfo = mContext.getPackageManager().getPackageInfo(packageName, 0);
            verCode = pinfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
        }
        return verCode;
    }



}
