package com.example.ijkplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

public class IjkPlayerActivity extends BaseActivity {
    private String TAG=IjkPlayerActivity.class.getSimpleName();

    private Activity mActivity;

    private VideoPlayView videoPlayView;

    private View parentlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ijk_player);
        parentlayout=this.findViewById(R.id.parentlayout);
        mActivity=this;
        if(videoPlayView==null){
            videoPlayView=new VideoPlayView(mActivity);
        }
        videoPlayView.initViews(parentlayout);

        final String url=getIntent().getStringExtra("url");

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(videoPlayView!=null){
                    videoPlayView.setShowContoller(true);
                    videoPlayView.start(url);
                }
            }
        },1000);


    }

    private Handler mHandler=new Handler(){

    };


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (videoPlayView!=null) {
            videoPlayView.onChanged(newConfig);
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            } else {

            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            if (getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                return true;
            }
        }
        if(keyCode==KeyEvent.KEYCODE_MENU){
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setTitle("选择播放比例");
                final String[] cities = {"4:3", "16:9", "full screen", "Original Scale"};
                builder.setItems(cities, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String item = cities[which];
                        if (!TextUtils.isEmpty(item)) {
                            int scale=getScale(item);
                            videoPlayView.getmVideoView().setVideoLayout(scale, 0);
                        }
                    }
                });
                builder.show();
        }
        return super.onKeyUp(keyCode, event);
    }

    private int getScale(String str){
        if(str.equals("4:3")){
            return 3;
        }
        if(str.equals("16:9")){
            return 2;
        }
        if(str.equals("full screen")){
            return 1;
        }
        if(str.equals("Original Scale")){
            return 0;
        }
        return 0;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(videoPlayView!=null){
            videoPlayView.stop();
            videoPlayView.release();
            videoPlayView.onDestroy();
            videoPlayView=null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoPlayView!=null){
            videoPlayView.stop();
        }
    }





}
