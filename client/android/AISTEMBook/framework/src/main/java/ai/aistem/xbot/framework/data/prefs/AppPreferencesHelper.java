package ai.aistem.xbot.framework.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;

/**
 * Created by aistem on 04/05/18.
 * 系统配置辅助类
 */
public class AppPreferencesHelper implements PreferencesHelper {

    public static final String PREF_KEY_SPEECH_TO_TEXT_DISPLAY_SWITCH
            = "PREF_KEY_SPEECH_TO_TEXT_DISPLAY_SWITCH";
    public static final String PREF_KEY_EYE_PROTECTION_MODE_SWITCH
            = "PREF_KEY_SPEECH_TO_TEXT_DISPLAY_SWITCH";
    public static final String PREF_MENU_VOICE_PROMPT_SWITCH
            = "PREF_MENU_VOICE_PROMPT_SWITCH";
    public static final String PREF_SCREEN_BRIGHTNESS_MODE_AUTOMATIC_SWITCH
            = "PREF_SCREEN_BRIGHTNESS_MODE_AUTOMATIC_SWITCH";
    public static final String PREF_SCREEN_BRIGHTNESS
            = "PREF_SCREEN_BRIGHTNESS";
    public static final String PREF_APP_LOCAL_VERSION_CODE
            = "PREF_APP_LOCAL_VERSION_CODE";
    public static final String PREF_APP_LOCAL_VERSION_NAME
            = "PREF_APP_LOCAL_VERSION_NAME";
    public static final String PREF_APP_LOCAL_RELEASE_NOTE
            = "PREF_APP_LOCAL_RELEASE_NOTE";
    public static final String PREF_DEVICE_PAIRED
            = "PREF_DEVICE_PAIRED";
    public static final String PREF_BATTERY_CAPCITY
            = "PREF_BATTERY_CAPCITY";
    public static final String PREF_ROBOT_ID
            = "PREF_ROBOT_ID";
    public static final String PREF_ROBOT_AUTH_TOKEN
            = "PREF_ROBOT_AUTH_TOKEN";
    public static final String PREF_ROBOT_TOKEN_EXPIRE_TIME
            = "PREF_ROBOT_TOKEN_EXPIRE_TIME";
    public static final String PREF_ROBOT_REFRESH_TOKEN
            = "PREF_ROBOT_REFRESH_TOKEN";
    public static final String PREF_ROBOT_MQTT_USERNAME
            = "PREF_ROBOT_MQTT_USERNAME";
    public static final String PREF_ROBOT_MQTT_PASSWORD
            = "PREF_ROBOT_MQTT_PASSWORD";
    public static final String PREF_ROBOT_NIGHT_MODE_SWITCH
            = "PREF_ROBOT_NIGHT_MODE_SWITCH";
    public static final String PREF_ROBOT_NIGHT_MODE_START_TIME
            = "PREF_ROBOT_NIGHT_MODE_START_TIME";
    public static final String PREF_ROBOT_NIGHT_MODE_END_TIME
            = "PREF_ROBOT_NIGHT_MODE_END_TIME";
    public static final String PREF_ROBOT_ANTI_ADDICTION_MODE_SWITCH
            = "PREF_ROBOT_ANTI_ADDICTION_MODE_SWITCH";
    public static final String PREF_ROBOT_ANTI_ADDICTION_MODE_DURATION
            = "PREF_ROBOT_ANTI_ADDICTION_MODE_DURATION";
    public static final String PREF_ROBOT_ANTI_ADDICTION_MODE_REST_DURATION
            = "PREF_ROBOT_ANTI_ADDICTION_MODE_REST_DURATION";
    public static final String PREF_ROBOT_CODE
            = "PREF_ROBOT_CODE";
    public static final String PREF_LISTEN_ACHIEVEMENT_RECORD_UPLOAD_SWITCH
            = "PREF_LISTEN_ACHIEVEMENT_RECORD_UPLOAD_SWITCH";

