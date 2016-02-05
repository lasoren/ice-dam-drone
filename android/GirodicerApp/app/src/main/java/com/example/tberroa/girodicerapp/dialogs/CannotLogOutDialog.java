package com.example.tberroa.girodicerapp.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.example.tberroa.girodicerapp.R;

public class CannotLogOutDialog extends AlertDialog {

    private final AlertDialog.Builder builder;

    public CannotLogOutDialog(final Context context){
        super(context);

        builder = new AlertDialog.Builder(context);
        builder
                .setMessage(R.string.cannot_logout)
                .setCancelable(false)
                .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
    }

    public AlertDialog getDialog(){
        return builder.create();
    }

}
