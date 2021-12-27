package ai.aistem.xbot.framework.internal.http;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.google.gson.JsonObject;
import com.litesuits.common.io.FileUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.squareup.okhttp.Request;

import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import ai.aistem.xbot.framework.application.DCApplication;
import ai.aistem.xbot.framework.application.GlobalParameter;
import ai.aistem.xbot.framework.data.DataManager;
import ai.aistem.xbot.framework.data.bean.TrackBean;
import ai.aistem.xbot.framework.data.bean.TrackInfo;
import ai.aistem.xbot.framework.data.db.entities.TddeAchievement;
import ai.aistem.xbot.framework.data.db.entities.TddeAppInfo;
import ai.aistem.xbot.framework.data.db.entities.TddeMusicInfo;
import ai.aistem.xbot.framework.data.db.entities.TddeMusicTag;
import ai.aistem.xbot.framework.data.db.impl.TddeAchievementImpl;
import ai.aistem.xbot.framework.data.db.impl.TddeAppInfoImpl;
import ai.aistem.xbot.framework.data.db.impl.TddeBookStatisticalImpl;
import ai.aistem.xbot.framework.data.db.impl.TddeMusicInfoImpl;
import ai.aistem.xbot.framework.data.db.impl.TddeMusicStatisticalImpl;
import ai.aistem.xbot.framework.data.db.impl.TddeMusicTagImpl;
import ai.aistem.xbot.framework.data.db.impl.TddeUsingTimeStatisticalImpl;
import ai.aistem.xbot.framework.data.db.impl.LetterBoxWordStatisticalImpl;
import ai.aistem.xbot.framework.data.db.model.PairUser;
import ai.aistem.xbot.framework.data.prefs.AppPreferencesHelper;
import ai.aistem.xbot.framework.internal.http.async.PostOrGetCallback;
import ai.aistem.xbot.framework.internal.http.async.PostOrGetExecutor;
import ai.aistem.xbot.framework.internal.http.model.AchievementTemplateResponse;
import ai.aistem.xbot.framework.internal.http.model.AppStoreResponse;
import ai.aistem.xbot.framework.internal.http.model.AuthTokenResponse;
import ai.aistem.xbot.framework.internal.http.model.BaseAntiAddictionResponse;
import ai.aistem.xbot.framework.internal.http.model.BaseNightResponse;
import ai.aistem.xbot.framework.internal.http.model.LocalSTSTokenResponse;
import ai.aistem.xbot.framework.internal.http.model.MusicTagResponse;
import ai.aistem.xbot.framework.internal.http.model.Records;
import ai.aistem.xbot.framework.internal.http.model.SoundAlbumResponse;
import ai.aistem.xbot.framework.internal.http.model.SoundVoicesResponse;
import ai.aistem.xbot.framework.internal.http.model.UserResponse;
import ai.aistem.xbot.framework.internal.http.model.UsersResponse;
import ai.aistem.xbot.framework.internal.http.model.VoiceResponse;
import ai.aistem.xbot.framework.internal.utils.MessageUtils;
import ai.aistem.xbot.framework.internal.utils.RobotInfoUtil;
import ai.aistem.xbot.framework.message.DCBroadcastMsgImpl;
import ai.aistem.xbot.framework.utils.AntiAddictionUtil;
import ai.aistem.xbot.framework.utils.LogUtil;
import ai.aistem.xbot.framework.utils.UUIDUtil;


public class HttpApiHelper {
    private final String TAG = HttpApiHelper.class.getSimpleName();

    private static HttpApiHelper instance = null;
    private DataManager mDataManager;


    public synchronized static HttpApiHelper getInstance() {
        if (instance == null) {
            instance = new HttpApiHelper();
        }
        return instance;
    }

    private HttpApiHelper() {
        mDataManager = DCApplication.app.getDataManager();
    }

//    public void doRobotAuthTokenApiPost() {
//        //String code = mDataManager.getRobotCode();
//        String code = UUIDUtil.getMacAddress();
//        LogUtil.getInstance().d("MAC Code:" + code);
//        JsonObject json = new JsonObject();
//        json.addProperty("code", code);
//
//        OkHttpClientManager.postAsynJson(HttpApiEndPoint.ENDPOINT_AUTH_TOKEN, json.toString(),
//                new OkHttpClientManager.ResultCallback<String>() {
//                    @Override
//                    public void onError(Request request, Exception e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            AuthTokenResponse content =
//                                    JSON.parseObject(response, AuthTokenResponse.class);
//                            Log.v(TAG, content.toString());
//                            //写入配置文件中
//                            if (content.getStatus() == 200) {
//                                int rid = content.getResult().getId();
//                                String token = content.getResult().getToken();
//                                String expire_time = content.getResult().getExpire_time();
//                                String refresh_token = content.getResult().getRefresh_token();
//                                String mqtt_username = content.getResult().getMqtt_username();
//                                String mqtt_password = content.getResult().getMqtt_password();
//
//                                //初始化 系统设置
//                                int night_status = content.getResult().getNight_status();
//                                String start_time = content.getResult().getNight_start_time();
//                                String end_time = content.getResult().getNight_end_time();
//                                mDataManager.setRobotNigthModeSwitch(night_status > 0);
//                                mDataManager.setRobotNightModeStartTime(start_time);
//                                mDataManager.setRobotNightModeEndTime(end_time);
//                                int anti_addiction_status = content.getResult().getAnti_addiction_status();
//                                int duration = content.getResult().getAnti_addiction_duration();
//                                mDataManager.setRobotAntiAddictionSwitch(anti_addiction_status > 0);
//                                mDataManager.setRobotAntiAddictionDuration(duration);
//
//                                String avatar = content.getResult().getAvatar();
//                                int gender = content.getResult().getGender();
//                                mDataManager.setRobotID(rid);
//                                mDataManager.setRobotGender(gender);
//                                mDataManager.setRobotAvatar(avatar);
//                                mDataManager.setRobotAuthToken(token);
//                                mDataManager.setRobotTokenExpireTime(expire_time);
//                                mDataManager.setRobotRefreshToken(refresh_token);
//                                mDataManager.setRobotMqttUsername(mqtt_username);
//                                mDataManager.setRobotMqttPassword(mqtt_password);
//                                //更新SharedPreferences广播
//                                String[] preferences = {AppPreferencesHelper.PREF_ROBOT_ID,
//                                        AppPreferencesHelper.PREF_ROBOT_AUTH_TOKEN,
//                                        AppPreferencesHelper.PREF_ROBOT_TOKEN_EXPIRE_TIME,
//                                        AppPreferencesHelper.PREF_ROBOT_REFRESH_TOKEN,
//                                        AppPreferencesHelper.PREF_ROBOT_MQTT_USERNAME,
//                                        AppPreferencesHelper.PREF_ROBOT_MQTT_PASSWORD};
//                                DCBroadcastMsgImpl.sendUpdateSPBroadcast(preferences);
//                            } else {
//                                Log.e(TAG, "status code is invalided:" + content.getMessage());
//                            }
//                        } catch (JSONException e) {
//                            Log.e(TAG, "无效JSON串");
//                            e.printStackTrace();
//                        }
//                    }
//                }, null);
//    }

