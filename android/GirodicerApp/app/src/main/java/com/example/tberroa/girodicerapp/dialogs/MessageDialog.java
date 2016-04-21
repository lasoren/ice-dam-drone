package com.example.tberroa.girodicerapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;

public class MessageDialog extends Dialog {

    private final String message;

    public MessageDialog(final Context context, String message) {
        super(context, R.style.dialogStyle);
        this.message = message;
        setCancelable(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.element_general_message);

        // initialize text view
        TextView textView = (TextView) findViewById(R.id.general_message);
        textView.setText(message);
    }
}