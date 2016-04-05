package com.example.tberroa.girodicerapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.Params;

public class SplashActivity extends AppCompatActivity {

    private boolean inView = false;
    private boolean serviceComplete = false;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView textView = (TextView) findViewById(R.id.general_message);
        textView.setText(R.string.grabbing_data);
        textView.setVisibility(View.VISIBLE);

        // setup receiver to listen for when sign in service completes
        IntentFilter filter = new IntentFilter(Params.SIGN_IN_SERVICE_COMPLETE);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case Params.SIGN_IN_SERVICE_COMPLETE:
                        serviceComplete = true;
                        if (inView) {
                            startActivity(new Intent(SplashActivity.this, ClientManagerActivity.class));
                            finish();
                        }
                        break;
                }
            }
        };
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        inView = true;
        if (serviceComplete){
            startActivity(new Intent(SplashActivity.this, ClientManagerActivity.class));
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        inView = false;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
    }
}
