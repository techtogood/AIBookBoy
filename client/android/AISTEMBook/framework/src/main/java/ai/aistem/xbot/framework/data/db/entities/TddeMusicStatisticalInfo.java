package ai.aistem.xbot.framework.data.db.entities;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class TddeMusicStatisticalInfo   extends LitePalSupport implements Serializable {

    private int mid;        //音乐的ID 不管名字
    private int Today;      //今天听的次数　　　不断更新
    private String YDay;       //前6天听的次数  以|隔开  初始化为:0|0|0|0|0|0  (6|5|4|3|2|1)
    private int AllCount;
    private String updateTime;    //更新时间

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public void setToday(int count) {
        this.Today = count;
    }
    public void addToday() {
        this.Today += 1;
    }
    public int getToday() {
        return Today;
    }

    public void setYDay(String str) {
        this.YDay = str;
    }
    public String getYDay() {
        return YDay;
    }

    //开始新的一天,将Today置为替换掉YDay中的第一个,然后将Today置为0
    public void aNewDay(String UpdateTime){
        String[] YDayCount = YDay.split("\\|");
        //判断YDay里面的天数是否足够
//        ArrayList<String> YDayCountList = new ArrayList<String>();
//        for(int i=0; i<YDayCount.length; i++)
//        {
//            YDayCountList.add(YDayCount[i]);
//        }
//        while (YDayCountList.size() < 6)
//        {
//            YDayCountList.add("0");
//        }
//        while (YDayCountList.size() > 6)
//        {
//            YDayCountList.remove(YDayCountList.size()-1);
//        }
//        YDayCount =  (String[])YDayCountList.toArray();

        //依次前移
//        String[] YDayDest = new String[6];
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

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public int getAllCount() {
        return AllCount;
    }

    public void setAllCount(int allCount) {
        AllCount = allCount;
    }
}
