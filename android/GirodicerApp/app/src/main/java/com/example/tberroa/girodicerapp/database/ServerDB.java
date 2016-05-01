package com.example.tberroa.girodicerapp.database;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.tberroa.girodicerapp.data.OperatorInfo;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.Provisions;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.example.tberroa.girodicerapp.models.InspectionImage;
import com.example.tberroa.girodicerapp.network.Http;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ServerDB {

    private final Context context;
    private final int operatorUserId;
    private final String sessionId;

    public ServerDB(Context context) {
        this.context = context;
        OperatorInfo operatorInfo = new OperatorInfo();
        operatorUserId = operatorInfo.getUserId(context);
        sessionId = operatorInfo.getSessionId(context);
    }

    @Nullable
    public Client createClient(Client client) {
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
            clientRequestJson.put("user_id", operatorUserId);
            clientRequestJson.put("session_id", sessionId);
            clientRequestJson.put("client", clientJson);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // make request
        String dataJSON = clientRequestJson.toString();
        String postResponse = new Http().postRequest(Params.CREATE_CLIENT_URL, dataJSON);
        Log.d(Params.TAG_DBG, "@ServerDB/createClient: postResponse is: " + postResponse);

        // convert the response from json to client object
        if (postResponse != null) {
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

    @Nullable
    public Inspection createInspection(int clientId) {
        // build into json format
        JSONObject innerJson = new JSONObject();
        try {
            innerJson.put("drone_operator_id", operatorUserId);
            innerJson.put("client_id", clientId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("user_id", operatorUserId);
            requestJson.put("session_id", sessionId);
            requestJson.put("inspection", innerJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // make request
        String dataJSON = requestJson.toString();
        String postResponse = new Http().postRequest(Params.CREATE_INSPECTION_URL, dataJSON);
        Log.d(Params.TAG_DBG, "@ServerDB/createInspection: postResponse is: " + postResponse);

        // convert the response from json to inspection object
        if (postResponse != null) {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            Type inspectionType = new TypeToken<Inspection>() {
            }.getType();
            try {
                return gson.fromJson(postResponse, inspectionType);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        return null;
    }

    @Nullable
    public List<InspectionImage> createInspectionImages(int inspectionId, List<Integer> imageType, String taken) {
        // create the list of images to create
        List<CreateImageModel> imagesToCreate = new ArrayList<>();
        for (int type : imageType) {
            imagesToCreate.add(new CreateImageModel(taken, inspectionId, type));
        }

        // create the request json
        CreateImageRequest createImageRequest = new CreateImageRequest(operatorUserId, sessionId, imagesToCreate);
        Type createImagesType = new TypeToken<CreateImageRequest>() {
        }.getType();
        String requestJson = new Gson().toJson(createImageRequest, createImagesType);

        // make request
        String postResponse = new Http().postRequest(Params.CREATE_INSPECTION_IMAGES_URL, requestJson);
        Log.d(Params.TAG_DBG, "@ServerDB/createInspectionImages: postResponse is: " + postResponse);

        // convert the response from json to a list of inspection images
        if (postResponse != null) {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            Type imagesType = new TypeToken<List<InspectionImage>>() {
            }.getType();
            try {
                return gson.fromJson(postResponse, imagesType);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Nullable
    public List<Client> getClients() {
        // get provision
        Provisions provisions = new Provisions();
        int provision = provisions.getClients(context);

        // build into json format
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", operatorUserId);
            jsonObject.put("session_id", sessionId);
            jsonObject.put("provision", provision);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String dataJSON = jsonObject.toString();
        String postResponse = new Http().postRequest(Params.GET_CLIENTS_URL, dataJSON);
        Log.d(Params.TAG_DBG, "@ServerDB/getClients: postResponse is: " + postResponse);

        if (postResponse != null) {
            // get client list from response json
            Type clientList = new TypeToken<GetClientsModel>() {
            }.getType();
            try {
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                GetClientsModel getClientsModel = gson.fromJson(postResponse, clientList);
                provisions.setClients(context, getClientsModel.provision);
                return getClientsModel.clients;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Nullable
    public List<Inspection> getInspections() {
        // get provision
        Provisions provisions = new Provisions();
        int provision = provisions.getInspections(context);

        // create the request json
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("user_id", operatorUserId);
            requestJson.put("session_id", sessionId);
            requestJson.put("provision", provision);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // post
        String postResponse = new Http().postRequest(Params.GET_INSPECTIONS_URL, requestJson.toString());
        Log.d(Params.TAG_DBG, "@ServerDB/getInspections: postResponse is: " + postResponse);

        if (postResponse != null) {
            // get inspection list from response json
            Type inspectionList = new TypeToken<GetInspectionsModel>() {
            }.getType();
            try {
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                GetInspectionsModel getInspectionsModel = gson.fromJson(postResponse, inspectionList);
                provisions.setInspections(context, getInspectionsModel.provision);
                return getInspectionsModel.inspections;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Nullable
    public List<InspectionImage> getInspectionImages() {
        // get provision
        Provisions provisions = new Provisions();
        int provision = provisions.getInspectionImages(context);

        // create the request json
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("user_id", operatorUserId);
            requestJson.put("session_id", sessionId);
            requestJson.put("provision", provision);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // post
        String postResponse = new Http().postRequest(Params.GET_INSPECTION_IMAGES_URL, requestJson.toString());
        Log.d(Params.TAG_DBG, "@ServerDB/getInspectionImages: postResponse is: " + postResponse);

        if (postResponse != null) {
            // get inspection images list from response json
            Type imagesList = new TypeToken<GetImagesModel>() {
            }.getType();
            try {
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                GetImagesModel getImagesModel = gson.fromJson(postResponse, imagesList);
                provisions.setInspectionImages(context, getImagesModel.provision);
                return getImagesModel.inspection_images;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Nullable
    public String getClientInspectionPortal(int inspectionId) {
        // create the request json
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("user_id", operatorUserId);
            requestJson.put("session_id", sessionId);
            requestJson.put("inspection_id", inspectionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // post
        String postResponse = new Http().postRequest(Params.CLIENT_INSPECTION_PORTAL, requestJson.toString());
        Log.d(Params.TAG_DBG, "@ServerDB/getClientInspectionPortal: postResponse is: " + postResponse);

        if (postResponse != null) {
            try {
                JSONObject jsonObject = new JSONObject(postResponse);
                return jsonObject.optString("url", "");
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    class CreateImageModel {

        String taken;
        int inspection_id;
        int image_type;

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

        public CreateImageRequest() {
        }

        public CreateImageRequest(int user_id, String session_id, List<CreateImageModel> images) {
            this.user_id = user_id;
            this.session_id = session_id;
            this.inspection_images = images;
        }
    }

    class GetClientsModel {

        @Expose
        List<Client> clients;
        @Expose
        int provision;

        public GetClientsModel() {
        }
    }

    class GetInspectionsModel {

        @Expose
        List<Inspection> inspections;
        @Expose
        int provision;

        public GetInspectionsModel() {
        }
    }

    class GetImagesModel {

        @Expose
        int provision;
        @Expose
        List<InspectionImage> inspection_images;

        public GetImagesModel() {
        }
    }
}


