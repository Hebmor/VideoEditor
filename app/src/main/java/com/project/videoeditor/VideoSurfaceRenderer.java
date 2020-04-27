package com.project.videoeditor;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class VideoSurfaceRenderer
        implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private static String TAG = "VideoRender";

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private int pStitchingSize = -1;
    private int pInvert = -1;
    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0, 0.f, 0.f,
            1.0f, -1.0f, 0, 1.f, 0.f,
            -1.0f, 1.0f, 0, 0.f, 1.f,
            1.0f, 1.0f, 0, 1.f, 1.f,
    };

    private FloatBuffer mTriangleVertices;

    private final String mVertexShader =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";
    String mFragmentShader = "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            + "varying vec2 vTextureCoord;\n"
            + "uniform samplerExternalOES sTexture;\n"
            + "void main() {\n"
            + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
            + "  float colorR = (color.r + color.g + color.b) / 3.0;\n"
            + "  float colorG = (color.r + color.g + color.b) / 3.0;\n"
            + "  float colorB = (color.r + color.g + color.b) / 3.0;\n"
            + "  gl_FragColor = vec4(colorR, colorG, colorB, color.a);\n"
            + "}\n";

    private float[] mMVPMatrix = new float[16];
    private float[] mSTMatrix = new float[16];

    private int mProgram;
    private int mTextureID;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;

    private int pResolution;
    private int pRadius;
    private int pDir;

    float color[] = {0.2f, 0.709803922f, 0.898039216f, 1.0f};
    private SurfaceTexture mSurface;
    private boolean updateSurface = false;

    private static int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

    private MediaPlayer mMediaPlayer;
    private Context context;
    private int pColorHandle;

    private int _updateTexImageCounter = 0;
    private int _updateTexImageCompare = 0;

    public VideoSurfaceRenderer(Context context) {
        mTriangleVertices = ByteBuffer.allocateDirect(
                mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);

        Matrix.setIdentityM(mSTMatrix, 0);
        this.context = context;

        InputStream inputStream = context.getResources().openRawResource(R.raw.default_state);
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line = null;
        try {
            line = r.readLine();
            while (line != null) {
                total.append(line).append('\n');
                line = r.readLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        mFragmentShader = total.toString();
    }

    public void setMediaPlayer(MediaPlayer player) {
        mMediaPlayer = player;
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        synchronized (this) {
            if( mSurface!=null && _updateTexImageCompare != _updateTexImageCounter )
            {
                // loop and call updateTexImage() for each time the onFrameAvailable() method was called below.
                while(_updateTexImageCompare != _updateTexImageCounter) {
                    mSurface.updateTexImage();
                    mSurface.getTransformMatrix(mSTMatrix);
                    _updateTexImageCompare++;  // increment the compare value until it's the same as _updateTexImageCounter
                }
            }
        }

        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

        Matrix.setIdentityM(mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);
            /*GLES20.glUniform1f(pStitchingSize,6.0f);
            GLES20.glUniform1i(pInvert,1);*/

        GLES20.glUniform1f(pResolution, 1);
        GLES20.glUniform1f(pRadius, 500);
        GLES20.glUniform2f(pDir, 1.0f, 1.0f);
        GLES20.glUniform4fv(pColorHandle, 1, color, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");
        GLES20.glFinish();

    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        //onSurfaceChanged(glUnused, width,height);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {

        //InputStream is = context.getResources().openRawResource();

        mProgram = createProgram(mVertexShader, mFragmentShader);
        if (mProgram == 0) {
            return;
        }

        pStitchingSize = GLES20.glGetUniformLocation(mProgram, "aStitchingSize");
        checkGlError("glGetUniformLocation aStitchingSize");
        pInvert = GLES20.glGetUniformLocation(mProgram, "aInvert");
        checkGlError("glGetUniformLocation aInvert");

        pResolution = GLES20.glGetUniformLocation(mProgram, "resolution");
        checkGlError("glGetUniformLocation resolution");
        pDir = GLES20.glGetUniformLocation(mProgram, "dir");
        checkGlError("glGetUniformLocation resolution");
        pRadius = GLES20.glGetUniformLocation(mProgram, "radius");
        checkGlError("glGetUniformLocation resolution");
        pColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");


        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }

        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        checkGlError("glGetUniformLocation uSTMatrix");
        if (muSTMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uSTMatrix");
        }


        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        mTextureID = textures[0];
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);
        checkGlError("glBindTexture mTextureID");

        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        /*
         * Create the SurfaceTexture that will feed this textureID,
         * and pass it to the MediaPlayer
         */
        mSurface = new SurfaceTexture(mTextureID);

        mSurface.setOnFrameAvailableListener(this);

        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "myvideo.mp4");
        if (file.exists()) {
            file.delete();
        }
        Surface surface = new Surface(mSurface);

        mMediaPlayer.setSurface(surface);

        mMediaPlayer.setScreenOnWhilePlaying(true);


        try {
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }


        synchronized (this) {
            updateSurface = false;
        }
        surface.release();
        mMediaPlayer.start();
    }

    synchronized public void onFrameAvailable(SurfaceTexture surface) {


        updateSurface = true;
        _updateTexImageCounter++;
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }
        int program = GLES20.glCreateProgram();
        if (program != 0) {

            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
        /*private String OpenShaderFromFile(String path) throws Exception {
            File shaderFile = new File(path);
            if(!shaderFile.exists())
                throw new Exception("Файл не существует!");
            if(!shaderFile.canRead())
                throw new Exception("Файл не может быть прочитан!");



        }*/

}
