package ai.aistem.xbot.framework.utils;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import ai.aistem.xbot.framework.application.DCApplication;
import ai.aistem.xbot.framework.data.bean.AchievementCondition;
import ai.aistem.xbot.framework.data.bean.AchievementSchedule;
import ai.aistem.xbot.framework.data.db.entities.TddeAchievement;
import ai.aistem.xbot.framework.data.db.impl.TddeAchievementImpl;
import ai.aistem.xbot.framework.internal.http.HttpApiHelper;
import ai.aistem.xbot.framework.internal.http.async.PostOrGetCallback;
import ai.aistem.xbot.framework.internal.http.model.Records;

public class AchvUtil {

      /*
      String[] test0 = {"元音a", "元音e", "元音i", "元音o", "元音u"};
        for (int i = 0; i < test0.length; i++)
            AchvUtil.UpdateCVCAchievementRecords(test0[i], 1000);

        String[] test = {"辅音丛",
                "二合辅音",
                "三合辅音",
                "长元音",
                "不发音的E",
                "R控制的元音",
                "其他元音字母组合"};
        for (int i = 0; i < test.length; i++)
            AchvUtil.UpdateLetterAchievementRecords(test[i], 1000);

        int len = AchvUtil.AchvTemplateNames.length;
        //上传A-Z子勋章测试
        for (int i = len - 1; i > len - 27; i--)
            AchvUtil.UpdateLetterAchievementRecords(AchvUtil.AchvTemplateNames[i]);*/


    public static String[] AchvTemplateNames = {
            "磨耳朵",
            "绘本",
            "Tdde ABC游戏",
            "Dino Crack游戏",
            "Letter Box游戏",
            "A-Z字母",
            "CVC单词",
            "辅音丛",
            "二合辅音",
            "三合辅音",
            "长元音",
            "不发音的E",
            "R控制的元音",
            "其他元音字母组合",
            "元音a",
            "元音e",
            "元音i",
            "元音o",
            "元音u",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};


    public static void showTips(String name, int sch_1, int con_1) {
        Log.d("AchvUtil", "勋章:" + name + " 进度:sch_1/con_1---" + sch_1 + "/" + con_1);
    }

