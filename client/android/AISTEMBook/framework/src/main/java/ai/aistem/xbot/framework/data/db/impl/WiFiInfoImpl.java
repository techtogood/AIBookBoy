package ai.aistem.xbot.framework.data.db.impl;

import android.content.ContentValues;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import ai.aistem.xbot.framework.data.db.entities.WiFiInfo;

public class WiFiInfoImpl {


        public static synchronized void midfyWiFiInfo(WiFiInfo wiFiInfo){
            if (checkWiFiInfoExist(wiFiInfo.getBSSID())){
                ContentValues values=new ContentValues();
                values.put("SSID",wiFiInfo.getSSID());
                values.put("BSSID",wiFiInfo.getBSSID());
                values.put("password",wiFiInfo.getPassword());
                values.put("type",wiFiInfo.getType());
                LitePal.updateAll(WiFiInfo.class,values,"BSSID=?",wiFiInfo.getBSSID());
            }else {
                wiFiInfo.save();
            }
        }

        private static boolean checkWiFiInfoExist(String BSSID){
            List<WiFiInfo> wiFiInfoList=LitePal.where("BSSID=?",BSSID).find(WiFiInfo.class);
            if (wiFiInfoList!=null&&wiFiInfoList.size()>0){
                return true;
            }
            return false;
        }


        public static synchronized String findWiFiPassword(String ssid,String bssid){
            List<WiFiInfo> wiFiInfoList=LitePal.where("SSID=? and BSSID=?",ssid,bssid).find(WiFiInfo.class);
            if (wiFiInfoList!=null&&wiFiInfoList.size()>0){
                return wiFiInfoList.get(0).getPassword();
            }else {
                return "-1";
            }
        }

        public static synchronized int findWiFiType(String ssid,String bssid){
            List<WiFiInfo> wiFiInfoList=LitePal.where("SSID=? and BSSID=?",ssid,bssid).find(WiFiInfo.class);
            if (wiFiInfoList!=null&&wiFiInfoList.size()>0){
                return wiFiInfoList.get(0).getType();
            }else {
                return -1;
            }
        }

        public static synchronized List<WiFiInfo> findSavedWiFiList(){
            List<WiFiInfo> wiFiInfoList=new ArrayList<>();
            wiFiInfoList.addAll(LitePal.findAll(WiFiInfo.class));
            return wiFiInfoList;
        }

        public static synchronized void deleteWrongWiFi(String SSID,String BSSID){
            LitePal.deleteAll(WiFiInfo.class,"SSID=? and BSSID=?",SSID,BSSID);
        }

}
