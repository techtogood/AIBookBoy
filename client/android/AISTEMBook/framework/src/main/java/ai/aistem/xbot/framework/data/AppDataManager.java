package ai.aistem.xbot.framework.data;

import android.content.Context;

import ai.aistem.xbot.framework.data.prefs.PreferencesHelper;


/**
 * Created by aistem on 04/05/18.
 * Refer:
 */

public class AppDataManager implements DataManager {
    private static final String TAG = AppDataManager.class.getSimpleName();

    private final PreferencesHelper mPreferencesHelper;

    public AppDataManager(Context context, PreferencesHelper preferencesHelper) {
        mPreferencesHelper = preferencesHelper;
    }

    public void clear() {
        mPreferencesHelper.clear();
    }

    @Override
    public boolean getSpeechToTextDisplaySwitch() {
        return mPreferencesHelper.getSpeechToTextDisplaySwitch();
    }

    @Override
    public void setSpeechToTextDisplaySwitch(boolean s) {
        mPreferencesHelper.setSpeechToTextDisplaySwitch(s);
    }

    @Override
    public boolean getEyeProtectionModeSwitch() {
        return mPreferencesHelper.getEyeProtectionModeSwitch();
    }

    @Override
    public void setEyeProtectionModeSwitch(boolean s) {
        mPreferencesHelper.setEyeProtectionModeSwitch(s);
    }

    @Override
    public boolean getMenuVoicePromptSwitch() {
        return mPreferencesHelper.getMenuVoicePromptSwitch();
    }

    @Override
    public void setMenuVoicePromptSwitchSwitch(boolean s) {
        mPreferencesHelper.setMenuVoicePromptSwitchSwitch(s);
    }

    @Override
    public boolean getScreenBrightnessModeAutomaticSwitch() {
        return mPreferencesHelper.getScreenBrightnessModeAutomaticSwitch();
    }

    @Override
    public void setScreenBrightnessModeAutomaticSwitch(boolean s) {
        mPreferencesHelper.setScreenBrightnessModeAutomaticSwitch(s);
    }

    @Override
    public int getScreenBrightness() {
        return mPreferencesHelper.getScreenBrightness();
    }

    @Override
    public void setScreenBrightness(int brightness) {
        mPreferencesHelper.setScreenBrightness(brightness);
    }

    @Override
    public int getAppLocalVersionCode() {
        return mPreferencesHelper.getAppLocalVersionCode();
    }

    @Override
    public String getAppLocalVersionName() {
        return mPreferencesHelper.getAppLocalVersionName();
    }

    @Override
    public String getAppLocalReleaseNote() {
        return mPreferencesHelper.getAppLocalReleaseNote();
    }

    @Override
    public void setAppLocalReleaseNote(String note) {
        mPreferencesHelper.setAppLocalReleaseNote(note);
    }

    @Override
    public boolean getAppFirstLaunchSwitch() {
        return mPreferencesHelper.getAppFirstLaunchSwitch();
    }

    @Override
    public boolean getUserPaired() {
        return mPreferencesHelper.getUserPaired();
    }

    @Override
    public void setUserPaired(boolean s) {
        mPreferencesHelper.setUserPaired(s);
    }

    @Override
    public int getRobotID() {
        return mPreferencesHelper.getRobotID();
    }

    @Override
    public void setRobotID(int rid) {
        mPreferencesHelper.setRobotID(rid);
    }

    @Override
    public void setRobotGender(int gender) {
        mPreferencesHelper.setRobotGender(gender);
    }

    @Override
    public int getRobotGender() {
        return mPreferencesHelper.getRobotGender();
    }

    @Override
    public void setRobotAvatar(String url) {
        mPreferencesHelper.setRobotAvatar(url);
    }

    @Override
    public String getRobotAvatar() {
        return mPreferencesHelper.getRobotAvatar();
    }

    @Override
    public String getRobotAuthToken() {
        return mPreferencesHelper.getRobotAuthToken();
    }

