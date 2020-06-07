package com.project.videoeditor;

interface PlayerControllerCallback {
    void callingUpdatePlayerControllerPosition(int positionMS);
    void callingAddVideoToPlaylistPlayer(String path);
    void callingMoveNextVideo(int beginPositionInMs);
    void callingMovePrevVideo(int endPositionMs);
}
