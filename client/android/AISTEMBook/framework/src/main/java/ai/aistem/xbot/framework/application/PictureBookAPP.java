package ai.aistem.xbot.framework.application;

/**
 * Created by aistem on 2018/7/9.
 */

import android.content.Context;
import android.graphics.ImageFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.squareup.okhttp.Call;

import org.json.JSONArray;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ai.aistem.xbot.framework.data.db.model.PictureBookData;
import ai.aistem.xbot.framework.internal.http.HttpApiHelper;
import ai.aistem.xbot.framework.internal.utils.AudioPlayer;
import ai.aistem.xbot.framework.utils.StatisticalUtil;

import static java.lang.Thread.sleep;

public class PictureBookAPP implements Runnable {

    private static final String TAG = PictureBookAPP.class.getSimpleName();

    private Context mContext;
    private boolean Installing = false;//是否处于下载安装绘本数据状态

    //绘本阅读部分
    public int OldCoverID = -1; //记录当前系统最后一次成功识别到的绘本封面
    public int CoverID = -1;    //记录当前识别到的封面
    public int OldContentID = -1;//记录当前系统最后一次成功识别到的绘本内容页
    public int ContentID = -1;   //记录当前系统识别到的绘本内容页
    public int LocalCoverFailCount = 0;   //本地查找绘本封面识别次数
    public long CoverDetectTime = 0;   //绘本封面检索的时间
    public int CoverDetectPeriod = 0;   //当已识别到绘本后，封面检测的周期
    private final Object CoverShare = new Object();//互斥锁:封面查找进程同步
    private final Object ContentShare = new Object();//互斥锁:内容查找进程同步
    private boolean IsValidImageFlg = true;//如果为真  表示当前帧图像中没有绘本数据
    private Mat mRgba; //用于显示摄像头采集到的图像
    private Mat mRgba_2; //用于显示摄像头采集到的图像2
    private int mRgba_id = 0;
    private boolean IsCoverUpdate = false;//是否需要更新本地封面算法文件
    private boolean IsLoadCover = true;//是否需要更新加载封面文件
    private int initCover = 0;//是否存在封面数据文件
    //end 绘本阅读部分

    //绘本阅读  上传服务器部分
    private final Object SendToServiceShare = new Object();
    private byte[] rgbData;
    private boolean SendToServiceFlg = true;
    //end绘本阅读  上传服务器部分

    //声音播放部分
    //private MediaManager audioPlayer = new MediaManager();;//声音播放对象
    private AudioPlayer audioPlayer = new AudioPlayer();
    private final Object PlayerShare = new Object();
    public String audioUrl = "";//声音URL
    public int firstNoCvoerCount = 0;   //开始阅读后，有图像但识别不到封面的次数；
    public int firstWithoutImageCount = 0;   //开始阅读后，无图像的次数；
    public long startTurningTime = 0;   //第一次识别到封面或识别到新一页的时间
    public int noContentCount = 0;   //识别到封面开读后，识别不到内容的次数
    public int networkFailCount = 0;   //后台识别到封面，但网络交互识别的次数
    public int cur_duration = 0; //当前播放音频的时长，单位：毫秒
    public int play_type = 0; //0直接播放，1播放同时返回音频时长
    private final String voiceDir = GlobalParameter.SOUND_RESOURCE_DIR;//本地声音路径
    private ArrayList<Integer> readList = new ArrayList();


    //ene of 声音播放部分
    private int feature_type = 1;

    private final Object AnsyHandleShare = new Object(); //异步处理线程共享对象
    private int i_event = -1; //内部事件

    private final Object HeartBeatShare = new Object(); //心跳线程共享对象

    public Mat transfromMat;

    public PictureBookAPP(Context context) {
        mContext = context;
    }

    //绘本阅读内部工作状态
    private final int read_inited = 0;
    private final int read_starting = 1;
    private final int read_started = 2;
    private final int read_stopping = 3;
    private int read_state = -1;
    //end绘本阅读内部工作状态

    //文件路径
    private final String bookRootDir = "/sdcard/data/book";
    private final String coverDir = "/sdcard/data/book/cover";
    private final String coverDataDir = "/sdcard/data/book/cover/data";
    private final String coverImageDir = "/sdcard/data/book/cover/image";
    private final String coverRecordDir = "/sdcard/data/book/cover/record";
    private final String coverFlagDir = "/sdcard/data/book/cover/flag";
    private final String contentDir = "/sdcard/data/book/content";
    private final String contentRecordDir = "/sdcard/data/book/content/record";
    private final String voiceRootDir = "/sdcard/data/voice";

    //绘本数据下载
    //private HashMap<Integer, OSSAsyncTask> downTaskMap = new HashMap<>(); //保存下载数据 （inxdex 下载任务对象）
    private HashMap<Integer, Call> downTaskMap = new HashMap<>(); //保存下载数据 （inxdex 下载任务对象）
    private boolean downError = false; //下载过程中是否发现下载错误


    private Handler coverHandler = null;
    private Handler contentHandler = null;
    private Handler contentHandler2 = null;

