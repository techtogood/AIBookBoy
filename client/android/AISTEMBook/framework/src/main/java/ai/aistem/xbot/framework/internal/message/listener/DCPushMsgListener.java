package ai.aistem.xbot.framework.internal.message.listener;

/**
 * @author: aistem
 * @created: 2018/6/11/11:16
 * @desc: DCPushMsgListener
 */
public interface DCPushMsgListener  {

    /**
     * @param soundMsg
     */
    void playSoundCallBack(String soundMsg);

    /**
     * @param albumMsg
     */
    void playAlbumCallBack(String albumMsg);

    /**
     * @param bookMsg
     */
    void addBookCallBack(String bookMsg);

    /**
     * @param textMsg
     */
    void chatTextCallBack(String textMsg);

    /**
     * @param faceMsg
     */
    void chatFaceCallBack(String faceMsg);

    /**
     * @param chatSoundMsg
     */
    void chatSoundCallBack(String chatSoundMsg);

}
