package ai.aistem.xbot.framework.network.listener;

import ai.aistem.xbot.framework.data.bean.TrackInfo;

public interface OnAlbumRequestListener {
    void onAlbumRequestSuccess(TrackInfo trackInfo);
    void onAlbumRequestError(String error);
}
