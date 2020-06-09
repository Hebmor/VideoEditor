package com.project.videoeditor;

import android.util.Log;

public class TimelineEntity {

    public enum Type
    {
        SCROLLABLE,
        EMPTY
    };

    private int width = 0;
    private int height = 0;
    private String name = "";
    private int globalBeginMs = 0;
    private int globalEndMs = 0;
    private int durationMs = 0;
    private int beginDp = 0;
    private int endDp = 0;
    private int attachedTimelineIndex = -1;
    private int videoIndex = 0;

    private int localBeginMs = 0;
    private int localEndMs = 0;

    private String pathAttachedVideo = "";
    private Type type;

    public TimelineEntity(int width, int height, String name, int globalBeginMs, int globalEndMs,
                          Type type) {
        this.width = width;
        this.height = height;
        this.name = name;
        this.globalBeginMs = globalBeginMs;
        this.globalEndMs = globalEndMs;
        this.durationMs = this.globalEndMs - this.globalBeginMs;
        this.type = type;
    }

    public TimelineEntity(int width, int height, String name, int globalBeginMs, int globalEndMs,
                          int beginDp, int endDp, Type type) {
        this.width = width;
        this.height = height;
        this.name = name;
        this.globalBeginMs = globalBeginMs;
        this.globalEndMs = globalEndMs;
        this.beginDp = beginDp;
        this.endDp = endDp;
        this.durationMs = this.globalEndMs - this.globalBeginMs;
        this.type = type;
    }

    public TimelineEntity(int width, int height, String name, int globalBeginMs, int globalEndMs,
                          int localBeginMs, int localEndMs, int beginDp, int endDp, int videoIndex, Type type) {
        this.width = width;
        this.height = height;
        this.name = name;
        this.globalBeginMs = globalBeginMs;
        this.globalEndMs = globalEndMs;
        this.localBeginMs = localBeginMs;
        this.localEndMs = localEndMs;
        this.beginDp = beginDp;
        this.endDp = endDp;
        this.videoIndex = videoIndex;
        this.type = type;
        this.durationMs = this.globalEndMs - this.globalBeginMs;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGlobalBeginMs() {
        return globalBeginMs;
    }

    public void setGlobalBeginMs(int globalBeginMs) {
        this.globalBeginMs = globalBeginMs;
    }

    public int getGlobalEndMs() {
        return globalEndMs;
    }

    public void setGlobalEndMs(int globalEndMs) {
        this.globalEndMs = globalEndMs;
    }


    public int getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(int durationMs) {
        this.durationMs = durationMs;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getBeginDp() {
        return beginDp;
    }

    public void setBeginDp(int beginDp) {
        this.beginDp = beginDp;
    }

    public int getEndDp() {
        return endDp;
    }

    public void setEndDp(int endDp) {
        this.endDp = endDp;
    }

    public void printDebugInfo()
    {
        Log.d("printDebugInfo", "Элемент:   ");
        Log.d("printDebugInfo", "Ширина:    " + (width));
        Log.d("printDebugInfo", "Начало в MS:   " + globalBeginMs);
        Log.d("printDebugInfo", "Конец в MS:    " + globalEndMs);
        Log.d("printDebugInfo", "Начало в DP:   " + beginDp);
        Log.d("printDebugInfo", "Конец в DP:    " + endDp);
    }

    public int getAttachedTimelineIndex() {
        return attachedTimelineIndex;
    }

    public void setAttachedTimelineIndex(int attachedTimelineIndex) {
        this.attachedTimelineIndex = attachedTimelineIndex;
    }

    public int getVideoIndex() {
        return videoIndex;
    }

    public void setVideoIndex(int videoIndex) {
        this.videoIndex = videoIndex;
    }

    public void setLocalTime(int localBeginMs,int localEndMs)
    {
        this.localBeginMs = localBeginMs;
        this.localEndMs = localEndMs;
    }

    public int getLocalBeginMs() {
        return localBeginMs;
    }

    public void setLocalBeginMs(int localBeginMs) {
        this.localBeginMs = localBeginMs;
    }

    public int getLocalEndMs() {
        return localEndMs;
    }

    public void setLocalEndMs(int localEndMs) {
        this.localEndMs = localEndMs;
    }

    public String getPathAttachedVideo() {
        return pathAttachedVideo;
    }

    public void setPathAttachedVideo(String pathAttachedVideo) {
        this.pathAttachedVideo = pathAttachedVideo;
    }
}
