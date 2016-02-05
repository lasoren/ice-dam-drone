package com.example.tberroa.girodicerapp.network;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;

public class CloudServiceTools {

    private final TransferUtility transferUtility;

    public CloudServiceTools(Context context){
        // initialize the Amazon credentials provider, AmazonS3 client, and transfer utility
        final String credentials = "us-east-1:d64bdcf1-1d5e-441e-ba35-0a3876e4c82c";
        final CognitoCachingCredentialsProvider cP = new CognitoCachingCredentialsProvider(context, credentials, Regions.US_EAST_1);
        final AmazonS3Client amazonS3Client = new AmazonS3Client(cP);
        transferUtility = new TransferUtility(amazonS3Client, context);
    }

    public TransferObserver getObserver(String key, File file){
        return transferUtility.upload("girodicer", key, file);
    }
}