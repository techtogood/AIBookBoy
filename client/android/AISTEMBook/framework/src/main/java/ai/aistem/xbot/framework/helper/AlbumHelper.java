package ai.aistem.xbot.framework.helper;

import com.google.gson.Gson;
import com.squareup.okhttp.Request;

import org.json.JSONException;
import org.json.JSONObject;

import ai.aistem.xbot.framework.data.bean.TrackInfo;
import ai.aistem.xbot.framework.internal.http.HttpApiEndPoint;
import ai.aistem.xbot.framework.network.AlbumRequestUtil;
import ai.aistem.xbot.framework.network.Requestparameter;
import ai.aistem.xbot.framework.network.listener.OnAlbumRequestListener;

public class AlbumHelper {

    private static AlbumHelper instance = new AlbumHelper();

    public static AlbumHelper getInstance(OnAlbumRequestListener requestListener) {
        setOnAlbumRequestListener(requestListener);
        return instance;
    }

    private static OnAlbumRequestListener albumRequestListener;
    private static void setOnAlbumRequestListener(OnAlbumRequestListener requestListener) {
        albumRequestListener = requestListener;
    }


    public void getTddeAlbum(String albumID, String token) {
        new AlbumRequestUtil() {
            @Override
            public void onAlbumResponse(String response) {

                if (response != null) {
                    try {
                        JSONObject albumJson = new JSONObject(response);
                        if (!albumJson.isNull("status")) {
                            int status = albumJson.getInt("status");
                            switch (status) {
                                case 200:
                                    String result = albumJson.getString("result");
                                    Gson gson = new Gson();
                                    TrackInfo trackInfo = gson.fromJson(result, TrackInfo.class);
                                        requestSuccess(trackInfo);
                                    break;
                                default:
                                    requestError("error code : " + status);
                                    break;
                            }
                        }
                    } catch (JSONException e) {
                        requestError("==JSONException==");
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onAlbumError(Request request, Exception e) {
                    requestError(e.toString());
            }
        }.getAlbum(HttpApiEndPoint.ENDPOINT_SOUND_ABLUM +albumID, Requestparameter.TOKEN, token);
    }


    private void requestError(String error) {
        if (albumRequestListener != null) {
            albumRequestListener.onAlbumRequestError(error);
        }
    }

    private void requestSuccess(TrackInfo trackInfo){
        if (albumRequestListener != null) {
            albumRequestListener.onAlbumRequestSuccess(trackInfo);
        }
    }

}
