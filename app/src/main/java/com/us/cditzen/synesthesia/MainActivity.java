package com.us.cditzen.synesthesia;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener{

    final private float GRADIENT_WEIGHT = 10f;

    private View green;
    private View blue;
    private View leftGradient;
    private View rightGradient;
    private TextView scoreLeft;
    private TextView scoreRight;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Synesthesia syn;
    private boolean checkingValue;
    private int currentSide;
    private static int count;

    Vibrator v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);

        blue = findViewById(R.id.blue);
        green = findViewById(R.id.green);
        leftGradient = findViewById(R.id.left_gradient);
        rightGradient = findViewById(R.id.right_gradient);
        scoreLeft = (TextView) findViewById(R.id.score_left);
        scoreRight = (TextView) findViewById(R.id.score_right);
        blue.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, GRADIENT_WEIGHT));
        green.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, GRADIENT_WEIGHT));
        v = (Vibrator) getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
        currentSide = 0;
        count = 0;

        checkingValue = false;

        syn = new Synesthesia(MainActivity.this);
        syn.loopBackgroundNoise();
        syn.playLightningStrike();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float yRotation = event.values[1];

            float ySquared = yRotation * yRotation;

            if (yRotation >= 0) {
                green.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, ySquared + GRADIENT_WEIGHT));
                blue.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, GRADIENT_WEIGHT));
            } else {
                blue.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, ySquared + GRADIENT_WEIGHT));
                green.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, GRADIENT_WEIGHT));
            }

            float alpha = Math.abs(yRotation) / 12;
            rightGradient.setAlpha(alpha);
            leftGradient.setAlpha(alpha);

            if (ySquared > 10f) {
                currentSide = setSide((int) yRotation);
                if (syn.getAudioEvent() && !checkingValue) {
                    checkSide();
                }
            }
        }
    }

    protected void onPause() {
        super.onPause();
        syn.stopBackgroundNoise();
        mSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        // Don't think I need to re start the media players
    }

    private int setSide(int yRotation ) {
        if (yRotation > 0) {
            return 1;
        } else {
            return -1;
        }
    }

    public static void miss() {
        count = 0;
    }

    /**
     * Run a loop for a second and see if it is still tilted.
     */
    private void checkSide() {
        Log.v("checkSide", "preDelayed");
        checkingValue = true;
        Handler handler = new Handler();
        if (syn.getSide() == currentSide) {
            if(syn.getSide() == -1) {
                leftGradient.setVisibility(View.VISIBLE);
            } else {
                rightGradient.setVisibility(View.VISIBLE);
            }
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.v("lol", "postDelayed");
                syn.confirmLightning();
                checkingValue = false;
                leftGradient.setVisibility(View.INVISIBLE);
                rightGradient.setVisibility(View.INVISIBLE);

                if(syn.getAudioEvent() == true && syn.getSide() == currentSide) {
                    // Get a point
                    syn.confirmLightning();
                    count++;
                    scoreLeft.setText(String.valueOf(count));
                    scoreRight.setText(String.valueOf(count));
                    syn.completeAudioEvent(false);
                    long[] pattern = {0, 18, 80, 80};
                    v.vibrate(pattern, -1);

                    Animation fadeIn = new AlphaAnimation(1.0f, 0.0f);
                    fadeIn.setInterpolator(new DecelerateInterpolator());
                    fadeIn.setDuration(3000);

                    AnimationSet anim = new AnimationSet(false);
                    anim.addAnimation(fadeIn);

                    Animation textAnimation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.simple_up_from_bottom);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            if(syn.getSide() == -1) {
                                scoreLeft.setVisibility(View.VISIBLE);
                            } else {
                                scoreRight.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            scoreLeft.setVisibility(View.GONE);
                            scoreRight.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    if(syn.getSide() == -1) {
                        leftGradient.setAnimation(anim);
                        scoreLeft.setAnimation(textAnimation);
                    } else {
                        rightGradient.setAnimation(anim);
                        scoreRight.setAnimation(textAnimation);
                    }
                }
            }
        }, 1000);
    }
}
