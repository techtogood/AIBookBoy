package ai.aistem.xbot.framework.application;

import android.os.Handler;

import org.opencv.core.Mat;

import ai.aistem.xbot.framework.internal.interaction.RobotStateMachine;


/**
 * Created by aistem on 2018/3/13.
 */

public final class GlobalParameter {
    public static Handler StateEngineHandler = null;
    public static RobotStateMachine StateMachineHandler = null;
    public static Handler QandAHandler = null;
    public static Handler CameraServiceHandler = null;
    public static Handler MicHandler = null;
    public static Handler TimerHandler = null;
    public static Handler PictureBookAPPHandler = null;
    public static Handler CharRecognitionAPPHandler = null;
    //绘本阅读UI更新
    public static Handler ReadUIHandler = null;

    //成就数据更新
    public static Handler AchvManagerHandler = null;
    public static Handler FaceManagerHandler = null;
    public static Handler ListenerManagerHandler = null;

    public static Handler DebugUIHandler = null;
    public static Mat debugCurImage = null;
    public static Mat debugCurImage2 = null;

    //message 由16bit组成，高8位是模块ID，低8位是消息ID
    //StateEngine
    public final static int StateEngine_Init_Finish_Or_Timeout = 0x0001;
    public final static int StateEngine_AAS_Set = 0x0002;
    public final static int StateEngine_AAS_Close = 0x0003;
    public final static int StateEngine_T_SS_Timeout = 0x0004;
    //进入防沉迷定时器超时,开启防沉迷功能
    public final static int StateEngine_T_AAEnable_Timeout = 0x0005;
    //进入防沉迷前1分钟定时器超时,准备开启防沉迷功能
    public final static int StateEngine_T_AAEnter_Timeout = 0x0006;
    //防沉迷失活定时器超时
    public final static int StateEngine_T_AAInactive_Timeout = 0x0007;
    //防沉迷失活前30秒定时器超时
    public final static int StateEngine_T_AAExit_Timeout = 0x0008;
    public final static int StateEngine_QandA_Result = 0x0009;
    public final static int StateEngine_T_CS_Timeout = 0x0010;
    //工作状态失活定时器超时
    public final static int StateEngine_T_WS_Timeout = 0x0011;

    public final static int StateEngine_CMD_BASE = 0x0020;
    public final static int StateEngine_CMD_SELF = 0x0021;
    public final static int StateEngine_CMD_CHAT = 0x0022;
    public final static int StateEngine_CMD_SLEEP = 0x0023;
    public final static int StateEngine_CMD_ANTI = 0x0024;
    public final static int StateEngine_CMD_NIGHT = 0x0025;
    public final static int StateEngine_CMD_HOME = 0x0026;

    //QnadA
    public final static int QandA_Awake = 0x0101;

    //RobotCameraService
    public final static int Camera_Cmd_Open = 0x0201;
    public final static int Camera_Cmd_Release = 0x0202;
    public static int CameradDeviceStatus = 0; //0 is idle, 1 is opening, 2 is open, 3 is closeing;


    public final static int Camera_Reading = 0x0280; //摄像头绘本阅读
    public final static int Camera_Lattering = 0x0281; //摄像头字母识别
    public static int CameraStatus = 0; //摄像头状态：绘本阅读 ,字母识别 或其他
    public final static Object imagePreShare = new Object();
    public final static Object imagePreShare_2 = new Object();
    public static Mat curImage = null;
    public static Mat curImage_2 = null;
    public static String CharRecognitionResult = "---";//字母识别的结果

    //Mic
    public final static int MIC_Open = 0x0301;
    public final static int MIC_Start_Record = 0x0302;
    public final static int MIC_Release = 0x0303;
    public final static int MIC_Stop = 0x0304;

    //Timer
    //基础状态失活定时器
    public final static int T_BSInactiv_Start = 0x0401;
    public final static int T_BSInactiv_Cancel = 0x0402;
    //闲聊状态失活定时器
    public final static int T_SSInactiv_Start = 0x0411;
    public final static int T_SSInactiv_Cancel = 0x0412;
    //工作状态失活定时器
    public final static int T_WSInactiv_Start = 0x0413;
    public final static int T_WSInactiv_Cancel = 0x0414;
    //防沉迷失活定时器(机器人休息时长)
    public final static int T_AAInactiv_Start = 0x0421;
    public final static int T_AAInactiv_Cancel = 0x0422;
    //防沉迷定时器(允许机器人单次使用时长)
    public final static int T_AAEnter_Start = 0x0431;
    public final static int T_AAEnter_Cancel = 0x0432;
    //防沉迷结束30秒前语音提示的定时器
    public final static int T_AAExit_Start = 0x0441;
    public final static int T_AAExit_Cancel = 0x0442;
    //进入防沉迷前1分钟有语音提示的定时器
    public final static int T_AAEnable_Start = 0x0451;
    public final static int T_AAEnable_Cancel = 0x0452;
    //语音交互状态失活定时器5分钟
    public final static int T_CSInactiv_Start = 0x0461;
    public final static int T_CSInactiv_Cancel = 0x0462;

