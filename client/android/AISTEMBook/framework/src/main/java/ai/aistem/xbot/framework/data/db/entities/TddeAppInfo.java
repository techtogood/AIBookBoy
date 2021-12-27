package ai.aistem.xbot.framework.data.db.entities;

import org.litepal.crud.LitePalSupport;

/*{
        "status": 200,
        "result": {
        "count": 3,
        "list": [
        {
        "id": 1001,
        "app_name": "app1",
        "package_name": "APP1",
        "version": 1,
        "icon_url": "https://aistem-voice.oss-cn-shenzhen.aliyuncs.com/cover/default.png",
        "package_url": "http://192.168.51.227:5000/fsdownload/981aqEgN8/app-debug.apk",
        "package_size": null,
        "package_md5": "",
        "release_note": "1.更新1;",
        "update_time": "2018-09-19T03:24:20.000Z",
        "create_time": "2018-09-19T03:24:23.000Z",
        "delete_time": null
        },
        {
        "id": 1002,
        "app_name": "app2",
        "package_name": "APP2",
        "version": 1,
        "icon_url": "https://aistem-voice.oss-cn-shenzhen.aliyuncs.com/cover/default.png",
        "package_url": "http://192.168.51.227:5000/fsdownload/981aqEgN8/app-debug.apk",
        "package_size": null,
        "package_md5": null,
        "release_note": "1.更新1;",
        "update_time": "2018-09-25T01:36:10.000Z",
        "create_time": "2018-09-25T01:36:13.000Z",
        "delete_time": null
        },
        {
        "id": 1003,
        "app_name": "app3",
        "package_name": "APP3",
        "version": 1,
        "icon_url": "https://aistem-voice.oss-cn-shenzhen.aliyuncs.com/cover/default.png",
        "package_url": "http://192.168.51.227:5000/fsdownload/981aqEgN8/app-debug.apk",
        "package_size": null,
        "package_md5": null,
        "release_note": "1.更新1;",
        "update_time": "2018-09-25T01:36:15.000Z",
        "create_time": "2018-09-25T01:36:18.000Z",
        "delete_time": null
       }
    ]
  }
}*/
public class TddeAppInfo extends LitePalSupport {

    private int aid;
    private String app_name;
    private String package_name;
    private int version;
    private String icon_url;
    private String package_url;
    private String package_size;
    private String package_md5;
    private String release_note;
    private long update_time;
    private long create_time;
    private String delete_time;
    private boolean updated;
    private boolean installed;

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public int getAid() {
        return aid;
    }

    public void setAid(int aid) {
        this.aid = aid;
    }

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    public String getPackage_url() {
        return package_url;
    }

    public void setPackage_url(String package_url) {
        this.package_url = package_url;
    }

    public String getPackage_size() {
        return package_size;
    }

    public void setPackage_size(String package_size) {
        this.package_size = package_size;
    }

    public String getPackage_md5() {
        return package_md5;
    }

    public void setPackage_md5(String package_md5) {
        this.package_md5 = package_md5;
    }

    public String getRelease_note() {
        return release_note;
    }

    public void setRelease_note(String release_note) {
        this.release_note = release_note;
    }

    public long getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(long update_time) {
        this.update_time = update_time;
    }

    public long getCreate_time() {
        return create_time;
    }

    public void setCreate_time(long create_time) {
        this.create_time = create_time;
    }

    public String getDelete_time() {
        return delete_time;
    }

    public void setDelete_time(String delete_time) {
        this.delete_time = delete_time;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }
}
