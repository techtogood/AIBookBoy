package ai.aistem.xbot.framework.data.bean;

import java.util.List;

public class AchievementSchedule {

    List<Schedule> sch;

    public List<Schedule> getSch() {
        return sch;
    }

    public void setSch(List<Schedule> sch) {
        this.sch = sch;
    }

    public class Schedule {
        private String sch_1;

        public String getSch_1() {
            return sch_1;
        }

        public void setSch_1(String sch_1) {
            this.sch_1 = sch_1;
        }
    }
}
