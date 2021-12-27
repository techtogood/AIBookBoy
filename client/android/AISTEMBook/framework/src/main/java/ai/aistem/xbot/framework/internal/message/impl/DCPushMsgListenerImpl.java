package ai.aistem.xbot.framework.internal.message.impl;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;

import ai.aistem.xbot.framework.internal.http.HttpApiHelper;
import ai.aistem.xbot.framework.internal.message.listener.DCPushMsgListener;
import ai.aistem.xbot.framework.internal.mqtt.model.ReceivedMessageContent;
import ai.aistem.xbot.framework.internal.utils.SoundPlayer;
import ai.aistem.xbot.framework.message.DCBroadcastMsgImpl;


/**
 * @author: aistem
 * @created: 2018/6/11/11:23
 * @desc: 处理一些推送消息，如语音聊天，磨耳朵中的音频推送等等
 */
public class DCPushMsgListenerImpl implements DCPushMsgListener {

    public static final String[] faceIds = {
            "10102001",
            "10102002",
            "10102003",
            "10102004",
            "10102005",
            "10102006",
            "10102007",
            "10102008",
            "10102009",
            "10102010",
            "10102011",
            "10102012",
            "10102013",
            "10102014",
            "10102015",
            "10102016"
    };

    public static final String[] faceSoundIds = {
            "20102001",
            "20102002",
            "20102003",
            "20102004",
            "20102005",
            "20102006",
            "20102007",
            "20102008",
            "20102009",
            "20102010",
            "20102011",
            "20102012",
            "20102013",
            "20102014",
            "20102015",
            "20102016"
    };


    @Override
    public void playSoundCallBack(String soundMsg) {
        ReceivedMessageContent content = JSON.parseObject(soundMsg, ReceivedMessageContent.class);
        ArrayList<Integer> ids = content.getIds();

        //如果推送的是单曲则马上播放
        if (ids.size() == 1) {
            HttpApiHelper.getInstance().doRobotSoundVoiceApiGet(String.valueOf(ids.get(0)));
        } else if (ids.size() > 1) {
            int[] voice_ids = new int[ids.size()];
            for (int i = 0; i < ids.size(); i++) {
                voice_ids[i] = ids.get(i);
            }
            HttpApiHelper.getInstance().doRobotSoundVoicesApiPut(voice_ids);
        }
    }

    @Override
    public void playAlbumCallBack(String albumMsg) {
        ReceivedMessageContent content = JSON.parseObject(albumMsg, ReceivedMessageContent.class);
        int album_id = content.getId();
        if (album_id > 0)
            HttpApiHelper.getInstance().doRobotSoundAlbumApiGet(String.valueOf(album_id));
    }

    @Override
    public void addBookCallBack(String bookMsg) {

    }

    @Override
    public void chatTextCallBack(String textMsg) {

    }

    @Override
    public void chatFaceCallBack(String faceMsg) {
        for (int i = 0; i < faceIds.length; i++) {
            if (faceIds[i].equals(faceMsg)) {
                String faceSoundId = faceSoundIds[i];
                SoundPlayer.getInstance().play(faceSoundId);
                int duration = SoundPlayer.getInstance().playDuration(faceSoundId);
                DCBroadcastMsgImpl.sendChatFaceBroadcast(duration);

                /*MediaManager.playSound(path,
                        new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {

                            }
                        },
                        new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                int duration = mp.getDuration() / 1000;
                                DCBroadcastMsgImpl.sendChatFaceBroadcast(duration);
                            }
                        });*/
            }
        }
    }

    @Override
    public void chatSoundCallBack(String chatSoundMsg) {

    }
}
