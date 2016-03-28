package com.example.tberroa.girodicerapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.activities.ClientManagerActivity;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.database.ServerDB;
import com.example.tberroa.girodicerapp.models.Client;

import java.util.List;

public class CreateClientDialog extends Dialog {

    private final Context context;
    private EditText firstName, lastName, email, streetAddress, cityTown, state, zipCode;

    public CreateClientDialog(Context context) {
        super(context);
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
                if (validateEntry()) {
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
                    if (validateEntry()) {
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

    private boolean validateEntry() {
        // validate here
        return true;
    }

    private void createClient() {
        String firstName = this.firstName.getText().toString();
        String lastName = this.lastName.getText().toString();
        String email = this.email.getText().toString();
        String streetAddress = this.streetAddress.getText().toString();
        String cityTown = this.cityTown.getText().toString();
        String state = this.state.getText().toString();
        String zipCode = this.zipCode.getText().toString();
        String address = streetAddress + "+" + cityTown + "+" + state + "+" + zipCode;

        // networking needs to be run in background
        final Client client = new Client(firstName, lastName, email, address);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                new ServerDB().createClient(new LocalDB().getOperator(), client);

                // after client is created, get the client back from the server and save them locally
                List<Client> clients = new ServerDB().getClients(new LocalDB().getOperator());
                if (clients  != null && !clients.isEmpty()){
                    clients.get(0).CascadeSave(); // get most recently created client and save them locally
                }
            }
        });
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // reload once creating the client has been completed
        Intent reload = new Intent(context, ClientManagerActivity.class);
        reload.setAction(Params.RELOAD).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(reload);
    }
}
