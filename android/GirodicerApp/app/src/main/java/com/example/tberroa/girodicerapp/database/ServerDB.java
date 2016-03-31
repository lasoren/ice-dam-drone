package com.example.tberroa.girodicerapp.database;

import android.util.Log;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.models.DroneOperator;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.example.tberroa.girodicerapp.models.InspectionImage;
import com.example.tberroa.girodicerapp.network.HttpPost;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
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

    public List<InspectionImage> createInspectionImages(int inspectionId, List<Integer> imageType, String taken) {
        // get operator
        DroneOperator operator = new LocalDB().getOperator();

        // create the list of images to create
        List<CreateImageModel> imagesToCreate = new ArrayList<>();
        for (int type : imageType) {
            imagesToCreate.add(new CreateImageModel(taken, inspectionId, type));
        }

        // create the request json
        CreateImageRequest createImageRequest = new CreateImageRequest(operator.user.id, operator.session_id, imagesToCreate);
        Type createImagesType = new TypeToken<CreateImageRequest>() {
        }.getType();
        String requestJson = new Gson().toJson(createImageRequest, createImagesType);

        // post
        String postResponse = "";
        try {
            Log.d("dbg", "@ServerDB/createInspectionImages: requestJson is: " + requestJson);
            postResponse = new HttpPost().doPostRequest(Params.CREATE_INSPECTION_IMAGES_URL, requestJson);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        // convert the response from json to a list of inspection images
        if (!postResponse.equals("")) {
            Log.d("dbg", "@ServerDB/createInspectionImages: postResponse is: " + postResponse);

            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            Type imagesType = new TypeToken<List<InspectionImage>>() {
            }.getType();
            try {
                return gson.fromJson(postResponse, imagesType);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        return null;
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
        Type clientList = new TypeToken<GetClientsModel>() {
        }.getType();
        try {
            GetClientsModel getClientsModel = new Gson().fromJson(postResponse, clientList);
            return getClientsModel.clients;
        } catch (Exception e) {
            return null;
        }
    }

    public List<Inspection> getInspections(DroneOperator operator) {
        // create the request json
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("user_id", operator.user.id);
            requestJson.put("session_id", operator.session_id);
            requestJson.put("provision", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // post
        String postResponse = "";
        try {
            postResponse = new HttpPost().doPostRequest(Params.GET_INSPECTIONS_URL, requestJson.toString());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        Log.d("dbg", "@ServerDB/getInspections: postResponse is: " + postResponse);

        // get inspection list from response json
        Type inspectionList = new TypeToken<GetInspectionsModel>() {
        }.getType();
        try {
            GetInspectionsModel getInspectionsModel = new Gson().fromJson(postResponse, inspectionList);
            return getInspectionsModel.inspections;
        } catch (Exception e) {
            return null;
        }
    }

    public List<InspectionImage> getInspectionImages(DroneOperator operator) {
        // create the request json
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("user_id", operator.user.id);
            requestJson.put("session_id", operator.session_id);
            requestJson.put("provision", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // post
        String postResponse = "";
        try {
            postResponse = new HttpPost().doPostRequest(Params.GET_INSPECTION_IMAGES_URL, requestJson.toString());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        Log.d("dbg", "@ServerDB/getInspectionImages: postResponse is: " + postResponse);

        // get inspection images list from response json
        Type imagesList = new TypeToken<GetImagesModel>() {
        }.getType();
        try {
            GetImagesModel getImagesModel = new Gson().fromJson(postResponse, imagesList);
            return getImagesModel.inspection_images;
        } catch (Exception e) {
            return null;
        }
    }

    class CreateImageModel {

        String taken;
        int inspection_id;
        int image_type;

        @SuppressWarnings("unused")
        public CreateImageModel() {
        }

        public CreateImageModel(String taken, int inspectionId, int imageType) {
            this.taken = taken;
            this.inspection_id = inspectionId;
            this.image_type = imageType;
        }
    }

    class CreateImageRequest {

        int user_id;
        String session_id;
        List<CreateImageModel> inspection_images;

        @SuppressWarnings("unused")
        public CreateImageRequest() {
        }

        public CreateImageRequest(int user_id, String session_id, List<CreateImageModel> images) {
            this.user_id = user_id;
            this.session_id = session_id;
            this.inspection_images = images;
        }
    }

    class GetClientsModel {

        List<Client> clients;
        @SuppressWarnings("unused")
        int provision;

        public GetClientsModel() {
        }
    }

    class GetInspectionsModel {

        List<Inspection> inspections;
        @SuppressWarnings("unused")
        int provision;

        public GetInspectionsModel() {
        }
    }

    class GetImagesModel {

        @SuppressWarnings("unused")
        int provision;
        List<InspectionImage> inspection_images;

        public GetImagesModel() {
        }
    }
}


