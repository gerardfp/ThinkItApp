package net.xeill.elpuig.thinkitapp;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {

    MediaPlayer musicPlayer;
    MediaPlayer playSoundPlayer;
    MediaController.MediaPlayerControl videoPlayer;
    MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicPlayer = MediaPlayer.create(this,  R.raw.theme);
        musicPlayer.start();
        musicPlayer.setLooping(true); // Set looping
        musicPlayer.setVolume(0.5f,0.5f);

        VideoView bgVideo = findViewById(R.id.bg_video);
        bgVideo.setVideoURI(Uri.parse("android.resource://net.xeill.elpuig.thinkitapp/" + R.raw.background));
        bgVideo.start();

        bgVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        playSoundPlayer = MediaPlayer.create(this,R.raw.play);
        playSoundPlayer.setVolume(1f,1f);

        final ImageView playButton = findViewById(R.id.play_button_image);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButton.setActivated(true);
                playSoundPlayer.start();
            }
        });

        final FloatingActionButton volumeFAB = findViewById(R.id.volume_fab);
        volumeFAB.setActivated(true);

        volumeFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (volumeFAB.isActivated()) {
                    volumeFAB.setActivated(false);
                    musicPlayer.setVolume(0,0);
                } else {
                    volumeFAB.setActivated(true);
                    musicPlayer.setVolume(0.5f,0.5f);
                }

            }
        });

        final FloatingActionButton languageFAB = findViewById(R.id.language_fab);
        languageFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent languageIntent = new Intent(MainActivity.this,LanguageActivity.class);
                startActivity(languageIntent);
            }
        });

        final FloatingActionButton helpFAB = findViewById(R.id.help_fab);
        languageFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent helpIntent = new Intent(MainActivity.this,HelpActivity.class);
                startActivity(helpIntent);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        musicPlayer.stop();
        musicPlayer.release();
        //videoPlayer.stop();
        //videoPlayer.release();
    }
}
