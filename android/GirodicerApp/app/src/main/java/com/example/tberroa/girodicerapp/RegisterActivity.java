package com.example.tberroa.girodicerapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
public class RegisterActivity extends BaseActivity{

    private EditText username, password, confirmPassword, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        confirmPassword = (EditText)findViewById(R.id.confirm_password);
        email = (EditText)findViewById(R.id.email);
        email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    new AttemptRegistration().execute();
                    handled = true;
                }
                return handled;
            }
        });
        Button createAccountButton = (Button)findViewById(R.id.create_account);
        createAccountButton.setOnClickListener(createAccountButtonListener);
        Button goToLoginButton = (Button)findViewById(R.id.go_to_login);
        goToLoginButton.setOnClickListener(goToLoginButtonListener);
    }

    private OnClickListener createAccountButtonListener = new OnClickListener() {
        public void onClick(View v) {
            new AttemptRegistration().execute();
        }
    };

    private OnClickListener goToLoginButtonListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        }
    };

    class AttemptRegistration extends AsyncTask<String, String, String> {

        private String username, password, confirmPassword, email, keyValuePairs, postResponse;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            username = RegisterActivity.this.username.getText().toString();
            password = RegisterActivity.this.password.getText().toString();
            confirmPassword = RegisterActivity.this.confirmPassword.getText().toString();
            email = RegisterActivity.this.email.getText().toString();
            keyValuePairs = "username="+username+
                            "&password="+password+
                            "&confirmPassword="+confirmPassword+
                            "&email="+email;
        }

        @Override
        protected String doInBackground(String... args) {
            try{
                String url = "http://girodicer.altervista.org/register.php";
                HttpPost httpPost = new HttpPost();
                postResponse = httpPost.doPostRequest(url, keyValuePairs);
            } catch(java.io.IOException e){
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String message) {
            Toast.makeText(RegisterActivity.this, postResponse, Toast.LENGTH_LONG).show();
            if (message != null){
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}


