package vitamio.vitamiolibrary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.ijkplay.view.IjkVideoView;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import vitamio.vitamiolibrary.utils.SystemApi;
import vitamio.vitamiolibrary.utils.ViewUtils;
import vitamio.vitamiolibrary.vieoData.VideoListData;

/**
 * 使用ijkPlayer 实现 RecycleView 列表点击播放
 */
public class IjkMediaListActivity extends AppCompatActivity {
    private String TAG=IjkMediaListActivity.class.getSimpleName();

    private Activity mActivity;

    //播放需要
    private VideoPlayView videoItemView;
    private ViewGroup listParent;//记录列表中item的父布局

    //播放列表
    private RecyclerView videoListRecyclerView;
    private VideoListData listData;
    private LinearLayoutManager mLayoutManager;
    private VideoAdapter adapter;

    //记录RecycleView 当前显示的item位置信息
    private int postion = -1;
    private int lastPostion=-1;

    //小窗口播放
    private RelativeLayout small_layout;
    private FrameLayout small_layout_video;
    private ImageView img_close;

    //全屏播放
    private FrameLayout full_screen;
    private boolean isFull=false;
    private boolean isCloseFloat=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ijk_media_list);
        mActivity=this;
        if (videoItemView==null){
            videoItemView=new VideoPlayView(mActivity);
        }

        getSupportActionBar().hide();
        initView();

    }

    private void initView(){
        small_layout=(RelativeLayout)findViewById(R.id.small_layout);
        small_layout_video=(FrameLayout)findViewById(R.id.small_layout_video);
        img_close=(ImageView)findViewById(R.id.img_close);
        img_close.setOnClickListener(new MyOnClickListener());

        full_screen=(FrameLayout)findViewById(R.id.full_screen);

        mLayoutManager = new LinearLayoutManager(this);
        videoListRecyclerView= (RecyclerView) findViewById(R.id.videoListRecyclerView);
        videoListRecyclerView.setLayoutManager(mLayoutManager);
        videoListRecyclerView.addOnChildAttachStateChangeListener(new MyOnChildAttachStateChangeListener());

        videoItemView.setCompletionListener(new MyVideoOnCompleteListener());

        initVideoListData();

        setFull();

    }

    private void initVideoListData(){
        String data = readTextFileFromRawResourceId(this, R.raw.video_list);
        listData = new Gson().fromJson(data, VideoListData.class);
        adapter=new VideoAdapter(this);
        adapter.refresh(listData.getList());
        videoListRecyclerView.setAdapter(adapter);

        adapter.setClick(new MyAdapterOnClick());
    }

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    private class MyOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.img_close:
                    if (videoItemView.isPlay()){
                        small_layout.setVisibility(View.GONE);
                        isCloseFloat=true;
                        videoItemView.stop();
                    }
                    break;
                case R.id.small_layout:
                    small_layout.setVisibility(View.GONE);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
            }
        }
    }

    private class MyVideoOnCompleteListener implements VideoPlayView.CompletionListener{
        @Override
        public void completion(IMediaPlayer mp) {
            FrameLayout frameLayout = (FrameLayout) videoItemView.getParent();
            if (frameLayout != null && frameLayout.getChildCount() > 0) {
                View itemView = (View) frameLayout.getParent();
                if (itemView != null) {
                    if(itemView.findViewById(R.id.showview)!=null){
                        itemView.findViewById(R.id.showview).setVisibility(View.VISIBLE);
                    }
                }
                frameLayout.removeAllViews();
            }

            lastPostion = -1;
            videoItemView.setShowContoller(true);
            videoItemView.release();

            //播放完还原播放界面
            if(isFull){
                resolveChangeFirstLogic(10);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resolveMaterialToNormal();
                    }
                },300);
            }else{
                if (small_layout.getVisibility() == View.VISIBLE) {
                    small_layout_video.removeAllViews();
                    small_layout.setVisibility(View.GONE);
                }
            }

        }
    }


    private class MyAdapterOnClick implements VideoAdapter.onClick{

        @Override
        public void onclick(int position) {
            IjkMediaListActivity.this.postion = position;

            if (videoItemView.VideoStatus() == IjkVideoView.STATE_PAUSED){
                if (position!=lastPostion) {
                    videoItemView.stop();
                    videoItemView.release();
                }
            }
            if(small_layout.getVisibility()==View.VISIBLE)
            {
                small_layout.setVisibility(View.GONE);
                small_layout_video.removeAllViews();
                videoItemView.setShowContoller(true);
            }
            videoListRecyclerView.findViewHolderForAdapterPosition(postion).itemView.findViewById(R.id.showview).setVisibility(View.GONE);

            if(lastPostion!=-1){
                ViewGroup last = (ViewGroup) videoItemView.getParent();//找到videoitemview的父类，然后remove
                if (last != null) {
                    last.removeAllViews();
                    View itemView = (View) last.getParent();
                    if (itemView != null) {
                        if(itemView.findViewById(R.id.showview)!=null){
                            itemView.findViewById(R.id.showview).setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            View view = videoListRecyclerView.findViewHolderForAdapterPosition(postion).itemView;
            FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.layout_video);
            frameLayout.removeAllViews();
            frameLayout.addView(videoItemView);
            listParent=(ViewGroup)view;
//            videoItemView.start(listData.getList().get(position).getMp4_url());
            videoItemView.setShowContoller(true);
            videoItemView.start(listData.getList().get(position).getM3u8_url());
            lastPostion=position;
            resolveMaterialFullVideoShow();
        }
    }


    private class MyOnChildAttachStateChangeListener implements RecyclerView.OnChildAttachStateChangeListener{

        @Override
        public void onChildViewAttachedToWindow(View view) {
            int index = videoListRecyclerView.getChildAdapterPosition(view);
            view.findViewById(R.id.showview).setVisibility(View.VISIBLE);
            if (index == postion) {
                FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.layout_video);
                frameLayout.removeAllViews();
                if (videoItemView != null &&
                        ((videoItemView.isPlay())||videoItemView.VideoStatus()== IjkVideoView.STATE_PAUSED)) {
                    if(isCloseFloat){
                        view.findViewById(R.id.showview).setVisibility(View.VISIBLE);
                    }else{
                        view.findViewById(R.id.showview).setVisibility(View.GONE);
                    }
                }
                isCloseFloat=false;

                if (videoItemView.VideoStatus()== IjkVideoView.STATE_PAUSED){
                    if (videoItemView.getParent()!=null)
                        ((ViewGroup)videoItemView.getParent()).removeAllViews();
                    frameLayout.addView(videoItemView);
                    return;
                }

                if (videoItemView != null && videoItemView.isPlay()) {
                    small_layout.setVisibility(View.GONE);
                    small_layout_video.removeAllViews();
                    videoItemView.setShowContoller(true);
                    frameLayout.addView(videoItemView);
                }
            }
        }

        @Override
        public void onChildViewDetachedFromWindow(View view) {
            int index = videoListRecyclerView.getChildAdapterPosition(view);
            if (index == postion) {
                FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.layout_video);
                frameLayout.removeAllViews();
                if (videoItemView != null
                        && videoItemView.isPlay()) {
                    full_screen.setVisibility(View.GONE);
                    small_layout.setVisibility(View.VISIBLE);
                    small_layout_video.removeAllViews();
                    videoItemView.setShowContoller(false);
                    small_layout_video.addView(videoItemView);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //update 窗口拖拽移动 2016,11,28 16:12pm
                            SmallVideoTouch smallVideoTouch=new SmallVideoTouch(small_layout,
                                    ViewUtils.getScreenWidth(mActivity)-small_layout_video.getWidth(),
                                    ViewUtils.getScreenHeight(mActivity)-small_layout_video.getHeight()-ViewUtils.getStatusBarHeight(mActivity));
                            small_layout.setOnTouchListener(smallVideoTouch);
                            videoItemView.setOnTouchListener(smallVideoTouch);
                            img_close.setOnTouchListener(null);
                        }
                    },300);
                }
            }
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(small_layout_video==null)
            return;
        if (small_layout.getVisibility()== View.VISIBLE){
            small_layout.setVisibility(View.GONE);
            small_layout_video.removeAllViews();
        }

        if (postion!=-1){
            ViewGroup view= (ViewGroup) videoItemView.getParent();
            if (view!=null){
                view.removeAllViews();
            }
        }
        videoItemView.stop();
        videoItemView.release();
        videoItemView.onDestroy();
        videoItemView=null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoItemView!=null){
            videoItemView.stop();
        }
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        if (videoItemView!=null){
//            videoItemView.onChanged(newConfig);
//            if (newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
//
//                getSupportActionBar().show();
//                full_screen.setVisibility(View.GONE);
//                videoListRecyclerView.setVisibility(View.VISIBLE);
//                full_screen.removeAllViews();
//                if (postion<=mLayoutManager.findLastVisibleItemPosition()
//                        &&postion>=mLayoutManager.findFirstVisibleItemPosition()) {
//                    View view = videoListRecyclerView.findViewHolderForAdapterPosition(postion).itemView;
//                    FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.layout_video);
//                    frameLayout.removeAllViews();
//                    frameLayout.addView(videoItemView);
//                    videoItemView.setShowContoller(true);
//                }else {
//                    small_layout_video.removeAllViews();
//                    small_layout_video.addView(videoItemView);
//                    videoItemView.setShowContoller(false);
//                    small_layout.setVisibility(View.VISIBLE);
//                }
//                videoItemView.setContorllerVisiable();
//            }else {
//                getSupportActionBar().hide();
//                ViewGroup viewGroup= (ViewGroup) videoItemView.getParent();
//                if (viewGroup==null)
//                    return;
//                viewGroup.removeAllViews();
//                full_screen.addView(videoItemView);
//                small_layout.setVisibility(View.GONE);
//                videoListRecyclerView.setVisibility(View.GONE);
//                full_screen.setVisibility(View.VISIBLE);
//            }
//        }else {
//            adapter.notifyDataSetChanged();
//            videoListRecyclerView.setVisibility(View.VISIBLE);
//            full_screen.setVisibility(View.GONE);
//        }
//    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            if (getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }


    public String readTextFileFromRawResourceId(Context context, int resourceId) {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(resourceId)));
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                builder.append(line).append("\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return builder.toString();
    }

    private int[] listItemRect;//当前item框的屏幕位置
    private int[] listItemSize;//当前item的大小
    private void setFull(){
        videoItemView.getMediaController().getFull().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFull){

                    resolveChangeFirstLogic(10);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resolveMaterialToNormal();
                        }
                    },300);
                }else{
                    resolveMaterialAnimation();
                }
            }
        });
    }

    /**
     * 放大
     * 如果是5.0的动画开始位置
     */
    private void resolveMaterialAnimation() {
        listItemRect = new int[2];
        listItemSize = new int[2];
        ViewGroup viewGroup= (ViewGroup) videoItemView.getParent();
        if (viewGroup != null) {
            viewGroup.removeAllViews();
        }
        isFull=true;
        saveLocationStatus(mActivity, true, true);
        FrameLayout.LayoutParams lpParent = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout frameLayout = new FrameLayout(mActivity);
        lpParent.gravity=Gravity.CENTER;
        frameLayout.setBackgroundColor(Color.BLACK);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(listItemSize[0], listItemSize[1]);
        lp.setMargins(listItemRect[0], listItemRect[1], 0, 0);
        frameLayout.addView(videoItemView, lp);
        full_screen.addView(frameLayout, lpParent);
        small_layout.setVisibility(View.GONE);
        videoListRecyclerView.setVisibility(View.GONE);
        full_screen.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //开始动画
                TransitionManager.beginDelayedTransition(full_screen);
                resolveMaterialFullVideoShow();
//                resolveChangeFirstLogic(300);
            }
        }, 300);

    }

    /**
     * 如果是5.0的，要从原位置过度到全屏位置
     */
    private void resolveMaterialFullVideoShow() {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) videoItemView.getLayoutParams();
        lp.setMargins(0, 0, 0, 0);
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.CENTER;
        videoItemView.setLayoutParams(lp);
    }

    /**
     * 保存大小和状态
     */
    private void saveLocationStatus(Context context, boolean statusBar, boolean actionBar) {
        listParent.getLocationOnScreen(listItemRect);
        int statusBarH = ViewUtils.getStatusBarHeight(context);
        int actionBerH = ViewUtils.getActionBarHeight((Activity) context);
        if (statusBar) {
            listItemRect[1] = listItemRect[1] - statusBarH;
        }
        if (actionBar) {
            listItemRect[1] = listItemRect[1] - actionBerH;
        }
        listItemSize[0] = listParent.getWidth();
        listItemSize[1] = listParent.getHeight();
    }

    /**
     * 缩小
     *
     * 动画回到正常效果
     */
    private void resolveMaterialToNormal() {
        if (full_screen instanceof FrameLayout) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    TransitionManager.beginDelayedTransition(full_screen);
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) videoItemView.getLayoutParams();
                    lp.setMargins(listItemRect[0], listItemRect[1], 0, 0);
                    lp.width = listItemSize[0];
                    lp.height = listItemSize[1];
                    //注意配置回来，不然动画效果会不对
                    lp.gravity = Gravity.NO_GRAVITY;
                    videoItemView.setLayoutParams(lp);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resolveToNormal();

                        }
                    }, 300);
                }
            }, 100);
        } else {
            resolveToNormal();
        }
    }

    private void resolveToNormal(){
        isFull=false;
        full_screen.removeAllViews();
        full_screen.setVisibility(View.GONE);
        videoListRecyclerView.setVisibility(View.VISIBLE);
        if (videoItemView.getParent() != null) {
            ((ViewGroup) videoItemView.getParent()).removeView(videoItemView);
        }
        if(videoItemView.mediaCanPlay()){
            if (postion<=mLayoutManager.findLastVisibleItemPosition()
                    &&postion>=mLayoutManager.findFirstVisibleItemPosition()) {
                View view = videoListRecyclerView.findViewHolderForAdapterPosition(postion).itemView;
                FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.layout_video);
                frameLayout.removeAllViews();
                frameLayout.addView(videoItemView);
                videoItemView.setShowContoller(true);
                resolveMaterialFullVideoShow();
            }
            videoItemView.setContorllerVisiable();
        }else{
            View view = videoListRecyclerView.findViewHolderForAdapterPosition(postion).itemView;
            view.findViewById(R.id.showview).setVisibility(View.VISIBLE);
        }

    }

    /**
     * 是否全屏一开始马上自动横屏
     */
    private void resolveChangeFirstLogic(int time) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                resolveByClick();
            }
        }, time);
    }

    /**
     * 点击切换的逻辑，比如竖屏的时候点击了就是切换到横屏不会受屏幕的影响
     */
    private int mIsLand=-1,screenType;
    public void resolveByClick() {
        if (mIsLand == 0) {
            screenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mIsLand = 1;
        } else if(mIsLand==1){
            screenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mIsLand = 0;
        }

    }


}
