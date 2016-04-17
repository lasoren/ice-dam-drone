package com.example.tberroa.girodicerapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.database.ServerDB;
import com.example.tberroa.girodicerapp.helpers.Utilities;

public class CIPDialog extends Dialog {

    private final Context context;
    private final int inspectionId;
    private boolean noError;
    private String url;

    public CIPDialog(Context context, int inspectionId) {
        super(context, R.style.dialogStyle);
        this.context = context;
        this.inspectionId = inspectionId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.element_general_message);

        // initialize text view
        TextView message = (TextView) findViewById(R.id.general_message);

        // get internet availability
        boolean netAvailable = Utilities.isInternetAvailable(context);

        if (netAvailable) {
            // backend request needs to be run in background
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    url = new ServerDB(context).getClientInspectionPortal(inspectionId);
                    noError = (url != null && !url.equals(""));
                }
            });
            thread.start();

            // wait for backend request to complete before continuing (will change in the future, this is terrible i know)
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (noError) {
                message.setText(url);
            } else {
                message.setText(R.string.error_occurred);
            }
        } else {
            message.setText(R.string.internet_not_available);
        }

        setTitle(R.string.client_inspection_portal);
        setCancelable(true);
    }

}
