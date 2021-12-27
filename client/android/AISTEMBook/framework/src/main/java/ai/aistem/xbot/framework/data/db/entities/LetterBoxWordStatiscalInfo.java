package ai.aistem.xbot.framework.data.db.entities;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class LetterBoxWordStatiscalInfo  extends LitePalSupport implements Serializable {

    private int wordID;        //音乐的ID 不管名字
    private int Today;      //今天错的次数　　不断更新
    private int oneDay;       //前1天错的次数
    private int twoDay;       //前2天错的次数
    private int threeDay;       //前3天错的次数
    private int fourDay;       //前4天错的次数
    private int fiveDay;       //前5天错的次数
    private int sixDay;       //前6天错的次数
    private String updateTime;    //更新时间

    public int getToday() {
        return Today;
    }
    public void addToday()
    {
        Today += 1;
    }
    public void setToday(int today) {
        Today = today;
    }

    public int getWordID() {
        return wordID;
    }

    public void setWordID(int wordID) {
        this.wordID = wordID;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
    public void aNewDay(String UpdateTime){
        sixDay = fiveDay;
        fiveDay = fourDay;
        fourDay = threeDay;
        threeDay = twoDay;
        twoDay = oneDay;
        oneDay = Today;
        Today = 0;
        updateTime = UpdateTime;
    }

    public void setOneDay(int oneDay) {
        this.oneDay = oneDay;
    }

    public void setTwoDay(int twoDay) {
        this.twoDay = twoDay;
    }

    public void setThreeDay(int threeDay) {
        this.threeDay = threeDay;
    }

    public void setFourDay(int fourDay) {
        this.fourDay = fourDay;
    }

    public void setFiveDay(int fiveDay) {
        this.fiveDay = fiveDay;
    }

    public void setSixDay(int sixDay) {
        this.sixDay = sixDay;
    }
}
