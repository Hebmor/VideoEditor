package com.project.videoeditor;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.nio.ByteBuffer;

import static java.lang.Thread.sleep;


public class FilterExecutor {
    private static final String TAG = "FilterThread_DEBUG";
    MediaCodec decoder = null;
    MediaCodec encoder = null;
    private OutputSurface outputSurface;
    private InputSurface inputSurface;
    private MediaMuxer mediaMuxer;
    private Long bitrateBitPerSeconds;
    private int mixerVideoIndex;
    private int mixerAudioIndex;

    public void setEditVideoInfo(VideoInfo editVideoInfo) {
        this.editVideoInfo = editVideoInfo;
    }

    private VideoInfo editVideoInfo;
    private boolean isLogDebug = false;
    private int trackIndexVideo;
    private int trackIndexAudio;
    MediaExtractor videoExtractor = null;
    MediaExtractor audioExtractor = null;
    private Context context;
    private boolean isSetup = false;
    String pathToVideo;
    String pathFromVideo;
    MediaFormat inputAudioFormat = null;
    public void setVideoExtractor(MediaExtractor videoExtractor) {
        this.videoExtractor = videoExtractor;
    }


    public FilterExecutor(Context context) {
        this.context = context;

    }


    public void setup() throws Exception {
        MediaFormat inputVideoFormat = null;

        MediaFormat outputFormat = null;

        String mime = null;
        String fragmentShader = UtilUri.OpenRawResourcesAsString(context, R.raw.black_and_white);
        videoExtractor = new MediaExtractor();
        audioExtractor = new MediaExtractor();

        videoExtractor.setDataSource(pathFromVideo);
        audioExtractor.setDataSource(pathFromVideo);

        trackIndexVideo = selectTrack(videoExtractor,"video/");
        if (trackIndexVideo < 0) {
            throw new RuntimeException("No video track found in ");
        }
        trackIndexAudio = selectTrack(audioExtractor,"audio/");
        if (trackIndexAudio < 0) {
            throw new RuntimeException("No audio track found in ");
        }

        videoExtractor.selectTrack(trackIndexVideo);
        audioExtractor.selectTrack(trackIndexAudio);

        inputVideoFormat = videoExtractor.getTrackFormat(trackIndexVideo);
        inputAudioFormat = audioExtractor.getTrackFormat(trackIndexAudio);

        mime = inputVideoFormat.getString(MediaFormat.KEY_MIME);
        outputFormat = MediaFormat.createVideoFormat(mime, inputVideoFormat.getInteger(MediaFormat.KEY_WIDTH), inputVideoFormat.getInteger(MediaFormat.KEY_HEIGHT));
        outputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, Math.toIntExact(bitrateBitPerSeconds));
        outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, inputVideoFormat.getInteger(MediaFormat.KEY_FRAME_RATE));
        outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);

        encoder = MediaCodec.createEncoderByType(mime);
        encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        inputSurface = new InputSurface(encoder.createInputSurface());
        inputSurface.makeCurrent();
        outputSurface = new OutputSurface();
        outputSurface.changeFragmentShader(fragmentShader);
        decoder = MediaCodec.createDecoderByType(mime);
        decoder.configure(inputVideoFormat, outputSurface.getSurface(), null, 0);
        File folder = UtilUri.CreateFolder(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath() + "/" + "FilteredVideo");
        File newFiltredFile = UtilUri.CreateFileInFolder(folder.getCanonicalPath(), "outputTest.mp4");
        this.pathToVideo = newFiltredFile.getCanonicalPath();
        mediaMuxer = new MediaMuxer(newFiltredFile.getCanonicalPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        //mixerVideoIndex =  mediaMuxer.addTrack(inputVideoFormat);
       // mixerAudioIndex = mediaMuxer.addTrack(inputAudioFormat);
    }

    public void startFiltered() throws Exception {
        encoder.start();
        decoder.start();
        doExtract(videoExtractor,audioExtractor, trackIndexVideo, trackIndexAudio,decoder, encoder, outputSurface, inputSurface);

    }

    public void release() {
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
        if (videoExtractor != null) {
            videoExtractor.release();
        }
        if (mediaMuxer != null) {
            mediaMuxer.stop();
            mediaMuxer.release();
        }
    }

    public void launchApplyFilterToVideo() throws Exception {

        if(isSetup) {
            this.setup();
            this.startFiltered();
            this.release();
            //ActionEditor.addAudioFromVideoToVideo(pathFromVideo,pathToVideo);
        }
        else
            throw new RuntimeException("Наложение фильтра не возможно! Вызовите метод - setupSettings(...)");
    }

    private int selectTrack(MediaExtractor extractor,String trackPrefix) {
        // Select the first video track we find, ignore the rest.
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith(trackPrefix)) {
                if(isLogDebug)
                    Log.d(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                return i;
            }

        }
        return -1;
    }

    private void doExtract(MediaExtractor videoExtractor,MediaExtractor audioExtractor, int trackIndexVideo,int trackIndexAudio, MediaCodec decoder, MediaCodec encoder, OutputSurface outputSurface, InputSurface inputSurface) throws Exception {
        final int TIMEOUT_USEC = 1000;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int inputChunk = 0;
        int decodeCount = 0;
        long rawSize = 0;
        int mTrackIndex = 0;

        boolean outputDone = false;
        boolean inputDone = false;
        boolean decoderDone = false;
        boolean muxerStart = false;
        int maxChunkSize = 1024 * 1024;

        while (!outputDone) {
            if(isLogDebug)
                Log.d(TAG, "loop");

            if (!inputDone) {
                int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                    ByteBuffer inputBuf = decoder.getInputBuffer(inputBufIndex);
                    int chunkSize = videoExtractor.readSampleData(inputBuf, 0);

                    if (chunkSize < 0) {
                        decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputDone = true;
                        if(isLogDebug)
                            Log.d(TAG, "sent input EOS");
                    } else {
                        if (videoExtractor.getSampleTrackIndex() != trackIndexVideo) {
                            if(isLogDebug)
                                Log.w(TAG, "WEIRD: got sample from track " +
                                    videoExtractor.getSampleTrackIndex() + ", expected " + trackIndexVideo);
                        }
                        long presentationTimeUs = videoExtractor.getSampleTime();
                        decoder.queueInputBuffer(inputBufIndex, 0, chunkSize, presentationTimeUs, 0 /*flags*/);
                        {
                            if(isLogDebug)
                                Log.d(TAG, "submitted frame " + inputChunk + " to dec, size=" +
                                    chunkSize);
                        }
                        inputChunk++;
                        videoExtractor.advance();
                    }
                } else {
                    if(isLogDebug)
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
                    if(isLogDebug)
                        Log.d(TAG, "no output from encoder available");
                    encoderOutputAvailable = false;
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = encoder.getOutputFormat();
                    if (muxerStart) {
                        throw new RuntimeException("format changed twice");
                    }
                    mTrackIndex = mediaMuxer.addTrack(newFormat);
                    mixerAudioIndex = mediaMuxer.addTrack(inputAudioFormat);
                    mediaMuxer.start();
                    muxerStart = true;
                    if(isLogDebug)
                        Log.d(TAG, "encoder output format changed: " + newFormat);
                } else if (encoderStatus < 0) {
                    Log.d(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
                } else { // encoderStatus >= 0
                    ByteBuffer encodedData = encoder.getOutputBuffer(encoderStatus);
                    if (encodedData == null) {
                        throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
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
                        if(isLogDebug)
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
                        if (isLogDebug)
                            Log.d(TAG, "no output from decoder available");
                    } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                        MediaFormat newFormat = decoder.getOutputFormat();
                        if(isLogDebug)
                            Log.d(TAG, "decoder output format changed: " + newFormat);

                    } else if (decoderStatus < 0) {
                        if(isLogDebug)
                            Log.d(TAG, "unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
                    } else { // decoderStatus >= 0
                        if(isLogDebug)
                        Log.d(TAG, "surface decoder given buffer " + decoderStatus +
                                " (size=" + info.size + ")");
                        if (info.size == 0)
                            Log.d(TAG, "got empty frame");
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
                            if(isLogDebug)
                                Log.d(TAG, "awaiting decode of frame " + decodeCount);
                            outputSurface.awaitNewImage();
                            outputSurface.drawImage();
                            inputSurface.setPresentationTime(info.presentationTimeUs * 1000);
                            if(isLogDebug)
                                Log.d(TAG, "swapBuffers");
                            inputSurface.swapBuffers();
                            decodeCount++;
                        } else
                            decoder.releaseOutputBuffer(decoderStatus, false);

                    }
                }
            }
        }
        // Copy audio
        ByteBuffer inputBuf2 = ByteBuffer.allocate(maxChunkSize);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (true) {
            int chunkSize = audioExtractor.readSampleData(inputBuf2, 0);

            if (chunkSize >= 0) {
                bufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                bufferInfo.flags = audioExtractor.getSampleFlags();
                bufferInfo.size = chunkSize;

                mediaMuxer.writeSampleData(trackIndexAudio, inputBuf2, bufferInfo);
                audioExtractor.advance();
            } else {
                break;
            }
        }
    }
    public void setupSettings(MediaExtractor extractor,Long bitrateBitPerSeconds,String pathFromVideo)
    {
        this.videoExtractor = extractor;
        this.pathFromVideo = pathFromVideo;
        this.bitrateBitPerSeconds = bitrateBitPerSeconds;
        this.isSetup = true;
    }
 /*   @Override
    public void run() {
        super.run();
        if(this.extractor == null)
            //throw new Exception("MediaExtractor не инициализирован!");
            if(this.editVideoInfo == null)

        //launchApplyFilterToVideo(extractor,);

    }*/
}
