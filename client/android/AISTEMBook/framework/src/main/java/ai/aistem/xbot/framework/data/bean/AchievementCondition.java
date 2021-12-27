package ai.aistem.xbot.framework.data.bean;


import java.util.List;

public class AchievementCondition {
    List<Condition> con;

    public List<Condition> getCon() {
        return con;
    }

    public void setCon(List<Condition> con) {
        this.con = con;
    }

    public class Condition{
        private String con_1;

        public String getCon_1() {
            return con_1;
        }

        public void setCon_1(String con_1) {
            this.con_1 = con_1;
        }
    }
}
