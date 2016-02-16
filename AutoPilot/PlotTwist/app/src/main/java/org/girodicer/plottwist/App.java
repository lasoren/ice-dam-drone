package org.girodicer.plottwist;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.UUID;

/**
 * Created by Carlos on 2/16/2016.
 */
public class App extends android.app.Application {
    public static BluetoothAdapter bAdapter;
    public static BluetoothDevice bDevice;
    public static final UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    public void onCreate(){
        super.onCreate();
    }
}
