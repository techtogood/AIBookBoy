package ai.aistem.xbot.framework.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;
import java.util.Arrays;

import ai.aistem.xbot.framework.application.GlobalParameter;

public class RobotCameraService extends Service {

    protected static final String TAG = "RobotCameraService";
    private int imageGetFlg = 0;
    private int subImageGetFlg = 0;

    protected static final int CAMERACHOICE = CameraCharacteristics.LENS_FACING_FRONT;

    protected CameraDevice mCameraDevice = null;
    protected CameraCaptureSession mCaptureSession = null;
    protected ImageReader mImageReader = null;
    private long cur_time = 0; // in ms


    protected CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onOpened(@NonNull CameraDevice camera) {

            Log.i(TAG, "CameraDevice.StateCallback onOpened");

            mCameraDevice = camera;

            ActOnReadyCameraDevice();

        }


        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

            Log.w(TAG, "CameraDevice.StateCallback onDisconnected");

        }


        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

            Log.e(TAG, "CameraDevice.StateCallback onError " + error);

        }

    };


    protected CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {

            Log.i(TAG, "CameraCaptureSession.StateCallback onConfigured");

            RobotCameraService.this.mCaptureSession = session;

            try {
                session.setRepeatingRequest(createCaptureRequest(), null, null);

            } catch (CameraAccessException e) {

                Log.e(TAG, e.getMessage());

            }

        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
        }

    };



    /*
     *  返回一个Binder对象
     */
    @Override
    public IBinder onBind(Intent intent) {
        GlobalParameter.CameraServiceHandler = new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case GlobalParameter.Camera_Cmd_Open:
                        cur_time = 0;
                        OpenCameraCollection();//打开相机
                        break;
                    case GlobalParameter.Camera_Cmd_Release:
                        ReleaseCamera();
                        break;
                    default:
                        break;

                }
                super.handleMessage(msg);
            }

        };


        return new MsgBinder();
    }

    public class MsgBinder extends Binder {
        public RobotCameraService getService() {
            return RobotCameraService.this;
        }
    }


    protected ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onImageAvailable(ImageReader reader) {

            if( GlobalParameter.CameradDeviceStatus == 1 ){
                GlobalParameter.CameradDeviceStatus = 2;
            }

            if (GlobalParameter.CameraStatus == GlobalParameter.Camera_Lattering) {
                Image image = reader.acquireNextImage();
                if (GlobalParameter.CharRecogntionFlg) {
                    GlobalParameter.CharRecogntionFlg = false;
                    GlobalParameter.curImage = imageToMat(image);

                    synchronized (GlobalParameter.CharShare) {
                        GlobalParameter.CharShare.notify();
                    }
                    image.close();
                } else {
                    //Log.d(TAG, "ignore image");
                    image.close();
                }
            } else if (GlobalParameter.CameraStatus == GlobalParameter.Camera_Reading) {
                Image image = reader.acquireNextImage();
                //long time =  System.currentTimeMillis();
                if(image != null/* && (cur_time == 0 || time - cur_time > 100 )*/){
                    //cur_time = time;
                    if( GlobalParameter.ImagePreThreadFlag ) {
                        //Log.d("PictureBookAPP", "sent image");
                        GlobalParameter.ImagePreThreadFlag = false;
                        GlobalParameter.curImage = imageToMat(image);
                        org.opencv.core.Core.rotate(GlobalParameter.curImage,GlobalParameter.curImage, Core.ROTATE_90_CLOCKWISE);
                        Core.flip(GlobalParameter.curImage,GlobalParameter.curImage,1);
                        synchronized (GlobalParameter.imagePreShare) {
                            GlobalParameter.imagePreShare.notify();
                        }
                    }
//                    }else if( GlobalParameter.ImagePreThreadFlag_2 ){
//                        GlobalParameter.ImagePreThreadFlag_2 = false;
//                        GlobalParameter.curImage_2 = imageToMat(image);
//                        synchronized (GlobalParameter.imagePreShare_2) {
//                            GlobalParameter.imagePreShare_2.notify();
//                        }
//                    }
                }
                image.close();

            }
            else{
                Image image = reader.acquireNextImage();
                image.close();
            }
        }

    };


    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void OpenCameraCollection() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);

        try {
            Log.i(TAG, "OpenCameraCollection ===");
            String pickedCamera = getCamera(manager);

            Log.i(TAG, "pickedCamera =" + pickedCamera+ "status="+GlobalParameter.CameraStatus);

            manager.openCamera(pickedCamera, cameraStateCallback, null);

            if (GlobalParameter.CameraStatus == GlobalParameter.Camera_Reading) {
                mImageReader = ImageReader.newInstance(640,
                        480, ImageFormat.YUV_420_888, 5 /* images buffered */);

            } else {
                mImageReader = ImageReader.newInstance(640,
                        480, ImageFormat.YUV_420_888, 1 /* images buffered */);
            }

            mImageReader.setOnImageAvailableListener(onImageAvailableListener, null);

            Log.i(TAG, "mImageReader created");

        } catch (CameraAccessException e) {

            Log.e(TAG, e.getMessage());

        }

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void ReleaseCamera() {
        Log.d(TAG, "ENTER ReleaseCamera");

        if (null != mCaptureSession) {
            try {

                mCaptureSession.abortCaptures();

            } catch (CameraAccessException e) {

                Log.e(TAG, e.getMessage());

            }

            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (null != mImageReader) {
            mImageReader.close();
            mImageReader = null;
        }

    }


    /**
     * Return the Camera Id which matches the field CAMERACHOICE.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public String getCamera(CameraManager manager) {
        try {

            for (String cameraId : manager.getCameraIdList()) {

                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (cOrientation == CAMERACHOICE) {

                    return cameraId;

                }
            }

        } catch (CameraAccessException e) {

            e.printStackTrace();

        }

        return null;

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "onStartCommand flags " + flags + " startId " + startId);
       /* CharRecognitionInit();
        GlobalParameter.CameraServiceHandler = new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case GlobalParameter.Camera_StarReading:
                        OpenCameraCollection();
                        break;
                    case GlobalParameter.Camera_Release:
                        ReleaseCamera();
                        break;
                    default:
                        break;

                }
                super.handleMessage(msg);
            }

        };*/

//        new MyThread().start();

        return super.onStartCommand(intent, flags, startId);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void ActOnReadyCameraDevice() {

        try {

            mCameraDevice.createCaptureSession(Arrays.asList(mImageReader.getSurface()), sessionStateCallback, null);

        } catch (CameraAccessException e) {

            Log.e(TAG, e.getMessage());

        }

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDestroy() {

        try {

            mCaptureSession.abortCaptures();

        } catch (CameraAccessException e) {

            Log.e(TAG, e.getMessage());

        }

        if (null != mCaptureSession) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (null != mImageReader) {
            mImageReader.close();
            mImageReader = null;
        }
//TODO   释放掉所有的资源
    }


    /**
     * Takes an Android Image in the YUV_420_888 format and returns an OpenCV Mat.
     *
     * @param image Image in the YUV_420_888 format.
     * @return OpenCV Mat.
     */

    private Mat imageToMat(Image image) {

        ByteBuffer buffer;

        int rowStride;

        int pixelStride;

        int width = image.getWidth();

        int height = image.getHeight();

        int offset = 0;


        Image.Plane[] planes = image.getPlanes();

        byte[] data = new byte[image.getWidth() * image.getHeight() * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];

        byte[] rowData = new byte[planes[0].getRowStride()];

        for (int i = 0; i < 1/*planes.length*/; i++) {

            buffer = planes[i].getBuffer();

            rowStride = planes[i].getRowStride();

            pixelStride = planes[i].getPixelStride();

            int w = (i == 0) ? width : width / 2;

            int h = (i == 0) ? height : height / 2;
            for (int row = 0; row < h; row++) {

                int bytesPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8;

                if (pixelStride == bytesPerPixel) {

                    int length = w * bytesPerPixel;

                    buffer.get(data, offset, length);
//                    Log.d(TAG,"buffer.get(rgbData, offset, length);");

                    // Advance buffer the remainder of the row stride, unless on the last row.
                    // Otherwise, this will throw an IllegalArgumentException because the buffer
                    // doesn't include the last padding.

                    if (h - row != 1) {

                        buffer.position(buffer.position() + rowStride - length);

                    }

                    offset += length;

                } else {

                    // On the last row only read the width of the image minus the pixel stride

                    // plus one. Otherwise, this will throw a BufferUnderflowException because the

                    // buffer doesn't include the last padding.

                    if (h - row == 1) {

                        buffer.get(rowData, 0, width - pixelStride + 1);

                    } else {

                        buffer.get(rowData, 0, rowStride);

                    }


                    for (int col = 0; col < w; col++) {

                        data[offset++] = rowData[col * pixelStride];

                    }

                }

            }

        }

        // Finally, create the Mat.

        Mat mat = new Mat(height, width, CvType.CV_8UC1);

        mat.put(0, 0, data);

        return mat;

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected CaptureRequest createCaptureRequest() {

        try {
            Log.d(TAG, "createCaptureRequest");
            CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            builder.addTarget(mImageReader.getSurface());

            return builder.build();

        } catch (CameraAccessException e) {

            Log.e(TAG, e.getMessage());

            return null;

        }

    }

    public Bitmap getSrcImage() {
        /* to do ...?*/
        return null;
    }

    public Bitmap getSubImage() {
        /* to do ...?*/
        return null;
    }


}