    /**
     * 判断字符串s第一个字符是否为大小字母
     *
     * @param s 字符串
     * @return true/false
     */
    public static boolean IsUpper(String s) {
        char c = s.charAt(0);
        int i = (int) c;
        if (i >= 65 && i <= 90) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 批量上传成就勋章记录
     */
    public static void UploadAchvRecords() {

        Records records = new Records();
        final List<Records.Record> records_list = new ArrayList<>();

        final List<TddeAchievement> tddeAchievementList = LitePal.findAll(TddeAchievement.class);
        for (TddeAchievement tddeAchievement : tddeAchievementList) {
            final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
            if (achievement_template_id < 1) return;

            Records.Record record = new Records.Record();
            record.setRobot_id(DCApplication.mDataManager.getRobotID());
            record.setAchievement_template_id(tddeAchievement.getAchievement_template_id());
            record.setStatus(tddeAchievement.getStatus());
            record.setSchedule(tddeAchievement.getSchedule());
            //判断是否需要上传，条件：未上传且有进度更新
            String schedule = tddeAchievement.getSchedule();
            if (!tddeAchievement.isUpload() && !schedule.equals(""))
                records_list.add(record);
        }
        if (records_list.size() > 0) {
            records.setRecords(records_list);
            HttpApiHelper.doRobotAchievementRecordsApiPost(records).listen(new PostOrGetCallback() {
                @Override
                public void onFinish() {
                    //上传成功则在数据库中记录
                    Log.d("UploadAchvRecords", "成功上传成就勋章，更新数据库记录");
                    for (TddeAchievement tddeAchievement : tddeAchievementList) {
                        if (!tddeAchievement.isUpload()) {
                            tddeAchievement.setUpload(true);
                            tddeAchievement.saveOrUpdate("achievement_template_id=?"
                                    , String.valueOf(tddeAchievement.getAchievement_template_id()));
                        }
                    }
                }
            });
        }
    }

    /**
     * 判断是否已经更新勋章模板，若没有更新则更新至数据库；
     */
    public static void CheckAndGetAchievementTemplate() {
        for (String AchvTemplateName : AchvTemplateNames) {
            List<TddeAchievement> tddeAchievementList =
                    LitePal.where("name=?", AchvTemplateName).find(TddeAchievement.class);
            if (tddeAchievementList.size() == 0) {
                HttpApiHelper.doRobotAchievementTemplateApiGet(AchvTemplateName).listen(new PostOrGetCallback() {
                    @Override
                    public void onFinish() {
                        Log.d("CheckAndGetAchievement", "成功获取勋章模板");
                    }
                });
            }
        }
    }

    /**
     * 保存开启学习之路勋章
     *
     * @param name "磨耳朵","绘本","Tdde ABC游戏","Dino Crack游戏","Letter Box游戏"
     */
    public static void UpdateStudyAchievementRecords(final String name) {
        //检查输入有效性
        boolean valid = false;
        String[] name_list = {"磨耳朵", "绘本", "Tdde ABC游戏", "Dino Crack游戏", "Letter Box游戏"};
        for (String n : name_list) {
            if (n.equals(name)) valid = true;
        }
        if (!valid) {
            Log.e("UploadStudyAchievement", "参数无效!");
            return;
        }
        //判断勋章是否已经上传,如果没有上传则从服务器获取到成就勋章模板,然后再上传.
        final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
        if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
            boolean isUpload = tddeAchievementList.get(0).isUpload();
            final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
            if (!isUpload) {
                tddeAchievementList.get(0).setStatus(2);
                tddeAchievementList.get(0).setSchedule("{\"sch\":[{\"sch_1\":\"1\"}]}");
                tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                        , String.valueOf(achievement_template_id));
            }
        } else {
            //从服务端获取成就勋章模板
            HttpApiHelper.doRobotAchievementTemplateApiGet(name).listen(new PostOrGetCallback() {
                @Override
                public void onFinish() {
                    final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
                    if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
                        boolean isUpload = tddeAchievementList.get(0).isUpload();
                        final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
                        if (!isUpload) {
                            tddeAchievementList.get(0).setStatus(2);
                            tddeAchievementList.get(0).setSchedule("{\"sch\":[{\"sch_1\":\"1\"}]}");
                            tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                                    , String.valueOf(achievement_template_id));
                        }
                    }
                }
            });
        }
    }

    /**
     * 保存 A-Z字母 父勋章成就记录
     */
    public static void UpdateAZAchievementRecord() {
        final String name = "A-Z字母";
        final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
        if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
            final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
            //判断是否更新了数据，若更新则isUpload至为false
            //统计数据
            int sch_1_new = TddeAchievementImpl.countByTypeId(achievement_template_id);
            //记录数据
            String sch = tddeAchievementList.get(0).getSchedule();
            AchievementSchedule schedule = JSON.parseObject(sch, AchievementSchedule.class);
            String sch_1_old;
            if (schedule == null) {
                sch_1_old = "0";
            } else {
                sch_1_old = schedule.getSch().get(0).getSch_1();
            }
            if (Integer.valueOf(sch_1_old) < sch_1_new) {
                tddeAchievementList.get(0).setUpload(false);
                String con = tddeAchievementList.get(0).getConditions();
                AchievementCondition condition = JSON.parseObject(con, AchievementCondition.class);
                if (condition == null) return;
                String con_1 = condition.getCon().get(0).getCon_1();
                int status;
                if (sch_1_new == Integer.valueOf(con_1)) {
                    status = 2;
                } else if (sch_1_new > 0 && sch_1_new < Integer.valueOf(con_1)) {
                    status = 1;
                } else {
                    status = 0;
                }
                tddeAchievementList.get(0).setStatus(status);
                tddeAchievementList.get(0).setSchedule("{\"sch\":[{\"sch_1\":\"" + sch_1_new + "\"}]}");
                tddeAchievementList.get(0).setUpload(false);
                tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                        , String.valueOf(achievement_template_id));

            }
        } else {
            //从服务端获取成就勋章模板
            HttpApiHelper.doRobotAchievementTemplateApiGet(name).listen(new PostOrGetCallback() {
                @Override
                public void onFinish() {
                    final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
                    if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
                        final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
                        //判断是否更新了数据，若更新则isUpload至为false
                        //统计数据
                        int sch_1_new = TddeAchievementImpl.countByTypeId(achievement_template_id);
                        //记录数据
                        String sch = tddeAchievementList.get(0).getSchedule();
                        AchievementSchedule schedule = JSON.parseObject(sch, AchievementSchedule.class);
                        String sch_1_old;
                        if (schedule == null) {
                            sch_1_old = "0";
                        } else {
                            sch_1_old = schedule.getSch().get(0).getSch_1();
                        }
                        if (Integer.valueOf(sch_1_old) < sch_1_new) {
                            tddeAchievementList.get(0).setUpload(false);
                            String con = tddeAchievementList.get(0).getConditions();
                            AchievementCondition condition = JSON.parseObject(con, AchievementCondition.class);
                            if (condition == null) return;
                            String con_1 = condition.getCon().get(0).getCon_1();
                            int status;
                            if (sch_1_new == Integer.valueOf(con_1)) {
                                status = 2;
                            } else if (sch_1_new > 0 && sch_1_new < Integer.valueOf(con_1)) {
                                status = 1;
                            } else {
                                status = 0;
                            }
                            tddeAchievementList.get(0).setStatus(status);
                            tddeAchievementList.get(0).setSchedule("{\"sch\":[{\"sch_1\":\"" + sch_1_new + "\"}]}");
                            tddeAchievementList.get(0).setUpload(false);
                            tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                                    , String.valueOf(achievement_template_id));
                        }
                    }
                }
            });
        }
    }

    /**
     * 保存字母(A-Z)子勋章
     *
     * @param name A-Z
     */
    public static void UpdateLetterAchievementRecords(final String name) {
        //检查输入有效性
        if (!IsUpper(name)) {
            Log.e("UploadLetterAchievement", "参数无效:" + name);
            return;
        }
        final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
        if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
            boolean isUpload = tddeAchievementList.get(0).isUpload();
            final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
            if (!isUpload) {
                tddeAchievementList.get(0).setStatus(2);
                tddeAchievementList.get(0).setSchedule("{\"sch\":[{\"sch_1\":\"1\"}]}");
                tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                        , String.valueOf(achievement_template_id));
                UpdateAZAchievementRecord();
            }

        } else {
            //从服务端获取成就勋章模板
            HttpApiHelper.doRobotAchievementTemplateApiGet(name).listen(new PostOrGetCallback() {
                @Override
                public void onFinish() {
                    final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
                    if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
                        boolean isUpload = tddeAchievementList.get(0).isUpload();
                        final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
                        if (!isUpload) {
                            tddeAchievementList.get(0).setStatus(2);
                            tddeAchievementList.get(0).setSchedule("{\"sch\":[{\"sch_1\":\"1\"}]}");
                            tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                                    , String.valueOf(achievement_template_id));
                            UpdateAZAchievementRecord();
                        }
                    }
                }
            });
        }
    }

    /**
     * 保存 CVC 单词 父勋章成就记录
     */
    public static void UpdateCVCAchievementRecord() {
        final String name = "CVC单词";

        final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
        if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
            final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
            //判断是否更新了数据，若更新则isUpload至为false
            //统计数据
            int sch_1_new = TddeAchievementImpl.countByTypeId(achievement_template_id);
            //记录数据
            String sch = tddeAchievementList.get(0).getSchedule();
            AchievementSchedule schedule = JSON.parseObject(sch, AchievementSchedule.class);
            String sch_1_old;
            if (schedule == null) {
                sch_1_old = "0";
            } else {
                sch_1_old = schedule.getSch().get(0).getSch_1();
            }
            if (Integer.valueOf(sch_1_old) < sch_1_new) {
                tddeAchievementList.get(0).setUpload(false);
                String con = tddeAchievementList.get(0).getConditions();
                AchievementCondition condition = JSON.parseObject(con, AchievementCondition.class);
                if (condition == null) return;
                String con_1 = condition.getCon().get(0).getCon_1();
                int status;
                if (sch_1_new == Integer.valueOf(con_1)) {
                    status = 2;
                } else if (sch_1_new > 0 && sch_1_new < Integer.valueOf(con_1)) {
                    status = 1;
                } else {
                    status = 0;
                }
                tddeAchievementList.get(0).setStatus(status);
                tddeAchievementList.get(0).setSchedule("{\"sch\":[{\"sch_1\":\"" + sch_1_new + "\"}]}");
                tddeAchievementList.get(0).setUpload(false);
                tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                        , String.valueOf(achievement_template_id));
            }
        } else {
            //从服务端获取成就勋章模板
            HttpApiHelper.doRobotAchievementTemplateApiGet(name).listen(new PostOrGetCallback() {
                @Override
                public void onFinish() {
                    final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
                    if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
                        final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
                        //判断是否更新了数据，若更新则isUpload至为false
                        //统计数据
                        int sch_1_new = TddeAchievementImpl.countByTypeId(achievement_template_id);
                        //记录数据
                        String sch = tddeAchievementList.get(0).getSchedule();
                        AchievementSchedule schedule = JSON.parseObject(sch, AchievementSchedule.class);
                        String sch_1_old;
                        if (schedule == null) {
                            sch_1_old = "0";
                        } else {
                            sch_1_old = schedule.getSch().get(0).getSch_1();
                        }
                        if (Integer.valueOf(sch_1_old) < sch_1_new) {
                            tddeAchievementList.get(0).setUpload(false);
                            String con = tddeAchievementList.get(0).getConditions();
                            AchievementCondition condition = JSON.parseObject(con, AchievementCondition.class);
                            if (condition == null) return;
                            String con_1 = condition.getCon().get(0).getCon_1();
                            int status;
                            if (sch_1_new == Integer.valueOf(con_1)) {
                                status = 2;
                            } else if (sch_1_new > 0 && sch_1_new < Integer.valueOf(con_1)) {
                                status = 1;
                            } else {
                                status = 0;
                            }
                            tddeAchievementList.get(0).setStatus(status);
                            tddeAchievementList.get(0).setSchedule("{\"sch\":[{\"sch_1\":\"" + sch_1_new + "\"}]}");
                            tddeAchievementList.get(0).setUpload(false);
                            tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                                    , String.valueOf(achievement_template_id));
                        }
                    }
                }
            });
        }
    }


    /**
     * 保存 CVC 单词 子勋章
     *
     * @param name  "元音a","元音e","元音i","元音o","元音u"
     * @param sch_1
     */
    public static void UpdateCVCAchievementRecords(final String name, final int sch_1) {
        //检查输入有效性
        boolean valid = false;
        String[] name_list = {"元音a", "元音e", "元音i", "元音o", "元音u"};
        for (String n : name_list) {
            if (n.equals(name)) valid = true;
        }
        if (!valid) {
            Log.e("UploadCVCAchievement", "参数无效!");
            return;
        }
        int sch_1_temp = sch_1;
        final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
        if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
            final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
            String con = tddeAchievementList.get(0).getConditions();
            AchievementCondition condition = JSON.parseObject(con, AchievementCondition.class);
            if (condition == null) return;
            String con_1 = condition.getCon().get(0).getCon_1();
            if (sch_1 > Integer.valueOf(con_1)) sch_1_temp = Integer.valueOf(con_1);
            int status;
            if (sch_1_temp == Integer.valueOf(con_1)) {
                status = 2;
            } else if (sch_1_temp > 0 && sch_1_temp < Integer.valueOf(con_1)) {
                status = 1;
            } else {
                status = 0;
            }
            tddeAchievementList.get(0).setStatus(status);
            tddeAchievementList.get(0).setSchedule("{\"sch\":[{\"sch_1\":\"" + sch_1_temp + "\"}]}");
            tddeAchievementList.get(0).setUpload(false);
            tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                    , String.valueOf(achievement_template_id));
            UpdateCVCAchievementRecord();
        } else {
            //从服务端获取成就勋章模板
            HttpApiHelper.doRobotAchievementTemplateApiGet(name).listen(new PostOrGetCallback() {
                @Override
                public void onFinish() {
                    int sch_1_temp = sch_1;
                    final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
                    if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
                        final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
                        String con = tddeAchievementList.get(0).getConditions();
                        AchievementCondition condition = JSON.parseObject(con, AchievementCondition.class);
                        if (condition == null) return;
                        String con_1 = condition.getCon().get(0).getCon_1();
                        if (sch_1 > Integer.valueOf(con_1)) sch_1_temp = Integer.valueOf(con_1);
                        int status;
                        if (sch_1_temp == Integer.valueOf(con_1)) {
                            status = 2;
                        } else if (sch_1_temp > 0 && sch_1_temp < Integer.valueOf(con_1)) {
                            status = 1;
                        } else {
                            status = 0;
                        }
                        tddeAchievementList.get(0).setStatus(status);
                        tddeAchievementList.get(0).setSchedule("{\"sch\":[{\"sch_1\":\"" + sch_1_temp + "\"}]}");
                        tddeAchievementList.get(0).setUpload(false);
                        tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                                , String.valueOf(achievement_template_id));
                        UpdateCVCAchievementRecord();
                    }
                }
            });
        }
    }


    /**
     * 保存开启知识勋章
     *
     * @param name  "辅音丛" "二合辅音" "三合辅音" "长元音" "不发音的E" "R控制的元音" "其他元音字母组合"
     * @param sch_1 勋章完成个数
     */
    public static void UpdateLetterAchievementRecords(final String name, final int sch_1) {
        //检查输入有效性
        boolean valid = false;
        String[] name_list = {"辅音丛", "二合辅音", "三合辅音", "长元音", "不发音的E", "R控制的元音", "其他元音字母组合"};
        for (String n : name_list) {
            if (n.equals(name)) valid = true;
        }
        if (!valid) {
            Log.e("UploadLetterAchievement", "参数无效!");
            return;
        }
        int sch_1_temp = sch_1;
        final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
        if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
            final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
            String con = tddeAchievementList.get(0).getConditions();
            AchievementCondition condition = JSON.parseObject(con, AchievementCondition.class);
            if (condition == null) return;
            String con_1 = condition.getCon().get(0).getCon_1();
            if (sch_1 > Integer.valueOf(con_1)) sch_1_temp = Integer.valueOf(con_1);
            int status;
            if (sch_1_temp == Integer.valueOf(con_1)) {
                status = 2;
            } else if (sch_1_temp > 0 && sch_1_temp < Integer.valueOf(con_1)) {
                status = 1;
            } else {
                status = 0;
            }
            tddeAchievementList.get(0).setStatus(status);
            tddeAchievementList.get(0).setSchedule("{\"sch\":[{\"sch_1\":\"" + sch_1_temp + "\"}]}");
            tddeAchievementList.get(0).setUpload(false);
            tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                    , String.valueOf(achievement_template_id));
        } else {
            //从服务端获取成就勋章模板
            HttpApiHelper.doRobotAchievementTemplateApiGet(name).listen(new PostOrGetCallback() {
                @Override
                public void onFinish() {
                    int sch_1_temp = sch_1;
                    final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
                    if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
                        final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
                        String con = tddeAchievementList.get(0).getConditions();
                        AchievementCondition condition = JSON.parseObject(con, AchievementCondition.class);
                        if (condition == null) return;
                        String con_1 = condition.getCon().get(0).getCon_1();
                        if (sch_1 > Integer.valueOf(con_1)) sch_1_temp = Integer.valueOf(con_1);
                        int status;
                        if (sch_1_temp == Integer.valueOf(con_1)) {
                            status = 2;
                        } else if (sch_1_temp > 0 && sch_1_temp < Integer.valueOf(con_1)) {
                            status = 1;
                        } else {
                            status = 0;
                        }
                        tddeAchievementList.get(0).setStatus(status);
                        tddeAchievementList.get(0).setSchedule("{\"sch\":[{\"sch_1\":\"" + sch_1_temp + "\"}]}");
                        tddeAchievementList.get(0).setUpload(false);
                        tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                                , String.valueOf(achievement_template_id));
                    }
                }
            });
        }
    }
