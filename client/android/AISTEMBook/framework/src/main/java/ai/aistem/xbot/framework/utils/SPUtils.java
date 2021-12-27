package ai.aistem.xbot.framework.utils;

import android.content.Context;
import android.content.SharedPreferences;

import ai.aistem.xbot.framework.application.DCApplication;


/**
 * @author: aistem
 * @created: 2018/5/15/17:58
 * @desc: SPUtils
 */
public class SPUtils implements IPreferences {
    private static SharedPreferences mPreferences;
    private static SharedPreferences.Editor mEditor;
    private static SPUtils instance;

    private SPUtils(){
        mPreferences =   DCApplication.app.getSharedPreferences(DEEPDONV_APPLICATION_FILE,Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();
    }

    public static synchronized SPUtils getInstance() {
        if (instance == null) {
            instance = new SPUtils();
        }
        return instance;
    }


    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param key
     * @param object
     */
    private void setParam(String key, Object object) {

        String type = object.getClass().getSimpleName();

        if ("String".equals(type)) {
            mEditor.putString(key, (String) object);
        } else if ("Integer".equals(type)) {
            mEditor.putInt(key, (Integer) object);
        } else if ("Boolean".equals(type)) {
            mEditor.putBoolean(key, (Boolean) object);
        } else if ("Float".equals(type)) {
            mEditor.putFloat(key, (Float) object);
        } else if ("Long".equals(type)) {
            mEditor.putLong(key, (Long) object);
        }

        mEditor.commit();
    }


    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param key
     * @param defaultObject
     * @return
     */
    private Object getParam(String key, Object defaultObject) {
        String type = defaultObject.getClass().getSimpleName();

        if ("String".equals(type)) {
            return mPreferences.getString(key, (String) defaultObject);
        } else if ("Integer".equals(type)) {
            return mPreferences.getInt(key, (Integer) defaultObject);
        } else if ("Boolean".equals(type)) {
            return mPreferences.getBoolean(key, (Boolean) defaultObject);
        } else if ("Float".equals(type)) {
            return mPreferences.getFloat(key, (Float) defaultObject);
        } else if ("Long".equals(type)) {
            return mPreferences.getLong(key, (Long) defaultObject);
        }

        return null;
    }


    private void remove(String key) {
        mEditor.remove(key);
        mEditor.commit();
    }


    /**
     * 记录用户是否第一次进入应用
     * <p>
     * 首页后的WiFi连接成功后就算第一次完成
     */
    public void saveFirstEnterApp(boolean isFirstTime) {
        setParam(FIRST_ENTER_KEY, isFirstTime);
    }

    /**
     * 获取用户是否第一次进入应用
     * true表示第一次
     * false表示不是第一次
     */
    public boolean isFirstEnterApp() {
        return (boolean) getParam(FIRST_ENTER_KEY, true);
    }


    /**
     * 记录用户是否第一次进入应用
     * <p>
     * 首页后的WiFi连接成功后就算第一次完成
     */
    public void saveFirstShowSlide(boolean isFirstTime) {
        setParam(FIRST_SHOW_ALERT_KEY, isFirstTime);
    }

    /**
     * 获取用户是否第一次进入应用
     * true表示第一次
     * false表示不是第一次
     */
    public boolean isFirstShowSlide() {
        return (boolean) getParam(FIRST_SHOW_ALERT_KEY, true);
    }


    public void saveWordIndex(int wordindex) {
        setParam(LETTER_BOX_WORD_INDEX, wordindex);
    }

    public int getWordIndex() {
        return (int) getParam(LETTER_BOX_WORD_INDEX, 0);
    }


    /**
     * 记录当前语聊的对象
     */
    public void saveChatPerson(int personId) {
        setParam(CURRENT_CHAT_PERSON, personId);
    }

    public int getChatPerson(int defaultId) {
        return (int) getParam(CURRENT_CHAT_PERSON, defaultId);
    }


    public void saveUserRelation(String relation) {
        setParam(CURRENT_CHAT_PERSON_RELATION, relation);
    }

    public String getUserRelation() {
        return (String) getParam(CURRENT_CHAT_PERSON_RELATION, "");
    }


    /**
     * true 自动调节
     * false 手动调节
     */
    public void saveBrightMethod(boolean value) {
        setParam(SYSTEM_SETTING_SCREEN_BRIGHT, value);
    }

    public boolean getBrightMethod() {
        return (boolean) getParam(SYSTEM_SETTING_SCREEN_BRIGHT, false);
    }

    public void saveBrightness(int value) {
        setParam(SYSTEM_SETTING_SCREEN_BRIGHTNESS, value);
    }

    public int getBrightness() {
        return (int) getParam(SYSTEM_SETTING_SCREEN_BRIGHTNESS, 255);
    }



    public void saveNormallyOnStatus(boolean status) {
        setParam(SYSTEM_SETTING_NORMALLY_ON, status);
    }

    public boolean getNormallyOnStatus() {
        return (boolean) getParam(SYSTEM_SETTING_NORMALLY_ON, false);
    }


    public void saveAntiAddictionStatus(boolean status) {
        setParam(SYSTEM_SETTING_ANTI_ADDICTION, status);
    }

    public boolean getAntiAddictionStatus() {
        return (boolean) getParam(SYSTEM_SETTING_ANTI_ADDICTION, true);
    }


    public void saveAntiUseTime(int time) {
        setParam(SETTING_ANTI_USE_TIME, time);
    }

    public int getAntiUseTime(int defaultTime) {
        return (int) getParam(SETTING_ANTI_USE_TIME, defaultTime);
    }

    public void saveAntiRestTime(int time) {
        setParam(SETTING_ANTI_REST_TIME, time);
    }

    public int getAntiRestTime(int defaultTime) {
        return (int) getParam(SETTING_ANTI_REST_TIME, defaultTime);
    }

    public void saveProtectEyeStatus(boolean status) {
        setParam(SYSTEM_SETTING_PROTECT_EYE, status);
    }

    public boolean getProtectEyeStatus() {
        return (boolean) getParam(SYSTEM_SETTING_PROTECT_EYE, false);
    }

    public void saveVoiceDialogStatus(boolean status) {
        setParam(SYSTEM_SETTING_VOICE_ALERT, status);
    }

    public boolean getVoiceDialogStatus() {
        return (boolean) getParam(SYSTEM_SETTING_VOICE_ALERT, true);
    }

    public void saveNightModeStatus(boolean status) {
        setParam(SYSTEM_SETTING_NIGHT_MODE, status);
    }

    public boolean getNightModeStatus() {
        return (boolean) getParam(SYSTEM_SETTING_NIGHT_MODE, true);
    }


    /**
     * 存储WiFi密码
     *
     * @param SSID     :WiFi SSID 作为key值
     * @param password : value
     */
    public void saveTddeWiFiInfo(String SSID, String password) {
        setParam(SSID, password);
    }

    public String getTddeWiFiInfo(String SSID) {
        return (String) getParam(SSID, "-1");
    }

    public void removeWiFiInfo(String SSID) {
        remove(SSID);
    }

    /**
     * 存储防沉迷休息起始时间（不能用的时间起点）
     */
    public void saveAntiAddiRestTimeStamp() {
        Long time = System.currentTimeMillis();
        setParam("AntiAddiRest", time);
    }

    public Long getAntiAddiRestTimeStamp() {
        return (Long) getParam("AntiAddiRest", 0L);
    }

    public void removeAntiAddiRestTimeStamp(){
        remove("AntiAddiRest");
    }

    /**
     * 存储防沉迷使用时间
     */
    public void saveAntiAddiWorkTime(Long time){
        setParam("AntiAddiWorkTime", time);
    }

    public Long getAntiAddiWorkTime(){
        return (Long) getParam("AntiAddiWorkTime", 0L);
    }

    public void removeAntiAddiWorkTime(){
        remove("AntiAddiWorkTime");
    }


    /**************************Debug*************************/

    public void saveLauncherDebug(boolean mode) {
        setParam("LAUNCHER_DEBUG", mode);
    }

    public boolean getLauncherDebug() {
        return (boolean) getParam("LAUNCHER_DEBUG", false);
    }

    public void saveCharRecognitionDebug(boolean mode) {
        setParam("RECOGNITION_DEBUG", mode);
    }

    public boolean getCharRecognitionDebug() {
        return (boolean) getParam("RECOGNITION_DEBUG", false);
    }

    public void savePowerConnectedStatus(boolean status){
        setParam(DEVICE_POWER_CONNECTED_KEY,status);
    }

    public boolean getPowerConnectedStatus(){
        return (boolean)getParam(DEVICE_POWER_CONNECTED_KEY,false);
    }

    /******************save game version***************/

    public void saveLauncherVersion(int version){
        setParam("LAUNCHER_VERSION",version);
    }

    public int getLauncherVersion(){
        return (int)getParam("LAUNCHER_VERSION",116);
    }


    /******************save game version***************/

    public void savexbotGameVersion(String verKey,int version){
        setParam(verKey,version);
    }

    public int getxbotGameVersion(String verKey){
        return (int)getParam(verKey,0);
    }


    /******************save Music index***************/

    public void saveMusicIndex(String musicKey,int index){
        setParam(musicKey,index);
    }

    public int getMusicIndex(String musicKey){
        return (int)getParam(musicKey,0);
    }

    /**************************User ID************************/
    public void saveUserId(int id){
        setParam(CHAT_USER_ID_KEY,id);
    }
    public int getUserId(){
        return (int)getParam(CHAT_USER_ID_KEY,0);
    }

    public void saveWiFiError(boolean isError){
        setParam(SETTING_WIFI_ERROR_KEY,isError);
    }
    public boolean getWiFiError(){
        return (boolean)getParam(SETTING_WIFI_ERROR_KEY,true);
    }
}
