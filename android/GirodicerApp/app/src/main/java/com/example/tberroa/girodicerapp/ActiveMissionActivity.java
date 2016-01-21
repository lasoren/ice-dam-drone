package com.example.tberroa.girodicerapp;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.Grantee;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;

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

        // mission over, time to upload images
        // initialize the Amazon credentials provider and AmazonS3 Client
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:d64bdcf1-1d5e-441e-ba35-0a3876e4c82c", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        AmazonS3 s3Client = new AmazonS3Client(credentialsProvider);
        TransferUtility transferUtility = new TransferUtility(s3Client, getApplicationContext());

        // upload the images
        String username = "missionphotos";
        String keyName = "Flight 11/Aerial/aerial1.jpg";
        File fileName = new File("/storage/emulated/0/Pictures/Screenshots/screenshot1.png");

        TransferObserver observer = transferUtility.upload(username, keyName, fileName);

    }

    // populate the navigation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu);

        MenuItem item = menu.findItem(R.id.title);
        item.setTitle("Logged in as: " + username);

        return true;
    }
}
