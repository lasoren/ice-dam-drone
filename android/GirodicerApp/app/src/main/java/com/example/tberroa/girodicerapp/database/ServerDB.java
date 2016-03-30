package com.example.tberroa.girodicerapp.database;

import android.util.Log;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.models.DroneOperator;
import com.example.tberroa.girodicerapp.models.InspectionImage;
import com.example.tberroa.girodicerapp.network.HttpPost;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

public class ServerDB {

    final public String GET_CLIENTS_URL = Params.BASE_URL + "users/clients/get.json";
    final public String CREATE_CLIENT_URL = Params.BASE_URL + "users/client/create.json";

    public ServerDB() {
    }

    public List<Client> getClients(DroneOperator operator) {
        // get required operator fields
        int userId = operator.user.id;
        String sessionId = operator.session_id;
        int provision = 0; // hard code this for now

        // build into json format
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", userId);
            jsonObject.put("session_id", sessionId);
            jsonObject.put("provision", provision);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String postResponse = "";
        try {
            String dataJSON = jsonObject.toString();
            postResponse = new HttpPost().doPostRequest(GET_CLIENTS_URL, dataJSON);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        Log.d("dbg", "@ServerDB/getClients: postResponse is: " + postResponse);

        // create List<Client> model from response json
        Type clientList = new TypeToken<List<Client>>() {
        }.getType();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.fromJson(postResponse, clientList);
    }

    public Client createClient(DroneOperator operator, Client client) {
        // get required operator fields
        int userId = operator.user.id;
        String sessionId = operator.session_id;

        // build into json format
        // create user json object
        JSONObject userJson = new JSONObject();
        try {
            userJson.put("first_name", client.user.first_name);
            userJson.put("last_name", client.user.last_name);
            userJson.put("email", client.user.email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // create client json object
        JSONObject clientJson = new JSONObject();
        try {
            clientJson.put("user", userJson);
            clientJson.put("address", client.address);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // create client request json object
        JSONObject clientRequestJson = new JSONObject();
        try {
            clientRequestJson.put("user_id", userId);
            clientRequestJson.put("session_id", sessionId);
            clientRequestJson.put("client", clientJson);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String postResponse = "";
        try {
            String dataJSON = clientRequestJson.toString();
            postResponse = new HttpPost().doPostRequest(CREATE_CLIENT_URL, dataJSON);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        // convert the response from json to client object
        if (!postResponse.equals("")) {
            // rename the id fields to be locally compatible
            Log.d("dbg", "@ServerDB/createClient: postResponse is: " + postResponse);

            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            Type clientType = new TypeToken<Client>() {
            }.getType();
            try {
                return gson.fromJson(postResponse, clientType);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        return null;
    }

    public void createInspection(DroneOperator operator, Client client) {
        // create an inspection on the backend
    }

    public void createInspectionImage(InspectionImage inspectionImage) {
        // create an inspection image on the backend
    }
}
