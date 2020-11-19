package sjtu.opennet.video;

import android.content.Context;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

public class Segmenter {

    public static final String TAG = "HONVIDEO.Segmenter";

    public static boolean isInit = false;
    private static int progressCounter = 0;

    public static ExecuteBinaryResponseHandler defaultHandler = new ExecuteBinaryResponseHandler(){
        @Override
        public void onSuccess(String message) {
            Log.i(TAG, String.format("Command success\n%s", message));
        }

        @Override
        public void onProgress(String message){
            progressCounter += 1;
            Log.i(TAG, String.format("cmd \t%d:\t%s", progressCounter, message));
        }

        @Override
        public void onFailure(String message) {
            progressCounter = 0;
            Log.e(TAG, "Command failure.");
        }

        @Override
        public void onStart() {
            progressCounter = 0;
        }

        @Override
        public void onFinish() {
            progressCounter = 0;
        }

    };


    public static void initFfmpeg(Context context) throws FFmpegNotSupportedException {

        FFmpeg ffmpeg = FFmpeg.getInstance(context);
        ffmpeg.loadBinary(new LoadBinaryResponseHandler(){
            @Override
            public void onSuccess(){
                Log.i(TAG, "FFmpeg binary load Successfully.");
            }
            @Override
            public void onFailure(){
                Log.e(TAG, "FFmpeg binary load failure.");
            }
        });
        isInit = true;
    }


    public static void segment(Context context, String cmd, ExecuteBinaryResponseHandler handler) throws Exception{
        if(!isInit){
            Log.w(TAG, "FFmpeg has not be loaded. Try to load it.");
            initFfmpeg(context);
        }
        FFmpeg ffmpeg = FFmpeg.getInstance(context);
        Log.d(TAG, String.format("Segment command %s", cmd));
        if(handler!=null){
            ffmpeg.execute(cmd.split(" "), handler);
        }else{
            ffmpeg.execute(cmd.split(" "), defaultHandler);
        }
    }
}
