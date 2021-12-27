package ai.aistem.xbot.framework.internal;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import ai.aistem.xbot.framework.application.GlobalParameter;
import ai.aistem.xbot.framework.internal.http.HttpApiHelper;
import ai.aistem.xbot.framework.utils.AchvUtil;


public class AchievementManager implements Runnable {

    private static final String TAG = AchievementManager.class.getSimpleName();

    @Override
    public void run() {
        Log.d(TAG, "run()");
        Looper.prepare();
        GlobalParameter.AchvManagerHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                  /*  case GlobalParameter.StudyRecords_SaveOrUpdate:
                        String studyName = (String) msg.obj;
                        AchvUtil.UpdateStudyAchievementRecords(studyName);
                        break;
                    case GlobalParameter.LetterRecords_SaveOrUpdate:
                        String letterName = (String) msg.obj;
                        if (msg.arg1 != -1) {
                            int sch_1 = msg.arg1;
                            AchvUtil.UploadLetterAchievementRecords(letterName, sch_1);
                        } else {
                            AchvUtil.UpdateLetterAchievementRecords(letterName);
                        }
                        break;
                    case GlobalParameter.CVCRecords_SaveOrUpdate:
                        String cvcName = (String) msg.obj;
                        int sch_1 = msg.arg1;
                        AchvUtil.UploadCVCAchievementRecords(cvcName, sch_1);
                        break;*/
                    case GlobalParameter.AchvRecords_Upload:
                        AchvUtil.UploadAchvRecords();
                        break;
                    case GlobalParameter.Statistics_Upload:
                        HttpApiHelper.getInstance().doTddeStatisticalPOST();
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };
        Looper.loop();
    }
}
