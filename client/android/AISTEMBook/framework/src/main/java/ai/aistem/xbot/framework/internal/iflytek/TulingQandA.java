package ai.aistem.xbot.framework.internal.iflytek;

/**
 * Created by aistem on 2018/2/28.
 * Modified by aistem on 2018/5/15.
 */

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ai.aistem.xbot.framework.application.GlobalParameter;
import ai.aistem.xbot.framework.internal.utils.MessageUtils;


public class TulingQandA implements Runnable {

    private static final String TAG = TulingQandA.class.getSimpleName();

    /**
     * WEB API URL
     */
    private String tulingUrl = "http://www.tuling123.com/openapi/api";
    /**
     * APK KEY
     */
    private String apiKey = "6b33c7d5d1c447aabf209de4c4b3f5e3";
    /**
     * USER ID
     */
    private String userId = "123456";

    private String question = "";

    private URL url;

    private static final Object syncObj = new Object();

    HttpURLConnection conn = null;
    JSONObject requestData;

    public TulingQandA() {
        // 创建url资源
        try {
            Log.d(TAG, "enter TulingQandA");
            requestData = new JSONObject();
            requestData.putOpt("key", apiKey); //api 1 data

            url = new URL(tulingUrl);

            /** web api2 data
             JSONObject  text = new JSONObject();
             JSONObject  inputText = new JSONObject();
             JSONObject  userInfo = new JSONObject();



             requestData.putOpt("reqType",0);
             text.put("text","你好");
             inputText.putOpt("inputText",text);
             requestData.putOpt("perception",inputText);

             userInfo.put("apiKey",apiKey);
             userInfo.put("userId","123456");
             requestData.putOpt("userInfo",userInfo);*/
        } catch (Exception e) {

        }
    }

    @Override
    public void run() {

        GlobalParameter.QandAHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case GlobalParameter.QandA_Awake:
                        Log.d(TAG, "get QandA_Awake message");
                        question = (String) msg.obj;
                        synchronized (syncObj) {
                            syncObj.notify();
                        }
                        break;

                    default:
                        Log.d(TAG, "get wrong message =" + msg.what);
                        break;
                }
            }
        };


        while (true) {
            synchronized (syncObj) {
                try {
                    syncObj.wait();
                    requestData.putOpt("info", question);
                    Log.d(TAG, "requestData==" + requestData);


                    // 建立http连接
                    conn = (HttpURLConnection) url.openConnection();
                    // 设置允许输出
                    conn.setDoOutput(true);

                    conn.setDoInput(true);

                    // 设置不用缓存
                    conn.setUseCaches(false);
                    // 设置传递方式
                    conn.setRequestMethod("POST");
                    // 设置维持长连接
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    // 设置文件字符集:
                    conn.setRequestProperty("Charset", "UTF-8");

                    // 设置文件类型:
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                    //设置连接超时时间
                    conn.setConnectTimeout(3000);

                    //System.out.println(requestData);


                    //转换为字节数组
                    byte[] data = (requestData.toString()).getBytes();


                    // 设置文件长度
                    conn.setRequestProperty("Content-Length", String.valueOf(data.length));

                    // 开始连接请求

                    conn.connect();

                    OutputStream out = conn.getOutputStream();
                    // 写入请求的字符串
                    out.write((requestData.toString()).getBytes());
                    out.flush();
                    out.close();

                    Log.d(TAG, "==========conn ==" + conn.getResponseCode());

                    // 请求返回的状态
                    if (conn.getResponseCode() == 200) {
                        //System.out.println("连接成功");
                        // 请求返回的数据
                        //InputStream in = conn.getInputStream();
                        //String a = null;
                        BufferedReader br = null;
                        String msg = "";
                        try {
                            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            msg = br.readLine(); //just one line
                            Log.i(TAG, "msg" + msg);
                            JSONObject responseData = new JSONObject(msg);

                            //text response
                            if (responseData.getInt("code") == 100000) {
                                Log.d(TAG, "==========response text=" + responseData.getString("text"));
                                MessageUtils.MessageFeed(GlobalParameter.StateEngineHandler,
                                        GlobalParameter.StateEngine_QandA_Result,
                                        responseData.getString("text"));
                            } else {
                                Log.d(TAG, "==========response code=" + responseData.getInt("code"));
                            }
                            //GlobalParameter.StateEngineHandler.sendEmptyMessage(0);

                    /*byte[] data1 = new byte[in.available()];
                    in.read(data1);
                    // 转成字符串
                    a = new String(data1);
                    Log.d(TAG,"==========response"+a);
                    System.out.println(a);*/
                        } catch (Exception e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    } else {
                        System.out.println("no++========");
                    }

                } catch (Exception e) {

                } finally {
                    conn.disconnect();
                }
            }
        }
    }
}
