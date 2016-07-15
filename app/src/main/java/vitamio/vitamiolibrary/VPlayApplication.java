package vitamio.vitamiolibrary;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import io.vov.vitamio.Vitamio;

/**
 * Created by aoe on 2016/3/25.
 */
public class VPlayApplication extends Application{

    private static VPlayApplication boPlayApplication;

    private static Context mContext;

    /**
     * 是否调试模式 true 调试模式，显示日志 false 发布模式，关闭日志等调试信息
     */
    public static boolean isDebug;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        isDebug = isApkDebugable(boPlayApplication);
        initializeVideoView();
    }

    public static VPlayApplication getApplication() {
        if (boPlayApplication == null){
            initialize();
        }
        return boPlayApplication;
    }

    private static void initialize() {
        boPlayApplication = new VPlayApplication();
        boPlayApplication.onCreate();
    }

    public static Context getContext() {
        return boPlayApplication;

    }

    public static VPlayApplication getInstance() {
        return boPlayApplication;
    }


    private static boolean isApkDebugable(Context context) {
        try {
            ApplicationInfo info= context.getApplicationInfo();
            return (info.flags&ApplicationInfo.FLAG_DEBUGGABLE)!=0;
        } catch (Exception e) {

        }
        return false;
    }


    /** 初始化软解SO文件 **/
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

}
