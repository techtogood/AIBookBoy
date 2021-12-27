package ai.aistem.xbot.framework.internal.http.model;

/*
{
  "status": 200,
  "result": [
    {
      "achievement_template_id": 2,
      "name": "绘本",
      "module": "study",
      "module_detail": null,
      "type_id": 0,
      "context": "宝宝开启绘本阅读功能，才能获得这个勋章哦！",
      "context_finish": "您的宝宝已开启绘本阅读之旅啦！",
      "show_type": 0,
      "reward": "{}",
      "pic_url": "https://aistem-voice.oss-cn-shenzhen.aliyuncs.com/cover/default.png",
      "conditions": "{\"con\":[{\"con_1\":1,\"des\":\"开启绘本\"}]}",
      "last_ver": 1,
      "is_valid": 1,
      "create_time": "2018-09-11T11:47:16.000Z",
      "update_time": "2018-09-13T02:49:44.000Z"
    }
  ]
}
*/


import java.util.List;

public class AchievementTemplateResponse {
    private int status;
    private String message;
    private List<Template> result;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Template> getResult() {
        return result;
    }

    public void setResult(List<Template> result) {
        this.result = result;
    }

    public class Template{
        private int achievement_template_id;
        private String name;
        private String conditions;
        private int type_id;

        public int getType_id() {
            return type_id;
        }

        public void setType_id(int type_id) {
            this.type_id = type_id;
        }

        public int getAchievement_template_id() {
            return achievement_template_id;
        }

        public void setAchievement_template_id(int achievement_template_id) {
            this.achievement_template_id = achievement_template_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getConditions() {
            return conditions;
        }

        public void setConditions(String conditions) {
            this.conditions = conditions;
        }
    }

}
