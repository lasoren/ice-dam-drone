package com.example.tberroa.girodicerapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
public class LoginActivity extends BaseActivity{

    protected EditText username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        Button loginButton = (Button)findViewById(R.id.login);
        loginButton.setOnClickListener(loginButtonListener);

        Button registerButton = (Button)findViewById(R.id.register);
        registerButton.setOnClickListener(registerButtonListener);

    }


    private OnClickListener loginButtonListener = new OnClickListener() {
        public void onClick(View v) {
            new AttemptLogin().execute();
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

            Toast.makeText(LoginActivity.this, postResponse, Toast.LENGTH_SHORT).show();
            if (message != null){
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();

            }

            if (postResponse.equals("success")) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        }
    }
}