    public static final String PREF_ROBOT_GENDER
            = "PREF_ROBOT_GENDER";


    public static final String PREF_ROBOT_AVATAR
            = "PREF_ROBOT_AVATAR";


    private final SharedPreferences mPrefs;
    private Context mContext;

    public AppPreferencesHelper(Context context, String prefFileName) {
        mContext = context;
        mPrefs = mContext.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
    }

    @Override
    public void clear() {
        mPrefs.edit().clear().apply();
    }

    @Override
    public boolean getSpeechToTextDisplaySwitch() {
        return mPrefs.getBoolean(PREF_KEY_SPEECH_TO_TEXT_DISPLAY_SWITCH, true);
    }

    @Override
    public void setSpeechToTextDisplaySwitch(boolean s) {
        mPrefs.edit().putBoolean(PREF_KEY_SPEECH_TO_TEXT_DISPLAY_SWITCH, s).apply();
    }

    @Override
    public boolean getEyeProtectionModeSwitch() {
        return mPrefs.getBoolean(PREF_KEY_EYE_PROTECTION_MODE_SWITCH, false);
    }

    @Override
    public void setEyeProtectionModeSwitch(boolean s) {
        mPrefs.edit().putBoolean(PREF_KEY_EYE_PROTECTION_MODE_SWITCH, s).apply();
    }

    @Override
    public boolean getMenuVoicePromptSwitch() {
        return mPrefs.getBoolean(PREF_MENU_VOICE_PROMPT_SWITCH, true);
    }

    @Override
    public void setMenuVoicePromptSwitchSwitch(boolean s) {
        mPrefs.edit().putBoolean(PREF_MENU_VOICE_PROMPT_SWITCH, s).apply();
    }

