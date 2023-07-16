package com.example.dhun2;

import static com.example.dhun2.R.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    Button btnPlay, btnNext, btnPrevious, btnFast, btnRewind;
    TextView txtSongStart, txtSongEnd, txtSongName;
    ImageView imgView;
    SeekBar seekBar;

    // variables
    String songName;
    int position;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    ArrayList<File> mySongs;
    Thread updateSeekBar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_player);

        // action bar settings
        getSupportActionBar().setTitle("Dhun - Music Player");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(color.black)));
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        btnPlay = findViewById(id.btnPlay);
        btnNext = findViewById(id.btnNext);
        btnFast = findViewById(id.btnFast);
        btnPrevious = findViewById(id.btnPrevious);
        btnRewind = findViewById(id.btnRewind);

        txtSongName = findViewById(id.txtSong);
        txtSongStart = findViewById(id.txtSongStart);
        txtSongEnd = findViewById(id.txtSongEnd);

        imgView = findViewById(id.imgView);

        seekBar = findViewById(id.seekBar);

        // media player check not null
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.release();
        }

        // extract intent form bundle
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        songName = (String) bundle.getString("songname");
        position = (int) bundle.getInt("pos", 0);

        txtSongName.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        songName = mySongs.get(position).getName();
        txtSongName.setText(songName);

        // media player create
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        // update seek bar using Thread
        updateSeekBar = new Thread(){
            @Override
            public void run(){
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;

                while (currentPosition<totalDuration){
                    try{
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                    } catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        // call update seek bar method
        seekBar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();
        seekBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, R.color.purple_700), PorterDuff.Mode.SRC_IN);
        seekBar.getThumb().setColorFilter(ContextCompat.getColor(this, R.color.purple_700), PorterDuff.Mode.SRC_IN);

        // change seekbar on user click
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        txtSongEnd.setText(endTime);

        // update song current time in every one second
        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtSongStart.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        },delay);

        // btn play set on click listener
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    btnPlay.setBackgroundResource(drawable.ic_play);
                    mediaPlayer.pause();
                }else {
                    btnPlay.setBackgroundResource(drawable.ic_pause);
                    mediaPlayer.start();

                    TranslateAnimation moveAnim = new TranslateAnimation(-25,25,-25,25);
                    moveAnim.setInterpolator(new AccelerateInterpolator());
                    moveAnim.setDuration(800);
                    moveAnim.setFillEnabled(true);
                    moveAnim.setFillAfter(true);
                    moveAnim.setRepeatMode(Animation.REVERSE);
                    moveAnim.setRepeatCount(1);
                    imgView.startAnimation(moveAnim);
                }
            }
        });

        // when on song is being complete then next song automatically start playing
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnNext.performClick();
                String endTime = createTime(mediaPlayer.getDuration());
                txtSongEnd.setText(endTime);
            }
        });

        // btn next set on click listener
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position+1)%mySongs.size());
                Uri uri = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
                songName = mySongs.get(position).getName();
                txtSongName.setText(songName);
                String endTime = createTime(mediaPlayer.getDuration());
                txtSongEnd.setText(endTime);
                mediaPlayer.start();

                startAnimation(imgView,360f);
            }
        });

        // btn previous set on click listener
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position-1)<0)?(mySongs.size()-1):position-1;
                Uri uri = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
                songName = mySongs.get(position).getName();
                txtSongName.setText(songName);
                String endTime = createTime(mediaPlayer.getDuration());
                txtSongEnd.setText(endTime);
                mediaPlayer.start();

                startAnimation(imgView,-360f);
            }
        });

        // btn fast forward set on click listener
        btnFast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });

        // btn rewind forward set on click listener
        btnRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });

    }

    // song image rotation animation
    public void startAnimation(View view,Float degree){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imgView,"rotation",0f,degree);
        objectAnimator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator);
        animatorSet.start();
    }

    public String createTime(int duration){
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time += min+":";
        if(sec<10){
            time+="0";
        }
        time += sec;
        return time;
    }

}