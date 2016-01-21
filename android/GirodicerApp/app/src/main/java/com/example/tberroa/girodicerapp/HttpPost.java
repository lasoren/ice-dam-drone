package com.example.tberroa.girodicerapp;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpPost {

    OkHttpClient httpClient = new OkHttpClient();
    private final MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    public String doPostRequest(String url, String keyValuePairs) throws IOException {
        RequestBody body = RequestBody.create(mediaType, keyValuePairs);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = httpClient.newCall(request).execute();
        return response.body().string().trim();
    }



}
