package vitamio.vitamiolibrary.videos.mediaView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;

import vitamio.vitamiolibrary.videos.utils.Alog;

public class MediaPlayerView extends BasePlayerView {

	private static final String TAG = Alog.registerMod("MediaPlayerView");
	private MediaPlayer m_MediaPlayer;


	public MediaPlayerView(Context context) {
		super(context);
		Alog.e(TAG, "MediaPlayerView construction 1");
	}

	public MediaPlayerView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		m_MediaPlayer = null;
		Alog.e(TAG, "MediaPlayerView construction 2");
	}

	public MediaPlayerView(Context context, AttributeSet attributeset, int defStyle)
	{
		super(context, attributeset, defStyle);
		m_MediaPlayer = null;
		Alog.e(TAG, "MediaPlayerView construction 3");
	}

	@SuppressLint("NewApi")
	private boolean toSetDataSource(Context context, MediaPlayer mediaplayer, Uri uri)
	{
		boolean flag = false;
		try {
			if(headers==null || headers.isEmpty()){
				mediaplayer.setDataSource(context, m_uri);
			}else {
				mediaplayer.setDataSource(context, m_uri, headers);
			}
			flag = true;
		} catch (IllegalArgumentException e) {
			flag = false;
			e.printStackTrace();
		} catch (SecurityException e) {
			flag = false;
			e.printStackTrace();
		} catch (IllegalStateException e) {
			flag = false;
			e.printStackTrace();
		} catch (IOException e) {
			flag = false;
			e.printStackTrace();
		}

		return flag;
	}

	@Override
	public int getCurrentPosition()
	{
		int curPosition;

		if(m_MediaPlayer != null && m_isPrepared)
			curPosition = m_MediaPlayer.getCurrentPosition();
		else
			curPosition = m_iSeekPosWhenPrepared;

		return curPosition;
	}

	@Override
	public int getDuration()
	{
		int duration = 0;

		if(m_MediaPlayer != null && m_isPrepared)
			duration = m_iDuration;

		return duration;
	}

	@Override
	public int getVideoHeight()
	{
		if(m_iVideoHeight == 0 && m_MediaPlayer != null)
			m_iVideoHeight = m_MediaPlayer.getVideoHeight();

		return m_iVideoHeight;
	}

	@Override
	public int getVideoWidth()
	{
		if(m_iVideoWidth == 0 && m_MediaPlayer != null)
			m_iVideoWidth = m_MediaPlayer.getVideoWidth();

		return m_iVideoWidth;
	}

	@Override
	public boolean isPlaying()
	{
		boolean isPlaying = false;

		if(m_MediaPlayer != null && m_isPrepared)
			isPlaying = m_MediaPlayer.isPlaying();

		return isPlaying;
	}

	@Override
	public boolean isStopped()
	{
		boolean isStopped = true;

		if(m_MediaPlayer != null && m_isPrepared)
			isStopped = m_isStopped;

		return isStopped;
	}

	@Override
	public void openMediaFile()
	{
		if(m_uri == null) {
			return;
		}

		Intent intent = new Intent("com.android.music.musicservicecommand");
		intent.putExtra("command", "pause");
		m_context.sendBroadcast(intent);

		m_isPrepared = false;
		if(m_MediaPlayer != null)
		{
			m_MediaPlayer.reset();
			if(!toSetDataSource(m_context, m_MediaPlayer, m_uri))
			{
				Alog.e(TAG, (new StringBuilder("toSetDataSource 2 error:")).append(m_uri.toString()).toString());
				onError(m_MediaPlayer, 0, 0);
			}
			else
			{
				try {
					m_MediaPlayer.setDisplay(getHolder());
					getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
				} catch (Exception e) {
					stopPlayback();
					Log.i("==MediaPlayerView==", new StringBuilder(" The surface has been released!"+e.toString()).toString());
					return;
				}
				m_MediaPlayer.setOnBufferingUpdateListener(this);
				m_MediaPlayer.setOnVideoSizeChangedListener(this);
				m_MediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				if(m_lUriType == VideoFileType.RTSP || m_lUriType == VideoFileType.HTTP)
				{
					try {
						m_MediaPlayer.prepareAsync();
					} catch (Exception e) {
						Log.i("==MediaPlayerView==", new StringBuilder(e.toString()).toString());
					}
				} else{
					try{
						m_MediaPlayer.prepare();
					}
					catch(IllegalStateException illegalstateexception){
						onError(m_MediaPlayer, 0, 0);
					}catch(IOException ioexception1){
						onError(m_MediaPlayer, 0, 0);
					}
				}
			}
		} else {
			m_MediaPlayer = new MediaPlayer();
			if(!toSetDataSource(m_context, m_MediaPlayer, m_uri))
			{
				Alog.e(TAG, (new StringBuilder("toSetDataSource 2 error:")).append(m_uri.toString()).toString());
				onError(m_MediaPlayer, 0, 0);
			}

			if(m_MediaPlayer != null) {
				m_MediaPlayer.setOnPreparedListener(this);
				m_MediaPlayer.setOnCompletionListener(this);
				m_MediaPlayer.setOnErrorListener(this);
				m_MediaPlayer.setOnVideoSizeChangedListener(this);
				m_MediaPlayer.setOnBufferingUpdateListener(this);
				m_MediaPlayer.setOnVideoSizeChangedListener(this);
				try {
					m_MediaPlayer.setDisplay(getHolder());
				} catch (Exception e) {
					stopPlayback();
					Log.i("==MediaPlayerView==", new StringBuilder(" The surface has been released!"+e.toString()).toString());
					return;
				}
				m_MediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				m_MediaPlayer.setScreenOnWhilePlaying(true);
				m_MediaPlayer.setOnInfoListener(this);
				if(m_lUriType == VideoFileType.RTSP || m_lUriType == VideoFileType.HTTP)
				{
					try {
						m_MediaPlayer.prepareAsync();
					} catch (Exception e) {
						Log.i("==MediaPlayerView==", new StringBuilder(e.toString()).toString());
					}
				} else {
					try{
						m_MediaPlayer.prepare();
					}catch(IOException e){
						onError(m_MediaPlayer, 0, 0);
					}catch(IllegalArgumentException e){
						onError(m_MediaPlayer, 0, 0);
					}
				}
			}
		}
	}

	@Override
	public void pause()
	{
		Alog.i(TAG, "pause");
		if(m_MediaPlayer != null && m_isPrepared)
		{
			m_MediaPlayer.pause();
			m_isPaused = true;
		}
	}

	@Override
	public void release() {
		Alog.i(TAG, "release");
		if(m_MediaPlayer != null)
		{
			m_MediaPlayer.release();
		}
	}


	@Override
	public void play()
	{
		if(m_MediaPlayer != null && m_isPrepared){
			m_MediaPlayer.start();
			m_isStopped = false;
			m_isPaused = false;
		}
	}

	@Override
	public void setLooping(boolean flag) {
		if(m_MediaPlayer != null && m_isPrepared){
			m_MediaPlayer.setLooping(flag);
		}
	}

	@Override
	public boolean isLoop() {
		return m_MediaPlayer.isLooping();
	}


	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {

		if(m_ActivityCallBack == null) {
			return true;
		}
		switch (what) {
			case 1:
				Alog.e("JPlayer2", (new StringBuilder("Unknown error, extra is "))
						.append(extra).toString());
				m_ActivityCallBack.onErrorAppeared(io.vov.vitamio.widget.Global.VideoPlayerError.EXCEPTION_ERROR);
				break;
			case 100:
				Alog.e("JPlayer2", (new StringBuilder(
						"Media Server died, extra is ")).append(extra).toString());
				m_ActivityCallBack.onErrorAppeared(io.vov.vitamio.widget.Global.VideoPlayerError.NETWORK_ERROR);
				break;
			case 200:
				Alog.e("JPlayer2", (new StringBuilder("File not valid for progressive playback, extra is ")).append(extra).toString());
				m_ActivityCallBack.onErrorAppeared(io.vov.vitamio.widget.Global.VideoPlayerError.UNSUPPORT_FILE_ERROR);
				break;
			case 300:
				m_ActivityCallBack.onErrorAppeared(io.vov.vitamio.widget.Global.VideoPlayerError.UNSUPPORT_FILE_ERROR);
				Alog.e("JPlayer2",(new StringBuilder("Codec unsupport, extra is ")).append(extra).toString());
				break;
			default:
				Alog.e("JPlayer2", (new StringBuilder("TNND, what error is ")).append(what).append("? code is ").append(extra).toString());
				m_ActivityCallBack.onErrorAppeared(io.vov.vitamio.widget.Global.VideoPlayerError.EXCEPTION_ERROR);
				break;

		}
		stopPlayback();

		return true;
	}

	@Override
	public void seekTo(final int seekTo)
	{
		Alog.i(TAG, "seekTo");
		if(m_MediaPlayer != null && m_isPrepared) {
			m_MediaPlayer.seekTo(seekTo);
		}
		else
			m_iSeekPosWhenPrepared = seekTo;
	}

	@Override
	public void stopPlayback()
	{
		Alog.i(TAG, "stopPlayback");
		if(m_MediaPlayer != null)
		{
			if(m_MediaPlayer.isPlaying()){
				m_MediaPlayer.stop();
			}
			m_MediaPlayer.release();
			m_MediaPlayer = null;
		}
		super.stopPlayback();
	}

	public MediaPlayer getMediaPlayer(){
		if(m_MediaPlayer!=null){
			return m_MediaPlayer;
		}
		return null;
	}

	@Override
	public boolean onInfo(MediaPlayer mediaplayer, int what, int extra) {
		switch (what) {
			case 1://MediaPlayer.MEDIA_INFO_UNKNOWN
				Alog.w("JPlayer2", (new StringBuilder("Unknown info, extra is "))
						.append(extra).toString());
				break;
			case 700://MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING 无法解码：不能快速解码帧。此时可能只能正常播放音频
				Alog.w("JPlayer2", (new StringBuilder(
						"It's too complex for the decoder, extra is ")).append(extra)
						.toString());
				if(m_ActivityCallBack!=null){
					m_ActivityCallBack.onTackLagging();
				}
				break;
			case MediaPlayer.MEDIA_INFO_BUFFERING_START:
				Alog.i("JPlayer2", "Buffering start");
				if (m_ActivityCallBack != null){
					m_ActivityCallBack.onBufferingStart();
				}
				break;
			case MediaPlayer.MEDIA_INFO_BUFFERING_END:
				Alog.i("JPlayer2", "Buffering end, start play");
				if(m_ActivityCallBack!=null){
					m_ActivityCallBack.onBufferingEnd();
				}
				break;
			case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
				Alog.i("JPlayer2", "MEDIA_INFO_VIDEO_RENDERING_START");
				break;
			case 800://MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING 不正确的交叉存储技术意味着媒体被不适当的交叉存储或者根本就没有交叉存储，例子里面有所有的视频和音频例子。
				Alog.e("JPlayer2",
						(new StringBuilder(
								"Bad interleaving of media file, audio/video are not well-formed, extra is "))
								.append(extra).toString());
				break;
			case 801://MediaPlayer.MEDIA_INFO_NOT_SEEKABLE 媒体位置不可查找
				Alog.e("JPlayer2", (new StringBuilder(
						"The stream cannot be seeked, extra is ")).append(extra)
						.toString());
				break;
			case MediaPlayer.MEDIA_INFO_METADATA_UPDATE://MediaPlayer.MEDIA_INFO_METADATA_UPDATE 一套新的可用的元数据
				Alog.w("JPlayer2", (new StringBuilder(
						"A new set of metadata is available, extra is ")).append(extra)
						.toString());
				break;
			default:
				Alog.i("JPlayer2", (new StringBuilder("Unknown info code: "))
						.append(what).append(", extra is ").append(extra).toString());
				break;
		}
		return super.onInfo(mediaplayer, what, extra);
	}

}
