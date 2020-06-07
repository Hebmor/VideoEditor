package com.project.videoeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.View;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;

import static com.google.android.exoplayer2.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING;

public class PlayerController implements Serializable {

    private PlayerView playerView;
    private SimpleExoPlayer player;
    private PlayerControlView playerControlView;
    private Context context;
    private boolean playWhenReady = false;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private ArrayList<Uri> videos;
    private ConcatenatingMediaSource playlist;

    public PlayerView getPlayerView() {
        return playerView;
    }
    public SimpleExoPlayer getPlayer() {
        return player;
    }
    public PlayerControlView getPlayerControlView() {
        return playerControlView;
    }

    public PlayerController(Context context) {
        this.context = context;
        this.playerView = new PlayerView(context);

    }
    public PlayerController(Context context,String path) {
        this.context = context;
        this.playerView = new PlayerView(context);
        this.playerControlView = new PlayerControlView(context);
        this.videos = new ArrayList<>();
        this.playlist = new ConcatenatingMediaSource();
        this.addPlaylistByPath(path);
    }
    public void initializePlayer() {
        this.player = ExoPlayerFactory.newSimpleInstance(context);
        this.playerView.setPlayer(player);
        this.playerControlView.setPlayer(player);
        this.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        this.player.setVideoScalingMode(VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.prepare(playlist, true, false);

    }
    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(context, "exoplayer-codelab");
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
    }
    public void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }
    @SuppressLint("InlinedApi")
    public void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    public void addPlaylistByPath(String path)
    {
        MediaSource mediaSource =
                new ProgressiveMediaSource.Factory(new FileDataSourceFactory())
                        .setTag(path)
                        .createMediaSource(Uri.parse(path));
        playlist.addMediaSource(mediaSource);
        videos.add(Uri.parse(path));
    }

    public void addPlaylistByPath(Uri path)
    {
        MediaSource mediaSource =
                new ProgressiveMediaSource.Factory(new FileDataSourceFactory())
                        .setTag(path)
                        .createMediaSource(path);
        playlist.addMediaSource(mediaSource);
        videos.add(path);
    }

    public void removePlaylistByIndex(int index)
    {
        playlist.removeMediaSource(index);
    }

    public void seekTo(int positionMs)
    {
        player.seekTo(positionMs);
    }

    public void moveNextVideo(int beginPositionInMs)
    {
        if(player.hasNext()) {
            int nextIndex = player.getNextWindowIndex();
            player.seekTo(nextIndex,beginPositionInMs);
        }
    }
    public void movePrevVideo(int endPositionMs)
    {
        if(player.hasPrevious()) {
            int nextIndex = player.getPreviousWindowIndex();
            player.seekTo(nextIndex,endPositionMs);
        }
    }
}
