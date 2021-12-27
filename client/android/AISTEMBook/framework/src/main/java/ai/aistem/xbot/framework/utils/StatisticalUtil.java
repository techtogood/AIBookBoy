package ai.aistem.xbot.framework.utils;

import ai.aistem.xbot.framework.data.db.impl.TddeBookStatisticalImpl;
import ai.aistem.xbot.framework.data.db.impl.TddeMusicStatisticalImpl;
import ai.aistem.xbot.framework.data.db.impl.TddeUsingTimeStatisticalImpl;
import ai.aistem.xbot.framework.data.db.impl.LetterBoxWordStatisticalImpl;

public class StatisticalUtil {

    private static StatisticalUtil instance = null;

//    private final Object ExitHandleShare = new Object(); //绘本阅读退出后台处理共享OBJECT

    public synchronized static StatisticalUtil getInstance() {
        if (instance == null) {
            instance = new StatisticalUtil();
        }
        return instance;
    }

    private static long startTime = 0;
    private static long stopTime = 0;
    private static int CurrentMode = 0; //1:磨耳朵  2:绘本阅读  3:自然拼读

    private StatisticalUtil() {
//        new ExitHandleThread().start();
    }

    /**
     * 开始摸耳朵计时
     */
    public void StartListenTiming()
    {
        CurrentMode = 1;
        startTime = System.currentTimeMillis()/1000;//得到开始计时的秒数
    }

    /**
     * 开始绘本阅读计时
     */
    public void StartReadTiming()
    {
        CurrentMode = 2;
        startTime = System.currentTimeMillis()/1000;//得到开始计时的秒数
    }
    /**
     * 开始自然拼读计时
     */
    public void StartSpellTiming()
    {
        CurrentMode = 3;
        startTime = System.currentTimeMillis()/1000;//得到开始计时的秒数
    }

    /**
     * 结束计时  根据CurrentMode更新对应的计时
     */
    public void StopTiming()
    {
        stopTime = System.currentTimeMillis()/1000;
        long time = (stopTime - startTime+30)/60;//对于大于30秒的按照1分钟计

        switch (CurrentMode)
        {
            case 1:
            {
                TddeUsingTimeStatisticalImpl.updateMusicUsingTime((int)time);
//                synchronized (ExitHandleShare) {
//                    ExitHandleShare.notify();
//                }

                break;
            }
            case 2:
            {
                TddeUsingTimeStatisticalImpl.updateBookUsingTime((int)time);
//                synchronized (ExitHandleShare) {
//                    ExitHandleShare.notify();
//                }

                break;
            }
            case 3:
            {
                TddeUsingTimeStatisticalImpl.updateLatterBoxUsingTime((int)time);
//                synchronized (ExitHandleShare) {
//                    ExitHandleShare.notify();
//                }
                break;
            }
            default:
            {
                break;
            }
        }
        startTime = 0;
        stopTime = 0;
        CurrentMode = 0;
    }


    /**
     * @param musicID  本次播放音乐的ID
     * 简介:每播放一首歌曲就调一次该借口将播放的音乐计次
     */
    public void ListenCount(int musicID)
    {
        TddeMusicStatisticalImpl.updateMusicCount(musicID);
//        HttpApiHelper.getInstance().doTddeStatisticalPOST();
    }

    /**
     * 简介:每学习一个新单词调用一次(注:只记录新单词)  只记录学习单词的个数
     */
    public void SpellCount()
    {
        TddeUsingTimeStatisticalImpl.updateLatterBoxWordCount();
//        HttpApiHelper.getInstance().doTddeStatisticalPOST();
    }

    /**
     * @param wordID  错误的单词ID
     *简介:每次学习错误一个单词就调用一次  单词错误的次数
     */
    public void WrongWordCount(int wordID)
    {
        LetterBoxWordStatisticalImpl.updateLetterBoxCount(wordID);
//        HttpApiHelper.getInstance().doTddeStatisticalPOST();
    }

    /**
     * @param bookID 本次阅读书的ID
     *简介:用于记录读书的ID的次数  每次开始读一本书时就调用一次
     */
    public void ReadCount(int bookID)
    {
        TddeBookStatisticalImpl.updateBookCount(bookID);
//        HttpApiHelper.getInstance().doTddeStatisticalPOST();
    }

    //暂时没用，注释掉
//    public class ExitHandleThread extends Thread {
//        public void run() {
//            synchronized (ExitHandleShare) {
//                while (true) {
//                    try {
//                        ExitHandleShare.wait();
//                        //HttpApiHelper.getInstance().doTddeStatisticalPOST();
//                    } catch (Exception e) {
//                    }
//                }
//            }
//        }
//    }

}
