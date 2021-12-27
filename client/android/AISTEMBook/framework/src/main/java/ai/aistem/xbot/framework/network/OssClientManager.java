package ai.aistem.xbot.framework.network;

import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectResult;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import java.io.File;
import java.util.Calendar;

import ai.aistem.xbot.framework.application.DCApplication;
import ai.aistem.xbot.framework.internal.http.HttpApiEndPoint;
import ai.aistem.xbot.framework.network.util.Config;
import ai.aistem.xbot.framework.network.util.OSSAuthCredentialsProvider2;


public class OssClientManager {


    public static final String uploadFilePath = ""; //本地文件上传地址
    //    public static final String bucket = "aistem-test";
    private static OSS mOss;


    private static OssClientManager instance;

    public static OssClientManager getInstance() {
        if (instance == null) {
            synchronized (OssClientManager.class) {
                instance = new OssClientManager();
                mOss = initOSS();
            }
        }

        return instance;
    }

    public void asyncDeleteFile(String bucket, String object) {
        // 创建删除请求
        DeleteObjectRequest delete = new DeleteObjectRequest(bucket, object);
        // 异步删除
        OSSAsyncTask deleteTask = mOss.asyncDeleteObject(delete, new OSSCompletedCallback<DeleteObjectRequest, DeleteObjectResult>() {
            @Override
            public void onSuccess(DeleteObjectRequest request, DeleteObjectResult result) {
                Log.d("asyncCopyAndDelObject", "success!");
            }

            @Override
            public void onFailure(DeleteObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }
            }

        });
    }


    public void asyncUploadFile(String object, String localFile, String bucket) {
        final long upload_start = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(upload_start);

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);

        object = "voice/" + year + "/" + month + "/" + day + "/robot/" + DCApplication.mDataManager.getRobotID() + "/" + object;

        if (object.equals("")) {
            Log.w("AsyncPutImage", "ObjectNull");
            return;
        }

        File file = new File(localFile);
        if (!file.exists()) {
            Log.w("AsyncPutImage", "FileNotExist");
            Log.w("LocalFile", localFile);
            return;
        }

        Log.d("BucketName", bucket);
        Log.d("Object", object);
        Log.d("LocalFile", localFile);
        // 构造上传请求   下面3个参数依次为bucket名，Object名，上传文件路径
        final PutObjectRequest put = new PutObjectRequest(bucket, object, localFile);

        put.setCRC64(OSSRequest.CRC64Config.YES);
        /*if (callbackAddress != null) {
            // 传入对应的上传回调参数，这里默认使用OSS提供的公共测试回调服务器地址
            put.setCallbackParam(new HashMap<String, String>() {
                {
                    put("callbackUrl", callbackAddress);
                    //callbackBody可以自定义传入的信息
                    put("callbackBody", "filename=${object}");
                }
            });
        }*/

        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
                int progress = (int) (100 * currentSize / totalSize);

            }
        });

        OSSAsyncTask task = mOss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Log.d("PutObject", "UploadSuccess");
                Log.d("ETag", result.getETag());
                Log.d("RequestId", result.getRequestId());

                long upload_end = System.currentTimeMillis();
                OSSLog.logDebug("upload cost: " + (upload_end - upload_start) / 1000f);

                if (uploadListener != null) {

                    uploadListener.onUploadSuccessToSendMqttMsg(request.getBucketName(), request.getObjectKey());
                    uploadListener.onUploadSuccess(request.getUploadFilePath(), request.getBucketName(), result.getETag(), result.getServerCallbackReturnBody());
                }
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                String clientInfo = "";
                String serviceInfo = "";
                // 请求异常
                if (clientException != null) {
                    // 本地异常如网络异常等
                    clientException.printStackTrace();
                    clientInfo = clientException.toString();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                    serviceInfo = serviceException.toString();

                }


                if (uploadListener != null) {
                    uploadListener.onUploadFailure(clientInfo, serviceInfo);
                }

            }
        });
    }

    private static OSS initOSS() {
        OSSCredentialProvider credentialProvider;
        //credentialProvider = new OSSAuthCredentialsProvider(Config.STSSERVER);
        credentialProvider = new OSSAuthCredentialsProvider2(HttpApiEndPoint.STS_SERVER_URL, Config.uploadObject);

        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(10 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(10 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求数，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        OSS oss = new OSSClient(DCApplication.app, Config.endpoint, credentialProvider, conf);
        return oss;
    }


    private OnUploadListener uploadListener;

    public void setOnUploadListener(OnUploadListener uploadListener) {
        this.uploadListener = uploadListener;
    }

    public interface OnUploadListener {
        void onUploadSuccess(String uploadFilePath, String bucketName, String eTag, String serverCallbackReturnBody);

        void onUploadFailure(String clientException, String serviceException);

        //Add by aistem
        void onUploadSuccessToSendMqttMsg(String bucketName, String objectKey);
    }


}
