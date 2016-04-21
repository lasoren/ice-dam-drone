package com.example.tberroa.girodicerapp.network;

import android.support.annotation.Nullable;
import android.util.Log;

import com.example.tberroa.girodicerapp.data.Params;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Http {

    private final OkHttpClient httpClient = new OkHttpClient();
    private final MediaType mediaType = MediaType.parse(Params.POST_MEDIA_TYPE);

    public Http() {
    }

    @Nullable
    public String postRequest(String url, String jsonString) {
        try {
            RequestBody body = RequestBody.create(mediaType, jsonString);
            Request request = new Request.Builder().url(url).post(body).build();
            Response response = httpClient.newCall(request).execute();
            String rawResponse = response.body().string().trim();
            Log.d(Params.TAG_DBG, "@Http/doPostRequest: rawResponse is: " + rawResponse);
            JSONObject jsonObject;
            jsonObject = new JSONObject(rawResponse);
            int code = jsonObject.optInt("code", 0);
            Log.d(Params.TAG_DBG, "@Http/doPostRequest: code is: " + code);
            if (code == 0) {
                return rawResponse;
            } else {
                return jsonObject.optString("detail", "");
            }
        } catch (Exception e) {
            return null;
        }
    }
}
