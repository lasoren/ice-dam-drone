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

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.helpers.ExceptionHandler;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.network.HttpPost;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private EditText firstName, lastName, password, confirmPassword, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getIntent().getAction() != null){
            overridePendingTransition(0, 0);
        }

        // check if user is already logged in
        if (new UserInfo().isLoggedIn(this)){
            startActivity(new Intent(RegisterActivity.this, ClientManagerActivity.class));
            finish();
        }

        // initialize text boxes for user to enter their information
        firstName = (EditText)findViewById(R.id.first_name);
        lastName = (EditText)findViewById(R.id.last_name);
        password = (EditText)findViewById(R.id.password);
        confirmPassword = (EditText)findViewById(R.id.confirm_password);
        email = (EditText)findViewById(R.id.email);

        // allow user to submit form via keyboard
        email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    Register();
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
            Register();
        }
    };

    private final OnClickListener goToLoginButtonListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(RegisterActivity.this, SignInActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).setAction(Params.RELOAD));
            finish();
        }
    };

    private void Register(){
        String enteredFirstName = firstName.getText().toString();
        String enteredLastName = lastName.getText().toString();
        String enteredPassword = password.getText().toString();
        String enteredConfirmPassword = confirmPassword.getText().toString();
        String enteredEmail = email.getText().toString();

        Bundle enteredInfo = new Bundle();
        enteredInfo.putString("first_name", enteredFirstName);
        enteredInfo.putString("last_ame", enteredLastName);
        enteredInfo.putString("password", enteredPassword);
        enteredInfo.putString("confirm_password", enteredConfirmPassword);
        enteredInfo.putString("email", enteredEmail);

        String string = Utilities.validate(enteredInfo);
        if (string.matches("")){
            new AttemptRegistration().execute();
        }
        else{
            if (string.contains("first_name")){
                firstName.setError(getResources().getString(R.string.name_format));
            }
            else{
                firstName.setError(null);
            }
            if (string.contains("last_ame")){
                lastName.setError(getResources().getString(R.string.name_format));
            }
            else{
                lastName.setError(null);
            }
            if (string.contains("password")){
                password.setError(getResources().getString(R.string.password_format));
            }
            else{
                password.setError(null);
            }
            if (string.contains("confirm_password")){
                confirmPassword.setError(getResources().getString(R.string.password_mismatch));
            }
            else{
                confirmPassword.setError(null);
            }
            if (string.contains("email")){
                email.setError(getResources().getString(R.string.enter_valid_email));
            }
            else{
                email.setError(null);
            }
        }
    }

    class AttemptRegistration extends AsyncTask<Void, Void, Void> {

        private String firstName, lastName, password, email, dataJSON, postResponse;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            firstName = RegisterActivity.this.firstName.getText().toString();
            lastName = RegisterActivity.this.lastName.getText().toString();
            password = RegisterActivity.this.password.getText().toString();
            email = RegisterActivity.this.email.getText().toString();

            JSONObject registerJson = new JSONObject();
            JSONObject userJson = new JSONObject();
            try{
                userJson.put("first_name", firstName);
                userJson.put("last_name", lastName);
                userJson.put("email", email);
                registerJson.put("user", userJson);
                registerJson.put("password", password);

            }catch (Exception e){
                new ExceptionHandler().HandleException(e);
            }

            dataJSON = registerJson.toString();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                String url = Params.BASE_URL + "users/register.json";
                postResponse = new HttpPost().doPostRequest(url, dataJSON);
            } catch(java.io.IOException e){
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void param) {
            if (postResponse.contains("id")){
                startActivity(new Intent(RegisterActivity.this, ConfirmEmailActivity.class));
                finish();
            }
            else{ // display error
                Toast.makeText(RegisterActivity.this, postResponse, Toast.LENGTH_LONG).show();
            }
        }
    }
}
