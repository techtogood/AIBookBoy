package ai.aistem.xbot.framework.internal.message.impl;

import ai.aistem.xbot.framework.internal.message.listener.DCChatMsgListener;
import ai.aistem.xbot.framework.internal.message.listener.DCPushMsgListener;
import ai.aistem.xbot.framework.internal.message.listener.DCSystemMsgListener;
import ai.aistem.xbot.framework.internal.mqtt.MQTTClient;

/**
 * @author: aistem
 * @created: 2018/6/11/11:45
 * @desc: DCMessageOperation
 */
public class DCMessageOperation {

    //DCPushMsgListener pushMessageListener
    public static void setDCMessageListener(MQTTClient client){
        if (client!=null){
            client.setDCPushMessageListener(initDCPushMessageListener());
            client.setDCSystemMessageListener(initDCSystemMessageListener());
            client.setDCChatMsgListener(initDCChatMsgListener());
        }
    }



    private static DCPushMsgListener initDCPushMessageListener(){
        return new DCPushMsgListenerImpl();
    }

    private static DCSystemMsgListener initDCSystemMessageListener(){
        return new DCSystemMsgListenerImpl();
    }

    private static DCChatMsgListener initDCChatMsgListener(){
        return new DCChatMsgListenerImpl();
    }

}
