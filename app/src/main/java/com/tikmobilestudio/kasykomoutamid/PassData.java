package com.tikmobilestudio.kasykomoutamid;

import android.app.Activity;
import android.os.Parcelable;
import android.view.TextureView;


public interface PassData extends Parcelable {
    void data(TextureView cameraKitView);
    void stopservice(Activity context);
}
