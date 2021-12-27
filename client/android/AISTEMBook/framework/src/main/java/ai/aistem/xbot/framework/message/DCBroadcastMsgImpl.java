package ai.aistem.xbot.framework.message;

import android.content.Intent;

import ai.aistem.xbot.framework.application.DCApplication;
import ai.aistem.xbot.framework.data.bean.TrackBean;
import ai.aistem.xbot.framework.data.bean.TrackInfo;
import ai.aistem.xbot.framework.data.db.entities.TddeVoiceInfo2;
import ai.aistem.xbot.framework.utils.LogUtil;

/**
 * @author: aistem
 * @created: 2018/5/30/10:58
 * @desc: DCBroadcastMsgImpl
 */
public class DCBroadcastMsgImpl implements DCMessage {

    public static void sendVoiceControlBroadcast(String command) {
        LogUtil.getInstance().d("发送语音命令广播---" + command);
        Intent intent = new Intent(ACTION_VOICE_CONTROL_COMMAND);
        intent.putExtra(EXTRA_VOICE_CONTROL_COMMAND, command);
        DCApplication.app.sendBroadcast(intent);
    }


    public static void sendChatMsgBroadcast(String chatMsg) {
        Intent intent = new Intent(ACTION_CHAT_MAG_COMMAND);
        intent.putExtra(EXTRA_CHAT_MAG_COMMAND, chatMsg);
        DCApplication.app.sendBroadcast(intent);
    }

    public static void sendChatMsgBroadcast(TddeVoiceInfo2 info) {
        LogUtil.getInstance().d("发送对讲机语音广播");
        Intent intent = new Intent(ACTION_CHAT_MAG_COMMAND);
        intent.putExtra(EXTRA_CHAT_VOICE_MAG_COMMAND, info);
        DCApplication.app.sendBroadcast(intent);
    }

    /**
     * 随机闲聊表情标识广播
     *
     * @param faceMsg 表情标识+表情时长
     */
    public static void sendSelfTalkFaceMsgBroadcast(String[] faceMsg) {
        LogUtil.getInstance().d("随机闲聊表情标识广播");
        Intent intent = new Intent(ACTION_SELF_TALK_FACEID_COMMAND);
        intent.putExtra(EXTRA_SELF_TALK_FACEID_COMMAND, faceMsg);
        DCApplication.app.sendBroadcast(intent);
    }

    /**
     * MQTT Client广播
     *
     * @param mqttStatus "CONNECTED","DISCONNECTED"
     */
    public static void sendMqttClientBroadcast(String mqttStatus) {
        LogUtil.getInstance().d("MQTT客户端广播");
        Intent intent = new Intent(ACTION_MQTT_CLIENT_COMMAND);
        intent.putExtra(EXTRA_MQTT_CLIENT_COMMAND, mqttStatus);
        DCApplication.app.sendBroadcast(intent);
    }


    /**
     * 磨耳朵单曲推送广播
     *
     * @param trackBean
     */
    public static void sendSoundVoiceBroadcast(TrackBean trackBean) {
        LogUtil.getInstance().d("磨耳朵单曲推送广播");
        Intent intent = new Intent(ACTION_SOUND_VOICE_COMMAND);
        intent.putExtra(EXTRA_SOUND_VOICE_COMMAND, trackBean);
        DCApplication.app.sendBroadcast(intent);
    }

    /**
     * 磨耳朵专辑推送广播
     *
     * @param trackInfo
     */
    public static void sendSoundAlbumBroadcast(TrackInfo trackInfo) {
        LogUtil.getInstance().d("磨耳朵专辑推送广播");
        Intent intent = new Intent(ACTION_SOUND_ALBUM_COMMAND);
        intent.putExtra(EXTRA_SOUND_ALBUM_COMMAND, trackInfo);
        DCApplication.app.sendBroadcast(intent);
    }

    /**
     * 网络音频资源列表广播
     *
     * @param trackInfo
     */
    public static void sendNetPlayListBroadcast(TrackInfo trackInfo) {
        LogUtil.getInstance().d("网络音频资源列表广播");
        Intent intent = new Intent(ACTION_NET_PLAYLIST_COMMAND);
        intent.putExtra(EXTRA_NET_PLAYLIST_COMMAND, trackInfo);
        DCApplication.app.sendBroadcast(intent);
    }


    /**
     * 蜜语文字广播
     *
     * @param text
     */
    public static void sendChatTextBroadcast(String text) {
        LogUtil.getInstance().d("蜜语文字广播" + text);
        Intent intent = new Intent(ACTION_CHAT_TEXT_COMMAND);
        intent.putExtra(EXTRA_CHAT_TEXT_COMMAND, text);
        DCApplication.app.sendBroadcast(intent);
    }

