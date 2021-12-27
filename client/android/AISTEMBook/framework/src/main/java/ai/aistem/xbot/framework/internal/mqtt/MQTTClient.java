package ai.aistem.xbot.framework.internal.mqtt;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.litepal.LitePal;
import org.litepal.crud.callback.SaveCallback;
import org.litepal.crud.callback.UpdateOrDeleteCallback;

import java.util.ArrayList;
import java.util.HashSet;

import ai.aistem.xbot.framework.R;
import ai.aistem.xbot.framework.application.DCApplication;
import ai.aistem.xbot.framework.application.GlobalParameter;
import ai.aistem.xbot.framework.data.DataManager;
import ai.aistem.xbot.framework.data.db.impl.TddeVoiceInfoImpl2;
import ai.aistem.xbot.framework.data.db.model.PairUser;
import ai.aistem.xbot.framework.internal.http.HttpApiHelper;
import ai.aistem.xbot.framework.internal.http.async.PostOrGetCallback;
import ai.aistem.xbot.framework.internal.message.listener.DCChatMsgListener;
import ai.aistem.xbot.framework.internal.message.listener.DCPushMsgListener;
import ai.aistem.xbot.framework.internal.message.listener.DCSystemMsgListener;
import ai.aistem.xbot.framework.internal.mqtt.model.ChatMQTTMsg;
import ai.aistem.xbot.framework.internal.mqtt.model.ReceivedMessage;
import ai.aistem.xbot.framework.internal.mqtt.model.ReceivedMessageContent;
import ai.aistem.xbot.framework.internal.mqtt.model.ToSendMessageContent;
import ai.aistem.xbot.framework.internal.utils.MessageUtils;
import ai.aistem.xbot.framework.internal.utils.RobotInfoUtil;
import ai.aistem.xbot.framework.message.DCBroadcastMsgImpl;
import ai.aistem.xbot.framework.utils.LogUtil;
import ai.aistem.xbot.framework.utils.UUIDUtil;

public class MQTTClient {

    private final String TAG = MQTTClient.class.getSimpleName();

    /**
     * MQTT连接参数
     */
    private String clientHandle = "";
    private String clientId = "";
    private String serverHostName = "";
    private int serverPort = 1883;
    private boolean cleanSession = true;
    private String username = "";
    private String password = "";


    private boolean tlsConnection = false;
    private String tlsServerKey = "";
    private String tlsClientKey = "";
    private int timeout = 80;
    private int keepAlive = 200;
    private String lwtTopic = "";
    private String lwtMessage = "";
    private int lwtQos = 0;
    private boolean lwtRetain = false;


    private Context mContext;

    /**
     * 系统数据统一管理对象
     */
    private DataManager mDataManager;

    /**
     * 单例
     */
    private static MQTTClient instance = null;

    private MQTTClient(Context context) {
        mContext = context;
        mDataManager = DCApplication.app.getDataManager();

        setPairUsersIDList();
        InitParams();
    }

    public synchronized static MQTTClient getInstance(Context context) {
        if (instance == null) {
            instance = new MQTTClient(context);
        }
        return instance;
    }


