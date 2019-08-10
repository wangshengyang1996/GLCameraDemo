package com.wsy.glcamerademo.widget.glsurface;

import android.opengl.GLES20;

import java.nio.IntBuffer;

public class GLUtil {
    private static final String TAG = "GLUtil";

    /**
     * 顶点着色器
     */
    private static String VERTEX_SHADER =
            "    attribute vec4 attr_position;\n" +
                    "    attribute vec2 attr_tc;\n" +
                    "    varying vec2 tc;\n" +
                    "    void main() {\n" +
                    "        gl_Position = attr_position;\n" +
                    "        tc = attr_tc;\n" +
                    "    }";

    /**
     * 片段着色器，正常效果
     */
    public static String FRAG_SHADER_NORMAL =
            "    varying vec2 tc;\n" +
                    "    uniform sampler2D ySampler;\n" +
                    "    uniform sampler2D uSampler;\n" +
                    "    uniform sampler2D vSampler;\n" +
                    "    const mat3 convertMat = mat3(1.0, 1.0, 1.0, 0, -0.344, 1.77, 1.403, -0.714,0);\n" +
                    "    void main()\n" +
                    "    {\n" +
                    "        vec3 yuv;\n" +
                    "        yuv.x = texture2D(ySampler, tc).r;\n" +
                    "        yuv.y = texture2D(uSampler, tc).r - 0.5;\n" +
                    "        yuv.z = texture2D(vSampler, tc).r - 0.5;\n" +
                    "        gl_FragColor = vec4(convertMat * yuv, 1.0);\n" +
                    "    }";
    /**
     * 片段着色器，灰度效果
     */
    public static String FRAG_SHADER_GRAY =
            "    varying vec2 tc;\n" +
                    "    uniform sampler2D ySampler;\n" +
                    "    uniform sampler2D uSampler;\n" +
                    "    uniform sampler2D vSampler;\n" +
                    "    const mat3 convertMat = mat3(1.0, 1.0, 1.0, 0, -0.344, 1.77, 1.403, -0.714,0);\n" +
                    "    void main()\n" +
                    "    {\n" +
                    "        vec3 yuv;\n" +
                    "        yuv.x = texture2D(ySampler, tc).r;\n" +
                    "        gl_FragColor = vec4(vec3(yuv.x), 1.0);\n" +
                    "    }";
    /**
     * 片段着色器，浮雕效果
     */
    public static String FRAG_SHADER_GRAVE =
            "precision mediump float;\n" +
                    "varying vec2 tc;\n" +
                    "    uniform sampler2D ySampler;\n" +
                    "    uniform sampler2D uSampler;\n" +
                    "    uniform sampler2D vSampler;\n" +
                    "    const vec2 texSize = vec2(100.0, 100.0);\n" +
                    "    const vec4 graveColor = vec4(0.5, 0.5, 0.5, 1.0);\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "    vec2 upLeftCoord = vec2(tc.x-1.0/texSize.x, tc.y-1.0/texSize.y);\n" +
                    "    vec4 curColor = texture2D(ySampler, tc);\n" +
                    "    vec4 upLeftColor = texture2D(ySampler, upLeftCoord);\n" +
                    "    vec4 delColor = curColor - upLeftColor;\n" +
                    "    gl_FragColor = vec4(vec3(delColor), 0.0) + graveColor;\n" +
                    "}";

    //SQUARE_VERTICES每2个值作为一个顶点
    static final int COUNT_PER_SQUARE_VERTICE = 2;
    //COORD_VERTICES每2个值作为一个顶点
    static final int COUNT_PER_COORD_VERTICES = 2;


    /**
     * 显示的顶点
     */
    static final float[] SQUARE_VERTICES = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f
    };
    /**
     * 原数据显示
     * 0,1***********1,1
     * *             *
     * *             *
     * *             *
     * *             *
     * *             *
     * 0,0***********1,0
     */
    static final float[] COORD_VERTICES = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
    };

    /**
     * 逆时针旋转90度显示
     * 1,1***********1,0
     * *             *
     * *             *
     * *             *
     * *             *
     * *             *
     * 0,1***********0,0
     */
    static final float[] ROTATE_90_COORD_VERTICES = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f
    };

    /**
     * 逆时针旋转180度显示
     * 0,1***********1,1
     * *             *
     * *             *
     * *             *
     * *             *
     * *             *
     * 0,0***********1,0
     */
    static final float[] ROTATE_180_COORD_VERTICES = {
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
    };

    /**
     * 逆时针旋转270度显示
     * 0,1***********1,1
     * *             *
     * *             *
     * *             *
     * *             *
     * *             *
     * 0,0***********1,0
     */
    static final float[] ROTATE_270_COORD_VERTICES = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
    };

    /**
     * 镜像显示
     * 1,1***********0,1
     * *             *
     * *             *
     * *             *
     * *             *
     * *             *
     * 1,0***********0,0
     */
    static final float[] MIRROR_COORD_VERTICES = {
            1.0f, 1.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f
    };

    /**
     * 镜像并逆时针旋转90度显示
     * 0,1***********0,0
     * *             *
     * *             *
     * *             *
     * *             *
     * *             *
     * 1,1***********1,0
     */
    static final float[] ROTATE_90_MIRROR_COORD_VERTICES = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };
    /**
     * 镜像并逆时针旋转180度显示
     * 1,0***********0,0
     * *             *
     * *             *
     * *             *
     * *             *
     * *             *
     * 1,1***********0,1
     */
    static final float[] ROTATE_180_MIRROR_COORD_VERTICES = {
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
    };
    /**
     * 镜像并逆时针旋转270度显示
     * 1,0***********1,1
     * *             *
     * *             *
     * *             *
     * *             *
     * *             *
     * 0,0***********0,1
     */
    static final float[] ROTATE_270_MIRROR_COORD_VERTICES = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f
    };

    /**
     * 创建OpenGL Program，并链接
     *
     * @param fragmentShaderCode 片段着色器代码
     * @return OpenGL Program对象的引用
     */
    static int createShaderProgram(String fragmentShaderCode) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // 创建一个空的OpenGL ES Program
        int mProgram = GLES20.glCreateProgram();
        // 将vertex shader添加到program
        GLES20.glAttachShader(mProgram, vertexShader);
        // 将fragment shader添加到program
        GLES20.glAttachShader(mProgram, fragmentShader);
        // 链接创建好的 OpenGL ES program
        GLES20.glLinkProgram(mProgram);

        // 检查链接状态
        IntBuffer linked = IntBuffer.allocate(1);
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linked);
        if (linked.get(0) == 0) {
            return -1;
        }
        return mProgram;
    }

    /**
     * 加载着色器
     *
     * @param type       着色器类型，可以是片段着色器{@link GLES20#GL_FRAGMENT_SHADER}或顶点着色器{@link GLES20#GL_VERTEX_SHADER}
     * @param shaderCode 着色器代码
     * @return 着色器对象的引用
     */
    private static int loadShader(int type, String shaderCode) {
        //创建空的shader
        int shader = GLES20.glCreateShader(type);
        //加载shader代码
        GLES20.glShaderSource(shader, shaderCode);
        //编译shader
        GLES20.glCompileShader(shader);

        //检查编译是否成功
        IntBuffer compiled = IntBuffer.allocate(1);
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled);
        if (compiled.get(0) == 0) {
            return 0;
        }
        return shader;
    }
}
