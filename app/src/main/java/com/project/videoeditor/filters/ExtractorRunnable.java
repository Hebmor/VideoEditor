package com.project.videoeditor.filters;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.util.Log;

import java.nio.ByteBuffer;

public class ExtractorRunnable implements Runnable {
    private static final String TAG = "ExtractorRunnable";
    private MediaCodec decoder;
    private MediaExtractor videoExtractor;
    private boolean isLogDebug;
    boolean inputVideoDone = false;
    int trackIndexVideo = 0;
    private long TIMEOUT_USEC = 0;
    private int extractChunkCount = 0;

    public ExtractorRunnable(MediaCodec decoder, MediaExtractor videoExtractor, boolean isLogDebug, int trackIndexVideo, long TIMEOUT_USEC) {
        this.decoder = decoder;
        this.videoExtractor = videoExtractor;
        this.isLogDebug = isLogDebug;
        this.trackIndexVideo = trackIndexVideo;
        this.TIMEOUT_USEC = TIMEOUT_USEC;
    }


    private boolean extractChunksToDecoder(int inputBufIndex)
    {
        if (inputBufIndex >= 0) {
            ByteBuffer inputBuf = decoder.getInputBuffer(inputBufIndex);
            int chunkSize = videoExtractor.readSampleData(inputBuf, 0);

            if (chunkSize < 0) {
                decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                if(isLogDebug)
                    Log.d(TAG, "sent input EOS");
                return true;
            } else {
                if (videoExtractor.getSampleTrackIndex() != trackIndexVideo) {
                    if(isLogDebug)
                        Log.w(TAG, "WEIRD: got sample from track " +
                                videoExtractor.getSampleTrackIndex() + ", expected " + trackIndexVideo);
                }
                long presentationTimeUs = videoExtractor.getSampleTime();
                decoder.queueInputBuffer(inputBufIndex, 0, chunkSize, presentationTimeUs, 0 /*flags*/);
                videoExtractor.advance();
                if (isLogDebug)
                    Log.d(TAG, "Кадр: " + extractChunkCount++ + " извлечен!");

            }
        } else {
            if(isLogDebug)
                Log.d(TAG, "input buffer not available");
        }
        return false;
    }
    @Override
    public void run() {
        int inputBufIndex = 0;
        while (!inputVideoDone) {
            inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
            inputVideoDone = extractChunksToDecoder(inputBufIndex);
        }
    }


}