    //private MQTTConnection connectionModel = null;
    private ArrayList<Subscription> subscriptions = null;
    private HashSet<String> PairUsersIDList = new HashSet<>();
    private Connection connection = null;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }


    private boolean setNewConnectionModleFromConfig() {
        username = mDataManager.getRobotMqttUsername();
        password = mDataManager.getRobotMqttPassword();
//        username = "test";
//        password = "test";
        if (username.isEmpty() || password.isEmpty()) {
            Log.e(TAG, "MQTT用户名或密码不能为空！");
            return false;
        }

        //RobotID为0时表示没有从服务器上获取到机器人的ID
        if (mDataManager.getRobotID() == 0) {
            Log.e(TAG, "MQTT参数错误，机器人ID为0！");
            return false;
        }
        String topic = "robot/" + mDataManager.getRobotID();

        Subscription subscription = new Subscription(topic, 0, clientHandle, false);
        subscriptions = new ArrayList<>();
        subscriptions.add(subscription);
        connection = Connection.createConnection(clientHandle, clientId, serverHostName, serverPort,
                mContext, tlsConnection);

        connection.getClient().setCallback(new MqttCallbackHandler(mContext, clientHandle,
                MQTTClient.this));
        Connections.getInstance(mContext).addConnection(connection);
        connection.addReceivedMessageListner(listener);
        connection.setSubscriptions(subscriptions);
        return true;
    }


    public void connect(int number) {
        for (int i = 0; i < number || number > 999; i++) {
            try {
                connect();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                System.err.println("连接失败,正在第" + i + "次尝试");
                continue;
            }
            return;
        }
        throw new RuntimeException("无法连接服务器");
    }


    private IMqttToken mIMqttToken = null;

    /**
     * 连接
     *
     * @throws MqttException
     */
    public void connect() throws MqttException {
        if (connection != null && connection.getClient().isConnected()) {
            System.out.println("MQTT Client is connected");
            return;
        } else {
            System.err.println("MQTT Client is disconnected");
        }
        if (!setNewConnectionModleFromConfig()) return;
        connection.changeConnectionStatus(Connection.ConnectionStatus.CONNECTING);
        MqttConnectOptions connOpts = getMqttConnectOptions();
        connection.addConnectionOptions(connOpts);
        String[] actionArgs = new String[1];
        actionArgs[0] = clientId;
        final ActionListener callback = new ActionListener(mContext,
                ActionListener.Action.CONNECT, connection, actionArgs);
        try {
            if (mIMqttToken != null) {
                mIMqttToken.getClient().disconnect();
                mIMqttToken = connection.getClient().connect(connOpts, null, callback);
            } else {
                mIMqttToken = connection.getClient().connect(connOpts, null, callback);
            }
        } catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(),
                    "MqttException occurred", e);
        } catch (Exception e) {
            Log.e(this.getClass().getCanonicalName(), "Exception : ", e);
        }

    }


    public void disconnect() {
        try {
            if (connection != null && connection.getClient().isConnected()) {
                connection.getClient().disconnect();
                mIMqttToken = null;
            }
        } catch (MqttException ex) {
            Log.e(TAG, "Exception occurred during disconnect: " + ex.getMessage());
        }
    }


    /**
     * 解析接收到的MQTT Message 的内容，并根据解析的事件结果触发相应的动作
     *
     * @param message
     */
    private void ReceivedMessageContentParser(final String message) {
        //Log.v(TAG, message);
        ReceivedMessageContent content = JSON.parseObject(message, ReceivedMessageContent.class);
        String event = content.getEvent();
        final String uid = content.getUid();
        final String eid = content.getEid();
        //判断uid不在绑定用户列表中
        if (PairUsersIDList.contains(uid)) {
            if (eid != null) {
                LogUtil.getInstance().d("收到一条需要确认的消息...");
                PublishEventAckMessage(uid, eid);
            }
            switch (event) {
                case "alive":
                    //立即返回心跳检测响应消息
                    PublishAliveMessage(uid);
                    break;
                case "unbind":
                    TddeVoiceInfoImpl2.deletedUnBindUser(uid);
                    LitePal.deleteAllAsync(PairUser.class, "uid = ?", uid)
                            .listen(new UpdateOrDeleteCallback() {
                                @Override
                                public void onFinish(int rowsAffected) {
                                    PairUsersIDList.remove(uid);
                                    if (PairUsersIDList.isEmpty()) {
                                        mDataManager.setUserPaired(true);
                                    }
                                    if (systemMsgListener != null) {
                                        systemMsgListener.unBindPhoneUserCallBack(message);
                                    }
                                }
                            });
                    break;
                case "set_night":
                    //请求获取夜间模式的设置,并保存值SP中.
                    HttpApiHelper.doRobotBaseNightApiGetAsync().listen(new PostOrGetCallback() {
                        @Override
                        public void onFinish() {
                            //从SP中读取夜间模式的值,设置开启/关闭,开始闹铃,结束闹铃
                            boolean is_open = mDataManager.getRobotNightModeSwitch();
                            String start = mDataManager.getRobotNightModeStartTime();
                            String end = mDataManager.getRobotNightModeEndTime();
                            String[] startAndend = {start, end};
                            MessageUtils.MessageFeed(GlobalParameter.TimerHandler, GlobalParameter.A_NightMode_Set, startAndend);

                            //设置夜间模式
                            if (systemMsgListener != null) {
                                systemMsgListener.setNightModeCallBack(message);
                            }
                        }

                    });

                    break;
                case "close_night":
                    HttpApiHelper.doRobotBaseNightApiGetAsync().listen(new PostOrGetCallback() {
                        @Override
                        public void onFinish() {
                            MessageUtils.MessageFeed(GlobalParameter.TimerHandler, GlobalParameter.A_NightMode_Close);
                            //关闭夜间模式
                            if (systemMsgListener != null) {
                                systemMsgListener.closeNightModeCallBack(message);
                            }
                        }
                    });
                    break;
                case "set_anti_addiction":
                    //HttpApiHelper.getInstance().doRobotBaseAntiAddictionApiGet();
                    HttpApiHelper.doRobotBaseAntiAddictionApiGetAsync().listen(new PostOrGetCallback() {
                        @Override
                        public void onFinish() {


                            if (systemMsgListener != null) {
                                systemMsgListener.setAntiAddictionCallBack(message);
                            }
                            //发送防沉迷设置消息
                            /*MessageUtils.MessageFeed(GlobalParameter.StateEngineHandler,
                                    GlobalParameter.StateEngine_AAS_Set);*/
                        }
                    });
                    break;
                case "close_anti_addiction":
                    HttpApiHelper.doRobotBaseAntiAddictionApiGetAsync().listen(new PostOrGetCallback() {
                        @Override
                        public void onFinish() {
                            //关闭防沉迷
                            if (systemMsgListener != null) {
                                systemMsgListener.closeAntiAddictionCallBack(message);
                            }
                            //发送关闭防沉迷消息
                            /*MessageUtils.MessageFeed(GlobalParameter.StateEngineHandler,
                                    GlobalParameter.StateEngine_AAS_Close);*/
                        }
                    });
                    break;
                case "chat_text":
                    //密语文字：1.TSS语音读出来 2.UI跳转
                    String msg = content.getMessage();
                    DCBroadcastMsgImpl.sendChatTextBroadcast(msg);
                    break;
                case "chat_face":
                    //密语表情 1.TSS语音读出来 2.UI表情
                    int id_face = content.getFace();

                    if (pushMessageListener != null) {
                        pushMessageListener.chatFaceCallBack(String.valueOf(id_face));
                    }
                    //这里有个BUG,接收到的mqtt消息中不包含duration的值
                    //int duration = content.getDuration();
                    //DCBroadcastMsgImpl.sendChatFaceBroadcast(duration);
                    //DCBroadcastMsgImpl.sendChatFaceBroadcast(String.valueOf(id_face));
                    break;
                case "chat_sound":
                    if (chatMsgListener != null) {
                        chatMsgListener.receiverUserVoice(message);
                    }
                    break;
                case "user":
                    String filename = DCApplication.app.getFilesDir().
                            getAbsolutePath() + "/user/" + uid + "-" + System.currentTimeMillis() + ".png";
                    final String avatar = content.getAvatar();
                    String nickname = content.getNickname();
                    String relation = content.getRelation();
                    PairUser user = new PairUser();
                    user.setUid(Integer.parseInt(uid));
                    user.setAvatar(avatar);
                    user.setNickname(nickname);
                    user.setRelation(relation);
                    user.setPhoto(filename);
                    user.saveOrUpdateAsync("uid = ?", uid).listen(new SaveCallback() {
                        @Override
                        public void onFinish(boolean success) {
                            if (pushMessageListener != null) {
                                systemMsgListener.updateUserCallBack(message);
                            }
                        }
                    });

                    break;
                case "play_sound":
                    ArrayList<Integer> ids = new ArrayList<>();
                    ids = content.getIds();
                    if (pushMessageListener != null) {
                        pushMessageListener.playSoundCallBack(message);
                    }
                    break;
                case "play_album":
                    int id = content.getId();
                    if (pushMessageListener != null) {
                        pushMessageListener.playAlbumCallBack(message);
                    }
                    break;
                case "add_book":
                    int id_book = content.getId();
                    if (pushMessageListener != null) {
                        pushMessageListener.addBookCallBack(message);
                    }
                    break;
                default:
                    break;
            }
        } else {
            LogUtil.getInstance().d("uid不在绑定用户列表中");
            switch (event) {
                case "bind":
                    //3.记录绑定用户信息到数据库中
                    PairUser user = new PairUser();
                    user.setUid(Integer.parseInt(uid));
                    user.saveOrUpdate("uid = ?", uid);
                    mDataManager.setUserPaired(true);
                    PairUsersIDList.add(uid);
                    //1.语音提示 2.跳转到动画界面
                    if (systemMsgListener != null) {
                        systemMsgListener.bindPhoneUserCallBack(message);
                    }
                    //确保数据库插入后进行的操作
                   /* user.saveOrUpdateAsync("uid = ?", uid).listen(new SaveCallback() {
                        @Override
                        public void onFinish(boolean success) {
                            mDataManager.setUserPaired(true);
                            PairUsersIDList.add(uid);
                            //1.语音提示 2.跳转到动画界面
                            if (systemMsgListener != null) {
                                systemMsgListener.bindPhoneUserCallBack(message);
                            }
                        }
                    });*/
                    break;
                default:
                    break;
            }
        }
    }

    private void publish(Connection connection, String topic, String message, int qos, boolean retain) {
        try {
            String[] actionArgs = new String[2];
            actionArgs[0] = message;
            actionArgs[1] = topic;
            final ActionListener callback = new ActionListener(mContext,
                    ActionListener.Action.PUBLISH, connection, actionArgs);
            if (connection.getClient().isConnected())
                connection.getClient().publish(topic, message.getBytes(), qos, retain, null, callback);
        } catch (MqttException ex) {
            Log.e(TAG, "Exception occurred during publish: " + ex.getMessage());
        }
    }


    /**
     * app/{uid}	alive rid
     */
    public void PublishAliveMessage(String uid) {
        if (uid.equals("0")) {
            return;
        }
        ToSendMessageContent content = new ToSendMessageContent();

        content.setEvent("alive");

        content.setRid(DCApplication.mDataManager.getRobotID());
        String message = JSON.toJSONString(content);
        //Log.d(TAG,message);

        //Connection connection = Connections.getInstance(mContext).
        //        getConnection(clientHandle);
        String topic = "app/" + uid;
        if (connection != null) {
            publish(connection, topic, message, 0, false);
        } else {
            Log.e(TAG, "connection is null!");
        }
       /* for (String uid:PairUsersIDList) {

        }*/
    }

    /**
     * TODO 系统升级后调用
     * /server 	upgrade	rid,version
     */
    public void PublishUpgradeMessage() {
        ToSendMessageContent content = new ToSendMessageContent();

        content.setEvent("upgrade");
        content.setRid(DCApplication.mDataManager.getRobotID());
        content.setVersion(String.valueOf(RobotInfoUtil.packageName(DCApplication.app)));
        //指定需要序列化的属性
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter(ToSendMessageContent.class,
                "event", "rid", "version");
        String message = JSON.toJSONString(content, filter);
        Log.d(TAG, message);
        String topic = "server";
        //Connection connection = Connections.getInstance(mContext).
        //        getConnection(clientHandle);
        if (connection != null) {
            publish(connection, topic, message, 0, false);
        } else {
            Log.e(TAG, "connection is null!");
        }
    }

    /**
     * /server close_anti_addiction rid
     */
    public void PublishCloseAntiAddictionMessage() {
        ToSendMessageContent content = new ToSendMessageContent();

        content.setEvent("close_anti_addiction");
        //TODO set rid
        content.setRid(2);
        String message = JSON.toJSONString(content);

        String topic = "server";

        //Log.d(TAG,message);
        //Connection connection = Connections.getInstance(mContext).
        //       getConnection(clientHandle);
        if (connection != null) {
            publish(connection, topic, message, 0, false);
        } else {
            Log.e(TAG, "connection is null!");
        }
    }

    /**
     * server chat_sound rid url duration
     */
    public void PublishChatSoundMessage(ChatMQTTMsg mqttMsg) {
        if (mqttMsg == null || mqttMsg.getUid().equals("0")) {
            return;
        }
        ToSendMessageContent content = new ToSendMessageContent();
        content.setEvent(mqttMsg.getEvent());
        content.setRid(Integer.valueOf(mqttMsg.getRid()));
        content.setUrl(mqttMsg.getUrl());
        content.setUid(Integer.valueOf(mqttMsg.getUid()));
        content.setDuration(mqttMsg.getDuration());
        String message = JSON.toJSONString(content);

        String topic = "server";
        Log.d(TAG, "message:" + message + " topic:" + topic);
        //Connection connection = Connections.getInstance(mContext).
        //        getConnection(clientHandle);
        if (connection != null) {
            publish(connection, topic, message, 0, false);
        } else {
            Log.e(TAG, "connection is null!");
        }
    }


    /**
     * 2018-08-08 高杰:app上需要确认推送和发送消息是否成功
     * 因为mqtt是没有确认的，所以需要我们加一下
     * 在需要确认的event中，我会增加一个eid字段
     * 能发送给对应的uid一条ack消息，消息格式如下：
     * {"event":"ack", "rid":1, "uid":2, "eid": "xxxxxx"}
     * 把eid回发给我就可以了
     *
     * @param uid uid
     * @param eid eid
     */
    public void PublishEventAckMessage(String uid, String eid) {
        if (uid.equals("0")) {
            return;
        }
        ToSendMessageContent content = new ToSendMessageContent();
        content.setEvent("ack");
        content.setRid(DCApplication.mDataManager.getRobotID());
        content.setUid(Integer.valueOf(uid));
        content.setEid(eid);

        //指定需要序列化的属性
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter(ToSendMessageContent.class,
                "event", "rid", "uid", "eid");
        String message = JSON.toJSONString(content, filter);
        String topic = "app/" + uid;
        Log.d(TAG, "message:" + message + " topic:" + topic);
        //Connection connection = Connections.getInstance(mContext).
        //        getConnection(clientHandle);
        if (connection != null) {
            publish(connection, topic, message, 0, false);
        } else {
            Log.e(TAG, "connection is null!");
        }
    }


    /**
     * server sync_sound rid id
     */
    public void PublishSyncSoundMessage(String vid) {
        ToSendMessageContent content = new ToSendMessageContent();

        content.setEvent("sync_sound");
        content.setRid(DCApplication.mDataManager.getRobotID());
        content.setId(vid);
        //指定需要序列化的属性
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter(ToSendMessageContent.class,
                "event", "rid", "id");
        String message = JSON.toJSONString(content, filter);
        String topic = "server";
        Log.d(TAG, message);
        //Connection connection = Connections.getInstance(mContext).
        //        getConnection(clientHandle);
        if (connection != null) {
            publish(connection, topic, message, 0, false);
        } else {
            Log.e(TAG, "connection is null!");
        }
    }

    public void PublisLowPowerMessage(int powerValue) {
        ToSendMessageContent content = new ToSendMessageContent();

        content.setEvent("power_status");
        content.setRid(DCApplication.mDataManager.getRobotID());
        content.setValue(powerValue);
        //指定需要序列化的属性
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter(ToSendMessageContent.class,
                "event", "rid", "value");
        String message = JSON.toJSONString(content, filter);
        Log.d(TAG, message);
        String topic = "server";

        //Connection connection = Connections.getInstance(mContext).
        //       getConnection(clientHandle);
        if (connection != null) {
            publish(connection, topic, message, 0, false);
        } else {
            Log.e(TAG, "connection is null!");
        }
    }

    /*********************************************************/

    private DCPushMsgListener pushMessageListener;

    public void setDCPushMessageListener(DCPushMsgListener pushMessageListener) {
        this.pushMessageListener = pushMessageListener;
    }

    private DCSystemMsgListener systemMsgListener;

    public void setDCSystemMessageListener(DCSystemMsgListener systemMessageListener) {
        this.systemMsgListener = systemMessageListener;
    }

    private DCChatMsgListener chatMsgListener;

    public void setDCChatMsgListener(DCChatMsgListener chatMsgListener) {
        this.chatMsgListener = chatMsgListener;
    }

    /***********************************************************************/

    /**
     * 初始化
     * 从本地数据库中，读取当前已绑定用户的列表
     */
    public void setPairUsersIDList() {
        for (PairUser pairUser : LitePal.findAll(PairUser.class)) {
            PairUsersIDList.add(String.valueOf(pairUser.getUid()));
        }
    }

    private void InitParams() {
        serverHostName = mContext.getResources().getString(R.string.mqtt_server_hostname);
        serverPort = mContext.getResources().getInteger(R.integer.mqtt_server_port);
        cleanSession = mContext.getResources().getBoolean(R.bool.clean_session);
        tlsConnection = mContext.getResources().getBoolean(R.bool.tls_connect);
        tlsServerKey = mContext.getResources().getString(R.string.mqtt_server_tls_key);
        tlsClientKey = mContext.getResources().getString(R.string.mqtt_client_tls_key);
        timeout = mContext.getResources().getInteger(R.integer.timeout);
        keepAlive = mContext.getResources().getInteger(R.integer.keepalive);
        lwtTopic = mContext.getResources().getString(R.string.lwt_topic);
        lwtMessage = mContext.getResources().getString(R.string.lwt_message);
        lwtQos = mContext.getResources().getInteger(R.integer.lwt_qos);
        lwtRetain = mContext.getResources().getBoolean(R.bool.lwt_retain);

        clientId = "robot-" + UUIDUtil.getMacAddress();
        clientHandle = serverHostName + "-" + clientId;
    }

    IReceivedMessageListener listener = new IReceivedMessageListener() {
        @Override
        public void onMessageReceived(ReceivedMessage message) {
            Log.d(TAG, message.getMessage().toString());
            ReceivedMessageContentParser(message.getMessage().toString());
        }
    };

    private MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(cleanSession);
        connOpts.setConnectionTimeout(timeout);
        connOpts.setKeepAliveInterval(keepAlive);
        if (!username.equals("")) {
            connOpts.setUserName(username);
        }

        if (!password.equals("")) {
            connOpts.setPassword(password.toCharArray());
        }
        if (!lwtTopic.equals("") && !lwtMessage.equals("")) {
            connOpts.setWill(lwtTopic, lwtMessage.getBytes(), lwtQos, lwtRetain);
        }
        //   if(tlsConnection){
        //       // TODO Add Keys to conOpts here
        //       //connOpts.setSocketFactory();
        //   }
        return connOpts;
    }
}
