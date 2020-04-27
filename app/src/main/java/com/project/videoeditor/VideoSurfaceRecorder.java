package com.project.videoeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.PrimitiveIterator;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class VideoSurfaceRecorder
        implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private static final int MAX_FRAMES = 30;
    private  String FILES_DIR;
    private static String TAG = "VideoRender";
    private ByteBuffer mPixelBuf;                       // used by saveFrame()
    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private int pStitchingSize = -1;
    private int pInvert = -1;
    private String DEBUG_FILE_NAME_BASE;
    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0, 0.f, 0.f,
            1.0f, -1.0f, 0, 1.f, 0.f,
            -1.0f, 1.0f, 0, 0.f, 1.f,
            1.0f, 1.0f, 0, 1.f, 1.f,
    };

    private FloatBuffer mTriangleVertices;
    private Object mFrameSyncObject = new Object();     // guards mFrameAvailable
    private boolean mFrameAvailable;

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
    private static final String MIME_TYPE = "video/avc";
    private int mProgram;
    private int mTextureID;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;

    private int pResolution;
    private int pRadius;
    private int pDir;
    private int mBitrate = 0;
    float color[] = {0.2f, 0.709803922f, 0.898039216f, 1.0f};

    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;
    private boolean updateSurface = false;


    private static int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
    MediaCodec decoder = null;
    MediaCodec encoder = null;
    private int mWidth = 1920;
    private int mHeight = 1080;
    private  OutputSurface outputSurface;

    public void setExtractor(MediaExtractor extractor) {
        this.extractor = extractor;
    }

    MediaExtractor extractor = null;
    private Context context;
    private int pColorHandle;

    private int _updateTexImageCounter = 0;
    private int _updateTexImageCompare = 0;

    public VideoSurfaceRecorder(Context context) {
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

        File mOutputFile = null;

        mOutputFile = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath() + "/" + "captures" + "/");
        if(!mOutputFile.exists())
            mOutputFile.mkdirs();
        FILES_DIR = mOutputFile.getPath();
        mOutputFile = new File(
                mOutputFile.getPath(),"outputDebug.mp4");

        DEBUG_FILE_NAME_BASE = mOutputFile.getPath();
    }
    public void setmBitrate(int mBitrate) {
        this.mBitrate = mBitrate;
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        synchronized (this) {
            if (mSurface != null && _updateTexImageCompare != _updateTexImageCounter) {
                // loop and call updateTexImage() for each time the onFrameAvailable() method was called below.
                while (_updateTexImageCompare != _updateTexImageCounter) {
                    mSurfaceTexture.updateTexImage();
                    mSurfaceTexture.getTransformMatrix(mSTMatrix);
                    _updateTexImageCompare++;  // increment the compare value until it's the same as _updateTexImageCounter
                }
            }
        }
        mSTMatrix[5] = -mSTMatrix[5];
        mSTMatrix[13] = 1.0f - mSTMatrix[13];
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
        mSurfaceTexture = new SurfaceTexture(mTextureID);

        mSurfaceTexture.setOnFrameAvailableListener(this);
        VideoChunks outputData = new VideoChunks();
        InputSurface inputSurface = null;
        MediaCodec encoder = null;
        mPixelBuf = ByteBuffer.allocateDirect(mWidth * mHeight * 4);
        mPixelBuf.order(ByteOrder.LITTLE_ENDIAN);
        mSurface = new Surface(mSurfaceTexture);
        try {
            int trackIndex = selectTrack(extractor);
            if (trackIndex < 0) {
                throw new RuntimeException("No video track found in ");
            }
            extractor.selectTrack(trackIndex);

            MediaFormat inputFormat = extractor.getTrackFormat(trackIndex);
            String mime = inputFormat.getString(MediaFormat.KEY_MIME);
            mWidth = inputFormat.getInteger(MediaFormat.KEY_WIDTH);
            mHeight = inputFormat.getInteger(MediaFormat.KEY_HEIGHT);
            MediaFormat outputFormat = MediaFormat.createVideoFormat(mime , mWidth, mHeight);
            outputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, (int) (3.8 * 1000000));
          outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE,
                    inputFormat.getInteger(MediaFormat.KEY_FRAME_RATE));
            outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,  2);


            outputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            outputData.setMediaFormat(outputFormat);

            encoder = MediaCodec.createEncoderByType(mime );

            encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);


            inputSurface = new InputSurface(encoder.createInputSurface());
            inputSurface.makeCurrent();
            encoder.start();

            Log.d(TAG, "Video size is " + inputFormat.getInteger(MediaFormat.KEY_WIDTH) + "x" +
                    inputFormat.getInteger(MediaFormat.KEY_HEIGHT));
            outputSurface = new OutputSurface();
            outputSurface.changeFragmentShader(mFragmentShader);


            decoder = MediaCodec.createDecoderByType(mime);

            decoder.configure(inputFormat,outputSurface.getSurface(), null, 0);
            decoder.start();
            doExtract(extractor,trackIndex,decoder,outputSurface,outputData,encoder,inputSurface);
            if (outputSurface != null) {
                outputSurface.release();
                outputSurface = null;
            }
            if (decoder != null) {
                decoder.stop();
                decoder.release();
                //decoder = null;
            }
            if (extractor != null) {
                extractor.release();
                //extractor = null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        //mMediaPlayer.setSurface(surface);

        // mMediaPlayer.setScreenOnWhilePlaying(true);


        synchronized (this) {
            updateSurface = false;
        }
    }
    public void saveFrame(String filename) throws IOException {
        // glReadPixels gives us a ByteBuffer filled with what is essentially big-endian RGBA
        // data (i.e. a byte of red, followed by a byte of green...).  To use the Bitmap
        // constructor that takes an int[] array with pixel data, we need an int[] filled
        // with little-endian ARGB data.
        //
        // If we implement this as a series of buf.get() calls, we can spend 2.5 seconds just
        // copying data around for a 720p frame.  It's better to do a bulk get() and then
        // rearrange the data in memory.  (For comparison, the PNG compress takes about 500ms
        // for a trivial frame.)
        //
        // So... we set the ByteBuffer to little-endian, which should turn the bulk IntBuffer
        // get() into a straight memcpy on most Android devices.  Our ints will hold ABGR data.
        // Swapping B and R gives us ARGB.  We need about 30ms for the bulk get(), and another
        // 270ms for the color swap.
        //
        // We can avoid the costly B/R swap here if we do it in the fragment shader (see
        // http://stackoverflow.com/questions/21634450/ ).
        //
        // Having said all that... it turns out that the Bitmap#copyPixelsFromBuffer()
        // method wants RGBA pixels, not ARGB, so if we create an empty bitmap and then
        // copy pixel data in we can avoid the swap issue entirely, and just copy straight
        // into the Bitmap from the ByteBuffer.
        //
        // Making this even more interesting is the upside-down nature of GL, which means
        // our output will look upside-down relative to what appears on screen if the
        // typical GL conventions are used.  (For ExtractMpegFrameTest, we avoid the issue
        // by inverting the frame when we render it.)
        //
        // Allocating large buffers is expensive, so we really want mPixelBuf to be
        // allocated ahead of time if possible.  We still get some allocations from the
        // Bitmap / PNG creation.

        mPixelBuf.rewind();
        
        GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                mPixelBuf);

        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(filename));
            Bitmap bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mPixelBuf.rewind();
            bmp.copyPixelsFromBuffer(mPixelBuf);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
            bmp.recycle();
        } finally {
            if (bos != null) bos.close();
        }

            Log.d(TAG, "Saved " + 1920 + "x" + 1080 + " frame as '" + filename + "'");

    }
    synchronized public void onFrameAvailable(SurfaceTexture surface) {

        Log.d(TAG, "new frame available");
        synchronized (mFrameSyncObject) {
            if (mFrameAvailable) {
                throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
            }
            mFrameAvailable = true;
            mFrameSyncObject.notifyAll();
        }
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
    private int selectTrack(MediaExtractor extractor) {
        // Select the first video track we find, ignore the rest.
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                Log.d(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
            }
            return i;
        }


        return -1;
    }
    private void doExtract(MediaExtractor extractor, int trackIndex, MediaCodec decoder,OutputSurface outputSurface,VideoChunks outputData, MediaCodec encoder,InputSurface inputSurface) throws IOException {
        VideoChunks videoChunks = new VideoChunks();
        videoChunks.setMediaFormat(extractor.getTrackFormat(trackIndex));
        final int TIMEOUT_USEC = 10000;
        ByteBuffer[] decoderInputBuffers = decoder.getInputBuffers();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();
        int inputChunk = 0;
        int decodeCount = 0;
        long frameSaveTime = 0;
        long rawSize = 0;
        ByteBuffer[] decoderOutputBuffers = decoder.getOutputBuffers();
        boolean outputDone = false;
        boolean inputDone = false;
        boolean decoderDone = false;
        int outputCount = 0;
        byte[] frameData;
        FileOutputStream outputStream = null;
        String fileName = DEBUG_FILE_NAME_BASE;
            try {
                outputStream = new FileOutputStream(fileName);
                Log.d(TAG, "encoded output will be saved as " + fileName);
            } catch (IOException ioe) {
                Log.w(TAG, "Unable to create debug output file " + fileName);
                throw new RuntimeException(ioe);
            }

        while (!outputDone) {
           Log.d(TAG, "loop");

            // Feed more data to the decoder.
            if (!inputDone) {
                int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                    ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                    int chunkSize = extractor.readSampleData(inputBuf, 0);

                    // Read the sample data into the ByteBuffer.  This neither respects nor
                    // updates inputBuf's position, limit, etc.

                    if (chunkSize < 0) {
                        // End of stream -- send empty frame with EOS flag set.
                        decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputDone = true;
                        Log.d(TAG, "sent input EOS");
                    } else {
                        if (extractor.getSampleTrackIndex() != trackIndex) {
                            Log.w(TAG, "WEIRD: got sample from track " +
                                    extractor.getSampleTrackIndex() + ", expected " + trackIndex);
                        }
                        long presentationTimeUs = extractor.getSampleTime();
                        decoder.queueInputBuffer(inputBufIndex, 0, chunkSize,
                                presentationTimeUs, 0 /*flags*/);
                        {
                            Log.d(TAG, "submitted frame " + inputChunk + " to dec, size=" +
                                    chunkSize);
                        }
                        //frameData = new byte[chunkSize];
                        //inputBuf.get(frameData);
                        //
                        inputChunk++;
                        extractor.advance();
                    }
                } else {
                    Log.d(TAG, "input buffer not available");
                }
            }
            // Assume output is available.  Loop until both assumptions are false.
            boolean decoderOutputAvailable = !decoderDone;
            boolean encoderOutputAvailable = true;
            while (decoderOutputAvailable || encoderOutputAvailable) {
                // Start by draining any pending output from the encoder.  It's important to
                // do this before we try to stuff any more data in.
                int encoderStatus = encoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
                if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    Log.d(TAG, "no output from encoder available");
                    encoderOutputAvailable = false;
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    encoderOutputBuffers = encoder.getOutputBuffers();
                     Log.d(TAG, "encoder output buffers changed");
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = encoder.getOutputFormat();
                    Log.d(TAG, "encoder output format changed: " + newFormat);
                } else if (encoderStatus < 0) {
                    Log.d(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
                } else { // encoderStatus >= 0
                    ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                    if (encodedData == null) {
                        Log.d(TAG, "encoderOutputBuffer " + encoderStatus + " was null");
                    }
                    // Write the data to the output "file".
                    if (info.size != 0) {
                        encodedData.position(info.offset);
                        encodedData.limit(info.offset + info.size);
                        outputData.addChunk(encodedData, info.flags, info.presentationTimeUs);
                        outputCount++;
                        Log.d(TAG, "encoder output " + info.size + " bytes");
                    }
                    outputDone = (info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
                    encoder.releaseOutputBuffer(encoderStatus, false);
                }
                if (encoderStatus != MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // Continue attempts to drain output.
                    continue;
                }

                if (!outputDone) {
                    int decoderStatus = decoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
                    if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        decoderOutputAvailable = false;
                        Log.d(TAG, "no output from decoder available");
                    } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        // not important for us, since we're using Surface
                        //decoderOutputBuffers = decoder.getOutputBuffers();
                        Log.d(TAG, "decoder output buffers changed");
                    } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                        MediaFormat newFormat = decoder.getOutputFormat();
                        Log.d(TAG, "decoder output format changed: " + newFormat);

                    } else if (decoderStatus < 0) {
                        Log.d(TAG, "unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
                    } else { // decoderStatus >= 0
                        Log.d(TAG, "surface decoder given buffer " + decoderStatus +
                                " (size=" + info.size + ")");
                        //encodedData = decoderOutputBuffers[decoderStatus];
                        ByteBuffer encodedData = decoder.getOutputBuffer(decoderStatus);

                        if (encodedData == null) {
                            Log.d(TAG, "decoderOutputBuffer " + decoderStatus + " was null");
                        }
                        // It's usually necessary to adjust the ByteBuffer values to match BufferInfo.
                        encodedData.position(info.offset);
                        encodedData.limit(info.offset + info.size);
                        rawSize += info.size;
                      /*  int inputBufIndex = encoder.dequeueInputBuffer(TIMEOUT_USEC);
                        if(inputBufIndex >= 0) {
                            ByteBuffer decodedData = encoder.getInputBuffer(inputBufIndex);
                            decodedData.put(encodedData);
                            encoder.queueInputBuffer(inputBufIndex, 0, info.size,
                                    info.presentationTimeUs, 0 *//*flags*//*);
                        }*/

                        Log.d(TAG, "OFFSET: " + info.offset);
                        Log.d(TAG, "RAW SIZE: " + rawSize + " LIMIT: " + encodedData.limit());
                        if (info.size == 0)
                            Log.d(TAG, "got empty frame");
                        //videoChunks.addChunk(encodedData,info.flags, info.presentationTimeUs);
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            Log.d(TAG, "output EOS");

                            outputDone = true;
                            decoderOutputAvailable = false;
                            decoderDone = true;
                            encoder.signalEndOfInputStream();
                            continue;
                        }
                        boolean doRender = (info.size != 0);


                        decoder.releaseOutputBuffer(decoderStatus, doRender);

                        if (doRender) {

                            Log.d(TAG, "awaiting decode of frame " + decodeCount);
                            outputSurface.awaitNewImage();
                            outputSurface.drawImage();
                            inputSurface.setPresentationTime(info.presentationTimeUs * 1000);
                            Log.d(TAG, "swapBuffers");
                            inputSurface.swapBuffers();
                            long startWhen = System.nanoTime();
                                //outputSurface.saveFrame(outputFile.toString());
                            frameSaveTime += System.nanoTime() - startWhen;

                            decodeCount++;
                        } else
                            decoder.releaseOutputBuffer(decoderStatus, false);

                        // As soon as we call releaseOutputBuffer, the buffer will be forwarded
                        // to SurfaceTexture to convert to a texture.  The API doesn't guarantee
                        // that the texture will be available before the call returns, so we
                        // need to wait for the onFrameAvailable callback to fire.

                    /*if (doRender) {
                       Log.d(TAG, "awaiting decode of frame " + decodeCount);
                        outputSurface.awaitNewImage();
                        outputSurface.drawImage(true);

                        if (decodeCount < MAX_FRAMES) {
                            File outputFile = new File(FILES_DIR,
                                    String.format("frame-%02d.png", decodeCount));
                            long startWhen = System.nanoTime();
                            outputSurface.saveFrame(outputFile.toString());
                            frameSaveTime += System.nanoTime() - startWhen;
                        }
                        decodeCount++;
                    }*/
                    }

                }
            }
           // outputStream.flush();
          //  outputStream.close();

        }
       // videoChunks.saveToFile(new File(DEBUG_FILE_NAME_BASE));