    /**
     * 蜜语表情广播
     *
     * @param face
     */
    public static void sendChatFaceBroadcast(String face) {
        LogUtil.getInstance().d("蜜语表情广播");
        Intent intent = new Intent(ACTION_CHAT_FACE_COMMAND);
        intent.putExtra(EXTRA_CHAT_FACE_COMMAND, face);
        DCApplication.app.sendBroadcast(intent);
    }

    public static void sendChatFaceBroadcast(int duration) {
        LogUtil.getInstance().d("蜜语表情时长广播：" + duration + "秒");
        Intent intent = new Intent(ACTION_CHAT_FACE_COMMAND);
        intent.putExtra(EXTRA_CHAT_FACE_COMMAND, duration);
        DCApplication.app.sendBroadcast(intent);
    }

    public static void sendChatDurationBroadcast(int duration) {
        LogUtil.getInstance().d("闲聊表情时长广播：" + duration + "秒");
        Intent intent = new Intent(ACTION_CHAT_DURATION_COMMAND);
        intent.putExtra(EXTRA_CHAT_DURATION_COMMAND, duration);
        DCApplication.app.sendBroadcast(intent);
    }

    /**
     * 绑定/解绑用户广播
     *
     * @param bindMsg "UNBIND_USER","BIND_USER"
     */
    public static void sendBindUserBroadcast(String bindMsg) {
        LogUtil.getInstance().d("绑定/解绑用户广播");
        Intent intent = new Intent(ACTION_BIND_USER_COMMAND);
        intent.putExtra(EXTRA_BIND_USER_COMMAND, bindMsg);
        DCApplication.app.sendBroadcast(intent);
    }

    /**
     * 设置夜间模式广播
     * MQTT客户端接收到night事件后发送该广播
     *
     * @param nightModel "ON","OFF"
     */
    public static void sendSetNightBroadcast(String[] nightModel) {
        LogUtil.getInstance().d("设置夜间模式广播");
        Intent intent = new Intent(ACTION_SET_NIGHT_COMMAND);
        intent.putExtra(EXTRA_SET_NIGHT_COMMAND, nightModel);
        DCApplication.app.sendBroadcast(intent);
    }

    public static void sendSetNightBroadcast(String nightModels) {
        LogUtil.getInstance().d("设置夜间模式广播");
        Intent intent = new Intent(ACTION_SET_NIGHT_COMMAND);
        intent.putExtra(EXTRA_SET_NIGHT_COMMAND, nightModels);
        DCApplication.app.sendBroadcast(intent);
    }

    /**
     * 防沉迷设置广播
     *
     * @param antiModel "ON","OFF"
     */

    public static void sendSetAntiAddictionBroadcast(String antiModel) {
        LogUtil.getInstance().d("设置防沉迷广播");
        Intent intent = new Intent(ACTION_SET_ANTI_ADDICTION_COMMAND);
        intent.putExtra(EXTRA_SET_ANTI_ADDICTION_COMMAND, antiModel);
        DCApplication.app.sendBroadcast(intent);
    }

    public static void sendSetAntiAddictionBroadcast(String[] antiModels) {
        LogUtil.getInstance().d("设置防沉迷广播");
        Intent intent = new Intent(ACTION_SET_ANTI_ADDICTION_COMMAND);
        intent.putExtra(EXTRA_SET_ANTI_ADDICTION_COMMAND, antiModels);
        DCApplication.app.sendBroadcast(intent);
    }

    /**
     * 更新SharedPreferences广播
     *
     * @param preference 更新的参数
     */
    public static void sendUpdateSPBroadcast(String preference) {
        LogUtil.getInstance().d("更新SharedPreferences广播");
        Intent intent = new Intent(ACTION_UPDATE_SP_COMMAND);
        intent.putExtra(EXTRA_UPDATE_SP_COMMAND, preference);
        DCApplication.app.sendBroadcast(intent);
    }

    /**
     * 更新SharedPreferences广播
     *
     * @param preferences 更新的参数列表
     */
    public static void sendUpdateSPBroadcast(String[] preferences) {
        LogUtil.getInstance().d("更新SharedPreferences广播");
        Intent intent = new Intent(ACTION_UPDATE_SP_COMMAND);
        intent.putExtra(EXTRA_UPDATE_SP_COMMAND, preferences);
        DCApplication.app.sendBroadcast(intent);
    }

    /**
     * 无操作超时广播
     *
     */
    public static void sendNOPTimeoutBroadcast() {
        LogUtil.getInstance().d("无操作超时广播");
        Intent intent = new Intent(ACTION_NOP_TIMEOUT_COMMAND);
        DCApplication.app.sendBroadcast(intent);
    }

    /*============================================================================================*/

    /**
     * 封装Night Intent
     *
     * @param alarm "START","END"
     * @return intent
     */
    public static Intent getNightAlarmIntent(String alarm) {
        Intent intent = new Intent(ACTION_NIGHT_ALARM_COMMAND);
        intent.putExtra(EXTRA_NIGHT_ALARM_COMMAND, alarm);
        return intent;
    }
    /*============================================================================================*/