    @Override
    public void setRobotAuthToken(String token) {
        mPreferencesHelper.setRobotAuthToken(token);
    }

    @Override
    public String getRobotTokenExpireTime() {
        return mPreferencesHelper.getRobotTokenExpireTime();
    }

    @Override
    public void setRobotTokenExpireTime(String date) {
        mPreferencesHelper.setRobotTokenExpireTime(date);
    }

    @Override
    public String getRobotRefreshToken() {
        return mPreferencesHelper.getRobotRefreshToken();
    }

    @Override
    public void setRobotRefreshToken(String token) {
        mPreferencesHelper.setRobotRefreshToken(token);
    }

    @Override
    public String getRobotMqttUsername() {
        return mPreferencesHelper.getRobotMqttUsername();
    }

    @Override
    public void setRobotMqttUsername(String username) {
        mPreferencesHelper.setRobotMqttUsername(username);
    }

    @Override
    public String getRobotMqttPassword() {
        return mPreferencesHelper.getRobotMqttPassword();
    }

    @Override
    public void setRobotMqttPassword(String password) {
        mPreferencesHelper.setRobotMqttPassword(password);
    }

    @Override
    public boolean getRobotNightModeSwitch() {
        return mPreferencesHelper.getRobotNightModeSwitch();
    }

    @Override
    public void setRobotNigthModeSwitch(boolean s) {
        mPreferencesHelper.setRobotNigthModeSwitch(s);
    }

    @Override
    public String getRobotNightModeStartTime() {
        return mPreferencesHelper.getRobotNightModeStartTime();
    }

    @Override
    public void setRobotNightModeStartTime(String start) {
        mPreferencesHelper.setRobotNightModeStartTime(start);
    }

    @Override
    public String getRobotNightModeEndTime() {
        return mPreferencesHelper.getRobotNightModeEndTime();
    }

    @Override
    public void setRobotNightModeEndTime(String end) {
        mPreferencesHelper.setRobotNightModeEndTime(end);
    }

    @Override
    public boolean getRobotAntiAddictionSwitch() {
        return mPreferencesHelper.getRobotAntiAddictionSwitch();
    }

    @Override
    public void setRobotAntiAddictionSwitch(boolean s) {
        mPreferencesHelper.setRobotAntiAddictionSwitch(s);
    }

    @Override
    public int getRobotAntiAddictionDuration() {
        return mPreferencesHelper.getRobotAntiAddictionDuration();
    }

    @Override
    public void setRobotAntiAddictionDuration(int duration) {
        mPreferencesHelper.setRobotAntiAddictionDuration(duration);
    }

    @Override
    public int getRobotAntiAddictionRestDuration() {
        return mPreferencesHelper.getRobotAntiAddictionRestDuration();
    }

    @Override
    public void setRobotAntiAddictionRestDuration(int duration) {
        mPreferencesHelper.setRobotAntiAddictionRestDuration(duration);
    }

    @Override
    public String getRobotCode() {
        return mPreferencesHelper.getRobotCode();
    }

    @Override
    public void setRobotCode(String code) {
        mPreferencesHelper.setRobotCode(code);
    }

    @Override
    public boolean getListenAchievementRecordUploadSwitch() {
        return mPreferencesHelper.getListenAchievementRecordUploadSwitch();
    }

    @Override
    public void setListenAchievementRecordUploadSwitch(boolean s) {
        mPreferencesHelper.setListenAchievementRecordUploadSwitch(s);
    }

    @Override
    public int getBatteryCapacity() {
        return mPreferencesHelper.getBatteryCapacity();
    }

    @Override
    public int getStreamMusicVolume() {
        return mPreferencesHelper.getStreamMusicVolume();
    }

    @Override
    public void RaiseStreamMusicVolume() {
        mPreferencesHelper.RaiseStreamMusicVolume();
    }

    @Override
    public void LowerStreamMusicVolume() {
        mPreferencesHelper.LowerStreamMusicVolume();
    }


}
