package com.aistem.xbot.book.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.litesuits.common.assist.Network;

import ai.aistem.xbot.framework.application.DCApplication;
import ai.aistem.xbot.framework.internal.http.HttpApiHelper;
import ai.aistem.xbot.framework.internal.http.async.PostOrGetCallback;

/**
 * @author: aistem
 * @created: 2018/6/29/9:43
 * @desc: NetChangeReceiver
 */

public class NetChangeReceiver extends BroadcastReceiver {

    private static String TAG = NetChangeReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        //LogUtil.getInstance().d("NetChangeReceiver", "================================" + info.getState() + "============================");
        //Log.d(TAG, "Context:" + context.getClass().getSimpleName() + " info:" + info.getState());
        if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {//如果断开连接
            Log.d(TAG, "on " + context.getClass().getSimpleName() + " NETWORK DISCONNECTED");
            DCApplication.index = 0;
        } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
            //判断token是否过期，若token已过期，可以用refresh_token去重新请求
            //LogUtil.getInstance().d("NetChangeReceiver", "============Init Network Connected==========");
            Log.d(TAG, "on " + context.getClass().getSimpleName() + " NETWORK CONNECTED");
            DCApplication.index++;
            if (DCApplication.index == 1) {
                if (Network.isConnected(DCApplication.app))
                    HttpApiHelper.doRobotBaseInfoApiPostAsync().listen(new PostOrGetCallback() {
                        @Override
                        public void onFinish() {
                            HttpApiHelper.doRobotBaseUsersApiGetAsync().listen(new PostOrGetCallback() {
                                @Override
                                public void onFinish() {
                                }
                            });
                        }
                    });
                else {
                    Log.e(TAG, "网络未连接");
                }
            }
        }
    }
}
