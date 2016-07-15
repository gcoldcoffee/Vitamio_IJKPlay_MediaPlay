package com.example.ijkplay;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import com.example.ijkplay.view.IjkVideoView;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Description 播放view
 */
public class VideoPlayView  implements MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {


    private CustomMediaContoller mediaController;
    private View player_btn, view;
    private IjkVideoView mVideoView;
    private Handler handler = new Handler();
    private boolean isPause;

    private View rView;
    private Context mContext;
    private boolean portrait;

    public VideoPlayView(Context context) {
        mContext = context;

    }

    public void initViews(View parentView) {
        rView=parentView;
        view = parentView.findViewById(R.id.media_contoller);
        mVideoView = (IjkVideoView) parentView.findViewById(R.id.main_video);
        mediaController = new CustomMediaContoller(mContext, rView);
        mVideoView.setMediaController(mediaController);

        mVideoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                view.setVisibility(View.GONE);

                if (completionListener != null)
                    completionListener.completion(mp);
            }
        });

    }



    public boolean isPlay() {
        return mVideoView.isPlaying();
    }

    public void pause() {
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        } else {
            mVideoView.start();
        }
    }

    public void start(String path) {
        Uri uri = Uri.parse(path);
        if (mediaController != null)
            mediaController.start();
        if (!mVideoView.isPlaying()) {
            mVideoView.setVideoURI(uri);
            mVideoView.start();
        } else {
            mVideoView.stopPlayback();
            mVideoView.setVideoURI(uri);
            mVideoView.start();
        }
    }

    public void start(){
        if (mVideoView.isPlaying()){
            mVideoView.start();
        }
    }

    public void setContorllerVisiable(){
        mediaController.setVisiable();
    }

    public void seekTo(int msec){
        mVideoView.seekTo(msec);
    }

    public void onChanged(Configuration configuration) {
        portrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT;
        doOnConfigurationChanged(portrait);
    }

    public void doOnConfigurationChanged(final boolean portrait) {
        if (mVideoView != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mVideoView.setVideoLayout(0,0);

                }
            });
        }
    }

    public void stop() {
        if (mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
        }
    }

    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
    }

    private void setFullScreen(boolean fullScreen) {
        if (mContext != null && mContext instanceof Activity) {
            WindowManager.LayoutParams attrs = ((Activity) mContext).getWindow().getAttributes();
            if (fullScreen) {
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                ((Activity) mContext).getWindow().setAttributes(attrs);
                ((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            } else {
                attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                ((Activity) mContext).getWindow().setAttributes(attrs);
                ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }
        }

    }

    public void setShowContoller(boolean isShowContoller) {
        mediaController.setShowContoller(isShowContoller);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    public IjkVideoView getmVideoView(){
        return  mVideoView;
    }

    public long getPalyPostion() {
        return mVideoView.getCurrentPosition();
    }

    public void release() {
        mVideoView.release(true);
    }

    public int VideoStatus() {
        return mVideoView.getmCurrentState();
    }

    private CompletionListener completionListener;

    public void setCompletionListener(CompletionListener completionListener) {
        this.completionListener = completionListener;
    }

    public interface CompletionListener {
        void completion(IMediaPlayer mp);
    }
}
