#include <jni.h>
#include <string>
#include <android/log.h>

extern "C" {
//编码
#include "libavcodec/avcodec.h"
//封装格式处理
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
//像素处理
#include "libswscale/swscale.h"
#include <android/native_window_jni.h>
#include <unistd.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
}

#include "FFmpegMusic.h"

#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"LC",FORMAT,##__VA_ARGS__);

SLObjectItf engineObject = NULL;//用SLObjectItf声明引擎接口对象
SLEngineItf engineEngine = NULL;//声明具体的引擎对象


SLObjectItf outputMixObject = NULL;//用SLObjectItf创建混音器接口对象
SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;////具体的混音器对象实例
SLEnvironmentalReverbSettings settings = SL_I3DL2_ENVIRONMENT_PRESET_DEFAULT;//默认情况


SLObjectItf audioplayer = NULL;//用SLObjectItf声明播放器接口对象
SLPlayItf slPlayItf = NULL;//播放器接口
SLAndroidSimpleBufferQueueItf slBufferQueueItf = NULL;//缓冲区队列接口



//uri播放器
SLEnvironmentalReverbSettings reverbSettings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;
SLObjectItf uriPlayerObject = NULL;
SLPlayItf uriPlayerPlay = NULL;
SLVolumeItf uriPlayerVolume = NULL;


size_t buffersize = 0;
void *buffer;
bool bPlayed = false;

int file_duration;

//将pcm数据添加到缓冲区中
void getQueueCallBack(SLAndroidSimpleBufferQueueItf slBufferQueueItf, void *context) {

    buffersize = 0;

    getPcm(&buffer, &buffersize);
    if (buffer != NULL && buffersize != 0) {
        //将得到的数据加入到队列中
        (*slBufferQueueItf)->Enqueue(slBufferQueueItf, buffer, buffersize);
    }
}

//创建引擎
void createEngine() {
    slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);//创建引擎
    (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);//实现engineObject接口对象
    (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE,
                                  &engineEngine);//通过引擎调用接口初始化SLEngineItf
}

//创建混音器
void createMixVolume() {
    (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 0, 0, 0);//用引擎对象创建混音器接口对象
    (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);//实现混音器接口对象
    SLresult sLresult = (*outputMixObject)->GetInterface(outputMixObject,
                                                         SL_IID_ENVIRONMENTALREVERB,
                                                         &outputMixEnvironmentalReverb);//利用混音器实例对象接口初始化具体的混音器对象
    //设置
    if (SL_RESULT_SUCCESS == sLresult) {
        (*outputMixEnvironmentalReverb)->
                SetEnvironmentalReverbProperties(outputMixEnvironmentalReverb, &settings);
    }
}

//创建播放器
void createPlayer(const char *file) {
    //初始化ffmpeg
    int rate;
    int channels;
    createFFmpeg(&rate, &channels, &file_duration, file);
    LOGE("RATE %d", rate);
    LOGE("channels %d", channels);
    LOGE("file_duration %d", file_duration);

    channels = 2;  //一直用立体声解码
    /*
     * typedef struct SLDataLocator_AndroidBufferQueue_ {
    SLuint32    locatorType;//缓冲区队列类型
    SLuint32    numBuffers;//buffer位数
} */

    SLDataLocator_AndroidBufferQueue android_queue = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    /**
    typedef struct SLDataFormat_PCM_ {
        SLuint32 		formatType;  pcm
        SLuint32 		numChannels;  通道数
        SLuint32 		samplesPerSec;  采样率
        SLuint32 		bitsPerSample;  采样位数
        SLuint32 		containerSize;  包含位数
        SLuint32 		channelMask;     立体声
        SLuint32		endianness;    end标志位
    } SLDataFormat_PCM;
     */
    SLDataFormat_PCM pcm = {SL_DATAFORMAT_PCM, (unsigned int) channels, (unsigned int) rate * 1000,
                            SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
                            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
                            SL_BYTEORDER_LITTLEENDIAN};

    /*
     * typedef struct SLDataSource_ {
	        void *pLocator;//缓冲区队列
	        void *pFormat;//数据样式,配置信息
        } SLDataSource;
     * */
    SLDataSource dataSource = {&android_queue, &pcm};


    SLDataLocator_OutputMix slDataLocator_outputMix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};


    SLDataSink slDataSink = {&slDataLocator_outputMix, NULL};


    const SLInterfaceID ids[3] = {SL_IID_BUFFERQUEUE, SL_IID_EFFECTSEND, SL_IID_VOLUME};
    const SLboolean req[3] = {SL_BOOLEAN_FALSE, SL_BOOLEAN_FALSE, SL_BOOLEAN_FALSE};

    /*
     * SLresult (*CreateAudioPlayer) (
		SLEngineItf self,
		SLObjectItf * pPlayer,
		SLDataSource *pAudioSrc,//数据设置
		SLDataSink *pAudioSnk,//关联混音器
		SLuint32 numInterfaces,
		const SLInterfaceID * pInterfaceIds,
		const SLboolean * pInterfaceRequired
	);
     * */

    (*engineEngine)->CreateAudioPlayer(engineEngine, &audioplayer, &dataSource, &slDataSink, 3, ids,
                                       req);
    (*audioplayer)->Realize(audioplayer, SL_BOOLEAN_FALSE);

    (*audioplayer)->GetInterface(audioplayer, SL_IID_PLAY, &slPlayItf);//初始化播放器
    //注册缓冲区,通过缓冲区里面 的数据进行播放
    (*audioplayer)->GetInterface(audioplayer, SL_IID_BUFFERQUEUE, &slBufferQueueItf);
    //设置回调接口
    (*slBufferQueueItf)->RegisterCallback(slBufferQueueItf, getQueueCallBack, NULL);
}


