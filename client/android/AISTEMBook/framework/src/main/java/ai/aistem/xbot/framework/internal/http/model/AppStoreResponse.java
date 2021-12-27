package ai.aistem.xbot.framework.internal.http.model;


import java.util.List;

public class AppStoreResponse {

    private int status;
    private String message;
    private AppsInfo result;

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

    public AppsInfo getResult() {
        return result;
    }

    public void setResult(AppsInfo result) {
        this.result = result;
    }

    public class AppsInfo {
        private int count;
        private List<AppInfo> list;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public List<AppInfo> getList() {
            return list;
        }

        public void setList(List<AppInfo> list) {
            this.list = list;
        }

        public class AppInfo {
            private int id;
            private String app_name;
            private String package_name;
            private int version;
            private String icon_url;
            private String package_url;
            private String package_size;
            private String package_md5;
            private String release_note;
            private String update_time;
            private String create_time;
            private String delete_time;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
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

            public String getUpdate_time() {
                return update_time;
            }

            public void setUpdate_time(String update_time) {
                this.update_time = update_time;
            }

            public String getCreate_time() {
                return create_time;
            }

            public void setCreate_time(String create_time) {
                this.create_time = create_time;
            }

            public String getDelete_time() {
                return delete_time;
            }

            public void setDelete_time(String delete_time) {
                this.delete_time = delete_time;
            }
        }
    }


}
