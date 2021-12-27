package com.aistem.xbot.book.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.aistem.xbot.book.receiver.NetChangeReceiver;

import java.util.ArrayList;
import java.util.List;

public class BroadcastFragment extends Fragment {

    private static String TAG = BroadcastFragment.class.getSimpleName();
    private NetChangeReceiver wifiReceiver = new NetChangeReceiver();
    private List<BroadcastReceiver> lists = new ArrayList<>();
    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach():" + context.getClass().getSimpleName());
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver(wifiReceiver, WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

    public void registerReceiver(BroadcastReceiver broadcastReceiver, String... actions) {
        IntentFilter myIntentFilter = new IntentFilter();
        for (String action : actions) {
            myIntentFilter.addAction(action);
        }
        getActivity().registerReceiver(broadcastReceiver, myIntentFilter);
        lists.add(broadcastReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "on "+mContext.getClass().getSimpleName()+" :onDestroy");
        for (int n = 0; n < lists.size(); n++) {
            try {
                getActivity().unregisterReceiver(lists.get(n));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        lists.clear();
    }
}
