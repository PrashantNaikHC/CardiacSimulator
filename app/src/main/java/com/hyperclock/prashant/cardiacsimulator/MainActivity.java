package com.hyperclock.prashant.cardiacsimulator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appolica.flubber.Flubber;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity implements SensorEventListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    final Handler handler = new Handler();
    private final int minArrivalForComp = 3;
    private final int maxArrivalForComp = 7;
    public Vibrator vibration;
    ArrayList<String> xVals = new ArrayList<String>();
    ArrayList<Entry> yVals = new ArrayList<Entry>();
    ArrayList<BarEntry> barValues = new ArrayList<>();
    Runnable runnableCode = null;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private float lastX, lastY, lastZ;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private LineChart mChart;
    private HorizontalBarChart horizontalBar;
    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;
    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;
    private int health_value = 20;
    private NumberFormat formatter = new DecimalFormat("0.0");
    private float vibrateThreshold = 0;
    private int beats = 0;
    private TextToSpeech textToSpeech;
    private TextView currentX, currentY, currentZ, maxX, maxY, maxZ, beatCount, health;
    private ImageView infalterImage;
    private int compressionSets = 0;
    private int arrivalForComp;

    private boolean COLD_START = true;
    private boolean CREST = false;
    private boolean BREATH = true;


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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        getIncomingIntent();
        releaseMediaPlayer();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mChart = (LineChart) findViewById(R.id.linechart);

        //barValues.add(new BarEntry(2f, 20));
        ambulanceArrival(minArrivalForComp, maxArrivalForComp);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            vibrateThreshold = accelerometer.getMaximumRange() / 2;
        } else {
            // fai! we dont have an accelerometer!
            Toast.makeText(this, "Accelerometer is not present in the device.", Toast.LENGTH_SHORT).show();
        }

        //initialize vibration
        vibration = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        runnableCode = new Runnable() {
            @Override
            public void run() {
                if (!BREATH) {


                    //case after one compression cycle
                    if (beats == 30) {
                        vibration.vibrate(500);
                        compressionSets++;
                        Speak(+compressionSets + "set of Compression complete.");
                        try {
                            mChart.invalidate();
                            drawLineChart();
                        } catch (NegativeArraySizeException ex) {
                            Toast.makeText(MainActivity.this, "hell no", Toast.LENGTH_SHORT).show();
                        }
                        beats = 0;
                    }

                    if (compressionSets == arrivalForComp) {
                        handler.removeCallbacks(runnableCode);
                        ambulance();
                        Speak("Professional help has arrived. Thanks for completing the test.");
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(R.string.completion_title);
                        builder.setMessage(R.string.completion_message);
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
                        return;
                    }

                    vibration.vibrate(50);


                    if (beats == 15) {
                        Speak(+beats + "compressions complete");
                    }

                    beats++;
                    health_value -= 1;

                    if (beats % 5 == 0) {
                        //checkHealthState(health_value);
                    }

                    try {
                        String StringValue = formatter.format(deltaZ);
                        Float numValue = Float.parseFloat(StringValue);
                        yVals.add(new Entry(Math.abs(numValue), beats));
                        xVals.add("" + beats);
                        barValues.add(new BarEntry(beats, 2f));
                        Log.d(TAG, "run: array values are" + xVals.get(beats - 1) + " " + yVals.get(beats - 1));
                    } catch (NegativeArraySizeException ex) {
                        Toast.makeText(MainActivity.this, "Exception found", Toast.LENGTH_SHORT).show();
                    }


                    com.appolica.flubber.Flubber.with()
                            .animation(Flubber.AnimationPreset.POP) // Slide up animation
                            .repeatCount(0)                            // Repeat once
                            .duration(300)                              // Last for 1000 milliseconds(1 second)
                            .createFor(beatCount)                             // Apply it to the view
                            .start();                                    // Start it now

                    //update beats, vibrate, with a delay of 600ms
                    beatCount.setText("" + beats);
                    handler.postDelayed(this, 600);

                    com.appolica.flubber.Flubber.with()
                            .animation(Flubber.AnimationPreset.FADE_IN_UP) // Slide up animation
                            .repeatCount(0)                            // Repeat once
                            .duration(100)                              // Last for 1000 milliseconds(1 second)
                            .createFor(health)                             // Apply it to the view
                            .start();                                    // Start it now
                    health.setText("" + health_value);
                } else {

                    if (beats == 30) {
                        vibration.vibrate(500);
                        compressionSets++;
                        Speak(+compressionSets + "set of Compression complete.");
                        try {
                            mChart.invalidate();
                            drawLineChart();
                        } catch (NegativeArraySizeException ex) {
                            Toast.makeText(MainActivity.this, "hell no", Toast.LENGTH_SHORT).show();
                        }
                    }

                    if (beats == 38) {
                        beats = 0;
                        Speak("Start compressions again");
                    }
                    if (beats == 31) {
                        Speak("Time for two rescue breaths");
                    }
                    if (beats == 31 || beats == 32 || beats == 33 || beats == 34 || beats == 35 || beats == 36 || beats == 37) {
                        ToastMessage("perform two breaths within five seconds");
                        float time = beats * 0.6f;
                        beatCount.setText(time + " Seconds");
                    } else {
                        vibration.vibrate(50);
                    }
                    if (compressionSets == arrivalForComp) {
                        handler.removeCallbacks(runnableCode);
                        ambulance();
                        Speak("Professional help has arrived. Thanks for completing the test.");
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(R.string.completion_title);
                        builder.setMessage(R.string.completion_message);
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
                        return;
                    }

                    if (beats == 15) {
                        Speak(+beats + "compressions complete");
                    }

                    beats++;
                    health_value -= 1;

                    if (beats % 5 == 0) {
                        //checkHealthState(health_value);
                    }

                    try {
                        String StringValue = formatter.format(deltaZ);
                        Float numValue = Float.parseFloat(StringValue);
                        yVals.add(new Entry(Math.abs(numValue), beats));
                        xVals.add("" + beats);
                        barValues.add(new BarEntry(beats, 2f));
                        Log.d(TAG, "run: array values are" + xVals.get(beats - 1) + " " + yVals.get(beats - 1));
                    } catch (NegativeArraySizeException ex) {
                        Toast.makeText(MainActivity.this, "Exception found", Toast.LENGTH_SHORT).show();
                    }


                    com.appolica.flubber.Flubber.with()
                            .animation(Flubber.AnimationPreset.POP) // Slide up animation
                            .repeatCount(0)                            // Repeat once
                            .duration(300)                              // Last for 1000 milliseconds(1 second)
                            .createFor(beatCount)                             // Apply it to the view
                            .start();                                    // Start it now

                    //update beats, vibrate, with a delay of 600ms
                    beatCount.setText("" + beats);
                    handler.postDelayed(this, 600);

                    com.appolica.flubber.Flubber.with()
                            .animation(Flubber.AnimationPreset.FADE_IN_UP) // Slide up animation
                            .repeatCount(0)                            // Repeat once
                            .duration(100)                              // Last for 1000 milliseconds(1 second)
                            .createFor(health)                             // Apply it to the view
                            .start();                                    // Start it now
                    health.setText("" + health_value);
                }
            }
        };
        handler.post(runnableCode);

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

    private void getIncomingIntent() {
        if (getIntent().getBooleanExtra("cpr_mode", false)) {
            BREATH = true;
        } else {
            BREATH = false;
        }
    }


    private void ambulanceArrival(int min, int max) {
        arrivalForComp = new Random().nextInt((max - min) + 1) + min;
        Log.d(TAG, "ambulanceArrival: ambulance arrival is set for compressions " + arrivalForComp);
    }


    private void checkHealthState(int health_value) {
        Log.d(TAG, "checkHealthState: " + health_value);
        if (health_value < 15) {
            Speak("Please continue CPR");
        } else if (health_value > 30) {
            Speak("Over compressions detected");
            health.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else if (health_value < 5) {
            Speak("Patient about to die");
            health.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else if (health_value < 0) {
            Speak("CPR failed");
            winner();
            finish();
        }
    }

    private void drawBarChart() {

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();

        BarDataSet barDataSet1 = new BarDataSet(barValues, "Your Time");
        barDataSet1.setColors(new int[]{ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)});

        dataSets.add(barDataSet1);
        BarData data = new BarData(dataSets);

        data.setValueTextSize(10f);
        data.setBarWidth(1f);
        horizontalBar.setData(data);
        horizontalBar.animateXY(1000, 1000);
        horizontalBar.invalidate();

    }

    private void drawLineChart() {
        LineDataSet set1;

        // create a dataset and give it a type
        set1 = new LineDataSet(yVals, "Compressions");
        set1.setFillAlpha(110);

        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setDrawFilled(true);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(dataSets);

        // set data
        mChart.setData(data);
    }

    private void Speak(String string) {
        textToSpeech.speak(string, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void ToastMessage(String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }


    private void winner() {
        mMediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.cartoon_winning_sound);

        // Start the audio file
        mMediaPlayer.start();
        mMediaPlayer.setOnCompletionListener(mCompletionListener);
    }

    private void ambulance() {
        mMediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.ambulance_sound);

        // Start the audio file
        mMediaPlayer.start();
        mMediaPlayer.setOnCompletionListener(mCompletionListener);
    }


    public void initializeViews() {
        currentX = (TextView) findViewById(R.id.currentX_tv);
        currentY = (TextView) findViewById(R.id.currentY_tv);
        currentZ = (TextView) findViewById(R.id.currentZ_tv);

        maxX = (TextView) findViewById(R.id.maxX_tv);
        maxY = (TextView) findViewById(R.id.maxY_tv);
        maxZ = (TextView) findViewById(R.id.maxZ_tv);

        beatCount = (TextView) findViewById(R.id.counts);
        infalterImage = (ImageView) findViewById(R.id.inflaterImage);
        horizontalBar = findViewById(R.id.horizontalBarchart);

        health = findViewById(R.id.health_checker);

    }

    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(runnableCode);

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // clean current values
        displayCleanValues();
        // display the current x,y,z accelerometer values
        displayCurrentValues();
        // display the max x,y,z accelerometer values
        displayMaxValues();
        //alert for shaking the body on xy plane


        // get the change of the x,y,z values of the accelerometer
        deltaX = Math.abs(lastX - event.values[0]);
        deltaY = Math.abs(lastY - event.values[1]);
        deltaZ = Math.abs(lastZ - event.values[2]);

        // if the change is below 2, it is just plain noise
        if (deltaX < 1)
            deltaX = 0;
        if (deltaY < 1)
            deltaY = 0;
        if (deltaZ < 1)
            deltaZ = 0;

        // set the last know values of x,y,z
        lastX = event.values[0];
        lastY = event.values[1];
        lastZ = event.values[2];

        //checking the body shake


        infalterImage.setScaleX(deltaZ);
        infalterImage.setScaleY(deltaZ);
        //checking the XY movement of the body
        checkMovement();
    }


    // if the change in the accelerometer value is big enough, then vibrate!
    // our threshold is MaxValue/2
    public void checkMovement() {
        //if ((deltaX > vibrateThreshold) || (deltaY > vibrateThreshold) || (deltaZ > vibrateThreshold)) {
        if ((deltaX > 2f) || (deltaY > 2f)) {
            Speak("don't shake the body");
        }
        if ((deltaZ > 5f)) {
            health_value += 1;
        }
    }

    public void displayCleanValues() {
        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");
    }

    // display the current x,y,z accelerometer values
    public void displayCurrentValues() {
        currentX.setText(Float.toString(deltaX));
        currentY.setText(Float.toString(deltaY));
        currentZ.setText(Float.toString(deltaZ));
    }

    // display the max x,y,z accelerometer values
    public void displayMaxValues() {
        if (deltaX > deltaXMax) {
            deltaXMax = deltaX;
            maxX.setText(Float.toString(deltaXMax));
        }
        if (deltaY > deltaYMax) {
            deltaYMax = deltaY;
            maxY.setText(Float.toString(deltaYMax));
        }
        if (deltaZ > deltaZMax) {
            deltaZMax = deltaZ;
            maxZ.setText(Float.toString(deltaZMax));
            CREST = true;
        }

        if (CREST) {
            if (deltaZ < deltaZMax) {
                deltaZMax = deltaZ;
                Toast.makeText(this, "value is : " + deltaXMax, Toast.LENGTH_SHORT).show();
                CREST = false;
            }
        }


    }
}
