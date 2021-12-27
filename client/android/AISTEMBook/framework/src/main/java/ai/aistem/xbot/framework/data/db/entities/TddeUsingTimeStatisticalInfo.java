package ai.aistem.xbot.framework.data.db.entities;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class TddeUsingTimeStatisticalInfo extends LitePalSupport implements Serializable {

    private int DateID;//主键  日期
    private int MusicTime; //摸耳朵使用时间
    private int LetterBoxTime;//学习单词时间
    private int LetterBoxWordCount;//每天学习单词个数
    private int BookTime;//绘本阅读时间


    public int getMusicTime() {
        return MusicTime;
    }

    public void setMusicTime(int musicTime) {
        MusicTime = musicTime;
    }
    public void addMusicTime(int musicTime) {
        MusicTime += musicTime;
    }

    public int getLetterBoxTime() {
        return LetterBoxTime;
    }

    public void setLetterBoxTime(int letterBoxTime) {
        LetterBoxTime = letterBoxTime;
    }
    public void addLetterBoxTime(int letterBoxTime) {
        LetterBoxTime += letterBoxTime;
    }

    public int getBookTime() {
        return BookTime;
    }

    public void setBookTime(int bookTime) {
        BookTime = bookTime;
    }
    public void addBookTime(int bookTime) {
        BookTime += bookTime;
    }

    public int getDateID() {
        return DateID;
    }

    public void setDateID(int dateID) {
        DateID = dateID;
    }

    public int getLetterBoxWordCount() {
        return LetterBoxWordCount;
    }

    public void setLetterBoxWordCount(int letterBoxWordCount) {
        LetterBoxWordCount = letterBoxWordCount;
    }
    public void addLetterBoxWordCount() {
        LetterBoxWordCount += 1;
    }
}
