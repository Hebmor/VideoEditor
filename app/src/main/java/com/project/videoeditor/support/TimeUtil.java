package com.project.videoeditor.support;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class TimeUtil {
    static public String GetFormattedTimeFromMilliseconds(long timeMs)
    {
        long msFrom = timeMs % 1000;
        long secsFrom = TimeUnit.MILLISECONDS.toSeconds(timeMs)  % 60;
        long hoursFrom = TimeUnit.MILLISECONDS.toHours(timeMs)  % 24;
        long minutesFrom = TimeUnit.MILLISECONDS.toMinutes(timeMs)  % 60;
        return String.format("%d:%d:%d.%d",hoursFrom,minutesFrom,secsFrom,msFrom);
    }

    static public String getTimeInString()
    {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
        return df.format(c.getTime());
    }
}
