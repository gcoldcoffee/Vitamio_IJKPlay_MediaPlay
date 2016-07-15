package vitamio.vitamiolibrary.videos.mediaimpl;

/**
 * Created by aoe on 2015/12/24.
 */
public class VPImpl {

    /**
     * 截屏
     */
    public interface VPShotScreen{
        public void shotScreen(boolean flag, String name, String filepath);
    }

    /**
     * 切换画面比例
     */
    public interface ScaleImpl {
        public void show();
        public void dismiss();
        public void onScale(int scale);
    }

    /**
     * 截屏发布动态
     * 网络断开重连
     */
    public interface DialogImpl {
        public void show();
        public void dismiss();
        public void onPlayOrBackfinish(boolean flag);//not find wifi 是否继续播放  false退出不观看  true继续观看
        public void onShotScreenPublish(boolean flag);//截屏成功跳转发布页面
    }


}
