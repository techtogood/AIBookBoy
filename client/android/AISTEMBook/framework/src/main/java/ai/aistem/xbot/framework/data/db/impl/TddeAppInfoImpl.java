package ai.aistem.xbot.framework.data.db.impl;

import android.content.ContentValues;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import ai.aistem.xbot.framework.data.db.entities.TddeAppInfo;

public class TddeAppInfoImpl {

    public static synchronized void modify(TddeAppInfo appInfo) {

        if (checkExistById(appInfo.getAid())) {
            ContentValues values = new ContentValues();
            values.put("app_name", appInfo.getApp_name());
            values.put("package_name", appInfo.getPackage_name());
            values.put("version", appInfo.getVersion());
            values.put("icon_url", appInfo.getIcon_url());
            values.put("package_url", appInfo.getPackage_url());
            values.put("release_note", appInfo.getRelease_note());
            boolean isUpdated = checkUpdated(appInfo.getAid(), appInfo.getVersion());
            values.put("updated", isUpdated);
            LitePal.updateAll(TddeAppInfo.class, values, "aid=?",
                    String.valueOf(appInfo.getAid()));
        } else {
            appInfo.setUpdate_time(System.currentTimeMillis());
            appInfo.setCreate_time(System.currentTimeMillis());
            appInfo.setInstalled(false);
            appInfo.setUpdated(true);
            appInfo.save();
        }
    }

    public static synchronized boolean setUpdated(int aid, boolean set) {
        ContentValues values = new ContentValues();
        if (set) {
            values.put("updated", "1");
        } else {
            values.put("updated", "0");
        }
        LitePal.updateAll(TddeAppInfo.class, values, "aid=?", String.valueOf(aid));
        return true;
    }

    public static synchronized boolean setInstalled(int aid, boolean install) {
        ContentValues values = new ContentValues();
        if (install) {
            values.put("installed", "1");
        } else {
            values.put("installed", "0");
        }
        LitePal.updateAll(TddeAppInfo.class, values,"aid=?",String.valueOf(aid));
        return true;
    }


    public static synchronized void updateInstallInfo(String pacName,boolean isInstall){
        ContentValues values = new ContentValues();
        if (isInstall) {
            values.put("installed", "1");
        } else {
            values.put("installed", "0");
        }
        LitePal.updateAll(TddeAppInfo.class, values, "package_name=?",pacName);
    }

    /**
     * @param aid  ???????????????APP ID
     * @return ???APP ID ???aid ???APP??????
     */
    public static synchronized TddeAppInfo selectByID(int aid) {
        List<TddeAppInfo> result = LitePal.where("aid=?", aid+"").find(TddeAppInfo.class);
        if(result.size() >0 )
        {
            return result.get(0);
        }
        return null;
    }

    public static synchronized List<TddeAppInfo> selectOwnApp() {
        return LitePal.where("aid<1000").find(TddeAppInfo.class);
    }

    public static synchronized List<TddeAppInfo> selectLauncherApp() {
        return LitePal.where("aid=1").find(TddeAppInfo.class);
    }


    public static synchronized TddeAppInfo getAppInfoByPackage(String pacName){
        List<TddeAppInfo> infoList=new ArrayList<>();
        infoList.addAll(LitePal.where("package_name=?",pacName).find(TddeAppInfo.class));
        if (infoList.size()>0){
            return infoList.get(0);
        }else {
            return null;
        }
    }

    /**
     * @param minID ?????????APP  ID
     * @return ????????????APP ID??????MinID???APP??????
     */
    public static synchronized List<TddeAppInfo> selectAllApp(int minID) {
        return LitePal.where("aid>?", minID+"")
                .find(TddeAppInfo.class);
    }

    /**
     * @return  ???????????????????????????APP??????
     */
    public static synchronized List<TddeAppInfo> selectUpdatedApp() {
        return LitePal.where("updated=?", "1")
                .order("aid asc").find(TddeAppInfo.class);
    }

    /**
     * @return  ??????????????????????????????APP??????
     */
    public static synchronized List<TddeAppInfo> selectInstallApp() {
        return LitePal.where("installed=?", "1")
                .order("aid asc").find(TddeAppInfo.class);
    }


    /**
     * ??????APP mid??????????????????
     *
     * @param aid     app Id
     * @param version ?????????
     * @return true/false
     */
    public static synchronized boolean checkUpdated(int aid, int version) {
        List<TddeAppInfo> list = LitePal.where("aid=?",
                String.valueOf(aid)).find(TddeAppInfo.class);
        if (list != null && list.size() > 0) {
            int current_version = list.get(0).getVersion();
            if (version > current_version || list.get(0).isUpdated()) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * ??????APP mid??????????????????
     *
     * @param aid
     * @return
     */
    public static synchronized boolean checkUpdated(int aid) {
        List<TddeAppInfo> list = LitePal.where("aid=? and updated=?",
                String.valueOf(aid), "1").find(TddeAppInfo.class);
        if (list != null && list.size() > 0) {
            return true;
        }
        return false;
    }


    private static boolean checkExistById(int aid) {
        List<TddeAppInfo> list = LitePal.where("aid=?",
                String.valueOf(aid)).find(TddeAppInfo.class);
        if (list != null && list.size() > 0) {
            return true;
        }
        return false;
    }
}
