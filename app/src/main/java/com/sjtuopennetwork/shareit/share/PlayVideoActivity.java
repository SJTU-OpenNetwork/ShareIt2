package com.sjtuopennetwork.shareit.share;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.DefaultHlsPlaylistTracker;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistParserFactory;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistTracker;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.LoadErrorHandlingPolicy;
import com.google.android.exoplayer2.util.Util;
import com.sjtuopennetwork.shareit.R;

import java.io.File;
import java.io.IOException;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.stream.video.VideoGetter;
import sjtu.opennet.stream.video.VideoGetter_tkt;


public class PlayVideoActivity extends AppCompatActivity {
    private static final String TAG = "====================PlayVideoActivity";

    static PlayerView playerView;
    static DataSource.Factory dataSourceFactory;
    static SimpleExoPlayer player;
    static VideoGetter videoGetter;
    static VideoGetter_tkt videoGetterTkt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        setContentView(R.layout.activity_video_stream_play);

        boolean isMine=getIntent().getBooleanExtra("ismine",false);
        playerView = findViewById(R.id.video_stream_player_view);

        player = ExoPlayerFactory.newSimpleInstance(PlayVideoActivity.this);
        playerView.setPlayer(player);

        DefaultHlsPlaylistTracker tracker;

        if(isMine){ //自己的就直接播放本地视频
            Uri uri=Uri.parse(getIntent().getStringExtra("videopath"));
            //直接播放本地视频文件
            dataSourceFactory = new DefaultDataSourceFactory(this,
                    Util.getUserAgent(this, "ShareIt"));
            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(uri);
            player.prepare(videoSource);
        }else{ //播放m3u8文件，根据videoid来构建m3u8的地址
            String videoId=getIntent().getStringExtra("videoid");
            boolean ticket=getIntent().getBooleanExtra("ticket",false);

            Uri uri;
            if(ticket){
                videoGetterTkt=new VideoGetter_tkt(this,videoId);
                videoGetterTkt.startGet(new Handlers.VideoPrefetchHandler() {
                    @Override
                    public void onPrefetch() {

                    }
                });
                uri=videoGetterTkt.getUri();
            }else{ //不是ticket
                videoGetter =new VideoGetter(this,videoId);
                videoGetter.startGet(new Handlers.VideoPrefetchHandler() {
                    @Override
                    public void onPrefetch() {

                    }
                });
                uri=videoGetter.getUri();
            }


//            String m3u8Path=getIntent().getStringExtra("m3u8");
//            Uri uri=Uri.fromFile(new File(m3u8Path));

            dataSourceFactory = new DefaultDataSourceFactory(PlayVideoActivity.this, Util.getUserAgent(PlayVideoActivity.this, "ShareIt"));

            HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                    .setMinLoadableRetryCount(10)
                    .setAllowChunklessPreparation(true)
                    .createMediaSource(uri);

            player.prepare(hlsMediaSource);
            player.seekTo(0,0);
//            player.setPlayWhenReady(true);
        }
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
