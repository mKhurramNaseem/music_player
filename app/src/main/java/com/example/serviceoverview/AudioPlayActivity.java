package com.example.serviceoverview;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.app.NotificationChannelGroupCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.core.graphics.BitmapCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Size;
import android.view.DragEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

public class AudioPlayActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    ShapeableImageView ivMusicImage;
    TextView tvMusicName , tvCurrentTimeStamp , tvTotalDuration;
    FloatingActionButton btnPlayPause;
    SeekBar musicSeekBar;
    Handler handler;
//    MyHandler myHandler;
    Timer timer;
    NotificationManager notificationManager;
    Uri myAudio;
    String displayName;
    boolean isPaused;
    int duration;
    public static final String CHANNEL_ID = "AudioChannelId";
    public static final String TITLE_PREVIOUS = "Previous";
    public static final String TITLE_NEXT = "Next";
    public static final String TITLE_PLAY_PAUSE = "PlayPause";
    public static final String CHANNEL_NAME = "Audio Channel";
    public static final String CONTENT_TITLE = "My Media";
    public static final int NOTIFICATION_ID = 1;
    public static final int PREVIOUS_REQUEST_CODE = 1;
    public static final int NEXT_REQUEST_CODE = 1;
    public static final int PLAY_PAUSE_REQUEST_CODE = 1;

    ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result){
            if(result){
                createMediaNotification();
            }
        }
    });
    @SuppressLint({"SetTextI18n", "Range"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_play);
        ivMusicImage = findViewById(R.id.ivMusicImage);
        tvMusicName = findViewById(R.id.tvMusicName);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        tvCurrentTimeStamp = findViewById(R.id.tvCurrentTimeStamp);
        tvTotalDuration = findViewById(R.id.tvTotalDuration);
        musicSeekBar = findViewById(R.id.musicSeekBar);

//        myHandler = new MyHandler();
//        timer = new Timer();
        handler = new Handler();

        myAudio = Uri.parse(getIntent().getStringExtra(MainActivity.EXTRA_URI_PATH));

        @SuppressLint("Recycle")
        Cursor cursor = getContentResolver().query(myAudio,new String[]{MediaStore.Audio.AudioColumns.DISPLAY_NAME},null,null,null);
        if(cursor != null && cursor.moveToNext()) {
            int displayNameIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME);
            displayName = cursor.getString(displayNameIndex);
            tvMusicName.setText(displayName);
        }
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(this,myAudio);
        String strDuration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        duration = Integer.parseInt(strDuration);

        tvTotalDuration.setText(getFormattedTimeFromMilliSeconds(duration));
        tvCurrentTimeStamp.setText(getFormattedTimeFromMilliSeconds(0));

        setMediaPlayer();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }else{
            createMediaNotification();
        }

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(mediaPlayer != null){
                   if(mediaPlayer.isPlaying()){
                       btnPlayPause.setImageResource(R.drawable.baseline_play_arrow_24);
                       mediaPlayer.pause();
                       isPaused = true;
//                       timer.cancel();
//                    timer = new Timer();
                   }else{
                       btnPlayPause.setImageResource(R.drawable.baseline_pause_24);
                       mediaPlayer.start();
                       isPaused = false;
                       updateSeekBar();
//                    timer.schedule(new ProgressUpdate(),0,1000);
                   }
               }else{
                   setMediaPlayer();
                   btnPlayPause.setImageResource(R.drawable.baseline_pause_24);
               }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            musicSeekBar.setMin(0);
            musicSeekBar.setMax(duration);
        }

        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    seekBar.setProgress(progress);
                    tvCurrentTimeStamp.setText(getFormattedTimeFromMilliSeconds(seekBar.getProgress()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
//                tvCurrentTimeStamp.setText(getFormattedTimeFromMilliSeconds(seekBar.getProgress()));
                if(mediaPlayer != null){
                    mediaPlayer.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(mediaPlayer != null){
                    mediaPlayer.seekTo(seekBar.getProgress());
                    if(!isPaused){
                        mediaPlayer.start();
                        updateSeekBar();
                    }
                }
            }
        });

    }

    public String getFormattedTimeFromMilliSeconds(int milliseconds){
        String formattedTime = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Duration myDuration = Duration.ofMillis(milliseconds);
            long seconds = myDuration.getSeconds();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                long hh = myDuration.toHoursPart();
                long mm = myDuration.toMinutesPart();
                long ss = myDuration.toSecondsPart();
                if(hh>0){
                    formattedTime =  String.format(Locale.getDefault(),"%02d:%02d:%02d",hh,mm,ss);
                }else{
                    formattedTime = String.format(Locale.getDefault(),"%02d:%02d",mm,ss);
                }
            }
        }
        return formattedTime;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(mediaPlayer != null){
                updateSeekBar();
            }
        }
    };

    public void updateSeekBar(){
        if(mediaPlayer.isPlaying()){
            musicSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            tvCurrentTimeStamp.setText(getFormattedTimeFromMilliSeconds(mediaPlayer.getCurrentPosition()));
            handler.postDelayed(runnable,1000);
        }
    }

    public void setMediaPlayer(){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());
        try {
            mediaPlayer.setDataSource(this,myAudio);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.seekTo(musicSeekBar.getProgress());
                    mediaPlayer.start();
//                    timer.schedule(new ProgressUpdate(),0,1000);
                    updateSeekBar();
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnPlayPause.setImageResource(R.drawable.baseline_play_arrow_24);
                musicSeekBar.setProgress(0);
                tvCurrentTimeStamp.setText(getFormattedTimeFromMilliSeconds(musicSeekBar.getProgress()));
                mp.release();
                mediaPlayer = null;
            }
        });
    }

    public void createMediaNotification(){
        Intent previousIntent = new Intent(this, AudioPlayActivity.class);
        PendingIntent previousPendingIntent = PendingIntent.getActivity(this,PREVIOUS_REQUEST_CODE,previousIntent,PendingIntent.FLAG_IMMUTABLE);
        PendingIntent nextPendingIntent = PendingIntent.getActivity(this,NEXT_REQUEST_CODE,previousIntent,PendingIntent.FLAG_IMMUTABLE);
        PendingIntent playPausePendingIntent = PendingIntent.getActivity(this,PLAY_PAUSE_REQUEST_CODE,previousIntent,PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notificationCompatBuilder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(CONTENT_TITLE)
                .setContentText(displayName)
                .setContentIntent(playPausePendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.baseline_music_note_24)
                .setPriority(Notification.PRIORITY_LOW)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0,1,2))
                .addAction(R.drawable.baseline_skip_previous_24,TITLE_PREVIOUS,previousPendingIntent)
                .addAction(R.drawable.baseline_pause_24,TITLE_PLAY_PAUSE,playPausePendingIntent)
                .addAction(R.drawable.baseline_skip_next_24,TITLE_NEXT,nextPendingIntent)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.baseline_music_note_24));
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notificationManager.notify(NOTIFICATION_ID,notificationCompatBuilder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(NOTIFICATION_ID);
    }

    //    class MyHandler extends Handler{
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            int currentPosition = msg.arg1;
//            musicSeekBar.setProgress(currentPosition);
//        }
//    }

//    class ProgressUpdate extends TimerTask{
//
//        @Override
//        public void run() {
//            int currentPosition = mediaPlayer.getCurrentPosition();
//            Message message = new Message();
//            message.arg1 = currentPosition;
//            myHandler.dispatchMessage(message);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    String currentTime = getFormattedTimeFromMilliSeconds(currentPosition);
//                    tvCurrentTimeStamp.setText(currentTime);
//                }
//            });
//        }
//    }

}

