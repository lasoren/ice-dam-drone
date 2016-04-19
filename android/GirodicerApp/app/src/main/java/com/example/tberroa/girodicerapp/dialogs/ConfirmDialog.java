package com.example.tberroa.girodicerapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;

public class ConfirmDialog extends Dialog {

    private final String message;

    public ConfirmDialog(final Context context, final String message) {
        super(context, R.style.dialogStyle);
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_confirm);

        // initialize text view
        TextView textView = (TextView) findViewById(R.id.message);
        textView.setText(message);

        // initialize buttons
        Button yesButton = (Button) findViewById(R.id.yes_button);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // the caller of the dialog should implement onDismissListener
                dismiss();
            }
        });
        Button noButton = (Button) findViewById(R.id.no_button);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // the caller of the dialog may want implement onCancelListener
                cancel();
            }
        });

        setCancelable(true);
    }
}