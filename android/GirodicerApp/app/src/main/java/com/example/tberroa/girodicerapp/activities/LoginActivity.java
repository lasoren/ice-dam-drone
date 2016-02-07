package com.example.tberroa.girodicerapp.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tberroa.girodicerapp.data.ActiveMissionInfo;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.PreviousMissionsInfo;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.network.HttpPost;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.services.FetchPMIntentService;

public class LoginActivity extends AppCompatActivity {

    private EditText username, password;
    private final UserInfo userInfo = new UserInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // check if user is already logged in
        if (userInfo.isLoggedIn(this)){ // if so, send them to the main screen
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        // initialize text boxes for user to enter their information
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);

        // allow user to submit form via keyboard
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    Login();
                    handled = true;
                }
                return handled;
            }
        });

        // declare and initialize buttons
        Button loginButton = (Button)findViewById(R.id.login);
        loginButton.setOnClickListener(loginButtonListener);
        TextView registerButton = (TextView)findViewById(R.id.register);
        registerButton.setOnClickListener(registerButtonListener);
    }

    private final OnClickListener loginButtonListener = new OnClickListener() {
        public void onClick(View v) {
            Login();
        }
    };

    private final OnClickListener registerButtonListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        }
    };

    private void Login(){
        String enteredUsername = username.getText().toString();
        String enteredPassword = password.getText().toString();

        Bundle enteredInfo = new Bundle();
        enteredInfo.putString("username", enteredUsername);
        enteredInfo.putString("password", enteredPassword);

        String response = Utilities.validate(enteredInfo);
        if (response.matches("")){
            new AttemptLogin().execute();
        }
        else{
            if (response.contains("username")){
                username.setError(getResources().getString(R.string.username_format));
            }
            else{
                username.setError(null);
            }
            if (response.contains("password")){
                password.setError(getResources().getString(R.string.password_format));
            }
            else{
                password.setError(null);
            }
        }
    }

    class AttemptLogin extends AsyncTask<Void, Void, Void> {

        private String username, password, keyValuePairs, postResponse;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            username = LoginActivity.this.username.getText().toString();
            password = LoginActivity.this.password.getText().toString();
            keyValuePairs = "username="+username+"&password="+password;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                String url = Params.LOGIN_URL;
                postResponse = new HttpPost().doPostRequest(url, keyValuePairs);
            } catch(java.io.IOException e){
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void param) {
            if (postResponse.equals(Params.LOGIN_SUCCESS)) {
                // clear all local data
                userInfo.clearAll(LoginActivity.this);
                new ActiveMissionInfo().clearAll(LoginActivity.this);
                PreviousMissionsInfo previousMissionsInfo = new PreviousMissionsInfo();
                previousMissionsInfo.clearAll(LoginActivity.this);

                // save the new user info
                userInfo.setUsername(LoginActivity.this, username);
                userInfo.setUserStatus(LoginActivity.this, true);

                // grab the new users previous missions
                if (!previousMissionsInfo.isFetching(LoginActivity.this)){
                    Intent fetch = new Intent(LoginActivity.this, FetchPMIntentService.class);
                    fetch.putExtra("username", username);
                    startService(fetch);
                }

                // go to app
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
            else{ // display error
                Toast.makeText(LoginActivity.this, postResponse, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
