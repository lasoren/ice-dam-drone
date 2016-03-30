package com.example.tberroa.girodicerapp.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.services.BluetoothService;

public class EndInspectionDialog extends AlertDialog {

    private final AlertDialog.Builder builder;

    public EndInspectionDialog(final Context context){
        super(context);

        builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.confirm_end_inspection);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                context.stopService(new Intent(context, BluetoothService.class));
            }
        });
    }

    public AlertDialog getDialog() {
        return builder.create();
    }
}