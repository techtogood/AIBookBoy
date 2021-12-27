package ai.aistem.xbot.framework.data.db.impl;

import org.litepal.LitePal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ai.aistem.xbot.framework.data.db.entities.TddeBookStatisticalInfo;

public class TddeBookStatisticalImpl {

    /**
     * @param bookID  歌曲的ID
     * 说明:将该ID对应的绘本的阅读次数加一  如果不存在插入到数据库
     */
    public static synchronized void updateBookCount(int bookID)
    {
//        TddeBookStatisticalInfo musicInfo =  DataSupport.find(TddeBookStatisticalInfo.class,bookID);
        List<TddeBookStatisticalInfo> BookList = LitePal.where("bookID = ?",String.valueOf(bookID)).find(TddeBookStatisticalInfo.class);
        TddeBookStatisticalInfo bookInfo = null;
        if(BookList.size() > 0)
        {
            bookInfo = BookList.get(0);
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String UpdateTime = df.format(new Date());
        if(null == bookInfo)
        {
            bookInfo = new TddeBookStatisticalInfo();
            bookInfo.setBookID(bookID);
            bookInfo.setToday(1);
            bookInfo.setAllCount(1);
            bookInfo.setYDay("0|0|0|0|0|0");
            bookInfo.setUpdateTime(UpdateTime);
            bookInfo.save();
        }
        else
        {
            if(UpdateTime.equals(bookInfo.getUpdateTime()))
            {
                bookInfo.addToday();
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
                    oldDate = format.parse(bookInfo.getUpdateTime());
                    Days = (newDate.getTime() - oldDate.getTime())/(24*60*60*1000);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                for(int i=0; i<Days; i++)
                {
                    bookInfo.aNewDay(UpdateTime);
                }
                bookInfo.addToday();
            }
            //计算七天播放的所有次数
            String[] counts = bookInfo.getYDay().split("\\|");
            int Count = bookInfo.getToday();
            for(int i=0; i<counts.length; i++)
            {
                Count += Integer.valueOf(counts[i]);
            }
            bookInfo.setAllCount(Count);

//            bookInfo.update(bookID);
            bookInfo.saveOrUpdate("bookID = ?",String.valueOf(bookID));
        }
    }

    /**
     * @return  音乐中播放次数最多的三首歌   mid|mid|mid  如果没有三首  用0填充
     */
    public static synchronized String selectMaxCountBook()
    {
        String result = "";
        List<TddeBookStatisticalInfo> selectResult = LitePal.select("bookID").order("AllCount desc").limit(3).find(TddeBookStatisticalInfo.class);
        if(selectResult.size() < 1)
        {
            result = "0|0|0";
        }
        else if(selectResult.size() == 1)
        {
            result = selectResult.get(0).getBookID() + "|0|0";
        }
        else if(selectResult.size() == 2)
        {
            result = selectResult.get(0).getBookID() + "|" + selectResult.get(1).getBookID() + "|0";
        }
        else
        {
            result = selectResult.get(0).getBookID() + "|" + selectResult.get(1).getBookID() + "|" + selectResult.get(2).getBookID();
        }

        return result;
    }
}
