package com.example.tberroa.girodicerapp.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.activities.ClientManagerActivity;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.database.ServerDB;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.models.Client;

public class CreateClientDialog extends Dialog {

    private final Context context;
    private EditText firstName, lastName, email, streetAddress, cityTown, state, zipCode;
    private Client client;
    private boolean noError = true;

    public CreateClientDialog(Context context) {
        super(context, R.style.dialogStyle);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_create_client);

        // initialize form
        firstName = (EditText) findViewById(R.id.first_name);
        lastName = (EditText) findViewById(R.id.last_name);
        email = (EditText) findViewById(R.id.email);
        streetAddress = (EditText) findViewById(R.id.street_address);
        cityTown = (EditText) findViewById(R.id.city_town);
        state = (EditText) findViewById(R.id.state);
        zipCode = (EditText) findViewById(R.id.zip_code);
        Button submit = (Button) findViewById(R.id.submit);

        // set on click listener to submit button
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (valid()) {
                    createClient();
                }
            }
        });

        // allow user to submit form via keyboard
        zipCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    if (valid()) {
                        createClient();
                    }
                    handled = true;
                }
                return handled;
            }
        });

        setTitle(R.string.add_client);
        setCancelable(true);
    }

    private boolean valid() {
        boolean valid = true;

        String first = firstName.getText().toString().trim();
        if (first.length() > 12 || !first.matches("[a-zA-Z]+")) {
            firstName.setError(context.getResources().getString(R.string.name_format));
            valid = false;
        } else {
            firstName.setError(null);
        }

        String last = lastName.getText().toString().trim();
        if (last.length() > 12 || !last.matches("[a-zA-Z]+")) {
            lastName.setError(context.getResources().getString(R.string.name_format));
            valid = false;
        } else {
            lastName.setError(null);
        }

        String emailString = email.getText().toString().trim();
        if (emailString.length() == 0 || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailString).matches()) {
            email.setError(context.getResources().getString(R.string.enter_valid_email));
            valid = false;
        } else {
            email.setError(null);
        }

        if (streetAddress.getText().toString().trim().length() == 0) {
            streetAddress.setError(context.getResources().getString(R.string.field_empty));
            valid = false;
        } else {
            streetAddress.setError(null);
        }

        if (cityTown.getText().toString().trim().length() == 0) {
            cityTown.setError(context.getResources().getString(R.string.field_empty));
            valid = false;
        } else {
            cityTown.setError(null);
        }

        String stateString = state.getText().toString().trim();
        if (stateString.length() != 2 || !stateString.matches("[a-zA-Z]+")) {
            state.setError(context.getResources().getString(R.string.state_format));
            valid = false;
        } else {
            state.setError(null);
        }

        String zip = zipCode.getText().toString().trim();
        if (zip.length() != 5 || !zip.matches("[0-9]+")) {
            zipCode.setError(context.getResources().getString(R.string.zip_format));
            valid = false;
        } else {
            zipCode.setError(null);
        }
        return valid;
    }

    private void createClient() {
        final String firstName = this.firstName.getText().toString().trim();
        String lastName = this.lastName.getText().toString().trim();
        String email = this.email.getText().toString().trim();
        String streetAddress = this.streetAddress.getText().toString().trim(); // replaceAll(" ", "+");
        String cityTown = this.cityTown.getText().toString().trim();
        String state = this.state.getText().toString().trim();
        String zipCode = this.zipCode.getText().toString().trim();
        String address = streetAddress + " " + cityTown + "," + state + " " + zipCode;

        // create initial client object
        client = new Client(firstName, lastName, email, address);

        // get internet availability
        boolean netAvailable = Utilities.isInternetAvailable(context);

        if (netAvailable) {
            // run backend request in background thread via async task
            new GetClient().execute();
        } else {
            Toast.makeText(context, R.string.internet_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    private class GetClient extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // create client on backend
            Client result = new ServerDB(context).createClient(client);
            if (result != null) {
                // save client locally
                try {
                    Client newClient = new Client(result);
                    newClient.cascadeSave();
                    noError = true;
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                noError = false;
            }
            noError = false;
            return null;
        }

        protected void onPostExecute(Void param) {
            if (noError) {
                Toast.makeText(context, R.string.client_created, Toast.LENGTH_SHORT).show();
                // reload once creating the client has been completed
                Intent reload = new Intent(context, ClientManagerActivity.class);
                reload.setAction(Params.RELOAD).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                context.startActivity(reload);
                if (context instanceof Activity) {
                    ((Activity) context).finish();
                }
            } else {
                Toast.makeText(context, R.string.error_check_fields, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
