package com.project.videoeditor.filters;

public class ImageKernelMatrix {

    public static final float[] identity_kernel = {
            // X, Y, Z
            0.0f,   0.0f,  0.0f,
            0.0f,   1.0f,  0.0f,
            0.0f,   0.0f,  0.0f
    };

    public static final float[] gaussianBlur_kernel = {
            // X, Y, Z
            1.0f / 16.0f,   2.0f / 16.0f,  1.0f / 16.0f,
            2.0f / 16.0f,   4.0f / 16.0f,  2.0f / 16.0f,
            1.0f / 16.0f,   2.0f / 16.0f,  1.0f / 16.0f
    };

    public static final float[] edge_kernel = {
            // X, Y, Z
            -1.0f / -1.0f, -1.0f / -1.0f, -1.0f / -1.0f,
            -1.0f / -1.0f,  9.0f / -1.0f, -1.0f / -1.0f,
            -1.0f / -1.0f, -1.0f / -1.0f, -1.0f / -1.0f
    };

    public static final float[] edgeEnhance_kernel = {
            // X, Y, Z
            0.0f / -1.0f,  0.0f / -1.0f,  0.0f,
            9.0f / -1.0f, -9.0f / -1.0f,  0.0f,
            0.0f / -1.0f,  0.0f / -1.0f,  0.0f
    };

    public static final float[] sharp_kernel = {
            // X, Y, Z
            0.0f ,  -1.0f,   0.0f,
            -1.0f,  5.0f,  -1.0f,
            0.0f,  -1.0f,   0.0f
    };
}
