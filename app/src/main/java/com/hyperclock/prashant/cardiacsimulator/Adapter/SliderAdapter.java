package com.hyperclock.prashant.cardiacsimulator.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyperclock.prashant.cardiacsimulator.R;
import com.hyperclock.prashant.cardiacsimulator.StepOneActivity;

public class SliderAdapter extends PagerAdapter {

    public int[] slide_images = {
            R.drawable.kid_performs_cpr,
            R.drawable.chest_image,
            R.drawable.cpr_image,
            R.drawable.heart_background,
            R.drawable.seattle_city_image,
            R.drawable.performing_cpr_on_time
    };
    public int[] slide_description = {
            R.string.descript_1,
            R.string.descript_2,
            R.string.descript_3,
            R.string.descript_4,
            R.string.descript_5,
            R.string.descript_6
    };
    public int[] slide_title = {
            R.string.step_1,
            R.string.step_2,
            R.string.step_3,
            R.string.step_4,
            R.string.step_5,
            R.string.step_6
    };
    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return slide_description.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == (LinearLayout) o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_layout, container, false);

        TextView titleSlider = (TextView) view.findViewById(R.id.title_slider);
        TextView descriptionSlider = (TextView) view.findViewById(R.id.description_slider);
        ImageView BoardingImage = (ImageView) view.findViewById(R.id.faceImage);
        Button tryButton = (Button) view.findViewById(R.id.tryButton);


        BoardingImage.setImageResource(slide_images[position]);
        titleSlider.setText(slide_title[position]);
        descriptionSlider.setText(slide_description[position]);

        tryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (position) {
                    case 1:
                        context.startActivity(new Intent(context, StepOneActivity.class));
                    default:
                        context.startActivity(new Intent(context, StepOneActivity.class));
                }
            }
        });

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }
}
