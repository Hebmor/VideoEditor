package com.project.videoeditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.videoeditor.support.UtilUri;

public class VideoTimelineSplitView extends LinearLayout implements RecyclerView.OnScrollChangeListener{

    private ImageView timelinePointer;
    private RecyclerView timelineBody;
    private TimelineSplitAdapter timelineAdapter;
    private TextView indicatorTimeline;
    private TypedArray a;

    private Button saveBitton;
    private Button splitButton;

    private int scrollPath = 0;
    private int prevMsValue = 0;
    private int scrollRange = 0;
    private int currentItemPosition = 0;
    private int lastItemPosition = 0;


    private int countFrame = 0;
    private int frameWidth = 0;
    private int frameHeight = 0;

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

                Log.d("ZONE", String.valueOf(tempPosition));
                overallXScroll = overallXScroll + dx;
                scrollPath += dx;


                if(tempPosition >= 0) {
                    if (tempPosition != currentItemPosition) {


                        if (currentTimelineEntity != null)
                            if (tempPosition > currentItemPosition) {
                                scrollPath -= currentTimelineEntity.getWidth();
                                overallXScroll -= currentTimelineEntity.getWidth();
                            }
                            else if (tempPosition < currentItemPosition) {
                                currentTimelineEntity = timelineAdapter.getTimelineEntityByItemIndex(tempPosition);
                                scrollPath += currentTimelineEntity.getWidth();
                                overallXScroll += currentTimelineEntity.getWidth();
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
                lastItemPosition = tempPosition;
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

                    if (scrollRange > 0) {

                        if (overallXScroll == 0)
                            updateTimeline(minMs);
                        else {

                            if (overallXScroll >= scrollRange)
                                value = maxMs;
                            else
                                value = Math.round((float) Math.abs(overallXScroll) * (float) maxMs / (float) scrollRange);
                            updateTimeline(value);
                        }


                    }

  //                  Log.d("dx", String.valueOf(dx));
                    Log.d("X", String.valueOf(overallXScroll));
//                    Log.d("X 2", String.valueOf(overallXScroll - scrollPath));
                    Log.d("scrollPath", String.valueOf(scrollPath));


                }
            }
        });
    }
    public void addItemInTimelineBody(Bitmap frameCollage, String nameCollage, int widthItem, int heightItem, int beginMs, int endMs, TimelineEntity.Type type)
    {
        int idx = timelineAdapter.getItemCount();
        TimelineEntity timelineEntity = new TimelineEntity(widthItem,heightItem,nameCollage,idx,beginMs,endMs,type);
        timelineAdapter.addItem(frameCollage,nameCollage,timelineEntity);
        timelineAdapter.notifyItemInserted(idx);
    }
    public void addItemInTimelineBody(Bitmap frameCollage, String nameCollage,int pos,int widthItem,int heightItem,int beginMs,int endMs,TimelineEntity.Type type)
    {
        TimelineEntity timelineEntity = new TimelineEntity(widthItem,heightItem,nameCollage,pos,beginMs,endMs,type);
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
            playerControllerCallback.callingUpdatePlayerControllerPosition((int)value);
        }
    }

    private void registerButtonListener()
    {
        splitButton.setOnClickListener(this::clickSplit);
    }

    private void clickSplit(View view)
    {
        int minMs = 0;
        int maxMs = timelineAdapter.getTimelineEntityByItemIndex(currentItemPosition).getDurationMs();
        int scrollPositionInMS = Math.abs(scrollPath) * maxMs / scrollRange;
        if(scrollPositionInMS <=  maxMs - MINIMAL_INTERVAL_MS && scrollPositionInMS >= MINIMAL_INTERVAL_MS)
        {
            this.splitCollage(Math.abs(scrollPath),scrollPositionInMS);
        }
    }
    private void splitCollage(int splitPosition,int scrollPositionInMS)
    {
        Bitmap collage = timelineAdapter.getBitmapByItemIndex(currentItemPosition);
        String name = timelineAdapter.getNameByItemIndex(currentItemPosition);
        TimelineEntity timelineEntity = timelineAdapter.getTimelineEntityByItemIndex(currentItemPosition);
        int normalizePosition = splitPosition;
        Bitmap[] splitPart = splitBitmap(collage,splitPosition);

        int beginMs = timelineEntity.getBeginMs();
        int endMs = timelineEntity.getEndMs();
        int width = timelineEntity.getWidth();
        int height = timelineEntity.getHeight();
        overallXScroll = 0;
        timelineAdapter.removeItemByIndex(currentItemPosition);
        timelineAdapter.notifyItemRangeRemoved(currentItemPosition,1);

        this.addItemInTimelineBody(splitPart[0],name + "(1)",currentItemPosition,splitPosition,
                height,beginMs,scrollPositionInMS ,TimelineEntity.Type.SCROLLABLE);
        this.addItemInTimelineBody(delimiterBitmap,"",currentItemPosition + 1,5,
               height,0,0 ,TimelineEntity.Type.EMPTY);
        this.addItemInTimelineBody(splitPart[1],name + "(2)",currentItemPosition + 2,
                width - splitPosition,
                height,scrollPositionInMS,endMs,TimelineEntity.Type.SCROLLABLE );
        scrollPath = 0;
        currentTimelineEntity = null;
        currentItemPosition = 0;
    }
    private Bitmap[] splitBitmap(Bitmap bmp,int splitPosition)
    {
        Paint paint=new Paint();
        Bitmap bitmaps[] = new Bitmap[2];
        Rect splitBox1 = new Rect(0,0,splitPosition,bmp.getHeight());
        Rect destBox1 = splitBox1;
        Rect splitBox2 = new Rect(splitPosition,0,bmp.getWidth(),bmp.getHeight());
        Rect destBox2 = new Rect(0,0,bmp.getWidth() - splitPosition,bmp.getHeight());

        bitmaps[0] = Bitmap.createBitmap(splitPosition,bmp.getHeight(),bmp.getConfig());
        bitmaps[1] = Bitmap.createBitmap(bmp.getWidth() - splitPosition,bmp.getHeight(),bmp.getConfig());
        Canvas canvas = new Canvas(bitmaps[0]);
        canvas.drawBitmap(bmp,splitBox1,destBox1,null);
        Canvas canvas2 = new Canvas(bitmaps[1]);
        canvas2.drawBitmap(bmp,splitBox2,destBox2,null);
        return bitmaps;
    }

    public void setupCollageParam(int frameWidth,int frameHeight,int countFrame)
    {
       this.frameWidth = frameWidth;
       this.frameHeight = frameHeight;
       this.countFrame = countFrame;
    }
    private Bitmap genDelimiterBitmap(int widthTimelineInDp, int heightTimelineInDp, String hexColor)
    {
        int widthInPx = UtilUri.dpToPx(widthTimelineInDp);
        int heightInPx = UtilUri.dpToPx(heightTimelineInDp);
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
