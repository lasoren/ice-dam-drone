package com.example.tberroa.girodicerapp.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.activities.ActiveInspectionActivity;
import com.example.tberroa.girodicerapp.activities.ClientManagerActivity;
import com.example.tberroa.girodicerapp.activities.PastInspectionsActivity;
import com.example.tberroa.girodicerapp.activities.SignInActivity;
import com.example.tberroa.girodicerapp.data.ActiveInspectionInfo;
import com.example.tberroa.girodicerapp.data.OperatorId;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.PastInspectionsInfo;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.database.TestCase;
import com.example.tberroa.girodicerapp.dialogs.ConfirmEndInspectionDialog;
import com.example.tberroa.girodicerapp.dialogs.MessageDialog;
import com.example.tberroa.girodicerapp.models.DroneOperator;
import com.example.tberroa.girodicerapp.services.DroneService;

import java.io.File;

final public class Utilities {

    private Utilities(){
    }

    private static Point getScreenDimensions(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenDimensions = new Point();
        display.getSize(screenDimensions);
        return screenDimensions;
    }

    private static boolean isLandscape(Context context){
        boolean bool = false;
        if (getScreenWidth(context) > getScreenHeight(context)){
            bool = true;
        }
        return bool;
    }

    private static int getScreenWidth(Context context){
        return getScreenDimensions(context).x;
    }

    private static int getScreenHeight(Context context){
        return getScreenDimensions(context).y;
    }

    public static int getImageWidthGrid(Context context){
        return getScreenWidth(context)/ getSpanGrid(context);
    }

    public static int getImageHeightGrid(Context context){
        int imageHeight;
        int screenHeight = getScreenHeight(context);
        int span = getSpanGrid(context);
        if (isLandscape(context)){
            imageHeight = screenHeight/(span/2);
        }
        else{
            imageHeight = screenHeight/(span*2);
        }
        return imageHeight;
    }

    public static int getSpanGrid(Context context){
        int span;
        if (isLandscape(context)){
            span = 4;
        }
        else{
            span = 2;
        }
        return span;
    }

    public static int getSpacingGrid(Context context){
        return getScreenWidth(context)/(getSpanGrid(context)*48);
    }

    public static String constructImageURL(int inspectionId, String imageName){ // always downloading inspection 1 for testing!!
        return Params.CLOUD_URL +inspectionId+"/images/"+imageName;
    }

    public static void deleteDirectory(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteDirectory(child);
            }
        }
        boolean success = false;
        while(!success){
            success = fileOrDirectory.delete();
        }
    }

    public static String constructImageKey(int inspectionId, String imageName){
        return inspectionId+"/images/"+imageName;
    }

    public static String validate(Bundle enteredInfo){
        String validation = "";

        // grab entered information
        String username = enteredInfo.getString("username", null);
        String password = enteredInfo.getString("password", null);
        String confirmPassword = enteredInfo.getString("confirm_password", null);
        String email = enteredInfo.getString("email", null);

        if (username != null){
            boolean tooShort = username.length() < 3;
            boolean tooLong = username.length() > 15;
            if (!username.matches("[a-zA-Z0-9]+") || tooShort || tooLong ) {
                validation = validation.concat("username");
            }
        }

        if (password != null){
            if (password.length() < 6 || password.length() > 20) {
                validation = validation.concat("password");
            }
        }

        if (confirmPassword != null){
            if (!confirmPassword.equals(password)) {
                validation = validation.concat("confirm_password");
            }
        }

        if (email != null){
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                validation = validation.concat("email");
            }
        }
        return validation;
    }

    public static void attemptInspectionStart(Context context){

        PastInspectionsInfo pMInfo = new PastInspectionsInfo();
        String message;
        if (!pMInfo.isUpToDate(context) || pMInfo.isUpdating(context)){
            message = context.getResources().getString(R.string.past_inspections_not_up_to_date);
            new MessageDialog(context, message).getDialog().show();
        }
        else if (!new ActiveInspectionInfo().isNotInProgress(context)) {
            message = context.getResources().getString(R.string.inspection_in_progress);
            new MessageDialog(context, message).getDialog().show();
        }
        else{
            context.startService(new Intent(context, DroneService.class));
            context.startActivity(new Intent(context, ActiveInspectionActivity.class));
            if(context instanceof Activity){
                ((Activity)context).finish();
            }

        }
    }

    public static void signIn(Context context, DroneOperator operator){
        // clear shared preferences of old data
        final OperatorId operatorId = new OperatorId();
        operatorId.clear(context);
        new ActiveInspectionInfo().clearAll(context);
        new PastInspectionsInfo().clearAll(context);

        // save the operators id to shared preference
        operatorId.set(context, operator.id);

        // save this operator to local storage
        operator.CascadeSave();

        // populate test database using real operator account (TEST CODE)
        new TestCase().Create(operator);
        new PastInspectionsInfo().setUpToDate(context, true); // TEST CODE!

        // update user sign in status
        new UserInfo().setUserStatus(context, true);

        // go to client manager
        context.startActivity(new Intent(context, ClientManagerActivity.class));
        if(context instanceof Activity){
            ((Activity)context).finish();
        }
    }

    public static void signOut(Context context){
        // clear shared preferences of old data
        new OperatorId().clear(context);
        new ActiveInspectionInfo().clearAll(context);
        new PastInspectionsInfo().clearAll(context);

        // update user sign in status
        new UserInfo().setUserStatus(context, false);

        // clear database
        new LocalDB().Clear();

        // go back to sign in page
        context.startActivity(new Intent(context, SignInActivity.class));
        if(context instanceof Activity){
            ((Activity)context).finish();
        }
    }

    public static boolean inspectionMenu(int itemId, Context context){
        ActiveInspectionInfo activeInspectionInfo = new ActiveInspectionInfo();
        Resources resources = context.getResources();

        switch (itemId) {
            case R.id.end_inspection:
                if (activeInspectionInfo.isNotInProgress(context)){
                    String message = resources.getString(R.string.no_active_inspection);
                    new MessageDialog(context, message).getDialog().show();
                }
                else {
                    int inspectionPhase = activeInspectionInfo.getPhase(context);
                    String message;
                    switch(inspectionPhase){
                        case 1:
                            new ConfirmEndInspectionDialog(context).getDialog().show();
                            break;
                        case 2:
                            message = resources.getString(R.string.transfer_phase_text);
                            new MessageDialog(context, message).getDialog().show();
                            break;
                        case 3:
                            message = resources.getString(R.string.upload_phase_text);
                            new MessageDialog(context, message).getDialog().show();
                            break;
                    }
                }
                return true;
            case R.id.start_inspection:
                Utilities.attemptInspectionStart(context);
                return true;
            case R.id.current_inspection:
                context.startActivity(new Intent(context, ActiveInspectionActivity.class));
                if(context instanceof Activity){
                    ((Activity)context).finish();
                }
                return true;
            case R.id.past_inspections:
                context.startActivity(new Intent(context, PastInspectionsActivity.class));
                if(context instanceof Activity){
                    ((Activity)context).finish();
                }
                return true;
            case R.id.sign_out:
                // check if there is an ongoing active inspection
                if (!activeInspectionInfo.isNotInProgress(context)){
                    String message = resources.getString(R.string.cannot_sign_out);
                    new MessageDialog(context, message).getDialog().show();
                }
                else{
                    Utilities.signOut(context);
                }
                return true;
            default:
                return false;
        }
    }
}
