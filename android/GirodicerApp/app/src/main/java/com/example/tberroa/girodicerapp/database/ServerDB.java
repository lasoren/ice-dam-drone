package com.example.tberroa.girodicerapp.database;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.helpers.ExceptionHandler;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.models.DroneOperator;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.example.tberroa.girodicerapp.models.InspectionImage;
import com.example.tberroa.girodicerapp.network.HttpPost;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

public class ServerDB {

    final public String GET_CLIENTS_URL = Params.BASE_URL + "users/clients/get";
    final public String CREATE_CLIENT_URL = Params.BASE_URL + "users/clients/create";

    public ServerDB(){
    }

    public List<Client> getClients(DroneOperator operator){
        // get required operator fields
        int userId = operator.user.id;
        String sessionId = operator.session_id;
        int provision = 0; // hard code this for now

        // build into json format
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("user_id", userId);
            jsonObject.put("session_id", sessionId);
            jsonObject.put("provision", provision);
        }catch (Exception e){
            new ExceptionHandler().HandleException(e);
        }

        String postResponse = "";
        try{
            String dataJSON = jsonObject.toString();
            postResponse = new HttpPost().doPostRequest(GET_CLIENTS_URL, dataJSON);
        }catch(java.io.IOException e){
            new ExceptionHandler().HandleException(e);
        }

        // create List<Client> model from response json
        Type clientList = new TypeToken<List<Client>>(){}.getType();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.fromJson(postResponse, clientList);
    }

    public void createClient(DroneOperator operator, Client client){
        // get required operator fields
        int userId = operator.user.id;
        String sessionId = operator.session_id;

        // build into json format
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("user_id", userId);
            jsonObject.put("session_id", sessionId);
            jsonObject.put("client", client);
        }catch (Exception e){
            new ExceptionHandler().HandleException(e);
        }

        try{
            String dataJSON = jsonObject.toString();
            new HttpPost().doPostRequest(CREATE_CLIENT_URL, dataJSON);
        }catch(java.io.IOException e){
            new ExceptionHandler().HandleException(e);
        }
    }

    public void createInspection(DroneOperator operator, Client client){
        // create an inspection on the backend
    }

    public void createInspectionImage(InspectionImage inspectionImage){
     // create an inspection image on the backend
    }
}
