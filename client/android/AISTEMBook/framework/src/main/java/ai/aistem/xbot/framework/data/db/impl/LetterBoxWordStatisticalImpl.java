package ai.aistem.xbot.framework.data.db.impl;

import android.util.Log;

import org.litepal.LitePal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ai.aistem.xbot.framework.data.db.entities.LetterBoxWordStatiscalInfo;
//import ai.aistem.xbot.framework.unity.database.entities.DinoCrackDisplayInfo;
//import ai.aistem.xbot.framework.unity.database.entities.DisplayWordInfo;

public class LetterBoxWordStatisticalImpl {


    /**
     * @param wordID  歌曲的ID
     * 说明:将该ID对应的单词的错误次数加一  如果不存在插入到数据库
     */
    public static synchronized void updateLetterBoxCount(int wordID)
    {
//        LetterBoxWordStatiscalInfo musicInfo =  LitePal.find(LetterBoxWordStatiscalInfo.class,wordID);
        List<LetterBoxWordStatiscalInfo> wordList = LitePal.where("wordID = ?",String.valueOf(wordID)).find(LetterBoxWordStatiscalInfo.class);
        LetterBoxWordStatiscalInfo wordInfo = null;

        if(wordList.size() > 0)
        {
            wordInfo = wordList.get(0);
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String UpdateTime = df.format(new Date());
        Log.d("Date",UpdateTime);
        if(null == wordInfo)
        {
            wordInfo = new LetterBoxWordStatiscalInfo();
            wordInfo.setWordID(wordID);
            wordInfo.setToday(1);
            wordInfo.setOneDay(0);
            wordInfo.setTwoDay(0);
            wordInfo.setThreeDay(0);
            wordInfo.setFourDay(0);
            wordInfo.setFiveDay(0);
            wordInfo.setSixDay(0);

            wordInfo.setUpdateTime(UpdateTime);
            wordInfo.save();
        }
        else
        {
            if(UpdateTime.equals(wordInfo.getUpdateTime()))
            {
                wordInfo.addToday();
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
                    oldDate = format.parse(wordInfo.getUpdateTime());
                    Days = (newDate.getTime() - oldDate.getTime())/(24*60*60*1000);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                for(int i=0; i<Days; i++)
                {
                    wordInfo.aNewDay(UpdateTime);
                }
                wordInfo.addToday();
            }
//            wordInfo.update(wordID);
            wordInfo.saveOrUpdate("wordID = ?",String.valueOf(wordID));
        }
    }

    /**
     * @return  历史七天中每天错误最多的五个单词  worid|worid|worid  如果没有5个  用0填充
     */
    private static synchronized String getWordNameByID(int ID)
    {
//        List<DisplayWordInfo>  Words = LitePal.where("wordId=?",String.valueOf(ID)).find(DisplayWordInfo.class);
//        if(Words.size() > 0)
//        {
//            return Words.get(0).getWordName();
//        }
//
//        List<DinoCrackDisplayInfo> Word2 = LitePal.where("wordId=?",String.valueOf(ID)).find(DinoCrackDisplayInfo.class);
//        if(Word2.size() > 0)
//        {
//            return Word2.get(0).getWordName();
//        }

        return "";
    }

    /**
     * @return  历史七天中每天错误最多的五个单词  worid|worid|worid  如果没有5个  用0填充
     */
    public static synchronized String selectMaxCountWord()
    {
        String result = "";
        //获取今天错误最多的五个单词
        List<LetterBoxWordStatiscalInfo> TodayWords = LitePal.select("wordID").where("Today > 1").order("Today desc").limit(5).find(LetterBoxWordStatiscalInfo.class);
        String TodayWord = "";
        for(int i=0; i<TodayWords.size() && i<5; i++)
        {
//            TodayWord += TodayWords.get(i).getWordID() + ",";
            TodayWord += getWordNameByID(TodayWords.get(i).getWordID()) + ",";
        }
//        result += TodayWord +"|";
        result = TodayWord;

        //获取前一天错误最多的五个单词
        List<LetterBoxWordStatiscalInfo> OnedayWords = LitePal.select("wordID").where("oneday > 1").order("oneday desc").limit(5).find(LetterBoxWordStatiscalInfo.class);
        String OnedayWord = "";
        for(int i=0; i<OnedayWords.size() && i<5; i++)
        {
//            OnedayWord += OnedayWords.get(i).getWordID() + ",";
            OnedayWord += getWordNameByID(OnedayWords.get(i).getWordID()) + ",";
        }
//        result += OnedayWord +"|";
        result = OnedayWord +"|" + result;

        //获取前两天的错误单词数
        List<LetterBoxWordStatiscalInfo> TwodayWords = LitePal.select("wordID").where("twoday > 1").order("twoday desc").limit(5).find(LetterBoxWordStatiscalInfo.class);
        String TwodayWord = "";
        for(int i=0; i<TwodayWords.size() && i<5; i++)
        {
//            TwodayWord += TwodayWords.get(i).getWordID() + ",";
            TwodayWord += getWordNameByID(TwodayWords.get(i).getWordID()) + ",";
        }
//        result += TwodayWord +"|";
        result = TwodayWord +"|" + result;
        //获取前3天的错误单词数
        List<LetterBoxWordStatiscalInfo> ThreedayWords = LitePal.select("wordID").where("threeDay > 1").order("threeDay desc").limit(5).find(LetterBoxWordStatiscalInfo.class);
        String ThreedayWord = "";
        for(int i=0; i<ThreedayWords.size() && i<5; i++)
        {
//            ThreedayWord += ThreedayWords.get(i).getWordID() + ",";
            ThreedayWord += getWordNameByID(ThreedayWords.get(i).getWordID()) + ",";
        }
//        result += ThreedayWord +"|";
        result = ThreedayWord +"|" + result;

        //获取前4天的错误单词数
        List<LetterBoxWordStatiscalInfo> FourdayWords = LitePal.select("wordID").where("fourDay > 1").order("fourDay desc").limit(5).find(LetterBoxWordStatiscalInfo.class);
        String FourdayWord = "";
        for(int i=0; i<FourdayWords.size() && i<5; i++)
        {
//            FourdayWord += FourdayWords.get(i).getWordID() + ",";
            FourdayWord += getWordNameByID(FourdayWords.get(i).getWordID()) + ",";
        }
//        result += FourdayWord +"|";
        result = FourdayWord +"|" + result;

        //获取前5天的错误单词数
        List<LetterBoxWordStatiscalInfo> FivedayWords = LitePal.select("wordID").where("fiveDay > 1").order("fiveDay desc").limit(5).find(LetterBoxWordStatiscalInfo.class);
        String FivedayWord = "";
        for(int i=0; i<FivedayWords.size() && i<5; i++)
        {
//            FivedayWord += FivedayWords.get(i).getWordID() + ",";
            FivedayWord += getWordNameByID(FivedayWords.get(i).getWordID()) + ",";
        }
//        result += FivedayWord +"|";
        result = FivedayWord +"|" + result;

        //获取前6天的错误单词数
        List<LetterBoxWordStatiscalInfo> SixdayWords = LitePal.select("wordID").where("sixDay > 1").order("sixDay desc").limit(5).find(LetterBoxWordStatiscalInfo.class);
        String SixdayWord = "";
        for(int i=0; i<SixdayWords.size() && i<5; i++)
        {
//            SixdayWord += SixdayWords.get(i).getWordID() + ",";
            SixdayWord += getWordNameByID(SixdayWords.get(i).getWordID()) + ",";
        }
//        result += SixdayWord;
        result = SixdayWord + "|" + result;

        return result;
    }
}
