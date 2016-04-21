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
import com.example.tberroa.girodicerapp.data.CurrentInspectionInfo;
import com.example.tberroa.girodicerapp.data.OperatorInfo;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.Provisions;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.models.DroneOperator;
import com.example.tberroa.girodicerapp.network.Http;
import com.example.tberroa.girodicerapp.services.SignInIntentService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;

public class SignInActivity extends AppCompatActivity {

    private EditText email, password;
    private boolean inView;

    // onClick listeners
    private final OnClickListener signInButtonListener = new OnClickListener() {
        public void onClick(View v) {
            start();
        }
    };
    private final OnClickListener goToRegisterButtonListener = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(SignInActivity.this, RegisterActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).setAction(Params.RELOAD);
            startActivity(intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // no animation if starting due to a reload
        String action = getIntent().getAction();
        if (action != null && action.equals(Params.RELOAD)) {
            overridePendingTransition(0, 0);
        }

        // check if user is already logged in
        if (new UserInfo().isLoggedIn(this)) { // if so, send them to the client manager
            startActivity(new Intent(SignInActivity.this, ClientManagerActivity.class));
            finish();
        }

        // initialize text boxes for user to enter their information
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        // allow user to submit form via keyboard
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    start();
                    handled = true;
                }
                return handled;
            }
        });

        // declare and initialize buttons
        Button signInButton = (Button) findViewById(R.id.sign_in);
        signInButton.setOnClickListener(signInButtonListener);
        TextView goToRegisterButton = (TextView) findViewById(R.id.register);
        goToRegisterButton.setOnClickListener(goToRegisterButtonListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        inView = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        inView = false;
    }

    private void start() {
        String enteredUsername = email.getText().toString();
        String enteredPassword = password.getText().toString();

        Bundle enteredInfo = new Bundle();
        enteredInfo.putString("email", enteredUsername);
        enteredInfo.putString("password", enteredPassword);

        String response = Utilities.validate(enteredInfo);
        if (response.matches("")) {
            if (Utilities.isInternetAvailable(this)) {
                new AttemptSignIn().execute();
            } else {
                Toast.makeText(this, R.string.internet_not_available, Toast.LENGTH_SHORT).show();
            }
        } else {
            if (response.contains("email")) {
                email.setError(getResources().getString(R.string.enter_valid_email));
            } else {
                email.setError(null);
            }
            if (response.contains("pass_word")) {
                password.setError(getResources().getString(R.string.password_format));
            } else {
                password.setError(null);
            }
        }
    }

    private void signIn(DroneOperator operator) {
        final OperatorInfo operatorInfo = new OperatorInfo();

        // clear old data
        operatorInfo.clear(this);
        new CurrentInspectionInfo().clearAll(this);
        new Provisions().clear(this);
        new LocalDB().clear();

        // save operator info
        operatorInfo.setOperatorId(this, operator.id);
        operatorInfo.setUserId(this, operator.user.id);
        operatorInfo.setSessionId(this, operator.session_id);
        operatorInfo.setFirstName(this, operator.user.first_name);
        operatorInfo.setLastName(this, operator.user.last_name);
        operatorInfo.setEmail(this, operator.user.email);

        // save this operator to local storage
        operator.cascadeSave();

        // start sign in intent service
        startService(new Intent(this, SignInIntentService.class));

        // go to splash page if app is in view
        if (inView) {
            startActivity(new Intent(this, SplashActivity.class));

            // apply sign in animation for entering splash page
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }

        finish();
    }

    class AttemptSignIn extends AsyncTask<Void, Void, Void> {

        private String email, password, dataJSON, postResponse;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            email = SignInActivity.this.email.getText().toString();
            password = SignInActivity.this.password.getText().toString();

            JSONObject signinJson = new JSONObject();
            try {
                signinJson.put("email", email);
                signinJson.put("password", password);
            } catch (Exception e) {
                e.printStackTrace();
            }

            dataJSON = signinJson.toString();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String url = Params.BASE_URL + "users/signin.json";
            postResponse = new Http().postRequest(url, dataJSON);

            return null;
        }

        protected void onPostExecute(Void param) {
            if (postResponse != null && postResponse.contains("id")) {
                // create DroneOperator model from response json
                Type droneOperator = new TypeToken<DroneOperator>() {
                }.getType();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                DroneOperator operator = gson.fromJson(postResponse, droneOperator);

                // sign in
                signIn(operator);
            } else { // display error
                if (postResponse == null) {
                    Toast.makeText(SignInActivity.this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignInActivity.this, postResponse, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
