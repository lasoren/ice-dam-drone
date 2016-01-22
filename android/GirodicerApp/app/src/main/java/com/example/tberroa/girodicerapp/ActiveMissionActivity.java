package com.example.tberroa.girodicerapp;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class ActiveMissionActivity extends BaseActivity {

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_mission);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Current Mission");
        setSupportActionBar(toolbar);

        // grab username
        UserInfo userInfo = new UserInfo();
        username = userInfo.getUsername(this.getApplicationContext());
    }

    // populate the navigation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu);

        MenuItem item = menu.findItem(R.id.title);
        item.setTitle("Logged in as: "+username);

        return true;
    }
}
