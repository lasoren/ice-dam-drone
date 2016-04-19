package com.example.tberroa.girodicerapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;

public class GotItDialog extends Dialog {

    private final String message;

    public GotItDialog(final Context context, final String message) {
        super(context, R.style.dialogStyle);
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_got_it);

        // initialize text view
        TextView textView = (TextView) findViewById(R.id.message);
        textView.setText(message);

        // initialize button
        Button gotItButton = (Button) findViewById(R.id.got_it_button);
        gotItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // the caller of the dialog should implement onDismissListener
                dismiss();
            }
        });

        setCancelable(false);
    }
}
