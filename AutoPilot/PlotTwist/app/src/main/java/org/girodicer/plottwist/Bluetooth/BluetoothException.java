package org.girodicer.plottwist.Bluetooth;

/**
 * Created by Carlos on 2/21/2016.
 */
public class BluetoothException extends Exception {
    public enum ERRORS{ARRAY_EMPTY, NO_SIZE, BAD_COMMAND, NO_DATA}

    ERRORS errorNum;

    public BluetoothException(){

    }

    public BluetoothException(String message, ERRORS errorNum){
        super(message);
        this.errorNum = errorNum;
    }

    public BluetoothException(Throwable cause){
        super(cause);
    }

    public BluetoothException(String message, Throwable cause){
        super(message, cause);
    }

}
