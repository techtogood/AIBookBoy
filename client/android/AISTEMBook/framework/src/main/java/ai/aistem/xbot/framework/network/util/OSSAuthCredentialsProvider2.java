package ai.aistem.xbot.framework.network.util;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ai.aistem.xbot.framework.application.DCApplication;


/**
 * 重写 OSSAuthCredentialsProvider 适配HTTP POST 方法
 */
public class OSSAuthCredentialsProvider2 extends OSSFederationCredentialProvider {

    private String mAuthServerUrl;
    private OSSAuthCredentialsProvider.AuthDecoder mDecoder;

    private String mPath;

    public OSSAuthCredentialsProvider2(String url, String path) {
        this.mAuthServerUrl = url;
        this.mPath = path;
    }

    /**
     * set auth server url
     * @param authServerUrl
     */
    public void setAuthServerUrl(String authServerUrl) {
        this.mAuthServerUrl = authServerUrl;
    }

    /**
     * set response data decoder
     * @param decoder
     */
    public void setDecoder(OSSAuthCredentialsProvider.AuthDecoder decoder) {
        this.mDecoder = decoder;
    }

    /**
     * 设置上传路径
     * @param path
     */
    public void setPath(String path) {
        this.mPath = path;
    }

    @Override
    public OSSFederationToken getFederationToken() throws ClientException {
        OSSFederationToken authToken;
        String authData;
        try {
            String urlParameters  = "path="+mPath;
            byte[] postData = urlParameters.getBytes();
            int postDataLength = postData.length;

            URL stsUrl = new URL(mAuthServerUrl);
            HttpURLConnection conn = (HttpURLConnection) stsUrl.openConnection();
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("accept","application/json");
            conn.setRequestProperty("token", DCApplication.app.getDataManager().getRobotAuthToken());
            conn.setDoOutput(true);
            conn.setDoInput(true);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.write(postData);
            wr.flush();
            wr.close();

            conn.connect();

            InputStream input = conn.getInputStream();
            authData = IOUtils.readStreamAsString(input, OSSConstants.DEFAULT_CHARSET_NAME);
            if (mDecoder != null) {
                authData = mDecoder.decode(authData);
            }

            JSONObject jsonObj = new JSONObject(authData);
            int statusCode = jsonObj.getInt("status");
            JSONObject resultJsonObj = jsonObj.getJSONObject("result");
            if (statusCode == 200) {
                String ak = resultJsonObj.getString("AccessKeyId");
                String sk = resultJsonObj.getString("AccessKeySecret");
                String token = resultJsonObj.getString("SecurityToken");
                String expiration = resultJsonObj.getString("Expiration");
                authToken = new OSSFederationToken(ak, sk, token, expiration);
            } else {
                String errorCode = jsonObj.getString("ErrorCode");
                String errorMessage = jsonObj.getString("ErrorMessage");
                throw new ClientException("ErrorCode: " + errorCode + "| ErrorMessage: " + errorMessage);
            }
            return authToken;
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    public interface AuthDecoder {
        String decode(String data);
    }
}
