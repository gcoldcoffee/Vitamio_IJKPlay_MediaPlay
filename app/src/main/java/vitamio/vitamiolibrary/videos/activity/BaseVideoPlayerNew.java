package vitamio.vitamiolibrary.videos.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;
import vitamio.vitamiolibrary.R;
import vitamio.vitamiolibrary.videos.mediaView.BasePlayerView;
import vitamio.vitamiolibrary.videos.mediaimpl.VPImpl;
import vitamio.vitamiolibrary.videos.utils.Alog;
import vitamio.vitamiolibrary.videos.utils.BoTools;
import vitamio.vitamiolibrary.videos.utils.PlayUtils;
import vitamio.vitamiolibrary.videos.utils.ScreenShot;
import vitamio.vitamiolibrary.videos.view.VPDialogInfo;
import vitamio.vitamiolibrary.videos.view.VideoScaleDialog;

/**
 * Created by aoe on 2015/12/22.
 */
public class BaseVideoPlayerNew extends Activity{
//        public String url="rtmp://live.hkstv.hk.lxdns.com/live/hks";
//    public String url="http://mvvideo1.meitudata.com/56f3622653a742935.mp4";

    public Activity mActivity;

    public Handler mHandler;

    public static final String TAG="**VideoPlayerNew**";

    public boolean isActivityResumOrStop=false;

    public boolean isVideoOrOnlive;// true 视频 / false 直播

    public boolean isMediaView=false,isVideoView=true;//硬解码  软解码

    public boolean vmisSurfaceViewCreate = false;//播放组件surfaceview是否创建

    public boolean isVitamioSoInit=false;

    /**视频播放画面比例**/
    public int mViewState = 0;//0==原始比例     1== 默认全屏   2==16:9  3==4:3

    public VideoView m_VideoView;//软解

    public BasePlayerView m_MediaView;//硬解


    /** 屏幕事件 */
    public GestureDetector mGestureDetector;
    public View mBottomview;


    /**菜单键选择比例***/
    public VideoScaleDialog videoScaleDialog=null;

    /***界面view***/
    public VideoHolder mVideoHolder;
    public MyControlClickListener myControlClickListener;


