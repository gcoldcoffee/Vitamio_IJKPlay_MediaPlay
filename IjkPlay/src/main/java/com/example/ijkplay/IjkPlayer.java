package com.example.ijkplay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class IjkPlayer extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ijkplayer);
        setStatusBar();

        findViewById(R.id.btn_ijk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(IjkPlayer.this, IjkPlayerActivity.class));
            }
        });
    }
}
