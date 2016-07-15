/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2013 YIXIA.COM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vov.vitamio.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.util.Map;

import io.vov.vitamio.MediaFormat;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.MediaPlayer.OnVideoSizeChangedListener;
import io.vov.vitamio.MediaPlayer.TrackInfo;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.utils.ALog;

/**
 * Displays a video file. The VideoView class can load images from various
 * sources (such as resources or content providers), takes care of computing its
 * measurement from the video so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting.
 * <p/>
 * VideoView also provide many wrapper methods for
 * {@link io.vov.vitamio.MediaPlayer}, such as {@link #getVideoWidth()},
 * {@link #setTimedTextShown(boolean)}
 */
public class VideoView extends SurfaceView implements
		MediaController.MediaPlayerControl{

	public IPlayerCallback m_ActivityCallBack = null;
	public static final int VIDEO_LAYOUT_ORIGIN = 0;//原始比列
    public static final int VIDEO_LAYOUT_FULL = 1;//全屏
    public static final int VIDEO_LAYOUT_16_9 = 2;//16：9
    public static final int VIDEO_LAYOUT_4_3 = 3;//4：3
	
	private static final int STATE_ERROR = -1;
	private static final int STATE_IDLE = 0;
	private static final int STATE_PREPARING = 1;
	private static final int STATE_PREPARED = 2;
	private static final int STATE_PLAYING = 3;
	private static final int STATE_PAUSED = 4;
	private static final int STATE_PLAYBACK_COMPLETED = 5;
	private static final int STATE_SUSPEND = 6;
	private static final int STATE_RESUME = 7; 
	private static final int STATE_SUSPEND_UNSUPPORTED = 8;
	
	public static final String FRAGMENT_TAG = "videoPlayer";
	
	private Uri mUri;
	private long mDuration;
	private int mCurrentState = STATE_IDLE;
	private int mTargetState = STATE_IDLE;
	public float mAspectRatio = 0;
	private int mVideoLayout = VIDEO_LAYOUT_ORIGIN;
	private SurfaceHolder mSurfaceHolder = null;
	private MediaPlayer mMediaPlayer = null;
	private int mVideoWidth;
	private int mVideoHeight;
	private float mVideoAspectRatio;
	private int mVideoChroma = MediaPlayer.VIDEOCHROMA_RGBA;
	private boolean mHardwareDecoder = false;
	private int mSurfaceWidth;
	private int mSurfaceHeight;
	private OnPreparedListener mOnPreparedListener;
//	private OnSeekCompleteListener mOnSeekCompleteListener;
	private int mCurrentBufferPercentage;
	private long mSeekWhenPrepared; // recording the seek position while
									// preparing
	private Context mContext;
	private Map<String, String> mHeaders;
	private int mBufSize;
	
	public SurfaceView getSurfaceView()
	{
		return this;
	}
	
	OnVideoSizeChangedListener mSizeChangedListener = new OnVideoSizeChangedListener() {
		public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			ALog.d("onVideoSizeChanged: (%dx%d)", width, height);
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			mVideoAspectRatio = mp.getVideoAspectRatio();

			if (m_ActivityCallBack != null){
				m_ActivityCallBack.onSetVideoViewLayout();
			}

		}
	};
	OnPreparedListener mPreparedListener = new OnPreparedListener() {
		public void onPrepared(MediaPlayer mp) {
			ALog.i("*mPreparedListener*","onPrepared");
			mCurrentState = STATE_PREPARED;
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			mVideoAspectRatio = mp.getVideoAspectRatio();

			long seekToPosition = mSeekWhenPrepared;
			if (seekToPosition != 0)
				seekTo(seekToPosition);
			if (mVideoWidth != 0 && mVideoHeight != 0) {
				if (mSurfaceWidth == mVideoWidth&& mSurfaceHeight == mVideoHeight) {
					if (mTargetState == STATE_PLAYING) {
						start();
					} 
				}
			} else if (mTargetState == STATE_PLAYING) {
				start();
			}
		   if(m_ActivityCallBack != null){
			   m_ActivityCallBack.onPreparedPlayback();
		   }
		}
	};
	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			ALog.i("*mSHCallback*","surfaceChanged");
			mSurfaceWidth = w;
			mSurfaceHeight = h;
			boolean isValidState = (mTargetState == STATE_PLAYING);
			boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
			if (mMediaPlayer != null && isValidState && hasValidSize) {
				if (mSeekWhenPrepared != 0)
					seekTo(mSeekWhenPrepared);
				start();
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
			ALog.i("*mSHCallback*","surfaceCreated");
			mSurfaceHolder = holder;
			if (mMediaPlayer != null && mCurrentState == STATE_SUSPEND
					&& mTargetState == STATE_RESUME) {
				mMediaPlayer.setDisplay(mSurfaceHolder);
				resume();
			} else {
				openVideo();
			}
			 if(m_ActivityCallBack != null){
				 m_ActivityCallBack.onSurfaceCreated(true);
			 }
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			ALog.i("*mSHCallback*","surfaceDestroyed");
			mSurfaceHolder = null;
			release(true);
		}
	};
	
	private OnCompletionListener mCompletionListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {
			ALog.d("onCompletion");
			mCurrentState = STATE_PLAYBACK_COMPLETED;
			mTargetState = STATE_PLAYBACK_COMPLETED;
			
			if(m_ActivityCallBack != null){
				m_ActivityCallBack.onCompletePlayback();
			}
		}
		
	};
	private OnErrorListener mErrorListener = new OnErrorListener() {
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
			ALog.d("Error: %d, %d", framework_err, impl_err);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			if(m_ActivityCallBack!=null){
				switch (framework_err) {
				case 1:
					ALog.e("JPlayer2", (new StringBuilder("Unknown error, extra is "))
							.append(impl_err).toString());
					m_ActivityCallBack.onErrorAppeared(io.vov.vitamio.widget.Global.VideoPlayerError.EXCEPTION_ERROR);
					break;
				case 100:
					ALog.e("JPlayer2", (new StringBuilder(
							"Media Server died, extra is ")).append(impl_err).toString());
					m_ActivityCallBack.onErrorAppeared(io.vov.vitamio.widget.Global.VideoPlayerError.NETWORK_ERROR);
					break;
				case 200:
					ALog.e("JPlayer2", (new StringBuilder("File not valid for progressive playback, extra is ")).append(impl_err).toString());
					m_ActivityCallBack.onErrorAppeared(io.vov.vitamio.widget.Global.VideoPlayerError.UNSUPPORT_FILE_ERROR);
					break;
				case 300:
					m_ActivityCallBack.onErrorAppeared(io.vov.vitamio.widget.Global.VideoPlayerError.UNSUPPORT_FILE_ERROR);
					ALog.e("JPlayer2",(new StringBuilder("Codec unsupport, extra is ")).append(impl_err).toString());
					break;
				default:
					ALog.e("JPlayer2", (new StringBuilder("TNND, what error is ")).append(impl_err).append("? code is ").append(impl_err).toString());
					m_ActivityCallBack.onErrorAppeared(io.vov.vitamio.widget.Global.VideoPlayerError.EXCEPTION_ERROR);
					break;

				}
			}
			return true;
		}
	};
	private OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			mCurrentBufferPercentage = percent;
			 if(m_ActivityCallBack != null){
				 m_ActivityCallBack.onBufferingback(percent);
			 }
		}
	};
	private OnInfoListener mInfoListener = new OnInfoListener() {
		@Override
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			ALog.d("onInfo: (%d, %d)", what, extra);
			switch (what) {
			case MediaPlayer.MEDIA_INFO_BUFFERING_START:
				ALog.i("mInfoListener", "MediaPlayer.MEDIA_INFO_BUFFERING_START");
				if(mMediaPlayer!=null){
					mMediaPlayer.pause();
				}
				 if(m_ActivityCallBack != null){
					 m_ActivityCallBack.onBufferingStart();
				 }
				break;
			case MediaPlayer.MEDIA_INFO_BUFFERING_END:
				ALog.i("mInfoListener", "MediaPlayer.MEDIA_INFO_BUFFERING_END");
				if(mMediaPlayer!=null){
					mMediaPlayer.start();
				}
				if(m_ActivityCallBack != null){
					m_ActivityCallBack.onBufferingEnd();
				}
				break;
			case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
				ALog.i("mInfoListener", "MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING 目前播放不流畅！");
				if(m_ActivityCallBack!=null){
					m_ActivityCallBack.onTackLagging();
				}
				break;
			case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED://加载速度
				ALog.i("mInfoListener", "download rate:" + extra);
				break;
			case MediaPlayer.VIDEOQUALITY_LOW://视频质量--流畅
				ALog.i("mInfoListener", "视频质量--流畅");
				break;
			case MediaPlayer.VIDEOQUALITY_MEDIUM://视频质量--普通
				ALog.i("mInfoListener", "视频质量--普通");
				break;
			case MediaPlayer.VIDEOQUALITY_HIGH://视频质量--高质
				ALog.i("mInfoListener", "视频质量--高质");
				break;
			case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE://是否支持快进快退
				ALog.i("mInfoListener", "MediaPlayer.MEDIA_INFO_NOT_SEEKABLE");
				break;
			default:
				break;
			}
			return true;
		}
	};