    /** 初始化软解SO文件 **/
    public void initializeVideoView(final Context mContext) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    isVitamioSoInit = Vitamio.initialize(mContext, getResources().getIdentifier("libarm", "raw", getPackageName()));
                } catch (UnsatisfiedLinkError e) {
                    e.printStackTrace();
                    isVitamioSoInit = false;
                }
            }
        });
    }



    public class VideoHolder{
        //点播
        public TextView vp_videostarttime_text;//播放刻度时间
        public TextView vp_videoendtime_text;//播放总时间
        public LinearLayout vp_video_controlView_buttom;//下部控制栏
        public LinearLayout vp_videoback_linear;//点播返回按钮
        public SeekBar vp_videoseektime_seekbar;//播放进度条
        public ImageView vp_videoplaypause_btn;//播放暂停按钮

        //直播
        public TextView playtitle_textview;//直播播放标题
        public TextView playchanel_textview;//直播播放平台
        public TextView playcountuser_textview;//观看人数
        public ImageView c_shotscreen;//截屏
        public ImageView playcountuser_imageview;//当前直播人数ico
        public ImageView playpause_btn;//播放暂停
        public RelativeLayout backvplayer_view;//返回按钮
        public LinearLayout vp_controlView_top;//上部控制栏
        public RelativeLayout vp_controlView_onlivebtm;

        //中间加载
        public ProgressBar vp_loadprogressbar;
        public LinearLayout load_linear;
        public TextView load_info_textview;
    }

    public void initControlView(VideoHolder videoHolder,Activity activity){
        myControlClickListener=new MyControlClickListener();
        //直播
        videoHolder.c_shotscreen=(ImageView)activity.findViewById(R.id.shotscreen);//截屏
        videoHolder.c_shotscreen.setOnClickListener(myControlClickListener);
        videoHolder.playpause_btn=(ImageView)activity.findViewById(R.id.playpause_btn);//控制播放暂停
        videoHolder.playpause_btn.setOnClickListener(myControlClickListener);
        videoHolder.backvplayer_view=(RelativeLayout)activity.findViewById(R.id.backvplayer_view);//返回退出按钮
        videoHolder.backvplayer_view.setOnClickListener(myControlClickListener);
        videoHolder.vp_controlView_top=(LinearLayout)activity.findViewById(R.id.vp_controlView_top);//直播上部控制栏
        videoHolder.playtitle_textview=(TextView)activity.findViewById(R.id.playtitle_textview);//直播当前播放节目标题
        videoHolder.playchanel_textview=(TextView)activity.findViewById(R.id.playchanel_textview);//直播播放平台
        videoHolder.playcountuser_textview=(TextView)activity.findViewById(R.id.playcountuser_textview);//当前直播人数
        videoHolder.playcountuser_imageview=(ImageView)activity.findViewById(R.id.playcountuser_imageview);//当前直播人数
        videoHolder.vp_controlView_onlivebtm=(RelativeLayout)activity.findViewById(R.id.vp_controlView_onlivebtm);//直播下部控制栏目
        //中间加载
        videoHolder.load_linear=(LinearLayout)activity.findViewById(R.id.load_linear);
        videoHolder.load_info_textview=(TextView)activity.findViewById(R.id.load_info_textview);
        videoHolder.vp_loadprogressbar=(ProgressBar)activity.findViewById(R.id.vp_loadprogressbar);
        //点播
        videoHolder.vp_videostarttime_text=(TextView)activity.findViewById(R.id.vp_videostarttime_text);//点播播放时间刻度信息
        videoHolder.vp_videoendtime_text=(TextView)activity.findViewById(R.id.vp_videoendtime_text);//点播源总时间
        videoHolder.vp_video_controlView_buttom=(LinearLayout)activity.findViewById(R.id.vp_video_controlView_buttom);//下部控制栏
        videoHolder.vp_videoback_linear=(LinearLayout)activity.findViewById(R.id.vp_videoback_linear);//点播返回按钮
        videoHolder.vp_videoback_linear.setOnClickListener(myControlClickListener);
        videoHolder.vp_videoplaypause_btn=(ImageView)activity.findViewById(R.id.vp_videoplaypause_btn);//点播控制播放暂停
        videoHolder.vp_videoplaypause_btn.setOnClickListener(myControlClickListener);
        videoHolder.vp_videoseektime_seekbar=(SeekBar)activity.findViewById(R.id.vp_videoseektime_seekbar);//点播可拖动快进快退控制条
        /**视频进度条***/
        if (videoHolder.vp_videoseektime_seekbar != null && isVideoOrOnlive) {
            if (videoHolder.vp_videoseektime_seekbar instanceof SeekBar) {
                videoHolder.vp_videoseektime_seekbar.setOnSeekBarChangeListener(new MySeekBarChangerListener());
                videoHolder.vp_videoseektime_seekbar.setMax(1000);
                videoHolder.vp_videoseektime_seekbar.setEnabled(true);
            }
        }

        setOnliveTitle();//填充直播播放信息

        hiddenVideoView(videoHolder);//隐藏点播控制栏目

        hiddenOnliveView(videoHolder);//隐藏直播控制栏目
    }

    private void setOnliveTitle(){
        if(!BoTools.isEmpty(controlHolder.playTitle)){//播放节目标题
            mVideoHolder.playtitle_textview.setText(controlHolder.playTitle);
        }
        if(!BoTools.isEmpty(controlHolder.chanelTitle)){//节目平台
            mVideoHolder.playchanel_textview.setText("直播平台："+controlHolder.chanelTitle);
        }
    }

    public void hiddenVideoView(VideoHolder videoHolder){
        videoHolder.vp_video_controlView_buttom.setVisibility(View.GONE);
        videoHolder.vp_videoback_linear.setVisibility(View.GONE);

    }
    public void hiddenOnliveView(VideoHolder videoHolder){
        if(isVideoOrOnlive){
            videoHolder.load_info_textview.setText(PlayUtils.getResString(mActivity,R.string.video_is_load)+"0%");
        }else{
            videoHolder.load_info_textview.setText(PlayUtils.getResString(mActivity,R.string.onlive_is_load)+"0%");
        }
        videoHolder.c_shotscreen.setVisibility(View.GONE);
        videoHolder.vp_controlView_onlivebtm.setVisibility(View.GONE);
        videoHolder.vp_controlView_top.setVisibility(View.GONE);
    }

    public void hiddenLoadView(VideoHolder videoHolder){
        if(PlayUtils.isEmptyView(videoHolder.load_linear)){
            videoHolder.load_linear.setVisibility(View.GONE);
        }
    }

    /**播放控制常量**/
    public VideoControlHolder controlHolder;
    public class VideoControlHolder{
        public boolean isFirstPlay=false;//首次是否开始播放

        public boolean isNotWifiPlay=false;//是否在没有wifi的状态下播放

        public String playUrl=null;//播放源地址

        /**截屏**/
        public String shotScreenPath=null;
        public String shotScreenName=null;

        //直播栏目标题，平台，播放人数信息
        public String chanelTitle=null;
        public String playTitle=null;
        public Integer liveId;

        //可拖动控制条
        public long m_iPrePositon = 0L;//拖动条拖动计算得到需要快进快退的刻度时间
        public long mlSeekToTime = 0L;//实时更新的播放刻度时间
        boolean m_ProgressTouched = false;//seekBar滑动

        /***播放不流畅*/
        public int tackLagg=0;
        public boolean isTackLagg=false;

        public boolean m_PlayPause = false;//播放是否暂停

        public boolean m_isLanCtrlBarShowing=false;//控制栏目是否显示

        /***网络状态**/
        public boolean isNetWorkEnabled=false;
        public boolean isNetWifiEnabled=false;
        public boolean isMobledEnabled=false;

    }

    public void onActivityStopPlay(){
        if(isVideoOrOnlive){
            playOrpause();
        }else{
            if(controlHolder.isNetWorkEnabled){
                playOrpause();
            }else{
                mediaStopPlayBack();
            }
        }
    }

    public void onActivityResumePlay(){
        if(controlHolder.isFirstPlay && isVideoOrOnlive){
            playOrpause();
        }else{
            if(mHandler!=null){
                mHandler.removeMessages(1);
                mHandler.sendEmptyMessageDelayed(1, 1000);
            }
        }
    }

    public void wifiOrMobledPlayer(){
        if(mHandler!=null){
            if(controlHolder.isMobledEnabled){
                mHandler.removeMessages(22);
                mHandler.sendEmptyMessageDelayed(22, 500);
            }else{
                mHandler.removeMessages(1);
                mHandler.sendEmptyMessageDelayed(1, 1000);
            }
        }
    }

    /***控制播放暂停**/
    public void playOrpause(){
        boolean isPlaying=isPlayer();
        if(isPlaying){
            pause(true);
            //更改播放暂停状态
            if(isVideoOrOnlive){
                mVideoHolder.vp_videoplaypause_btn.setImageResource(R.drawable.vp_video_play);
            }else{
                mVideoHolder.playpause_btn.setImageResource(R.drawable.vp_video_play);
            }
        }else {
            mediaPlay();
            mHandler.sendEmptyMessage(2);
            //更改播放暂停状态
            if(isVideoOrOnlive){
                mVideoHolder.vp_videoplaypause_btn.setImageResource(R.drawable.vp_video_pause);
            }else{
                mVideoHolder.playpause_btn.setImageResource(R.drawable.vp_video_pause);
            }
            controlHolder.m_PlayPause = false;
        }
        //显示控制栏目
        handlerControlViewVisibleGone();
    }

    public void pause(boolean flag) {
        if (mHandler != null) {
            mHandler.removeMessages(2);
        }
        mediaPause();
        if (flag) {
            boolean isPlayer=isPlayer();
            if (!isPlayer) {
                controlHolder.m_PlayPause = true;
            }
        }
    }

    //暂停
    public void mediaPause(){
        if(isMediaView && m_MediaView!=null){
            m_MediaView.pause();
        }
        if(isVideoView && m_VideoView!=null){
            m_VideoView.pause();
        }
    }
    //播放
    public void mediaPlay(){
        if(isMediaView && m_MediaView!=null){
            m_MediaView.play();
        }
        if(isVideoView && m_VideoView!=null){
            m_VideoView.start();
        }
    }
    //停止
    public void mediaStopPlayBack(){
        if (m_MediaView != null && isMediaView) {
            m_MediaView.stopPlayback();
        }
        if(m_VideoView!=null && isVideoView){
            m_VideoView.stopPlayback();
        }
    }
    //获取播放总时长
    public int getDuration(){
        if(isMediaView && m_MediaView!=null){
            return m_MediaView.getDuration();
        }
        if(isVideoView && m_VideoView!=null){
            return (int) m_VideoView.getDuration();
        }
        return 0;
    }
    //获取播放刻度
    public int getCurrentPosition(){
        if(isMediaView && m_MediaView!=null){
            return m_MediaView.getCurrentPosition();
        }
        if(isVideoView && m_VideoView!=null){
            return (int) m_VideoView.getCurrentPosition();
        }
        return 0;
    }
    //获取是否播放
    public boolean isPlayer(){
        if(isMediaView && m_MediaView!=null){
            return m_MediaView.isPlaying();
        }
        if(isVideoView && m_VideoView!=null){
            return m_VideoView.isPlaying();
        }
        return false;
    }
    //控制快进快退
    public void doSeek(long duration){
        if(isMediaView && m_MediaView!=null){
            m_MediaView.seekTo((int) duration);
        }
        if(isVideoView && m_VideoView!=null){
            m_VideoView.seekTo((int) duration);
        }
    }
    //设置单曲循环
    public void mediaLooper(boolean flag){
        if(isMediaView && m_MediaView!=null){
            m_MediaView.setLooping(flag);
        }
        if(isVideoView && m_VideoView!=null){
            m_VideoView.setLooping(flag);
        }
    }
    //播放器当前是否为单曲循环
    public boolean isLooping(){
        if(isMediaView && m_MediaView!=null){
            return m_MediaView.isLoop();
        }
        if(isVideoView && m_VideoView!=null){
            return m_VideoView.isLooping();
        }
        return false;
    }

    /**切换画面比例**/
    public void onVideoScale(int scale){
        if (isMediaView && m_MediaView != null) {
            if(m_MediaView.getSurfaceView() != null){
                m_MediaView.setVideoLayout(scale, 0);
            }
        }
        if(isVideoView && m_VideoView!=null){
            m_VideoView.setVideoLayout(scale, 0);
        }
        mViewState=scale;
    }

    //退出播放清除所有Handler，以及数据
    public void removeHandler(){};

    /***设置视频播放比例**/
    public void setVideoMediaViewLayout(int current){
        if(isMediaView && m_MediaView!=null){
            m_MediaView.setVideoLayout(current, 0);
        }
        if(isVideoView && m_VideoView!=null){
            m_VideoView.setVideoLayout(current, 0);
        }
    }

    /**截屏**/
    public void shotScreen(){
        if (m_VideoView != null && mActivity!=null && controlHolder.isFirstPlay) {
            controlBarVisible(false);
            mVideoHolder.c_shotscreen.setVisibility(View.GONE);
//            mVideoHolder.vp_controlView_onlivebtm.setVisibility(View.GONE);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ScreenShot.shotScrean(mActivity, m_VideoView.getShotScreenFrame(), new VPImpl.VPShotScreen() {
                        @Override
                        public void shotScreen(boolean flag, String name, String filepath) {
                            mVideoHolder.c_shotscreen.setVisibility(View.VISIBLE);
                            if (flag && !BoTools.isEmpty(filepath)) {
                                controlHolder.shotScreenPath = filepath;
                                onActivityStopPlay();
                                showWifiDialog(true);
                            } else {
                                Toast.makeText(mActivity, PlayUtils.getResString(mActivity,R.string.shotscreen_failure), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }, 500);
        }
    }

    /**控制栏目事件**/
    public class MyControlClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            handlerControlViewVisibleGone();
            if(v.getId()==R.id.shotscreen){
                if(controlHolder.isFirstPlay){
                    mHandler.removeMessages(20);
                    mHandler.sendEmptyMessageDelayed(20,300);
                }
            }
            if(v.getId()==R.id.playpause_btn || v.getId()==R.id.vp_videoplaypause_btn){
                playOrpause();
            }
            if(v.getId()==R.id.vp_videoback_linear || v.getId()==R.id.backvplayer_view){
                mediaStopPlayBack();
                removeHandler();
                finish();
            }
//            switch (v.getId()){
//                case R.id.shotscreen://截屏
//                    if(controlHolder.isFirstPlay){
//                        mHandler.removeMessages(20);
//                        mHandler.sendEmptyMessageDelayed(20,300);
//                    }
//                    break;
//                case R.id.playpause_btn://控制播放暂停
//                case R.id.vp_videoplaypause_btn:
//                    playOrpause();
//                    break;
//                case R.id.vp_videoback_linear:
//                case R.id.backvplayer_view://退出播放
//                    mediaStopPlayBack();
//                    removeHandler();
//                    finish();
//                    break;
//            }
        }
    }

    /**屏幕中间手势点击事件 **/
    public class MyOnGestureListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        @Override
        public void onShowPress(MotionEvent e) {
        }
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            //显示或隐藏栏目控制条
            if (controlHolder.m_isLanCtrlBarShowing) {
                controlBarVisible(false);
                controlHolder.m_isLanCtrlBarShowing = false;
            } else {
                controlBarVisible(true);
                handlerControlViewVisibleGone();
                controlHolder.m_isLanCtrlBarShowing = true;
            }
            return true;
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }
        @Override
        public void onLongPress(MotionEvent e) {
        }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }
    public class MyOnTouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return mGestureDetector.onTouchEvent(event);
        }
    }

    /**播放进度条滑动控制事件(拖动过程中不seek播放器，拖动停止即可seek播放器)**/
    public class MySeekBarChangerListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser && controlHolder.m_ProgressTouched){
                mHandler.removeMessages(2);
                mHandler.removeMessages(4);
                mHandler.removeMessages(5);
                int duration=getDuration();
                controlHolder.mlSeekToTime = progress * (duration / 1000);
                mVideoHolder.vp_videostarttime_text.setText(BoTools.stringForTime((int) controlHolder.mlSeekToTime));
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            controlHolder.m_ProgressTouched=true;
            mHandler.removeMessages(2);
            mHandler.removeMessages(4);
            mHandler.removeMessages(5);
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if(controlHolder.m_ProgressTouched){
                controlHolder.m_ProgressTouched=false;
            }
            mHandler.removeMessages(5);
            mHandler.removeMessages(4);
            mHandler.sendEmptyMessageDelayed(4,1000);
        }
    }

    /**弹出与消失Dialog(无网络，截屏)提示**/
    public VPDialogInfo vpDialogInfo;
    private boolean isShowVPDialog=false;
    public void showWifiDialog(boolean isShotScreen){
        if(vpDialogInfo==null){
            vpDialogInfo=new VPDialogInfo(mActivity,new MyDialogImpl(),isShotScreen);
        }
        if(!isShowVPDialog){
            vpDialogInfo.show();
        }
    }
    public void dismissWifiDialog(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(vpDialogInfo!=null){
                    isShowVPDialog=false;
                    vpDialogInfo.dismiss();
                    vpDialogInfo=null;
                }
            }
        });
    }
    private class MyDialogImpl implements VPImpl.DialogImpl {
        @Override
        public void show() {
            isShowVPDialog=true;
        }
        @Override
        public void dismiss() {
            isShowVPDialog=false;
            vpDialogInfo=null;
            if(mHandler!=null && controlHolder.isNetWorkEnabled){
                mHandler.removeMessages(6);
                mHandler.sendEmptyMessageDelayed(6,1000);
            }
        }
        @Override
        public void onPlayOrBackfinish(boolean flag) {
            if(flag){//继续播放
                if(controlHolder.isNetWorkEnabled || controlHolder.isMobledEnabled){
                    onActivityResumePlay();
                }else{
                    Alog.i(TAG,"**网络无连接**");
                }
            }else{//退出播放
                mediaStopPlayBack();
                removeHandler();
                finish();
            }
            dismissWifiDialog();
        }
        @Override
        public void onShotScreenPublish(boolean flag) {
            if(flag){//跳转发布
                onActivityStopPlay();
//                Intent intent=new Intent(mActivity, PublishBlogActivity.class);
//                intent.putExtra("isPlayVideoScreen",true);
//                intent.putExtra("videoShotScreenPath",controlHolder.shotScreenPath);
//                mActivity.startActivity(intent);
            }
            dismissWifiDialog();
        }
    }

    /*******************控制栏目显示或者隐藏*************/
    public void handlerControlViewVisibleGone(){
        if(mHandler!=null){
            mHandler.removeMessages(5);
            mHandler.sendEmptyMessageDelayed(5, 3000);
        }
    }
    public void controlBarVisible(boolean flag) {
        if (controlHolder.m_isLanCtrlBarShowing == flag) {
            upAndDownShowHidden(8);
            return;
        }
        mHandler.removeMessages(1);
        if (flag) {//显示
            upAndDownShowHidden(0);
        } else {
            controlHolder.m_isLanCtrlBarShowing = flag;
            upAndDownShowHidden(8);
        }
    }
    private void upAndDownShowHidden(int i) {
        if (mVideoHolder == null)
            return;
        if (i == View.VISIBLE) {
            if (!mVideoHolder.vp_controlView_top.isShown()) {
                topViewShowHIddenAnimation(true);
            }
            if (!mVideoHolder.vp_controlView_onlivebtm.isShown()) {
                buttomViewShowhiddenAnimation(true);
            }
            if (!mVideoHolder.vp_video_controlView_buttom.isShown()) {
                buttomViewShowhiddenAnimation(true);
            }
            if (!mVideoHolder.vp_videoback_linear.isShown()) {
                topViewShowHIddenAnimation(true);
            }
        } else {
            if (mVideoHolder.vp_controlView_top.isShown()) {
                topViewShowHIddenAnimation(false);
            }
            if (mVideoHolder.vp_controlView_onlivebtm.isShown()) {
                buttomViewShowhiddenAnimation(false);
            }
            if (mVideoHolder.vp_video_controlView_buttom.isShown()) {
                buttomViewShowhiddenAnimation(false);
            }
            if (mVideoHolder.vp_videoback_linear.isShown()) {
                topViewShowHIddenAnimation(false);
            }
        }
    }
    private void topViewShowHIddenAnimation(boolean flag) {
        if (mVideoHolder != null) {
            if(flag){
                Animation animation1 = AnimationUtils.loadAnimation(mActivity, R.anim.topcontrolview_in_bottom);
                if(isVideoOrOnlive){
                    mVideoHolder.vp_videoback_linear.setAnimation(animation1);
                    mVideoHolder.vp_videoback_linear.setVisibility(View.VISIBLE);
                }else{
                    mVideoHolder.vp_controlView_top.setAnimation(animation1);
                    mVideoHolder.vp_controlView_top.setVisibility(View.VISIBLE);
//                    mVideoHolder.c_shotscreen.setVisibility(View.VISIBLE);
                }
            }else{
                Animation animation1 = AnimationUtils.loadAnimation(mActivity, R.anim.topcontrolview_out_bottom);
                if(isVideoOrOnlive){
                    mVideoHolder.vp_videoback_linear.setAnimation(animation1);
                    mVideoHolder.vp_videoback_linear.setVisibility(View.GONE);
                }else{
                    mVideoHolder.vp_controlView_top.setAnimation(animation1);
                    mVideoHolder.vp_controlView_top.setVisibility(View.GONE);
//                    mVideoHolder.c_shotscreen.setVisibility(View.GONE);
                }
            }
        }
    }
    private void buttomViewShowhiddenAnimation(boolean flag) {
        if (mVideoHolder != null) {
            if(flag){
                Animation animation1 = AnimationUtils.loadAnimation(mActivity, R.anim.buttomcontrolview_in_bottom);
                if(isVideoOrOnlive){
                    mVideoHolder.vp_video_controlView_buttom.setAnimation(animation1);
                    mVideoHolder.vp_video_controlView_buttom.setVisibility(View.VISIBLE);
                }else{
                    mVideoHolder.vp_controlView_onlivebtm.setAnimation(animation1);
                    mVideoHolder.vp_controlView_onlivebtm.setVisibility(View.VISIBLE);
                }
            }else{
                Animation animation1 = AnimationUtils.loadAnimation(mActivity, R.anim.buttomcontrolview_out_bottom);
                if(isVideoOrOnlive){
                    mVideoHolder.vp_video_controlView_buttom.setAnimation(animation1);
                    mVideoHolder.vp_video_controlView_buttom.setVisibility(View.GONE);
                }else{
                    mVideoHolder.vp_controlView_onlivebtm.setAnimation(animation1);
                    mVideoHolder.vp_controlView_onlivebtm.setVisibility(View.GONE);
                }
            }
        }
    }



}
