package com.sefaozgur.haydisahayaap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.Random;

public class SplashScreen extends AppCompatActivity {

    private static int DELAY_TIME = 3000;
    private TextView splashText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        getSupportActionBar().hide();

        final int random = new Random().nextInt(5); // [0, 4]
        String[] myString = {"'Çalım Atmak Yok Paslı Oynıcaz.'"
                ,"'Ben yoruldum kaleye geçiyim.'"
                ,"'Allahını Seven Defansa Gelsin !!!'"
                ,"'Takımları değişelim böyle dengesiz oldu.'"
                ,"'Abi eldiven yok uçamıyorum ki!"};

        //widget
        splashText = findViewById(R.id.splash_text_view);
        splashText.setText(myString[random]);

        //animations
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.animation);
        splashText.startAnimation(animation);

        //Geçiş
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this,SignActivity.class);
                startActivity(intent);
                finish();
            }
        },DELAY_TIME);
    }
}