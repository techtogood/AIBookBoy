package ai.aistem.xbot.framework.data.prefs;

/**
 * 系统配置 高级配置
 * Created by aistem on 04/05/18.
 */
public interface PreferencesHelper {

    /**
     * 清除所有设置
     */
    void clear();

    /**
     * 获取语音文字提示开关
     *
     * @return true/false 开或关
     */
    boolean getSpeechToTextDisplaySwitch();

    /**
     * 设置语音文字提示开关
     *
     * @param s true/false 开或关
     */
    void setSpeechToTextDisplaySwitch(boolean s);

    /**
     * 获取护眼模式开关
     *
     * @return true/false 开或关
     */
    boolean getEyeProtectionModeSwitch();

    /**
     * 设置护眼模式开关
     *
     * @param s true/false 开或关
     */
    void setEyeProtectionModeSwitch(boolean s);

    /**
     * 获取菜单语音播报开关
     *
     * @return true/false 开或关
     */
    boolean getMenuVoicePromptSwitch();

    /**
     * 置菜单语音播报开关
     *
     * @param s true/false 开或关
     */
    void setMenuVoicePromptSwitchSwitch(boolean s);


    /**
     * 获取屏幕亮度自动调节开关
     *
     * @return true/false 开或关
     * @link https://developer.android.com/reference/android/provider/Settings.System#screen_brightness
     */
    boolean getScreenBrightnessModeAutomaticSwitch();

    /**
     * 设置屏幕亮度自动调节开关
     *
     * @param s true/false 开或关
     */
    void setScreenBrightnessModeAutomaticSwitch(boolean s);


    /**
     * 获取系统信息：屏幕亮度
     *
     * @return 屏幕亮度
     */
    int getScreenBrightness();


    /**
     * 设置配置文件中屏幕亮度
     *
     * @param brightness 屏幕亮度
     */
    void setScreenBrightness(int brightness);

    /**
     * 获取系统信息:电池容量
     *
     * @return 容量 1-100
     */
    int getBatteryCapacity();


    /**
     * 获取流媒体音量
     *
     * @return 音量 0-15
     */
    int getStreamMusicVolume();


    /**
     * 增大流媒体音量
     * 根据音量分为4个等级[0] [1-5] [6-10] [11-15]，调用一次增加一个等级
     */
    void RaiseStreamMusicVolume();


    /**
     * 减少流媒体音量
     */
    void LowerStreamMusicVolume();


    /**
     * 应用版本号
     * android:versionCode:主要是用于版本升级所用，是INT类型的，第一个版本定义为1，以后递增，这样只要判断该值就能确定是否需要升级，该值不显示给用户。
     * android:versionName:这个是我们常说明的版本号，由三部分组成<major>.<minor>.<point>,该值是个字符串，可以显示给用户。
     *
     * @return code
     */
    int getAppLocalVersionCode();

    /**
     * 应用版本名称
     *
     * @return name
     */
    String getAppLocalVersionName();


    /**
     * 应用版本描述
     *
     * @return note
     */
    String getAppLocalReleaseNote();

    /**
     * 设置应用版本描述
     *
     * @return note
     */
    void setAppLocalReleaseNote(String note);

    /**
     * 应用初始化启动开关
     *
     * @return true:初始化启动 false:正常启动
     */
    boolean getAppFirstLaunchSwitch();

    /**
     * 获取是否用户绑定
     *
     * @return true:已绑定 false：未绑定
     */
    boolean getUserPaired();


    /**
     * 设置绑定标识
     *
     * @param s true:绑定 false：解绑
     */
    void setUserPaired(boolean s);



    /**
     * 获取机器人ID
     *
     * @return
     */
    int getRobotID();

    /**
     * 设置机器人ID
     *
     * @param rid
     */
    void setRobotID(int rid);


    /**
     * 性别
     *
     * */
    void setRobotGender(int gender);

    int getRobotGender();

    /**
     * 头像
     * @param url
     * */
    void setRobotAvatar(String url);

    String getRobotAvatar();

    /**
     * 获取设备与HTTP服务器通信的Token
     *
     * @return
     */
    String getRobotAuthToken();

    /**
     * 设置通信Token
     *
     * @param token 令牌
     */
    void setRobotAuthToken(String token);

    /**
     * 获取Token过期时间
     *
     * @return
     */
    String getRobotTokenExpireTime();

    /**
     * 设置通信Token过期时间
     *
     * @param date 令牌
     */
    void setRobotTokenExpireTime(String date);

    /**
     * 获取设备与HTTP服务器通信的Refresh Token
     *
     * @return
     */
    String getRobotRefreshToken();

    /**
     * 设置Refresh Token
     *
     * @param token 令牌
     */
    void setRobotRefreshToken(String token);


    /**
     * 获取设备与MQTT服务器通信的Username
     *
     * @return
     */
    String getRobotMqttUsername();

    /**
     * 设置设备与MQTT服务器通信的Username
     *
     * @param username 用户名
     */
    void setRobotMqttUsername(String username);

    /**
     * 获取设备与MQTT服务器通信的Password
     *
     * @return
     */
    String getRobotMqttPassword();

    /**
     * 设置设备与MQTT服务器通信的Password
     *
     * @param password 密码
     */
    void setRobotMqttPassword(String password);

    /**
     * 获取夜间模式开关
     *
     * @return true/false 开或关
     */
    boolean getRobotNightModeSwitch();

    /**
     * 设置夜间模式开关
     *
     * @param s true/false 开或关
     */
    void setRobotNigthModeSwitch(boolean s);

    /**
     * 获取夜间模式开始时间
     *
     * @return
     */
    String getRobotNightModeStartTime();

    /**
     * 设置夜间模式开始时间
     *
     * @param start 开始时间
     */
    void setRobotNightModeStartTime(String start);

    /**
     * 获取夜间模式结束时间
     *
     * @return
     */
    String getRobotNightModeEndTime();

    /**
     * 设置夜间模式结束时间
     *
     * @param end 结束时间
     */
    void setRobotNightModeEndTime(String end);

    /**
     * 获取防沉迷开关
     *
     * @return
     */
    boolean getRobotAntiAddictionSwitch();

    /**
     * 设置防沉迷开关
     *
     * @param s 开或关
     */
    void setRobotAntiAddictionSwitch(boolean s);

    /**
     * 获取防沉迷持续时间
     *
     * @return
     */
    int getRobotAntiAddictionDuration();

    /**
     * 设置防沉迷持续时间
     *
     * @param duration 持续时间
     */
    void setRobotAntiAddictionDuration(int duration);

    /**
     * 获取防沉迷休息时间
     *
     * @return
     */
    int getRobotAntiAddictionRestDuration();

    /**
     * 设置防沉迷休息时间
     *
     * @param duration 休息时间
     */
    void setRobotAntiAddictionRestDuration(int duration);

    /**
     * 获取机器人code
     *
     * @return
     */
    String getRobotCode();

    /**
     * 设置机器人code
     *
     * @param code
     */
    void setRobotCode(String code);


    /**
     * 获取是否上传磨耳朵成就勋章
     *
     * @return true/false 是/否
     */
    boolean getListenAchievementRecordUploadSwitch();

    /**
     * 设置是否上传磨耳朵成就勋章
     *
     * @param s true/false 是/否
     */
    void setListenAchievementRecordUploadSwitch(boolean s);
}
