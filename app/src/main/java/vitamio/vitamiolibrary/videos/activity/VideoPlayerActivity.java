package vitamio.vitamiolibrary.videos.activity;

import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.IPlayerCallback;
import io.vov.vitamio.widget.VideoView;
import vitamio.vitamiolibrary.R;
import vitamio.vitamiolibrary.videos.mediaView.BasePlayerView;
import vitamio.vitamiolibrary.videos.mediaimpl.VPImpl;
import vitamio.vitamiolibrary.videos.receiver.NetworkStateReceiver;
import vitamio.vitamiolibrary.videos.utils.Alog;
import vitamio.vitamiolibrary.videos.utils.BoTools;
import vitamio.vitamiolibrary.videos.utils.PlayUtils;
import vitamio.vitamiolibrary.videos.view.VideoScaleDialog;

public class VideoPlayerActivity extends BaseVideoPlayerNew implements IPlayerCallback {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isVideoOrOnlive=getIntent().getBooleanExtra("isVideoOrOnlive", false);

//        changeScreenPortraitLandscape();//视频为横屏，直播为竖屏

        setContentView(R.layout.vp_onlive);

        mActivity=this;

        registerNetWorkReceiver();//注册网络状态监听

        initializeVideoView(mActivity);

        getIntentData();

        initVideoMediaView();

