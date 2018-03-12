package net.xeill.elpuig.thinkitapp.view.manager;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.util.SparseArray;
import android.util.SparseIntArray;

import java.util.ArrayList;


/**
 * Created by gerard on 11/03/2018.
 */

public class SoundManager {

    public interface OnLoadCompleteListener {
        void onLoadComplete(SoundManager soundManager);
    }

    public static class Builder {
        Context context;
        ArrayList<Integer> sounds = new ArrayList<>();
        ArrayList<Integer> audios = new ArrayList<>();

        public Builder(Context context) {
            this.context = context;
        }

        public Builder addSound(int resId) {
            sounds.add(resId);
            return this;
        }

        public Builder addAudio(int resId) {
            audios.add(resId);
            return this;
        }

        public SoundManager build() {
            return new SoundManager(context, sounds, audios);
        }
    }

    private OnLoadCompleteListener onLoadCompleteListener;
    private Context context;

    ArrayList<Integer> sounds = new ArrayList<>();
    ArrayList<Integer> audios = new ArrayList<>();

    private SoundPool mSoundPool;
    private SparseIntArray mSoundPoolMap = new SparseIntArray();
    SparseArray<Boolean> mSoundPoolLoadedMap = new SparseArray<>();
    private Handler mHandler = new Handler();

    SparseArray<MediaPlayer> mMediaPlayerMap = new SparseArray<>();
    SparseArray<Boolean> mMediaPlayerLoadedMap = new SparseArray<>();
    SparseArray<Boolean> mMediaPlayerPausedMap = new SparseArray<>();

    private boolean mMuted = false;
    private float mVolume = 1;

    private static final int MAX_STREAMS = 2;
    private static final int STOP_DELAY_MILLIS = 3000;

    public SoundManager(Context context, final ArrayList<Integer> sounds, ArrayList<Integer> audios) {
        this.context = context;
        this.sounds = sounds;
        this.audios = audios;
    }

    public void load(){
        if(sounds.size() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mSoundPool = new SoundPool.Builder().setMaxStreams(15)
                        .build();
            } else {
                mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
            }

            for (Integer sound : sounds) {
                int soundId = mSoundPool.load(context, sound, 1);
                mSoundPoolMap.put(sound, soundId);
                mSoundPoolLoadedMap.put(soundId, false);
            }

            mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int soundId, int status) {
                    if(status == 0) {
                        mSoundPoolLoadedMap.put(soundId, true);
                        dispatchOnLoadComplete();
                    }
                }
            });
        }
        if (audios.size() > 0) {
            for (final Integer audio: audios) {
                MediaPlayer mediaPlayer = MediaPlayer.create(context, audio);
                mMediaPlayerLoadedMap.put(audio, false);
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mMediaPlayerLoadedMap.put(audio, true);
                        dispatchOnLoadComplete();
                    }
                });
                mMediaPlayerMap.put(audio, mediaPlayer);
                mMediaPlayerPausedMap.put(audio, false);
            }
        }
    }

    public void setOnLoadCompleteListener(OnLoadCompleteListener onLoadCompleteListener) {
        this.onLoadCompleteListener = onLoadCompleteListener;
    }

    private void dispatchOnLoadComplete(){
        if (onLoadCompleteListener == null) {
            return;
        }

        Boolean loaded = true;
        for (int i = 0; i < mSoundPoolLoadedMap.size(); i++) {
            loaded = loaded && mSoundPoolLoadedMap.get(mSoundPoolLoadedMap.keyAt(i));
        }
        for (int i = 0; i < mMediaPlayerLoadedMap.size(); i++) {
            loaded = loaded && mMediaPlayerLoadedMap.get(mMediaPlayerLoadedMap.keyAt(i));
        }

        if(loaded) {
            onLoadCompleteListener.onLoadComplete(SoundManager.this);
        }
    }

    public void playSound(int soundID) {
        if(mMuted){
            return;
        }

        final int soundId = mSoundPool.play(mSoundPoolMap.get(soundID), mVolume, mVolume, 1, 0, 1f);
        scheduleSoundStop(soundId);
    }

    public void playAudio(int audioId) {
        mMediaPlayerMap.get(audioId).setLooping(true);
        mMediaPlayerMap.get(audioId).start();
    }

    public void stopAudios() {
        for (int i = 0; i < mMediaPlayerMap.size(); i++) {
            mMediaPlayerMap.get(mMediaPlayerMap.keyAt(i)).stop();
        }
    }

    public void pauseAudios() {
        for (int i = 0; i < mMediaPlayerMap.size(); i++) {
            MediaPlayer mediaPlayer = mMediaPlayerMap.get(mMediaPlayerMap.keyAt(i));
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mMediaPlayerPausedMap.put(mMediaPlayerMap.keyAt(i), true);
            }
        }
    }

    public void resumeAudios() {
        for (int i = 0; i < mMediaPlayerPausedMap.size(); i++) {
            if(mMediaPlayerPausedMap.get(mMediaPlayerPausedMap.keyAt(i))){
                mMediaPlayerMap.get(mMediaPlayerMap.keyAt(i)).start();
            }
        }
    }

    private void scheduleSoundStop(final int soundId){
        mHandler.postDelayed(new Runnable() {
            public void run() {
                mSoundPool.stop(soundId);
            }
        }, STOP_DELAY_MILLIS);
    }

    public void setMuted(boolean muted) {
        this.mMuted = muted;

        if(muted) {
            for (int i = 0; i < mMediaPlayerMap.size(); i++) {
                mMediaPlayerMap.get(mMediaPlayerMap.keyAt(i)).setVolume(0,0);
            }
        } else {
            for (int i = 0; i < mMediaPlayerMap.size(); i++) {
                mMediaPlayerMap.get(mMediaPlayerMap.keyAt(i)).setVolume(mVolume, mVolume);
            }
        }
    }
}