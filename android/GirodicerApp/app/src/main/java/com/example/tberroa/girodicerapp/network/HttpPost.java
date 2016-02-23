package com.example.tberroa.girodicerapp.network;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.helpers.ExceptionHandler;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpPost {

    private final OkHttpClient httpClient = new OkHttpClient();
    private final MediaType mediaType = MediaType.parse(Params.POST_MEDIA_TYPE);

    public HttpPost(){
    }

    public String doPostRequest(String url, String jsonString) throws IOException {
        RequestBody body = RequestBody.create(mediaType, jsonString);
        Request request = new Request.Builder().url(url).post(body).build();
        Response response = httpClient.newCall(request).execute();
        String rawResponse = response.body().string().trim();
        JSONObject jsonObject;
        try{
            jsonObject = new JSONObject(rawResponse);
            int code = jsonObject.optInt("code", 0);
            if(code == 0){
                return rawResponse;
            }
            else{
                return jsonObject.optString("detail", "");
            }
        }catch (Exception e){
            new ExceptionHandler().HandleException(e);
        }
        return "json error occurred";
    }

}