    public static PostOrGetExecutor doRobotAuthTokenApiPostAsync() {
        final PostOrGetExecutor executor = new PostOrGetExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String code = UUIDUtil.getMacAddress();
                LogUtil.getInstance().d("MAC Code:" + code);
                JsonObject json = new JsonObject();
                json.addProperty("code", code);

                OkHttpClientManager.postAsynJson(HttpApiEndPoint.ENDPOINT_AUTH_TOKEN, json.toString(),
                        new OkHttpClientManager.ResultCallback<String>() {
                            @Override
                            public void onError(Request request, Exception e) {
                                LogUtil.getInstance().e("doRobotAuthTokenApiPostAsync",e);
                            }

                            @Override
                            public void onResponse(String response) {
                                try {
                                    AuthTokenResponse content =
                                            JSON.parseObject(response, AuthTokenResponse.class);
                                    LogUtil.getInstance().d(content.toString());
                                    //写入配置文件中
                                    if (content.getStatus() == 200) {
                                        int rid = content.getResult().getId();
                                        String token = content.getResult().getToken();
                                        String expire_time = content.getResult().getExpire_time();
                                        String refresh_token = content.getResult().getRefresh_token();
                                        String mqtt_username = content.getResult().getMqtt_username();
                                        String mqtt_password = content.getResult().getMqtt_password();

                                        //初始化 系统设置
                                        int night_status = content.getResult().getNight_status();
                                        String start_time = content.getResult().getNight_start_time();
                                        String end_time = content.getResult().getNight_end_time();
                                        DCApplication.mDataManager.setRobotNigthModeSwitch(night_status > 0);
                                        DCApplication.mDataManager.setRobotNightModeStartTime(start_time);
                                        DCApplication.mDataManager.setRobotNightModeEndTime(end_time);
                                        int anti_addiction_status = 0;
                                        if (GlobalParameter.ANTI_ADDICTION_ENABLE)
                                            anti_addiction_status = content.getResult().getAnti_addiction_status();
                                        int duration = content.getResult().getAnti_addiction_duration();
                                        DCApplication.mDataManager.setRobotAntiAddictionSwitch(anti_addiction_status > 0);
                                        DCApplication.mDataManager.setRobotAntiAddictionDuration(duration);

                                        DCApplication.mDataManager.setRobotID(rid);
                                        DCApplication.mDataManager.setRobotAuthToken(token);
                                        DCApplication.mDataManager.setRobotTokenExpireTime(expire_time);
                                        DCApplication.mDataManager.setRobotRefreshToken(refresh_token);
                                        DCApplication.mDataManager.setRobotMqttUsername(mqtt_username);
                                        DCApplication.mDataManager.setRobotMqttPassword(mqtt_password);
                                        //更新SharedPreferences广播
                                        String[] preferences = {AppPreferencesHelper.PREF_ROBOT_ID,
                                                AppPreferencesHelper.PREF_ROBOT_AUTH_TOKEN,
                                                AppPreferencesHelper.PREF_ROBOT_TOKEN_EXPIRE_TIME,
                                                AppPreferencesHelper.PREF_ROBOT_REFRESH_TOKEN,
                                                AppPreferencesHelper.PREF_ROBOT_MQTT_USERNAME,
                                                AppPreferencesHelper.PREF_ROBOT_MQTT_PASSWORD};
                                        DCBroadcastMsgImpl.sendUpdateSPBroadcast(preferences);
                                        executor.getListener().onFinish();
                                    } else {
                                        LogUtil.getInstance().e("doRobotAuthTokenApiPostAsync##status code is invalided:" + content.getMessage());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, null);
            }
        };
        executor.submit(runnable);
        return executor;
    }

//    public void doRobotAuthRefreshTokenApiPost() {
//        String token = mDataManager.getRobotAuthToken();
//        String refresh_token = mDataManager.getRobotRefreshToken();
//        JsonObject json = new JsonObject();
//        json.addProperty("refresh_token", refresh_token);
//
//        OkHttpClientManager.postAsynJson(HttpApiEndPoint.ENDPOINT_AUTH_REFRESH_TOKEN, "token",
//                token, json.toString(), new OkHttpClientManager.ResultCallback<String>() {
//                    @Override
//                    public void onError(Request request, Exception e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            AuthTokenResponse content =
//                                    JSON.parseObject(response, AuthTokenResponse.class);
//                            Log.v(TAG, content.toString());
//                            //写入配置文件中
//                            if (content.getStatus() == 200) {
//                                int rid = content.getResult().getId();
//                                String token = content.getResult().getToken();
//                                String expire_time = content.getResult().getExpire_time();
//                                String refresh_token = content.getResult().getRefresh_token();
//                                String mqtt_username = content.getResult().getMqtt_username();
//                                String mqtt_password = content.getResult().getMqtt_password();
//                                mDataManager.setRobotID(rid);
//                                mDataManager.setRobotAuthToken(token);
//                                mDataManager.setRobotTokenExpireTime(expire_time);
//                                mDataManager.setRobotRefreshToken(refresh_token);
//                                mDataManager.setRobotMqttUsername(mqtt_username);
//                                mDataManager.setRobotMqttPassword(mqtt_password);
//                                //更新SharedPreferences广播
//                                String[] preferences = {AppPreferencesHelper.PREF_ROBOT_ID,
//                                        AppPreferencesHelper.PREF_ROBOT_AUTH_TOKEN,
//                                        AppPreferencesHelper.PREF_ROBOT_TOKEN_EXPIRE_TIME,
//                                        AppPreferencesHelper.PREF_ROBOT_REFRESH_TOKEN,
//                                        AppPreferencesHelper.PREF_ROBOT_MQTT_USERNAME,
//                                        AppPreferencesHelper.PREF_ROBOT_MQTT_PASSWORD};
//                                DCBroadcastMsgImpl.sendUpdateSPBroadcast(preferences);
//
//                            } else if (content.getStatus() == 401) {
//                                LogUtil.getInstance().w("未授权，无法访问！");
//                            } else {
//                                Log.e(TAG, "status code is invalided:" + content.getMessage());
//                            }
//                        } catch (JSONException e) {
//                            Log.e(TAG, "无效JSON串");
//                            e.printStackTrace();
//                        }
//
//                    }
//                }, null);
//    }

    public void doRobotBaseNightApiGet() {
        String token = mDataManager.getRobotAuthToken();
        OkHttpClientManager.getAsyn(HttpApiEndPoint.ENDPOINT_BASE_NIGHT, "token",
                token, new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.v(TAG, response);
                            BaseNightResponse content =
                                    JSON.parseObject(response, BaseNightResponse.class);
                            //写入配置文件
                            if (content.getStatus() == 200) {
                                boolean is_open = content.getResult().isIs_open();
                                String start_time = content.getResult().getStart_time();
                                String end_time = content.getResult().getEnd_time();
                                mDataManager.setRobotNigthModeSwitch(is_open);
                                mDataManager.setRobotNightModeStartTime(start_time);
                                mDataManager.setRobotNightModeEndTime(end_time);
                                //更新SharedPreferences广播
                                String[] preferences = {
                                        AppPreferencesHelper.PREF_ROBOT_NIGHT_MODE_SWITCH,
                                        AppPreferencesHelper.PREF_ROBOT_NIGHT_MODE_START_TIME,
                                        AppPreferencesHelper.PREF_ROBOT_NIGHT_MODE_END_TIME};
                                DCBroadcastMsgImpl.sendSetNightBroadcast(preferences);
                            } else {
                                Log.e(TAG, "status code is invalided:" + content.getMessage());
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "无效JSON串");
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void doRobotBaseNightApiPut() {
        String is_open = DCApplication.mDataManager.getRobotNightModeSwitch() ? "true" : "false";
        String start_time = DCApplication.mDataManager.getRobotNightModeStartTime();
        String end_time = DCApplication.mDataManager.getRobotNightModeEndTime();
        OkGo.<String>put(HttpApiEndPoint.ENDPOINT_BASE_NIGHT)
                .tag(this)
                .headers("token", DCApplication.mDataManager.getRobotAuthToken())
                .upJson("{\"is_open\":" + is_open + ",\"start_time\":\"" + start_time +
                        "\",\"end_time\":\"" + end_time + "\"}")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.v(TAG, "后台夜间模式设置成功!");
                    }
                });
    }

    public static PostOrGetExecutor doRobotBaseNightApiGetAsync() {
        final PostOrGetExecutor executor = new PostOrGetExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String token = DCApplication.mDataManager.getRobotAuthToken();
                OkHttpClientManager.getAsyn(HttpApiEndPoint.ENDPOINT_BASE_NIGHT, "token",
                        token, new OkHttpClientManager.ResultCallback<String>() {
                            @Override
                            public void onError(Request request, Exception e) {

                            }

                            @Override
                            public void onResponse(String response) {
                                try {
                                    LogUtil.getInstance().d(response);
                                    BaseNightResponse content =
                                            JSON.parseObject(response, BaseNightResponse.class);
                                    //写入配置文件
                                    if (content.getStatus() == 200) {
                                        boolean is_open = content.getResult().isIs_open();
                                        String start_time = content.getResult().getStart_time();
                                        String end_time = content.getResult().getEnd_time();
                                        DCApplication.mDataManager.setRobotNigthModeSwitch(is_open);
                                        DCApplication.mDataManager.setRobotNightModeStartTime(start_time);
                                        DCApplication.mDataManager.setRobotNightModeEndTime(end_time);
                                        //更新SharedPreferences广播
                                        String[] preferences = {
                                                AppPreferencesHelper.PREF_ROBOT_NIGHT_MODE_SWITCH,
                                                AppPreferencesHelper.PREF_ROBOT_NIGHT_MODE_START_TIME,
                                                AppPreferencesHelper.PREF_ROBOT_NIGHT_MODE_END_TIME};
                                        DCBroadcastMsgImpl.sendSetNightBroadcast(preferences);
                                        executor.getListener().onFinish();
                                    } else if (content.getStatus() == 401) {
                                        LogUtil.getInstance().w("未授权，无法访问！");
                                    } else {
                                        LogUtil.getInstance().e("status code is invalided:" + content.getMessage());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            }
        };
        executor.submit(runnable);
        return executor;
    }

    public void doRobotBaseAntiAddictionApiGet() {
        String token = mDataManager.getRobotAuthToken();
        OkHttpClientManager.getAsyn(HttpApiEndPoint.ENDPOINT_BASE_ANTI_ADDICTION,
                "token", token, new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.v(TAG, response);
                            BaseAntiAddictionResponse content =
                                    JSON.parseObject(response, BaseAntiAddictionResponse.class);
                            //写入配置文件
                            if (content.getStatus() == 200) {
                                boolean is_open = content.getResult().isIs_open();
                                int duration = content.getResult().getDuration();
                                mDataManager.setRobotAntiAddictionSwitch(is_open);
                                mDataManager.setRobotAntiAddictionDuration(duration);
                                //更新SharedPreferences广播
                                String[] preferences = {
                                        AppPreferencesHelper.PREF_ROBOT_ANTI_ADDICTION_MODE_SWITCH,
                                        AppPreferencesHelper.PREF_ROBOT_ANTI_ADDICTION_MODE_DURATION};
                                DCBroadcastMsgImpl.sendSetAntiAddictionBroadcast(
                                        AppPreferencesHelper.PREF_ROBOT_ANTI_ADDICTION_MODE_SWITCH);
                            } else {
                                Log.e(TAG, "status code is invalided:" + content.getMessage());
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "无效JSON串");
                            e.printStackTrace();
                        }
                    }
                });
    }

    public static PostOrGetExecutor doRobotBaseAntiAddictionApiGetAsync() {
        final PostOrGetExecutor executor = new PostOrGetExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String token = DCApplication.mDataManager.getRobotAuthToken();
                OkHttpClientManager.getAsyn(HttpApiEndPoint.ENDPOINT_BASE_ANTI_ADDICTION,
                        "token", token, new OkHttpClientManager.ResultCallback<String>() {
                            @Override
                            public void onError(Request request, Exception e) {

                            }

                            @Override
                            public void onResponse(String response) {
                                try {
                                    LogUtil.getInstance().d(response);
                                    BaseAntiAddictionResponse content =
                                            JSON.parseObject(response, BaseAntiAddictionResponse.class);
                                    //写入配置文件
                                    if (content.getStatus() == 200) {
                                        boolean is_open = false;
                                        if (GlobalParameter.ANTI_ADDICTION_ENABLE) {
                                            is_open = content.getResult().isIs_open();
                                        }
                                        int duration = content.getResult().getDuration();
                                        int rest_duration = content.getResult().getRest_duration();
                                        DCApplication.mDataManager.setRobotAntiAddictionSwitch(is_open);
                                        DCApplication.mDataManager.setRobotAntiAddictionDuration(duration);
                                        DCApplication.mDataManager.setRobotAntiAddictionRestDuration(rest_duration);
                                        AntiAddictionUtil.getInstance().restart();
                                        //更新SharedPreferences广播
                                        String[] preferences = {
                                                AppPreferencesHelper.PREF_ROBOT_ANTI_ADDICTION_MODE_SWITCH,
                                                AppPreferencesHelper.PREF_ROBOT_ANTI_ADDICTION_MODE_DURATION,
                                                AppPreferencesHelper.PREF_ROBOT_ANTI_ADDICTION_MODE_REST_DURATION};
                                        DCBroadcastMsgImpl.sendSetAntiAddictionBroadcast(AppPreferencesHelper.PREF_ROBOT_ANTI_ADDICTION_MODE_SWITCH);

                                        executor.getListener().onFinish();
                                    } else if (content.getStatus() == 401) {
                                        LogUtil.getInstance().w("未授权，无法访问！");
                                    } else {
                                        LogUtil.getInstance().e("status code is invalided:" + content.getMessage());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            }
        };
        executor.submit(runnable);
        return executor;
    }

    public void doRobotBaseAntiAddictionApiPut() {
        String is_open = DCApplication.mDataManager.getRobotAntiAddictionSwitch() ? "true" : "false";
        int duration = DCApplication.mDataManager.getRobotAntiAddictionDuration();
        int rest_duration = DCApplication.mDataManager.getRobotAntiAddictionRestDuration();
        OkGo.<String>put(HttpApiEndPoint.ENDPOINT_BASE_ANTI_ADDICTION)
                .tag(this)
                .headers("token", DCApplication.mDataManager.getRobotAuthToken())
                .upJson("{\"is_open\":" + is_open + ",\"duration\":" + duration +
                        ",\"rest_duration\":" + rest_duration + "}")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.v(TAG, "后台防沉迷设置成功!");
                    }
                });
    }

    public void doRobotCommonUploadTalkApiPost() {
        String token = mDataManager.getRobotAuthToken();

        OkHttpClientManager.postAsyn(HttpApiEndPoint.STS_SERVER_URL, "token",
                token, "path=null", new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, response);
                        //TODO 从服务器获取OSS TOKEN
                        try {

                        } catch (JSONException e) {
                            Log.e(TAG, "无效JSON串");
                            e.printStackTrace();
                        }
                    }
                }, null);
    }

    public void doRobotLocalSTSTokenApiGet() {
        String token = mDataManager.getRobotAuthToken();

        OkHttpClientManager.getAsyn(HttpApiEndPoint.LOCAL_STS_SERVER_URL,
                new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            LocalSTSTokenResponse content =
                                    JSON.parseObject(response, LocalSTSTokenResponse.class);

                            if (content.getStatus() == 200) {
                                Log.v(TAG, content.toString());
                                //TODO
                                String a = content.getAccessKeyId();
                                String b = content.getAccessKeySecret();
                                String c = content.getSecurityToken();
                                String d = content.getExpiration();
                            } else {
                                Log.e(TAG, "status code is invalided:" + content.getStatus());
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "无效JSON串");
                            e.printStackTrace();
                        }
                    }
                }, null);
    }

    public void doRobotUserApiGet(final String uid) {
        String token = mDataManager.getRobotAuthToken();
        OkHttpClientManager.getAsyn(HttpApiEndPoint.ENDPOINT_USER + uid, "token", token,
                new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, response);
                        try {
                            UserResponse content = JSON.parseObject(response, UserResponse.class);
                            if (content.getStatus() == 200) {
                                //更新数据库
                                String filename = DCApplication.app.getFilesDir().
                                        getAbsolutePath() + "/user/" + uid + "-" +
                                        System.currentTimeMillis() + ".png";
                                PairUser user = new PairUser();
                                user.setAvatar(content.getResult().getAvatar());
                                user.setNickname(content.getResult().getNickname());
                                user.setRelation(content.getResult().getRelation());
                                user.setUid(Integer.valueOf(uid));
                                user.setPhoto(filename);
                                user.saveOrUpdate("uid = ?", uid);
                                //保存用户头像至本地
                                if (!user.getAvatar().isEmpty()) {
                                    try {
                                        final URL url = new URL(user.getAvatar());
                                        final File file = new File(filename);
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    FileUtils.copyURLToFile(url, file);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }.start();
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {

                                //删除UID下无数据的条目
                                LitePal.deleteAll(PairUser.class, "uid=?", uid);

                                Log.e(TAG, "status code is invalided:" + content.getMessage());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void doRobotSoundVoiceApiGet(String vid) {
        String token = mDataManager.getRobotAuthToken();
        OkHttpClientManager.getAsyn(HttpApiEndPoint.ENDPOINT_SOUND_VOICE + vid, "token",
                token, new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, response);
                        try {
                            VoiceResponse content = JSON.parseObject(response, VoiceResponse.class);
                            if (content.getStatus() == 200) {
                                TrackBean trackBean = new TrackBean();
                                trackBean.setAlbum_id(content.getResult().getAlbum_id());
                                trackBean.setAttach(content.getResult().getAttach());
                                trackBean.setCover(content.getResult().getCover());
                                trackBean.setCreate_time(content.getResult().getCreate_time());
                                trackBean.setDuration(content.getResult().getDuration());
                                trackBean.setTitle(content.getResult().getTitle());
                                trackBean.setId(content.getResult().getId());
                                DCBroadcastMsgImpl.sendSoundVoiceBroadcast(trackBean);

                                //判断是否是收藏的歌曲，如果是则保存至收藏列表中，如果不在则从收藏列表中删除
                                //若两个手机用户A和B绑定了同一机器人，A收藏了单曲1,B没有收藏单曲1，
                                //本地收藏与否仅取决于最新推送的单曲的用户的消息
                                boolean collected = content.getResult().isCollected();
                                if (collected) {
                                    Log.v(TAG, "推送的单曲id" + content.getResult().getId() +
                                            "为云收藏单曲,保存至本地收藏列表中,收藏标志至1");
                                    TddeMusicInfo musicInfo = new TddeMusicInfo();
                                    musicInfo.setMid(content.getResult().getId());
                                    musicInfo.setAlbumId(content.getResult().getAlbum_id());
                                    musicInfo.setTitle(content.getResult().getTitle());
                                    musicInfo.setCover(content.getResult().getCover());
                                    musicInfo.setDuration(content.getResult().getDuration());
                                    musicInfo.setAttach(content.getResult().getAttach());
                                    musicInfo.setTags(content.getResult().getTags());
                                    musicInfo.setOrder(0);
                                    musicInfo.setCollected(true);
                                    TddeMusicInfoImpl.modify(musicInfo);
                                } else {
                                    Log.v(TAG, "推送的单曲id" + content.getResult().getId() +
                                            "不是云收藏单曲,保存至本地收藏列表中,收藏标志至0");
                                    TddeMusicInfo musicInfo = new TddeMusicInfo();
                                    musicInfo.setMid(content.getResult().getId());
                                    musicInfo.setAlbumId(content.getResult().getAlbum_id());
                                    musicInfo.setTitle(content.getResult().getTitle());
                                    musicInfo.setCover(content.getResult().getCover());
                                    musicInfo.setDuration(content.getResult().getDuration());
                                    musicInfo.setAttach(content.getResult().getAttach());
                                    musicInfo.setTags(content.getResult().getTags());
                                    musicInfo.setOrder(0);
                                    musicInfo.setCollected(false);
                                    TddeMusicInfoImpl.modify(musicInfo);
                                }
                            } else {
                                Log.e(TAG, "status code is invalided:" + content.getStatus());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Deprecated
    public static PostOrGetExecutor doRobotSoundAlbumIdApiGetAsync(final List<Integer> ids) {
        final PostOrGetExecutor executor = new PostOrGetExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String token = DCApplication.mDataManager.getRobotAuthToken();
                OkHttpClientManager.getAsyn(HttpApiEndPoint.ENDPOINT_SOUND_VOICE, "token",
                        token, new OkHttpClientManager.ResultCallback<String>() {
                            @Override
                            public void onError(Request request, Exception e) {

                            }

                            @Override
                            public void onResponse(String response) {
                                try {
                                    VoiceResponse content = JSON.parseObject(response, VoiceResponse.class);
                                    if (content.getStatus() == 200) {
                                       /* executor.getListener().
                                                onFinishResult(String.valueOf(content.getResult().getAlbum_id()));*/
                                    } else {
                                        Log.e("HttpApiHelper", "status code is invalided:" + content.getStatus());
                                    }
                                } catch (JSONException e) {
                                    Log.e("HttpApiHelper", "无效JSON串");
                                    e.printStackTrace();
                                }
                            }
                        });
            }
        };
        executor.submit(runnable);
        return executor;
    }

    public void doRobotSoundAlbumApiGet(String aid) {
        String token = mDataManager.getRobotAuthToken();
        OkHttpClientManager.getAsyn(HttpApiEndPoint.ENDPOINT_SOUND_ALBUM + aid, "token",
                token, new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, response);
                        SoundAlbumResponse content = JSON.parseObject(response, SoundAlbumResponse.class);
                        switch (content.getStatus()) {
                            case 200:
                                try {
                                    TrackInfo trackInfo = new TrackInfo();
                                    trackInfo.setId(String.valueOf(content.getResult().getId()));
                                    trackInfo.setTitle(content.getResult().getTitle());
                                    trackInfo.setAuthor(content.getResult().getAuthor());
                                    trackInfo.setCover(content.getResult().getCover());
                                    trackInfo.setCount(content.getResult().getCount());
                                    trackInfo.setFilter_age(String.valueOf(content.getResult().getFilter_age()));
                                    trackInfo.setList(content.getResult().getList());
                                    DCBroadcastMsgImpl.sendSoundAlbumBroadcast(trackInfo);

                                    //判断是否是收藏的歌曲，如果是则保存至收藏列表中，如果不在则从收藏列表中删除
                                    //若两个手机用户A和B绑定了同一机器人，A收藏了单曲1,B没有收藏单曲1，
                                    //本地收藏与否仅取决于最新推送的单曲的用户的消息
                                    List<TrackBean> list = content.getResult().getList();
                                    for (TrackBean bean : list) {
                                        if (bean.isCollected()) {
                                            Log.v(TAG, "推送的专辑中单曲id" + bean.getId() +
                                                    "为云收藏单曲,保存至本地收藏列表中,收藏标志至1");
                                            TddeMusicInfo musicInfo = new TddeMusicInfo();
                                            musicInfo.setMid(bean.getId());
                                            musicInfo.setAlbumId(bean.getAlbum_id());
                                            musicInfo.setTitle(bean.getTitle());
                                            musicInfo.setCover(bean.getCover());
                                            musicInfo.setDuration(bean.getDuration());
                                            musicInfo.setAttach(bean.getAttach());
                                            musicInfo.setTags(bean.getTags());
                                            musicInfo.setOrder(0);
                                            musicInfo.setCollected(true);
                                            TddeMusicInfoImpl.modify(musicInfo);
                                        } else {
                                            Log.v(TAG, "推送专辑中单曲id" + bean.getId() +
                                                    "为云收藏单曲,保存至本地收藏列表中,收藏标志至0");
                                            TddeMusicInfo musicInfo = new TddeMusicInfo();
                                            musicInfo.setMid(bean.getId());
                                            musicInfo.setAlbumId(bean.getAlbum_id());
                                            musicInfo.setTitle(bean.getTitle());
                                            musicInfo.setCover(bean.getCover());
                                            musicInfo.setDuration(bean.getDuration());
                                            musicInfo.setAttach(bean.getAttach());
                                            musicInfo.setTags(bean.getTags());
                                            musicInfo.setOrder(0);
                                            musicInfo.setCollected(false);
                                            TddeMusicInfoImpl.modify(musicInfo);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 403:
                            default:
                                Log.e(TAG, "status code is invalided:" + content.getStatus());
                                break;
                        }

                    }
                });
    }

    public void doRobotBookAlbumApiGet(String aid) {
        String token = mDataManager.getRobotAuthToken();
        OkHttpClientManager.getAsyn(HttpApiEndPoint.ENDPOINT_BOOK_ALBUM + aid, "token",
                token, new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, response);
                    }
                });
    }

    public void doRobotBookVolumeApiGet(String vid) {
        String token = mDataManager.getRobotAuthToken();
        OkHttpClientManager.getAsyn(HttpApiEndPoint.ENDPOINT_BOOK_VOLUME + vid, "token",
                token, new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, response);
                    }
                });
    }

    public void doRobotBookScanVolumeApiPut(int[] ids) {
        StringBuilder volume_ids = new StringBuilder();
        for (int id : ids) {
            volume_ids.append(id);
            volume_ids.append(",");
        }
        if (volume_ids.length() > 2) {
            //删除最后一个逗号
            volume_ids.deleteCharAt(volume_ids.length() - 1);

            OkGo.<String>put(HttpApiEndPoint.ENDPOINT_BOOK_SCAN_VOLUME)
                    .tag(this)
                    .headers("token", DCApplication.mDataManager.getRobotAuthToken())
                    .upJson("{" + "\"volume_ids\":[" + volume_ids.toString() + "]}")
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            Log.v(TAG, response.body());
                        }
                    });
        }
    }

    /**
     * 通过 PUT 上传音频 ids 获取音频的详细信息
     *
     * @param ids 音频标识列表
     */
    public void doRobotSoundVoicesApiPut(int[] ids) {
        final StringBuilder voice_ids = new StringBuilder();
        for (int id : ids) {
            voice_ids.append(id);
            voice_ids.append(",");
        }
        if (voice_ids.length() > 2) {
            //删除最后一个逗号
            voice_ids.deleteCharAt(voice_ids.length() - 1);

            OkGo.<String>put(HttpApiEndPoint.ENDPOINT_SOUND_VOICES_PUT)
                    .tag(this)
                    .headers("token", DCApplication.mDataManager.getRobotAuthToken())
                    .upJson("{" + "\"voice_ids\":[" + voice_ids.toString() + "]}")
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            SoundVoicesResponse content = JSON.parseObject(response.body(), SoundVoicesResponse.class);
                            switch (content.getStatus()) {
                                case 200:
                                    try {
                                        TrackInfo trackInfo = new TrackInfo();
                                        trackInfo.setList(content.getResult().getList());
                                        DCBroadcastMsgImpl.sendSoundAlbumBroadcast(trackInfo);

                                        //判断是否是收藏的歌曲，如果是则保存至收藏列表中，如果不在则从收藏列表中删除
                                        //若两个手机用户A和B绑定了同一机器人，A收藏了单曲1,B没有收藏单曲1，
                                        //本地收藏与否仅取决于最新推送的单曲的用户的消息
                                        List<TrackBean> list = content.getResult().getList();
                                        for (TrackBean bean : list) {
                                            if (bean.isCollected()) {
                                                Log.v(TAG, "推送的专辑中单曲id" + bean.getId() +
                                                        "为云收藏单曲,保存至本地收藏列表中,收藏标志至1");
                                                TddeMusicInfo musicInfo = new TddeMusicInfo();
                                                musicInfo.setMid(bean.getId());
                                                musicInfo.setAlbumId(bean.getAlbum_id());
                                                musicInfo.setTitle(bean.getTitle());
                                                musicInfo.setCover(bean.getCover());
                                                musicInfo.setDuration(bean.getDuration());
                                                musicInfo.setAttach(bean.getAttach());
                                                musicInfo.setTags(bean.getTags());
                                                musicInfo.setOrder(0);
                                                musicInfo.setCollected(true);
                                                TddeMusicInfoImpl.modify(musicInfo);
                                            } else {
                                                Log.v(TAG, "推送专辑中单曲id" + bean.getId() +
                                                        "为云收藏单曲,保存至本地收藏列表中,收藏标志至0");
                                                TddeMusicInfo musicInfo = new TddeMusicInfo();
                                                musicInfo.setMid(bean.getId());
                                                musicInfo.setAlbumId(bean.getAlbum_id());
                                                musicInfo.setTitle(bean.getTitle());
                                                musicInfo.setCover(bean.getCover());
                                                musicInfo.setDuration(bean.getDuration());
                                                musicInfo.setAttach(bean.getAttach());
                                                musicInfo.setTags(bean.getTags());
                                                musicInfo.setOrder(0);
                                                musicInfo.setCollected(false);
                                                TddeMusicInfoImpl.modify(musicInfo);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case 403:
                                default:
                                    Log.e(TAG, "status code is invalided:" + content.getStatus());
                                    break;
                            }
                        }
                    });
        }
    }

    public static PostOrGetExecutor doRobotBaseInfoApiPostAsync() {
        final PostOrGetExecutor executor = new PostOrGetExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String code = UUIDUtil.getMacAddress();
                JsonObject json = new JsonObject();
                json.addProperty("ip", RobotInfoUtil.getRobotHostAddress());
                json.addProperty("model", "tddebot");
                json.addProperty("public_id", "0002");
                json.addProperty("sn_no", code);
                json.addProperty("mac", RobotInfoUtil.getRobotMacAddress());
                String token = DCApplication.mDataManager.getRobotAuthToken();
                LogUtil.getInstance().d("doRobotBaseInfoApiPostAsync requestParams code:"+code+" mac:"+RobotInfoUtil.getRobotMacAddress()+" token:"+token);
                OkHttpClientManager.postAsynJson(HttpApiEndPoint.ENDPOINT_BASE_INFO, "token",
                        token, json.toString(), new OkHttpClientManager.ResultCallback<String>() {
                            @Override
                            public void onError(Request request, Exception e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(String response) {
                                LogUtil.getInstance().d("收到响应：" + response);
                                JSONObject response_json = null;
                                try {
                                    response_json = new JSONObject(response);
                                    int status = response_json.getInt("status");
                                    if (status == 401) {
                                        LogUtil.getInstance().w("未授权,准备重新获取授权...");
                                        doRobotAuthTokenApiPostAsync().listen(new PostOrGetCallback() {
                                            @Override
                                            public void onFinish() {
                                                doRobotBaseInfoApiPostAsync().listen(new PostOrGetCallback() {
                                                    @Override
                                                    public void onFinish() {
                                                        HttpApiHelper.doRobotBaseUsersApiGetAsync().listen(new PostOrGetCallback() {
                                                            @Override
                                                            public void onFinish() {
//                                                                DCApplication.mMQTTClient.setPairUsersIDList();
//                                                                DCApplication.mMQTTClient.connect(1);
                                                                MessageUtils.MessageFeed(GlobalParameter.StateEngineHandler,
                                                                        GlobalParameter.StateEngine_Init_Finish_Or_Timeout);
                                                            }
                                                        });
                                                    }
                                                });
                                            }

                                           /* @Override
                                            public void onFinishResult(String result) {

                                            }*/
                                        });
                                    } else if ((status == 200)) {
                                        LogUtil.getInstance().d("机器人信息更新成功...");
                                        executor.getListener().onFinish();
                                    }
                                } catch (org.json.JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, null);
            }
        };
        executor.submit(runnable);
        return executor;
    }

    public static PostOrGetExecutor doRobotBaseUsersApiGetAsync() {
        final PostOrGetExecutor executor = new PostOrGetExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String token = DCApplication.mDataManager.getRobotAuthToken();
                OkHttpClientManager.getAsyn(HttpApiEndPoint.ENDPOINT_BASE_USERS, "token", token,
                        new OkHttpClientManager.ResultCallback<String>() {
                            @Override
                            public void onError(Request request, Exception e) {

                            }

                            @Override
                            public void onResponse(String response) {
                                LogUtil.getInstance().d("收到响应：" + response);

                                try {
                                    UsersResponse content = JSON.parseObject(response, UsersResponse.class);
                                    if (content.getStatus() == 200) {
                                        try {
                                            LitePal.deleteAll(PairUser.class);
                                            int[] ids = content.getResult().getIds();
                                            for (int uid : ids) {
                                                LogUtil.getInstance().d("从服务器获取到绑定的用户id:" + uid);
                                                //更新数据库
                                                PairUser user = new PairUser();
                                                user.setUid(uid);
                                                user.saveOrUpdate("uid = ?", String.valueOf(uid));
                                            }
                                            //获取User详细信息
                                            FileUtils.deleteQuietly(FileUtils.getFile(DCApplication.app.getFilesDir().
                                                    getAbsolutePath() + "/user"));
                                            for (int uid : ids) {
                                                HttpApiHelper.getInstance().doRobotUserApiGet(String.valueOf(uid));
                                            }
                                            //SP更新广播
                                            if (ids.length > 0) {
                                                DCApplication.mDataManager.setUserPaired(true);
                                            } else {
                                                DCApplication.mDataManager.setUserPaired(false);
                                            }
                                            String[] preferences = {AppPreferencesHelper.PREF_DEVICE_PAIRED};
                                            DCBroadcastMsgImpl.sendUpdateSPBroadcast(preferences);
                                            executor.getListener().onFinish();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        LogUtil.getInstance().d("status code is invalided:" + content.getMessage());
                                    }
                                } catch (JSONException e) {
                                    LogUtil.getInstance().d("无效JSON串");
                                    e.printStackTrace();
                                }
                            }
                        });
            }
        };
        executor.submit(runnable);
        return executor;
    }

    public void doRobotBaseUsersApiGet() {
        OkGo.<String>get(HttpApiEndPoint.ENDPOINT_BASE_USERS)
                .tag(this)
                .headers("token", DCApplication.mDataManager.getRobotAuthToken())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            UsersResponse content = JSON.parseObject(response.body(), UsersResponse.class);
                            if (content.getStatus() == 200) {
                                int[] ids = content.getResult().getIds();
                                for (int uid : ids) {
                                    //更新数据库
                                    PairUser user = new PairUser();
                                    user.setUid(uid);
                                    user.saveOrUpdate("uid=?", String.valueOf(uid));
                                }
//                                DCApplication.mMQTTClient.setPairUsersIDList();
                            } else {
                                LogUtil.getInstance().d("status code is invalided:" + content.getMessage());
                            }
                        } catch (JSONException e) {
                            LogUtil.getInstance().d("无效JSON串");
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void doRobotTagsApiGet() {
        String token = mDataManager.getRobotAuthToken();
        OkHttpClientManager.getAsyn(HttpApiEndPoint.ENDPOINT_SOUND_TAG, "token", token,
                new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, response);
                        try {
                            MusicTagResponse content = JSON.parseObject(response, MusicTagResponse.class);
                            if (content.getStatus() == 200) {
                                //更新数据库
                                int count = content.getResult().getCount();
                                for (MusicTagResponse.Tag musicTag : content.getResult().getList()) {
                                    TddeMusicTag tag = new TddeMusicTag();
                                    tag.setTid(musicTag.getId());
                                    tag.setTitle(musicTag.getTitle());
                                    TddeMusicTagImpl.modify(tag);
                                }
                            } else {
                                Log.e(TAG, "status code is invalided:" + content.getMessage());
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "无效JSON串");
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void doRobotSoundVoicesApiGet(String tagId) {
        String token = mDataManager.getRobotAuthToken();
        OkHttpClientManager.getAsyn(HttpApiEndPoint.ENDPOINT_SOUND_VOICES + tagId, "token",
                token, new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, response);
                        try {
                            SoundVoicesResponse content = JSON.parseObject(response, SoundVoicesResponse.class);
                            if (content.getStatus() == 200) {
                                int count = content.getResult().getCount();
                                /*for (Voice voice : content.getResult().getList()) {
                                    //int vid = voice.getId();
                                    //doRobotSoundVoiceApiGet(String.valueOf(vid));
                                }*/
                            } else {
                                Log.e(TAG, "status code is invalided:" + content.getStatus());
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "无效JSON串");
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 将本地的统计数据上传到服务器上
     */
    public void doTddeStatisticalPOST() {
        String token = DCApplication.mDataManager.getRobotAuthToken();
        JSONObject json = new JSONObject();
        JSONObject listen = new JSONObject();
        JSONObject read = new JSONObject();
        JSONObject spell = new JSONObject();

        try {
            listen.put("time_weekly", TddeUsingTimeStatisticalImpl.getLast7DayMusicInfo());//过去一周每天听音频的时间
            listen.put("favourite_weekly", TddeMusicStatisticalImpl.selectMaxCountMusic());//每周播放次数前三的音频id

            read.put("time_weekly", TddeUsingTimeStatisticalImpl.getLast7DayBookInfo());//过去一周每天读绘本的时间
            read.put("favourite_weekly", TddeBookStatisticalImpl.selectMaxCountBook());//每周阅读次数前三的绘本id

            spell.put("time_weekly", TddeUsingTimeStatisticalImpl.getLast7DayLetterBoxInfo()); //过去一周每天自然拼读使用的时间
            spell.put("words_weekly", TddeUsingTimeStatisticalImpl.getLast7DayLetterBoxCountInfo());//过去一周每天通过自然拼读学习单词的个数
            spell.put("mistakes_weekly", LetterBoxWordStatisticalImpl.selectMaxCountWord());//过去一周每天通过自然拼读学习出错次数最多的5个单词

            json.put("listen", listen);
            json.put("read", read);
            json.put("spell", spell);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }


        LogUtil.getInstance().d("LetterBoxWord", "token : " + token + "  ,json : " + json.toString());

        OkHttpClientManager.postAsynJson(HttpApiEndPoint.ENDPOINT_ACHIEVEMENT_STATISTICS, "token",
                token, json.toString(), new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {
                        LogUtil.getInstance().d("LetterBoxWord", "exception : " + e.toString());

                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response) {
                        LogUtil.getInstance().d("LetterBoxWord", "response : " + response);
                    }
                }, null);
    }

    public static PostOrGetExecutor doRobotAchievementTemplateApiGet(final String templateName) {
        final PostOrGetExecutor executor = new PostOrGetExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String token = DCApplication.mDataManager.getRobotAuthToken();
                OkHttpClientManager.getAsyn(HttpApiEndPoint.ENDPOINT_ACHIEVEMENT_TEMPLATES +
                                "?keyword=" + templateName, "token", token,
                        new OkHttpClientManager.ResultCallback<String>() {
                            @Override
                            public void onError(Request request, Exception e) {

                            }

                            @Override
                            public void onResponse(String response) {
                                Log.v("收到获取成就勋章模板的响应：", response);
                                AchievementTemplateResponse content =
                                        JSON.parseObject(response, AchievementTemplateResponse.class);
                                if (content.getStatus() == 200) {
                                    try {
                                        TddeAchievement achievement = new TddeAchievement();
                                        achievement.setConditions(content.getResult().get(0).getConditions());
                                        achievement.setName(content.getResult().get(0).getName());
                                        achievement.setAchievement_template_id(
                                                content.getResult().get(0).getAchievement_template_id()
                                        );
                                        achievement.setType_id(content.getResult().get(0).getType_id());
                                        achievement.setUpload(false);
                                        TddeAchievementImpl.modify(achievement);
                                        executor.getListener().onFinish();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    //Log.e("AchievementTemplateApi", "invalid code:" + content.getStatus());
                                }
                            }
                        });
            }
        };
        executor.submit(runnable);
        return executor;
    }

    public static PostOrGetExecutor doRobotAchievementRecordsApiPost(final Records records) {
        /*{
            "records": [
            {
                "robot_id": 0,
                    "achievement_template_id": 0,
                    "status": 0,
                    "schedule": "string"
            }
            ]
        }*/
        final PostOrGetExecutor executor = new PostOrGetExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.v("Upload Achv Records", JSON.toJSONString(records));
                String token = DCApplication.mDataManager.getRobotAuthToken();
                OkHttpClientManager.postAsynJson(HttpApiEndPoint.ENDPOINT_ACHIEVEMENT_RECORDS, "token",
                        token, JSON.toJSONString(records), new OkHttpClientManager.ResultCallback<String>() {
                            @Override
                            public void onError(Request request, Exception e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(String response) {
                                LogUtil.getInstance().d("收到上传成就勋章记录响应：" + response);
                                /*{"status":200,"result":{"robot_id":2}}*/
                                JSONObject json = null;
                                try {
                                    json = new JSONObject(response);
                                    if (!json.isNull("status")) {
                                        int status = json.getInt("status");
                                        switch (status) {
                                            case 200:
                                                executor.getListener().onFinish();
                                                break;
                                            default:
                                                LogUtil.getInstance().d("Invalid status:" + status);
                                                break;
                                        }
                                    }
                                } catch (org.json.JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, null);
            }
        };
        executor.submit(runnable);
        return executor;
    }

    /**
     * @param type   1:第三方应用  0:自研应用
     * @param page   第几页
     * @param number 每页有几个应用   number决定Page  比如type1共有20个应用 number设置为了10   那么Page就只能为(1,2)
     * @return
     */
    public static PostOrGetExecutor doRobotAppStoreApiGet(final String type, final String page, final String number) {
        final PostOrGetExecutor executor = new PostOrGetExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String token = DCApplication.mDataManager.getRobotAuthToken();
                String url = HttpApiEndPoint.ENDPOINT_APP_STORES +
                        "?type=" + type + "&page=" + page + "&number=" + number + "&order=id&direction=asc";
                //Log.v("获取AppStore的请求地址：", url);
                OkHttpClientManager.getAsyn(url, "token", token,
                        new OkHttpClientManager.ResultCallback<String>() {
                            @Override
                            public void onError(Request request, Exception e) {
                                LogUtil.getInstance().e("doRobotAppStoreApiGet",e);
                            }

                            @Override
                            public void onResponse(String response) {
                                LogUtil.getInstance().d("doRobotAppStoreApiGet##reponse:", response);
                                AppStoreResponse content =
                                        JSON.parseObject(response, AppStoreResponse.class);
                                if (content.getStatus() == 200) {
                                    try {
                                        List<AppStoreResponse.AppsInfo.AppInfo> appInfoList = content.getResult().getList();
                                        for (AppStoreResponse.AppsInfo.AppInfo appInfo : appInfoList) {
                                            TddeAppInfo appInfo1 = new TddeAppInfo();
                                            appInfo1.setAid(appInfo.getId());
                                            appInfo1.setApp_name(appInfo.getApp_name());
                                            appInfo1.setPackage_name(appInfo.getPackage_name());
                                            appInfo1.setVersion(appInfo.getVersion());
                                            appInfo1.setIcon_url(appInfo.getIcon_url());
                                            appInfo1.setPackage_url(appInfo.getPackage_url());
                                            appInfo1.setPackage_size(appInfo.getPackage_size());
                                            appInfo1.setRelease_note(appInfo.getRelease_note());
                                            appInfo1.setDelete_time(appInfo.getDelete_time());
                                            TddeAppInfoImpl.modify(appInfo1);
                                        }
                                        executor.getListener().onFinish();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Log.e("AppStoreApiGet", "invalid code:" + content.getStatus());
                                }
                            }
                        });
            }
        };
        executor.submit(runnable);
        return executor;
    }

    public String doBookCoverSearchPOST(byte[] bodyStr, Object tag) {
        String resBody = null;
        com.squareup.okhttp.Response response = OkHttpClientManager.postSyncOctetStream(HttpApiEndPoint.BOOK_SEARCH_URL, bodyStr, tag);
        if (response != null) {
            try {
                resBody = response.body().string();
            } catch (IOException e) {
                resBody = "0";
            }
        } else {
            resBody = "0";
        }
        return resBody;
    }



}

