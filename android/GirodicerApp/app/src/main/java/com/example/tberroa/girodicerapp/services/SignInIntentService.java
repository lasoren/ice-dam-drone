package com.example.tberroa.girodicerapp.services;

import android.app.IntentService;
import android.content.Intent;

import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.database.ServerDB;
import com.example.tberroa.girodicerapp.models.Client;

import java.util.List;

public class SignInIntentService extends IntentService {

    public SignInIntentService() {
        super("SignInIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // get clients
        List<Client> clients = new ServerDB().getClients(new LocalDB().getOperator());

        if (clients != null && !clients.isEmpty()) {
            for (int i = 0; i < clients.size(); i++) {
                clients.get(clients.size() - 1 - i).cascadeSave();
            }
        }

        // update user sign in status
        new UserInfo().setUserStatus(this, true);
    }
}
