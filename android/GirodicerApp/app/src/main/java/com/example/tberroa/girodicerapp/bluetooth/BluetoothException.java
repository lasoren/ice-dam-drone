package com.example.tberroa.girodicerapp.bluetooth;

public class BluetoothException extends Exception {
    public enum ERRORS{ARRAY_EMPTY, NO_SIZE, BAD_COMMAND, NO_DATA}

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final ERRORS errorNum;

    public BluetoothException(String message, ERRORS errorNum){
        super(message);
        this.errorNum = errorNum;
    }
}
