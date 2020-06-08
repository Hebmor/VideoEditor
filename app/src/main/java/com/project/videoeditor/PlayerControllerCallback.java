package com.project.videoeditor;

interface PlayerControllerCallback {
    void callingUpdatePlayerControllerPosition(int positionMS);
    int callingAddVideoToPlaylistPlayer(String path);
    void moveVideoByVideoIndex(int index);
}
