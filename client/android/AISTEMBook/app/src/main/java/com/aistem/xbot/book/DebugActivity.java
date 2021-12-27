package com.aistem.xbot.book;



/**
 * @author: aistem
 * @created: 2019/4/3
 * @desc: 图像采集调试APP
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Script.KernelID;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;

import ai.aistem.xbot.framework.application.GlobalParameter;
import ai.aistem.xbot.framework.internal.utils.MessageUtils;
import ai.aistem.xbot.framework.internal.utils.SoundPlayer;
import ai.aistem.xbot.framework.utils.AchvUtil;

public class DebugActivity extends Activity {
    private ImageView imgView1,imgView2;
    private Bitmap bmp1= null;
    private Bitmap bmp2= null;
    private static final String TAG = DebugActivity.class.getSimpleName();
    private Handler mHandler1, mHandler2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_view_layout);


        imgView1 = (ImageView) this.findViewById(R.id.imageview1);
        imgView2 = (ImageView) this.findViewById(R.id.imageview2);

        initEventAndData();

    }

    protected void initEventAndData() {


        if (mHandler2 == null) {
            mHandler2 = new Handler();
        }
        mHandler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "run: Book_StarReading!");
                MessageUtils.MessageFeed(GlobalParameter.PictureBookAPPHandler, GlobalParameter.Book_StarReading);
            }
        }, 1 * 6000);

        GlobalParameter.DebugUIHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:  // image update
                        //Log.i(TAG, "DebugUIHandler show image");
                        showImage();
                        break;
                    default:
                        break;
                }
            }
        };

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        return super.onKeyUp(keyCode, event);
    }

    private void showImage()
    {
        bmp1 =Bitmap.createBitmap( GlobalParameter.debugCurImage.width(),  GlobalParameter.debugCurImage.height(),  Bitmap.Config.ARGB_8888);
        bmp2 =Bitmap.createBitmap( GlobalParameter.debugCurImage2.width(),  GlobalParameter.debugCurImage2.height(),  Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(GlobalParameter.debugCurImage, bmp1);
        Utils.matToBitmap(GlobalParameter.debugCurImage2, bmp2);
        imgView1.setImageBitmap( bmp1 );
        imgView2.setImageBitmap( bmp2 );
    }


}