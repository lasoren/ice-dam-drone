package com.example.tberroa.girodicerapp.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.activities.SignInActivity;
import com.example.tberroa.girodicerapp.activities.SplashActivity;
import com.example.tberroa.girodicerapp.data.CurrentInspectionInfo;
import com.example.tberroa.girodicerapp.data.OperatorInfo;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.models.DroneOperator;
import com.example.tberroa.girodicerapp.services.SignInIntentService;

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

    public static void signIn(Context context, DroneOperator operator, boolean inView) {
        // clear shared preferences of old data
        final OperatorInfo operatorInfo = new OperatorInfo();
        operatorInfo.clear(context);
        new CurrentInspectionInfo().clearAll(context);

        // save operator info
        operatorInfo.setOperatorId(context, operator.id);
        operatorInfo.setUserId(context, operator.user.id);
        operatorInfo.setSessionId(context, operator.session_id);
        operatorInfo.setFirstName(context, operator.user.first_name);
        operatorInfo.setLastName(context, operator.user.last_name);
        operatorInfo.setEmail(context, operator.user.email);

        // save this operator to local storage
        operator.cascadeSave();

        // start sign in intent service
        context.startService(new Intent(context, SignInIntentService.class));

        // go to splash page if app is in view
        if (inView) {
            context.startActivity(new Intent(context, SplashActivity.class));

            // apply sign in animation for entering splash page
            ((Activity) context).overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }

        ((Activity) context).finish();
    }

    public static void signOut(Context context) {
        // clear shared preferences of old data
        new OperatorInfo().clear(context);
        new CurrentInspectionInfo().clearAll(context);

        // update user sign in status
        new UserInfo().setUserStatus(context, false);

        // clear database
        new LocalDB().clear();

        // go back to sign in page
        context.startActivity(new Intent(context, SignInActivity.class));
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }
}