    @Override
    public void run() {

        InitData();

        Log.d(TAG, "picture read init.. initCover=" + initCover);
        FindObjectInit();//绘本阅读初始化
        //LoadCoverAndSave();

        //new CoverDetectionThread().start();
        ContentDetectionThread contentTask = new ContentDetectionThread();
        contentTask.setPriority(Thread.MAX_PRIORITY);
        contentTask.start();

        //ContentDetectionThread2 contentTask2 = new ContentDetectionThread2();
        //contentTask2.setPriority(Thread.MAX_PRIORITY);
        //contentTask2.start();
        new SendToServiceThread().start();
        new ImagePreThread().start();
        //new ImagePreThread_2().start();
        new PlayAudioThread().start();
        new AnsyHandleThread().start();
        new HeartBeatThread().start();

        read_state = read_inited;

        //LitePal.deleteAll(PictureBookData.class);

        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);//第一帧四点位置坐标储存对象（4行1列32位2通道）
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);//第二帧四点位置坐标存储对象（4行1列32位2通道）

        src_mat.put(0, 0, 720 * 0.304, 480 * 0.280, 720 * 0.675, 480 * 0.280, 720 * 0.831, 480 * 0.800, 720 * 0.162, 480 * 0.800);
        dst_mat.put(0, 0, 720 * 0.162, 480 * 0.180, 720 * 0.831, 480 * 0.180, 720 * 0.831, 480 * 0.800, 720 * 0.162, 480 * 0.800);
        transfromMat = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        GlobalParameter.PictureBookAPPHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case GlobalParameter.Book_StarReading:
                        //绘本阅读模式
                        Log.d(TAG, "Book_StarReading===" );
                        if (read_state != read_inited) {
                            break;
                        }
                        GlobalParameter.CameraStatus = GlobalParameter.Camera_Reading;
                        read_state = read_starting;
                        synchronized (AnsyHandleShare) {
                            i_event = 0;
                            AnsyHandleShare.notify();
                        }
                        Log.d(TAG, "Book_StarReading");
                        synchronized (HeartBeatShare) {
                            HeartBeatShare.notify();
                        }
                        break;
                    case GlobalParameter.Book_StarReading2:
                        //绘本阅读模式
                        //ServiceStatus = GlobalParameter.Camera_StarReading;
                        if (GlobalParameter.CameraStatus == GlobalParameter.Camera_Reading) {
                            break;
                        }
                        feature_type = 2; //ORB
                        GlobalParameter.CameraStatus = GlobalParameter.Camera_Reading;
                        Message tempMsg2 = GlobalParameter.CameraServiceHandler.obtainMessage();
                        tempMsg2.what = GlobalParameter.Camera_Cmd_Open;
                        GlobalParameter.CameraServiceHandler.sendMessage(tempMsg2);
                        break;
                    case GlobalParameter.Book_StarReading3:
                        //绘本阅读模式
                        //ServiceStatus = GlobalParameter.Camera_StarReading;
                        if (GlobalParameter.CameraStatus == GlobalParameter.Camera_Reading) {
                            break;
                        }
                        feature_type = 3; //SIFT
                        GlobalParameter.CameraStatus = GlobalParameter.Camera_Reading;
                        Message tempMsg3 = GlobalParameter.CameraServiceHandler.obtainMessage();
                        tempMsg3.what = GlobalParameter.Camera_Cmd_Open;
                        GlobalParameter.CameraServiceHandler.sendMessage(tempMsg3);
                        break;
                    case GlobalParameter.Book_StopReading:
                        if (read_state == read_started || read_state == read_starting) {
                            GlobalParameter.CameraStatus = 0;
                            read_state = read_stopping;
                            //唤醒退出后台处理现场
                            synchronized (AnsyHandleShare) {
                                i_event = 1;
                                AnsyHandleShare.notify();
                            }
                        }
                        break;
                    default:
                        break;

                }
                super.handleMessage(msg);
            }

        };
        Log.d(TAG, "picture read init exit..");

    }

    private void playSound(String file) {
        if (read_state == read_started || file == voiceDir + "20602004.mp3") {
            audioUrl = file;
            synchronized (PlayerShare) {
                PlayerShare.notify();
            }
        }
    }

    public void doRobotdown(final String url, final String dir, final int index) {
//        OSSAsyncTask task = OssDownloadManager.getInstance().asyncGetObject(url, dir, new OssDownloadManager.ResultCallback() {
//            @Override
//            public void onProgress(long currentSize, long totalSize) {
//
//            }
//
//            @Override
//            public void onError() {
//                //Log.d(TAG, "onError: " + url+" index=="+index);
//                downError = true;
//                downTaskMap.remove(index);
//            }
//
//            @Override
//            public void onSuccess() {
//                //Log.d(TAG, "onResponse: " + url+" index=="+index);
//                downTaskMap.remove(index);
//            }
//        });
//        downTaskMap.put(index,task);



        //Log.d(TAG,"cnd url= "+cdnUrl);

        Call task = ai.aistem.xbot.framework.network.OkHttpClientManagerNoPrivateSSL.getDownloadDelegate().downloadAsyn(
                url,
                dir,
                new ai.aistem.xbot.framework.network.OkHttpClientManagerNoPrivateSSL.ResultCallback<String>() {
                    @Override
                    public void onError(com.squareup.okhttp.Request request, Exception e) {
                        Log.d(TAG, "onError: " + url);
                        downError = true;
                        downTaskMap.remove(index);
                    }

                    @Override
                    public void onResponse(String response) {
                        //Log.d(TAG, "onResponse: "+response);
                        downTaskMap.remove(index);
                    }
                });
        downTaskMap.put(index, task);
    }

    public void createDir(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    /** 删除单个文件
     * @param fileName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false     */
    public static boolean deleteSingleFile(String fileName) {
        File file = new File(fileName);        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }





    public void SendMessageToUI(int what, int arg1, String obj) {
        Message tempMsg = GlobalParameter.ReadUIHandler.obtainMessage();
        tempMsg.what = what;
        tempMsg.arg1 = arg1;
        tempMsg.obj = obj;
        GlobalParameter.ReadUIHandler.sendMessage(tempMsg);
    }


    public void downAndInstallData(final String data) {

        try {
            JSONObject responseData = new JSONObject(data);
            JSONArray content = responseData.getJSONArray("content");
            int id = responseData.getInt("id");
            int type = responseData.getInt("type");
            String name = responseData.getString("name");
            String[] imageList = {};
            Installing = true;
            int downTaskIndex = 0;

            /*GlobalParameter.UIHandler.sendEmptyMessage(5);//唤醒mainActivity线程 更新界面 查找结果
            Message tempMsg = GlobalParameter.UIHandler.obtainMessage();
            tempMsg.what = 5;
            tempMsg.obj = name;
            GlobalParameter.UIHandler.sendMessage(tempMsg);*/


            downError = false;
            downTaskMap.clear();

            //List<PictureBookData> bookData = LitePal.where("book_id = ?", String.valueOf(id)).find(PictureBookData.class);

            //int book_num = bookData.size();
            //Log.d(TAG, "count=" + book_num);

            int book_num = 0;

            if ((new File(coverFlagDir+"/"+Integer.toString(id))).exists()) {
                book_num = 1;
            }


            if (book_num == 0) {

                playSound(voiceDir + "20602006.mp3");

                createDir(contentRecordDir + "/" + id);

                Log.d(TAG, "id =" + id + "type=" + type);


                if (responseData.getString("data") != "") {
                    String[] dataList = (responseData.getString("data")).split("\\|");
                    if (type >= 1 && type <= 3) {
                        ;
                        doRobotdown(dataList[type - 1], coverDataDir + "/", downTaskIndex++);
                    } else {

                        for (int i = 0; i < dataList.length; i++) {
                            doRobotdown(dataList[i], coverDataDir + "/", downTaskIndex++);
                        }
                    }
                }

                if (responseData.getString("image") != "") {
                    imageList = (responseData.getString("image")).split("\\|");
                    for (int i = 0; i < imageList.length; i++) {
                        Log.d(TAG, "imageList" + i + "==" + imageList[i]);
                        doRobotdown(imageList[i], coverImageDir + "/", downTaskIndex++);
                    }
                }


                String[] recordList = (responseData.getString("record")).split("\\|");
                for (int i = 0; i < recordList.length; i++) {
                    Log.d(TAG, "recordList" + i + "==" + recordList[i]);
                    if (recordList[i] != "") {
                        doRobotdown(recordList[i], coverRecordDir + "/", downTaskIndex++);
                    }
                }

                for (int i = 0; i < content.length(); i++) {
                    JSONObject oneContent = new JSONObject(content.get(i).toString());

                    String[] contentRecordList = (oneContent.getString("record")).split("\\|");
                    for (int j = 0; j < contentRecordList.length; j++) {
                        Log.d(TAG, "contentRecordList" + j + "==" + contentRecordList[j]);
                        if (contentRecordList[j] != "") {
                            doRobotdown(contentRecordList[j], contentRecordDir + "/" + id + "/", downTaskIndex++);
                        }
                    }

                }

                int runCount = 0, totalCount = downTaskIndex, curCount = downTaskIndex;
                SendMessageToUI(2, 0, "");

                Log.d(TAG, "downTaskIndex" + downTaskIndex + "==" + downTaskMap.size());

                while (true && downTaskMap.size() > 0) {
                    sleep(1000);
                    if (curCount - downTaskMap.size() > totalCount / 5) {
                        curCount = downTaskMap.size();
                        SendMessageToUI(2, ((totalCount - downTaskMap.size()) * 100 / totalCount) - 20, "");
                    }
                    if (runCount > 8 && (runCount % 15) == 0) {
                        playSound(voiceDir + "20602010.mp3");
                    }
                    Log.d(TAG, "downTaskMap.size()" + downTaskMap.size());

                    if (runCount++ > 600 || downError || read_state != read_started) {
                        Log.d(TAG, "wait down break=======" + downTaskMap.size());
                        if (read_state == read_started && downTaskIndex > 0) {
                            Log.d(TAG, "network fail");
                            playSound(voiceDir + "20602007.mp3");

                        }

                        for (Call value : downTaskMap.values()) {
                            Log.d(TAG, "cancel task");
                            value.cancel();
                        }
                        OldCoverID = -1;
                        downTaskIndex = 0;
                        downError = false;
                        SendMessageToUI(5, 0, "");
                        return;

                    }

                }

                if (!downError) {
                    if (initCover == 0) {
                        Log.d(TAG, "LoadCoverFromDir before time=" + System.currentTimeMillis());
                        //LoadCoverFromDir(1);
                        Log.d(TAG, "LoadCoverFromDir after time=" + System.currentTimeMillis());
                    } else {
                        for (int i = 0; i < imageList.length; i++) {
                            int lastIndex = imageList[i].lastIndexOf("/");
                            String fileName = imageList[i].substring(lastIndex + 1);
                            Log.d(TAG, "AddOneCover before time=" + System.currentTimeMillis());
                            //AddOneCover(fileName);
                            Log.d(TAG, "AddOneCover after time=" + System.currentTimeMillis());
                        }
                    }
                    //保存到本地数据库
//                    PictureBookData bookDataSave = new PictureBookData();
//                    bookDataSave.setBookId(id);
//                    bookDataSave.setType(type);
//                    bookDataSave.setName(name);
//                    if (bookDataSave.save()) {
//                        Log.d(TAG, "save ok");
//                    } else {
//                        Log.d(TAG, "save fail");
//                    }

                    SendMessageToUI(2, 90, "");

                    IsCoverUpdate = true;
                    Log.d(TAG, "SaveCover before time=" + System.currentTimeMillis());
                    SaveCover();
                    Log.d(TAG, "SaveCover after time=" + System.currentTimeMillis());

                    SendMessageToUI(2, 100, "");

                    File file = new File(coverFlagDir,Integer.toString(id));
                    if(!file.exists()) {
                        try {
                            file.createNewFile();
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            Log.d(TAG, "downError =" + downError +"OldCoverID =" + "id= "+id );
            if (!downError && id != OldCoverID) {

                OldCoverID = CoverID = id;
                initCover = 1;
                if (book_num > 0) {
                    playSound(voiceDir + "20602012.mp3");
                }

                SendMessageToUI(5, 0, "");

                Installing = true;
                int count = 0;
                Log.d(TAG, "GlobalParameter.ContentFlg" + GlobalParameter.ContentFlg );
                //检测到新封面，等待内容检索结束后在加载新封面的内容。
                while (GlobalParameter.ContentFlg == false) {
                    sleep(20);
                    if (count++ > 1000) {
                        break;
                    }
                }
                Log.d(TAG, "ReloadContenData start time=" + System.currentTimeMillis());
                ReloadContenData(OldCoverID * 10 + 1);
                Log.d(TAG, "ReloadContenData end time=" + System.currentTimeMillis());
                SendMessageToUI(1, 0, "");  //更新UI，开始阅读

                //Log.d(TAG, "play=" + coverRecordDir+"/"+OldCoverID+"0.mp3");
                cur_duration = 0;
                play_type = 1;
                startTurningTime = System.currentTimeMillis();
                OldContentID = -1;
                SendMessageToUI(5, 0, name);
                if ((new File(coverRecordDir + "/" + OldCoverID + "0.mp3")).exists()) {
                    playSound(coverRecordDir + "/" + OldCoverID + "0.mp3");
                } else if ((new File(coverRecordDir + "/" + OldCoverID + "1.mp3")).exists()) {
                    playSound(coverRecordDir + "/" + OldCoverID + "1.mp3");
                }
                synchronized (AnsyHandleShare) {
                    i_event = 3;
                    AnsyHandleShare.notify();
                }

                Installing = false;

                //重置无内容次数
                noContentCount = 0;


                if ((new File(coverRecordDir + "/" + (id * 10) + ".mp3")).exists()) {
                    playSound(coverRecordDir + "/" + (id * 10) + ".mp3");
                } else if ((new File(coverRecordDir + "/" + (id * 10 + 1) + ".mp3")).exists()) {
                    playSound(coverRecordDir + "/" + (id * 10 + 1) + ".mp3");
                }
            }
            //ReloadContenData(id * 10 + feature_type);//for testing,should use the type from server


        } catch (Exception e) {

        } finally {
            Installing = false;
        }
    }

    public static void copyFilesFromAssets(Context context, String assetsPath, String savePath) {
        try {
            String fileNames[] = context.getAssets().list(assetsPath);// 获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {// 如果是目录
                File file = new File(savePath);
                file.mkdirs();// 如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesFromAssets(context, assetsPath + "/" + fileName,
                            savePath + "/" + fileName);
                }
            } else {// 如果是文件
                InputStream is = context.getAssets().open(assetsPath);
                FileOutputStream fos = new FileOutputStream(new File(savePath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void InitData() {
        Log.d(TAG, "InitData");
        Process process = null;
        DataOutputStream os = null;
        try {
            //创建数据文件目录；
            createDir(bookRootDir);
            createDir(coverDir);
            createDir(coverDataDir);
            createDir(coverRecordDir);
            createDir(coverFlagDir);
            createDir(coverImageDir);
            createDir(contentDir);
            createDir(contentRecordDir);
            createDir(voiceRootDir);

//            if (!(new File(bookRootDir + "/my_find_object.ini")).exists()) {
//                Log.d(TAG, "Init my_find_object.ini");
//                InputStream is = mContext.getAssets().open("my_find_object.ini");
//                FileOutputStream fos = new FileOutputStream(new File(bookRootDir + "/my_find_object.ini"));
//                byte[] buffer = new byte[1024];
//                int byteCount = 0;
//                while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
//                    // buffer字节
//                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
//                }
//                fos.flush();// 刷新缓冲区
//                is.close();
//                fos.close();
//            }
            //copyFilesFromAssets(mContext,"voice",voiceRootDir);

        } catch (Exception e) {
            e.printStackTrace();

        }

    }


    public void ReleaseParameters() {
        Log.d(TAG, "ENTER ReleaseParameters");
        GlobalParameter.CoverFlg = true;
        GlobalParameter.ContentFlg = true;
        OldCoverID = -1; //记录当前系统最后一次成功识别到的绘本封面
        CoverID = -1;    //记录当前识别到的封面
        OldContentID = -1;//记录当前系统最后一次成功识别到的绘本内容页
        ContentID = -1;   //记录当前系统识别到的绘本内容页
        LocalCoverFailCount = 0;
        CoverDetectTime = 0;
        CoverDetectPeriod = 0;
        IsValidImageFlg = true;
        //audioPlayer.exit();
        audioUrl = "";
        initCover = 0;
        Installing = false;
        firstNoCvoerCount = 0;   //开始阅读后，有图像但识别不到封面的次数；
        firstWithoutImageCount = 0;   //开始阅读后，无图像的次数；
        startTurningTime = 0;   //第一次识别到封面或识别到新一页的时间
        noContentCount = 0;   //识别到封面开读后，识别不到内容的次数
        networkFailCount = 0;   //后台识别到封面，但网络交互识别的次数
        play_type = 0;
        mRgba_id = 0;

        //playSound(voiceDir + "20602004.mp3");

    }

    /**
     * 识别绘本的子线程
     */
    public class CoverDetectionThread extends Thread {
        public void run() {
            coverHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case 1:
                        case 2:
                            try {
                                //Log.d(TAG, "Cover search before time=" + System.currentTimeMillis());
                                GlobalParameter.CoverFlg = false;
                                if (read_state != read_started || Installing == true) {
                                    //Log.d(TAG, "intalling data ignore CoverDetectionThread");
                                    GlobalParameter.CoverFlg = true;
                                    break;
                                }

                                CoverID = -1;
                                if (msg.what == 1) {
                                    CoverID = CoverDetection();
                                } else {
                                    CoverID = CoverDetection2();
                                }


                                if (CoverID >= 10) {
                                    CoverID = CoverID / 10;
                                    LocalCoverFailCount = 0;

                                    // 重置no content  count
                                    if (noContentCount > 0) {
                                        noContentCount = 0;
                                    }
                                }
                                Log.d(TAG, "CoverID===" + CoverID);
                        /*if (-1 != CoverID) {
                            mCharRecognitionResult = String.valueOf(CoverID);
                            GlobalParameter.UIHandler.sendEmptyMessage(2);//唤醒mainActivity线程 更新界面 查找结果
                        }*/
                                if (read_state != read_started || Installing == true) {
                                    GlobalParameter.CoverFlg = true;
                                    break;
                                }
                                if (-1 != CoverID && CoverID != OldCoverID) {
                                    int count = 0;
                                    OldCoverID = CoverID;//更新书ID
                                    //找到绘本啦
                                    playSound(voiceDir + "20602012.mp3");

                                    List<PictureBookData> bookData = LitePal.where("book_id = ?", String.valueOf(CoverID)).find(PictureBookData.class);
                                    if (bookData.size() > 0) {
                                        SendMessageToUI(5, 0, bookData.get(0).getName());  //更新UI，开始阅读
                                    }

                                    readList.add(OldCoverID);

                                    Installing = true;

                                    //检测到新封面，等待内容检索结束后在加载新封面的内容。
                                    while (GlobalParameter.ContentFlg == false) {
                                        sleep(20);
                                        if (count++ > 1000) {
                                            break;
                                        }
                                    }
                                    //Log.d(TAG, "ReloadContenData start time=" + System.currentTimeMillis());
                                    ReloadContenData(OldCoverID * 10 + 1);
                                    //Log.d(TAG, "ReloadContenData end time=" + System.currentTimeMillis());
                                    SendMessageToUI(1, 0, "");  //更新UI，开始阅读

                                    //Log.d(TAG, "play=" + coverRecordDir+"/"+OldCoverID+"0.mp3");
                                    cur_duration = 0;
                                    play_type = 1;
                                    startTurningTime = System.currentTimeMillis();
                                    OldContentID = -1;
                                    //保存获取的封面录音
                                    if ((new File(coverRecordDir + "/" + OldCoverID + "0.mp3")).exists()) {
                                        playSound(coverRecordDir + "/" + OldCoverID + "0.mp3");
                                    } else if ((new File(coverRecordDir + "/" + OldCoverID + "1.mp3")).exists()) {
                                        playSound(coverRecordDir + "/" + OldCoverID + "1.mp3");
                                    }
                                    synchronized (AnsyHandleShare) {
                                        i_event = 3;
                                        AnsyHandleShare.notify();
                                    }

                                    Installing = false;
                                    //GlobalParameter.ContentFlg = true;
                                } else if (-1 != CoverID && CoverID == OldCoverID && OldContentID == -1) {
                                    if (cur_duration > 0 && System.currentTimeMillis() - startTurningTime > cur_duration) {
                                        Log.d(TAG, "cover to page");
                                        cur_duration = 0;
                                        SendMessageToUI(4, 0, ""); //读完一页
                                        startTurningTime = System.currentTimeMillis();
                                        playSound(voiceDir + "20602005.mp3");
                                    }

                                } else if (-1 == CoverID) {
//                                    if (OldCoverID == -1 && SendToServiceFlg && ++LocalCoverFailCount >= 3) {
//                                        LocalCoverFailCount = 0;
//                                        //唤醒 上传服务器线程
//                                        Log.d(TAG, "no cover send to server===");
//                                        synchronized (SendToServiceShare) {
//                                            SendToServiceShare.notify();
//                                        }
//                                    } else if (OldCoverID != -1 && SendToServiceFlg && ++LocalCoverFailCount >= 2) {
//                                        LocalCoverFailCount = 0;
//                                        Log.d(TAG, "have cover send to server===");
//                                        synchronized (SendToServiceShare) {
//                                            SendToServiceShare.notify();
//                                        }
//
//                                    }
                                    if (SendToServiceFlg) {
                                        if (msg.what == 1) {
                                            mRgba_id = 0;
                                        } else {
                                            mRgba_id = 2;
                                        }
                                        synchronized (SendToServiceShare) {
                                            SendToServiceShare.notify();
                                        }
                                    }

                                    if (OldCoverID == -1 && Installing == true) {
                                        if (++firstNoCvoerCount > 8) {
                                            firstNoCvoerCount = -10;
                                            playSound(voiceDir + "20602003.mp3");
                                        }
                                    }
                                }
                                //Log.d(TAG, "Cover search end time=" + System.currentTimeMillis());
                                GlobalParameter.CoverFlg = true;

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            break;
                    }
                    super.handleMessage(msg);
                }
            };
        }
    }

    public class ContentDetectionThread extends Thread {
        public void run() {

            contentHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case 1:
                        case 2:
                            try {
                                //Log.d(TAG, "Content search before time=" + System.currentTimeMillis());
                                GlobalParameter.ContentFlg = false;
                                if (read_state != read_started || Installing == true) {
                                    Log.d(TAG, "intalling data ignore ContentDetectionThread");
                                    GlobalParameter.ContentFlg = true;
                                    break;
                                }
                                ContentID = -1;
                                if (msg.what == 1) {
                                    ContentID = ContentDetection();
                                } else {
                                    ContentID = ContentDetection2();
                                }
                                //Log.d(TAG, "Content search end time=" + System.currentTimeMillis());
                                Log.d(TAG, "ContentID=" + ContentID);
                                if (ContentID != -1) {

                                    ContentID /= 10;
                                    LocalCoverFailCount = 0; //当检测到内容页，则重置通过服务器查找封面的计数，避免不必要的访问服务器
                                    if (noContentCount > 0) {
                                        noContentCount = 0;
                                    }
                                } else {
                                    LocalCoverFailCount++;

                                    if (++noContentCount > 16) {
                                        noContentCount = -10;
                                        Log.d(TAG, "noContentCount");
                                        if (audioPlayer.getPlayerState() == 0) {
                                            playSound(voiceDir + "20602009.mp3");
                                        }

                                    }

                                }

                                if (ContentID != -1) {
                            /*GlobalParameter.UIHandler.sendEmptyMessage(4);//唤醒mainActivity线程 更新界面 查找结果
                            Message tempMsg = GlobalParameter.UIHandler.obtainMessage();
                            tempMsg.what = 4;
                            tempMsg.obj = String.valueOf(ContentID);
                            GlobalParameter.UIHandler.sendMessage(tempMsg);*/
                                }
                                if (-1 != ContentID && ContentID != OldContentID) {
                                    OldContentID = ContentID;//更新页码
                                    SendMessageToUI(3, 0, ""); //翻页更新UI
                                    int audioID0 = OldCoverID * 10000 + ContentID * 10 + 0;
                                    int audioID1 = OldCoverID * 10000 + ContentID * 10 + 1;

                                    cur_duration = 0;//重置时长
                                    startTurningTime = System.currentTimeMillis();
                                    play_type = 1;
                                    if ((new File(contentRecordDir + "/" + OldCoverID + "/" + audioID0 + ".mp3")).exists()) {
                                        playSound(contentRecordDir + "/" + OldCoverID + "/" + audioID0 + ".mp3");
                                    } else if ((new File(contentRecordDir + "/" + OldCoverID + "/" + audioID1 + ".mp3")).exists()) {
                                        playSound(contentRecordDir + "/" + OldCoverID + "/" + audioID1 + ".mp3");
                                    }
                                } else if (-1 != ContentID && ContentID == OldContentID) {
                                    if (cur_duration > 0 && System.currentTimeMillis() - startTurningTime > cur_duration) {
                                        Log.d(TAG, "content to page");
                                        cur_duration = 0;
                                        SendMessageToUI(4, 0, ""); //读完一页
                                        startTurningTime = System.currentTimeMillis();
                                        playSound(voiceDir + "20602005.mp3");
                                    }
                                }
                                //Log.d(TAG, "Content search end time=" + System.currentTimeMillis());
                                GlobalParameter.ContentFlg = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            break;
                    }
                    super.handleMessage(msg);
                }
            };

        }
    }

    public class SendToServiceThread extends Thread {

        public void run() {
            String s_id = "";

            synchronized (SendToServiceShare) {
                while (true) {
                    try {
                        SendToServiceShare.wait();

                        if (read_state != read_started || Installing == true) {
                            SendToServiceFlg = true;
                            continue;
                        }

                        SendToServiceFlg = false;

                        //生成图像尺寸信息
                        short height, width;
                        if (mRgba_id == 0) {
                            height = (short) mRgba.rows();
                            width = (short) mRgba.cols();
                        } else {
                            height = (short) mRgba_2.rows();
                            width = (short) mRgba_2.cols();
                        }
                        //Log.d(TAG, "height=="+height+ "  width=="+width);
                        byte[] size = new byte[4];
                        size[0] = (byte) (height & 0xff);
                        size[1] = (byte) (height >> 8 & 0xff);
                        size[2] = (byte) (width & 0xff);
                        size[3] = (byte) (width >> 8 & 0xff);


                        //图像RGB数据
                        if (mRgba_id == 0) {
                            rgbData = new byte[mRgba.rows() * mRgba.cols() * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
                            mRgba.get(0, 0, rgbData);
                        } else {
                            rgbData = new byte[mRgba_2.rows() * mRgba_2.cols() * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
                            mRgba_2.get(0, 0, rgbData);
                        }


                        //合并尺寸和数据数组
                        byte[] imageData = new byte[size.length + rgbData.length];
                        System.arraycopy(size, 0, imageData, 0, size.length);
                        System.arraycopy(rgbData, 0, imageData, size.length, rgbData.length);

                        Log.d(TAG, "Send to Service before time=" + System.currentTimeMillis());
                        //Log.d(TAG, "rgbData[100]=" + imageData[100]+ "size"+height+width);
                        String response_str = HttpApiHelper.getInstance().doBookCoverSearchPOST(imageData, null);

                        Log.d(TAG, "response=" + response_str);

                        if (read_state != read_started || Installing == true) {
                            SendToServiceFlg = true;
                            continue;
                        }

                        if (response_str != "0") {
                            //ServerDataContent content = JSON.parseObject(response_str, ServerDataContent.class);
                            downAndInstallData(response_str);
                               /* while (true){
                                    sleep(100);
                                }*/
                        } else {

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        Log.d(TAG, "Send to Service after time=" + System.currentTimeMillis());
                        SendToServiceFlg = true;
                        rgbData = null;
                    }
                }
            }
        }
    }

    public class ImagePreThread extends Thread {
        public void run() {
            synchronized (GlobalParameter.imagePreShare) {
                while (true) {
                    try {
                        GlobalParameter.imagePreShare.wait();
                        //Log.d(TAG,"ImagePreThread="+System.currentTimeMillis());
                        if (read_state != read_started || Installing == true) {
                            //Log.d(TAG, "intalling data ignore image handle");
                            GlobalParameter.ImagePreThreadFlag = true;
                            continue;
                        }
                        /*Core.flip(GlobalParameter.curImage, GlobalParameter.curImage, -1);
                        Mat destImage = new Mat(GlobalParameter.curImage.rows(), GlobalParameter.curImage.cols(), GlobalParameter.curImage.type());
                        GlobalParameter.curImage.copyTo(destImage);
                        Core.flip(GlobalParameter.curImage, GlobalParameter.curImage, -1);
                        Imgproc.warpPerspective(GlobalParameter.curImage,destImage,transfromMat,GlobalParameter.curImage.size(),Imgproc.INTER_LINEAR);
                        mRgba = destImage.clone();
*/
//                        Core.flip(GlobalParameter.curImage, GlobalParameter.curImage, -1);
//                        Mat destImage = new Mat(GlobalParameter.curImage.rows()*2, GlobalParameter.curImage.cols()*2, GlobalParameter.curImage.type());
//                        Imgproc.warpPerspective(GlobalParameter.curImage,destImage,transfromMat,destImage.size(),Imgproc.INTER_LINEAR);
//                        mRgba = destImage.clone();
//                        GlobalParameter.curImage = destImage.clone();

//
//                        Core.flip(GlobalParameter.curImage, GlobalParameter.curImage, -1);
//                        mRgba = GlobalParameter.curImage.clone();

                        // GlobalParameter.UIHandler.sendEmptyMessage(1);//唤醒mainActivity线程 更新界面 查找结果

                        //sleep(10);
                        // Log.d(TAG,"ImagePreThread="+System.currentTimeMillis());
                        //

                        //Log.d(TAG,"CameraStatus=" + GlobalParameter.CameraStatus );
//                        if (0 == IsValidImage(GlobalParameter.curImage.nativeObj) || GlobalParameter.CameraStatus == 0) {
//                            IsValidImageFlg = true;
//                            if (OldCoverID == -1 && Installing == false) {
//                                if (++firstWithoutImageCount > 100) {
//                                    Log.d(TAG, "firstWithoutImageCount=" + firstWithoutImageCount);
//                                    firstWithoutImageCount = -100;
//                                    playSound(voiceDir + "20602002.mp3");
//                                }
//                            }
//                            GlobalParameter.ImagePreThreadFlag = true;
//                            CoverID = -1;
//                            ContentID = -1;
//
//                        } else {
                        if (GlobalParameter.CameraStatus != 0) {
                            IsValidImageFlg = false;
                            firstWithoutImageCount = 0;

                            //图像采集调试 与DebugActivity配合查看图像采集纠正效果
                            if (false) {
                                GlobalParameter.debugCurImage = GlobalParameter.curImage.clone();
                                byte[] destImage = imageTransform(GlobalParameter.curImage.nativeObj);
                                mRgba = new Mat(GlobalParameter.curImage.rows(), GlobalParameter.curImage.cols(), GlobalParameter.curImage.type());
                                mRgba.put(0, 0, destImage);
                                FeatureCompute(mRgba.nativeObj);
                                GlobalParameter.debugCurImage2 = mRgba.clone();
                                Log.d(TAG,"sendMessage="+System.currentTimeMillis());
                                Message tempMsg = GlobalParameter.DebugUIHandler.obtainMessage();
                                tempMsg.what = 1;
                                tempMsg.arg1 = 0;
                                tempMsg.obj = "";
                                GlobalParameter.DebugUIHandler.sendMessage(tempMsg);
                                GlobalParameter.ImagePreThreadFlag = true;
                                continue;
                            } else {
                                mRgba = GlobalParameter.curImage.clone();
                                //byte[] destImage = imageTransform(GlobalParameter.curImage.nativeObj);
                                //Mat tRgba = new Mat(GlobalParameter.curImage.rows(), GlobalParameter.curImage.cols(), GlobalParameter.curImage.type());
                                //tRgba.put(0, 0, destImage);
                                FeatureCompute(mRgba.nativeObj);
                            }
//                            String filename = "/sdcard/imgs/"+ System.currentTimeMillis()+".bmp";
//                            System.out.println(filename);
//                            Imgcodecs.imwrite(filename, mRgba);

                            //GlobalParameter.curImage = mRgba.clone();
                            //GlobalParameter.curBitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
                            //Utils.matToBitmap(GlobalParameter.curImage, GlobalParameter.curBitmap);
                            //GlobalParameter.UIHandler.sendEmptyMessage(1);//唤醒mainActivity线程 更新界面 查找结果
                            //Log.d(TAG,"GlobalParameter.curImage row="+GlobalParameter.curImage.rows()+"col="+GlobalParameter.curImage.cols());
                            //显示图片到界面使用的代码
                            //Log.d(TAG,"FeatureCompute before time="+System.currentTimeMillis());

                            //Log.d(TAG,"FeatureCompute end time="+System.currentTimeMillis());
                            //ContentFlg = true;
                            //CoverFlg = true;
                            long time = System.currentTimeMillis();
                            if (OldCoverID != -1) {
                                contentHandler.sendEmptyMessage(1);
                                if (LocalCoverFailCount > 3 || time - CoverDetectTime > 8000) {
                                    CoverDetectTime = time;
                                    LocalCoverFailCount = 0;
                                    if (SendToServiceFlg) {
                                        mRgba_id = 0;
                                        Log.d(TAG,"SendToServiceShare="+System.currentTimeMillis());
                                        synchronized (SendToServiceShare) {
                                            SendToServiceShare.notify();
                                        }
                                    }
                                    //coverHandler.sendEmptyMessage(1);
                                }
                            } else {
                                if (time - CoverDetectTime > 3000) {
                                    CoverDetectTime = time;
                                if (SendToServiceFlg) {
                                    mRgba_id = 0;
                                    SendToServiceFlg = false;
                                    Log.d(TAG, "SendToServiceShare2222=" + System.currentTimeMillis());
                                    synchronized (SendToServiceShare) {
                                        SendToServiceShare.notify();
                                    }
                                }
                            }
                                //coverHandler.sendEmptyMessage(1);
                            }


//                            if (initCover != 0) {
//                                if (OldCoverID == -1) {
//                                    synchronized (CoverShare) {
//                                        CoverShare.notify();
//                                    }
//                                } else {  //当已检测到书正在阅读，检测封面线程周期性工作，不需要每一帧都检测封面，加快内容检测线程的执行速度
//                                    if (++CoverDetectPeriod >= 3) {
//                                        ;
//                                        CoverDetectPeriod = 0;
//                                        synchronized (CoverShare) {
//                                            CoverShare.notify();
//                                        }
//                                    } else {
//                                        GlobalParameter.CoverFlg = true;
//                                    }
//                                }
//                            } else {
//                                if (SendToServiceFlg && OldCoverID == -1 && ++LocalCoverFailCount > 3) {
//                                    Log.d(TAG, "======SEND TO SERVER====");
//                                    LocalCoverFailCount = 0;
//                                    synchronized (SendToServiceShare) {
//                                        SendToServiceShare.notify();
//                                    }
//                                    GlobalParameter.CoverFlg = true;
//                                } else {
//
//
//                                    //SendToServiceFlg = true;
//                                    GlobalParameter.CoverFlg = true;
//                                }
//                            }
//
//                            if (OldCoverID != -1) {
//                                GlobalParameter.CoverFlg = true;
//                            }

                        }
                        GlobalParameter.ImagePreThreadFlag = true;

                    } catch (Exception e) {

                    }
                }
            }
        }
    }


    public class ImagePreThread_2 extends Thread {
        public void run() {
            synchronized (GlobalParameter.imagePreShare_2) {
                while (true) {
                    try {
                        GlobalParameter.imagePreShare_2.wait();
                        //Log.d(TAG,"ImagePreThread="+System.currentTimeMillis());
                        if (read_state != read_started || Installing == true) {
                            //Log.d(TAG, "intalling data ignore image handle222");
                            GlobalParameter.ImagePreThreadFlag_2 = true;
                            continue;
                        }
                        /*Core.flip(GlobalParameter.curImage, GlobalParameter.curImage, -1);
                        Mat destImage = new Mat(GlobalParameter.curImage.rows(), GlobalParameter.curImage.cols(), GlobalParameter.curImage.type());
                        GlobalParameter.curImage.copyTo(destImage);
                        Core.flip(GlobalParameter.curImage, GlobalParameter.curImage, -1);
                        Imgproc.warpPerspective(GlobalParameter.curImage,destImage,transfromMat,GlobalParameter.curImage.size(),Imgproc.INTER_LINEAR);
                        mRgba = destImage.clone();
*/
//                        Core.flip(GlobalParameter.curImage, GlobalParameter.curImage, -1);
//                        Mat destImage = new Mat(GlobalParameter.curImage.rows()*2, GlobalParameter.curImage.cols()*2, GlobalParameter.curImage.type());
//                        Imgproc.warpPerspective(GlobalParameter.curImage,destImage,transfromMat,destImage.size(),Imgproc.INTER_LINEAR);
//                        mRgba = destImage.clone();
//                        GlobalParameter.curImage = destImage.clone();

//
//                        Core.flip(GlobalParameter.curImage, GlobalParameter.curImage, -1);
//                        mRgba = GlobalParameter.curImage.clone();

                        // GlobalParameter.UIHandler.sendEmptyMessage(1);//唤醒mainActivity线程 更新界面 查找结果

                        //sleep(10);
                        // Log.d(TAG,"ImagePreThread="+System.currentTimeMillis());
                        //

                        //Log.d(TAG,"CameraStatus=" + GlobalParameter.CameraStatus );
//                        if (0 == IsValidImage(GlobalParameter.curImage_2.nativeObj) || GlobalParameter.CameraStatus == 0) {
//                            IsValidImageFlg = true;
//                            if (OldCoverID == -1 && Installing == false) {
//                                if (++firstWithoutImageCount > 100) {
//                                    Log.d(TAG, "firstWithoutImageCount=" + firstWithoutImageCount);
//                                    firstWithoutImageCount = -100;
//                                    playSound(voiceDir + "20602002.mp3");
//                                }
//                            }
//                            GlobalParameter.ImagePreThreadFlag_2 = true;
//                            CoverID = -1;
//                            ContentID = -1;
//
//                        } else {
//                            IsValidImageFlg = false;
//                            firstWithoutImageCount = 0;
                        Core.flip(GlobalParameter.curImage_2, GlobalParameter.curImage_2, -1);
                        byte[] destImage = imageTransform(GlobalParameter.curImage_2.nativeObj);
                        mRgba_2 = new Mat(GlobalParameter.curImage_2.rows(), GlobalParameter.curImage_2.cols(), GlobalParameter.curImage_2.type());
                        mRgba_2.put(0, 0, destImage);
                        //GlobalParameter.curImage = mRgba.clone();
                        //GlobalParameter.curBitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
                        //Utils.matToBitmap(GlobalParameter.curImage, GlobalParameter.curBitmap);
                        //GlobalParameter.UIHandler.sendEmptyMessage(1);//唤醒mainActivity线程 更新界面 查找结果
                        //Log.d(TAG,"GlobalParameter.curImage row="+GlobalParameter.curImage.rows()+"col="+GlobalParameter.curImage.cols());
                        //显示图片到界面使用的代码
                        //Log.d(TAG,"FeatureCompute_2 before time="+System.currentTimeMillis());
                        FeatureCompute2(mRgba_2.nativeObj);
                        //Log.d(TAG,"FeatureCompute_2 end time="+System.currentTimeMillis());
                        //ContentFlg = true;
                        //CoverFlg = true;

                        if (OldCoverID != -1) {
                            contentHandler2.sendEmptyMessage(2);
                            if (LocalCoverFailCount > 3) {
                                LocalCoverFailCount = 0;
                                coverHandler.sendEmptyMessage(2);
                            }
                        } else {
                            coverHandler.sendEmptyMessage(2);
                        }
                        GlobalParameter.ImagePreThreadFlag_2 = true;

                        //}

                    } catch (Exception e) {

                    }
                }
            }
        }
    }

    public class PlayAudioThread extends Thread {
        public void run() {
            synchronized (PlayerShare) {
                while (true) {
                    try {
                        PlayerShare.wait();
                        if (audioUrl != "") {
                            //Log.d(TAG, "PlayAudioThread: play" + audioUrl + "type=" + play_type);
                            if (play_type == 0) {
                                audioPlayer.playLocal(audioUrl);
                            } else {
                                play_type = 0;
                                cur_duration = audioPlayer.playLocalDuration(audioUrl);
                                //Log.d(TAG, "get duration=" + cur_duration);
                            }
                        }
                        //Log.d(TAG, "PlayAudioThread: reset url");
                        audioUrl = "";
                    } catch (Exception e) {

                    }
                }
            }
        }
    }

    public class AnsyHandleThread extends Thread {
        public void run() {
            synchronized (AnsyHandleShare) {
                while (true) {
                    try {
                        AnsyHandleShare.wait();
                        if (i_event == 0) {  //start read book
                            Log.d(TAG, "start handle.....");
                            readList.clear();

                            if ((new File(coverDataDir + "/cover_surf.data")).exists()) {
                                initCover = 1;
                            } else {
                                initCover = 0;
                                //LitePal.deleteAll(PictureBookData.class);
                            }
                            if (initCover != 0) {
                                Log.d(TAG, "load cover.....");
//                                if (LoadCoverFromFile(1) != 0) {
//                                    Log.d(TAG, "reload cover.....");
//                                    LoadCoverFromDir(1);
//                                    SaveCover();
//                                }
                                Log.d(TAG, "load cover.....end");
                            }

                            Log.d(TAG, "delete database");
                            //LitePal.deleteAll(PictureBookData.class);

                            //StatisticalUtil.getInstance().StartReadTiming();
                            feature_type = 1; //SURF

                            GlobalParameter.CameradDeviceStatus = 0; //idle closed

                            Message tempMsg1 = GlobalParameter.CameraServiceHandler.obtainMessage();
                            tempMsg1.what = GlobalParameter.Camera_Cmd_Open;
                            GlobalParameter.CameraServiceHandler.sendMessage(tempMsg1);

                            GlobalParameter.CameradDeviceStatus = 1; //opening

                            read_state = read_started;

                        } else if (i_event == 1) {  //exit read book
                            Log.d(TAG, "exit handle.....");
                            if (IsCoverUpdate) {

                                IsCoverUpdate = false;

                            }
                            Message tempMsg4 = GlobalParameter.CameraServiceHandler.obtainMessage();
                            tempMsg4.what = GlobalParameter.Camera_Cmd_Release;
                            GlobalParameter.CameraServiceHandler.sendMessage(tempMsg4);
                            GlobalParameter.CameraStatus = 0;
                            ReleaseParameters();

                            Log.d(TAG, "readList size=" + readList.size());
                            int[] ids = new int[readList.size()];
                            for (int i = 0; i < readList.size(); i++) {
                                Log.d(TAG, "ids[]=" + readList.get(i));
                                ids[i] = (int) readList.get(i);
                            }
                            //HttpApiHelper.getInstance().doRobotBookScanVolumeApiPut(ids);
                            sleep(500);
                            StatisticalUtil.getInstance().StopTiming();
                            sleep(2000);
                            readList.clear(); //清除绘本统计数组
                            ClearContent();
                            ClearCover();
                            read_state = read_inited;


                        } else if (i_event == 3) { //change book static
                            //Log.d(TAG, "ReadCount....."+System.currentTimeMillis());
                            StatisticalUtil.getInstance().ReadCount(OldCoverID);//读书统计
                            //Log.d(TAG, "ReadCount end....."+System.currentTimeMillis());
                        }
                    } catch (Exception e) {

                    } finally {
                        i_event = 0;
                    }
                }
            }
        }
    }

    public class HeartBeatThread extends Thread {
        public void run() {
            synchronized (HeartBeatShare) {
                while (true) {
                    try {
                        HeartBeatShare.wait();
                        while (true) {
                            sleep(1000);
                            if (GlobalParameter.Book_Status == GlobalParameter.Book_To_Finish && GlobalParameter.CameradDeviceStatus == 2) {
                                GlobalParameter.CameraStatus = 0;
                                GlobalParameter.CameradDeviceStatus = 0;
                                read_state = read_stopping;
                                int exit_count = 0;
                                while (true) {
                                    sleep(100);
                                    if (exit_count++ > 50 || (GlobalParameter.CoverFlg && GlobalParameter.ContentFlg && SendToServiceFlg)) {
                                        break;
                                    }
                                }
                                Log.d(TAG, "HeartBeatThread handle.....");
                                if (IsCoverUpdate) {

                                    IsCoverUpdate = false;

                                }
                                Message tempMsg4 = GlobalParameter.CameraServiceHandler.obtainMessage();
                                tempMsg4.what = GlobalParameter.Camera_Cmd_Release;
                                GlobalParameter.CameraServiceHandler.sendMessage(tempMsg4);
                                GlobalParameter.CameraStatus = 0;
                                ReleaseParameters();

                                Log.d(TAG, "readList size=" + readList.size());
                                int[] ids = new int[readList.size()];
                                for (int i = 0; i < readList.size(); i++) {
                                    Log.d(TAG, "ids[]=" + readList.get(i));
                                    ids[i] = (int) readList.get(i);
                                }
                                //HttpApiHelper.getInstance().doRobotBookScanVolumeApiPut(ids);
                                sleep(500);
                                StatisticalUtil.getInstance().StopTiming();
                                sleep(2000);
                                readList.clear(); //清除绘本统计数组
                                ClearContent();
                                ClearCover();
                                read_state = read_inited;
                                GlobalParameter.Book_Status = GlobalParameter.Book_Idle;
                                break;
                            }
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }
    }


    static {
        System.loadLibrary("native-lib");
    }

    public static native int FindObjectInit();//绘本阅读初始化

    public static native int LoadCoverFromFile(int type);//从数据文件加载封面

    public static native int LoadCoverFromDir(int type);//从数据文件加载封面

    public static native int AddOneCover(String file);//增加一个封面

    public static native int SaveCover();//保存封面到数据文件

    public static native int ClearCover();//清除封面数据

    public static native int ReloadContenData(int ID);//导入指定ID的内容数据

    public static native int ClearContent();//清除内容数据

    public static native int FeatureCompute(long mat);//提取图片的特征点

    public static native int FeatureCompute2(long mat);//提取图片的特征点2

    public static native int ContentDetection();//内容查找函数

    public static native int ContentDetection2();//内容查找函数2

    public static native int CoverDetection();//封面查找

    public static native int CoverDetection2();//封面查找2

    public static native int IsValidImage(long mat);

    public static native byte[] imageTransform(long mat);//图像矫正


}

