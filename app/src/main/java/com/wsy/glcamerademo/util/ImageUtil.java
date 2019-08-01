package com.wsy.glcamerademo.util;

import android.graphics.Rect;

public class ImageUtil {
    /**
     * 将Y:U:V == 4:2:2的数据转换为nv21
     *
     * @param y      Y 数据
     * @param u      U 数据
     * @param v      V 数据
     * @param nv21   生成的nv21，需要预先分配内存
     * @param stride 步长
     * @param height 图像高度
     */
    public static void yuv422ToYuv420sp(byte[] y, byte[] u, byte[] v, byte[] nv21, int stride, int height) {
        System.arraycopy(y, 0, nv21, 0, y.length);
        int nv21UVIndex = stride * height;
        int length = y.length + u.length / 2 + v.length / 2 - 2;
        int uIndex = 0, vIndex = 0;
        for (int i = nv21UVIndex; i < length; i += 2) {
            vIndex += 2;
            uIndex += 2;
            nv21[i] = v[vIndex];
            nv21[i + 1] = u[uIndex];
        }
    }

    /**
     * 裁剪NV21数据
     *
     * @param originNV21 原始的NV21数据
     * @param cropNV21   裁剪结果NV21数据，需要预先分配内存
     * @param width      原始数据的宽度
     * @param height     原始数据的高度
     * @param left       原始数据被裁剪的区域的左边界
     * @param top        原始数据被裁剪的区域的上边界
     * @param right      原始数据被裁剪的区域的右边界
     * @param bottom     原始数据被裁剪的区域的下边界
     */
    public static void cropNV21(byte[] originNV21, byte[] cropNV21, int width, int height, int left, int top, int right, int bottom) {
        int halfWidth = width / 2;
        int cropImageWidth = right - left;
        int cropImageHeight = bottom - top;

        //原数据Y左上
        int originalYLineStart = top * width;
        int targetYIndex = 0;

        //原数据UV左上
        int originalUVLineStart = width * height + top * halfWidth;

        //目标数据的UV起始值
        int targetUVIndex = cropImageWidth * cropImageHeight;

        for (int i = top; i < bottom; i++) {
            System.arraycopy(originNV21, originalYLineStart + left, cropNV21, targetYIndex, cropImageWidth);
            originalYLineStart += width;
            targetYIndex += cropImageWidth;
            if ((i & 1) == 0) {
                System.arraycopy(originNV21, originalUVLineStart + left, cropNV21, targetUVIndex, cropImageWidth);
                originalUVLineStart += width;
                targetUVIndex += cropImageWidth;
            }
        }
    }


    /**
     * 裁剪NV21数据
     *
     * @param originNV21 原始的NV21数据
     * @param cropNV21   裁剪结果NV21数据，需要预先分配内存
     * @param width      原始数据的宽度
     * @param height     原始数据的高度
     * @param rect       原始数据被裁剪的区域
     */
    public static void cropNV21(byte[] originNV21, byte[] cropNV21, int width, int height, Rect rect) {
        if (originNV21.length != width * height * 3 / 2) {
            throw new IllegalArgumentException("invalid origin squareNV21");
        }
        if (rect == null || rect.isEmpty()) {
            throw new IllegalArgumentException("rect is null or empty");
        }
        if (cropNV21.length != rect.width() * rect.height() * 3 / 2) {
            throw new IllegalArgumentException("cropNV21's size and rect size mismatch");
        }
        if (rect.left < 0 || rect.top < 0 || rect.right > width || rect.bottom > height) {
            throw new IllegalArgumentException("rect is not inside image");
        }
        cropNV21(originNV21, cropNV21, width, height, rect.left, rect.top, rect.right, rect.bottom);
    }
}