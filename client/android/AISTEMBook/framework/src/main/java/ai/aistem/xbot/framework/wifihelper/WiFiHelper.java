package ai.aistem.xbot.framework.wifihelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ai.aistem.xbot.framework.application.DCApplication;
import ai.aistem.xbot.framework.data.db.entities.WiFiInfo;
import ai.aistem.xbot.framework.data.db.impl.WiFiInfoImpl;

/**
 * @author: aistem
 * @created: 2018/5/16/12:00
 * @desc: WiFiHelper
 */
public class WiFiHelper {

    /**
     * 与WIFI相关的数据FileName
     */
    public static final String WIFI_DATA_FILE = "WIFI_DATA_FILE";
    /**
     * 获取Touch连接网络的方式的key
     */
    public static final String WIFI_CONNECT_MODE_KEY = "WIFI_CONNECT_MODE_KEY";
    /**
     * Touch连接网络的方式：连接外网wifi
     */
    public static final int CONNECT_WIFI_NETWORK = 100;
    /**
     * Touch连接网络的方式：连接Cube的AP
     */
    public static final int CONNECT_CUBE_NETWORK = 101;
    /**
     * 获取WiFi SSID的key
     */
    public static final String WIFI_SSID_KEY = "WIFI_SSID_KEY";
    /**
     * 获取WiFi 密码的key
     */
    public static final String WIFI_PASSWORD_KEY = "WIFI_PASSWORD_KEY";

    /**
     * 获取WiFi加密方式的key
     */
    public static final String WIFI_TYPE_KEY = "WIFI_TYPE_KEY";

    public final static int WIFICIPHER_NOPASS = 1;
    public final static int WIFICIPHER_WEP = 2;
    public final static int WIFICIPHER_WPA = 3;

    private Context context;
    private static WiFiHelper instance;
    private WifiManager wifiManager;

    /**
     * wifi信号排序
     */
    private Comparator<ScanResult> wifi = new Comparator<ScanResult>() {

        @Override
        public int compare(ScanResult lhs, ScanResult rhs) {

            return lhs.level == rhs.level ? 0 : (lhs.level < rhs.level ? 1 : -1);

//																	if (lhs.level >= rhs.level)
//																	{
//
//																		return -1;
//																	}
//																	else
//																	{
//																		return 1;
//																	}
        }
    };

    private WiFiHelper(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        openWifi();
    }

    /**
     * 获取唯一实例
     */
    public static synchronized WiFiHelper getInstance(Context context) {
        if (null == instance) {
            instance = new WiFiHelper(context);
        }
        return instance;
    }


    /**
     * 需求：打开WIFI
     */
    public void openWifi() {

        if (wifiManager != null) {
            startScan();
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

        }
    }

    /**
     * 需求：开始扫描wifi
     */
    public void startScan() {
        wifiManager.startScan();
    }