    //Alarm
    //夜间模式设置闹钟
    public final static int A_NightMode_Set = 0x0601;
    public final static int A_NightMode_Close = 0x0602;


    //广播
    public final static int BC_Speech_Wake_Cmd = 0x0501;
    public final static int BC_Speech_ReadBook_Cmd = 0x0502;
    public final static int BC_Speech_LearnEng_Cmd = 0x0503;
    public final static int BC_Speech_VoiceRaise_Cmd = 0x0504;
    public final static int BC_Speech_VoiceLower_Cmd = 0x0505;
    public final static int BC_Speech_MediaPlay_Cmd = 0x0506;
    public final static int BC_Speech_MediaStop_Cmd = 0x0507;
    public final static int BC_Speech_MediaNext_Cmd = 0x0508;
    public final static int BC_Speech_MediaPrev_Cmd = 0x0509;
    public final static int BC_Speech_Music_Cmd = 0x0510;
    public final static int BC_Speech_Oral_Cmd = 0x0511;
    public final static int BC_Speech_Music_Kid_Cmd = 0x0512;
    public final static int BC_Speech_Music_Book_Cmd = 0x0513;
    public final static int BC_Speech_Music_Tdde_Cmd = 0x0515;
    public final static int BC_Speech_Music_Movie_Cmd = 0x0516;
    public final static int BC_Speech_Face_Finish_Cmd = 0x0517;

    //PictureBookAPP
    public static int Book_Status = 0; //绘本阅读状态
    public final static int Book_Idle = 0; //完成开启或完成结束状态；
    public final static int Book_To_Strat = 1; //正在开启状态
    public final static int Book_To_Finish = 2; //正在结束状态
    public final static int Book_StarReading = 0x0701; //开启绘本阅读SURF
    public final static int Book_StopReading = 0x0702; //结束绘本阅读
    public final static int Book_StarReading2 = 0x0703; //开启绘本阅读ORB
    public final static int Book_StarReading3 = 0x0704; //开启绘本阅读SIFT
    public static boolean CoverFlg = true;//标志位:封面识别进程是否完成
    public static boolean ContentFlg = true;//标志位:内容查找进程是否完成

    public static boolean ImagePreThreadFlag = true;//标志位:图像前处理线程1是否完成
    public static boolean ImagePreThreadFlag_2 = true;//标志位:图像前处理线程2是否完成

    //PictureBookAPP
    public final static int Lettter_StartRecognition = 0x0801; //开启字母识别
    public final static int Lettter_StopRecognition = 0x0802; //结束字母识别
    public static boolean CharRecogntionFlg = true;
    public final static Object CharShare = new Object();//互斥锁:字母识别进程同步

    //AchvUpdateThread
    public final static int StudyRecords_SaveOrUpdate = 0x0901; //更新开启学习之路勋章
    public final static int LetterRecords_SaveOrUpdate = 0x0902; //更新上传字母(A-Z)子勋章 及 知识勋章
    public final static int CVCRecords_SaveOrUpdate = 0x0903; //更新CVC 单词子勋章
    public final static int Statistics_Upload = 0x0904;
    public final static int AchvRecords_Upload = 0x0905;

    //UI更新
    public static Handler UIHandler = null;
    public final static int State_Update = 0x9901;
    public final static int Debug_Update = 0x9902;
    public final static int Toast_Update = 0x9903;

    //ReadActivity绘本阅读更新
    public static final int READ_STATUS_START_DISPLAY_NAME = 5;//找到绘本，展示名字
    public static final int READ_STATUS_START = 1;//开始阅读
    public static final int READ_STATUS_DOWNLOAD = 2;//绘本下载进度，下载完成
    public static final int READ_STATUS_PAGE_TURNING = 3;//检测到翻页了
    public static final int READ_STATUS_FINISH = 4;//读完一页（请翻页，有惊喜额）
    public static final int READ_STATUS_ANIM_START = 6;//播放动画
    public static final int READ_STATUS_ANIM_STOP = 7;//关闭动画

    public static String MEDIA_RESOURCE_DIR = "/sdcard/res";
    public static String SOUND_RESOURCE_DIR = MEDIA_RESOURCE_DIR + "/sound/";
   // public static String SOUND_RESOURCE_DIR =  "/asserts/raw/book_";
    public static String VIDEO_RESOURCE_DIR = MEDIA_RESOURCE_DIR + "/video/";
    public final static String API_PROTOCOL = "https://";
    //public final static String API_URL = "api.xbot.com";
    //public final static String BOOK_URL = "book.xbot.com";


    //防沉迷功能总开关，如果设置为false，则关闭防沉迷功能；如果设置为true，则开启防沉迷功能（具体功能仍依赖于Launcher设置）
    public final static boolean ANTI_ADDICTION_ENABLE = false;
    //护眼模式功能总开关，如果设置为false，则关闭防沉迷功能；如果设置为true，则开启防沉迷功能（具体功能仍依赖于Launcher设置）
    public final static boolean PROTECT_EYE_ENABLE = false;

    //休眠时间设置
    public final static int SLEEP_SECONDS = 300;
    //自言自语时间设置
    public final static int SELF_SECONDS = 128;
}