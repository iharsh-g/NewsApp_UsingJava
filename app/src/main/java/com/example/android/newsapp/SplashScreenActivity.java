package com.example.android.newsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.ybq.android.spinkit.SpinKitView;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        getSupportActionBar().hide();

        TextView textView1 = findViewById(R.id.splash_title_1);
        TextView textView2 = findViewById(R.id.splash_title_2);
        ImageView imageView = findViewById(R.id.splash_icon);

        YoYo.with(Techniques.SlideInLeft).duration(1000).playOn(imageView);
        YoYo.with(Techniques.SlideInLeft).duration(1000).playOn(textView1);
        YoYo.with(Techniques.SlideInLeft).duration(1000).playOn(textView2);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SpinKitView spinKitView = findViewById(R.id.spin_kit_pb);
                spinKitView.setProgress(2000);

                Intent intent = new Intent(SplashScreenActivity.this, NewsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        }, 2000);
    }
}