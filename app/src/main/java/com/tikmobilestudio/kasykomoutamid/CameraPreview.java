package com.tikmobilestudio.kasykomoutamid;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Context context;
    private OrientationEventListener mOrientationEventListener;
    private int mOrientation =  -1;

    private int width = 0;
    private int height = 0;


    private static final int ORIENTATION_PORTRAIT_NORMAL =  1;
    private static final int ORIENTATION_PORTRAIT_INVERTED =  2;
    private static final int ORIENTATION_LANDSCAPE_NORMAL =  3;
    private static final int ORIENTATION_LANDSCAPE_INVERTED =  4;
    private String rotation = "portrait";

    public CameraPreview(Context context, Camera camera,String rotation) {
        super(context);
        this.context = context;
        this.rotation = rotation;
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // create the surface and start camera preview
            if (mCamera == null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            Log.d(VIEW_LOG_TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        this.width = w;
        this.height = h;
        refreshCamera(mCamera);
    }


    public void refreshCamera(Camera camera) {
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        setCamera(camera);
        try {

           // final Camera.Parameters params = mCamera.getParameters();
            // viewParams is from the view where the preview is displayed
            final Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size optimalSize = getOptimalPreviewSize(mCamera);
            parameters.setPreviewSize(optimalSize.width, optimalSize.height);
            mCamera.setParameters(parameters);

           // mCamera.setDisplayOrientation(CameraUtil.getCameraOrientation(context));
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

            setRotation();

        } catch (Exception e) {
            Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }



    public Camera.Size getOptimalPreviewSize(Camera camera) {
        if (camera == null) {
            return null;
        }

        List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
        double targetRatio = 0;
        if (rotation.equals("portrait")) {

            Log.d("p_width",""+width);
            Log.d("p_height",""+height);
            targetRatio = (double) height / width;

            Log.d("p_target",""+targetRatio);
        }
        else if (rotation.equals("landscape")){

            targetRatio = (double) width / height;
            Log.d("l_width",""+width);
            Log.d("l_height",""+height);

            Log.d("l_target",""+targetRatio);
        }

        final double ASPECT_TOLERANCE = 0.1;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }


    private void setRotation(){
        if (mOrientationEventListener == null) {
            mOrientationEventListener = new OrientationEventListener(context,
                    SensorManager.SENSOR_DELAY_NORMAL) {

                @Override
                public void onOrientationChanged(int orientation) {

                    // determine our orientation based on sensor response
                    int lastOrientation = mOrientation;

                    if (orientation >= 315 || orientation < 45) {
                        if (mOrientation != ORIENTATION_PORTRAIT_NORMAL) {
                            mOrientation = ORIENTATION_PORTRAIT_NORMAL;
                        }
                    } else if (orientation < 315 && orientation >= 225) {
                        if (mOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
                            mOrientation = ORIENTATION_LANDSCAPE_NORMAL;
                        }
                    } else if (orientation < 225 && orientation >= 135) {
                        if (mOrientation != ORIENTATION_PORTRAIT_INVERTED) {
                            mOrientation = ORIENTATION_PORTRAIT_INVERTED;
                        }
                    } else if (orientation < 135 && orientation > 45) { // orientation <135 && orientation > 45
                        if (mOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
                            mOrientation = ORIENTATION_LANDSCAPE_INVERTED;
                        }
                    }

                    if (lastOrientation != mOrientation) {
                        changeRotation(mOrientation);
                    }
                }
            };
        }
        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }
    }

    private void changeRotation(int orientation) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);

        int degrees = 0;
        switch (orientation) {
            case ORIENTATION_PORTRAIT_NORMAL: degrees = 0;break;
            case ORIENTATION_PORTRAIT_INVERTED: degrees = 90;break;
            case ORIENTATION_LANDSCAPE_NORMAL: degrees = 90;break;
            case ORIENTATION_LANDSCAPE_INVERTED: degrees =270;break;
        }
        int result;
        result = (info.orientation + degrees) % 360;
        result = (360 - result) % 360;

        try{
            mCamera.setDisplayOrientation(result);
        }catch (Exception e){
            Log.e("rotation",""+e.getMessage());
        }
    }

    public void setCamera(Camera camera) {
        //method to set a camera instance
        mCamera = camera;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        // mCamera.release();

    }
}
