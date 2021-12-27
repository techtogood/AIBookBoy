package ai.aistem.xbot.framework.internal.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 *@author aistem
 *@date  2018-08-01
 *@describe 获取机器人硬件信息
 **/

public class RobotInfoUtil {
    public static String getRobotHostAddress() {
        String ip = "0.0.0.0";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            byte[] mac = null;
            while (interfaces.hasMoreElements() && (mac == null || mac.length != 6)) {
                NetworkInterface netInterface = interfaces.nextElement();
                if (netInterface.getDisplayName().contains("wlan")) {
                    mac = netInterface.getHardwareAddress();
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        // *EDIT*
                        if (addr instanceof Inet6Address) continue;
                        ip = addr.getHostAddress();
                    }
                }
            }
            return ip;
        } catch (Exception e) {
            throw new RuntimeException("Could not get Host address");
        }
    }

    public static String getRobotMacAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            byte[] mac = null;

            while (interfaces.hasMoreElements() && (mac == null || mac.length != 6)) {
                NetworkInterface netInterface = interfaces.nextElement();
                if (netInterface.getDisplayName().contains("wlan"))
                    mac = netInterface.getHardwareAddress();
            }

            // if the machine is not connected to a network it has no active MAC address
            if (mac == null)
                mac = new byte[]{0, 0, 0, 0, 0, 0};

            char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    'a', 'b', 'c', 'd', 'e', 'f'};
            int j = mac.length;
            char str[] = new char[j * 2 + 5];
            int k = 0;
            for (byte byte0 : mac) {
                str[k++] = hexDigits[byte0 >> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
                if (k < j * 2 + 5) {
                    str[k++] = ':';
                }
            }
            return new String(str);
        } catch (Exception e) {
            throw new RuntimeException("Could not get MAC address");
        }
    }


    public static String packageName(Context context) {
        PackageManager manager = context.getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return name;
    }

    public static String getDeviceSerial() {
        String serial = "unknown";
        try {
            Class clazz = Class.forName("android.os.Build");
            Class paraTypes = Class.forName("java.lang.String");
            Method method = clazz.getDeclaredMethod("getString", paraTypes);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            serial = (String)method.invoke(new Build(), "ro.serialno");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return serial;
    }


}
