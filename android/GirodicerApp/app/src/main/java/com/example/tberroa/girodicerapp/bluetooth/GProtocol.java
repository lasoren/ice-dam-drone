package com.example.tberroa.girodicerapp.bluetooth;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Carlos on 2/21/2016.
 *
 * This class is for handling the communication protocol between the mission planner and the android app
 *
 * Possible commands are defined
 *
 * Incoming messages must have the protocol message 'Unpacked' then have the message be 'read'
 *  To know what object is being return upon 'read', the command must be deciphered
 */

public class GProtocol {
    public static final byte COMMAND_ARM = 0x1;
    public static final byte COMMAND_UNARM = 0x2;
    public static final byte COMMAND_START_INSPECTION = 0x3;
    public static final byte COMMAND_END_INSPECTION = 0x4;
    public static final byte COMMAND_STATUS = 0x5;
    public static final byte COMMAND_SEND_POINTS = 0x6;
    public static final byte COMMAND_READY_TO_TRANSFER = 0x7;
    public static final byte COMMAND_NEW_HOUSE = 0x8;
    public static final byte COMMAND_BLUETOOTH_SEND_PATH = 0x9;
    public static final byte COMMAND_BLUETOOTH_SEND_IMAGES_RGB = 0xA;
    public static final byte COMMAND_BLUETOOTH_SEND_IMAGES_THERM = 0xB;
    public static final byte COMMAND_BLUETOOTH_RETURN_HOME = 0xC;
    public static final byte COMMAND_BLUETOOTH_SEND_JSON_RGB = 0xD;
    public static final byte COMMAND_BLUETOOTH_SEND_JSON_THERM = 0xE;

    public static final byte PARTIAL_MESSAGE = (byte) 0x80;

    private final byte command;
    private final byte[] data;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final boolean partial;

    @SuppressWarnings("SameParameterValue")
    public static byte[] Pack(byte command, int payloadSize, byte[] array, boolean partial){
        ByteBuffer builder = ByteBuffer.allocate(payloadSize + 1 + 4).order(ByteOrder.LITTLE_ENDIAN);

        if(partial){
            command |= PARTIAL_MESSAGE;
        }
        builder.put(command);
        builder.putInt(payloadSize);
        builder.put(array);

        return builder.array();
    }

    public static GProtocol Unpack(byte[] array) throws BluetoothException{
        ByteBuffer builder = ByteBuffer.wrap(array);

        if(!builder.hasRemaining()){ // array passed in has nothing
            throw new BluetoothException("Empty Array", BluetoothException.ERRORS.ARRAY_EMPTY);
        }

        byte receivedCommand = builder.get();

        switch(receivedCommand){
            case COMMAND_ARM:
            case COMMAND_UNARM:
            case COMMAND_START_INSPECTION:
            case COMMAND_END_INSPECTION:
            case COMMAND_READY_TO_TRANSFER:
                return new GProtocol(receivedCommand, null, false);
            case COMMAND_STATUS:
            case COMMAND_SEND_POINTS:
            case COMMAND_BLUETOOTH_SEND_PATH:
                boolean partial = ((receivedCommand & PARTIAL_MESSAGE) != 0x0);
                if(builder.hasRemaining()){
                    int payloadSize = builder.getInt();
                    byte[] data = new byte[payloadSize];

                    if(builder.hasRemaining()){
                        builder.get(data);
                        return new GProtocol(receivedCommand, data, partial);
                    } else {
                        throw new BluetoothException("No Data", BluetoothException.ERRORS.NO_DATA);
                    }

                } else {
                    throw new BluetoothException("No payload size", BluetoothException.ERRORS.NO_SIZE);
                }
            case COMMAND_BLUETOOTH_SEND_IMAGES_RGB:

            case COMMAND_BLUETOOTH_SEND_IMAGES_THERM:

            case COMMAND_BLUETOOTH_RETURN_HOME:

            case COMMAND_BLUETOOTH_SEND_JSON_RGB:

            case COMMAND_BLUETOOTH_SEND_JSON_THERM:

            default:
                throw new BluetoothException("Bad Command", BluetoothException.ERRORS.BAD_COMMAND);
        }
    }

    private GProtocol(byte command, byte[] data, boolean partial){
        this.command = command;
        this.data = data.clone();
        this.partial = partial;
    }

    public byte getCommand(){
        return this.command;
    }

    public Object read(){
        switch(this.command){
            case COMMAND_STATUS:
                return Status.Unpack(this.data);
            case COMMAND_SEND_POINTS:
            case COMMAND_BLUETOOTH_SEND_PATH:
                return Points.Unpack(this.data);
        }
        return null;
    }
}
