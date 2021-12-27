package ai.aistem.xbot.framework.data.db.entities;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class TddeBookStatisticalInfo extends LitePalSupport implements Serializable {
    private int bookID;             //书的ID 和绘本阅读里面的书ID一一对应
    private int Today;             //该本书今天读的次数  为了方便更新 搞成int
    private String YDay;          //以往6天的读次数信息
    private int AllCount;         //最近七天读的次数和
    private String updateTime;    //更新时间

    public int getBookID() {
        return bookID;
    }

    public void setBookID(int bookID) {
        this.bookID = bookID;
    }

    public int getToday() {
        return Today;
    }

    public void setToday(int today) {
        Today = today;
    }
    public void addToday() {
        this.Today += 1;
    }
    public String getYDay() {
        return YDay;
    }
    public void setYDay(String YDay) {
        this.YDay = YDay;
    }

    public int getAllCount() {
        return AllCount;
    }

    public void setAllCount(int allCount) {
        AllCount = allCount;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    //开始新的一天,将Today置为替换掉YDay中的第一个,然后将Today置为0
    public void aNewDay(String UpdateTime){
        String[] YDayCount = YDay.split("\\|");
        //        YDay = Today+"|";
        //其中第一个是前第六天的   更新一天时需要去掉  所以下标从1开始
        YDay = "";
        for(int i=1; i<YDayCount.length; i++)
        {
            YDay += YDayCount[i] + "|";
        }
        YDay += Today;
        Today = 0;
        updateTime = UpdateTime;
    }
}
