package com.wsy.glcamerademo;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.hardware.camera2.CameraDevice;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.wsy.glcamerademo.camera2.Camera2Helper;
import com.wsy.glcamerademo.camera2.Camera2Listener;
import com.wsy.glcamerademo.widget.RoundBorderView;
import com.wsy.glcamerademo.widget.RoundTextureView;


public class Camera2Activity extends BaseActivity implements ViewTreeObserver.OnGlobalLayoutListener, Camera2Listener, SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "CameraActivity";
    private static final int ACTION_REQUEST_PERMISSIONS = 1;
    private Camera2Helper camera2Helper;
    private RoundTextureView textureView;
    private RoundBorderView roundBorderView;
    //用于调整圆角大小
    private SeekBar radiusSeekBar;
    //默认打开的CAMERA
    private static final String CAMERA_ID = Camera2Helper.CAMERA_ID_BACK;
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
        camera2Helper = new Camera2Helper.Builder()
                .cameraListener(this)
                .specificCameraId(CAMERA_ID)
                .context(getApplicationContext())
                .previewOn(textureView)
                .previewViewSize(new Point(textureView.getLayoutParams().width, textureView.getLayoutParams().height))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .build();
        camera2Helper.start();
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
        if (camera2Helper != null) {
            camera2Helper.stop();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (camera2Helper != null) {
            camera2Helper.start();
        }
    }


    @Override
    public void onCameraOpened(CameraDevice cameraDevice, String cameraId, final Size previewSize, final int displayOrientation, boolean isMirror) {
        Log.i(TAG, "onCameraOpened:  previewSize = " + previewSize.getWidth() + "x" + previewSize.getHeight());
        //在相机打开时，添加右上角的view用于显示原始数据和预览数据
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //将预览控件和预览尺寸比例保持一致，避免拉伸
                {
                    ViewGroup.LayoutParams layoutParams = textureView.getLayoutParams();
                    //横屏
                    if (displayOrientation % 180 == 0) {
                        layoutParams.height = layoutParams.width * previewSize.getHeight() / previewSize.getWidth();
                    }
                    //竖屏
                    else {
                        layoutParams.height = layoutParams.width * previewSize.getWidth() / previewSize.getHeight();
                    }
                    textureView.setLayoutParams(layoutParams);
                }
                if (radiusSeekBar != null) {
                    return;
                }

                roundBorderView = new RoundBorderView(Camera2Activity.this);
                ((FrameLayout) textureView.getParent()).addView(roundBorderView, textureView.getLayoutParams());

                radiusSeekBar = new SeekBar(Camera2Activity.this);
                radiusSeekBar.setOnSeekBarChangeListener(Camera2Activity.this);

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                FrameLayout.LayoutParams radiusSeekBarLayoutParams = new FrameLayout.LayoutParams(
                        displayMetrics.widthPixels, displayMetrics.heightPixels / 4
                );
                radiusSeekBarLayoutParams.gravity = Gravity.BOTTOM;
                radiusSeekBar.setLayoutParams(radiusSeekBarLayoutParams);
                ((FrameLayout) textureView.getParent()).addView(radiusSeekBar);
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
    public void onPreview(final byte[] y, final byte[] u, final byte[] v, final Size previewSize, final int stride) {

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
    protected void onDestroy() {
        if (camera2Helper != null) {
            camera2Helper.release();
        }
        super.onDestroy();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        textureView.setRadius(progress * Math.min(textureView.getWidth(), textureView.getHeight()) / 2 / seekBar.getMax());
        textureView.turnRound();

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