package com.hyperclock.prashant.cardiacsimulator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.hyperclock.prashant.cardiacsimulator.Configuration.YouTubeConfig;
import com.hyperclock.prashant.cardiacsimulator.Configuration.YouTubeConfig;

public class InformationActivity extends YouTubeBaseActivity {

    Button startButton;
    YouTubePlayerView mYoutubePlayerView;
    YouTubePlayer.OnInitializedListener mOnInitializedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mYoutubePlayerView.initialize(YouTubeConfig.getApiKey(), mOnInitializedListener);
            }
        });

        //mYoutubePlayerView.initialize(YouTubeConfig.getApiKey(), mOnInitializedListener);
        mYoutubePlayerView = findViewById(R.id.youtube_view);

        mOnInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadVideo("Wp2k8s9-TSM");
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };


    }
}