    /**
     * wifi是否连接
     *
     * @return
     */
    public boolean isConnectWifi() {

        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            return true;
        }
        return false;

    }

    /**
     * 断开当前连接的网络
     */

    public void disconnectWifi() {

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int netId = wifiInfo.getNetworkId();

        if (isConnectWifi()) {
            if (netId >= 0) {
                wifiManager.disableNetwork(netId);
                wifiManager.disconnect();
                wifiManager.removeNetwork(netId);
                wifiManager.saveConfiguration();
                // wifiManager.forget(netId, new ActionListener() {
                //
                // @Override
                // public void onSuccess() {
                //
                // }
                //
                // @Override
                // public void onFailure(int reason) {
                //
                // }
                // });

            }
        }

    }

    /**
     * 获得已连接的SSID
     *
     * @return
     */
    public String getConnectedSSID() {

        if (isConnectWifi()) {

            String connectedSSID = wifiManager.getConnectionInfo().getSSID();
            String connected = connectedSSID.substring(1, connectedSSID.length() - 1);
            return connected;
        }

        return null;

    }

    /**
     * 需求：获取wifi列表
     *
     * @return
     */
    public List<ScanResult> getScanResults() {
        List<ScanResult> listScanResult = new ArrayList<ScanResult>();
//		List<ScanResult> listScanResult = wifiManager.getScanResults();
        listScanResult.addAll(wifiManager.getScanResults());
        List<ScanResult> list_wifi_data = new ArrayList<ScanResult>();

        // 信号排序
        if (listScanResult != null && listScanResult.size() > 0 && wifi != null) {
            Collections.sort(listScanResult, wifi);
        }

        String connectedSSID = getConnectedSSID();
        // 已连接的放在第一位
        for (ScanResult scanResult : listScanResult) {
            if (!scanResult.SSID.equals("")&&scanResult.SSID.length()>0) {
                if (scanResult.SSID.equals(connectedSSID)) {
                    list_wifi_data.add(0, scanResult);
                } else {
                    list_wifi_data.add(scanResult);
                }
            }
        }

        return list_wifi_data;
    }


    /**
     * 需求：获取wifi列表 连接成功的在第一位
     * 保存过密码在其后
     *
     * @return
     */
    public List<ScanResult> getScanResultsList() {
        List<ScanResult> listScanResult = new ArrayList<>();
        listScanResult.addAll(wifiManager.getScanResults());
        List<ScanResult> list_wifi_data = new ArrayList<>();

        List<WiFiInfo> saveWiFiList = WiFiInfoImpl.findSavedWiFiList();

        // 信号排序
        if (listScanResult != null && listScanResult.size() > 0 && wifi != null) {
            Collections.sort(listScanResult, wifi);
        }

        String connectedSSID = getConnectedSSID();

        int index = 0;

        // 已连接的放在第一位
        for (ScanResult scanResult : listScanResult) {
            if (!scanResult.SSID.equals("")&&scanResult.SSID.length()>0) {

//                LogUtil.getInstance().d("Myssid_WiFi_Tag", "wifi_name : " + scanResult.SSID);
                if (scanResult.SSID.equals(connectedSSID)) {
                    list_wifi_data.add(0, scanResult);
                } else {

                    list_wifi_data.add(scanResult);
                }
            }
        }

        ScanResult scanResult;
        for (int i = 0; i < list_wifi_data.size(); i++) {
            if (saveWiFiList.size() > 0) {
                for (int j = 0; j < saveWiFiList.size(); j++) {
                    String saveSsid = saveWiFiList.get(j).getSSID();
                    String listSsid=list_wifi_data.get(i).SSID;
                    if (!saveSsid.equals(connectedSSID)&&listSsid.equals(saveSsid) && list_wifi_data.size() > 0) {
                        if (i!=0){
                            index++;
                            scanResult=list_wifi_data.get(i);
                            list_wifi_data.remove(i);
                            if (index<list_wifi_data.size()){
                                list_wifi_data.add(index, scanResult);
                            }
                        }
                    }
                }
            }
        }

        return list_wifi_data;
    }


    /**
     * 需求：获取Wifi加密类型
     *
     * @param choose
     * @return
     */
    public int getWiFiType(ScanResult choose) {
        String choose_state = choose.capabilities.toLowerCase();
        int type = -1;

        if (choose_state.contains("wpa")) {

            type = WIFICIPHER_WPA;
        } else if (choose_state.contains("wep"))

        {
            type = WIFICIPHER_WEP;
        } else {

            type = WIFICIPHER_NOPASS;

        }
        return type;

    }

    /**
     * 需求：连接wifi
     *
     * @param mSSID
     * @param mPassword
     * @param mWifiType
     */
    public void connect(String mSSID, String mPassword, int mWifiType) {
        WifiConfiguration configuration = createWifiInfo(mSSID, mPassword, mWifiType);

        List<WifiConfiguration> mWifiConfigurations = new ArrayList<WifiConfiguration>();

        mWifiConfigurations.addAll(wifiManager.getConfiguredNetworks());
        if (mWifiConfigurations != null) {
            for (WifiConfiguration mConfiguration : mWifiConfigurations) {

                if (configuration.SSID.equals(mConfiguration.SSID)) {

                    int networkId = mConfiguration.networkId;

                    if (networkId > 0) {
                        wifiManager.removeNetwork(networkId);
//						wifiManager.forget(networkId, new ActionListener() {
//
//							@Override
//							public void onSuccess()
//							{
//
//							}
//
//							@Override
//							public void onFailure(int reason)
//							{
//
//							}
//						});
                        wifiManager.saveConfiguration();
                    }

                }
            }
        }
        wifiManager.enableNetwork(wifiManager.addNetwork(configuration), true);

    }

    /**
     * 清除所有wifi连接信息
     */
    public void clearAllWifiInfo() {

        List<WifiConfiguration> mWifiConfigurations = new ArrayList<WifiConfiguration>();
        try {
            mWifiConfigurations.addAll(wifiManager.getConfiguredNetworks());

            if (mWifiConfigurations != null && mWifiConfigurations.size() > 0) {
                for (WifiConfiguration wifiConfiguration : mWifiConfigurations) {

                    int networkId = wifiConfiguration.networkId;

                    if (networkId > 0) {
                        wifiManager.removeNetwork(networkId);
//						wifiManager.forget(networkId, new ActionListener() {
//							@Override
//							public void onSuccess()
//							{
//
//							}
//
//							@Override
//							public void onFailure(int reason)
//							{
//							}
//						});

                    }

                }
                wifiManager.saveConfiguration();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public WifiConfiguration createWifiInfo(String SSID, String Password, int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        // WifiConfiguration tempConfig = isExsits(SSID);
        // if (tempConfig != null) {
        // mWifiManager.removeNetwork(tempConfig.networkId);
        // }

        if (Type == WIFICIPHER_NOPASS) { // WIFICIPHER_NOPASS
            // config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            // config.wepTxKeyIndex = 0;
        }
        if (Type == WIFICIPHER_WEP) {// WIFICIPHER_WEP
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";

            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;

        }
        if (Type == WIFICIPHER_WPA) {// WIFICIPHER_WPA
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    /**
     * @param mode touch连接网络的类型
     * @see #CONNECT_WIFI_NETWORK
     * @see #CONNECT_CUBE_NETWORK
     */
    public void putConnectMode(int mode) {
        SharedPreferences preferences = DCApplication.app.getSharedPreferences(WIFI_DATA_FILE,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(WIFI_CONNECT_MODE_KEY, mode);
        editor.commit();
    }

    /**
     * @see #CONNECT_WIFI_NETWORK
     * @see #CONNECT_CUBE_NETWORK
     */
    public int getConnectMode() {
        SharedPreferences preferences = DCApplication.app.getSharedPreferences(WIFI_DATA_FILE,
                Context.MODE_PRIVATE);
        return preferences.getInt(WIFI_CONNECT_MODE_KEY, CONNECT_CUBE_NETWORK);
    }


}
