package ai.aistem.xbot.framework.internal.http.model;


    /*   {
          "records": [
          {
              "robot_id": 0,
                  "achievement_template_id": 0,
                  "status": 0,
                  "schedule": "string"
          }
]
      }*/

import java.util.List;

public class Records {
    private List<Record> records;

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    public static class Record {
        private int robot_id;
        private int achievement_template_id;
        private int status;
        private String schedule;

        public int getRobot_id() {
            return robot_id;
        }

        public void setRobot_id(int robot_id) {
            this.robot_id = robot_id;
        }

        public int getAchievement_template_id() {
            return achievement_template_id;
        }

        public void setAchievement_template_id(int achievement_template_id) {
            this.achievement_template_id = achievement_template_id;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getSchedule() {
            return schedule;
        }

        public void setSchedule(String schedule) {
            this.schedule = schedule;
        }
    }
}
