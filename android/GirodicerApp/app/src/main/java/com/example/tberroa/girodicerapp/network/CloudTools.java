package com.example.tberroa.girodicerapp.network;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.example.tberroa.girodicerapp.data.Params;

public class CloudTools {

    private final AmazonS3Client amazonS3Client;
    private final TransferUtility transferUtility;

    public CloudTools(Context context){
        CognitoCachingCredentialsProvider cred;
        Regions region = Regions.US_EAST_1;
        cred = new CognitoCachingCredentialsProvider(context, Params.CLOUD_CREDENTIALS, region);
        amazonS3Client = new AmazonS3Client(cred);
        transferUtility = new TransferUtility(amazonS3Client, context);
    }

    public AmazonS3Client getAmazonS3Client(){
        return amazonS3Client;
    }

    public TransferUtility getTransferUtility(){
        return transferUtility;
    }
}