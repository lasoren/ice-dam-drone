package com.example.tberroa.girodicerapp.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.services.ActiveMissionService;

public class ConfirmEndMissionDialog extends AlertDialog {

    private final AlertDialog.Builder builder;

    public ConfirmEndMissionDialog(final Context context){
        super(context);

        builder = new AlertDialog.Builder(context);
        builder
                .setMessage(R.string.confirm_end_mission)
                .setCancelable(true)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        context.stopService(new Intent(context, ActiveMissionService.class));
                    }
                });
    }

    public AlertDialog getDialog() {
        return builder.create();
    }
}