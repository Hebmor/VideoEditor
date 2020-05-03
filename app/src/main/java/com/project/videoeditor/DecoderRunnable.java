package com.project.videoeditor;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

public class DecoderRunnable implements Runnable {
    private static final String TAG = "DecoderRunnable";
    private  long TIMEOUT_USEC = 0;
    private boolean decoderDone = false;
    private boolean isLogDebug = false;
    private MediaCodec decoder;
    MediaCodec.BufferInfo infoVideo = new MediaCodec.BufferInfo();
    private int decodeCount = 0;
    private OutputSurface outputSurface;
    private InputSurface inputSurface;
    private MediaCodec encoder;

    public DecoderRunnable( OutputSurface outputSurface, InputSurface inputSurface,MediaCodec decoder, MediaCodec encoder,long TIMEOUT_USEC,boolean isLogDebug) {
        this.isLogDebug = isLogDebug;
        this.decoder = decoder;
        this.outputSurface = outputSurface;
        this.inputSurface = inputSurface;
        this.encoder = encoder;
        this.TIMEOUT_USEC = TIMEOUT_USEC;
    }

    @Override
    public void run() {
        int decoderStatus = 0;
        while(!decoderDone) {
            decoderStatus = decoder.dequeueOutputBuffer(infoVideo, TIMEOUT_USEC);
            if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                //decoderOutputAvailable = false;
                if (isLogDebug)
                    Log.d(TAG, "no output from decoder available");
            } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                MediaFormat newFormat = decoder.getOutputFormat();
                if (isLogDebug)
                    Log.d(TAG, "decoder output format changed: " + newFormat);

            } else if (decoderStatus < 0) {
                if (isLogDebug)
                    Log.d(TAG, "unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
            } else { // decoderStatus >= 0
                if (isLogDebug)
                    Log.d(TAG, "surface decoder given buffer " + decoderStatus +
                            " (size=" + infoVideo.size + ")");
                if (infoVideo.size == 0)
                    Log.d(TAG, "got empty frame");
                if ((infoVideo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.d(TAG, "output EOS");
                    decoderDone = true;
                    encoder.signalEndOfInputStream();
                    continue;
                }
                boolean doRender = (infoVideo.size != 0);

                decoder.releaseOutputBuffer(decoderStatus, doRender);
                if (doRender) {
                    if (isLogDebug)
                        Log.d(TAG, "awaiting decode of frame " + decodeCount);
                    outputSurface.awaitNewImage();
                    outputSurface.drawImage();
                    inputSurface.setPresentationTime(infoVideo.presentationTimeUs * 1000);
                    if (isLogDebug)
                        Log.d(TAG, "swapBuffers");
                    inputSurface.swapBuffers();
                    decodeCount++;
                } else
                    decoder.releaseOutputBuffer(decoderStatus, false);

            }
        }
    }
}
