package vitamio.vitamiolibrary.videos.utils;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.example.ijkplay.IjkPlayerActivity;

import vitamio.vitamiolibrary.videos.activity.VideoPlayerActivity;

/**
 * Created by aoe on 2015/12/25.
 */
public class PlayUtils {

    public static final String TAG="**PlayUtils**";

    /**\
     *
     * @param context
     * @param chanelTitle 直播平台
     * @param onliveTitle 直播标题
     * @param liveId  直播ID
     * @param url  播放源
     */
    public static void startPlayOnlive(Context context,String chanelTitle,String onliveTitle,int liveId,String url){
        Intent onliveIntent=new Intent(context,VideoPlayerActivity.class);
        onliveIntent.putExtra("chanelTitle",chanelTitle);
        onliveIntent.putExtra("onliveTitle",onliveTitle);
        onliveIntent.putExtra("liveId",liveId);
        onliveIntent.putExtra("url",url);
        onliveIntent.putExtra("isVideoOrOnlive",false);
        onliveIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        onliveIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(onliveIntent);
    }

    public static void startPlayVideo(Context context,String url){
        Intent onvideoIntent=new Intent(context,VideoPlayerActivity.class);
        onvideoIntent.putExtra("url",url);
        onvideoIntent.putExtra("isVideoOrOnlive",true);
        onvideoIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        onvideoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(onvideoIntent);
    }

    public static void startVitamioPlayVideo(Context context,String url,boolean flag){
        Intent onvideoIntent=new Intent(context,VideoPlayerActivity.class);
        onvideoIntent.putExtra("url",url);
        onvideoIntent.putExtra("isVideoOrOnlive",flag);
        onvideoIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        onvideoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(onvideoIntent);
    }

    public static void startIjkPlayVideo(Context context,String url,boolean flag){
        Intent intent=new Intent(context, IjkPlayerActivity.class);
        intent.putExtra("url",url);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean isEmptyView(View view){
        if(view==null){return false;}return true;
    }

    public static String getResString(Context context,int resId){
        return context.getResources().getString(resId);
    }

}
