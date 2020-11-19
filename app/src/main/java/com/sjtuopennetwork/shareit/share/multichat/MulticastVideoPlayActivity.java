package com.sjtuopennetwork.shareit.share.multichat;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.sjtuopennetwork.shareit.R;

import java.io.File;

import sjtu.opennet.multicastfile.HONMulticaster;
import sjtu.opennet.util.FileUtil;

public class MulticastVideoPlayActivity extends AppCompatActivity {
    private static final String TAG = "==============MulticastVideoPlayActiv";

    PlayerView playerView;
    DataSource.Factory dataSourceFactory;
    SimpleExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        setContentView(R.layout.activity_multicast_video_play);

        String videoDir = HONMulticaster.TXTL_VIDEO_DIR;
        String videoId=getIntent().getStringExtra("videoId");
        Log.d(TAG, "onCreate: videoId:"+videoId);
        File m3u8File=new File(videoDir + "/" + videoId + "/" +videoId+".m3u8");
        Uri uri=Uri.fromFile(m3u8File);

        playerView = findViewById(R.id.video_multi_player_view);
        player = ExoPlayerFactory.newSimpleInstance(MulticastVideoPlayActivity.this);
        playerView.setPlayer(player);

        dataSourceFactory = new DefaultDataSourceFactory(MulticastVideoPlayActivity.this, Util.getUserAgent(MulticastVideoPlayActivity.this, "ShareIt"));
        HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true).createMediaSource(uri);
        player.prepare(hlsMediaSource);
        player.seekTo(0,0);
//        player.setPlayWhenReady(true);
    }


    @Override
    protected void onStop() {
        super.onStop();

        player.setPlayWhenReady(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.setPlayWhenReady(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //结束播放
        if(player!=null){
            player.stop();
            player.release(); //释放播放器
        }
    }
}
