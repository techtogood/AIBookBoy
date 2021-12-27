package ai.aistem.xbot.framework.application;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.SPCookieStore;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import ai.aistem.xbot.framework.R;
import ai.aistem.xbot.framework.data.AppDataManager;
import ai.aistem.xbot.framework.data.DataManager;
import ai.aistem.xbot.framework.data.prefs.AppPreferencesHelper;
import ai.aistem.xbot.framework.data.prefs.PreferencesHelper;
import ai.aistem.xbot.framework.internal.http.OkHttpClientManager;
import ai.aistem.xbot.framework.service.RobotCameraService;
import ai.aistem.xbot.framework.utils.RobotUtil;
import okhttp3.OkHttpClient;

/**
 * @author: LiQi
 * @created: 2018/5/29/15:24
 * @desc: DCApplication
 */
public class DCApplication extends Application {

    public static DCApplication app;

    public static int index = 0;

    public static DataManager mDataManager;
    //    public static MQTTClient mMQTTClient;
//    public static MainInteraction mMainInteraction;
    public static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(9);

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        //LeakCanary.install(this);
        Log.d("DCApplication", "=====Start Tdde Launcher=====");

        createDatabase();

        /**
         * 初始化数据库
         * */
        LitePal.initialize(this);

        Stetho.initializeWithDefaults(this);

        //初始化第三方HTTP请求库
        initOkGo();

        //思必驰参数
        //AIConstant.openLog();
//        AIConstant.setNewEchoEnable(true);// 打开AEC
//        AIConstant.setEchoCfgFile("AEC_ch2-2-ch1_1ref_common_20180510_v0.9.4.bin");// 设置AEC的配置文件
//        AIConstant.setRecChannel(2);

        //设置休眠时间
//        if (SPUtils.getInstance().getNormallyOnStatus()) {
//            RobotUtil.saveScreenOffTimeOut(this, Integer.MAX_VALUE);
//        } else {
//            RobotUtil.saveScreenOffTimeOut(this,
//                    GlobalParameter.SLEEP_SECONDS * 1000);
//        }

        /**
         *  后台初始化
         *  1.判断是否联网 连接网络：跳转到2 未连接网络：语音提示连接网络
         *  2.Timers/Alarm AISpeech
         *  3.RobotStateMachine
         *  5.判断TOKEN是否为空或过期,空或过期：重新请求AUTH Token
         *  6.MQTTClient
         **/
        PreferencesHelper preferencesHelper = new AppPreferencesHelper(getApplicationContext(), "robot-tddebot");
//        mAISpeechEngine = new AISpeechEngine(this);
        mDataManager = new AppDataManager(getApplicationContext(), preferencesHelper);
//        mMQTTClient = MQTTClient.getInstance(this);
//        mMainInteraction = new MainInteraction();
        /**
         * 设置消息的回调接口
         * */
//        DCMessageOperation.setDCMessageListener(mMQTTClient);

//        scheduler.execute(new Timers(this));
//        scheduler.schedule(mMainInteraction, 1, TimeUnit.SECONDS);
        scheduler.schedule(new PictureBookAPP(this), 2, TimeUnit.SECONDS);
//        scheduler.schedule(new LetterRecognitionAPP(this), 1, TimeUnit.SECONDS);
//        scheduler.schedule(new AchievementManager(), 1, TimeUnit.SECONDS);
        scheduler.schedule(DelayHandler, 6, TimeUnit.SECONDS);

        Intent intentOne = new Intent(this, RobotCameraService.class);
        startService(intentOne);
        bindService(intentOne, conn, Context.BIND_AUTO_CREATE);

        try {
            //Log.d("DCApplication","load 215082401730553.pem");
            OkHttpClientManager.getInstance()
                    .setCertificates(getAssets().open("215082401730553.pem"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable DelayHandler = new Runnable() {
        @Override
        public void run() {
            Log.d("DCApplication", " DelayHandler off light");
            RobotUtil.enableEarLight(false);
        }
    };

    //    private String DB_NAME = "unitydatabase.db"; // for LogCat
    private String DB_NAME = "robot.db"; // for LogCat
    private String DB_PATH = "/data/data/com.aistem.tddebot.launcher/databases/"; // %s is packageName

    /**
     * 拷贝assets下的数据库至data/data目录
     */
    private void createDatabase() {
        final int BUFFER_SIZE = 200000;
//        final String DB_NAME = "unitydatabase.db"; //保存的数据库文件名


        final String dbfile = DB_PATH + DB_NAME;
        try {
            if (!(new File(dbfile).exists())) {//判断数据库文件是否存在，若不存在则执行导入
                ///
                File filepath = new File(DB_PATH);
                if (!filepath.exists()) {
                    filepath.mkdirs();
                }
                ///
                if (filepath.exists()) {
                    InputStream is = getResources().openRawResource(
                            R.raw.robot); //欲导入的数据库
                    FileOutputStream fos = new FileOutputStream(dbfile);
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int count = 0;
                    while ((count = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, count);
                    }
                    fos.close();
                    is.close();
                }
            }


            Runtime chg = Runtime.getRuntime();
            try {
                chg.exec("chmod 777" + " " + dbfile).waitFor();
            } catch (Exception e) {
                Log.e("robot", "chmod failed! path = " + dbfile);
            }

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    public static RobotCameraService mRobotCameraService = null;
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            RobotCameraService.MsgBinder bd = (RobotCameraService.MsgBinder) iBinder;
            mRobotCameraService = bd.getService();
            //Log.d(TAG,mRobotCameraService.getString());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    public DataManager getDataManager() {
        return mDataManager;
    }


    private void initOkGo() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        /************************************** 配置log ******************************/
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
        //log打印级别，决定了log显示的详细程度
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
        //log颜色级别，决定了log在控制台显示的颜色
        loggingInterceptor.setColorLevel(Level.INFO);
        builder.addInterceptor(loggingInterceptor);
        /************************************** 配置超时时间 ******************************/
        //全局的读取超时时间
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        //全局的写入超时时间
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        //全局的连接超时时间
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        /************************************** 配置Cookie ******************************/
        //使用sp保持cookie，如果cookie不过期，则一直有效
        builder.cookieJar(new CookieJarImpl(new SPCookieStore(this)));
        //使用数据库保持cookie，如果cookie不过期，则一直有效
        //builder.cookieJar(new CookieJarImpl(new DBCookieStore(this)));
        //使用内存保持cookie，app退出后，cookie消失
        //builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));
        /************************************** Https配置 ******************************/
        //方法一：信任所有证书,不安全有风险
        //HttpsUtils.SSLParams sslParams1 = HttpsUtils.getSslSocketFactory();
        //方法二：自定义信任规则，校验服务端证书
        //HttpsUtils.SSLParams sslParams2 = HttpsUtils.getSslSocketFactory(new SafeTrustManager());
        //方法三：使用预埋证书，校验服务端证书（自签名证书）
        HttpsUtils.SSLParams sslParams3 = null;
        try {
            sslParams3 = HttpsUtils.getSslSocketFactory(getAssets().open("215082401730553.pem"));
            builder.sslSocketFactory(sslParams3.sSLSocketFactory, sslParams3.trustManager);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //方法四：使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
        //HttpsUtils.SSLParams sslParams4 = HttpsUtils.getSslSocketFactory(getAssets().open("xxx.bks"), "123456", getAssets().open("yyy.cer"));

        //必须调用初始化
        OkGo.getInstance().init(this)
                .setOkHttpClient(builder.build());
        //OkGo.getInstance().init(this);
    }
}
