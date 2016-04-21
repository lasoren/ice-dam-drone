package com.example.tberroa.girodicerapp.network;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.example.tberroa.girodicerapp.data.Params;

final public class CloudTools {

    private static AmazonS3Client amazonS3Client = null;
    private static TransferUtility transferUtility = null;

    private CloudTools(){
    }

    public static TransferUtility getTransferUtility(Context context){
        CognitoCachingCredentialsProvider cred;
        Regions region = Regions.US_EAST_1;
        cred = new CognitoCachingCredentialsProvider(context, Params.CLOUD_CREDENTIALS, region);
        if (amazonS3Client == null){
            amazonS3Client = new AmazonS3Client(cred);
        }
        if (transferUtility == null){
            transferUtility = new TransferUtility(amazonS3Client, context);
        }
        return transferUtility;
    }
}