//        int numSaved = (MAX_FRAMES < decodeCount) ? MAX_FRAMES : decodeCount;
     /*   Log.d(TAG, "Saving " + numSaved + " frames took " +
                (frameSaveTime / numSaved / 1000) + " us per frame");*/
        outputData.saveToFile(new File(DEBUG_FILE_NAME_BASE));
    }
    public void awaitNewImage() {
        final int TIMEOUT_MS = 500;
        synchronized (mFrameSyncObject) {
            while (!mFrameAvailable) {
                try {
                    // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
                    // stalling the test if it doesn't arrive.
                    mFrameSyncObject.wait(TIMEOUT_MS);
                    if (!mFrameAvailable) {
                        // TODO: if "spurious wakeup", continue while loop
                        throw new RuntimeException("Surface frame wait timed out");
                    }
                } catch (InterruptedException ie) {
                    // shouldn't happen
                    throw new RuntimeException(ie);
                }
            }
            mFrameAvailable = false;
        }
        // Latch the data.
        checkGlError("before updateTexImage");
        mSurfaceTexture.updateTexImage();
    }
    /**
     * Draws the data from SurfaceTexture onto the current EGL surface.
     */
    public void drawImage() {
        onDrawFrame(null);
    }

    private static long computePresentationTime(int frameIndex) {
        return 132 + frameIndex * 1000000 / 30;
    }
    private static class VideoChunks {
        private MediaFormat mMediaFormat;
        private ArrayList<byte[]> mChunks = new ArrayList<byte[]>();
        private ArrayList<Integer> mFlags = new ArrayList<Integer>();
        private ArrayList<Long> mTimes = new ArrayList<Long>();
        /**
         * Sets the MediaFormat, for the benefit of a future decoder.
         */
        public void setMediaFormat(MediaFormat format) {
            mMediaFormat = format;
        }
        /**
         * Gets the MediaFormat that was used by the encoder.
         */
        public MediaFormat getMediaFormat() {
            return mMediaFormat;
        }
        /**
         * Adds a new chunk.  Advances buf.position to buf.limit.
         */
        public void addChunk(ByteBuffer buf, int flags, long time) {
            byte[] data = new byte[buf.remaining()];
            buf.get(data);
            mChunks.add(data);
            mFlags.add(flags);
            mTimes.add(time);
        }
        /**
         * Returns the number of chunks currently held.
         */
        public int getNumChunks() {
            return mChunks.size();
        }
        /**
         * Copies the data from chunk N into "dest".  Advances dest.position.
         */
        public void getChunkData(int chunk, ByteBuffer dest) {
            byte[] data = mChunks.get(chunk);
            dest.put(data);
        }
        /**
         * Returns the flags associated with chunk N.
         */
        public int getChunkFlags(int chunk) {
            return mFlags.get(chunk);
        }
        /**
         * Returns the timestamp associated with chunk N.
         */
        public long getChunkTime(int chunk) {
            return mTimes.get(chunk);
        }
        /**
         * Writes the chunks to a file as a contiguous stream.  Useful for debugging.
         */
        public void saveToFile(File file) {
            Log.d(TAG, "saving chunk data to file " + file);
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            try {
                fos = new FileOutputStream(file);
                bos = new BufferedOutputStream(fos);
                fos = null;     // closing bos will also close fos
                int numChunks = getNumChunks();
                for (int i = 0; i < numChunks; i++) {
                    byte[] chunk = mChunks.get(i);
                    bos.write(chunk);
                }
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            } finally {
                try {
                    if (bos != null) {
                        bos.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            }
        }
    }
}
