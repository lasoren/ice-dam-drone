package com.example.tberroa.girodicerapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class MissionActivity extends AppCompatActivity {

    private Context app_context;
    private Mission mission;
    private int mission_num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);

        // set context
        app_context = this.getApplicationContext();

        // set tab layout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Aerial"));
        tabLayout.addTab(tabLayout.newTab().setText("Thermal"));
        tabLayout.addTab(tabLayout.newTab().setText("IceDams"));
        tabLayout.addTab(tabLayout.newTab().setText("Salt"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // grab mission json and mission number from intent
        Intent intent = getIntent();
        String jsonMission = intent.getExtras().getString("mission");
        mission_num = intent.getExtras().getInt("mission_num");
        // unpack mission json into Mission object
        Gson gson = new Gson();
        Type singleMission = new TypeToken<Mission>(){}.getType();
        mission = gson.fromJson(jsonMission, singleMission);

        // set toolbar
        Toolbar missionToolbar = (Toolbar) findViewById(R.id.mission_toolbar);
        missionToolbar.setTitle("Mission "+Integer.toString(mission_num));
        setSupportActionBar(missionToolbar);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), mission_num, mission.num_of_aerials);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    // get Mission
    public Mission getMission(){
        return mission;
    }

    // populate the navigation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    // implement navigation functions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.end_mission:
                // user chose the "End Mission" item
                ServiceStatus service_status = new ServiceStatus(app_context);
                if (!service_status.getServiceStatus()) {   // if there is no mission in progress
                    // tell user there is no mission in progress
                    AlertDialog no_active_mission = no_active_mission_createDialog();
                    no_active_mission.show();
                }
                else{   // otherwise, there is a mission in progress
                    // ask user for confirmation to end mission
                    AlertDialog confirm_end_mission = confirm_end_mission_createDialog();
                    confirm_end_mission.show();
                }
                return true;
            case R.id.current_mission:
                // user chose the "Current Mission" item, change activity
                Intent current_mission = new Intent(MissionActivity.this,ActiveMissionActivity.class);
                startActivity(current_mission);
                return true;
            default:
                // the users action was not recognized, invoke the superclass to handle it
                return super.onOptionsItemSelected(item);
        }
    }

    private AlertDialog no_active_mission_createDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage(R.string.no_active_mission)
                .setCancelable(true)
                .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        return alertDialogBuilder.create();
    }

    private AlertDialog confirm_end_mission_createDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage(R.string.confirm_end_mission)
                .setCancelable(true)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        stopService(new Intent(MissionActivity.this, AdvertiseService.class));
                    }
                });
        return alertDialogBuilder.create();
    }
}
