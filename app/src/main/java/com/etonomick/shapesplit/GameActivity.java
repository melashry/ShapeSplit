package com.etonomick.shapesplit;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {

    Point displaySize;
    RelativeLayout gameActivity;
    TextView titleTextView, descriptionTextView;
    View leftShape, rightShape;
    Timer timerPlume, timerObstacles;
    InterstitialAd interstitialAd;
    int score, bestScore;
    SharedPreferences sharedPreferences;
    MediaPlayer failureSound, jumpingSound;

    void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        interstitialAd.loadAd(adRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        failureSound = MediaPlayer.create(getApplicationContext(), R.raw.failure);
        jumpingSound = MediaPlayer.create(getApplicationContext(), R.raw.jumping);

        interstitialAd = new InterstitialAd(getApplicationContext());
        interstitialAd.setAdUnitId(getString(R.string.ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });
        requestNewInterstitial();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);

        gameActivity = (RelativeLayout) findViewById(R.id.activity_game);
        gameActivity.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.gradient));

        titleTextView = new TextView(getApplicationContext());
        RelativeLayout.LayoutParams titleLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        titleLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        titleTextView.setLayoutParams(titleLayoutParams);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60);
        titleTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorMain));
        titleTextView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/FredokaOne-Regular.ttf"));
        titleTextView.setText(getString(R.string.app_name));
        titleTextView.setPadding(0, displaySize.y / 5, 0, 0);
        gameActivity.addView(titleTextView);

        descriptionTextView = new TextView(getApplicationContext());
        RelativeLayout.LayoutParams descriptionLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        descriptionLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        descriptionLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        descriptionTextView.setLayoutParams(descriptionLayoutParams);
        descriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        descriptionTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        descriptionTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorMain));
        descriptionTextView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/FredokaOne-Regular.ttf"));
        descriptionTextView.setText(getString(R.string.description));
        descriptionTextView.setPadding(0, 0, 0, displaySize.y / 10);
        gameActivity.addView(descriptionTextView);

        RelativeLayout.LayoutParams shapeLayoutParams = new RelativeLayout.LayoutParams(100, 100);

        leftShape = new View(getApplicationContext());
        leftShape.setLayoutParams(shapeLayoutParams);
        leftShape.setX(displaySize.x / 2 - leftShape.getLayoutParams().width / 2);
        leftShape.setY(displaySize.y / 1.5f - leftShape.getLayoutParams().height / 2);
        leftShape.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.shape));
        gameActivity.addView(leftShape);

        rightShape = new View(getApplicationContext());
        rightShape.setLayoutParams(shapeLayoutParams);
        rightShape.setX(displaySize.x / 2 - rightShape.getLayoutParams().width / 2);
        rightShape.setY(displaySize.y / 1.5f - rightShape.getLayoutParams().height / 2);
        rightShape.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.shape));
        gameActivity.addView(rightShape);

        score = 0;
        sharedPreferences = getApplicationContext().getSharedPreferences(getPackageName(), MODE_PRIVATE);
        bestScore = sharedPreferences.getInt("best_score", 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                descriptionTextView.setVisibility(View.INVISIBLE);
                timerPlume = new Timer();
                score = 0;
                titleTextView.setText(String.format(Locale.getDefault(), "%d", score));
                timerPlume.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                View leftPlume = new View(getApplicationContext());
                                RelativeLayout.LayoutParams plumeLayoutParams = new RelativeLayout.LayoutParams(leftShape.getLayoutParams().width, leftShape.getLayoutParams().height);
                                leftPlume.setLayoutParams(plumeLayoutParams);
                                leftPlume.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.shape));
                                leftPlume.setX(leftShape.getX());
                                leftPlume.setY(leftShape.getY());
                                gameActivity.addView(leftPlume);

                                View rightPlume = new View(getApplicationContext());
                                rightPlume.setLayoutParams(plumeLayoutParams);
                                rightPlume.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.shape));
                                rightPlume.setX(rightShape.getX());
                                rightPlume.setY(rightShape.getY());
                                gameActivity.addView(rightPlume);

                                AnimatorSet plumeAnimationSet = new AnimatorSet();
                                plumeAnimationSet.playTogether(ObjectAnimator.ofFloat(leftPlume, "y", displaySize.y), ObjectAnimator.ofFloat(leftPlume, "alpha", 0.0f), ObjectAnimator.ofFloat(rightPlume, "y", displaySize.y), ObjectAnimator.ofFloat(rightPlume, "alpha", 0.0f));
                                plumeAnimationSet.setDuration(100);
                                plumeAnimationSet.start();
                            }
                        });
                    }
                }, 0, 100);
                timerObstacles = new Timer();
                timerObstacles.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                score++;
                                titleTextView.setText(String.format(Locale.getDefault(), "%d", score));
                                if (score > bestScore) {
                                    sharedPreferences.edit().putInt("best_score", bestScore).apply();
                                    bestScore = score;
                                }
                                jumpingSound.start();
                            }
                        });

                        int numberOfObstacles = new Random().nextInt(3);
                        final ArrayList<Integer> obstaclesX = new ArrayList<>();
                        final Integer obstacleSizeX;

                        switch (numberOfObstacles) {
                            case 0:
                                obstacleSizeX = displaySize.x / 2;
                                obstaclesX.add(displaySize.x / 2 - obstacleSizeX / 2);
                                break;
                            case  1:
                                obstacleSizeX = displaySize.x / 4;
                                obstaclesX.add(displaySize.x / 4 - obstacleSizeX / 2);
                                obstaclesX.add((displaySize.x - displaySize.x / 4) - obstacleSizeX / 2);
                                break;
                            case 2:
                                obstacleSizeX = displaySize.x / 5;
                                obstaclesX.add(0);
                                obstaclesX.add(displaySize.x / 2 - obstacleSizeX / 2);
                                obstaclesX.add(displaySize.x - obstacleSizeX);
                                break;
                            default:
                                obstacleSizeX = null;
                                break;
                        }

                        if (obstacleSizeX != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    for (Integer obstacleX : obstaclesX) {
                                        final View obstacle = new View(getApplicationContext());
                                        obstacle.setLayoutParams(new RelativeLayout.LayoutParams(obstacleSizeX, leftShape.getLayoutParams().height));
                                        obstacle.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorMain));
                                        obstacle.setX(obstacleX);
                                        obstacle.setY(-obstacle.getLayoutParams().height);
                                        gameActivity.addView(obstacle);
                                        ObjectAnimator obstacleAnimator = ObjectAnimator.ofFloat(obstacle, "y", displaySize.y);
                                        obstacleAnimator.setDuration(2000);
                                        obstacleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                            @Override
                                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                                if (Rect.intersects(new Rect((int) leftShape.getX(), (int) leftShape.getY(), (int) leftShape.getX() + leftShape.getWidth(), (int) leftShape.getY() + leftShape.getHeight()), new Rect((int) obstacle.getX(), (int) obstacle.getY(), (int) obstacle.getX() + obstacle.getWidth(), (int) obstacle.getY() + obstacle.getHeight()))) {
                                                    collisionDetected();
                                                }
                                            }
                                        });
                                        obstacleAnimator.start();
                                    }
                                }
                            });
                        }
                    }
                }, 0, 500);
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getX() > displaySize.x / 2) {
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(ObjectAnimator.ofFloat(leftShape, "x", (displaySize.x - event.getX()), leftShape.getX()), ObjectAnimator.ofFloat(rightShape, "x", event.getX() - rightShape.getWidth(), rightShape.getX()));
                    animatorSet.setDuration(100);
                    animatorSet.start();
                }
                else if (event.getX() < displaySize.x / 2) {
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(ObjectAnimator.ofFloat(leftShape, "x", event.getX(), leftShape.getX()), ObjectAnimator.ofFloat(rightShape, "x", (displaySize.x - event.getX()) - rightShape.getWidth(), rightShape.getX()));
                    animatorSet.setDuration(100);
                    animatorSet.start();
                }
                else {
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(ObjectAnimator.ofFloat(leftShape, "x", event.getX(), leftShape.getX()), ObjectAnimator.ofFloat(rightShape, "x", event.getX(), rightShape.getX()));
                }
                break;
            case MotionEvent.ACTION_UP:
                descriptionTextView.setVisibility(View.VISIBLE);
                cancelTimers();
                break;
            default: break;
        }
        return super.onTouchEvent(event);
    }

    void collisionDetected() {
        descriptionTextView.setVisibility(View.VISIBLE);
        descriptionTextView.setText(String.format(Locale.getDefault(), "Best score is %d", bestScore));
        cancelTimers();
        failureSound.start();
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

    void cancelTimers() {
        if (timerObstacles != null && timerPlume != null) {
            timerObstacles.cancel();
            timerObstacles = null;
            timerPlume.cancel();
            timerPlume = null;
        }
    }

}
