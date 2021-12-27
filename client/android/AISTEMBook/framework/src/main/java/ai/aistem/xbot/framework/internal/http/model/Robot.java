package ai.aistem.xbot.framework.internal.http.model;

public class Robot {
    private int id;
    private String code;
    private String avatar;
    private String nickname;
    private String birthday;
    private int gender;
    private String version;
    private String alive_time;
    private String token;
    private String refresh_token;
    private String expire_time;
    private String mqtt_username;
    private String mqtt_password;
    private int night_status;
    private String night_start_time;
    private String night_end_time;
    private int anti_addiction_status;
    private int anti_addiction_duration;
    private String ip;
    private String model;//型号
    private String public_id;
    private String sn_no;
    private String mac;
    private String update_time;
    private String create_time;
    private String delete_time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAlive_time() {
        return alive_time;
    }

    public void setAlive_time(String alive_time) {
        this.alive_time = alive_time;
    }



    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getExpire_time() {
        return expire_time;
    }

    public void setExpire_time(String expire_time) {
        this.expire_time = expire_time;
    }

    public String getMqtt_username() {
        return mqtt_username;
    }

    public void setMqtt_username(String mqtt_username) {
        this.mqtt_username = mqtt_username;
    }

    public String getMqtt_password() {
        return mqtt_password;
    }

    public void setMqtt_password(String mqtt_password) {
        this.mqtt_password = mqtt_password;
    }

    public int getNight_status() {
        return night_status;
    }

    public void setNight_status(int night_status) {
        this.night_status = night_status;
    }

    public String getNight_start_time() {
        return night_start_time;
    }

    public void setNight_start_time(String night_start_time) {
        this.night_start_time = night_start_time;
    }

    public String getNight_end_time() {
        return night_end_time;
    }

    public void setNight_end_time(String night_end_time) {
        this.night_end_time = night_end_time;
    }

    public int getAnti_addiction_status() {
        return anti_addiction_status;
    }

    public void setAnti_addiction_status(int anti_addiction_status) {
        this.anti_addiction_status = anti_addiction_status;
    }

    public int getAnti_addiction_duration() {
        return anti_addiction_duration;
    }

    public void setAnti_addiction_duration(int anti_addiction_duration) {
        this.anti_addiction_duration = anti_addiction_duration;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPublic_id() {
        return public_id;
    }

    public void setPublic_id(String public_id) {
        this.public_id = public_id;
    }

    public String getSn_no() {
        return sn_no;
    }

    public void setSn_no(String sn_no) {
        this.sn_no = sn_no;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "Robot{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", avatar='" + avatar + '\'' +
                ", nickname='" + nickname + '\'' +
                ", birthday='" + birthday + '\'' +
                ", gender=" + gender +
                ", version='" + version + '\'' +
                ", alive_time='" + alive_time + '\'' +
                ", token='" + token + '\'' +
                ", refresh_token='" + refresh_token + '\'' +
                ", expire_time='" + expire_time + '\'' +
                ", mqtt_username='" + mqtt_username + '\'' +
                ", mqtt_password='" + mqtt_password + '\'' +
                ", night_status=" + night_status +
                ", night_start_time='" + night_start_time + '\'' +
                ", night_end_time='" + night_end_time + '\'' +
                ", anti_addiction_status=" + anti_addiction_status +
                ", anti_addiction_duration=" + anti_addiction_duration +
                ", ip='" + ip + '\'' +
                ", model='" + model + '\'' +
                ", public_id='" + public_id + '\'' +
                ", sn_no='" + sn_no + '\'' +
                ", mac='" + mac + '\'' +
                ", update_time='" + update_time + '\'' +
                ", create_time='" + create_time + '\'' +
                ", delete_time='" + delete_time + '\'' +
                '}';
    }
}
