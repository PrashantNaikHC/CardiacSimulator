package com.hyperclock.prashant.cardiacsimulator;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hyperclock.prashant.cardiacsimulator.Adapter.SliderAdapter;

public class LearnCprActivity extends AppCompatActivity {

    private static final String TAG = "LearnCprActivity";
    public static int currentItem;
    private ViewPager viewPager;
    private SliderAdapter sliderAdapter;
    private Button next, previous;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_cpr);

        next = (Button) findViewById(R.id.next_btn);
        previous = (Button) findViewById(R.id.previous_btn);

        viewPager = findViewById(R.id.viewpager);

        sliderAdapter = new SliderAdapter(this);
        viewPager.setAdapter(sliderAdapter);

        next.setOnClickListener(new View.OnClickListener() {
            int currentPosition = viewPager.getCurrentItem();


            @Override
            public void onClick(View view) {
                int currentPosition = viewPager.getCurrentItem();

                Log.d(TAG, "onClick: current page" + currentPosition);
                if (currentPosition == 5) {
                    Toast.makeText(LearnCprActivity.this, "This is the last step", Toast.LENGTH_SHORT);
                } else {
                    viewPager.setCurrentItem(currentPosition + 1);
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                int currentPosition = viewPager.getCurrentItem();
                Log.d(TAG, "onClick: current page" + currentPosition);

                if (currentPosition == 0) {
                    Toast.makeText(LearnCprActivity.this, "This is the first step", Toast.LENGTH_SHORT);
                } else {
                    viewPager.setCurrentItem(currentPosition - 1);
                }
            }
        });

    }
}
