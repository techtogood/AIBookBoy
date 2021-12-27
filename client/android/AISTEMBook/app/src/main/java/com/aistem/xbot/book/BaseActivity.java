package com.aistem.xbot.book;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;

import com.aistem.xbot.book.fragment.BroadcastFragment;

/**
 * @author: aistem
 * @created: 2018/5/15/11:14
 * @desc: BaseActivity
 */
public abstract class BaseActivity extends FragmentActivity {
    private static final String TAG = "BaseActivity";

    private BroadcastFragment receiverFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(attachLayoutRes());
        initViews();
        initEventAndData();
        initRes();
    }

    protected void initRes() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (receiverFragment == null) {
            Log.d(TAG, "new BroadcastFragment()");
            receiverFragment = new BroadcastFragment();
        }
        if (!receiverFragment.isAdded()) {
            Log.d(TAG, "Add BroadcastFragment()");
            transaction.add(receiverFragment, null);
        }
        transaction.commit();
        fragmentManager.executePendingTransactions();
    }

    /**
     * 绑定布局文件
     *
     * @return 布局文件ID
     */
    protected abstract int attachLayoutRes();

    /**
     * 初始化视图控件
     */
    protected abstract void initViews();

    protected abstract void initEventAndData();

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 3 || keyCode == 4) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}


