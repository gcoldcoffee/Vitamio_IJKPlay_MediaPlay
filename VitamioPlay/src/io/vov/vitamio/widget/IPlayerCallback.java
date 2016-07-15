package io.vov.vitamio.widget;


public abstract interface IPlayerCallback {
	/**加载完成**/
	public abstract void onBufferingEnd();

	/**开始加载**/
	public abstract void onBufferingStart();
	
	/**播放不流畅   声画不同步**/
	public abstract void onTackLagging();

	/**当前加载返回值**/
	public abstract void onBufferingback(int paramInt);

	/**当前播放完成  开始播放下一个**/
	public abstract void onCompletePlayback();

	/**播放出错**/
	public abstract void onErrorAppeared(Global.VideoPlayerError paramVideoPlayerError);

	/**播放器准备完成**/
	public abstract void onPreparedPlayback();

	/**播放器画面已准备 设置播放比例**/
	public abstract void onSetVideoViewLayout();

	/**播放器SurfaceView 已创建**/
	public abstract void onSurfaceCreated(boolean paramBoolean);
	
}
