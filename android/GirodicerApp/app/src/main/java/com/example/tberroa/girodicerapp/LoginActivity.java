package com.example.tberroa.girodicerapp;

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

public class LoginActivity extends AppCompatActivity {

    protected EditText username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    if(validate()){
                        new AttemptLogin().execute();
                    }
                    handled = true;
                }
                return handled;
            }
        });
        Button loginButton = (Button)findViewById(R.id.login);
        loginButton.setOnClickListener(loginButtonListener);
        TextView registerButton = (TextView)findViewById(R.id.register);
        registerButton.setOnClickListener(registerButtonListener);

        // check if user is already logged on
        UserInfo userInfo = new UserInfo();
        Boolean userLoggedOn = userInfo.getUserStatus(this.getApplicationContext());

        if (userLoggedOn){
            // go to app
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

    }


    private OnClickListener loginButtonListener = new OnClickListener() {
        public void onClick(View v) {

            if(validate()){
                new AttemptLogin().execute();
            }
        }
    };

    private OnClickListener registerButtonListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        }
    };

    class AttemptLogin extends AsyncTask<String, String, String> {

        private String username, password, keyValuePairs, postResponse;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            username = LoginActivity.this.username.getText().toString();
            password = LoginActivity.this.password.getText().toString();
            keyValuePairs = "username="+username+"&password="+password;
        }

        @Override
        protected String doInBackground(String... args) {



            try{
                String url = "http://girodicer.altervista.org/login.php";
                HttpPost httpPost = new HttpPost();
                postResponse = httpPost.doPostRequest(url, keyValuePairs);
                } catch(java.io.IOException e){
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String message) {


            if (message != null){
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();

            }

            if (postResponse.equals("success")) {
                // save user info
                UserInfo userInfo = new UserInfo();
                userInfo.setUsername(getApplicationContext(), username);
                userInfo.setUserStatus(getApplicationContext(), true);

                // go to app
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
            else{
                Toast.makeText(LoginActivity.this, postResponse, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean validate() {
        boolean valid = true;

        String enteredUsername = LoginActivity.this.username.getText().toString();
        String enteredPassword = LoginActivity.this.password.getText().toString();

        // make sure username is alphanumeric and 3 to 15 characters
        if (!enteredUsername.matches("[a-zA-Z0-9]+") || enteredUsername.length() < 3
                || enteredUsername.length() > 15 ) {
            username.setError("3 to 15 alphanumeric characters");
            valid = false;
        } else {
            username.setError(null);
        }

        // make sure password is 6 to 20 characters
        if (enteredPassword.length() < 6 || enteredPassword.length() > 20) {
            password.setError("between 6 and 20 characters");
            valid = false;
        } else {
            password.setError(null);
        }
        return valid;
    }
}


