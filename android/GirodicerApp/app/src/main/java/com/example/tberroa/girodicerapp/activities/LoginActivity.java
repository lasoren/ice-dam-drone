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

import com.example.tberroa.girodicerapp.network.HttpPost;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.UserInfo;

public class LoginActivity extends AppCompatActivity {

    private EditText username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // check if user is already logged in
        if (new UserInfo().isLoggedIn(this)){
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
                    if(validate()){
                        new AttemptLogin().execute();
                    }
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
            if(validate()){
                new AttemptLogin().execute();
            }
        }
    };

    private final OnClickListener registerButtonListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        }
    };

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
                String url = "http://girodicer.altervista.org/login.php";
                postResponse = new HttpPost().doPostRequest(url, keyValuePairs);
            } catch(java.io.IOException e){
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void param) {
            if (postResponse.equals("success")) {
                // save user info
                UserInfo userInfo = new UserInfo();
                userInfo.setUsername(LoginActivity.this, username);
                userInfo.setUserStatus(LoginActivity.this, true);

                // go to app
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
            else{ // display error
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