//	private OnSeekCompleteListener mSeekCompleteListener = new OnSeekCompleteListener() {
//		@Override
//		public void onSeekComplete(MediaPlayer mp) {
//			ALog.d("onSeekComplete");
//		}
//	};
	
  public void setOnActivityCallBack(IPlayerCallback iplayercallback)
	{
		m_ActivityCallBack = iplayercallback;
	}
	    
	
	public VideoView(Context context) {
		super(context);
		initVideoView(context);
	}

	public VideoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		initVideoView(context);
	}

	public VideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initVideoView(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
		int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
		setMeasuredDimension(width, height);
	}

	@Override
	public void setLayoutDirection(int layoutDirection) {
		super.setLayoutDirection(layoutDirection);
	}

	/**设置画面比例
	 * Set the display options
	 * @param aspectRatio
	 *            video aspect ratio, will audo detect if 0.
	 */
	public void setVideoLayout(int layout, float aspectRatio) {
		
		  DisplayMetrics disp = mContext.getResources().getDisplayMetrics();
	        int displayWidth = disp.widthPixels, displayHeight = disp.heightPixels;
	        int targetWidth = -1;
	        int targetHeight = -1;

	        mVideoLayout = layout;
	        mAspectRatio = 0;

	        if (mVideoWidth == 0 || mVideoHeight == 0) {
	            return;
	        }
	        int videoWidth = mVideoWidth;
	        int videoHeight = mVideoHeight;
	        mSurfaceHeight = mVideoHeight;
	        mSurfaceWidth = mVideoWidth;

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
	        android.widget.RelativeLayout.LayoutParams lp = new android.widget.RelativeLayout.LayoutParams(displayWidth, displayHeight);
	        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
	        this.setLayoutParams(lp);
	}
	

	/**初始化   SurfaceView Holder**/
	@SuppressWarnings("deprecation")
	private void initVideoView(Context ctx) {
		mContext = ctx;
		mVideoWidth = 0;
		mVideoHeight = 0;
		if (ctx!=null) {
//			FragmentActivity fragmentActivity = (FragmentActivity)ctx;
//			Fragment fragment = fragmentActivity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
//			m_ActivityCallBack = (IPlayerCallback)fragment;
			m_ActivityCallBack = (IPlayerCallback)ctx;
		}
		getHolder().setFormat(PixelFormat.RGBA_8888); // PixelFormat.RGB_565
		getHolder().addCallback(mSHCallback);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		getHolder().setKeepScreenOn(true);//保持屏幕常亮  不锁屏
		// this value only use Hardware decoder before Android 2.3
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				&& mHardwareDecoder) {
			getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		mCurrentState = STATE_IDLE;
		mTargetState = STATE_IDLE;
		if (ctx instanceof Activity){
			((Activity) ctx).setVolumeControlStream(AudioManager.STREAM_MUSIC);
		}
	}
	/**销毁SurfaceView  Holder**/
	public void uninitVideoView()
	{
		if(getHolder() != null){
			getHolder().removeCallback(mSHCallback);
		}
		m_ActivityCallBack = null;
	}
	

	public boolean isValid() {
		return (mSurfaceHolder != null && mSurfaceHolder.getSurface().isValid());
	}

	public void setVideoPath(Uri path) {
		setVideoURI(path);
	}

	public void setVideoURI(Uri uri) {
		setVideoURI(uri, null);
	}

	public enum VideoFileType{
		HTTP,NORMAL,RTSP;
	}
	public VideoFileType m_lUriType = VideoFileType.NORMAL;
	
	public void setVideoURI(Uri uri, Map<String, String> headers) {
		mUri = uri;
		String s = mUri.toString();
		if(s.startsWith("rtsp://", 0)){
			m_lUriType = VideoFileType.RTSP;
		}else{
			if(s.startsWith("http://", 0) || s.startsWith("playlist", 0)){
				m_lUriType = VideoFileType.HTTP;
			}
			else{
				m_lUriType = VideoFileType.NORMAL;
			}
		}
		mHeaders = headers;
		mSeekWhenPrepared = 0;
		openVideo();
	}

	public void stopPlayback() {
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			ALog.i("*stopPlayback()*", "stopPlayback()");
			mMediaPlayer.reset();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			mTargetState = STATE_IDLE;
		}
	}

	/**初始化播放器   开始加载播放**/
	private void openVideo() {
		if (mUri == null || mSurfaceHolder == null
				|| !Vitamio.isInitialized(mContext))
			return;
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		mContext.sendBroadcast(i);
		if(mMediaPlayer!=null){
			if(mMediaPlayer.isPlaying()){
				mMediaPlayer.stop();
			}
			try {
				mMediaPlayer.setDisplay(mSurfaceHolder);
			} catch (Exception e) {
				ALog.i("==MediaPlayerView==", new StringBuilder(" The surface has been released!"+e.toString()).toString());
				stopPlayback();
				return;
			}
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setVideoChroma(MediaPlayer.VIDEOCHROMA_RGB565);
			if(m_lUriType == VideoFileType.RTSP || m_lUriType == VideoFileType.HTTP)
			{
				try {
					mMediaPlayer.prepareAsync();
				} catch (IllegalStateException e) {
					ALog.i("==MediaPlayerView==", new StringBuilder(e.toString()).toString());
					mCurrentState = STATE_ERROR;
					mTargetState = STATE_ERROR;
					mErrorListener.onError(mMediaPlayer,0, 0);
				}
			}else {
				try{
					mMediaPlayer.prepare();
				}
				catch(IllegalStateException illegalstateexception){
					ALog.i("==MediaPlayerView==", new StringBuilder(illegalstateexception.toString()).toString());
					mCurrentState = STATE_ERROR;
					mTargetState = STATE_ERROR;
					mErrorListener.onError(mMediaPlayer,0, 0);
				}catch(IOException ioexception1){
					ALog.i("==MediaPlayerView==", new StringBuilder(ioexception1.toString()).toString());
					mCurrentState = STATE_ERROR;
					mTargetState = STATE_ERROR;
					mErrorListener.onError(mMediaPlayer,0, 0);
				}
			}
			
			
		}else{
			release(false);
			try {
				mDuration = -1;
				mCurrentBufferPercentage = 0;
				mMediaPlayer = new MediaPlayer(mContext, mHardwareDecoder);
				mMediaPlayer.setOnPreparedListener(mPreparedListener);
				mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
				mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
				mMediaPlayer.setOnCompletionListener(mCompletionListener); 
				mMediaPlayer.setOnErrorListener(mErrorListener);
				mMediaPlayer.setOnInfoListener(mInfoListener);
//				mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
				mMediaPlayer.setDataSource(mContext, mUri, mHeaders);
				mMediaPlayer.setDisplay(mSurfaceHolder);
				mMediaPlayer.setBufferSize(mBufSize);
				mMediaPlayer.setVideoChroma(mVideoChroma == MediaPlayer.VIDEOCHROMA_RGB565 ? MediaPlayer.VIDEOCHROMA_RGB565
						: MediaPlayer.VIDEOCHROMA_RGBA);
				mMediaPlayer.setScreenOnWhilePlaying(true);
				mMediaPlayer.prepareAsync();
				mCurrentState = STATE_PREPARING;
			} catch (IOException ex) {
				ALog.i("Unable to open content: " + mUri, ex);
				mCurrentState = STATE_ERROR;
				mTargetState = STATE_ERROR;
				mErrorListener.onError(mMediaPlayer,MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
				return;
			} catch (IllegalArgumentException ex) {
				ALog.i("Unable to open content: " + mUri, ex);
				mCurrentState = STATE_ERROR;
				mTargetState = STATE_ERROR;
				mErrorListener.onError(mMediaPlayer,MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
				return;
			}
		}
	}


	public void setOnPreparedListener(OnPreparedListener l) {
		mOnPreparedListener = l;
	}

	private void release(boolean cleartargetstate) {
		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			if (cleartargetstate)
				mTargetState = STATE_IDLE;
		}
	}


	public void start() {
		if (isInPlaybackState() ) {
			mMediaPlayer.start();
			mCurrentState = STATE_PLAYING;
		}
		mTargetState = STATE_PLAYING;
	}

	public boolean canPause(){
		return mTargetState==STATE_PAUSED;
	}
	public void pause() {
		if (isInPlaybackState()) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				mCurrentState = STATE_PAUSED;
			}
		}
		mTargetState = STATE_PAUSED;
	}
	
	public void release(){
		if(isInPlaybackState()){
			mMediaPlayer.release();
		}
	}

	public void suspend() {
		if (isInPlaybackState()) {
			release(false);
			mCurrentState = STATE_SUSPEND_UNSUPPORTED;
			ALog.d("Unable to suspend video. Release MediaPlayer.");
		}
	}

	public void resume() {
		if (mSurfaceHolder == null && mCurrentState == STATE_SUSPEND) {
			mTargetState = STATE_RESUME;
		} else if (mCurrentState == STATE_SUSPEND_UNSUPPORTED) {
			openVideo();
		}
	}
	
	public void setLooping(boolean flag){
		if(mMediaPlayer != null){
			mMediaPlayer.setLooping(flag);
		}
	}
	
	public boolean isLooping(){
		if(mMediaPlayer!=null){
			return mMediaPlayer.isLooping();
		}
		return false;
	}

	public long getDuration() {
		if (isInPlaybackState()) {
			if (mDuration > 0)
				return mDuration;
			mDuration = mMediaPlayer.getDuration();
			return mDuration;
		}
		mDuration = -1;
		return mDuration;
	}

	public long getCurrentPosition() {
		if (isInPlaybackState())
			return mMediaPlayer.getCurrentPosition();
		return 0;
	}

	public void seekTo(long msec) {
		if (isInPlaybackState()) {
			mMediaPlayer.seekTo(msec);
			mSeekWhenPrepared = 0;
		} else {
			mSeekWhenPrepared = msec;
		}
	}

	public boolean isPlaying() {
		return isInPlaybackState() && mMediaPlayer.isPlaying();
	}

	public int getBufferPercentage() {
		if (mMediaPlayer != null)
			return mCurrentBufferPercentage;
		return 0;
	}

	public void setVolume(float leftVolume, float rightVolume) {
		if (mMediaPlayer != null)
			mMediaPlayer.setVolume(leftVolume, rightVolume);
	}

	public int getVideoWidth() {
		return mVideoWidth;
	}

	public int getVideoHeight() {
		return mVideoHeight;
	}

	public float getVideoAspectRatio() {
		return mVideoAspectRatio;
	}

	public Bitmap getShotScreenFrame(){ return mMediaPlayer.getCurrentFrame();}

	/**
	 * Must set before {@link #setVideoURI}
	 * 
	 * @param chroma
	 */
	public void setVideoChroma(int chroma) {
		getHolder().setFormat(
				chroma == MediaPlayer.VIDEOCHROMA_RGB565 ? PixelFormat.RGB_565
						: PixelFormat.RGBA_8888); // PixelFormat.RGB_565
		mVideoChroma = chroma;
	}

	public void setHardwareDecoder(boolean hardware) {
		mHardwareDecoder = hardware;
	}

	public void setVideoQuality(int quality) {
		if (mMediaPlayer != null)
			mMediaPlayer.setVideoQuality(quality);
	}

	public void setBufferSize(int bufSize) {
		mBufSize = bufSize;
	}

	public boolean isBuffering() {
		if (mMediaPlayer != null)
			return mMediaPlayer.isBuffering();
		return false;
	}

	public String getMetaEncoding() {
		if (mMediaPlayer != null)
			return mMediaPlayer.getMetaEncoding();
		return null;
	}

	public void setMetaEncoding(String encoding) {
		if (mMediaPlayer != null)
			mMediaPlayer.setMetaEncoding(encoding);
	}

	public SparseArray<MediaFormat> getAudioTrackMap(String encoding) {
		if (mMediaPlayer != null)
			return mMediaPlayer.findTrackFromTrackInfo(
					TrackInfo.MEDIA_TRACK_TYPE_AUDIO,
					mMediaPlayer.getTrackInfo(encoding));
		return null;
	}

	public int getAudioTrack() {
		if (mMediaPlayer != null)
			return mMediaPlayer.getAudioTrack();
		return -1;
	}

	public void setAudioTrack(int audioIndex) {
		if (mMediaPlayer != null)
			mMediaPlayer.selectTrack(audioIndex);
	}

	public void setTimedTextShown(boolean shown) {
		if (mMediaPlayer != null)
			mMediaPlayer.setTimedTextShown(shown);
	}

	public void setTimedTextEncoding(String encoding) {
		if (mMediaPlayer != null)
			mMediaPlayer.setTimedTextEncoding(encoding);
	}

	public int getTimedTextLocation() {
		if (mMediaPlayer != null)
			return mMediaPlayer.getTimedTextLocation();
		return -1;
	}

	public void addTimedTextSource(String subPath) {
		if (mMediaPlayer != null)
			mMediaPlayer.addTimedTextSource(subPath);
	}

	public String getTimedTextPath() {
		if (mMediaPlayer != null)
			return mMediaPlayer.getTimedTextPath();
		return null;
	}

	public void setSubTrack(int trackId) {
		if (mMediaPlayer != null)
			mMediaPlayer.selectTrack(trackId);
	}

	public int getTimedTextTrack() {
		if (mMediaPlayer != null)
			return mMediaPlayer.getTimedTextTrack();
		return -1;
	}

	public SparseArray<MediaFormat> getSubTrackMap(String encoding) {
		if (mMediaPlayer != null)
			return mMediaPlayer.findTrackFromTrackInfo(
					TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT,
					mMediaPlayer.getTrackInfo(encoding));
		return null;
	}

	protected boolean isInPlaybackState() {
		return (mMediaPlayer != null && mCurrentState != STATE_ERROR
				&& mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
	}

}