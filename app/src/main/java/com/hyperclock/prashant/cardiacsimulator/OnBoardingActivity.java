package com.hyperclock.prashant.cardiacsimulator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.hyperclock.prashant.cardiacsimulator.certifications.Certifications;

import java.util.ArrayList;
import java.util.HashMap;

public class OnBoardingActivity extends Activity implements BaseSliderView.OnSliderClickListener,
        ViewPagerEx.OnPageChangeListener {

    private HashMap<String, Integer> HashMapForLocalRes;
    private SliderLayout sliderLayout;
    private PieChart pieChart;
    private Context context;

    private ArrayList<Certifications> certifications = new ArrayList<>();

    private TextView performCPR;
    private Button informationView, certificationButton, learnCpr;

    @Override
    protected void onStop() {
        super.onStop();
        sliderLayout.stopAutoCycle();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        performCPR = findViewById(R.id.perform_cpr);
        performCPR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(OnBoardingActivity.this);
                builder.setTitle(R.string.cpr_modes);
                //builder.setMessage(R.string.cpr_mode_string);
                builder.setItems(R.array.cpr_mode_array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int item) {
                        if (item == 0) {
                            Intent intent = new Intent(OnBoardingActivity.this, MainActivity.class);
                            intent.putExtra("cpr_mode", false);
                            startActivity(intent);
                        } else if (item == 1) {
                            Intent intent = new Intent(OnBoardingActivity.this, MainActivity.class);
                            intent.putExtra("cpr_mode", true);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(OnBoardingActivity.this, MainActivity.class);
                            intent.putExtra("cpr_mode", true);
                            startActivity(intent);
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        informationView = findViewById(R.id.information_view);
        informationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OnBoardingActivity.this, InformationActivity.class));
            }
        });

        learnCpr = findViewById(R.id.learncpr_btn);
        learnCpr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OnBoardingActivity.this, LearnCprActivity.class));
            }
        });


        certifications.add(new Certifications("Red Cross", "https://www.redcross.org/take-a-class/cpr/cpr-training/cpr-certification"));
        certifications.add(new Certifications("American Academy of CPR", "https://www.onlinecprcertification.net/"));
        certifications.add(new Certifications("First Aid Web", "https://www.firstaidweb.com/"));
        certifications.add(new Certifications("Indigo medical training", "https://www.cprcertified.com/our-courses"));

        certificationButton = findViewById(R.id.certification_button);
        certificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OnBoardingActivity.this);
                builder.setTitle(R.string.certifications_string);
                builder.setItems(R.array.certifications, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int item) {
                        if (item == 0) {
                            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(certifications.get(0).getCertificationUrl())));
                        } else if (item == 1) {
                            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(certifications.get(1).getCertificationUrl())));
                        } else if (item == 2) {
                            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(certifications.get(2).getCertificationUrl())));
                        } else if (item == 3) {
                            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(certifications.get(3).getCertificationUrl())));
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        pieChart = findViewById(R.id.piechart);
        loadPieChart(59f);

        sliderLayout = (SliderLayout) findViewById(R.id.slider_id);
        AddImageUrlFormLocalRes();

        for (String name : HashMapForLocalRes.keySet()) {

            TextSliderView textSliderView = new TextSliderView(OnBoardingActivity.this);

            textSliderView
                    .description(name)
                    .image(HashMapForLocalRes.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);

            textSliderView.bundle(new Bundle());

            textSliderView.getBundle()
                    .putString("extra", name);

            sliderLayout.addSlider(textSliderView);
        }
        sliderLayout.setPresetTransformer(SliderLayout.Transformer.DepthPage);
        sliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        sliderLayout.setCustomAnimation(new DescriptionAnimation());
        sliderLayout.setDuration(10000);
        sliderLayout.addOnPageChangeListener(OnBoardingActivity.this);

    }

    private void loadPieChart(Float completionStatus) {

        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(true);
        pieChart.setExtraOffsets(2, 2, 2, 2);
        pieChart.setDragDecelerationFrictionCoef(0.9f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.animateY(1000, Easing.EasingOption.EaseInOutCubic);
        ArrayList<PieEntry> yValues = new ArrayList<>();
        yValues.add(new PieEntry(completionStatus, "Completed"));
        yValues.add(new PieEntry(100f - completionStatus, "Remaining"));

        PieDataSet dataSet = new PieDataSet(yValues, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData pieData = new PieData((dataSet));
        pieData.setValueTextSize(10f);
        pieData.setValueTextColor(Color.YELLOW);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(R.color.transparent);
        pieChart.setHoleRadius(90f);
        pieChart.setData(pieData);
    }

    private void AddImageUrlFormLocalRes() {
        HashMapForLocalRes = new HashMap<String, Integer>();

        HashMapForLocalRes.put(getResources().getString(R.string.string_1), R.drawable.kid_performs_cpr);
        HashMapForLocalRes.put(getResources().getString(R.string.string_2), R.drawable.seattle_city_image);
        HashMapForLocalRes.put(getResources().getString(R.string.string_3), R.drawable.performing_cpr);
        HashMapForLocalRes.put(getResources().getString(R.string.string_4), R.drawable.performing_cpr_on_time);
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
        Toast.makeText(this, slider.getBundle().get("extra") + "", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

}
