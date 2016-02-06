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

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.network.HttpPost;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.UserInfo;

public class RegisterActivity extends AppCompatActivity {

    private EditText username, password, confirmPassword, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // check if user is already logged in
        if (new UserInfo().isLoggedIn(this)){
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        }

        // initialize text boxes for user to enter their information
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        confirmPassword = (EditText)findViewById(R.id.confirm_password);
        email = (EditText)findViewById(R.id.email);

        // allow user to submit form via keyboard
        email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    if (validate()){
                        new AttemptRegistration().execute();
                    }
                    handled = true;
                }
                return handled;
            }
        });

        // declare and initialize buttons
        Button createAccountButton = (Button)findViewById(R.id.create_account);
        createAccountButton.setOnClickListener(createAccountButtonListener);
        TextView goToLoginButton = (TextView)findViewById(R.id.go_to_login);
        goToLoginButton.setOnClickListener(goToLoginButtonListener);
    }

    private final OnClickListener createAccountButtonListener = new OnClickListener() {
        public void onClick(View v) {
            if(validate()){
                new AttemptRegistration().execute();
            }
        }
    };

    private final OnClickListener goToLoginButtonListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        }
    };

    class AttemptRegistration extends AsyncTask<Void, Void, Void> {

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
        protected Void doInBackground(Void... params) {
            try{
                String url = Params.REGISTER_URL;
                postResponse = new HttpPost().doPostRequest(url, keyValuePairs);
            } catch(java.io.IOException e){
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void param) {
            if (postResponse.equals(Params.REGISTER_SUCCESS)){
                // save user info
                UserInfo userInfo = new UserInfo();
                userInfo.setUsername(getApplicationContext(), username);
                userInfo.setUserStatus(getApplicationContext(), true);

                // go to app
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }
            else{ // display error
                Toast.makeText(RegisterActivity.this, postResponse, Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean validate() {
        boolean valid = true;

        String enteredUsername = RegisterActivity.this.username.getText().toString();
        String enteredPassword = RegisterActivity.this.password.getText().toString();
        String enteredConfirmPassword = RegisterActivity.this.confirmPassword.getText().toString();
        String enteredEmail = RegisterActivity.this.email.getText().toString();

        // make sure username is of proper format
        if (!enteredUsername.matches("[a-zA-Z0-9]+") || enteredUsername.length() < 3
                || enteredUsername.length() > 15 ) {
            username.setError(getResources().getString(R.string.username_format));
            valid = false;
        } else {
            username.setError(null);
        }

        // make sure password is of proper format
        if (enteredPassword.length() < 6 || enteredPassword.length() > 20) {
            password.setError(getResources().getString(R.string.password_format));
            valid = false;
        } else {
            password.setError(null);
        }

        // make sure passwords match
        if (!enteredConfirmPassword.equals(enteredPassword)) {
            confirmPassword.setError(getResources().getString(R.string.password_mismatch));
            valid = false;
        } else {
            confirmPassword.setError(null);
        }

        // make sure email is valid
        if (enteredEmail.isEmpty() ||
                !android.util.Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches()) {
            email.setError(getResources().getString(R.string.enter_valid_email));
            valid = false;
        } else {
            email.setError(null);
        }
        return valid;
    }
}
