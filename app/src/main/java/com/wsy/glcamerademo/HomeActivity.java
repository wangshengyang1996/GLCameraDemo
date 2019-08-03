package com.wsy.glcamerademo;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.core.app.ActivityCompat;

public class HomeActivity extends BaseActivity {
    private static final int REQUEST_CODE_USING_CAMERA = 1;
    private static final int REQUEST_CODE_USING_CAMERA2 = 2;
    private static final int REQUEST_CODE_USING_CAMERA_AND_OPENGL = 3;
    private static final String[] CAMERA_PERMISSION = new String[]{
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void usingCamera(View view) {
        if (checkPermissions(CAMERA_PERMISSION)) {
            startActivity(new Intent(this, CameraActivity.class));
        } else {
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, REQUEST_CODE_USING_CAMERA);
        }
    }
    public void usingCameraWithShelter(View view) {
        if (checkPermissions(CAMERA_PERMISSION)) {
            startActivity(new Intent(this, CoverByParentCameraActivity.class));
        } else {
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, REQUEST_CODE_USING_CAMERA);
        }
    }

    public void usingCamera2(View view) {
        if (checkPermissions(CAMERA_PERMISSION)) {
            startActivity(new Intent(this, Camera2Activity.class));
        } else {
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, REQUEST_CODE_USING_CAMERA2);
        }
    }

    public void usingCameraAndOpenGL(View view) {
        if (checkPermissions(CAMERA_PERMISSION)) {
            startActivity(new Intent(this, GLCameraActivity.class));
        } else {
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, REQUEST_CODE_USING_CAMERA_AND_OPENGL);
        }
    }

    @Override
    protected void onRequestPermissionResult(int requestCode, boolean isAllGranted) {
        super.onRequestPermissionResult(requestCode, isAllGranted);
        if (isAllGranted) {
            switch (requestCode) {
                case REQUEST_CODE_USING_CAMERA:
                    usingCamera(null);
                    break;
                case REQUEST_CODE_USING_CAMERA2:
                    usingCamera2(null);
                    break;
                case REQUEST_CODE_USING_CAMERA_AND_OPENGL:
                    usingCameraAndOpenGL(null);
                    break;
                default:
                    break;
            }
        }
    }
}
