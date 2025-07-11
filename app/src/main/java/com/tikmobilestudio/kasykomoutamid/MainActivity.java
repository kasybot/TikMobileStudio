package com.tikmobilestudio.kasykomoutamid;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 99107;
    Button start;
    EditText name;
    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"
            , "android.permission.WRITE_EXTERNAL_STORAGE"
    };
    SharedPreferences sharedPreferences;
    private int flag;

    private Camera mCamera;
    private CameraPreview mPreview;
    private LinearLayout cameraPreview;
    private boolean cameraFront = true;

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR | ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        name = findViewById(R.id.name);
        start = findViewById(R.id.btnStart);
        cameraPreview = findViewById(R.id.camera);
        // cameraKitView.setFacing(CameraKit.FACING_FRONT);
        sharedPreferences = new SharedPreferences(this);

        // cameraPreview.setOrientation(1);
        // cameraPreview.setTranslationX((w - h) / 2);
        // cameraPreview.setTranslationY((h - w) / 2);

        /*Dexter.withContext(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                })
                .check();
*/
        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        start.setOnClickListener(v -> {
            if (name.getText().toString().isEmpty()) {
                name.setError("Please Add Your Name");
            } else {
                if (checkCameraHardware(this)) {
                    if (!isServiceRunningInForeground(this, ForegroundService.class)) {
                        sharedPreferences.saveName(name.getText().toString());
                    } else {
                        Toast.makeText(this, "Service is already running, Please Close that First", Toast.LENGTH_SHORT).show();
                    }
                    startService();
                } else
                    Toast.makeText(MainActivity.this, "No Camera Detected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startCamera() {

        try {

            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            //  mCamera.setDisplayOrientation((int) (cameraPreview.getRotation() + 90));
            mPreview = new CameraPreview(this, mCamera, "portrait");
            cameraPreview.addView(mPreview);
            //setCameraDisplayOrientation(MainActivity.this,cameraId,mCamera);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    public void startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // check if the user has already granted
            // the Draw over other apps permission
            if (Settings.canDrawOverlays(this)) {
                // start the service based on the android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent i = new Intent(this, ForegroundService.class);
                    startForegroundService(i);
                } else {
                    Intent i = new Intent(this, ForegroundService.class);
                    startService(i);
                }
            } else {
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(myIntent);
            }
        } else {
            Intent i = new Intent(this, ForegroundService.class);
            startService(i);
        }
    }

    public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }

            }
        }
        return false;
    }

    // method to ask user to grant the Overlay permission
    public void checkOverlayPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // send user to the device settings
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(myIntent);
            }
        }
    }

    // check for permission again when user grants it from
    // the device settings, and start the service
    public void onResume() {

        super.onResume();
      /*  if(mCamera == null) {
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
            mPreview.refreshCamera(mCamera);
        }else {

        }*/

    }

    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        //releaseCamera();
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /** A safe way to get an instance of the Camera object. */
    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }


    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

}