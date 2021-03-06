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

        //εε§ηΆζ
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
        if (cmd.contains("ε­¦θ±θ―­") || cmd.contains("η©ζΈΈζ") || cmd.contains("θͺηΆζΌθ―»"))
            c = GlobalParameter.BC_Speech_LearnEng_Cmd;
        if (cmd.contains("θ―»η»ζ¬"))
            c = GlobalParameter.BC_Speech_ReadBook_Cmd;
        if (cmd.contains("θ―΄θ±θ―­"))
            c = GlobalParameter.BC_Speech_Oral_Cmd;
        if (cmd.contains("η£¨θ³ζ΅"))
            c = GlobalParameter.BC_Speech_Music_Cmd;

        if (cmd.contains("θ°ι«ι³ι")) c = GlobalParameter.BC_Speech_VoiceRaise_Cmd;
        if (cmd.contains("θ°δ½ι³ι")) c = GlobalParameter.BC_Speech_VoiceLower_Cmd;
        if (cmd.contains("δΈδΈι¦")) c = GlobalParameter.BC_Speech_MediaPrev_Cmd;
        if (cmd.contains("δΈδΈι¦")) c = GlobalParameter.BC_Speech_MediaNext_Cmd;
        if (cmd.contains("εζ­’ζ­ζΎ")) c = GlobalParameter.BC_Speech_MediaStop_Cmd;
        if (cmd.contains("εΌε§ζ­ζΎ")) c = GlobalParameter.BC_Speech_MediaPlay_Cmd;
        if (cmd.contains("ιεΊ")) c = GlobalParameter.BC_Speech_Face_Finish_Cmd;
        return c;
    }
}