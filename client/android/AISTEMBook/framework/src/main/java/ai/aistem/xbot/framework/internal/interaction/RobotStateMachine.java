package ai.aistem.xbot.framework.internal.interaction;

import android.os.Message;
import android.util.Log;

import ai.aistem.xbot.framework.application.GlobalParameter;
import ai.aistem.xbot.framework.message.DCBroadcastMsgImpl;


public class RobotStateMachine extends StateMachine {

    private static final String TAG = RobotStateMachine.class.getSimpleName();

    public final int CMD_BASE = 0x01;
    public final int CMD_SELF = 0x02;
    public final int CMD_CHAT = 0x03;
    public final int CMD_SLEEP = 0x04;
    public final int CMD_ANTI = 0x05;
    public final int CMD_NIGHT = 0x06;
    public final int CMD_HOME = 0x07;


    public State mBaseState = new BaseState();
    public State mSelfTalkState = new SelfTalkState();
    public State mChattingState = new ChattingState();
    public State mSleepState = new SleepState();
    public State mHomeState = new HomeStateState();
    public State mAntiAddictionState = new AntiAddictionState();
    public State mNightState = new NightState();


    public RobotStateMachine(String name) {
        super(name);
        addState(mBaseState);
        addState(mHomeState);
        addState(mSelfTalkState);
        addState(mChattingState);
        addState(mAntiAddictionState);
        addState(mNightState);
        addState(mSleepState);

        //初始状态
        setInitialState(mBaseState);

    }

    public void MessageFeed(int msg) {
        sendMessage(obtainMessage(msg));
    }

    public void MessageFeed(int msg, Object obj) {
        sendMessage(obtainMessage(msg, obj));
    }


    class BaseState extends State {
        @Override
        public void enter() {
            Log.i(TAG, "Enter BaseState.");
            DCBroadcastMsgImpl.sendBaseStateBroadcast("ENTER");
        }

        @Override
        public void exit() {
            Log.i(TAG, "Exit BaseState.");
            DCBroadcastMsgImpl.sendBaseStateBroadcast("EXIT");
        }

        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case CMD_HOME:
                    transitionTo(mHomeState);
                    return HANDLED;
                default:
                    return NOT_HANDLED;
            }
        }
    }

    class HomeStateState extends State {

        @Override
        public void enter() {
            Log.i(TAG, "Enter HomeStateState");
            DCBroadcastMsgImpl.sendWorkStateBroadcast("ENTER");

        }

        @Override
        public void exit() {
            Log.i(TAG, "Exit HomeStateState");
            DCBroadcastMsgImpl.sendWorkStateBroadcast("EXIT");
        }

        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case CMD_HOME:
                    transitionTo(mHomeState);
                    return HANDLED;
                default:
                    return NOT_HANDLED;
            }
        }
    }

    class SelfTalkState extends State {
        @Override
        public void enter() {
            Log.i(TAG, "Enter SelfTalkState.");
            DCBroadcastMsgImpl.sendSelfTalkStateBroadcast("ENTER");
        }

        @Override
        public void exit() {
            Log.i(TAG, "Exit SelfTalkState.");
            DCBroadcastMsgImpl.sendSelfTalkStateBroadcast("EXIT");
        }

        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what) {
                default:
                    return NOT_HANDLED;
            }
        }
    }


    class ChattingState extends State {
        @Override
        public void enter() {
            Log.i(TAG, "Enter ChattingState");
            DCBroadcastMsgImpl.sendChatStateBroadcast("ENTER");
        }

        @Override
        public void exit() {
            Log.i(TAG, "Exit ChattingState");
            DCBroadcastMsgImpl.sendChatStateBroadcast("EXIT");
        }

        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what) {

                default:
                    return NOT_HANDLED;
            }
        }
    }

    class SleepState extends State {
        @Override
        public void enter() {
            Log.i(TAG, "Enter SleepState.");
            DCBroadcastMsgImpl.sendSleepStateBroadcast("ENTER");
        }

        @Override
        public void exit() {
            Log.i(TAG, "Exit SleepState.");
            DCBroadcastMsgImpl.sendSleepStateBroadcast("EXIT");
        }

        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what) {
                default:
                    return NOT_HANDLED;
            }
        }
    }

    class NightState extends State {
        @Override
        public void enter() {
            Log.i(TAG, "Enter NightState.");
            DCBroadcastMsgImpl.sendNightStateBroadcast("ENTER");
        }

        @Override
        public void exit() {
            Log.i(TAG, "Exit NightState.");
            DCBroadcastMsgImpl.sendNightStateBroadcast("EXIT");
        }

        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what) {
                default:
                    return NOT_HANDLED;
            }
        }

        @Override
        public String getName() {
            return "NightState";
        }
    }

    class AntiAddictionState extends State {

        @Override
        public void enter() {
            Log.i(TAG, "Enter AntiAddictionState");
            DCBroadcastMsgImpl.sendAntiAddiStateBroadcast("ENTER");
        }

        @Override
        public void exit() {
            Log.i(TAG, "Exit AntiAddictionState");
            DCBroadcastMsgImpl.sendAntiAddiStateBroadcast("EXIT");
        }

        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what) {
                default:
                    return NOT_HANDLED;
            }
        }
    }


    private int CommandString2Int(String cmd) {
        int c = 0x0000;
        if (cmd.contains("学英语") || cmd.contains("玩游戏") || cmd.contains("自然拼读"))
            c = GlobalParameter.BC_Speech_LearnEng_Cmd;
        if (cmd.contains("读绘本"))
            c = GlobalParameter.BC_Speech_ReadBook_Cmd;
        if (cmd.contains("说英语"))
            c = GlobalParameter.BC_Speech_Oral_Cmd;
        if (cmd.contains("磨耳朵"))
            c = GlobalParameter.BC_Speech_Music_Cmd;

        if (cmd.contains("调高音量")) c = GlobalParameter.BC_Speech_VoiceRaise_Cmd;
        if (cmd.contains("调低音量")) c = GlobalParameter.BC_Speech_VoiceLower_Cmd;
        if (cmd.contains("上一首")) c = GlobalParameter.BC_Speech_MediaPrev_Cmd;
        if (cmd.contains("下一首")) c = GlobalParameter.BC_Speech_MediaNext_Cmd;
        if (cmd.contains("停止播放")) c = GlobalParameter.BC_Speech_MediaStop_Cmd;
        if (cmd.contains("开始播放")) c = GlobalParameter.BC_Speech_MediaPlay_Cmd;
        if (cmd.contains("退出")) c = GlobalParameter.BC_Speech_Face_Finish_Cmd;
        return c;
    }
}