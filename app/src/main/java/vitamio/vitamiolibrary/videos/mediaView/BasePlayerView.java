package vitamio.vitamiolibrary.videos.mediaView;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import java.util.Map;

import io.vov.vitamio.widget.Global;
import io.vov.vitamio.widget.IPlayerCallback;
import vitamio.vitamiolibrary.videos.utils.Alog;

public class BasePlayerView extends SurfaceView
		implements MediaPlayer.OnBufferingUpdateListener,
		MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
		MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener,
		MediaPlayer.OnVideoSizeChangedListener, SurfaceHolder.Callback{

	/**
	 * fragment tag，Activity中调用Fragment的时候添加
	 */
	public static final String FRAGMENT_TAG = "videoPlayer";

	public IPlayerCallback m_ActivityCallBack = null;
	public boolean m_bVideoCodecUnsupport = false;
	protected Context m_context = null;
	protected int m_iDuration  = 0;
	protected int m_iSeekPosWhenPrepared  = 0;
	protected int m_iVideoHeight= 0;
	protected int m_iVideoWidth = 0;
	private int mSurfaceWidth;
	private int mSurfaceHeight;
	protected boolean m_isPaused;
	protected boolean m_isPrepared = false;
	protected boolean m_isStopped = true;
	protected VideoFileType m_lUriType = VideoFileType.NORMAL;
	protected Uri m_uri = null;

	protected Map<String, String> headers;

//	protected IBufferUpdate callback;

	public enum VideoFileType{
		HTTP,NORMAL,RTSP;
	}
	public BasePlayerView(Context context) {
		super(context);
		m_context = context;
		m_bVideoCodecUnsupport = false;
		initVideoView(context);
	}

	public BasePlayerView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		m_context = context;
		m_bVideoCodecUnsupport = false;
		initVideoView(context);
	}

	public BasePlayerView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		m_context = context;
		m_bVideoCodecUnsupport = false;
		initVideoView(context);
	}

	public void initVideoView(Context context)
	{
		Alog.d("BasePlayerView", "initVideoView");
		//Fragment
		if (context!=null) {
//			FragmentActivity fragmentActivity = (FragmentActivity)ctx;
//			Fragment fragment = fragmentActivity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
//			m_ActivityCallBack = (IPlayerCallback)fragment;
			m_ActivityCallBack = (IPlayerCallback)context;
		}
		m_iVideoWidth = 0;
		m_iVideoHeight = 0;
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		getHolder().setKeepScreenOn(true);//保持屏幕常亮  不锁屏
		addSurfaceCallback();
	}

	protected void uninitVideoView()
	{
		if(getHolder() != null)
			getHolder().removeCallback(this);
		m_ActivityCallBack = null;
	}

	public int getCurrentPosition()
	{
		return 0;
	}

	public int getDuration()
	{
		return 0;
	}

	public SurfaceView getSurfaceView()
	{
		return this;
	}

	public int getVideoHeight()
	{
		return 0;
	}

	public int getVideoWidth()
	{
		return 0;
	}

	public boolean isPlaying()
	{
		return true;
	}

	public boolean isStopped()
	{
		return true;
	}

	public void setLooping(boolean flag){

	}

	public boolean isLoop(){
		return false;
	}


	protected void addSurfaceCallback()
	{
		getHolder().addCallback(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {
		mSurfaceWidth = width;
		mSurfaceHeight = height;

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if(m_ActivityCallBack != null)
			m_ActivityCallBack.onSurfaceCreated(true);

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
//		if(m_ActivityCallBack != null)
//			m_ActivityCallBack.onSurfaceCreated(false);

	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		m_iVideoHeight=mp.getVideoHeight();
		m_iVideoWidth=mp.getVideoWidth();

		if(m_ActivityCallBack != null){
			m_ActivityCallBack.onSetVideoViewLayout();
		}

	}

	public MediaPlayer getMediaPlayer(){
		return null;
	}

	public void openMediaFile()
	{
	}

	public void pause()
	{
	}

	public void play()
	{
	}


	public void release(){

	}

	public void recordCurPlayingPos(int i)
	{
		Alog.d("BasePlayerView", "getBufferPercentage");
		if(i > 0)
			m_iSeekPosWhenPrepared = i;
	}

	public void seekTo(int i)
	{
	}

	public void setOnActivityCallBack(IPlayerCallback iplayercallback)
	{
		m_ActivityCallBack = iplayercallback;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {

		m_isPrepared = true;
		m_iDuration = mp.getDuration();
		m_iVideoHeight=mp.getVideoHeight();
		m_iVideoWidth=mp.getVideoWidth();
		if(m_iSeekPosWhenPrepared > 0)
		{
			seekTo(m_iSeekPosWhenPrepared);
			m_iSeekPosWhenPrepared = 0;
		}
		if(m_ActivityCallBack != null)
			m_ActivityCallBack.onPreparedPlayback();

	}

	Global.VideoPlayerError errorCodeTransformation(int what){
		Global.VideoPlayerError videoplayererror = null;
		switch(what){
			case 1:
				videoplayererror = Global.VideoPlayerError.EXCEPTION_ERROR;
				break;
			case 100:
				videoplayererror = Global.VideoPlayerError.EXCEPTION_ERROR;
				break;
			case 200:
				videoplayererror = Global.VideoPlayerError.UNSUPPORT_FILE_ERROR;
				break;
			case 300:
				videoplayererror = Global.VideoPlayerError.UNSUPPORT_FILE_ERROR;
				break;
			default:
				videoplayererror = Global.VideoPlayerError.EXCEPTION_ERROR;
				break;
		}

		return videoplayererror;
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		return true;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Alog.d("BasePlayerView", (new StringBuilder("OnErrorListener.onError: ")).append(what).append(",").append(what).toString());
		stopPlayback();
		if(m_ActivityCallBack != null)
			m_ActivityCallBack.onErrorAppeared(errorCodeTransformation(what));
		return true;
	}

	public void stopPlayback()
	{
		m_isPrepared = false;
		m_isStopped = true;
		m_isPaused = false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		m_iSeekPosWhenPrepared = 0;
		if(m_iDuration > 0){
			if(m_ActivityCallBack != null)
				m_ActivityCallBack.onCompletePlayback();
		}else{
			m_ActivityCallBack.onErrorAppeared(Global.VideoPlayerError.EXCEPTION_ERROR);
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		Alog.i("BasePlayerView", (new StringBuilder("onBufferingUpdate...")).append(percent).toString());
		Alog.d("Come into onBufferingUpdate for BasePlayerView :"+percent);
		if(m_ActivityCallBack != null)
			m_ActivityCallBack.onBufferingback(percent);
	}

	public void setVideoURI(Uri uri,Map<String, String> headers)
	{
		m_uri = uri;
		this.headers=headers;
		String s = m_uri.toString();
		if(s.startsWith("rtsp://", 0))
			m_lUriType = VideoFileType.RTSP;
		else
		if(s.startsWith("http://", 0) || s.startsWith("playlist", 0))
			m_lUriType = VideoFileType.HTTP;
		else
			m_lUriType = VideoFileType.NORMAL;
		m_isPaused = false;
		m_isStopped = false;
	}


	public void setVideoURI(Uri uri)
	{
		m_uri = uri;
		String s = m_uri.toString();
		if(s.startsWith("rtsp://", 0))
			m_lUriType = VideoFileType.RTSP;
		else
		if(s.startsWith("http://", 0) || s.startsWith("playlist", 0))
			m_lUriType = VideoFileType.HTTP;
		else
			m_lUriType = VideoFileType.NORMAL;
		m_isPaused = false;
		m_isStopped = false;
	}


	/**设置画面比例
	 * Set the display options
	 * @param aspectRatio
	 *            video aspect ratio, will audo detect if 0.
	 */

	public static final int VIDEO_LAYOUT_ORIGIN = 0;//原始比列
	public static final int VIDEO_LAYOUT_FULL = 1;//全屏
	public static final int VIDEO_LAYOUT_16_9 = 2;//16：9
	public static final int VIDEO_LAYOUT_4_3 = 3;//4：3

	public void setVideoLayout(int layout, float aspectRatio) {

		DisplayMetrics disp = m_context.getResources().getDisplayMetrics();
		int displayWidth = disp.widthPixels, displayHeight = disp.heightPixels;
		int targetWidth = -1;
		int targetHeight = -1;

//		mVideoLayout = layout;
//		mAspectRatio = 0;

		if (m_iVideoWidth == 0 || m_iVideoHeight == 0) {
			return;
		}
		int videoWidth = m_iVideoWidth;
		int videoHeight = m_iVideoHeight;
		mSurfaceHeight = m_iVideoHeight;
		mSurfaceWidth = m_iVideoWidth;

		switch (layout) {
			case VIDEO_LAYOUT_FULL: {
				targetWidth = displayWidth;
				targetHeight = displayHeight;
				break;
			}
			case VIDEO_LAYOUT_ORIGIN: {
				targetWidth = videoWidth;
				targetHeight = videoHeight;
				break;
			}
			case VIDEO_LAYOUT_4_3: {
				targetWidth = 4;
				targetHeight = 3;
				break;
			}
			case VIDEO_LAYOUT_16_9: {
				targetWidth = 16;
				targetHeight = 9;
				break;
			}
			default:
				return;
		}
		if (targetWidth > 0 && targetHeight > 0) {
			double ard = (double) displayWidth / (double) displayHeight;
			double art = (double) targetWidth / (double) targetHeight;
			if (ard > art) {
				displayWidth = displayHeight * targetWidth / targetHeight;
			} else {
				displayHeight = displayWidth * targetHeight / targetWidth;
			}
		}
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(displayWidth, displayHeight);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		this.setLayoutParams(lp);
	}



















}
