package com.wsy.glcamerademo;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.wsy.glcamerademo.camera.CameraHelper;
import com.wsy.glcamerademo.camera.CameraListener;
import com.wsy.glcamerademo.widget.RoundBorderView;
import com.wsy.glcamerademo.widget.RoundFrameLayout;
import com.wsy.glcamerademo.widget.RoundTextureView;

public class CoverByParentCameraActivity extends BaseActivity implements ViewTreeObserver.OnGlobalLayoutListener, CameraListener, SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "CameraActivity";
    private static final int ACTION_REQUEST_PERMISSIONS = 1;
    private CameraHelper cameraHelper;
    private RoundTextureView textureView;
    private RoundBorderView roundBorderView;
    private RoundFrameLayout insertFrameLayout;
    //用于调整圆角大小
    private SeekBar radiusSeekBar;
    //默认打开的CAMERA
    private static final int CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_BACK;
    //需要的权限
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        initView();
    }

    private void initView() {
        textureView = findViewById(R.id.texture_preview);
        textureView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }


    void initCamera() {
        cameraHelper = new CameraHelper.Builder()
                .cameraListener(this)
                .specificCameraId(CAMERA_ID)
                .previewOn(textureView)
//                .previewSize(new Point(1920, 1080))
                .previewViewSize(new Point(textureView.getLayoutParams().width, textureView.getLayoutParams().height))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .build();
        cameraHelper.start();
    }

    @Override
    protected void onRequestPermissionResult(int requestCode, boolean isAllGranted) {
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            if (isAllGranted) {
                initCamera();
            } else {
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onGlobalLayout() {
        textureView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        ViewGroup.LayoutParams layoutParams = textureView.getLayoutParams();
        int sideLength = Math.min(textureView.getWidth(), textureView.getHeight()) * 3 / 4;
        layoutParams.width = sideLength;
        layoutParams.height = sideLength;
        textureView.setLayoutParams(layoutParams);
        textureView.turnRound();
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            initCamera();
        }
    }

    @Override
    protected void onPause() {
        if (cameraHelper != null) {
            cameraHelper.stop();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraHelper != null) {
            cameraHelper.start();
        }
    }


    private Camera.Size previewSize;

    @Override
    public void onCameraOpened(Camera camera, int cameraId, final int displayOrientation, boolean isMirror) {
        previewSize = camera.getParameters().getPreviewSize();
        Log.i(TAG, "onCameraOpened:  previewSize = " + previewSize.width + "x" + previewSize.height);
        //在相机打开时，添加右上角的view用于显示原始数据和预览数据
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //将预览控件和预览尺寸比例保持一致，避免拉伸
                {
                    FrameLayout.LayoutParams textureViewLayoutParams = (FrameLayout.LayoutParams) textureView.getLayoutParams();
                    int newHeight = 0;
                    int newWidth = textureViewLayoutParams.width;
                    //横屏
                    if (displayOrientation % 180 == 0) {
                        newHeight = textureViewLayoutParams.width * previewSize.height / previewSize.width;
                    }
                    //竖屏
                    else {
                        newHeight = textureViewLayoutParams.width * previewSize.width / previewSize.height;
                    }
                    //当不是正方形预览的情况下，添加一层ViewGroup作为
                    if (newHeight != textureViewLayoutParams.height) {
                        insertFrameLayout = new RoundFrameLayout(CoverByParentCameraActivity.this);
                        int sideLength = Math.min(newWidth, newHeight);
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(sideLength, sideLength);
                        insertFrameLayout.setLayoutParams(layoutParams);
                        FrameLayout parentView = (FrameLayout) textureView.getParent();
                        parentView.removeView(textureView);
                        parentView.addView(insertFrameLayout);

                        insertFrameLayout.addView(textureView);
                        FrameLayout.LayoutParams newTextureViewLayoutParams = new FrameLayout.LayoutParams(newWidth, newHeight);
                        //横屏
                        if (displayOrientation % 180 == 0) {
                            newTextureViewLayoutParams.leftMargin = ((newHeight - newWidth) / 2);
                        }
                        //竖屏
                        else {
                            newTextureViewLayoutParams.topMargin = -(newHeight - newWidth) / 2;
                        }
                        textureView.setLayoutParams(newTextureViewLayoutParams);
                    }
                }
                if (radiusSeekBar != null) {
                    return;
                }
                View siblingView = insertFrameLayout != null ? insertFrameLayout : textureView;
                roundBorderView = new RoundBorderView(CoverByParentCameraActivity.this);
                ((FrameLayout) siblingView.getParent()).addView(roundBorderView, siblingView.getLayoutParams());

                radiusSeekBar = new SeekBar(CoverByParentCameraActivity.this);
                radiusSeekBar.setOnSeekBarChangeListener(CoverByParentCameraActivity.this);

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                FrameLayout.LayoutParams radiusSeekBarLayoutParams = new FrameLayout.LayoutParams(
                        displayMetrics.widthPixels, displayMetrics.heightPixels / 4
                );

                radiusSeekBarLayoutParams.gravity = Gravity.BOTTOM;
                radiusSeekBar.setLayoutParams(radiusSeekBarLayoutParams);
                ((FrameLayout) siblingView.getParent()).addView(radiusSeekBar);
                radiusSeekBar.post(new Runnable() {
                    @Override
                    public void run() {
                        radiusSeekBar.setProgress(radiusSeekBar.getMax());
                    }
                });
            }
        });
    }

    @Override
    public void onPreview(final byte[] nv21, Camera camera) {

    }

    @Override
    public void onCameraClosed() {
        Log.i(TAG, "onCameraClosed: ");
    }

    @Override
    public void onCameraError(Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {

    }

    @Override
    protected void onDestroy() {
        if (cameraHelper != null) {
            cameraHelper.release();
        }
        super.onDestroy();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (insertFrameLayout != null) {
            insertFrameLayout.setRadius(progress * Math.min(insertFrameLayout.getWidth(), insertFrameLayout.getHeight()) / 2 / seekBar.getMax());
            insertFrameLayout.turnRound();
        }else {
            textureView.setRadius(progress * Math.min(textureView.getWidth(), textureView.getHeight()) / 2 / seekBar.getMax());
            textureView.turnRound();
        }

        roundBorderView.setRadius(progress * Math.min(roundBorderView.getWidth(), roundBorderView.getHeight()) / 2 / seekBar.getMax());
        roundBorderView.turnRound();

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}