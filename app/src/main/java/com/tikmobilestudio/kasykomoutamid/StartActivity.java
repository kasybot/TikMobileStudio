package com.tikmobilestudio.kasykomoutamid;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.OrientationEventListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity {
    TextView nameTv;
    ImageView closeImg;
    private Camera mCamera;
    private CameraPreview mPreview;
    private LinearLayout cameraPreview;
    private boolean cameraFront = true;
    private OrientationEventListener mOrientationEventListener;
    private int mOrientation =  -1;
    private static final int ORIENTATION_PORTRAIT_NORMAL =  1;
    private static final int ORIENTATION_PORTRAIT_INVERTED =  2;
    private static final int ORIENTATION_LANDSCAPE_NORMAL =  3;
    private static final int ORIENTATION_LANDSCAPE_INVERTED =  4;

    @SuppressLint({"MissingInflatedId", "WrongConstant"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR | ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        cameraPreview = findViewById(R.id.camera);
        nameTv = findViewById(R.id.name);
        closeImg = findViewById(R.id.close);
        String name = getIntent().getStringExtra("name");
        nameTv.setText(name);
        startCamera();
        setRotation();
    }


    private void startCamera() {
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            //  mCamera.setDisplayOrientation((int) (cameraPreview.getRotation() + 90));
            mPreview = new CameraPreview(this, mCamera,"portrait");
            cameraPreview.addView(mPreview);
            //setCameraDisplayOrientation(MainActivity.this,cameraId,mCamera);
            mCamera.startPreview();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
       // cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void setRotation(){
        if (mOrientationEventListener == null) {
            mOrientationEventListener = new OrientationEventListener(this,
                    SensorManager.SENSOR_DELAY_NORMAL) {

                @Override
                public void onOrientationChanged(int orientation) {

                    // determine our orientation based on sensor response
                    int lastOrientation = mOrientation;
                    //Toast.makeText(context, ""+screenorientation, Toast.LENGTH_SHORT).show();

                    /*if (screenorientation == Surface.ROTATION_0) {
                        if (orientation >= 315 || orientation < 45) {
                            if (mOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
                                mOrientation = ORIENTATION_LANDSCAPE_NORMAL;
                            }
                        } else if (orientation < 315 && orientation >= 225) {
                            if (mOrientation != ORIENTATION_PORTRAIT_INVERTED) {
                                mOrientation = ORIENTATION_PORTRAIT_INVERTED;
                            }
                        } else if (orientation < 225 && orientation >= 135) {
                            if (mOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
                                mOrientation = ORIENTATION_LANDSCAPE_INVERTED;
                            }
                        } else if (orientation < 135 && orientation > 45) { // orientation <135 && orientation > 45
                            if (mOrientation != ORIENTATION_PORTRAIT_NORMAL) {
                                mOrientation = ORIENTATION_PORTRAIT_NORMAL;
                            }
                        }
                    }else {
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
                    }*/

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
        switch (orientation) {
            case ORIENTATION_PORTRAIT_NORMAL:
                cameraPreview.setRotation(90);
                break;
            case ORIENTATION_PORTRAIT_INVERTED:
                cameraPreview.setRotation(270);
                break;
            case ORIENTATION_LANDSCAPE_NORMAL:
                cameraPreview.setRotation(0);
                break;
            case ORIENTATION_LANDSCAPE_INVERTED:
                cameraPreview.setRotation(180);
                break;
        }
        //  List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();


    }


}