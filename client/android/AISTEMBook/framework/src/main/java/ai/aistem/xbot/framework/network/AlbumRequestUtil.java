package ai.aistem.xbot.framework.network;

import com.squareup.okhttp.Request;

import ai.aistem.xbot.framework.internal.http.OkHttpClientManager;

public abstract class AlbumRequestUtil {



    public   void getAlbum(String url,String tokenName,String token){
        OkHttpClientManager.getAsyn(url, tokenName, token, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                onAlbumError(request,e);
            }

            @Override
            public void onResponse(String response) {
                onAlbumResponse(response);
            }
        });
    }


    public abstract void onAlbumResponse(String response);


    public abstract void onAlbumError(Request request, Exception e);

}
