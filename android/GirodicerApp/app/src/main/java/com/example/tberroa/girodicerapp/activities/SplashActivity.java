package com.example.tberroa.girodicerapp.activities;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;

public class SplashActivity extends AppCompatActivity {

    private boolean inView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView textView = (TextView) findViewById(R.id.general_message);
        textView.setText(R.string.grabbing_data);
        textView.setVisibility(View.VISIBLE);

        // go to client manager after 3 seconds have passed
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (inView) {
                    startActivity(new Intent(SplashActivity.this, ClientManagerActivity.class));
                }
                finish();
            }
        }, 3000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        inView = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        inView = false;
    }
}