    @Override
    public boolean getScreenBrightnessModeAutomaticSwitch() {
        boolean isAuto;
        try {
            isAuto = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) ==
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            //返回配置文件中的值
            return mPrefs.getBoolean(PREF_SCREEN_BRIGHTNESS_MODE_AUTOMATIC_SWITCH, false);
        }
        return isAuto;
    }

    @Override
    public void setScreenBrightnessModeAutomaticSwitch(boolean s) {
        if (s) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        } else {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        }
        //设置配置文件中的值
        mPrefs.edit().putBoolean(PREF_SCREEN_BRIGHTNESS_MODE_AUTOMATIC_SWITCH, s).apply();
    }

    @Override
    public int getScreenBrightness() {
        int nowBrightnessValue;
        try {
            nowBrightnessValue = Settings.System.getInt(
                    mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
            //返回配置文件中的值
            return mPrefs.getInt(PREF_SCREEN_BRIGHTNESS, 0);
        }
        return nowBrightnessValue;
    }

    @Override
    public void setScreenBrightness(int brightness) {
        if (brightness < 0 || brightness > 255) brightness = 255;
        //TODO 调试是否能生效
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                brightness);
        //设置配置文件中的值
        mPrefs.edit().putInt(PREF_SCREEN_BRIGHTNESS, brightness).apply();
    }

    @Override
    public int getAppLocalVersionCode() {
        int code;
        try {
            PackageInfo packageInfo = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0);
            code = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            //返回配置文件中的值
            return mPrefs.getInt(PREF_APP_LOCAL_VERSION_CODE, 0);
        }
        return code;
    }

    @Override
    public String getAppLocalVersionName() {
        String name;
        try {
            PackageInfo packageInfo = mContext.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(mContext.getPackageName(), 0);
            name = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return mPrefs.getString(PREF_APP_LOCAL_VERSION_NAME, "VersionName");
        }
        return name;
    }

    @Override
    public String getAppLocalReleaseNote() {
        return mPrefs.getString(PREF_APP_LOCAL_RELEASE_NOTE, "最新版本;");
    }

    @Override
    public void setAppLocalReleaseNote(String note) {
        mPrefs.edit().putString(PREF_APP_LOCAL_RELEASE_NOTE, note).apply();
    }

    @Override
    public boolean getAppFirstLaunchSwitch() {
        boolean isFirstStartUp = false;
        int currentVersion, lastVersion;
        try {
            PackageInfo packageInfo = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0);
            currentVersion = packageInfo.versionCode;
            lastVersion = mPrefs.getInt(PREF_APP_LOCAL_VERSION_CODE, 0);
            if (currentVersion != lastVersion) {
                //如果当前版本不等于上次版本，该版本属于第一次启动
                isFirstStartUp = true;
                //将当前版本写入preference中，则下次启动的时候，据此判断，不再为首次启动
                mPrefs.edit().putInt(PREF_APP_LOCAL_VERSION_CODE, currentVersion).apply();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return isFirstStartUp;
    }

    @Override
    public boolean getUserPaired() {
        return mPrefs.getBoolean(PREF_DEVICE_PAIRED, false);
    }

    @Override
    public void setUserPaired(boolean s) {
        mPrefs.edit().putBoolean(PREF_DEVICE_PAIRED, s).apply();
    }

    @Override
    public int getRobotID() {
        return mPrefs.getInt(PREF_ROBOT_ID, -1);
    }

    @Override
    public void setRobotID(int rid) {
        mPrefs.edit().putInt(PREF_ROBOT_ID, rid).apply();
    }

    @Override
    public void setRobotGender(int gender) {
        mPrefs.edit().putInt(PREF_ROBOT_GENDER, gender).apply();
    }

    @Override
    public int getRobotGender() {
        return mPrefs.getInt(PREF_ROBOT_GENDER,0);
    }

    @Override
    public void setRobotAvatar(String url) {
        mPrefs.edit().putString(PREF_ROBOT_AVATAR, url).apply();
    }

    @Override
    public String getRobotAvatar() {
        return mPrefs.getString(PREF_ROBOT_AVATAR,null);
    }

    @Override
    public int getBatteryCapacity() {
        int battery = 0;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                BatteryManager batteryManager =
                        (BatteryManager) mContext.getSystemService(Context.BATTERY_SERVICE);
                assert batteryManager != null;
                battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            }
            //设置配置文件中的值
            //mPrefs.edit().putInt(PREF_BATTERY_CAPCITY, battery);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return battery;
    }

    @Override
    public int getStreamMusicVolume() {
        int volume = 0;
        try {
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            assert am != null;
            volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return volume;
    }

    @Override
    public void RaiseStreamMusicVolume() {
        int current = getStreamMusicVolume();
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        assert am != null;
        if (current < 5)
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 5, AudioManager.FLAG_PLAY_SOUND);
        else if (current < 10)
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 10, AudioManager.FLAG_PLAY_SOUND);
        else if (current <= 15)
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 15, AudioManager.FLAG_PLAY_SOUND);
    }

    @Override
    public void LowerStreamMusicVolume() {
        int current = getStreamMusicVolume();
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        assert am != null;
        if (current <= 5)
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
        else if (current <= 10)
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 5, AudioManager.FLAG_PLAY_SOUND);
        else if (current <= 15)
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 10, AudioManager.FLAG_PLAY_SOUND);
    }


    @Override
    public String getRobotAuthToken() {
        return mPrefs.getString(PREF_ROBOT_AUTH_TOKEN, "");
    }

    @Override
    public void setRobotAuthToken(String token) {
        mPrefs.edit().putString(PREF_ROBOT_AUTH_TOKEN, token).apply();
    }

    @Override
    public String getRobotTokenExpireTime() {
        return mPrefs.getString(PREF_ROBOT_TOKEN_EXPIRE_TIME, "");
    }

    @Override
    public void setRobotTokenExpireTime(String date) {
        mPrefs.edit().putString(PREF_ROBOT_TOKEN_EXPIRE_TIME, date).apply();
    }

    @Override
    public String getRobotRefreshToken() {
        return mPrefs.getString(PREF_ROBOT_REFRESH_TOKEN, "");
    }

    @Override
    public void setRobotRefreshToken(String token) {
        mPrefs.edit().putString(PREF_ROBOT_REFRESH_TOKEN, token).apply();
    }

    @Override
    public String getRobotMqttUsername() {
        return mPrefs.getString(PREF_ROBOT_MQTT_USERNAME, "");
    }

    @Override
    public void setRobotMqttUsername(String username) {
        mPrefs.edit().putString(PREF_ROBOT_MQTT_USERNAME, username).apply();
    }

    @Override
    public String getRobotMqttPassword() {
        return mPrefs.getString(PREF_ROBOT_MQTT_PASSWORD, "");
    }

    @Override
    public void setRobotMqttPassword(String password) {
        mPrefs.edit().putString(PREF_ROBOT_MQTT_PASSWORD, password).apply();
    }

    @Override
    public boolean getRobotNightModeSwitch() {
        return mPrefs.getBoolean(PREF_ROBOT_NIGHT_MODE_SWITCH, false);
    }

    @Override
    public void setRobotNigthModeSwitch(boolean s) {
        mPrefs.edit().putBoolean(PREF_ROBOT_NIGHT_MODE_SWITCH, s).apply();
    }

    @Override
    public String getRobotNightModeStartTime() {
        return mPrefs.getString(PREF_ROBOT_NIGHT_MODE_START_TIME, "21:30");
    }

    @Override
    public void setRobotNightModeStartTime(String start) {
        mPrefs.edit().putString(PREF_ROBOT_NIGHT_MODE_START_TIME, start).apply();
    }

    @Override
    public String getRobotNightModeEndTime() {
        return mPrefs.getString(PREF_ROBOT_NIGHT_MODE_END_TIME, "10:00");
    }

    @Override
    public void setRobotNightModeEndTime(String end) {
        mPrefs.edit().putString(PREF_ROBOT_NIGHT_MODE_END_TIME, end).apply();
    }

    @Override
    public boolean getRobotAntiAddictionSwitch() {
        return mPrefs.getBoolean(PREF_ROBOT_ANTI_ADDICTION_MODE_SWITCH, false);
    }

    @Override
    public void setRobotAntiAddictionSwitch(boolean s) {
        mPrefs.edit().putBoolean(PREF_ROBOT_ANTI_ADDICTION_MODE_SWITCH, s).apply();
    }

    @Override
    public int getRobotAntiAddictionDuration() {
        return mPrefs.getInt(PREF_ROBOT_ANTI_ADDICTION_MODE_DURATION, 30);
    }

    @Override
    public void setRobotAntiAddictionDuration(int duration) {
        mPrefs.edit().putInt(PREF_ROBOT_ANTI_ADDICTION_MODE_DURATION, duration).apply();
    }

    @Override
    public int getRobotAntiAddictionRestDuration() {
        return mPrefs.getInt(PREF_ROBOT_ANTI_ADDICTION_MODE_REST_DURATION, 10);
    }

    @Override
    public void setRobotAntiAddictionRestDuration(int duration) {
        mPrefs.edit().putInt(PREF_ROBOT_ANTI_ADDICTION_MODE_REST_DURATION, duration).apply();
    }

    @Override
    public String getRobotCode() {
        return mPrefs.getString(PREF_ROBOT_CODE, "-1");
    }

    @Override
    public void setRobotCode(String code) {
        mPrefs.edit().putString(PREF_ROBOT_CODE, code).apply();
    }

    @Override
    public boolean getListenAchievementRecordUploadSwitch() {
        return mPrefs.getBoolean(PREF_LISTEN_ACHIEVEMENT_RECORD_UPLOAD_SWITCH, false);
    }

    @Override
    public void setListenAchievementRecordUploadSwitch(boolean s) {
        mPrefs.edit().putBoolean(PREF_LISTEN_ACHIEVEMENT_RECORD_UPLOAD_SWITCH, s).apply();
    }
}
