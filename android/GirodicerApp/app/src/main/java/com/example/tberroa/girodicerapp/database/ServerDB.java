package com.example.tberroa.girodicerapp.database;

import android.util.Log;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.models.DroneOperator;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.example.tberroa.girodicerapp.network.HttpPost;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

public class ServerDB {

    public ServerDB() {
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
            postResponse = new HttpPost().doPostRequest(Params.CREATE_CLIENT_URL, dataJSON);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        // convert the response from json to client object
        if (!postResponse.equals("")) {
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

    public int createInspection(DroneOperator operator, int clientId) {
        // get required operator fields
        int userId = operator.user.id;
        String sessionId = operator.session_id;

        // build into json format
        JSONObject innerJson = new JSONObject();
        try {
            innerJson.put("drone_operator_id", userId);
            innerJson.put("client_id", clientId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("user_id", userId);
            requestJson.put("session_id", sessionId);
            requestJson.put("inspection", innerJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String postResponse = "";
        try {
            String dataJSON = requestJson.toString();
            postResponse = new HttpPost().doPostRequest(Params.CREATE_INSPECTION_URL, dataJSON);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        // convert the response from json to inspection object
        if (!postResponse.equals("")) {
            Log.d("dbg", "@ServerDB/createInspection: postResponse is: " + postResponse);

            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            Type inspectionType = new TypeToken<Inspection>() {
            }.getType();
            try {
                Inspection inspection = gson.fromJson(postResponse, inspectionType);
                return inspection.id;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return -1;
        }
        return -1;
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
            postResponse = new HttpPost().doPostRequest(Params.GET_CLIENTS_URL, dataJSON);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        Log.d("dbg", "@ServerDB/getClients: postResponse is: " + postResponse);

        // get client list from response json
        Type clientList = new TypeToken<GetClientsJson>() {
        }.getType();
        try{
            GetClientsJson getClientsJson = new Gson().fromJson(postResponse, clientList);
            return getClientsJson.clients;
        }catch (Exception e){
            return null;
        }
    }

    class GetClientsJson{
        List<Client> clients;
        @SuppressWarnings("unused")
        int provision;

        public GetClientsJson(){
        }
    }
}


