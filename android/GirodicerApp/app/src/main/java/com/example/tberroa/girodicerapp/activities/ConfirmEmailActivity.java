package com.example.tberroa.girodicerapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.helpers.Utilities;

public class ConfirmEmailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_email);

        Button okayButton = (Button) findViewById(R.id.okay_confirm);
        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utilities.signOut(ConfirmEmailActivity.this);
            }
        });
    }

}
