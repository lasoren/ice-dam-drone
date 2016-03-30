package com.example.tberroa.girodicerapp.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;

import com.example.tberroa.girodicerapp.activities.ClientManagerActivity;
import com.example.tberroa.girodicerapp.activities.SignInActivity;
import com.example.tberroa.girodicerapp.data.CurrentInspectionInfo;
import com.example.tberroa.girodicerapp.data.OperatorId;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.PastInspectionsInfo;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.models.DroneOperator;

import java.io.File;

final public class Utilities {

    private Utilities() {
    }

    private static Point getScreenDimensions(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenDimensions = new Point();
        display.getSize(screenDimensions);
        return screenDimensions;
    }

    private static boolean isLandscape(Context context) {
        boolean bool = false;
        if (getScreenWidth(context) > getScreenHeight(context)) {
            bool = true;
        }
        return bool;
    }

    private static int getScreenWidth(Context context) {
        return getScreenDimensions(context).x;
    }

    private static int getScreenHeight(Context context) {
        return getScreenDimensions(context).y;
    }

    public static int getImageWidthGrid(Context context) {
        return getScreenWidth(context) / getSpanGrid(context);
    }

    public static int getImageHeightGrid(Context context) {
        int imageHeight;
        int screenHeight = getScreenHeight(context);
        int span = getSpanGrid(context);
        if (isLandscape(context)) {
            imageHeight = screenHeight / (span / 2);
        } else {
            imageHeight = screenHeight / (span * 2);
        }
        return imageHeight;
    }

    public static int getSpanGrid(Context context) {
        int span;
        if (isLandscape(context)) {
            span = 4;
        } else {
            span = 2;
        }
        return span;
    }

    public static int getSpacingGrid(Context context) {
        return getScreenWidth(context) / (getSpanGrid(context) * 48);
    }

    public static String constructImageURL(int inspectionId, String imageName) { // always downloading inspection 1 for testing!!
        return Params.CLOUD_URL + inspectionId + "/images/" + imageName;
    }

    public static void deleteDirectory(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteDirectory(child);
            }
        }
        boolean success = false;
        while (!success) {
            success = fileOrDirectory.delete();
        }
    }

    public static String constructImageKey(int inspectionId, String imageName) {
        return inspectionId + "/images/" + imageName;
    }

    public static String validate(Bundle enteredInfo) {
        String validation = "";

        // grab entered information
        String firstName = enteredInfo.getString("first_name", null);
        String lastName = enteredInfo.getString("last_name", null);
        String password = enteredInfo.getString("pass_word", null);
        String confirmPassword = enteredInfo.getString("confirm_password", null);
        String email = enteredInfo.getString("email", null);

        if (firstName != null) {
            firstName = firstName.trim();
            boolean tooLong = firstName.length() > 12;
            if (!firstName.matches("[a-zA-Z]+") || tooLong) {
                validation = validation.concat("first_name");
            }
        }

        if (lastName != null) {
            lastName = lastName.trim();
            boolean tooLong = lastName.length() > 12;
            if (!lastName.matches("[a-zA-Z]+") || tooLong) {
                validation = validation.concat("last_name");
            }
        }

        if (password != null) {
            if (password.length() < 6 || password.length() > 20) {
                validation = validation.concat("pass_word");
            }
        }

        if (confirmPassword != null) {
            if (!confirmPassword.equals(password)) {
                validation = validation.concat("confirm_password");
            }
        }

        if (email != null) {
            email = email.trim();
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                validation = validation.concat("email");
            }
        }
        return validation;
    }

    public static void signIn(Context context, DroneOperator operator) {
        // clear shared preferences of old data
        final OperatorId operatorId = new OperatorId();
        operatorId.clear(context);
        new CurrentInspectionInfo().clearAll(context);
        new PastInspectionsInfo().clearAll(context);

        // save the operators id to shared preference
        operatorId.set(context, operator.id);

        // save this operator to local storage
        operator.cascadeSave();

        // populate test database using real operator account (TEST CODE)
        //new TestCase().Create(operator);

        // say that inspection stuff is up to date (this will be unneeded later)
        new PastInspectionsInfo().setUpToDate(context, true); // TEST CODE!

        // update user sign in status
        new UserInfo().setUserStatus(context, true);

        // go to client manager
        context.startActivity(new Intent(context, ClientManagerActivity.class));
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    public static void signOut(Context context) {
        // clear shared preferences of old data
        new OperatorId().clear(context);
        new CurrentInspectionInfo().clearAll(context);
        new PastInspectionsInfo().clearAll(context);

        // update user sign in status
        new UserInfo().setUserStatus(context, false);

        // clear database
        new LocalDB().Clear();

        // go back to sign in page
        context.startActivity(new Intent(context, SignInActivity.class));
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }
}
