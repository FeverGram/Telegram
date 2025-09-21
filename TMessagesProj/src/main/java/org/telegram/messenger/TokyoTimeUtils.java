package org.telegram.messenger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TokyoTimeUtils {
    
    private static final TimeZone TOKYO_TIMEZONE = TimeZone.getTimeZone("Asia/Tokyo");
    
    public static String getCurrentTokyoTime() {
        Calendar tokyoCalendar = Calendar.getInstance(TOKYO_TIMEZONE);
        Date tokyoTime = tokyoCalendar.getTime();
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        timeFormat.setTimeZone(TOKYO_TIMEZONE);
        
        return timeFormat.format(tokyoTime);
    }
    
    public static String getTokyoTimeWithEmoji() {
        String time = getCurrentTokyoTime();
        return "It's " + time + " in Tokyo \uD83D\uDE0E";
    }
}
