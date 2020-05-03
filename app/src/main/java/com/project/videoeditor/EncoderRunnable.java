package com.project.videoeditor;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.nio.ByteBuffer;

public class EncoderRunnable implements Runnable {
    private static final String TAG = "EncoderRunnable";
    private  long TIMEOUT_USEC = 0;
    private MediaCodec encoder;
    private boolean isLogDebug = false;
    private MediaMuxer mediaMuxer;
    private MediaCodec.BufferInfo infoVideo = new MediaCodec.BufferInfo();

    private boolean muxerStart = false;
    private boolean outputDone = false;
    private int mixerVideoIndex;

    public EncoderRunnable(MediaCodec encoder, MediaMuxer mediaMuxer,int mixerVideoIndex,long TIMEOUT_USEC, boolean isLogDebug) {
        this.encoder = encoder;
        this.isLogDebug = isLogDebug;
        this.mediaMuxer = mediaMuxer;
        this.TIMEOUT_USEC = TIMEOUT_USEC;
        this.mixerVideoIndex = mixerVideoIndex;
    }

    @Override
    public void run() {
        int encoderStatus = 0;
        while (!outputDone) {
            encoderStatus = encoder.dequeueOutputBuffer(infoVideo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (isLogDebug)
                    Log.d(TAG, "no output from encoder available");
                //encoderOutputAvailable = false;
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                mediaMuxer.start();
                muxerStart = true;
                if (isLogDebug)
                    Log.d(TAG, "encoder output format changed: ");
            } else if (encoderStatus < 0) {
                Log.d(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
            } else { // encoderStatus >= 0

                ByteBuffer encodedData = encoder.getOutputBuffer(encoderStatus);
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
                }
                // Write the data to the output "file".
                if (infoVideo.size != 0) {
                    encodedData.position(infoVideo.offset);
                    encodedData.limit(infoVideo.offset + infoVideo.size);

                    if (!muxerStart) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    mediaMuxer.writeSampleData(mixerVideoIndex, encodedData, infoVideo);
                    if (isLogDebug)
                        Log.d(TAG, "encoder output " + infoVideo.size + " bytes");
                }

                outputDone = (infoVideo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
                encoder.releaseOutputBuffer(encoderStatus, false);
            }
            if (encoderStatus != MediaCodec.INFO_TRY_AGAIN_LATER) {
                // Continue attempts to drain output.
                continue;
            }
        }
    }
}
