package com.example.jdixit1.learnhssprarthna;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnTimedTextListener {
    MediaPlayer player = null;
    ImageButton play_button, pause_button, rewind_button;
    TextView subtitle_view;
    int duration;
    int current;
    private static final String TAG = "LearnHSSPrarthna";
    private static Handler handler = new Handler();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.prarthna_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.help:
                showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
       }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        play_button = (ImageButton) findViewById(R.id.imageButton_play);
        pause_button = (ImageButton) findViewById(R.id.imageButton_pause);
        rewind_button = (ImageButton)findViewById(R.id.imageButton_rewind);
        subtitle_view = (TextView) findViewById(R.id.textView_subtitle);

        // All button color Gray at start
        play_button.setBackgroundColor(Color.GRAY);
        pause_button.setBackgroundColor(Color.GRAY);
        rewind_button.setBackgroundColor(Color.GRAY);

        // Help Text
      //  "welcome to simple way to remember our prarthna"
      //  "play: plays audio and subtitle appears in Sanskrit and Hindi"
      //  "pause: - pause audio after listening each stanza and sing it aloud in the same tune"
      //         "- plays audio again from where it was paused"
      //  "rewind: replay last 10 seconds audio - useful to learn and remember last stanza"
        showBriefHelp();
    }

    private void showBriefHelp() {

        StringBuilder briefHelp = new StringBuilder();
        briefHelp.append("welcome to simple way to remember our prarthna");
        briefHelp.append("\n");
        briefHelp.append("\n");
        briefHelp.append("play      : plays audio with subtitle");
        briefHelp.append("\n");
        briefHelp.append("pause   : pause audio and plays again");
        briefHelp.append("\n");
        briefHelp.append("rewind : replays last 10 seconds");
        subtitle_view.setText(briefHelp.toString());

    }
    private void showHelp() {
        Intent helpIntent = new Intent();
        helpIntent.setClass(this,HelpActivity.class);
        startActivity(helpIntent);

    }
    private void releasePlayer() {
        if (player != null){
            player.stop();
            player.release();
            player = null;
        }
    }

    private void createPlayer(){
        player = MediaPlayer.create(this,R.raw.prarthana);
        duration = player.getDuration();
    }

    private void createCompletionListner(){

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                play_button.setBackgroundColor(Color.GRAY);
                pause_button.setBackgroundColor(Color.GRAY);
                //player.stop();
                //try {
                //    player.prepare();
                //} catch (IOException e) {
                //    e.printStackTrace();
                //}
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                releasePlayer();
                showBriefHelp();
//
            }
        });

    }

    private void initSubtitle(){
        try {
            player.addTimedTextSource(getSubtitleFile(R.raw.prarthana_subtitle),
                    MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP);
            int textTrackIndex = findTrackIndexFor(
                    MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT, player.getTrackInfo());
            if (textTrackIndex >= 0) {
                player.selectTrack(textTrackIndex);
            } else {
                Log.w(TAG, "Cannot find text track!");
            }
            player.setOnTimedTextListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int findTrackIndexFor(int mediaTrackType, MediaPlayer.TrackInfo[] trackInfo) {
        int index = -1;
        Log.d(TAG, "tracks = " + trackInfo.length);
        for (int i = 0; i < trackInfo.length; i++) {
            if (trackInfo[i].getTrackType() == mediaTrackType) {
                return i;
            }
        }
        return index;
    }

    private String getSubtitleFile(int resId) {
        String fileName = getResources().getResourceEntryName(resId);
        File subtitleFile = getFileStreamPath(fileName);
        if (subtitleFile.exists()) {
            Log.d(TAG, "Subtitle already exists");
            return subtitleFile.getAbsolutePath();
        }
        Log.d(TAG, "Subtitle does not exists, copy it from res/raw");

        // Copy the file from the res/raw folder to your app folder on the
        // device
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = getResources().openRawResource(resId);
            outputStream = new FileOutputStream(subtitleFile, false);
            copyFile(inputStream, outputStream);
            return subtitleFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeStreams(inputStream, outputStream);
        }
        return "";
    }

    private void copyFile(InputStream inputStream, OutputStream outputStream)
            throws IOException {
        final int BUFFER_SIZE = 1024;
        byte[] buffer = new byte[BUFFER_SIZE];
        int length = -1;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
    }

    // A handy method I use to close all the streams
    private void closeStreams(Closeable... closeables) {
        if (closeables != null) {
            for (Closeable stream : closeables) {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onTimedText(final MediaPlayer mp, final TimedText text) {

//        Log.d(TAG, "onTimedText ");
        if (text != null) {
            Log.d(TAG, "onTimedText " + "[" + secondsToDuration(mp.getCurrentPosition() / 1000) + "]" +
                    " " + text.getText().toString());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    subtitle_view.setText (text.getText());
                }
            });
        }
    }
    // To display the seconds in the duration format 00:00:00
    public String secondsToDuration(int seconds) {
        return String.format("%02d:%02d:%02d", seconds / 3600,
                (seconds % 3600) / 60, (seconds % 60), Locale.US);
    }

    public void playMusic(View v){

        //float dur_min = ((float)duration/1000)/60;
        if (v.equals(play_button)) {
            v.setBackgroundColor(Color.GREEN);
            if (player == null) {
                createPlayer();
                initSubtitle();
                createCompletionListner();
            }

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            player.start();
            player.pause();
            player.start();

            //Toast.makeText(getApplicationContext(), "duration = " + Float.toString(dur_min) , Toast.LENGTH_SHORT).show();
        }
    }

    public void pauseMusic (View v) {
        if (v.equals(pause_button)){
            if (player != null) {
                if (player.isPlaying()) {
//                Toast.makeText(getApplicationContext(),"pause pressed",Toast.LENGTH_SHORT).show();
                    current = player.getCurrentPosition();
                    player.pause();
                    play_button.setBackgroundColor(Color.RED);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
//                Toast.makeText(getApplicationContext(),"pause pressed",Toast.LENGTH_SHORT).show();
                    current = player.getCurrentPosition();
                    player.start();
                    player.pause();
                    player.start();
                    play_button.setBackgroundColor(Color.GREEN);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                }
            }
        }
    }

    public void onRewind(View v) {
       if (v.equals(rewind_button)) {
           if (player != null) {
               int cur = player.getCurrentPosition();
               int seek = (cur < 10000) ? 0 : cur - 10000;
               player.seekTo(seek);
               Toast.makeText(getApplicationContext(), "rewind audio: 10 sec", Toast.LENGTH_SHORT).show();
           }
       }
    }

//    public void onForward(View v) {
//        if (v.equals(forward_button)) {
//            int cur = player.getCurrentPosition();
//            player.seekTo((cur < duration - 10000) ? cur+10000 : duration - 1);
//        }
//    }

}
