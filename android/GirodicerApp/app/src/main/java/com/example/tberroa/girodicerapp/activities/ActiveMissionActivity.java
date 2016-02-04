package com.example.tberroa.girodicerapp.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.UserInfo;

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
        username = new UserInfo().getUsername(this);
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
