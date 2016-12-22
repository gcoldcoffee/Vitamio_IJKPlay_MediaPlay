package vitamio.vitamiolibrary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import vitamio.vitamiolibrary.videos.utils.BoTools;
import vitamio.vitamiolibrary.videos.utils.PlayUtils;

/**
 *
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    String onlive_url="rtmp://live.hkstv.hk.lxdns.com/live/hks";
    String video_url="http://mvvideo1.meitudata.com/56f3622653a742935.mp4";

    private Activity mActivity;

    private EditText edit_url;

    //vitamio
    private Button btn_vitamio_play_video,btn_vitamio_play_onlive;

    //ijk
    private Button btn_ijk_play_video,btn_ijk_play_onlive,btn_ijk_play_list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!this.isTaskRoot()){
            Intent intent = getIntent();
            String action = intent.getAction();
            if(intent.hasCategory(Intent.CATEGORY_LAUNCHER)
                    && action.equals(Intent.ACTION_MAIN)){
                finish();
                return;
            }
        }else{
        }
        setContentView(R.layout.activity_main);
        mActivity=this;
        initView();

    }

    private void initView(){

        edit_url=(EditText)this.findViewById(R.id.edit_url);

        btn_vitamio_play_video=(Button)this.findViewById(R.id.btn_vitamio_play_video);
        btn_vitamio_play_video.setOnClickListener(this);

        btn_vitamio_play_onlive=(Button)this.findViewById(R.id.btn_vitamio_play_onlive);
        btn_vitamio_play_onlive.setOnClickListener(this);

        btn_ijk_play_video=(Button)this.findViewById(R.id.btn_ijk_play_video);
        btn_ijk_play_video.setOnClickListener(this);

        btn_ijk_play_onlive=(Button)this.findViewById(R.id.btn_ijk_play_onlive);
        btn_ijk_play_onlive.setOnClickListener(this);

        btn_ijk_play_list=(Button)this.findViewById(R.id.btn_ijk_play_list);
        btn_ijk_play_list.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        String url=edit_url.getText().toString().trim();
        //vitamio
        if (v.getId() == R.id.btn_vitamio_play_onlive){//视频
            if(BoTools.isEmpty(url)){
                url=onlive_url;
            }
            PlayUtils.startVitamioPlayVideo(mActivity, url, false);
        }
        if(v.getId()==R.id.btn_vitamio_play_video){//直播
            if(BoTools.isEmpty(url)){
                url=video_url;
            }
            PlayUtils.startVitamioPlayVideo(mActivity, url, true);
        }

        //ijk
        if(v.getId()==R.id.btn_ijk_play_video){//视频
            if(BoTools.isEmpty(url)){
                url=video_url;
            }
            PlayUtils.startIjkPlayVideo(mActivity, url, true);
        }
        if(v.getId()==R.id.btn_ijk_play_onlive){//直播
            if(BoTools.isEmpty(url)){
                url=onlive_url;
            }
            PlayUtils.startIjkPlayVideo(mActivity, url, true);
        }
        //列表播放
        if(v.getId()==R.id.btn_ijk_play_list){
            startActivity(new Intent(mActivity,IjkMediaListActivity.class));
        }
    }
}
