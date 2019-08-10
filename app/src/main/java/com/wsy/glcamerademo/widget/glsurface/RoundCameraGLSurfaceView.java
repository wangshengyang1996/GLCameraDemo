package com.wsy.glcamerademo.widget.glsurface;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class RoundCameraGLSurfaceView extends GLSurfaceView {
    private static final String TAG = "CameraGLSurfaceView";
    // 源视频帧宽/高
    private int frameWidth, frameHeight;
    private boolean isMirror;
    private int rotateDegree = 0;
    // 用于判断preview数据是否被传入，避免在初始化时有一段时间的绿色背景（y、u、v均全为0）
    private boolean dataInput = false;
    // 圆角半径
    private int radius = 0;

    private ByteBuffer yBuf = null, uBuf = null, vBuf = null;
    // 纹理id
    private int[] yTexture = new int[1];
    private int[] uTexture = new int[1];
    private int[] vTexture = new int[1];

    private byte[] yArray;
    private byte[] uArray;
    private byte[] vArray;

    private static final int FLOAT_SIZE_BYTES = 4;

    private String fragmentShaderCode = GLUtil.FRAG_SHADER_NORMAL;

    private FloatBuffer squareVertices = null;
    private FloatBuffer coordVertices = null;
    private boolean rendererReady = false;
    float[] coordVertice = null;

    public RoundCameraGLSurfaceView(Context context) {
        this(context, null);
    }

    public RoundCameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        //设置Renderer到GLSurfaceView
        setRenderer(new YUVRenderer());
        // 只有在绘制数据改变时才绘制view
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                Rect rect = new Rect(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
                outline.setRoundRect(rect, radius);
            }
        });
        setClipToOutline(true);
    }

    public void turnRound() {
        invalidateOutline();
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    /**
     * 设置不同的片段着色器代码以达到不同的预览效果
     * @param fragmentShaderCode 片段着色器代码
     */
    public void setFragmentShaderCode(String fragmentShaderCode) {
        this.fragmentShaderCode = fragmentShaderCode;
    }

    public void init(boolean isMirror, int rotateDegree, int frameWidth, int frameHeight) {
        if (this.frameWidth == frameWidth
                && this.frameHeight == frameHeight
                && this.rotateDegree == rotateDegree
                && this.isMirror == isMirror) {
            return;
        }
        dataInput = false;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.rotateDegree = rotateDegree;
        this.isMirror = isMirror;
        yArray = new byte[this.frameWidth * this.frameHeight];
        uArray = new byte[this.frameWidth * this.frameHeight / 4];
        vArray = new byte[this.frameWidth * this.frameHeight / 4];

        int yFrameSize = this.frameHeight * this.frameWidth;
        int uvFrameSize = yFrameSize >> 2;
        yBuf = ByteBuffer.allocateDirect(yFrameSize);
        yBuf.order(ByteOrder.nativeOrder()).position(0);

        uBuf = ByteBuffer.allocateDirect(uvFrameSize);
        uBuf.order(ByteOrder.nativeOrder()).position(0);

        vBuf = ByteBuffer.allocateDirect(uvFrameSize);
        vBuf.order(ByteOrder.nativeOrder()).position(0);
        // 顶点坐标
        squareVertices = ByteBuffer
                .allocateDirect(GLUtil.SQUARE_VERTICES.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        squareVertices.put(GLUtil.SQUARE_VERTICES).position(0);
        //纹理坐标
        if (isMirror) {
            switch (rotateDegree) {
                case 0:
                    coordVertice = GLUtil.MIRROR_COORD_VERTICES;
                    break;
                case 90:
                    coordVertice = GLUtil.ROTATE_90_MIRROR_COORD_VERTICES;
                    break;
                case 180:
                    coordVertice = GLUtil.ROTATE_180_MIRROR_COORD_VERTICES;
                    break;
                case 270:
                    coordVertice = GLUtil.ROTATE_270_MIRROR_COORD_VERTICES;
                    break;
                default:
                    break;
            }
        } else {
            switch (rotateDegree) {
                case 0:
                    coordVertice = GLUtil.COORD_VERTICES;
                    break;
                case 90:
                    coordVertice = GLUtil.ROTATE_90_COORD_VERTICES;
                    break;
                case 180:
                    coordVertice = GLUtil.ROTATE_180_COORD_VERTICES;
                    break;
                case 270:
                    coordVertice = GLUtil.ROTATE_270_COORD_VERTICES;
                    break;
                default:
                    break;
            }
        }
        coordVertices = ByteBuffer.allocateDirect(coordVertice.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        coordVertices.put(coordVertice).position(0);
    }

    /**
     * 创建OpenGL Program并关联GLSL中的变量
     *
     * @param fragmentShaderCode 片段着色器代码
     */
    private void createGLProgram(String fragmentShaderCode) {
        int programHandleMain = GLUtil.createShaderProgram(fragmentShaderCode);
        if (programHandleMain != -1) {
            // 使用着色器程序
            GLES20.glUseProgram(programHandleMain);
            // 获取顶点着色器变量
            int glPosition = GLES20.glGetAttribLocation(programHandleMain, "attr_position");
            int textureCoord = GLES20.glGetAttribLocation(programHandleMain, "attr_tc");

            // 获取片段着色器变量
            int ySampler = GLES20.glGetUniformLocation(programHandleMain, "ySampler");
            int uSampler = GLES20.glGetUniformLocation(programHandleMain, "uSampler");
            int vSampler = GLES20.glGetUniformLocation(programHandleMain, "vSampler");

            //给变量赋值
            /**
             * GLES20.GL_TEXTURE0 和 ySampler 绑定
             * GLES20.GL_TEXTURE1 和 uSampler 绑定
             * GLES20.GL_TEXTURE2 和 vSampler 绑定
             *
             * 也就是说 glUniform1i的第二个参数代表图层序号
             */
            GLES20.glUniform1i(ySampler, 0);
            GLES20.glUniform1i(uSampler, 1);
            GLES20.glUniform1i(vSampler, 2);

            GLES20.glEnableVertexAttribArray(glPosition);
            GLES20.glEnableVertexAttribArray(textureCoord);

            /**
             * 设置Vertex Shader数据
             */
            squareVertices.position(0);
            GLES20.glVertexAttribPointer(glPosition, GLUtil.COUNT_PER_SQUARE_VERTICE, GLES20.GL_FLOAT, false, 8, squareVertices);
            coordVertices.position(0);
            GLES20.glVertexAttribPointer(textureCoord, GLUtil.COUNT_PER_COORD_VERTICES, GLES20.GL_FLOAT, false, 8, coordVertices);
        }
    }

    public class YUVRenderer implements Renderer {
        private void initRenderer() {
            rendererReady = false;
            createGLProgram(fragmentShaderCode);

            //启用纹理
            GLES20.glEnable(GLES20.GL_TEXTURE_2D);
            //创建纹理
            createTexture(frameWidth, frameHeight, GLES20.GL_LUMINANCE, yTexture);
            createTexture(frameWidth / 2, frameHeight / 2, GLES20.GL_LUMINANCE, uTexture);
            createTexture(frameWidth / 2, frameHeight / 2, GLES20.GL_LUMINANCE, vTexture);

            rendererReady = true;
        }

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            initRenderer();
        }

        // 根据宽高和格式创建纹理
        private void createTexture(int width, int height, int format, int[] textureId) {
            //创建纹理
            GLES20.glGenTextures(1, textureId, 0);
            //绑定纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
            /**
             * {@link GLES20#GL_TEXTURE_WRAP_S}代表左右方向的纹理环绕模式
             * {@link GLES20#GL_TEXTURE_WRAP_T}代表上下方向的纹理环绕模式
             *
             *  {@link GLES20#GL_REPEAT}：重复
             *  {@link GLES20#GL_MIRRORED_REPEAT}：镜像重复
             *  {@link GLES20#GL_CLAMP_TO_EDGE}：忽略边框截取
             *
             * 例如我们使用{@link GLES20#GL_REPEAT}：
             *
             *             squareVertices           coordVertices
             *             -1.0f, -1.0f,            1.0f, 1.0f,
             *             1.0f, -1.0f,             1.0f, 0.0f,         ->          和textureView预览相同
             *             -1.0f, 1.0f,             0.0f, 1.0f,
             *             1.0f, 1.0f               0.0f, 0.0f
             *
             *             squareVertices           coordVertices
             *             -1.0f, -1.0f,            2.0f, 2.0f,
             *             1.0f, -1.0f,             2.0f, 0.0f,         ->          和textureView预览相比，分割成了4 块相同的预览（左下，右下，左上，右上）
             *             -1.0f, 1.0f,             0.0f, 2.0f,
             *             1.0f, 1.0f               0.0f, 0.0f
             */
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            /**
             * {@link GLES20#GL_TEXTURE_MIN_FILTER}代表所显示的纹理比加载进来的纹理小时的情况
             * {@link GLES20#GL_TEXTURE_MAG_FILTER}代表所显示的纹理比加载进来的纹理大时的情况
             *
             *  {@link GLES20#GL_NEAREST}：使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
             *  {@link GLES20#GL_LINEAR}：使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
             */
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, format, width, height, 0, format, GLES20.GL_UNSIGNED_BYTE, null);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            // 分别对每个纹理做激活、绑定、设置数据操作
            if (dataInput) {
                //y
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yTexture[0]);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D,
                        0,
                        0,
                        0,
                        frameWidth,
                        frameHeight,
                        GLES20.GL_LUMINANCE,
                        GLES20.GL_UNSIGNED_BYTE,
                        yBuf);

                //u
                GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, uTexture[0]);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D,
                        0,
                        0,
                        0,
                        frameWidth >> 1,
                        frameHeight >> 1,
                        GLES20.GL_LUMINANCE,
                        GLES20.GL_UNSIGNED_BYTE,
                        uBuf);

                //v
                GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, vTexture[0]);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D,
                        0,
                        0,
                        0,
                        frameWidth >> 1,
                        frameHeight >> 1,
                        GLES20.GL_LUMINANCE,
                        GLES20.GL_UNSIGNED_BYTE,
                        vBuf);
                //在数据绑定完成后进行绘制
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            }
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
        }
    }

    /**
     * 传入NV21刷新帧
     *
     * @param data NV21数据
     */
    public void refreshFrameNV21(byte[] data) {
        if (rendererReady) {
            yBuf.clear();
            uBuf.clear();
            vBuf.clear();
            putNV21(data, frameWidth, frameHeight);
            dataInput = true;
            requestRender();
        }
    }

    /**
     * 传入YV12数据刷新帧
     *
     * @param data YV12数据
     */
    public void refreshFrameYV12(byte[] data) {
        if (rendererReady) {
            yBuf.clear();
            uBuf.clear();
            vBuf.clear();
            putYV12(data, frameWidth, frameHeight);
            dataInput = true;
            requestRender();
        }
    }

    /**
     * 将NV21数据的Y、U、V分量取出
     *
     * @param src    nv21帧数据
     * @param width  宽度
     * @param height 高度
     */
    private void putNV21(byte[] src, int width, int height) {

        int ySize = width * height;
        int frameSize = ySize * 3 / 2;

        //取分量y值
        System.arraycopy(src, 0, yArray, 0, ySize);

        int k = 0;

        //取分量uv值
        int index = ySize;
        while (index < frameSize) {
            vArray[k] = src[index++];
            uArray[k++] = src[index++];
        }
        yBuf.put(yArray).position(0);
        uBuf.put(uArray).position(0);
        vBuf.put(vArray).position(0);
    }

    /**
     * 将YV12数据的Y、U、V分量取出
     *
     * @param src    YV12帧数据
     * @param width  宽度
     * @param height 高度
     */
    private void putYV12(byte[] src, int width, int height) {

        int ySize = width * height;

        //取分量y值
        System.arraycopy(src, 0, yArray, 0, ySize);

        //取分量uv值
        System.arraycopy(src, ySize, vArray, 0, vArray.length);
        System.arraycopy(src, ySize + vArray.length, uArray, 0, uArray.length);

        yBuf.put(yArray).position(0);
        uBuf.put(uArray).position(0);
        vBuf.put(vArray).position(0);
    }


}
