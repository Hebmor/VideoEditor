package com.project.videoeditor.filters;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import com.project.videoeditor.R;
import com.project.videoeditor.support.UtilUri;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static java.lang.Thread.sleep;


public class FilterExecutor extends Thread {
    private static final String TAG = "FilterThread_DEBUG";
    MediaCodec decoder = null;
    MediaCodec encoder = null;

    private OutputSurface outputSurface;
    private InputSurface inputSurface;
    private MediaMuxer mediaMuxer;
    private Long bitrateBitPerSeconds;


    private boolean isLogDebug = false;
    private int trackIndexVideo;
    private int trackIndexAudio;

    MediaExtractor videoExtractor = null;
    MediaExtractor audioExtractor = null;

    private Context context;

    private boolean isSetup = false;
    private boolean noSoundFlag = false;
    String pathOutVideoFile;
    String pathInVideoFile;
    MediaFormat inputAudioFormat = null;
    private BaseFilters filter;
    private String newFilename;
    private int framerate;

    private Thread extractorThread;

    public void setVideoExtractor(MediaExtractor videoExtractor) {
        this.videoExtractor = videoExtractor;
    }


    public FilterExecutor(Context context) {
        this.context = context;

    }


    public void setup() throws Exception {
        MediaFormat inputVideoFormat = null;
        MediaFormat outputVideoFormat = null;

        String mime = null;
        String fragmentShader = UtilUri.OpenRawResourcesAsString(context, R.raw.black_and_white);
        videoExtractor = new MediaExtractor();

        audioExtractor = new MediaExtractor();

        videoExtractor.setDataSource(pathInVideoFile);
        audioExtractor.setDataSource(pathInVideoFile);

        trackIndexVideo = selectTrack(videoExtractor,"video/");
        if (trackIndexVideo < 0) {
            throw new RuntimeException("No video track found in ");
        }

        trackIndexAudio = selectTrack(audioExtractor,"audio/");
        if (trackIndexAudio < 0) {
            noSoundFlag = true;
        }

        inputVideoFormat = videoExtractor.getTrackFormat(trackIndexVideo);
        videoExtractor.selectTrack(trackIndexVideo);

        if(!noSoundFlag) {
            audioExtractor.selectTrack(trackIndexAudio);
            inputAudioFormat = audioExtractor.getTrackFormat(trackIndexAudio);
        }

        mime = inputVideoFormat.getString(MediaFormat.KEY_MIME);

        outputVideoFormat = MediaFormat.createVideoFormat(mime, inputVideoFormat.getInteger(MediaFormat.KEY_WIDTH), inputVideoFormat.getInteger(MediaFormat.KEY_HEIGHT));
        outputVideoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        outputVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE, Math.toIntExact(bitrateBitPerSeconds));
        outputVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE,framerate);
        outputVideoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
        outputVideoFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);

        encoder = MediaCodec.createEncoderByType(mime);
        encoder.configure(outputVideoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        inputSurface = new InputSurface(encoder.createInputSurface());
        inputSurface.makeCurrent();
        outputSurface = new OutputSurface(filter);
       // outputSurface.changeFragmentShader(fragmentShader);
        decoder = MediaCodec.createDecoderByType(mime);
        decoder.configure(inputVideoFormat, outputSurface.getSurface(), null, 0);

        File folder = UtilUri.CreateFolder(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath() + "/" + "FilteredVideo");
        File newFiltredFile = UtilUri.CreateFileInFolder(folder.getCanonicalPath(), newFilename);
        this.pathOutVideoFile = newFiltredFile.getCanonicalPath();
        int outputFormat = getMediaMixerOutputFormatByMimeType(mime);
        mediaMuxer = new MediaMuxer(newFiltredFile.getCanonicalPath(), outputFormat);
    }
    private int getMediaMixerOutputFormatByMimeType(String mime)
    {
        switch (mime)
        {
            case MediaFormat.MIMETYPE_VIDEO_VP9:
            case MediaFormat.MIMETYPE_VIDEO_VP8:
                return MediaMuxer.OutputFormat.MUXER_OUTPUT_WEBM;
            default:
                return MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4;
        }
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
        final int maxChunkSize = 1024 * 1024;

        MediaCodec.BufferInfo infoVideo = new MediaCodec.BufferInfo();
        MediaCodec.BufferInfo infoAudio = new MediaCodec.BufferInfo();

        int extractChunkCount = 0;
        int decodeCount = 0;

        int mixerVideoIndex = 0;
        int mixerAudioIndex = 0;

        boolean outputDone = false;

        boolean decoderDone = false;
        boolean muxerStart = false;
        boolean inputAudioDone = false;
        boolean decoderOutputAvailable = false;
        boolean encoderOutputAvailable = false;

        extractorThread = new Thread(new ExtractorRunnable(decoder,videoExtractor,isLogDebug,trackIndexVideo,1));
        long beginTime = System.currentTimeMillis();

        extractorThread.start();

        while (!outputDone) {
            if (isLogDebug)
                Log.d(TAG, "loop");


            // Assume output is available.  Loop until both assumptions are false.
            decoderOutputAvailable = !decoderDone;
            encoderOutputAvailable = true;

            while (decoderOutputAvailable || encoderOutputAvailable) {
                // Start by draining any pending output from the encoder.  It's important to
                // do this before we try to stuff any more data in.
                int encoderStatus = encoder.dequeueOutputBuffer(infoVideo, TIMEOUT_USEC);
                if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    if (isLogDebug)
                        Log.d(TAG, "no output from encoder available");
                    encoderOutputAvailable = false;
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                    MediaFormat newFormat = encoder.getOutputFormat();
                    if (muxerStart) {
                        throw new RuntimeException("format changed twice");
                    }

                    mixerVideoIndex = mediaMuxer.addTrack(newFormat);
                    if (!noSoundFlag)
                        mixerAudioIndex = mediaMuxer.addTrack(inputAudioFormat);
                    mediaMuxer.start();
                    muxerStart = true;

                    if (isLogDebug)
                        Log.d(TAG, "encoder output format changed: " + newFormat);
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
                        // outputData.addChunk(encodedData, info.flags, info.presentationTimeUs);
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

                if (!decoderDone) {
                    int decoderStatus = decoder.dequeueOutputBuffer(infoVideo, TIMEOUT_USEC);
                    if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        decoderOutputAvailable = false;
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

                            //outputDone = true;
                            decoderOutputAvailable = false;
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
        extractorThread.join();
        // Copy audio
        if (!noSoundFlag) {
            ByteBuffer inputBuf = ByteBuffer.allocate(maxChunkSize);
            while (!inputAudioDone) {
                int chunkSize = audioExtractor.readSampleData(inputBuf, 0);

                if (chunkSize >= 0) {
                    infoAudio.presentationTimeUs = audioExtractor.getSampleTime();
                    infoAudio.flags = audioExtractor.getSampleFlags();
                    infoAudio.size = chunkSize;

                    mediaMuxer.writeSampleData(mixerAudioIndex, inputBuf, infoAudio);
                    audioExtractor.advance();
                } else {
                    inputAudioDone = true;
                }
            }
        }

        Log.d("Время выполнения фильтрации: ",(System.currentTimeMillis() - beginTime) + " MS");
    }
    public void setupSettings(MediaExtractor extractor,Long bitrateBitPerSeconds,String pathFromVideo,int framerate,BaseFilters filter) throws IOException {
        this.videoExtractor = extractor;
        this.pathInVideoFile = pathFromVideo;
        this.bitrateBitPerSeconds = bitrateBitPerSeconds;
        String filename = new File(pathFromVideo).getName();
        int idx = filename.lastIndexOf(".");
        this.newFilename = filename.substring(0,idx) + "_" + filter.getFilterName()+filename.substring(idx);
        this.filter = filter;
        this.isSetup = true;
        this.framerate = framerate;
    }


    @Override
    public void run() {
        super.run();
        try {
            launchApplyFilterToVideo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
