# Vitamio_IJKPlay_MediaPlay
可播放在线视频，本地视频，直播，切换画面比例，软硬解码切换，支持安卓手机，盒子播放，播放器截图.

Demo目前还不完善，但功能都有，后续会补上
- 增加列表小窗口播放可拖拽移动

Vitamio官网(https://www.vitamio.org/)

Vitamio SDK(https://www.vitamio.org/Download/)

IJKPlay SDK(http://github.com/Bilibili/ijkplayer )

列表播放VideoListDemo(https://github.com/w1123440793/VideoListDemo)

    /** Vitamio 需要初始化软解SO文件 **/
    private void initializeVideoView() {
        new Thread() {
            public void run() {
                try {
                   Vitamio.initialize(mContext,
                            getResources().getIdentifier("libarm", "raw", getPackageName()));
                }catch (UnsatisfiedLinkError e){
                    e.printStackTrace();
                }
            }
        }.start();
    }


![image](https://github.com/gcoldcoffee/Vitamio_IJKPlay_MediaPlay/blob/master/image/video.gif)