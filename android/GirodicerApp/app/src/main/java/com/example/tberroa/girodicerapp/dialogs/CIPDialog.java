package com.example.tberroa.girodicerapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.database.ServerDB;
import com.example.tberroa.girodicerapp.helpers.Utilities;

public class CIPDialog extends Dialog {

    private final Context context;
    private final int inspectionId;
    private TextView message;
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
        message = (TextView) findViewById(R.id.general_message);
        message.setText("...");

        // get internet availability
        boolean netAvailable = Utilities.isInternetAvailable(context);

        if (netAvailable) {
            // run backend request in background thread via async task
            new GetURL().execute();
        } else {
            message.setText(R.string.internet_not_available);
        }

        setTitle(R.string.client_inspection_portal);
        setCancelable(true);
    }

    private class GetURL extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            url = new ServerDB(context).getClientInspectionPortal(inspectionId);
            return null;
        }

        protected void onPostExecute(Void param) {
            if (url != null && !url.equals("")) {
                message.setText(url);
            } else {
                message.setText(R.string.error_occurred);
            }
        }
    }
}
