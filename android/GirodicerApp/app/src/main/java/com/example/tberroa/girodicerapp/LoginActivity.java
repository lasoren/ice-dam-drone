package com.example.tberroa.girodicerapp;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
public class LoginActivity extends BaseActivity implements OnClickListener{

    protected EditText username, password;
    private ProgressDialog progressDialog;
    private String url = "http://smplicity.altervista.org/login_girodicer.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        Button loginButton = (Button)findViewById(R.id.login);
        loginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                new AttemptLogin().execute();
                // here we have used, switch case, because on login activity you may //also want to show registration button, so if the username is new ! we can go the //registration activity , other than this we could also do this without switch //case.
            default:
                break;
        }
    }

    class AttemptLogin extends AsyncTask<String, String, String> {

        private String username, password, keyValuePairs, postResponse;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            username = LoginActivity.this.username.getText().toString();
            password = LoginActivity.this.password.getText().toString();
            keyValuePairs = "username="+username+"&password="+password;
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("Attempting to login...");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

            try{
                HttpPost httpPost = new HttpPost();
                postResponse = httpPost.doPostRequest(url, keyValuePairs);
                } catch(java.io.IOException e){
                e.printStackTrace();
            }
            return null;
        }
        /**
         * Once the background process is done we need to  Dismiss the progress dialog asap
         * **/
        protected void onPostExecute(String message) {


            progressDialog.dismiss();
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


