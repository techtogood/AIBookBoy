package ai.aistem.xbot.framework.internal.message.impl;

import android.os.Environment;

import com.squareup.okhttp.Request;

import org.json.JSONException;
import org.json.JSONObject;

import ai.aistem.xbot.framework.data.db.entities.TddeVoiceInfo;
import ai.aistem.xbot.framework.data.db.entities.TddeVoiceInfo2;
import ai.aistem.xbot.framework.data.db.impl.TddeVoiceInfoImpl;
import ai.aistem.xbot.framework.data.db.impl.TddeVoiceInfoImpl2;
import ai.aistem.xbot.framework.helper.NotificationHelper;
import ai.aistem.xbot.framework.internal.message.listener.DCChatMsgListener;
import ai.aistem.xbot.framework.message.DCBroadcastMsgImpl;
import ai.aistem.xbot.framework.network.OkHttpClientManagerNoPrivateSSL;
import ai.aistem.xbot.framework.utils.LogUtil;
import ai.aistem.xbot.framework.utils.SPUtils;
import ai.aistem.xbot.framework.utils.UUIDUtil;

/**
 * @author: aistem
 * @created: 2018/6/27/11:35
 * @desc: DCChatMsgListenerImpl
 */
public class DCChatMsgListenerImpl implements DCChatMsgListener {
    @Override
    public void receiverUserVoice(String voiceInfo) {
        LogUtil.getInstance().d("DCChatMsgListenerImpl","voiceInfo : "+voiceInfo);

        /**
         * duration:时长
         * uid：用户ID
         * url：语音下载路径
         * */

        if(voiceInfo!=null){
            try {
                TddeVoiceInfo2 tddeVoiceInfo =new TddeVoiceInfo2();

//                PhoneVoiceMsg voiceMsg=new PhoneVoiceMsg();


                JSONObject object=new JSONObject(voiceInfo);
//                if (!object.isNull("uid")){
//                    String uid=object.getString("uid");
//                    tddeVoiceInfo.setUserId(Integer.parseInt(uid));
//                }
//
//                if (!object.isNull("duration")){
//                    double duration=object.getDouble("duration");
//
//                    tddeVoiceInfo.setDuration((float) duration);
//                }
//
//                if (!object.isNull("url")){
//                    String url=object.getString("url");
//                    tddeVoiceInfo.setPath(url);
//                }

//                tddeVoiceInfo.setRead(false);

                //1，下载语音 2，通知UI接收到新消息
                String dir = Environment.getExternalStorageDirectory() + "/test_recorder_audios";
                //  Add by aistem
                // 优化语音数据库表
                TddeVoiceInfo2 tddeVoiceInfo2 =new TddeVoiceInfo2();
                if (!object.isNull("uid")) {
                    tddeVoiceInfo2.setUserId(Integer.valueOf(object.getString("uid")));
                }
                if (!object.isNull("duration")){
                    int duration=object.getInt("duration");

                    tddeVoiceInfo2.setDuration((float) duration);
                }

                if (!object.isNull("url")){
                    String url=object.getString("url");
                    tddeVoiceInfo2.setUrl(url);
                }

                tddeVoiceInfo2.setRead(false);
                tddeVoiceInfo2.setType(1);
                tddeVoiceInfo2.setPath(dir+"/"+getFileName(tddeVoiceInfo2.getUrl()));
                tddeVoiceInfo2.setFileName(getFileName(tddeVoiceInfo2.getUrl()));
                modifyUserVoiceInfo2(tddeVoiceInfo2);

                downloadVoice(tddeVoiceInfo2,dir);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }


    private void downloadVoice(final TddeVoiceInfo2 tddeVoiceInfo, final String path){
        OkHttpClientManagerNoPrivateSSL.getDownloadDelegate().downloadAsyn(tddeVoiceInfo.getUrl(), path, new OkHttpClientManagerNoPrivateSSL.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                LogUtil.getInstance().d("DCChatMsgListenerImpl","Exception : "+e.toString());
            }

            @Override
            public void onResponse(String response) {
                LogUtil.getInstance().d("DCChatMsgListenerImpl","response : "+getFileName(response));

                tddeVoiceInfo.setPath(response);
                tddeVoiceInfo.setFileName(getFileName(response));
                tddeVoiceInfo.setType(1);
                modifyUserVoiceInfo2(tddeVoiceInfo);
                SPUtils.getInstance().saveUserId(tddeVoiceInfo.getUserId());
                NotificationHelper.getInstance().playNotiSound();
                DCBroadcastMsgImpl.sendChatMsgBroadcast(tddeVoiceInfo);
            }
        });
    }






    private String getFileName(String path)
    {
        int separatorIndex = path.lastIndexOf("/");
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
    }


    private void modifyUserVoiceInfo( TddeVoiceInfo voiceInfo){
        voiceInfo.setRelationshipId("Tdde_"+ UUIDUtil.getMacAddress()+voiceInfo.getUserId());
        voiceInfo.setUsername("");
        //voiceInfo.setUsername("baba");
        voiceInfo.setUpload(true);
        voiceInfo.setRead(false);
        voiceInfo.setDownload(true);
        voiceInfo.setCreateTime(System.currentTimeMillis());
        TddeVoiceInfoImpl.modifyTddeVoiceInfo(voiceInfo);
    }

    private void modifyUserVoiceInfo2( TddeVoiceInfo2 voiceInfo2){
        //voiceInfo.setRelationshipId("Tdde_"+ UUIDUtil.getMacAddress()+voiceInfo.getUserId());
        //voiceInfo.setUsername("baba");
        voiceInfo2.setUpload(true);
        voiceInfo2.setRead(false);
        voiceInfo2.setDownload(true);
        voiceInfo2.setCreateTime(System.currentTimeMillis());
        TddeVoiceInfoImpl2.modifyTddeVoiceInfo(voiceInfo2);
    }
}
