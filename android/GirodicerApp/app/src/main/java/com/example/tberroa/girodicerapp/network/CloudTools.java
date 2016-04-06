package com.example.tberroa.girodicerapp.network;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.example.tberroa.girodicerapp.data.Params;

final public class CloudTools {

    private static CloudTools instance;
    private static TransferUtility transferUtility;

    private CloudTools(Context context){
        CognitoCachingCredentialsProvider cred;
        Regions region = Regions.US_EAST_1;
        cred = new CognitoCachingCredentialsProvider(context, Params.CLOUD_CREDENTIALS, region);
        AmazonS3Client amazonS3Client = new AmazonS3Client(cred);
        transferUtility = new TransferUtility(amazonS3Client, context);
    }

    private static void initInstance(Context context) {
        if (instance == null) {
            // Create the instance
            instance = new CloudTools(context);
        }
    }

    public static TransferUtility getTransferUtility(Context context){
        initInstance(context);
        return transferUtility;
    }
}