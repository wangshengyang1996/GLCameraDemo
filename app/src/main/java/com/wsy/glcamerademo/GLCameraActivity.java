package com.wsy.glcamerademo;

import android.Manifest;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.wsy.glcamerademo.camera.CameraHelper;
import com.wsy.glcamerademo.camera.CameraListener;
import com.wsy.glcamerademo.widget.glsurface.GLUtil;
import com.wsy.glcamerademo.widget.glsurface.RoundCameraGLSurfaceView;
import com.wsy.glcamerademo.util.ImageUtil;
import com.wsy.glcamerademo.widget.RoundBorderView;
import com.wsy.glcamerademo.widget.RoundTextureView;

public class GLCameraActivity extends BaseActivity implements ViewTreeObserver.OnGlobalLayoutListener, CameraListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "CameraActivity";
    private static final int ACTION_REQUEST_PERMISSIONS = 1;
    private CameraHelper cameraHelper;
    //原始数据显示的控件
    private RoundTextureView textureView;
    //用于调整圆角大小
    private SeekBar radiusSeekBar;

    //默认打开的CAMERA
    private static final int CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_BACK;
    //需要的权限
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA
    };
    //原始预览尺寸
    private Camera.Size previewSize;
    //正方形预览数据的宽度/高度
    private int squarePreviewSize;
    //被裁剪的区域，默认实现取最中心部分
    private Rect cropRect = null;
    //正方形预览数据
    private byte[] squareNV21;
    //正方形预览的控件
    private RoundCameraGLSurfaceView roundCameraGLSurfaceView;
    //边角效果
    private RoundBorderView borderTextureView;
    private RoundBorderView borderGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glsurface);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView();
    }

    private void initView() {
        textureView = findViewById(R.id.texture_preview);
        roundCameraGLSurfaceView = findViewById(R.id.camera_gl_surface_view);
        /**
         * {@link GLUtil#FRAG_SHADER_NORMAL} 正常效果
         * {@link GLUtil#FRAG_SHADER_GRAY} 灰度效果
         * {@link GLUtil#FRAG_SHADER_GRAVE} 浮雕效果
         */
        roundCameraGLSurfaceView.setFragmentShaderCode(GLUtil.FRAG_SHADER_NORMAL);
        roundCameraGLSurfaceView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    void initCamera() {
        cameraHelper = new CameraHelper.Builder()
                .cameraListener(this)
                .specificCameraId(CAMERA_ID)
                .previewOn(textureView)
                .previewViewSize(new Point(roundCameraGLSurfaceView.getLayoutParams().width, roundCameraGLSurfaceView.getLayoutParams().height))
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

    /**
     * 在布局完成时，
     * 将CameraGLSurfaceView设置为圆形，并将其宽高缩小到屏幕宽度的1/2；
     * 将TextureView的宽度缩小到屏幕宽度的1/2，高度等比缩放
     */
    @Override
    public void onGlobalLayout() {
        roundCameraGLSurfaceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        FrameLayout.LayoutParams glSurfaceViewLayoutParams = (FrameLayout.LayoutParams) roundCameraGLSurfaceView.getLayoutParams();
        FrameLayout.LayoutParams textureViewLayoutParams = (FrameLayout.LayoutParams) textureView.getLayoutParams();
        int sideLength = Math.min(textureView.getWidth(), textureView.getHeight()) / 2;
        glSurfaceViewLayoutParams.width = sideLength;
        glSurfaceViewLayoutParams.height = sideLength;
        textureViewLayoutParams.width = sideLength;
        textureViewLayoutParams.height = textureView.getHeight() * sideLength / textureView.getWidth();

        FrameLayout.LayoutParams tvGlSurfaceViewLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        FrameLayout.LayoutParams tvTextureViewLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        TextView tvTextureViewNotice = new TextView(this);
        tvTextureViewNotice.setText(textureView.getClass().getSimpleName());
        tvTextureViewNotice.setTextColor(Color.WHITE);
        tvTextureViewNotice.setBackgroundColor(getResources().getColor(R.color.colorShadow));
        tvTextureViewNotice.setLayoutParams(tvTextureViewLayoutParams);
        ((FrameLayout) textureView.getParent()).addView(tvTextureViewNotice);

        TextView tvGLSurfaceViewNotice = new TextView(this);
        tvGLSurfaceViewNotice.setText(roundCameraGLSurfaceView.getClass().getSimpleName());
        tvGLSurfaceViewNotice.setTextColor(Color.WHITE);
        tvGLSurfaceViewNotice.setBackgroundColor(getResources().getColor(R.color.colorShadow));
        tvGLSurfaceViewNotice.setLayoutParams(tvGlSurfaceViewLayoutParams);
        tvGLSurfaceViewNotice.setTranslationX(textureView.getWidth() - sideLength);

        ((FrameLayout) roundCameraGLSurfaceView.getParent()).addView(tvGLSurfaceViewNotice);

        glSurfaceViewLayoutParams.gravity = Gravity.END;
        roundCameraGLSurfaceView.setLayoutParams(glSurfaceViewLayoutParams);
        textureView.setLayoutParams(textureViewLayoutParams);


        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            initCamera();
        }
    }

    /**
     * 停止预览
     */
    @Override
    protected void onPause() {
        if (cameraHelper != null) {
            cameraHelper.stop();
        }
        super.onPause();
    }

    /**
     * 继续预览
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (cameraHelper != null) {
            cameraHelper.start();
        }
    }


    @Override
    public void onCameraOpened(Camera camera, final int cameraId, final int displayOrientation, boolean isMirror) {
        previewSize = camera.getParameters().getPreviewSize();
        //正方形预览区域的边长
        squarePreviewSize = Math.min(previewSize.width, previewSize.height);
        //裁剪的区域
        cropRect = new Rect((previewSize.width - squarePreviewSize) / 2, 0,
                (previewSize.width - (previewSize.width - squarePreviewSize) / 2), squarePreviewSize);
        squareNV21 = new byte[squarePreviewSize * squarePreviewSize * 3 / 2];
        Log.i(TAG, "onCameraOpened:  previewSize = " + previewSize.width + "x" + previewSize.height);
        //在相机打开时，添加右上角的view用于显示原始数据和预览数据
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //将预览控件和预览尺寸比例保持一致，避免拉伸
                {
                    ViewGroup.LayoutParams layoutParams = textureView.getLayoutParams();
                    //横屏
                    if (displayOrientation % 180 == 0) {
                        layoutParams.height = layoutParams.width * previewSize.height / previewSize.width;
                    }
                    //竖屏
                    else {
                        layoutParams.height = layoutParams.width * previewSize.width / previewSize.height;
                    }
                    textureView.setLayoutParams(layoutParams);
                }
                roundCameraGLSurfaceView.init(cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT, displayOrientation, squarePreviewSize, squarePreviewSize);
                if (radiusSeekBar != null) {
                    return;
                }
                borderTextureView = new RoundBorderView(GLCameraActivity.this);
                borderGLSurfaceView = new RoundBorderView(GLCameraActivity.this);
                ((FrameLayout) textureView.getParent()).addView(borderTextureView, textureView.getLayoutParams());
                ((FrameLayout) roundCameraGLSurfaceView.getParent()).addView(borderGLSurfaceView, roundCameraGLSurfaceView.getLayoutParams());
                addNotificationView();

            }
        });
    }

    /**
     * 添加显示原始预览帧和画面中的预览帧数据
     */
    private void addNotificationView() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        FrameLayout.LayoutParams radiusSeekBarLayoutParams = new FrameLayout.LayoutParams(
                displayMetrics.widthPixels, displayMetrics.heightPixels / 4
        );
        radiusSeekBarLayoutParams.gravity = Gravity.BOTTOM;

        radiusSeekBar = new SeekBar(GLCameraActivity.this);
        radiusSeekBarLayoutParams.gravity = Gravity.BOTTOM;
        radiusSeekBar.setOnSeekBarChangeListener(this);
        radiusSeekBar.setLayoutParams(radiusSeekBarLayoutParams);
        ((FrameLayout) roundCameraGLSurfaceView.getParent()).addView(radiusSeekBar);
        radiusSeekBar.post(new Runnable() {
            @Override
            public void run() {
                radiusSeekBar.setProgress(radiusSeekBar.getMax());
            }
        });
    }

    @Override
    public void onPreview(final byte[] nv21, Camera camera) {
        //裁剪指定的图像区域
        ImageUtil.cropNV21(nv21, this.squareNV21, previewSize.width, previewSize.height, cropRect);
        //刷新GLSurfaceView
        roundCameraGLSurfaceView.refreshFrameNV21(this.squareNV21);
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
        roundCameraGLSurfaceView.setRadius(progress * Math.min(roundCameraGLSurfaceView.getWidth(), roundCameraGLSurfaceView.getHeight()) / 2 / seekBar.getMax());
        roundCameraGLSurfaceView.turnRound();

        textureView.setRadius(progress * Math.min(textureView.getWidth(), textureView.getHeight()) / 2 / seekBar.getMax());
        textureView.turnRound();

        borderTextureView.setRadius(progress * Math.min(borderTextureView.getWidth(), borderTextureView.getHeight()) / 2 / seekBar.getMax());
        borderTextureView.turnRound();

        borderGLSurfaceView.setRadius(progress * Math.min(borderGLSurfaceView.getWidth(), borderGLSurfaceView.getHeight()) / 2 / seekBar.getMax());
        borderGLSurfaceView.turnRound();

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}