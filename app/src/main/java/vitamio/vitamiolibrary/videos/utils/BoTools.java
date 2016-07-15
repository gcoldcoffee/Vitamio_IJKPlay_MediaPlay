package vitamio.vitamiolibrary.videos.utils;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

/**
 * Created by aoe on 2016/3/25.
 */
public class BoTools {

    /**
     * 判断是否为空字符串（多个空格也视为空）
     * @param str
     * @return true 空字符串
     */
    public final static boolean isEmpty(String str){
        if(TextUtils.isEmpty(str)) return true;
        if(TextUtils.isEmpty(str.trim())) return true;
        return false;
    }

    //播放时间刻度格式化
    public static String stringForTime(long l, boolean flag)
    {
        StringBuilder stringbuilder = new StringBuilder();
        String s = null;
        int j = 0;
        int k = 0;
        long l1 = 0;
        int i = 0;
        int i1 = 0;
        Object obj2;
        Object obj3;
        Object obj4;
        if(flag) {

            if(l > 0L)
            {
                s = "+";
                l1 = l;
            } else
            if(l < 0L)
            {
                s = "-";
                l1 = 0L - l;
            } else
            {
                s = "";
                l1 = l;
            }
            i = (int)(l1 / 1000L);
            j = i % 60;
            k = (i / 60) % 60;
            i1 = i / 3600;
            if(i1 > 0){
                stringbuilder.append("[");
                stringbuilder.append(s);
            }

            if(i1 < 10)
                obj2 = (new StringBuilder("0")).append(i1).toString();
            else
                obj2 = Integer.valueOf(i1);
            stringbuilder.append(obj2);
            stringbuilder.append(":");
            if(k < 10)
                obj3 = (new StringBuilder("0")).append(k).toString();
            else
                obj3 = Integer.valueOf(k);
            stringbuilder.append(obj3);
            stringbuilder.append(":");
            if(j < 10)
                obj4 = (new StringBuilder("0")).append(j).toString();
            else
                obj4 = Integer.valueOf(j);
            stringbuilder.append(obj4);
            stringbuilder.append("]");
        }else {
            String s1;
            int j1 = (int)(l / 1000);
            int k1 = j1 % 60;
            int i2 = (j1 / 60) % 60;
            int j2 = j1 / 3600;
            Object obj5;
            Object obj6;
            Object obj7;
            if(j2 < 10)
                obj5 = (new StringBuilder("0")).append(j2).toString();
            else
                obj5 = Integer.valueOf(j2);
            stringbuilder.append(obj5);
            stringbuilder.append(":");
            if(i2 < 10)
                obj6 = (new StringBuilder("0")).append(i2).toString();
            else
                obj6 = Integer.valueOf(i2);
            stringbuilder.append(obj6);
            stringbuilder.append(":");
            if(k1 < 10)
                obj7 = (new StringBuilder("0")).append(k1).toString();
            else
                obj7 = Integer.valueOf(k1);
            stringbuilder.append(obj7);
        }
        return stringbuilder.toString();

    }

    /**
     * 播放时间格式化
     * @param timeMs
     * @return
     */
    public static String stringForTime(int timeMs) {
        StringBuilder mFormatBuilder= new StringBuilder();
        Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }


    public static String getDataStr(){
        SimpleDateFormat formatter=new  SimpleDateFormat("yyyyMMdd HH:mm:ss");
        Date curDate=new Date(System.currentTimeMillis());
        String str=formatter.format(curDate);
        Alog.i("**getDataStr**",str);
        return str;
    }


}
