package com.project.videoeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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

import com.project.videoeditor.support.SupportUtil;

import static com.project.videoeditor.support.SupportUtil.splitBitmap;

public class VideoTimelineSplitView extends LinearLayout implements RecyclerView.OnScrollChangeListener{

    private ImageView timelinePointer;
    private RecyclerView timelineBody;
    private TimelineSplitAdapter timelineAdapter;
    private TextView indicatorTimeline;
    private TypedArray a;

    private Button splitButton;

    private int scrollPath = 0;
    private int prevMsValue = 0;
    private int scrollRange = 0;
    private int currentItemPosition = 0;

    private Bitmap delimiterBitmap;
    private final int MINIMAL_INTERVAL_MS = 1000;
    private int overallXScroll = 0;
    private TimelineEntity currentTimelineEntity;

    @Override
    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        //Log.d("X P", String.valueOf(oldScrollX));
    }

    interface PlayerControllerCallback {
        void callingUpdatePlayerControllerPosition(int positionMS);
    }

    VideoTimelineCutView.PlayerControllerCallback playerControllerCallback;

    public void registerCallBack(VideoTimelineCutView.PlayerControllerCallback playerControllerCallback){
        this.playerControllerCallback = playerControllerCallback;
    }
    public VideoTimelineSplitView(Context context) {
        super(context);
        inflate(context,R.layout.timeline_split,this);

        timelinePointer = findViewById(R.id.timelinePointer);
        timelineBody = findViewById(R.id.timeline_recycler);
        indicatorTimeline = findViewById(R.id.indicatorTimeline);
        splitButton = findViewById(R.id.buttonSplit);
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        timelineBody.setLayoutManager(layoutManager);
        timelineBody.setAdapter(timelineAdapter);
        timelineBody.setOnScrollChangeListener(this);
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

                Log.d("ZONE", String.valueOf(tempPosition));
                overallXScroll = overallXScroll + dx;

                if(tempPosition >= 0) {
                    if (tempPosition != currentItemPosition) {


                        if (currentTimelineEntity != null)
                            if (tempPosition > currentItemPosition) {
                                scrollPath -= currentTimelineEntity.getWidth();
                                //overallXScroll -= currentTimelineEntity.getWidth();
                            }
                            else if (tempPosition < currentItemPosition) {
                                currentTimelineEntity = timelineAdapter.getTimelineEntityByItemIndex(tempPosition);
                                scrollPath += currentTimelineEntity.getWidth();
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

                int offset = recyclerView.computeHorizontalScrollOffset();
                int extent = recyclerView.computeHorizontalScrollExtent();
                int range = recyclerView.computeHorizontalScrollRange();

                int percentage = (int) (100.0 * offset / (float) (range - extent));

               // Log.d("dddde", "RecyclerView, " + "percentage:" + percentage + "%");


                if (currentTimelineEntity != null) {
                    int value = 0;
                    int minMs = 0;
                    int maxMs = currentTimelineEntity.getDurationMs();
                    scrollRange = currentTimelineEntity.getWidth();
                    scrollPath = Math.abs( overallXScroll - currentTimelineEntity.getBeginDp());
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

  //                  Log.d("dx", String.valueOf(dx));
                    Log.d("X", String.valueOf(overallXScroll));
                    Log.d("playerTimeValue", String.valueOf(playerTimeValue));
//                    Log.d("X 2", String.valueOf(overallXScroll - scrollPath));
                    Log.d("scrollPath", String.valueOf(scrollPath));


                }
            }
        });
    }

    public void addItemInTimelineBody(Bitmap frameCollage, String nameCollage, int widthItem, int heightItem,
                                      int beginMs, int endMs,int beginDp,int endDp, TimelineEntity.Type type)
    {
        int idx = timelineAdapter.getItemCount();
        TimelineEntity timelineEntity = new TimelineEntity(widthItem,heightItem,nameCollage,idx,beginMs,endMs,beginDp,endDp,type);
        timelineAdapter.addItem(frameCollage,nameCollage,timelineEntity);
        timelineAdapter.notifyItemInserted(idx);
    }

    public void addItemInTimelineBody(Bitmap frameCollage, String nameCollage,int pos,int widthItem,int heightItem,
                                      int beginMs,int endMs,int beginDp,int endDp, TimelineEntity.Type type)
    {
        TimelineEntity timelineEntity = new TimelineEntity(widthItem,heightItem,nameCollage,pos,beginMs,endMs,beginDp,endDp,type);
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

    private void registerButtonListener()
    {
        splitButton.setOnClickListener(this::clickSplit);
    }

    private void clickSplit(View view)
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
}
