package ai.aistem.xbot.framework.data.db.impl;


import android.content.ContentValues;

import org.litepal.LitePal;

import java.util.List;

import ai.aistem.xbot.framework.data.db.entities.TddeAchievement;

public class TddeAchievementImpl {

    public static synchronized void modify(TddeAchievement achievement) {

        if (checkExistByTemplateId(achievement.getAchievement_template_id())) {
            ContentValues values = new ContentValues();
            values.put("name", achievement.getName());
            values.put("type_id", achievement.getType_id());
            values.put("status", achievement.getStatus());
            values.put("conditions", achievement.getConditions());
            values.put("schedule", achievement.getSchedule());
            values.put("isUpload", achievement.isUpload());
            values.put("updateTime", System.currentTimeMillis());
            LitePal.updateAll(TddeAchievement.class, values, "achievement_template_id=?",
                    String.valueOf(achievement.getAchievement_template_id()));
        } else {
            achievement.setUpdateTime(System.currentTimeMillis());
            achievement.setCreateTime(System.currentTimeMillis());
            achievement.save();
        }
    }


    public static synchronized void setStatusByTemplateId(int achievement_template_id, int status) {
        if (checkExistByTemplateId(achievement_template_id)) {
            ContentValues values = new ContentValues();
            values.put("status", status);
            LitePal.updateAll(TddeAchievement.class, values, "achievement_template_id=?",
                    String.valueOf(achievement_template_id));
        }
    }

    private static boolean checkExistByName(String name) {
        List<TddeAchievement> list = LitePal.where("name=?", name)
                .find(TddeAchievement.class);
        if (list != null && list.size() > 0) {
            return true;
        }
        return false;
    }

    public static List<TddeAchievement> getByName(String name) {
        return LitePal.where("name=?", name)
                .find(TddeAchievement.class);
    }

    /**
     * 查询父勋章 template_id 为 type_id 的子勋章的个数
     *
     * @param type_id 父勋章id
     * @return
     */
    public static int countByTypeId(int type_id) {
        return LitePal.where("type_id=?", String.valueOf(type_id))
                .count(TddeAchievement.class);
    }

    /**
     * 查询父勋章 template_id 为 type_id 且完成状态为 status 的子勋章的个数
     *
     * @param type_id 父勋章id
     * @param status  完成状态 0-未完成 1-进行中 2-已完成
     * @return
     */
    public static int countByTypeIdAndStatus(int type_id, int status) {
        return LitePal.where("type_id=? and status=?", String.valueOf(type_id),
                String.valueOf(status)).count(TddeAchievement.class);
    }

    private static boolean checkExistByTemplateId(int achievement_template_id) {
        List<TddeAchievement> list = LitePal.where("achievement_template_id=?",
                String.valueOf(achievement_template_id)).find(TddeAchievement.class);
        if (list != null && list.size() > 0) {
            return true;
        }
        return false;
    }
}
