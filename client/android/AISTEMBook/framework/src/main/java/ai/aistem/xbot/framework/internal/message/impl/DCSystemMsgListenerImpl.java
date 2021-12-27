package ai.aistem.xbot.framework.internal.message.impl;

import com.alibaba.fastjson.JSON;
import com.litesuits.common.io.FileUtils;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import ai.aistem.xbot.framework.data.db.model.PairUser;
import ai.aistem.xbot.framework.internal.message.listener.DCSystemMsgListener;
import ai.aistem.xbot.framework.internal.mqtt.model.ReceivedMessageContent;
import ai.aistem.xbot.framework.message.DCBroadcastMsgImpl;
import ai.aistem.xbot.framework.utils.LogUtil;

/**
 * @author: aistem
 * @created: 2018/6/11/14:19
 * @desc: DCSystemMsgListenerImpl
 */
public class DCSystemMsgListenerImpl implements DCSystemMsgListener {
    @Override
    public void aliveCheckCallBack(String aliveMsg) {

    }

    @Override
    public void bindPhoneUserCallBack(String userMsg) {
        //TODO:
        LogUtil.getInstance().d("DCSystemMsgListenerImpl", "userMsg : " + userMsg);

        //And by aistem
        //DCBroadcastMsgImpl.sendVoiceControlBroadcast("BIND_USER");
        DCBroadcastMsgImpl.sendBindUserBroadcast("BIND_USER");
    }

    @Override
    public void unBindPhoneUserCallBack(String unBindMsg) {
        //TODO:
        LogUtil.getInstance().d("DCSystemMsgListenerImpl", "unBindMsg : " + unBindMsg);

        //And by aistem
        DCBroadcastMsgImpl.sendBindUserBroadcast("UNBIND_USER");
    }

    @Override
    public void setNightModeCallBack(String setNightMsg) {
        LogUtil.getInstance().d("DCSystemMsgListenerImpl", "setNightMsg : " + setNightMsg);
        //TODO:从SP中读取夜间模式的值,设置开启/关闭,开始闹铃,结束闹铃


    }

    @Override
    public void closeNightModeCallBack(String closeNightMsg) {
        LogUtil.getInstance().d("DCSystemMsgListenerImpl", "closeNightMsg : " + closeNightMsg);
        //TODO:
    }

    @Override
    public void setAntiAddictionCallBack(String setAntiAddMsg) {

    }

    @Override
    public void closeAntiAddictionCallBack(String closeAntiAddMsg) {

    }

    @Override
    public void updateUserCallBack(String userMsg) {
        //保存用户头像至本地
        final ReceivedMessageContent content = JSON.parseObject(userMsg, ReceivedMessageContent.class);
        String avatar = content.getAvatar();
        final String uid = content.getUid();
        if (!avatar.isEmpty()) {
            try {
                final URL url = new URL(avatar);
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            List<PairUser> users = LitePal.where("uid=?", uid).find(PairUser.class);
                            for (PairUser user : users) {
                                String filename = user.getPhoto();
                                if (FileUtils.getFile(filename).exists())
                                    FileUtils.deleteQuietly(FileUtils.getFile(filename));
                                final File newfile = new File(filename);
                                FileUtils.copyURLToFile(url, newfile);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
