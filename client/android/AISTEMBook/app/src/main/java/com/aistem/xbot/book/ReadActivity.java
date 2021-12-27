package com.aistem.xbot.book;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import com.aistem.xbot.book.view.ColorArcProgressBar;
import com.aistem.xbot.book.view.MarqueeView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ai.aistem.xbot.framework.application.DCApplication;
import ai.aistem.xbot.framework.application.GlobalParameter;
import ai.aistem.xbot.framework.internal.utils.MessageUtils;
import ai.aistem.xbot.framework.internal.utils.SoundPlayer;
import ai.aistem.xbot.framework.utils.AchvUtil;

/**
 * @author: aistem
 * @created: 2019/4/3
 * @desc: 绘本阅读独立APP
 */
public class ReadActivity extends BaseActivity {
    private static final String TAG = ReadActivity.class.getSimpleName();

    private VideoView faceView;
    private ColorArcProgressBar downloadBar;
    private boolean DOWNLOADING = true;
    private TextView staticName;
    private MarqueeView scrollName;
    private String bookName;

    public static boolean isRead = false;

    private Handler mHandler1, mHandler2;

    @Override
    protected int attachLayoutRes() {
        return R.layout.activity_read_book_layout;
    }

    @Override
    protected void initViews() {
        staticName = findViewById(R.id.book_static_name);
        scrollName = findViewById(R.id.book_scoll_name);
        faceView = findViewById(R.id.read_book_expression);
        downloadBar = findViewById(R.id.id_download_book);
        downloadBar.setMaxValues(100);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET}, 1);
        }

        copyAssets();

    }

    @Override
    protected void initEventAndData() {

        SoundPlayer.getInstance().play("20602001");

        if (mHandler1 == null) {
            mHandler1 = new Handler();
        }
        mHandler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                displayScanFace();
            }
        }, 2 * 1000);

        if (mHandler2 == null) {
            mHandler2 = new Handler();
        }
        mHandler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "run: Book_StarReading!");
                MessageUtils.MessageFeed(GlobalParameter.PictureBookAPPHandler, GlobalParameter.Book_StarReading);
            }
        }, 6 * 1000);

        GlobalParameter.ReadUIHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:  //start reading
                        Log.d(TAG, "got msg 1");
                        displayReadFace();
                        break;
                    case 2: //downloading percent arg1 0~100 percent
                        Log.d(TAG, "got msg 2 arg1=" + msg.arg1);
                        if (msg.arg1 == 100) {
                            DOWNLOADING = true;
                            Log.d(TAG, "下载完成 ");
                            displayDownloadFinish();
                        } else if (msg.arg1 < 100) {
                            refreshDownloadBar(msg.arg1);
                            Log.d(TAG, "receiver i : " + msg.arg1);
                            if (DOWNLOADING) {
                                DOWNLOADING = !DOWNLOADING;
                                displayDownloadFace();
                            }
                        }
                        displayBookName(bookName);
                        break;
                    case 3: //paging
                        Log.d(TAG, "got msg 3");
                        displayReadFace();
                        break;
                    case 4:
                        Log.d(TAG, "got msg 4");
                        displayFinishRead();
                        break;
                    case 5:
                        Log.d(TAG, "got msg 5 book name =" + msg.obj.toString());
                        bookName = msg.obj.toString();
                        displayBookName(bookName);
                        break;
                    default:
                        break;
                }
            }
        };

    }

    private void displayBookName(String bookName) {
        if (bookName != null) {
            if (bookName.length() <= 25) {
                staticName.setVisibility(View.VISIBLE);
                scrollName.setVisibility(View.GONE);
                staticName.setText(bookName);
            } else {
                staticName.setVisibility(View.GONE);
                scrollName.setVisibility(View.VISIBLE);
                scrollName.setContent(bookName);
                scrollName.setTextDistance(50);
            }
        }

        if (downloadBar.isShown()) {
            staticName.setVisibility(View.GONE);
            scrollName.setVisibility(View.GONE);
        } else {
            staticName.setVisibility(View.VISIBLE);
            scrollName.setVisibility(View.VISIBLE);
        }
    }

    //Add by aistem
    @Override
    protected void onResume() {
        super.onResume();
        //确保打开该页面时停止语音引擎
//        DCApplication.app.getAISpeechEngine().stopTts();
//        DCApplication.app.getAISpeechEngine().stopAsr();
//        DCApplication.app.getAISpeechEngine().stopWakeup();
        isRead = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRead = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        if (mHandler1 != null) {
            mHandler1.removeCallbacksAndMessages(null);
            mHandler1 = null;
        }
        if (mHandler2 != null) {
            mHandler2.removeCallbacksAndMessages(null);
            mHandler2 = null;
        }
//        isFinish = true;
        if (GlobalParameter.Book_Status == GlobalParameter.Book_To_Strat) {
            SoundPlayer.getInstance().play("20602004");
            GlobalParameter.Book_Status = GlobalParameter.Book_To_Finish;
        }
        GlobalParameter.Book_Status = GlobalParameter.Book_Idle;
    }

    private void displayScanFace() {
        faceView.setVisibility(View.VISIBLE);
        faceView.setVideoURI(videoURI(this, R.raw.face_start_scan_book));
//        faceView.setVideoPath(videoFile("20601005"));
        faceView.start();
        faceView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                faceView.setVideoURI(videoURI(DCApplication.app, R.raw.face_cycle_scan_book));
//                faceView.setVideoPath(videoFile("20601006"));
                faceView.start();
            }
        });

    }

    private void displayReadFace() {
        if (faceView.isPlaying()) {
            faceView.pause();
            faceView.setVideoURI(videoURI(this, R.raw.face_start_read_book));
//            faceView.setVideoPath(videoFile("20601001"));
            faceView.start();
            faceView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //Toast.makeText(DCApplication.app, "ReadFace", Toast.LENGTH_SHORT).show();
                    faceView.setVideoURI(videoURI(DCApplication.app, R.raw.face_cycle_read_book));
//                    faceView.setVideoPath(videoFile("20601002"));
                    faceView.start();
                }
            });
        }
    }


    private void displayFinishRead() {
        if (faceView.isPlaying()) {
            faceView.pause();
            faceView.setVideoURI(videoURI(this, R.raw.face_finish_read_book_1));//
//            faceView.setVideoPath(videoFile("20601001"));
            faceView.start();
            faceView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d(TAG, "face_finish_read_book_2");
                    //Toast.makeText(DCApplication.app, "ReadFace", Toast.LENGTH_SHORT).show();
//                    faceView.setVideoURI(videoURI(DCApplication.app, R.raw.face_finish_read_book_2));
//                    faceView.start();
//                    displayPaddingRead();

                    displayScanFace();

                }
            });
        }
    }


    private void displayDownloadFace() {
        if (faceView.isPlaying()) {
            faceView.pause();
            faceView.setVideoURI(videoURI(this, R.raw.face_start_book_download));
//            faceView.setVideoPath(videoFile("20601008"));
            faceView.start();
            faceView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //Toast.makeText(DCApplication.app, "on Download", Toast.LENGTH_SHORT).show();
                    faceView.setVideoURI(videoURI(DCApplication.app, R.raw.face_during_book_download));
//                    faceView.setVideoPath(videoFile("20601009"));
                    faceView.start();

                }
            });
        }
    }

    private void refreshDownloadBar(int index) {
        downloadBar.setVisibility(View.VISIBLE);
        downloadBar.setCurrentValues(index);
    }


    private void displayDownloadFinish() {
        if (faceView.isPlaying()) {
            faceView.pause();
            faceView.setVideoURI(videoURI(this, R.raw.face_end_book_download));
//            faceView.setVideoPath(videoFile("20601010"));
            faceView.start();

            faceView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //Toast.makeText(DCApplication.app, "ReadFace", Toast.LENGTH_SHORT).show();
                    faceView.setVideoURI(videoURI(DCApplication.app, R.raw.face_cycle_read_book));
//                    faceView.setVideoPath(videoFile("20601002"));
                    faceView.start();
                }
            });

        }
        downloadBar.setCurrentValues(0);
        downloadBar.setVisibility(View.INVISIBLE);
    }


    /**
     * @param resId   设备的索引
     * @param context 上下文
     * @return 根据设备的索引得到对应设备演示视频的URi
     */
    public Uri videoURI(Context context, int resId) {
        String video = "android.resource://" + context.getPackageName() + "/" + resId;
        Uri videoUri = Uri.parse(video);
        return videoUri;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 297) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void copyAssets() {
        File dir = new File("/sdcard/res/sound/");
        if (!dir.exists())
            dir.mkdirs();
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("sounds");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open("sounds/"+filename);
                File outFile = new File("/sdcard/res/sound/", filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }


}
