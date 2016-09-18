package com.us.cditzen.synesthesia;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import java.util.Random;

/**
 * Created by cditz_000 on 9/16/2016.
 */
public class Synesthesia {

    private MediaPlayer mp;
    private MediaPlayer lightning;
    private Activity activity;
    private boolean audioEvent;
    private boolean selectedLightning;
    private int getSide;

    public Synesthesia(Activity activity) {
        this.activity = activity;
        audioEvent = false;
        selectedLightning = false;
        getSide = 0;
    }

    public void loopBackgroundNoise() {
        Log.v("loopBackgroundNoise", "loopBackgroundNoise");
        mp = MediaPlayer.create(activity, chooseRandomBackground());
        mp.setVolume(0.95f, 0.95f);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loopBackgroundNoise();
                    }
                }, 1000);
            }
        });
        mp.start();
    }

    public void playLightningStrike() {
        Log.v("playLightningStrike", "playLightningStrike");
        selectedLightning = false;
        lightning = MediaPlayer.create(activity, chooseRandomLightning());
        final Random rand = new Random();
        int r = rand.nextInt(2);
        if (r == 0) {
            lightning.setVolume(1.0f, 0.0f);
            getSide = 1;
        } else {
            lightning.setVolume(0.0f, 1.0f);
            getSide = -1;
        }
        lightning.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Handler handler = new Handler();
                int length = rand.nextInt(5) * 1000 + 500;
                if (selectedLightning == false) {
                    System.out.println("Fail");
                    MainActivity.miss();
                }
                audioEvent = false;
                System.out.println("AE: " + audioEvent);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playLightningStrike();
                    }
                }, length);
            }

        });
        lightning.start();
        audioEvent = true;
        System.out.println("AE: " + audioEvent);
    }

    /**
     * Called when user correctly hits lightning
     */
    public void confirmLightning() {
        selectedLightning = true;
    }

    public void stopBackgroundNoise() {
        mp.stop();
        lightning.stop();
        mp.release();
        lightning.release();
    }

    private int chooseRandomBackground() {
        Random rand = new Random();
        int r = rand.nextInt(5);
        System.out.println("Random" + r);
        switch (r) {
            case 0:
                return R.raw.rain;
            case 1:
                return R.raw.rainone;
            case 2:
                return R.raw.raintwo;
            case 3:
                return R.raw.rainthree;
            case 4:
                return R.raw.rainseven;
            default:
                return R.raw.rain;
        }
    }

    private int chooseRandomLightning(){
        Random rand = new Random();
        int r = rand.nextInt(5);
        switch (r) {
            case 0:
                return R.raw.thunder;
            case 1:
                return R.raw.thunderone;
            case 2:
                return R.raw.thunderexclamation;
            case 3:
                return R.raw.stormthunder;
            case 4:
                return R.raw.thundertwo;
            default:
                return R.raw.bellringing;
        }
    }

    public boolean getAudioEvent() {
        return audioEvent;
    }

    public void completeAudioEvent(boolean audioEvent) {
        this.audioEvent = audioEvent;
    }

    public int getSide() {
        return getSide;
    }




}
