package ai.aistem.xbot.framework.data.db.impl;

import org.litepal.LitePal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ai.aistem.xbot.framework.data.db.entities.TddeUsingTimeStatisticalInfo;

public class TddeUsingTimeStatisticalImpl {


    //根据当前系统日期去读取本地数据库
    public static synchronized TddeUsingTimeStatisticalInfo getDataByTimeID(String dateID)
    {
//        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
//        String todayTime = df.format(new Date());

        List<TddeUsingTimeStatisticalInfo> usingInfoList = LitePal.where("DateID = ?",dateID).find(TddeUsingTimeStatisticalInfo.class);

        if(usingInfoList.size() > 0)
        {
            return usingInfoList.get(0);
        }
        return  null;
    }

    /**
     * @param MusicTime  歌曲的时间
     * 说明:更新Music使用时间更新到数据库中(与原来的累加)  以系统时间为
     */
    public static synchronized void updateMusicUsingTime(int MusicTime)
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String todayTime = df.format(new Date());
        int DateID = Integer.valueOf(todayTime); //将时间转化为YYYYMMDD格式的整数型
//        TddeUsingTimeStatisticalInfo UsingTimeInfo = LitePal.find(TddeUsingTimeStatisticalInfo.class,DateID);
        TddeUsingTimeStatisticalInfo UsingTimeInfo = getDataByTimeID(todayTime);
        if(null == UsingTimeInfo)
        {
            UsingTimeInfo = new TddeUsingTimeStatisticalInfo();
            UsingTimeInfo.setDateID(DateID);
            UsingTimeInfo.setBookTime(0);
            UsingTimeInfo.setMusicTime(MusicTime);
            UsingTimeInfo.setLetterBoxWordCount(0);
            UsingTimeInfo.setLetterBoxTime(0);
            UsingTimeInfo.save();
        }
        else
        {
            UsingTimeInfo.addMusicTime(MusicTime);
//            UsingTimeInfo.update(DateID);
            UsingTimeInfo.saveOrUpdate("DateID = ?",todayTime);
        }
    }

    /**
     * @param BookTime  读书的时间
     * 说明:更新读书的使用时间更新到数据库中(与原来的累加)  以系统时间为
     */
    public static synchronized void updateBookUsingTime(int BookTime)
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String todayTime = df.format(new Date());
        int DateID = Integer.valueOf(todayTime); //将时间转化为YYYYMMDD格式的整数型
//        TddeUsingTimeStatisticalInfo UsingTimeInfo = LitePal.find(TddeUsingTimeStatisticalInfo.class,DateID);
        TddeUsingTimeStatisticalInfo UsingTimeInfo = getDataByTimeID(todayTime);
        if(null == UsingTimeInfo)
        {
            UsingTimeInfo = new TddeUsingTimeStatisticalInfo();
            UsingTimeInfo.setDateID(DateID);
            UsingTimeInfo.setMusicTime(0);
            UsingTimeInfo.setBookTime(BookTime);
            UsingTimeInfo.setLetterBoxWordCount(0);
            UsingTimeInfo.setLetterBoxTime(0);
            UsingTimeInfo.save();
        }
        else
        {
            UsingTimeInfo.addBookTime(BookTime);
//            UsingTimeInfo.update(DateID);
            UsingTimeInfo.saveOrUpdate("DateID = ?",todayTime);
        }
    }

    /**
     * @param BoxTime  游戏时间
     * 说明:更新游戏的使用时间更新到数据库中(与原来的累加)  以系统时间为
     */
    public static synchronized void updateLatterBoxUsingTime(int BoxTime)
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String todayTime = df.format(new Date());
        int DateID = Integer.valueOf(todayTime); //将时间转化为YYYYMMDD格式的整数型
