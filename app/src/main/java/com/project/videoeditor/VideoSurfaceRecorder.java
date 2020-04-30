package com.project.videoeditor;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static java.lang.Thread.sleep;


public class VideoSurfaceRecorder
{
    private static final String TAG = "VideoSurfaceRecorder";
    MediaCodec decoder = null;
    MediaCodec encoder = null;
    private  OutputSurface outputSurface;
    private  InputSurface inputSurface;
    private MediaMuxer mediaMuxer;

    public void setEditVideoInfo(VideoInfo editVideoInfo) {
        this.editVideoInfo = editVideoInfo;
    }

    private VideoInfo editVideoInfo;
    private boolean isLogDebug = false;
    private int trackIndex;
    MediaExtractor extractor = null;
    private Context context;

    public void setExtractor(MediaExtractor extractor) {
        this.extractor = extractor;
    }




    public VideoSurfaceRecorder(Context context) {
        this.context = context;

    }


    public void setup() throws Exception {
        MediaFormat inputFormat = null;
        MediaFormat outputFormat = null;

        String mime = null;
        String fragmentShader = UtilUri.OpenRawResourcesAsString(context,R.raw.default_state);
        trackIndex = selectTrack(extractor);
        if (trackIndex < 0) {
            throw new RuntimeException("No video track found in ");
        }
        extractor.selectTrack(trackIndex);


        //outputSurface.changeFragmentShader(fragmentShader);
        inputFormat = extractor.getTrackFormat(trackIndex);
        mime = inputFormat.getString(MediaFormat.KEY_MIME);
        outputFormat = MediaFormat.createVideoFormat(mime,Math.toIntExact(this.editVideoInfo.getWidth()), Math.toIntExact(this.editVideoInfo.getHeight()));
        outputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,  MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, (int) (this.editVideoInfo.getBitrate() * 1024 * 8));
        outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, inputFormat.getInteger(MediaFormat.KEY_FRAME_RATE));
        outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,  2);

        encoder = MediaCodec.createEncoderByType(mime);
        encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        inputSurface = new InputSurface(encoder.createInputSurface());
        inputSurface.makeCurrent();
        outputSurface = new OutputSurface();

        decoder = MediaCodec.createDecoderByType(mime);
        decoder.configure(inputFormat,outputSurface.getSurface(), null, 0);
        File folder = UtilUri.CreateFolder(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath() + "/"+"FilteredVideo");
        File newFiltredFile = UtilUri.CreateFileInFolder(folder.getCanonicalPath(),"outputTest.avi");
        mediaMuxer = new MediaMuxer(newFiltredFile.getCanonicalPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

    }
    public void run() throws Exception {
        encoder.start();
        decoder.start();
        doExtract(extractor,trackIndex,decoder,encoder,outputSurface,inputSurface);
    }
    public void release()
    {
        if (outputSurface != null) {
            outputSurface.release();
        }
        if (inputSurface != null) {
            inputSurface.release();
        }
        if (decoder != null) {
            decoder.stop();
            decoder.release();
        }
        if (extractor != null) {
            extractor.release();
        }
        if (mediaMuxer != null) {
            mediaMuxer.stop();
            mediaMuxer.release();
        }
    }
    public void launchApplyFilterToVideo(MediaExtractor extractor,VideoInfo editVideoInfo) throws Exception {
        this.setExtractor(extractor);
        this.setEditVideoInfo(editVideoInfo);
        this.setup();
        this.run();
        this.release();
    }
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
    private void doExtract(MediaExtractor extractor, int trackIndex, MediaCodec decoder,MediaCodec encoder,OutputSurface outputSurface,InputSurface inputSurface) throws Exception {
        final int TIMEOUT_USEC = 10000;
        ByteBuffer[] decoderInputBuffers = decoder.getInputBuffers();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();

        int inputChunk = 0;
        int decodeCount = 0;
        long rawSize = 0;
        int mTrackIndex = 0;
        int outputCount = 0;
        long frameSaveTime = 0;
        ByteBuffer[] decoderOutputBuffers = decoder.getOutputBuffers();
        boolean outputDone = false;
        boolean inputDone = false;
        boolean decoderDone = false;
        boolean muxerStart = false;


        while (!outputDone) {
           Log.d(TAG, "loop");

            // Feed more data to the decoder.
            if (!inputDone) {
                int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                    ByteBuffer inputBuf = decoder.getInputBuffer(inputBufIndex);
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
                     Log.d(TAG, "encoder output buffers changed");
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = encoder.getOutputFormat();
                    if (muxerStart) {
                        throw new RuntimeException("format changed twice");
                    }
                    mTrackIndex = mediaMuxer.addTrack(newFormat);
                    mediaMuxer.start();
                    muxerStart = true;
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
                       // outputData.addChunk(encodedData, info.flags, info.presentationTimeUs);
                        if (!muxerStart) {
                            throw new RuntimeException("muxer hasn't started");
                        }

                        mediaMuxer.writeSampleData(mTrackIndex, encodedData, info);
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
                           // encoder.signalEndOfInputStream();
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
                            frameSaveTime = System.nanoTime() - startWhen;

                            decodeCount++;
                        } else
                            decoder.releaseOutputBuffer(decoderStatus, false);


                    }

                }
            }
        }

    }
}
