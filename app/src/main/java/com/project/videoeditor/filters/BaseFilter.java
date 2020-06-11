package com.project.videoeditor.filters;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Parcelable;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.project.videoeditor.PlayerController;
import com.project.videoeditor.support.SupportUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;



public abstract class BaseFilter implements GLSurfaceView.Renderer,SurfaceTexture.OnFrameAvailableListener,Cloneable, Parcelable {

    private final String TAG = "BaseFilter";

    protected static final int FLOAT_SIZE_BYTES = 4;
    protected static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    protected static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    protected static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;

    protected FloatBuffer mTriangleVertices;
    protected final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0, 0.f, 0.f,
            1.0f, -1.0f, 0, 1.f, 0.f,
            -1.0f, 1.0f, 0, 0.f, 1.f,
            1.0f, 1.0f, 0, 1.f, 1.f,
    };

    protected float[] mMVPMatrix = new float[16];
    protected float[] mSTMatrix = new float[16];

    protected int mProgram;
    protected int mTextureID;
    protected int muMVPMatrixHandle;
    protected int muSTMatrixHandle;
    protected int maPositionHandle;
    protected int maTextureHandle;
    protected Surface surface;
    protected SurfaceTexture mSurfaceTexture;
    private boolean isPlayerMod = false;

    protected boolean changeShaderFlag = false;
    protected static int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

    private boolean isInvert = false;

    protected int _updateTexImageCounter = 0;
    private int _updateTexImageCompare = 0;

    protected final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    protected String FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            + "varying vec2 vTextureCoord;\n"
            + "uniform samplerExternalOES sTexture;\n"
            + "void main() {\n"
            + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
            + "  gl_FragColor = color;\n"
            + "}\n";


    public Surface getSurface() {
        return surface;
    }
    private Context context;
    private PlayerController playerController;

    public BaseFilter() {
        initTriangleVertices();
    }

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    abstract public FiltersFactory.NameFilters getFilterName();

    public boolean isInvert() {
        return isInvert;
    }

    public void setInvert(boolean invert) {
        isInvert = invert;
    }

    protected void initTriangleVertices()
    {
        mTriangleVertices = ByteBuffer.allocateDirect(
                mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);
        Matrix.setIdentityM(mSTMatrix, 0);
    }
    public BaseFilter(Context context) {

        initTriangleVertices();
        this.context = context;
    }

    public BaseFilter(Context context, PlayerController playerController) {

        initTriangleVertices();
        this.context = context;
        this.playerController = playerController;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public int getTextureID() {
        return mTextureID;
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    void loadFragmentShaderFromResource(int resourceId)
    {
        FRAGMENT_SHADER = SupportUtil.OpenRawResourcesAsString(context,resourceId);
    }

    public void recreate(BaseFilter oldFilter) throws IOException {
        GLES20.glDeleteProgram(mProgram);
        mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (mProgram == 0) {
            return;
        }
        this._updateTexImageCounter = oldFilter._updateTexImageCounter;
        this._updateTexImageCompare = oldFilter._updateTexImageCompare;

        if(this._updateTexImageCompare == this._updateTexImageCounter)
            this._updateTexImageCounter++;

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
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);
        checkGlError("glBindTexture mTextureID");

        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        mTextureID = oldFilter.mTextureID;
        mSurfaceTexture = oldFilter.mSurfaceTexture;
        this.surface = oldFilter.surface;
        this.playerController = oldFilter.playerController;

    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (mProgram == 0) {
            return;
        }

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

        mSurfaceTexture = new SurfaceTexture(mTextureID);
       // mSurfaceTexture.setOnFrameAvailableListener(this);

        surface = new Surface(mSurfaceTexture);
        if(playerController != null && isPlayerMod) {
            playerController.getPlayer().setVideoSurface(surface);
            //mMediaPlayer.setScreenOnWhilePlaying(true);

        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        this.synchronizeDrawFrame();
        this.preDraw();
        this.bindResource();
        this.draw();
    }
    protected int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
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
    protected void bindResource()
    {
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
    }
    protected void draw()
    {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");
        GLES20.glFinish();
    }
    protected void synchronizeDrawFrame()
    {
        synchronized (this) {
            if(_updateTexImageCompare > Integer.MAX_VALUE - 2 || _updateTexImageCounter > Integer.MAX_VALUE - 1)
            {
                _updateTexImageCompare = 0;
                _updateTexImageCounter = 1;
            }
            if( mSurfaceTexture !=null && _updateTexImageCompare != _updateTexImageCounter )
            {
                // loop and call updateTexImage() for each time the onFrameAvailable() method was called below.
                while(_updateTexImageCompare != _updateTexImageCounter) {
                    mSurfaceTexture.updateTexImage();
                    mSurfaceTexture.getTransformMatrix(mSTMatrix);
                    _updateTexImageCompare++;  // increment the compare value until it's the same as _updateTexImageCounter
                }
            }
        }
    }
    protected void preDraw()
    {
        if(changeShaderFlag)
        {
            GLES20.glFinish();
            this.changeFragmentShader(FRAGMENT_SHADER);
            return;
        }
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");
    }
    protected int createProgram(String vertexSource, String fragmentSource) {
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

    protected void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    public void changeFragmentShaderInRealTime(String fragmentShader)
    {
        this.FRAGMENT_SHADER = fragmentShader;
        changeShaderFlag = true;
    }

    protected void changeFragmentShader(String fragmentShader) {
        GLES20.glDeleteProgram(mProgram);
        mProgram = createProgram(VERTEX_SHADER, fragmentShader);
        if (mProgram == 0) {
            throw new RuntimeException("failed creating program");
        }
        changeShaderFlag = false;
    }


    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        _updateTexImageCounter++;
    }

    public  void setPlayerController(PlayerController playerController)
    {
        this.playerController = playerController;
        if(playerController != null && isPlayerMod)
            playerController.getPlayer().setVideoSurface(surface);

    }
    public boolean isPlayerMod() {
        return isPlayerMod;
    }

    public void setPlayerMod(boolean playerMod) {
        isPlayerMod = playerMod;
    }

    public PlayerController getPlayerController() {
        return playerController;
    }

}
