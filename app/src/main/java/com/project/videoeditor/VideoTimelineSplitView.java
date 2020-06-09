package com.project.videoeditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.videoeditor.activity.MainEditor;
import com.project.videoeditor.codecs.ActionEditor;
import com.project.videoeditor.support.SupportUtil;

import static com.project.videoeditor.support.SupportUtil.splitBitmap;

public class VideoTimelineSplitView extends LinearLayout implements MainEditor.IResultCallbackTakeVideoInfo{

    private ImageView timelinePointer;
    private RecyclerView timelineBody;
    private TimelineSplitAdapter timelineAdapter;
    private TextView indicatorTimeline;
    private TypedArray a;

    private Button splitButton;
    private Button addVideoToTimelineButton;
    private Button extractFrameButton;

    private int scrollPath = 0;
    private int localScrollPath = 0;
    private int prevMsValue = 0;
    private int scrollRange = 0;
    private int currentItemPosition = 0;
    private int currentVideoIndex = 0;

    private int localMsPath = 0;


    private Bitmap delimiterBitmap;
    private final int MINIMAL_INTERVAL_MS = 1000;
    private int overallXScroll = 0;
    private TimelineEntity currentTimelineEntity;
    private static boolean isDebugMod = true;

    PlayerControllerCallback playerControllerCallback;

    public interface IClickSplitEdit
    {
        void clickExtractFrame(View view, String path, String filename, float frametimeInMs);
        void clickSplit(View view);
        void clickSave(View view);
    }

    private IClickSplitEdit callbackClickEdit;

    public void registerIClickSplitEditCallback(IClickSplitEdit callback)
    {
        this.callbackClickEdit = callback;
    }
    @Override
    public void call(VideoInfo data) {
        currentVideoIndex = playerControllerCallback.callingAddVideoToPlaylistPlayer(data.getPath());
        addProcess(data);
    }

    public void registerCallBack(PlayerControllerCallback playerControllerCallback){
        this.playerControllerCallback = playerControllerCallback;
    }

    public VideoTimelineSplitView(Context context) {
        super(context);
        inflate(context,R.layout.timeline_split,this);

        timelinePointer = findViewById(R.id.timelinePointer);
        timelineBody = findViewById(R.id.timeline_recycler);
        indicatorTimeline = findViewById(R.id.indicatorTimeline);

        init(null);
        registerButton();
    }

    public VideoTimelineSplitView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
         inflate(context,R.layout.timeline_split,this);

