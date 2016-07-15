package vitamio.vitamiolibrary;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.ijkplay.view.IjkVideoView;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import vitamio.vitamiolibrary.vieoData.VideoListData;

/**
 * 使用ijkPlayer 实现 RecycleView 列表点击播放
 */
public class IjkMediaListActivity extends AppCompatActivity {
    private String TAG=IjkMediaListActivity.class.getSimpleName();

    private Activity mActivity;

    //播放需要
    private VideoPlayView videoItemView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ijk_media_list);
        mActivity=this;
        if (videoItemView==null){
            videoItemView=new VideoPlayView(mActivity);
        }
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
                        videoItemView.pause();
                        small_layout.setVisibility(View.GONE);
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
            //播放完还原播放界面
            if (small_layout.getVisibility() == View.VISIBLE) {
                small_layout_video.removeAllViews();
                small_layout.setVisibility(View.GONE);
                videoItemView.setShowContoller(true);
            }

            FrameLayout frameLayout = (FrameLayout) videoItemView.getParent();
            videoItemView.release();
            if (frameLayout != null && frameLayout.getChildCount() > 0) {
                frameLayout.removeAllViews();
                View itemView = (View) frameLayout.getParent();

                if (itemView != null) {
                    itemView.findViewById(R.id.showview).setVisibility(View.VISIBLE);
                }
            }
            lastPostion = -1;
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

            if(lastPostion!=-1)

            {
                ViewGroup last = (ViewGroup) videoItemView.getParent();//找到videoitemview的父类，然后remove
                if (last != null) {
                    last.removeAllViews();
                    View itemView = (View) last.getParent();
                    if (itemView != null) {
                        itemView.findViewById(R.id.showview).setVisibility(View.VISIBLE);
                    }
                }
            }

            View view = videoListRecyclerView.findViewHolderForAdapterPosition(postion).itemView;
            FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.layout_video);
            frameLayout.removeAllViews();
            frameLayout.addView(videoItemView);
//            videoItemView.start(listData.getList().get(position).getMp4_url());
            videoItemView.start(listData.getList().get(position).getM3u8_url());
            lastPostion=position;
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
                    view.findViewById(R.id.showview).setVisibility(View.GONE);
                }

                if (videoItemView.VideoStatus()==IjkVideoView.STATE_PAUSED){
                    if (videoItemView.getParent()!=null)
                        ((ViewGroup)videoItemView.getParent()).removeAllViews();
                    frameLayout.addView(videoItemView);
                    return;
                }

                if (small_layout.getVisibility() == View.VISIBLE && videoItemView != null && videoItemView.isPlay()) {
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
                if (small_layout.getVisibility() == View.GONE && videoItemView != null
                        && videoItemView.isPlay()) {
                    small_layout.setVisibility(View.VISIBLE);
                    small_layout_video.removeAllViews();
                    videoItemView.setShowContoller(false);
                    small_layout_video.addView(videoItemView);
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (videoItemView!=null){
            videoItemView.onChanged(newConfig);
            if (newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
                full_screen.setVisibility(View.GONE);
                videoListRecyclerView.setVisibility(View.VISIBLE);
                full_screen.removeAllViews();
                if (postion<=mLayoutManager.findLastVisibleItemPosition()
                        &&postion>=mLayoutManager.findFirstVisibleItemPosition()) {
                    View view = videoListRecyclerView.findViewHolderForAdapterPosition(postion).itemView;
                    FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.layout_video);
                    frameLayout.removeAllViews();
                    frameLayout.addView(videoItemView);
                    videoItemView.setShowContoller(true);
                }else {
                    small_layout_video.removeAllViews();
                    small_layout_video.addView(videoItemView);
                    videoItemView.setShowContoller(false);
                    small_layout.setVisibility(View.VISIBLE);
                }
                videoItemView.setContorllerVisiable();
            }else {
                ViewGroup viewGroup= (ViewGroup) videoItemView.getParent();
                if (viewGroup==null)
                    return;
                viewGroup.removeAllViews();
                full_screen.addView(videoItemView);
                small_layout.setVisibility(View.GONE);
                videoListRecyclerView.setVisibility(View.GONE);
                full_screen.setVisibility(View.VISIBLE);
            }
        }else {
            adapter.notifyDataSetChanged();
            videoListRecyclerView.setVisibility(View.VISIBLE);
            full_screen.setVisibility(View.GONE);
        }
    }

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

}
