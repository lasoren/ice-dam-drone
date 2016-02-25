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
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.database.TestCase;
import com.example.tberroa.girodicerapp.helpers.ExceptionHandler;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.models.DroneOperator;
import com.example.tberroa.girodicerapp.R;

import org.json.JSONObject;

public class SignInActivity extends AppCompatActivity {

    private EditText email, password;
    final public String LOGIN_URL = Params.BASE_URL + "users/signin.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // create test case database
        new TestCase().Create();

        // check if user is already logged in
        if (new UserInfo().isLoggedIn(this)){ // if so, send them to the client manager
            startActivity(new Intent(SignInActivity.this, ClientManagerActivity.class));
            finish();
        }

        // initialize text boxes for user to enter their information
        email = (EditText)findViewById(R.id.email);
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
            startActivity(new Intent(SignInActivity.this, RegisterActivity.class));
            finish();
        }
    };

    private void Login(){
        String enteredUsername = email.getText().toString();
        String enteredPassword = password.getText().toString();

        Bundle enteredInfo = new Bundle();
        enteredInfo.putString("email", enteredUsername);
        enteredInfo.putString("password", enteredPassword);

        String response = Utilities.validate(enteredInfo);
        if (response.matches("")){
            new AttemptLogin().execute();
        }
        else{
            if (response.contains("email")){
                email.setError(getResources().getString(R.string.enter_valid_email));
            }
            else{
                email.setError(null);
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

        private String email, password, dataJSON, postResponse;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            email = SignInActivity.this.email.getText().toString();
            password = SignInActivity.this.password.getText().toString();

            JSONObject signinJson = new JSONObject();
            try{
                signinJson.put("email", email);
                signinJson.put("password", password);
            }catch (Exception e){
                new ExceptionHandler().HandleException(e);
            }

            dataJSON = signinJson.toString();
        }

        @Override
        protected Void doInBackground(Void... params) {
            /* LIVE code
            try{
                String url = LOGIN_URL;
                postResponse = new HttpPost().doPostRequest(url, dataJSON);
            } catch(java.io.IOException e){
                new ExceptionHandler().HandleException(e);
            }
            */

            // TEST code
            postResponse = "id";
            return null;
        }

        protected void onPostExecute(Void param) {
            if (postResponse.contains("id")) {
                /* LIVE code
                // create DroneOperator model from response json
                Type droneOperator = new TypeToken<DroneOperator>(){}.getType();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                DroneOperator operator = gson.fromJson(postResponse, droneOperator);
                */

                // TEST code
                DroneOperator operator = new LocalDB().getOperator();

                // sign in
                Utilities.SignIn(SignInActivity.this, operator);
            }
            else{ // display error
                Toast.makeText(SignInActivity.this, postResponse, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
