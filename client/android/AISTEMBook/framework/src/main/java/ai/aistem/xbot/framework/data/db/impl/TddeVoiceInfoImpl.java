package ai.aistem.xbot.framework.data.db.impl;

import android.content.ContentValues;

import org.litepal.LitePal;

import java.util.List;

import ai.aistem.xbot.framework.data.db.entities.TddeVoiceInfo;
import ai.aistem.xbot.framework.data.db.entities.TddeVoiceInfo2;

/**
 *
 * @author: LiQi
 * @created: 2018/6/27/10:19
 * @desc: TddeVoiceInfoImpl
 */

public class TddeVoiceInfoImpl {

    public static synchronized void modifyTddeVoiceInfo(TddeVoiceInfo voiceInfo){

        if(checkExistForAbbyVoice(voiceInfo.getFileName())){
            ContentValues values = new ContentValues();

            values.put("userId",voiceInfo.getUserId());
            values.put("username",voiceInfo.getUsername());
            values.put("relationshipId",voiceInfo.getRelationshipId());
            values.put("isRead",voiceInfo.isRead());
            values.put("isUpload",voiceInfo.isUpload());
            values.put("isDownload",voiceInfo.isDownload());
            values.put("duration",voiceInfo.getDuration());
            values.put("path",voiceInfo.getPath());
            values.put("fileName",voiceInfo.getFileName());
            LitePal.updateAll(TddeVoiceInfo.class,values,"fileName=?",voiceInfo.getFileName());
        }else {
            voiceInfo.save();
        }

    }


    public static synchronized void updataTddeVoiceInfo (TddeVoiceInfo voiceInfo){

            if(checkExistForAbbyVoice(voiceInfo.getFileName())) {
                voiceInfo.updateAll("fileName=?", voiceInfo.getFileName());
            }

    }

    public static int getUnreadVoiceNum(String uid) {
        int num = 0;
        num = LitePal.where("userid=? and isread=0",uid).count(TddeVoiceInfo2.class);
        return num;
    }
    public static boolean checkExistForAbbyVoice(String fileName){
        List<TddeVoiceInfo> voiceList= LitePal.where("fileName=?",fileName).find(TddeVoiceInfo.class);

        if(voiceList!=null&&voiceList.size()>0){
            return true;
        }
        return false;
    }

}
