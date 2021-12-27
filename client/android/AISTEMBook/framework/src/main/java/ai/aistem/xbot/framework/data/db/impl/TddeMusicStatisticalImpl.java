package ai.aistem.xbot.framework.data.db.impl;

import org.litepal.LitePal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ai.aistem.xbot.framework.data.db.entities.TddeMusicStatisticalInfo;

public class TddeMusicStatisticalImpl {

    /**
     * @param mid  歌曲的ID
     * 说明:将该ID对应的歌曲的播放次数加一  如果不存在插入该歌曲到数据库
     */
    public static synchronized void updateMusicCount(int mid)
    {
//        TddeMusicStatisticalInfo musicInfo =  LitePal.find(TddeMusicStatisticalInfo.class,mid);
        List<TddeMusicStatisticalInfo> MusicList = LitePal.where("mid = ?",String.valueOf(mid)).find(TddeMusicStatisticalInfo.class);
        TddeMusicStatisticalInfo musicInfo = null;
        if(MusicList.size() > 0)
        {
            musicInfo = MusicList.get(0);
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String UpdateTime = df.format(new Date());
        if(null == musicInfo)
        {
            musicInfo = new TddeMusicStatisticalInfo();
            musicInfo.setMid(mid);
            musicInfo.setToday(1);
            musicInfo.setAllCount(1);
            musicInfo.setYDay("0|0|0|0|0|0");
            musicInfo.setUpdateTime(UpdateTime);
            musicInfo.save();
        }
        else
        {
            if(UpdateTime.equals(musicInfo.getUpdateTime()))
            {
                musicInfo.addToday();
            }
            else
            {
                //计算以下上一次更新到本次更新中间隔了几天
                long Days = 0;
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Date oldDate;
                    Date newDate;
                    newDate = format.parse(UpdateTime);
                    oldDate = format.parse(musicInfo.getUpdateTime());
                    Days = (newDate.getTime() - oldDate.getTime())/(24*60*60*1000);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                for(int i=0; i<Days; i++)
                {
                    musicInfo.aNewDay(UpdateTime);
                }
                musicInfo.addToday();
            }
            //计算七天播放的所有次数
            String[] counts = musicInfo.getYDay().split("\\|");
            int Count = musicInfo.getToday();
            for(int i=0; i<counts.length; i++)
            {
                Count += Integer.valueOf(counts[i]);
            }
            musicInfo.setAllCount(Count);

//            musicInfo.update(mid);
            musicInfo.saveOrUpdate("mid = ?",String.valueOf(mid));
        }
    }

    /**
     * @return  音乐中播放次数最多的三首歌   mid|mid|mid  如果没有三首  用0填充
     */
    public static synchronized String selectMaxCountMusic()
    {
        String result = "";
        List<TddeMusicStatisticalInfo> selectResult = LitePal.select("mid").order("AllCount desc").limit(3).find(TddeMusicStatisticalInfo.class);
        if(selectResult.size() < 1)
        {
            result = "0|0|0";
        }
        else if(selectResult.size() == 1)
        {
            result = selectResult.get(0).getMid() + "|0|0";
        }
        else if(selectResult.size() == 2)
        {
            result = selectResult.get(0).getMid() + "|" + selectResult.get(1).getMid() + "|0";
        }
        else
        {
            result = selectResult.get(0).getMid() + "|" + selectResult.get(1).getMid() + "|" + selectResult.get(2).getMid();
        }

        return result;
    }
}
