package ai.aistem.xbot.framework.internal.mqtt;

import ai.aistem.xbot.framework.internal.mqtt.model.ReceivedMessage;

public interface IReceivedMessageListener {

    void onMessageReceived(ReceivedMessage message);
}