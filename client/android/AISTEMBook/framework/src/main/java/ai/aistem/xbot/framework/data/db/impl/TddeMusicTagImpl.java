package ai.aistem.xbot.framework.data.db.impl;


import android.content.ContentValues;

import org.litepal.LitePal;

import java.util.List;

import ai.aistem.xbot.framework.data.db.entities.TddeMusicTag;

public class TddeMusicTagImpl {

    public static synchronized void modify(TddeMusicTag musicTag) {

        if (checkExistForTddeMusicTag(musicTag.getTid())) {
            ContentValues values = new ContentValues();
            values.put("title", musicTag.getTitle());
            values.put("updateTime", System.currentTimeMillis());
            LitePal.updateAll(TddeMusicTag.class, values, "tid=?",
                    String.valueOf(musicTag.getTid()));
        } else {
            musicTag.setUpdateTime(System.currentTimeMillis());
            musicTag.setCreateTime(System.currentTimeMillis());
            musicTag.save();
        }
    }

    public static synchronized void delete(TddeMusicTag musicTag) {
        ContentValues values = new ContentValues();
        values.put("deleteTime", System.currentTimeMillis());
        LitePal.updateAll(TddeMusicTag.class, values, "tid=?",
                String.valueOf(musicTag.getTid()));
    }

    private static boolean checkExistForTddeMusicTag(int tid) {
        List<TddeMusicTag> list = LitePal.where("tid=?",
                String.valueOf(tid)).find(TddeMusicTag.class);
        return list != null && list.size() > 0;
    }
}
