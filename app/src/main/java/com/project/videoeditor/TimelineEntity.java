package com.project.videoeditor;

public class TimelineEntity {

    private int width = 0;
    private int height = 0;
    private String name = "";
    private int id = 0;
    private int beginMs = 0;
    private int endMs = 0;
    private int durationMs = 0;
    private int beginDp = 0;
    private int endDp = 0;

    public enum Type
    {
        SCROLLABLE,
        EMPTY
    };

    private Type type;

    public TimelineEntity(int width, int height, String name, int id, int beginMs, int endMs,Type type) {
        this.width = width;
        this.height = height;
        this.name = name;
        this.id = id;
        this.beginMs = beginMs;
        this.endMs = endMs;
        this.durationMs = this.endMs - this.beginMs;
        this.type = type;
    }

    public TimelineEntity(int width, int height, String name, int id, int beginMs, int endMs, int beginDp, int endDp, Type type) {
        this.width = width;
        this.height = height;
        this.name = name;
        this.id = id;
        this.beginMs = beginMs;
        this.endMs = endMs;
        this.beginDp = beginDp;
        this.endDp = endDp;
        this.durationMs = this.endMs - this.beginMs;
        this.type = type;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBeginMs() {
        return beginMs;
    }

    public void setBeginMs(int beginMs) {
        this.beginMs = beginMs;
    }

    public int getEndMs() {
        return endMs;
    }

    public void setEndMs(int endMs) {
        this.endMs = endMs;
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

}
