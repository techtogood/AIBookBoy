package ai.aistem.xbot.framework.data.db.entities;

import org.litepal.crud.LitePalSupport;


/**
 * 已保存密码的wifi
 * */
public class WiFiInfo extends LitePalSupport {

    private String SSID;
    private String password;
    private String BSSID;
    private int type;

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