        timelinePointer = findViewById(R.id.timelinePointer);
        timelineBody = findViewById(R.id.timeline_recycler);
        indicatorTimeline = findViewById(R.id.indicatorTimeline);
        init(attrs);
        registerButton();
    }
    @SuppressLint("ResourceType")
    private void init(AttributeSet attrs) {

        if(attrs != null) {
            a = getContext().obtainStyledAttributes(
                    attrs, R.styleable.VideoTimelineSplitView);
            Drawable imagePointer = a.getDrawable(R.styleable.VideoTimelineSplitView_timelinePointer);
            timelinePointer.setImageDrawable(imagePointer);
        }

        delimiterBitmap = genDelimiterBitmap(5,90,getResources().getString(R.color.white));
        timelineAdapter = new TimelineSplitAdapter();
        timelineAdapter.setDebugMod(isDebugMod);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        timelineBody.setLayoutManager(layoutManager);
        timelineBody.setAdapter(timelineAdapter);

        MainEditor.registerResultCallbackTakeVideo(this);

        timelineBody.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                {
                    int tempPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                    //Log.d("ZONE Z", String.valueOf(tempPosition));
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int tempPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                int playerTimeValue = 0;
                overallXScroll = overallXScroll + dx;

                if(tempPosition >= 0) {
                    if (tempPosition != currentItemPosition) {

                        if(timelineAdapter.getTimelineEntityByItemIndex(tempPosition).getType() != TimelineEntity.Type.EMPTY)
                                movePlayerToVideoIndex(timelineAdapter.getTimelineEntityByItemIndex(tempPosition).getVideoIndex());
                        if (currentTimelineEntity != null) {

                            if (tempPosition > currentItemPosition) {
                                //scrollPath -= currentTimelineEntity.getWidth();

                                //overallXScroll -= currentTimelineEntity.getWidth();
                            } else if (tempPosition < currentItemPosition) {
                                currentTimelineEntity = timelineAdapter.getTimelineEntityByItemIndex(tempPosition);
                                //scrollPath += currentTimelineEntity.getWidth();

                                //overallXScroll += currentTimelineEntity.getWidth();
                            }
                        }
                        if(timelineAdapter.getTimelineEntityByItemIndex(currentItemPosition).getType() != TimelineEntity.Type.EMPTY &&
                                timelineAdapter.getTimelineEntityByItemIndex(tempPosition).getType() != TimelineEntity.Type.EMPTY ) {
                           // scrollPath -= computeEmplyOffset(currentItemPosition, tempPosition);
                            Log.d("OTN", String.valueOf(1));
                        }

                        currentTimelineEntity = timelineAdapter.getTimelineEntityByItemIndex(tempPosition);
                        currentItemPosition = tempPosition;

                    } else if (currentTimelineEntity == null)
                        currentTimelineEntity = timelineAdapter.getTimelineEntityByItemIndex(tempPosition);
                }

                if (currentTimelineEntity != null) {
                    int value = 0;
                    int minMs = 0;
                    int maxMs = 0;

                    maxMs = currentTimelineEntity.getDurationMs();
                    scrollRange = currentTimelineEntity.getWidth();
                    localScrollPath = Math.abs( overallXScroll - currentTimelineEntity.getBeginDp());
                    scrollPath = localScrollPath;
                    playerTimeValue = Math.round((float) overallXScroll * (float) timelineAdapter.getCommonDurationMs()
                            / (float) getLastEndDp());

                    if(currentTimelineEntity.getAttachedTimelineIndex() != -1)
                    {
                        maxMs+= getAttachedOffsetInMs(currentItemPosition);
                        scrollRange+= getAttachedOffsetInDp(currentItemPosition);
                        scrollPath+= overallXScroll;

                    }
                    playerTimeValue-= currentTimelineEntity.getGlobalBeginMs();
                    playerTimeValue+= currentTimelineEntity.getLocalBeginMs();


                    if (scrollRange > 0) {

                        if (scrollPath == 0) {
                            updateTimeline(minMs);
                        }
                        else {

                            if (scrollPath >= scrollRange)
                                value = maxMs;
                            else {
                                if (currentTimelineEntity != null)
                                    value = Math.round((float) scrollPath * (float) maxMs / (float) scrollRange);
                                else
                                    value = 0;
                            }

                            updateTimeline(value);
                            updatePlayer(playerTimeValue);

                            localMsPath = playerTimeValue;
                        }
                    }

                    if(isDebugMod) {
                        Log.d("onScrolled","Позиция RecyclerView:" + tempPosition);
                        Log.d("onScrolled", "Отклонение по X:" + dx);
                        Log.d("onScrolled", "Пройденный путь по скролу:" + overallXScroll);
                        Log.d("onScrolled", "Временное значение:" + playerTimeValue);
                        Log.d("onScrolled", "Общий путь - локальный путь:" + (overallXScroll - scrollPath));
                        Log.d("onScrolled", "Локальный путь:" + scrollPath);
                    }
                }
            }
        });
    }

    public void addItemInTimelineBody(Bitmap frameCollage, String nameCollage, String videoPath, int widthItem, int heightItem,
                                       int globalBeginMs, int globalEndMs, int localBeginMs, int localEndMs, int beginDp, int endDp, int videoIndex, TimelineEntity.Type type)
    {
        int idx = timelineAdapter.getItemCount();
        TimelineEntity timelineEntity = new TimelineEntity(widthItem,heightItem,nameCollage,globalBeginMs,
                globalEndMs,localBeginMs,localEndMs,beginDp,endDp,videoIndex,type);
        timelineEntity.setPathAttachedVideo(videoPath);
        timelineAdapter.addItem(frameCollage,nameCollage,timelineEntity);
        timelineAdapter.notifyItemInserted(idx);
    }

    public void addItemInTimelineBody(Bitmap frameCollage, String nameCollage, int pos,int widthItem, int heightItem,
                                      int globalBeginMs, int globalEndMs,int localBeginMs, int localEndMs,int beginDp, int endDp, int videoIndex, TimelineEntity.Type type)
    {
        TimelineEntity timelineEntity = new TimelineEntity(widthItem,heightItem,nameCollage,globalBeginMs,globalEndMs,localBeginMs,localEndMs,beginDp,endDp,videoIndex,type);
        timelineAdapter.addItemInPos(frameCollage,nameCollage,timelineEntity,pos);
        timelineAdapter.notifyItemInserted(pos);
    }

    public void addItemInTimelineBody(Bitmap frameCollage, String nameCollage, String pathVideo, int pos,int widthItem, int heightItem,
                                      int globalBeginMs, int globalEndMs,int localBeginMs, int localEndMs,int beginDp, int endDp, int videoIndex, TimelineEntity.Type type)
    {
        TimelineEntity timelineEntity = new TimelineEntity(widthItem,heightItem,nameCollage,globalBeginMs,globalEndMs,localBeginMs,localEndMs,beginDp,endDp,videoIndex,type);
        timelineEntity.setPathAttachedVideo(pathVideo);
        timelineAdapter.addItemInPos(frameCollage,nameCollage,timelineEntity,pos);
        timelineAdapter.notifyItemInserted(pos);
    }

    private void updateTimeline(int value)
    {
        String indicatorText = "";
        if(prevMsValue != value) {
            int millisSBL = value % 1000;
            int secondSBL = (value / 1000) % 60;
            int minuteSBL = (value/ (1000 * 60)) % 60;
            int hourSBL = (value / (1000 * 60 * 60)) % 24;
            prevMsValue = value;
            if(hourSBL > 0)
                indicatorText = String.format("%02d:%02d:%02d.%d",hourSBL,minuteSBL, secondSBL, millisSBL);
            else
                indicatorText = String.format("%02d:%02d.%d",minuteSBL, secondSBL, millisSBL);

            indicatorTimeline.setText(indicatorText);

        }
    }

    private void updatePlayer(int timeStampInMs)
    {
        playerControllerCallback.callingUpdatePlayerControllerPosition(timeStampInMs);
    }

    public void movePlayerToVideoIndex(int videoIndex)
    {
        playerControllerCallback.moveVideoByVideoIndex(videoIndex);
        currentVideoIndex = videoIndex;
    }

    private void registerButton()
    {
        splitButton = findViewById(R.id.buttonSplit);
        addVideoToTimelineButton = findViewById(R.id.buttonAdd);
        extractFrameButton = findViewById(R.id.buttonExtractFrame);

        splitButton.setOnClickListener(this::clickSplit);
        addVideoToTimelineButton.setOnClickListener(this::clickAddVideo);
        extractFrameButton.setOnClickListener(this::clickExtractFrame);
    }

    private void splitProcess()
    {
        if(timelineAdapter.getTimelineEntityByItemIndex(currentItemPosition).getType() != TimelineEntity.Type.EMPTY) {
            int maxMs = timelineAdapter.getTimelineEntityByItemIndex(currentItemPosition).getDurationMs();
            int globalScrollPositionInMS = Math.abs(overallXScroll) * timelineAdapter.getCommonDurationMs() / timelineAdapter.getCommonWidthDp();
            int localScrollPositionInMS = Math.abs(localScrollPath) * maxMs / scrollRange;
            int rangePathInMs = maxMs - localScrollPositionInMS;
            if (localScrollPositionInMS <= timelineAdapter.getCommonDurationMs() - MINIMAL_INTERVAL_MS
                    && localScrollPositionInMS >= MINIMAL_INTERVAL_MS
                    && rangePathInMs >= MINIMAL_INTERVAL_MS) {
                this.splitCollage(Math.abs(overallXScroll),globalScrollPositionInMS,localScrollPath, localScrollPositionInMS);
            } else
                Toast.makeText(getContext(), "Разделяймая часть должна быть больше 1 сек!", Toast.LENGTH_LONG).show();
        }
    }

    private void addProcess(VideoInfo info)
    {
        if(timelineAdapter.getTimelineEntityByItemIndex(currentItemPosition).getType() != TimelineEntity.Type.EMPTY) {

            int commonScrollPositionInMS = Math.abs(overallXScroll) * timelineAdapter.getCommonDurationMs() / timelineAdapter.getCommonWidthDp();
            addCollage(info,Math.abs(overallXScroll),commonScrollPositionInMS);
        }
    }

    private void clickSplit(View view)
    {
        splitProcess();
    }

    public void clickAddVideo(View view)
    {
        try {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            ((Activity)getContext()).startActivityForResult(Intent.createChooser(intent, "Select Video"), MainEditor.REQUEST_TAKE_GALLERY_VIDEO);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void clickExtractFrame(View view)
    {
        callbackClickEdit.clickExtractFrame(view,currentTimelineEntity.getPathAttachedVideo(),currentTimelineEntity.getName(),localMsPath);
    }

    private void splitCollage(int globalSplitPosition,int globalSplitPositionInMs,int localSplitPosition,int localSplitPositionInMS)
    {
        Bitmap collage = timelineAdapter.getBitmapByItemIndex(currentItemPosition);
        String name = timelineAdapter.getNameByItemIndex(currentItemPosition);
        TimelineEntity timelineEntity = timelineAdapter.getTimelineEntityByItemIndex(currentItemPosition);

        Bitmap[] splitPart = splitBitmap(collage,localSplitPosition);

        int globalBeginMs = timelineEntity.getGlobalBeginMs();
        int globalEndMs = timelineEntity.getGlobalEndMs();
        int beginDp = timelineEntity.getBeginDp();
        int endDp = timelineEntity.getEndDp();
        int localBeginMs = timelineEntity.getLocalBeginMs();
        int localEndMs = timelineEntity.getLocalEndMs();
        int width = timelineEntity.getWidth();
        int height = timelineEntity.getHeight();
        int link = timelineEntity.getAttachedTimelineIndex();
        String attachedPath = timelineEntity.getPathAttachedVideo();


        timelineAdapter.removeItemByIndex(currentItemPosition);
        timelineAdapter.notifyItemRangeRemoved(currentItemPosition,1);


        this.addItemInTimelineBody(splitPart[0],name,attachedPath, currentItemPosition, localSplitPosition, height, globalBeginMs,
                globalSplitPositionInMs, localBeginMs, localSplitPositionInMS, beginDp, globalSplitPosition, currentVideoIndex,TimelineEntity.Type.SCROLLABLE);
        this.timelineAdapter.getTimelineEntityByItemIndex(currentItemPosition).setAttachedTimelineIndex(link);

        this.addItemInTimelineBody(delimiterBitmap,"",currentItemPosition + 1,5,height,0,
                0,0,0,0,0,-1,TimelineEntity.Type.EMPTY);

        this.addItemInTimelineBody(splitPart[1],name,attachedPath, currentItemPosition + 2, width - localSplitPosition, height, globalSplitPositionInMs,
                globalEndMs, localSplitPositionInMS, localEndMs, globalSplitPosition, endDp, currentVideoIndex, TimelineEntity.Type.SCROLLABLE);

        scrollPath = 0;
        currentTimelineEntity = null;
        timelineBody.getLayoutManager().scrollToPosition(currentItemPosition + 1);

    }


    private Bitmap genDelimiterBitmap(int widthTimelineInDp, int heightTimelineInDp, String hexColor)
    {
        int widthInPx = SupportUtil.dpToPx(widthTimelineInDp);
        int heightInPx = SupportUtil.dpToPx(heightTimelineInDp);
        int color = Color.parseColor(hexColor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        int colorValue = Color.argb(1,red,green,blue);
        int [] colors = new int[widthInPx * heightInPx];

        for(int i = 0;i < colors.length;i++)
        {
            colors[i] = colorValue;
        }
        return Bitmap.createBitmap(colors,widthInPx,heightInPx, Bitmap.Config.ARGB_8888);
    }

    private int computeEmplyOffset(int idx1,int idx2)
    {
        int temp = 0;
        int offset = 0;
        if(idx1 > idx2)
        {
            temp = idx1;
            idx1 = idx2;
            idx2 = temp;
        }
        if(idx1 == idx2)
            return 0;

        for(int i = idx1;i < idx2;i++)
        {
            TimelineEntity entity  = this.timelineAdapter.getTimelineEntityByItemIndex(i);
            if(entity.getType() == TimelineEntity.Type.EMPTY)
                offset+=entity.getWidth();

        }
        return offset;
    }

    public void addVideoInTimeline(VideoInfo videoInfo)
    {
        Bitmap frameCollage = ActionEditor.getFrameCollage(videoInfo.getPath(),videoInfo.getDuration(),
                160,90,12);
        int offsetX = 0;
        int offsetMs = 0;
        if(timelineAdapter.getItemCount() > 0) {
            offsetX = timelineAdapter.getCommonWidthDp();
            offsetMs = timelineAdapter.getCommonDurationMs();
        }
        this.addItemInTimelineBody(frameCollage, videoInfo.getFilename(), videoInfo.getPath(),
                160 * 12,106,offsetMs,videoInfo.getDuration() + offsetMs,0,videoInfo.getDuration(),
                offsetX,160 * 12 + offsetX,currentVideoIndex,TimelineEntity.Type.SCROLLABLE);
    }

    public void setAttached(int indexTo,int indexFrom)
    {
        this.timelineAdapter.getTimelineEntityByItemIndex(indexFrom).setAttachedTimelineIndex(indexTo);
    }

    public void addCollage(VideoInfo videoInfo,int splitPosition,int scrollPositionInMS)
    {
        Bitmap[] splitPart = null;
        Bitmap collage = timelineAdapter.getBitmapByItemIndex(currentItemPosition);
        Bitmap newCollage = ActionEditor.getFrameCollage(videoInfo.getPath(),videoInfo.getDuration(),
                160,90,12);
        String name = timelineAdapter.getNameByItemIndex(currentItemPosition);
        TimelineEntity timelineEntity = timelineAdapter.getTimelineEntityByItemIndex(currentItemPosition);
        int videoIndex = timelineEntity.getVideoIndex();
        int normalizeSplitPos = localScrollPath;
        if(normalizeSplitPos > timelineEntity.getWidth())
            normalizeSplitPos-= timelineEntity.getBeginDp();

        if(normalizeSplitPos > 0 && !(normalizeSplitPos >= timelineEntity.getWidth()))
            splitPart = splitBitmap(collage,normalizeSplitPos);
        else
        {
            splitPart = new Bitmap[2];
            splitPart[0] = collage;
            splitPart[1] = collage;
        }

        int globalBeginMs = timelineEntity.getGlobalBeginMs();
        int globalEndMs = timelineEntity.getGlobalEndMs();
        int beginDp = timelineEntity.getBeginDp();
        int endDp = timelineEntity.getEndDp();
        int width = timelineEntity.getWidth();
        int height = timelineEntity.getHeight();
        int localBeginMs = timelineEntity.getLocalBeginMs();
        int localEndMs = timelineEntity.getLocalEndMs();
        int addPosition = currentItemPosition;
        int oldCommonWidth = timelineAdapter.getCommonWidthDp();
        String attachedPath = timelineEntity.getPathAttachedVideo();

        timelineAdapter.removeItemByIndex(currentItemPosition);
        timelineAdapter.notifyItemRangeRemoved(currentItemPosition,1);

        if(!(normalizeSplitPos <= 0 && localScrollPath <= 0)) {
            this.addItemInTimelineBody(splitPart[0], name, attachedPath, addPosition++, normalizeSplitPos,
                    height, globalBeginMs, scrollPositionInMS, localBeginMs, scrollPositionInMS, beginDp, splitPosition,
                    videoIndex, TimelineEntity.Type.SCROLLABLE);
        }

        this.addItemInTimelineBody(newCollage, videoInfo.getFilename(), videoInfo.getPath(), addPosition,
                160 * 12,106, scrollPositionInMS, videoInfo.getDuration() + scrollPositionInMS,0,videoInfo.getDuration(),splitPosition,
                160 * 12 + splitPosition, currentVideoIndex, TimelineEntity.Type.SCROLLABLE);

        timelineBody.getLayoutManager().scrollToPosition(addPosition);

        if(currentItemPosition < addPosition)
            setAttached(currentItemPosition ,addPosition);

        if(!(normalizeSplitPos == localScrollPath && localScrollPath >= oldCommonWidth)) {
            this.addItemInTimelineBody(splitPart[1], name, attachedPath, addPosition++ + 1,
                    width - normalizeSplitPos, height, videoInfo.getDuration()
                            + scrollPositionInMS, globalEndMs + videoInfo.getDuration() + scrollPositionInMS,
                    scrollPositionInMS,localEndMs, 160 * 12 + splitPosition, endDp  + 160 * 12  + splitPosition,
                    videoIndex, TimelineEntity.Type.SCROLLABLE);

            setAttached(addPosition - 1 ,addPosition);
        }

        currentTimelineEntity = null;
        //movePlayerToVideoIndex(currentVideoIndex);
        // updateTimeline(0);

    }

    public int getAttachedOffsetInMs(int timelineEntityIndex)
    {
        int attachedOffset = 0;
        int attachedIdx = timelineAdapter
                .getTimelineEntityByItemIndex(timelineEntityIndex)
                .getAttachedTimelineIndex();
        TimelineEntity entity = timelineAdapter.getTimelineEntityByItemIndex(attachedIdx);
        attachedOffset += entity.getGlobalEndMs();

//        while (attachedIdx != -1)
//        {
//            TimelineEntity entity = timelineAdapter.getTimelineEntityByItemIndex(attachedIdx);
//            attachedOffset += entity.getDurationMs();
//            attachedIdx = entity.getAttachedTimelineIndex();
//        }

        return attachedOffset;
    }

    public int getAttachedOffsetInDp(int timelineEntityIndex)
    {
        int attachedOffset = 0;
        int attachedIdx = timelineAdapter
                .getTimelineEntityByItemIndex(timelineEntityIndex)
                .getAttachedTimelineIndex();

        TimelineEntity entity = timelineAdapter.getTimelineEntityByItemIndex(attachedIdx);
        attachedOffset += entity.getEndDp();

//        while (attachedIdx != -1)
//        {
//            TimelineEntity entity = timelineAdapter.getTimelineEntityByItemIndex(attachedIdx);
//            attachedOffset += entity.getEndDp();
//            attachedIdx = entity.getAttachedTimelineIndex();
//        }
        return attachedOffset;
    }

    public TimelineEntity getLastAddElemTimeline()
    {
        int lastIndex = this.timelineAdapter.getItemCount() - 1;
        return this.timelineAdapter.getTimelineEntityByItemIndex(lastIndex);
    }

    public int getLastEndDp()
    {
        return getLastAddElemTimeline().getEndDp();
    }
}

