package ai.aistem.xbot.framework.data.db.impl;


import android.content.ContentValues;

import org.litepal.LitePal;

import java.util.List;

import ai.aistem.xbot.framework.data.db.entities.TddeMusicInfo;

public class TddeMusicInfoImpl {

    public static synchronized void modify(TddeMusicInfo musicInfo) {

        if (checkExistForTddeMusic(musicInfo.getMid())) {
            ContentValues values = new ContentValues();
            values.put("albumId", musicInfo.getAlbumId());
            values.put("title", musicInfo.getTitle());
            values.put("cover", musicInfo.getCover());
            values.put("duration", musicInfo.getDuration());
            values.put("attach", musicInfo.getAttach());
            values.put("order", musicInfo.getOrder());
            //收藏优先级：本地收藏高于手机收藏（当本地单曲收藏标志置1时，忽略手机是否收藏,当置0时,判断手机是否收藏）
            boolean isCollected = checkCollected(musicInfo.getMid());
            if (!isCollected) isCollected = musicInfo.isCollected();
            values.put("collected", isCollected);
            values.put("updateTime", System.currentTimeMillis());
            values.put("tags", musicInfo.getTags());
            LitePal.updateAll(TddeMusicInfo.class, values, "mid=?",
                    String.valueOf(musicInfo.getMid()));
        } else {
            musicInfo.setUpdateTime(System.currentTimeMillis());
            musicInfo.setCreateTime(System.currentTimeMillis());
            musicInfo.save();
        }
    }

    public static synchronized void delete(TddeMusicInfo musicInfo) {
        ContentValues values = new ContentValues();
        values.put("deleteTime", System.currentTimeMillis());
        LitePal.updateAll(TddeMusicInfo.class, values, "mid=?",
                String.valueOf(musicInfo.getMid()));
    }

    public static synchronized List<TddeMusicInfo> selectCollectedMusics() {
        return LitePal.where("collected=?", "1")
                .order("updateTime desc").find(TddeMusicInfo.class);
    }

    public static synchronized List<TddeMusicInfo> getMusicsByTag(int tagId) {
        return LitePal.where("(',' || tags || ',') LIKE ?", "%" + tagId + "%")
                .order("updateTime desc").find(TddeMusicInfo.class);
    }

    /**
     * 判断单曲mid是否为收藏歌曲
     *
     * @param mid 服务器单曲id
     * @return
     */
    public static synchronized boolean checkCollected(int mid) {
        List<TddeMusicInfo> list = LitePal.where("mid=? and collected=?",
                String.valueOf(mid), "1").find(TddeMusicInfo.class);
        if (list != null && list.size() > 0) {
            return true;
        }
        return false;
    }

    public static synchronized boolean setCollected(int mid, boolean set) {
        ContentValues values = new ContentValues();
        if (set) {
            values.put("collected", "1");
        } else {
            values.put("collected", "0");
        }
        LitePal.updateAll(TddeMusicInfo.class, values, "mid=?", String.valueOf(mid));
        return true;
    }


    private static boolean checkExistForTddeMusic(int id) {
        List<TddeMusicInfo> list = LitePal.where("mid=?",
                String.valueOf(id)).find(TddeMusicInfo.class);
        if (list != null && list.size() > 0) {
            return true;
        }
        return false;
    }
}