//---------------------------------------------------弃用--------------------------------------------

    /**
     * 上传开启学习之路勋章
     *
     * @param name "磨耳朵","绘本","Tdde ABC游戏","Dino Crack游戏","Letter Box游戏"
     */
    public static void UploadStudyAchievementRecords(final String name) {
        //检查输入有效性
        boolean valid = false;
        String[] name_list = {"磨耳朵", "绘本", "Tdde ABC游戏", "Dino Crack游戏", "Letter Box游戏"};
        for (String n : name_list) {
            if (n.equals(name)) valid = true;
        }
        if (!valid) {
            Log.e("UploadStudyAchievement", "参数无效!");
            return;
        }
        //判断勋章是否已经上传,如果没有上传则从服务器获取到成就勋章模板,然后再上传.
        final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
        if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
            boolean isUpload = tddeAchievementList.get(0).isUpload();
            final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
            if (!isUpload) {
                Records records = new Records();
                List<Records.Record> records_list = new ArrayList<>();
                Records.Record record = new Records.Record();
                record.setRobot_id(DCApplication.mDataManager.getRobotID());
                record.setAchievement_template_id(achievement_template_id);
                record.setStatus(2);
                record.setSchedule("{\"sch\":[{\"sch_1\":\"1\"}]}");
                records_list.add(record);
                records.setRecords(records_list);

                showTips(name, 1, 1);
                HttpApiHelper.doRobotAchievementRecordsApiPost(records).listen(new PostOrGetCallback() {
                    @Override
                    public void onFinish() {
                        //上传成功则在数据库中记录
                        tddeAchievementList.get(0).setUpload(true);
                        tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                                , String.valueOf(achievement_template_id));
                    }
                });
            }
        } else {
            //从服务端获取成就勋章模板
            HttpApiHelper.doRobotAchievementTemplateApiGet(name).listen(new PostOrGetCallback() {
                @Override
                public void onFinish() {
                    final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
                    if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
                        final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
                        Records records = new Records();
                        List<Records.Record> records_list = new ArrayList<>();
                        Records.Record record = new Records.Record();
                        record.setRobot_id(DCApplication.mDataManager.getRobotID());
                        record.setAchievement_template_id(achievement_template_id);
                        record.setStatus(2);
                        record.setSchedule("{\"sch\":[{\"sch_1\":\"1\"}]}");
                        records_list.add(record);
                        records.setRecords(records_list);
                        showTips(name, 1, 1);
                        HttpApiHelper.doRobotAchievementRecordsApiPost(records).listen(new PostOrGetCallback() {
                            @Override
                            public void onFinish() {
                                //上传成功则在数据库中记录
                                tddeAchievementList.get(0).setUpload(true);
                                tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                                        , String.valueOf(achievement_template_id));
                            }
                        });
                    }
                }
            });
        }
    }


    /**
     * 上传 A-Z字母 父勋章成就记录
     */
    public static void UploadAZAchievementRecord() {
        final String name = "A-Z字母";
        //判断勋章是否已经上传,如果没有上传则从服务器获取到成就勋章模板,然后再上传.
        final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
        if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
            boolean isUpload = tddeAchievementList.get(0).isUpload();
            final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();

            Records records = new Records();
            List<Records.Record> records_list = new ArrayList<>();
            Records.Record record = new Records.Record();
            record.setRobot_id(DCApplication.mDataManager.getRobotID());
            record.setAchievement_template_id(achievement_template_id);

            int sch_1 = TddeAchievementImpl.countByTypeId(achievement_template_id);
            String con = tddeAchievementList.get(0).getConditions();
            AchievementCondition condition = JSON.parseObject(con, AchievementCondition.class);
            String con_1 = condition.getCon().get(0).getCon_1();
            int status;
            if (sch_1 == Integer.valueOf(con_1)) {
                status = 2;
            } else if (sch_1 > 0 && sch_1 < Integer.valueOf(con_1)) {
                status = 1;
            } else {
                status = 0;
            }
            record.setStatus(status);
            record.setSchedule("{\"sch\":[{\"sch_1\":\"" + sch_1 + "\"}]}");
            records_list.add(record);
            records.setRecords(records_list);
            showTips(name, sch_1, Integer.valueOf(con_1));
            HttpApiHelper.doRobotAchievementRecordsApiPost(records).listen(new PostOrGetCallback() {
                @Override
                public void onFinish() {
                    //上传成功则在数据库中记录
                    //tddeAchievementList.get(0).setUpload(true);
                    tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                            , String.valueOf(achievement_template_id));
                }

            });

        } else {
            //从服务端获取成就勋章模板
            HttpApiHelper.doRobotAchievementTemplateApiGet(name).listen(new PostOrGetCallback() {
                @Override
                public void onFinish() {
                    final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
                    if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
                        final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
                        Records records = new Records();
                        List<Records.Record> records_list = new ArrayList<>();
                        Records.Record record = new Records.Record();
                        record.setRobot_id(DCApplication.mDataManager.getRobotID());
                        record.setAchievement_template_id(achievement_template_id);

                        int sch_1 = TddeAchievementImpl.countByTypeId(achievement_template_id);
                        String con = tddeAchievementList.get(0).getConditions();
                        AchievementCondition condition = JSON.parseObject(con, AchievementCondition.class);
                        String con_1 = condition.getCon().get(0).getCon_1();
                        int status;
                        if (sch_1 == Integer.valueOf(con_1)) {
                            status = 2;
                        } else if (sch_1 > 0 && sch_1 < Integer.valueOf(con_1)) {
                            status = 1;
                        } else {
                            status = 0;
                        }
                        record.setStatus(status);
                        //TODO Fix Bug
                        //TddeAchievementImpl.setStatusByTemplateId(achievement_template_id, status);
                        record.setSchedule("{\"sch\":[{\"sch_1\":\"" + sch_1 + "\"}]}");
                        //record.setStatus(2);
                        //record.setSchedule("{\"sch\":[{\"sch_1\":\"1\"}]}");
                        records_list.add(record);
                        records.setRecords(records_list);
                        showTips(name, sch_1, Integer.valueOf(con_1));
                        HttpApiHelper.doRobotAchievementRecordsApiPost(records).listen(new PostOrGetCallback() {
                            @Override
                            public void onFinish() {
                                //上传成功则在数据库中记录
                                //tddeAchievementList.get(0).setUpload(true);
                                tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                                        , String.valueOf(achievement_template_id));
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * 上传字母(A-Z)子勋章
     *
     * @param name A-Z
     */
    public static void UploadLetterAchievementRecords(final String name) {
        //检查输入有效性
        if (!IsUpper(name)) {
            Log.e("UploadLetterAchievement", "参数无效:" + name);
            return;
        }
        //判断勋章是否已经上传,如果没有上传则从服务器获取到成就勋章模板,然后再上传.
        final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
        if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
            boolean isUpload = tddeAchievementList.get(0).isUpload();
            final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
            if (!isUpload) {
                Records records = new Records();
                List<Records.Record> records_list = new ArrayList<>();
                Records.Record record = new Records.Record();
                record.setRobot_id(DCApplication.mDataManager.getRobotID());
                record.setAchievement_template_id(achievement_template_id);
                record.setStatus(2);
                TddeAchievementImpl.setStatusByTemplateId(achievement_template_id, 2);
                record.setSchedule("{\"sch\":[{\"sch_1\":\"1\"}]}");
                records_list.add(record);
                records.setRecords(records_list);
                showTips(name, 1, 1);
                HttpApiHelper.doRobotAchievementRecordsApiPost(records).listen(new PostOrGetCallback() {
                    @Override
                    public void onFinish() {
                        //上传成功则在数据库中记录
                        tddeAchievementList.get(0).setUpload(true);
                        tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                                , String.valueOf(achievement_template_id));

                        //每次上传完A-Z子勋章后更新A-Z字母父勋章
                        UploadAZAchievementRecord();
                    }
                });
            }
        } else {
            //从服务端获取成就勋章模板
            HttpApiHelper.doRobotAchievementTemplateApiGet(name).listen(new PostOrGetCallback() {
                @Override
                public void onFinish() {
                    final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
                    if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
                        final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
                        Records records = new Records();
                        List<Records.Record> records_list = new ArrayList<>();
                        Records.Record record = new Records.Record();
                        record.setRobot_id(DCApplication.mDataManager.getRobotID());
                        record.setAchievement_template_id(achievement_template_id);
                        record.setStatus(2);
                        TddeAchievementImpl.setStatusByTemplateId(achievement_template_id, 2);
                        record.setSchedule("{\"sch\":[{\"sch_1\":\"1\"}]}");
                        records_list.add(record);
                        records.setRecords(records_list);
                        showTips(name, 1, 1);
                        HttpApiHelper.doRobotAchievementRecordsApiPost(records).listen(new PostOrGetCallback() {
                            @Override
                            public void onFinish() {
                                //上传成功则在数据库中记录
                                tddeAchievementList.get(0).setUpload(true);
                                tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                                        , String.valueOf(achievement_template_id));

                                //每次上传完A-Z子勋章后更新A-Z字母父勋章
                                UploadAZAchievementRecord();
                            }


                        });
                    }
                }


            });
        }
    }

    /**
     * 上传 CVC 单词 父勋章成就记录
     */
    public static void UploadCVCAchievementRecord() {
        final String name = "CVC单词";
        //判断勋章是否已经上传,如果没有上传则从服务器获取到成就勋章模板,然后再上传.
        final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
        if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
            boolean isUpload = tddeAchievementList.get(0).isUpload();
            final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
            Records records = new Records();
            List<Records.Record> records_list = new ArrayList<>();
            Records.Record record = new Records.Record();
            record.setRobot_id(DCApplication.mDataManager.getRobotID());
            record.setAchievement_template_id(achievement_template_id);
            int sch_1 = TddeAchievementImpl.countByTypeId(achievement_template_id);
            String con = tddeAchievementList.get(0).getConditions();
            AchievementCondition condition = JSON.parseObject(con, AchievementCondition.class);
            String con_1 = condition.getCon().get(0).getCon_1();
            int status;
            if (sch_1 == Integer.valueOf(con_1)) {
                status = 2;
            } else if (sch_1 > 0 && sch_1 < Integer.valueOf(con_1)) {
                status = 1;
            } else {
                status = 0;
            }
            record.setStatus(status);
            record.setSchedule("{\"sch\":[{\"sch_1\":\"" + sch_1 + "\"}]}");
            records_list.add(record);
            records.setRecords(records_list);
            showTips(name, sch_1, Integer.valueOf(con_1));
            HttpApiHelper.doRobotAchievementRecordsApiPost(records).listen(new PostOrGetCallback() {
                @Override
                public void onFinish() {
                    //上传成功则在数据库中记录
                    //tddeAchievementList.get(0).setUpload(true);
                    tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                            , String.valueOf(achievement_template_id));
                }


            });

        } else {
            //从服务端获取成就勋章模板
            HttpApiHelper.doRobotAchievementTemplateApiGet(name).listen(new PostOrGetCallback() {
                @Override
                public void onFinish() {
                    final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
                    if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
                        final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
                        Records records = new Records();
                        List<Records.Record> records_list = new ArrayList<>();
                        Records.Record record = new Records.Record();
                        record.setRobot_id(DCApplication.mDataManager.getRobotID());
                        record.setAchievement_template_id(achievement_template_id);

                        int sch_1 = TddeAchievementImpl.countByTypeId(achievement_template_id);

                        String con = tddeAchievementList.get(0).getConditions();
                        AchievementCondition condition = JSON.parseObject(con, AchievementCondition.class);
                        String con_1 = condition.getCon().get(0).getCon_1();
                        int status;
                        if (sch_1 == Integer.valueOf(con_1)) {
                            status = 2;
                        } else if (sch_1 > 0 && sch_1 < Integer.valueOf(con_1)) {
                            status = 1;
                        } else {
                            status = 0;
                        }
                        record.setStatus(status);
                        //Fix Bug
                        //TddeAchievementImpl.setStatusByTemplateId(achievement_template_id, status);

                        record.setSchedule("{\"sch\":[{\"sch_1\":\"" + sch_1 + "\"}]}");
                        //record.setStatus(2);
                        //record.setSchedule("{\"sch\":[{\"sch_1\":\"1\"}]}");
                        records_list.add(record);
                        records.setRecords(records_list);
                        showTips(name, sch_1, Integer.valueOf(con_1));
                        HttpApiHelper.doRobotAchievementRecordsApiPost(records).listen(new PostOrGetCallback() {
                            @Override
                            public void onFinish() {
                                //上传成功则在数据库中记录
                                //tddeAchievementList.get(0).setUpload(true);
                                tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                                        , String.valueOf(achievement_template_id));
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * 上传 CVC 单词 子勋章
     *
     * @param name  "元音a","元音e","元音i","元音o","元音u"
     * @param sch_1
     */
    public static void UploadCVCAchievementRecords(final String name, final int sch_1) {
        //检查输入有效性
        boolean valid = false;
        String[] name_list = {"元音a", "元音e", "元音i", "元音o", "元音u"};
        for (String n : name_list) {
            if (n.equals(name)) valid = true;
        }
        if (!valid) {
            Log.e("UploadCVCAchievement", "参数无效!");
            return;
        }
        int sch_1_temp = sch_1;
        //判断勋章是否已经上传,如果没有上传则从服务器获取到成就勋章模板,然后再上传.
        final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
        if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
            boolean isUpload = tddeAchievementList.get(0).isUpload();
            final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
            //if (!isUpload) {

            Records records = new Records();
            List<Records.Record> records_list = new ArrayList<>();
            Records.Record record = new Records.Record();
            record.setRobot_id(DCApplication.mDataManager.getRobotID());
            record.setAchievement_template_id(achievement_template_id);


            String con = tddeAchievementList.get(0).getConditions();
            AchievementCondition condition = JSON.parseObject(con, AchievementCondition.class);
            String con_1 = condition.getCon().get(0).getCon_1();

            if (sch_1 > Integer.valueOf(con_1)) sch_1_temp = Integer.valueOf(con_1);
            int status;
            if (sch_1_temp == Integer.valueOf(con_1)) {
                status = 2;
            } else if (sch_1_temp > 0 && sch_1_temp < Integer.valueOf(con_1)) {
                status = 1;
            } else {
                status = 0;
            }
            record.setStatus(status);
            record.setSchedule("{\"sch\":[{\"sch_1\":\"" + sch_1_temp + "\"}]}");
            records_list.add(record);
            records.setRecords(records_list);

            showTips(name, sch_1_temp, Integer.valueOf(con_1));
            HttpApiHelper.doRobotAchievementRecordsApiPost(records).listen(new PostOrGetCallback() {
                @Override
                public void onFinish() {
                    //上传成功则在数据库中记录
                    //tddeAchievementList.get(0).setUpload(true);
                    tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                            , String.valueOf(achievement_template_id));

                    //每次上传完子勋章后更新父勋章
                    UploadCVCAchievementRecord();
                }
            });
            //}
        } else {
            //从服务端获取成就勋章模板
            HttpApiHelper.doRobotAchievementTemplateApiGet(name).listen(new PostOrGetCallback() {
                @Override
                public void onFinish() {
                    final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
                    if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
                        final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
                        Records records = new Records();
                        List<Records.Record> records_list = new ArrayList<>();
                        Records.Record record = new Records.Record();
                        record.setRobot_id(DCApplication.mDataManager.getRobotID());
                        record.setAchievement_template_id(achievement_template_id);

                        String con = tddeAchievementList.get(0).getConditions();
                        AchievementCondition condition = JSON.parseObject(con, AchievementCondition.class);
                        String con_1 = condition.getCon().get(0).getCon_1();

                        int sch_1_temp = sch_1;
                        if (sch_1 > Integer.valueOf(con_1)) sch_1_temp = Integer.valueOf(con_1);
                        int status;
                        if (sch_1_temp == Integer.valueOf(con_1)) {
                            status = 2;
                        } else if (sch_1_temp > 0 && sch_1_temp < Integer.valueOf(con_1)) {
                            status = 1;
                        } else {
                            status = 0;
                        }
                        record.setStatus(status);
                        //TODO Fix Bug
                        //TddeAchievementImpl.setStatusByTemplateId(achievement_template_id, status);
                        record.setSchedule("{\"sch\":[{\"sch_1\":\"" + sch_1_temp + "\"}]}");
                        //record.setStatus(2);
                        //record.setSchedule("{\"sch\":[{\"sch_1\":\"1\"}]}");
                        records_list.add(record);
                        records.setRecords(records_list);

                        showTips(name, sch_1_temp, Integer.valueOf(con_1));
                        HttpApiHelper.doRobotAchievementRecordsApiPost(records).listen(new PostOrGetCallback() {
                            @Override
                            public void onFinish() {
                                //上传成功则在数据库中记录
                                //tddeAchievementList.get(0).setUpload(true);
                                tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                                        , String.valueOf(achievement_template_id));

                                //每次上传完子勋章后更新父勋章
                                UploadCVCAchievementRecord();
                            }
                        });
                    }
                }
            });
        }
    }


    /**
     * 上传开启知识勋章
     *
     * @param name  "辅音丛" "二合辅音" "三合辅音" "长元音" "不发音的E" "R控制的元音" "其他元音字母组合"
     * @param sch_1 勋章完成个数
     */
    public static void UploadLetterAchievementRecords(final String name, final int sch_1) {
        //检查输入有效性
        boolean valid = false;
        String[] name_list = {"辅音丛", "二合辅音", "三合辅音", "长元音", "不发音的E", "R控制的元音", "其他元音字母组合"};
        for (String n : name_list) {
            if (n.equals(name)) valid = true;
        }
        if (!valid) {
            Log.e("UploadLetterAchievement", "参数无效!");
            return;
        }

        int sch_1_temp = sch_1;
        //判断勋章是否已经上传,如果没有上传则从服务器获取到成就勋章模板,然后再上传.
        final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
        if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
            final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();

            Records records = new Records();
            List<Records.Record> records_list = new ArrayList<>();
            Records.Record record = new Records.Record();
            record.setRobot_id(DCApplication.mDataManager.getRobotID());
            record.setAchievement_template_id(achievement_template_id);

            String con = tddeAchievementList.get(0).getConditions();
            AchievementCondition condition = JSON.parseObject(con, AchievementCondition.class);
            String con_1 = condition.getCon().get(0).getCon_1();

            if (sch_1 > Integer.valueOf(con_1)) sch_1_temp = Integer.valueOf(con_1);
            int status;
            if (sch_1_temp == Integer.valueOf(con_1)) {
                status = 2;
            } else if (sch_1_temp > 0 && sch_1_temp < Integer.valueOf(con_1)) {
                status = 1;
            } else {
                status = 0;
            }
            record.setStatus(status);
            record.setSchedule("{\"sch\":[{\"sch_1\":\"" + sch_1_temp + "\"}]}");
            records_list.add(record);
            records.setRecords(records_list);

            showTips(name, sch_1_temp, Integer.valueOf(con_1));
            HttpApiHelper.doRobotAchievementRecordsApiPost(records).listen(new PostOrGetCallback() {
                @Override
                public void onFinish() {
                    //上传成功则在数据库中记录
                    //tddeAchievementList.get(0).setUpload(true);
                    tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                            , String.valueOf(achievement_template_id));
                }
            });

        } else {
            //从服务端获取成就勋章模板
            HttpApiHelper.doRobotAchievementTemplateApiGet(name).listen(new PostOrGetCallback() {
                @Override
                public void onFinish() {
                    final List<TddeAchievement> tddeAchievementList = TddeAchievementImpl.getByName(name);
                    if (tddeAchievementList != null && tddeAchievementList.size() == 1) {
                        final int achievement_template_id = tddeAchievementList.get(0).getAchievement_template_id();
                        Records records = new Records();
                        List<Records.Record> records_list = new ArrayList<>();
                        Records.Record record = new Records.Record();
                        record.setRobot_id(DCApplication.mDataManager.getRobotID());
                        record.setAchievement_template_id(achievement_template_id);

                        String con = tddeAchievementList.get(0).getConditions();
                        AchievementCondition condition = JSON.parseObject(con, AchievementCondition.class);
                        String con_1 = condition.getCon().get(0).getCon_1();

                        int sch_1_temp = sch_1;
                        if (sch_1 > Integer.valueOf(con_1)) sch_1_temp = Integer.valueOf(con_1);
                        int status;
                        if (sch_1_temp == Integer.valueOf(con_1)) {
                            status = 2;
                        } else if (sch_1_temp > 0 && sch_1_temp < Integer.valueOf(con_1)) {
                            status = 1;
                        } else {
                            status = 0;
                        }
                        record.setStatus(status);
                        //TODO Fix Bug
                        //TddeAchievementImpl.setStatusByTemplateId(achievement_template_id, status);
                        record.setSchedule("{\"sch\":[{\"sch_1\":\"" + sch_1_temp + "\"}]}");
                        //record.setStatus(2);
                        //record.setSchedule("{\"sch\":[{\"sch_1\":\"1\"}]}");
                        records_list.add(record);
                        records.setRecords(records_list);

                        showTips(name, sch_1_temp, Integer.valueOf(con_1));
                        HttpApiHelper.doRobotAchievementRecordsApiPost(records).listen(new PostOrGetCallback() {
                            @Override
                            public void onFinish() {
                                //上传成功则在数据库中记录
                                //tddeAchievementList.get(0).setUpload(true);
                                tddeAchievementList.get(0).saveOrUpdate("achievement_template_id=?"
                                        , String.valueOf(achievement_template_id));
                            }


                        });
                    }
                }
            });
        }
    }

}
