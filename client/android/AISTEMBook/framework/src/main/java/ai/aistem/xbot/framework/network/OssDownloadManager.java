package ai.aistem.xbot.framework.network;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import ai.aistem.xbot.framework.application.DCApplication;
import ai.aistem.xbot.framework.internal.http.HttpApiEndPoint;
import ai.aistem.xbot.framework.network.util.Config;
import ai.aistem.xbot.framework.network.util.OSSAuthCredentialsProvider2;

/**
 *@author aistem
 *@date  2018-10-23
 *@describe OSS 授权下载文件，当前指定下载的buket:aistem-book,指定目录picture_book
 **/

public class OssDownloadManager {


    private static OSS mOss;
    private OSSAuthCredentialsProvider2 mCredentialProvider;

    private static OssDownloadManager instance;

    public static OssDownloadManager getInstance() {
        if (instance == null) {
            synchronized (OssDownloadManager.class) {
                instance = new OssDownloadManager();
            }
        }
        return instance;
    }

    public OssDownloadManager() {
        OSSLog.disableLog();
        mCredentialProvider = new OSSAuthCredentialsProvider2(HttpApiEndPoint.STS_SERVER_URL2, "picture_book");
        mOss = initOSS();
    }

    //TODO
    public void setDownloadBucketName(String bucketName) {
        //mCredentialProvider.setPath();
    }

    public void setDownloadPath(String path) {
        mCredentialProvider.setPath(path);
    }

    private OSS initOSS() {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(10 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(10 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求数，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        OSS oss = new OSSClient(DCApplication.app, Config.endpoint1, mCredentialProvider, conf);
        return oss;
    }

    //url:https://aistem-book.oss-cn-hangzhou.aliyuncs.com/picture_book/cover/data/10001.data
    //Bucket: aistem-book
    //Object: picture_book/cover/data/10001.data
    public OSSAsyncTask asyncGetObject(final String url, final String dir, final ResultCallback callback) {
       /* String targetBucket = "aistem-book";
        String targetObject = "picture_book/cover/data/10001.data";*/
        String targetBucket = getBucketName(url);
        String targetObject = getObjectName(url);
        GetObjectRequest get = new GetObjectRequest(targetBucket, targetObject);
        get.setProgressListener(new OSSProgressCallback<GetObjectRequest>() {
            @Override
            public void onProgress(GetObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("getobj_progress: " + currentSize + "  total_size: " + totalSize, false);
                if (callback != null)
                    callback.onProgress(currentSize, totalSize);
            }
        });

        OSSAsyncTask task = mOss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                /*callback.onSuccess(request, result);*/
                // request sucess
                InputStream inputStream = result.getObjectContent();
                byte[] buffer = new byte[2048];
                int len;
                FileOutputStream fos = null;
                try {
                    File dir0 = new File(dir);
                    if (!dir0.exists()) {
                        dir0.mkdirs();
                    }
                    File file = new File(dir0, getFileName(url));
                    fos = new FileOutputStream(file);
                    while ((len = inputStream.read(buffer)) != -1) {
                        // Process the downloaded data
                        fos.write(buffer, 0, len);
                        OSSLog.logDebug("asyncGetObjectSample", "read length: " + len, false);
                    }
                    fos.flush();
                    if(callback != null){
                        callback.onSuccess();
                    }
                    OSSLog.logDebug("asyncGetObjectSample", "download success.");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inputStream != null) inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (fos != null) fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(GetObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                if (callback != null)
                    callback.onError();
                // request exception
                if (clientExcepion != null) {
                    // client side exception
                    //clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // service side exception
                    OSSLog.logError("ErrorCode", serviceException.getErrorCode());
                    OSSLog.logError("RequestId", serviceException.getRequestId());
                    OSSLog.logError("HostId", serviceException.getHostId());
                    OSSLog.logError("RawMessage", serviceException.getRawMessage());
                }
            }
        });

        return task;
    }


    public interface ResultCallback {

        /**
         * 正常回调
         *
         * @param currentSize
         * @param totalSize
         */
        void onProgress(long currentSize, long totalSize);

        /**
         * 错误回调
         */
        void onError();

        void onSuccess();
    }

    private String getFileName(String path) {
        int separatorIndex = path.lastIndexOf("/");
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
    }

    private String getBucketName(String url) {
        int separatorIndex = url.indexOf("//");
        int separatorIndex2 = url.indexOf(".");
        return url.substring(separatorIndex + 2, separatorIndex2);
    }

    private String getObjectName(String url) {
        int separatorIndex = url.indexOf("//");
        String sub_url = url.substring(separatorIndex + 2, url.length());
        separatorIndex = sub_url.indexOf("/");
        return (separatorIndex < 0) ? sub_url : sub_url.substring(separatorIndex + 1, sub_url.length());
    }
}