void setPlayState(SLPlayItf slPlayItf, SLuint32 state) {
    if (slPlayItf != NULL)
        (*slPlayItf)->SetPlayState(slPlayItf, state);
}

//释放资源
void realseResource() {

    if (audioplayer != NULL) {
        (*audioplayer)->Destroy(audioplayer);
        audioplayer = NULL;
        slBufferQueueItf = NULL;
        slPlayItf = NULL;
    }


    if (outputMixObject != NULL) {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = NULL;
        outputMixEnvironmentalReverb = NULL;
    }

    if (engineObject != NULL) {
        (*engineObject)->Destroy(engineObject);
        engineObject = NULL;
        engineEngine = NULL;
    }

    // destroy URI audio player object, and invalidate all associated interfaces
    /*if (uriPlayerObject != NULL) {
        (*uriPlayerObject)->Destroy(uriPlayerObject);
        uriPlayerObject = NULL;
        uriPlayerPlay = NULL;
        uriPlayerVolume = NULL;

    }*/

    realseFFmpeg();
}

extern "C"
JNIEXPORT void JNICALL
Java_ai_aistem_xbot_framework_internal_utils_AudioPlayer_playLocal(JNIEnv *env, jobject instance,
                                                                     jstring file) {

    const char *fileDir;
    fileDir = env->GetStringUTFChars(file, 0);
    if (fileDir == NULL || access(fileDir, 0) != 0) {
        LOGE("playLocal file not exist");
        return;
    }
    bPlayed = true;

    realseResource();

    createEngine();
    createMixVolume();
    createPlayer(fileDir);
    //开始播放
    getQueueCallBack(slBufferQueueItf, NULL);
    //播放
    setPlayState(slPlayItf, SL_PLAYSTATE_PLAYING);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ai_aistem_xbot_framework_internal_utils_AudioPlayer_playLocalDuration(JNIEnv *env,
                                                                             jobject instance,
                                                                             jstring file) {

    const char *fileDir;
    fileDir = env->GetStringUTFChars(file, 0);
    if (fileDir == NULL || access(fileDir, 0) != 0) {
        LOGE("playLocalDuration file not exist");
        return 0;
    }
    bPlayed = true;
    realseResource();

    createEngine();
    createMixVolume();
    createPlayer(fileDir);
    //开始播放
    getQueueCallBack(slBufferQueueItf, NULL);
    //播放
    setPlayState(slPlayItf, SL_PLAYSTATE_PLAYING);
    return file_duration;
}


extern "C"
JNIEXPORT void JNICALL
Java_ai_aistem_xbot_framework_internal_utils_AudioPlayer_resume(JNIEnv *env, jobject instance) {
    //播放
    setPlayState(slPlayItf, SL_PLAYSTATE_PLAYING);
}

extern "C"
JNIEXPORT void JNICALL
Java_ai_aistem_xbot_framework_internal_utils_AudioPlayer_pause(JNIEnv *env, jobject instance) {
    setPlayState(slPlayItf, SL_PLAYSTATE_PAUSED);
}


extern "C"
JNIEXPORT void JNICALL
Java_ai_aistem_xbot_framework_internal_utils_AudioPlayer_exit(JNIEnv *env, jobject instance) {
    realseResource();
}

extern "C"
JNIEXPORT jint JNICALL
Java_ai_aistem_xbot_framework_internal_utils_AudioPlayer_getPlayerState(JNIEnv *env) {

    unsigned int position = 0;
    if (bPlayed) {
        (*slPlayItf)->GetPosition(slPlayItf, &position);
    }
    LOGE("position %d", position);
    return position;
}


extern "C"
JNIEXPORT void JNICALL
Java_ai_aistem_xbot_framework_internal_utils_AudioPlayer_playUri(JNIEnv *env, jobject instance,
                                                                   jstring uri) {

    SLresult result;

    realseResource();

    // convert Java string to UTF-8

    const char *utf8 = env->GetStringUTFChars(uri, NULL);


    LOGE("playUri 111=%s\n", utf8);

    //第一步，创建引擎

    createEngine();



    //第二步，创建混音器

    const SLInterfaceID mids[1] = {SL_IID_ENVIRONMENTALREVERB};

    const SLboolean mreq[1] = {SL_BOOLEAN_FALSE};

    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, mids, mreq);

    (void) result;

    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);

    (void) result;

    result = (*outputMixObject)->GetInterface(outputMixObject, SL_IID_ENVIRONMENTALREVERB,
                                              &outputMixEnvironmentalReverb);

    if (SL_RESULT_SUCCESS == result) {

        result = (*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(

                outputMixEnvironmentalReverb, &reverbSettings);

        (void) result;

    }

    LOGE("playUri 222\n");


    //第三步，设置播放器参数和创建播放器

    // configure audio source

    // (requires the INTERNET permission depending on the uri parameter)

    SLDataLocator_URI loc_uri = {SL_DATALOCATOR_URI, (SLchar *) utf8};

    SLDataFormat_MIME format_mime = {SL_DATAFORMAT_MIME, NULL, SL_CONTAINERTYPE_UNSPECIFIED};

    SLDataSource audioSrc = {&loc_uri, &format_mime};


    LOGE("playUri 333\n");

    // configure audio sink

    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};

    SLDataSink audioSnk = {&loc_outmix, NULL};

    LOGE("playUri 444\n");


    // create audio player

    const SLInterfaceID ids[3] = {SL_IID_SEEK, SL_IID_MUTESOLO, SL_IID_VOLUME};

    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};

    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &uriPlayerObject, &audioSrc,
                                                &audioSnk, 3, ids, req);

    LOGE("playUri 555\n");


    (void) result;



    // release the Java string and UTF-8

    env->ReleaseStringUTFChars(uri, utf8);



    // realize the player

    result = (*uriPlayerObject)->Realize(uriPlayerObject, SL_BOOLEAN_FALSE);

    // this will always succeed on Android, but we check result for portability to other platforms

    if (SL_RESULT_SUCCESS != result) {

        (*uriPlayerObject)->Destroy(uriPlayerObject);

        uriPlayerObject = NULL;

        return;

    }


    LOGE("playUri 666\n");

    // get the play interface

    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_PLAY, &uriPlayerPlay);

    (void) result;


    LOGE("playUri 777\n");



    // get the volume interface

    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_VOLUME, &uriPlayerVolume);

    (void) result;

    LOGE("playUri 888\n");


    if (NULL != uriPlayerPlay) {

        // set the player's state
        LOGE("playUri 999\n");

        result = (*uriPlayerPlay)->SetPlayState(uriPlayerPlay, SL_PLAYSTATE_PLAYING);

        //(void)result;

    }


    LOGE("playUri 10109\n");

    //设置播放音量 （100 * -50：静音 ）

//    (*uriPlayerVolume)->SetVolumeLevel(uriPlayerVolume, 0 * -50);

}

