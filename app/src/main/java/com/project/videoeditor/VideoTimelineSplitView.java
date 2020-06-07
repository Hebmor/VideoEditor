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

    private int scrollPath = 0;
    private int prevMsValue = 0;
    private int scrollRange = 0;
    private int currentItemPosition = 0;
    private int currentVideoIndex = 0;


    private Bitmap delimiterBitmap;
    private final int MINIMAL_INTERVAL_MS = 1000;
    private int overallXScroll = 0;
    private TimelineEntity currentTimelineEntity;

    PlayerControllerCallback playerControllerCallback;

    private static boolean isDebugMod = true;

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
        splitButton = findViewById(R.id.buttonSplit);
        addVideoToTimelineButton = findViewById(R.id.buttonAdd);
        init(null);
        registerButtonListener();
    }

    public VideoTimelineSplitView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
         inflate(context,R.layout.timeline_split,this);

        timelinePointer = findViewById(R.id.timelinePointer);
        timelineBody = findViewById(R.id.timeline_recycler);
        indicatorTimeline = findViewById(R.id.indicatorTimeline);
        init(attrs);
        registerButtonListener();
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


                        if (currentTimelineEntity != null)
                            if (tempPosition > currentItemPosition) {
                                scrollPath -= currentTimelineEntity.getWidth();
                                moveNextVideo(0);
                                //overallXScroll -= currentTimelineEntity.getWidth();
                            }
                            else if (tempPosition < currentItemPosition) {
                                currentTimelineEntity = timelineAdapter.getTimelineEntityByItemIndex(tempPosition);
                                scrollPath += currentTimelineEntity.getWidth();
                                movePrevVideo(0);
                                //overallXScroll += currentTimelineEntity.getWidth();
                            }

                        if(timelineAdapter.getTimelineEntityByItemIndex(currentItemPosition).getType() != TimelineEntity.Type.EMPTY &&
                                timelineAdapter.getTimelineEntityByItemIndex(tempPosition).getType() != TimelineEntity.Type.EMPTY ) {
                            scrollPath -= computeEmplyOffset(currentItemPosition, tempPosition);
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
                    scrollPath = Math.abs( overallXScroll - currentTimelineEntity.getBeginDp());
                    if(currentTimelineEntity.getAttachedTimelineIndex() != -1)
                    {
                        maxMs+= getAttachedOffsetInMs(currentItemPosition);
                        scrollRange+= getAttachedOffsetInDp(currentItemPosition);
                        scrollPath = overallXScroll;
                    }


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
                            playerTimeValue = Math.round((float) overallXScroll * (float) timelineAdapter.getCommonDurationMs()
                                    / (float) timelineAdapter.getCommonWidthDp());
                            updatePlayer(playerTimeValue);
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

    public void addItemInTimelineBody(Bitmap frameCollage, String nameCollage, int widthItem, int heightItem,
                                      int beginMs, int endMs, int beginDp, int endDp, int videoIndex, TimelineEntity.Type type)
    {
        int idx = timelineAdapter.getItemCount();
        TimelineEntity timelineEntity = new TimelineEntity(widthItem,heightItem,nameCollage,idx,beginMs,endMs,beginDp,endDp,videoIndex,type);
        timelineAdapter.addItem(frameCollage,nameCollage,timelineEntity);
        timelineAdapter.notifyItemInserted(idx);
    }

    public void addItemInTimelineBody(Bitmap frameCollage, String nameCollage, int pos,int widthItem, int heightItem,
                                      int beginMs, int endMs, int beginDp, int endDp, int videoIndex, TimelineEntity.Type type)
    {
        TimelineEntity timelineEntity = new TimelineEntity(widthItem,heightItem,nameCollage,pos,beginMs,endMs,beginDp,endDp,videoIndex,type);
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
    private void moveNextVideo(int timeStampInMs)
    {
        playerControllerCallback.callingMoveNextVideo(timeStampInMs);
    }
    private void movePrevVideo(int timeStampInMs)
    {
        playerControllerCallback.callingMovePrevVideo(timeStampInMs);
    }
    private void registerButtonListener()
    {
        splitButton.setOnClickListener(this::clickSplit);
        addVideoToTimelineButton.setOnClickListener(this::clickAddVideo);
    }

    private void splitProcess()
    {
        if(timelineAdapter.getTimelineEntityByItemIndex(currentItemPosition).getType() != TimelineEntity.Type.EMPTY) {
            int maxMs = timelineAdapter.getTimelineEntityByItemIndex(currentItemPosition).getDurationMs();
            int commonScrollPositionInMS = Math.abs(overallXScroll) * timelineAdapter.getCommonDurationMs() / timelineAdapter.getCommonWidthDp();
            int localScrollPositionInMS = Math.abs(scrollPath) * maxMs / scrollRange;
            int rangePathInMs = maxMs - localScrollPositionInMS;
            if (localScrollPositionInMS <= timelineAdapter.getCommonDurationMs() - MINIMAL_INTERVAL_MS
                    && localScrollPositionInMS >= MINIMAL_INTERVAL_MS
                    && rangePathInMs >= MINIMAL_INTERVAL_MS) {
                this.splitCollage(Math.abs(overallXScroll), commonScrollPositionInMS);
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

    private void splitCollage(int splitPosition,int scrollPositionInMS)
    {
        Bitmap collage = timelineAdapter.getBitmapByItemIndex(currentItemPosition);
        String name = timelineAdapter.getNameByItemIndex(currentItemPosition);
        TimelineEntity timelineEntity = timelineAdapter.getTimelineEntityByItemIndex(currentItemPosition);

        Bitmap[] splitPart = splitBitmap(collage,scrollPath);

        int beginMs = timelineEntity.getBeginMs();
        int endMs = timelineEntity.getEndMs();
        int beginDp = timelineEntity.getBeginDp();
        int endDp = timelineEntity.getEndDp();
        int width = timelineEntity.getWidth();
        int height = timelineEntity.getHeight();

        timelineAdapter.removeItemByIndex(currentItemPosition);
        timelineAdapter.notifyItemRangeRemoved(currentItemPosition,1);

        this.addItemInTimelineBody(splitPart[0],name + "(1)",currentItemPosition,scrollPath,
                height,beginMs,scrollPositionInMS,beginDp,splitPosition,TimelineEntity.Type.SCROLLABLE);
        this.addItemInTimelineBody(delimiterBitmap,"",currentItemPosition + 1,5,
               height,0,0,0,0,TimelineEntity.Type.EMPTY);
        this.addItemInTimelineBody(splitPart[1],name + "(2)",currentItemPosition + 2,
                width - scrollPath,
                height,scrollPositionInMS,endMs,splitPosition,endDp,TimelineEntity.Type.SCROLLABLE );

        scrollPath = 0;
        currentTimelineEntity = null;
       // updateTimeline(0);
        timelineBody.getLayoutManager().scrollToPosition(currentItemPosition + 2);

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
        this.addItemInTimelineBody(frameCollage, videoInfo.getFilename(),
                160 * 12,106,offsetMs,videoInfo.getDuration() + offsetMs,
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

        int normalizeSplitPos = scrollPath;
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

        int beginMs = timelineEntity.getBeginMs();
        int endMs = timelineEntity.getEndMs();
        int beginDp = timelineEntity.getBeginDp();
        int endDp = timelineEntity.getEndDp();
        int width = timelineEntity.getWidth();
        int height = timelineEntity.getHeight();
        int addPosition = currentItemPosition;
        int oldCommonWidth = timelineAdapter.getCommonWidthDp();

        timelineAdapter.removeItemByIndex(currentItemPosition);
        timelineAdapter.notifyItemRangeRemoved(currentItemPosition,1);

        if(!(normalizeSplitPos <= 0 && scrollPath <= 0)) {
            this.addItemInTimelineBody(splitPart[0], name + "(1)", addPosition++, normalizeSplitPos,
                    height, beginMs, scrollPositionInMS, beginDp, splitPosition, TimelineEntity.Type.SCROLLABLE);
        }

        this.addItemInTimelineBody(newCollage, videoInfo.getFilename(),addPosition,
                160 * 12,106,scrollPositionInMS,videoInfo.getDuration() + scrollPositionInMS,splitPosition,160 * 12 + splitPosition,TimelineEntity.Type.SCROLLABLE);
        if(currentItemPosition < addPosition)
            setAttached(currentItemPosition ,addPosition++);
        if(!(normalizeSplitPos == scrollPath && scrollPath >= oldCommonWidth)) {
            this.addItemInTimelineBody(splitPart[1], name + "(2)", addPosition,
                    width - normalizeSplitPos, height, videoInfo.getDuration()
                            + scrollPositionInMS, endMs + videoInfo.getDuration() + scrollPositionInMS,
                    160 * 12 + splitPosition, endDp + 160 * 12 + splitPosition, TimelineEntity.Type.SCROLLABLE);
            setAttached(addPosition - 1 ,addPosition);
        }

        currentTimelineEntity = null;
        // updateTimeline(0);
        timelineBody.getLayoutManager().scrollToPosition(currentItemPosition + 1);
    }

    public int getAttachedOffsetInMs(int timelineEntityIndex)
    {
        int attachedOffset = 0;
        int attachedIdx = timelineAdapter
                .getTimelineEntityByItemIndex(timelineEntityIndex)
                .getAttachedTimelineIndex();
        TimelineEntity entity = timelineAdapter.getTimelineEntityByItemIndex(attachedIdx);
        attachedOffset += entity.getEndMs();

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
}