//        TddeUsingTimeStatisticalInfo UsingTimeInfo = LitePal.find(TddeUsingTimeStatisticalInfo.class,DateID);
        TddeUsingTimeStatisticalInfo UsingTimeInfo = getDataByTimeID(todayTime);
        if(null == UsingTimeInfo)
        {
            UsingTimeInfo = new TddeUsingTimeStatisticalInfo();
            UsingTimeInfo.setDateID(DateID);
            UsingTimeInfo.setBookTime(0);
            UsingTimeInfo.setMusicTime(0);
            UsingTimeInfo.setLetterBoxWordCount(0);
            UsingTimeInfo.setLetterBoxTime(BoxTime);
            UsingTimeInfo.save();
        }
        else
        {
            UsingTimeInfo.addLetterBoxTime(BoxTime);
//            UsingTimeInfo.update(DateID);
            UsingTimeInfo.saveOrUpdate("DateID = ?",todayTime);
        }
    }

    /**
     * 说明:更新游戏的学习的新单词更新到数据库中(与原来的累加)每次加一  以系统时间为
     */
    public static synchronized void updateLatterBoxWordCount()
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String todayTime = df.format(new Date());
        int DateID = Integer.valueOf(todayTime); //将时间转化为YYYYMMDD格式的整数型
//        TddeUsingTimeStatisticalInfo UsingTimeInfo = LitePal.find(TddeUsingTimeStatisticalInfo.class,DateID);
        TddeUsingTimeStatisticalInfo UsingTimeInfo = getDataByTimeID(todayTime);
        if(null == UsingTimeInfo)
        {
            UsingTimeInfo = new TddeUsingTimeStatisticalInfo();
            UsingTimeInfo.setDateID(DateID);
            UsingTimeInfo.setBookTime(0);
            UsingTimeInfo.setMusicTime(0);
            UsingTimeInfo.setLetterBoxTime(0);
            UsingTimeInfo.setLetterBoxWordCount(1);
            UsingTimeInfo.save();
        }
        else
        {
            UsingTimeInfo.addLetterBoxWordCount();
            UsingTimeInfo.saveOrUpdate("DateID = ?",todayTime);
        }
    }

    public static synchronized String getLast7DayBookInfo()
    {
        String result = "";
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String todayTime = df.format(new Date());
        String Times  = "";
        try {
            Date date = df.parse(todayTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(calendar.DATE,-7); //调到七天前的日期
            for(int i=0; i<6; i++)
            {
                Times = df.format(calendar.getTime());
//                Log.d("Date",Times);
//                TddeUsingTimeStatisticalInfo UsingTimeInfo = LitePal.find(TddeUsingTimeStatisticalInfo.class,Times);
                TddeUsingTimeStatisticalInfo UsingTimeInfo = getDataByTimeID(Times);
                if(null == UsingTimeInfo)
                {
                    result += "0|";
                }
                else
                {
                    result += UsingTimeInfo.getBookTime() + "|";
                }
                calendar.add(calendar.DATE,1); //后移一天
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //获取今天的
//        Times = Integer.valueOf(todayTime);
//        TddeUsingTimeStatisticalInfo UsingTimeInfo = LitePal.find(TddeUsingTimeStatisticalInfo.class,Times);
        TddeUsingTimeStatisticalInfo UsingTimeInfo = getDataByTimeID(todayTime);
        if(null == UsingTimeInfo)
        {
            result += "0";
        }
        else
        {
            result += UsingTimeInfo.getBookTime() + "";
        }

        //返回前七天的结果 6|5|4|3|2|1|0
//        Log.d("Date Book",result);
        return result;
    }

    public static synchronized String getLast7DayMusicInfo()
    {
        String result = "";
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String todayTime = df.format(new Date());
        String Times  = "";
        try {
            Date date = df.parse(todayTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(calendar.DATE,-7); //调到七天前的日期
            for(int i=0; i<6; i++)
            {
                Times = df.format(calendar.getTime());
//                Log.d("Date",Times);
//                TddeUsingTimeStatisticalInfo UsingTimeInfo = LitePal.find(TddeUsingTimeStatisticalInfo.class,Times);
                TddeUsingTimeStatisticalInfo UsingTimeInfo = getDataByTimeID(Times);
                if(null == UsingTimeInfo)
                {
                    result += "0|";
                }
                else
                {
                    result += UsingTimeInfo.getMusicTime() + "|";
                }
                calendar.add(calendar.DATE,1); //后移一天
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //获取今天的
//        Times = Integer.valueOf(todayTime);
//        TddeUsingTimeStatisticalInfo UsingTimeInfo = LitePal.find(TddeUsingTimeStatisticalInfo.class,Times);
        TddeUsingTimeStatisticalInfo UsingTimeInfo = getDataByTimeID(todayTime);
        if(null == UsingTimeInfo)
        {
            result += "0";
        }
        else
        {
            result += UsingTimeInfo.getMusicTime() + "";
        }

        //返回前七天的结果 6|5|4|3|2|1|0
//        Log.d("Date Music",result);
        return result;
    }

    public static synchronized String getLast7DayLetterBoxInfo()
    {
        String result = "";
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String todayTime = df.format(new Date());
        String Times  = "";
        try {
            Date date = df.parse(todayTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(calendar.DATE,-7); //调到七天前的日期
            for(int i=0; i<6; i++)
            {
                Times = df.format(calendar.getTime());
//                Log.d("Date",Times);
//                TddeUsingTimeStatisticalInfo UsingTimeInfo = LitePal.find(TddeUsingTimeStatisticalInfo.class,Times);
                TddeUsingTimeStatisticalInfo UsingTimeInfo = getDataByTimeID(Times);
                if(null == UsingTimeInfo)
                {
                    result += "0|";
                }
                else
                {
                    result += UsingTimeInfo.getLetterBoxTime() + "|";
                }
                calendar.add(calendar.DATE,1); //后移一天
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //获取今天的
//        TddeUsingTimeStatisticalInfo UsingTimeInfo = LitePal.find(TddeUsingTimeStatisticalInfo.class,Times);
        TddeUsingTimeStatisticalInfo UsingTimeInfo = getDataByTimeID(todayTime);
        if(null == UsingTimeInfo)
        {
            result += "0";
        }
        else
        {
            result += UsingTimeInfo.getLetterBoxTime() + "";
        }

        //返回前七天的结果 6|5|4|3|2|1|0
//        Log.d("Date Word",result);
        return result;
    }

    public static synchronized String getLast7DayLetterBoxCountInfo()
    {
        String result = "";
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String todayTime = df.format(new Date());
        String Times  = "";
        try {
            Date date = df.parse(todayTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(calendar.DATE,-7); //调到七天前的日期
            for(int i=0; i<6; i++)
            {
                Times = df.format(calendar.getTime());
//                Log.d("Date",Times);
//                TddeUsingTimeStatisticalInfo UsingTimeInfo = LitePal.find(TddeUsingTimeStatisticalInfo.class,Times);
                TddeUsingTimeStatisticalInfo UsingTimeInfo = getDataByTimeID(Times);
                if(null == UsingTimeInfo)
                {
                    result += "0|";
                }
                else
                {
                    result += UsingTimeInfo.getLetterBoxWordCount() + "|";
                }
                calendar.add(calendar.DATE,1); //后移一天
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //获取今天的
//        TddeUsingTimeStatisticalInfo UsingTimeInfo = LitePal.find(TddeUsingTimeStatisticalInfo.class,Times);
        TddeUsingTimeStatisticalInfo UsingTimeInfo = getDataByTimeID(todayTime);
        if(null == UsingTimeInfo)
        {
            result += "0";
        }
        else
        {
            result += UsingTimeInfo.getLetterBoxWordCount() + "";
        }

        //返回前七天的结果 6|5|4|3|2|1|0
//        Log.d("Date WordCount",result);
        return result;
    }

}
