package com.hyperclock.prashant.cardiacsimulator;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class StepOneActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = StepOneActivity.class.getSimpleName();
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private float lastX, lastY, lastZ;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;
    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;
    private int shakes = 0;
    private TextToSpeech textToSpeech;
    private TextView currentX, currentY, shakesView;

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                mMediaPlayer.pause();
                mMediaPlayer.seekTo(0);
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                mMediaPlayer.start();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                releaseMediaPlayer();
            }
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            releaseMediaPlayer();
        }
    };

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_one);
        initializeViews();
        releaseMediaPlayer();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        } else {
            // failed! we don't have an accelerometer!
            ToastMessage("Accelerometer is not present in the device");
        }

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                    textToSpeech.setSpeechRate(1f);
                    textToSpeech.setPitch(1f);
                }
            }
        });


    }


    private void initializeViews() {
        currentX = (TextView) findViewById(R.id.currentX_tv_1);
        currentY = (TextView) findViewById(R.id.currentY_tv_1);

        shakesView = (TextView) findViewById(R.id.shakes);

    }

    private void Speak(String string) {
        textToSpeech.speak(string, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void ToastMessage(String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }

    public void checkMovement() {
        //if ((deltaX > vibrateThreshold) || (deltaY > vibrateThreshold) || (deltaZ > vibrateThreshold)) {
        if ((deltaX > 2f) || (deltaY > 2f)) {
            shakes++;
            shakesView.setText("Shake the body : " + shakes + "/5");
        }
        if ((deltaX > 10f) || (deltaY > 10f)) {
            Speak("Violent shaking. Please shake gently.");
            ToastMessage("Let's start again.");
            shakes = 0;
        }

        if (shakes == 5) {
            shakesView.setText("Five shakes complete");
            winner();
            AlertDialog.Builder builder = new AlertDialog.Builder(StepOneActivity.this);
            builder.setTitle(R.string.completion_title);
            builder.setMessage(R.string.completion_message_step_one);
            builder.setPositiveButton(R.string.positive_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    recreate();
                }
            });
            builder.setNegativeButton(R.string.negative_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void winner() {
        mMediaPlayer = MediaPlayer.create(StepOneActivity.this, R.raw.cartoon_winning_sound);

        // Start the audio file
        mMediaPlayer.start();
        mMediaPlayer.setOnCompletionListener(mCompletionListener);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // clean current values
        displayCleanValues();
        // display the current x,y,z accelerometer values
        displayCurrentValues();

        // get the change of the x,y,z values of the accelerometer
        deltaX = Math.abs(lastX - event.values[0]);
        deltaY = Math.abs(lastY - event.values[1]);

        // if the change is below 2, it is just plain noise
        if (deltaX < 1)
            deltaX = 0;
        if (deltaY < 1)
            deltaY = 0;

        // set the last know values of x,y,z
        lastX = event.values[0];
        lastY = event.values[1];
        lastZ = event.values[2];

        //checking the XY movement of the body
        checkMovement();
    }

    private void displayCurrentValues() {
        currentX.setText(Float.toString(deltaX));
        currentY.setText(Float.toString(deltaY));
    }

    private void displayCleanValues() {
        currentX.setText("0.0");
        currentY.setText("0.0");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