        initControlView(mVideoHolder, VideoPlayerActivity.this);
    }


    /**
     * 横竖屏切换保存播放状态
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        Alog.i(TAG, "********onConfigurationChanged");
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){//竖屏
            Alog.i(TAG,"********ORIENTATION_LANDSCAPE");
            onVideoScale(mViewState);
        }
        else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){//横屏
            Alog.i(TAG,"********SCREEN_ORIENTATION_PORTRAIT");
            onVideoScale(mViewState);
        }
    }

    /***获取传入播放参数**/
    private void getIntentData(){
        mVideoHolder=new VideoHolder();
        controlHolder=new VideoControlHolder();

        controlHolder.playUrl=getIntent().getStringExtra("url");
        controlHolder.chanelTitle=getIntent().getStringExtra("chanelTitle");
        controlHolder.playTitle=getIntent().getStringExtra("onliveTitle");
        controlHolder.liveId=getIntent().getIntExtra("liveId", -1);
    }

    private void changeScreenPortraitLandscape(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if(isVideoOrOnlive){//点播
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
        }else{//直播
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
        }
    }
    /***初始化播放组件**/
    public void initVideoMediaView(){
        if(isVideoView){
            m_VideoView=(VideoView)this.findViewById(R.id.videoPlayerView);
            m_VideoView.setOnActivityCallBack(VideoPlayerActivity.this);
            m_VideoView.setVideoChroma(MediaPlayer.VIDEOCHROMA_RGB565);
            if(m_MediaView!=null){
                m_MediaView.setVisibility(View.GONE);
            }
            m_VideoView.getSurfaceView().setVisibility(View.VISIBLE);
        }else if(isMediaView){
            m_MediaView=(BasePlayerView)this.findViewById(R.id.mediaPlayerView);
            m_MediaView.setOnActivityCallBack(VideoPlayerActivity.this);
            if(m_VideoView!=null){
                m_VideoView.setVisibility(View.GONE);
            }
            m_MediaView.getSurfaceView().setVisibility(View.VISIBLE);
        }
        //屏幕手势监听
        mBottomview = this.findViewById(R.id.video_root_view_new);
        mGestureDetector = new GestureDetector(new MyOnGestureListener());
        mBottomview.setOnTouchListener(new MyOnTouchListener());
    }

    public VideoPlayerActivity(){
        mHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 1://播放器是否初始化，播放或者继续等待初始化
                        if(vmisSurfaceViewCreate){
                            mHandler.removeMessages(1);
                            openPlayerUrl(controlHolder.playUrl);
                        }else{
                            if (isMediaView && !isVideoView && m_MediaView.getSurfaceView().getVisibility() != View.GONE){//硬解
                                m_VideoView.setVisibility(View.GONE);
                                m_MediaView.getSurfaceView().setVisibility(View.VISIBLE);
                            }
                            if(isVideoView && !isMediaView){//软解
                                m_MediaView.getSurfaceView().setVisibility(View.GONE);
                                m_VideoView.setVisibility(View.VISIBLE);
                            }
                            if (mHandler != null){
                                sendEmptyMessageDelayed(3, 200L);
                            }
                        }
                        break;
                    case 2://更新播放时间信息
                        if(!isVideoOrOnlive){
                            return;
                        }
                        int i =getCurrentPosition();
                        sendBufferMessage(i);
                        break;
                    case 3://播放器是否开始播放
                        boolean isPlayer=isPlayer();
                        removeMessages(3);
                        if(isPlayer){
                            controlHolder.isFirstPlay=isPlayer;
                            sendEmptyMessage(2);
                            handlerControlViewVisibleGone();
                            mVideoHolder.load_linear.setVisibility(View.GONE);

                        }else{
                            sendEmptyMessageDelayed(3,500);
                        }
                        break;
                    case 4://seek快进快退播放
                        doSeek(controlHolder.mlSeekToTime);
                        sendEmptyMessage(3);
                        break;
                    case 5://栏目控制条显示或者隐藏
                        controlBarVisible(false);
                        break;
                    case 6:
                        onActivityResumePlay();
                        break;
                    case 20://截屏
                        if(!isVideoOrOnlive){
                            shotScreen();
                        }
                        break;
                    case 21://网络连接
                        dismissWifiDialog();
                        if(controlHolder.isFirstPlay && isVideoOrOnlive){
                            mediaPlay();
                        }else{
                            if(controlHolder.liveId>0 && !isVideoOrOnlive){
                                //loadPlayCount(controlHolder.liveId);
                                wifiOrMobledPlayer();
                            }else{
                                wifiOrMobledPlayer();
                            }
                        }
                        break;
                    case 22://网络断开
                        onActivityStopPlay();
                        showWifiDialog(false);
                        break;
                }
            }
        };
    }
    /**播放途中用于更新时间信息 与  进度条刻度**/
    private void sendBufferMessage(int i) {
        if (controlHolder.m_iPrePositon != (long) i) {
            setMainProgressBar(false, true);
            controlHolder.m_iPrePositon = (long) i;
        }
        int j = 1000 - i % 1000;
        if (j < 500){
            j = 500;
        }
        mHandler.sendEmptyMessageDelayed(2, j);
    }
    /***更新View时间信息 **/
    protected void setMainProgressBar(boolean flag, boolean saveFlag) {
        int j =this.getDuration();
        int i =this.getCurrentPosition();
        if (j > 1000) {
            int k = i / (j / 1000);
            if (!flag) {
                if(PlayUtils.isEmptyView(mVideoHolder.vp_videoseektime_seekbar)){
                    mVideoHolder.vp_videoseektime_seekbar.setProgress(k);
                }
                controlHolder.mlSeekToTime=i;
                //此处可保存播放断点刻度
                if (isVideoOrOnlive && PlayUtils.isEmptyView(mVideoHolder.vp_videostarttime_text)) {
                    mVideoHolder.vp_videostarttime_text.setText(BoTools.stringForTime(i));
                }
            }
        }
    }

    /*** 播放器加载完毕 */
    @Override
    public void onBufferingEnd() {
        Alog.i(TAG, "**onBufferingEnd()**");
        controlHolder.tackLagg=0;
        controlHolder.isTackLagg=false;
        hiddenLoadView(mVideoHolder);
        if(controlHolder.m_PlayPause){
            pause(true);
        }
    }

    /*** 播放器开始加载 */
    @Override
    public void onBufferingStart() {
        Alog.i(TAG, "**onBufferingStart()**");
        controlHolder.tackLagg=0;
        controlHolder.isTackLagg=false;
        hiddenLoadView(mVideoHolder);
    }

    /**播放不流畅  声画不同步**/
    @Override
    public void onTackLagging() {
        Alog.i(TAG, "**onTackLagging()**");
        hiddenLoadView(mVideoHolder);
        controlHolder.tackLagg++;
        Alog.i(" mediaPlayerTackLagging():", "**" + controlHolder.tackLagg + "==" + controlHolder.isTackLagg);
        if(controlHolder.tackLagg>50){
            controlHolder.isTackLagg=true;
            Alog.i(TAG, "播放不流畅");
        }
    }

    /***播放器加载刻度返回**/
    @Override
    public void onBufferingback(int paramInt) {
        Alog.i(TAG, "**onBufferingback()**" + paramInt);
        controlHolder.tackLagg=0;
        controlHolder.isTackLagg=false;
        if(isPlayer()){
            hiddenLoadView(mVideoHolder);
        }else{
            mVideoHolder.load_linear.setVisibility(View.VISIBLE);
            if(isVideoOrOnlive){

                mVideoHolder.load_info_textview.setText(PlayUtils.getResString(mActivity,R.string.video_is_load)+paramInt+"%");
            }else{
                mVideoHolder.load_info_textview.setText(PlayUtils.getResString(mActivity,R.string.onlive_is_load)+paramInt+"%");
            }
        }
        if(controlHolder.m_PlayPause && isPlayer()){
            pause(true);
        }
    }
    /**播放下一个视频**/
    @Override
    public void onCompletePlayback() {
        Alog.i(TAG, "**onCompletePlayback()**");
        if(isVideoOrOnlive){
            mediaStopPlayBack();
            removeHandler();
            finish();
        }else{
            Toast.makeText(mActivity, PlayUtils.getResString(mActivity,R.string.onlive_is_end), Toast.LENGTH_SHORT).show();
        }
    }

    //播放器初始化完成
    @Override
    public void onPreparedPlayback() {
        Alog.i(TAG, "**onPreparedPlayback()**");
        mVideoHolder.load_linear.setVisibility(View.GONE);
        onVideoPrepared();
    }

    //播放组件尺寸初始化
    @Override
    public void onSetVideoViewLayout() {
        Alog.i(TAG,"**onSetVideoViewLayout()**");
        onVideoScale(mViewState);
    }

    //播放组件初始化完成
    @Override
    public void onSurfaceCreated(boolean paramBoolean) {
        Alog.i(TAG,"**onSurfaceCreated()**");
        vmisSurfaceViewCreate=paramBoolean;

    }

    /** * 播放器错误信息 */
    @Override
    public void onErrorAppeared(io.vov.vitamio.widget.Global.VideoPlayerError paramVideoPlayerError) {
        if (paramVideoPlayerError != io.vov.vitamio.widget.Global.VideoPlayerError.EXCEPTION_ERROR) {
            if (paramVideoPlayerError == io.vov.vitamio.widget.Global.VideoPlayerError.NETWORK_ERROR) {
                Alog.i(TAG,"NETWORK_ERROR 网络错误");
            } else if (paramVideoPlayerError == io.vov.vitamio.widget.Global.VideoPlayerError.STORAGE_UNENOUGH_ERROR) {
                Toast.makeText(this,"STORAGE_UNENOUGH_ERROR",Toast.LENGTH_SHORT).show();
                Alog.i(TAG, "STORAGE_UNENOUGH_ERROR 找不到储存位置");
            } else if (paramVideoPlayerError == io.vov.vitamio.widget.Global.VideoPlayerError.STREAMING_TIMEOUT_ERROR) {
                Alog.i(TAG, "STREAMING_TIMEOUT_ERROR 播放超时");
            } else if (paramVideoPlayerError == io.vov.vitamio.widget.Global.VideoPlayerError.UNSUPPORT_FILE_ERROR) {
                Alog.i(TAG, "UNSUPPORT_FILE_ERROR 文件错误");
            } else if (paramVideoPlayerError == io.vov.vitamio.widget.Global.VideoPlayerError.GETVIDEOSOURCE_ERROR) {
                Alog.i(TAG, "GETVIDEOSOURCE_ERROR 播放源错误");
            } else if (paramVideoPlayerError == io.vov.vitamio.widget.Global.VideoPlayerError.STREAMING_SERVER_ERROR) {
                Alog.i(TAG, "STREAMING_SERVER_ERROR 播放服务错误");
            }
        } else {
            toastPlayFailure();
            mediaStopPlayBack();
        }
    }

    private void toastPlayFailure(){
        if(isVideoOrOnlive){
            Toast.makeText(this,PlayUtils.getResString(mActivity,R.string.video_load_error),Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,PlayUtils.getResString(mActivity,R.string.onlive_load_error),Toast.LENGTH_SHORT).show();
        }
    }

    /** * 为播放器设置路径 开始播放 */
    public void openPlayerUrl(String url) {
        if(BoTools.isEmpty(url)){
            toastPlayFailure();
            return;
        }
        Uri m_uri=Uri.parse(url);
        Alog.i(TAG,"openPlayerUrl:"+m_uri.toString());
        if(isMediaView &&  m_MediaView!=null){
            m_MediaView.setVideoURI(m_uri);
            m_MediaView.openMediaFile();
        }
        if(isVideoView && m_VideoView!=null){
            m_VideoView.setVideoPath(m_uri);
        }
    }

    /*** 通知播放器开始播放 */
    public void onVideoPrepared() {
        int duration=getDuration();
        if (isVideoOrOnlive) {
            mVideoHolder.vp_videoendtime_text.setText(BoTools.stringForTime(duration));
        }
        mediaPlay();
        mHandler.removeMessages(3);
        mHandler.sendEmptyMessage(3);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK://监听返回键退出播放
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                    mediaStopPlayBack();
                    removeHandler();
                }
                break;
            case KeyEvent.KEYCODE_MENU://监听菜单键
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                    videoScaleDialog=new VideoScaleDialog(mActivity, new VPImpl.ScaleImpl() {
                        @Override
                        public void show() {
                        }
                        @Override
                        public void dismiss() {
                        }
                        @Override
                        public void onScale(int scale) {
                            onVideoScale(scale);
                            if(videoScaleDialog!=null){
                                videoScaleDialog.dismiss();
                            }
                        }
                    });
                    videoScaleDialog.show();
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isActivityResumOrStop){
            onActivityResumePlay();
            isActivityResumOrStop=false;
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        isActivityResumOrStop=true;
        if(isVideoOrOnlive){
            playOrpause();
        }else{
            mediaStopPlayBack();
        }
    }

    /**注册网络监听广播*/
    private NetworkStateReceiver networkStateReceiver;//检测网络状态
    private void registerNetWorkReceiver(){
        networkStateReceiver = new NetworkStateReceiver(new NetworkStateReceiver.NetworkStateListener() {
            @Override
            public void onNetworkEnabled() {
                controlHolder.isNetWorkEnabled=true;
                mHandler.removeMessages(21);
                mHandler.sendEmptyMessageDelayed(21, 1000);
            }
            @Override
            public void onNetworkDisabled() {
                controlHolder.isNetWorkEnabled=false;
                mHandler.removeMessages(22);
                mHandler.sendEmptyMessageDelayed(22, 1000);
            }
            @Override
            public void onNetWifiEnabled() {
                controlHolder.isNetWifiEnabled=true;
            }
            @Override
            public void onNetWifiDisabled() {
                controlHolder.isNetWifiEnabled=false;
            }
            @Override
            public void onNetMobleEnabled() {
                controlHolder.isMobledEnabled=true;
            }
            @Override
            public void onNetMobleDisabled() {
                controlHolder.isMobledEnabled=false;
            }
        });
        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        localIntentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        registerReceiver(networkStateReceiver, localIntentFilter);
    }
    public void unregisterReceiver(){
        if(networkStateReceiver!=null){
            unregisterReceiver(networkStateReceiver);
        }
    }

    @Override
    public void removeHandler() {
        super.removeHandler();
        if(mHandler!=null){
            mHandler.removeMessages(1);
            mHandler.removeMessages(2);
            mHandler.removeMessages(3);
            mHandler.removeMessages(4);
            mHandler.removeMessages(5);
            mHandler.removeMessages(6);
            mHandler.removeMessages(20);
            mHandler.removeMessages(21);
            mHandler.removeMessages(22);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
        mediaStopPlayBack();
        removeHandler();
    }


}
