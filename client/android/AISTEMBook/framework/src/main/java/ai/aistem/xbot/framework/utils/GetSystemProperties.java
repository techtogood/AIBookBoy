package ai.aistem.xbot.framework.utils;

import java.lang.reflect.Method;

public class GetSystemProperties {
    private static volatile Method set = null;
    private static volatile Method get = null;

    public static void set(String prop, String value) {

        try {
            if (null == set) {
                synchronized (GetSystemProperties.class) {
                    if (null == set) {
                        Class<?> cls = Class.forName("android.os.SystemProperties");
                        set = cls.getDeclaredMethod("set", new Class<?>[]{String.class, String.class});
                    }
                }
            }
            set.invoke(null, new Object[]{prop, value});
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public static String get(String prop, String defaultvalue) {
        String value = defaultvalue;
        try {
            if (null == get) {
                synchronized (GetSystemProperties.class) {
                    if (null == get) {
                        Class<?> cls = Class.forName("android.os.SystemProperties");
                        get = cls.getDeclaredMethod("get", new Class<?>[]{String.class, String.class});
                    }
                }
            }
            value = (String) (get.invoke(null, new Object[]{prop, defaultvalue}));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return value;
    }

    //获取设备名称
    public static String getBuildDisplayId() {
        String Id = GetSystemProperties.get("ro.build.display.id","20181111");
        if (Id.contains(" ")) {
            Id = Id.replaceAll(" ", "");
        }
        return Id;
    }
}