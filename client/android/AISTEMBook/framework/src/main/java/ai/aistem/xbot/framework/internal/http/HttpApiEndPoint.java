package ai.aistem.xbot.framework.internal.http;


import ai.aistem.xbot.framework.application.GlobalParameter;

public final class HttpApiEndPoint {

    public static final String BASE_URL = GlobalParameter.API_PROTOCOL;


    public static final String ENDPOINT_AUTH_TOKEN = BASE_URL +
            "/robot/auth/token";

    public static final String ENDPOINT_AUTH_REFRESH_TOKEN = BASE_URL +
            "/robot/auth/refresh_token";

    public static final String ENDPOINT_BASE_NIGHT = BASE_URL +
            "/robot/base/night";

    public static final String ENDPOINT_BASE_ANTI_ADDICTION = BASE_URL +
            "/robot/base/anti_addiction";

    public static final String ENDPOINT_BASE_INFO = BASE_URL +
            "/robot/base/info";


    public static final String ENDPOINT_ACHIEVEMENT_STATISTICS = BASE_URL +
            "/robot/achievement/statistics";

    public static final String ENDPOINT_BASE_USERS = BASE_URL +
            "/robot/base/users";

    public static final String ENDPOINT_USER = BASE_URL +
            "/robot/user/";

    public static final String ENDPOINT_SOUND_VOICE = BASE_URL +
            "/robot/sound/voice/";

    public static final String ENDPOINT_SOUND_VOICES_PUT = BASE_URL +
            "/robot/sound/voices";

    public static final String ENDPOINT_SOUND_ALBUM = BASE_URL +
            "/robot/sound/album/";

    public static final String ENDPOINT_BOOK_ALBUM = BASE_URL +
            "/robot/book/album/";

    public static final String ENDPOINT_BOOK_VOLUME = BASE_URL +
            "/robot/book/volume/";

    public static final String ENDPOINT_BOOK_SCAN_VOLUME = BASE_URL +
            "/robot/book/scan/volume/";

    public static final String STS_SERVER_URL = BASE_URL +
            "/robot/common/upload/talk";

    public static final String STS_SERVER_URL2 = BASE_URL +
            "/robot/common/upload/base";

    public static final String PRIVATE_STS_SERVER_URL = BASE_URL +
            "/robot/common/upload/private";

    public static final String LOCAL_STS_SERVER_URL = "http://192.168.51.227:7080";

    public static final String ENDPOINT_SOUND_CATEGORY = BASE_URL +
            "/robot/sound/category/";

    public static final String ENDPOINT_SOUND_ABLUM = BASE_URL +
            "/robot/sound/album/";

    public static final String ENDPOINT_SOUND_TAG = BASE_URL +
            "/robot/tags?module=sound_voice&page=1&number=50";

    public static final String ENDPOINT_SOUND_VOICES = BASE_URL +
            "/robot/sound/voices?page=1&number=50&tags=";

    public static final String BASE_LOCAL_URL = "http://192.168.51.112";

    public static final String ENDPOINT_ACHIEVEMENT_TEMPLATES = BASE_URL +
            "/robot/achievement/templates";

    public static final String ENDPOINT_ACHIEVEMENT_RECORDS = BASE_URL +
            "/robot/achievement/records";

    public static final String ENDPOINT_APP_STORES = BASE_URL +
            "/robot/app/store";

    /*public static final String ENDPOINT_ACHIEVEMENT_STATISTICS = BASE_URL +
            "/robot/achievement/statistics";*/
//    public static final String BOOK_SEARCH_URL = GlobalParameter.API_PROTOCOL +
//            GlobalParameter.BOOK_URL+"/search_book";

    public static final String BOOK_SEARCH_URL = "http://xxx.xxx.xxx.xxx/search_book";
//    public static final String BOOK_SEARCH_URL = "http://192.168.51.228/search_book"; //internal server
    private HttpApiEndPoint() {
        // This class is not publicly instantiable
    }
}