    /**
     * 夜间状态广播
     * 状态机进入/退出夜间状态时发送该广播
     *
     * @param state "ENTER","EXIT"
     */
    public static void sendNightStateBroadcast(String state) {
        LogUtil.getInstance().d("进入/退出夜间状态广播---" + state);
        Intent intent = new Intent(ACTION_NIGHT_STATE_COMMAND);
        intent.putExtra(EXTRA_NIGHT_STATE_COMMAND, state);
        DCApplication.app.sendBroadcast(intent);
    }


    /**
     * 夜间状态广播
     * 退出夜间状态时发送该广播
     */
    public static void sendNightStateFinishBroadcast() {
        LogUtil.getInstance().d("退出夜间状态广播");
        Intent intent = new Intent(ACTION_NIGHT_STATE_FINISH_COMMAND);
        DCApplication.app.sendBroadcast(intent);
    }

    /**
     * 防沉迷状态广播
     * 状态机进入/退出防沉迷状态时发送该广播
     *
     * @param state "ENTER","EXIT"
     */

    public static void sendAntiAddiStateBroadcast(String state) {
        LogUtil.getInstance().d("进入/退出防沉迷状态广播---" + state);
        Intent intent = new Intent(ACTION_PRE_ANTI_ADDICTION_STATE_COMMAND);
        intent.putExtra(EXTRA_PRE_ANTI_ADDICTION_STATE_COMMAND, state);
        DCApplication.app.sendBroadcast(intent);
    }

    /**
     * 防沉迷准备开始和准备结束广播
     * 状态机准备进入/退出防沉迷状态时发送该广播
     *
     * @param state "ENTER" - 进入防沉迷前1分钟广播
     *              "EXIT"  - 退出防沉迷前30秒广播
     */
    public static void sendPreAntiAddiStateBroadcast(String state) {
        LogUtil.getInstance().d("进入/退出防沉迷状态广播---" + state);
        Intent intent = new Intent(ACTION_ANTI_ADDICTION_STATE_COMMAND);
        intent.putExtra(EXTRA_ANTI_ADDICTION_STATE_COMMAND, state);
        DCApplication.app.sendBroadcast(intent);
    }

    /**
     * 休眠状态广播
     * 状态机进入/退出休眠状态时发送该广播
     *
     * @param state "ENTER","EXIT"
     */
    public static void sendSleepStateBroadcast(String state) {
        LogUtil.getInstance().d("进入/退出休眠状态广播---" + state);
        Intent intent = new Intent(ACTION_SLEEP_STATE_COMMAND);
        intent.putExtra(EXTRA_SLEEP_STATE_COMMAND, state);
        DCApplication.app.sendBroadcast(intent);
    }

    /**
     * 自言自语状态广播
     * 状态机进入/退出聊天状态时发送该广播
     *
     * @param state "ENTER","EXIT"
     */
    public static void sendSelfTalkStateBroadcast(String state) {
        LogUtil.getInstance().d("进入/退出闲聊状态广播---" + state);
        Intent intent = new Intent(ACTION_SELF_TALK_STATE_COMMAND);
        intent.putExtra(EXTRA_SELF_TALK_STATE_COMMAND, state);
        DCApplication.app.sendBroadcast(intent);
    }

    /**
     * 聊天状态广播
     * 状态机进入/退出聊天状态时发送该广播
     *
     * @param state "ENTER","EXIT"
     */
    public static void sendChatStateBroadcast(String state) {
        LogUtil.getInstance().d("进入/退出聊天状态广播---" + state);
        Intent intent = new Intent(ACTION_CHAT_STATE_COMMAND);
        intent.putExtra(EXTRA_CHAT_STATE_COMMAND, state);
        DCApplication.app.sendBroadcast(intent);
    }

    /**
     * 基础状态广播
     * 状态机进入/退出基础状态时发送该广播
     *
     * @param state "ENTER","EXIT"
     */
    public static void sendBaseStateBroadcast(String state) {
        LogUtil.getInstance().d("进入/退出基础状态广播---" + state);
        Intent intent = new Intent(ACTION_BASE_STATE_COMMAND);
        intent.putExtra(EXTRA_BASE_STATE_COMMAND, state);
        DCApplication.app.sendBroadcast(intent);
    }

    /**
     * 工作状态广播
     * 状态机进入/退出工作状态时发送该广播
     *
     * @param state "ENTER","EXIT"
     */
    public static void sendWorkStateBroadcast(String state) {
        LogUtil.getInstance().d("进入/退出工作状态广播---" + state);
        Intent intent = new Intent(ACTION_WORK_STATE_COMMAND);
        intent.putExtra(EXTRA_WORK_STATE_COMMAND, state);
        DCApplication.app.sendBroadcast(intent);
    }
}
